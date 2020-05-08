 /*
 * Copyright (C) 2013 InventIt Inc.
 * 
 * See https://github.com/inventit/moatandroid-examples
  */
 package com.yourinventit.moat.android.example;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.Context;
 
 import com.yourinventit.dmc.api.moat.ContextFactory;
 
 /**
  * The {@link ContextFactory} providing Android's {@link Context} object.
  * 
  * @author dbaba@yourinventit.com
  * 
  */
 public class AndroidContextFactory implements ContextFactory {
 
 	/**
 	 * {@link Context}
 	 */
 	private final Context context;
 
 	/**
 	 * 
 	 * @param context
 	 */
 	public AndroidContextFactory(Context context) {
 		this.context = context;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see com.yourinventit.dmc.api.moat.ContextFactory#createExecutionContext(java.lang.Object,
 	 *      java.lang.String)
 	 */
 	public <T> Map<String, Object> createExecutionContext(T model,
 			String methodName) {
 		final Map<String, Object> executionContext = new HashMap<String, Object>();
 		executionContext.put(Context.class.getName(), context);
 		return executionContext;
 	}
 
 }
