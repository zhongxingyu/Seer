 package org.exoplatform.common.regex;/*
  * Copyright (C) 2011 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 import org.exoplatform.common.regex.annotation.MachineConfiguration;
 import org.exoplatform.common.regex.event.MachineEventListener;
 import org.exoplatform.common.regex.util.ConfigurationUtil;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 /**
  * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
  * @date 7/5/11
  */
 public abstract class FiniteStateMachine<C, A>
 {
 
    private final State<C> startState;
 
    private State<C> currentState;
 
    private final Stack<A> stackMemory;
 
    private Map<String, MachineEventListener<C, A>> listenerMap;
 
    private C commingInput;
 
    private volatile boolean fireEvent;
 
    public FiniteStateMachine(State<C> startState)
    {
       this.startState = startState;
       this.stackMemory = new Stack<A>();
       this.currentState = startState;
       this.listenerMap = new HashMap<String, MachineEventListener<C, A>>();
       processConfig();
    }
 
    abstract protected void makeTransition();
 
    final public boolean accept(InputTape<C> inputTape) throws IOException
    {
       while(inputTape.hasNext())
       {
          commingInput = inputTape.next();
          makeTransition();
          if(currentState == null)
          {
             return false;
          }
          else if(currentState.isFinal())
          {
             return true;
          }
       }
 
       return currentState.isFinal();
    }
 
    public void reset()
    {
       this.currentState = startState;
    }
 
    public A getTop()
    {
       return stackMemory.peek();
    }
 
    public void pushTop(A item)
    {
       stackMemory.push(item);
    }
 
    public void popTop()
    {
       stackMemory.pop();
    }
 
    public C getCommingInput()
    {
       return commingInput;
    }
 
    public State<C> getCurrentState()
    {
       return currentState;
    }
 
    public void setCurrentState(State<C> nextState)
    {
       this.currentState = nextState;
    }
 
    private void processConfig()
    {
       MachineConfiguration config = this.getClass().getAnnotation(MachineConfiguration.class);
       if(config != null)
       {
         this.listenerMap = ConfigurationUtil.getListeners(config);
       }
    }
 }
