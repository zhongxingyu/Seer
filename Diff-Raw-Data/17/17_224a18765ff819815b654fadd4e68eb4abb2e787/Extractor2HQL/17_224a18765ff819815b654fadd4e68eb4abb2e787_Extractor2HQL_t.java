 /*
  * Copyright (c) 2008, 2009
  *  Rough Diamond Co., Ltd.              -- http://www.rough-diamond.co.jp/
  *  Information Systems Institute, Ltd.  -- http://www.isken.co.jp/
  *  All rights reserved.
  */
 package jp.rough_diamond.commons.service.hibernate;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import jp.rough_diamond.commons.extractor.And;
 import jp.rough_diamond.commons.extractor.Avg;
 import jp.rough_diamond.commons.extractor.CombineCondition;
 import jp.rough_diamond.commons.extractor.Condition;
 import jp.rough_diamond.commons.extractor.Count;
 import jp.rough_diamond.commons.extractor.Desc;
 import jp.rough_diamond.commons.extractor.Eq;
 import jp.rough_diamond.commons.extractor.ExtractValue;
 import jp.rough_diamond.commons.extractor.Extractor;
 import jp.rough_diamond.commons.extractor.FreeFormat;
 import jp.rough_diamond.commons.extractor.Ge;
 import jp.rough_diamond.commons.extractor.Gt;
 import jp.rough_diamond.commons.extractor.In;
 import jp.rough_diamond.commons.extractor.InnerJoin;
 import jp.rough_diamond.commons.extractor.IsNotNull;
 import jp.rough_diamond.commons.extractor.IsNull;
 import jp.rough_diamond.commons.extractor.Join;
 import jp.rough_diamond.commons.extractor.LabelHoldingCondition;
 import jp.rough_diamond.commons.extractor.Le;
 import jp.rough_diamond.commons.extractor.Like;
 import jp.rough_diamond.commons.extractor.Lt;
 import jp.rough_diamond.commons.extractor.Max;
 import jp.rough_diamond.commons.extractor.Min;
 import jp.rough_diamond.commons.extractor.NotEq;
 import jp.rough_diamond.commons.extractor.NotIn;
 import jp.rough_diamond.commons.extractor.Or;
 import jp.rough_diamond.commons.extractor.Order;
 import jp.rough_diamond.commons.extractor.Property;
 import jp.rough_diamond.commons.extractor.RegularExp;
 import jp.rough_diamond.commons.extractor.Sum;
 import jp.rough_diamond.commons.extractor.SummaryFunction;
 import jp.rough_diamond.commons.extractor.Value;
 import jp.rough_diamond.commons.extractor.ValueHoldingCondition;
 import jp.rough_diamond.commons.util.PropertyUtils;
 import jp.rough_diamond.framework.transaction.hibernate.HibernateUtils;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.EntityMode;
 import org.hibernate.LockMode;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.hibernate.dialect.Dialect;
 import org.hibernate.dialect.Oracle10gDialect;
 import org.hibernate.dialect.PostgreSQLDialect;
 import org.hibernate.engine.SessionImplementor;
 import org.hibernate.engine.query.HQLQueryPlan;
 import org.hibernate.impl.SessionFactoryImpl;
 import org.hibernate.metadata.ClassMetadata;
 import org.hibernate.type.Type;
 import org.hibernate.type.TypeFactory;
 
 /**
  * ExtractorIuWFNgHibernateHQL𐶐
  * {NX́AServicewȊOł̓ۏ͂Ȃ 
  */
 @SuppressWarnings("unchecked")
 public class Extractor2HQL {
     private final static Log log = LogFactory.getLog(Extractor2HQL.class);
 
     private final Extractor extractor;
     private StringBuilder builder;
     private int patemeterIndex;
     private boolean usingGroupBy = false;
     
     private Extractor2HQL(Extractor extractor) {
         this.extractor = extractor;
         builder = new StringBuilder();
         patemeterIndex = 0;
     }
     
     /**
      * ExtractorIuWFNgHibernateHQL𐶐
      * @param extractor oi[IuWFNg
      * @return              HibernateHQL
      */
     public static Query extractor2Query(Extractor extractor, LockMode lockMode) {
         Extractor2HQL tmp = new Extractor2HQL(extractor);
         Query q = tmp.makeQuery(lockMode);
         tmp.setParameter(q);
         int offset = extractor.getOffset();
         if(offset > 0) {
             q.setFirstResult(offset);
         }
         int limit = extractor.getLimit();
         if(limit > 0) {
             q.setMaxResults(limit);
         }
         if(extractor.getValues().size() == 0) {
         	q.setLockMode(getAlias(extractor.target, extractor.targetAlias), lockMode);
         }
         if(extractor.getFetchSize() != Extractor.DEFAULT_FETCH_SIZE) {
         	q.setFetchSize(extractor.getFetchSize());
         }
         return q;
     }
 
 	public static Query extractor2CountQuery(Extractor extractor) {
         Extractor2HQL tmp = new Extractor2HQL(extractor);
         Query q = tmp.makeCountQuery();
         tmp.setParameter(q);
         return q;
 	}
 
 	/**
 	 * ExtractorIuWFNg猏擾pPreparedStatement𐶐/ԋp
 	 * ́Aextractor.getValues().size() > 0̏ꍇɂ̂݌ĂяoƁB
 	 * ȊȌꍇsubqueryp邽߁A\򉻂̌ɂȂ܂B
 	 * @param extractor
 	 * @return
 	 */
 	public static PreparedStatement extractor2PreparedStatement(Extractor extractor) {
 		try {
 	        Extractor2HQL tmp = new Extractor2HQL(extractor);
 	        Query q = tmp.makeQuery(LockMode.NONE);
 	        SessionImplementor session = (SessionImplementor)HibernateUtils.getSession();
 			SessionFactoryImpl sfi = (SessionFactoryImpl)HibernateUtils.getSession().getSessionFactory();
 			HQLQueryPlan hqp = sfi.getQueryPlanCache().getHQLQueryPlan(q.getQueryString(), false, session.getEnabledFilters());
 			String sql = String.format("select count(*) from (%s) x", hqp.getSqlStrings()[0]);
 			log.debug(sql);
 			PreparedStatement pstmt;
 			pstmt = session.connection().prepareStatement(sql);
 			tmp.setParameter(pstmt);
 	        return pstmt;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 		
 	}
 	
 	/**
      * olMap̃Xg𐶐
      * @param list
      * @return
      * @deprecated
      */
 	@Deprecated
     public static List<Map<String, Object>> makeMap(Extractor extractor, List<Object> list) {
         List<ExtractValue> values = extractor.getValues();
         return makeMap(values, list);
     }
     
     static List<Map<String, Object>> makeMap(List<ExtractValue> values, List<Object> list) {
     	List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
         MakeMapStrategy strategy = (values.size() == 1) ? SingleStrategy.INSTANCE : MultiStrategy.INSTANCE;
         for(Object row : list) {
         	Map<String, Object> rowMap = strategy.getMap(row, values);
             ret.add(rowMap);
         }
         return ret;
     }
     
     public static <T> List<T> makeList(final Class<T> returnType, Extractor extractor, List<Object> list) {
     	List<ExtractValue> values = extractor.getValues();
     	if(extractor.getValues().size() == 0) {
     		values = new ArrayList<ExtractValue>(){
 				private static final long serialVersionUID = 1L;
 			{
 				add(new ExtractValue("entity", new Wrapper(returnType)));
     		}};
     	}
     	List<Map<String, Object>> mapList = makeMap(values, list);
     	if(returnType.isAssignableFrom(Map.class)) {
     		return (List<T>) mapList;
     	}
     	List<T> retList = new ArrayList<T>(mapList.size());
     	if(extractor.target.isAssignableFrom(returnType) && extractor.getValues().size() == 0) {
     		for(Map<String, Object> map : mapList) {
     			retList.add((T)map.values().toArray()[0]);
     		}
     	} else {
 	    	for(Map<String, Object> map : mapList) {
 	   			retList.add(makeInstance(returnType, map));
 	    	}
     	}
     	return retList;
     }
    
     static <T> T makeInstance(Class<T> returnType, Map<String, Object> map) {
     	T ret = tryingConstructorInjection(returnType, map);
     	if(ret == null) {
     		ret = tryingSetterInjection(returnType, map);
     	}
     	BasicServiceInterceptor.addPostLoadObject(ret);
 		return ret;
     }
 
     static <T> T tryingSetterInjection(Class<T> returnType, Map<String, Object> map) {
     	try {
 			T ret = returnType.newInstance();
 			for(Map.Entry<String, Object> entry : map.entrySet()) {
 				PropertyUtils.setProperty(ret, entry.getKey(), entry.getValue());
 			}
 			return ret;
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
     }
     
     static <T> T tryingConstructorInjection(Class<T> returnType, Map<String, Object> map) {
     	Object[] array = map.values().toArray();
     	Constructor<?>[] consts = returnType.getConstructors();
     	for(Constructor<?> con : consts) {
     		try {
 				T ret = (T)con.newInstance(array);
 				return ret;
 			} catch (IllegalArgumentException e) {
 			} catch (InstantiationException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalAccessException e) {
 			} catch (InvocationTargetException e) {
 			}
     	}
     	return null;
     }
 
     private static interface MakeMapStrategy {
     	public Map<String, Object> getMap(Object target, List<ExtractValue> values);
     }
     
     private static class SingleStrategy implements MakeMapStrategy {
     	private final static MakeMapStrategy INSTANCE = new SingleStrategy();
 		public Map<String, Object> getMap(Object target, List<ExtractValue> values) {
 			Map<String, Object> ret = new LinkedHashMap<String, Object>();
 			Object value = target;
 			if((values.get(0).value instanceof SummaryFunction) && value == null) {
 				value = 0L;
 			} else if(value.getClass().isArray()) {
 				value = Array.get(value, 0);
 			}
 			ret.put(values.get(0).key, value);
 			return ret;
 		}
     	
     }
 
     private static class MultiStrategy implements MakeMapStrategy {
     	private final static MakeMapStrategy INSTANCE = new MultiStrategy();
 		public Map<String, Object> getMap(Object target, List<ExtractValue> values) {
 			Object[] row = (Object[])target;
 			Map<String, Object> rowMap = new LinkedHashMap<String, Object>();
 			for(int i = 0 ; i < values.size() ; i++) {
 				Object value = ((values.get(i).value instanceof SummaryFunction) && row[i] == null) ? 0L : row[i]; 
 				rowMap.put(values.get(i).key, value);
 			}
 			return rowMap;
 		}
     	
     }
     
 	private Query makeCountQuery() {
         builder.append("select ");
         if(extractor.getValues().size() == 0) {
         	builder.append("count(distinct ");
         	builder.append(getAlias(extractor.target, extractor.targetAlias));
         	ClassMetadata cm = HibernateUtils.getSession().getSessionFactory().getClassMetadata(extractor.target);
         	builder.append(".");
         	builder.append(cm.getIdentifierPropertyName());
         	builder.append(")");
         } else {
         	builder.append("count(*)");
         }
         makeFromCouse(false);
         makeWhereCouse();
         String hql = builder.toString();
         log.debug(hql);
         Query query = HibernateUtils.getSession().createQuery(hql);
         return query;
 	}
 
     private Query makeQuery(LockMode lockMode) {
         makeSelectCouse(lockMode);
         makeFromCouse(true);
         makeWhereCouse();
         makeGroupByCouse();
         makeHavingCouse();
         makeOrderByCouse();
         String hql = builder.toString();
         log.debug(hql);
         Query query = HibernateUtils.getSession().createQuery(hql);
         return query;
     }
     
 	private void makeHavingCouse() {
 		if(extractor.getHavingIterator().size() == 0) {
 			return;
 		}
         String joinString = "";
         builder.append(" having ");
         for(Condition<? extends Value> con : extractor.getHavingIterator()) {
             builder.append(joinString);
             makeCondition(con);
             joinString = " and ";
         }
 	}
 
 	private void makeGroupByCouse() {
 		if(!usingGroupBy) {
 			return;
 		}
 		String delimitor = " group by ";
 		for(ExtractValue ev : extractor.getValues()) {
 			delimitor = makeGroupByCouse(ev.value, delimitor);
 		}
 	}
 
 	private String makeGroupByCouse(Object value, String delimitor) {
 		if(value instanceof Property) {
 			builder.append(delimitor);
 			builder.append(VALUE_MAKE_STRATEGY_MAP.get(value.getClass()).makeValue(this, (Value)value));
 			delimitor = ", ";
 		} else if(value instanceof FreeFormat) {
 			FreeFormat ff = (FreeFormat)value;
 			for(Object o : ff.values) {
 				delimitor = makeGroupByCouse(o, delimitor);
 			}
 		}
 		return delimitor;
 	}
 	
 	private void makeOrderByCouse() {
         if(extractor.getOrderIterator().size() == 0) {
             return;
         }
         builder.append(" order by ");
         String delimitor = "";
         for(Order<? extends Value> order : extractor.getOrderIterator()) {
             builder.append(delimitor);
             String property = VALUE_MAKE_STRATEGY_MAP.get(order.label.getClass()).makeValue(this, order.label);
             builder.append(property);
             if(order instanceof Desc) {
                 builder.append(" desc");
             }
             delimitor = ",";
         }
     }
 
     private void setParameter(Object query) {
     	List<Condition<? extends Value>> list = new ArrayList<Condition<? extends Value>>(extractor.getConditionIterator());
     	list.addAll(extractor.getHavingIterator());
         for(Condition<? extends Value> con : list) {
        		setParameter(query, con);
         }
     }
 
     private void setParameter(Object query, Condition<? extends Value> con) {
     	ConditionStrategy<Condition> strategy = (ConditionStrategy<Condition>) CONDITION_STRATEGY_MAP2.get(con.getClass()); 
     	strategy.setParameter(query, this, con);
     }
     
     private void makeWhereCouse() {
         if(extractor.getConditionIterator().size() + extractor.getInnerJoins().size() == 0) {
             return;
         }
         String joinString = "";
         builder.append(" where ");
         for(Condition<? extends Value> con : extractor.getConditionIterator()) {
             builder.append(joinString);
             makeCondition(con);
             joinString = " and ";
         }
 /*
         Set<String> set = new HashSet<String>();
         set.add(getAlias(extractor.target, extractor.targetAlias));
         //deletedinDBIɃ`FbN
         for(InnerJoin join : extractor.getInnerJoins()) {
             if(LogicalDeleteEntity.class.isAssignableFrom(join.target)) {
                 String aliase = getAlias(join.target, join.targetAlias);
                 if(!set.contains(aliase)) {
                     set.add(aliase);
                     builder.append(joinString);
                     builder.append(aliase);
                     builder.append(".deletedInDB");
                     builder.append("=");
                     builder.append("'");
                     builder.append(HibernateUtils.BOOLEAN_CHAR_F);
                     builder.append("'");
                     joinString = " and ";
                 }
             }
             if(LogicalDeleteEntity.class.isAssignableFrom(join.joined)) {
                 String aliase = getAlias(join.joined, join.joinedAlias);
                 if(!set.contains(aliase)) {
                     set.add(aliase);
                     builder.append(joinString);
                     builder.append(aliase);
                     builder.append(".deletedInDB");
                     builder.append("=");
                     builder.append("'");
                     builder.append(HibernateUtils.BOOLEAN_CHAR_F);
                     builder.append("'");
                     joinString = " and ";
                 }
             }
         }
 */
         for(InnerJoin join : extractor.getInnerJoins()) {
             builder.append(joinString);
             builder.append(getAlias(join.target, join.targetAlias));
             addProperty(join.targetProperty);
             builder.append("=");
             builder.append(getAlias(join.joined, join.joinedAlias));
             addProperty(join.joinedProperty);
             joinString = " and ";
         }
     }
 
     private void makeCondition(Condition<? extends Value> con) {
         ConditionStrategy<Condition> strategy = (ConditionStrategy<Condition>) CONDITION_STRATEGY_MAP2.get(con.getClass());
         String where = strategy.makeWhereCouse(this, con);
         builder.append(where);
     }
     
     private void addProperty(String targetProperty) {
         if(targetProperty == null || "".equals(targetProperty)) {
             return;
         }
         builder.append(".");
         builder.append(targetProperty);
     }
     
     private void makeSelectCouse(LockMode lockMode) {
         builder.append("select ");
         if(extractor.getValues().size() == 0) {
         	if(lockMode == LockMode.NONE) {
         		builder.append("distinct ");
         	} else if(extractor.isDistinct()) {
         		builder.append("distinct ");
         	}
             builder.append(getAlias(extractor.target, extractor.targetAlias));
             for(Order<? extends Value> order : extractor.getOrderIterator()) {
             	if(isSkipSelect(order)) {
             		continue;
             	}
                 String property = VALUE_MAKE_STRATEGY_MAP.get(order.label.getClass()).makeValue(this, order.label);
            	builder.append(",");
             	builder.append(property);
             }
         } else {
 	    	if(extractor.isDistinct()) {
 	    		builder.append("distinct ");
 	    	}
 	        String delimitor = "";
 	        for(ExtractValue v : extractor.getValues()) {
 	            builder.append(delimitor);
 	            String property = VALUE_MAKE_STRATEGY_MAP.get(v.value.getClass()).makeValue(this, v.value);
 	            builder.append(property);
 	            delimitor = ",";
 	        }
         }
     }
 
     private boolean isSkipSelect(Order<? extends Value> order) {
     	if(!(order.label instanceof Property)) {
     		//vpeBȊOȂ炽ԂExtractValueƂĉĂ̂ŃXLbvėǂ
     		return true;
     	}
     	Property p = (Property)order.label;
     	if(p.target == null) {
     		return true;
     	}
     	return false;
     }
     
     static interface ValueMaker<T extends Value> {
     	public String makeValue(Extractor2HQL generator, T v);
     }
 
     static class SummaryFunctionValueMaker<T extends SummaryFunction> implements ValueMaker<T> {
     	static final String format = "%s(%s)";
     	private final String prefix;
     	SummaryFunctionValueMaker(String prefix) {
     		this.prefix = prefix;
     	}
 		@Override
 		public String makeValue(Extractor2HQL generator, T v) {
 			generator.usingGroupBy = true;
 			return String.format(format, prefix, getValue(generator, v));
 		}
     	
 		protected String getValue(Extractor2HQL generator, T v) {
 			return VALUE_MAKE_STRATEGY_MAP.get(v.value.getClass()).makeValue(generator, v.value);
 		}
     }
     final static Map<Class<? extends Value>, ValueMaker> VALUE_MAKE_STRATEGY_MAP;
     static {
     	Map<Class<? extends Value>, ValueMaker> tmp = new HashMap<Class<? extends Value>, ValueMaker>();
     	tmp.put(Property.class, new ValueMaker<Property>() {
 			@Override
 			public String makeValue(Extractor2HQL generator, Property v) {
 				return generator.getProperty(v.target, v.aliase, v.property);
 			}
     	});
     	tmp.put(Max.class, new SummaryFunctionValueMaker<Max>("max"));
     	tmp.put(Min.class, new SummaryFunctionValueMaker<Min>("min"));
     	tmp.put(Sum.class, new SummaryFunctionValueMaker<Sum>("sum"));
     	tmp.put(Avg.class, new SummaryFunctionValueMaker<Avg>("avg"));
     	tmp.put(Count.class, new SummaryFunctionValueMaker<Count>("count") {
 			@Override
 			public String getValue(Extractor2HQL generator, Count v) {
 				return (v.distinct ? "distinct " : "") + super.getValue(generator, v);
 			}
     	});
     	tmp.put(FreeFormat.class, new ValueMaker<FreeFormat>() {
 			@Override
 			public String makeValue(Extractor2HQL generator, FreeFormat v) {
 				int replaceIndex = 0;
 				int length = v.format.length();
 				StringBuilder ret = new StringBuilder();
 				for(int i = 0 ; i < length ; i++) {
 					char ch = v.format.charAt(i);
 					if(ch == '?') {
 						ret.append(replacetext(generator, v.values.get(replaceIndex++)));
 					} else {
 						ret.append(ch);
 					}
 				}
 				return ret.toString();
 			}
 
 			String replacetext(Extractor2HQL generator, Object val) {
 				if(val instanceof Value) {
 					return VALUE_MAKE_STRATEGY_MAP.get(val.getClass()).makeValue(generator, (Value)val);
 				} else if(val instanceof Number) {
 					return val.toString();
 				} else {
 					return "'" + val.toString() + "'";
 				}
 			}
     	});
     	VALUE_MAKE_STRATEGY_MAP = Collections.unmodifiableMap(tmp);
     }
     
     
 //    private Set<Class> joinedEntity;
     private void makeFromCouse(boolean isFetch) {
         Set<String> aliases = new HashSet<String>();
         builder.append(" from ");
         builder.append(getAlias(extractor.target, null));
         builder.append(" as ");
         builder.append(getAlias(extractor.target, extractor.targetAlias));
         if(extractor.getValues().size() == 0) {
             //QƃIuWFNg̃WC
 //            joinedEntity = new HashSet<Class>();
             makeOuterJoin(extractor.target, getAlias(extractor.target, extractor.targetAlias), isFetch);
         }
         aliases.add(getAlias(extractor.target, extractor.targetAlias));
 /*
         for(InnerJoin join : extractor.getInnerJoins()) {
             if(!joinedEntity.contains(join.target)) {
                 joinedEntity.set(join.target);
             }
         }
 */
         for(InnerJoin join : extractor.getInnerJoins()) {
             String aliase = getAlias(join.target, join.targetAlias);
             if(!aliases.contains(aliase)) {
                 aliases.add(aliase);
                 builder.append(", ");
                 builder.append(getAlias(join.target, null));
                 builder.append(" as ");
                 builder.append(aliase);
             }
             aliase = getAlias(join.joined, join.joinedAlias);
             if(!aliases.contains(aliase)) {
                 aliases.add(aliase);
                 builder.append(", ");
                 builder.append(getAlias(join.joined, null));
                 builder.append(" as ");
                 builder.append(aliase);
             }
         }
     }
     
     private void makeOuterJoin(Class target, String currentAliase, boolean isFetch) {
 //        joinedEntity.add(target);
         Map<String, Class> map = getEntityRelationProperty(target);
         for(Map.Entry<String, Class> entry : map.entrySet()) {
             builder.append(" left outer join ");
             if(isFetch) {
             	builder.append("fetch ");
             }
             String property = currentAliase + "." + entry.getKey();
             builder.append(property);
             Class cl = entry.getValue();
             if(!cl.equals(target)) {    //ċA
                 makeOuterJoin(entry.getValue(), property, isFetch);
             }
         }
     }
 
     private Map<String, Class> getEntityRelationProperty(Class target) {
         ClassMetadata cm = HibernateUtils.getSession().getSessionFactory().getClassMetadata(target);
         Map<String, Class> ret = new HashMap<String, Class>();
         String[] names = cm.getPropertyNames();
         for(String name : names) {
             Type t = cm.getPropertyType(name);
             if(t.isEntityType()) {
                 try {
                     ret.put(name, Class.forName(t.getName()));
                 } catch (ClassNotFoundException e) {
                     throw new RuntimeException(e);
                 }
             }
         }
         return ret;
     }
     
     private String getProperty(Class cl, String aliase, String property) {
     	if(cl == null) {
     		cl = this.extractor.target;
     	}
         aliase = getAlias(cl, aliase);
         if(property == null || "".equals(property)) {
             return aliase;
         } else if(property.equals("*")){
         	return "*";
         } else {
             return aliase + "." + property;
         }
     }
     
     private static String getAlias(Class cl, String alias) {
         if(alias == null || "".equals(alias)) {
             String name = cl.getName();
             return name.substring(name.lastIndexOf(".") + 1);
         } else {
             return alias;
         }
     }
     
     private static interface ConditionStrategy<T extends Condition> {
     	public String makeWhereCouse(Extractor2HQL generator, T condition);
     	public void setParameter(Object query, Extractor2HQL generator, T condition);
     }
     
     private static class CombineConditionStrategy<T extends CombineCondition> implements ConditionStrategy<T> {
     	private final String combinString;
     	CombineConditionStrategy(String combinString) {
     		this.combinString = " " + combinString + " ";
     	}
 
 		@Override
 		public String makeWhereCouse(Extractor2HQL generator, T condition) {
 			StringBuilder sb = new StringBuilder();
             if(condition.getConditions().size() == 0) {
                 return "";
             }
             sb.append("(");
             String combineString = "";
             for(Object o : condition.getConditions()) {
             	Condition con = (Condition)o;
                 sb.append(combineString);
             	ConditionStrategy<Condition> strategy = (ConditionStrategy<Condition>) CONDITION_STRATEGY_MAP2.get(con.getClass());
             	sb.append(strategy.makeWhereCouse(generator, con));
                 combineString = this.combinString;
             }
             sb.append(")");
             return sb.toString();
 		}
 
 		@Override
 		public void setParameter(Object query, Extractor2HQL generator, T condition) {
             for(Object o : condition.getConditions()) {
             	Condition con = (Condition)o;
             	ConditionStrategy<Condition> strategy = (ConditionStrategy<Condition>) CONDITION_STRATEGY_MAP2.get(con.getClass());
             	strategy.setParameter(query, generator, con);
             }
 		}
     }
     
     private static class LabelHoldingStrategy<T extends LabelHoldingCondition> implements ConditionStrategy<T> {
     	public String makeWhereCouse(Extractor2HQL generator, T condition) {
     		return String.format("%s %s %s", 
     				getLeftSide(generator, condition), 
     				getExpression(generator, condition), 
     				getRightSide(generator, condition));
     	}
     	
     	protected String getLeftSide(Extractor2HQL generator, T condition) {
     		return VALUE_MAKE_STRATEGY_MAP.get(condition.label.getClass()).makeValue(generator, condition.label);
     	}
     	
     	protected String getExpression(Extractor2HQL generator, T condition) {
     		return CONDITION_EXPRESSION_MAP.get(condition.getClass());
     	}
     	
     	protected String getRightSide(Extractor2HQL generator, T condition) {
     		return "";
     	}
 
 		@Override
 		public void setParameter(Object query, Extractor2HQL generator, T condition) {
 		}
     }
 
     private static class ValueHoldingStrategy<T extends ValueHoldingCondition> extends LabelHoldingStrategy<T> {
     	@Override
     	protected String getRightSide(Extractor2HQL generator, T condition) {
     		if(condition.value instanceof Value) {
     			return VALUE_MAKE_STRATEGY_MAP.get(condition.value.getClass()).makeValue(generator, (Value)condition.value);
     		} else {
     			return "?";
     		}
     	}
 		@Override
 		public void setParameter(Object query, Extractor2HQL generator, T condition) {
 			if(!(condition.value instanceof Value)) {
 				setParameter2(query, generator, condition.value);
 			}
 		}
 		
 	    protected void setParameter2(Object o, Extractor2HQL generator, Object value) {
 	    	if(o instanceof Query) {
 	    		Query query = (Query)o;
 		        if(value instanceof Number) {
 		            query.setParameter(generator.patemeterIndex, value, TypeFactory.basic(value.getClass().getName()));
 		        } else {
 		            query.setParameter(generator.patemeterIndex, value);
 		        }
 	    	} else {
 	    		try {
 		    		PreparedStatement pstmt = (PreparedStatement)o;
 	    			SessionFactory sf = HibernateUtils.getSession().getSessionFactory();
 	    	        ClassMetadata cm = sf.getClassMetadata(value.getClass());
 	    	        if(cm != null) {
 	    	        	//TODO L[ƂǂȂH
 	    	        	value = cm.getIdentifier(value, EntityMode.POJO);
 	    	        } else if ((value instanceof Date) && !value.getClass().getName().startsWith("java.sql.")) {
 	    	        	value = new Timestamp(((Date)value).getTime());
 	    	        }
     	        	pstmt.setObject(generator.patemeterIndex + 1, value);
 	    		} catch(Exception e) {
 	    			throw new RuntimeException(e);
 	    		}
 	    	}
 	        generator.patemeterIndex++;
 	    }
     }
     
     private static class CollectionValueHoldingStrategy<T extends ValueHoldingCondition> extends ValueHoldingStrategy<T> {
     	@Override
     	protected String getRightSide(Extractor2HQL generator, T condition) {
         	StringBuilder sb = new StringBuilder();
         	sb.append("(");
         	String delimiter = "";
         	for(@SuppressWarnings("unused") Object o : (Collection)condition.value) {
         		sb.append(delimiter);
         		sb.append("?");
         		delimiter = ", ";
         	}
         	sb.append(")");
         	return sb.toString();
     	}
 		@Override
 		public void setParameter(Object query, Extractor2HQL generator, T condition) {
 			if(!(condition.value instanceof Value)) {
             	Collection col = (Collection)condition.value;
             	for(Object o : col) {
             		setParameter2(query, generator, o);
             	}
 			}
 		}
     }
 
     private final static Map<Class<? extends Condition>, ConditionStrategy<? extends Condition>> CONDITION_STRATEGY_MAP2;
     static {
     	Map<Class<? extends Condition>, ConditionStrategy<? extends Condition>> tmp = 
     		new HashMap<Class<? extends Condition>, ConditionStrategy<? extends Condition>>();
     	tmp.put(And.class, new CombineConditionStrategy<And>("and"));
     	tmp.put(Or.class, new CombineConditionStrategy<Or>("or"));
     	tmp.put(Join.class, new CombineConditionStrategy<Join>(""){
 			@Override
 			public String makeWhereCouse(Extractor2HQL generator, Join condition) {
 	               throw new RuntimeException("HQLł̓T|[gĂ܂BîƂj");
 			}
     	});
     	tmp.put(Eq.class, new ValueHoldingStrategy<Eq>());
     	tmp.put(Ge.class, new ValueHoldingStrategy<Ge>());
     	tmp.put(Gt.class, new ValueHoldingStrategy<Gt>());
     	tmp.put(In.class, new CollectionValueHoldingStrategy<In>());
     	tmp.put(IsNotNull.class, new LabelHoldingStrategy<IsNotNull>());
     	tmp.put(IsNull.class, new LabelHoldingStrategy<IsNull>());
     	tmp.put(Le.class, new ValueHoldingStrategy<Le>());
     	tmp.put(Like.class, new ValueHoldingStrategy<Like>());
     	tmp.put(Lt.class, new ValueHoldingStrategy<Lt>());
     	tmp.put(NotEq.class, new ValueHoldingStrategy<NotEq>());
     	tmp.put(NotIn.class, new CollectionValueHoldingStrategy<NotIn>());
     	tmp.put(RegularExp.class, new ValueHoldingStrategy<RegularExp>(){
     		@Override
         	protected String getLeftSide(Extractor2HQL generator, RegularExp condition) {
 	        	Dialect dialect = HibernateUtils.getDialect();
 	        	if(dialect == null) {
 	        		throw new RuntimeException("dialect can't get.");
 	        	}
 	        	String tmpl = REGEXP_TEMPLATE.get(dialect.getClass());
 	        	log.debug(tmpl);
 	        	if(tmpl == null) {
 	        		throw new RuntimeException("unsupported regular Expression in " + dialect.getClass().getName() + ".");
 	        	}
 	        	String property = super.getLeftSide(generator, condition);
 	        	String value = super.getRightSide(generator, condition);
 	        	return MessageFormat.format(tmpl, property, value);
     		}
     		@Override
         	protected String getRightSide(Extractor2HQL generator, RegularExp condition) {
     			return "0";
     		}
     	});
     	CONDITION_STRATEGY_MAP2 = Collections.unmodifiableMap(tmp);
     }
     
     private final static Map<Class<? extends Condition> , String> CONDITION_EXPRESSION_MAP;
     
     static {
         Map<Class<? extends Condition>, String> tmp = new HashMap<Class<? extends Condition>, String>();
         tmp.put(Eq.class, 			"=");
         tmp.put(Ge.class, 			">=");
         tmp.put(Gt.class, 			">");
         tmp.put(In.class, 			"in");
         tmp.put(IsNotNull.class, 	"is not null");
         tmp.put(IsNull.class, 		"is null");
         tmp.put(Le.class, 			"<=");
         tmp.put(Like.class, 		"like");
         tmp.put(Lt.class, 			"<");
         tmp.put(NotEq.class, 		"<>");
         tmp.put(NotIn.class, 		"not in");
         tmp.put(RegularExp.class, 	">");
         CONDITION_EXPRESSION_MAP = Collections.unmodifiableMap(tmp);
     }
 
     private final static Map<Class<? extends Dialect>, String> REGEXP_TEMPLATE;
     static {
         Map<Class<? extends Dialect>, String> tmp = new HashMap<Class<? extends Dialect>, String>();
         tmp.put(PostgreSQLDialect.class, "char_length(substring({0},{1}))");
         tmp.put(Oracle10gDialect.class, "REGEXP_INSTR({0}, {1})");
         REGEXP_TEMPLATE = Collections.unmodifiableMap(tmp);
     }
 }
