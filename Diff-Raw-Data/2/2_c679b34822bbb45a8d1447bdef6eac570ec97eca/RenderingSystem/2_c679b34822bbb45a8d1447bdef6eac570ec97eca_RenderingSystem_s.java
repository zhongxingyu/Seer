 package org.oskar.modules.rendering;
 
 import org.lwjgl.BufferUtils;
 import org.oskar.modules.GameModule;
 import org.oskar.world.GameWorld;
 
 import java.io.File;
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.nio.ShortBuffer;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL13.*;
 import static org.lwjgl.opengl.GL15.*;
 import static org.lwjgl.opengl.GL20.*;
 import static org.lwjgl.opengl.GL30.*;
 
 /**
  * Handles all the rendering.
  *
  * @author Oskar Veerhoek
  */
 public class RenderingSystem implements GameModule {
     private GameWorld gameWorld;
     /**
      * The vertex attribute position for the vertex position.
      */
     private final int VERTEX_POSITION = 0;
     /**
      * The vertex attribute position for the vertex colour.
      */
     private final int VERTEX_COLOUR = 1;
     private int vbo;
     private int ibo;
     private int vao;
     private int vertexShader;
     private int fragmentShader;
     private int shaderProgram;
     public RenderingSystem() {
 
     }
 
     private ShortBuffer asShortBuffer(short[] values) {
         ShortBuffer buffer = BufferUtils.createShortBuffer(values.length);
         buffer.put(values);
         return buffer;
     }
 
     private ShortBuffer asFlippedShortBuffer(short[] values) {
         ShortBuffer buffer = BufferUtils.createShortBuffer(values.length);
         buffer.put(values);
         buffer.flip();
         return buffer;
     }
 
     private IntBuffer asIntBuffer(int[] values) {
         IntBuffer buffer = BufferUtils.createIntBuffer(values.length);
         buffer.put(values);
         return buffer;
     }
 
     private IntBuffer asFlippedIntBuffer(int[] values) {
         IntBuffer intBuffer = asIntBuffer(values);
         intBuffer.flip();
         return intBuffer;
     }
 
     private FloatBuffer asFloatBuffer(float[] values) {
         FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(values.length);
         floatBuffer.put(values);
         return floatBuffer;
     }
 
     private FloatBuffer asFlippedFloatBuffer(float[] values) {
         FloatBuffer floatBuffer = asFloatBuffer(values);
         floatBuffer.flip();
         return floatBuffer;
     }
 
     /**
      * Check for OpenGL errors. Prints them to the GameWorld logger if the occur.
      */
     private void checkForErrors() {
         int error = glGetError();
         switch (error) {
             case GL_NO_ERROR:
                 break;
             case GL_INVALID_ENUM:
                 gameWorld.error(RenderingSystem.class, "OpenGL error: GL_INVALID_ENUM");
                 break;
             case GL_INVALID_VALUE:
                 gameWorld.error(RenderingSystem.class, "OpenGL error: GL_INVALID_VALUE");
                 break;
             case GL_INVALID_OPERATION:
                 gameWorld.error(RenderingSystem.class, "OpenGL error: GL_INVALID_OPERATION");
                 break;
             case GL_INVALID_FRAMEBUFFER_OPERATION:
                 gameWorld.error(RenderingSystem.class, "OpenGL error: GL_INVALID_FRAMEBUFFER_OPERATION");
                 break;
             case GL_OUT_OF_MEMORY:
                 gameWorld.error(RenderingSystem.class, "OpenGL error: GL_OUT_OF_MEMORY");
                 break;
         }
     }
 
     private void createBuffers() {
         gameWorld.debug(RenderingSystem.class, "Creating VAO");
         // >> Vertex Array Objects (VAO) are OpenGL Objects that store the
         // >> set of bindings between vertex attributes and the user's source
         // >> vertex data. (http://www.opengl.org/wiki/Vertex_Array_Object)
         // >> glGenVertexArrays returns n vertex array object names in arrays.
         // Create one VAO and store it in int vao.
         vao = glGenVertexArrays();
         // >> glBindVertexArray binds the vertex array object with name array.
         // Bind the aforementioned VAO to OpenGL.
         glBindVertexArray(vao);
         gameWorld.debug(RenderingSystem.class, "Creating IBO");
         // >> glGenBuffers returns n buffer object names in buffers.
         // >> No buffer objects are associated with the returned buffer object names
         // >> until they are first bound by calling glBindBuffer.
         // Create an OpenGL buffer that we'll use as an index buffer object (stores
         // indices that point to the data in the vertex buffer object).
         ibo = glGenBuffers();
         gameWorld.debug(RenderingSystem.class, "Creating VBO");
         // Create an OpenGL buffer that we'll use as a vertex buffer object (stores
         // vertex position and colour data).
         vbo = glGenBuffers();
         // >> glBindBuffer binds a buffer object to the specified buffer binding point.
         // Bind ibo to GL_ELEMENT_ARRAY_BUFFER.
         glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
         // Store the index data inside an IntBuffer and make it readable to OpenGL.
         IntBuffer indexData = asFlippedIntBuffer(new int[]{
                 0, 1, 2,
                 0, 2, 3
         });
         // >> glBufferData creates a new data store for the buffer object currently bound
         // >> to target. Any pre-existing data store is deleted. The new data store is created
         // >> with the specified size in bytes and usage. If data is not NULL, the data
         // >> store is initialized with data from this pointer. In its initial state, the
         // >> new data store is not mapped, it has a NULL mapped pointer, and its mapped
         // >> access is GL_READ_WRITE.
         // Store the index data inside the IBO.
         glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW);
         // Unbind the IBO.
         glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
         // Bind vbo to GL_ARRAY_BUFFER.
         glBindBuffer(GL_ARRAY_BUFFER, vbo);
         // Store the vertex position data inside a FloatBuffer and make it readable to OpenGL.
         FloatBuffer vertexPositionData = asFlippedFloatBuffer(new float[] {
                 -1.0f, -1.0f,
                 +1.0f, -1.0f,
                 +1.0f, +1.0f,
                 -1.0f, +1.0f,
         });
         // Calculate the size of the FloatBuffer in bytes.
         int vertexPositionDataSize = /* amount of elements */ 4 * /* amount of components */ 2 * /* size of float */ 4;
         // Store the vertex colour data inside a FloatBuffer and make it readable to OpenGL.
         FloatBuffer vertexColourData = asFlippedFloatBuffer(new float[] {
                 +1.0f, +0.0f, +0.0f,
                 +0.0f, +1.0f, +0.0f,
                 +0.0f, +0.0f, +1.0f,
                 +1.0f, +1.0f, +1.0f
         });
         // Calculate the size of the FloatBuffer in bytes.
         int vertexColourDataSize = /* amount of elements */ 4 * /* amount of components */ 3 * /* size of float */ 4;
         // Allocate enough size in vbo to support the vertexPositionData and colourPositionData FloatBuffers.
         glBufferData(GL_ARRAY_BUFFER, vertexPositionDataSize + vertexColourDataSize, GL_STATIC_DRAW);
         // >> glBufferSubData redefines some or all of the data store for the buffer object currently bound to target.
         // >> int target = type of buffer object (VBO = GL_ARRAY_BUFFER, IBO = GL_ELEMENT_ARRAY_BUFFER)
         // >> int offset = offset in bytes in the buffer object's data store where data will be stored
         // >> FloatBuffer data = new data that will be copied into the data store
         // Store the vertex position data at offset 0.
         glBufferSubData(GL_ARRAY_BUFFER, 0, vertexPositionData);
         // Store the vertex colour data after the vertex position data.
         glBufferSubData(GL_ARRAY_BUFFER, vertexPositionDataSize, vertexColourData);
         // >> glEnableVertexAttribArray enables the generic vertex attribute array specified by index.
         // >> glDisableVertexAttribArray disables the generic vertex attribute array specified by
         // >> index. By default, all client-side capabilities are disabled, including all generic
         // >> vertex attribute arrays. If enabled, the values in the generic vertex attribute array
         // >> will be accessed and used for rendering when calls are made to vertex array commands
         // >> such as glDrawArrays, glDrawElements, glDrawRangeElements, glMultiDrawElements, or glMultiDrawArrays.
         // Enable the vertex position attribute.
         glEnableVertexAttribArray(VERTEX_POSITION);
         // Enable the vertex colour attribute.
         glEnableVertexAttribArray(VERTEX_COLOUR);
         // >> glVertexAttribPointer and glVertexAttribIPointer specify the location and data format of the
         // >> array of generic vertex attributes at index index to use when rendering. size specifies
         // >> the number of components per attribute and must be 1, 2, 3, 4, or GL_BGRA. type specifies
         // >> the data type of each component, and stride specifies the byte stride from one attribute
         // >> to the next, allowing vertices and attributes to be packed into a single array or stored
         // >> in separate arrays.
         // Tell OpenGL where to find the vertex position data inside the VBO.
         glVertexAttribPointer(VERTEX_POSITION, 2, GL_FLOAT, false, 0, 0);
         // Tell OpenGL where to find the vertex colour data inside the VBO.
         glVertexAttribPointer(VERTEX_COLOUR, 3, GL_FLOAT, false, 0, 32);
         // Unbind the vertex buffer object.
         glBindBuffer(GL_ARRAY_BUFFER, 0);
         // Unbind the vertex array object.
         glBindVertexArray(0);
         checkForErrors();
     }
 
     private void createShaders() {
         gameWorld.debug(RenderingSystem.class, "Creating shaders");
         vertexShader = glCreateShader(GL_VERTEX_SHADER);
         glShaderSource(vertexShader, gameWorld.getResourceSystem().getTextFileContent("RESOURCE_VERTEX_SHADER"));
         glCompileShader(vertexShader);
         if (glGetShader(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
            gameWorld.fatal(RenderingSystem.class, "OpenGL vertex shader info log: " + glGetShaderInfoLog(fragmentShader, 2056));
         }
         fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
         glShaderSource(fragmentShader, gameWorld.getResourceSystem().getTextFileContent("RESOURCE_FRAGMENT_SHADER"));
         glCompileShader(fragmentShader);
         if (glGetShader(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
             gameWorld.fatal(RenderingSystem.class, "OpenGL fragment shader info log: " + glGetShaderInfoLog(fragmentShader, 2056));
         }
         shaderProgram = glCreateProgram();
         glAttachShader(shaderProgram, vertexShader);
         glAttachShader(shaderProgram, fragmentShader);
         glLinkProgram(shaderProgram);
         if (glGetProgram(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
             gameWorld.fatal(RenderingSystem.class, "OpenGL shader program info log: " + glGetProgramInfoLog(shaderProgram, 2056));
         }
         glUseProgram(shaderProgram);
         glValidateProgram(shaderProgram);
         glUseProgram(0);
         checkForErrors();
     }
 
     @Override
     public void create(GameWorld gameWorld) {
         gameWorld.info(RenderingSystem.class, "Creating rendering system");
         this.gameWorld = gameWorld;
         gameWorld.debug(RenderingSystem.class, "Checking OpenGL version");
         double openglVersion = Double.parseDouble(glGetString(GL_VERSION).substring(0, 3));
         if (openglVersion >= 3.2) {
             gameWorld.debug(RenderingSystem.class, "OpenGL version is correct: " + glGetString(GL_VERSION));
         } else {
             gameWorld.fatal(RenderingSystem.class, "Wrong OpenGL version: " + glGetString(GL_VERSION));
         }
         createBuffers();
         createShaders();
     }
 
     @Override
     public void destroy() {
         gameWorld.info(RenderingSystem.class, "Destroying rendering system");
         gameWorld.debug(RenderingSystem.class, "Destroying VBO");
         glBindBuffer(GL_ARRAY_BUFFER, 0);
         glDeleteBuffers(vbo);
         checkForErrors();
         gameWorld.debug(RenderingSystem.class, "Destroying IBO");
         glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
         glDeleteBuffers(ibo);
         checkForErrors();
         gameWorld.debug(RenderingSystem.class, "Destroying shader program");
         glUseProgram(0);
         glDeleteProgram(shaderProgram);
         checkForErrors();
         gameWorld.debug(RenderingSystem.class, "Destroying vertex shader");
         glDeleteShader(vertexShader);
         checkForErrors();
         gameWorld.debug(RenderingSystem.class, "Destroing fragment shader");
         glDeleteShader(fragmentShader);
         checkForErrors();
         gameWorld.debug(RenderingSystem.class, "Destroying VAO");
         glBindVertexArray(0);
         glDeleteVertexArrays(vao);
         checkForErrors();
     }
 
     @Override
     public GameWorld getGameWorld() {
         return gameWorld;
     }
 
     public void update() {
         glClear(GL_COLOR_BUFFER_BIT);
         glBindVertexArray(vao);
         glUseProgram(shaderProgram);
         glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
         glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
         glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
         glUseProgram(0);
         glBindVertexArray(0);
         checkForErrors();
     }
 
 }
