 package sk.stuba.fiit.perconik.core.listeners;
 
 import com.google.common.base.Preconditions;
 
 abstract class AbstractResource<T extends Listener> implements Resource<T>
 {
 	private final Pool<T> pool;
 	
 	AbstractResource(final Pool<T> pool)
 	{
 		this.pool = Preconditions.checkNotNull(pool);
 	}
 	
 	public final void register(final T listener)
 	{
 		listener.preRegister();
 		
 		this.pool.add(listener);
 		
 		listener.postRegister();
 	}
 	
 	public final void unregister(final T listener)
 	{
 		listener.preUnregister();
 		
 		this.pool.remove(listener);
 		
 		listener.postUnregister();
 	}
 	
	public final void unregisterAll(final Class<? extends Listener> type)
 	{
 		for (T listener: this.pool.toCollection())
 		{
 			if (type.isInstance(listener))
 			{
 				this.unregister(listener);
 			}
 		}
 	}
 }
