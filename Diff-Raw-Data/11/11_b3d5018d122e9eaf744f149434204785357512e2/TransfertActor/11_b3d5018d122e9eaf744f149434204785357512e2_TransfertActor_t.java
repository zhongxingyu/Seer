 package com.tacoid.superflu.actors;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.tacoid.superflu.GameScreen;
 import com.tacoid.superflu.SuperFlu;
 import com.tacoid.superflu.entities.Transfert;
 import com.tacoid.superflu.entities.Ville;
 
 public class TransfertActor extends Actor{
 	
 	private final Transfert transfert;
 	private final TextureRegion avion;
 	
 	public TransfertActor(Transfert transfert) {
 		SuperFlu superflu = SuperFlu.getInstance();
 		
 		this.transfert = transfert;
 		Texture texAvion = superflu.manager.get("images/avion.png", Texture.class);
 		avion = new TextureRegion(texAvion, texAvion.getWidth(), texAvion.getHeight());
		width = avion.getRegionWidth();
		height = avion.getRegionHeight();
 	}
 		
 	@Override
 	public void draw(SpriteBatch batch, float arg1) {
 		int depart_x, arrivee_x;
 		Ville depart = transfert.getDepart();
 		Ville arrivee = transfert.getArrivee();
 		
 		if (depart.getX() < arrivee.getX()) {
 			depart_x = depart.getX() + (transfert.isDirect() ? 0 : 1024);
 			arrivee_x = arrivee.getX();
 		} else {
 			depart_x = depart.getX();
 			arrivee_x = arrivee.getX() + (transfert.isDirect() ? 0 : 1024);
 		}
 		double angle = (180/Math.PI) * Math.atan2(arrivee.getY() - depart.getY(), arrivee_x - depart_x);
 		double avancement = ((double)(GameScreen.getInstance().getGameLogic().getTime() - transfert.getTempsDepart())) / ((double)(transfert.getTempsArrivee() - transfert.getTempsDepart()));
 		float zoom = 0.25f + 0.75f*(float)Math.sin(avancement*Math.PI);
 
 		batch.draw(avion, 
				  (int)(depart_x*(1 - avancement) + arrivee_x*avancement) % 1024 - width / 2, 
				   (int)(depart.getY()*(1 - avancement) + arrivee.getY()*avancement - height / 2),
				   width/2,
				   height/2,
 				   avion.getRegionWidth(),
 				   avion.getRegionHeight(),
 				   zoom,
 				   zoom,
 				   (float)angle);
 		
 		//draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation)
 		/*sprite.draw( + ScreenManager.getInstance().getOrigineCarteX(), 
 				      + ScreenManager.getInstance().getOrigineCarteY(), 
 				    (float)angle, 
 				    zoom);*/
 	}
 
 	@Override
 	public Actor hit(float arg0, float arg1) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
