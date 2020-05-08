 package net.anei.cadpage.donation;
 
 import net.anei.cadpage.R;
 
 /**
   
   Sponsored by Cadpage
 
   You are using a location sponsored by Cadpage.\n 
   Cadpage sponsors the general locations because their performance is 
   unreliable and we do not want to charge users who are waiting until a
   better location can be implemented.
   
  */
 public class CadpageDonateEvent extends DonateScreenEvent {
   
   public CadpageDonateEvent() {
     super(AlertStatus.GREEN, R.string.donate_cadpage_title, R.string.donate_cadpage_text,
            AndroidDonateEvent.instance(),
            PaypalDonateEvent.instance(),
           MagicWordEvent.instance());
   }
   
   @Override
   public boolean isEnabled() {
     return ("Cadpage".equals(DonationManager.instance().sponsor()));
   }
 
   private static final CadpageDonateEvent instance = new CadpageDonateEvent();
   
   public static CadpageDonateEvent instance() {
     return instance;
   }
 
 }
