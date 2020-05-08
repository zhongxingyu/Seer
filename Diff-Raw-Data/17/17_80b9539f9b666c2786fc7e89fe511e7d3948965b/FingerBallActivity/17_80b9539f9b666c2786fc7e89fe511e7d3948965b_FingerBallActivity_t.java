 package beothorn.labs.android;
 
 import playn.android.GameActivity;
 import playn.core.PlayN;
import beothorn.fingerball.FingerBallGame;
 
 public class FingerBallActivity extends GameActivity {
 
   @Override
   public void main(){
     platform().assets().setPathPrefix("beothorn/labs/resources");
    PlayN.run(new FingerBallGame());
   }
 }
