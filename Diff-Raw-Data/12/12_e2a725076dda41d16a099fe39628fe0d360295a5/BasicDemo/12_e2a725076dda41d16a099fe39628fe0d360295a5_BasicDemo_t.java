 package deform.tests;
 
 import static deform.Combinators.*;
 import static deform.Library.*;
 
 import deform.Color;
 import deform.Library.ColorAndFraction;
 import deform.Texture;
 import deform.Vec;
 import deform.shapes.Shape;
 import deform.texturedshape.TexturedShape;
 
 public class BasicDemo extends DemoBase{
 
 	public static boolean awt = true;
 
 	public static void main(String[] argv){
 		new BasicDemo();
 	}
 	
 	@Override
 	public void handleKeyStroke(char key){
 		if(key == 'e'){
 			awt = !awt;
 		}
 	}
 	
 	@Override
 	void draw() {
 		final Vec lu = new Vec(0,0);
 		final double w = 100;
 		final double h = 100;
 		Vec ru = new Vec(lu.x + w, lu.y);
 		Vec ld = new Vec(lu.x , lu.y + h);
 		Vec rd = new Vec(lu.x + w , lu.y + h);
 		Shape rect = close(
 				path(lu, lineTo(ru), lineTo(rd), lineTo(ld),lineTo(lu)));
 //		rect = stroke(rect,30);
 		Texture multip = transform(scale(250).compose(translate(1,1)),radialGradient(new ColorAndFraction(0,new Color(255,0,0)),
 								new ColorAndFraction(0.7,new Color(0,255,0)),
 								new ColorAndFraction(1.0,new Color(0,0,255))));
 		rect = transform(translate(100,600).compose(scale(50)),text("Atze"));
 		TexturedShape s =fill(rect //, transform(scale(100),horGradient(new Color(255,0,0,255),new Color(0,255,0,255))));
 				,multip);
		s = over(fill(stroke(rect,15),fillColor(new Color(0,140,0))),s);
 //				,new Texture() {
 //			
 //			@Override
 //			public Color sample(Vec point) {
 ////				return new Color(255,0,0,255);
 //				
 //				if((int)(point.x /10) % 2 == 0 ^  (int)(point.y /10) % 2 ==0){
 //					double frac = (point.x - lu.x) /w;
 //					return new Color(255,0,0,255).lerp(frac, new Color(0,255,0,255));
 //				} else {
 //					double frac = (point.y - lu.y) /h;
 //					return new Color(0,0,255,255).lerp(1.0 - frac, new Color(255,255,0,255));
 //				}
 //				
 //			}
 //		});
 //		double d = Math.abs(wheel)/100  == 0 ? 1 : Math.abs(wheel)/100 ;
 //		s = transform(fisheye(mouse,Math.abs(wheel)/100 , 200), s);
		s = transform(coneLens(mouse, 100 + wheel),s);
 //		s = transform(rotate(wheel/100*Math.PI),s);
 //		System.out.println(translate(new Vec(w/2,h/2)).compose(rotate(wheel/100*Math.PI).compose(translate(new Vec(-w/2,-h/2)))));
 //		s = transform(translate(mouse).compose(rotate(wheel/100*Math.PI).compose(translate(new Vec(-w/2,-h/2)))),s);
 		draw(s);
 		
 	}
 
 
 }
