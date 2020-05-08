 package model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 
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
 	
 	public int setTapeIncludes(Tape tape, int includesBitmap)
 	{
 		return TapeIncludes.put(tape, includesBitmap);
 	}
 	
 	public void setTapeIncludes(HashMap<Tape, Integer> tapeIncludes)
 	{
 		TapeIncludes.putAll(tapeIncludes);
 	}
 	
	public int removeTapeIncludes(Tape tape)
 	{
		return TapeIncludes.remove(tape);
 	}
 	
	public int getTapeIncludes(Tape tape)
 	{
		return TapeIncludes.get(tape);
 	}
 	
 	public void clearTapeIncludes()
 	{
 		clearTapeIncludes();
 	}
 
 	
 	// BIASES
 	
 	/**
 	 * @return the biases
 	 */
 	@SuppressWarnings("unchecked")
 	public HashMap<Integer, Double> getBiases() {
 		return (HashMap<Integer, Double>) Biases.clone();
 	}
 	
 	public double setBias(int typeID, double percentageBias)
 	{
 		return Biases.put(typeID, percentageBias);
 	}
 	
 	public void setBiases(HashMap<Integer, Double> biases)
 	{
 		Biases.putAll(biases);
 	}
 	
 	public double removeBias(int typeID)
 	{
 		return Biases.remove(typeID);
 	}
 	
 	public double getBias(int typeID)
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
