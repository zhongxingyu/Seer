 package pl.agh.enrollme.controller.preferencesmanagement;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import pl.agh.enrollme.model.EnrollConfiguration;
 import pl.agh.enrollme.model.StudentPointsPerTerm;
 import pl.agh.enrollme.model.Subject;
 import pl.agh.enrollme.model.Term;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This class is used as a controller for the Progress Ring component from preferences-management view.
  * Author: Piotr Turek
  */
 public class ProgressRingController implements Serializable {
 
     private static final long serialVersionUID = 6578477862668112963L;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ProgressRingController.class);
 
     private EnrollConfiguration enrollConfiguration;
 
     private List<Subject> subjects;
 
     private List<Term> terms;
 
     private List<StudentPointsPerTerm> points;
 
     /**
      * This is a list of ProgressToken instances used by the Ring component.
      * They encapsulate current state of the process.
      */
     private List<ProgressToken> progressTokens = new ArrayList<>();
 
     /**
      * A map used to hold current amounts of used points per subject. SubjectID works as a key.
      */
     private Map<Integer, Integer> pointsMap = new HashMap<>();
 
     /**
      * How many extra points have been used.
      */
     private int extraPointsUsed = 0;
 
 
     public ProgressRingController(EnrollConfiguration enrollConfiguration, List<Subject> subjects, List<Term> terms,
                                   List<StudentPointsPerTerm> points) {
         this.enrollConfiguration = enrollConfiguration;
         this.subjects = subjects;
         this.terms = terms;
         this.points = points;
 
         updatePointsUsage();
         createProgressTokens();
     }
 
     public EnrollConfiguration getEnrollConfiguration() {
         return enrollConfiguration;
     }
 
     public void setEnrollConfiguration(EnrollConfiguration enrollConfiguration) {
         this.enrollConfiguration = enrollConfiguration;
     }
 
     public List<Subject> getSubjects() {
         return subjects;
     }
 
     public void setSubjects(List<Subject> subjects) {
         this.subjects = subjects;
     }
 
     public List<Term> getTerms() {
         return terms;
     }
 
     public void setTerms(List<Term> terms) {
         this.terms = terms;
     }
 
     public List<StudentPointsPerTerm> getPoints() {
         return points;
     }
 
     public void setPoints(List<StudentPointsPerTerm> points) {
         this.points = points;
     }
 
     public List<ProgressToken> getProgressTokens() {
         return progressTokens;
     }
 
     public Map<Integer, Integer> getPointsMap() {
         return pointsMap;
     }
 
     public int getExtraPointsUsed() {
         return extraPointsUsed;
     }
 
     /**
      * Updates used values and state of progress tokens
      */
     public void update() {
         updatePointsUsage();
         updateProgressTokens();
         LOGGER.debug("Progress Ring updated");
     }
 
     /**
      * Updates state of progress tokens
      */
     private void updateProgressTokens() {
         for (ProgressToken token : progressTokens) {
             final int id = token.getId();
 
             Integer used = 0;
             if (id == -1) {
                 used = extraPointsUsed;
             } else {
                 used = pointsMap.get(id);
                 if (used == null) {
                     used = 0;
                 }
             }
 
             token.setPointsUsed(used);
             LOGGER.debug("Token: " + token.getId() + ", " + token.getName() + ", " + token.getMaxPoints() +
                     ", " + token.getMinPoints() + ", " + token.getPointsUsed() +  ", #" + token.getColor() + " updated");
 
         }
         LOGGER.debug("Tokens updated");
     }
 
     /**
      * Updates points used per progress token
      */
     private void updatePointsUsage() {
         pointsMap.clear();
         extraPointsUsed = 0;
         LOGGER.debug("Usage data cleared");
 
         for (StudentPointsPerTerm p : points) {
             final Term term = p.getTerm();
             final Subject subject = term.getSubject();
             Integer value = pointsMap.get(subject.getSubjectID());
             if(value == null) {
                 value = 0;
             }
 
             int assignedPoints = p.getPoints();
             //we treat impossibility as a zero-point term
             if (assignedPoints == -1) {
                 assignedPoints = 0;
             }
 
             if (value >= enrollConfiguration.getPointsPerSubject()) {
                 extraPointsUsed += assignedPoints;
                 LOGGER.debug("Extra points usage updated to " + extraPointsUsed);
             } else if (value + assignedPoints > enrollConfiguration.getPointsPerSubject()) {
                 extraPointsUsed += assignedPoints - (enrollConfiguration.getPointsPerSubject() - value);
                 LOGGER.debug("Extra points usage updated to " + extraPointsUsed);
             }
 
             pointsMap.put(subject.getSubjectID(), value + assignedPoints);
             LOGGER.debug("Subject " + subject.getName() + " usage updated to " + pointsMap.get(subject.getSubjectID()));
         }
         LOGGER.debug("Point usage computed");
     }
 
     /**
      * Creates progress tokens after points usage has been calculated
      */
     private void createProgressTokens() {
         final ProgressToken extraToken = new ProgressToken(-1, "Extra",
                 enrollConfiguration.getAdditionalPoints(), 0, extraPointsUsed);
 
         progressTokens.add(extraToken);
         LOGGER.debug("Extra token added");
 
         for (Subject s : subjects) {
             LOGGER.debug("subjectID=" + s.getSubjectID() + ", subjectName=" + s.getName() +
                     ", pps=" + enrollConfiguration.getPointsPerSubject() + ", mpps=" + enrollConfiguration.getMinimumPointsPerSubject() +
             ", pointsUsed=" + pointsMap.get(s.getSubjectID()) + ", subjectColor=#" + s.getColor());
 
             if (!s.getHasInteractive()) {
                 continue;
             }
 
             Integer pointsUsed = pointsMap.get(s.getSubjectID());
             if (pointsUsed == null) {
                 pointsUsed = 0;
             }
 
             final ProgressToken token = new ProgressToken(s.getSubjectID(), s.getName(),
                     enrollConfiguration.getPointsPerSubject(),enrollConfiguration.getMinimumPointsPerSubject(),
                     pointsUsed, s.getColor());
 
             LOGGER.debug("New token created: " + token.getId() + ", " + token.getName() + ", " + token.getMaxPoints() +
             ", " + token.getMinPoints() + ", " + token.getPointsUsed() +  ", #" + token.getColor());
 
             progressTokens.add(token);
         }
 
         //if there are exactly 2 progress tokens, clone the second one (to prevent unwanted behaviour of the ring component)
         if (progressTokens.size() == 2) {
             progressTokens.add(progressTokens.get(1));
             final ProgressToken token = progressTokens.get(2);
             LOGGER.debug("Additional token created: " + token.getId() + ", " + token.getName() + ", " + token.getMaxPoints() +
                     ", " + token.getMinPoints() + ", " + token.getPointsUsed() +  ", #" + token.getColor());
         }
         LOGGER.debug("Tokens created");
     }
 
 }
