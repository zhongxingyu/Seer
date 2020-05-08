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
 package com.netspective.axiom.policy.ddl;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.netspective.axiom.DatabasePolicy;
 import com.netspective.axiom.policy.AnsiDatabasePolicy;
 import com.netspective.axiom.policy.SqlDdlFormats;
 import com.netspective.axiom.policy.SqlDdlGenerator;
 import com.netspective.axiom.schema.Column;
 import com.netspective.axiom.schema.Columns;
 import com.netspective.axiom.schema.ForeignKey;
 import com.netspective.axiom.schema.Index;
 import com.netspective.axiom.schema.Indexes;
 import com.netspective.axiom.schema.PrimaryKeyColumns;
 import com.netspective.axiom.schema.Row;
 import com.netspective.axiom.schema.Rows;
 import com.netspective.axiom.schema.Schema;
 import com.netspective.axiom.schema.Table;
 import com.netspective.axiom.schema.Tables;
 import com.netspective.axiom.schema.column.BasicColumn;
 import com.netspective.axiom.schema.column.SqlDataDefns;
 import com.netspective.axiom.schema.column.type.AutoIncColumn;
 import com.netspective.axiom.schema.table.IndexesCollection;
 import com.netspective.axiom.schema.table.TablesCollection;
 import com.netspective.axiom.sql.DbmsSqlText;
 import com.netspective.axiom.value.DatabasePolicyValueContext;
 import com.netspective.commons.template.TemplateProcessor;
 import com.netspective.commons.text.JavaExpressionText;
 import com.netspective.commons.text.TextUtils;
 
 public class AnsiSqlDdlGenerator implements SqlDdlGenerator
 {
     private static final Log log = LogFactory.getLog(AnsiSqlDdlGenerator.class);
 
     public AnsiSqlDdlGenerator()
     {
     }
 
     public void generateSqlDdl(File output, DatabasePolicyValueContext vc, Schema schema, boolean dropFirst, boolean createCommentObjects, boolean createAbbreviationsMapCommentBlock) throws IOException
     {
         Writer writer = new FileWriter(output);
         try
         {
             generateSqlDdl(writer, vc, schema, dropFirst, createCommentObjects, createAbbreviationsMapCommentBlock);
         }
         finally
         {
             if(writer != null) writer.close();
         }
     }
 
     public void generateSqlDdl(Writer writer, DatabasePolicyValueContext vc, Schema schema, boolean dropFirst, boolean createCommentObjects, boolean createAbbreviationsMapCommentBlock) throws IOException
     {
         SqlDdlGeneratorContext gc = new SqlDdlGeneratorContext(writer, vc, schema, dropFirst, createCommentObjects, createAbbreviationsMapCommentBlock);
         renderSqlDdlSchemaScript(gc);
     }
 
     public void renderSqlDdlSchemaScript(SqlDdlGeneratorContext gc) throws IOException
     {
         DatabasePolicy policy = gc.getDatabasePolicy();
         Writer writer = gc.getWriter();
         SqlDdlFormats ddlFormats = gc.getSqlDdlFormats();
         Tables tablesWithData = new TablesCollection();
 
         TemplateProcessor preDdlContent = ddlFormats.getPreDdlContentTemplate();
         TemplateProcessor preStaticDataContent = ddlFormats.getPreStaticDataContentTemplate();
         TemplateProcessor postDdlContent = ddlFormats.getPostDdlContentTemplate();
 
         Map prePostDdlTemplateVars = new HashMap();
         prePostDdlTemplateVars.put("gc", gc);
         prePostDdlTemplateVars.put("schema", gc.getSchema());
         prePostDdlTemplateVars.put("textUtils", TextUtils.getInstance());
 
         if(preDdlContent != null)
             preDdlContent.process(writer, gc.getValueContext(), prePostDdlTemplateVars);
 
         if(gc.isCreateAbbreviationsMapCommentBlock())
             renderSqlDdlAbbreviationsCommentBlock(gc);
 
         Tables tables = gc.getSchema().getTables();
         for(int i = 0; i < tables.size(); i++)
         {
             Table table = tables.get(i);
 
             if(i > 0)
                 writer.write("\n");
 
             if(gc.isDropObjectsFirst())
             {
                 if(policy.supportsSequences())
                     renderSqlDdlSequenceStatements(gc, table, true);
 
                 renderSqlDdlIndexStatements(gc, table, true);
 
                 if(renderSqlDdlTableStatement(gc, table, true))
                 {
                     writer.write(ddlFormats.getScriptStatementTerminator());
                     writer.write("\n");
                 }
             }
 
             if(policy.supportsSequences())
                 renderSqlDdlSequenceStatements(gc, table, false);
 
             if(renderSqlDdlTableStatement(gc, table, false))
             {
                 writer.write(ddlFormats.getScriptStatementTerminator());
                 writer.write("\n");
 
                 renderSqlDdlIndexStatements(gc, table, false);
 
                 if(gc.isCreateCommentObjects())
                     renderSqlDdlCommentObjects(gc, table);
 
                 if(table.getData() != null)
                     tablesWithData.add(table);
             }
 
             gc.getVisitedTables().add(table);
         }
 
         if(gc.getDelayedConstraints().size() > 0)
         {
             writer.write("\n");
             for(Iterator iter = gc.getDelayedConstraints().iterator(); iter.hasNext();)
             {
                 ForeignKey fKey = (ForeignKey) iter.next();
                 renderSqlDdlConstraintClause(gc, null, fKey);
                 writer.write(ddlFormats.getScriptStatementTerminator());
                 writer.write("\n");
             }
         }
 
         if(preStaticDataContent != null)
             preStaticDataContent.process(writer, gc.getValueContext(), prePostDdlTemplateVars);
 
         if(tablesWithData.size() > 0)
         {
             for(int i = 0; i < tablesWithData.size(); i++)
             {
                 Table table = tablesWithData.get(i);
                 Rows dataRows = table.getData();
 
                 writer.write("\n");
                 try
                 {
                     for(int dr = 0; dr < dataRows.size(); dr++)
                     {
                         Row dataRow = dataRows.getRow(dr);
                         String sql = policy.insertValues(null, 0, dataRow.getColumnValues(), null);
                         writer.write(sql);
                         writer.write(ddlFormats.getScriptStatementTerminator());
                         writer.write("\n");
                     }
                 }
                 catch(Exception e)
                 {
                     log.error(e.getMessage(), e);
                 }
             }
         }
 
         if(postDdlContent != null)
             postDdlContent.process(writer, gc.getValueContext(), prePostDdlTemplateVars);
     }
 
     public void renderSqlDdlAbbreviationsCommentBlock(SqlDdlGeneratorContext gc) throws IOException
     {
         Map tablesSortedByAbbrev = new TreeMap();
         int maxTableAbbrevWidth = 0;
 
         Map columnsSortedByAbbrev = new TreeMap();
         int maxColumnAbbrevWidth = 0;
 
         Tables tables = gc.getSchema().getTables();
         for(int t = 0; t < tables.size(); t++)
         {
             Table table = tables.get(t);
             final String tableAbbrev = table.getAbbrev();
             if(!tableAbbrev.equals(table.getName()))
             {
                 if(tableAbbrev.length() > maxTableAbbrevWidth)
                     maxTableAbbrevWidth = tableAbbrev.length();
 
                 if(tablesSortedByAbbrev.containsKey(tableAbbrev))
                 {
                     List dupes = null;
                     if(tablesSortedByAbbrev.get(tableAbbrev) instanceof List)
                         dupes = (List) tablesSortedByAbbrev.get(tableAbbrev);
                     else
                     {
                         dupes = new ArrayList();
                         dupes.add(((Table) tablesSortedByAbbrev.get(tableAbbrev)).getName());
                     }
                     dupes.add(table.getName());
                     tablesSortedByAbbrev.put(tableAbbrev, dupes);
                 }
                 else
                     tablesSortedByAbbrev.put(tableAbbrev, table);
             }
 
             Columns columns = table.getColumns();
             for(int c = 0; c < columns.size(); c++)
             {
                 Column column = columns.get(c);
                 final String colAbbrev = column.getAbbrev();
                 if(!colAbbrev.equals(column.getName()))
                 {
                     final String qualifiedColAbbrev = tableAbbrev + "." + colAbbrev;
                     if(qualifiedColAbbrev.length() > maxColumnAbbrevWidth)
                         maxColumnAbbrevWidth = qualifiedColAbbrev.length();
 
                     if(columnsSortedByAbbrev.containsKey(qualifiedColAbbrev))
                     {
                         List dupes = null;
                         if(columnsSortedByAbbrev.get(qualifiedColAbbrev) instanceof List)
                             dupes = (List) columnsSortedByAbbrev.get(qualifiedColAbbrev);
                         else
                         {
                             dupes = new ArrayList();
                             dupes.add(((Column) columnsSortedByAbbrev.get(qualifiedColAbbrev)).getQualifiedName());
                         }
                         dupes.add(column.getQualifiedName());
                         columnsSortedByAbbrev.put(qualifiedColAbbrev, dupes);
                     }
                     else
                         columnsSortedByAbbrev.put(qualifiedColAbbrev, column);
                 }
             }
         }
 
         TextUtils textUtils = TextUtils.getInstance();
 
         Writer writer = gc.getWriter();
         if(tablesSortedByAbbrev.size() > 0)
         {
             writer.write("\n/*\n");
             writer.write("   Some table names have been abbreviated in constraint and/or index names.\n");
             writer.write("   ========================================================================\n");
 
             for(Iterator i = tablesSortedByAbbrev.entrySet().iterator(); i.hasNext();)
             {
                 Map.Entry entry = (Map.Entry) i.next();
                 boolean dupes = entry.getValue() instanceof List;
 
                 if(dupes)
                     writer.write(">>>");
                 else
                     writer.write("   ");
                 writer.write(textUtils.pad(entry.getKey().toString(), maxTableAbbrevWidth + 1, " "));
                 if(dupes)
                     writer.write("DUPLICATES: " + entry.getValue());
                 else
                     writer.write(((Table) entry.getValue()).getName());
                 writer.write("\n");
             }
             writer.write("*/\n");
         }
 
         if(tablesSortedByAbbrev.size() > 0)
         {
             writer.write("\n/*\n");
             writer.write("   Some column names have been abbreviated in constraint and/or index names.\n");
             writer.write("   =========================================================================\n");
 
             for(Iterator i = columnsSortedByAbbrev.entrySet().iterator(); i.hasNext();)
             {
                 Map.Entry entry = (Map.Entry) i.next();
                 boolean dupes = entry.getValue() instanceof List;
 
                 if(dupes)
                     writer.write(">>>");
                 else
                     writer.write("   ");
                 writer.write(textUtils.pad(entry.getKey().toString(), maxColumnAbbrevWidth + 1, " "));
                 if(dupes)
                     writer.write("DUPLICATES: " + entry.getValue());
                 else
                     writer.write(((Column) entry.getValue()).getQualifiedName());
                 writer.write("\n");
             }
             writer.write("*/\n\n");
         }
     }
 
     public void renderSqlDdlCommentObjects(SqlDdlGeneratorContext gc, Table table) throws IOException
     {
         Writer writer = gc.getWriter();
         SqlDdlFormats ddlFormats = gc.getSqlDdlFormats();
 
         String tableCommentClauseFormat = ddlFormats.getTableCommentClauseFormat();
         String columnCommentClauseFormat = ddlFormats.getColumnCommentClauseFormat();
 
         Map vars = ddlFormats.createJavaExpressionVars();
         vars.put("table", table);
 
         if(tableCommentClauseFormat != null)
         {
             JavaExpressionText jet = new JavaExpressionText(tableCommentClauseFormat, vars);
             writer.write(jet.getFinalText(gc.getValueContext()));
             writer.write(ddlFormats.getScriptStatementTerminator());
             writer.write("\n");
         }
 
         if(columnCommentClauseFormat != null)
         {
             Columns columns = table.getColumns();
             for(int i = 0; i < columns.size(); i++)
             {
                 Column column = columns.get(i);
                 vars.put("column", column);
 
                 JavaExpressionText jet = new JavaExpressionText(ddlFormats.getColumnCommentClauseFormat(), vars);
                 writer.write(jet.getFinalText(gc.getValueContext()));
                 writer.write(ddlFormats.getScriptStatementTerminator());
                 writer.write("\n");
             }
         }
     }
 
     public boolean renderSqlDdlSequenceStatements(SqlDdlGeneratorContext gc, Table table, boolean isDropSql) throws IOException
     {
         Writer writer = gc.getWriter();
         SqlDdlFormats ddlFormats = gc.getSqlDdlFormats();
 
         String format = isDropSql
                         ? ddlFormats.getDropSequenceStatementFormat() : ddlFormats.getCreateSequenceStatementFormat();
 
         int seqCount = 0;
         Map vars = ddlFormats.createJavaExpressionVars();
         vars.put("table", table);
 
         JavaExpressionText jet = new JavaExpressionText(format, vars);
 
         try
         {
             Columns columns = table.getColumns();
             for(int i = 0; i < columns.size(); i++)
             {
                 Column column = columns.get(i);
                 if(column instanceof AutoIncColumn)
                 {
                     jet.getJexlContext().getVars().put("column", column);
                     String clause = jet.getFinalText(gc.getValueContext());
 
                     writer.write(clause);
                     writer.write(ddlFormats.getScriptStatementTerminator());
                     writer.write("\n");
 
                     seqCount++;
                 }
             }
         }
         catch(Exception e)
         {
             log.error("Error in " + AnsiDatabasePolicy.class + ".renderSqlDdlSequenceStatements(): " + e.getMessage(), e);
             throw new IOException(e.toString());
         }
 
         return seqCount > 0;
     }
 
     public boolean renderSqlDdlIndexStatements(SqlDdlGeneratorContext gc, Table table, boolean isDropSql) throws IOException
     {
         Writer writer = gc.getWriter();
         SqlDdlFormats ddlFormats = gc.getSqlDdlFormats();
 
         String format = isDropSql
                         ? ddlFormats.getDropIndexStatementFormat() : ddlFormats.getCreateIndexStatementFormat();
 
         Map vars = ddlFormats.createJavaExpressionVars();
         vars.put("table", table);
 
         JavaExpressionText jet = new JavaExpressionText(format, vars);
 
         Indexes tableIndexes = table.getIndexes();
         Indexes createIndexes = new IndexesCollection();
         createIndexes.merge(tableIndexes);
 
         if(ddlFormats.isCreatePrimaryKeyIndex())
         {
             PrimaryKeyColumns pkCols = table.getPrimaryKeyColumns();
             if(pkCols != null && pkCols.size() > 0)
             {
                 Index index = table.createIndex();
                 index.setName("PK_" + table.getName());
                 index.setColumns(pkCols.getOnlyNames(","));
                 createIndexes.add(index);
             }
         }
 
         if(ddlFormats.isCreateParentKeyIndex())
         {
             Columns prCols = table.getParentRefColumns();
             if(prCols != null && prCols.size() > 0)
             {
                 for(int i = 0; i < prCols.size(); i++)
                 {
                     Index index = table.createIndex();
                     index.setName("PR_" + table.getAbbrev() + "_" + prCols.get(i).getName());
                     index.setColumns(prCols.get(i).getName());
                     createIndexes.add(index);
                 }
             }
         }
 
         DatabasePolicy policy = gc.getDatabasePolicy();
         try
         {
             for(int i = 0; i < createIndexes.size(); i++)
             {
                 Index index = createIndexes.get(i);
                 jet.getJexlContext().getVars().put("index", index);
 
                 String statement = jet.getFinalText(gc.getValueContext());
 
                 writer.write(statement);
 
                 DbmsSqlText appendExpr = index.getSqlDataDefns().getCreateIndexAppendParams().getByDbmsOrAnsi(policy);
                 if(appendExpr != null)
                     writer.write(appendExpr.getSql(gc.getValueContext()));
                 else
                 {
                     String appendParamsFormat = ddlFormats.getCreateIndexAppendParamsFormat();
                     if(appendParamsFormat != null)
                     {
                         JavaExpressionText appenderJet = new JavaExpressionText(appendParamsFormat, vars);
                         writer.write(appenderJet.getFinalText(gc.getValueContext()));
                     }
                 }
 
                 writer.write(ddlFormats.getScriptStatementTerminator());
                 writer.write("\n");
             }
         }
         catch(Exception e)
         {
             log.error("Error in " + AnsiDatabasePolicy.class + ".renderSqlDdlIndexStatements(): " + e.getMessage(), e);
             throw new IOException(e.toString());
         }
 
         return createIndexes.size() > 0;
     }
 
     public void renderSqlDdlConstraintClause(SqlDdlGeneratorContext gc, Table table, ForeignKey fkey) throws IOException
     {
         Writer writer = gc.getWriter();
         SqlDdlFormats ddlFormats = gc.getSqlDdlFormats();
 
         String format = table != null
                         ? ddlFormats.getFkeyConstraintTableClauseFormat()
                         : ddlFormats.getfkeyConstraintAlterTableStatementFormat();
         if(format == null)
             return;
 
         Map vars = ddlFormats.createJavaExpressionVars();
         vars.put("table", table);
         vars.put("fkey", fkey);
 
         JavaExpressionText jet = new JavaExpressionText(format, vars);
         writer.write(jet.getFinalText(gc.getValueContext()));
     }
 
     public boolean renderSqlDdlTableStatement(SqlDdlGeneratorContext gc, Table table, boolean isDropSql) throws IOException
     {
         if(table.getColumns().size() == 0)
             return false;
 
         TextUtils textUtils = TextUtils.getInstance();
         Writer writer = gc.getWriter();
         SqlDdlFormats ddlFormats = gc.getSqlDdlFormats();
 
         String format = isDropSql ? ddlFormats.getDropTableStatementFormat() : ddlFormats.getCreateTableClauseFormat();
         if(format == null)
             return false;
 
         Map vars = ddlFormats.createJavaExpressionVars();
         vars.put("table", table);
 
         Set tableConstraints = new HashSet();
         Set tableDelayedConstraints = new HashSet();
 
         JavaExpressionText jet = new JavaExpressionText(format, vars);
         try
         {
             writer.write(jet.getFinalText(gc.getValueContext()));
 
             // the rest of the text from here on is for create table, not drop table
             if(isDropSql)
                 return true;
 
             writer.write("\n");
             writer.write("(\n");
 
             final String indent = "    ";
 
             Columns columns = table.getColumns();
             int lastColumn = columns.size() - 1;
             for(int i = 0; i < columns.size(); i++)
             {
                 Column column = columns.get(i);
 
                 writer.write(indent);
                 renderSqlDdlColumnCreateClause(gc, column);
 
                 if(gc.getDatabasePolicy().supportsForeignKeyConstraints())
                 {
                     ForeignKey fKey = column.getForeignKey();
                     if(fKey != null && !fKey.isLogical())
                     {
                         /*
                           1) if visitedTables is not provided, we'll assume that we'll place the foreign key contraints inside the table
                           2) if visitedTables is provided, we'll assume that we'll only place the fkey constraints inside the table if the
                              table has already been defined; otherwise, we'll assume fkeys will be created later using "alter table"
                         */
 
                         if(gc.getVisitedTables().contains(fKey.getReferencedColumns().getFirst().getTable()))
                             tableConstraints.add(fKey);
                         else
                         {
                             gc.getDelayedConstraints().add(fKey);
                             tableDelayedConstraints.add(fKey);
                         }
                     }
                 }
 
                 if(i < lastColumn)
                     writer.write(",");
                 else if(i == lastColumn && tableConstraints.size() > 0)
                     writer.write(",");
 
                 writer.write(" /* ");
                 writer.write(textUtils.getRelativeClassName(BasicColumn.class, column.getClass()));
                 if(!column.getAbbrev().equals(column.getName()))
                     writer.write(", Abbrev '" + column.getAbbrev() + "'");
                 writer.write(" */");
 
                 writer.write("\n");
             }
 
             if(tableConstraints.size() > 0)
             {
                 if(ddlFormats.isBlankLineAllowedInCreateTable())  // create a separator between columns and in-table constraints
                     writer.write("\n");
 
                 int lastConstr = tableConstraints.size() - 1;
                 int constrIndex = 0;
                 for(Iterator constr = tableConstraints.iterator(); constr.hasNext();)
                 {
                     ForeignKey fKey = (ForeignKey) constr.next();
 
                     writer.write(indent);
 
                     renderSqlDdlConstraintClause(gc, table, fKey);
                     if(constrIndex < lastConstr)
                         writer.write(",");
 
                     writer.write("\n");
                     constrIndex++;
                 }
 
                 if(tableDelayedConstraints.size() > 0)
                 {
                    if(ddlFormats.isBlankLineAllowedInCreateTable()) writer.write("\n");
 
                     for(Iterator constr = tableDelayedConstraints.iterator(); constr.hasNext();)
                     {
                         ForeignKey fKey = (ForeignKey) constr.next();
 
                         writer.write(indent);
 
                         writer.write("/* DELAYED: ");
                         renderSqlDdlConstraintClause(gc, table, fKey);
                         writer.write(" (" + fKey.getReferencedColumns().getFirst().getTable().getName() + " table not created yet) */");
 
                         writer.write("\n");
                         constrIndex++;
                     }
                 }
             }
 
             writer.write(")");
 
             DatabasePolicy policy = gc.getDatabasePolicy();
             DbmsSqlText appendExpr = table.getSqlDataDefns().getCreateTableAppendParams().getByDbmsOrAnsi(policy);
             if(appendExpr != null)
                 writer.write(appendExpr.getSql(gc.getValueContext()));
             else
             {
                 String appendParamsFormat = ddlFormats.getCreateTableAppendParamsFormat();
                 if(appendParamsFormat != null)
                 {
                     JavaExpressionText appenderJet = new JavaExpressionText(appendParamsFormat, vars);
                     writer.write(appenderJet.getFinalText(gc.getValueContext()));
                 }
             }
         }
         catch(Exception e)
         {
             log.error("Error in " + AnsiDatabasePolicy.class + ".renderSqlDdlTableStatement(): " + e.getMessage(), e);
             throw new IOException(e.toString());
         }
 
         return true;
     }
 
     public void renderSqlDdlColumnCreateClause(SqlDdlGeneratorContext gc, Column column) throws IOException
     {
         Writer writer = gc.getWriter();
         DatabasePolicy policy = gc.getDatabasePolicy();
 
         SqlDataDefns sqlDataDefns = column.getSqlDdl();
         DbmsSqlText defineExpr = sqlDataDefns.getSqlDefns().getByDbmsOrAnsi(policy);
         DbmsSqlText defaultExpr = sqlDataDefns.getDefaultSqlExprValues().getByDbmsOrAnsi(policy);
 
         writer.write(column.getName());
         writer.write(" ");
 
         if(defineExpr != null)
             writer.write(defineExpr.getSql(gc.getValueContext()).toUpperCase());
         else
             writer.write("No definition found in column '" + column + "' for policy '" + policy.getDbmsIdentifier() + "' or ANSI. Available: " + sqlDataDefns.getSqlDefns().getAvailableDbmsIds());
 
         if(defaultExpr != null)
         {
             writer.write(" DEFAULT ");
             writer.write(defaultExpr.getSql(gc.getValueContext()));
         }
 
         if(column.isPrimaryKey())
             writer.write(" PRIMARY KEY");
 
         if(column.isRequiredByApp() || column.isRequiredByDbms())
             writer.write(" NOT NULL");
     }
 }
