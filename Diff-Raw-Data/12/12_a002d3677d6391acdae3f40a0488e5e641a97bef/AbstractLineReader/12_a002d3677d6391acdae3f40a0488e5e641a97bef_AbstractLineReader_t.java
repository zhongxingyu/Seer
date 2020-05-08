 package genepi.io.text;
 
 import genepi.io.FileUtil;
 import genepi.io.table.reader.IReader;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 public abstract class AbstractLineReader<o> implements IReader<o> {
 
 	private String filename;
 
 	private BufferedReader in;
 
 	private String line;
 
 	private int lineNumber;
 
 	public AbstractLineReader(String filename) throws IOException {
 		this.filename = filename;
 		FileInputStream inputStream = new FileInputStream(filename);
 		InputStream in2 = FileUtil.decompressStream(inputStream);
 		in = new BufferedReader(new InputStreamReader(in2));
 	}
 
 	public AbstractLineReader(DataInputStream inputStream) throws IOException {
 		InputStream in2 = FileUtil.decompressStream(inputStream);
 		in = new BufferedReader(new InputStreamReader(in2));
 	}
 
 	@Override
 	public boolean next() throws IOException {
 		line = in.readLine();

		
 		if (line != null) {
 			try {
 				lineNumber++;
 				if (!line.trim().isEmpty()) {
 					parseLine(line);
 				}
 			} catch (Exception e) {
 				throw new IOException(filename + ": Line " + lineNumber + ": "
 						+ e.getMessage());
 			}
 		}
 		return line != null;
 	}
 
 	protected abstract void parseLine(String line) throws Exception;
 
 	public int getLineNumber() {
 		return lineNumber;
 	}
 
 	@Override
 	public void close() {
 		try {
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void reset() {
 
 	}
 
 	public String getFilename() {
 		return filename;
 	}
	

 
 }
