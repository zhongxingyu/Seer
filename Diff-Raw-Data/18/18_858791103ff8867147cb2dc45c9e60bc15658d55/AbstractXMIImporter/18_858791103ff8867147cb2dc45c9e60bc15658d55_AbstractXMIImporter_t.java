 
 package edu.common.dynamicextensions.xmi.importer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.util.StringTokenizer;
 
 import javax.jmi.model.ModelPackage;
 import javax.jmi.model.MofPackage;
 import javax.jmi.xmi.XmiReader;
 
 import org.netbeans.api.mdr.CreationFailedException;
 import org.netbeans.api.mdr.MDRepository;
 import org.omg.uml.UmlPackage;
 import org.openide.util.Lookup;
 
 import edu.common.dynamicextensions.client.DEClient;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ConstraintPropertiesInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.entitymanager.DynamicExtensionBaseQueryBuilder;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.entitymanager.EntityManagerUtil;
 import edu.common.dynamicextensions.entitymanager.QueryBuilderFactory;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManagerConstants;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.MetaDataIntegrator;
 import edu.common.dynamicextensions.util.QueryIntegrator;
 import edu.common.dynamicextensions.util.SaveEntityGroupAndDETablesUtil;
 import edu.common.dynamicextensions.util.global.DEConstants;
 import edu.common.dynamicextensions.util.global.Variables;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationDirection;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationType;
 import edu.common.dynamicextensions.util.global.DEConstants.Cardinality;
