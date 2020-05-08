 /*******************************************************************************
  * Copyright (c) 2013 Sam Bell.
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License,
  * or  any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
  * the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     Sam Bell - initial API and implementation
  ******************************************************************************/
 /**
  * 
  */
 package podsalinan;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
import com.mimpidev.dev.debug.Log;

 /**
  * @author bugman
  *
  */
 public class CLInput {
 	private BufferedReader stdInReader;
	private Log debugLog;
 	
 	public CLInput(){
 		stdInReader = new BufferedReader(new InputStreamReader(System.in));
 	}
 	
	public CLInput(Log debugFile) {
		debugLog = debugFile;
	}

 	public char getSingleCharInput() {
 		char input=' ';
 		
 		try {
 			input = (char) stdInReader.read();
 		} catch (IOException e){
			if (debugLog.showDebug())
				debugLog.printStackTrace(e.getStackTrace());
 		}
 		return input;
 	}
 
 	public String getStringInput(){
 		String input="";
 		try {
 			input = stdInReader.readLine();
 		} catch (IOException e) {
			if (debugLog.showDebug())
				debugLog.printStackTrace(e.getStackTrace());
 		}
 
 		return input;
 	}
 
 	/**
 	 * @param minNumber
 	 * @param maxNumber
 	 * @return
 	 */
 	public String getValidNumber (long minNumber, long maxNumber){
 		Boolean isValid=true;
 		String inputValue = getStringInput();
 		if ((inputValue.length()>0)&&(inputValue!=null)){
 			try {
 				// Tests to see if the string input is a number
 				Integer.parseInt(inputValue);
 			} catch (NumberFormatException e){
 				isValid=false;
 			}
 			if ((isValid)&&
 				(Integer.parseInt(inputValue)>=minNumber)&&
 				(Integer.parseInt(inputValue)<=maxNumber))
 				return inputValue;
 		}
 		System.err.println ("Error: User Input Invalid. Valid values between "+minNumber
 				+"-"+maxNumber);
 		return null;
 	}
 
 	public boolean confirmRemoval(){
 		System.out.println("Are you sure you want to delete this (Y/N)?");
 		String deletePrompt = getStringInput();
 		if ((deletePrompt.equalsIgnoreCase("Y"))||
 			(deletePrompt.equalsIgnoreCase("Yes"))){
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public boolean confirmCreation(){
 		System.out.println("Would you like to create it (Y/N)?");
 		String deletePrompt = getStringInput();
 		if ((deletePrompt.equalsIgnoreCase("Y"))||
 			(deletePrompt.equalsIgnoreCase("Yes"))){
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public String getValidLetter(char lowestLetter, char highestLetter) {
 		char inputValue = getSingleCharInput();
 		int lowestInt, highestInt, inputInt;
 		
 		
 		lowestInt = (int)(Character.toUpperCase(lowestLetter)-64);
 		highestInt = (int)(Character.toUpperCase(highestLetter)-64);
 		inputInt = (int)(Character.toUpperCase(inputValue)-64);
 		
 		if ((inputInt>=lowestInt)&&
 			(inputInt<=highestInt))
 			return ""+inputValue;
 		System.err.println ("Error: User Input Invalid. Valid values between "+lowestLetter
 				+"-"+highestLetter);
 		return null;
 	}
 }
