 /**
  * scrawler - to.networld.scrawler.ebooks
  *
  * Copyright (C) 2010 by Networld Project
  * Written by Alex Oberhauser <alexoberhauser@networld.to>
  * All Rights Reserved
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by 
  * the Free Software Foundation, version 3 of the License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of 
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this software.  If not, see <http://www.gnu.org/licenses/>
  */
 
 package to.networld.scrawler.ebooks.epub;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.UUID;
 import java.util.Vector;
 import java.util.zip.ZipFile;
 
 import org.dom4j.DocumentException;
 
 import to.networld.scrawler.common.ExtractZipFile;
 import to.networld.scrawler.interfaces.IEPub;
 
 /**
  * Root class that is responsible for the parsing of ebooks in the epub format.
  * 
  * @author Alex Oberhauser
  */
 public class EPub implements IEPub {
 	private final File tmpDirectory;
 	private final EPubMeta epubMeta;
 	
 	private URL url = null;
 	
 	private String author = null;
 	private String title = null;
 	private String description = null;
 	private String language = null;
 	private String orgPublicationDate = null;
 	private String opsPublicationDate = null;
 	private Vector<String> subjects = null;
 	private String publisher = null;
 	private String primaryIdentifier = null;
 	private String uriIdentifier = null;
 	private String source = null;
 	private String coverage = null;
 	
 	public EPub(ZipFile _epubFile) throws IOException, DocumentException {
 		this.tmpDirectory = new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString());
 		this.tmpDirectory.mkdir();
 		try {
 			ExtractZipFile.extractZIPFile(_epubFile, this.tmpDirectory);
 			EPubContainer epubContainer = new EPubContainer(new URL("file://" + new File(this.tmpDirectory + File.separator +
 					"META-INF" + File.separator + "container.xml").toString()));
 			this.epubMeta = new EPubMeta(new URL("file://" + new File(this.tmpDirectory + File.separator +
 					epubContainer.getRootFile())));
 			this.setInformation();
 		} finally {
 			this.clean();
 		}
 	}
 	
 	private void setInformation() {
 		this.url = this.epubMeta.getURL();
 		
 		this.author = this.epubMeta.getAuthor();
 		this.title = this.epubMeta.getTitle();
 		this.description = this.epubMeta.getDescription();
 		this.language = this.epubMeta.getLanguage();
		this.orgPublicationDate = this.epubMeta.getOriginalPublicationDate();
 		this.opsPublicationDate = this.epubMeta.getOPSPublicationDate();
 		this.subjects = this.epubMeta.getSubjects();
 		this.publisher = this.epubMeta.getPublisher();
 		this.primaryIdentifier = this.epubMeta.getPrimaryIdentifier();
 		this.uriIdentifier = this.epubMeta.getURIIdentifier();
 		this.source = this.epubMeta.getSource();
 		this.coverage = this.epubMeta.getCoverage();
 	}
 	
 	private void clean() throws FileNotFoundException {
 		ExtractZipFile.deleteDirectory(this.tmpDirectory);
 	}
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getAuthor()
 	 */
 	@Override
 	public String getAuthor() { return this.author; }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getDescription()
 	 */
 	@Override
 	public String getDescription() { return this.description; }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getLanguage()
 	 */
 	@Override
 	public String getLanguage() { return this.language; }
 
 	/**
	 * @see to.networld.scrawler.interfaces.IEPub#getOriginalPublicationDate()
 	 */
 	@Override
	public String getOriginalPublicationDate() { return this.orgPublicationDate; }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getPublisher()
 	 */
 	@Override
 	public String getPublisher() { return this.publisher; }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getSubjects()
 	 */
 	@Override
 	public Vector<String> getSubjects() { return this.subjects; }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getTitle()
 	 */
 	@Override
 	public String getTitle() { return this.title; }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getOPSPublicationDate()
 	 */
 	@Override
 	public String getOPSPublicationDate() { return this.opsPublicationDate; }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getPrimaryIdentifier()
 	 */
 	@Override
 	public String getPrimaryIdentifier() { return this.primaryIdentifier; }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getURIIdentifier()
 	 */
 	@Override
 	public String getURIIdentifier() { return this.uriIdentifier; }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getSource()
 	 */
 	@Override
 	public String getSource() { return this.source; }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getCoverage()
 	 */
 	@Override
 	public String getCoverage() { return this.coverage; }
 	
 	/**
 	 * @see to.networld.scrawler.interfaces.IRDFEntity#getURL()
 	 */
 	@Override
 	public URL getURL() { return this.url; }
 }
