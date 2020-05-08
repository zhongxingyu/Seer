 /*If all the numbers from 1 to 1000 (one
 thousand) inclusive were written out in
 words, how many letters would be used?*/
 
 import java.lang.Integer;
 
 public class problem017
 {
 	public static void main(String[] args)
 	{
 		int min_num = 1; //min: 1
 		int max_num = 1000; //max: 9999
 		int sum = 0;
 		String temp_word = "";
 
 		if (args.length>0)
			System.out.println(Integer.parseInt(args[0]));
 		else
 		{
 			for (int i=min_num; i<=max_num; i++)
 			{
 				temp_word = number_word(i);
 				temp_word = temp_word.replace(" ","");
 				temp_word = temp_word.replace("-","");
 
 				sum = sum + (temp_word.length());
 			}
 			System.out.println(sum);
 		}
 	}
 
 	public static String number_word(int num)
 	{
 		String num_string = Integer.toString(num);
 		String word_string = "";
 		int value = 0;
 
 		String[] less_than_twenty = {"one", "two", "three",
 			"four", "five", "six", "seven", "eight", "nine",
 			"ten", "eleven", "twelve", "thirteen", "fourteen",
 			"fifteen", "sixteen", "seventeen", "eighteen",
 			"nineteen"};
 
 		String[] tens = {"twenty", "thirty", "forty", "fifty",
 			"sixty", "seventy", "eighty", "ninety"};
 
 		if (num_string.length()==1)
 		{
 			value = Integer.parseInt(Character.toString(num_string.charAt(0)));
			word_string = word_string + less_than_twenty[value-1];
 		}
 
 		if (num_string.length()==4)
 		{
 			value = Integer.parseInt(Character.toString(num_string.charAt(0)));
			word_string = word_string + less_than_twenty[value-1] + " thousand ";
 			num_string = num_string.substring(1);
 		}
 
 		if (num_string.length()==3)
 		{
 			value = Integer.parseInt(Character.toString(num_string.charAt(0)));
 			if (value == 0)
 				if (num%1000!=0)
					word_string = word_string + "and ";
 				else
 					word_string = word_string;
 			else
 			{
 				if (Integer.parseInt(num_string)%100!=0)
 				{
					word_string = word_string + less_than_twenty[value-1] + " hundred and ";
 					num_string = num_string.substring(1);
 				}
 				else
 				{
					word_string = word_string + less_than_twenty[value-1] + " hundred";
 					num_string = num_string.substring(1);
 				}
 			}
 		}
 
 		if (num_string.length()==2)
 		{
 			value = Integer.parseInt(Character.toString(num_string.charAt(0)));
 
 			if (value>1)
 			{
 				if (Integer.parseInt(num_string)%10!=0)
 				{
					word_string = word_string + tens[value-2] + "-";
 					num_string = num_string.substring(1);
 					value = Integer.parseInt(Character.toString(num_string.charAt(0)));
 
 					if (value!=0)
						word_string = word_string + less_than_twenty[value-1];
 				}
 				else
 				{
					word_string = word_string + tens[value-2];
 				}
 			}
 			else
 			{
 				value = Integer.parseInt(num_string);
 
 				if (value != 0)
					word_string = word_string + less_than_twenty[value-1];
 			}
 		}
 
 		return word_string;
 	}
 }
