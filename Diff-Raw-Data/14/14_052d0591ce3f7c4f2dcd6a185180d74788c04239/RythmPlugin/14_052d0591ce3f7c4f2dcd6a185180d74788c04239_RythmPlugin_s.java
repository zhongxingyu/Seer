 /* 
  * Copyright (C) 2013 The JFinal Rythm Plugin project
  * Gelin Luo <greenlaw110(at)gmail.com>
  *
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 package com.jfinal.core;
 
 import com.jfinal.config.Constants;
 import com.jfinal.plugin.IPlugin;
 import com.jfinal.render.IMainRenderFactory;
 import com.jfinal.render.Render;
 import com.jfinal.render.RenderException;
 import org.rythmengine.RythmEngine;
 import org.rythmengine.jfinal.ActiveRecordPropAccessor;
 import org.rythmengine.jfinal.LocaleManager;
 import org.rythmengine.jfinal.RythmRender;
 import org.rythmengine.template.ITemplate;
 import org.rythmengine.utils.S;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 import java.util.Map;
 
 /**
  * The JFinal Rythm Plugin
  */
 public class RythmPlugin implements IPlugin {
 
     public static RythmPlugin instance; 
 
     public RythmPlugin(Map conf) {
         this.conf = conf;
     }
 
     private Map conf;
     private RythmEngine engine;
 
     @Override
     public boolean start() {
         engine = new RythmEngine(conf);
         engine.registerPropertyAccessor(new ActiveRecordPropAccessor());
         Constants c = Config.getConstants();
         c.setMainRenderFactory(new IMainRenderFactory() {
             @Override
             public Render getRender(String view) {
                 return new RythmRender(view);
             }
 
             @Override
             public String getViewExtension() {
                 String ext = engine.conf().resourceNameSuffix();
                 if (S.empty(ext)) {
                     ext = ".html";
                 }
                 return ext;
             }
         });
         if (conf.containsKey("rythm.i18n.enabled")) {
            boolean i18n = Boolean.parseBoolean(conf.get("rythm.i18n.enabled").toString());
             if (i18n) {
                 Config.getInterceptors().add(new LocaleManager());
                 Object o = conf.get("rythm.i18n.param_name"); 
                 if (null != o) {
                     LocaleManager.setLocaleParamName(o.toString());
                 }
             }
         }
         instance = this;
         return true;
     }
 
     @Override
     public boolean stop() {
         engine.shutdown();
         return true;  
     }
     
     public void render(String view, HttpServletRequest req, HttpServletResponse res) {
         ITemplate template = engine.getTemplate(view);
         for (Enumeration<String> e = req.getAttributeNames();e.hasMoreElements();) {
             String k = e.nextElement();
             template.__setRenderArg(k, req.getAttribute(k));
         }
         template.__setRenderArg("request", req);
         template.__setRenderArg("response", req);
         PrintWriter w = null;
         try {
             w = res.getWriter();
             w.write(template.render());
         } catch (Exception e) {
             throw new RenderException(e);
         } finally {
             if (null != w) {
                 try {w.close();} catch (Exception e) {}
             }
         }
     }
 }
