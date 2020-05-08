 /* 
  * Copyright 2012 Vaadin Ltd.
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
 
 package org.vaadin.training.fundamentals.happening.ui;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.vaadin.training.fundamentals.happening.ui.view.AddNewView;
 import org.vaadin.training.fundamentals.happening.ui.view.ListHappeningsView;
 import org.vaadin.training.fundamentals.happening.ui.view.VaadinView;
 
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.NativeButton;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Button.ClickEvent;
 
 public class NavigationComponent extends VerticalLayout implements Navigation {
 
     private static final long serialVersionUID = 1L;
 
     private VaadinView<?> activeView;
     private Class<?> activeViewType;
 
     private Views views;
 
     public NavigationComponent() {
         buildLayout();
     }
 
     @SuppressWarnings("serial")
     private void buildLayout() {
         HorizontalLayout header = new HorizontalLayout();
         header.setWidth("100%");
         header.setHeight("50px");
         Label appName = new Label("Happening Application Header");
         header.addComponent(appName);
         NativeButton listButton = new NativeButton("List");
         listButton.addListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 setCurrentView(ListHappeningsView.class, null);
             }
         });
 
         NativeButton addButton = new NativeButton("Add");
         addButton.addListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 setCurrentView(AddNewView.class, null);
             }
         });
 
         NativeButton logoutButton = new NativeButton("Logout");
         listButton.addListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
 
             }
         });
         header.addComponent(listButton);
         header.addComponent(addButton);
         header.addComponent(logoutButton);
         addComponent(header);
     }
 
     public VaadinView<?> getCurrentView() {
         return activeView;
     }
 
     /**
      * Sets the current view unless the current view is already set. Uses
      * {@link #Views} to create a new instance of the view and calls its
      * {@link VaadinView#init} and gets the view content to display with
      * {@link VaadinView#getViewContent}.
      * 
      * @param type
      *            Class of a VaadinView that has a ViewProvider registered for
      *            it
      * @param params
      *            Parameters to be passed to the view
      */
     public void setCurrentView(Class<?> type, Map<String, String> params) {
         if (activeView != null && activeViewType.equals(type)) {
            // do nothing, since requested viewMock already active
             return;
         }
 
         if (activeView != null
                 && activeView instanceof Navigates.WithUserPrompt) {
             PendingNavigationCallback callback = new NavigationCallbackImpl(
                     activeView, type, params);
             if (((Navigates.WithUserPrompt) activeView)
                     .showUserPrompt(callback)) {
                 return;
             }
         }
 
         doSetCurrentView(type, params);
     }
 
     private void doSetCurrentView(Class<?> type, Map<String, String> params) {
         try {
            // Create new viewMock
             VaadinView<?> newView = views.newInstance(type);
 
            // Initialize selected viewMock (once)
             newView.init(this, params);
 
             Component newContent = newView.getViewContent();
 
            // Remove current viewMock if one exists
             if (activeView != null) {
                 removeComponent(activeView.getViewContent());
             }
 
             activeView = newView;
             activeViewType = type;
 
            // Add new viewMock
             addComponent(newContent);
             setExpandRatio(newContent, 1.0f);
 
             AppData.getUriFragmentUtility().setFragment(
                    type.getSimpleName() + toFragmentParams(params));
 
         } catch (final Exception e) {
             throw new RuntimeException("View instantiation failed!", e);
         }
     }
 
     public void setViews(Views views) {
         this.views = views;
     }
 
     /**
      * Parses URI fragment #<viewName>/<key>/<value>/<key>/<value> and returns
      * the <viewName> part of fragment
      * 
      * @param params
      * @return
      */
     public static String parseFragmentView(String fragment) {
         String[] s = fragment.split("/");
         if (s != null && s.length > 0) {
             return s[0];
         }
         return null;
     }
 
     /**
      * Parses URI fragment #<viewName>/<key>/<value>/<key>/<value> and returns a
      * map of all the key-value pairs.
      * 
      * @param params
      * @return
      */
     public static Map<String, String> parseFragmentParams(String fragment) {
         String[] s = fragment.split("/");
         if (s != null && s.length > 1 && (s.length % 2) == 1) {
             HashMap<String, String> params = new HashMap<String, String>();
             for (int i = 1; i < s.length; i += 2) {
                 params.put(s[i], s[i + 1]);
             }
             return params;
         }
         return null;
 
     }
 
    private static String toFragmentParams(Map<String, String> params) {
         if (params == null) {
             return "";
         }
         String result = "/";
         for (Entry<String, String> entry : params.entrySet()) {
             result += entry.getKey() + "/" + entry.getValue();
         }
         return result;
     }
 
     private class NavigationCallbackImpl implements
             Navigation.PendingNavigationCallback {
 
         private VaadinView<?> from;
         private Class<?> to;
         private Map<String, String> params;
 
         public NavigationCallbackImpl(VaadinView<?> from, Class<?> to,
                 Map<String, String> params) {
             this.from = from;
             this.to = to;
             this.params = params;
         }
 
         @Override
         public void commit() {
             if (activeView == from) {
                 doSetCurrentView(to, params);
             }
         }
 
         @Override
         public void discard() {
             // Do nothing
         }
     }
 }
