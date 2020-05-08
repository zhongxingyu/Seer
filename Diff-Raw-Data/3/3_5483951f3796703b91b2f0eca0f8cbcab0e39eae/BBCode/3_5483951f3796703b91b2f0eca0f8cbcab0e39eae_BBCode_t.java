 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package libs;
 
 
 public class BBCode {
     public static String parse(String text) {
         // italique
         text = text.replaceAll("\\[i\\](.+?)\\[/i\\]", "<em>$1</em>");
         // gras
         text = text.replaceAll("\\[b\\](.+?)\\[/b\\]", "<strong>$1</strong>");
         // souligné
         text = text.replaceAll("\\[u\\](.+?)\\[/u\\]", "<u>$1</u>");
 
        // sauts de ligne
        text = text.replaceAll("\n", "<br />");

         return text;
     }
 }
