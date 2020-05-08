 package br.edu.ifpr.ThiagoRomano.Camaleao;
 
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.DelayModifier;
 import org.andengine.entity.modifier.FadeInModifier;
 import org.andengine.entity.modifier.MoveModifier;
 import org.andengine.entity.modifier.SequenceEntityModifier;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.util.modifier.ease.EaseElasticOut;
 
 public class MainMenuScene extends MenuScene implements
 		IOnMenuItemClickListener {
 	CamaleaotestActivity activity;
 
 	Sprite mBackground;
 	Sprite mChameleon;
 	Sprite mTabua;
 	Sprite mTitle;
 
 	IMenuItem mNinjaModeButton;
 	IMenuItem mArcadeModeButton;
 	IMenuItem mAbout;
 	IMenuItem mCredits;
 
 	final int MENU_ARCADE = 0;
 	final int MENU_NINJA = 1;
 	final int MENU_ABOUT = 2;
 	final int MENU_CREDITS = 3;
 
 	public MainMenuScene() {
 
 		activity = CamaleaotestActivity.getSharedInstance();
 		this.mCamera = activity.mCamera;
 
 		mNinjaModeButton = new SpriteMenuItem(MENU_NINJA,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.NINJA_ID),
 				activity.getVertexBufferObjectManager());
 
 		mArcadeModeButton = new SpriteMenuItem(MENU_ARCADE,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.ARCADE_ID),
 				activity.getVertexBufferObjectManager());
 		mArcadeModeButton.setPosition(76, 30);
 
 		mAbout = new SpriteMenuItem(MENU_ABOUT,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.ABOUT_ID),
 				activity.getVertexBufferObjectManager());
 		mAbout.setPosition(101, 217);
 
 		mCredits = new SpriteMenuItem(MENU_CREDITS,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.CREDITS_ID),
 				activity.getVertexBufferObjectManager());
 		mCredits.setPosition(73, 309);
 
 		mBackground = new Sprite(0, 0,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.MENU_BG_ID),
 				activity.getVertexBufferObjectManager());
 
 		mChameleon = new Sprite(277, 256,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.CHAMELEON_ID),
 				activity.getVertexBufferObjectManager());
 		mChameleon.setAlpha(0f);
 
 		mTabua = new Sprite(64, 350,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.TABUA_ID),
 				activity.getVertexBufferObjectManager()) {
 			@Override
 			public void setPosition(float pX, float pY) {
 				super.setPosition(pX, pY);
 				mNinjaModeButton.setPosition(this.mX + 32, this.mY + 122);
 				mArcadeModeButton.setPosition(this.mX + 76, this.mY + 30);
 				mAbout.setPosition(this.mX + 101, this.mY + 217);
 				mCredits.setPosition(this.mX + 73, this.mY + 309);
 			}
 		};
 		mTabua.registerEntityModifier(new MoveModifier(2f, 64, 64, 800, 350,
 				EaseElasticOut.getInstance()) {
 			@Override
 			protected void onModifierFinished(IEntity pItem) {
 				// TODO Auto-generated method stub
 				super.onModifierFinished(pItem);
 				mChameleon.registerEntityModifier(new SequenceEntityModifier(
 						new DelayModifier(0.5f), new FadeInModifier(3f)));
 			}
 		});
 
 		mTitle = new Sprite(28, 36,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.TITLE_ID),
 				activity.getVertexBufferObjectManager());
 
 		attachChild(mBackground);
 		attachChild(mTabua);
 		addMenuItem(mCredits);
 		addMenuItem(mNinjaModeButton);
 		addMenuItem(mArcadeModeButton);
 		addMenuItem(mAbout);
 		attachChild(mTitle);
 		attachChild(mChameleon);
 
 		setBackgroundEnabled(false);
 		setOnMenuItemClickListener(this);
 
 	}
 
 	@Override
 	public boolean onMenuItemClicked(MenuScene arg0, IMenuItem arg1,
 			float arg2, float arg3) {
 		switch (arg1.getID()) {
 		case MENU_NINJA:
 			activity.setCurrentScene(new NinjaModeScene());
 			return true;
 		case MENU_ARCADE: {
 			activity.setCurrentScene(new ArcadeModeScene());
 			return true;
 		}
 		default:
 			break;
 
 		}
 		return false;
 	}

 }
