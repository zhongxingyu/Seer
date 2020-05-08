 /**
  * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
  * Licensed under the Common Development and Distribution License,
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.sun.com/cddl/
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package com.sun.facelets.compiler;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.el.ELException;
 import javax.el.ExpressionFactory;
 import javax.faces.FacesException;
 import javax.faces.context.FacesContext;
 
 import com.sun.facelets.FaceletException;
 import com.sun.facelets.FaceletHandler;
 import com.sun.facelets.tag.CompositeTagDecorator;
 import com.sun.facelets.tag.CompositeTagLibrary;
 import com.sun.facelets.tag.TagDecorator;
 import com.sun.facelets.tag.TagLibrary;
 import com.sun.facelets.util.Assert;
 import com.sun.facelets.util.FacesAPI;
 
 /**
  * A Compiler instance may handle compiling multiple sources
  * 
  * @author Jacob Hookom
 * @version $Id: Compiler.java,v 1.3 2005-07-11 21:54:10 jhook Exp $
  */
 public abstract class Compiler {
 
     protected final static Logger log = Logger.getLogger("facelets.compiler");
 
     public final static String EXPRESSION_FACTORY = "compiler.ExpressionFactory";
 
     private static final TagLibrary EMPTY_LIBRARY = new CompositeTagLibrary(
             new TagLibrary[0]);
 
     private static final TagDecorator EMPTY_DECORATOR = new CompositeTagDecorator(
             new TagDecorator[0]);
 
    private boolean validating = true;
 
    private boolean trimmingWhitespace = true;
 
    private boolean trimmingComments = false;
 
     private final List libraries = new ArrayList();
 
     private final List decorators = new ArrayList();
 
     private final Map features = new HashMap();
 
     private boolean initialized = false;
 
     /**
      * 
      */
     public Compiler() {
         this.features.put(EXPRESSION_FACTORY,
                 "com.sun.el.ExpressionFactoryImpl");
     }
 
     private synchronized void initialize() {
         if (this.initialized)
             return;
         log.fine("Initializing");
         try {
             TagLibraryConfig cfg = new TagLibraryConfig();
             cfg.loadImplicit(this);
         } catch (IOException e) {
             log.log(Level.SEVERE, "Compiler Initialization Error", e);
         } finally {
             this.initialized = true;
         }
         log.fine("Initialization Successful");
     }
 
     public final FaceletHandler compile(URL src, String alias)
             throws IOException, FaceletException, ELException, FacesException {
         if (!this.initialized)
             this.initialize();
         return this.doCompile(src, alias);
     }
 
     protected abstract FaceletHandler doCompile(URL src, String alias)
             throws IOException, FaceletException, ELException, FacesException;
 
     public TagDecorator createTagDecorator() {
         if (this.decorators.size() > 0) {
             return new CompositeTagDecorator((TagDecorator[]) this.decorators
                     .toArray(new TagDecorator[this.decorators.size()]));
         }
         return EMPTY_DECORATOR;
     }
 
     public void addTagDecorator(TagDecorator decorator) {
         Assert.param("decorator", decorator);
         if (!this.decorators.contains(decorator)) {
             this.decorators.add(decorator);
         }
     }
 
     public ExpressionFactory createExpressionFactory() {
         ExpressionFactory el = null;
         if (FacesAPI.getVersion() >= 12) {
             try {
                 el = FacesContext.getCurrentInstance().getApplication()
                         .getExpressionFactory();
             } catch (Exception e) {
                 // do nothing
             }
         }
         if (el == null) {
             log
                     .warning("No default ExpressionFactory from Faces Implementation, attempting to load from Feature["
                             + EXPRESSION_FACTORY + "]");
             el = (ExpressionFactory) this.featureInstance(EXPRESSION_FACTORY);
         }
         return el;
     }
 
     private Object featureInstance(String name) {
         String type = (String) this.features.get(name);
         if (type != null) {
             try {
                 return Class.forName(type).newInstance();
             } catch (Throwable t) {
                 throw new FaceletException("Could not instantiate feature["
                         + name + "]: " + type);
             }
         }
         return null;
     }
 
     public TagLibrary createTagLibrary() {
         if (this.libraries.size() > 0) {
             return new CompositeTagLibrary((TagLibrary[]) this.libraries
                     .toArray(new TagLibrary[this.libraries.size()]));
         }
         return EMPTY_LIBRARY;
     }
 
     public void addTagLibrary(TagLibrary library) {
         Assert.param("library", library);
         if (!this.libraries.contains(library)) {
             this.libraries.add(library);
         }
     }
 
     public void setFeature(String name, String value) {
         this.features.put(name, value);
     }
 
     public String getFeature(String name) {
         return (String) this.features.get(name);
     }
 
     public boolean isTrimmingComments() {
         return this.trimmingComments;
     }
 
     public void setTrimmingComments(boolean trimmingComments) {
         this.trimmingComments = trimmingComments;
     }
 
     public boolean isTrimmingWhitespace() {
         return this.trimmingWhitespace;
     }
 
     public void setTrimmingWhitespace(boolean trimmingWhitespace) {
         this.trimmingWhitespace = trimmingWhitespace;
     }
 
     public boolean isValidating() {
         return this.validating;
     }
 
     public void setValidating(boolean validating) {
         this.validating = validating;
     }
 }
