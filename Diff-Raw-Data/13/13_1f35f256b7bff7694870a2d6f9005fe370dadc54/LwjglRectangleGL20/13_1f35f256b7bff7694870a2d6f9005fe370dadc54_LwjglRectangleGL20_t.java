 package com.summ.sge.graphics.engine.lwjgl.gl20.objects;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
 import static org.lwjgl.opengl.GL13.glActiveTexture;
 import static org.lwjgl.opengl.GL20.*;
 
 import com.summ.sge.graphics.engine.lwjgl.common.utils.LwjglProgramLoader;
 import com.summ.sge.graphics.objects.Rectangle;
 import com.summ.sge.graphics.utils.ColorUtils;
 
 public class LwjglRectangleGL20 extends Rectangle {
 
 	private static final int VERTICES_COUNT = 4;
 	private static final int VERTEX_SIZE = 2;
 
 	@Override
 	public void draw() {
		glLoadIdentity();
 
		int program = LwjglProgramLoader.getDefaultProgram();
 		glUseProgram(program);
 
 		glTranslatef(mPosition.getX(), mPosition.getY(), 0);
 		glRotatef(mRotation, 0f, 0f, 1f);
 		glTranslatef(-mPosition.getX(), -mPosition.getY(), 0);
 
 		glColor4f(ColorUtils.getRed(mColor), ColorUtils.getGreen(mColor), ColorUtils.getBlue(mColor), ColorUtils.getAlpha(mColor));
 
 		int textureHandler = glGetUniformLocation(program, "sTexture");
 		int useTextureHandler = glGetUniformLocation(program, "useTexture");
 
 		if (mTexture > -1) {
 			glEnable(GL_BLEND);
 			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 
 			glActiveTexture(GL_TEXTURE0);
 			glBindTexture(GL_TEXTURE_2D, mTexture);
 
 			glUniform1i(textureHandler, 0);
 			glUniform1i(useTextureHandler, 1);
 		} else {
 			glUniform1i(useTextureHandler, 0);
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
 
 		glUseProgram(0);
 		glDisable(GL_BLEND);
 	}
 }
