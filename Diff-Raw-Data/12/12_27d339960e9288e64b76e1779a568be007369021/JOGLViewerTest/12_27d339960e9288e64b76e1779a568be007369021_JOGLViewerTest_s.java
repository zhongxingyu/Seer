 package de.tum.in.reitinge.test;
 
 import java.awt.Color;
 import java.util.Hashtable;
 
 import de.tum.in.cindy3dplugin.AppearanceState;
 import de.tum.in.cindy3dplugin.Cindy3DViewer;
 import de.tum.in.cindy3dplugin.LightModificationInfo;
 import de.tum.in.cindy3dplugin.Cindy3DViewer.MeshTopology;
 import de.tum.in.cindy3dplugin.Cindy3DViewer.NormalType;
 import de.tum.in.cindy3dplugin.LightModificationInfo.LightFrame;
 import de.tum.in.cindy3dplugin.LightModificationInfo.LightType;
 import de.tum.in.cindy3dplugin.jogl.JOGLViewer;
 
 public class JOGLViewerTest {
 	public static void main(String[] args) {
 		JOGLViewer viewer = new JOGLViewer();
 		
 		Hashtable<String, Object> hintsMap = new Hashtable<String,Object>();
 		
 		hintsMap.put("quality", 4.0);
 
 		viewer.setRenderHints(hintsMap);
 		
 		//viewer.setBackgroundColor(Color.white);
 		//viewer.setBackgroundColor(Color.black);
 		
 		viewer.begin();
 //		materials(viewer);
 //		colorSpiral(viewer);
 //		circles(viewer);
 //		lines(viewer);
 //		spheres(viewer);
 //		icosahedron(viewer);
 //		enneper(viewer, true);
 		lights(viewer);
 		
 		viewer.end();
 	}
 	
 	public static void materials(Cindy3DViewer viewer) {
 		AppearanceState app = new AppearanceState(Color.red, 0, 1, 1);
 		
 		app.setAlpha(1);
 		
 		for (int i = 1; i <= 12; ++i) {
 			for (int j = 1; j <= 12; ++j) {
 				app.setShininess(i*10);
 				app.setColor(new Color(Color.HSBtoRGB(j/12.0f, 1, 1)));
 				viewer.addSphere(i-6, j-6, 0, 0.45, app);
 			}
 		}
 	}
 	
 	public static void lights(Cindy3DViewer viewer) {
 		AppearanceState appearance = new AppearanceState(Color.white, 60, 1.0, 1);
 		viewer.addSphere(0, 0, 0, 3, appearance);
 		
 //		LightModificationInfo info = new LightModificationInfo(
 //				LightType.DIRECTIONAL_LIGHT);
 //
 //		info.setDirection(new double[] { 0, 1, 0 });
 //		info.setDiffuse(new Color(1.0f, 1.0f, 1.0f));
 //		info.setFrame(LightFrame.WORLD);
 //		viewer.setLight(0, info);
 		
 		LightModificationInfo info = new LightModificationInfo(
 		LightType.SPOT_LIGHT);
 
		info.setPosition(new double[] {15,15,0});
		info.setDirection(new double[] { -1, -1, 0 });
		info.setDiffuse(new Color(1.0f, 1.0f, 1.0f));
		info.setCutoffAngle(5.0);
		info.setSpotExponent(0.0);
		info.setFrame(LightFrame.WORLD);
 		viewer.setLight(0, info);
 
 		appearance.setColor(Color.green);
 		appearance.setAlpha(1.0);
 		viewer.addSphere(-3, 0, 0, 1.5, appearance);
 	}
 	
 	public static void enneper(Cindy3DViewer viewer, boolean dots) {
 		AppearanceState app = new AppearanceState(Color.white, 60, 1, 1);
 		
 		viewer.setBackgroundColor(Color.black);
 		int rr = 30;
 		int ss = 30;
 		
 		double[][] vertices = new double[(2*rr+1)*(2*ss+1)][3];
 		
 		int i = 0;
 		for (int ii = -rr; ii <= rr; ++ii) {
 			double u = ii/20.0;
 			for (int jj = -ss; jj <= ss; ++jj, ++i) {
 				double v = jj/20.0;
 				double x = u - u*u*u/3.0 + u*v*v;
 				double y = v - v*v*v/3.0 + v*u*u;
 				double z = u*u - v*v;
 				vertices[i][0] = x;
 				vertices[i][1] = y;
 				vertices[i][2] = z;
 			}
 		}
 		
 		if (dots) {
 			app.setSize(0.6);
 			app.setColor(new Color(0.9f, 0.1f, 0.1f));
 			for (double[] vertex : vertices) {
 				viewer.addPoint(vertex[0], vertex[1], vertex[2], app);
 			}
 		}
 		
 		app.setAlpha(0.8);
 		app.setColor(new Color(0.5f, 0.5f, 1.0f));
 		viewer.addMesh(2*rr+1, 2*ss+1, vertices, NormalType.PER_VERTEX, MeshTopology.OPEN, app);
 	}
 	
 	public static void colorSpiral(Cindy3DViewer viewer) {
 		AppearanceState appearance = new AppearanceState(Color.red, 60, 1.0, 1.0);
 		
 		double height = 2;
 		double radius = 1;
 		int segments = 100;
 		int rings = 20;
 
 		for (int segment = 1; segment <= segments; ++segment) {
 			double frac = (double) segment / segments;
 			appearance.setColor(new Color(Color.HSBtoRGB((float) frac, 1, 1)));
 			for (int ring = 1; ring <= rings; ++ring) {
 				double rad = (double) ring / rings * radius;
 				viewer.addPoint(Math.cos(frac * 2 * Math.PI) * rad,
 						Math.sin(frac * 2 * Math.PI) * rad, (frac - 0.5)
 								* height, appearance);
 			}
 		}
 	}
 	
 	public static void circles(Cindy3DViewer viewer) {
 		AppearanceState appearance = new AppearanceState(Color.red, 50, 1.0, 1.0);
 		
 		LightModificationInfo info = new LightModificationInfo(LightType.DIRECTIONAL_LIGHT);
 		//LightModificationInfo info = new LightModificationInfo(LightType.POINT_LIGHT);
 
 		//info.setSpecular(Color.black);
 		
 		info.setPosition(new double[]{10,0,0});		
 		info.setDirection(new double[]{1,0,0});
 		viewer.setLight(0, info);
 		
 		info.setPosition(new double[]{0,10,0});
 		info.setDirection(new double[]{0,1,0});
 		viewer.setLight(1, info);
 		
 		info.setPosition(new double[]{0,0,10});
 		info.setDirection(new double[]{0,0,1});
 		viewer.setLight(2, info);
 		
 		//appearance.setAlpha(0.5);
 		
 		for (int i = -5; i <= 5; ++i) {
 			for (int j = -5; j <= 5; ++j) {
 				appearance.setColor(Color.red);
 				viewer.addCircle(i * 2, j * 2, 0, 1, 0, 0, 1, appearance);
 				appearance.setColor(Color.green);
 				viewer.addCircle(i * 2, j * 2, 0, 0, 1, 0, 1, appearance);
 				appearance.setColor(Color.blue);
 				viewer.addCircle(i * 2, j * 2, 0, 0, 0, 1, 1, appearance);
 			}
 		}
 	}
 	
 	public static void spheres(Cindy3DViewer viewer) {
 		AppearanceState appearance = new AppearanceState(Color.red, 60, 1.0, 1);
 		
 //		viewer.addPoint(0, 0, 0, appearance);
 		
 //		appearance.setColor(Color.green);
 //		appearance.setSize(50);
 //		appearance.setAlpha(0.1);
 		viewer.addPoint(0, 0, 0, appearance);
 		
 		appearance.setColor(Color.blue);
 		viewer.addPoint(2, 0, 0, appearance);
 		
 //		double size = 10;
 		int rings = 10;
 		int segments = 20;
 		
 		double[][] vert = new double[rings*segments][3];
 		
 		for (int i=0; i<rings; ++i) {
 			double u = i * 2 * Math.PI / rings;
 			for (int j=0; j<segments; ++j) {
 				double v = j * 2 * Math.PI / segments;
 				double z = Math.cos(v) * 0.25;
 				double x = Math.sin(v) * 0.25 + 1;
 				double y = Math.sin(u)*x;
 				
 				x *= Math.cos(u);
 				
 				vert[i*segments+j][0] = x;
 				vert[i*segments+j][1] = y;
 				vert[i*segments+j][2] = z;
 				
 //				double x = -size/2.0 + i*size/sizeCount;
 //				double z = -size/2.0 + j*size/sizeCount;
 //				
 //				vert[i*sizeCount+j][0] = x;
 //				vert[i*sizeCount+j][1] = -0.1*(x*x+z*z)+2;
 //				vert[i*sizeCount+j][2] = z;
 			}
 		}
 		appearance.setColor(Color.RED);
 		//appearance.setAlpha(0.7);
 		viewer.addMesh(rings, segments, vert, NormalType.PER_VERTEX, MeshTopology.CLOSE_BOTH, appearance);
 		
 		//viewer.addPoint(0, 0, 0, appearance);
 	}
 	
 	public static void lines(Cindy3DViewer viewer) {
 		AppearanceState appearance = new AppearanceState(Color.red, 60, 1.0, 1.0);
 		
 		//viewer.addSphere(0, 0, 0, 2, appearance);
 		viewer.addSegment(-2, 0, 0, 2, 0, 0, appearance);
 //		viewer.addLine(0, 2, 0, 5, 4, 0, appearance);
 //
 //		viewer.addRay(0, 4, 0, 5, -2, 2, appearance);
 
 //		double r = 1;
 //		int n = 750;
 //		double[][] vertices = new double[n][3];
 //		for (int i=0; i<n; ++i) {
 //			vertices[i][2] = 0;
 //			vertices[i][0] = r*Math.sin(i*2*Math.PI/n);
 //			vertices[i][1] = r*Math.cos(i*2*Math.PI/n);
 //		}
 //		
 ////		double[][] vertices2 = new double[][] {{0,0,0},{1,0,0},{1,0,1},{0,0,1}};
 ////		
 ////		
 ////		
 ////		viewer.addPolygon(vertices2, null, appearance);
 ////		
 ////		appearance.setAlpha(0.25);
 ////		viewer.addMesh(2, 3, new double[][] { { 0, 0, 0 }, { 1, 1, 0 },
 ////				{ 2, 0, 0 }, { 0, 0, 1 }, { 1, 1, 1 }, { 2, 0, 1 } }, true, appearance);
 //		
 //		double size = 10;
 //		int sizeCount = 50;
 //		
 //		
 //		double[][] vert = new double[sizeCount*sizeCount][3];
 //		
 //		for (int i=0; i<sizeCount; ++i) {
 //			for (int j=0; j<sizeCount; ++j) {
 //				double x = -size/2.0 + i*size/sizeCount;
 //				double z = -size/2.0 + j*size/sizeCount;
 //				
 //				vert[i*sizeCount+j][0] = x;
 //				vert[i*sizeCount+j][1] = 0.1*(x*x+z*z)-1;
 //				vert[i*sizeCount+j][2] = z;
 //			}
 //		}
 //		appearance.setColor(Color.BLUE);
 //		appearance.setAlpha(0.5);
 //		viewer.addMesh(sizeCount, sizeCount, vert, true, MeshTopology.OPEN, appearance);
 //		
 //		vert = new double[sizeCount*sizeCount][3];
 //		
 //		for (int i=0; i<sizeCount; ++i) {
 //			for (int j=0; j<sizeCount; ++j) {
 //				double x = -size/2.0 + i*size/sizeCount;
 //				double z = -size/2.0 + j*size/sizeCount;
 //				
 //				vert[i*sizeCount+j][0] = x;
 //				vert[i*sizeCount+j][1] = -0.1*(x*x+z*z)+2;
 //				vert[i*sizeCount+j][2] = z;
 //			}
 //		}
 //		appearance.setColor(Color.RED);
 //		appearance.setAlpha(0.5);
 //		viewer.addMesh(sizeCount, sizeCount, vert, true, MeshTopology.OPEN, appearance);
 //		
 //		vert = new double[sizeCount*sizeCount][3];
 //		
 //		for (int i=0; i<sizeCount; ++i) {
 //			for (int j=0; j<sizeCount; ++j) {
 //				double x = -size/2.0 + i*size/sizeCount;
 //				double z = -size/2.0 + j*size/sizeCount;
 //				
 //				vert[i*sizeCount+j][0] = x;
 //				vert[i*sizeCount+j][1] = z;
 //				vert[i*sizeCount+j][2] = -0.1*(x*x+z*z)+2;
 //			}
 //		}
 //		appearance.setColor(Color.YELLOW);
 //		appearance.setAlpha(0.5);
 //		viewer.addMesh(sizeCount, sizeCount, vert, true, MeshTopology.OPEN, appearance);
 //		
 //		vert = new double[sizeCount*sizeCount][3];
 //		
 //		for (int i=0; i<sizeCount; ++i) {
 //			for (int j=0; j<sizeCount; ++j) {
 //				double x = -size/2.0 + i*size/sizeCount;
 //				double z = -size/2.0 + j*size/sizeCount;
 //				
 //				vert[i*sizeCount+j][0] = x;
 //				vert[i*sizeCount+j][1] = z;
 //				vert[i*sizeCount+j][2] = 0.1*(x*x+z*z)-2;
 //			}
 //		}
 //		appearance.setColor(Color.GREEN);
 //		appearance.setAlpha(0.5);
 //		viewer.addMesh(sizeCount, sizeCount, vert, true, MeshTopology.OPEN, appearance);
 		
 		// viewer.addLineStrip(vertices, appearance, true);
 	}
 
 	public static void icosahedron(JOGLViewer viewer) {
 		AppearanceState appearance = new AppearanceState(Color.yellow, 60, 1.0, 1.0);
 		double golden = (1.0 + Math.sqrt(5)) / 2.0;
 
 		double verts[][] = { { 0, 1, golden }, { 0, -1, golden },
 				{       0,       1, -golden },
 				{       0,      -1, -golden },
 				{  golden,       0,       1 },
 				{  golden,       0,      -1 },
 				{ -golden,       0,       1 },
 				{ -golden,       0,      -1 },
 				{       1,  golden,       0 },
 				{      -1,  golden,       0 },
 				{       1, -golden,       0 },
 				{      -1, -golden,       0 },
 		};
 		
 		int[][] edges = {
 				{ 0, 1 },
 				{ 0, 4 },
 				{ 0, 6 },
 				{ 0, 8 },
 				{ 0, 9 },
 				{ 1, 4 },
 				{ 1, 6 },
 				{ 1, 10 },
 				{ 1, 11 },
 				{ 2, 3 }, 
 				{ 2, 5 },
 				{ 2, 7 },
 				{ 2, 8 },
 				{ 2, 9 },
 				{ 3, 5 },
 				{ 3, 7 },
 				{ 3, 10 },
 				{ 3, 11 },
 				{ 4, 5 },
 				{ 4, 8 },
 				{ 4, 10 },
 				{ 5, 8 },
 				{ 5, 10 },
 				{ 6, 7 },
 				{ 6, 9 },
 				{ 6, 11 },
 				{ 7, 9 },
 				{ 7, 11 },
 				{ 8, 9 },
 				{ 10, 11 },
 		};
 		
 		for (int[] edge : edges) {
 			double vert1[] = verts[edge[0]];
 			double vert2[] = verts[edge[1]];
 			viewer.addSegment(vert1[0], vert1[1], vert1[2], vert2[0], vert2[1],
 					vert2[2], appearance);
 		}
 		
 		for (int[] edge : edges) {
 			double vert1[] = verts[edge[0]];
 			double vert2[] = verts[edge[1]];
 			viewer.addSegment(2*vert1[0], 2*vert1[1], 2*vert1[2], 2*vert2[0], 2*vert2[1],
 					2*vert2[2], appearance);
 		}
 		
 		int[][] triangles = {
 				{0,1,4},{0,1,6},{0,4,8},{0,6,9},{0,8,9},
 				{1,4,10},{1,6,11},{1,10,11},
 				{2,3,5},{2,3,7},{2,5,8},{2,7,9},{2,8,9},
 				{3,5,10},{3,7,11},{3,10,11},
 				{4,5,8},{4,5,10},
 				{6,7,9},{6,7,11}
 		};
 		
 		Color[] col = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.WHITE, Color.WHITE};
 		
 		appearance.setColor(Color.RED);
 		//appearance.setAlpha(0.3);
 		
 		int h=0;
 		for (int[] triangle : triangles) {
 			double[][] v = new double[3][3];
 			for (int i=0; i<3; ++i) {
 				v[i] = verts[triangle[i]];
 			}
 			appearance.setColor(col[(h++)%col.length]);
 			viewer.addPolygon(v, null, appearance);
 		}
 		
 		appearance.setColor(Color.BLUE);
 		//appearance.setAlpha(0.3);
 		
 		h=3;
 		for (int[] triangle : triangles) {
 			double[][] v = new double[3][3];
 			for (int i=0; i<3; ++i) {
 				v[i] = verts[triangle[i]].clone();
 				for (int j=0; j<3; ++j)
 					v[i][j] *= 2;
 			}
 			appearance.setColor(col[(h++)%col.length]);
 			viewer.addPolygon(v, null, appearance);
 		}
 	}
 }
