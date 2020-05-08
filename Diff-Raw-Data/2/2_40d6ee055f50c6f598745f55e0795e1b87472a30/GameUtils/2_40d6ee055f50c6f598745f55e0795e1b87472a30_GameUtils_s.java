 package utils;
 
 /**
  *
  * @author daniel
  */
 public class GameUtils
 {
     public static final int GAME_SIZE = 8;
     
     public static String buildBoard()
     {
         StringBuilder sb = new StringBuilder();
         sb.append("<table>");
         for (int i = 0; i < GAME_SIZE; i++)
         {
             sb.append("<tr>");
             for (int j = 0; j < GAME_SIZE; j++)
             {
                sb.append(String.format("<td><button id=\"%d,%d\" value=\"%d,%d\"></td>", i,j,i,j));
             }
             sb.append("</tr>").append(HttpUtils.CRLF);
         }
         sb.append("</table>");
         return sb.toString();
     }
 }
