 /*
  * Copyright 2011 Tomas Schlosser
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.anadix.swingparser;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Properties;
 
 import javax.swing.text.MutableAttributeSet;
 import javax.swing.text.html.HTML.Tag;
 import javax.swing.text.html.HTMLEditorKit.ParserCallback;
 
 import org.anadix.html.HTMLElementFactory;
 import org.anadix.html.Position;
 import org.apache.log4j.Logger;
 
 
 /**
  * Callback class that handles the events from swingparser
  *
  * @author tomason
  * @version $Id: $
  */
 public class StatefulParserCallback extends ParserCallback {
 	private static final Logger logger = Logger.getLogger(StatefulParserCallback.class);
 	private static final BigInteger JUMP = BigInteger.valueOf(100L);
 	private static final String ENTRY_POINT = "parser";
 
 	private final HTMLElementFactory factory;
 	private final String source;
 
 	private final int[] lines;
 
 	private BigInteger ID = BigInteger.ZERO;
 
 	/**
 	 * Constructor
 	 *
 	 * @param factory instance of HTMLElementFactory used for inserting events
 	 */
 	public StatefulParserCallback(HTMLElementFactory factory) {
 		this(factory, null);
 	}
 
 	/**
 	 * Constructor
 	 *
 	 * @param factory instance of HTMLElementFactory used for inserting events
 	 * @param source HTML source code
 	 */
 	public StatefulParserCallback(HTMLElementFactory factory, String source) {
 		this.factory = factory;
 		this.source = source;
 
 		factory.setGlobal("jump", JUMP);
 		this.lines = countLines(this.source);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos) {
 		ID = ID.add(BigInteger.ONE);
 
 		TagEvent e = new SimpleTagEvent(
 				ID, t.toString(), parseAttributes(a), createPosition(pos),
 				getSource(t.toString(), pos));
 
 		if (e.getSource() != null && e.getSource().replace(" ", "").length() == e.getTagName().length() + 3) {
 			logger.warn("end simple tag: " + e.getSource());
 		} else {
 			factory.insertEvent(ENTRY_POINT, e);
 		}
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void handleStartTag(Tag t, MutableAttributeSet a, int pos) {
 		ID = ID.add(BigInteger.ONE);
 
 		TagEvent e = new StartTagEvent(
 				ID, t.toString(), parseAttributes(a), createPosition(pos),
 				getSource(t.toString(), pos));
 
 		if (e.getSource() != null && e.getSource().replace(" ", "").length() == e.getTagName().length() + 3) {
 			logger.warn("end simple tag: " + e.getSource());
 		} else {
 			factory.insertEvent(ENTRY_POINT, e);
 		}
 
 		ID = ID.multiply(JUMP);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void handleEndTag(Tag t, int pos) {
 		ID = ID.divide(JUMP);
 
 		TagEvent e = new EndTagEvent(ID, t.toString().toLowerCase(), createPosition(pos));
 
 		factory.insertEvent(ENTRY_POINT, e);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void handleText(char[] data, int pos) {
 		TextContentEvent e = new TextContentEvent(ID.divide(JUMP), new String(data), pos);
 
 		factory.insertEvent(ENTRY_POINT, e);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void handleError(String errorMsg, int pos) {
 		logger.error(errorMsg);
 	}
 
 	private static int[] countLines(String source) {
 		if (source == null) {
 			return null;
 		}
 		List<Integer> list = new ArrayList<Integer>();
 
 		int pos = 0;
 		while((pos = source.indexOf('\n', pos)) != -1) {
 			list.add(pos);
 			pos++;
 		}
 
 		int[] result = new int[list.size()];
 		for (int i = 0; i < list.size(); i++) {
 			result[i] = list.get(i);
 		}
 
 		return result;
 	}
 
 	private Properties parseAttributes(MutableAttributeSet attributes) {
 		Properties result = new Properties();
 		Enumeration<?> names = attributes.getAttributeNames();
 
 		Object name;
 		while (names.hasMoreElements()) {
 			name = names.nextElement();
 
 			result.setProperty(
 					name.toString().toLowerCase(),
 					attributes.getAttribute(name).toString().toLowerCase()
 					);
 		}
 
 		return result;
 	}
 
 	private String getSource(String tagName, int position) {
 		if (source == null) {
 			return null;
 		}
 
 		int beginIndex = source.substring(0, position + tagName.length()).lastIndexOf("<"/* + tagName*/);
 		int endIndex = source.indexOf(">", beginIndex);
 
 		if (beginIndex > -1 && endIndex > -1) {
 			String result = source.substring(beginIndex, endIndex + 1);
 			result = result.replace("\r", "\n");
 			result = result.replace("\n", " ");
 			result = result.replace("\t", " ");
 
 			while (result.contains("  ")) {
 				result = result.replace("  ", " ");
 			}
 
 			return result;
 		}
 
 		logger.error(String.format("Could not find tag %s starting at %s", tagName, position));
 		return "!" + tagName + "(" + beginIndex + "," + endIndex + ") - " + source.substring(position, position + 50);
 	}
 
 	private Position createPosition(int position) {
 		if (lines == null || lines.length < 1) {
 			return new Position(position);
 		} else {
 			int line = 0;
 			int col = 0;
 
 			int i = 0;
 			while (position > lines[i++]);
 			line = i;						// keep i+1 => lines start from 1
			col = position - lines[i - 2];  // but let's subtract from previous line
 
 			return new Position(line, col);
 		}
 	}
 }
