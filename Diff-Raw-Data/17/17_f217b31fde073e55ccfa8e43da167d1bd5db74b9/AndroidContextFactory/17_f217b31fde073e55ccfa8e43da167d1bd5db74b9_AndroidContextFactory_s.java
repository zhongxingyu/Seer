 /*
 * LEGAL NOTICE
 *
 * Copyright (C) 2013 InventIt Inc. All rights reserved.
 *
 * This source code, product and/or document is protected under licenses 
 * restricting its use, copying, distribution, and decompilation.
 * No part of this source code, product or document may be reproduced in
 * any form by any means without prior written authorization of InventIt Inc.
 * and its licensors, if any.
 *
 * InventIt Inc.
 * 9F 4-4-7 Kojimachi, Chiyoda-ku, Tokyo 102-0083
 * JAPAN
 * http://www.yourinventit.com/
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
