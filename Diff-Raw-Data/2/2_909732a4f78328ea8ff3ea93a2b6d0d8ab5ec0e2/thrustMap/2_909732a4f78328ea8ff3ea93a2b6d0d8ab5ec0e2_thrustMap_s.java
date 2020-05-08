 package thrust.maps;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 
 
 public class thrustMap
 {
 	// Size of 
 	private char[][] myMap = new char[55][24];
 	private String[] myMapLines;
 	private String myDetails;
 	
 	public mapSection myHighScore;
 	public mapSection myMapSection;
 	
 
 	public thrustMap(String fileName)
 	{
 		try
         { 
 		
 		// Open file and read contents
 		BufferedReader tempOne = new BufferedReader(new FileReader(fileName));
 		myDetails = "";
 			while(tempOne.ready())
 			{
 			// Read contents into memory
 			myDetails = myDetails + tempOne.readLine() + "\n";
 			}
 		// Close file 
 		tempOne.close();
 		
 		// Split raw map details into lines
 		myMapLines = myDetails.split("\n");
 		
 			// Filter Map into sections
 			
 				// locks and positions for sections/headers
 			boolean startSection = false;
 			boolean endSection = false;	
 			int startSectionInt = 0;
 			int endSectionInt = 0;
 			
 			for(int i=0;i<myMapLines.length;i++)
 			{ 	
 				// Find header
 				if(isHeader(myMapLines[i]) && startSection == false)
 				{
 				startSection = true;
 				startSectionInt = i;	
 				}// Find footer
 				
 				if(isFooter(myMapLines[i]) && endSection == false)
 				{
 				endSection = true;
 				endSectionInt = i;
 				}
 				
 				// If the header and footer have been found enter 
 				if(endSection == true && endSection == true)
 				{	
 				String tempHeader = myMapLines[startSectionInt];
 				String tempSection = getStringFromLines(myMapLines, startSectionInt, endSectionInt);			
 				
 				// Create temporary mapSection variable
 				mapSection tempThree = new mapSection(tempHeader, tempSection);
 				
 					// Check temp section against known list of sections
 					for(sectionHeaders a : sectionHeaders.values())
 					{
 						// If the header matches the one found preform switch
 						if(a.isMyHeader(tempThree.mySectionHeader))
 						{
 							switch(a)
 							{
 							case HIGH_SCORE:
 								myHighScore = tempThree;
 									break;
 							case MAP:
 								myMapSection = tempThree;
 									break;		
 							}
 						}
 					}
 				//Reset locks and positions
 				startSection = false; 
 				endSection = false;
 				startSectionInt = 0;
 				endSectionInt = 0;
 				}		
 			}
 		}
 		catch(Exception e)
 		{	
 		}		
 	}
 	
 	public String getMapRaw()
 	{
 	String tempOne = "";
 		for(String a : myMapLines)
 		{
 		tempOne = tempOne + a;
 		}
 	return tempOne;
 	}
 	
 	private boolean isHeader(String s)
 	{
 	char[] tempOne = s.trim().toCharArray();
 		if(tempOne[0] == '[' && tempOne[tempOne.length-1] == ']')
 		{
 		return true;
 		}
 	return false;
 	}
 	
 	private boolean isFooter(String s)
 	{
 	char[] tempOne = s.trim().toCharArray();
 		if(tempOne[0] == '[' && tempOne[1] == '/' && tempOne[tempOne.length-1] == ']')
 		{
 		return true;
 		}
 	return false;
 	}
 	
 	private String getStringFromLines(String[] s, int start, int end)
 	{
 	String tempOne = "";
		for(int i=start;i<=end;i++)
 		{
 		tempOne = tempOne + s[i] + "\n";
 		}
 	return tempOne;
 	}
 	
 
 	enum sectionHeaders
 	{
 		HIGH_SCORE("[High Score]"),
 		MAP("[Map]");
 	
 		private String myHeader;
 		
 		private sectionHeaders(String s)
 		{
 		myHeader = s;	
 		}
 		
 		public String getMyHeader()
 		{
 		return myHeader;
 		}
 		
 		public boolean isMyHeader(String s)
 		{
 			if(myHeader.equals(s.trim()))
 			{
 			return true;
 			}
 		return false;
 		}
 	}
 }
