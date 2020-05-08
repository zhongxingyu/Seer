 package spia1001.InvFall;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 /*
 InvFall Plugin
 
 @author Chris Lloyd (SPIA1001)
 */
 
 public class FileManager 
 {
 	private String dir = "Plugins/InvFall";
	public boolean failed = false;
 	public FileManager()
 	{
 		ensureDir(dir);
 	}
 	private void ensureDir(String path)
 	{
 		File dir = new File(path);
 		if(!dir.exists())
 		{
 			dir.mkdir();
 			createFile("players.txt");
 		}
 	}
 	public File createFile(String name)
 	{
 		try {
 			return makeFileInDir(name);
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
			failed = true;
 		}
 		return null;
 	}
 	private File makeFileInDir(String name) throws IOException
 	{
 		File file = new File(dir + "/" + name);
 		file.createNewFile();
 		if(!file.exists())
 			return file;
 		return null;
 	}
 	public boolean writeFile(String fileName,String data)
 	{
 		try {
 			writeToFileInDir(fileName,data);
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
			failed = true;
 			return false;
 		}
 		return true;
 	}
 	private void writeToFileInDir(String name,String data) throws IOException
 	{
 		 FileWriter fstream = new FileWriter(dir + "/" + name);
 		 BufferedWriter out = new BufferedWriter(fstream);
 		 out.write(data);
 		 out.close();
 	}
 	public String readFile(String fileName)
 	{
 		try {
 			return readFileFromDir(fileName);
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
			failed = true;
 		}
 		return null;
 	}
 	private String readFileFromDir(String name) throws IOException
 	{
 		FileInputStream fstream = new FileInputStream(dir + "/" + name);
 		DataInputStream in = new DataInputStream(fstream);
 		BufferedReader br = new BufferedReader(new InputStreamReader(in));
 		String line = br.readLine();
 		String output = "";
 		while(line != null)
 		{
 			output += line;
 			line = br.readLine();
 		}
 		return output;
 	}
 }
