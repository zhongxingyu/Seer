 package bombi;
 
 import java.awt.Graphics2D;
 
 /**
  * Diese Klasse erzeugt Objekte, welche als Bomben interpretiert werden.
  * 
  * @author Charalambos Georgiadis
  **/
 public class Bomben {
 
     // Ein paar Konstanten für die verschiedene Arten von Bomben
     private static final short EXPLODED = 0;
     private static final short BOMB = 1;
     private static final short EXPLODING = 2;
     // Countdowns & Timer
     private static final short COUNTDOWN = 120;
     private static final short ANIMCOUNTER = 10;
     private static final short RADIUSDELAY = 0;
 
     // Verschiedene Variablen
     private int state = BOMB;
     private int posX, posY;
     private int radius;
     private int radiusup = 0;
     private int radiusdown = 0;
     private int radiusleft = 0;
     private int radiusright = 0;
     private int countdown = COUNTDOWN;
     private int animCounter = ANIMCOUNTER;
     private int radiusDelayCounter = RADIUSDELAY;
     private int animFrame = 0;
     private BombermanLevel bLevel;
     Player player;
     private int breakup = 0;
     private int breakdown = 0;
     private int breakright = 0;
     private int breakleft = 0;
     private int fireu=0;
     private int fired=0;
     private int firel=0;
     private int firer=0;
 
     /**
      * Diese Methode erstellt die Bomben.
      * 
      * @param posX Position des Spielers auf der X-Achse
      * @param posY Position des Spielers auf der Y-Achse
      * @param radius Explosionsradius der Bombe
      * @param player Der Spieler der die Bombe gelegt hat
      * @param bLevel Level in dem die Bombe gezeichnet wird
      */
     public Bomben(int posX, int posY, int radius,Player player, BombermanLevel bLevel) {
         this.posX = posX;
         this.posY = posY;
         this.radius = radius;
         this.bLevel = bLevel;
         this.player = player;
         bLevel.putBombByPixel(posX, posY);
     }
     
     /**
      * 
      * @return Gibt die Position der Bombe auf der X-Achse zurück
      */
     public int getPosX() {
         return posX;
     }
 
     /**
      * 
      * @return Gibt die Position der Bombe auf der Y-Achse zurück
      */
     public int getPosY() {
         return posY;
     }
   
     // Update für die Bombe. Countdown etc wird runter gezählt
     // Sobald Countdown auf 0 steht, wir markiert welche Steine zerstört werden
     // und prüft, ob unzerstörbare im Weg sind.
     /**
      * Überprüft dauerhaft den Zustand der Bombe. Falls die Bombe noch nicht
      * explodiert, läuft ein Countdown runter bis zur Explosion. Falls die Bombe
      * explodiert wird markiert, wo der Strahl sichtbar ist,fügt Feuer für die Dauer der Explosion
      * hinzu & enfernt es nach der Explosion, markiert welche Blöcke zerstört werden müssen und löst
      * die Kettenreaktionen aus.
      */
     public void update() {
         if (state == EXPLODED)
             return;
         int width = bLevel.getTileWidth();
         int height = bLevel.getTileHeight();
 
         if (countdown > 0) {
             countdown--;
              if (bLevel.hasFireByPixel(posX, posY)){
         	countdown=0;
             }
         }
         else if (state == BOMB ) {
             state = EXPLODING;
         } else if (state == EXPLODING && radius > 0) {
             if (radiusDelayCounter > 0) {
                 radiusDelayCounter--;
                 return;
             }
             radiusDelayCounter = RADIUSDELAY;
             radius--;
             radiusup++;
             radiusdown++;
             radiusleft++;
             radiusright++;
             bLevel.addFireByPixel(posX, posY);
             for (int i = 1; i <= radiusup; i++ ) {
             	if (bLevel.getTileByPixel(posX, posY - i * height)==0 && fireu==0){
                 bLevel.addFireByPixel(posX, posY - i * height);}
             	else if (bLevel.getTileByPixel(posX, posY - i * height)==1 && fireu==0){
             		bLevel.addFireByPixel(posX, posY - i * height);
             		fireu=1;
             	}
             	else if (bLevel.getTileByPixel(posX, posY - i * height)==2 && fireu==0)
             		fireu=1;
             	if (bLevel.getTileByPixel(posX, posY - i * height) == 2 && breakup == 0)
             		breakup=1;
                 if (bLevel.getTileByPixel(posX, posY - i * height) == 1 && breakup == 0 || bLevel.getTileByPixel(posX, posY - i * height) == 4 && breakup == 0) {
                     bLevel.destroyBlockByPixel(posX, posY - i * height);
                     breakup = 1;
                     player.addpoints();
                 }
             }
             for (int i = 1; i <= radiusdown; i++) {
                 if (bLevel.getTileByPixel(posX, posY + i * height) == 0 && fired==0) {
                 	bLevel.addFireByPixel(posX, posY + i * height);
                 }
                 else if(bLevel.getTileByPixel(posX, posY + i*height)==1 && fired==0) {
                 	bLevel.addFireByPixel(posX, posY + i * height);
                 	fired=1;
                 }
                 else if (bLevel.getTileByPixel(posX, posY + i*height)==2 && fired==0)
                 	fired=1;
                 if (bLevel.getTileByPixel(posX, posY + i * height) == 2 && breakdown == 0) 
                 	breakdown=1;
                 if (bLevel.getTileByPixel(posX, posY + i * height) == 1 && breakdown == 0 || bLevel.getTileByPixel(posX, posY + i * height) == 4 && breakdown == 0) {
                     bLevel.destroyBlockByPixel(posX, posY + i * height);
                     breakdown = 1;
                     player.addpoints();
                 }
             }
             for (int i = 1; i <= radiusright; i++) {
                 if (bLevel.getTileByPixel(posX + i * width, posY) == 0 && firer==0) {
                 	bLevel.addFireByPixel(posX + i * width, posY);
                 }
                 else if(bLevel.getTileByPixel(posX + i * width, posY)==1 && firer==0) {
                 	bLevel.addFireByPixel(posX + i * width, posY);
                 	firer=1;
                 }
                 else if(bLevel.getTileByPixel(posX + i * width, posY)==2 && firer==0)
                 	firer=1;
                 if (bLevel.getTileByPixel(posX + i * width, posY) == 2 && breakright == 0)
                 	breakright=1;
                 if (bLevel.getTileByPixel(posX + i * width, posY) == 1 && breakright == 0 || bLevel.getTileByPixel(posX + i * width, posY) == 4 && breakright == 0) {
                     bLevel.destroyBlockByPixel(posX + i * width, posY);
                     breakright = 1;
                 	player.addpoints();
                 }
             }
             for (int i = 1; i <= radiusleft; i++) {
                 if (bLevel.getTileByPixel(posX - i * width, posY) == 0 && firel==0) {
                 	bLevel.addFireByPixel(posX - i * width, posY);
                 }
                 else if(bLevel.getTileByPixel(posX - i * width, posY)==1 && firel==0) {
                 	bLevel.addFireByPixel(posX - i * width, posY);
                 	firel=1;
                 }
                 else if (bLevel.getTileByPixel(posX - i * width, posY)==2 && firel==0)
                 	firel=1;
                 if (bLevel.getTileByPixel(posX - i * width, posY) == 2 && breakleft == 0) 
                 	breakleft=1;
                 if (bLevel.getTileByPixel(posX - i * width, posY) == 1 && breakleft == 0 || bLevel.getTileByPixel(posX - i * width, posY) == 4 && breakleft == 0) {
                     bLevel.destroyBlockByPixel(posX - i * width, posY);
                     breakleft = 1;
                     player.addpoints();
                 }
              } player.playAudio.playSound("Bumm"); // sound abspielen
         } else if (radiusDelayCounter > 0)
             radiusDelayCounter--;
         else {
             state = EXPLODED;
            /*bLevel.removeFireByPixel(posX, posY);
             for (int i = 1; i <= radiusup; i++) {
                 bLevel.removeFireByPixel(posX, posY - i * height);
             }
             for (int i = 1; i <= radiusdown; i++) {
                 bLevel.removeFireByPixel(posX, posY + i * height);
             }
             for (int i = 1; i <= radiusright; i++) {
                 bLevel.removeFireByPixel(posX + i * width, posY);
             }
             for (int i = 1; i <= radiusleft; i++) {
                 bLevel.removeFireByPixel(posX - i * width, posY);
             }
            */
             bLevel.removeBombByPixel(posX, posY);
             breakright = 0;
             breakleft = 0;
             breakup = 0;
             breakdown = 0;
             fireu=0;
             fired=0;
             firel=0;
             firer=0;
 
         }
     } 
 
 
     /**
      * Diese Methode zeichnet die Bombe und in den Feldern in denen es Feuer gibt
      * die Explosion.
      * @param g Das Graphics-Objekt, welches genutzt wird, um die Bomben zu
      * zeichnen.
      */
     public void draw(Graphics2D g) {
         if (state == EXPLODED)
             return;
         int width = bLevel.getTileWidth();
         int height = bLevel.getTileHeight();
         if (state == BOMB) {
             if (animCounter > 0)
                 animCounter--;
             else {
                 animFrame = (animFrame + 1) % Texture.BOMB.length;
                 animCounter = ANIMCOUNTER;
             }
             Texture.BOMB[animFrame].draw(posX, posY, width, height, g);
             
         } else if (state == EXPLODING) {
             Texture.EXPLMID.draw(posX, posY, width, height, g);
            bLevel.removeFireByPixel(posX, posY);
             
 
             for (int i = 1; i <= radiusup ; i++) {
             	if(i<radiusup && bLevel.hasFireByPixel(posX, posY - i * height) ){
             	Texture.EXPLVER.draw(posX, posY - i * height, width, height, g);
            	bLevel.markForUpdateByPixel(posX, posY - radiusup * height);
            	bLevel.removeFireByPixel(posX, posY - i * height);
            	}
             	
             	else if (i==radiusup && bLevel.hasFireByPixel(posX, posY - i * height)){
             		Texture.EXPLTOP.draw(posX, posY - radiusup * height, width,height, g);
                	bLevel.removeFireByPixel(posX, posY - i * height);
                     bLevel.markForUpdateByPixel(posX, posY - radiusup * height);
             	}
             }
 
             for (int i = 1; i <= radiusdown ; i++) {
             	if(i<radiusdown && bLevel.hasFireByPixel(posX, posY + i * height) ){
                 	Texture.EXPLVER.draw(posX, posY + i * height, width, height, g);
                	bLevel.removeFireByPixel(posX, posY + i * height);
                 	bLevel.markForUpdateByPixel(posX, posY + radiusdown * height);}
                 	
                 	else if (i==radiusdown && bLevel.hasFireByPixel(posX, posY + i * height)){
                 		Texture.EXPLBOT.draw(posX, posY + radiusdown * height, width,height, g);
                		bLevel.removeFireByPixel(posX, posY + i * height);
                         bLevel.markForUpdateByPixel(posX, posY + radiusdown * height);
                 	}
             }
 
             for (int i = 1; i <= radiusleft; i++) {
             	if(i<radiusleft && bLevel.hasFireByPixel(posX-i * width, posY)){
                 	Texture.EXPLHOR.draw(posX-i* width, posY, width, height, g);
                	bLevel.removeFireByPixel(posX - i * width, posY);
                 	bLevel.markForUpdateByPixel(posX - radiusleft * width, posY);}
                 	
                 	else if (i==radiusleft && bLevel.hasFireByPixel(posX - i * width, posY)){
                 		Texture.EXPLLEF.draw(posX - radiusleft * width, posY, width,height, g);
                		bLevel.removeFireByPixel(posX - i * width, posY);
                         bLevel.markForUpdateByPixel(posX -radiusleft * width, posY);
                 	}
             }
 
             for (int i = 1; i <= radiusright; i++) {
 
             	if(i<radiusright && bLevel.hasFireByPixel(posX+i * width, posY)){
                 	Texture.EXPLHOR.draw(posX+i* width, posY, width, height, g);
                	bLevel.removeFireByPixel(posX + i * width, posY);
                 	bLevel.markForUpdateByPixel(posX + radiusright * width, posY);
 }
                 	
                 	else if (i==radiusright && bLevel.hasFireByPixel(posX + i * width, posY)){
                 		Texture.EXPLRIG.draw(posX + radiusright * width, posY, width,height, g);
                		bLevel.removeFireByPixel(posX + i * width, posY);
                         bLevel.markForUpdateByPixel(posX +radiusright * width, posY);
                 	}
             }
 
         }
         bLevel.markForUpdateByPixel(posX, posY);
     }
 /**
  * Diese Methode gibt an ob die Bombe explodiert ist.
  * @return
  */
     public boolean isExploded() {
         return state == EXPLODED;
     }
 }
