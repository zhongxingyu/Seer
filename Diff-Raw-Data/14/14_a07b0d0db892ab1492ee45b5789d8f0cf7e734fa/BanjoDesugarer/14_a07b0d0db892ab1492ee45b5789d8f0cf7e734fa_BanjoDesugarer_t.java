 package banjo.desugar;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.jdt.annotation.Nullable;
 
 import banjo.desugar.errors.ElseClauseNotLast;
 import banjo.desugar.errors.ExpectedField;
 import banjo.desugar.errors.ExpectedFieldName;
 import banjo.desugar.errors.ExtraTableColumn;
 import banjo.desugar.errors.InvalidProjection;
 import banjo.desugar.errors.MissingElseClauseInConditional;
 import banjo.desugar.errors.MissingValueForTableColumn;
 import banjo.desugar.errors.MixedSemicolonAndComma;
 import banjo.desugar.errors.MultipleElseClausesInConditional;
 import banjo.desugar.errors.UnexpectedEllipsis;
 import banjo.dom.AbstractOp;
 import banjo.dom.BinaryOp;
 import banjo.dom.Call;
 import banjo.dom.Comment;
 import banjo.dom.CoreExpr;
 import banjo.dom.CoreExprVisitorWithDefault;
 import banjo.dom.Ellipsis;
 import banjo.dom.Expr;
 import banjo.dom.ExprList;
 import banjo.dom.Field;
 import banjo.dom.FieldRef;
 import banjo.dom.FunArg;
 import banjo.dom.FunctionLiteral;
 import banjo.dom.Identifier;
 import banjo.dom.Key;
 import banjo.dom.Let;
 import banjo.dom.ListLiteral;
 import banjo.dom.NumberLiteral;
 import banjo.dom.ObjectLiteral;
 import banjo.dom.Operator;
 import banjo.dom.OperatorRef;
 import banjo.dom.SetLiteral;
 import banjo.dom.SourceExpr;
 import banjo.dom.SourceExprVisitor;
 import banjo.dom.StringLiteral;
 import banjo.dom.UnaryOp;
 import banjo.dom.UnitRef;
 import banjo.dom.Whitespace;
 import banjo.parser.errors.BanjoParseException;
 import banjo.parser.errors.ExpectedElement;
 import banjo.parser.errors.ExpectedExpression;
 import banjo.parser.errors.ExpectedIdentifier;
 import banjo.parser.errors.UnsupportedBinaryOperator;
 import banjo.parser.errors.UnsupportedUnaryOperator;
 import banjo.parser.util.FileRange;
 import fj.data.Option;
 
 public class BanjoDesugarer implements SourceExprVisitor<CoreExpr> {
 	private final LinkedList<BanjoParseException> errors = new LinkedList<>();
 	
 	/**
 	 * Coming in we have a tree of basically just unary and binary operations and parens and atoms.  
 	 * Let's enrich that a bit so we can have ObjectLiteral, ListLiteral, Let, FunctionLiteral,
 	 * LetFun.
 	 * 
 	 * @param node
 	 * @return
 	 */
 	public CoreExpr desugar(SourceExpr node) {
 		final CoreExpr result = node.acceptVisitor(this);
 		if(result == null) throw new NullPointerException();
 		return result;
 	}
 
 	/**
 	 * Assuming that Boolean has a method <code>lazyOr(f): ifTrue(->true,f)()</code>
 	 */
 	private CoreExpr lazyOr(BinaryOp op) {
 		// left || right = left.if(true(): true, false(): right)
 		final CoreExpr condition = desugar(op.getLeft());
 		final FileRange range = op.getFileRange();
 		Field trueField = new Field(new Identifier(range, "false"), new FunctionLiteral(desugar(op.getRight())));
 		Field falseField = new Field(new Identifier(range, "true"), new FunctionLiteral(new Identifier(range, "false")));
 		CoreExpr arg = new ObjectLiteral(range, trueField, falseField);
 		CoreExpr method = new FieldRef(condition, new Identifier(range, "if"));
 		return new Call(method, arg);
 	}
 
 	/**
 	 * 
 	 */
 	private CoreExpr lazyAnd(BinaryOp op) {
 		// left && right == left.if(true(): right, false(): false)
 		final CoreExpr condition = desugar(op.getLeft());
 		final FileRange range = op.getFileRange();
 		Field trueField = new Field(new Identifier(range, "true"), new FunctionLiteral(desugar(op.getRight())));
 		Field falseField = new Field(new Identifier(range, "false"), new FunctionLiteral(new Identifier(range, "false")));
 		CoreExpr arg = new ObjectLiteral(range, trueField, falseField);
 		CoreExpr method = new FieldRef(condition, new Identifier(range, "if"));
 		return new Call(method, arg);
 	}
 
 	private CoreExpr comparison(BinaryOp op) {
 		final CoreExpr left = desugar(op.getLeft());
 		final CoreExpr right = desugar(op.getRight());
 		final FileRange range = op.getFileRange();
 		Call cmp = new Call(new FieldRef(left, new Identifier(range, Operator.CMP.getMethodName().some())), right);
 		boolean checkEqual;
 		String checkField;
 		switch(op.getOperator()) {
 		case GT: checkEqual = true; checkField = "greater"; break;
 		case GE: checkEqual = false; checkField = "less"; break;
 		case LT: checkEqual = true; checkField = "less"; break;
 		case LE: checkEqual = false; checkField = "greater"; break;
 		default: throw new Error();
 		}
 		CoreExpr check = new Call(new FieldRef(cmp, new Identifier(range, checkField)));
 		if(checkEqual)
 			return check;
 		else
 			return new Call(new FieldRef(check, new Identifier(range, Operator.NOT.getMethodName().some())));
 	}
 
 	private CoreExpr projection(BinaryOp op) {
 		return projection(op.getFileRange(), op.getLeft(), op.getRight());
 	}
 
 	private CoreExpr projection(final FileRange fileRange,
 			final SourceExpr left, final SourceExpr right) {
 		final CoreExpr base = desugar(left);
 		final CoreExpr projection = desugar(right);
 		final CoreExpr result = projection.acceptVisitor(new CoreExprVisitorWithDefault<CoreExpr>() {
 			@Override
 			public CoreExpr visitStringLiteral(StringLiteral stringLiteral) {
 				return new FieldRef(base, stringLiteral);
 			}
 			@Override
 			public CoreExpr visitObjectLiteral(ObjectLiteral objectLiteral) {
 				
 				return new Call(new FieldRef(base, new Identifier(fileRange, "extend")), objectLiteral);
 			}
 			
 			@Override
 			public CoreExpr visitSetLiteral(SetLiteral setLiteral) {
 				SetLiteral fieldSet = (SetLiteral) projection;
 				LinkedHashMap<String,Field> fields = new LinkedHashMap<>(fieldSet.getElements().size());
 				for(Expr e : fieldSet.getElements()) {
 					Key key;
 					
 					if(e instanceof Identifier) {
 						key = (Identifier)e;
 					} else if(e instanceof StringLiteral) {
 						key = (StringLiteral)e;
 					} else {
 						getErrors().add(new ExpectedIdentifier(e));
 						continue;
 					}
 					fields.put(key.getKeyString(), new Field(key, new FieldRef(base, key)));
 					
 				}
 				return new ObjectLiteral(fileRange, fields);
 			}
 			
 			@Override
 			public CoreExpr visitIdentifier(Identifier simpleName) {
 				return new FieldRef(base, simpleName);
 			}
 			
 			public CoreExpr fallback(CoreExpr x) {
 				getErrors().add(new InvalidProjection(x));
 				return base;
 			}
 		});
 		if(result == null) throw new NullPointerException();
 		return result;
 	}
 	
 	
 	// Optional projection: x?.foo == x.map((x) -> x.foo)
 	private CoreExpr optionProjection(BinaryOp op) {
 		final FileRange r = op.getFileRange();
 		final Identifier argName = gensym(r);
 		final CoreExpr projection = projection(op.getFileRange(), argName, op.getRight());
 		CoreExpr projectFunc = new FunctionLiteral(new FunArg(argName), projection);
 		final CoreExpr left = desugar(op.getLeft());
 		CoreExpr mapCall = new Call(new FieldRef(left, new Identifier(r, "map")), projectFunc);
 		return mapCall;
 	}
 	
 	// orElse: x ?: y == x.orElse(-> y)
 	private CoreExpr orElse(BinaryOp op) {
 		final FileRange r = op.getFileRange();
 		final CoreExpr left = desugar(op.getLeft());
 		final CoreExpr right = desugar(op.getRight());
 		return new Call(new FieldRef(left, new Identifier(r, "valueOrElse")), new FunctionLiteral(right));
 	}
 
 	int gensymCounter = 0;
 	private Identifier gensym(FileRange r) {
 		gensymCounter++;
 		return new Identifier(r, "__t"+gensymCounter);
 	}
 
 	private CoreExpr call(BinaryOp op) {
 		List<CoreExpr> args;
 		CoreExpr callee = desugar(op.getLeft());
 		CoreExpr arg = desugar(op.getRight());
 		if(arg instanceof ExprList) {
 			args = ((ExprList) arg).getElements();
 		} else {
 			args = Collections.singletonList(arg);
 		}
 		return new Call(op.getFileRange(), callee, args);
 	}
 
 	private CoreExpr let(BinaryOp op) {
 		SourceExpr target = stripParens(op.getLeft()); // Optional parens around lhs if it has a contract spec
 		SourceExpr value = op.getRight();
 		Option<CoreExpr> contract = FunctionLiteral.CONTRACT_NONE;
 		CoreExpr dsValue;
 		if(isPair(target)) {
 			final BinaryOp targetBOp = (BinaryOp)target;
 			target = targetBOp.getLeft();
 			contract = Option.some(desugar(targetBOp.getRight()));
 		}
 		if(isCallWithArgs(target)) {
 			BinaryOp call = (BinaryOp) target;
 			dsValue = functionLiteral(op.getFileRange(), call.getRight(), contract, desugar(value));
 			target = call.getLeft();
 			contract = FunctionLiteral.CONTRACT_NONE;
 		} else if(isUnaryCall(target)) {
 			UnaryOp call = (UnaryOp) target;
 			dsValue = new FunctionLiteral(op.getFileRange(), Collections.<FunArg>emptyList(), contract, desugar(value));
 			target = call.getOperand();
 			contract = FunctionLiteral.CONTRACT_NONE;
 		} else {
 			dsValue = desugar(value);
 		}
 		if(target instanceof Identifier) {
 			Identifier id = (Identifier) target;
 			if(contract.isSome()) {
 				dsValue = new Call(contract.some(), dsValue);
 			}
 			return new Let(id, dsValue);
 		} else {
 			getErrors().add(new ExpectedIdentifier(target));
 			return dsValue;
 		}
 	}
 
 	private boolean isUnaryCall(SourceExpr target) {
 		return isUnaryOp(target, Operator.UNARY_CALL);
 	}
 
 	private boolean isCallWithArgs(SourceExpr target) {
 		return target instanceof BinaryOp && ((BinaryOp)target).getOperator() == Operator.CALL;
 	}
 
 	/**
 	 * Requires Boolean to have a method <code>lazyIfTrue(trueFunc,falseFunc): ifTrue(trueFunc,falseFunc)()</code>
 	 */
 	private CoreExpr exprListToCond(FileRange range, LinkedList<SourceExpr> exprs) {
 		CoreExpr result = null;
 		boolean missingElseClause = false;
 		SourceExpr duplicateElseClause = null;
 		for(Iterator<SourceExpr> it = exprs.descendingIterator(); it.hasNext(); ) {
 			SourceExpr e = it.next();
 			if(isCondCase(e)) {
 				BinaryOp caseOp = (BinaryOp)e;
 				final CoreExpr thenExpr = desugar(caseOp.getRight());
 				if(caseOp.getLeft() instanceof Ellipsis) {
 					if(result != null) {
 						duplicateElseClause = e;
 					}
 					result = thenExpr;
 				} else {
 					if(result == null) {
 						missingElseClause = true;
 						result = new Identifier(range, "~~~MISSING ELSE~~~");
 					}
 					
 					final CoreExpr condition = desugar(caseOp.getLeft());
 					Field trueField = new Field(new Identifier(e.getFileRange(), "true"), new FunctionLiteral(thenExpr));
 					Field falseField = new Field(new Identifier(e.getFileRange(), "false"), new FunctionLiteral(result));
 					CoreExpr arg = new ObjectLiteral(e.getFileRange(), trueField, falseField);
 					CoreExpr ifMethod = new FieldRef(condition, new Identifier(e.getFileRange(), "if"));
 					result = new Call(ifMethod, arg);
 				}
 			} else {
 				if(result != null) {
 					duplicateElseClause = e;
 				}
 				result = desugar(e);
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
 		// result would only be null if the 
 		if(result == null) throw new NullPointerException();
 		return result;
 	}
 
 	private final boolean isCondCase(Expr e) {
 		return (e instanceof BinaryOp) && ((BinaryOp)e).getOperator() == Operator.COND;
 	}
 
 	private CoreExpr listLiteral(final FileRange range, List<SourceExpr> list, @Nullable Operator requireBullet) {
 		return new ListLiteral(range, elements(list, requireBullet));
 	}
 
 	private CoreExpr setLiteral(final FileRange range, List<SourceExpr> list, @Nullable Operator requireBullet) {
 		return new SetLiteral(range, elements(list, requireBullet));
 	}
 
 	private ArrayList<CoreExpr> elements(List<SourceExpr> list, @Nullable Operator requireBullet) {
 		ArrayList<CoreExpr> elements = new ArrayList<>(list.size());
 		List<SourceExpr> headings = null;
 		if(!list.isEmpty()) {
 			for(SourceExpr e : list) {
 				if(isTableHeader(e)) {
 					final UnaryOp headerOp = (UnaryOp)e;
 					headings = flattenCommasOrSemicolons(headerOp.getOperand(), new ArrayList<SourceExpr>());
 					continue;
 				}
 				SourceExpr eltExpr;
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
 
 	private CoreExpr objectLiteral(FileRange range, Collection<SourceExpr> pairs) {
 		// Key/value pair - treat as an object
 		LinkedHashMap<String, Field> fields = new LinkedHashMap<>(pairs.size()*2);
 		List<SourceExpr> headings = null;
 		for(SourceExpr e : pairs) {
 			if(e == null) throw new NullPointerException();
 			SourceExpr keyExpr;
 			SourceExpr valueExpr;
 			if(isPair(e)) {
 				final BinaryOp fieldOp = (BinaryOp)e;
 				keyExpr = fieldOp.getLeft();
 				valueExpr = fieldOp.getRight();
 			} else if(isMirrorElement(e)) {
 				final UnaryOp fieldOp = (UnaryOp)e;
 				keyExpr = valueExpr = fieldOp.getOperand();
 			} else if(isTableHeader(e)) {
 				final UnaryOp headerOp = (UnaryOp)e;
 				headings = flattenCommasOrSemicolons(headerOp.getOperand(), new ArrayList<SourceExpr>());
 				continue;
 			} else {
 				getErrors().add(new ExpectedField(e.getFileRange()));
 				continue;
 			}
 			addField(keyExpr, valueExpr, headings, fields);
 		}
 		return new ObjectLiteral(range, fields);
 	}
 
 	private void addField(SourceExpr keyExpr, SourceExpr valueExpr,
 			@Nullable List<SourceExpr> headings, LinkedHashMap<String, Field> fields) {
 		Option<CoreExpr> contract = FunctionLiteral.CONTRACT_NONE;
 		if(isPair(keyExpr)) {
 			final BinaryOp targetBOp = (BinaryOp)keyExpr;
 			keyExpr = targetBOp.getLeft();
 			contract = Option.some(desugar(targetBOp.getRight()));
 			if(contract == null) throw new NullPointerException();
 		}
 		CoreExpr value;
 		Option<Key> selfName = Option.none();
 		if(isCallWithArgs(keyExpr)) {
 			BinaryOp call = (BinaryOp) keyExpr;
 			keyExpr = call.getLeft();
 			if(isProjection(keyExpr)) {
 				SourceExpr selfExpr = ((BinaryOp)keyExpr).getLeft();
 				if(selfExpr instanceof Key) {
 					selfName = Option.some((Key)selfExpr);
 				} else {
 					selfName = Option.some((Key)new StringLiteral(selfExpr.getFileRange(), selfExpr.toSource()));
 					errors.add(new ExpectedIdentifier(selfExpr));
 				}
 				keyExpr = ((BinaryOp)keyExpr).getRight();
 			}
 			value = functionLiteral(valueExpr.getFileRange(), flattenCommasOrSemicolons(call.getRight(), new ArrayList<SourceExpr>()), desugar(valueExpr), contract, selfName);
 			contract = FunctionLiteral.CONTRACT_NONE;
 		} else if(isUnaryCall(keyExpr)) {
 			UnaryOp call = (UnaryOp) keyExpr;
 			keyExpr = call.getOperand();
 			if(isProjection(keyExpr)) {
 				SourceExpr selfExpr = ((BinaryOp)keyExpr).getLeft();
 				if(selfExpr instanceof Key) {
 					selfName = Option.some((Key)selfExpr);
 				} else {
 					selfName = Option.some((Key)new StringLiteral(selfExpr.getFileRange(), selfExpr.toSource()));
 					errors.add(new ExpectedIdentifier(selfExpr));
 				}
 				keyExpr = ((BinaryOp)keyExpr).getRight();
 			}
 			List<SourceExpr> args = Collections.<SourceExpr>emptyList();
 			if(args == null) throw new NullPointerException();
 			value = functionLiteral(valueExpr.getFileRange(), args, desugar(valueExpr), contract, selfName);
 			contract = FunctionLiteral.CONTRACT_NONE;
 		} else if(headings != null) {
 			// If this is a table, construct the row
 			value = makeRow(headings, valueExpr);
 		} else {
 			value = desugar(valueExpr);
 		}
 		if(contract.isSome()) {
 			// TODO Figure out how to call the contract properly ...
 			final CoreExpr c = contract.some();
 			if(c == null) throw new NullPointerException();
 			value = new Call(c, value);
 		}
 		if(!(keyExpr instanceof Key)) {
 			getErrors().add(new ExpectedFieldName("Expected identifier or string; got "+keyExpr.getClass().getSimpleName()+" '"+keyExpr.toSource()+"'", keyExpr.getFileRange()));
 			keyExpr = new Identifier(keyExpr.getFileRange(), keyExpr.toSource());
 		}
 		Key key = (Key)keyExpr;
 		final Field field = new Field(key, value);
 		fields.put(field.getKey().getKeyString(), field);
 	}
 
 	private boolean isProjection(SourceExpr keyExpr) {
 		return isBinaryOp(keyExpr, Operator.PROJECTION);
 	}
 
 	private boolean isBinaryOp(SourceExpr expr, Operator opToCheckFor) {
 		return((expr instanceof BinaryOp) && ((BinaryOp) expr).getOperator() == opToCheckFor);
 	}
 
 	private CoreExpr makeRow(List<SourceExpr> headings, SourceExpr rowExpr) {
 		List<SourceExpr> values = flattenCommasOrSemicolons(stripParens(rowExpr), new ArrayList<SourceExpr>(headings.size()));
 		for(int i=headings.size(); i < values.size(); i++) {
 			final SourceExpr val = values.get(i);
 			getErrors().add(new ExtraTableColumn(i, headings.size(), val.toSource(), val.getFileRange()));
 		}
 		if(values.size() < headings.size()) {
 			getErrors().add(new MissingValueForTableColumn(values.size(), headings.size(), rowExpr.getFileRange(), headings.get(values.size()).toSource()));
 		}
 		LinkedHashMap<String, Field> fields = new LinkedHashMap<>(headings.size()*2);
 		for(int i=0; i < values.size(); i++) {
 			addField(headings.get(i), values.get(i), null, fields);
 		}
 		return new ObjectLiteral(rowExpr.getFileRange(), fields);
 	}
 
 	/**
 	 * If the expression is wrapped in parenthesis, remove them.
 	 * @param valueExpr
 	 * @return
 	 */
 	private SourceExpr stripParens(SourceExpr valueExpr) {
 		if(isUnaryOp(valueExpr, Operator.PARENS))
 			return ((UnaryOp)valueExpr).getOperand();
 		return valueExpr;
 	}
 
 	private CoreExpr functionLiteral(BinaryOp op) {
 		SourceExpr argsDef = op.getLeft();
 		final SourceExpr body = op.getRight();
 		FileRange range = op.getFileRange();
 		return functionLiteral(argsDef, desugar(body), range);
 	}
 
 	private CoreExpr functionLiteral(SourceExpr argsDef, final CoreExpr body,
 			FileRange range) {
 		Option<CoreExpr> returnContract = FunctionLiteral.CONTRACT_NONE;
 		// Optional return type/contract
 		if(isPair(argsDef)) {
 			returnContract = Option.some(desugar(((BinaryOp)argsDef).getRight()));
 			argsDef = ((BinaryOp)argsDef).getLeft();
 		}
 		return functionLiteral(range, argsDef, returnContract, body);
 	}
 
 	private CoreExpr functionLiteral(FileRange range, SourceExpr args, Option<CoreExpr> contract, final CoreExpr body) {
 		// Args should be comma-separated
 		List<SourceExpr> exprs = new LinkedList<>();
 		if(args instanceof UnitRef) {
 			// Leave the list empty if args is an empty list of some kind
 		} else {
 			// Optional parentheses around formal parameter list
 			flattenCommas(stripParens(args), Operator.COMMA, exprs);
 		}
 		return functionLiteral(range, exprs, body, contract, Key.NONE);
 	}
 
 	private CoreExpr functionLiteral(FileRange range, List<SourceExpr> exprs,
 			final CoreExpr body, Option<CoreExpr> returnContract, Option<Key> selfName) {
 		List<FunArg> args = exprs.isEmpty() ? Collections.<FunArg>emptyList() : new ArrayList<FunArg>(exprs.size());
 		if(args == null) throw new NullPointerException();
 		for(SourceExpr argExpr : exprs) {
 			SourceExpr name = argExpr;
 			if(name == null) throw new NullPointerException();
 			Option<CoreExpr> contract = FunctionLiteral.CONTRACT_NONE;
 			if(isPair(name)) {
				BinaryOp pair = (BinaryOp) name;
				name = pair.getLeft();
				contract = Option.some(desugar(pair.getRight()));
 				if(contract == null) throw new NullPointerException();
 			}
 			if(!(name instanceof Key)) {
 				getErrors().add(new ExpectedIdentifier(name));
 				continue;
 			}
 			args.add(new FunArg((Key)name, contract));
 		}
 		return new FunctionLiteral(range, selfName, args, returnContract, body);
 	}
 
 	private boolean isListElement(SourceExpr e) {
 		return isUnaryOp(e, Operator.LIST_ELEMENT);
 	}
 	private boolean isSetElement(SourceExpr e) {
 		return isUnaryOp(e, Operator.SET_ELEMENT);
 	}
 
 	private boolean isPair(SourceExpr e) {
 		return (e instanceof BinaryOp) && 
 				(((BinaryOp) e).getOperator() == Operator.PAIR);
 	}
 	private boolean isMirrorElement(SourceExpr e) {
 		return isUnaryOp(e, Operator.MIRROR);
 	}
 	private boolean isTableHeader(SourceExpr e) {
 		return isUnaryOp(e, Operator.TABLE_HEADER);
 	}
 
 	private boolean isUnaryOp(SourceExpr e, final Operator operator) {
 		return (e instanceof UnaryOp) && ((UnaryOp) e).getOperator() == operator;
 	}
 
 	private List<SourceExpr> flattenCommas(SourceExpr arg, Operator type, List<SourceExpr> exprs) {
 		if(arg instanceof BinaryOp) {
 			BinaryOp bop = (BinaryOp) arg;
 			if(isElementSeparator(bop)) {
 				final Operator boper = bop.getOperator();
 				if(boper != type && bop.getOperator() != Operator.NEWLINE) {
 					if(type == Operator.NEWLINE) type = bop.getOperator();
 					else getErrors().add(new MixedSemicolonAndComma(bop));
 				}
 				flattenCommas(bop.getLeft(), type, exprs);
 				flattenCommas(bop.getRight(), type, exprs);
 				return exprs;
 			}
 		}
 		exprs.add(arg);
 		return exprs;
 	}
 
 	private List<SourceExpr> flattenCommasOrSemicolons(SourceExpr arg, List<SourceExpr> list) {
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
 
 	private static boolean isElementSeparator(final Operator operator) {
 		return operator == Operator.COMMA || operator == Operator.SEMICOLON || operator == Operator.NEWLINE;
 	}
 	
 	public LinkedList<BanjoParseException> getErrors() {
 		return errors;
 	}
 
 	@Override
 	public CoreExpr visitBinaryOp(BinaryOp op) {
 		// Comma outside of a parentheses should be a list or map without the braces/brackets
 		final FileRange range = op.getFileRange();
 		switch(op.getOperator()) {
 		case COMMA:
 		case SEMICOLON:
 		case NEWLINE: {
 			LinkedList<SourceExpr> exprs = new LinkedList<>();
 			flattenCommas(op, op.getOperator(), exprs);
 			SourceExpr first = exprs.get(0);
 			if(first == null) throw new NullPointerException();
 			if(isTableHeader(first)) {
 				SourceExpr second = exprs.get(1);
 				if(second == null) throw new NullPointerException();
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
 				ArrayList<CoreExpr> exprList = new ArrayList<>(exprs.size());
 				for(SourceExpr e : exprs) {
 					if(e == null) throw new NullPointerException();
 					exprList.add(desugar(e));
 				}
 				return new ExprList(op.getFileRange(), exprList);
 			}
 		}
 		case LAZY_AND: return lazyAnd(op);
 		case LAZY_OR: return lazyOr(op);
 		case COND: return exprListToCond(op.getFileRange(), new LinkedList<SourceExpr>(Collections.singletonList(op)));
 		case CALL: return call(op);
 		case FUNCTION: return functionLiteral(op);
 		case PAIR: return objectLiteral(range, Collections.<SourceExpr>singletonList(op));
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
 			if(op.getOperator().getMethodName().isSome())
 				return new Call(new FieldRef(desugar(op.getLeft()), new Identifier(op.getFileRange(), op.getOperator().getMethodName().some())), desugar(op.getRight()));
 			getErrors().add(new UnsupportedBinaryOperator(op.getOperator().getOp(), op.getFileRange()));
 			return desugar(op.getRight());
 		}
 	}
 
 	@Override
 	public CoreExpr visitUnaryOp(UnaryOp op) {
 		final SourceExpr operand = op.getOperand();
 		switch(op.getOperator()) {
 		case LIST_ELEMENT: return new ListLiteral(op.getFileRange(), Collections.singletonList(desugar(operand)));
 		case SET_ELEMENT: return new SetLiteral(op.getFileRange(), Collections.singletonList(desugar(operand)));
 		case LAZY: return new FunctionLiteral(op.getFileRange(), Collections.<FunArg>emptyList(), FunctionLiteral.CONTRACT_NONE, desugar(operand));
 		case MIRROR: return objectLiteral(op.getFileRange(), Collections.singletonList(operand));
 		case LIST_LITERAL: {
 			LinkedList<SourceExpr> exprs = new LinkedList<>();
 			flattenCommasOrSemicolons(operand, exprs);
 			return listLiteral(operand.getFileRange(), exprs, null);
 		}
 		case OBJECT_OR_SET_LITERAL: { // Expecting an object or set
 			CoreExpr dsOperand = desugar(operand);
 			if(dsOperand instanceof ObjectLiteral) return dsOperand;
 			if(dsOperand instanceof SetLiteral) return dsOperand;
 			if(dsOperand instanceof ExprList) 
 				return new SetLiteral(op.getFileRange(), ((ExprList)dsOperand).getElements());
 			// Singleton set literal
 			return new SetLiteral(op.getFileRange(), Collections.singletonList(dsOperand));
 		}
 		case UNARY_CALL:
 			return new Call(desugar(operand));
 		case PARENS:
 		case NEWLINE:
 		case RETURN:
 			return desugar(operand);
 		default: {
 			CoreExpr dsOperand = desugar(operand);
 			if(op.getOperator().hasMethodName())
 				return new Call(new FieldRef(dsOperand, new Identifier(op.getFileRange(), op.getOperator().getMethodName().some())));
 			getErrors().add(new UnsupportedUnaryOperator(op.getOpToken().toSource(), op.getFileRange()));
 			return dsOperand;
 		}
 		}
 	}
 
 	@Override
 	public CoreExpr visitStringLiteral(StringLiteral stringLiteral) {
 		return stringLiteral;
 	}
 
 	@Override
 	public CoreExpr visitNumberLiteral(NumberLiteral numberLiteral) {
 		return numberLiteral;
 	}
 
 	@Override
 	public CoreExpr visitIdentifier(Identifier simpleName) {
 		return simpleName;
 	}
 
 	@Override
 	public CoreExpr visitUnit(UnitRef unit) {
 		switch(unit.getParenType()) {
 		case BRACES: return new ObjectLiteral(unit.getFileRange(), Collections.<String,Field>emptyMap());
 		case BRACKETS: return new ListLiteral(unit.getFileRange(), Collections.<CoreExpr>emptyList());
 		default:
 		case PARENS:
 			getErrors().add(new ExpectedExpression(unit.getFileRange(), unit.toSource())); 
 			return new Identifier(unit.getFileRange(), "unit");
 		}
 	}
 
 	@Override
 	public CoreExpr visitOperator(OperatorRef operatorRef) {
 		return operatorRef;
 	}
 
 //	@Override @Nullable 
 //	public CoreExpr visitWhitespace(Whitespace ws) {
 //		return null;
 //	}
 //
 //	@Override @Nullable 
 //	public CoreExpr visitComment(Comment c) {
 //		return null;
 //	}
 //
 //	@Override @Nullable 
 //	public CoreExpr visitEof() {
 //		return null;
 //	}
 
 	@Override
 	public CoreExpr visitEllipsis(Ellipsis ellipsis) {
 		getErrors().add(new UnexpectedEllipsis(ellipsis));
 		return new Identifier(ellipsis.getFileRange(), ellipsis.toSource());
 	}
 }
