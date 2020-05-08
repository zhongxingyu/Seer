 import java.util.Random;
 
public class Password 
 {
 	private int passLength;	
 	private int numMaxDigits;
 	private int numMinDigits;	
 	private int numMaxUpperCase;
 	private int numMinUpperCase;
 	private int numMinSpecial;
 	private int numMaxSpecial;
 	private String allowedSpecialChracters;
 	private char[] pwArray;		
 	private int currentDigits;
 	private int currentUppercase;
 	private int currentSpecial;
 	
 
 	/**
 		default constructor. sets the minumum of each element to 1 and the maximum to 100
 	*/
 	public Password()
 	{
 		allowedSpecialChracters = "`~@#%^&*()-_=+[]{}\\|;:' ,.<>/?";		
 		
 		numMaxDigits = 100;
 		numMinDigits = 1;	
 		
 		numMaxUpperCase = 100;
 		numMinUpperCase = 1;
 		
 		numMinSpecial = 100;
 		numMaxSpecial = 1;		
 	}		
 
	
 	private void initArrayList(int n)
 	{
 		pwArray = new char[n];
 		for (int i = 0; i < n; i++)
 		{
 			pwArray[i] = ' ';
 		}
 	}	
 	private int findSpotInString()
 	{
 		int a = getRandomNumber(0, passLength);					
 		for (int j = a; j <  passLength+a; j++)
 		{
 			if (j == passLength)
 				j = 0;
 			if(pwArray[j]  == ' ')
 			{
 				return j;
 			}					
 		}
 		System.out.println("NEVER");
 		return -1;
 		
 	}
 	/**
 		generates the password
 		@param length the lenght of the desired password
 	*/
 		
 	public String generate(int length)
 	{
 		initArrayList(length);
 		passLength = length;
 		int count = 0;
 		for (int i = 0; i < numMinDigits; i++ )
 		{
 			
 			int a = getRandomNumber(48, 57);	
 			int index = findSpotInString();
 			pwArray[index] = (char)a;
 			currentDigits++;
 		}			
 		
 		for (int i = 0; i < numMinUpperCase; i++ )
 		{
 			int a = getRandomNumber(65, 90);	
 			int index = findSpotInString();
 			pwArray[index] = (char)a;
 			currentUppercase++;
 		}	
 		
 		for (int i = 0; i < numMinSpecial; i++ )
 		{
 			int a = getRandomNumber(0, allowedSpecialChracters.length()-1);	
 			int index = findSpotInString();
 			pwArray[index] = (allowedSpecialChracters.charAt(a));		
 			currentSpecial++;
 		}	
 		
 		count+= currentDigits;
 		count+= currentUppercase;
 		count+= currentSpecial;		
 		
 		while (count < passLength)
 		{
 			int a = getRandomNumber(33,126);	
 			int index = findSpotInString();					
 				
 			if (isInBetween(a,48,57)) // if its a digit
 			{
 				if (currentDigits < numMaxDigits)
 				{
 					currentDigits++;						
 				}
 				else
 					continue; 
 			}
 			else if (isInBetween(a,65,90)) // if its a UPPERCASE
 			{
 				if (currentUppercase < numMaxUpperCase)
 				{
 					currentUppercase++;					
 				}
 				else 
 					continue;
 			}
 			else if (isInBetween(a,33,47) || isInBetween(a,58,64) || isInBetween(a,91,96) || isInBetween(a,123,126) ) // if it's a special character
 			{
 				if (currentSpecial < numMaxSpecial)
 				{
 					if (allowedSpecialChracters.contains(((char)a)+"") == true)
 					{
 						currentSpecial++;	
 					}
 					else
 						continue;								
 				}
 				else 
 					continue;
 			}	
 			// its a lower case letter for which there is no limit
 			count++;			
 			pwArray[index] = (char)a;					
 		}			
 		return toString();
 	}	
 	private boolean isInBetween(int a, int min, int max)
 	{
 		if (a >= min && a <= max)
 			return true;
 		return false;
 	}
 	
 	private int getRandomNumber(int start, int end)
 	{
 		Random r = new Random();
 		int num = r.nextInt(end-start+1)+start;	
 		return num;
 	}	
 	/**
 		returns the genererated password
 	*/
 	public String toString()
 	{
 		String s = "";
 		for (char c : pwArray)
 		{
 			s+=c + "";
 		}
 		return s;
 	}
 	
 	/**
 		sets the minimum number of digits the password can have
 		@param num minimum number of digits
 	*/
 	public void setMinDigits(int num) {numMinDigits = num;}	
 	/**
 		sets the maximum number of digits the password can have
 		@param num maximum number of digits
 	*/
 	public void setMaxDigits(int num) {numMaxDigits = num;}	
 	/**
 		sets the maximum number of UpperCase letters the password can have
 		@param num the maximum number of upper case letters
 	*/
 	public void setMaxUppercase(int num) {numMaxUpperCase = num;}	
 	/**
 		sets the minimum number of UpperCase letters the password can have
 		@param num the minimum number of upper case letters
 	*/
 	public void setMinUpperCase(int num) {numMinUpperCase = num;}	
 
 
 	/**
 		sets the minimum number of special characters the password can have
 		@param num the minumum number of special characters
 	*/	
 	public void setMinSpecial(int num) {numMinSpecial = num;}	
 	/**
 		sets the maximum number of special characters the password can have
 		@param num the maximum number of special characters
 	*/
 	public void setMaxSpecial(int num) {numMaxSpecial = num;}	
 	/**
 		sets the allowed special characters that can be used in a password
 		@param s the string containing characters that can be used as special characters
 	*/
 	public void setAllowedSpecialCharacters(String s) {allowedSpecialChracters = s;}	
 	
 	public static void main(String[] args)
 	{
 		Password pass = new Password();
 		pass.setMinDigits(2);
 		pass.setMaxDigits(2); 
 		pass.setMaxUppercase(0);
 		pass.setMinUpperCase(0);
 		pass.setMinSpecial(0);
 		pass.setMaxSpecial(0); 		
 		pass.setAllowedSpecialCharacters("!"); 			
 		
 		System.out.println(pass.generate(20));			
 	}
 }
