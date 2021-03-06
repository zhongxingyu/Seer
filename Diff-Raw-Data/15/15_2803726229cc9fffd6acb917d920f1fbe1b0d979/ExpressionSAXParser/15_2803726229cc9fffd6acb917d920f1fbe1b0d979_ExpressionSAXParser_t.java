 /*
  *    Geotools - OpenSource mapping toolkit
  *    (C) 2002, Centre for Computational Geography
  *
  *    This library is free software; you can redistribute it and/or
  *    modify it under the terms of the GNU Lesser General Public
  *    License as published by the Free Software Foundation; 
  *    version 2.1 of the License.
  *
  *    This library is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *    Lesser General Public License for more details.
  *
  *    You should have received a copy of the GNU Lesser General Public
  *    License along with this library; if not, write to the Free Software
  *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *    
  */
 
 package org.geotools.filter;
 
 // J2SE dependencies
 import java.util.logging.Logger;
 
 // Java Topology Suite dependencies
 import com.vividsolutions.jts.geom.*;
 
 // Geotools dependencies
 import org.geotools.data.*;
 import org.geotools.feature.*;
 
 
 /**
  * Defines a like filter, which checks to see if an attribute matches a REGEXP.
  *
 * @version $Id: ExpressionSAXParser.java,v 1.5 2003/05/27 19:21:57 cholmesny Exp $
  * @author Rob Hranac, Vision for New York
  */
 public class ExpressionSAXParser {
 
     /** The logger for the filter module. */
     private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");
     private static final org.geotools.filter.FilterFactory filterFactory = org.geotools.filter.FilterFactory.createFilterFactory();
     /** The attribute value, which must be an attribute expression. */
     private ExpressionSAXParser expressionFactory = null;
 
     /** The (limited) REGEXP pattern. */
     private Expression currentExpression = null;
 
     /** The (limited) REGEXP pattern. */
     private String currentState = null;
 
     /** The (limited) REGEXP pattern. */
     private String declaredType = null;
 
     /** The (limited) REGEXP pattern. */
     private boolean readyFlag = false;
 
     /** The (limited) REGEXP pattern. */
     private FeatureType schema;
 
     /** The (limited) REGEXP pattern. */
     private boolean readCharacters = false;
 
     /**
      * Constructor which flags the operator as between.
      */
     public ExpressionSAXParser (FeatureType schema) {
         this.schema = schema;
     }
 
 
     /**
      * Constructor which flags the operator as between.
      */
     public void start(String declaredType) throws IllegalFilterException{
 
         LOGGER.finer("incoming type: " + declaredType);
         LOGGER.finer("declared type: " + this.declaredType);
         LOGGER.finer("current state: " + currentState);
         
         if( expressionFactory == null) {
 
             this.declaredType = declaredType;
 
             // if the expression is math, then create a factory for its
             // sub expressions, otherwise just instantiate the main expression
             if( DefaultExpression.isMathExpression( convertType(declaredType) ) ) {
                 expressionFactory = new ExpressionSAXParser(schema);
                 currentExpression = filterFactory.createMathExpression(convertType(declaredType));
                 LOGGER.finer("is math expression");
             }
             else if( DefaultExpression.
                      isLiteralExpression( convertType(declaredType) ) ) {
                 currentExpression = filterFactory.createLiteralExpression();
                 readCharacters = true;
                 LOGGER.finer("is literal expression");
             }
             else if( DefaultExpression.
                      isAttributeExpression( convertType(declaredType) ) ) {
                 currentExpression = filterFactory.createAttributeExpression(schema);
                 readCharacters = true;
                 LOGGER.finer("is attribute expression");
             }
             currentState = setInitialState(currentExpression);
             readyFlag = false;
         }
 
         else {
             expressionFactory.start(declaredType);
         }
     }
 
 
     /**
      * Sets the matching pattern for this FilterLike.
      *
      * @param pattern The limited REGEXP pattern for this string. 
      */
     public void end(String message) throws IllegalFilterException{
 
         LOGGER.finer("declared type: " + declaredType);
         LOGGER.finer("end message: " + message);
         LOGGER.finer("current state: " + currentState);
         LOGGER.finest("expression factory: " + expressionFactory);
 
         // first, check to see if there are internal (nested) expressions
         //  note that this is identical to checking if the currentExpression
         //  is a math expression
         // if this internal expression exists, send its factory an end message
         if( expressionFactory != null ) {
             expressionFactory.end(message);
 
             // if the factory is ready to be returned:
             //  (1) add its expression to the current expression, as determined
             //      by the current state
             //  (2) increment the current state
             //  (3) set the factory to null to indicate that it is now done
             // if in a bad state, throw exception
             if( expressionFactory.isReady() ) {
                 if( currentState.equals("leftValue") ) {
                     ((MathExpression) currentExpression).
                         addLeftValue(expressionFactory.create());
                     currentState = "rightValue";
                     expressionFactory = new ExpressionSAXParser(schema);
                     LOGGER.finer("just added left value: " + currentState);
                 }
                 else if( currentState.equals("rightValue") ) {
                     ((MathExpression) currentExpression).
                         addRightValue(expressionFactory.create());
                     currentState = "complete";
                     expressionFactory = null;
                     LOGGER.finer("just added right value: " + currentState);
                 }
                 else {
                     throw new IllegalFilterException
                         ("Attempted to add sub expression in a bad state: "
                          + currentState);
                 }
             }
         }
 
         // if there are no nested expressions here,
         //  determine if this expression is ready and set flag appropriately
         else if( declaredType.equals(message) && currentState.equals("complete") ){
             readCharacters = false;
             readyFlag = true;
         }
 
         // otherwise, throw exception
         else {
             throw new IllegalFilterException
                 ("Reached end of unready, non-nested expression: "
                  + currentState);
         }
 
     }
 
     /**
      * Checks to see if this expression is ready to be returned.
      *
      */
     public boolean isReady() {
         return readyFlag;
     }
 
     /**
      * Handles incoming characters.
      *
      * @param message
      * @tasks TODO: this function is a mess, but it's mostly due to filters
      * being loosely coupled with schemas, so we have to make a lot of guesses.
     * @tasks TODO: Revisit putting characters to lower case and stripping
     * leading characters.  Needed now to get things working, and may be
     * the best choice in the end, but it should be thought through more.  
      */
     public void message(String message) throws IllegalFilterException{
 
         // TODO 2:
         // AT SOME POINT MUST MAKE THIS HANDLE A TYPED FEATURE
         // BY PASSING IT A FEATURE AND CHECKING ITS TYPE HERE
 
         LOGGER.finer("incoming message: " + message);
         LOGGER.finer("should read chars: " + readCharacters);
        //HACK: this is needed for postgis datasource, and all dses that 
	//store everything in lower case, to work properly.  We should 
	//revisit this, if we want it to be the default policy for geotools
	//to be case insensitive, or if we want to do some elaborate hacks
	//for postgis et al. (it will be nasty)
	//message = message.toLowerCase();
	
         if(readCharacters) {
             // If an attribute path, set it.  Assumes undeclared type.
             if( currentExpression instanceof AttributeExpression) {
                 LOGGER.finer("...");
 		//HACK: this code is to get rid of the leading junk that can
 		//occur in a filter encoding.  The '.' is from the .14 wfs spec
 		//when the style was typeName.propName, such as road.nlanes, The
 		//':' is from wfs 1.0 xml request, such as myns:nlanes, and the
 		//'/' is from wfs 1.0 kvp style: road/nlanes.  We're not currently
 		//checking to see if the typename matches, or if the namespace is
 		//right, which isn't the best, so that should be fixed.
 		String[] splitName = message.split("[.:/]");
 		String newAttName = message;
 		if (splitName.length == 1) {
 		    newAttName = splitName[0];
 		} else {
 		    //REVISIT: not sure what to do if there are multiple
 		    //delimiters.  
 		    //REVISIT: should we examine the first value?  See
 		    //if the namespace or typename matches up right?
 		    //this is currently very permissive, just grabs
 		    //the value of the end.
 		    newAttName = splitName[splitName.length - 1];
 		}
 
 		((AttributeExpression) currentExpression).setAttributePath(newAttName);
                 LOGGER.finer("...");
                 currentState = "complete";
                 LOGGER.finer("...");
             }
             
             // This is a relatively loose assignment routine, which uses
             //  the fact that the three allowed literal types have a strict
             //  instatiation hierarchy (ie. double can be an int can be a 
             //  string, but not the other way around).
             // A better routine would consider the use of this expression
             //  (ie. will it be compared to a double or searched with a
             //  like filter?)
             else if( currentExpression instanceof LiteralExpression) {
                 try {
                     Object tempLiteral = new Double(message);
                     ((LiteralExpression) currentExpression).setLiteral(tempLiteral);
                     currentState = "complete";
                 }
                 catch(NumberFormatException e1) {
                     try {
                         Object tempLiteral = new Integer(message);
                         ((LiteralExpression) currentExpression).
                             setLiteral(tempLiteral);
                         currentState = "complete";
                     }
                     catch(NumberFormatException e2) {
                         Object tempLiteral = message;
                         ((LiteralExpression) currentExpression).
                         setLiteral(tempLiteral);
                         currentState = "complete";
                 }                
                 }
             }
             else if (expressionFactory != null) {
                 expressionFactory.message(message);
             }
         }
         else if (expressionFactory != null) {
             expressionFactory.message(message);
         }
     }
 
     /**
      * Gets geometry.
      *
      * @param geometry The geometry from the filter.
      */
     public void geometry(Geometry geometry) 
         throws IllegalFilterException {
 
         // Sets the geometry for the expression, as appropriate
         LOGGER.finer("got geometry: " + geometry.toString());
         //if( currentExpression.getType() == ExpressionDefault.LITERAL_GEOMETRY ) {
         //LOGGER.finer("got geometry: ");
         currentExpression = filterFactory.createLiteralExpression();
         ((LiteralExpression) currentExpression).setLiteral(geometry);
         LOGGER.finer("set expression: " + currentExpression.toString());
         currentState = "complete";
         LOGGER.finer("set current state: " + currentState);
         //        }
             
     }
 
     /**
      * Sets the multi wildcard for this FilterLike.
      *
      */
     public Expression create() {
         LOGGER.finer("about to create expression: " + currentExpression.toString());
         return currentExpression;
     }
 
 
     /**
      * Sets the multi wildcard for this FilterLike.
      *
      */
     private static String setInitialState(Expression currentExpression) 
         throws IllegalFilterException {
 
         if( currentExpression instanceof MathExpression ) {
             return "leftValue";
         }
         else if( ( currentExpression instanceof AttributeExpression ) ||
                  ( currentExpression instanceof LiteralExpression ) ) {
             return "";
         }
         else {
             throw new IllegalFilterException
                 ("Created illegal expression: " + 
                  currentExpression.getClass().toString());
         }
 
     }
 
     /**
      * Checks to see if passed type is logic.
      *
      * @param filterType Type of filter for check.
      * @return Whether or not this is a logic filter type.
      */
     protected static short convertType(String expressionType) {
 
         // matches all filter types to the default logic type
         if( expressionType.equals("Add") ) {
             return DefaultExpression.MATH_ADD;
         }
         else if( expressionType.equals("Sub") ) {
             return DefaultExpression.MATH_SUBTRACT;
         }
         else if( expressionType.equals("Mul") ) {
             return DefaultExpression.MATH_MULTIPLY;
         }
         else if( expressionType.equals("Div") ) {
             return DefaultExpression.MATH_DIVIDE;
         }
         else if( expressionType.equals("PropertyName") ) {
             return DefaultExpression.ATTRIBUTE_DOUBLE;
         }
         else if( expressionType.equals("Literal") ) {
             return DefaultExpression.LITERAL_DOUBLE;
         }
         return DefaultExpression.ATTRIBUTE_UNDECLARED;
 
     }
 
     
 }
