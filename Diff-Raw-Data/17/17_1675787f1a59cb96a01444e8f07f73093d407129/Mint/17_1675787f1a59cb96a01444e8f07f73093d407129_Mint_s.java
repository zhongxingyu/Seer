 package gutenberg.workers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardCopyOption;
 import java.nio.file.StandardOpenOption;
 import java.util.List;
 import java.util.Random;
 
 import gutenberg.blocs.AssignmentType;
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.ManifestType;
 import gutenberg.blocs.PageType;
 import gutenberg.blocs.QuizType;
 
 public class Mint {
 
     public Mint(Config config) throws Exception {
         sharedPath = new File(config.getPath(Resource.shared)).toPath();
         mintPath = new File(config.getPath(Resource.mint)).toPath();
         vault = new Vault(config);
         atm = ATM.instance(config);
         locker = new Locker(config);
     }
 
     /**
      * Creates PDFs and preview jpegs for a quiz. Steps are: 0. Set up
      * directories 1. Write latex 2. Link Makefiles 3. Make (the PDFs) 4.
      * Populate and return manifest
      * 
      * @param quiz
      * @return manifest containing generated files
      * @throws Exception
      * 
      */
     public ManifestType generate(QuizType quiz) throws Exception {
 
         // 0. Set up directories
         String quizId = quiz.getQuiz().getId();
         Path quizDir = Files.createDirectories(mintPath.resolve(quizId)
                 .resolve("answer-key"));
         Path staging = Files.createDirectory(quizDir.resolve("staging"));
         Path downloads = Files.createDirectory(quizDir.resolve("downloads"));
         Path preview = Files.createDirectory(quizDir.resolve("preview"));
         // generate symbolic link to quiz in ATM
         ManifestType manifest = atm.deposit(quizDir.getParent());
 
         // 1. write latex
         PrintWriter answerKeyTex = new PrintWriter(Files.newBufferedWriter(
                 staging.resolve("answer-key.tex"), StandardCharsets.UTF_8,
                 StandardOpenOption.CREATE));
 
         PageType[] pages = quiz.getPage();
         // Write any information that might be pertinent during
         // test paper generation - like # of pages at the beginning
         answerKeyTex.println(String.format("%% num_pages=%s",
                 pages[pages.length - 1].getNumber()));
 
         writePreamble(answerKeyTex, quiz.getSchool().getName(), quiz
                 .getTeacher().getName());
         beginDoc(answerKeyTex);
         answerKeyTex.println(printanswers);
         for (int i = 0; i < pages.length; i++) {
 
             EntryType[] questions = pages[i].getQuestion();
             if (questions == null)
                 continue;
 
             for (EntryType question : questions) {
                 setQuestion(question.getId(), answerKeyTex, staging);
             }
             answerKeyTex.println(newpage);
         }
         endDoc(answerKeyTex);
         answerKeyTex.close();
 
         // 2. Link Makefiles and make PDFs
         Path rel = staging.relativize(sharedPath);
         Files.createSymbolicLink(staging.resolve("Makefile"),
                 rel.resolve("answer-key.mk"));
         Files.createSymbolicLink(staging.resolve("test-paper.mk"),
                 rel.resolve("test-paper.mk"));
 
         if (make(staging, downloads, preview) != 0) {
             throw new Exception("Problem! Non-zero return code from make");
         }
 
         // 3. Populate manifest
         String[] filenames = preview.toFile().list();
         if (filenames.length == 0) {
             throw new Exception("All preview files not prepared.");
         }
         EntryType[] images = new EntryType[filenames.length];
         for (int i = 0; i < images.length; i++) {
             images[i] = new EntryType();
             images[i].setId(filenames[i]);
         }
         manifest.setImage(images);
 
         EntryType[] documents = new EntryType[1];
         if (!Files.exists(downloads.resolve("answer-key.pdf"))) {
             throw new Exception("answer-key.pdf is missing!");
         }
         documents[0] = new EntryType();
         documents[0].setId("answer-key.pdf");
         manifest.setDocument(documents);
         
         return manifest;
     }
 
     /**
      * Create an assignment (test paper). Steps: 0. Set up directories 1. Copy
      * blueprint from answer-key.tex 2. Write composite and per student latex
      * and QRCode key files 3. Make (the PDFs) 4. Populate and return manifest
      * 
      * @param assignment
      * @return manifest
      * @throws Exception
      */
     public ManifestType generate(AssignmentType assignment) throws Exception {
 
         // 0. Set up directories
         String quizId = assignment.getQuiz().getId();
         String testpaperId = assignment.getInstance().getId();
         Path quizDir = mintPath.resolve(quizId);
         Path blueprintDir = quizDir.resolve("answer-key/staging");
         Path assignmentDir = Files
                 .createDirectory(quizDir.resolve(testpaperId));
         Path staging = Files.createDirectory(assignmentDir.resolve("staging"));
         Path downloads = Files.createDirectory(assignmentDir
                 .resolve("downloads"));
         // generate symbolic link to test paper in ATM
         ManifestType manifest = atm.deposit(assignmentDir);
 
         // 1. Copy blueprint answer-key.tex
         List<String> linesList = Files.readAllLines(
                 blueprintDir.resolve("answer-key.tex"), StandardCharsets.UTF_8);
         String[] lines = linesList.toArray(new String[linesList.size()]);
         int totalPages = Integer.parseInt(lines[0].split("=")[1]);
 
         // 2. Write latex files for each student's test paper
         String compositeNameFormat = "assignment-%s-%s.%s";
         Path compositeTex = staging.resolve(String.format(compositeNameFormat,
                 quizId, testpaperId, "tex"));
         PrintWriter composite = new PrintWriter(
                 Files.newBufferedWriter(compositeTex, StandardCharsets.UTF_8,
                         StandardOpenOption.CREATE));
 
         // Randomized manifest root is needed for key file
         String atmKey = new File(manifest.getRoot()).toPath().getFileName()
                 .toString();
 
         Path keyFile = assignmentDir.resolve("keyFile");
         PrintWriter keyFileWriter = new PrintWriter(Files.newBufferedWriter(
                 keyFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE));
 
         int MAX_2_DIG_BASE36_NUM = 1296;
         Random random = new Random();
        String singleNameFormat = "%s-%s-%s.%s", pseudoStudentId = null;
         EntryType[] students = assignment.getStudents();
         for (int i = 0; i < students.length; i++) {
 
             Path singleTex = staging.resolve(String.format(singleNameFormat,
                     quizId, testpaperId, students[i].getId(), "tex"));
             PrintWriter single = new PrintWriter(Files.newBufferedWriter(
                     singleTex, StandardCharsets.UTF_8,
                     StandardOpenOption.CREATE));
 
             // QRKey = [TestPaperId(6)][studentIdx(2)][pageNum(1/2)]
             pseudoStudentId = Integer.toString(
                     random.nextInt(MAX_2_DIG_BASE36_NUM), Character.MAX_RADIX);
             String QRKey = String.format("%s%2s", atmKey, pseudoStudentId)
                     .replace(' ', '0');
             replicateBlueprint(lines, composite, single, QRKey, 
                     students[i].getName(), (i == 0));
 
             String baseQR = baseQR(students[i], assignment);
             String QRKeyVal = null;
             int pageNumber = 0;
             for (int j = 0; j < totalPages; j++) {
 
                 pageNumber = j + 1;
                 QRKeyVal = String.format("%s%s:%s-0-%s-%s", QRKey, pageNumber,
                         baseQR, pageNumber, totalPages);
                 keyFileWriter.println(QRKeyVal);
 
             }
 
             if (totalPages % 2 != 0) {
                 insertBlankPage(composite);
             }
 
             resetQuestionNumbering(composite);
             resetPageNumbering(composite);
         }
         endDoc(composite);
         composite.close();
         keyFileWriter.close();
 
         // copy plot files from blueprint folder
         File[] plotfiles = quizDir.resolve("answer-key").resolve("staging")
                 .toFile().listFiles(new NameFilter(".gnuplot"));
         for (int i = 0; i < plotfiles.length; i++) {
             Files.copy(plotfiles[i].toPath(),
                     staging.resolve(plotfiles[i].getName()));
         }
 
         // 3. Link Makefiles and make the PDFs
         Path rel = staging.relativize(sharedPath);
         Files.createSymbolicLink(staging.resolve("Makefile"),
                 rel.resolve("test-paper.mk"));
         
         if (make(staging, downloads, null) != 0)
             throw new Exception("Problem! Non-zero return code from make");
 
         // 4. Prepare manifest
         EntryType[] documents = new EntryType[students.length + 1];
         for (int i = 0; i < students.length; i++) {
 
             String filename = String.format(singleNameFormat, quizId,
                     testpaperId, students[i].getId(), "pdf");
 
             if (Files.exists(downloads.resolve(filename))) {
                 documents[i] = new EntryType();
                 documents[i].setId(filename);
             } else {
                 throw new Exception(String.format("Individual %s missing!",
                         filename));
             }
         }
 
         String compositePDF = String.format(compositeNameFormat, quizId,
                testpaperId, ".pdf");
        if (Files.exists(downloads.resolve(compositePDF))) {
             throw new Exception(String.format("Composite %s + missing!",
                     compositePDF));
         }
         documents[students.length] = new EntryType();
         documents[students.length].setId(compositePDF);
         manifest.setDocument(documents);
         
         // 5. Make room in the locker for receiving scans
         locker.makeRoom(quizId, testpaperId);
         
         return manifest;
     }
     
     private Path  sharedPath, mintPath;
     private Locker locker;
     private ATM   atm;
     private Vault vault;
 
     private void setQuestion(String questionId, PrintWriter answerKeyTex,
             Path staging) throws Exception {
         String content = vault.getContent(questionId, "question.tex")[0];
         answerKeyTex.print(content);
 
         Path plotfile = vault.getFiles(questionId, "figure.gnuplot")[0]
                 .toPath();
         Files.copy(plotfile, staging.resolve(questionId + ".gnuplot"));
 
     }
 
     private void replicateBlueprint(String[] lines, PrintWriter composite,
             PrintWriter single, String baseQRKey, String author,
             boolean firstPass) {
         for (int j = 0; j < lines.length; j++) {
 
             String line = lines[j];
             String trimmed = line.trim();
 
             if (trimmed.startsWith(printanswers)) {
                 continue;
             } else if (trimmed.startsWith(insertQR)) {
                 line = line.replace("QRC", baseQRKey + pageNumber);
             } else if (trimmed.startsWith(docAuthor)) {
                 line = docAuthor + "{" + author + "}"; // change the name
             }
 
             // This is the only chance the per-student TeX has to
             // get content. So, grab it ...
             single.println(line);
 
             if (trimmed.startsWith(beginDocument)
                     || trimmed.startsWith(beginQuestions)
                     || trimmed.startsWith(docClass)
                     || trimmed.startsWith(fancyfoot)
                     || trimmed.startsWith(school)
                     || trimmed.startsWith(usepackage)) {
                 if (firstPass)
                     composite.println(line);
             } else if (trimmed.startsWith(endDocument)
                     || trimmed.startsWith(endQuestions)) {
                 continue; // will be printed once, later
             } else {
                 composite.println(line);
             }
         }
         single.close();
         composite.flush();
     }
 
     private void writePreamble(PrintWriter writer, String school, String author)
             throws Exception {
         List<String> file = Files.readAllLines(
                 sharedPath.resolve("preamble.tex"), StandardCharsets.UTF_8);
         String[] lines = new String[file.size()];
         lines = file.toArray(lines);
 
         for (int j = 0; j < lines.length; j++) {
             String line = lines[j];
             String trimmed = line.trim();
 
             if (trimmed.startsWith(this.school)) {
                 writer.println(this.school + "{" + school + "}");
             } else if (trimmed.startsWith(docAuthor)) {
                 if (author == null)
                     author = "Gutenberg";
                 writer.println(docAuthor + "{" + author + "}");
             } else { // write whatever else is in preamble.tex
                 writer.println(line);
             }
         }
     }
 
     private void beginDoc(PrintWriter writer) throws Exception {
         writer.println(beginDocument);
         writer.println(beginQuestions);
     }
 
     private void endDoc(PrintWriter writer) throws Exception {
         writer.println(endQuestions);
         writer.println(endDocument);
     }
 
     private void resetPageNumbering(PrintWriter writer) throws Exception {
         writer.println("\\setcounter{page}{1}");
     }
 
     private void resetQuestionNumbering(PrintWriter writer) throws Exception {
         writer.println("\\setcounter{question}{0}");
     }
 
     private void insertBlankPage(PrintWriter writer) {
         writer.println("\\begingroup\\centering For rough work. Will NOT be graded \\\\ \\endgroup");
         writer.println(newpage);
     }
 
     private int make(Path workingDir, Path downloadsDir, Path previewsDir)
             throws Exception {
         ProcessBuilder pb = new ProcessBuilder("make");
 
         pb.directory(workingDir.toFile());
         pb.redirectErrorStream(true);
 
         Process process = pb.start();
         BufferedReader messages = new BufferedReader(new InputStreamReader(
                 process.getInputStream()));
 
         String line = null;
         while ((line = messages.readLine()) != null) {
             System.out.println(line);
         }
         
         int retCode = 0;
         if ((retCode = process.waitFor()) == 0) {
             
             Path target = null; 
             DirectoryStream<Path> stream = Files.newDirectoryStream(workingDir, "*.pdf");
             for (Path entry: stream) {
                 target = downloadsDir.resolve(entry.getFileName());
                 Files.move(entry, target, StandardCopyOption.REPLACE_EXISTING);
             }
             stream.close();
             
             if (previewsDir != null) {
                 stream = Files.newDirectoryStream(workingDir, "page-*.jpeg");
                 for (Path entry: stream) {
                     target = previewsDir.resolve(entry.getFileName());
                     Files.move(entry, target, StandardCopyOption.REPLACE_EXISTING);
                 }
                 stream.close();
             }
 
         }
 
         return retCode;
     }
 
     private String baseQR(EntryType student, AssignmentType assignment)
             throws Exception {
         String quiz = assignment.getQuiz().getId();
         String testpaper = assignment.getInstance().getId();
         String name = student.getName().toLowerCase().replace(' ', '-');
         String id = student.getId();
         return String.format("%s-%s-%s-%s", id, name, quiz, testpaper);
     }
 
     private final String printanswers = "\\printanswers",
             pageNumber = "\\thepage", docAuthor = "\\DocAuthor",
             newpage = "\\newpage", usepackage = "\\usepackage",
             fancyfoot = "\\fancyfoot", beginDocument = "\\begin{document}",
             beginQuestions = "\\begin{questions}", school = "\\School",
             docClass = "\\documentclass", insertQR = "\\insertQR",
             endQuestions = "\\end{questions}", endDocument = "\\end{document}";
 
 }
