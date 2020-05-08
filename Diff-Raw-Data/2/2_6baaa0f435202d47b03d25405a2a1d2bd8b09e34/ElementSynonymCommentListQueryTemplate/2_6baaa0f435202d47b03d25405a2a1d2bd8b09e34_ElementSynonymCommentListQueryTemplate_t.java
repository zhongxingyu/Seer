 package org.lh.dmlj.schema.editor.dictionary.tools.template;
 
 import org.lh.dmlj.schema.editor.dictionary.tools.template.IQueryTemplate;
 
 public class ElementSynonymCommentListQueryTemplate implements IQueryTemplate {
 
   protected static String nl;
   public static synchronized ElementSynonymCommentListQueryTemplate create(String lineSeparator)
   {
     nl = lineSeparator;
     ElementSynonymCommentListQueryTemplate result = new ElementSynonymCommentListQueryTemplate();
     nl = null;
     return result;
   }
 
   public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
   protected final String TEXT_1 = " " + NL + "SELECT NAMESYN_083.ROWID AS NAMESYN_083_ROWID," + NL + "       NAMEDES_186.ROWID AS NAMEDES_186_ROWID," + NL + "       * " + NL + "FROM \"";
  protected final String TEXT_2 = "\".\"NAMESYN-083\" AS NAMESYN_083," + NL + "\t \"";
   protected final String TEXT_3 = "\".\"NAMEDES-186\" AS NAMEDES_186                                   " + NL + "WHERE NAMESYN_083.ROWID = X'";
   protected final String TEXT_4 = "' AND " + NL + "      \"NAMESYN-NAMEDES\"";
 
 	public String generate(Object argument)
   {
     final StringBuffer stringBuffer = new StringBuffer();
     
 /**
  * Copyright (C) 2014  Luc Hermans
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program.  If
  * not, see <http://www.gnu.org/licenses/>.
  * 
  * Contact information: kozzeluc@gmail.com.
  */
 
     
 Object[] args = (Object[]) argument;
 String sysdirlSchema = (String) args[0];
 String hexDbkeyNamesyn_083 = (String) args[1];
 
     stringBuffer.append(TEXT_1);
     stringBuffer.append( sysdirlSchema );
     stringBuffer.append(TEXT_2);
     stringBuffer.append( sysdirlSchema );
     stringBuffer.append(TEXT_3);
     stringBuffer.append( hexDbkeyNamesyn_083 );
     stringBuffer.append(TEXT_4);
     return stringBuffer.toString();
   }
 }
