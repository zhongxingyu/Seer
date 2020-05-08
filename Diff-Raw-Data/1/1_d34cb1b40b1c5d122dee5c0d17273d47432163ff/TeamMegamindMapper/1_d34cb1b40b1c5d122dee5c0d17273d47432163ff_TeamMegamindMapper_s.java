 package mapthatset.g3;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 import mapthatset.sim.*;
 
 public class TeamMegamindMapper extends Mapper {
 
 	int intMappingLength;
 	String strID = "MegamindMapper";
 	int round; // index to keep track of how many games played
 	
 	
 	private ArrayList< Integer > getNewMapping() {
 		
 		
 		ArrayList< Integer > alNewMapping = new ArrayList< Integer >();
 		Random rdmGenerator = new Random();
 		int randomChoice = rdmGenerator.nextInt(3); //creates a random choice of 0, 1 or 2
 		//int randomChoice = 2;
 		
 		switch(randomChoice) {
 		
 		case 0:
 			System.out.print("All unique map\n");
 			for ( int intIndex = 0; intIndex < intMappingLength; intIndex ++ ) {
 				alNewMapping.add( intIndex+1 );
				alNewMapping.add( rdmGenerator.nextInt( intMappingLength ) + 1 );
 			}
 			Collections.shuffle(alNewMapping);
 			System.out.println( "The mapping is: " + alNewMapping );
 			break;
 		case 1:
 			System.out.print("Majority Duplicates\n");
 			//choose a number that will become the duplicate
 			int duplicateNumber = rdmGenerator.nextInt(intMappingLength) + 1;
 			System.out.print("duplicate number is: " + duplicateNumber + "\n");
 			int amountOfDuplicates;
 			//need to make sure the amount of duplicates is more than half
 			if(intMappingLength == 2)
 				amountOfDuplicates = 2;
 			else {
 				amountOfDuplicates = rdmGenerator.nextInt((int) Math.ceil(intMappingLength)) + 1;
 			}
 			for ( int intIndex = 0; intIndex < intMappingLength; intIndex ++ ) {
 				if(amountOfDuplicates >= 0) { //add the amount of duplicates first
 					alNewMapping.add(duplicateNumber);
 					amountOfDuplicates--;
 					continue;
 				}
 				alNewMapping.add( rdmGenerator.nextInt( intMappingLength ) + 1 );
 			}
 			//randomize final map before sending it for security reasons
 			Collections.shuffle(alNewMapping);
 			System.out.println( "The mapping is: " + alNewMapping );
 			break;
 		case 2:
 			System.out.print("All keys map to two elements\n");
 			// choose the two numbers to map everything to
 			int firstNumber = rdmGenerator.nextInt(intMappingLength) + 1;
 			int secondNumber = rdmGenerator.nextInt(intMappingLength) + 1;
 			// pick random number between n/4 and 3n/4
 			int split = rdmGenerator.nextInt(intMappingLength/2) + intMappingLength/4;
 			// this number is the frequency of 'firstNumber' in the mapping
 			// firstNumber occurs split times, second number occurs n-split times
 			for ( int intIndex = 0; intIndex < split; intIndex ++ ) {
 				alNewMapping.add( firstNumber );
 			}
 			for ( int intIndex = split; intIndex < intMappingLength; intIndex ++ ) {
 				alNewMapping.add( secondNumber );
 			}
 			Collections.shuffle(alNewMapping);
 			System.out.println( "The mapping is: " + alNewMapping );
 			break;
 		}
 
 		return alNewMapping;
 	}
 
 	@Override
 	public void updateGuesserAction(GuesserAction gsaGA) {
 		// dumb mapper do nothing here
 	}
 
 	@Override
 	public ArrayList<Integer> startNewMapping(int intMappingLength) {
 		// TODO Auto-generated method stub
 		this.intMappingLength = intMappingLength;
 		return getNewMapping();
 	}
 
 	@Override
 	public String getID() {
 		// TODO Auto-generated method stub
 		return strID;
 	}
 }
