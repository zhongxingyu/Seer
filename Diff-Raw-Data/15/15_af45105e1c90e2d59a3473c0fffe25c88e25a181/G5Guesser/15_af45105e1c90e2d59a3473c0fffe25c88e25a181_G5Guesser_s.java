 package mapthatset.g5;
 
 import java.util.ArrayList;
 
 import mapthatset.sim.Guesser;
 import mapthatset.sim.GuesserAction;
 
 public class G5Guesser extends Guesser {
 
 	private int mappingLength;
 	private int intLastQueryIndex = 0;
	private ArrayList<Integer> guess = new ArrayList< Integer >();
 		
	public void startNewMapping( int intMappingLength ) {
 		this.mappingLength = intMappingLength;
 		intLastQueryIndex = 0;
		guess = new ArrayList< Integer >();
 	}
 	
 	@Override
 	public GuesserAction nextAction() {
		intLastQueryIndex ++;
 		if ( intLastQueryIndex > mappingLength ) {
 			return new GuesserAction("g", guess);
 		} else {
 			ArrayList<Integer> queryContent = new ArrayList<Integer>();
 			queryContent.add(intLastQueryIndex);
 			return new GuesserAction("q", queryContent);
 		}
 	}
 	
 	@Override
	public void setResult( ArrayList< Integer > alResult )
	{
 		guess.add(alResult.get(0));
 	}
 	
 	@Override
 	public String getID() {
 		return "G5Guesser";
 	}
 
 }
