 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.medic.log.impl;
 
 import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
 
 public final class StandardCallingBundleResolver implements CallingBundleResolver {
 
     private final ExecutionStackAccessor stackAccessor;
 
     private final ClassSelector classSelector;
 
    public StandardCallingBundleResolver(ExecutionStackAccessor stackAccessor, ClassSelector classSelector) {
         this.stackAccessor = stackAccessor;
         this.classSelector = classSelector;
     }
 
     public Bundle getCallingBundle() {
         Class<?>[] executionStack = this.stackAccessor.getExecutionStack();
         Class<?> loggingCallersClass = classSelector.select(executionStack);
        return FrameworkUtil.getBundle(loggingCallersClass);
     }
 }
