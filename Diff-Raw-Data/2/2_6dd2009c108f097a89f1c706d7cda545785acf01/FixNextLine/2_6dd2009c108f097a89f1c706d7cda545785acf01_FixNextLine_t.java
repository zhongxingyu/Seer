 import java.util.Scanner;
 
 /**
  * 英語論文PDFからコピーしたテキストを
  * 適切な位置の改行に直す(ピリオドで改行)プログラム
  * (生成テキストは，Google翻訳で利用可能)
  *
  * @author (TAT)chaN
  * @since 2014/9/28
  */
 public class FixNextLine {
     public static void main(String[] args) {
 	Scanner s = new Scanner(System.in);
 	StringBuilder b = new StringBuilder();
 	while(s.hasNext())
	    b.append(s.nextLine().replace("\n", "")+" ");
 	System.out.println(b.toString().replace(". ", ".\n"));
     }
 }
