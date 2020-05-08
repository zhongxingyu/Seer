 package com.java.hackermeter.recursion;
 
 import java.util.*;
 /**
  * A couple of friends are afraid of their nosy roommates looking at their LINE history, so they have the following discussion:
  *
  * X: I really don't want my messages to be read...let's start encrypting our messages. We'll replace each letter with a number. 'A' is 1, 'B' is 2, and so on...
  * Y: What. The. ****.
  * X: It'll be great!
  * Y: I guess you didn't study cryptography in college. But, just to let you know, that would let you decode words in different ways. For example, "ABBA" is the same as "LU".
  * X: Maybe there aren't that many ways to do it?
  * Y: Well, THAT we can find out. Where's your laptop?
  *
  * ---
  *
  * The first line of the input will be an integer N (1 <= N <= 1000).
  *
  * The following N lines each contain a string S comprised only of digits (no spaces or special characters - just digits).
  *
  * For each test case, print the number of different ways there are to decrypt S.
  */
 public class AmbiguousAlphabet {
     public static void run(Scanner scanner) {
         //Code here!
         String numStr = scanner.nextLine();
        //String numStr = "25114";
         int length = numStr.toCharArray().length;
         int[] s = new int[length];
 
         for(int i = 0; i < length; i++) {
             s[i] = Character.getNumericValue(numStr.charAt(i));
         }
 
         int x = perms(s, 0);
         System.out.println(x);
     }
 
     public static int perms(int[] s, int count) {
         //base case
         for(int i = 0; i < s.length; i++) {
            if(s[i] > 26 || s[i] < 1)   //what about if that 0 was actually 10/20 ? fix this !
                 return count;
         }
 
         count++;
 
         for(int j = 1; j < s.length; j++) {
             int[] s2 = Arrays.copyOf(s, s.length);
 
             if(s2[j-1] > 9)
                 s2[j-1] = s2[j-1]%10;
             s2[j] = (s[j-1]*10) + s[j];
 
             count = perms(Arrays.copyOfRange(s2, j, s2.length), count);
         }
 
         return count;
     }
 
     public static void main(String args[]) {
         Scanner scanner = new Scanner(System.in);
 
         int cases = Integer.parseInt(scanner.nextLine());
 
         for(int i = 0; i < cases; i++) {
             run(scanner);
         }
     }
 }
