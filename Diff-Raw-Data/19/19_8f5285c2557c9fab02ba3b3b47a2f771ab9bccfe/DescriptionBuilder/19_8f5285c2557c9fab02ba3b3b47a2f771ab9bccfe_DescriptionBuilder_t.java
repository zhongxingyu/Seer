 /*
  * Copyright 2000 - 2010 Ivan Khalopik. All Rights Reserved.
  */
 
 package org.greatage.util;
 
 import java.util.List;
 
 /**
  * This class represents utility that helps to implement toString() method. It builds entity description according to
  * configured entity name and entity parameters. The result string will look like <tt>EntityName(key1=value1,key2=value2,value3,key4=value4...)</tt>
  *
  * @author Ivan Khalopik
  * @since 1.0
  */
 public class DescriptionBuilder {
 	private final String entityName;
 	private final List<String> parameters = CollectionUtils.newList();
 
 	/**
 	 * Creates new utility that helps to implement toString() method and initializes entity name with specified parameter.
 	 *
 	 * @param entityName entity name
 	 */
 	public DescriptionBuilder(final String entityName) {
 		this.entityName = entityName;
 	}
 
 	/**
 	 * Creates new utility that helps to implement toString() method and initializes entity name with simplified class
 	 * name.
 	 *
 	 * @param entityClass entity class
 	 */
 	public DescriptionBuilder(final Class entityClass) {
 		this.entityName = entityClass.getSimpleName();
 	}
 
 	/**
 	 * Appends key-value pair to entity description. They will appear in result string like <tt>(parameter=value,
 	 * parameter=value...)</tt>.
 	 *
 	 * @param key   key
 	 * @param value value
 	 * @return this description builder instance
 	 */
 	public DescriptionBuilder append(final String key, final Object value) {
 		parameters.add(key + "=" + value);
 		return this;
 	}
 
 	/**
 	 * Appends value to entity description. It will appear in result string like <tt>(value, value...)</tt>.
 	 *
 	 * @param value value
 	 * @return this description builder instance
 	 */
 	public DescriptionBuilder append(final Object value) {
 		parameters.add(String.valueOf(value));
 		return this;
 	}
 
 	/**
 	 * Builds entity description according to configured earlier entity name and entity parameters. The result string will
 	 * look like <tt>EntityName(key1=value1,key2=value2,value3,key4=value4...)</tt>
 	 *
 	 * @return entity description built according to configured earlier entity name and entity parameters
 	 */
 	public String toString() {
		final StringBuilder builder = new StringBuilder(entityName);
 		if (!CollectionUtils.isEmpty(parameters)) {
			builder.append("(");
			final StringBuilder parametersBuilder = new StringBuilder();
 			for (String parameter : parameters) {
				if (parametersBuilder.length() > 0) {
					parametersBuilder.append(", ");
				}
				parametersBuilder.append(parameter);
 			}
			builder.append(parametersBuilder.toString());
			builder.append(")");
 		}
		return builder.toString();
 	}
 }
