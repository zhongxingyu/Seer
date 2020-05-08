 package kanjieditor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 import javax.swing.JFileChooser;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import vocab.Kanji;
 
 
 /**
  * The open menu item.
  */
 public class OpenMenuItem extends JMenuItem implements ActionListener {
 
 	/** The Constant serialVersionUID. */
 	private static final long serialVersionUID = -1550475588185729536L;
 	
 	/** The editor. */
 	private KanjiEditor editor;
 	
 	/**
 	 * Instantiates a new open menu item.
 	 *
 	 * @param wordEditor the word editor
 	 */
 	public OpenMenuItem(KanjiEditor wordEditor){
 		super("Open");
 		this.editor = wordEditor;
 		addActionListener(this);
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	public void actionPerformed(ActionEvent arg0) {
 	    JFileChooser chooser = new JFileChooser();
	    chooser.setDialogTitle("Open kanji list");
 	    chooser.showOpenDialog(null);
 	    File file = chooser.getSelectedFile();
 	    ArrayList<Kanji> newlist;
 	    if (file == null){return;}
 		try{
 			newlist = KanjiEditorControl.loadFromFile(file);
 			editor.load(newlist);
 		}catch(Exception e){
 			JOptionPane.showMessageDialog(null, "Error loading file!");
 		}
 	}
 }
