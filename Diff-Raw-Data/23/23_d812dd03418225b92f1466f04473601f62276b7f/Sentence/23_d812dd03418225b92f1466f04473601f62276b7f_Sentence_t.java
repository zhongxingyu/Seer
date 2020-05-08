 
 
 /* First created by JCasGen Wed Oct 17 15:05:29 EDT 2012 */
 package util;
 
 import org.apache.uima.jcas.JCas; 
 import org.apache.uima.jcas.JCasRegistry;
 import org.apache.uima.jcas.cas.TOP_Type;
 
 import org.apache.uima.jcas.tcas.Annotation;
 
 
 /** 
 * Updated by JCasGen Thu Oct 18 10:04:33 EDT 2012
  * XML source: E:/Homework/Software Engineering/hw1-yvchen/src/main/resources/typeSystem/typeSystemDescriptor.xml
  * @generated */
 public class Sentence extends Annotation {
   /** @generated
    * @ordered 
    */
   @SuppressWarnings ("hiding")
   public final static int typeIndexID = JCasRegistry.register(Sentence.class);
   /** @generated
    * @ordered 
    */
   @SuppressWarnings ("hiding")
   public final static int type = typeIndexID;
   /** @generated  */
   @Override
   public              int getTypeIndexID() {return typeIndexID;}
  
   /** Never called.  Disable default constructor
    * @generated */
   protected Sentence() {/* intentionally empty block */}
     
   /** Internal - constructor used by generator 
    * @generated */
   public Sentence(int addr, TOP_Type type) {
     super(addr, type);
     readObject();
   }
   
   /** @generated */
   public Sentence(JCas jcas) {
     super(jcas);
     readObject();   
   } 
 
   /** @generated */  
   public Sentence(JCas jcas, int begin, int end) {
     super(jcas);
     setBegin(begin);
     setEnd(end);
     readObject();
   }   
 
   /** <!-- begin-user-doc -->
     * Write your own initialization here
     * <!-- end-user-doc -->
   @generated modifiable */
   private void readObject() {/*default - does nothing empty block */}
      
  
     
   //*--------------*
   //* Feature: id
 
   /** getter for id - gets 
    * @generated */
   public String getId() {
     if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_id == null)
       jcasType.jcas.throwFeatMissing("id", "util.Sentence");
     return jcasType.ll_cas.ll_getStringValue(addr, ((Sentence_Type)jcasType).casFeatCode_id);}
     
   /** setter for id - sets  
    * @generated */
   public void setId(String v) {
     if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_id == null)
       jcasType.jcas.throwFeatMissing("id", "util.Sentence");
     jcasType.ll_cas.ll_setStringValue(addr, ((Sentence_Type)jcasType).casFeatCode_id, v);}    
    
     
   //*--------------*
   //* Feature: text
 
   /** getter for text - gets 
    * @generated */
   public String getText() {
     if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_text == null)
       jcasType.jcas.throwFeatMissing("text", "util.Sentence");
     return jcasType.ll_cas.ll_getStringValue(addr, ((Sentence_Type)jcasType).casFeatCode_text);}
     
   /** setter for text - sets  
    * @generated */
   public void setText(String v) {
     if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_text == null)
       jcasType.jcas.throwFeatMissing("text", "util.Sentence");
     jcasType.ll_cas.ll_setStringValue(addr, ((Sentence_Type)jcasType).casFeatCode_text, v);}    
   }
 
     
