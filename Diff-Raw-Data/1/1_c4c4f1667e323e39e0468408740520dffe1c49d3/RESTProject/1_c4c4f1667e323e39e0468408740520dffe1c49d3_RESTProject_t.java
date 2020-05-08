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
 
 package net.sf.okapi.lib.longhornapi.impl.rest;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 
 import net.sf.okapi.lib.longhornapi.LonghornFile;
 import net.sf.okapi.lib.longhornapi.LonghornProject;
 import net.sf.okapi.lib.longhornapi.impl.rest.RESTFile.Filetype;
 
 import org.apache.commons.httpclient.methods.multipart.FilePart;
 import org.apache.commons.httpclient.methods.multipart.Part;
 
 /**
  * Implementation of {@link LonghornProject} for Longhorn's RESTful interface.
  */
 public class RESTProject implements LonghornProject {
 	private URI projUri;
 
 	protected RESTProject() {
 	}
 	
 	protected RESTProject(URI projUri) {
 		this.projUri = projUri;
 	}
 	
 	protected RESTProject(URI serviceUri, String projId) {
 		String newProjUri = serviceUri.toString();
 		if (!newProjUri.endsWith("/"))
 			newProjUri += "/";
		newProjUri += "projects/";
 		newProjUri += projId;
 		
 		try {
 			this.projUri = new URI(newProjUri);
 		}
 		catch (URISyntaxException e) {
 			// Should not happen, because the URI was used in the Service before
 			throw new RuntimeException(e);
 		}
 	}
 	
 	protected URI getProjectURI() {
 		return projUri;
 	}
 
 	@Override
 	public void addBatchConfiguration(File bconf) throws FileNotFoundException {
 		Part[] parts = {
 				new FilePart("batchConfiguration", bconf.getName(), bconf)};
 		try {
 			Util.post(projUri + "/batchConfiguration", parts);
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void addInputFile(File inputFile, String relativePath) throws FileNotFoundException {
 		String uri = projUri + "/inputFiles/" + relativePath;
 		Part[] inputParts = {
 				new FilePart("inputFile", inputFile.getName(), inputFile)};
 		try {
 			Util.put(uri, inputParts);
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void delete() {
 		try {
 			Util.delete(projUri.toString());
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void executePipeline() {
 		try {
 			Util.post(projUri + "/tasks/execute", null);
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	@Override
 	public void executePipeline(String sourceLanguage, String targetLanguage) {
 		if (sourceLanguage == null || targetLanguage == null)
 			throw new NullPointerException();
 		
 		try {
 			Util.post(projUri + "/tasks/execute/" + sourceLanguage + "/" + targetLanguage, null);
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public ArrayList<LonghornFile> getInputFiles() {
 		try {
 			ArrayList<String> filenames = Util.getList(projUri + "/inputFiles");
 			ArrayList<LonghornFile> files = new ArrayList<LonghornFile>();
 			
 			for (String filename : filenames) {
 				files.add(new RESTFile(this, Filetype.input, filename));
 			}
 			
 			return files;
 		}
 		catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public ArrayList<LonghornFile> getOutputFiles() {
 		try {
 			ArrayList<String> filenames = Util.getList(projUri + "/outputFiles");
 			ArrayList<LonghornFile> files = new ArrayList<LonghornFile>();
 			
 			for (String filename : filenames) {
 				files.add(new RESTFile(this, Filetype.output, filename));
 			}
 			
 			return files;
 		}
 		catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void addInputFilesFromZip(File zipFile) throws FileNotFoundException {
 		Part[] parts = {
 				new FilePart("inputFile", zipFile.getName(), zipFile)};
 		try {
 			Util.post(projUri + "/inputFiles.zip", parts);
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public InputStream getOutputFilesAsZip() {
 		//TODO check if any files are available
 		try {
 			URI remoteFile = new URI(projUri + "/outputFiles.zip");
 			return remoteFile.toURL().openStream();
 		}
 		catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 }
