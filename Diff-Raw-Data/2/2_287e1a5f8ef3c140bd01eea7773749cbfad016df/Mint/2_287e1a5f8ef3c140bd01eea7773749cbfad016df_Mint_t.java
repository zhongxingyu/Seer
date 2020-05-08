 package gutenberg.workers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.List;
 
 import gutenberg.blocs.AssignmentType;
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.ManifestType;
 import gutenberg.blocs.PageType;
 import gutenberg.blocs.QuizType;
 
 public class Mint {
 
 	public Mint(Config config) throws Exception {
 		this.bankRoot = new File(config.getPath(Resource.bank)).toPath();
 		this.mint = new File(config.getPath(Resource.mint)).toPath();
 		this.vault = new Vault(config);
 	}
 
 	/**
 	 * creates an answer key pdf for a quiz and preview jpegs
 	 * 
 	 * @param quiz
 	 * @throws Exception
 	 */
 	public ManifestType generate(QuizType quiz) throws Exception {
 
 		String quizId = quiz.getQuiz().getId();
 		Path quizDir = this.mint.resolve(quizId);
 		Path staging = quizDir.resolve("answer-key/staging");
 
 		boolean failed = (this.make("Prepare", quizId, null) == 0)? false:true;
 		if (failed) {
 			throw new Exception("[scribe:quiz] : Sandbox creation failed ... (quiz = " + quizId + " )");
 		}
 
 		PrintWriter answerKey = new PrintWriter(staging.resolve("answer-key.tex").toFile());
 		PageType[] pages = quiz.getPage();
 
 		// Write any information that might be pertinent during testpaper generation
 		// - like # of pages - as a comment right at the beginning
 		answerKey.println("% num_pages = " + pages[pages.length-1].getNumber());
 
 		// Write the preamble & initial stuff into the answerKey
 		writePreamble(answerKey, quiz.getSchool().getName(), quiz.getTeacher().getName());
 		beginDoc(answerKey);
 		answerKey.println(printanswers);
 		for (int i = 0; i < pages.length; i++) {
 			answerKey.println(preparePage(pages[i], staging));
 			answerKey.println(newpage);
 		}
 		endDoc(answerKey);
 		answerKey.close();
 
 		System.out.println("Return Code: " + make("Compile", quizId, null));
 		return prepareManifest(quiz);
 	}
 
 	/**
 	 * create an assignment
 	 * 
 	 * @param assignment
 	 * @throws Exception
 	 */
 	public ManifestType generate(AssignmentType assignment) throws Exception {
 
 		String quizId = assignment.getQuiz().getId();
 		String testpaperId = assignment.getInstance().getId();
 
 		boolean failed = (this.make("Prepare", quizId, testpaperId) == 0)? false: true;
 		if (failed) {
 			throw new Exception("[scribe:testpaper] : Sandbox creation failed ... ( quiz = " + quizId + " )");
 		}
 
 		int totalPages = 0;
 		Path quizDir = this.mint.resolve(quizId);
 		Path assignmentDir = quizDir.resolve(testpaperId);
 		Path staging = assignmentDir.resolve("staging");
 		Path blueprintPath = quizDir
 				.resolve("answer-key/staging/answer-key.tex");
 		BufferedReader blueprint = new BufferedReader(new FileReader(
 				blueprintPath.toFile()));
 		PrintWriter composite = new PrintWriter(staging + "/assignment-"
 				+ quizId + "-" + testpaperId + ".tex");
 		EntryType[] students = assignment.getStudents();
 		String[] lines = this.buffToString(blueprint);
 
 		for (int i = 0; i < students.length; i++) {
 			String name = students[i].getName();
 			String baseQR = QRCode(students[i], assignment);
 			int currQues = 1;
 			PrintWriter single = new PrintWriter(staging.resolve(
 					baseQR + ".tex").toFile());
 			boolean firstPass = (i == 0) ? true : false;
 
 			for (int j = 0; j < lines.length; j++) {
 				String line = lines[j];
 				String trimmed = line.trim();
 
 				if (trimmed.startsWith(printanswers)) {
 					continue;
 				} else if (trimmed.startsWith(insertQR)) {
 					line = line.replace("QRC", baseQR + "-" + currQues + "-" + pageNumber + "-" + totalPages);
 				} else if (trimmed.startsWith(question)) {
 					currQues += 1;
 				} else if (trimmed.startsWith(docAuthor)) {
 					line = docAuthor + "{" + name + "}"; // change the name
 				} else if (totalPages == 0 && trimmed.startsWith("% num_pages")) {
 					String[] tokens = trimmed.split(" ");
 					totalPages = Integer.parseInt(tokens[tokens.length - 1]);
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
 			if (totalPages%2 != 0) {
 				insertBlankPage(composite);
 			}
 			resetQuestionNumbering(composite);
 			resetPageNumbering(composite);
 		}
 		endDoc(composite);
 		composite.close();
 
 		System.out.println("Return Code: " + make("Compile", quizId, testpaperId));
 		return prepareManifest(assignment);
 	}
 
 	private String[] buffToString(BufferedReader stream) throws Exception {
 		List<String> lines = new ArrayList<String>();
 		String line = null;
 
 		while ((line = stream.readLine()) != null) {
 			lines.add(line);
 		}
 		stream.close();
 		return lines.toArray(new String[lines.size()]);
 	}
 
 	private String preparePage(PageType page, Path staging) throws Exception {
 		StringBuilder contents = new StringBuilder();
 		EntryType[] questionIds = page.getQuestion();
 		String questionId = null, question = null;
 		File[] resources = null;
 		if (questionIds != null) {
 			for (int i = 0; i < questionIds.length; i++) {
 				questionId = questionIds[i].getId();
 				question = this.vault.getContent(questionId, "question.tex")[0];
 				contents.append(question);
 				resources = this.vault.getFiles(questionId, "figure.gnuplot");
 				linkResources(resources[0], staging, questionId + ".gnuplot");
 			}
 		}
 		return contents.toString();
 	}
 
 	private void linkResources(File resource, Path targetDir, String targetFile)
 			throws Exception {
 		Path target = targetDir.resolve(targetFile);
 		if (!target.toFile().exists())
 			Files.createSymbolicLink(target, resource.toPath());
 	}
 
 	private void writePreamble(PrintWriter writer, String school, String author)
 			throws Exception {
 		Path preamble = this.bankRoot.resolve("shared/preamble.tex");
 		BufferedReader reader = new BufferedReader(new FileReader(
 				preamble.toFile()));
 		String[] lines = this.buffToString(reader);
 
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
 
 	private int make(String operation, String quizId, String testpaperId)
 			throws Exception {
 		testpaperId = (testpaperId == null || testpaperId.length() == 0) ? ""
 				: testpaperId;
 		ProcessBuilder pb = new ProcessBuilder("make", operation, "QUIZ="
 				+ quizId, "PRINTING_PRESS=" + this.bankRoot, "TEST="
 				+ testpaperId);
 
 		System.out.println("[make] : make " + operation + " QUIZ=" + quizId
 				+ " TEST=" + testpaperId);
 
 		pb.directory(this.mint.toFile());
 		pb.redirectErrorStream(true);
 
 		Process process = pb.start();
 		BufferedReader messages = new BufferedReader(new InputStreamReader(
 				process.getInputStream()));
 		String line = null;
 
 		while ((line = messages.readLine()) != null) {
 			System.out.println(line);
 		}
 		return process.waitFor();
 	}
 
 	private String QRCode(EntryType student, AssignmentType assignment)
 			throws Exception {
 		String quiz = assignment.getQuiz().getId();
 		String testpaper = assignment.getInstance().getId();
 		String name = student.getName().toLowerCase().replace(' ', '-');
 		String id = student.getId();
 		return String.format("%s-%s-%s-%s", 
 				id, name, quiz, testpaper);
 	}
 
 	private ManifestType prepareManifest(QuizType quiz) throws Exception {
 
 		manifest = new ManifestType();
 		String quizId = quiz.getQuiz().getId();
 		String atmKey = this.getAtmKey(quizId);
 
 		manifest.setRoot(atmKey);
 		Path preview = this.mint.resolve(quizId + "/answer-key/preview");
 		
 		String[] filenames = preview.toFile().list(); 
 		int pages = filenames.length;						
 		EntryType[] images = new EntryType[pages];
 				
 		if (pages == 0) {
 			throw new Exception("All preview thumbnail files not prepared.");			
 		}
 		
 		int i = 0;
 		for (String filename: filenames) {
 			images[i] = new EntryType();
 			images[i].setId(filename);
 			i++;
 		}
 		manifest.setImage(images);
 
 		Path downloads = this.mint.resolve("answer-key/downloads");
 		EntryType[] documents = new EntryType[1];
 		if (downloads.resolve("answer-key.pdf").toFile().exists()) {
 			throw new Exception("answer-key.pdf is missing!");
 		}
 		documents[0] = new EntryType();
 		documents[0].setId("answer-key.pdf");
 		manifest.setDocument(documents);
 
 		return manifest;
 	}
 
 	private ManifestType prepareManifest(AssignmentType assignment)
 			throws Exception {
 
 		manifest = new ManifestType();
 		String quizId = assignment.getQuiz().getId();
 		String atmKey = this.getAtmKey(quizId);
 		String instanceId = assignment.getInstance().getId();
 
 		manifest.setRoot(atmKey + "/" + instanceId);
 
 		EntryType[] students = assignment.getStudents();
 		EntryType[] documents = new EntryType[students.length + 1];
 
 		Path downloads = this.mint.resolve(quizId).resolve(instanceId)
 				.resolve("downloads");
 		for (int i = 0; i < students.length; i++) {
 			String filename = QRCode(students[i], assignment) + ".pdf";
 			if (downloads.resolve(filename).toFile().exists()) {
 				documents[i] = new EntryType();
 				documents[i].setId(filename);
 			} else {
 				throw new Exception(filename
 						+ " missing! All assignment files not prepared.");
 			}
 		}
 		String assignmentPdf = "assignment-" + quizId + "-" + instanceId
 				+ ".pdf";
 		if (!downloads.resolve(assignmentPdf).toFile().exists()) {
 			throw new Exception(assignmentPdf + " missing!");
 		}
 		documents[students.length] = new EntryType();
 		documents[students.length].setId(assignmentPdf);
 		manifest.setDocument(documents);
 		return manifest;
 	}
 
 	private String getAtmKey(String quizId) throws Exception {
 		// Each quiz folder has within it a single-line file called 'atm-key'
 		// which in turn has within it a 9-digit random number. A link with
 		// this name(number?) - pointing to the parent quiz folder - is created
 		// within the atm/ folder
 		Path atmkey = this.mint.resolve(quizId).resolve("atm-key");
 		BufferedReader r = new BufferedReader(new FileReader(atmkey.toFile()));
 		String key = r.readLine(); // 'atm-key' has only one line
 		if (key != null) {
 			return key;
 		} else {
 			throw new Exception("No ATM-key for quiz = " + quizId);
 		}
 	}
 
 	private Path bankRoot, mint;
 	private Vault vault;
 	private ManifestType manifest;
 	private final String printanswers = "\\printanswers", pageNumber = "\\thepage",
 			docAuthor = "\\DocAuthor", newpage = "\\newpage",
 			usepackage = "\\usepackage", fancyfoot = "\\fancyfoot",
 			question = "\\question", beginDocument = "\\begin{document}",
 			beginQuestions = "\\begin{questions}", school = "\\School",
 			docClass = "\\documentclass", insertQR = "\\insertQR",
 			endQuestions = "\\end{questions}", endDocument = "\\end{document}";
 
 }
