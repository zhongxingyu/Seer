 /*
  * Copyright 2011, Erik Lund
  *
  * This file is part of Voxicity.
  *
  *  Voxicity is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Voxicity is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Voxicity.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package voxicity;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.glu.GLU;
 
 public class Renderer
 {
 	public static int quads = 0;
 	public static int draw_calls = 0;
 	public static int batch_draw_calls = 0;
 
 	Config config;
 	Map< ChunkID, ChunkNode > chunks = new HashMap< ChunkID, ChunkNode >();
 
 	Frustum camera = new Frustum();
 
 	public Renderer( Config config )
 	{
 		this.config = config;
 	}
 
 	public void set_chunk( int x, int y, int z, Chunk chunk )
 	{
 		if ( chunk == null )
 			return;
 
 		ChunkNode node = new ChunkNode( chunk );
 		node.set_pos( x, y, z );
 
 		chunks.put( new ChunkID( x, y, z ), node );
 	}
 
 	public void render()
 	{
 		boolean cleaned_one = false;
 		quads = 0;
 		draw_calls = 0;
 		batch_draw_calls = 0;
 
 		GL11.glLoadIdentity();
 		GLU.gluLookAt( camera.pos.x, camera.pos.y, camera.pos.z, camera.pos.x + camera.look.x, camera.pos.y + camera.look.y, camera.pos.z  + camera.look.z, 0,1,0 );
 
 
 		// Clear the screen and depth buffer
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 
 		
 		System.out.println( "Before clean " + Time.get_time_µs() );
 		for ( ChunkNode chunk : chunks.values() )
 //			if ( !cleaned_one )
 				cleaned_one = chunk.clean();
 
 		System.out.println( "Before render " + Time.get_time_µs() );
 		for ( ChunkNode chunk : chunks.values() )
 			chunk.render( camera );
 
 		System.out.println( "After render " + Time.get_time_µs() );
 
 		TextRenderer.draw( "FPS: " + Integer.toString( Voxicity.fps), 5, 5 + TextRenderer.line_height() * 0 );
 		TextRenderer.draw( "X: " + Float.toString(camera.pos.x), 5, 5 + TextRenderer.line_height() * 1 );
 		TextRenderer.draw( "Y: " + Float.toString(camera.pos.y), 5, 5 + TextRenderer.line_height() * 2 );
 		TextRenderer.draw( "Z: " + Float.toString(camera.pos.z), 5, 5 + TextRenderer.line_height() * 3 );
 		TextRenderer.draw( "Verts: " + Integer.toString(quads * 4), 5, 5 + TextRenderer.line_height() * 4 );
 		TextRenderer.draw( "Tris: " + Integer.toString(quads * 2), 5, 5 + TextRenderer.line_height() * 5 );
 		TextRenderer.draw( "Render chunks: " + Integer.toString(draw_calls) + "/" + chunks.size(), 5, 5 + TextRenderer.line_height() * 6 );
 		TextRenderer.draw( "Render batches: " + Integer.toString(batch_draw_calls), 5, 5 + TextRenderer.line_height() * 7 );
 
 		int[] chunk = Coord.GlobalToChunk( Math.round( camera.pos.x), Math.round( camera.pos.y ), Math.round( camera.pos.z ) );
 
 		TextRenderer.draw( "Chunk X Y Z: " + chunk[0] + " " + chunk[1] + " " + chunk[2], 5, 5 + TextRenderer.line_height() * 8 );
 
 		Display.update();
 		cleaned_one = false;
 	}
 }
