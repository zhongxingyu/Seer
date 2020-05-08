 package com.zx.evildragon.stage;
 
 import info.u250.c2d.engine.Engine;
 import info.u250.c2d.graphic.C2dStage;
 
 import com.badlogic.gdx.Application;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.zx.evildragon.EvilDragon;
 import com.zx.evildragon.net.ITalk.RecognitionCallBack;
 
 public class UIStage extends C2dStage {
 
 	private final TextureAtlas atlas;
 	private final Button mButtonTalk;
 	private final Label mLabelText;
 
 	private final UIEventListener listener;
 	private final RecognitionCallBack callback;
 
 	public UIStage(UIEventListener uiEventListener) {
 		this.listener = uiEventListener;
 
 		atlas = Engine.resource("atlas");
 		mButtonTalk = new Button(new TextureRegionDrawable(atlas.findRegion("button_darkblue")), new TextureRegionDrawable(atlas.findRegion("button_green")));
 		mButtonTalk.addListener(new ClickListener() {
 
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				listener.performTalkEvent();
 				EvilDragon.talk.recognition(callback);
 			}
 
 		});
 
		LabelStyle labelStyle = new LabelStyle(EvilDragon.font, new Color(0, 1, 1, 1));
		mLabelText = new Label("this is text", labelStyle);
 		mLabelText.setPosition(300, 100);
 
 		callback = new RecognitionCallBack() {
 			@Override
 			public void before() {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void begin() {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void end(String text) {
 				Gdx.app.debug("debug", "return:" + text);
 				if (Gdx.app.getType() == Application.ApplicationType.Android) {
 					mLabelText.setText(text);
 				}
 			}
 
 		};
 
 		this.addActor(mButtonTalk);
 		this.addActor(mLabelText);
 	}
 
 	public static interface UIEventListener {
 		public void performTalkEvent();
 	}
 }
