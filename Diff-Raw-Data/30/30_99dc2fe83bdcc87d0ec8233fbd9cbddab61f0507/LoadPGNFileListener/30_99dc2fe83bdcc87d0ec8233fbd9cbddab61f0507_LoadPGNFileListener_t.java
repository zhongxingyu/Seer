 package org.adjudju.gui.actions;
 
 import org.adjudju.gui.panels.MoveListPanel;
import org.adjudju.utils.ResourceBundles;
import org.adjudju.utils.Utils;
 import org.plutonium239.pgnparser.InvalidPGNFileException;
 import org.plutonium239.pgnparser.MovePair;
 import org.plutonium239.pgnparser.PGNParser;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
import java.util.ResourceBundle;
 
 /**
  * LoadPGNFileListener
  * Author: Alexander M
  */
 public class LoadPGNFileListener implements ActionListener {
     private Component parent;
     private MoveListPanel moveListPanel;
 
     public LoadPGNFileListener(Component parent, MoveListPanel moveListPanel) {
         this.parent = parent;
         this.moveListPanel = moveListPanel;
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         JFileChooser fileChooser = new JFileChooser("/home/alex/");
         fileChooser.setFileFilter(new FileNameExtensionFilter(".pgn files", "pgn"));
 
         int retVal = fileChooser.showOpenDialog(parent);
 
         switch (retVal) {
             case JFileChooser.CANCEL_OPTION:
                 return;
             case JFileChooser.ERROR_OPTION:
                 return;
             default:
                 break;
         }
 
         try {
             ArrayList<MovePair> moveList = PGNParser.buildGame(fileChooser.getSelectedFile().getPath()).getMovePairList();
             moveListPanel.setMoves(moveList);
         } catch (IOException e1) {

            ResourceBundle bundle = ResourceBundle.getBundle(Utils.BUNDLE_PACKAGE + ResourceBundles.ERRORS_BUNDLE.getBundleName());
            String title = bundle.getString("LoadPGNDialog.Title");
            String text = bundle.getString("LoadPGNDialog.Text");

            JOptionPane.showMessageDialog(parent, text, title, JOptionPane.ERROR_MESSAGE);

         } catch (InvalidPGNFileException e1) {

            ResourceBundle bundle = ResourceBundle.getBundle(Utils.BUNDLE_PACKAGE + ResourceBundles.ERRORS_BUNDLE.getBundleName());
            String title = bundle.getString("LoadPGNDialog.Title");
            String text = bundle.getString("LoadPGNDialog.Text");

            JOptionPane.showMessageDialog(parent, text, title, JOptionPane.ERROR_MESSAGE);

        } catch (Exception e1) {

            ResourceBundle bundle = ResourceBundle.getBundle(Utils.BUNDLE_PACKAGE + ResourceBundles.ERRORS_BUNDLE.getBundleName());
            String title = bundle.getString("LoadPGNDialog.Title");
            String text = bundle.getString("LoadPGNDialog.Text");

            JOptionPane.showMessageDialog(parent, text, title, JOptionPane.ERROR_MESSAGE);

         }
     }
 }