import edu.common.dynamicextensions.util.parser.ImportPermissibleValues;
 import edu.common.dynamicextensions.xmi.AnnotationUtil;
 import edu.common.dynamicextensions.xmi.DynamicQueryList;
 import edu.common.dynamicextensions.xmi.PathObject;
 import edu.common.dynamicextensions.xmi.UpdateCSRToEntityPath;
 import edu.common.dynamicextensions.xmi.XMIConfiguration;
 import edu.common.dynamicextensions.xmi.XMIConstants;
 import edu.common.dynamicextensions.xmi.XMIUtilities;
 import edu.common.dynamicextensions.xmi.exporter.XMIExporter;
 import edu.common.dynamicextensions.xmi.exporter.XMIExporterUtility;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.common.util.logger.LoggerConfig;
 import edu.wustl.dao.DAO;
 import edu.wustl.dao.HibernateDAO;
 import edu.wustl.dao.JDBCDAO;
 import edu.wustl.dao.exception.DAOException;
 
 /**
  * This class is used for importing the Dynamic Models using the DE.
  * It will take care of importing model creating their tables associating with hook entity
  * & then adding the Query paths.
  * @author pavan_kalantri
  *
  */
 public abstract class AbstractXMIImporter
 {
 
 	static
 	{
 		LoggerConfig.configureLogger(System.getProperty("user.dir"));
 	}
 
 	private static final Logger LOGGER = Logger.getCommonLogger(AbstractXMIImporter.class);
 
 	// name of a UML extent (instance of UML metamodel) that the UML models will be loaded into
 	private static final String UML_INSTANCE = "UMLInstance";
 	// name of a MOF extent that will contain definition of UML metamodel
 	private static final String UML_MM = "UML";
 
 	private final static String STORAGE_FILE_NAME = "importer";
 	// repository
 	private MDRepository rep;
 	// UML extent
 	private UmlPackage uml;
 
 	// XMI reader
 	private XmiReader reader;
 
 	private XMIConfiguration xmiConfiguration = null;
 
 	private String fileName = "";
 	private String packageName = "";
 	private String pathCsvFileName = "";
 	private String coRecObjCsvFName = "";
	private String pvFile="";
 
 	private String hookEntityName = "";
 	private final Set<AssociationInterface> intermodelAssociationCollection = new HashSet<AssociationInterface>();
 	private EntityInterface hookEntity;
 	private boolean isAddQueryPaths = true;
 	private boolean addQueryPaths = true;
 	private boolean isEntGrpSysGented = false;
 	private boolean isGenerateCacore = true;
 	private HibernateDAO hibernatedao = null;
 	private JDBCDAO jdbcdao = null;
 	private String domainModelName = "";
 	XMIImportValidator xmiImportValidator= new XMIImportValidator();
 	MetaDataIntegrator metaDataIntegrator= new MetaDataIntegrator();
 	private static final String TIME_TAKEN = "  Time taken  = ";
 	private static final String MINUTES = " minutes ";
 	private static final String SECONDS = "  seconds";
 	private static final String MESSAGE_SEPARATOR = "#############################################";
 
 	/**
 	 * Step 1  : initialize all resources
 	 * Step 2: Process XMI
 	 * Step 3: Associate with Hook Entity
 	 * step 4: Execute Queries & commit all model.
 	 * Step 5: Add Query paths.
 	 * step 6: Post process the model i.e whatever host application wants to do
 	 * can be done here (e.g. Associate main containers with clinical Study).
 	 * @param args command line arguments
 	 */
 	public void importXMI(String[] args)
 	{
 		FileInputStream fileInput = null;
 		List<ContainerInterface> mainContainerList = null;
 		boolean isImportSuccess = true;
 
 		try
 		{
 			//step 1: Initialize Resources
 			long processStartTime = System.currentTimeMillis();
 			initializeResources(args);
 			File file = new File(fileName);
 			fileInput = new FileInputStream(file);
 			reader.read(fileInput, null, uml);
 			List<String> containerNames = readFile(pathCsvFileName);
 			//step 2: Process XMI
 			XMIImportProcessor xmiImportProcessor = new XMIImportProcessor();
 			xmiImportProcessor.setXmiConfigurationObject(xmiConfiguration);
 			xmiImportValidator=xmiImportProcessor.getXmiImportValidator();
 			long processXMIStartTime = System.currentTimeMillis();
 			DynamicQueryList dynamicQueryList = xmiImportProcessor.processXmi(uml, domainModelName,
 					packageName, containerNames, hibernatedao);
 
 			mainContainerList = xmiImportProcessor.getMainContainerList();
 			Map<AssociationInterface, String> multiselectMigartionScripts = xmiImportProcessor
 			.getMultiselectMigartionScripts();
 			boolean isEditedXmi = xmiImportProcessor.isEditedXmi;
 			if (isEditedXmi)
 			{
 				lockFroms(xmiImportProcessor.getEntityGroup().getName());
 			}
 			generateLogForProcessXMI(processXMIStartTime, isEditedXmi);
 			long assoWithHEstartTime = System.currentTimeMillis();
 
 			//Step 3: associate with hook entity.
 			metaDataIntegrator.setIntermodelAssociationCollection(intermodelAssociationCollection);
 			metaDataIntegrator.integrateWithHookEntity(hookEntityName, hibernatedao, dynamicQueryList, mainContainerList, isEditedXmi);
 			//integrateWithHookEntity(hibernatedao, dynamicQueryList, mainContainerList, isEditedXmi);
 			//step 4: commit model & create DE Tables
 			/*LOGGER.info("Now Creating DE Tables....");
 			if (hibernatedao != null)
 			{
 				createDETablesAndSaveEntityGroup(hibernatedao,multiselectMigartionScripts, dynamicQueryList);
 			}*/
 			SaveEntityGroupAndDETablesUtil saveGroupandDETablesUtil = new SaveEntityGroupAndDETablesUtil();
 			saveGroupandDETablesUtil.createDETablesAndSaveEntityGroup(hibernatedao, multiselectMigartionScripts, dynamicQueryList);
 			hibernatedao.commit();
 			//step 5: add Query paths.
 		/*	if (addQueryPaths)
 			{
 				LOGGER.info("Now Adding Query Paths ....");
 				addQueryPathsForConatiners(hookEntityName,hibernatedao, jdbcdao, isEntGrpSysGented,
 						mainContainerList);
 			}*/
 			QueryIntegrator queryPaths= new QueryIntegrator();
 			queryPaths.addQueryPaths(addQueryPaths, hookEntityName, hibernatedao, jdbcdao, isEditedXmi, mainContainerList);
 			System.out.println("**********done with addQueryPaths");
 			jdbcdao.commit();
 			//step 6: associate with clinical study.
 			LOGGER.info("Now associating the clinical study to the main Containers");
 			postProcess(isEditedXmi, coRecObjCsvFName, mainContainerList, domainModelName);
 			generateValidationLogs();
			//import pv offline 
			//Specify pv file path as -Dpv.file.name
			//Sanjay Sengupta
			if(!pvFile.equals(""))
			{
				ImportPermissibleValues.main(new String[] {pvFile});
				
			}
 		}
 		catch (Exception e)
 		{
 			LOGGER.fatal("Fatal error reading XMI!!", e);
 			isImportSuccess = false;
 			e.printStackTrace();
 			generateValidationLogs();
 			if (!xmiImportValidator.getErrorList().isEmpty() && xmiConfiguration.isValidateXMI())
 			{
 				throw new RuntimeException(e);
 			}
 		}
 		finally
 		{
 			closeTransaction(fileInput, hibernatedao, jdbcdao);
 		}
 		if (isImportSuccess)
 		{
 			LOGGER.info("updating server cache");
 
 			updateCache(mainContainerList);
 			exportXmiForCacore(mainContainerList);
 		}
 	}
 
 	private void updateCache(List<ContainerInterface> mainContainerList)
 
 	{
 		try{
 		//step 7: update server cache
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put(WebUIManagerConstants.ENTITY, hookEntityName);
 		map.put(WebUIManagerConstants.OPERATION, WebUIManagerConstants.UPDATE_CACHE);
 		map.put(WebUIManagerConstants.ASSOCIATION, getDummyAssociations(intermodelAssociationCollection));
 		map.put(WebUIManagerConstants.ENTITY_GROUP, ((EntityInterface) mainContainerList.get(0)
 				.getAbstractEntity()).getEntityGroup());
 
 		DEClient client = new DEClient();
 		client.setParamaterObjectMap(map);
 		client.setServerUrl(getServerUrl());
 		client.execute(null);
 		}
 		catch(MalformedURLException exception)
 		{
 			LOGGER.error(exception.getCause());
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			LOGGER.info("Problem in updating cache.");
 			LOGGER.error(e.getCause());
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			LOGGER.info("Problem in updating cache.");
 			LOGGER.error(e.getCause());
 		}
 	}
 
 	private Collection<AssociationInterface>  getDummyAssociations(Set<AssociationInterface> intermodelAssociationCollection2) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Collection<AssociationInterface> dummyAssociationCollection = new HashSet<AssociationInterface>();
 		for(AssociationInterface association:intermodelAssociationCollection2)
 		{
 			dummyAssociationCollection.add(createDummyAssociation(association));
 		}
 		return dummyAssociationCollection;
 	}
 
 	/**
 	 * This method checks whether Application URL has '/' appended or not and accordingly returns the URL object
 	 * @return
 	 * @throws MalformedURLException
 	 */
 	private URL getServerUrl() throws MalformedURLException
 	{
 		StringBuilder url = new StringBuilder(Variables.serverUrl);
 		if (!Variables.serverUrl.endsWith("/"))
 		{
 			url.append("/");
 		}
 		url.append("UpdateCache");
 		return new URL(url.toString());
 	}
 
 	/**
 	 * Lock froms.
 	 * @param entityGroupName
 	 * @throws DynamicExtensionsApplicationException the dynamic extensions application exception
 	 */
 	private void lockFroms(String entityGroupName)
 			throws DynamicExtensionsApplicationException
 	{
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put(WebUIManagerConstants.ENTITY_GROUP, entityGroupName);
 		map.put(WebUIManagerConstants.OPERATION, WebUIManagerConstants.LOCK_FORMS);
 		DEClient client = new DEClient();
 		client.setParamaterObjectMap(map);
 		try
 		{
 			client.setServerUrl(getServerUrl());
 		}
 		catch (MalformedURLException e)
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Error in locking forms on the server cache", e);
 		}
 		client.execute(null);
 
 	}
 
 	/**
 	 * adds the query paths for the given main container list.
 	 * @param mainContainerList containers for which to add paths.
 	 * @throws DynamicExtensionsSystemException exception
 	 * @throws DynamicExtensionsApplicationException exception
 	 * @throws BizLogicException exception
 	 * @throws DAOException exception
 	 */
 	private void addQueryPaths(List<ContainerInterface> mainContainerList)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException,
 			BizLogicException, DAOException
 	{
 		long csrStartTime = System.currentTimeMillis();
 
 		//step 5: add Query paths.
 		if (isAddQueryPaths)
 		{
 			LOGGER.info("Now Adding Query Paths ....");
 			addQueryPathsForConatiners(hibernatedao, jdbcdao, isEntGrpSysGented, mainContainerList);
 		}
 		generateLog("  IMPORT_XMI --> TASK : ADD QUERY PATHS", csrStartTime);
 	}
 
 	/**
 	 *
 	 */
 	private void generateValidationLogs()
 	{
 		if (!xmiImportValidator.getErrorList().isEmpty())
 		{
 			LOGGER.error("==========================================");
 			LOGGER.error("Following ERRORS encountered in the XMI: ");
 			LOGGER.error("==========================================");
 			for (String error : xmiImportValidator.getErrorList())
 			{
 				LOGGER.error(error);
 			}
 		}
 	}
 
 	/**
 	 * It will export the currently Imported model in XMI v 1.1. for generating cacore.
 	 * @param mainContainerList main container list.
 	 */
 	private void exportXmiForCacore(List<ContainerInterface> mainContainerList)
 	{
 		if (mainContainerList == null)
 		{
 			LOGGER.info("Main container list is empty hence not exporting the XMI for cacore!");
 		}
 		else if (isGenerateCacore)
 		{
 			long processStartTime = System.currentTimeMillis();
 			LOGGER.info("Now Exporting xmi for cacore !!");
 			try
 			{
 				EntityGroupInterface entityGroup = ((EntityInterface) mainContainerList.get(0)
 						.getAbstractEntity()).getEntityGroup();
 				if (metaDataIntegrator.getHookEntity()!= null)
 				{
 					XMIExporterUtility.addHookEntitiesToGroup(metaDataIntegrator.getHookEntity(), entityGroup);
 				}
 				XMIExporter exporter = new XMIExporter();
 				String exportedXmiFilePath = "./temp_deaudit_related_files/temp_exported_xmi/";
 				String[] arguments = {entityGroup.getName(),
 						exportedXmiFilePath + domainModelName + ".xmi",
 						XMIConstants.XMI_VERSION_1_1, hookEntityName};
 				exporter.initilizeInstanceVariables(arguments);
 				exporter.exportXMI(entityGroup, null);
 				generateLog("  IMPORT_XMI --> TASK : EXPORT_XMI_FOR_CACORE", processStartTime);
 			}
 			catch (Exception e)
 			{
 				LOGGER.fatal("Fatal error while exporting XMI for caCore!!", e);
 			}
 		}
 	}
 
 	private void generateLog(String message, long processStartTime)
 	{
 		long processEndTime = System.currentTimeMillis();
 		long totalTime = processEndTime - processStartTime;
 		LOGGER.info(" ");
 		LOGGER.info(" ");
 		LOGGER.info(MESSAGE_SEPARATOR);
 		LOGGER.info(message);
 		LOGGER.info(" ");
 		LOGGER.info(TIME_TAKEN + totalTime / 1000 / 60 + MINUTES + totalTime / 1000 % 60 + SECONDS);
 		LOGGER.info(MESSAGE_SEPARATOR);
 		LOGGER.info(" ");
 		LOGGER.info(" ");
 	}
 
 	/**
 	 * It will create the Logg statement for timing required for importing the model.
 	 * @param processXMIStartTime
 	 * @param isEditedXmi
 	 */
 	private void generateLogForProcessXMI(long processXMIStartTime, boolean isEditedXmi)
 	{
 		long processXMIEndTime = System.currentTimeMillis();
 		long processXMITotalTime = processXMIEndTime - processXMIStartTime;
 		LOGGER.info(" ");
 		LOGGER.info(MESSAGE_SEPARATOR);
 		LOGGER.info("  IMPORT_XMI --> TASK : IMPORT DYNAMIC MODEL");
 		LOGGER.info("  --------------------------------------");
 		LOGGER.info("  Time taken = " + processXMITotalTime / 1000 / 60 + MINUTES
 				+ processXMITotalTime / 1000 % 60 + SECONDS);
 		LOGGER.info(MESSAGE_SEPARATOR);
 		LOGGER.info(" ");
 
 		LOGGER.info(" ");
 		LOGGER.info(" ");
 		LOGGER.info("EDIT_XMI case = " + (isEditedXmi ? "Y" : "N"));
 		LOGGER.info(" ");
 		LOGGER.info(" ");
 	}
 
 	/**
 	 * It will logg all the arguments passed to import the model.
 	 */
 	private void loggAllInstanceVariables()
 	{
 		LOGGER.info("************IMPORT XMI PARAMETERS*********************");
 		LOGGER.info("File name = " + fileName);
 		LOGGER.info("CSV File name = " + pathCsvFileName);
 		LOGGER.info("Package name = " + packageName);
 		LOGGER.info("Add query paths = " + isAddQueryPaths);
 		LOGGER.info("Condition record object CSV File name = " + coRecObjCsvFName);
 		LOGGER.info("Hook entity = " + hookEntityName);
 		LOGGER.info("Domain model name = " + domainModelName);
 		LOGGER.info("Generate caCore = " + isGenerateCacore);
 		LOGGER.info("******************************************************");
 	}
 
 	/**
 	 * It will validate all the arguments & initialize all the resources which
 	 * are required for importing the model.
 	 * @param args cammond line arguments.
 	 * @throws CreationFailedException
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DAOException
 	 * @throws Exception exception
 	 */
 	private void initializeResources(String[] args) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException, CreationFailedException
 	{
 		validate(args);
 		intitializeInstanceVaribles(args);
 		xmiConfiguration = getXMIConfigurationObject();
 		domainModelName = getDomainModelName(fileName);
 		xmiImportValidator.validatePackageName(packageName, domainModelName);
 		if (hookEntityName.equalsIgnoreCase("None"))
 		{
 			xmiConfiguration.setEntityGroupSystemGenerated(true);
 			isEntGrpSysGented = true;
 		}
 		// get the default repository
 		rep = XMIUtilities.getRepository(STORAGE_FILE_NAME);
 		// create an XMIReader
 		reader = (XmiReader) Lookup.getDefault().lookup(XmiReader.class);
 		init();
 		// start a read-only transaction
 		rep.beginTrans(true);
 		intializeDao();
 		loggAllInstanceVariables();
 	}
 
 	/**
 	 * It will initialize all the instance variables with the given arguments.
 	 * @param argscammond line arguments.
 	 */
 	private void intitializeInstanceVaribles(String[] args)
 	{
 		fileName = args[0];
 		if (args.length > 1 && args[1].trim().length() > 0)
 		{
 			pathCsvFileName = args[1];
 		}
 		if (args.length > 2 && args[2].trim().length() > 0)
 		{
 			packageName = args[2];
 		}
 		if (args.length > 3 && args[3].trim().length() > 0)
 		{
 			hookEntityName = args[3];
 		}
 		if (args.length > 4 && args[4].trim().length() > 0)
 		{
 			isAddQueryPaths = Boolean.valueOf(args[4]);
 		}
 		if (args.length > 5 && args[5].trim().length() > 0)
 		{
 			coRecObjCsvFName = args[5];
 		}
 		if (args.length > 6 && DEConstants.FALSE.equalsIgnoreCase(args[6].trim()))
 		{
 			isGenerateCacore = false;
 		}
 		if (args.length > 7 && args[7].trim().length() > 0)
 		{
 			Variables.serverUrl = args[7];
 		}
		if (args.length > 8 && args[8].trim().length() > 0)
		{
			pvFile = args[8];
		}
 	}
 
 	/**
 	 * It will validate weather the correct number of arguments are passed or not & then throw exception accordingly.
 	 * @param args arguments
 	 * @throws DynamicExtensionsApplicationException exception
 	 */
 	private static void validate(String[] args) throws DynamicExtensionsApplicationException
 	{
 		if (args.length == 0)
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Please Specify the file name to be imported");
 		}
 		if (args.length < 2)
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Please Specify the Main Container names CSV file name.");
 		}
 		if (args.length < 3)
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Please Specify the name of the Package to be imported");
 		}
 		if (args[0] != null && args[0].trim().length() == 0)
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Please Specify the file name to be imported");
 		}
 		if (args[1] != null && args[1].trim().length() == 0)
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Please Specify the Main Container names CSV file name.");
 		}
 		if (args[2] != null && args[2].trim().length() == 0)
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Please Specify the name of the Package to be imported");
 		}
 	}
 
 	/**
 	 * It will create the instances of the hibernate & jdbc DAOs for furthure use.
 	 * @throws DAOException exception
 	 */
 	private void intializeDao() throws DynamicExtensionsSystemException
 	{
 
 		jdbcdao = DynamicExtensionsUtility.getJDBCDAO();
 		hibernatedao = DynamicExtensionsUtility.getHibernateDAO();
 
 	}
 
 	/**
 	 * It will close all the open resource like input stream & daos.
 	 * @param fileInput file input stream.
 	 * @param hibernatedao hibernate dao
 	 * @param jdbcdao jdbcdao
 	 */
 	private void closeTransaction(FileInputStream fileInput, HibernateDAO hibernatedao,
 			JDBCDAO jdbcdao)
 	{
 		rep.endTrans();
 		rep.shutdown();
 		//MDRManager.getDefault().shutdownAll();
 		try
 		{
 			DynamicExtensionsUtility.closeDAO(hibernatedao);
 			DynamicExtensionsUtility.closeDAO(jdbcdao);
 			if (fileInput != null)
 			{
 				fileInput.close();
 			}
 
 		}
 		catch (Exception e)
 		{
 			LOGGER.fatal("Fatal error reading XMI!!", e);
 		}
 		XMIUtilities.cleanUpRepository(STORAGE_FILE_NAME);
 	}
 
 	/**
 	 * It will hook all the mainContainers with the Provided Hook entity.
 	 * @param hibernatedao dao used for retrieving the hook entity.
 	 * @param dynamicQueryList query list in which to add the Queries for adding the association column
 	 * @param mainContainerList main containers which should be hooked.
 	 * @param isEditedXmi is it the edit xmi case.
 	 * @throws DAOException exception
 	 * @throws DynamicExtensionsSystemException exception
 	 * @throws BizLogicException exception
 	 * @throws DynamicExtensionsApplicationException exception.
 	 */
 	private void integrateWithHookEntity(HibernateDAO hibernatedao,
 			DynamicQueryList dynamicQueryList, List<ContainerInterface> mainContainerList,
 			boolean isEditedXmi) throws DAOException, DynamicExtensionsSystemException,
 			BizLogicException, DynamicExtensionsApplicationException
 	{
 		if (!hookEntityName.equalsIgnoreCase("None"))
 		{
 			// For CLINPORTAL, there is only one hook entity object i.e. RECORD ENTRY
 			//LOGGER.info("Number of main containers = " + mainContainerList.size());
 			LOGGER.info(" ");
 			LOGGER.info("Now associating with hook entity -> " + hookEntityName + "....");
 			LOGGER.info(" ");
 			DynamicQueryList queryList = associateHookEntity(mainContainerList, isEditedXmi,
 					hibernatedao);
 			if (queryList != null)
 			{
 				dynamicQueryList.getQueryList().addAll(queryList.getQueryList());
 				dynamicQueryList.getRevQueryList().addAll(queryList.getRevQueryList());
 			}
 		}
 	}
 
 	/**
 	 * It will add the Query paths for all the Entities & if addQueryPahs argument was
 	 * true then it will add the currated paths from clinicalStudyRegistration to mainConatainers.
 	 * @param hibernatedao dao used for retrieving the ids of entities.
 	 * @param jdbcdao dao used to insert the path.
 	 * @param isEntGrpSysGented specifies weather the entityGroup is system generated or not.
 	 * @param mainContainerList list of main containers for which the paths to be added.
 	 * @throws DynamicExtensionsSystemException exception
 	 * @throws DynamicExtensionsApplicationException exception
 	 * @throws BizLogicException exception
 	 * @throws DAOException exception
 	 */
 	private void addQueryPathsForConatiners(HibernateDAO hibernatedao, JDBCDAO jdbcdao,
 			boolean isEntGrpSysGented, List<ContainerInterface> mainContainerList)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException,
 			BizLogicException, DAOException
 	{
 		if (hookEntityName.equalsIgnoreCase("None"))
 		{
 			LOGGER.info("Main Container list size " + mainContainerList.size());
 			Set<PathObject> processedPathList = new HashSet<PathObject>();
 			AnnotationUtil.setHookEntityId(null);
 			addQueryPathsWithoutHookEntity(jdbcdao, isEntGrpSysGented, mainContainerList,
 					processedPathList);
 
 		}
 		else
 		{
 			EntityInterface staticEntity = XMIUtilities.getStaticEntity(hookEntityName,
 					hibernatedao);
 			for (ContainerInterface mainContainer : mainContainerList)
 			{
 				AnnotationUtil.addNewPathsForExistingMainContainers(staticEntity,
 						((EntityInterface) mainContainer.getAbstractEntity()), true, jdbcdao,
 						staticEntity);
 			}
 
 		}
 
 		LOGGER.info("Now adding CSR query paths for entities....");
 		List<AssociationInterface> associationList = getAssociationListForCurratedPath(hibernatedao);
 		UpdateCSRToEntityPath.addCuratedPathsFromToAllEntities(associationList, xmiConfiguration
 				.getNewEntitiesIds());
 
 	}
 
 	/**
 	 * It will add the Query paths for which no hook entity is specified.
 	 * @param jdbcdao dao used to insert the path.
 	 * @param isEntGrpSysGented specifies weather the entityGroup is system generated or not.
 	 * @param mainContainerList list of main containers for which the paths to be added.
 	 * @param processedPathList list of the paths which are already added.
 	 * @throws BizLogicException exception
 	 * @throws DynamicExtensionsSystemException exception.
 	 */
 	private void addQueryPathsWithoutHookEntity(JDBCDAO jdbcdao, boolean isEntGrpSysGented,
 			List<ContainerInterface> mainContainerList, Set<PathObject> processedPathList)
 			throws BizLogicException, DynamicExtensionsSystemException
 	{
 		for (ContainerInterface mainContainer : mainContainerList)
 		{
 			AnnotationUtil.addQueryPathsForAllAssociatedEntities(((EntityInterface) mainContainer
 					.getAbstractEntity()), null, null, processedPathList, isEntGrpSysGented,
 					jdbcdao);
 		}
 
 		// Following will add Parent Entity's association paths to child Entity also.
 		for (ContainerInterface mainContainer : mainContainerList)
 		{
 			AnnotationUtil.addInheritancePathforSystemGenerated(((EntityInterface) mainContainer
 					.getAbstractEntity()));
 		}
 	}
 
 	/**
 	 * It will execute all the queries present in the dynamicQueryList object &
 	 * multiselectMigartionScripts then will commit the hibernatedao.
 	 * If some problem occurs during this then it will roll back the dao &
 	 * also execute the revrese queries present in the dynamicQueryList.
 	 * @param multiselectMigartionScripts queries to be fired to migrate multiselect attribute.
 	 * @param dynamicQueryList queries to be fired to create all DE tables.
 	 * @throws DynamicExtensionsSystemException exception.
 	 */
 	private void createDETablesAndSaveEntityGroup(
 			Map<AssociationInterface, String> multiselectMigartionScripts,
 			DynamicQueryList dynamicQueryList) throws DynamicExtensionsSystemException
 	{
 		Stack<String> rlbkQryStack = new Stack<String>();
 		try
 		{
 
 			if (dynamicQueryList != null)
 			{
 				DynamicExtensionBaseQueryBuilder queryBuilder = QueryBuilderFactory
 						.getQueryBuilder();
 				queryBuilder.executeQueries(dynamicQueryList.getQueryList(), dynamicQueryList
 						.getRevQueryList(), rlbkQryStack);
 			}
 			hibernatedao.commit();
 		}
 		catch (Exception e)
 		{
 			rollbackQueries(rlbkQryStack, e, hibernatedao);
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 		// Execute data migration scripts for attributes that were changed from a normal attribute to
 		// a multiselect attribute.
 		List<String> multiSelMigrationQueries = EntityManagerUtil
 				.updateSqlScriptToMigrateOldDataForMultiselectAttribute(multiselectMigartionScripts);
 		XMIImporterUtil.executeDML(multiSelMigrationQueries);
 
 	}
 
 	/**
 	 * It will call rollback on the provided DAO & then will execute the
 	 * Queries which are present int the revQryStack to restore the original state.
 	 * @param revQryStack stack which contains the Queries to be fired to restore the state.
 	 * @param exception exception occurred because of which rollback is called.
 	 * @param dao dao which is to be rollback.
 	 * @throws DynamicExtensionsSystemException exception
 	 */
 	protected void rollbackQueries(Stack<String> revQryStack, Exception exception, DAO dao)
 			throws DynamicExtensionsSystemException
 	{
 		String message = "";
 		rollbackDao(dao);
 		if (revQryStack != null && !revQryStack.isEmpty())
 		{
 			JDBCDAO jdbcDao = null;
 			try
 			{
 				jdbcDao = DynamicExtensionsUtility.getJDBCDAO();
 				executeQuriesInStack(revQryStack, jdbcDao);
 				jdbcDao.commit();
 			}
 			catch (DAOException exc)
 			{
 				message = exc.getMessage();
 				//logFatalError(exc, abstrMetadata);
 			}
 			finally
 			{
 
 				DynamicExtensionsUtility.closeDAO(jdbcDao);
 				//logDebug("rollbackQueries", DynamicExtensionsUtility.getStackTrace(exception));
 				DynamicExtensionsSystemException xception = new DynamicExtensionsSystemException(
 						message, exception);
 
 				//xception.setErrorCode(DYEXTN_S_000);
 				throw xception;
 			}
 		}
 	}
 
 	/**
 	 * It will execute all the Queries present into the stack using the provided dao
 	 * @param revQryStack which contains queries
 	 * @param jdbcDao dao
 	 * @throws DynamicExtensionsSystemException exception.
 	 */
 	private void executeQuriesInStack(Stack<String> revQryStack, JDBCDAO jdbcDao)
 			throws DynamicExtensionsSystemException
 	{
 		while (!revQryStack.empty())
 		{
 			String query = revQryStack.pop();
 			try
 			{
 				jdbcDao.executeUpdate(query);
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException(
 						"Exception occured while executing rollback queries.", e);
 			}
 		}
 	}
 
 	/**
 	 * It will call rollback on the given dao.
 	 * @param dao dao which is to be rollbacked.
 	 * @throws DynamicExtensionsSystemException exception.
 	 */
 	private void rollbackDao(DAO dao) throws DynamicExtensionsSystemException
 	{
 		if (dao != null)
 		{
 			try
 			{
 				dao.rollback();
 			}
 			catch (DAOException excep)
 			{
 				throw new DynamicExtensionsSystemException("Not able to rollback the transaction.",
 						excep);
 			}
 		}
 	}
 
 	/**
 	 * It will return the name of the domain model. i.e. it will return the name of the file
 	 * which is used for importing the model without the extension part.
 	 * @param fileName file name
 	 * @return domain model name
 	 */
 	private String getDomainModelName(String fileName)
 	{
 		File file = new File(fileName);
 		int indexOfExtension = file.getName().lastIndexOf(".");
 		String modelName;
 
 		if (indexOfExtension == -1)
 		{
 			modelName = file.getName();
 		}
 		else
 		{
 			modelName = file.getName().substring(0, indexOfExtension);
 		}
 		return modelName;
 	}
 
 	/**
 	 * It will add the association between the provided hook entity & all the maincontainers.
 	 * @param mainContainerList main container list.
 	 * @param hookentity hook entity name
 	 * @param isEditedXmi is edit xmi
 	 * @param hibernatedao dao used to retrieve the hook entity
 	 * @return the query list to add column .
 	 * @throws DAOException exception.
 	 * @throws DynamicExtensionsSystemException exception.
 	 * @throws BizLogicException exception.
 	 * @throws DynamicExtensionsApplicationException exception.
 	 */
 	private DynamicQueryList associateHookEntity(List<ContainerInterface> mainContainerList,
 			boolean isEditedXmi, HibernateDAO hibernatedao) throws DAOException,
 			DynamicExtensionsSystemException, BizLogicException,
 			DynamicExtensionsApplicationException
 	{
 		//hooked with the record Entry
 		DynamicQueryList queryList = null;
 		hookEntity = XMIUtilities.getStaticEntity(hookEntityName, hibernatedao);
 		if (isEditedXmi)
 		{
 			//Edit Case
 			List<ContainerInterface> newContainers = new ArrayList<ContainerInterface>();
 			List<ContainerInterface> existingContainers = new ArrayList<ContainerInterface>();
 			separateNewAndExistingContainers(mainContainerList, hookEntity, newContainers,
 					existingContainers);
 			if (!newContainers.isEmpty())
 			{
 				queryList = addNewIntegrationObjects(hookEntity, newContainers, hibernatedao);
 			}
 		}
 		else
 		{//Add Case
 			queryList = addNewIntegrationObjects(hookEntity, mainContainerList, hibernatedao);
 		}
 		return queryList;
 
 	}
 
 	/**
 	 * It will separate the containers in new containers list & existing main container list
 	 * according to weather they are already associated with hook entity or not.
 	 * @param mainContainerList containers which are to be separated.
 	 * @param staticEntity static entity.
 	 * @param newContainers list in which the new containers are populated
 	 * @param existingContainers list in which the existing containers are populated
 	 */
 	private void separateNewAndExistingContainers(List<ContainerInterface> mainContainerList,
 			EntityInterface staticEntity, List<ContainerInterface> newContainers,
 			List<ContainerInterface> existingContainers)
 	{
 		for (ContainerInterface mainContainer : mainContainerList)
 		{
 			boolean isAssonPresent = false;
 			Collection<AssociationInterface> allAssociations = staticEntity.getAllAssociations();
 			for (AssociationInterface association : allAssociations)
 			{
 				if (association.getTargetEntity().getId().compareTo(
 						mainContainer.getAbstractEntity().getId()) == 0)
 				{
 					isAssonPresent = true;
 					break;
 				}
 			}
 			if (isAssonPresent)
 			{
 				existingContainers.add(mainContainer);
 			}
 			else
 			{
 				newContainers.add(mainContainer);
 			}
 
 		}
 	}
 
 	/**
 	 * It will add the new association between static entity & each of the main containers entity.
 	 * @param staticEntity source entity of the association to be added.
 	 * @param mainContainerList main container list .
 	 * @param hibernatedao dao
 	 * @return query list
 	 * @throws DynamicExtensionsSystemException exception.
 	 * @throws DAOException exception.
 	 * @throws BizLogicException exception.
 	 * @throws DynamicExtensionsApplicationException exception.
 	 */
 	protected DynamicQueryList addNewIntegrationObjects(EntityInterface staticEntity,
 			List<ContainerInterface> mainContainerList, HibernateDAO hibernatedao)
 			throws DynamicExtensionsSystemException, DAOException, BizLogicException,
 			DynamicExtensionsApplicationException
 	{
 
 
 
 		if (hibernatedao == null)
 		{
 			EntityManager.getInstance().persistEntityMetadataForAnnotation(staticEntity, true,
 					false, null);
 		}
 		else
 		{
 			EntityManager.getInstance().persistEntityMetadataForAnnotation(staticEntity, true,
 					false, null, hibernatedao);
 		}
 
 		List<String> queriesList = new ArrayList<String>();
 		List<String> revQueryList = new ArrayList<String>();
 		for (ContainerInterface containerInterface : mainContainerList)
 		{
 			AssociationInterface association = createAssociation(staticEntity,
 					(EntityInterface) containerInterface.getAbstractEntity());
 			//Add association to the static entity.
 			staticEntity.addAssociation(association);
 
 			//create an association without setting the source entity.
 			//Hook entity will be updated as the source on the server
 			intermodelAssociationCollection.add(association);
 			queriesList.addAll(QueryBuilderFactory.getQueryBuilder().getQueryPartForAssociation(
 					association, revQueryList, true));
 		}
 
 		DynamicQueryList dynamicQueryList = new DynamicQueryList();
 		dynamicQueryList.setQueryList(queriesList);
 		dynamicQueryList.setRevQueryList(revQueryList);
 		return dynamicQueryList;
 	}
 
 	/**
 	 *It will create a new association object between the staticEntity & dynamicEntity.
 	 * @param staticEntity source entity
 	 * @param dynamicEntity destination entity.
 	 * @return newly added association
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public AssociationInterface createAssociation(EntityInterface staticEntity,
 			EntityInterface dynamicEntity) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		//Create source role and target role for the association
 		String roleName = staticEntity.getId().toString().concat("_").concat(
 				dynamicEntity.getId().toString());
 		RoleInterface sourceRole = getRole(AssociationType.CONTAINTMENT, roleName,
 				Cardinality.ZERO, Cardinality.ONE);
 		RoleInterface targetRole = getRole(AssociationType.CONTAINTMENT, roleName,
 				Cardinality.ZERO, Cardinality.MANY);
 
 		//Create association with the created source and target roles.
 		AssociationInterface association = getAssociation(dynamicEntity,
 				AssociationDirection.SRC_DESTINATION, roleName, sourceRole, targetRole);
 
 		//Create constraint properties for the created association.
 		ConstraintPropertiesInterface constProperts = AnnotationUtil.getConstraintProperties(
 				staticEntity, dynamicEntity);
 		association.setConstraintProperties(constProperts);
 
 		return association;
 
 	}
 
 	/**
 	 *It will create a new association object between the staticEntity & dynamicEntity.
 	 * @param staticEntity source entity
 	 * @param dynamicEntity destination entity.
 	 * @return newly added association
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public AssociationInterface createDummyAssociation(AssociationInterface originalAssociation) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		AssociationInterface association =createAssociation(originalAssociation.getEntity(), originalAssociation.getTargetEntity());
 		EntityInterface dummyEntityInterface = DomainObjectFactory.getInstance().createEntity();
 		dummyEntityInterface.setId(association.getTargetEntity().getId());
 		association.setTargetEntity(dummyEntityInterface);
 
 		//Create constraint properties for the created association.
 		ConstraintPropertiesInterface constProperts = AnnotationUtil.getDummyConstraintProperties(
 				originalAssociation.getEntity(), association.getTargetEntity());
 		association.setConstraintProperties(constProperts);
 		association.setId(originalAssociation.getId());
 
 		return association;
 
 	}
 
 	/**
 	 * It will create a new association & then set its name etc by using the given parameter.
 	 * @param targetEntity target entity of the association
 	 * @param assonDirectn direction of association
 	 * @param assoName name of association
 	 * @param sourceRole source role
 	 * @param targetRole target role
 	 * @return association object
 	 * @throws DynamicExtensionsSystemException exception.
 	 */
 	private static AssociationInterface getAssociation(EntityInterface targetEntity,
 			AssociationDirection assonDirectn, String assoName, RoleInterface sourceRole,
 			RoleInterface targetRole) throws DynamicExtensionsSystemException
 	{
 		AssociationInterface association = DomainObjectFactory.getInstance().createAssociation();
 		association.setTargetEntity(targetEntity);
 		association.setAssociationDirection(assonDirectn);
 		association.setName(assoName);
 		association.setSourceRole(sourceRole);
 		association.setTargetRole(targetRole);
 		return association;
 	}
 
 	/**
 	 * It will create a new Role object using given parameters
 	 * @param associationType associationType
 	 * @param name name
 	 * @param minCard  minCard
 	 * @param maxCard maxCard
 	 * @return  RoleInterface
 	 */
 	private RoleInterface getRole(AssociationType associationType, String name,
 			Cardinality minCard, Cardinality maxCard)
 	{
 		RoleInterface role = DomainObjectFactory.getInstance().createRole();
 		role.setAssociationsType(associationType);
 		role.setName(name);
 		role.setMinimumCardinality(minCard);
 		role.setMaximumCardinality(maxCard);
 		return role;
 	}
 
 	/**
 	 * It will read the file specified by given path & then create alist of names specified in that file
 	 * which are comma separated.
 	 * @param path file path
 	 * @return list of container names
 	 * @throws IOException exception
 	 */
 	protected static List<String> readFile(String path) throws IOException
 	{
 		List<String> containerNames = new ArrayList<String>();
 		File file = new File(path);
 
 		BufferedReader bufRdr = new BufferedReader(new FileReader(file));
 		String line = bufRdr.readLine();
 		try
 		{
 			//read each line of text file
 			while (line != null)
 			{
 				StringTokenizer stringTokenizer = new StringTokenizer(line, ",");
 				while (stringTokenizer.hasMoreTokens())
 				{
 					//get next token and store it in the array
 					containerNames.add(stringTokenizer.nextToken());
 				}
 
 				line = bufRdr.readLine();
 			}
 		}
 		finally
 		{
 			bufRdr.close();
 		}
 		return containerNames;
 	}
 
 	/**
 	 * Initializes the MOF repository.
 	 * @throws Exception exception.
 	 */
 	private void init() throws DynamicExtensionsSystemException, CreationFailedException
 	{
 		uml = (UmlPackage) rep.getExtent(UML_INSTANCE);
 
 		if (uml == null)
 		{
 			// UML extent does not exist -> create it (note that in case one want's
 			// to instantiate a metamodel other than MOF, they need to provide the second
 			// parameter of the createExtent
 			// method which indicates the metamodel package that should be instantiated)
 			uml = (UmlPackage) rep.createExtent(UML_INSTANCE, getUmlPackage());
 		}
 	}
 
 	/**
 	 * Finds "UML" package -> this is the topmost package of UML metamodel - that's the
 	 * package that needs to be instantiated in order to create a UML extent
 	 * @return Mof Package
 	 * @throws Exception exception
 	 */
 	/**
 	 * Finds "UML" package -> this is the topmost package of UML metamodel - that's the
 	 * package that needs to be instantiated in order to create a UML extent
 	 * @return Mof Package
 	 * @throws DynamicExtensionsSystemException
 	 * @throws Exception exception
 	 */
 	public MofPackage getUmlPackage() throws DynamicExtensionsSystemException
 	{
 		// get the MOF extent containing definition of UML metamodel
 		ModelPackage umlMM = (ModelPackage) rep.getExtent(UML_MM);
 		MofPackage result = null;
 		try
 		{
 			if (umlMM == null)
 			{
 				// it is not present -> create it
 				umlMM = (ModelPackage) rep.createExtent(UML_MM);
 			}
 			// find package named "UML" in this extent
 			result = getUmlPackage(umlMM);
 			reader.read(UmlPackage.class.getResource("resources/01-02-15_Diff.xml").toString(),
 					umlMM);
 			// try to find the "UML" package again
 			result = getUmlPackage(umlMM);
 			if (result == null)
 			{
 				// it cannot be found -> UML metamodel is not loaded -> load it from XMI
 				reader.read(UmlPackage.class.getResource("resources/01-02-15_Diff.xml").toString(),
 						umlMM);
 				// try to find the "UML" package again
 				result = getUmlPackage(umlMM);
 			}
 		}
 		catch (Exception e)
 		{
 			throw new DynamicExtensionsSystemException(
 					"Exception occured while Intializing UML Package", e);
 		}
 		return result;
 	}
 
 	/** Finds "UML" package in a given extent
 	 * @param umlMM MOF extent that should be searched for "UML" package.
 	 * @return Mof Package
 	 */
 	private static MofPackage getUmlPackage(ModelPackage umlMM)
 	{
 		// iterate through all instances of package
 		MofPackage pkg = null;
 		for (Iterator it = umlMM.getMofPackage().refAllOfClass().iterator(); it.hasNext();)
 		{
 			pkg = (MofPackage) it.next();
 			LOGGER.info("\n\nName = " + pkg.getName());
 
 			// is the package topmost and is it named "UML"?
 			if (pkg.getContainer() == null && "UML".equals(pkg.getName()))
 			{
 				// yes -> return it
 				break;
 			}
 		}
 		// a topmost package named "UML" could not be found
 		return pkg;
 	}
 
 	/**
 	 * This method will return the List of Association which contains the
 	 * association list up to the hook entity from some base entity
 	 * so that indirect path from the base entity (i.e. the first entity
 	 * which is source of the first association in the association list)
 	 * to the main containers found in the model
 	 * @param hibernatedao dao used for retrieving the associations
 	 * @return the list of associations
 	 * @throws DynamicExtensionsSystemException exception
 	 * @throws DynamicExtensionsApplicationException exception
 	 */
 	protected abstract List<AssociationInterface> getAssociationListForCurratedPath(
 			HibernateDAO hibernatedao) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException;
 
 	/**
 	 * It will return the xmi configuration object to be used while importing the model
 	 * @return XMIConfiguration object
 	 */
 	protected abstract XMIConfiguration getXMIConfigurationObject();
 
 	/**
 	 * This method will be implemented by the host application & it can do whatever it wants after the
 	 * Xmi Importer like(associate all the given mainConatainer to the events of given clinical
 	 * Study Ids in the form of studyContextEvent) etc.
 	 * @param isEditedXmi specifies weather the Edit XMI case or not.
 	 * @param coRecObjCsvFName name of the file
 	 * @param mainContainerList main container list.
 	 * @param domainModelName domain model name.
 	 * @throws BizLogicException exception.
 	 * @throws DAOException exception.
 	 * @throws DynamicExtensionsApplicationException exception.
 	 */
 	protected abstract void postProcess(boolean isEditedXmi, String coRecObjCsvFName,
 			List<ContainerInterface> mainContainerList, String domainModelName)
 			throws BizLogicException, DAOException, DynamicExtensionsApplicationException;
 
 }
