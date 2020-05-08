 package mapthatset.g5;
 
 import java.util.ArrayList;
 import java.util.Random;
 import mapthatset.sim.*;
 
 /*
  * Mapper that easily lets you choose between strategies.
  */
 public class G5CustomMapper extends Mapper
 {
 	
 	int intMappingLength;
 	String strID = "G5CustomMapper";
 	
	int maxN = 500;
 	
 	// CHANGE THIS TO CHANGE MAPPING STRAT
	private final Mapping mappingStrat = Mapping.RANDOM;
 
 	
 	private enum Mapping {
 		RANDOM, PERM, BINARY, TRIPLE, HALF,MIX,OUTLAYER, MAX_N;
 	}
 	
 	private ArrayList< Integer > getNewMapping() {
 		Random rdmGenerator = new Random();
 		ArrayList< Integer > alNewMapping = new ArrayList< Integer >();
 		for ( int intIndex = 0; intIndex < intMappingLength; intIndex ++ )
 		{			
 			switch(mappingStrat) {
 				case RANDOM:
 					alNewMapping.add( rdmGenerator.nextInt( intMappingLength ) + 1 );
 					break;
 				case PERM:
 					// Generate numbers until it's not in list...this is very
 					// inefficient, but acceptable for our use case.
 					int i = 1;
 					while (alNewMapping.contains(i)) {
 					  i = rdmGenerator.nextInt( intMappingLength ) + 1;
 					}
 					alNewMapping.add(i);
 					break;
 				case BINARY:
 					alNewMapping.add( rdmGenerator.nextInt( 2 ) * intMappingLength/2  + 1 );
 					break;
 				case TRIPLE:
 					alNewMapping.add( (rdmGenerator.nextInt( intMappingLength ) + 1) % 3 + 1 );
 					break;
 				case MAX_N:
 					alNewMapping.add( (rdmGenerator.nextInt( intMappingLength ) + 1) % maxN + 1 );
 					break;
 				case HALF:
 					alNewMapping.add( (rdmGenerator.nextInt( intMappingLength ) + 1) % (intMappingLength / 2) + 1 );
 					break;
 				case MIX:
 					if(intIndex<intMappingLength/2){
 						alNewMapping.add( rdmGenerator.nextInt( 2 ) * intMappingLength/2  + 1 );
 					}else{
 						int j = 1;
 						while (alNewMapping.contains(j)) {
 						  j = rdmGenerator.nextInt( intMappingLength ) + 1;
 						}
 						alNewMapping.add(j);
 					}
 					break;
 				case OUTLAYER:
 					if(intIndex<intMappingLength-1){
 						alNewMapping.add( rdmGenerator.nextInt( 2 ) * intMappingLength/2  + 1 );
 					}else{
 						int j = 1;
 						while (alNewMapping.contains(j)) {
 						  j = rdmGenerator.nextInt( intMappingLength ) + 1;
 						}
 						alNewMapping.add(j);
 					}
 					break;
 			}
 		}
 		System.out.println( "The mapping is: " + alNewMapping );
 		return alNewMapping;
 	}
 
 	@Override
 	public void updateGuesserAction(GuesserAction gsaGA) 
 	{
 		// dumb mapper do nothing here
 	}
 
 	@Override
 	public ArrayList<Integer> startNewMapping(int intMappingLength) 
 	{
 		// TODO Auto-generated method stub
 		this.intMappingLength = intMappingLength;
 		return getNewMapping();
 	}
 
 	@Override
 	public String getID() 
 	{
 		// TODO Auto-generated method stub
 		return strID;
 	}
 }
