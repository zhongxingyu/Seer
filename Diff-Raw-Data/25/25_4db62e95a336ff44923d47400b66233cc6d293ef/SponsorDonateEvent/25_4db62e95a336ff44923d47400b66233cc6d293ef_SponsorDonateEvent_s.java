 package net.anei.cadpage.donation;
 
 import net.anei.cadpage.R;
 
 /**
   
   Sponsored by %s
 
   You are using a location sponsored by\n%s\n 
   Support for Cadpage development and maintenance is provided by the
   location sponsor.  No further payment is expected from you as long as you
   are using this location.
   
  */
 public class SponsorDonateEvent extends DonateScreenEvent {
   
   public SponsorDonateEvent() {
     super(AlertStatus.GREEN, R.string.donate_sponsor_title, R.string.donate_sponsor_text,
            AndroidDonateEvent.instance(),
            PaypalDonateEvent.instance(),
           MagicWordEvent.instance());
   }
   
   @Override
   public boolean isEnabled() {
     return (DonationManager.instance().status() == DonationManager.DonationStatus.SPONSOR);
   }
   
   @Override
   protected Object[] getTextParms(int type) {
     return new Object[]{
         DonationManager.instance().sponsor()
     };
   }
 
   private static final SponsorDonateEvent instance = new SponsorDonateEvent();
   
   public static SponsorDonateEvent instance() {
     return instance;
   }
 
 }
