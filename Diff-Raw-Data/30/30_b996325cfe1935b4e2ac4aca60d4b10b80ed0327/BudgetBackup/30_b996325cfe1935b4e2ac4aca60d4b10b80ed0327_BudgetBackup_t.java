 package budgetapp.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 
 /**
  * Class responsible for reading and writing to a backupfile
  * @author Steen
  *
  */
 
 public class BudgetBackup {
 	
 	/**
 	 * Writes a backup file. Overwrites file if it exists
 	 * @param entries - The list of entries to write
 	 * @param filename - Filename to write to
 	 */
 	public void writeBackupFile(ArrayList<BudgetEntry> entries, String filename)
 	{
 		
 		try 
 		{
 	        File myFile = new File(filename);
 	        myFile.createNewFile();
 	        FileOutputStream fOut = new FileOutputStream(myFile);
 	        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
 	        
 	        for(int i = 0; i < entries.size(); i++)
 	        {
 	        	myOutWriter.append(entries.get(i).getDate() + "\n");
 	        	myOutWriter.append(""+entries.get(i).getValue().get() + "\n");
 	        	myOutWriter.append(entries.get(i).getCategory() + "\n");
 	        	myOutWriter.append(entries.get(i).getComment() + "\n");
 	        	myOutWriter.append(entries.get(i).getFlags() + "\n");
 	        	
 	        }
 	        
 	        myOutWriter.close();
 	        fOut.close();
 	    } 
 		catch (Exception e) 
 		{
              e.printStackTrace();
 	    } 
 
 	}
 	
	/**
	 * Reads a file with entries and returns a list with them
	 * @param filename - The file to read
	 * @return - A list of all transactions
	 */
 	public ArrayList<BudgetEntry> readBackupFile(String filename)
 	{
 		
 		ArrayList<BudgetEntry> theList = new ArrayList<BudgetEntry>();
 		FileInputStream is;
 		InputStreamReader streamReader;
 		BufferedReader bufferedReader;
 		try
 		{
 			File inputFile = new File(filename);
 			if(!inputFile.isFile())
 				throw new Exception();
 			
 			is = new FileInputStream(inputFile);
 			streamReader = new InputStreamReader(is);
 			bufferedReader = new BufferedReader(streamReader);
 			
 			String date = "", value = "", category = "", comment = "", flags = "";
 			
 			while(true)
 			{
 				date = bufferedReader.readLine();
 				value = bufferedReader.readLine();
 				category = bufferedReader.readLine();
 				comment = bufferedReader.readLine();
 				flags = bufferedReader.readLine();
 				
 				if(date == null)
 					break;
 				
 				theList.add(createEntry(date, value, category, comment, flags));
 				
 			}
 			
 			bufferedReader.close();
 			streamReader.close();
 			is.close();
 			return theList;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	private BudgetEntry createEntry(String date, String value, String category, String comment,String flags)
 	{
 		
 		double theValue = Double.parseDouble(value);
 		int theFlags = Integer.parseInt(flags);
 		return new BudgetEntry(new Money(theValue), date, category, comment, theFlags);
 	}
 }
