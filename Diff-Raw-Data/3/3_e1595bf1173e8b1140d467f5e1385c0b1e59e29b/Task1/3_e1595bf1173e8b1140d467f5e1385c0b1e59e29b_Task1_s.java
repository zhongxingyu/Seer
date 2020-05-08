 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Scanner;
 
 public class Task1 {
 
     public ArrayList<Integer> getArray(String line){
         ArrayList<Integer> arrayList = new ArrayList<Integer>();
         String[] firstStrings = line.split(" ");
         for (String str : firstStrings){
            arrayList.add(new Integer(str));
         }
 
         return arrayList;
     }
 
     public static void main(String[] args) {
         Scanner scanner = new Scanner(new InputStreamReader(System.in));
         Task1 task1 = new Task1();
 
         ArrayList<Integer> firstArr = task1.getArray(scanner.nextLine());
         ArrayList<Integer> secondArr = task1.getArray(scanner.nextLine());
         firstArr.retainAll(secondArr);
         Collections.sort(firstArr);
         for (Integer var : firstArr){
             System.out.print(var + " ");
         }
 
     }
 }
