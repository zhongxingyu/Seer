 package com.utc.graphemobile.element;
 
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.utc.graphemobile.utils.Utils;
 
 public class TextButton extends Group {
 
	private final int size = 40;
	private final int margin = 10;
 	private TextureRegion tr;
 	private Label l;
 
 	public TextButton(String name, String title, TextureRegion imageTR, Skin s) {
 		this.tr = imageTR;
 		this.setName(name);
 		l = new Label(title, s);
		l.setFontScale(Utils.toDp(0.7f));
 		l.setHeight(Utils.toDp(size + margin));
 		l.setPosition(Utils.toDp(margin + size), 0);
 		l.setTouchable(Touchable.disabled);
 		this.addActor(l);
 		Image image = new Image(tr);
 		image.setWidth(Utils.toDp(size));
 		image.setHeight(Utils.toDp(size));
 		image.setX(0);
 		image.setY(0);
 		image.setTouchable(Touchable.disabled);
 		this.addActor(image);
 		this.setWidth(Utils.toDp(LeftMenu.WIDTH));
 		this.setHeight(Utils.toDp(size));
 	}
 
 	public void setText(String newText) {
 		l.setText(newText);
 		l.setHeight(Utils.toDp(size + margin));
 		l.setPosition(Utils.toDp(margin + size), 0);
 	}
 }
