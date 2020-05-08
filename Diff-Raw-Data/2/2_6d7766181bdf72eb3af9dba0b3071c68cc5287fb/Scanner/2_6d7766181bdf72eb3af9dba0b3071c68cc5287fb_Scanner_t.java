 package org.oobium.build.esp.parser.internal.parsers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.oobium.build.esp.dom.EspDom;
 import org.oobium.build.esp.dom.EspElement;
 import org.oobium.build.esp.dom.EspPart;
 import org.oobium.build.esp.dom.EspDom.DocType;
 import org.oobium.build.esp.dom.EspPart.Type;
 import org.oobium.build.esp.parser.exceptions.EspEndException;
 import org.oobium.logging.Logger;
 
 public class Scanner {
 
 	private class Containment {
 		char opener, closer;
 		int level;
 		Containment parent, sub;
 		
 		public Containment() {
 			opener = ca[offset];
 			closer = getCloserChar(opener);
 			level = 1;
 		}
 		
 		public Containment(char closer) {
 			this.closer = closer;
 			this.level = Scanner.this.level;
 		}
 		
 		public void addBlock() {
 			if(sub != null) {
 				sub.addBlock();
 			} else {
 				sub = new Containment();
 			}
 			sub.parent = this;
 		}
 		
 		public void check() throws EspEndException {
 			if(offset < 0) return;
 			if(offset >= ca.length) throw EspEndException.instance(offset);
 
 			if(sub != null) {
 				try {
 					sub.check();
 				} catch(EspEndException e) {
 					checkBlock();
 					throw e;
 				}
 			}
 			
 			if(isBlock()) {
 				checkBlock();
 			} else {
 				checkEnd();
 			}
 		}
 		
 		private void checkBlock() throws EspEndException {
 			if(ca[offset] == opener && ca[offset] != closer) {
 				level++;
 				return;
 			}
 			
 			if(ca[offset] == closer) {
 				if((closer != '"' && closer != '\'') || ca[offset-1] != '\\') { // check for escape char
 					level--;
 					if(level == 0) {
 						throw EspEndException.instance(this, offset);
 					}
 				}
 				return;
 			}
 		}
 		
 		public void checkBlocks() {
 			if(sub != null) {
 				sub.checkBlocks();
 			}
 			
 			if(isBlock()) {
 				try {
 					checkBlock();
 				} catch(EspEndException e) {
 					// discard (running all block checks to handle nesting)
 				}
 			}
 		}
 		
 		private void checkEnd() throws EspEndException {
 			if(offset >= ca.length) {
 				throw EspEndException.instance(this, offset);
 			}
 			if(closer == EOL) {
 				if(ca[offset] == EOL) {
 					throw EspEndException.instance(this, offset);
 				}
 				else if(ca[offset] == '<') {
 					int next = offset + 1;
 					if(next < ca.length && ca[next] == '-') {
 						throw EspEndException.instance(this, offset);
 					}
 				}
 			}
 			else if(closer == EOE) {
 				if(ca[offset] == EOL) {
 					for(int j = ++offset; offset < ca.length; offset++) {
 						if(ca[offset] != '\t') {
 							int l2 = offset-j;
 							Scanner.this.level = l2;
 							if(l2 <= level) {
 								throw EspEndException.instance(this, j-1);
 							}
 							break;
 						}
 					}
 					if(offset >= ca.length) {
 						throw EspEndException.instance(this, offset);
 					}
 				}
 			}
 		}
 
 		public int remove() {
 			int count = 1;
 			while(sub != null) {
 				count++;
 				sub = sub.sub;
 			}
 			if(parent != null) {
 				parent = parent.sub = null;
 			} // else, top level
 			return count;
 		}
 		
 		public boolean isBlock() {
 			return opener != 0;
 		}
 		
 		@Override
 		public String toString() {
 			if(isBlock()) {
 				StringBuilder sb = new StringBuilder();
 				sb.append(opener).append(level);
 				if(sub != null) sb.append(sub);
 				sb.append(closer);
 				return sb.toString();
 			} else {
 				if(sub == null) {
 					if(closer == EOL) return "EOL: " + level;
 					if(closer == EOE) return "EOE: " + level;
 					return closer + ": " + level;
 				} else {
 					if(closer == EOL) return "EOL: " + level + "," + sub;
 					if(closer == EOE) return "EOE: " + level + "," + sub;
 					return closer + ": " + level + "," + sub;
 				}
 			}
 		}
 	}
 
 
 	/** End of Line */
 	public static final char EOL = '\n';
 	
 	/** End of Element */
 	public static final char EOE = (char) 25; // "End of Element"
 
 	private static boolean all(char c, char...test) {
 		for(char t : test) {
 			if(c != t) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private static boolean any(char c, char...test) {
 		for(char t : test) {
 			if(c == t) {
 				return true;
 			}
 			if(t == ' ' && Character.isWhitespace(c)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private final char[] name;
 	private final DocType doctype;
 	private final char[] ca;
 	private EspPart part;
 	
 	private int level;
 	private final Map<EspPart, Integer> levels;
 	private final LinkedHashMap<EspPart, Containment> containments;
 	private int containmentEndCount;
 
 	private int mark;
 	private int offset;
 	
 	private boolean inString;
 	private boolean inStyle;
 	
 	Logger logger;
 
 	public Scanner(DocType type, char[] name, CharSequence src, Logger logger) {
 		this.name = name;
 		this.doctype = type;
 		this.ca = (src == null) ? new char[0] : src.toString().toCharArray();
 		this.mark = -1;
 		this.offset = -1;
 		this.logger = logger;
 		this.levels = new HashMap<EspPart, Integer>();
 		this.containments = new LinkedHashMap<EspPart, Containment>();
 	}
 
 	public void check() throws EspEndException {
 		checkContainment();
 		checkJava();
 		try {
 			offset = checkComment(offset);
 		} catch(EspEndException e) {
 			offset = e.getOffset();
 		}
 	}
 	
 	public boolean isCharSequence(char...c) {
 		if(offset+c.length-1 < ca.length) {
 			for(int i = 0; i < c.length; i++) {
 				int j = offset+i;
 				if((c[i] == ' ' && Character.isWhitespace(ca[j])) || c[i] == ca[j]) {
 					continue;
 				}
 				return false;
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * @return the position of the end of the comment
 	 * @throws EspEndException if comment consumes the rest of the document (in which case, this method
 	 * would have return ca.length)
 	 */
 	private int checkComment(int i) throws EspEndException {
 		return checkComment(i, false);
 	}
 
 	private int checkComment(int i, boolean test) throws EspEndException {
 		if(inString) return i;
 		if(i < 0) return i;
 		if(i >= ca.length) throw EspEndException.instance(i);
 		
 		int next = i + 1;
 		if(next < ca.length) {
 			if(ca[i] == '/') {
 				if(ca[next] == '/') {
 					i = next;
 					while(++i < ca.length && ca[i] != '\n');
 					if(!test) new EspPart(Type.Comment).setParent(part).setStart(next-1).setEnd(i);
 					if(i == ca.length) throw EspEndException.instance(i);
 					return i;
 				}
 				if(ca[next] == '*') {
 					i = next;
 					while(++i < ca.length && !(ca[i] == '/' && ca[i-1] == '*'));
 					if(i < ca.length) i++;
 					if(!test) new EspPart(Type.Comment).setParent(part).setStart(next-1).setEnd(i);
 					if(i == ca.length) throw EspEndException.instance(i);
 					return i;
 				}
 			}
 		}
 		
 		return i;
 	}
 	
 	private void checkContainment() throws EspEndException {
 		if(offset < 0) return;
 		if(offset >= ca.length) throw EspEndException.instance(offset);
 
 		EspEndException exception = null;
 		Object[] keys = containments.keySet().toArray();
 		for(int i = keys.length-1; i >= 0; i--) {
 			Object key = keys[i];
 			Containment containment = containments.get(key);
 			if(exception == null) {
 				try {
 					containment.check();
 				} catch(EspEndException e) {
 					exception = e;
 					if(key != part.getDom()) {
 						Containment c = e.getSourceAs(Containment.class);
 						containmentEndCount += c.remove();
 						if(c == containment) {
 							containments.remove(key);
 						}
 						for(int j = i+1; j < keys.length; j++) {
 							c = containments.get(keys[j]);
 							containmentEndCount += c.remove();
 							containments.remove(keys[j]);
 						}
 					}
 				}
 			}
 			else {
 				containment.checkBlocks();
 			}
 		}
 		if(exception != null) {
 			throw exception;
 		}
 	}
 	
 	public void handleContainmentEnd() throws EspEndException {
 		if(containmentEndCount > 0) {
 			containmentEndCount--;
 			if(containmentEndCount > 0) {
 				throw EspEndException.instance(offset);
 			}
 		}
 	}
 	
 	private void checkJava() throws EspEndException {
 		if(offset < 0) return;
 		if(offset >= ca.length) throw EspEndException.instance(offset);
 		
 		if(ca[offset] == '"' || ca[offset] == '\'') {
 			if(part.isA(Type.JavaSource)) {
 				pop();
 			}
 			if((ca[offset] == '"' && part.isA(Type.JavaContainer)) || part.isA(Type.ScriptPart)) {
 				inString = true;
 				EspPart str = push(Type.JavaString);
 				try {
 					findCloser();
 				} catch(EspEndException e) {
 					handleContainmentEnd();
 					popTo(str);
 					move(1); // don't use next() because that may start another Java Part before we want to
 					pop(str);
 					move(-1); // move back so the call to next() hits the char following the closing quote
 					inString = false;
 				}
 				next();
 			}
 			else if( ! inString && !inStyle && ca[offset] == '\'' && part.isA(Type.JavaContainer)) {
 				EspPart str = push(Type.JavaString);
 				while(++offset < ca.length && ca[offset] != '\'');
 				pop(str);
 			}
 			return;
 		}
 		
 		if((ca[offset] == '(' || ca[offset] == '{') && part.isA(Type.JavaSource)) {
 			try {
 				findCloser();
 			} catch(EspEndException e) {
 				handleContainmentEnd();
 //				if(isNotChar(')', '}')) {
 //					throw e;
 //				} // else - fall through
 			}
 		}
 		
 		if(part.isA(Type.JavaContainer) && (!Character.isWhitespace(ca[offset]) || part.hasParts())) {
 			checkJavaEscape();
 			push(Type.JavaSource);
 			return;
 		}
 		
 		if(ca[offset] == '$' && (offset == 0 || ca[offset-1] != '\\')) {
 			if(part.isA(Type.JavaString) || part.isA(Type.ScriptPart) || part.isA(Type.StylePropertyValue)) {
 				int next = offset + 1;
 				if(next < ca.length) {
 					if(ca[next] == '{') {
 						EspPart container = push(Type.JavaContainer);
 						offset = next;
 						checkContainment();
 						setContainmentToCloser();
 						try {
 							while(true) next();
 						} catch(EspEndException e) {
 							handleContainmentEnd();
 							popTo(container);
 							next();
 						}
 						pop(container);
 					}
 					else if(part.isA(Type.JavaString) && Character.isJavaIdentifierStart(ca[next])) {
 						EspPart container = push(Type.JavaContainer);
 						offset = next;
 						push(Type.JavaSource);
 						try {
 							findEndOfJavaIdentifier();
 						} catch(EspEndException e) {
 							pop(container);
 							throw e;
 						}
 						pop(container);
 					}
 				}
 			}
 			return;
 		}
 
 		if(ca[offset] == '{') {
 			switch(part.getType()) {
 			case MarkupId: case MarkupClass: case InnerTextPart:
 				EspPart jpart = push(Type.JavaContainer);
 				try {
 					findCloser();
 				} catch(EspEndException e) {
 					handleContainmentEnd();
 					popTo(jpart);
 					next();
 					pop(jpart);
 				}
 				check();
 			}
 			return;
 		}
 	}
 	
 	private void checkJavaEscape() throws EspEndException {
 		if(ca[offset-1] == '{') {
 			switch(ca[offset]) {
 			case 'n': case 'h': case 'j': case 'f': case 'r':
 				int next = offset + 1;
 				if(next < ca.length && Character.isWhitespace(ca[next])) {
 					EspPart escape = push(Type.JavaEscape);
 					move(1).next();
 					pop(escape);
 				}
 				break;
 			}
 		}
 	}
 
 	private int entryCheck(int offset) throws EspEndException {
 		offset = checkComment(offset);
 
 		char opener = ca[offset];
 		char closer = getCloserChar(ca[offset]);
 		if(closer == 0) {
 			return checkComment(offset);
 		}
 		
 		int count = 1;
 		while(++offset < ca.length) {
 			if(ca[offset] == opener && ca[offset] != closer) {
 				count++;
 			}
 			else if(ca[offset] == closer) {
 				if((closer != '"' && closer != '\'') || ca[offset-1] != '\\') {
 					count--;
 					if(count == 0) {
 						return offset;
 					}
 				}
 			}
 			else if(ca[offset] == '"') {
 				offset = entryCheck(offset); // just entered a string - get out of it
 			}
 			offset = checkComment(offset);
 		}
 		
 		throw EspEndException.instance(-1);
 	}
 	
 	public Scanner find(char c) throws EspEndException {
 		if(offset < 0) offset = 0;
 		while(offset < ca.length) {
 			if(ca[offset] == c) break;
 			check();
 			if(offset < ca.length) {
 				if(ca[offset] == c) break;
 				offset++;
 			}
 		}
 		return this;
 	}
 
 	public Scanner findAll(char...test) throws EspEndException {
 		if(offset < 0) offset = 0;
 		while(offset < ca.length) {
 			if(all(ca[offset], test)) break;
 			check();
 			if(offset < ca.length) {
 				if(all(ca[offset], test)) break;
 				offset++;
 			}
 		}
 		return this;
 	}
 
 	public Scanner findAny(char...test) throws EspEndException {
 		if(offset < 0) offset = 0;
 		while(offset < ca.length) {
 			if(any(ca[offset], test)) break;
 			check();
 			if(offset < ca.length) {
 				if(any(ca[offset], test)) break;
 				offset++;
 			}
 		}
 		return this;
 	}
 
 	public boolean findChild(int parentLevel) throws EspEndException {
 		while(offset < ca.length) {
 			offset = checkComment(offset);
 			if(ca[offset] == '\n') {
 				int l2 = offset + 1;
 				for( ; l2 < ca.length; l2++) {
 					if(ca[l2] != '\t') break;
 				}
 				if(l2 < ca.length && ca[l2] == '\n') {
 					offset = l2;
 					return findChild(parentLevel);
 				}
 				l2 -= (offset+1);
 				level = l2;
 				offset += (l2+1);
 				if(l2 > parentLevel) {
 					return true;
 				}
 				throw EspEndException.instance(offset);
 			}
 			if(ca[offset] == '<') {
 				int next = offset + 1;
 				if(next < ca.length && ca[next] == '-') {
 					new EspPart(Type.Separator).setParent(part).setStart(offset).setEnd(next);
 					for(offset += 2; offset < ca.length && Character.isWhitespace(ca[offset]); offset++);
 					if(offset >= ca.length) {
 						throw EspEndException.instance(offset);
 					}
 					return true;
 				}
 			}
 			offset++;
 		}
 		throw EspEndException.instance(offset);
 	}
 
 //	public Scanner findCloser() throws EspEndException {
 //		if(offset >= 0 && offset+1 < ca.length) {
 //			findCloser(ca[offset]);
 //		}
 //		return this;
 //	}
 //
 //	public Scanner findCloser(char opener) throws EspEndException {
 //		if(offset >= 0 && offset+1 < ca.length) {
 //			char closer = getCloserChar(opener);
 //			if(closer != 0) {
 //				findCloser(opener, closer);
 //			}
 //		}
 //		return this;
 //	}
 //
 //	private Scanner findCloser1(char opener, char closer) throws EspEndException {
 //		checkContainment();
 //		int count = 1;
 //		while(true) {
 //			next();
 //			if(ca[offset] == opener && ca[offset] != closer) {
 //				count++;
 //			}
 //			else if(ca[offset] == closer) {
 //				if((closer != '"' && closer != '\'') || ca[offset-1] != '\\') { // check for escape char
 //					count--;
 //					if(count == 0) {
 //						return this;
 //					}
 //				}
 //			}
 //			else if(ca[offset] == '"') {
 //				findCloser('"', '"'); // just entered a string - get out of it
 //			}
 //		}
 //	}
 	
 	public Scanner findDeclaration() throws EspEndException {
 		int parentLevel = level;
 		while(offset < ca.length) {
 			offset = checkComment(offset);
 			if(ca[offset] == '{') {
 				return this;
 			}
 			if(ca[offset] == '\n') {
 				int l2 = offset + 1;
 				for( ; l2 < ca.length; l2++) {
 					if(ca[l2] != '\t') break;
 				}
 				if(ca[l2] == '\n') {
 					offset = l2;
 					return findDeclaration();
 				}
 				l2 -= (offset+1);
 				level = l2;
 				offset += (l2+1);
 				if(l2 > parentLevel) {
 					return this;
 				}
 				throw EspEndException.instance(offset);
 			}
 			offset++;
 		}
 		throw EspEndException.instance(offset);
 	}
 
 	/**
 	 * ALWAYS throws an {@link EspEndException}
 	 */
 	public void findCloser() throws EspEndException {
 		setContainmentToCloser();
 		while(true) next();
 	}
 	
 	/**
 	 * ALWAYS throws an {@link EspEndException}
 	 */
 	public void findEndOfContainment() throws EspEndException {
 		check();
 		while(true) next();
 	}
 	
 	public Scanner findEndOfElement() throws EspEndException {
 		try {
 			check();
 			while(next().hasNext());
 		} catch(EspEndException e) {
 			handleContainmentEnd();
 			offset = e.getOffset();
 		}
 		return this;
 	}
 	
 	public Scanner findEndOfJavaIdentifier() throws EspEndException {
 		check();
 		if(Character.isJavaIdentifierStart(getChar())) {
 			while(Character.isJavaIdentifierPart(next().getChar()));
 		}
 		return this;
 	}
 	
 	public Scanner findEndOfJavaType() throws EspEndException {
 		check();
 		if(Character.isJavaIdentifierStart(getChar())) {
 			while(Character.isJavaIdentifierPart(next().getChar()));
 			if(isChar('<')) {
 				try {
 					findCloser();
 				} catch(EspEndException e) {
 					handleContainmentEnd();
 					next();
 				}
 			}
 		}
 		return this;
 	}
 
 	public Scanner findEndOfLine() throws EspEndException {
 		return find(EOL);
 	}
 
 	public Scanner findEndOfMarkupId() throws EspEndException {
 		check();
 		if(isWordChar() || ca[offset] == '.' || ca[offset] == '#') {
 			while(next().isWordChar() || ca[offset] == '-' || ca[offset] == '[' || ca[offset] == ']');
 		}
 		return this;
 	}
 	
 	public Scanner findEndOfMarkupAttr() throws EspEndException {
 		check();
 		if(isWordChar()) {
 			while(next().isWordChar() || ca[offset] == '-' || ca[offset] == '[' || ca[offset] == ']');
 		}
 		return this;
 	}
 	
 	public Scanner findEndOfStylePropertyName() throws EspEndException {
 		while(offset < ca.length) {
 			switch(ca[offset]) {
 			case ' ': case ':': case ';': case '{': case '(': case EOL:
 				return this;
 			case '[':
 				try {
 					findCloser();
 				} catch(EspEndException e) {
 					handleContainmentEnd();
 					continue;
 				}
 			default:
 				if(Character.isWhitespace(ca[offset])) return this;
 			}
 			check();
 			offset++;
 		};
 		throw EspEndException.instance(offset);
 	}
 	
 	public Scanner findEndOfStyleSelector() throws EspEndException {
 		boolean stopOnParen = (ca[offset] != '@'); // @media
 		while(offset < ca.length) {
 			switch(ca[offset]) {
 			case ',': case '{':
 				return this;
 			case ':':
 				stopOnParen = false;
 				break;
 			case '(':
 				if(stopOnParen) {
 					return this;
 				}
 				break;
 			case '[':
 				try {
 					findCloser();
 				} catch(EspEndException e) {
 					handleContainmentEnd();
 					continue;
 				}
 			}
 			check();
 			offset++;
 		};
 		throw EspEndException.instance(offset);
 	}
 	
 	public Scanner findEndOfWord() throws EspEndException {
 		check();
 		if(isWordChar()) {
 			while(next().isWordChar());
 		}
 		return this;
 	}
 	
 	private void finishJavaContainer(EspPart container) {
 		List<EspPart> sources = new ArrayList<EspPart>(container.getParts());
 		int last = sources.size() - 1;
 		for(int i = last; i >= 0; i--) {
 			EspPart source = sources.get(i);
 			if(i == last) {
 				int start = source.getStart();
 				int end = source.getEnd() - 1;
 				while(end > start && Character.isWhitespace(ca[end])) {
 					end--;
 				}
 				source.setEnd(end + 1);
 			}
 			if(source.length() == 0) {
 				source.setParent(null);
 			}
 		}
 	}
 	
 	/**
 	 * Advance from offset (inclusive) to the first non-whitespace character.
 	 * The search goes until either the character string ends or the part's element ends
 	 * (a {@link EspEndException} will be thrown in this case).
 	 * @param part the EspPart to search in (uses it's char[] and element level)
 	 * @param start the position to offset the search
 	 * @return the position of the first non-whitespace character in the given part's element, or -1 if one is not found
 	 * @throws EspEndException if the part's element ends before a non-whitespace character is found
 	 */
 	public Scanner forward() throws EspEndException {
 		check();
 		if(offset == -1 || Character.isWhitespace(getChar())) {
 			while(Character.isWhitespace(next().getChar()));
 		}
 		return this;
 	}
 	
 	public char getChar() {
 		if(offset >= 0 && offset < ca.length) {
 			return ca[offset];
 		}
 		return EOL;
 	}
 	
 	private char getCloserChar(char opener) {
 		switch(opener) {
 			case '<':  return '>';
 			case '(':  return ')';
 			case '{':  return '}';
 			case '[':  return ']';
 			case '"':  return '"';
 			case '\'': return '\'';
 			default: return 0;
 		}
 	}
 
 	public int getTrimmedEnd() {
 		return getTrimmedEndFrom(offset);
 	}
 	
 	public int getTrimmedEndFrom(int offset) {
 		for(int i = offset - 1; i >= 0 && i < ca.length; i--) {
 			if( ! Character.isWhitespace(ca[i])) {
 				return i + 1;
 			}
 		}
 		throw new IllegalStateException("you didn't call this blind, did you?");
 	}
 	
 	public int getLevel() {
 		return level;
 	}
 	
 	public int getLevelAt(int offset) {
 		int i = offset;
 		while(i > 0) {
 			if(ca[i] == '\n') {
 				break;
 			}
 			i--;
 		}
 		int j = ++i;
 		for( ; i < ca.length; i++) {
 			if(ca[i] != '\t') break;
 		}
 		return i-j;
 	}
 	
 	public int getMark() {
 		return mark;
 	}
 	
 	public int getOffset() {
 		return offset;
 	}
 
 	public boolean hasDeclaration() {
 		try {
 			for(int i = offset; i < ca.length; i++) {
 				if(ca[i] == '\n') {
 					for(int l2 = i + 1; l2 < ca.length; l2++) {
 						if(ca[l2] != '\t') return (l2-i) > level;
 					}
 				}
 				if(ca[i] == '{') {
 					return true;
 				}
 				i = checkComment(i, true);
 			}
 		} catch(EspEndException e) {
 			// fall through
 		}
 		return false;
 	}
 	
 	public boolean hasNext() {
 		return offset < ca.length;
 	}
 	
 	public boolean isChar(char c) {
 		if(offset >= 0 && offset < ca.length) {
 			return ca[offset] == c;
 		}
 		return c == EOL;
 	}
 
 	public boolean isChar(char...c) {
 		if(offset >= 0 && offset < ca.length) {
 			return any(ca[offset], c);
 		}
 		return any(EOL, c);
 	}
 
 	public boolean isCharEscaped() {
 		if(offset >= 1) {
 			return ca[offset-1] == '\\' && ca[offset-2] == '\\';
 		}
 		return false;
 	}
 
 	public boolean isEmpty() {
 		return length() == 0;
 	}
 
 	public boolean isEndOfLine() {
 		return isChar(EOL);
 	}
 
 	public boolean isLowerCase() {
 		if(offset >= 0 && offset < ca.length) {
 			return Character.isLowerCase(ca[offset]);
 		}
 		return false;
 	}
 	
 	public boolean isNext(char c) {
 		int next = offset + 1;
 		if(next < ca.length) {
 			return (c == ' ') ? Character.isWhitespace(ca[next]) : (c == ca[next]);
 		}
 		return false;
 	}
 	
 	public boolean isNextEntry() {
 		int i = offset;
 		try {
			while(i < ca.length && ca[i] != '"' && ca[i] != ',' && ca[i] != ')' && ca[i] != '\n') {
 				if(ca[i] == ':') return true;
 				i = entryCheck(i);
 				i++;
 			}
 		} catch(EspEndException e) {
 			// fall through
 		}
 		return false;
 	}
 	
 	public boolean isNextNotWordChar() {
 		return ! isNextWordChar();
 	}
 	
 	public boolean isNextParametricRuleset() {
 		int i = offset;
 		if(i < ca.length && (Character.isLetter(ca[i]) || ca[i] == '.' || ca[i] == '#')) {
 			while(++i < ca.length && (Character.isLetter(ca[i]) || ca[i] == '_' || ca[i] == '-'));
 			return i < ca.length && ca[i] == '(';
 		}
 		return false;
 	}
 
 	public boolean isNextWordChar() {
 		int next = offset + 1;
 		if(next < ca.length) {
 			return Character.isLetterOrDigit(ca[next]) || ca[next] == '_';
 		}
 		return false;
 	}
 	
 	public boolean isNotChar(char c) {
 		return ! isChar(c);
 	}
 	
 	public boolean isNotChar(char...c) {
 		return ! isChar(c);
 	}
 	
 	public boolean isNotEndOfLine() {
 		return ! isChar(EOL);
 	}
 
 	public boolean isNotWordChar() {
 		return ! isWordChar();
 	}
 	
 	public boolean isWhitespace() {
 		return Character.isWhitespace(getChar());
 	}
 	
 	public boolean isWordChar() {
 		char c = getChar();
 		return Character.isLetterOrDigit(c) || c == '_';
 	}
 	
 	public int length() {
 		return (mark == -1) ? 0 : (offset - mark);
 	}
 	
 	public Scanner mark() {
 		mark = offset;
 		return this;
 	}
 	
 	public Scanner move(int steps) {
 		offset += steps;
 		return this;
 	}
 	
 	public Scanner next() throws EspEndException {
 		if(offset < ca.length) {
 			offset++;
 			check();
 			return this;
 		}
 		throw EspEndException.instance(offset = ca.length);
 	}
 	
 	public void parseChildren() throws EspEndException {
 		ElementBuilder builder = new ElementBuilder(this);
 		builder.parseChildren();
 	}
 	
 	public void parseConstructorElement() throws EspEndException {
 		JavaBuilder builder = new JavaBuilder(this);
 		builder.parseConstructorElement();
 	}
 	
 	public EspDom parseDom() {
 		DomBuilder builder = new DomBuilder(this);
 		return builder.parse(doctype, new String(name), ca);
 	}
 
 	public void parseImportElement() throws EspEndException {
 		JavaBuilder builder = new JavaBuilder(this);
 		builder.parseImportElement();
 	}
 	public void parseJavaElement() throws EspEndException {
 		JavaBuilder builder = new JavaBuilder(this);
 		builder.parseJavaElement();
 	}
 	
 	public void parseMarkupComment() throws EspEndException {
 		MarkupBuilder builder = new MarkupBuilder(this);
 		builder.parseMarkupComment();
 	}
 	
 	public void parseMarkupElement() throws EspEndException {
 		MarkupBuilder builder = new MarkupBuilder(this);
 		builder.parseMarkupElement();
 	}
 
 	public void parseScriptElement() throws EspEndException {
 		ScriptBuilder builder = new ScriptBuilder(this);
 		builder.parseScriptElement();
 	}
 	
 	public void parseStyleElement() throws EspEndException {
 		StyleBuilder builder = new StyleBuilder(this);
 		builder.parseStyleElement();
 	}
 	
 	private EspPart pop() {
 		containments.remove(part);
 
 		EspPart parent = this.part.getParent();
 		EspPart part = this.part;
 		this.part = parent;
 
 		part.setEnd(offset);
 		
 		if(part.isA(Type.JavaContainer)) {
 			finishJavaContainer(part);
 		}
 		else if(part.isA(Type.StylePart)) {
 			inStyle = false;
 		}
 		
 		return part;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T extends EspPart> T pop(T part) {
 		T popped;
 		while((popped = (T) pop()) != part);
 		return popped;
 	}
 	
 	public <T extends EspPart> T pop(T part, int offset) throws EspEndException {
 		int tmp = this.offset;
 		this.offset = offset;
 		T popped = pop(part);
 		this.offset = tmp;
 		popped.setEnd(offset);
 		
 		handleContainmentEnd();
 		
 		if(inStyle) {
 			if(level < levels.get(popped)) {
 				throw EspEndException.instance(offset);
 			}
 		}
 		else if(popped instanceof EspElement) {
 			int elevel = levels.get(popped);
 			if(level < elevel) {
 				throw EspEndException.instance(offset);
 			}
 			Integer plevel = levels.get(popped.getParent());
 			if(plevel != null && plevel.intValue() == elevel) {
 				throw EspEndException.instance(offset);
 			}
 		}
 		
 		return popped;
 	}
 	
 	public EspPart popTo(EspPart part) {
 		EspPart popped = this.part;
 		while(popped != part) { popped = pop().getParent(); }
 		return popped;
 	}
 
 	public <T extends EspPart> T push(T part) {
 		part.setParent(this.part);
 		part.setStart(this.offset);
 		this.part = part;
 
 		if(inStyle) {
 			levels.put(part, level);
 		} else {
 			if(part instanceof EspElement) {
 				levels.put(part, level);
 			} 
 			else if(part.isA(Type.StylePart)) {
 				inStyle = true;
 			}
 		}
 		
 		return part;
 	}
 	
 	public EspPart push(Type type) {
 		return push(new EspPart(type));
 	}
 
 	public char scanForAny(char...test) {
 		try {
 			for(int i = offset; i < ca.length; i++) {
 				for(int j = 0; j < test.length; j++) {
 					if(ca[i] == test[j]) return test[j];
 				}
 				i = checkComment(i, true);
 			}
 		} catch(EspEndException e) {
 			// fall through
 		}
 		return EOE;
 	}
 	
 	public Scanner setContainmentToCloser() {
 		EspPart key = part.isA(Type.JavaSource) ? part.getParent() : part;
 		Containment containment = containments.get(key);
 		if(containment == null) {
 			containments.put(key, new Containment());
 		} else {
 			containment.addBlock();
 		}
 		return this;
 	}
 	
 	public Scanner setContainmentToEOE() {
 		containments.put(part, new Containment(EOE));
 		return this;
 	}
 	
 	public Scanner setContainmentToEOL() {
 		containments.put(part, new Containment(EOL));
 		return this;
 	}
 
 	public Scanner skip() {
 		return move(1);
 	}
 	
 }
