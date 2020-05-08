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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.gooddata.connector.AbstractConnector;
 import com.gooddata.connector.DateColumnsExtender;
 import com.gooddata.modeling.generator.MaqlGenerator.State.Attribute;
 import com.gooddata.modeling.generator.MaqlGenerator.State.Column;
 import com.gooddata.modeling.generator.MaqlGenerator.State.ConnectionPoint;
 import com.gooddata.modeling.generator.MaqlGenerator.State.Label;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.naming.N;
 import com.gooddata.util.StringUtil;
 
 import static com.gooddata.modeling.model.SourceColumn.*;
 
 /**
  * GoodData MAQL Generator generates the MAQL from the LDM schema object
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class MaqlGenerator {
 
     private static Logger l = Logger.getLogger(MaqlGenerator.class);
 
     private final SourceSchema schema;
     private final String ssn, lsn;
 	private final String factsOfAttrMaqlDdl;
 	
 	private boolean synchronize = true; // should generateMaql*() methods append SYNCHRONIZE commands?
 
     public MaqlGenerator(SourceSchema schema) {
         this.schema = schema;
         this.ssn = StringUtil.toIdentifier(schema.getName());
         this.lsn = StringUtil.toTitle(schema.getName());
         this.factsOfAttrMaqlDdl = createFactOfMaqlDdl(schema.getName());
     }
 
 	/**
      * Generates the MAQL from the schema
      *
      * @return the MAQL as a String
      */
     public String generateMaqlCreate() {
         String script = "# THIS IS MAQL SCRIPT THAT GENERATES PROJECT LOGICAL MODEL.\n# SEE THE MAQL DOCUMENTATION " +
                 "AT http://developer.gooddata.com/api/maql-ddl.html FOR MORE DETAILS\n\n";
         script += "# CREATE DATASET. DATASET GROUPS ALL FOLLOWING LOGICAL MODEL ELEMENTS TOGETHER.\n";
         script += "CREATE DATASET {" + schema.getDatasetName() + "} VISUAL(TITLE \"" + lsn + "\");\n\n";
         script += generateFoldersMaqlDdl(schema.getColumns());
         
         script += generateMaqlAdd(schema.getColumns(), new ArrayList<SourceColumn>(), true);
         
         return script;
     }
     
     /**
      * should generateMaql*() methods append SYNCHRONIZE commands?
      * @param synchronize
      */
     public void setSynchronize(final boolean synchronize) {
     	this.synchronize = synchronize;
     }
 
     /**
      * Generate MAQL for specified (new) columns
      * @return MAQL as String
      */
     public String generateMaqlAdd(Iterable<SourceColumn> newColumns, Iterable<SourceColumn> knownColumns) {
     	return generateMaqlAdd(newColumns, knownColumns, false);
     }
 
     /**
      * Creates attribute table name
      * @param schema source schema
      * @param sc source column
      * @return the attribute table name
      */
 	public static String createAttributeTableName(SourceSchema schema, SourceColumn sc) {
 		final String ssn = StringUtil.toIdentifier(schema.getName());
 		return "d_" + ssn + "_" + StringUtil.toIdentifier(sc.getName());
 	}
 	
 	/**
      * Generate MAQL DROP statement for selected columns
      * @param columns list of columns
      * @return MAQL as String
      */
     public String generateMaqlDrop(List<SourceColumn> columns, Iterable<SourceColumn> knownColumns) {
         // generate attributes and facts
     	State state = new State();
         for (SourceColumn column : columns) {
         	state.processColumn(column);
         }
         
         StringBuffer nonLabelsScript = new StringBuffer("# DROP ATTRIBUTES.\n");
         
         for (final Column c : state.attributes.values()) {
             nonLabelsScript.append(c.generateMaqlDdlDrop());
         }
         nonLabelsScript.append("# DROP FACTS\n");
         for (final Column c : state.facts) {
             nonLabelsScript.append(c.generateMaqlDdlDrop());
         }
         nonLabelsScript.append("# DROP DATEs\n# DATES ARE REPRESENTED AS FACTS\n");
         nonLabelsScript.append("# DATES ARE ALSO CONNECTED TO THE DATE DIMENSIONS\n");
         for (final Column c : state.dates) {
             nonLabelsScript.append(c.generateMaqlDdlDrop());
         }
         nonLabelsScript.append("# DROP REFERENCES\n# REFERENCES CONNECT THE DATASET TO OTHER DATASETS\n");
         for (final Column c : state.references) {
             nonLabelsScript.append(c.generateMaqlDdlDrop());
         }
 
         state.addKnownColumns(knownColumns);
         
         StringBuilder script = new StringBuilder();
         script.append("# DROP LABELS\n");
         for (final Column c: state.labels) {
             script.append(c.generateMaqlDdlDrop());
         }
         script.append(nonLabelsScript);
 
         // finally 
         if (synchronize) {
             script.append("# SYNCHRONIZE THE STORAGE AND DATA LOADING INTERFACES WITH THE NEW LOGICAL MODEL\n");
             script.append("SYNCHRONIZE {" + schema.getDatasetName() + "};\n\n");
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
 		script.append("# SYNCHRONIZE THE STORAGE AND DATA LOADING INTERFACES WITH THE NEW LOGICAL MODEL\n");
         script.append("SYNCHRONIZE {" + schema.getDatasetName() + "};\n\n");
         return script.toString();
 	}
 
     /**
      * Generate MAQL for selected (new) columns
      * @param newColumns list of columns
      * @param createFactsOf create the facts of attribute
      * @return MAQL as String
      */
     private String generateMaqlAdd(Iterable<SourceColumn> newColumns, Iterable<SourceColumn> knownColumns, boolean createFactsOf) {
 
         // generate attributes and facts
     	State state = new State();
     	for (SourceColumn column : newColumns) {
             state.processColumn(column);
         }
 
         String script = "# CREATE ATTRIBUTES.\n# ATTRIBUTES ARE CATEGORIES THAT ARE USED FOR SLICING AND DICING THE " +
                     "NUMBERS (FACTS)\n";
         
         ConnectionPoint connectionPoint = null; // hold the CP's default label to be created at the end
         for (final Column c : state.attributes.values()) {
             script += c.generateMaqlDdlAdd();
             if (c instanceof ConnectionPoint) {
                 connectionPoint = (ConnectionPoint)c;
             } else {
                 final Attribute a = (Attribute)c;
                 script += a.generateOriginalLabelMaqlDdl();
                 script += a.generateDefaultLabelMaqlDdl();
             }
         }
         script += "# CREATE FACTS\n# FACTS ARE NUMBERS THAT ARE AGGREGATED BY ATTRIBUTES.\n";
         for (final Column c : state.facts) {
             script += c.generateMaqlDdlAdd();
         }
         script += "# CREATE DATE FACTS\n# DATES ARE REPRESENTED AS FACTS\n# DATES ARE ALSO CONNECTED TO THE " +
                 "DATE DIMENSIONS\n";
         for (final Column c : state.dates) {
             script += c.generateMaqlDdlAdd();
         }
         script += "# CREATE REFERENCES\n# REFERENCES CONNECT THE DATASET TO OTHER DATASETS\n";
         for (final Column c : state.references) {
         	script += c.generateMaqlDdlAdd();
         }
 
         if (createFactsOf & (!state.hasCp)) {
 	        script += "# THE FACTS OF ATTRIBUTE IS SORT OF DATASET IDENTIFIER\n";
 	        script += "# IT IS USED FOR COUNT AGGREGATIONS\n";
 	        // generate the facts of / record id special attribute
 	        script += "CREATE ATTRIBUTE " + factsOfAttrMaqlDdl + " VISUAL(TITLE \""
 	                  + "Records of " + lsn + "\") AS KEYS {" + getFactTableName() + "."+state.factsOfPrimaryColumn+"} FULLSET;\n";
 	        script += "ALTER DATASET {" + schema.getDatasetName() + "} ADD {attr." + ssn + ".factsof};\n\n";
         }
         
     	state.addKnownColumns(knownColumns);
 
         // labels last
     	boolean cpDefLabelSet = false;
         for (final Column c : state.labels) {
             script += c.generateMaqlDdlAdd();
             Label l = (Label)c;
             if (!cpDefLabelSet && (connectionPoint != null) && l.attr.identifier.equals(connectionPoint.identifier)) {
                 script += l.generateMaqlDdlDefaultLabel();
                 cpDefLabelSet = true;
             }
         }
         
         // CP's default label after all other labels
         if (connectionPoint != null) {
             script += connectionPoint.generateOriginalLabelMaqlDdl();
         }
         
         // finally 
         if (synchronize) {
 	        script += "# SYNCHRONIZE THE STORAGE AND DATA LOADING INTERFACES WITH THE NEW LOGICAL MODEL\n";
 	        script += "SYNCHRONIZE {" + schema.getDatasetName() + "};\n\n";
         }
         return script;
     }
 
     /**
      * Generate MAQL folders for specified columns
      * @param columns list of columns
      * @return MAQL as String
      */
     private String generateFoldersMaqlDdl(List<SourceColumn> columns) {
         final ArrayList<String> attributeFolders = new ArrayList<String>();
         final ArrayList<String> factFolders = new ArrayList<String>();
 
         for (SourceColumn column : columns) {
             String folder = column.getFolder();
             if (folder != null && folder.length() > 0) {
                 if (column.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_ATTRIBUTE) ||
                         column.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_LABEL) ||
                         column.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_CONNECTION_POINT) ||
                         column.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_REFERENCE) ||
                         column.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_DATE))
                 {
                     if (!attributeFolders.contains(folder))
                         attributeFolders.add(folder);
                 }
                 if (column.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_FACT) ||
                 		column.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_DATE))
                 {
                     if (!factFolders.contains(folder))
                         factFolders.add(folder);
                 }
             }
         }
 
         String script = "";
         if(!attributeFolders.isEmpty() || !factFolders.isEmpty())
             script += "# CREATE THE FOLDERS THAT GROUP ATTRIBUTES AND FACTS\n";
         // Generate statements for the ATTRIBUTE folders
         for (String folder : attributeFolders) {
             String sfn = StringUtil.toIdentifier(folder);
             String lfn = StringUtil.toTitle(folder);
             script += "CREATE FOLDER {dim." + sfn + "} VISUAL(TITLE \"" + lfn + "\") TYPE ATTRIBUTE;\n";
         }
         script += "\n";
 
         // Generate statements for the FACT folders
         for (String folder : factFolders) {
             String sfn = StringUtil.toIdentifier(folder);
             String lfn = StringUtil.toTitle(folder);
             script += "CREATE FOLDER {ffld." + sfn + "} VISUAL(TITLE \"" + lfn + "\") TYPE FACT;\n";
         }
 
         script += "\n";
         return script;
     }
 
     /**
      * Generate fact table name
      * @return fact table name
      */
     private String getFactTableName() {
     	return N.FCT_PFX + ssn;
     }
 
     /**
      * Generate the MAQL for the facts of attribute
      * @param schemaName schema name
      * @return facts of attribute MAQL DDL
      */
     private static String createFactOfMaqlDdl(String schemaName) {
     	return "{attr." + StringUtil.toIdentifier(schemaName) + "." + N.FACTS_OF + "}";
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
 		 * @param column source columns
 		 */
 		private void processColumn(SourceColumn column) {
 		    if (column.getLdmType().equals(SourceColumn.LDM_TYPE_ATTRIBUTE)) {
 		    	Attribute attr = new Attribute(column);
 		        attributes.put(attr.scn, attr);
 		    } else if (column.getLdmType().equals(SourceColumn.LDM_TYPE_FACT)) {
 		        facts.add(new Fact(column));
 		    } else if (column.getLdmType().equals(SourceColumn.LDM_TYPE_DATE)) {
                 if(column.getSchemaReference() != null && column.getSchemaReference().length() > 0) {
 		            dates.add(new DateColumn(column));
                 }
                 else {
                     Attribute attr = new Attribute(column);
 		            attributes.put(attr.scn, attr);
                 }
 		    } else if (column.getLdmType().equals(SourceColumn.LDM_TYPE_LABEL)) {
 		        labels.add(new Label(column));
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
 		 * @param column source column
 		 */
 		private void processConnectionPoint(SourceColumn column) {
 		    if (column.getLdmType().equals(SourceColumn.LDM_TYPE_CONNECTION_POINT)) {
 		        if (hasCp) {
 		            throw new IllegalStateException("Only one connection point per dataset is allowed. "
 		                    + "Consider declaring the duplicate connection points as labels of the main connection point.");
 		        }
 		        ConnectionPoint connectionPoint = new ConnectionPoint(column);
 		        attributes.put(connectionPoint.scn, connectionPoint);
 		    }
 		}
     	
 	    // columns
 
 	    abstract class Column {
 	        protected final SourceColumn column;
 	        protected final String scn, lcn;
 	        protected final String identifier;
 
 	        Column(SourceColumn column, String idprefix) {
 	            this.column = column;
 	            this.scn = StringUtil.toIdentifier(column.getName());
 	            this.lcn = StringUtil.toTitle(column.getTitle());
 	            this.identifier = idprefix + "." + ssn + "." + scn;
 	        }
 
 	        protected String createForeignKeyMaqlDdl() {
 	           	return "{" + getFactTableName() + "." + scn + "_" + N.ID+ "}";
 	        }
 	                
 	        public abstract String generateMaqlDdlAdd();
 	        
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
 	            this.defaultLabelIdentifier = "label." + ssn + "." + scn;
 	            this.defaultLabelDdl = "{" + defaultLabelIdentifier + "} VISUAL(TITLE \""
                                     + lcn + "\") AS {" + this.table + "."+N.NM_PFX + scn + "}";
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
 
 	            String script = "CREATE ATTRIBUTE {" + identifier + "} VISUAL(TITLE \"" + lcn
 	                    + "\"" + folderStatement + ") AS KEYS {" + table + "."+N.ID+"} FULLSET";
                 String fks = createForeignKeyMaqlDdl();
                 // FK can be null in case of the connection point
 	            script += (fks != null && fks.length() > 0) ? (", "+fks) : ("");
 	            script += ";\n";
                 // The connection point are going to have labels in the fact table
 	            script += "ALTER DATASET {" + schema.getDatasetName() + "} ADD {attr." + ssn + "." + scn + "};\n";
 
                 String dataType = column.getDataType();
                 if(SourceColumn.LDM_IDENTITY.equalsIgnoreCase(dataType))
                     dataType = SourceColumn.IDENTITY_DATATYPE;
                 if(dataType != null && dataType.length() > 0) {
                     script += "ALTER DATATYPE {" + table + "."+N.NM_PFX + scn + "} "+dataType+";\n";
                 }
                 else {
                     script += "\n";
                 }
 
 	            return script;
 	        }
 	        
 	        public String generateOriginalLabelMaqlDdl() {
 	            String script = "ALTER ATTRIBUTE {" + identifier + "} ADD LABELS " + defaultLabelDdl + "; \n";
 	            return script;
 	        }
 	        
 	        public String generateDefaultLabelMaqlDdl() {
 	           return "ALTER ATTRIBUTE  {" + identifier + "} DEFAULT LABEL {" + defaultLabelIdentifier + "};\n";
 	        }
 	    }
 
 	    //facts
 	    private class Fact extends Column {
 
 	        Fact(SourceColumn column) {
 	            super(column, "fact");
 	        }
 
 	        @Override
 	        public String generateMaqlDdlAdd() {
 	            String folderStatement = "";
 	            String folder = column.getFolder();
 	            if (folder != null && folder.length() > 0) {
 	                String sfn = StringUtil.toIdentifier(folder);
 	                folderStatement = ", FOLDER {ffld." + sfn + "}";
 	            }
 
 	            String script =  "CREATE FACT {fact." + ssn + "." + scn + "} VISUAL(TITLE \"" + lcn
 	                    + "\"" + folderStatement + ") AS {" + getFactTableName() + "."+N.FCT_PFX + scn + "};\n"
 	                    + "ALTER DATASET {" + schema.getDatasetName() + "} ADD {" + identifier + "};\n";
                 String dataType = column.getDataType();
                 if(SourceColumn.LDM_IDENTITY.equalsIgnoreCase(dataType))
                     dataType = SourceColumn.IDENTITY_DATATYPE;
                 if(dataType != null && dataType.length() > 0) {
                     script += "ALTER DATATYPE {" + getFactTableName() + "."+N.FCT_PFX + scn + "} "+dataType+";\n";
                 }
                 else {
                     script += "\n";
                 }
                 return script;
 	        }
 	    }
 
 	    //labels
 	    class Label extends Column {
 	        
 	        final String scnPk;
 	        Attribute attr;
 
 	        Label(SourceColumn column) {
 	            super(column, "label");
 	            scnPk = StringUtil.toIdentifier(column.getReference());
 	        }
 
 	        @Override
 	        public String generateMaqlDdlAdd() {
 	            attr = attributes.get(scnPk);
 	            if (attr == null) {
 	            	throw new IllegalArgumentException("Label " + scn + " points to non-existing attribute " + scnPk);
 	            }
 	            String script = "# ADD LABELS TO ATTRIBUTES\n";
 	            script += "ALTER ATTRIBUTE {attr." + ssn + "." + scnPk + "} ADD LABELS {label." + ssn + "." + scnPk + "."
 	                    + scn + "} VISUAL(TITLE \"" + lcn + "\") AS {" + attr.table + "."+N.NM_PFX + scn + "};\n";
 
                 String dataType = column.getDataType();
                 if(SourceColumn.LDM_IDENTITY.equalsIgnoreCase(dataType))
                     dataType = SourceColumn.IDENTITY_DATATYPE;
                 if(dataType != null && dataType.length() > 0) {
                     script += "ALTER DATATYPE {" + attr.table + "."+N.NM_PFX + scn + "} "+dataType+";\n";
                 }
                 else {
                     script += "\n";
                 }
                 return script;
 	        }
 	        
 	        public String generateMaqlDdlDrop() {
                 attr = attributes.get(scnPk);
                 if (attr == null) {
                     throw new IllegalArgumentException("Label " + scn + " points to non-existing attribute " + scnPk);
                 }
                 String script = "# DROP LABELS FROM ATTRIBUTES\n";
                 final String labelId = "label." + ssn + "." + scnPk + "." + scn;
                 script += "ALTER ATTRIBUTE  {" + attr.identifier + "} DROP LABELS {" + labelId + "};\n";
                 return script;
 	        }
 	        
 	        public String generateMaqlDdlDefaultLabel() {
 	            attr = attributes.get(scnPk);
 	            if (attr == null) {
 	                throw new IllegalArgumentException("Label " + scn + " points to non-existing attribute " + scnPk);
 	            }
 	            // TODO why is this different than this.identifier?
 	            final String labelId = "label." + ssn + "." + scnPk + "." + scn;
 	            return "ALTER ATTRIBUTE  {" + attr.identifier + "} DEFAULT LABEL {" + labelId + "};\n";	            
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
 	            if(reference != null && reference.length() > 0) {
 	                reference = StringUtil.toIdentifier(reference);
                     String r = column.getReference();
                     if(r == null || r.length() <= 0) {
                         r = N.DT_ATTR_NAME;
                     }
 	                stat += "# CONNECT THE DATE TO THE DATE DIMENSION\n";
 	                stat += "ALTER ATTRIBUTE {"+reference+"."+r+"} ADD KEYS {"+getFactTableName() +
 	                        "."+N.DT_PFX + scn + "_"+N.ID+"};\n\n";
                     if(includeTime) {
                         stat += "# CONNECT THE TIME TO THE TIME DIMENSION\n";
 	                    stat += "ALTER ATTRIBUTE {"+N.TM_ATTR_NAME+reference+"} ADD KEYS {"+getFactTableName() +
 	                        "."+N.TM_PFX + scn + "_"+N.ID+"};\n\n";
                     }
 	            }
 	            return stat;
 	        }
 
 	        public String generateMaqlDdlDrop() {
 	            String script = generateFactMaqlDrop();
 	        	String reference = column.getSchemaReference();
                 boolean includeTime = column.isDatetime();
 	        	if(reference != null && reference.length() > 0) {
 	                reference = StringUtil.toIdentifier(reference);
                     String r = column.getReference();
                     if(r == null || r.length() <= 0) {
                         r = N.DT_ATTR_NAME;
                     }
 	                script += "# DISCONNECT THE DATE DIMENSION\n";
                     script += "ALTER ATTRIBUTE {"+reference+"."+r+"} DROP KEYS {"+getFactTableName() +
 	                        "."+N.DT_PFX + scn + "_"+N.ID+"};\n\n";
                     if(includeTime) {
                         script += "ALTER ATTRIBUTE {"+N.TM_ATTR_NAME+reference+"} DROP KEYS {"+getFactTableName() +
                                 "."+N.TM_PFX + scn + "_"+N.ID+"};\n\n";
                     }
 	            }
 	        	return script;
 	        }
 
             public String generateFactMaqlDrop() {
                 String script = "DROP {" + identifier + "} CASCADE;\n";
                 if (includeTime) {
                     script += "DROP {" + N.TM_PFX + identifier + "};\n";
                 }
                 return script;
             }
 
             public String generateFactMaqlCreate() {
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
 	    		String foreignAttrId = "{attr"+"."+StringUtil.toIdentifier(column.getSchemaReference())+"."+StringUtil.toIdentifier(column.getReference())+"}";
 	            String script = "# CONNECT THE REFERENCE TO THE APPROPRIATE DIMENSION\n";
 	    		script += "ALTER ATTRIBUTE " + foreignAttrId
 	    					  + " ADD KEYS " + createForeignKeyMaqlDdl() + ";\n\n"; 
 	    		return script;
 	    	}
 	    	
 	    	public String generateMaqlDdlDrop() {
 	    		String foreignAttrId = "{attr"+"."+StringUtil.toIdentifier(column.getSchemaReference())+"."+StringUtil.toIdentifier(column.getReference())+"}";
 	            String script = "# DISCONNECT THE REFERENCE FROM THE APPROPRIATE DIMENSION\n";
 	    		script += "ALTER ATTRIBUTE " + foreignAttrId
 	    					  + " DROP KEYS " + createForeignKeyMaqlDdl() + ";\n\n"; 
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
      * @param newColumns list of new {@link SourceColumn}s
      * @param maql the MAQL DDL script generated by {@link MaqlGenerator} from both deleted
      *      and new columns
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
                 final String factMaqlDrop   = state.dates.get(0).generateFactMaqlDrop();
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
 
