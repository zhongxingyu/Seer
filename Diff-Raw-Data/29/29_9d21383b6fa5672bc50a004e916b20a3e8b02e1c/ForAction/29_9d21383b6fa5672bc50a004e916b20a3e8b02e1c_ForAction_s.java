 /*
  * JAHM - Java Advanced Hierarchical Model 
  * 
  * ForAction.java
  * 
  * Copyright 2009 Robert Arvin Dunnagan
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.xmodel.xaction;
 
 import java.util.List;
 
 import org.xmodel.IModelObject;
 import org.xmodel.xpath.expression.IContext;
 import org.xmodel.xpath.expression.IExpression;
 import org.xmodel.xpath.expression.StatefulContext;
 import org.xmodel.xpath.variable.IVariableScope;
 
 
 /**
  * An XAction which executes another action once with each context from a node-set expression.
  * The current node being iterated can be accessed either through the assigned variable or 
  * through the context (if the <i>assign</i> attribute is not specified). In the latter case,
  * a new StatefulContext is created for each iteration and variable assignments within the
  * for loop will be local to the for loop.
  */
 public class ForAction extends GuardedAction
 {
   /* (non-Javadoc)
    * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.ViewModel)
    */
   @Override
   public void configure( XActionDocument document)
   {
     super.configure( document);
     
     // node-set iteration
     IModelObject config = document.getRoot();
    var = Conventions.getVarName( config, false, "assign");    
    sourceExpr = document.getExpression( "source", true);
 
     // OR numeric iteration
     fromExpr = document.getExpression( "from", true);
     toExpr = document.getExpression( "to", true);
     byExpr = document.getExpression( "by", true);
         
     // reuse ScriptAction to handle for script (must temporarily remove condition if present)
     Object when = config.removeAttribute( "when");
     script = document.createScript( "source");
     if ( when != null) config.setAttribute( "when", when);
     
     // hookup continue actions
     for( IXAction action: script.getActions())
       if ( action instanceof ContinueAction)
         ((ContinueAction)action).setFor( this);
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.ui.swt.form.actions.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
    */
   protected Object[] doAction( IContext context)
   {
     IVariableScope scope = null;
     if ( scope == null)
     {
       scope = context.getScope();
       if ( scope == null)
         throw new IllegalArgumentException( 
           "ForAction context does not have a variable scope: "+this);
     }
     
     // node-set iteration 
    if ( sourceExpr != null)
     {
      List<IModelObject> nodes = sourceExpr.evaluateNodes( context);
       for( int i=0; i<nodes.size(); i++)
       {
         // store the current element in either a variable or the context
         if ( var != null) scope.set( var, nodes.get( i));
         else context = new StatefulContext( context, nodes.get( i), i+1, nodes.size());
         
         Object[] result = script.run( context);
         if ( result != null && !continued) return result;
         if ( continued) continued = false;
       }
     }
     
     // numeric iteration
     if ( var != null && fromExpr != null && toExpr != null)
     {
       double from = fromExpr.evaluateNumber( context);
       double to = toExpr.evaluateNumber( context);
       double by = (byExpr != null)? byExpr.evaluateNumber( context): 1;
       if ( by >= 0)
       {
         for( double i = from; i <= to; i += by)
         {
           scope.set( var, i);
           Object[] result = script.run( context);
           if ( result != null && !continued) return result;
           if ( continued) continued = false;
         }
       }
       else
       {
         for( double i = from; i >= to; i += by)
         {
           scope.set( var, i);
           Object[] result = script.run( context);
           if ( result != null && !continued) return result;
           if ( continued) continued = false;
         }
       }
     }
     
     return null;
   }
   
   /**
    * Called from ContinueAction.
    */
   protected final void doContinue()
   {
     continued = true;
   }
 
   private String var;
  private IExpression sourceExpr;
   private IExpression fromExpr;
   private IExpression toExpr;
   private IExpression byExpr;
   private ScriptAction script;
   private boolean continued;
 }
