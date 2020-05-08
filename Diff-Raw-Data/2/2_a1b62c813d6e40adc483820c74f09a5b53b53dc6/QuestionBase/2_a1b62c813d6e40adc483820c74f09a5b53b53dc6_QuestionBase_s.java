 package universite.toulouse.moodlexmlapi.tartopom.impl.model;
 
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorNode;
 
 import universite.toulouse.moodlexmlapi.core.data.QuestionError;
 import universite.toulouse.moodlexmlapi.core.data.QuestionText;
 import universite.toulouse.moodlexmlapi.core.data.QuestionType;
 
 /**
  * Classe representant la base de tous les types de questions
  * @author rvermunt
  */
 @XmlDiscriminatorNode("@type")
 @XmlRootElement
 public class QuestionBase implements
         universite.toulouse.moodlexmlapi.core.data.Question {
 
     private Float defaultGrade;
     private List<QuestionError> errors;
     @XmlElement(name = "generalfeedback")
     private EnclosedText genFeedBack;
     private String imageBase64;
     private String imageURL;
     @XmlElement(name = "name")
     private EnclosedText name;
     private Float penalty;
     @XmlElement(name = "questiontext")
     private QuestionTextAdaptated questionText;
    @XmlAttribute(name = "type")
     private String questionType;
     @XmlElement(name = "hidden")
     private int hidden;
 
     /**
      * Constructeur vide
      */
     public QuestionBase() {
 
     }
 
     /**
      * @param defaultGrade Float
      * @param errors List<QuestionError>
      * @param genFeedBack EnclosedText
      * @param imageBase64 String
      * @param imageURL String
      * @param name EnclosedText
      * @param penalty Float
      * @param questionText QuestionText
      * @param questionType QuestionType
      * @param hidden boolean
      */
     public QuestionBase(Float defaultGrade, List<QuestionError> errors,
             EnclosedText genFeedBack, String imageBase64, String imageURL,
             EnclosedText name, Float penalty, QuestionText questionText,
             QuestionType questionType, boolean hidden) {
         super();
         this.defaultGrade = defaultGrade;
         this.errors = errors;
         this.genFeedBack = genFeedBack;
         this.imageBase64 = imageBase64;
         this.imageURL = imageURL;
         this.name = name;
         this.penalty = penalty;
         this.setQuestionText(questionText);
         this.questionType = questionType.name();
         this.setHidden(hidden);
     }
 
     /**
      * @return defaultGrade
      */
 
     @Override
     @XmlElement(name = "defaultgrade")
     public Float getDefaultGrade() {
         return this.defaultGrade;
     }
 
     /**
      * @return errors
      */
     @Override
     @XmlTransient
     public List<QuestionError> getErrors() {
         return this.errors;
     }
 
     /**
      * @return generalFeedBack
      */
     @Override
     @XmlTransient
     public String getGeneralFeedBack() {
         return this.genFeedBack.getText();
     }
 
     /**
      * @return imageBase64
      */
     @Override
     @XmlElement(name = "image_base64")
     public String getImageBase64() {
         return this.imageBase64;
     }
 
     /**
      * @return imageURL
      */
     @Override
     @XmlElement(name = "image")
     public String getImageUrl() {
         return this.imageURL;
     }
 
     /**
      * @return name
      */
 
     @Override
     @XmlTransient
     public String getName() {
         return this.name.getText();
     }
 
     /**
      * @return penalty
      */
 
     @Override
     @XmlElement(name = "penalty")
     public Float getPenalty() {
         return this.penalty;
     }
 
     /**
      * @return questionText
      */
 
     @Override
     @XmlTransient
     public QuestionText getQuestionText() {
         return new QuestionText(this.questionText.getText(),
                 this.questionText.getQuestionTextFormat());
     }
 
     /**
      * @return type
      */
 
     @Override
     @XmlTransient
     public QuestionType getQuestionType() {
         return QuestionType.valueOf(this.questionType);
     }
 
     /**
      * @return hidden
      */
 
     @Override
     @XmlTransient
     public Boolean isHidden() {
         return this.hidden == 1;
     }
 
     /**
      * @param defaultGrade
      *            Float
      */
     public void setDefaultGrade(Float defaultGrade) {
         this.defaultGrade = defaultGrade;
     }
 
     /**
      * @param errors
      *            List<QuestionError>
      */
     public void setErrors(List<QuestionError> errors) {
         this.errors = errors;
     }
 
     /**
      * @param genFeedBack
      *            EnclosedText
      */
     public void setGeneralFeedBack(EnclosedText genFeedBack) {
         this.genFeedBack = genFeedBack;
     }
 
     /**
      * @param hidden
      *            boolean
      */
     public void setHidden(boolean hidden) {
         if (hidden) {
             this.hidden = 1;
         }
         else {
             this.hidden = 0;
         }
     }
 
     /**
      * @param imageBase64
      *            String
      */
     public void setImageBase64(String imageBase64) {
         this.imageBase64 = imageBase64;
     }
 
     /**
      * @param imageURL
      *            String
      */
     public void setImageURL(String imageURL) {
         this.imageURL = imageURL;
     }
 
     /**
      * @param name
      *            EnclosedText
      */
     public void setName(EnclosedText name) {
         this.name = name;
     }
 
     /**
      * @param penalty
      *            Float
      */
     public void setPenalty(Float penalty) {
         this.penalty = penalty;
     }
 
     /**
      * @param questionText
      *            QuestionText
      */
     public void setQuestionText(QuestionText questionText) {
         this.questionText = new QuestionTextAdaptated(questionText.getText(),
                 questionText.getQuestionTextFormat());
     }
 
     /**
      * @param questionType
      *            QuestionType
      */
     public void setQuestionType(QuestionType questionType) {
         this.questionType = questionType.name();
     }
 
 }
