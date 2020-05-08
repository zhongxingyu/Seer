 package org.utils.shape;
 
 import java.awt.Graphics;
 
 import org.utils.transform.Projection;
 
 /**
  * Geometry for cube.
  * @author kerry
  *
  */
 public class ShapeCube extends Geometry {
 
 	@Override
 	public void globe(int M, int N) {
 		// vertices should contain the normal for each vertice.
		vertices = new double[][] { { 1, 1, 1, 1, 1, 1 },
				{ 1, -1, 1, 1, -1, 1 }, { -1, -1, 1, -1, -1, 1 },
				{ -1, 1, 1, -1, 1, 1 }, { 1, 1, -1, 1, 1, -1 },
				{ 1, -1, -1, 1, -1, -1 }, { -1, -1, -1, -1, -1, -1 },
				{ -1, 1, -1, -1, 1, -1 }, { 1, 1, 1, 1, 1, 1 },
				{ 1, -1, 1, 1, -1, 1 }, { 1, -1, -1, 1, -1, -1 },
				{ 1, 1, -1, 1, 1, -1 }, { -1, 1, 1, -1, 1, 1 },
				{ -1, -1, 1, -1, -1, 1 }, { -1, -1, -1, -1, -1, -1 },
				{ -1, 1, -1, -1, 1, -1 }, { 1, 1, 1, 1, 1, 1 },
				{ -1, 1, 1, -1, 1, 1 }, { -1, 1, -1, -1, 1, -1 },
				{ 1, 1, -1, 1, 1, -1 }, { 1, -1, 1, 1, -1, 1 },
				{ -1, -1, 1, -1, -1, 1 }, { -1, -1, -1, -1, -1, -1 },
				{ 1, -1, -1, 1, -1, -1 }, };
 		faces = new int[6][4];
 		for (int i = 0; i < faces.length; i++) {
 			faces[i] = new int[] { i * 4, i * 4 + 1, i * 4 + 2, i * 4 + 3 };
 		}
 
 		this.m.identity();
 	}
 
 	@Override
 	double plotX(double u, double v) {
 		return 0;
 	}
 
 	@Override
 	double plotY(double u, double v) {
 		return 0;
 	}
 
 	@Override
 	double plotZ(double u, double v) {
 		return 0;
 	}
 
 	@Override
 	public void drawShape(Graphics g, Projection p) {
 		for (int i = 0; i < faces.length; i++) {
 			drawEdge(vertices[faces[i][0]], vertices[faces[i][1]], g, p);
 			drawEdge(vertices[faces[i][1]], vertices[faces[i][2]], g, p);
 			drawEdge(vertices[faces[i][2]], vertices[faces[i][3]], g, p);
 			drawEdge(vertices[faces[i][3]], vertices[faces[i][0]], g, p);
 		}
 	}
 
 }
