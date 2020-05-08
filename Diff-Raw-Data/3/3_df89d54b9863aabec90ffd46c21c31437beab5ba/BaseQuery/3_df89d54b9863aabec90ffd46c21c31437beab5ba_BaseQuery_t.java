 package internal.containers.query;
 
 import internal.containers.Statement;
 import internal.containers.StatementType;
 import internal.containers.condition.ICondition;
 import internal.containers.pattern.IPattern;
 import internal.tree.IWorldTree;
 
 public class BaseQuery extends Statement implements IQuery {
 	private String level;
 	private IPattern pattern;
 	private ICondition condition;
 	
 	public BaseQuery(String level, IPattern pattern, ICondition condition) {
 		super(StatementType.QUERY);
 		this.level		= level;
 		this.pattern	= pattern;
 		this.condition	= condition;
 	}
 	
 	@Override
 	public String toString() {
 		StringBuffer result = new StringBuffer("AT " + level + " " + pattern.toString());
 		if(condition != null)
 			result.append(" WHERE " + condition.toString());
 		
 		return result.toString();
 	}
 
 	@Override
 	public String debugString() {
 		StringBuffer result = new StringBuffer("QUERY(AT " + level + " " + pattern.debugString());
 		
 		if(condition != null)
 			result.append(" WHERE " + condition.debugString() + ")");
 		
 		return result.toString();
 	}
 
 	@Override
 	public IPattern pattern() {
 		return pattern;
 	}
 
 	@Override
 	public ICondition condition() {
 		return condition;
 	}
 
 	@Override
 	public IQuery subQuery() {
 		return null;
 	}
 
 	@Override
 	public Class<?> level() {
 		try {
			String className = level.substring(0, 1).toUpperCase() + level.toLowerCase().substring(1);
			return Class.forName("internal.tree.WorldTreeFactory$" + className);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
