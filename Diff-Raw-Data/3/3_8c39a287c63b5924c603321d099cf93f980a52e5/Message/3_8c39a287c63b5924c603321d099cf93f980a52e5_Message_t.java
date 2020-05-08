 package rky.portfolio.io;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Message
 {
     public static final Message ACK = new Message("OK");
 
     private String body;
 
     public Message(String body)
     {
         this.body = body;
     }
     
     public Message(String... body)
     {
         this.body = "";
         for (String b : body)
         {
             this.body += b + " ";
         }
         this.body = this.body.trim();
     }
     
     public static Map<Integer, Double> parseDistribution( Message m )
     {
     	Map<Integer, Double> map = new HashMap<Integer, Double>();
     	
     	if( m.body.charAt(0) != '[' || m.body.charAt(m.body.length()-1) != ']' )
     		throw new IllegalArgumentException("Messaged expected to be wrapped in '[' and ']': " + m.body );
     	
     	String[] pairs = m.body.substring(1, m.body.length()-1).split(", ");
    	if( pairs.length == 1 && pairs[0].length() == 0 )
    		return map;
    	
     	for( String p : pairs )
     	{
     		String[] pair = p.split(":");
     		if( pair.length != 2 )
     			throw new RuntimeException("coulnd't properly break pair (" + p + ") in message " + m.body);
     		
     		Integer id = Integer.parseInt(pair[0]);
     		Double value = Double.parseDouble(pair[1]);
     		
     		map.put( id, value );
     	}
     	
     	return map;
     }
 
     /**
      * Takes an array of String values and forms a message body
      * is in the format "[v1, v2, v3, v4 ... ]"
      */
     public static Message createVector(String[] s)
     {
         return new Message(formatArray(s));
     }
 
     public static Message createError(String errorMessage)
     {
         return new Message("ERROR \"" + errorMessage + "\"");
     }
     
     public static Message createGameOver(Double score)
     {
         return new Message("GAMEOVER " + score);
     }
 
     private static String formatArray(String[] a)
     {
         StringBuffer result = new StringBuffer();
         result.append("[");
         if (a.length > 0)
         {
             result.append(a[0]);
             for (int i = 1; i < a.length; i++)
             {
                 result.append(", ");
                 result.append(a[i]);
             }
         }
         result.append("]");
         
         return result.toString();
     }
     
     @Override
     public String toString()
     {
         return body;
     }
 
 }
