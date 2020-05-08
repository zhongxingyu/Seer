 package handlers;
 
 import assistant.Constants;
 import assistant.Serializer;
 import assistant.SharedHandler;
 import assistant.pair.NullableExtendedParam;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
 import models.*;
 import models.Thread;
 import org.hibernate.collection.internal.PersistentSet;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: OMMatte
  * Date: 2013-03-13
  * Time: 11:57
  */
 public class CategoryRequestHandler extends SharedHandler {
 
     public CategoryRequestHandler() {
         super();
     }
 
     @Override
     public String[] handledRequests() {
         return new String[]{
             Constants.Method.GET_FORUM_CONTENT,
             Constants.Method.GET_CATEGORY_CONTENT,
             Constants.Method.CREATE_CATEGORY,
             Constants.Method.MODIFY_CATEGORY,
             Constants.Method.DELETE_CATEGORY,
         };
     }
 
     @Override
     public void process(Map<String, Object> jsonrpc2Params) throws Exception {
         if (method.equals(Constants.Method.GET_FORUM_CONTENT)) {
             getForumContent(jsonrpc2Params);
         } else if (method.equals(Constants.Method.GET_CATEGORY_CONTENT)) {
             getCategoryContent(jsonrpc2Params);
         } else if (method.equals(Constants.Method.CREATE_CATEGORY)) {
             createCategory(jsonrpc2Params);
         }else if (method.equals(Constants.Method.MODIFY_CATEGORY)) {
             modifyCategory(jsonrpc2Params);
         }  else if (method.equals(Constants.Method.DELETE_CATEGORY)) {
             deleteCategory(jsonrpc2Params);
         }
         else {
             throwJSONRPC2Error(JSONRPC2Error.METHOD_NOT_FOUND, Constants.Errors.METHOD_NOT_FOUND, method);
         }
     }
 
     /**
      * Returns the categories by the given forum type.
      *
      * @param jsonrpc2Params
      * @throws Exception
      */
     private void getForumContent(Map<String, Object> jsonrpc2Params) throws Exception {
         Map<String, Object> params = getParams(jsonrpc2Params,
                 Constants.Param.Name.TYPE);
 
         String type = (String) params.get(Constants.Param.Name.TYPE);
         List<Category> categories = null;
 
         if (type.equals(Constants.Param.Value.PUBLIC)) {
             categories = getAllColumns(Category.class);
         } else if (type.equals(Constants.Param.Value.PRIVATE)) {
 
             categories = getAllColumns(Category.class);
 
         } else {
             throwJSONRPC2Error(JSONRPC2Error.INVALID_PARAMS, Constants.Errors.PARAM_VALUE_NOT_ALLOWED, "Type: " + type);
         }
         responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
         responseParams.put(Constants.Param.Name.CATEGORIES, Serializer.serialize((Serializable) categories));
     }
 
     /**
      * Returns a list of threads by the given categoryId.
      *
      * @param jsonrpc2Params
      * @throws Exception
      */
     private void getCategoryContent(Map<String, Object> jsonrpc2Params) throws Exception {
 
         Map<String, Object> params = getParams(jsonrpc2Params,
                 new NullableExtendedParam(Constants.Param.Name.CATEGORY_ID, false));
 
         int categoryId = (Integer) params.get(Constants.Param.Name.CATEGORY_ID);
 
 
         Category category = em.find(Category.class, categoryId);
         if (category == null) {
             responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.OBJECT_NOT_FOUND);
             return;
         }
 
        Collection<Thread> threadList = new LinkedList<Thread>(category.getThreads());
 
         responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
         responseParams.put(Constants.Param.Name.THREADS, Serializer.serialize((Serializable) threadList));
     }
 
     /**
      * Creates a new category with id and headline and returns the object.
      *
      * @param jsonrpc2Params
      * @throws Exception
      */
     private void createCategory(Map<String, Object> jsonrpc2Params) throws Exception {
 
         Map<String, Object> params = getParams(jsonrpc2Params,
                 new NullableExtendedParam(Constants.Param.Name.TYPE, false),
                 new NullableExtendedParam(Constants.Param.Name.HEADLINE, false),
                 new NullableExtendedParam(Constants.Param.Name.COLOR_CODE, true));
 
         String type = (String) params.get(Constants.Param.Name.TYPE);
         Integer colorCode = (Integer)params.get(Constants.Param.Name.COLOR_CODE);
         Object category = null;
 
         if(colorCode == null){
             colorCode = Constants.Database.DEFAULT_COLOR_CODE;
         }
 
         if(type.equals(Constants.Param.Value.PUBLIC)){
             category = new Category(
                     (String) params.get(Constants.Param.Name.HEADLINE),
                     colorCode,
                     null);
 
         }else if(type.equals(Constants.Param.Value.PRIVATE)){
             category = new PrivateCategory(
                     (String) params.get(Constants.Param.Name.HEADLINE),
                     colorCode,
                     null,
                     em.find(Account.class, accountId));
 
         }else{
             throwJSONRPC2Error(JSONRPC2Error.INVALID_PARAMS, Constants.Errors.PARAM_VALUE_NOT_ALLOWED, "Type: " + type);
         }
         persistObjects(category);
 
         responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
         responseParams.put(Constants.Param.Name.CATEGORY, Serializer.serialize((Serializable) category));
     }
 
 
     /**
      * Modify an existing category's headline by id and return the new version of the object.
      *
      * @param jsonrpc2Params
      * @throws Exception
      */
     private void modifyCategory(Map<String, Object> jsonrpc2Params) throws Exception {
 
         Map<String, Object> params = getParams(jsonrpc2Params,
                 new NullableExtendedParam(Constants.Param.Name.TYPE, false),
                 new NullableExtendedParam(Constants.Param.Name.CATEGORY_ID, false),
                 new NullableExtendedParam(Constants.Param.Name.HEADLINE, true),
                 new NullableExtendedParam(Constants.Param.Name.COLOR_CODE, true));
 
         String type = (String) params.get(Constants.Param.Name.TYPE);
         int categoryId = (Integer) params.get(Constants.Param.Name.CATEGORY_ID);
         String headLine = (String) params.get(Constants.Param.Name.HEADLINE);
         Integer colorCode = (Integer) params.get(Constants.Param.Name.COLOR_CODE);
 
 
         //TODO: Private and public categories should extend each other somehow so we do not need duplicate code.
         if(type.equals(Constants.Param.Value.PUBLIC)){
             Category category = em.find(Category.class, params.get(categoryId));
             if(category == null){
                 responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.OBJECT_NOT_FOUND);
                 return;
             }
             if(headLine != null){
                 category.setHeadLine(headLine);
             }
             if(colorCode != null){
                 category.setColorCode(colorCode);
             }
             persistObjects(category);
             responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
             responseParams.put(Constants.Param.Name.CATEGORY, Serializer.serialize((Serializable) category));
 
         }else if(type.equals(Constants.Param.Value.PRIVATE)){
             PrivateCategory category = em.find(PrivateCategory.class, params.get(categoryId));
             if(category == null){
                 responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.OBJECT_NOT_FOUND);
                 return;
             }
             if(headLine != null){
                 category.setHeadLine(headLine);
             }
             if(colorCode != null){
                 category.setColorCode(colorCode);
             }
             persistObjects(category);
             responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
             responseParams.put(Constants.Param.Name.CATEGORY, Serializer.serialize((Serializable) category));
 
         }else{
             throwJSONRPC2Error(JSONRPC2Error.INVALID_PARAMS, Constants.Errors.PARAM_VALUE_NOT_ALLOWED, "Type: " + type);
         }
     }
 
     /**
      * Delete a category.
      *
      * @param jsonrpc2Params
      * @throws Exception
      */
     private void deleteCategory(Map<String, Object> jsonrpc2Params) throws Exception {
 
         Map<String, Object> params = getParams(jsonrpc2Params,
                 Constants.Param.Name.TYPE,
                 Constants.Param.Name.CATEGORY_ID);
 
         String type = (String) params.get(Constants.Param.Name.TYPE);
         int categoryId = (Integer) params.get(Constants.Param.Name.CATEGORY_ID);
         Object category = null;
 
         if(type.equals(Constants.Param.Value.PUBLIC)){
             category = em.find(Category.class, categoryId);
         } else if(type.equals(Constants.Param.Value.PRIVATE)){
             category = em.find(PrivateCategory.class, categoryId);
         }else{
             throwJSONRPC2Error(JSONRPC2Error.INVALID_PARAMS, Constants.Errors.PARAM_VALUE_NOT_ALLOWED, "Type: " + type);
         }
         if(category == null){
             responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.OBJECT_NOT_FOUND);
         }else{
             removeObjects(category);
             responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
         }
     }
 }
