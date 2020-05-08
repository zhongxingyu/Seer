 package org.cwi.waebric.interpreter;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Stack;
 
 import org.cwi.waebric.parser.ast.AbstractSyntaxNode;
 import org.cwi.waebric.parser.ast.DefaultNodeVisitor;
 import org.cwi.waebric.parser.ast.AbstractSyntaxNodeList;
 import org.cwi.waebric.parser.ast.basic.IdCon;
 import org.cwi.waebric.parser.ast.expression.Expression;
 import org.cwi.waebric.parser.ast.expression.KeyValuePair;
 import org.cwi.waebric.parser.ast.expression.Expression.CatExpression;
 import org.cwi.waebric.parser.ast.expression.Expression.Field;
 import org.cwi.waebric.parser.ast.expression.Expression.ListExpression;
 import org.cwi.waebric.parser.ast.expression.Expression.NatExpression;
 import org.cwi.waebric.parser.ast.expression.Expression.RecordExpression;
 import org.cwi.waebric.parser.ast.expression.Expression.SymbolExpression;
 import org.cwi.waebric.parser.ast.expression.Expression.TextExpression;
 import org.cwi.waebric.parser.ast.expression.Expression.VarExpression;
 import org.cwi.waebric.parser.ast.markup.Argument;
 import org.cwi.waebric.parser.ast.markup.Arguments;
 import org.cwi.waebric.parser.ast.markup.Attribute;
 import org.cwi.waebric.parser.ast.markup.Attributes;
 import org.cwi.waebric.parser.ast.markup.Markup;
 import org.cwi.waebric.parser.ast.markup.Markup.Call;
 import org.cwi.waebric.parser.ast.markup.Markup.Tag;
 import org.cwi.waebric.parser.ast.module.function.Formals;
 import org.cwi.waebric.parser.ast.module.function.FunctionDef;
 import org.cwi.waebric.parser.ast.statement.Assignment;
 import org.cwi.waebric.parser.ast.statement.Statement;
 import org.cwi.waebric.parser.ast.statement.Assignment.FuncBind;
 import org.cwi.waebric.parser.ast.statement.Assignment.VarBind;
 import org.cwi.waebric.parser.ast.statement.Statement.MarkupsEmbedding;
 import org.cwi.waebric.parser.ast.statement.Statement.MarkupsExpression;
 import org.cwi.waebric.parser.ast.statement.Statement.MarkupsMarkup;
 import org.cwi.waebric.parser.ast.statement.Statement.MarkupsStatement;
 import org.cwi.waebric.parser.ast.statement.Statement.MarkupStatement;
 import org.cwi.waebric.parser.ast.statement.embedding.Embed;
 import org.cwi.waebric.parser.ast.statement.embedding.Embedding;
 import org.cwi.waebric.parser.ast.statement.embedding.TextTail;
 import org.cwi.waebric.parser.ast.statement.predicate.Predicate;
 import org.cwi.waebric.parser.ast.statement.predicate.Type;
 import org.cwi.waebric.util.Environment;
 
 import org.jdom.CDATA;
 import org.jdom.Comment;
 import org.jdom.Content;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.Namespace;
 import org.jdom.Text;
 
 /**
  * Convert AST in JDOM format, the eventual XHTML document is generated using
  * the JDOM libraries. During conversion the visitor pattern is applied to
  * minimize the need for node casts and result in more readable code.
  * @author Jeroen van Schagen
  * @date 11-06-2009
  */
 public class JDOMVisitor extends DefaultNodeVisitor {
 
 	private final Document document;
 	private Element current;
 	
 	/**
 	 * Expression evaluation
 	 */
 	public String eeval = "";
 	
 	/**
 	 * Predicate evaluation
 	 */
 	public boolean peval = false;
 	
 	/**
 	 * Function specific environments
 	 */
 	private final Map<FunctionDef, Environment> functionEnvs;
 	
 	/**
 	 * Current environment
 	 */
 	private Environment environment;
 	
 	/**
 	 * Current yield stack
 	 */
 	private Stack<AbstractSyntaxNode> yield;
 	
 	/**
 	 * Construct JDOM visitor
 	 * @param document
 	 */
 	public JDOMVisitor(Document document) {
 		this(document, new Environment());
 	}
 	
 	/**
 	 * Construct JDOM visitor
 	 * @param document Document
 	 */
 	public JDOMVisitor(Document document, Environment environment) {
 		this.document = document;
 		this.environment = environment;
 		yield = new Stack<AbstractSyntaxNode>();
 		functionEnvs = new HashMap<FunctionDef, Environment>();
 	}
 
 	/**
 	 * Attach content to current element, in-case element
 	 * does not exist create root element.
 	 * @param content
 	 */
 	private void addContent(Content content) {
 		// Construct root element
 		if(current == null) {
 			if(content instanceof Element) {
 				Element rootElement = (Element) content;
 				document.setRootElement(rootElement);
 				current = rootElement;
 				return; // Content added, quit function
 			} else {
 				Element XHTML = createXHTMLTag();
 				document.setRootElement(XHTML);
 				current = XHTML;
 			}
 		}
 		
 		current.addContent(content); // Attach content
 		if(content instanceof Element) { current = (Element) content; } // Update current
 	}
 	
 	/**
 	 * Create default XHTML root tag
 	 * @return
 	 */
 	private Element createXHTMLTag() {
 		Namespace XHTML = Namespace.getNamespace("xhtml", "http://www.w3.org/1999/xhtml");
 		Element tag = new Element("html", XHTML);
 		tag.setAttribute("lang", "en");
 		return tag;
 	}
 	
 	/**
 	 * Delegate interpretation to called function, in case the called function does 
 	 * not exists the call is interpreted as a mark-up tag and its arguments as attributes.
 	 */
 	public void visit(Call markup) {
 		String name = markup.getDesignator().getIdentifier().getName();
 
 		if(environment.isDefinedFunction(name)) { // Call to defined function
 			FunctionDef function = environment.getFunction(name); // Retrieve function definition
 			
 			// Create new environment for function
 			Environment previous = environment; // Store previous environment
 			environment = new Environment(getEnvironment(function)); 
 			
 			// Store function variables
 			int index = 0; 
 			Arguments arguments = markup.getArguments();
 			for(IdCon formal: function.getFormals().getIdentifiers()) {
 				if(arguments.size() > index) {
 					Expression expression = arguments.get(index).getExpression();
 					environment.defineVariable(formal.getName(), expression);
 				} else { 
 					environment.defineVariable(formal.getName(), new Expression.TextExpression("undef"));
 				}
 			}
 			
 			function.accept(this); // Visit function
 			
 			// Restore parent environment
 			environment = previous;
 		} else { // Call to undefined function
 			// Delegate mark-up as tag
 			new Tag(markup.getDesignator()).accept(this);
 
 			// Interpret arguments as attributes
 			String value = ""; // Value attribute
 			for(Argument argument: markup.getArguments()) {
 				if(argument instanceof Argument.RegularArgument) {
 					// The combined value of regular arguments are stored as value attribute
 					argument.getExpression().accept(this);
 					if(eeval.equals("undef")) { eeval = "UNDEFINED"; } // Convert undef to UNDEFINED to match reference impl
 					if(! value.equals("") && ! eeval.equals("")) { value += " "; } // Attach separator
 					value += eeval; // Store text in value
 				} else if(argument instanceof Argument.Attr) {
 					// Attribute arguments are stored as separate attribute
 					argument.getExpression().accept(this);
 					String attribute = ((Argument.Attr) argument).getIdentifier().getName();
 					if(attribute.equals("xmlns")) {
 						current.setNamespace(Namespace.getNamespace("xhtml", "http://www.w3.org/1999/xhtml"));
 					} else {
 						current.setAttribute(attribute, eeval);
 					}
 				}
 			}
 			
 			// Store value attribute
 			if(! value.equals("")) { current.setAttribute("value", value); }
 		}
 	}
 
 	/**
 	 * Create next element in JDOM tree structure for tag. Attach additional attributes
 	 * when these are specified in the tag mark-up.
 	 */
 	public void visit(Tag markup) {
 		String name = markup.getDesignator().getIdentifier().getName();
 		if(environment.isDefinedFunction(name)) { // Call to defined function
 			// Delegate mark-up as call
 			new Markup.Call(markup.getDesignator()).accept(this);
 		} else {
 			Element tag = new Element(name);
 			addContent(tag); // Store tag as element in JDOM structure
 			visit(markup.getDesignator().getAttributes()); // Process attributes
 		}
 	}
 	
 	/**
 	 * Attach attributes to current JDOM element.
 	 */
 	public void visit(Attributes attributes) {
 		for(Attribute attribute: attributes) {
 			attribute.accept(this);
 		}
 	}
 	
 	/**
 	 * Store class attribute.
 	 */
 	public void visit(Attribute.ClassAttribute attribute) {
 		current.setAttribute("class", attribute.getIdentifier().getName());
 	}
 	
 	/**
 	 * Store id attribute.
 	 */
 	public void visit(Attribute.IdAttribute attribute) {
 		current.setAttribute("id", attribute.getIdentifier().getName());
 	}
 	
 	/**
 	 * Store name attribute.
 	 */
 	public void visit(Attribute.NameAttribute attribute) {
 		current.setAttribute("name", attribute.getIdentifier().getName());
 	}
 	
 	/**
 	 * Store type attribute.
 	 */
 	public void visit(Attribute.TypeAttribute attribute) {
 		current.setAttribute("type", attribute.getIdentifier().getName());
 	}
 	
 	/**
 	 * Store width attribute.
 	 */
 	public void visit(Attribute.WidthAttribute attribute) {
 		current.setAttribute("width", "" + attribute.getWidth().getValue());
 	}
 	
 	/**
 	 * Store width and height attribute.
 	 */
 	public void visit(Attribute.WidthHeightAttribute attribute) {
 		current.setAttribute("width", "" + attribute.getWidth().getValue());
 		current.setAttribute("height", "" + attribute.getHeight().getValue());
 	}
 	
 	/**
 	 * Determine root element and visit each statement.
 	 */
 	public void visit(FunctionDef function) {	
 		// Construct XHTML tag when multiple statements can be root
 		if(function.getStatements().size() > 1 && ! document.hasRootElement()) {
 			addContent(createXHTMLTag());
 		}
 
 		// Process statement(s)
 		Element root = current;
 		for(Statement statement: function.getStatements()) {
 			current = root; // Reset current to root
 			statement.accept(this);
 		}
 	}
 
 	/**
 	 * Evaluate predicate and potentially execute statement based on outcome.
 	 */
 	public void visit(Statement.If statement) {
 		statement.getPredicate().accept(this);
 		if(peval) {
 			statement.getTrueStatement().accept(this);
 		}
 	}
 
 	/**
 	 * Evaluate predicate and execute either of the statements based on outcome.
 	 */
 	public void visit(Statement.IfElse statement) {
 		statement.getPredicate().accept(this);
 		if(peval) {
 			statement.getTrueStatement().accept(this);
 		} else {
 			statement.getFalseStatement().accept(this);
 		}
 	}
 	
 	public void visit(Predicate.Not not) {
 		not.getPredicate().accept(this);
 		peval = !peval;
 	}
 	
 	public void visit(Predicate.And and) {
 		and.getLeft().accept(this);
 		boolean eval = peval;
 		and.getRight().accept(this);
 		peval = eval && peval;
 	}
 	
 	public void visit(Predicate.Or or) {
 		or.getLeft().accept(this);
 		boolean eval = peval;
 		or.getRight().accept(this);
 		peval = eval || peval;
 	}
 	
 	public void visit(Predicate.Is is) {
 		if(is.getType() instanceof Type.StringType) {
 			peval = is.getExpression().getClass() == Expression.TextExpression.class;
 		} else if(is.getType() instanceof Type.ListType) {
 			peval = is.getExpression().getClass() == Expression.ListExpression.class;
 		} else if(is.getType() instanceof Type.RecordType) {
 			peval = is.getExpression().getClass() == Expression.RecordExpression.class;
 		} else { peval = false; } // Invalid type, should not be parsed in the first place
 	}
 	
 	public void visit(Predicate.RegularPredicate predicate) {
 		if(predicate.getExpression() instanceof Expression.Field) {
 			// Field predicates check if the referenced field exists in record expression
 			Expression value = getFieldExpression((Expression.Field) predicate.getExpression());
 			peval = value != null;
 		} else if(predicate.getExpression() instanceof Expression.VarExpression) {
 			// Variable predicates check if the referenced variable is defined
 			String name = ((Expression.VarExpression) predicate.getExpression()).getId().getName();
 			peval = environment.isDefinedVariable(name);
 		} else { peval = true; }
 	}
 
 	/**
 	 * Interpret all sub-statements embedded in block.
 	 */
 	public void visit(Statement.Block statement) {
 		Element root = current;
 		for(Statement sub: statement.getStatements()) {
 			current = root; // Reset current to root
 			sub.accept(this);
 		}
 	}
 
 	/**
 	 * Execute statement for each element in list expression, also
 	 * create a local element variable during each loop.
 	 */
 	public void visit(Statement.Each statement) {
 		Expression expression = statement.getExpression();
 		if(expression instanceof Expression.ListExpression) {
 			Expression.ListExpression list = (Expression.ListExpression) expression;
 			
 			// Execute statement for each element
 			Element root = current;
 			for(Expression e: list.getExpressions()) {
 				current = root;
 				environment = new Environment(environment);
 				environment.defineVariable(statement.getVar().getName(), e);
 				statement.getStatement().accept(this);
 				environment = environment.getParent();
 			}
 		} else if(expression instanceof Expression.VarExpression) {
 			Expression.VarExpression var = (Expression.VarExpression) expression;
 			
 			// Retrieve actual expression from variable cache
 			expression = environment.getVariable(var.getId().getName());
 			
 			// Execute function again
 			Statement.Each each = new Statement.Each();
 			each.setVar(statement.getVar());
 			each.setStatement(statement.getStatement());
 			each.setExpression(expression);
 			visit(each);
 		} else if(expression instanceof Expression.Field) {
 			// Retrieve actual expression from field
 			expression = getFieldExpression((Expression.Field) expression);
 			
 			// Execute function again
 			Statement.Each each = new Statement.Each();
 			each.setVar(statement.getVar());
 			each.setStatement(statement.getStatement());
 			each.setExpression(expression);
 			visit(each);
 		}
 	}
 	
 	/**
 	 * Attach <!-- COMMENT --> to current element.
 	 */
 	public void visit(Statement.Comment statement) {
 		Comment comment = new Comment(statement.getComment().getLiteral().toString());
 		addContent(comment);
 	}
 	
 	/**
 	 * Attach <-CDATA TEXT -> to current element.
 	 */
 	public void visit(Statement.CData statement) {
 		statement.getExpression().accept(this);
 		addContent(new CDATA(eeval));
 	}
 
 	/**
 	 * Attach text to current element.
 	 */
 	public void visit(Statement.Echo statement) {
 		statement.getExpression().accept(this);
 		addContent(new Text(eeval));
 	}
 
 	/**
 	 * Attach text to current element.
 	 */
 	public void visit(Statement.EchoEmbedding statement) {
 		statement.getEmbedding().accept(this);
 	}
 
 	/**
 	 * Extend definition with assignments and execute statements, remove
 	 * definitions after completion of the let statement.
 	 */
 	public void visit(Statement.Let statement) {
 		// Create new environment for each assignment
 		for(Assignment assignment: statement.getAssignments()) {
 			environment = new Environment(environment);
 			assignment.accept(this);
 		}
 		
 		// Visit sub-statements
 		for(Statement sub: statement.getStatements()) {
 			sub.accept(this);
 		}
 		
 		// Restore previous state by removing each assignment environment
 		for(int i = 0; i < statement.getAssignments().size(); i++) {
 			environment = environment.getParent();
 		}
 	}
 
 	/**
 	 * Retrieve first element from yield stack and visit it.
 	 */
 	public void visit(Statement.Yield statement) {
 		if(yield.isEmpty()) { return; }
 		AbstractSyntaxNode replacement = yield.pop(); // Retrieve replacement
 
 		if(replacement != null) {
 			// Clone yield stack
 			Stack<AbstractSyntaxNode> clone = new Stack<AbstractSyntaxNode>();
 			clone.addAll(yield);
 			
 			replacement.accept(this); // Visit replacement
 	
 			// Place text value in current element
 			if(replacement instanceof Expression || replacement instanceof Embedding) {
 				addContent(new Text(eeval));
 			}
 			
 			yield = clone; // Restore yield stack
 		}
 	}
 
 	/**
 	 * Interpret mark-up embedded in statement.
 	 */
 	public void visit(MarkupStatement statement) {
 		if(isCall(statement.getMarkup())) {
 			String name = statement.getMarkup().getDesignator().getIdentifier().getName();
 			if(containsYield(environment.getFunction(name))) {
 				yield.add(null); // Place empty yield element in stack
 			}
 		}
 		
 		statement.getMarkup().accept(this);
 	}
 
 	/**
 	 * Interpret mark-ups embedded in statement.
 	 */
 	public void visit(MarkupsMarkup statement) {
 		Iterator<Markup> markups = statement.getMarkups().iterator();
 		while(markups.hasNext()) {
 			Markup markup = markups.next(); 
 			
 			if(isCall(markup)) {
 				// Calls to functions with yield, store the remainder of mark-up chain as argument
 				String name = markup.getDesignator().getIdentifier().getName();
 				if(containsYield(environment.getFunction(name))) {
 					// Remainder of mark-up chain is stored as yield statement
 					AbstractSyntaxNodeList<Markup> remainder = new AbstractSyntaxNodeList<Markup>();
 					while(markups.hasNext()) { remainder.add(markups.next()); }
 					MarkupsMarkup replacement = new MarkupsMarkup(remainder);
 					replacement.setMarkup(statement.getMarkup());
 					yield.add(replacement);
 				}
 				markup.accept(this); // Interpret call
 				return; // Quit interpreting after call
 			} else {
 				markup.accept(this); // Interpret tag
 			}
 		}
 		
 		// Interpret element when mark-up chain is call free
 		statement.getMarkup().accept(this);
 	}
 	
 	/**
 	 * Check if mark-up is a call, and calls to a valid function.
 	 * @param markup
 	 */
 	public boolean isCall(Markup markup) {
 		String name = markup.getDesignator().getIdentifier().getName();
 		return environment.getFunction(name) != null;
 	}
 	
 	/**
 	 * Check if node contains a yield statement, either contained directly in 
 	 * the statement collection or indirectly by calling a statement with yield.
 	 * @param node
 	 * @return
 	 */
 	private boolean containsYield(AbstractSyntaxNode node) {
 		if(node instanceof Statement.Yield) { return true; }
 		else {
 			// Delegate check to children
 			for(AbstractSyntaxNode child: node.getChildren()) {
 				if(containsYield(child)) { return true; }
 			} return false; // Nothing found
 		}
 	}
 
 	/**
 	 * Interpret mark-ups and expression embedded in statement.
 	 */
 	public void visit(MarkupsExpression statement) {
 		Iterator<Markup> markups = statement.getMarkups().iterator();
 		while(markups.hasNext()) {
 			Markup markup = markups.next(); 
 			
 			if(isCall(markup)) {
 				// Calls to functions with yield, store the remainder of mark-up chain as argument
 				String name = markup.getDesignator().getIdentifier().getName();
 				if(containsYield(environment.getFunction(name))) {
 					// Remainder of mark-up chain is stored as yield statement
 					AbstractSyntaxNodeList<Markup> remainder = new AbstractSyntaxNodeList<Markup>();
 					while(markups.hasNext()) { remainder.add(markups.next()); }
 					MarkupsExpression replacement = new MarkupsExpression(remainder);
 					replacement.setExpression(statement.getExpression());
 					yield.add(replacement);
 				}
 				markup.accept(this); // Interpret call
 				return; // Quit interpreting after call
 			} else {
 				markup.accept(this); // Interpret tag
 			}
 		}
 		
 		// Interpret element when mark-up chain is call free
 		statement.getExpression().accept(this);
 		addContent(new Text(eeval));
 	}
 
 	/**
 	 * Interpret mark-ups and sub-statement embedded in statement.
 	 */
 	public void visit(MarkupsStatement statement) {
 		Iterator<Markup> markups = statement.getMarkups().iterator();
 		while(markups.hasNext()) {
 			Markup markup = markups.next(); 
 			
 			if(isCall(markup)) {
 				// Calls to functions with yield, store the remainder of mark-up chain as argument
 				String name = markup.getDesignator().getIdentifier().getName();
 				if(containsYield(environment.getFunction(name))) {
 					// Remainder of mark-up chain is stored as yield statement
 					AbstractSyntaxNodeList<Markup> remainder = new AbstractSyntaxNodeList<Markup>();
 					while(markups.hasNext()) { remainder.add(markups.next()); }
 					MarkupsStatement replacement = new MarkupsStatement(remainder);
 					replacement.setStatement(statement.getStatement());
 					yield.add(replacement);
 				}
 				markup.accept(this); // Interpret call
 				return; // Quit interpreting after call
 			} else {
 				markup.accept(this); // Interpret tag
 			}
 		}
 		
 		// Interpret element when mark-up chain is call free
 		statement.getStatement().accept(this);
 	}
 
 	/**
 	 * Interpret mark-ups and embedding embedded in statement.
 	 */
 	public void visit(MarkupsEmbedding statement) {
 		Iterator<Markup> markups = statement.getMarkups().iterator();
 		while(markups.hasNext()) {
 			Markup markup = markups.next(); 
 			
 			if(isCall(markup)) {
 				// Calls to functions with yield, store the remainder of mark-up chain as argument
 				String name = markup.getDesignator().getIdentifier().getName();
 				if(containsYield(environment.getFunction(name))) {
 					// Remainder of mark-up chain is stored as yield statement
 					AbstractSyntaxNodeList<Markup> remainder = new AbstractSyntaxNodeList<Markup>();
 					while(markups.hasNext()) { remainder.add(markups.next()); }
 					MarkupsEmbedding replacement = new MarkupsEmbedding(remainder);
 					replacement.setEmbedding(statement.getEmbedding());
 					yield.add(replacement);
 				}
 				markup.accept(this); // Interpret call
 				return; // Quit interpreting after call
 			} else {
 				markup.accept(this); // Interpret tag
 			}
 		}
 		
 		// Interpret element when mark-up chain is call free
 		statement.getEmbedding().accept(this);
 	}
 
 	/**
 	 * Extend function definitions with binding.
 	 */
 	public void visit(FuncBind bind) {
 		// Construct new function definition based on bind data
 		FunctionDef definition = new FunctionDef();
 		
 		definition.setIdentifier(bind.getIdentifier());
 		definition.addStatement(bind.getStatement());
 		
 		if(bind.getVariables().size() == 0) {
 			definition.setFormals(new Formals.EmptyFormal());
 		} else {
 			Formals.RegularFormal formals = new Formals.RegularFormal();
 			for(IdCon variable : bind.getVariables()) {
 				formals.addIdentifier(variable);
 			}
 			definition.setFormals(formals);
 		}
 
 		// Store hard-copy of current environment
 		functionEnvs.put(definition, environment.clone()); 
 		
 		// Extend current environment with new function definition
 		environment.defineFunction(definition);
 	}
 
 	/**
 	 * Extend current variable definition with binding, in case variable
 	 * already exists its value will be overwritten.
 	 */
 	public void visit(VarBind bind) {
 		environment.defineVariable(bind.getIdentifier().getName(), bind.getExpression());
 	}
 
 	/**
 	 * Execute left and right expression after each other.
 	 */
 	public void visit(CatExpression expression) {
 		String result = "";
 		
 		expression.getLeft().accept(this);
 		result += eeval; // Retrieve left value
 		
 		expression.getRight().accept(this);
 		result += eeval; // Retrieve right value
 		
 		// Store result
 		eeval = result;
 	}
 
 	/**
 	 * Retrieve expression element from record expression, when
 	 * undefined or other expression type return "undef".
 	 */
 	public void visit(Field field) {
 		Expression expr = getFieldExpression(field);
 		if(expr == null) { new Expression.TextExpression("undef"); }
 		expr.accept(this); // Visit expression
 	}
 	
 	/**
 	 * Retrieve defined value from record expression.
 	 * @param field
 	 * @return
 	 */
 	public Expression getFieldExpression(Field field) {
 		Expression expression = field.getExpression();
 		while(expression instanceof Expression.VarExpression) {
 			// Browse over variable expressions until a raw type is detected
 			Expression.VarExpression var = (Expression.VarExpression) expression;
 			expression = environment.getVariable(var.getId().getName());
 		}
 		
 		if(expression instanceof Expression.RecordExpression) {
 			Expression.RecordExpression record = (Expression.RecordExpression) expression;
 			
 			// Retrieve referenced element from record and visit it
 			Expression result = record.getExpression(field.getIdentifier());
 			if(result != null) { return result; }
 		}
 
 		return null; // Undefined value
 	}
 
 	/**
 	 * Convert list in [element1,element2,...] text value
 	 */
 	public void visit(ListExpression expression) {
 		String result = "[";
 		for(Expression sub: expression.getExpressions()) {
 			// Attach a comma separator in front of each element, except first in list
 			if(expression.getExpressions().indexOf(sub) != 0) { result += ","; }
 			
 			// Surround symbol and text expressions between double quotes
 			if(sub instanceof SymbolExpression || sub instanceof TextExpression) { 
 				result += "\"";
 			}
 			
 			sub.accept(this); // Fill text field with expression value
 			result += eeval; // Store value in result string, before it is overwritten by next element
 			
 			// Surround symbol and text expressions between double quotes
 			if(sub instanceof SymbolExpression || sub instanceof TextExpression) { result += "\""; }
 		}
 		result += "]";
 
 		this.eeval = result;
 	}
 
 	/**
 	 * Convert record in [id1:expr1,id2:expr2,...] text value
 	 */
 	public void visit(RecordExpression expression) {
 		String result = "{";
 		for(KeyValuePair pair: expression.getPairs()) {
 			// Attach a comma separator in front of each element, except first in list
 			if(expression.getPairs().indexOf(pair) != 0) { result += ","; }
 			
 			result += pair.getIdentifier().getName() + ":";
 			
 			// Surround symbol and text expressions between double quotes
 			if(pair.getExpression() instanceof SymbolExpression || pair.getExpression() instanceof TextExpression) { 
 				result += "\"";
 			}
 			
 			pair.getExpression().accept(this); // Fill text field with expression value
 			result += eeval; // Store value in result string, before it is overwritten by next element
 			
 			// Surround symbol and text expressions between double quotes
 			if(pair.getExpression() instanceof SymbolExpression || pair.getExpression() instanceof TextExpression) { 
 				result += "\"";
 			}
 		}
 		result += "}";
 
 		this.eeval = result;
 	}
 	
 	/**
 	 * Store number in text field.
 	 */
 	public void visit(NatExpression expression) {
 		eeval = "" + expression.getNatural().getValue();
 	}
 
 	/**
 	 * Store symbol name in text field.
 	 */
 	public void visit(SymbolExpression expression) {
 		eeval = expression.getSymbol().getName().toString();
 	}
 
 	/**
 	 * Store text expression literal in text field.
 	 */
 	public void visit(TextExpression expression) {
 		eeval = expression.getText().getLiteral().toString();
 	}
 
 	/**
 	 * Delegate visit to variable value.
 	 */
 	public void visit(VarExpression expression) {
 		String name = expression.getId().getName();
 		
 		Expression reference = environment.getVariable(name);
 		if(reference == expression && environment.getParent() != null) {
 			reference = environment.getParent().getVariable(name);
 		}
 		
 		if(reference != null) {
 			reference.accept(this);
 		} else {
 			this.eeval = "undef"; // Undeclared variable reference
 		}
 	}
 
 	/**
 	 * Attach pretext to current element and delegate interpret to embed and tail.
 	 */
 	public void visit(Embedding embedding) {
 		// Attach pretext to current element
 		Text pre = new Text(embedding.getPre().getText().toString());
 		addContent(pre);
 		
 		embedding.getEmbed().accept(this); // Delegate embed
 		embedding.getTail().accept(this); // Delegate tail
 	}
 	
 	/**
 	 * Interpret mark-ups and expression.
 	 * @param embed
 	 */
 	public void visit(Embed.ExpressionEmbed embed) {
 		// Interpret similar to mark-up expression
 		Statement.MarkupsExpression stm = new Statement.MarkupsExpression(embed.getMarkups());
 		stm.setExpression(embed.getExpression());
 		stm.accept(this);
 	}
 	
 	/**
 	 * Interpret mark-ups embedded in embed.
 	 * @param embed
 	 */
 	public void visit(Embed.MarkupEmbed embed) {
 		// Interpret similar to mark-up mark-up
 		Statement.MarkupsMarkup stm = new Statement.MarkupsMarkup(embed.getMarkups());
 		stm.setMarkup(embed.getMarkup());
 		stm.accept(this);
 	}
 	
 	/**
 	 * Interpret mid text while visiting embed and tail.
 	 */
 	public void visit(TextTail.MidTail tail) {
 		// Attach mid text to current element
 		Text mid = new Text(tail.getMid().getText().toString());
 		addContent(mid);
 		
 		tail.getEmbed().accept(this);
 		tail.getTail().accept(this);
 	}
 	
 	/**
 	 * Store post text.
 	 */
 	public void visit(TextTail.PostTail tail) {
 		// Attach post text to current element
 		Text post = new Text(tail.getPost().getText().toString());
 		addContent(post);
 	}
 	
 	/**
 	 * Retrieve JDOM document.
 	 */
 	public Document getDocument() {
 		return document;
 	}
 
 	/**
 	 * Retrieve current JDOM element.
 	 * @return
 	 */
 	public Element getCurrent() {
 		return current;
 	}
 	
 	/**
 	 * Modify current JDOM element.
 	 * @param current
 	 */
 	public void setCurrent(Element current) {
 		if(! document.hasRootElement()) { document.setRootElement(current); }
 		this.current = current;
 	}
 	
 	/**
 	 * 
 	 * @param statement
 	 */
 	public void addYield(AbstractSyntaxNode statement) {
 		yield.push(statement);
 	}
 	
 	/**
 	 * Return environment.
 	 * @return
 	 */
 	public Environment getEnvironment() {
 		return environment;
 	}
 	
 	/**
 	 * Return environment specific to function.
 	 * @param function
 	 * @return
 	 */
 	private Environment getEnvironment(FunctionDef function) {
 		if(functionEnvs.containsKey(function)) { return functionEnvs.get(function); }
 		return environment;
 	}
 	
 }
