 
 /* First created by JCasGen Wed Oct 17 15:05:29 EDT 2012 */
 package util;
 
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.JCasRegistry;
 import org.apache.uima.cas.impl.CASImpl;
 import org.apache.uima.cas.impl.FSGenerator;
 import org.apache.uima.cas.FeatureStructure;
 import org.apache.uima.cas.impl.TypeImpl;
 import org.apache.uima.cas.Type;
 import org.apache.uima.cas.impl.FeatureImpl;
 import org.apache.uima.cas.Feature;
 import org.apache.uima.jcas.tcas.Annotation_Type;
 
 /** 
 * Updated by JCasGen Wed Oct 17 15:05:29 EDT 2012
  * @generated */
 public class NameEntity_Type extends Annotation_Type {
   /** @generated */
   @Override
   protected FSGenerator getFSGenerator() {return fsGenerator;}
   /** @generated */
   private final FSGenerator fsGenerator = 
     new FSGenerator() {
       public FeatureStructure createFS(int addr, CASImpl cas) {
   			 if (NameEntity_Type.this.useExistingInstance) {
   			   // Return eq fs instance if already created
   		     FeatureStructure fs = NameEntity_Type.this.jcas.getJfsFromCaddr(addr);
   		     if (null == fs) {
   		       fs = new NameEntity(addr, NameEntity_Type.this);
   			   NameEntity_Type.this.jcas.putJfsFromCaddr(addr, fs);
   			   return fs;
   		     }
   		     return fs;
         } else return new NameEntity(addr, NameEntity_Type.this);
   	  }
     };
   /** @generated */
   @SuppressWarnings ("hiding")
   public final static int typeIndexID = NameEntity.typeIndexID;
   /** @generated 
      @modifiable */
   @SuppressWarnings ("hiding")
   public final static boolean featOkTst = JCasRegistry.getFeatOkTst("util.NameEntity");
  
   /** @generated */
   final Feature casFeat_label;
   /** @generated */
   final int     casFeatCode_label;
   /** @generated */ 
   public String getLabel(int addr) {
         if (featOkTst && casFeat_label == null)
       jcas.throwFeatMissing("label", "util.NameEntity");
     return ll_cas.ll_getStringValue(addr, casFeatCode_label);
   }
   /** @generated */    
   public void setLabel(int addr, String v) {
         if (featOkTst && casFeat_label == null)
       jcas.throwFeatMissing("label", "util.NameEntity");
     ll_cas.ll_setStringValue(addr, casFeatCode_label, v);}
     
   
  
   /** @generated */
   final Feature casFeat_text;
   /** @generated */
   final int     casFeatCode_text;
   /** @generated */ 
   public String getText(int addr) {
         if (featOkTst && casFeat_text == null)
       jcas.throwFeatMissing("text", "util.NameEntity");
     return ll_cas.ll_getStringValue(addr, casFeatCode_text);
   }
   /** @generated */    
   public void setText(int addr, String v) {
         if (featOkTst && casFeat_text == null)
       jcas.throwFeatMissing("text", "util.NameEntity");
     ll_cas.ll_setStringValue(addr, casFeatCode_text, v);}
     
   
  
   /** @generated */
   final Feature casFeat_sent;
   /** @generated */
   final int     casFeatCode_sent;
   /** @generated */ 
   public int getSent(int addr) {
         if (featOkTst && casFeat_sent == null)
       jcas.throwFeatMissing("sent", "util.NameEntity");
     return ll_cas.ll_getRefValue(addr, casFeatCode_sent);
   }
   /** @generated */    
   public void setSent(int addr, int v) {
         if (featOkTst && casFeat_sent == null)
       jcas.throwFeatMissing("sent", "util.NameEntity");
     ll_cas.ll_setRefValue(addr, casFeatCode_sent, v);}
     
   
 
 
 
   /** initialize variables to correspond with Cas Type and Features
 	* @generated */
   public NameEntity_Type(JCas jcas, Type casType) {
     super(jcas, casType);
     casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());
 
  
     casFeat_label = jcas.getRequiredFeatureDE(casType, "label", "uima.cas.String", featOkTst);
     casFeatCode_label  = (null == casFeat_label) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_label).getCode();
 
  
     casFeat_text = jcas.getRequiredFeatureDE(casType, "text", "uima.cas.String", featOkTst);
     casFeatCode_text  = (null == casFeat_text) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_text).getCode();
 
  
     casFeat_sent = jcas.getRequiredFeatureDE(casType, "sent", "util.Sentence", featOkTst);
     casFeatCode_sent  = (null == casFeat_sent) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_sent).getCode();
 
   }
 }
 
 
 
     
