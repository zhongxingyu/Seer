 package image;
 
 import image.threads.ImageStorage;
 
 import java.awt.image.Raster;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
import java.sql.SQLException;
 import java.util.ArrayList;
 
 import main.Lunar;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.ArrayUtils;
 
 import com.sun.media.jai.codec.ImageCodec;
 import com.sun.media.jai.codec.ImageDecoder;
 
 import database.threads.EnterBaseDataThread;
 import database.threads.ReadRowFilesThread;
 
 public class ImageProcess {
 
 	private PaletteProcess palette;
 
 	private Integer pixelHeight;
 	private Integer pixelWidth;
 
 	private Float pixelLat;
 	private Float pixelLon;
 
 	private Raster image;
 
 	public ImageProcess(PaletteProcess palette) {
 		this.palette = palette;
 	}
 
 	public void generateData(String filename) throws Exception {
		//createDirectories();
		//loadImage(filename);
		//convertImage();
		loadConvertImage();
		//convertRgb();
 
 		Thread readFiles1 = new Thread(new ReadRowFilesThread());
 		Thread readFiles2 = new Thread(new ReadRowFilesThread());
 		Thread readFiles3 = new Thread(new ReadRowFilesThread());
 		Thread readFiles4 = new Thread(new ReadRowFilesThread());
 
 		Thread baseData1 = new Thread(new EnterBaseDataThread());
 		Thread baseData2 = new Thread(new EnterBaseDataThread());
 		Thread baseData3 = new Thread(new EnterBaseDataThread());
 		Thread baseData4 = new Thread(new EnterBaseDataThread());
 
 		readFiles1.start();
 		readFiles2.start();
 		readFiles3.start();
 		readFiles4.start();
 
 		baseData1.start();
 		baseData2.start();
 		baseData3.start();
 		baseData4.start();
 
 		while (readFiles1.isAlive() && readFiles2.isAlive()
 				&& readFiles3.isAlive() && readFiles4.isAlive()) {
 			Thread.sleep(5000);
 		}
 
 		ImageStorage.setEndStatement(true);
 
 		while (baseData1.isAlive() && baseData2.isAlive()
 				&& baseData3.isAlive() && baseData4.isAlive()) {
 			Thread.sleep(5000);
 		}
 		System.out.println("Database Filled");
 	}
 
 	/**
 	 * 
 	 */
 	private void createDirectories() {
 		// TODO: Do some checks here
 		File toCreate = new File(Lunar.OUT_DIR + Lunar.PIXEL_DIR
 				+ Lunar.PROCESSED_DIR);
 		toCreate.mkdirs();
 		toCreate = null;
 		toCreate = new File(Lunar.OUT_DIR + Lunar.ROW_DIR + Lunar.PROCESSED_DIR);
 		toCreate.mkdirs();
 		toCreate = null;
 	}
 
 	private void loadImage(String filename) throws IOException {
 		File file = new File(filename);
 
 		ImageDecoder dec = ImageCodec.createImageDecoder("tiff", file, null);
 
 		image = dec.decodeAsRaster();
 		pixelHeight = image.getHeight();
 		pixelWidth = image.getWidth();
 		System.out.println("Image loaded");
 
 	}
 
 	/**
 	 * 
 	 * @throws IOException
 	 */
 	private void convertImage() throws IOException {
 		// TODO: Thread this
 		for (int y = 0; y < pixelHeight; y++) {
 			ArrayList<Integer[]> row = new ArrayList<Integer[]>();
 			for (int x = 0; x < pixelWidth; x++) {
 
 				int[] rgb = null;
 
 				rgb = image.getPixel(x, y, rgb);
 
 				row.add(ArrayUtils.toObject(rgb));
 			}
 			// Write row to disc
 			File file = new File(Lunar.OUT_DIR + Lunar.PIXEL_DIR + "\\pixel"
 					+ y + ".txt");
 			BufferedWriter out = new BufferedWriter(new FileWriter(file));
 			for (Integer[] pixel : row) {
 				out.write(String.format("%d,%d,%d\n", pixel[0], pixel[1],
 						pixel[2]));
 			}
 			out.close();
 			ImageStorage.pixelFilesAdd(file);
 		}
 		System.out.println("Picture to text complete");
 	}
 
 	private void loadConvertImage() {
 		File filePlace = new File(Lunar.OUT_DIR + Lunar.ROW_DIR + "//");
 		String[] files = filePlace.list(new Filter(".txt"));
 		for (String file : files) {
 			ImageStorage.rowFilesAdd(new File(Lunar.OUT_DIR + Lunar.ROW_DIR
 					+ "//" + file));
 		}
 		files = null;
 		System.out.println("Loaded files back in");
 	}
 
 	/**
 	 * 
 	 * @throws Exception
 	 */
 	private void convertRgb() throws Exception {
 		// TODO: Thread this
 		int latPos = 0;
 		int lonPos = 0;
 		pixelLat = 180f / pixelHeight.floatValue();
 		pixelLon = 360f / pixelWidth.floatValue();
 
 		File processedFileDir = new File(Lunar.OUT_DIR + Lunar.PIXEL_DIR
 				+ Lunar.PROCESSED_DIR);
		
 		for (int i = 0; i < ImageStorage.pixelFilesSize(); i++) {
 			File pixelFile = ImageStorage.pixelFilesGet(i);
 			// +1 just to make it the same numbering as pixel files
 			File heightFile = new File(Lunar.OUT_DIR + Lunar.ROW_DIR + "\\row"
 					+ (latPos + 1) + ".txt");
 			BufferedReader in = new BufferedReader(new FileReader(pixelFile));
 			BufferedWriter out = new BufferedWriter(new FileWriter(heightFile));
 			String line = null;
 			line = in.readLine();
 			while (line != null) {
 				String[] rgbString = line.split(",");
				Float lat = Lunar.LAT_MAX - (pixelLat * latPos);
 				Float lon = Lunar.LON_MIN + (pixelLon * lonPos);
 				Float height = palette.convertRgb(rgbString);
 				out.write(String.format("%s,%s,%s\n", lat.toString(),
 						lon.toString(), height.toString()));
 				line = in.readLine();
 				lonPos++;
				FileUtils.moveFileToDirectory(pixelFile, processedFileDir, false);
 			}
 			in.close();
 			out.close();
 			ImageStorage.rowFilesAdd(heightFile);
 			lonPos = 0;
 			latPos++;
 		}
 		System.out.println("Pixels convert to raw data");
 	}
 
 	private class Filter implements FilenameFilter {
 		private String ext;
 
 		public Filter(String ext) {
 			this.ext = ext;
 		}
 
 		@Override
 		public boolean accept(File dir, String name) {
 			return name.endsWith(ext);
 		}
 	}
 
 }
