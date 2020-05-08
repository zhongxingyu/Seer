 package se.l4.aurochs.config.internal;
 
 import java.util.Arrays;
 import java.util.Map;
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Path;
 import javax.validation.Path.Node;
 import javax.validation.Validator;
 import javax.validation.ValidatorFactory;
 
 import se.l4.aurochs.config.Config;
 import se.l4.aurochs.config.ConfigException;
 import se.l4.aurochs.config.Value;
 import se.l4.aurochs.config.internal.streaming.MapInput;
 import se.l4.aurochs.serialization.Serializer;
 import se.l4.aurochs.serialization.SerializerCollection;
 import se.l4.aurochs.serialization.format.StreamingInput;
 
 import com.google.common.base.Joiner;
 
 /**
  * Default implementation of {@link Config}.
  * 
  * @author Andreas Holstenson
  *
  */
 public class DefaultConfig
 	implements Config
 {
 	private final SerializerCollection collection;
 	private final Map<String, Object> data;
 	private final ValidatorFactory validatorFactory;
 	
 	public DefaultConfig(SerializerCollection collection, 
 			ValidatorFactory validatorFactory, 
 			Map<String, Object> data)
 	{
 		this.collection = collection;
 		this.validatorFactory = validatorFactory;
 		this.data = data;
 	}
 	
 	private Object get(String path)
 	{
 		String[] parts = path.split("\\.");
 		Map<String, Object> current = data;
 		for(int i=0, n=parts.length-1; i<n; i++)
 		{
 			if(current.containsKey(parts[i]))
 			{
 				Object o = current.get(parts[i]);
 				if(o instanceof Map)
 				{
 					current = (Map) o;
 				}
 				else
 				{
 					// TODO: Proper error message
 					String subPath = Joiner
 						.on('.')
 						.join(Arrays.copyOf(parts, i+1));
 					
					throw new ConfigException("Expected several values at `" + subPath + "` but only got a single value: " + o);
 				}
 			}
 			else
 			{
 				return null;
 			}
 		}
 		
 		return current.get(parts[parts.length - 1]);
 	}
 
 	@Override
 	public <T> T asObject(String path, Class<T> type)
 	{
 		return get(path, type).get();
 	}
 	
 	private void validateInstance(String path, Object object)
 	{
 		Validator validator = validatorFactory.getValidator();
 		Set<ConstraintViolation<Object>> violations = validator.validate(object);
 		
 		if(violations.isEmpty())
 		{
 			// No violations
 			return;
 		}
 		
		StringBuilder builder = new StringBuilder("Validation failed for `" + path + "`:\n");
 		
 		for(ConstraintViolation<Object> violation : violations)
 		{
 			builder
 				.append("* ")
 				.append(join(violation.getPropertyPath()))
 				.append(violation.getMessage())
 				.append("\n");
 		}
 		
 		throw new ConfigException(builder.toString());
 	}
 	
 	private String join(Path path)
 	{
 		StringBuilder builder = new StringBuilder();
 		for(Node node : path)
 		{
 			if(builder.length() > 0)
 			{
 				builder.append(".");
 			}
 			
 			builder.append(node.getName());
 		}
 		
 		if(builder.length() > 0)
 		{
 			builder.append(": ");
 		}
 		
 		return builder.toString();
 	}
 	
 	@Override
 	public <T> Value<T> get(String path, Class<T> type)
 	{
 		Serializer<T> serializer = collection.find(type);
 		if(serializer == null)
 		{
 			throw new ConfigException("Unable to find a serializer suitable for " + type);
 		}
 		
 		Object data = get(path);
 		if(data == null)
 		{
			throw new ConfigException("Configuration data for `" + path + "` is missing");
 		}
 		
 		StreamingInput input = MapInput.resolveInput(data);
 		try
 		{
 			T instance = serializer.read(input);
 			
 			validateInstance(path, instance);
 			
 			return new ValueImpl<T>(instance);
 		}
 		catch(ConfigException e)
 		{
 			throw e;
 		}
 		catch(Exception e)
 		{
			throw new ConfigException("Unable to create " + type + " from data at `" + path + "`; " + e.getMessage(), e);
 		}
 	}
 	
 	private static class ValueImpl<T>
 		implements Value<T>
 	{
 		private final T instance;
 
 		public ValueImpl(T instance)
 		{
 			this.instance = instance;
 		}
 
 		@Override
 		public T get()
 		{
 			return instance;
 		}
 	}
 }
