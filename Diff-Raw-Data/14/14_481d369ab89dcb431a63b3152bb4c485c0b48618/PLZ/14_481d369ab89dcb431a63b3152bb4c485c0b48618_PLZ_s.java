 package PLZ;
 
 import java.io.*;
 import java.util.*;
 
 
 public class PLZ 
 {
 	private static final boolean verbose = true;
 	private List<cAddy> PLZlist;
 	
 	public PLZ(){}
 	
  	public boolean parse(String filename)
 	{
 		try
 		{
 			//creating a handle with 'r' rights to access the file
 			FileInputStream fstream = new FileInputStream(filename);
 			//creating a buffered reader to read line by line
 			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
 			
 			List<cAddy> pp = new ArrayList<cAddy>();
 			//temporary string that always holds one line of the text file
 			String s;
 			String l[];
 			//read a line and s<--line
 			try{
 				while((s=br.readLine())!= null)
 				{
 					l = s.split("\t");
 					if(l.length == 7)
 					{
 						try
 						{
 							pp.add(new cAddy(
 									//<interpreting the text input>
 									Integer.parseInt(l[2]),//PLZ
 									l[4],//village name
 									l[6],//kanton
 									Integer.parseInt(l[0]),//dunno what, maybe some internal number the post uses
 									Integer.parseInt(l[1]),//no clue what this number stands for
 									Integer.parseInt(l[3])//looks like an iterator of the different village names
 									//</interpreting the text input>
 									));
 						}
 						catch(NumberFormatException e)
 						{
 							if (verbose)//the show must go on
 								System.out.println("Error: converting some numbers in \'"+s+"\' failed!");
 						}
 							
 					}
 					else
 					{
 						if(verbose && !s.equals(""))//printing some debug information (if wanted)
 							System.out.println("Error: Splitting \'"+s+"\' by tabs returns an invalid result!" );
 					}
 				}
 				//unregging the access handle
 				fstream.close();
 				Collections.sort(pp);
 				PLZlist = pp;
 				return true;
 			}
 			catch(IOException e)
 			{
 				if(verbose)//fails reading it... so nothing's returned
 					System.out.println("Error: can't access \'"+filename+"\'!");
 				return false;
 			}
 			
 			//sorting the list for binsearch
 		}
 		catch(FileNotFoundException e)
 		{
 			//please specify a valid file
 			if(verbose)//fails reading it... so nothing's returned
 				System.out.println("Error: File \'"+filename+"\' not found!");
 			return false;			
 		}
 		
 		
 		
 	}
 
 	public cAddy[] HandleInput(String Input)
 	{
 		if (PLZlist == null)
 		{
 			if(verbose)
 				System.out.println("Please parse a file first!");
 			return null;
 		}
 		
 		int index,high,low;
 		
 		try
 		{
 			//searching for the PLZ
 			index = Collections.binarySearch(PLZlist,new cAddy(Integer.parseInt(Input),"","",0,0,0));
 		}
 		catch(NumberFormatException e)
 		{
 			if(verbose)
 				System.out.println("Seems your input wasn't a Number and thus no PLZ");
 			return null;
 		}
 		if(index < 0 || index > PLZlist.size()-1)
 		{
 			if(verbose)
 				System.out.println("Couldn't find the PLZ "+Input+" in switzerland");
 			return null;
 		}
 		
 		low = 0;
 		high = PLZlist.size()-1;
 		for(int i = index;i!=0;i--)
 		{
 			if(PLZlist.get(index).GetPLZ() != PLZlist.get(i).GetPLZ())
 			{
 				low = i+1;
 				break;
 			}
 		}
 		
 		for(int i = index;i!=PLZlist.size();i++)
 		{
 			if(PLZlist.get(index).GetPLZ() != PLZlist.get(i).GetPLZ())
 			{
 				high = i-1;
 				break;
 			}
 		}
 		
 		cAddy[] ret=new cAddy[high-low+1];
 		
 		int i = 0;
 		do
 		{	
 			ret[i] = PLZlist.get(low+i);
 			i++;
 		}
 		while(low+i <= high);
 		
 		return ret;				
 	}
 	
 	public String makeString(cAddy[] a)
 	{
 		String ret = "";
 		ret += "The PLZ "+a[0].GetPLZ()+" belongs to ";
 		for(int i = 0; i != a.length;i++)
 		{
 			if(i!=a.length-1) 
 				ret += a[i].GetName()+" & ";
 			else
 				ret += a[i].GetName();
 		}
 		ret += " in the canton of "+a[0].GetKanton();
 		
 		return ret;
 	}
 	
 	public void addnew(int PLZ, String name, String Kanton)
 	{
 		PLZlist.add(new cAddy(PLZ,name,Kanton,0,0,0));
 		Collections.sort(PLZlist);
 	}
 	
 	
 
 	public void save(String fname)
 	{
 		try
 		{
 			FileWriter fstream = new FileWriter(fname);
 			BufferedWriter out = new BufferedWriter(fstream);
 			for(int i = 0; i != PLZlist.size(); i++)
 			{
				out.write(PLZlist.get(i).toString()+"\n");
 			}
 			fstream.close();
 		}
 		catch(IOException e)
 		{
			System.out.println("failed to write the file");
 			//meh don't bother anymore
 		}
 	}
 }
