 public class guessVarify{
 
 
 static Boolean guessVarify(char[] guess, char[] pass)
 {
 	int guessLength = guess.length;
 	int passLength = pass.length;
	char[] a = guess;
	char [] b = pass;
 	int exactright=0;
 	int right=0;
 	String guess2 = new String (guess);
 	String pass2 = new String (pass);
 	System.out.println("guess2:" + guess2);
 	System.out.println("pass2:" + pass2);
 	
 
 
 	for (int i=0; i < passLength; i++){
 		
 		//see if i letter in pass is exactly right with i letter in guess
		if (b[i] == (a[i])){
 			exactright = exactright + 1;
			b[i] = '.';
			a[i] = ',';
 		}
 		
 	}
 	
 	//runs through the length of the password
 	for (int i=0; i < passLength; i++){
 		
 		//see if i letter in pass is found somewhere in guess
 		for (int x = 0; x < passLength; x++){
			if (b[i] == a[x]) {
 				right = right +1;
				b[i] = '.';
				a[x] = ',';
 				break;
 			}
 		}
 		
 	}
 	
 	
 	if (exactright == passLength){
 		return true;
 	}
 	System.out.printf("your code holds %d characters exactly in the right place.\n", exactright);
 	if (right!=0){
 		System.out.printf("your code holds %d characters right, but not in the correct place.\n", right);
 	}
 	else{
 		System.out.println("none of your other characters match to the password");
 	}
 
 	return false;
 	
 }
 }
