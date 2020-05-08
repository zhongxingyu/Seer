 package com.inspedio.system.helper.extension;
 
 import com.inspedio.entity.primitive.InsCallback;
 
 /**
  * This is a helper class for animation handling in <code>InsAnimatedObject</code>. <br>
  * It represents Animation Sequence, Name, and Callback event
  * 
  * @author Hyude
  * @version 1.0
  */
 public class InsAnim {
 	/**
 	 * The name of Animation
 	 */
 	public String name;
 	/**
 	 * Total frames in animation
 	 */
 	public int frameCount;
 	/**
 	 * Animation current position of frame array
 	 */
 	public int currentFrame;
 	/**
 	 * Container of frame list
 	 */
 	public int[] frames;
 	/**
 	 * How much delay between frames
 	 */
 	public int frameDelay;
 
 	protected int currentDelay;
 	/**
 	 * Callback method to execute when animation sequence end.
 	 */
 	public InsCallback callback;
 	
 	/**
 	 * Construct a new InsAnim with given name and sequence
 	 * 
 	 * @param	Name			Animation name
 	 * @param	FrameSequence	Array of integer which represents frame sequence
 	 * @param	FrameDelay		Delay between frames, default is 0
 	 * @param	Callback		Callback method that called after animation finished
 	 * 
 	 */
 	public InsAnim(String Name, int[] FrameSequence, int FrameDelay, InsCallback Callback)
 	{
 		this.name = Name;
 		this.frames = FrameSequence;
 		this.frameDelay = FrameDelay;
 		this.currentFrame = 0;
 		this.frameCount = this.frames.length;
 		this.currentDelay = 0;
 		this.callback = Callback;
 	}
 	
 	/**
 	 * Return the next frame in sequence
 	 * 
 	 * @return	The next frame in sequence
 	 */
 	public int nextFrame()
 	{
 		if(this.currentFrame == this.frameCount-1){
 			if(this.callback != null){
 				this.callback.call();
 			}
 		}
		
 		if(this.currentDelay < this.frameDelay){
 			this.currentDelay++;
 		} else {
 			this.currentFrame = (this.currentFrame + 1) % this.frameCount;
			this.currentDelay = 0;
 		}
 		return this.frames[this.currentFrame];
 	}
 	
 	/**
 	 * Retrieve the current frame
 	 * 
 	 * @return	Current frame
 	 */
 	public int getFrame()
 	{
 		return this.frames[this.currentFrame];
 	}
 }
