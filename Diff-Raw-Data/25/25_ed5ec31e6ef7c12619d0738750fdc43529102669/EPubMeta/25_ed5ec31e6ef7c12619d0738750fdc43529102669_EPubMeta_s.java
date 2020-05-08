 /**
  * scrawler - to.networld.scrawler.ebooks.epub
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
 
 import java.net.URL;
 import java.util.Vector;
 
 import org.dom4j.DocumentException;
 
 import to.networld.scrawler.common.Ontologies;
 import to.networld.scrawler.common.RDFParser;
 import to.networld.scrawler.interfaces.IEPub;
 
 /**
  * @author Alex Oberhauser
  */
 public class EPubMeta extends RDFParser implements IEPub {
 
 	/**
 	 * @param url
 	 * @throws DocumentException
 	 */
 	public EPubMeta(URL url) throws DocumentException {
 		super(url);
 		this.namespace.put("dc", Ontologies.dcURI);
 		this.namespace.put("opf", Ontologies.opfURI);
 	}
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getAuthor()
 	 */
 	@Override
 	public String getAuthor() { return this.getSingleNode("dc:creator"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getDescription()
 	 */
 	@Override
 	public String getDescription() { return this.getSingleNode("dc:description"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getLanguage()
 	 */
 	@Override
 	public String getLanguage() { return this.getSingleNode("dc:language"); }
 
 	/**
	 * @see to.networld.scrawler.interfaces.IEPub#getOrginalPublicationDate()
 	 */
 	@Override
	public String getOrginalPublicationDate() { return this.getConditionalSingleNode("dc:date", "opf:event", "original-publication"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getPublisher()
 	 */
 	@Override
 	public String getPublisher() { return this.getSingleNode("dc:publisher"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getSubjects()
 	 */
 	@Override
 	public Vector<String> getSubjects() { return this.getNodes("dc:subject"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getTitle()
 	 */
 	@Override
 	public String getTitle() { return this.getSingleNode("dc:title"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IRDFEntity#getURL()
 	 */
 	@Override
 	public URL getURL() { return this.url; }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getOPSPublicationDate()
 	 */
 	@Override
 	public String getOPSPublicationDate() { return this.getConditionalSingleNode("dc:date", "opf:event", "ops-publication"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getPrimaryIdentifier()
 	 */
 	@Override
 	public String getPrimaryIdentifier() { return this.getConditionalSingleNode("dc:identifier", "id", "PrimaryID"); };
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getURIIdentifier()
 	 */
 	@Override
 	public String getURIIdentifier() { return this.getConditionalSingleNode("dc:identifier", "opf:scheme", "URI"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getSource()
 	 */
 	@Override
 	public String getSource() { return this.getSingleNode("dc:source"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.IEPub#getCoverage()
 	 */
 	@Override
 	public String getCoverage() { return this.getSingleNode("dc:coverage"); }
 
 }
