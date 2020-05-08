 package Test;
 
 import java.util.*;
 import java.io.*;
 import java.text.*;
 
 
 public class ParseCSV {
 
     /**
      * @param args the command line arguments
      */
 
 
     public static void main(String[] args)throws Exception {
 
         
         ArrayList<String> headerList = new ArrayList();
        String filename = "c:/Users/NEEL/Desktop/E0.csv";
        Scanner sc=new Scanner(new File("c:/Users/NEEL/Desktop/E0.csv"));
         sc.useDelimiter(",");
         String line="",header=""; int numberOfRows=0;
 
         numberOfRows = numberOfRows(filename);
         System.out.println("number of rows "+numberOfRows);
 
         header = sc.nextLine();
 
         StringTokenizer head = new StringTokenizer(header);
 
         while(head.hasMoreTokens())
         headerList.add(head.nextToken(","));
 
         System.out.println("header size or number of columns: "+headerList.size());
 
         int i=0;
    //     while(i<headerList.size()){
    //         System.out.println(headerList.get(i));
    //        i++;
     //    }
 
 
         String [][]data = new String[numberOfRows][headerList.size()];
         String token="";
         StringTokenizer str;// = new StringTokenizer("");
 
 
        // while(sc.hasNext()){
    System.out.println("number of coulmns "+data[0].length);
 
         for(i=379;i>=0;i--){
           line=sc.nextLine();
           str = new StringTokenizer(line);
           for(int j=0;j<data[0].length;j++){
              if(str.hasMoreTokens())//if((token=str.nextToken(",")).equals(null)!=true)
                data[i][j]=str.nextToken();
             
                
           }
        
           
         }//for
           
 
        // }
   for(i=0;i<data.length;i++){
       for(int j=0;j<data[0].length;j++)
          System.out.print(data[i][j]+"");
   System.out.println();
   }
 
 
         
 
 
 
 
 
 /*
 
 
 
 // TODO code application logic here
     }
 */
     }//main
 
     public static int numberOfRows(String filename)throws Exception{
 
         Scanner sc = new Scanner(new File(filename));
         sc.nextLine();
         int count =0;
 
         while(sc.hasNext()){
             sc.nextLine();
             count++;
         }
         return count;
     }//numberOfRows
 
 }//class
