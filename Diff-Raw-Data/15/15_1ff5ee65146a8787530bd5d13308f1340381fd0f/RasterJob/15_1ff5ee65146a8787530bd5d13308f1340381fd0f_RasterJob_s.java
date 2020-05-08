 /**
  *
  */
 package ca.eandb.jmist.packages.job;
 
 import java.awt.Rectangle;
 import java.awt.image.DataBuffer;
 import java.awt.image.DataBufferDouble;
 import java.awt.image.PixelInterleavedSampleModel;
 import java.awt.image.Raster;
 import java.awt.image.SampleModel;
 import java.awt.image.WritableRaster;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Iterator;
 
 import javax.imageio.IIOImage;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageWriter;
 import javax.imageio.stream.MemoryCacheImageOutputStream;
 
 import ca.eandb.jmist.framework.PixelShader;
 import ca.eandb.jmist.toolkit.Box2;
 
 import ca.eandb.jdcp.job.AbstractParallelizableJob;
 import ca.eandb.jdcp.job.TaskWorker;
 import ca.eandb.util.progress.ProgressMonitor;
 
 /**
  * A <code>ParallelizableJob</code> that renders a <code>Raster</code> image.
  * @author bkimmel
  */
 public final class RasterJob extends AbstractParallelizableJob implements
 		Serializable {
 
 	private String formatName;
 
 	/**
 	 * Creates a new <code>RasterJob</code>.  This job will
 	 * divide the image into <code>rows * cols</code> tasks to render roughly
 	 * equally sized chunks of the image.
 	 * @param shader The <code>PixelShader</code> to use to compute the values
 	 * 		of individual <code>Pixel</code>s.
 	 * @param raster The <code>Raster</code> to write the results to.
 	 * @param cols The number of columns to divide the image into.
 	 * @param rows The number of rows to divide the image into.
 	 */
 	public RasterJob(PixelShader shader, WritableRaster raster, String formatName, int cols, int rows) {
 		this.worker = new RasterTaskWorker(shader, raster.getWidth(), raster.getHeight());
 		this.raster = raster;
 		this.cols = cols;
 		this.rows = rows;
 		this.formatName = formatName;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.ParallelizableJob#getNextTask()
 	 */
 	public Object getNextTask() {
 
 		if (this.nextRow < this.rows) {
 
 			/* Get the next cell. */
 			Rectangle cell = this.getCell(this.nextCol++, this.nextRow);
 
 			/* If we're done this row, move on to the next row. */
 			if (this.nextCol >= this.cols) {
 				this.nextCol = 0;
 				this.nextRow++;
 			}
 
 			return cell;
 
 		} else { /* this.nextRow >= this.rows */
 
 			/* no remaining tasks. */
 			return null;
 
 		}
 
 	}
 
 	/**
 	 * Gets the bounds of the cell at the specified row and column.
 	 * @param col The column index.
 	 * @param row The row index.
 	 * @return The cell bounds.
 	 */
 	private Rectangle getCell(int col, int row) {
 
 		/* Figure out how big the cells should be:
 		 *    - Make them as large as possible without exceeding the size
 		 *      of the image.
 		 *    - Allocate the remainder of the pixels (if any) to the first n
 		 *      cells, where n is the remainder.
 		 */
 
 		/* Ensure that the cell is valid whose dimensions are being
 		 * requested.
 		 */
 		assert(0 <= col && col < this.cols);
 		assert(0 <= row && row < this.rows);
 
 		int w = this.raster.getWidth();
 		int h = this.raster.getHeight();
 
 		/* First figure out the base dimensions of the cells and the number
 		 * of remaining pixels in each dimension that need to be allocated.
 		 */
 		int wdivc = w / this.cols;
 		int hdivr = h / this.rows;
 		int wmodc = w % this.cols;
 		int hmodr = h % this.rows;
 
 		/* Calculate the upper-left corner of the cell, first assuming that
 		 * there are no "remainder" pixels.. we will then adjust for this.
 		 */
 		int xmin = col * wdivc;
 		int ymin = row * hdivr;
 
 		/* Adjust to account for the cells above and to the left that
 		 * received extra pixels.
 		 */
 		xmin += Math.min(col, wmodc);
 		ymin += Math.min(row, hmodr);
 
 		/* Now compute the lower right pixel, again first assuming that this
 		 * cell is not to receive extra pixels.
 		 */
 		int xmax = xmin + wdivc - 1;
 		int ymax = ymin + hdivr - 1;
 
 		/* Add the extra pixels, if required. */
 		if (col < wmodc) xmax++;
 		if (row < hmodr) ymax++;
 
 		/* Make sure the computed cell extents fall within the image. */
 		assert(0 <= xmin && xmin <= xmax && xmax < w);
 		assert(0 <= ymin && ymin <= ymax && ymax < h);
 
 		return new Rectangle(xmin, ymin, xmax - xmin + 1, ymax - ymin + 1);
 
 	}
 
 	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.ParallelizableJob#submitTaskResults(java.lang.Object, java.lang.Object, ca.eandb.jmist.framework.reporting.ProgressMonitor)
 	 */
 	public void submitTaskResults(Object task, Object results, ProgressMonitor monitor) {
 
 		Rectangle	cell	= (Rectangle) task;
 		double[]	pixels	= (double[]) results;
 
 		/* Write the submitted results to the raster. */
 		this.raster.setPixels(cell.x, cell.y, cell.width, cell.height, pixels);
 
 		/* Update the progress monitor. */
 		monitor.notifyProgress(++this.tasksComplete, this.rows * this.cols);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.ParallelizableJob#isComplete()
 	 */
 	public boolean isComplete() {
 		return this.tasksComplete >= (this.rows * this.cols);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.ParallelizableJob#finish()
 	 */
 	public void finish() throws IOException {
 
 		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(this.formatName);
 		ImageWriter writer = writers.next();
 		String[] suffix = writer.getOriginatingProvider().getFileSuffixes();
 
 		assert(suffix != null && suffix.length > 0);
 
 		FileOutputStream fs = createFileOutputStream("raster." + suffix[0]);
 		MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(fs);
 		writer.setOutput(ios);
 
 		writer.write(null, new IIOImage(this.raster, null, null), null);
 		ios.flush();
 		ios.close();
 
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.ParallelizableJob#worker()
 	 */
 	public TaskWorker worker() {
 		return this.worker;
 	}
 
 	/**
 	 * A <code>TaskWorker</code> that renders a rectangular subset of a
 	 * <code>Raster</code> image.
 	 * @author bkimmel
 	 */
 	private static final class RasterTaskWorker implements TaskWorker {
 
 		/**
 		 * Creates a new <code>RasterTaskWorker</code>.
 		 * @param shader The <code>PixelShader</code> to use to compute the
 		 * 		values of individual <code>Pixel</code>s.
 		 * @param width The width of the entire <code>Raster</code> image.
 		 * @param height The height of the entire <code>Raster</code> image.
 		 */
 		public RasterTaskWorker(PixelShader shader, int width, int height) {
 			this.shader		= shader;
 			this.width		= width;
 			this.height		= height;
 		}
 
 		/* (non-Javadoc)
		 * @see ca.eandb.jmist.framework.TaskWorker#performTask(java.lang.Object, ca.eandb.jmist.framework.reporting.ProgressMonitor)
 		 */
 		public Object performTask(Object task, ProgressMonitor monitor) {
 
 			Rectangle		cell				= (Rectangle) task;
 			int				numPixels			= cell.width * cell.height;
 			double[]		pixels				= null;
 			double[]		pixel				= null;
 			Box2			bounds;
 			double			x0, y0, x1, y1;
 			double			w					= this.width;
 			double			h					= this.height;
 			WritableRaster	image				= null;
 
 			for (int n = 0, y = cell.y; y < cell.y + cell.height; y++) {
 
 				if (!monitor.notifyProgress(n, numPixels))
 					return null;
 
 				y0			= (double) y / h;
 				y1			= (double) (y + 1) / h;
 
 				for (int x = cell.x; x < cell.x + cell.width; x++, n++) {
 
 					x0		= (double) x / w;
 					x1		= (double) (x + 1) / w;
 
 					bounds	= new Box2(x0, y0, x1, y1);
 
 					pixel = this.shader.shadePixel(bounds, pixel);
 
 					if (pixels == null) {
 						pixels = new double[numPixels * pixel.length];
 						image = createCellWritableRaster(cell, pixel.length, pixels);
 					}
 
 					image.setPixel(x, y, pixel);
 
 				}
 
 			}
 
 			monitor.notifyProgress(pixels.length, pixels.length);
 			monitor.notifyComplete();
 
 			return pixels;
 
 		}
 
 		/**
 		 * Creates a <code>WritableRaster</code> for writing to a cell.
 		 * @param cell The <code>Rectangle</code> describing the bounds of the
 		 * 		cell for which to create the <code>WritableRaster</code>.
 		 * @param bands The number of bands in the <code>WritableRaster</code>.
 		 * @param buffer The array of <code>double</code>s to hold the pixel
 		 * 		data.
 		 * @return The new <code>WritableRaster</code>.
 		 */
 		private static WritableRaster createCellWritableRaster(Rectangle cell, int bands, double[] buffer) {
 
 			int[] offsets = new int[bands];
 			for (int i = 0; i < bands; i++) {
 				offsets[i] = i;
 			}
 
 			SampleModel		sm		= new PixelInterleavedSampleModel(DataBuffer.TYPE_DOUBLE, cell.width, cell.height, bands, cell.width * bands, offsets);
 			DataBuffer		db		= new DataBufferDouble(buffer, buffer.length);
 
 			return Raster.createWritableRaster(sm, db, cell.getLocation());
 
 		}
 
 		/**
 		 * The <code>PixelShader</code> to use to compute the values of
 		 * individual <code>Pixel</code>s.
 		 */
 		private final PixelShader shader;
 
 		/** The width of the entire <code>Raster</code> image. */
 		private final int width;
 
 		/** The height of the entire <code>Raster</code> image. */
 		private final int height;
 
 		/**
 		 * Serialization version ID.
 		 */
 		private static final long serialVersionUID = 8318742231359439076L;
 
 	}
 
 	/**
 	 * The <code>TaskWorker</code> to use to render rectangular subsets of
 	 * the <code>Raster</code> image.
 	 */
 	private final TaskWorker worker;
 
 	/** The <code>WritableRaster</code> image to render to. */
 	private final WritableRaster raster;
 
 	/** The number of columns to divide the <code>Raster</code> image into. */
 	private final int cols;
 
 	/** The number of rows to divide the <code>Raster</code> image into. */
 	private final int rows;
 
 	/** The column index of the next task to return. */
 	private int nextCol = 0;
 
 	/** The row index of the next task to return. */
 	private int nextRow = 0;
 
 	/** The number of tasks that have been completed. */
 	private int tasksComplete = 0;
 
 	/**
 	 * Serialization version ID.
 	 */
 	private static final long serialVersionUID = 9173731839475893020L;
 
 }
