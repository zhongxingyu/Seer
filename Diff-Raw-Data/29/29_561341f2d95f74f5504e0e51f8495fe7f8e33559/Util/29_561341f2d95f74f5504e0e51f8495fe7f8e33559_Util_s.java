 package com.todesking.castleatack;
 
 import java.io.PrintStream;
 import java.util.Iterator;
 
 import jp.ac.washi.quinte.api.GameInfo;
 import jp.ac.washi.quinte.api.Point;
 import jp.ac.washi.quinte.api.TileInfo;
 
 import com.google.common.collect.UnmodifiableIterator;
 
 public class Util {
 	public static int manhattanDistance(Point p1, Point p2) {
 		return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
 	}
 
 	/**
 	 * centerを中心にしたらせん状の座標を返す。centerは含まず。
 	 * 
 	 * @param map
 	 * @param center
 	 * @return
 	 */
	public static Iterable<Point> spiralPoints(final int size,
 			final Point center) {
		if (size % 2 == 0)
			throw new IllegalArgumentException();
 		return new Iterable<Point>() {
 			@Override
 			public Iterator<Point> iterator() {
 				return new UnmodifiableIterator<Point>() {
 					int index = 1;
 					int x = 1;
 					int y = 0;
 
 					@Override
 					public boolean hasNext() {
						return index <= size / 2 + 1;
 					}
 
 					@Override
 					public Point next() {
 						// エレガントじゃないなー
						final Point p = new Point(x, y);
 						increment();
 						return p;
 					}
 
 					private void increment() {
 						if (y < index && x == index) {
 							y++;
 						} else if (y == index && -index < x) {
 							x--;
 						} else if (x == -index && -index < y) {
 							y--;
 						} else if (y == -index && x < index) {
 							x++;
 						}
 						if (y == 0 && x == index) {
 							index++;
 							x = index;
 						}
 					}
 				};
 			}
 		};
 	}
 
 	/**
 	 * min,max is inclusive(like sql)
 	 * 
 	 * @param val
 	 * @param min
 	 * @param max
 	 * @return
 	 */
 	public static boolean between(int val, int min, int max) {
 		return min <= val && val <= max;
 	}
 
 	public static void printMapInfo(GameInfo info, final PrintStream out)
 			throws AssertionError {
 		for (int y = 0; y < info.getMap().getSize(); y++) {
 			for (int x = 0; x < info.getMap().getSize(); x++) {
 				final TileInfo tile = info.getMap().getTile(Point.create(x, y));
 				final char c;
 				switch (tile.getType()) {
 				case BIG_GATE:
 					c = 'G';
 					break;
 				case SMALL_GATE:
 					c = 'g';
 					break;
 				case ROAD:
 					if (tile.getOwner() == info.getLeftCountry())
 						c = '1';
 					else if (tile.getOwner() == info.getOppositeCountry())
 						c = '2';
 					else if (tile.getOwner() == info.getRightCountry())
 						c = '3';
 					else if (tile.getOwner() == info.getMyCountry())
 						c = '0';
 					else if (tile.getOwner() == null)
 						c = '?';
 					else
 						throw new AssertionError();
 					break;
 				case WALL:
 					c = '#';
 					break;
 				default:
 					throw new AssertionError();
 				}
 				out.print(c);
 			}
 			out.println();
 		}
 	}
 
 }
