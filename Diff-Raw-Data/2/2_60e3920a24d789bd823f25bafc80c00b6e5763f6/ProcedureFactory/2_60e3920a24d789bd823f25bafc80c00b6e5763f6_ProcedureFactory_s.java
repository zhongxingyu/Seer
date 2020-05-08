 package edu.cmu.hcii.novo.kadarbra;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.util.Log;
 import android.util.Xml;
 import edu.cmu.hcii.novo.kadarbra.structure.Callout;
 import edu.cmu.hcii.novo.kadarbra.structure.Callout.CType;
 import edu.cmu.hcii.novo.kadarbra.structure.Cycle;
 import edu.cmu.hcii.novo.kadarbra.structure.CycleNote;
 import edu.cmu.hcii.novo.kadarbra.structure.ExecNote;
 import edu.cmu.hcii.novo.kadarbra.structure.Procedure;
 import edu.cmu.hcii.novo.kadarbra.structure.ProcedureItem;
 import edu.cmu.hcii.novo.kadarbra.structure.Reference;
 import edu.cmu.hcii.novo.kadarbra.structure.Reference.RType;
 import edu.cmu.hcii.novo.kadarbra.structure.Step;
 import edu.cmu.hcii.novo.kadarbra.structure.StowageItem;
 
 public class ProcedureFactory {
 	private static final String TAG = "ProcedureFactory";
 	
 	// We don't use namespaces
     private static final String ns = null;
 	
     
     
     /**
      * Generate a list of procedure objects based off of the 
      * xml definitions in the ___ directory.
      * 
      * @return a list of procedure objects
      */
     public static List<Procedure> getProcedures(Context ctx) {
     	Log.d(TAG, "Getting procedures from xml");
     	
     	List<Procedure> results = new ArrayList<Procedure>();
     	
 		try {
 			//yup
 			AssetManager assMan = ctx.getAssets();
 			String[] procs = assMan.list("procedures");
 			
 			Log.v(TAG, procs.length + " file(s) to parse");
 			
 			for (String proc : procs) {
 				if (proc.endsWith(".xml")) {
 		    		try {
 		    			Log.d(TAG, "Parsing " + proc);
 	
 		    			InputStream in = assMan.open("procedures/" + proc);
 		    			results.add(getProcedure(in));
 		    		} catch (Exception e) {
 		    			Log.e(TAG, "Error parsing procedure", e);
 		    		}
 				}
 	    	}
 			
 		} catch (IOException e) {
 			Log.e(TAG, "Error gathering asset files", e);
 		}
     	
     	return results;
     }
     
     
     
     /**
      * Get a procedure object based off of the given input stream.
      * Sets up the parser then calls a different parse method.
      * 
      * @param in the input stream to parse
      * @return a procedure object
      * @throws XmlPullParserException
      * @throws IOException
      */
 	public static Procedure getProcedure(InputStream in) throws XmlPullParserException, IOException {		
 		Log.v(TAG, "Initializing parser");
 		
 		try {			
             XmlPullParser parser = Xml.newPullParser();
             parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
             parser.setInput(in, null);
             parser.nextTag();
             return readProcedure(parser);
         } finally {
             in.close();
         }
 	}
 	
 	
 	
 	/**
 	 * Parse an xml document to create a procedure object.
 	 * 
 	 * TODO: these methods could definitely be more flexible, maybe keep a 
 	 * map of the corresponding tags?
 	 * 
 	 * @param parser the xml to parse
 	 * @return the resulting procedure object
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private static Procedure readProcedure(XmlPullParser parser) throws XmlPullParserException, IOException {
 		Log.d(TAG, "Parsing procedure");
 		
 		String section = null;
 		String title = null;
 		String objective = null;
 		String duration = null;	
 		List<ExecNote> execNotes = null;
 		Map<String, List<StowageItem>> stowageItems = null;
 		List<ProcedureItem> children = null;		
 		
 		//This is the tag we are looking for
 	    parser.require(XmlPullParser.START_TAG, ns, "procedure");
 	    
 	    //Until we get to the closing tag
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	        if (parser.getEventType() != XmlPullParser.START_TAG) {
 	            continue;
 	        }
 	        String tag = parser.getName();
 	       
 	        //Get the attributes
 	        if (tag.equals("section")) {
 	        	section = readTag(parser, tag);
 	        	
 	        } else if (tag.equals("subsection")) {
 	        	section += "." + readTag(parser, tag);
 	        	
 	        } else if (tag.equals("sub_subsection")) {
 	        	section += "." +  readTag(parser, tag);
 	        	
 	        } else if (tag.equals("title")) {
 	        	title = readTag(parser, tag);
 	        	
 	        } else if (tag.equals("objective")) {
 	        	objective = readTag(parser, tag);
 	        	
 	        } else if (tag.equals("duration")) {
 	        	duration = readTag(parser, tag);
 	        	
 	        } else if (tag.equals("execution_notes")) {
 	        	execNotes = readExecNotes(parser);
 	        	
 	        } else if (tag.equals("stowage_notes")) {
 	        	stowageItems = readStowageNotes(parser);
 	        	
 	    	} else if (tag.equals("steps")) {
 	            children = readSteps(parser);
 	            
 	        } else {
 	            skip(parser);
 	        }
 	    }  
 	    return new Procedure(section, title, objective, duration, execNotes, stowageItems, children);
 	}
 	
 	
 	
 	/**
 	 * Parse the xml for a list of overall execution notes
 	 * 
 	 * @param parser the xml to parse
 	 * @return the resulting list of execution notes
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private static List<ExecNote> readExecNotes(XmlPullParser parser) throws XmlPullParserException, IOException {
 		Log.d(TAG, "Parsing execution notes");
 		
 		List<ExecNote> execNotes = new ArrayList<ExecNote>();
 	    
 	    //This is the tag we are looking for
   		parser.require(XmlPullParser.START_TAG, ns, "execution_notes");
 	    
 	    //Until we get to the closing tag
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	        if (parser.getEventType() != XmlPullParser.START_TAG) {
 	            continue;
 	        }
 	        String tag = parser.getName();
 	        
 	        //Get the attributes
 	        if (tag.equals("note")) {
 	        	execNotes.add(readExecNote(parser));
 	        	
 	        } else {
 	            skip(parser);
 	        }
 	    }
 	    return execNotes;
 	}
 	
 	
 	
 	/**
 	 * Parse xml for an execution note
 	 * 
 	 * @param parser the xml to parse
 	 * @return the resulting execution note
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private static ExecNote readExecNote(XmlPullParser parser) throws XmlPullParserException, IOException {
 		Log.d(TAG, "Parsing execution note");
 		
 		String step = null;
 		String text = null;
 	    
 	    //This is the tag we are looking for
   		parser.require(XmlPullParser.START_TAG, ns, "note");
 	    
 	    //Until we get to the closing tag
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	        if (parser.getEventType() != XmlPullParser.START_TAG) {
 	            continue;
 	        }
 	        String tag = parser.getName();
 	        
 	        //Get the attributes
 	        if (tag.equals("step")) {
 	        	step = readTag(parser, tag);
 	        	
 	        } else if (tag.equals("substep")) {
 	        	step += "." + readTag(parser, tag);
 	        	
 	        } else if (tag.equals("text")) {
 	        	text = readTag(parser, tag);
 	        	
 	        } else {
 	            skip(parser);
 	        }
 	    }
 	    return new ExecNote(step, text);
 	}
 	
 	
 	
 	/**
 	 * Parse the xml for a list of overall stowage items
 	 * 
 	 * @param parser the xml to parse
 	 * @return the resulting list of stowage items
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private static Map<String, List<StowageItem>> readStowageNotes(XmlPullParser parser) throws XmlPullParserException, IOException {
 		Log.d(TAG, "Parsing stowage notes");
 		
 		Map<String, List<StowageItem>> stowageItems = new HashMap<String, List<StowageItem>>();
 	    
 	    //This is the tag we are looking for
   		parser.require(XmlPullParser.START_TAG, ns, "stowage_notes");
 	    
 	    //Until we get to the closing tag
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	        if (parser.getEventType() != XmlPullParser.START_TAG) {
 	            continue;
 	        }
 	        String tag = parser.getName();
 	        
 	        //Get the attributes
 	        if (tag.equals("item")) {
 	        	StowageItem newItem = readStowageItem(parser);
 	        	String module = newItem.getModule();
 	        	
 	        	if (!stowageItems.containsKey(module)) {
 	        		stowageItems.put(module, new ArrayList<StowageItem>());
 	        	}
 	        	
 	        	stowageItems.get(module).add(newItem);
 	        } else {
 	            skip(parser);
 	        }
 	    }
 	    return stowageItems;
 	}
 	
 	
 	
 	/**
 	 * Parse xml for an execution note
 	 * 
 	 * @param parser the xml to parse
 	 * @return the resultin execution note
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private static StowageItem readStowageItem(XmlPullParser parser) throws XmlPullParserException, IOException {
 		Log.v(TAG, "Parsing stowage item");
 		
 		String module = null;
 		String name = null;
 		int quantity = 0;
 		String itemCode = null;
 		String binCode = null;
 		String text = null;
 		String url = null;
 	    
 	    //This is the tag we are looking for
   		parser.require(XmlPullParser.START_TAG, ns, "item");
 	    
 	    //Until we get to the closing tag
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	        if (parser.getEventType() != XmlPullParser.START_TAG) {
 	            continue;
 	        }
 	        String tag = parser.getName();
 	        
 	        //Get the attributes
 	        if (tag.equals("module")) {
 	        	module = readTag(parser, tag);
 	        	
 	        } else if (tag.equals("name")) {
 	        	name = readTag(parser, tag);
 	        	
 	        } else if (tag.equals("quantity")) {
 	        	quantity = Integer.parseInt(readTag(parser, tag));
 	        	
 	        } else if (tag.equals("item_code")) {
 	        	itemCode = readTag(parser, tag);
 	        	
 	        } else if (tag.equals("bin_code")) {
 	        	binCode = readTag(parser, tag);
 	        	
 	        } else if (tag.equals("text")) {
 	        	text = readTag(parser, tag);
 	        	
 	        } else if (tag.equals("url")) {
 	        	url = readTag(parser, tag);
 	        	
 	        } else {
 	            skip(parser);
 	        }
 	    }
 	    return new StowageItem(module, name, quantity, itemCode, binCode, text, url);
 	}
 	
 	
 	
 	/**
 	 * Parse the xml for the list of overall procedure steps.
 	 * 
 	 * 
 	 * @param parser the xml to parse
 	 * @return a list of steps
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private static List<ProcedureItem> readSteps(XmlPullParser parser) throws XmlPullParserException, IOException {
 		Log.d(TAG, "Parsing steps");
 		
 	    List<ProcedureItem> steps = new ArrayList<ProcedureItem>();
 	    
 	    //This is the tag we are looking for
   		parser.require(XmlPullParser.START_TAG, ns, "steps");
 	    
 	    //Until we get to the closing tag
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	        if (parser.getEventType() != XmlPullParser.START_TAG) {
 	            continue;
 	        }
 	        String tag = parser.getName();
 	        
 	        //Get the attributes
 	        if (tag.equals("step")) {
 	        	steps.add(readStep(parser));
 	        	
 	        } else if (tag.equals("cycle")) {
 	        	steps.add(readCycle(parser));
 	        	
 	        } else {
 	            skip(parser);
 	        }
 	    }
 	    return steps;
 	}
 	
 	
 	
 	/**
 	 * Parse an xml document to create a parent step.
 	 * 
 	 * @param parser the xml to parse
 	 * @return a step object
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private static Step readStep(XmlPullParser parser) throws XmlPullParserException, IOException {
 		Log.d(TAG, "Parsing step");
 		
 		String number = null;
 	    String text = null;
 	    String consequent = null;
 	    List<Callout> callouts = new ArrayList<Callout>();
 	    List<ProcedureItem> children = new ArrayList<ProcedureItem>();
 	    List<Reference> references = new ArrayList<Reference>();
 	    boolean timer = false;
 	    boolean inputAllowed = false;
 	    
 	    //This is the tag we are looking for
   		parser.require(XmlPullParser.START_TAG, ns, "step");
 	    
 	    //Until we get to the closing tag
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	        if (parser.getEventType() != XmlPullParser.START_TAG) {
 	            continue;
 	        }
 	        String tag = parser.getName();
 	        
 	        //Get the attributes
 	        if (tag.equals("number")) {
 	            number = readTag(parser, tag);
 	        
 	        } else if (tag.equals("consequent")) {
 	            consequent = readTag(parser, tag);
 	            
 	        } else if (tag.equals("text")) {
 	            text = readTag(parser, tag);
 	            
 	        } else if (tag.equals("callout")) {
 	            callouts.add(readCallout(parser));
 	            
 	        } else if (tag.equals("reference")) {
 	            references.add(readReference(parser));
 	            
 	        } else if (tag.equals("step")) {
 	            children.add(readStep(parser));
 	            
 	        } else if (tag.equals("cycle")) {
 	            children.add(readCycle(parser));
 	            
 	        } else if (tag.equals("timer")){
	        	timer = true;
 	        	
 	        } else if (tag.equals("input")) {
 	            inputAllowed = Boolean.parseBoolean(readTag(parser, tag));
 	            
 	        } else {
 	            skip(parser);
 	        }
 	    }
 	    
 	    Step result = new Step(number, text, callouts, references, children, timer, inputAllowed);
 	    if (consequent != null) result.setConsequent(consequent);
 	
 	    return result;
 	}
 	
 	
 	
 	/**
 	 * Parse and xml document to create a new Callout object.
 	 * 
 	 * @param parser the xml to parse
 	 * @return the resulting Callout object
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private static Callout readCallout(XmlPullParser parser) throws XmlPullParserException, IOException {
 		Log.d(TAG, "Parsing callout");
 		
 		CType type = null;
 	    String text = null;
 	    
 	    //This is the tag we are looking for
   		parser.require(XmlPullParser.START_TAG, ns, "callout");
 	    
 	    //Until we get to the closing tag
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	        if (parser.getEventType() != XmlPullParser.START_TAG) {
 	            continue;
 	        }
 	        String tag = parser.getName();
 	        
 	        //Get the attributes
 	        if (tag.equals("type")) {
 	            String t = readTag(parser, tag);
 
 	            if (t.equals("note")) {
 	            	type = CType.NOTE;
 	            	
 	            } else if (t.equals("warning")) {
 	            	type = CType.WARNING;
 	            	
 	            } else if (t.equals("caution")) {
 	            	type = CType.CAUTION;
 	            	
 	            }
 	        
 	        } else if (tag.equals("text")) {
 	            text = readTag(parser, tag);
 	            
 	        } else {
 	            skip(parser);
 	        }
 	    }
 	    return new Callout(type, text);
 	}
 	
 	
 	
 	/**
 	 * Parse and xml document to create a new Callout object.
 	 * 
 	 * @param parser the xml to parse
 	 * @return the resulting Callout object
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private static Reference readReference(XmlPullParser parser) throws XmlPullParserException, IOException {
 		Log.d(TAG, "Parsing reference");
 		
 		RType type = null;
 	    String name = null;
 	    String description = null;
 	    String url = null;
 	    List<List<String>> table = new ArrayList<List<String>>();
 	    
 	    //This is the tag we are looking for
   		parser.require(XmlPullParser.START_TAG, ns, "reference");
 	    
 	    //Until we get to the closing tag
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	        if (parser.getEventType() != XmlPullParser.START_TAG) {
 	            continue;
 	        }
 	        String tag = parser.getName();
 	        
 	        //Get the attributes
 	        if (tag.equals("type")) {
 	            String t = readTag(parser, tag);
 
 	            if (t.equals("image")) {
 	            	type = RType.IMAGE;
 	            	
 	            } else if (t.equals("video")) {
 	            	type = RType.VIDEO;
 	            	
 	            } else if (t.equals("audio")) {
 	            	type = RType.AUDIO;
 	            	
 	            } else if (t.equals("table")) {
 	            	type = RType.TABLE;
 	            	
 	            }
 	        
 	        } else if (tag.equals("name")) {
 	            name = readTag(parser, tag);
 	            
 	        } else if (tag.equals("description")) {
 	            description = readTag(parser, tag);
 	            
 	        } else if (tag.equals("url")) {
 	            url = readTag(parser, tag);
 	            
 	        } else if (tag.equals("table")) {
 	            table = readTable(parser);
 	            
 	        } else {
 	            skip(parser);
 	        }
 	    }
 	    return new Reference(type, name, description, url, table);
 	}
 	
 	
 	
 	/**
 	 * Parse and xml document to create a new cycle of steps.  A cycle is 
 	 * really just a linear list of the same steps being repeated.
 	 * 
 	 * @param parser the xml to parse
 	 * @return the resulting list of steps
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private static Cycle readCycle(XmlPullParser parser) throws XmlPullParserException, IOException {
 		Log.d(TAG, "Parsing cycle");
 		
 		int repetitions = 0;
 		List<CycleNote> notes = new ArrayList<CycleNote>();
 	    List<ProcedureItem> children = new ArrayList<ProcedureItem>();
 	    
 	    //This is the tag we are looking for
   		parser.require(XmlPullParser.START_TAG, ns, "cycle");
 	    
 	    //Until we get to the closing tag
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	        if (parser.getEventType() != XmlPullParser.START_TAG) {
 	            continue;
 	        }
 	        String tag = parser.getName();
 	        
 	        //Get the attributes
 	        if (tag.equals("repetitions")) {
 	        	repetitions = Integer.parseInt(readTag(parser, tag));
 	        	
 	        } else if (tag.equals("step")) {
 	        	children.add(readStep(parser));
 	        	
 	        } else if (tag.equals("cycle")) {
 	        	children.add(readCycle(parser));
 	        	
 	        } else if (tag.equals("cycle_notes")) {
 	        	notes = readCycleNotes(parser);
 	        	
 	        } else {
 	            skip(parser);
 	        }
 	    }
 	    
 	    return new Cycle(repetitions, notes, children);
 	}
 	
 	
 	
 	/**
 	 * Parse xml to produce a list of cycle notes
 	 * 
 	 * @param parser
 	 * @return
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private static List<CycleNote> readCycleNotes(XmlPullParser parser) throws XmlPullParserException, IOException {
 		Log.d(TAG, "Parsing cycle notes");
 		
 		List<CycleNote> notes = new ArrayList<CycleNote>();
 	    
 	    //This is the tag we are looking for
   		parser.require(XmlPullParser.START_TAG, ns, "cycle_notes");
 	    
 	    //Until we get to the closing tag
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	        if (parser.getEventType() != XmlPullParser.START_TAG) {
 	            continue;
 	        }
 	        String tag = parser.getName();
 	        
 	        //Get the attributes
 	        if (tag.equals("note")) {
 	        	notes.add(readCycleNote(parser));
 	        	
 	        } else {
 	            skip(parser);
 	        }
 	    }
 	    
 	    return notes;
 	}
 	
 	
 	
 	/**
 	 * Parse xml to produce a cycle note object 
 	 * @param parser
 	 * @return
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private static CycleNote readCycleNote(XmlPullParser parser) throws XmlPullParserException, IOException {
 		Log.v(TAG, "Parsing cycle note");
 		
 		String text = null;
 		Reference ref = null;
 	    
 	    //This is the tag we are looking for
   		parser.require(XmlPullParser.START_TAG, ns, "note");
 	    
 	    //Until we get to the closing tag
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	        if (parser.getEventType() != XmlPullParser.START_TAG) {
 	            continue;
 	        }
 	        String tag = parser.getName();
 	        
 	        //Get the attributes
 	        if (tag.equals("text")) {
 	        	text = readTag(parser, tag);
 	        	
 	        } else if (tag.equals("reference")) {
 	        	ref = readReference(parser);
 	        	
 	        } else {
 	            skip(parser);
 	        }
 	    }
 	    
 	    return new CycleNote(text, ref);
 	}
 	
 	
 	
 	/**
 	 * Read the xml to produce a table object.  Its a list of lists of strings 
 	 * defining rows and columns.
 	 * 
 	 * TODO this really should be a 2d array
 	 * 
 	 * @param parser
 	 * @return
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private static List<List<String>> readTable(XmlPullParser parser) throws XmlPullParserException, IOException {
 		Log.d(TAG, "Parsing table");
 		
 	    List<List<String>> cells = new ArrayList<List<String>>();
 	    
 	    //This is the tag we are looking for
   		parser.require(XmlPullParser.START_TAG, ns, "table");
 	    
 	    //Until we get to the closing tag
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	        if (parser.getEventType() != XmlPullParser.START_TAG) {
 	            continue;
 	        }
 	        String tag = parser.getName();
 	        
 	        //Get the attributes
 	        if (tag.equals("row")) {
 	        	cells.add(readRow(parser));
 	        	
 	        } else {
 	            skip(parser);
 	        }
 	    }
 	    
 	    return cells;
 	}
 	
 	
 	
 	private static List<String> readRow(XmlPullParser parser) throws XmlPullParserException, IOException {
 		Log.d(TAG, "Parsing row");
 		
 	    List<String> row = new ArrayList<String>();
 	    
 	    //This is the tag we are looking for
   		parser.require(XmlPullParser.START_TAG, ns, "row");
 	    
 	    //Until we get to the closing tag
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	        if (parser.getEventType() != XmlPullParser.START_TAG) {
 	            continue;
 	        }
 	        String tag = parser.getName();
 	        
 	        //Get the attributes
 	        if (tag.equals("cell")) {
 	        	row.add(readTag(parser, tag));
 	        	
 	        } else {
 	            skip(parser);
 	        }
 	    }
 	    
 	    //row.toArray(new String[row.size()]);
 	    return row;
 	}
 	
 	
 	
 	/**
 	 * Read the text value of the given tag from the xml.
 	 * 
 	 * @param parser the xml to parse
 	 * @param tag the tag to look for
 	 * @return the string value of the tag
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private static String readTag(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
 		Log.v(TAG, "Reading tag " + tag);
 		
 		parser.require(XmlPullParser.START_TAG, ns, tag);
 	    String result = readTextValue(parser);
 	    parser.require(XmlPullParser.END_TAG, ns, tag);
 	    return result;
 	}
 	
 	
 	
 	/**
 	 * Extracts a tag's textual value.
 	 * 
 	 * @param parser the xml to parse
 	 * @return the string value contained within a tag
 	 * @throws IOException
 	 * @throws XmlPullParserException
 	 */
 	private static String readTextValue(XmlPullParser parser) throws IOException, XmlPullParserException {
 	    String result = "";
 	    if (parser.next() == XmlPullParser.TEXT) {
 	        result = parser.getText();
 	        parser.nextTag();
 	    }
 	    return result;
 	}
 	
 	
 	
 	/**
 	 * Skip the tag currently pointed to by the parser.  This lets us 
 	 * ignore any tags we don't currently care about.
 	 * 
 	 * @param parser the xml to parse
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
 	    if (parser.getEventType() != XmlPullParser.START_TAG) {
 	        throw new IllegalStateException();
 	    }
 	    
 	    //Move through the xml until we get to the close
 	    //of the current tag.
 	    int depth = 1;
 	    while (depth != 0) {
 	        switch (parser.next()) {
 	        case XmlPullParser.END_TAG:
 	            depth--;
 	            break;
 	        case XmlPullParser.START_TAG:
 	            depth++;
 	            break;
 	        }
 	    }
 	 }
 }
