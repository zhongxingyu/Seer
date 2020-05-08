 package mensa.swfr;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import android.content.Context;
 import android.text.format.Time;
 
 public class ReadURL {
 	public static String[] inhalt;
     public static String data;
     int x = 0;
     public static int r = 0;
     public int week=0;
     String[] returner = new String[2];
    
     public String[] getData(Context context) {
 	     //holt String aus Datei und weist ihn data zu
          try{
              FileInputStream fIn = context.openFileInput("settings.dat");     
              Reader reader = new InputStreamReader(fIn);
              BufferedReader bufferedReader = new BufferedReader(reader);
              data = bufferedReader.readLine();
              fIn.close();
            }
          catch (Exception e) {      
         	 e.printStackTrace();
         	 returner[0]="";
         	 return returner;
          }
         data=data.replaceAll(" valign=\"top\"", "");
 		data=data.replaceAll("<b>", "");
 		data=data.replaceAll("</b>", "");
 
 	    
 		//data wird in einzelteile gesplittet
 	    inhalt = data.split("!!!");
 	    //inhalt[0] = Datum von Montag der gespeicherten Woche 0
 	    //inhalt[1] = Mensa_Case
 	    //inhalt[2,3,4] = url0, url1, url2;
 		
 	    //prüft ob aktualisiert werden muss (today = zugriffsdatum) 
 		Time monday = new Time();
 		monday.parse(inhalt[0].substring(0,8));
 		        
         Time today = new Time();
         today.setToNow();
         
         //x = unterschied in tagen
         if(monday.month != today.month){
         	x = today.monthDay;
         	today.set(today.monthDay - x, today.month, today.year);
         	today.normalize(true);
         	x = x + today.monthDay-monday.monthDay;
         }
         else x = today.monthDay-monday.monthDay;     
         
         switch(x){
         	case 0:
         	case 1:
         	case 2:  
         	case 3:
         	case 4:
         	case 5:  week = 0; break;
         	case 6:  //sonntag
         	case 7:
         	case 8:
         	case 9:  
         	case 10:
         	case 11:
         	case 12: week = 1; break;
         	case 13: //sonntag
         	case 14:
         	case 15:
         	case 16: 
         	case 17:
         	case 18:
         	case 19: week = 2; break;
        }
          
        returner[0]=inhalt[1];
         returner[1]=""+week;
         if(x>12) r = 1;
 	    return returner;//gibt mensa_id zurück
 
      }   
 }
