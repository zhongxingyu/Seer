 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.tripbrush.service;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import org.apache.poi.xssf.usermodel.XSSFCell;
 import org.apache.poi.xssf.usermodel.XSSFRow;
 import org.apache.poi.xssf.usermodel.XSSFSheet;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.hibernate.classic.Session;
 import org.hibernate.criterion.Restrictions;
 import uk.tripbrush.model.travel.Attraction;
 import uk.tripbrush.model.travel.AttractionEvent;
 import uk.tripbrush.model.travel.AttractionSeason;
 import uk.tripbrush.model.travel.AttractionTime;
 import uk.tripbrush.model.travel.Category;
 import uk.tripbrush.util.DateUtil;
 import uk.tripbrush.util.PojoConstant;
 
 /**
  *
  * @author sseetal
  */
 public class OlympicAttractionService {
 
 
     public static String getCellValue(Iterator cells) {
         XSSFCell cell = (XSSFCell) cells.next();
         String result  = "";
         try {
             result = ""+cell.getDateCellValue();
         }
         catch (Exception e) {
             cell.setCellType(XSSFCell.CELL_TYPE_STRING);
             result = cell.getStringCellValue();
         }        
         return result;
     }
     
     public static String[] getRow(XSSFRow row) {
         ArrayList<String> result = new ArrayList<String>();
         Iterator cells = row.cellIterator();
 
         while (cells.hasNext()) {
             result.add(getCellValue(cells));
         }
         return result.toArray(new String[0]);
     }
     
     public static void addTime(Session session,Attraction attraction,int dow,String time) {
         AttractionTime result = new AttractionTime();
         result.setAttraction(attraction);
         result.setDow(dow);
         String[] seasonsplit = time.split(";");
         String[] timesplit = seasonsplit[0].split("-");
         if (timesplit.length==2) {
             String[] from = timesplit[0].trim().split(":");
             if (from.length==2) {
                 String[] to = timesplit[1].trim().split(":");
                 result.setStarthour(Integer.parseInt(from[0].trim()));
                 result.setStartminute(Integer.parseInt(from[1].trim()));
                 result.setEndhour(Integer.parseInt(to[0].trim()));
                 result.setEndminute(Integer.parseInt(to[1].trim())); 
                 if (result!=null) {
                     session.saveOrUpdate(result);
                 }
             } 
         }
         if (seasonsplit.length>1) {
             for (int counter=1;counter<seasonsplit.length;counter++) {
                 timesplit = seasonsplit[counter].split("-");
                 AttractionSeason season = new AttractionSeason();
                 season.setAttraction(attraction);
                 season.setDow(dow);
                 if (timesplit.length==4) {
                     String fromdate = timesplit[0].trim();
                     String todate = timesplit[1].trim();
                     season.setFromday(Calendar.getInstance());
                     season.setToday(Calendar.getInstance());
                     season.getFromday().setTime(DateUtil.formatDate(fromdate));
                     season.getToday().setTime(DateUtil.formatDate(todate));
                     String[] from = timesplit[2].trim().split(":");
                     if (from.length==2) {
                         String[] to = timesplit[3].trim().split(":");
                         season.setStarthour(Integer.parseInt(from[0].trim()));
                         season.setStartminute(Integer.parseInt(from[1].trim()));
                         season.setEndhour(Integer.parseInt(to[0].trim()));
                         season.setEndminute(Integer.parseInt(to[1].trim()));
                         if (season!=null) {
                             session.saveOrUpdate(season);
                         }
                     }
                 }
             }
         }
     }
     
     public static Category getOlympicCategory() {
         Session session = Database.getSession();
         Category cat = (Category)session.createCriteria(PojoConstant.CATEGORY_MODEL).add(Restrictions.eq("description","olympic")).uniqueResult();
         if (cat==null) {
             cat = new Category();
             cat.setDescription("olympic");
             cat.setName("Olympic");
             session.save(cat);
         }
         return cat;
         
     }
     
     public static Attraction getAttraction(Category cat,String sport,String location) {
         Session session = Database.getSession();
         Attraction attraction = (Attraction)session.createCriteria(PojoConstant.ATTRACTION_MODEL).add(Restrictions.eq("name",sport+"-"+location)).uniqueResult();
         if (attraction==null) {
             System.out.println("ERROR: Could not find attraction '" + sport+"-"+location + "'. Please correct before continuing");
             System.exit(-1);
         }
         return attraction;        
     }
     
     public static String getRowValue(String[] row, int id) {
         if (row.length>id) {
             return row[id];
         }
         return "";
     }
     
     public static void process(InputStream is) throws Exception {
         XSSFWorkbook wb = new XSSFWorkbook(is);  
         
         XSSFSheet sheet = wb.getSheetAt(1);
         Iterator rows = sheet.rowIterator();
 
         Session session = Database.getSession();
 
         Database.beginTransaction();
 
         Category cat = getOlympicCategory();
         
         SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
         
         String[] row = getRow((XSSFRow) rows.next());
         while (rows.hasNext()) {
             row = getRow((XSSFRow) rows.next());
             
             
             String category = getRowValue(row,0);
             String location = getRowValue(row,1);
             
             Attraction attraction = getAttraction(cat, category, location);
             attraction.setDescription(getRowValue(row,2));
             attraction.setImageFileName_small(getRowValue(row,3));
             attraction.setImageFileName(getRowValue(row,4));
             attraction.setPostcode(getRowValue(row,5));
             session.merge(attraction);
         }
 
         Database.commitTransaction();
     }
     
     public static String convertArray(String[] input) {
         StringBuffer result = new StringBuffer();
         for (String r : input) {
             result.append(r);
             result.append("\t");
         }
         return result.toString();
     }    
     
     public static void main(String[] args) throws Exception {
        String file = "/Users/sseetal/Dropbox/Life Made Easy Ltd/Olympic Sched.xlsx";
         //String file = "c://Users//Samir//Documents//My DropBox//Mauritius//input//Agro-Industry and FS//input.xlsx";
         InputStream input = new BufferedInputStream(new FileInputStream(file));
         process(input);      
     }
 }
