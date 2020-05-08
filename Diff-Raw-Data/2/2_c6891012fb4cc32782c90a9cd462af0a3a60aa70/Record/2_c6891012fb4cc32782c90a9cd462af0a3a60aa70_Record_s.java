 package com.sunshine;
 
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import org.xml.sax.Attributes;
 
 //Represents a while XML file
 public class Record extends LinkedList<Record.Section> {
 	private static final long serialVersionUID = 1L;
 	
 	public String name;
 	
 	public Record(Attributes atts) {
 		this.name = atts.getValue("name");
 	}
 	
 	public static boolean isRecord(String qName) {
 		return "record".equalsIgnoreCase(qName);
 	}
 	
 	//Should be unnecessary now that html displays in utf-8
 	//Used to get rid of MSWord chatacters
 	public static String removeSpecialChars(String s) {
 		if (s == null) return null;
 		return s;
 //		return s.replace("”", "\"")
 //		.replace("“", "\"")
 //		.replace("’", "'")
 //		.replace("‘", "'")
 //		.replace("–", "-")
 //		.replace("-", "-");
 	}
 	
 	//A section of the XML file (or a group of buttons in the app)
 	public static class Section extends LinkedList<Record.Header> {
 		private static final long serialVersionUID = 1L;
 		
 		public String title;
 		
 		public Section(Attributes atts) {
 			this.title = removeSpecialChars(atts.getValue("title"));
 		}
 		
 		public static boolean isSection(String qName) {
 			return "section".equalsIgnoreCase(qName);
 		}
 	}
 	
 	//A single button/QnA group
 	public static class Header implements Serializable, Iterable<Question> {
 		private static final long serialVersionUID = 1L;
 	
 		//We contain a LinkedList instead of extending it because
 		//when Android parcels LL's it uses another representation
 		//which loses any data in the subclass
 		public LinkedList<Question> questions = new LinkedList<Record.Question>();
 	
 		public String title;
 		public String tip;
 		
 		public Header(Attributes atts) {
 			this.title = removeSpecialChars(atts.getValue("title"));
 			//special italicized "tip" that appear as the top
 			//of the qna list
 			this.tip = removeSpecialChars(atts.getValue("tip"));
 		}
 		
 		public Header(String title) {
 			this.title = title;
 		}
 		
 		//LinkedList wrapping methods
 		
 		public Question get(int index) {
 			return questions.get(index);
 		}
 		
 		public int size() {
 			return questions.size();
 		}
 		
 		public void add(Question question) {
 			questions.add(question);
 		}
 
 		@Override
 		public Iterator<Question> iterator() {
 			return questions.iterator();
 		}
 		
 		
 		//Add a <q> or <an>
 		public void addElement(String qName, Attributes atts, String body, boolean containsHTML) {
 			if (isQuestion(qName)) {
 				add(new Question(this, removeSpecialChars(body), atts));
 			} else if (isAnswer(qName)) {
 				get(size() - 1).answer = removeSpecialChars(body);
 				get(size() - 1).containsHTML = containsHTML;
 			}
 		}
 		
 		public static boolean isHeader(String qName) {
 			return "header".equalsIgnoreCase(qName);
 		}
 		
 		public static boolean isHeaderElement(String qName) {
 			return isQuestion(qName) || isAnswer(qName);
 		}
 		
 		public static boolean isQuestion(String qName) {
 			return "q".equalsIgnoreCase(qName);
 		}
 		
 		public static boolean isAnswer(String qName) {
 			return "an".equalsIgnoreCase(qName);
 		}
 		
 		public static boolean isListItem(String qName) {
 			return  qName.equalsIgnoreCase("li");
 		}
 	}
 	
	//Represents a singe question and answer
 	public static class Question implements Serializable {
 		private static final long serialVersionUID = 1L;
 		public String question, answer, anchor;
 		public boolean containsHTML;
 		public Header parent;
 		
 		public Question(Header parent, String question, Attributes atts) {
 			this.parent = parent;
 			this.question = question;
 			this.answer = "";
 			this.anchor = atts.getValue("anchor");
 		}
 		
 	}
 }
