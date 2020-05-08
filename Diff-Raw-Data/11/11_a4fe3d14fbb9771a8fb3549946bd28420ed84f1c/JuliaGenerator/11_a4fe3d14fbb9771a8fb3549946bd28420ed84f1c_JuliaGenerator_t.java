 package ratson.genimageexplorer.generators;
 
 import ratson.genimageexplorer.ObservationArea;
 import ratson.utils.FloatMatrix;
 
 public class JuliaGenerator extends MandelbrotLike {
 	public JuliaGenerator(){
 	}
 	
 	private double cx=-0.6288,cy=0.42515;
 	
 	public void setCx(double cx) {
 		this.cx = cx;
 	}
 	
 	
 	public double getCx() {
 		return cx;
 	}
 	public void setCy(double cy) {
 		this.cy = cy;
 	}
 	public double getCy() {
 		return cy;
 	}
 	
 	class Func extends Function{
 
 		@Override
 		public
 		float evaluate(double x, double y) {
 			double x2=0,y2,xx;
 			int iters=0;
 			double r2=0;
 			while (iters<maxIters && r2<r2Max){
 				x2=x*x;
 				y2=y*y;
 				r2=x2+y2;
 				
 				xx=x2-y2+cx;
 				y=2*x*y+cy;
 				
 				x=xx;
 				
 				iters++;
 			}
 			if (iters>=maxIters)
 				//return (float)x2;
 				return -1.0f;
 			
 			//smooth extrapolation
 			if (isSmooth){
 				double dx=(ln_ln_r2max-Math.log(Math.log(r2)))/ln2;
 				return (float)dx+(float)iters;
 			}else{
 				return iters;
 			}		
 		}
 		
 	}
 	public Function get() {
		return new Func();
 	}
 }
