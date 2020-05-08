 package hash.parsing.visitors.evaluators;
 
 import static hash.parsing.HashParser.ATTRIBUTE;
 import static hash.parsing.HashParser.INDEX;
 import hash.lang.Context;
 import hash.parsing.tree.HashNode;
 import hash.runtime.Factory;
 import hash.runtime.Runtime;
 import hash.util.Constants;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.antlr.runtime.tree.Tree;
 
 /**
  * Executes a hash program by walking its AST with a context reference.
  * 
  * @author Thiago de Arruda
  * 
  */
 public class ProgramEvaluator extends LiteralEvaluator {
 	// TODO Need to push line numbers/filenames to a stack in order to produce
 	// meaningful traces when exceptions are thrown
 	public static final String RETURN_KEY = "returnValue";
 
 	private Context context;
 
 	public ProgramEvaluator(Context context) {
 		this.context = context;
 	}
 
 	@Override
 	protected Tree visitForeach(Tree node, Tree id, Tree iterable, Tree action) {
 		Tree lastResult = null;
 		Iterator iterator = Runtime
 				.getIterator(((ExpressionResult) visit(iterable)).getNodeData());
 		while (iterator.hasNext()) {
 			String name = id.getText();
 			context.put(name, iterator.next());
 			lastResult = visit(action);
 			if (lastResult instanceof ReturnStatement)
 				return lastResult;
 			else if (lastResult == BreakStatement.INSTANCE)
 				break;
 		}
		return lastResult;
 	}
 
 	@Override
 	protected Tree visitFor(Tree node, Tree init, Tree condition, Tree update,
 			Tree action) {
 		Tree lastResult = null;
 		visit(init);
 		Object cond = ((ExpressionResult) visit(condition)).getNodeData();
 		while ((Boolean) Runtime.invokeNormalMethod(cond,
 				Constants.BOOLEAN_VALUE)) {
 			lastResult = visit(action);
 			if (lastResult instanceof ReturnStatement)
 				return lastResult;
 			else if (lastResult == BreakStatement.INSTANCE)
 				break;
 			visit(update);
 			cond = ((ExpressionResult) visit(condition)).getNodeData();
 		}
		return lastResult;
 	}
 
 	@Override
 	protected Tree visitWhile(Tree node, Tree condition, Tree action) {
 		Tree lastResult = null;
 		Object cond = ((ExpressionResult) visit(condition)).getNodeData();
 		while ((Boolean) Runtime.invokeNormalMethod(cond,
 				Constants.BOOLEAN_VALUE)) {
 			lastResult = visit(action);
 			if (lastResult instanceof ReturnStatement)
 				return lastResult;
 			else if (lastResult == BreakStatement.INSTANCE)
 				break;
 			cond = ((ExpressionResult) visit(condition)).getNodeData();
 		}
		return lastResult;
 	}
 
 	@Override
 	protected Tree visitDoWhile(Tree node, Tree condition, Tree action) {
 
 		Tree lastResult = null;
 		Object cond = null;
 		do {
 			lastResult = visit(action);
 			if (lastResult instanceof ReturnStatement)
 				return lastResult;
 			else if (lastResult == BreakStatement.INSTANCE)
 				break;
 			cond = ((ExpressionResult) visit(condition)).getNodeData();
 		} while ((Boolean) Runtime.invokeNormalMethod(cond,
 				Constants.BOOLEAN_VALUE));
		return lastResult;
 	}
 
 	@Override
 	protected Tree visitIf(Tree node, Tree condition, Tree trueAction,
 			Tree falseAction) {
 		Tree lastResult = null;
 		Object cond = ((ExpressionResult) visit(condition)).getNodeData();
 		if ((Boolean) Runtime.invokeNormalMethod(cond, Constants.BOOLEAN_VALUE))
 			lastResult = visit(trueAction);
 		else
 			lastResult = visit(falseAction);
 		return lastResult;
 	}
 
 	@Override
 	protected Tree visitTryStatement(Tree node, Tree tryBlock,
 			Tree catchBlocks, Tree finallyBlock) {
 		Tree lastResult = null;
 		Exception exception = null;
 		try {
 			lastResult = visit(tryBlock);
 		} catch (Exception ex) {
 			boolean handled = false;
 			int catchBlocksLen = catchBlocks.getChildCount();
 			for (int i = 0; i < catchBlocksLen; i++) {
 				Tree catchBlock = catchBlocks.getChild(i);
 				Object exceptionType = ((ExpressionResult) visit(catchBlock
 						.getChild(0))).getNodeData();
 				if (exceptionType != null
 						&& !Runtime.isInstance(ex, exceptionType))
 					continue;
 				String id = catchBlock.getChild(1).getText();
 				context.put(id, ex);
 				lastResult = visit(catchBlock.getChild(2));
 				handled = true;
 				break;
 			}
 			if (!handled)
 				exception = ex;
 		} finally {
 			if (finallyBlock != null)
 				if (!(lastResult instanceof ReturnStatement))
 					lastResult = visit(finallyBlock);
 			if (exception != null)
 				throw new RuntimeException(exception);
 		}
 		return lastResult;
 	}
 
 	@Override
 	protected Tree visitThrow(Tree node, Tree throwableExpression) {
 		Object throwable = ((ExpressionResult) visit(throwableExpression))
 				.getNodeData();
 		throw Runtime.throwObj(throwable);
 	}
 
 	@Override
 	protected Tree visitFunction(Tree node, Tree parameters, Tree block) {
 		boolean isMethod = false;
 		HashNode fNode = (HashNode) node;
 		isMethod = fNode.getNodeData(HashNode.IS_METHOD) == Boolean.TRUE;
 		List params = (List) ((ExpressionResult) visit(parameters))
 				.getNodeData();
 		return new ExpressionResult(new FunctionEvaluator(context, params,
 				block, isMethod));
 	}
 
 	@Override
 	protected Tree visitReturn(Tree node, Tree returnExpression) {
 		return new ReturnStatement(
 				((HashNode) visit(returnExpression)).getNodeData());
 	}
 
 	@Override
 	protected Tree visitContinue(Tree node) {
 		return ContinueStatement.INSTANCE;
 	}
 
 	@Override
 	protected Tree visitBreak(Tree node) {
 		return BreakStatement.INSTANCE;
 	}
 
 	@Override
 	protected Tree visitBlock(Tree node) {
 		int len = node.getChildCount();
 		Tree lastResult = null;
 		for (int i = 0; i < len; i++) {
 			lastResult = visit(node.getChild(i));
 			if (lastResult instanceof ReturnStatement
 					|| lastResult == BreakStatement.INSTANCE
 					|| lastResult == ContinueStatement.INSTANCE)
 				return lastResult;
 		}
 		return lastResult;
 	}
 
 	@Override
 	protected Tree visitAssignment(Tree node, Tree target, Tree expression) {
 		Object value = ((ExpressionResult) visit(expression)).getNodeData();
 		if (target.getType() == ATTRIBUTE || target.getType() == INDEX) {
 			Object ownerObject = ((ExpressionResult) visit(target.getChild(0)))
 					.getNodeData();
 			Object key = ((ExpressionResult) visit(target.getChild(1)))
 					.getNodeData();
 			if (target.getType() == ATTRIBUTE)
 				Runtime.setAttribute(ownerObject, key, value);
 			else
 				Runtime.setIndex(ownerObject, key, value);
 		} else {
 			// target is an identifier
 			Context c = getContext((HashNode) target);
 			c.put(target.getText(), value);
 		}
 		return new ExpressionResult(value);
 	}
 
 	@Override
 	protected Tree visitEvalAndIncrement(Tree node, Tree target, Tree assignment) {
 		Object rv = ((ExpressionResult) visit(target)).getNodeData();
 		visit(assignment);
 		return new ExpressionResult(rv);
 	}
 
 	@Override
 	protected Tree visitBinaryExpression(Tree node, Tree left, Tree right) {
 		Object l = ((ExpressionResult) visit(left)).getNodeData();
 		Object r = ((ExpressionResult) visit(right)).getNodeData();
 		String text = node.getText();
 		if (text.equals("is")) // skip the runtime
 			return new ExpressionResult(l == r);
 		else
 			return new ExpressionResult(Runtime.invokeBinaryOperator(
 					node.getText(), l, r));
 	}
 
 	@Override
 	protected Tree visitUnaryExpression(Tree node, Tree operand) {
 		String operatorTxt = node.getText();
 		if (operatorTxt.equals("+"))// ignore
 			return visit(operand);
 		Object op = ((ExpressionResult) visit(operand)).getNodeData();
 		return new ExpressionResult(
 				Runtime.invokeUnaryOperator(operatorTxt, op));
 	}
 
 	@Override
 	protected Tree visitInvocation(Tree node, Tree expression, Tree arguments) {
 		Object[] args = ((List) ((ExpressionResult) visit(arguments))
 				.getNodeData()).toArray();
 		if (expression.getType() == ATTRIBUTE || expression.getType() == INDEX) {
 			// this is a method call
 			Object tgt = ((ExpressionResult) visit(expression.getChild(0)))
 					.getNodeData();
 			Object methodKey = ((ExpressionResult) visit(expression.getChild(1)))
 					.getNodeData();
 			return new ExpressionResult(Runtime.invokeNormalMethod(tgt,
 					methodKey, args));
 		} else {
 			// normal function call
 			Object exp = ((ExpressionResult) visit(expression)).getNodeData();
 			return new ExpressionResult(Runtime.invokeFunction(exp, args));
 		}
 	}
 
 	@Override
 	protected Tree visitMap(Tree node) {
 		Map rv = Factory.createMap();
 		int len = node.getChildCount();
 		for (int i = 0; i < len; i++) {
 			Object key = ((ExpressionResult) visit(node.getChild(i)))
 					.getNodeData();
 			Object value = ((ExpressionResult) visit(node.getChild(i).getChild(
 					0))).getNodeData();
 			rv.put(key, value);
 		}
 		return new ExpressionResult(rv);
 	}
 
 	@Override
 	protected Tree visitList(Tree node) {
 		int len = node.getChildCount();
 		List rv = Factory.createList();
 		for (int i = 0; i < len; i++)
 			rv.add(((ExpressionResult) visit(node.getChild(i))).getNodeData());
 		return new ExpressionResult(rv);
 	}
 
 	@Override
 	protected Tree visitAttributeAccess(Tree node, Tree target,
 			Tree attributeKey) {
 		Object tgt = ((ExpressionResult) visit(target)).getNodeData();
 		Object key = ((ExpressionResult) visit(attributeKey)).getNodeData();
 		return new ExpressionResult(Runtime.getAttribute(tgt, key));
 	}
 
 	@Override
 	protected Tree visitIndexAccess(Tree node, Tree target, Tree itemKey) {
 		Object tgt = ((ExpressionResult) visit(target)).getNodeData();
 		Object key = ((ExpressionResult) visit(itemKey)).getNodeData();
 		return new ExpressionResult(Runtime.getIndex(tgt, key));
 	}
 
 	@Override
 	protected Tree visitSlice(Tree node, Tree target, Tree sliceArgs) {
 		Object tgt = ((ExpressionResult) visit(target)).getNodeData();
 		List args = (List) ((ExpressionResult) visit(sliceArgs)).getNodeData();
 		return new ExpressionResult(Runtime.getSlice(tgt, args.get(0),
 				args.get(1), args.get(2)));
 	}
 
 	@Override
 	protected Tree visitNameReference(Tree node) {
 		Context c = getContext((HashNode) node);
 		return new ExpressionResult(c.get(node.getText()));
 	}
 
 	@Override
 	protected Tree visitRegex(Tree node) {
 		String regexLiteral = node.getText();
 		String regexText = regexLiteral.substring(1,
 				regexLiteral.lastIndexOf('/'));
 		int flags = 0;
 		if (regexLiteral.endsWith("i"))
 			flags = Pattern.CASE_INSENSITIVE;
 		return new ExpressionResult(Pattern.compile(regexText, flags));
 	}
 
 	@Override
 	protected Tree visitThis(Tree node) {
 		return visitNameReference(node);
 	}
 
 	private Context getContext(HashNode identifier) {
 		int level = 0;
 		if (identifier.getNodeData(HashNode.CONTEXT_LEVEL) != null)
 			level = (Integer) identifier.getNodeData(HashNode.CONTEXT_LEVEL);
 		Context c = context;
 		while (level > 0 && c.getParent() != null)
 			c = c.getParent();
 		return c;
 	}
 }
