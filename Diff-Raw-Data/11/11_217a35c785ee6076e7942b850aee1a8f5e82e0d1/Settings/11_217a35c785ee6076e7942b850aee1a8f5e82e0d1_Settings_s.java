 package tinf11bc.kbs.yavalath.util;
 
 public class Settings {
 
 	
 
 	
 	public static int[] mPlayerInformation;
 	
 	
 	public static int mDifficultyUTCAiOne; 
 	public static boolean mIsActivatedOne; 
 	
 	
 	public static int mDifficultyUTCAiTwo;
 	public static boolean mIsActivatedTwo; 
 	
 	
 	public static int mDifficultyUTCAiThree;
 	public static boolean mIsActivatedThree; 
 	
 	public Settings(){
 		mPlayerInformation = new int[]{1,3,0};
 		mDifficultyUTCAiOne = 1;
 		mDifficultyUTCAiTwo = 2;
 		mIsActivatedTwo = true;
 		
 		mDifficultyUTCAiThree = 3;
 		
 	}
 	
 	
 	public void setPlayerInformation(int[] playerInformation){
 		mPlayerInformation = playerInformation;		
 	}
 	
 	public int[] getPlayerInformation(){
 		return mPlayerInformation;
 	}
 
 
 	
 	
 	
 	
 	
 	public static int getDifficultyUTCAiOne() {
 		int result = 0;
 		
 		switch(mDifficultyUTCAiOne){
 			case 1:
				result = 1000;
 				break;
 			case 2:
				result = 5000;
 				break;
 			case 3:
 				result = 50000;
 				break;
 		}
 
 		return result;
 	}
 	
 	public static int getDifficultyUTCAiTwo() {
 		int result = 0;
 		
 		switch(mDifficultyUTCAiTwo){
 			case 1:
				result = 1000;
 				break;
 			case 2:
				result = 5000;
 				break;
 			case 3:
 				result = 50000;
 				break;
 		}
 
 		return result;
 	}
 	
 	public static int getDifficultyUTCAiThree() {
 		int result = 0;
 		
 		switch(mDifficultyUTCAiThree){
 			case 1:
 				result = 10000;
 				break;
 			case 2:
 				result = 20000;
 				break;
 			case 3:
 				result = 50000;
 				break;
 		}
 
 		return result;
 	}
 
 
 	public void setDifficultyUTCAiOne(int difficultyUTCAi, boolean isActivated) {
 		mDifficultyUTCAiOne = difficultyUTCAi;
 		mIsActivatedOne = isActivated;
 	}
 	
 	public void setDifficultyUTCAiTwo(int difficultyUTCAi, boolean isActivated) {
 		mDifficultyUTCAiTwo = difficultyUTCAi;
 		mIsActivatedTwo = isActivated;
 	}
 	
 	public void setDifficultyUTCAiThree(int difficultyUTCAi, boolean isActivated) {
 		mDifficultyUTCAiThree = difficultyUTCAi;
 		mIsActivatedThree = isActivated;
 	}
 
 
 	public static boolean ismIsActivatedOne() {
 		return mIsActivatedOne;
 	}
 
 
 	public static boolean ismIsActivatedTwo() {
 		return mIsActivatedTwo;
 	}
 
 
 	public static boolean ismIsActivatedThree() {
 		return mIsActivatedThree;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 }
