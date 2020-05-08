 package de.unisiegen.gtitool.ui.popup;
 
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 import de.unisiegen.gtitool.core.entities.State;
 import de.unisiegen.gtitool.ui.Messages;
 import de.unisiegen.gtitool.ui.logic.MachinePanel;
 import de.unisiegen.gtitool.ui.logic.MainWindow;
 import de.unisiegen.gtitool.ui.netbeans.MainWindowForm;
 
 
 /**
  * A Popup Menu for {@link State}s
  * 
  * @author Benjamin Mies
  * @version $Id$
  */
 public final class EnterWordModePopupMenu extends JPopupMenu
 {
 
   /**
    * The serial version uid.
    */
   private static final long serialVersionUID = 706578962258712542L;
 
 
   /**
    * The {@link MachinePanel}
    */
   MachinePanel machinePanel;
 
 
   /**
    * The start item
    */
   private JMenuItem jMenuItemStart;
 
 
   /**
    * The previous step item
    */
   private JMenuItem jMenuItemPreviousStep;
 
 
   /**
    * The next step item
    */
   private JMenuItem jMenuItemNextStep;
 
 
   /**
    * The rename checkbox item
    */
   private JMenuItem jMenuItemStop;
 
 
   /**
    * The {@link MainWindowForm}
    */
   private MainWindowForm mainWindow;
 
 
   /**
    * Allocates a new {@link StatePopupMenu}.
    * 
    * @param machinePanel The {@link MachinePanel}
    * @param mainWindow The {@link MainWindow}
    */
   public EnterWordModePopupMenu ( MachinePanel machinePanel,
       MainWindowForm mainWindow )
   {
     this.machinePanel = machinePanel;
     this.mainWindow = mainWindow;
     populateMenues ();
   }
 
 
   /**
    * Populates the menues of this popup menu.
    */
   private final void populateMenues ()
   {
 
     this.jMenuItemStart = new JMenuItem ( Messages
         .getString ( "MachinePanel.WordModeStart" ) ); //$NON-NLS-1$
     this.jMenuItemStart
         .setIcon ( new ImageIcon (
             getClass ()
                 .getResource (
                    "/de/unisiegen/gtitool/ui/icon/popupMenu/wordnavigation/start.png" ) ) ); //$NON-NLS-1$
     this.jMenuItemStart.addActionListener ( new ActionListener ()
     {
 
       @SuppressWarnings ( "synthetic-access" )
       public void actionPerformed ( @SuppressWarnings ( "unused" )
       ActionEvent event )
       {
         if ( EnterWordModePopupMenu.this.machinePanel.handleWordStart () )
         {
           EnterWordModePopupMenu.this.mainWindow.jGTIToolBarButtonStart
               .setEnabled ( false );
           EnterWordModePopupMenu.this.mainWindow.jGTIToolBarButtonNextStep
               .setEnabled ( true );
           EnterWordModePopupMenu.this.mainWindow.jGTIToolBarButtonPrevious
               .setEnabled ( true );
           EnterWordModePopupMenu.this.mainWindow.jGTIToolBarToggleButtonAutoStep
               .setEnabled ( true );
           EnterWordModePopupMenu.this.mainWindow.jGTIToolBarButtonStop
               .setEnabled ( true );
         }
       }
     } );
     this.jMenuItemStart.setEnabled ( !this.machinePanel.isWordNavigation () );
     add ( this.jMenuItemStart );
 
     this.jMenuItemPreviousStep = new JMenuItem ( Messages
         .getString ( "MachinePanel.WordModePreviousStep" ) ); //$NON-NLS-1$
     this.jMenuItemPreviousStep
         .setIcon ( new ImageIcon (
             getClass ()
                 .getResource (
                    "/de/unisiegen/gtitool/ui/icon/popupMenu/wordnavigation/backward.png" ) ) ); //$NON-NLS-1$
     this.jMenuItemPreviousStep.addActionListener ( new ActionListener ()
     {
 
       @SuppressWarnings ( "synthetic-access" )
       public void actionPerformed ( @SuppressWarnings ( "unused" )
       ActionEvent event )
       {
         EnterWordModePopupMenu.this.machinePanel.handleWordPreviousStep ();
       }
     } );
     this.jMenuItemPreviousStep.setEnabled ( this.machinePanel
         .isWordNavigation () );
     add ( this.jMenuItemPreviousStep );
 
     this.jMenuItemNextStep = new JMenuItem ( Messages
         .getString ( "MachinePanel.WordModeNextStep" ) ); //$NON-NLS-1$
     this.jMenuItemNextStep.setIcon ( new ImageIcon ( getClass ().getResource (
        "/de/unisiegen/gtitool/ui/icon/popupMenu/wordnavigation/forward.png" ) ) ); //$NON-NLS-1$
     this.jMenuItemNextStep.addActionListener ( new ActionListener ()
     {
 
       @SuppressWarnings ( "synthetic-access" )
       public void actionPerformed ( @SuppressWarnings ( "unused" )
       ActionEvent event )
       {
         EnterWordModePopupMenu.this.machinePanel.handleWordNextStep ();
       }
     } );
     this.jMenuItemNextStep.setEnabled ( this.machinePanel.isWordNavigation () );
     add ( this.jMenuItemNextStep );
 
     this.jMenuItemStop = new JMenuItem ( Messages
         .getString ( "MachinePanel.WordModeStop" ) ); //$NON-NLS-1$
     this.jMenuItemStop.setIcon ( new ImageIcon ( getClass ().getResource (
        "/de/unisiegen/gtitool/ui/icon/popupMenu/wordnavigation/stop.png" ) ) ); //$NON-NLS-1$
     this.jMenuItemStop.addActionListener ( new ActionListener ()
     {
 
       @SuppressWarnings ( "synthetic-access" )
       public void actionPerformed ( @SuppressWarnings ( "unused" )
       ActionEvent event )
       {
         EnterWordModePopupMenu.this.mainWindow.jGTIToolBarButtonStart
             .setEnabled ( true );
         EnterWordModePopupMenu.this.mainWindow.jGTIToolBarButtonNextStep
             .setEnabled ( false );
         EnterWordModePopupMenu.this.mainWindow.jGTIToolBarButtonPrevious
             .setEnabled ( false );
         EnterWordModePopupMenu.this.mainWindow.jGTIToolBarToggleButtonAutoStep
             .setEnabled ( false );
         EnterWordModePopupMenu.this.mainWindow.jGTIToolBarButtonStop
             .setEnabled ( false );
         EnterWordModePopupMenu.this.machinePanel.handleWordStop ();
       }
     } );
     this.jMenuItemStop.setEnabled ( this.machinePanel.isWordNavigation () );
     add ( this.jMenuItemStop );
   }
 }
