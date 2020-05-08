 package net.anei.cadpage.preferences;
 
 import android.content.Context;
 import android.preference.CheckBoxPreference;
 import android.view.View;
 
 public class LocationCheckBoxPreference extends CheckBoxPreference {
   
   private String location;
   private LocationManager locMgr;
 
   public LocationCheckBoxPreference(Context context, String location, String name,
                                      LocationManager locMgr) {
     super(context);
     this.location = location;
     this.locMgr = locMgr;
     
     setTitle(name);
   }
 
   @Override
   protected void onBindView(View view) {
     setChecked(locMgr.isSet(location));
     super.onBindView(view);
   }
 
   @Override
   protected void onClick() {
     super.onClick();
    locMgr.adjustLocation(isChecked(), location);
   }
 
 }
