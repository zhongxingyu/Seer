 /* 
  * Copyright 2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.galactogolf.specificobjectmodel;
 
 import com.galactogolf.genericobjectmodel.BoundingBox;
 import com.galactogolf.genericobjectmodel.GameWorld;
 import com.galactogolf.genericobjectmodel.NonPlayerEntity;
 import com.galactogolf.genericobjectmodel.Vector2D;
 import com.galactogolf.genericobjectmodel.GameWorld.CollisionModes;
 import com.galactogolf.views.RenderInstructionBuffer;
 
 /***
  * Represents a 2d line barrier in space, which destroys the player if we hit it
  * 
  */
 public class BarrierEntity extends NonPlayerEntity {
 
 	/***
 	 * because the barrier is a long line, test for visibility by testing the
 	 * end points
 	 * 
 	 * TODO - this won't work with long lines!
 	 */
 	@Override
 	public boolean isVisibleAndInFrustrum(float cameraMinX, float cameraMinY,
 			float cameraMaxX, float cameraMaxY) {
 		if (_position1.x > cameraMinX && _position1.y > cameraMinY
 				&& _position1.x < cameraMaxX && _position1.y < cameraMaxY) {
 			return true;
 		}
 
 		else if (_position2.x > cameraMinX && _position2.y > cameraMinY
 				&& _position2.x < cameraMaxX && _position2.y < cameraMaxY)
 
 		{
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 	protected Vector2D _position1;
 
 	public Vector2D getPosition1() {
 		return _position1;
 	}
 
 	public void setPosition1(Vector2D position1) {
 		_position1 = position1;
 	}
 
 	public Vector2D getPosition2() {
 		return _position2;
 	}
 
 	public void setPosition2(Vector2D position2) {
 		_position2 = position2;
 	}
 
 	protected Vector2D _position2;
 
 	public BarrierEntity(String name, GameWorld world) {
 		super(name, world);
 		_position1 = new Vector2D();
 		_position2 = new Vector2D();
 		_boundingBox = new BoundingBox();
 	}
 
 	private float _timeSinceLastFrameChange = 100000.0f;
 
 	@Override
 	protected void updateAnimation(long timeSinceLastFrame) {
 		_timeSinceLastFrameChange += timeSinceLastFrame;
		if (_timeSinceLastFrameChange > 200.0) {
 			_currentFrame = (_currentFrame + 1) % 4;
 			_timeSinceLastFrameChange = 0;
 		}
 		calculateBoundingBox();
 
 	}
 
 	@Override
 	public float getAngle() {
 		_angle = (float) Vector2D.Sub(_position2, _position1).GetAngle();
 		return super.getAngle();
 	}
 
 	@Override
 	public Vector2D getPosition() {
 		_position.x = (_position1.x + _position2.x) / 2;
 		_position.y = (_position1.y + _position2.y) / 2;
 
 		return super.getPosition();
 	}
 
 	@Override
 	public void render(RenderInstructionBuffer instructionList,
 			long timeSinceLastFrame) {
 		// TODO Auto-generated method stub
 		updateAnimation(timeSinceLastFrame);
 		instructionList.AddRenderSimpleSpriteInstruction(getPosition().x,
 				getPosition().y, (float) (getAngle() * 180 / Math.PI), Vector2D
						.Sub(_position2, _position1).magnitude() / 128, 1.0f,
 				GetCurrentFrameSet(), getCurrentFrame(), GetRenderable());
 	}
 
 	@Override
 	public CollisionModes getCollisionMode() {
 		return CollisionModes.BOUNDING_BOX;
 	}
 
 	private void calculateBoundingBox() {
 		setBoundingSphereRadius(Vector2D.Sub(_position1, _position2)
 				.magnitude() / 2);
 		_boundingBox.setup(getPosition(), getBoundingSphereRadius(), 2,
 				getAngle());
 	}
 
 }
