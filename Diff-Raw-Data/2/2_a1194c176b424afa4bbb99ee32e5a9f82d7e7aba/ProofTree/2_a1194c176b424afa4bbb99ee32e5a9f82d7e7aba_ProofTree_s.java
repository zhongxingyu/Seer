 package source;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Stack;
 
 import source.Token.Type;
 
 //import com.sun.tools.example.debug.bdi.MethodNotFoundException;
 
 public class ProofTree extends BinaryTree<Token> {	protected final boolean debug = false;
 
 	static final String kErrorVariable = "Variable exception";
 	static final String kErrorOperator = "Operator exception";
 	static final String kErrorParenthesis = "Parenthesis Exception";
 	static final String kErrorUnary = "Unary Operator Exception";
 	static final String kErrorMissing = "Missing argument";
 	static final String kExpressionInvalid = "Expression Exception";
 
 	public ProofTree() {
 		super();
 	}
 
 	public ProofTree(ProofNode root) {
 		super(root);
 	}
 	
 	private static boolean isValidExpression(Stack<Token.Type> expressionStack)
 	{
 //		for (Iterator<Token.Type> iter = expressionStack.iterator(); iter.hasNext();)
 //		{
 //			System.out.println(iter.next());
 //		}
 		
 		if (expressionStack.size() != 3)
 			return false;
 		
 		return true;
 	}
 
 	private static Queue<Token> infixToPostfix(ArrayList<Token> tokenArray) throws IllegalLineException {
 
 		Stack<Token> operatorStack = new Stack<Token>();
 		Queue<Token> linkedList = new LinkedList<Token>();
 
 		Stack<Token.Type> errorStack = new Stack<Token.Type>();
 		Stack<Token.Type> parenthesisStack = new Stack<Token.Type>();
 		Stack<Stack<Token.Type>> expressionStack = new Stack<Stack<Token.Type>>();
 
 		Token oldToken = null;
 		Stack<Token.Type> currentTokenExpressionStack = null;
 
 		for (Iterator<Token> iter = tokenArray.iterator(); iter.hasNext();) 
 		{
 			Token token = iter.next();
 
 			if (token.getType() == Token.Type.OPEN_PARENTHESIS)
 			{
 				parenthesisStack.push(Token.Type.OPEN_PARENTHESIS);
 				operatorStack.push(token);
 				
 				Stack<Token.Type> tokenExpressionStack = new Stack<Token.Type>();
 				expressionStack.push(tokenExpressionStack);
 				currentTokenExpressionStack = tokenExpressionStack;
 			}
 			else if (token.getType() == Token.Type.CLOSE_PARENTHESIS)
 			{
 				if (parenthesisStack.empty() == true)
 					throw new IllegalLineException(kErrorParenthesis);
 
 				Token.Type type = parenthesisStack.pop();
 				if (type != Token.Type.OPEN_PARENTHESIS)
 					throw new IllegalLineException(kErrorParenthesis);
 
 				while (!operatorStack.isEmpty())
 				{					
 					Token currentToken = operatorStack.pop();
 					if (currentToken.getType() == Token.Type.OPEN_PARENTHESIS)
 					{
 						break;
 					}
 
 					linkedList.add(currentToken);
 				}
 				
 				if (isValidExpression(currentTokenExpressionStack))
 				{
 					expressionStack.pop();
 					if (expressionStack.empty())
 						currentTokenExpressionStack = null;
 					else
 					{
 						currentTokenExpressionStack = expressionStack.lastElement();
 						currentTokenExpressionStack.push(Token.Type.VARIABLE);
 					}
 				}
 				else
 					throw new IllegalLineException(kExpressionInvalid);
 				
 			}
 			else if (token.getType() == Token.Type.VARIABLE)
 			{
 				linkedList.add(token);
 
 				if (errorStack.empty() == false)
 				{
 					Token.Type lastType = errorStack.pop();
 					if (lastType == Token.Type.VARIABLE)
 						throw new IllegalLineException(kErrorVariable);
 				}
 
 				errorStack.push(Token.Type.VARIABLE);
 			
 				if (currentTokenExpressionStack == null)
 					throw new IllegalLineException(kErrorVariable);
 					
 				currentTokenExpressionStack.push(Token.Type.VARIABLE);
 				
 			}
 			else if (token.getType() == Token.Type.UNARY_NOT_OPERATOR)
 			{
 
 				if (oldToken != null && oldToken.getType() == Token.Type.VARIABLE)
 					throw new IllegalLineException(kErrorUnary);
 
 				operatorStack.push(token);
 
 			}
 			else
 			{
 
 				if (operatorStack.empty() == false)
 				{
 					Token lastToken = operatorStack.lastElement();
 
 					if (lastToken.getType() == Token.Type.UNARY_NOT_OPERATOR)
 					{
 						operatorStack.pop();
 						linkedList.add(lastToken);
 					}
 				}
 
 				operatorStack.push(token);
 
 				if (errorStack.empty() == true)
 					throw new IllegalLineException(kErrorOperator);
 
 				Token.Type type = errorStack.pop();
 
 				if (type != Token.Type.VARIABLE)
 					throw new IllegalLineException(kErrorOperator);
 
 				errorStack.push(token.getType());
 				currentTokenExpressionStack.push(token.getType());
 
 			}
 
 			oldToken = token;
 		}
 
 		while (!operatorStack.isEmpty())
 		{
 			Token token = operatorStack.pop();
 			linkedList.add(token);
 		}
 
 		if (parenthesisStack.empty() == false)
 			throw new IllegalLineException(kErrorParenthesis);
 		
 		if (expressionStack.empty() == false)
 		{
 			if (!isValidExpression(currentTokenExpressionStack))
 				throw new IllegalLineException(kExpressionInvalid);
 		}
 
 		return linkedList;
 	}
 
 	public static ProofTree buildTree(ArrayList<Token> tokenArray) throws IllegalLineException {
 
 		Queue<Token> queue = infixToPostfix(tokenArray);
 
 		Stack<ProofNode> treeNodeStack = new Stack<ProofNode>();
 
 		for (Iterator<Token> iter = queue.iterator(); iter.hasNext();)
 		{
 			Token token = iter.next();
 
 			if (token.getType() == Token.Type.VARIABLE)
 			{
 				treeNodeStack.push(new ProofNode(token));
 			}
 			else if (token.getType() == Token.Type.UNARY_NOT_OPERATOR)
 			{
 				if (treeNodeStack.isEmpty() == true)
 					throw new IllegalLineException(kErrorUnary);
 
 				treeNodeStack.lastElement().switchUnaryFlag();
 			}
 			else
 			{
 				ProofNode parentNode = new ProofNode(token);
 
 				if (treeNodeStack.size() < 2)
 					throw new IllegalLineException(kErrorMissing);
 
 				ProofNode rightNode = treeNodeStack.pop();
 				ProofNode leftNode = treeNodeStack.pop();
 
 				parentNode.linkTo(leftNode, rightNode);
 				treeNodeStack.push(parentNode);
 			}
 		}
 
 		return new ProofTree(treeNodeStack.pop());
 
 	}
 	
 	private ArrayList<Token.Type> getExpressionOrder(ProofTree definitionTree, Queue<Type> definitionQueue)
 	{
 		ArrayList<Token.Type> linkedList = new ArrayList<Token.Type>();
 
 		for (Iterator<Node<Token>> iter = definitionTree.iterator(); iter.hasNext();)
 		{
 			Node<Token> node = iter.next();
 			Token.Type token = node.getData().getType();
 			
 			if (token != Token.Type.VARIABLE)
 				linkedList.add(token);
 			
 			definitionQueue.add(token);
 		}
 		
 		return linkedList;
 	}
 	
	public boolean equivalent(ProofTree definitionTree)
 	{
 		Queue<Token.Type> definitionQueue = new LinkedList<Token.Type>();
 		Queue<Token.Type> useQueue = new LinkedList<Token.Type>();
 		
 		ArrayList<Token.Type> definitionOperatorQueue = getExpressionOrder(definitionTree, definitionQueue);
 		
 		int currentIndex = 0;
 		
 		for (Iterator<Node<Token>> iter = this.iterator(); iter.hasNext();)
 		{
 			Node<Token> node = iter.next();
 			Token.Type token = node.getData().getType();
 			
 			if (token != Token.Type.VARIABLE)
 			{
 				if (currentIndex >= definitionOperatorQueue.size())
 					return false;
 				
 				Token.Type definitionToken = definitionOperatorQueue.get(currentIndex);
 				if (definitionToken == token)
 				{
 					++currentIndex;
 					useQueue.add(token);
 				}
 				else
 					useQueue.add(Token.Type.VARIABLE);
 			}	
 		}
 		
 		return definitionQueue.equals(useQueue);
 	}
 	
 	public boolean equals(ProofTree pt) {
 		if (pt != null) {
 			return equals (pt.root);
 		}
 		return false;
 	}
 
 	public boolean equals(ProofNode n) {
 		if (n != null)
 			return equalsHelper((ProofNode)root, n);
 		return false;
 	}
 
 	public boolean equalsHelper(ProofNode n1, ProofNode n2) {
 		if (n1 == null ^ n2 == null) //only one is null
 			return false;
 		else if (n1 == null && n2 == null) //both are null
 			return true;
 		if (!n1.equals(n2))
 			return false;
 		boolean same = equalsHelper((ProofNode)n1.getLeft(), (ProofNode)n2.getLeft());
 		if (same)
 			return equalsHelper((ProofNode)n1.getRight(),(ProofNode)n2.getRight());
 		return false;
 	}
 	
 	/*
 	// equalsNoSign
 	public boolean equalsNoSign(Object o) {
 		if (o != null)
 			return equalsNoSign((ProofNode)((ProofTree)o).root);
 		return false;
 	}
 
 	public boolean equalsNoSign(ProofNode n) {
 		if (n != null)
 			return equalsNoSignHelper((ProofNode)root, n);
 		if(debug)
 			System.out.println("equalsNoSign NULL");
 		return false;
 	}
 
 	public boolean equalsNoSignHelper(ProofNode n1, ProofNode n2) {
 		if (!n1.equalsNoSign(n2)) {
 			if(debug)
 				System.out.println(" it is not equal sign");
 			return false;
 		}
 		boolean same = equalsHelper((ProofNode)n1.getLeft(), (ProofNode)n2.getLeft());
 		if (same)
 			return equalsHelper((ProofNode)n1.getRight(),(ProofNode)n2.getRight());
 		return false;
 	}
 	*/
 	
 	//equalsOpositeSign
 	public boolean equalsOpositeSign(ProofTree o) {
 		if (debug)
 			System.out.println("equalsOpositeSign ProofTree");
 		if (o != null)
 			return equalsOpositeSign((ProofNode)((ProofTree)o).root);
 		return false;
 	}
 
 	public boolean equalsOpositeSign(ProofNode n) {
 		if (debug)
 			System.out.println("equalsOpositeSign ProofNode");
 		if (n != null)
 			return equalsOpositeSignHelper((ProofNode)root, n);
 		if(debug)
 			System.out.println("equalsNoSign NULL");
 		return false;
 	}
 
 	public boolean equalsOpositeSignHelper(ProofNode n1, ProofNode n2) {
 		if (!n1.equalsOpositeSign(n2)) {
 			if(debug)
 				System.out.println(" it is not equal sign");
 			return false;
 		}
 		boolean same = equalsHelper((ProofNode)n1.getLeft(), (ProofNode)n2.getLeft());
 		if (same)
 			return equalsHelper((ProofNode)n1.getRight(),(ProofNode)n2.getRight());
 		return false;
 	}
 	
 /*	public boolean isEquivalent(Object o) throws MethodNotFoundException {
 		throw new MethodNotFoundException();
 	}*/
 
 }
