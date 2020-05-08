 package com.skorulis.heli2.ui;
 
 import static forplay.core.ForPlay.*;
 
 import forplay.core.CanvasLayer;
 import forplay.core.Image;
 import forplay.core.Pointer;
 import forplay.core.ResourceCallback;
 
 public class Button implements Pointer.Listener,ResourceCallback<Image>{
 
 	public static final int STATE_NORMAL = 1;
 	public static final int STATE_PRESSED = 2;
 	public static final int STATE_DISABLED = 4;
 	
 	private int state;
 	private CanvasLayer layer;
 	private int textColor;
 	private String text;
 	private StateImage foreImage;
 	private StateImage bgImage;
 	private ClickHandler clickHandler;
 	private float transX,transY;
 	private boolean visible;
 	
 	public int textX,textY;
 	
 	
 	public Button(int width,int height) {
 		state = STATE_NORMAL;
 		layer = graphics().createCanvasLayer(width, height);
 		textColor = 0xFF000000;
 		visible = true;
 	}
 	
 	public void setTranslation(int x,int y) {
 	  this.transX = x; this.transY = y;
 	  layer.setTranslation(x, y);
 	}
 	
 	public CanvasLayer layer() {
 		return layer;
 	}
 	
 	public void setText(String text) {
 	  this.text = text;
 	  this.redraw();
 	}
 	
 	public void setTextColor(int color) {
 	  this.textColor = color;
 	}
 	
 private void redrawNowOrLater(Image image) {
 	if(image==null || image.isReady()) {
 		redraw();
 	} else {
 		image.addCallback(this);
 	}
 }
 	
 	public void setForeImage(Image image) {
 		this.foreImage=new StateImage(STATE_NORMAL, image);
 		redrawNowOrLater(foreImage.getImage(state));
 	}
 	
 	public void setForeImage(String path) {
 		this.foreImage = new StateImage(STATE_NORMAL,assetManager().getImage(path));
 		redrawNowOrLater(foreImage.getImage(state));
 	}
 	
 	public void setForeImage(int[] states,String[] paths) {
 		Image[] images = new Image[paths.length];
 		for(int i=0; i < images.length; ++i) {
 			images[i] = assetManager().getImage(paths[i]);
 		}
 		this.foreImage = new StateImage(states, images);
 		redrawNowOrLater(foreImage.getImage(state));
 	}
 	
 	public void setBgImage(Image image) {
 	  this.bgImage = new StateImage(STATE_NORMAL,image);
 	  redrawNowOrLater(bgImage.getImage(state));
 	}
 	
 	public void setBgImage(String path) {
 		this.bgImage = new StateImage(STATE_NORMAL,assetManager().getImage(path));
 		redrawNowOrLater(bgImage.getImage(state));
 	}
 	
 	public void setBgImage(int[] states,String[] paths) {
 		Image[] images = new Image[paths.length];
 		for(int i=0; i < images.length; ++i) {
 			images[i] = assetManager().getImage(paths[i]);
 		}
 		this.bgImage = new StateImage(states, images);
 		redrawNowOrLater(bgImage.getImage(state));
 	}
 	
 	public void setClickHandler(ClickHandler click) {
 	  this.clickHandler = click;
 	}
 	
 	private void redraw() {
 	  layer.canvas().clear();
	  if(!visible) {
		  return; //Nothing to draw
	  }
 	  if(bgImage!=null && bgImage.getImage(state)!=null) {
 	    layer.canvas().drawImage(bgImage.getImage(state), 0, 0, layer.canvas().width(), layer.canvas().height());
 	  }
 	  
 	  if(text!=null) {
 		  layer.canvas().setFillColor(textColor);
 		  layer.canvas().drawText(text, textX, textY);
 	  }
 	  if(foreImage!=null && foreImage.getImage(state)!=null) {
 		  layer.canvas().drawImage(foreImage.getImage(state), 0, 0, layer.canvas().width(), layer.canvas().height());
 	  }
 	}
 
   @Override
   public void onPointerStart(float x, float y) {
 	  int oldState = state;
 	  if(pointInButton(x, y)) {
 		  state = state | STATE_PRESSED;
 	  } else {
 		  state = state & ~STATE_PRESSED;
 	  }
 	  if(state!=oldState) {
 		  redraw();
 	  }
   }
 
   @Override
   public void onPointerEnd(float x, float y) {
 	int oldState = state;
 	state = state & ~STATE_PRESSED;
     if(pointInButton(x, y) && clickHandler!=null) {
       ClickEvent click = new ClickEvent();
       click.sender = this;
       clickHandler.onClick(click);
     }
     if(state!=oldState) {
     	redraw();
     }
   }
 
   public void setVisible(boolean visible) {
 	  this.visible = visible;
	  redraw();
   }
   
   public boolean visible() {
     return visible;
   }
   
   @Override
   public void onPointerDrag(float x, float y) {
     // TODO Auto-generated method stub
     
   }
   
   private boolean pointInButton(float x,float y) {
     if(!visible()) {
       return false;
     }
     return (x >= transX && y >= transY && x <=transX+layer.canvas().width() && y <= transY+layer.canvas().width());
   }
 
 @Override
 public void done(Image resource) {
 	redraw();
 }
 
 @Override
 public void error(Throwable err) {
 	// TODO Auto-generated method stub
 	
 }
 	
 	
 	
 }
