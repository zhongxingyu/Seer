 /*-
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.crystallography;
 
 import java.beans.XMLDecoder;
 import java.beans.XMLEncoder;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.jscience.physics.amount.Amount;
 import org.osgi.framework.Version;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 import com.thoughtworks.xstream.core.util.CompositeClassLoader;
 
 /**
  * CalibrationFactory when we go to e4 like all xxxFactory classes this will become
  * ICalibrationService and be injected.
  */
 public class CalibrationFactory {
 
 	private static Logger logger = LoggerFactory.getLogger(CalibrationFactory.class);
 	
 	/**
 	 * Reads the calibration standards from disk, creating a new one if required.
 	 * 
 	 * Only if save is called will they be persisted for future use.
 	 * 
 	 * @return CalibrationStandards
 	 */
 	public static CalibrationStandards getCalibrationStandards() {
 		return getCalibrationStandards(false);
 	}
 	
 	private static CalibrationStandards staticInstance;
 	/**
 	 * 
 	 * @param createNew
 	 * @return cs
 	 */
 	public static CalibrationStandards getCalibrationStandards(boolean createNew) {
 		if (createNew) {
 			return createCalibrationStandards();
 		}
 
 		if (staticInstance==null) {
 			staticInstance = createCalibrationStandards();
 			staticInstance.setUnmodifiable(true);
 		}
 		return staticInstance;
 	}
 
 
 	/**
 	 * Call to save CalibrationStandards to disk
 	 * @param cs
 	 * @throws Exception
 	 */
 	static void saveCalibrationStandards(CalibrationStandards cs) throws Exception {
 		if (cs == null) return;
 		XMLEncoder encoder=null;
 		try {
 			final File calFile = getCalibrantFile();
 			calFile.getParentFile().mkdirs();
 			calFile.createNewFile();
 			encoder = new XMLEncoder(new FileOutputStream(getCalibrantFile()));
 			encoder.writeObject(cs);
 			
 		} finally  {
 			if (encoder!=null) encoder.close();
 		}
 		
 		CalibrationStandards old = staticInstance;
 		staticInstance = cs;
 		if (old!=null && old.getSelectedCalibrant()!=null) {
 		    fireCalibrantSelectionListeners(cs, cs.getSelectedCalibrant());
 		}
 
 	}
 	
 	static CalibrationStandards readCalibrationStandards() throws Exception {
 		XMLDecoder decoder=null;
 		final ClassLoader originalLoader=Thread.currentThread().getContextClassLoader();
 		try {
 		    final CompositeClassLoader customLoader = new CompositeClassLoader();
		    customLoader.add( uk.ac.diamond.scisoft.analysis.dataset.Activator.class.getClassLoader());
		    customLoader.add( uk.ac.diamond.scisoft.analysis.Activator.class.getClassLoader());
 		    customLoader.add(Amount.class.getClassLoader());
 			
 			AccessController.doPrivileged(new PrivilegedAction<Object>() {
 				@Override
 				public Object run() {
 					Thread.currentThread().setContextClassLoader(customLoader);
 					return null;
 				}
 			});
 
 			decoder = new XMLDecoder(new FileInputStream(getCalibrantFile()));
 			return (CalibrationStandards)decoder.readObject();
 			
 		} finally  {
 			Thread.currentThread().setContextClassLoader(originalLoader);
 			
 			if (decoder!=null) decoder.close();
 		}
 	}
 
 	/**
 	 * TODO Best place to keep it? Seems to work when tested.
 	 * @return file
 	 */
 	private static File getCalibrantFile() {
 		File dir = new File(new File(System.getProperty("user.home")), ".dawn");
 		try {
 			dir.mkdirs();
 		} catch (Throwable ne) {
 			try {
 				logger.error("Cannot store calibration standards!", ne);
 				return File.createTempFile("CalibrationStandards", "xml");
 			} catch (Throwable neOther) {
 				logger.error("Cannot create files! Dawn will not function correctly. Please contact you support representative.", neOther);
 			    return null;
 			}
 		}
 		return new File(dir, "CalibrationStandards.xml");
 	}
 
 	private static CalibrationStandards createCalibrationStandards() {
 		final File file = getCalibrantFile();
 		CalibrationStandards cs = null;
 		if (file.exists()) {
 			try {
 				cs = readCalibrationStandards();
 			} catch (Exception e) {
 				cs = null;
 			}
 		}
 		if (cs==null || cs.isEmpty() || isOldVersion(CalibrationStandards.CURRENT_VERSION, cs.getVersion())) {
 			cs = new CalibrationStandards();
 			cs.setVersion(CalibrationStandards.CURRENT_VERSION); // Versions are so that we can wipe out configurations with new Dawn versions if we have to
 			                        // TODO consider new file for this instead.
 			cs.setCal2peaks(CalibrationStandards.createDefaultCalibrants());
 			cs.setSelectedCalibrant("Silicon", false);
 			try {
 				saveCalibrationStandards(cs);
 			} catch (Exception e) {
 				logger.error("Cannot save calibration standards!", e);
 			}
 			return cs;
 		}
 		
 		return cs;
 	}
 	
 	private static boolean isOldVersion(String versionCompare, String versionWith) {
 		Version compare = new Version(versionCompare);
 		Version with    = new Version(versionWith);
 		int c    =  compare.compareTo(with);
 		return c > 0;
 	}
 
 	private static Collection<CalibrantSelectedListener> listeners;
 	private static boolean processingListeners = false;
 	/**
 	 * 
 	 * @param calibrant
 	 */
 	static void fireCalibrantSelectionListeners(CalibrationStandards standards, String calibrant) {
 		if (listeners==null)     return;
 		if (processingListeners) return;
 		
 		try {
 			processingListeners = true;
 			final CalibrantSelectionEvent evt = new CalibrantSelectionEvent(standards, calibrant);
 			for (CalibrantSelectedListener l : listeners) {
 				try {
 				    l.calibrantSelectionChanged(evt);
 				} catch (Throwable ne) {
 					logger.error("Cannot fire calibrant selection changed!", ne);
 				}
 			}
 		} finally {
 			processingListeners = false;
 		}
 	}
 
 	public static void addCalibrantSelectionListener(CalibrantSelectedListener l) {
 		if (listeners==null) listeners = new HashSet<CalibrantSelectedListener>(7);
 		listeners.add(l);
 	}
 	public static void removeCalibrantSelectionListener(CalibrantSelectedListener l) {
 		if (listeners==null) return;
 		listeners.remove(l);
 	}
 
 
 }
