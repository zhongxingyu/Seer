 package autoeq.modules.rezwait;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import autoeq.ThreadScoped;
 import autoeq.eq.ChatListener;
 import autoeq.eq.Command;
 import autoeq.eq.EverquestSession;
 import autoeq.eq.Me;
 import autoeq.eq.Module;
 
 import com.google.inject.Inject;
 
 
 @ThreadScoped
 public class RezWaitModule implements Module {
   private final EverquestSession session;
 
   private long deathMillis;
 //  private float lastExperience;
   private boolean death;
 
   @Inject
   public RezWaitModule(final EverquestSession session) {
     this.session = session;
 
     session.addChatListener(new ChatListener() {
 
       @Override
       public Pattern getFilter() {
         return Pattern.compile("(You regain some experience from resurrection\\.|You gained party experience!)");
       }
 
       @Override
       public void match(Matcher matcher) {
         session.echo("REZWAIT: Rezzed or gained experience, cancelling automatic camp-out.");
         death = false;
         deathMillis = 0;
       }
     });
   }
 
   @Override
   public boolean isLowLatency() {
     return false;
   }
 
   @Override
   public List<Command> pulse() {
     Me me = session.getMe();
 
     if(!me.isAlive() && !death) {
       death = true;
       deathMillis = System.currentTimeMillis();
       session.echo("REZWAIT: We died.");
     }
 
     if(deathMillis != 0) {
       long waitedMillis = System.currentTimeMillis() - deathMillis;
 
       session.delay(2500);
 
       // If dead more than an hour then camp out
       if(waitedMillis > 60 * 60 * 1000) {
         session.echo("REZWAIT: Waited for an hour, camping out.");
         session.doCommand("/camp desktop");
         session.delay(60 * 1000);
       }
       else if((waitedMillis / 1000) % 60 == 0) {
         session.echo("REZWAIT: " + (60 - (waitedMillis / 1000 / 60)) + " minutes left before camping out");
         session.delay(1000);
       }
 
       /*
        * Auto accept rez
        */
 
      if(session.evaluate("${Window[ConfirmationDialogBox].Child[CD_TextOutput].Text.Find[percent) upon you.]}")) {
         session.delay(1000);
         session.doCommand("/nomodkey /notify ConfirmationDialogBox Yes_Button leftmouseup");
         session.delay(1000);
 
         if(session.delay(2500, "${Window[RespawnWnd].Open}")) {
           session.doCommand("/nomodkey /notify RespawnWnd RW_OptionsList listselect 2");
           session.delay(1000);
           session.doCommand("/nomodkey /notify RespawnWnd RW_SelectButton leftmouseup");
           session.delay(1000);
         }
       }
 
       // TODO Useless now because no corpses need looting, but it blocks other commands from processing.
       return new ArrayList<>(Arrays.asList(new Command[] {new LootCorpseCommand()}));
     }
 
     return null;
   }
 
 //  public List<Command> pulse() {
 //    Me me = session.getMe();
 //    float currentExperience = me.getExperience() + me.getLevel();
 //
 //    if(lastExperience == 0) {
 //      lastExperience = currentExperience;
 //    }
 //
 //    float expDelta = currentExperience - lastExperience;
 //    lastExperience = currentExperience;
 //
 //    if(expDelta < 0) {
 //      if(deathMillis == 0) {
 //        deathMillis = System.currentTimeMillis();
 //      }
 //    }
 //    else if(expDelta > 0) {
 //      if(deathMillis != 0) {
 //        session.echo("REZWAIT: Rezzed or gained experience, cancelling automatic camp-out.");
 //      }
 //      deathMillis = 0;
 //    }
 //
 //    if(deathMillis != 0) {
 //      long waitedMillis = System.currentTimeMillis() - deathMillis;
 //
 //      // If dead more than an hour then camp out
 //      if(waitedMillis > 60 * 60 * 1000) {
 //        session.echo("REZWAIT: Waited for an hour, camping out.");
 //        session.doCommand("/camp desktop");
 //        session.delay(60 * 1000);
 //      }
 //      else if((waitedMillis / 1000) % 60 == 0) {
 //        session.echo("REZWAIT: " + (60 - (waitedMillis / 1000 / 60)) + " minutes left before camping out");
 //        session.delay(1000);
 //      }
 //
 //      // TODO Use now because no corpses need looting, but it blocks other commands from processing.
 //      return new ArrayList<Command>(Arrays.asList(new Command[] {new LootCorpseCommand()}));
 //    }
 //
 //    return null;
 //  }
 }
