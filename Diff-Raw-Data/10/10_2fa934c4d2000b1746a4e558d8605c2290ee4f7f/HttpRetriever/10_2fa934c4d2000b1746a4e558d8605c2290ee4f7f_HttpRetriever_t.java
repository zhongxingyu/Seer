 /*
  * Copyright 2007-2012 The Europeana Foundation
  *
  *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
  *  by the European Commission;
  *  You may not use this work except in compliance with the Licence.
  * 
  *  You may obtain a copy of the Licence at:
  *  http://joinup.ec.europa.eu/software/page/eupl
  *
  *  Unless required by applicable law or agreed to in writing, software distributed under
  *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
  *  any kind, either express or implied.
  *  See the Licence for the specific language governing permissions and limitations under
  *  the Licence.
  */
 package eu.europeana.uim.europeanaspecific.workflowstarts.httpzip;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
 import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
 import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
 import org.apache.commons.io.FileUtils;
 import eu.europeana.uim.europeanaspecific.workflowstarts.util.PropertyReader;
 import eu.europeana.uim.europeanaspecific.workflowstarts.util.UimConfigurationProperty;
 
 
 /**
  * Retrieves the specified zip file over the remote http location and performs
  * an iteration within the zipped file contents
  * 
  * @author Georgios Markakis <gwarkx@hotmail.com>
  * @since 5 Mar 2012
  */
 public class HttpRetriever implements Iterator<String> {
 
 
 	private List<String> filecontents;
 	
 	private TarArchiveInputStream tarInputstream;
 	
 	private Iterator<String> xmlentriesiterator;
 	
 	private static Random generator; 
 
 	private int number_of_recs;
 
 	/**
 	 * Private Class constructor (can be instantiated only with factory method)
 	 * 
 	 * @param zf
 	 *            The zip file reference
 	 * @param number_of_recs
 	 *            The number of files contained within the specific fzip file
 	 * @param zipentries
 	 *            References to the zipped files
 	 */
 	private HttpRetriever(List<String> filecontents) {
 		this.filecontents = filecontents;
 		this.number_of_recs = filecontents.size();
 		this.xmlentriesiterator = filecontents.iterator();
 		
 	}
 
 	/**
 	 * Static synchronized factory method that returns an instance of this
 	 * class. It first copies the remote file locally and then instantiates the
 	 * iterator.
 	 * 
 	 * @param url
 	 *            The url from which to fetch the file
 	 * @return an instance of this class
 	 * @throws IOException
 	 */
 	public synchronized static HttpRetriever createInstance(URL url)
 			throws IOException {
 
 		List<String> list = new ArrayList<String>();
 		
 		if(generator == null){
 			generator =  new Random();
 		}
 		//First copy the remote URI to a 
 		File dest = new File(PropertyReader.getProperty(UimConfigurationProperty.UIM_STORAGE_LOCATION)+new Integer(generator.nextInt()).toString());
 		FileUtils.copyURLToFile(url, dest, 100, 100000);
 
 		TarArchiveInputStream tarfile;
 

		
		//InputStreamReader isr = new InputStreamReader();

		
 			tarfile =  new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(dest)));
 
 			tarfile.available();
 			
 			TarArchiveEntry  entry;
 			
 			while( (entry = tarfile.getNextTarEntry()) != null){
 				
 				if(entry.isDirectory()){
 
 				}
 				else{
 					
 					byte[] content = new byte[(int) entry.getSize()];
 					
 					tarfile.read(content, 0, (int) entry.getSize());
 
					String xml = new String(content,"UTF-8");
 					
 					list.add(xml);
 				}
 			}
 			
 			
 			
 			tarfile.close();
 			
 		return new HttpRetriever(list);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Iterator#hasNext()
 	 */
 	@Override
 	public boolean hasNext() {
 		return xmlentriesiterator.hasNext();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Iterator#next()
 	 */
 	@Override
 	public String next() {
        return xmlentriesiterator.next();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Iterator#remove()
 	 */
 	@Override
 	public void remove() {
 		throw new UnsupportedOperationException("Operation not supported");
 	}
 
 	// Getters & Setters
 
 	/**
 	 * @return the number_of_recs
 	 */
 	public int getNumber_of_recs() {
 		return number_of_recs;
 	}
 
 	/**
 	 * @param number_of_recs
 	 *            the number_of_recs to set
 	 */
 	public void setNumber_of_recs(int number_of_recs) {
 		this.number_of_recs = number_of_recs;
 	}
 }
