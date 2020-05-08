 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Hashtable;
 
 /**
  *
  * @author Herat
  */
 public class Operation {
    public static void main(String[] args) {
        Hashtable_QuadraticProbing ho = new Hashtable_QuadraticProbing(3);
        ho.put("abc", "sdfsf");
        ho.put("abc", "adsdadasd");
       ho.put("abcd", "wwww");
        ho.debugPrintWholeArray();
        ho.delete("abc");
        ho.debugPrintWholeArray();
    } 
 }
