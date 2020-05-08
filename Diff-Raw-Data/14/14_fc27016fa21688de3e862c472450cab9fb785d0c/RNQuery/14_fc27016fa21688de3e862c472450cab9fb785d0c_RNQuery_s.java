 package org.restnucleus.dao;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import cz.jirutka.rsql.parser.model.Comparison;
 import cz.jirutka.rsql.parser.model.ComparisonExpression;
 import cz.jirutka.rsql.parser.model.Expression;
 import cz.jirutka.rsql.parser.model.Logical;
 import cz.jirutka.rsql.parser.model.LogicalExpression;
 
 /**
  * An instance of this is used to collect query information during request
  * processing.
  * 
  * @author johann
  */
 public class RNQuery {
 	public final static long MAX_PAGE_SIZE = 1000;
 	public final static long DEF_PAGE_SIZE = 10;
 	public static final String BEFORE = "before";
 	public static final String BFORE_DESC = "query paramater for creationDate before x.";
 	public static final String AFTER = "after";
 	public static final String AFTER_DESC = "query paramater for creationDate after x.";
 	public static final String QUERY_PARAM = "org.restnucleus.Query";
 	public static final String FILTER = "filter";
 	public static final String FILTER_DESC = "like param1::value1|param2::value2";
 	public static final String SORT = "sort";
 	public static final String SORT_DESC = "like param1|-param2  (- for ascending)";
 	public static final String PAGE = "page";
 	public static final String PAGE_DESC = "page number for pagination";
 	public static final String SIZE = "size";
 	public static final String SIZE_DESC = "page-size for pagination";
 
 	private Expression e = null;
 
 	private String ordering = null;
 
 	private Long page = null;
 
 	private Long size = null;
 	
 	private int varCount = 0;
 
 	private Map<String, Object> queryObjects = new HashMap<String, Object>();
 
 	public String getJdoFilter() {
 		return getJdoFilter(this.e);
 	}
 
 	public String getJdoFilter(Expression e) {
 		if (null == e)
 			return null;
 		if (e.isComparison()) {
 			ComparisonExpression ce = (ComparisonExpression) e;
 			String arg = null;
 			if (ce.getArgument().contains("_noQuotes_"))
 				arg = ce.getArgument().substring(10, ce.getArgument().length());
 			else
 				arg = "\"" + ce.getArgument() + "\"";
 			if (ce.getOperator() == Comparison.EQUAL)
 				return ce.getSelector() + " == " + arg;
 			else
 				return ce.getSelector() + " " + ce.getOperator() + " " + arg;
 		} else {
 			LogicalExpression le = (LogicalExpression) e;
 			if (le.getOperator() == Logical.AND)
 				return "(" + getJdoFilter(le.getLeft()) + " && "
 						+ getJdoFilter(le.getRight()) + ")";
 			else
 				return "(" + getJdoFilter(le.getLeft()) + " || "
 						+ getJdoFilter(le.getRight()) + ")";
 		}
 	}
 
 	public String getFilter(String key) {
		return ((ComparisonExpression) getFilter(this.e, key)).getArgument();
 	}
 
 	public Expression getExpression() {
 		return this.e;
 	}
 
 	public RNQuery addExpression(Expression exp) {
 		if (null == this.e) {
 			this.e = exp;
 		} else {
 			this.e = new LogicalExpression(this.e, Logical.AND, exp);
 		}
 		return this;
 	}
 
 	public RNQuery addFilter(String key, String value) {
 		ComparisonExpression ce = new ComparisonExpression(key,
 				Comparison.EQUAL, value);
 		if (null == this.e) {
 			this.e = ce;
 		} else {
 			this.e = new LogicalExpression(this.e, Logical.AND, ce);
 		}
 		return this;
 	}
 
 	public boolean hasFilter(String key) {
 		return (null != getFilter(this.e, key));
 	}
 
 	public Expression getFilter(Expression e, String key) {
 		if (e.isComparison()) {
 			ComparisonExpression ce = (ComparisonExpression) e;
 			if (ce.getSelector().equals(key))
 				return ce;
 			else
 				return null;
 		} else {
 			LogicalExpression le = (LogicalExpression) e;
 			Expression rv = getFilter(le.getLeft(), key);
 			if (null == rv)
 				rv = getFilter(le.getRight(), key);
 			return rv;
 		}
 	}
 
 	public RNQuery clearFilter() {
 		this.e = null;
 		return this;
 	}
 
 	public String getOrdering() {
 		return ordering;
 	}
 
 	public RNQuery setOrdering(String ordering) {
 		this.ordering = ordering;
 		return this;
 	}
 
 	/*
 	 * we handle pagination logic here
 	 */
 	public RNQuery setRange(Long page, Long size) {
 		if (null == page || page < 0)
 			this.page = 0L;
 		else
 			this.page = page;
 		if (null == size || size < 1)
 			this.size = DEF_PAGE_SIZE;
 		else if (size > MAX_PAGE_SIZE)
 			this.size = MAX_PAGE_SIZE;
 		else
 			this.size = size;
 		return this;
 	}
 
 	public Long getFrom() {
 		if (null == page || null == size)
 			return 0L;
 		return page * size;
 	}
 
 	public Long getTo() {
 		if (null == page || null == size)
 			return DEF_PAGE_SIZE;
 		return getFrom() + size;
 	}
 
 	public Long getPage() {
 		if (null == page)
 			return 0L;
 		return this.page;
 	}
 
 	public Long getSize() {
 		if (null == size)
 			return DEF_PAGE_SIZE;
 		return this.size;
 	}
 
 	public Query getJdoQ(PersistenceManager pm, Class<? extends Model> clazz) {
 		Query rv = pm.newQuery(clazz);
 		rv.setFilter(this.getJdoFilter());
 		rv.setRange(this.getFrom(), this.getTo());
 		rv.setOrdering(this.getOrdering());
 		if (queryObjects != null) {
 			StringBuffer params = new StringBuffer();
 			StringBuffer imports = new StringBuffer();
 			for (Entry<String, Object> e : queryObjects.entrySet()) {
 				if (params.length() > 3)
 					params.append(", ");
 				params.append(e.getValue().getClass().getSimpleName() + " "
 						+ e.getKey());
 				if (imports.length() > 3)
 					imports.append("; ");
 				imports.append("import " + e.getValue().getClass().getName());
 			}
 			if (queryObjects.size() > 0) {
 				rv.declareImports(imports.toString());
 				if (params.length() > 3)
 					rv.declareParameters(params.toString());
 			}
 		}
 		// some optimizations
 		rv.addExtension("datanucleus.query.flushBeforeExecution", "true");
 		return rv;
 	}
 
 	public Map<String, Object> getQueryObjects() {
 		return this.queryObjects;
 	}
 
 	public RNQuery setBefore(Date date) {
 		addObjectQuery("creationTime", Comparison.LESS_THAN, date);
 		return this;
 	}
 
 	public RNQuery setAfter(Date date) {
 		addObjectQuery("creationTime", Comparison.GREATER_THAN, date);
 		return this;
 	}
 
 	public RNQuery addQueryObject(String name, Object value) {
 		addObjectQuery(name, Comparison.EQUAL, value);
 		return this;
 	}
 
 	public RNQuery addObjectQuery(String name, Comparison c, Object value) {
 		String varName = genName();
 		addExpression(new ComparisonExpression(name, c, "_noQuotes_" + varName));
 		queryObjects.put(varName, value);
 		return this;
 	}
 	
 	protected String genName(){
 		varCount++;
 		return "object"+varCount;
 	}
 
 }
