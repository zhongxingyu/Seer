 package generator;
 
 import graph.Graph;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import parser.VisualParadigmXmlParser;
 import pattern.MergePattern;
 import pattern.PatternHandler;
 import pattern.SequencePattern;
 import pattern.SplitPattern;
 
 public class App extends JPanel implements ActionListener {
 
 	private static File mInFile;
 	private static File mOutFile;
 
 	private JButton mOpenButton;
 	private JButton mSaveButton;
 	private JButton mGenerateButton;
 	private JFileChooser mFileChooser;
 	private JTextArea mLog;
 
 	public App() {
 		super(new BorderLayout());
 
 		mLog = new JTextArea(5, 20);
 		mLog.setMargin(new Insets(5, 5, 5, 5));
 		mLog.setEditable(false);
 		JScrollPane logScrollPane = new JScrollPane(mLog);
 
 		JLabel inLabel = new JLabel("Input file");
 		add(inLabel);
 
 		mOpenButton = new JButton("Open a File...");
 		mOpenButton.addActionListener(this);
 
 		mSaveButton = new JButton("Save a File...");
 		mSaveButton.addActionListener(this);
 
 		mGenerateButton = new JButton("Generate formula!");
 		mGenerateButton.addActionListener(this);
 		mGenerateButton.setEnabled(false);
 
 		JPanel buttonPanel = new JPanel(); // use FlowLayout
 		buttonPanel.add(mOpenButton);
 		buttonPanel.add(mSaveButton);
 		buttonPanel.add(mGenerateButton);
 
 		// add(buttonPanel, BorderLayout.PAGE_START);
 		add(buttonPanel, BorderLayout.PAGE_START);
 		add(logScrollPane, BorderLayout.CENTER);
 
 		mFileChooser = new JFileChooser(new File("."));
 	}
 
 	private static void createAndShowGUI() {
 		// Create and set up the window.
 		JFrame frame = new JFrame("Generator");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// Add content to the window.
 		frame.add(new App());
 
 		// Display the window.
 		frame.setSize(new Dimension(500, 300));
 		frame.setVisible(true);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == mOpenButton) {
 			int returnVal = mFileChooser.showOpenDialog(this);
 
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				mInFile = mFileChooser.getSelectedFile();
 				mOpenButton.setText("In: " + mInFile.getName());
 				mGenerateButton.setEnabled(true);
 			}
 		} else if (e.getSource() == mSaveButton) {
 			int returnVal = mFileChooser.showOpenDialog(this);
 
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				mOutFile = mFileChooser.getSelectedFile();
 				mSaveButton.setText("Out: " + mOutFile.getName());
 			}
 		} else if (e.getSource() == mGenerateButton) {
 			generateFormula();
 		}
 	}
 
 	void generateFormula() {
 		Graph graph = new VisualParadigmXmlParser().parse(mInFile.getAbsolutePath());
 
 		PatternHandler splitHandler = new PatternHandler(graph, new SplitPattern());
 		PatternHandler mergeHandler = new PatternHandler(graph, new MergePattern());
 		PatternHandler sequencehandler = new PatternHandler(graph, new SequencePattern());
 		splitHandler.process();
 		mergeHandler.process();
 		sequencehandler.process();
 
 		if (graph.getNodes().size() != 1) {
			mLog.append("Something went wrong...");
			mLog.append("DEBUG:");
			mLog.append(graph.toString());
 		} else {
 			mLog.append("Generated formula from " + mInFile.getName() + ": \n");
 
 			final String formula = graph.getNodes().get(0).getFormula();
 			mLog.append("\t" + formula + "\n");
 			// mLog.append("DEBUG: \n" + graph.toString() + "\n");
 
 			if (mOutFile != null) {
 				// TODO: print to file
 				BufferedWriter out;
 				try {
 					out = new BufferedWriter(new FileWriter(mOutFile));
 					out.write(formula);
 					out.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		createAndShowGUI();
 		//
 		// TODO Auto-generated method stub
 		// Graph graph = new Graph();
 		// graph.addNode("1", "a");
 		// graph.addNode("2", "b1");
 		// graph.addNode("3", "b2");
 		// graph.addNode("4", "c1");
 		// graph.addNode("5", "c2");
 		// graph.addNode("6", "d1");
 		//
 		// // split
 		// graph.addConnection("1", "2");
 		// graph.addConnection("1", "3");
 		// // sequence 1
 		// graph.addConnection("2", "4");
 		// graph.addConnection("3", "5");
 		//
 		// // merge
 		// graph.addConnection("4", "6");
 		// graph.addConnection("5", "6");
 		//
 		// System.out.println("*************** przed splitem, merge  i sequencem ************** \n");
 		// System.out.println(graph);
 		// System.out.println("\n\n\n");
 		//
 		// PatternHandler splitHandler = new PatternHandler(graph, new
 		// SplitPattern());
 		// PatternHandler mergeHandler = new PatternHandler(graph, new
 		// MergePattern());
 		// PatternHandler sequencehandler = new PatternHandler(graph, new
 		// SequencePattern());
 		// splitHandler.process();
 		// mergeHandler.process();
 		// sequencehandler.process();
 		//
 		// System.out.println("*************** po splicie, merge'u i sequence ************** \n");
 		// System.out.println(graph);
 	}
 }
