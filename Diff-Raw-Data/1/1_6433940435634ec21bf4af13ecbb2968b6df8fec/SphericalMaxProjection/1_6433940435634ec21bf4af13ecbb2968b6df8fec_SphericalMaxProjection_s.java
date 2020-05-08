 package huisken.projection;
 
 import ij.ImagePlus;
 import ij.ImageStack;
 
 import ij.process.ImageProcessor;
 
 import java.io.DataOutputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 
 import javax.vecmath.Point3i;
 import javax.vecmath.Point3f;
 import javax.vecmath.Vector3f;
 import javax.vecmath.Matrix4f;
 
 import meshtools.IndexedTriangleMesh;
 
 import fiji.util.node.Leaf;
 import fiji.util.KDTree;
 import fiji.util.NNearestNeighborSearch;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.EOFException;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 
 import vib.FastMatrix;
 
 
 public class SphericalMaxProjection {
 
 	// These fields are set in prepareForProjection();
 	private Point4[] lut;
 	private float[] maxima;
 	private float[] weights;
 
 	// These fields must be set in the constructor and
 	// contain info about the sphere geometry
 	final Point3f center;
 	final float radius;
 	private final IndexedTriangleMesh sphere;
 	private final HashMap<Point3f, Integer> vertexToIndex;
 	private final NNearestNeighborSearch<Node3D> nnSearch;
 
 	public SphericalMaxProjection(Point3f center, float radius, int subd) {
 		this(createSphere(center, radius, subd), center, radius);
 	}
 
 	public SphericalMaxProjection(IndexedTriangleMesh sphere, Point3f center, float radius) {
 		this(sphere, center, radius, null);
 	}
 
 	public SphericalMaxProjection(IndexedTriangleMesh sph, Point3f c, float radius, FastMatrix transform) {
 		this.center = new Point3f(c);
 		this.radius = radius;
 
 		this.sphere = (IndexedTriangleMesh)sph.clone();
 
 		if(transform != null && !transform.isIdentity()) {
 			for(Point3f v : sphere.getVertices()) {
 				transform.apply(v.x, v.y, v.z);
 				v.set((float)transform.x, (float)transform.y, (float)transform.z);
 			}
 			transform.apply(center.x, center.y, center.z);
 			center.set((float)transform.x, (float)transform.y, (float)transform.z);
 		}
 
 		ArrayList<Node3D> nodes = new ArrayList<Node3D>(sphere.nVertices);
 		for(Point3f p : sphere.getVertices())
 			nodes.add(new Node3D(p));
 		KDTree<Node3D> tree = new KDTree<Node3D>(nodes);
 		nnSearch = new NNearestNeighborSearch<Node3D>(tree);
 
 		vertexToIndex = new HashMap<Point3f, Integer>();
 		for(int i = 0; i < sphere.nVertices; i++)
 			vertexToIndex.put(sphere.getVertices()[i], i);
 
 	}
 
 	public SphericalMaxProjection(String objfile) throws IOException {
 		this.sphere = loadSphere(objfile);
 
 		ArrayList<Node3D> nodes = new ArrayList<Node3D>(sphere.nVertices);
 		double mx = 0, my = 0, mz = 0;
 		for(Point3f p : sphere.getVertices()) {
 			nodes.add(new Node3D(p));
 			mx += p.x;
 			my += p.y;
 			mz += p.z;
 		}
 		this.center = new Point3f(
 			(float)(mx / sphere.nVertices),
 			(float)(my / sphere.nVertices),
 			(float)(mz / sphere.nVertices));
 		this.radius = sphere.getVertices()[0].distance(center);
 
 
 		KDTree<Node3D> tree = new KDTree<Node3D>(nodes);
 		nnSearch = new NNearestNeighborSearch<Node3D>(tree);
 
 		vertexToIndex = new HashMap<Point3f, Integer>();
 		for(int i = 0; i < sphere.nVertices; i++)
 			vertexToIndex.put(sphere.getVertices()[i], i);
 	}
 
 	private static IndexedTriangleMesh createSphere(Point3f center, float radius, int subd) {
 		// calculate the sphere coordinates
 		float tao = 1.61803399f;
 		Icosahedron icosa = new Icosahedron(tao, radius);
 
 		IndexedTriangleMesh sphere = icosa.createBuckyball(radius, subd);
 		for(Point3f p : sphere.getVertices())
 			p.add(center);
 		return sphere;
 	}
 
 	public static void saveSphere(IndexedTriangleMesh sphere, String objpath) throws IOException {
 		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(objpath)));
 		out.println("# OBJ File");
 		out.println("g Sphere");
 		for(Point3f v : sphere.getVertices())
 			out.println("v " + v.x + " " + v.y + " " + v.z);
 		out.println("s 1");
 		int[] faces = sphere.getFaces();
 		for(int i = 0; i < faces.length; i += 3)
 			out.println("f " + faces[i] + " " + faces[i+1] + " " + faces[i+2]);
 		out.close();
 	}
 
 	public void saveSphere(String objpath) throws IOException {
 		saveSphere(sphere, objpath);
 	}
 
 	public static IndexedTriangleMesh loadSphere(String objpath) throws IOException {
 		BufferedReader in = new BufferedReader(new FileReader(objpath));
 		ArrayList<Point3f> points = new ArrayList<Point3f>();
 		ArrayList<Integer> faces = new ArrayList<Integer>();
 		String line = in.readLine();
 		while(line != null && !line.startsWith("v "))
 			line = in.readLine();
 
 		while(line != null && line.startsWith("v ")) {
 			String[] toks = line.split("\\s");
 			points.add(new Point3f(
 				Float.parseFloat(toks[1]),
 				Float.parseFloat(toks[2]),
 				Float.parseFloat(toks[3])));
 			line = in.readLine();
 		}
 
 		while(line != null && !line.startsWith("f "))
 			line = in.readLine();
 
 		while(line != null && line.startsWith("f ")) {
 			String[] toks = line.split("\\s");
 			faces.add(Integer.parseInt(toks[1]));
 			faces.add(Integer.parseInt(toks[2]));
 			faces.add(Integer.parseInt(toks[3]));
 			line = in.readLine();
 		}
 
 		Point3f[] vertices = new Point3f[points.size()];
 		points.toArray(vertices);
 
 		int[] f = new int[faces.size()];
 		for(int i = 0; i < f.length; i++)
 			f[i] = faces.get(i);
 		return new IndexedTriangleMesh(vertices, f);
 	}
 
 	public void saveMaxima(String path) throws IOException {
 		DataOutputStream out = new DataOutputStream(
 			new BufferedOutputStream(
 				new FileOutputStream(path)));
 		for(float f : maxima)
 			out.writeFloat(f);
 		out.close();
 	}
 
 	public void loadMaxima(String file) throws IOException {
 		maxima = new float[sphere.nVertices];
 		DataInputStream in = new DataInputStream(
 			new BufferedInputStream(
 				new FileInputStream(file)));
 		for(int i = 0; i < maxima.length; i++) {
 			try {
 				maxima[i] = in.readFloat();
 			} catch(EOFException e) {
 				break;
 			}
 		}
 		in.close();
 	}
 
 	public IndexedTriangleMesh getSphere() {
 		return sphere;
 	}
 
 	public float[] getMaxima() {
 		return maxima;
 	}
 
 	public void addMaxima(float[] maxima) {
 		for(int i = 0; i < this.maxima.length; i++)
 			this.maxima[i] += maxima[i];
 	}
 
 	public void smooth() {
 		int[] nNeighbors = new int[maxima.length];
 		float[] newMaxima = new float[maxima.length];
 
 		int[] faces = sphere.getFaces();
 		for(int i = 0; i < sphere.nFaces; i += 3) {
 			int f1 = faces[i];
 			int f2 = faces[i + 1];
 			int f3 = faces[i + 2];
 			nNeighbors[f1] += 2;
 			newMaxima[f1] += maxima[f2];
 			newMaxima[f1] += maxima[f3];
 			nNeighbors[f2] += 2;
 			newMaxima[f2] += maxima[f1];
 			newMaxima[f2] += maxima[f3];
 			nNeighbors[f3] += 2;
 			newMaxima[f3] += maxima[f1];
 			newMaxima[f3] += maxima[f2];
 		}
 		for(int i = 0; i < newMaxima.length; i++)
 			newMaxima[i] /= (nNeighbors[i] + 1);
 		maxima = newMaxima;
 	}
 
 	public boolean[] isMaximum() {
 		boolean[] maxs = new boolean[maxima.length];
 		for(int i = 0; i < maxs.length; i++)
 			maxs[i] = true;
 		int[] faces = sphere.getFaces();
 		for(int i = 0; i < sphere.nFaces; i += 3) {
 			int f1 = faces[i];
 			int f2 = faces[i + 1];
 			int f3 = faces[i + 2];
 			float m1 = maxima[f1];
 			float m2 = maxima[f2];
 			float m3 = maxima[f3];
 
 			if(m1 <= m2 || m1 <= m3)
 				maxs[f1] = false;
 			if(m2 <= m1 || m2 <= m3)
 				maxs[f2] = false;
 			if(m3 <= m1 || m3 <= m1)
 				maxs[f3] = false;
 		}
 		return maxs;
 	}
 
 	public void applyTransform(Matrix4f matrix) {
 		Matrix4f inverse = new Matrix4f(matrix);
 		inverse.invert();
 		applyInverseTransform(inverse);
 	}
 
 	public void applyTransform(FastMatrix matrix) {
 		applyInverseTransform(matrix.inverse());
 	}
 
 	public void applyInverseTransform(FastMatrix inverse) {
 		float[] newmaxima = new float[sphere.nVertices];
 		Point3f p = new Point3f();
 		Point3f[] vertices = sphere.getVertices();
 		for(int i = 0; i < vertices.length; i++) {
 			p.set(vertices[i]);
 			inverse.apply(p.x, p.y, p.z);
 			p.set((float)inverse.x, (float)inverse.y, (float)inverse.z);
 			p.sub(center); // TODO transform the center too?
 			double lat = Math.asin(p.z / radius);
 			double lon = Math.atan2(p.y / radius, p.x / radius);
 			newmaxima[i] = get((float)lon, (float)lat);
 		}
 		maxima = newmaxima;
 	}
 
 	public void applyInverseTransform(Matrix4f inverse) {
 		float[] newmaxima = new float[sphere.nVertices];
 		Point3f p = new Point3f();
 		Point3f[] vertices = sphere.getVertices();
 		for(int i = 0; i < vertices.length; i++) {
 			p.set(vertices[i]);
 			inverse.transform(p);
 			p.sub(center); // TODO transform the center too?
 			double lat = Math.asin(p.z / radius);
 			double lon = Math.atan2(p.y / radius, p.x / radius);
 			newmaxima[i] = get((float)lon, (float)lat);
 		}
 		maxima = newmaxima;
 	}
 
 	public void scaleMaxima(AngleWeighter weighter) {
 		for(int vIndex = 0; vIndex < sphere.nVertices; vIndex++) {
 			Point3f vertex = sphere.getVertices()[vIndex];
 			maxima[vIndex] *= weighter.getWeight(vertex, center);
 		}
 	}
 
 	public void prepareForProjection(int w, int h, int d, double pw, double ph, double pd, FusionWeight weighter) {
 
 		Vector3f dx = new Vector3f();
 		Point3f pos = new Point3f();
 		Point3i imagePos = new Point3i();
 		ArrayList<Point4> correspondences = new ArrayList<Point4>();
 		weights = new float[sphere.nVertices];
 
 		for(int vIndex = 0; vIndex < sphere.nVertices; vIndex++) {
 			Point3f vertex = sphere.getVertices()[vIndex];
 			weights[vIndex] = weighter.getWeight(vertex.x, vertex.y, vertex.z);
 			if(weights[vIndex] == 0)
 				continue;
 
 			dx.sub(vertex, center);
 			dx.normalize();
 
 			// calculate the distance needed to move to the neighbor pixel
 			float scale = 1f / (float)Math.max(Math.abs(dx.x / pw), Math.max(
 					Math.abs(dx.y / ph), Math.abs(dx.z / pd)));
 
 			int k = (int)Math.round(0.2f * radius / scale);
 
 			for(int i = -k; i <= k; i++) {
 				// TODO check if we are in the image at all
 
 				pos.scaleAdd(i * scale, dx, vertex);
 
 				// calculate the position in pixel dims
 				imagePos.x = (int)Math.round(pos.x / pw);
 				imagePos.y = (int)Math.round(pos.y / ph);
 				imagePos.z = (int)Math.round(pos.z / pd);
 
 				correspondences.add(new Point4(imagePos, vIndex));
 			}
 		}
 
 		// sort according to ascending z coordinate
 		Collections.sort(correspondences, new Comparator<Point4>() {
 			public int compare(Point4 p1, Point4 p2) {
 				if(p1.z < p2.z) return -1;
 				if(p1.z > p2.z) return +1;
 				return 0;
 			}
 		});
 
 		lut = new Point4[correspondences.size()];
 		correspondences.toArray(lut);
 	}
 
 	public void project(ImagePlus image) {
 		ImageStack stack = image.getStack();
 		int w = image.getWidth(), h = image.getHeight();
 		int wh = w * h;
 		int d = image.getStackSize();
 		maxima = new float[sphere.nVertices];
 		int lutIndex = 0;
 		for(int z = 0; z < d; z++) {
 			ImageProcessor ip = stack.getProcessor(z + 1);
 			Point4 p;
 			while(lutIndex < lut.length && (p = (Point4)lut[lutIndex++]).z == z) {
 				float v = 0;
 				if(p.x >= 0 && p.x < w && p.y >= 0 && p.y < h)
 					v = ip.getf(p.x, p.y);
 				if(v > maxima[p.vIndex])
 					maxima[p.vIndex] = v;
 			}
 			lutIndex--;
 		}
 		for(int i = 0; i < maxima.length; i++)
 			maxima[i] *= weights[i];
 	}
 
 	Point3f tmp = new Point3f();
 	Point3f[] nn = new Point3f[3];
 	// in radians
 	public float get(float longitude, float latitude) {
 		return get(Math.sin(longitude), Math.cos(longitude), Math.sin(latitude), Math.cos(latitude));
 	}
 
 	public float get(double sinLong, double cosLong, double sinLat, double cosLat) {
 		// get point on sphere
 		tmp.x = (float)(center.x + radius * cosLat * cosLong);
 		tmp.y = (float)(center.y + radius * cosLat * sinLong);
 		tmp.z = (float)(center.z + radius * sinLat);
 		return get(tmp);
 	}
 
 	public float get(Point3f p) {
 		// get three nearest neighbors
 		Node3D[] nn = nnSearch.findNNearestNeighbors(new Node3D(p), 3);
 		int i0 = vertexToIndex.get(nn[0].p);
 		int i1 = vertexToIndex.get(nn[1].p);
 		int i2 = vertexToIndex.get(nn[2].p);
 
 		// interpolate according to distance
 		float d0 = p.distance(nn[0].p);
 		float d1 = p.distance(nn[1].p);
 		float d2 = p.distance(nn[2].p);
 
 		if(d0 == 0) return maxima[i0];
 		if(d1 == 0) return maxima[i1];
 		if(d2 == 0) return maxima[i2];
 
 		float sum = 1 / d0 + 1 / d1 + 1 / d2;
 
 		d0 = 1 / d0 / sum;
 		d1 = 1 / d1 / sum;
 		d2 = 1 / d2 / sum;
 		float v0 = d0 * maxima[i0];
 		float v1 = d1 * maxima[i1];
 		float v2 = d2 * maxima[i2];
 		float ret = v0 + v1 + v2;
 		return ret;
 	}
 
 	public void getThreeNearestVertexIndices(Point3f p, int[] ret) {
 		Node3D[] nn = nnSearch.findNNearestNeighbors(new Node3D(p), 3);
 		ret[0] = vertexToIndex.get(nn[0].p);
 		ret[1] = vertexToIndex.get(nn[1].p);
 		ret[2] = vertexToIndex.get(nn[2].p);
 	}
 
 	public SphericalMaxProjection clone() {
 		SphericalMaxProjection cp = new SphericalMaxProjection(this.sphere, this.center, this.radius);
 		if(this.weights != null) {
 			cp.weights = new float[this.weights.length];
 			System.arraycopy(this.weights, 0, cp.weights, 0, this.weights.length);
 		}
 		if(this.maxima != null) {
 			cp.maxima = new float[this.maxima.length];
 			System.arraycopy(this.maxima, 0, cp.maxima, 0, this.maxima.length);
 		}
 		if(this.lut != null) {
 			cp.lut = new Point4[this.lut.length];
 			for(int i = 0; i < this.lut.length; i++)
 				cp.lut[i] = this.lut[i].clone();
 		}
 		return cp;
 	}
 
 	/**
 	 * Holds the image coordinates together with the vertex index.
 	 */
 	private final class Point4 {
 		final int vIndex;
 		final int x, y, z;
 
 		public Point4(Point3i p, int vIndex) {
 			this(p.x, p.y, p.z, vIndex);
 		}
 
 		public Point4(int x, int y, int z, int vIndex) {
 			this.x = x;
 			this.y = y;
 			this.z = z;
 			this.vIndex = vIndex;
 		}
 
 		public Point4 clone() {
 			return new Point4(this.x, this.y, this.z, this.vIndex);
 		}
 	}
 
 
 	private static class Node3D implements Leaf<Node3D> {
 
 		final Point3f p;
 
 		public Node3D(final Point3f p) {
 			this.p = p;
 		}
 
 		public Node3D(final Node3D node) {
 			this.p = (Point3f)node.p.clone();
 		}
 
 		@Override
 		public boolean isLeaf() {
 			return true;
 		}
 
 		public boolean equals(final Node3D o) {
 	                 return p.equals(o.p);
 		}
 
 		@Override
 		public float distanceTo(final Node3D o) {
 			return p.distance(o.p);
 		}
 
 		@Override
 		public float get(final int k) {
 			switch(k) {
 				case 0: return p.x;
 				case 1: return p.y;
 				case 2: return p.z;
 			}
 			return 0f;
 		}
 
 		@Override
 		public String toString() {
 			return p.toString();
 		}
 
 		@Override
 		public Node3D[] createArray(final int n) {
 			return new Node3D[n];
 		}
 
 		@Override
 		public int getNumDimensions() {
 			return 3;
 		}
 	}
 
 	public static void main(String[] args) throws Exception {
 		IndexedTriangleMesh mesh1 = createSphere(new Point3f(0, 0, 0), 1f, 5);
 		System.out.println("created");
 		saveSphere(mesh1, "/tmp/Sphere.obj");
 		System.out.println("saved");
 		IndexedTriangleMesh mesh2 = loadSphere("/tmp/Sphere.obj");
 		System.out.println("loaded");
 		if(mesh1.nVertices != mesh2.nVertices)
 			throw new RuntimeException("Not the same number of vertices");
 		for(int i = 0; i < mesh1.nVertices; i++) {
 			Point3f p1 = mesh1.getVertices()[i];
 			Point3f p2 = mesh2.getVertices()[i];
 			if(!p1.equals(p2))
 				throw new RuntimeException("Vertex positions do not match: " + p1 + " " + p2);
 		}
 		if(mesh1.nFaces != mesh2.nFaces)
 			throw new RuntimeException("Not the same number of faces");
 		for(int i = 0; i < mesh1.nFaces; i++) {
 			int p1 = mesh1.getFaces()[i];
 			int p2 = mesh2.getFaces()[i];
 			if(p1 != p2)
 				throw new RuntimeException("Faces do not match: " + p1 + " " + p2);
 		}
 		System.out.println("finished");
 	}
 }
