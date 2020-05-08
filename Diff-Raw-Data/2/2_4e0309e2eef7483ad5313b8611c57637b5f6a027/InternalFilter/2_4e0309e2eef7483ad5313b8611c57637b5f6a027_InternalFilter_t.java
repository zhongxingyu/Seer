 package sk.stuba.fiit.perconik.core.resources;
 
 import javax.annotation.Nullable;
 import sk.stuba.fiit.perconik.core.Listener;
 import sk.stuba.fiit.perconik.core.Nameable;
 
 abstract class InternalFilter<L extends Listener> extends AbstractWrapper<L> implements Nameable
 {
 	InternalFilter(final L listener)
 	{
 		super(listener);
 	}
 
 	@Override
 	public final boolean equals(@Nullable final Object o)
 	{
 		if (this == o)
 		{
 			return true;
 		}
 		
		if (null == o || this.getClass() != o.getClass())
 		{
 			return false;
 		}
 		
 		InternalFilter<?> other = (InternalFilter<?>) o;
 		
 		return this.listener.equals(other.listener);
 	}
 
 	@Override
 	public final int hashCode()
 	{
 		return this.getName().hashCode();
 	}
 	
 	@Override
 	public final String toString()
 	{
 		return this.getName();
 	}
 
 	public final String getName()
 	{
 		return this.getClass().getName() + " for " + this.listener;
 	}
 }
