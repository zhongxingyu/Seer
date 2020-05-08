 import java.util.HashMap;
 import java.util.Map;
 
 
 public class IntegerToRoman 
 {	
 	public String intToRoman(int num)
 	{
 		Map<Integer, Character> mappings = new HashMap<Integer, Character>();
 		mappings.put(1, 'I');
 		mappings.put(5, 'V');
 		mappings.put(10, 'X');
 		mappings.put(50, 'L');
 		mappings.put(100, 'C');
 		mappings.put(500, 'D');
 		mappings.put(1000, 'M');
 		
 		
 		StringBuilder sb = new StringBuilder();
 		
 		int base = 1000;
 		while(base > 0)
 		{
 			int digit = num / base;
 			switch(digit)
 			{
 				case 1:
 					sb.append(mappings.get(base));
 					break;
 				case 2:
 					sb.append(mappings.get(base)).append(mappings.get(base));
 					break;					
 				case 3:
 					sb.append(mappings.get(base)).append(mappings.get(base)).append(mappings.get(base));
 					break;
 				case 4:
					sb.append(mappings.get(base)).append(base * 5);
 					break;
 				case 5:
 					sb.append(mappings.get(base * 5));
 					break;
 				case 6:
 					sb.append(mappings.get(base * 5)).append(mappings.get(base));
 					break;
 				case 7:
 					sb.append(mappings.get(base * 5)).append(mappings.get(base)).append(mappings.get(base));
 					break;
 				case 8:
 					sb.append(mappings.get(base * 5)).append(mappings.get(base)).append(mappings.get(base)).append(mappings.get(base));
 					break;
 				case 9:
 					sb.append(mappings.get(base)).append(mappings.get(base * 10));
 					break;
 			}
 			num %= base;
 			base /= 10;
 		}
 		
 		return sb.toString();
 	}
 }
