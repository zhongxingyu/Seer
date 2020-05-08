 package com.lab_programming.util;
 
 import java.util.Enumeration;
 import java.util.NoSuchElementException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 public class RegularTokenizer implements Enumeration<String> {
 
 	private final String str;
 	private Matcher matcher;
 	private Pattern delim;
 	private final boolean returnDelim;
 	private int currentPosistion = 0;
 	private int maxPosistion;
 
 	public RegularTokenizer(final String str, final String regex,
 			final boolean returnDelims) throws NullPointerException,
 			PatternSyntaxException {
 		if (str == null)
			throw new NullPointerException("str is null");
 		this.str = str;
 		returnDelim = returnDelims;
 		assignNewRegex(regex);
 	}
 
 	public RegularTokenizer(final String str, final String regex)
 			throws NullPointerException, PatternSyntaxException {
 		this(str, regex, false);
 	}
 
 	public RegularTokenizer(final String str) throws NullPointerException {
 		this(str, "\\s", false);
 	}
 
 	private void assignNewRegex(final String regex)
 			throws NullPointerException, PatternSyntaxException {
 		if (regex == null)
			throw new NullPointerException("regex is null");
 		delim = Pattern.compile(regex);
 		matcher = delim.matcher(str);
 		if (!returnDelim) {
 			while (matcher.find()) {
				if (matcher.end() >= str.length()) {
 					maxPosistion = matcher.start();
 				} else {
 					maxPosistion = str.length();
 				}
 			}
 		} else {
 			maxPosistion = str.length();
 		}
 		matcher.reset();
 	}
 
 	public boolean hasMoreTokens() {
 		return currentPosistion < maxPosistion;
 	}
 
 	public String nextToken() throws NoSuchElementException {
 		while (true) {
 			if (!matcher.find(currentPosistion)) {
 				if (!hasMoreTokens())
					throw new NoSuchElementException("You are at the end of this tokenizer");
 				final String ret = str.substring(currentPosistion);
 				currentPosistion = maxPosistion;
 				return ret;
 			}
 			if (matcher.start() != currentPosistion) {
 				final String ret = str.substring(currentPosistion,
 						matcher.start());
 				currentPosistion = matcher.start();
 				return ret;
 			} else {
 				currentPosistion = matcher.end();
 				if (returnDelim)
 					return matcher.group();
 			}
 		}
 	}
 
 	public String nextToken(final String regex) throws NoSuchElementException,
 			NullPointerException, PatternSyntaxException {
 		if (currentPosistion >= str.length())
 			throw new NoSuchElementException();
 		assignNewRegex(regex);
 		return nextToken();
 	}
 
 	@Override
 	public boolean hasMoreElements() {
 		return hasMoreTokens();
 	}
 
 	@Override
 	public String nextElement() throws NoSuchElementException {
 		return nextToken();
 	}
 	
 	public int countTokens() {
 		int curPos = currentPosistion;
 		int i = 0;
 		while(matcher.find(curPos)){
 			if(curPos != matcher.start()) {
 				curPos = matcher.start();
 				i++;
 			}else if(returnDelim) {
 				curPos = matcher.end();
 				i++;
 			}else {
 				curPos = matcher.end();
 			}
 		}
 		if(curPos < maxPosistion) i++;
 		return i;
 	}
 
 }
