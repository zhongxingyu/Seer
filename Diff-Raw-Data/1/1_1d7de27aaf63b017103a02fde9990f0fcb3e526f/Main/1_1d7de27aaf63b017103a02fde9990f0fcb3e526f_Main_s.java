 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package original;
 
 /**
  *
  * @author dsb
  */
 public class Main {
 
     /**
      * @param args the command line arguments
      */
     static Container c;
 
     public static void main(String[] args) {
        Container.debug = (args != null);   // any command-line arguments, turn on debugging
 
         c = new Container("mylist");
         c.insert(new Node("don  ", 19, 53));
         c.insert(new Node("karen", 19, 60));
         c.insert(new Node("alex ", 19, 92));
         c.insert(new Node("hannah", 19, 94));
         c.insert(new Node("chief", 20, 11));
         c.insert(new Node("scarlet", 20, 07));
 
         c.print();
         deletesome();
         c.print();
         addsome();
         c.print();
     }
 
     static void deletesome() {
         Iterator i = new Iterator(c);
         boolean flip = false;
         while (i.hasNext()) {
             Node n = i.getNext();
             if (flip) {
                 i.delete();
             }
             flip = !flip;
         }
 
     }
 
     static void addsome() {
         c.insert(new Node("kelsey", 19, 88));
         c.insert(new Node("chili", 19, 94));
     }
 }
