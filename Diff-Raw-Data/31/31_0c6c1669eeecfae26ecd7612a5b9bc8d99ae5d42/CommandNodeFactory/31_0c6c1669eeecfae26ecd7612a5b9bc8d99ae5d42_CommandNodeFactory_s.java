 package com.huskycode.jpaquery.command;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * The way to create Command Node
  * 
  * @author Varokas Panusuwan
  */
public class CommandNodeFactory {
	static CommandNodeImpl n(Class<?> entity) {
		return new CommandNodeImpl(entity, new ArrayList<CommandNode>());
	}
	
 	public static CommandNodeImpl n(Class<?> entity, CommandNode... children) {
		return new CommandNodeImpl(entity, Arrays.asList(children));
 	}
 	
 	/**
 	 * Implementation of a Command Node
 	 */
 	public static class CommandNodeImpl implements CommandNode, CommandNodeBuilder {
 		private final Class<?> entity;
 		private final List<CommandNode> children;
 		private final Map<Field, Object> values;
 		
 		public CommandNodeImpl(Class<?> entity, List<CommandNode> children) {
 			this.entity = entity;
 			this.children = children;
 			values = new HashMap<Field, Object>(0);
 		}
 		
 		@Override
 		public Class<?> getEntity() {
 			return entity;
 		}
 
 		@Override
 		public List<CommandNode> getChildren() {
 			return children;
 		}
 		
 
 		@Override
 		public Map<Field, Object> getFieldValues() {
 			return values;
 		}
 
 		@Override
 		public String toString() {
 			return "CommandNodeImpl [entity=" + entity.getSimpleName() + ", children="
 					+ children + ", values=" + values + "]";
 		}
 
 		@Override
		public CommandNodeImpl with(CommandNode... children) {
			this.children.addAll(Arrays.asList(children));
 			return this;
 		}
 
 		@Override
 		public CommandNodeImpl withValues(Map<Field, ? extends Object> values) {
 			this.values.putAll(values);
 			return this;
 		}
 
 	}
 }
