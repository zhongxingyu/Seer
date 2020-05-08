 package org.logparser.provider;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 
 import org.apache.commons.lang.builder.ReflectionToStringBuilder;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.annotate.JsonCreator;
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.logparser.io.CommandLineArguments;
 import org.logparser.io.IPreProcessor;
 import org.logparser.io.LogFiles;
 
 import com.google.common.base.Strings;
 
 /**
  * Responsible for providing bespoke instances of {@link LogFiles}.
  * 
  * @author jorge.decastro
  * 
  */
 public final class LogFilesProvider {
 	private static final Logger LOGGER = Logger.getLogger(LogFilesProvider.class);
 	private String filenamePattern;
 	private String[] inputDirs;
 	private String outputDir;
 	private final PreProcessorProvider preProcessorProvider;
 
 	@JsonCreator
 	public LogFilesProvider(
 			@JsonProperty("filenamePattern") final String filenamePattern,
 			@JsonProperty("inputDirs") final String[] inputDirs,
 			@JsonProperty("outputDir") final String outputDir,
 			@JsonProperty("preprocessor") final PreProcessorProvider preProcessorProvider) {
 
 		this.filenamePattern = filenamePattern;
 		this.inputDirs = inputDirs;
 		this.outputDir = outputDir;
 		this.preProcessorProvider = preProcessorProvider;
 	}
 
 	public String getFilenamePattern() {
 		return filenamePattern;
 	}
 
 	public String[] getInputDirs() {
 		return inputDirs;
 	}
 
 	public String getOutputDir() {
 		return outputDir;
 	}
 
 	public PreProcessorProvider getPreProcessorProvider() {
 		return preProcessorProvider;
 	}
 
 	@Override
 	public String toString() {
 		return ReflectionToStringBuilder.toString(this);
 	}
 
 	public void applyCommandLineOverrides(final CommandLineArguments cla) {
 		if (!Strings.isNullOrEmpty(cla.inputDir)) {
 			inputDirs = new String[] { cla.inputDir };
 		}
 		if (cla.outputDir != null) {
 			outputDir = cla.outputDir;
 		}
 	}
 
 	public static class PreProcessorProvider {
 		private final String type;
 		private final String filenamePattern;
 
 		@JsonCreator
		public PreProcessorProvider(@JsonProperty("type") final String type, @JsonProperty("filenamePattern") final String filenamePattern) {
 
 			this.type = type;
 			this.filenamePattern = filenamePattern;
 		}
 
 		public String getType() {
 			return type;
 		}
 
 		public String getFilenamePattern() {
 			return filenamePattern;
 		}
 
 		@Override
 		public String toString() {
 			return ReflectionToStringBuilder.toString(this);
 		}
 	}
 
 	public LogFiles build() {
 		LogFiles.Builder logFilesBuilder = new LogFiles.Builder();
 		if (!Strings.isNullOrEmpty(filenamePattern)) {
 			logFilesBuilder.filenamePattern(filenamePattern);
 		}
 		if (inputDirs != null) {
 			logFilesBuilder.inputDirs(inputDirs);
 		}
 		if (outputDir != null) {
 			logFilesBuilder.outputDir(outputDir);
 		}
 		if (preProcessorProvider != null) {
 			String type = preProcessorProvider.type;
 			if (Strings.isNullOrEmpty(type)) {
 				throw new IllegalArgumentException("'type' argument of pre-processor is required.");
 			}
 			String filenamePattern = preProcessorProvider.filenamePattern;
 			if (Strings.isNullOrEmpty(filenamePattern)) {
 				throw new IllegalArgumentException("'filenamePattern' argument of pre-processor is required.");
 			}
 
 			try {
 				@SuppressWarnings("unchecked")
 				Class<IPreProcessor> clazz = (Class<IPreProcessor>) Class.forName(type);
 				@SuppressWarnings("rawtypes")
 				Class[] argsClass = new Class[] { String.class, String.class };
 				Object[] argsObjs = new Object[] { filenamePattern, outputDir };
 				if (clazz != null) {
 					Constructor<IPreProcessor> constructor;
 					constructor = clazz.getConstructor(argsClass);
 					IPreProcessor preprocessor = constructor.newInstance(argsObjs);
 					logFilesBuilder.preProcessor(preprocessor);
 					LOGGER.info(String.format("Successfully constructed pre-processor '%s'", type));
 				}
 			} catch (Throwable t) {
 				throw new IllegalArgumentException(String.format("Unable to instantiate pre-processor of type '%s'", type), t);
 			}
 		}
 		LogFiles logFiles = logFilesBuilder.build();
 		makeOutputDir(logFiles.getOutputDir());
 		return logFiles;
 	}
 
 	private void makeOutputDir(final String outputDir) {
 		File f = new File(outputDir);
 		boolean createdDirSuccessfully = false;
 		if (!(f.exists() && f.isDirectory())) {
 			createdDirSuccessfully = f.mkdirs();
 			if (!createdDirSuccessfully) {
 				throw new IllegalArgumentException(String.format("Unable to make 'outputDir' dir at '%s'.", f.getAbsolutePath()));
 			}
 		}
 	}
 }
