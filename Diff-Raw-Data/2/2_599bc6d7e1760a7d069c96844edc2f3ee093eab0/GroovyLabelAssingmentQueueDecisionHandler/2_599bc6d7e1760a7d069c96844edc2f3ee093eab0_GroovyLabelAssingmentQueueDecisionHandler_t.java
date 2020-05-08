 /*
  * The MIT License
  * 
  * Copyright (c) 2013 IKEDA Yasuyuki
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package jp.ikedam.jenkins.plugins.groovy_label_assignment;
 
 import java.util.List;
//import java.util.logging.Logger;
 
 import hudson.Extension;
 import hudson.matrix.MatrixConfiguration;
 import hudson.model.Action;
 import hudson.model.AbstractProject;
 import hudson.model.Queue.QueueDecisionHandler;
 import hudson.model.Queue.Task;
 
 /**
  * Handler that handles GroovyLabelAssingmentProperty.
  * 
  * This is called before a new build is enqueued.
  * Decide label of nodes where a job with GroovyLabelAssingmentProperty will be executed.
  */
 @Extension
 public class GroovyLabelAssingmentQueueDecisionHandler extends QueueDecisionHandler
 {
     //private static final Logger LOGGER = Logger.getLogger(GroovyLabelAssingmentQueueDecisionHandler.class.getName());
     /**
      * Handles GroovyLabelAssingmentProperty.
      * 
      * Trigger CondtionalLabelProperty, which results 
      * adding LabelAssignmentAction to actions.
      * 
      * @return true if GroovyLabelAssingmentPropety is not set or GroovyLabelAssingmentProperty succeeds.
      * @see hudson.model.Queue.QueueDecisionHandler#shouldSchedule(hudson.model.Queue.Task, java.util.List)
      */
     @Override
     public boolean shouldSchedule(Task p, List<Action> actions)
     {
         if(p instanceof MatrixConfiguration)
         {
             // MatrixConfiguration is a build for each set of axes in MatrixProject.
             // In this case, GroovyLabelAssingmentProperty is set not in MatrixConfiguration,
             // but to its parent, MarixProject.
             MatrixConfiguration child = (MatrixConfiguration)p;
             AbstractProject<?,?> project = child.getParent();
             GroovyLabelAssingmentProperty prop = project.getProperty(GroovyLabelAssingmentProperty.class);
             if(prop != null)
             {
                 return prop.assignLabel(child, actions);
             }
         }
         else if(p instanceof AbstractProject<?,?>)
         {
             AbstractProject<?,?> project = (AbstractProject<?,?>)p;
             GroovyLabelAssingmentProperty prop = project.getProperty(GroovyLabelAssingmentProperty.class);
             if(prop != null)
             {
                 return prop.assignLabel(project, actions);
             }
         }
         
         // GroovyLabelAssingmentProperty is not set.
         return true;
     }
 }
