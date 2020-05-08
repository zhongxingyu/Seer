 package universite.toulouse.moodlexmlapi.tartopom.impl.model;
 
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;
 
 import universite.toulouse.moodlexmlapi.core.data.QuestionType;
 
 /**
  * Question de type 'multichoice'.
  *
  * @author natsu
  *
  */
 @XmlDiscriminatorValue("multichoice")
 public class QuestionMultichoice extends QuestionBase {
 
     private boolean single;
     private boolean shuffleAnswers;
     @XmlElement(name = "answernumbering")
     private String answerNumbering;
     private EnclosedText correctFeedBack;
     private EnclosedText partiallycorrectFeedBack;
     private EnclosedText incorrectFeedBack;
     // on utilise EnclosedText juste pour avoir un objet vide permettant une
     // balise auto-fermante
     @XmlElement(name = "shownumcorrect")
     private EnclosedText showNumCorrect;
     private List<MultichoiceAnswer> answers;
     private List<Hint> hints;
 
     /**
      * Constructeur par défaut
      */
     public QuestionMultichoice() {
         super();
         super.setQuestionType(QuestionType.multichoice);
     }
 
     /**
      * Constructeur d'une question de type 'multichoice'.
      *
      * @param single vrai : une seule réponse, faux : plusieurs réponses
      * @param shuffleAnswers faut-il mélanger les réponses
      * @param answerNumbering le type de numérotation
      * @param correctFeedBack feedback en cas de réponse correcte
      * @param partiallycorrectFeedBack feedback en cas de réponse partiellement
      *        correcte
      * @param incorrectFeedBack feedback en cas de réponse incorrecte
      * @param showNumCorrect faut-il afficher le nombre de bonnes réponses
      * @param answers les réponses possibles
      * @param hints les indices
      */
     public QuestionMultichoice(boolean single, boolean shuffleAnswers,
             AnswerNumbering answerNumbering, EnclosedText correctFeedBack,
             EnclosedText partiallycorrectFeedBack,
             EnclosedText incorrectFeedBack, boolean showNumCorrect,
             List<MultichoiceAnswer> answers, List<Hint> hints) {
         super();
         this.single = single;
         this.shuffleAnswers = shuffleAnswers;
         this.setAnswerNumbering(answerNumbering);
         this.correctFeedBack = correctFeedBack;
         this.partiallycorrectFeedBack = partiallycorrectFeedBack;
         this.incorrectFeedBack = incorrectFeedBack;
         this.setShowNumCorrect(showNumCorrect);
         this.answers = answers;
         this.hints = hints;
     }
 
     /**
      * Renvoie le type de numérotation des réponses.
      *
      * @return the answerNumbering
      */
     @XmlTransient
     public AnswerNumbering getAnswerNumbering() {
         return AnswerNumbering.valueOf("_" + answerNumbering);
     }
 
     /**
      * Renvoie les réponses possibles.
      *
      * @return the answers
      */
     @XmlElement(name = "answer")
     public List<MultichoiceAnswer> getAnswers() {
         return answers;
     }
 
     /**
      * Renvoie le feedback en cas de réponse correcte.
      *
      * @return the correctFeedBack
      */
     @XmlElement(name = "correctfeedback")
     public EnclosedText getCorrectFeedBack() {
         return correctFeedBack;
     }
 
     /**
      * Renvoie les indices.
      *
      * @return the hints
      */
     @XmlElement(name = "hint")
     public List<Hint> getHints() {
         return hints;
     }
 
     /**
      * Renvoie le feedback en cas de réponse incorrecte.
      *
      * @return the incorrectFeedBack
      */
     @XmlElement(name = "incorrectfeedback")
     public EnclosedText getIncorrectFeedBack() {
         return incorrectFeedBack;
     }
 
     /**
      * Renvoie le feedback en cas de réponse partiellement correcte.
      *
      * @return the partiallycorrectFeedBack
      */
     @XmlElement(name = "partiallycorrectfeedback")
     public EnclosedText getPartiallycorrectFeedBack() {
         return partiallycorrectFeedBack;
     }
 
     /**
      * Le nombre de réponses correctes doit-il être affiché ?
      *
      * @return the showNumCorrect
      */
     @XmlTransient
     public boolean isShowNumCorrect() {
         return showNumCorrect != null;
     }
 
     /**
      * Est-ce que les réponses doivent être mélangées ?
      *
      * @return the shuffleAnswers
      */
    @XmlElement(name = "suffleanswers")
     public boolean isShuffleAnswers() {
         return shuffleAnswers;
     }
 
     /**
      * Une seule réponse autorisée ou réponses multiples ?
      *
      * @return the single true : une seule réponse, false : multi réponses
      */
     @XmlElement(name = "single")
     public boolean isSingle() {
         return single;
     }
 
     /**
      * Assigne le type de numérotation.
      *
      * @param answerNumbering the answerNumbering to set
      */
     public void setAnswerNumbering(AnswerNumbering answerNumbering) {
         this.answerNumbering = answerNumbering.name().substring(1);
     }
 
     /**
      * Assigne les réponses possibles.
      *
      * @param answers the answers to set
      */
     public void setAnswers(List<MultichoiceAnswer> answers) {
         this.answers = answers;
     }
 
     /**
      * Assigne le feedback en cas de réponse correcte.
      *
      * @param correctFeedBack the correctFeedBack to set
      */
     public void setCorrectFeedBack(EnclosedText correctFeedBack) {
         this.correctFeedBack = correctFeedBack;
     }
 
     /**
      * Assigne les indices.
      *
      * @param hints the hints to set
      */
     public void setHints(List<Hint> hints) {
         this.hints = hints;
     }
 
     /**
      * Assigne le feedback en cas de réponse incorrecte.
      *
      * @param incorrectFeedBack the incorrectFeedBack to set
      */
     public void setIncorrectFeedBack(EnclosedText incorrectFeedBack) {
         this.incorrectFeedBack = incorrectFeedBack;
     }
 
     /**
      * Assigne le feedback en cas de réponse partiellement correcte.
      *
      * @param partiallycorrectFeedBack the partiallycorrectFeedBack to set
      */
     public void setPartiallycorrectFeedBack(
             EnclosedText partiallycorrectFeedBack) {
         this.partiallycorrectFeedBack = partiallycorrectFeedBack;
     }
 
     /**
      * Faut-il afficher le nombre de bonnes réponses ?
      *
      * @param showNumCorrect the showNumCorrect to set
      */
     public void setShowNumCorrect(boolean showNumCorrect) {
         if (showNumCorrect) {
             this.showNumCorrect = new EnclosedText();
         }
         else {
             this.showNumCorrect = null;
         }
     }
 
     /**
      * Est-ce que les réponses doivent être mélangées ?
      *
      * @param shuffleAnswers the shuffleAnswers to set
      */
     public void setShuffleAnswers(boolean shuffleAnswers) {
         this.shuffleAnswers = shuffleAnswers;
     }
 
     /**
      * Une seule réponse ou réponses multiples ?
      *
      * @param single the single to set true : une seule réponse, false : multi
      *          réponses
      */
     public void setSingle(boolean single) {
         this.single = single;
     }
 }
