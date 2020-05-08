 package thesaurus.parser;
 
 import java.io.File;
 
 
 public class Driver 
 {
 
 	public static void main(String[] args)
 	{
 		File test = new File("/home/james/GIT/myWorkspace/TP3/data.graphml");
 		System.out.println(test.getAbsolutePath());
 		InternalRepresentation driver = new InternalRepresentation(test);
 		
 		String[] antonym = new String[3];
 		antonym[0] = "happy";
 		antonym[1] = "content";
 		antonym[2] = "joyful";
 		
 		//driver.addVertex("mathew","Peaceful, Merry", "happy, content");
 		
 		//driver.removeVertex("hate");
 		
 		
 	
 		
 		//driver.editVertex("Joyful", "sad");
		System.out.println(driver.getListOfSynomyns("mathew"));
 		
 	}
 }
