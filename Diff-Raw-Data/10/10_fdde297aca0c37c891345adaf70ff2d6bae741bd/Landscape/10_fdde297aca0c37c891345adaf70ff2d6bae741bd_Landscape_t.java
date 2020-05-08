 package com.skorulis.heli.client;
 
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.canvas.dom.client.CssColor;
 import com.google.gwt.user.client.Random;
 import com.skorulis.heli.base.RectBoundBox;
 import com.skorulis.heli.base.RenderComponent;
 import com.skorulis.heli.base.UpdateComponent;
 

 public class Landscape implements RenderComponent,UpdateComponent{
 
 	public static final int SEGMENT_WIDTH = 20;
 	public float slideRate;
 	
 	int width,height;
 	
 	int topHeight[],bottomHeight[],centerHeight[];
 	int boringTop,boringBottom;
 	
 	CssColor color = CssColor.make("rgba(8,180,75,1)");
 	CssColor bestColor = CssColor.make("rgba(45,55,175,1)");
 	
 	float slideF;
 	int slideI;
 	
 	private int shrink;
 	private boolean moveUp;
 	private int minGap;
 	private static final int ABSOLUTE_MINGAP = 80;
 	private static final int MIN_CENTERGAP = 100;
 	private int bestX;
 	
 	
 	public Landscape(int width,int height) {
 		this.width = width;
 		this.height = height;
 		int segs = (width/SEGMENT_WIDTH)+1;
 		topHeight = new int[segs];
 		bottomHeight = new int[segs];
 		centerHeight = new int[segs];
 		reset();
 	}
 	
 	public void reset() {
 		minGap = 200;
 		slideF = 0; slideI = 0;
 		shrink = -1;
 		slideRate = 50;
 		
 		topHeight[0] = 20;
 		bottomHeight[0] = 20;
 		for(int i=1; i < topHeight.length; ++i) {
 			generatePosition(i);
 		}
 		
 	}
 	
 	public void generatePosition(int position) {
 		int prev;
 		if(position==0) {
 			prev = topHeight.length-1;
 		} else {
 			prev = (position-1)%topHeight.length;
 		}
 		int prevTop = topHeight[prev];
 		int prevBottom = bottomHeight[prev];
 		int chance = (95-boringBottom-boringTop);
 		if(Random.nextInt(100)>=chance) {
 			moveUp = !moveUp;
 		}
 		if(shrink==-1) {
 			if(Random.nextInt(100)>=50) {
 				shrink = 1;
 			}
 		} else {
 			if(Random.nextInt(100)>=95) {
 				shrink = -1;
 			}
 		}
 		
 		
 		
 		topHeight[position] = Math.min(Math.max(prevTop + Random.nextInt(20)*shrink,0),height-minGap);
 		bottomHeight[position] = Math.min(Math.max(prevBottom + Random.nextInt(20)*shrink,0),height-minGap);
 		int total = topHeight[position] + bottomHeight[position]; 
 		if(total > height-minGap) {
 			if(moveUp) {
 				topHeight[position]-=(total- (height-minGap));
 			} else {
 				bottomHeight[position]-=(total- (height-minGap));
 			}
 		}
 		
 		if(Random.nextInt(100) >=chance && slideI > 20) {
 			int gap = (height - bottomHeight[position] - topHeight[position] -MIN_CENTERGAP);
 			centerHeight[position] = Math.max(0, Random.nextInt(gap));
 		} else {
 			centerHeight[position] = 0;
 		}
 		if(topHeight[position]==topHeight[prev]) {
 			boringTop++;
 		} else {
 			boringTop = 0;
 		}
 		
 		if(bottomHeight[position]==bottomHeight[prev]) {
 			boringBottom++;
 		} else {
 			boringBottom = 0;
 		}
 	}
 	
 	
 	@Override
 	public void render(Context2d context) {
 		context.setFillStyle(color);
 		int pos;
 		int bestPos = -1;
 		int bestI=-1;
 		for(int i=0; i < topHeight.length; ++i) {
 			pos = (i+slideI)%topHeight.length;
 			if(bestPos<0 && isPosBestScore(i+slideI)) {
 				bestPos = pos;
 				bestI = i;
 			} else {
 				context.fillRect(i*SEGMENT_WIDTH - slideF-1, 0, SEGMENT_WIDTH+2, topHeight[pos]);
 				context.fillRect(i*SEGMENT_WIDTH - slideF-1, height-bottomHeight[pos], SEGMENT_WIDTH+2, bottomHeight[pos]);
 				if(centerHeight[pos] > 0) {
 					int gap = (height-bottomHeight[pos]-topHeight[pos]-centerHeight[pos])/2;
 					context.fillRect(i*SEGMENT_WIDTH - slideF-1, topHeight[pos]+gap, SEGMENT_WIDTH+2, centerHeight[pos]);
 				}
 				
 			}	
 		}
 		context.fill();
 		if(bestPos >=0) {
 			context.setFillStyle(bestColor);
 			context.fillRect(bestI*SEGMENT_WIDTH - slideF-1, 0, SEGMENT_WIDTH+2, topHeight[bestPos]);
 			context.fillRect(bestI*SEGMENT_WIDTH - slideF-1, height-bottomHeight[bestPos], SEGMENT_WIDTH+2, bottomHeight[bestPos]);
 			context.fill();
 		}
 	}
 	
 	public int getPos(int x) {
 		return (x/SEGMENT_WIDTH+slideI)%topHeight.length;
 	}
 
 	@Override
 	public void update(float delta) {
 		slideF+=delta*slideRate;
 		if(slideF > SEGMENT_WIDTH) {
 			slideF-=SEGMENT_WIDTH;
 			generatePosition(slideI%topHeight.length);
 			slideI++;
 			if(slideI%5==0 && minGap > ABSOLUTE_MINGAP) {
 				minGap--;
 				slideRate++;
 			}
 		}
 	}
 	
 	public boolean collides(RectBoundBox box) {
 		int x =(int) box.x;
 		int pos;
 		while(x < box.x+box.width) {
 			pos = getPos(x);
 			int top = topHeight[pos];
 			int bottom = bottomHeight[pos];
 			int center = centerHeight[pos];
 			if(box.y < top) {
 				return true;
 			}
 			if(box.y+box.height > height-bottom) {
 				return true;
 			}
 			if(center > 0) {
 				int gap = (height-bottom-center-top)/2;
 				int cT = top+gap;
 				int cB = top+gap+center;
 				if(cT < box.y+box.height && cB > box.y) {
 					return true;
 				}
 			}
 			x+=SEGMENT_WIDTH;
 		}
 		return false;
 	}
 	
 	public boolean isPosBestScore(int x) {
 		return bestX == x;
 	}
 	
 	public void setBestScore(float score) {
 		bestX = ((int)score)/SEGMENT_WIDTH;
 	}
 
 }
