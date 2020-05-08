 /*
 * Strat-game PC-Server :
  * 
  * Code : Frédéric Meslin, Florent Touchard, Benjamin Blois
  * Released on 29/01/12 at 15h00 for the 4th GGJ
  * 
  * This program and all its resources included is
  * published under the Creative common license By-NC 3.0.
  * 
  * For more informations :
  * http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package org.globalgamejam.strat;
 
 import java.io.IOException;
 
 import android.util.Log;
 import android.webkit.WebChromeClient.CustomViewCallback;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 
 public class GameRenderer implements ApplicationListener {
 
 	private static final int NB_JOUEURS = 6, NB_BONUS = 5, NB_SPRITE_LIFEBAR = 2, NB_SPRITE_BLOCKBAR = 7;
 	private static final int NB_BLOCK_MAX = 10;
 	public static String PATH_IMG = "img/";
 	
 	Texture texAvatar, texBonus, texLifeBar, texBlockBar;
 	private Sprite[] bonus, avatars, lifeBar, blockBar;
 	private Sprite bg, bgWait, bgWin, bgLost, bgDeco, cursor, cursorBonus;
 	private SpriteBatch batch, batch2;
 	
 	
 	
 	
 	private Communication com;
 	
 	//Interface
 		private int selected = -1, select = -1;
 		private int[] posiX, posiY ;
 		public int w, h, monId, bonusId;
 		private int bonusposition = 0, bonusSelected=-1;
 		
 		
 
 	public GameRenderer(String host, int port) throws IOException {
 		com = new Communication(host, port);
 		com.start();
 	}
 
 	public void create() {
 		
 		w = Gdx.graphics.getWidth();
 		h = Gdx.graphics.getHeight(); // helps the placement of the avatars
 
 		batch = new SpriteBatch();
 		batch2 = new SpriteBatch();
 
 		bg = new Sprite(new Texture(PATH_IMG + "bgPhone.png"));
 		bgWin = new Sprite(new Texture(PATH_IMG + "bgWin.png"));
 		bgLost = new Sprite(new Texture(PATH_IMG + "bgLost.png"));
 		bgDeco = new Sprite(new Texture(PATH_IMG + "bgDeco.png"));
 		bgWait = new Sprite(new Texture(PATH_IMG + "bgWait.png"));
 		
 		cursor = new Sprite(new Texture(PATH_IMG + "cursor.png"));
 		cursorBonus = new Sprite(new Texture(PATH_IMG + "cursorBonus.png"));
 
 		allocTextures();
 		loadTextures();
 		
 		setPositions();
 	}
 
 	public void render() {
 		int nbPa = com.getActions();
 		int nbBlock = com.getStones();
 		int i;
 		
 		monId = com.getId();
 		bonusId = com.getBonus();
 		
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		if (!com.isConnected()) {
 			batch.begin();
 			bgWait.draw(batch);
 			batch.end();
 			return;
 		}
 
 		switch(com.getStatus()) {
 			case Communication.STATUS_WIN:
 				batch.begin();
 				bgWin.draw(batch);
 				batch.end();
 				return;
 			case Communication.STATUS_LOST:
 				batch.begin();
 				bgLost.draw(batch);
 				batch.end();
 				return;
 			case Communication.STATUS_DECO:
 				batch.begin();
 				bgDeco.draw(batch);
 				batch.end();
 				return;
 		}	
 
 		if(Gdx.input.justTouched())
 			touchProcessing();
 			
 		
 		batch.begin();
 		bg.draw(batch);
 		
 		if (bonusId >= 0 && bonusId < bonus.length) {
 			if (bonusposition < 0) bonusposition = (int) ((Math.random() * (Gdx.graphics.getHeight()- 64 - 20))) + 10;
 			bonus[bonusId].setPosition(w-64-10, bonusposition);
 			if(bonusSelected != -1)
 			{
 				cursorBonus.setPosition(w-64-10, bonusposition);
 				cursorBonus.draw(batch);
 			}
 			bonus[bonusId].draw(batch);
 		} else bonusposition = -100;
 		
 		/* LIFEBAR */
 		for (i = 0; i < nbPa - 1; i++) {
 			lifeBar[0].setPosition(i * (lifeBar[0].getWidth()),
 					Gdx.graphics.getHeight() - lifeBar[0].getHeight());
 			lifeBar[0].draw(batch);
 		}
 		lifeBar[1].setPosition((nbPa - 1) * (lifeBar[1].getWidth()),
 				Gdx.graphics.getHeight() - lifeBar[1].getHeight());
 		lifeBar[1].draw(batch);
 		/* LIFEBAR */
 
 		/* BLOCKS */
 		for (i = 0; i < nbBlock; i++) {
 			blockBar[monId]
 					.setPosition(10, 5 + i * (blockBar[monId].getHeight() + 5));
 			blockBar[monId].draw(batch);
 		}
 		for (; i < NB_BLOCK_MAX; i++) {
 			blockBar[NB_SPRITE_BLOCKBAR-1].setPosition(10, 5 + i * (blockBar[0].getHeight() + 5));
 			blockBar[NB_SPRITE_BLOCKBAR-1].draw(batch);
 		}
 		/* FIN BLOCK */
 		batch.end();
 
 		batch2.begin();
 		avatars[monId].setPosition(posiX[0], posiY[0]);
 		avatars[monId].draw(batch2);
 		for (i = 1; i < NB_JOUEURS; i++) {
 			//Log.d("isAlive", "id : " + posiToId(i, monId) + " is alive : " + com.isAlive(posiToId(i, monId)));
 			if(com.isAlive(posiToId(i, monId))) {
 				avatars[posiToId(i, monId)].setPosition(posiX[i], posiY[i]);
 				avatars[posiToId(i, monId)].setScale(0.8f);
 				avatars[posiToId(i, monId)].draw(batch2);
 			}
 		}
 		
 		if(selected != -1)
 		{
 			cursor.setPosition(posiX[idToPosi(selected, monId)], posiY[idToPosi(selected, monId)]);
 			cursor.draw(batch2);
 			Log.d("lulz", "selected : " + selected + " select : " + select);
 		}
 		batch2.end();
 		
 
 	}
 	
 	private void setPositions() {
 		int padding = 20;
 		
 		posiX = new int[6];
 		posiY = new int[6];
 		
 		posiX[0] = (int) ((w - bonus[0].getWidth() - blockBar[0].getWidth()) / 2) ;//- avatars[0].getWidth()/2);
 		posiX[1] = (int) (w - bonus[1].getWidth() - avatars[1].getWidth() - padding);
 		posiX[2] = (int) (posiX[1] - avatars[1].getWidth()/2 - padding);
 		posiX[3] = posiX[0];
 		posiX[4] = (int) (posiX[5] + avatars[0].getWidth() + 2*padding);
 		posiX[5] = (int) (blockBar[1].getWidth() + padding);
 
 		posiY[0] = (int) (h - lifeBar[0].getHeight() - avatars[0].getHeight() - padding);
 		posiY[1] = posiY[0] - padding;
 		posiY[2] = (int) (h/2 - avatars[0].getHeight()/2 - 2*padding);
 		posiY[3] = padding;
 		posiY[4] = posiY[2];
 		posiY[5] = posiY[1];
 	}
 	
 		
 	private void allocTextures() {
 		texAvatar = new Texture(PATH_IMG + "avatar.png");
 		texBonus = new Texture(PATH_IMG + "bonus.png");
 		texLifeBar = new Texture(PATH_IMG + "lifebar.png");
 		texBlockBar = new Texture(PATH_IMG + "blockbar.png");
 	}
 	
 	private void desallocTextures() {
 		texAvatar.dispose();
 		texBonus.dispose();
 		texLifeBar.dispose();
 		texBlockBar.dispose();
 	}
 	
 	private void loadTextures() {
 		bonus = new Sprite[NB_BONUS];
 		avatars = new Sprite[NB_JOUEURS];
 		lifeBar = new Sprite[NB_SPRITE_LIFEBAR];
 		blockBar = new Sprite[NB_SPRITE_BLOCKBAR];
 
 		for (int i = 0; i < NB_JOUEURS; i++)
 			avatars[i] = new Sprite(new TextureRegion(texAvatar, (i % 3) * 128,
 					(i / 3) * 128, 128, 128));
 
 		for (int i = 0; i < NB_BONUS; i++)
 			bonus[i] = new Sprite(new TextureRegion(texBonus, (i % 2) * 64,
 					(i / 2) * 64, 64, 64));
 
 		for (int i = 0; i < NB_SPRITE_LIFEBAR; i++)
 			lifeBar[i] = new Sprite(new TextureRegion(texLifeBar, i * 64, 0,
 					64, 32));
 
 		for (int i = 0; i < NB_SPRITE_BLOCKBAR; i++)
 			blockBar[i] = new Sprite(new TextureRegion(texBlockBar,
 					(i % 4) * 64, (i / 4) * 32, 64, 32));
 	}
 	
 	private void touchProcessing() {
 		int x = Gdx.input.getX(), y = Gdx.input.getY();
 		
 		select = -1;
 		
 		//Log.i("colision", "collision au " + "x " + x + " y : " + y);
 		
 		if(bonusId>=0 && bonusId<NB_BONUS)
 			if(collision(x, h-y, bonus[bonusId].getX(), bonus[bonusId].getY(), bonus[bonusId].getWidth(), bonus[bonusId].getHeight())) {
 				bonusSelected = bonusId;
 				selected = -1;
 				return;
 			}
 		
 		for(int i=0; i< NB_JOUEURS;i++)
 		{
 			if(collision(x, h-y, avatars[i].getX(), avatars[i].getY(),avatars[i].getWidth(), avatars[i].getHeight()))
 				select = i;			
 		}
 		
 		if(select == -1 || select == selected) {//if nothing or the last selected avatar is selected, nothing to do
 			selected = -1;
 			bonusSelected = -1;
 			return;
 		}
 		
 		Log.i("setSelected", " new selection : " + selected);
 		
 		if(selected == -1) { // if nothing was previously selected
 			if(bonusSelected!=-1) {
 				com.useBonus(bonusSelected, monId, select);
 			}
 			else
 				selected=select;
 			
 			return;
 		}
 		
 		if(selected == monId) // if the first selected avatar was the player's one
 		{
 			Log.i("action", "transfert de pierre du joueur vers " + select);
 			com.giveStone(select);
 		}
 		else if(select == monId) // if the last selected avatar is the player's one
 		{
 			Log.i("action", "transfert de pierre vers le joueur de la part de " + select);
 			com.stealStone(selected);
 		}
 		else
 		{
 			Log.i("action", "action spéciale de " + selected + "vers " + select);
 		}
 		
 		selected = -1;		
 	}
 
 	private boolean collision(int pX, int pY, float cX, float cY, float cW, float cH) {
 		if(pX<cX)
 			return false;
 		if(pX > cX+cW)
 			return false;
 		if(pY<cY)
 			return false;
 		if(pY > cY+cH)
 			return false;
 		
 		return true;
 	}
 	
 	private int idToPosi(int id, int monId) {
 		return (id-monId+NB_JOUEURS)%NB_JOUEURS;
 	}
 	
 	private int posiToId(int posi, int monId) {
 		return (posi + monId)%NB_JOUEURS;
 	}
 	
 	public void dispose() {
 		desallocTextures();
 	}
 	
 	public void pause()  {
 		desallocTextures();
 	}
 	public void resize(int arg0, int arg1)  {
 		allocTextures();
 		loadTextures();
 		render();
 		
 	}
 	public void resume()  {
 		allocTextures();
 		loadTextures();
 		render();
 	}
 }
