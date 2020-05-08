 package www.forum_rallye.data;
 
 import java.io.IOException;
 import java.util.GregorianCalendar;
 import java.util.Vector;
 import android.util.JsonReader;
 import android.util.Log;
 
 public class StaticReader {
 	
 	public static Vector<Rallye> readUsers(JsonReader reader) throws IOException{
 		Vector<Rallye> rallyes = new Vector<Rallye>();
 		reader.beginArray();
 		String header;
 		while(reader.hasNext())
 		{
 			reader.beginObject();
 			header =reader.nextName();
 			if(header.equals("User"))
 			{
 			try{
 				rallyes.add(readRallye(reader));}
 			catch(IOException e)
 			{
 				Log.d("DEBUG", "Crash dans readUser");
 			}
 			}
 			reader.endObject();
 		}
 		reader.endArray();
 		Log.d("NOM",rallyes.get(1).getNom());
 		return rallyes;
 		
 	}
 	
 
 	private static Rallye readRallye(JsonReader reader) throws IOException {
 		Log.d("DEBUG", "Rentre dans readRallye");
 		Rallye rallye = new Rallye();
 		String header;
 	    while (reader.hasNext()) {
 	    	header = reader.nextName();
 	       if (header.equals("nom")) {
 				Log.d("DEBUG", "Check");
 
 	         rallye.setNom(reader.nextString());
 	       } else if (header.equals("jourDepart")) {
 	    	   rallye.setDateDeb(readDate(reader));
 	       } else if (header.equals("joueArrivee")) {
 	    	   rallye.setDateFin(readDate(reader));
 	       } else if (header.equals("id_course")) {
 		    	   rallye.setId(reader.nextLong());
 	       } else {
 	         reader.skipValue();
 	       }
 	     }
 	     reader.endObject();
 	     
 	     return rallye;
 	}
 	
 	public static Vector<Rallye> readRallyes(JsonReader reader) throws IOException{
 		Vector<Rallye> rallyes = new Vector<Rallye>();
 		reader.beginObject();
 		if (reader.nextName().equals("course")){
 			String header;
 			reader.beginArray();
 			while(reader.hasNext())
 			{
 				reader.beginObject();
 				header=reader.nextName();
 				if(header.equals("nom"))
 				{
 				try{
 					rallyes.add(readRallye(reader));}
 				catch(IOException e)
 				{
 					Log.d("DEBUG", "Crash dans readRallye");
 				}
 				}
 			}
 			reader.endArray();
 			reader.endObject();
 			Log.d("rallye",rallyes.get(1).getNom());
 		}
 		
 		return rallyes;
 		
 	}
 	
 	private static GregorianCalendar readDate(JsonReader reader){
 		GregorianCalendar cal = null;;
 		String tmp = null;
 		try {
 			tmp = reader.nextString();
 		} catch (IOException e) {
 			Log.d("DEBUG","Crash dans readDate");
 			e.printStackTrace();
 		}
 		if(tmp != null)
 			cal = new GregorianCalendar(Integer.parseInt(tmp.substring(0, 4)),
 										Integer.parseInt(tmp.substring(5, 7)),
 										Integer.parseInt(tmp.substring(8)));
 		return cal;
 	}
 	
 	private static GregorianCalendar readTimestamp(JsonReader reader){
 		GregorianCalendar cal = null;;
 		
 		String tmp = null;
 		try {
 			tmp = reader.nextString();
 		} catch (IOException e) {
 			Log.d("DEBUG","Crash dans readTimestamp");
 			e.printStackTrace();
 		}
 		if(tmp != null)
 			cal = new GregorianCalendar(Integer.parseInt(tmp.substring(0, 4)),
 										Integer.parseInt(tmp.substring(5, 7)),
 										Integer.parseInt(tmp.substring(8,10)),
 										Integer.parseInt(tmp.substring(11,13)),
 										Integer.parseInt(tmp.substring(14,16)),
 										Integer.parseInt(tmp.substring(17,19)));
 		return cal;
 	}
 	
 }
