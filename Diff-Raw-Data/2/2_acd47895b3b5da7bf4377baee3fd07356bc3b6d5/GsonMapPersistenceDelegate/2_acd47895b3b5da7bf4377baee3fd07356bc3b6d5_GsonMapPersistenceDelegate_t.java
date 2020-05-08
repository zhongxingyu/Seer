 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.tcf.te.runtime.persistence;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.tcf.te.runtime.extensions.ExecutableExtension;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IVariableDelegate;
 import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 /**
  * GsonMapPersistenceDelegate
  */
 public class GsonMapPersistenceDelegate extends ExecutableExtension implements IPersistenceDelegate {
 
 	private final String defaultFileExtension;
 
 	protected static final String VARIABLES = "__VariablesMap__"; //$NON-NLS-1$
 
 	/**
 	 * Constructor.
 	 */
 	public GsonMapPersistenceDelegate() {
 		this("json"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Constructor.
 	 */
 	public GsonMapPersistenceDelegate(String defaultFileExtension) {
 		super();
 		Assert.isNotNull(defaultFileExtension);
 		this.defaultFileExtension = defaultFileExtension;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#getPersistedClass(java.lang.Object)
 	 */
 	@Override
 	public Class<?> getPersistedClass(Object context) {
 		return Map.class;
 	}
 
 	/**
 	 * Return the default file extension if container is an URI.
 	 */
 	protected String getDefaultFileExtension() {
 		return defaultFileExtension;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#write(java.lang.Object, java.lang.Object, java.lang.String)
 	 */
 	@Override
 	public final Object write(Object context, Object container, String key) throws IOException {
 		Assert.isNotNull(context);
 		Assert.isNotNull(container);
 
 		if (container instanceof URI) {
 			URI uri = (URI)container;
 
 			// Only "file:" URIs are supported
 			if (!"file".equalsIgnoreCase(uri.getScheme())) { //$NON-NLS-1$
 				throw new IOException("Unsupported URI schema '" + uri.getScheme() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 
 			// Create the file object from the given URI
 			File file = new File(uri.normalize());
 
 			// The file must be absolute
 			if (!file.isAbsolute()) {
 				throw new IOException("URI must denote an absolute file path."); //$NON-NLS-1$
 			}
 
 			// If the file defaultFileExtension is no set, default to "properties"
 			IPath path = new Path(file.getCanonicalPath());
 			if (path.getFileExtension() == null) {
 				file = path.addFileExtension(getDefaultFileExtension()).toFile();
 			}
 
 			Writer writer = null;
 			try {
 				writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8"); //$NON-NLS-1$
 				Gson gson = new GsonBuilder().setPrettyPrinting().create();
 
 				gson.toJson(internalToMap(context), Map.class, writer);
 			} finally {
 				if (writer != null) {
 					writer.close();
 				}
 			}
 		}
 		else if (container instanceof String || String.class.equals(container)) {
 			Gson gson = new GsonBuilder().create();
 
 			container = gson.toJson(internalToMap(context));
 		}
 
 		return container;
 	}
 
 	/*
 	 * Convert the context to a Map, extract and use variables and add them to the map as key VARIABLE.
 	 */
 	private Map<String,Object> internalToMap(Object context) {
 		try {
 			Map<String,Object> data = toMap(context);
 
 			if (data != null) {
 				Map<String,String> variables = new HashMap<String, String>();
 				IVariableDelegate[] delegates = PersistenceManager.getInstance().getVariableDelegates(this);
 				for (IVariableDelegate delegate : delegates) {
 					delegate.getVariables(data, variables);
 				}
 				if (!variables.isEmpty()) {
 					data.put(VARIABLES, variables);
 				}
 			}
 			return data;
 		}
 		catch (Exception e) {
 
 		}
 
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#read(java.lang.Object, java.lang.Object, java.lang.String)
 	 */
 	@Override
 	public final Object read(Object context, Object container, String key) throws IOException {
 		Assert.isNotNull(container);
 
 		Gson gson = new GsonBuilder().create();
 		Map<String, Object> data = null;
 
 		if (container instanceof URI) {
 			URI uri = (URI)container;
 
 			// Only "file:" URIs are supported
 			if (!"file".equalsIgnoreCase(uri.getScheme())) { //$NON-NLS-1$
 				throw new IOException("Unsupported URI schema '" + uri.getScheme() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 
 			// Create the file object from the given URI
 			File file = new File(uri.normalize());
 
 			// The file must be absolute
 			if (!file.isAbsolute()) {
 				throw new IOException("URI must denote an absolute file path."); //$NON-NLS-1$
 			}
 
 			Reader reader = null;
 			try {
 				reader = new InputStreamReader(new FileInputStream(file), "UTF-8"); //$NON-NLS-1$
 				data = gson.fromJson(reader, Map.class);
 			} finally {
 				if (reader != null) {
 					reader.close();
 				}
 			}
 		}
 		else if (container instanceof String) {
 			data = gson.fromJson((String)container, Map.class);
 		}
 
 		if (data != null && data.containsKey(VARIABLES)) {
 			Map<String,String> variables = (Map<String,String>)data.remove(VARIABLES);
 			IVariableDelegate[] delegates = PersistenceManager.getInstance().getVariableDelegates(this);
 			for (IVariableDelegate delegate : delegates) {
 				delegate.putVariables(data, variables);
 			}
 		}
 
 		return data != null ? fromMap(data, context) : context;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#delete(java.lang.Object, java.lang.Object, java.lang.String)
 	 */
 	@Override
 	public boolean delete(Object context, Object container, String key) throws IOException {
 		Assert.isNotNull(container);
 
 		if (container instanceof URI) {
 			URI uri = (URI)container;
 
 			// Only "file:" URIs are supported
 			if (!"file".equalsIgnoreCase(uri.getScheme())) { //$NON-NLS-1$
 				throw new IOException("Unsupported URI schema '" + uri.getScheme() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 
 			// Create the file object from the given URI
 			File file = new File(uri.normalize());
 
 			// The file must be absolute
 			if (!file.isAbsolute()) {
 				throw new IOException("URI must denote an absolute file path."); //$NON-NLS-1$
 			}
 
 			// If the file defaultFileExtension is no set, default to "properties"
 			IPath path = new Path(file.getCanonicalPath());
 			if (path.getFileExtension() == null) {
 				file = path.addFileExtension(getDefaultFileExtension()).toFile();
 			}
 
 			return file.delete();
 		}
 
 		return false;
 	}
 
 	/**
 	 * Convert the given context to map.
 	 *
 	 * @param context The context. Must not be <code>null</code>.
 	 * @return Map representing the context.
 	 *
 	 * @throws IOException
 	 */
 	@SuppressWarnings("unchecked")
 	protected Map<String, Object> toMap(final Object context) throws IOException {
 		Map<String, Object> result = new HashMap<String,Object>();
 
 		Map<String,Object> attrs = null;
 		if (context instanceof Map) {
 			attrs = (Map<String, Object>)context;
 		}
 		else if (context instanceof IPropertiesContainer) {
 			IPropertiesContainer container = (IPropertiesContainer)context;
 			attrs = new HashMap<String,Object>(container.getProperties());
 		}
 
 		if (attrs != null) {
 			for (Entry<String, Object> entry : attrs.entrySet()) {
 				if (!entry.getKey().endsWith(".transient")) { //$NON-NLS-1$
 					result.put(entry.getKey(), entry.getValue());
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Convert a map into the needed context object.
 	 *
 	 * @param map The map representing the context. Must not be <code>null</code>.
 	 * @param context The context to put the map values in or <code>null</code>.
 	 * @return The context object.
 	 *
 	 * @throws IOException
 	 */
 	protected Object fromMap(Map<String,Object> map, Object context) throws IOException {
		if (context == null || Map.class.equals(context)) {
 			return map;
 		}
 		else if (context instanceof Map) {
 			@SuppressWarnings({ "rawtypes", "unchecked" })
 			Map<String,Object> newMap = new HashMap<String, Object>((Map)context);
 			newMap.putAll(map);
 			return newMap;
 		}
 		else if (IPropertiesContainer.class.equals(context.getClass())) {
 			IPropertiesContainer container = new PropertiesContainer();
 			container.setProperties(map);
 
 			return container;
 		}
 		else if (context instanceof IPropertiesContainer) {
 			IPropertiesContainer container = (IPropertiesContainer)context;
 			container.setProperties(map);
 
 			return container;
 		}
 
 		return null;
 	}
 }
