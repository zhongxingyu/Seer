 package edu.umass.ciir.trec.parsers;
 
import edu.umass.ciir.trec.types.TRECTopic;
import edu.umass.ciir.trec.types.TRECJudgment;

 import java.io.File;
import java.io.IOException;
 import java.io.FileNotFoundException;
import java.io.BufferedReader;
 import java.io.FileReader;

import java.util.Iterator;
import java.util.Stack;
 import java.util.ArrayList;
import java.util.HashMap;
 import java.util.NoSuchElementException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class TRECBlog08Parser {
 
 	private static Pattern pJudgments = Pattern.compile("(\\d+) (\\d+) (\\S+) (\\d+)");
 	private static Pattern pTopics = Pattern.compile("<top>\\s*<num> Number: (\\d+) </num>\\s*<title>(.+?)</title>\\s*<desc> Description:(.+?)</desc>\\s*<narr> Narrative:(.+?)</narr>\\s*</top>", Pattern.DOTALL);
 
 
 	public TRECBlog08Parser() {}
 
 	private static TRECJudgment parseJudgmentLine(String line) {
 		Matcher m = pJudgments.matcher(line);
 
 		if (m.matches()) {	    
 			return (new TRECJudgment(Integer.parseInt(m.group(1)),
 					Integer.parseInt(m.group(2)),
 					m.group(3),
 					Integer.parseInt(m.group(4))));
 		} else {
 			return null;
 		}	
 	}
 
 	public Iterator<TRECTopic> topicIterator(File source) {
 		return (new TRECTopicIterator(source));
 	}
 
 	public Iterator<TRECJudgment> judgmentIterator(File source) {
 		return (new TRECJudgmentIterator(source));
 	}
 
 	public TRECJudgment[] parseJudgmentFile(File source)
 	throws IOException {
 		String line;
 		int lineno = 0;
 		ArrayList<TRECJudgment> judgments = new ArrayList<TRECJudgment>();
 		BufferedReader br = new BufferedReader(new FileReader(source));
 		while (br.ready()) {
 			line = br.readLine();
 			judgments.add(parseJudgmentLine(line));
 			lineno++;
 		}
 		br.close();
 
 		return (judgments.toArray(new TRECJudgment[0]));
 	}
 
 	public TRECTopic[] parseTopicFile(File source) 
 	throws IOException {
 		ArrayList<TRECTopic> topics = new ArrayList<TRECTopic>();
 		StringBuilder builder = new StringBuilder();
 		FileReader reader = new FileReader(source);
 		char[] buffer = new char[1024];
 		int counter;
 
 		while ((counter = reader.read(buffer)) != -1) {
 			builder.append(buffer, 0, counter);
 		}
 		reader.close();
 		Matcher m = pTopics.matcher(builder.toString());
 
 		while (m.find()) {
 			topics.add(new TRECTopic(Integer.parseInt(m.group(1)), m.group(2), 
 					m.group(3), m.group(4)));
 		}
 
 		return (topics.toArray(new TRECTopic[0]));
 	}
 
 	public class TRECTopicIterator implements Iterator<TRECTopic> {
 		private File source;
 		private Matcher m;
 		private boolean attempted;
 		private boolean cached;
 
		public TRECTopicIterator(File source) {
 			attempted = cached = false;
 			try {
 				StringBuilder builder = new StringBuilder();	    
 				FileReader reader = new FileReader(source);
 				char[] buffer = new char[1024];
 				int counter;
 
 				while ((counter = reader.read(buffer)) != -1) {
 					builder.append(buffer, 0, counter);
 				}
 				reader.close();
 				m = pTopics.matcher(builder.toString());
 			} catch (IOException ioe) {
 				m = null;
 			}
 		}
 
 		public boolean hasNext() {
 			if (!attempted) {
 				cached = m.find();
 				attempted = true;
 			}
 			return cached;
 		}
 
 		public TRECTopic next() throws NoSuchElementException {
 			TRECTopic element;
 			if (!attempted) {
 				if (!m.find()) throw new NoSuchElementException();
 			}
 			element = new TRECTopic(Integer.parseInt(m.group(1)), m.group(2), 
 					m.group(3), m.group(4));
 			attempted = false;
 			return element;
 		}
 
 		/**
 		 * Not implemented as this is a read-only iterator.
 		 */
 		public void remove() {}
 	}
 
 	public class TRECJudgmentIterator implements Iterator<TRECJudgment> {
 
 		private File source;
 		private BufferedReader br;
 
 		public TRECJudgmentIterator(File s) {
 			try {
				br = new BufferedReader(new FileReader(s));
 			} catch (FileNotFoundException fnfe) {
 				br = null;
 			}	    
 		}
 
 		public boolean hasNext() {
 			try {
 				return ((br == null) ? false : br.ready());
 			} catch (IOException ioe) {
 				return false;		
 			}
 		}
 
 		public TRECJudgment next() throws NoSuchElementException {
 			TRECJudgment element;
 
 			try {
 
 				if ((br == null) || (br.ready() == false)) {
 					throw new NoSuchElementException();
 				} else {
 					element = TRECBlog08Parser.parseJudgmentLine(br.readLine());
 				}
 
 				if (br.ready() == false) {
 					br.close();
 					br = null;
 				}
 				return element;
 			} catch (IOException ioe) {
 				throw new NoSuchElementException(ioe.getMessage());
 			}
 		}
 
 		/**
 		 * Not implemented as this is a read-only iterator.
 		 */
 		public void remove() {}
 	}
 
 	public static void main(String argv[]) throws IOException {
 		TRECBlog08Parser parser = new TRECBlog08Parser();
 
 		if (argv.length < 2) {
 			System.err.println("Usage: TRECBlog08Parser [topic|judgment] <input file>");
 			System.exit(-1);
 		}
 
 		File source = new File(argv[1]);
 		Object[] elements;
 
 		if ("topic".equals(argv[0])) {
 			System.err.println("Parsing topic file: " + argv[1]);
 			TRECTopic[] topics = parser.parseTopicFile(source);
 			elements = (Object[]) topics;
 		} else {
 			System.err.println("Parsing judgment file: " + argv[1]);
 			TRECJudgment[] judgments = parser.parseJudgmentFile(source);
 			elements = (Object[]) judgments;
 		}
 
 		for (int i = 0; i < elements.length; i++) {
 			System.err.println(elements[i].toString());
 		}
 	}   
 }
