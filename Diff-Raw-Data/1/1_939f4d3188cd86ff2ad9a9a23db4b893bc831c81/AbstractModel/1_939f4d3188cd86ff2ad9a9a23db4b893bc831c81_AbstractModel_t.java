 package de.aidger.model;
 
 import static de.aidger.utils.Translation._;
 
 import java.lang.reflect.InvocationTargetException;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Vector;
 
 import de.aidger.model.validators.DateRangeValidator;
 import de.aidger.model.validators.EmailValidator;
 import de.aidger.model.validators.InclusionValidator;
 import de.aidger.model.validators.PresenceValidator;
 import de.aidger.model.validators.Validator;
 import de.aidger.utils.Logger;
 import de.unistuttgart.iste.se.adohive.controller.AdoHiveController;
 import de.unistuttgart.iste.se.adohive.controller.IAdoHiveManager;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IAdoHiveModel;
 
 /**
  * AbstractModel contains all important database related functions which all
  * models need to contain. This includes getting instances of models and saving
  * or removing them.
  * 
  * @author Philipp Gildein
  */
 public abstract class AbstractModel<T> extends Observable implements
         IAdoHiveModel<T> {
 
     /**
      * The unique id of the model in the database.
      */
     protected int id = 0;
 
     /**
      * Determines if the model has been saved in the db yet.
      */
     protected boolean isNew = true;
 
     /**
      * Should the model first be removed before saveing. Needed for example for
      * HourlyWage which has several Primary Keys and needs to be removed when
      * edited.
      */
     protected boolean removeOnUpdate = false;
 
     /**
      * Used to cache the AdoHiveManagers after getting them the first time.
      */
     protected static Map<String, IAdoHiveManager> managers =
         new HashMap<String, IAdoHiveManager>();
 
     /**
      * Array containing all validators for that specific model.
      */
     protected List<Validator> validators = new Vector<Validator>();
 
     /**
      * Array containing errors if a validator fails.
      */
     protected List<String> errors = new Vector<String>();
 
     /**
      * Map of errors for specific fields.
      */
     protected Map<String, List<String>> fieldErrors = new HashMap<String, List<String>>();
 
     /**
      * Cloneable function inherited from IAdoHiveModel.
      * 
      * @return Clone of the model
      */
     @Override
     abstract public T clone();
 
     /**
      * Get all models from the database.
      * 
      * @return An array containing all found models or null
      */
     @SuppressWarnings("unchecked")
     public List getAll() throws AdoHiveException {
         return getManager().getAll();
     }
 
     /**
      * Get a specific model by specifying its unique id.
      * 
      * @param id
      *            The unique id of the model
      * @return The model if one was found or null
      */
     @SuppressWarnings("unchecked")
     public T getById(int id) throws AdoHiveException {
         return (T) getManager().getById(id);
     }
 
     /**
      * Get a specific model by specifying a set of keys.
      * 
      * @param o
      *            The set of keys specific to this model
      * @return The model if one was found or null
      */
     @SuppressWarnings("unchecked")
     public T getByKeys(Object... o) throws AdoHiveException {
         return (T) getManager().getByKeys(o);
     }
 
     /**
      * Get the number of models in the database.
      * 
      * @return The number of models
      * @throws AdoHiveException
      */
     public int size() throws AdoHiveException {
         return getManager().size();
     }
 
     /**
      * Returns true if no model has been saved into the database.
      * 
      * @return True if no model is in the database
      * @throws AdoHiveException
      */
     public boolean isEmpty() throws AdoHiveException {
         return getManager().isEmpty();
     }
 
     /**
      * Checks if the current instance exists in the database.
      * 
      * @return True if the instance exists
      * @throws AdoHiveException
      */
     public boolean isInDatabase() throws AdoHiveException {
         return getManager().contains(this);
     }
 
     /**
      * Deletes everything from the associated table.
      * 
      * @throws AdoHiveException
      */
     public void clearTable() throws AdoHiveException {
         getManager().clear();
         id = 0; // Reset
     }
 
     // TODO: Add get(index) method?
 
     /**
      * Save the current model to the database.
      * 
      * @return True if validation succeeds
      * @throws AdoHiveException
      */
     @SuppressWarnings("unchecked")
     public boolean save() throws AdoHiveException {
         if (!doValidate()) {
             return false;
         } else if (!errors.isEmpty()) {
             Logger.debug(_("The model was not saved because the error list is not empty."));
             return false;
         }
 
         /* Add or update model */
         IAdoHiveManager mgr = getManager();
         if (isNew) {
             mgr.add(this);
             isNew = false;
         } else if (removeOnUpdate) {
             remove();
             mgr.add(this);
            setNew(false);
         } else {
             mgr.update(this);
         }
         setChanged();
         notifyObservers();
 
         return true;
     }
 
     /**
      * Remove the current model from the database.
      * 
      * @throws AdoHiveException
      */
     @SuppressWarnings("unchecked")
     public void remove() throws AdoHiveException {
         if (!isNew) {
             getManager().remove(this);
             clearChanged();
             notifyObservers();
 
             setNew(true);
         }
     }
 
     /**
      * Get a list of all errors.
      * 
      * @return A list of errors
      */
     public List<String> getErrors() {
         return errors;
     }
 
     /**
      * Get a list of errors for a specific field.
      * 
      * @param field
      *            The field to get the errors for
      * @return A list of errors
      */
     public List<String> getErrorsFor(String field) {
         return fieldErrors.get(field);
     }
 
     /**
      * Add an error to the list,
      * 
      * @param error
      *            The error to add
      */
     public void addError(String error) {
         errors.add(error);
     }
 
     /**
      * Add an error for a specific field to the list.
      * 
      * @param field
      *            The field on which the error occured
      * @param error
      *            The error to add
      */
     public void addError(String field, String error) {
         error = field + " " + error;
         errors.add(error);
         if (fieldErrors.containsKey(field)) {
             fieldErrors.get(field).add(error);
         } else {
             List<String> list = new Vector<String>();
             list.add(error);
             fieldErrors.put(field, list);
         }
     }
 
     /**
      * Clear the error lists.
      */
     public void resetErrors() {
         errors.clear();
         fieldErrors.clear();
     }
 
     /**
      * Add a validator to the model.
      * 
      * @param valid
      *            The validator to add
      */
     public void addValidator(Validator valid) {
         validators.add(valid);
     }
 
     /**
      * Add a presence validator to the model.
      * 
      * @param members
      *            The name of the member variables to validate
      */
     public void validatePresenceOf(String[] members) {
         validators.add(new PresenceValidator(this, members));
     }
 
     /**
      * Add an email validator to the model.
      * 
      * @param member
      *            The name of the member variable to validate
      */
     public void validateEmailAddress(String member) {
         validators.add(new EmailValidator(this, new String[] { member }));
     }
 
     /**
      * Add an date range validator to the model.
      * 
      * @param from
      *            The starting date
      * @param to
      *            The end date
      */
     public void validateDateRange(String from, String to) {
         validators.add(new DateRangeValidator(this, from, to));
     }
 
     /**
      * Add an inclusion validator to the model.
      * 
      * @param members
      *            The name of the member variables to validate
      * @param inc
      *            The list to check for inclusion
      */
     public void validateInclusionOf(String[] members, String[] inc) {
         validators.add(new InclusionValidator(this, members, inc));
     }
 
     /**
      * Returns the unique id of the activity.
      * 
      * @return The unique id of the activity
      */
     @Override
     public int getId() {
         return id;
     }
 
     /**
      * Set the unique id of the assistant.
      *
      * <b>!!! THIS IS FOR INTERNAL ADOHIVE USAGE ONLY !!!</b>
      * 
      * @param id
      *            The unique id of the assistant
      */
     @Override
     public void setId(int id) {
         this.id = id;
     }
 
     /**
      * Set if the model is new and should be added to the database.
      * 
      * @param isnew
      *            Is the model new?
      */
     public void setNew(boolean isnew) {
         isNew = isnew;
         if (isNew) {
             setId(0);
         }
     }
 
     /**
      * Returns a string containing all informations stored in the model.
      * 
      * @return A string containing informations on the model
      */
     @Override
     public String toString() {
         String ret = getClass().getSimpleName() + " [" + "ID: " + getId()
                 + ", ";
         try {
             for (java.lang.reflect.Method m : getClass().getDeclaredMethods()) {
                 if (m.getName().startsWith("get")
                         && m.getParameterTypes().length == 0) {
                     ret += m.getName().substring(3) + ": ";
                     ret += m.invoke(this, new Object[0]) + ", ";
                 }
             }
             if (ret.endsWith(", ")) {
                 ret = ret.substring(0, ret.length() - 2);
             }
         } catch (InvocationTargetException ex) {
             System.err.println(ex.getMessage());
         } catch (IllegalArgumentException ex) {
             System.err.println(ex.getMessage());
         } catch (IllegalAccessException ex) {
             System.err.println(ex.getMessage());
         }
         return ret + "]";
     }
 
     /**
      * Extract the name of the class and return the correct manager.
      * 
      * @return The name of the model class
      */
     @SuppressWarnings("unchecked")
     protected IAdoHiveManager getManager() {
         String classname = getClass().getSimpleName();
         if (!managers.containsKey(classname) ||
                 managers.get(classname) == null) {
             /* Try to get the correct manager from the AdoHiveController */
             try {
                 java.lang.reflect.Method m = AdoHiveController.class
                         .getMethod("get" + classname + "Manager");
                 managers.put(classname, (IAdoHiveManager) m.invoke(
                         AdoHiveController.getInstance(), new Object[0]));
             } catch (Exception ex) {
                 Logger.error(MessageFormat.format(
                                 _("Could not get manager for class \"{0}\". Error: {1}"),
                                 new Object[] { classname, ex.getMessage() }));
             }
         }
 
         return managers.get(classname);
     }
 
     /**
      * Validate the input using the validators and a custom validate function.
      * 
      * @return True if everything validates
      */
     protected boolean doValidate() {
         /* Try to validate before adding/updating */
         boolean ret = true;
         for (Validator v : validators) {
             if (!v.validate()) {
                 ret = false;
             }
         }
 
         /* Check if the model got a validate() function */
         try {
             java.lang.reflect.Method m = getClass().getDeclaredMethod(
                     "validate");
             if (!(Boolean) m.invoke(this, new Object[0])) {
                 ret = false;
             }
         } catch (Exception ex) {
         }
 
         return ret;
     }
 }
