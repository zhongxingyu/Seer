 package world;
 
 import game.TextureStore;
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.opengl.Texture;
 
 import types.Vector3;
 import types.Vector3f;
 
 public class World {
 
 	// Sizes
 	private Vector3 size;
 	private Vector3f cubeSize;
 	
 	// Array containing cube data
 	private char[][][] cubes;
 	
 	// Chunks (used to render the cubes)
 	private Chunk[][][] chunks;
 	private Vector3 chunkArraySize;
 	private Vector3 chunkSize;
 	
 	// Texture
 	private Texture textures;
 	private boolean drawTextures = true;
 	
 	public World(TextureStore textureStore, Vector3f cubeSize, Vector3 chunkSize) {
 		this.cubeSize = cubeSize;
 		this.chunkSize = chunkSize;
 		
 		// Load texture
 		textures = textureStore.getTexture("res/cube_textures.png");
 	}
 	
 	public void setDrawTextures(boolean drawTextures) {
 		this.drawTextures = drawTextures;
 	}
 	
 	public void fromData(Vector3 size, char[][][] data) {
 		this.size = size;
 		cubes = data;
 		
 		// Calculate required number of chunks
 		chunkArraySize = new Vector3(((int)Math.ceil(((double)size.x) / ((double)chunkSize.x))),
 									((int)Math.ceil(((double)size.y) / ((double)chunkSize.y))),
 									((int)Math.ceil(((double)size.z) / ((double)chunkSize.z))));
 		
 		chunks = new Chunk[chunkArraySize.x][chunkArraySize.y][chunkArraySize.z];
 		
 		// Create the chunks
 		for(int x = 0; x < chunkArraySize.x; x++) {
 			for(int y = 0; y < chunkArraySize.y; y++) {
 				for(int z = 0; z < chunkArraySize.z; z++) {
 					// Make sure the chunk size does not go outside of the world size
 					Vector3 adaptedChunkSize = new Vector3(chunkSize.x, chunkSize.y, chunkSize.z);
 					
 					if(x * chunkSize.x + chunkSize.x > size.x)
 						adaptedChunkSize.x = size.x - x * chunkSize.x;
 					
 					if(y * chunkSize.y + chunkSize.y > size.y)
 						adaptedChunkSize.y = size.y - y * chunkSize.y;
 					
 					if(z * chunkSize.z + chunkSize.z > size.z)
 						adaptedChunkSize.z = size.z - z * chunkSize.z;
 					
 					// Create the chunk
 					chunks[x][y][z] = new Chunk(new Vector3(x * chunkSize.x, y * chunkSize.y, z * chunkSize.z),
 												adaptedChunkSize, cubes, size, cubeSize);
 					
 					chunks[x][y][z].setDrawTextures(drawTextures);
 					chunks[x][y][z].buildRenderData();
 				}
 			}
 		}
 	}
 	
 	public void render() {
 		if(drawTextures) {
 			GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
 			GL11.glEnable(GL11.GL_TEXTURE_2D);
 			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.getTextureID());
 		} else {
 			GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
 			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
 			GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
 		}
 		
 		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
 		GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
 
 		// CALL ALL CHUNK RENDER FUNCTIONS
 		for(int x = 0; x < chunkArraySize.x; x++) {
 			for(int y = 0; y < chunkArraySize.y; y++) {
 				for(int z = 0; z < chunkArraySize.z; z++) {
 					chunks[x][y][z].render();
 				}
 			}
 		}
 		
 		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
 		GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
 		
 		if(drawTextures) {
 			GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
 			GL11.glDisable(GL11.GL_TEXTURE_2D);
 		} else {
 			GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
 			GL11.glDisable(GL11.GL_COLOR_MATERIAL);
 		}
 	}
 	
 	public boolean solidAt(Vector3f coordinates) {
 		// Calculate cube array coordinates
 		Vector3 arrayCoords = new Vector3((int)((coordinates.x) / cubeSize.x), (int)((coordinates.y) / cubeSize.y), (int)((coordinates.z) / cubeSize.z));
 		
 		// Is this within world bounds?
 		if(arrayCoords.x >= 0 && arrayCoords.x < size.x &&
 			arrayCoords.y >= 0 && arrayCoords.y < size.y &&
 			arrayCoords.z >= 0 && arrayCoords.z < size.z) {
 			
 			if(cubes[arrayCoords.x][arrayCoords.y][arrayCoords.z] != 0)
 				return true;
 		}
 		
 		return false;
 	}
 	
 	public void setCube(Vector3 arrayCoords, char type) {
 		// Is this within world bounds?
 		if(arrayCoords.x >= 0 && arrayCoords.x < size.x &&
 			arrayCoords.y >= 0 && arrayCoords.y < size.y &&
 			arrayCoords.z >= 0 && arrayCoords.z < size.z) {
 			// Set the cube type
 			cubes[arrayCoords.x][arrayCoords.y][arrayCoords.z] = type;
 			
 			// Calculate which chunk this belongs to
 			Vector3 chunkArrayCoords = new Vector3(arrayCoords.x / chunkSize.x, arrayCoords.y / chunkSize.y, arrayCoords.z / chunkSize.z);
 			
 			// Rebuild render data for the chunk
 			chunks[chunkArrayCoords.x][chunkArrayCoords.y][chunkArrayCoords.z].buildRenderData();
 		}
 	}
 	
 	public Vector3 arrayCoordinates(Vector3f coordinates) {
 		Vector3 arrayCoords = new Vector3((int)(coordinates.x / cubeSize.x), (int)(coordinates.y / cubeSize.y), (int)(coordinates.z / cubeSize.z));
 		
 		// Is this within world bounds?
 		if(arrayCoords.x >= 0 && arrayCoords.x < size.x &&
 			arrayCoords.y >= 0 && arrayCoords.y < size.y &&
 			arrayCoords.z >= 0 && arrayCoords.z < size.z) {
 			
 			return arrayCoords;
 		}
 		
 		return null;
 	}
 }
