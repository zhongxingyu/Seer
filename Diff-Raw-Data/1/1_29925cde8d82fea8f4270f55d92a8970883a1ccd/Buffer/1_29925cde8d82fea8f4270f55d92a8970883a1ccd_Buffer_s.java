 package Checker;
 
 import java.util.Stack;
 import Tag.TagCollection;
 
 /**
  * This is the buffer, it reads through the text from one of the windows and
  * finds all of the tags and validates that it is valid html
  * @author Eric Majchrzak
  *
  */
 public class Buffer {
 	
 	//stack of tags
 	Stack<String> theStack = new Stack<String>();
 	
 	/**
 	 * This is the main method of the buffer that reads through the text and identifies all
 	 * of the tags, validates them and checks for the ending tag.
 	 * @return String
 	 */
 	public String validate(String text,TagCollection tags){
 		
 		char[] textArray = text.toCharArray();
 		boolean inTag = false;
 		String temp = "";
 		
 		//loop through the text
 		for(int i = 0; i<textArray.length;i++){
 			
 			//If character is the end of a tag
 			if(inTag == true && textArray[i] == '>'){
 				
 				//convert temp into an array to check if end tag
 				char[] tempArray = temp.toCharArray();
 				
 				//If it is an end tag
 				if(tempArray[0] == '/'){
 					//pop / of string
 					temp = temp.substring(1);
 					//pop off of stack and compare tags
 					if(!(theStack.pop().equals(temp))){
 						//give invalid tag name
 						return "</" + temp + "> may be spelled incorrectly or in the wrong place";
 					}
 					
 				//if not an end tag
 				} else {
 					//Check to see if the tag is valid
 					if(!tags.checkTag(temp)){
 						return "<" + temp + "> is an invalid tag";
 					}
 					//If valid push onto the stack
 					theStack.push(temp);
 				}
 				
 				//no longer in a tag
 				inTag = false;
 			}
 			
 			//If in a tag and not end append character to temp
 			if(inTag == true && textArray[i] != '>'){
 				temp = temp + textArray[i];
 			}
 			
 			//If the character is the start of a tag put us into a tag
 			if(inTag == false && textArray[i] == '<'){
 				inTag = true;
 			}
 			
 		}
 		
 		//Check to see if stack is empty after checking everything
 		if(theStack.empty()){
 			return "Valid HTML";
 		}
 		
 		//if not empty
 		return "<" + theStack.pop() + "> does not have an end tag.";
 	}
 
 }
