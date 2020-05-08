 package uk.co.zutty.glarena;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.*;
 import uk.co.zutty.glarena.vertex.VertexArray;
 import uk.co.zutty.glarena.vertex.VertexBuffer;
 import uk.co.zutty.glarena.vertex.VertexFormat;
 
 import java.nio.FloatBuffer;
 import java.nio.ShortBuffer;
 
 import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
 import static uk.co.zutty.glarena.vertex.VertexFormat.Builder.format;
 
 /**
  * Represents a model stored on the GPU that can be rendered.
  */
 public class Model {
 
     private VertexArray vertexArray;
     private VertexBuffer vertexBuffer;
     private VertexBuffer indexBuffer;
     private int numIndices;
     private int glTexture;
 
     public Model(int texture) {
         glTexture = texture;
         numIndices = 0;
     }
 
     public static Model fromMesh(Mesh mesh, int texture) {
         VertexFormat format = format()
                 .withAttribute(3)
                 .withAttribute(3)
                 .withAttribute(2)
                 .build();
 
         Model model = new Model(texture);
         model.numIndices = mesh.getIndices().size();
 
         FloatBuffer vertexData = BufferUtils.createFloatBuffer(mesh.getVertices().size() * format.getStride());
         ShortBuffer indexData = BufferUtils.createShortBuffer(model.numIndices);
 
         for (Vertex vertex : mesh.getVertices()) {
             vertex.put(vertexData);
         }
 
         for (short index : mesh.getIndices()) {
             indexData.put(index);
         }
 
         vertexData.flip();
         indexData.flip();
 
         model.vertexArray = new VertexArray();
         model.vertexArray.bind();
 
         model.vertexBuffer = new VertexBuffer();
         model.vertexBuffer.bind();
 
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW);
 
         model.vertexArray.createAttributePointers(format);
 
         model.indexBuffer = new VertexBuffer(GL_ELEMENT_ARRAY_BUFFER);
         model.indexBuffer.bind();
        GL15.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL15.GL_STATIC_DRAW);
 
        GL30.glBindVertexArray(0);
 
         return model;
     }
 
     public void render() {
         GL13.glActiveTexture(GL13.GL_TEXTURE0);
         GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTexture);
 
         vertexArray.bind();
 
         GL11.glDrawElements(GL11.GL_TRIANGLES, numIndices, GL11.GL_UNSIGNED_SHORT, 0);
 
         vertexArray.unbind();
     }
 
     public void destroy() {
         // Delete texture
         GL11.glDeleteTextures(glTexture);
 
         // Delete the mesh
         vertexArray.destroy();
         vertexBuffer.destroy();
         indexBuffer.destroy();
     }
 }
