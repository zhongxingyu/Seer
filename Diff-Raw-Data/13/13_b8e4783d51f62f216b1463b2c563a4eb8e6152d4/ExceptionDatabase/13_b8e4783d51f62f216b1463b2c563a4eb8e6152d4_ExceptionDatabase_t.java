 package de.smeo.tools.exceptionmonitor.common;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.smeo.tools.exceptionmonitor.common.ExceptionDatabase.CategorizedExceptions;
 import de.smeo.tools.exceptionmonitor.exceptionparser.EqualCauseExceptionChainContainer;
 import de.smeo.tools.exceptionmonitor.exceptionparser.ExceptionCausedByChain;
 
 /**
  * Storage for know exceptions and source for statistics etc
  * @author smeo
  */
 public class ExceptionDatabase {
 	private Map<String, List<ExceptionCausedByChain>> absFilenameToknownExceptionSamples = new HashMap<String, List<ExceptionCausedByChain>>();
 	private File storageFile;
 	
 	public ExceptionDatabase(String storageFile) {
 		openOrCreateFile(storageFile);
 		loadStorageFromFile();
 	}
 
 	private void loadStorageFromFile() {
 		absFilenameToknownExceptionSamples = (Map<String, List<ExceptionCausedByChain>> ) FileUtils.readObjectFromFile(storageFile);
		if (absFilenameToknownExceptionSamples == null){
			absFilenameToknownExceptionSamples =  new HashMap<String, List<ExceptionCausedByChain>>();
		}
 	}
 	
 	public void saveStorageToFile() {
 		FileUtils.writeObjectToFile(absFilenameToknownExceptionSamples, storageFile);
 	}
 	
 	private void openOrCreateFile(String storageFileName) {
 		storageFile = new File(storageFileName);
 		if (!storageFile.exists()){
 			try {
 				storageFile.createNewFile();
 				saveStorageToFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void updateDatabase(
 			File file,
 			List<ExceptionCausedByChain> newExceptions) {
 		
 		for (ExceptionCausedByChain currExceptionCausedByChain : newExceptions){
 			boolean isKnownException = false;
 	
 			List<ExceptionCausedByChain> exceptionCausedByChainsForFile = absFilenameToknownExceptionSamples.get(file.getAbsolutePath());
 			if (exceptionCausedByChainsForFile == null){
 				exceptionCausedByChainsForFile = new ArrayList<ExceptionCausedByChain>();
 				absFilenameToknownExceptionSamples.put(file.getAbsolutePath(), exceptionCausedByChainsForFile);
 			}
 			
 			isKnownException = listContainsExceptionWithSameRootCause(currExceptionCausedByChain, exceptionCausedByChainsForFile);
 			if (!isKnownException){
 				exceptionCausedByChainsForFile.add(currExceptionCausedByChain);
 			}
 		}
 	}
 
 	public CategorizedExceptions categorizeExceptions(
 			File file,
 			List<ExceptionCausedByChain> newExceptions) {
 		
 		CategorizedExceptions categorizedExceptions = new CategorizedExceptions();
 		
 		for (ExceptionCausedByChain currExceptionCausedByChain : newExceptions){
 			boolean isKnownException = false;
 	
 			List<ExceptionCausedByChain> exceptionCausedByChainsForFile = absFilenameToknownExceptionSamples.get(file.getAbsolutePath());
 			if (exceptionCausedByChainsForFile == null){
 				exceptionCausedByChainsForFile = new ArrayList<ExceptionCausedByChain>();
 				absFilenameToknownExceptionSamples.put(file.getAbsolutePath(), exceptionCausedByChainsForFile);
 			}
 			
 			isKnownException = listContainsExceptionWithSameRootCause(currExceptionCausedByChain, exceptionCausedByChainsForFile);
 			if (isKnownException){
 				categorizedExceptions.addKnownException(currExceptionCausedByChain);
 			} else {
 				categorizedExceptions.addYetUnkownException(currExceptionCausedByChain);
 			}
 		}
 		
 		return categorizedExceptions;
 	}
 
 	private boolean listContainsExceptionWithSameRootCause(ExceptionCausedByChain exception, List<ExceptionCausedByChain> exceptionList){
 		for (ExceptionCausedByChain currExceptionCausedByChain : exceptionList){
 			if (currExceptionCausedByChain.hasEqualRootCause(exception)){
 				return true;
 			}
 		}
 		return false;
 	}
 		
 	public static class CategorizedExceptions {
 		private List<EqualCauseExceptionChainContainer> knownExceptions = new ArrayList<EqualCauseExceptionChainContainer>();
 		private List<EqualCauseExceptionChainContainer> yetUnknownExceptions = new ArrayList<EqualCauseExceptionChainContainer>();
 		
 		public boolean hasYetUnkownExceptions(){
 			return (yetUnknownExceptions.size() > 0);
 		}
 		public List<EqualCauseExceptionChainContainer> getKnownExceptions() {
 			return knownExceptions;
 		}
 
 		public List<EqualCauseExceptionChainContainer> getYetUnknownExceptions() {
 			return yetUnknownExceptions;
 		}
 
 		public void addKnownException(ExceptionCausedByChain knownException){
 			addExceptionToExceptionChainContainerList(knownException, knownExceptions);
 		}
 		
 		public void addYetUnkownException(ExceptionCausedByChain unkownException){
 			addExceptionToExceptionChainContainerList(unkownException, yetUnknownExceptions);
 		}
 		
 		private void addExceptionToExceptionChainContainerList(ExceptionCausedByChain exceptionCausedByChain, List<EqualCauseExceptionChainContainer> exceptionContainers){
 			boolean exceptionAdded = false;
 			for (int i=0; !exceptionAdded && (i < exceptionContainers.size()); i++){
 				EqualCauseExceptionChainContainer equalCauseExceptionChainContainer = exceptionContainers.get(i);
 				exceptionAdded = equalCauseExceptionChainContainer.addExceptionIfHasEqualRootCause(exceptionCausedByChain);
 			}
 			
 			if (!exceptionAdded){
 				exceptionContainers.add(new EqualCauseExceptionChainContainer(exceptionCausedByChain));
 			}
 		}
 	}
 
 }
