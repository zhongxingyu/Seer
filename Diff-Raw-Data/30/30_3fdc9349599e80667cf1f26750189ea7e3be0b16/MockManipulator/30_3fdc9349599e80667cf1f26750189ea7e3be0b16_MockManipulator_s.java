 package com.ontometrics.scraper.extraction;
 
 import net.htmlparser.jericho.Source;
 
 public class MockManipulator extends Manipulator {
 
 	public Source getResult() {
 		return getSource();
 	}
 
 	@Override
 	public String performExtraction() {
		return null;
 	}
 
 	@Override
 	public void setMatcher(String matcher) {
 
 	}
 
 }
