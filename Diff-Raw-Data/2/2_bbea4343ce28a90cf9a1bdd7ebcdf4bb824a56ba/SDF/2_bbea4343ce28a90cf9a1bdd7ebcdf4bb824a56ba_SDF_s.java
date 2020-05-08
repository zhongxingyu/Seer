package hialin.serv;
 
 import java.awt.image.BufferedImage;
 
 public class SDF {
 	private SDF(BufferedImage bi) {
 		w = bi.getWidth();
 		h = bi.getHeight();
 		X = new short[w][h];
 		Y = new short[w][h];
 	}
 
 	final int w, h;
 	final short[][] X, Y;
 
 	static class Point {
 		public int x, y;
 
 		Point(int x, int y) {
 			this.x = x;
 			this.y = y;
 		}
 	}
 
 	static final int INF = 4096;
 	static final Point inside = new Point(0, 0);
 	static final Point empty = new Point(INF, INF);
 
 	static int distSq(int dx, int dy) {
 		return dx * dx + dy * dy;
 	}
 
 	Point getP(int x, int y) {
 		boolean in = (x >= 0 && y >= 0 && x < w && y < h);
 		return in ? new Point(X[x][y], Y[x][y]) : empty;
 	}
 
 	void getP(Point p, int x, int y) {
 		boolean in = (x >= 0 && y >= 0 && x < w && y < h);
 		p.x = in ? X[x][y] : INF;
 		p.y = in ? Y[x][y] : INF;
 	}
 
 	void putP(int x, int y, Point p) {
 		X[x][y] = (short) p.x;
 		Y[x][y] = (short) p.y;
 	}
 
 	void put(int x, int y, int dx, int dy) {
 		// expect(Short.MIN_VALUE <= dx && dx <= Short.MAX_VALUE);
 		// expect(Short.MIN_VALUE <= dy && dy <= Short.MAX_VALUE);
 		X[x][y] = (short) dx;
 		Y[x][y] = (short) dy;
 	}
 
 	void compareAndUpdate(Point p, int x, int y, int offsetx, int offsety) {
 		int nx = x + offsetx, ny = y + offsety;
 		boolean in = (nx >= 0 && ny >= 0 && nx < w && ny < h);
 		if (in) {
 			int ndx = X[nx][ny] + offsetx;
 			int ndy = Y[nx][ny] + offsety;
 			if (distSq(ndx, ndy) < distSq(p.x, p.y)) {
 				p.x = ndx;
 				p.y = ndy;
 			}
 		}
 	}
 
 	void generateSDF() throws Exception {
 		// Pass 0
 		for (int y = 0; y < h; y++) {
 			for (int x = 0; x < w; x++) {
 				int dx = X[x][y];
 				int dy = Y[x][y];
 				int dsq = distSq(dx, dy);
 				{
 					int nx = x + -1;
 					if (nx >= 0) {
 						int ndx = X[nx][y] + -1, ndy = Y[nx][y];
 						int ndsq = distSq(ndx, ndy);
 						if (ndsq < dsq) {
 							dx = ndx;
 							dy = ndy;
 							dsq = ndsq;
 						}
 					}
 				}
 				{
 					int ny = y + -1;
 					if (ny >= 0) {
 						int ndx = X[x][ny], ndy = Y[x][ny] + -1;
 						int ndsq = distSq(ndx, ndy);
 						if (ndsq < dsq) {
 							dx = ndx;
 							dy = ndy;
 							dsq = ndsq;
 						}
 					}
 				}
 				{
 					int nx = x + -1, ny = y + -1;
 					if (nx >= 0 && ny >= 0) {
 						int ndx = X[nx][ny] + -1, ndy = Y[nx][ny] + -1;
 						int ndsq = distSq(ndx, ndy);
 						if (ndsq < dsq) {
 							dx = ndx;
 							dy = ndy;
 							dsq = ndsq;
 						}
 					}
 				}
 				{
 					int nx = x + 1, ny = y + -1;
 					if (ny >= 0 && nx < w) {
 						int ndx = X[nx][ny] + 1, ndy = Y[nx][ny] + -1;
 						int ndsq = distSq(ndx, ndy);
 						if (ndsq < dsq) {
 							dx = ndx;
 							dy = ndy;
 							dsq = ndsq;
 						}
 					}
 				}
 				put(x, y, dx, dy);
 				//
 				// getP(p, x, y);
 				// compareAndUpdate(p, x, y, -1, 0);
 				// compareAndUpdate(p, x, y, 0, -1);
 				// compareAndUpdate(p, x, y, -1, -1);
 				// compareAndUpdate(p, x, y, 1, -1);
 				// putP(x, y, p);
 			}
 
 			for (int x = w - 1; x >= 0; x--) {
 				int dx = X[x][y];
 				int dy = Y[x][y];
 				int dsq = distSq(dx, dy);
 				{
 					int nx = x + 1, ny = y + 0;
 					if (nx < w) {
 						int ndx = X[nx][ny] + 1, ndy = Y[nx][ny] + 0;
 						int ndsq = distSq(ndx, ndy);
 						if (ndsq < dsq) {
 							dx = ndx;
 							dy = ndy;
 							dsq = ndsq;
 						}
 					}
 				}
 				put(x, y, dx, dy);
 				// getP(p, x, y);
 				// compareAndUpdate(p, x, y, 1, 0);
 				// putP(x, y, p);
 			}
 		}
 
 		// Pass 1
 		for (int y = h - 1; y >= 0; y--) {
 			for (int x = w - 1; x >= 0; x--) {
 				int dx = X[x][y];
 				int dy = Y[x][y];
 				int dsq = distSq(dx, dy);
 				{
 					int nx = x + 1;
 					if (nx < w) {
 						int ndx = X[nx][y] + 1, ndy = Y[nx][y];
 						int ndsq = distSq(ndx, ndy);
 						if (ndsq < dsq) {
 							dx = ndx;
 							dy = ndy;
 							dsq = ndsq;
 						}
 					}
 				}
 				{
 					int ny = y + 1;
 					if (ny < h) {
 						int ndx = X[x][ny], ndy = Y[x][ny] + 1;
 						int ndsq = distSq(ndx, ndy);
 						if (ndsq < dsq) {
 							dx = ndx;
 							dy = ndy;
 							dsq = ndsq;
 						}
 					}
 				}
 				{
 					int nx = x + -1, ny = y + 1;
 					if (nx >= 0 && ny < h) {
 						int ndx = X[nx][ny] + -1, ndy = Y[nx][ny] + 1;
 						int ndsq = distSq(ndx, ndy);
 						if (ndsq < dsq) {
 							dx = ndx;
 							dy = ndy;
 							dsq = ndsq;
 						}
 					}
 				}
 				{
 					int nx = x + 1, ny = y + 1;
 					if (nx < w && ny < h) {
 						int ndx = X[nx][ny] + 1, ndy = Y[nx][ny] + 1;
 						int ndsq = distSq(ndx, ndy);
 						if (ndsq < dsq) {
 							dx = ndx;
 							dy = ndy;
 							dsq = ndsq;
 						}
 					}
 				}
 
 				put(x, y, dx, dy);
 				// getP(p, x, y);
 				// compareAndUpdate(p, x, y, 1, 0);
 				// compareAndUpdate(p, x, y, 0, 1);
 				// compareAndUpdate(p, x, y, -1, 1);
 				// compareAndUpdate(p, x, y, 1, 1);
 				// putP(x, y, p);
 			}
 
 			for (int x = 0; x < w; x++) {
 				int dx = X[x][y];
 				int dy = Y[x][y];
 				int dsq = distSq(dx, dy);
 				{
 					int nx = x - 1, ny = y;
 					if (nx >= 0) {
 						int ndx = X[nx][ny] - 1, ndy = Y[nx][ny] + 0;
 						int ndsq = distSq(ndx, ndy);
 						if (ndsq < dsq) {
 							dx = ndx;
 							dy = ndy;
 							dsq = ndsq;
 						}
 					}
 				}
 				put(x, y, dx, dy);
 				// getP(p, x, y);
 				// compareAndUpdate(p, x, y, -1, 0);
 				// putP(x, y, p);
 			}
 		}
 	}
 
 	public static void sdfize(BufferedImage bi) throws Exception {
 		long t0 = System.currentTimeMillis();
 		sdfize2(bi);
 		System.out.println("sdfize: " + (System.currentTimeMillis() - t0)
 				+ "ms");
 	}
 
 	public static int clampToByte(int c) throws Exception {
 		if (c < 0)
 			return 0;
 		else if (c > 255)
 			return 255;
 		else
 			return c;
 	}
 
 	public static int getGreen(int rgb) throws Exception {
 		int g = (rgb >>> 8) & 0xff;
 		expect(g == (g & 0xff), null);
 		expect((g << 8) == (rgb & 0xff00), null);
 		expect(g >= 0, null);
 		return g;
 	}
 
 	public static int toGray(int c) throws Exception {
 		expect(c >= 0, null);
 		int b = c & 0xff;
 		int r = b << 16 | b << 8 | b;
 		expect((r & 0xff) == b, null);
 		return r;
 	}
 
 	public static void sdfize2(BufferedImage bi) throws Exception {
 		SDF g1 = new SDF(bi);
 		SDF g2 = new SDF(bi);
 
 		for (int y = 0; y < bi.getHeight(); y++) {
 			for (int x = 0; x < bi.getWidth(); x++) {
 				int v = getGreen(bi.getRGB(x, y));
 				if (v < 128) {
 					g1.putP(x, y, inside);
 					g2.putP(x, y, empty);
 				} else {
 					g2.putP(x, y, inside);
 					g1.putP(x, y, empty);
 				}
 			}
 		}
 
 		g1.generateSDF();
 		g2.generateSDF();
 
 		// int min = Short.MAX_VALUE, max = Short.MIN_VALUE, n = 0;
 		for (int y = 0; y < bi.getHeight(); y++) {
 			for (int x = 0; x < bi.getWidth(); x++) {
 				int dist1 = (int) Math.sqrt(distSq(g1.X[x][y], g1.Y[x][y]));
 				int dist2 = (int) Math.sqrt(distSq(g2.X[x][y], g2.Y[x][y]));
 				int dist = dist1 - dist2;
 
 				int c = clampToByte(128 + dist);
 
 				// if (c > max) {
 				// max = c;
 				// }
 				// if (c < min) {
 				// min = c;
 				// }
 				// if (c > 128) {
 				// n++;
 				// }
 
 				int sc = toGray(c);
 				bi.setRGB(x, y, sc);
 				// expect(sc == bi.getRGB(x, y),
 				// "put 0x" + Integer.toHexString(sc) + ", got 0x"
 				// + Integer.toHexString(bi.getRGB(x, y)));
 			}
 		}
 		// System.out.printf("[%d, %d]; count > 128: %d\n", min, max, n);
 	}
 
 	static void expect(boolean b, String m) throws Exception{
 		if (!b)
 			throw new Exception("expectation failed: " + m);
 	}
 }
