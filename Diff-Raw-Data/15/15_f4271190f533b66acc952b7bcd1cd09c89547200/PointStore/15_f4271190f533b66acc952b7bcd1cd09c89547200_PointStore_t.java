 package edu.washington.cs.games.ktuite.pointcraft;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.util.vector.Matrix3f;
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector3f;
 
 import toxi.geom.PointOctree;
 import toxi.geom.Vec3D;
 
 public class PointStore {
 
 	// stuff about the point cloud
 	public static int num_points, num_cameras;
 	public static FloatBuffer point_positions;
 	public static ByteBuffer point_colors;
 	// VBO state tracking
 	private static boolean point_vbo_dirty = false;
 
 	public static ByteBuffer point_properties;
 	private static PointOctree tree;
 	public static float min_corner[] = { Float.MAX_VALUE, Float.MAX_VALUE,
 			Float.MAX_VALUE };
 	public static float max_corner[] = { -1 * Float.MAX_VALUE,
 			-1 * Float.MAX_VALUE, -1 * Float.MAX_VALUE };
 	private static Map<Vec3D, Integer> index_map;
 	public static FloatBuffer camera_frusta_lines;
 	private static List<Camera> cameras = new LinkedList<Camera>();
 
 	private static class Camera {
 		Matrix3f r;
 		@SuppressWarnings("unused")
 		Vector3f t;
 		Vector3f pos;
 		float focal_length;
 		int w;
 		int h;
 
 		public Camera(float focal_length2, Matrix3f r2, Vector3f t2,
 				Vector3f pos2, int w2, int h2) {
 			focal_length = focal_length2;
 			r = r2;
 			t = t2;
 			pos = pos2;
 			w = w2;
 			h = h2;
 		}
 
 	}
 
 	private static void initBuffers() {
 		point_colors = BufferUtils.createByteBuffer(num_points * 4);
 		markPointVBODirty();
 		point_positions = BufferUtils.createFloatBuffer(num_points * 3);
 		point_properties = BufferUtils.createByteBuffer(num_points * 3);
 		camera_frusta_lines = BufferUtils
 				.createFloatBuffer(num_cameras * 3 * 16);
 		point_colors.rewind();
 		point_positions.rewind();
 		point_properties.rewind();
 
 		for (int i = 0; i < num_points; i++) {
 			point_properties.put(i, (byte) 0);
 		}
 	}
 
 	public static void markPointVBODirty() {
 		point_vbo_dirty = true;
 	}
 
 	public static void markPointVBOClean() {
 		point_vbo_dirty = false;
 	}
 
 	public static boolean isPointVBODirty() {
 		return point_vbo_dirty;
 	}
 
 	public static void loadRandom() {
 		num_points = 2000;
 		initBuffers();
 
 		for (int i = 0; i < num_points; i++) {
 			for (int k = 0; k < 3; k++) {
 				point_colors.put(i * 4 + k, (byte) (Math.random() * 255));
 				point_positions.put(i * 3 + k,
 						(float) (Math.random() * 0.01 - 0.005));
 			}
 			point_colors.put(i * 4 + 3, (byte) 255);
 		}
 		point_colors.rewind();
 		point_positions.rewind();
 		for (int k = 0; k < 3; k++) {
 			min_corner[k] = -0.005f;
 			max_corner[k] = 0.005f;
 		}
 		buildLookupTree();
 	}
 
 	public static void loadCube() {
 		int n = 50000;
 		num_points = n * 6;
 		float s = 0.05f;
 		initBuffers();
 
 		for (int i = 0; i < num_points; i++) {
 			double[] pos = new double[3];
 			for (int k = 0; k < 3; k++) {
 				pos[k] = Math.random();
 			}
 
 			for (int k = 0; k < 3; k++) {
 				float b = (float) (pos[k] * 2 * s - s);
 				point_positions.put(i * 3 + k, b);
 				point_colors.put(i * 4 + 3, (byte) 255);
 			}
 
 		}
 
 		for (int i = n * 0; i < n * 1; i++) {
 			point_positions.put(i * 3 + 0, -s);
 			byte[] color = { (byte) 153, 84, (byte) 153 };
 			for (int k = 0; k < 3; k++) {
 				point_colors.put(i * 4 + k, color[k]);
 			}
 		}
 		for (int i = n * 1; i < n * 2; i++) {
 			point_positions.put(i * 3 + 0, s);
 			byte[] color = { -1, -1, -1 };
 			for (int k = 0; k < 3; k++) {
 				point_colors.put(i * 4 + k, color[k]);
 			}
 		}
 		for (int i = n * 2; i < n * 3; i++) {
 			point_positions.put(i * 3 + 1, -s);
 			byte[] color = { 120, 84, (byte) 136 };
 			for (int k = 0; k < 3; k++) {
 				point_colors.put(i * 4 + k, color[k]);
 			}
 		}
 		for (int i = n * 3; i < n * 4; i++) {
 			point_positions.put(i * 3 + 1, s);
 			byte[] color = { 51, 25, 51 };
 			for (int k = 0; k < 3; k++) {
 				point_colors.put(i * 4 + k, color[k]);
 			}
 		}
 		for (int i = n * 4; i < n * 5; i++) {
 			point_positions.put(i * 3 + 2, -s);
 			byte[] color = { 120, 100, (byte) 153 };
 			for (int k = 0; k < 3; k++) {
 				point_colors.put(i * 4 + k, color[k]);
 			}
 		}
 		for (int i = n * 5; i < n * 6; i++) {
 			point_positions.put(i * 3 + 2, s);
 		}
 
 		point_colors.rewind();
 		point_positions.rewind();
 		for (int k = 0; k < 3; k++) {
 			min_corner[k] = (-1 * s) * 2;
 			max_corner[k] = s * 2;
 		}
 		buildLookupTree();
 	}
 
 	public static void load(String filename) {
 		try {
 			BufferedReader buf = new BufferedReader(new FileReader(filename));
 			String first_line = buf.readLine();
 
 			if (first_line.startsWith("ply")) {
 				parsePlyFile(buf, filename);
 				buildLookupTree();
 			} else if (first_line.contains("# Bundle file")) {
 				parseBundleFile(first_line, buf, filename);
 				buildLookupTree();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static int getNearestPoint(float x, float y, float z, float radius) {
 		ArrayList<Vec3D> results = tree.getPointsWithinSphere(
 				new Vec3D(x, y, z), radius);
 		if (results != null)
 			return index_map.get(results.get(0));
 		else
 			return 0;
 	}
 
 	public static int[] getNearestPoints(float x, float y, float z, float radius) {
 		ArrayList<Vec3D> results = tree.getPointsWithinSphere(
 				new Vec3D(x, y, z), radius);
 		if (results != null) {
 			int[] indices = new int[results.size()];
 			for (int i = 0; i < results.size(); i++) {
 				indices[i] = index_map.get(results.get(i));
 			}
 			return indices;
 		} else
 			return null;
 	}
 
 	public static int getNumPointsInSphere(float x, float y, float z,
 			float radius) {
 		ArrayList<Vec3D> results = tree.getPointsWithinSphere(
 				new Vec3D(x, y, z), radius);
 		if (results != null)
 			return results.size();
 		else
 			return 0;
 	}
 
 	public static Vector3f getCenter(float x, float y, float z, float radius) {
 		Vector3f center = new Vector3f();
 		ArrayList<Vec3D> results = tree.getPointsWithinSphere(
 				new Vec3D(x, y, z), radius);
 		if (results != null) {
 			for (Vec3D p : results) {
 				center.x += p.x;
 				center.y += p.y;
 				center.z += p.z;
 			}
 			center.scale(1f / results.size());
 			return center;
 		} else {
 			return null;
 		}
 	}
 
 	private static void buildLookupTree() {
 		point_positions.rewind();
 		point_colors.rewind();
		
		Main.world_scale = (float) ((float) ((PointStore.max_corner[1] - PointStore.min_corner[1])) / 0.071716);
		// lewis hall height for scale ref...
 
 		System.out.println("starting to build lookup tree");
 		Vec3D center = new Vec3D(min_corner[0], min_corner[1], min_corner[2]);
 		float max_span = max_corner[0] - min_corner[0];
 		if (max_corner[1] - min_corner[1] > max_span)
 			max_span = max_corner[1] - min_corner[1];
 		if (max_corner[2] - min_corner[2] > max_span)
 			max_span = max_corner[2] - min_corner[2];
 
 		tree = new PointOctree(center, max_span);
 		tree.setMinNodeSize(max_span / 2);
 
 		index_map = new HashMap<Vec3D, Integer>();
 
 		for (int i = 0; i < num_points; i++) {
 			final Vec3D vec = new Vec3D((float) point_positions.get(i * 3 + 0),
 					(float) point_positions.get(i * 3 + 1),
 					(float) point_positions.get(i * 3 + 2));
 			index_map.put(vec, i);
 			tree.addPoint(vec);
 		}
 		System.out.println("done adding " + tree.getPoints().size()
 				+ " points to lookup tree");
 
 		// tree.intersectsRay(arg0, arg1, arg2)
 	}
 
 	private static void parsePlyFile(BufferedReader buf, String filename) {
 		try {
 			int r_idx, g_idx, b_idx, a_idx;
 			int x_idx, y_idx, z_idx;
 			r_idx = g_idx = b_idx = x_idx = y_idx = z_idx = 0;
 			a_idx = -1;
 
 			initPointStore();
 
 			boolean binary = false;
 			int chars_read = 0;
 			int len = 0;
 			boolean vert_properties_started = false;
 			boolean vert_properties_finished = false;
 
 			int count = 0;
 			String line = buf.readLine();
 			chars_read += (line.length() + 1);
 			while (!line.startsWith("end_header")) {
 				if (line.startsWith("element vertex")) {
 					num_points = Integer.parseInt(line.split("\\s+")[2]);
 					count = 0;
 					vert_properties_started = true;
 				} else if (line.startsWith("element")) {
 					if (vert_properties_started) {
 						vert_properties_finished = true;
 					}
 				} else if (line.contains("cloud_id")) {
 					int cloud_id = Integer.parseInt(line.split(" ")[2]);
 					Main.server.setTextureServer(cloud_id);
 				}
 
 				if (line.contains("binary_little_endian")) {
 					binary = true;
 				}
 
 				if (line.contains(" x"))
 					x_idx = count;
 				else if (line.contains(" y"))
 					y_idx = count;
 				else if (line.contains(" z"))
 					z_idx = count;
 				else if (line.contains("red")) {
 					r_idx = count;
 					len -= 3;
 				} else if (line.contains("green")) {
 					g_idx = count;
 					len -= 3;
 				} else if (line.contains("blue")) {
 					b_idx = count;
 					len -= 3;
 				} else if (line.contains("alpha")) {
 					a_idx = count;
 					len -= 3;
 				}
 
 				if (line.contains("property") && vert_properties_started
 						&& !vert_properties_finished)
 					count++;
 
 				line = buf.readLine();
 				chars_read += (line.length() + 1);
 			}
 			chars_read += 4;
 
 			System.out.println(x_idx + "," + y_idx + "," + z_idx + "    "
 					+ r_idx + "," + g_idx + "," + b_idx);
 
 			initBuffers();
 
 			len += count * 4;
 
 			if (!binary) {
 				int i = 0;
 				line = buf.readLine();
 				for (int j = 0; j < num_points; j++) {
 					String[] split_line = line.split("\\s+");
 					point_colors
 							.put((byte) Integer.parseInt(split_line[r_idx]));
 					point_colors
 							.put((byte) Integer.parseInt(split_line[g_idx]));
 					point_colors
 							.put((byte) Integer.parseInt(split_line[b_idx]));
 					point_colors.put((byte) 255); // alpha
 
 					point_positions.put(Float.parseFloat(split_line[x_idx]));
 					point_positions.put(Float.parseFloat(split_line[y_idx]));
 					point_positions.put(Float.parseFloat(split_line[z_idx]));
 
 					for (int k = 0; k < 3; k++) {
 						if (point_positions.get(i * 3 + k) < min_corner[k]) {
 							min_corner[k] = (float) point_positions.get(i * 3
 									+ k);
 						}
 						if (point_positions.get(i * 3 + k) > max_corner[k]) {
 							max_corner[k] = (float) point_positions.get(i * 3
 									+ k);
 						}
 					}
 					line = buf.readLine();
 					i++;
 					if (i % 10000 == 0)
 						System.out.println("points loaded: " + i + "/"
 								+ num_points);
 				}
 			} else {
 				// advance the binary thing forward past the ascii
 				DataInputStream stream = new DataInputStream(
 						new FileInputStream(filename));
 				byte[] header = new byte[chars_read];
 				stream.read(header, 0, chars_read);
 
 				byte[] pt = new byte[len];
 				ByteBuffer bb = BufferUtils.createByteBuffer(len);
 				bb.order(ByteOrder.LITTLE_ENDIAN);
 
 				// System.out.println("reading " + count + " values with " + len
 				// + " bytes");
 
 				for (int h = 0; h < num_points; h++) {
 					stream.read(pt, 0, len);
 					bb.rewind();
 					bb.put(pt, 0, len);
 					bb.rewind();
 
 					byte r = 0, g = 0, b = 0;
 					float x = 0, y = 0, z = 0;
 					for (int i = 0; i < count; i++) {
 						if (i == r_idx) {
 							r = bb.get();
 							// System.out.print(r + ", ");
 						} else if (i == g_idx) {
 							g = bb.get();
 							// System.out.print(g + ", ");
 						} else if (i == b_idx) {
 							b = bb.get();
 							// System.out.print(b + ", ");
 						} else if (i == a_idx) {
 							bb.get();
 						} else if (i == x_idx) {
 							x = bb.getFloat();
 						} else if (i == y_idx) {
 							y = bb.getFloat();
 						} else if (i == z_idx) {
 							z = bb.getFloat();
 						} else {
 							bb.getFloat();
 							// System.out.print(f + ", ");
 						}
 					}
 					// System.out.println("");
 
 					point_colors.put(r);
 					point_colors.put(g);
 					point_colors.put(b);
 					point_colors.put((byte) 255);
 
 					point_positions.put(x);
 					point_positions.put(y);
 					point_positions.put(z);
 
 					for (int k = 0; k < 3; k++) {
 						if (point_positions.get(h * 3 + k) < min_corner[k]) {
 							min_corner[k] = (float) point_positions.get(h * 3
 									+ k);
 						}
 						if (point_positions.get(h * 3 + k) > max_corner[k]) {
 							max_corner[k] = (float) point_positions.get(h * 3
 									+ k);
 						}
 					}
 
 					if (h % 10000 == 0) {
 						System.out.println("points loaded: " + h + "/"
 								+ num_points);
 						// System.out.println(" " + r + "," + g + "," + b );
 
 					}
 				}
 
 			}
 
 			point_colors.rewind();
 			point_positions.rewind();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void initPointStore() {
 		for (int k = 0; k < 3; k++) {
 			min_corner[k] = Float.MAX_VALUE;
 			max_corner[k] = -1 * Float.MAX_VALUE;
 		}
 
 		if (Main.server != null)
 			Main.server.texture_server = null;
 	}
 
 	private static void parseBundleFile(String first_line, BufferedReader buf,
 			String filename) throws IOException {
 		System.out.println("Reading bundle file");
 
 		String version = "";
 		if (first_line.contains("v0.3"))
 			version = "0.3";
 		else if (first_line.contains("v0.5"))
 			version = "0.5";
 
 		String second_line = buf.readLine();
 		num_cameras = 0;
 		num_points = 0;
 
 		num_cameras = Integer.parseInt(second_line.split("\\s+")[0]);
 		num_points = Integer.parseInt(second_line.split("\\s+")[1]);
 
 		System.out.println("Version #: " + version + ", Reading " + num_cameras
 				+ " cameras and " + num_points + " points");
 
 		initBuffers();
 
 		Main.draw_cameras = true;
 
 		for (int i = 0; i < num_cameras; i++) {
 			float focal_length;
 			Matrix3f r = new Matrix3f();
 			Vector3f t = new Vector3f();
 			Vector2f rd = new Vector2f();
 			String[] s;
 			int w = 1600; // default image width
 			int h = 1200; // and height
 
 			if (version == "0.5") {
 				s = buf.readLine().split("\\s+"); // file name
 				w = Integer.parseInt(s[1]);
 				h = Integer.parseInt(s[2]);
 			}
 
 			s = buf.readLine().split("\\s+");
 			focal_length = Float.parseFloat(s[0]);
 			rd.x = Float.parseFloat(s[1]);
 			rd.y = Float.parseFloat(s[2]);
 
 			s = buf.readLine().split("\\s+");
 
 			r.m00 = Float.parseFloat(s[0]);
 			r.m10 = Float.parseFloat(s[1]);
 			r.m20 = Float.parseFloat(s[2]);
 
 			s = buf.readLine().split("\\s+");
 
 			r.m01 = Float.parseFloat(s[0]);
 			r.m11 = Float.parseFloat(s[1]);
 			r.m21 = Float.parseFloat(s[2]);
 
 			s = buf.readLine().split("\\s+");
 
 			r.m02 = Float.parseFloat(s[0]);
 			r.m12 = Float.parseFloat(s[1]);
 			r.m22 = Float.parseFloat(s[2]);
 
 			s = buf.readLine().split("\\s+");
 
 			t.x = Float.parseFloat(s[0]);
 			t.y = Float.parseFloat(s[1]);
 			t.z = Float.parseFloat(s[2]);
 
 			Vector3f pos = (Vector3f) Matrix3f.transform(
 					Matrix3f.transpose(r, null), t, null).scale(-1f);
 
 			cameras.add(new Camera(focal_length, r, t, pos, w, h));
 		}
 
 		for (int i = 0; i < num_points; i++) {
 			if (version == "0.5") {
 				buf.readLine(); // player id who owns the point
 			}
 			String[] point = buf.readLine().split("\\s+");
 			String[] color = buf.readLine().split("\\s+");
 			buf.readLine(); // features
 
 			point_colors.put((byte) Integer.parseInt(color[0]));
 			point_colors.put((byte) Integer.parseInt(color[1]));
 			point_colors.put((byte) Integer.parseInt(color[2]));
 			point_colors.put((byte) 255);
 
 			point_positions.put(Float.parseFloat(point[0]));
 			point_positions.put(Float.parseFloat(point[1]));
 			point_positions.put(Float.parseFloat(point[2]));
 
 			for (int k = 0; k < 3; k++) {
 				if (point_positions.get(i * 3 + k) < min_corner[k]) {
 					min_corner[k] = (float) point_positions.get(i * 3 + k);
 				}
 				if (point_positions.get(i * 3 + k) > max_corner[k]) {
 					max_corner[k] = (float) point_positions.get(i * 3 + k);
 				}
 			}
 		}
 

 
 		buildCameraFrustaWithWorldScale();
 
 	}
 
 	private static void buildCameraFrustaWithWorldScale() {
 		for (int i = 0; i < cameras.size(); i++) {
 			Camera c = cameras.get(i);
 			float scale = Main.world_scale / 2000f;
 			float ref_length = (float) (2.0e-3 * Math
 					.min(8000f, c.focal_length));
 			float xExt = 0.5f * c.w * ref_length * scale / c.focal_length;
 			float yExt = 0.5f * c.h * ref_length * scale / c.focal_length;
 			Vector3f pos = c.pos;
 
 			Vector3f pt0 = new Vector3f(-xExt, -yExt, -ref_length * scale);
 			pt0 = Vector3f.add(Matrix3f.transform(
 					Matrix3f.transpose(c.r, null), pt0, null), pos, null);
 
 			Vector3f pt1 = new Vector3f(-xExt, yExt, -ref_length * scale);
 			pt1 = Vector3f.add(Matrix3f.transform(
 					Matrix3f.transpose(c.r, null), pt1, null), pos, null);
 
 			Vector3f pt2 = new Vector3f(xExt, yExt, -ref_length * scale);
 			pt2 = Vector3f.add(Matrix3f.transform(
 					Matrix3f.transpose(c.r, null), pt2, null), pos, null);
 
 			Vector3f pt3 = new Vector3f(xExt, -yExt, -ref_length * scale);
 			pt3 = Vector3f.add(Matrix3f.transform(
 					Matrix3f.transpose(c.r, null), pt3, null), pos, null);
 
 			// center of plane
 			Vector3f pt4 = new Vector3f(0, 0, -ref_length * scale);
 			pt4 = Vector3f.add(Matrix3f.transform(c.r, pt4, null), pos, null);
 
 			int n = 16;
 			addFrustaVertex(i, n, 0, pos);
 			addFrustaVertex(i, n, 1, pt0);
 			addFrustaVertex(i, n, 2, pos);
 			addFrustaVertex(i, n, 3, pt1);
 			addFrustaVertex(i, n, 4, pos);
 			addFrustaVertex(i, n, 5, pt2);
 			addFrustaVertex(i, n, 6, pos);
 			addFrustaVertex(i, n, 7, pt3);
 
 			addFrustaVertex(i, n, 8, pt0);
 			addFrustaVertex(i, n, 9, pt1);
 
 			addFrustaVertex(i, n, 10, pt1);
 			addFrustaVertex(i, n, 11, pt2);
 
 			addFrustaVertex(i, n, 12, pt2);
 			addFrustaVertex(i, n, 13, pt3);
 
 			addFrustaVertex(i, n, 14, pt3);
 			addFrustaVertex(i, n, 15, pt0);
 		}
 	}
 
 	private static void addFrustaVertex(int i, int n, int s, Vector3f v) {
 		camera_frusta_lines.put(i * n * 3 + s * 3 + 0, v.x);
 		camera_frusta_lines.put(i * n * 3 + s * 3 + 1, v.y);
 		camera_frusta_lines.put(i * n * 3 + s * 3 + 2, v.z);
 	}
 
 	public static Vector3f getIthPoint(int i) {
 		return new Vector3f((float) point_positions.get(i * 3 + 0),
 				(float) point_positions.get(i * 3 + 1),
 				(float) point_positions.get(i * 3 + 2));
 	}
 
 	public static void changePointColorToRed(int i) {
 		point_colors.put(i * 3 + 0, (byte) 255);
 		point_colors.put(i * 3 + 1, (byte) 0);
 		point_colors.put(i * 3 + 2, (byte) 0);
 		markPointVBODirty();
 	}
 
 	public static void makePointTransparent(int i) {
 		point_colors.put(i * 4 + 3, (byte) 100);
 		markPointVBODirty();
 	}
 
 	public static void changePointColorToGray(int i) {
 		point_colors.put(i * 3 + 0, (byte) 130);
 		point_colors.put(i * 3 + 1, (byte) 130);
 		point_colors.put(i * 3 + 2, (byte) 130);
 		markPointVBODirty();
 	}
 }
