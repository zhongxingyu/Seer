 package pcc.io;
 
 import java.util.*;
 import java.io.*;
 
 import pcc.vercon.Project;
 import pcc.vercon.ProjectVersion;
 
 public class IOUtils {
 	public static Project openProject(String fileName) throws IOException, ClassNotFoundException {
 		Project		retProj = null;
 		
 		FileInputStream	fin	=	new FileInputStream (fileName);
 		
 		ObjectInputStream	ois	=	new ObjectInputStream (fin);
 		
 		retProj	=	(Project) ois.readObject();
 		ois.close();
 		
 		return (retProj);
 	}
 
 	public static void saveProject(Project project, String fileName) throws IOException {
 		FileOutputStream fout	=	new FileOutputStream (fileName);
 		
 		ObjectOutputStream oos	=	new ObjectOutputStream (fout);
 		
 		oos.writeObject (project);
 		oos.close();
 		
 		
 	}
 
 	public static ProjectVersion openVersion(String fileName) throws IOException, ClassNotFoundException {
 		ProjectVersion		retVersion = null;
 		
 		FileInputStream	fin	=	new FileInputStream (fileName);
 		
 		ObjectInputStream	ois	=	new ObjectInputStream (fin);
 		
 		retVersion	=	(ProjectVersion) ois.readObject();
 		ois.close();
 		
 		return (retVersion);
 	}
 
 	public static void saveVersion(ProjectVersion projectVersion, String fileName) throws IOException {
 
 		FileOutputStream fout	=	new FileOutputStream (fileName);
 		
 		ObjectOutputStream oos	=	new ObjectOutputStream (fout);
 		
 		oos.writeObject (projectVersion);
 		oos.close();
 	}
 
 	public static String[] openSourceFile(String fileName) throws IOException {
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new FileReader(new File(fileName)));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		ArrayList<String> store=new ArrayList<String>();
 		String line;
 		while((line = reader.readLine())!=null)
 		{
 			String [] splitLine = line.split("\n");
 			for(String x:splitLine)
 			{
 				store.add(x);
 			}
 		}
 		
 		reader.close();
 		
		String[] lines = new String[store.size()];
		for(int i = 0; i < store.size(); i++)
			lines[i] = store.get(i);
		return lines;
 
 	}
 
 	public static void writeSourceFile(String fileName, String contents) {
 		
 		BufferedWriter writer=null;
 		FileWriter fwriter=null;
 		try{
 			fwriter=new FileWriter(fileName);
 			writer=new BufferedWriter(fwriter);
 			writer.write(contents);
 			writer.newLine();
 			
 			writer.close();
 		}catch (Exception e)
 		{
 			
 		}
 	}
 
 	public static void createFolder(String name)
 	{
 		File theDir = new File(name);
 		  // if the directory does not exist, create it
 		  if (!theDir.exists())
 		  {
 		  
 		    boolean result = theDir.mkdir();  
 		    if(result){    
 		    	  System.out.println("Folder created");  
 		     }
 
 		  }
 	}
 }
