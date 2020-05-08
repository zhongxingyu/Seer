 package mvhsbandinventory;
 
 import java.io.File;
 
 /**
  *
  * @author jonathan
  */
 public class InstrumentFileStore extends InstrumentStore 
 {
 	private File directory;
 	
 	/**
 	 * Constructs a new InstrumentFileStore object for storing Instrument 
 	 * information on the disk.
 	 * @param path
 	 * @throws Error
 	 */
 	public void InstrumentFileStore (String path) throws Error
 	{
 		directory = new File(path);
 		
 		if (!directory.exists()) 
 		{
 			throw new Error("File store path does not exist.");
 		}
 		
 		if (!directory.isDirectory()) 
 		{
 			throw new Error("File story path must be a directory; the current" +
 							" path is not a directory");
 		}
 	}
 	
 	/**
 	 * Generates a File object that should reference the specified instrument
 	 * in the data store.
 	 * @param instrument
 	 * @return file that should reference the specified instrument
 	 */
 	private File file (Instrument instrument)
 	{
		String name = (String) instrument.get("name");
		String brand = (String) instrument.get("brand");
		String serial = (String) instrument.get("serial");
 		
 		// Generate the CSV file name
 		String fileName = name + "-" + brand + "-" + serial + ".csv";
 		
 		return new File(directory, fileName);
 	}
 	
 	/**
 	 * Determines whether the specified instrument is already stored in the 
 	 * data store.
 	 * @param instrument
 	 * @return a boolean value determining whether the instrument is in the 
 	 * data store; true indicates that the instrument is in the store and false
 	 * indicates that it doesn't exist in the data store
 	 */
 	public boolean exists (Instrument instrument)
 	{
 		return file(instrument).exists();
 	}
	
	public void add (Instrument instrument)
	{
		
	}
	
	public void update (Instrument instrument)
	{
		
	}
	
	public void delete (Instrument instrument)
	{
		
	}
	
	public Instrument[] load ()
	{
		
	}
 }
