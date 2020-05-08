 package com.turlutu;
 
 
 import javax.microedition.khronos.opengles.GL10;
 
 import com.android.angle.AnglePhysicObject;
 import com.android.angle.AnglePhysicsEngine;
 import com.android.angle.AngleSurfaceView;
 import com.android.angle.AngleVector;
 import com.turlutu.Ball.Color;
 
 /**
  * L'objet ajouté en premier à cet objet sera dessiné en dernier
  * @author thomas
  *
  */
 public class MyPhysicsEngine extends AnglePhysicsEngine
 {
 	float mWorldWidth, mWorldHeight;
 	int toNewPlateform=10;
 	AngleSurfaceView mGLSurfaceView;
 	GameUI mGameUI;
 	private int mCounterScore;
 	
 	public MyPhysicsEngine(int maxObjects, float worldWidth, float worldHeight,AngleSurfaceView SurfaceView, GameUI gameUI)
 	{
 		super(maxObjects);
 		mWorldWidth = worldWidth;
 		mWorldHeight = worldHeight;
 		mGLSurfaceView = SurfaceView;
 		mGameUI = gameUI;
 	}
 
 	private void addPlateform(float decalage)
 	{
 		// TODO : cette fonction est pas térrible pour l'instant
 		float size, posX;
 		int couleur;
 		size = 85.f;
 		toNewPlateform-=(int)decalage;
 		if(toNewPlateform<0) {
 		    toNewPlateform = (int) (Math.random() * (100-25) + 25);
 			posX = (float) (Math.random() * (mWorldWidth - size)) + size / 2;
 			couleur = (int) (Math.random() * 5);
 			Plateforme newPlateforme = new Plateforme(mGLSurfaceView, size, 1,couleur);
 			newPlateforme.mPosition.set(posX,-1);
 			addObject(newPlateforme);
 		}
 	}
 	
 	
 	private void translateAll(AngleVector t)
 	{
 		addPlateform(t.mY);
 		for (int o = 0; o < mChildsCount; o++)
 		{
 			if (mChilds[o] instanceof AnglePhysicObject)
 			{
 				AnglePhysicObject mChildO = (AnglePhysicObject) mChilds[o];
 				mChildO.mPosition.add(t);
 				if(mChildO.mPosition.mY > mWorldHeight) 
 				{
 					removeObject(mChildO);
 				}
 			}
 		}
 	}
 	
 	
 	@Override
 	protected void physics(float secondsElapsed)
 	{
 		
 		for (int o = 0; o < mChildsCount; o++)
 		{
 			if (mChilds[o] instanceof Ball)
 			{
 				Ball mChildO = (Ball) mChilds[o];
				mChildO.mVelocity.mY += mChildO.mMass * mGravity.mY * secondsElapsed;
				// Je pense que cette méthode de calcul impliquera toujours un rebond lent, je vais donc tenter une autre approche
				// je garde juste le calcul de vitesse pour savoir quand arreter de monter/descendre mais dans le calcul je fige la vitesse
				mChildO.mDelta.mX = mChildO.mVelocity.mX * secondsElapsed;
				if(mChildO.mVelocity.mY > 0)
					mChildO.mDelta.mY = 10 * secondsElapsed;
				else
					mChildO.mDelta.mY = -10 * secondsElapsed;
 			}
 		}
 	}
 
 
 	@Override
 	protected void kynetics(float secondsElapsed)
 	{
 		
 		for (int o = 0; o < mChildsCount; o++)
 		{
 			if (mChilds[o] instanceof Ball)
 			{
 				Ball mChildO = (Ball) mChilds[o];
 				
 				// perdu
 				// TODO trouver où Matthieu a mis l'autre suppression de la balle dans le code (surement dans le code de ANGLE)
 				if (mChildO.mPosition.mY > mWorldHeight)
 				{
 					mChildO.delete();
 					return;
 				}
 				
 				if ((mChildO.mDelta.mX != 0) || (mChildO.mDelta.mY != 0))
 				{
 					// Changement d'état
 					mChildO.mPosition.mX += mChildO.mDelta.mX;
 					mChildO.mPosition.mY += mChildO.mDelta.mY;
 					if (mChildO.mPosition.mX > mWorldWidth)
 					{
 						mChildO.mPosition.mX = 0;
 						mChildO.changeColorRight();
 					}
 					else if  (mChildO.mPosition.mX < 0)
 					{
 						mChildO.mPosition.mX = mWorldWidth;
 						mChildO.changeColorLeft();
 					}
 
 					// translation
 					if (mChildO.mPosition.mY < mWorldHeight/2)
 					{
 						mCounterScore += (int) (mWorldHeight/2 - mChildO.mPosition.mY);
 						translateAll(new AngleVector(0,mWorldHeight/2 - mChildO.mPosition.mY));
 					}
 					// score
 					if (mChildO.mVelocity.mY > 0) // la balle descend
 					{
 						if (mCounterScore != 0)
 						{
 							mGameUI.upScore(mCounterScore);
 							mCounterScore = 0;
 						}
 					}
 					
 					// collisions
 					for (int c = 0; c < mChildsCount; c++)
 					{
 						if (c != o)
 						{
 							if (mChilds[c] instanceof Plateforme // l'objet est de type plateforme
 									&& mChildO.mVelocity.mY > 0)  // et il est entrain de descendre
 							{
 								Plateforme mChildC = (Plateforme) mChilds[c];
 								if(mChildC.mColor == Color.TOUTE || mChildC.mColor == mChildO.getColor()) // si l'objet est de la même couleure
 								{
 									if (mChildO.collide(mChildC))
 									{
 										mChildO.mPosition.mX -= mChildO.mDelta.mX;
 										mChildO.mVelocity.mY = - 8 * mChildO.mRadius; // la balle rebondit toujours de la même hauteur (simule un saut)
 										mChildC.mDelta.mX = mChildC.mVelocity.mX * secondsElapsed;
 										mChildC.mDelta.mY = mChildC.mVelocity.mY * secondsElapsed;
 										break;
 									}
 								}
 							}
 							/*else if (!(mChilds[c] instanceof Plateforme) && mChilds[c] instanceof AnglePhysicObject)
 							{
 								AnglePhysicObject mChildC = (AnglePhysicObject) mChilds[c];
 								if (mChildO.collide(mChildC))
 								{
 									mChildO.mPosition.mX -= mChildO.mDelta.mX;
 									mChildO.mPosition.mY -= mChildO.mDelta.mY;
 									mChildC.mDelta.mX = mChildC.mVelocity.mX * secondsElapsed;
 									mChildC.mDelta.mY = mChildC.mVelocity.mY * secondsElapsed;
 									break;
 								}
 							}*/
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void step(float secondsElapsed)
 	{
 		if (secondsElapsed > 0.08)
 			secondsElapsed = (float) 0.04;
 		
 		super.step(secondsElapsed);
 	}
 
 	/**
 	 * @author thomas
 	 */
 	@Override
 	public void draw(GL10 gl)
 	{
 		for (int t=1;t<mChildsCount;t++)
 			mChilds[t].draw(gl);
 		mChilds[0].draw(gl); // balle dessiné en dernier
 	}
 	
 }
