 package com.tacoid.puyopuyo.actors;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.tacoid.puyopuyo.PuyoPuyo;
 import com.tacoid.puyopuyo.logic.Coord;
 import com.tacoid.puyopuyo.logic.Falling;
 import com.tacoid.puyopuyo.logic.GameLogic;
 import com.tacoid.puyopuyo.logic.State;
 
 public class GridActor extends Actor {
 
 	private TextureRegion[] boules = new TextureRegion[5];
 	private TextureRegion[] boules_f = new TextureRegion[5];
 	private TextureRegion[] boules_fall = new TextureRegion[5];
 	private TextureRegion[] boules_h = new TextureRegion[5];
 	private TextureRegion[] boules_v = new TextureRegion[5];
 	private Sprite panel;
 	private PuyoPuyo puyopuyo;
 	private GameLogic logic;
 	private int origX;
 	private int origY;
 	private TextureRegion white;
 	private int size;
 	private int sizePuyo;
 	private int offWhite;
 
 	/**
 	 * 
 	 * @param logic
 	 * @param origX
 	 * @param origY
 	 * @param size Taille d'une colonne.
 	 * @param sizePuyo Taille d'un puyo
 	 */
 	public GridActor(GameLogic logic, int origX, int origY, int size, int sizePuyo) {
 		this.origX = origX;
 		this.origY = origY;
 		this.logic = logic;
 		this.size = size;
 		this.sizePuyo = sizePuyo;
 		this.offWhite = (size - sizePuyo) / 2;
 		puyopuyo = PuyoPuyo.getInstance();
 		
 		// TODO: changer nom fichiers pour intégrer taille.
 		boules[0] = puyopuyo.atlasPuyo.findRegion("green_happy-48");
 		boules[1] = puyopuyo.atlasPuyo.findRegion("yellow_happy-48");
 		boules[2] = puyopuyo.atlasPuyo.findRegion("red_happy-48");
 		boules[3] = puyopuyo.atlasPuyo.findRegion("blue_happy-48");
 		boules[4] = puyopuyo.atlasPuyo.findRegion("nuisance");
 		
 		boules_f[0] = puyopuyo.atlasPuyo.findRegion("green_sleep-48");
 		boules_f[1] = puyopuyo.atlasPuyo.findRegion("yellow_sleep-48");
 		boules_f[2] = puyopuyo.atlasPuyo.findRegion("red_sleep-48");
 		boules_f[3] = puyopuyo.atlasPuyo.findRegion("blue_sleep-48");
 		boules_f[4] = puyopuyo.atlasPuyo.findRegion("nuisance");
 		
 		boules_fall[0] = puyopuyo.atlasPuyo.findRegion("green_happy-48");
 		boules_fall[1] = puyopuyo.atlasPuyo.findRegion("yellow_happy-48");
 		boules_fall[2] = puyopuyo.atlasPuyo.findRegion("red_fall-48");
 		boules_fall[3] = puyopuyo.atlasPuyo.findRegion("blue_happy-48");
 		boules_fall[4] = puyopuyo.atlasPuyo.findRegion("nuisance");
 		
		boules_h[0] = puyopuyo.atlasPuyo.findRegion("vert_horizontal");
		boules_h[1] = puyopuyo.atlasPuyo.findRegion("jaune_horizontal");
		boules_h[2] = puyopuyo.atlasPuyo.findRegion("rouge_horizontal");
		boules_h[3] = puyopuyo.atlasPuyo.findRegion("bleu_horizontal");
 
		boules_v[0] = puyopuyo.atlasPuyo.findRegion("vert_vertical");
		boules_v[1] = puyopuyo.atlasPuyo.findRegion("jaune_vertical");
		boules_v[2] = puyopuyo.atlasPuyo.findRegion("rouge_vertical");
		boules_v[3] = puyopuyo.atlasPuyo.findRegion("bleu_vertical");
 		
 		white = puyopuyo.atlasPuyo.findRegion("white");
 	}
 
 	@Override
 	public void draw(SpriteBatch batch, float alpha) {
 		int[][] grid = logic.getGrid();
 		
 		for (int l = 0; l < logic.LINES - 1; l++) {
 			for (int c = 0; c < logic.COLUMNS - 1; c++) {
 				if (grid[l][c] > 0 && grid[l][c] != logic.GARBAGE) {
 					if (grid[l][c] == grid[l][c+1]) {
 						batch.draw(boules_h[grid[l][c] - 1], c * size + origX + size / 2, l * size + origY);
 					}
 					if (grid[l][c] == grid[l+1][c]) {
 						batch.draw(boules_v[grid[l][c] - 1], c * size + origX, l * size + origY + size / 2);
 					}
 				}
 				
 			}
 		}
 		
 		for (int l = 0; l < logic.LINES; l++) {
 			for (int c = 0; c < logic.COLUMNS; c++) {
 				if (grid[l][c] > 0) {
 					batch.draw(boules_f[grid[l][c] - 1], c * (size + 1) + origX,
 							l * size + origY);
 				}
 			}
 		}
 
 		if (logic.getState() != State.LOST) {
 			Coord[] next = logic.getPiece();
 			if (next != null) {
 				if (next[0].coul > 0 && next[0].l < logic.LINES) {
 					batch.draw(white, next[0].c * (size + 1) + origX - offWhite, 
 							next[0].l * size + origY - offWhite);
 					batch.draw(boules[next[0].coul - 1],
 							next[0].c * (size + 1) + origX, next[0].l * size + origY);
 				}
 				if (next[1].coul > 0 && next[1].l < logic.LINES) {
 					batch.draw(boules[next[1].coul - 1],
 							next[1].c * (size + 1) + origX, next[1].l * size + origY);
 				}
 			}
 		}
 
 		if (logic.getState() == State.GRAVITY) {
 			if (logic.getFallings() != null) {
 				for (Falling f : logic.getFallings()) {
 					Coord end = f.getEnd();
 					batch.draw(boules_fall[end.coul - 1], end.c * (size + 1) + origX, end.l
 							* size + f.getRemaining() * size + origY);
 				}
 			}
 		}
 	}
 
 	@Override
 	public Actor hit(float arg0, float arg1) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
