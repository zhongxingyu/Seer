 package com.vloxlands.render;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.util.Arrays;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import com.vloxlands.game.Game;
 import com.vloxlands.util.Direction;
 import com.vloxlands.util.RenderAssistant;
 
 public class VoxelFace
 {
 	Direction dir;
 	Vector3f pos;
 	int textureIndex;
 	Vector3f tl, tr, bl, br;
 	
 	int sizeX, sizeY, sizeZ;
 	
 	public VoxelFace(Direction dir, Vector3f pos, int texInd)
 	{
 		this(dir, pos, texInd, 1, 1, 1);
 	}
 	
 	public VoxelFace(Direction dir, Vector3f pos, int texInd, int sizeX, int sizeY, int sizeZ)
 	{
 		super();
 		this.dir = dir;
 		this.pos = pos;
 		textureIndex = texInd;
 		setSize(sizeX, sizeY, sizeZ);
 	}
 	
 	public void setSize(int sizeX, int sizeY, int sizeZ)
 	{
 		this.sizeX = sizeX;
 		this.sizeY = sizeY;
 		this.sizeZ = sizeZ;
 		
 		updateVertices();
 	}
 	
 	public void updateVertices()
 	{
 		tl = new Vector3f(0, sizeY, 0);
 		tr = new Vector3f(sizeX, sizeY, 0);
 		bl = new Vector3f(0, 0, 0);
 		br = new Vector3f(sizeX, 0, 0);
 		switch (dir)
 		{
 			case NORTH:
 			{
 				tl.x = sizeX;
 				bl.x = sizeX;
 				
 				tr.z = sizeZ;
 				br.z = sizeZ;
 				
 				break;
 			}
 			case SOUTH:
 			{
 				tl.z = sizeZ;
 				bl.z = sizeZ;
 				
 				tr.x = 0;
 				br.x = 0;
 				
 				break;
 			}
 			case WEST:
 			{
 				tl.z = sizeZ;
 				bl.z = sizeZ;
 				tr.z = sizeZ;
 				br.z = sizeZ;
 				
				tl.x = sizeX;
				bl.x = sizeX;
				tr.x = 0;
				br.x = 0;
 				
 				break;
 			}
 			case UP:
 			{
 				tl.z = sizeZ;
 				tr.z = sizeZ;
 				
 				bl.y = sizeY;
 				br.y = sizeY;
 				break;
 			}
 			case DOWN:
 			{
 				tl.y = 0;
 				tr.y = 0;
 				
 				bl.z = sizeZ;
 				br.z = sizeZ;
 				break;
 			}
 			default:
 				break;
 		}
 	}
 	
 	public void increaseSize(int sizeX, int sizeY, int sizeZ)
 	{
 		setSize(this.sizeX + sizeX, this.sizeY + sizeY, this.sizeZ + sizeZ);
 	}
 	
 	public void render()
 	{
 		int texX = textureIndex % 32;
 		int texY = textureIndex / 32;
 		
		glEnable(GL_CULL_FACE);
 		RenderAssistant.bindTextureAtlasTile("graphics/textures/voxelTextures.png", texX, texY);
 		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
 		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
 		
 		glPushMatrix();
 		{
 			glEnable(GL_BLEND);
 			
 			glTranslatef(pos.x, pos.y, pos.z);
 			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
 			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
 			glBegin(GL_QUADS);
 			{
 				int[] ints = new int[] { sizeX, sizeY, sizeZ };
 				Arrays.sort(ints);
 				int vertical = (dir == Direction.UP || dir == Direction.DOWN) ? 0 : 1;
 				
 				glTexCoord2d(0, ints[2 - vertical]);
 				glNormal3d(0, 0, -1);
 				glVertex3f(tl.x, tl.y, tl.z);
 				
 				glTexCoord2d(ints[1 + vertical], ints[2 - vertical]);
 				glNormal3d(0, 0, -1);
 				glVertex3f(tr.x, tr.y, tr.z);
 				
 				glTexCoord2d(ints[1 + vertical], 0);
 				glNormal3d(0, 0, -1);
 				glVertex3f(br.x, br.y, br.z);
 				
 				glTexCoord2d(0, 0);
 				glNormal3d(0, 0, -1);
 				glVertex3f(bl.x, bl.y, bl.z);
 			}
 			glEnd();
 		}
 		glPopMatrix();
 	}
 	
 	public double getDistanceToCamera()
 	{
 		return Vector3f.sub(Game.currentGame.camera.position, pos, null).length();
 	}
 }
