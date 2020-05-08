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

			long time = Long.valueOf(s);

			// assume time as millisecond
			// if x1000 is larger than 9999-01-01
			long ms = time * 1000;

			if (ms >= 253402300799000L)
				return new Date(time);

			return new Date(ms);
 		} catch (Throwable t) {
 			return null;
 		}
 	}
 }
