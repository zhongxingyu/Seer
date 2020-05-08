 package com.ankurdave.boggle;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 public class GeneticClientThread extends Thread {
 	private Population bp;
 	private Dictionary dict;
 	private int sideLength, startingPopulation, startingChildrenPerCouple,
 	        startingPopCap;
 	private GeneticClient manager;
 	private Boolean resetRequested = false, terminateRequested = false;
 	private Board inboundMigrant;
 	public GeneticClientThread(String dictPath, int sideLength,
 	        int startingPopulation, int childrenPerCouple, int popCap,
 	        GeneticClient manager) {
 		this.sideLength = sideLength;
 		this.startingPopulation = startingPopulation;
 		this.startingChildrenPerCouple = childrenPerCouple;
 		this.startingPopCap = popCap;
 		this.manager = manager;
 		// init dictionary
 		dict = new Dictionary();
 		dict.buildDictionary(dictPath);
 		// init population
 		bp = new Population(sideLength, this.startingPopulation,
 		        startingChildrenPerCouple, startingPopCap, dict);
 	}
 	@Override public void run() {
 		while (true) {
 			try {
 				if (inboundMigrant != null) {
 					bp.add(inboundMigrant);
 					inboundMigrant = null;
 				}
 				bp.evolve();
 				System.out.println(bp);
 				for (Board b : bp.getCurrentGeneration()) {
 					System.out.println(b);
 				}
 				System.out.println();
 				if (resetRequested) {
 					System.out.println("Reset");
 					bp = new Population(sideLength, this.startingPopulation,
 					        startingChildrenPerCouple, startingPopCap, dict);
 					inboundMigrant = null;
 					resetRequested = false;
 					continue;
 				}
 				if (terminateRequested) {
 					break;
 				}
 				// communicate with manager
 				manager.setHighest(bp.highest());
				// TODO analyze migration algorithm
 				manager.setOutboundMigrant(Util.weightedRandomFromList(bp
 				        .getCurrentGeneration()));
 			}
 			catch (GenerationEmptyException e) {
 				System.err.println(e);
 				break;
 			}
 		}
 	}
 	public void terminate() {
 		terminateRequested = true;
 	}
 	public int getSideLength() {
 		return sideLength;
 	}
 	public Dictionary getDictionary() {
 		return dict;
 	}
 	public void setInboundMigrant(Board migrant) {
 		inboundMigrant = migrant;
 	}
	// TODO analyze variable pop cap
 	public void setPopCap(int popCap) {
 		bp.setPopCap(popCap);
 	}
 	public void reset() {
 		resetRequested = true;
 	}
 }
