 /* Copyright 2012 Richard Sahlin
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 package com.super2k.openglen.program;
 
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.StringTokenizer;
 
 import com.super2k.openglen.ConstantValues;
 import com.super2k.openglen.OpenGLENException;
 import com.super2k.openglen.utils.GraphicsLibraryHandler;
 import com.super2k.openglen.utils.Log;
 
 /**
  * This class handles shader program related functions in a platform dependent way.
  * One version is needed for each different OS/platform/GL
  * Eg one for OpenGL ES on Android.
  * @see AndroidProgramHandler
  * @author Richard Sahlin
  *
  */
 public abstract class ProgramHandler {
 
     private final String TAG = getClass().getSimpleName();
 
     protected final static String ATTACH_SHADER_ERROR_STR = "Could not attach shader to program:";
     protected final static String DETACH_SHADER_ERROR_STR = "Could not detach shader from program:";
     protected final static String COMPILE_SHADER_ERROR_STR = "Could not compile shader:";
     protected final static String LINK_PROGRAM_ERROR_STR = "Could not link program:";
     protected final static String DELETE_SHADER_ERROR_STR = "Could not delete shader:";
     protected final static String DELETE_PROGRAM_ERROR_STR = "Could not delete program:";
     protected final static String SHADER_SOURCE_ERROR_STR = "Could not set source to shader:";
     protected final static String CREATE_SHADER_ERROR_STR = "Could not create shader type:";
     protected final static String CREATE_PROGRAM_ERROR_STR = "Could not create program";
     protected final static String ERROR_STR = " Error:";
     protected final static String GRAPHICS_LIBRARY_NULL_STR = "GraphicsLibraryHandler is NULL";
     protected final static String INVALID_PROGRAM_STR = "Could not use program, invalid program (program/error):";
     protected final static String INVALID_UNIFORM_STR = "Could not set uniform, invalid value";
 
     private final static int MATRIX_SIZE = 16;
     /**
      * GraphicsLibraryHandler to be used.
      */
     protected GraphicsLibraryHandler mGraphicsLibrary;
 
     /**
      * Controls if shader sources and extra info is output.
      */
     protected boolean mDebugOutput = false;
 
     /**
      * Constructs a new ProgramHandler with the specified GraphicsLibraryHandler.
      * @param graphicsLibrary The GraphicsLibraryHandler to be used.
      * @throws IllegalArgumentException if graphicsLibrary is NULL
      */
     protected ProgramHandler(GraphicsLibraryHandler graphicsLibrary) {
         if (graphicsLibrary == null) {
             throw new IllegalArgumentException(GRAPHICS_LIBRARY_NULL_STR);
         }
         mGraphicsLibrary = graphicsLibrary;
     }
 
     /**
      * Compiles a shader of the specified type using source read from an array of InputStream,
      * all shader sources will be combined into one source.
      * This is handy since GLES 2 does not support attaching multiple shaders to one program.
      * The shader will be compiled and attached to the specified program.
      * If shaderName is -1 then a new name is allocated.
      * The caller should delete the shader name when it is not needed anymore.
      * The InputStream is left as is, caller shall close it when it is not needed anymore.
      * @param programName The program to attach the shader to.
      * @param type Type of shader. Currently ConstantValues.GL_VERTEX_SHADER or
      * ConstantValues.GL_FRAGMENT_SHADER
      * @param shaderSource The shader source.
      * @param graphicsHandler The {@link GraphicsLibraryHandler} to use
      * @return Name of the shader.
      * @throws OpenGLENException If there is a problem compiling or attaching shader sources.
      * @throws IOException If there is an IO error reading the shader source.
      */
     public int compileAndAttachShader(int programName, int shaderName, int type, InputStream[] in,
             GraphicsLibraryHandler graphicsHandler) throws OpenGLENException, IOException {
 
         if (shaderName == -1) {
             shaderName = createShader(type);
         }
 
         /**
          * Combine sources into one.
          */
         boolean mainFile = true;
         StringBuffer shaderSource = new StringBuffer();
         for (int i = 0; i < in.length; i++){
 
             BufferedReader shaderReader = new BufferedReader(new InputStreamReader(in[i]));
 
             int lineNo = 0;
             String src = getShaderSource(type, shaderReader, mainFile);
             if (mDebugOutput) {
                 StringTokenizer st = new StringTokenizer(src, "\n");
                 //Log shader source.
                 while (st.hasMoreTokens()) {
                     Log.d(Integer.toString(lineNo++), st.nextToken());
                 }
             }
             shaderSource.append(src);
             mainFile = false;
             shaderReader.close();
         }
         //Make sure no error
         while (graphicsHandler.checkError() != ConstantValues.NO_ERROR);
 
         int error = ConstantValues.NO_ERROR;
         try {
             setShaderSource(shaderName, shaderSource.toString());
             error = graphicsHandler.checkError();
             if (error != ConstantValues.NO_ERROR)
                 throw new IllegalArgumentException("Error setting shader source : " + error);
 
             compileShader(shaderName);
             if ((error = graphicsHandler.checkError()) != ConstantValues.NO_ERROR) {
                 throw new IllegalArgumentException("Error calling glCompileShader (" +
                         shaderName + ") : " + error);
             }
 
             if (getShaderParam(shaderName,ConstantValues.COMPILE_STATUS)
                     != ConstantValues.TRUE) {
                 throw new IllegalArgumentException(COMPILE_SHADER_ERROR_STR + "(" + shaderName + ")");
             }
 
             attachShader(programName, shaderName);
             if ((error = graphicsHandler.checkError()) != ConstantValues.NO_ERROR) {
                 throw new IllegalArgumentException("Could not attach shader (" +
                         shaderName + ") to program (" + programName + ") : " + error);
             }
 
             if (mDebugOutput) {
                 Log.d(TAG, "Compiled shader " + shaderName);
                 logShaderInfo(shaderName);
             }
 
         }
         catch (Exception e) {
             StringTokenizer shaderSt = new StringTokenizer(shaderSource.toString(), "\n");
             StringBuffer text = new StringBuffer();
             int line = 1;
             while (shaderSt.hasMoreTokens()) {
                 text.append("\n" + line++ + ": " + shaderSt.nextToken());
             }
             Log.d(TAG, text.toString());
             if (shaderName != 0) {
                 logShaderInfo(shaderName);
                 //Delete the shader
                 deleteShader(shaderName);
             }
             throw new IllegalArgumentException(e);
         }
         return shaderName;
 
     }
 
 
     /**
      * Creates a program and returns the program name.
      * This is to abstract the need of calling the underlying graphics library to create a program.
      * @return The program name
      * @throws OpenGLENException If program can't be created.
      */
     public int createProgram() throws OpenGLENException    {
         int name = internalCreateProgram();
         if (name > 0)  {
             return name;
         }
         throw new OpenGLENException(CREATE_PROGRAM_ERROR_STR);
     }
 
     /**
      * Use the specified program.
      * @param program The program to use, shall be a compiled and linked program.
      * @throws IllegalArgumentException If program is not valid.
      */
     public void useProgram(int program) {
         int result = internalUseProgram(program);
         if (result != ConstantValues.NO_ERROR) {
             throw new IllegalArgumentException(INVALID_PROGRAM_STR + program + "/" + result);
         }
     }
 
     /**
      * Set the Matrix(es) to the specified uniform
      * @param uniform The uniform to set the matrix to.
      * @param count Number of matrixes to set.
      * @param matrix The matrix(es) to set.
      * @param offset Offset into matrix where data starts.
      * @throws IllegalArgumentException If matrix is null, matrix.length < 16 + offset, or an invalid value.
      */
     public void setUniformMatrix(int uniform, int count, float[] matrix, int offset) {
         if (matrix == null || count < 0 || matrix.length < count * MATRIX_SIZE + offset) {
             throw new IllegalArgumentException(INVALID_UNIFORM_STR);
         }
         int result = internalSetUniformMatrix(uniform, count, matrix, offset);
         if (result != ConstantValues.NO_ERROR) {
            throw new IllegalArgumentException(INVALID_UNIFORM_STR + " " + uniform);
         }
     }
 
     /**
      * Sets the int uniform to the specified uniform location.
      * @param uniform The uniform location.
      * @param value The value to set.
      * @throws IllegalArgumentException If the value could not be set, invalid uniform location.
      */
     public void setUniformInt(int uniform, int value) {
         int result = internalSetUniformInt(uniform, value);
         if (result != ConstantValues.NO_ERROR) {
             throw new IllegalArgumentException(INVALID_UNIFORM_STR);
         }
     }
 
     /**
      * Sets the float uniform to the specified uniform location.
      * @param uniform The uniform location.
      * @param value The value to set.
      * @throws IllegalArgumentException If the value could not be set, invalid uniform location.
      */
     public void setUniformFloat(int uniform, float value) {
         int result = internalSetUniformFloat(uniform, value);
         if (result != ConstantValues.NO_ERROR) {
             throw new IllegalArgumentException(INVALID_UNIFORM_STR);
         }
 
     }
 
     /**
      * Sets the vector 4 uniform at the specified location, one or more vectors.
      * @param uniform The uniform location
      * @param count Number of vectors to set
      * @param vector The data to store.
      * @param offset Offset into vector.
      * @throws IllegalArgumentException If count is negative, vector null, not enough values or
      * the uniform could not be set (invalid uniform location or other error)
      */
     public void setUniformVector(int uniform, int count, float[] vector, int offset) {
         if (count < 0 || vector == null || vector.length < (count * 4 + offset)) {
             throw new IllegalArgumentException(INVALID_UNIFORM_STR);
         }
         int result = internalSetUniformVector(uniform, count, vector, offset);
         if (result != ConstantValues.NO_ERROR) {
             throw new IllegalArgumentException(INVALID_UNIFORM_STR + ":" + result);
         }
     }
 
     /**
      * Sets the vector 3 uniform at the specified location, one or more vectors.
      * @param uniform The uniform location
      * @param count Number of vectors to set
      * @param vector The data to store.
      * @param offset Offset into vector.
      * @throws IllegalArgumentException If count is negative, vector null, not enough values or
      * the uniform could not be set (invalid uniform location or other error)
      */
     public void setUniformVector3(int uniform, int count, float[] vector, int offset) {
         if (count < 0 || vector == null || vector.length < (count * 3 + offset)) {
             throw new IllegalArgumentException(INVALID_UNIFORM_STR);
         }
         int result = internalSetUniformVector3(uniform, count, vector, offset);
         if (result != ConstantValues.NO_ERROR) {
             throw new IllegalArgumentException(INVALID_UNIFORM_STR + ":" + result);
         }
     }
 
     public void setUniformVector2(int uniform, int count, float[] vector, int offset) {
         if (count < 0 || vector == null || vector.length < (count * 2 + offset)) {
             throw new IllegalArgumentException(INVALID_UNIFORM_STR);
         }
         int result = internalSetUniformVector2(uniform, count, vector, offset);
         if (result != ConstantValues.NO_ERROR) {
             throw new IllegalArgumentException(INVALID_UNIFORM_STR + ":" + result);
         }
     }
 
     /**
      * Creates a shader name for the specified shader type and returns the name.
      * @param type Shader type ConstantValues.VERTEX_SHADER or ConstantValues.FRAGMENT_SHADER
      * @return Shader name
      * @throws IllegalArgumentException if type is not
      * ConstantValues.VERTEX_SHADER or ConstantValues.FRAGMENT_SHADER
      * @throws OpenGLENException If a shader could not be created.
      */
     public int createShader(int type) throws OpenGLENException  {
         if (type != ConstantValues.VERTEX_SHADER && type != ConstantValues.FRAGMENT_SHADER){
             throw new IllegalArgumentException("Invalid shader type " + type);
         }
         int shader = internalCreateShader(type);
         if (shader > 0)    {
             return shader;
         }
         throw new OpenGLENException(CREATE_SHADER_ERROR_STR + type);
     }
 
     /**
      * Internal wrapper method for createShader call, implement on platform.
      * Error checking shall be performed before calling this method.
      * @param type Shader type ConstantValues.VERTEX_SHADER or ConstantValues.FRAGMENT_SHADER
      * @return The created shader or 0 if fails.
      */
     protected abstract int internalCreateShader(int type);
 
     /**
      * Internal wrapper method for createProgram, implement on platform.
      * @return The created program or 0 if fails.
      */
     protected abstract int internalCreateProgram();
 
     /**
      * Internal wrapper method for delete shader.
      * Return the result of the operation.
      * @param name Name of shader to delete
      * @return Result ConstantValues.NO_ERROR or
      * ConstantValues.INVALID_VALUE if name is invalid.
      */
     protected abstract int internalDeleteShader(int name);
 
     /**
      * Internal wrapper method for delete program.
      * Returns the result of the operation.
      * @param program The name of the program to delete.
      * @return Result, ConstantValues.NO_ERROR or
      * ConstantValues.INVALID_VALUE if program is invalid.
      */
     protected abstract int internalDeleteProgram(int program);
 
     /**
      * Internal wrapper method for use program.
      * Return the result of the operation.
      * @param program The program to use.
      * @return ConstantValues.NO_ERROR or error code.
      */
     protected abstract int internalUseProgram(int program);
 
     /**
      * Internal wrapper method to set one or more uniformMatrix values
      * @param uniform The uniform to set the Matrix to.
      * @param count Number of matrix(es) to set.
      * @param matrix The matrix data.
      * @param offset Offset into matrix.
      * @return result - ConstantValues.NO_ERROR or error code.
      */
     protected abstract int internalSetUniformMatrix(int uniform, int count, float[] matrix, int offset);
 
     /**
      * Internal wrapper method to set one int value to the specified uniform location.
      * @param uniform The uniform location to set value to
      * @param value The int value to set.
      * @return result ConstantValues.NO_ERROR or error code.
      */
     protected abstract int internalSetUniformInt(int uniform, int value);
 
     /**
      * Internal wrapper method to set one float value to the specified uniform location
      * @param uniform The uniform location to set the value to.
      * @param value The value to set.
      * @return result ConstantValues.NO_ERROR or error code.
      */
     protected abstract int internalSetUniformFloat(int uniform, float value);
 
     /**
      * Internal wrapper method to set one vec4 uniform to the specified uniform location.
      * @param uniform The uniform location to set the vec4 to
      * @param count Number of vec4 to set.
      * @param vector The data
      * @param offset Offset into vector.
      * @return result ConstantValues.NO_ERROR or error code.
      */
     protected abstract int internalSetUniformVector(int uniform, int count, float[] vector, int offset);
 
     /**
      * Internal wrapper method to set one vec3 uniform to the specified uniform location.
      * @param uniform The uniform location to set the vec3 to
      * @param count Number of vec3 to set.
      * @param vector The data
      * @param offset Offset into vector.
      * @return result ConstantValues.NO_ERROR or error code.
      */
     protected abstract int internalSetUniformVector3(int uniform, int count, float[] vector, int offset);
 
     /**
      * Internal wrapper method to set one vec2 uniform to the specified uniform location.
      * @param uniform The uniform location to set the vec2 to
      * @param count Number of vec2 to set.
      * @param vector The data
      * @param offset Offset into vector.
      * @return result ConstantValues.NO_ERROR or error code.
      */
     protected abstract int internalSetUniformVector2(int uniform, int count, float[] vector, int offset);
 
     /**
      * Internal wrapper method for setshadersource.
      * Returns the result of the operation.
      * @param shader Shader to set the source to.
      * @param source The shader sourcecode.
      * @return Result  ConstantValues.NO_ERROR
      * ConstantValues.INVALID_VALUE or ConstantValues.INVALID_OPERATION
      */
     protected abstract int internalSetShaderSource(int shader, String source);
 
     /**
      * Internal wrapper method for attachshader.
      * Returns the result of the operation.
      * @param program The program to attach the shader to.
      * @param shader The shader to attach.
      * @return Result ConstantValues.NO_ERROR
      * ConstantValues.INVALID_VALUE or ConstantValues.INVALID_OPERATION
      */
     protected abstract int internalAttachShader(int program, int shader);
 
     /**
      * Internal wrapper method for compileshader.
      * Returns the result of the operation, callers should check COMPILE_STATUS
      * by calling getShaderParam.
      * @param shader The shader to compile.
      * @return Result ConstantValues.NO_ERROR
      * ConstantValues.INVALID_VALUE or ConstantValues.INVALID_OPERATION
      */
     protected abstract int internalCompileShader(int shader);
 
     /**
      * Internal wrapper method for linkprogram.
      * Returns the result of the operation, callers should check LINK_STATUS
      * by calling getProgramParam.
      * @param program The program object to link.
      * @return Result ConstantValues.NO_ERROR
      * ConstantValues.INVALID_VALUE or ConstantValues.INVALID_OPERATION
      */
     protected abstract int internalLinkProgram(int program);
 
     /**
      * Internal wraper method for detachshader.
      * Returns the result of the operation, callers should check DELETE_STATUS
      * by calling getShaderParam.
      * @param program The program object to detach the shader from.
      * @param shader The shader object to detach.
      * @return Result ConstantValues.NO_ERROR
      * ConstantValues.INVALID_VALUE or ConstantValues.INVALID_OPERATION
      */
     protected abstract int internalDetachShader(int program, int shader);
 
     /**
      * Frees the memory and invalidates the name
      * associated with the shader object specified by shader.
      * For a shader to be deleted it must first be detached.
      * The implementation shall check the status of the call to
      * the underlying platform and log error.
      * Don't throw exception since there is no recovery from failing.
      * @param name Name of shader to delete.
      */
     public void deleteShader(int name) {
         int result = internalDeleteShader(name);
         if (result == ConstantValues.NO_ERROR){
             return;
         }
         Log.d(TAG, DELETE_SHADER_ERROR_STR + name + ERROR_STR + result);
     }
 
     /**
     * Frees the memory and invalidates the name associated with the program object specified
     * by program. This command effectively undoes the effects of a call to createProgram.
     * If a program object is in use as part of current rendering state,
     * it will be flagged for deletion, but it will not be deleted until it is no longer part of
     * current state for any rendering context.
     * If a program object to be deleted has shader objects attached to it,
     * those shader objects will be automatically detached but not deleted unless they have already
     * been flagged for deletion by a previous call to deleteShader.
     * A value of 0 for program will be silently ignored.
      * @param program
      */
     public void deleteProgram(int program) {
         int result = internalDeleteProgram(program);
         if (result == ConstantValues.NO_ERROR){
             return;
         }
         Log.d(TAG, DELETE_PROGRAM_ERROR_STR + program + ERROR_STR + result);
 
     }
 
 
     /**
      * Sets the shader source for the specified shader name.
      * Any source code previously stored in the shader object is completely
      * replaced.
      * The source code strings are not scanned or parsed at this time;
      * they are simply copied into the specified shader object.
      * @param shader
      * @param source
      * @throws OpenGLENException If setting shader source did not succeed.
      */
     public void setShaderSource(int shader, String source) throws OpenGLENException {
         int result = internalSetShaderSource(shader, source);
         if (result != ConstantValues.NO_ERROR) {
             throw new OpenGLENException(SHADER_SOURCE_ERROR_STR + shader +
                     ERROR_STR + result, result);
         }
     }
 
     /**
      * Compiles the shader, shader source must be set before calling this method.
      * An exception will be raised if the shader can not be compiled.
      * @param shader The shader to be compiled, source must be set.
      * @throws OpenGLENException If the program could not be linked, error contains reason.
      * ConstantValues.INVALID_OPERATION if shader compiler not supported or
      * shader is not a shader object.
      * ConstantValues.INVALID_VALUE if shader is invalid value.
      * ConstantValues.COMPILE_STATUS Failed to compile, error in source.
      */
     public void compileShader(int shader) throws OpenGLENException  {
         int result = internalCompileShader(shader);
         if (result != ConstantValues.NO_ERROR){
             throw new OpenGLENException(COMPILE_SHADER_ERROR_STR + shader +
                     ERROR_STR +result, result);
         }
         //Check compile status.
         if (getShaderParam(shader, ConstantValues.COMPILE_STATUS) != ConstantValues.TRUE)    {
             //Output info from shader to help debugging.
             logShaderInfo(shader);
             throw new OpenGLENException(COMPILE_SHADER_ERROR_STR +
                     ERROR_STR + "COMPILE_STATUS=false");
         }
 
     }
 
     /**
      * Links the program object specified by program.
      * Shader objects of type GL_VERTEX_SHADER attached to
      * program are used to create an executable that will run on the programmable vertex
      * processor. Shader objects of type GL_FRAGMENT_SHADER attached to program
      * are used to create an executable that will run on the programmable fragment
      * processor.
      * @param program The program object to link.
      * @throws OpenGLENException If the program could not be linked, error contains reason.
      * ConstantValues.INVALID_OPERATION If program is not a program object.
      * ConstantValues.INVALID_VALUE If program is an invalid value.
      * ConstantValues.LINK_STATUS If linking failed.
      */
     public void linkProgram(int program) throws OpenGLENException   {
         int result = internalLinkProgram(program);
         if (result != ConstantValues.NO_ERROR){
             throw new OpenGLENException(LINK_PROGRAM_ERROR_STR + program +
                     ERROR_STR + result, result);
         }
         //Check status of LINK_PROGRAM flag.
         if (getProgramParam(program, ConstantValues.LINK_STATUS) != ConstantValues.TRUE)    {
             throw new OpenGLENException(LINK_PROGRAM_ERROR_STR +
                     ERROR_STR + "LINK_STATUS=false");
         }
     }
 
 
     /**
      * Attach the specified shader to a program.
      * Shaders that are to be linked together in a program object
      * must first be attached to that program object.
      * It is permissible to attach a shader object to a
      * program object before source code has been loaded into the
      * shader object or before the shader object has been compiled.
      * Multiple shader objects of the same type may not be attached to
      * a single program object. However, a single shader object may be
      * attached to more than one program object.
      * If a shader object is deleted while it is attached to a program object,
      * it will be flagged for deletion, and deletion will not occur until
      * detachShader is called to detach it from all program objects to which it is
      * attached.     *
      * @param program Name of the program to attach the shader to.
      * @param shader Shader to attach to the specified program.
      * @throws OpenGLENException If the shader could not be attached to the program.
      */
     public void attachShader(int program, int shader) throws OpenGLENException {
         int result = internalAttachShader(program, shader);
         if (result != ConstantValues.NO_ERROR)  {
             throw new OpenGLENException(ATTACH_SHADER_ERROR_STR + shader + "," + program +
                     ERROR_STR + result, result);
         }
     }
 
     /**
      * Detaches the shader object specified by shader from the
      * program object specified by program.
      * This command can be used to undo the effect of the command
      * attachShader().
      * If shader has already been flagged for deletion,
      *  by a call to deleteShader() and it is not attached
      *  to any other program object, it will be
      * deleted after it has been detached.     *
      * @param program
      * @param shader
      * @throws OpenGLENException If the shader could not be detached, error contains reason.
      * ConstantValues.INVALID_OPERATION If program or shader is not valie object.
      * ConstantValues.INVALID_VALUE If program or shader is an invalid value.
      * ConstantValues.DELETE_STATUS If deletion failed.
      */
     public void detachShader(int program, int shader) throws OpenGLENException  {
         int result = internalDetachShader(program, shader);
         if (result != ConstantValues.NO_ERROR){
             throw new OpenGLENException(DETACH_SHADER_ERROR_STR + shader + "," + program +
                     ERROR_STR + result, result);
         }
         //Check status of LINK_PROGRAM flag.
         if (getShaderParam(shader, ConstantValues.DELETE_STATUS) != ConstantValues.TRUE)    {
             throw new OpenGLENException(DETACH_SHADER_ERROR_STR + shader + "," + program +
                     ERROR_STR + "LINK_STATUS=false");
         }
 
     }
 
 
     /**
      * Prepare the shader source for the specific shader type from the inputstream in a platform specific manner.
      * @param shaderType Type of shader. ConstantValues.VERTEX_SHADER or ConstantValues.FRAGMENT_SHADER
      * @param shaderSource
      * @param mainFile True if the source is the main file (with version definition,precision qualifiers etc), false for an attached lib file.
      * @return The prepared shader source.
      */
     public abstract String getShaderSource(int shaderType, BufferedReader shaderSource, boolean mainFile) throws IOException;
 
     /**
      * Get a shader parameter, this can be used to check the shader status.
      * @param shader
      * @param param The shader parameter to get.
      * @return
      */
     public abstract int getShaderParam(int shader, int param);
 
     /**
      * Get a program parameter, this can be used to check the program status.
      * @param program
      * @param param
      * @return
      */
     public abstract int getProgramParam(int program, int param);
 
     /**
      * Logs the info for the specified shader.
      * @param shader
      */
     public abstract void logShaderInfo(int shader);
 
     /**
      * Logs the info for the specified program.
      * @param program
      */
     public abstract void logProgramInfo(int program);
 
     /**
      *
      * @param program
      * @param index Specifies the index of the generic vertex attribute to be bound.
      * @param name containing the name of the vertex shader attribute variable to which index is to be bound.
      */
     public abstract void bindAttributeLocation(int program,int index, String name);
 
     /**
      * Enable a vertex attribute array.
      * @param index
      */
     public abstract void enableVertexAttribArray(int index);
 
 
 }
 
