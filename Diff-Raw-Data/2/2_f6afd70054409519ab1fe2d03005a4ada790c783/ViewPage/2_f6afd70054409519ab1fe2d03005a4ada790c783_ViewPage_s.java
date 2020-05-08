 /*
  * The MIT License
  *
  * Copyright 2011 Robert Sandell - sandell.robert@gmail.com. All rights reserved.
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
 
 package jenkins.plugins.slideshow.model;
 
 import hudson.Extension;
 import hudson.model.Descriptor;
 import hudson.model.Hudson;
 import hudson.model.View;
 import hudson.util.ListBoxModel;
 import jenkins.plugins.slideshow.Messages;
 import jenkins.plugins.slideshow.PluginImpl;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * A page where the user can select a view to show in the slideshow.
  * Created: 7/14/11 2:06 PM
  *
  * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
  */
 public class ViewPage extends Page {
 
     private ViewPojo view;
 
     /**
      * Standard constructor.
      *
      * @param overrideTime the display time if it is overridden.
      * @param viewUrl         the URL of the view to show.
      * @see Page#Page(jenkins.plugins.slideshow.model.Page.Time)
      */
     @DataBoundConstructor
     public ViewPage(Time overrideTime, String viewUrl) {
         super(overrideTime);
         this.view = findView(viewUrl);
         if (view == null) {
            throw new IllegalArgumentException("No view with url \"" + view + "\" found!");
         }
     }
 
 
     /**
      * Default constructor.
      * <strong>Do not use unless you are a serializer.</strong>
      */
     public ViewPage() {
     }
 
     @Override
     public String getFullDisplayUrl() {
         return PluginImpl.getFromRootUrl(view.getViewUrl());
     }
 
     @Override
     public Descriptor<Page> getDescriptor() {
         return Hudson.getInstance().getDescriptorByType(ViewPageDescriptor.class);
     }
 
     /**
      * The view to show.
      *
      * @return the view
      */
     public ViewPojo getView() {
         return view;
     }
 
     /**
      * The URL to the view to show.
      * Used for stapler data binding.
      * @return the URL.
      * @see jenkins.plugins.slideshow.model.ViewPage.ViewPojo#getViewUrl()
      */
     public String getViewUrl() {
         if (view != null) {
             return view.getViewUrl();
         } else {
             return null;
         }
 
     }
 
     /**
      * Sets the view to show.
      *
      * @param view the view
      */
     public void setView(ViewPojo view) {
         this.view = view;
     }
 
     /**
      * Finds the view with the given URL.
      *
      * @param viewUrl the URL of the view to find.
      * @return the view.
      */
     public static ViewPojo findView(String viewUrl) {
         Collection<View> views = Hudson.getInstance().getViews();
         for (View v : views) {
             if (viewUrl.equals(v.getViewUrl())) {
                 return new ViewPojo(v);
             }
         }
         return null;
     }
 
     /**
      * The descriptor for {@link ViewPage}.
      */
     @Extension
     public static class ViewPageDescriptor extends PageDescriptor {
 
         @Override
         public String getDisplayName() {
             return Messages.ViewPage();
         }
 
         /**
          * Gives the select box in the Jelly file its items.
          *
          * @return all available views.
          */
         public ListBoxModel doFillViewUrlItems() {
             ListBoxModel model = new ListBoxModel();
             List<ViewPojo> views = getViews();
             for (ViewPojo v : views) {
                 model.add(v.getFullName(), v.getViewUrl());
             }
             return model;
         }
 
         /**
          * Lists all views in the system as {@link ViewPojo}s.
          *
          * @return the views.
          */
         public List<ViewPojo> getViews() {
             List<ViewPojo> list = new LinkedList<ViewPojo>();
             Collection<View> views = Hudson.getInstance().getViews();
             for (View v : views) {
                 list.add(new ViewPojo(v));
             }
             Collections.sort(list);
             return list;
         }
     }
 
     /**
      * A POJO for simpler serialization of what view to show.
      */
     public static class ViewPojo implements Serializable, Comparable<ViewPojo> {
         private String fullName;
         private String viewUrl;
 
         /**
          * Standard Constructor.
          * Creates a ViewPojo representing the given view.
          *
          * @param view the view
          */
         public ViewPojo(View view) {
             this.viewUrl = view.getViewUrl();
             this.fullName = getFullName(view);
         }
 
         /**
          * Calculates the "full name" of the view.
          *
          * @param view the view
          * @return the name.
          * @see #getFullName()
          */
         public static String getFullName(View view) {
             String owner = "";
             if (view.getOwner() != null) {
                 owner = view.getOwner().getUrl();
             }
             return owner + "/" + view.getDisplayName();
         }
 
         /**
          * Standard constructor.
          *
          * @param fullName the name.
          * @param viewUrl  the URL.
          */
         public ViewPojo(String fullName, String viewUrl) {
             this.fullName = fullName;
             this.viewUrl = viewUrl;
         }
 
         /**
          * Default constructor.
          * <strong>Do not use unless you are a serializer.</strong>
          */
         public ViewPojo() {
         }
 
         /**
          * The "full name" of the view. I.e. owner.viewUrl/view.displayName.
          *
          * @return the name.
          */
         public String getFullName() {
             return fullName;
         }
 
         /**
          * The relative URL to the view.
          *
          * @return the URL
          * @see hudson.model.View#getViewUrl()
          */
         public String getViewUrl() {
             return viewUrl;
         }
 
         @Override
         public int compareTo(ViewPojo viewPojo) {
             return fullName.compareTo(viewPojo.getFullName());
         }
 
 
         //CS IGNORE InlineConditionals FOR NEXT 12 LINES. REASON: Auto generated code.
         //CS IGNORE NeedBraces FOR NEXT 12 LINES. REASON: Auto generated code.
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             ViewPojo viewPojo = (ViewPojo)o;
 
             if (viewUrl != null ? !viewUrl.equals(viewPojo.viewUrl) : viewPojo.viewUrl != null) return false;
 
             return true;
         }
 
 
         //CS IGNORE InlineConditionals FOR NEXT 5 LINES. REASON: Auto generated code.
         //CS IGNORE NeedBraces FOR NEXT 5 LINES. REASON: Auto generated code.
         @Override
         public int hashCode() {
             return viewUrl != null ? viewUrl.hashCode() : 0;
         }
     }
 }
