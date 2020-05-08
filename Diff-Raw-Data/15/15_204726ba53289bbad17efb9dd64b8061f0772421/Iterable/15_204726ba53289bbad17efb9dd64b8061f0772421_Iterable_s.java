 package fr.cantor.functional;
 
 import java.util.Collection;
 import java.util.NoSuchElementException;
 
 import fr.cantor.functional.concurrent.ConcurrentIterable;
 import fr.cantor.functional.functions.Function1;
 import fr.cantor.functional.functions.Injecter;
 import fr.cantor.functional.functions.predicates.NotPredicate1;
 import fr.cantor.functional.functions.predicates.Predicate1;
 import fr.cantor.functional.functions.procedure.Procedure1;
 import fr.cantor.functional.nuple.Pair;
 
 /**
  * An extension to java.lang.Iterable to add functional operations like each,
  * filter, map, inject, etc.
  * 
  * @param <T>
  *            Type to iterate on
  */
 public abstract class Iterable<T> implements java.lang.Iterable<T>
 {	
 	/**
 	 * Create an Iterable from an existing java.lang.Iterable
 	 * 
 	 * @param <T>
 	 *            Type to iterate on
 	 * @param iterable
 	 *            existing java.lang.Iterable
 	 * @return a new Iterable object wrapping the iterable parameter
 	 */
 	public static <T> Iterable<T> wrap(final java.lang.Iterable<T> iterable)
 	{
 		return new Iterable<T>()
 		{
 			public Iterator<T> iterator()
 			{
 				return Iterator.wrap(iterable.iterator());
 			}
 		};
 	}
 	
 	/**
 	 * This methods is used in order to efficiently wrap an java.lang.Iterable
 	 * that would also be of our class
 	 * 
 	 * @param <T>
 	 *            Type to iterate on
 	 * @param iterable
 	 *            existing Iterable
 	 * @return directly the iterable parameter
 	 * @see #wrap(java.lang.Iterable)
 	 */
 	public static <T> Iterable<T> wrap(final Iterable<T> iterable)
 	{
 		return iterable;
 	}
 	
 	/**
 	 * The only method to implement.
 	 * We suggest you just return an implementation of our Iterator.
 	 * @see Iterator
 	 */
 	public abstract Iterator<T> iterator();
 	
 	/**
 	 * Compare two iterables to see if they return the same elements.
 	 * They must be of equal size of each element must either be the same (==)
 	 * or equals.
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public boolean equals(Object obj)
 	{
 		if ( obj == this )
 		{
 			return true;
 		}
 		if ( obj instanceof Iterable )
 		{
 			final Iterable<Object> it = (Iterable<Object>) obj;
 			try
 			{
				return combine(it).inject(Boolean.valueOf(true), new Injecter<Boolean, Pair<T, Object>>()
 				{
 					public Boolean call(Boolean bOtherEquals, Pair<T, Object> pair) throws Exception 
 					{
 						return bOtherEquals && ( ( pair.first == pair.second ) || pair.first.equals(pair.second) );
 					}
 				});
 			}
 			catch ( IterationException e )
 			{
 				// equals cannot throw :-(
 				throw new RuntimeException(e);
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Computes a value by processing every elements in the iterator Used to
 	 * build one value from every elements in the iterator. This version takes
 	 * the initial value to inject and returns a value of the same type.
 	 * 
 	 * @param <V>
 	 *            Type of the value to inject and of the resulting value
 	 * @param value
 	 *            initial value to inject
 	 * @param injecter
 	 *            Functor that will apply the injection on each element
 	 * @return the injected value modified by the injecter called for every
 	 *         elements
 	 * @throws IterationException 
 	 */
 	public <V> V inject(V value, final Injecter<V, T> injecter) throws IterationException
 	{
 		return injectWithIterator(iterator(), value, injecter);
 	}
 
 	/**
 	 * Computes a value by processing every elements in the iterator This
 	 * version uses the first value of the iterator as the initial value
 	 * 
 	 * @param injecter
 	 *            injecter object to use to compute each element injection
 	 * @return injected value modified by the injecter called for every elements
 	 * @throws IterationException 
 	 * @see #inject(Object, Injecter)
 	 */
 	public T inject(final Injecter<T, T> injecter) throws IterationException
 	{
 		Iterator<T> it = iterator();
 		if ( !it.hasNext() )
 		{
 			return null;
 		}
 		return injectWithIterator(it, it.next(), injecter);
 	}
 
 	/**
 	 * Internal implementation of inject.
 	 * @param <V> Type to inject and return
 	 * @param it iterator containing the elements to inject on
 	 * @param value initial value to inject
 	 * @param injecter closure to execute the injection
 	 * @return the injected value modified from all the injection
 	 * @throws IterationException 
 	 */
 	protected <V> V injectWithIterator(Iterator<T> it, V value, final Injecter<V, T> injecter) throws IterationException
 	{
 		while ( it.hasNext() )
 		{
 			try
 			{
 				value = injecter.call(value, it.next());
 			}
 			catch ( IterationException e ) 
 			{
 				throw e;
 			}
 			catch ( Exception e )
 			{
 				throw new IterationException(e);
 			}
 		}
 		return value;
 	}
 
 	/**
 	 * Transform every elements in the iterator
 	 * 
 	 * @param <V>
 	 *            Type of the returned iterator element
 	 * @param mapper
 	 *            Functor that will transform each element of the iterator
 	 * @return an Iterator containing the transformed elements
 	 */
 	public <V> Iterable<V> map(final Function1<V, T> mapper)
 	{
 		return new Iterable<V>()
 		{
 			public Iterator<V> iterator()
 			{
 				final Iterator<T> it = Iterable.this.iterator();
 				return new Iterator<V>()
 				{
 					public boolean hasNext() { return it.hasNext(); }
 	
 					public V next()
 					{
 						T next = it.next();
 						try
 						{
 							return mapper.call(next);
 						}
 						catch ( RuntimeException e ) 
 						{
 							throw e;
 						}
 						catch ( Exception e )
 						{
 							throw new RuntimeException(e);
 						}
 					}
 				};
 			}
 		};
 	}
 
 	/**
 	 * Filter an Iterable to keep only some elements
 	 * @param predicate Predicate that returns true for elements to be kept
 	 * @return an Iterator containing the retained elements
 	 */
 	public Iterable<T> select(final Predicate1<T> predicate)
 	{
 		return new Iterable<T>()
 		{
 			public Iterator<T> iterator()
 			{
 				final Iterator<T> it = Iterable.this.iterator();
 				return new EasierIterator<T>()
 				{
 					private T m_tCurrent;
 					
 					@Override
 					protected T getCurrent()
 					{
 						return m_tCurrent;
 					}
 
 					@Override
 					protected boolean moveNext() throws Exception
 					{
 						while ( it.hasNext() )
 						{
 							T t = it.next();
 							if ( predicate.call(t) )
 							{
 								m_tCurrent = t;
 								return true;
 							}
 						}
 						return false;
 					}
 				};
 			}
 		};
 	}
 
 	/**
 	 * @return the first element of the iterator or null if it does not exists
 	 * @throws IterationException 
 	 */
 	public T first() throws IterationException
 	{
 		return inject(new Injecter<T, T>()
 		{
 			public T call(T first, T value)
 			{
 				return first;
 			}
 		});
 	}
 
 	/**
 	 * @params predicate Predicate describing the first element to search
 	 * @return the first element of the iterator or null if it does not exists
 	 * @throws IterationException 
 	 */
 	public T first(Predicate1<T> predicate) throws IterationException
 	{
 		return select(predicate).first();
 	}
 
 	/**
 	 * Calls procedure for each element in the iterator
 	 * 
 	 * @param procedure
 	 *            Functor that will process each element
 	 * @throws Exception
 	 */
 	public void each(final Procedure1<T> procedure) throws Exception
 	{
 		inject(null, new Injecter<Void, T>() 
 		{
 			public Void call(Void unused, T value) throws Exception 
 			{
 				procedure.call(value);
 				return null;
 			}
 		});
 	}
 
 	/**
 	 * Search for any element that satisfies the predicate. Stop the iteration
 	 * after one element has been found.
 	 * 
 	 * @param predicate
 	 *            Predicate to test each element
 	 * @return true if one element satisfies the predicate, false otherwise
 	 * @throws IterationException
 	 */
 	public boolean any(final Predicate1<T> predicate) throws IterationException
 	{
 		return inject(false, new Injecter<Boolean, T>() 
 		{
 			public Boolean call(Boolean others, T value) throws Exception 
 			{
 				return others || predicate.call(value);
 			}
 		});
 	}
 
 	/**
 	 * @param predicate
 	 *            Predicate to test each element
 	 * @return true if all elements satisfy the predicate, false otherwise
 	 * @throws IterationException
 	 */
 	public boolean all(final Predicate1<T> predicate) throws IterationException 
 	{
 		return inject(true, new Injecter<Boolean, T>() 
 		{
 			public Boolean call(Boolean others, T value) throws Exception 
 			{
 				return others && predicate.call(value);
 			}
 		});
 	}
 	
 	/**
 	 * Rejects an Iterable to keep only some elements
 	 * @param predicate Predicate that returns true for elements to be kept
 	 * @return an Iterator containing the retained elements
 	 */
 	public Iterable<T> reject(final Predicate1<T> predicate)
 	{
 		return select(new NotPredicate1<T>(predicate));
 	}
 
 	/**
 	 * Dumps the iterator in a collection
 	 * 
 	 * @param collection the collection to append elements into
 	 * @return the collection filled with all elements from the iterator
 	 * @throws IterationException 
 	 */
 	public <C extends Collection<T>> C dump(C collection) throws IterationException
 	{
 		return inject(collection, new Injecter<C, T>()
 		{
 			public C call(C collection, T t)
 			{
 				collection.add(t);
 				return collection;
 			}
 		});
 	}
 	
 	/**
 	 * Concatenate every elements separated by a text. Iterator elements are
 	 * transformed into String by calling toString()
 	 * 
 	 * @param separator
 	 *            text to use to separator elements
 	 * @return the concatenated string
 	 * @throws IterationException 
 	 */
 	public String join(final String separator) throws IterationException
 	{
 		return inject(new StringBuilder(), new Injecter<StringBuilder, T>()
 		{
 			private boolean m_first = true;
 
 			public StringBuilder call(StringBuilder sb, T value) throws Exception 
 			{
 				if ( m_first )
 				{
 					m_first = false;
 				}
 				else
 				{
 					sb.append(separator);
 				}
 				sb.append(value);
 				return sb;
 			}
 		}).toString();
 	}
 
 	/**
 	 * Combines this iterable with another one to produce an iterable
 	 * of pairs of each type of the combined iterabled.
 	 * If both iterables do not contain the same amount of objects
 	 * @param <V> Iterated type of the other iterable
 	 * @param it Iterable to combine
 	 * @return an Iterable containing pairs of this iterable with the other. 
 	 */
 	public <V> Iterable<Pair<T, V>> combine(final Iterable<V> it)
 	{
 		return new Iterable<Pair<T,V>>()
 		{
 			public Iterator<Pair<T, V>> iterator()
 			{
 				final Iterator<T> it1 = Iterable.this.iterator();
 				final Iterator<V> it2 = it.iterator();
 				return new Iterator<Pair<T,V>>()
 				{
 					public boolean hasNext()
 					{
 						return it1.hasNext() || it2.hasNext();
 					}
 					public Pair<T, V> next()
 					{
 						T value1 = null;
 						try 
 						{
 							value1 = it1.hasNext() ? it1.next() : null;
 						} 
 						catch ( NoSuchElementException e ) 
 						{
 							// keep the null value
 						} 
 						V value2 = null;
 						try 
 						{
 							value2 = it2.hasNext() ? it2.next() : null;
 						} 
 						catch ( NoSuchElementException e ) 
 						{
 							// keep the null value
 						} 
 						return new Pair<T, V>(value1, value2);
 					}
 				};
 			}
 		};
 	}
 	
 	/**
 	 * Wraps the current Iterable in a ConcurrentIterable
 	 * to dispatch iteration on multiple threads
 	 * @return
 	 */
 	public Iterable<T> concurrently()
 	{
 		return new ConcurrentIterable<T>(this);
 	}
 }
