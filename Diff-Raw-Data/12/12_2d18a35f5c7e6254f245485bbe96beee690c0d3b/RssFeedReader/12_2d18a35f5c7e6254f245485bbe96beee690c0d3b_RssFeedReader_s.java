 /*
  * Copyright 2007 Sebastien Brunot (sbrunot@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.sourceforge.buildmonitor.utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.ParseException;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  * A class that can parse an RSS feed and return a Feed object.
  * @author sbrunot
  *
  */
 public class RssFeedReader
 {
 	public class RssFeedContentHandler extends DefaultHandler
 	{
 		private static final int TITLE_ATTRIBUTE = 1;
 		private static final int DESCRIPTION_ATTRIBUTE = 2;
 		private static final int PUBDATE_ATTRIBUTE = 3;
 		private static final int LINK_ATTRIBUTE = 4;
 		
 		RssFeedDocument rssFeedDocument = null;
 		RssFeedItem currentItem = null;
 		int currentItemAttributeToSet = -1;
 		DateFormat rssFeedDateFormat = null;
 		
 		public RssFeedContentHandler(DateFormat rssFeedDateFormat)
 		{
 			if (rssFeedDateFormat == null)
 			{
 				throw new IllegalArgumentException("The RSS Feed date format cannot be null !");
 			}
 			this.rssFeedDateFormat = rssFeedDateFormat;
 		}
 		
 		public void startDocument()
 		{
 			this.rssFeedDocument = new RssFeedDocument();
 		}
 
 		public void startElement(String nameSpace, String localName, String qName, Attributes attributes)
 		{
 			if ("item".equals(localName))
 			{
 				// this is a new Item
 				this.currentItem = new RssFeedItem();
 			}
 			else if ("title".equals(localName))
 			{
 				this.currentItemAttributeToSet = TITLE_ATTRIBUTE;
 			}
 			else if ("link".equals(localName))
 			{
 				this.currentItemAttributeToSet = LINK_ATTRIBUTE;
 			}
 			else if ("description".equals(localName))
 			{
 				this.currentItemAttributeToSet = DESCRIPTION_ATTRIBUTE;
 			}
 			else if ("pubDate".equals(localName))
 			{
 				this.currentItemAttributeToSet = PUBDATE_ATTRIBUTE;
 			}
 			else
 			{
 				this.currentItemAttributeToSet = -1;
 			}
 		}
 
 		public void endElement(String nameSpace, String localName, String qName)
 		{
 			if ("item".equals(localName))
 			{
 				// end of the item: add it to the document
 				this.rssFeedDocument.add(this.currentItem);
 				this.currentItem = null;
 			}
 		}
 
		public void characters(char[] characters, int startIndex, int length)
 		{
			String characters = new String(characters, startIndex, length).trim();
 			String trimedCharacters = characters.replace("\n", "");
 			if (!"".equals(trimedCharacters))
 			{
 				setCurrentItemAttribute(characters);
 			}
 		}
 
 		public RssFeedDocument getDocument()
 		{
 			return this.rssFeedDocument;
 		}
 
 		private void setCurrentItemAttribute(String valueOfTheAttribute)
 		{
 			if (this.currentItem != null && this.currentItemAttributeToSet != -1)
 			{
 				if (this.currentItemAttributeToSet == TITLE_ATTRIBUTE)
 				{
 					this.currentItem.setTitle(valueOfTheAttribute);
 				}
 				else if (this.currentItemAttributeToSet == DESCRIPTION_ATTRIBUTE)
 				{
 					this.currentItem.setDescription(valueOfTheAttribute);
 				}
 				else if (this.currentItemAttributeToSet == LINK_ATTRIBUTE)
 				{
 					this.currentItem.setLink(valueOfTheAttribute);
 				}
 				else if (this.currentItemAttributeToSet == PUBDATE_ATTRIBUTE)
 				{
 					// TODO: USE A DateFormat
 					try
 					{
 						this.currentItem.setPubDate(this.rssFeedDateFormat.parse(valueOfTheAttribute));
 					}
 					catch (ParseException e)
 					{
 						this.currentItem.setPubDate(null);
 						// TODO: ADD A LOG INSTEAD OF SYSTEM.ERR OUTPUT
 						System.err.println("WARNING: publication date <" + valueOfTheAttribute + "> does not follow the expected date format.");
 					}
 				}
 				else
 				{
 					throw new RuntimeException("Error discovered in RssFeedContentHandler: please contact sbrunot@gmail.com");
 				}
 			}
 		}
 	}
 	
 	private URL rssFeedUrl;
 	private DateFormat rssFeedDateFormat;
 	
 	public RssFeedReader(URL rssFeedUrl, DateFormat rssFeedDateFormat)
 	{
 		if (rssFeedUrl == null)
 		{
 			throw new IllegalArgumentException("URL of the RSS feed cannot be null.");
 		}
 		if (rssFeedDateFormat == null)
 		{
 			throw new IllegalArgumentException("Date format for the RSS feed cannot be null.");
 		}
 		this.rssFeedUrl = rssFeedUrl;
 		this.rssFeedDateFormat = rssFeedDateFormat;
 	}
 
 	public RssFeedDocument getRssFeedDocument() throws IOException, SAXException
 	{
 		InputStream rssDocumentInputStream = null;
 		try
 		{
 			rssDocumentInputStream = this.rssFeedUrl.openStream();
 			XMLReader rssDocumentReader = XMLReaderFactory.createXMLReader();
 			RssFeedContentHandler contentHandler = new RssFeedContentHandler(this.rssFeedDateFormat);
 			rssDocumentReader.setContentHandler(contentHandler);
 			rssDocumentReader.parse(new InputSource(rssDocumentInputStream));
 			return contentHandler.getDocument();
 		}
 		finally
 		{
 			if (rssDocumentInputStream != null)
 			{
 				try
 				{
 					rssDocumentInputStream.close();
 				}
 				catch (IOException e)
 				{
 					// do nothing here: it may mask a previous exception ? (to be verified in java spec)
 				}
 			}
 		}
 	}
 }
