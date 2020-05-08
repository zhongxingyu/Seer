 package differ;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.LinkedList;
 
 import differ.diff_match_patch;
 import differ.diff_match_patch.Diff;
 import differ.diff_match_patch.LinesToCharsResult;
 
 ////////////////////////////////////////////////////////////////
 // Compare two files to get all the changes out (deleted, added, modified lines)
 // each diff object, find the function that the diff belongs to
 // Need diff(start,stop)
 // After Each diff get
 // 		currentOldText = Equal + Delete
 //		currentNewText = Equal + Insert
 // Parse currentOldText/Newtext to find the new search Location, this guarantee the diff location.
 // Find involved methods
 //	0. Parse Package, Class
 //	1. Parse all methods in both Old/New file methods(start,stop)
 //		Get Method signature as well.
 //  2. Parse all methods appreared in diff object
 //  3. Check if diff(start,stop) is inside any methods(start,stop) -> changedMethod list
 public class filediffer {
 	
 	// Store location of method in a file
 	public class methodResult
 	{
 		public int start = -1;
 		public int end = -1;
 		public String fullName;
 		public String packageName;
 		public String className;
 		public String signature;
 		public List<String> parameters = new ArrayList<String>();
 		
 		public methodResult(){};
 	};
 	
 	// Store location of a diff in a file
 	public class diffObjectResult
 	{
 		public int start = -1;
 		public int end = -1;
 		Diff diffObject;
 	};
 	
 	private boolean isModified = false;
 	private String oldFileContent;
 	private String newFileContent;
 	
 	private diff_match_patch myDiffer = new diff_match_patch();
 	private List<Diff> diffObjects 	  = new LinkedList<Diff>();
 	
 	private List<diffObjectResult> deleteObjects = new ArrayList<diffObjectResult>();
 	private List<diffObjectResult> insertObjects = new ArrayList<diffObjectResult>();
 	
 	/**
 	 * fileDiffer constructor
 	 * @param filecontent1: raw file from old commit
 	 * @param filecontent2: raw file from new commit
 	 */
 	public filediffer(String oldFileContent, String newFileContent) {
 		this.oldFileContent = oldFileContent;
 		this.newFileContent = newFileContent;
 	}
 
 	/**
 	 * Diff the two files by line number
 	 * Compare line by line
 	 */
 	public void diffFilesLineMode()
 	{
 		if(this.oldFileContent == null || this.newFileContent == null)
 			return;
 		
 		this.diffObjects.clear();
 		this.deleteObjects.clear();
 		this.insertObjects.clear();
 		
 		// convert diff object to set of lines
 		LinesToCharsResult result = myDiffer.diff_linesToChars(oldFileContent, newFileContent);
 		this.diffObjects = myDiffer.diff_main(result.chars1, result.chars2, false);
 		myDiffer.diff_charsToLines((LinkedList)diffObjects, result.lineArray);
 		
 		// parse the diff location
 		getDiffLocation();
 	}
 	
 	public void print()
 	{
 		for(Diff mydiff : this.diffObjects)
 		{
 			if(mydiff.operation != diff_match_patch.Operation.EQUAL)
 				System.out.println(mydiff.toString());
 		}
 		
 		// Print diff objects
 		int count = 0;
 		for(diffObjectResult mydiff : this.deleteObjects)
 		{
 			count ++;
 			System.out.print("Delete object " + count);
 			System.out.println("Start: "+ mydiff.start + "Stop " + mydiff.end);
 		}
 		
 		for(diffObjectResult mydiff : this.insertObjects)
 		{
 			count ++;
 			System.out.print("Insert object " + count);
 			System.out.println("Start: "+ mydiff.start + "Stop " + mydiff.end);
 		}
 	}
 	
 	/**
 	 * Parse DiffObjects to methods and class list
 	 */
 	public void getClassContent(String input)
 	{
 		String regex = "public[\\s]+class[\\s]+([\\w]+)[\\s]+[\\w]+";
 		Pattern pattern = Pattern.compile(regex);
 		Matcher matcher = pattern.matcher(input);
 		while (matcher.find())
 		{
 			String content = matcher.group(2);
 			String className = matcher.group(1);
 			//parse all method inside
 			ArrayList<methodResult> methods = parseMethods(content);
 			for(methodResult m :methods)
 			{
 				m.className = className;
 			}
 		}
 	}
 	
 	/**
 	 * Parse Package name
 	 * @param input text to search, for example a file content
 	 * @return package name
 	 */
 	public String parsePackage(String input)
 	{
 		String regex = "package[\\s]+([\\w_]+)[\\s]*[;]";
 		Pattern pattern = Pattern.compile(regex);
 		Matcher matcher = pattern.matcher(input);
 		while (matcher.find())
 		{
 			String packageName = matcher.group(1);
 			return packageName;
 		}
 		
 		return "";
 	}
 	
 	/**
 	 * Parse DiffObjects to methods and class list
 	 * @param input the txt to search for function
 	 * @return List of methods appeared in the input txt
 	 */
 	public ArrayList<methodResult> parseMethods(String input)
 	{
 		ArrayList<methodResult> results = new ArrayList<methodResult>();
 		
 		// get all method names, use non-greedy match 
 		// account for public, private, protected, static function etc
 		String regex = "[\\s]*(public|private|protect)?[\\s]*[\\w<>]*?[\\s]+([\\w]+)[(](.*?)[)]";
 		Pattern pattern = Pattern.compile(regex);
 		
 		Matcher matcher = pattern.matcher(input);
 		while (matcher.find())
 		{
 			methodResult method = new methodResult();
 			method.className = matcher.group(2);
 			method.signature = matcher.group(3);
 			method.start 	 = matcher.start();
 			method.end 		 = matcher.end();
 			
 			// Parse parameters
 			ArrayList<String> paras = parseFunctionParameters(method.signature);
 			method.parameters = paras;
 			results.add(method);
 			
 			System.out.println("Function: " +
 					matcher.group() + " from "+
 					matcher.start() + " to " +
 					matcher.end());
 		}
 		
 		return results;
 	}
 	
 	/**
 	 * Parse parameters type in a function
 	 * @param method the method that has parameters in it
 	 * ex: "int a, int b, Map<String> list, String s"
 	 */
 	public ArrayList<String> parseFunctionParameters(String parameter)
 	{
 		ArrayList<String> results = new ArrayList<String>();
 		// get type of the method
 		String regex = "[\\s]*([\\w<>_]+?)[\\s]+([\\w_]+?)[\\s]*[,]?";
 		Pattern pattern = Pattern.compile(regex);
 		
 		Matcher matcher = pattern.matcher(parameter);
 		while (matcher.find())
 		{
 			String type = matcher.group(1);
 			String name = matcher.group(2);
 			results.add(type);
 		}
 		
 		return results;
 	}
 	
 	/**
 	 * Calculate diffLocation
 	 */
 	public void getDiffLocation()
 	{
 		if (diffObjects.isEmpty())
 			return;
 		
 		String oldText = "";
 		String newText = "";
 		
 		int oldTextLocation = 0;
 		int newTextLocation = 0;
 		
 		for(Diff mydiff : this.diffObjects)
 		{
 			// Append equal to current text
 			if(mydiff.operation == diff_match_patch.Operation.EQUAL)
 			{
 				oldText += mydiff.text;
 				newText += mydiff.text;
 				
 				// Update current location
 				oldTextLocation += mydiff.text.length();
 				newTextLocation += mydiff.text.length();
 			}
 			else
 			// Search OldFileContent for DELETE
 			// Old TEXT is EQUAL + DELETE
 			if(mydiff.operation == diff_match_patch.Operation.DELETE)
 			{
 				// Each diff, match old txt to find the location
 				int startlocation = this.oldFileContent.indexOf(mydiff.text, oldTextLocation);
 				if(startlocation == -1)
 				{
 					System.out.println("No location found for this delete");
 				}
 				else
 				{
 					// calculate the range of this diff
 					diffObjectResult diffResult = new diffObjectResult();
 					int stoplocation 	  = startlocation + mydiff.text.length();
 					diffResult.diffObject = mydiff;
 					diffResult.start 	  = startlocation;
 					diffResult.end 		  = stoplocation;
 					
 					this.deleteObjects.add(diffResult);
 				}
 				
 				// Update oldtext
 				oldText += mydiff.text;
 				oldTextLocation += mydiff.text.length();
 				
 				this.isModified = true;
 			}
 			else
 			// Search NewFileContent for INSERT
 			// New TEXT is EQUAL + INSERT	
 			if(mydiff.operation == diff_match_patch.Operation.INSERT)
 			{
 				// Each diff, match old txt to find the location
 				int startlocation = this.newFileContent.indexOf(mydiff.text, newTextLocation);
 				if(startlocation == -1)
 				{
 					System.out.println("No location found for this insert");
 				}
 				else
 				{
 					// calculate the range of this diff
 					diffObjectResult diffResult = new diffObjectResult();
 					int stoplocation 	  = startlocation + mydiff.text.length();
 					diffResult.diffObject = mydiff;
 					diffResult.start 	  = startlocation;
 					diffResult.end 		  = stoplocation;
 					this.insertObjects.add(diffResult);
 				}
 				
 				// Update newtext
 				newText += mydiff.text;
 				newTextLocation += mydiff.text.length();
 				
 				this.isModified = true;
 			}
 		}
 		
 	}
 	
 			
 	public String getOldFileContent() {
 		return oldFileContent;
 	}
 
 	public void setOldFileContent(String filecontent1) {
 		this.oldFileContent = filecontent1;
 	}
 
 	public String getNewFileContent() {
 		return newFileContent;
 	}
 
 	public void setNewFileContent(String filecontent2) {
 		this.newFileContent = filecontent2;
 	}
 
 	public List<Diff> getDiffObjects()
 	{
 		return this.diffObjects;
 	}
 
 	public boolean isModified() {
 		return isModified;
 	}
 
 	public void setModified(boolean isModified) {
 		this.isModified = isModified;
 	}
 
 	public List<diffObjectResult> getDeleteObjects() {
 		return deleteObjects;
 	}
 	
 	public List<diffObjectResult> getInsertObjects() {
 		return insertObjects;
 	}
 	
 }
