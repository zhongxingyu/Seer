 package obir.otr;
 
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Scanner;
 import javax.swing.SwingUtilities;
 
 import obir.ir.Corpus;
 import obir.ir.indexing.SemanticIndexing;
 import obir.misc.ColorManager;
 import edu.stanford.smi.protege.exception.OntologyLoadException;
 import edu.stanford.smi.protege.model.Project;
 import edu.stanford.smi.protege.ui.ProjectManager;
 import edu.stanford.smi.protegex.owl.ProtegeOWL;
 import edu.stanford.smi.protegex.owl.database.OWLDatabaseModel;
 import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
 import edu.stanford.smi.protegex.owl.model.OWLIndividual;
 import edu.stanford.smi.protegex.owl.model.OWLModel;
 import edu.stanford.smi.protegex.owl.model.RDFProperty;
 import edu.stanford.smi.protegex.owl.model.RDFSClass;
 
 
 /**
  * Main class gathering crucial pieces of information for the current project, such as the used OTR, corpus
  * @author Axel Reymonet
  *
  */
 public class ObirProject {
 
 	/**
 	 * Indicates whose domain OTR is currently opened
 	 */
 	private static String domainPartner;
 
 	/**
 	 * Indicates whether all elements are considered the same for all domain partners
 	 */
 	private static boolean isPrototypeGeneric;
 
 	/**
 	 * Plugin properties to read from a config file
 	 */
 	private static Properties fixedPluginProperties = null ;
 	
 	/**
 	 * Language used in the corpus 
 	 */
 	private static String language;
 
 	/**
 	 * Set of documents to work with
 	 */
 	private static Corpus corpus;
 
 	/**
 	 * Current OTR
 	 */
 	private static OTR otr;
 
 	/**
 	 * Current Protégé OWL model
 	 */
 	private static OWLModel model;
 
 	/**
 	 * Current processor aiming at indexing semantically the corpus
 	 */
 	private static SemanticIndexing indexingProcessor;
 
 	/**
 	 * Set of fields which will be semantically indexed in each XML document of the corpus
 	 */
 	private static HashMap<String,HashMap<String,String>> semanticFields;
 
 	/**
 	 * Set of fields which will be classically indexed in each XML document of the corpus
 	 */
 	private static HashMap<String,HashMap<String,String>> classicFields;
 
 	/**
 	 * Indice to name instances (term occurrence or concept instance)
 	 */
 	static int COUNT;
 
 	/**
 	 * Current color manager associating a term or concept with a color
 	 */
 	static ColorManager colorMgr;
 
 
 	/**
 	 * Static plugin property file 
 	 */
 	//public static File propertiesFile;
 	private static String propertyDir;
 	/**
 	 * Static boolean allowing to choose the global debug state for the plugin
 	 */
 	public static boolean debug = false;
 
 	/**
 	 * An auto-saving task
 	 */
 	public static AutoSaveTask autoSaveTask;
 
 	/**
 	 * Static string corresponding to the name of the file created for each query
 	 */
 	public static String xmlQueryFile = "query.xml";
 
 	public static final String ARKEO = "arkeo";
 	public static final String ARTAL = "artal";
 
 	private static ObirProject instance = null; 
 
 	public static boolean isFirstInitializing = true;
 
 	private ObirProject()
 	{
 		if (instance==null)
 			instance = this;
 	}
 
 
 	/**
 	 * Constructor called when opening only semantic search engine
 	 * @param otrFile the OTR with the annotations
 	 * @param corpusDir the corresponding corpus directory
 	 */
 	public ObirProject(File otrFile,File corpusDir, String propertyFilePath, OWLModel owlmodel)
 	{
 		this();
 		colorMgr = new ColorManager();
 		//model = (OWLModel)ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase();
 		model=owlmodel;
 		propertyDir=propertyFilePath.replace("plugin.properties", "");
 		File propertiesFile= new File(propertyFilePath);
 		initParameters(propertiesFile);
 		otr = new OTR(otrFile, propertyDir);
 		corpus = new Corpus(corpusDir);
 		
 	}
 
 	/**
 	 * Method that returns the parameter directory
 	 * @return
 	 */
 	public static String getParameterDir(){
 		return propertyDir;
 	}
 	
 	/**
 	 * Method that sets the searchable fields according to the information in the property file
 	 * @param prop
 	 */
 	public static void setSearchableFields(Properties prop){
 		semanticFields = new HashMap<String,HashMap<String,String>>();
 		classicFields = new HashMap<String,HashMap<String,String>>();
 		HashSet<String> semXMLTags = new HashSet<String>();
 		//		System.out.println("semantic fields loaded: "+pluginProperties.getProperty("field.searchable.semantic"));
 		Scanner scan = new Scanner(prop.getProperty("field.searchable.semantic"));
 		scan.useDelimiter(",");
 		while (scan.hasNext())
 		{
 			semXMLTags.add(scan.next().replaceAll(" ", ""));
 		}
 		HashSet<String> classicXMLTags = new HashSet<String>();
 		scan.reset();
 		//		System.out.println("classic fields loaded: "+pluginProperties.getProperty("field.searchable.classic"));
 		scan = new Scanner(prop.getProperty("field.searchable.classic"));
 		scan.useDelimiter(",");
 		while (scan.hasNext())
 		{
 			classicXMLTags.add(scan.next().replaceAll(" ", ""));
 		}
 		
 		File paramDir = new File(propertyDir);
 		String [] paramFiles = paramDir.list();
 		for (String filename:paramFiles)
 		{
 			if (filename.startsWith("stylesheet_") && filename.endsWith(".xslt"))
 			{
 				String xsltLang = filename.substring(filename.indexOf("stylesheet_")+11, filename.lastIndexOf(".xslt"));
 				File xsltFile = new File(paramDir,filename);
 
 				BufferedReader xsltRead;
 				try {
 					xsltRead = new BufferedReader(new FileReader(xsltFile));
 
 					String line = xsltRead.readLine();
 					String interestingXMLTag = null;
 					while (line!=null)
 					{
 						//				line = (new String(line.getBytes(),"UTF-8")).toString();
 						if (interestingXMLTag!=null)
 						{
 							String htmlTag = line.substring(line.indexOf("<br/><b>")+8,line.indexOf("</b><br/>"));
 							if (semXMLTags.contains(interestingXMLTag))
 							{
 								HashMap<String,String> htmlTagByLang = new HashMap<String, String>();
 								if (semanticFields.containsKey(interestingXMLTag))
 									htmlTagByLang = semanticFields.get(interestingXMLTag);
 								htmlTagByLang.put(xsltLang,htmlTag);
 								semanticFields.put(interestingXMLTag, htmlTagByLang);
 							}
 
 							if (classicXMLTags.contains(interestingXMLTag))
 							{
 								HashMap<String,String> htmlTagByLang = new HashMap<String, String>();
 								if (classicFields.containsKey(interestingXMLTag))
 									htmlTagByLang = classicFields.get(interestingXMLTag);
 								htmlTagByLang.put(xsltLang,htmlTag);
 								classicFields.put(interestingXMLTag, htmlTagByLang);
 							}
 							interestingXMLTag = null;
 						}
 						else //if <xsl:template match="preliminary"> 
 						{
 							if (line.contains("<xsl:template match="))
 							{
 								String tagID = line.substring(line.indexOf("<xsl:template match=")+21, line.lastIndexOf("\""));
 								if (semXMLTags.contains(tagID)||classicXMLTags.contains(tagID))
 									interestingXMLTag = tagID;
 							}
 						}
 						line = xsltRead.readLine();
 					}
 				} 
 				catch (FileNotFoundException e) {e.printStackTrace();}
 				catch (IOException e) {e.printStackTrace();}	
 			}
 		}
 	}
 	/**
 	 * Initialization of parameters from the property file
 	 * @param propertiesFile
 	 */
 	private void initParameters(File propertiesFile)
 	{
 		// Load the properties file
 		fixedPluginProperties = new Properties() ; 
 		try {
 			System.err.println(propertiesFile.getAbsolutePath()+" can read: "+propertiesFile.canRead());
 			java.io.FileInputStream configurationFile = new java.io.FileInputStream(propertiesFile);
 			fixedPluginProperties.load(configurationFile);
 
 		} catch (IOException e) {
 			System.out.println("Impossible to find file " + propertiesFile.getAbsolutePath());
 		}
 
 		//		System.out.println("genericity loaded: "+pluginProperties.getProperty("plugin.generic"));
 		setPrototypeGenericity(new Boolean(fixedPluginProperties.getProperty("plugin.generic")));
 		//		System.out.println("partner loaded: "+pluginProperties.getProperty("plugin.partner"));
 		setDomainPartner(fixedPluginProperties.getProperty("plugin.partner"));
 
 		//		System.out.println("language loaded: "+pluginProperties.getProperty("plugin.language"));
 		language = fixedPluginProperties.getProperty("plugin.language");
 
 
 		String colorParams = fixedPluginProperties.getProperty("categories.colors");
 		if (colorParams!=null)
 		{
 			Scanner scan = new Scanner(colorParams);
 			scan.useDelimiter(",");
 			while (scan.hasNext())
 			{
 				String param = scan.next();
 				String categ = param.substring(0, param.indexOf(":"));
 				Color color = Color.decode(param.substring(param.indexOf("#")));
 				colorMgr.overrideColor(categ, color, true);
 			}
 		}
 
 		setSearchableFields(fixedPluginProperties);
 
 		
 
 		String autoIndex =(String)fixedPluginProperties.getProperty("plugin.autoindex");
 		if (autoIndex==null)
 			fixedPluginProperties.setProperty("plugin.autoindex", "false");
 		indexingProcessor = new SemanticIndexing(new Boolean(autoIndex));
 	}
 
 	/**
 	 * Indicates whose domain OTR is currently used
 	 * @return the current domain partner
 	 */
 	public static String getDomainPartner() {
 		return domainPartner;
 	}
 
 	public static boolean isPrototypeGeneric()
 	{
 		return isPrototypeGeneric;
 	}
 
 	/**
 	 * @param property
 	 * @return true iff the propery is defined in the OTR namespace (not in protege, swrl, etc.) (prop can be a metaproperty)
 	 */
 	public static boolean isOTRProperty(RDFProperty property) {
 		//NOTE does not work with sub-ontologies (they have different namespaces)
 		//SOLUTION: filter out protege and swrl namespaces explicitly
 		//return property.getNamespace().equals(getOWLModel().getNamespaceManager().getDefaultNamespace()) ;
 		String [] forbiddenPrefixes = {"http://www.w3.org/", "http://protege.stanford.edu", "http://swrl.stanford.edu"};
 		String propertyNameSpace = property.getNamespace();
 		for(String prefix : forbiddenPrefixes){
 			if(propertyNameSpace.startsWith(prefix)) return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @param prop
 	 * @return true iff prop is a domain property 
 	 */
 	public static boolean isDomainProperty(RDFProperty prop)
 	{
 		RDFSClass domain = prop.getDomain(false) ; 
 		return  ObirProject.isOTRProperty(prop) && 
 		(domain!=null) && 
 		(!domain.equals(model.getOWLNamedClass(OTR.TERM))) && 
 		(!domain.equals(model.getOWLNamedClass(OTR.CONCEPT))) &&
 		(!domain.equals(model.getOWLNamedClass(OTR.TERM_OCCURRENCE)))&&
 		(!domain.equals(model.getOWLNamedClass(OTR.DOCUMENT)))&&
 		(!domain.equals(model.getOWLNamedClass(OTR.COMPARABLE_CONCEPT))) ; 
 	}
 
 	/**
 	 * Sets the genericity of the prototype
 	 * @param isPrototypeGeneric boolean to set
 	 */
 	public static void setPrototypeGenericity(boolean isGeneric)
 	{
 		isPrototypeGeneric = isGeneric;
 	}
 
 
 	/**
 	 * Sets the domain partner, therefore sets the weighting scheme used for retrieval
 	 * @param partner "arkeo" for concepts only, "artal" for concepts and relations
 	 */
 	public static void setDomainPartner(String partner) {
 		domainPartner = partner;
 	}
 
 	/**
 	 * Plugin properties getter
 	 * @return the properties associated with the plugin
 	 */
 	public static Properties getPluginProperties(){
 		return fixedPluginProperties;
 	}
 
 	/**
 	 * Language getter
 	 * @return the language of the indexed corpus
 	 */
 	public static String getDefaultLanguage() {
 		return language;
 	}
 
 	/**
 	 * Corpus getter
 	 * @return the set of documents to work with
 	 */
 	public static Corpus getCorpus() {
 		return corpus;
 	}
 
 	/**
 	 * Increments a counter to give unique IDs to OWL individuals (i.e. term occurrences, concept or document instances)
 	 * @return an unused name to use as an ID
 	 */
 	public static String generateNextIndName(){
 		while (otr.getOntology().getOWLIndividual("Onto_Individual_"+COUNT)!=null)
 			COUNT ++;
 		return "Onto_Individual_"+COUNT;
 	}
 
 
 	/**
 	 * Resets the OWL individual counter
 	 */
 	public static void restartIndividualNumbering() {
 		COUNT = 0;
 	}
 
 	/**
 	 * OTR getter
 	 * @return the current OTR
 	 */
 	public static OTR getOTR()
 	{
 		return otr;
 	}
 
 
 	/**
 	 * Prot�g� OWL model getter
 	 * @return the current OWL model
 	 */
 	public static OWLModel getOWLModel()
 	{
 		return model;
 	}
 
 	/**
 	 * Prot�g� OWL model setter
 	 * @param oModel a given OWL model
 	 */
 	public static void setOWLModel(OWLModel oModel)
 	{
 		model = oModel;
 	}
 
 
 	/**
 	 * Color manager getter
 	 * @return the current color manager
 	 */
 	public static ColorManager getColorManager()
 	{
 		return colorMgr;
 	}
 
 	/**
 	 * Copy the file into newFile Path
 	 * Works only on Windows...  
 	 * @param file : the string giving the source filename
 	 * @param newFilepath : the string giving the destination filename
 	 */
 	public static void copyFileTo(String file, String newFilepath)
 	{
 		try 
 		{
 			java.lang.Runtime l_Runtime = java.lang.Runtime.getRuntime();
 			String[] cmdArray = new String[3];
 			cmdArray[0]="cmd";
 			cmdArray[1]="/c";
 			cmdArray[2]="\"copy "+file+" "+newFilepath+"\"";
 			java.lang.Process l_Proc = l_Runtime.exec(cmdArray);
 			l_Proc.waitFor();
 		}
 		catch(java.lang.InterruptedException ie) {ie.printStackTrace();}
 		catch (java.io.IOException ioe) {ioe.printStackTrace();}
 	}
 
 
 	/**
 	 * Static method to get the first key under which a value is stored into a map. 
 	 * @param map a given map
 	 * @param value the value stored in a map under one (or more) key(s)
 	 * @return the first appropriate key
 	 */
 	@SuppressWarnings("unchecked")
 	public static Object getKeyFromValue(Map map,Object value)
 	{
 		for (Object key:map.keySet())
 			if (map.get(key).equals(value))
 				return key;
 		return null;
 	}
 
 	/**
 	 * Indexing processor getter
 	 * @return the processor in charge of semantic indexing
 	 */
 	public static SemanticIndexing getIndexingProcessor() {
 		return indexingProcessor;
 	}
 
 	/**
 	 * Semantic fields getter
 	 * @return the fields of the corpus which are semantically indexed
 	 */
 	public static HashMap<String,HashMap<String,String>> getSemanticFields() {
 		return semanticFields;
 	}
 
 	/**
 	 * Semantic fields getter
 	 * @return the fields of the corpus which are classically indexed
 	 */
 	public static HashMap<String,HashMap<String,String>> getClassicFields() {
 		return classicFields;
 	}
 
 
 
 	public static void printDebug(String s)
 	{
 		if (debug)
 			System.out.println(s);
 	}
 
 	public static ObirProject getInstance()
 	{
 		return instance;
 	}
 
 	//DB version
 	public static void exportOnlyOTR(OWLModel model,String otrExportName)
 	{
 		//TODO
 	}
 
 	//Local file version
 	@SuppressWarnings("unchecked")
 	private static void exportOnlyOTR(OWLModel model,File exportOntoFile)
 	{
 		File annotationFile = new File(((JenaOWLModel)model).getOWLFilePath().replace("file:/", ""));
 
 		if (exportOntoFile.exists())
 			exportOntoFile.delete();
 		obir.otr.ObirProject.copyFileTo(annotationFile.getAbsolutePath().replaceAll("/", "\\\\"), exportOntoFile.getAbsolutePath().replaceAll("/", "\\\\"));
 
 
 		try 
 		{
 			InputStream in = new FileInputStream(exportOntoFile);
 
 			JenaOWLModel onto = ProtegeOWL.createJenaOWLModelFromInputStream(in);
 
 			for (OWLIndividual ind:(Collection<OWLIndividual>)onto.getOWLIndividuals())
 				ind.delete();
 
 			HashSet errors = new HashSet();
 			onto.save(new FileOutputStream(exportOntoFile.getAbsolutePath()), "RDF/XML-ABBREV", errors);
 		} 
 		catch (FileNotFoundException e) {e.printStackTrace();}
 		catch (OntologyLoadException e) {e.printStackTrace();}
 	}
 
 
 	/**
 	 * Actually saves the OTR and corpus annotations into 2 different files
 	 * @param isUserTriggered indicates whether the save is manually or automatically triggered
 	 */
 	@SuppressWarnings("unchecked")
 	public
 	static void doSaving(boolean isUserTriggered)
 	{
 		boolean isOTRLocal = true;
 		if (ObirProject.getOWLModel() instanceof OWLDatabaseModel)
 			isOTRLocal = false;
 
 		String owlfilepath = otr.getPath();
 		if (isUserTriggered)
 		{
 			if (isOTRLocal)
 			{
 				String ontoBackupPath = owlfilepath.replaceAll("file:///", "")+".bak";
 				File ontoBackupFile = new File(ontoBackupPath);
 				if (ontoBackupFile.exists())
 					ontoBackupFile.delete();
 
 				obir.otr.ObirProject.copyFileTo(owlfilepath.replaceAll("file:///", "").replaceAll("/", "\\\\"), ontoBackupFile.getAbsolutePath().replaceAll("/", "\\\\"));
 			}
 			else
 			{
 				//TODO copie de sauvegarde de table en BD
 			}
 
 		}
 
 		try 
 		{
 			Project currentProject = ProjectManager.getProjectManager().getCurrentProject();
 
 			if (isUserTriggered)
 			{
 				HashSet errors = new HashSet();
 				if(currentProject != null) currentProject.save(errors);
 			}
 			else
 			{
 				if (isOTRLocal)
 				{
 					HashSet errors = new HashSet();
 					File ontoBackup = null;
 					String ontoPath = ((JenaOWLModel)currentProject.getKnowledgeBase()).getOWLFilePath();
 					if (ontoPath!=null)
 						ontoBackup = new File(ontoPath.replace(".owl", ".tmp").replace("file:/",""));
 					else
 						ontoBackup = new File(new File(currentProject.getProjectDirectoryURI()),"project.tmp");
 
 					if (ontoBackup.exists())
 						ontoBackup.delete();
 					ontoBackup.createNewFile();
 
 					((JenaOWLModel)getOWLModel()).save(new FileOutputStream(ontoBackup), "RDF/XML-ABBREV", errors);
 				}
 				else
 				{
 					//TODO
 				}
 			}
 		} 
 
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 
 	public static void doOTRExporting(OWLModel model,String key)
 	{
 		//		OWLModel model = ObirProject.getInstance().getOWLModel();
 		if (model instanceof JenaOWLModel)
 		{
 			String filepath = key;
 			if (!filepath.endsWith(".owl"))
 				filepath = filepath+".owl";
 			File exportedFile = new File(filepath);
 			ObirProject.exportOnlyOTR(model,exportedFile);
 			//			return exportedFile;
 		}
 
 		else if (model instanceof OWLDatabaseModel)
 		{
 			//TODO code permettant l'export
 			//			return key;
 		}
 
 		//		return null;
 	}
 
 	/**
 	 * Launches the saving process on current OTR and corpus annotations 
 	 * @returns true iff the project has been saved
 	 */
 	public static boolean saveProject()
 	{
 		if (!otr.isAutoSaveInProgress())
 		{
 			System.err.println("Saving project...");
 			ObirProject.getOWLModel().getOWLProject().getSettingsMap().setString("dynamo_corpus",ObirProject.getCorpus().getDirectoryPath().replace("\\", "/"));
 
 			(new ObirProject.SaveTask()).run();
 		}
 		else
 		{
 			return false ; 
 		}
 
 
 		return true ; 
 	}
 
 	static class AutoInitializer extends Thread {
 
 		/**
 		 * M�thode appel�e dans une autre thread lors de la construction d'un
 		 * objet de type Task
 		 */
 		@Override
 		public void run(){
 
 			if (!otr.isAutoSaveInProgress())
 			{
 
 				getIndexingProcessor().launchIndexing(corpus.getDirectory(),true,true);
 
 			}
 		}
 
 		protected void onFailure(Throwable t) {
 			t.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Internal class corresponding to an automatic saving process
 	 * @author Axel Reymonet
 	 */
 	public static class AutoSaveTask extends Thread {
 
 		/**
 		 * M�thode appel�e dans une autre thread lors de la construction d'un objet de type Task
 		 */
 		@Override
 		public void run()
 		{
 			boolean retry = false;
 			while (true)
 			{
 				if ( ! retry )
 					try {
 						java.lang.Thread.sleep(1200000);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 				if ( ! this.interrupted() && otr.isAutoSavePossible()) 
 				{
 					retry = false;
 					otr.setAutoSaveInProgress(true);
 					doSaving(false);
 					otr.setAutoSaveInProgress(false);
 				}
 				else
 				{
 					try {
 						java.lang.Thread.sleep(10000);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					retry = true;
 				}
 			}
 		}
 
 
 		protected void onFailure(Throwable t) 
 		{
 			t.printStackTrace();	
 		}
 
 	}
 
 	/**
 	 * Internal class corresponding to a manual saving process
 	 * @author Axel Reymonet
 	 */
 	public static class SaveTask extends Thread{
 
 
 		@Override
 		public void run() {
 			otr.setAutoSavePossible(false);
 			doSaving(true);
 			otr.setAutoSavePossible(true);
 		}
 
 
 
 	}
 
 	public static class ExportTask extends Thread {
 
 		OWLModel myModel;
 		String key;
 
 		public ExportTask(OWLModel model,String pointer)
 		{
 			myModel = model;
 			key = pointer;
 		}
 
 		@Override
 		public void run() {
 			otr.setAutoSavePossible(false);
 			doOTRExporting(myModel,key);
 			otr.setAutoSavePossible(true);
 		}
 
 	}
 
 	/**
 	 * Internal class corresponding to a previous project deletion process (basically, its only use is to display a progress bar during the instance deletion) 
 	 */
 	public static class DeleteTask extends Thread {
 
 		/**
 		 * M�thode appel�e dans une autre thread lors de la construction d'un objet de type Task
 		 */
 		@SuppressWarnings("unchecked")
 		@Override
 		public void run()
 		{
 			int nb_instances = getOWLModel().getOWLIndividuals().size() ;
 			int i = 0 ;
 
 			//boolean eventGenerationEnabled = model.setGenerateEventsEnabled(false) ;
 			getOWLModel().setDispatchEventsEnabled(false) ;
 
 
 
 			for (OWLIndividual ind:(Collection<OWLIndividual>)getOWLModel().getOWLIndividuals())
 			{
 				if (ind!=null)
 					ind.delete();
 			}
 
 			getOWLModel().setDispatchEventsEnabled(true) ;
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					getOWLModel().flushEvents();
 				}
 			});
 
 			ObirProject.getCorpus().clear();
 			//			resetEditedFiles();
 		}
 
 	}
 	
 	public static boolean blockDispatchEvents()
 	{
 		boolean previousDispatchEventState = model.getDispatchEventsEnabled();
 		if (previousDispatchEventState)
 		{
 			model.setDispatchEventsEnabled(false);
 			return true;
 		}
 		return false;
 	}
 	
 	public static void unblockDispatchEvents(boolean needed)
 	{
 		if (needed)
 		{
 			model.setDispatchEventsEnabled(true) ;
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					model.flushEvents();
 				}
 			});
 		}
 	}
 
 }
