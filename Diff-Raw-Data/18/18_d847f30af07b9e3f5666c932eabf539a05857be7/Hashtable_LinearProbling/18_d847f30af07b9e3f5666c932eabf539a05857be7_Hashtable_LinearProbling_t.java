 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Hashtable;
 
 /**
  *
  * @author Herat
  */
 public class Hashtable_LinearProbling {
     String[][] array;
     
     int getHashValue(String key) {
         return Math.abs(key.hashCode()%array.length);
     }
 
     public Hashtable_LinearProbling() {
         array = new String[50][2];
     }
     
     public Hashtable_LinearProbling(int capacity) {
         array = new String[capacity][2];
         for(int i=0;i<array.length;i++)
             array[i][0] = "-1";
     }
     
     boolean delete(String key) {
         int index = getHashValue(key);
         if(array[index][0].equals("-1"))
            return false;
         else if(array[index][0].equals(key)) {
             array[index][0] = "-1";
         }
         else {
             int sindex = index;
             index++;
             index %= array.length;
             while(!array[index][0].equals(key)) {
                 if(index == sindex || array[index][0].equals("-1"))
                     return false;
                 index++;
                 index %= array.length;
             }
             if(array[index][0].equals(key))
                 array[index][0] = "-1";
             else
                 return false;
         }
         int sindex = index;
         int pindex = index;
         index ++;
         index %= array.length;
        while(pindex != sindex) {
             if(index != getHashValue(array[index][0])) {
                 array[pindex][0] = array[index][0];
                 array[pindex][1] = array[index][1];
                 array[index][0] = "-1";
                 pindex++;
                 pindex %= array.length;
             }
             index++;
             index %= array.length;
         }
         return true;
     }
     
     void debugPrintWholeArray() {
         for(int i=0;i<array.length;i++) {
             System.out.println(array[i][0] + "->" + array[i][1]);
         }
     }
     
     String get(String key) {
         int index = getHashValue(key);
         if(array[index][0].equals("-1"))
            return "-1";
         else if(array[index][0].equals(key))
             return array[index][1];
         else {
             int sindex = index;
             index++;
             index %= array.length;
             while(!array[index][0].equals(key)) {
                 if(index == sindex || array[index][0].equals("-1"))
                     return "-1";
                 index++;
                 index %= array.length;
             }
             return array[index][1];
         }
     }
     
     void put(String key, String value) {
         int index = getHashValue(key);
         if(array[index][0].equals("-1")) {
             array[index][0] = key;
             array[index][1] = value;
         } else {
             int sindex = index;
             index++;
             index %= array.length;
             boolean resize = false;
             while(!array[index][0].equals("-1")) {
                 if(index == sindex) {
                     resize = true;
                     break;
                 }
                 index += 1;
                 index %= array.length;
             }
             if(!resize) {
                 array[index][0] = key;
                 array[index][1] = value;
             } else {
                 System.out.println("Capacity exceeded");
             }
         }
     }
 }
