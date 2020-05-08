 package ratson.genimageexplorer.extra;
 
 import java.awt.image.renderable.RenderContext;
 
 import ratson.genimageexplorer.ObservationArea;
 import ratson.genimageexplorer.generators.Function;
 import ratson.genimageexplorer.generators.FunctionFactory;
 import ratson.genimageexplorer.generators.Renderer;
 import ratson.utils.FloatMatrix;
 import ratson.utils.Utils;
 
 public class HilbertUnwrap implements FunctionFactory{
 
 	private double x0, y0, z0, r02;
 	
 	public HilbertUnwrap(){
 		x0=0.3;
 		y0=0.4;
 		z0=0.36;
 		r02=Utils.sqr(0.24);
 	}
 	
 
 	public double getX0() {
 		return x0;
 	}
 	public void setX0(double x0) {
 		this.x0 = x0;
 	}
 	public double getY0() {
 		return y0;
 	}
 	public void setY0(double y0) {
 		this.y0 = y0;
 	}
 	public double getZ0() {
 		return z0;
 	}
 	public void setZ0(double z0) {
 		this.z0 = z0;
 	}
 	public double getR() {
 		return Math.sqrt(r02);
 	}
 	public void setR(double r) {
 		r02 = r*r;
 	}
 	
 	class Func extends Function{
 		Func(){
 			
 		}
 		public double x,y,z;
 		public void setxyz(double x1, double y1, double z1, double k) {
 			x=x1*k;y=y1*k;z=z1*k;
 		}
 		/**Calculates position of point on 3d hilbert curve*/
 		public void hilb3_point(double n, int lvl){
 			if (lvl==0){
 				x=0;y=n;z=0;
 				return;
 			}
 			if ( n > 0.5){
 			  hilb3_point(1 - n,lvl);
 			  y=1-y;
 			  return;
 			}
 
 			n=n*8;// %n <= 4 now
 		    hilb3_point(n-Math.floor(n),lvl-1);
 
 			if (n<=1){
 			  setxyz(y,z,x,0.5);
 			  return;
 			}
 
 			if (n<=2){
 			  setxyz(x+1, z, y, 0.5);
 			  return;
 			}
 
 			if (n<=3){
 			  setxyz(x+1, z, y+1, 0.5);
 			  return;
 			}
 
 			if (n<=4){
 			  setxyz(1 - x, y, 2- z, 0.5);
 			  return;
 			}
 
 		}
 
 		@Override
 		public float evaluate(double X, double Y) {
 			if (X<0 || X>1 ||Y<0 ||Y>1)
 				return -1;
 			
 			hilb3_point(hilb_index(X, Y, 30), 30);
 
 			double r2=Utils.sqr(x-x0)+Utils.sqr(y-y0)+Utils.sqr(z-z0);
 			
 			if (r2>r02) return -1;
 			return (float)Math.pow( (1.0/(1-r2/r02)), 0.333);
 			
 		}
 	}
 
 
 	private double hilb_index(double x, double y, int lvl){
 		if (lvl==0) return x;
 		if ( x<0.5 && y<0.5 )
 			return hilb_index(y*2, x*2, lvl-1)*0.25;
 
 		if ( x<0.5 && y>=0.5)
 			return 0.25 + hilb_index(x*2, y*2-1, lvl-1)*0.25;
 
 		if (x>=0.5 && y<0.5)
 			return  0.75 + hilb_index(1-y*2, 2-x*2, lvl-1)*0.25;
 
 		if (x>=0.5 && y>=0.5)
 			return 0.5 + hilb_index(x*2-1, y*2-1, lvl-1)*0.25;
 		
 		throw new RuntimeException("hilb-index: coordinate is invalid");
 	}
 
 
 	public Function get() {
		return new Func();
 	}
 	
 
 	
 	
 
 
 }
