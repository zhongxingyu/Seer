 package at.easydiet.businesslogic;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.HibernateException;
 
 import at.easydiet.EasyDietApplication;
 import at.easydiet.businessobjects.DietParameterBO;
 import at.easydiet.businessobjects.DietPlanBO;
 import at.easydiet.businessobjects.DietTreatmentBO;
 import at.easydiet.businessobjects.IDietParameterizable;
 import at.easydiet.businessobjects.MealBO;
 import at.easydiet.businessobjects.MealLineBO;
 import at.easydiet.businessobjects.PlanTypeBO;
 import at.easydiet.businessobjects.RecipeBO;
 import at.easydiet.businessobjects.TimeSpanBO;
 import at.easydiet.dao.DAOFactory;
 import at.easydiet.dao.DietPlanDAO;
 import at.easydiet.dao.HibernateUtil;
 import at.easydiet.dao.MealDAO;
 import at.easydiet.domainlogic.DietParameterController.ValidationResult;
 import at.easydiet.util.StringUtils;
 import at.easydiet.validation.ParameterTemplateValidator;
 
 /**
  * Provides data and methods for the {@link DietPlanManagementView}
  */
 public class DietPlanEditingController extends BusinessLogicController
 {
     /**
      * Logger for debugging purposes
      */
     private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
                                                              .getLogger(DietPlanEditingController.class);
 
     /**
      * The current opened {@link DietPlanBO}
      */
     private DietPlanBO                           _dietPlan;
     /**
      * Stores existing meal codes
      */
     private Collection<String>                         _mealCodes;
     /**
      * Stores existing meal names
      */
     private Collection<String>                         _mealNames;
 
     /**
      * Stores errors showed in the ErrorBox
      */
     private List<String>                         _errors;
 
     /**
      * Gets the dietPlan.
      * 
      * @return the dietPlan
      */
     public DietPlanBO getDietPlan()
     {
         return _dietPlan;
     }
 
     /**
      * Sets the current active {@link DietPlanBO}
      * 
      * @param dietPlan
      *            The {@link DietPlanBO} to set
      */
     public void setDietPlan(DietPlanBO dietPlan)
     {
         _dietPlan = dietPlan;
         validateDietPlan();
     }
 
     /**
      * Create a new {@link TimeSpanBO} and add it to dhe {@link DietPlanBO}
      * 
      * @return The new {@link TimeSpanBO}
      */
     public TimeSpanBO createTimeSpan()
     {
         TimeSpanBO t = new TimeSpanBO();
         _dietPlan.addTimeSpans(t);
         validateDietPlan();
         return t;
     }
 
     /**
      * Gets a new instance of this class.
      * @return a new instance for the current thread.
      */
     static DietPlanEditingController newInstance(BusinessLogicProvider provider)
     {
         return new DietPlanEditingController(provider);
     }
 
     /**
      * Create a new {@link DietPlanBO} in a {@link DietTreatmentBO}
      * 
      * @param dietTreatment
      *            The {@link DietTreatmentBO} which contains the new
      *            {@link DietPlanBO}
      */
     public void createNew(DietTreatmentBO dietTreatment)
     {
         _dietPlan = new DietPlanBO();
         _dietPlan.setDietTreatment(dietTreatment);
     }
 
     /**
      * Create a new {@link MealBO} in a {@link TimeSpanBO}
      * 
      * @param timeSpan
      *            The {@link TimeSpanBO} which contains the new {@link MealBO}
      * @return The new {@link MealBO}
      */
     public MealBO createMeal(TimeSpanBO timeSpan)
     {
         MealBO meal = new MealBO();
         timeSpan.addMeals(meal);
         validateDietPlan();
         return meal;
     }
 
     /**
      * Add a {@link RecipeBO} to a {@link MealBO}
      * 
      * @param meal
      *            {@link MealBO} to add the {@link RecipeBO}
      * @param recipe
      *            {@link RecipeBO} to add to the {@link MealBO}
      * @return A {@link MealLineBO}.
      */
     public MealLineBO addRecipeToMeal(MealBO meal, RecipeBO recipe)
     {
         MealLineBO line = new MealLineBO();
         line.setMeal(meal);
         line.setRecipe(recipe);
         line.setQuantity(recipe.getAmount());
         meal.addMealLines(line);
         validateDietPlan();
         return line;
     }
 
     /**
      * Remove a {@link MealLineBO}
      * 
      * @param mealLine
      *            The {@link MealLineBO} to remove
      */
     public void removeMealLine(MealLineBO mealLine)
     {
         if (mealLine == null) return;
         mealLine.getMeal().removeMealLines(mealLine);
         // If alternative, remove from parent too
         if (mealLine.isAlternative())
         {
             mealLine.getParent().getAlternatives().remove(mealLine);
         }
         validateDietPlan();
     }
 
     /**
      * Add a {@link RecipeBO} as an alternative meal to a {@link MealLineBO}
      * 
      * @param mealLine
      *            The {@link MealLineBO} to add the alternative
      * @param recipe
      *            The {@link RecipeBO} to add
      * @return The alternative {@link MealLineBO}
      */
     public MealLineBO addRecipeAsAlternative(MealLineBO mealLine,
             RecipeBO recipe)
     {
         MealLineBO alternative = new MealLineBO();
         alternative.setParent(mealLine);
         alternative.setMeal(mealLine.getMeal());
         alternative.setRecipe(recipe);
         alternative.setQuantity(recipe.getAmount());
 
         // insert alternative after mealLine
         int index = mealLine.getMeal().getMealLines().indexOf(mealLine) + 1;
         mealLine.getMeal().addMealLines(index, alternative);
         validateDietPlan();
         return alternative;
     }
 
     /**
      * Save the current opened {@link DietPlanBO}
      * 
      * @return True if saving was possible
      */
     public boolean saveDietPlan()
     {
         validateDietPlan(true);
 
         if (getErrors().size() > 0) return false;
 
         SimpleDateFormat formatter = new SimpleDateFormat(
                 EasyDietApplication.DATETIME_FORMAT);
         // generate a good name if it's a new plan
         if (_dietPlan.getDietPlanId() <= 0)
         {
             _dietPlan.setCreatedOn(new Date());
             String name = String.format("Diätplan vom %s",
                     formatter.format(_dietPlan.getCreatedOn()));
             _dietPlan.setName(name);
         }
 
         // update creator
         _dietPlan.setCreator(getRootProvider().getSystemUserController().getCurrentUser());
 
         try
         {
             HibernateUtil.currentSession().beginTransaction();
             DietPlanDAO dao = DAOFactory.getInstance().getDietPlanDAO();
             dao.makePersistent(_dietPlan.getModel());
             HibernateUtil.currentSession().getTransaction().commit();
             _dietPlan = null;
             return true;
         }
         catch (HibernateException e)
         {
             LOG.error("Could not save dietplan", e);
             HibernateUtil.currentSession().getTransaction().rollback();
             return false;
         }
     }
 
     /**
      * Validate the {@link DietPlanBO} if something is wrong
      */
     public void validateDietPlan()
     {
         validateDietPlan(false);
     }
 
     /**
      * Validate the {@link DietPlanBO} if someting is wrong or missing
      * 
      * @param checkForEmpty
      *            If true: check for empty elements too
      */
     public void validateDietPlan(boolean checkForEmpty)
     {
         _errors.clear();
 
         // validate empty elements
         if (checkForEmpty)
         {
             validateEmptyElements();
         }
 
         // validate all timespans
         validateTimeSpans();
 
         // validate all dietparameters for conflicts
         validateDietParameterConflicts();
 
         // validate all dietparameters if they match for hierarchy
         validateDietPlanParameters();
 
     }
 
     /**
      * If the {@link PlanTypeBO} is {@link PlanTypeBO#NUTRITION_RECOMMENDATION},
      * the {@link DietPlanBO} is only for {@link DietParameterBO} and it's not
      * possible to add {@link MealLineBO}s
      * 
      * @return True if {@link DietPlanBO} is of type
      *         {@link PlanTypeBO#NUTRITION_RECOMMENDATION}
      */
     public boolean isDietParameterOnlyPlan()
     {
         return _dietPlan.getPlanType().equals(
                 PlanTypeBO.NUTRITION_RECOMMENDATION);
     }
 
     /**
      * Validate for empty elements
      */
     private void validateEmptyElements()
     {
         int planParameterCount = 0;
 
         for (TimeSpanBO timeSpan : _dietPlan.getTimeSpans())
         {
             int timeSpanParameterCount = 0;
             if (timeSpan.getMeals().size() == 0)
             {
                 getErrors()
                         .add(String
                                 .format("Kein Mahlzeiten zum Zeitraum '%s' hinzugefügt!",
                                         timeSpan.getDisplayText()));
             }
 
             for (MealBO meal : timeSpan.getMeals())
             {
                 if (StringUtils.isNullOrWhitespaceOnly(meal.getCode()))
                 {
                     getErrors()
                             .add(String
                                     .format("Kein Code für die Mahlzeit '%s' angegeben!",
                                             meal.getDisplayText()));
                 }
                 if (StringUtils.isNullOrWhitespaceOnly(meal.getName()))
                 {
                     getErrors()
                             .add(String
                                     .format("Kein Name für die Mahlzeit '%s' angegeben!",
                                             meal.getDisplayText()));
                 }
 
                 // no meallines required for nutrition recommendations
                 if (!isDietParameterOnlyPlan())
                 {
                     if (meal.getMealLines().size() == 0)
                     {
                         getErrors()
                                 .add(String
                                         .format("Kein Rezepte zur Mahlzeit '%s' hinzugefügt!",
                                                 meal.getDisplayText()));
                     }
                 }
                 else
                 {
                     if (meal.getDietParameters().size() == 0)
                     {
                         getErrors()
                                 .add(String
                                         .format("Keine Zielparameter zur Mahlzeit '%s' hinzugefügt!",
                                                 meal.getDisplayText()));
 
                     }
 
                     // sum parameters for timeSpan
                     timeSpanParameterCount += meal.getDietParameters()
                             .size();
                 }
             }
 
             if (isDietParameterOnlyPlan() && timeSpanParameterCount == 0
                     && timeSpan.getDietParameters().size() == 0)
             {
                 getErrors()
                         .add(String
                                 .format("Keine Zielparameter zum Zeitraum '%s' hinzugefügt!",
                                         timeSpan.getDisplayText()));
 
             }
             planParameterCount += timeSpanParameterCount;
         }
 
         if (_dietPlan.getTimeSpans().size() == 0)
         {
             getErrors().add(
                     String.format(
                             "Keine Zeiträume im Diätplan '%s' vorhanden!",
                             _dietPlan.getDisplayText()));
 
         }
         else if (isDietParameterOnlyPlan() && planParameterCount == 0
                 && _dietPlan.getDietParameters().size() == 0)
         {
             getErrors()
                     .add(String
                             .format("Keine Zielparameter zum Diätplan '%s' hinzugefügt!",
                                     _dietPlan.getDisplayText()));
 
         }
     }
 
     /**
      * Validate all timespans
      */
     private void validateTimeSpans()
     {
         for (TimeSpanBO timeSpan : _dietPlan.getTimeSpans())
         {
             validateTimeSpan(timeSpan);
         }
     }
 
     /**
      * Validate if {@link DietParameterBO}s are conflicting
      */
     private void validateDietParameterConflicts()
     {
         List<IDietParameterizable> conflicts = ParameterTemplateValidator
                 .getInstance().getConflictingComponents();
         for (IDietParameterizable component : conflicts)
         {
             getErrors().add(
                     "Parameterkonflikt in: " + component.getDisplayText());
         }
     }
 
     /**
      * Validate if the {@link DietPlanBO} corresponds to it's
      * {@link DietParameterBO}s
      */
     private void validateDietPlanParameters()
     {
         List<ValidationResult> violations = getRootProvider().getDietParameterController().validateDietPlanDietParameters(_dietPlan);
 
         for (ValidationResult validationResult : violations)
         {
 
             String error = String
                     .format("Zielparameter '%s' von '%s' wird nicht eingehalten: %.2f%s ist %s %s%s",
                             validationResult.getDietParameter()
                                     .getParameterDefinition().getName(),
                             validationResult.getAffectedObject()
                                     .getDisplayText(), validationResult
                                     .getCurrentValue(), validationResult
                                     .getDietParameter()
                                     .getParameterDefinitionUnit().getName(),
                             validationResult.getErrorType().getDisplayText(),
                             validationResult.getDietParameter().getValue(),
                             validationResult.getDietParameter()
                                     .getParameterDefinitionUnit().getName());
 
             _errors.add(error);
         }
     }
 
     /**
      * Validate if the {@link TimeSpanBO} collides with another
      * {@link TimeSpanBO}, {@link DietPlanBO} or {@link DietTreatmentBO}
      * 
      * @param t
      *            {@link TimeSpanBO} to validate
      */
     private void validateTimeSpan(TimeSpanBO t)
     {
         // check for timespan collisions
         List<Object> timeSpanCollisions = getRootProvider().getTimeSpanController()
                 .validateCollisions(t);
 
         // generate error messages
         for (Object object : timeSpanCollisions)
         {
             if (TimeSpanBO.class.isAssignableFrom(object.getClass()))
             {
                 _errors.add(String
                         .format("Der Zeitraum '%s' überschneidet sich mit dem Zeitraum '%s'",
                                 t.getDisplayText(),
                                 ((TimeSpanBO) object).getDisplayText()));
             }
             else if (DietPlanBO.class.isAssignableFrom(object.getClass()))
             {
                 _errors.add(String
                         .format("Der Zeitraum '%s' überschneidet sich mit dem Diätplan '%s'",
                                 t.getDisplayText(),
                                 ((DietPlanBO) object).getName()));
             }
             else if (DietTreatmentBO.class.isAssignableFrom(object.getClass()))
             {
                 _errors.add(String
                         .format("Der Zeitraum '%s' überschneidet sich mit der Diätbehandlung '%s'",
                                 t.getDisplayText(),
                                 ((DietTreatmentBO) object).getName()));
             }
         }
     }
 
     /** 
      * Initializes a new instance of the {@link DietPlanEditingController} class. 
      * @param currentProvider
      */
     protected DietPlanEditingController(BusinessLogicProvider currentProvider)
     {
         super(currentProvider);
         _errors = new ArrayList<String>();
     }
 
     /**
      * Reload the {@link DietPlanBO} caches
      */
     public void refresh()
     {
         refresh(true);
     }
 
     /**
      * Reload the {@link DietPlanBO} caches
      * 
      * @param refreshDietPlan
      *            Set to true if you want to reload the {@link DietPlanBO} from
      *            the database
      */
     public void refresh(boolean refreshDietPlan)
     {
         if (refreshDietPlan && _dietPlan != null
                 && _dietPlan.getDietPlanId() > 0)
         {
             DietPlanDAO dao = DAOFactory.getInstance().getDietPlanDAO();
             dao.refresh(_dietPlan.getModel());
         }
 
         MealDAO mealDao = DAOFactory.getInstance().getMealDAO();
         _mealCodes = mealDao.findCodes();
         _mealNames = mealDao.findNames();
     }
 
     /**
      * Get all meal codes
      * 
      * @return List of all meal codes
      */
     public Collection<String> getMealCodes()
     {
         return _mealCodes;
     }
 
     /**
      * Get all meal names
      * 
      * @return List of all meal names
      */
     public Collection<String> getMealNames()
     {
         return _mealNames;
     }
 
     /**
      * Get all error messages
      * 
      * @return List of all error messages
      */
     public List<String> getErrors()
     {
         return _errors;
     }
 
     /**
      * Delete a {@link TimeSpanBO} from the {@link DietPlanBO}
      * 
      * @param timeSpan
      *            {@link TimeSpanBO} to remove
      */
     public void deleteTimeSpan(TimeSpanBO timeSpan)
     {
         timeSpan.getDietPlan().removeTimeSpans(timeSpan);
         validateDietPlan();
     }
 
     /**
      * Delete a {@link MealBO} from a {@link TimeSpanBO}
      * 
      * @param meal
      *            The {@link MealBO} to remove
      */
     public void deleteMeal(MealBO meal)
     {
         meal.getTimeSpan().removeMeals(meal);
         validateDietPlan();
     }
 }
