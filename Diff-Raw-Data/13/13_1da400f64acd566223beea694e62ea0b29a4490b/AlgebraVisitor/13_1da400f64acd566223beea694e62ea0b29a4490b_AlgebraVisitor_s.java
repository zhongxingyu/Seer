 package main;
 
 import java.sql.DatabaseMetaData;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 
 import database.FileSystemDatabase;
 
 import parser.syntaxtree.AndExpression;
 import parser.syntaxtree.ColumnDefinition;
 import parser.syntaxtree.ColumnNames;
 import parser.syntaxtree.CompilationUnit;
 import parser.syntaxtree.CreateTable;
 import parser.syntaxtree.EqualityExpression;
 import parser.syntaxtree.Insert;
 import parser.syntaxtree.Item;
 import parser.syntaxtree.Items;
 import parser.syntaxtree.LiteralExpression;
 import parser.syntaxtree.Literals;
 import parser.syntaxtree.Name;
 import parser.syntaxtree.Node;
 import parser.syntaxtree.NodeSequence;
 import parser.syntaxtree.NodeToken;
 import parser.syntaxtree.OrExpression;
 import parser.syntaxtree.PrimaryExpression;
 import parser.syntaxtree.Query;
 import parser.syntaxtree.Table;
 import parser.syntaxtree.Tables;
 import parser.syntaxtree.Where;
 import parser.visitor.ObjectDepthFirst;
 import relationenalgebra.CrossProduct;
 import relationenalgebra.ITreeNode;
 import relationenalgebra.Projection;
 import relationenalgebra.Relation;
 import relationenalgebra.Selection;
 
 public class AlgebraVisitor extends ObjectDepthFirst {
 
 	/**
 	 * f0 -> Query() | Update() | Delete() | Insert() | CreateTable() |
 	 * DropTable()
 	 */
 	public Object visit(CompilationUnit n, Object argu) {
 		Logger.debug("call: cu");
 		Object _ret = n.f0.accept(this, argu);
 		Logger.debug("return: cu");
 		return _ret;
 	}
 
 	/**
 	 * f0 -> <INSERT> f1 -> <INTO> f2 -> Table() f3 -> [ "(" ColumnNames() ")" ]
 	 * f4 -> <VALUES> f5 -> "(" f6 -> Literals() f7 -> ")"
 	 */
 	public Object visit(Insert n, Object argu) {
 		Logger.debug("  call: insert");
 		// table
 
		String name = (String) n.f2.accept(this, null);
 
 		// column names
 		List<String> list = new ArrayList<String>();
 		n.f3.accept(this, list);
 
 		// values
 		List<String> values = new ArrayList<String>();
 		n.f6.accept(this, values);
 
		relationenalgebra.Insert op = new relationenalgebra.Insert(name, list,
 				values);
 		Logger.debug("  return: insert");
 		return op;
 	}
 
 	/**
 	 * f0 -> Name() f1 -> ( "," Name() )*
 	 */
 	public Object visit(ColumnNames n, Object argu) {
 		Logger.debug("    call: columnnames");
 		Object _ret;
 		_ret = n.f0.accept(this, argu);
 		n.f1.accept(this, argu);
 		Logger.debug("    return: columnnames");
 		return _ret;
 	}
 
 	/**
 	 * f0 -> <STRING_LITERAL> f1 -> ( "," <STRING_LITERAL> )*
 	 */
 	public Object visit(Literals n, Object argu) {
 		Object _ret = null;
 		Logger.debug("    call: values");
 		((List<Object>) argu).add(n.f0.toString());
 		n.f1.accept(this, argu);
 		Logger.debug("    return: values");
 		return _ret;
 	}
 
 	/**
 	 * f0 -> <CREATE> f1 ->
 	 * <TABLE>
 	 * f2 -> Name() f3 -> "(" f4 -> ColumnDefinition() f5 -> ( ","
 	 * ColumnDefinition() )* f6 -> ")"
 	 */
 	public Object visit(CreateTable n, Object argu) {
 		String name = (String) n.f2.accept(this, argu);
 		List<String> columns = new ArrayList<String>();
 		String column = (String) n.f4.accept(this, columns);
 		if (column != null)
 			columns.add(column);
 		n.f5.accept(this, columns);
 		relationenalgebra.CreateTable op = new relationenalgebra.CreateTable(
 				name, columns);
 		return op;
 	}
 
 	/**
 	 * f0 -> Name() f1 -> DataType()
 	 */
 	public Object visit(ColumnDefinition n, Object argu) {
 		Object _ret = null;
 		_ret = n.f0.accept(this, argu);
 		return _ret;
 	}
 
 	/**
 	 * f0 -> <SELECT> f1 -> Items() f2 -> <FROM> f3 -> Tables() f4 -> [ Where()
 	 * ]
 	 */
 	public Object visit(Query n, Object argu) {
 		Logger.debug("  call: query");
 
 		// get column names
 		List<String> columns = new ArrayList<String>();
 		n.f1.accept(this, columns);
 
 		// get tables
 		ITreeNode tables = (ITreeNode) n.f3.accept(this, argu);
 
 		// get where ... if one exists
 		Selection where = (Selection) n.f4.accept(this, argu);
 		Projection projection = new Projection(columns);
 
 		if (where != null) {
 			projection.setChild(where);
 			where.setChild(tables);
 		} else {
 			projection.setChild(tables);
 		}
 		Logger.debug("  return: query");
 		return projection;
 	}
 
 	/**
 	 * f0 -> <WHERE> f1 -> AndExpression()
 	 */
 	public Object visit(Where n, Object argu) {
 		Logger.debug("    call: where");
 		relationenalgebra.AndExpression expr = (relationenalgebra.AndExpression) n.f1
 				.accept(this, argu);
 		Selection selection = new Selection(expr);
 		Logger.debug("    return: where");
 		return selection;
 	}
 
 	/**
 	 * f0 -> OrExpression() f1 -> ( <AND> OrExpression() )*
 	 */
 	public Object visit(AndExpression n, Object argu) {
 		Logger.debug("      call: AndExpression");
 		List<relationenalgebra.OrExpression> exprs = new ArrayList<relationenalgebra.OrExpression>();
 		n.f0.accept(this, exprs);
 		n.f1.accept(this, exprs);
 		for (Object o : exprs) {
 			if (o instanceof String)
 				exprs.remove(o);
 		}
 		relationenalgebra.AndExpression and = new relationenalgebra.AndExpression(
 				exprs);
 		Logger.debug("      return: AndExpression");
 		return and;
 	}
 
 	/**
 	 * f0 -> [ "(" ] f1 -> EqualityExpression() f2 -> ( <OR>
 	 * EqualityExpression() )* f3 -> [ ")" ]
 	 */
 	public Object visit(OrExpression n, Object argu) {
 		Logger.debug("        call: OrExpression");
 		Object _ret = null;
 		List<relationenalgebra.EqualityExpression> exprs = new ArrayList<relationenalgebra.EqualityExpression>();
 		n.f1.accept(this, exprs);
 		n.f2.accept(this, exprs);
 		for (Object o : exprs) {
 			if (o instanceof String)
 				exprs.remove(o);
 		}
 		relationenalgebra.OrExpression or = new relationenalgebra.OrExpression(
 				exprs);
 		((List<relationenalgebra.OrExpression>) argu).add(or);
 		Logger.debug("        return: OrExpression");
 		return _ret;
 	}
 
 	/**
 	 * f0 -> PrimaryExpression() f1 -> [ ( "=" | "!=" | <LT> | <GT> | <LE> |
 	 * <GE> ) PrimaryExpression() ]
 	 * 
 	 * "lefthandside" und "righthandside"
 	 */
 	public Object visit(EqualityExpression n, Object argu) {
 		Logger.debug("          call: EqualityExpression");
 		Object _ret = null;
 		List<Object> stuff = new ArrayList<Object>();
 		n.f0.accept(this, stuff);
 		n.f1.accept(this, stuff);
 
 		relationenalgebra.EqualityExpression eq = new relationenalgebra.EqualityExpression(
 				relationenalgebra.EqualityExpression.Operator.parseOperator(stuff
 						.get(1).toString()),
 				(relationenalgebra.PrimaryExpression) stuff.get(0),
 				(relationenalgebra.PrimaryExpression) stuff.get(2));
 		((List<relationenalgebra.EqualityExpression>) argu).add(eq);
 		Logger.debug("          ----->" + eq.toString());
 		Logger.debug("          return: EqualityExpression");
 		return _ret;
 	}
 
 	/**
 	 * f0 -> <STRING_LITERAL>
 	 */
 	public Object visit(LiteralExpression n, Object argu) {
 		Object _ret = null;
 		_ret = n.f0.toString();
 		return _ret;
 	}
 
 	/**
 	 * f0 -> <IDENTIFIER> [ "." <IDENTIFIER> ] | LiteralExpression()
 	 */
 	public Object visit(PrimaryExpression n, Object argu) {
 		Object _ret = null;
 		Logger.debug("            call: PrimaryExpression");
 		List<Object> strings = new ArrayList<Object>();
 		String literal = (String) n.f0.accept(this, strings);
 		String value = "";
 		boolean isConstant = false;
 		if (!strings.isEmpty()) {
 			if (strings.size() == 1) {
 				value = strings.get(0).toString();
 			} else {
 				value = strings.get(0).toString() + strings.get(1).toString()
 						+ strings.get(2).toString();
 			}
 
 		} else {
 			value = literal;
 			isConstant = true;
 		}
 
 		((List<Object>) argu).add(new relationenalgebra.PrimaryExpression(
 				isConstant, value));
 
 		Logger.debug("            return: PrimaryExpression");
 		return _ret;
 	}
 
 	public Object visit(NodeToken n, Object argu) {
 		return n.toString();
 	}
 
 	public Object visit(NodeSequence n, Object argu) {
 		Object _ret = null;
 		int _count = 0;
 		for (Enumeration e = n.elements(); e.hasMoreElements();) {
 			Object o = ((Node) e.nextElement()).accept(this, argu);
 			if (argu instanceof List<?> && o != null && o.toString() != ","
 					&& o.toString() != "(" && o.toString() != ")")
 				((List<Object>) argu).add(o);
 			_count++;
 		}
 		return _ret;
 	}
 
 	/**
 	 * f0 -> Table() f1 -> ( "," Table() )*
 	 */
 	public Object visit(Tables n, Object argu) {
 		Logger.debug("    call: tables");
 		// collect all table names
 		List<Relation> tables = new ArrayList<Relation>();
 		Relation table = (Relation) n.f0.accept(this, null);
 		if (table != null)
 			tables.add(table);
 		n.f1.accept(this, tables);
 
 		ITreeNode _ret = null;
 		if (tables.isEmpty() == false) {
 			if (tables.size() == 1) {
 				_ret = tables.get(0);
 			} else { // min 2 names -> min one cross product
 				Iterator<Relation> iter = tables.iterator();
 				CrossProduct root = new CrossProduct(iter.next());
 				CrossProduct current = root;
 				while (iter.hasNext()) {
 
 					Relation rel = iter.next();
 					if (iter.hasNext()) {
 						// node
 						CrossProduct node = new CrossProduct(rel);
 						current.setSecondChild(node);
 						current = node;
 					} else {
 						// leaf
 						current.setSecondChild(rel);
 					}
 				}
 				_ret = root;
 			}
 		}
 
 		Logger.debug("    return: tables");
 		return _ret;
 	}
 
 	/**
 	 * f0 -> Name() f1 -> [ <AS> Name() ]
 	 */
 	public Object visit(Table n, Object argu) {
 		Logger.debug("      call: table");
 		List<String> list = new ArrayList<String>();
 		String s = (String) n.f0.accept(this, null);
 		n.f1.accept(this, list);
 		Logger.debug("      call: table");
 		if (list.isEmpty()) {
 			return new Relation(s);
 		} else {
 			return new Relation(s, list.get(1));
 		}
 
 	}
 
 	/**
 	 * f0 -> Item() f1 -> ( "," Item() )* WICHTIG --->>> das braucht man nur
 	 * wenn man am comma interesiert ist
 	 */
 	public Object visit(Items n, Object argu) {
 		Logger.debug("    call: items");
 		String name = null;
 		name = (String) n.f0.accept(this, argu);
 		if (name != null)
 			((List<String>) argu).add(name);
 		n.f1.accept(this, argu);
 		Logger.debug("    return: items");
 		return null;
 	}
 
 	/**
 	 * f0 -> Name() f1 -> [ "." Name() ]
 	 */
 	public Object visit(Item n, Object argu) {
 		Logger.debug("      call: item");
 		String _ret = null;
 		List<String> list = new ArrayList<String>();
 		_ret = (String) n.f0.accept(this, null);
 		n.f1.accept(this, list);
 		if (!list.isEmpty()) {
 			_ret += "." + list.get(1);
 		}
 		Logger.debug("      return: item");
 		return _ret;
 	}
 
 	/**
 	 * f0 -> <IDENTIFIER>
 	 */
 	public Object visit(Name n, Object argu) {
 		Logger.debug("        call: name");
 		Object _ret = null;
 		_ret = n.f0.accept(this, argu);
 		if (n != null && n.f0 != null) {
 			_ret = n.f0.toString();
 		}
 		Logger.debug("        return: name");
 		return _ret;
 	}
 }
