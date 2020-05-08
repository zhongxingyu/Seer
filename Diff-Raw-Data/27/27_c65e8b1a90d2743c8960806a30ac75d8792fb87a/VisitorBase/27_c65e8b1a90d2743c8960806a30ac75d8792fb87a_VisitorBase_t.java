 /**
  * Copyright 2011 The Open Source Research Group,
  *                University of Erlangen-Nürnberg
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.fau.cs.osr.ptk.common;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.concurrent.ConcurrentHashMap;
 
 public abstract class VisitorBase<T>
 {
 	private static final int LOWER_CAPACITY = 256;
 	
 	private static final int UPPER_CAPACITY = 384;
 	
 	private static final float LOAD_FACTOR = .6f;
 	
 	private static final ConcurrentHashMap<Target, Target> cache =
			new ConcurrentHashMap<Target, Target>(LOWER_CAPACITY, LOAD_FACTOR);
 	
 	// =========================================================================
 	
 	protected abstract Object dispatch(T node);
 	
 	/**
 	 * Called before the visitation starts.
 	 * 
 	 * @param node
 	 *            The node at which the visitation will start.
 	 * @return Always returns <code>true</code>. If an overridden version of
 	 *         this method returns <code>false</code> the visitation will be
 	 *         aborted.
 	 */
 	protected boolean before(T node)
 	{
 		return true;
 	}
 	
 	/**
 	 * Called after the visitation has finished. This method will not be called
 	 * if before() returned false.
 	 * 
 	 * @param node
 	 *            The node at which the visitation started.
 	 * @param result
 	 *            The result of the visitation. If the visit() method for the
 	 *            given node doesn't return a value, <code>null</code> is
 	 *            returned.
 	 * @return Returns the result parameter.
 	 */
 	protected Object after(T node, Object result)
 	{
 		return result;
 	}
 	
 	/**
 	 * This method is called if no suitable visit() method could be found. If
 	 * not overridden, this method will throw an UnvisitableException.
 	 * 
 	 * @param node
 	 *            The node that should have been visited.
 	 * @return The result of the visitation.
 	 */
 	protected Object visitNotFound(T node)
 	{
 		throw new VisitNotFoundException(this, node);
 	}
 	
 	// =========================================================================
 	
 	/**
 	 * Start visitation at the given node.
 	 * 
 	 * @param node
 	 *            The node at which the visitation will start.
 	 * @return The result of the visitation. If the visit() method for the given
 	 *         node doesn't return a value, <code>null</code> is returned.
 	 */
 	public final Object go(T node)
 	{
 		if (!before(node))
 			return null;
 		
 		Object result = dispatch(node);
 		return after(node, result);
 	}
 	
 	// =========================================================================
 	
 	protected final Object resolveAndVisit(T node)
 	{
 		Class<?> vClass = this.getClass();
 		Class<?> nClass = node.getClass();
 		
 		Target key = new Target(vClass, nClass);
 		Target cached = cache.get(key);
 		try
 		{
			if (cached == null)
 				cached = findVisit(key);
 			
 			if (cached.getMethod() == null)
 				return visitNotFound(node);
 			
 			return cached.invoke(this, node);
 		}
 		catch (InvocationTargetException e)
 		{
 			Throwable cause = e.getCause();
 			if (cause instanceof VisitingException)
 				throw (VisitingException) cause;
 			throw new VisitingException(node, cause);
 		}
 		catch (VisitingException e)
 		{
 			throw e;
 		}
 		catch (VisitNotFoundException e)
 		{
 			throw e;
 		}
 		catch (Exception e)
 		{
 			throw new VisitorException(node, e);
 		}
 	}
 	
 	private final Target findVisit(Target key) throws SecurityException
 	{
 		Method method = null;
 		
 		Class<?> vClass = key.getVClass();
 		Class<?> nClass = key.getNClass();
 		do
 		{
 			try
 			{
 				method = vClass.getMethod("visit", nClass);
 				break;
 			}
 			catch (NoSuchMethodException e)
 			{
 				// Try the interfaces
 				method = tryInterfaces(nClass.getInterfaces(), vClass);
 				if (method != null)
 					break;
 				
 				// Try to find visit() method for a superclass of node class
 				nClass = nClass.getSuperclass();
 			}
 		} while (nClass != null);
 		
 		Target target = new Target(key, method);
 		Target cached = cache.putIfAbsent(target, target);
 		if (cached != null)
 		{
 			return cached;
 		}
 		else
 		{
 			// Make sure the target is not swept from the cache ...
 			target.touch();
 			if (cache.size() > UPPER_CAPACITY)
 				sweepCache();
 			
 			return target;
 		}
 	}
 	
 	private Method tryInterfaces(Class<?>[] interfaces, Class<?> vClass)
 	{
 		Method method = null;
 		for (Class<?> nClass : interfaces)
 		{
 			try
 			{
 				method = vClass.getMethod("visit", nClass);
 				break;
 			}
 			catch (NoSuchMethodException e)
 			{
 				// Try the interfaces
 				method = tryInterfaces(nClass.getInterfaces(), nClass);
 				if (method != null)
 					break;
 			}
 		}
 		return method;
 	}
 	
 	private final synchronized void sweepCache()
 	{
 		if (cache.size() <= UPPER_CAPACITY)
 			return;
 		
 		Target keys[] = new Target[cache.size()];
 		
 		Enumeration<Target> keysEnum = cache.keys();
 		
 		int i = 0;
 		while (i < keys.length && keysEnum.hasMoreElements())
 			keys[i++] = keysEnum.nextElement();
 		
 		int length = i;
 		Arrays.sort(keys, 0, length);
 		
 		int to = length - LOWER_CAPACITY;
 		for (int j = 0; j < to; ++j)
 			cache.remove(keys[j]);
 	}
 	
 	// =========================================================================
 	
 	protected static final class Target
			implements
				Comparable<Target>
 	{
		private static long useCounter = 0;
 		
		private long lastUse = -1;
 		
 		private final Class<?> vClass;
 		
 		private final Class<?> nClass;
 		
 		private final Method method;
 		
 		public Target(Class<?> vClass, Class<?> nClass)
 		{
 			this.vClass = vClass;
 			this.nClass = nClass;
 			this.method = null;
 		}
 		
 		public Target(Target key, Method method)
 		{
 			this.vClass = key.vClass;
 			this.nClass = key.nClass;
 			this.method = method;
 		}
 		
 		public Class<?> getVClass()
 		{
 			return vClass;
 		}
 		
 		public Class<?> getNClass()
 		{
 			return nClass;
 		}
 		
 		public Method getMethod()
 		{
 			return method;
 		}
 		
 		@Override
 		public int hashCode()
 		{
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + nClass.hashCode();
 			result = prime * result + vClass.hashCode();
 			return result;
 		}
 		
 		@Override
 		public boolean equals(Object obj)
 		{
 			Target other = (Target) obj;
 			if (nClass != other.nClass)
 				return false;
 			if (vClass != other.vClass)
 				return false;
 			return true;
 		}
 		
 		public void touch()
 		{
 			lastUse = ++useCounter;
 		}
 		
 		public Object invoke(VisitorBase<?> visitor, Object node) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
 		{
 			touch();
 			return method.invoke(visitor, node);
 		}
 		
 		@Override
 		public int compareTo(Target o)
 		{
 			// Equality is not possible!
 			return (lastUse < o.lastUse) ? -1 : +1;
 		}
 		
 		@Override
 		public String toString()
 		{
 			return String.format(
					"Target [%d - %s; %s:%s]",
					lastUse,
					method != null ? "O" : "X",
					vClass.getSimpleName(),
					nClass.getSimpleName());
 		}
 	}
 }
