 package controllers;
 
 import controllers.logic.SurveyJsonTransformer;
 import flexjson.TypeContext;
 import flexjson.transformer.AbstractTransformer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author damian.janicki
  */
 public class ConstraintJsonTransformer extends AbstractTransformer {
 
     String constraintMax = "";
     String constraintMin = "";
     String constraintString  = null;
 
    public static final String MIN_REGEX = ".\\s*>\\s* \\d+(/\\d{1,2}/\\d{4})?";
    public static final String MAX_REGEX = ".\\s*<\\s* \\d+(/\\d{1,2}/\\d{4})?";

 
     public void transform( Object o ) {
         if(o == null){
             return;
         }
 
         constraintString = (String)o;
         constraintString.trim();
         if( constraintString == null || constraintString.isEmpty()){
             return;
         }
 
         TypeContext typeContext = getContext().peekTypeContext();
         if ( !typeContext.isFirst() ) {
             getContext().writeComma();
         }
         typeContext.setFirst(false);
 
         parseConstraintString();
 
         getContext().writeName( SurveyJsonTransformer.CONSTRAINT_MAX );
         getContext().writeQuoted( constraintMax );
 
         getContext().writeComma();
 
         getContext().writeName( SurveyJsonTransformer.CONSTRAINT_MIN );
         getContext().writeQuoted( constraintMin );
 
     }
 
     public void parseConstraintString(){
 
         Pattern patternMin = Pattern.compile( MIN_REGEX );
         Matcher matcherMin = patternMin.matcher( constraintString );
 
         Pattern patternMax = Pattern.compile( MAX_REGEX );
         Matcher matcherMax = patternMax.matcher( constraintString );
 
 
         int index;
         if( ( index = constraintString.indexOf( SurveyJsonTransformer.STRING_LENGTH_CONSTRAINT )) != -1 ){
             constraintMax = constraintString.substring(
                         index + SurveyJsonTransformer.STRING_LENGTH_CONSTRAINT.length() ).trim();
             return;
         }
 
         if( matcherMin.find() ){
             int start = matcherMin.start();
             int stop = matcherMin.end();
             String minString = constraintString.substring( start, stop );
             constraintMin = parseMinMax( minString );
         }
 
         if( matcherMax.find() ){
             int start = matcherMax.start();
             int stop = matcherMax.end();
             String maxString = constraintString.substring( start, stop );
             constraintMax = parseMinMax( maxString );
         }
     }
 
     public String parseMinMax(String expr){
         int i = -1;
         if( expr.contains( "<" ) ){
             i = expr.indexOf( "<" );
         }else if( expr.contains( ">" ) ){
             i = expr.indexOf( ">" );
         }
 
         return expr.substring( i + 1 ).trim();
     }
 
     @Override
     public Boolean isInline() {
         return Boolean.TRUE;
     }
 
 }
