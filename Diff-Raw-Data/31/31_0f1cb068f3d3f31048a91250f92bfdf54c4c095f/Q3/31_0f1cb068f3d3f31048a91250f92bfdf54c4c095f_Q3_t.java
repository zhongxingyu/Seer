 package code.chap1;
 
 /**
  * 2つの文字列が与えられたとき、片方がもう片方の並び替えになっているかどうかを決定するメソッドを書いてください。
  * @author kimi
  *
  */
 public class Q3 {
     public static boolean isSorted(String org, String aft) {
        if (org.length() != aft.length()) return false;

        return sortString(org).equals(sortString(aft));
    }
 
    private static String sortString(String str) {
        char[] chars = str.toCharArray();
        java.util.Arrays.sort(chars);
        return String.valueOf(chars);
     }
 }
