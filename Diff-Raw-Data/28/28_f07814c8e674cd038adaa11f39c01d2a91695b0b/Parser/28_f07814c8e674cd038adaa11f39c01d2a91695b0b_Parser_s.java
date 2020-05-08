 package org.eweb4j.spiderman.xml;
 
 import org.eweb4j.util.xml.AttrTag;
 
 
 /**
  * TODO
  * @author weiwei l.weiwei@163.com
  * @date 2013-1-9 下午01:45:36
  */
 public class Parser {
 	
 	@AttrTag
 	private String skipErr;
 	
 	@AttrTag
 	private String xpath;
 	
 	@AttrTag
 	private String attribute;
 	
 	@AttrTag
 	private String exp;
 	
 	@AttrTag
 	private String regex;
 
 	public String getXpath() {
 		return this.xpath;
 	}
 
 	public void setXpath(String xpath) {
 		this.xpath = xpath;
 	}
 
 	public String getAttribute() {
 		return this.attribute;
 	}
 
 	public void setAttribute(String attribute) {
 		this.attribute = attribute;
 	}
 
 	public String getExp() {
 		return this.exp;
 	}
 
 	public void setExp(String exp) {
 		this.exp = exp;
 	}
 
 	public String getRegex() {
 		return this.regex;
 	}
 
 	public void setRegex(String regex) {
 		this.regex = regex;
 	}
 
 	public String getSkipErr() {
 		return this.skipErr;
 	}
 
 	public void setSkipErr(String skipErr) {
 		this.skipErr = skipErr;
 	}
 
 }
