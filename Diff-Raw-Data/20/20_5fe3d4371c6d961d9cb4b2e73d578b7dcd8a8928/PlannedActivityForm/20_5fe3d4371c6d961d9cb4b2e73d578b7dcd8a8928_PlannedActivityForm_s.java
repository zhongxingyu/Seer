 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
 import edu.northwestern.bioinformatics.studycalendar.domain.Population;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.restlet.data.Status;
 import org.restlet.resource.Representation;
 import org.restlet.resource.ResourceException;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * @author Rhett Sutphin
  */
 public class PlannedActivityForm extends ValidatingForm {
     private Study study;
     private ActivityDao activityDao;
     private PopulationDao populationDao;
 
     public PlannedActivityForm(Representation entity, Study study, ActivityDao activityDao, PopulationDao populationDao) {
         super(entity);
         this.study = study;
         this.activityDao = activityDao;
         this.populationDao = populationDao;
 
         validatePresenceOf(FormParameters.DAY);
         validateIntegralityOf(FormParameters.DAY);
         validatePresenceOf(FormParameters.ACTIVITY_CODE);
         validatePresenceOf(FormParameters.ACTIVITY_SOURCE);
     }
 
     public PlannedActivity createDescribedPlannedActivity() throws ResourceException {
         throwForValidationFailureIfNecessary();
         Integer day = FormParameters.DAY.extractFirstAsIntegerFrom(this);
         Activity activity = findDescribedActivity();
         Population population = findDescribedPopulation();
 
         PlannedActivity newPlannedActivity = new PlannedActivity();
         newPlannedActivity.setDay(day);
         newPlannedActivity.setActivity(activity);
         newPlannedActivity.setPopulation(population);
         newPlannedActivity.setCondition(
             StringEscapeUtils.unescapeHtml(FormParameters.CONDITION.extractFirstFrom(this)));
         newPlannedActivity.setDetails(
             StringEscapeUtils.unescapeHtml(FormParameters.DETAILS.extractFirstFrom(this)));
         addDescribedLabels(newPlannedActivity);
 
         return newPlannedActivity;
     }
 
     private Activity findDescribedActivity() throws ResourceException {
         String activitySource = FormParameters.ACTIVITY_SOURCE.extractFirstFrom(this);
         String activityCode = FormParameters.ACTIVITY_CODE.extractFirstFrom(this);
 
         Activity activity = activityDao.getByCodeAndSourceName(activityCode, activitySource);
         if (activity == null) {
             throw new ResourceException(
                 Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Activity not found");
         }
         return activity;
     }
 
     private Population findDescribedPopulation() throws ResourceException {
         Population population = null;
         String populationAbbrev = FormParameters.POPULATION.extractFirstFrom(this);
         if (populationAbbrev != null) {
            population = populationDao.getByAbbreviation(study, populationAbbrev);
             if (population == null) {
                 throw new ResourceException(
                     Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Population not found");
             }
         }
         return population;
     }
 
     private void addDescribedLabels(PlannedActivity target) {
         for (String serializedPaLabel : this.getValuesArray(FormParameters.LABEL.attributeName())) {
             if (!StringUtils.isBlank(serializedPaLabel)) {
                 addDescribedLabels(target, serializedPaLabel);
             }
         }
     }
 
     private void addDescribedLabels(PlannedActivity target, String serialized) {
         String labelText;
         List<Integer> reps = new LinkedList<Integer>();
         if (serialized.indexOf(';') >= 0) {
             String[] parts = serialized.split(";");
             labelText = parts[0];
             if (parts.length > 1 && !StringUtils.isBlank(parts[1])) {
                 for (String n : parts[1].trim().split("\\s+")) {
                     try {
                         int i = Integer.parseInt(n);
                         if (i < 0) {
                             throw new StudyCalendarValidationException("The label '" + serialized + "' is invalid.  All rep numbers must be nonnegative integers.");
                         }
                         reps.add(i);
                     } catch (NumberFormatException nfe) {
                         throw new StudyCalendarValidationException("The label '" + serialized + "' is invalid.  All rep numbers must be nonnegative integers.", nfe);
                     }
                 }
             } else {
                 reps.add(null);
             }
         } else {
             labelText = serialized;
             reps.add(null);
         }
         for (Integer rep : reps) {
             PlannedActivityLabel newLabel = new PlannedActivityLabel();
             newLabel.setRepetitionNumber(rep);
             newLabel.setLabel(labelText);
             target.addPlannedActivityLabel(newLabel);
         }
     }
 }
