 package topcoder.editor;
 
 import com.topcoder.client.contestant.ProblemComponentModel;
 import com.topcoder.client.contestant.RoundModel;
 import com.topcoder.shared.language.Language;
 import com.topcoder.shared.problem.Renderer;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import topcoder.editor.ui.Common;
 import topcoder.editor.ui.EditorPanel;
 
 public class Editor implements Observer {
 	JPanel panel;
 	static JTextArea log = new JTextArea();
 	String dirName;
 	String roundName;
 	String fileName;
 	String beginCut;
 	String endCut;
 	Preferences pref = Preferences.getInstance();
 	Map<String, String> userDefinedTags = new HashMap<String, String>();
 	ProblemComponentModel component;
 	Language language;
 	Renderer renderer;
 
 	public Editor() {
 		log.setForeground(Common.FG_COLOR);
 		log.setBackground(Common.BG_COLOR);
 		pref.addSaveObserver(this);
 
 		loadPreferences();
 		this.panel = new EditorPanel(log);
 	}
 
 	public void setUserDefinedTags(Map<String, String> userDefinedTags) {
 		this.userDefinedTags = userDefinedTags;
 	}
 
 	public void setProblemComponent(
 			ProblemComponentModel component,
 			Language lang,
 			Renderer renderer) {
 		this.component = component;
 		this.language = lang;
 		this.renderer = renderer;
 	}
 
 	public JPanel getEditorPanel() {
 		return this.panel;
 	}
 
 	protected String getSignature() {
 		String sigFileName = this.pref.getSignatureFileName().trim();
 		if (!sigFileName.equals("")) {
 			File sigFile = new File(sigFileName);
 
 			if (sigFile.exists()) {
 				StringBuffer sig = new StringBuffer((int) sigFile.length());
 				try {
 					BufferedReader in = new BufferedReader(new FileReader(sigFile));
 					while (true) {
 						String line = in.readLine();
 						if (line == null)
 							break;
 						sig.append(line);
 						sig.append(Utilities.lineEnding);
 					}
 					in.close();
 					return sig.toString();
 				} catch (IOException e) {
 					writeLog("Error reading the signature file " + sigFileName
 							+ ":" + e.toString() + Utilities.lineEnding
 							+ "SigFile ignored");
 				}
 			}
 		}
 		return "";
 	}
 
 	public String getSource() {
 		this.loadDirFileNames();
		File f = newFile(this.pref.getTextDescExtension());
 
 		if (f == null) {
 			writeLog("Trying to read source but file isn't initialized!.  Returning nothing.");
 			return "";
 		}
 
 		if (!f.exists()) {
 			writeLog("Trying to read File " + f
 					+ " but it does not exist!.  Returning nothing.");
 			return "";
 		}
 
 		int len = (int) f.length();
 		StringBuffer sourceComments = new StringBuffer(len);
 		StringBuffer source = new StringBuffer(len);
 
 		boolean ignoreLine = false;
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(f));
 			while (true) {
 				String line = in.readLine();
 				if (line == null) {
 					break;
 				}
 				sourceComments.append(line);
 				sourceComments.append(Utilities.lineEnding);
 
 				if (line.trim().equals(this.beginCut.trim())) {
 					ignoreLine = true;
 				} else if (line.trim().equals(this.endCut.trim())) {
 					ignoreLine = false;
 				} else if (!ignoreLine) {
 					source.append(line);
 					source.append(Utilities.lineEnding);
 				}
 			}
 			in.close();
 		} catch (IOException e) {
 			writeLog("Error reading source code from file " + f
 					+ ": " + e.toString() + Utilities.lineEnding
 					+ "Returning nothing.");
 			return "";
 		}
 		writeLog("Source read from file " + f);
 
 		if ((this.pref.isPoweredBy())
 				&& (!source.toString().endsWith(EntryPoint.POWEREDBY))
 				&& (source.length() != 0)) {
 			source.append(Utilities.lineEnding);
 			source.append(Utilities.lineEnding);
 			source.append(EntryPoint.POWEREDBY);
 		}
 
 		String sig = getSignature();
 		if ((!source.toString().startsWith(sig)) && (source.length() != 0)) {
 			source.insert(0, sig);
 		}
 
 		return source.toString();
 	}
 
 	static public String join(String[] list, String conjunction) {
 		StringBuilder sb = new StringBuilder();
 		boolean first = true;
 		for (String item : list) {
 			if (first)
 				first = false;
 			else
 				sb.append(conjunction);
 			sb.append(item);
 		}
 		return sb.toString();
 	}
 
 	public static String expandYear(String input, String prefix) {
 		Pattern p = Pattern.compile(prefix + "(\\d{2,2})");
 		Matcher m = p.matcher(input);
 		if (m.find()) {
 			return m.replaceAll(prefix + "20" + m.group(1));
 		}
 		return input;
 	}
 
 	private void loadDirFileNames() {
 		this.fileName = this.pref.isUseClassName()
 				? this.component.getClassName() : this.pref.getFileName();
 
 		String dirPrefix = this.pref.getDirectoryName();
 		RoundModel round = this.component.getProblem().getRound();
 		roundName = round.getDisplayName();
 		roundName = roundName.replace((CharSequence) "-", "");
 		roundName = roundName.replace((CharSequence) "'", "");
 		if (roundName.matches("SRM")) {
 		} else if (roundName.matches("TCHS")) {
 			roundName = expandYear(roundName, "TCHS");
 		} else {
 			roundName = expandYear(roundName, "TCO");
 			roundName = expandYear(roundName, "TCCC");
 			roundName = roundName.replace((CharSequence) "Algorithm",
 					(CharSequence) "");
 			String[] names = roundName.split(" ");
 			try {
 				Integer.parseInt(names[0]);
 				String tmp = names[0];
 				names[0] = names[1];
 				names[1] = tmp;
 			} catch (Exception e) {
 
 			}
 			roundName = join(names, "");
 		}
 		roundName = join(roundName.split(" "), "");
 		roundName = roundName.replace((CharSequence) "DIV1", (CharSequence) "");
 		roundName = roundName.replace((CharSequence) "DIV2", (CharSequence) "");
 		File dir = new File(dirPrefix, roundName);
 		this.dirName = dir.getAbsolutePath();
 		if (!dir.exists()) {
 			if (dir.mkdirs()) {
 				writeLog("Directory " + this.dirName + " was created");
 			}
 		}
 	}
 
 	private File newFile(String extension) {
 		return new File(this.dirName, this.fileName + '.' + extension);
 	}
 
 	public static void writeFile(Preferences pref, File f, String content, boolean isBackup) {
 		String fName = f.getAbsolutePath();
 		if (f.exists()) {
 			if (!isBackup) {
 				writeLog("File '"
 						+ f
 						+ "' already exists - not overwriting due to config option");
 				return;
 			}
 			
 			File backup = new File(fName + ".bak");
 			if (backup.exists()) {
 				if (backup.delete())
 					writeLog("Backup file " + backup
 							+ " exists and was deleted");
 				else {
 					writeLog("Error deleting backup file " + backup);
 					return ;
 				}
 			}
 
 			File renameIt = new File(fName);
 			if (renameIt.renameTo(backup)) {
 				writeLog("File " + f + " is being backed up to "
 						+ backup);
 			} else {
 				writeLog("Error backing up file " + f);
 				writeLog("No Backup exists for the file - be careful");
 				return ;
 			}
 		}
 		if (f.exists()) {
 			writeLog("File " + f +" still exist, writing failed!");
 			return;
 		}
 		BufferedReader in = null;
 		BufferedWriter out = null;
 		try {
 			out = new BufferedWriter(new FileWriter(f));
 			in = new BufferedReader(new StringReader(content));
 			while (true) {
 				String line = in.readLine();
 				if (line == null)
 					break;
 				out.write(line);
 				out.write(Utilities.lineEnding);
 			}
 			writeLog("Successfully written to " + f);
 			in.close();
 			out.flush();
 			out.close();
 		} catch (IOException e) {
 			writeLog("Failed written to" + f + ": " + e.toString());
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	private String getTextDesc() {
 		String desc = ""; 
 		try {
 				desc = this.renderer.toPlainText(this.language);
 		} catch (Exception e) {
 			System.err.println("Exception happened during rendering into text: " + e);
 			desc = "<html><body>Error happened - see applet for problem text</body></html>";
 		}
 		return desc;
 	}
 
 	static String getSourceExtension(Preferences pref, String lang) {
 		if (lang == "Java") {
 			return pref.getJAVAExtension(); 
 		} else if (lang == "C++") {
 			return pref.getCPPExtension();
 		} else if (lang == "C#") {
 			return pref.getCSHARPExtension();
 		} else {
 			/*TODO: Add other language support */
 			return pref.getJAVAExtension();
 		}
 	}
 
 	private void generateHtml(){
 		if (!this.pref.isWriteHtmlDescFile()) {
 			return;
 		}
 		File f = newFile("html");
 		String desc = ""; 
 		try {
 				desc = this.renderer.toHTML(this.language);
 		} catch (Exception e) {
 			System.err.println("Exception happened during rendering into HTML: " + e);
 			desc = "<html><body>Error happened - see applet for problem text</body></html>";
 		}
 		writeFile(this.pref, f, desc, pref.isBackup());
 	}
 	
 	private void generateText(String textDesc){
 		if (!this.pref.isWriteTextDescFile()) {
 			return;
 		}
 		File f = newFile(this.pref.getTextDescExtension());
 		writeFile(this.pref, f, textDesc, pref.isBackup());
 	}
 
 	private void generateSource(String textDesc) {
 		String extension = getSourceExtension(this.pref, this.language.getName()); 
 		File f = newFile(extension);
 		String source = Utilities.getSource(
 				this.language,
 				this.component,
 				this.pref.isWriteCodeDescFile()
 					? Utilities.parseProblem(textDesc) : "",
 				this.roundName,
 				this.fileName);
 		source = Utilities.replaceUserDefined(source, this.userDefinedTags);
 		writeFile(this.pref, f, source, pref.isBackup());
 	}
 	
	private void writeCommitedSource(String source) {
 		String extension = getSourceExtension(this.pref, this.language.getName()); 
		writeFile(this.pref, newFile("commited." + extension), source, true);
 	}
 
 	public void setSource(String source) {
 		this.loadDirFileNames();
 		if (source == null
 				|| source == ""
 				|| source.equals(this.component.getDefaultSolution())) {
 			/* 
 			 * The first time setting the source code, when opened the problem
 			 * We doesn't get the source yet.
 			 */
 			String textDesc = getTextDesc();
 			generateHtml();
 			generateText(textDesc);
 			generateSource(textDesc);
 		} else {
 			/* 
 			 * The committed source code, if we doesn't committed before
 			 * then this condition won't happen.
 			 */ 
			writeCommitedSource(source);
 		}
 	}
 
 	public void update(Observable o, Object a) {
 		loadPreferences();
 	}
 
 	private final void loadPreferences() {
 		this.beginCut = this.pref.getBeginCut();
 		this.endCut = this.pref.getEndCut();
 	}
 
 	private final static void writeLog(String text) {
 		System.out.println(text);
 		log.append(text);
 		log.append("\n");
 		log.setCaretPosition(log.getDocument().getLength() - 1);
 	}
 
 	public static void main(String[] args) {
 		List<String> parms = new ArrayList<String>();
 		parms.add("String");
 		parms.add("String");
 		parms.add("int");
 
 		Editor en = new Editor();
 		System.out.println(en.getSource());
 	}
 }
