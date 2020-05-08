 package com.chromabits.ugaacm.WarpDrive.render;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 
 /**
  * Created by Eduardo Trujillo <ed@chromabits.com> on 10/19/13.
  */
 public class VertexBuffer {
 
     private ArrayList<Vertex> vertices;
 
     public VertexBuffer(){
         vertices = new ArrayList<Vertex>();
     }
 
     public void add(Vertex v){
         vertices.add(v);
     }
 
     public float[] getFloatArray(){
         float[] result = new float[vertices.size() * Vertex.COORDS_PER_VERTEX];
 
         int i = 0;
         for (Vertex vertex : vertices){
             result[i] = vertex.getX();
             i++;
             result[i] = vertex.getY();
             i++;
             result[i] = vertex.getZ();
             i++;
         }
 
         return result;
     }
 
     public FloatBuffer getBuffer(){
         FloatBuffer buffer;
         // A float has 4 bytes so we allocate for each coordinate 4 bytes
        ByteBuffer bbuffer = ByteBuffer.allocateDirect(vertices.size() * Vertex.getStride());
 
         bbuffer.order(ByteOrder.nativeOrder());
 
         // Allocates the memory from the byte buffer
         buffer = bbuffer.asFloatBuffer();
 
         // Put vertices on the float buffer
         buffer.put(getFloatArray());
 
         // Set position to the beginning of the buffer
         buffer.position(0);
 
         return buffer;
     }
 
     public int countVertices(){
         return vertices.size();
     }
 }
