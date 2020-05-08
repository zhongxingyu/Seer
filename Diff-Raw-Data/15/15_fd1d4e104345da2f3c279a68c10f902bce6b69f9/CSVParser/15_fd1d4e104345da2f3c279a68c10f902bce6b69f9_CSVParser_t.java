 package com.bluegraybox.demo;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * Utility to process a data file.
  * The data file contains one entry per row. Each entry has a user id, date, and one or more floating-point numbers, all comma-separated like so:
  * <pre>
  * foo,2011-04-01,37.5,56.8,73.9
  * </pre>
  * Invoke as:
  * <pre>
  * java -cp ../java/target/csv_parser-1.0-SNAPSHOT.jar com.bluegraybox.demo.CSVParser data.csv > data.out
  * </pre>
  */
 public class CSVParser {
 
 	private HashMap<String, RunningAverage> runningAverages = new HashMap<String, RunningAverage>();
 
 	/**
 	 * Parse an input file, perform a calculation, and print out the results. 
 	 * @param args command-line arguments; should just be filename
 	 * @throws IOException If unable to read from the input file.
 	 */
 	public static void main(String[] args) throws IOException {
 		if (args.length < 1)
 			throw new IllegalArgumentException("Missing filename parameter.");
 		String filename = args[0];
 		CSVParser parser = new CSVParser();
 		parser.processFile(filename);
 		HashMap<String,RunningAverage> averagesSet = parser.getAverages();
 		List<String> keys = new ArrayList<String>(averagesSet.keySet());
 		Collections.sort(keys);
 		for (String key : keys) {
 			System.out.println( String.format("%s\t%.8f", key, averagesSet.get(key).getValue()));
 		}
 	}
 
 	/**
 	 * Process a file and print out the averages for each ID.
 	 * @param filename Path to file to read from.
 	 * @throws IOException If unable to read from the input file.
 	 */
 	public void processFile(String filename) throws IOException {
 		Reader fileReader = new FileReader(filename);
 		processReader(fileReader);
 	}
 
 	/**
 	 * Process a Reader and print out the averages for each ID.
 	 * @param fileReader Reader to read from.
 	 * @throws IOException If unable to read from the input Reader.
 	 */
 	public void processReader(Reader fileReader) throws IOException {
 		BufferedReader reader = new BufferedReader(fileReader);
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 			processLine(line);
 		}
 	}
 
 	/**
 	 * Process a single line of the input.
 	 * @param line user id, date, and one or more digits, comma-separated like so:
 	 * "foo,2011-04-01,37.5,56.8,73.9"
 	 */
 	public void processLine(String line) {
 		String[] fields = line.split(",");
 		if (fields.length >= 3) {
 			// Entries would normally be unique by id and date, but we don't enforce that.
			String client_id = fields[0];
 			// String date = fields[1];
 			String[] data = Arrays.copyOfRange(fields, 2, fields.length);
 			try {
 			double value = calcValue(data);
 
			if (! runningAverages.containsKey(client_id))
				runningAverages.put(client_id, new RunningAverage());
			RunningAverage rAvg = runningAverages.get(client_id);
 			rAvg.updateAverage(value);
 			}
 			catch (NumberFormatException ex) {
 				// FIXME: should log this
 				// skip this line and go on to next one.
 			}
 		}
 	}
 
 	/**
 	 * Calculate the average of an arry of numeric strings. 
 	 * @param data Array of numeric strings.
 	 * @return
 	 */
 	protected double calcValue(String[] data) {
 		double total = 0;
 		for (int i = 0; i < data.length; i++) {
 			total += Double.valueOf(data[i]);
 		}
 		double value = total / data.length;  // calculate the average of the fields, not their total.
 		return value;
 	}
 
 	public HashMap<String,RunningAverage> getAverages() {
 		return runningAverages;
 	}
 	
	public double getAverage(String client_id) {
 		double value = 0.0;
		RunningAverage average = runningAverages.get(client_id);
 		if (average != null)
 			value = average.getValue();
 		return value;
 	}
 	
 }
 
 class RunningAverage {
 	private int count = 0;
 	private double average = 0;
 
 	void updateAverage(double value) {
 		double old_total = average * count;
 		average = (old_total + value) / (count + 1);
 		count += 1;
 	}
 
 	public double getValue() {
 		return average;
 	}
 }
