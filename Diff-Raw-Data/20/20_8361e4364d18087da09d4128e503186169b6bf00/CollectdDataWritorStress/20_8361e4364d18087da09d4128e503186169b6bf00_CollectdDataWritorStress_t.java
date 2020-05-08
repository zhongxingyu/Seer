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
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: fabien foerster
  * Date: 08/08/13
  * Time: 23:01
  */
 public class CollectdDataWritorStress extends CollectdDataWritor {
 
     private int row_offset;
     private int col_offset ;
     private int oldTimestamp ;
     private long nbRow ;
     private List<OverviewSheet> overviewSheets ;
 
     public CollectdDataWritorStress(String outputName,int row_offset , int col_offset,long nbRow,List<OverviewSheet> overviewSheets) throws IOException, InvalidFormatException {
         super(outputName);
         this.row_offset = row_offset ;
         this.col_offset = col_offset ;
         oldTimestamp = -1 ;
         this.nbRow = nbRow ;
         this.overviewSheets = overviewSheets ;
 
     }
 
     @Override
     public void writeToExcel(DBCursor cursor , CollectdQuery query ) throws IOException, ParseException, InvalidFormatException {
         System.out.println("Write :"+query.getQueryName());
         Sheet[] sheets = createSheets(cursor.copy(),query);
         for(Sheet s : sheets){
             setDefaultText(s);
         }
         writeMultipleSheet(sheets, cursor);
     }
 
 
     private Sheet[] createSheets(DBCursor cursor , CollectdQuery query){
         Sheet[] sheets = new Sheet[1] ;
         if(cursor.hasNext()){
             BasicDBList names = (BasicDBList) cursor.next().get("dsnames");
             if(names.size() > 1){
                 OverviewSheet current = null ;
                 for(OverviewSheet overviewSheet : overviewSheets){
                     if(overviewSheet.contains(query.getQueryName())){
                         overviewSheet.removeQuery(query.getQueryName());
                         current = overviewSheet;
                         break;
                     }
                 }
                 sheets = new Sheet[names.size()];
                 for(int i = 0 ; i < names.size() ; i++ ){
                     String sheetName = query.getQueryName() + "."+ names.get(i).toString();
                     sheets[i] = getSheet(sxssfWorkbook, sheetName);
                     if(current != null){
                         current.addQuery(sheetName);
                     }
                 }
                 return sheets;
             }
 
         }
         sheets[0] = getSheet(sxssfWorkbook, query.getQueryName());
         return sheets;
     }
 
     private void createFormulas(Sheet s , int row){
         Cell min,max,average,stdev;
         min = getCell(getRow(s, row-1), 11);
         min.setCellType(Cell.CELL_TYPE_FORMULA);
         min.setCellFormula("MIN(B"+row+":K"+row+")");
         min.setCellStyle(cellStyle);
         max = getCell(getRow(s, row-1), 12);
         max.setCellType(Cell.CELL_TYPE_FORMULA);
         max.setCellFormula("MAX(B"+row+":K"+row+")");
         max.setCellStyle(cellStyle);
         average = getCell(getRow(s, row-1), 13);
         average.setCellType(Cell.CELL_TYPE_FORMULA);
         average.setCellFormula("AVERAGE(B"+row+":K"+row+")");
         average.setCellStyle(cellStyle);
         stdev = getCell(getRow(s, row-1), 14);
         stdev.setCellType(Cell.CELL_TYPE_FORMULA);
         stdev.setCellFormula("STDEV(B"+row+":K"+row+")");
         stdev.setCellStyle(cellStyle);
 
     }
 
     private int writeMultipleSheet(Sheet[] sheets , DBCursor cursor){
         Row[] rows = new Row[sheets.length];
         Cell[] cells = new Cell[sheets.length];
         DBObject data ;
         int i = 0;
         try{
 
             for(i = 0 ; cursor.hasNext(); i++){
                 data = cursor.next();
                 for(int j = 0 ; j < rows.length ; j++){
                     rows[j] = getRow(sheets[j],i+ row_offset);
                     cells[j] = getCell(rows[j],0);
                     int timestamp = (int)((Date)data.get("time")).getTime()/1000 ;
                     i += timestamp_offset(timestamp);
                     cells[j].setCellStyle(cellStyle);
                     cells[j].setCellValue(i);
                     cells[j] = getCell(rows[j], col_offset);
                     BasicDBList values = (BasicDBList) data.get("values");
                     for(int k = 0 ; k < sheets.length ; k++){
                         Double value = (Double)values.get(k);
                         cells[j].setCellType(Cell.CELL_TYPE_NUMERIC);
                         cells[j].setCellValue(value);
                     }
                     createFormulas(sheets[j],i+row_offset+1);
                 }
             }
         }finally {
             cursor.close();
         }
         return i;
     }
 
     public void writeOverviewSheet(){
         for(OverviewSheet overviewSheet : overviewSheets){
             Sheet s = getSheet(sxssfWorkbook,overviewSheet.getName());
             Row r = getRow(s,0);
             Cell c = getCell(r,0);
             c.setCellValue("T");
             int j = 1 ;
             for(String name : overviewSheet.getQueryName()){
                 c = r.createCell(j);
                 c.setCellValue(name.replaceAll(overviewSheet.getName()+"[\\-\\.]?",""));
                 j++;
             }
            for(int i = 0 ; i < j ; i++){
                s.autoSizeColumn(i);
            }
             for(int i = 1 ; i <= nbRow ; i ++){
                 r = getRow(s,i);
                 c = getCell(r,0);
                 c.setCellValue(i);
                 j=1;
                 for(String name : overviewSheet.getQueryName()){
                     c = getCell(r,j);
                     c.setCellType(Cell.CELL_TYPE_FORMULA);
                     c.setCellFormula("'"+name+"'!N"+(i+1));
                     j++;
                 }
 
             }
            s.autoSizeColumn(0);
             sxssfWorkbook.setSheetOrder(overviewSheet.getName(),0);
         }
 
     }
 
     private int timestamp_offset(int timestamp){
         if( oldTimestamp == -1){
             return 0 ;
         } else {
             int offset = timestamp - oldTimestamp ;
             oldTimestamp = timestamp ;
             return offset - 1 ;
         }
 
     }
 
 
     private void setDefaultText(Sheet s){
         Row r = getRow(s, 0);
         Cell c ;
         c = getCell(r, 0);
         c.setCellValue("T");
         c.setCellStyle(cellStyle);
         for(int i = 1 ; i <= 10 ; i++){
             c = getCell(r, i);
             c.setCellValue("#"+i);
             c.setCellStyle(cellStyle);
         }
         c = getCell(r, 11);
         c.setCellValue("MIN");
         c.setCellStyle(cellStyle);
         c = getCell(r, 12);
         c.setCellValue("MAX");
         c.setCellStyle(cellStyle);
         c = getCell(r, 13);
         c.setCellValue("AVG");
         c.setCellStyle(cellStyle);
         c = getCell(r, 14);
         c.setCellValue("STDEV");
         c.setCellStyle(cellStyle);
     }
 }
