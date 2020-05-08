 package ecologylab.generic;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Set;
 
 import ecologylab.pools.HashMapPool;
 
 public class FeatureVector<T> extends Observable implements IFeatureVector<T>
 {
 
 	private static final int	UNCALCULATED	= -1;
 
 	protected HashMap<T,Double>		values;
 
 	private double					norm	= UNCALCULATED, max	= UNCALCULATED;
 
 	private static final Map ZERO_LENGTH_HASHMAP = new HashMap(0);
 	
 	public FeatureVector ()
 	{
 	}
 	
 	private void initValues()
 	{
 		values = new HashMap<T,Double>();
 	}
 
 	public FeatureVector ( int size )
 	{
 		values = (HashMap<T,Double>) new HashMap<T,Double>(size);
 	}
 
 	public FeatureVector ( IFeatureVector<T> copyMe )
 	{
 		Map<T, Double> otherMap = copyMe.map();
		if (otherMap.size() > 0)
 			values = new HashMap<T,Double>(otherMap);
 		norm = copyMe.norm();
 	}
 	
 	protected void reset() 
 	{
 		if (values != null)
 			values.clear();
 		resetNorm();
 	}
 
 	public FeatureVector<T> copy ( )
 	{
 		return new FeatureVector<T>(this);
 	}
 
 	public double get ( T term )
 	{
 		if (values == null)
 			return 0;
 		
 		Double d = values.get(term);
 		if (d == null)
 			return 0;
 		return d;
 	}
 
 	public void add ( T term, Double val )
 	{
 		if (values == null)
 			initValues();
 		
 		synchronized (values)
 		{
 			if (values.containsKey(term))
 				val += values.get(term);
 			values.put(term, val);
 			resetNorm();
 		}
 	}
 
 	public void set ( T term, Double val )
 	{
 		if (values == null)
 			initValues();
 		
 		synchronized (values)
 		{
 			values.put(term, val);
 			resetNorm();
 		}
 	}
 
 	/**
 	 * Pairwise multiplies this Vector by another Vector, in-place.
 	 * 
 	 * @param v
 	 *            Vector by which to multiply
 	 */
 	public void multiply ( IFeatureVector<T> v )
 	{
 		Map<T, Double> other = v.map();
 		if (other == null || values == null || other.size() == 0)
 			return;
 		synchronized (values)
 		{
 			this.values.keySet().retainAll(other.keySet());
 			for (T term : this.values.keySet())
 				this.values.put(term, other.get(term) * this.values.get(term));
 		}
 		resetNorm();
 	}
 
 	/**
 	 * Scalar multiplication of this vector by some constant
 	 * 
 	 * @param c
 	 *            Constant to multiply this vector by.
 	 */
 	public void multiply ( double c )
 	{
 		if (values == null)
 			return;
 		
 		synchronized (values)
 		{
 			ArrayList<T> terms_to_delete = new ArrayList<T>();
 			for (T term : this.values.keySet())
 			{
 				double new_val = c * this.values.get(term);
 				if (Math.abs(new_val) < 0.001)
 					terms_to_delete.add(term);
 				else
 					this.values.put(term, new_val);
 			}
 			for (T t : terms_to_delete)
 				values.remove(t);
 		}
 		resetNorm();
 	}
 
 	/**
 	 * Pairwise addition of this vector by some other vector times some constant.<br>
 	 * i.e. this + (c*v)<br>
 	 * Vector v is not modified.
 	 * 
 	 * @param c
 	 *            Constant which Vector v is multiplied by.
 	 * @param v
 	 *            Vector to add to this one
 	 */
 	public void add ( double c, IFeatureVector<T> v )
 	{
 		Map<T, Double> other = v.map();
 		
 		if (other == null || other.size() == 0)
 			return;
 		
 		if (values == null)
 			initValues();
 		
 		synchronized (other)
 		{
 			synchronized (values)
 			{
 				for (T term : other.keySet())
 					if (this.values.containsKey(term))
 						this.values.put(term, new Double(c * other.get(term) + this.values.get(term)));
 					else
 						this.values.put(term, new Double(c * other.get(term)));
 			}
 		}
 		resetNorm();
 	}
 
 	/**
 	 * Adds another Vector to this Vector, in-place.
 	 * 
 	 * @param v
 	 *            Vector to add to this
 	 */
 	public void add ( IFeatureVector<T> v )
 	{
 		add(1, v);
 	}
 
 	public double dot ( IFeatureVector<T> v )
 	{
 		return dot(v, false);
 	}
 
 	public double dot ( IFeatureVector<T> v, boolean simplex )
 	{
 		Map<T, Double> other = v.map();
 		if (other == null || other.size() == 0 || values == null || v.norm() == 0 || this.norm() == 0)
 			return 0;
 
 		double dot = 0;
 		Map<T, Double> vector = this.values;
 		synchronized (values)
 		{
 			for (T term : vector.keySet())
 				if (other.containsKey(term))
 					if (simplex)
 						dot += vector.get(term);
 					else
 						dot += vector.get(term) + other.get(term);
 		}
 		return dot;
 	}
 
 	public Set<T> elements ( )
 	{
 		if (values == null)
 			return new HashSet<T>(0);
 		return new HashSet<T>(values.keySet());
 	}
 
 	public Set<Double> values ( )
 	{
 		if (values == null)
 			return new HashSet<Double>(0);
 		return new HashSet<Double>(values.values());
 	}
 	/**
 	 * Access to the inner-level HashMap backing this FeatureVector. 
 	 * Do NOT modify this collection outside of FeatureVector.java or TermVector.java, 
 	 * you will probably cause things to break.
 	 * 
 	 * @return HashMap backing this FeatureVector
 	 */
 	public Map<T, Double> map ( )
 	{
 		if (values == null)
 			return ZERO_LENGTH_HASHMAP;
 		return values;
 	}
 
 	public int size ( )
 	{
 		if (values == null)
 			return 0;
 		return values.size();
 	}
 
 	private void recalculateNorm ( )
 	{
 		if (values == null)
 			return;
 		double norm = 0;
 		synchronized(values)
 		{
 			for (double d : this.values.values())
 			{
 				norm += Math.pow(d, 2);
 			}
 		}
 			this.norm = Math.sqrt(norm);
 	}
 
 	private void resetNorm ( )
 	{
 		norm = UNCALCULATED;
 		max = UNCALCULATED;
 	}
 
 	public double norm ( )
 	{
 		if (norm == UNCALCULATED)
 			recalculateNorm();
 		return norm;
 	}
 
 	public double max ( )
 	{
 		if (max == UNCALCULATED)
 			recalculateMax();
 		return max;
 	}
 
 	private void recalculateMax ( )
 	{
 		if (values == null)
 			return;
 		
 		synchronized(values)
 		{
 			for (Double d : values.values())
 				if (Math.abs(d) > max)
 					max = d;
 		}
 	}
 
 	/**
 	 * Linearly scales the vector such that the max value in the vector is no more than the passed
 	 * in value.<br/>
 	 * <br/>
 	 * Pivots around zero so that negative values with a magnitude greater than the clamp will be
 	 * scaled appropriately.
 	 * 
 	 * @param clampTo
 	 *            The new max value of the vector.
 	 */
 	public void clamp ( double clampTo )
 	{
 		if (values == null)
 			return;
 		
 		clampTo = Math.abs(clampTo);
 		double max = this.max();
 		if (!(max > clampTo))
 			return;
 
 		double clampRatio = clampTo / max;
 		synchronized (values)
 		{
 			Set<T> keySet = this.values.keySet();
 			for (T term : keySet)
 			{
 				double old_value = this.values.get(term);
 				double new_value = clampRatio * old_value;
 				this.values.put(term, new_value);
 			}
 		}
 		resetNorm();
 	}
 
 	public void clampExp ( double clampTo )
 	{
 		if (values == null)
 			return;
 		
 		clampTo = Math.abs(clampTo);
 		double max = 0;
 		synchronized (values)
 		{
 			for (Double d : values.values())
 			{
 				double d2 = Math.abs(d);
 				if (d2 > max)
 					max = d2;
 			}
 			if (!(max > clampTo))
 				return;
 			
 			ArrayList<T> terms_to_delete = new ArrayList<T>();
 			for (T term : this.values.keySet())
 			{
 				double old_value = this.values.get(term);
 				double new_value = clampTo * old_value / max;
 				if (Math.abs(new_value) < 0.001)
 					terms_to_delete.add(term);
 				else
 					this.values.put(term, new_value);
 			}
 			for (T t : terms_to_delete)
 				values.remove(t);
 		}
 		resetNorm();
 	}
 
 	public IFeatureVector<T> unit ( )
 	{
 		FeatureVector<T> v = new FeatureVector<T>(this);
 		v.clamp(1);
 		return v;
 	}
 
 	public double dotSimplex ( IFeatureVector<T> v )
 	{
 		return dot(v, true);
 	}
 
 	public int commonDimensions ( IFeatureVector<T> v )
 	{
 		Set<T> v_elements = v.elements();
 		v_elements.retainAll(this.elements());
 		return v_elements.size();
 	}
 
 	public FeatureVector<T> simplex ( )
 	{
 		FeatureVector<T> v = new FeatureVector<T>(this);
 		for (T t : v.values.keySet())
 		{
 			v.values.put(t, 1.0);
 		}
 		return v;
 	}
 	
 	public void clear()
 	{
 		if (values == null)
 			return;
 		
 		synchronized(values)
 		{
 			values.clear();
 		}
 	}
 	
 	public void recycle()
 	{
 		if (values != null)
 		{
 			values.clear();
 			values	= null;//TODO could drop this line to be more moderate
 		}
 	}
 
 	@Override
 	public boolean isRecycled() 
 	{
 		return values == null;
 	}
 	
 }
