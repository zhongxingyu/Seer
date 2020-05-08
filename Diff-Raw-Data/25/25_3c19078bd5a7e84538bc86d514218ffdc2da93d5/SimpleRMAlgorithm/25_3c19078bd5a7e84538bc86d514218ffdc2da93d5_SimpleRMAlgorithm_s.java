 package model.algorithms;
 
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import model.Event;
 import model.Result;
 import model.ShiftVector;
 import model.Event.EventType;
 import model.algorithms.utils.Block;
 import model.algorithms.utils.DCTWorkerpool;
 
 public class SimpleRMAlgorithm extends ICopyMoveDetection implements Observer {
 
 	private boolean abort = false;
 	private long oldtime = 0;
 	List<Block> dcts = new ArrayList<Block>();
 	private int height, width;
 
 	public float QUANT[][] = new float[][] {
 			{ 32.0f, 27.5f, 25.0f, 40.0f, 60.0f, 100.0f, 127.5f, 152.5f,
 					152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f,
 					152.5f },
 			{ 30.0f, 30.0f, 35.0f, 47.5f, 65.0f, 145.0f, 150.0f, 137.5f,
 					152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f,
 					152.5f },
 			{ 35.0f, 32.5f, 40.0f, 60.0f, 100.0f, 142.5f, 172.5f, 140.0f,
 					152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f,
 					152.5f },
 			{ 35.0f, 42.5f, 55.0f, 72.5f, 127.5f, 217.5f, 200.0f, 155.0f,
 					152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f,
 					152.5f },
 			{ 45.0f, 55.0f, 92.5f, 140.0f, 170.0f, 272.5f, 257.5f, 192.5f,
 					152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f,
 					152.5f },
 			{ 60.0f, 87.5f, 137.5f, 160.0f, 202.5f, 260.0f, 282.5f, 230.0f,
 					152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f,
 					152.5f },
 			{ 122.5f, 160.0f, 195.0f, 217.5f, 257.5f, 302.5f, 300.0f, 252.5f,
 					152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f,
 					152.5f },
 			{ 180.0f, 230.0f, 237.5f, 245.0f, 280.0f, 250.0f, 257.5f, 247.5f,
 					152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f, 152.5f,
 					152.5f },
 			{ 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f,
 					247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f,
 					247.5f },
 			{ 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f,
 					247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f,
 					247.5f },
 			{ 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f,
 					247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f,
 					247.5f },
 			{ 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f,
 					247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f,
 					247.5f },
 			{ 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f,
 					247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f,
 					247.5f },
 			{ 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f,
 					247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f,
 					247.5f },
 			{ 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f,
 					247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f,
 					247.5f },
 			{ 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f,
 					247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f, 247.5f,
 					247.5f } };
 
 	@Override
 	public void abort() {
 		abort = true;
 		setChanged();
 		notifyObservers(new Event(EventType.ABORT,
 				"The detection was aborted successfully!"));
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		if (arg1 != null && arg1 instanceof Block) {
 			synchronized (dcts) {
 				dcts.add((Block) arg1);
 			}
 			if (dcts.size() % (height - 15) == 0) {
 				setChanged();
 				notifyObservers(new Event(EventType.PROGRESS, new Result(
 						(float) dcts.size()
 								/ (float) ((width - 15) * (height - 15)))));
 			}
 		}
 	}
 
 	@Override
 	public void detect(BufferedImage input, float quality, int threshold,
 			int minLength, int threads) {
 
 		if (!checkImage(input)) {
 			setChanged();
 			notifyObservers(new Event(Event.EventType.ERROR,
 					"The image could not be processed!"));
 			return;
 		}
 
 		long start = System.currentTimeMillis();
 		takeTime();
 		height = input.getHeight();
 		width = input.getWidth();
 
 		/*
 		 * The image can be processed. First calc grayscale image...
 		 */
 		final int[][] grayscale = new int[height][width];
 		for (int y = 0; y < height; y++) {
 			for (int x = 0; x < width; x++) {
 				int pixel = input.getRGB(x, y);
 				grayscale[y][x] = (int) (((pixel >> 16) & 0xff) * 0.299
 						+ ((pixel >> 8) & 0xff) * 0.587 + ((pixel) & 0xff) * 0.114) - 128;
 			}
 		}
 
 		setChanged();
 		notifyObservers(new Event(Event.EventType.STATUS,
 				"Luminance matrix of image calculated in " + takeTime() + "ms"));
 
 		final float[][][][] constants = new float[16][16][16][16];
 
 		for (int u = 0; u < 16; u++) {
 			float alphau = (float) (u == 0 ? 1.0f / Math.sqrt(16) : 1.0f / Math
 					.sqrt(32));
 			for (int v = 0; v < 16; v++) {
 				float alphav = (float) (v == 0 ? 1.0f / Math.sqrt(16)
 						: 1.0f / Math.sqrt(32));
 				for (int i = 0; i < 16; i++) {
 					for (int j = 0; j < 16; j++) {
 						constants[u][v][i][j] = (float) (alphau * alphav
 								* Math.cos((Math.PI * (2 * i + 1) * u) / 32.0f) * Math
 								.cos((Math.PI * (2 * j + 1) * v) / 32.0f));
 					}
 				}
 				QUANT[u][v] *= quality;
 			}
 		}
 
 		/*
 		 * Calculate the dcts of each block...
 		 */
 
 		Thread[] tA = new Thread[threads];
 
 		for (int t = 0; t < threads; t++) {
 			Worker w = new Worker(t, threads, width, height, grayscale,
 					constants);
 			w.addObserver(this);
 			Thread th = new Thread(w);
 			tA[t] = th;
 			th.start();
 		}
 
 		for (int t = 0; t < threads; t++) {
 			try {
 				tA[t].join();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		setChanged();
 		notifyObservers(new Event(Event.EventType.STATUS,
 				"DCT of each block was calculated in " + takeTime() + "ms"));
 
 		/*
 		 * Sort the dcts lexicographically...
 		 */
 		Collections.sort(dcts);
 
 		setChanged();
 		notifyObservers(new Event(Event.EventType.STATUS,
 				"Lexicographically sorted all DCTs in " + takeTime() + "ms"));
 
 		/*
 		 * Collect vectors... The Shift Vector is an array double of the
 		 * original image size. The position in the array identifies the vector
 		 * itself, the value at a given position represents the count of the
 		 * shift vector.
 		 */
		int shiftVectors[] = new int[width * height * 2];
 		for (int i = 0; i < dcts.size() - 1; i++) {
 			Block b1 = dcts.get(i);
 			Block b2 = dcts.get(i + 1);
 
 			if (b1.compareTo(b2) == 0) {
 				int sx = b1.getPos_x() - b2.getPos_x();
 				int sy = b1.getPos_y() - b2.getPos_y();
 
				if (getVLenght(sx, sy) > minLength) {
 
 					if (sx < 0) {
 						sx = -sx;
 						sy = -sy;
 					}
 
 					/*
 					 * This has to be done because sy may be negative...
 					 */
 					sy += height;
 
					shiftVectors[sy * width + sx]++;
 				}
 			}
 		}
 
 		setChanged();
 		notifyObservers(new Event(Event.EventType.STATUS,
 				"Calculated the shift vectors in " + takeTime() + "ms"));
 
 		/*
 		 * Save time that will be notified along with the result of the
 		 * shiftvectors
 		 */
 		long end = System.currentTimeMillis();
 
 		Event event = new Event(EventType.COPY_MOVE_DETECTION_FINISHED,
 				new Result(end - start));
 		/*
 		 * Mark the detected copies...
 		 */
 		for (int i = 0; i < dcts.size() - 1; i++) {
 			Block b1 = dcts.get(i);
 			Block b2 = dcts.get(i + 1);
 
 			if (b1.compareTo(b2) == 0) {
 				int sx = b1.getPos_x() - b2.getPos_x();
 				int sy = b1.getPos_y() - b2.getPos_y();
 
				if (getVLenght(sx, sy) > minLength) {
 					if (sx < 0) {
 						sx = -sx;
 						sy = -sy;
 					}
 
 					/*
 					 * This has to be done because sy may be negative...
 					 */
 					sy += height;
 
					if (shiftVectors[sy * width + sx] > threshold) {
 						event.getResult().addShiftVector(
 								new ShiftVector(b1.getPos_x(), b1.getPos_y(),
 										-b1.getPos_x() + b2.getPos_x(), -b1
 												.getPos_y()
 												+ b2.getPos_y(),
 										DCTWorkerpool.BLOCK_SIZE));
 					}
 				}
 			}
 		}
 
 		setChanged();
 		notifyObservers(event);
 	}
 
 	/**
 	 * Checks if the copy move detection can be performed on the given image.
 	 * 
 	 * @param image
 	 * @return
 	 */
 	private boolean checkImage(BufferedImage image) {
 		if (image.getWidth() < 16 || image.getHeight() < 16) {
 			return false;
 		}
 		return true;
 	}
 
 	private double getVLenght(int x, int y) {
 		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
 	}
 
 	private long takeTime() {
 		if (oldtime != 0) {
 			long ret = System.currentTimeMillis() - oldtime;
 			oldtime = System.currentTimeMillis();
 			return ret;
 		} else {
 			oldtime = System.currentTimeMillis();
 			return -1;
 		}
 	}
 
 	private class Worker extends Observable implements Runnable {
 		private final int num, threads, width, height, grayscale[][];
 		private final float[][][][] constants;
 
 		public Worker(final int num, final int threads, final int width,
 				final int height, final int grayscale[][],
 				final float[][][][] constants) {
 			this.num = num;
 			this.threads = threads;
 			this.width = width;
 			this.height = height;
 			this.grayscale = grayscale;
 			this.constants = constants;
 
 		}
 
 		@Override
 		public void run() {
 
 			for (int yy = num; yy < width - 15 && !abort; yy += threads) {
 
 				for (int xx = 0; xx < height - 15; xx++) {
 					float[] dct = new float[16 * 16];
 
 					for (int u = 0; u < 16; u++) {
 						for (int v = 0; v < 16; v++) {
 							float f = 0f;
 							for (int i = 0; i < 16; i++) {
 								for (int j = 0; j < 16; j++) {
 									f += grayscale[(xx + i)][yy + j]
 											* constants[u][v][i][j];
 								}
 							}
 
 							dct[u * 16 + v] = (float) Math
 									.rint(f / QUANT[u][v]);
 						}
 					}
 
 					setChanged();
 					notifyObservers(new Block(dct, yy, xx));
 				}
 			}
 
 		}
 	}
 
 }
