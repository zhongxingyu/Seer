 package com.bigvisible.kanbansimulator;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 import java.util.List;
 
 public class Stimulator {
 	private int totalStories = 0;
 	private int storiesCompleted = 0;
 	private int storiesUnplayed = 0;
	private int batchSize = 11;
 	private int businessAnalystCapacity = 1;
 	private int developmentCapacity = 1;
 	private int webDevelopmentCapacity = 1;
 	private int qualityAssuranceCapacity = 1;
 	private List<IterationResult> results = new LinkedList<IterationResult>();
 
 	public void run(OutputStream rawOutputStream) {
 		if(rawOutputStream == null) {
 			rawOutputStream = new NullOutputStream();
 		}
 		PrintWriter output = new PrintWriter(rawOutputStream);
 		
 		int iterationNumber = 1;
 		storiesUnplayed = totalStories;
 	    IterationResult iteration = new IterationResult();
 	    iteration.setIterationNumber(iterationNumber);
 	    
 		while(storiesCompleted < totalStories) {
 		    iteration.setPutIntoPlay(Math.min(storiesUnplayed, batchSize));
 		    iteration.setCapacity("BA", businessAnalystCapacity);
 		    iteration.setCapacity("Dev", developmentCapacity);
 		    iteration.setCapacity("WebDev", webDevelopmentCapacity);
 		    iteration.setCapacity("QA", qualityAssuranceCapacity);
 		    iteration.run();
 		    
 		    results.add(iteration);
 		    storiesUnplayed -= iteration.getPutIntoPlay();
 		    storiesCompleted = iteration.getTotalCompleted();
 		    
 		    output.println(iteration.toCSVString());
 		    
 		    iteration = iteration.nextIteration();
 		}
 		// It's this PrintWriter instance that's buffering, calling flush() on the wrapped
 		// raw OutputStream will have no effect.
 		output.flush();
 	}
 
 	public void setBatchSize(int batchSize) {
 		this.batchSize = batchSize;
 	}
 
 	public void setBusinessAnalystCapacity(int businessAnalystCapacity) {
 		this.businessAnalystCapacity = businessAnalystCapacity;
 	}
 
 	public void setDevelopmentCapacity(int developmentCapacity) {
 		this.developmentCapacity = developmentCapacity;
 	}
 
 	public void setWebDevelopmentCapacity(int webDevelopmentCapacity) {
 		this.webDevelopmentCapacity = webDevelopmentCapacity;
 	}
 
 	public void setQualityAssuranceCapacity(int qualityAssuranceCapacity) {
 		this.qualityAssuranceCapacity = qualityAssuranceCapacity;
 	}
 
 	public List<IterationResult> results() {
 		return results;
 	}
 
 	public void addStories(int storiesToAdd) {
 		totalStories += storiesToAdd;
 		storiesUnplayed += storiesToAdd;
 	}
 
 	public int getStoriesCompleted() {
 		return storiesCompleted;
 	}
 	
 	/**
 	 * OutputStream that simply sinks all data given to it.
 	 */
 	public static class NullOutputStream extends OutputStream {
 		@Override
 		public void write(int b) throws IOException {
 			// this is where bytes go to die.
 		}
 
 	}
 
 }
