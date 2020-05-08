 package org.araqne.logdb.query.expr;
 
 import java.util.Date;
 import java.util.List;
 
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.Row;
 
 public class Epoch extends FunctionExpression {
 
 	private Expression valueExpr;
 
 	public Epoch(QueryContext ctx, List<Expression> exprs) {
 		super("epoch", exprs, 1);
		
 		this.valueExpr = exprs.get(0);
 	}
 
 	@Override
 	public Object eval(Row map) {
 		Object value = valueExpr.eval(map);
 		if (value == null)
 			return null;
 
 		try {
 			String s = value.toString();
 			if (s.isEmpty())
 				return null;
			
			return new Date(Long.valueOf(s) * 1000);
 		} catch (Throwable t) {
 			return null;
 		}
 	}
 }
