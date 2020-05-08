 package GameObjects;
 
 import java.util.Random;
 
 import Game.MainGame;
 import GameComponents.ObjectRenderer.Shape;
 import GameComponents.RigidBody.ForceMode;
 import GameComponents.Transform;
 import Maths.Vector2;
 
 public class ParticleEffects extends GameObject
 {
 	public boolean isTurnedOn = false;
 	public Transform transform;
 
 	private Particles[] particleArray;
 
 	public ParticleEffects(Transform transform,int maxParticles)
 	{
 		this.transform = transform;
 
 		particleArray = new Particles[maxParticles];
 	}
 
 	public void Update()
 	{
 		Random ran = new Random();
		Vector2 back = transform.LocalDirectionToWorld(new Vector2(0,-1)).Normalized();
 		int shipLength = 15;
 		Vector2 particlePos = transform.LocalPositionToWorld(new Vector2(0,-shipLength));
 		for(int i = 0; i < particleArray.length; i++)
 		{
 			if( particleArray[i] == null )
 			{
 				if(ran.nextInt(1000) % 217 == 0 )
 				{
 					if( isTurnedOn )
 					{	
 						particleArray[i] = new Particles(ran.nextInt(1500)+3000, particlePos);
 						particleArray[i].objectRenderer.shape= Shape.Square;
 						particleArray[i].objectRenderer.SetTexture("smoke");
 						particleArray[i].rigidBody.frictionCoefficient = 0.01f;
 						particleArray[i].rigidBody.PushForce(new Vector2((ran.nextInt(20))*15*back.x,(ran.nextInt(20))*50*back.y),ForceMode.Impulse);
 						particleArray[i].transform.size = new Vector2((1+ran.nextInt(2)) - 0.075f*ran.nextInt(50));
 						particleArray[i].rigidBody.PushTorque((ran.nextInt(20) -10) * 10, ForceMode.Impulse);
 						particleArray[i].objectRenderer.opacity = 0.8f;
 					}
 				}
 			}
 			else
 			{
 				if( particleArray[i].TimeToDie() )
 				{
 					particleArray[i].Delete();
 					particleArray[i] = null;
 				}
 				else
 				{
 					particleArray[i].Update();
 				}
 			}
 		}
 	}
 
 	public void TurnOn()
 	{
 		if( !isTurnedOn )
 		{
 			isTurnedOn = true;
 		}
 	}
 
 	public void TurnOff()
 	{
 		if( isTurnedOn )
 		{
 			isTurnedOn = false;
 		}
 	}
 
 
 }
