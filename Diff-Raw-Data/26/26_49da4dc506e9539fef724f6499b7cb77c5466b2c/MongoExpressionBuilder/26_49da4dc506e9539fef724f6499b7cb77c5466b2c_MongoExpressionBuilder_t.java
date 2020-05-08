 package com.anyconsole.db;
 
 import java.util.List;
 
 import net.sf.jsqlparser.expression.Expression;
 import net.sf.jsqlparser.schema.Column;
 import net.sf.jsqlparser.schema.Table;
 import net.sf.jsqlparser.statement.select.PlainSelect;
 import net.sf.jsqlparser.statement.update.Update;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.WriteResult;
 
 public class MongoExpressionBuilder implements Builder {
 	private String result = "";
 	private MongoPlugin mongoPlugin;
 	private static String DB_NAME = "hackathon";
 
 	
 	public MongoExpressionBuilder(MongoPlugin mongoPlugin) {
 		this.mongoPlugin = mongoPlugin;
 	}
 	
 	@Override
 	public String getResult() {
 		return result;
 	}
 
 	@Override
 	public void doSelect(PlainSelect plainSelect) {
 		DBCollection coll = mongoPlugin.getDatastore(DB_NAME).getCollection(plainSelect.getFromItem().getAlias());
 
 	}
 
 	@Override
 	public void doUpdate(Table table, Update update) {
 		DBCollection coll = mongoPlugin.getDatastore(DB_NAME).getCollection(table.getName());
 		List<Column> colNames = update.getColumns();
 		List<Expression> values = update.getExpressions();
 		BasicDBObject updateExpr = new BasicDBObject();
		BasicDBObject statement = new BasicDBObject("$set", updateExpr);
 		for (int i = 0; i < colNames.size(); ++i) {
			updateExpr.append(colNames.get(i).getColumnName(), MongoStringUtils.trimSingleQuotes(values.get(i).toString()));
 		}
 		
 		BasicDBObject where = new BasicDBObject();
 		MongoWhereExprVisitor exprVisitor = new MongoWhereExprVisitor(where);
 		Expression updateWhere = update.getWhere();
 		if (updateWhere != null) {
 			updateWhere.accept(exprVisitor);
 		}
 			
		WriteResult wr = coll.update(exprVisitor.getExpression(), statement);
 		result = wr.toString();
 	}
 
 }
