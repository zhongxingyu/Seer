 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.watergis.gui.components;
 
 import java.io.File;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileSystemView;
 
 /**
  * DOCUMENT ME!
  *
  * @author   Gilles Baatz
  * @version  $Revision$, $Date$
  */
 public class ConfirmationJFileChooser extends JFileChooser {
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ConfirmationJFileChooser object.
      */
     public ConfirmationJFileChooser() {
     }
 
     /**
      * Creates a new ConfirmationJFileChooser object.
      *
      * @param  currentDirectoryPath  DOCUMENT ME!
      */
     public ConfirmationJFileChooser(final String currentDirectoryPath) {
         super(currentDirectoryPath);
     }
 
     /**
      * Creates a new ConfirmationJFileChooser object.
      *
      * @param  currentDirectory  DOCUMENT ME!
      */
     public ConfirmationJFileChooser(final File currentDirectory) {
         super(currentDirectory);
     }
 
     /**
      * Creates a new ConfirmationJFileChooser object.
      *
      * @param  fsv  DOCUMENT ME!
      */
     public ConfirmationJFileChooser(final FileSystemView fsv) {
         super(fsv);
     }
 
     /**
      * Creates a new ConfirmationJFileChooser object.
      *
      * @param  currentDirectory  DOCUMENT ME!
      * @param  fsv               DOCUMENT ME!
      */
     public ConfirmationJFileChooser(final File currentDirectory, final FileSystemView fsv) {
         super(currentDirectory, fsv);
     }
 
     /**
      * Creates a new ConfirmationJFileChooser object.
      *
      * @param  currentDirectoryPath  DOCUMENT ME!
      * @param  fsv                   DOCUMENT ME!
      */
     public ConfirmationJFileChooser(final String currentDirectoryPath, final FileSystemView fsv) {
         super(currentDirectoryPath, fsv);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void approveSelection() {
         final File f = getSelectedFile();
         if (f.exists() && (getDialogType() == SAVE_DIALOG)) {
             final String message = org.openide.util.NbBundle.getMessage(
                     ConfirmationJFileChooser.class,
                     "ConfirmationJFileChooser.approveSelection.message");
             final String title = org.openide.util.NbBundle.getMessage(
                     ConfirmationJFileChooser.class,
                     "ConfirmationJFileChooser.approveSelection.title");
 
             final int result = JOptionPane.showConfirmDialog(
                     this,
                     message,
                     title,
                     JOptionPane.YES_NO_CANCEL_OPTION);
             switch (result) {
                 case JOptionPane.YES_OPTION: {
                     super.approveSelection();
                     return;
                 }
                 case JOptionPane.NO_OPTION: {
                     return;
                 }
                 case JOptionPane.CLOSED_OPTION: {
                     return;
                 }
                 case JOptionPane.CANCEL_OPTION: {
                     cancelSelection();
                     return;
                 }
             }
         }
     }
 }
