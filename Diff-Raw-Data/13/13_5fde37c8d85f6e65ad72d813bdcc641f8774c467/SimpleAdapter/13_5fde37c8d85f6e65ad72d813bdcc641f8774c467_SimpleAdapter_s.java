 /*
  * Copyright (c) 2013 Noveo Group
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
  * Except as contained in this notice, the name(s) of the above copyright holders
  * shall not be used in advertising or otherwise to promote the sale, use or
  * other dealings in this Software without prior written authorization.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package ru.tulupov.nsuconnect.util.adapter;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public abstract class SimpleAdapter<T> extends BaseAdapter {
 
     protected final Object lock = new Object();
     protected final List<T> list = new ArrayList<T>();
 
     public void updateList(List<T> list) {
         synchronized (lock) {
             this.list.clear();
             if (list != null) {
                 this.list.addAll(list);
             }
             notifyDataSetChanged();
         }
     }
 
     public void clear() {
         synchronized (lock) {
             this.list.clear();
             notifyDataSetChanged();
         }
     }
 
     public void appendList(List<T> list) {
         synchronized (lock) {
             this.list.addAll(list);
             notifyDataSetChanged();
         }
     }
 
     @Override
     public int getCount() {
         synchronized (lock) {
             return list.size();
         }
     }
 
     @Override
     public T getItem(int position) {
         synchronized (lock) {
            return list.get(position);
         }
     }
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     protected abstract View getView(Context context, int position, T item, View convertView, ViewGroup parent);
 
     @Override
     @SuppressWarnings("unchecked")
     public View getView(int position, View convertView, ViewGroup parent) {
         Context context = parent.getContext();
         T item = getItem(position);
         return getView(context, position, item, convertView, parent);
     }
 
 
 }
