 package controllers.deserializer;
 
 import controllers.logic.SurveyJsonTransformer;
 import flexjson.ObjectBinder;
 import flexjson.ObjectFactory;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.HashMap;
 import models.Category;
 import models.Question;
 import models.QuestionOption;
 import models.QuestionType;
 import models.constants.QuestionTypesConsts;
 import play.cache.Cache;
 
 /**
  *
  * @author damian.janicki
  */
 public class QuestionObjectFactory implements ObjectFactory{
 
 
     public static final String MIN_CONSTR_STR = ". > ";
     public static final String MAX_CONSTR_STR = ". < ";
 
     public Object instantiate( ObjectBinder ob, Object o, Type type, Class type1 ) {
         Question question = null;
         Category currentCategory = (Category)Cache.get( "currentCategory" );
 
         HashMap map = ( HashMap )o;
         if( map.containsKey( "id" ) ){
             Long lId = new Long( ( Integer ) map.get( "id" ) );
             question = Question.findById( lId );
 
             if( map.containsKey( "isDelete" ) ){//delete question
                 question.delete();
                 question = null;
             }else{ //edit question
                 bind( ob, map, question );
                 addConstraintString( map, question );
 
                 if( !currentCategory.questionCollection.contains( question ) ){ // move to other category
                     question.category.questionCollection.remove( question );
                     currentCategory.questionCollection.add( question );
                     question.category = currentCategory;
                 }
                 question.save();
             }
 
         }else if( !map.containsKey( "id" ) && !map.containsKey( "isDelete" ) ){ //add new question
             question = createNewQuestion();
 
             currentCategory.questionCollection.add( question );
             question.category = currentCategory;
             question.save();
 
             bind( ob, map, question );
             addConstraintString( map, question );
             question.save();
         }
         return question;
     }
 
     private void addConstraintString(HashMap map, Question question){
         String constraintString = "";
         String minValue = (String) map.get( SurveyJsonTransformer.CONSTRAINT_MIN );
         String maxValue = (String) map.get( SurveyJsonTransformer.CONSTRAINT_MAX );
 
         if(question.questionType.typeName.equals( QuestionTypesConsts.STRING ) &&
                 maxValue != null && !maxValue.isEmpty()){
             question.constraintText = SurveyJsonTransformer.STRING_LENGTH_CONSTRAINT + maxValue;
             return;
         }
 
         if( minValue != null && !minValue.isEmpty() ){
             constraintString += MIN_CONSTR_STR + minValue;
         }
 
         if( maxValue != null && !maxValue.isEmpty() ){
 
             if( !constraintString.isEmpty() ){
                 constraintString += " and";
             }
             constraintString += MAX_CONSTR_STR + maxValue;
         }
 
         if( !constraintString.isEmpty() ){
             question.constraintText = "(" + constraintString + ")";
         }
     }
 
     private void bind(ObjectBinder ob, HashMap map, Question question){
             Cache.add( "currentQuestion", question );
             ob.bindIntoObject( map, question, Question.class );
             Cache.delete( "currentQuestion" );
     }
 
     private Question createNewQuestion (){//TODO move to question class
             Question question = new Question();
 
             question.questionOptionCollection = new ArrayList<QuestionOption>();
             question.label = "";
             question.objectName = "";
             question.questionType = (QuestionType)QuestionType.all().fetch().get( 0 );
             question.required = 0;
             question.readonly = 0;
 
             return question;
     }
 }
 
