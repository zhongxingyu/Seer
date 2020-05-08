 package gutenberg.workers;
 
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.ManifestType;
 import gutenberg.blocs.QuestionTagsType;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardCopyOption;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 
 public class Vault {
 
     public Vault(Config config) {
         vaultPath = config.getPath(Resource.vault);
         sharedPath = config.getPath(Resource.shared);
     }
 
     public Vault() throws Exception {
         Config config = new Config();
         vaultPath = config.getPath(Resource.vault);
         sharedPath = config.getPath(Resource.shared);
     }
 
     /**
      * @param id
      *            - question id
      * @param filter
      *            - type of content e.g. "tex", "gnuplot" etc.
      * @return returns file contents for given id
      * @throws Exception
      */
     public String[] getContent(String id, String filter) throws Exception {
         File directory = new File(vaultPath + "/" + id);
         File[] files = directory.listFiles(new NameFilter(filter));
         String[] contents = new String[files.length];
         for (int i = 0; i < files.length; i++) {
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Files.copy(files[i].toPath(), baos);
             contents[i] = baos.toString();
         }
         return contents;
     }
 
     /**
      * @param id
      *            - question id
      * @param filter
      *            - type of content e.g. "tex", "gnuplot" etc.
      * @return Files
      * @throws Exception
      */
     public File[] getFiles(String id, String filter) throws Exception {
         File directory = new File(vaultPath + "/" + id);
         return directory.listFiles(new NameFilter(filter));
     }
 
     /**
      * Creates a question in the Vault
      * 
      * @param quizMasterId
      *            - id of person creating question
      * @return Manifest
      * @throws Exception
      */
     public ManifestType createQuestion(String quizMasterId) throws Exception {
 
         String hexTimestamp = Long.toString(System.currentTimeMillis(),
                 Character.MAX_RADIX);
         String dirName = quizMasterId + "-" + hexTimestamp.substring(0, 3)
                 + "-" + hexTimestamp.substring(3);
         Path questionDir = vaultPath.resolve(dirName);
         Files.createDirectory(questionDir);
 
         Files.copy(sharedPath.resolve("templates").resolve(texFile), 
                 questionDir.resolve(texFile));
         Files.copy(sharedPath.resolve("templates").resolve(plotFile), 
                 questionDir.resolve(plotFile));
         Files.copy(sharedPath.resolve("templates").resolve(bc2FigFile),
                 questionDir.resolve(bc2FigFile));
         Path rel = questionDir.relativize(sharedPath.resolve("makefiles")
                 .resolve(makeFile));
         Files.createSymbolicLink(questionDir.resolve("Makefile"), rel);
 
         ManifestType manifest = new ManifestType();
         manifest.setRoot(questionDir.toString());
         return manifest;
     }
 
     /**
      * 
      * @param tags
      *            - properties to be encoded in question.tex file
      * @return Manifest
      * @throws Exception
      */
     public ManifestType tagQuestion(QuestionTagsType tags) throws Exception {
 
         String id = tags.getId();
         String marks = "";
         String length = "";
         int[] breaks = tags.getBreaks();
         if (breaks == null) {
             breaks = new int[0];
         }
 
         Path questionTex = vaultPath.resolve(id).resolve(texFile);
         BufferedReader reader = new BufferedReader(new FileReader(
                 questionTex.toFile()));
 
         Path questionTexTmp = questionTex.resolveSibling(texFile + ".tmp");
         PrintWriter writer = new PrintWriter(new FileWriter(
                 questionTexTmp.toFile()));
 
         int partIdx = 0;
         int pageIdx = 0;
         String line = null, trimmed = null;
         while ((line = reader.readLine()) != null) {
             trimmed = line.trim();
             if (trimmed.startsWith(questionTag)) {
                 if (tags.getMarks().length == 1) {// no parts to the question
                     marks = String
                             .format(marksFormat, tags.getMarks()[partIdx]);
                     line = line.replaceFirst(
                             Matcher.quoteReplacement(questionTag) + marksRegex,
                             Matcher.quoteReplacement(questionTag) + marks);
                 }
             } else if (trimmed.startsWith(solutionTag)) {
                 length = String.format(lengthFormat, tags.getLength()[partIdx]);
                 line = solutionTag + length;
                 partIdx++;
             } else if (trimmed.startsWith(partTag)) {
                 if (tags.getMarks().length > 1) {// multiple part question
                     marks = String
                             .format(marksFormat, tags.getMarks()[partIdx]);
                     line = line.replaceFirst(Matcher.quoteReplacement(partTag)
                             + marksRegex, Matcher.quoteReplacement(partTag)
                             + marks);
                     // tricky bit, insert a new page before next part starts
                     if (breaks.length > pageIdx) {
                         if (partIdx == breaks[pageIdx] + 1) {
                             writer.println(newpage);
                             writer.println(insertQRTag.replace("{}", "{QRC}"));
                             pageIdx++;
                         }
                     }
                 }
             } else if (trimmed.startsWith(insertQRTag)) {
                line = line.replaceFirst("\\{.*\\}", "{QRC}");
             } else if (trimmed.equals(newpage)) {
                 continue;
             }
             writer.println(line);
         }
         writer.flush();
         writer.close();
         Files.move(questionTexTmp, questionTex,
                 StandardCopyOption.REPLACE_EXISTING);
 
         ManifestType manifest = new ManifestType();
         manifest.setRoot(questionTex.getParent().getFileName().toString());
 
         String[] pages = questionTex.getParent().toFile().list();
         EntryType image = null;
         ArrayList<EntryType> images = new ArrayList<EntryType>();
         for (String filename : pages) {
             if (filename.matches("page-[\\d]+.jpeg")) {
                 image = new EntryType();
                 image.setId(filename);
                 images.add(image);
             }
         }
         EntryType[] imagarray = new EntryType[images.size()];
         manifest.setImage(images.toArray(imagarray));
         return manifest;
     }
 
     private Path vaultPath, sharedPath;
     private final String texFile = "question.tex", plotFile = "figure.gnuplot",
             bc2FigFile = "figure.bc", makeFile = "individual.mk";
 
     private final String questionTag = "\\question",
             solutionTag = "\\begin{solution}", partTag = "\\part",
             lengthFormat = "[\\%s]", marksFormat = "[%s]",
             marksRegex = "\\[?[1-9]?\\]?", newpage = "\\nextpg",
             insertQRTag = "\\insertQR";
 }
