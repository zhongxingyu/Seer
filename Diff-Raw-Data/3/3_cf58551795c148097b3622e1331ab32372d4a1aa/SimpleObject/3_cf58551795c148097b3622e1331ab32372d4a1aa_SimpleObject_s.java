 package jp.yahei.lwjgl;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 /**
  * Pȕ`\IuWFNg̒ۃNX<br>
  * ʒuƌ̂3`̕`Ɋւ@\<br>
  * @author Yahei
  *
  */
 public abstract class SimpleObject implements Drawable {
 	// `ɕKvȈʒuƎp̏
 	double x,y,z;
 	float[] rotMatrix;
 	// ]s̓_CNgobt@ɓēnKv邽߁ApӂĂ
 	FloatBuffer fb;
 	{
 		ByteBuffer bb = ByteBuffer.allocateDirect(16*4);
 		bb.order(ByteOrder.nativeOrder());
 		fb = bb.asFloatBuffer();
 	}
 	
 	/**
 	 * `s<br>
 	 * ł͉]Esړ̏SA
 	 * ̓Iȕ``́ATuNXŒ`drawGeometry()Ō肳<br>
 	 */
 	@Override
 	public void draw(){
 		glPushMatrix(); {
			glMatrixMode(GL_MODELVIEW);
 			glTranslated(x, y, z);
 			fb.put(rotMatrix);
 			fb.flip();
 			glMultMatrix(fb);
 			drawGeometry();
 		} glPopMatrix();
 	}
 	
 	/**
 	 * ̂̍̕Wݒ<br>
 	 */
 	public void setCoordinate(double x, double y, double z){
 		this.x = x;
 		this.y = y;
 		this.z = z;
 	}
 	
 	/**
 	 * ̂̌̕ݒ<br>
 	 */
 	public void setAngle(float[] rotMatrix){
 		this.rotMatrix = rotMatrix;
 	}
 
 	/**
 	 * _zuĎۂɕ`悵Ă<br>
 	 * TuNXŃI[o[Ch<br>
 	 */
 	abstract protected void drawGeometry();
 }
 
 
 
 
 
 
 
 
 
 
 
 
