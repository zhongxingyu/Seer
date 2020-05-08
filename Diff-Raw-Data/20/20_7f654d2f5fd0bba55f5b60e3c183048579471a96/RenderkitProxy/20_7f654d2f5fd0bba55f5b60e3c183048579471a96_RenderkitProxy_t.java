 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.scripting.jsf.dynamicdecorators.implemetations;
 
 import org.apache.myfaces.scripting.api.Decorated;
 import org.apache.myfaces.scripting.api.ScriptingConst;
 import org.apache.myfaces.scripting.core.util.WeavingContext;
 import org.apache.myfaces.scripting.jsf2.annotation.purged.PurgedRenderer;
 import org.apache.myfaces.scripting.jsf2.annotation.purged.PurgedClientBehaviorRenderer;
 
 import javax.faces.context.FacesContext;
 import javax.faces.render.RenderKit;
 import javax.faces.render.Renderer;
 import javax.faces.render.ResponseStateManager;
 import javax.faces.render.ClientBehaviorRenderer;
 import javax.faces.context.ResponseWriter;
 import javax.faces.context.ResponseStream;
 import javax.faces.FacesException;
 import java.io.Writer;
 import java.io.OutputStream;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * Weaving renderkit which
  * acts as a proxy factory for
  * our internal reloading referers
  *
  * @author Werner Punz
  */
 public class RenderkitProxy extends RenderKit implements Decorated {
 
     RenderKit _delegate = null;
 
     public RenderkitProxy(RenderKit delegate) {
         _delegate = delegate;
     }
 
     public void addRenderer(String componentFamily, String rendererType, Renderer renderer) {
         weaveDelegate();
         //wo do it brute force here because we have sometimes casts and hence cannot rely on proxies
         //renderers itself are flyweight patterns which means they are shared over objects
        
         renderer = (Renderer) reloadInstance(renderer, ScriptingConst.ARTIFACT_TYPE_RENDERER);
 
         _delegate.addRenderer(componentFamily, rendererType, renderer);
     }
 
     public Renderer getRenderer(String componentFamily, String rendererType) {
         weaveDelegate();
         Renderer rendr = _delegate.getRenderer(componentFamily, rendererType);
         Renderer rendr2 = (Renderer) reloadInstance(rendr, ScriptingConst.ARTIFACT_TYPE_RENDERER);
         if (rendr != rendr2) {
             Renderer tempRenderer = _delegate.getRenderer(componentFamily, rendererType);
             if (tempRenderer instanceof PurgedRenderer) {
                 return handleAnnotationChange(componentFamily, rendererType);
             }
 
             _delegate.addRenderer(componentFamily, rendererType, rendr2);
             return rendr2;
         }
         return rendr;
     }
 
     //TODO probably not needed anymore
 
     private ClientBehaviorRenderer handleAnnotationChangeBehaviorRenderer(String s) {
         ClientBehaviorRenderer rendr2;
         //WeavingContext.getWeaver().fullClassScan();
 
         rendr2 = _delegate.getClientBehaviorRenderer(s);
         if (rendr2 instanceof PurgedClientBehaviorRenderer) {
             throw new FacesException("Renderer not found");
         }
         rendr2 = _delegate.getClientBehaviorRenderer(s);
         return rendr2;
     }
 
     //TODO probably not needed anymore
 
     private Renderer handleAnnotationChange(String s, String s1) {
         Renderer rendr2;
 
         //WeavingContext.getWeaver().fullClassScan();
         rendr2 = _delegate.getRenderer(s, s1);
         if (rendr2 instanceof PurgedRenderer) {
             throw new FacesException("Renderer not found");
         }
         rendr2 = _delegate.getRenderer(s, s1);
         return rendr2;
     }
 
     public ResponseStateManager getResponseStateManager() {
         weaveDelegate();
         return _delegate.getResponseStateManager();
     }
 
     public ResponseWriter createResponseWriter(Writer writer, String s, String s1) {
         weaveDelegate();
         return (ResponseWriter) reloadInstance(_delegate.createResponseWriter(writer, s, s1), ScriptingConst.ARTIFACT_TYPE_RESPONSEWRITER);
     }
 
     public ResponseStream createResponseStream(OutputStream outputStream) {
         weaveDelegate();
         return (ResponseStream) reloadInstance(_delegate.createResponseStream(outputStream), ScriptingConst.ARTIFACT_TYPE_RESPONSESTREAM);
     }
 
     //TODO add full support for myfaces 2.0 here
 
     @Override
     public void addClientBehaviorRenderer(String s, ClientBehaviorRenderer renderer) {
 
         weaveDelegate();
         renderer = (ClientBehaviorRenderer) reloadInstance(renderer, ScriptingConst.ARTIFACT_TYPE_CLIENTBEHAVIORRENDERER);
         _delegate.addClientBehaviorRenderer(s, renderer);
     }
 
     @Override
     public ClientBehaviorRenderer getClientBehaviorRenderer(String s) {
         weaveDelegate();
         ClientBehaviorRenderer rendr = _delegate.getClientBehaviorRenderer(s);
         ClientBehaviorRenderer rendr2 = (ClientBehaviorRenderer) reloadInstance(rendr, ScriptingConst.ARTIFACT_TYPE_CLIENTBEHAVIORRENDERER);
         if (rendr != rendr2) {
             //TODO simplyfy this
             rendr2 = _delegate.getClientBehaviorRenderer(s);
             if (rendr2 instanceof PurgedClientBehaviorRenderer) {
                 return handleAnnotationChangeBehaviorRenderer(s);
             }
             return rendr2;
         }
         return rendr;
     }
 
     @Override
     public Iterator<String> getClientBehaviorRendererTypes() {
         weaveDelegate();
         return _delegate.getClientBehaviorRendererTypes();
     }
 
     @Override
     public Iterator<String> getComponentFamilies() {
         weaveDelegate();
         return _delegate.getComponentFamilies();
     }
 
     @Override
     public Iterator<String> getRendererTypes(String s) {
         weaveDelegate();
         return _delegate.getRendererTypes(s);
     }
 
     public Object getDelegate() {
         return _delegate;
     }
 
     private final void weaveDelegate() {
         _delegate = (RenderKit) WeavingContext.getWeaver().reloadScriptingInstance(_delegate, ScriptingConst.ARTIFACT_TYPE_RENDERKIT);
     }
 
     private final Object reloadInstance(Object instance, int artefactType) {
         if (instance == null) {
             return null;
         }
         if (WeavingContext.isDynamic(instance.getClass()) && !alreadyWovenInRequest(instance.getClass().getName())) {
             alreadyWovenInRequest(instance.getClass().getName());
             instance = WeavingContext.getWeaver().reloadScriptingInstance(instance, artefactType);
 
             //now the add should be done properly if possible
         }
         return instance;
     }
 
     private final boolean alreadyWovenInRequest(String clazz) {
         //portlets now can be enabled thanks to the jsf2 indirections regarding the external context
         Map<String, Object> req = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
         if (req.get(ScriptingConst.SCRIPTING_REQUSINGLETON + clazz) == null) {
             req.put(ScriptingConst.SCRIPTING_REQUSINGLETON + clazz, "");
             return false;
         }
         return true;
     }
 
 }
