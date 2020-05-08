 package sort;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.net.URISyntaxException;
 import java.security.CodeSource;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import members.Competitor;
 import result.CvsReader;
 import result.Parser;
 import result.ParserException;
 
 public class ResultCompilerMain {
 
 	public static final String STARTTIMES = "start";
 	public static final String NAMEFILE = "namn";
 	public static final String FINISHTIMES = "slut";
 	public static final String RESULTFILE = "resultat";
 	public static final String EXTENSION = ".txt";
 
 	/**
 	 * 
 	 * Read input files and create list with competitors, and call printResults
 	 * to print the results to the output file.
 	 * 
 	 * @throws URISyntaxException
 	 * 
 	 */
 	public static void main(String[] args) throws URISyntaxException {
 		// Detects the location of the executable.
 		CodeSource codeSource = ResultCompilerMain.class.getProtectionDomain()
 				.getCodeSource();
 		File jarFile = new File(codeSource.getLocation().toURI().getPath());
 		String jarDir = jarFile.getParentFile().getPath();
 
 		Parser p = new Parser();
 
 		String startPath = jarDir + File.separator + STARTTIMES + EXTENSION;
 		String namePath = jarDir + File.separator + NAMEFILE + EXTENSION;
 		String finishPath = jarDir + File.separator + FINISHTIMES + EXTENSION;
 		String resultPath = jarDir + File.separator + RESULTFILE + EXTENSION;
 
 		CvsReader startReader = new CvsReader(startPath);
 		CvsReader nameReader = new CvsReader(namePath);
 		CvsReader endReader = new CvsReader(finishPath);
 
 		// Reads multiple end files for laps.
 		// ArrayList<CvsReader> endReaderList = new ArrayList<CvsReader>();
 		//for (int i = 0; new File(finishPathPart + i + EXTENSION).exists(); i++) {
 		//	endReaderList.add(new CvsReader(finishPathPart + i + EXTENSION));
 		//}
 
 		Map<Integer, Competitor> map = new HashMap<Integer, Competitor>();
 
 		try {
 			// Read starts.
 			map = p.parse(startReader.readAll(), map);
 			// Read ends
 			//for (CvsReader end : endReaderList) {
 				map = p.parse(endReader.readAll(), map);
 			//}
 			// Read Names
 			map = p.parse(nameReader.readAll(), map);
 			StdCompetitorPrinter printer = new StdCompetitorPrinter();
 			printer.printResults(new ArrayList<Competitor>(map.values()),
 					resultPath);
 			System.out.println("Finished results compilation.");
 		} catch (FileNotFoundException e) {
 			errorMessage(e.getMessage());
 		} catch (ParserException e) {
			errorMessage(e.getMessage());
 			System.exit(-1);
 		}
 	}
 
 	private static void errorMessage(String e) {
 		JFrame frame = new JFrame();
 		JOptionPane.showMessageDialog(frame, e, "FEL", JOptionPane.ERROR_MESSAGE);
 		frame.dispose();
 	}
 
 }
