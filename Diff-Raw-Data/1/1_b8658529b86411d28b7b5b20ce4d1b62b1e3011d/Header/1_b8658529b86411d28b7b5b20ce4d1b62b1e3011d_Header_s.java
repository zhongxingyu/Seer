 package construct;
 
 public class Header implements Construct
 {
     private int level;
     public Header(int level){
         System.out.println(level);
     }
     
     public String toHTML(){
         return "<h"+level+"> </h"+level+">\n";
     }
 
 }
