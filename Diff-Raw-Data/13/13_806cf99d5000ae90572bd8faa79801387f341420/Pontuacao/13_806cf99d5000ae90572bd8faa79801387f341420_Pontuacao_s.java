 package br.edu.ifpr.ThiagoRomano.Camaleao;
 
 import org.andengine.entity.sprite.TiledSprite;
 import org.andengine.entity.sprite.batch.SpriteGroup;
 import org.andengine.entity.text.Text;
 import org.andengine.opengl.texture.region.TiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 public class Pontuacao extends SpriteGroup {
 
 	GameScene mScene;
 	CamaleaotestActivity activity;
 	float time;
 	Text timeText;
 	TiledSprite mNumeros[];
 
 	public Pontuacao(int x, int y, int qtdNumeros, GameScene scene,
 			VertexBufferObjectManager mVertexBufferObjectManager) {
 		super(
 				x,
 				y,
 				CamaleaotestActivity.getSharedInstance().mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.ABOUT_ID).getTexture(), 3,
 				mVertexBufferObjectManager);
 		activity = CamaleaotestActivity.getSharedInstance();
 		this.mScene = scene;
 		time = NinjaModeScene.STARTING_TIME;
 		mNumeros = new TiledSprite[qtdNumeros];
 		TiledTextureRegion regiao = new TiledTextureRegion(
 				activity.mSpritesheetTexturePackTextureRegionLibrary.get(
 						posicoes.NUMERO0_ID).getTexture(),
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.NUMERO0_ID),
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.NUMERO1_ID),
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.NUMERO2_ID),
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.NUMERO3_ID),
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.NUMERO4_ID),
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.NUMERO5_ID),
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.NUMERO6_ID),
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.NUMERO7_ID),
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.NUMERO8_ID),
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.NUMERO9_ID));
 		
 		for (int i = 0; i < qtdNumeros; i++)
 		{
 			mNumeros[i] = new TiledSprite(46*i, 0, regiao, mVertexBufferObjectManager);
 			this.attachChild(mNumeros[i]);
 		}
 		this.setScale(0.75f);
 	}

 	public void updateScore(int score) {
 
 		for (int i = 0; i < mNumeros.length; i++) {
 			mNumeros[mNumeros.length - 1 - i]
 					.setCurrentTileIndex((int) ((score / Math.pow(10, i)) % 10));
 		}
 	}
 
 }
