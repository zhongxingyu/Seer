 package controllers;
 
 import controllers.exceptions.SurveySavingException;
 import controllers.logic.SurveyPersister;
 import flexjson.JSONDeserializer;
 import flexjson.ObjectBinder;
 import java.lang.reflect.Type;
 import java.util.List;
 import play.mvc.Controller;
 import flexjson.JSONSerializer;
 import flexjson.ObjectFactory;
 import flexjson.transformer.AbstractTransformer;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import models.Category;
 import models.NdgResult;
 import models.NdgUser;
 import models.Question;
 import models.QuestionType;
 import models.Survey;
 import models.TransactionLog;
 import models.UserRole;
 import models.constants.TransactionlogConsts;
 import models.utils.NdgQuery;
 import models.utils.SurveyDuplicator;
 
 
 
 public class Application extends Controller {
 
     private static final int RESULTS_PER_SIDE = 10;
 
     public static void index() {
         render();
     }
 
     static class QuestionTypeObjectFactory implements ObjectFactory{
         public Object instantiate(ObjectBinder ob, Object o, Type type, Class type1) {
             QuestionType qType = null;
             HashMap map = (HashMap)o;
             if(map.containsKey("type")){
                 String typeId = (String)map.get("type");
                 qType = QuestionType.findById(Long.decode(typeId));
             }
             return qType;
         }
     }
 
     public static void saveSurvey(String id, String surveyData){
 
 
         JSONDeserializer<ArrayList<Category>> deserializer = new JSONDeserializer<ArrayList<Category>>();
         deserializer
                 .use("values", Category.class)
                 .use("values.questionCollection", ArrayList.class)
                 .use("values.questionCollection.values", Question.class)
                 .use("values.questionCollection.values.questionType", new QuestionTypeObjectFactory());
         ArrayList<Category> categoryList = deserializer.deserialize(surveyData, ArrayList.class);
 
 
         Survey survey = Survey.findById( Long.decode( id ) );
 
         for(Category cat : survey.categoryCollection){
             cat.delete();
         }
 
         for(Category cat : categoryList){
             for(Question q : cat.questionCollection){
                 q.category = cat;
             }
             cat.survey = survey;
             survey.categoryCollection.add(cat);
             cat.save();
         }
 
     }
 
     public static void categoryList( int surveyId){
         String query = "survey_id = " + String.valueOf( surveyId ) + " order by categoryIndex";
         List<Category> categories = Category.find(query).fetch();
         JSONSerializer categoryListSerializer = new JSONSerializer();
         categoryListSerializer.transform( new NdgResultCollectionTransformer(), "resultCollection" );
         categoryListSerializer.include( "id","categoryIndex", "questionCollection", "label" )
             .rootName( "categories" );
         renderJSON(categoryListSerializer.serialize(categories));
     }
 
     public static void listResults( int surveyId, int startIndex, boolean isAscending, String orderBy ) {
         String query = "survey_id = "+ String.valueOf( surveyId ) + " order by "+ orderBy + ( isAscending ? " asc" : " desc" );
 
         List<NdgResult> results = NdgResult.find( query ).from( RESULTS_PER_SIDE * startIndex ).fetch( RESULTS_PER_SIDE );
         JSONSerializer surveyListSerializer = new JSONSerializer();
         surveyListSerializer.transform( new NdgResultCollectionTransformer(), "resultCollection" );
         surveyListSerializer.include( "id","resultId", "title", "startTime", "ndgUser.username", "latitude" )
             .exclude( "*" ).rootName( "items" );
         renderJSON( surveyListSerializer.serialize( results ) );
     }
 
     public static void listSurveysCount() {
         long surveys =  Survey.count();
         JSONSerializer surveysCountSerializer = new JSONSerializer();
         surveysCountSerializer.include( "*" ).rootName( "itemsCount" );
         renderJSON( surveysCountSerializer.serialize( pages( surveys ) ) );
     }
 
     public static void listUsersCount() {
         long users = NdgUser.count();
         JSONSerializer userCountSerializer = new JSONSerializer();
         userCountSerializer.include( "*" ).rootName( "itemsCount" );
         renderJSON( userCountSerializer.serialize( pages( users ) ) );
     }
 
     public static void listResultsCount( int surveyId ) {
         long results =  NdgResult.find( "survey_id", String.valueOf( surveyId ) ).fetch().size();
         JSONSerializer resultsCountSerializer = new JSONSerializer();
         resultsCountSerializer.include( "*" ).rootName( "itemsCount" );
         renderJSON( resultsCountSerializer.serialize( pages( results) ) );
     }
 
     public static void listSurveys( int startIndex, boolean isAscending, String orderBy ) {
         List<Survey> surveys = null;
         if(orderBy != null && orderBy.equals("resultCollection"))
         {
             surveys =  Survey.all().fetch();
             Collections.sort( surveys, new SurveyNdgResultCollectionComapator() );
             if( !isAscending ) {
                 Collections.reverse( surveys );
             }
 
             int subListEndIndex = startIndex * RESULTS_PER_SIDE + RESULTS_PER_SIDE < surveys.size() ?
                                                     startIndex * RESULTS_PER_SIDE + RESULTS_PER_SIDE :
                                                     surveys.size();
             surveys = surveys.subList( startIndex * RESULTS_PER_SIDE , subListEndIndex );
         } else
         {
             String query = "order by "+ orderBy + ( isAscending ? " asc" : " desc" );
             surveys =  Survey.find( query ).from( RESULTS_PER_SIDE * startIndex ).fetch( RESULTS_PER_SIDE );
         }
         serializeSurveys( surveys );
     }
 
     public static void listUsers(int startIndex, boolean isAscending, String orderBy ) {
         List<NdgUser> users = null;
         if(orderBy != null && orderBy.equals("userRoleCollection"))
         {
             users =  NdgUser.find("order by username asc").fetch();
             Collections.sort( users, new NdgUserUserRoleCollectionComapator(isAscending) );
             int subListEndIndex = startIndex * RESULTS_PER_SIDE + RESULTS_PER_SIDE < users.size() ?
                                                 startIndex * RESULTS_PER_SIDE + RESULTS_PER_SIDE :
                                                 users.size();
             users = users.subList( startIndex * RESULTS_PER_SIDE , subListEndIndex);
         } else {
             String query = "order by "+ orderBy + ( isAscending ? " asc" : " desc" );
             users =  NdgUser.find( query ).from( RESULTS_PER_SIDE * startIndex ).fetch( RESULTS_PER_SIDE );
         }
         serializeUsers(users);
     }
 
     private static void serializeUsers(List<NdgUser> users) {
         JSONSerializer userListSerializer = new JSONSerializer();
         userListSerializer.transform(new NdgResultCollectionTransformer() , "resultCollection");
         userListSerializer.include("id","username", "phoneNumber", "email", "userRoleCollection.ndgRole.roleName" ).exclude("*").rootName("items");
         renderJSON(userListSerializer.serialize(users));
     }
 
     private static void serializeSurveys( List<Survey> subList ) {
         JSONSerializer surveyListSerializer = new JSONSerializer();
         surveyListSerializer.transform( new NdgResultCollectionTransformer(), "resultCollection" );
         surveyListSerializer.include( "id", "title", "uploadDate", "idUser", "surveyId", "ndgUser.username", "resultCollection", "available" )
             .exclude( "*" ).rootName( "items" );
         renderJSON( surveyListSerializer.serialize( subList ) );
     }
 
     private static long pages( long entries ) {
         long pages = entries/RESULTS_PER_SIDE + ( entries%RESULTS_PER_SIDE > 0 ? 1 : 0 );
         return pages == 0 ? 1 : pages;
     }
 
     public static class SurveyNdgResultCollectionComapator implements Comparator<Survey> {
 
         public int compare( Survey o1, Survey o2 ) {
             return o1.resultCollection.size() < o2.resultCollection.size() ? 1 : -1;
         }
     }
 
         private static class NdgUserUserRoleCollectionComapator implements Comparator<NdgUser> {
 
         private boolean  isAscending = true;
 
         NdgUserUserRoleCollectionComapator(boolean  isAscending)
         {
             this.isAscending = isAscending;
         }
 
         public int compare( NdgUser o1, NdgUser o2) {
             int retval = 0;
             UserRole role1 = (UserRole) o1.userRoleCollection.toArray()[0];
             UserRole role2 = (UserRole) o2.userRoleCollection.toArray()[0];
             retval = role1.ndgRole.id.compareTo(role2.ndgRole.id);
             return isAscending ?  retval : -retval;
         }
     }
 
     private static class NdgResultCollectionTransformer extends AbstractTransformer {
 
         public void transform( Object collection ) {
             Collection ndgResultCollection = (Collection)collection;
             getContext().write( String.valueOf( ndgResultCollection.size() ) );
         }
     }
 
     public static void upload(File filename, String uploadSurveyId) throws IOException, SurveySavingException {
         InputStreamReader is = null;
         String result = "failure";
         try {
             is = new InputStreamReader(new FileInputStream(filename), "UTF-8");
             SurveyPersister persister = new SurveyPersister(is);
             persister.saveSurvey(uploadSurveyId);
             result = "success";
         } finally {
             if (is != null) {
                 is.close();
             }
         }
         renderText(result);
     }
 
     public static void delete(String formID)
     {
         Survey deleted = Survey.find("bySurveyId", formID).first();
         deleted.delete();
     }
 
     public static void duplicate(String formID)
     {
         Survey origin = Survey.find("bySurveyId", formID).first();
 
         SecureRandom random = new SecureRandom();
         String newId = new BigInteger(40, random).toString(32);
 
         Survey copy = SurveyDuplicator.plainCopy(origin, newId);
 
         copy.save();
     }
 
     public static void sendSurveys(String formID, String users[])
     {
         for (int i = 0; i < users.length; i++) {
             TransactionLog transaction = new TransactionLog();
             transaction.transactionDate =new Date();
             transaction.transactionType = TransactionlogConsts.TransactionType.TYPE_SEND_SURVEY;
             transaction.transactionStatus = TransactionlogConsts.TransactionStatus.STATUS_AVAILABLE;
             transaction.transmissionMode = TransactionlogConsts.TransactionMode.MODE_HTTP;
 
             transaction.address = request.remoteAddress;
             transaction.ndgUser  = NdgQuery.getUsersbyId(Long.parseLong(users[i]));
             transaction.survey = NdgQuery.getSurveyById(formID);
 
             transaction.save();
         }
     }
 }
