 package com.inspedio.entity.ui;
 
 
 public class InsHoldButton extends InsButton{
 
 	protected boolean focused;
 	protected boolean checked;
 	
 	public InsHoldButton(int X, int Y, int Width, int Height, String Caption, int Color) {
 		super(X, Y, Width, Height, Caption, Color);
 		this.focused = false;
 		this.checked = false;
 	}
 	
 	public boolean onPointerPressed(int X, int Y) {
 		if(this.isOverlap(X, Y)){
 			this.setFocus(true);
 			return onTouchPressed();
 		}
 		return false;
 	}
 
 	public boolean onPointerReleased(int X, int Y) {
 		if(this.isOverlap(X, Y)){
 			this.setFocus(false);
 			return onTouchReleased();
 		}
 		return false;
 	}
 
 	public boolean onPointerDragged(int X, int Y) {
 		if(this.isOverlap(X, Y)){
 			this.setFocus(true);
 			return onTouchDragged();
 		}
 		return false;
 	}
 	
 	public boolean onPointerHold(int X, int Y) {
 		if(this.isOverlap(X, Y)){
 			this.setFocus(true);
 			return onTouchHold();
 		}
 		return false;
 	}
 	
 	
 	public void setFocus(boolean focus){
 		if(!this.focused){
 			if(focus){
				this.focus();
 				this.focused = true;
 			}
 		} else {
 			if(!focus){
				this.unfocus();
 				this.focused = false;
 			}
 		}
 		this.checked = true;
 	}
	
	public void focus(){
		this.addSize(10, 10);
		this.borderWidth += 2;
	}
	
	public void unfocus(){
		this.addSize(-10, -10);
		this.borderWidth -= 2;
	}
	
 
 	public void checkHold(){
 		if(this.focused && !this.checked){
 			this.setFocus(false);
 		}
 		this.checked = false;
 	}
 	
 	public void update(){
 		super.update();
 		this.checkHold();
 	}
 }
