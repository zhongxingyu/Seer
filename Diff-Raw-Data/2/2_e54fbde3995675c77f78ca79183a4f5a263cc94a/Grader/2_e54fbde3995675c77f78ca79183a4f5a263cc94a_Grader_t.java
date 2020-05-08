 package Grader;
 
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.util.TreeMap;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.ProgressMonitor;
 import javax.swing.Timer;
 import javax.swing.text.Caret;
 
 import GeneticModels.GeneticModel;
 import VGL.GeneticModelAndCageSet;
 import VGL.VGLII;
 
 public class Grader extends JFrame {
 
 	private File workingDir;
 	private VGLII vglII;
 
 	private Timer fileLoadingTimer;
 	private WorkFileLoader workFileLoader;
 	private ProgressMonitor fileLoadingProgressMonitor;
 
 	private JList workFileList;
 	private DefaultListModel workFileNames;
 
 	public JEditorPane correctAnswer;
 	public JScrollPane correctAnswerScroller;
 	public Caret topOfCorrectAnswer;
 
 	public JEditorPane theirAnswer;
 	public JScrollPane theirAnswerScroller;
 	public Caret topOfTheirAnswer;
 
 	private TreeMap<String, GeneticModelAndCageSet> filenamesAndResults;
 
 	public Grader(File workingDir, VGLII vglII) {
 		this.workingDir = workingDir;
 		this.vglII = vglII;
 		fileLoadingTimer = new Timer(100, new FileLoadingTimerListener());
 		filenamesAndResults = new TreeMap<String, GeneticModelAndCageSet>();
 		setupUI();
 		pack();
 		setVisible(true);
 	}
 
 	private void setupUI() {
 		JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
 		mainPanel.add(Box.createRigidArea(new Dimension(1,400)));
 
 		JPanel leftPanel = new JPanel();
 		leftPanel.setBorder(BorderFactory.createTitledBorder("Work Files"));
 		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
 		leftPanel.add(Box.createRigidArea(new Dimension(300,1)));
 
 		workFileNames = new DefaultListModel();
 		workFileList = new JList(workFileNames);
 		workFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		workFileList.setLayoutOrientation(JList.VERTICAL);
 		workFileList.setVisibleRowCount(-1);
 		JScrollPane listScroller = new JScrollPane(workFileList);
 		listScroller.setPreferredSize(new Dimension(300,80));
 		leftPanel.add(listScroller);
 
 		workFileList.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent evt) {
 				JList list = (JList) evt.getSource();
 				boolean showCagesEtc = false;
 				if ((evt.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK) { 
 					showCagesEtc = true;
 				} 
 
 				String workFileName =
 					(workFileNames.get((list.locationToIndex(evt.getPoint())))).toString();
 				showWorkByName(workFileName, showCagesEtc);
 			}
 
 		});
 
 		mainPanel.add(leftPanel);
 
 		JPanel correctAnswerPanel = new JPanel();
 		correctAnswerPanel.setBorder(BorderFactory.createTitledBorder("Correct Answer"));
 		correctAnswerPanel.setLayout(new BoxLayout(correctAnswerPanel, BoxLayout.Y_AXIS));
 		correctAnswerPanel.add(Box.createRigidArea(new Dimension(300,1)));
 		correctAnswer = new JEditorPane();
 		correctAnswer.setContentType("text/html");
 		correctAnswerScroller = new JScrollPane(correctAnswer);
 		correctAnswerScroller.setPreferredSize(new Dimension(300,80));
 		topOfCorrectAnswer = correctAnswer.getCaret();
 		correctAnswerPanel.add(correctAnswerScroller);
 
 		mainPanel.add(correctAnswerPanel);
 
 		JPanel theirAnswerPanel = new JPanel();
 		theirAnswerPanel.setBorder(BorderFactory.createTitledBorder("Student's Answer"));
 		theirAnswerPanel.setLayout(new BoxLayout(theirAnswerPanel, BoxLayout.Y_AXIS));
 		theirAnswerPanel.add(Box.createRigidArea(new Dimension(300,1)));
 		theirAnswer = new JEditorPane();
 		theirAnswer.setContentType("text/html");
 		theirAnswerScroller = new JScrollPane(theirAnswer);
 		theirAnswerScroller.setPreferredSize(new Dimension(300,80));
 		topOfTheirAnswer = theirAnswer.getCaret();
 		theirAnswerPanel.add(theirAnswerScroller);
 
 		mainPanel.add(theirAnswerPanel);
 
 
 		getContentPane().setLayout(new BorderLayout());
 		getContentPane().add(mainPanel, BorderLayout.CENTER);
 	}
 
 	public void openDirectoryAndLoadFiles() {
 		String[] files = workingDir.list();
 		for (int i = 0; i < files.length; i++) {
 			if (files[i].endsWith(".wr2")) {
 				workFileNames.addElement(files[i]);
 			}
 		}
 		workFileLoader = new WorkFileLoader(
 				workingDir, 
 				workFileNames, 
 				filenamesAndResults);
 		Thread t = new Thread(workFileLoader);
 		t.start();
 		fileLoadingTimer.start();
 		fileLoadingProgressMonitor = new ProgressMonitor(
 				Grader.this,
 				"Reading in " + workFileNames.getSize() + " work files.",
 				"",
 				0, 
 				workFileLoader.getLengthOfTask());
 	}
 
 	class FileLoadingTimerListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if(fileLoadingProgressMonitor.isCanceled() || 
 					(workFileLoader.getProgress() == workFileLoader.getLengthOfTask())) {
 				workFileLoader.stop();
 				fileLoadingTimer.stop();
 				Grader.this.setCursor(Cursor.DEFAULT_CURSOR);
 				fileLoadingProgressMonitor.close();
 				workFileList.setEnabled(true);
 			} else {
 				Grader.this.setCursor(Cursor.WAIT_CURSOR);
 				fileLoadingProgressMonitor.setProgress(workFileLoader.getProgress());
 				workFileList.setEnabled(false);
 			}
 		}		
 	}
 
 	private void showWorkByName(String fileName, boolean showCagesEtc) {
 		
 		vglII.cleanUp();
 		
 		GeneticModelAndCageSet result = filenamesAndResults.get(fileName);
 
 		vglII.setupForGrading(result, showCagesEtc);
 
 		GeneticModel geneticModel = result.getGeneticModel();
 		String answer = geneticModel.toString();
 		answer = answer.replace("<body>", "<body><font color=red size=+2>" 
 				+ fileName 
 				+ " " + makeBeginnerModeString(geneticModel)
 				+ "</font><hr>");
 		correctAnswer.setText(answer);
 		correctAnswer.setCaret(null);
 		correctAnswer.setCaret(topOfCorrectAnswer);
 
 		theirAnswer.setText(vglII.getModelBuilder().getAsHtml());
 		theirAnswer.setCaret(null);
 		theirAnswer.setCaret(topOfTheirAnswer);
 
		if (showCagesEtc) this.toFront();
 	}
 
 	private String makeBeginnerModeString(GeneticModel model) {
 		StringBuffer buf = new StringBuffer();
 		buf.append("Beginner Mode ");
 		if(model.isBeginnerMode()) {
 			buf.append("On");
 		} else {
 			buf.append("Off");
 		}
 		buf.append(" ");
 		return buf.toString();
 	}
 
 }
