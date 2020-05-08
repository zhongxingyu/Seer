 package fr.cantor.functional.concurrent;
 
import java.util.Collection;
 import java.util.HashSet;
 import java.util.NoSuchElementException;
 import java.util.Set;
import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import fr.cantor.functional.Iterable;
 import fr.cantor.functional.IterationException;
 import fr.cantor.functional.Iterator;
 import fr.cantor.functional.Range;
 import fr.cantor.functional.functions.Function1;
 import fr.cantor.functional.functions.Injecter;
 
 public class ConcurrentIterable<T> extends Iterable<T>
 {
 	private Iterable<T> m_iterable;
 
 	public ConcurrentIterable(Iterable<T> iterable)
 	{
 		m_iterable = iterable;
 	}
 	
 	@Override
 	public Iterator<T> iterator()
 	{
 		return m_iterable.iterator();
 	}
 	
 	/**
 	 * Override the implementation of inject in order to run concurrently every algorithm
 	 * based on it: any(), all(), dump(), join(), etc.
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	protected <V> V injectWithIterator(final Iterator<T> it, final V value, final Injecter<V,T> injecter) 
 	{
 		final Object result[] = { value };
 		
 		int countThreads = Runtime.getRuntime().availableProcessors();
 		ExecutorService executor = Executors.newFixedThreadPool(countThreads);
 		for ( int i = 0; i < countThreads; i += 1 ) 
 		{
 			executor.execute(new Runnable()
 			{
 				public void run()
 				{
 					while( true )
 					{
 						if( !it.hasNext() )
 						{
 							return;
 						}
 						T next;
 						try
 						{
 							next = it.next();
 						}
 						catch ( NoSuchElementException e )
 						{
 							return;
 						} 
 						synchronized( value )
 						{
 							try
 							{
 								result[0] = injecter.call((V)result[0], next);
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
 					}
 				}
 			});
 		}
 		executor.shutdown();
 		
 		try
 		{
 			executor.awaitTermination(1, TimeUnit.DAYS); // Infinite?
 		}
 		catch ( InterruptedException e )
 		{
 			// Nothing?
 		} 
 		
 		return (V)result[0];
 	}
 	
 	///////////////////////////////////////////////////////////////////////////////////////////////////
 	public static void main(String[] args) throws InterruptedException 
 	{
 		// Iterated on all numbers from 0 to 999 and transform them to words 
 		// adding some free time to simulate a CPU yield action like a disk IO 
 		final Iterable<String> it = new Range(10000).map(new Function1<String, Integer>() 
 		{
 			public String call(Integer n) throws Exception 
 			{
 				return Integer.toString(n)
 				.replaceAll("0", " Zero ")
 				.replaceAll("1", " One ")
 				.replaceAll("2", " Two ")
 				.replaceAll("3", " Three ")
 				.replaceAll("4", " Four ")
 				.replaceAll("5", " Five ")
 				.replaceAll("6", " Six ")
 				.replaceAll("7", " Seven ")
 				.replaceAll("8", " Eight ")
 				.replaceAll("9", " Nine ")
 				.trim()
 				;
 			}
 		});
 		
 		final Set<String> set1 = new HashSet<String>();
 		final Set<String> set2 = new HashSet<String>();
 				
 		System.out.println("single-threaded: " + profile(new Runnable()
 		{
 			public void run()
 			{
 				try
 				{
 					it.dump(set2);
 				}
 				catch ( IterationException e )
 				{
 					throw new RuntimeException(e);
 				}
 			}
 		}));
 		System.out.println("multi-threaded:  " + profile(new Runnable()
 		{
 			public void run()
 			{
 				try
 				{
					it.iterator().concurrently().dump(set1);
 				}
 				catch ( IterationException e )
 				{
 					throw new RuntimeException(e);
 				}
 			}
 		}));
 	}
 	
 	private static long profile(Runnable r)
 	{
 		long begin = System.nanoTime();
 		r.run();
 		long duration = System.nanoTime() - begin;
 		return duration / 1000;
 	}
 }
