 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Scan 
 {
 
     public static class Entry 
     {
 	public long uid=0;
 	public String username="";
 	public String name="";
 	public String first_name="";
 	public String last_name="";
 	public String picture="";
 
 	public String toString() 
 	{
 	    //return username;
 	    String rv = "";
 	    rv += username + "\t" + uid + "\n";
 	    rv += "  " + first_name + " " + last_name + " (" + name + ")\n";
 	    rv += "  " + picture + "\n";
 	    
 	    return rv;
 	}
 	
 
     };
     
     
 
     public static void main(String [] args)
     {
 	BufferedReader reader = null;
 	ArrayList<Entry> list = new ArrayList<Entry>();
 	Entry cur = null;
 	
 	Pattern pattern = Pattern.compile("^([0-9]*)\\s+(\\w+)\\s+(.+)");
 	
 	try {
 	    reader = new BufferedReader(new FileReader(args[0]));
 	    
 	    String s;
 	    reader.readLine(); //first line is useless heading.
 	    
 	    while ((s=reader.readLine()) != null){
 		Matcher matcher = pattern.matcher(s);
 		if (matcher.matches()) {
 		    
 		    if (! matcher.group(1).equals("")){
 			cur = new Entry();
 			list.add(cur);
 		    }
 
 		    String key = matcher.group(2);
 		    String val = matcher.group(3);
 
 		    if(key.equals("uid"))
 			cur.uid = Long.parseLong(val);
 		    else if (key.equals("name"))
 			cur.name = val;
 		    else if (key.equals("first_name"))
 			cur.first_name = val;
 		    else if (key.equals("last_name"))
 			cur.last_name = val;
 		    else if (key.equals("username"))
 			cur.username = val;
 		    else if (key.equals("pic_big"))
 			cur.picture = val;
 		}
 		
 		
		for (Entry ent : list ) {
		    System.out.println(ent);
		}
		

 	    }
 	    
 	    if (reader != null)
 		reader.close();
 	}
 	catch (Exception e) {
 	    System.err.println( e.getMessage() );
 	}
 	
     }
 }
