 package com.compomics.relims.concurrent;
 
 import com.compomics.relims.conf.RelimsProperties;
 import com.compomics.relims.manager.filemanager.FileManager;
 import com.compomics.relims.manager.filemanager.RepositoryManager;
 import com.compomics.relims.manager.progressmanager.Checkpoint;
 import com.compomics.relims.manager.progressmanager.ProgressManager;
 import com.compomics.relims.manager.variablemanager.ProcessVariableManager;
 import com.compomics.relims.model.beans.PeptideShakerJobBean;
 import com.compomics.relims.model.beans.RelimsProjectBean;
 import com.compomics.relims.model.beans.SearchGUIJobBean;
 import com.compomics.relims.model.guava.predicates.PredicateManager;
 import com.compomics.relims.model.interfaces.DataProvider;
 import com.compomics.relims.model.interfaces.ModificationResolver;
 import com.compomics.relims.model.interfaces.ProjectRunner;
 import com.compomics.relims.model.interfaces.SearchStrategy;
 import com.compomics.relims.model.provider.ProjectProvider;
 import com.compomics.relims.model.provider.pride.PrideProjectProvider;
 import com.compomics.util.experiment.identification.SearchParameters;
 import com.google.common.base.Predicate;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Observable;

import static java.lang.String.format;
 
 public class RelimsJobController extends Observable implements ProjectRunner {
 
     /**
      * Plain logger
      */
     private static Logger logger = Logger.getLogger(RelimsJobController.class);
     /**
      * a RelimsProjectBean stores all required data to start a process. It is an
      * intermediate step to build the searchParameters. TODO : is this still
      * required?
      */
     private RelimsProjectBean relimsProjectBean;
     /**
      * Predicates are requirements that need to be forfilled before runtime.
      */
     private PredicateManager predicateManager;
     /**
      * Dataprovider is a type of class that handles the loading and handling of
      * the required data (spectra etc)
      */
     private DataProvider dataProvider;
     /**
      * the ModificationResolver sets the fixed modifications? ---> TODO:FIGURE
      * THIS OUT
      */
     private ModificationResolver modificationResolver;
     /**
      * the projectProvider is the class that handles the usage local project
      * lists to run them. This is used in the classic relims...TODO:
      * re-implement this?
      */
     private ProjectProvider projectProvider;
     /**
      * the project accession number...
      */
     private long projectID;
     /**
      * the searchresultfolder is actually the workspace, adapted to the certain
      * project with a timestamp and other features in the name of the folder.
      */
     private File searchResultFolder;
     /**
      * an instance of the class that contains all required information to build
      * and run a searchGUI process
      */
     private SearchGUIJobBean searchGUIJobBean;
     /**
      * an instance of the class that contains all required information to build
      * and run a peptideshaker process
      */
     private PeptideShakerJobBean lPeptideShakerJobBean;
     /**
      * TODO : Are these not the same ?
      */
     private String sampleID;
     private String experimentID;
     /**
      * This is the MGF file. Since searchgui accepts a list of MGF files as
      * well, I haven't taken out the list implementation from this class.
      */
     private File spectrumFile;
     /**
      * A ProgressManager to store the state of the project and monitor it
      */
     private ProgressManager progressManager = ProgressManager.getInstance();
     /**
     * The filegrabber is a generic tool to acquire MGF files and
      * searchparameters files in a resultfolder. //TODO : it currently only
      * grabs the first file with the required extension that is found. Should
      * these be put in a list? In theory, only ONE file per extension should be
      * generated anyway...
      */
     private FileManager fileGrabber = FileManager.getInstance();
 
     @Override
     public void setPredicateManager(PredicateManager aPredicateManager) {
         predicateManager = aPredicateManager;
     }
 
     @Override
     public void setSearchStrategy(SearchStrategy aSearchStrategy) {
         //TODO REMOVE THIS COMPLETELY?
         return;
     }
 
     @Override
     public void setProjectID(long projectID) {
         this.projectID = projectID;
     }
 
     @Override
     public void setProjectProvider(ProjectProvider iProjectProvider) {
         this.projectProvider = iProjectProvider;
     }
 
     public void setProject(RelimsProjectBean aRelimsProjectBean) {
         relimsProjectBean = aRelimsProjectBean;
     }
 
     public void setDataProvider(DataProvider aDataProvider) {
         dataProvider = aDataProvider;
     }
 
     public void setModificationResolver(ModificationResolver aModificationResolver) {
         modificationResolver = aModificationResolver;
     }
 
     private RelimsProjectBean makeRelimsJobBean() {
          logger.debug("Building projectbean");
         //  ProcessVariableManager.setSearchResultFolder(searchResultFolder.getAbsolutePath().toString());
         setDataProvider(projectProvider.getDataProvider());
         setModificationResolver(modificationResolver = projectProvider.getModificationResolver());
         ProcessVariableManager.setProjectID(projectID);
         relimsProjectBean = projectProvider.getProject(projectID);
         if (relimsProjectBean != null) {
             relimsProjectBean.setDataProvider(dataProvider);
             relimsProjectBean.getSpectrumFile();
             //make a searchparameters object and file
             relimsProjectBean.createSearchParameters();
         }
         return relimsProjectBean;
     }
     //Prepare and run the searchgui job (different pathways to do so)
 
     private boolean runSearchGUI() throws IOException, ConfigurationException {
         long lProjectId = relimsProjectBean.getProjectID();
         Collection<Predicate> lPredicates = predicateManager.createCollection(
                 PredicateManager.Types.INSTRUMENT //                    PredicateManager.Types.PROJECT_SIZE,
                 //                    PredicateManager.Types.SPECIES,
                 //                    PredicateManager.Types.SEARCH_SET_SIZE
                 );
 
         logger.debug(format("validating project contents by %d predices", lPredicates.size()));
         for (Predicate lProjectPredicate : lPredicates) {
             try {
                 boolean lResult = lProjectPredicate.apply(relimsProjectBean);
                 if (!lResult) {
                     logger.error("END " + lProjectId);
                     //Try anyway...with defaults in searchparams
                     //     return false;
                 }
             } catch (NullPointerException e) {
                 logger.error("No analyzerdata found !");
                 //Try anyway...with defaults in searchparams
                 //return false;
             }
         }
 
         try {
             searchGUIJobBean = new SearchGUIJobBean(relimsProjectBean);
             sampleID = searchGUIJobBean.getName();
         logger.debug("acquiring the search results with SearchGUI");
             if (searchGUIJobBean.launch() == 0) {
                 experimentID = sampleID;
                 return true;
             } else {
                 progressManager.setEndState(Checkpoint.PROCESSFAILURE);
                 return false;
             }
         } catch (Exception e) {
             logger.error("ERROR OCCURRED FOR PROJECT " + lProjectId);
             logger.error(e);
             e.printStackTrace();
             progressManager.setState(Checkpoint.FAILED, e);
         } finally {
             dataProvider.clearResources();
             return true;
         }
     }
 
     //Prepare the peptideshaker job using the output from searchgui
     private boolean prepareAndLaunchPeptideShaker() {
         //PEPTIDESHAKER -----------------------------------------------------------------------
         logger.debug("processing the search results with PeptideShaker");
         File peptideShakerFolder = new File(RelimsProperties.getPeptideShakerArchivePath().replace(RelimsProperties.getPeptideShakerArchive(), ""));
         Command.setWorkFolder(peptideShakerFolder);
         lPeptideShakerJobBean = new PeptideShakerJobBean(relimsProjectBean);
         double lFDR = RelimsProperties.getFDR();
         lPeptideShakerJobBean.setPepfdr(lFDR);
         lPeptideShakerJobBean.setProtfdr(lFDR);
         lPeptideShakerJobBean.setPsmfdr(lFDR);
         lPeptideShakerJobBean.setSampleName(sampleID);
         lPeptideShakerJobBean.setExperimentName(experimentID);
         lPeptideShakerJobBean.setAscore(true);
         try {
             lPeptideShakerJobBean.setMaxPrecursorError(relimsProjectBean.getPrecursorError());
         } catch (NullPointerException e) {
             //this can happen when loaded from repository
             SearchParameters params = relimsProjectBean.getSearchParameters();
             lPeptideShakerJobBean.setMaxPrecursorError(params.getPrecursorAccuracy());
         }
         // Run PeptideShaker
         // IF the return value = 0 (= system.exit.value) then the process ran correctly. (Timeout etc will change this value)
         if (lPeptideShakerJobBean.launch() == 0) {
             logger.debug(format(
                     "finished PeptideShakerCLI on project '%s', sample '%s'",
                     experimentID,
                     sampleID));
             storeInRepository();
             return true;
         } else {
             progressManager.setState(Checkpoint.PROCESSFAILURE);
             return false;
         }
     }
 
     @Override
     public String call() {
         logger.setLevel(Level.DEBUG);
         if (projectProvider instanceof PrideProjectProvider) {
             searchResultFolder = RelimsProperties.createWorkSpace(projectID, "pride");
         } else {
             searchResultFolder = RelimsProperties.createWorkSpace(projectID, "mslims");
         }
 
         String provider = null;
         boolean runPeptideshaker;
 
 
         if (projectProvider.getClass().toString().contains("mslims")) {
             provider = "mslims";
         } else {
             provider = "pride";
         }
 
         if (!RepositoryManager.hasBeenRun(provider, projectID)) {
             logger.debug("Project was not located in repository. Building from scratch...");
             relimsProjectBean = makeRelimsJobBean();
         } else {
             logger.debug("Project was located in repository. Building from files...");
             File repositorySpectrumFile = new File(RelimsProperties.getWorkSpace() + "/" + projectID + ".mgf");
             File repositorySearchParametersFile = new File(RelimsProperties.getWorkSpace() + "/SearchGUI.parameters");
             relimsProjectBean = new RelimsProjectBean(projectID, repositorySpectrumFile, repositorySearchParametersFile);
         }
             try {
                 runPeptideshaker = runSearchGUI();
                 if (runPeptideshaker) {
                     logger.debug("Preparing peptideshaker");
                     prepareAndLaunchPeptideShaker();
                     progressManager.setEndState(Checkpoint.FINISHED);
                 }
             } catch (Throwable e) {
                 e.printStackTrace();
                 logger.error("ERROR OCCURRED FOR PROJECT " + projectID);
                 logger.error(e);
                 if (e instanceof java.lang.IllegalArgumentException) {
                     progressManager.setEndState(Checkpoint.MODFAILURE);
                 } else {
                     if (provider.equals("pride")) {
                         progressManager.setEndState(Checkpoint.PRIDEFAILURE);
                     } else {
                         progressManager.setState(Checkpoint.FAILED, e);
                     }
                 }
             } finally {
                 RelimsProperties.saveLoggedFiles();
             }
             //nullcheck to prevent standalone relims to delete its folders
             if (ProcessVariableManager.getClassicMode()) {
                 if (progressManager.getEndState() == Checkpoint.FAILED || progressManager.getEndState() == Checkpoint.PRIDEFAILURE) {
                     //              fileGrabber.deleteResultFolder();
                 }
             }
         return "";
     }
 
     private void storeInRepository() {
         if (projectProvider.getClass().toString().contains("mslims")) {
             RepositoryManager.copyToRepository("mslims", RelimsProperties.getWorkSpace(), projectID);
             logger.debug("Stored sourcefiles (MGF / SearchParameters) to MSLIMS repository");
         } else {
             RepositoryManager.copyToRepository("pride", RelimsProperties.getWorkSpace(), projectID);
             logger.debug("Stored sourcefiles (MGF / SearchParameters) to PRIDE repository");
         }
     }
 }
