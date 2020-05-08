 package edu.sru.andgate.bitbot.tools;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import android.content.Context;
 import android.util.Log;
 
 public class FileManager
 {
 	private static Context _context;
 	
 	public static void setContext(Context context)
 	{
 		_context = context;
 	}
 	
 	public static String getFileDescriptionFromFile(String directory, String filename)
 	{
 		String line = "";
 		// try opening the myfilename.txt
 		try 
 		{
 			// open the file for reading
 			File f = new File(_context.getDir(directory, Context.MODE_PRIVATE), filename);
 			FileInputStream fis = new FileInputStream(f);
 			//InputStream instream = openFileInput();
 			
 			// if file the available for reading
 			if (fis != null)
 			{
 				// prepare the file for reading
 				InputStreamReader inputreader = new InputStreamReader(fis);
 				BufferedReader buffreader = new BufferedReader(inputreader);
 				
 				line = buffreader.readLine();
 				
 			}
 			// close the file again
 			fis.close();
			return line.toString();
 		}
 		catch (java.io.FileNotFoundException e)
 		{
 			// do something if the myfilename.txt does not exits
 			Log.v("Test", "File Not Found");
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			Log.v("Test", "I/O Error");
 		}
 		
		return line.toString();
 	}
 	
 	public static String readTextFileFromDirectory(String directory, String filename)
 	{
 		String line;
 		String temp = "";
 		// try opening the myfilename.txt
 		try 
 		{
 			// open the file for reading
 			File f = new File(_context.getDir(directory, Context.MODE_PRIVATE), filename);
 			FileInputStream fis = new FileInputStream(f);
 			//InputStream instream = openFileInput();
 			
 			// if file the available for reading
 			if (fis != null)
 			{
 				// prepare the file for reading
 				InputStreamReader inputreader = new InputStreamReader(fis);
 				BufferedReader buffreader = new BufferedReader(inputreader);
 				
 				// read every line of the file into the line-variable, on line at the time
 				while (( line = buffreader.readLine()) != null)
 				{
 					// do something with the settings from the file
 					temp += line + "\n";
 				}
 			}
 			// close the file again
 			fis.close();
 			return temp.toString();
 		}
 		catch (java.io.FileNotFoundException e)
 		{
 			// do something if the myfilename.txt does not exits
 			Log.v("Test", "File Not Found");
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			Log.v("Test", "I/O Error");
 		}
 		
 		return temp.toString();
 	}
 	
 	/*
 	 * Method that recieves an xml file name, and target <tag> 
 	 * 	returns the text in the specified <tag></tag>
 	 */
 	public static String readXML(String my_file, String tag_name)
 	{
 		try {
 			InputStream is = _context.getAssets().open(my_file);
 			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder;
 			docBuilder = docBuilderFactory.newDocumentBuilder();
 			Document doc = docBuilder.parse(is);
 			doc.getDocumentElement ().normalize ();
 			
 			NodeList tutorialText = doc.getElementsByTagName(tag_name);
 			
 			Element myText = (Element) tutorialText.item(0);
 			
 			return ((Node)myText.getChildNodes().item(0)).getNodeValue().trim();
 			
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 		
 	}//end of readXML()
 	
 	public static InputStream readFile(String fileName)
 	{
 		try {
 			return _context.getAssets().open(fileName);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	
 	/**
 	 * This was originally getFiles in ReadDirectory
 	 * @param dir the directory to get the files out of
 	 * @return an array of filenames
 	 */
 	public static String[] getFileNamesInDir(String dir)
 	{
 		File folder = new File(dir);
 		File[] listOfFiles = folder.listFiles();
 		String[] items = new String[listOfFiles.length];
 		for (int i = 0; i < listOfFiles.length; i++) 
 		{
 			if (listOfFiles[i].isFile()) 
 			{
 				items[i] = listOfFiles[i].getName();
 			}
 			else if (listOfFiles[i].isDirectory()) 
 			{
 				// System.out.println("Directory " + listOfFiles[i].getName());
 			}
 		}
 		return items;	
 	}
 	
 	public static void deleteFile(String directory, String filename)
 	{
 		File f = new File(_context.getDir(directory,Context.MODE_PRIVATE), filename);
 		f.delete();
 	}
 	public static void saveCodeFile(String data, String filename)
 	{
 
 		File f = new File(_context.getDir("Code", Context.MODE_PRIVATE), filename);
 		
 		try
 		{
 			f.createNewFile();
 			FileOutputStream fos = new FileOutputStream(f);
 			fos.write(data.getBytes());
 			fos.close();
 		} catch (Exception e)
 		{
 			Log.v("Test", "Error writing file");
 		}
 	}
 	
 }
