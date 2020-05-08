 /**
  * @author:Alex Bogart, Justin Peterson
  * TextWindow.java represents the actual Swing window that will be kept in the
  * main focus of the HTML editor.  
  * 
  */
 
 
 package Views;
 
 import Tag.TagCollection;
 import Views.TextTabWindow;
 import java.awt.BorderLayout;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.HTMLEditorKit;
 
 public class TextWindow extends JTextArea{
 	
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Constructs an individual window to add to the TextTabWindow. 
 	 * @param tabWindow - the main TextTabWindow of the editor. 
 	 */
 	public TextWindow(String windowName, TextTabWindow tabWindow, TagCollection tabs){
 		setLayout(new BorderLayout(5,10));
 		new JTextArea();
 		
 		//allows line wrapping; defaults to by letter if wrapStyleWord is false
 		setLineWrap(true);
 		
 		//will not work if lineWrap is false, true wraps by word
 		setWrapStyleWord(true);
 		
 		//change the tab size; default is 8
 		setTabSize(8);
 		
 		//opens the right-click menu
 		addMouseListener(new RightClickMenu(this, tabs));
 		
 		//places text area inside a scrolling pane
 		JScrollPane scrollPane = new JScrollPane(this);
 		scrollPane.setBounds(10,60,780,500);
 		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		
		tabWindow.addTab(windowName, scrollPane);
 		setVisible(true);
 	}
 
 }
