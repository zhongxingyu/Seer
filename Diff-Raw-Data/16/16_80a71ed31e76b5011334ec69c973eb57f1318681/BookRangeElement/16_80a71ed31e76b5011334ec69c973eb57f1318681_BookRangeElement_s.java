 /*
  *  Copyright 2011 Jeffrey Hoyt. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.fcps.hutchison;
 
 import org.mitre.mrald.util.*;
 import org.mitre.mrald.query.*;
 import org.mitre.mrald.control.MsgObject;
 import org.mitre.mrald.formbuilder.FormBuilderElement;
 import org.mitre.mrald.util.Config;
 import org.mitre.mrald.util.DBMetaData;
 import org.mitre.mrald.util.FBUtils;
 import org.mitre.mrald.util.FormTags;
 import org.mitre.mrald.util.MraldException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.Text;
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 
 /**
  * The BooKRangeElement is a custom class designed to work with the Hutchison Elementary
  * school library.  It work only on the book table in that schema.
  *
  * @author Jeffrey Hoyt
  *
  * @see java.lang.Object
  */
 public class BookRangeElement extends SqlElements implements FormBuilderElement
 {
     /**
      *  Constructor for the QueryElements object
      *
      *@since
      */
     public BookRangeElement(  )
     {
         super(  );
         elementType = "BookRange";
         MraldOutFile.logToFile(Config.getProperty("LOGFILE"), "Created BookRangeElement with simple constructor"      );
     }
 
     /**
      *  Constructor for the QueryElements object
      *
      *@param  msg  Description of the Parameter
      *@since
      */
     public BookRangeElement( MsgObject msg )
     {
         super( msg );
         elementType = "BookRange";
         MraldOutFile.logToFile(Config.getProperty("LOGFILE"), "Created BookRangeElement with MsgObject constructor"      );
     }
 
     /**
      * Produces the HTML for inclusion on the second step of form
      * building. The HTML returned should be self-supporting - i.e., only the
      * guts of a &lt;div&gt; or a &lt;td&gt; tag. It should not be part of a
     * larger table structure. It is used in building the second page of the
      * form buliding process.
      *
      * @param md Description of the Parameter
      * @param num Which iteration this is. This should be used to create unique
      *        tag names
      *
      * @return The HTML for inclusion in the second form building page.
      */
     public String getFBHtml( DBMetaData md, int num, int thread)
     {
         StringBuffer ret = new StringBuffer(  );
         ret.append( "<strong>Works only on the book table :</strong>" );
         ret.append( "&nbsp; &nbsp; <strong>Desired Field Label :</strong>" );
         ret.append( "<input name=\"Range" );
         ret.append( num );
         ret.append( "\" type=\"text\" size=\"22\" value=\"Range of Book Level\">" );
 
         return ret.toString(  );
     }
 
     /**
      * Builds the Node to be added to the root node of an XML Document.<br>
      * <br>
      * Range should be of the form:<pre> <range>  <table>   GRIDMORA </table>
      *  <field>LONGITUDE</field> <label>Longitude</label> </range> </pre>
      *
      * @param document Description of the Parameter
      *
      * @return The fBNode value
      */
     public Node getFBNode( Document document )
     {
         String label = nameValues.getValue( FormTags.VALUE_TAG )[0];
 
         if ( label.equals( Config.EMPTY_STR ) )
         {
             return null;
         }
 
         Element ret = document.createElement( "bookrange" );
 
         Element labelElement = document.createElement( "label" );
         Text labelText = document.createTextNode( label );
         labelElement.appendChild( labelText );
         ret.appendChild( labelElement );
 
         return ret;
     }
 
     /**
      * Only works on the book table
      *
      * @param currentFromList Description of Parameter
      *
      * @return Description of the Returned Value
      *
      * @since
      */
     public ArrayList<String> buildFrom( ArrayList<String> currentFromList )
     {
         if ( !currentFromList.contains( "book" ) )
         {
             currentFromList.add( "book" );
         }
 
         return currentFromList;
     }
 
     /**
      * Description of the Method
      *
      * @param currentGroupByList List of Group By parameters
      * @return Description of the Returned Value
      * @exception MraldException Description of Exception
      */
     public ArrayList<String> buildGroupBy( ArrayList<String> currentGroupByList ) throws MraldException
     {
         return currentGroupByList;
     }
 
     /**
      * Description of the Method
      *
      * @param currentOrderBy Description of Parameter
      * @return Description of the Returned Value
      * @exception MraldException Description of Exception
      */
     public ArrayList<String> buildOrderBy( ArrayList<String> currentOrderBy ) throws MraldException
     {
         return currentOrderBy;
     }
 
     /**
      * Description of the Method
      *
      * @param currentSelectList Description of Parameter
      * @return Description of the Returned Value
      * @exception MraldException Description of Exception
      */
     public ArrayList<String> buildSelect( ArrayList<String> currentSelectList ) throws MraldException
     {
         return currentSelectList;
     }
 
     /**
      * Description of the Method
      *
      * @param currentWhereList Description of the Parameter
      * @return Description of the Returned Value
      */
     public ArrayList<String> buildWhere( ArrayList<String> currentWhereList )
     {
         //Build the Where clause using items in the
        String newValue = null;
         int noOfValues = nameValues.getValue( FormTags.VALUE_TAG ).length;
 
         if ( noOfValues == 0 )
         {
             return currentWhereList;
         }
 
         String maxValue = nameValues.getValue( FormTags.MAXTAG )[0].toString(  );
         String minValue = nameValues.getValue( FormTags.MINTAG )[0].toString(  );
 
         if ( !minValue.equals( "" ) )
         {
             if ( !maxValue.equals( "" ) )
             {
                newValue = minValue + " between minlevel and maxlevel or " + maxValue + " between minlevel and maxlevel";
             }
             else
             {
                newValue = minValue + " between minlevel and maxlevel";
             }
         }
         else if ( !maxValue.equals( "" ) )
         {
            newValue = maxValue + " between minlevel and maxlevel";
         }
         else
         {
             return currentWhereList;
         }
 
         if ( !currentWhereList.contains( newValue ) )
         {
             currentWhereList.add( newValue );
         }
 
         return currentWhereList;
     }
 
     /**
      * Preprocessor - carriers out any additional processing required.
      *
      * @param msg Description of the Parameter
      * @param currentName Description of the Parameter
      * @return Description of the Return Value
      */
     public String preProcess( MsgObject msg, String currentName )
     {
         String[] groupTags = { "Min", "Max" };
         collectElementGroup( msg, currentName, groupTags );
 
         return currentName;
     }
 }
 
