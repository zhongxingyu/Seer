 //Filename: JSliderDelegate.java
 //Revision: $Revision: 1.0 $
 //Rev Date: $Date: 2008/06/05 $
 
 package dgu.util.swing.binding.delegate;
 
 //<editor-fold defaultstate="collapsed" desc="         Imports         ">
 
 import dgu.util.swing.binding.Binding;
 import dgu.util.translator.TypeTranslator;
 import javax.swing.JSlider;
 
 //</editor-fold>
 import javax.swing.event.ChangeEvent;
 
 
 /**
  * A delegate for a JSlider which is bound to a value of type FROM which can be represented as a Integer.
  *
  * @author David Underhill
  */
 public class JSliderDelegate<FROM> extends GenericJComponentDelegate<FROM, Integer> implements BoundDelegateComponent { 
     
     //<editor-fold defaultstate="collapsed" desc="      Constructors       ">
     
     /** 
      * Instantiates an unbound JSlider Delegate 
      *
      * @param  owner   the BoundComponent which this belongs to
      * @param  compon  the component that this works for; must be a JSlider
      */
     public JSliderDelegate( BoundComponent owner, BoundComponent compon ) {
         super( owner, compon );
     }
     
     /** 
      * Instantiates a bound JSlider Delegate
      *
      * @param  owner       the BoundComponent which this belongs to
      * @param  compon      the component that this works for; must be a JSlider
      * @param  translator  how to translate between the value in the boundItem (type FROM) a another value of type TO
      * @param  varName     the name of the variable; assumes the getter and setter name will
      *                     be get and set followed by varName with its first letter capitalized 
      *                     (ex: if varName is "value" then the getter will be getValue).
      */
     public JSliderDelegate( BoundComponent owner, BoundComponent compon, 
                                   TypeTranslator<FROM, Integer> translator, String varName ) {
         super( false, owner, compon, translator, varName );
         completeInit();
     }
     
     /** 
      * Instantiates a bound JSlider Delegate
      *
      * @param  owner       the BoundComponent which this belongs to
      * @param  compon      the component that this works for; must be a JSlider
      * @param  translator  how to translate between the value in the boundItem (type FROM) a another value of type TO
      * @param  boundItem   the object this is bound to (contains the value to be modified as specified by varName)
      * @param  varName     the name of the variable; assumes the getter and setter name will
      *                     be get and set followed by varName with its first letter capitalized 
      *                     (ex: if varName is "value" then the getter will be getValue).
      */
     public JSliderDelegate( BoundComponent owner, BoundComponent compon, 
                                   TypeTranslator<FROM, Integer> translator, Object boundItem, String varName ) {
         super( false, owner, compon, translator, boundItem, varName );
         completeInit();
     }
     
     /** 
      * Instantiates a bound JSlider Delegate
      *
      * @param  owner       the BoundComponent which this belongs to
      * @param  compon      the component that this works for; must be a JSlider
      * @param  translator  how to translate between the value in the boundItem (type FROM) a another value of type TO
      * @param  boundItem   the object this is bound to (contains the value to be modified as specified by the getter and setter name)
      * @param  getterName  how to get the bound value from the bound object
      * @param  setterName  how to set the bound value from the bound object
      */
     public JSliderDelegate( BoundComponent owner, BoundComponent compon, 
                                   TypeTranslator<FROM, Integer> translator, Object boundItem, String getterName, String setterName ) {
         super( false, owner, compon, translator, boundItem, getterName, setterName );
         completeInit();
     }
     
     /** 
      * Instantiates a bound JSlider Delegate
      *
      * @param  owner       the BoundComponent which this belongs to
      * @param  compon      the component that this works for; must be a JSlider
      * @param  translator  how to translate between the value in the boundItem (type FROM) a another value of type TO
      * @param  boundItem   the object this is bound to (contains the value to be modified as specified by the getter and setter name)
      * @param  getterName  how to get the bound value from the bound object
      * @param  setterName  how to set the bound value from the bound object
      * @param  indexAt     if bound value is stored within a container with get(index) and 
      *                     set(index, Object) methods, then indexAt should be the index to 
      *                     use; otherwise, indexAt should be -1
      */
     public JSliderDelegate( BoundComponent owner, BoundComponent compon, 
                                   TypeTranslator<FROM, Integer> translator, Object boundItem, String getterName, String setterName, int indexAt ) {
         super( false, owner, compon, translator, boundItem, getterName, setterName, indexAt );
         completeInit();
     }
     
     /** adds a focus listener which binds any changes in the component to the object it is bound to when focus is lost */
     protected void completeInit() {
         load();
         
         //on focus lost, update the data structure
         me().addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 save(); //update the subcomponents AND refresh the text of the menu
             }
         });
     }
     
     //</editor-fold>
             
         
     //<editor-fold defaultstate="collapsed" desc="   Update the Binding    ">
     
     /** load the current text for this component from the bound object */
     public void load() {
         if( binding() == null ) return;
         
         try {
             me().setValue( binding().getTranslatedValue() );
             setValidationError( false );
         } catch( Exception e ) { //occurs when the boundItem is null
             me().setValue( 0 );
             if( binding().getBoundItem() != null) //only flag an error if the boundItem isn't null (shouldn't happen)
                 setValidationError( true, e.getMessage() );
             else
                 setValidationError( false );
         }
     }
     
     /** save the current text in this component to the bound object */
     public void save() {
         if( binding() == null ) return;
         
         setValue( me().getValue() );
     }
     
     //</editor-fold>
     
     
     //<editor-fold defaultstate="collapsed" desc="   Accessors/Mutators    ">
   
     /** get the JSlider this is bound to */
     private JSlider me() { return (JSlider)super.getMe(); }
     
     /** get the binding this is bound to */
     private Binding<FROM, Integer> binding() { 
         return super.getBinding(); 
     }
     
     /**
      * Sets the bound item with the the specified value
      *
     * @param i the new value to be set
      */
     public void setValue( Integer i ) {
         if( binding() == null ) return;
         
         try {
             //though there shouldn't ever be any conflict translating a Integer, allow for it
             if( !binding().canSetFromTranslatedValue( i  ) ) {
                 me().setValue( i  );
                 setValidationError( true, "Unable to translate Integer value: " + 
                                     binding().getTranslator().getLastException().getMessage() );
                 return;
             }
             setValidationError( false );
         
             binding().setFromTranslatedValue( i  );
             me().setValue( binding().getTranslatedValue() );
             if( getBoundParent() != null ) getBoundParent().notifyBoundComponentChange( getOwner() );
         } catch( Exception e ) {
             //can't happen
             setValidationError( true, e.getMessage() ); //apparently something went wrong
         }
     }
 
     //</editor-fold>
     
 }
