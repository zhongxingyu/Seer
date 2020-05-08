 package banjo.parser.errors;
 
 import banjo.parser.util.FileRange;
 
 public class BanjoParseException extends java.text.ParseException {
 	private static final long serialVersionUID = 1L;
 	private final FileRange range;
 
 	public BanjoParseException(String message, FileRange range) {
 		super(message, range.getStartOffset());
 		this.range = range;
 	}
 
 	public int getStartLine() { return range.getStart().line; }
 	public int getStartColumn() { return range.getStart().column; }
 	public int getEndLine() { return range.getEnd().line; }
 	public int getEndColumn() { return range.getEnd().column; }
	public int getStartOffset() {return range.getStart().offset;	}
	public int getEndOffset() {return range.getEnd().offset;	}
 	
 	@Override
 	public String toString() {
 		return range.toString()+": "+getLocalizedMessage();
 	}

 }
