 package org.globalgamejam.strat;
 
 import java.io.IOException;
 
 import android.util.Log;
 
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
 	private static int[] posiX, //= { -84, +128, +64, -80, -228, -292 }, 
 						posiY ;//= {-184, -248, -372, -460, -372, -248 };
 	public static String PATH_IMG = "img/";
 	
 	Texture texAvatar, texBonus, texLifeBar, texBlockBar;
 	private Sprite[] bonus, avatars, lifeBar, blockBar;
 	private Sprite bg, bgWait, cursor;
 	private SpriteBatch batch, batch2;
 	public static int w, h;
 	
 	private Communication com;
 	
 	//Interface
 		private int selected = -1;
 		
 
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
 		cursor = new Sprite(new Texture(PATH_IMG + "cursor1.png"));
 		bgWait = new Sprite(new Texture(PATH_IMG + "bgWait.png"));
 
 		allocTextures();
 		loadTextures();
 		
 		setPositions();
 	}
 
 	public void render() {
 		int nbPa = com.getActions();
 		int nbBlock = com.getStones();
 		int monId = com.getId();
 		int i, select = -1;
 		
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		if (!com.isConnected()) {
 			batch.begin();
 			bgWait.draw(batch);
 			batch.end();
 			return;
 		}
 
 		if(com.getStatus()!=0) // beark caca !
 			return;
 		
 		
 		if(Gdx.input.isTouched())
 		{
 			int x = Gdx.input.getX(), y = Gdx.input.getY();
 			//select = -1;
 			//Log.i("colision", "collision au " + "x " + x + " y : " + y);
 			
 			for(i=0; i< NB_JOUEURS;i++)
 			{
 				if(collision(x, h-y, avatars[i].getX(), avatars[i].getY(),avatars[i].getWidth(), avatars[i].getHeight()))
 				{
 					select = i;
 					//Log.i("colision", "collision avec  " + select);
 					//Log.i("coordonnees", "x : " + avatars[select].getX() + "y : " + avatars[select].getY() +
 					//					 "w : "+ avatars[select].getWidth() + "h : " + avatars[select].getHeight());
 					
 				}
 			}
 			
 			if(select != -1 && select != selected)
 			{
 				if(selected == -1)
 				{
 					selected=select;
 					Log.i("setSelected", " new selection : " + selected);
 				}
 				else
 				{
 					Log.i("setSelect", " new selection : " + selected);
 					
 					if(selected == monId)
 					{
 						Log.i("action", "transfert de pierre du joueur vers " + select);
 						com.giveStone(select);
 					}
 					else if(select == monId)
 					{
 						Log.i("action", "transfert de pierre vers le joueur de la part de " + select);
						com.stealStone(select);
 					}
 					else
 					{
 						Log.i("action", "action spéciale de " + selected + "vers " + select);
 					}
 					selected=-1;
 				}
 			}
 			
 		}
 		
 		batch.begin();
 		bg.draw(batch);
 		
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
 			avatars[posiToId(i, monId)].setPosition(posiX[i], posiY[i]);
 			avatars[posiToId(i, monId)].setScale(0.8f);
 			avatars[posiToId(i, monId)].draw(batch2);
 		}
 		
 		if(selected != -1)
 		{
 			cursor.setPosition(posiX[idToPosi(selected, monId)], posiY[idToPosi(selected, monId)]);
 			cursor.draw(batch2);
 			//Log.d("lulz", "selected : " + selected + " select : " + select);
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
