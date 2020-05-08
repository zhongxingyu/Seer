 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.q1cc.cfs.mmm.client.render;
 
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import net.q1cc.cfs.mmm.client.Client;
 import net.q1cc.cfs.mmm.common.world.Block;
 import net.q1cc.cfs.mmm.common.world.Chunklet;
 import net.q1cc.cfs.mmm.common.world.WorldOctree;
 import org.lwjgl.BufferUtils;
 import org.lwjgl.MemoryUtil;
 import org.lwjgl.Sys;
 
 /**
  * The GL Extension of a chunklet, able to store VBO IDs and such.
  * Stores a pointer to the underlying chunklet.
  * Not to be serialized.
  * @author cfstras
  */
 public class GLChunklet extends Chunklet implements WorkerTask {
     
     /**
      * The VBO ID for this chunklet, if it is in VRAM.
      * if not, this is -1.
      */
     public int vboID=-1;
     public int iboID=-1;
     
     /**
      * The VAO ID for render setup of this chunklet.
      * if none exists, is -1.
      */
     public int vaoID=-1;
     
     /**
      * Resident client memory size of this chunklet, in bytes.
      */
     public int memorySize = 0;
     
     /**
      * number of indices in index buffer
      */
     public int indCount;
     
     public FloatBuffer vertexB;
     public IntBuffer indexB;
     
     boolean built=false;
     boolean update=false;
     
     /**
      * creates a GLChunklet from a given common chunklet.
      * Does not create video memory representations yet.
      * @param from 
      */
     public GLChunklet(Chunklet from) {
         super(from.posX,from.posY,from.posZ, from.parent);
        this.blocks=from.blocks;
         if(from.parent==null){
             System.out.println("muh!");
         }
     }
     
     //public GLChunklet(int x, int y, int z, WorldOctree parent){
     //    super(x,y,z, parent);
     //}
     
     /**
      * Builds a chunks representation into video memory.
      * takes an existing ByteBuffer as source, if null or too small, requests
      * a new one.
      * Vertices are stored as such:
      * 
      * struct vertex {
      *      float posX, posY, posZ;
      *      float lightLevel;
      *      float texX, texY, texZ;
      *      float padding; // does nothing.
      * }
      * 
      * @param targetV the target vertex buffer, if one is avaliable.
      * @param targetI same for index buffer
      */
     public void buildChunklet(ByteBuffer targetV, ByteBuffer targetI) {
         int sizeGivenI=0, sizeGivenV=0;
         //build chunk
         //TODO compress surfaces
         // needed:    float perVertex
         int vertexSize = 8;
         int indexSize = 1;
         int vertices = 0;
         int indices = 0;
         
         for(Block b:blocks){
             if(b!=null){
                 //TODO check for special block
                 vertices += 8;
                 indices += 6*2*3;
             }
         }
         blocksInside = vertices / 8;
         if(blocksInside==0) {
             built=true;
            //System.out.println("height "+parent.height+" discarded. x="+posX+" y="+posY+" z="+posZ);
             return;
         } else {
            //System.out.println("height "+parent.height+" has "+blocksInside+" blocks. x="+posX+" y="+posY+" z="+posZ);
         }
         
         FloatBuffer bufferV=null;
         if(targetV!=null) {
             bufferV = targetV.asFloatBuffer();
             sizeGivenV = bufferV.capacity();
         }
         IntBuffer bufferI=null;
         if(targetI!=null) {
             bufferI = targetI.asIntBuffer();
             sizeGivenI = bufferI.capacity();
         }
         if( (sizeGivenI < indices * indexSize)){
             bufferI = BufferUtils.createIntBuffer(indices);
         }
         if( (sizeGivenV < vertices * vertexSize)){
             bufferV = BufferUtils.createFloatBuffer(vertices);
         }
         //start filling
         int vertexPos=0;
         Block b;
         for (int ix=0; ix<Chunklet.csl; ix++) {
             for(int iy=0; iy<Chunklet.csl; iy++) {
                 for(int iz=0; iz<Chunklet.csl; iz++) {
                     b = blocks[ix + iy*Chunklet.csl + iz*Chunklet.csl2];
                     if(b==null) continue;
                     // the indices
                     //front
                     bufferI.put(vertexPos+0).put(vertexPos+4).put(vertexPos+2);
                     bufferI.put(vertexPos+4).put(vertexPos+6).put(vertexPos+2);
                     //right
                     bufferI.put(vertexPos+4).put(vertexPos+5).put(vertexPos+6);
                     bufferI.put(vertexPos+6).put(vertexPos+5).put(vertexPos+7);
                     //left
                     bufferI.put(vertexPos+2).put(vertexPos+1).put(vertexPos+0);
                     bufferI.put(vertexPos+2).put(vertexPos+3).put(vertexPos+1);
                     //top
                     bufferI.put(vertexPos+2).put(vertexPos+6).put(vertexPos+3);
                     bufferI.put(vertexPos+3).put(vertexPos+6).put(vertexPos+7);
                     //back
                     bufferI.put(vertexPos+5).put(vertexPos+1).put(vertexPos+7);
                     bufferI.put(vertexPos+7).put(vertexPos+1).put(vertexPos+3);
                     //bottom
                     bufferI.put(vertexPos+1).put(vertexPos+4).put(vertexPos+0);
                     bufferI.put(vertexPos+1).put(vertexPos+5).put(vertexPos+4);
                     
                     for(int iix=0;iix<2;iix++){ // do the vertices
                         for(int iiy=0;iiy<2;iiy++){
                             for(int iiz=0;iiz<2;iiz++){
                                 bufferV.put(iix+ix+posX).put(iiy+iy+posY).put(iiz+iz+posZ);
                                 bufferV.put(b.lightLevel);
                                 bufferV.put(b.color.getRed()/255.0f).put(b.color.getGreen()/255.0f).put(b.color.getBlue()/255.0f);
                                 bufferV.put(1337);
                                 vertexPos++;
                             }
                         }
                     }
                     
                 }
             }
             //done.
             indCount=indices;
             bufferV.flip();
             bufferI.flip();
             vertexB = bufferV;
             indexB = bufferI;
             built=true;
             Client.instance.renderer.chunksToBuffer.add(this);
            //System.out.print(".");
         }
         
     }
 
     @Override
     public boolean doWork() {
         if(!built){
             buildChunklet(null, null);
         } else {
             //System.out.println("nothing to do.");
         }
         return true;
     }
 
     @Override
     public int getPriority() {
         return WorkerTask.PRIORITY_NORM;
     }
     
     
 }
