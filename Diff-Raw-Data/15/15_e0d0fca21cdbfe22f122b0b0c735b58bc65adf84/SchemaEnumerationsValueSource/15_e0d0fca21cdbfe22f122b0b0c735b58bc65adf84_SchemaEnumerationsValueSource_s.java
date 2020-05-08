 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.axiom.value.source;
 
 import java.util.StringTokenizer;
 
 import org.apache.commons.lang.exception.NestableRuntimeException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.netspective.axiom.connection.AbstractConnectionContext;
 import com.netspective.axiom.schema.Schema;
 import com.netspective.axiom.schema.Schemas;
 import com.netspective.axiom.schema.Table;
 import com.netspective.axiom.schema.table.type.EnumerationTable;
 import com.netspective.axiom.schema.table.type.EnumerationTableRow;
 import com.netspective.axiom.value.DatabaseConnValueContext;
 import com.netspective.commons.value.GenericValue;
 import com.netspective.commons.value.PresentationValue;
 import com.netspective.commons.value.Value;
 import com.netspective.commons.value.ValueContext;
 import com.netspective.commons.value.ValueSourceDocumentation;
 import com.netspective.commons.value.ValueSourceSpecification;
 import com.netspective.commons.value.exception.ValueSourceInitializeException;
 import com.netspective.commons.value.source.AbstractValueSource;
 
 public class SchemaEnumerationsValueSource extends AbstractValueSource
 {
     private static final Log log = LogFactory.getLog(SchemaEnumerationsValueSource.class);
 
     public static final String[] IDENTIFIERS = new String[]{"schema-enum"};
     public static final ValueSourceDocumentation DOCUMENTATION = new ValueSourceDocumentation("Obtains the values or a value from a schema's enumeration table.",
                                                                                               new ValueSourceDocumentation.Parameter[]
                                                                                               {
                                                                                                   new ValueSourceDocumentation.Parameter("enum-table", true, "The format is 'schema-name.table-name'. Where the only required value is the table-name (if schema-name is not provided, the first available schema is used)."),
                                                                                                   new ValueSourceDocumentation.Parameter("enum-name", false, "The name/caption of the enumeration entry in the table"),
                                                                                               });
 
     private String schemaName;
     private String tableName;
     private String enumCaption;
 
     public static String[] getIdentifiers()
     {
         return IDENTIFIERS;
     }
 
     public static ValueSourceDocumentation getDocumentation()
     {
         return DOCUMENTATION;
     }
 
     public SchemaEnumerationsValueSource()
     {
     }
 
     public String getSchemaName()
     {
         return schemaName;
     }
 
     public void setSchemaName(String schemaName)
     {
         this.schemaName = schemaName;
     }
 
     public String getTableName()
     {
         return tableName;
     }
 
     public void setTableName(String tableName)
     {
         this.tableName = tableName;
     }
 
     public String getEnumCaption()
     {
         return enumCaption;
     }
 
     public void setEnumCaption(String enumCaption)
     {
         this.enumCaption = enumCaption;
     }
 
     public void initialize(ValueSourceSpecification spec) throws ValueSourceInitializeException
     {
         super.initialize(spec);
         StringTokenizer st = new StringTokenizer(spec.getParams(), ",");
 
         String schemaAndTableName = st.nextToken();
         int schemaDelimPos = schemaAndTableName.indexOf('.');
         if(schemaDelimPos != -1)
         {
             setSchemaName(schemaAndTableName.substring(0, schemaDelimPos));
             setTableName(schemaAndTableName.substring(schemaDelimPos + 1));
         }
         else
             setTableName(schemaAndTableName);
 
         if(st.hasMoreTokens())
         {
             setEnumCaption(st.nextToken());
         }
     }
 
     public EnumerationTable getEnumerationTable(ValueContext vc)
     {
         DatabaseConnValueContext dcvc = null;
         if(vc instanceof AbstractConnectionContext)
             dcvc = ((AbstractConnectionContext) vc).getDatabaseValueContext();
         else
             dcvc = (DatabaseConnValueContext) vc;
         Schemas schemas = dcvc.getSqlManager().getSchemas();
         Table table = null;
         try
         {
             if(schemas.size() == 0)
                 throw new RuntimeException("No schemas available in SQL Manager.");
 
             Schema schema = schemaName == null ? schemas.getDefault() : schemas.getByNameOrXmlNodeName(schemaName);
             if(schema == null)
                 throw new RuntimeException("Schema '" + schemaName + "' not found.");
 
             table = schema.getTables().getByNameOrXmlNodeName(tableName);
             if(table == null)
                 throw new RuntimeException("Table '" + tableName + "' not found in schema '" + schemaName + "'");
 
             if(!(table instanceof EnumerationTable))
                 throw new RuntimeException("Table '" + tableName + "' in schema '" + schemaName + "' is not an enumeration table.");
         }
         catch(Exception e)
         {
             log.error("Failed to get enumeration table.", e);
             throw new NestableRuntimeException(e);
         }
 
         return (EnumerationTable) table;
     }
 
     public Value getValue(ValueContext vc)
     {
         if(getEnumCaption() == null)
            return getEnumerationTable(vc).getEnums().getEnumerationsValue();
         else
         {
            EnumerationTableRow enumRow = getEnumerationTable(vc).getEnums().getByIdOrCaptionOrAbbrev(getEnumCaption());
             return new GenericValue(enumRow.getIdAsInteger());
         }
     }
 
     public PresentationValue getPresentationValue(ValueContext vc)
     {
         if(getEnumCaption() == null)
             return getEnumerationTable(vc).getEnums().getEnumerationsPresentationValue();
         else
         {
             return new PresentationValue(getValue(vc));
         }
     }
 
     public boolean hasValue(ValueContext vc)
     {
         return false;
     }
 
     public String toString()
     {
         StringBuffer result = new StringBuffer(super.toString());
         result.append(", schema-name: " + schemaName);
         result.append(", table-name: " + tableName);
         return result.toString();
     }
 }
