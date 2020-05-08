 package org.vamdc.validator.iocontroller;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 import org.apache.commons.io.input.TeeInputStream;
 import org.vamdc.dictionary.HeaderMetrics;
 import org.vamdc.validator.OperationModes;
 import org.vamdc.validator.Setting;
 import org.vamdc.validator.interfaces.DocumentElementsLocator;
 import org.vamdc.validator.interfaces.ProgressMonitor;
 import org.vamdc.validator.interfaces.XSAMSIOModel;
 import org.vamdc.validator.interfaces.XSAMSValidatorException;
 import org.vamdc.validator.source.XSAMSSource;
 import org.vamdc.validator.source.XSAMSSourceException;
 import org.vamdc.validator.source.http.HttpXSAMSSource;
 import org.vamdc.validator.source.plugin.PluginXSAMSSource;
 import org.vamdc.validator.storage.RawStorage;
 import org.vamdc.validator.validator.Validator;
import org.vamdc.xsams.io.PrettyPrint;
 
 public class XSAMSDocument implements XSAMSIOModel{
 
 	@Override
 	public long doQuery(String query) throws XSAMSSourceException {
 		//Save query string
 		this.query = query;
		
		if (source==null)
			throw new XSAMSSourceException("XSAMS source is not available");
 		//Setup xsams source
 		xsamsStream = source.getXsamsStream(query);
 		//Process it
 		return processStream(xsamsStream, query);
 	}
 
 	@Override
 	public String getBlock(long lineIndex, int lineCount) {
 		if (storage!=null)
 			return storage.getLines((int) lineIndex, lineCount);
 		else return "";
 	}
 
 	@Override
 	public long getLineCount() {
 		if (storage!=null)
 			return storage.getLineCount();
 		else 
 			return 0;
 	}
 
 	@Override
 	public long getSize() {
 		if (storage!=null) 
 			return storage.getSizeBytes();
 		else 
 			return 0;
 	}
 
 	@Override
 	public long loadFile(File xsamsDocument) throws IOException {
 		//Setup XSAMS input stream
		xsamsStream = new BufferedInputStream(new FileInputStream(xsamsDocument),4096);
		if (Setting.PrettyPrint.getBool())
			xsamsStream = PrettyPrint.transformStatic(xsamsStream);
 		//Process it
 		return processStream(xsamsStream, "");
 	}
 	
 	
 
 	@Override
 	public long saveFile(File xsamsDocument)throws IOException {
 		storage.saveFile(xsamsDocument);
 		return storage.getSizeBytes();
 	}
 
 	@Override
 	public int searchString(String word, int lineOffset,boolean ignoreCase) {
 		if (ignoreCase)
 			word = word.toLowerCase();
 		if (storage!=null)
 			for (int line = lineOffset;line<storage.getLineCount();line++){
 				String textLine = storage.getLine(line);
 				if (ignoreCase)
 					textLine = textLine.toLowerCase();
 				if (textLine.contains(word)) 
 					return line;
 			}
 		return -1;
 	}
 
 	@Override
 	public void setProgressMonitor(ProgressMonitor mon) {
 		this.monitor = mon;
 	}
 
 	@Override
 	public String getErrorInfo() {
 		if (errorMsg!=null)
 			return errorMsg;
 		return "";
 	}
 
 
 	/**
 	 * Default constructor
 	 */
 	public XSAMSDocument(){
 		try{
 			reconfigure();
 		}catch (XSAMSSourceException e){
 			errorMsg = e.getMessage();
 		} catch (XSAMSValidatorException e) {
 			errorMsg = e.getMessage();
 		}
 	}
 
 	private XSAMSSource source;
 	private RawStorage storage;
 	private Validator validator;
 	private ProgressMonitor monitor;
 	private String errorMsg;
 	private InputStream xsamsStream;
 	private String query;
 
 	/**
 	 * Process xsams stream: copy to temp storage, validate
 	 * @param XSAMSStream stream containing XSAMS data
 	 * @return
 	 */
 	private long processStream(InputStream XSAMSStream,String query){
 		//Process query, copy stream into storage
 		if (XSAMSStream==null){
 			storage = new RawStorage(monitor);
 			monitor.tick();
 			monitor.done(0,query);
 			return 0;
 		}
 			
 		//Reset errors
 		errorMsg=null;
 		//Create new storage
 		storage = new RawStorage(monitor);
 		
 		//Setup copying stream
 		InputStream copied = new TeeInputStream(XSAMSStream,storage);
 		//Parse stream to find block positions and errors
 		try{
 			validator.parse(copied);
 			//Parsing is complete, say that we're done.
 			if (monitor!=null)
 				monitor.done(storage.getLineCount(),query);
 		}catch (IOException e){
 			if (monitor!=null)
 				monitor.done(-1,query);
 			errorMsg = e.getMessage();
 		}finally{
 			if (copied!=null)
 				try {	
 					copied.close();
 				} catch (IOException e) {
 				}
 
 				if (storage!=null)
 					try {
 						storage.flush();
 					} catch (IOException e) {
 					}
 		}
 
 		//Return document size in lines
 		return storage.getLineCount();
 	}
 	
 	private XSAMSSource setupSource() throws XSAMSSourceException{
 		XSAMSSource src = null;
 		switch( OperationModes.valueOf(Setting.OperationMode.getValue())){
 		case network:
 			src = new HttpXSAMSSource();
 			break;
 		case plugin:
 			String pluginClass = Setting.PluginClass.getValue();
 			src = new PluginXSAMSSource(pluginClass);
 			break;
 		default:
 			break;
 		}
 
 		return src;
 	}
 
 	@Override
 	public DocumentElementsLocator getElementsLocator() {
 		if (validator!=null)
 			return validator.getElements();
 		return null;
 	}
 
 	@Override
 	public void close() {
 		
 	}
 
 	@Override
 	public void stopQuery() {
 		if (xsamsStream!=null)
 			try {
 				xsamsStream.close();
 			} catch (IOException e) {
 			}
 	}
 
 	@Override
 	public void reconfigure() throws XSAMSSourceException, XSAMSValidatorException {
 		//Stop running queries if any
 		stopQuery();
 		//Remove source&validator
 		source = null;
 		validator=null;
 		
 		//Setup XSAMS validator
 		validator = new Validator(
 				Setting.SchemaFile.getValue(),
 				Setting.SchemaLocations.getValue());
 		
 		//Setup XSAMS source
 		source = setupSource();
 		
 	}
 
 	@Override
 	public Collection<String> getRestrictables() {
 		Collection<String> restrict = new ArrayList<String>();
 		if (source!=null)
 			restrict.addAll(source.getRestrictables());
 		return restrict;
 	}
 	
 	@Override
 	public Collection<String> getSampleQueries() {
 		Collection<String> queries = new ArrayList<String>();
 		if (source!=null && source.getSampleQueries()!=null)
 			queries.addAll(source.getSampleQueries());
 		return queries;
 	}
 	
 	@Override
 	public String getQuery() {
 		return query;
 	}
 
 	@Override
 	public Map<HeaderMetrics, String> previewQuery(String query)
 			throws XSAMSSourceException {
 		this.query = query;

		if (source==null)
			throw new XSAMSSourceException("XSAMS source is not available");
		
 		return source.getMetrics(query);
 	}
 
 	
 
 
 
 
 
 
 }
