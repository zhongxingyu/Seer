 package gutenberg.workers;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.nio.file.Files;
 import java.util.Properties;
 
 import gutenberg.blocs.AssignmentType;
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.ManifestType;
 import gutenberg.blocs.PageType;
 import gutenberg.blocs.QuizType;
 
 public class Scribe {
 
   public Scribe() throws Exception {
   Properties config = new Properties();
 
   config.loadFromXML(new FileInputStream("/opt/gutenberg/config.xml"));
     this.bankRoot = config.getProperty("BANK_ROOT") ;
     this.webRoot = config.getProperty("WEB_ROOT") ; 
     this.mint = this.bankRoot + "/mint/" ;
     this.vault = new Vault(this.bankRoot + "/vault/") ;
   } 
 
   /**
    * creates an answer key pdf for a quiz and preview jpegs
    * 
    * @param quiz
    * @throws Exception
    */
   public void generate(QuizType quiz) throws Exception {
 
     String quizId = quiz.getQuiz().getId();
     //File quizDir = new File(this.mint + quizId);
     File staging = new File(this.mint + quizId + "/answer-key/staging") ;
 
     boolean failed = (this.make("Prepare",quizId, null) == 0) ? false : true ;
 
     if (failed) { 
       throw new Exception("[scribe:quiz] : Sandbox creation failed ... ( quiz = " + quizId + " )") ;
     } 
     
     //System.out.println("[Scribe] : mint = " + this.mint) ;
 
     PrintWriter   answerKey = new PrintWriter(staging + "/answer-key.tex") ;
     PageType[]    pages = quiz.getPage();
 
     // Write any information that might be pertinent during testpaper generation
     // - like # of pages - as a comment right at the beginning
     answerKey.println("% num_pages = " + pages.length) ;
 
     // Write the preamble & initial stuff into the answerKey
     writePreamble(answerKey, quiz.getSchool().getName(), quiz.getTeacher().getName());
     beginDoc(answerKey);
     answerKey.println(printanswers);
 
     for (int i = 0; i < pages.length; i++) {
       PrintWriter  preview = new PrintWriter(staging + "/page-" + i + ".tex") ; 
       String       page = preparePage(pages[i], staging) ;
 
       writePreamble(preview, quiz.getSchool().getName(), quiz.getTeacher().getName());
       beginDoc(preview);
       preview.println(printanswers);
 
       preview.println(page) ;
       answerKey.println(page) ;
 
       endDoc(preview) ;
       preview.close() ;
       answerKey.println(newpage) ;
     }
     endDoc(answerKey);
     answerKey.close();
 
     // make command : make Compile QUIZ=<quizId>
     System.out.println("Return Code: " + make("Compile", quizId,null));
     prepareManifest(quiz);
   }
 
   /**
    * create an assignment
    * 
    * @param assignment
    * @throws Exception
    */
   public void generate(AssignmentType assignment) throws Exception {
 
     String   quizId = assignment.getQuiz().getId();
     String   testpaperId = assignment.getInstance().getId();
     //String   school = assignment.getQuiz().getName() ;
 
     boolean failed = (this.make("Prepare", quizId, testpaperId) == 0) ? false : true ;
     if (failed) { 
       throw new Exception("[scribe:testpaper] : Sandbox creation failed ... ( quiz = " + quizId + " )") ;
     } 
     /*
     File     quizDir = new File(this.mint + quizId);
     File     answerKey = new File(this.mint + quizId + "/answer-key") ;
     //File     blueprint = new File(answerKey + "/staging/blueprint.tex.skip") ;
     File     target = new File(quizDir + "/" + testpaperId);
     File     staging = new File(target + "/staging");
     */
     
     int              totalPages = 0 ;
     String           staging = this.mint + quizId + "/" + testpaperId + "/staging/" ;
     String           blueprintPath = this.mint + quizId + "/answer-key/staging/answer-key.tex" ;
     BufferedReader   blueprint = new BufferedReader(new FileReader(blueprintPath)) ;
     PrintWriter      composite = new PrintWriter(staging + "/assignment-" + quizId + "-" + testpaperId + ".tex") ;
     EntryType[]      students = assignment.getStudents() ;
 
     for (int i = 0 ; i < students.length ; i++) {
       String         name = students[i].getName() ;
       String         baseQR = QRCode(students[i], assignment) ;
       int            currQues = 1, currPage = 1 ;
       PrintWriter    single = new PrintWriter(staging + baseQR + ".tex") ;
       boolean        firstPass = (i == 0) ? true : false ; 
       String         line = null ;
 
       while (( line = blueprint.readLine()) != null) {
         String   trimmed = line.trim() ;
 
         if (trimmed.startsWith(printanswers)) {
           continue ; 
         } else if (trimmed.startsWith(insertQR)) {
           line = line.replace("QRC", baseQR + "-" + currQues + "-" + currPage + "-" + totalPages) ;
         } else if (trimmed.startsWith(newpage)) {
           currPage += 1 ;
         } else if (trimmed.startsWith(question)) {
           currQues += 1 ;
         } else if (trimmed.startsWith(docAuthor)) {
           line = "\\DocAuthor{" + name + "}" ; // change the name
         } else if (trimmed.startsWith("% num_pages")) { // A TeX comment
           String[]  tokens = trimmed.split(" ") ; 
           totalPages = Integer.parseInt(tokens[tokens.length - 1]) ;
         } 
         
         // This is the only chance the per-student TeX has to 
         // get content. So, grab it ... 
         single.println(line) ;
 
         if (trimmed.startsWith(beginDocument) || 
             trimmed.startsWith(beginQuestions)) {
           if (firstPass) composite.println(line) ;
         } else if ( trimmed.startsWith(endDocument) || 
                     trimmed.startsWith(endQuestions) ) {
           continue ;
         } else { 
           composite.println(line) ;
         }
       }
       single.close() ;
       resetQuestionNumbering(composite) ;
       resetPageNumbering(composite) ;
     } 
     endDoc(composite) ;
     composite.close() ;
 
     // make command : make Compile QUIZ=<quizId> TEST=<testpaperId>
     System.out.println("Return Code: " + make("Compile", quizId, testpaperId)) ;
     prepareManifest(assignment);
   }
 
   public ManifestType getManifest() {
     return manifest;
   }
   private String preparePage(PageType page, File staging) throws Exception {
     StringBuilder contents = new StringBuilder();
     EntryType[] questionIds = page.getQuestion();
     String questionId = null, question = null;
     File[] resources = null;
     for (int i = 0; i < questionIds.length; i++) {
       questionId = questionIds[i].getId();
       question = this.vault.getContent(questionId, "question.tex")[0];
       contents.append(insertQR).append('\n');
       contents.append(question);
       resources = this.vault.getFiles(questionId, "figure.gnuplot");      
       linkResources(resources[0], staging, questionId + ".gnuplot");
     }
     return contents.toString();
   }
 
   private void linkResources(File resource, File targetDir, String targetFile)
       throws Exception {
     File target = new File(targetDir.getPath() + "/" + targetFile);
     if (!target.exists())
       Files.createSymbolicLink(target.toPath(), resource.toPath());
   }
   
   private void writePreamble(PrintWriter writer, String school, String author)
       throws Exception {
     writer.println("\\documentclass[justified]{tufte-exam}");
     writer.println("\\School{" + school + "}");
     if (author != null) writer.println("\\DocAuthor{" + author + "}");
     writer.println("\\fancyfoot[C]{\\copyright\\, Gutenberg}");
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
 
   private int make(String operation, String quizId, String testpaperId) throws Exception {
   testpaperId = (testpaperId == null || testpaperId.length() == 0) ? "" : testpaperId ;
     ProcessBuilder pb = new ProcessBuilder("make", operation, "QUIZ=" + quizId,
                                            "PRINTING_PRESS=" + this.bankRoot,
                                            "TEST=" + testpaperId) ;
                                            
     System.out.println("[make] : make " + operation + " QUIZ=" + quizId + " TEST=" + testpaperId) ;
 
     pb.directory(new File(this.mint)) ;
     pb.redirectErrorStream(true) ;
 
     Process        process = pb.start() ;
     BufferedReader messages = new BufferedReader(new InputStreamReader(process.getInputStream())) ;
     String         line = null ;
 
     while ((line = messages.readLine()) != null) {
       System.out.println(line) ;
     }
     return process.waitFor() ;
   }
 
   private String QRCode(EntryType student, AssignmentType assignment) throws Exception {
     String  quiz = assignment.getQuiz().getId() ;
     String  testpaper = assignment.getInstance().getId() ;
     String  name = student.getName() ;
     String  id = student.getId() ;
     
     return (id + "-" + name.toLowerCase().replace(' ','-') + "-" + quiz + "-" + testpaper) ;
   }
 
   private void prepareManifest(QuizType quiz) throws Exception {
 
     manifest = new ManifestType();
     String quizId = quiz.getQuiz().getId();
     manifest.setRoot(webRoot + "/" + quizId);
     String previewDir = this.mint + quizId + "/answer-key/preview";
 
     int pages = quiz.getPage().length;
     EntryType[] images = new EntryType[pages * 2];
     String filename = null;
     for (int i = 0; i < pages; i++) {
 
       filename = "page-" + i + "-preview.jpeg";
       if (new File(previewDir + "/" + filename).exists()) {
         images[2 * i] = new EntryType();
         images[2 * i].setId(filename);
       } else {
         throw new Exception(filename
             + " missing! All preview files not prepared.");
       }
 
       filename = "page-" + i + "-thumbnail.jpeg";
       if (new File(previewDir + "/" + filename).exists()) {
         images[2 * i + 1] = new EntryType();
         images[2 * i + 1].setId(filename);
       } else {
         throw new Exception(filename
             + " missing! All preview thumbnail files not prepared.");
       }
 
     }
     manifest.setImage(images);
 
     String downloadsDir = this.mint + quizId + "/answer-key/downloads";
     EntryType[] documents = new EntryType[1];
     if (!new File(downloadsDir + "/answer-key.pdf").exists()) {
       throw new Exception("answer-key.pdf is missing!");
     }
     documents[0] = new EntryType();
     documents[0].setId("answer-key.pdf");
     manifest.setDocument(documents);
   }
 
   private void prepareManifest(AssignmentType assignment) throws Exception {
 
     manifest = new ManifestType();
     String quizId = assignment.getQuiz().getId();
     String instanceId = assignment.getInstance().getId();
     
     manifest.setRoot(webRoot + "/" + quizId + "/" + instanceId);
 
     EntryType[] students = assignment.getStudents();
     EntryType[] documents = new EntryType[students.length + 1];
     
    String assignmentPdf = "/assignment-" + quizId + "-" + instanceId ;
     String downloadsDir = this.mint + quizId + "/" + instanceId + "/downloads";
 
     for (int i = 0; i < students.length; i++) {
       String filename = QRCode(students[i], assignment) + ".pdf" ;
       if (new File(downloadsDir + "/" + filename).exists()) {
         documents[i] = new EntryType();
         documents[i].setId(filename);
       } else {
         throw new Exception(filename
             + " missing! All assignment files not prepared.");
       }
     }
     if (!new File(downloadsDir + assignmentPdf).exists()) {
       throw new Exception(assignmentPdf + " missing!");
     }
     documents[students.length] = new EntryType();
     documents[students.length].setId(assignmentPdf);
     manifest.setDocument(documents);
   }
 
   private String       bankRoot, mint, webRoot ;
   private Vault        vault;
   private ManifestType manifest;
   private final String printanswers = "\\printanswers",
                        docAuthor = "\\DocAuthor", 
                        newpage = "\\newpage",
                        question = "\\question", 
                        beginDocument = "\\begin{document}",
                        beginQuestions = "\\begin{questions}", 
                        docClass = "\\documentclass",
                        insertQR = "\\insertQR{QRC}", 
                        endQuestions = "\\end{questions}",
                        endDocument = "\\end{document}" ;
 
 
 }
