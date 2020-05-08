 
 public class YourParserImpl implements Parser {
 	private Tokenizer to;	
 
 	public YourParserImpl(Tokenizer to) {
 		this.to = to;
 	}
 
 	@Override
 	public Node parse() throws RuntimeException {
 		AssignNode node = new AssignNode();
 		Token t = to.current();
 		Token n = to.next();
 		
 		//check to see if assign statement is valid
 		if (t.type() == Token.Type.IDENTIFIER && 
 				n.type() == Token.Type.EQ) {
 			node.left = new IdentifierNode();
 			IdentifierNode.class.cast(node.left).value = t.text();
 			
 			node.right = this.parseExpression();
 		} else {
 			throw new RuntimeException("Invalid assignment syntax");
 		}		
 		return node;
 	}
 	
 	
 	private Node parseExpression () {
 		ExpressionNode node = new ExpressionNode();
 		
 		Token t = to.next();
 		
 		node.left = this.parseTerm();
 		
 		t = to.current();
 		
 		if (t.type() == Token.Type.EOF){
 			node.operator = null;
 			node.right = null;
 		} else if (t.type() == Token.Type.PLUS || t.type() == Token.Type.MINUS ) {
 			node.operator = t.text();
 
 			//treat the possibly repeating expression by calling parse expression again
 			node.right = this.parseExpression();
		} else {
 			throw new RuntimeException("Not a valid Expression");
 		}
 		
 		return node;
 	}
 	
 	private Node parseTerm  () {
 		TermNode node = new TermNode();
 		
 		Token lhs = to.current();
 		
 		node.left = this.parseFactor();
 		
 		Token op = to.next();
 		
 		if (op.type() == Token.Type.EOF){
 			node.operator = null;
 			node.right = null;
 		} else if (op.type() == Token.Type.MULT || op.type() == Token.Type.DIV ) {
 			node.operator = op.text();
 			to.next(); //need to move to the beginning of the factor 
 			node.right = this.parseFactor();
 			to.next(); 
		} else if (!(op.type() == Token.Type.PLUS || op.type() == Token.Type.MINUS )) {
 			throw new RuntimeException("Not a valid Term");
 		}
 		
 		return node;
 	}
 	
 	private Node parseFactor () {
 		FactorNode node = new FactorNode();
 		
 		Token t = to.current();
 		
 		if (t.type() == Token.Type.NUMBER) {
 			NumberNode n = new NumberNode ();
 			n.value = (Integer) t.value();
 			node.node = n;
 		} else if (t.type() == Token.Type.LEFT_PAREN) {
 			node.node = this.parseExpression();
			t = to.next();
 			
 			if (t.type() != Token.Type.RIGHT_PAREN) 
 				throw new RuntimeException("Not a valid Factor, ')' expected.");
 			
 		} else {
 			throw new RuntimeException("Not a valid Factor, '(' expected.");
 		}
 		
 		return node;
 	}
 
 }
