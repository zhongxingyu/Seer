 package de.unisiegen.tpml.ui ;
 
 
 import java.awt.Color ;
 import java.awt.Frame ;
 import java.awt.event.KeyEvent ;
 import java.awt.event.KeyListener ;
 import java.io.File ;
 import java.util.ResourceBundle ;
 import java.util.prefs.Preferences ;
 import javax.swing.JComponent ;
 import javax.swing.JDialog ;
 import javax.swing.JFileChooser ;
 import javax.swing.JFrame ;
 import javax.swing.JOptionPane ;
 import javax.swing.JPanel ;
 import javax.swing.JProgressBar ;
 import javax.swing.JTextArea ;
 import javax.swing.filechooser.FileFilter ;
 import javax.swing.text.Utilities ;
 import de.unisiegen.tpml.core.ProofModel ;
 import de.unisiegen.tpml.core.latex.LatexException ;
 import de.unisiegen.tpml.core.latex.LatexExport ;
 import de.unisiegen.tpml.core.latex.LatexPrintable ;
 import de.unisiegen.tpml.graphics.outline.Outline ;
 import de.unisiegen.tpml.ui.netbeans.TexDialog ;
 
 
 /**
  * this class provides the GUI for the latex-export. it is used for the
  * proofviews and the tpml.tex
  * 
  * @author michael
  */
 public class GeneralLaTex
 {
   /**
    * the latex manager
    */
   private LatexPrintable laTexPrintable ;
 
 
   /**
    * the parent of the shown dialogs
    */
   private JComponent parent ;
 
 
   /**
    * the parent of the shown dialogs
    */
   private Frame parentFrame ;
 
 
   /**
    * TODO vielleicht gibt es irgendwann eien Statusdialog
    */
   private JDialog status ;
 
 
   /**
    * TODO Textarea des Stustusses
    */
   private JTextArea text ;
 
 
   /**
    * TODO Prograssbar, alles nur Test im Moment
    */
   private JProgressBar progress ;
 
 
   /**
    * The {@link TexDialog}.
    */
   protected TexDialog dialog ;
 
 
   /**
    * the default constructor
    * 
    * @param pParent - the parent frame
    */
   public GeneralLaTex ( Frame pParent )
   {
     this.parentFrame = pParent ;
   }
 
 
   /**
    * the constructor used exporting files
    * 
    * @param pLaTexPrintable
    * @param pParent
    */
   public GeneralLaTex ( LatexPrintable pLaTexPrintable , JPanel pParent )
   {
     this.laTexPrintable = pLaTexPrintable ;
     this.parent = pParent ;
   }
 
 
   /**
    * this method shows the file save dialog. If needed the optins for
    * overlapping and the pagecount are enabled. The File will be automatical be
    * a tex-file. After getting the needed informations the texexport is called.
    */
   public void export ( )
   {
     this.dialog = new TexDialog (
         ( JFrame ) this.parent.getTopLevelAncestor ( ) , true ) ;
    Preferences preferences = Preferences.userNodeForPackage ( Outline.class ) ;
     this.dialog.filechooser.setCurrentDirectory ( new File ( preferences.get (
         "lastDir" , "." ) ) ) ; //$NON-NLS-1$//$NON-NLS-2$
     this.dialog.filechooser.setFileFilter ( new FileFilter ( )
     {
       @ Override
       public boolean accept ( File pFile )
       {
         return ( pFile.isDirectory ( ) )
             || ( pFile.getName ( ).toLowerCase ( ).endsWith ( ".tex" ) ) ; //$NON-NLS-1$
       }
 
 
       @ Override
       public String getDescription ( )
       {
         return ResourceBundle.getBundle ( "de/unisiegen/tpml/ui/ui" ) //$NON-NLS-1$
             .getString ( "Latex.FileFilter" ) ; //$NON-NLS-1$
       }
     } ) ;
     this.dialog.overlappingLabel.setText ( ResourceBundle.getBundle (
         "de/unisiegen/tpml/ui/ui" ).getString ( "Latex.Overlapping" ) //$NON-NLS-1$//$NON-NLS-2$
         + ":" ) ; //$NON-NLS-1$
     this.dialog.pageCountLabel.setText ( ResourceBundle.getBundle (
         "de/unisiegen/tpml/ui/ui" ).getString ( "Latex.PageCount" ) //$NON-NLS-1$ //$NON-NLS-2$
         + ":" ) ; //$NON-NLS-1$
     this.dialog.allCheckBox.setText ( ResourceBundle.getBundle (
         "de/unisiegen/tpml/ui/ui" ).getString ( "Latex.OneFile" ) ) ; //$NON-NLS-1$//$NON-NLS-2$
     this.dialog.allCheckBox.setToolTipText ( ResourceBundle.getBundle (
         "de/unisiegen/tpml/ui/ui" ).getString ( "Latex.OneFileToolTip" ) ) ; //$NON-NLS-1$ //$NON-NLS-2$
     this.dialog.pageCountTextField.addKeyListener ( new KeyListener ( )
     {
       public void keyPressed ( @ SuppressWarnings ( "unused" )
       KeyEvent pKeyEvent )
       {
         // Do nothing
       }
 
 
       public void keyReleased ( @ SuppressWarnings ( "unused" )
       KeyEvent pKeyEvent )
       {
         try
         {
           int count = Integer.valueOf (
               GeneralLaTex.this.dialog.pageCountTextField.getText ( ) )
               .intValue ( ) ;
           if ( ( count <= 0 ) || ( count > 13 ) )
           {
             GeneralLaTex.this.dialog.pageCountTextField
                 .setBackground ( Color.RED ) ;
           }
           else
           {
             GeneralLaTex.this.dialog.pageCountTextField
                 .setBackground ( Color.WHITE ) ;
           }
         }
         catch ( NumberFormatException e )
         {
           GeneralLaTex.this.dialog.pageCountTextField
               .setBackground ( Color.RED ) ;
         }
       }
 
 
       public void keyTyped ( KeyEvent pKeyEvent )
       {
         int key = pKeyEvent.getKeyChar ( ) ;
         if ( ( key != 48 ) && ( key != 49 ) && ( key != 50 ) && ( key != 51 )
             && ( key != 52 ) && ( key != 53 ) && ( key != 54 ) && ( key != 55 )
             && ( key != 56 ) && ( key != 57 ) && ( key != 8 ) && ( key != 127 ) )
         {
           pKeyEvent.setKeyChar ( '\u0000' ) ;
         }
       }
     } ) ;
     this.dialog.overlappingTextField.addKeyListener ( new KeyListener ( )
     {
       public void keyPressed ( @ SuppressWarnings ( "unused" )
       KeyEvent pKeyEvent )
       {
         // Do nothing
       }
 
 
       public void keyReleased ( @ SuppressWarnings ( "unused" )
       KeyEvent pKeyEvent )
       {
         try
         {
           int count = Integer.valueOf (
               GeneralLaTex.this.dialog.overlappingTextField.getText ( ) )
               .intValue ( ) ;
           if ( ( count < 0 ) || ( count > 50 ) )
           {
             GeneralLaTex.this.dialog.overlappingTextField
                 .setBackground ( Color.RED ) ;
           }
           else
           {
             GeneralLaTex.this.dialog.overlappingTextField
                 .setBackground ( Color.WHITE ) ;
           }
         }
         catch ( NumberFormatException e )
         {
           GeneralLaTex.this.dialog.overlappingTextField
               .setBackground ( Color.RED ) ;
         }
       }
 
 
       public void keyTyped ( KeyEvent pKeyEvent )
       {
         int key = pKeyEvent.getKeyChar ( ) ;
         if ( ( key != 48 ) && ( key != 49 ) && ( key != 50 ) && ( key != 51 )
             && ( key != 52 ) && ( key != 53 ) && ( key != 54 ) && ( key != 55 )
             && ( key != 56 ) && ( key != 57 ) && ( key != 8 ) && ( key != 127 ) )
         {
           pKeyEvent.setKeyChar ( '\u0000' ) ;
         }
       }
     } ) ;
     this.dialog.setLocationRelativeTo ( this.parent ) ;
     // let us now if weneed the overlap or not
     boolean needed = false ;
     if ( this.laTexPrintable instanceof ProofModel )
     {
       try
       {
         ( ( ProofModel ) this.laTexPrintable ).setOverlap ( 0 ) ;
         needed = true ;
       }
       catch ( UnsupportedOperationException e )
       {
         // Do nothing
       }
     }
     if ( ! needed )
     {
       this.dialog.overlappingLabel.setVisible ( false ) ;
       this.dialog.overlappingTextField.setVisible ( false ) ;
       this.dialog.overlappingEntity.setVisible ( false ) ;
       this.dialog.pageCountLabel.setVisible ( false ) ;
       this.dialog.pageCountTextField.setVisible ( false ) ;
     }
     this.dialog.setVisible ( true ) ;
     if ( this.dialog.cancelled )
     {
       return ;
     }
     File file = this.dialog.filechooser.getSelectedFile ( ) ;
     // get the overlapping
     int overlapping = this.dialog.overlappingInt ;
     // get the pagecount
     int pageCount = this.dialog.pagecount ;
     // get the information if the TPML.TEX shold be included or not
     boolean all = this.dialog.all ;
     // File file = showFileDialog();
     if ( file != null )
     {
       preferences.put ( "lastDir" , file.getAbsolutePath ( ) ) ; //$NON-NLS-1$
       // fix the filename if the user has not entered a filename ending with
       // .tex
       String filename = file.getAbsolutePath ( ) ;
       if ( ! filename.substring ( filename.length ( ) - 4 ).equalsIgnoreCase (
           ".tex" ) ) //$NON-NLS-1$
       {
         filename = filename + ".tex" ; //$NON-NLS-1$
         file = new File ( filename ) ;
       }
       // LatexTest.exportLatexPrintable((SmallStepProofModel)this.ourProofView.getModel(),
       // file);
       try
       {
         // this.status = new JDialog ();
         // this.status.setTitle("Stautus");
         // this.status.setModal(false);
         //				
         // this.text = new JTextArea ( "LaTex-Export will be done. Pleas
         // wait.");
         // this.status.add(this.text);
         // this.status.setSize(150, 100);
         //				
         // // Größe des Bildschirms ermitteln
         // Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         //
         // // Position des JFrames errechnen
         // int top = (screenSize.height - this.status.getHeight()) / 2;
         // int left = (screenSize.width - this.status.getWidth()) / 2;
         //
         // // Position zuordnen
         // this.status.setLocation(left, top);
         //				
         //				
         // this.status.setVisible(true);
         if ( this.laTexPrintable instanceof ProofModel )
         {
           try
           {
             ( ( ProofModel ) this.laTexPrintable ).setOverlap ( overlapping ) ;
             ( ( ProofModel ) this.laTexPrintable ).setPages ( pageCount ) ;
           }
           catch ( UnsupportedOperationException e )
           {
             // nothing to to
           }
         }
         if ( file.exists ( ) )
         {
           if ( JOptionPane.showConfirmDialog ( this.parent , ResourceBundle
               .getBundle ( "de/unisiegen/tpml/ui/ui" ).getString ( //$NON-NLS-1$
                   "Latex.OverrideFile" ) //$NON-NLS-1$
               + "?" , ResourceBundle.getBundle ( "de/unisiegen/tpml/ui/ui" ) //$NON-NLS-1$ //$NON-NLS-2$
               .getString ( "Latex.Override" ) //$NON-NLS-1$
               + "?" , JOptionPane.YES_NO_OPTION ) == JOptionPane.NO_OPTION ) //$NON-NLS-1$
           {
             export ( ) ;
             return ;
           }
         }
         LatexExport.export ( this.laTexPrintable , file , all ) ;
         // this.status.dispose();
         JOptionPane.showMessageDialog ( this.parent , ResourceBundle.getBundle (
             "de/unisiegen/tpml/ui/ui" ).getString ( "Latex.Done" ) ) ; //$NON-NLS-1$//$NON-NLS-2$
       }
       catch ( LatexException e )
       {
         JOptionPane.showMessageDialog ( this.parent , e.toString ( ) ,
             ResourceBundle.getBundle ( "de/unisiegen/tpml/ui/ui" ).getString ( //$NON-NLS-1$
                 "Latex.Error" ) , JOptionPane.ERROR_MESSAGE ) ; //$NON-NLS-1$
       }
     }
   }
 
 
   // public File showFileDialog ()
   // {
   // JFileChooser fc = new JFileChooser();
   // fc.setMultiSelectionEnabled(false);
   // fc.setDialogType(JFileChooser.SAVE_DIALOG);
   // fc.setDialogTitle("LaTex - Export");
   // fc.setDragEnabled(false);
   // fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
   // fc.setFileFilter ( new FileFilter ( )
   // {
   // @Override
   // public boolean accept ( File f )
   // {
   // return f.getName ( ).toLowerCase ( ).endsWith ( ".tex" ) //$NON-NLS-1$
   // || f.isDirectory ( );
   // }
   //
   // @Override
   // public String getDescription ( )
   // {
   // return "TEX-Files (*.tex)"; //$NON-NLS-1$
   // }
   // } );
   // fc.showDialog(this.parent, "Export");
   // return fc.getSelectedFile();
   // }
   /**
    * exports the tpml.tex to an choosen dir
    */
   public void exportTPML ( )
   {
     JFileChooser fc = new JFileChooser ( ) ;
     fc.setMultiSelectionEnabled ( false ) ;
     fc.setDialogType ( JFileChooser.SAVE_DIALOG ) ;
     fc.setDialogTitle ( ResourceBundle.getBundle ( "de/unisiegen/tpml/ui/ui" ) //$NON-NLS-1$
         .getString ( "Latex.Title" ) ) ; //$NON-NLS-1$
     fc.setDragEnabled ( false ) ;
     fc.setFileSelectionMode ( JFileChooser.DIRECTORIES_ONLY ) ;
     Preferences preferences = Preferences.userNodeForPackage ( Outline.class ) ;
     fc.setCurrentDirectory ( new File ( preferences.get ( "lastDir" , "." ) ) ) ; //$NON-NLS-1$//$NON-NLS-2$
     fc.showDialog ( this.parentFrame , ResourceBundle.getBundle (
         "de/unisiegen/tpml/ui/ui" ).getString ( "Latex.Export" ) ) ; //$NON-NLS-1$//$NON-NLS-2$
     if ( fc.getSelectedFile ( ) != null )
     {
       try
       {
         preferences
             .put ( "lastDir" , fc.getSelectedFile ( ).getAbsolutePath ( ) ) ; //$NON-NLS-1$
         File tpmlFile = new File ( fc.getSelectedFile ( ).getAbsolutePath ( )
             + "/tpml.tex" ) ; //$NON-NLS-1$
         if ( tpmlFile.exists ( ) )
         {
           if ( JOptionPane.showConfirmDialog ( this.parent , ResourceBundle
               .getBundle ( "de/unisiegen/tpml/ui/ui" ).getString ( //$NON-NLS-1$
                   "Latex.OverrideFile" ) //$NON-NLS-1$
               + "?" , ResourceBundle.getBundle ( "de/unisiegen/tpml/ui/ui" ) //$NON-NLS-1$ //$NON-NLS-2$
               .getString ( "Latex.Override" ) //$NON-NLS-1$
               + "?" , JOptionPane.YES_NO_OPTION ) == JOptionPane.NO_OPTION ) //$NON-NLS-1$
           {
             exportTPML ( ) ;
             return ;
           }
         }
         LatexExport.exportTPML ( fc.getSelectedFile ( ) ) ;
         JOptionPane.showMessageDialog ( this.parent , ResourceBundle.getBundle (
             "de/unisiegen/tpml/ui/ui" ).getString ( "Latex.Done" ) ) ; //$NON-NLS-1$//$NON-NLS-2$
       }
       catch ( LatexException e )
       {
         JOptionPane.showMessageDialog ( this.parent , e.toString ( ) ,
             ResourceBundle.getBundle ( "de/unisiegen/tpml/ui/ui" ).getString ( //$NON-NLS-1$
                 "Latex.Error" ) , JOptionPane.ERROR_MESSAGE ) ; //$NON-NLS-1$
       }
     }
   }
 }
