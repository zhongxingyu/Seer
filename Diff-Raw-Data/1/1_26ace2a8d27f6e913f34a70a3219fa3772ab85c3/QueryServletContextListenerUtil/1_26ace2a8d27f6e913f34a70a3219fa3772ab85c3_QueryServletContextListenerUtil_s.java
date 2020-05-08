 
 package edu.wustl.query.util.listener;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.Connection;
 import java.util.Properties;
 
 import javax.naming.InitialContext;
 import javax.servlet.ServletContextEvent;
 import javax.sql.DataSource;
 
 import edu.wustl.cab2b.server.path.PathFinder;
 import edu.wustl.common.util.XMLPropertyHandler;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.common.vocab.VocabularyException;
 import edu.wustl.common.vocab.utility.VocabUtil;
 import edu.wustl.query.util.filter.StrutsConfigReader;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.global.VIProperties;
 import edu.wustl.query.util.global.Variables;
 
 public class QueryServletContextListenerUtil
 {
 
 	public static void initializeQuery(ServletContextEvent sce, String datasourceJNDIName) throws Exception
 	{
 		Logger.configDefaultLogger(sce.getServletContext());
 		Variables.applicationHome = sce.getServletContext().getRealPath("");
 
 		setGlobalVariable();
 
 		//Added by Baljeet....This method caches all the Meta data
 		initEntityCache(datasourceJNDIName);
 		
 		// Added to create map of query actions which will be used by QueryRequestFilter to check authorization
 		StrutsConfigReader.init(Variables.applicationHome+File.separator+Constants.WEB_INF_FOLDER_NAME+File.separator+Constants.AQ_STRUTS_CONFIG_FILE_NAME);
 
 
 	}
 
 	private static void initEntityCache(String datasourceJNDIName)
 	{
 		try
 		{
 			//Added for initializing PathFinder and EntityCache
 			InitialContext ctx = new InitialContext();
 			DataSource ds = (DataSource) ctx.lookup(datasourceJNDIName);
 			Connection conn = ds.getConnection();
 			PathFinder.getInstance(conn);
 		}
 		catch (Exception e)
 		{
 			//logger.debug("Exception occured while initialising entity cache");
 		}
 
 	}
 
 	private static void setGlobalVariable() throws Exception
 	{
 		String path = System.getProperty("app.propertiesFile");
 		XMLPropertyHandler.init(path);
 		File propetiesDirPath = new File(path);
 		Variables.propertiesDirPath = propetiesDirPath.getParent();
 
 		Variables.applicationName = ApplicationProperties.getValue("app.name");
 		Variables.applicationVersion = ApplicationProperties.getValue("app.version");
 		int maximumTreeNodeLimit = Integer.parseInt(XMLPropertyHandler.getValue(Constants.MAXIMUM_TREE_NODE_LIMIT));
 		Variables.maximumTreeNodeLimit = maximumTreeNodeLimit;
 		readProperties();
 		path = System.getProperty("app.propertiesFile");
 		
 		//configure VI
 		setVIProperties();
 		
 	}
 
 	private static void setVIProperties() throws VocabularyException
 	{
 		Properties vocabProperties = VocabUtil.getVocabProperties();
 		VIProperties.sourceVocabName = vocabProperties.getProperty("source.vocab.name");
 		VIProperties.sourceVocabVersion = vocabProperties.getProperty("source.vocab.version");
 		VIProperties.sourceVocabUrn = vocabProperties.getProperty("source.vocab.urn");
 		VIProperties.searchAlgorithm = vocabProperties.getProperty("match.algorithm");
 		VIProperties.maxPVsToShow = Integer.valueOf(vocabProperties.getProperty("pvs.to.show"));
 		VIProperties.maxToReturnFromSearch = Integer.valueOf(vocabProperties.getProperty("max.to.return.from.search"));
 		VIProperties.translationAssociation = vocabProperties.getProperty("vocab.translation.association.name");
 		VIProperties.medClassName = vocabProperties.getProperty("med.class.name");
 		
 	}
 
 	private static void readProperties()
 	{
 		File file = new File(Variables.applicationHome + System.getProperty("file.separator") + "WEB-INF" + System.getProperty("file.separator")
 				+ "classes" + System.getProperty("file.separator") + "query.properties");
 
 		if (file.exists())
 		{
 			Properties queryProperties = new Properties();
 			try
 			{
 				queryProperties.load(new FileInputStream(file));
 
 				Variables.queryGeneratorClassName = queryProperties.getProperty("query.queryGeneratorClassName");
 				//Added to get AbstractQuery Implementer Class Name.
 				Variables.abstractQueryClassName = queryProperties.getProperty("query.abstractQueryClassName");
 				Variables.abstractQueryManagerClassName = queryProperties.getProperty("query.abstractQueryManagerClassName");
 				Variables.abstractQueryUIManagerClassName = queryProperties.getProperty("query.abstractQueryUIManagerClassName");
 				Variables.abstractQueryITableManagerClassName = queryProperties.getProperty("query.abstractQueryITableManagerClassName");
 				Variables.viewIQueryGeneratorClassName = queryProperties.getProperty("query.viewIQueryGeneratorClassName");
 				Variables.recordsPerPageForSpreadSheet = Integer.parseInt(queryProperties.getProperty("spreadSheet.recordsPerPage"));
 				Variables.recordsPerPageForTree = Integer.parseInt(queryProperties.getProperty("tree.recordsPerPage"));
 				Variables.resultLimit = Integer.parseInt(queryProperties.getProperty("datasecurity.resultLimit"));
 				Variables.exportDataThreadClassName = queryProperties.getProperty("query.exportDataThreadClassName");
 				Variables.dataQueryExecutionClassName = queryProperties.getProperty("query.dataQueryExecutionClassName");
 				Variables.properties = queryProperties;
 				Variables.csmUtility = queryProperties.getProperty("query.csmUtility");
 			}
 			catch (FileNotFoundException e)
 			{
 				e.printStackTrace();
 			}
 			catch (IOException e)
 			{
 				e.printStackTrace();
 			}
 		}
 
 	}
 }
