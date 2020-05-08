 package models.management;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import play.db.ebean.Model.Finder;
 import play.mvc.Call;
 
 import com.avaje.ebean.ExpressionList;
 import com.avaje.ebean.Page;
 
 /**
  * Abstract class for every manager that contains the functionality for CRUD operations
  * on an entity.
  *
  * @author Ruben Taelman
  * @author Kevin Stobbelaar
  *
  */
 public abstract class Manager<T extends ManageableModel> {
 
     private Finder<String, T> finder;
 
     protected int pageSize;
     protected String orderBy;
     protected String order;
     protected String filterBy;
     protected String filter;
 
     public static final int DEFAULTPAGESIZE = 10;
     public static final String DEFAULTORDER = "asc";
 
     protected Map<String, FieldType> fields = new LinkedHashMap<String, FieldType>();
     protected Map<String, Class<?>> fieldTypes = new HashMap<String, Class<?>>();
     protected Map<String, Boolean> disabledFields = new HashMap<String, Boolean>();
     protected Map<String, Boolean> hiddenListFields = new HashMap<String, Boolean>();
 
     private boolean ignoreErrors = false;
     private ModelState state;
    private boolean hasActions = true;
 
     /**
      * Constructor for manager.
      *
      * @param modelClass The class object for the ManageableModel class
      * @param state The state this manager should be in
      * @param orderBy The column name the rows should be ordered by
      * @param filterBy The column name the rows should be filtered by
      */
     public Manager(Class<T> modelClass, ModelState state, String orderBy, String filterBy){
         this.finder = new Finder<String, T>(String.class, modelClass);
         this.pageSize = DEFAULTPAGESIZE;
         this.order = DEFAULTORDER;
         this.state = state;
         this.orderBy = orderBy;
         this.filterBy = filterBy;
 
         // Add the necessary fields to the fields-map
         for(Field field : modelClass.getDeclaredFields()) {
             if(field.isAnnotationPresent(Editable.class)) {
                 fields.put(field.getName(), FieldType.getType(field.getType()));
                 fieldTypes.put(field.getName(), field.getType());
 
                 if(field.getAnnotation(Editable.class).uponCreation()
                         || field.getAnnotation(Editable.class).alwaysHidden()
                   )
                     this.disableField(field.getName());
                 if(field.getAnnotation(Editable.class).hiddenInList())
                     this.disableListField(field.getName());
             }
         }
     }
 
     public void setIgnoreErrors(boolean ignoreErrors) {
         this.ignoreErrors = ignoreErrors;
     }
 
     /**
      * Check if the errors should be ignored in a form view
      * @return if the errors should be ignored
      */
     public boolean isIgnoreErrors() {
         return this.ignoreErrors;
     }
 
     /**
      * Set the pagesize for pagination
      * @param pageSize new pagesize
      */
     public void setPageSize(int pageSize) {
         this.pageSize = pageSize;
     }
 
     /**
      * Set the default field on which the sorting should happen
      * @param orderBy order field
      */
     public void setOrderBy(String orderBy) {
         this.orderBy = orderBy;
     }
 
     /**
      * Set the sorting order
      * @param order "asc" or "desc"
      */
     public void setOrder(String order) {
         this.order = order;
     }
 
     /**
      * Set the default field on which the filtering should happen
      * @param filterBy filter field
      */
     public void setFilterBy(String filterBy) {
         this.filterBy = filterBy;
     }
 
     /**
      * Set the filter value
      * @param filter the value to filter on
      */
     public void setFilter(String filter) {
         this.filter = filter;
     }
 
     /**
      * Returns the field to filter by
      * @return the field to filter by depending on the filter input
      */
     public String getFilterBy() {
         return this.filterBy;
     }
 
     /**
      * Returns a page with elements of type T.
      * Overriding is advised.
      *
      * @param page     page number
      * @return the requested page
      */
     public Page<T> page(int page) {
         return getDataSet()
             .ilike(filterBy, "%" + filter + "%")
             .orderBy(orderBy + " " + order)
             .findPagingList(pageSize)
             .getPage(page);
     }
     
     /**
      * 
      * @return The Dataset the Manager is working
      */
     protected ExpressionList<T> getDataSet(){
     	return getFinder().where();
     }
 
     /**
      * Returns the finder object for this manager.
      *
      * @return finder
      */
     public Finder<String, T> getFinder(){
         return finder;
     }
 
     /**
      * Returns the number of elements per page.
      * @return page size
      */
     public int getPageSize(){
         return pageSize;
     }
 
     /**
      * Returns the column headers for the objects of type T. This array must agree with
      * getFieldValues() from the ManageableModel
      *
      * @return column headers
      */
     public List<String> getColumnHeaders() {
         List<String> headers = new ArrayList<String>();
         for(String key : fields.keySet()) {
             if(this.isFieldListVisible(key)) headers.add(key);
         }
         return headers;
     }
 
     /**
      * Returns the route that must be followed to refresh the list.
      *
      * @param page     current page number
      * @param orderBy  current order by
      * @param order    current order
      * @param filter   filter on the items
      * @return Call Route that must be followed
      */
     public abstract Call getListRoute(int page, String orderBy, String order, String filter);
 
     /**
      * Returns the route that must be followed to refresh the list with default parameters
      * @return Call Route that must be followed
      */
     public Call getListRoute() {
         return getListRoute(0, orderBy, DEFAULTORDER, "");
     }
 
     /**
      * Returns the path of the route that must be followed to create a new item.
      *
      * @return Call path of the route that must be followed
      */
     public Call getAddRoute(){
         return null;
     }
 
     /**
      * Returns the path of the route that must be followed to edit the selected item.
      *
      * @return Call path of the route that must be followed
      */
     public Call getEditRoute(String id){
         return null;
     }
 
     /**
      * Returns the path of the route that must be followed to remove the selected item.
      *
      * @return Call path of the route that must be followed
      */
     public Call getRemoveRoute(String id){
         return null;
     }
 
     /**
      * Returns the path of the route that must be followed to save the current item.
      * @return Call path of the route that must be followed
      */
     public play.api.mvc.Call getSaveRoute(){
         return null;
     }
 
     /**
      * Returns the path of the route that must be followed to update(save) the current item.
      * @return Call path of the route that must be followed
      */
     public play.api.mvc.Call getUpdateRoute(){
         return null;
     }
 
     /**
      * Returns true is this manager has implemented action routes.
      * @return true if this manager has implemented action routes.
      */
     public boolean hasActions(){
        return hasActions;
    }

    /**
     * Sets the hasActions field.
     * @param hasActions hasActions
     */
    public void setHasActions(boolean hasActions){
        this.hasActions = hasActions;
     }
 
     /**
      * The field names this manager will show
      * @return set with field names
      */
     public Iterator<String> getFieldNames() {
         // A set is required to keep the original order after conversion to a scala collection
         return fields.keySet().iterator();
     }
 
     /**
      * Disabling a field will make them uneditable in the form
      * @param field field name
      */
     private void disableField(String field) {
         disabledFields.put(field, true);
     }
     
     /**
      * Make a field hidden in the listview
      * @param field field name
      */
     private void disableListField(String field) {
         hiddenListFields.put(field, true);
     }
 
     /**
      * Check if a field is disabled
      * @param field field name
      * @return if the field is disabled
      */
     public boolean isFieldDisabled(String field) {
         Boolean val = disabledFields.get(field);
         if(val == null)                           return false;
         else if(!state.equals(ModelState.UPDATE)) return false;
         else                                      return val;
     }
     
     /**
      * Check if a field is visible in the list view
      * @param field field name
      * @return if the field is disabled in a listview
      */
     public boolean isFieldListVisible(String field) {
         Boolean val = hiddenListFields.get(field);
         if(val == null)                           return true;
         else                                      return !val;
     }
 
     /**
      * The fields this manager will show
      * @return map with field names and their type
      */
     public Map<String, FieldType> getFields() {
         return fields;
     }
 
     /**
      * Create an empty dummy object for a certain field from the ManageableModel.
      * @param field the name of the field
      * @return a new dummy object for the given field
      */
     public Object getDummyField(String field) {
         try {
             return fieldTypes.get(field).newInstance();
         } catch (InstantiationException ie) {
             return null;
         } catch (IllegalAccessException ile) {
             return null;
         } catch (NullPointerException e) {
             return null;
         }
     }
 
     /**
      * Returns the prefix for translation messages.
      *
      * @return name
      */
     public abstract String getMessagesPrefix();
 }
