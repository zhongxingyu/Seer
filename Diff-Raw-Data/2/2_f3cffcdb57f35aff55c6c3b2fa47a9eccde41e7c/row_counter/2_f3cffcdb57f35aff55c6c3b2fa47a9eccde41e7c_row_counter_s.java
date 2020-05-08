 
 /**
  * Create different possibilities for rows
  * @author david
  */
 public class row_counter {
     /**
      * Create different possibilities for rows
      * Create permutations of rows based on length, with length being unknown
      * Uses row[] object to house data, which is all nested into array of rows
      * 
      *TODO: Switch row permutations over to array list?
      *
      */    
     
     /**
      * Used for creating rows
      */
     public row_counter(){}
     
     /**
      * Create possible matches of rows to check
      * 
      * @param length
      * @param base
      * @return 
      */
     public int[][] roper(int height,int rows){
         int amount = (int)Math.pow(rows, height);
         int output[][] = new int[amount][height];
         
         // starting point
         
         for(int before = 0; before < amount; before++){
             String temp_string = convert_base(before,rows);
             output[before] = convert_type(temp_string, height);
             //printit(output[before],length); // for debugging
         }
         
         return output;
     }
 
     /**
      * Roper -- Takes a number of base 10 as input, then converts the number to
      * the specified base and returns it as a string; recursive
      * 
      * @param number what to convert
      * @param base base to convert to
      * @return string containing conversion
      */
     public String convert_base(int number, int base){
        if (number > 1){
                 return convert_base(number/base,base) + "" +number%base;
         }
         else{
                 return "";
         }
     }
     
     /**
      * Roper -- Simply converts the strings that are output from convert
      * base function into array of integers
      * 
      * @param input
      * @return 
      */
     public int[] convert_type(String input,int len){
         int size = input.length();
         int output[] = new int[len];
         
         for(int index = 0;index < size; index++){
             String temp = input.charAt(index)+"";
             output[index+(len-size)] = Integer.parseInt(temp);
         }
         
         return output;
     }
     
     /**
      * Roper -- Function prints to std-out from an int[]
      * 
      * @param input
      * @param size 
      */
     public static void printit(int input[], int size){
 
         for (int index=0;index < size;index++)
                 System.out.print(input[index]);
         System.out.print("\n");
     }
     
     /**
      * Mocked up for JUnit Testing...
      * @param input
      * @param size 
      */
     public static void printit(double input[], int size){
 
         for (int index=0;index < size;index++)
                 System.out.print(input[index]);
         System.out.print("\n");
     }
     
     /**
      * returns number of permutations using powers, base^length, however this
      * will be more than the actual, as the actual will only have rows with
      * the correct length, however makes a good starting point
      * 
      * @param length
      * @param base
      * @return 
      */
     public int get_starting_size(int length, int base){
         return (int)Math.pow(base, length);
     }
 }
