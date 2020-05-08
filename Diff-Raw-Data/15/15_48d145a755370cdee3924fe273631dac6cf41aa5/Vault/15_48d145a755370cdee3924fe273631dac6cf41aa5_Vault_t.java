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
 
 public class Vault implements ITagLib {
 
     public Vault(Config config) {
         vaultPath = config.getPath(Resource.vault);
         sharedPath = config.getPath(Resource.shared);
         commonPath = config.getPath(Resource.common);
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
      * @param examinerId
      *            - id of person creating question
      * @return Manifest
      * @throws Exception
      */
     public ManifestType createQuestion(String examinerId) throws Exception {
 
         String timeStamp = Long.toString(System.currentTimeMillis(), Character.MAX_RADIX);
         String reversed = new StringBuffer(timeStamp).reverse().toString() ;
         String levelOne = reversed.substring(5),
         	   levelTwo = reversed.substring(0,5);
         
         // eg. 1/ozs/yuuw6
         String dirName = String.format("%s/%s/%s", examinerId, levelOne, levelTwo);
         
         Path questionDir = vaultPath.resolve(dirName);
         Path templatesDir = sharedPath.resolve(templates);
         String[] files = {texFile, plotFile, bc2FigFile};
         
         Files.createDirectories(questionDir);
         
         // Copy question.tex etc. 
         for(String f: files) {
         	Files.copy(templatesDir.resolve(f), questionDir.resolve(f)) ;
         }
         
         // Ensure that Makefiles are present all the way down to examinerId/levelOne
         String[] hierarchy = {examinerId, examinerId + "/" + levelOne} ;
         Path     toMake = null ;
         for(String h: hierarchy) {
         	toMake = vaultPath.resolve(h).resolve(makeFile);
         	if (!Files.exists(toMake)){ 
         		Files.createSymbolicLink(toMake, sharedPath.resolve(makefiles).resolve(diveDown));
         	}
         }
         
         // As a last step, add a Makefile within levelTwo
         Files.createSymbolicLink(questionDir.resolve(makeFile),
                commonPath.resolve("makefiles").resolve(individual));
         Files.createSymbolicLink(questionDir.resolve("shell-script"),
                 commonPath.resolve("scripts").resolve("compile.sh"));
         
         ManifestType manifest = new ManifestType();
         manifest.setRoot(dirName); 
         
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
         
         Path questionTexTmp = questionTex.resolveSibling(texFile + ".tmp");
         PrintWriter writer = new PrintWriter(new FileWriter(
                 questionTexTmp.toFile()));
 
         int partIdx = 0;
         int pageIdx = 0;
         String line = null, trimmed = null;        
         
         try (BufferedReader reader = 
                 new BufferedReader(new FileReader(questionTex.toFile()))) {
             while ((line = reader.readLine()) != null) {
                 trimmed = line.trim();
                 if (trimmed.startsWith(question)) {
                     if (tags.getMarks().length == 1) {// no parts to the question
                         marks = String
                                 .format(marksFormat, tags.getMarks()[partIdx]);
                         line = line.replaceFirst(
                                 Matcher.quoteReplacement(question) + marksRegex,
                                 Matcher.quoteReplacement(question) + marks);
                     }
                 } else if (trimmed.startsWith(solution)) {
                     length = String.format(lengthFormat, tags.getLength()[partIdx]);
                     line = solution + length;
                     partIdx++;
                 } else if (trimmed.startsWith(part)) {
                     if (tags.getMarks().length > 1) {// multiple part question
                         marks = String
                                 .format(marksFormat, tags.getMarks()[partIdx]);
                         line = line.replaceFirst(
                                 Matcher.quoteReplacement(part) + marksRegex, 
                                 Matcher.quoteReplacement(part) + marks);
                         // tricky bit, insert a new page before next part starts
                         if (breaks.length > pageIdx) {
                             if (partIdx == breaks[pageIdx] + 1) {
                                 writer.println(newpage);
                                 pageIdx++;
                             }
                         }
                     }
                 } else if (trimmed.equals(newpage)) {
                     continue;
                 }
                 writer.println(line);
             }            
         }
         
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
 
     private Path vaultPath, sharedPath, commonPath;
     private final String texFile = "question.tex", 
                          plotFile = "figure.gnuplot",
                          bc2FigFile = "figure.bc", 
                          individual = "vault.mk",
                          diveDown = "divedown-vault.mk",
                          makeFile = "Makefile",
                          templates = "templates",
                          makefiles = "makefiles";
 
     private final String lengthFormat = "[\\%s]", marksFormat = "[%s]",
             marksRegex = "\\[?[1-9]?\\]?";
 }
