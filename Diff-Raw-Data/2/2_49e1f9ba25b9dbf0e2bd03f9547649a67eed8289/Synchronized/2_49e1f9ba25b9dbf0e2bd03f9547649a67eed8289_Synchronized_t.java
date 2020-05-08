 package sk.stuba.fiit.perconik.core.resources;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.Collection;
 import sk.stuba.fiit.perconik.core.Listener;
 import sk.stuba.fiit.perconik.core.Resource;
 import com.google.common.base.Preconditions;
 
 class Synchronized
 {
 	private Synchronized()
 	{
 		throw new AssertionError();
 	}
 	
 	private static class SynchronizedObject<T> implements Serializable
 	{
 		private static final long serialVersionUID = 0L;
 
 		final T delegate;
 		
 		final Object mutex;
 
 		SynchronizedObject(final T delegate, final Object mutex)
 		{
 			this.delegate = Preconditions.checkNotNull(delegate);
 			this.mutex    = (mutex == null) ? this : mutex;
 		}
 
 		@Override
 		public final String toString()
 		{
 			synchronized (this.mutex)
 			{
 				return this.delegate.toString();
 			}
 		}
 
 		private final  void writeObject(final ObjectOutputStream stream) throws IOException
 		{
 			synchronized (this.mutex)
 			{
 				stream.defaultWriteObject();
 			}
 		}
 	}
 	
 	private static final class SynchronizedResource<T extends Listener> extends SynchronizedObject<Resource<T>> implements Resource<T>
 	{
 		private static final long serialVersionUID = 0L;
 
 		SynchronizedResource(final Resource<T> resource, final Object mutex)
 		{
 			super(resource, mutex);
 		}
 
 		@Override
 		public final boolean equals(final Object o)
 		{
 			if (o == this)
 			{
 				return true;
 			}
 
 			synchronized (this.mutex)
 			{
 				return this.delegate.equals(o);
 			}
 		}
 
 		@Override
 		public final int hashCode()
 		{
 			synchronized (this.mutex)
 			{
 				return this.delegate.hashCode();
 			}
 		}
 		
 		public final void register(final T listener)
 		{
 			synchronized (this.mutex)
 			{
 				this.delegate.register(listener);
 			}
 		}
 
 		public final void unregister(final T listener)
 		{
 			synchronized (this.mutex)
 			{
 				this.delegate.register(listener);
 			}
 		}
 
 		public final void unregisterAll(final Class<? extends Listener> type)
 		{
 			synchronized (this.mutex)
 			{
 				this.delegate.unregisterAll(type);
 			}
 		}
 
 		public final <U extends Listener> Collection<U> registered(final Class<U> type)
 		{
 			synchronized (this.mutex)
 			{
 				return this.delegate.registered(type);
 			}
 		}
 	}
 	
 	private static final class SynchronizedPool<T extends Listener> extends SynchronizedObject<Pool<T>> implements Pool<T>
 	{
 		private static final long serialVersionUID = 0L;
 
 		SynchronizedPool(final Pool<T> pool, final Object mutex)
 		{
 			super(pool, mutex);
 		}
 		
 		@Override
 		public final boolean equals(final Object o)
 		{
 			if (o == this)
 			{
 				return true;
 			}
 
 			synchronized (this.mutex)
 			{
 				return this.delegate.equals(o);
 			}
 		}
 
 		@Override
 		public final int hashCode()
 		{
 			synchronized (this.mutex)
 			{
 				return this.delegate.hashCode();
 			}
 		}
 
 		public final boolean contains(final T listener)
 		{
 			synchronized (this.mutex)
 			{
 				return this.delegate.contains(listener);
 			}
 		}
 		
 		public final void add(final T listener)
 		{
 			synchronized (this.mutex)
 			{
 				this.delegate.add(listener);
 			}
 		}
 		
 		public final void remove(final T listener)
 		{
 			synchronized (this.mutex)
 			{
 				this.delegate.remove(listener);
 			}
 		}
 		
 		public final Collection<T> toCollection()
 		{
 			synchronized (this.mutex)
 			{
 				return this.delegate.toCollection();
 			}
 		}
 	}
 	
 	private static final class SynchronizedHandler<T extends Listener> extends SynchronizedObject<Handler<T>> implements Handler<T>
 	{
 		private static final long serialVersionUID = 0L;
 
 		SynchronizedHandler(final Handler<T> handler, final Object mutex)
 		{
 			super(handler, mutex);
 		}
 
 		@Override
 		public final boolean equals(final Object o)
 		{
 			if (o == this)
 			{
 				return true;
 			}
 
 			synchronized (this.mutex)
 			{
 				return this.delegate.equals(o);
 			}
 		}
 		
 		@Override
 		public final int hashCode()
 		{
 			synchronized (this.mutex)
 			{
 				return this.delegate.hashCode();
 			}
 		}
 
 		public final void add(final T listener)
 		{
 			synchronized (this.mutex)
 			{
 				this.delegate.add(listener);
 			}
 		}
 
 		public final void remove(final T listener)
 		{
 			synchronized (this.mutex)
 			{
 				this.delegate.remove(listener);
 			}
 		}
 	}
 	
 	private static final class SynchronizedHook<U, T extends Listener> extends SynchronizedObject<Hook<U, T>> implements Hook<U, T>
 	{
 		private static final long serialVersionUID = 0L;
 
 		SynchronizedHook(final Hook<U, T> handler, final Object mutex)
 		{
 			super(handler, mutex);
 		}
 
 		@Override
 		public final boolean equals(final Object o)
 		{
 			if (o == this)
 			{
 				return true;
 			}
 
 			synchronized (this.mutex)
 			{
 				return this.delegate.equals(o);
 			}
 		}
 		
 		@Override
 		public final int hashCode()
 		{
 			synchronized (this.mutex)
 			{
 				return this.delegate.hashCode();
 			}
 		}
 
 		public final void add(final U object)
 		{
 			synchronized (this.mutex)
 			{
 				this.delegate.add(object);
 			}
 		}
 
 		public final void remove(final U object)
 		{
 			synchronized (this.mutex)
 			{
 				this.delegate.remove(object);
 			}
 		}
 
 		public final Collection<U> objects()
 		{
 			synchronized (this.mutex)
 			{
 				return this.delegate.objects();
 			}
 		}
 
 		public final T forListener()
 		{
 			synchronized (this.mutex)
 			{
 				return this.delegate.forListener();
 			}
 		}
 
 		public final void preRegister()
 		{
 			synchronized (this.mutex)
 			{
 				this.delegate.preRegister();
 			}
 		}
 
 		public final void postRegister()
 		{
 			synchronized (this.mutex)
 			{
 				this.delegate.postRegister();
 			}
 		}
 
 		public final void preUnregister()
 		{
 			synchronized (this.mutex)
 			{
 				this.delegate.preUnregister();
 			}
 		}
 
 		public final void postUnregister()
 		{
 			synchronized (this.mutex)
 			{
 				this.delegate.preUnregister();
 			}
 		}
 	}
 	
 	static final <T extends Listener> Resource<T> resource(final Resource<T> resource)
 	{
 		return new SynchronizedResource<>(resource, new Object());
 	}
 	
 	static final <T extends Listener> Pool<T> pool(final Pool<T> pool)
 	{
 		return new SynchronizedPool<>(pool, new Object());
 	}
 	
 	static final <T extends Listener> Handler<T> handler(final Handler<T> handler)
 	{
 		return new SynchronizedHandler<>(handler, new Object());
 	}
 	
	static final <U, T extends Listener> Hook<U, T> hook(final Hook<U, T> hook)
 	{
 		return new SynchronizedHook<>(hook, new Object());
 	}
 }
