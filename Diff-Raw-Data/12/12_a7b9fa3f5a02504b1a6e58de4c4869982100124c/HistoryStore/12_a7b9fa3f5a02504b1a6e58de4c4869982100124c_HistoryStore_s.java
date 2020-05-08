 /*
  *   Copyright 2012 Hai Bison
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package group.pals.android.lib.ui.filechooser.utils.history;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 /**
  * A history store of any object extending {@link Parcelable}.<br>
  * <b>Note:</b> This class does not support storing its {@link HistoryListener}
  * 's into {@link Parcelable}. You must re-make all listeners after getting your
  * {@link HistoryStore} from a {@link Bundle} for example.
  * 
  * @author Hai Bison
  * @since v2.0 alpha
  */
 public class HistoryStore<A extends Parcelable> implements History<A> {
 
    private ArrayList<A> mHistoryList = new ArrayList<A>();
     private final int mMaxSize;
     private final List<HistoryListener<A>> mListeners = new ArrayList<HistoryListener<A>>();
 
     /**
      * Creates new {@link HistoryStore}
      * 
      * @param maxSize
      *            the maximum size that allowed, if it is &lt;= {@code 0},
      *            {@code 11} will be used
      */
     public HistoryStore(int maxSize) {
         this.mMaxSize = maxSize > 0 ? maxSize : 11;
     }
 
     @Override
     public void push(A currentItem, A newItem) {
         int idx = currentItem == null ? -1 : mHistoryList.indexOf(currentItem);
        if (idx < 0 || idx == size() - 1)
             mHistoryList.add(newItem);
         else {
            mHistoryList = (ArrayList<A>) mHistoryList.subList(0, idx + 1);
             mHistoryList.add(newItem);
         }
 
         if (mHistoryList.size() > mMaxSize)
             mHistoryList.remove(0);
 
         notifyHistoryChanged();
     }// push()
 
     @Override
     public void remove(A item) {
         if (mHistoryList.remove(item))
             notifyHistoryChanged();
     }
 
     @Override
     public void removeAll(HistoryFilter<A> filter) {
         boolean changed = false;
         for (int i = mHistoryList.size() - 1; i >= 0; i--) {
             if (filter.accept(mHistoryList.get(i))) {
                 mHistoryList.remove(i);
                 if (!changed)
                     changed = true;
             }
         }// for
 
         if (changed)
             notifyHistoryChanged();
     }// removeAll()
 
     @Override
     public void notifyHistoryChanged() {
         for (HistoryListener<A> listener : mListeners)
             listener.onChanged(this);
     }
 
     @Override
     public int size() {
         return mHistoryList.size();
     }
 
     @Override
     public int indexOf(A a) {
         return mHistoryList.indexOf(a);
     }
 
     @Override
     public A prevOf(A a) {
         int idx = mHistoryList.indexOf(a);
         if (idx > 0)
             return mHistoryList.get(idx - 1);
         return null;
     }
 
     @Override
     public A nextOf(A a) {
         int idx = mHistoryList.indexOf(a);
         if (idx >= 0 && idx < mHistoryList.size() - 1)
             return mHistoryList.get(idx + 1);
         return null;
     }
 
     @Override
     public void addListener(HistoryListener<A> listener) {
         mListeners.add(listener);
     }
 
     @Override
     public void removeListener(HistoryListener<A> listener) {
         mListeners.remove(listener);
     }
 
     /*-----------------------------------------------------
      * Parcelable
      */
 
     static final String _HistoryList = "history_list";
     static final String _MaxSize = "max_size";
 
     @Override
     public int describeContents() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel dest, int flags) {
         Bundle b = new Bundle();
 
         b.putInt(_MaxSize, mMaxSize);
         b.putSerializable(_HistoryList, mHistoryList);
 
         dest.writeBundle(b);
     }
 
     public static final Parcelable.Creator<HistoryStore> CREATOR = new Parcelable.Creator<HistoryStore>() {
 
         public HistoryStore createFromParcel(Parcel in) {
             return new HistoryStore(in);
         }
 
         public HistoryStore[] newArray(int size) {
             return new HistoryStore[size];
         }
     };
 
     private HistoryStore(Parcel in) {
         Bundle bundle = in.readBundle();
 
         mMaxSize = bundle.getInt(_MaxSize);
        mHistoryList = (ArrayList<A>) bundle.getSerializable(_HistoryList);
     }
 }
