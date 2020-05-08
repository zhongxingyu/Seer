 package numberConversionAlgoritme;
 
 public class SecondComplementBinaryConversion implements NumberConversionAlgoritmeInterface{
 
 	@Override
 	public Object getSpecificNumber(double number) {
 		double numberOfBits = 0;
 		if(number != 0)
 			numberOfBits = Math.log((int)Math.abs(number))/Math.log(2);
 		String bits = Integer.toBinaryString((int)Math.abs(number));
 		int numberOfBitsInString = bits.length();
 		numberOfBits++;
 		numberOfBits = Math.abs(numberOfBits);
 		numberOfBits = Math.ceil((int) (Math.ceil(numberOfBits / 4d) * 4));
 		
 		int bitsNeeded = (int) (numberOfBits-numberOfBitsInString);
 		
 		StringBuilder b = new StringBuilder();
 		for(int i = 0; i < bitsNeeded-1; i++){
 			b.append("0");
 		}
 		
 		if(number > 0){
 			b.insert(0, "0");
 			return b.toString() + bits;
 		}
 		number++;
 		b.insert(0, "1");
//		return b.toString() + " " + Integer.toBinaryString(~(int)Math.abs(number));
		return b.toString();
 	}
 
 	@Override
 	public double getDoubleNumber(Object number) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 }
