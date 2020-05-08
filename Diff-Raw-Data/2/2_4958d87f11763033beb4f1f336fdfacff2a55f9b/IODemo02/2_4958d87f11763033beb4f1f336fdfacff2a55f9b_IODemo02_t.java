 import java.io.*;
 public class IODemo02{
 	public static void main(String[] args){
 		int i = 0;
 		int j = 0;
 		InputDate input = new InputDate();
 		i = input.getInt("请输入第一个数：","输入的内容必须为数字，请重新输入");
 		j = input.getInt("请输入第二个数：","输入的内容必须为数字，请重新输入");
 		System.out.println(i + "+" + j + "=" + (i+j));
		input.getDate("请输入日期:","输入错误，格式为（yyyy-MM-dd）");
 	}
 }
