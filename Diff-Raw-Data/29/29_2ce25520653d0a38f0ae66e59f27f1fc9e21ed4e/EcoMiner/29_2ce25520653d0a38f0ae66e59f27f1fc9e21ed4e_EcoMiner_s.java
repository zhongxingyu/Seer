 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.Timer;
 import javax.swing.text.html.HTMLDocument;
 
 public class EcoMiner extends JFrame {
 
 	private static final String versionString = "1.0";
 	
 	private static final String htmlStart = "<html><body>";
 	private static final String htmlEnd = "</html></body>";
 	private static final String step1String = 
 		"<li><font color=black>Select Species Distribution arff File</font></li>";
 	private static final String step2String =
 		"<li><font color=black>Select Climate Data File</font></li>";
 	private static final String step3String =
 		"<li><font color=black>Save Results as csv File</font></li>";
 	private static final String step4String =
 		"<li><font color=black>All Done</font></li>";
 	
 	private Point centerOfScreen;
 	
 	private File wekaJar;
 	private String wekaJarFilename;
 	
 	private int currentStep;
 	
 	private JButton wekaButton;
 	private JButton printButton;
 	private JButton useLastClassifierButton;
 	
 	private JLabel stepsLabel;
 	private JTextPane resultPane;
 	private JScrollPane rpScrollPane;
 	private JButton nextButton;
 	private JButton backButton;
 	
 	private File speciesDistributionFile;
 	private String speciesName;
 	private ClassifierGenerator cg;
 
 	private DocumentRenderer docRenderer;
 
 	private File classifier;
 	
 	private ClassifierRunner cr;
 	
 	private File climateDistributionFile;
 	private ArffReader climateDataReader;
 	private ArrayList climateAttributes;
 	private ArrayList climateInstances;
 	
 	private File resultFile;
 		
 	public EcoMiner() {		
 		super("EcoMiner version " + versionString);
 		getContentPane().setLayout(new BorderLayout());
 		
 		JPanel topButtonPanel = new JPanel();
 		wekaButton = new JButton("Run WEKA directly");
 		printButton = new JButton("Print");
 		useLastClassifierButton = new JButton("Use Last Saved Classifier");
 		topButtonPanel.add(wekaButton);
 		topButtonPanel.add(printButton);
 		topButtonPanel.add(useLastClassifierButton);
 		getContentPane().add(topButtonPanel, BorderLayout.NORTH); 
 				
 		stepsLabel = new JLabel();
 		stepsLabel.setVerticalAlignment(JLabel.TOP);
 		stepsLabel.setBorder(BorderFactory.createEtchedBorder());
 		stepsLabel.setPreferredSize(new Dimension(150,300));
 		getContentPane().add(stepsLabel, BorderLayout.WEST);
 		
 		resultPane = new JTextPane();
 		resultPane.setContentType("text/html");
 		resultPane.setDragEnabled(false);
 		resultPane.setEditable(false);
 
 		rpScrollPane = new JScrollPane(resultPane);
 		rpScrollPane.setBorder(BorderFactory.createEtchedBorder());
 		rpScrollPane.setPreferredSize(new Dimension(500,300));
 		getContentPane().add(rpScrollPane, BorderLayout.CENTER);
 		
 		nextButton = new JButton("NEXT");
 		backButton = new JButton("BACK");
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.add(backButton);
 		buttonPanel.add(nextButton);
 		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
 		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
 				
 		wekaJarFilename = "workspace/weka.jar";
 		wekaJar = new File (wekaJarFilename);
 		if (!wekaJar.exists()) {
 			MoveableJFileChooser fc = new MoveableJFileChooser();
 			fc.setDialogTitle("File weka.jar not found; please locate!");
 			int returnVal = fc.showOpenDialog(this);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				wekaJar = fc.getSelectedFile();
 				wekaJarFilename = wekaJar.getAbsolutePath();
 				System.out.println(wekaJarFilename);
 				if (!wekaJar.getName().equals("weka.jar")) {
 					JOptionPane.showMessageDialog(null, "Incorrect file selected; exiting.", 
 							"Incorrect file", JOptionPane.ERROR_MESSAGE);
 					System.exit(0);
 				}
 			} else {
 				System.exit(0);
 			}
 			
 		}
 		
 		docRenderer = new DocumentRenderer();
 		
 		speciesName = "";
 
 		currentStep = 0;
 		doStep();
 		
 		wekaButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Runtime rt = Runtime.getRuntime();
 				try {
 					Process proc = rt.exec("java -Xmx300m -jar " + wekaJarFilename);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		printButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				HTMLDocument htdoc = (HTMLDocument)resultPane.getDocument();
 				docRenderer.print(htdoc);
 			}
 		});
 		
 		useLastClassifierButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				
 				//find first (and hopefully only) .classifier file
 				File workspaceDir = new File("workspace");
 				File[] workspaceFiles = workspaceDir.listFiles();
 				for (int i = 0; i < workspaceFiles.length; i++) {
 					File file = (File)workspaceFiles[i];
 					if (file.getName().endsWith(".classifier")) {
 						classifier = file;
 						break;
 					}
 				}
 				
 				speciesName = classifier.getName().replaceAll(".arff.classifier", "");
 				currentStep = 2;
 				doStep();
 			}
 		});
 		
 		nextButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				currentStep++;
 				doStep();
 			}
 		});
 		
 		backButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				currentStep--;
 				doStep();
 			}
 		});
 		
 		speciesDistributionFile = null;
 		climateDistributionFile = null;
 		resultFile = null;
 
 	}
 
 	public static void main(String[] args) {
 		EcoMiner eM = new EcoMiner();
 
 		eM.pack();
 		eM.show();
 		
 		eM.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
 
 	}
 	
 	private void doStep() {
 		
 		switch (currentStep) {
 			
 		case 0:
 			doStep0();
 			break;
 			
 		case 1:
 			doStep1();			
 			break;
 			
 		case 2:
 			doStep2();
 			break;
 			
 		case 3:
 			doStep3();
 			break;
 			
 		case 4:
 			doStep4();
 			break;
 			
 		case 5:
 			System.exit(0);
 		}
 		
 	}
 	
 	private void doStep0() {
 		
 		printButton.setEnabled(false);
 		useLastClassifierButton.setEnabled(true);
 		
 		nextButton.setEnabled(true);
 		backButton.setEnabled(false);
 		
 		stepsLabel.setText(htmlStart
 				+ "<ol>"
 				+ step1String
 				+ step2String
 				+ step3String
 				+ step4String
 				+ "</ol>"
 				+ htmlEnd);
 		
 		resultPane.setText(htmlStart
 				+ "<center><h3>Ready to Start; click &quot;NEXT&quot;</center></h3>"
 				+ htmlEnd);
 	}
 	
 	private void doStep1() {
 		
 		printButton.setEnabled(false);
 		nextButton.setEnabled(true);
 		backButton.setEnabled(true);
 		
 		stepsLabel.setText(htmlStart
 				+ "<ol>"
 				+ step1String.replaceAll("black", "red")
 				+ step2String
 				+ step3String
 				+ step4String
 				+ "</ol>"
 				+ htmlEnd);
 		
 		resultPane.setText(htmlStart
 				+ "Select arff file for a particular species.<br>"
 				+ "This will be used to make a classifier."
 				+ htmlEnd);
 		
 		MoveableJFileChooser fc = new MoveableJFileChooser();
 		fc.setFileFilter(new ArffFileFilter());
 		fc.setDialogTitle("Choose Species Distribution .arff file");
 		fc.setCurrentDirectory(new File("workspace/SpeciesDistributionFiles"));
 		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		int returnVal = fc.showOpenDialog(this);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			speciesDistributionFile = fc.getSelectedFile();
 			speciesName = speciesDistributionFile.getName().replaceAll(".arff", "");
 
 			//clean up old classifiers
 			File workspaceDir = new File("workspace/");
 			File[] workspaceFiles = workspaceDir.listFiles();
 			for (int i = 0; i < workspaceFiles.length; i++) {
 				File file = (File)workspaceFiles[i];
 				if (file.getName().endsWith(".classifier")) {
 					file.delete();
 				}
 			}
 			
 			wekaButton.setEnabled(false);
 			useLastClassifierButton.setEnabled(false);
 			backButton.setEnabled(false);
 			nextButton.setEnabled(false);
 			
 			//first, make temporary version of the species distrib file
 			// without the potentially distracting attributes: site, lat, and long
 			Runtime rt = Runtime.getRuntime();
 			try {
 				Process proc = rt.exec("java -cp " + wekaJarFilename 
 						+ " weka.filters.unsupervised.attribute.Remove -R 1-3"
						+ " -i " + speciesDistributionFile.getAbsolutePath()
 						+ " -o workspace/temp.arff");
 				proc.waitFor();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
			
 			//now build classifier on reduced file
 			cg = new ClassifierGenerator(
						"java -cp " + wekaJarFilename + " -mx300m "
						+ "weka.classifiers.rules.JRip -F 3 -N 2.0 -O 2 -S 1 "
 						+ "-t workspace/temp.arff "  //training file
 						+ "-T workspace/temp.arff "  //test with same
 												   //file to eliminate x-validation
 						+ "-i -k "
						+ "-d workspace" 
						+ System.getProperty("file.separator")
 						+ speciesDistributionFile.getName()
 						+ ".classifier");
 			
 			classifier = new File("workspace"
 					+ System.getProperty("file.separator")
 					+ speciesDistributionFile.getName()
 					+ ".classifier");
 			
 			cg.go();
 			generatorTimer.start();
 						
 		} else {
 			currentStep = 0;
 			doStep();
 		}
 	}
 	
 	private Timer generatorTimer = new Timer(500, new ActionListener() {
 		double time = 0;
 		int minutes;
 		int seconds;
 		boolean whichVersion = false;
 		public void actionPerformed(ActionEvent e) {
 			
 			time = time + 0.5;
 			minutes = (int) time/60;
 			seconds = (int) time%60;
 			
 			if(whichVersion) {
 				resultPane.setText(htmlStart
 						+ "<font color=blue>Making classifier, please be patient...</font><br>"
 						+ "Elapsed time = " + minutes + " minutes and " + seconds + " seconds."
 						+ htmlEnd);
 				whichVersion = false;
 			} else {
 				resultPane.setText(htmlStart
 						+ "<font color=purple>Making classifier, please be patient......</font><br>"
 						+ "Elapsed time = " + minutes + " minutes and " + seconds + " seconds."
 						+ htmlEnd);		
 				whichVersion = true;
 			}
 			
 			if (cg.done()){
 				Toolkit.getDefaultToolkit().beep();
 				generatorTimer.stop();
 				resultPane.setText(htmlStart
 						+ "<font color=green>Classifier successfully created.<br>"
 						+ "Used " +  speciesDistributionFile.toString() + " as training file.<br>"
 						+ "Elapsed time = " + minutes + " minutes and " + seconds + " seconds.<br>"
 						+ "Click &quot;NEXT&quot; to continue.</font>"
 						+ "<hr>"
 						+ "<pre>" + cg.getResult() + "</pre>"
 						+ htmlEnd);
 				printButton.setEnabled(true);
 				wekaButton.setEnabled(true);
 				useLastClassifierButton.setEnabled(true);
 				backButton.setEnabled(true);
 				nextButton.setEnabled(true);
 				
 				File tempFile = new File("workspace/temp.arff");
 				tempFile.delete();
 			}
 		}
 	});
 	
 	private void doStep2() {
 		
 		printButton.setEnabled(false);
 		nextButton.setEnabled(true);
 		backButton.setEnabled(true);
 		
 		stepsLabel.setText(htmlStart
 				+ "<ol>"
 				+ step1String
 				+ step2String.replaceAll("black", "red")
 				+ step3String
 				+ step4String
 				+ "</ol>"
 				+ htmlEnd);
 		
 		resultPane.setText(htmlStart
 				+ "Select arff climate file .<br>"
 				+ "The classifier will predict the distribution of the species<br>" 
 				+"using this climate distribution."
 				+ htmlEnd);
 		
 		MoveableJFileChooser fc = new MoveableJFileChooser();
 		fc.setFileFilter(new ArffFileFilter());
 		fc.setDialogTitle("Choose Climate arff file");
 		fc.setCurrentDirectory(new File("workspace/ClimateFiles/"));
 		int returnVal = fc.showOpenDialog(this);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			climateDistributionFile = fc.getSelectedFile();
 			
 			climateDataReader = new ArffReader(climateDistributionFile);
 			climateDataReader.go();
 			readerTimer.start();
 			
 		} else {
 			currentStep = 0;
 			doStep();
 		}
 	}
 	
 	private Timer readerTimer = new Timer(500, new ActionListener() {
 		double time = 0;
 		int minutes;
 		int seconds;
 		boolean whichVersion = false;
 		public void actionPerformed(ActionEvent e) {
 			
 			time = time + 0.5;
 			minutes = (int) time/60;
 			seconds = (int) time%60;
 			
 			if(whichVersion) {
 				resultPane.setText(htmlStart
 						+ "<font color=blue>Reading in climate file, please be patient...</font><br>"
 						+ climateDataReader.getNumAttributes() + " attributes loaded; "
 						+ climateDataReader.getNumInstances() + " sites loaded.<br>"
 						+ "Elapsed time = " + minutes + " minutes and " + seconds + " seconds."
 						+ htmlEnd);
 				whichVersion = false;
 			} else {
 				resultPane.setText(htmlStart
 						+ "<font color=purple>Reading in climate file, please be patient......</font><br>"
 						+ climateDataReader.getNumAttributes() + " attributes loaded; "
 						+ climateDataReader.getNumInstances() + " sites loaded.<br>"
 						+ "Elapsed time = " + minutes + " minutes and " + seconds + " seconds."
 						+ htmlEnd);		
 				whichVersion = true;
 			}
 			
 			if (climateDataReader.done()){
 				Toolkit.getDefaultToolkit().beep();
 				readerTimer.stop();
 				
 				climateAttributes = climateDataReader.getAttributes();
 				climateInstances = climateDataReader.getInstances();
 				
 				resultPane.setText(htmlStart
 						+ "<font color=green>Climate file successfully classified.<br>"
 						+ "Used " +  climateDistributionFile.toString() + " as climate file.<br>"
 						+ climateDataReader.getNumAttributes() + " attributes loaded; "
 						+ climateDataReader.getNumInstances() + " sites loaded.<br>"
 						+ "Elapsed time = " + minutes + " minutes and " + seconds + " seconds.<br>"
 						+ "Click &quot;NEXT&quot; to continue.</font>"
 						+ "<hr>"
 						+ htmlEnd);
 				printButton.setEnabled(false);
 				wekaButton.setEnabled(true);
 				useLastClassifierButton.setEnabled(true);
 				backButton.setEnabled(true);
 				nextButton.setEnabled(true);
 			}
 		}
 	});
 	
 	private void doStep3() {
 				
 		printButton.setEnabled(false);
 		nextButton.setEnabled(true);
 		backButton.setEnabled(true);
 		
 		stepsLabel.setText(htmlStart
 				+ "<ol>"
 				+ step1String
 				+ step2String
 				+ step3String.replaceAll("black", "red")
 				+ step4String
 				+ "</ol>"
 				+ htmlEnd);
 		
 		resultPane.setText(htmlStart
 				+ "Name a csv file for your results.<br>"
 				+ "The classifier will put the results there.<br>"
 				+ "You can then open the csv file with MyWorld."
 				+ htmlEnd);
 		
 		MoveableJFileChooser fc = new MoveableJFileChooser();
 		fc.setFileFilter(new CsvFileFilter());
 		fc.setDialogTitle("Name Result file");
 		int returnVal = fc.showSaveDialog(this);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			resultFile = fc.getSelectedFile();
 			
 			wekaButton.setEnabled(false);
 			useLastClassifierButton.setEnabled(false);
 			backButton.setEnabled(false);
 			nextButton.setEnabled(false);
 
 			//first, make temporary version of the climate file
 			// without the attributes: site, lat, and long
 			Runtime rt = Runtime.getRuntime();
 			try {
 				Process proc = rt.exec("java -cp " + wekaJarFilename 
 						+ " weka.filters.unsupervised.attribute.Remove -R 1-3"
						+ " -i " + climateDistributionFile.getAbsolutePath()
 						+ " -o workspace/temp.arff");
 				proc.waitFor();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
			
			//now build classifier on reduced file
 
 			cr = new ClassifierRunner("java -cp " 
 					+ wekaJarFilename
					+ " weka.classifiers.rules.JRip -l "
					+ classifier.toString() 
 					+ " -T workspace/temp.arff"
 					+ " -p 1",
 					climateDataReader.getAttributes(),
 					climateDataReader.getInstances(),
 					speciesName,
 					resultFile);
 			cr.go();
 			runnerTimer.start();
 
 		} else {
 			currentStep = 0;
 			doStep();
 		}
 	}
 	
 	private Timer runnerTimer = new Timer(500, new ActionListener() {
 		double time = 0;
 		int minutes;
 		int seconds;
 		boolean whichVersion = false;
 		public void actionPerformed(ActionEvent e) {
 			
 			time = time + 0.5;
 			minutes = (int) time/60;
 			seconds = (int) time%60;
 			
 			if(whichVersion) {
 				resultPane.setText(htmlStart
 						+ "<font color=blue>Running classifier, please be patient...</font><br>"
 						+ "Elapsed time = " + minutes + " minutes and " + seconds + " seconds.<br>"
 						+ cr.getProgress() + " locations classified."
 						+ htmlEnd);
 				whichVersion = false;
 			} else {
 				resultPane.setText(htmlStart
 						+ "<font color=purple>Running classifier, please be patient......</font><br>"
 						+ "Elapsed time = " + minutes + " minutes and " + seconds + " seconds.<br>"
 						+ cr.getProgress() + " locations classified."
 						+ htmlEnd);		
 				whichVersion = true;
 			}
 			
 			if (cr.done()){
 				Toolkit.getDefaultToolkit().beep();
 				runnerTimer.stop();
 				resultPane.setText(htmlStart
 						+ "<font color=green>Climate file successfully classified.<br>"
 						+ "Used " +  climateDistributionFile.toString() + " as climate file.<br>"
 						+ "Used " + classifier.toString() + " as classifier.<br>"
 						+ "Elapsed time = " + minutes + " minutes and " + seconds + " seconds.<br>"	
 						+ cr.getProgress() + " locations classified.<br>"
 						+ "Click &quot;NEXT&quot; to continue.</font>"
 						+ "<hr>"
 						+ htmlEnd);
 				printButton.setEnabled(false);
 				wekaButton.setEnabled(true);
 				useLastClassifierButton.setEnabled(true);
 				backButton.setEnabled(true);
 				nextButton.setEnabled(true);
 				
 				File tempFile = new File("workspace/temp.arff");
 				tempFile.delete();
 			}
 		}
 	});
 	
 	private void doStep4() {
 		
 		printButton.setEnabled(false);
 		nextButton.setEnabled(true);
 		backButton.setEnabled(true);
 		
 		stepsLabel.setText(htmlStart
 				+ "<ol>"
 				+ step1String
 				+ step2String
 				+ step3String
 				+ step4String.replaceAll("black", "red")
 				+ "</ol>"
 				+ htmlEnd);
 		
 		resultPane.setText(htmlStart
 				+ "You are all done.<br>" 
 				+ "Click &quot;NEXT&quot to exit."
 				+ htmlEnd);
 	}
 	
 }
