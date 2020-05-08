 package de.unisiegen.gtitool.ui.logic;
 
 
 import java.util.ArrayList;
 import java.util.TreeSet;
 
 import javax.swing.JFrame;
 
 import de.unisiegen.gtitool.core.entities.Alphabet;
 import de.unisiegen.gtitool.core.entities.Symbol;
 import de.unisiegen.gtitool.core.entities.listener.AlphabetChangedListener;
 import de.unisiegen.gtitool.core.exceptions.alphabet.AlphabetException;
 import de.unisiegen.gtitool.core.machines.Machine;
 import de.unisiegen.gtitool.core.parser.exceptions.ParserException;
 import de.unisiegen.gtitool.core.parser.exceptions.ScannerException;
 import de.unisiegen.gtitool.ui.Messages;
 import de.unisiegen.gtitool.ui.netbeans.AlphabetDialogForm;
 
 
 /**
  * The logic class for the create new transition dialog.
  * 
  * @author Christian Fehler
  * @version $Id$
  */
 public final class AlphabetDialog
 {
 
   /**
    * The {@link AlphabetDialogForm}.
    */
   private AlphabetDialogForm gui;
 
 
   /**
   * The parent {@link JFrame}.
    */
  private JFrame parent;
 
 
   /**
   * The {@link Machine} of this dialog.
    */
  private Machine machine;
 
 
   /**
    * Create a new {@link AlphabetDialog}
    * 
    * @param parent The parent frame.
    * @param machine The {@link Machine} of this dialog.
    */
   public AlphabetDialog ( JFrame parent, Machine machine )
   {
     this.parent = parent;
     this.machine = machine;
     this.gui = new AlphabetDialogForm ( this, parent );
     this.gui.alphabetPanelForm.styledAlphabetParserPanelInput
         .setAlphabet ( this.machine.getAlphabet () );
     this.gui.alphabetPanelForm.styledAlphabetParserPanelInput
         .addAlphabetChangedListener ( new AlphabetChangedListener ()
         {
 
           @SuppressWarnings ( "synthetic-access" )
           public void alphabetChanged ( @SuppressWarnings ( "unused" )
           Alphabet newAlphabet )
           {
             setButtonStatus ();
           }
         } );
 
     this.gui.alphabetPanelForm.styledAlphabetParserPanelPushDown
         .setAlphabet ( this.machine.getPushDownAlphabet () );
     this.gui.alphabetPanelForm.styledAlphabetParserPanelPushDown
         .addAlphabetChangedListener ( new AlphabetChangedListener ()
         {
 
           @SuppressWarnings ( "synthetic-access" )
           public void alphabetChanged ( @SuppressWarnings ( "unused" )
           Alphabet newAlphabet )
           {
             setButtonStatus ();
           }
         } );
 
     this.gui.alphabetPanelForm.jCheckBoxPushDownAlphabet
         .setSelected ( this.machine.isUsePushDownAlphabet () );
   }
 
 
   /**
    * Returns the set of not removeable {@link Symbol}s.
    * 
    * @param newAlphabet The new {@link Alphabet}.
    * @return The set of not removeable {@link Symbol}s.
    */
   private final TreeSet < Symbol > getNotRemoveableSymbolsFromAlphabet (
       Alphabet newAlphabet )
   {
     if ( newAlphabet == null )
     {
       throw new IllegalArgumentException ( "new alphabet is null" ); //$NON-NLS-1$
     }
     TreeSet < Symbol > notRemoveableSymbols = new TreeSet < Symbol > ();
     TreeSet < Symbol > symbolsToRemove = new TreeSet < Symbol > ();
     for ( Symbol current : this.machine.getAlphabet () )
     {
       if ( !newAlphabet.contains ( current ) )
       {
         symbolsToRemove.add ( current );
       }
     }
     for ( Symbol current : symbolsToRemove )
     {
       if ( !this.machine.isSymbolRemoveableFromAlphabet ( current ) )
       {
         notRemoveableSymbols.add ( current );
       }
     }
     return notRemoveableSymbols;
   }
 
 
   /**
    * Returns the set of not removeable {@link Symbol}s.
    * 
    * @param newAlphabet The new {@link Alphabet}.
    * @return The set of not removeable {@link Symbol}s.
    */
   private final TreeSet < Symbol > getNotRemoveableSymbolsFromPushDownAlphabet (
       Alphabet newAlphabet )
   {
     if ( newAlphabet == null )
     {
       throw new IllegalArgumentException ( "new alphabet is null" ); //$NON-NLS-1$
     }
     TreeSet < Symbol > notRemoveableSymbols = new TreeSet < Symbol > ();
     TreeSet < Symbol > symbolsToRemove = new TreeSet < Symbol > ();
     for ( Symbol current : this.machine.getPushDownAlphabet () )
     {
       if ( !newAlphabet.contains ( current ) )
       {
         symbolsToRemove.add ( current );
       }
     }
     for ( Symbol current : symbolsToRemove )
     {
       if ( !this.machine.isSymbolRemoveableFromPushDownAlphabet ( current ) )
       {
         notRemoveableSymbols.add ( current );
       }
     }
     return notRemoveableSymbols;
   }
 
 
   /**
    * Handles cancel button pressed.
    */
   public final void handleCancel ()
   {
     this.gui.dispose ();
   }
 
 
   /**
    * Handle ok button pressed.
    */
   public final void handleOk ()
   {
     this.gui.setVisible ( false );
     performAlphabetChange ( this.machine.getAlphabet (),
         this.gui.alphabetPanelForm.styledAlphabetParserPanelInput
             .getAlphabet () );
     performAlphabetChange ( this.machine.getPushDownAlphabet (),
         this.gui.alphabetPanelForm.styledAlphabetParserPanelPushDown
             .getAlphabet () );
     this.machine
         .setUsePushDownAlphabet ( this.gui.alphabetPanelForm.jCheckBoxPushDownAlphabet
             .isSelected () );
     this.gui.dispose ();
   }
 
 
   /**
    * Preforms the {@link Alphabet} change.
    * 
    * @param oldAlphabet The old {@link Alphabet}.
    * @param newAlphabet The new {@link Alphabet}.
    */
   private final void performAlphabetChange ( Alphabet oldAlphabet,
       Alphabet newAlphabet )
   {
     TreeSet < Symbol > symbolsToAdd = new TreeSet < Symbol > ();
     TreeSet < Symbol > symbolsToRemove = new TreeSet < Symbol > ();
     for ( Symbol current : newAlphabet )
     {
       if ( !oldAlphabet.contains ( current ) )
       {
         symbolsToAdd.add ( current );
       }
     }
     for ( Symbol current : oldAlphabet )
     {
       if ( !newAlphabet.contains ( current ) )
       {
         symbolsToRemove.add ( current );
       }
     }
     try
     {
       oldAlphabet.add ( symbolsToAdd );
       oldAlphabet.remove ( symbolsToRemove );
     }
     catch ( AlphabetException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
     }
   }
 
 
   /**
    * Sets the status of the buttons.
    */
   private final void setButtonStatus ()
   {
     if ( ( this.gui.alphabetPanelForm.styledAlphabetParserPanelInput
         .getAlphabet () == null )
         || ( this.gui.alphabetPanelForm.styledAlphabetParserPanelPushDown
             .getAlphabet () == null ) )
     {
       this.gui.jGTIButtonOk.setEnabled ( false );
     }
     else
     {
       TreeSet < Symbol > notRemoveableSymbolsFromAlphabet = getNotRemoveableSymbolsFromAlphabet ( this.gui.alphabetPanelForm.styledAlphabetParserPanelInput
           .getAlphabet () );
       TreeSet < Symbol > notRemoveableSymbolsFromPushDownAlphabet = getNotRemoveableSymbolsFromPushDownAlphabet ( this.gui.alphabetPanelForm.styledAlphabetParserPanelPushDown
           .getAlphabet () );
       if ( notRemoveableSymbolsFromAlphabet.size () > 0 )
       {
         AlphabetDialog.this.gui.jGTIButtonOk.setEnabled ( false );
         ArrayList < ScannerException > exceptionList = new ArrayList < ScannerException > ();
         for ( Symbol current : notRemoveableSymbolsFromAlphabet )
         {
           exceptionList.add ( new ParserException ( 0, 0, Messages.getString (
               "AlphabetDialog.SymbolUsed", current ) ) ); //$NON-NLS-1$
         }
         AlphabetDialog.this.gui.alphabetPanelForm.styledAlphabetParserPanelInput
             .setException ( exceptionList );
       }
      else if ( notRemoveableSymbolsFromPushDownAlphabet.size () > 0 )
       {
         AlphabetDialog.this.gui.jGTIButtonOk.setEnabled ( false );
         ArrayList < ScannerException > exceptionList = new ArrayList < ScannerException > ();
         for ( Symbol current : notRemoveableSymbolsFromPushDownAlphabet )
         {
           exceptionList.add ( new ParserException ( 0, 0, Messages.getString (
               "AlphabetDialog.SymbolUsed", current ) ) ); //$NON-NLS-1$
         }
         AlphabetDialog.this.gui.alphabetPanelForm.styledAlphabetParserPanelPushDown
             .setException ( exceptionList );
       }
      else
       {
         this.gui.jGTIButtonOk.setEnabled ( true );
       }
     }
   }
 
 
   /**
    * Show the dialog for creating a new transition
    */
   public final void show ()
   {
     int x = this.parent.getBounds ().x + ( this.parent.getWidth () / 2 )
         - ( this.gui.getWidth () / 2 );
     int y = this.parent.getBounds ().y + ( this.parent.getHeight () / 2 )
         - ( this.gui.getHeight () / 2 );
     this.gui.setBounds ( x, y, this.gui.getWidth (), this.gui.getHeight () );
     this.gui.setVisible ( true );
   }
 }
