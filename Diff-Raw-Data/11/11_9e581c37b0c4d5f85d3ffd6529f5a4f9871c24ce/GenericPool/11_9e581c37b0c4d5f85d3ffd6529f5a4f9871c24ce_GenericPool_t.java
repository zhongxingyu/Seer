 package sk.stuba.fiit.perconik.core.listeners;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 final class GenericPool<T extends Listener> extends AbstractPool<T>
 {
 	private final String name;
 	
 	private final PresenceStrategy strategy;
 	
 	GenericPool(final Builder<T> builder)
 	{
 		super(builder.handler, builder.implementation);
 		
 		this.name     = this.handler.getClass().getCanonicalName().replace("Handler", "") + "Pool";
 		this.strategy = Preconditions.checkNotNull(builder.strategy);
 	}
 
 	public static final class Builder<T extends Listener>
 	{
 		Handler<T> handler;
 		
 		Collection<T> implementation;
 		
 		PresenceStrategy strategy;
 		
 		Builder(final Handler<T> handler)
 		{
 			this.handler = Preconditions.checkNotNull(handler);
 		}
 		
 		public final Builder<T> arrayList()
 		{
			Preconditions.checkState(this.implementation == null);
 			
 			this.implementation = Lists.newArrayList();
 			
 			return this;
 		}
 		
 		public final Builder<T> hashSet()
 		{
			Preconditions.checkState(this.implementation == null);
 			
 			this.implementation = Sets.newHashSet();
 			
 			return this;
 		}
 
 		public final Builder<T> linkedList()
 		{
			Preconditions.checkState(this.implementation == null);
 			
 			this.implementation = Lists.newLinkedList();
 			
 			return this;
 		}
 
 		public final Builder<T> linkedHashSet()
 		{
			Preconditions.checkState(this.implementation == null);
 			
 			this.implementation = Sets.newLinkedHashSet();
 			
 			return this;
 		}
 
 		public final Builder<T> identity()
 		{
 			Preconditions.checkState(this.strategy == null);
 			
 			this.strategy = PresenceStrategy.IDENTITY;
 			
 			return this;
 		}
 
 		public final Builder<T> equals()
 		{
 			Preconditions.checkState(this.strategy == null);
 			
 			this.strategy = PresenceStrategy.EQUALS;
 			
 			return this;
 		}
 
 		public final Pool<T> build()
 		{
 			if (this.strategy == null)
 			{
 				this.strategy = PresenceStrategy.DEFAULT;
 			}
 			
 			if (this.strategy == PresenceStrategy.IDENTITY && this.implementation instanceof HashSet)
 			{
 				this.implementation = Sets.newIdentityHashSet();
 			}
 
 			return new GenericPool<>(this);
 		}
 	}
 	
 	public static final <T extends Listener> Builder<T> builder(final Handler<T> handler)
 	{
 		return new Builder<>(handler);
 	}
 	
 	private enum PresenceStrategy
 	{
 		DEFAULT
 		{
 			@Override
 			final boolean contains(final Collection<?> collection, final Object object)
 			{
 				return collection.contains(object);
 			}
 		},
 
 		IDENTITY
 		{
 			@Override
 			final boolean contains(final Collection<?> collection, final Object object)
 			{
 				for (Object other: collection)
 				{
 					if (object == other)
 					{
 						return true;
 					}
 				}
 				
 				return false;
 			}
 		},
 
 		EQUALS
 		{
 			@Override
 			final boolean contains(final Collection<?> collection, final Object object)
 			{
 				for (Object other: collection)
 				{
 					if (Objects.equal(object, other))
 					{
 						return true;
 					}
 				}
 				
 				return false;
 			}
 		};
 		
 		abstract boolean contains(Collection<?> collection, Object object);
 	}
 	
 	public final boolean contains(final T listener)
 	{
 		return this.strategy.contains(this.listeners, listener);
 	}
 
 	public final Collection<T> toCollection()
 	{
 		return new ArrayList<>(this.listeners);
 	}
 	
 	@Override
 	public final String toString()
 	{
 		return this.getName();
 	}
 
 	public final String getName()
 	{
 		return this.name;
 	}
 }
