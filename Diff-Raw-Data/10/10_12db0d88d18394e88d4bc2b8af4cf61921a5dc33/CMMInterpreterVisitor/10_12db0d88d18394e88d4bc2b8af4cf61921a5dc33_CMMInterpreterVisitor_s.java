 
 
 public class CMMInterpreterVisitor implements
 		CMMVisitor<CMMData, CMMEnvironment> {
 
 	/**
 	 * Environment keeps track of variable bindings
 	 */
 	protected CMMEnvironment env;
 	
 	public CMMInterpreterVisitor() {
 		env = new CMMEnvironment();
 	}
 
 	public CMMData visit(CMMASTNode node, CMMEnvironment data) {
 		return null;
 	}
 	
 	public CMMData visit(CMMASTProgramNode node, CMMEnvironment data) {
 		return visitChildren(node, data);
 	}
 
 	// FunctionDefinition -> Type id ParameterList Block
 	public CMMData visit(CMMASTFunctionDefinitionNode node, CMMEnvironment data) {
 		String id = node.getChild(1).getValue();
 		env.bind(id, new CMMFunction(node));
 		if (id.equals("main")) {
 			return node.getChild(3).accept(this, data);
 		}
 		return null;
 	}
 
 	public CMMData visit(CMMASTParameterListNode node, CMMEnvironment data) {
 		return null;
 	}
 
 	public CMMData visit(CMMASTParameterNode node, CMMEnvironment data) {
 		return null;
 	}
 
 	public CMMData visit(CMMASTElementNode node, CMMEnvironment data) {
 		return visitChildren(node, data);
 	}
 
 	public CMMData visit(CMMASTExpressionListNode node, CMMEnvironment data) {
 		return visitChildren(node, data);
 	}
 
 	public CMMData visit(CMMASTSimpleStatementNode node, CMMEnvironment data) {
 		return visitChildren(node, data);
 	}
 
 	public CMMData visit(CMMASTConstantNode node, CMMEnvironment data) {
 		return visitChildren(node, data);
 	}
 
 	// Assignment -> Logical (gets Logical)?
 	public CMMData visit(CMMASTAssignmentNode node, CMMEnvironment data) {
 		if (node.numChildren() > 1) {
 			CMMASTNode n = node.getChild(0);  // Element
 			if (!n.getName().equals("Element") || n.numChildren() != 1)
 				throw new RuntimeException("Assigning to non-lvalue");
 			n = n.getChild(0);   // ElementPlus
 			if (!n.getName().equals("ElementPlus") || n.numChildren() != 1) 
 				throw new RuntimeException("Assigning to non-lvalue");
 			n = n.getChild(0);   // Token
 			if (!n.getName().equals("id"))
 				throw new RuntimeException("Assigning to non-lvalue");
 			String id = n.getValue();
 			if (env.lookup(id) == null)
 				throw new RuntimeException("Assigning to undeclared variable " + id);
 			CMMData res = node.getChild(2).accept(this, data);
 			if (res.getClass() != env.lookup(id).getClass()) 
 				throw new RuntimeException("Type mismatch on assignment " 
 						+ res.getClass() + " vs. " + env.lookup(id).getClass());
 			env.assign(id, res);
 			return res; 
 		} else {
 			return visitChildren(node, data);
 		}
 	}
 
 	// Logical -> Comparison ((and|or) Comparison)*  [>1]
 	public CMMData visit(CMMASTLogicalNode node, CMMEnvironment data) {
 		CMMData x = node.getChild(0).accept(this, data);
 		if (!(x instanceof CMMBoolean)) {
 			throw new RuntimeException("Invalid operand to logical operator");
 		}
 		CMMBoolean a = (CMMBoolean)x;
 		for (int i = 1; i < node.numChildren(); i += 2) {
 			CMMData y = node.getChild(i+1).accept(this, data);
 			String op = node.getChild(i).getName();
 			if (!(y instanceof CMMBoolean)) {
 				throw new RuntimeException("Invalid operand to logical operator");
 			}
 			CMMBoolean b = (CMMBoolean)y;
 			if (op.equals("and")) {
 				a = new CMMBoolean(a.value() && b.value());
 			} else if (op.equals("or")) {
 				a = new CMMBoolean(a.value() || b.value());
 			} else {
 				throw new RuntimeException("Unknown operator:" + op);
 			}
 		}
 		return a;
 	}
 
 	// Comparison -> Sum ((lt|gt|eq|le|ge|ne) Sum)?  [>1]
 	public CMMData visit(CMMASTComparisonNode node, CMMEnvironment data) {
 		CMMData x = node.getChild(0).accept(this, data);
 		CMMData y = node.getChild(2).accept(this, data);
 		if (!(x instanceof CMMNumber) || !(y instanceof CMMNumber)) {
 			throw new RuntimeException("Invalid operand to comparison operator");
 		}
 		CMMNumber a = (CMMNumber)x;
 		CMMNumber b = (CMMNumber)y;
 		String op = node.getChild(1).getName();
 		if (op.equals("lt")) {
 			return new CMMBoolean(a.value < b.value);
 		} else if (op.equals("gt")) {
 			return new CMMBoolean(a.value > b.value);
 		} else if (op.equals("le")) {
 			return new CMMBoolean(a.value <= b.value);
 		} else if (op.equals("ge")) {
 			return new CMMBoolean(a.value >= b.value);
 		} else if (op.equals("eq")) {
 			return new CMMBoolean(a.value == b.value);
 		} else if (op.equals("ne")) {
 			return new CMMBoolean(a.value != b.value);
 		} else {
 			throw new RuntimeException("Unknown operator:" + op);
 		}
 	}
 
 	// Sum -> Term ((plus|minus) Term)*  [>1]
 	public CMMData visit(CMMASTSumNode node, CMMEnvironment data) {
 		CMMData x = node.getChild(0).accept(this, data);
 		if (!(x instanceof CMMNumber)) {
 			throw new RuntimeException("Invalid operand to numerical operator");
 		}
 		CMMNumber a = (CMMNumber)x;
 		for (int i = 1; i < node.numChildren(); i += 2) {
 			CMMData y = node.getChild(i+1).accept(this, data);
 			String op = node.getChild(i).getName();
 			if (!(y instanceof CMMNumber)) {
 				throw new RuntimeException("Invalid operand to numerical operator +/-");
 			}
 			CMMNumber b = (CMMNumber)y;
 			if (op.equals("plus")) {
 				a = new CMMNumber(a.value() + b.value());
 			} else if (op.equals("minus")) {
 				a = new CMMNumber(a.value() - b.value());
 			} else {
 				throw new RuntimeException("Unknown operator:" + op);
 			}
 		}
 		return a;
 	}
 
 	// Term -> Exp ((multiply|divide|mod) Exp)* [>1]
 	public CMMData visit(CMMASTTermNode node, CMMEnvironment data) {
 		CMMData x = node.getChild(0).accept(this, data);
 		if (!(x instanceof CMMNumber)) {
 			throw new RuntimeException("Invalid operand to numerical operator +/-");
 		}
 		CMMNumber a = (CMMNumber)x;
 		for (int i = 1; i < node.numChildren(); i += 2) {
 			CMMData y = node.getChild(i+1).accept(this, data);
 			String op = node.getChild(i).getName();
 			if (!(y instanceof CMMNumber)) {
 				throw new RuntimeException("Invalid operand to numerical operator +/-");
 			}
 			CMMNumber b = (CMMNumber)y;
 			if (op.equals("multiply")) {
 				a = new CMMNumber(a.value() * b.value());
 			} else if (op.equals("divide")) {
 				a = new CMMNumber(a.value() / b.value());
 			} else if (op.equals("mod")) {
 				a = new CMMNumber(a.value() % b.value());
 			} else {
 				throw new RuntimeException("Unknown operator:" + op);
 			}
 		}
 		return a;
 	}
 
 	// Exp -> Element (exp Element)*  [>1] 
 	public CMMData visit(CMMASTExpNode node, CMMEnvironment data) {
 		CMMData x = node.getChild(0).accept(this, data);
 		if (!(x instanceof CMMNumber)) {
 			throw new RuntimeException("Invalid operand to numeric operator");
 		}
 		CMMNumber a = (CMMNumber)x;
 		for (int i = 1; i < node.numChildren(); i += 2) {
 			CMMData y = node.getChild(i+1).accept(this, data);
 			String op = node.getChild(i).getName();
 			if (!(y instanceof CMMNumber)) {
 				throw new RuntimeException("Invalid operand to numeric operator");
 			}
 			CMMNumber b = (CMMNumber)y;
 			if (op.equals("exp")) {
 				a = new CMMNumber(Math.pow(a.value(), b.value()));
 			} else {
 				throw new RuntimeException("Unknown operator:" + op);
 			}
 		}
 		return a;
 	}
 
 	// ElementPlus -> id ArgumentList?
 	public CMMData visit(CMMASTElementPlusNode node, CMMEnvironment data) {
 		if (node.numChildren() == 1) { // just an identifier
 			return node.getChild(0).accept(this, data); 
 		} else { // a function call
 			String fname = node.getChild(0).getValue();
 			if (fname.equals("print")) {
 				CMMData res = visitChildren(node.getChild(1), data);
 				System.out.print(res);
 				return res;
 			}
 			if (fname.equals("println")) {
 				CMMData res = visitChildren(node.getChild(1), data);
 				System.out.println(res);
 				return res;
 			}
 			CMMData f = env.lookup(fname);
 			if (!(f instanceof CMMFunction)) {
 				throw new RuntimeException("Attempt to call non-function "+ fname);
 			}
 			CMMFunction fn = (CMMFunction)f;
 			env.pushFrame(); // add a frame for the parameters
 			env.bind("11this", fn);
 			node.getChild(1).accept(this, data);
 			CMMData res = fn.value().getChild(3).accept(this, data);  // visit the block now
 			if (res == null)
 				throw new RuntimeException("Function not returning a value " + fname);
 			// TODO: typecheck return value
 			env.popFrame();
 			return res;
 		}
 	}
 
 	// ArgumentList -> lparen (Assignment (listsep Assignment)*)? rparen
 	public CMMData visit(CMMASTArgumentListNode node, CMMEnvironment data) {
 		CMMFunction fn = (CMMFunction)env.lookup("11this");
 		CMMASTParameterListNode pl = (CMMASTParameterListNode)fn.value().getChild(2);
 		if (pl.numChildren() != node.numChildren()) {
 			throw new RuntimeException("Calling function with wrong number of arguments");
 		}
 		for (int i = 1; i < node.numChildren()-1; i += 2) {
 			CMMData value = node.getChild(i).accept(this, data);
 			String id = pl.getChild(i).getChild(1).getValue();
 			env.bind(id, value);
 		}
 		return null;
 	}
 
 	// WhileLoop -> while Condition Block
 	public CMMData visit(CMMASTWhileLoopNode node, CMMEnvironment data) {
 		CMMData cont = node.getChild(1).accept(this, data);
 		if (!(cont instanceof CMMBoolean)) {
 			throw new RuntimeException("Invalid (non-boolean) condition in while loop");
 		}
 		CMMData res = null;
 		CMMBoolean cb = (CMMBoolean)cont;
 		while (cb.value()) {
 			res = node.getChild(2).accept(this, data);
 			cb = (CMMBoolean)node.getChild(1).accept(this, data);
 		}
 		return res;
 	}
 
 	public CMMData visit(CMMASTConditionNode node, CMMEnvironment data) {
 		return visitChildren(node, data);
 	}
 
 	// DoLoop -> do Block while Condition eol
 	public CMMData visit(CMMASTDoLoopNode node, CMMEnvironment data) {
 		throw new UnsupportedOperationException();
 	}
 
 
	@Override
 	public CMMData visit(CMMASTStatementNode node, CMMEnvironment data) {
 		return visitChildren(node, data);
 	}
 
	@Override
 	public CMMData visit(CMMASTTypeNode node, CMMEnvironment data) {
 		return null;
 	}
 
	@Override
 	public CMMData visit(CMMASTIfStatementNode node, CMMEnvironment data) {
 		throw new UnsupportedOperationException();
 	}
 
	@Override
 	/*
 	 * Declaration -> Type Identifier (listsep Identifier)* eol
 	 */
 	public CMMData visit(CMMASTDeclarationNode node, CMMEnvironment data) {
 		CMMASTNode type = node.getChild(0);
 		String stype = type.getChild(0).getName();
 		if (stype.equals("number_t")) {
 			for (int i = 1; i < node.numChildren(); i += 2)
 				env.bind(node.getChild(i).getValue(), new CMMNumber(0));
 		} else if (stype.equals("string_t")) {
 			for (int i = 1; i < node.numChildren(); i += 2)
 				env.bind(node.getChild(i).getValue(), new CMMString(""));			
 		} else if (stype.equals("boolean_t")) {
 			for (int i = 1; i < node.numChildren(); i += 2)
 				env.bind(node.getChild(i).getValue(), new CMMBoolean(false));						
 		}
 		return null;
 	}
 
 
 	public CMMData visit(CMMASTBlockNode node, CMMEnvironment data) {
 		env.pushFrame();
 		CMMData res = visitChildren(node, data);
 		env.popFrame();
 		return res;
 	}
 
 	public CMMData visit(CMMASTToken node, CMMEnvironment data) {
 		if (node.getName().equals("number")) {
 			return new CMMNumber(Double.parseDouble(node.getValue()));
 		} else if (node.getName().equals("string")) {
 			return new CMMString(node.getValue());
 		} else if (node.getName().equals("boolean")) {
 			return new CMMBoolean(Boolean.parseBoolean(node.getValue()));			
 		} else if (node.getName().equals("id")) {
 			String id = node.getValue();
 			if (env.lookup(id) == null)
 				throw new RuntimeException("Reference to undefined variable " + id);
 			return env.lookup(id);
 		}
 		return null;
 	}
 	
 	protected CMMData visitChildren(CMMASTNode node, CMMEnvironment data) {
 		CMMData last = null;
 		for (int i = 0; i < node.numChildren(); i++) {
 			CMMData tmp = node.getChild(i).accept(this, data);
 			if (tmp != null) last = tmp;
 		}
 		return last;	
 	}
 }
