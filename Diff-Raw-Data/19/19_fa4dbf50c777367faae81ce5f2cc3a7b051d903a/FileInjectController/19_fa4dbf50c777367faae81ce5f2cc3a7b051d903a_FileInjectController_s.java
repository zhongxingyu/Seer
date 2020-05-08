 package com.kik.inject.json;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.naming.NameNotFoundException;
 
 import org.apache.commons.codec.binary.Base64InputStream;
 import org.apache.maven.plugin.logging.Log;
 import org.json.simple.JSONObject;
 
 import com.kik.inject.iface.ParamProcessor;
 import com.kik.inject.replace.InputFileReplacer;
 
 public class FileInjectController implements ParamProcessor {
 	public final Map<String, File> mapConfig;
 	
 	private final Log _log;
 	private final File _outDir;
 	private final ParamProcessor _subProcessor;
 	private static final int CHARACTER_NOT_FOUND = -1;
 	private static final int MAX_BUFFER_SIZE = 1024;
 	
 	public FileInjectController(File rootFileHandle,File outDir, ParamProcessor mavenParamProcessor, Log log ) {
 		_outDir = outDir;
 		_log = log;
 		_subProcessor = mavenParamProcessor;
 		
 		if (rootFileHandle.isDirectory()) {
 			mapConfig = getFileList(rootFileHandle, true);
 		} else {
 			mapConfig = new HashMap<String, File>();
 			mapConfig.put(getVarEntry("", rootFileHandle), rootFileHandle);
 		}
 	}
 	
 
 	protected Map<String, File> getFileList(File directory, boolean recursive) {
 		return getFileList(directory, recursive, new String());
 	}
 
 	private Map<String, File> getFileList(File directory, boolean recursive,
 			String prepend ) {
 		final Map<String, File> fileMap = new HashMap<String, File>();
 		final File[] files = directory.listFiles();
 
 		for (File currFile : files) {
 			if (currFile.isFile()) {
 				try {
 					fileMap.put(getVarEntry(prepend, currFile), processFile(_outDir,currFile));
 				} catch (NameNotFoundException e) {
 					_log.error(e);
 				} catch (IOException e) {
 					_log.error(e);
 				}
 			} else if (recursive && currFile.isDirectory()) {
 				if (!currFile.getName().equals("")) {
 					String subDirPrepend = prepend + currFile.getName() + ".";
 					fileMap.putAll(getFileList(currFile, recursive,
 							subDirPrepend));
 				}
 			}
 		}
 
 		return fileMap;
 	}
 	
 	private String getVarEntry(String prepend, File file) {
 		final StringBuilder sb = new StringBuilder(prepend);
 		final String fileName = file.getName();
 
 		String varName = fileName;
 		int lastIndex = fileName.lastIndexOf(".");
 
 		if (lastIndex != CHARACTER_NOT_FOUND) {
 			varName = fileName.substring(0, lastIndex);
 		}
 
 		sb.append(varName);
 		return sb.toString();
 	}
 	
 	private File processFile(File outDir,File in) throws NameNotFoundException, IOException {
 		String tmpFile = UUID.randomUUID().toString();
 		
 		File outFile = new File(outDir, tmpFile);
 		InputFileReplacer replacer = new InputFileReplacer(in, outFile, "{*{", "}}", _log);
 		replacer.runReplace(_subProcessor);
 		return outFile;
 	}
 
 	
 	@Override
 	public boolean outputVarContentsToStream(OutputStream os, String varName)
 			throws NameNotFoundException, IOException {
 		
 		boolean isBase64 = false;
 		boolean isEscaped = false;
 		boolean canHandle = false;
 		final String cleanParamName;
 		if ( varName.contains("|") ) {
 			int index = varName.lastIndexOf("|");
 			String options = varName.substring(index,varName.length());
 			cleanParamName = varName.substring(0, index);
 			
 			if ( options.contains("b") ) {
 				isBase64 = true;
 			} 
 			
 			if ( options.contains("e") ) {
 				isEscaped = true;
 			}
 			
 		} else {
 			cleanParamName = varName;
 		}
		
 		if ( mapConfig.containsKey(cleanParamName)) {
 			File fileForVar = mapConfig.get(cleanParamName);
 			InputStream is = null;
 			try {
 				is = new FileInputStream(fileForVar);
 				InputStream wrappedInputStream = null;
 				if (  isBase64) {
					wrappedInputStream = new Base64InputStream(is,true);
 				} else {
 					wrappedInputStream = new BufferedInputStream(is);
 				}
 
 				byte[] ret = new byte[MAX_BUFFER_SIZE];
 				boolean done = false;
 
 				while (!done) {
 					int read = wrappedInputStream.read(ret);
 					if (read == -1) {
 						done = true;
 						break;
 					}
					String tmpString = new String(ret, 0, read);
					if ( isEscaped ) {
						writeJSONEscapedStringToStream(os, tmpString);
					} else {
						os.write(tmpString.getBytes(Charset.forName("UTF-8")));
					}
 				}
 
 				wrappedInputStream.close();
 
 			} catch (IOException ioe) {
 				_log.error(ioe);
 			} finally {
 				if (is != null) {
 					try {
 						is.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			canHandle = true;
 		}
 		
 		return canHandle;
 	}
 	
 	protected void writeJSONEscapedStringToStream(OutputStream os,
 			String toWrite) throws IOException {
 		String tmpString = JSONObject.escape(toWrite);
 		os.write(tmpString.getBytes(Charset.forName("UTF-8")));
 	}
 
 	@Override
 	public Set<String> getKeySet() {
 		Set<String> fileEnvVars = new HashSet<String>();
 		fileEnvVars.addAll(mapConfig.keySet());
 		return fileEnvVars;
 	}
 }
