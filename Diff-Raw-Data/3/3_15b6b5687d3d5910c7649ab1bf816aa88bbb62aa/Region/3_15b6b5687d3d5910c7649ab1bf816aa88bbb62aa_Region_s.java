 package org.fluent2d;
 
 @SuppressWarnings("unchecked")
 public class Region<T extends Region<?>> {
 
 	private int top;
 	private int left;
 	private int bottom;
 	private int right;
 	private Region<?> parent;
 
 	public Region(int top, int left, int bottom, int right) {
 		this.top = top;
 		this.left = left;
 		this.bottom = bottom;
 		this.right = right;
 		parent = this;
 	}
 
 	public Region(Region<?> parentRegion) {
 		this.parent = parentRegion;
 		this.top = parentRegion.top();
 		this.left = parentRegion.left();
 		this.bottom = parentRegion.bottom();
 		this.right = parentRegion.right();
 	}
 
 	protected Region() {
 	}
 
 	protected void setParent(Region<?> parent) {
 		this.parent = parent;
 	}
 
 	// gets the absolute value of top
 	public int top() {
 		return this.top;
 	}
 
 	// sets the top as a percentage
 	public T top(float topP) {
 		top = MathUtil.getPoint(topP, parent.top(), parent.bottom());
 		return (T) this;
 	}
 
 	// sets the top in pixels relative to the parent
 	public T top(int topV) {
 		top = topV + parent.top();
 		return (T) this;
 	}
 
 	// sets the top value in absolute pixels, ignoring parent dimensions
 	public T topA(int topV) {
 		top = topV;
 		return (T) this;
 	}
 
 	// sets the top value in absolte pixels relative to it's current value
 	public T topR(int topR) {
 		top = top + topR;
 		return (T) this;
 	}
 
 	public int left() {
 		return this.left;
 	}
 
 	// sets the left as a percentage
 	public T left(float leftP) {
 		top = MathUtil.getPoint(leftP, parent.left(), parent.right());
 		return (T) this;
 	}
 
 	// sets the left in pixels relative to the parent
 	public T left(int leftV) {
 		left = leftV + parent.left();
 		return (T) this;
 	}
 
 	// sets the left value in absolute pixels, ignoring parent dimensions
 	public T leftA(int leftV) {
 		left = leftV;
 		return (T) this;
 	}
 
 	// sets the left value in absolte pixels relative to it's current value
 	public T leftR(int leftV) {
 		left = leftV + left;
 		return (T) this;
 	}
 
 	public int bottom() {
 		return this.bottom;
 	}
 
 	// sets the bottom in absolute pixels
 	public T bottom(int bottom) {
 		this.bottom = bottom;
 		return (T) this;
 	}
 
 	public T bottom(float bottom) {
		bottom = (int) ((parent.bottom() - parent.top()) * bottom)
				+ parent.top();
 		return (T) this;
 	}
 
 	// sets the bottom in absolute pixels relative to it's current value
 	public T bottomR(int bottom) {
 		this.bottom = this.bottom + bottom;
 		return (T) this;
 	}
 
 	public int right() {
 		return this.right;
 	}
 
 	// sets the right value in absolute pixels
 	public T right(int right) {
 		this.right = right;
 		return (T) this;
 	}
 
 	// sets the right value in absolute pixels relative to it's current value
 	public T rightR(int right) {
 		this.right = this.right + right;
 		return (T) this;
 	}
 
 	public T width(float widthP) {
 		right = MathUtil.getPoint(widthP, parent.left(), parent.right()) + left;
 		return (T) this;
 	}
 
 	public T height(float heightP) {
 		right = MathUtil.getPoint(heightP, parent.top(), parent.bottom()) + top;
 		return (T) this;
 	}
 
 	public T centerHorizontal() {
 		int offset = MathUtil.getCentreOffset(parent.left(), parent.right(),
 				left(), right());
 		left(offset);
 		right(offset);
 		return (T) this;
 	}
 
 }
