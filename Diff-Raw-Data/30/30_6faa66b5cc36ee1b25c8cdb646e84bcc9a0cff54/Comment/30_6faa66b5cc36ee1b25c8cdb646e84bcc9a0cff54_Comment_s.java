 package descent.internal.compiler.parser;
 
 import descent.internal.compiler.parser.ast.IASTVisitor;
 
 // DMD 1.020
 public class Comment extends ASTDmdNode {
 	
 	public final static int LINE_COMMENT = 1;
 	public final static int PLUS_COMMENT = 2;
 	public final static int BLOCK_COMMENT = 3;
 	public final static int DOC_LINE_COMMENT = 4;
 	public final static int DOC_PLUS_COMMENT = 5;
 	public final static int DOC_BLOCK_COMMENT = 6;
 	
 	public final int kind;
 	public final char[] string;
 	public int index; // This is for ASTConverter
 	
 	public Comment(int kind, char[] string) {
 		this.kind = kind;
 		this.string = string;		
 	}
 	
 	public boolean isDDocComment() {
 		return kind > 3;
 	}
 
 	@Override
 	protected void accept0(IASTVisitor visitor) {
 	}
 
 	@Override
 	public int getNodeType() {
 		return COMMENT;
 	}
 	
 	@Override
 	public String toString() {
		return "#Comment";
 	}
 
 }
