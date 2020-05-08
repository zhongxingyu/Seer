 
 package org.eclipse.releng.build.tools.comparator;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * This class is responsible for extracting the relevent "Debug" messages from
  * the huge maven debug log.
  * 
  * @author davidw
  * 
  */
 public class Extractor {
 
     public final static String  BUILD_DIRECTORY_PROPERTY = "builddirectory";
     private static final String EOL                      = System.getProperty("line.separator", "\n");
 
     public static void main(final String[] args) {
         final Extractor extractor = new Extractor();
         if (args.length > 0) {
             extractor.setBuildDirectory(args[0]);
         }
         // set explicitly for local test
         // extractor.setBuildDirectory("/home/davidw/temp/I20130417-1750zz");
         try {
             extractor.processBuildfile();
         }
         catch (final IOException e) {
             e.printStackTrace();
         }
     }
 
     private final String  debugFilename                         = "mb060_run-maven-build_output.txt";
     private final String  outputFilenameFull                    = "buildtimeComparatorFull.log";
     private final String  outputFilenameSign                    = "buildtimeComparatorSignatureOnly.log";
     private final String  outputFilenameDoc                     = "buildtimeComparatorDocBundle.log";
     private final String  outputFilenameOther                   = "buildtimeComparatorUnanticipated.log";
     private final String  buildlogsDirectory                    = "buildlogs";
     private final String  comparatorLogsDirectory              = "comparatorlogs";
     private String        buildDirectory;
     private String        inputFilename;
     private String        outputFilenameFullLog;
     private String        outputFilenameSignLog;
     private String        outputFilenameDocLog;
     private String        outputFilenameOtherLog;
     private final String  mainregexPattern                      = "^\\[WARNING\\].*eclipse.platform.releng.aggregator/(.*)/pom.xml: baseline and build artifacts have same version but different contents";
     private final Pattern mainPattern                           = Pattern.compile(mainregexPattern);
     private final String  noclassifierregexPattern              = "^.*no-classifier:.*$";
     private final Pattern noclassifierPattern                   = Pattern.compile(noclassifierregexPattern);
     private final String  classifier_sourcesregexPattern        = "^.*classifier-sources:.*$";
     private final Pattern classifier_sourcesPattern             = Pattern.compile(classifier_sourcesregexPattern);
     private final String  classifier_sourcesfeatureregexPattern = "^.*classifier-sources-feature:.*$";
     private final Pattern classifier_sourcesfeaturePattern      = Pattern.compile(classifier_sourcesfeatureregexPattern);
 
     private final String  sign1regexPattern                     = "^.*META-INF/ECLIPSE_.RSA.*$";
     private final Pattern sign1Pattern                          = Pattern.compile(sign1regexPattern);
     private final String  sign2regexPattern                     = "^.*META-INF/ECLIPSE_.SF.*$";
     private final Pattern sign2Pattern                          = Pattern.compile(sign2regexPattern);
     private final String  docNameregexPattern                   = "^.*eclipse\\.platform\\.common.*\\.doc\\..*$";
     private final Pattern docNamePattern                        = Pattern.compile(docNameregexPattern);
     private int           count;
     private int           countSign;
     private int           countDoc;
     private int           countOther;
 
     public Extractor() {
 
     }
 
     private boolean docItem(final LogEntry newEntry) {
         boolean result = false;
         final String name = newEntry.getName();
         final Matcher matcher = docNamePattern.matcher(name);
         if (matcher.matches()) {
             result = true;
         }
         return result;
     }
 
     public String getBuildDirectory() {
         // if not set explicitly, see if its a system property
         if (buildDirectory == null) {
             buildDirectory = System.getProperty(BUILD_DIRECTORY_PROPERTY);
         }
         return buildDirectory;
     }
 
     private String getInputFilename() {
         if (inputFilename == null) {
            inputFilename = getBuildDirectory() + "/" + buildlogsDirectory + "/" + debugFilename;
         }
         return inputFilename;
     }
 
     private String getOutputFilenameDoc() {
         if (outputFilenameDocLog == null) {
             outputFilenameDocLog = getBuildDirectory() + "/" + buildlogsDirectory + "/" + comparatorLogsDirectory + "/"  + outputFilenameDoc;
         }
         return outputFilenameDocLog;
     }
 
     private String getOutputFilenameFull() {
         if (outputFilenameFullLog == null) {
             outputFilenameFullLog = getBuildDirectory() + "/" + buildlogsDirectory + "/" + comparatorLogsDirectory + "/"  + outputFilenameFull;
         }
         return outputFilenameFullLog;
     }
 
     private String getOutputFilenameOther() {
         if (outputFilenameOtherLog == null) {
             outputFilenameOtherLog = getBuildDirectory() + "/" + buildlogsDirectory + "/" + comparatorLogsDirectory + "/"  + outputFilenameOther;
         }
         return outputFilenameOtherLog;
     }
 
     private String getOutputFilenameSign() {
         if (outputFilenameSignLog == null) {
             outputFilenameSignLog = getBuildDirectory() + "/" + buildlogsDirectory + "/" + comparatorLogsDirectory + "/"  + outputFilenameSign;
         }
         return outputFilenameSignLog;
     }
 
     public void processBuildfile() throws IOException {
         
         // Make sure directory exists
         File outputDir = new File(getBuildDirectory() + "/" + buildlogsDirectory, comparatorLogsDirectory);
         if (!outputDir.exists()) {
             outputDir.mkdirs();
         }
         
         final File infile = new File(getInputFilename());
         final Reader in = new FileReader(infile);
         BufferedReader input = null;
         input = new BufferedReader(in);
         final File outfile = new File(getOutputFilenameFull());
         final Writer out = new FileWriter(outfile);
         final BufferedWriter output = new BufferedWriter(out);
 
         final File outfileSign = new File(getOutputFilenameSign());
         final Writer outsign = new FileWriter(outfileSign);
         final BufferedWriter outputSign = new BufferedWriter(outsign);
 
         final File outfileDoc = new File(getOutputFilenameDoc());
         final Writer outdoc = new FileWriter(outfileDoc);
         final BufferedWriter outputDoc = new BufferedWriter(outdoc);
 
         final File outfileOther = new File(getOutputFilenameOther());
         final Writer outother = new FileWriter(outfileOther);
         final BufferedWriter outputOther = new BufferedWriter(outother);
 
         writeHeader(output);
         writeHeader(outputSign);
         writeHeader(outputDoc);
         writeHeader(outputOther);
         count = 0;
         countSign = 0;
         countDoc = 0;
         countOther = 0;
         try {
             String inputLine = "";
 
             while (inputLine != null) {
                 inputLine = input.readLine();
                 if (inputLine != null) {
                     final Matcher matcher = mainPattern.matcher(inputLine);
                     if (matcher.matches()) {
 
                         final LogEntry newEntry = new LogEntry();
                         newEntry.setName(matcher.group(1));
                         // read and write differences, until next blank line
                         do {
                             inputLine = input.readLine();
                             if ((inputLine != null) && (inputLine.length() > 0)) {
                                 newEntry.addReason(inputLine);
                             }
                         }
                         while ((inputLine != null) && (inputLine.length() > 0));
                         // //output.write(EOL);
                         // now, do one more, to get the "info" that says
                         // what was copied, or not.
                         do {
                             inputLine = input.readLine();
                             if ((inputLine != null) && (inputLine.length() > 0)) {
                                 // except leave out the first line, which is a
                                 // long [INFO] line repeating what we already
                                 // know.
                                 if (!inputLine.startsWith("[INFO]")) {
                                     newEntry.addInfo(inputLine);
                                 }
                             }
                         }
                         while ((inputLine != null) && (inputLine.length() > 0));
                         // Write full log, for sanity check, if nothing else
                         writeEntry(++count, output, newEntry);
                         if (docItem(newEntry)) {
                             writeEntry(++countDoc, outputDoc, newEntry);
                         } else if (pureSignature(newEntry)) {
                             writeEntry(++countSign, outputSign, newEntry);
                         } else {
                             writeEntry(++countOther, outputOther, newEntry);
                         }
                     }
                 }
             }
         }
         finally {
             if (input != null) {
                 input.close();
             }
             if (output != null) {
                 output.close();
             }
             if (outputSign != null) {
                 outputSign.close();
             }
             if (outputDoc != null) {
                 outputDoc.close();
             }
             if (outputOther != null) {
                 outputOther.close();
             }
         }
     }
 
     private void writeHeader(final BufferedWriter output) throws IOException {
         output.write("Comparator differences from current build" + EOL);
         output.write("\t" + getBuildDirectory() + EOL);
         output.write("\t\t" + "compared to reference repo at .../eclipse/updates/4.3-I-builds" + EOL + EOL);
     }
 
     private boolean pureSignature(final LogEntry newEntry) {
         // if all lines match one of these critical patterns,
         // then assume "signature only" difference. If even
         // one of them does not match, assume not.
         boolean result = true;
         final List<String> reasons = newEntry.getReasons();
         for (final String reason : reasons) {
             final Matcher matcher1 = noclassifierPattern.matcher(reason);
             final Matcher matcher2 = classifier_sourcesPattern.matcher(reason);
             final Matcher matcher3 = classifier_sourcesfeaturePattern.matcher(reason);
             final Matcher matcher4 = sign1Pattern.matcher(reason);
             final Matcher matcher5 = sign2Pattern.matcher(reason);
 
             if (matcher1.matches() || matcher2.matches() || matcher3.matches() || matcher4.matches() || matcher5.matches()) {
                 continue;
             } else {
                 result = false;
                 break;
             }
         }
 
         return result;
     }
 
     public void setBuildDirectory(final String buildDirectory) {
         this.buildDirectory = buildDirectory;
     }
 
     private void writeEntry(int thistypeCount, final Writer output, final LogEntry newEntry) throws IOException {
 
         output.write(thistypeCount + ".  " + newEntry.getName() + EOL);
         final List<String> reasons = newEntry.getReasons();
         for (final String reason : reasons) {
             output.write(reason + EOL);
         }
         final List<String> infolist = newEntry.getInfo();
         for (final String info : infolist) {
             output.write(info + EOL);
         }
         output.write(EOL);
     }
 }
