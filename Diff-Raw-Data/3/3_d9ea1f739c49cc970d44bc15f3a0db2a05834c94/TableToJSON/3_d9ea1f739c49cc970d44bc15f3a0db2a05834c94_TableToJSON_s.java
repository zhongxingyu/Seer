 /**
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, Board of Trustees-University of Illinois.
  * All rights reserved.
  *
  * Developed by:
  *
  * Automated Learning Group
  * National Center for Supercomputing Applications
  * http://www.seasr.org
  *
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal with the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimers.
  *
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimers in the
  *    documentation and/or other materials provided with the distribution.
  *
  *  * Neither the names of Automated Learning Group, The National Center for
  *    Supercomputing Applications, or University of Illinois, nor the names of
  *    its contributors may be used to endorse or promote products derived from
  *    this Software without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
  * WITH THE SOFTWARE.
  */
 
 package org.seasr.meandre.components.transform.table;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.datatypes.table.Table;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.vis.gwt.tableviewer.client.ColumnTypes;
 
 /**
  * @author Loretta Auvil
  */
 
 @Component(
         creator = "Loretta Auvil",
         description = "Converts table to JSON format.",
         name = "Table To JSON",
         tags = "table, JSON, convert",
         firingPolicy = FiringPolicy.any,
         rights = Licenses.UofINCSA,
         baseURL = "meandre://seasr.org/components/foundry/",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class TableToJSON extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------
 
     @ComponentInput(
             name = "table",
             description = "The table" +
             "<br>TYPE: org.seasr.datatypes.table.Table"
     )
     protected static final String IN_TABLE = "table";
 
     //------------------------------ OUTPUTS ------------------------------
 
     @ComponentOutput(
             name = "json",
             description = "text output as JSON" +
             "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_JSON = "json";
 
     //--------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 
         Table mytable = (Table)cc.getDataComponentFromInput(IN_TABLE);
 
         int numRows = mytable.getNumRows();
         int numCols = mytable.getNumColumns();
         JSONArray jsonData = new JSONArray();
         for (int i=0; i < numRows; i++) {
             JSONObject jsonRow = new JSONObject();
             for (int j=0; j< numCols; j++){
                 int type = mytable.getColumnType(j);
                 if (i==0)
                     console.finest("col = "+j+" type = "+type);
                 //TODO: test all the data types
                 switch (type) {
                     case ColumnTypes.INTEGER:
                         jsonRow.put(mytable.getColumnLabel(j), mytable.getInt(i,j));
                         break;
                     case ColumnTypes.FLOAT:
                         jsonRow.put(mytable.getColumnLabel(j), mytable.getFloat(i,j));
                         break;
                     case ColumnTypes.DOUBLE:
                         jsonRow.put(mytable.getColumnLabel(j), mytable.getDouble(i,j));
                         break;
                     case ColumnTypes.SHORT:
                         jsonRow.put(mytable.getColumnLabel(j), mytable.getShort(i,j));
                         break;
                     case ColumnTypes.LONG:
                         jsonRow.put(mytable.getColumnLabel(j), mytable.getLong(i,j));
                         break;
                     case ColumnTypes.STRING:
                         jsonRow.put(mytable.getColumnLabel(j), mytable.getString(i,j));
                         break;
                     case ColumnTypes.CHAR_ARRAY:
                         jsonRow.put(mytable.getColumnLabel(j), mytable.getChars(i,j));
                         break;
                     case ColumnTypes.BYTE_ARRAY:
                         jsonRow.put(mytable.getColumnLabel(j), mytable.getBytes(i,j));// cannot display
                         break;
                     case ColumnTypes.BOOLEAN:
                         jsonRow.put(mytable.getColumnLabel(j), mytable.getBoolean(i,j));
                         break;
                     case ColumnTypes.OBJECT:
                         jsonRow.put(mytable.getColumnLabel(j), mytable.getObject(i,j));
                         break;
                     case ColumnTypes.BYTE:
                         jsonRow.put(mytable.getColumnLabel(j), mytable.getByte(i,j));
                         break;
                     case ColumnTypes.CHAR:
                         jsonRow.put(mytable.getColumnLabel(j), mytable.getChar(i,j));
                         break;
                     case ColumnTypes.NOMINAL:
                         jsonRow.put(mytable.getColumnLabel(j), mytable.getString(i,j));
                     case ColumnTypes.UNSPECIFIED:
                         jsonRow.put(mytable.getColumnLabel(j), mytable.getObject(i,j));
                         break;
                 }
             }
             jsonData.put(jsonRow);
         }
 
         cc.pushDataComponentToOutput(OUT_JSON,BasicDataTypesTools.stringToStrings(jsonData.toString(4)));
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 }
