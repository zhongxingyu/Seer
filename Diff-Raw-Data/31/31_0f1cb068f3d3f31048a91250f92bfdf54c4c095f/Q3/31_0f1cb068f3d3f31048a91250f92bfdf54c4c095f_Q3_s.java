 package code.chap1;
 
 /**
  * 2つの文字列が与えられたとき、片方がもう片方の並び替えになっているかどうかを決定するメソッドを書いてください。
  * @author kimi
  *
  */
 public class Q3 {
     public static boolean isSorted(String org, String aft) {
        char[] chars = org.toCharArray();
        java.util.Arrays.sort(chars);
 
        for (int i = 0; i < org.length(); i++) {
            if (chars[i] != aft.charAt(i)) {
                return false;
            }
        }
        return true;
     }
 }
