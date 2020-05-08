 /**
  * Copyright 2013 ArcBees Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.arcbees.plugin.template.domain.presenter;
 
 import com.arcbees.plugin.template.create.place.CreatedNameTokens;
 
 public class CreatedNestedPresenter {
     private RenderedTemplate module;
     private RenderedTemplate presenter;
     private RenderedTemplate uihandlers;
     private RenderedTemplate view;
     private RenderedTemplate viewui;
     private CreatedNameTokens nameTokens;
     
     public CreatedNestedPresenter() {
     }
     
     public RenderedTemplate getModule() {
         return module;
     }
 
     public void setModule(RenderedTemplate module) {
         this.module = module;
     }
 
     public RenderedTemplate getPresenter() {
         return presenter;
     }
 
     public void setPresenter(RenderedTemplate presenter) {
         this.presenter = presenter;
     }
 
     public RenderedTemplate getUihandlers() {
         return uihandlers;
     }
 
     public void setUihandlers(RenderedTemplate uihandlers) {
         this.uihandlers = uihandlers;
     }
 
     public RenderedTemplate getView() {
         return view;
     }
 
     public void setView(RenderedTemplate view) {
         this.view = view;
     }
 
     public RenderedTemplate getViewui() {
         return viewui;
     }
 
     public void setViewui(RenderedTemplate viewui) {
         this.viewui = viewui;
     }
     
     public CreatedNameTokens getNameTokens() {
         return nameTokens;
     }
 
     public void setNameTokens(CreatedNameTokens nameTokens) {
         this.nameTokens = nameTokens;
     }
     
     @Override
     public String toString() {
         String s = "{ CreatedNestedPresenter ";
         s += "module=" + module + " ";
         s += "presenter=" + presenter + " ";
         s += "uihandlers=" + uihandlers + " ";
         s += "view=" + view + " ";
         s += "viewui=" + viewui + " ";
         s += " }";
         return s;
     }
 }
