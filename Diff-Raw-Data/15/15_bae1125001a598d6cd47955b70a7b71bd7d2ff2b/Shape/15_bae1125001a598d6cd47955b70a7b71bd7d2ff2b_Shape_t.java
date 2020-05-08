 package com.GBE.Utilities;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL15.*;
 
 import java.nio.FloatBuffer;
 
 import org.lwjgl.BufferUtils;
 
 import com.GBE.Positions.Vertex;
 
 public class Shape
 {
 	public static final int QUAD = GL_QUADS;
 	public static final int TRIANGLE = GL_TRIANGLES;
 	public static final int LINE = GL_LINES;
 	public static final int POINT = GL_POINTS;
 	public static final int POLYGON = GL_POLYGON;
 	
 	private int shape;
 	private int vbo;
 	private int size;
 	
 	public Shape(int shape, Vertex[] vertices)
 	{
 		this.shape = shape;
 		vbo = glGenBuffers();
 
 		size = vertices.length;
 		
 		FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length * Vertex.SIZE);
 		
 		for(int i = 0; i < vertices.length; i++)
 		{
 			buffer.put(vertices[i].getPos().getX());
 			buffer.put(vertices[i].getPos().getY());
 			buffer.put(vertices[i].getPos().getZ());
 		}
 		
 		buffer.flip();
 		
 		glBindBuffer(GL_ARRAY_BUFFER, vbo);
 		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
 	}
 	
 	public void draw()
 	{
 		Texture.clear();
 		
 		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glVertexPointer(Vertex.SIZE, GL_FLOAT, 0, 0L);
 		
		glEnableClientState(GL_VERTEX_ARRAY);
 		glDrawArrays(shape, 0, size);
		glDisableClientState(GL_VERTEX_ARRAY);
 	}
 }
