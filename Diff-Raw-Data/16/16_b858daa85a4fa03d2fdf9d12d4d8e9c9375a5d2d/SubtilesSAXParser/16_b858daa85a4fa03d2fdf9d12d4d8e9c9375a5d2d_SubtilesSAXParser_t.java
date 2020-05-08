 /**
  * Copyright (c) 2012, Minimalgap
 
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
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  * 
  **/
 
 package com.minimalgap.transformer.subtitles;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.log4j.Logger;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.minimalgap.transformer.subtitles.model.Subtitle;
 import com.minimalgap.transformer.subtitles.utils.TimeUtils;
 
 public class SubtilesSAXParser extends DefaultHandler {
 	private Logger logger = Logger.getLogger(this.getClass());
 
 	List<Subtitle> subtitles;
 
 	private Integer count = 0;
 	private String timebase;
 	private Subtitle aSubtitle;
 
 	private boolean rootElement = false;
 	private StringBuilder tempVal;
 	private boolean textParent = false;
 	private String destinationPath;
 
 	public SubtilesSAXParser(String destinationPath){
 		this.subtitles = new ArrayList<Subtitle>();
 		this.destinationPath = destinationPath;
 	}
 
 	/**
 	 * Create instance of SAX and parse file
 	 * @param sourcePath - source file path
 	 * @throws SAXException 
 	 * @throws ParserConfigurationException 
 	 * @throws IOException 
 	 */
 	public synchronized void handleSubtitleTransformation(String sourcePath) throws ParserConfigurationException, SAXException, IOException {		
 		//get a factory
 		SAXParserFactory spf = SAXParserFactory.newInstance();
 
 			//get a new instance of parser
 			SAXParser sp = spf.newSAXParser();
 			//parse the file and also register this class for call backs
 			sp.parse(sourcePath, this);
 	}
 
 	/**
 	 * Iterate through the list of subtitles and print to file
 	 * format is:
 	 * id
 	 * startTime --> endTime
 	 * text
 	 * \n
 	 * @param destinationPath - .srt file
 	 */
 	private void printSubtitlesList(String destinationPath){
 		try{
 			// Create file 
 			FileWriter fstream = new FileWriter(destinationPath);
 			BufferedWriter out = new BufferedWriter(fstream);
 			for (Subtitle subtitle : subtitles) {
 				out.write(subtitle.getId()+"");
 				out.newLine();
 				out.write(subtitle.getStartTime() + " --> " + subtitle.getEndTime());
 				out.newLine();
 				out.write(new String(subtitle.getText().getBytes(),Charset.forName("UTF-8")) );
 				out.newLine();
 				out.newLine();
 				out.flush();
 			}
 			//Close the output stream
 			out.close();
 		}catch (Exception e){
 			logger.info("Error: " + e.getMessage());
 		}
 	}
 
 	private String formatTime(String time) {
 		Double milliseconds = new Double (new Double(time) / new Double(timebase));
 		// integer part
 		long seconds = (long) milliseconds.doubleValue();
 		// fractional part
 		long fractional = (long) ((milliseconds.doubleValue() - seconds) * 1000);
 
 		return TimeUtils.convertSecondsInTime(seconds) + ","+ fractional;
 	}
 
 	//Event Handlers
 	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
 		tempVal = new StringBuilder();
 		if(qName.equalsIgnoreCase("generatoritem")) {
 			aSubtitle = new Subtitle();
 			aSubtitle.setId(++count);
 			aSubtitle.setText(attributes.getValue("type"));
 		}
 		// check if dealing with correct type of xml
 		// ROOT is sequence || xeml 
 		else if (!rootElement) {
 			if (! (qName.equalsIgnoreCase("sequence") || qName.equalsIgnoreCase("xmeml")) ) {
 				throw new SAXParseException("Wrong root element", null);
 			}
 			rootElement = true;
 		}
 	}
 
 	public void endElement(String uri, String localName, String qName) throws SAXException {
 		if(qName.equalsIgnoreCase("generatoritem")) {
 			//add it to the list
			subtitles.add(aSubtitle);
			// invalid subtitle element 
			aSubtitle = null;
 		} else if (qName.equalsIgnoreCase("timebase")) {
 			timebase = tempVal.toString();
 		} else if (qName.equalsIgnoreCase("start")) {
			if (aSubtitle != null) {
				aSubtitle.setStartTime(this.formatTime(tempVal.toString()));
			}
 		} else if (qName.equalsIgnoreCase("end")) {
			if (aSubtitle != null) {
				aSubtitle.setEndTime(this.formatTime(tempVal.toString()));
			}
 		} else if (qName.equalsIgnoreCase("parameterid")) {
 			if(tempVal.toString().equalsIgnoreCase("str")){
 				textParent = true; // flag for text "value"
 			}
 		} else if (qName.equalsIgnoreCase("value")) {
 			if(textParent){
 				aSubtitle.setText(tempVal.toString());
 				textParent = false;
 			}
 		}
		
 		tempVal = new StringBuilder();
 	}
 
 	public void characters(char ch[], int start, int length) throws SAXException {
 		tempVal.append(new String(ch, start, length));
 	}
 
 	public void endDocument () throws SAXException {
 		this.printSubtitlesList(destinationPath);
 	}
 }
 
 
 
 
