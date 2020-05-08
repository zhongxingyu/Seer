 package mrz.lab1.processor;
 
 import mrz.lab1.binary.BinaryNumber;
 
 /**
  * @author Q-YAA
  */
 public class Summator implements ProcessorElement {
 
     private int number;
 
     public Summator(int number) {
         this.number = number;
     }
 
     @Override
     public void process(ProcessingItem processingItem) {
         BinaryNumber multiplicand = processingItem.getMultiplicand();
         BinaryNumber partialSum = processingItem.getPartialSum();
         BinaryNumber factor = processingItem.getFactor();
 
        if (factor.getDigits()[factor.getDigitCapacity() - number] == 1) {
             int digitsToSumLength = (partialSum.getDigitCapacity()) / 2 + 1;
             byte[] digitsToSum = new byte[digitsToSumLength];
 
             System.arraycopy(partialSum.getDigits(), 0, digitsToSum, 0, digitsToSumLength);
 
             BinaryNumber sumResult =
                 BinaryNumber.sum(new BinaryNumber(digitsToSumLength, digitsToSum), multiplicand, digitsToSumLength);
 
             System.arraycopy(sumResult.getDigits(), 0, partialSum.getDigits(), 0, digitsToSumLength);
         }
     }
 
     @Override
     public String getName() {
         return "Summator element";
     }
 
     @Override
     public int getNumber() {
         return number;
     }
 }
