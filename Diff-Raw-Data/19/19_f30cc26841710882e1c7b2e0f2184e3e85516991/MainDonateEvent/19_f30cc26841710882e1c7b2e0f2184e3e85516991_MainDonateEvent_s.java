 package net.anei.cadpage.donation;
 
 import android.app.Activity;
 import android.preference.Preference;
 
 /**
  * This is a dummy root node of the screen menu tree that is used to set
  * the main preference
  */
 public class MainDonateEvent extends DonateScreenEvent {
   
   private Activity activity;
   private Preference pref;
   
   public MainDonateEvent() {
     super(null, 0, 0,     
         FreeDonateEvent.instance(),
         AuthDonateEvent.instance(),
         PaidDonateEvent.instance(),
         PaidWarnDonateEvent.instance(),
         PaidExpireDonateEvent.instance(),
         DemoDonateEvent.instance(),
         DemoExpireDonateEvent.instance());
   }
 
   /**
    * Recalculate and redisplay donation status
    */
   public void refreshStatus() {
     setPreference(activity, pref);
   }
 
   @Override
   public boolean setPreference(Activity activity, Preference pref) {
     this.activity = activity;
     this.pref = pref;
    for (DonateEvent event : getEvents()) {
      if (event.setPreference(activity, pref)) return true;
     }
     return false;
   }
   
   private static final MainDonateEvent instance = new MainDonateEvent();
   
   public static MainDonateEvent instance() {
     return instance;
   }
 
 }
