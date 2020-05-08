 /*
  * Copyright 2009 Andrew Pietsch 
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you 
  * may not use this file except in compliance with the License. You may 
  * obtain a copy of the License at 
  *      
  *      http://www.apache.org/licenses/LICENSE-2.0 
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing permissions 
  * and limitations under the License. 
  */
 
 package com.pietschy.gwt.pectin.client.binding;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  * Base class for binders that provides the common registration and dispose methods.
  */
 public class AbstractBinder implements BindingContainer
 {
    private ArrayList<AbstractBinding> bindings = new ArrayList<AbstractBinding>();
 
    /**
     * Registers a binding with this binder.  The binding will be disposed when this binder
     * is disposed.
     * 
     * @param binding the binding to register.
     */
    public void registerBinding(AbstractBinding binding)
    {
       binding.updateTarget();
       bindings.add(binding);
    }
 
    /**
     * Disposes all bindings created by the binder.  After this methods has finished 
     * listeners created by the bindings will be removed from all widgets and models.
     */
    public void dispose()
    {
       for (AbstractBinding binding : bindings)
       {
          binding.dispose();
       }
      bindings.clear();
    }
 
    /**
     * Disposes all bindings whose target (widget) is the specified target.
     * @param target the target (widget) for which bindings are to be disposed.
     */
    public void disposeBindingFor(Object target)
    {
       Iterator<AbstractBinding> iter = bindings.iterator();
       while (iter.hasNext())
       {
          AbstractBinding binding = iter.next();
          if (binding.getTarget().equals(target))
          {
             binding.dispose();
             iter.remove();
          }
       }
    }
 
 
 }
