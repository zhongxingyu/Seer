 /**
  * 
  */
 package org.concord.otrunk.test;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.net.MalformedURLException;
 
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.datamodel.OTDatabase;
 import org.concord.otrunk.view.OTFolder;
 import org.concord.otrunk.view.OTViewerHelper;
 
 /**
  * @author scott
  *
  */
 public class CopyObjectTest {
 
 	/**
 	 * @param args
 	 * @throws Exception 
 	 * @throws MalformedURLException 
 	 */
 	public static void main(String[] args) 
 		throws MalformedURLException, Exception 
 	{
 		// load in the first argument as an otml file
 		// assume the root object is a folder, and then 
 		// get the first child of the folder and
 		// copy it and store the copy as the second
 		// object in the folder
 		OTViewerHelper viewerHelper = new OTViewerHelper();
 		
 		File input = new File(args[0]);
 		OTDatabase mainDb = viewerHelper.loadOTDatabase(input.toURL());
 
 		viewerHelper.loadOTrunk(mainDb, null);
		
 		OTFolder root = (OTFolder)viewerHelper.getRootObject();
 		OTObject first = (OTObject)root.getChild(0);
 		
 		OTrunkImpl otrunk = (OTrunkImpl)viewerHelper.getOtrunk();
 		OTObjectService objService = otrunk.getRootObjectService();
 		
 		OTObject second = objService.copyObject(first, -1);
 		
 		root.addChild(second);
 		
 		FileOutputStream output = new FileOutputStream(args[1]);
 		viewerHelper.saveOTDatabase(mainDb, output);
 		output.close();
 		
 		System.exit(0);
 	}
 
 }
