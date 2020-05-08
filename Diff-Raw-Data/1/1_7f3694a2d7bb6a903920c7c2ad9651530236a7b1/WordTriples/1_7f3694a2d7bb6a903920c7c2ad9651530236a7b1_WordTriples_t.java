 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 
 public class WordTriples extends JFrame {
 
 	JTabbedPane steps;
 	
 	SelectHypFileUI selectHypFileUI;
 	File hypFile;
 	ArrayList hypotheses;
 	ShowLoadedHypsUI showLoadedHypsUI;
 	ShowWordsUI showWordsUI;
 	HashMap wordsAndCounts;
 			
 	public WordTriples () {
 		super("Word Triples Analyzer");
 				
 		addWindowListener(new ApplicationCloser());
 		Container contentPane = getContentPane();
 		
 		selectHypFileUI = new SelectHypFileUI();
 		hypFile = null;
 		
 		hypotheses = null;
 		wordsAndCounts = null;
 		
 		showLoadedHypsUI = new ShowLoadedHypsUI();
 		showWordsUI = new ShowWordsUI();
 		
 		steps = new JTabbedPane();
 		steps.addTab("(0)Select Input File", selectHypFileUI);
 		steps.addTab("(1)Hypotheses found in Input File", showLoadedHypsUI);
 		steps.addTab("(2)Form word groups", showWordsUI);
 		
 		contentPane.add(steps);
 		
 		steps.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				switch (steps.getSelectedIndex()) {
 				case 0:
 					break;
 				
 				case 1:
 					loadAndShowHyps();
 					break;
 				
 				case 2:
 					showAndEditWords();
 					break;
 				
 				}
 			}
 		});
 		
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		WordTriples wordTriples = new WordTriples();
 		wordTriples.setSize(800,600);
 		wordTriples.show();
 
 	}
 
 	
 	JPanel makeLoadHypsPane() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout());
 		panel.add(new JLabel("hi there"), BorderLayout.NORTH);
 		
 		return panel;
 	}
 	
 	private void loadAndShowHyps() {
 		if (selectHypFileUI.getSelectedHypFile() != null) {
 			hypotheses = selectHypFileUI.getLoadedHyps();
 			Iterator hypothesisIterator = hypotheses.iterator();
 			showLoadedHypsUI.createTable(hypotheses.size());
 			int rowNumber = 0;
 			while (hypothesisIterator.hasNext()){
 				Hypothesis hypothesis = (Hypothesis)hypothesisIterator.next();
 					showLoadedHypsUI.addHyp(rowNumber,
 					hypothesis.getNumber(), 
 					hypothesis.getHypothesisText(),
 					hypothesis.getScore());
 				rowNumber++;
 			}
 			showLoadedHypsUI.setInfoLabelText(
 					"You selected "
 					+ selectHypFileUI.getSelectedHypFile().getName().toString() 
 					+ " as the input file."
 					+ " I found "
 					+ hypotheses.size()
 					+ " hypotheses.");
 
 		} else {
 			showLoadedHypsUI.setInfoLabelText(
 					"No hypothesis file selected.");
 		}
 	}
 	
 	private void showAndEditWords() {
 		if (hypotheses != null) {
 			Iterator hypothesisIterator = hypotheses.iterator();
 			wordsAndCounts = new HashMap();
 			while(hypothesisIterator.hasNext()){
 				Hypothesis hypothesis = (Hypothesis)hypothesisIterator.next();
 				ArrayList wordSet = hypothesis.getWordSet();
 				Iterator wordIterator = wordSet.iterator();
 				while (wordIterator.hasNext()) {
 					String wordText = (String)wordIterator.next();
 					if (wordsAndCounts.containsKey(wordText)){
 						int oldCount = 
 							((Integer)wordsAndCounts.get(wordText)).intValue();
 						wordsAndCounts.put(wordText, new Integer(oldCount + 1));
 					} else {
 						wordsAndCounts.put(wordText, new Integer(1));
 					}
 				}
 			}
 			Iterator wordListIterator = wordsAndCounts.keySet().iterator();
 			showWordsUI.createTable(wordsAndCounts.size());
 			int rowNumber = 0;
 			while (wordListIterator.hasNext()) {
 				String wordText = (String)wordListIterator.next();
 				int count = ((Integer)wordsAndCounts.get(wordText)).intValue();
 				showWordsUI.addWord(rowNumber, wordText, count, 0);
				System.out.println(wordText + "," + count);
 				rowNumber++;
 			}
 			showWordsUI.setInfoLabelText("I found " + rowNumber	+ " words.");
 
 		} else {
 			showWordsUI.setInfoLabelText("No hypotheses loaded.");
 		}
 	}
 	
 	class ApplicationCloser extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {
 			System.exit(0);
 		}
 	}
 
 }
