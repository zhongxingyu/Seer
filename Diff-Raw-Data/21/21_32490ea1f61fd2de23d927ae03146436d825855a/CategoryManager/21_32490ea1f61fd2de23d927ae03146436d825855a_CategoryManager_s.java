 /*
  * Copyright (C) 2013 Jack Wakefield
  *
  * This file is part of TorrentFreak Reader.
  *
  * TorrentFreak Reader is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TorrentFreak Reader is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TorrentFreak Reader.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.torrentfreak.reader.free.categories;
 
 import java.io.IOException;
 import java.lang.Exception;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 import android.content.Context;
 import android.content.res.Resources;
 import android.content.res.XmlResourceParser;
 import android.util.Log;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.torrentfreak.reader.free.categories.CategoryItem;
 import com.torrentfreak.reader.free.categories.CategoryType;
 import com.torrentfreak.reader.free.helpers.WeakReferenceHelper;
 import com.torrentfreak.reader.free.R;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 @Singleton
 public class CategoryManager {
     /**
      * The application context.
      */
     private Context context;
 
     /**
      * The list of category changed listeners.
      */
     private final ArrayList<WeakReference<OnCategoryChangedListener>> categoryChangedListeners;
 
     /**
      * The list of available categories.
      */
     private final List<CategoryItem> categories;
 
     /**
      * The current selected category.
      */
     private CategoryItem selectedCategory;
 
     @Inject
     public CategoryManager(final Context context) {
         this.context = context;
         categoryChangedListeners = new ArrayList<WeakReference<OnCategoryChangedListener>>();
         categories = new ArrayList<CategoryItem>();
 
         try {
             // attempt to parse the category.xml resource
            parseResource();
         } catch (final Exception ex) {
             Log.e("torrentfreak-reader", "Unable to parse categories - " + ex.toString());
         }
     }
 
     public List<CategoryItem> getCategories() {
         return categories;
     }
 
     public CategoryItem getCategoryById(final int id) {
         // iterate through each category attempting to find a match for the given ID
         for (final CategoryItem category : categories) {
             if (category.getId() == id) {
                 return category;
             }
         }
 
         return null;
     }
 
     public CategoryItem getCategoryByName(final String name) {
         // iterate through each category attempting to find a match for the given name
         for (final CategoryItem category : categories) {
             if (category.getName().equals(name)) {
                 return category;
             }
         }
 
         return null;
     }
 
     public void setSelectedCategory(final CategoryItem category) {
         // set the selected category
         selectedCategory = category;
 
         // iterate through each category changed listener informing the listener the category has
         // been changed
         for (final WeakReference<OnCategoryChangedListener> weakReference :
             categoryChangedListeners) {
             final OnCategoryChangedListener listener = weakReference.get();
 
             // ensure the listener exists
             if (listener != null) {
                 // inform the listener the category has been changed
                 listener.onCategoryChanged(category);
             } else {
                 // remove the old listener reference from the category listeners
                 WeakReferenceHelper.removeReference(categoryChangedListeners, listener);
             }
         }
     }
 
     public CategoryItem getSelectedCategory() {
         return selectedCategory;
     }
 
     public CategoryItem getPrimaryCategory() {
         // iterate through each category finding the primary category to return
         for (final CategoryItem category : categories) {
             if (category.isPrimary()) {
                 return category;
             }
         }
 
         // if no primary category has been found, return the first category
         if (categories.size() > 0) {
             return categories.get(0);
         }
 
         return null;
     }
 
     private void parseResource() throws XmlPullParserException, IOException {
         // retrieve the application resources and create a parser for the category XML resource
         final Resources resources = context.getResources();
         final XmlResourceParser parser = resources.getXml(R.xml.categories);
 
         String currentTag = null;
         CategoryItem currentCategory = null;
 
         // retrieve the first event type
         int eventType = parser.next();
 
         // continue iterating through each XML event until the end of the document has been reached
         while (eventType != XmlPullParser.END_DOCUMENT) {
             switch (eventType) {
                 case XmlPullParser.START_TAG:
                     // set the current tag to the element name
                     currentTag = parser.getName();
 
                     // if the opening tag is for a category, create a new category item
                     if (currentTag.equals("category")) {
                         currentCategory = new CategoryItem();
 
                         // determine whether the element includes the primary attribute
                         if (parser.getAttributeValue(null, "primary") != null) {
                             currentCategory.setPrimary(true);
                         }
                     }
                     break;
                 case XmlPullParser.TEXT:
                     // retrieve the element inner text
                     String value = parser.getText();
 
                     // ensure a category and current tag has been set
                     if (currentCategory != null && currentTag != null) {
                         // set the relevant value based on the tag name
                         if (currentTag.equals("id")) {
                             currentCategory.setId(Integer.parseInt(value));
                         } else if (currentTag.equals("name")) {
                             currentCategory.setName(value);
                         } else if (currentTag.equals("type")) {
                             if (value.equals("latest-news")) {
                                 currentCategory.setType(CategoryType.LatestNews);
                             } else {
                                 currentCategory.setType(CategoryType.Category);
                             }
                         } else if (currentTag.equals("url")) {
                             currentCategory.setUrl(value);
                         } else if (currentTag.equals("per_page")) {
                             currentCategory.setPerPage(Integer.parseInt(value));
                         } else if (currentTag.equals("icon")) {
                             currentCategory.setIcon(value);
                         }
                     }
                     break;
                 case XmlPullParser.END_TAG:
                     // if the category tag is being closed, add the current category to the category
                     // list
                     if (parser.getName().equals("category") && currentCategory != null) {
                         categories.add(currentCategory);
                     }
 
                     // clear the current tag name
                     currentTag = null;
                     break;
             }
 
             // retrieve the next event type
             eventType = parser.next();
         }
     }
 
     public void addCategoryChangedListener(final OnCategoryChangedListener listener) {
         // ensure the listener reference doesn't already exist
         if (!WeakReferenceHelper.containsReference(categoryChangedListeners, listener)) {
             // add the listener reference to the listener list
             categoryChangedListeners.add(new WeakReference<OnCategoryChangedListener>(listener));
         }
     }
 
     public void removeCategoryChangedListener(final OnCategoryChangedListener listener) {
         // remove the listener reference from category changed listeners
         WeakReferenceHelper.removeReference(categoryChangedListeners, listener);
     }
 
     public interface OnCategoryChangedListener {
         void onCategoryChanged(final CategoryItem category);
     }
 }
