 package sk.stuba.fiit.perconik.core.resources;
 
 import java.util.Collection;
 import java.util.Set;
 import sk.stuba.fiit.perconik.core.Listener;
 import sk.stuba.fiit.perconik.utilities.SmartStringBuilder;
 import com.google.common.collect.Sets;
 
 abstract class InternalHook<U, T extends Listener> extends AbstractHook<U, T>
 {
 	private final String name;
 	
 	InternalHook(final Collection<U> objects, final T listener)
 	{
 		super(set(objects), listener);
 		
 		this.name = name(listener);
 	}
 	
 	private static final <U> Set<U> set(final Collection<U> objects)
 	{
 		Set<U> set = Sets.newIdentityHashSet();
 		
 		set.addAll(objects);
 		
 		return set;
 	}
 	
 	private static final String name(final Listener listener)
 	{
 		SmartStringBuilder name = new SmartStringBuilder();
 		
 		name.append(listener.getClass().getCanonicalName());
 		
 		if (name.isEmpty())
 		{
 			name.append(listener.getClass().getName());
 		}
 		
 		return name.replaceLast(".", ".Internal").replaceLast("Listener", "Hook").toString();
 	}
 
 	public final void add(final U object)
 	{
 		this.objects.add(object);
 		this.addInternal(object);
 	}
 
 	public final void remove(final U object)
 	{
 		this.removeInternal(object);
 		this.objects.remove(object);
 	}
 	
 	abstract void addInternal(U object);
 	
 	abstract void removeInternal(U object);
 
 	@Override
 	public final void preRegister()
 	{
 		this.preRegisterInternal();
 		this.addAll(this.objects);
 	}
 
 	@Override
 	public final void postRegister()
 	{
 		this.postRegisterInternal();
 	}
 
 	@Override
 	public final void preUnregister()
 	{
 		this.preUnregisterInternal();
 	}
 
 	@Override
 	public final void postUnregister()
 	{
 		this.removeAll(this.objects);
 		this.postUnregisterInternal();
 	}
 	
 	void preRegisterInternal()
 	{
 	}
 
 	void postRegisterInternal()
 	{
 	}
 
 	void preUnregisterInternal()
 	{
 	}
 
 	void postUnregisterInternal()
 	{
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
