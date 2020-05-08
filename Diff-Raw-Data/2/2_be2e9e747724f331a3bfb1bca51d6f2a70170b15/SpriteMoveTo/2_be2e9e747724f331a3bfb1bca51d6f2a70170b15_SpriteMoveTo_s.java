 /*
 The MIT License (MIT)
 
 Copyright (c) 2013 devnewton <devnewton@bci.im>
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
 package org.geekygoblin.nedetlesmaki.game.components.visual;
 
 import im.bci.timed.OneShotTimedAction;
 
 import org.lwjgl.util.vector.Vector3f;
 
 /**
  * 
  * @author devnewton
  *
  */
 class SpriteMoveTo extends SpriteControl {
 	private final Vector3f to;
 	private final float duration;
 	private final Sprite sprite;
 	private Vector3f from;
 	private OneShotTimedAction action;
 	
 	SpriteMoveTo(Sprite sprite, Vector3f to, float duration) {
 		this.sprite = sprite;
 		this.to = to;
 		this.duration = duration;
 	}
 
 	@Override
 	public void update(float elapsedTime) {
 		if(null == action) {
 			this.from = new Vector3f(sprite.getPosition());
 			action = new OneShotTimedAction(duration);
 		}		
 		action.update(elapsedTime);
         Vector3f newPos;
         final float progress = action.getProgress();
         if (progress >= 1.0f) {
             newPos = to;
         } else {
             newPos = new Vector3f();
             Vector3f.sub(to, from, newPos);
             newPos.scale(progress);
             newPos.x += from.x;
             newPos.y += from.y;
             newPos.z += from.z;
         }
         sprite.getPosition().x = newPos.x;
         sprite.getPosition().y = newPos.y;
        sprite.getPosition().y = newPos.y;
 	}
 
 	@Override
 	public boolean isFinished() {
 		return action.getProgress() >= 1.0f;
 	}
 	
 
 }
