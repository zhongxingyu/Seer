 public class ALU {
 
 
   public String performOperation(String data1, String data2, int fifi){
 
     long temp = 0;
     long x;
     long y;
     String binaryValue;
     int counter;
 
     switch (fifi) {
     
     	case 100000:
     		temp = Long.parseLong(data1, 2) + Long.parseLong(data2, 2);
     		binaryValue = Long.toBinaryString(temp);
     		counter = 32 - binaryValue.length();
     		for ( int i = 0; i< counter; i++) {
     			binaryValue = '0' + binaryValue;
     		}
     		return binaryValue;
     		
 
     	case 100111:
     		temp = (Long.parseLong(data1, 2) | Long.parseLong(data2, 2));
     		String temp2 = Long.toBinaryString(temp);
     		binaryValue = "";
     		for (int i = 0; i < temp2.length(); i++) {
     			if (temp2.charAt(i) == '1') {
     				binaryValue += "0";
     			} else {
     				binaryValue += "1";
     			}
     		}
     		counter = 32 - binaryValue.length();
     		for ( int i = 0; i< counter; i++) {
     			binaryValue = '0' + binaryValue;
     		}
     		return binaryValue;
 
     	case 100101:
     		temp = (Long.parseLong(data1, 2) | Long.parseLong(data2, 2));
     		String orValue = Long.toBinaryString(temp);
     		counter = 32 - orValue.length();
     		for ( int i = 0; i< counter; i++) {
     			orValue = '0' + orValue;
     		}
     		return orValue;
 
     	case 100100:
     		temp = (Long.parseLong(data1, 2) & Long.parseLong(data2, 2));
     		String andValue = Long.toBinaryString(temp);
     		counter = 32 - andValue.length();
     		for ( int i = 0; i< counter; i++) {
     			andValue = '0' + andValue;
     		}
     		return andValue;
     		
     	case 100010:
     		temp = Long.parseLong(data1, 2) - Long.parseLong(data2, 2);
     		binaryValue = Long.toBinaryString(temp);
     		if (binaryValue.length() > 32) {
     			int counter2 = binaryValue.length() - 32;
         		binaryValue = binaryValue.substring(counter2, binaryValue.length());
     		}
     		counter = 32 - binaryValue.length();
     		for ( int i = 0; i< counter; i++) {
     			binaryValue = '0' + binaryValue;
     		}
     		return binaryValue;
 
     	case 000000:
     		temp = Long.parseLong(data1, 2) << Long.parseLong(data2, 2);
     		binaryValue = Long.toBinaryString(temp);
     		counter = 32 - binaryValue.length();
     		if (binaryValue.length() > 32) {
     			int counter2 = binaryValue.length() - 32;
         		binaryValue = binaryValue.substring(counter2, binaryValue.length());
     		}
     		for ( int i = 0; i< counter; i++) {
     			binaryValue = '0' + binaryValue;
     		}
     		return binaryValue;
 
     	case 000010:
     		System.out.println("1");
     		temp = Long.parseLong(data1, 2) >>> Long.parseLong(data2, 2);
     		
     		binaryValue = Long.toBinaryString(temp);
     		System.out.println(binaryValue);
     		counter = 32 - binaryValue.length();
     		for ( int i = 0; i< counter; i++) {
     			binaryValue = '0' + binaryValue;
     		}
     		return binaryValue;  
     		
     	case 10:
     		System.out.println("1");
     		temp = Long.parseLong(data1, 2) >>> Long.parseLong(data2, 2);
     		
     		binaryValue = Long.toBinaryString(temp);
     		System.out.println(binaryValue);
     		counter = 32 - binaryValue.length();
     		for ( int i = 0; i< counter; i++) {
     			binaryValue = '0' + binaryValue;
     		}
     		return binaryValue;  
 
     	case 101010:
     		if(Long.parseLong(data1,2) < Long.parseLong(data2,2)){
     			return "1";
     		}
     		return "0";
     }
     
     return "";
 
   }
 
 }
