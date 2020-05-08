 package org.utils.render;
 
 import java.util.ArrayList;
 
 public class RenderPolygons {
 
 	public ArrayList<Triangle> render = new ArrayList<Triangle>();
 	
 	public String toString(){
 		return render.toString();
 	}
 	
 	
 	public void renderToTrapezoidWithColor(int point[][], double zIndex[]) {
 		ArrayList<Triangle> triangles = new ArrayList<Triangle>();
 		Triangle temp;
 		if(point[0][0] == point[1][0] && point[0][0] == point[2][0])
 			return;
 		if(point[0][1] == point[1][1] && point[0][1] == point[2][1])
 			return;
 		if((point[0][1] - point[1][1])/((double)point[0][0] - point[1][0]) ==
 				(point[0][1] - point[2][1])/((double)point[0][0] - point[2][0])	)
 			return;
 		if (point.length == 3) {
 				temp = new Triangle(point[0], point[1], point[2], zIndex[0], zIndex[1], zIndex[2]); 
 				triangles.add(temp);
 		}
 		else{
 			temp = new Triangle(point[0], point[1], point[2], zIndex[0], zIndex[1], zIndex[2]); 
 			triangles.add(temp);
 			temp = new Triangle(point[0], point[2], point[3], zIndex[0], zIndex[2], zIndex[3]); 
 			triangles.add(temp);
 		}
 		
 		for(int i = 0 ; i< triangles.size() ;i++){
 			temp = triangles.get(i);
 			ArrayList<Triangle> x= Triangle.splitTriangle(temp);
 			render.addAll(x);
 		}
 	}
 		
 	public void colorPolygon(int pix[], int color, int W, double zBuffer[]){
 		for(int i = 0 ; i < this.render.size() ;i++){
			//if(render.get(i).area != 0){
 			render.get(i).renderTriangle(pix, color, W, zBuffer);
			//}
 		}
 	}
 }
