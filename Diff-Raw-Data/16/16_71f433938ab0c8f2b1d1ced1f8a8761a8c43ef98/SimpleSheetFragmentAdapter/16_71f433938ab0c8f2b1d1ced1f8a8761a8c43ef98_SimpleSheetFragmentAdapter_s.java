 package com.tackmobile.sheets;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.view.ViewGroup;
 
 public class SimpleSheetFragmentAdapter extends FragmentSheetAdapter implements ISheetListener {
 
   private static final String KEY_DESCRIPTORS = "descriptors";
   private ArrayList<SheetDescriptor> mDescriptors;
   private Context mContext;
 
   public SimpleSheetFragmentAdapter(Context context, FragmentManager fm) {
     super(fm);
     mContext = context;
   }
 
   @Override
   public int getCount() {
     return mDescriptors != null ? mDescriptors.size() : 0;
   }

   @Override
   public Fragment getItem(int position) {
     SheetDescriptor info = mDescriptors.get(position);
     return Fragment.instantiate(mContext, info.clazz.getName(), info.args);
   }
   
   @Override
   public void addSheetFragment(Class<? extends Fragment> clazz, Bundle args) {
     addSheetFragment(new SheetDescriptor(clazz, args));
   }
 
   @Override
   public void popSheetFragment(int position) {
     if (mDescriptors == null || mDescriptors.size() == 0 || position >= mDescriptors.size())
       return;
 
     mDescriptors.remove(position);
     removeFragmentAtPosition(position);
   }
 
   @Override
   public void addSheetFragment(SheetDescriptor descriptor) {
     if (mDescriptors == null) {
       mDescriptors = new ArrayList<SheetDescriptor>();
     }
     mDescriptors.add(descriptor);
     notifyDataSetChanged();
   }
   
   @Override
   public void popTopSheetFragment() {
     popSheetFragment(getCount() - 1);
   }
   
   @Override
   public void popAllSheets() {
     mDescriptors.clear();
     removeAllFragments();
   }
   
   @Override
   public Fragment instantiateItem(ViewGroup container, int position) {
     Fragment fragment = super.instantiateItem(container, position);
     if (fragment instanceof ISheetFragment) {
       ((ISheetFragment)fragment).setSheetListener(this);
     }
     return fragment;
   }
 
   @Override
   public Parcelable saveState() {
     Bundle state = (Bundle) super.saveState();
     if (mDescriptors != null && mDescriptors.size() > 0) {
       if (state == null) {
         state = new Bundle();
       }
       SheetDescriptor[] descriptors = new SheetDescriptor[mDescriptors.size()];
       mDescriptors.toArray(descriptors);
       state.putParcelableArray(KEY_DESCRIPTORS, descriptors);
     }
     return state;
   }
   
   @Override
   public void restoreState(Parcelable state, ClassLoader loader) {
     super.restoreState(state, loader);
     Bundle bundle = (Bundle) state;
     if (mDescriptors == null) {
       mDescriptors = new ArrayList<SheetDescriptor>();
     }
     mDescriptors.clear();
     
     if (bundle != null && bundle.containsKey(KEY_DESCRIPTORS)) {
       Parcelable[] descriptors = bundle.getParcelableArray(KEY_DESCRIPTORS);
       if (descriptors != null) {
         for (int i=0; i<descriptors.length; i++) {
           mDescriptors.add((SheetDescriptor)descriptors[i]);
         }
       }
     }
   }
 
   public static class SheetDescriptor implements Parcelable {
     public Class<? extends Fragment> clazz;
     public Bundle args;
 
     public SheetDescriptor(Class<? extends Fragment> clazz, Bundle args) {
       this.clazz = clazz;
       this.args = args;
     }
 
     /* Parcelable implementation */
 
     public SheetDescriptor(Parcel parcel) {
       String className = parcel.readString();
       // Interpret class
       ClassLoader cl = getClass().getClassLoader();
       Class<?> clazz = null;
       try {
         clazz = cl.loadClass(className);
         if (clazz != null && clazz.isInstance(Fragment.class)) {
           this.clazz = clazz.asSubclass(Fragment.class);
         }
       } catch (ClassNotFoundException e) {
         e.printStackTrace();
         return;
       }
       
       args = parcel.readBundle();
     }
 
     public static final Parcelable.Creator<SheetDescriptor> CREATOR = new Parcelable.Creator<SheetDescriptor>() {
       @Override
       public SheetDescriptor createFromParcel(Parcel source) {
         return new SheetDescriptor(source);
       }
 
       @Override
       public SheetDescriptor[] newArray(int size) {
         return new SheetDescriptor[size];
       }
     };
 
     @Override
     public void writeToParcel(Parcel dest, int flags) {
       dest.writeString(clazz.getCanonicalName());
     }
 
     @Override
     public int describeContents() {
       return 0;
     }
   }
 
 }
