 package com.turlutu;
 
 
 import javax.microedition.khronos.opengles.GL10;
 
 import com.android.angle.AnglePhysicObject;
 import com.android.angle.AngleSegmentCollider;
 import com.android.angle.AngleSprite;
 import com.android.angle.AngleSpriteLayout;
 import com.android.angle.AngleSurfaceView;
 import com.turlutu.Ball.Color;
 
 public class Plateforme extends AnglePhysicObject
 {
 	private AngleSprite mSprite;
 	private float mWidth;
 	protected Color mColor;
 
 	/**
 	 * 
 	 * @param layout sprite
 	 * @param width width of the plateform
 	 * @param bounce Coefficient of restitution (1 return all the energy) 
 	 */
 	public Plateforme(AngleSpriteLayout layout, float width, float bounce)
 	{
 		super(1, 0); // Note : super (nb_segment, nb_circle);
 		mSprite=new AngleSprite(layout);
 		mWidth = width;
 		float w = width/2;
 		addSegmentCollider(new PlateformeCollider(-w,w, 0)); // haut
 		//addSegmentCollider(new AngleSegmentCollider(-w, 0, w, 0)); // haut
 		mMass = 0;
 		mBounce = bounce;
 		mColor = Color.TOUTE;
 	}
 	
 	public Plateforme(AngleSurfaceView view, float width, float bounce, int color)
 	{
 		super(1, 0); // Note : super (nb_segment, nb_circle);
		// TODO ici c'est moche
 		mSprite = new AngleSprite(new AngleSpriteLayout(view, 128, 32, com.turlutu.R.drawable.ball, 0, 0, 256, 64));
 		mWidth = width;
 		float w = width/2;
		addSegmentCollider(new AngleSegmentCollider(-w, 0, w, 0)); // haut
 		mMass = 0;
 		mBounce = bounce;
 		if(color==1)
 			mColor = Color.ROUGE;
 		else if(color==2)
 			mColor = Color.BLEU;
 		else if(color==3)
 			mColor = Color.VERT;
 		else
 			mColor = Color.TOUTE;
 	}
 
 	/**
 	 * @return surface of box
 	 */
 	@Override
 	public float getSurface()
 	{
 		return mWidth; 
 	}
 
 	/**
 	 * Draw sprite and/or colliders
 	 */
 	@Override
 	public void draw(GL10 gl)
 	{
 		mSprite.mPosition.set(mPosition);
 		//mSprite.draw(gl);
 		if(mColor == Color.ROUGE)
 			drawColliders(gl,1f,0f,0f);
 		else if(mColor ==  Color.BLEU)
 			drawColliders(gl,0f,0f,1f);
 		else if(mColor == Color.VERT)
 			drawColliders(gl,0f,1f,0f);
 		else
 			drawColliders(gl,1f,1f,1f);
 		
 	}
 	
 }
