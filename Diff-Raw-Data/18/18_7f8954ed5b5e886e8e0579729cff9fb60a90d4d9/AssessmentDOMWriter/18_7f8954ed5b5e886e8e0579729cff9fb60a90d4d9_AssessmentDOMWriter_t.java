 /**
  * <e-Adventure> is an <e-UCM> research project. <e-UCM>, Department of Software
  * Engineering and Artificial Intelligence. Faculty of Informatics, Complutense
  * University of Madrid (Spain).
  * 
  * @author Del Blanco, A., Marchiori, E., Torrente, F.J. (alphabetical order) *
  * @author Lpez Maas, E., Prez Padilla, F., Sollet, E., Torijano, B. (former
  *         developers by alphabetical order)
  * @author Moreno-Ger, P. & Fernndez-Manjn, B. (directors)
  * @year 2009 Web-site: http://e-adventure.e-ucm.es
  */
 
 /*
  * Copyright (C) 2004-2009 <e-UCM> research group
  * 
  * This file is part of <e-Adventure> project, an educational game & game-like
  * simulation authoring tool, available at http://e-adventure.e-ucm.es.
  * 
  * <e-Adventure> is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * <e-Adventure>; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 package es.eucm.eadventure.editor.control.writer.domwriters;
 
 import java.util.List;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import es.eucm.eadventure.common.data.assessment.AssessmentProfile;
 import es.eucm.eadventure.common.data.assessment.AssessmentProperty;
 import es.eucm.eadventure.common.data.assessment.AssessmentRule;
 import es.eucm.eadventure.common.data.assessment.TimedAssessmentEffect;
 import es.eucm.eadventure.common.data.assessment.TimedAssessmentRule;
 
 public class AssessmentDOMWriter {
 
     /**
      * Private constructor.
      */
     private AssessmentDOMWriter( ) {
 
     }
 
     /**
      * Returns the DOM element for the chapter
      * 
      * @param chapter
      *            Chapter data to be written
      * @return DOM element with the chapter data
      */
     public static Element buildDOM( AssessmentProfile profile, Document doc ) {
 
         List<AssessmentRule> rules = profile.getRules( );
 
         Element assessmentNode = null;
 
         // Create the root node
         assessmentNode = doc.createElement( "assessment" );
         if( profile.isShowReportAtEnd( ) ) {
             assessmentNode.setAttribute( "show-report-at-end", "yes" );
         }
         else {
             assessmentNode.setAttribute( "show-report-at-end", "no" );
         }
         if( !profile.isShowReportAtEnd( ) || !profile.isSendByEmail( ) )
             assessmentNode.setAttribute( "send-to-email", "" );
         else {
             if( profile.getEmail( ) == null || !profile.getEmail( ).contains( "@" ) )
                 assessmentNode.setAttribute( "send-to-email", "" );
             else {
                 assessmentNode.setAttribute( "send-to-email", profile.getEmail( ) );
             }
         }
         // add scorm attributes 
         if( profile.isScorm12( ) ) {
             assessmentNode.setAttribute( "scorm12", "yes" );
         }
         else {
             assessmentNode.setAttribute( "scorm12", "no" );
         }
         if( profile.isScorm2004( ) ) {
             assessmentNode.setAttribute( "scorm2004", "yes" );
         }
         else {
             assessmentNode.setAttribute( "scorm2004", "no" );
         }
         //add the profile's name
         assessmentNode.setAttribute( "name", profile.getName( ) );
 
         Element smtpConfigNode = doc.createElement( "smtp-config" );
         smtpConfigNode.setAttribute( "smtp-ssl", ( profile.isSmtpSSL( ) ? "yes" : "no" ) );
         smtpConfigNode.setAttribute( "smtp-server", profile.getSmtpServer( ) );
         smtpConfigNode.setAttribute( "smtp-port", profile.getSmtpPort( ) );
         smtpConfigNode.setAttribute( "smtp-user", profile.getSmtpUser( ) );
         smtpConfigNode.setAttribute( "smtp-pwd", profile.getSmtpPwd( ) );
 
         assessmentNode.appendChild( smtpConfigNode );
 
         // Append the assessment rules
         for( AssessmentRule rule : rules ) {
 
             if( rule instanceof TimedAssessmentRule ) {
                 TimedAssessmentRule tRule = (TimedAssessmentRule) rule;
                 //Create the rule node and set attributes
                 Element ruleNode = doc.createElement( "timed-assessment-rule" );
                 ruleNode.setAttribute( "id", tRule.getId( ) );
                 ruleNode.setAttribute( "importance", AssessmentRule.IMPORTANCE_VALUES[tRule.getImportance( )] );
                 ruleNode.setAttribute( "usesEndConditions", ( tRule.isUsesEndConditions( ) ? "yes" : "no" ) );
 
                 //Append concept
                 if( tRule.getConcept( ) != null && !tRule.getConcept( ).equals( "" ) ) {
                     Node conceptNode = doc.createElement( "concept" );
                     conceptNode.appendChild( doc.createTextNode( tRule.getConcept( ) ) );
                     ruleNode.appendChild( conceptNode );
                 }
 
                 //Append conditions (always required at least one)
                 if( !tRule.getInitConditions( ).isEmpty( ) ) {
                     Node conditionsNode = ConditionsDOMWriter.buildDOM( ConditionsDOMWriter.INIT_CONDITIONS, tRule.getInitConditions( ) );
                     doc.adoptNode( conditionsNode );
                     ruleNode.appendChild( conditionsNode );
                 }
 
                 //Append conditions (always required at least one)
                 if( !tRule.getEndConditions( ).isEmpty( ) ) {
                     Node conditionsNode = ConditionsDOMWriter.buildDOM( ConditionsDOMWriter.END_CONDITIONS, tRule.getEndConditions( ) );
                     doc.adoptNode( conditionsNode );
                     ruleNode.appendChild( conditionsNode );
                 }
 
                 // Create effects
                 for( int i = 0; i < tRule.getEffectsCount( ); i++ ) {
                     //Create effect element and append it
                     Element effectNode = doc.createElement( "assessEffect" );
 
                     // Append time attributes
                     effectNode.setAttribute( "time-min", Integer.toString( tRule.getMinTime( i ) ) );
                     effectNode.setAttribute( "time-max", Integer.toString( tRule.getMaxTime( i ) ) );
 
                     //Append set-text when appropriate
                     TimedAssessmentEffect currentEffect = tRule.getEffects( ).get( i );
                     if( currentEffect.getText( ) != null && !currentEffect.getText( ).equals( "" ) ) {
                         Node textNode = doc.createElement( "set-text" );
                         textNode.appendChild( doc.createTextNode( currentEffect.getText( ) ) );
                         effectNode.appendChild( textNode );
                     }
                     //Append properties
                     for( AssessmentProperty property : currentEffect.getAssessmentProperties( ) ) {
                         Element propertyElement = doc.createElement( "set-property" );
                         propertyElement.setAttribute( "id", property.getId( ) );
                         propertyElement.setAttribute( "value", String.valueOf( property.getValue( ) ) );
                         effectNode.appendChild( propertyElement );
                     }
                     //Append the effect
                     ruleNode.appendChild( effectNode );
                 }
 
                 //Append the rule
                 assessmentNode.appendChild( ruleNode );
             }
             else {
                 //Create the rule node and set attributes
                 Element ruleNode = doc.createElement( "assessment-rule" );
                 ruleNode.setAttribute( "id", rule.getId( ) );
                 ruleNode.setAttribute( "importance", AssessmentRule.IMPORTANCE_VALUES[rule.getImportance( )] );
 
                 //Append concept
                 if( rule.getConcept( ) != null && !rule.getConcept( ).equals( "" ) ) {
                     Node conceptNode = doc.createElement( "concept" );
                     conceptNode.appendChild( doc.createTextNode( rule.getConcept( ) ) );
                     ruleNode.appendChild( conceptNode );
                 }
 
                 //Append conditions (always required at least one)
                 if( !rule.getConditions( ).isEmpty( ) ) {
                     Node conditionsNode = ConditionsDOMWriter.buildDOM( rule.getConditions( ) );
                     doc.adoptNode( conditionsNode );
                     ruleNode.appendChild( conditionsNode );
                 }
 
                 //Create effect element and append it
                 Node effectNode = doc.createElement( "assessEffect" );
                 //Append set-text when appropriate
                 if( rule.getText( ) != null && !rule.getText( ).equals( "" ) ) {
                     Node textNode = doc.createElement( "set-text" );
                     textNode.appendChild( doc.createTextNode( rule.getText( ) ) );
                     effectNode.appendChild( textNode );
                 }
                 //Append properties
                 for( AssessmentProperty property : rule.getAssessmentProperties( ) ) {
                     Element propertyElement = doc.createElement( "set-property" );
                     propertyElement.setAttribute( "id", property.getId( ) );
                     propertyElement.setAttribute( "value", property.getValue( )  );
                     if ( property.getVarName( )!= null )
                        propertyElement.setAttribute( "varName",  property.getVarName( )  );
                     effectNode.appendChild( propertyElement );
                 }
                 //Append the effect
                 ruleNode.appendChild( effectNode );
 
                 //Append the rule
                 assessmentNode.appendChild( ruleNode );
             }
 
         }
 
         return assessmentNode;
     }
 }
