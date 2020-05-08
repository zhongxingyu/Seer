 package huisken.projection;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.vecmath.Matrix4f;
 import javax.vecmath.Point2f;
 import javax.vecmath.Point3f;
 import javax.vecmath.Point3i;
 import javax.vecmath.Vector3f;
 
 import meshtools.IndexedTriangleMesh;
 import fiji.util.KDTree;
 import fiji.util.NearestNeighborSearch;
 import fiji.util.node.Leaf;
 
 public class SphericalMaxProjection {
 
 	// These fields are set in prepareForProjection();
 	private int[][] lutxy;
 	private int[][] luti;
 	private short[] maxima;
 
 	// These fields must be set in the constructor and
 	// contain info about the sphere geometry
 	final Point3f center;
 	final float radius;
 	private final IndexedTriangleMesh sphere;
 	private final KDTree<Node3D> tree;
 
 	public SphericalMaxProjection(IndexedTriangleMesh sphere, Point3f center, float radius) {
 		this(sphere, center, radius, null);
 	}
 
 	public SphericalMaxProjection(IndexedTriangleMesh sph, Point3f c, float radius, Matrix4f transform) {
 		this.center = new Point3f(c);
 		this.radius = radius;
 
 		this.sphere = (IndexedTriangleMesh)sph.clone();
 
 		if(transform != null) {
 			for(Point3f v : sphere.getVertices())
 				transform.transform(v);
 			transform.transform(center);
 		}
 
 		ArrayList<Node3D> nodes = new ArrayList<Node3D>(sphere.nFaces / 3);
 		for(int i = 0; i < sphere.nFaces / 3; i++)
 			nodes.add(new Node3D(calculateCOG(i), i));
 		tree = new KDTree<Node3D>(nodes);
 	}
 
 	private Point3f calculateCOG(int triangle) {
 		Point3f[] v = sphere.getVertices();
 		int[] faces = sphere.getFaces();
 		int f1 = faces[triangle * 3];
 		int f2 = faces[triangle * 3 + 1];
 		int f3 = faces[triangle * 3 + 2];
 		Point3f p = new Point3f();
 		p.x = (v[f1].x + v[f2].x + v[f3].x) / 3f;
 		p.y = (v[f1].y + v[f2].y + v[f3].y) / 3f;
 		p.z = (v[f1].z + v[f2].z + v[f3].z) / 3f;
 		return p;
 	}
 
 	public float[] createLines(float tolInDegree, float value) {
 		float[] ret = new float[sphere.nVertices];
 		Point2f polar = new Point2f();
 		for(int v = 0; v < sphere.nVertices; v++) {
 			Point3f vtx = sphere.getVertices()[v];
 			getPolar(vtx, polar);
 
 			polar.x = (float)(polar.x * 180 / Math.PI);
 			polar.y = (float)(polar.y * 180 / Math.PI);
 
 			if((polar.x + 360) % 30 < tolInDegree)
 				ret[v] = value;
 			else if((polar.y + 360) % 30 < tolInDegree)
 				ret[v] = value;
 			else
 				ret[v] = 0;
 		}
 		return ret;
 	}
 
 	public SphericalMaxProjection(String objfile) throws IOException {
 		this(objfile, null);
 	}
 
 	public SphericalMaxProjection(String objfile, Matrix4f transform) throws IOException {
 		this.sphere = loadSphere(objfile);
 		if(transform != null) {
 			for(Point3f v : sphere.getVertices())
 				transform.transform(v);
 		}
 
 		double mx = 0, my = 0, mz = 0;
 		for(Point3f p : sphere.getVertices()) {
 			mx += p.x;
 			my += p.y;
 			mz += p.z;
 		}
 		this.center = new Point3f(
 			(float)(mx / sphere.nVertices),
 			(float)(my / sphere.nVertices),
 			(float)(mz / sphere.nVertices));
 		this.radius = sphere.getVertices()[0].distance(center);
 
 		ArrayList<Node3D> nodes = new ArrayList<Node3D>(sphere.nFaces / 3);
 		for(int i = 0; i < sphere.nFaces / 3; i++)
 			nodes.add(new Node3D(calculateCOG(i), i));
 
 		tree = new KDTree<Node3D>(nodes);
 	}
 
 	public Point3f getCenter() {
 		return center;
 	}
 
 	public float getRadius() {
 		return radius;
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
 		saveShortData(maxima, path);
 	}
 
 	public static void saveIntData(int[] data, String path) throws IOException {
 		DataOutputStream out = new DataOutputStream(
 			new BufferedOutputStream(
 				new FileOutputStream(path)));
 		for(int f : data)
 			out.writeInt(f);
 		out.close();
 	}
 
 	public static void saveShortData(short[] data, String path) throws IOException {
 		DataOutputStream out = new DataOutputStream(
 			new BufferedOutputStream(
 				new FileOutputStream(path)));
 		for(short f : data)
 			out.writeShort(f);
 		out.close();
 	}
 
 	public void loadMaxima(String file) throws IOException {
 		maxima = loadShortData(file, sphere.nVertices);
 	}
 
 	/**
 	 * Loads data from a file, either as floats or shorts, depending on
 	 * the file size, and converts them to shorts.
 	 * @param file
 	 * @param n
 	 * @return
 	 * @throws IOException
 	 */
 	public static short[] loadShortData(String file, int n) throws IOException {
 		File f = new File(file);
 		DataInputStream in = new DataInputStream(
 			new BufferedInputStream(
 				new FileInputStream(f)));
 
 		long nBytes = f.length();
 		short[] data = new short[n];
 
 		// legacy: read float data
 		if(nBytes == n * 4) {
 			for(int i = 0; i < n; i++) {
 				try {
 					data[i] = (short)in.readFloat();
 				} catch(EOFException e) {
 					break;
 				}
 			}
 		} else {
 			for(int i = 0; i < n; i++) {
 				try {
 					data[i] = in.readShort();
 				} catch(EOFException e) {
 					break;
 				}
 			}
 		}
 		in.close();
 		return data;
 	}
 
 	public static int[] loadIntData(String file, int n) throws IOException {
 		int[] data = new int[n];
 		DataInputStream in = new DataInputStream(
 			new BufferedInputStream(
 				new FileInputStream(file)));
 		for(int i = 0; i < data.length; i++) {
 			try {
 				data[i] = in.readInt();
 			} catch(EOFException e) {
 				break;
 			}
 		}
 		in.close();
 		return data;
 	}
 
 	public IndexedTriangleMesh getSphere() {
 		return sphere;
 	}
 
 	public short[] getMaxima() {
 		return maxima;
 	}
 
 	public void setMaxima(short[] maxima) {
 		this.maxima = maxima;
 	}
 
 	public static void add(short[] d1, short v) { // TODO check overflow
 		for(int i = 0; i < d1.length; i++)
 			d1[i] += v;
 	}
 
 	public static float getMode(short[] data) {
 		int nBins = 65536;
 		int[] histogram = new int[nBins];
 		for(int i = 0; i < data.length; i++) {
 			int v = (data[i] & 0xffff);
 			if(v > 0)
 				histogram[v]++;
 		}
 
 		int mode = 0;
 		int maxn = histogram[0];
 		for(int i = 1; i < histogram.length; i++) {
 			int n = histogram[i];
 			if(n > maxn) {
 				maxn = n;
 				mode = i;
 			}
 		}
 		return mode;
 	}
 
 	public static float getMean(short[] data) {
 		double mean = 0.0;
 		double n = 0;
 		for(short v : data) {
 			mean += v;
 			n++;
 		}
 		return (float)(mean / n);
 	}
 
 	public float getMean() {
 		return getMean(getMaxima());
 	}
 
 	public void smooth() {
 		int[] nNeighbors = new int[maxima.length];
 		float[] newMaxima = new float[maxima.length];
		for(int i = 0; i < maxima.length; i++)
			newMaxima[i] = maxima[i];
 
 		int[] faces = sphere.getFaces();
 		for(int i = 0; i < sphere.nFaces; i += 3) {
 			int f1 = faces[i];
 			int f2 = faces[i + 1];
 			int f3 = faces[i + 2];
 			nNeighbors[f1] += 2;
 			newMaxima[f1] += (maxima[f2] & 0xffff);
 			newMaxima[f1] += (maxima[f3] & 0xffff);
 			nNeighbors[f2] += 2;
 			newMaxima[f2] += (maxima[f1] & 0xffff);
 			newMaxima[f2] += (maxima[f3] & 0xffff);
 			nNeighbors[f3] += 2;
 			newMaxima[f3] += (maxima[f1] & 0xffff);
 			newMaxima[f3] += (maxima[f2] & 0xffff);
 		}
 		for(int i = 0; i < newMaxima.length; i++)
 			maxima[i] = (short)(newMaxima[i] / (nNeighbors[i] + 1));
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
 			float m1 = (maxima[f1] & 0xffff);
 			float m2 = (maxima[f2] & 0xffff);
 			float m3 = (maxima[f3] & 0xffff);
 
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
 
 	public int[] applyTransformNearestNeighbor(Matrix4f matrix, int[] data) {
 		Matrix4f inverse = new Matrix4f(matrix);
 		inverse.invert();
 		return applyInverseTransformNearestNeighbor(inverse, data);
 	}
 
 	public int[] applyInverseTransformNearestNeighbor(final Matrix4f inverse, final int[] data) {
 		final int[] newmaxima = new int[maxima.length];
 		final Point3f[] vertices = sphere.getVertices();
 
 		final int nProcessors = Runtime.getRuntime().availableProcessors();
 		ExecutorService exec = Executors.newFixedThreadPool(nProcessors);
 
 		int nVerticesPerThread = (int)Math.ceil(vertices.length / (double)nProcessors);
 		for(int p = 0; p < nProcessors; p++) {
 			final int start = p * nVerticesPerThread;
 			final int end = Math.min(vertices.length, (p + 1) * nVerticesPerThread);
 
 			exec.submit(new Runnable() {
 				@Override
 				public void run() {
 					Point3f p = new Point3f();
 					for(int i = start; i < end; i++) {
 						p.set(vertices[i]);
 						inverse.transform(p);
 						newmaxima[i] = (int)getNearestNeighborValue(p, data);
 					}
 				}
 			});
 		}
 		try {
 			exec.shutdown();
 			exec.awaitTermination(300, TimeUnit.MINUTES);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return newmaxima;
 	}
 
 	public void applyInverseTransform(final Matrix4f inverse) {
 		final short[] newmaxima = new short[sphere.nVertices];
 		final Point3f[] vertices = sphere.getVertices();
 
 		final int nProcessors = Runtime.getRuntime().availableProcessors();
 		ExecutorService exec = Executors.newFixedThreadPool(nProcessors);
 
 		int nVerticesPerThread = (int)Math.ceil(vertices.length / (double)nProcessors);
 		for(int p = 0; p < nProcessors; p++) {
 			final int start = p * nVerticesPerThread;
 			final int end = Math.min(vertices.length, (p + 1) * nVerticesPerThread);
 
 			exec.submit(new Runnable() {
 				@Override
 				public void run() {
 					Point3f p = new Point3f();
 					for(int i = start; i < end; i++) {
 						p.set(vertices[i]);
 						inverse.transform(p);
 						newmaxima[i] = (short)getInterpolatedValue(p);
 					}
 				}
 			});
 		}
 		try {
 			exec.shutdown();
 			exec.awaitTermination(300, TimeUnit.MINUTES);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		maxima = newmaxima;
 	}
 
 	public void prepareForProjection(final int w, final int h, final int d, final double pw, final double ph, final double pd, final FusionWeight weighter) {
 
 		final int nProcessors = Runtime.getRuntime().availableProcessors();
 		ExecutorService exec = Executors.newFixedThreadPool(nProcessors);
 
 		@SuppressWarnings("unchecked")
 		final ArrayList<Point3i>[] all_correspondences = new ArrayList[d];
 		for(int i = 0; i < d; i++)
 			all_correspondences[i] = new ArrayList<Point3i>();
 
 		for(int proc = 0; proc < nProcessors; proc++) {
 			final int currentProc = proc;
 			exec.execute(new Runnable() {
 				@Override
 				public void run() {
 					Vector3f dx = new Vector3f();
 					Point3f pos = new Point3f();
 
 					@SuppressWarnings("unchecked")
 					ArrayList<Point3i>[] correspondences = new ArrayList[d];
 					for(int i = 0; i < d; i++)
 						correspondences[i] = new ArrayList<Point3i>();
 
 					int nVerticesPerThread = (int)Math.ceil(sphere.nVertices / (double)nProcessors);
 					int startV = currentProc * nVerticesPerThread;
 					int lenV = Math.min((currentProc + 1) * nVerticesPerThread, sphere.nVertices);
 					for(int vIndex = startV; vIndex < lenV; vIndex++) {
 						Point3f vertex = sphere.getVertices()[vIndex];
 						float weight = weighter.getWeight(vertex.x, vertex.y, vertex.z);
 						if(weight == 0)
 							continue;
 
 						dx.sub(vertex, center);
 						dx.normalize();
 
 						// calculate the distance needed to move to the neighbor pixel
 						float scale = 1f / (float)Math.max(Math.abs(dx.x / pw), Math.max(
 								Math.abs(dx.y / ph), Math.abs(dx.z / pd)));
 
 						int k = Math.round(0.4f * radius / scale);
 
 						for(int i = -k; i <= k; i++) {
 							pos.scaleAdd(i * scale, dx, vertex);
 
 							// calculate the position in pixel dims
 							int x = (int)Math.round(pos.x / pw);
 							int y = (int)Math.round(pos.y / ph);
 							int z = (int)Math.round(pos.z / pd);
 
 							// only add it if the pixel is inside the image
 							if(x >= 0 && x < w && y >= 0 && y < h && z >= 0 && z < d)
 								correspondences[z].add(new Point3i(x, y, vIndex));
 						}
 					}
 					synchronized(SphericalMaxProjection.this) {
 						System.out.println("processor " + currentProc + ": before" + SphericalMaxProjection.this.hashCode());
 						for(int i = 0; i < d; i++) {
 							try {
 								all_correspondences[i].addAll(correspondences[i]);
 							} catch(Throwable t) {
 								System.out.println("i = " + i);
 								System.out.println(all_correspondences[i].size());
 								System.out.println(correspondences[i].size());
 							}
 						}
 						System.out.println("processor " + currentProc + ": after " + SphericalMaxProjection.this.hashCode());
 					}
 				}
 			});
 		}
 
 		try {
 			exec.shutdown();
 			exec.awaitTermination(30, TimeUnit.MINUTES);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		lutxy = new int[d][];
 		luti = new int[d][];
 		for(int i = 0; i < d; i++) {
 			int l = all_correspondences[i].size();
 			lutxy[i] = new int[l];
 			luti[i] = new int[l];
 
 			for(int j = 0; j < l; j++) {
 				Point3i p = all_correspondences[i].get(j);
 				lutxy[i][j] = p.y * w + p.x;
 				luti[i][j] = p.z;
 			}
 		}
 	}
 
 	public void resetMaxima() {
 		maxima = new short[sphere.nVertices];
 	}
 
 	/*
 	 * z starts with 0;
 	 */
 	public void projectPlane(int z, short[] ip) {
 		for(int i = 0; i < luti[z].length; i++) {
 			// float v = ip.getf(lutx[z][i], luty[z][i]);
 			float v = ip[lutxy[z][i]] & 0xffff;
 			if(v > (maxima[luti[z][i]] & 0xffff))
 				maxima[luti[z][i]] = (short)v;
 		}
 	}
 
 	// in rad
 	public void getPolar(Point3f in, Point2f out) {
 		in = new Point3f(in);
 		in.x -= center.x;
 		in.y -= center.y;
 		in.z -= center.z;
 		double lon = Math.atan2(-in.x, in.z);
 		double lat = Math.asin(-in.y / radius);
 		out.x = (float)lon;
 		out.y = (float)lat;
 	}
 
 	private final Point3f tmp = new Point3f();
 	// in radians
 	public float get(float longitude, float latitude) {
 		return getInterpolatedValue(Math.sin(longitude), Math.cos(longitude), Math.sin(latitude), Math.cos(latitude));
 	}
 
 	public void getPoint(float longitude, float latitude, Point3f ret) {
 		getPoint(Math.sin(longitude), Math.cos(longitude), Math.sin(latitude), Math.cos(latitude), ret);
 	}
 
 	public void getPoint(double sinLong, double cosLong, double sinLat, double cosLat, Point3f ret) {
 		ret.z = (float)(center.z + radius * cosLat * cosLong);
 		ret.x = (float)(center.x - radius * cosLat * sinLong);
 		ret.y = (float)(center.y - radius * sinLat);
 	}
 
 	public float getInterpolatedValue(double sinLong, double cosLong, double sinLat, double cosLat) {
 		// get point on sphere
 		getPoint(sinLong, cosLong, sinLat, cosLat, tmp);
 		return getInterpolatedValue(tmp);
 	}
 
 	public int getNearestNeighbor(Point3f p) {
 		int[] nn = new int[3];
 		getThreeNearestVertexIndices(p, nn);
 
 		int minIdx = nn[0];
 		float minDist = p.distance(sphere.getVertices()[minIdx]);
 		for(int i = 1; i < 3; i++) {
 			int vIdx = nn[i];
 			float dist = p.distance(sphere.getVertices()[vIdx]);
 			if(dist < minDist) {
 				minDist = dist;
 				minIdx = vIdx;
 			}
 		}
 		return minIdx;
 	}
 
 	public float getNearestNeighborValue(Point3f p, int[] data) {
 		int idx = getNearestNeighbor(p);
 		return data[idx];
 	}
 
 	public float getNearestNeighborValue(Point3f p) {
 		int idx = getNearestNeighbor(p);
 		return maxima[idx];
 	}
 
 	public float getInterpolatedValue(Point3f p) {
 		// get three nearest neighbors
 		int[] nn = new int[3];
 		getThreeNearestVertexIndices(p, nn);
 		Point3f[] vertices = sphere.getVertices();
 
 		int i0 = nn[0];
 		int i1 = nn[1];
 		int i2 = nn[2];
 
 		// interpolate according to distance
 		float d0 = p.distance(vertices[i0]);
 		float d1 = p.distance(vertices[i1]);
 		float d2 = p.distance(vertices[i2]);
 
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
 		NearestNeighborSearch<Node3D> nnSearch = new NearestNeighborSearch<Node3D>(tree);
 		Node3D nearest = nnSearch.findNearestNeighbor(new Node3D(p, -1));
 		int triangleIdx = nearest.triangleIdx;
 		int[] faces = sphere.getFaces();
 		ret[0] = faces[triangleIdx * 3];
 		ret[1] = faces[triangleIdx * 3 + 1];
 		ret[2] = faces[triangleIdx * 3 + 2];
 	}
 
 	@Override
 	public SphericalMaxProjection clone() {
 		SphericalMaxProjection cp = new SphericalMaxProjection(this.sphere, this.center, this.radius);
 		if(this.maxima != null) {
 			cp.maxima = new short[this.maxima.length];
 			System.arraycopy(this.maxima, 0, cp.maxima, 0, this.maxima.length);
 		}
 		if(this.luti != null) {
 			int l = this.luti.length;
 			cp.luti = new int[l][];
 			cp.lutxy = new int[l][];
 			for(int z = 0; z < l; z++) {
 				int lz = this.luti[z].length;
 				cp.luti[z] = new int[lz];
 				cp.lutxy[z] = new int[lz];
 				System.arraycopy(this.luti[z], 0, cp.luti[z], 0, lz);
 				System.arraycopy(this.lutxy[z], 0, cp.lutxy[z], 0, lz);
 			}
 		}
 		return cp;
 	}
 
 	private static class Node3D implements Leaf<Node3D> {
 
 		final Point3f p;
 		final int triangleIdx;
 
 		public Node3D(final Point3f p, final int triangleIdx) {
 			this.p = p;
 			this.triangleIdx = triangleIdx;
 		}
 
 		@SuppressWarnings("unused")
 		public Node3D(final Node3D node) {
 			this.p = (Point3f)node.p.clone();
 			this.triangleIdx = node.triangleIdx;
 		}
 
 		@Override
 		public boolean isLeaf() {
 			return true;
 		}
 
 		@SuppressWarnings("unused")
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
 }
