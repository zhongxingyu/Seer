 package edu.uwsp.lucene;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 
 public class LuceneSaxParser extends DefaultHandler {
 
 	private String startTag;
 	private Document doc;
 	
 	public LuceneSaxParser() {
 		doc = new Document();
 	}
 	
 	public Document getDoc()
 	{
 		return doc;
 	}
 
 	@Override
 	public void characters(char[] ch, int start, int length)
 			throws SAXException {
 		String printS = "";
     	int i = start;
     	while (i < start + length) {
     		if(ch[i] != 10 && ch[i] != 32)
     			printS += ch[i];
     		i++;
 		}
     	printS.trim();
     	if(printS.length()>0)
     		//System.out.print(" | tag:" + startTag + ": " + printS);
    		//Field.Store.YES, Field.Index.ANALYZED
    		doc.add(new Field(startTag, printS, Field.Store.YES, Field.Index.ANALYZED));
 	}
 
 	@Override
 	public void startElement(String uri, String localName, String qName,
 			Attributes attributes) throws SAXException {
 		super.startElement(uri, localName, qName, attributes);
 		startTag = localName;
 	}
 
 	public void clearDoc() {
 		doc = new Document();
 	}
 
 }
