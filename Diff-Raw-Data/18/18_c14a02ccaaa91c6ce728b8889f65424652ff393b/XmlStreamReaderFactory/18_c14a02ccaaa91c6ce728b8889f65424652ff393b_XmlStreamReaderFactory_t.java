 package com.cee.news.parser.net.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 
 import com.cee.news.parser.net.ReaderFactory;
 import org.apache.commons.io.input.XmlStreamReader;;
 
 public class XmlStreamReaderFactory implements ReaderFactory {
 
 	@Override
 	public Reader createReader(InputStream inputStream, String contentTypeHint, String characterEncodingHint) throws IOException {
		if (characterEncodingHint == null && contentTypeHint == null) {
 			return new XmlStreamReader(inputStream, true);
 		} else {
 			return new XmlStreamReader(inputStream, contentTypeHint, true, characterEncodingHint);
 		}
 	}
 
 }
