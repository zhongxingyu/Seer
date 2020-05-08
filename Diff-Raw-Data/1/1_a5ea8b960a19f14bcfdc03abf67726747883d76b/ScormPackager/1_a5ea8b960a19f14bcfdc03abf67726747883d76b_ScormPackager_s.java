 package au.edu.labshare.schedserver.scormpackager.service;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Properties;
 
 import au.edu.labshare.schedserver.scormpackager.sahara.RigLaunchPageCreator;
 import au.edu.labshare.schedserver.scormpackager.sahara.RigMedia;
 import au.edu.labshare.schedserver.scormpackager.types.CreatePIF;
 import au.edu.labshare.schedserver.scormpackager.types.CreatePIFResponse;
 import au.edu.labshare.schedserver.scormpackager.types.CreateSCO;
 import au.edu.labshare.schedserver.scormpackager.types.CreateSCOResponse;
 import au.edu.labshare.schedserver.scormpackager.types.DeletePIF;
 import au.edu.labshare.schedserver.scormpackager.types.DeletePIFResponse;
 import au.edu.labshare.schedserver.scormpackager.types.DeleteSCO;
 import au.edu.labshare.schedserver.scormpackager.types.DeleteSCOResponse;
 import au.edu.labshare.schedserver.scormpackager.types.ValidateManifest;
 import au.edu.labshare.schedserver.scormpackager.types.ValidateManifestResponse;
 import au.edu.labshare.schedserver.scormpackager.types.ValidatePIF;
 import au.edu.labshare.schedserver.scormpackager.types.ValidatePIFResponse;
 import au.edu.labshare.schedserver.scormpackager.types.ValidateSCO;
 import au.edu.labshare.schedserver.scormpackager.types.ValidateSCOResponse;
 import au.edu.labshare.schedserver.scormpackager.utilities.ScormUtilities;
 import au.edu.labshare.schedserver.scormpackager.lila.ManifestXMLDecorator;
 import au.edu.labshare.schedserver.scormpackager.lila.ShareableContentObjectCreator;
 
 //import au.edu.uts.eng.remotelabs.schedserver.dataaccess.DataAccessActivator;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigTypeDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigTypeMedia;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 
 
 
 public class ScormPackager implements ScormPackagerSkeletonInterface
 {
 	 /** Logger. */
     private Logger logger;
     //private org.hibernate.Session session;
 
     public ScormPackager()
     {
         this.logger = LoggerActivator.getLogger();
         //this.session = DataAccessActivator.getNewSession();
     }
 
 	@Override
 	public CreatePIFResponse createPIF(CreatePIF createPIF) 
 	{		
 		au.edu.labshare.schedserver.scormpackager.types.CreateSCO SCOInfo = new au.edu.labshare.schedserver.scormpackager.types.CreateSCO();
 		SCOInfo.setContent(createPIF.getContent());
 		SCOInfo.setExperimentName(createPIF.getExperimentName());
 		CreateSCOResponse SCOResponse = createSCO(SCOInfo);
 		
 		//Setup the response with the data to return back to the user
 		CreatePIFResponse createPIFResponse = new CreatePIFResponse();
 		createPIFResponse.setPathPIF(SCOResponse.getPathSCO());
 		
 		return createPIFResponse;
 	}
 
 	@Override
 	public CreateSCOResponse createSCO(CreateSCO createSCO) 
 	{
 		String pathOfSCO = null;
         Properties defaultProps = new Properties();
         FileInputStream in;
         String cwd = null;
 		
 		LinkedList<File> content = new LinkedList<File>();
 		
 		//Start by adding extra content that was provided by user. Will not set if it is null
 		if(createSCO.getContent() != null)
 			content = ScormUtilities.getFilesFromPath(createSCO.getContent());
 	
 		try
 		{
 			cwd = new java.io.File( "." ).getCanonicalPath();
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace(); //TODO: Need to replace with Sahara Logger
 		}
 		
 		//Add the lmsstub.js
 		if(!cwd.contains("ScormPackager"))
 			content.add(new File(cwd + "/ScormPackager/" + ManifestXMLDecorator.RESOURCES_PATH + "/lmsstub.js"));
 		else
 			content.add(new File(ManifestXMLDecorator.RESOURCES_PATH + "/lmsstub.js"));
 		
 		//We want to get the content from the Rig DB Persistence end
         org.hibernate.Session db = new RigTypeDao().getSession();
         RigMedia saharaRigMedia = new RigMedia(db);
         
         //Go through the rig media information and add any data that is in them
         Iterator<RigTypeMedia> iter;
         if(saharaRigMedia.getRigType(createSCO.getExperimentName()) != null)
         	iter = saharaRigMedia.getRigType(createSCO.getExperimentName()).getMedia().iterator();
         else 
         {
         	CreateSCOResponse errorSCOResponse = new CreateSCOResponse();
         	errorSCOResponse.setPathSCO("NON EXISTENT RIGTYPE - SCORM WEB SERVICE ERROR"); //TODO: Place this as a status code static string
         	return errorSCOResponse;
         }
         
         while(iter.hasNext())
         	content.add(new File(iter.next().getFileName()));
         
         // We want to generate a LaunchPage (launchpage.html) that is to be added to SCO
         RigLaunchPageCreator rigLaunchPageCreator = new RigLaunchPageCreator();
 
         // create and load default properties
 		try 
 		{
 			if(!cwd.contains("ScormPackager"))
 				in = new FileInputStream(cwd + "/ScormPackager/" + "resources/scormpackager.properties"); //TODO: Should place this as a static string
 			else
 				in = new FileInputStream("resources/scormpackager.properties"); //TODO: Should place this as a static string
 	       
 			defaultProps.load(in);
 	        in.close();
 		} 
 		catch (Exception e) 
 		{
 			e.printStackTrace(); //TODO: Need to replace with Sahara Logger
 		}
 
 		//TODO: Should place this as static string - scormpackager_output_path
 		pathOfSCO = (String) defaultProps.getProperty("scormpackager_output_path");
 
 		//Add the content - i.e. Add launchPage with Experiment/Rig name
         rigLaunchPageCreator.setOutputPath(pathOfSCO + ScormUtilities.replaceWhiteSpace(createSCO.getExperimentName(),"_") + ".html"); 
         content.add(new File(rigLaunchPageCreator.createLaunchPage(createSCO.getExperimentName(), db)));
         
 		//Create the SCO to be sent out
 		ShareableContentObjectCreator shrContentObj = new ShareableContentObjectCreator(logger);
 		shrContentObj.createSCO(createSCO.getExperimentName(), content, pathOfSCO);
 		
 		//Setup the response with the data to return back to the user
 		CreateSCOResponse createSCOResponse = new CreateSCOResponse();
 		createSCOResponse.setPathSCO(pathOfSCO);
 		
 		return createSCOResponse;
 	}
 
 	@Override
 	public DeletePIFResponse deletePIF(DeletePIF deletePIF) 
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public DeleteSCOResponse deleteSCO(DeleteSCO deleteSCO) 
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public ValidateManifestResponse validateManifest(ValidateManifest validateManifest) 
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public ValidatePIFResponse validatePIF(ValidatePIF validatePIF) 
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public ValidateSCOResponse validateSCO(ValidateSCO validateSCO) 
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
