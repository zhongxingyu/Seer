 package features.penguin;
 
 public class FizzBuzz implements features.FizzBuzz {
 
 	@Override
 	public String fizzBuzz(int arg0) {
 		
 		String result = "";
 		
 		if(arg0 % 3 == 0){
 			result = "Fizz";
 		}
 		
 		if(arg0 % 5 == 0){
 			result += "Buzz";
 		}
 		
		if(result.equals("")){
 			result = Integer.toString(arg0);
 		}
 		
 		return result;
 	
 	}
 
 }
