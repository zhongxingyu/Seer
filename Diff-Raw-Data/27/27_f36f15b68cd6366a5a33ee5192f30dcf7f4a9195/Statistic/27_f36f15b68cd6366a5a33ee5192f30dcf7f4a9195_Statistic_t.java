 package hudson.plugins.javancss.parser;
 
 import hudson.model.AbstractBuild;
 import hudson.util.IOException2;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * TODO javadoc.
  *
  * @author Stephen Connolly
  * @since 25-Feb-2008 21:33:40
  */
 public class Statistic implements Serializable {
 // ------------------------------ FIELDS ------------------------------
 
     private AbstractBuild<?, ?> owner;
     private String name;
     private long classes;
     private long functions;
     private long ncss;
     private long javadocs;
     private long javadocLines;
     private long singleCommentLines;
     private long multiCommentLines;
 
 // -------------------------- STATIC METHODS --------------------------
 
     public static Collection<Statistic> parse(File inFile) throws IOException, XmlPullParserException {
         Collection<Statistic> results = new ArrayList<Statistic>();
         FileInputStream fis = null;
         BufferedInputStream bis = null;
         try {
             fis = new FileInputStream(inFile);
             bis = new BufferedInputStream(fis);
             XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
             factory.setNamespaceAware(true);
             factory.setValidating(false);
             XmlPullParser parser = factory.newPullParser();
             parser.setInput(bis, null);
 
             // check that the first tag is <javancss>
             expectNextTag(parser, "javancss");
 
             // skip until we get to the <packages> tag
             while (parser.getDepth() > 0 && (parser.getEventType() != XmlPullParser.START_TAG || !"packages".equals(parser.getName()))) {
                 parser.next();
             }
             while (parser.getDepth() > 0 && (parser.getEventType() != XmlPullParser.START_TAG || !"package".equals(parser.getName()))) {
                 parser.next();
             }
             while (parser.getDepth() >= 2 && parser.getEventType() == XmlPullParser.START_TAG && "package".equals(parser.getName())) {
                 Map<String, String> data = new HashMap<String, String>();
                 String lastTag = null;
                 String lastText = null;
                 int depth = parser.getDepth();
                 while (parser.getDepth() >= depth) {
                     parser.next();
                     switch (parser.getEventType()) {
                         case XmlPullParser.START_TAG:
                             lastTag = parser.getName();
                             break;
                         case XmlPullParser.TEXT:
                             lastText = parser.getText();
                             break;
                         case XmlPullParser.END_TAG:
                             if (parser.getDepth() == 4 && lastTag != null && lastText != null) {
                                 data.put(lastTag, lastText);
                             }
                             lastTag = null;
                             lastText = null;
                             break;
                     }
                 }
                 if (data.containsKey("name")) {
                     Statistic s = new Statistic(data.get("name"));
                     s.setClasses(Long.valueOf(data.get("classes")));
                     s.setFunctions(Long.valueOf(data.get("functions")));
                     s.setNcss(Long.valueOf(data.get("ncss")));
                     s.setJavadocs(Long.valueOf(data.get("javadocs")));
                     s.setJavadocLines(Long.valueOf(data.get("javadoc_lines")));
                     s.setSingleCommentLines(Long.valueOf(data.get("single_comment_lines")));
                     s.setMultiCommentLines(Long.valueOf(data.get("multi_comment_lines")));
                     results.add(s);
                 }
                 parser.next();
             }
 
 
         } catch (XmlPullParserException e) {
             throw new IOException2(e);
         } finally {
             if (bis != null) {
                 bis.close();
             }
             if (fis != null) {
                 fis.close();
             }
         }
         return results;
     }
 
     private static boolean skipToTag(XmlPullParser parser, String tagName)
             throws IOException, XmlPullParserException {
         while (true) {
             if (parser.getEventType() == XmlPullParser.END_TAG) {
                 return false;
             }
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 parser.next();
                 continue;
             }
             if (parser.getName().equals(tagName)) {
                 return true;
             }
             skipTag(parser);
         }
     }
 
     private static void skipTag(XmlPullParser parser)
             throws IOException, XmlPullParserException {
         parser.next();
         endElement(parser);
     }
 
     private static void expectNextTag(XmlPullParser parser, String tag)
             throws IOException, XmlPullParserException {
         while (true) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 parser.next();
                 continue;
             }
             if (parser.getName().equals(tag)) {
                 return;
             }
             throw new IOException("Expecting tag " + tag);
         }
     }
 
     private static void endElement(XmlPullParser parser)
             throws IOException, XmlPullParserException {
         int depth = parser.getDepth();
         while (parser.getDepth() >= depth) {
             parser.next();
         }
     }
 
     public static Statistic total(Collection<Statistic>... results) {
         Collection<Statistic> merged = merge(results);
         Statistic total = new Statistic("");
         for (Statistic individual : merged) {
             total.add(individual);
         }
         return total;
     }
 
     public void add(Statistic r) {
         classes += r.classes;
         functions += r.functions;
         ncss += r.ncss;
         javadocs += r.javadocs;
         javadocLines += r.javadocLines;
         singleCommentLines += r.singleCommentLines;
         multiCommentLines += r.multiCommentLines;
     }
 
     public static Collection<Statistic> merge(Collection<Statistic>... results) {
        Collection<Statistic> newResults = new ArrayList<Statistic>();
         if (results.length == 0) {
             return Collections.emptySet();
         } else if (results.length == 1) {
             return results[0];
         } else {
            
            List<String> indivNames = new ArrayList<String>();
             for (Collection<Statistic> result : results) {
                 for (Statistic individual : result) {
                    if (!indivNames.contains(individual.name)) {
                        indivNames.add(individual.name);
                     }
                 }
             }

            for (String indivName : indivNames) {
                Statistic indivStat = new Statistic(indivName);
                for (Collection<Statistic> result : results) {
                    for (Statistic individual : result) {
                        if (indivName.equals(individual.name)) {
                            indivStat.add(individual);
                        }
                    }
                }
                newResults.add(indivStat);
            }
            return newResults;
         }
     }
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     public Statistic(String name) {
         this.name = name;
     }
 
 // --------------------- GETTER / SETTER METHODS ---------------------
 
     public long getClasses() {
         return classes;
     }
 
     public void setClasses(long classes) {
         this.classes = classes;
     }
 
     public long getFunctions() {
         return functions;
     }
 
     public void setFunctions(long functions) {
         this.functions = functions;
     }
 
     public long getJavadocLines() {
         return javadocLines;
     }
 
     public void setJavadocLines(long javadocLines) {
         this.javadocLines = javadocLines;
     }
 
     public long getJavadocs() {
         return javadocs;
     }
 
     public void setJavadocs(long javadocs) {
         this.javadocs = javadocs;
     }
 
     public long getMultiCommentLines() {
         return multiCommentLines;
     }
 
     public void setMultiCommentLines(long multiCommentLines) {
         this.multiCommentLines = multiCommentLines;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public long getNcss() {
         return ncss;
     }
 
     public void setNcss(long ncss) {
         this.ncss = ncss;
     }
 
     public AbstractBuild<?, ?> getOwner() {
         return owner;
     }
 
     public void setOwner(AbstractBuild<?, ?> owner) {
         this.owner = owner;
     }
 
     public long getSingleCommentLines() {
         return singleCommentLines;
     }
 
     public void setSingleCommentLines(long singleCommentLines) {
         this.singleCommentLines = singleCommentLines;
     }
 
 // ------------------------ CANONICAL METHODS ------------------------
 
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Statistic statistic = (Statistic) o;
 
         if (classes != statistic.classes) return false;
         if (functions != statistic.functions) return false;
         if (javadocLines != statistic.javadocLines) return false;
         if (javadocs != statistic.javadocs) return false;
         if (multiCommentLines != statistic.multiCommentLines) return false;
         if (ncss != statistic.ncss) return false;
         if (singleCommentLines != statistic.singleCommentLines) return false;
         if (!name.equals(statistic.name)) return false;
         if (owner != null ? !owner.equals(statistic.owner) : statistic.owner != null) return false;
 
         return true;
     }
 
     public int hashCode() {
         int result;
         result = (owner != null ? owner.hashCode() : 0);
         result = 31 * result + name.hashCode();
         return result;
     }
 
     public String toString() {
         return "Statistic{" +
                 "name='" + name + '\'' +
                 ", classes=" + classes +
                 ", functions=" + functions +
                 ", ncss=" + ncss +
                 ", javadocs=" + javadocs +
                 ", javadocLines=" + javadocLines +
                 ", singleCommentLines=" + singleCommentLines +
                 ", multiCommentLines=" + multiCommentLines +
                 '}';
     }
 
     public String toSummary() {
         return "<ul>"
                 + diff(0, classes, "classes")
                 + diff(0, functions, "functions")
                 + diff(0, ncss, "ncss")
                 + diff(0, javadocs, "javadocs")
                 + diff(0, javadocLines, "javadoc lines")
                 + diff(0, singleCommentLines, "single line comments")
                 + diff(0, multiCommentLines, "multi-line comments")
                 + "</ul>";
     }
 
     private static String diff(long a, long b, String name) {
         if (a == b) {
             return "";
         } else if (a < b) {
             return "<li>" + name + " (+" + (b - a) + ")</li>";
         } else { // if (a < b)
             return "<li>" + name + " (-" + (a - b) + ")</li>";
         }
     }
 
     public String toSummary(Statistic totals) {
         return "<ul>"
                 + diff(totals.classes, classes, "classes")
                 + diff(totals.functions, functions, "functions")
                 + diff(totals.ncss, ncss, "ncss")
                 + diff(totals.javadocs, javadocs, "javadocs")
                 + diff(totals.javadocLines, javadocLines, "javadoc lines")
                 + diff(totals.singleCommentLines, singleCommentLines, "single line comments")
                 + diff(totals.multiCommentLines, multiCommentLines, "multi-line comments")
                 + "</ul>";
     }
 
     public void set(Statistic that) {
         this.name = that.name;
         this.classes = that.classes;
         this.functions = that.functions;
         this.ncss = that.ncss;
         this.javadocs = that.javadocs;
         this.javadocLines = that.javadocLines;
         this.singleCommentLines = that.singleCommentLines;
         this.multiCommentLines = that.multiCommentLines;
     }
 }
