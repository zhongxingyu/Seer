 package hg.histo;
 
 import java.io.File;
 
 public class SearchFile {
 
 	String path ;
	String name;
	File filePath ; 
 	String path_default = "C:/Users/Laetitia/Documents/Projet_Histo/image0046.csv" ;
 	String name_default =  "image0046.csv";
 	File filePath_default = new File("C:/Users/Laetitia/Documents/Projet_Histo"); 
 	
 	
 
 
 	public SearchFile(String path) {
 		super();
 		if(path == null){
 			this.path = this.path_default;
			this.name = this.name_default;
			this.filePath = this.filePath_default;
 		}
 		else
 		{
 		this.path = path;
 		File f = new File(path);
		this.name = f.getName();
		this.filePath = new File(f.getParent());
 		
 		}
 		
 		
 	}
 	
     public  boolean searchFileImage(String name,File filePath)
     {
     	int count = 0 ;
     	//fill list with all files in FilePath
         File[] list = filePath.listFiles();
         if(list!=null)
         	//for all files in list 
         for (File fil : list)
         {
         	//if there are folders
             if (fil.isDirectory())
             {
             	searchFileImage(name,fil);
             }
             //attention aux Majuscules ! 
             else if (name.equalsIgnoreCase(fil.getName()))
             {
             	
                
                 System.out.println("found : "+ fil + " in folder : "+ fil.getParentFile());
                 
                 count = count + 1;
             }
         }
         if (count == 0)
         	return false;
         else
         	return true;
 }
 
 }
