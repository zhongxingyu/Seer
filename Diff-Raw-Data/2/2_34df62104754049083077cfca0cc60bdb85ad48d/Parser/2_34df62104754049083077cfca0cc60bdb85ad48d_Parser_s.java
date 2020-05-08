 package io.reader;
 
 import io.Formater;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import members.Competitor;
 import members.Time;
 
 /**
  * @author Henrik & Philip
  * 
  *         Parses files.
  */
 public class Parser {
 
 	private int stationNr;
 
 	public Parser(int stationNr) {
 		this.stationNr = stationNr;
 	}
 
 	public Parser() {
 		stationNr = Competitor.NO_STATION;
 	}
 
 	/**
 	 * Parses the input string matrix. First parses the first line to get the
 	 * type of each column. Then loop each row and add the data in each row to
 	 * either a new or an already existing competitor.
 	 * 
 	 * @param input
 	 *            The string matrix of data to parse.
 	 * @param competitors
 	 *            The hashmap of competitors to modify.
 	 * @return
 	 * @throws ParserException
 	 *             If the input is incorrect.
 	 */
 	public Map<Integer, Competitor> parse(ArrayList<ArrayList<String>> input,
 			Map<Integer, Competitor> cs) throws ParserException {
 		if (input.size() < 2)
 			throw new ParserException("Invalid input.");
 
 		Map<Integer, Competitor> competitors = new HashMap<Integer, Competitor>(
 				cs);
 		ArrayList<Identifier> types = new ArrayList<Identifier>();
 		ArrayList<String> firstLine = input.get(0);
 		String file = "";
 		// Parses the first line to know what each column means.
 		types = parseIdentifier(firstLine, file);
 
 		if (types.size() < 1 || types.get(0) != Identifier.start_nr)
 			throw new ParserException("Missing start number.");
 
 		parseRows(input.subList(1, input.size()), types, competitors);
 		return competitors;
 	}
 
 	public Map<Integer, Competitor> parse(List<ArrayList<String>> input,
 			Map<Integer, Competitor> cs, FileIdentifier fileIdentifier)
 			throws ParserException {
		if (input.size() < 2)
 			throw new ParserException("Invalid input.");
 
 		Map<Integer, Competitor> competitors = new HashMap<Integer, Competitor>(
 				cs);
 		ArrayList<Identifier> types = new ArrayList<Identifier>();
 		types = parseIdentifier(fileIdentifier);
 
 		if (types.size() < 1 || types.get(0) != Identifier.start_nr)
 			throw new ParserException("Missing start number.");
 
 		parseRows(input, types, competitors);
 		return competitors;
 	}
 
 	/**
 	 * Parses all the rows and att the data to competitors which in turn are
 	 * added to the hashmap of competitors.
 	 * 
 	 * @param input
 	 *            an arraylist with rows
 	 * @param types
 	 *            types of each column
 	 * @param competitors
 	 *            hashmap with competitors
 	 * @throws ParserException
 	 *             thrown if row does not contain the expected nbr of rows
 	 */
 	private void parseRows(List<ArrayList<String>> input,
 			ArrayList<Identifier> types, Map<Integer, Competitor> competitors)
 			throws ParserException {
 		String classType = "";
 		for (int i = 0; i < input.size(); i++) {
 			ArrayList<String> row = input.get(i);
 
 			if (row.size() != 1 && row.size() != types.size()) {
 				throw new ParserException("Column length mismatch.");
 			} else if (row.size() == 1) {
 				classType = row.get(0);
 				continue;
 			}
 
 			// Startnbr is always first column.
 			int startNbr = Integer.valueOf(row.get(0));
 
 			Competitor comp = competitors.get(startNbr);
 			// If comp does not already exist in hashmap, create a new one
 			if (comp == null) {
 				comp = new Competitor(startNbr);
 			}
 
 			if (classType != "") {
 				comp.setClassType(classType);
 			}
 
 			parseRow(row, types, comp);
 
 			competitors.put(startNbr, comp);
 		}
 	}
 
 	/**
 	 * Parses a row and adds data to the competitor. How to interpret each
 	 * column depends on which types are in the list types.
 	 * 
 	 * @param row
 	 *            the row to parse
 	 * @param types
 	 *            the types for each column
 	 * @param comp
 	 *            the competitor to add the data to
 	 * @throws ParserException
 	 *             is thrown if it finds an invalid type
 	 */
 	private void parseRow(ArrayList<String> row, ArrayList<Identifier> types,
 			Competitor comp) throws ParserException {
 		// Starts at index 1 because first column (startnbr) is already
 		// parsed
 		for (int j = 1; j < row.size(); j++) {
 			switch (types.get(j)) {
 			case finish_time:
 				comp.addFinishTime(Time.parse(row.get(j)), stationNr);
 				break;
 			case name:
 				comp.addName(row.get(j));
 				break;
 			case start_time:
 				comp.addStartTime(Time.parse(row.get(j)), stationNr);
 				break;
 			default:
 				throw new ParserException("Invalid type.");
 			}
 		}
 	}
 
 	/**
 	 * Creates a new HashMap of competitors and calls the other parse method.
 	 * 
 	 * @param input
 	 *            The string matrix of data.
 	 * @return A hashmap of competitors.
 	 * @throws ParserException
 	 *             If the input file is incorrect.
 	 */
 	public Map<Integer, Competitor> parse(ArrayList<ArrayList<String>> input,
 			FileIdentifier identifier) throws ParserException {
 		Map<Integer, Competitor> competitors = new HashMap<Integer, Competitor>();
 		competitors = parse(input, competitors, identifier);
 
 		return competitors;
 	}
 
 	/**
 	 * Creates a new HashMap of competitors and calls the other parse method.
 	 * 
 	 * @param input
 	 *            The string matrix of data.
 	 * @return A hashmap of competitors.
 	 * @throws ParserException
 	 *             If the input file is incorrect.
 	 */
 	public Map<Integer, Competitor> parse(ArrayList<ArrayList<String>> input)
 			throws ParserException {
 		Map<Integer, Competitor> competitors = new HashMap<Integer, Competitor>();
 		competitors = parse(input, competitors);
 
 		return competitors;
 	}
 
 	/**
 	 * Container for column types.
 	 */
 	private enum Identifier {
 		start_time, finish_time, name, start_nr;
 	}
 
 	/**
 	 * Container for file types.
 	 */
 	public enum FileIdentifier {
 		name_file, start_file, finish_file;
 	}
 
 	/**
 	 * Parses the first lines and returns an arraylist containing enum types the
 	 * types of the columns.
 	 * 
 	 * @param firstLine
 	 *            The first line containing types of columns.
 	 * @return the arraylist of types
 	 * @throws ParserException
 	 *             if a type is invalid
 	 */
 	private ArrayList<Identifier> parseIdentifier(ArrayList<String> firstLine,
 			String file) throws ParserException {
 		ArrayList<Identifier> types = new ArrayList<Identifier>();
 
 		for (String s : firstLine) {
 			if (s.equalsIgnoreCase(Formater.START_NR)) {
 				types.add(Identifier.start_nr);
 			} else if (s.equalsIgnoreCase(Formater.START_TIME)) {
 				types.add(Identifier.start_time);
 			} else if (s.equalsIgnoreCase(Formater.FINISH_TIME)) {
 				types.add(Identifier.finish_time);
 			} else if (s.equalsIgnoreCase(Formater.NAME)) {
 				types.add(Identifier.name);
 			} else {
 				throw new ParserException("Invalid String " + s);
 			}
 
 		}
 
 		return types;
 	}
 
 	/**
 	 * Adds types to the list of types of columns, depending on what type of
 	 * file, specified in fileIdentity.
 	 * 
 	 * @param fileIdentity
 	 *            the type of file
 	 * @return an arraylist of types of columns
 	 * @throws ParserException
 	 */
 	private ArrayList<Identifier> parseIdentifier(FileIdentifier fileIdentity)
 			throws ParserException {
 		ArrayList<Identifier> types = new ArrayList<Identifier>();
 		types.add(Identifier.start_nr);
 		if (fileIdentity.equals(FileIdentifier.start_file)) {
 			types.add(Identifier.start_time);
 		} else if (fileIdentity.equals(FileIdentifier.finish_file)) {
 			types.add(Identifier.finish_time);
 		} else if (fileIdentity.equals(FileIdentifier.name_file)) {
 			types.add(Identifier.name);
 		} else {
 			throw new ParserException("Invalid file identity");
 		}
 
 		return types;
 	}
 
 }
