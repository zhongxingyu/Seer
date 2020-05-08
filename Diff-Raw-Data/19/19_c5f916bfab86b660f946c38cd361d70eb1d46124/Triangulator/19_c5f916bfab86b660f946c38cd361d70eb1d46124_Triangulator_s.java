 package com.secondhand.math;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.badlogic.gdx.math.Vector2;
 
 /**
  * The triangulation and area compution logic of this class was not written by me, it comes from
  * John W. Ratcliff's excellent polygon triangulator:
  * http://www.flipcode.com/archives/Efficient_Polygon_Triangulation.shtml
  * 
  * 
  */
 public final class Triangulator {
 
 	private static final float EPSILON=0.0000000001f;
 
 	private Triangulator() { }
 
 	public static List<Vector2> triangulate(List<Vector2> polygon) {
 
 		/* allocate and initialize list of Vertices in polygon */
 
 		int n = polygon.size();
 		if ( n < 3 ) 
 			throw new IllegalArgumentException("A polygon with less than 3 points is not a polygon!");
 
 		int V[] = new int[n];
 		// int *V = new int[n];
 
 		/* we want a counter-clockwise polygon in V */
 
 		if ( 0.0f < area(polygon) )
 			for (int v=0; v<n; v++) V[v] = v;
 		else
 			for(int v=0; v<n; v++) V[v] = (n-1)-v;
 
 		int nv = n;
 
 		/*  remove nv-2 Vertices, creating 1 triangle every time */
 		int count = 2*nv;   /* error detection */
 
 		/* the resulting triangulated polygon */
 		List<Vector2> result = new ArrayList<Vector2>();
 
 		for(int v=nv-1; nv>2; )
 		{
 			/* if we loop, it is probably a non-simple polygon */
 			if (0 >= (count--))
 			{
 				throw new IllegalArgumentException("Bad polygon!");
 			}
 
 			/* three consecutive vertices in current polygon, <u,v,w> */
 			int u = v  ; if (nv <= u) u = 0;     /* previous */
 			v = u+1; if (nv <= v) v = 0;     /* new v    */
 			int w = v+1; if (nv <= w) w = 0;     /* next     */
 
 			if ( snip(polygon,u,v,w,nv,V) )
 			{
 				int a,b,c,s,t;
 
 				/* true names of the vertices */
 				a = V[u]; b = V[v]; c = V[w];
 
 				/* output Triangle */
 				/* we do it in this order to get a clockwise circle.*/
 				/* andengine won't draw it unless it is clockwise. */
 				result.add( polygon.get(c) );
 				result.add( polygon.get(b) );
 				result.add( polygon.get(a) );
 
 
 				/* remove v from remaining polygon */
 				for(s=v,t=v+1;t<nv;s++,t++) V[s] = V[t]; nv--;
 
 				/* resest error detection counter */
 				count = 2*nv;
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * compute area of apolygon. 
 	 */
 	private static float area(List<Vector2> polygon) {
 		int n = polygon.size();
 
 		float A=0.0f;
 
 		for(int p=n-1,q=0; q<n; p=q++)
 		{
 			A+= polygon.get(p).x*polygon.get(q).y - polygon.get(q).x*polygon.get(p).y;
 		}
 		return A*0.5f;
 	}
 
 	private static boolean snip(List<Vector2> polygon,int u,int v,int w,int n, int V[])
 	{
 		int p;
 		float Ax, Ay, Bx, By, Cx, Cy, Px, Py;
 
 		Ax = polygon.get(V[u]).x;
 		Ay = polygon.get(V[u]).y;
 
 		Bx = polygon.get(V[v]).x;
 		By = polygon.get(V[v]).y;
 
 		Cx = polygon.get(V[w]).x;
 		Cy = polygon.get(V[w]).y;
 
 		if ( EPSILON > (((Bx-Ax)*(Cy-Ay)) - ((By-Ay)*(Cx-Ax))) ) return false;
 
 		for (p=0;p<n;p++)
 		{
 			if( (p == u) || (p == v) || (p == w) ) continue;
 			Px = polygon.get(V[p]).x;
 			Py = polygon.get(V[p]).y;
 			if (insideTriangle(Ax,Ay,Bx,By,Cx,Cy,Px,Py)) return false;
 		}
 
 		return true;
 	}
 
 
 	private static boolean insideTriangle(float Ax, float Ay,
 			float Bx, float By,
 			float Cx, float Cy,
 			float Px, float Py)
 
 	{
 		float ax, ay, bx, by, cx, cy, apx, apy, bpx, bpy, cpx, cpy;
 		float cCROSSap, bCROSScp, aCROSSbp;
 
 		ax = Cx - Bx;  ay = Cy - By;
 		bx = Ax - Cx;  by = Ay - Cy;
 		cx = Bx - Ax;  cy = By - Ay;
 		apx= Px - Ax;  apy= Py - Ay;
 		bpx= Px - Bx;  bpy= Py - By;
 		cpx= Px - Cx;  cpy= Py - Cy;
 
 		aCROSSbp = ax*bpy - ay*bpx;
 		cCROSSap = cx*apy - cy*apx;
 		bCROSScp = bx*cpy - by*cpx;
 
 		return ((aCROSSbp >= 0.0f) && (bCROSScp >= 0.0f) && (cCROSSap >= 0.0f));
 	};
 
 }
