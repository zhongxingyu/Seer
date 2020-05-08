 package edu.washington.cs.games.ktuite.pointcraft;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.CharBuffer;
 import java.nio.DoubleBuffer;
 import java.util.ArrayList;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.util.vector.Vector3f;
 
 import toxi.geom.PointOctree;
 import toxi.geom.Vec3D;
 
 public class KdTreeOfPoints {
 
 	// stuff about the point cloud
 	public static int num_points;
 	public static DoubleBuffer point_positions;
 	public static DoubleBuffer point_colors;
 	private static PointOctree tree;
 	public static float min_corner[] = { Float.MAX_VALUE, Float.MAX_VALUE,
 			Float.MAX_VALUE };
 	public static float max_corner[] = { -1 * Float.MAX_VALUE,
 			-1 * Float.MAX_VALUE, -1 * Float.MAX_VALUE };
 
 	public static void loadRandom() {
 		num_points = 200;
 		point_colors = BufferUtils.createDoubleBuffer(num_points * 3);
 		point_positions = BufferUtils.createDoubleBuffer(num_points * 3);
 
 		for (int i = 0; i < num_points; i++) {
 			for (int k = 0; k < 3; k++) {
 				point_colors.put(i * 3 + k, Math.random());
 				point_positions.put(i * 3 + k, Math.random() * 0.01 - 0.005);
 			}
 		}
 	}
 
 	public static void load(String filename) {
 		try {
 			BufferedReader buf = new BufferedReader(new FileReader(filename));
 
 			if (buf.readLine().startsWith("ply")) {
 				parsePlyFile(buf, filename);
 				buildLookupTree();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static int queryKdTree(float x, float y, float z, float radius) {
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
 		System.out.println("starting to build lookup tree");
 		Vec3D center = new Vec3D(min_corner[0], min_corner[1], min_corner[2]);
 		float max_span = max_corner[0] - min_corner[0];
 		if (max_corner[1] - min_corner[1] > max_span)
 			max_span = max_corner[1] - min_corner[1];
 		if (max_corner[2] - min_corner[2] > max_span)
 			max_span = max_corner[2] - min_corner[2];
 
 		tree = new PointOctree(center, max_span);
 		tree.setMinNodeSize(max_span / 5);
 		
 
 		for (int i = 0; i < num_points; i++) {
 			tree.addPoint(new Vec3D((float) point_positions.get(i * 3 + 0),
 					(float) point_positions.get(i * 3 + 1),
 					(float) point_positions.get(i * 3 + 2)));
 		}
 		System.out.println("done adding " + tree.getPoints().size()
 				+ " points to lookup tree");
 	}
 
 	private static void parsePlyFile(BufferedReader buf, String filename) {
 		try {
 			int r_idx, g_idx, b_idx;
 			int x_idx, y_idx, z_idx;
 			r_idx = g_idx = b_idx = x_idx = y_idx = z_idx = 0;
 
 			boolean binary = false;
 			int chars_read = 0;
 
 			int count = 0;
 			String line = buf.readLine();
 			chars_read += (line.length() + 1);
 			while (!line.startsWith("end_header")) {
 				if (line.startsWith("element vertex")) {
 					num_points = Integer.parseInt(line.split("\\s+")[2]);
 					count = 0;
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
 				else if (line.contains("red"))
 					r_idx = count;
 				else if (line.contains("green"))
 					g_idx = count;
 				else if (line.contains("blue"))
 					b_idx = count;
 
 				if (line.contains("property"))
 					count++;
 
 				line = buf.readLine();
 				chars_read += (line.length() + 1);
 			}
 			chars_read += 4;
 
 			System.out.println(x_idx + "," + y_idx + "," + z_idx + "    "
 					+ r_idx + "," + g_idx + "," + b_idx);
 
 			point_colors = BufferUtils.createDoubleBuffer(num_points * 3);
 			point_positions = BufferUtils.createDoubleBuffer(num_points * 3);
 			point_colors.rewind();
 			point_positions.rewind();
 
 			if (!binary) {
 				int i = 0;
 				line = buf.readLine();
 				while (line != null) {
 					String[] split_line = line.split("\\s+");
 					point_colors
 							.put(Integer.parseInt(split_line[r_idx]) / 255.0);
 					point_colors
 							.put(Integer.parseInt(split_line[g_idx]) / 255.0);
 					point_colors
 							.put(Integer.parseInt(split_line[b_idx]) / 255.0);
 
 					point_positions.put(Double.parseDouble(split_line[x_idx]));
 					point_positions.put(Double.parseDouble(split_line[y_idx]));
 					point_positions.put(Double.parseDouble(split_line[z_idx]));
 
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
 
 				int len = count * 4 - 9;
 				byte[] pt = new byte[len];
 				ByteBuffer bb = BufferUtils.createByteBuffer(len);
 				bb.order(ByteOrder.LITTLE_ENDIAN);
 
 				System.out.println("reading " + count + " values with " + len
 						+ " bytes");
 				for (int h = 0; h < num_points; h++) {
 					stream.read(pt, 0, len);
 					bb.rewind();
 					bb.put(pt, 0, len);
 					bb.rewind();
 
					byte r = 0, g = 0, b = 0;
 					double x = 0, y = 0, z = 0;
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
 						} else if (i == x_idx) {
 							x = bb.getFloat();
 						} else if (i == y_idx) {
 							y = bb.getFloat();
 						} else if (i == z_idx) {
 							z = bb.getFloat();
 						} else {
 							float f = bb.getFloat();
 							// System.out.print(f + ", ");
 						}
 					}
 					// System.out.println("");
 
		
 					point_colors.put(r / 255.0);
 					point_colors.put(g / 255.0);
 					point_colors.put(b / 255.0);
 
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
 
					if (h % 10000 == 0){
 						System.out.println("points loaded: " + h + "/"
 								+ num_points);
						

					}
 				}
 
 			}
 
 			point_colors.rewind();
 			point_positions.rewind();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 }
