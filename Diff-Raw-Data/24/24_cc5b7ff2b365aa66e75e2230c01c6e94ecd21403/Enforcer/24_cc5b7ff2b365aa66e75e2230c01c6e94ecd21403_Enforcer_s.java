 import java.util.StringTokenizer;
 
 /*
  * Needs to handle:
  * 
  * Handle - Done
  * Time Preferences - Need to figure out better way of setting/getting data since this less complex
  * Department Preferences - Done
  * Current Department - done
  * Current Shift - done
  * Specialized Constructor
  * Happiness Level based upon Department/ Time - calculation needed
  * Output method - on hold
  */
 public class Enforcer extends Object {
 
 	private static final int shifts = 3;
 	private static final int depPrefNum = 5;
 	
 	private String handle;
 	private String[] depPref;
 	private int[] timePref = new int[shifts];
 	private String myDepartment;
	private int myShift = -1;
 	
 	public Enforcer()
 	{
 		
 		setDepPref(new String[depPrefNum]);
 		setHandle ("Enforcer");
 		myDepartment = null;
 	}
 	
 
 	//ToString output of Enforcer Data.  Currently paragraph style for testing/readability...
 	//Will need to later convert into outputfriendly format or make a new method for output friendly format (probably second)
 	public String toString()
 	{
 		//Output handle
		String output = "Handle: " + getHandle() + "  |  Department: " + myDepartment + "  |   Shift: " +myShift;
						
		output+= "\n";
 		
 		//Output Department Preferences
 		output += "Department Preferences: ";
 		for (int i=0;i<depPref.length;i++)
 		{
 			output+= depPref[i];
 			if (i+1<depPref.length)
 				{output+=",";}
 		}
		output += "\n";
 		
 		//Output Time Preferences
 		output += "Shift Preferences: ";
 		for (int i=0;i<timePref.length;i++)
 		{
			output+= timePref[i];
 			if (i+1<timePref.length)
 				{output+=",";}
 		}
 		output += "\n";
 		
 		//Output 
 		return output;
 	}
 	
 	/*
 	 * Converts a string into the array of departments.
 	 * Currently mandates that the array has to match the preset depPrefNum
 	 * Consider changing so people can put in null preferences...?
 	 *  
 	*/
 	private String[] depConvert(String depString) throws Exception
 	{
 		String[] myString = depString.split(",");
 		
 		if(myString.length !=depPrefNum){
 			throw new Exception("Departments in the string does not match " +
 					"Department Prefr Number of " + depPrefNum);
 		}
 		return myString;
 	}
 	
 	//Calculate happiness levels
 	public int calculate()
 	{
 		return 0;
 	}
 	
 	//Getters and Setters
 	public String[] getDepPref() {
 		return depPref;
 	}
 
 	public void setDepPref(String[] depPref) {
 		this.depPref = depPref;
 	}
 
 	//add a list of departments based upon a string
 	public void setDepPref(String depPrefString)
 	{
 		try {
 			this.depPref = depConvert(depPrefString);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public String getHandle() {
 		return handle;
 	}
 
 	public void setHandle(String handle) {
 		this.handle = handle;
 	}
 	
 	public void setDepartment (String newDep)
 	{
 		this.myDepartment = newDep;
 	}
 	
 	public String getDepartment()
 	{
 		return myDepartment;
 	}
 	
 	public void setShift (int newShift)
 	{
 		try {
 			if (newShift >3)
 			{
 				throw new Exception("New Shift is out of bounds: " + newShift);
 			}
 			this.myShift= newShift;
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public void setShift(String str)
 	{
 		setShift(Integer.parseInt(str));
 	}
 	
 	public int getShift()
 	{
 		return myShift;
 	}
 	public int[] getTimePref() {
 		return timePref;
 	}
 
 	public void setTimePref(String str)
 	{
 		String remComma = str.replace(",","");
 		for (int i=0;i<remComma.length()-1;i++)
 		{
 			timePref[i]=Character.getNumericValue(remComma.charAt(i));
 		}
 	}
 	
 	
 }
 
