 
 package com.redcareditor.mate;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Position;
 import org.eclipse.swt.custom.StyledText;
 
 import com.redcareditor.onig.Match;
 import com.redcareditor.onig.Rx;
 
 public class Scope {
 	public MateText mateText;
 	private StyledText styledText;
 	
 	public String name;
 	public Pattern pattern;
 	
 	public boolean isOpen;
 	public boolean isCapture;
 	
 	public Match openMatch;
 	public Match closeMatch;
 	
 	public Position startPos;
 	public Position innerStartPos;
 	public Position innerEndPos;
 	public Position endPos;
 	
 	public Rx closingRegex;
 	public String beginMatchString;
 	public String endMatchString;
 	
 	public Scope parent;
 	public List<Scope> children;
 	
 	StringBuilder prettyString;
 	int indent;
 	
 	public Scope(MateText mt, String name) {
 		this.mateText = mt;
 		this.styledText = mt.getTextWidget();
 		this.name = name;
 		this.children = new ArrayList<Scope>();
 	}
 	
 	public void clearAfter(int lineIx, int something) {
 		// TODO: port this method
 	}
 
 	public Scope scopeAt(int line, int lineOffset) {
 //		System.out.printf("scopeAt(%d, %d) isOpen:%s\n", line, lineOffset, isOpen);
 		TextLocation loc = new TextLocation(line, lineOffset);
 		TextLocation start = startLoc();
 		if (TextLocation.lte(start, loc) || parent == null) {
 			if (isOpen || TextLocation.gte(endLoc(), loc)) {
 				if (children.size() == 0) {
 					return this;
 				}
 				for (Scope child : children) {
 					if (child.containsLoc(loc)) {
 						return child.scopeAt(line, lineOffset);
 					}
 				}
 				return this;
 			}
 			else {
 				return null;
 			}
 		}
 		else {
 			return null;
 		}
 	}
 	
 	private boolean containsLoc(TextLocation loc) {
 		if (TextLocation.lte(startLoc(), loc) && TextLocation.gt(endLoc(), loc))
 			return true;
 		else 
 			return false;
 	}
 
 	public Scope containingDoubleScope(int lineIx) {
 		// TODO: port this method
 		return this;
 	}
 	
 	public boolean surfaceIdenticalTo(Scope other) {
 		// TODO: port me
 		return false;
 	}
 
 	public boolean surfaceIdenticalToModuloEnding(Scope other) {
 		// TODO: port me
 		return false;
 	}
 
 	public boolean overlapsWith(Scope other) {
 		// TODO: port me
 		return false;
 	}
 
 	public void addChild(Scope newChild) {
 		// TODO: should insert in the correct position
 		if (children.size() == 0){
 			children.add(newChild);
 			return;
 		}
 			
 		if (children.get(0).startOffset() > newChild.startOffset()){
 			children.add(0, newChild);
 			return;
 		}
 		
 		int insertIx = 0;
 		int ix = 1;
 		for (Scope child : children) {
 			if (child.startOffset() <= newChild.startOffset()) {
 				insertIx = ix;
 			}
 			ix++;
 		}
 		children.add(insertIx, newChild);
 	}
 	
 	public void removeChild(Scope child) {
 		children.remove(child);
 	}
 
 	private int comparePositions(Position a, Position b) {
 		return 0;
 	}
 
 	public Scope firstChildAfter(TextLocation loc) {
 //		stdout.printf("\"%s\".first_child_after(%d, %d)\n", name, loc.line, loc.line_offset);
 		if (children.size() == 0)
 			return null;
 		
 		for (Scope child : children) {
 			if (TextLocation.gte(child.startLoc(), loc)) {
 				return child;
 			}
 		}
 		return null;
 	}
 
 	private Position makePosition(int line, int lineOffset) {
 		Position pos = new Position(styledText.getOffsetAtLine(line) + lineOffset, 0);
 		try {
 			mateText.getDocument().addPosition(pos);
 		}
 		catch(BadLocationException e) {
 			System.out.printf("BadLocationException in Scope (%d, %d)\n", line, lineOffset);
 			e.printStackTrace();
 		}
 		return pos;
 	}
 	
 	public void setStartPos(int line, int lineOffset, boolean hasLeftGravity) {
 		this.startPos = makePosition(line, lineOffset);
 	}
 
 	public void setInnerStartPos(int line, int lineOffset, boolean hasLeftGravity) {
 		this.innerStartPos = makePosition(line, lineOffset);
 	}
 
 	public void setInnerEndPos(int line, int lineOffset, boolean c) {
 		this.innerEndPos = makePosition(line, lineOffset);
 	}
 
 	public void setEndPos(int line, int lineOffset, boolean c) {
 		this.endPos = makePosition(line, lineOffset);
 	}
 
 	public TextLocation startLoc() {
 		return new TextLocation(startLine(), startLineOffset());
 	}
 
 	public TextLocation innerEndLoc() {
 		return new TextLocation(endLine(), innerEndLineOffset());
 	}
 	
 	public TextLocation endLoc() {
 		return new TextLocation(endLine(), endLineOffset());
 	}
 	
 	public int startLine() {
 		if (startPos == null)
 			return 0;
 		else
 			return styledText.getLineAtOffset(startPos.offset);
 	}
 
 	public int endLine() {
 		if (endPos == null)
 			return styledText.getLineCount() - 1;
 		else
 			return styledText.getLineAtOffset(endPos.offset);
 	}
 
 	public int startLineOffset() {
 		if (startPos == null)
 			return 0;
 		else
 			return startPos.offset - styledText.getOffsetAtLine(startLine());
 	}
 
 	public int innerEndLineOffset() {
 		if (innerEndPos == null)
 			return styledText.getCharCount();
 		else
 			return innerEndPos.offset - styledText.getOffsetAtLine(endLine());
 	}
 	
 	public int endLineOffset() {
 		if (endPos == null)
			return styledText.getCharCount() - styledText.getOffsetAtLine(endLine());
 		else
 			return endPos.offset - styledText.getOffsetAtLine(endLine());
 	}
 	
 	public int startOffset() {
 		return this.startPos.offset;
 	}
 	
 	public int endOffset() {
 		return this.endPos.offset;
 	}
 	
 	public String pretty(int indent) {
 		prettyString = new StringBuilder("");
 		this.indent = indent;
 		for (int i = 0; i < indent; i++)
 			prettyString.append("  ");
 		if (this.isCapture)
 			prettyString.append("c");
 		else
 			prettyString.append("+");
 		prettyString.append(" " + this.name);
 		if (this.pattern instanceof DoublePattern && 
 				this.isCapture == false && 
 				((DoublePattern) this.pattern).contentName != null) 
 			prettyString.append(" " + ((DoublePattern) this.pattern).contentName);
 		prettyString.append(" (");
 		// TODO: port these sections once Positions are figured out
 //		if (startPos == null) {
 //			prettyString.append("inf");
 //		}
 //		else {
 			prettyString.append(String.format(
 					"%d,%d",
 					startLine(), 
 					startLineOffset()));
 //		}
 		prettyString.append(")-(");
 //		if (endPos == null) {
 //			prettyString.append("inf");
 //		}
 //		else {
 			prettyString.append(String.format(
 					"%d,%d",
 					endLine(), 
 					endLineOffset()));
 //		}
 		prettyString.append(")");
 		prettyString.append((isOpen ? " open" : " closed"));
 		prettyString.append("\n");
 
 		this.indent += 1;
 		for (Scope child : this.children) {
 			prettyString.append(child.pretty(this.indent));
 		}
 		
 		return prettyString.toString();
 	}
 }
