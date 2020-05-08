 /*
  * Copyright (c) 2012-2013 Veniamin Isaias.
  *
  * This file is part of web4thejob.
  *
  * Web4thejob is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or any later version.
  *
  * Web4thejob is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with web4thejob.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.web4thejob.web.dialog;
 
 import org.springframework.context.annotation.Scope;
 import org.web4thejob.context.ContextUtil;
 import org.web4thejob.message.Message;
 import org.web4thejob.message.MessageArgEnum;
 import org.web4thejob.message.MessageEnum;
 import org.web4thejob.orm.PathMetadata;
 import org.web4thejob.util.L10nString;
 import org.web4thejob.util.ValueWrapper;
 import org.web4thejob.web.util.ZkUtil;
 import org.zkoss.zk.ui.HtmlBasedComponent;
 import org.zkoss.zkplus.databind.DataBinder;
 import org.zkoss.zul.Textbox;
 
 /**
  * @author Veniamin Isaias
  * @since 1.0.0
  */
 
 @org.springframework.stereotype.Component
 @Scope("prototype")
 public class DefaultValueInputDialog extends AbstractDialog implements ValueInputDialog {
     public static final L10nString L10N_TITLE_INPUT = new L10nString(DefaultValueInputDialog.class, "dialog_title",
             "User input");
 
     private final HtmlBasedComponent component;
     private final DataBinder dataBinder = new DataBinder();
     private final ValueWrapper valueWrapper;
     private final boolean editing;
 
     protected DefaultValueInputDialog() {
         super();
         editing = false;
         component = new Textbox();
         valueWrapper = new ValueWrapper("");
     }
 
     protected DefaultValueInputDialog(String value) {
         super();
         editing = value != null;
         component = new Textbox();
         valueWrapper = new ValueWrapper(value);
     }
 
     protected DefaultValueInputDialog(PathMetadata pathMetadata, Object value) {
         super();
         editing = value != null;
         component = (HtmlBasedComponent) ZkUtil.getEditableComponentForPropertyType(pathMetadata);
         valueWrapper = new ValueWrapper(value);
     }
 
 
     @Override
     protected String prepareTitle() {
         return L10N_TITLE_INPUT.toString();
     }
 
     @Override
     protected void prepareWindow() {
         super.prepareWindow();
         window.setMaximizable(false);
         window.setWidth("600px");
         window.setVflex("min");
     }
 
     @Override
     protected void prepareContent() {
         component.setParent(dialogContent.getPanelchildren());
         component.setHflex("true");
         ZkUtil.addBinding(dataBinder, component, "bean", "rawValue");
         dataBinder.bindBean("bean", valueWrapper);
         dataBinder.loadAll();
     }
 
     @Override
     protected boolean isOKReady() {
         dataBinder.saveAll();
        return valueWrapper.hasValue();
     }
 
     @Override
     protected Message getOKMessage() {
         MessageArgEnum messageArgEnum = editing ? MessageArgEnum.ARG_NEW_ITEM : MessageArgEnum.ARG_ITEM;
         return ContextUtil.getMessage(MessageEnum.AFFIRMATIVE_RESPONSE, this, messageArgEnum,
                 valueWrapper.getRawValue());
     }
 
     @Override
     public boolean isEditing() {
         return editing;
     }
 }
 
 
 
