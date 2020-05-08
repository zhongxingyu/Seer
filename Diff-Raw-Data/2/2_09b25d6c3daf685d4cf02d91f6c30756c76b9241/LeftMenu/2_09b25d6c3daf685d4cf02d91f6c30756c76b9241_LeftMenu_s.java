 package com.utc.graphemobile.element;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Interpolation;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.utc.graphemobile.input.ShowLeftMenuEventListener;
 import com.utc.graphemobile.input.UIEventListener;
 import com.utc.graphemobile.screen.IGrapheScreen;
 import com.utc.graphemobile.utils.Utils;
 
 public class LeftMenu extends Table {
 	public static final float WIDTH = 200;
 	public static final float PADDING = 10;
 	public static final float CLOSE_SIZE = 50;
 	private Image img;
 	private IGrapheScreen screen = null;
 	private Table table;
 
 	public LeftMenu(IGrapheScreen screen) {
 		this.screen = screen;
 		this.table = new Table();
 
 		UIEventListener listener = new UIEventListener(screen);
 		this.top().left();
 		table.top().left();
 
 		TextureRegion deleteTR = screen.getSkin().getRegion("seeMore");
 		img = new Image(deleteTR);
 
 		img.setHeight(Utils.toDp(CLOSE_SIZE));
 		img.setWidth(Utils.toDp(CLOSE_SIZE));
 		img.setPosition(Utils.toDp(WIDTH),
 				Gdx.graphics.getHeight() - img.getHeight());
 		img.addListener(new ShowLeftMenuEventListener(this));
 		this.addActor(img);
 
 		TextButton bt = new TextButton("open", "Open", screen.getSkin()
 				.getRegion("open"), screen.getSkin());
 		bt.addListener(listener);
 		table.add(bt).pad(Utils.toDp(PADDING)).left()
 				.width(Utils.toDp(WIDTH - PADDING * 2));
 		table.row();
 
 		bt = new TextButton("center", "Center", screen.getSkin().getRegion(
 				"center"), screen.getSkin());
 		bt.addListener(listener);
 		table.add(bt).pad(Utils.toDp(PADDING)).left()
 				.width(Utils.toDp(WIDTH - PADDING * 2));
 		table.row();
 
		bt = new TextButton("spatial", "Spatialisation", screen.getSkin()
 				.getRegion("spatial"), screen.getSkin());
 		bt.addListener(listener);
 		table.add(bt).pad(Utils.toDp(PADDING)).left()
 				.width(Utils.toDp(WIDTH - PADDING * 2));
 		table.row();
 
 		bt = new TextButton("label", "Label", screen.getSkin().getRegion(
 				"label"), screen.getSkin());
 		bt.addListener(listener);
 		table.add(bt).pad(Utils.toDp(PADDING)).left()
 				.width(Utils.toDp(WIDTH - PADDING * 2));
 		table.row();
 
 		bt = new TextButton("edit", "Normal", screen.getSkin().getRegion("edit"),
 				screen.getSkin());
 		bt.addListener(listener);
 		table.add(bt).pad(Utils.toDp(PADDING)).left()
 				.width(Utils.toDp(WIDTH - PADDING * 2));
 		table.row();
 
 		bt = new TextButton("edge", "Type edge", screen.getSkin().getRegion(
 				"edge"), screen.getSkin());
 		bt.addListener(listener);
 		table.add(bt).pad(Utils.toDp(PADDING)).left()
 				.width(Utils.toDp(WIDTH - PADDING * 2));
 		table.row();
 
 		bt = new TextButton("about", "About", screen.getSkin().getRegion(
 				"about"), screen.getSkin());
 		bt.addListener(listener);
 		table.add(bt).pad(Utils.toDp(PADDING)).left()
 				.width(Utils.toDp(WIDTH - PADDING * 2));
 
 		this.setWidth(Utils.toDp(WIDTH));
 		this.setHeight(Gdx.graphics.getHeight());
 
 		setBackground(screen.getSkin().getDrawable("gray-pixel"));
 
 		this.setX(Utils.toDp(-WIDTH));
 		table.setX(Utils.toDp(-WIDTH));
 
 		this.add(table);
 	}
 
 	public void onResize() {
 		setHeight(Gdx.graphics.getHeight());
 
 		invalidate();
 		img.setY(getHeight() - img.getHeight());
 	}
 
 	public boolean pan(float x, float y, float deltaX, float deltaY) {
 		if(this.getHeight() > table.getHeight()) return true;
 		table.setY(table.getY() - deltaY);
 		return false;
 	}
 
 	public boolean containsX(float x) {
 		return this.getX() < x && this.getX() + this.getWidth() > x;
 	}
 
 	public boolean containsY(float y) {
 		return this.getY() < y && this.getY() + this.getHeight() > y;
 	}
 
 	public boolean contains(float x, float y) {
 		return containsX(x) && containsY(y);
 	}
 
 	public boolean fling(float velocityX, float velocityY, int button) {
 		if(this.getHeight() > table.getHeight()) return true;
 		if(velocityY > 0) {
 			table.addAction(Actions.moveTo(table.getX(), this.getHeight() - table.getHeight(), (float)(0.4f + Math.log10(velocityY) / 10),
 					Interpolation.fade));
 		} else if(velocityY != 0) {
 			table.addAction(Actions.moveTo(table.getX(), 0, (float)(0.4f + Math.log10(-velocityY) / 10), Interpolation.fade));
 		}
 		return false;
 	}
 }
