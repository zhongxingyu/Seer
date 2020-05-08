 package outlast.game;
 
import com.jogamp.common.nio.Buffers;
 import io.metacake.core.output.RenderingInstructionBundle;
 import io.metacake.core.process.state.GameState;
 import io.metacake.core.process.state.TransitionState;
 import outlast.engine.output.Asset;
 import outlast.engine.output.JOGLDevice;
 import outlast.engine.output.buffer.*;
 import outlast.engine.output.shader.CreateShaderInstruction;
 import outlast.engine.output.shader.ShaderProgram;
 import outlast.engine.state.LoadingPhase;
 import outlast.engine.state.PhaseLoadingState;
 
 import javax.media.opengl.GL3;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 public class LoadingState extends PhaseLoadingState {
     public static final float[] vertices = {
             0.75f, 0.75f, 0.0f, 1.0f,
             0.75f, -0.75f, 0.0f, 1.0f,
             -0.75f, -0.75f, 0.0f, 1.0f,
     };
 
     Asset<ShaderProgram> shaderAsset = new Asset<>(new ShaderProgram());
     Asset<BufferObject> vbo;
     Asset<VertexAttributeObject> vao;
 
     public LoadingState() {
         super();
         this.addPhase(phase0());
         this.addPhase(phase1());
     }
 
     private LoadingPhase phase0() {
         return new LoadingPhase() {
             @Override
             public RenderingInstructionBundle getRenderBundle() {
                 RenderingInstructionBundle bundle = new RenderingInstructionBundle();
                 Path vPath = Paths.get("engine", "src", "main", "resources", "vertex.glsl");
                 Path fPath = Paths.get("engine", "src", "main", "resources", "fragment.glsl");
                 CreateShaderInstruction inst = ShaderProgram.create(shaderAsset).withVertexShader(vPath).withFragmentShader(fPath);
                 bundle.add(JOGLDevice.NAME, inst);
                 return bundle;
             }
         };
     }
 
     private LoadingPhase phase1() {
         return new LoadingPhase() {
             @Override
             public RenderingInstructionBundle getRenderBundle() {
                 RenderingInstructionBundle bundle = new RenderingInstructionBundle();
 
                 GenerateBufferInstruction vboInstruction = GenerateBufferInstruction.generateBuffer(GL3.GL_ARRAY_BUFFER);
                vboInstruction.withFloatData(vertices).withRenderingHint(GL3.GL_STATIC_DRAW);
                 vbo = vboInstruction.getAsset();
                 bundle.add(JOGLDevice.NAME, vboInstruction);
 
                 GenerateVAOInstruction vaoInstruction = GenerateVAOInstruction.create();
                 vaoInstruction.withVertexAttribute(new VertexAttribute(shaderAsset.getValue().getAttributeLocation("position"), 4, 0));
                 vao = vaoInstruction.getAsset();
                 bundle.add(JOGLDevice.NAME, vaoInstruction);
                 return bundle;
             }
         };
     }
 
     @Override
     protected GameState nextState() {
         return TransitionState.transitionWithTriggers(new MainState(shaderAsset, vbo, vao));
     }
 }
