 /*-
  * Copyright © 2010 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.diamond.scisoft.analysis.io;
 
 import gda.analysis.io.ScanFileHolderException;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.ZipInputStream;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.gda.monitor.IMonitor;
 import uk.ac.gda.util.io.FileUtils;
 
 /**
  * A class which gives a single point of entry to loading data files
  * into the system.
  * 
  * In order to work with the factory a loader must have:
  * 1. a no argument constructor
  * 2. a setFile(...) method with a string path argument
  * 
  * *OR*
  * 
  * A constructor with a string argument.
  * 
  * In order to work well the loader should implement:
  * 
  * 1. IMetaLoader - this interface marks it possible to extract dataset names and other meta
  *    data without reading all the file data into memory.
  *    
  * 2. IDataSetLoader to load a single data set without loading the rest of the file.
  * 
  */
 public class LoaderFactory {
 	private static final Logger logger = LoggerFactory.getLogger(LoaderFactory.class);
 
 	private static final Map<String, List<Class<? extends AbstractFileLoader>>> LOADERS = new HashMap<String, List<Class<? extends AbstractFileLoader>>>(19);
 	private static final Map<String, Class<? extends java.io.InputStream>>     UNZIPERS = new HashMap<String, Class<? extends java.io.InputStream>>(3);
 
 	/**
 	 *      
 	 * It is intended to add a catch all loader which is based on FabioFile
 	 * 
 	 * Loaders can be registered at run time using registerLoader(...)
 	 * 
 	 * There is no need for an extension point now and no dependency on eclipse.
 	 * To change a loader from an external plugin for example:
 	 * 
 	 * 1. LoaderFactory.getSupportedExtensions();
 	 * 2. LoaderFactory.clearLoader("h5");
 	 * 3. LoaderFactory.registerLoader("h5", MyH5ClassThatIsBetter.class);
 	 * 
 	 */
 	static {
 		try {
 		    LoaderFactory.registerLoader("npy",  NumPyFileLoader.class);
 		    LoaderFactory.registerLoader("img",  ADSCImageLoader.class);
 		    LoaderFactory.registerLoader("osc",  RAxisImageLoader.class);
 		    LoaderFactory.registerLoader("cbf",  CBFLoader.class);
 		    LoaderFactory.registerLoader("img",  CrysalisLoader.class);
 			LoaderFactory.registerLoader("tif",  PixiumLoader.class);
 		    LoaderFactory.registerLoader("jpeg", JPEGLoader.class);
 		    LoaderFactory.registerLoader("jpg",  JPEGLoader.class);
 		    LoaderFactory.registerLoader("mccd", MARLoader.class);
 		    
 		    // There is some disagreement about the proper nexus/hdf5 
 		    // file extension at different facilities
 		    LoaderFactory.registerLoader("nxs",  NexusLoader.class);
 		    LoaderFactory.registerLoader("h5",   NexusLoader.class);
 		    LoaderFactory.registerLoader("hdf",  NexusLoader.class);
 		    LoaderFactory.registerLoader("hdf5", NexusLoader.class);
 		    LoaderFactory.registerLoader("hd5",  NexusLoader.class);
 		    LoaderFactory.registerLoader("nexus",NexusLoader.class);
 		    
 		    LoaderFactory.registerLoader("tif",  PilatusTiffLoader.class);
 		    LoaderFactory.registerLoader("png",  PNGLoader.class);
 		    LoaderFactory.registerLoader("raw",  RawBinaryLoader.class);
 		    LoaderFactory.registerLoader("srs",  SRSLoader.class);
 		    LoaderFactory.registerLoader("dat",  DatLoader.class);
 		    LoaderFactory.registerLoader("dat",  SRSLoader.class);
 		    LoaderFactory.registerLoader("tif",  TIFFImageLoader.class);		    
 		    LoaderFactory.registerLoader("tiff", TIFFImageLoader.class);		    
 		    LoaderFactory.registerLoader("zip",  XMapLoader.class);
 		    LoaderFactory.registerLoader("edf",  PilatusEdfLoader.class);
 		    LoaderFactory.registerLoader("pgm",  PgmLoader.class);
 		    
 		    LoaderFactory.registerUnzip("gz",  GZIPInputStream.class);
 		    LoaderFactory.registerUnzip("zip", ZipInputStream.class);
 		    LoaderFactory.registerUnzip("bz2", CBZip2InputStream.class);
 
 		} catch (Exception ne) {
 			logger.error("Cannot register loader - ALL loader registration aborted!", ne);
 		}
 	}
 
 	/**
 	 * This method is used to define the supported extensions that the LoaderFactory
 	 * already knows about.
 	 * 
 	 * NOTE that is can be called from Jython. It is probably not used in the GDA/SDA 
 	 * code based that much but external code like Jython can 
 	 * 
 	 * @return collection of extensions.
 	 */
 	public static Collection<String> getSupportedExtensions() {
 		return LOADERS.keySet();
 	}
 
 	/**
 	 * Call to load any file type into memory. By default loads all data sets, therefore
 	 * could take a **long time**.
 	 * 
 	 * In addition to find out if a given file loads with a particular loader - it actually 
 	 * LOADS it. 
 	 * 
 	 * Therefore it can take a while to run depending on how quickly the loader
 	 * fails. Also if there are many loaders called in turn, much memory could be consumed and
 	 * discarded. For this reason the registration process requires a file extension and tries 
 	 * all the loaders for a given extension if the extension is registered already. 
 	 * Otherwise it tries all loaders - in no particular order.
 	 * 
 	 * @param path
 	 * @return DataHolder
 	 * @throws Exception
 	 */
 	public static DataHolder getData(final String path) throws Exception {
 		return getData(path, true, new IMonitor() {
 			
 			@Override
 			public void worked(int amount) {
 				// Deliberately empty
 			}
 			
 			@Override
 			public boolean isCancelled() {
 				return false;
 			}
 		});
 	}
 	
 	
 	/**
 	 * Call to load any file type into memory. By default loads all data sets, therefore
 	 * could take a **long time**.
 	 * 
 	 * In addition to find out if a given file loads with a particular loader - it actually 
 	 * LOADS it. 
 	 * 
 	 * Therefore it can take a while to run depending on how quickly the loader
 	 * fails. Also if there are many loaders called in turn, much memory could be consumed and
 	 * discarded. For this reason the registration process requires a file extension and tries 
 	 * all the loaders for a given extension if the extension is registered already. 
 	 * Otherwise it tries all loaders - in no particular order.
 	 * 
 	 * @param path
 	 * @param mon
 	 * @return DataHolder
 	 * @throws Exception
 	 */
 	public static DataHolder getData(final String path, final IMonitor mon) throws Exception {
 		return getData(path, true, mon);
 	}
 
 	/**
 	 * Call to load any file type into memory. By default loads all data sets, therefore
 	 * could take a **long time**.
 	 * 
 	 * In addition to find out if a given file loads with a particular loader - it actually 
 	 * LOADS it. 
 	 * 
 	 * Therefore it can take a while to run depending on how quickly the loader
 	 * fails. Also if there are many loaders called in turn, much memory could be consumed and
 	 * discarded. For this reason the registration process requires a file extension and tries 
 	 * all the loaders for a given extension if the extension is registered already. 
 	 * Otherwise it tries all loaders - in no particular order.
 	 * 
 	 * @param path to file
 	 * @param willLoadMetadata dictates whether metadata is not loaded (if possible)
 	 * @param mon
 	 * @return DataHolder
 	 * @throws Exception
 	 */
 	public static DataHolder getData(final String path, final boolean willLoadMetadata, final IMonitor mon) throws Exception {
 
 		if (!(new File(path)).exists())
 			throw new FileNotFoundException(path);
 
 		final Iterator<Class<? extends AbstractFileLoader>> it = getIterator(path);
 
 		// Currently this method simply cycles through all loaders.
 		// When it finds one which does not give an exception on loading it
 		// returns the data from this loader.
 		while (it.hasNext()) {
 			final Class<? extends AbstractFileLoader> clazz = it.next();
 			final AbstractFileLoader loader = LoaderFactory.getLoader(clazz, path);
 			loader.setLoadMetadata(willLoadMetadata);
 			try {
 				// NOTE Assumes loader fails quickly and nicely
 				// if given the wrong file. If a loader does not
 				// do this it should not be registered with LoaderFactory
 				DataHolder holder = loader.loadFile(mon);
 				return holder;
 			} catch (OutOfMemoryError ome) {
 				logger.error("There was not enough memory to load " + path);
 				throw new ScanFileHolderException("Out of memory in loader factory", ome);
 			} catch (Throwable ne) {
 				logger.trace("Loader error " + loader, ne);
 				continue;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Call to load any file type into memory. If a loader implements IMetaLoader will
 	 * use this fast method to avoid loading the entire file into memory. If the loader
 	 * does not implement IMetaLoader it will return null. Then you should use getData(...) 
 	 * to load the entire file.
 	 * 
 	 * 
 	 * @param path
 	 * @param mon
 	 * @return IMetaData
 	 * @throws Exception
 	 */
 	public static IMetaData getMetaData(final String path, final IMonitor mon) throws Exception {
 
 		final Iterator<Class<? extends AbstractFileLoader>> it = getIterator(path);
 
 		// Currently this method simply cycles through all loaders.
 		// When it finds one which does not give an exception on loading, it
 		// returns the data from this loader.
 		while (it.hasNext()) {
 			final Class<? extends AbstractFileLoader> clazz = it.next();
 			final AbstractFileLoader loader = LoaderFactory.getLoader(clazz, path);
 			if (!IMetaLoader.class.isInstance(loader))
 				continue;
 
 			try {
 				// NOTE Assumes loader fails quickly and nicely
 				// if given the wrong file. If a loader does not
 				// do this, it should not be registered with LoaderFactory
 				((IMetaLoader) loader).loadMetaData(mon);
 				IMetaData meta = ((IMetaLoader) loader).getMetaData();
 				return meta;
 			} catch (Throwable ne) {
 				logger.trace("Cannot load nexus meta data", ne);
 				continue;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Loads a single data set where the loader allows loading of only one data set.
 	 * Otherwise returns null.
 	 * 
 	 * @param path
 	 * @param mon
 	 * @return IDataset
 	 * @throws Exception
 	 */
 	public static AbstractDataset getDataSet(final String path, final String name, final IMonitor mon) throws Exception {
 
 		final Iterator<Class<? extends AbstractFileLoader>> it = getIterator(path);
 
 		// Currently this method simply cycles through all loaders.
 		// When it finds one which does not give an exception on loading it
 		// returns the data from this loader.
 		while (it.hasNext()) {
 			final Class<? extends AbstractFileLoader> clazz = it.next();
 			final AbstractFileLoader loader = LoaderFactory.getLoader(clazz, path);
 
 			try {
 				// NOTE Assumes loader fails quickly and nicely
 				// if given the wrong file. If a loader does not
 				// do this, it should not be registered with LoaderFactory
 				final AbstractDataset set = ((IDataSetLoader) loader).loadSet(path, name, mon);
 				return set;
 			} catch (Throwable ne) {
 				continue;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Loads a single data set where the loader allows loading of only one data set.
 	 * Otherwise returns null.
 	 * 
 	 * @param path
 	 * @param mon
 	 * @return IDataset
 	 * @throws Exception
 	 */
 	public static Map<String,ILazyDataset> getDataSets(final String path, final List<String> names, final IMonitor mon) throws Exception {
 
 		final Iterator<Class<? extends AbstractFileLoader>> it = getIterator(path);
 
 		// Currently this method simply cycles through all loaders.
 		// When it finds one which does not give an exception on loading it
 		// returns the data from this loader.
 		while (it.hasNext()) {
 			final Class<? extends AbstractFileLoader> clazz = it.next();
 			final AbstractFileLoader loader = LoaderFactory.getLoader(clazz, path);
 
 			try {
 				// NOTE Assumes loader fails quickly and nicely
 				// if given the wrong file. If a loader does not
 				// do this, it should not be registered with LoaderFactory
 				return ((IDataSetLoader) loader).loadSets(path, names, mon);
 			} catch (Throwable ne) {
 				continue;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Gets a slice right out of the file, probably only nexus supports this
 	 * for now.
 	 * <p>
 	 * If the slice cannot be loaded, null is returned.
 	 * 
 	 * @param object
 	 * @param mon
 	 * @return AbstractDataset - slice
 	 */
 	public static AbstractDataset getSlice(final SliceObject object, final IMonitor mon) throws Exception {
 
 		final Iterator<Class<? extends AbstractFileLoader>> it = getIterator(object.getPath());
 
 		// Currently this method simply cycles through all loaders.
 		// When it finds one which does not give an exception on loading, it
 		// returns the data from this loader.
 		while (it.hasNext()) {
 			final Class<? extends AbstractFileLoader> clazz = it.next();
 			final AbstractFileLoader loader = LoaderFactory.getLoader(clazz, object.getPath());
 			if (!ISliceLoader.class.isInstance(loader))
 				continue;
 
 			try {
 				// NOTE Assumes loader fails quickly and nicely
 				// if given the wrong file. If a loader does not
 				// do this, it should not be registered with LoaderFactory
 				final AbstractDataset set = ((ISliceLoader) loader).slice(object, mon);
 				return set;
 			} catch (Exception ne) {
 				throw ne;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Returns true if a given file is an IMetaData and able to load metadata without the data
 	 * 
 	 * @param path
 	 * @return true if can load metadata without all data being loaded.
 	 */
 	public boolean isMetaLoader(final String path) throws Exception {
 
 		return isInstanceSupported(path, IMetaLoader.class);
 	}
 
 	/**
 	 * Returns true if a given file is an IDataSetLoader and able to load data without the metadata
 	 * 
 	 * @param path
 	 * @return true if can load individual data sets.
 	 */
 	public boolean isDataSetLoader(final String path) throws Exception {
 
 		return isInstanceSupported(path, IDataSetLoader.class);
 	}
 
 	private boolean isInstanceSupported(String path, Class<?> interfaceClass) throws Exception {
 
 		final String extension = FileUtils.getFileExtension(path).toLowerCase();
 
 		if (LOADERS.containsKey(extension)) {
 			final Collection<Class<? extends AbstractFileLoader>> loaders = LOADERS.get(extension);
 
 			for (Class<? extends AbstractFileLoader> clazz : loaders) {
 				final AbstractFileLoader loader = LoaderFactory.getLoader(clazz, path);
 				if (interfaceClass.isInstance(loader))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Gets an AbstractFileLoader for the given class and file path.
 	 * 
 	 * @param clazz
 	 * @param path
 	 * @return AbstractFileLoader
 	 * @throws Exception
 	 */
 	public static AbstractFileLoader getLoader(Class<? extends AbstractFileLoader> clazz, final String path)
 			throws Exception {
 
 		AbstractFileLoader loader;
 		try {
 			final Constructor<?> singleString = clazz.getConstructor(String.class);
 			loader = (AbstractFileLoader) singleString.newInstance(path);
 		} catch (NoSuchMethodException e) {
 			loader = clazz.newInstance();
 
 			final Method setFile = loader.getClass().getMethod("setFile", String.class);
 			setFile.invoke(loader, path);
 		}
 
 		return loader;
 	}
 
 	/**
 	 * Get class that can load files of given extension
 	 * 
 	 * @param extension
 	 * @return loader class
 	 */
 	public static Class<? extends AbstractFileLoader> getLoaderClass(String extension) {
 		return LOADERS.get(extension).get(0);
 	}
 
 	private static Iterator<Class<? extends AbstractFileLoader>> getIterator(String path) throws IllegalAccessException {
 
 		if ((new File(path).isDirectory()))
 			throw new IllegalAccessException("Cannot load directories with LoaderFactory!");
 
 		final String extension = FileUtils.getFileExtension(path).toLowerCase();
 		Iterator<Class<? extends AbstractFileLoader>> it = null;
 
 		if (LOADERS.containsKey(extension)) {
 			it = LOADERS.get(extension).iterator();
 		} else {
 			// We may have a zipped file type that we support
 			final File file = new File(path);
 			final String regEx = ".+\\." + getLoaderExpression() + "\\." + getZipExpression();
 
 			final Matcher m = Pattern.compile(regEx).matcher(file.getName());
 			if (m.matches()) {
 				final String realExt = m.group(1);
 				if (LoaderFactory.LOADERS.keySet().contains(realExt)) {
 					final Collection<Class<? extends AbstractFileLoader>> ret = new ArrayList<Class<? extends AbstractFileLoader>>(
 							1);
 					ret.add(CompressedLoader.class);
 					return ret.iterator();
 				}
 			}
 
 			if (!searchingAllowed)
 				return null;
 
 			final Set<Class<? extends AbstractFileLoader>> all = new HashSet<Class<? extends AbstractFileLoader>>();
 			for (String ext : LOADERS.keySet())
 				all.addAll(LOADERS.get(ext));
 			it = all.iterator();
 		}
 		return it;
 	}
 
 	public static void registerUnzip(final String extension, final Class<? extends InputStream> input) {
 		UNZIPERS.put(extension, input);
 	}
 
 	/**
 	 * Could cache this but it will be fast
 	 */
 	protected static String getZipExpression() {
 		return getExpression(UNZIPERS.keySet().iterator());
 	}
 
 	/**
 	 * Could cache this but it will be fast
 	 */
 	protected static String getLoaderExpression() {
 		return getExpression(LOADERS.keySet().iterator());
 	}
 
 	/**
 	 * Could cache this but it will be fast
 	 */
 	private static String getExpression(final Iterator<String> it) {
 		final StringBuilder buf = new StringBuilder();
 		buf.append("(");
 		while (it.hasNext()) {
 
 			buf.append(it.next());
 			if (it.hasNext())
 				buf.append("|");
 		}
 		buf.append(")");
 		return buf.toString();
 	}
 
 	/**
 	 * Throws an exception if the loader is not ready to be used with LoaderFactory.
 	 * Otherwise adds the class to the list of loaders.
 	 * 
 	 * NOTE that duplicates are allowed and the LoaderFactory simply tries loaders until
 	 * one works. If loaders do not fail fast on invalid files then this approach does not work.
 	 * 
 	 * This has been tested by adding a test for each file type using the loader factory. This
 	 * coverage could be extended by adding more example files and attempting to load them
 	 * with the factory. However as long as each file type is passed through LoaderFactory and
 	 * checks are made in the test to ensure that the loader is working, there is a good chance
 	 * that it will find the right loader.
 	 * 
 	 * @param extension - lower case string
 	 * @param loader
 	 * @throws Exception
 	 */
 	public static void registerLoader(final String extension, final Class<? extends AbstractFileLoader> loader)
 			throws Exception {
 
 		try {
 			loader.getConstructor(String.class);
 		} catch (NoSuchMethodException e) {
 			// TODO These messages are not quite correct
 			if (loader.getMethod("setFile", String.class) == null)
 				throw new Exception("Loaders must have method setFile(String path)");
 			if (loader.getConstructor() == null)
 				throw new Exception("Loaders must have a no argument constructor!");
 		}
 
 		List<Class<? extends AbstractFileLoader>> list = LOADERS.get(extension);
 		if (list == null) {
 			list = new ArrayList<Class<? extends AbstractFileLoader>>();
 			LOADERS.put(extension, list);
 		}
 
 		// Since not using set of loaders anymore must use contains to ensure
 		// that a memory leak does not occur.
 		if (!list.contains(loader))
 			list.add(loader);
 	}
 
 	/**
 	 * Call to clear all the loaders registered for a given extension.
 	 * 
 	 * @param extension
 	 * @return the old loader list, now removed, if any.
 	 */
 	public static List<Class<? extends AbstractFileLoader>> clearLoader(final String extension) {
 		return LOADERS.remove(extension);
 	}
 
 	private static boolean searchingAllowed = false;
 
 	public static void setLoaderSearching(final boolean sa) {
 		searchingAllowed = sa;
 	}
 
 	protected static Class<? extends java.io.InputStream> getZipStream(final String extension) {
 		return UNZIPERS.get(extension);
 	}
 }
