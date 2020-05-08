 package sk.stuba.fiit.perconik.core.persistence.data;
 
 import java.io.Serializable;
 import javax.annotation.Nullable;
 import sk.stuba.fiit.perconik.core.Listener;
 import sk.stuba.fiit.perconik.core.Resource;
 import sk.stuba.fiit.perconik.core.persistence.ListenerRegistration;
 import sk.stuba.fiit.perconik.core.persistence.MarkableRegistration;
 import sk.stuba.fiit.perconik.core.persistence.Registration;
 import sk.stuba.fiit.perconik.core.persistence.ResourceRegistration;
 import sk.stuba.fiit.perconik.core.persistence.serialization.SerializedListenerData;
 import sk.stuba.fiit.perconik.core.persistence.serialization.SerializedResourceData;
 import sk.stuba.fiit.perconik.core.services.Services;
 import com.google.common.base.Objects;
 import com.google.common.base.Objects.ToStringHelper;
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 
 final class Utilities
 {
 	private Utilities()
 	{
 		throw new AssertionError();
 	}
 	
 	static final Class<? extends Listener> checkListenerClass(final Class<? extends Listener> type)
 	{
 		try
 		{
 			return Services.getListenerService().getListenerProvider().loadClass(type.getName());
 		}
 		catch (Exception e)
 		{
 			throw new IllegalArgumentException(e);
 		}
 	}
 	
 	static final Listener checkListenerImplementation(final Class<? extends Listener> type, @Nullable final Listener listener)
 	{
 		if (listener != null && type != listener.getClass())
 		{
 			throw new IllegalArgumentException("Listener " + listener + " is not implemented by " + type.getName());
 		}
 		
 		return listener;
 	}
 
 	static final Class<? extends Listener> checkListenerType(final Class<? extends Listener> type)
 	{
 		Preconditions.checkArgument(Listener.class.isAssignableFrom(type), "Class " + type.getName() + " is not assignable to " + Listener.class.getName());
 		
 		return type;
 	}
 
 	static final String checkResourceName(final String name)
 	{
 		Preconditions.checkArgument(!name.isEmpty(), "Resource name can not be empty");
 		
 		return name;
 	}
 	
 	static final Resource<?> checkResourceImplementation(final String name, @Nullable final Resource<?> resource)
 	{
 		if (resource != null && !name.equals(resource.getName()))
 		{
 			throw new IllegalArgumentException("Resource " + resource + " has not name " + name);
 		}
 		
 		return resource;
 	}
 	
 	static final <T> T serializableOrNull(@Nullable final T object)
 	{
 		return object instanceof Serializable ? object : null;
 	}
 
 	static final <T> Optional<T> serializableOrNullAsOptional(@Nullable final T object)
 	{
 		return Optional.fromNullable(serializableOrNull(object));
 	}
 
 	private static final ToStringHelper toStringHelperFor(final Registration registration)
 	{
 		ToStringHelper helper = Objects.toStringHelper(registration);
 		
		helper.add("registred", registration.isRegistered());
 		
 		if (registration instanceof MarkableRegistration)
 		{
			helper.add("registred mark", ((MarkableRegistration) registration).hasRegistredMark());
 		}
 		
 		helper.add("serializable", registration instanceof Serializable);
 		
 		return helper;
 	}
 
 	static final String toString(final ListenerRegistration registration)
 	{
 		ToStringHelper helper = toStringHelperFor(registration);
 		
		helper.add("listener class", registration.getListenerClass().getName());
 		helper.add("listener", registration.getListener());
 		
 		if (registration instanceof SerializedListenerData)
 		{
			helper.add("serialized listener", ((SerializedListenerData) registration).hasSerializedListener());	
 		}
 		
 		return helper.toString();
 	}
 
 	static final String toString(final ResourceRegistration registration)
 	{
 		ToStringHelper helper = toStringHelperFor(registration);
 		
		helper.add("listener type", registration.getListenerType().getName());
		helper.add("resource name", registration.getResourceName());
 		helper.add("resource", registration.getResource());
 		
 		if (registration instanceof SerializedResourceData)
 		{
			helper.add("serialized resource", ((SerializedResourceData) registration).hasSerializedResource());	
 		}
 		
 		return helper.toString();
 	}
 }
