 package osu.parser;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.StringTokenizer;
 
 import osu.beatmap.Metadata;
 import osu.game.*;
 
 import android.graphics.Point;
 import android.util.Log;
 
 public class Parser {
 		
 	// Storage
 	HashMap<Subsections, HashMap<String, String>> dict;
 	
 	
 	// *** CONSTRUCTORS *** //
 	public Parser()
 	{
 		initDict();
 	}
 	
 	
 	// *** INIT METHODS *** //
 	/**
 	 * Initializes the HashMap we store all of our information in as we are parsing.
 	 */
 	private void initDict()
 	{
 		dict = new HashMap<Subsections, HashMap<String, String>>();
 		
 		// Init Subsections
 		dict.put(Subsections.GENERAL, new HashMap<String, String>());
 		dict.put(Subsections.EDITOR, new HashMap<String, String>());
 		dict.put(Subsections.METADATA, new HashMap<String, String>());
 		dict.put(Subsections.DIFFICULTY, new HashMap<String, String>());
 		
 		HashMap<String, String> cur;
 		
 		// General //
 		cur = dict.get(Subsections.GENERAL);
 		cur.put("audiofilename", null);
 		cur.put("audioleadin", null);
 		cur.put("previewtime", null);
 		cur.put("countdown", null);
 		cur.put("sampleset", null);
 		cur.put("stackleniency", null);
 		cur.put("mode", null);
 		cur.put("letterboxinbreaks", null);
 		cur.put("skinpreference", null);
 		cur.put("countdownoffset", null);
 		cur.put("storyfireinfront", null);
 		cur.put("epilepsywarning", null);
 		
 		// Editor //
 		cur = dict.get(Subsections.EDITOR);
 		cur.put("distancespacing", null);
 		cur.put("beatdivisor", null);
 		cur.put("gridsize", null);
 		
 		// Metadata //
 		cur = dict.get(Subsections.METADATA);
 		cur.put("title", null);
 		cur.put("artist", null);
 		cur.put("creator", null);
 		cur.put("version", null);
 		cur.put("source", null);
 		cur.put("tags", null);
 		
 		// Difficulty //
 		cur = dict.get(Subsections.DIFFICULTY);
 		cur.put("hpdrainrate", null);
 		cur.put("circlesize", null);
 		cur.put("overalldifficulty", null);
 		cur.put("approachrate", null);
 		cur.put("slidermultiplier", null);
 		cur.put("slidertickrate", null);
 	}
 	
 	
 	// *** ACTION *** //
 	/**
 	 * Parses an osu file and returns data to the given data structures.
 	 * Opens the path given.
 	 * 
 	 * @param path The path to the desired desktop resource to be read.
 	 * @throws ParseException If anything unexpected happens in the file, causing parsing errors.
 	 * @throws IOException For any problems with java IO (BufferedReader).
 	 */
 	public void parseResource(String path, ParserContainer pc) throws ParseException, IOException
 	{
 		// Open Resources
 		File file = new File(path);
 		BufferedReader reader = new BufferedReader(new FileReader(file));
 		
 		// Parse!
 		parse(reader, pc);
 		
 		// Close all used resources
 		reader.close();
 	}
 	
 	
 	/**
 	 * Parses and returns the Metadata section of the given file at the given path.
 	 */
 	public Metadata parseMetadataResource(String path) throws ParseException, IOException
 	{
 		Metadata out = new Metadata();
 		
 		// File IO
 		File file = new File(path);
 		BufferedReader reader = new BufferedReader(new FileReader(file));
 		
 		// Parse for metadata!
 		String line = getSpecificHeader(reader, "metadata");
 		if (line == null)
 			throw new ParseException("Metadata header does not exist.");
 		
 		line = reader.readLine();
 		while (!lineCheck(line)) // Loop until EOF or a new header
 		{
 			if (line.length() == 0) // Error checking
 			{
 				line = reader.readLine();
 				continue;
 			}
 			
 			String to_check = removeSpaces(line.substring(0, line.indexOf(':'))).toLowerCase();
 			String val = removeFirstSpaces(line.substring(line.indexOf(':') + 1));
 			
 			if (to_check.equals("title"))
 				out.title = val;
 			else if (to_check.equals("artist"))
 				out.artist = val;
 			else if (to_check.equals("creator"))
 				out.creator = val;
 			else if (to_check.equals("version"))
 				out.version = val;
 			else if (to_check.equals("source"))
 				out.source = val;
 			else if (to_check.equals("tags"))
 				out.tags = val;
 			
 			line = reader.readLine();
 		}
 		
 		// Make sure we got good results
 		if (out.anyNull())
 			throw new ParseException("A field in Metadata was not filled in.");
 		
 		// Close all used resources
 		reader.close();
 		
 		return out;
 	}
 
 	
 	/**
 	 * Parses and returns the overall difficulty from the given file at the given path.
 	 * Throws an exception if this attribute does not exist or cannot be formatted
 	 */
 	public int parseOverallDifficulty(String path) throws ParseException, IOException, NumberFormatException
 	{
 		// File IO
 		File file = new File(path);
 		BufferedReader reader = new BufferedReader(new FileReader(file));
 		
 		// Parse for metadata!
 		String line = getSpecificHeader(reader, "difficulty");
 		if (line == null)
 			throw new ParseException("Difficulty header does not exist.");
 		
 		line = reader.readLine();
 		while (!lineCheck(line))
 		{
 			String attrib = removeSpaces(line.substring(0, line.indexOf(":")).toLowerCase()); 
 			
 			if (attrib.equals("overalldifficulty"))
 				return Integer.parseInt(removeFirstSpaces(line.substring(line.indexOf(":") + 1)));
 			
 			line = reader.readLine();
 		}
 		
 		throw new ParseException("OverallDifficulty does not exist in this file!");
 	}
 	
 	
 	/**
 	 * Parses an osu file and returns data to the given data structures based on the BufferedReader sent to it.
 	 * 
 	 * @param reader The BufferedReader to read the file from.
 	 * @throws ParseException If anything unexpected happens in the file, causing parsing errors.
 	 * @throws IOException For any problems with java IO (BufferedReader).
 	 */
 	private void parse(BufferedReader reader, ParserContainer pc) throws ParseException, IOException
 	{
 		// NOTE: I do not check to make sure all the necessary subsections are found. Perhaps we want to do this.
 		
 		// Error Checking
 		if (reader == null) // Null Reader
 			throw new ParseException("BufferedReader is null");
 		
 		if (!confirmFileFormat(reader)) // Invalid file header
 			throw new ParseException("Invalid file format.");
 		
 		// Iterate through sub-sections and parse their attributes
 		String output = getFirstHeader(reader);
 		
 		while (output != null) // Loop on subsections until the end of the file (null)
 			output = manageSubsection(reader, output, pc);
 		
 		pc.dict = dict; // Return dict
 	}
 	
 	
 	/**
 	 * Parses the subsection following the current given header (String in) and handles the attributes accordingly.
 	 * 
 	 * @param in The current subsection header String read off from the BufferedReader.
 	 * @return The first non-attribute String found. Returns null if the end of the current file has been reached.
 	 * @throws IOException 
 	 * @throws ParseException 
 	 */
 	private String manageSubsection(BufferedReader reader, String in, ParserContainer pc) throws IOException, ParseException
 	{
 		String section_name = in.substring(in.indexOf('[') + 1, in.lastIndexOf(']')).toUpperCase();
 		
 		try {
 			Subsections section_enum = Subsections.valueOf(section_name);
 			
 			switch (section_enum)
 			{
 			
 			// Attribute-Based
 			case GENERAL:
 			case EDITOR:
 			case METADATA:
 			case DIFFICULTY:
 				return handleAttributeBased(reader, section_enum);
 				
 			// List-Based
 			case TIMINGPOINTS:
 				return handleTimingPoints(reader, pc);
 			case COLOURS:
 				return handleColours(reader, pc);
 			case HITOBJECTS:
 				return handleHitObjects(reader, pc);
 				
 			// Special-Case
 			case EVENTS:
 				return handleEvents(reader, pc);
 				
 			default:
 				throw new ParseException("Coder error in manageSubsection: missed  header title.");
 			}
 		} catch (IllegalArgumentException ex) {
 			throw new ParseException("Unknown section header.");
 		}
 	}
 	
 	
 	/**
 	 * Ensures the file format header conforms to the osu! format.
 	 * @return True if the header is vaild, false otherwise
 	 * @throws IOException If the act of reading the file's input stream throws an Exception.
 	 */
 	private boolean confirmFileFormat(BufferedReader reader) throws IOException
 	{
 		String output = reader.readLine();
 		
 		// Error checking
 		if (output == null)
 			return false;
 		
 		// Check for vaild version descriptor
 		if (!output.substring(0, ParserConstants.HEADER_START.length()).equals(ParserConstants.HEADER_START))
 			return false;
 		
 		// Check for valid version number
 		if (!intArrContains(ParserConstants.ACCEPTABLE_FILE_VERSIONS, Integer.parseInt(output.substring(ParserConstants.HEADER_START.length()))))
 			return false;
 		
 		return true;
 	}
 	
 	
 	/**
 	 * Finds the first header in the file (as defined by "[HEADER_NAME]").
 	 * Helps deal with unnecessary newlines.
 	 * 
 	 * @param reader The BufferedReader we are using to read in from the file.
 	 * @return The String of the first header we find.
 	 * @throws IOException If any Java IO-related issues occur
 	 * @throws ParseException If any parsing errors occur
 	 */
 	private String getFirstHeader(BufferedReader reader) throws IOException, ParseException
 	{
 		String output = "";
 		while (!isHeader(output))
 		{
 			output = reader.readLine();
 			
 			if (output == null) // Error checking
 				throw new ParseException("No subsections found.");
 		}
 		
 		return output;
 	}
 	
 	
 	// *** HANDLE SUBSECTIONS *** //
 	// Attribute-Based //
 	private String handleAttributeBased(BufferedReader reader, Subsections header) throws IOException
 	{
 		HashMap<String, String> subsect = dict.get(header); // Current subsection we are working in
 		
 		String line, attrib, val;
 		while (true)
 		{
 			line = reader.readLine();
 			
 			if (lineCheck(line)) // Return at end of file or next header
 				break;
 			if (line.length() == 0) // Skip over blank lines
 				continue;
 			
 //			line = line.toLowerCase(); // For easier checking against HashMap
 			
 			// Parse left-side of expression
 			attrib = removeSpaces(line.substring(0, line.indexOf(':'))).toLowerCase();
 			
 			// Parse right-side of expression
 			line = line.substring(line.indexOf(':') + 1);
 			if (line.length() == 0)
 				continue;
 			
 			if (line.charAt(0) == ' ')
 				val = line.substring(1);
 			else
 				val = line;
 			
 			// Set values in HashMap
 			if (subsect.containsKey(attrib))
 				subsect.put(attrib, val);
 			else // Invalid attribute
 				printError("Parser.handleAttributeBased", "Invalid Attribute, \"" + attrib + "\" in subsection, \"" + header.toString() + "\".");
 		}
 		
 		return line;
 	}
 	
 	
 	// List-Based //
 	private String handleTimingPoints(BufferedReader reader, ParserContainer pc) throws IOException
 	{
 		// List-based
 		String line;
 		while (true)
 		{
 			line = reader.readLine();
 			
 			if (lineCheck(line)) // Return at end of file or next header
 				break;
 			if (line.length() == 0) // Skip over blank lines
 				continue;
 			
 			line = removeSpaces(line); // Remove spaces
 			
 			// Get our TimingPoints
 			TimingPoint tp = new TimingPoint();
 			
 			StringTokenizer tokenizer = new StringTokenizer(line, ",", false);
 			// Offset, BPM, Time, SampleSet, CustomSampleSet, Vol, Inheritance, KIAI
 			tp.setOffset(Long.parseLong(tokenizer.nextToken()));
 			tp.setBPM(Double.parseDouble(tokenizer.nextToken()));
 			tp.setTimeSignature(Integer.parseInt(tokenizer.nextToken()));
 			tp.setSampleSet(Integer.parseInt(tokenizer.nextToken()));
 			tp.setCustomSampleSet(Integer.parseInt(tokenizer.nextToken()));
 			tp.setVolume(Integer.parseInt(tokenizer.nextToken()));
 			tp.setInheritance(Integer.parseInt(tokenizer.nextToken()) == 1 ? true : false);
 			tp.setKiai(Integer.parseInt(tokenizer.nextToken()) == 1 ? true : false);
 			
 			pc.timing_points.add(tp);
 		}
 		
 		return line;
 	}
 	
 	private String handleColours(BufferedReader reader, ParserContainer pc) throws IOException
 	{
 		// List-Based
 		String line;
 		while (true)
 		{
 			line = reader.readLine();
 			
 			if (lineCheck(line)) // Return at end of file or next header
 				break;
 			if (line.length() == 0) // Skip over blank lines
 				continue;
 			
 			if (!line.substring(0, 5).toLowerCase().equals("combo")) // Make sure we have a combo
 			{
 				printError("Parser.handleColours", "Unidentifiable line in \"Colours\" header - " + line);
 				continue;
 			}
 			
 			line = line.substring(line.indexOf(":") + 1); // Get the string past the colon
 			line = removeSpaces(line); // Remove the spaces
 			
 			// Get our colors
 			// ComboX : r, g, b
 			int r, g, b;
 			StringTokenizer tokenizer = new StringTokenizer(line, ",", false);
 			r = Integer.parseInt(tokenizer.nextToken());
 			g = Integer.parseInt(tokenizer.nextToken());
 			b = Integer.parseInt(tokenizer.nextToken());
 			
 			if (r < 0 || r > 255 ||
 					g < 0 || g > 255 ||
 					b < 0 || b > 255) // Bounds checking
 			{
 				printError("Parser.handleColours", "Color values are too large - " + r + ", " + b + ", " + g + ".");
 				continue;
 			}
 			
 			ComboColor cc = new ComboColor(r, g, b);
 			pc.combo_colors.add(cc);
 		}
 		
 		return line;
 	}
 	
 	private String handleHitObjects(BufferedReader reader, ParserContainer pc) throws IOException
 	{
 		// List-based
 		String line;
 		while (true)
 		{
 			line = reader.readLine();
 			
 			if (lineCheck(line)) // Null or next header, break
 				break;
 			if (line.length() == 0) // Skip empty lines
 				continue;
 			
 			// General Hit Object Information
 			line = removeSpaces(line);
 			
 			// x, y, timing, piece_type, sound_type, SPECIAL
 			StringTokenizer tokenizer = new StringTokenizer(line, ",", false);
 			int x = Integer.parseInt(tokenizer.nextToken());
 			int y = Integer.parseInt(tokenizer.nextToken());
 			long timing = Long.parseLong(tokenizer.nextToken());
 			int piece_type = Integer.parseInt(tokenizer.nextToken());
 			int sound_type = Integer.parseInt(tokenizer.nextToken());
 
 			// bitflags lol
 			// 1 = button
 			// 2 = slider
 			// 4 = new combo
 			// 8 = spinner
 			// 16 = ??, appears in krison's heavy tsugaru (1 + 4 + 16 = 21)
 			// 32 = ??, appears in krisom's heavy tsugaru (2 + 4 + 32 = 38)
 			
 			// Specific Hit Object Information
 			HitObject ho = null;
 			boolean newcombo = (piece_type & 4) != 0;
 			if ((piece_type & 1) != 0) // button!
 			{
 				ho = new HOButton(x, y, timing, newcombo, sound_type);
 			}
 			else if ((piece_type & 2) != 0) // slider!
 			{
 				ho = new HOSlider(x, y, timing, newcombo, sound_type);
 				
 				if (!handleSlider(ho, line, tokenizer))
 					continue;
 			}
 			else if ((piece_type & 8) != 0) // spinner!
 			{
 				ho = new HOSpinner(x, y, timing, newcombo, sound_type);
 				
 				if (!handleSpinner(ho, line, tokenizer))
 					continue;
 			}
 			else
 			{
 				Log.v("Parser.handleHitObjects", "Hit-object with type " + piece_type + " is not button, slider, or spinner!");
 				continue;
 			}
 			
 			pc.hit_objects.add(ho); // Add to ParserContainer list
 		}
 		
 		return line;
 	}
 	
 	
 	// Special-Case //
 	private String handleEvents(BufferedReader reader, ParserContainer pc) throws IOException
 	{
 		// Special case
 		String line = reader.readLine(); // NOTE: I never check to make sure that this line is an event header.
 		while (line.length() == 0)
 			line = reader.readLine(); // Skip all blank lines
 		
 		while (true)
 		{
 			line = line.toLowerCase(); // Get lower case!
 			
 			if (lineCheck(line)) // Null or next header, break
 				break;
 			if (line.length() == 0) // Skip empty lines
 				continue;
 			
 			if (line.equals("//background and video events"))
 			{
 				line = reader.readLine();
 				if (line.length() == 0 || line.charAt(0) != '0') // Error checking
 				{
 					if (!isEventHeader(line))
 						getNextEventHeader(reader);
 					continue;
 				}
 					
 				Background background = pc.background;
 				
 				// Get background image
 				StringTokenizer tokenizer = new StringTokenizer(line, ",", false);
 				background.setBveUn1(Float.parseFloat(tokenizer.nextToken()));
 				background.setBveUn2(Float.parseFloat(tokenizer.nextToken()));
 				
 				String filename = tokenizer.nextToken();
 				background.setImagePath(filename.substring(1, filename.lastIndexOf('"'))); // Remove quotations
 				
 				// Does not deal with videos
 			}
 			
 			else if (line.equals("//break periods"))
 			{
 				LinkedList<BreakTiming> break_timings = pc.break_timings;
 				
 				line = reader.readLine();
 				while (!isEventHeader(line))
 				{
 					if (line.length() == 0) // Error checking!
 						continue;
 					
 					StringTokenizer tokenizer = new StringTokenizer(line, ",", false);
 					int unknown = Integer.parseInt(tokenizer.nextToken());
 					long start_time = Long.parseLong(tokenizer.nextToken());
 					long end_time = Long.parseLong(tokenizer.nextToken());
 					
 					break_timings.add(new BreakTiming(unknown, start_time, end_time));
 					
 					line = reader.readLine(); // Next line
 				}
 			}
 			
 			else if (line.equals("//background colour transformations"))
 			{
 				line = reader.readLine();
 				if (line.length() == 0) // Error checking (be more thorough?)
 				{
 					if (!isEventHeader(line))
 						getNextEventHeader(reader);
 					continue;
 				}
 				
 				Background background = pc.background;
 				
 				// Get background transformation colors
 				StringTokenizer tokenizer = new StringTokenizer(line, ",", false);
 				background.setBctUn1(Float.parseFloat(tokenizer.nextToken()));
 				background.setBctUn2(Float.parseFloat(tokenizer.nextToken()));
 				
 				background.setR(Integer.parseInt(tokenizer.nextToken()));
 				background.setG(Integer.parseInt(tokenizer.nextToken()));
 				background.setB(Integer.parseInt(tokenizer.nextToken()));
 			}
 			
 			else // Unknown result
 				printError("Parser.handleEvents", "Unknown event header: " + line);
 			
 			// Get next event header (or null or section header)
 			line = getNextEventHeader(reader);
 		}
 		
 		return line;
 	}
 	
 	
 	// *** SUBFUNCTIONS *** //
 	private boolean handleSlider(HitObject ho, String line, StringTokenizer tokenizer) throws IOException
 	{
 		if (ho.getClass() != HOSlider.class)
 			return false;
 		
 		HOSlider slider = (HOSlider)ho;
 		
 		// Parse the remaining slider components:
 		// Points, repeats, path length, [sounds]
 		StringTokenizer points = new StringTokenizer(tokenizer.nextToken(), "|", false);
 		
 		// Get slider type
 		try {
 			String slider_type = points.nextToken();
 			if (slider_type.equals("B"))
 				slider.setSliderType(HOSliderType.BEZIER);
 			else if (slider_type.equals("L"))
 				slider.setSliderType(HOSliderType.LINEAR);
 			else if (slider_type.equals("C"))
 				slider.setSliderType(HOSliderType.CATMULL);
 			else
 			{
 				printError("Parser.handleSlider", "Unknown slider type: \"" + slider_type + "\".");
 				return false;
 			}
 		} catch (UnsupportedOperationException ex) {
 			printError("Parser.handleSlider", ex.toString());
 			return false;
 		}
 		
 		// Parse the points on the path
 		LinkedList<Point> path_points = slider.getPathPoints();
 		while (points.hasMoreTokens())
 		{
 			String part = points.nextToken();
 			int breakpoint = part.indexOf(":");
 			path_points.add(new Point(Integer.parseInt(part.substring(0, breakpoint)),
 					Integer.parseInt(part.substring(breakpoint + 1))));
 		}
 		
 		slider.setRepeats(Integer.parseInt(tokenizer.nextToken()));
 		slider.setPathLength(Float.parseFloat(tokenizer.nextToken()));
 		
 		// Parse the sounds (if they exist)
 		if (tokenizer.hasMoreTokens()) // Sound is not a required attribute
 		{
 			LinkedList<Integer> sound_points = slider.getSoundPoints();
 			
 			String sound_str = tokenizer.nextToken();
 			StringTokenizer sound_tokenizer = new StringTokenizer(sound_str, "|", false);
 			while (sound_tokenizer.hasMoreTokens())
 				sound_points.add(Integer.parseInt(sound_tokenizer.nextToken()));
 		}
 		
 		return true;
 	}
 	
 	
 	private boolean handleSpinner(HitObject ho, String line, StringTokenizer tokenizer) throws IOException
 	{
 		if (ho.getClass() != HOSpinner.class)
 			return false;
 		
 		HOSpinner spinner = (HOSpinner)ho;
 		
 		// Parse for end timing
 		spinner.setEndTiming(Long.parseLong(tokenizer.nextToken()));
 		
 		return true;
 	}
 	
 	
 	// *** HELPER METHODS *** //
 	/**
 	 * Checks if a line is null (EOF) or the next header in the list.
 	 */
 	private boolean lineCheck(String s) { return s == null || isHeader(s); }
 	
 	
 	/**
 	 * Returns true if the given int array contains the given int, false otherwise.
 	 */
 	private boolean intArrContains(int[] arr, int i)
 	{
 		int s = arr.length;
 		for (int x = 0; x < s; ++x)
 		{
 			if (arr[x] == i)
 				return true;
 		}
 		
 		return false;
 	}
 	
 	
 	/**
 	 * Returns true if the specified string is a section header.
 	 * Does not check if the given String is null beforehand.
 	 * 
 	 * @param s The String to check.
 	 * @return True if s is a section header, false otherwise.
 	 */
 	private boolean isHeader(String s)
 	{
 		if (s.length() == 0)
 			return false;
 		
 		if (s.charAt(0) == '[' && s.contains("]"))
 			return true;
 		
 		return false;
 	}
 	
 	
 	/**
 	 * Returns true if the specified string is an event header.
 	 * This is notated by the beginning "//". 
 	 * 
 	 * @param s The String to check
 	 * @return True if the header is an event header, false otherwise.
 	 */
 	private boolean isEventHeader(String s)
 	{
 		if (s.length() < 2)
 			return false;
 		
 		return s.charAt(0) == '/' && s.charAt(1) == '/';
 	}
 	
 	
 	/**
 	 * Reads lines from the BufferedReader until the next event header is found, the stream is null,
 	 * or a new section header file is found.
 	 * 
 	 * @param reader The BufferedReader we are reading from
 	 * @return The next event header, if applicable
 	 * @throws IOException If there is some Java IO error
 	 */
 	private String getNextEventHeader(BufferedReader reader) throws IOException
 	{
 		String line = reader.readLine();
 		while (line != null && !isEventHeader(line) && !isHeader(line))
 			line = reader.readLine();
 		
 		return line;
 	}
 	
 	
 	/**
 	 * Returns the header of the specified name, or null if it does not exist.
 	 */
 	private String getSpecificHeader(BufferedReader reader, String header_name) throws IOException
 	{
 		String name = header_name.toLowerCase();
 		
 		String line = removeSpaces(reader.readLine().toLowerCase());
 		while (line != null && !line.equals("[" + name + "]"))
		{
			line = reader.readLine();
			if (line == null)
				return line;
		
			line = removeSpaces(line.toLowerCase());
		}
 			
 		return line;
 	}
 	
 	
 	/**
 	 * Removes all spaces in the input String.
 	 * @param s The String to remove the spaces from.
 	 * @return The given String with no space characters.
 	 */
 	private String removeSpaces(String s)
 	{	
 		int space_ind = s.indexOf(' ');
 		while (space_ind >= 0)
 		{
 			if (space_ind != (s.length() - 1)) // Not out of bounds
 				s = s.substring(0, space_ind).concat(s.substring(space_ind + 1));
 			else // Out of bounds
 				s = s.substring(0, space_ind);
 			
 			space_ind = s.indexOf(' ');
 		}
 		
 		return s;
 	}
 
 	
 	/**
 	 * Removes all the first spaces from the input String (before the content begins).
 	 */
 	private String removeFirstSpaces(String s)
 	{
 		while (s.length() != 0 && s.charAt(0) == ' ')
 			s = s.substring(1);
 		
 		return s;
 	}
 	
 	
 	/**
 	 * Prints any non-thrown parse errors to the command line (Logcat and stdout).
 	 * @param tag The tag to put in (LogCat - used in stdout as start entry)
 	 * @param s The message to print
 	 */
 	private void printError(String tag, String s)
 	{
 		Log.e(tag, s);
 		System.err.println(tag + " - " + s);
 	}
 	
 }
