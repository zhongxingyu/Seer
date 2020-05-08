 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.voet.datasetcreator.io;
 
 import com.voet.datasetcreator.data.entities.ColumnMapper;
 import com.voet.datasetcreator.data.entities.SchemaMapper;
 import com.voet.datasetcreator.data.entities.TableMapper;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.Types;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 
 /**
  *
  * @author dev
  */
 public class DatasetWriter {
 
     private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy/MM/dd HH:mm:SS" );
     private final SchemaMapper schema;
 
     public DatasetWriter( SchemaMapper schema ) {
         this.schema = schema;
     }
 
     /**
      *
      * @param outfile
      * @param fieldChoice
      * @param numRows
      * @param includeDefaults
      * @param respectConstraints
      * @return boolean
      */
     public boolean writeDataset( File outfile, String fieldChoice, int numRows,
             boolean includeDefaults, boolean respectConstraints ) {
         XMLWriter writer = null;
         try {
             OutputFormat format = OutputFormat.createPrettyPrint();
             Document doc = buildDocument( fieldChoice, numRows, includeDefaults, respectConstraints );
             writer = new XMLWriter( new FileWriter( outfile ), format );
             writer.write( doc );
             writer.flush();
             return true;
         } catch ( IOException ex ) {
             Logger.getLogger( DatasetWriter.class.getName() ).log( Level.SEVERE,
                     null, ex );
             return false;
         } finally {
             if ( writer != null ) {
                 try {
                     writer.close();
                 } catch ( IOException ex ) {
                     Logger.getLogger( DatasetWriter.class.getName() ).log( Level.SEVERE, null, ex );
                 }
             }
         }
     }
 
     private String getDefault( Integer type ) {
         switch ( type ) {
             case Types.VARCHAR:
                 return "";
             case Types.INTEGER:
                 return String.valueOf( 1 );
             case Types.DECIMAL:
                 return String.valueOf( 1 );
             case Types.NUMERIC:
                 return String.valueOf( 1 );
             case Types.DATE:
                 return DATE_FORMAT.format( new Date() );
             case Types.TIMESTAMP:
                 return DATE_FORMAT.format( new Date() );
             case Types.BOOLEAN:
                 return Boolean.TRUE.toString();
             case Types.BIT:
                 return "1";
             case Types.DOUBLE:
                 return Double.valueOf( "0" ).toString();
             case Types.FLOAT:
                 return Float.valueOf( "0" ).toString();
             default:
                 return type.toString();
         }
     }
 
     /**
      * Method builds a document based on all the table schema meta data collected.
      * @param fieldChoice What fields to include in the document.  Accepted values are ( ALL, REQ, NONE )
      * @param numRows The number of table rows to create
      * @param includeDefaults Whether to include default data or not.
      * @param respectConstraints Whether to create foreign associations if possible
      * @return Document A DOM4J document containing the dataset.
      */
     private Document buildDocument( String fieldChoice, int numRows,
             boolean includeDefaults, boolean respectConstraints ) {
 
         Element datasetElement = DocumentHelper.createElement( "dataset" );
         Document doc = DocumentHelper.createDocument( datasetElement );
 
         for ( TableMapper table : schema.getTables() ) {
             int id = 0;
             for ( int i = 0; i < numRows; i++ ) {
                 id++;
 
                 Element tableElement = datasetElement.addElement( table.getName() );
                 for ( ColumnMapper column : table.getColumms() ) {
                     if ( fieldChoice.equals( "ALL" ) ) {
                         if ( column.isPrimaryKey() ) {
                             tableElement.addAttribute( column.getColumnName(), String.valueOf( id ) );
                         } else {
                             if ( column.isForeignKey() && respectConstraints ) {
                             } else {
                                 String defaultValue = "";
                                 if ( includeDefaults ) {
                                     defaultValue = getDefault( column.getType() );
                                 }
                                 tableElement.addAttribute( column.getColumnName(), defaultValue );
                             }
                         }
 
                     } else if ( fieldChoice.equals( "REQ" ) ) {
                         if ( column.isPrimaryKey() ) {
                             tableElement.addAttribute( column.getColumnName(), String.valueOf( id ) );
                         } else if ( column.isRequired() ) {
                             if ( column.isForeignKey() && respectConstraints ) {
                             } else {
                                 String defaultValue = "";
                                 if ( includeDefaults ) {
                                     defaultValue = getDefault( column.getType() );
                                 }
                                 tableElement.addAttribute( column.getColumnName(), defaultValue );
                             }
                         }
 
                     } else if ( fieldChoice.equals( "NONE" ) ) {
                     }
                 }
             }
         }
         if ( respectConstraints ) {
             doc = buildForeignKeyRelationships( doc, schema, numRows );
         }
         return doc;
 
     }
 
     /**
      * The document that has been modified to include foreign key relationships.
      * @param doc The original document
      * @param schema The table schema.
      */
     private Document buildForeignKeyRelationships( Document doc,
             SchemaMapper schema, int numRows ) {
         Element rootElement = doc.getRootElement();
 
         for ( TableMapper table : schema.getTables() ) {
             List<ColumnMapper> fkCols = table.getForeignKeys();
             for ( ColumnMapper column : fkCols ) {
                 for ( int i = 0; i < numRows; i++ ) {
                     String xpath1 = "/dataset/" + column.getForeignKeyTable() +"["+ Integer.valueOf(i+1).toString() + "]";
                     Element pkElement = (Element) rootElement.selectSingleNode( xpath1 );
 
                     String xpath2 = "/dataset/" + table.getName() + "["+ Integer.valueOf(i+1).toString() + "]";
                     Element curElement = (Element) rootElement.selectSingleNode( xpath2 );
                     curElement.addAttribute( column.getColumnName(), pkElement.attributeValue( column.getForeignKeyColumn() )  );
                 }
             }
         }
         return doc;
     }
 }
