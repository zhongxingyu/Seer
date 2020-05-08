 package LJava;
 
 import java.util.Collection;
 import java.util.Map;
 
 public final class Utils {
 	
 	static public final boolean CUT=true;
 	static public final LogicOperator OR=LogicOperator.OR;
 	static public final LogicOperator AND=LogicOperator.AND;
 	static public final LogicOperator DIFFER=LogicOperator.DIFFER;
 	static public final LogicOperator WHERE=LogicOperator.WHERE;
 	static public enum  LogicOperator{
 		OR, AND, DIFFER , NONE, WHERE  }
 	
 
 	public static final boolean variable(Object x) {
 		return (x instanceof Variable);
 	}
 	
 	
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	protected static <K,V> void addTo(Map map, K key, V val, Class<?> type) {
 		Collection collection=(Collection) map.get(key);
 		if (collection==null) {
 			try{
 				collection=(Collection) type.newInstance();
 				map.put(key, collection);
 			}catch (Exception e){}
 		}
 		collection.add(val);
 	}	
 	
 	
 	@SuppressWarnings("unchecked")
 	protected static <K,V> V getFrom(Map<K,V> map, K key, Class<?> type) {
 		V val = map.get(key);
 		try {
 			if (val==null) return (V) type.newInstance();
 		}catch (Exception e) {}
 		return val;
 	}		
 	
 
 //Predefined Functors
 	public static Formula<Number,Double> max=new Formula<Number,Double>("Max", Number.class) {
 		@Override
 		protected Double f(Number... p) {
 			Double result=Double.MIN_VALUE;
 			for (Number n : p) 
 				if (n.doubleValue()>result) result=n.doubleValue();
 			return result;
 		}};
 	
 	public static Formula<Number,Double> min=new Formula<Number,Double>("Min", Number.class){
 		@Override
 		protected Double f(Number... p) {
 			if (p.length==0) return Double.MIN_VALUE;
 			Double result=p[0].doubleValue();
 			for (Number n : p)
 				if (n.doubleValue()<result) result=n.doubleValue();
 			return result;
 		}};
 
 	public static Formula<Object,Integer> cmp=new Formula<Object,Integer>("Compare", Object.class) {
 		@SuppressWarnings({ "rawtypes", "unchecked" })
 		@Override
 		protected Integer f(Object... p) {
 			if (p.length!=2) return Integer.MIN_VALUE;
 			if ((p[0] instanceof Number) && (p[1] instanceof Number)) {
 				double a=((Number)p[0]).doubleValue();
 				double b=((Number)p[1]).doubleValue();
 				if (a>b) return 1;
 				if (b>a) return -1;
 				return 0;
 			}
 			else {
				if (p[0].getClass().equals(p[1].getClass()) && (p[0] instanceof Comparable)) {
					int result = ((Comparable) p[0]).compareTo(p[1]);
					if (result<0) return -1;
					if (result>0) return 1;
					return 0;
				}
 				return Integer.MIN_VALUE;
 			}
 		}};
 
 	public static Formula<Object, Boolean> LJTrue=new Formula<Object,Boolean>("$LJava_True$", Object.class) {
 		@Override
 		protected Boolean f(Object... p) {
 			return true;
 		}};
 
 	public static Formula<Object, Boolean> LJFalse=new Formula<Object,Boolean>("$LJava_False$", Object.class) {
 		@Override
 		protected Boolean f(Object... p) {
 			return false;
 		}};
 //End of predefined functors
 }
