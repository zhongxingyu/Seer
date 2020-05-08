 package uk.ac.cam.groupproj.racethewild;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 public class XmlParser {
 	
 	/** Reads the XML file to produce a dictionary of animal data. */
 	public static Map<Integer, Animal> createDictionary(XmlPullParser parser) 
 			throws DictionaryReadException {
 		try {
 			// Skip the preamble.
 			parser.next();
 			// Check the <animaldictionary> tag.
 			parser.next();
 			if(!(parser.getName().equals("animaldictionary") && 
 					parser.getEventType() == XmlPullParser.START_TAG))
 				throw new DictionaryReadException(
 						"Failed to read opening <animaldictionary> tag.");
 			
 			Map<Integer, Animal> dictionary = new TreeMap<Integer, Animal>();
 			
 			// Loop and read animals.
 			while(true) {
 				// Read the opening <animal> tag.
 				parser.next();
 				if(parser.getEventType() == XmlPullParser.START_TAG &&
 						parser.getName().equals("animal")) {
 					// Read the animal data.
 					parser.next();
 					Animal nextAnimal = readNewAnimal(parser);
 					dictionary.put(nextAnimal.getID(), nextAnimal);
 					// Read the closing </animal> tag.
 					if(!(parser.getEventType() == XmlPullParser.END_TAG &&
 							parser.getName().equals("animal"))) throw new DictionaryReadException(
 									"Didn't find a proper closing tag for animal #" + 
 											nextAnimal.getID());
 				}
 				// If we're at the end, read the closing </animaldictionary> tag.
 				else if(parser.getEventType() == XmlPullParser.END_TAG &&
 						parser.getName().equals("animaldictionary")) break;
 				else throw new DictionaryReadException("Unexpected tag \"" + 
 						parser.getName() + "\" when looking for the next animal.");
 			}
 			return dictionary;
 			
 		} catch(XmlPullParserException e) {
 			e.printStackTrace();
 			throw new DictionaryReadException(
 					"Experienced an XmlPullParserException, see above trace");
 		} catch(IOException e) {
 			throw new DictionaryReadException("IOException when reading dictionary.");
 		}
 	}
 	
 	/** Parse XML animal data, given a parser pointing at the opening tag of the first data item. 
 	 * */
 	private static Animal readNewAnimal(XmlPullParser parser) 
 			throws IOException, XmlPullParserException, DictionaryReadException {
 		// Parse each data item in turn.
		int id = 0;
 		try {
 			id = Integer.parseInt(parseTag(parser, "id"));
 		} catch(NumberFormatException e) {
 			throw new DictionaryReadException(
					"Non-integer tag " + parser.getName() + " cannot be parsed.");
 		}
 		String name = parseTag(parser, "name");
 		String description = parseTag(parser, "description");
 		String hint = parseTag(parser, "hint");
 		String photo = parseTag(parser, "photo");
 		String sprite = parseTag(parser, "sprite");
		int distancePerDay = 0;
		try {
			distancePerDay = Integer.parseInt(parseTag(parser, "distance"));
		} catch(NumberFormatException e) {
			throw new DictionaryReadException(
					"Non-integer tag " + parser.getName() + " cannot be parsed.");
		}
 		List<String> nodes = parseSequence(parser, "node");
 		// And return a new Animal object containing the extracted data.
 		return new Animal(id, name, description, hint, photo, sprite, distancePerDay, nodes);
 	}
 	
 	/** Parse a single data item with the given tag, assuming the parser points to the opening tag
 	 *  of that data item. */
 	private static String parseTag(XmlPullParser parser, String tag) 
 			throws IOException, XmlPullParserException, DictionaryReadException {
 		// Check that the opening tag is correct.
 		if(!parser.getName().equals(tag)) throw new DictionaryReadException(
 				"Expected tag \"" + tag + "\", found \"" + parser.getName() + "\".");
 		if(parser.getEventType() != XmlPullParser.START_TAG) throw new DictionaryReadException(
 				"Tag \"" + tag + "\" wasn't a start tag!");
 		// Get the actual data.
 		parser.next();
 		String text = parser.getText();
 		// Check that the closing tag is correct.
 		parser.next();
 		if(!parser.getName().equals(tag)) throw new DictionaryReadException(
 				"Expected tag \"" + tag + "\", found \"" + parser.getName() + "\".");
 		if(parser.getEventType() != XmlPullParser.END_TAG) throw new DictionaryReadException(
 				"Tag \"" + tag + "\" wasn't an end tag!");
 		parser.next();
 		// And return the extracted data.
 		return text;
 	}
 	
 	/** Parses a variable-length (possibly empty) sequence of the same tag. */
 	private static List<String> parseSequence(XmlPullParser parser, String tag) 
 			throws XmlPullParserException, IOException, DictionaryReadException {
 		List<String> list = new ArrayList<String>();
 		// Loop as long as there's another matching tag.
 		while(parser.getName().equals(tag)) {
 			// Extract the content.
 			parser.next();
 			list.add(parser.getText());
 			// Check the closing tag.
 			parser.next();
 			if (!(parser.getEventType() == XmlPullParser.END_TAG &&
 					parser.getName().equals(tag))) throw new DictionaryReadException(
 							"Expected closing tag </" + tag + ">, found tag \"" + 
 									parser.getName() + "\".");
 			// Step onto the next tag.
 			parser.next();
 		}
 		// Return the list of extracted strings.
 		return list;
 	}
 	
 }
