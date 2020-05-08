 class LongestPalindrome
 {
 	public static void main(String[] args)
 	{
 		LongestPalindrome lp = new LongestPalindrome();
		
 		lp.parse("racecar");
 		lp.parse("racecar driver");
 		lp.parse("refeeeefer");
 		lp.parse("hello,palindromemordnilap find the longest palindrome");
 		lp.parse(".");
 		lp.parse("");
		lp.parse("a.");
 		lp.parse("racecar+suparacecarapus-platypus");
		lp.parse("aa");
		lp.parse("aaa");
		lp.parse("aaaa");
		lp.parse("baaaaa");
 	}
 	
 	public void parse(String line)
 	{
 		if(line.isEmpty())
 		{
 			System.out.println("");
 			return;
 		}
 		
 		char[] chars = line.toCharArray();
 		String longest = "";
 		
		for(int i = 0; i < chars.length-1; i++)
 		{
 			int offset = detectCluster(chars, i);
 			StringBuilder canindate = new StringBuilder("");
 			int min = 0;
 			int max = 0;
 			
 			if(offset > 0)
 				max = i + offset;
 			
 			for(int j = 0; j < chars.length/2 + 1; j++)
 			{
 				if(i-j < 0 || i+offset+j > chars.length-1)
 					break;
 
 				if(chars[i-j] == chars[i+offset+j])
 				{
 					min = i - j;
 					max = i + offset + j;
 				}
 				else
 					break;
 			}
 			
			if(max-min > longest.length())
			{
				for(int j = min; j <= max; j++)
					canindate.append(chars[j]);
 				longest = canindate.toString();
			}

			i += offset;
 		}
 		
 		if(longest.isEmpty())
 			System.out.println(chars[0]);
 		else
 			System.out.println(longest);
 	}
 	
 	private int detectCluster(char[] chars, int start)
 	{
 		int result = 0;
		for(int i = start+1; i < chars.length; i++)
		{	
 			if(chars[start] != chars[i])
 			{
 				result = i-start-1;
 				break;
 			}
			else if(i == chars.length-1) //last case
				result = i-start;
		}
 		return result;
 	}
 }
