 package com.senselessweb.android.universeofpictures.scene;
 
 import com.threed.jpct.Matrix;
 import com.threed.jpct.Object3D;
 import com.threed.jpct.Primitives;
 
 public class Picture extends Object3D
 {
 	private static final int pictureScale = 4;
 
 	private static final int pictureDistance = 2 * pictureScale + 2;
 
 	private static final long serialVersionUID = 9018072087016918351L;
 
 	public Picture(final RenderableScene parent, final int orbitIndex, final float angle)
 	{
 		super(Primitives.getBox(pictureScale, 0.01f));
 		parent.addObject(this);
 
 		final Object3D plane = Primitives.getPlane(pictureScale * 2, 1f);
 		parent.addObject(plane);
 
 		plane.setTexture("testfoto");
 		plane.rotateX((float) (Math.PI / 2.0));
 		plane.rotateY((float) (Math.PI / 4.0));
 		plane.translate(0, -0.1f, 0);
 		this.addChild(plane);
 
 		this.setTexture("testfoto");
		this.rotateY((float) (-Math.PI / 4.0));
 		
 		final Matrix m = new Matrix();
 		m.translate(160 + orbitIndex * pictureDistance, 0, 0);
 		m.rotateY(angle);
 
 		this.setTranslationMatrix(m);
 		
 
 	}
 }
