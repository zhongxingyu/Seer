 /*
  * MediaWiki import/export processing tools
  * Copyright 2005 by Brion Vibber
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  * $Id$
  */
 
 package org.mediawiki.importer;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class XmlDumpReader {
 	InputStream input;
 	DumpWriter writer;
 	
 	StringBuffer buffer;
 	
 	Siteinfo siteinfo;
 	Page page;
 	boolean pageSent;
 	Contributor contrib;
 	Revision rev;
 	int nskey;
 	
 	public XmlDumpReader(InputStream inputStream, DumpWriter writer) {
 		input = inputStream;
 		this.writer = writer;
 	}
 	
 	class MediaWikiHandler extends DefaultHandler {
 		public void startElement(String uri, String localname, String qName, Attributes attributes) throws SAXException {
 			//System.out.println("<" + qName + ">");
 			buffer = new StringBuffer();
 			try {
 				if (qName.equals("mediawiki")) openMediaWiki();
 				else if (qName.equals("siteinfo")) openSiteinfo();
 				else if (qName.equals("namespaces")) openNamespaces();
 				else if (qName.equals("namespace")) openNamespace(attributes);
 				else if (qName.equals("page")) openPage();
 				else if (qName.equals("revision")) openRevision();
 				else if (qName.equals("contributor")) openContributor();
 			} catch (IOException e) {
 				throw new SAXException(e);
 			}
 		}
 		
 		public void characters(char[] ch, int start, int length) {
 			buffer.append(ch, start, length);
 		}
 		
 		public void endElement(String uri, String localname, String qName) throws SAXException {
 			//System.out.println("</" + qName + ">");
 			try {
 				if (qName.equals("mediawiki")) closeMediaWiki();
 				else if (qName.equals("siteinfo")) closeSiteinfo();
 				else if (qName.equals("sitename")) readSitename();
 				else if (qName.equals("base")) readBase();
 				else if (qName.equals("generator")) readGenerator();
 				else if (qName.equals("case")) readCase();
 				else if (qName.equals("namespaces")) closeNamespaces();
 				else if (qName.equals("namespace")) closeNamespace();
 				else if (qName.equals("page")) closePage();
 				else if (qName.equals("title")) readTitle();
 				else if (qName.equals("id")) readId();
 				else if (qName.equals("restrictions")) readRestrictions();
 				else if (qName.equals("revision")) closeRevision();
 				else if (qName.equals("timestamp")) readTimestamp();
 				else if (qName.equals("contributor")) closeContributor();
 				else if (qName.equals("username")) readUsername();
 				else if (qName.equals("ip")) readIp();
 				else if (qName.equals("comment")) readComment();
 				else if (qName.equals("minor")) readMinor();
 				else if (qName.equals("text")) readText();
 			} catch (IOException e) {
 				throw new SAXException(e);
 			}
 		}
 	}
 	
 	public void readDump() throws IOException {
 		try {
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		//_reader.WhitespaceHandling = WhitespaceHandling.Significant;
 		SAXParser parser = factory.newSAXParser();
 		
 		parser.parse(input, new MediaWikiHandler());
 		} catch (ParserConfigurationException e) {
 			throw new IOException(e.toString());
 		} catch (SAXException e) {
 			throw new IOException(e.toString());
 		}
 		writer.close();
 	}
 	
 	// ----------
 	
 	private void openMediaWiki() throws IOException {
 		siteinfo = null;
 		writer.writeStartWiki();
 	}
 	
 	private void closeMediaWiki() throws IOException {
 		writer.writeEndWiki();
 		siteinfo = null;
 	}
 	
 	// ------------------
 		
 	private void openSiteinfo() {
 		siteinfo = new Siteinfo();
 	}
 	
 	private void closeSiteinfo() throws IOException {
 		writer.writeSiteinfo(siteinfo);
 	}
 	
 	private void readSitename() {
 		siteinfo.Sitename = buffer.toString();
 	}
 	
 	private void readBase() {
 		siteinfo.Base = buffer.toString();
 	}
 	
 	private void readGenerator() {
 		siteinfo.Generator = buffer.toString();
 	}
 	
 	private void readCase() {
 		siteinfo.Case = buffer.toString();
 	}
 	
 	private void openNamespaces() {
 		siteinfo.Namespaces = new NamespaceSet();
 	}
 	
 	private void openNamespace(Attributes attribs) {
 		nskey = Integer.parseInt(attribs.getValue("key"));
 	}
 	
 	private void closeNamespace() {
 		siteinfo.Namespaces.add(nskey, buffer.toString());
 	}
 
 	private void closeNamespaces() {
 		// NOP
 	}
 	
 	// -----------
 	
 	private void openPage() {
 		page = new Page();
 		pageSent = false;
 	}
 	
 	private void closePage() throws IOException {
 		if (pageSent)
 			writer.writeEndPage();
 		page = null;
 	}
 	
 	private void readTitle() {
 		page.Title = new Title(buffer.toString(), siteinfo.Namespaces);
 	}
 	
 	private void readId() {
 		int id = Integer.parseInt(buffer.toString());
 		if (contrib != null)
 			contrib.Id = id;
 		else if (rev != null)
 			rev.Id = id;
 		else if (page != null)
 			page.Id = id;
 		else
 			throw new IllegalArgumentException("Unexpected <id> outside a <page>, <revision>, or <contributor>");
 	}
 	
 	private void readRestrictions() {
 		page.Restrictions = buffer.toString();
 	}
 	
 	// ------
 	
 	private void openRevision() throws IOException {
 		if (!pageSent) {
 			writer.writeStartPage(page);
 			pageSent = true;
 		}
 		
 		rev = new Revision();
 	}
 	
 	private void closeRevision() throws IOException {
 		writer.writeRevision(rev);
 		rev = null;
 	}
 
 	private void readTimestamp() {
 		rev.Timestamp = parseUTCTimestamp(buffer.toString());
 	}
 
 	private void readComment() {
 		rev.Comment = buffer.toString();
 	}
 
 	private void readMinor() {
 		rev.Minor = true;
 	}
 
 	private void readText() {
 		rev.Text = buffer.toString();
 	}
 	
 	// -----------
 	private void openContributor() {
 		contrib = null;
 	}
 	
 	private void closeContributor() {
 		if (contrib == null)
 			throw new IllegalArgumentException("Invalid contributor");
 		
 		rev.Contributor = contrib;
 		contrib = null;
 	}
 
 
 	private void readUsername() {
 		contrib = new Contributor(buffer.toString());
 	}
 	
 	private void readIp() {
 		contrib = new Contributor(buffer.toString());
 	}
 	
 	private Calendar parseUTCTimestamp(String text) {
 		// 2003-10-26T04:50:47Z
		//
		// We're doing this manually because:
		// * XmlConvert.ToDateTime() is slow
		// * XmlConvert.ToDateTime() is lossy at local DST boundary
		// * DateTime.ParseExact seems to be similarly lossy, unless
		//   there's a magic formula I haven't found yet.
 		String trimmed = text.trim();
		return new GregorianCalendar(
 			Integer.parseInt(trimmed.substring(0,0+4)),     // year
 			Integer.parseInt(trimmed.substring(5,5+2)) - 1, // month is 0-based!
 			Integer.parseInt(trimmed.substring(8,8+2)),     // day
 			Integer.parseInt(trimmed.substring(11,11+2)),   // hour
 			Integer.parseInt(trimmed.substring(14,14+2)),   // minute
 			Integer.parseInt(trimmed.substring(17,17+2)));  // second
 	}
 }
