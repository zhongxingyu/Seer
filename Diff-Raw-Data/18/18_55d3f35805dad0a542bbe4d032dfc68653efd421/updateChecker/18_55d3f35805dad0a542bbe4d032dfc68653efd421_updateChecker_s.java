 package dbit.belegii;
 
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 public final class updateChecker {
 	
 	//returns tables that need to be removed from the buffer as String e.g. String("a,b,c")
 	//return null if no match was found
 	public String checkUpdate(String sql){
		Pattern p = Pattern.compile("\\(?(select|update|insert|create|delete)\\s+(\\*|\\w*\\,?)\\s+(from)\\s+([a-zA-Z_0-9\\s\\,]*)(\\s+(where)?\\s+(.*)\\)?)*");//(where)\\s+(.*|\\(.*\\))");
 		Matcher m = p.matcher(sql);
 		boolean b = m.matches();
 		
 		String tables = new String("");
 		while (b){
 		   	int count = m.groupCount();
 
		   	if(m.group(1).contains("update")) 
 		   		tables+=","+m.group(4);
 		    String last = m.group(count);
		    if(last.charAt(last.length()-1)==')') 
 		    	last=last.substring(0, last.length()-1);
 		    m = p.matcher(last);
 		    b = m.matches();
 		}
 		
 		if(tables.length()>0) 
 			return tables.substring(1);
 		else
 			return null;
 	}
 	
 	//returns true if table was found in sql query
 	//otherwise returns false
 	public boolean checkTable(String tables,String sql){
		Pattern p = Pattern.compile("\\(?(select|update|insert|create|delete)\\s+(\\*|\\w*\\,?)\\s+(from)\\s+([a-zA-Z_0-9\\s\\,]*)(\\s+(where)?\\s+(.*))*\\)?");//(where)\\s+(.*|\\(.*\\))");
 		Matcher m = p.matcher(sql);
 		boolean b = m.matches();
 		
 		String tablesArray[] = tables.split(",");
 		//System.out.println("b is: "+b);
 		while (b){
 		   	int count = m.groupCount();
 
 		   	for(String t:tablesArray){
 		   		//System.out.println("group 4 "+m.group(4));
 		   		if((m.group(4).contains(t))) 
 			   		return true;
 		   	}
 		   
 		   	
 		    String last = m.group(count);
 		    if(last==null) break;
 		    //System.out.println("last "+last);
 		    //if(last.charAt(last.length()-1)==')') 
 		    //	last=last.substring(0, last.length()-1);
 		    m = p.matcher(last);
 		    b = m.matches();
 		}
 		
 		return false;
 	}
 }
