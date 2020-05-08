 /*
  *  Copyright (C) 2011  INdT - Instituto Nokia de Tecnologia
  *
  *  NDG is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  NDG is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with NDG.  If not, see <http://www.gnu.org/licenses/
  */
 package controllers.logic;
 
 import controllers.exceptions.ResultSaveException;
 import java.io.ByteArrayInputStream;
 import models.Answer;
import models.Category;
 import models.NdgResult;
 import java.io.Reader;
 import java.util.Hashtable;
 import models.NdgUser;
 import models.Question;
import models.Survey;
 import models.constants.QuestionTypesConsts;
 import org.javarosa.xform.parse.XFormParser;
 import org.joda.time.format.ISODateTimeFormat;
 import org.kxml2.kdom.Document;
 import org.kxml2.kdom.Element;
 import play.db.jpa.Blob;
 import play.libs.Codec;
 
 public class ResultParser {
 
     private static final String OPEN_ROSA_ROOT = "data";
     private static final String OPEN_ROSA_INSTANCE_ID = "instanceID";
     private static final String OPEN_ROSA_TIME_START = "timeStart";
     private static final String OPEN_ROSA_TIME_FINISH = "timeEnd";
     private static final String OPEN_ROSA_NAMESPACE = "http://openrosa.org/xforms/metadata";
     private NdgResult result;
     private final Reader reader;
     private Hashtable<String, ResultElementHandler> elementHandlers;
 
     ResultParser( Reader reader, NdgResult result, String surveyId ) throws ResultSaveException {
         this.reader = reader;
         this.result = result;
         initElementHandlers();
     }
 
     private void initElementHandlers() {
         elementHandlers = new Hashtable<String, ResultElementHandler>();
         elementHandlers.put( OPEN_ROSA_INSTANCE_ID, new ResultElementHandler() {
 
             public void handleElement( ResultParser parser, Element element ) {
                 result.resultId = element.getText( 0 );
             }
         } );
 
         elementHandlers.put( OPEN_ROSA_TIME_START, new ResultElementHandler() {
 
             public void handleElement( ResultParser parser, Element element ) {
                 result.startTime = ISODateTimeFormat.dateTimeNoMillis().parseDateTime( element.getText( 0 ) ).toDate();
             }
         } );
 
         elementHandlers.put( OPEN_ROSA_TIME_FINISH, new ResultElementHandler() {
 
             @Override
             public void handleElement( ResultParser parser, Element element ) {
                 result.endTime = ISODateTimeFormat.dateTimeNoMillis().parseDateTime( element.getText( 0 ) ).toDate();
             }
         } );
 
     }
 
     public void parse() {
         result.ndgUser = NdgUser.find( "byId", new Long( 1 ) ).first();// todo correct user from parsed device Id
         Document xmlDoc = XFormParser.getXMLDocument( reader );
         Element root = xmlDoc.getRootElement();
 
         parseXmlElement( root );
 
         result.save();
     }
 
     private void parseXmlElement( Element element ) {
         String name = element.getName();
         if ( element.getNamespace().equals( OPEN_ROSA_NAMESPACE ) ) {
             ResultElementHandler handler = elementHandlers.get( name );
             if ( handler != null ) {
                 handler.handleElement( this, element );
             } else {
                 parseChilds( element );
             }
         } else if ( name.equals( OPEN_ROSA_ROOT ) ) {
             parseChilds( element );
         } else {
             if ( element.getChildCount() > 0 ) {
                 parseAnswer( element );
             }
         }
     }
 
     private void parseChilds( Element element ) {
         for ( int i = 0; i < element.getChildCount(); i++ ) {
             if ( element.getType( i ) == Element.ELEMENT ) {
                 parseXmlElement( element.getElement( i ) );
             }
         }
     }
 
     private void parseAnswer( Element element ) {
        Survey survey = Survey.find("bySurveyId",  result.survey.surveyId ).first();
        Category getCategory = survey.categoryCollection.get(0);
        Question answeredQuestion = Question.find( "byObjectNameAndCategory_id", element.getName(), getCategory.id ).first();
         if ( answeredQuestion != null ) {
             Answer answer = new Answer( result.answerCollection.size() );
             answer.ndgResult = result;
             answer.question = answeredQuestion;
             if ( answeredQuestion.questionType.typeName.equalsIgnoreCase( QuestionTypesConsts.IMAGE ) ) {
                 answer.binaryData = new Blob();
                 answer.binaryData.set( new ByteArrayInputStream( Codec.decodeBASE64( element.getText( 0 ) ) ), "image/jpeg" );
             } else {
                 answer.textData = element.getText( 0 );
             }
             result.answerCollection.add( answer );
         }
     }
 }
