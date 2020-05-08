 package fatworm.logicplan;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.tree.CommonTree;
 import org.antlr.runtime.tree.CommonTreeNodeStream;
 
 import fatworm.absyn.*;
 import fatworm.dataentity.DataEntity;
 import fatworm.parser.FatwormLexer;
 import fatworm.parser.FatwormParser;
 import fatworm.plantree.*;
 import fatworm.record.Schema;
 
 public class PlanGen {
 	public static void printTree(CommonTree t){
 		if (t.getChildCount() != 0) {
 			System.out.print("(");
 		}
 		System.out.print(t.getText());
 		if (t.getChildCount() != 0) {
 				
 		}
 		for (int i = 0; i < t.getChildCount(); i++){
 			
 			printTree((CommonTree)t.getChild(i));
 		}
 		if (t.getChildCount() != 0)
 			System.out.print(")");
 	}
 	
 	/**
 	 * select the first child of the certain root having a given Text 
 	 * return null if there isn't any 
 	 * @param root
 	 * @param pattern
 	 * @return
 	 */
 	public static CommonTree selectChild(CommonTree root, String pattern){
 		for (int i = 0; i < root.getChildCount(); i++){
 			if (root.getChild(i).getText().startsWith(pattern)){
 				return (CommonTree)root.getChild(i);
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * join all the tables or queries together and
 	 * generate a Join node as the root of all the other nodes
 	 * 
 	 * @param query		the root of the query tree
 	 * @param current	the current root of the generated query plan tree 
 	 * @return 
 	 */
 	public static Node processFrom(CommonTree query, Node current){
 		Join join = new Join();
 		CommonTree tree = selectChild(query, "From");
 		if (tree == null) return current;
 		CommonTree refList = (CommonTree)tree.getChild(0); // refList.getText() = "TableRefList"
 		for (int j = 0; j < refList.getChildCount(); j++){
 			CommonTree childJ = (CommonTree) refList.getChild(j).getChild(0);
 			if (childJ.getText().startsWith("SimpleRef")){
 				/**
 				 * the table is referred by an IDENTIFIER stored in the first child's Text
 				 */
 				join.childList.add(new Table(childJ.getChild(0).getText()));
 			} else 
 			if (childJ.getText().startsWith("QueryRef")){
 				/**
 				 *  queryRef(query,IDENTIFIER)
 				 *  first child is the subtree of the query
 				 *  second child's Text is the IDENTIFIER(alias)
 				 */
 				CommonTree temp = (CommonTree)childJ.getChild(0); 
 				Rename r = new Rename(childJ.getChild(1).getText());  							
 				r.childList.add((Node)planGen(temp));
 				join.childList.add(r);
 			} else if (childJ.getText().startsWith("RenameRef")){
 				/**
 				 * the table is referred by the real table name and an alias is given
 				 * tableRef(RenameRef(table,alias))
 				 */
 				Rename r = new Rename(childJ.getChild(1).getText());
 				r.childList.add(new Table(childJ.getChild(0).getText()));
 				join.childList.add(r);
 			}
 		}
 		if (current == null) 
 			current = join;
 		else
 			current.childList.add(join);
 		return current;
 	}
 	
 	/**
 	 * generate a GroupBy node as the root of the current node
 	 * @param query 	the root of the query tree
 	 * @param current 	the current root of the generated query plan tree 
 	 * @return
 	 */
 	public static Node processGroupBy(CommonTree query, Node current){
 		CommonTree tree = selectChild(query, "GroupBy");
 		if (tree == null) return current;
 		ColName colName;
 		CommonTree temp = (CommonTree)tree.getChild(0).getChild(0);
 		if (temp.getText().startsWith("SimpleColumn")){
 			colName = new SimpleCol(temp);
 		} else {
 			colName = new FieldCol(temp);
 		}
 		GroupBy gb = new GroupBy(colName);
 
 		if (current != null) {
 			current.parent = gb;
 			gb.childList.add(current);
 		}
 		return gb;
 	}
 	
 	/**
 	 * generate a OrderBy node as the root of the current node
 	 * @param query	  the root of the query tree
 	 * @param current the current root of the generated logical query plan tree
 	 * @return
 	 */
 	public static Node processOrderBy(CommonTree query, Node current){
 		CommonTree tree = selectChild(query, "OrderBy");
 		if (tree == null) {
 			return current;
 		}
 		LinkedList<OrderByColumn> cnl = new LinkedList<OrderByColumn>();
 		CommonTree keyList = (CommonTree)tree.getChild(0);
 		for (int j = 0; j < keyList.getChildCount(); j++){
 			ColName colName = ColName.getColName((CommonTree)keyList.getChild(j).getChild(0).getChild(0));
 			String desc = keyList.getChild(j).getChild(1).getText();
 			desc = desc.toUpperCase();
 			boolean d = false;
 			if (desc.startsWith("DESC")){
 				d = true;
 			}
 			OrderByColumn orderByColumn = new OrderByColumn(colName,d);
 			cnl.add(orderByColumn);
 		}
 		OrderBy orderBy = new OrderBy(cnl);
 
 		if (current != null) {
 			current.parent = orderBy;
 			orderBy.childList.add(current);			
 		}
 		return orderBy;
 	}
 	
 	/**
 	 * generate a Distinct node as the root of the current node
 	 * @param query		the root of the query tree
 	 * @param current	the current root of the generated logical query plan tree
 	 * @return
 	 */
 	public static Node processDistinct(CommonTree query, Node current){
 		CommonTree tree = selectChild(query, "Distinct");
 		if (tree == null) return current;
 		Distinct distinct = new Distinct();
 
 		if (current != null) {
 			current.parent = distinct;
 			distinct.childList.add(current);			
 		}
 		return distinct;
 	}
 
 	
 	
 	/**
 	 * generate a projection node as the root of attributes
 	 * @param query
 	 * @param current
 	 * @return
 	 */
 	public static Node processSelectColumn(CommonTree query, Node current){
 		CommonTree tree = selectChild(query, "SelectColumn");
 		if (tree == null) return current;
 		LinkedList<ProjectionValue> valList = new LinkedList<ProjectionValue>();
 		CommonTree exprList = (CommonTree)tree.getChild(0);
 		for (int j = 0; j < exprList.getChildCount(); j++){
 			/**
 			 * SelectColumn(SelectExprList(SelectExpr(SimpleValue/RenameValue/AllColumn)))
 			 * 					^							^		
 			 * 					|							|
 			 * 					|							|
 			 * 				 exprList	 	 			  ChildJ
 			 */
 			CommonTree childJ = (CommonTree)exprList.getChild(j).getChild(0);
 			ProjectionValue val = null;
 			if (childJ.getText().startsWith("AllColumn")){
 				val =  new ProjectionAllColumnValue(new ConstDefault("AllColumn"));
 			}
 			if (childJ.getText().startsWith("RenameValue")){
 				val = new ProjectionRenameValue(getValue((CommonTree)childJ.getChild(0)), childJ.getChild(1).getText());
 			}
 			if (childJ.getText().startsWith("SimpleValue")){
 				val = new ProjectionSimpleValue(getValue((CommonTree)childJ.getChild(0)));
 			}
 			valList.add(val);
 		}
 		Projection projection = new Projection(valList);
 		
 		if (current != null) {
 			current.parent = projection;
 			projection.childList.add(current);			
 		}
 		return projection;
 	}
 	
 	/**
 	 * 
 	 * @param query
 	 * @param current
 	 * @return
 	 */
 	public static Node processWhereCondition(CommonTree query, Node current){
 		CommonTree tree = selectChild(query, "WhereCondition");
 		if (tree == null) return current;
 		Select select = new Select(getBoolExpr((CommonTree)tree.getChild(0)));
 		if (current != null) {
 			current.parent = select;
 			select.childList.add(current);
 		}
 		return select;
 	}
 	
 	/**
 	 * 
 	 * @param query
 	 * @param current
 	 * @return
 	 */
 	public static Node processHavingCondition(CommonTree query, Node current){
 		CommonTree tree = selectChild(query, "HavingCondition");
 		if (tree == null) return current;
 		Select select = new Select(getBoolExpr((CommonTree)tree.getChild(0)));
 		if (current != null) {
 			current.parent = select;
 			select.childList.add(current); 
 		}
 		return select;
 		
 	}
 	
 	public static void printNode(PrintWriter writer, int level, Node cur){
 		if (cur == null) return;
 		writer.println(level+"\t"+cur.toString());
 		for (Node n : cur.childList){
 			printNode(writer, level+1, n);
 		}
 	}
 	
 	/**
 	 * get the value of the subtree
 	 * @param tree
 	 * @return
 	 */
 	static String regx = "-+/%*";
 	public static Value getValue(CommonTree tree){
 		/*if (tree.getText().startsWith("AllColumn")){
 			System.out.println("bazinga");
 			return new ConstDefault("AllColumn");
 		}
 		if (tree.getText().startsWith("RenameValue")){
 			System.out.println("bazinga");
 			return null;//RenameValue(getValue((CommonTree)tree.getChild(0)), tree.getChild(1).getText());
 		}
 		if (tree.getText().startsWith("SimpleValue")){
 			System.out.println("bazinga");
 			return getValue((CommonTree)tree.getChild(0));
 		}*/
 		String text = tree.getText();
 		if (text.startsWith("ConstValue")) {
 			return getValue((CommonTree)tree.getChild(0));
 		}
 		String root = tree.getText();
 		if (root != null && "-+/%*".indexOf(root) != -1){
 			/**
 			 * a operator and two operands
 			 */
 			Value left = getValue((CommonTree)tree.getChild(0));
 			Value right = getValue((CommonTree)tree.getChild(1));
 			return new OpValue(root, left, right);
 		}
 		if (root != null && root.startsWith("Unary")) {
 			Value left = new ConstInt("0");
 			Value right = getValue((CommonTree)tree.getChild(0));
 			return new OpValue("-", left, right);
 		}
 		if (text.startsWith("ColumnName")){
 			ColName colName;
 			CommonTree temp = (CommonTree)tree.getChild(0);
 			if (temp.getText().startsWith("SimpleColumn")){
 				colName = new SimpleCol(temp);
 			} else {
 				colName = new FieldCol(temp);
 			}
 			return new ColumnValue(colName);
 		}
 		if (text.startsWith("Func")){
 			text = text.substring(4);
 			text.trim();
 			String func = tree.getChild(0).getText();
 			Value val = getValue((CommonTree)tree.getChild(1));
 			return new FuncValue(func, val);
 		}
 		if (text.startsWith("ConstInt")) 
 			return new ConstInt(tree.getChild(0).getText());
 		if (text.startsWith("ConstFloat")) 
 			return new ConstFloat(tree.getChild(0).getText());
 		if (text.startsWith("ConstTimeStamp")) 
 			return new ConstTimeStamp(tree.getChild(0).getText());
 		if (text.startsWith("ConstString")) 
 			return new ConstString(tree.getChild(0).getText());
 		if (text.startsWith("ConstNull")) 
 			return new ConstNull(tree.getChild(0).getText());
 		if (text.startsWith("ConstDefault")) 
 			return new ConstDefault(tree.getChild(0).getText());
 		if (text.startsWith("ConstBoolean"))
 			return new ConstBoolean(tree.getChild(0).getText());
 		if (text.startsWith("QueryValue"))
 			return new QueryValue((Node)planGen((CommonTree)tree.getChild(0)));
 		// must not be reached
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param tree
 	 * @return
 	 */
 	public static BoolExpr getBoolExpr(CommonTree tree){
 		if (tree.getText().startsWith("AndList")){
 			LinkedList<BoolExpr> andList = new LinkedList<BoolExpr>();
 			for (int j = 0; j < tree.getChildCount(); j++){
 				CommonTree childJ = (CommonTree)tree.getChild(j);
 				andList.add(getBoolExpr(childJ));
 			}
 			return new AndList(andList);
 		}
 		if (tree.getText().startsWith("OrList")){
 			LinkedList<BoolExpr> orList = new LinkedList<BoolExpr>();
 			for (int j = 0; j < tree.getChildCount(); j++){
 				CommonTree childJ = (CommonTree)tree.getChild(j);
 				orList.add(getBoolExpr(childJ));
 			}
 			return new OrList(orList);
 		}
 		if (tree.getText().startsWith("In")){
 			Value val = getValue((CommonTree)tree.getChild(0));
 			Node query = (Node)planGen((CommonTree)tree.getChild(1));
 			return new InExpr(val, query);
 		}
 		if (tree.getText()=="Compare"){
 			Value left = getValue((CommonTree)tree.getChild(0));
 			Value right = getValue((CommonTree)tree.getChild(1));
 			String cop = tree.getChild(2).getText();
 			return new CompareExpr(left, right, cop);
 		}
 		if (tree.getText()=="CompareAny"){
 			Value val = getValue((CommonTree)tree.getChild(0));
 			Node query = (Node)planGen((CommonTree)tree.getChild(1));
 			String cop = tree.getChild(2).getText();
 			return new CompareAnyExpr(val, query, cop);
 		}
 		if (tree.getText()=="CompareAll"){
 			Value val = getValue((CommonTree)tree.getChild(0));
 			Node query = (Node)planGen((CommonTree)tree.getChild(1));
 			String cop = tree.getChild(2).getText();
 			return new CompareAllExpr(val, query, cop);
 		}
 		if (tree.getText() == "Exist") {
 			Node query = (Node)planGen((CommonTree)tree.getChild(0));
 			return new ExistExpr(false, query);
 		}
 		if (tree.getText() == "NotExist") {
 			Node query = (Node)planGen((CommonTree)tree.getChild(0));
 			return new ExistExpr(true, query);
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param t   the parse result 
 	 * @return	the logical query plan tree
 	 */
 	public static SqlStatement planGen(CommonTree t) {
 		SqlStatement current = null;
 		
 		if (t.getText().startsWith("Query")) {
 			current = processFrom(t, (Node)current);
 			current = processWhereCondition(t, (Node)current);
 			current = processGroupBy(t, (Node)current);			
 			current = processHavingCondition(t, (Node)current);
 			current = processSelectColumn(t, (Node)current);
 			current = processOrderBy(t, (Node)current);
 			current = processDistinct(t, (Node)current);
 		}
 		if (t.getText().startsWith("CreateDatabase")){
 			current = new CreateDatabase(t.getChild(0).getText());
 		}
 		if (t.getText().startsWith("UseDatabase")){
 			current = new UseDatabase(t.getChild(0).getText());
 		}
 		if (t.getText().startsWith("CreateTable")){
 			String tableName = t.getChild(0).getText();
 			//System.out.println(tableName);
 			LinkedList<ColumnDef> columnDefList = new LinkedList<ColumnDef>();
 			LinkedList<String> primaryKeyList = new LinkedList<String>();
 			Schema schema = new Schema();
 			CommonTree tree = (CommonTree)t.getChild(1);
 			//System.out.println(tree.getText());
 			for (int j = 0; j < tree.getChildCount(); j++){
 				CommonTree childJ = (CommonTree)tree.getChild(j);
 				if (childJ.getText().startsWith("ColumnDef")){
 					ColumnDef columnDef = getColumnDef(childJ);
 					columnDefList.add(columnDef);
 					//System.out.println(childJ.getText());
 					//schema.addField(columnDef.colName, columnDef.type, columnDef.length, columnDef.isNotNull, columnDef.autoIncrement, columnDef.defaultValue.getValue(null));
 				} else {
 					//System.out.println(childJ.getText()+" "+childJ.getChild(0).getText());
 					primaryKeyList.add(childJ.getChild(0).getText());
 				}
 			}
 			for (int p = 0; p < primaryKeyList.size(); p++){
 				String primaryKey = primaryKeyList.get(p);
 				for (int q = 0; q < columnDefList.size(); q++){
 					if (columnDefList.get(q).colName.startsWith(primaryKey)){	
 						columnDefList.get(q).primary = true;
 					}
 				}
 			}
 			for (int j = 0; j < columnDefList.size(); j++){
 				ColumnDef c = columnDefList.get(j);
 				DataEntity defaultValue = null;
 				if(c.defaultValue != null)
 					defaultValue = c.defaultValue.getValue(null);
				schema.addField(c.colName, c.type, c.length, c.isNotNull, c.autoIncrement, c.primary, defaultValue.toType(c.type));
 			}
 			current = new CreateTable(tableName,schema);
 		}
 		if (t.getText().startsWith("InsertStmt")){
 			String tableName = t.getChild(0).getText();
 			ArrayList<Value> valueList = null;
 			LinkedList<ColName> colNameList = null;
 			Node query = null;
 			for (int i = 1; i < t.getChildCount(); i++){
 				CommonTree tree = (CommonTree)t.getChild(i);
 				if (tree.getText().startsWith("ColNameList")){
 					colNameList = new LinkedList<ColName>();
 					for (int j = 0; j < tree.getChildCount(); j++){
 						if (tree.getChild(j).getChild(0).getText().startsWith("SimpleColumn")){
 							SimpleCol simpleCol = new SimpleCol((CommonTree)tree.getChild(j).getChild(0));
 							colNameList.add(simpleCol);
 						} else {
 							FieldCol fieldCol = new FieldCol((CommonTree)tree.getChild(j).getChild(0));
 							colNameList.add(fieldCol);
 						}
 					}
 					continue;
 				}
 				if (tree.getText().startsWith("Query")){
 					query = (Node)planGen(tree);
 					continue;
 				}
 				if (tree.getText().startsWith("ValueList")){
 					valueList = new ArrayList<Value>();
 					for (int j = 0; j < tree.getChildCount(); j++){
 						Value value = getValue((CommonTree)tree.getChild(j));
 						valueList.add((Value)value);
 					}
 					continue;
 				}
 			}
 			if (colNameList != null){
 				//FieldInsert
 				FieldInsert fieldInsert = new FieldInsert(tableName);
 				for (int i = 0; i < colNameList.size(); i++){
 					fieldInsert.assigns.put(colNameList.get(i).toString(), (Value)valueList.get(i));
 				}
 				current = fieldInsert;
 			} else 
 			if (query != null){
 				//QueryInsert
 				QueryInsert queryInsert = new QueryInsert(tableName);
 				queryInsert.query = query;
 				current = queryInsert;
 			} else {
 				//SimpleInsert
 				SimpleInsert simpleInsert = new SimpleInsert(tableName);
 				simpleInsert.values = valueList;
 				current = simpleInsert;
 			}
 		}
 		if (t.getText().startsWith("DeleteStmt")){
 			String tableName = t.getChild(0).getText();
 			BoolExpr condition = null;
 			if (t.getChildCount() > 1)
 				condition = getBoolExpr((CommonTree)t.getChild(1).getChild(0));
 			current = new DeleteCommand(tableName);
 			((DeleteCommand)current).condition = condition;
 		}
 		if (t.getText().startsWith("UpdateStmt")){
 			String tableName = t.getChild(0).getText();
 			
 			current = new UpdateCommand(tableName);
 			
 			for (int i = 1; i < t.getChildCount(); i++){
 				if (t.getChild(i).getText().startsWith("Condition")){
 					((UpdateCommand)current).condition = getBoolExpr((CommonTree)t.getChild(i).getChild(0));
 					continue;
 				}
 				if (t.getChild(i).getText().startsWith("Assign")){
 					CommonTree tree = (CommonTree)t.getChild(i);
 					
 					for (int j = 0; j < tree.getChildCount(); j++){
 						((UpdateCommand)current).assigns.put(tree.getChild(j).getChild(0).getText(), getValue((CommonTree)tree.getChild(j).getChild(1)));
 					}
 				}
 			}
 		}
 		if (t.getText().startsWith("DropTable")){
 			DropTable dropTable = new DropTable(t.getChild(0).getText());
 			dropTable.tableList.add(t.getChild(0).getText());
 			for (int i = 0; i < t.getChild(0).getChildCount(); i++){
 				dropTable.tableList.add(t.getChild(0).getChild(i).getText());
 			}
 			//System.out.println(dropTable.tableList.get(1));
 			//dropTable.tableList.add(e)
 			current = dropTable;
 		}
 		if (t.getText().startsWith("DropDatabase")){
 			DropDatabase dropDatabase = new DropDatabase(t.getChild(0).getText());
 			current = dropDatabase;
 		}
 		//System.out.println(t);
 		return current;
 	}
 	public static ColumnDef getColumnDef(CommonTree childJ) {
 		ColumnDef columnDef = new ColumnDef(childJ);
 		for (int i = 2; i < childJ.getChildCount(); i++){
 			CommonTree columnDes = (CommonTree)childJ.getChild(i);
 			//NULL
 			if (columnDes.getChild(0).getText().startsWith("NULL")){
 				columnDef.setIsNull();
 				continue;
 			}
 			//AUTO_INCREMENT
 			if (columnDes.getChild(0).getText().startsWith("AUTO")){
 				columnDef.setAutoIncrement();
 				continue;
 			}
 			//NOT NULL
 			if (columnDes.getChild(0).getText().startsWith("NOT") && columnDes.getChild(1).getText().startsWith("NULL")){
 				columnDef.setIsNotNull();
 				continue;
 			}
 			if (columnDes.getChild(0).getText().startsWith("DefaultValue")) {
 				Value defaultValue = getValue((CommonTree)columnDes.getChild(1));
 				columnDef.defaultValue = (ConstValue) defaultValue;
 			}
 			//System.out.println("defaultValue=\t"+defaultValue.toString());
 		}
 		return columnDef;
 	}
 	static PrintWriter writer = null;
 	public static void main(String[] args) throws Exception {
 
         //initial
 		File file = new File("/home/hxr/fatworm/input.txt");
     	InputStream inp = new FileInputStream(file);
         ANTLRInputStream input = new ANTLRInputStream(inp);
         writer = new PrintWriter(new FileOutputStream("/home/hxr/fatworm/output.txt"));
         FatwormLexer lexer = new FatwormLexer(input);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         
         //parser 
         FatwormParser parser = new FatwormParser(tokens);
         FatwormParser.prog_return r= parser.prog();
         CommonTree t = (CommonTree)r.getTree();
         CommonTreeNodeStream ns = new CommonTreeNodeStream(t);
         System.out.println(t.toStringTree());
         //System.out.println(t.getChildCount());
         //logical query plan/ relation algebra tree
         LinkedList<SqlStatement> nodeList = new LinkedList<SqlStatement>();
         for (int i = 0; i < t.getChildCount(); i++){
         	SqlStatement sqlStatement = planGen((CommonTree)t.getChild(i));
         	nodeList.add(sqlStatement);
         }
         
         //output
         //printNode(writer, 0, nodeList.getLast());
         writer.flush();
     }
 }
