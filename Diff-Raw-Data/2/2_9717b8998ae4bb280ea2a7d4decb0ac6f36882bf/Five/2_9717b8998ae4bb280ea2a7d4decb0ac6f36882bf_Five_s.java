 package recruiting;
 
 /**
 * 2012-May-30
  */
 public class Five extends StrStrTest {
 
     @Override
     public int strstr(char[] pattern, char[] string) {
 	boolean matched = false;
 	for( int currIndex = 0; currIndex < string.length - pattern.length; currIndex++ ) {
 		matched = true;
 		for( int i = 0; i < pattern.length; i++ ) {
 			if( string[ currIndex + i ] != pattern[ i ] ) {
 				matched = false;
 				break;
 			}
 		}
 		if( matched == true ) {
 			return currIndex;
 		}
 	}
 	return -1;
     }
     
 }
