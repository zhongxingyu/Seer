 /*===========================================================================
   Copyright (C) 2011 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.applications.rainbow.batchconfig;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.RandomAccessFile;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.okapi.applications.rainbow.Input;
 import net.sf.okapi.applications.rainbow.pipeline.PipelineStorage;
 import net.sf.okapi.applications.rainbow.pipeline.StepInfo;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.ReferenceParameter;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.exceptions.OkapiFileNotFoundException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.pipeline.IPipeline;
 import net.sf.okapi.common.pipeline.IPipelineStep;
 
 public class BatchConfiguration {
 	
 	private static final int MAXBUFFERSIZE = 1024*8; 
 	private static final int MAXBLOCKLEN = 65000;
 	private static final String SIGNATURE = "batchConf";
 	private static final int VERSION = 1;
 
 	public void exportConfiguration (String configPath,
 		IPipeline pipeline,
 		IFilterConfigurationMapper fcMapper,
 		List<Input> inputFiles)
 	{
 		DataOutputStream dos = null;
 		
 		try {
 			// Prepare the output
 			dos = new DataOutputStream(new FileOutputStream(configPath));
 			dos.writeUTF(SIGNATURE);
 			dos.writeInt(VERSION);
 
 			//=== Section 1: the dereferenced files of the pipeline's parameters
 			// int = id (-1 mark the end)
 			// String = extension
 			// String = content
 			
 			// Go through each step of the pipeline
 			for ( IPipelineStep step : pipeline.getSteps() ) {
 				// Get the parameters for that step
 				IParameters params = step.getParameters();
 				if ( params == null ) continue;
 				// Get all methods for the parameters object
 				Method[] methods = params.getClass().getMethods();
 				// Look for references
 				int id = 0;
 				for ( Method m : methods ) {
 					if ( Modifier.isPublic(m.getModifiers() ) && m.isAnnotationPresent(ReferenceParameter.class)) {
 						String refPath = (String)m.invoke(params);
 						harvestReferencedFile(dos, ++id, refPath);
 					}
 				}
 			}
 			
 			// Last ID=-1 to mark no more references
 			dos.writeInt(-1);
 
 			
 			//=== Section 2: The pipeline itself
 			
 			// OK to use null, because the available steps are not used for writing
 			PipelineStorage store = new PipelineStorage(null);
 			store.write(pipeline);
 			writeLongString(dos, store.getStringOutput());
 			
 
 			//=== Section 3: The filter configurations
 
 			// Get the number of custom configurations
 			Iterator<FilterConfiguration> iter = fcMapper.getAllConfigurations();
 			int count = 0;
 			while ( iter.hasNext() ) {
 				if ( iter.next().custom ) count++;
 			}
 			dos.writeInt(count);
 
 			// Write each filter configuration
 			iter = fcMapper.getAllConfigurations();
 			while ( iter.hasNext() ) {
 				FilterConfiguration fc = iter.next();
 				if ( fc.custom ) {
 					dos.writeUTF(fc.configId);
 					IParameters params = fcMapper.getCustomParameters(fc);
 					dos.writeUTF(params.toString());
 				}
 			}
 			
 			//=== Section 4: Mapping extensions -> filter configuration id
 			
 			if ( inputFiles != null ) {
 				// Gather the extensions (if duplicate: first one is used)
 				HashMap<String, String> extMap = new HashMap<String, String>();
 				// From the input files first
 				for ( Input input : inputFiles ) {
 					String ext = Util.getExtension(input.relativePath);
 					if ( !Util.isEmpty(ext) && !extMap.containsKey(ext) && !Util.isEmpty(input.filterConfigId)) {
 						extMap.put(ext, input.filterConfigId);
 					}
 				}
 				// Then complement with the default
 				iter = fcMapper.getAllConfigurations();
 				while ( iter.hasNext() ) {
 					FilterConfiguration fc = iter.next();
 					String ext = fc.extensions;
 					if ( Util.isEmpty(ext) ) continue;
 					int n = ext.indexOf(';');
 					if ( n > 0 ) ext = ext.substring(0, n);
 					if ( !extMap.containsKey(ext) ) {
 						extMap.put(ext, fc.configId);
 					}
 				}
 				// Write out the mapping
 				dos.writeInt(extMap.size());
 				for ( String ext : extMap.keySet() ) {
 					dos.writeUTF(ext);
 					dos.writeUTF(extMap.get(ext));
 				}
 			}
 			else {
 				dos.writeInt(0); // None
 			}
 		}
 		catch ( IllegalArgumentException e ) {
 			throw new OkapiIOException("Error when calling getter method.", e); 
 		}
 		catch ( IllegalAccessException e ) {
 			throw new OkapiIOException("Error when calling getter method.", e); 
 		}
 		catch ( InvocationTargetException e ) {
 			throw new OkapiIOException("Error when calling getter method.", e); 
 		}
 		catch ( FileNotFoundException e ) {
 			throw new OkapiFileNotFoundException(e);
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException(e);
 		}
 		finally {
 			// Close the output file
 			if ( dos != null ) {
 				try {
 					dos.close();
 				}
 				catch ( IOException e ) {
 					throw new OkapiIOException(e);
 				}
 			}
 		}
 	}
 	
 	public void installConfiguration (String configPath,
 		String outputDir,
 		Map<String, StepInfo> availableSteps)
 	{
 		RandomAccessFile raf = null;
 		PrintWriter pw = null;
 		try {
 			raf = new RandomAccessFile(configPath, "r");
 			String tmp = raf.readUTF(); // signature
 			if ( !SIGNATURE.equals(tmp) ) {
 				throw new OkapiIOException("Invalid file format.");
 			}
 			int version = raf.readInt(); // Version info
 			if ( version != VERSION ) {
 				throw new OkapiIOException("Invalid version.");
 			}
 			
 			Util.createDirectories(outputDir+File.separator);
 			
 			//=== Section 1: references data
 
 			// Build a lookup table to the references
 			HashMap<Integer, Long> refMap = new HashMap<Integer, Long>();
 			int id = raf.readInt(); // First ID or end of section marker
 			long pos = raf.getFilePointer();
 			while ( id != -1 ) {
 				raf.readUTF(); // Skip filename
 				// Add the entry in the lookup table
 				refMap.put(id, pos);
 				// Skip over the data to move to the next reference
 				long size = raf.readLong();
 				if ( size > 0 ) {
 					raf.seek(raf.getFilePointer()+size);
 				}
 				// Then get the information for next entry
 				id = raf.readInt(); // ID
 				pos = raf.getFilePointer(); // Position
				if ( id > 0 ) raf.readUTF(); // Skip filename
 			}
 		
 			//=== Section 2 : the pipeline itself
 			
 			tmp = readLongString(raf);
 			long startFilterConfigs = raf.getFilePointer();
 			
 			// Read the pipeline and instantiate the steps
 			PipelineStorage store = new PipelineStorage(availableSteps, (CharSequence)tmp);
 			IPipeline pipeline = store.read(); 
 			
 			byte[] buffer = new byte[MAXBUFFERSIZE];
 
 			// Go through each step of the pipeline
 			for ( IPipelineStep step : pipeline.getSteps() ) {
 				// Get the parameters for that step
 				IParameters params = step.getParameters();
 				if ( params == null ) continue;
 				// Get all methods for the parameters object
 				Method[] methods = params.getClass().getMethods();
 				// Look for references
 				id = 0;
 				for ( Method m : methods ) {
 					if ( Modifier.isPublic(m.getModifiers() ) && m.isAnnotationPresent(ReferenceParameter.class)) {
 						// Update the references to point to the new location
 						// Read the reference content
 						pos = refMap.get(++id);
 						raf.seek(pos);
 						String filename = raf.readUTF();
 						long size = raf.readLong(); // Read the size
 						// Save the data to a file
 						if ( !Util.isEmpty(filename) ) {
 							String path = outputDir + File.separator + filename; 
 							FileOutputStream fos = new FileOutputStream(path);
 							int toRead = (int)Math.min(size, MAXBUFFERSIZE);
 							int bytesRead = raf.read(buffer, 0, toRead);
 							while ( bytesRead > 0 ) {
 								fos.write(buffer, 0, bytesRead);
 								size -= bytesRead;
 								if ( size <= 0 ) break;
 								toRead = (int)Math.min(size, MAXBUFFERSIZE);
 								bytesRead = raf.read(buffer, 0, toRead);
 							}
 							fos.close();
 							// Update the reference in the parameters to point to the saved file
 							// Test changing the value
 							String setMethodName = "set"+m.getName().substring(3);
 							Method setMethod = params.getClass().getMethod(setMethodName, String.class);
 							setMethod.invoke(params, path);
 						}
 					}
 				}
 			}
 			
 			// Write out the pipeline file
 			String path = outputDir + File.separator + "pipeline.pln";
 			pw = new PrintWriter(path, "UTF-8");
 			store.write(pipeline);
 			pw.write(store.getStringOutput());
 			pw.close();
 			
 			//=== Section 3 : the filter configurations
 			
 			raf.seek(startFilterConfigs);
 			// Get the number of filter configurations
 			int count = raf.readInt();
 			
 			// Read each one
 			for ( int i=0; i<count; i++ ) {
 				String configId = raf.readUTF();
 				String data = raf.readUTF();
 				// And create the parameters file
 				path = outputDir + File.separator + configId + ".fprm";
 				pw = new PrintWriter(path, "UTF-8"); 
 				pw.write(data);
 				pw.close();
 			}
 		
 			//=== Section 4: the extensions -> filter configuration id mapping
 			
 			// Get the number of mappings
 			path = outputDir + File.separator + "extensions-mapping.txt";
 			pw = new PrintWriter(path, "UTF-8");
 			String lb = System.getProperty("line.separator");
 			count = raf.readInt();
 			for ( int i=0; i<count; i++ ) {
 				String ext  = raf.readUTF();
 				String configId = raf.readUTF();
 				pw.write(ext + "\t" + configId + lb);
 			}
 			pw.close();
 			
 		}
 		catch ( Throwable e ) {
 			throw new OkapiIOException("Error when installing the batch configuration.\n"+e.getMessage(), e);
 		}
 		finally {
 			if ( pw != null ) {
 				pw.close();
 			}
 			if ( raf != null ) {
 				try {
 					raf.close();
 				}
 				catch ( IOException e ) {
 					throw new OkapiIOException(e);
 				}
 			}
 		}
 	}
 
 	private void harvestReferencedFile (DataOutputStream dos,
 		int id,
 		String refPath)
 		throws IOException
 	{
 		FileInputStream fis = null;
 		try {
 			dos.writeInt(id);
 			String filename = Util.getFilename(refPath, true);
 			dos.writeUTF(filename);
 
 			// Deal with empty references
 			if ( Util.isEmpty(refPath) ) {
 				dos.writeLong(0);
 				return;
 			}
 			// Else: copy the content of the referenced file
 			
 			// Write the size of the file
 			File file = new File(refPath);
 			long size = file.length();
 			dos.writeLong(size);
 			
 			// Write the content
 			if ( size > 0 )  {
 				fis = new FileInputStream(refPath);
 				int bufferSize = Math.min(fis.available(), MAXBUFFERSIZE);
 				byte[] buffer = new byte[bufferSize];
 				int bytesRead = fis.read(buffer, 0, bufferSize);
 				while ( bytesRead > 0 ) {
 					dos.write(buffer, 0, bufferSize);
 					bufferSize = Math.min(fis.available(), MAXBUFFERSIZE);
 					bytesRead = fis.read(buffer, 0, bufferSize);
 				}
 			}
 		}
 		finally {
 			if ( fis != null ) {
 				fis.close();
 			}
 		}
 	}
 	
 	private void writeLongString (DataOutputStream dos,
 		String data)
 		throws IOException
 	{
 		int r = (data.length() % MAXBLOCKLEN);
 		int n = (data.length() / MAXBLOCKLEN);
 		int count = n + ((r > 0) ? 1 : 0);
 		
 		dos.writeInt(count); // Number of blocks
 		int pos = 0;
 	
 		// Write the full blocks
 		for ( int i=0; i<n; i++ ) {
 			dos.writeUTF(data.substring(pos, pos+MAXBLOCKLEN));
 			pos += MAXBLOCKLEN;
 		}
 		// Write the remaining text
 		if ( r > 0 ) {
 			dos.writeUTF(data.substring(pos));
 		}
 	}
 
 	private String readLongString (RandomAccessFile raf)
 		throws IOException
 	{
 		StringBuilder tmp = new StringBuilder();
 		int count = raf.readInt();
 		for ( int i=0; i<count; i++ ) {
 			tmp.append(raf.readUTF());
 		}
 		return tmp.toString();
 	}
 
 }
