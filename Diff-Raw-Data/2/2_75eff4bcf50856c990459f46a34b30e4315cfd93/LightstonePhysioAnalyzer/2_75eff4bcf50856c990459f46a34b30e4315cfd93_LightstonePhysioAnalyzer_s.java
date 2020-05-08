 package dk.itu.biologger;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import dk.itu.mario.level.generator.bio.Chunk;
 import dk.itu.mario.level.generator.bio.ScreenChunk;
 import dk.itu.mario.level.generator.bio.ScreenChunkLibrary;
 import dk.itu.mario.level.generator.bio.ScreenChunkWrapper;
 import dk.itu.mario.scene.LevelScene;
 
 public class LightstonePhysioAnalyzer {
 	
 	/**
 	 * Returns the average phasic arousal for a given levelchunk
 	 * @param tileStart the start of the levelchunk in tile counts
 	 * @param tileEnd the end of the levelchunk in tile count
 	 * @return
 	 */
 	
 	LevelScene level;
 	ArrayList<LightstoneInterpolatedSample> phasicSamples;
 	ArrayList<LightstoneInterpolatedSample> smoothedSamples;
 	ArrayList<LightstoneInterpolatedSample> interpolatedSamples;
 	ArrayList<LightstoneInterpolatedSample> normalizedSamples;
 	
 	/**
 	 * 
 	 * @param level The level that the physio-recordings were made from
 	 */
 	public LightstonePhysioAnalyzer(ArrayList<LightstoneInterpolatedSample> phasicSamples)
 	{
 		this.phasicSamples = phasicSamples;
 	}
 	
 	/**
 	 * Applies all transformations on the raw sample data.
 	 */
 	public void analyze()
 	{
 		smoothSamples();
 		interpolateSamples();
 		normalizeSamples();
 	}
 	
 	/**
 	 * Returns the mean activation for a part of the level.
 	 * @param tileStart The first tile of the chunk.
 	 * @param tileEnd The last tile of the chunk.
 	 * @return
 	 */
 	public float getScreenChunkMean(float tileStart, float tileEnd)
 	{
 		if(normalizedSamples == null)
 			this.analyze();
 		
 		ArrayList<LightstoneInterpolatedSample> temp = new ArrayList<LightstoneInterpolatedSample>();
 		float cumulativeSum = 0;
 		int count = 0;
 		
 		for(LightstoneInterpolatedSample sample : normalizedSamples)
 		{
 			if(sample.getTile() >= tileStart && sample.getTile() <= tileEnd)
 			{
 				temp.add(sample);
 				cumulativeSum += sample.getValue();
 				count++;
 			}
 		}
 		
 		float mean = 0.0f;
 		if(count > 0)
 			mean = cumulativeSum/(float)count; 
 		return mean;
 	}
 	
 	/**
 	 * Removes noise from the raw phasic signal by applying a moving average.
 	 * Currently looks 2 samples back and forth.  
 	 */
 	public void smoothSamples()
 	{
 		smoothedSamples = new ArrayList<LightstoneInterpolatedSample>();
 		
 		for(int i = 2; i < phasicSamples.size()-2; i++)
 		{ 
 			float averageScl =  (phasicSamples.get(i-2).getValue() + phasicSamples.get(i-1).getValue() + phasicSamples.get(i).getValue() + phasicSamples.get(i+1).getValue() + phasicSamples.get(i+2).getValue())/5;
 			
 			smoothedSamples.add(new LightstoneInterpolatedSample(-1, phasicSamples.get(i).getFrame(), averageScl, phasicSamples.get(i).getHrv(), phasicSamples.get(i).getMarioX()));
 		}
 	}
 	
 	public void interpolateSamples()
 	{
 		//No interpolation needed atm.
 	}
 	
 	/**
 	 * Scales all sample values to be between 0 and 1.
 	 */
 	public void normalizeSamples()
 	{
 		float min = Float.MAX_VALUE;
 		float max = Float.MIN_VALUE;
 		
 		for(LightstoneInterpolatedSample sample : smoothedSamples)
 		{
 			if(sample.getValue() < min) min = sample.getValue();
 			if(sample.getValue() > max) max = sample.getValue();
 		}
 		
 		normalizedSamples = new ArrayList<LightstoneInterpolatedSample>();
 		for(LightstoneInterpolatedSample sample : smoothedSamples)
 		{
 			float normalizedValue = (sample.getValue() - min) / max;
			normalizedSamples.add(new LightstoneInterpolatedSample(sample.getType(), sample.getFrame(), (int)normalizedValue, sample.getHrv(), sample.getMarioX())); //TODO, convert to float values
 		}
 	}
 	
 	/**
 	 * Writes all variations of samples to disk
 	 */
 	public void saveSamples(){
 		DataWriter writer = new DataWriter("originalPhasic", phasicSamples);
 		writer.writeData();
 		writer = new DataWriter("smoothed", smoothedSamples);
 		writer.writeData();
 		//writer = new DataWriter("interpolated", interpolatedSamples);
 		//writer.writeData();
 		writer = new DataWriter("normalized", normalizedSamples);
 		writer.writeData();
 	}
 	
 	public void saveChunkData(List<ScreenChunkWrapper> screenChunks)
 	{
 		ScreenChunkLibrary scLib = ScreenChunkLibrary.getInstance();
 		
 		
 		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
 		StringBuilder outString = new StringBuilder();
 		
 		String header =	"@RELATION trainingData_" + timestamp + "\n\n";
 		for(int i = 0; i < scLib.getNumOfChunks(); i++)
 		{
 			header += "@ATTRIBUTE chunk" + i + " NUMERIC\n";
 		}
 		header += "@ATTRIBUTE arousal NUMERIC\n\n";
 		header += "@DATA\n";
 		outString.append(header);
 		
 		for(ScreenChunkWrapper screenChunk : screenChunks)
 		{
 			String observation = "";
 			List<Chunk> chunks = screenChunk.sc.getChunks(null);
 			
 			//Count the number of each kind of chunk in the screenchunk
 			int[] chunkCounts = new int[scLib.getNumOfChunks()];
 			for(int i = 0; i < scLib.getNumOfChunks(); i++)
 			{
 				chunkCounts[i] = 0;
 				for(Chunk chunk : chunks)
 				{
 					if(chunk.getId() == i)
 					{
 						chunkCounts[i] += 1;
 					}
 				}
 			}
 			
 			//Add the chunkcounts to the observations
 			for(int j = 0; j < chunkCounts.length; j++)
 			{
 				observation += Integer.toString(chunkCounts[j]) + ",";
 			}
 			
 			float chunkMean = this.getScreenChunkMean(screenChunk.x, (screenChunk.x + screenChunk.sc.getWidth()) );
 			
 			if(chunkMean > 0){
 				observation += chunkMean;
 				observation += "\n";
 				outString.append(observation);
 			}
 		}
 		
 		
 		
 		try {
 			FileWriter file = new FileWriter(new File("trainingSamples" + "_" + timestamp + ".arff"));
 			file.write(outString.toString());
 			file.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
