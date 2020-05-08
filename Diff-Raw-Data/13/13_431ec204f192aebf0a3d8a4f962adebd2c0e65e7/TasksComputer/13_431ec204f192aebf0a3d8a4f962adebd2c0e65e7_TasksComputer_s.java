 package org.pescuma.buildhealth.computer.staticanalysis;
 
 import static com.google.common.base.Objects.*;
 import static org.apache.commons.io.IOUtils.*;
 import static org.pescuma.buildhealth.utils.FilenameToLanguage.*;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.LineIterator;
 import org.pescuma.buildhealth.computer.BuildDataComputer;
 import org.pescuma.buildhealth.computer.BuildDataComputerException;
 import org.pescuma.buildhealth.computer.BuildDataComputerTracker;
 import org.pescuma.buildhealth.extractor.BuildDataExtractor;
 import org.pescuma.buildhealth.extractor.BuildDataExtractorException;
 import org.pescuma.buildhealth.extractor.CSVExtractor;
 import org.pescuma.buildhealth.extractor.PseudoFiles;
 
 import au.com.bytecode.opencsv.CSVParser;
 import au.com.bytecode.opencsv.CSVWriter;
 
 // Based on https://github.com/jenkinsci/tasks-plugin/blob/master/src/main/java/hudson/plugins/tasks/parser/TaskScanner.java by Ulli Hafner
 public class TasksComputer implements BuildDataComputer {
 	
 	private final PseudoFiles files;
 	private final Pattern[] patterns;
 	private final String[] markers;
 	
 	public TasksComputer(PseudoFiles files) {
 		this(files, false);
 	}
 	
 	public TasksComputer(PseudoFiles files, boolean caseInsensitive, String... markers) {
 		if (files == null)
 			throw new IllegalArgumentException();
 		
 		if (markers == null || markers.length < 1)
 			markers = new String[] { "TODO", "FIXME", "HACK", "XXX" };
 		
 		int flags = (caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
 		
 		this.markers = markers;
 		this.patterns = new Pattern[markers.length];
 		for (int i = 0; i < markers.length; i++) {
 			String marker = markers[i];
 			
 			if (marker.isEmpty())
 				throw new IllegalArgumentException("Pattern can't be empty");
 			
 			if (Character.isLetterOrDigit(marker.charAt(0)))
 				marker = "\\b" + marker;
 			if (Character.isLetterOrDigit(marker.charAt(marker.length() - 1)))
 				marker += "\\b";
 			
 			this.patterns[i] = Pattern.compile(marker, flags);
 		}
 		
 		this.files = files;
 	}
 	
 	@Override
 	public BuildDataExtractor compute(File folder, BuildDataComputerTracker tracker) {
		File outputFile = new File(folder, "tasks.csv");
 		
 		FileWriter writer = null;
 		CSVWriter out = null;
 		try {
 			
 			writer = new FileWriter(outputFile);
 			out = new CSVWriter(writer, CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER,
 					CSVParser.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
 			
 			extractTo(out, tracker);
 			
 			tracker.onFileOutputCreated(outputFile);
 			
 			return new CSVExtractor(new PseudoFiles(outputFile));
 			
 		} catch (IOException e) {
 			throw new BuildDataComputerException(e);
 			
 		} finally {
 			closeQuietly(out);
 			closeQuietly(writer);
 		}
 	}
 	
 	private void extractTo(CSVWriter out, BuildDataComputerTracker tracker) {
 		try {
 			if (files.isStream()) {
 				extractStream(firstNonNull(files.getStreamFilename(), "<stream>"),
 						new InputStreamReader(files.getStream()), out);
 				tracker.onStreamProcessed();
 				
 			} else {
 				for (File file : files.getFiles()) {
 					if (!isKnownFileType(file.getPath()))
 						continue;
 					
 					FileReader reader = new FileReader(file);
 					try {
 						
 						extractStream(file.getPath(), reader, out);
 						tracker.onFileProcessed(file);
 						
 					} finally {
 						IOUtils.closeQuietly(reader);
 					}
 				}
 			}
 		} catch (IOException e) {
 			throw new BuildDataExtractorException(e);
 		}
 	}
 	
 	private void extractStream(String filename, Reader reader, CSVWriter out) throws IOException {
 		LineIterator it = IOUtils.lineIterator(reader);
 		int lineNum = 0;
 		while (it.hasNext()) {
 			String line = it.next();
 			lineNum++;
 			
 			if (line.isEmpty())
 				continue;
 			
 			for (int i = 0; i < patterns.length; i++) {
 				Matcher m = patterns[i].matcher(line);
 				if (!m.find())
 					continue;
 				
 				String marker = markers[i];
 				String text = "";
 				
 				int end = m.end();
 				if (end < line.length()) {
 					text = line.substring(end).trim();
 					
 					if (text.startsWith(":"))
 						text = text.substring(1).trim();
 				}
 				
 				write(out, Double.toString(1), "Static analysis", detectLanguage(filename), "Tasks", filename,
 						Integer.toString(lineNum), marker, text);
 			}
 		}
 	}
 	
 	private void write(CSVWriter out, String... line) {
 		out.writeNext(line);
 	}
 }
