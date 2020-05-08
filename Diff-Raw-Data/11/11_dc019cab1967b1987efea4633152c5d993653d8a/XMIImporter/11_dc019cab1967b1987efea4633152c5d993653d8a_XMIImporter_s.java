 
 package edu.common.dynamicextensions.xmi.importer;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.jmi.model.ModelPackage;
 import javax.jmi.model.MofPackage;
 import javax.jmi.xmi.XmiReader;
 
 import org.netbeans.api.mdr.MDRManager;
 import org.netbeans.api.mdr.MDRepository;
 import org.omg.uml.UmlPackage;
 import org.openide.util.Lookup;
 
 import edu.common.dynamicextensions.xmi.XMIUtilities;
 
 
 
 public class XMIImporter
 {
 	// name of a UML extent (instance of UML metamodel) that the UML models will be loaded into
 	private static final String UML_INSTANCE = "UMLInstance";
 	// name of a MOF extent that will contain definition of UML metamodel
 	private static final String UML_MM = "UML";
 
 	// repository
 	private static MDRepository rep;
 	// UML extent
 	private static UmlPackage uml;
 
 	// XMI reader
 	private static XmiReader reader;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) 
 	{
 		FileInputStream in = null;
 		try
 		{
 			if(args.length == 0)
 			{
 				throw new Exception("Please Specify the file name to be imported");
 			}	
 			
 			//Ist parameter is fileName
 	//		Fully qualified Name of the xmi file to be imported
 			String fileName = args[0];
 //				"C://Documents and Settings//ashish_gupta//Desktop//XMLs//roundtrip_1.4.xmi";	
 			fileName = fileName.replaceAll("\\\\", "//");		
 			System.out.println("Filename = " +fileName);
 			
 			String packageName = "";
 			if(args.length > 1)
 			{
 				packageName = args[1];
 			}
 			int beginIndex = fileName.lastIndexOf("//");
 			int endIndex = fileName.lastIndexOf(".");
			String domainModelName = fileName.substring(beginIndex+2, endIndex);
 			System.out.println("Package name = " +packageName);
 			System.out.println("Name of the file = " +domainModelName);
 			
 			// get the default repository
 			rep = MDRManager.getDefault().getDefaultRepository();
 			// create an XMIReader
 			reader = (XmiReader) Lookup.getDefault().lookup(XmiReader.class);
 			
 			init();
 	
 			in = new FileInputStream(fileName);
 	
 			// start a read-only transaction
 			rep.beginTrans(true);
 		
 			// read the document
 			reader.read(in, null, uml);
 			XMIImportProcessor xmiImportProcessor = new XMIImportProcessor();
 			
 			 List<String> containerNames = new ArrayList<String>();
 			xmiImportProcessor.processXmi(uml, domainModelName,packageName,containerNames);			
 			System.out.println("--------------- Done ------------");
 		
 		}
 		catch (Exception e)
 		{
 			System.out.println("Fatal error reading XMI.");
 			e.printStackTrace();
 		}
 		finally
 		{
 			// release the transaction
 			rep.endTrans();
 			MDRManager.getDefault().shutdownAll();
 			try
 			{
 				in.close();
 			}
 			catch (IOException e)
 			{
 				System.out.println("Error. Specified file does not exist.");
 			}
 			XMIUtilities.cleanUpRepository();
 		
 		}
 	}
 
 	private static void init() throws Exception
 	{
 		uml = (UmlPackage) rep.getExtent(UML_INSTANCE);
 
 		if (uml == null)
 		{
 			// UML extent does not exist -> create it (note that in case one want's to instantiate
 			// a metamodel other than MOF, they need to provide the second parameter of the createExtent
 			// method which indicates the metamodel package that should be instantiated)
 			uml = (UmlPackage) rep.createExtent(UML_INSTANCE, getUmlPackage());
 		}
 	}
 
 	/** Finds "UML" package -> this is the topmost package of UML metamodel - that's the
 	 * package that needs to be instantiated in order to create a UML extent
 	 */
 	private static MofPackage getUmlPackage() throws Exception
 	{
 		// get the MOF extent containing definition of UML metamodel
 		ModelPackage umlMM = (ModelPackage) rep.getExtent(UML_MM);
 		if (umlMM == null)
 		{
 			// it is not present -> create it
 			umlMM = (ModelPackage) rep.createExtent(UML_MM);
 		}
 		// find package named "UML" in this extent
 		MofPackage result = getUmlPackage(umlMM);
 		if (result == null)
 		{
 			// it cannot be found -> UML metamodel is not loaded -> load it from XMI
 			reader.read(UmlPackage.class.getResource("resources/01-02-15_Diff.xml").toString(),
 					umlMM);
 			// try to find the "UML" package again
 			result = getUmlPackage(umlMM);
 		}
 		return result;
 	}
 
 	/** Finds "UML" package in a given extent
 	 * @param umlMM MOF extent that should be searched for "UML" package.
 	 */
 	private static MofPackage getUmlPackage(ModelPackage umlMM)
 	{
 		// iterate through all instances of package
 		System.out.println("Here");
 		for (Iterator it = umlMM.getMofPackage().refAllOfClass().iterator(); it.hasNext();)
 		{
 			MofPackage pkg = (MofPackage) it.next();
 			System.out.println("\n\nName = " + pkg.getName());
 
 			// is the package topmost and is it named "UML"?
 			if (pkg.getContainer() == null && "UML".equals(pkg.getName()))
 			{
 				// yes -> return it
 				return pkg;
 			}
 		}
 		// a topmost package named "UML" could not be found
 		return null;
 	}
 
 }
