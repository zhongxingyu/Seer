 package pro.oneredpixel.deflektorclassic;
 
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class LevelsState extends State {
 
 	int page=-1;
 	int maxpage = -1;
 	int savedUnlockedLevel = -1;
 	
 	LevelsState(Deflektor defl) {
 		super(defl);
 		// TODO Auto-generated constructor stub
 	}
 	//init state for showing
 	void start() {
 		maxpage = app.unlockedLevel/20+1;
 		if (maxpage>3) maxpage=3;
 		
 		if (savedUnlockedLevel!=app.unlockedLevel) {
 			savedUnlockedLevel=app.unlockedLevel;
 			page = app.unlockedLevel/20+1;
 			if (page>3) page=3;	
 		};
 	};
 	public boolean tap(float x, float y, int tapCount, int button) {
 		//app.gotoAppState(Deflektor.APPSTATE_GAME);
 		int ix=(int)((x-app.winX)/app.sprScale);
 		int iy=(int)((y-app.winY)/app.sprScale);
 		if ((ix>=0) && (ix<240) && (iy>=0) && (iy<160)) {
 			if ((page>1) && checkInBox(ix,iy,0,160/2-8-8,32,32)) {
 				page--;
 				app.playSound(Deflektor.SND_TAP);
 			};
 			if ((page<maxpage) && checkInBox(ix,iy,240-16-8-8-8, 160/2-8,32,32)) {
 				page++;
 				app.playSound(Deflektor.SND_TAP);
 			}
 			int lx=(ix-44)/8;
 			int ly=(iy-20)/8;
			if ( ((lx&3)!=3) && ((ly&3)!=3) && (ix>=44) && (iy>=20)) {
 				lx=lx/4; ly=ly/4;
 				int lev=ly*5+lx+(page-1)*20+1;
 				if ((lx>=0) && (lx<5) && (ly>=0) && (ly<4) && (lev<=app.unlockedLevel)) {
 					app.playingLevel = lev;
 					app.gotoAppState(Deflektor.APPSTATE_GAME);
 					app.playSound(Deflektor.SND_TAP);
 				};
 			}
 			if (checkInBox(ix,iy,8, 160-8-16,16,16)) {
 				app.gotoAppState(Deflektor.APPSTATE_MENU);
 				app.playSound(Deflektor.SND_TAP);
 			}
 		}		
 		return false;
 	}
 	
 	public boolean keyUp(int k) {
 		if (k==Keys.BACK) {
 			app.gotoAppState(Deflektor.APPSTATE_MENU);
 			app.playSound(Deflektor.SND_TAP);
 			return true;
 		};
 		return false;
 	}
 	
 	boolean checkInBox(int x,int y, int bx, int by, int bwidth, int bheight) {
 		return (x>=bx)&&(x<(bx+bwidth))&&(y>=by)&&(y<(by+bheight));
 	};
 
 	public void render(SpriteBatch batch) {
 		batch.setProjectionMatrix(app.camera.combined);
 		batch.begin();
 		
 		app.showString((240-8*14)/2, 8, "SELECT A LEVEL");
 		
 		int s=(page-1)*20+1;
 		for (int i=0;i<4;i++) {
 			for (int j=0;j<5;j++) {
 				drawLevelBox(44+j*32,20+i*32,s++);
 				if (s>app.countOfLevels) break;
 			};
 			if (s>app.countOfLevels) break;
 		};
 
 		if (page>1) app.spr_putRegion(8, 160/2-8, 16, 16, 64,160);
 		if (page<maxpage) app.spr_putRegion(240-8-16, 160/2-8, 16, 16, 80, 160);
 		
 		app.spr_putRegion(8, 160-8-16, 16, 16, 96, 160);
 		
 		batch.end();
 	};
 	
 	void drawLevelBox(int x, int y, int levelNumber) {
 		app.spr_putRegion(x, y, 24, 24, 0,32+144);
 		app.showBigNumber(x+4,y+8,levelNumber);
 		if (app.unlockedLevel<levelNumber) app.spr_putRegion(x+15, y+15, 8, 8, 48,192);
 	};
 	
 
 	
 
 
 }
