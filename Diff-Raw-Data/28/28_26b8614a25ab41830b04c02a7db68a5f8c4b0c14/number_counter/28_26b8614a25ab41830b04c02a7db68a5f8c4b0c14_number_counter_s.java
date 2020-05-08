 import java.util.ArrayList;
 
 /**
  * Creates ArrayList of Row Objects
  * 
  * TODO: might have some issues from array list conversion in here
  * TODO: Switch row permutations over to array list?
  * 
  * @author david
  */
 public class number_counter{
     /**
      * Create different possibilities for rows
      * Create permutations of rows based on length, with length being unknown
      * Uses row[] object to house data, which is all nested into array of rows
      *
      * @param len
      * @param base
      * @return row_array 
      */    
     
     double brick1;
     double brick2;
     
     /**
      * Used for creating rows
      */
     public number_counter(){}
     
     /**
      * Used for creating bricks
      * @param b1
      * @param b2 
      */
     public number_counter(double b1, double b2){
         brick1 = b1;
         brick2 = b2;
     }
     
     
     public ArrayList row_generator(int len, int base){
         int amount = (int)Math.pow(base, len);
         ArrayList row_array = new ArrayList();
 
         for(int before = 0; before < amount; before++){
             //Convert number:base10 to base2
             String temp_string = convert_base(before,base);
             //take output(String) and convert to double[]
             double temp_num[] = convert_amount_and_type(temp_string);
             //create new row object to hold double[]
             row temp_row = new row(temp_num,len,temp_num.length);
             //copy new object in array of objects if correct length
             if (temp_row.get_row_length() == len){
                 row_array.add(temp_row);
             }
             //printit(temp_num,temp_string.length()); // for debugging
         }
         return row_array;
     }
     
     /**
      * Create possible matches of rows to check
      * 
      * @param length
      * @param base
      * @return 
      */
     public int[][] roper(int length,int base){
         int amount = (int)Math.pow(base, length);
         int output[][] = new int[amount][length];
         
         for(int before = 0; before < amount;before++){
             String temp_string = convert_base(before,base);
            output[before] = convert_type(temp_string);
     //        printit(output[before],temp_string.length()); // for debugging
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
                 return number%base+"";
         }
     }
 
     /**
      * Row gen -- For the instance of rows, the bricks are not lengths of 1 and 0, so
      * this function will convert them to brick1 & brick2
      * 
      * @param input
      * @return 
      */
     public double[] convert_amount_and_type(String input){
         int size = input.length();
         double output[] = new double[size];
 
         for(int index = 0; index < size; index++){
                 double temp = (double)input.charAt(index);
                 if(temp == '0')
                         temp = brick1;
                 else if (temp == '1')
                         temp = brick2;
                 else
                         temp = 0;
                 output[index] = temp;
         }
 
         return output;
     }
     
     /**
      * Roper -- Simply converts the strings that are output from convert
      * base function into array of integers
      * 
      * @param input
      * @return 
      */
    public int[] convert_type(String input){
         int size = input.length();
        int output[] = new int[size];
         
         for(int index = 0;index < size; index++){
             String temp = input.charAt(index)+"";
            output[index] = Integer.parseInt(temp);
         }
         
         return output;
     }
 
     /**
      * Row Gen -- Function prints to std out from a double[]
      * 
      * @param input
      * @param size 
      */
     public static void printit(ArrayList input, int size){
 
         for (int index=0;index < size;index++)
                 System.out.print(input.get(index));
         System.out.print("\n");
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
     
     public void printit(double input[], int size){
 
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
