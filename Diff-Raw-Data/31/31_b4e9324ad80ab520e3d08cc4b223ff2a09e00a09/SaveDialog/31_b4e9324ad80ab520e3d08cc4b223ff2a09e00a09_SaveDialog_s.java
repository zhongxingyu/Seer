 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import logic.Board;
 import logic.EditorBoard;
 
 /**
  * 
  * @author Eren, Fabian, Anatoli
  * @version 0.1
  *
  */
 
 public class SaveDialog extends JFrame {
 
 	private boolean cancel = false;
 	
     public SaveDialog() {
     	saveAs(null);
     }
 
     boolean saveAs(String pfad) {
    		JFileChooser chooser;
    		if (pfad == null){
    			pfad = System.getProperty("user.home");
    			pfad = pfad + "\\Sternenhimmel-Puzzle";
    			File saveDirectory = new File(pfad);
    			if(saveDirectory.isDirectory()){}
    			else{
    				saveDirectory.mkdir();
    			}
    		}
    		File file = new File(pfad.trim());
     		chooser = new JFileChooser(pfad);
    		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
 
    		FileNameExtensionFilter markUpFilter = new FileNameExtensionFilter(
                "xml", "xml");
    		chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter());
 
    		chooser.setFileFilter(markUpFilter);
    		chooser.setDialogTitle("Speichern unter...");
    		chooser.setVisible(true);
     		int result = chooser.showSaveDialog(this);
    		
    		if (result == JFileChooser.APPROVE_OPTION) 
    		{
    			pfad = chooser.getSelectedFile().toString();
    			if(pfad.matches(".*(.xml).*")){}
    			else{
    				pfad = pfad + ".xml";
    			}
    			file = new File(pfad);
    			if (markUpFilter.accept(file))
    			{
    				System.out.println(pfad + " kann gespeichert werden.");
    				//Board oBoard = PlayFrame._oPlayFrame._oBoard.getLogicBoard();
    				Board oBoard = PlayFrame._oPlayFrame._oBoard.getLogicBoard();
    				if (oBoard instanceof EditorBoard)
    				{
    					oBoard.save(pfad);
    				}
    				else
    				{
    					oBoard.save(pfad);
    				}
    				
    			}
    		}
   		else if (result == JFileChooser.CANCEL_OPTION)
   		{
                System.out.println("Doch nicht gespeichert");
                cancel = true;
                
                System.out.println(cancel);
                
                chooser.setVisible(false);
   		}
   		else
   		{
   		
             System.out.println(pfad + " ist der falsche Dateityp.");
            	chooser.setVisible(false);
            	return true;
        	}
    	chooser.setVisible(false);
    	return false;
     }
     
     public boolean isCancel() 
     {
     	return cancel;
 	}
 
 }
