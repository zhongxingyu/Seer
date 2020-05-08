 package gutenberg.workers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.sql.Date;
 import java.text.DateFormat;
 
 import gutenberg.blocs.AssignmentType;
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.ManifestType;
 import gutenberg.blocs.PageType;
 import gutenberg.blocs.QuizType;
 import gutenberg.blocs.StudentType;
 
 public class Scribe {
 
 	public Scribe(String mint, String shared) throws Exception {
 		MINT = mint;
 		loadShared(shared);
 		manifest = new ManifestType();
 	}
 
 	public void setVault(Vault vault) {
 		this.vault = vault;
 	}
 
 	/**
 	 * creates an answer key pdf for a quiz and preview jpgs
 	 * 
 	 * @param quiz
 	 * @throws Exception
 	 */
 	public void generate(QuizType quiz) throws Exception {
 		File quizDir = new File(MINT + "/" + quiz.getId());
 		File staging = new File(quizDir + "/staging");
 		if (!quizDir.exists()) {
 			quizDir.mkdir();
 			quizDir.setExecutable(true, false);
 			staging.mkdir();
 			staging.setExecutable(true, false);
 		}
 
 		PrintWriter answerkey = new PrintWriter(staging + "/answer-key.tex");
 		// TODO substitute teacherId
 		answerkey.println(preamble.replace("Prof. Dumbledore", "The Teacher"));
 		answerkey.println(printanswers);
 		answerkey.println(docBegin);
 
 		PageType[] pages = quiz.getPage();
 		PrintWriter preview = null;
 		String page = null;
 		for (int i = 0; i < pages.length; i++) {
 			preview = new PrintWriter(staging + "/page" + i + ".tex");
 			preview.println(docBegin);
 			page = buildPage(pages[i], staging);
 			preview.println(page);
 			preview.print(docEnd);
 			preview.close();
 
 			answerkey.println(page);
 			if (i < pages.length - 1) {
 				answerkey.println(newpage);
 			}
 		}
 		answerkey.println(docEnd);
 		answerkey.close();
 
 		System.out.println("Return Code: " + make(quiz));
 		prepareManifest(quizDir.getPath(), pages.length);
 	}
 
 	/**
 	 * create an assignment
 	 * 
 	 * @param assignment
 	 * @throws Exception
 	 */
 	public void generate(AssignmentType assignment) throws Exception {
 		QuizType quiz = assignment.getQuiz();
 
 		File quizDir = new File(MINT + "/" + quiz.getId());
 		File staging = new File(quizDir + "/staging");
 		if (!quizDir.exists()) {
 			throw new Exception("Cannot assign non-existant quiz: "
 					+ quiz.getId());
 		}
 
 		int totalPages = staging.list(new NameFilter("page")).length;
 
 		PrintWriter composite = new PrintWriter(staging + "/assignment.tex");
 		PrintWriter individual = null;
 
 		StudentType[] students = assignment.getStudents();
 		for (int i = 0; i < students.length; i++) {
 
 			individual = new PrintWriter(staging + "/" + students[i].getId()
 					+ "-assignment.tex");
 			int pageNumber = 1;
 
 			String line = null;
 			BufferedReader reader = new BufferedReader(new FileReader(staging
 					+ "/answer-key.tex"));
 			while ((line = reader.readLine()) != null) {
 
 				line = line.trim();
 				if (line.startsWith(newpage)) {
 					pageNumber++;
 				}
 
 				if (line.startsWith(question)) {
 					String questionId = line.substring(line.indexOf('[') + 1,
 							line.indexOf(']'));
 					line = String.format("QRC> %s.%s.%s.%s.%s.%s\n%s", quiz
 							.getId(), pageNumber, totalPages, questionId,
 							students[i].getId(), students[i].getName(), line);
 				}
 
 				if (line.startsWith(docAuthor)) {
					line = String.format(docAuthor + "{%s}", students[i].getName());
 				}
 
 				if (line.startsWith(printanswers)) {
 					line = "";
 				}
 
 				individual.println(line);
 				// docBegin and docEnd print only once for composite document
 				if (line.startsWith(beginDocument) && i != 0) {
 					line = "";
 				}
 				if (line.startsWith(endDocument) && i != students.length-1) {
 					line = "";
 				}
 				composite.println(line);
 
 			}
 			composite.flush();
 			individual.close();
 		}
 
 		composite.close();
 
 		System.out.println("Return Code: " + make(quiz));
 		manifest = new ManifestType();
 		manifest.setRoot(staging.getPath());
 	}
 
 	public ManifestType getManifest() {
 		return manifest;
 	}
 
 	private String MINT;
 	private String preamble, docBegin, docEnd;
 	private final String printanswers = "\\printanswers",
 			docAuthor = "\\DocAuthor", newpage = "\\newpage",
 			question = "\\question", beginDocument = "\\begin{document}",
 			endDocument = "\\end{document}";
 	private Vault vault;
 	private ManifestType manifest;
 
 	private void loadShared(String shared) throws Exception {
 		Filer filer = new Filer();
 		preamble = filer.get(shared + "/preamble.tex");
 		docBegin = filer.get(shared + "/doc_begin.tex");
 		docEnd = filer.get(shared + "/doc_end.tex");
 
 	}
 
 	private String buildPage(PageType page, File staging) throws Exception {
 		return buildPage(page, staging, null);
 	}
 
 	private String buildPage(PageType page, File staging, String qrcode)
 			throws Exception {
 		StringBuilder contents = new StringBuilder();
 		EntryType[] questionIds = page.getQuestion();
 		String question = null;
 		for (int i = 0; i < questionIds.length; i++) {
 			if (qrcode != null) {
 				qrcode += questionIds[i].getId();
 				contents.append(qrcode).append("\n");
 			}
 			question = vault.getContent(questionIds[i].getId(), "question.tex")[0];
 			contents.append(question);
 			copyResources(questionIds[i].getId(), staging);
 		}
 		return contents.toString();
 	}
 
 	private void copyResources(String questionId, File staging)
 			throws Exception {
 		Filer filer = new Filer();
 		File[] files = vault.getFiles(questionId, "figure.gnuplot");
 		for (int j = 0; j < files.length; j++) {
 			filer.copy(files[j], new File(staging.getPath() + "/"
 					+ files[j].getName()));
 		}
 	}
 
 	private int make(QuizType quiz) throws Exception {
 		ProcessBuilder processBuilder = new ProcessBuilder("make", "dir="
 				+ quiz.getId());
 		File mintDir = new File(MINT);
 		processBuilder.directory(mintDir);
 		processBuilder.redirectErrorStream(true);
 		Process process = processBuilder.start();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				process.getInputStream()));
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 			System.out.println(line);
 		}
 		return process.waitFor();
 	}
 
 	private void prepareManifest(String path, int length) {
 		manifest.setRoot(path);
 		EntryType[] images = new EntryType[length * 2];
 		for (int i = 0; i < length; i++) {
 			images[2 * i] = new EntryType();
 			images[2 * i].setId(i + "page.jpg");
 			images[2 * i + 1] = new EntryType();
 			images[2 * i + 1].setId(i + "thumb.jpg");
 		}
 		EntryType[] documents = new EntryType[1];
 		documents[0] = new EntryType();
 		documents[0].setId("answerkey.pdf");
 		manifest.setImage(images);
 		manifest.setDocument(documents);
 	}
 
 }
