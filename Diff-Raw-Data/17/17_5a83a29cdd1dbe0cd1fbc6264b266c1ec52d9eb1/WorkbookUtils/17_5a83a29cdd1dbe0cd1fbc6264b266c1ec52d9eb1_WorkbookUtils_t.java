 package org.bsc.poi;
 
 import java.io.IOException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.poi.POIXMLDocumentPart;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.hssf.util.CellReference;
 import org.apache.poi.openxml4j.opc.PackagePart;
 import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
 import org.apache.poi.xssf.usermodel.XSSFRow;
 import org.apache.poi.xssf.usermodel.XSSFSheet;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.apache.xmlbeans.SimpleValue;
 import org.apache.xmlbeans.XmlCalendar;
 import org.apache.xmlbeans.XmlDateTime;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlObject;
 import org.apache.xmlbeans.XmlTime;
 import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSingleXmlCell;
 import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSingleXmlCells;
 import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
 import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
 import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns;
 import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXmlCellPr;
 import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXmlColumnPr;
 import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXmlPr;
 import org.openxmlformats.schemas.spreadsheetml.x2006.main.STXmlDataType;
 import org.openxmlformats.schemas.spreadsheetml.x2006.main.SingleXmlCellsDocument;
 import org.openxmlformats.schemas.spreadsheetml.x2006.main.TableDocument;
 
 /**
  * 
  *
  */
 public class WorkbookUtils
 {
     public static final String TABLESINGLECELLS_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.tableSingleCells+xml";
     public static final String TABLE_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.table+xml";
     static final Log log = LogFactory.getLog("excel-mapping");
 
     private WorkbookUtils() {
 
     }
 
     public static interface Converter<T> {
 
         T convert( SimpleValue value) throws Exception;
     }
 
     static Converter<Integer> toInt = new Converter<Integer>() {
 
         public Integer convert(SimpleValue value) throws Exception {
             //return  value.getIntValue();
 
             String v = value.getStringValue();
 
             return Integer.valueOf(v);
         }
 
     };
     static Converter<Double> toDouble = new Converter<Double>() {
 
         public Double convert(SimpleValue value) throws Exception {
             //return  value.getDoubleValue();
 
             String v = value.getStringValue();
 
             return Double.valueOf(v);
         }
 
     };
     static Converter<Float> toFloat = new Converter<Float>() {
 
         public Float convert(SimpleValue value) throws Exception {
             //return  value.getFloatValue();
 
             String v = value.getStringValue();
 
             return Float.valueOf(v);
         }
 
     };
     static Converter<Long> toLong = new Converter<Long>() {
 
         public Long convert(SimpleValue value) throws Exception {
             //return  value.getLongValue();
 
             String v = value.getStringValue();
 
             return Long.valueOf(v);
         }
 
     };
     static Converter<Boolean> toBoolean = new Converter<Boolean>() {
 
         public Boolean convert(SimpleValue value) throws Exception {
             //return  value.getBooleanValue();
 
             String v = value.getStringValue();
 
             return Boolean.valueOf(v);
         }
 
     };
 
     static Converter<java.util.Date> toDate = new Converter<java.util.Date>() {
 
         public java.util.Date convert(SimpleValue value) throws Exception {
            //return  value.getDateValue();
 
             String v = value.getStringValue();
 
             XmlCalendar d = new XmlCalendar(v);
 
             return d.getTime();
         }
 
     };
 
     static Converter<java.util.Date> toDateTime = new Converter<java.util.Date>() {
 
         public java.util.Date convert(SimpleValue value) throws Exception {
            //return  value.getDateValue();
 
             String v = value.getStringValue();
 
             XmlCalendar d = new XmlCalendar(v);
 
             return d.getTime();
         }
 
     };
     static Converter<java.util.Date> toTime = new Converter<java.util.Date>() {
 
         public java.util.Date convert(SimpleValue value) throws Exception {
            //return  value.getDateValue();
 
             String v = value.getStringValue();
 
             XmlCalendar d = new XmlCalendar(v);
 
             return d.getTime();
         }
 
     };
 
     /**
      *
      * @param converter
      * @return
      */
     public static Converter<Integer> registerIntConverter( Converter<Integer> converter ) {
         if( converter==null ) throw new IllegalArgumentException("converter is null");
 
         Converter<Integer> prev = toInt;
 
         toInt = converter;
 
         return prev;
         
     }
 
     /**
      *
      * @param converter
      * @return
      */
     public static Converter<Float> registerFloatConverter( Converter<Float> converter ) {
         if( converter==null ) throw new IllegalArgumentException("converter is null");
 
         Converter<Float> prev = toFloat;
 
         toFloat = converter;
 
         return prev;
 
     }
 
     /**
      *
      * @param converter
      * @return
      */
     public static Converter<Double> registerDoubleConverter( Converter<Double> converter ) {
         if( converter==null ) throw new IllegalArgumentException("converter is null");
 
         Converter<Double> prev = toDouble;
 
         toDouble = converter;
 
         return prev;
 
     }
 
     /**
      *
      * @param converter
      * @return
      */
     public static Converter<Long> registerLongConverter( Converter<Long> converter ) {
         if( converter==null ) throw new IllegalArgumentException("converter is null");
 
         Converter<Long> prev = toLong;
 
         toLong = converter;
 
         return prev;
 
     }
 
     /**
      *
      * @param converter
      * @return
      */
     public static Converter<Boolean> registerBooleanConverter( Converter<Boolean> converter ) {
         if( converter==null ) throw new IllegalArgumentException("converter is null");
 
         Converter<Boolean> prev = toBoolean;
 
         toBoolean = converter;
 
         return prev;
 
     }
 
     /**
      *
      * @param converter
      * @return
      */
     public static Converter<java.util.Date> registerDateConverter( Converter<java.util.Date> converter ) {
         if( converter==null ) throw new IllegalArgumentException("converter is null");
 
         Converter<java.util.Date> prev = toDate;
 
         toDate = converter;
 
         return prev;
 
     }
 
     /**
      *
      * @param converter
      * @return
      */
     public static Converter<java.util.Date> registerDateTimeConverter( Converter<java.util.Date> converter ) {
         if( converter==null ) throw new IllegalArgumentException("converter is null");
 
         Converter<java.util.Date> prev = toDateTime;
 
         toDateTime = converter;
 
         return prev;
 
     }
 
     /**
      *
      * @param converter
      * @return
      */
     public static Converter<java.util.Date> registerTimeConverter( Converter<java.util.Date> converter ) {
         if( converter==null ) throw new IllegalArgumentException("converter is null");
 
         Converter<java.util.Date> prev = toTime;
 
         toTime = converter;
 
         return prev;
 
     }
 
     
     private static void setCellValue( XSSFCell cell, XmlObject value, STXmlDataType.Enum xmlDataType  ) {
         SimpleValue v = (SimpleValue) value;
 
         try {
             switch (xmlDataType.intValue()) {
                 case STXmlDataType.INT_INTEGER:
                 case STXmlDataType.INT_INT:
                     cell.setCellValue(toInt.convert(v));
                     break;
                 case STXmlDataType.INT_DOUBLE:
                     cell.setCellValue(toDouble.convert(v));
                     break;
                 case STXmlDataType.INT_FLOAT:
                     cell.setCellValue(toFloat.convert(v));
                     break;
                 case STXmlDataType.INT_LONG:
                     cell.setCellValue(toLong.convert(v));
                     break;
                 case STXmlDataType.INT_DATE:
                     cell.setCellValue( toDate.convert(v) );
                     break;
                 case STXmlDataType.INT_DATE_TIME:
                     cell.setCellValue( toDateTime.convert(v) );
                     break;
                 case STXmlDataType.INT_TIME:
                     cell.setCellValue( toTime.convert(v) );
                     break;
                 case STXmlDataType.INT_BOOLEAN:
                     cell.setCellValue(toBoolean.convert(v));
                     break;
                 default:
                     cell.setCellValue(v.getStringValue());
             }
         } catch (Exception ex) {
             log.warn( String.format("conversion of [%s] to [%s]error [%s] ! Tring to convert to string: ",  v.getStringValue(), xmlDataType.toString(), ex.getMessage())   );
             cell.setCellValue(v.getStringValue());
         }
         
     }
 
     /**
      *
      * @param sheet
      * @param table
      * @param xmlSourceObject
      * @throws IOException
      * @throws XmlException
      */
     private static void processTableSingleCells( XSSFSheet sheet, PackagePart singleCells, XmlObject xmlSourceObject ) throws IOException, XmlException {
         SingleXmlCellsDocument singleCellsXml = SingleXmlCellsDocument.Factory.parse( singleCells.getInputStream() );
 
         CTSingleXmlCells scs = singleCellsXml.getSingleXmlCells();
 
         assert scs!=null;
 
         for( CTSingleXmlCell sc :  scs.getSingleXmlCellArray() ) {
             final String ref = sc.getR();
 
             assert ref!=null;
             if( ref==null ) {
                 final String msg = String.format( "R attribute of singleCell [%] is null!", sc.getId() );
                 log.error( msg);
                 throw new IllegalStateException(msg);
             }
 
             final CellReference cellRef = new CellReference( ref );
 
             final CTXmlCellPr cellPr = sc.getXmlCellPr();
 
             assert cellPr!=null;
             if( cellPr==null ) {
                 final String msg = String.format( "cell pr for cell [%d] is null. Will be ignored", sc.getId() );
                 log.warn( msg);
                 continue;
             }
 
             log.debug( String.format( "cellPr [%s] being procesed", cellPr.getUniqueName()));
             final CTXmlPr pr = cellPr.getXmlPr();
 
             assert pr!=null;
             if( pr==null ) {
                 final String msg = String.format( "xmlPr for cell [%d] uniqueName [%s] is null. Will be ignored", sc.getId(), cellPr.getUniqueName() );
                 log.warn( msg);
                 continue;
             }
 
             final String xpath = pr.getXpath();
 
             assert xpath!=null;
             if( xpath==null ) {
                 final String msg = String.format( "xpath attribute for xmlPr for cell [%d] uniqueName [%s] is null. Will be ignored", sc.getId(), cellPr.getUniqueName() );
                 log.warn( msg);
                 continue;
             }
 
             // PERFORM AN XPATH QUERY
             final String xpathQuery = String.format( "$this%s", xpath );
 
             log.debug( String.format( "perfrom xpath query [%s]", xpathQuery) );
             
             XmlObject[] bindValues = xmlSourceObject.selectPath(xpathQuery);
 
             assert bindValues!=null;
             if( bindValues==null ) {
                 final String msg = String.format( "No values match for xpath query [%s] for cell [%d] uniqueName [%s]. Cell will be ignored", xpathQuery,  sc.getId(), cellPr.getUniqueName() );
                 log.warn( msg);
                 continue;
             }
 
             assert bindValues.length == 1: "bindValues.length=" + bindValues.length;
             if( bindValues.length!=1) {
                 final String msg = String.format( "Result of xpath query [%s] doesn't match with just one value but with [%d]. Cell will be ignored", xpathQuery,  bindValues.length );
                 log.warn( msg);
                 continue;
             }
 
             final int rowNum = cellRef.getRow();
 
             XSSFRow row = sheet.getRow(rowNum);
 
             if( row == null ) {
                 row = sheet.createRow(rowNum);
             }
 
             assert row!=null;
             if( row==null ) {
                 final String msg = String.format( "detected problem to get/create row [%d]. Will be ignored!", rowNum );
                 log.warn( msg);
                 continue;
             }
 
             final int cellNum = cellRef.getCol();
 
             XSSFCell cell = row.getCell( cellNum, Row.CREATE_NULL_AS_BLANK  );
 
             assert cell!=null;
             if( cell==null ) {
                 final String msg = String.format( "detected problem to get cell [%d,%d]. Will be ignored!", rowNum, cellNum );
                 log.warn( msg);
                 continue;
             }
 
             final XmlObject value = bindValues[0];
 
             setCellValue( cell, value, pr.getXmlDataType() );
             //cell.setCellValue( ((SimpleValue)value).getStringValue() );
 
         }
     }
 
     /**
      * 
      * @param sheet
      * @param table
      * @param xmlSourceObject
      * @throws IOException
      * @throws XmlException
      */
     private static void processTable( XSSFSheet sheet, PackagePart table, XmlObject xmlSourceObject ) throws IOException, XmlException {
 
         TableDocument tableXml = TableDocument.Factory.parse(table.getInputStream());
 
         CTTable sht = tableXml.getTable();
 
         assert sht!=null;
 
         final String ref = sht.getRef();
 
         assert ref!=null;
         if( ref==null ) {
             final String msg = String.format( "ref attribute of table [%] is null!", sht );
             log.error( msg);
             throw new IllegalStateException(msg);
         }
 
         final String [] cellRef = ref.split(":");
 
         assert cellRef!=null;
         assert cellRef.length==2;
 
         if( cellRef==null || cellRef.length!=2 ) {
             final String msg = String.format( "ref value [%s] is not valid !", ref );
             log.error( msg);
             throw new IllegalStateException(msg);
         }
 
         final CellReference startCell = new CellReference( cellRef[0] );
         final CellReference endCell = new CellReference( cellRef[1] );
 
         final CTTableColumns columns = sht.getTableColumns();
 
         assert columns!=null;
         assert columns.getCount()>0;
 
         if( columns==null || columns.getCount()<=0 ) {
             final String msg = String.format( "non columns detected!" );
             log.error( msg);
             throw new IllegalStateException(msg);
         }
 
        XSSFCellStyle[] _cellStyle = new XSSFCellStyle[(int)columns.getCount()];
        
         // FOR EACH COLUMN
         for( int col =0 ; col< columns.getCount(); ++col ) {
 
             final CTTableColumn tableCol = columns.getTableColumnArray(col);
 
             assert tableCol!=null;
             if( tableCol==null ) {
                 final String msg = String.format( "column [%d] is null. Will be ignored ", col );
                 log.warn( msg);
                 continue;
             }
 
             final CTXmlColumnPr colPr = tableCol.getXmlColumnPr();
 
             assert colPr!=null;
             if( colPr==null ) {
                 final String msg = String.format( "column pr for column [%d] is null. Will be ignored", col );
                 log.warn( msg);
                 continue;
             }
 
             final String xpath = colPr.getXpath();
 
             assert xpath!=null;
             if( xpath==null ) {
                 final String msg = String.format( "xpath attribute for column [%d] is null. Will be ignored", col );
                 log.warn( msg);
                 continue;
             }
 
             // PERFORM AN XPATH QUERY
             final String xpathQuery = String.format( "$this%s", xpath );
 
             XmlObject[] bindValues = xmlSourceObject.selectPath(xpathQuery);
 
             assert bindValues!=null;
             assert bindValues.length > 0;
             if( bindValues==null ) {
                 final String msg = String.format( "no values match for xpath query [%s] for column [%d]. Column will be ignored", xpathQuery, col );
                 log.warn( msg);
                 continue;
             }
 
             
             // FILL SHEET
             int rowNum = endCell.getRow();
 
             for( XmlObject value : bindValues ) {
 
                 XSSFRow row = sheet.getRow(rowNum);
 
                 if( row == null ) {
                     row = sheet.createRow(rowNum);
                 }
 
                 assert row!=null;
                 if( row==null ) {
                     final String msg = String.format( "detected problem to get/create row [%d]. Will be ignored!", rowNum );
                     log.warn( msg);
                     continue;
                 }
 
                 final int cellNum = startCell.getCol() + col;
 
                 XSSFCell cell = row.getCell( cellNum, Row.CREATE_NULL_AS_BLANK  );
                if( _cellStyle[col] == null ) {
                    _cellStyle[col] = cell.getCellStyle();
                }
                else {
                    cell.setCellStyle( _cellStyle[col] );
                }
 
                 assert cell!=null;
                 if( cell==null ) {
                     final String msg = String.format( "detected problem to get cell [%d,%d]. Will be ignored!", rowNum, cellNum );
                     log.warn( msg);
                     continue;
                 }
 


                 setCellValue( cell, value, colPr.getXmlDataType() );
                 //cell.setCellValue( ((SimpleValue)value).getStringValue() );
 
                 ++rowNum;
             }
         }
 
     }
 
     public static void importXML( XSSFWorkbook wb, java.io.Reader xmlSource, java.io.OutputStream out ) throws XmlException, IOException {
         assert wb!=null;
         assert xmlSource!=null;
         assert out!=null;
 
         if( wb==null ) throw new IllegalArgumentException("workbook is null!");
         if( xmlSource==null ) throw new IllegalArgumentException( "reader is null!");
         if( out==null ) throw new IllegalArgumentException( "out is null!");
 
         int numOfSheets = wb.getNumberOfSheets();
 
         assert numOfSheets>=1;
         if( numOfSheets<1 ) {
             log.warn( "workbook doesn't contain sheet!");
             return;
         }
 
         // Load XML
         // TODO performa validation gathering schema from MapInfo
         XmlObject xmlSourceObject = XmlObject.Factory.parse( xmlSource );
 
         // For each Sheet
         for( XSSFSheet sheet : wb ) {
 
             // For each relations
             for( POIXMLDocumentPart doc : sheet.getRelations() ) {
 
                 final PackagePart part = doc.getPackagePart();
                 assert null!=part;
 
                 if( part==null ) {
                     log.warn( String.format("part of relation [%s] is null. Will be ignored!", doc ));
                     continue;
                 }
 
                 log.debug( String.format("contentType [%s]", part.getContentType() ));
 
                 if( part.getContentType().equalsIgnoreCase(TABLE_CONTENT_TYPE) ) {
                     processTable( sheet, part, xmlSourceObject);
                 }
                 else if( part.getContentType().equalsIgnoreCase(TABLESINGLECELLS_CONTENT_TYPE) ) {
                     processTableSingleCells( sheet, part, xmlSourceObject);
                 }
 
 
 
 
             }
         }
 
         wb.write( out );
         
     }
     
 }
