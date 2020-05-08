 /**
  * This is the tag class of the wpv program that is responsible
  * for checking and validating tags found in the given input file(s). 
  * Any kind of tag reading and validation operation in wpv should be 
  * defined here.
  *
  * Author: Tyler Vodak
  * Date: 01/16/2012
  */
 
 import java.util.*;
  
 public class Tags {
 
 	//These are the variables of a Tags object
 	FileUtil fu_data = null;
 	ArrayList<ResultSet> r_set = new ArrayList<ResultSet>();
 	Stack<StackPair<String,Integer>> openTags = new Stack<StackPair<String,Integer>>();
 	TagConfig n_config = null;
 	
 	//These are class variables
 	private static final char OPEN_ARROW = '<';
 	private static final char CLOSE_ARROW = '>';
 	private static final short TRUE_EXCEPTION = 255;	//This error makes the program exit
 
 	//Tags works by reading in data from each file and keeping
 	//track of all the found tag data. So the constructor will
 	//need a FileUtil object that it can use to keep reading
 	//the next line of data.
 	public Tags (FileUtil f_u) {
 		fu_data = f_u;
  		n_config = new TagConfig(new Vector<String>());
 	}
 	
 	/* This is the starting function for the Tags class. Calling
 	 * this function will read the data from all the web files
 	 * and keep track of any errors or warnings.
 	 *
 	 * Returns => Status code indicating of all data was read
 	 *			  successfully or not
 	 *			0 => Everything is OK
 	 *			>0 => Something went wrong
 	 */
 	public int validate_tags() {
 		//Local variables
 		int res = 0;
 		String n_file = null;
 		String buf = null;
 		int lineNum = 0;
 		
 		//First, let's try to open the FileUtil object
 		res = fu_data.openSource();
 		
 		if(res > 0)
 			return res;
 			
 		//Now, iterate through each file
 		n_file = fu_data.getNextFile();
 		while(null != n_file) {
 			//Keep reading until the end
 			buf = fu_data.getNextLine(n_file);
 			while(null != buf) {
 				lineNum = lineNum + buf.split("\r\n|\r|\n").length;
 				validate_tag_line(n_file,lineNum,buf);
 				buf = fu_data.getNextLine(n_file);
 			}
 			n_file = fu_data.getNextFile();
 			lineNum = 0;
 		}
 		
 		return res;
 	}
 	
 	/* This is the function that will validate each line for
 	 * tags. The idea is to keep track of all data in a stack
 	 * like structure so we know if any tags are missing or
 	 * not complete
 	 *
 	 * Returns => A status code indicating if everything was
 	 * 			  OK or not
 	 *			0 =>  Everything is fine
 	 *			>0 => Everythign is not fine
 	 */
 	 private int validate_tag_line(String f_name, int lineNum, String line) {
 	 	//Local variables should go here
 	 	
 	 	//If the given string is empty, just return 0
 	 	if(null == line || line.isEmpty()) {
 	 		return 0;
 	 	}
 	 	
 	 	//This is the part where we actually look for tag data.
 	 	//The plan is to identify if a string is a "tag" based
 	 	//on data in some configuration file and then add/remove
 	 	//this data from a stack structure as tag data is encountered
 	 	
 	 	String n_tag = "";
 	 	boolean add_chars = false;
 	 	int i = 0;
 	 	for(i = 0; i < line.length(); i++) {
 	 		switch(line.charAt(i)) {
 	 			case OPEN_ARROW:
 	 				add_chars = true;
 	 				break;
 	 			case CLOSE_ARROW:
 	 				n_tag = n_tag  + line.charAt(i);
 	 				//if we find a CLOSE_TAG with no OPEN_TAG == ERROR!
 	 				if(false == add_chars) {
 	 					addResult(f_name,lineNum,line,
 	 							ResultSet.Message.MISSING_OPEN_ARROW,i);
 	 				}
 	 				add_chars = false;
 	 				//call tag function here
 	 				validate_tag(n_tag,f_name,lineNum);
 	 				n_tag = "";
 	 				break;
 	 		}
 	 		if(true == add_chars) {
 	 			n_tag = n_tag + line.charAt(i);
 	 		}
 	 	}
 	 	//add_chars should be false at this point. If not == ERROR!
 	 	if(true == add_chars) {
 	 		addResult(f_name,lineNum,line,
 	 							ResultSet.Message.MISSING_CLOSE_ARROW,i);
 	 	}
 	 	
 	 	return 0;
 	 }
 	 
 	 /*
 	  * This is the function where we really validate the tag. It's where
 	  * we determine if the tag is open or closed, and then process the 
 	  * tag and mark any errors or warnings as necessary.
 	  *
 	  * Returns => nothing; will add errors and warnings to result container
 	  */
 	  private void validate_tag(String tag, String f_name, int lineNum) {
 	  	//First, we need to determine if the given tag is an open or close tag
 	  	//if open, go one way. if closed, go the other way
 	  	int pos = 1;
 	  	boolean open_tag = false;
 	  	while(pos < tag.length() - 1) {
 	  		if(tag.charAt(pos) == '/') {
 	  			process_close_tag(tag,f_name,lineNum,pos); 
 	  			break; //this is a close tag
 	  		}
 	  		if(!Character.isWhitespace(tag.charAt(pos))) {
 	  			open_tag = true; //this is an open tag
 	  			break;
 	  		}
 	  		pos++;
 	  	}
 	  	
 	  	//If the tag is determined to be an open tag
 	  	if(true == open_tag) {
 	  		process_open_tag(tag,f_name,lineNum);
 	  	}
 	  }
 	  
 	/*
 	 * This function processes all open tags, first by validating
 	 * if the tag name is valid, if the attributes are valid,
 	 * and then adds it to the list of open tags
 	 * 
 	 * Returns => true if everything is OK, false if not
 	 */
 	 private boolean process_open_tag(String tag,String f_name,int lineNum) {
 	 	//First, we need to get the actual name of this tag
 	 	int pos = 1;
 	 	String tag_name = "";
 	 	//Move position to the first non-whitespace
 	 	while(Character.isWhitespace(tag.charAt(pos))) {
 	 		pos++;
 	 	}
 	 	//Check if this line is a comment line
 	 	if('!' == tag.charAt(pos)) {
 	 		if(tag.substring(pos + 1, pos + 3).equals("--")) {
 	 			return true;
 	 		}
 	 	}
 	 	//Now, get the actual name
 	 	while(!Character.isWhitespace(tag.charAt(pos)) && 
 	 										CLOSE_ARROW != tag.charAt(pos)) {
 	 		tag_name = tag_name + tag.charAt(pos);
 	 		pos++;
 	 	}
 	 	//Add the tag_name to the stack
 	 	if(!addTagToStack(tag_name,lineNum)) {
 	 		addResult(f_name,lineNum,tag,ResultSet.Message.INVALID_OPEN_TAG,pos);
 	 		return false;
 	 	}
 	 	//Now, pick apart the attributes and their values
 	 	if(CLOSE_ARROW == tag.charAt(pos)) {
 	 		//if we've already hit the close arrow, then we're done
 	 		return true;
 	 	}
 	 	else {
 	 		//Get the attributes and their corresponding values
 	 		String attr = ""; String value = ""; boolean is_attr = true;
 	 		boolean is_value = true; boolean adding_attr = false;
 	 		Vector<String> prev_attrs = new Vector<String>();
 	 		while(CLOSE_ARROW != tag.charAt(pos)) {
 	 			if(Character.isWhitespace(tag.charAt(pos))) {
 	 				if (false == is_value) {
 	 					value = value + tag.charAt(pos);
 	 				}
 	 				if (true == adding_attr) {
 	 					if(!Character.isWhitespace(tag.charAt(pos + 1)) && 
 	 						'=' != tag.charAt(pos + 1)) {
 	 						addResult(f_name,lineNum,tag,ResultSet.Message.INVALID_ATTR,pos);
 	 						return false;
 	 					}
 	 				}
 	 				pos++;
 	 				continue;
 	 			}
 	 			if(true == is_attr && '=' != tag.charAt(pos)) {
 	 				attr = attr + tag.charAt(pos);
 	 				adding_attr = true;
 	 				pos++;
 	 				continue;
 	 			}
 	 			else if(true == is_attr) {
 	 				is_attr = false;
 	 				adding_attr = false;
 	 				pos++;
 	 				continue;
 	 			}
 	 			
 	 			if(true == is_value && '"' != tag.charAt(pos)) {
 	 				addResult(f_name,lineNum,tag,ResultSet.Message.NO_OPEN_PAREN,pos);
 	 				return false;
 	 			}
 	 			else if (true == is_value) {
 	 				is_value = false;
 	 				pos++;
 	 				continue;
 	 			}
 	 			if('"' != tag.charAt(pos)) {
 	 				value = value + tag.charAt(pos);
 	 				if(tag.charAt(pos + 1) == CLOSE_ARROW) {
 	 					pos++;
 	 					addResult(f_name,lineNum,tag,ResultSet.Message.NO_CLOSE_PAREN,pos);
 	 					return false;
 	 				}
 	 				pos++;
 	 			}
 	 			else {
 	 				is_attr = true;
 	 				is_value = true;
 	 				pos++;
 	 				//First, get the container of attr/val pairs
 	 				HashMap<String,String> attr_val = n_config.getAttrVals(tag_name);
 	 				if(null == attr_val) {
 	 					System.out.println("ERROR - The open tag [" + tag_name + "] returns null");
  	  					System.exit(TRUE_EXCEPTION);
 	 				}
 	 				//Now, check the attr and value
 	 				attr = attr.toLowerCase();
 	 				if(!attr_val.containsKey(attr)) {
 	 					addResult(f_name,lineNum,tag,ResultSet.Message.INVALID_ATTR,pos);
 	 				}
 	 				else if(prev_attrs.contains(attr)) {
 	 					addResult(f_name,lineNum,tag,ResultSet.Message.DUPLICATE_ATTR,pos);
 	 				}
 	 				else {
 	 					prev_attrs.add(attr.trim().toLowerCase());
 	 				}
 	 				attr = ""; value = "";
 	 			}
 	 		}
 	 		Vector <String> req_attrs = n_config.getReqAttrs(tag_name);
	 		//if req_attrs is null, that means the tag is considered invalid
	 		//Make result message of it here
	 		if (null == req_attrs) {
	 			addResult(f_name,lineNum,tag,ResultSet.Message.INVALID_OPEN_TAG,pos);
	 			req_attrs = new Vector<String>(); //make an empty vector
	 		}
 	 		req_attrs.removeAll(prev_attrs);
 	 		if(!req_attrs.isEmpty()) {
 	 			for(int i = 0; i < req_attrs.size(); i++) {
 	 				addResult(f_name,lineNum,tag,ResultSet.Message.MISSING_REQ_ATTR,pos);
 	 			}
 	 		}
 	 			
 	 	}
 	 	
 	 	return true;
 	 }
 	 
 	 /*
 	  * This function processes all close tags found while processing
 	  * the html input.
 	  * 
 	  * Returns => nothing at this time
 	  */
 	 private void process_close_tag(String tag_name,String f_name,int lineNum,int pos) {
 	 	tag_name = tag_name.trim().toLowerCase().split("<")[1];
 	 	String[] tmp_data = tag_name.split(">");
 	 	tag_name = tmp_data[0];
 	 	tag_name = tag_name.replaceAll("\\s","");
 	 	
 	 	if(openTags.empty()) {
 	 		addResult(f_name,lineNum,tag_name,ResultSet.Message.MISSING_OPEN_TAG,pos);
 	 		return;
 	 	}
 	 	StackPair<String,Integer> open_tag_pair = openTags.pop();
 	 	String open_tag = open_tag_pair.getFirstItem().trim().toLowerCase();
 	 	int stackLineNum = open_tag_pair.getSecondItem().intValue();
 	 	if(null != n_config.getCloseTag(open_tag) && 
 	 							!n_config.getCloseTag(open_tag).equalsIgnoreCase(tag_name)) {
 	 		addResult(f_name,stackLineNum,open_tag,ResultSet.Message.MISSING_CLOSE_TAG,pos);
 	 	}
 	 }
 	 
 	 /*
 	  * This is a helper function that will add a result to the
 	  * ResultSet container in the Tags class
 	  *
 	  * Returns => nothing at this time
 	  */
 	 private void addResult(String f_name, int lineNum, String line, 
 	 									ResultSet.Message msg, int cursor) {
 	 	r_set.add(new ResultSet(f_name,lineNum,line,msg,cursor));
 	 }
 	 
 	 /*
 	  * This is a helper function for adding tags to the stack that
 	  * will be used to check for missing or un-closed tags near the
 	  * end.
 	  * 
 	  * Returns => true if add was successful, false if not
 	  */
 	 private boolean addTagToStack(String tag_name, int lineNum) {
 	 	if(n_config.isOpenTag(tag_name) && (null != n_config.getCloseTag(tag_name))) {
 	 		StackPair<String,Integer> n_pair = new StackPair<String,Integer>(tag_name,
 	 																		new Integer(lineNum));
 	 		openTags.push(n_pair);
 	 		return true;
 	 	}
 	 	else if(null == n_config.getCloseTag(tag_name)) {
 	 		return true;
 	 	}
 	 	return false;
 	 }
 	 
 	/*
 	 * This function will return the list of result sets found
 	 * while processing the HTML data
 	 *
 	 * Returns => the list of result sets
 	 */
 	public ArrayList<ResultSet> getResultSet() {
 		return r_set;
 	}
 }
