 import com.sleepycat.db.*;
 import java.io.*;
 
 public class CreateIndex
 {
 	private static Database db;
 	public static void main(String[] args)
 	{
 		String dataFile = null;
 		String invertedTitleFile = null;
 		String invertedContributorFile = null;
 		String invertedTextFile = null;
 
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 		try
 		{
 			System.out.print("Enter name of data file:");
 			dataFile= in.readLine();
 
 			System.out.print("Enter name of inverted title file:");
 			invertedTitleFile = in.readLine();
 
 			System.out.print("Enter name of inverted contributor file:");
 			invertedContributorFile = in.readLine();
 
 			System.out.print("Enter name of inverted text file:");
 			invertedTextFile = in.readLine();
 		}
 		catch (IOException e)
 		{
 			System.out.println("Could not get one more more required filenames." + e.getMessage());
 			System.exit(1);
 		}
 
 		DatabaseConfig dbConfig = new DatabaseConfig();
 		dbConfig.setType(DatabaseType.BTREE);
 
 		try
 		{
 			CreateIndex.db = new Database("database", null, dbConfig);
 		}
 		catch (DatabaseException e)
 		{
 			System.out.println("Weird database exception." + e.getMessage());
 			System.exit(1);
 		}
 		catch (FileNotFoundException e)
 		{
 			System.out.println("Could not open database file." + e.getMessage());
 			System.exit(1);
 		}
 
 		try
 		{
			CreateIndex.createTitleIndex(invertedTextFile);	
 		}
 		catch (IOException e)
 		{
 			System.out.println("Could not create title index file." + e.getMessage());
 			try
 			{
 				CreateIndex.db.close();
 			}
 			catch (DatabaseException dbException)
 			{
 				System.out.println("Could not close database." + dbException.getMessage());
 			}
 			System.exit(1);
 		}
 	}
 
 	private static void createTitleIndex(String invertedTitleFile) throws IOException
 	{
 		OperationStatus oprStatus;
		DatabaseEntry key = null;
		DatabaseEntry data = null;
 		String title = null;
 		String documentId = null;
 
 		// Open inverted title file for reading.
 		BufferedReader file = new BufferedReader(new FileReader(invertedTitleFile));
 
 		// Read through the entire file.
 		title = file.readLine();
 		while (title != null)
 		{
 			documentId = file.readLine();
 
 			// Fill in key and data pair.
 			data.setData(documentId.getBytes());
 			data.setSize(documentId.length());
 			key.setData(title.getBytes());
 			key.setSize(title.length());
 
 			try
 			{
 				oprStatus = CreateIndex.db.put(null, key, data);
 				if (oprStatus != OperationStatus.SUCCESS)
 				{
 					throw new IOException("Invalid key/data pair in file.");
 				}
 			}
 			catch (DatabaseException e)
 			{
 				System.out.println("Could not add index entry." + e.getMessage());
 			}
 
 			title = file.readLine();
 		}
 	}
 }
