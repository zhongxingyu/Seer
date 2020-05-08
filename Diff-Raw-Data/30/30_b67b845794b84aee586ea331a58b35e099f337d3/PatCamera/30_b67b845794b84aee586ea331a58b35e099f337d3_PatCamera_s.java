 package com.deeter;
 
 import java.nio.FloatBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.GLContext;
 import org.lwjgl.util.glu.GLU;
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 
 import com.deeter.utility.Camera;
 import com.deeter.utility.MatrixUtils;
 
 import static java.lang.Math.*;
 import static org.lwjgl.opengl.ARBDepthClamp.GL_DEPTH_CLAMP;
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL20.glUniformMatrix4;
 
 public class PatCamera implements Camera {
 	
 	private float x = 0;
 	private float y = 0;
 	private float z = 0;
 	private float pitch = 0;
 	private float yaw = 0;
 	private float roll = 0;
 	private float fov = 0;
 	private float aspectRatio = 1;
 	private float zNear;
 	private float zFar;
 	private Matrix4f projectionMatrix;
 	private Matrix4f viewMatrix;
 	private FloatBuffer matrix44Buffer = null;
 	
 	public PatCamera(Builder builder) {
 		this.x = builder.x;
 		this.y = builder.y;
 		this.z = builder.z;
 		this.pitch = builder.pitch;
 		this.yaw = builder.yaw;
 		this.roll = builder.roll;
 		this.aspectRatio = builder.aspectRatio;
 		this.zNear = builder.zNear;
 		this.zFar = builder.zFar;
 		this.fov = builder.fov;
 		this.matrix44Buffer = BufferUtils.createFloatBuffer(16);
 	}
 	
 	public PatCamera() {
 		this.zNear = 0.3f;
 		this.zFar = 100;
 	}
 	
 	public PatCamera(float aspectRatio) {
 		if (aspectRatio <= 0) {
 			throw new IllegalArgumentException("aspectRatio " + aspectRatio + " was less than or equal to 0");
 		}
 		this.aspectRatio = aspectRatio;
 		this.zNear = 0.3f;
 		this.zFar = 100f;
 	}
 	
 	public PatCamera(float aspectRatio, float x, float y, float z) {
 		this(aspectRatio);
 		this.x = x;
 		this.y = y;
 		this.z = z;
 	}
 	
 	public PatCamera(float aspectRatio, float x, float y, float z, float pitch, float yaw, float roll) {
 		this(aspectRatio, x, y, z);
 		this.pitch = pitch;
 		this.yaw = yaw;
 		this.roll = roll;
 	}
 	
 	public PatCamera(float aspectRatio, float x, float y, float z, float pitch, float yaw, float roll, float zNear, float zFar) {
 		this(aspectRatio, x, y, z, pitch, yaw, roll);
 		if (zNear < 0) {
 			throw new IllegalArgumentException("zNear " + zNear + " was less than 0");
 		}
 		if (zFar <= zNear) {
 			throw new IllegalArgumentException("zFar " + zFar + " was less than or equal to zNear " + zNear);
 		}
 		this.zNear = zNear;
 		this.zFar = zFar;
 	}
 
 	@Override
 	public void processMouse() {
 		final float MAX_LOOK_UP = 90;
 		final float MAX_LOOK_DOWN = -90;
 		float mouseDX = Mouse.getDX() * 0.16f;
 		float mouseDY = Mouse.getDY() * 0.16f;
 		if (yaw + mouseDX >= 360) {
 			yaw = yaw + mouseDX - 360;
 		}
 		else if (yaw + mouseDX < 0) {
 			yaw = 360 - yaw + mouseDX;
 		}
 		else {
 			yaw += mouseDX;
 		}
 		if (pitch - mouseDY >= MAX_LOOK_DOWN
 				&& pitch - mouseDY <= MAX_LOOK_UP) {
 			pitch += -mouseDY;
 		}
 		else if (pitch - mouseDY < MAX_LOOK_DOWN) {
 			pitch = MAX_LOOK_DOWN;
 		}
 		else if (pitch - mouseDY > MAX_LOOK_UP) {
 			pitch = MAX_LOOK_UP;
 		}
 	}
 
 	@Override
 	public void processMouse(float mouseSpeed) {
 		final float MAX_LOOK_UP = 90;
 		final float MAX_LOOK_DOWN = -90;
 		float mouseDX = Mouse.getDX() * mouseSpeed * 0.16f;
 		float mouseDY = Mouse.getDY() * mouseSpeed * 0.16f;
 		if (yaw + mouseDX >= 360) {
 			yaw = yaw + mouseDX - 360;
 		}
 		else if (yaw + mouseDX < 0) {
 			yaw = 360 - yaw + mouseDX;
 		}
 		else {
 			yaw += mouseDX;
 		}
 		if (pitch - mouseDY >= MAX_LOOK_DOWN
 				&& pitch - mouseDY <= MAX_LOOK_UP) {
 			pitch += -mouseDY;
 		}
 		else if (pitch - mouseDY < MAX_LOOK_DOWN) {
 			pitch = MAX_LOOK_DOWN;
 		}
 		else if (pitch - mouseDY > MAX_LOOK_UP) {
 			pitch = MAX_LOOK_UP;
 		}
 	}
 
 	@Override
 	public void processMouse(float mouseSpeed, float maxLookUp, float maxLookDown) {
 		float mouseDX = Mouse.getDX() * mouseSpeed * 0.16f;
 		float mouseDY = Mouse.getDY() * mouseSpeed * 0.16f;
 		if (yaw + mouseDX >= 360) {
 			yaw = yaw + mouseDX - 360;
 		}
 		else if (yaw + mouseDX < 0) {
 			yaw = 360 - yaw + mouseDX;
 		}
 		else {
 			yaw += mouseDX;
 		}
 		if (pitch - mouseDY >= maxLookDown
 				&& pitch - mouseDY <= maxLookUp) {
 			pitch += -mouseDY;
 		}
 		else if (pitch - mouseDY < maxLookDown) {
 			pitch = maxLookDown;
 		}
 		else if (pitch - mouseDY > maxLookUp) {
 			pitch = maxLookUp;
 		}
 	}
 
 	@Override
 	public void processKeyboard(float delta) {
 		boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W);
 		boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S);
 		boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A);
 		boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_RIGHT)|| Keyboard.isKeyDown(Keyboard.KEY_D);
 		boolean flyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
 		boolean flyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
 		
 		if (keyUp && keyRight && !keyLeft && !keyDown) {
             moveFromLook(delta * 0.003f, 0, -delta * 0.003f);
         }
         if (keyUp && keyLeft && !keyRight && !keyDown) {
             moveFromLook(-delta * 0.003f, 0, -delta * 0.003f);
         }
         if (keyUp && !keyLeft && !keyRight && !keyDown) {
             moveFromLook(0, 0, -delta * 0.003f);
         }
         if (keyDown && keyLeft && !keyRight && !keyUp) {
             moveFromLook(-delta * 0.003f, 0, delta * 0.003f);
         }
         if (keyDown && keyRight && !keyLeft && !keyUp) {
             moveFromLook(delta * 0.003f, 0, delta * 0.003f);
         }
         if (keyDown && !keyUp && !keyLeft && !keyRight) {
             moveFromLook(0, 0, delta * 0.003f);
         }
         if (keyLeft && !keyRight && !keyUp && !keyDown) {
             moveFromLook(-delta * 0.003f, 0, 0);
         }
         if (keyRight && !keyLeft && !keyUp && !keyDown) {
             moveFromLook(delta * 0.003f, 0, 0);
         }
         if (flyUp && !flyDown) {
             y += delta * 0.003f;
         }
         if (flyDown && !flyUp) {
             y -= delta * 0.003f;
         }
 	}
 
 	@Override
 	public void processKeyboard(float delta, float speed) {
         boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W);
         boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S);
         boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A);
         boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D);
         boolean flyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
         boolean flyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
 
         if (keyUp && keyRight && !keyLeft && !keyDown) {
             moveFromLook(speed * delta * 0.003f, 0, -speed * delta * 0.003f);
         }
         if (keyUp && keyLeft && !keyRight && !keyDown) {
             moveFromLook(-speed * delta * 0.003f, 0, -speed * delta * 0.003f);
         }
         if (keyUp && !keyLeft && !keyRight && !keyDown) {
             moveFromLook(0, 0, -speed * delta * 0.003f);
         }
         if (keyDown && keyLeft && !keyRight && !keyUp) {
             moveFromLook(-speed * delta * 0.003f, 0, speed * delta * 0.003f);
         }
         if (keyDown && keyRight && !keyLeft && !keyUp) {
             moveFromLook(speed * delta * 0.003f, 0, speed * delta * 0.003f);
         }
         if (keyDown && !keyUp && !keyLeft && !keyRight) {
             moveFromLook(0, 0, speed * delta * 0.003f);
         }
         if (keyLeft && !keyRight && !keyUp && !keyDown) {
             moveFromLook(-speed * delta * 0.003f, 0, 0);
         }
         if (keyRight && !keyLeft && !keyUp && !keyDown) {
             moveFromLook(speed * delta * 0.003f, 0, 0);
         }
         if (flyUp && !flyDown) {
             y += speed * delta * 0.003f;
         }
         if (flyDown && !flyUp) {
             y -= speed * delta * 0.003f;
         }
 	}
 
 	@Override
 	public void processKeyboard(float delta, float speedX, float speedY, float speedZ) {
         boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W);
         boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S);
         boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A);
         boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D);
         boolean flyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
         boolean flyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
 
         if (keyUp && keyRight && !keyLeft && !keyDown) {
             moveFromLook(speedX * delta * 0.003f, 0, -speedZ * delta * 0.003f);
         }
         if (keyUp && keyLeft && !keyRight && !keyDown) {
             moveFromLook(-speedX * delta * 0.003f, 0, -speedZ * delta * 0.003f);
         }
         if (keyUp && !keyLeft && !keyRight && !keyDown) {
             moveFromLook(0, 0, -speedZ * delta * 0.003f);
         }
         if (keyDown && keyLeft && !keyRight && !keyUp) {
             moveFromLook(-speedX * delta * 0.003f, 0, speedZ * delta * 0.003f);
         }
         if (keyDown && keyRight && !keyLeft && !keyUp) {
             moveFromLook(speedX * delta * 0.003f, 0, speedZ * delta * 0.003f);
         }
         if (keyDown && !keyUp && !keyLeft && !keyRight) {
             moveFromLook(0, 0, speedZ * delta * 0.003f);
         }
         if (keyLeft && !keyRight && !keyUp && !keyDown) {
             moveFromLook(-speedX * delta * 0.003f, 0, 0);
         }
         if (keyRight && !keyLeft && !keyUp && !keyDown) {
             moveFromLook(speedX * delta * 0.003f, 0, 0);
         }
         if (flyUp && !flyDown) {
             y += speedY * delta * 0.003f;
         }
         if (flyDown && !flyUp) {
             y -= speedY * delta * 0.003f;
         }
 	}
 
 	@Override
 	public void moveFromLook(float dx, float dy, float dz) {
 		this.z += dx * (float) cos(toRadians(yaw - 90)) + dz * cos(toRadians(yaw));
 		this.x -= dx * (float) sin(toRadians(yaw - 90)) + dz * sin(toRadians(yaw));
 		this.y += dy * (float) sin(toRadians(pitch - 90)) + dz * sin(toRadians(pitch));
 	}
 
 	@Override
 	public void setPosition(float x, float y, float z) {
 		this.x = x;
 		this.y = y;
 		this.z = z;
 	}
 	
 	@Override
 	public void setRotation(float pitch, float yaw, float roll) {
 		this.pitch = pitch;
 		this.yaw = yaw;
 		this.roll = roll;
 	}
 	
 	@Override
 	public void applyOptimalStates() {
 		if (GLContext.getCapabilities().GL_ARB_depth_clamp) {
             glEnable(GL_DEPTH_CLAMP);
         }
 	}
 	
 	public void applyPerspectiveMatrix() {
 		glPushAttrib(GL_TRANSFORM_BIT);
         glMatrixMode(GL_PROJECTION);
         glLoadIdentity();
         GLU.gluPerspective(fov, aspectRatio, zNear, zFar);
         glPopAttrib();
 	}
 
 	@Override
 	public void applyPerspectiveMatrix(int uniformLocation) {
 		projectionMatrix = MatrixUtils.createProjectionMatrix(this.fov, this.aspectRatio, this.zNear, this.zFar);
 		projectionMatrix.store(matrix44Buffer); matrix44Buffer.flip();
 		glUniformMatrix4(uniformLocation, false, matrix44Buffer);
 	}
 	
 	public void applyTranslations() {
 		glPushAttrib(GL_TRANSFORM_BIT);
         glMatrixMode(GL_MODELVIEW);
         glRotatef(pitch, 1, 0, 0);
         glRotatef(yaw, 0, 1, 0);
         glRotatef(roll, 0, 0, 1);
         glTranslatef(-x, -y, -z);
         glPopAttrib();
 	}
 	
 	@Override
 	public void applyTranslations(int uniformLocation) {
 		viewMatrix = new Matrix4f();
 		Matrix4f.rotate(MatrixUtils.degreesToRadians(pitch), new Vector3f(1, 0, 0), viewMatrix, viewMatrix);
 		Matrix4f.rotate(MatrixUtils.degreesToRadians(yaw), new Vector3f(0, 1, 0), viewMatrix, viewMatrix);
 		Matrix4f.rotate(MatrixUtils.degreesToRadians(roll), new Vector3f(0, 0, 1), viewMatrix, viewMatrix);
 		Matrix4f.translate(new Vector3f(-x, -y, -z), viewMatrix, viewMatrix);
 		viewMatrix.store(matrix44Buffer); matrix44Buffer.flip();
 		glUniformMatrix4(uniformLocation, false, matrix44Buffer);
 	}
 
 	@Override
 	public float x() {
 		return x;
 	}
 
 	@Override
 	public float y() {
 		return y;
 	}
 
 	@Override
 	public float z() {
 		return z;
 	}
 
 	@Override
 	public float pitch() {
 		return pitch;
 	}
 
 	@Override
 	public float yaw() {
 		return yaw;
 	}
 
 	@Override
 	public float roll() {
 		return roll;
 	}
 
 	@Override
 	public float fieldOfView() {
 		return fov;
 	}
 
 	@Override
 	public void setFieldOfView(float fov) {
 		this.fov = fov;
 	}
 
 	@Override
 	public void setAspectRatio(float aspectRatio) {
 		 if (aspectRatio <= 0) {
 			 throw new IllegalArgumentException("aspectRatio " + aspectRatio + " is 0 or less");
 		 }
 		 this.aspectRatio = aspectRatio;
 	}
 
 	@Override
 	public float aspectRatio() {
 		return aspectRatio;
 	}
 
 	@Override
 	public float nearClippingPlane() {
 		return zNear;
 	}
 
 	@Override
 	public float farClippingPlane() {
 		return zFar;
 	}
 	
 	@Override
     public String toString() {
         return "PatCamera [x=" + x + ", y=" + y + ", z=" + z + ", pitch=" + pitch
                 + ", yaw=" + yaw + ", roll=" + roll + ", fov=" + fov
                 + ", aspectRatio=" + aspectRatio + ", zNear=" + zNear
                 + ", zFar=" + zFar + "]";
     }
 	
 	public static class Builder {
 		private float aspectRatio = 1;
 		private float x = 0,
 				y = 0,
 				z = 0,
 				pitch = 0,
 				yaw = 0,
 				roll = 0;
 		private float zNear = 0.3f;
 		private float zFar = 100;
 		private float fov = 90;
 		
 		public Builder() {}
 		
 		public Builder setAspectRatio(float aspectRatio) {
 			if (aspectRatio <= 0) {
 				throw new IllegalArgumentException("aspectRatio " + aspectRatio + " was less than or equal to 0");
 			}
 			this.aspectRatio = aspectRatio;
 			return this;
 		}
 		
 		public Builder setNearClippingPlane(float nearClippingPlane) {
 			if (nearClippingPlane <= 0) {
 				throw new IllegalArgumentException("nearClippingPlane " + nearClippingPlane + " was less than or equal to 0");
 			}
 			this.zNear = nearClippingPlane;
 			return this;
 		}
 		
 		public Builder setFarClippingPlane(float farClippingPlane) {
 			if (farClippingPlane <= 0) {
 				throw new IllegalArgumentException("farClippingPlane " + farClippingPlane + " was less than or equal to 0");
 			}
 			this.zFar = farClippingPlane;
 			return this;
 		}
 		
 		public Builder setFieldOfView(float fov) {
 			this.fov = fov;
 			return this;
 		}
 		
 		public Builder setPosition(float x, float y, float z) {
 			this.x = x;
 			this.y = y;
 			this.z = y;
 			return this;
 		}
 		
 		public Builder setRotation(float pitch, float yaw, float roll) {
 			this.pitch = pitch;
 			this.yaw = yaw;
 			this.roll = roll;
 			return this;
 		}
 		
 		public PatCamera build() {
 			if (zFar <= zNear) {
 				throw new IllegalArgumentException("farClippingPlane " + this.zFar + " is less than or equal to nearClippingPlane " + this.zNear);
 			}
 			return new PatCamera(this);
 		}
 	}
 
 }
