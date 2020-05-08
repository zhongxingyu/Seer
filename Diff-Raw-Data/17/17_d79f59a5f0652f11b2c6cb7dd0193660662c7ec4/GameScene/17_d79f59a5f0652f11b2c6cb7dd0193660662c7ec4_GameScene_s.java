 package br.edu.ifpr.ThiagoRomano.Camaleao;
 
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.Sprite;
 
 public class GameScene extends Scene {
 
 	public int colors;
 	public boolean iniciando = true;
 	public Scene mConfirmExit;
 	public PauseMenu mMenuScene;
 	public Scene mConfirmRestart;
 	Sprite mBlackBehind;
 
 	public void ChangeColors(float fValor, int id) {
 
 	}
 
 	public void restart() {
 	}
 
 	public void clearChildScenes() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void toggleEscuro(boolean bool) {
 		if (bool)
 		{
 			mBlackBehind.setVisible(true);
	
 		}else{
 			mBlackBehind.setVisible(false);
 		}
 	}
 }
