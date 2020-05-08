 package banjo.desugar;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import banjo.parser.ast.Atom;
 import banjo.parser.ast.BinaryOp;
 import banjo.parser.ast.BinaryOperator;
 import banjo.parser.ast.Call;
 import banjo.parser.ast.Ellipsis;
 import banjo.parser.ast.Expr;
 import banjo.parser.ast.ExprList;
 import banjo.parser.ast.Field;
 import banjo.parser.ast.FieldRef;
 import banjo.parser.ast.FunArg;
 import banjo.parser.ast.FunctionLiteral;
 import banjo.parser.ast.IdRef;
 import banjo.parser.ast.Key;
 import banjo.parser.ast.Let;
 import banjo.parser.ast.ListLiteral;
 import banjo.parser.ast.ObjectLiteral;
 import banjo.parser.ast.ParenType;
 import banjo.parser.ast.RowUpdate;
 import banjo.parser.ast.SetLiteral;
 import banjo.parser.ast.StringLiteral;
 import banjo.parser.ast.UnaryOp;
 import banjo.parser.ast.UnaryOperator;
 import banjo.parser.ast.UnitRef;
 import banjo.parser.errors.BanjoParseException;
 import banjo.parser.errors.ElseClauseNotLast;
 import banjo.parser.errors.ExpectedElement;
 import banjo.parser.errors.ExpectedExpression;
 import banjo.parser.errors.ExpectedField;
 import banjo.parser.errors.ExpectedFieldName;
 import banjo.parser.errors.ExpectedIdentifier;
 import banjo.parser.errors.ExtraTableColumn;
 import banjo.parser.errors.InvalidProjection;
 import banjo.parser.errors.MissingElseClauseInConditional;
 import banjo.parser.errors.MissingValueForTableColumn;
 import banjo.parser.errors.MixedSemicolonAndComma;
 import banjo.parser.errors.MultipleElseClausesInConditional;
 import banjo.parser.errors.UnexpectedContract;
 import banjo.parser.errors.UnsupportedBinaryOperator;
 import banjo.parser.errors.UnsupportedUnaryOperator;
 import banjo.parser.util.FileRange;
 
 public class BanjoDesugarer {
 	private final LinkedList<BanjoParseException> errors = new LinkedList<>();
 	
 	/**
 	 * Coming in we have a tree of basically just unary and binary operations and parens and atoms.  
 	 * Let's enrich that a bit so we can have ObjectLiteral, ListLiteral, Let, FunctionLiteral,
 	 * LetFun.
 	 * 
 	 * @param node
 	 * @return
 	 */
 	public Expr desugar(Expr node) {
 		if(node instanceof UnaryOp) {
 			UnaryOp op = (UnaryOp) node;
 			final Expr operand = desugar(op.getOperand());
 			switch(op.getOperator()) {
 			case LIST_ELEMENT: return new ListLiteral(op.getFileRange(), Collections.singletonList(desugar(operand)));
 			case SET_ELEMENT: return new SetLiteral(op.getFileRange(), Collections.singletonList(desugar(operand)));
 			case LAZY: return new FunctionLiteral(op.getFileRange(), Collections.<FunArg>emptyList(), null, operand);
 			case MIRROR: return objectLiteral(op.getFileRange(), Collections.singletonList(operand));
 			case PARENS: return operand;
 			case NEWLINE: return operand;
 			case LIST_LITERAL:
 				if(operand instanceof ListLiteral) {
 					return operand;
 				} else if(operand instanceof ExprList) {
 					return listLiteral(operand.getFileRange(), ((ExprList)operand).getElements(), null);
 				} else {
 					LinkedList<Expr> exprs = new LinkedList<>();
 					flattenCommasOrSemicolons(operand, exprs);
 					return listLiteral(operand.getFileRange(), exprs, null);
 				}
 			case OBJECT_OR_SET_LITERAL: // Expecting an object or set
 				if(operand instanceof ObjectLiteral) return operand;
 				if(operand instanceof SetLiteral) return operand;
 				if(operand instanceof ExprList) 
 					return new SetLiteral(node.getFileRange(), ((ExprList)operand).getElements());
 				// Singleton set literal
 				return new SetLiteral(node.getFileRange(), Collections.singletonList(operand));
 			case CALL:
 				return new Call(operand);
 			case RETURN:
 				return operand;
 			default:
 				if(op.getOperator().getMethodName() != null)
 					return new Call(new FieldRef(operand, new IdRef(op.getFileRange(), op.getOperator().getMethodName())));
 				getErrors().add(new UnsupportedUnaryOperator(op.getOperator().getOp(), op.getFileRange()));
 				return operand;
 			}
 		} else if(node instanceof BinaryOp) {
 			BinaryOp op = (BinaryOp) node;
 			// Comma outside of a parentheses should be a list or map without the braces/brackets
 			final FileRange range = op.getFileRange();
 			switch(op.getOperator()) {
 			case COMMA:
 			case SEMICOLON:
 			case NEWLINE: {
 				LinkedList<Expr> exprs = new LinkedList<>();
 				flattenCommas(op, op.getOperator(), exprs);
 				Expr first = exprs.get(0);
 				if(isTableHeader(first)) {
 					Expr second = exprs.get(1);
 					if(isPair(second) || isMirrorElement(second)) {
 						return objectLiteral(range, exprs);
 					} else if(isSetElement(second)){
 						return setLiteral(range, exprs, ((UnaryOp)second).getOperator());
 					} else if(isListElement(second)) {
 						return listLiteral(range, exprs, ((UnaryOp)second).getOperator());
 					} else {
 						return listLiteral(range, exprs, null);
 					}
 				} else if(isPair(first) || isMirrorElement(first)) {
 					return objectLiteral(range, exprs);
 				} else if(isListElement(first)) {
 					// Bulleted list item - treat as a list
 					return listLiteral(range, exprs, ((UnaryOp)first).getOperator());
 				} else if(isSetElement(first)) {
 					// Bulleted set item - treat as a list
 					return setLiteral(range, exprs, ((UnaryOp)first).getOperator());
 				} else if(isCondCase(first)) {
 					return exprListToCond(range, exprs);
 				} else {
 					// Everything else - treat as a series of steps
 					ArrayList<Expr> exprList = new ArrayList<>(exprs.size());
 					for(Expr e : exprs) {
 						exprList.add(desugar(e));
 					}
 					return new ExprList(op.getFileRange(), exprList);
 				}
 			}
 			case LAZY_AND: return lazyAnd(op);
 			case LAZY_OR: return lazyOr(op);
 			case COND: return exprListToCond(op.getFileRange(), new LinkedList<>(Collections.singletonList(node)));
 			case CALL: return call(op);
 			case FUNCTION: return functionLiteral(op);
			case PAIR:
			case PAIR2: return objectLiteral(range, Collections.<Expr>singletonList(op));
 			case ASSIGNMENT: return let(op);
 			case PROJECTION: return projection(op);
 			case MAP_PROJECTION: return optionProjection(op);
 			case OR_ELSE: return orElse(op);
 			case GT: 
 			case GE: 
 			case LT: 
 			case LE: 
 				return comparison(op); 
 				
 			// TODO Eliminate ALL binary ops as function calls
 			default:
 				if(op.getOperator().getMethodName() != null)
 					return new Call(new FieldRef(desugar(op.getLeft()), new IdRef(op.getFileRange(), op.getOperator().getMethodName())), desugar(op.getRight()));
 				getErrors().add(new UnsupportedBinaryOperator(op.getOperator().getOp(), op.getFileRange()));
 				return desugar(op.getRight());
 			}
 		} else if(node instanceof UnitRef) {
 			UnitRef u = (UnitRef) node;
 			switch(((UnitRef) node).getParenType()) {
 			case BRACES: return new ObjectLiteral(node.getFileRange(), Collections.<String,Field>emptyMap());
 			default:
 			case PARENS: getErrors().add(new ExpectedExpression(node.getFileRange(), node.toSource())); break;
 			case BRACKETS: return new ListLiteral(u.getFileRange(), Collections.<Expr>emptyList());
 			}
 		} else if(node instanceof Atom) {
 			return node;
 		} else {
 			getErrors().add(new BanjoParseException("Not implemented: "+node.getClass().getSimpleName(), node.getFileRange()));
 			return node;
 		}
 		throw new Error("Not implemented: "+node.getClass().getSimpleName()+" ("+node.toSource()+") "+node.getFileRange());
 	}
 
 	/**
 	 * Assuming that Boolean has a method <code>lazyOr(f): ifTrue(->true,f)()</code>
 	 */
 	private Expr lazyOr(BinaryOp op) {
 		// left || right = left.if(true(): true, false(): right)
 		final Expr condition = desugar(op.getLeft());
 		final FileRange range = op.getFileRange();
 		Field trueField = new Field(new IdRef(range, "false"), new FunctionLiteral(desugar(op.getRight())));
 		Field falseField = new Field(new IdRef(range, "true"), new FunctionLiteral(new IdRef(range, "false")));
 		Expr arg = new ObjectLiteral(range, trueField, falseField);
 		Expr method = new FieldRef(condition, new IdRef(range, "if"));
 		return new Call(method, arg);
 	}
 
 	/**
 	 * 
 	 */
 	private Expr lazyAnd(BinaryOp op) {
 		// left && right == left.if(true(): right, false(): false)
 		final Expr condition = desugar(op.getLeft());
 		final FileRange range = op.getFileRange();
 		Field trueField = new Field(new IdRef(range, "true"), new FunctionLiteral(desugar(op.getRight())));
 		Field falseField = new Field(new IdRef(range, "false"), new FunctionLiteral(new IdRef(range, "false")));
 		Expr arg = new ObjectLiteral(range, trueField, falseField);
 		Expr method = new FieldRef(condition, new IdRef(range, "if"));
 		return new Call(method, arg);
 	}
 
 	private Expr comparison(BinaryOp op) {
 		final Expr left = desugar(op.getLeft());
 		final Expr right = desugar(op.getRight());
 		final FileRange range = op.getFileRange();
 		Call cmp = new Call(new FieldRef(left, new IdRef(range, BinaryOperator.CMP.getMethodName())), right);
 		boolean checkEqual;
 		String checkField;
 		switch(op.getOperator()) {
 		case GT: checkEqual = true; checkField = "greater"; break;
 		case GE: checkEqual = false; checkField = "less"; break;
 		case LT: checkEqual = true; checkField = "less"; break;
 		case LE: checkEqual = false; checkField = "greater"; break;
 		default: throw new Error();
 		}
 		Expr check = new Call(new FieldRef(cmp, new IdRef(range, checkField)));
 		if(checkEqual)
 			return check;
 		else
 			return new Call(new FieldRef(check, new IdRef(range, UnaryOperator.NOT.getMethodName())));
 	}
 
 	private Expr projection(BinaryOp op) {
 		Expr base = desugar(op.getLeft());
 		Expr projection = desugar(op.getRight());
 		if(projection instanceof StringLiteral) {
 			return new FieldRef(base, (StringLiteral) projection);
 		} else if(projection instanceof IdRef) {
 			return new FieldRef(base, (IdRef) projection);
 		} else if(projection instanceof ObjectLiteral) {
 			return new RowUpdate(op.getFileRange(), base, (ObjectLiteral)projection);
 		} else if(projection instanceof SetLiteral) {
 			SetLiteral fieldSet = (SetLiteral) projection;
 			LinkedHashMap<String,Field> fields = new LinkedHashMap<>(fieldSet.getElements().size());
 			for(Expr e : fieldSet.getElements()) {
 				Key key;
 				
 				if(e instanceof IdRef) {
 					key = (IdRef)e;
 				} else if(e instanceof StringLiteral) {
 					key = (StringLiteral)e;
 				} else {
 					getErrors().add(new ExpectedIdentifier(e));
 					continue;
 				}
 				fields.put(key.getKeyString(), new Field(key, new FieldRef(base, key)));
 				
 			}
 			return new ObjectLiteral(op.getFileRange(), fields);
 		} else {
 			getErrors().add(new InvalidProjection(projection));
 			return base;
 		}
 	}
 	
 	
 	// Optional projection: x?.foo == x.map((x) -> x.foo)
 	private Expr optionProjection(BinaryOp op) {
 		final FileRange r = op.getFileRange();
 		final IdRef argName = gensym(r);
 		final Expr projection = projection(new BinaryOp(BinaryOperator.PROJECTION, argName, op.getRight()));
 		Expr projectFunc = new FunctionLiteral(new FunArg(argName), projection);
 		final Expr left = desugar(op.getLeft());
 		Expr mapCall = new Call(new FieldRef(left, new IdRef(r, "map")), projectFunc);
 		return mapCall;
 	}
 	
 	// orElse: x ?: y == x.orElse(-> y)
 	private Expr orElse(BinaryOp op) {
 		final FileRange r = op.getFileRange();
 		final Expr left = desugar(op.getLeft());
 		final Expr right = desugar(op.getRight());
 		return new Call(new FieldRef(left, new IdRef(r, "valueOrElse")), new FunctionLiteral(right));
 	}
 
 	int gensymCounter = 0;
 	private IdRef gensym(FileRange r) {
 		gensymCounter++;
 		return new IdRef(r, "__t"+gensymCounter);
 	}
 
 	private Expr call(BinaryOp op) {
 		List<Expr> args;
 		Expr callee = desugar(op.getLeft());
 		Expr arg = desugar(op.getRight());
 		if(arg instanceof ExprList) {
 			args = ((ExprList) arg).getElements();
 		} else {
 			args = Collections.singletonList(arg);
 		}
 		return new Call(op.getFileRange(), callee, args);
 	}
 
 	private Expr let(BinaryOp op) {
 		Expr target = op.getLeft();
 		Expr value = desugar(op.getRight());
 		Expr contract = null;
 		String name = null;
 		FileRange nameRange = null;
 		if(isPair(target)) {
 			final BinaryOp targetBOp = (BinaryOp)target;
 			target = targetBOp.getLeft();
 			contract = targetBOp.getRight();
 		}
 		if(isCallWithArgs(target)) {
 			BinaryOp call = (BinaryOp) target;
 			value = enrichFunctionLiteral(op.getFileRange(), call.getRight(), contract, value);
 			target = call.getLeft();
 			contract = null;
 		} else if(target instanceof UnaryOp && ((UnaryOp) target).getOperator() == UnaryOperator.CALL) {
 			UnaryOp call = (UnaryOp) target;
 			value = new FunctionLiteral(op.getFileRange(), Collections.<FunArg>emptyList(), contract, value);
 			target = call.getOperand();
 			contract = null;
 		}
 		if(target instanceof IdRef) {
 			IdRef id = (IdRef) target;
 			name = id.getId();
 			nameRange = id.getFileRange();
 			if(contract != null) {
 				getErrors().add(new UnexpectedContract(contract));
 			}
 			return new Let(nameRange, name, value);
 		} else {
 			getErrors().add(new ExpectedIdentifier(target));
 			return op;
 		}
 	}
 
 	private boolean isCallWithArgs(Expr target) {
 		return target instanceof BinaryOp && ((BinaryOp)target).getOperator() == BinaryOperator.CALL;
 	}
 
 	/**
 	 * Requires Boolean to have a method <code>lazyIfTrue(trueFunc,falseFunc): ifTrue(trueFunc,falseFunc)()</code>
 	 */
 	private Expr exprListToCond(FileRange range, LinkedList<Expr> exprs) {
 		Expr result = null;
 		boolean missingElseClause = false;
 		Expr duplicateElseClause = null;
 		for(Iterator<Expr> it = exprs.descendingIterator(); it.hasNext(); ) {
 			Expr e = it.next();
 			if(isCondCase(e)) {
 				BinaryOp caseOp = (BinaryOp)e;
 				final Expr thenExpr = desugar(caseOp.getRight());
 				if(caseOp.getLeft() instanceof Ellipsis) {
 					if(result != null) {
 						duplicateElseClause = e;
 					}
 					result = thenExpr;
 				} else {
 					if(result == null) {
 						missingElseClause = true;
 						result = new UnitRef(range);
 					}
 					
 					final Expr condition = desugar(caseOp.getLeft());
 					Field trueField = new Field(new IdRef(e.getFileRange(), "true"), new FunctionLiteral(thenExpr));
 					Field falseField = new Field(new IdRef(e.getFileRange(), "false"), new FunctionLiteral(result));
 					Expr arg = new ObjectLiteral(e.getFileRange(), trueField, falseField);
 					Expr ifMethod = new FieldRef(condition, new IdRef(e.getFileRange(), "if"));
 					result = new Call(ifMethod, arg);
 				}
 			} else {
 				if(result != null) {
 					duplicateElseClause = e;
 				}
 				result = e;
 			}
 		}
 		
 		if(duplicateElseClause != null) {
 			if(missingElseClause) {
 				// Else clause too early
 				getErrors().add(new ElseClauseNotLast(duplicateElseClause));
 			} else {
 				getErrors().add(new MultipleElseClausesInConditional(duplicateElseClause));
 			}
 		} else if(missingElseClause) {
 			getErrors().add(new MissingElseClauseInConditional(range));
 		}
 		
 		return result;
 	}
 
 	private final boolean isCondCase(Expr e) {
 		return (e instanceof BinaryOp) && ((BinaryOp)e).getOperator() == BinaryOperator.COND;
 	}
 
 	private Expr listLiteral(final FileRange range, List<Expr> list, UnaryOperator requireBullet) {
 		return new ListLiteral(range, elements(list, requireBullet));
 	}
 
 	private Expr setLiteral(final FileRange range, List<Expr> list, UnaryOperator requireBullet) {
 		return new SetLiteral(range, elements(list, requireBullet));
 	}
 
 	private ArrayList<Expr> elements(List<Expr> list, UnaryOperator requireBullet) {
 		ArrayList<Expr> elements = new ArrayList<>();
 		List<Expr> headings = null;
 		if(!list.isEmpty()) {
 			for(Expr e : list) {
 				if(isTableHeader(e)) {
 					final UnaryOp headerOp = (UnaryOp)e;
 					headings = flattenCommasOrSemicolons(headerOp.getOperand(), new ArrayList<Expr>());
 					continue;
 				}
 				Expr eltExpr;
 				if(requireBullet != null && isUnaryOp(e, requireBullet)) {
 					eltExpr = ((UnaryOp)e).getOperand();
 				} else {
 					if(requireBullet != null) {
 						// TODO Wrong error
 						getErrors().add(new ExpectedElement(e.getFileRange()));
 					}
 					eltExpr = e;
 				}
 				if(headings != null) {
 					elements.add(makeRow(headings, eltExpr));
 				} else {
 					elements.add(desugar(eltExpr));
 				}
 			}
 		}
 		return elements;
 	}
 
 	private Expr objectLiteral(FileRange range, Collection<Expr> pairs) {
 		// Key/value pair - treat as an object
 		LinkedHashMap<String, Field> fields = new LinkedHashMap<>(pairs.size()*2);
 		List<Expr> headings = null;
 		for(Expr e : pairs) {
 			Expr keyExpr;
 			Expr valueExpr;
 			if(isPair(e)) {
 				final BinaryOp fieldOp = (BinaryOp)e;
 				keyExpr = fieldOp.getLeft();
 				valueExpr = fieldOp.getRight();
 			} else if(isMirrorElement(e)) {
 				final UnaryOp fieldOp = (UnaryOp)e;
 				keyExpr = valueExpr = fieldOp.getOperand();
 			} else if(isTableHeader(e)) {
 				final UnaryOp headerOp = (UnaryOp)e;
 				headings = flattenCommasOrSemicolons(headerOp.getOperand(), new ArrayList<Expr>());
 				continue;
 			} else {
 				getErrors().add(new ExpectedField(e.getFileRange()));
 				continue;
 			}
 			Expr contract = null;
 			if(isPair(keyExpr)) {
 				final BinaryOp targetBOp = (BinaryOp)keyExpr;
 				keyExpr = targetBOp.getLeft();
 				contract = targetBOp.getRight();
 			}
 			Expr value;
 			if(isCallWithArgs(keyExpr)) {
 				BinaryOp call = (BinaryOp) keyExpr;
 				value = makeFunctionLiteral(e.getFileRange(), flattenCommasOrSemicolons(call.getRight(), new ArrayList<Expr>()), valueExpr, contract);
 				keyExpr = call.getLeft();
 				contract = null;
 			} else if(headings != null) {
 				// If this is a table, construct the row
 				value = makeRow(headings, valueExpr);
 			} else {
 				value = desugar(valueExpr);
 			}
 			if(contract != null) {
 				getErrors().add(new UnexpectedContract(contract));
 			}
 			Key key;
 			if(keyExpr instanceof IdRef) {
 				key = (IdRef)keyExpr;
 			} else if(keyExpr instanceof StringLiteral) {
 				key = (StringLiteral)keyExpr;
 			} else {
 				getErrors().add(new ExpectedFieldName("Expected identifier or string; got "+keyExpr.getClass().getSimpleName()+" '"+keyExpr.toSource()+"'", keyExpr.getFileRange()));
 				continue;
 			}
 			
 			fields.put(key.getKeyString(), new Field(key, value));
 		}
 		return new ObjectLiteral(range, fields);
 	}
 
 	private Expr makeRow(List<Expr> headings, Expr valueExpr) {
 		List<Expr> values = flattenCommasOrSemicolons(stripParens(valueExpr), new ArrayList<Expr>(headings.size()));
 		for(int i=headings.size(); i < values.size(); i++) {
 			final Expr val = values.get(i);
 			getErrors().add(new ExtraTableColumn(i, headings.size(), val.toSource(), val.getFileRange()));
 		}
 		if(values.size() < headings.size()) {
 			getErrors().add(new MissingValueForTableColumn(values.size(), headings.size(), valueExpr.getFileRange(), headings.get(values.size()).toSource()));
 		}
 		ArrayList<Expr> fieldOps = new ArrayList<>(headings.size());
 		for(int i=0; i < values.size(); i++) {
 			fieldOps.add(new BinaryOp(BinaryOperator.PAIR, headings.get(i), values.get(i)));
 		}
 		valueExpr = objectLiteral(valueExpr.getFileRange(), fieldOps);
 		return valueExpr;
 	}
 
 	/**
 	 * If the expression is wrapped in parenthesis, remove them.
 	 * @param valueExpr
 	 * @return
 	 */
 	private Expr stripParens(Expr valueExpr) {
 		if(isUnaryOp(valueExpr, UnaryOperator.PARENS))
 			return ((UnaryOp)valueExpr).getOperand();
 		return valueExpr;
 	}
 
 	private Expr functionLiteral(BinaryOp op) {
 		Expr argsDef = op.getLeft();
 		final Expr body = op.getRight();
 		FileRange range = op.getFileRange();
 		return enrichFunctionLiteral(argsDef, body, range);
 	}
 
 	private Expr enrichFunctionLiteral(Expr argsDef, final Expr body,
 			FileRange range) {
 		Expr returnContract = null;
 		// Optional return type/contract
 		if(isPair(argsDef)) {
 			returnContract = ((BinaryOp)argsDef).getRight();
 			argsDef = ((BinaryOp)argsDef).getLeft();
 		}
 		return enrichFunctionLiteral(range, argsDef, returnContract, body);
 	}
 
 	private Expr enrichFunctionLiteral(FileRange range, Expr args,
 			Expr contract, final Expr body) {
 		// Args should be comma-separated
 		List<Expr> exprs = new LinkedList<>();
 		// Optional parentheses
 		if((args instanceof UnaryOp) && ((UnaryOp)args).getOperator().getParenType() == ParenType.PARENS) {
 			args = ((UnaryOp)args).getOperand();
 		}
 		if(args instanceof ExprList) {
 			exprs = ((ExprList)args).getElements();
 		} else if(args instanceof UnitRef) {
 			// Leave the list empty
 		} else {
 			flattenCommas(args, BinaryOperator.COMMA, exprs);
 		}
 		return makeFunctionLiteral(range, exprs, body, contract);
 	}
 
 	private Expr makeFunctionLiteral(FileRange range, List<Expr> exprs,
 			final Expr body, Expr returnContract) {
 		List<FunArg> args = exprs.isEmpty() ? Collections.<FunArg>emptyList() : new ArrayList<FunArg>(exprs.size());
 		for(Expr argExpr : exprs) {
 			String name = null;
 			Expr nameExpr = argExpr;
 			Expr contract = null;
 			if(isPair(nameExpr)) {
 				nameExpr = ((BinaryOp) nameExpr).getLeft();
 				contract = ((BinaryOp) nameExpr).getRight();
 			}
 			if(nameExpr instanceof IdRef) {
 				name = ((IdRef) nameExpr).getId();
 			} else {
 				getErrors().add(new ExpectedIdentifier(nameExpr));
 				continue;
 			}
 			args.add(new FunArg(nameExpr.getFileRange(), name, contract));
 		}
 		return new FunctionLiteral(range, args, returnContract, body);
 	}
 
 	private boolean isListElement(Expr e) {
 		return isUnaryOp(e, UnaryOperator.LIST_ELEMENT);
 	}
 	private boolean isSetElement(Expr e) {
 		return isUnaryOp(e, UnaryOperator.SET_ELEMENT);
 	}
 
 	private boolean isPair(Expr e) {
 		return (e instanceof BinaryOp) && 
				(((BinaryOp) e).getOperator() == BinaryOperator.PAIR ||
				 ((BinaryOp) e).getOperator() == BinaryOperator.PAIR2);
 	}
 	private boolean isMirrorElement(Expr e) {
 		return isUnaryOp(e, UnaryOperator.MIRROR);
 	}
 	private boolean isTableHeader(Expr e) {
 		return isUnaryOp(e, UnaryOperator.TABLE_HEADER);
 	}
 
 	private boolean isUnaryOp(Expr e, final UnaryOperator operator) {
 		return (e instanceof UnaryOp) && ((UnaryOp) e).getOperator() == operator;
 	}
 
 	private void flattenCommas(Expr arg, BinaryOperator type, List<Expr> exprs) {
 		if(arg instanceof BinaryOp) {
 			BinaryOp bop = (BinaryOp) arg;
 			if(isElementSeparator(bop)) {
 				final BinaryOperator boper = bop.getOperator();
 				if(boper != type && bop.getOperator() != BinaryOperator.NEWLINE) {
 					if(type == BinaryOperator.NEWLINE) type = bop.getOperator();
 					else getErrors().add(new MixedSemicolonAndComma(bop));
 				}
 				flattenCommas(bop.getLeft(), type, exprs);
 				flattenCommas(bop.getRight(), type, exprs);
 				return;
 			}
 		}
 		exprs.add(arg);
 	}
 
 	private List<Expr> flattenCommasOrSemicolons(Expr arg, List<Expr> list) {
 		if(arg instanceof BinaryOp) {
 			BinaryOp op = (BinaryOp) arg;
 			if(isElementSeparator(op)) {
 				flattenCommas(op, op.getOperator(), list);
 				return list;
 			}
 		}
 		list.add(arg);
 		return list;
 	}
 
 	private static boolean isElementSeparator(BinaryOp op) {
 		return isElementSeparator(op.getOperator());
 	}
 
 	private static boolean isElementSeparator(final BinaryOperator operator) {
 		return operator == BinaryOperator.COMMA || operator == BinaryOperator.SEMICOLON || operator == BinaryOperator.NEWLINE;
 	}
 	
 	public LinkedList<BanjoParseException> getErrors() {
 		return errors;
 	}
 
 
 }
