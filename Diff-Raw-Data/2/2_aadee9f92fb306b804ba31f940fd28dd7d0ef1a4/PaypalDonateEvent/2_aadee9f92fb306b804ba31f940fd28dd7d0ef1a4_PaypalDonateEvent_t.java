 package net.anei.cadpage.donation;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import net.anei.cadpage.R;
 import net.anei.cadpage.SmsPopupUtils;
 
 /**
 Donate through PayPal
  */
 public class PaypalDonateEvent extends DonateEvent {
   
  private static final String TARGET_URL = "http://www.cadpage.org/financial-support/paypal-payments";
   
   public PaypalDonateEvent() {
     super(AlertStatus.GREEN, R.string.donate_paypal_title);
   }
 
   @Override
   protected void doEvent(Activity activity) {
     if (!SmsPopupUtils.haveNet(activity)) return;
     Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(TARGET_URL)); 
     activity.startActivity(intent);
     closeEvents(activity);
   }
   
   private static final PaypalDonateEvent instance = new PaypalDonateEvent();
   
   public static PaypalDonateEvent instance() {
     return instance;
   }
 
 }
