 package view;
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultHighlighter;
 import javax.swing.text.Highlighter;
 import javax.swing.text.Highlighter.HighlightPainter;
 
 import model.Result;
 
 /**
  * Tab for displaying a full file from the index
  */
 public class FileDisplayTab extends JScrollPane {
 
 	private static final long serialVersionUID = -8170495304477233177L;
 
 	private String file;
 	private JPanel container;
 	private JTextArea textArea;
 	private String fileContent;
 	
 	
 	public FileDisplayTab(String file, List<String> resultsList) {
 		this.file = file;
 		container = new JPanel();
 	    container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
 	 
 		setViewportView(container);
 	    setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 	    createTextView();
 	}
 
 	private void createTextView() {
 		textArea = new JTextArea();
 		textArea.setEditable(false);
 	
 		try {
 			fileContent= new String(readFile());
 
 			textArea.insert(fileContent,0);
 		} catch (IOException e) {
 			System.err.printf("Error reading file %s%n", file);
 			textArea.insert(new String("Error reading file"), 0);
 		}
 		container.add(textArea);
 		
 	}
 
 	private byte[] readFile() throws IOException {
 		return Files.readAllBytes(Paths.get(file));
 	}
 
 	public String getFile() {
 		return file;
 	}
 	public void prepareResultsText(Result result, int chosenResult){
 		Highlighter h = textArea.getHighlighter();
 		h.removeAllHighlights();
 		String sHighlight = "<highlight>";
 		String eHighlight = "</highlight>";
 		for(int i = 0; i < result.getResults().size() ;i++){
 			String r = result.getResults().get(i);
 			r=r.replaceAll(sHighlight, "");
 			r=r.replaceAll(eHighlight, "");
 		        int stringChar= 0;
 		        HighlightPainter p = DefaultHighlighter.DefaultPainter  ;
 		        while ((stringChar = fileContent.indexOf(r, stringChar)) >= 0) {
 		        	try {
 						h.addHighlight(stringChar, stringChar + r.length(), p);
 					} catch (BadLocationException e) {
 						e.printStackTrace();
 					}
 		            if(i == chosenResult){textArea.setCaretPosition(stringChar);}
 		        	stringChar += r.length();
 		        }
 		}
 	}
 }
