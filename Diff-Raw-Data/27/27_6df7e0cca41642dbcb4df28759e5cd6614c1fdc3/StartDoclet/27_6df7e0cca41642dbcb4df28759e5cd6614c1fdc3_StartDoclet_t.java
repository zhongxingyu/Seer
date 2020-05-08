 package prodoc;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.HashMap;
 
 import com.sun.javadoc.ClassDoc;
 import com.sun.javadoc.Doclet;
 import com.sun.javadoc.PackageDoc;
 import com.sun.javadoc.RootDoc;
 import com.sun.javadoc.Tag;
 
 public class StartDoclet extends Doclet{
 	
 	static File libFolder;
 	
 	static File docFolder;
 	
 	static File templateFolder;
 	
 	static File exampleFolder;
 	
 	static String libName;
 	
 	static HashMap<String,String> classLinks = new HashMap<String,String>();
 	
 	public static void report( String msg )
 	{
 		System.err.println( msg );
 	}
 
 	/**
 	 * This function is needed to start the generation of the documentation
 	 */
 	public static boolean start(RootDoc root)
 	{	
 		setFolders(root);
 		
 		try
 		{
 			for( PackageDoc pack : root.specifiedPackages() )
 			{
 				ClassDoc[] classes = pack.allClasses();
 				for (ClassDoc classDoc : classes)
 				{
 					if(classDoc.tags("@invisible").length == 0)
 					{
 						classLinks.put(classDoc.name(),new ClassTemplate().buildFileName(classDoc,classDoc.name()));
 					}
 				}
 			}
 			
 			for( PackageDoc pack : root.specifiedPackages() )
 			{
 				ProcessingDoclet pDoclet = new ProcessingDoclet( pack );
 				pDoclet.generate();
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return true;
 	}
 	
 	public static int optionLength(String option) 
 	{
         if(option.equals("-destdir")) 
         {
         	return 2;
         }
         
         return 0;
     }
 	
 	private static void setFolders(RootDoc root)
 	{
 		System.out.println( "Setting folders for prodoc..." );
 		
 		PackageDoc packageDoc = root.specifiedPackages()[0];
 		
 		Tag[] packageTags = packageDoc.tags("@libname");
 		if(packageTags.length > 0)
 		{
 			libName = packageTags[0].text();
 		}
 		else
 		{
 			libName = packageDoc.name();
 		}
 		
 		String libfolder = "";
 		String docletFolder = "";
 		String outputFolder = "documentation";
 		
 		//get the sourcepath from the sourcepath option setup with javadoc call
 		for( String[] options : root.options() )
 		{				
 			if (options[0].equals("-sourcepath") )
 			{
				libfolder = options[1].replace( "src", "" );
 			}
 			
 			if ( options[0].equals("-docletpath") )
 			{
				docletFolder = options[1].replace( "prodoc.jar", "" );
 			}
 			
 			if ( options[0].equals( "-destdir" ) )
 			{
 				outputFolder +=  "/" + options[1];
 			}
 		}
 		
 		libFolder = new File(libfolder + "/src");
 		
 		//gets the templatefolder from the library or takes the standard templates if there is non available
 		templateFolder = new File(libfolder,"templates");
 		
 		// look next to the jar
 		if ( !templateFolder.exists() )
 		{
			System.out.println( "Looking for templates folder next to the doclet folder: " + docletFolder );
 			templateFolder = new File(docletFolder,"templates");
 		}
 		
 		// look inside the jar
 		if( !templateFolder.exists() )
 		{
 			URL rootURL = StartDoclet.class.getResource( "" );
 			if ( rootURL != null )
 			{
 				templateFolder = new File(rootURL.getPath()+"/templates");
 			}
 			else 
 			{
 				System.out.println( "Couldn't get a rootURL to search for standard templates." );
 			}
 		}
 		
		System.out.println( "Final template folder is: " + templateFolder.toString() );
		
 		//get the docfolder and copies the ressource files from the templatefolder 
 		docFolder = new File(libfolder,outputFolder);
 		
 		if (!docFolder.exists())
 		{
 			docFolder.mkdir();
 		}
 				
 		try
 		{
 			copy(new File(templateFolder,"stylesheet.css"),new File(docFolder,"stylesheet.css"));
 			File images = new File(templateFolder,"images");
 			if( images.exists() )
 			{
 				File newImages = new File(docFolder,"images");
 				if( !newImages.exists() )
 				{
 					newImages.mkdir();
 				}
 				
 				for(String image:images.list())
 				{
 					copy(new File(images,image),new File(newImages,image));
 				}
 			}
 		}
 		catch (IOException e)
 		{
 			System.out.println("Problem with copying sourcefiles!");
 			e.printStackTrace();
 		}
 		
 		//get the example folder with the examples pde files for the documentation
 		exampleFolder = new File(libfolder,"examples");
 		
 		System.out.println( "Folders are all set..." );
 	}
 	
 	
 	private static void copy(File src, File dest) throws IOException{
 		copy(new FileInputStream(src), new FileOutputStream(dest));
 	}
 
 	private static void copy(InputStream fis, OutputStream fos) throws IOException{
 		byte buffer[] = new byte[0xffff];
 		int nbytes;
 
 		while ((nbytes = fis.read(buffer)) != -1){
 			fos.write(buffer, 0, nbytes);
 		}
 
 		if (fis != null)
 			fis.close();
 		if (fos != null)
 			fos.close();
 	}
 }
