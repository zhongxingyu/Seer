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
 
 package com.gooddata.connector.backend;
 
 import java.io.File;
 import java.io.IOException;
 import com.gooddata.exception.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.gooddata.connector.model.PdmColumn;
 import com.gooddata.connector.model.PdmSchema;
 import com.gooddata.connector.model.PdmTable;
 import com.gooddata.exception.ConnectorBackendException;
 import com.gooddata.exception.InternalErrorException;
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.DLI;
 import com.gooddata.integration.model.DLIPart;
 import com.gooddata.integration.rest.GdcRESTApiWrapper;
 import com.gooddata.util.FileUtil;
 import com.gooddata.util.StringUtil;
 
 /**
  * GoodData abstract connector backend. This connector backend provides the base implementation that the specific
  * connector backends reuse.
  * <p>
  * Connector backend handles communication with the specific storage, typically flat files or SQL database.
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public abstract class AbstractConnectorBackend implements ConnectorBackend {
 
     private static Logger l = Logger.getLogger(AbstractConnectorBackend.class);
 
     // PDM schema
     protected PdmSchema pdm;
 
     // Project id
     private String projectId;
 
     // separates the different LABELs when we concatenate them to create an unique identifier out of them
     protected String HASH_SEPARATOR = "%";
 
     /**
      * {@inheritDoc}
      */
     public abstract void dropSnapshots();
         
 
     /**
      * {@inheritDoc}
      */
     public void deploy(DLI dli, List<DLIPart> parts, String dir, String archiveName)
             throws IOException {
         deploySnapshot(dli, parts, dir, archiveName, null);
     }
     
     /**
      * {@inheritDoc}
      */
     public void deploySnapshot(DLI dli, List<DLIPart> parts, String dir, String archiveName, int[] snapshotIds)
             throws IOException {
         l.debug("Deploying snapshots ids "+snapshotIds);
        if (snapshotIds != null && snapshotIds.length > 1 && !getPdm().getConnectionPointTables().isEmpty()) {
         	throw new InvalidParameterException("Only one snapshot of a data set defining a connection point may be transfered.");
         }
         loadSnapshot(parts, dir, snapshotIds);
         String fn = dir + System.getProperty("file.separator") +
                 GdcRESTApiWrapper.DLI_MANIFEST_FILENAME;
         String cn = dli.getDLIManifest(parts);
         FileUtil.writeStringToFile(cn, fn);
         l.debug("Manifest file written to file '"+fn+"'. Content: "+cn);
         FileUtil.compressDir(dir, archiveName);
         l.debug("Snapshots ids "+snapshotIds+" deployed.");
     }
 
     /**
      * {@inheritDoc}
      */
     public PdmSchema getPdm() {
         return pdm;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setPdm(PdmSchema pdm) {
         this.pdm = pdm;
     }
 
     /**
      * {@inheritDoc}
      */
     public void initialize() {
         try {
             l.debug("Initializing schema.");
             if(!isInitialized()) {
                 l.debug("Initializing system schema.");
                 initializeLocalProject();
                 l.debug("System schema initialized.");
             }
             initializeLocalDataSet(getPdm());
             l.debug("Schema initialized.");
         }
         catch (ConnectorBackendException e) {
             throw new InternalErrorException("Error initializing pdm schema '" + getPdm().getName() + "'", e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void transform() {
         try {
             createSnowflake(getPdm());
         }
         catch (ConnectorBackendException e) {
             throw new InternalErrorException("Error normalizing PDM Schema " + getPdm().getName() + " " + getPdm().getTables(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public abstract int getLastSnapshotId();
 
     /**
      * {@inheritDoc}
      */
     public boolean isInitialized() {
         return exists("snapshots");
     }
 
     /**
      * {@inheritDoc}
      */
     protected abstract boolean exists(String tbl);
     
     /**
      * {@inheritDoc}
      */
     public void extract(File dataFile, boolean hasHeader) {
         l.debug("Extracting CSV file="+dataFile.getAbsolutePath());
         if(!dataFile.exists()) {
             l.error("The file "+dataFile.getAbsolutePath()+" doesn't exists!");
             throw new InternalErrorException("The file "+dataFile.getAbsolutePath()+" doesn't exists!");
         }
         l.debug("The file "+dataFile.getAbsolutePath()+" does exists size="+dataFile.length());
         executeExtract(getPdm(), dataFile.getAbsolutePath(), hasHeader);
 
         l.debug("Extracted CSV file="+dataFile.getAbsolutePath());
     }
 
     /**
      * Execustes the load
      * @param pdm PDM schema
      * @param p DLI part
      * @param dir target directory
      * @param snapshotIds snapshot IDs to be loaded
      */
 	protected abstract void executeLoad(PdmSchema pdm, DLIPart p, String dir, int[] snapshotIds);
 
     /**
      * Execute the extract
      * @param pdm PDM schema
      * @param absolutePath extracted file absolutr path
      * @param hasHeader true if the data file has header
      */
     protected abstract void executeExtract(PdmSchema pdm, String absolutePath, boolean hasHeader);
 
 	/**
      * {@inheritDoc}
      */
     public void load(List<DLIPart> parts, String dir) {
         loadSnapshot(parts, dir, null);
     }
 
     /**
      * {@inheritDoc}
      */
     public void loadSnapshot(List<DLIPart> parts, String dir, int[] snapshotIds) {
         for (DLIPart p : parts) {
             executeLoad(getPdm(), p, dir, snapshotIds);
         }
     }
 
 
     /**
      * TODO: PK to document
      * @param table target table
      * @return
      */
     protected final List<Map<String,String>> prepareInitialTableLoad(PdmTable table) {
     	final List<Map<String,String>> result = new ArrayList<Map<String,String>>();
     	final List<PdmColumn> toLoad = new ArrayList<PdmColumn>();
     	int max = 0;
     	for (final PdmColumn col : table.getColumns()) {
     		if (col.getElements() != null && !col.getElements().isEmpty()) {
     			int size = col.getElements().size();
     			if (max == 0)
     				max = size;
     			else if (size != max)
     				throw new IllegalStateException(
     						"Column " + col.getName() + " of table " + table.getName()
     						+ " has a different number of elements than: " + toLoad.toString());
     			toLoad.add(col);
     		}
     	}
     	if (!toLoad.isEmpty()) {    	
 	    	for (int i = 0; i < toLoad.get(0).getElements().size(); i++) {
 	    		final Map<String,String> row = new HashMap<String, String>();
 	    		for (final PdmColumn col : toLoad) {
 	    			row.put(col.getName(), col.getElements().get(i));
 	    		}
 	    		result.add(row);
 	    	}
     	}
     	return result;
     }
 
     /**
      * Get all columns that will be inserted (exclude autoincrements)
      * @param lookupTable lookup table
      * @return all columns eglibile for insert
      */
     protected String getInsertColumns(PdmTable lookupTable) {
         String insertColumns = "";
         for(PdmColumn column : lookupTable.getAssociatedColumns()) {
             if(insertColumns.length() > 0)
                 insertColumns += "," + column.getName();
             else
                 insertColumns += column.getName();
         }
         return insertColumns;
     }
 
     /**
      * Returns associted columns in the source table
      * @param lookupTable lookup table
      * @return list of associated source columns
      */
     protected String getAssociatedSourceColumns(PdmTable lookupTable) {
         String sourceColumns = "";
         for(PdmColumn column : lookupTable.getAssociatedColumns()) {
             if(sourceColumns.length() > 0)
                 sourceColumns += "," + column.getSourceColumn();
             else
                 sourceColumns += column.getSourceColumn();
         }
         return sourceColumns;
     }
 
     /**
      * Returns non-autoincrement columns
      * @param tbl table
      * @return non-autoincrement columns
      */
     protected String getNonAutoincrementColumns(PdmTable tbl) {
         String cols = "";
         for (PdmColumn col : tbl.getColumns()) {
             String cn = col.getName();
             if(!col.isAutoIncrement())
                 if (cols != null && cols.length() > 0)
                     cols += "," + cn;
                 else
                     cols += cn;
         }
         return cols;
     }
 
     /**
      * Uses DBMS specific functions for decorating fact columns for unloading from DB to CSV
      * @param cols column list
      * @param cl column to add to cols
      * @param table table name
      * @return the amended list
      */
     protected String decorateFactColumnForLoad(String cols, Column cl, String table) {
         return decorateOtherColumnForLoad(cols, cl, table);
     }
 
     /**
      * Uses DBMS specific functions for decorating lookup columns for unloading from DB to CSV
      * @param cols column list
      * @param cl column to add to cols
      * @param table table name
      * @return the amended list
      */
     protected String decorateLookupColumnForLoad(String cols, Column cl, String table) {
         return decorateOtherColumnForLoad(cols, cl, table);
     }
 
     /**
      * Uses DBMS specific functions for decorating generic columns for unloading from DB to CSV
      * @param cols column list
      * @param cl column to add to cols
      * @param table table name
      * @return the amended list
      */
     protected String decorateOtherColumnForLoad(String cols, Column cl, String table) {
         if (cols != null && cols.length() > 0)
             cols += "," + table + "." + StringUtil.toIdentifier(cl.getName());
         else
             cols +=  table + "." + StringUtil.toIdentifier(cl.getName());
         return cols;
     }
 
     /**
      * Get tab,e name from DLI part
      * @param part DLI part
      * @return table name
      */
     protected String getTableNameFromPart(DLIPart part) {
         return StringUtil.toIdentifier(part.getFileName().split("\\.")[0]);
     }
 
 
     /**
      * {@inheritDoc}
      */
     public String getProjectId() {
         return projectId;
     }
     
     /**
      * {@inheritDoc}
      */
     public void setProjectId(String projectId) {
         this.projectId = projectId;
     }
     
 	/**
      * {@inheritDoc}
      */
     protected abstract void initializeLocalProject() throws ConnectorBackendException;
 
     /**
      * {@inheritDoc}
      */
     protected abstract void initializeLocalDataSet(PdmSchema schema) throws ConnectorBackendException;
 
     /**
      * {@inheritDoc}
      */
     protected abstract void createSnowflake(PdmSchema schema) throws ConnectorBackendException;
 
 }
