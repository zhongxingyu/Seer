 package de.unisiegen.gtitool.ui.logic;
 
 
 import java.awt.event.ItemEvent;
 
 import de.unisiegen.gtitool.ui.netbeans.NewDialogForm;
 import de.unisiegen.gtitool.ui.netbeans.NewDialogGrammarChoiceForm;
 
 
 /**
  * The {@link NewDialogChoice}.
  * 
  * @author Benjamin Mies
  * @version $Id$
  */
 public final class NewDialogGrammarChoice
 {
 
   /**
    * Signals the user choice
    */
   public enum Choice
   {
     /**
      * Contextfree is choosen.
      */
     CONTEXT_FREE,
 
     /**
      * Regular is choosen.
      */
     REGULAR;
   }
 
 
   /**
    * The {@link NewDialogForm}.
    */
   private NewDialogGrammarChoiceForm gui;
 
 
   /**
    * The actual user choice
    */
  private Choice actualChoice = Choice.CONTEXT_FREE;
 
 
   /** The parent Dialog containing this panel */
   private NewDialog parent;
 
 
   /**
    * Creates a new {@link NewDialogChoice}.
    * 
    * @param parent The Dialog containing this panel
    */
   public NewDialogGrammarChoice ( NewDialog parent )
   {
     this.parent = parent;
     this.gui = new NewDialogGrammarChoiceForm ();
     this.gui.setLogic ( this );
   }
 
 
   /**
    * Getter for the gui of this logic class.
    * 
    * @return The {@link NewDialogGrammarChoiceForm}.
    */
   public final NewDialogGrammarChoiceForm getGui ()
   {
     return this.gui;
   }
 
 
   /**
    * Returns the user choice.
    * 
    * @return The user choice of this panel.
    */
   public final Choice getUserChoice ()
   {
     return this.actualChoice;
   }
 
 
   /**
    * Handle the cancel button pressed event.
    */
   public final void handleCancel ()
   {
     this.parent.getGui ().dispose ();
   }
 
 
   /**
    * Handle Contextfree Item State changed.
    * 
    * @param evt The {@link ItemEvent}.
    */
   public final void handleContextFreeItemStateChanged ( ItemEvent evt )
   {
     if ( evt.getStateChange () == ItemEvent.SELECTED )
     {
       this.actualChoice = Choice.CONTEXT_FREE;
     }
   }
 
 
   /**
    * Handle the next button pressed event.
    */
   public final void handleNextGrammarChoice ()
   {
     this.parent.handleNextGrammarChoice ();
   }
 
 
   /**
    * Handle the previous button pressed event.
    */
   public final void handlePreviousGrammarChoice ()
   {
     this.parent.handlePreviousGrammarChoice ();
   }
 
 
   /**
    * Handle Regular Item State changed.
    * 
    * @param evt The {@link ItemEvent}.
    */
   public final void handleRegularGrammarItemStateChanged ( ItemEvent evt )
   {
     if ( evt.getStateChange () == ItemEvent.SELECTED )
     {
       this.actualChoice = Choice.REGULAR;
     }
   }
 }
