 package com.earlofmarch.reach.input;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.logging.*;
 
 /**
  * Windows access to buzzers through BuzzIO.
  * @author Ian Dewan
  *
  */
 class WindowsBuzzerBinding extends AbstractBuzzerBinding {
 	private HashMap<String, Boolean> sensitivities = new HashMap<String, Boolean>();
 	private volatile Pair<Integer, Integer> currBuzz = null;
 	private BufferedReader in;
 	private PrintWriter out;
 	private PipeReader pr;
 	
 	private Object stateLock = new Object();
 	
 	public WindowsBuzzerBinding(InputStream i, OutputStream o) throws IOException {
 		Logger.getLogger("reach.input").log(Level.INFO,
 				"Creating windows binding w/ " + i + " " + o);
 		out = new PrintWriter(o, true);
 		in = new BufferedReader(new InputStreamReader(i));
 		
 		pr = new PipeReader();
 		pr.start();
 	}
 	
 	@Override
 	public void setButtonSensitivity(boolean red, boolean blue, boolean green,
 			boolean orange, boolean yellow) {
		Logger.getLogger("reach.input").log(Level.INFO, "Setting sensitivity: R-" +
 			red + " B-" + blue + " G-" + green + " O-" + orange + " Y-" + yellow);
 		synchronized(sensitivities) {
 			sensitivities.put("red", red);
 			sensitivities.put("blue", blue);
 			sensitivities.put("green", green);
 			sensitivities.put("orange", orange);
 			sensitivities.put("yellow", yellow);
 		}
 	}
 	
 	@Override
 	public boolean clear() {
 		Logger.getLogger("reach.input").log(Level.INFO, "Clear!");
 		synchronized (stateLock) {
 			try{
 				if(currBuzz==null)
 					return false;
 				pr.unlight(currBuzz.getFirst(), currBuzz.getSecond());
 			}catch(NullPointerException e){
 				System.err.println("Error unlighting ");
 				return false;
 			}
 			currBuzz = null;
 		}
 		return true;
 	}
 	
 	@Override
 	public Pair<Integer, Integer> getCurrentBuzzed() {
 		synchronized (stateLock) {
 			Logger.getLogger("reach.input").log(Level.INFO, "Getting buzz:" +
 					((currBuzz == null) ? "null" : currBuzz));
 			return currBuzz;
 		}
 	}
 	
 	private class PipeReader extends Thread {
 		
 		public void run() {
 			String input;
 			String[] parts;
 			String[] subparts;
 			Logger.getLogger("reach.input").log(Level.INFO, "In PipeReader.run()");
 			try {
 				input = in.readLine();
 			} catch (IOException e) {
 				Logger.getLogger("reach.input").log(Level.SEVERE,
 						"IO error getting data from glue.exe.", e);
 				return;
 			}
 			while(input != null) {
 				Logger.getLogger("reach.input").log(Level.INFO, "Got line: " + input);
 				try {
 					parts = input.split(" |\t");
 					Logger.getLogger("reach.input").log(Level.INFO, "Split into" +
 							parts);
 					if (parts[0].equals("press")) {
 						subparts = parts[1].split(":");
 						buzz(Integer.parseInt(subparts[0]),
 								Integer.parseInt(subparts[1]), subparts[2]);
 					} else if (parts[0].equals("unplugged")) {
 						unplug();
 					} else if (parts[0].equals("error")) {
 						Logger.getLogger("reach.input").log(Level.WARNING,
 							"Error occured in glue.exe:" + input);
 					}
 				} catch (Exception e) {
 					Logger.getLogger("reach.input").log(Level.WARNING,
 							"Error occurred parsing glue.exe input.", e);
 				}
 				try {
 					input = in.readLine();
 				} catch (IOException e) {
 					Logger.getLogger("reach.input").log(Level.SEVERE,
 						"IO error getting data from glue.exe.", e);
 					return;
 				}
 			}
 		}
 		
 		private synchronized void buzz(int h, int b, String button) {
 			synchronized (sensitivities) {
 			synchronized (stateLock) {
 				Logger.getLogger("reach.input").log(Level.INFO, "Buzzed: "
 						+ h + ":" + b + ":" + button);
 				if (sensitivities.containsKey(button) &&
 						sensitivities.get(button).equals(true) &&
 						currBuzz == null) {
 					out.println("light " + h + ":" + b);
 					currBuzz = new Pair<Integer, Integer>(h, b);
 					WindowsBuzzerBinding.this.buzz();
 				}
 			}
 			}
 		}
 		
 		public void unlight(Integer h, Integer b) {
 			Logger.getLogger("reach.input").log(Level.INFO, "In PipeReader.unlight()");
 			out.println("unlight " + h + ":" + b);
 		}
 	}
 }
