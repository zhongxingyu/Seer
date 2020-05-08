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
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import com.torrentfreak.reader.free.articles.providers.ArticleListProvider;
 import com.torrentfreak.reader.free.articles.providers.CategoryListProvider;
 import com.torrentfreak.reader.free.articles.providers.LatestNewsListProvider;
 import com.torrentfreak.reader.free.categories.CategoryType;
 
 public class CategoryItem implements Parcelable {
     /**
      * The internal category ID.
      */
     private int id;
 
     /**
      * The name of the category.
      */
     private String name;
 
     /**
      * The category type.
      */
     private CategoryType type;
 
     /**
      * The category URL.
      */
     private String url;
 
     /**
      * The number of articles per page the category serves.
      */
     private int perPage;
 
     /**
      * The icon name used in the menu item.
      */
     private String icon;
 
     /**
      * Determines whether the category is the primary category.
      */
     private boolean primary;
 
     public CategoryItem() {
 
     }
 
     public CategoryItem(final Parcel in) {
         // read the category details from the parcel
         id = in.readInt();
         name = in.readString();
         type = CategoryType.fromValue(in.readInt());
         url = in.readString();
         perPage = in.readInt();
         icon = in.readString();
         primary = in.readByte() != 0 ? true : false;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(final int id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(final String name) {
         this.name = name;
     }
 
     public CategoryType getType() {
         return type;
     }
 
     public void setType(final CategoryType type) {
         this.type = type;
     }
 
     public String getUrl() {
         return url;
     }
 
     public void setUrl(final String url) {
         this.url = url;
     }
 
     public int getPerPage() {
         return perPage;
     }
 
     public void setPerPage(final int perPage) {
         this.perPage = perPage;
     }
 
     public String getIcon() {
         return icon;
     }
 
     public void setIcon(final String icon) {
         this.icon = icon;
     }
 
     public boolean isPrimary() {
         return primary;
     }
 
     public void setPrimary(final boolean primary) {
         this.primary = primary;
     }
 
     public ArticleListProvider createProvider() {
         // create the list provider relevant to the category type
         if (type == CategoryType.LatestNews) {
             return new LatestNewsListProvider(this);
         }
 
         return new CategoryListProvider(this);
     }
 
     @Override
     public int describeContents(){
         return 0;
     }
 
     @Override
     public void writeToParcel(final Parcel dest, final int flags) {
         // write the category details to the parcel
         dest.writeInt(id);
         dest.writeString(name);
         dest.writeInt(type.getValue());
         dest.writeString(url);
         dest.writeInt(perPage);
         dest.writeString(icon);
         dest.writeByte(primary ? (byte)1 : (byte)0);
     }
 
     public static final Parcelable.Creator<CategoryItem> CREATOR =
         new Parcelable.Creator<CategoryItem>() {
         public CategoryItem createFromParcel(final Parcel in) {
             return new CategoryItem(in);
         }
 
         public CategoryItem[] newArray(final int size) {
             return new CategoryItem[size];
         }
     };
 }
