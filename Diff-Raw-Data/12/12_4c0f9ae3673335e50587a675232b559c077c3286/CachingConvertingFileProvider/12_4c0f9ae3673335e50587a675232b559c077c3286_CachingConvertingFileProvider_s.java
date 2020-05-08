 package de.uniluebeck.itm.tr.util;
 
 import com.google.common.base.Function;
 import com.google.inject.Provider;
 
 import java.io.File;
 
 public class CachingConvertingFileProvider<T> implements Provider<T> {
 
 	private final Function<File, T> conversionFunction;
 
	private final File file;
 
 	private transient long lastFileTimestamp = Long.MIN_VALUE;
 
 	private T lastFileContents;
 
 	public CachingConvertingFileProvider(final File file, final Function<File, T> conversionFunction) {
 		this.file = file;
 		this.conversionFunction = conversionFunction;
 	}
 
 	@Override
	public T get() {
 
 		if (!file.exists()) {
			throw new RuntimeException("File " + file.getAbsolutePath() + " does not exist!");
 		}
 
 		if (!file.canRead()) {
 			throw new RuntimeException("File " + file.getAbsolutePath() + " can't be read!");
 		}
 
 		long fileLastModified = file.lastModified();
 
 		if (lastFileTimestamp == Long.MIN_VALUE || fileLastModified > lastFileTimestamp) {
 			lastFileTimestamp = fileLastModified;
 			lastFileContents = conversionFunction.apply(file);
 		}
 
 		return lastFileContents;
 	}
 }
