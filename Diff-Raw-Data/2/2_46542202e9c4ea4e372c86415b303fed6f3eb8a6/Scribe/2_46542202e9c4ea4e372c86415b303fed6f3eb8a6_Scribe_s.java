 package gutenberg.workers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import gutenberg.blocs.AssignmentType;
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.ManifestType;
 import gutenberg.blocs.PageType;
 import gutenberg.blocs.QuizType;
 
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
 	 * @param quiz
 	 * @throws Exception
 	 */
 	public void generate(QuizType quiz) throws Exception {
 		File quizDir = new File(MINT + "/" + quiz.getId());
 		File staging = new File(MINT + "/" + quiz.getId() + "/" + "staging");
 		if (!quizDir.exists()) {
 			quizDir.mkdir();
 			quizDir.setExecutable(true, false);
 			staging.mkdir();
 			staging.setExecutable(true, false);
 		}
 		manifest.setRoot(quizDir.getPath());
 		
 		PrintWriter answerkey = new PrintWriter(staging + "/answerkey.tex");
 		//TODO substitute teacherId 
 		answerkey.println(preamble);
 		answerkey.println("\\printanswers");
 		answerkey.println(docBegin);
 		
 		PageType[] pages = quiz.getPage();
 		PrintWriter[] preview = new PrintWriter[pages.length];
 		String page = null;
 		EntryType[] images = new EntryType[pages.length*2];
 		for (int i = 0; i < pages.length; i++) {
 			preview[i] = new PrintWriter(staging + "/page" + i + ".tex");
 			preview[i].println(docBegin);
 			page = buildPage(pages[i], staging);
 			preview[i].println(page);
 			preview[i].print(docEnd);
 			preview[i].close();			
 			answerkey.println(page);
 			images[2*i] = new EntryType();
 			images[2*i].setId(i+"page.jpg");
 			images[2*i+1] = new EntryType();
 			images[2*i+1].setId(i+"thumb.jpg");
 			if (i == pages.length-1) {
 				answerkey.println("\\newpage");
 			}
 		}
 		if (answerkey.checkError())
 			throw new Exception("Check returned with error ") ;
 		answerkey.close();
 		EntryType[] documents = new EntryType[1];
 		documents[0] = new EntryType();
 		documents[0].setId("answerkey.pdf");
 		
 		int ret = make(quiz) ;
 		if (ret != 0) {
 			throw new Exception("Make returned with: " + ret) ;
 		} else {
 			manifest.setImage(images);
 			manifest.setDocument(documents);			
 		}
 	}
 	
 	/**
 	 * create an assignment
 	 * @param assignment
 	 */
 	public void generate(AssignmentType assignment) {
 
 	}
 	
 	public ManifestType getManifest() {
 		return manifest;
 	}
 	
 	private String MINT;
 	private String preamble, docBegin, docEnd;
 	private Vault vault;
 	private ManifestType manifest;
 	
 	private void loadShared(String shared) throws Exception {
 		Filer filer = new Filer();
 		preamble = filer.get(shared + "/preamble.tex");
 		docBegin = filer.get(shared+ "/doc_begin.tex");
 		docEnd = filer.get(shared + "/doc_end.tex");		
 
 	}
 	
 	private String buildPage(PageType page, File staging) throws Exception {
 		return buildPage(page, staging, null);
 	}
 		
 	private String buildPage(PageType page, File staging, String qrcode) throws Exception {
 		StringBuilder contents = new StringBuilder();
 		EntryType[] questionIds = page.getQuestion();
 		Filer filer = new Filer();
 		String question = null;
 		for(int i = 0; i < questionIds.length; i++) {
 			if (qrcode != null) {
 				qrcode += questionIds[i].getId();
 				contents.append(qrcode).append("\n");
 			}
 			question = vault.getContent(questionIds[i].getId(), "tex")[0];
 			contents.append(question);
 			File[] files = vault.getFiles(questionIds[i].getId(), "gnuplot");
 			for (int j = 0; j < files.length; j++) {			
				filer.copy(files[i], new File(staging.getPath() + "/" + files[i].getName()));
 			}
 		}
 		return contents.toString();
 	}
 
 	private int make(QuizType quiz) throws Exception {
 		ProcessBuilder processBuilder = new ProcessBuilder("make", "dir="+quiz.getId());
 		File mintDir = new File(MINT);
 		processBuilder.directory(mintDir);
 		processBuilder.redirectErrorStream(true);
 		Process process = processBuilder.start();
 		BufferedReader reader = new BufferedReader (new InputStreamReader(process.getInputStream()));
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 			System.out.println(line);
 		}
 		return process.waitFor();				
 	}
 	
 }
