 package gutenberg.workers;
 
 import gutenberg.blocs.ManifestType;
 import gutenberg.blocs.QuestionTagsType;
 import gutenberg.blocs.QuestionType;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardCopyOption;
 import java.util.ArrayList;
 
 public class Vault {
 
 	public Vault(Config config) {
 		VAULT = config.getPath(Resource.vault);
 		SHARED = config.getPath(Resource.shared);
 	}
 	public Vault() throws Exception {
 		Config config = new Config();
 		VAULT = config.getPath(Resource.vault);
 		SHARED = config.getPath(Resource.shared);
 	}
 
 	/**
 	 * @param id - question id
 	 * @param filter - type of content e.g. "tex", "gnuplot" etc.
 	 * @return returns file contents for given id
 	 * @throws Exception
 	 */
 	public String[] getContent(String id, String filter) throws Exception {
 		File directory = new File(VAULT + "/" + id);
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
 	 * @param id - question id
 	 * @param filter - type of content e.g. "tex", "gnuplot" etc.
 	 * @return Files
 	 * @throws Exception
 	 */
 	public File[] getFiles(String id, String filter) throws Exception {
 		File directory = new File(VAULT + "/" + id);
 		return directory.listFiles(new NameFilter(filter));
 	}	
 	
 	public File[] getFiles(String[] id, String filter) throws Exception {
 		ArrayList<File> fileSet = new ArrayList<File>();
 		File[] files = null;
 		for (int i = 0; i < id.length; i++) {
 			files = getFiles(id[i], filter);
 			for (int j = 0; j < files.length; j++) {
 				fileSet.add(files[j]);
 			}
 		}
 		return fileSet.toArray(new File[0]);
 	}	
 	
 	/**
 	 * Creates a question in the Vault
 	 * 
 	 * @param quizMasterId - id of person creating question
 	 * @return Manifest
 	 * @throws Exception
 	 */
 	public ManifestType createQuestion(String quizMasterId) throws Exception {
 
 		String hexTimestamp = Long.toString(System.currentTimeMillis(), 
 				Character.MAX_RADIX); 
 		String dirName = quizMasterId + "-" 
 				+ hexTimestamp.substring(0, 3) + "-"
 				+ hexTimestamp.substring(3);
 		Path questionDir = new File(VAULT).toPath().resolve(dirName);
 		Files.createDirectory(questionDir);
 
 		Path shared = new File(SHARED).toPath();
 		Files.copy(shared.resolve(texFile), questionDir.resolve(texFile));
 		Files.copy(shared.resolve(plotFile), questionDir.resolve(plotFile));
 		Files.createSymbolicLink(questionDir.resolve("Makefile"), shared.resolve(makeFile));
 
 		ManifestType manifest = new ManifestType();
 		manifest.setRoot(questionDir.toString());
 		return manifest;
 	}
 	
 	/**
 	 * 
 	 * @param tags - properties to be encoded in question.tex file
 	 * @return Manifest
 	 * @throws Exception 
 	 */
 	public ManifestType tagQuestion(QuestionTagsType tags) throws Exception {
 		
 		String id = tags.getId();
 		String marks = "";
 		String length = "";
 		if (tags.getMarks().length == 1) {//single part question
 			marks = String.format(marksFormat, tags.getMarks()[0]);
 			length = String.format(lengthFormat, tags.getLength()[0]);
		} else {//multi part question
			int totalMarks = 0;
			for (int m: tags.getMarks()) {
				totalMarks += m;
			}
			marks = String.format(marksFormat, totalMarks);
 		}
 		
 		Path questionTex = new File(VAULT).toPath().resolve(id).resolve(texFile);
 		BufferedReader reader = new BufferedReader(new FileReader(questionTex.toFile()));
 		
 		Path questionTexTmp = questionTex.resolveSibling(texFile + ".tmp");
 		PrintWriter writer = new PrintWriter(new FileWriter(questionTexTmp.toFile()));
 		
 		int part = 0;
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 			if (line.trim().startsWith(questionTag)) {
 				if (!line.contains(" ")) {
 					throw new Exception("[tagQuestion]: Cannot tag a blank question");
 				}
				line = questionTag + marks + line.substring(line.indexOf(' '));
 			} else if (line.trim().startsWith(solutionTag)) {
 				length = String.format(lengthFormat, tags.getLength()[part++]);
 				line = solutionTag + length;
 			} else if (line.trim().startsWith(partTag)) {
 				if (!line.contains(" ")) {
 					throw new Exception("[tagQuestion]: Cannot tag a blank question (part)");
 				}
 				marks = String.format(marksFormat, tags.getMarks()[part]);
				line = partTag + marks + line.substring(line.indexOf(' '));
 			}
 			writer.println(line);
 		}
 		writer.flush();
 		writer.close();
 		Files.move(questionTexTmp, questionTex, StandardCopyOption.REPLACE_EXISTING);
 		
 		ManifestType manifest = new ManifestType();
 		manifest.setRoot(questionTex.getParent().getFileName().toString());
 		return manifest;
 	}
 	
 	public ManifestType writeQuestion(QuestionType question) throws Exception {
 		
 		QuestionTagsType tags = question.getTags();
 		String questionTex = question.getQuestion();
 		String marginnoteTex = question.getMarginnotes();
 		String solutionTex = question.getSolution();
 		
 		StringBuilder sb = new StringBuilder();		
 		
 		sb.append(questionTag).append(String.format(marksFormat, tags.getMarks()));
 		sb.append(" ").append(questionTex).append("\n");
 		sb.append("\\ifprintanswers\n");
 		if (marginnoteTex.length() != 0) {
 			sb.append("  \\marginnote {").append(marginnoteTex).append("}\n");
 		}
 		sb.append("\\fi\n");		
 
 		sb.append(solutionTag).append(String.format(lengthFormat, tags.getLength()[0])).append("\n");
 		sb.append(solutionTex).append("\n");
 		sb.append("\\end{solution}");
 		
 		File questionTexFile = new File(VAULT + "/preview/question.tex");
 		PrintWriter writer = new PrintWriter(new FileWriter(questionTexFile));
 		
 		writer.write(sb.toString());
 		writer.close();
 		
 		ProcessBuilder pb = new ProcessBuilder("make");
 		pb.directory(questionTexFile.getParentFile());
 		pb.redirectErrorStream(true);
 		
 		Process process = pb.start();
 		BufferedReader messages = new BufferedReader(new InputStreamReader(
 				process.getInputStream()));
 		
 		String line = null;
 		while ((line = messages.readLine()) != null) {
 			System.out.println(line);
 		}
 		
 		int returnCode = process.waitFor();
 		System.out.println("[writeQuestion]: Return Code: " + returnCode);
 		
 		if (returnCode != 0) {
 			throw new Exception("[writeQuestion]: make failed with code " + returnCode);
 		}
 		
 		ManifestType manifest = new ManifestType();		
 		manifest.setRoot(questionTexFile.getName());
 		return manifest;		
 	}
 
 	public String getPath() throws Exception {
 		return this.VAULT;
 	}
 
 	private String VAULT, SHARED;
 	private final String texFile = "question.tex", plotFile = "figure.gnuplot", makeFile = "individual.mk";
 	private final String questionTag = "\\question", 
                        solutionTag = "\\begin{solution}",
                        partTag = "\\part",
                        lengthFormat = "[\\%s]",
                        marksFormat = "[%s]" ;
 }
