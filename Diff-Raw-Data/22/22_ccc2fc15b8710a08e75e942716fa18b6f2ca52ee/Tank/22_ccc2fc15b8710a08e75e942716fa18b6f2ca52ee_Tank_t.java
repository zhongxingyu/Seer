 package game;
 
 import org.newdawn.slick.SlickException;
 
 
 public class Tank extends CollidableRenderableObject{
 	public Tank() throws SlickException{
 		super("res/Sherman Tank Sprite.png",CollidableRenderableObject.Physics.Circular);
		SetPosition(0.5f,0.5f);
 		resolve = false;
 	}
 	
 	public void Shoot(){
 		if(timer<=0f){
 		
 			try {
 				//System.out.print("test");
 				Bullet bul = new Bullet();
 				//bul.SetPosition(0.5f,0.5f);
 				//System.out.print(rectangle.getCenterY());
 				bul.SetCenterPosition(GetCenterPosition());
 				//bul.SetCenterPosition(GetCenterPosition());//rectangle.getCenterX(),rectangle.getCenterY());//this.GetPosition());
 				float rotation = (float) Math.toRadians(GetRotation());
 				float tankx = (float) (Math.sin(rotation));
 				float tanky = -(float) (Math.cos(rotation));
 				bul.SetVelocity(tankx,tanky);
 				bul.image.rotate(GetRotation());
 			} catch (SlickException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			timer = 0.5f;
 		}
 	}
 	
 	
 	public void MoveTank(float x){
 		//SetVelocity()
 		//float hip = 0.4f * x;
 
 		float rotation = (float) Math.toRadians(GetRotation());
 		
 		float tankx = (float) (x * Math.sin(rotation));
 		float tanky = (float) (x * Math.cos(rotation));
 		SetVelocity(tankx,-tanky);
 	}
 	float timer = 0f;
 	float immune = 3f;
 	@Override
 	public void Update(float deltaTime){
 		if(timer > 0f){
 			timer -= deltaTime;
 		}
 		if(immune > 0f){
 			immune -= deltaTime;
 		}
 		super.Update(deltaTime);
 		SetVelocity(0f,0f);
 		angleSpeed =0f;
 		
 	}
 	@Override
 	public void Explode(){
 		if(immune > 0f)
 			return;
 		
 		super.Explode();
 		
 	}
 	public void RotateTank(float r){
 		angleSpeed = r;
 	}
 	
 	public void isDestroyed(){
 		this.Destroy();
 	}
 	
 }
