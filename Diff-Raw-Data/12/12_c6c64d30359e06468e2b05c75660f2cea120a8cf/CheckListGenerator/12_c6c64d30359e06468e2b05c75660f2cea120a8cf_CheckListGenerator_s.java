 /*
  * Copyright 2012 EMBL - European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or impl
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.ebi.fg.annotare2.magetabcheck;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Strings;
 import com.google.common.collect.Ordering;
 import org.reflections.Reflections;
 import org.reflections.scanners.MethodAnnotationsScanner;
 import org.reflections.scanners.TypeAnnotationsScanner;
 import uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckModality;
 import uk.ac.ebi.fg.annotare2.magetabcheck.checker.annotation.MageTabCheck;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static com.google.common.io.CharStreams.newWriterSupplier;
 import static com.google.common.io.CharStreams.write;
 import static com.google.common.io.Files.newOutputStreamSupplier;
 import static com.google.inject.internal.util.$Maps.newHashMap;
 
 /**
  * @author Olga Melnichuk
  */
 public class CheckListGenerator {
 
     private static final Pattern REF_PATTERN = Pattern.compile("([a-zA-Z]+)[0-9]*");
 
     private static final Map<String, String> prefixes = newHashMap();
 
     static {
         prefixes.put("G", "General");
         prefixes.put("TS", "Term Source");
         prefixes.put("PR", "Protocol");
         prefixes.put("PB", "Publication");
         prefixes.put("C", "Contact");
         prefixes.put("EF", "Experimental Factor");
         prefixes.put("ED", "Experimental Design");
         prefixes.put("QC", "Quality Control Type");
         prefixes.put("NT", "Normalization Type");
         prefixes.put("RT", "Replicate Type");
     }
 
     private final String packageName;
 
     private final String title;
 
     public CheckListGenerator(String packageName, String title) {
         this.packageName = packageName;
         this.title = title;
     }
 
     public static void main(String... args) {
         toMarkdown("uk.ac.ebi.fg.annotare2.magetabcheck.checks.idf", "IDF checks", "./doc/idf-checks.md");
         toMarkdown("uk.ac.ebi.fg.annotare2.magetabcheck.checks.sdrf", "SDRF checks", "./doc/sdrf-checks.md");
     }
 
     private static void toMarkdown(String packageName, String title, String fileOut) {
         try {
             (new CheckListGenerator(packageName, title)).generateMarkdown(new File(fileOut));
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private void generateMarkdown(final File out) throws IOException {
         if (!out.exists()) {
             File parent = out.getParentFile();
             if (!parent.exists() && !parent.mkdirs()) {
                 throw new IllegalStateException("Can't create directories " + parent);
             }
         }
         write(markdown(), newWriterSupplier(newOutputStreamSupplier(out), Charsets.UTF_8));
     }
 
     private String markdown() {
         Reflections reflections = new Reflections(packageName,
                 new TypeAnnotationsScanner(), new MethodAnnotationsScanner());
 
         List<MageTabCheck> checks = new ArrayList<MageTabCheck>();
         for (Class<?> clazz : reflections.getTypesAnnotatedWith(MageTabCheck.class)) {
             checks.add(clazz.getAnnotation(MageTabCheck.class));
         }
         for (Method method : reflections.getMethodsAnnotatedWith(MageTabCheck.class)) {
             checks.add(method.getAnnotation(MageTabCheck.class));
         }
 
         checks = Ordering.from(new Comparator<MageTabCheck>() {
             @Override
             public int compare(MageTabCheck o1, MageTabCheck o2) {
                 int prefixDiff = prefix(o1.ref()).compareTo(prefix(o2.ref()));
                 if (prefixDiff != 0) {
                     return prefixDiff;
                 }
                 int modalityDiff = o1.modality().compareTo(o2.modality());
                 return modalityDiff == 0 ? o1.ref().compareTo(o2.ref()) : modalityDiff;
             }
 
             private String prefix(String str) {
                 Matcher m = REF_PATTERN.matcher(str);
                 return (m.matches()) ? m.group(1) : str;
             }
         }).sortedCopy(checks);
 
         MarkdownChecks markdown = new MarkdownChecks();
         markdown.header1(title);
         String prefix = null;
         for (MageTabCheck check : checks) {
             if (prefix == null || !check.ref().startsWith(prefix)) {
                 Matcher m = REF_PATTERN.matcher(check.ref());
                 if (m.matches()) {
                     prefix = m.group(1);
                     if (prefixes.containsKey(prefix)) {
                         markdown.header2(prefixes.get(prefix) + " Checks");
                     }
                     markdown.checksStart();
                 } else {
                     throw new IllegalStateException("Wrong REF format: " + check.ref());
                 }
             }
             markdown.addCheck(check);
         }
         return markdown.toString();
     }
 
     private static class MarkdownChecks {
         private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
         private final StringBuilder title = new StringBuilder();
         private final StringBuilder text = new StringBuilder();
         private final StringBuilder toc = new StringBuilder();
        private int hc = 0;
 
         public void header1(String header) {
             title.append("# ")
                     .append(header)
                     .append("\n(updated: ")
                     .append(dateFormat.format(new Date()))
                     .append(")\n\n");
         }
 
         public void header2(String header) {
            hc++;
            text.append("\n## <a id=\"head").append(hc).append("\"></a>")
                     .append(header)
                     .append("\n");
            toc.append("+ [").append(header).append("](#head").append(hc).append(")\n");
         }
 
         public void checksStart() {
             text.append("\n");
             for (Column c : Column.values()) {
                 text.append("|");
                 text.append(c.getTitle());
             }
             text.append("|\n");
             for (Column c : Column.values()) {
                 text.append("|");
                 text.append(Strings.repeat("-", c.getTitle().length()));
             }
             text.append("|\n");
         }
 
         public void addCheck(MageTabCheck check) {
             for (Column c : Column.values()) {
                 text.append("|");
                 text.append(c.getValue(check));
 
             }
             text.append("|\n");
         }
 
         @Override
         public String toString() {
             return title.toString() + toc.toString() + text.toString();
         }
     }
 
     private static enum Column {
         REF("Ref") {
             @Override
             String getValue(MageTabCheck annot) {
                 return annot.ref();
             }
         },
         MODALITY("Modality") {
             @Override
             String getValue(MageTabCheck annot) {
                 CheckModality m = annot.modality();
                 String str = m.toString();
                 return m.isError() ? "**" + str + "**" : str;
             }
         },
         TYPE("Type") {
             @Override
             String getValue(MageTabCheck annot) {
                 return annot.application().toString();
             }
         },
         TITLE("Title") {
             @Override
             String getValue(MageTabCheck annot) {
                 return annot.value();
             }
         },
         DETAILS("Details") {
             @Override
             String getValue(MageTabCheck annot) {
                 return "TBA";
             }
         };
 
         private final String title;
 
         private Column(String title) {
             this.title = title;
         }
 
         private String getTitle() {
             return title;
         }
 
         abstract String getValue(MageTabCheck annot);
     }
 }
