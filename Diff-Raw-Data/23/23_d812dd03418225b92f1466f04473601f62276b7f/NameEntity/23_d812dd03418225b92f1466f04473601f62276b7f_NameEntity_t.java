 
 
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
 public class NameEntity extends Annotation {
   /** @generated
    * @ordered 
    */
   @SuppressWarnings ("hiding")
   public final static int typeIndexID = JCasRegistry.register(NameEntity.class);
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
   protected NameEntity() {/* intentionally empty block */}
     
   /** Internal - constructor used by generator 
    * @generated */
   public NameEntity(int addr, TOP_Type type) {
     super(addr, type);
     readObject();
   }
   
   /** @generated */
   public NameEntity(JCas jcas) {
     super(jcas);
     readObject();   
   } 
 
   /** @generated */  
   public NameEntity(JCas jcas, int begin, int end) {
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
   //* Feature: label
 
   /** getter for label - gets 
    * @generated */
   public String getLabel() {
     if (NameEntity_Type.featOkTst && ((NameEntity_Type)jcasType).casFeat_label == null)
       jcasType.jcas.throwFeatMissing("label", "util.NameEntity");
     return jcasType.ll_cas.ll_getStringValue(addr, ((NameEntity_Type)jcasType).casFeatCode_label);}
     
   /** setter for label - sets  
    * @generated */
   public void setLabel(String v) {
     if (NameEntity_Type.featOkTst && ((NameEntity_Type)jcasType).casFeat_label == null)
       jcasType.jcas.throwFeatMissing("label", "util.NameEntity");
     jcasType.ll_cas.ll_setStringValue(addr, ((NameEntity_Type)jcasType).casFeatCode_label, v);}    
    
     
   //*--------------*
   //* Feature: text
 
   /** getter for text - gets 
    * @generated */
   public String getText() {
     if (NameEntity_Type.featOkTst && ((NameEntity_Type)jcasType).casFeat_text == null)
       jcasType.jcas.throwFeatMissing("text", "util.NameEntity");
     return jcasType.ll_cas.ll_getStringValue(addr, ((NameEntity_Type)jcasType).casFeatCode_text);}
     
   /** setter for text - sets  
    * @generated */
   public void setText(String v) {
     if (NameEntity_Type.featOkTst && ((NameEntity_Type)jcasType).casFeat_text == null)
       jcasType.jcas.throwFeatMissing("text", "util.NameEntity");
     jcasType.ll_cas.ll_setStringValue(addr, ((NameEntity_Type)jcasType).casFeatCode_text, v);}    
    
     
   //*--------------*
   //* Feature: sent
 
   /** getter for sent - gets 
    * @generated */
   public Sentence getSent() {
     if (NameEntity_Type.featOkTst && ((NameEntity_Type)jcasType).casFeat_sent == null)
       jcasType.jcas.throwFeatMissing("sent", "util.NameEntity");
     return (Sentence)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((NameEntity_Type)jcasType).casFeatCode_sent)));}
     
   /** setter for sent - sets  
    * @generated */
   public void setSent(Sentence v) {
     if (NameEntity_Type.featOkTst && ((NameEntity_Type)jcasType).casFeat_sent == null)
       jcasType.jcas.throwFeatMissing("sent", "util.NameEntity");
     jcasType.ll_cas.ll_setRefValue(addr, ((NameEntity_Type)jcasType).casFeatCode_sent, jcasType.ll_cas.ll_getFSRef(v));}    
   }
 
     
