 package com.vloxlands.render;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import com.vloxlands.game.voxel.Voxel;
 import com.vloxlands.util.Direction;
 import com.vloxlands.util.RenderAssistant;
 
 public class VoxelFace
 {
 	Direction dir;
 	Vector3f pos;
 	int textureIndex;
 	Vector3f tl = new Vector3f(0, 1, 0), tr = new Vector3f(1, 1, 0), bl = new Vector3f(0, 0, 0), br = new Vector3f(1, 0, 0);
 	
 	public VoxelFace(Direction dir, Vector3f pos, Voxel v)
 	{
 		super();
 		this.dir = dir;
 		this.pos = pos;
 		this.textureIndex = v.getTextureIndex();
 	}
 	
 	public void render()
 	{
 		int texX = textureIndex % 32;
 		int texY = textureIndex / 32;
 		
 		double squareSize = 0.03125d;
 		
 		RenderAssistant.bindTexture("graphics/textures/voxelTextures.png");
 		
 		glPushMatrix();
 		{
 			glEnable(GL_BLEND);
 			
 			glTranslatef(pos.x, pos.y, pos.z);
 			Vector3f v = Direction.getNeededRotation(Direction.EAST, dir);
 			glTranslatef(0.5f, 0.5f, 0.5f);
 			glRotatef(v.x, 1, 0, 0);
 			glRotatef(v.y, 0, 1, 0);
 			glRotatef(v.z, 0, 0, 1);
 			glTranslatef(-0.5f, -0.5f, -0.5f);
 			
 			glBegin(GL_QUADS);
 			{
 				glTexCoord2d(texX * squareSize, (texY + 1) * squareSize);
				glNormal3d(0, 0, 1);
 				glVertex3f(tl.x, tl.y, tl.z);
 				glTexCoord2d((texX + 1) * squareSize, (texY + 1) * squareSize);
				glNormal3d(0, 0, 1);
 				glVertex3f(tr.x, tr.y, tr.z);
 				glTexCoord2d((texX + 1) * squareSize, texY * squareSize);
				glNormal3d(0, 0, 1);
 				glVertex3f(br.x, br.y, br.z);
 				glTexCoord2d(texX * squareSize, texY * squareSize);
				glNormal3d(0, 0, 1);
 				glVertex3f(bl.x, bl.y, bl.z);
 			}
 			glEnd();
 		}
 		glPopMatrix();
 	}
 }
