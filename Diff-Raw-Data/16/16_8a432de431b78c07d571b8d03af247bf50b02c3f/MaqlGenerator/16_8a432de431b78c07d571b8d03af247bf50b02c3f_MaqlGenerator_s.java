 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.modeling.generator;
 
 import static com.gooddata.modeling.model.SourceColumn.LDM_TYPE_ATTRIBUTE;
 import static com.gooddata.modeling.model.SourceColumn.LDM_TYPE_CONNECTION_POINT;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.gooddata.modeling.generator.MaqlGenerator.State.Attribute;
 import com.gooddata.modeling.generator.MaqlGenerator.State.Column;
 import com.gooddata.modeling.generator.MaqlGenerator.State.ConnectionPoint;
 import com.gooddata.modeling.generator.MaqlGenerator.State.Label;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.naming.N;
 import com.gooddata.util.StringUtil;
 
 /**
  * GoodData MAQL Generator generates the MAQL from the LDM schema object
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class MaqlGenerator {
 
     private static Logger l = Logger.getLogger(MaqlGenerator.class);
 
     protected final SourceSchema schema;
     protected final String schemaName, lsn;
     protected final String factsOfAttrMaqlDdl;
 
     private boolean synchronize = true; // should generateMaql*() methods append SYNCHRONIZE commands?
 
     public MaqlGenerator(SourceSchema schema) {
         this.schema = schema;
         this.schemaName = schema.getName();
         this.lsn = schema.getTitle();
         this.factsOfAttrMaqlDdl = createFactOfMaqlDdl(schema.getName());
     }
 
     /**
      * Generates the MAQL from the schema
      *
      * @return the MAQL as a String
      */
     public String generateMaqlCreate() {
         String script = "# This is MAQL script that generates project logical model.\n# See the MAQL documentation " +
                "at http://developer.gooddata.com/api/maql-ddl.html for more details.\n\n";
         script += "# Create dataset. Dataset groups all following logical model elements together.\n";
         script += "CREATE DATASET {" + schema.getDatasetName() + "} VISUAL(TITLE \"" + lsn + "\");\n\n";
         script += generateFoldersMaqlDdl(schema.getColumns());
 
         script += generateMaqlAdd(schema.getColumns(), new ArrayList<SourceColumn>(), true);
 
         return script;
     }
 
     /**
      * should generateMaql*() methods append SYNCHRONIZE commands?
      *
      * @param synchronize
      */
     public void setSynchronize(final boolean synchronize) {
         this.synchronize = synchronize;
     }
 
     /**
      * Generate MAQL for specified (new) columns
      *
      * @return MAQL as String
      */
     public String generateMaqlAdd(Iterable<SourceColumn> newColumns, Iterable<SourceColumn> knownColumns) {
         return generateMaqlAdd(newColumns, knownColumns, false);
     }
 
     /**
      * Creates attribute table name
      *
      * @param schema source schema
      * @param sc     source column
      * @return the attribute table name
      */
     public static String createAttributeTableName(SourceSchema schema, SourceColumn sc) {
         final String ssn = schema.getName();
         return "d_" + ssn + "_" + sc.getName();
     }
 
     /**
      * Generate MAQL DROP statement for selected columns
      *
      * @param columns list of columns
      * @return MAQL as String
      */
     public String generateMaqlDrop(List<SourceColumn> columns, Iterable<SourceColumn> knownColumns) {
         // generate attributes and facts
         State state = new State();
         for (SourceColumn column : columns) {
             state.processColumn(column);
         }
 
         StringBuffer nonLabelsScript = new StringBuffer("# Drop attributes.\n");
 
         for (final Column c : state.attributes.values()) {
             nonLabelsScript.append(c.generateMaqlDdlDrop());
         }
         nonLabelsScript.append("# Drop facts.\n");
         for (final Column c : state.facts) {
             nonLabelsScript.append(c.generateMaqlDdlDrop());
         }
         nonLabelsScript.append("# Drop dates.\n# Dates are represented as facts.\n");
         nonLabelsScript.append("# Dates are also connected to the date dimensions.\n");
         for (final Column c : state.dates) {
             nonLabelsScript.append(c.generateMaqlDdlDrop());
         }
         nonLabelsScript.append("# Drop references.\n# References connect the dataset to other datasets.\n");
         for (final Column c : state.references) {
             nonLabelsScript.append(c.generateMaqlDdlDrop());
         }
 
         state.addKnownColumns(knownColumns);
 
         StringBuilder script = new StringBuilder();
         script.append("# Drop labels\n");
         for (final Column c : state.labels) {
             script.append(c.generateMaqlDdlDrop());
         }
         script.append(nonLabelsScript);
 
         // finally
         if (synchronize) {
             script.append("# Synchronize the storage and data loading interfaces with the new logical model.\n");
             script.append("SYNCHRONIZE {" + schema.getDatasetName() + "} PRESERVE DATA;\n\n");
         }
 
         return script.toString();
     }
 
     /**
      * Generates the single <tt>SYNCHRONIZE</tt> command based on the given
      * schema name. Ignores the <tt>synchronize</tt> flag (set by the {@link #setSynchronize(boolean)}
      * method)
      *
      * @return
      */
     public String generateMaqlSynchronize() {
         StringBuffer script = new StringBuffer();
         script.append("# Synchronize the storage and data loading interfaces with the new logical model.\n");
         script.append("SYNCHRONIZE {" + schema.getDatasetName() + "} PRESERVE DATA;\n\n");
         return script.toString();
     }
 
     /**
      * Generate MAQL for selected (new) columns
      *
      * @param newColumns    list of columns
      * @param createFactsOf create the facts of attribute
      * @return MAQL as String
      */
     protected String generateMaqlAdd(Iterable<SourceColumn> newColumns, Iterable<SourceColumn> knownColumns, boolean createFactsOf) {
 
         // generate attributes and facts
         State state = new State();
         for (SourceColumn column : newColumns) {
             state.processColumn(column);
         }
 
         String script = "# Create attributes.\n# Attributes are categories that are used for slicing and dicing the " +
                 "numbers (facts)\n";
 
         ConnectionPoint connectionPoint = null; // hold the CP's default label to be created at the end
         for (final Column c : state.attributes.values()) {
             script += c.generateMaqlDdlAdd();
             if (c instanceof ConnectionPoint) {
                 connectionPoint = (ConnectionPoint) c;
             } else {
                 final Attribute a = (Attribute) c;
                 script += a.generateOriginalLabelMaqlDdl();
                 script += a.generateDefaultLabelMaqlDdl();
             }
         }
         script += "# Create facts.\n# Facts are numbers that are aggregated by attributes.\n";
         for (final Column c : state.facts) {
             script += c.generateMaqlDdlAdd();
         }
         script += "# Create date facts.\n# Dates are represented as facts.\n# Dates are also connected to the " +
                 "date dimensions.\n";
         for (final Column c : state.dates) {
             script += c.generateMaqlDdlAdd();
         }
         script += "# Create references.\n# References connect the dataset to other datasets.\n";
         for (final Column c : state.references) {
             script += c.generateMaqlDdlAdd();
         }
 
         if (createFactsOf & (!state.hasCp)) {
             script += "# The facts of attribute is sort of dataset identifier,\n";
             script += "# it is used for COUNT aggregations.\n";
             // generate the facts of / record id special attribute
             script += "CREATE ATTRIBUTE " + factsOfAttrMaqlDdl + " VISUAL(TITLE \""
                     + "Records of " + lsn + "\") AS KEYS {" + getFactTableName() + "." + state.factsOfPrimaryColumn + "} FULLSET;\n";
             script += "ALTER DATASET {" + schema.getDatasetName() + "} ADD {attr." + schemaName + ".factsof};\n\n";
         }
 
         state.addKnownColumns(knownColumns);
 
         // labels last
         boolean cpDefLabelSet = false;
         boolean cpSortSet = false;
         for (final Column c : state.labels) {
             script += c.generateMaqlDdlAdd();
             Label l = (Label) c;
             if (!cpDefLabelSet && (connectionPoint != null) && l.attr.identifier.equals(connectionPoint.identifier)) {
                 script += l.generateMaqlDdlDefaultLabel();
                 cpDefLabelSet = true;
             }
             if (!cpSortSet && l.column.getName().equals(StringUtil.toIdentifier(l.attr.column.getSortLabel()))) {
                 script += l.generateMaqlSortLabel();
                 cpSortSet = true;
             }
         }
 
         // CP's default label after all other labels
         if (connectionPoint != null) {
             script += connectionPoint.generateOriginalLabelMaqlDdl();
         }
 
         // finally
         if (synchronize) {
             script += "# Synchronize the storage and data loading interfaces with the new logical model.\n";
             script += "SYNCHRONIZE {" + schema.getDatasetName() + "};\n\n";
         }
         return script;
     }
     
     /**
      * Generate MAQL to alter titles of provided columns
      * @param columns
      * @return
      */
     public String generateMaqlUpdateTitles(Iterable<SourceColumn> columns) {
     	StringBuffer maql = new StringBuffer("");
     	State state = new State();
     	for (final SourceColumn sc : columns) {
     		state.processColumn(sc);
     	}
     	for (final Column c : state.getColumns()) {
     		maql.append(c.generateMaqlAlterTitle());
     	}
     	return maql.toString();
     }
 
     /**
      * Generate MAQL to alter specified data types of given columns
      * @param columns
      * @return
      */
     public String generateMaqlUpdateDataTypes(Iterable<SourceColumn> columns) {
     	StringBuffer maql = new StringBuffer("");
     	State state = new State();
     	for (final SourceColumn sc : columns) {
     		state.processColumn(sc);
     	}
     	for (final Column c : state.getColumns()) {
     		maql.append(c.generateMaqlAlterDataType());
     	}
     	return maql.toString();
     }
 
     /**
      * Generate MAQL to alter titles of provided columns
      * @param columns
      * @return
      */
     public String generateMaqlSorting(Iterable<SourceColumn> columns) {
     	StringBuffer maql = new StringBuffer("");
     	State state = new State();
     	for (final SourceColumn sc : columns) {
     		state.processColumn(sc);
     	}
     	for (final Column c : state.labels) {
     		Label l = (Label) c;
             maql.append(l.generateMaqlSortLabel());
     	}
     	return maql.toString();
     }
 
     /**
      * Generate MAQL folders for specified columns
      *
      * @param columns list of columns
      * @return MAQL as String
      */
     protected String generateFoldersMaqlDdl(List<SourceColumn> columns) {
         final ArrayList<String> attributeFolders = new ArrayList<String>();
         final ArrayList<String> factFolders = new ArrayList<String>();
 
         for (SourceColumn column : columns) {
             String folder = column.getFolder();
             if (folder != null && folder.length() > 0) {
                 String ldmType = column.getLdmType();
                 if(ldmType != null && ldmType.length()>0) {
                     if (column.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_ATTRIBUTE) ||
                             column.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_LABEL) ||
                             column.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_HYPERLINK) ||
                             column.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_CONNECTION_POINT) ||
                             column.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_REFERENCE) ||
                             column.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_DATE)) {
                         if (!attributeFolders.contains(folder))
                             attributeFolders.add(folder);
                     }
                     if (column.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_FACT) ||
                             column.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_DATE)) {
                         if (!factFolders.contains(folder))
                             factFolders.add(folder);
                     }
                 }
                 else {
                     throw new IllegalArgumentException("Column " + column.getName()+ " doesn't contain the LDM type specification.");
                 }
             }
         }
 
         String script = "";
         if (!attributeFolders.isEmpty() || !factFolders.isEmpty())
             script += "# Create the folders that group attributes and facts.\n";
         // Generate statements for the ATTRIBUTE folders
         for (String folder : attributeFolders) {
             String sfn = StringUtil.toIdentifier(folder);
             String lfn = folder;
             script += "CREATE FOLDER {dim." + sfn + "} VISUAL(TITLE \"" + lfn + "\") TYPE ATTRIBUTE;\n";
         }
         script += "\n";
 
         // Generate statements for the FACT folders
         for (String folder : factFolders) {
             String sfn = StringUtil.toIdentifier(folder);
             String lfn = folder;
             script += "CREATE FOLDER {ffld." + sfn + "} VISUAL(TITLE \"" + lfn + "\") TYPE FACT;\n";
         }
 
         script += "\n";
         return script;
     }
 
     /**
      * Generate fact table name
      *
      * @return fact table name
      */
     private String getFactTableName() {
         return N.FCT_PFX + schemaName;
     }
 
     /**
      * Generate the MAQL for the facts of attribute
      *
      * @param schemaName schema name
      * @return facts of attribute MAQL DDL
      */
     private static String createFactOfMaqlDdl(String schemaName) {
         return "{attr." + schemaName + "." + N.FACTS_OF + "}";
     }
 
 
     class State {
 
         private Map<String, Attribute> attributes = new HashMap<String, Attribute>();
         private List<Fact> facts = new ArrayList<Fact>();
         private List<Label> labels = new ArrayList<Label>();
         private List<DateColumn> dates = new ArrayList<DateColumn>();
         private List<Reference> references = new ArrayList<Reference>();
         private boolean hasCp = false;
         private String factsOfPrimaryColumn = N.ID;
 
         /**
          * Main loop. Process all columns in the schema
          *
          * @param column source columns
          */
         private void processColumn(SourceColumn column) {
             if (column.getLdmType().equals(SourceColumn.LDM_TYPE_ATTRIBUTE)) {
                 Attribute attr = new Attribute(column);
                 attributes.put(attr.columnName, attr);
             } else if (column.getLdmType().equals(SourceColumn.LDM_TYPE_FACT)) {
                 facts.add(new Fact(column));
             } else if (column.getLdmType().equals(SourceColumn.LDM_TYPE_DATE)) {
                 if (column.getSchemaReference() != null && column.getSchemaReference().length() > 0) {
                     dates.add(new DateColumn(column));
                 } else {
                     Attribute attr = new Attribute(column);
                     attributes.put(attr.columnName, attr);
                 }
             } else if (column.getLdmType().equals(SourceColumn.LDM_TYPE_LABEL)) {
                 labels.add(new Label(column));
             } else if (column.getLdmType().equals(SourceColumn.LDM_TYPE_HYPERLINK)) {
                 labels.add(new Hyperlink(column));
             } else if (column.getLdmType().equals(SourceColumn.LDM_TYPE_REFERENCE)) {
                 references.add(new Reference(column));
             } else if (column.getLdmType().equals(SourceColumn.LDM_TYPE_IGNORE)) {
                 ; // intentionally do nothing
             } else if (column.getLdmType().equals(SourceColumn.LDM_TYPE_CONNECTION_POINT)) {
                 processConnectionPoint(column);
             } else {
                 throw new IllegalArgumentException("Unsupported ldm type '" + column.getLdmType() + "'.");
             }
         }
 
         private void addKnownColumns(Iterable<SourceColumn> knownColumns) { // attributes only
             for (SourceColumn column : knownColumns) {
                 if (LDM_TYPE_ATTRIBUTE.equals(column.getLdmType())) {
                     attributes.put(column.getName(), new Attribute(column));
                 } else if (LDM_TYPE_CONNECTION_POINT.equals(column.getLdmType())) {
                     processConnectionPoint(column);
                 }
             }
         }
 
         /**
          * Processes connection point column
          *
          * @param column source column
          */
         private void processConnectionPoint(SourceColumn column) {
             if (column.getLdmType().equals(SourceColumn.LDM_TYPE_CONNECTION_POINT)) {
                 if (hasCp) {
                     throw new IllegalStateException("Only one connection point per dataset is allowed. "
                             + "Consider declaring the duplicate connection points as labels of the main connection point.");
                 }
                 ConnectionPoint connectionPoint = new ConnectionPoint(column);
                 attributes.put(connectionPoint.columnName, connectionPoint);
             }
         }
         
         private Iterable<Column> getColumns() {
         	List<Column> result = new ArrayList<MaqlGenerator.State.Column>();
         	result.addAll(attributes.values());
         	result.addAll(facts);
         	result.addAll(labels);
         	result.addAll(dates);
         	return result;
         }
 
         // columns
 
         abstract class Column {
             protected final SourceColumn column;
             protected final String columnName, lcn;
             protected String identifier;
             protected final boolean reference;
 
             Column(SourceColumn column, String idprefix) {
                 this.column = column;
                 this.columnName = column.getName();
                 this.lcn = column.getTitle();
                 this.reference = (column.getSchemaReference() != null) && !schemaName.equals(column.getSchemaReference());
                 String schema = reference ? column.getSchemaReference() : schemaName;
                 this.identifier = idprefix + "." + schema + "." + columnName;
             }
 
             protected String createForeignKeyMaqlDdl() {
                 return "{" + getFactTableName() + "." + columnName + "_" + N.ID + "}";
             }
 
             public abstract String generateMaqlDdlAdd();
 
             public String generateMaqlAlterTitle() {
             	return ""; // intentionally left blank - no applicable MAQL DDL
             }
 
             public String generateMaqlAlterDataType() {
             	return ""; // intentionally left blank - no applicable MAQL DDL
             }
 
             public String generateMaqlDdlDrop() {
                 return "DROP {" + identifier + "} CASCADE;\n\n";
             }
         }
 
 
         // attributes
         class Attribute extends Column {
 
             protected final String table;
             protected final String defaultLabelIdentifier;
             protected final String defaultLabelDdl;
 
             Attribute(SourceColumn column, String table) {
                 super(column, "attr");
                 this.table = (table == null) ? createAttributeTableName(schema, column) : table;
                 this.defaultLabelIdentifier = "label." + schemaName + "." + columnName;
                 this.defaultLabelDdl = "{" + defaultLabelIdentifier + "} VISUAL(TITLE \""
                         + lcn + "\") AS {" + this.table + "." + N.NM_PFX + columnName + "}";
             }
 
             Attribute(SourceColumn column) {
                 this(column, null);
             }
 
             public String generateMaqlDdlAdd() {
                 String folderStatement = "";
                 String folder = column.getFolder();
                 if (folder != null && folder.length() > 0) {
                     String sfn = StringUtil.toIdentifier(folder);
                     folderStatement = ", FOLDER {dim." + sfn + "}";
                 }
 
                 String script = "";
                 String fks = createForeignKeyMaqlDdl();
                 if (!reference) {
                     script += "CREATE ATTRIBUTE {" + identifier + "} VISUAL(TITLE \"" + lcn
                             + "\"" + folderStatement + ") AS KEYS {" + table + "." + N.ID + "} FULLSET";
                     if ((fks != null) && (fks.length() > 0)) {
                         script += ", " + fks;
                     }
                 } else if ((fks != null) && (fks.length() > 0)) {
                     script += String.format("ALTER ATTRIBUTE {%s} ADD KEYS %s", identifier, fks);
                 }
 
                 script += ";\n";
 
                 if (!reference) { // don't add cross-dataset attributes
                     script += "ALTER DATASET {" + schema.getDatasetName() + "} ADD {" + identifier + "};\n";
                 }
 
                 String dataType = column.getDataType();
                 if (SourceColumn.LDM_IDENTITY.equalsIgnoreCase(column.getTransformation()))
                     dataType = SourceColumn.IDENTITY_DATATYPE;
                 if (dataType != null && dataType.length() > 0) {
                     script += "ALTER DATATYPE {" + table + "." + N.NM_PFX + columnName + "} " + dataType + ";\n";
                 } else {
                     script += "\n";
                 }
 
                 return script;
             }
             
             public String generateMaqlAlterTitle() {
             	return "ALTER ATTRIBUTE {" + identifier + "} VISUAL(TITLE \"" + lcn + "\");\n";
             }
 
             public String generateMaqlAlterDataType() {
             	if (column.getDataType() != null) {
             		return "ALTER DATATYPE {" + table + "." + N.NM_PFX + columnName + "} " + column.getDataType() + ";\n";
             	}
             	return "";
             }
 
             @Override
             public String generateMaqlDdlDrop() {
                 if (reference) {
                     String fks = createForeignKeyMaqlDdl();
                     return String.format("ALTER ATTRIBUTE {%s} DROP KEYS %s;\n", identifier, fks);
                 }
                 return super.generateMaqlDdlDrop();
             }
 
             public String generateOriginalLabelMaqlDdl() {
                 if (!reference) {
                     String script = "ALTER ATTRIBUTE {" + identifier + "} ADD LABELS " + defaultLabelDdl + "; \n";
                     return script;
                 }
                 return "";
             }
 
             public String generateDefaultLabelMaqlDdl() {
                 if (!reference) {
                     return "ALTER ATTRIBUTE  {" + identifier + "} DEFAULT LABEL {" + defaultLabelIdentifier + "};\n";
                 }
                 return "";
             }
         }
 
         //facts
         private class Fact extends Column {
         	private final String fcolname;
 
             Fact(SourceColumn column) {
                 super(column, "fact");
                 // unfortunate backward compatibility fix
                 // we have converted the date/time facts and the time attribute to explicit schema elements
                 // we needed to distinguish these schema columns, so we have added the suffixes
                 // we need to strip the suffixes here to make sure that the identifiers have backward compatible names
                 if (column.isDateFact()) {
                     this.identifier = N.DT + "." + schemaName + "." + columnName.replace(N.DT_SLI_SFX, "");
                 }
                 if (column.isTimeFact()) {
                     this.identifier = N.TM + "." + N.DT + "." + schemaName + "." + columnName.replace(N.TM_SLI_SFX, "");
                 }
 
                 String _fcolname = N.FCT_PFX + columnName;
                 if (column.isDateFact())
                     _fcolname = N.DT_PFX + columnName.replace(N.DT_SLI_SFX, "");
                 if (column.isTimeFact())
                     _fcolname = N.TM_PFX + columnName.replace(N.TM_SLI_SFX, "");
                 fcolname = _fcolname;
 
             }
 
             @Override
             public String generateMaqlDdlAdd() {
                 String folderStatement = "";
                 String folder = column.getFolder();
                 if (folder != null && folder.length() > 0) {
                     String sfn = StringUtil.toIdentifier(folder);
                     folderStatement = ", FOLDER {ffld." + sfn + "}";
                 }
                 // unfortunate backward compatibility fix
                 // we have converted the date/time facts and the time attribute to explicit schema elements
                 // we needed to keep the dt_ and tm_ prefixes instead of the f_
                 String script = "CREATE FACT {" + identifier + "} VISUAL(TITLE \"" + lcn
                         + "\"" + folderStatement + ") AS {" + getFactTableName() + "." + fcolname + "};\n"
                         + "ALTER DATASET {" + schema.getDatasetName() + "} ADD {" + identifier + "};\n";
                 String dataType = column.getDataType();
                 if (SourceColumn.LDM_IDENTITY.equalsIgnoreCase(column.getTransformation()))
                     dataType = SourceColumn.IDENTITY_DATATYPE;
                 if (dataType != null && dataType.length() > 0) {
                     script += "ALTER DATATYPE {" + getFactTableName() + "." + fcolname + "} " + dataType + ";\n";
                 } else {
                     script += "\n";
                 }
                 return script;
             }
             
             public String generateMaqlAlterTitle() {
             	return "ALTER FACT {" + identifier + "} VISUAL(TITLE \"" + lcn + "\");\n";
             }
 
             public String generateMaqlAlterDataType() {
             	if (column.getDataType() != null) {
             		return "ALTER DATATYPE {" + getFactTableName() + "." + fcolname + "} " + column.getDataType() + ";\n";
             	}
             	return "";
             }
         }
 
         //labels
         class Label extends Column {
 
             final String scnPk;
             Attribute attr;
 
             Label(SourceColumn column) {
                 super(column, "label");
                 scnPk = column.getReference();
             }
 
             @Override
             public String generateMaqlDdlAdd() {
                 attr = attributes.get(scnPk);
                 if (attr == null) {
                     throw new IllegalArgumentException("Label " + columnName + " points to non-existing attribute " + scnPk);
                 }
                 String script = "# Add labels to attributes\n";
                 script += "ALTER ATTRIBUTE {attr." + schemaName + "." + scnPk + "} ADD LABELS {label." + schemaName + "." + scnPk + "."
                         + columnName + "} VISUAL(TITLE \"" + lcn + "\") AS {" + attr.table + "." + N.NM_PFX + columnName + "};\n";
 
                 String dataType = column.getDataType();
                 if (SourceColumn.LDM_IDENTITY.equalsIgnoreCase(column.getTransformation()))
                     dataType = SourceColumn.IDENTITY_DATATYPE;
                 if (dataType != null && dataType.length() > 0) {
                     script += "ALTER DATATYPE {" + attr.table + "." + N.NM_PFX + columnName + "} " + dataType + ";\n";
                 } else {
                     script += "\n";
                 }
                 return script;
             }
 
             public String generateMaqlDdlDrop() {
                 attr = attributes.get(scnPk);
                 if (attr == null) {
                     throw new IllegalArgumentException("Label " + columnName + " points to non-existing attribute " + scnPk);
                 }
                 String script = "# Drop labels from attributes.\n";
                 final String labelId = getLabelId();
                 script += "ALTER ATTRIBUTE  {" + attr.identifier + "} DROP LABELS {" + labelId + "};\n";
                 return script;
             }
 
             public String generateMaqlDdlDefaultLabel() {
                 attr = attributes.get(scnPk);
                 if (attr == null) {
                     throw new IllegalArgumentException("Label " + columnName + " points to non-existing attribute " + scnPk);
                 }
                 // TODO why is this different than this.identifier?
                 final String labelId = getLabelId();
                 return "ALTER ATTRIBUTE  {" + attr.identifier + "} DEFAULT LABEL {" + labelId + "};\n";
             }
 
             public String generateMaqlSortLabel() {
                 attr = attributes.get(scnPk);
                 if (attr == null) {
                     throw new IllegalArgumentException("Label " + columnName + " points to non-existing attribute " + scnPk);
                 }
                 if (column.getName().equals(StringUtil.toIdentifier(attr.column.getSortLabel()))) {
                 	String sortOrder = attr.column.getSortOrder();
 	                final String labelId = getLabelId();
 	                if(sortOrder == null || sortOrder.length() <=0 || !(SourceColumn.LDM_SORT_ORDER_ASC.equals(sortOrder) &&
 	                        SourceColumn.LDM_SORT_ORDER_DESC.equals(sortOrder)))
 	                    sortOrder = SourceColumn.LDM_SORT_ORDER_ASC;
 	                return "ALTER ATTRIBUTE  {" + attr.identifier + "} ORDER BY {" + labelId + "} "+sortOrder+";\n";
                 }
                 return "";
             }
 
             public String generateMaqlAlterTitle() {
                 attr = attributes.get(scnPk);
                 if (attr == null) {
                     throw new IllegalArgumentException("Label " + columnName + " points to non-existing attribute " + scnPk);
                 }
                 // TODO why is this different than this.identifier?
                 final String labelId = getLabelId();
                 return "ALTER ATTRIBUTE {attr." + schemaName + "." + scnPk + "} ALTER LABELS {label." + schemaName + "." + scnPk + "."
                         + columnName + "} VISUAL(TITLE \"" + lcn + "\");\n";
             }
 
             public String generateMaqlAlterDataType() {
             	attr = attributes.get(scnPk);
                 if (attr == null) {
                     throw new IllegalArgumentException("Label " + columnName + " points to non-existing attribute " + scnPk);
                 }
             	if (column.getDataType() != null) {
             		return "ALTER DATATYPE {" + attr.table + "." + N.NM_PFX + columnName + "} " + column.getDataType() + ";\n";
             	}
             	return "";
             }
 
             protected String getLabelId() {
                 final String labelId = "label." + schemaName + "." + scnPk + "." + columnName;
                 return labelId;
             }
         }
 
         class Hyperlink extends Label {
             Hyperlink(SourceColumn column) {
                 super(column);
             }
 
             @Override
             public String generateMaqlDdlAdd() {
                 String script = super.generateMaqlDdlAdd();
                 attr = attributes.get(scnPk);
                 script += "# Add hyperlink mark to label\n";
                 script += "ALTER ATTRIBUTE {" + attr.identifier + "} ALTER LABELS {" + getLabelId() + "} HYPERLINK;\n";
                 return script;
             }
         }
 
         // dates
         private class DateColumn extends Column {
             private final String folderStatement;
             private final boolean includeTime;
 
             DateColumn(SourceColumn column) {
                 super(column, N.DT);
                 String folder = column.getFolder();
                 if (folder != null && folder.length() > 0) {
                     String sfn = StringUtil.toIdentifier(folder);
                     folderStatement = ", FOLDER {ffld." + sfn + "}";
                 } else {
                     folderStatement = "";
                 }
                 includeTime = column.isDatetime();
             }
 
             @Override
             public String generateMaqlDdlAdd() {
                 String reference = column.getSchemaReference();
 
                 String stat = generateFactMaqlCreate();
                 if (reference != null && reference.length() > 0) {
                     String r = column.getReference();
                     if (r == null || r.length() <= 0) {
                         r = N.DT_ATTR_NAME;
                     }
                     stat += "# Connect the date to the date dimension.\n";
                     stat += "ALTER ATTRIBUTE {" + reference + "." + r + "} ADD KEYS {" + getFactTableName() +
                             "." + N.DT_PFX + columnName + "_" + N.ID + "};\n\n";
                     /* This is now handled by adding entirely new attribute to the schema in the initSchema
                     if(includeTime) {
                         stat += "# Connect the time to the time dimension.\n";
 	                    stat += "ALTER ATTRIBUTE {"+N.TM_ATTR_NAME+reference+"} ADD KEYS {"+getFactTableName() +
 	                        "."+N.TM_PFX + scn + "_"+N.ID+"};\n\n";
                     }
                      */
                 }
                 return stat;
             }
 
             public String generateMaqlDdlDrop() {
                 String script = generateFactMaqlDrop();
                 String reference = column.getSchemaReference();
                 boolean includeTime = column.isDatetime();
                 if (reference != null && reference.length() > 0) {
                     String r = column.getReference();
                     if (r == null || r.length() <= 0) {
                         r = N.DT_ATTR_NAME;
                     }
                     script += "# Disconnect the date dimension.\n";
                     script += "ALTER ATTRIBUTE {" + reference + "." + r + "} DROP KEYS {" + getFactTableName() +
                             "." + N.DT_PFX + columnName + "_" + N.ID + "};\n\n";
                     /* Consistently with generateMaqlAddDrop(), it's moved somewhere else
                     if(includeTime) {
                         script += "ALTER ATTRIBUTE {"+N.TM_ATTR_NAME+reference+"} DROP KEYS {"+getFactTableName() +
                                 "."+N.TM_PFX + scn + "_"+N.ID+"};\n\n";
                     }
                      */
                 }
                 return script;
             }
 
             public String generateFactMaqlDrop() {
                 /*
                 String script = "DROP {" + identifier + "} CASCADE;\n";
                 if (includeTime) {
                     script += "DROP {" + N.TM_PFX + identifier + "};\n";
                 }
                 return script;
                  */
                 //CHANGED THE BEHAVIOUR, THE DATE FACTS ARE NOW ADDED VIA TRANSFORMATIONS!
                 return "";
             }
 
             public String generateFactMaqlCreate() {
                 /*
                 String script = "CREATE FACT {" + identifier + "} VISUAL(TITLE \"" + lcn
                     + " (Date)\"" + folderStatement + ") AS {" + getFactTableName() + "."+N.DT_PFX + scn +"};\n"
                     + "ALTER DATATYPE {" + getFactTableName() + "."+N.DT_PFX + scn +"} INT;\n"
                     + "ALTER DATASET {" + schema.getDatasetName() + "} ADD {"+ identifier + "};\n\n";
                 if (includeTime) {
                     script += "CREATE FACT {" + N.TM + "." + identifier + "} VISUAL(TITLE \"" + lcn
                         + " (Time)\"" + folderStatement + ") AS {" + getFactTableName() + "."+N.TM_PFX + scn +"};\n"
                         + "ALTER DATATYPE {" + getFactTableName() + "."+N.TM_PFX + scn +"} INT;\n"
                         + "ALTER DATASET {" + schema.getDatasetName() + "} ADD {"+ N.TM + "." + identifier + "};\n\n";
                 }
                 return script;
                  */
                 return "";
             }
         }
 
 
         // connection points
         class ConnectionPoint extends Attribute {
             public ConnectionPoint(SourceColumn column) {
                 super(column, getFactTableName());
                 hasCp = true;
                 //factsOfPrimaryColumn = scn + "_" + N.ID;
             }
 
             @Override
             protected String createForeignKeyMaqlDdl() {
                 // The fact table's primary key values are identical with the primary key values
                 // of a Connection Point attribute. This is why the fact table's PK may act as
                 // the connection point's foreign key as well
                 return null;
             }
 
             public String generateMaqlDdlDrop() {
                 throw new UnsupportedOperationException("Generate MAQL Drop is not supported for CONNECTION_POINTS yet");
             }
         }
 
         // references
         private class Reference extends Column {
             public Reference(SourceColumn column) {
                 super(column, "");
             }
 
             @Override
             public String generateMaqlDdlAdd() {
                 String foreignAttrId = "{attr" + "." + column.getSchemaReference() + "." + column.getReference() + "}";
                 String fk = createForeignKeyMaqlDdl();
                 if (column.isTimeFact()) {
                     foreignAttrId = "{" + N.TM_ATTR_NAME + column.getSchemaReference() + "}";
                     fk = "{" + getFactTableName() + "." + N.TM_PFX + columnName + "}";
                 }
                 String script = "# Connect the reference to the appropriate dimension.\n";
                 script += "ALTER ATTRIBUTE " + foreignAttrId
                         + " ADD KEYS " + fk + ";\n\n";
                 return script;
             }
 
 
             public String generateMaqlDdlDrop() {
                 String foreignAttrId = "{attr" + "." + column.getSchemaReference() + "." + column.getReference() + "}";
                 String fk = createForeignKeyMaqlDdl();
                 if (column.isTimeFact()) {
                     foreignAttrId = "{" + N.TM_ATTR_NAME + column.getSchemaReference() + "}";
                     fk = "{" + getFactTableName() + "." + N.TM_PFX + columnName + "}";
                 }
                 String script = "# Disconnect the reference from the appropriate dimension.\n";
                 script += "ALTER ATTRIBUTE " + foreignAttrId
                         + " DROP KEYS " + fk + ";\n\n";
                 return script;
             }
         }
 
     }
 
     /**
      * If the deleted columns and new columns passed to MAQL identifier contained the
      * same date fields with different schema references, the generated scripts contain
      * redundant lines for dropping and re-creating the identical date fact. This method
      * drops these lines from the generated MAQL DDL script.
      *
      * @param deletedColumns list of deleted {@link SourceColumn}s
      * @param newColumns     list of new {@link SourceColumn}s
      * @param maql           the MAQL DDL script generated by {@link MaqlGenerator} from both deleted
      *                       and new columns
      * @return MAQL DDL script without the redundant DROP and CREATE lines
      */
     public String removeDropAndRecreateOfDateFacts(
             final List<SourceColumn> deletedColumns,
             final List<SourceColumn> newColumns,
             final String maql) {
 
         String result = maql;
         for (final SourceColumn dc : newColumns) {
             if (containsDateByName(deletedColumns, dc)) {
                 if (dc.getSchemaReference() == null) {
                     throw new AssertionError(String.format("Date field '%s' without schemaReference", dc.getName()));
                 }
                 final State state = new State();
                 state.processColumn(dc);
                 if (state.dates.size() != 1) {
                     throw new AssertionError(String.format("One date field processed by MaqlGenerator.State but the state object holds %d date fields", state.dates.size()));
                 }
                 final String factMaqlDrop = state.dates.get(0).generateFactMaqlDrop();
                 final String factMaqlCreate = state.dates.get(0).generateFactMaqlCreate();
                 if (maql.contains(factMaqlDrop) && maql.contains(factMaqlCreate)) {
                     result = result.replace(factMaqlDrop, "");
                     result = result.replace(factMaqlCreate, "");
                 } else {
                     l.warn("Date reconnection MAQL DDL does not contain expected fact drop/create statements for " + dc);
                 }
             }
         }
         return result;
     }
 
     private boolean containsDateByName(List<SourceColumn> newColumns, SourceColumn dc) {
         if (SourceColumn.LDM_TYPE_DATE.equals(dc.getLdmType())) {
             for (final SourceColumn sc : newColumns) {
                 if (sc.getName().equals(dc.getName())) {
                     return true;
                 }
             }
         }
         return false;
     }
 }
 
