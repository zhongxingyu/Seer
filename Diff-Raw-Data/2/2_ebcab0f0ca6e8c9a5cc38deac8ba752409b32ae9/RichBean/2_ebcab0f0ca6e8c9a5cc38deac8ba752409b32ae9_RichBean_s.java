 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
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
  *******************************************************************************/
 package org.richfaces.tests.metamer.bean;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.annotation.PostConstruct;
 import javax.el.ExpressionFactory;
 import javax.el.ValueExpression;
 import javax.faces.FacesException;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.model.SelectItem;
 import javax.servlet.http.HttpSession;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Managed bean storing glogal setting for the application, e.g. skin.
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 @ManagedBean
 @SessionScoped
 public class RichBean implements Serializable {
 
     private static final long serialVersionUID = 5590865106686406193L;
     private Logger logger;
     private String skin;
     private List<SelectItem> skinningList;
     private Skinning skinning;
     private List<String> skins;
     private boolean reDefault;
     private boolean reComponent;
     private boolean reTests;
     private boolean log;
     private String component;
     private Map<String, String> components; // [a4jCommandLink; A4J Command Link]
     private String container;
 
     public enum Skinning {
 
         NONE, SKINNING, SKINNING_CLASSES
     }
 
     @PostConstruct
     public void init() {
         logger = LoggerFactory.getLogger(RichBean.class);
         createSkinList();
         createComponentsMap();
 
         component = "none";
         container = "plain";
         skin = "blueSky";
         skinningList = new ArrayList<SelectItem>();
         skinningList.add(new SelectItem(Skinning.NONE));
         skinningList.add(new SelectItem(Skinning.SKINNING));
         skinningList.add(new SelectItem(Skinning.SKINNING_CLASSES));
         skinning = Skinning.SKINNING;
         reTests = false;
         reComponent = true;
     }
 
     private void createComponentsMap() {
         components = new TreeMap<String, String>();
 
         components.put("a4jActionListener", "A4J Action Listener");
         components.put("a4jAjax", "A4J Ajax");
         components.put("a4jAttachQueue", "A4J Attach Queue");
         components.put("a4jCommandLink", "A4J Command Link");
         components.put("a4jCommandButton", "A4J Command Button");
         components.put("a4jJSFunction", "A4J JavaScript Function");
         components.put("a4jLog", "A4J Log");
         components.put("a4jMediaOutput", "A4J Media Output");
         components.put("a4jOutputPanel", "A4J Output Panel");
         components.put("a4jParam", "A4J Action Parameter");
         components.put("a4jPoll", "A4J Poll");
         components.put("a4jPush", "A4J Push");
         components.put("a4jQueue", "A4J Queue");
         components.put("a4jRegion", "A4J Region");
         components.put("a4jRepeat", "A4J Repeat");
         components.put("a4jStatus", "A4J Status");
         components.put("commandButton", "JSF Command Button");
         components.put("hDataTable", "JSF Data Table");
         components.put("uiRepeat", "UI Repeat");
         components.put("richAccordion", "Rich Accordion");
         components.put("richAccordionItem", "Rich Accordion Item");
         components.put("richAutocomplete", "Rich Autocomplete");
         components.put("richCollapsiblePanel", "Rich Collapsible Panel");
         components.put("richColumn", "Rich Column");
         components.put("richColumnGroup", "Rich Column Group");
         components.put("richComponentControl", "Rich Component Control");
         components.put("richDataGrid", "Rich Data Grid");
         components.put("richDataScroller", "Rich Data Scroller");
         components.put("richDataTable", "Rich Data Table");
         components.put("richExtendedDataTable", "Rich Extended Data Table");
         components.put("richFunctions", "Rich Functions");
         components.put("richInplaceInput", "Rich Inplace Input");
         components.put("richInputNumberSlider", "Rich Input Number Slider");
         components.put("richInputNumberSpinner", "Rich Input Number Spinner");
         components.put("richJQuery", "Rich jQuery");
         components.put("richList", "Rich List");
         components.put("richPanel", "Rich Panel");
         components.put("richPopupPanel", "Rich Popup Panel");
         components.put("richProgressBar", "Rich Progress Bar");
         components.put("richSubTable", "Rich Subtable");
         components.put("richSubTableToggleControl", "Rich Subtable Toggle Control");
         components.put("richTab", "Rich Tab");
         components.put("richTabPanel", "Rich Tab Panel");
         components.put("richToggleControl", "Rich Toggle Control");
         components.put("richTogglePanel", "Rich Toggle Panel");
         components.put("richTogglePanelItem", "Rich Toggle Panel Item");
     }
 
     private void createSkinList() {
         skins = new ArrayList<String>();
         skins.add("blueSky");
         skins.add("classic");
         skins.add("deepMarine");
         skins.add("emeraldTown");
         skins.add("japanCherry");
         skins.add("ruby");
         skins.add("wine");
     }
 
     /**
      * Getter for user's skin.
      * 
      * @return a RichFaces skin
      */
     public String getSkin() {
         return skin;
     }
 
     /**
      * Setter for user's skin.
      * 
      * @param skin
      *            a RichFaces skin
      */
     public void setSkin(String skin) {
         this.skin = skin;
     }
 
     public String getSkinning() {
         if (skinning == Skinning.SKINNING) {
             return "enabled";
         } else {
             return "disabled";
         }
     }
 
     public void setSkinning(String skinning) {
         this.skinning = Skinning.valueOf(skinning);
     }
 
     public String getSkinningClasses() {
         if (skinning == Skinning.SKINNING_CLASSES) {
             return "enabled";
         } else {
             return "disabled";
         }
     }
 
     public void setSkinningClasses(String skinningClasses) {
         this.skinning = Skinning.valueOf(skinningClasses);
     }
 
     public List<SelectItem> getSkinningList() {
         return skinningList;
     }
 
     public void setSkins(List<String> skins) {
         this.skins = skins;
     }
 
     public List<String> getSkins() {
         return skins;
     }
 
     public void setReDefault(boolean reDefault) {
         this.reDefault = reDefault;
     }
 
     public boolean isReDefault() {
         return reDefault;
     }
 
     public void setReComponent(boolean reComponent) {
         this.reComponent = reComponent;
     }
 
     public boolean isReComponent() {
         return reComponent;
     }
 
     public void setLog(boolean log) {
         this.log = log;
     }
 
     public boolean isLog() {
         return log;
     }
 
     public void setComponent(String component) {
         this.component = component;
     }
 
     public String getComponent() {
         return component;
     }
 
     public Set<String> getComponentList() {
         return components.keySet();
     }
 
     /**
      * @return the components
      */
     public Map<String, String> getComponents() {
         return components;
     }
 
     /**
      * @param components
      *            the components to set
      */
     public void setComponents(Map<String, String> components) {
         this.components = components;
     }
 
     public List<String> getRichComponents() {
         List<String> richComponents = new ArrayList<String>();
         for (String aComponent : components.keySet()) {
             if (aComponent.startsWith("rich")) {
                 richComponents.add(aComponent);
             }
         }
         return richComponents;
     }
 
     public List<String> getA4JComponents() {
         List<String> a4jComponents = new ArrayList<String>();
         for (String aComponent : components.keySet()) {
             if (aComponent.startsWith("a4j")) {
                 a4jComponents.add(aComponent);
             }
         }
         return a4jComponents;
     }
 
     public List<String> getOtherComponents() {
         List<String> otherComponents = new ArrayList<String>();
         for (String aComponent : components.keySet()) {
             if (!aComponent.startsWith("rich") && !aComponent.startsWith("a4j")) {
                 otherComponents.add(aComponent);
             }
         }
         return otherComponents;
     }
 
     public String getContainer() {
         return container;
     }
 
     public void setContainer(String container) {
         this.container = container;
     }
 
     public boolean isReTests() {
         return reTests;
     }
 
     public void setReTests(boolean reTests) {
         this.reTests = reTests;
     }
 
     public String getTestsPage() {
         if (component.equals("none")) {
             return "/blank.xhtml";
         } else {
             return String.format("/components/%s/tests.xhtml", component);
         }
     }
 
     public String invalidateSession() {
         Object session = FacesContext.getCurrentInstance().getExternalContext().getSession(true);
 
         if (session == null) {
             return "/index";
         }
 
         if (session instanceof HttpSession) {
             ((HttpSession) session).invalidate();
            return FacesContext.getCurrentInstance().getViewRoot().getViewId() + "?faces-redirect=tru";
         }
 
         throw new IllegalStateException();
     }
 
     public static void logToPage(String msg) {
         FacesContext ctx = FacesContext.getCurrentInstance();
         ExpressionFactory factory = ctx.getApplication().getExpressionFactory();
         ValueExpression exp = factory.createValueExpression(ctx.getELContext(), "#{phasesBean.phases}", List.class);
         List<String> phases = (List<String>) exp.getValue(ctx.getELContext());
         phases.add(msg);
     }
 
     /**
      * Action that causes an error. Suitable for testing 'onerror' attribute.
      * 
      * @return method never returns any value
      * @throws FacesException
      *             thrown always
      */
     public String causeError() {
         throw new FacesException("Ajax request caused an error. This is intentional behavior.");
     }
 }
