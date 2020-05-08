 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
 import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityLabelDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.LabelDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
 import edu.northwestern.bioinformatics.studycalendar.domain.Label;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.bind.ServletRequestUtils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nshurupova
  * Date: Jun 4, 2008
  * Time: 2:47:31 PM
  * To change this template use File | Settings | File Templates.
  */
 public class AddLabelWithRepetitionsController extends PscAbstractController {
     private PlannedActivityLabelDao plannedActivityLabelDao;
     private LabelDao labelDao;
     private PlannedActivityDao plannedActivityDao;
     protected final Logger log = LoggerFactory.getLogger(getClass());
 
     protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
         String days = ServletRequestUtils.getRequiredStringParameter(request, "days");
         String repetitions = ServletRequestUtils.getRequiredStringParameter(request, "repetitions");
         String plannedActivityIndices = ServletRequestUtils.getRequiredStringParameter(request, "arrayOfPlannedActivityIndices");
         Integer labelId = ServletRequestUtils.getIntParameter(request, "labelId");
 
         //have to delete labels completely and reenter with a new repetitions
         plannedActivityLabelDao.deleteByLabelId(labelId);
 
 
        Integer[] arrayOfDays = getArrayFromString(days, ";");
         Integer[] arrayOfPlannedActivityIndices = getArrayFromString(plannedActivityIndices, ",");
         String[] arrayOfRepetitions = repetitions.split(";");
 
         for (int i = 0; i < arrayOfDays.length; i++) {
             String repetitionString = arrayOfRepetitions[i];
 
             if (repetitionString !=null && repetitionString.length()>0) {
                 Integer[] repetitionsInteger = getArrayFromString(repetitionString, ",");
 
                 if (repetitionsInteger.length > 0) {
                     Integer plannedActivityId = arrayOfPlannedActivityIndices[i];
                     PlannedActivity plannedActivity = plannedActivityDao.getById(plannedActivityId);
                     for (int j =0; j< repetitionsInteger.length; j++) {
                         if (repetitionsInteger[j]!=null) {
                             PlannedActivityLabel plannedActivityLabel = new PlannedActivityLabel();
                             plannedActivityLabel.setLabel(labelDao.getById(labelId));
                             plannedActivityLabel.setPlannedActivity(plannedActivity);
                             plannedActivityLabel.setRepetitionNumber(repetitionsInteger[j]);
                             plannedActivityLabelDao.save(plannedActivityLabel);
                         }
                     }
                 }
             }
         }
         return null;
     }
 
 
     public PlannedActivityLabelDao getPlannedActivityLabelDao() {
         return plannedActivityLabelDao;
     }
 
     public void setPlannedActivityLabelDao(PlannedActivityLabelDao plannedActivityLabelDao) {
         this.plannedActivityLabelDao = plannedActivityLabelDao;
     }
 
 
     public LabelDao getLabelDao() {
         return labelDao;
     }
 
     public void setLabelDao(LabelDao labelDao) {
         this.labelDao = labelDao;
     }
 
 
     public PlannedActivityDao getPlannedActivityDao() {
         return plannedActivityDao;
     }
 
     public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
         this.plannedActivityDao = plannedActivityDao;
     }
 
     private Integer[] getArrayFromString(String stringToParse, String delimiter) {
         String[] parsedArray = stringToParse.split(delimiter);
         Integer[] result = new Integer[parsedArray.length];
         for (int i=0; i< parsedArray.length; i++) {
             if (parsedArray[i].indexOf("-")!=0){
                 Integer something = new Integer(parsedArray[i]);
                 result[i] = something;
             } else {
                 result[i] = null;
             }
         }
         return result;
     }
 
 }
