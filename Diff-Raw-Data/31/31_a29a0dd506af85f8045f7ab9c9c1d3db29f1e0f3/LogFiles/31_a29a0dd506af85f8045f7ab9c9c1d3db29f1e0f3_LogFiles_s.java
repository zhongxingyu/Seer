 package org.logparser.io;
 
 import static org.logparser.Constants.FILE_SEPARATOR;
 
 import java.io.File;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import net.jcip.annotations.Immutable;
 
 import org.apache.log4j.Logger;
 
 import com.google.common.base.CharMatcher;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Strings;
 
 /**
  * Represents one or more log files on the file system, to be collected for
  * processing.
  * 
  * @author jorge.decastro
  * 
  */
 @Immutable
 public final class LogFiles {
 	private static final Logger LOGGER = Logger.getLogger(LogFiles.class);
 	public static final String DEFAULT_TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss";
 	public static final String DEFAULT_FILENAME_PATTERN = ".*.log$";
 	public static final String DEFAULT_INPUT_DIR = ".";
 
 	private final Pattern filenamePattern;
 	private final String[] inputDirs;
 	private final String outputDir;
 	private final IPreProcessor preProcessor;
 
 	private LogFiles(final Builder builder) {
 		filenamePattern = builder.filenamePattern;
 		inputDirs = builder.inputDirs;
 		outputDir = builder.outputDir;
 		preProcessor = builder.preProcessor;
 	}
 
 	public Pattern getFilenamePattern() {
 		return filenamePattern;
 	}
 
 	public String[] getInputDirs() {
 		return inputDirs;
 	}
 
 	public String getOutputDir() {
 		return outputDir;
 	}
 
 	public IPreProcessor getPreProcessor() {
 		return preProcessor;
 	}
 
 	public File[] list() {
 		return list(inputDirs, filenamePattern);
 	}
 
 	private File[] list(final String[] inputDirs, final Pattern filenamePattern) {
 		List<File> listOfFiles = new ArrayList<File>();
 		for (String path : inputDirs) {
 			File f = new File(path.trim());
 			if (!f.exists()) {
 				throw new IllegalArgumentException(String.format("Unable to find path to log file '%s'", path));
 			}
 			File[] contents = f.listFiles();
 			for (File file : contents) {
 				if (filenamePattern.matcher(file.getName()).matches()) {
 					LOGGER.info(String.format("Log file to parse '%s'", file.getAbsolutePath()));
 					listOfFiles.add(file);
 				}
 			}
 		}
 		LOGGER.info(String.format("Extracted log files matching pattern '%s' from input dir(s) '%s'", filenamePattern.pattern(), Arrays.toString(inputDirs)));
 		listOfFiles = preProcessor.apply(listOfFiles);
 		File[] files = listOfFiles.toArray(new File[0]);
 		LOGGER.info(String.format("Pre-processing applied; returning log files '%s'", Arrays.toString(files)));
 		return files;
 	}
 
 	public static class Builder {
 		// required parameters
 		// optional parameters
 		private Pattern filenamePattern = Pattern.compile(DEFAULT_FILENAME_PATTERN);
 		private String[] inputDirs = new String[] { DEFAULT_INPUT_DIR };
 		private IPreProcessor preProcessor = new IdentityPreProcessor();
 		private DateFormat dateFormat = new SimpleDateFormat(DEFAULT_TIMESTAMP_PATTERN);
 		private Date today = new Date();
		private String outputDir = "";
 
 		public Builder() {
 		}
 
 		public Builder filenamePattern(final String filenamePattern) {
 			if (Strings.isNullOrEmpty(filenamePattern)) {
 				throw new IllegalArgumentException("'filenamePattern' argument cannot be null.");
 			}
 			this.filenamePattern = Pattern.compile(filenamePattern);
 			return this;
 		}
 
 		public Builder inputDirs(final String[] inputDirs) {
 			this.inputDirs = Preconditions.checkNotNull(inputDirs, "'inputDirs' argument cannot be null.");
 			return this;
 		}
 
 		public Builder outputDir(final String outputDir) {
 			this.outputDir = Preconditions.checkNotNull(outputDir, "'outputDir' argument cannot be null.");
 			return this;
 		}
 
 		public Builder preProcessor(final IPreProcessor preProcessor) {
 			this.preProcessor = Preconditions.checkNotNull(preProcessor, "'preProcessor' argument cannot be null.");
 			return this;
 		}
 
 		public LogFiles build() {
 			sanitizeOutputDir();
 			return new LogFiles(this);
 		}
 
 		private void sanitizeOutputDir() {
 			outputDir = CharMatcher.anyOf("/\\").replaceFrom(outputDir, FILE_SEPARATOR);
			outputDir = CharMatcher.anyOf("<>:;\"|?*,~'[]{}=&^%$@!`").replaceFrom(outputDir, "_");
 			// allow distinct output folders
 			outputDir = String.format("%s%s%s", outputDir, FILE_SEPARATOR, dateFormat.format(today));
 		}
 	}
 }
