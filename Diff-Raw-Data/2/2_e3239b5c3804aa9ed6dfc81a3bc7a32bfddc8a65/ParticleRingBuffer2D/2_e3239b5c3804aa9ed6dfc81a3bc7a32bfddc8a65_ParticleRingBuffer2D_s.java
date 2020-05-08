 package yang.graphics.particles;
 
 import yang.graphics.buffers.IndexedVertexBuffer;
 import yang.graphics.defaults.DefaultGraphics;
 import yang.graphics.programs.BasicProgram;
 
public abstract class ParticleRingBuffer2D<ParticleType extends Particle> extends AbstractParticleRingBuffer<DefaultGraphics,ParticleType> {
 
 	public BasicProgram mProgram;
 	public float mCelShadingX=1,mCelShadingY=1;
 
 	@Override
 	public ParticleRingBuffer2D<ParticleType> init(DefaultGraphics graphics,int maxParticleCount) {
 		super.init(graphics,maxParticleCount);
 		mProgram = (BasicProgram) graphics.getDefaultProgram();
 		return this;
 	}
 
 	public void setCelShading(float factor) {
 		mCelShadingX = factor;
 		mCelShadingY = factor;
 	}
 
 	@Override
 	protected abstract ParticleType createParticle();
 
 	@Override
 	protected void drawParticles() {
 		final IndexedVertexBuffer vertexBuffer = mGraphics.getCurrentVertexBuffer();
 		mGraphics.setShaderProgram(mProgram);
 		mGraphics.setBlack();
 		if(mCelShadingX!=1 && mCelShadingY!=1) {
 			for(final Object particleObj:mParticles) {
 				ParticleType particle = (ParticleType)particleObj;
 				if(particle.mExists && particle.mVisible) {
 					float uScale;
 					if(mScaleSpeed==0) {
 						uScale = 1;
 					}else{
 						if(mScaleLookUp!=null) {
 							uScale = mScaleLookUp.get(particle.mNormLifeTime*mScaleSpeed);
 						}else{
 							uScale = (1-particle.mNormLifeTime*mScaleSpeed);
 						}
 					}
 					vertexBuffer.beginQuad(false);
 					vertexBuffer.putRotatedRect3D(DefaultGraphics.ID_POSITIONS, uScale*mCelShadingX*particle.mScaleX, uScale*mCelShadingY*particle.mScaleY, particle.mPosX, particle.mPosY, particle.mPosZ, particle.mRotation);
 					vertexBuffer.putArray(DefaultGraphics.ID_TEXTURES, particle.mTextureCoordinates.mAppliedCoordinates);
 					vertexBuffer.putArray(DefaultGraphics.ID_COLORS, DefaultGraphics.RECT_BLACK);
 					vertexBuffer.putArray(DefaultGraphics.ID_SUPPDATA, DefaultGraphics.RECT_BLACK);
 				}
 			}
 		}
 
 		for(int i=mParticles.length-1;i>=0;i--) {
 			ParticleType particle = (ParticleType)mParticles[i];
 			if(particle.mExists && particle.mVisible) {
 				if(mAlphaSpeed==0) {
 					mGraphics.setColor(particle.mColor);
 				}else{
 					if(mAlphaLookUp!=null) {
 						mGraphics.setColor(particle.mColor[0],particle.mColor[1],particle.mColor[2],particle.mColor[3]*mAlphaLookUp.get(particle.mNormLifeTime*mAlphaSpeed));
 					}else{
 						mGraphics.setColor(particle.mColor[0],particle.mColor[1],particle.mColor[2],particle.mColor[3]*(1-particle.mNormLifeTime*mAlphaSpeed));
 					}
 				}
 
 				float uScale;
 				if(mScaleSpeed==0) {
 					uScale = 1;
 				}else{
 					if(mScaleLookUp!=null)
 						uScale = mScaleLookUp.get(particle.mNormLifeTime*mScaleSpeed);
 					else{
 						uScale = (1-particle.mNormLifeTime*mScaleSpeed);
 					}
 				}
 				vertexBuffer.beginQuad(false);
 				vertexBuffer.putRotatedRect3D(DefaultGraphics.ID_POSITIONS, uScale*particle.mScaleX, uScale*particle.mScaleY, particle.mPosX, particle.mPosY, particle.mPosZ, particle.mRotation);
 				vertexBuffer.putArray(DefaultGraphics.ID_TEXTURES, particle.mTextureCoordinates.mAppliedCoordinates);
 				vertexBuffer.putArrayMultiple(DefaultGraphics.ID_COLORS, mGraphics.mCurColor, 4);
 				vertexBuffer.putArray(DefaultGraphics.ID_SUPPDATA, DefaultGraphics.RECT_BLACK);
 			}
 		}
 	}
 
 }
