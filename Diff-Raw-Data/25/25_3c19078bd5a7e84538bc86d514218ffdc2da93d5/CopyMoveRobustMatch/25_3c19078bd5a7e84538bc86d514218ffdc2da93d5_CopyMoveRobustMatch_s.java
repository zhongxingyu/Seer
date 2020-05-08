 package model.algorithms;
 
 import java.awt.image.BufferedImage;
 import java.util.Collections;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import model.Event;
 import model.Result;
 import model.ShiftVector;
 import model.Event.EventType;
 import model.algorithms.utils.Block;
 import model.algorithms.utils.DCTWorkerpool;
 
 /**
  * @author Huber Bastian
  * 
  *         This class implements the robust match algorithm for detecting
  *         copy-move changes of an image.
  * 
  */
 public class CopyMoveRobustMatch extends ICopyMoveDetection implements Observer {
 	private DCTWorkerpool workerpool;
 
 	@Override
 	public void abort() {
 		if (workerpool != null) {
 			workerpool.abort();
 		}
 		setChanged();
 		notifyObservers(new Event(EventType.ABORT,
 				"The detection was aborted successfully!"));
 	}
 
 	/**
 	 * Detects the copy-move-parts of the image with the given quality and
 	 * threshold. The method notifies different events that can be observed. At
 	 * the end an event with a {@link List} of {@link ShiftVector}s are
 	 * notified, so one knows what parts are most likely copy-moved.
 	 */
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
 		int height = input.getHeight();
 		int width = input.getWidth();
 
 		/*
 		 * The image can be processed. First calc grayscale image...
 		 */
 		int[] grayscale = new int[width * height];
 		for (int y = 0; y < height; y++) {
 			for (int x = 0; x < width; x++) {
 				int pixel = input.getRGB(x, y);
 				grayscale[y * width + x] = (int) (((pixel >> 16) & 0xff)
 						* 0.299 + ((pixel >> 8) & 0xff) * 0.587 + ((pixel) & 0xff) * 0.114) - 128;
 			}
 		}
 
 		setChanged();
 		notifyObservers(new Event(Event.EventType.STATUS,
 				"Luminance matrix of image calculated in " + takeTime() + "ms"));
 
 		/*
 		 * Calculate the dcts of each block...
 		 */
 		workerpool = new DCTWorkerpool(grayscale, width, height, quality,
 				threads, this);
 		workerpool.start();
 
 		if (workerpool.getAborted()) {
 			return;
 		}
 
 		List<Block> dcts = workerpool.getResult();
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
 												.getPos_y() + b2.getPos_y(),
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
 
 	private long oldtime = 0;
 
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
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		if (arg1 != null && arg1 instanceof Float) {
 			setChanged();
 			notifyObservers(new Event(EventType.PROGRESS, new Result(
 					(Float) arg1)));
 		}
 	}
 }
