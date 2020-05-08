 package dcll.jfri.projetConvertisseur;
 
 /**
  * Hello world!
  *
  */
 public class App 
 {
     public static void main( String[] args )
     {
    	String a = "c.ccccc";
    	a = a.substring(0, a.lastIndexOf('.'));
    	System.out.println(a);
         Menu menu = new Menu();
         menu.affiche();
     }
 }
