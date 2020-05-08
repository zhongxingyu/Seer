 public class GenSeq
 {
   final String aSequence;
   final char[] sSeq;
   final char   aMaxValue;
   final char   aMinValue;
 
   public GenSeq(String aString)
   {
     aSequence  = aString;
     sSeq       = aString.toCharArray();
     aMaxValue  = sSeq[sSeq.length-1];
     aMinValue  = sSeq[0];
   }
 
   public static void main(String[] argv)
   {
    GenSeq.testCase("01", "");
    GenSeq.testCase("01", "1");
    GenSeq.testCase("01", "10");
     GenSeq.testCase("01", "110");
     GenSeq.testCase("01", "111");
    GenSeq.testCase("01", "1000");
    GenSeq.testCase("01", "1001");
    GenSeq.testCase("01", "1010");
    GenSeq.testCase("01", "1011");
   }
 
   public String getNextInSeq(String aInput)
   {
     char[] sWorking = aInput.toCharArray();
     char[] sReturn  = new char[sWorking.length];
    if(aInput.equals(""))
      return (Character.toString(aMinValue));
     boolean isToBeBumpedUp = true; //this digit needs incrementing
     for (int i = sWorking.length-1; i >= 0; i--)
     {
       if (isToBeBumpedUp)
       {
         //if at max value, reset to min value but leave flag to bump up next digit
         //else set to next value & set isToBeBumpedUp false
         if(sWorking[i] == aMaxValue)
         {
           sReturn[i] = aMinValue; 
         }
         else //set to the next value & set isToBeBumpedUp false
         {
           sReturn[i] = getNextDigit(sWorking[i],sSeq);
           isToBeBumpedUp = false;
         }
       }
       else //if not ToBeBumpedUp
       {
         //just copy it
         sReturn[i] = sWorking[i];
       }
     }//end-for
     //if at the end of the for loop we still have isToBeBumpedUp==true
     //then we need to slap on another MSD and set it to next value up from Min value
     if (isToBeBumpedUp)
     {
        char[] sRetVal = new char[sReturn.length+1];
        sRetVal[0] = getNextDigit(aMinValue,sSeq); //concat the char at the front
        for (int i = 0; i < sReturn.length; i++)  //copy the rest
        {
          sRetVal[i+1] = sReturn[i];
        }
        //convert to String
       return (new String(sRetVal));
      }
     return (new String(sReturn));   
   }
 
   public char getNextDigit(char aChar, char[] sChar) 
   {
     for (int i = 0, j=sChar.length; i < sChar.length; i++,j--)
     {
       if (aChar == sChar[i])
       {
 //        System.out.println("returning next in sequence of: " + sChar[j-1]);
         return sChar[j-1];
       }
     }
     return (char) -1;  //should never hit this, only if there is no match
   }
 
   static public void testCase(String aSeq,   //The sequence to use
                               String aInput) //The value to increment from
   {
     GenSeq aTest = new GenSeq(aSeq);
 
     System.out.println("Using sequence:  " + aSeq);
     System.out.println("Starting value:  " + aInput);
     System.out.println("got back =====>  " + aTest.getNextInSeq(aInput) ); 
     System.out.println("-------------------------------------------------");
   }
 }
