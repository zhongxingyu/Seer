 package fava.functionable;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import fava.Functions;
 import fava.signatures.FnFold;
 import fava.signatures.FnEach;
 import fava.signatures.FnMap;
 
 /**
  * This is a base abstract class which provides a default implementation of some of the most common functional commands. 
  * Objects extending this class gain access to these methods, but will be required to implement the Iterable interface
  * @author Nathaniel Sherry, 2010-2011
  *
  * @param <T1>
  */
 
 public abstract class Functionable<T1> implements Iterable<T1> {
 
 	
 	protected <T> Collection<T> getNewCollection()
 	{ 
 		return new ArrayList<T>();
 	}
 	
 	
 	protected <T> Functionable<T> wrapNewCollection(Collection<T> col)
 	{
 		if (! (col instanceof List)) throw new ClassCastException();
 		return FList.wrap((ArrayList<T>)col);
 	}
 	 
 	
 	
 
 	
 	
 	/**
 	 * Applies the given {@link FnEach} function to each contained element. 
 	 * @param f
 	 */
 	public void each(FnEach<T1> f)
 	{
 		each(this, f);
 	}
 	
 	
 	/**
 	 * Applies the given {@link FnMap} function to each contained element. Returns a Functionable object representing 
 	 * the results of those applications. 
 	 * @param <T2>
 	 * @param f the mapping function to apply
 	 * @return a Functionable object containing the results of applying the {@link FnMap} to the elements in this Functionable object
 	 */
 	public <T2> Functionable<T2> map(FnMap<T1, T2> f)
 	{
 		
 		Collection<T2> target = getNewCollection();		
 		map(this, f, target);
 		return wrapNewCollection(target);
 		
 	}
 	
 
 	
 	/**
 	 * Applies the given {@link FnMap} function to each contained element. Returns a Functionable object containing the 
 	 * elements of this object for which the given function returned true.
 	 * @param f the condition function to apply
 	 * @return a Functionable object containing only those elements in this Functionable object for which the given 
 	 * function returned true
 	 */
 	public Functionable<T1> filter(FnMap<T1, Boolean> f)
 	{
 		Collection<T1> target = getNewCollection();		
 		filter(this, f, target);
 		return wrapNewCollection(target);
 	}
 	
 	/**
 	 * Applies the given {@link FnFold} function to consecutive contained elements, also threading a running sum or 
 	 * result from call to call. When the function has been applied to every element, the result will be a single value. 
 	 * @param f the folding function to apply
 	 * @return the result of applying this function to all elements as if it were an n-ary function 
 	 */
 	public T1 fold(FnFold<T1, T1> f)
 	{
 		return fold(this, f);
 	}
 	
 	/**
 	 * Applies the given {@link FnFold} function to consecutive contained elements, also threading a running sum or 
 	 * result from call to call. When the function has been applied to every element, the result will be a single value. 
 	 * Since the return value may be of a different type than the contained elements, a starting value of that type is requried 
 	 * @param f the folding function to apply
 	 * @return the result of applying this function to all elements as if it were an n-ary function 
 	 */
 	public <T2> T2 fold(T2 base, FnFold<T1, T2> f)
 	{
 		return fold(this, base, f);
 	}
 	
 	
 	
 	/**
 	 * Converts the contents of this object to a String representation, using the given separator. 
 	 * @param separator the separator shown between elements 
 	 * @return a String representation of the contained elements
 	 */
 	public String show(String separator)
 	{
 		final StringBuilder sb = new StringBuilder();
 		
 		
 		
 		this.map(Functions.<T1>show()).each(new FnEach<String>() {
 
 			public void f(String element) {
 				sb.append(element);
 				sb.append(",");
 			}
 		});
 		
 		
 		return "[" + sb.substring(0, Math.max(sb.length()-1, 0)) + "]";
 	}
 	
 	/**
 	 * Converts the contents of this object to a String representation. 
 	 * @return a String representation of the contained elements
 	 */
 	public String show()
 	{
 		return show(",");
 	}
 	
 	
 	@Override
 	public String toString()
 	{
 		return "Fava: " + this.getClass().getName();
 	}
 	
 	/**
 	 * Write the data in this Functionable<T> source data structure out to an FList<T> sink
 	 * @return an FList<T> containing all values in this data structure
 	 */
 	public FList<T1> toSink()
 	{
 		return new FList<T1>(this);
 	}
 	
 	
 	
 	
 
 	protected static <S1> void each(Iterable<S1> source, FnEach<S1> f)
 	{
 		for (S1 s : source)
 		{
 			f.f(s);
 		}
 
 	}
 	
 	protected static <S1, S2> Collection<S2> map(Iterable<S1> source, FnMap<S1, S2> f, Collection<S2> target)
 	{
 		for (S1 s : source)
 		{
 			target.add(f.f(s));
 		}
 		
 		return target;
 	}
 	
 
 	
 	protected static <S1> Collection<S1> filter(Iterable<S1> source, FnMap<S1, Boolean> f, Collection<S1> target)
 	{
 		for (S1 s : source)
 		{
 			if (f.f(s)) target.add(s);
 		}
 		
 		return target;
 	}
 	
 
 	protected static <S1> S1 fold(Iterable<S1> source, FnFold<S1, S1> f)
 
 	{
 		S1 acc = null;
 		boolean first = true;
 		
 		for (S1 s : source)
 		{
			if (first) { acc = s; first = false; }
 			else { acc = f.f(s, acc); }
 		}
 		
 		return acc;
 	}
 	
 
 	
 	protected static <S1, S2> S2 fold(Iterable<S1> source, S2 base, FnFold<S1, S2> f)
 	{
 		S2 acc = base;
 		
 		for (S1 s : source)
 		{
 			acc = f.f(s, acc);
 		}
 		
 		return acc;
 	}
 	
 	
 	
 	
 	
 	
 	
 }
