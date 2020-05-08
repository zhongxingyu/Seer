 package br.edu.ifpr.ThiagoRomano.Camaleao;
 
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.texture.region.ITextureRegion;
 
 import android.view.KeyEvent;
 
 public class ArcadeLevelSelect extends MenuScene implements
 		IOnMenuItemClickListener {
 
 	static final int MENU_LEVEL_1 = 0;
 	static final int MENU_LEVEL_2 = MENU_LEVEL_1 + 1;
 	static final int MENU_LEVEL_3 = MENU_LEVEL_2 + 1;
 	static final int MENU_LEVEL_4 = MENU_LEVEL_3 + 1;
 	static final int MENU_LEVEL_5 = MENU_LEVEL_4 + 1;
 	static final int MENU_BACK = MENU_LEVEL_5 + 1;
 
 	CamaleaotestActivity activity;
 	Sprite mBackground;
 	SpriteMenuItem[] mLevelCross = new SpriteMenuItem[5];
 
 	Sprite mCaminho[] = new Sprite[4];
 	Sprite mUnopen[] = new Sprite[9];
 	private SpriteMenuItem mBack;
 
 	public ArcadeLevelSelect() {
 		activity = CamaleaotestActivity.getSharedInstance();
 		this.mCamera = activity.mCamera;
 		mBackground = new Sprite(0, 0,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.MUNDO01_ID),
 				activity.getVertexBufferObjectManager());
 		this.attachChild(mBackground);
 
 		/*
 		 * TiledTextureRegion crossTexture = new TiledTextureRegion(
 		 * activity.mSpritesheetTexturePackTextureRegionLibrary.get(0)
 		 * .getTexture(), activity.mSpritesheetTexturePackTextureRegionLibrary
 		 * .get(posicoes.X_DISABLED_ID),
 		 * activity.mSpritesheetTexturePackTextureRegionLibrary
 		 * .get(posicoes.X_ATIVO_ID));
 		 */
 		ITextureRegion crossTexture = activity.mSpritesheetTexturePackTextureRegionLibrary
 				.get(posicoes.X_ATIVO_ID);
 		mLevelCross[0] = new SpriteMenuItem(MENU_LEVEL_1, crossTexture,
 				activity.getVertexBufferObjectManager());
 		mLevelCross[0].setPosition(332, 87);
 
 		mLevelCross[1] = new SpriteMenuItem(MENU_LEVEL_2, crossTexture,
 				activity.getVertexBufferObjectManager());
 		mLevelCross[1].setPosition(286f, 351f);
 
 		mLevelCross[2] = new SpriteMenuItem(MENU_LEVEL_3, crossTexture,
 				activity.getVertexBufferObjectManager());
 		mLevelCross[2].setPosition(51f, 319f);
 
 		mLevelCross[3] = new SpriteMenuItem(MENU_LEVEL_4, crossTexture,
 				activity.getVertexBufferObjectManager());
 		mLevelCross[3].setPosition(330f, 571f);
 
 		mLevelCross[4] = new SpriteMenuItem(MENU_LEVEL_5, crossTexture,
 				activity.getVertexBufferObjectManager());
 		mLevelCross[4].setPosition(138f, 593f);
 		for (int i = activity.mLevel; i < 5; i++) {
 			mLevelCross[i].setVisible(false);
 		}
 
 		mUnopen[0] = new Sprite(332, 87,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.X_DISABLED_ID),
 				activity.getVertexBufferObjectManager());
 		mUnopen[1] = new Sprite(286, 351,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.X_DISABLED_ID),
 				activity.getVertexBufferObjectManager());
 		mUnopen[2] = new Sprite(51, 319,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.X_DISABLED_ID),
 				activity.getVertexBufferObjectManager());
 		mUnopen[3] = new Sprite(330, 571,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.X_DISABLED_ID),
 				activity.getVertexBufferObjectManager());
 		mUnopen[4] = new Sprite(138, 593,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.X_DISABLED_ID),
 				activity.getVertexBufferObjectManager());
 
 		mUnopen[5] = new Sprite(326, 152,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.CAMINHO1_DISABLED_ID),
 				activity.getVertexBufferObjectManager());
 		mUnopen[6] = new Sprite(136, 336,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.CAMINHO2_DISABLED_ID),
 				activity.getVertexBufferObjectManager());
 		mUnopen[7] = new Sprite(122, 397,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.CAMINHO3_DISABLED_ID),
 				activity.getVertexBufferObjectManager());
 		mUnopen[8] = new Sprite(229, 603,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.CAMINHO4_DISABLED_ID),
 				activity.getVertexBufferObjectManager());
 
 		mCaminho[0] = new Sprite(326, 152,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.CAMINHO1_ID),
 				activity.getVertexBufferObjectManager());
 		mCaminho[1] = new Sprite(136, 336,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.CAMINHO2_ID),
 				activity.getVertexBufferObjectManager());
 		mCaminho[2] = new Sprite(122, 397,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.CAMINHO3_ID),
 				activity.getVertexBufferObjectManager());
 		mCaminho[3] = new Sprite(229, 603,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.CAMINHO4_ID),
 				activity.getVertexBufferObjectManager());
 		for (int i = 0; i < mCaminho.length; i++) {
 			if (mLevelCross[i + 1].isVisible())
 				continue;
 			mCaminho[i].setVisible(false);
 		}
 
 		mBack = new SpriteMenuItem(MENU_BACK,
 				activity.mSpritesheetTexturePackTextureRegionLibrary2
 						.get(posicoes2.BACK_ID),
 				activity.getVertexBufferObjectManager());
 		mBack.setPosition(375, 678);
 
 		this.attachChild(mUnopen[0]);
 		this.attachChild(mUnopen[1]);
 		this.attachChild(mUnopen[2]);
 		this.attachChild(mUnopen[3]);
 		this.attachChild(mUnopen[4]);
 		this.attachChild(mUnopen[5]);
 		this.attachChild(mUnopen[6]);
 		this.attachChild(mUnopen[7]);
 		this.attachChild(mUnopen[8]);
 
 		this.addMenuItem(mLevelCross[0]);
 		this.addMenuItem(mLevelCross[1]);
 		this.addMenuItem(mLevelCross[2]);
 		this.addMenuItem(mLevelCross[3]);
 		this.addMenuItem(mLevelCross[4]);
 		this.attachChild(mCaminho[0]);
 		this.attachChild(mCaminho[1]);
 		this.attachChild(mCaminho[2]);
 		this.attachChild(mCaminho[3]);
 
 		this.addMenuItem(mBack);
 		setOnMenuItemClickListener(this);
 	}
 
 	@Override
 	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
 			float pMenuItemLocalX, float pMenuItemLocalY) {
 		switch (pMenuItem.getID()) {
 		case MENU_LEVEL_1:
 			activity.setCurrentScene(new ArcadeModeScene(1, new int[] {
 
 			255, 255, 0,
 
 			0, 255, 0,
 
 			255, 0, 0,
 
 			255, 0, 255,
 
 			0, 0, 255,
 
 			255, 255, 255, }));
 			return true;
 		case MENU_LEVEL_2:
 			activity.setCurrentScene(new ArcadeModeScene(2, new int[] {
 
 			0, 255, 255,
 
 			0, 135, 255,
 
 			0, 135, 60,
 
 			135, 135, 60,
 
 			0, 150, 150,
 
 			150, 150, 150,
 
 			150, 255, 255, }) {
 				@Override
 				public void DiferenciadorDeFases() {
 					// TODO Auto-generated method stub
 					super.DiferenciadorDeFases();
 					final Sprite diferenciadorDeFases = new Sprite(
 							11,
 							235,
 							activity.mSpritesheetTexturePackTextureRegionLibrary
 									.get(posicoes.FASE2_ID), activity
 									.getVertexBufferObjectManager());
 					attachChild(diferenciadorDeFases);
 				}
 			});
 			return true;
 		case MENU_LEVEL_3:
 			activity.setCurrentScene(new ArcadeModeScene(3, new int[] {
 
 			0, 120, 120,
 
 			255, 120, 120,
 
 			75, 120, 120,
 
 			75, 120, 0,
 
 			120, 60, 0,
 
 			0, 0, 0, }) {
 				@Override
 				public void DiferenciadorDeFases() {
 					// TODO Auto-generated method stub
 					super.DiferenciadorDeFases();
 					final Sprite diferenciadorDeFases = new Sprite(
 							0,
 							0,
 							activity.mSpritesheetTexturePackTextureRegionLibrary
 									.get(posicoes.FASE3_ID), activity
 									.getVertexBufferObjectManager());
 					attachChild(diferenciadorDeFases);
 				}
 			});
 			return true;
 		case MENU_LEVEL_4:
 			activity.setCurrentScene(new ArcadeModeScene(4, new int[] {
 
 			180, 0, 0,
 
 			180, 0, 75,
 
 			255, 75, 0,
 
 			255, 75, 255,
 
 			105, 165, 0,
 
 			105, 0, 0, }) {
 				@Override
 				public void DiferenciadorDeFases() {
 					// TODO Auto-generated method stub
 					super.DiferenciadorDeFases();
 					final Sprite diferenciadorDeFases = new Sprite(
 							0,
 							158,
 							activity.mSpritesheetTexturePackTextureRegionLibrary
 									.get(posicoes.FASE2_ID), activity
 									.getVertexBufferObjectManager());
 					attachChild(diferenciadorDeFases);
 				}
 			});
 			return true;
 		case MENU_LEVEL_5:
 			activity.setCurrentScene(new ArcadeModeScene(5, new int[] {
 
 			105, 45, 0,
 
 			255, 210, 150,
 
 			165, 135, 90,
 
 			165, 60, 60,
 
 			180, 45, 0,
 
 			255, 210, 165, }) {
 				@Override
 				public void DiferenciadorDeFases() {
 					// TODO Auto-generated method stub
 					super.DiferenciadorDeFases();
 					final Sprite diferenciadorDeFases = new Sprite(
 							0,
 							250,
 							activity.mSpritesheetTexturePackTextureRegionLibrary
									.get(posicoes.FASE2_ID), activity
 									.getVertexBufferObjectManager());
 					attachChild(diferenciadorDeFases);
 				}
 			});
 			return true;
 		case MENU_BACK:
 			activity.setCurrentScene(new MainMenuScene());
 			return true;
 		default:
 			return false;
 		}
 
 	}
 
 	@Override
 	public boolean handleKeyDown(int pKeyCode, KeyEvent pEvent) {
 		if (pKeyCode == KeyEvent.KEYCODE_BACK
 				&& pEvent.getAction() == KeyEvent.ACTION_DOWN) {
 			activity.setCurrentScene(new MainMenuScene());
 			return true;
 		}
 		return false;
 	}
 }
