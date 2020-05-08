 package universite.toulouse.moodlexmlapi.tartopom.impl;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import universite.toulouse.moodlexmlapi.core.InvalidQuizFormatException;
 import universite.toulouse.moodlexmlapi.core.InvalidStreamSizeException;
 import universite.toulouse.moodlexmlapi.core.QuizImportExportService;
 import universite.toulouse.moodlexmlapi.tartopom.impl.model.ImportedQuiz;
 import universite.toulouse.moodlexmlapi.tartopom.impl.model.QuestionBase;
 import universite.toulouse.moodlexmlapi.tartopom.impl.model.QuestionCalculated;
 import universite.toulouse.moodlexmlapi.tartopom.impl.model.QuestionCategory;
 import universite.toulouse.moodlexmlapi.tartopom.impl.model.QuestionCloze;
 import universite.toulouse.moodlexmlapi.tartopom.impl.model.QuestionDescription;
 import universite.toulouse.moodlexmlapi.tartopom.impl.model.QuestionEssay;
 import universite.toulouse.moodlexmlapi.tartopom.impl.model.QuestionMatching;
 import universite.toulouse.moodlexmlapi.tartopom.impl.model.QuestionMultichoice;
 import universite.toulouse.moodlexmlapi.tartopom.impl.model.QuestionNumerical;
 import universite.toulouse.moodlexmlapi.tartopom.impl.model.QuestionShortAnswer;
 import universite.toulouse.moodlexmlapi.tartopom.impl.model.QuestionTrueFalse;
 import universite.toulouse.moodlexmlapi.tartopom.impl.model.Quiz;
 
 /**
  * Classe permettant l'import et l'export de Quiz.
  *
  * @author natsu
  *
  */
 public class ImportExportService implements QuizImportExportService {
 
     /**
      * Exporter un quiz à partir d'un objet java vers le format moodle xml.
      *
      * @param quiz le quiz en objet java
      * @return le quiz sous forme de flux
      */
     @Override
     public OutputStream exportQuiz(
             universite.toulouse.moodlexmlapi.core.data.Quiz quiz) {
        // TODO Auto-generated method stub
        return null;
     }
 
     /**
      * Importer un quiz à partir du format moodle xml vers un objet java.
      *
      * @param is quiz sous forme de flux
      * @return le quiz en objet java
      * @throws InvalidQuizFormatException format invalide (balises minimales
      *         non présentes)
      * @throws InvalidStreamSizeException taille du flux invalide
      */
     @Override
     public universite.toulouse.moodlexmlapi.core.data.ImportedQuiz importQuiz(
             InputStream is) throws InvalidQuizFormatException,
             InvalidStreamSizeException {
 
         ImportedQuiz quiz = null;
 
         try {
             // initialisation Jaxb pour unmarshalling
             JAXBContext context = JAXBContext.newInstance(Quiz.class,
                     QuestionBase.class, QuestionCalculated.class,
                     QuestionCategory.class, QuestionCloze.class,
                     QuestionDescription.class, QuestionEssay.class,
                     QuestionMatching.class, QuestionMultichoice.class,
                     QuestionNumerical.class, QuestionTrueFalse.class,
                     QuestionShortAnswer.class);
             Unmarshaller unmarshaller = context.createUnmarshaller();
 
             // parsing du xml
             Quiz importedQuiz = (Quiz) unmarshaller.unmarshal(is);
 
             // transformation en ImportedQuiz
             quiz = new ImportedQuiz();
             quiz.setQuestionList(importedQuiz.getQuestionList());
 
         }
         catch (JAXBException e) {
             e.printStackTrace();
         }
 
         return quiz;
     }
 }
