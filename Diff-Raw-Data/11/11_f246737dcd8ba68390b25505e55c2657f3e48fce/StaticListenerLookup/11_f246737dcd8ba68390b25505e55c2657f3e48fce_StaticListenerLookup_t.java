 package sk.stuba.fiit.perconik.core.services.listeners;
 
 import sk.stuba.fiit.perconik.core.Listener;
import sk.stuba.fiit.perconik.utilities.reflect.accessor.DelayedLookup;
 import com.google.common.base.Supplier;
 
 final class StaticListenerLookup<L extends Listener> implements Supplier<L>
 {
	private final DelayedLookup<L> lookup;
 	
	private StaticListenerLookup(final DelayedLookup<L> lookup)
 	{
 		this.lookup = lookup;
 	}
 	
 	static final <L extends Listener> StaticListenerLookup<L> forClass(final Class<L> type)
 	{
		DelayedLookup.Builder<L> builder = DelayedLookup.builder();
 		
 		builder.enumConstant(type, "INSTANCE");
 		builder.enumConstant(type, "instance");
 		builder.classMethod(type, type, "getInstance");
 		builder.classConstant(type, type, "INSTANCE");
 		builder.classConstant(type, type, "instance");
 		builder.classConstructor(type);
 		
 		return new StaticListenerLookup<>(builder.build());
 	}
 	
 	public final L get()
 	{
 		return this.lookup.get();
 	}
 }
