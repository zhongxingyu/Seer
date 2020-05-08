 package org.adjudju.gui.actions;
 
 import org.adjudju.gui.panels.MoveListPanel;
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
            e1.printStackTrace();
         } catch (InvalidPGNFileException e1) {
            e1.printStackTrace();
         }
     }
 }
