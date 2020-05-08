 package com.shoky.myapp.opengl;
 
 import android.opengl.GLES20;
 
 public class Shaders {
 	public final static String CURRENT_SHADER = "phong";
 	
     public final static String gouraudVertexShaderCode =
         "uniform mat4 uMVPMatrix;" +
         "uniform mat4 uMVMatrix;" +
        "uniform mat4 uNormalMatrix;" + // transpose of inverse of mvp matrix ( should be of mv? )
         "uniform vec4 uEcLightPos;" +
 
         "attribute vec4 aPosition;" +
         "attribute vec3 aNormal;" +
         "attribute vec4 aColor;" +
         "varying vec4 vColor;" +
                 
         "void main() {" +
         "  vec4 globalAmbient, ambient, diffuse, specular = vec4(0);" +
         "  vec3 ecPosition = vec3(uMVMatrix * aPosition);" +
        "  vec3 ecNormal = normalize(vec3(uMVMatrix * vec4(aNormal, 0.0)));" + // normal-matrix * vertex-normal
         
         "  vec3 lightDir = normalize(vec3(uEcLightPos) - ecPosition);" + // L = vertex-to-lightpos vector, in eye-coordinates 
         "  float NdotL = max(dot(ecNormal, lightDir), 0.0);" +
         
         "  if (NdotL > 0.0) { " +
         "	 vec3 eyeDir = -ecPosition;" + // vertex-to-eye vector, in eye-coordinates
         "    vec3 lightHalfVector = normalize(eyeDir + lightDir);" +
     	"    float NdotHV = max(dot(ecNormal, lightHalfVector), 0.0);" +
     	"    specular = vec4(0.2,0.3,0.5,1.0) * vec4(0.8,0.8,0.8,1.0) * pow(NdotHV, 16.0);" + // gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(NdotHV,gl_FrontMaterial.shininess);" +
     	"  }" +
     		
         "  globalAmbient = aColor * vec4(0.2, 0.2, 0.2, 1.0);" + // material-ambient * global-ambient
         "  ambient = aColor * vec4(0.2, 0.1, 0.1, 1.0);" + // material-ambient * light-ambient
 		"  diffuse = aColor * vec4(0.8, 0.7, 0.8, 1.0);" + // material-diffuse * light-diffuse
 		
 		"  vColor = globalAmbient + ambient + NdotL * diffuse + specular;" +
 
         "  gl_Position = uMVPMatrix * aPosition;" +
         "}";
     
     public final static String gouraudFragmentShaderCode =
         "precision mediump float;" +
         "varying vec4 vColor;" +
         "void main() {" +
         "  if (gl_FrontFacing) {" +
         "    gl_FragColor = vColor;" +
         "  }" +
         "}";
     
     
     
     public final static String phongVertexShaderCode =
             "uniform mat4 uMVPMatrix;" +
             "uniform mat4 uMVMatrix;" +
            "uniform mat4 uNormalMatrix;" + // transpose of inverse of mvp matrix
             "uniform vec4 uEcLightPos;" +
             "attribute vec4 aPosition;" +
             "attribute vec3 aNormal;" +
             "attribute vec4 aColor;" +
             "varying vec4 vDiffuse, vAmbient;" +
             "varying vec3 vNormal, vHalfVector;" +
             "varying vec3 vLightDir;" +
             "void main() {" +
             "  " + 
            "  vec3 ecPosition = vec3(uMVMatrix * aPosition);" +
            "  vLightDir = normalize(vec3(uEcLightPos) - ecPosition);" +
            //"  vec3 lightDir = normalize(vec3(uMVMatrix * vec4(uLightDir, 0.0))); " +
            "  vNormal = normalize(vec3(uMVMatrix * vec4(aNormal, 0.0)));" + // normal-matrix * vertex-normal
            "  vec3 eyeDir = -ecPosition;" +
            "  vHalfVector = normalize(eyeDir + vLightDir);" +
             
             "  vec4 globalAmbient = aColor * vec4(0.2, 0.2, 0.2, 1.0);" + // material-ambient * global-ambient
             "  vAmbient = globalAmbient + aColor * vec4(0.2, 0.1, 0.1, 1.0);" + // global + material-ambient * light-ambient
     		"  vDiffuse =  aColor * vec4(0.8, 0.7, 0.8, 1.0);" + // material-diffuse * light-diffuse
     		
             "  gl_Position = uMVPMatrix * aPosition;" +
             "}";
     
     public final static String phongFragmentShaderCode =
     		"uniform vec3 uFragShaderEcLightPos;" +
             "varying vec4 vDiffuse, vAmbient;" +
             "varying vec3 vNormal, vHalfVector;" +
             "varying vec3 vLightDir;" +
             "void main() {" +
             "  if (!gl_FrontFacing) { return; }" +
             "  vec3 lightDir = normalize(vLightDir);" +
             "  vec3 n = normalize(vNormal);" +
             "  vec4 color = vAmbient;" +
             "  float NdotL, NdotHV;" +
             
             "  NdotL = max(dot(n, lightDir), 0.0);" +
             "  if (NdotL > 0.0) {" +
             "    color += vDiffuse * NdotL;" +
             "    vec3 halfV = normalize(vHalfVector);" +
             "    NdotHV = max(dot(n,halfV), 0.0);" +
             "    color += vec4(0.2,0.3,0.5,1.0) * vec4(0.8,0.8,0.8,1.0) * pow(NdotHV, 16.0);" + //gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(NdotHV, gl_FrontMaterial.shininess);" +
             "  }" +
             
             "  gl_FragColor = color;" +
             "}";
 
 	
     public static int makeProgram() {    	
 	    // prepare shaders and OpenGL program
 	    int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
 	            "phong".equals(CURRENT_SHADER) ? Shaders.phongVertexShaderCode : Shaders.gouraudVertexShaderCode);
 	    int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
 	    		"phong".equals(CURRENT_SHADER) ? Shaders.phongFragmentShaderCode : Shaders.gouraudFragmentShaderCode);
 	
 	    int program = GLES20.glCreateProgram();             // create empty OpenGL Program
 	    GLES20.glAttachShader(program, vertexShader);   // add the vertex shader to program
 	    GLES20.glAttachShader(program, fragmentShader); // add the fragment shader to program
 	    GLES20.glLinkProgram(program);                  // create OpenGL program executables
 	    return program;
     }
 
     public static int loadShader(int type, String shaderCode){
 
         // create a vertex shader type (GLES20.GL_VERTEX_SHADER) or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
         int shader = GLES20.glCreateShader(type);
 
         // add the source code to the shader and compile it
         GLES20.glShaderSource(shader, shaderCode);
         GLES20.glCompileShader(shader);
 
         return shader;
     }
 
 }
