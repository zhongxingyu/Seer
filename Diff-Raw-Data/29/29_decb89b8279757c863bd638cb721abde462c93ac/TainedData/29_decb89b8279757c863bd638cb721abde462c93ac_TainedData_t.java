 /**
  * 
  */
 package corpus;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 /**
  * @author abilng
  *
  */
 public class TainedData {
 
 	private static final String FILE_NAME = "TranedData.dat";
 
 	private static final String BROWN_TXT_PATH = "/home/abil/workspace/NLP/data/";
 
 	Hashtable<String,Integer> dataMap;
 
 	public TainedData() {
 		try {
 			read(FILE_NAME);
 		} catch (ClassNotFoundException | IOException e) {
 			System.err.println("Data File Not Found: (" + e.getMessage() + ")");
 			System.err.println("Reading taining file name from Text File..");
 
 			dataMap = new Hashtable<String, Integer>();
 
 			train();
 
 			try {
 				save(FILE_NAME);
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		}
 	}
 
 	private void read(String file) throws IOException, ClassNotFoundException {
 		InputStream buffer;
 		buffer = new BufferedInputStream( new FileInputStream(file));
 		ObjectInput input = new ObjectInputStream ( buffer );
 		dataMap = (Hashtable<String, Integer>) input.readObject();
 		input.close();
 	}
 
 	private void save(String file) throws IOException {
 		OutputStream buffer;
 		buffer = new BufferedOutputStream( new FileOutputStream(file));
 		ObjectOutput output = new ObjectOutputStream ( buffer );
 		output.writeObject(dataMap);
 		output.close();
 	}
 	private void train() {
 		String catfile = BROWN_TXT_PATH + "cats.txt";
 		List<String> file_names = new ArrayList<String>();
 		BufferedReader buffer;
 		try
 		{
 			buffer = new BufferedReader(new FileReader(catfile));   
 			String line, temp[];
 
 			while ((line = buffer.readLine())!= null)
 			{ 
 				temp = line.split(" "); //split spaces
 				file_names.add(temp[0]);
 			}
 			buffer.close();
 		} catch(IOException | NumberFormatException ex){
 			ex.printStackTrace();
 		}
 
 		BrownCorpusReader brownCorpusReader = new BrownCorpusReader();
 		
 		for (String filename : file_names) {
 			
 			try {
 				buffer = new BufferedReader(new FileReader(BROWN_TXT_PATH+filename));
 				System.err.println("Opening :" + filename +"..");
 				brownCorpusReader.getWords(buffer, dataMap);
 				buffer.close();
 				
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 	
 	
 	public int count(String word) {
		Integer ret = dataMap.get(word);
		if(ret == null) return 0;
		return ret;
 	}
 
 }
