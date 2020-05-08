 package FMIndex;
 
 
 public class WaveletNode {
 
 	private WaveletNode left = null;
 	private WaveletNode right = null;
 	private Character pivot;
 	private BitLookup bitContent;
 	
 	public WaveletNode(StringWrapper string){
		if(string.length() <= 1){
			return;
		}
 		ConcreteAlphabet alphabet = new ConcreteAlphabet(string);
		int half = alphabet.size() / 2;
 		int totalOccurencesSoFar = 0;
 		Character[] allCharacters = alphabet.getAllCharacters();
 		
 		int pivotIndex;
 		for(pivotIndex = 0; pivotIndex < half; pivotIndex++){
 			totalOccurencesSoFar += alphabet.getOccurancesForCharacter(allCharacters[pivotIndex]);
 		}
 
 		if( (string.length() - totalOccurencesSoFar) > 
 			(string.length() + alphabet.getOccurancesForCharacter(allCharacters[pivotIndex + 1]) - totalOccurencesSoFar) ){
 				pivotIndex ++;
 			}
 		this.pivot = allCharacters[pivotIndex];
 		bitContent = new RRR(pivot, string);
 		
 		StringWrapper zeros = new StringWrapper();
 		StringWrapper ones = new StringWrapper();
 		StringBuilder buildZeros = new StringBuilder();
 		StringBuilder buildOnes = new StringBuilder();
 		
 		for(int i = 0; i < string.length(); i++){
			if(string.charAt(i) < pivotIndex)
 				buildZeros.append(string.charAt(i));
 			else
 				buildOnes.append(string.charAt(i));
 		}
 		
 		zeros.string = buildZeros.toString();
		ones.string = buildZeros.toString();
 		
 		left = new WaveletNode(zeros);
 		right = new WaveletNode(ones);
 	}
 	
 }
