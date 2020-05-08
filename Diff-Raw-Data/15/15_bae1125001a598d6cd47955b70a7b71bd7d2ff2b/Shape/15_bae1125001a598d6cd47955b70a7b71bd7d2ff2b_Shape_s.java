 package com.GBE.Utilities;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
 
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
 	}
 	
 	public void draw()
 	{
 		Texture.clear();
 		
		glEnableVertexAttribArray(0);
		
 		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, Vertex.SIZE * 4, 0);
 		
 		glDrawArrays(shape, 0, size);
		
		glDisableVertexAttribArray(0);
 	}
 }
