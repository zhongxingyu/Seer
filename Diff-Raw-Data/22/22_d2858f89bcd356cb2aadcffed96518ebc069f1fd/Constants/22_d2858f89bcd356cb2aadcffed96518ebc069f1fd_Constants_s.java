 package com.aeroheart.owlery;
 
import android.content.res.Resources;
 
 public final class Constants {
    private static final Resources resources = Resources.getSystem();
    
    public static final String LOG_TAG = Constants.resources.getString(R.string.lib__tag);
    
    private Constants() {}
 }
