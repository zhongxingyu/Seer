 package chalmers.dax021308.ecosystem.model.environment;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 
 import chalmers.dax021308.ecosystem.model.agent.AbstractAgent;
 import chalmers.dax021308.ecosystem.model.agent.IAgent;
 import chalmers.dax021308.ecosystem.model.population.AbstractPopulation;
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.util.Log;
 
 /**
  * Class representing a Recorded simulation.
  * @author Erik
  *
  */
 public class SimulationRecording {
 
 	/*	Text syntax constants */
 	private static final String headerDividerStart = "<HEAD>";
 	private static final String headerDividerEnd   = "</HEAD>";
 	private static final String frameDividerStart = "<FRM>";
 	private static final String frameDividerEnd   = "</FRM>";
 	private static final String populationDivider = "<POP>";
 	private static final String agentDivider      = "<AGE>";
 	
 	private File recordedFile;
 	private BufferedReader br;
 	private FileInputStream fileStream;
 	private PrintWriter pw;
 	
 	public boolean initReading(String fileName) {
 		recordedFile = new File(fileName);
 		if (!recordedFile.exists()) {
 			return false;
 		}
 		if (!recordedFile.canRead()) {
 			return false;
 		}
 		try {
 			fileStream = new FileInputStream(recordedFile);
 			Charset utf8 = Charset.forName("UTF-8");
 			br = new BufferedReader(new InputStreamReader(fileStream, utf8));
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean initWriting(String fileName) {
 		recordedFile = new File(fileName);
 		if (recordedFile.exists()) {
 			recordedFile.delete();
 		}
 		try {
 			recordedFile.createNewFile();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		if (!recordedFile.canWrite()) {
 			return false;
 		}
 		try {
 			pw = new PrintWriter(recordedFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	public void appendFrame(List<IPopulation> popList) {
 		pw.println(frameDividerStart);
 		for (IPopulation p : popList) {
 			pw.println(populationDivider + ';' + p.toBinaryString());
 			for (IAgent a : p.getAgents()) {
 				pw.println(agentDivider + ';' + a.toBinaryString());
 			}
 		}
 		pw.println(frameDividerEnd);
 	}
 	
	/**
	 * Read one frame from the loaded text-file and return it.
	 * @return
	 */
 	public synchronized List<IPopulation> readFrame() {
 		List<IPopulation> currentFrame = null;
 		String input = null;
 		try {
 			input = br.readLine();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		IPopulation currentPop = null;
 		while (input != null) {
 			if (input.startsWith(frameDividerStart)) {
 				currentFrame = new ArrayList<IPopulation>();
 			} else if (input.startsWith(frameDividerEnd)) {
				if(currentPop != null) {
					currentFrame.add(currentPop);
				}
 				return currentFrame;
 			} else if (input.startsWith(populationDivider)) {
				Log.v(input);
 				if (currentPop != null) {
 					currentFrame.add(currentPop);
 				}
 				String[] inputArr = input.split(";", 2);
 				currentPop = AbstractPopulation.createFromFile(inputArr[1]);
				Log.v(currentPop.toString());
 			} else if (input.startsWith(agentDivider)) {
 				if (currentPop != null) {
 					String[] inputArr = input.split(";", 2);
 					IAgent newIAgent = AbstractAgent.createFromFile(inputArr[1]);
 					currentPop.getAgents().add(newIAgent);
 				}
 			}
 			try {
 				input = br.readLine();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		Log.v("Error no ending found while reading Simulation.");
 		return currentFrame;
 	}
 	
	/**
	 * Closes the streams used by this class.
	 */
 	public void close() {
 		try {
 			if(br != null) br.close();
 			if(fileStream != null) fileStream.close();
 			if(pw != null) pw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 	/**
 	 * Helper method for readRecordFromDisk.
 	 * 
 	 * Unfinished! Untested!
 	 * @param br
 	 * @return
 	 */
	@Deprecated
 	private List<List<IPopulation>> parseFile() {
 
 		List<List<IPopulation>> result = new ArrayList<List<IPopulation>>();
 
 		List<IPopulation> currentFrame = null;
 		IPopulation currentPop = null;
 
 		String input = null;
 		try {
 			input = br.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		while (input != null) {
 			try {
 				input = br.readLine();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			if (input.startsWith(frameDividerStart)) {
 				if (currentFrame != null) {
 					result.add(currentFrame);
 				}
 				currentFrame = new ArrayList<IPopulation>();
 			} else if (input.startsWith(populationDivider)) {
 				if (currentPop != null) {
 					currentFrame.add(currentPop);
 				}
 				String[] inputArr = input.split(";", 2);
 				currentPop = AbstractPopulation.createFromFile(inputArr[1]);
 			} else if (input.startsWith(agentDivider)) {
 				if (currentPop != null) {
 					String[] inputArr = input.split(";", 2);
 					IAgent newIAgent = AbstractAgent.createFromFile(inputArr[1]);
 					currentPop.getAgents().add(newIAgent);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Saves the given recording to the filePath
 	 * 
 	 * Unfinished! Untested!
 	 * 
 	 * @param record
 	 * @param filePath
 	 * @return
 	 */
 	public boolean dumpRecordToDisk(List<List<IPopulation>> record,
 			String filePath) {
 		for (List<IPopulation> popList : record) {
 			pw.println(frameDividerStart);
 			for (IPopulation p : popList) {
 				pw.println(populationDivider + ';' + p.toBinaryString());
 				for (IAgent a : p.getAgents()) {
 					pw.println(agentDivider + ';' + a.toBinaryString());
 				}
 			}
 			pw.println(frameDividerEnd);
 		}
 		pw.close();
 		return true;
 	}
 }
