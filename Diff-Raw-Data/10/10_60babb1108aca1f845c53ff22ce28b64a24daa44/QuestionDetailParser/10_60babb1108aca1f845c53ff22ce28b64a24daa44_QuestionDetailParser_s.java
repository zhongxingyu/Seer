 package com.ehb.questiontime;
 
 import java.util.ArrayList;
 
 import org.xml.sax.helpers.DefaultHandler;
 
 public class QuestionDetailParser extends DefaultHandler {
 
 	public ArrayList<Question> questions;
 	private Question tempQuestion;
 	private StringBuilder builder;
 
 	/** XML node keys **/
 	static final String KEY_ROW = "row"; // parent node
 	static final String KEY_DATA = "data";
 	static final String KEY_ID = "ID";
 	static final String KEY_QUESTION = "QUESTION";
 	static final String KEY_ISOPEN = "ISOPEN";
 	static final String KEY_TEACHERID = "TEACHERID";
 	static final String KEY_ISPUSHED = "ISPUSHED";
 
 }
