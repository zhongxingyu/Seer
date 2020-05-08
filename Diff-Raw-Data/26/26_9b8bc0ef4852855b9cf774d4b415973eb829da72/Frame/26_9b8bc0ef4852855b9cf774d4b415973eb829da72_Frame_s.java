 package graphics;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.common.collect.ImmutableList;
 
 import math.Vector2;
 import utils.Rectangle;
 
 public class Frame {
 
 	private Rectangle sourceRect;
 	private Vector2 origin;
 
 	public Frame(Rectangle sourceRect, Vector2 origin) {
 		this.sourceRect = sourceRect;
 		this.origin = origin;
 	}
 
 	public Rectangle getSourceRect() {
 		return sourceRect;
 	}
 	public void setSourceRect(Rectangle sourceRect) {
 		this.sourceRect = sourceRect;
 	}
 	public Vector2 getOrigin() {
 		return origin;
 	}
 	public void setOrigin(Vector2 origin) {
 		this.origin = origin;
 	}	
 
 	public static ImmutableList<Frame> generateFrames(Vector2 startPos, Vector2 Dimensions, int numFrames, Boolean mirrored){
 		List<Frame> frames = new ArrayList<Frame>();
		for (int i=numFrames; i>0;i--){
 			frames.add(new Frame(new Rectangle((int)(startPos.X + Dimensions.X*i), (int)startPos.Y, (int)Dimensions.X, (int)Dimensions.Y),
 					   new Vector2(Dimensions.X/2, Dimensions.Y/2)));
 		}
 		return ImmutableList.copyOf(frames);
 	}
 }
