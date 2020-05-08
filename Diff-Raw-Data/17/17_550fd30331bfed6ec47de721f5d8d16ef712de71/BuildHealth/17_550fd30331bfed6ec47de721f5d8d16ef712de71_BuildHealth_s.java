 package org.pescuma.buildhealth.core;
 
 import static org.apache.commons.io.FileUtils.*;
 import static org.pescuma.buildhealth.utils.FileHelper.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.ServiceLoader;
 
 import org.apache.commons.io.FileUtils;
 import org.pescuma.buildhealth.analyser.BuildHealthAnalyser;
 import org.pescuma.buildhealth.computer.BuildDataComputer;
 import org.pescuma.buildhealth.computer.BuildDataComputerException;
 import org.pescuma.buildhealth.computer.BuildDataComputerTracker;
 import org.pescuma.buildhealth.core.data.BuildDataTable;
 import org.pescuma.buildhealth.core.data.DiskBuildData;
 import org.pescuma.buildhealth.core.listener.BuildHealthListener;
 import org.pescuma.buildhealth.core.listener.CompositeBuildHealthListener;
 import org.pescuma.buildhealth.extractor.BuildDataExtractor;
 import org.pescuma.buildhealth.extractor.BuildDataExtractorTracker;
 import org.pescuma.buildhealth.prefs.DiskPreferencesStore;
 import org.pescuma.buildhealth.prefs.LowercaseDiskPreferencesStore;
 import org.pescuma.buildhealth.prefs.LowercasePreferencesStore;
 import org.pescuma.buildhealth.prefs.MemoryPreferencesStore;
 import org.pescuma.buildhealth.prefs.Preferences;
 import org.pescuma.buildhealth.prefs.PreferencesStore;
 import org.pescuma.buildhealth.prefs.PropertiesPreferencesStore;
 
 import com.google.common.base.Strings;
 
 public class BuildHealth {
 	
 	private static final String DEFAULT_FOLDER_NAME = ".buildhealth";
 	
 	private final File home;
 	private final BuildData table;
 	private final PreferencesStore store;
 	private final Preferences preferences;
 	private final List<BuildHealthAnalyser> analysers = new ArrayList<BuildHealthAnalyser>();
 	private final CompositeBuildHealthListener listeners = new CompositeBuildHealthListener();
 	
 	public BuildHealth() {
 		this(null);
 	}
 	
 	public BuildHealth(File home) {
 		this(home, getDefaultBuildData(home), getDefaultPreferencesStore(home));
 	}
 	
 	public BuildHealth(File home, BuildData table, PreferencesStore store) {
 		this.home = getCanonicalFile(home);
 		this.table = table;
 		this.store = store;
 		preferences = new Preferences(store);
 	}
 	
 	public void shutdown() {
 		if (table instanceof DiskBuildData)
 			((DiskBuildData) table).saveToDisk();
 		
 		if (store instanceof DiskPreferencesStore)
 			((DiskPreferencesStore) store).saveToDisk();
 	}
 	
 	public Preferences getPreferences() {
 		return preferences;
 	}
 	
 	public boolean addListener(BuildHealthListener listener) {
 		return listeners.addListener(listener);
 	}
 	
 	public boolean removeListener(BuildHealthListener listener) {
 		return listeners.removeListener(listener);
 	}
 	
 	public void startNewBuild() {
 		if (home == null)
 			throw new IllegalStateException();
 		
 		try {
 			
 			if (home.exists())
 				forceDelete(home);
 			
 			forceMkdir(home);
 			
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public void addAnalyser(BuildHealthAnalyser analyser) {
 		analysers.add(analyser);
 	}
 	
 	public void addAnalysersFromServices() {
		ServiceLoader<BuildHealthAnalyser> locator = ServiceLoader.load(BuildHealthAnalyser.class);
 		for (BuildHealthAnalyser analyser : locator)
 			addAnalyser(analyser);
 	}
 	
 	public List<BuildHealthAnalyser> getAnalysers() {
 		return Collections.unmodifiableList(analysers);
 	}
 	
 	public void compute(final BuildDataComputer computer) {
 		File computedFolder = new File(home, "computed");
 		
 		try {
 			forceMkdir(computedFolder);
 		} catch (IOException e) {
 			throw new BuildDataComputerException(e);
 		}
 		
 		BuildDataExtractor extractor = computer.compute(computedFolder, new BuildDataComputerTracker() {
 			@Override
 			public void onStreamProcessed() {
 			}
 			
 			@Override
 			public void onFileProcessed(File file) {
 			}
 			
 			@Override
 			public void onFileOutputCreated(File file) {
 				listeners.onFileComputed(computer, file);
 			}
 		});
 		
 		extract(extractor);
 	}
 	
 	public void extract(final BuildDataExtractor extractor) {
 		extractor.extractTo(table, new BuildDataExtractorTracker() {
 			@Override
 			public void onFileProcessed(File file) {
 				listeners.onFileExtracted(extractor, file);
 			}
 			
 			@Override
 			public void onStreamProcessed() {
 				listeners.onStreamExtracted(extractor);
 			}
 		});
 	}
 	
 	public Report generateReportSummary() {
 		if (table.isEmpty())
 			return null;
 		
 		if (analysers.isEmpty())
 			addAnalysersFromServices();
 		
 		List<Report> reports = new ArrayList<Report>();
 		
 		Collections.sort(analysers, new Comparator<BuildHealthAnalyser>() {
 			@Override
 			public int compare(BuildHealthAnalyser o1, BuildHealthAnalyser o2) {
 				return o1.getPriority() - o2.getPriority();
 			}
 		});
 		
 		for (BuildHealthAnalyser analyser : analysers)
 			reports.addAll(analyser.computeSimpleReport(table, preferences));
 		
		if (reports.isEmpty())
			return null;
		
 		BuildStatus status = Report.mergeBuildStatus(reports);
 		
		return new Report(status, "Build", status.name(), null, reports);
 	}
 	
 	// Helper methods to find home
 	
 	public static File findHome(File home, boolean searchCurrentFolder) {
 		if (home != null)
 			return home;
 		
 		File result = searchForHomeFolderInEnvironmentVariables();
 		if (result != null)
 			return result;
 		
 		if (searchCurrentFolder) {
 			result = searchForHomeFolderInCurrentPath(new File("."));
 			if (result != null)
 				return result;
 		}
 		
 		return getDefaultHomeFolder();
 	}
 	
 	public static File searchForHomeFolderInCurrentPath(File currentPath) {
 		currentPath = getCanonicalFile(currentPath);
 		
 		do {
 			File result = new File(currentPath, DEFAULT_FOLDER_NAME);
 			if (result.exists() && result.isDirectory())
 				return result;
 			currentPath = currentPath.getParentFile();
 		} while (currentPath != null);
 		
 		return null;
 	}
 	
 	public static File searchForHomeFolderInEnvironmentVariables() {
 		String home = System.getenv("BUILDHEALTH_HOME");
 		if (Strings.isNullOrEmpty(home))
 			return null;
 		else
 			return new File(home);
 	}
 	
 	public static File getDefaultHomeFolder() {
 		return new File(FileUtils.getUserDirectory(), DEFAULT_FOLDER_NAME);
 	}
 	
 	public static PreferencesStore getDefaultPreferencesStore(File home) {
 		if (home != null)
 			return new LowercaseDiskPreferencesStore(new PropertiesPreferencesStore(getConfigFile(home)));
 		else
 			return new LowercasePreferencesStore(new MemoryPreferencesStore());
 	}
 	
 	private static File getConfigFile(File home) {
 		return getCanonicalFile(new File(home, "config"));
 	}
 	
 	public static BuildData getDefaultBuildData(File home) {
 		if (home != null)
 			return new DiskBuildData(getDataFile(home), new BuildDataTable());
 		else
 			return new BuildDataTable();
 	}
 	
 	private static File getDataFile(File home) {
 		return getCanonicalFile(new File(home, "data.csv"));
 	}
 }
