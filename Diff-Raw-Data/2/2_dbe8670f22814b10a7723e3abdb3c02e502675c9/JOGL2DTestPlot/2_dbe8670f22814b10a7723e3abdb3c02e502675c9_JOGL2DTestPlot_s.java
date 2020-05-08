 package JOGL2DLinePlotSample;
 
 import JOGL2DLinePlotSample.JOGL2DLinePlot;
 import JOGL2DLinePlotSample.JOGL2DLinePlot.Points;
 
 public class JOGL2DTestPlot{
 	private static JOGL2DLinePlot lineplot;
 	
 	public static void main(String[] args){
 		lineplot = new JOGL2DLinePlot("Fourier", -2, 2, 0.f, 0.4f, "t", "u(t)", "#.###");
 		
 		
 		Points pts1= lineplot.new Points(1000, new float[]{1f,0f,0f,0.5f}){
 
 			@Override
 			protected float func(float x) {
 				// TODO Auto-generated method stub
 				if(0<=x&&x<=1){
 					return x*(1-x*x);
 				}
 				else if(-2<=x&&x<=-1){
 					x+=2;
 					return x*(1-x*x);
 				}
 				else{
 					return 0;
 				}
 			}
 		};
 		
 		
 		lineplot.addPoints(pts1);
 		
 		Points pts2= lineplot.new Points(1000, new float[]{0f,1f,0f,0.7f}){
 
 			private float b(int n){
 				int sgn;
 				if (n%2==0){
 					sgn=1;
 				}
 				else{
 					sgn=-1;
 				}
 				return -(float)(6*sgn)/(float)Math.pow(Math.PI*n,3);
 			}
 			
 			private float a(int n){
 				int sgn;
 				if (n%2==0){
 					sgn=1;
 				}
 				else{
 					sgn=-1;
 				}
 				return ((float)6*(-1+sgn)-(float)(1+2*sgn)*(float)Math.pow(Math.PI*n,2))/(float)Math.pow(Math.PI*n,4);
 			}
 			
 			private float f(float x,int n){
 				float y=1f/8f;
 				for(int i = 1; i<=n; ++i){
 					y+=b(i)*Math.sin(Math.PI*i*x);
 					y+=a(i)*Math.cos(Math.PI*i*x);
 				}
 				return y;
 			}
 			
 			@Override
 			protected float func(float x) {
 				// TODO Auto-generated method stub
				return f(x,7);
 			}
 		};
 		lineplot.addPoints(pts2);
 		
 	}
 }
