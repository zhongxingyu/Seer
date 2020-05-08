 /*
  * JBoss, Home of Professional Open Source
  * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
 package org.jboss.log4j.el;
 
 import org.apache.log4j.Layout;
 import org.apache.log4j.spi.LoggingEvent;
 import org.jboss.log4j.el.impl.AppenderContext;
 import org.jboss.log4j.el.impl.LoggingEventContext;
 
 import javax.el.ELContext;
 import javax.el.ExpressionFactory;
 import javax.el.ValueExpression;
 
 /**
  * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
  */
 public class ExpressionLanguageLayout extends Layout
 {
    private static final Class<String> EXPECTED_TYPE = String.class;
 
    private String expression;
    
    private ExpressionFactory expressionFactory;
    
    private AppenderContext baseContext;
    private ValueExpression valueExpression;
 
    public void activateOptions()
    {
       if(expression == null)
          throw new IllegalStateException("no expression set");
 
       expressionFactory = ExpressionFactory.newInstance();
       baseContext = new AppenderContext(expressionFactory);
    }
    
    @Override
    public String format(LoggingEvent loggingEvent)
    {
       ELContext context = new LoggingEventContext(baseContext, loggingEvent);
       valueExpression = expressionFactory.createValueExpression(context, expression, EXPECTED_TYPE);
      try
      {
         return EXPECTED_TYPE.cast(valueExpression.getValue(context));
      }
      catch(RuntimeException e)
      {
         e.printStackTrace();
         return "Error in expression " + expression + "--> " + e + "\n";
      }
    }
 
    @Override
    public boolean ignoresThrowable()
    {
      return true;
    }
 
    public void setExpression(String s)
    {
       this.expression = s;
    }
 }
