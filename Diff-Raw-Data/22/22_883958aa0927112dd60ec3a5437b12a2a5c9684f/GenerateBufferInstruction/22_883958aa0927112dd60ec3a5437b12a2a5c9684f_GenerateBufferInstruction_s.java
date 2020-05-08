 package outlast.engine.output.buffer;
 
 import com.jogamp.common.nio.Buffers;
 import outlast.engine.output.Asset;
 import outlast.engine.output.JOGLInstruction;
 
 import javax.media.opengl.GL3;
 import java.nio.Buffer;
 import java.nio.IntBuffer;
 
 public class GenerateBufferInstruction extends JOGLInstruction<GL3> {
 
     public static GenerateBufferInstruction generateBuffer(int target) {
         return new GenerateBufferInstruction(target);
     }
 
     private BufferObject bufferObject;
    private int target, stride, hint;
     private Buffer data;
 
     private GenerateBufferInstruction(int target) {
         this.target = target;
         this.bufferObject = new BufferObject(target);
     }
 
     public GenerateBufferInstruction withFloatData(float[] data) {
         this.data = Buffers.newDirectFloatBuffer(data).asReadOnlyBuffer();
         return this;
     }
 
     public GenerateBufferInstruction withRenderingHint(int hint) {
         this.hint = hint;
         return this;
     }
 
    public GenerateBufferInstruction withStride(int stride) {
        this.stride = stride;
        return this;
    }

     public Asset<BufferObject> getAsset() {
         return new Asset<>(bufferObject);
     }
 
     @Override
     public void render(GL3 gl) {
         generateBuffer(gl);
     }
 
     protected void generateBuffer(GL3 gl) {
         int handle = generateBufferHandle(gl);
         gl.glBindBuffer(target, handle);
        gl.glBufferData(target, data.capacity() * stride, data, hint);
         gl.glBindBuffer(target, 0);
         bufferObject.setHandle(handle);
     }
 
     protected int generateBufferHandle(GL3 gl) {
         IntBuffer handleBuffer = Buffers.newDirectIntBuffer(1);
         gl.glGenBuffers(1, handleBuffer);
         return handleBuffer.get();
     }
 }
