 package model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 
 public class Settings {
 	
 	private HashMap<Tape, Integer> TapeIncludes;
 	private HashMap<Integer, Double> Biases;
 	private ArrayList<Landmark> Landmarks;
 	
 	public Settings()
 	{
 		TapeIncludes = new HashMap<>();
 		Biases = new HashMap<>();
 		Landmarks = new ArrayList<Landmark>();
 	}
 
 	
 	// TAPE INCLUDES
 	
 	/**
 	 * @return the tapeIncludes
 	 */
 	@SuppressWarnings("unchecked")
 	public HashMap<Tape, Integer> getTapeIncludes() {
 		return (HashMap<Tape, Integer>) TapeIncludes.clone();
 	}
 	
	public Integer addTapeIncludes(Tape tape, int includesBitmap)
 	{
 		return TapeIncludes.put(tape, includesBitmap);
 	}
 	
	public void addTapeIncludes(HashMap<Tape, Integer> tapeIncludes)
 	{
 		TapeIncludes.putAll(tapeIncludes);
 	}
 	
	public Integer removeTapeIncludes(Tape tape)
 	{
 		return TapeIncludes.remove(tape);
 	}
 	
	public Integer getTapeIncludes(Tape tape)
 	{
 		return TapeIncludes.get(tape);
 	}
 	
 	public void clearTapeIncludes()
 	{
		TapeIncludes.clear();
 	}
 
 	
 	// BIASES
 	
 	/**
 	 * @return the biases
 	 */
 	@SuppressWarnings("unchecked")
 	public HashMap<Integer, Double> getBiases() {
 		return (HashMap<Integer, Double>) Biases.clone();
 	}
 	
	public Double addBias(int typeID, double percentageBias)
 	{
 		if(percentageBias < 0)
 		{
 			throw new IllegalArgumentException("A type cannot have a negative percentage bias.");
 		}
 		return Biases.put(typeID, percentageBias);
 	}
 	
	public void addBiases(HashMap<Integer, Double> biases)
 	{
 		Iterator<Double> percentageBiases = biases.values().iterator();
 		while(percentageBiases.hasNext())
 		{
 			if(percentageBiases.next() < 0)
 			{
 				throw new IllegalArgumentException("A type cannot have a negative percentage bias. An element in this hashmap has a negative value.");
 			}
 		}
 		Biases.putAll(biases);
 	}
 	
	public Double removeBias(int typeID)
 	{
 		return Biases.remove(typeID);
 	}
 	
	public Double getBias(int typeID)
 	{
 		return Biases.get(typeID);
 	}
 	
 	public void clearBiases()
 	{
 		Biases.clear();
 	}
 
 	
 	// LANDMARKS
 	
 	/**
 	 * @return the landmarks
 	 */
 	@SuppressWarnings("unchecked")
 	public ArrayList<Landmark> getLandmarks() {
 		return (ArrayList<Landmark>) Landmarks.clone();
 	}
 	
 	public void addLandmark(Landmark landmark)
 	{
 		Landmarks.add(landmark);
 	}
 	
 	public void addLandmarks(ArrayList<Landmark> landmarks) {
 		Landmarks.addAll(landmarks);
 	}
 	
 	public void removeLandmark(Landmark landmark)
 	{
 		Landmarks.remove(landmark);
 	}
 	
 	public void clearLandmarks()
 	{
 		Landmarks.clear();
 	}
 }
