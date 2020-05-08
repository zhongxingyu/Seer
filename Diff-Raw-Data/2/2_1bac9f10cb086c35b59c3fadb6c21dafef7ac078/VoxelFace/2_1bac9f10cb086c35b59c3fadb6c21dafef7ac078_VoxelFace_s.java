 package com.vloxlands.render;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.util.Arrays;
 
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.opengl.TextureImpl;
 
 import com.vloxlands.game.Game;
 import com.vloxlands.game.world.Island;
 import com.vloxlands.settings.CFG;
 import com.vloxlands.util.Direction;
 import com.vloxlands.util.RenderAssistant;
 
 public class VoxelFace
 {
 	public Direction dir;
 	public Vector3f pos, tl, tr, bl, br, n;
 	
 	public int textureIndex;
 	public int sizeX, sizeY, sizeZ;
 	
 	public VoxelFace(Direction dir, Vector3f pos, int texInd)
 	{
 		this(dir, pos, texInd, 1, 1, 1);
 	}
 	
 	public VoxelFace(VoxelFace o)
 	{
 		sizeX = o.sizeX;
 		sizeY = o.sizeY;
 		sizeZ = o.sizeZ;
 		dir = o.dir;
 		pos = new Vector3f(o.pos);
 		textureIndex = o.textureIndex;
 		
 		updateVertices();
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
 		
		n = Vector3f.cross(br, tl, null).normalise(null);
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
 		
 		if (CFG.SHOW_WIREFRAME)
 		{
 			RenderAssistant.bindTextureAtlasTile("graphics/textures/voxelTextures.png", 7, 7);
 		}
 		else RenderAssistant.bindTextureAtlasTile("graphics/textures/voxelTextures.png", texX, texY);
 		
 		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
 		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
 		glPushMatrix();
 		{
 			glEnable(GL_BLEND);
 			
 			if (CFG.SHOW_WIREFRAME) glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
 			
 			glTranslatef(pos.x, pos.y, pos.z);
 			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
 			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
 			glBegin(GL_QUADS);
 			{
 				int[] ints = new int[] { sizeX, sizeY, sizeZ };
 				Arrays.sort(ints);
 				boolean dirVertical = dir == Direction.UP || dir == Direction.DOWN;
 				int vertical = (dirVertical) ? 0 : 1 + ((sizeY > sizeX && sizeY > sizeZ) ? -1 : 0);
 				
 				glTexCoord2d(0, 0);
 				glNormal3d(n.x, n.y, n.z);
 				glVertex3f(tl.x, tl.y, tl.z);
 				
 				if (dirVertical && sizeX > sizeZ) glTexCoord2d(ints[2], 0);
 				else glTexCoord2d(ints[1 + vertical], 0);
 				glNormal3d(n.x, n.y, n.z);
 				glVertex3f(tr.x, tr.y, tr.z);
 				
 				if (dirVertical && sizeX > sizeZ) glTexCoord2d(ints[2], ints[1]);
 				else glTexCoord2d(ints[1 + vertical], ints[2 - vertical]);
 				glNormal3d(n.x, n.y, n.z);
 				glVertex3f(br.x, br.y, br.z);
 				
 				
 				if ((dirVertical && sizeX > sizeZ)) glTexCoord2d(0, ints[1]);
 				else glTexCoord2d(0, ints[2 - vertical]);
 				glNormal3d(n.x, n.y, n.z);
 				glVertex3f(bl.x, bl.y, bl.z);
 			}
 			glEnd();
 			
 			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
 		}
 		glPopMatrix();
 		
 		TextureImpl.bindNone();
 	}
 	
 	@Override
 	public String toString()
 	{
 		return "VoxelFace[pos=" + pos.toString() + ", DIR=" + dir + ", sizeX=" + sizeX + ", sizeY=" + sizeY + ", sizeZ=" + sizeZ + ", tl=" + tl + ", tr=" + tr + ", bl=" + bl + ", br=" + br + "]";
 	}
 	
 	public double getDistanceToCamera()
 	{
 		return Vector3f.sub(Game.camera.position, pos, null).length();
 	}
 	
 	public static class VoxelFaceKey implements Comparable<VoxelFaceKey>
 	{
 		public int x, y, z, d;
 		
 		public VoxelFaceKey(int x, int y, int z, int d)
 		{
 			this.x = x;
 			this.y = y;
 			this.z = z;
 			this.d = d;
 		}
 		
 		public VoxelFaceKey(VoxelFace vf)
 		{
 			x = (int) vf.pos.x;
 			y = (int) vf.pos.y;
 			z = (int) vf.pos.z;
 			d = vf.dir.ordinal();
 		}
 		
 		@Override
 		public int hashCode()
 		{
 			return ((x * Island.SIZE + y) * Island.SIZE + z) * Island.SIZE + d;// Integer.parseInt(x + "" + y + "" + z + "" + d);
 		}
 		
 		@Override
 		public boolean equals(Object o)
 		{
 			if (!(o instanceof VoxelFaceKey)) return false;
 			
 			return hashCode() == o.hashCode();
 		}
 		
 		@Override
 		public String toString()
 		{
 			return "[" + x + ", " + y + ", " + z + ", " + Direction.values()[d] + "]";
 		}
 		
 		@Override
 		public int compareTo(VoxelFaceKey o)
 		{
 			if (x != o.x) return x - o.x;
 			else if (y != o.x) return y - o.y;
 			else if (z != o.z) return z - o.z;
 			
 			return d - o.d;
 		}
 	}
 }
