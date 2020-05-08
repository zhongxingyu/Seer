 package com.mel.wallpaper.starWars.view;
 
 import com.mel.util.MathUtil;
 import com.mel.util.Point;
 import com.mel.wallpaper.starWars.entity.JediKnight;
 
 //Animacion y medidas basadas en STORM_TROOPER
 public class JediAnimator extends WalkerAnimator implements IJediAnimator{
 	
 	public static float SPRITE_WIDTH= 175f*SpriteFactory.PLAYERS_SPRITE_SCALEFACTOR;
 	public static float SPRITE_HEIGHT = 175f*SpriteFactory.PLAYERS_SPRITE_SCALEFACTOR;
 	public static float VERTICAL_CENTER = 36f*SpriteFactory.PLAYERS_SPRITE_SCALEFACTOR;
 	public static String TEXTURE_ID = SpriteFactory.STORM_TROOPER;
 	
 	
 	public JediAnimator(String textureID, float speed){
 		super(textureID, speed);
 	}
 	
 	
 	public float getSpriteOffsetX(){
 		return SPRITE_WIDTH/2;
 	}
 	public float getSpriteOffsetY(){
 		return SPRITE_HEIGHT-VERTICAL_CENTER;
 	}
 	
 	public Point getSpriteDimensions() {
 		return new Point(SPRITE_WIDTH, SPRITE_HEIGHT);
 	}
 	
 	public Point getRotationCenter(){
 		return new Point(35,35);
 	}
 	
 	//ANIMATIONS
 	public void animateDuel(JediKnight jedi, Point destination){
 		Animation a = calculateFightAnimation(jedi.getPosition().toPoint(), destination);
 		animate(a);
 	}
 
 	public void animateParry(JediKnight jedi, Point destination){
 		Animation a = calculateParryAnimation(jedi.getPosition().toPoint(), destination);
 		animate(a);
 	}
 	
 	public void animateIdleWithSword(){
 		
 	}
 	
 	
 	//ANIMATION MATHS
 	protected Animation calculateFightAnimation(Point origin, Point destination){
 		double angulo = MathUtil.getAngulo(origin.getX(), origin.getY(), destination.getX(), destination.getY());
 		
 		if(angulo>=2*MathUtil.PI_Q && angulo<5*MathUtil.PI_Q){
 			return Animation.FIGHT_W;
 		}
 		
 		if(angulo>=5*MathUtil.PI_Q && angulo<=7*MathUtil.PI_Q){
 			return Animation.FIGHT_N;
 		}
 		
 		return Animation.FIGHT_E;
 		
 	}
 	
 	protected Animation calculateParryAnimation(Point origin, Point destination){
 		double angulo = MathUtil.getAngulo(origin.getX(), origin.getY(), destination.getX(), destination.getY());
 		
 		if(angulo>=2*MathUtil.PI_Q && angulo<5*MathUtil.PI_Q){
 			return Animation.PARRY_W;
 		}
 		
 		if(angulo>=5*MathUtil.PI_Q && angulo<=7*MathUtil.PI_Q){
 			return Animation.PARRY_N;
 		}
 		
 		return Animation.PARRY_E;
 		
 	}
 	
 	
 	
 	protected void animate(Animation a){
 		if(a==null){
 			a = Animation.STOP_S;
 		}
 		
 		if(this.lastAnimation == a){
 			return;
 		}
 		
 		long tileDuration = 200;
 		switch(a) {
 			case WALK_E: //derecha
 				tileDuration =  Math.round(10000/speed);
 				sprite.animate(new long[]{tileDuration, tileDuration, tileDuration, tileDuration},new int[]{3,2,1,0}, true); //fila1
 				break;
 			case WALK_W: //izquierda
 				tileDuration =  Math.round(10000/speed);
 				sprite.animate(new long[]{tileDuration, tileDuration, tileDuration, tileDuration}, 8, 11, true);  //fila2 
 				break;
 			case WALK_N: //arriba
 				tileDuration =  Math.round(10000/speed);
 				sprite.animate(new long[]{tileDuration, tileDuration, tileDuration, tileDuration}, 16, 19, true); //fila3
 				break;
 			case WALK_S: //abajo
 				tileDuration =  Math.round(10000/speed);
 				sprite.animate(new long[]{tileDuration, tileDuration, tileDuration, tileDuration}, 24, 27, true); //fila4 
 				break;
 			case STOP_S: //abajo
 				sprite.stopAnimation(32); //fila5
 				break;
 			case STOP_N: //arriba
 				sprite.stopAnimation(34); 
 				break;
 			case STOP_W: //izquierda
 				sprite.stopAnimation(40);  //fila6 
 				break;
 			case STOP_E: //derecha
 				sprite.stopAnimation(42); 
 				break;
 			case FIGHT_W: //izquierda
 				sprite.animate(new long[]{500,100}, new int[]{49,40}, false);
 				break;
 			case FIGHT_E: //derecha
 				sprite.animate(new long[]{500,100},  new int[]{50, 42}, false);
 				break;
 			case FIGHT_N: //arriba
 				sprite.animate(new long[]{500,100},  new int[]{51, 34}, false);
 				break;
			case PARRY_W: //izquierda
				sprite.animate(new long[]{500,100}, new int[]{49,40}, false);
				break;
			case PARRY_E: //derecha
				sprite.animate(new long[]{500,100},  new int[]{50, 42}, false);
				break;
			case PARRY_N: //arriba
				sprite.animate(new long[]{500,100},  new int[]{51, 34}, false);
				break;
 				
 			case APLASTADO:
 //				if(this.textureId == SpriteFactory.MARC){
 //					sprite.animate(new long[]{200,200}, new int[]{4, 6}, true);
 //				}else{
 					//sprite.stopAnimation(MathUtils.random(4, 6)); //aqui habra que poner un random, y quizas una rotacion?
 				//}
 				break;
 			default: //parado_s
				sprite.stopAnimation(32);  //fila5
 		}
 		
 		this.lastAnimation = a;
 	}
 
 
 	
 	
 }
