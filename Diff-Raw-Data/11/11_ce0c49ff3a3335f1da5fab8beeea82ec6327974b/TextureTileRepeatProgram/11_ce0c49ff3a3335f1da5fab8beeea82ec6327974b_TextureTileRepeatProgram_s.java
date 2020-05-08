 package yang.graphics.defaults.programs;
 
 import yang.graphics.programs.BasicProgram;
 import yang.graphics.translator.AbstractGFXLoader;
 
 public class TextureTileRepeatProgram extends BasicProgram{
 	
 	public final static String VERTEX_SHADER = 
 			"uniform mat4 projTransform;\n" +
 			"uniform float time;\n" +
 			"attribute vec4 vPosition;\n" +
 			"attribute vec2 vTexture;\n" +
 			"attribute vec4 vColor;\n" +
 			"attribute vec4 vTexBounds;" +
 			"varying vec2 texCoord;\n" +
 			"varying vec4 color;\n" +
 			"varying vec4 texBounds;\n" +
 			"\n" +
 			"void main() {\n" +
 			"	gl_Position = projTransform * vPosition;\n" +
 			"	texCoord = vec2(vTexture.x,vTexture.y);\n" +
 			"	color = vColor;\n" +
 			"	texBounds = vTexBounds;\n" +
 			"}\n";
 	
 	public final static String FRAGMENT_SHADER = 
 			"#ANDROID precision mediump float;\n" +
 			"uniform sampler2D texSampler;\n" +
			"uniform vec4 ambientColor;\n" +
 			"varying vec2 texCoord;\n" +
 			"varying vec4 color;\n" +
 			"varying vec4 texBounds;\n" +
 			"\n" +
 			"void main() {\n" +
			"	gl_FragColor = (texture2D(texSampler, vec2(mod(texCoord.x,texBounds[2])+texBounds[0],mod(texCoord.y,texBounds[3])+texBounds[1])) * color) * ambientColor;\n" +
 			//"	gl_FragColor = (vec4(mod(texCoord.x,texBounds[2])+texBounds[0],mod(texCoord.y,texBounds[3])+texBounds[1],0,1) * color) * ambientColor;\n" +
 			//"	gl_FragColor = (texture2D(texSampler, vec2(mod(texCoord.x,texBounds[2])+texBounds[0],mod(texCoord.y,texBounds[3])+texBounds[1])) * color) * ambientColor * vec4(mod(texCoord.x,texBounds[2]),mod(texCoord.y,texBounds[3]),1,1);\n" +
 			"}\n";
 	
 	@Override
 	protected String getSuppDataIdentifier() {
 		return "vTexBounds";
 	}
 	
 	@Override
 	protected String getVertexShader(AbstractGFXLoader gfxLoader) {
 		return VERTEX_SHADER;
 	}
 
 	@Override
 	protected String getFragmentShader(AbstractGFXLoader gfxLoader) {
 		return FRAGMENT_SHADER;
 	}
 
 }
