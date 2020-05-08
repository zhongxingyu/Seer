 package org.pescuma.buildhealth.computer.loc;
 
 import static org.apache.commons.io.IOUtils.*;
 import static org.pescuma.buildhealth.utils.FileHelper.*;
 import static org.pescuma.buildhealth.utils.FilenameToLanguage.*;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
import java.util.Random;
 import java.util.zip.ZipInputStream;
 
 import org.pescuma.buildhealth.computer.BuildDataComputer;
 import org.pescuma.buildhealth.computer.BuildDataComputerException;
 import org.pescuma.buildhealth.computer.BuildDataComputerTracker;
 import org.pescuma.buildhealth.extractor.BuildDataExtractor;
 import org.pescuma.buildhealth.extractor.PseudoFiles;
 import org.pescuma.buildhealth.extractor.loc.CLOCExtractor;
 
 public class LOCComputer implements BuildDataComputer {
 	
 	private final PseudoFiles files;
 	
 	public LOCComputer(PseudoFiles files) {
 		this.files = files;
 	}
 	
 	@Override
 	public BuildDataExtractor compute(File folder, BuildDataComputerTracker tracker) {
 		if (files.isStream())
 			throw new UnsupportedOperationException();
 		
 		File cloc = null;
 		File fileList = null;
 		File out = null;
 		boolean success = false;
 		try {
 			
 			cloc = extractCLOCToTmp();
 			fileList = createFileList();
			out = new File(folder, "cloc-" + new Random().nextInt() + ".csv");
 			
 			run("perl", toPath(cloc), "--by-file", "--csv", "--list-file=" + toPath(fileList), "--out=" + toPath(out));
 			
 			tracker.onFileOutputCreated(out);
 			
 			success = true;
 			return new CLOCExtractor(new PseudoFiles(out));
 			
 		} catch (IOException e) {
 			throw new BuildDataComputerException(e);
 			
 		} catch (InterruptedException e) {
 			throw new BuildDataComputerException(e);
 			
 		} finally {
 			deleteFile(fileList);
 			deleteFile(cloc);
 			
 			if (!success)
 				deleteFile(out);
 		}
 	}
 	
 	private String toPath(File file) {
 		return getCanonicalFile(file).getPath();
 	}
 	
 	private void run(String... args) throws IOException, InterruptedException {
 		Process process = Runtime.getRuntime().exec(args);
 		
 		int result = process.waitFor();
 		if (result != 0)
 			throw new IOException("Process returned: " + result);
 	}
 	
 	private File createFileList() throws IOException {
 		File result = null;
 		FileWriter writer = null;
 		boolean success = false;
 		try {
 			
 			result = File.createTempFile("cloc.flielist.", ".txt");
 			
 			writer = new FileWriter(result);
 			for (File file : files.getFiles()) {
 				if (!isKnownFileType(file.getPath()))
 					continue;
 				
 				writer.write(toPath(file) + "\n");
 			}
 			
 			success = true;
 			return result;
 			
 		} finally {
 			closeQuietly(writer);
 			
 			if (!success)
 				deleteFile(result);
 		}
 	}
 	
 	private File extractCLOCToTmp() throws IOException {
 		File result = null;
 		ZipInputStream in = null;
 		FileOutputStream out = null;
 		boolean success = false;
 		try {
 			
 			result = File.createTempFile("cloc.", ".pl");
 			
 			in = new ZipInputStream(load("cloc.pl.zip"));
 			if (in.getNextEntry() == null)
 				throw new IOException("No files inside zip");
 			
 			out = new FileOutputStream(result);
 			
 			copy(in, out);
 			
 			success = true;
 			return result;
 			
 		} finally {
 			closeQuietly(in);
 			closeQuietly(out);
 			
 			if (!success)
 				deleteFile(result);
 		}
 	}
 	
 	protected InputStream load(String filename) throws IOException {
 		Class<?> cls = getClass();
 		
 		InputStream result = cls.getClassLoader().getResourceAsStream(
 				cls.getPackage().getName().replace('.', '/') + "/" + filename);
 		if (result == null)
 			throw new IOException("Could not read " + filename);
 		
 		return result;
 	}
 	
 }
