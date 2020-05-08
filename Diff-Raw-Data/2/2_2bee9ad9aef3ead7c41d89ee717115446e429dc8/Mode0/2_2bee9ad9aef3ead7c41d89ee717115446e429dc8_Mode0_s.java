 package mode;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Random;
 
 
 public class Mode0 {
 
 	public ArrayList<Character> generateSet(){
 
 		ArrayList<Character> newSet = new ArrayList<Character>(Arrays.asList(
 				'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
 				'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
 				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
 				'-', '+', '@', '#', '$', '%', '^', '&', '*', '|', '<', '>', '?'
 				));
 
 		return newSet;
 	}
 
 	public ArrayList<String> randomChar(ArrayList<Character> availableChar){
 
 		ArrayList<String> result = new ArrayList<String>();
 		Random rand = new Random();
 		int randomNumber = rand.nextInt(availableChar.size()-1);
 		result.add(Integer.toString(randomNumber));
 		char randomChar = availableChar.get(randomNumber);
 		result.add(Character.toString(randomChar));
 
 		return result;
 	}
 
 	public boolean checkParentExclude(ArrayList<Character> excludeParent, char parent){
 		boolean result = false;
 
 		for(int i=0; i<excludeParent.size(); i++){
 			if(excludeParent.get(i)==parent){
 				result = true;
 				return result;
 			}
 		}
 
 		return result;
 	}
 
 	public char generateCharParent(ArrayList<Character> availableChar, ArrayList<Character> excludeParent){
 		char result = Character.UNASSIGNED;
 		boolean generateParent = true;
 
 		while(generateParent){
 
 			result = this.randomChar(availableChar).get(1).charAt(0);
 
 			switch(excludeParent.size()){
 			case 0:
 				generateParent = false;
 				break;
 			default:
 				boolean sameParent = this.checkParentExclude(excludeParent, result);
 				if(sameParent == false){
 					generateParent = false;
 				}
 				break;
 			}
 
 		}
 
 		return result;
 	}
 
 	public boolean checkSameChild(ArrayList<Character> childContainer, char child){
 		boolean result = false;
 
 		for(int m = 0; m<childContainer.size();m++){
 
 			if(childContainer.get(m)==child){
 				result = true;
 			}
 
 		}
 
 		return result;
 	}
 
 	public int checkChildIsParent(ArrayList<ArrayList<Object>> resultContainer, char child){
 		int result = 0;
 
 		for(int i = 0; i<resultContainer.size(); i++){
 			if(resultContainer.get(i).get(0).toString().charAt(0)==child){
 				result = i;
 			}
 		}
 
 		return result;
 	}
 
 	public boolean checkChildParentDependent(ArrayList<ArrayList<Object>> resultContainer, char child, char parent){
 		boolean result = false;
 
 		int indexParent = this.checkChildIsParent(resultContainer, child);
 
 		if(indexParent!=0){
 
 			ArrayList<Character> focusChild = (ArrayList<Character>) resultContainer.get(indexParent).get(1);
 
 			for(int l = 0; l<focusChild.size(); l++){
 
 				if(focusChild.get(l)==parent){
 					result = true;
 				}
 
 			}
 		}
 
 		return result;
 	}
 
 	public void removeChar(ArrayList<Character> availableChar, char targetChar){
 		for(int i=0; i<availableChar.size(); i++){
 
 			if(availableChar.get(i)==targetChar){
 				availableChar.remove(i);
 			}
 
 		}
 	}
 
 	public char generateCharChild(ArrayList<ArrayList<Object>> resultContainer, ArrayList<Character> childContainer, char parent){
 		char result = Character.UNASSIGNED;
 		ArrayList<Character> availableChar = this.generateSet();
 		this.removeChar(availableChar, parent);
 
 		boolean generateChild = true;
 
 		while(generateChild){
 
 			if(availableChar.size()>0){
 				result = this.randomChar(availableChar).get(1).charAt(0);
 				boolean sameChild = this.checkSameChild(childContainer, result);
 				boolean ChildParentDependent = this.checkChildParentDependent(resultContainer, result, parent);
 
 				if(sameChild || ChildParentDependent){
 					this.removeChar(availableChar, result);
 				}else{
 					generateChild = false;
 				}
 			}else{
 				return Character.UNASSIGNED;
 			}
 
 		}
 
 		return result;
 	}
 
 	public ArrayList<ArrayList<Object>> generateFPO(int lengthX, int lengthY){
 		ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();
 		ArrayList<Character> availableChar = this.generateSet();
 		ArrayList<Character> excludeParent = new ArrayList<Character>();
 
 
 		for(int i = 0; i<lengthY; i++){
 
 			ArrayList<Object> lineContainer = new ArrayList<Object>();
 			ArrayList<Character> childContainer = new ArrayList<Character>();
 
 			char parent = this.generateCharParent(availableChar, excludeParent);
 
 			lineContainer.add(parent);
 			
 			Random rand = new Random();
 			int randomNumber = rand.nextInt(lengthX);
 
 			for(int j = 0; j<randomNumber; j++){
 
 				char child = this.generateCharChild(result, childContainer, parent);
 				childContainer.add(child);
 
 			}
 
 			lineContainer.add(childContainer);
 
 
 			result.add(lineContainer);
 		}
 
 		return result;
 	}
 
 }
