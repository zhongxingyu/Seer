 package frontEnd;
 
 import util.Global;
 
 public class Item {
 	private int x,y;
 	private int height,width;
 	private int image;
 	
 	public Item(int _image, int _height, int _width){
 		image=_image;
 		height=_height;
 		width=_width;
		
		//x = 50;
		//y = 50;
 	}
 	public int getX(){return x;}
 	public int getY(){return y;}
 	public int getHeight(){return height;}
 	public int getWidth(){return width;}
 	
 	public void init(){}
 	public void update(int _x,int _y){
 		x=_x;
 		y=_y;
 	}
 }
