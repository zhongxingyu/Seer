 package intro;
 
 import lge.events.Event.NullState;
import lge.events.EventNode;
 import lge.gui.Key;
 import lge.res.images.ImgChange;
 import lge.res.text.TextKey;
 import crashsite.Plane1_YourSeat;
 
 public class Airport extends EventNode<NullState> {
   public static enum Text implements TextKey {
     AIRPORT_DESCRIPTION,
     OPTION_ENTER_BUS,
     OPTION_SKIP_INTRO;
   }
   
   @Override
   protected ImgChange updateImageArea() {
     return ImgChange.setBGAndFG(IntroImage.AIRPORT);
   }
   
   @Override
   protected void doEvent() {
     setText(Text.AIRPORT_DESCRIPTION);
     
    addOption(Key.OPTION_ENTER, Text.OPTION_ENTER_BUS, new Bus());
     addOption(Key.OPTION_LEAVE, Text.OPTION_SKIP_INTRO, new Plane1_YourSeat());
   }
 }
