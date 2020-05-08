 package yang.graphics.stereovision;
 
 import yang.graphics.defaults.programs.MinimumTexShader;
 import yang.graphics.translator.AbstractGFXLoader;
 
 public class LensDistortionShader extends MinimumTexShader {
 	
 	public int mLensParametersHandle;
 	public int mScaleHandle;
 	//public float[] mLensParameters = new float[]{1.5f,1,1,1};
 	public float mScaleX = 0.3f,mScaleY = 0.35f;
 	public float[] mLensParameters = new float[]{1,0.22f,0.24f,1};
 	
 	public final static String VERTEX_SHADER = 
 			"attribute vec4 vPosition;\r\n" +
 			"attribute vec2 vTexture;\r\n" +	
 			"varying vec2 texCoord;\r\n" +
 			"\r\n" +
 			"void main() {\r\n" +
 			"	gl_Position = vPosition;\r\n" +
 			"	texCoord = vTexture;\r\n" +
 			"}\r\n";
 	
 	public final static String FRAGMENT_SHADER = 
 			"#ANDROID precision mediump float;\r\n" +
 			"const vec2 lensCenter = vec2(0.5,0.5);\r\n" +
 			"const float sampleDist = 1.5/2048.0;\r\n"+
 			"uniform vec2 scale;\r\n" +
 			"uniform sampler2D texSampler;\r\n" +
 			"uniform vec4 lensParameters;\r\n" +
 			"varying vec2 texCoord;\r\n" +
 			"\r\n" +
 			"void main() {\r\n" +
 			"	vec2 theta = (texCoord - lensCenter) * 2.0;\r\n" +
 			"   float rSq = theta.x * theta.x + theta.y * theta.y;\r\n" +
 			"   vec2 rvector = theta * (lensParameters.x + lensParameters.y*rSq + lensParameters.z*rSq*rSq + lensParameters.w*rSq*rSq*rSq);\r\n" +
//			"	gl_FragColor = texture2D(texSampler, lensCenter + scale*rvector);\r\n" +
 			"   vec2 resCoord = lensCenter + scale*rvector;\r\n" +
			"	gl_FragColor = texture2D(texSampler, resCoord)*0.3 + (texture2D(texSampler, vec2(resCoord.x-sampleDist,resCoord.y-sampleDist))+texture2D(texSampler, vec2(resCoord.x+sampleDist,resCoord.y-sampleDist))+texture2D(texSampler, vec2(resCoord.x-sampleDist,resCoord.y+sampleDist))+texture2D(texSampler, vec2(resCoord.x+sampleDist,resCoord.y+sampleDist)))*0.7*0.25;\r\n" +
 			"}\r\n";
 	
 	public void initHandles() {
 		super.initHandles();
 		mLensParametersHandle = mProgram.getUniformLocation("lensParameters");
 		mScaleHandle = mProgram.getUniformLocation("scale");
 	}
 	
 	@Override
 	protected String getVertexShader(AbstractGFXLoader gfxLoader) {
 		return VERTEX_SHADER;
 	}
 
 	@Override
 	protected String getFragmentShader(AbstractGFXLoader gfxLoader) {
 		return FRAGMENT_SHADER;
 	}
 	
 	public void activate() {
 		super.activate();
 		mProgram.setUniform4f(mLensParametersHandle, mLensParameters);
 		mProgram.setUniform2f(mScaleHandle, mScaleX, mScaleY);
 	}
 
 }
