 package judo;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Scanner;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.PlainDocument;
 
 public class JudoEditorController {
 	
 	private ArrayList<EditableDocument> documents; 
 	private MainEditorView guiView; 
 	
 	public JudoEditorController()
 	{
 		this.documents = new ArrayList<EditableDocument>();
 		this.guiView = new MainEditorView(this.documents, this);
 		guiView.setVisible(true);
 		
 	}
 	
 //	public void open() throws FileNotFoundException, BadLocationException
 //	{
 //		JFileChooser filechooser = new JFileChooser();
 //		JTextArea txtArea = new JTextArea();
 //		int status = filechooser.showOpenDialog(guiView.getContentPane());
 //		
 //		if(status == JFileChooser.APPROVE_OPTION)
 //		{
 //			Scanner fileScanner = null;
 //			boolean noError = true;
 //			
 //			try
 //			{
 //				fileScanner = new Scanner(filechooser.getSelectedFile());
 //			}
 //			catch(FileNotFoundException e)
 //			{
 //				noError = false;
 //				JOptionPane.showMessageDialog(guiView.getContentPane(), "File not found!");
 //			}
 //			
 //			while(noError && fileScanner.hasNextLine())
 //			{
 //				 txtArea.append(fileScanner.nextLine() + "\n");
 //			}
 //			
 //			guiView.addTextarea(txtArea); //perhaps here I should be modifying the "model" and thus causing the new textpane with
 //		
 //		}
 //    }
 	
 	public void OnViewExitEditor()
 	{
 		this.guiView.CloseEditor();
 	}
 	
 	public void CreateNewDocument()
 	{
 		EditableDocument newDoc = new EditableDocument(new JScrollPane(), new JTextArea(), new PlainDocument(), "");
 		documents.add(newDoc);
 		
 		guiView.getTabbedPane().add("New", newDoc.getScrollpane());
 		guiView.getTabbedPane().setSelectedComponent(newDoc.getScrollpane());
 		guiView.setCurrentDocument(newDoc);
 	}
 	
 	public void saveDocument(EditableDocument d)
 	{
 		EditableDocument doc = this.guiView.getCurrentDocument();
 		if(doc.getFilepath().equals(""))
 		{
 			
 			final JFileChooser chooser = new JFileChooser();
 			final String defaultFileName = "new.md";
 
 			// add listener to filter changes
 			chooser.addPropertyChangeListener(JFileChooser.DIRECTORY_CHANGED_PROPERTY,
 			 	new PropertyChangeListener() {
 					    public void propertyChange(PropertyChangeEvent evt) {
 
 					    chooser.setSelectedFile(new File(chooser.getCurrentDirectory().getAbsolutePath() + "\\" + defaultFileName));
 					    chooser.updateUI();
 
 					   }
 					});
 		}
 		else
 		{
 			File file = new File(d.getFilepath());
 	        FileWriter fileWriter = null;
 			try {
 				fileWriter = new FileWriter(file);
			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 	        int textLength = d.getDocument().getLength();
 	        try {
 				fileWriter.write(d.getDocument().getText(0, textLength));
 				 fileWriter.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (BadLocationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	       
 		}
 		
 		/*
 			JFileChooser filechooser = new JFileChooser();
 			int stat = filechooser.showOpenDialog(guiView.getContentPane());
 			if (stat == JFileChooser.APPROVE_OPTION)
 			{
 				File file = new File();			
 			}
 		}
 		
 		*/
 		
 //		ActionListener listener = new ActionListener() {
 //            @Override
 //            public void actionPerformed(ActionEvent event) {
 //            	if (event.getSource() == openBtn)
 //            	{
 //            		JFileChooser filechooser = new JFileChooser();
 //            		JTextArea txtArea = new JTextArea();
 //            		int status = filechooser.showOpenDialog(guiView.getContentPane());
 //            		if(status == JFileChooser.APPROVE_OPTION){
 //            			Scanner fileScanner = null;
 //            			boolean noError = true;
 //            			
 //            			try{
 //            				fileScanner = new Scanner(filechooser.getSelectedFile());
 //            			} catch(FileNotFoundException e){
 //            				noError = false;
 //            				JOptionPane.showMessageDialog(getContentPane(), "File not found!");
 //            			}
 //            			
 //            			while(noError && fileScanner.hasNextLine()){
 //            				 txtArea.append(fileScanner.nextLine() + "\n");
 //            			}
 //            			textareas.add(txtArea); //perhaps here I should be modifying the "model" and thus causing the new textpane with
 //            		
 //            		}
 //            	}
 //            }
 //        };
 			/*
 			int returnVal = fc.showSaveDialog(FileChooserDemo.this);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 File file = fc.getSelectedFile();
                 //This is where a real application would save the file.
                 log.append("Saving: " + file.getName() + "." + newline);
             } else {
                 log.append("Save command cancelled by user." + newline);
             }
             
 			 * */
 		
 	}
 
 } //end of controller class
