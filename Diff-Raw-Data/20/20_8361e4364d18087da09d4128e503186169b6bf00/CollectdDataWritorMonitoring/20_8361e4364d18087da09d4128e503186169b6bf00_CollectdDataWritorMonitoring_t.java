 package yourcast.mongodb.extractor;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: fabien foerster
  * Date: 09/08/13
  * Time: 10:19
  */
 public class CollectdDataWritorMonitoring extends CollectdDataWritor {
     private long start,ligne_max ;
     private int lastColumnToWrite;
     private int lastColumn;
     private Sheet sheet ;
 
     public CollectdDataWritorMonitoring(String outputName, long start ,long end) throws IOException, InvalidFormatException {
         super(outputName);
         lastColumnToWrite = 1 ;
         lastColumn = 1 ;
         sheet = getSheet(sxssfWorkbook,"probes");
         this.start = start ;
         // The max number of line correspond to the second between start and end but we have the time in milliseconds
         this.ligne_max = (end - start ) / 1000;
     }
 
     @Override
     public void writeToExcel(DBCursor cursor, CollectdQuery query) throws IOException, ParseException, InvalidFormatException {
         throw new RuntimeException("Not implemented");
     }
 
 
     public void writeToExcel() throws IOException, ParseException, InvalidFormatException {
         System.out.println("Begin writing to "+outputName);
         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
         GregorianCalendar cal =new GregorianCalendar();
         DBObject data ;
         Row r ;
         Cell c ;
         int k = 0 ;
         DBObject[] oldData = new DBObject[cursors.size()];
         for(Map.Entry<String,DBCursor> entry : cursors.entrySet()){
             if(entry.getValue().hasNext()){
                 oldData[k] = entry.getValue().next();
                 createColumnName(oldData[k],entry.getKey());
             }
             k++;
         }
        setAutoSizeColumn(lastColumnToWrite);
         k = 0 ;
         lastColumnToWrite = 1 ;
         int number_values;
         int i = 1 ;
         boolean keepOnLooping = true ;
         while(keepOnLooping){
             keepOnLooping = false ;
             if(i > ligne_max){
                 break;
             }
             r = getRow(sheet,i);
             c = getCell(r,0);
             cal.setTimeInMillis(start);
             String date = simpleDateFormat.format(cal.getTime());
             c.setCellValue(date);
             start += 1000 ;
             for(Map.Entry<String,DBCursor> entry : cursors.entrySet()){
                 number_values = 1 ;
                 keepOnLooping = keepOnLooping || entry.getValue().hasNext() ;
                 if(entry.getValue().hasNext()){
                     if(oldData[k] == null){
                         data = entry.getValue().next();
                         oldData[k] = data ;
                     } else {
                         data = oldData[k];
                     }
                     r = getRow(sheet,i);
                     c = getCell(r,0);
                     cal.setTime((Date)data.get("time"));
                     date = simpleDateFormat.format(cal.getTime());
                     c.setCellType(Cell.CELL_TYPE_STRING);
                     if(date.equals(c.getStringCellValue())){
                         BasicDBList values = (BasicDBList) data.get("values");
                         number_values = values.size() ;
                         for(int j = 0 ; j < number_values ; j++){
                             c = getCell(r , lastColumnToWrite +j);
                             double value =  (Double)values.get(j);
                             c.setCellValue(value);
                         }
                         oldData[k] = null ;
                     }
                 }
                 k++;
                 lastColumnToWrite += number_values ;
             }
             k = 0 ;
             i++;
             lastColumn = lastColumnToWrite > lastColumn ? lastColumnToWrite : lastColumn;
             lastColumnToWrite = 1 ;
         }
        setAutoSizeColumn(1);
         System.out.println("Finish writing to "+outputName);
     }
 
 
     private void createColumnName(DBObject data , String queryName ){
         Row r = getRow(sheet,0);
         Cell c = getCell(r,0);
         c.setCellValue("time");
         BasicDBList names = (BasicDBList) data.get("dsnames");
         String columnName = queryName;
         if(names.size() > 1){
             for(int i = 0 ; i < names.size() ; i++){
                 c = getCell(r, lastColumnToWrite +i);
                 columnName += "."+names.get(i).toString();
                 c.setCellValue(columnName);
             }
             lastColumnToWrite += names.size();
         } else {
             c = getCell(r, lastColumnToWrite);
             c.setCellValue(columnName);
             lastColumnToWrite++ ;
         }
 
     }
 
    private void setAutoSizeColumn(int lastColumn){
         for(int i = 0 ; i < lastColumn; i++){
             sheet.autoSizeColumn(i);
         }
     }
 
     @Override
     public void close() throws IOException {
         super.close();
         for(DBCursor cursor : cursors.values()){
             cursor.close();
         }
         lastColumnToWrite = 1 ;
     }
 }
