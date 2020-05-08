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
 
 package com.pietschy.gwt.pectin.client.value;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * ValueModelFunction is a value model whose value is derived from collection of source
  * {@link ValueModel}s and a {@link Reduce}.  Changes in any of the source models result in
 * the function being re-evaluated and the value updating.
  */
 public class ReducingValueModel<T, S> extends AbstractValueModel<T>
 {
    private Reduce<T, ? super S> function;
    private ArrayList<ValueModel<S>> sourceModels = new ArrayList<ValueModel<S>>();
    private ValueChangeHandler<S> changeMonitor = new ValueChangeHandler<S>()
    {
       public void onValueChange(ValueChangeEvent<S> event)
       {
          tryRecompute();
       }
    };
 
    private T computedValue = null;
    private boolean ignoreChanges = false;
 
    private ReducingValueModel(Reduce<T, ? super S> function, boolean compute)
    {
       if (function == null)
       {
          throw new NullPointerException("function is null");
       }
 
       this.function = function;
 
       if (compute)
       {
          recompute();
       }
    }
 
    /**
     * Creates an instance with an empty function that always returns null.
     */
    protected ReducingValueModel()
    {
       // we use a null function.
       this(new Reduce<T, S>()
       {
          public T compute(List<? extends S> source)
          {
             return null;
          }
       }, true);
    }
 
    public ReducingValueModel(Reduce<T, ? super S> function)
    {
       this(function, true);
    }
 
    public ReducingValueModel(Reduce<T, ? super S> function, ValueModel<S> a, ValueModel<S> b)
    {
       this(function, Arrays.asList(a, b));
    }
 
    public ReducingValueModel(Reduce<T, ? super S> function, Collection<ValueModel<S>> models)
    {
       this(function, false);
 
       for (ValueModel<S> model : models)
       {
          addSourceModel(model, false);
       }
 
       recompute();
    }
 
    public void addSourceModel(ValueModel<S> model)
    {
       addSourceModel(model, true);
    }
 
    private void addSourceModel(ValueModel<S> model, boolean recompute)
    {
       if (model == null)
       {
          throw new NullPointerException("source model is null");
       }
 
       model.addValueChangeHandler(changeMonitor);
       sourceModels.add(model);
 
       if (recompute)
       {
          recompute();
       }
    }
 
    public Reduce<T, ? super S> getFunction()
    {
       return function;
    }
 
    public void setFunction(Reduce<T, ? super S> function)
    {
       if (function == null)
       {
          throw new NullPointerException("function is null");
       }
 
       this.function = function;
 
       // we use tryRecompute so we only recompute if we're not ignoring
       // changes for now.
       tryRecompute();
    }
 
    protected void tryRecompute()
    {
       if (!ignoreChanges)
       {
          recompute();
       }
    }
 
    protected void recompute()
    {
       T old = computedValue;
       computedValue = computeValue();
       fireValueChangeEvent(old, computedValue);
    }
 
    T computeValue()
    {
       ArrayList<S> values = new ArrayList<S>();
       for (ValueModel<S> model : sourceModels)
       {
          values.add(model.getValue());
       }
 
       return function.compute(values);
    }
 
    public T getValue()
    {
       return computedValue;
    }
 
    protected boolean isIgnoreChanges()
    {
       return ignoreChanges;
    }
 
    protected void setIgnoreChanges(boolean ignoreChanges)
    {
       this.ignoreChanges = ignoreChanges;
    }
 
    /**
     * Delays re-computation of the result until after the specified runnable
     * has been completed.  This method is re-entrant.
     *
     * @param r the runnable to run.
     */
    public void recomputeAfterRunning(Runnable r)
    {
       boolean oldValue = isIgnoreChanges();
       try
       {
          setIgnoreChanges(true);
          r.run();
       }
       finally
       {
          setIgnoreChanges(oldValue);
          // we could have been called in a re-entrant mode so we only
          // try and recompute.
          tryRecompute();
       }
 
    }
 }
