 package com.compomics.relims.concurrent;
 
 import com.compomics.relims.conf.RelimsProperties;
 import com.compomics.relims.model.beans.RelimsProjectBean;
 import com.compomics.relims.model.beans.SearchGUIJobBean;
 import com.compomics.relims.model.guava.functions.SearchGuiModStringFunction;
 import com.google.common.base.Function;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * This class is a
  */
 public abstract class SearchCommandGenerator {
 
     private static Logger logger = Logger.getLogger(SearchCommandGenerator.class);
 
     private String iName = null;
     private RelimsProjectBean iRelimsProjectBean = null;
 
     private File iSearchResultFolder = null;
     private List<File> iSpectrumFiles;
     protected PropertiesConfiguration iSearchGuiConfiguration;
     protected long iTimeStamp;
 
 
     public SearchCommandGenerator(String aName, RelimsProjectBean aRelimsProjectBean, List<File> aSpectrumFiles) {
         try {
             iRelimsProjectBean = aRelimsProjectBean.clone();
         } catch (CloneNotSupportedException e) {
             logger.error(e.getMessage(), e);
         }
 
         iName = aName;
         iSpectrumFiles = aSpectrumFiles;
     }
 
     public String generateCommand() throws IOException, ConfigurationException {
         prepare();
         applyChildMethods();
 
         SearchGUIJobBean lSearchGUIJobBean = null;
         try {
             lSearchGUIJobBean = makeSearchGuiJobBean();
         } catch (IOException e) {
             logger.error(e.getMessage(), e);
         } catch (ConfigurationException e) {
             logger.error(e.getMessage(), e);
         }
         return lSearchGUIJobBean.getSearchGUICommandString();
 
     }
 
     protected void prepare() {
         iSearchGuiConfiguration = RelimsProperties.getDefaultSearchGuiConfiguration();
         iTimeStamp = System.currentTimeMillis();
 
         iSearchResultFolder = new File(RelimsProperties.getWorkSpace(), ("" + getProjectId() + "_" + iName + "_" + iTimeStamp));
         iSearchResultFolder.mkdir();
 
         logger.debug("setting modifications to searchgui configuration file");
         Function<List<String>, String> lModFormatter = new SearchGuiModStringFunction();
         iSearchGuiConfiguration.setProperty("FIXED_MODIFICATIONS", lModFormatter.apply(iRelimsProjectBean.getFixedMatchedPTMs()));
         iSearchGuiConfiguration.setProperty("VARIABLE_MODIFICATIONS", lModFormatter.apply(iRelimsProjectBean.getVariableMatchedPTMs()));
 
         iSearchGuiConfiguration.setProperty("PRECURSOR_MASS_TOLERANCE", iRelimsProjectBean.getPrecursorError());
         iSearchGuiConfiguration.setProperty("FRAGMENT_MASS_TOLERANCE", iRelimsProjectBean.getFragmentError());
         iSearchGuiConfiguration.setProperty("MISSED_CLEAVAGES", RelimsProperties.getMissedCleavages());
        iSearchGuiConfiguration.setProperty("PRECURSOR_MASS_TOLERANCE_UNIT", "Da");
 
         String lSearchDatabase = RelimsProperties.getDefaultSearchDatabase();
         logger.debug(String.format("setting DEFAULT fasta database '%s' to searchgui configuration", lSearchDatabase));
         iSearchGuiConfiguration.setProperty("DATABASE_FILE", lSearchDatabase);
     }
 
 
     protected abstract void applyChildMethods();
 
     public String getName() {
         return iName;
     }
 
     public long getProjectId() {
         return iRelimsProjectBean.getProjectID();
     }
 
     public File getSearchResultFolder() {
         return iSearchResultFolder;
     }
 
     public void setSearchResultFolder(File aSearchResultFolder) {
         iSearchResultFolder = aSearchResultFolder;
     }
 
 
     private SearchGUIJobBean makeSearchGuiJobBean() throws IOException, ConfigurationException {
         File lSearchGuiConfigurationFile = RelimsProperties.getTmpFile("" + iRelimsProjectBean.getProjectID() + "_" + iName + "_" + iTimeStamp);
 
         logger.debug("saving searchgui configuration to " + lSearchGuiConfigurationFile.getCanonicalPath());
         iSearchGuiConfiguration.save(lSearchGuiConfigurationFile);
 
         SearchGUIJobBean lSearchGUIJobBean = new SearchGUIJobBean();
         lSearchGUIJobBean.setSearch(true);
         lSearchGUIJobBean.setGui(false);
         lSearchGUIJobBean.setOmssa(RelimsProperties.useOmssa());
         lSearchGUIJobBean.setXtandem(RelimsProperties.useTandem());
         lSearchGUIJobBean.setConfig(lSearchGuiConfigurationFile);
         lSearchGUIJobBean.setSpectra(iSpectrumFiles);
         lSearchGUIJobBean.setResults(iSearchResultFolder);
 
         logger.debug("starting new searchgui process");
         logger.debug("writing results to " + iSearchResultFolder.getCanonicalPath());
         return lSearchGUIJobBean;
     }
 }
