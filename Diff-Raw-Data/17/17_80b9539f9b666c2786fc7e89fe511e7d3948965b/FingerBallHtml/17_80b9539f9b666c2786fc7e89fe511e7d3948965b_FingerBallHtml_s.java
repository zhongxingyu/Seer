 package beothorn.labs.html;
 
 import playn.core.PlayN;
 import playn.html.HtmlGame;
 import playn.html.HtmlPlatform;

import beothorn.labs.core.FingerBall;
 
 public class FingerBallHtml extends HtmlGame {
 
   @Override
   public void start() {
     HtmlPlatform platform = HtmlPlatform.register();
     platform.assets().setPathPrefix("fingerBall/");
    PlayN.run(new FingerBall());
   }
 }
