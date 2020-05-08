 package org.rubypeople.rdt.internal.codeassist;
 
 import org.rubypeople.rdt.core.IRubyScript;
 import org.rubypeople.rdt.core.RubyModelException;
 
 public class CompletionContext {
 	
 	private IRubyScript script;
 	private int offset;
 	private boolean isMethodInvokation = false;
 	private String correctedSource;
 	private String partialPrefix;
 	private String fullPrefix;
 	private int replaceStart;
 
 	public CompletionContext(IRubyScript script, int offset) throws RubyModelException {
 		this.script = script;
 		if (offset < 0)
 			offset = 0;
 		this.offset = offset;
 		replaceStart = offset + 1;
 		run();
 	}
 	
 	private void run() throws RubyModelException {
 		StringBuffer source = new StringBuffer(script.getSource());
 		// Read from offset back until we hit a: space, period
 		// if we hit a period, use character before period as offset for
 		// inferrer
 		StringBuffer tmpPrefix = new StringBuffer();
 		for (int i = offset; i >= 0; i--) {
 			char curChar = source.charAt(i);
 			if (offset == i) { // check the first character
 				switch (curChar) {
 				case '.': // if it breaks syntax, lets fix it
 				case '$':
 				case '@':
 					// TODO What if there is a valid character after this, so syntax isn't broken?
 					source.deleteCharAt(i);
 					break;
 				}
 			}
 			if (curChar == '.') {
 				isMethodInvokation = true;
 				offset = i - 1;
 				if (partialPrefix == null) this.partialPrefix = tmpPrefix.toString();
 			}
			if (Character.isWhitespace(curChar)) {
 				offset = i + 1;
 				break;
 			}
 			tmpPrefix.insert(0, curChar);
 		}
 		this.fullPrefix = tmpPrefix.toString();
 		if (partialPrefix == null)
 			partialPrefix = fullPrefix;
 		if (partialPrefix != null)
 			replaceStart -= partialPrefix.length();
 		this.correctedSource = source.toString();
 	}
 	
 	public boolean isMethodInvokation() {
 	  return isMethodInvokation;	
 	}
 	
 	/**
 	 * The last portion of prefix is not null, not empty and starts with an uppercase letter
 	 * @return
 	 */
 	public boolean isConstant() {
 		return getPartialPrefix() != null && getPartialPrefix().length() > 0 && Character.isUpperCase(getPartialPrefix().charAt(0));
 	}
 	
 	public int getReplaceStart() {
 		return replaceStart;
 	}
 	
 	/**
 	 * Modified source which should not fail parsing.
 	 * @return
 	 */
 	public String getCorrectedSource() {
 		return correctedSource;
 	}
 	
 	/**
 	 * The original source
 	 * @return
 	 */
 	public String getSource() {
 		try {
 			return getScript().getSource();
 		} catch (RubyModelException e) {
 			return "";
 		}
 	}
 	
 	public String getFullPrefix() {
 		return fullPrefix;
 	}
 	
 	public String getPartialPrefix() {
 		return partialPrefix;
 	}
 	
 	public int getOffset() {
 		return offset;
 	}
 	
 	public IRubyScript getScript() {
 		return script;
 	}
 
 	public boolean emptyPrefix() {
 		return getFullPrefix() == null || getFullPrefix().length() == 0;
 	}
 
 	public boolean prefixStartsWith(String name) {
 		return name != null && getPartialPrefix() != null && name.startsWith(getPartialPrefix());
 	}
 
 	public boolean isGlobal() {
 		return !emptyPrefix() && !isMethodInvokation() && getPartialPrefix().startsWith("$");
 	}
 
 }
