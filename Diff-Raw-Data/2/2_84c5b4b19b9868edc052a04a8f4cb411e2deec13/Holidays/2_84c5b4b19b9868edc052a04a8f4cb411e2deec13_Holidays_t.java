 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package models;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import utils.fileaccess.BinaryFileReader;
 import utils.fileaccess.BinaryFileWriter;
 
 /**
  *
  * @author jaspertomas
  */
 public class Holidays {
 
     //---------------SINGLETON-------------------
 
     static Holidays instance;
 
     public static Holidays getInstance() {
         if (instance == null) {
             instance = new Holidays();
         }
         return instance;
     }
     //---------------VARIABLES---------------------  
 //    public static final String REGULAR="R";
 //    public static final String SPECIAL="S";
 //    public static final String OTHER="O";
     public static final Integer REGULAR=0;
     public static final Integer SPECIAL=1;
     public static final Integer OTHER=2;
 
     private static final String OUTPUT_FILE_NAME = "holidays.dat";
     public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
     public static final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
 
     
     ArrayList<Holiday> items,allItems=new ArrayList<Holiday>();
 
     public ArrayList<Holiday> getItems() {
         if(items==null)
         {
             items=new ArrayList<Holiday>();
             load();
         }
         
         return items;
     }
 //    public void reset() {
 //        items.clear();
 //    }
 
     public void save()
     {
         //initialize if necessary
         getItems();
         
         BinaryFileWriter writer = new BinaryFileWriter();
         writer.connectToFile(OUTPUT_FILE_NAME);
         
         for(Holiday item:allItems)
         {
             writer.writeString(item.getName());
             writer.writeInt(item.getType());
 //            writer.writeString(dateFormat.format(item.getDate()));
             writer.writeDate(item.getDate());
         }
         writer.close(); 
     }
     public void load()
     {
         String currentYear=Settings.getInstance().getCurrentYear();
         
         items.clear();
         allItems.clear();
         
         Holiday h;
                 
         BinaryFileReader reader = new BinaryFileReader();
         boolean result=reader.connectToFile(OUTPUT_FILE_NAME);
         
         if(!result)
         {
             //file not found
             System.out.println("File "+OUTPUT_FILE_NAME+" not found");
             return;
         }
         
         while(reader.notEOF())
         {
             h=new Holiday();
             h.setName(reader.readString());
             if(!reader.notEOF())break;
             h.setType(reader.readInt());
             try {
                 h.setDate(dateFormat.parse(reader.readString()));
             } catch (ParseException ex) {
                 ex.printStackTrace();
 //                Logger.getLogger(Holidays.class.getName()).log(Level.SEVERE, null, ex);
             }
             
             //add holidays only if their year matches this year
             if(yearFormat.format(h.getDate()).contentEquals(currentYear))
                 items.add(h);
             allItems.add(h);
         }
         Collections.sort(items);
         reader.close();        
     }
 
     //add items that don't already exist in the items array
 //    void generate(ArrayList<Holiday> items) {
 ////        EmployeeList temp=new EmployeeList();
 //        
 //        //scan employee list for matching nickname; 
 //        //if it doesnt exist, add it
 ////        for(String name:items)
 ////        {
 ////            if(items.getByName(name)==null)
 ////            items.add(new Holiday(name,"","","",0d,0d,0d));
 ////        }
 ////        save();
 //    }
     public void generate(String yearstring) {
         try {
             //Regular holidays
             allItems.add(new Holiday("New Year",Holidays.REGULAR,dateFormat.parse(yearstring+"/01/01")));
             allItems.add(new Holiday("Araw ng Kagitingan",Holidays.REGULAR,dateFormat.parse(yearstring+"/4/9")));
             allItems.add(new Holiday("Labor Day",Holidays.REGULAR,dateFormat.parse(yearstring+"/5/1")));
             allItems.add(new Holiday("Independence Day",Holidays.REGULAR,dateFormat.parse(yearstring+"/6/12")));
             allItems.add(new Holiday("Bonifacio Day",Holidays.REGULAR,dateFormat.parse(yearstring+"/11/30")));
             allItems.add(new Holiday("Christmas",Holidays.REGULAR,dateFormat.parse(yearstring+"/12/25")));
             allItems.add(new Holiday("Rizal Day",Holidays.REGULAR,dateFormat.parse(yearstring+"/12/30")));
 
             //movable regular holidays for 2014
             if(yearstring.contentEquals("2014"))
             {
                 allItems.add(new Holiday("Maundy Thursday",Holidays.REGULAR,dateFormat.parse(yearstring+"/4/17")));
                 allItems.add(new Holiday("Good Friday",Holidays.REGULAR,dateFormat.parse(yearstring+"/4/18")));
                 allItems.add(new Holiday("National Heroes' Day",Holidays.REGULAR,dateFormat.parse(yearstring+"/8/25")));
             }
 
             //Special non/working holidays
             allItems.add(new Holiday("Ninoy Aquino Day",Holidays.SPECIAL,dateFormat.parse(yearstring+"/8/21")));
             allItems.add(new Holiday("All Saints' Day",Holidays.SPECIAL,dateFormat.parse(yearstring+"/11/1")));
 //            allItems.add(new Holiday("All Souls' Day",Holidays.SPECIAL,dateFormat.parse(yearstring+"/11/2")));
             allItems.add(new Holiday("Christmas Eve",Holidays.SPECIAL,dateFormat.parse(yearstring+"/12/24")));
             allItems.add(new Holiday("Day after Christmas",Holidays.SPECIAL,dateFormat.parse(yearstring+"/12/26")));
             allItems.add(new Holiday("Last Day of the Year",Holidays.SPECIAL,dateFormat.parse(yearstring+"/12/31")));
 
             //movable special holidays for 2014
             if(yearstring.contentEquals("2014"))
             {
                 allItems.add(new Holiday("Chinese New Year",Holidays.SPECIAL,dateFormat.parse(yearstring+"/1/31")));
                 allItems.add(new Holiday("Black Saturday",Holidays.SPECIAL,dateFormat.parse(yearstring+"/4/19")));
             }
     
             Collections.sort(allItems);
             save();
         } catch (ParseException ex) {
             ex.printStackTrace();
         }
 //        load();
     }
     public void delete(Holiday holiday)
     {
         //initialize if necessary
         getItems();
         
         allItems.remove(holiday);
         save();
         items.remove(holiday);
     }
     public void add(Holiday holiday)
     {
         //initialize if necessary
         getItems();
         
         allItems.add(holiday);
         Collections.sort(allItems);
         save();
         items.add(holiday);
         Collections.sort(items);
     }    
     public void edit(Holiday h,String name, Integer type, Date date)
     {
         //initialize if necessary
         getItems();
                 
         h.setName(name);
         h.setType(type);
         h.setDate(date);
         
         Collections.sort(allItems);
         save();
         Collections.sort(items);
     }      
 
     public Holiday getByDate(Date date) {
         //initialize if necessary
         getItems();
         
         for(Holiday h:items)
         {
             if(h.getDate().equals(date))
                 return h;
         }
         return null;
     }
     public Holiday getByDateString(String datestring) throws ParseException {
         //initialize if necessary
         getItems();
         
         Date date=dateFormat.parse(datestring);
        for(Holiday h:allItems)
         {
             if(h.getDate().equals(date))
                 return h;
         }
         return null;
     }
 
     public Holiday getByName(String name) {
         //initialize if necessary
         getItems();
         
         for(Holiday h:items)
         {
             if(h.getName().equals(name))
                 return h;
         }
         return null;
     }    
     public Date getFreeDate() {
         Date date;
         java.util.Calendar c = java.util.Calendar.getInstance();
         String year=Settings.getInstance().getCurrentYear();
         
         try {
             date = dateFormat.parse(Settings.getInstance().getCurrentYear()+"-01-01");
             
             while(yearFormat.format(date).contentEquals(year))
             {
                 if(getByDate(date)==null)
                 {
                     return date;
                 }
 
                 c.setTime(date);
                 c.add(java.util.Calendar.DATE, 1);  // number of days to add
                 date=c.getTime();
             }
         } catch (ParseException ex) {
             ex.printStackTrace();
         }
         return null;
     }        
     public String getFreeHolidayName() {
         String name="--New Holiday--";
         Integer counter=1;
         while(true)
         {
             if(getByName(name)==null)
             {
                 return name;
             }
             counter++;
             name="--New Holiday"+counter.toString()+"--";
         }
     }        
 }
