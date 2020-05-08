 import java.util.ArrayList;
 
 //Specific department at PAX (ex. Line Entertainment)
 
 /*
  * Needs to handle:
  * 
  * Department Name - Done
  * 2d array with time zones and list of enforcers in time zones -done
  * Staff Number for departments (2d array perhaps?).  Possibly implement this later
  * Specialized Constructor
  * Output Method - on hold
  * Remove Enforcer from Department - Done
  * Contains Enforcer - Done
  */
 public class Department extends Object{
 	public String departmentName; 
 	
 	//2d array of enforcers.  1st dimension = shifts.  2nd dimension = enforcers (arraylist to ease adding/removing)
 	//Maybe fix unchecked conversion but probably unnecessary
 	public ArrayList<Enforcer>[] myTeamArray = new ArrayList[3];
 	
 	public Department(String setName)
 	{
 		this.departmentName = setName;
 		myTeamArray[0]= new ArrayList<Enforcer>();
 		myTeamArray[1]= new ArrayList<Enforcer>();
 		myTeamArray[2]= new ArrayList<Enforcer>();
 	}
 	
 	//Add an enforcer to a shift
 	public void addEnforcer(Enforcer addedEnforcer, int shift)
 	{
 		//Throw an exception if adding an enforcer who already exists
 			try {
 				if (myTeamArray[shift-1].contains(addedEnforcer)){			
 					throw new Exception("Enforcer exists in this department already");
 				}
 				
 				try{
 					if (shift>3||shift<0)
 					{
 						throw new Exception ("Shift is outside of bounds: " + shift);
 					}
 					//System.out.println("Adding enforcer " + addedEnforcer.getHandle());
 					myTeamArray[shift-1].add(addedEnforcer);
 				}
 				catch (Exception d)
 				{
 					d.printStackTrace();
 				}
 				
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}		
 
 	}
 	
 	//Department contains an enforcer based upon handle name
 	public boolean contains(String handle)
 	{
 		//Iterate through all shifts
 		for (int i=0; i<myTeamArray.length;i++)
 		{
 			for (int n=0;n<myTeamArray[i].size();n++)
 			{
 				if (myTeamArray[i].get(n).getHandle()==handle)
 				{
 					return true;
 				}
 			}
 			
 		}
 		return false;
 	}
 	
 	//Remove an enforcer based upon handle name
 	public Enforcer remove(String handle)
 	{
 		//Iterate through all shifts
 		for (int i=0; i<myTeamArray.length;i++)
 		{
 			for (int n=0;n<myTeamArray[i].size();n++)
 			{
 				if (myTeamArray[i].get(n).getHandle()==handle)
 				{
 					System.out.println ("Successful removal!");
 					return myTeamArray[i].remove(n);
 				}
 			}
 			
 		}
 		return null;
 	}
 	
 	//ToString output of Department Data
 	public String toString()
 	{
 		String output = "Department Name: ";
 		output += departmentName + "\n";
 		
 		//Output each enforcer in each shift's arraylist
 		for (int i=0; i<myTeamArray.length;i++)
 		{
			output+= "Shift: " + i + "\n";
 			for (int n=0;n<myTeamArray[i].size();n++)
 			{
 				output+=myTeamArray[i].get(n).toString();	
 			}
			
 		}
 		
 		return output;
 	}
 }
