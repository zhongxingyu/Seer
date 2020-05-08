 /*
  * Filename: SuffixCalc.java
  * Description: This file is to calculate the value of an suffix
  *              expression, which has been transferred from infix
  *              one by MidToSuffix.
  */
 
 package core.grant;
 
 public class SuffixCalc {
     
 	//mid is to save the suffix expression.(Well, I chose a BAD 
 	//variable name.:(
 	
 	private String mid;
     
 	@SuppressWarnings("unused")
     
 	//1 is integer mode;2 is double mode.
     private int mode;                             
     
 	//nums[] is also a Stack to store the values. Finally, the answer
 	//will be stored at nums[0], if there is no syntax error.
 	
 	private double[] nums;
     public static final int numsMax=100;
    
     private int numsCount;
     
     /*WA and isNaN are the flag to determine the syntax error condition 
      * and the "divide by 0" condition.
      */
     private boolean WA=false;
     private boolean isNaN=false;
     
     //To define the operators.
     private static final char[] opSet=new char[]{'+','-','*','/'};
     private static final char[] numSet=new char[]{'0','1','2','3','4','5','6','7','8','9','.'};
     
     // To tell whether a character is an operator.
     private boolean isOp(char x)
     {
     	for (int i=0;i<opSet.length;i++)
     		if (x==opSet[i])
     			return true;
     	return false;
     }
     
     private boolean isDigit(char x)
     {
     	for (int i=0;i<numSet.length;i++)
     	  if (x==numSet[i])
     		  return true;
     	return false;
     }
     
     //The main method to calculate a suffix expression.
      
     private void calc()
     {
     	numsCount=0;
     	
     	//tmpInt is temporary to save the integer part of the number.
     	//tmpFloat is temporary to save the fraction part of the number.
     	
     	double tmpInt=0,tmpFloat=0;
     	int tmpFloatCount=0;
     	
     	//isInt is a flag to determine where a new digit
     	//should be place. true means before the dot, false
     	//means place it after the dot.
     	
     	boolean isInt=true;
     	
     	//negativeFlag is to determine the present number is
     	//less than 0.
     	
     	boolean negativeFlag=false;
     	
     	for (int i=0;i<mid.length();i++)
     	{
     		if (mid.charAt(i)>='0' && mid.charAt(i)<='9')
            {
         	  if (isInt)
         		  //no dot at present, add it to tmpInt.
         		  
         		  tmpInt=tmpInt*10+mid.charAt(i)-'0';
         	  else
         	  {
         		  //after dot, should be placed at the fraction part.
         		  
         		  tmpFloatCount++;
         		  double bit=mid.charAt(i)-'0';
         		  for (int j=0;j<tmpFloatCount;j++)
         			  bit/=10;
         		  tmpFloat+=bit;
         	  }
            }
     	   
     	   if (mid.charAt(i)=='.')
     	   {
     		   //We meet the dot, so change the flag and set the 
     		   //variable which saves the fraction to 0.
    		   
     		   tmpFloatCount=0;
     		   tmpFloat=0;
     		   isInt=false;
     	   }
     	   
     	   if (mid.charAt(i)==' ')
     	   {
     		   //space means the end of the number.
     		   //We add the number to stack, be cautious about  
     		   //the negative and the positive.
     		   
     		   if (!negativeFlag)
     			   addNumberToStack(tmpInt+tmpFloat);
     		   else
     			   addNumberToStack(-(tmpInt+tmpFloat));
     		   
     		   tmpInt=0;tmpFloat=0;
     		   tmpFloatCount=0;
     		   isInt=true;
     		   negativeFlag=false;
     	   }
     	   
   
     	   if (isOp(mid.charAt(i)))
     	   {
     		   if (mid.charAt(i)=='-' && (i==0 || (i!=mid.length()-1 && mid.charAt(i-1)==' ' && isDigit(mid.charAt(i+1)))))
     				   negativeFlag=true;
     		   else
     		   refreshStackbyOperators(mid.charAt(i));
     		   
     	   }
     	}
     }
     
     //To add numbers to stack.
     private void addNumberToStack(double x)
     {
     	nums[numsCount++]=x;
     }
     
     //to get the top 2 elements of the stack and do calculate,
     //then put the answer back to the stack.
     private void refreshStackbyOperators(char x)
     {
     	double num1=nums[numsCount-1];
     	double num2=nums[numsCount-2];
     	numsCount-=2;
     	double t=0;
     	
     	switch (x)
     	{
     	case '+':
     		t=num2+num1;
     		break;
     	case '-':
     		t=num2-num1;
     		break;
     	case '*':
     		t=num2*num1;
     		break;
     	case '/':
     	{
     		if (num1==0) isNaN=true;   //Divide by 0!
     		else
     		  t=num2/num1;
     		break;
     	}
     	}
     	nums[numsCount++]=t;
         
     }
     
     //The constructor, mode is to define the float mode or
     //integer mode.
 	public SuffixCalc(String mid,int mode)
 	{
 		//System.out.println("mid is:"+mid);
 		nums=new double[numsMax];
 		this.mid=mid;
 		this.mode=mode;
 		if (!mid.equals("Syntax Error"))
 		try
 		{
 			calc();
 		}
 	    catch (Exception e)
 	    {
 	    	WA=true;
 	    }
 		else 
 		  WA=true;
 		  
 	}
 	
 	//return the final value by string.
 	
 	public String getAns()
 	{
 		if (WA) 
		{
			System.out.println("Error in midto suffix");
			return mid;
		}
 		
 		if (isNaN) return "NaN";
 				
 		else if (numsCount!=1)
		{
			System.out.println("Error in calc");
			for (int i=0;i<numsCount;i++)
				System.out.println("Stack "+i+" :"+nums[i]);
 			return "Syntax Error";
		}
 		
 		else
 			return String.valueOf((double)Math.round(nums[0]*1000000)/1000000);
 	}
 }
