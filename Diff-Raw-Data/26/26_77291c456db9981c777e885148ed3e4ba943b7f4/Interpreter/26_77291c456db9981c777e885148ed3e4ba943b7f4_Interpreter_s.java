 /*
  *  Copyright 2012 Matti Vesa
  *
  *  Licensed under the Apache License, Version 2.0 (the "License"); you
  *  may not use this file except in compliance with the License. You may
  *  obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  *  implied. See the License for the specific language governing
  *  permissions and limitations under the License.
  */
 package com.github.mjvesa.f4v;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.ListIterator;
 import java.util.Set;
 import java.util.Stack;
 
 import com.github.mjvesa.f4v.wordset.ArithmeticWordSet;
 import com.github.mjvesa.f4v.wordset.FlowControlWordSet;
 import com.github.mjvesa.f4v.wordset.HeapWordSet;
 import com.github.mjvesa.f4v.wordset.InterpreterWordSet;
 import com.github.mjvesa.f4v.wordset.MiscWordSet;
 import com.github.mjvesa.f4v.wordset.SQLWordSet;
 import com.github.mjvesa.f4v.wordset.StackWordSet;
 import com.github.mjvesa.f4v.wordset.StringWordSet;
 import com.github.mjvesa.f4v.wordset.VaadinWordSet;
 import com.vaadin.ui.ComponentContainer;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.UI;
 
 /**
  * Core interpreter. The outer interpreter reads words from the input stream
  * (source file) and parses them into executable words. The inner interpreter
  * simply executes defined words by executing their constituent words in
  * sequence.
  * 
  * Words execute themselves, which allows for WordSets that can be installed
  * dynamically. Defined words are executed in the interpreter, which is called
  * by the defined words execute() method.
  * 
  * Dictionary and heap space are separate.
  * 
  * @author mjvesa@vaadin.com
  * 
  */
 public class Interpreter implements Presenter {
 
 	private HashMap<String, Word> dictionary;
 	private HashMap<String, String> source;
 	private Stack<Object> dataStack;
 	private Stack<Integer> returnStack;
 	private Stack<CompiledWord[]> codeStack;
 	private Object[] heap;
 	private DefinedWord currentDefinition; // These two are used to define new
 											// words
 	private ArrayList<CompiledWord> currentDefinitionWords;
 	private boolean isCompiling;
 
 	// ip and code represent the defined word currently being executed
 	private int ip;
 	private CompiledWord[] code;
 
 	private Stack<Parser> parsers;
 	private Parser parser;
 	private ComponentContainer mainComponentContainer;
 	private Blocks blocks;
 	private SQL sql;
 	private boolean logNewWords;
 	private boolean logExecutedWords;
 	
 	private View view;
 
 	/**
 	 * Default constructor
 	 */
 	public Interpreter() {
 		logNewWords = false;
 		logExecutedWords = false;
 	}
 
 	/**
 	 * Does actual setting up of the interpreters.
 	 */
 	public void setup() {
 		setUpStorage();
 		installWordSets();
 		loadBuffers();
 
 		sql = new SQL();
 		sql.setDictionary(dictionary);
 		sql.setHeap(heap);
 	}
 
 
 
 	private void loadBuffers() {
 		blocks = new Blocks();
 		source = blocks.loadBuffers();
 	}
 
 	public Set<String> getWordList() {
 		return dictionary.keySet();
 	}
 
 	public Set<String> getBufferList() {
 		return source.keySet();
 	}
 
 	/**
 	 * Initializes stacks, dictionary and heap
 	 */
 	private void setUpStorage() {
 		dataStack = new Stack<Object>();
 		returnStack = new Stack<Integer>();
 		codeStack = new Stack<CompiledWord[]>();
 		heap = new Object[2000];
 		dictionary = new HashMap<String, Word>();
 		parsers = new Stack<Parser>();
 	}
 
 	/**
 	 * Installs all the built-in wordsets.
 	 */
 	private void installWordSets() {
 
 		new ArithmeticWordSet().install(this);
 		new FlowControlWordSet().install(this);
 		new HeapWordSet().install(this);
 		new InterpreterWordSet().install(this);
 		new MiscWordSet().install(this);
 		new SQLWordSet().install(this);
 		new StackWordSet().install(this);
 		new StringWordSet().install(this);
 		new VaadinWordSet().install(this);
 	}
 
 	public void runBuffer(String command) {
 		try {
 			dataStack.clear();
 			returnStack.clear();
 			interpret(command);
 			print("OK");
 		} catch (Exception e) {
 			print("ERROR: " + e.getClass().toString() + " -- Current word: "
 					+ code[ip].getName() + " Location of error: line: "
 					+ parser.getLine() + " column: " + parser.getCol());
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Outer interpreter.
 	 * 
 	 * @param str
 	 */
 	public void interpret(String str) {
 
 		parsers.push(parser);
 		isCompiling = false;
 		parser = new Parser();
 		parser.setString(str);
 
 		String word = parser.getNextWord();
 
 		while (!word.isEmpty()) {
 
 			if (isCompiling) {
 				compileWord(word);
 			} else {
 
 				if (dictionary.containsKey(word)) {
 					Word w = dictionary.get(word);
 					logExecutedWord(w);
 					w.execute(this);
 				} else {
 					if (word.charAt(0) == '"') {
 						dataStack.push(word.substring(1, word.length() - 1));
 					} else if (Character.isDigit(word.charAt(0))) {
 						dataStack.push(Integer.valueOf(word));
 					} else {
 						print("INTERPRETER ERROR: Could not resolve: " + word
 								+ " " + parser.getPosition());
 					}
 				}
 			}
 			word = parser.getNextWord();
 		}
 
 		parser = parsers.pop();
 	}
 
 	/**
 	 * Takes a word and finds its definition from the dictionary and compiles a
 	 * reference to it into the current definition.
 	 * 
 	 * @param word
 	 */
 	private void compileWord(String word) {
 
 		if (dictionary.containsKey(word)) {
 			Word w = dictionary.get(word);
 			if (w.isImmediate()) {
 				w.execute(this);
 			} else {
 				currentDefinitionWords.add(new CompiledWord(w));
 			}
 		} else {
 			if (word.charAt(0) == '"') {
 				generateLiteral(word.substring(1, word.length() - 1));
 			} else if (Character.isDigit(word.charAt(0))) {
 				generateLiteral(Integer.valueOf(word));
 			} else {
 				print("COMPILER ERROR: Could not resolve: \"" + word + "\" "
 						+ parser.getPosition());
 			}
 		}
 
 	}
 
 	/**
 	 * 
 	 * Inner interpreter
 	 * 
 	 * Execution:
 	 * 
 	 * Base words are atomic, so just fetch and execute. Defined words consist
 	 * of other words. Obtain list of words and execute in series. Store
 	 * previous point in return stack so there will be no problems with nested
 	 * calls.
 	 */
 	public void execute(Word word) {
 
 		if (word == null) {
			Notification.show("Attempted to excute undefined word!");
 			return;
 			// TODO return to main loop or stop interpreting when this happens.
 		}
 		logExecutedWord(word);
 
 		word.execute(this);
 
 	}
 
 	/**
 	 * Executes a defined word. This is done in the Interpreter so that the
 	 * innards of the defined word are exposed.
 	 * 
 	 * @param word
 	 */
 	public void executeDefinedWord(DefinedWord definedWord) {
 		returnStack.push(ip);
 		codeStack.push(code);
 		code = definedWord.getCode();
 		ip = 0;
 		while (ip < code.length) {
 			logExecutedWord(code[ip]);
 			code[ip].execute(this);
 			ip++;
 		}
 		ip = returnStack.pop();
 		code = codeStack.pop();
 	}
 
 	private void logExecutedWord(Word word) {
 		if (logExecutedWords) {
 			print("Executing: " + word.getName());
 		}
 	}
 
 	public Word getNextWord() {
 
 		String s = getParser().getNextWord();
 		Word word = getDictionary().get(s);
 		if (word == null) {
 			print("The word \"" + s + "\" has not been defined!");
 			return null;
 		}
 		return word;
 	}
 
 	/* This can be used to skip filling words */
 	public String getNextNonNopWord() {
 
 		String word = parser.getNextWord();
 		while (!word.isEmpty()) {
 			Word w = dictionary.get(word);
 			if (w instanceof DefinedWord
 					&& ((DefinedWord) w).getCode().length > 0) {
 				return word;
 			}
 			word = parser.getNextWord();
 		}
 		return null;
 	}
 
 	/* Generates a word which pushes a literal onto the stack */
 	public void generateLiteral(Object value) {
 		Word w = dictionary.get("literal");
 		CompiledWord cw = new CompiledWord(w, value);
 		currentDefinitionWords.add(cw);
 	}
 
 	public void create() {
 		createNewWord(parser.getNextWord());
 	}
 
 	public void createFromStack() {
 		createNewWord((String) dataStack.pop());
 	}
 
 	public void anonCreate() {
 		createNewWord("_anonymous_");
 		dataStack.push(currentDefinition);
 	}
 
 	private void createNewWord(String name) {
 		if (isCompiling) {
 			// TODO is this the right thing to do? "Specs" seem kinda vague
 			finishCompilation();
 		}
 		currentDefinition = new DefinedWord();
 		view.addNewWord(name);
 		currentDefinition.setName(name);
 		dictionary.put(name, currentDefinition);
 		currentDefinitionWords = new ArrayList<CompiledWord>();
 		isCompiling = true;
 	}
 
 	/*
 	 * Used to finish compilation of either colon definitions or stuff made with
 	 * CREATE. Or anonymous colon definition.
 	 */
 	public void finishCompilation() {
 		String name = currentDefinition.getName();
 		currentDefinition.setCode(currentDefinitionWords
 				.toArray(new CompiledWord[0]));
 		if (logNewWords) {
 			print("ADDED: " + name);
 		}
 		isCompiling = false;
 	}
 
 	public void printStack() {
 		ListIterator<Object> iterator = dataStack.listIterator();
 		while (iterator.hasNext()) {
 			Object o = iterator.next();
 			print("Class: " + o.getClass() + " -- [" + o.toString() + "]");
 		}
 	}
 
 	public void print(String msg) {
 		view.print(msg);
 	}
 
 
 	public void addSource(String name, String code) {
 
 		blocks.saveBuffer(name, code);
 		source.put(name, code);
 
 	}
 
 	public String getSource(String value) {
 		return source.get(value);
 	}
 
 	public void continueExecution() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void setLogNewWords(boolean b) {
 		logNewWords = b;
 	}
 
 	public void setLogExecutedWords(boolean b) {
 		logExecutedWords = b;
 	}
 
 	public HashMap<String, Word> getDictionary() {
 		return dictionary;
 	}
 
 	public Object popData() {
 		return dataStack.pop();
 	}
 
 	public void pushData(Object obj) {
 		dataStack.push(obj);
 	}
 
 	public Object peekData() {
 		return dataStack.peek();
 	}
 
 	public Integer peekReturn() {
 		return returnStack.peek();
 	}
 	
 	public Integer popReturn() {
 		return returnStack.pop();
 	}
 
 	public void pushReturn(Integer value) {
 		returnStack.push(value);
 	}
 
 	public int getIp() {
 		return ip;
 	}
 
 	public void setIp(int ip) {
 		this.ip = ip;
 	}
 
 	public Word getExecutedWord() {
 		return code[ip];
 	}
 
 	public int getCurrentDefinitionSize() {
 		return currentDefinitionWords.size();
 	}
 
 	public CompiledWord getFromCurrentDefinition(int index) {
 		return currentDefinitionWords.get(index);
 	}
 
 	public void addToCurrentDefinition(CompiledWord word) {
 		currentDefinitionWords.add(word);
 
 	}
 
 	public CompiledWord[] getCode() {
 		return code;
 	}
 
 	public Object peek(int index) {
 		return heap[index];
 	}
 
 	public void poke(int index, Object value) {
 		heap[index] = value;
 	}
 
 	public void setCompiling(boolean b) {
 		isCompiling = b;
 	}
 
 	public Parser getParser() {
 		return parser;
 	}
 
 	public DefinedWord getCurrentDefinition() {
 		return currentDefinition;
 	}
 
 	public Object getCurrentParam() {
 		return ((CompiledWord) getExecutedWord()).getParameter();
 	}
 
 	public SQL getSQL() {
 		return sql;
 	}
 
 	public ComponentContainer getMainPanel() {
 		return view.getMainComponentContainer();
 	}
 
 	public void setView(View view) {
 		this.view = view;
 	}
 	
 	public View getView() {
 		return this.view;
 	}
 
 }
