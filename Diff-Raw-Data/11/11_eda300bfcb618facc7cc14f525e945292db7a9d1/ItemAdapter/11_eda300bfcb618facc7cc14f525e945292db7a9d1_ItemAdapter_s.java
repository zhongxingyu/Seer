 /*
  * Authored By Julian Chu <walkingice@0xlab.org>
  *
  * Copyright (c) 2012 0xlab.org - http://0xlab.org/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.zeroxlab.momome.widget;
 
 import org.zeroxlab.momome.Momo;
 import org.zeroxlab.momome.MomoApp;
 import org.zeroxlab.momome.MomoModel;
 import org.zeroxlab.momome.data.Item;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.LayoutInflater;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 import java.util.List;
 
 public class ItemAdapter extends BaseAdapter implements Momo {
 
     protected Context         mContext;
     protected LayoutInflater  mInflater;
     protected MomoModel       mModel;
 
     public ItemAdapter(Context context) {
         super();
         mContext  = context;
         mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         mModel    = MomoApp.getModel();
     }
 
     @Override
     public int getCount() {
        if (mModel.status() != DataStatus.OK) {
             return mModel.getItemsSize();
         } else {
             return 0;
         }
     }
 
     @Override
     public Object getItem(int pos) {
         if (mModel.status() == DataStatus.OK) {
             List<Item> items = mModel.getItems();
             return items.get(pos);
         } else {
             return null;
         }
     }
 
     @Override
     public long getItemId(int pos) {
         return pos;
     }
 
     @Override
     public View getView(int pos, View convertView, ViewGroup parent) {
          if (convertView == null || !(convertView instanceof TextView)) {
              convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
          }
 
          TextView tv = (TextView) convertView;
          Item item = (Item) getItem(pos);
          tv.setText(item.getTitle());
          tv.setTag(item.getId());
 
          return convertView;
     }
 }
