 package com.summ.sge.graphics.engine.lwjgl.gl11.objects;
 
 import static org.lwjgl.opengl.GL11.GL_BLEND;
 import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
 import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glBindTexture;
 import static org.lwjgl.opengl.GL11.glBlendFunc;
 import static org.lwjgl.opengl.GL11.glColor4f;
 import static org.lwjgl.opengl.GL11.glDisable;
 import static org.lwjgl.opengl.GL11.glEnable;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glRotatef;
 import static org.lwjgl.opengl.GL11.glTexCoord2f;
 import static org.lwjgl.opengl.GL11.glTranslatef;
 import static org.lwjgl.opengl.GL11.glVertex2f;
 
 import com.summ.sge.graphics.objects.Rectangle;
 import com.summ.sge.graphics.utils.ColorUtils;
 
 class LwjglRectangleGL11 extends Rectangle {
 
 	@Override
 	public void draw() {
 		glTranslatef(mPosition.getX(), mPosition.getY(), 0);
 		glRotatef(mRotation, 0f, 0f, 1f);
 		glTranslatef(-mPosition.getX(), -mPosition.getY(), 0);
 
 		glColor4f(ColorUtils.getRed(mColor), ColorUtils.getGreen(mColor), ColorUtils.getBlue(mColor), ColorUtils.getAlpha(mColor));
 
 		if (mTexture > -1) {
 			glDisable(GL_DEPTH_TEST);
 //			glEnable(GL_DEPTH_TEST);
 //			glDepthFunc(GL_LEQUAL);
 
 //			glEnable(GL_ALPHA_TEST);
 //			glAlphaFunc(GL_GEQUAL, 0);
 
 			glEnable(GL_BLEND);
 			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 
 			glEnable(GL_TEXTURE_2D);
 			glBindTexture(GL_TEXTURE_2D, mTexture);
 		}
 
 		glBegin(GL_QUADS);
 			glTexCoord2f(0.0f, 1.0f);
 			glVertex2f(mPosition.getX(),			mPosition.getY());
 
 			glTexCoord2f(1.0f, 1.0f);
 			glVertex2f(mPosition.getX() + mWidth,	mPosition.getY());
 
 			glTexCoord2f(1.0f, 0.0f);
 			glVertex2f(mPosition.getX() + mWidth,	mPosition.getY() + mHeight);
 
 			glTexCoord2f(0.0f, 0.0f);
 			glVertex2f(mPosition.getX(),			mPosition.getY() + mHeight);
 		glEnd();
 
 		glDisable(GL_BLEND);
 		glDisable(GL_TEXTURE_2D);
 	}
 }
