 package org.hw8;
 
 import java.util.ArrayList;
 
 import org.utils.MISApplet;
 import org.utils.material.Light;
 import org.utils.render.RenderPolygons;
 import org.utils.shape.Geometry;
 import org.utils.shape.ShapeCube;
 import org.utils.shape.ShapeCylinder;
 import org.utils.shape.ShapeNull;
 import org.utils.shape.ShapeSphere;
 import org.utils.transform.Projection;
 
 @SuppressWarnings("serial")
 public class RenderingScene extends MISApplet {
 	ArrayList<RenderPolygons> scene;
 	Projection proj = new Projection(W, H, 15.0);
 
 	Geometry joint[][] = new Geometry[4][3];
 	Geometry dactylus[][] = new Geometry[4][3];
 	Geometry root = new ShapeNull();
 	Geometry hand = new ShapeCube();
 
 	Geometry ball = new ShapeSphere();
 
 	double speedCirclePerSecond = 0.1;
 	int frameCount = 0;
 	double alpha;
 	
 	Light[] l = new Light[2];
 	double[] eye = new double[]{0, 0, 1.0};
 	
 	double[] AColor = {0.2, 0, 0};
 	double[] DColor = {0.8, 0, 0};
 	double[] SColor = {.50, .50, .50};
 	double power = 10.0;
 	double[] AColor1 = {0.1, 0.05, 0.05};
 	double[] DColor1 = {0.82, 0.5, 0.44};
 	double[] SColor1 = {0.1, 0.1, 0.1};
 	double power1 = 10.0;
 	
 	public void create() {
 		root = new ShapeNull();
 		hand = new ShapeCube();
 		root.getMatrix().identity();
 		hand.getMatrix().identity();
 		ball.getMatrix().identity();
 		
 		root.addChild(hand);
 		root.addChild(ball);
		hand.globe(30, 30);
		ball.globe(30, 30);
 
 		hand.getMatrix().scale(1, 1, 0.3);
 		ball.getMatrix().translate(-3, 0, 0);
 
 		for (int i = 0; i < 4; i++) {
 			for (int j = 0; j < 3; j++) {
 				joint[i][j] = new ShapeNull();
 				dactylus[i][j] = new ShapeCylinder();
 			}
 		}
 		for (int i = 0; i < 4; i++) {
 			root.addChild(joint[i][0]);
 			joint[i][0].addChild(joint[i][1]);
 			joint[i][0].addChild(dactylus[i][0]);
 			joint[i][1].addChild(joint[i][2]);
 			joint[i][1].addChild(dactylus[i][1]);
 			joint[i][2].addChild(dactylus[i][2]);
 		}
 		double translate[] = { -.8, -.3, .3, .8 };
 		double length[][] = { { .6, .5, .3 }, { .7, .5, .3 }, { .5, .4, .3 },
 				{ .3, .3, .2 } };
 		for (int i = 0; i < 4; i++) {
 			joint[i][0].getMatrix().identity();
 			joint[i][0].getMatrix().translate(translate[i], 1, 0);
 			joint[i][0].getMatrix().rotateX(-Math.PI / 2);
 			joint[i][0].getMatrix().scale(.15, .15, 1);
			dactylus[i][0].globe(30, 30);
 			dactylus[i][0].getMatrix().translate(0, 0, length[i][0]);
 			dactylus[i][0].getMatrix().scale(1, 1, length[i][0]);
 
 			joint[i][1].getMatrix().identity();
 			joint[i][1].getMatrix().translate(0, 0, 2 * length[i][0]);
			dactylus[i][1].globe(30, 30);
 			dactylus[i][1].getMatrix().translate(0, 0, length[i][1]);
 			dactylus[i][1].getMatrix().scale(1, 1, length[i][1]);
 
 			joint[i][2].getMatrix().identity();
 			joint[i][2].getMatrix().translate(0, 0, 2 * length[i][1]);
			dactylus[i][2].globe(30, 30);
 			dactylus[i][2].getMatrix().translate(0, 0, length[i][2]);
 			dactylus[i][2].getMatrix().scale(1, 1, length[i][2]);
 		}
 		
 		Geometry.setMaterialFromRoot(root, AColor1, DColor1, SColor1, power);
 		Geometry.setMaterialFromRoot(ball, AColor, DColor, SColor, power);
 	}
 
 	@Override
 	public void initialize() {
 		
 		proj = new Projection(W, H, 15.0);
 		alpha = 0;
 		
 		l[0] = new Light();
 		l[0].setLightDir(1.0, 1.0, .5);
 		l[0].setLightColor(1.0, 1.0, 1.0);
 		l[1] = new Light();
 		l[1].setLightDir(-1.0, -1.0, -.5);
 		l[1].setLightColor(1.0, 1.0, 1.0);
 	}
 
 	@Override
 	public void initFrame(double time) {
 		this.create();
 		scene = new ArrayList<RenderPolygons>();
 		alpha = speedCirclePerSecond * 2 * Math.PI
 				* ((frameCount) / (double) 20);
 		frameCount = (frameCount + 1 + 200) % 200;
 		
 		if (alpha < Math.PI) {
 			for (int i = 0; i < 4; i++) {
 				joint[i][0].getMatrix().rotateX(alpha / 2);
 				joint[i][1].getMatrix().rotateX(alpha / 2);
 				joint[i][2].getMatrix().rotateX(alpha / 2);
 			}
 		} else {
 			for (int i = 0; i < 4; i++) {
 				joint[i][0].getMatrix().rotateX((2 * Math.PI - alpha) / 2);
 				joint[i][1].getMatrix().rotateX((2 * Math.PI - alpha) / 2);
 				joint[i][2].getMatrix().rotateX((2 * Math.PI - alpha) / 2);
 			}
 		}
 		
 		for(int i = 0 ; i < W ; i++){
 			for(int j = 0 ; j < H ;j++){
 				zBuffer[j*W+i] = -15.0;
 			}
 		}
 		
 		root.getMatrix().translate(0, -2, 0);
 		root.getMatrix().scale(1.8, 1.8, 1.8);
 		root.getMatrix().rotateY(alpha);
 		Geometry.renderFromRoot(root, scene, proj, l, eye);
 	}
 
 	@Override
 	public void setPixel(int x, int y, int rgb[]) {
 		rgb[0] = 0x99;
 		rgb[1] = 0x26;
 		rgb[2] = 0x67;
 	}
 
 	@Override
 	public void computeImage(double time) {
 		initFrame(time); // INITIALIZE COMPUTATION FOR FRAME
 		int rgb[] = new int[3];
 		int color = 0;
 		int i = 0;
 		for (int y = 0; y < H; y++){
 			for (int x = 0; x < W; x++) { // COMPUTE COLOR FOR EACH PIXEL
 				setPixel(x, y, rgb);
 				pix[i++] = pack(rgb[0], rgb[1], rgb[2]);
 			}
 		}
 		color = pack(0xd2, 0xb1, 0x6f);
 		for(int j = 0 ; j < scene.size(); j++){
 			scene.get(j).colorPolygon(pix, color, W, zBuffer);
 		}
 	}
 }
