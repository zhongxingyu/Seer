 package com.compomics.relims.model.beans;
 
 import com.compomics.omssa.xsd.UserMod;
 import com.compomics.relims.concurrent.Command;
 import com.compomics.relims.conf.RelimsProperties;
 import com.compomics.relims.manager.progressmanager.ProgressManager;
 import com.compomics.relims.manager.variablemanager.ProcessVariableManager;
 import com.compomics.util.experiment.biology.*;
 import com.compomics.util.experiment.identification.SearchParameters;
 import com.compomics.util.experiment.massspectrometry.Charge;
 import com.compomics.util.preferences.ModificationProfile;
 import com.google.common.base.Function;
 import com.google.common.collect.Sets;
 import no.uib.jsparklines.data.XYDataPoint;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.log4j.Logger;
 
 import javax.annotation.Nullable;
 import java.io.*;
 import java.util.*;
 
 /**
  * This class is a
  */
 public class SearchGUIJobBean {
 
     /**
      * a plain logger
      */
     private static Logger logger = Logger.getLogger(SearchGUIJobBean.class);
     private File spectra;
     private String iName = null;
     private RelimsProjectBean iRelimsProjectBean = null;
     private File iSearchResultFolder = null;
     private List<File> iSpectrumFiles = new ArrayList<File>();
     protected PropertiesConfiguration iSearchGuiConfiguration;
     protected long iTimeStamp;
     private ArrayList<String> searchGUICommandLine;
     private SearchParameters searchParameters;
     private File searchParametersFile;
     private EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
     private PTMFactory ptmFactory = PTMFactory.getInstance();
     private String iProjectProvider;
     private ProgressManager progressManager = ProgressManager.getInstance();
     private String command;
 
     public SearchGUIJobBean(String aName, String provider, File searchParameters, File aSpectrumFile) {
 
         try {
             iProjectProvider = provider;
             searchParametersFile = searchParameters;
             iName = aName;
             iSpectrumFiles.add(aSpectrumFile);
         } catch (Exception e) {
             logger.error(e.getMessage(), e);
         }
     }
 
     public SearchGUIJobBean(String aName, String provider, RelimsProjectBean aRelimsProjectBean, List<File> aSpectrumFiles) {
         try {
             iProjectProvider = provider;
             iRelimsProjectBean = aRelimsProjectBean.clone();
         } catch (CloneNotSupportedException e) {
             logger.error(e.getMessage(), e);
         }
         iName = aName;
         iSpectrumFiles = aSpectrumFiles;
     }
 
     public SearchGUIJobBean(String aName, String provider, RelimsProjectBean aRelimsProjectBean, File aSpectrumFile) {
 
         try {
             this.searchParametersFile = null;
             iProjectProvider = provider;
             iRelimsProjectBean = aRelimsProjectBean.clone();
         } catch (CloneNotSupportedException e) {
             logger.error(e.getMessage(), e);
         }
         iName = aName;
         iSpectrumFiles.add(aSpectrumFile);
 
     }
 
     public SearchGUIJobBean(String aName, String provider, RelimsProjectBean aRelimsProjectBean, File aSpectrumFile, File aSearchParametersFile) {
         try {
             this.searchParametersFile = aSearchParametersFile;
             iProjectProvider = provider;
             iRelimsProjectBean = aRelimsProjectBean.clone();
         } catch (CloneNotSupportedException e) {
             logger.error(e.getMessage(), e);
         }
         iName = aName;
         iSpectrumFiles.add(aSpectrumFile);
     }
 
     public String getiName() {
         return iName;
     }
 
     public void setiName(String iName) {
         this.iName = iName;
     }
 
     public RelimsProjectBean getiRelimsProjectBean() {
         return iRelimsProjectBean;
     }
 
     public void setiRelimsProjectBean(RelimsProjectBean aRelimsProjectBean) {
         try {
             iRelimsProjectBean = aRelimsProjectBean.clone();
         } catch (CloneNotSupportedException e) {
             logger.error(e.getMessage(), e);
         }
     }
 
     public List<File> getiSpectrumFiles() {
         return iSpectrumFiles;
     }
 
     public void setiSpectrumFiles(List<File> iSpectrumFiles) {
         this.iSpectrumFiles = iSpectrumFiles;
     }
 
     public ArrayList<String> generateCommand() throws IOException, ConfigurationException {
 
         searchGUICommandLine = new ArrayList<String>();
 
         //the actual command
         searchGUICommandLine.clear();
         //  searchGUICommandLine.append(RelimsProperties.getSearchGuiFolder());
         searchGUICommandLine.add("java ");
         searchGUICommandLine.add("-cp ");
         searchGUICommandLine.add(RelimsProperties.getSearchGuiArchive());
         searchGUICommandLine.add(" eu.isas.searchgui.cmd.SearchCLI ");
 
         searchGUICommandLine.add("-spectrum_files ");
 
         //TODO THIS HAS TO BE A LIST OF THE SPECTRA
         //FOR NOW FIX              
 
         for (File aSpectrum : iSpectrumFiles) {
             searchGUICommandLine.add(iSpectrumFiles.get(0).getAbsolutePath().toString());
             if (iSpectrumFiles.indexOf(aSpectrum) != iSpectrumFiles.size() && iSpectrumFiles.indexOf(aSpectrum) != 0) {
                 searchGUICommandLine.add(",");
             }
         }
 
         searchGUICommandLine.add(" -output_folder ");
         //figure out what the projectprovider was
         searchGUICommandLine.add(ProcessVariableManager.getResultsFolder() + "/");
 
         searchGUICommandLine.add(" -search_params ");
         searchGUICommandLine.add(searchParametersFile.getAbsolutePath());
 
         searchGUICommandLine.add(" -ppm ");
         searchGUICommandLine.add("2");
         System.err.println("");
         System.err.println("SEARCHGUICOMMAND");
         StringBuilder commandLine = new StringBuilder();
         for (String aParam : searchGUICommandLine) {
             commandLine.append(aParam);
         }
         return searchGUICommandLine;
 
     }
 
     public void setSearchParameters(SearchParameters searchParameters) {
         this.searchParameters = searchParameters;
     }
 
     protected void createSearchParametersFile() {
 
         File searchParametersRepositoryFile = new File(ProcessVariableManager.getSearchResultFolder() + "/SearchGUI.parameters");
         File fastaFile = new File(RelimsProperties.getDefaultSearchDatabase());
         //makeBlastDB(fastaFile) Searchgui should take care of this !;
         if (searchParametersRepositoryFile.exists()) {
             try {
                 searchParameters = SearchParameters.getIdentificationParameters(searchParametersRepositoryFile);
                 searchParameters.setFastaFile(fastaFile);
                 logger.debug("USING FASTA : " + searchParameters.getFastaFile().getAbsolutePath());
             } catch (FileNotFoundException ex) {
                 logger.error(ex);
             } catch (IOException ex) {
                 logger.error(ex);
             } catch (ClassNotFoundException ex) {
                 logger.error(ex);
             }
         } else {
             try {
                 searchParameters = new SearchParameters();// this should be default..;
                 searchParameters.setFastaFile(fastaFile);
                 logger.debug("USING FASTA : " + searchParameters.getFastaFile().getAbsolutePath());
             } catch (Exception e) {
                 e.printStackTrace();
             }
             logger.debug("Project was not found in repository. Remaking searchparameters file");
 
             logger.debug("building modification profile for searchparameters file");
             ModificationProfile modProfile = new ModificationProfile();
             List<UserMod> lStandardModificationList = iRelimsProjectBean.getStandardModificationList();
             List<UserMod> lExtraModificationList = iRelimsProjectBean.getExtraModificationList();
 
             Function<UserMod, PTM> UserModToPTMFunction = new Function<UserMod, PTM>() {
                 @Override
                 public PTM apply(@Nullable UserMod input) {
                     // Prepare the position as an AminoAcidPattern
                     AminoAcidPattern pos = new AminoAcidPattern();
                     ArrayList<AminoAcid> target = new ArrayList<AminoAcid>();
                     for (char aa : input.getLocation().toCharArray()) {
                         target.add(AminoAcid.getAminoAcid(aa));
                     }
                     pos.setTargeted(0, target);
 
                     // Create a PTM instance from the UserMod data
                     PTM res = new PTM(
                             input.getLocationType().getLocationTypeID(),
                             input.getModificationName().toLowerCase(),
                             input.getMass(),
                             pos);
                     return res;
                 }
             };
 
             // Join all default and usermod names from the current 
             HashSet<String> ptmFactoryAllModifications = Sets.newHashSet();
             ptmFactoryAllModifications.addAll(ptmFactory.getDefaultModifications());
             ptmFactoryAllModifications.addAll(ptmFactory.getUserModifications());
 
             // Join all Relims Modifications needed for this SearchGUIJob
             HashSet<UserMod> jobUserMods = Sets.newHashSet();
             jobUserMods.addAll(lStandardModificationList);
             jobUserMods.addAll(lExtraModificationList);
 
             // Iterate over the relims modifications
             for (UserMod aMod : jobUserMods) {
                 try {
                     PTM aModPTM = UserModToPTMFunction.apply(aMod);
 
                     boolean isInFactory = false;
                     // Check whether the relims modification name is known in the PTMFactory  
                     for (String lPtmName : ptmFactoryAllModifications) {
                         PTM lPTM = ptmFactory.getPTM(lPtmName);
                         if (lPTM.isSameAs(aModPTM)) {
 
                             isInFactory = true;
                             aModPTM = lPTM;
                             //OMSSA is caps sensitive
                             aModPTM.setName(aModPTM.getName().toLowerCase());
                             break;
                         }
                     }
                     // If not known, convert to Utilities-PTM instance, and add keep the modification name.
                     if (!isInFactory) {
                         ptmFactory.addUserPTM(aModPTM);
                         ptmFactoryAllModifications.add(aModPTM.getName().toLowerCase());
                     }
                     // Add the PTM to the ModificationProfile instance that will be used in the SearchParameters instance.
                     if (aMod.isFixed()) {
                         if (!modProfile.getFixedModifications().contains(aModPTM.getName())) {
                             modProfile.addFixedModification(ptmFactory.getPTM(aModPTM.getName().toLowerCase()));
                             logger.debug("Added fixed modification : " + aModPTM.getName().toLowerCase());
                         }
                     } else {
                         if (!modProfile.getVariableModifications().contains(aModPTM.getName())) {
                             modProfile.addVariableModification(ptmFactory.getPTM(aModPTM.getName().toLowerCase()));
                             logger.debug("Added variable modification : " + aModPTM.getName().toLowerCase());
                         }
                     }
                     //  ptmFactory.addUserPTM(aModPTM);
                 } catch (Exception e) {
                     logger.error(e);
                     logger.debug("failed to set " + aMod + " in the modificationprofile.");
                 } finally {
                 }
 
             }
             // persist the usermods to the usermod.xml
             File relimsUserMod = RelimsProperties.getUserModsFile();
             try {
                 logger.debug("Writing OMSSA modifications to " + relimsUserMod);
                 ptmFactory.writeOmssaUserModificationFile(relimsUserMod);
             } catch (IOException ex) {
                 logger.error(ex);
             } finally {
             }
             searchParameters.setModificationProfile(modProfile);
             // Set other parameters (defaults)
             try {
                 File enzymesFile = new File(RelimsProperties.getSearchGuiFolder() + "/resources/conf/searchGUI_enzymes.xml");
                 enzymeFactory.importEnzymes(enzymesFile);
                 Enzyme enzyme = iRelimsProjectBean.getEnzyme();
                 if (iRelimsProjectBean.getEnzyme() == null) {
                     enzyme = enzymeFactory.getEnzyme("Trypsin");
                 }
                 searchParameters.setEnzyme(enzyme);
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
             searchParameters.setPrecursorAccuracy(iRelimsProjectBean.getPrecursorError());
             searchParameters.setFragmentIonAccuracy(iRelimsProjectBean.getFragmentError());
             searchParameters.setnMissedCleavages(RelimsProperties.getMissedCleavages());
             // Precursor
             searchParameters.setPrecursorAccuracyType(SearchParameters.PrecursorAccuracyType.DA);
            int maxCharge;
            int minCharge;
            try {
                maxCharge = Collections.max(iRelimsProjectBean.getCharges());
            } catch (NullPointerException e) {
                maxCharge = 4;
                logger.error("No max charge found, setting default to 4");
            }
            try {
                minCharge = Collections.min(iRelimsProjectBean.getCharges());
            } catch (NullPointerException e) {
                minCharge = 1;
                logger.error("No min charge found, setting default to 1");
            }
             Charge maxChargeSearched = new Charge(1, maxCharge);
             Charge minChargeSearched = new Charge(1, minCharge);
             searchParameters.setMaxChargeSearched(maxChargeSearched);
             searchParameters.setMinChargeSearched(minChargeSearched);
             validate(searchParameters);
         }
         logger.info("Searchparameters were loaded");
         try {
             //save the file to the correct folder
             searchParametersFile = new File(RelimsProperties.getWorkSpace().toString() + "/SearchGUI.parameters");
             SearchParameters.saveIdentificationParameters(searchParameters, searchParametersFile);
             logger.debug("Loaded parameters and stored at " + searchParametersFile.getAbsolutePath());
         } catch (FileNotFoundException ex) {
             ex.printStackTrace();
         } catch (IOException ex) {
             ex.printStackTrace();
         } catch (ClassNotFoundException ex) {
             ex.printStackTrace();
         }
     }
 
     private void validate(SearchParameters searchParameters) {
         //ENZYME
         if (searchParameters.getEnzyme() == null) {
             logger.debug("Enzyme was null!");
             searchParameters.setEnzyme(enzymeFactory.getEnzyme("Trypsin"));
         }
         logger.debug("Using enzyme : " + searchParameters.getEnzyme());
 
 
         if (searchParameters.getFastaFile() == null) {
             logger.debug("Fasta was null!");
             searchParameters.setFastaFile(new File(RelimsProperties.getDefaultSearchDatabase()));
         }
         logger.debug("Using Fasta : " + searchParameters.getFastaFile().getName());
 
 
         if (searchParameters.getFractionMolecularWeightRanges() == null) {
             logger.debug("Fraction Molecular Weight Range was null!");
             searchParameters.setFractionMolecularWeightRanges(new HashMap<String, XYDataPoint>());
         }
         logger.debug("Using Fraction Molecular Weight Range : " + searchParameters.getFractionMolecularWeightRanges());
 
         if (searchParameters.getFragmentIonAccuracy() == null) {
             logger.debug("Fragment Ion Accuracy was null");
             searchParameters.setFragmentIonAccuracy(0.0);
         }
         logger.debug("Using Fragment Ion Accuracy : " + searchParameters.getFractionMolecularWeightRanges());
 
         if (searchParameters.getPrecursorAccuracy() == null) {
             logger.debug("Precursor Accuracy was null!");
             searchParameters.setPrecursorAccuracy(0.0);
         }
         logger.debug("Using Precursor Accuracy : " + searchParameters.getPrecursorAccuracy());
 
 
         if (searchParameters.getHitListLength() == null) {
             logger.debug("Hitlist length was null!");
             searchParameters.setHitListLength(0);
         }
         logger.debug("Using Hitlist length : " + searchParameters.getHitListLength());
 
         if (searchParameters.getIonSearched1() == null) {
             logger.debug("Ion 1 was null!");
             searchParameters.setIonSearched1("");
         }
         logger.debug("Using Ion 1 searched : " + searchParameters.getIonSearched1());
         if (searchParameters.getIonSearched2() == null) {
             logger.debug("Ion 2 was null!");
             searchParameters.setIonSearched2("");
         }
 
         logger.debug("Using Ion 2 searched : " + searchParameters.getIonSearched2());
 
         if (searchParameters.getMaxEValue() == null) {
             logger.debug("Max E-value was null!");
             searchParameters.setMaxEValue(0.0);
         }
 
         logger.debug("Using Max E-value : " + searchParameters.getMaxEValue());
 
         if (searchParameters.getMaxPeptideLength() == null) {
             logger.debug("Max peptide length was null!");
             searchParameters.setMaxPeptideLength(0);
         }
 
         logger.debug("Using Max Peptide Length : " + searchParameters.getMaxPeptideLength());
 
         ArrayList<String> fixModList = searchParameters.getModificationProfile().getFixedModifications();
         logger.debug("Using Fixed Mod profile : ");
         for (String aMod : fixModList) {
             logger.debug("Fixed Modification : " + aMod);
         }
         ArrayList<String> varModList = searchParameters.getModificationProfile().getVariableModifications();
         logger.debug("Using Variable Mod profile : ");
         for (String aMod : varModList) {
             logger.debug("Var Modification : " + aMod);
         }
         if (searchParameters.getnMissedCleavages() == null) {
             logger.debug("Missed Cleavages was null!");
             searchParameters.setnMissedCleavages(0);
         }
         logger.debug("Using Missed Cleavages : " + searchParameters.getnMissedCleavages());
 
         if (searchParameters.getMaxChargeSearched() == null) {
             logger.debug("Max Charge Searched was null!");
             searchParameters.setMaxChargeSearched(new Charge(1, 4));
         }
         logger.debug("Using Max Charge : " + searchParameters.getMaxChargeSearched());
 
         if (searchParameters.getMinChargeSearched() == null) {
             logger.debug("Min Charge Searched was null!");
             searchParameters.setMinChargeSearched(new Charge(1, 1));
         }
         logger.debug("Using Min Charge : " + searchParameters.getMinChargeSearched());
     }
 
     protected void prepare() {
         //Create the resultFolder
         iTimeStamp = System.currentTimeMillis();
         iSearchResultFolder = new File(ProcessVariableManager.getSearchResultFolder());
         ProcessVariableManager.setSearchResultFolder(iSearchResultFolder.toString());
         createSearchParametersFile();
     }
 
     //protected abstract void applyChildMethods() throws Exception;
     public String getName() {
         return iName;
     }
 
     public File getSearchResultFolder() {
         return iSearchResultFolder;
     }
 
     public void setSearchResultFolder(File aSearchResultFolder) {
         iSearchResultFolder = aSearchResultFolder;
     }
 
     public void setParametersFile(File repositoryParametersFile) {
         this.searchParametersFile = repositoryParametersFile;
         try {
             searchParameters = SearchParameters.getIdentificationParameters(repositoryParametersFile);
             searchParameters.setFastaFile(new File(RelimsProperties.getDefaultSearchDatabase()));
         } catch (FileNotFoundException ex) {
             ex.printStackTrace();
         } catch (IOException ex) {
             ex.printStackTrace();
         } catch (ClassNotFoundException ex) {
             ex.printStackTrace();
         }
     }
 
     public int launch() throws IOException, ConfigurationException {
         prepare();
         File searchGuiFolder = new File(RelimsProperties.getSearchGuiFolder());
         Command.setWorkFolder(searchGuiFolder);
         logger.debug("Launching searchgui from" + searchGuiFolder.getAbsolutePath());
         StringBuilder totalCommandLine = new StringBuilder();
         searchGUICommandLine = generateCommand();
 
         for (String aCmd : searchGUICommandLine) {
             totalCommandLine.append(aCmd);
         }
         logger.debug("Launching command : ");
         logger.debug(totalCommandLine.toString());
         return Command.call(totalCommandLine.toString());
     }
 }
