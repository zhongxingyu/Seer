 package tools;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 public class ConfigFileGenerator {
 
    public static void main(final String[] args) throws IOException {
         final File templateFile = new File(args[0]);
         final String templateFileName = templateFile.getName();
         final String templateName = templateFileName.substring(0, templateFileName.lastIndexOf('.'));
         final File configRootDir = new File(args[1]);
 
         final LinkedHashMap<String, List<TemplateEntry>> entries = new LinkedHashMap<String, List<TemplateEntry>>();
         final Properties template = new Properties();
         template.load(new FileReader(templateFile));
 
         for (String name: template.stringPropertyNames()) {
             final String value = template.getProperty(name);
             for (int start = value.indexOf('[', 0); start != -1; start = value.indexOf('[', start)) {
                 final int end = value.indexOf(']', start + 1);
                 final String entryStr = value.substring(start + 1, end);
                 final String[] parts = entryStr.split(",");
                 final BigDecimal min = new BigDecimal(parts[0].trim());
                 final BigDecimal max = new BigDecimal(parts[1].trim());
                 final BigDecimal inc;
                 if (parts.length > 2) {
                     inc = new BigDecimal(parts[2].trim());
                 } else {
                     inc = new BigDecimal(1);
                 }
                 List<TemplateEntry> list = entries.get(name);
                 if (list == null) {
                     list = new ArrayList<TemplateEntry>();
                     entries.put(name, list);
                 }
                 list.add(new TemplateEntry(min, max, inc, start, end));
                 start = end + 1;
             }
         }
 
         Map<String, TemplateEntry> fileNameEntries = new HashMap<String, TemplateEntry>();
         for (String name: template.stringPropertyNames()) {
             final List<TemplateEntry> templateEntries = entries.get(name);
             if (templateEntries.size() > 1) {
                 fileNameEntries.clear();
                 break;
             }
             if (templateEntries.size() == 1) {
                 fileNameEntries.put(name, templateEntries.get(0));
             }
         }
         final TemplateIdGenerator idGenerator;
         if (fileNameEntries.size() > 1) {
             final Set<String> names = fileNameEntries.keySet();
             final int prefixLen = getCommonLen(names, true);
             final int suffixLen = getCommonLen(names, false);
             if (prefixLen + suffixLen > 0) {
                 idGenerator = new MultiEntryIdGenerator(fileNameEntries, prefixLen, suffixLen);
             } else {
                 idGenerator = new IncrementalIdGenerator();
             }
         } else if (fileNameEntries.size() == 1) {
             idGenerator = new SingleEntryIdGenerator(fileNameEntries.values().iterator().next());
         } else {
             idGenerator = new IncrementalIdGenerator();
         }
 
         final File targetDir = new File(configRootDir, templateName);
         targetDir.mkdirs();
         boolean hasNext = true;
         int templateCount = 0;
         while (hasNext) {
             final Properties config = new Properties();
             for (String name: template.stringPropertyNames()) {
                 String value = template.getProperty(name);
                 final List<TemplateEntry> list = entries.get(name);
                 if (list != null) {
                     final StringBuilder builder = new StringBuilder(value);
                     final ListIterator<TemplateEntry> iter = list.listIterator(list.size());
                     while (iter.hasPrevious()) {
                         final TemplateEntry entry = iter.previous();
                         builder.replace(entry.getStartPos(), entry.getEndPos() + 1, entry.getCounter().toPlainString());
                     }
                     value = builder.toString();
                 }
                 config.setProperty(name, value);
             }
             config.store(
                 new FileWriter(new File(targetDir, String.format("%s-%s.ini", templateName, idGenerator.getId()))),
                 "Config #" + templateCount);
             templateCount++;
             hasNext = increment(entries);
         }
     }
 
     private static int getCommonLen(final Set<String> names, final boolean prefixMatch) {
         int len = 0;
         int dotPos = 0;
         for (boolean match = true; match; ) {
             char ch = (char) -1;
             for (String name: names) {
                 if (name.length() <= len) {
                     match = false;
                     break;
                 }
                 final int pos;
                 if (prefixMatch) {
                     pos = len;
                 } else {
                     pos = name.length() - 1 - len;
                 }
                 if (ch == (char) -1) {
                     ch = name.charAt(pos);
                 } else {
                     if (name.charAt(pos) != ch) {
                         match = false;
                         break;
                     }
                 }
             }
             if (match) {
                 len++;
                 if (ch == '.') {
                     dotPos = len;
                 }
             }
         }
         return dotPos;
     }
 
     private static boolean increment(final LinkedHashMap<String, List<TemplateEntry>> entries) {
         for (List<TemplateEntry> list: entries.values()) {
             for (TemplateEntry entry: list) {
                 if (entry.incCounter()) {
                     return true;
                 } else {
                     entry.resetCounter();
                 }
             }
         }
         return false;
     }
 
     private static interface TemplateIdGenerator {
         public String getId();
     }
 
     private static class IncrementalIdGenerator implements TemplateIdGenerator {
         private int counter = 0;
         @Override
         public String getId() {
             return String.valueOf(counter++);
         }
     }
 
     private static class SingleEntryIdGenerator implements TemplateIdGenerator {
         private final TemplateEntry entry;
 
         public SingleEntryIdGenerator(final TemplateEntry entry) {
             this.entry = entry;
         }
 
         @Override
         public String getId() {
             return entry.getCounter().toPlainString();
         }
     }
 
     private static class MultiEntryIdGenerator implements TemplateIdGenerator {
         private final Map<String, TemplateEntry> entries;
         private final int prefixLen;
         private final int suffixLen;
 
         public MultiEntryIdGenerator(final Map<String, TemplateEntry> entries, final int prefixLen, final int suffixLen) {
             this.entries = entries;
             this.prefixLen = prefixLen;
             this.suffixLen = suffixLen;
         }
 
         @Override
         public String getId() {
             final StringBuilder builder = new StringBuilder();
             for (Map.Entry<String, TemplateEntry> entry: entries.entrySet()) {
                 if (builder.length() > 0) {
                     builder.append("-");
                 }
                 final String name = entry.getKey();
                 builder.append(name.substring(prefixLen, name.length() - suffixLen));
                 builder.append("@");
                 builder.append(entry.getValue().getCounter().toPlainString());
             }
             return builder.toString();
         }
     }
 
     private static class TemplateEntry {
         private final BigDecimal min;
         private final BigDecimal max;
         private final BigDecimal inc;
         private final int startPos;
         private final int endPos;
         private BigDecimal counter;
 
         private TemplateEntry(final BigDecimal min, final BigDecimal max, final BigDecimal inc,
                               final int startPos, final int endPos) {
             this.min = min;
             this.max = max;
             this.inc = inc;
             this.startPos = startPos;
             this.endPos = endPos;
             counter = min;
         }
 
         public int getStartPos() {
             return startPos;
         }
 
         public int getEndPos() {
             return endPos;
         }
 
         public BigDecimal getCounter() {
             return counter;
         }
 
         public void resetCounter() {
             counter = min;
         }
 
         public boolean incCounter() {
             counter = counter.add(inc);
             return counter.compareTo(max) <= 0;
         }
     }
 }
