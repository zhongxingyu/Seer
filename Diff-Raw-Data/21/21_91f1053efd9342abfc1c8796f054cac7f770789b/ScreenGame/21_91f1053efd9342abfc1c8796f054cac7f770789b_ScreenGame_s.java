 package com.jordan.tunnelgame;
 
 import java.util.List;
 
 import android.graphics.Color;
 import android.graphics.Paint;
 
 import com.jordan.framework.Game;
 import com.jordan.framework.Graphics;
 import com.jordan.framework.Image;
 import com.jordan.framework.Input;
 import com.jordan.framework.Input.TouchEvent;
 import com.jordan.framework.Screen;
 
 public class ScreenGame extends Screen {
 	enum GameState {
 		Ready, Running, Paused, Fail, Finish
 	}
 	GameState state = GameState.Ready;
 
     private int time;
     private int ticks;
     private int pauseTicks;
 	Paint paint;
     private LevelPack levelPack;
     private Level level;
     private float lastDeltaTime = 0.0f;
     private final int darkness = 150;
 
 	public ScreenGame(Game game, int packID, int levelNum) {
 		super(game);
         levelPack = new LevelPack(packID);
         levelPack.startOn(levelNum);
         level = levelPack.nextLevel();
 
         time = 0;
         ticks = 0;
 
         //how to animate
 		/*hanim = new Animation();
 		hanim.addFrame(heliboy, 100);
 		hanim.addFrame(heliboy2, 100);
 		hanim.addFrame(heliboy3, 100);*/
 		// Defining a paint object
 		paint = new Paint();
 		paint.setTextSize(30);
 		paint.setTextAlign(Paint.Align.CENTER);
 		paint.setAntiAlias(true);
 		paint.setColor(Color.BLUE);
 
 	}
 
 
 	private void updateRunning(List<TouchEvent> touchEvents, float deltaTime) {
         boolean pauseable = true;
         for (Orb o: level.orbs)
             if (o.coord.x>1280-C.pauseArea-10 && o.coord.y<C.pauseArea + 10) pauseable = false;
         for (TouchEvent event: touchEvents)
             if (pauseable && event.x > 1280 - C.pauseArea && event.y < C.pauseArea && event.type == TouchEvent.TOUCH_DOWN)
             {
                 pause();
                 for (Orb o: level.orbs) o.pointerID = -1;
                 return;
             }
 
         time++;
 
         state = GameRunner.update(touchEvents,deltaTime,level);
 
         //anim.update(10);
         lastDeltaTime = deltaTime;
 	}
     private void drawRunningUI() {
         Graphics g = game.getGraphics();
         GameDrawer.draw(g,level);
         g.drawImage(Assets.pauseicon, 1230,0);
         if (C.cheats)
             g.drawString(lastDeltaTime + " " + GameRunner.message, 400,30, paint);
     }
 
 
     private void updateReady(List<TouchEvent> touchEvents) {
        if (touchEvents.size() > 0 && touchEvents.get(0).type == TouchEvent.TOUCH_UP)
            state = GameState.Running;
     }
     private void drawReadyUI() {
         Graphics g = game.getGraphics();
         GameDrawer.draw(g, level);
         g.drawARGB(darkness, 0, 0, 0);
         Assets.menuByString("ready");
         g.drawImage(Assets.menu,0,0);
     }
 
     public int pauretryy = 475, paummy = 650, pauresy = 300;
 	private void updatePaused(List<TouchEvent> touchEvents) {
         for (Input.TouchEvent event: touchEvents) {
             switch(event.type)
             {
                 case Input.TouchEvent.TOUCH_DOWN:
                     if (!Assets.buttonretry.down && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,pauretryy), new Coord(990,pauretryy+150)))
                     {
                         Assets.buttonretry.down = true;
                         Assets.buttonretry.pointerID = event.pointer;
                     }
                     else if (!Assets.buttonmainmenu.down && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,paummy), new Coord(990,paummy+150)))
                     {
                         Assets.buttonmainmenu.down = true;
                         Assets.buttonmainmenu.pointerID = event.pointer;
                     }
                     else if (!Assets.buttonresume.down && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,pauresy), new Coord(990,pauresy+150)))
                     {
                         Assets.buttonresume.down = true;
                         Assets.buttonresume.pointerID = event.pointer;
                     }
                     break;
                 case Input.TouchEvent.TOUCH_DRAGGED:
                     if (event.pointer == Assets.buttonretry.pointerID && !Assets.inBounds(new Coord(event.x,event.y),new Coord(290,pauretryy), new Coord(990,pauretryy+150)))
                     {
                         Assets.buttonretry.reset();
                     }
                     else if (event.pointer == Assets.buttonmainmenu.pointerID && !Assets.inBounds(new Coord(event.x,event.y),new Coord(290,paummy), new Coord(990,paummy+150)))
                     {
                         Assets.buttonmainmenu.reset();
                     }
                     else if (event.pointer == Assets.buttonresume.pointerID && !Assets.inBounds(new Coord(event.x,event.y),new Coord(290,pauresy), new Coord(990,pauresy+150)))
                     {
                         Assets.buttonresume.reset();
                     }
                     break;
                 case Input.TouchEvent.TOUCH_UP:
                     if (event.pointer == Assets.buttonretry.pointerID && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,pauretryy), new Coord(990,pauretryy+150)))
                     {
                         Assets.buttonretry.reset();
                         Assets.buttonmainmenu.reset();
                         Assets.buttonresume.reset();
                         retry();
                     }
                     else if (event.pointer == Assets.buttonmainmenu.pointerID && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,paummy), new Coord(990,paummy+150)))
                     {
                         Assets.buttonretry.reset();
                         Assets.buttonmainmenu.reset();
                         Assets.buttonresume.reset();
                         mainMenu();
                     }
                     else if (event.pointer == Assets.buttonresume.pointerID && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,pauresy), new Coord(990,pauresy+150)))
                     {
                         Assets.buttonretry.reset();
                         Assets.buttonmainmenu.reset();
                         Assets.buttonresume.reset();
                         resume();
                     }
             }
         }
 	}
     private void drawPausedUI() {
         Graphics g = game.getGraphics();
         GameDrawer.draw(g,level);
         g.drawARGB(darkness, 0, 0, 0);
         Assets.menuByString("pause");
         g.drawImage(Assets.menu,0,0);
         g.drawImage(Assets.buttonretry.getImage(),290,pauretryy);
         g.drawImage(Assets.buttonmainmenu.getImage(),290,paummy);
         g.drawImage(Assets.buttonresume.getImage(),290,pauresy);
     }
 
 
     public int failretryy = 400, failmmy =600;
 	private void updateFail(List<TouchEvent> touchEvents) {
         for (Input.TouchEvent event: touchEvents) {
             switch(event.type)
             {
                 case Input.TouchEvent.TOUCH_DOWN:
                     if (!Assets.buttonretry.down && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,failretryy), new Coord(990,failretryy+150)))
                     {
                         Assets.buttonretry.down = true;
                         Assets.buttonretry.pointerID = event.pointer;
                     }
                     else if (!Assets.buttonmainmenu.down && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,failmmy), new Coord(990,failmmy+150)))
                     {
                         Assets.buttonmainmenu.down = true;
                         Assets.buttonmainmenu.pointerID = event.pointer;
                     }
                     break;
                 case Input.TouchEvent.TOUCH_DRAGGED:
                     if (event.pointer == Assets.buttonretry.pointerID && !Assets.inBounds(new Coord(event.x,event.y),new Coord(290,failretryy), new Coord(990,failretryy+150)))
                     {
                         Assets.buttonretry.reset();
                     }
                     else if (event.pointer == Assets.buttonmainmenu.pointerID && !Assets.inBounds(new Coord(event.x,event.y),new Coord(290,failmmy), new Coord(990,failmmy+150)))
                     {
                         Assets.buttonmainmenu.reset();
                     }
                     break;
                 case Input.TouchEvent.TOUCH_UP:
                     if (event.pointer == Assets.buttonretry.pointerID && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,failretryy), new Coord(990,failretryy+150)))
                     {
                         Assets.buttonretry.reset();
                         Assets.buttonmainmenu.reset();
                         retry();
                     }
                     else if (event.pointer == Assets.buttonmainmenu.pointerID && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,failmmy), new Coord(990,failmmy+150)))
                     {
                         Assets.buttonretry.reset();
                         Assets.buttonmainmenu.reset();
                         mainMenu();
                     }
             }
         }
     }
 	private void drawFailUI() {
 		Graphics g = game.getGraphics();
         GameDrawer.draw(g,level);
         g.drawARGB(darkness, 0, 0, 0);
         Assets.menuByString("fail");
         g.drawImage(Assets.menu,0,0);
         g.drawImage(Assets.buttonretry.getImage(),290,failretryy);
         g.drawImage(Assets.buttonmainmenu.getImage(),290,failmmy);
 	}
 
 
     public int finretryy = 475, finmmy = 650, finnly = 300;
     private void updateFinish(List<TouchEvent> touchEvents) {
         for (Input.TouchEvent event: touchEvents) {
             switch(event.type)
             {
                 case Input.TouchEvent.TOUCH_DOWN:
                     if (!Assets.buttonretry.down && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,finretryy), new Coord(990,finretryy+150)))
                     {
                         Assets.buttonretry.down = true;
                         Assets.buttonretry.pointerID = event.pointer;
                     }
                     else if (!Assets.buttonmainmenu.down && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,finmmy), new Coord(990,finmmy+150)))
                     {
                         Assets.buttonmainmenu.down = true;
                         Assets.buttonmainmenu.pointerID = event.pointer;
                     }
                     else if (!Assets.buttonnextlevel.down && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,finnly), new Coord(990,finnly+150)))
                     {
                         Assets.buttonnextlevel.down = true;
                         Assets.buttonnextlevel.pointerID = event.pointer;
                     }
                     break;
                 case Input.TouchEvent.TOUCH_DRAGGED:
                     if (event.pointer == Assets.buttonretry.pointerID && !Assets.inBounds(new Coord(event.x,event.y),new Coord(290,finretryy), new Coord(990,finretryy+150)))
                     {
                         Assets.buttonretry.reset();
                     }
                     else if (event.pointer == Assets.buttonmainmenu.pointerID && !Assets.inBounds(new Coord(event.x,event.y),new Coord(290,finmmy), new Coord(990,finmmy+150)))
                     {
                         Assets.buttonmainmenu.reset();
                     }
                     else if (event.pointer == Assets.buttonnextlevel.pointerID && !Assets.inBounds(new Coord(event.x,event.y),new Coord(290,finnly), new Coord(990,finnly+150)))
                     {
                         Assets.buttonnextlevel.reset();
                     }
                     break;
                 case Input.TouchEvent.TOUCH_UP:
                     if (event.pointer == Assets.buttonretry.pointerID && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,finretryy), new Coord(990,finretryy+150)))
                     {
                         Assets.buttonretry.reset();
                         Assets.buttonmainmenu.reset();
                         Assets.buttonnextlevel.reset();
                         retry();
                     }
                     else if (event.pointer == Assets.buttonmainmenu.pointerID && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,finmmy), new Coord(990,finmmy+150)))
                     {
                         Assets.buttonretry.reset();
                         Assets.buttonmainmenu.reset();
                         Assets.buttonnextlevel.reset();
                         mainMenu();
                     }
                     else if (event.pointer == Assets.buttonnextlevel.pointerID && Assets.inBounds(new Coord(event.x,event.y),new Coord(290,finnly), new Coord(990,finnly+150)))
                     {
                         Assets.buttonretry.reset();
                         Assets.buttonmainmenu.reset();
                         Assets.buttonnextlevel.reset();
                         nextLevel();
                     }
             }
         }
     }
     private void drawFinishUI() {
         Graphics g = game.getGraphics();
         GameDrawer.draw(g,level);
         g.drawARGB(darkness, 0, 0, 0);
         Assets.menuByString("finish");
         g.drawImage(Assets.menu,0,0);
         g.drawImage(Assets.buttonretry.getImage(),290,finretryy);
         g.drawImage(Assets.buttonmainmenu.getImage(),290,finmmy);
         g.drawImage(Assets.buttonnextlevel.getImage(),290,finnly);
     }
 
 
     @Override
     public void update(float deltaTime) {
         ticks++;
         List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
 
         if (state == GameState.Ready)
             updateReady(touchEvents);
         if (state == GameState.Running)
             updateRunning(touchEvents, deltaTime);
         if (state == GameState.Paused)
             updatePaused(touchEvents);
         if (state == GameState.Fail)
             updateFail(touchEvents);
         if (state == GameState.Finish)
             updateFinish(touchEvents);
     }
 
     @Override
     public void paint(float deltaTime) {
         if (state == GameState.Ready)
             drawReadyUI();
         if (state == GameState.Running)
             drawRunningUI();
         if (state == GameState.Paused)
             drawPausedUI();
         if (state == GameState.Fail)
             drawFailUI();
         if (state == GameState.Finish)
             drawFinishUI();
     }
 
 
     private void retry() {
         time = 0;
         ticks = 0;
         level = levelPack.thisLevel();
         state = GameState.Ready;
     }
 
     private void mainMenu() {
         game.setScreen(new ScreenLevelSelect(game,levelPack.packID));
     }
 
     private void nextLevel(){
         time = 0;
         ticks = 0;
         level = levelPack.nextLevel();
         if (level == null) mainMenu();
         state = GameState.Ready;
     }
 
 	@Override
 	public void pause() {
 		if (state == GameState.Running)
         {
 			state = GameState.Paused;
         }
 	}
 
 	@Override
 	public void resume() {
 		if (state == GameState.Paused)
 			state = GameState.Running;
 	}
 
 	@Override
 	public void dispose() {
 
 	}
 
 	@Override
 	public void backButton() {
 		pause();
 	}
 
 }
