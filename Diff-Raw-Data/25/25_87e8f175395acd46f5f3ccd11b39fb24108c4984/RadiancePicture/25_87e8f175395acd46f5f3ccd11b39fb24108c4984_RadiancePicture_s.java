 /**
  * 
  */
 package ca.eandb.jmist.framework.loader.radiance;
 
 import java.io.DataInputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.Serializable;
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 
 import ca.eandb.jmist.framework.color.CIEXYZ;
 import ca.eandb.jmist.framework.color.RGB;
 import ca.eandb.util.UnimplementedException;
 
 /**
  * Represents an image stored in RADIANCE (.pic, .hdr) format.
  * 
  * @author Brad Kimmel
  */
 public class RadiancePicture implements Serializable {
 	
 	/** Serialization version ID. */
 	private static final long serialVersionUID = -5359236700826115108L;
 
 	/** The default software string to use when writing an image to a file. */
 	private static final String DEFAULT_SOFTWARE = "JMist 0.1 (ca.eandb.jmist)";
 	
 	/** The string that should be in the first line of a RADIANCE file. */
 	private static final String HEADER_IDENTIFIER = "#?RADIANCE";
 
 	/** The raw image data. */
 	private final byte[] data;
 	
 	/** The width of the image. */
 	private final int sizeX;
 	
 	/** The height of the image. */
 	private final int sizeY;
 	
 	/** The format that the image is stored in. */
 	private Format format;
 	
 	/** The name of the software that created this image. */
 	private String software = DEFAULT_SOFTWARE;
 	
 	/** The exposure level for the data in this image. */
 	private double exposure = 1.0;
 
 	/** The pixel aspect ratio. */
 	private double pixelAspect = 1.0;
 	
 	/** The color correction to apply to the raw data in this image. */
 	private double[] colorcorr = { 1.0, 1.0, 1.0 };
 	
 	/** An <code>IntBuffer</code> to read the raw data as 32-bit integers. */
 	private transient IntBuffer buffer;
 
 	/**
 	 * Represents a pixel format for a RADIANCE image file.
 	 * @author Brad Kimmel
 	 */
 	public static enum Format {
 		
 		/** Represents an image stored in RGBE format. */
 		RGBE("32-bit_rle_rgbe", RGBEPixelFormat.INSTANCE),
 		
 		/** Represents an image stored in XYZE format. */
 		XYZE("32-bit_rle_xyze", XYZEPixelFormat.INSTANCE);
 		
 		/** The string identifying this format in the file. */
 		private final String name;
 		
 		/** The interpreter for the pixel format. */
 		private final PixelFormat interp;
 		
 		/**
 		 * Creates a new <code>Format</code>.
 		 * @param name The string identifying this format in the file.
 		 * @param interp The <code>PixelFormat</code> to use to convert between
 		 * 		RADIANCE internal form and RGB/CIEXYZ values.
 		 */
 		Format(String name, PixelFormat interp) {
 			this.name = name;
 			this.interp = interp;
 		}
 		
 		/**
 		 * Gets the string identifying this format in the file.
 		 * @return The string identifying this format in the file.
 		 */
 		public String getName() {
 			return name;
 		}
 		
 	};
 	
 	/**
 	 * Creates a new <code>RadiancePicture</code>.
 	 * @param sizeX The width of the image, in pixels.
 	 * @param sizeY The height of the image, in pixels.
 	 * @param data The raw image data.
 	 * @param format The <code>Format</code> that the image data is stored in.
 	 */
 	private RadiancePicture(int sizeX, int sizeY, byte[] data, Format format) {
 		if (data.length != (sizeX * sizeY * 4)) {
 			throw new IllegalArgumentException("incorrect array size");
 		}
 		this.sizeX = sizeX;
 		this.sizeY = sizeY;
 		this.format = format;
 		this.data = data;
		this.buffer = ByteBuffer.wrap(data).asIntBuffer();
 	}
 	
 	/**
 	 * Creates a new <code>RadiancePicture</code>.
 	 * @param sizeX The width of the image, in pixels.
 	 * @param sizeY The height of the image, in pixels.
 	 */
 	public RadiancePicture(int sizeX, int sizeY) {
 		this(sizeX, sizeY, Format.RGBE);
 	}
 	
 	/**
 	 * Creates a new <code>RadiancePicture</code>.
 	 * @param sizeX The width of the image, in pixels.
 	 * @param sizeY The height of the image, in pixels.
 	 * @param format The <code>Format</code> to store the image data in.
 	 */
 	public RadiancePicture(int sizeX, int sizeY, Format format) {
 		this.sizeX = sizeX;
 		this.sizeY = sizeY;
 		this.format = format;
 		this.data = new byte[sizeX * sizeY * 4];
		this.buffer = ByteBuffer.wrap(data).asIntBuffer();
 	}
 	
 	/**
 	 * Writes the image to a file.
 	 * @param file The name of the file to write.
 	 * @throws IOException If an error occurs while attempting to write to the
 	 * 		file.
 	 */
 	public void write(File file) throws IOException {
 		FileOutputStream stream = new FileOutputStream(file);
 		try {
 			write(stream);
 		} finally {
 			stream.close();
 		}
 	}
 	
 	/**
 	 * Writes the image to an <code>OutputStream</code>.
 	 * @param stream The <code>OutputStream</code> to write to.
 	 * @throws IOException If an error occurs while attempting to write to the
 	 * 		stream.
 	 */
 	public void write(OutputStream stream) throws IOException {
 		PrintStream p = new PrintStream(stream);
 		p.println(HEADER_IDENTIFIER);
 		p.printf("FORMAT=%s\n", format.getName());
 		p.printf("EXPOSURE=%f\n", exposure);
 		p.printf("COLORCORR=%f %f %f\n", colorcorr[0], colorcorr[1], colorcorr[2]);
 		p.printf("SOFTWARE=%s\n", software);
 		p.printf("PIXASPECT=%f\n", pixelAspect);
 		p.println();
 		p.printf("-Y %d +X %d\n", sizeY, sizeX);
 		p.flush();
 		p.write(data);
 		p.flush();
 	}
 
 	/**
 	 * Reads an image from a file.
 	 * @param file The name of the file to read.
 	 * @return The <code>RadiancePicture</code>.
 	 * @throws IOException If an error occurs while attempting to read the
 	 * 		file.
 	 */
 	public static RadiancePicture read(File file) throws IOException {
 		FileInputStream stream = new FileInputStream(file);
 		try {
 			return read(stream);
 		} finally {
 			stream.close();
 		}
 	}
 	
 	/**
 	 * Reads an image from a <code>URL</code>.
 	 * @param url The <code>URL</code> indicating the location of the file to
 	 * 		read.
 	 * @return The <code>RadiancePicture</code>.
 	 * @throws IOException If an error occurs while attempting to read the
 	 * 		file.
 	 */
 	public static RadiancePicture read(URL url) throws IOException {
 		return read(url.openStream());
 	}
 
 	/**
 	 * Converts an unsigned byte to an integer.
 	 * @param b The value to interpret as an unsigned byte.
 	 * @return The corresponding 32-bit integer value.
 	 */
 	private static int ubyte2int(byte b) {
 		return b < 0 ? ((int) b) + 256 : (int) b;
 	}
 	
 	/**
 	 * Reads an image from an <code>InputStream</code>.
 	 * @param stream The <code>InputStream</code> from which to read the image.
 	 * @return The <code>RadiancePicture</code>.
 	 * @throws IOException If an error occurs while attempting to read the
 	 * 		stream.
 	 */
 	public static RadiancePicture read(InputStream stream) throws IOException {
 		DataInputStream reader = new DataInputStream(stream);
 		Format format = Format.RGBE;
 		double exposure = 1.0;
 		double[] colorcorr = { 1.0, 1.0, 1.0 };
 		String software = "";
 		double pixelAspect = 1.0;
 		double[] primaries = { 0.640, 0.330, 0.290, 0.600, 0.150, 0.060, 0.333, 0.333 };
 		String view = "";
 		int size[] = new int[2];
 		boolean swap;
 		String line;
 		
 		line = reader.readLine();
 		if (!line.equalsIgnoreCase(HEADER_IDENTIFIER)) {
 			throw new IOException("Incorrect format.");
 		}
 		
 		while (true) {
 			line = reader.readLine().trim();
 			if (line.isEmpty()) {
 				break;
 			} else if (line.startsWith("#")) {
 				continue;
 			}
 			
 			String[] args = line.split("=", 2);
 			String key = args[0].trim();
 			String val = args[1].trim();
 			
 			if (key.equalsIgnoreCase("format")) {
 				if (val.equalsIgnoreCase(Format.RGBE.getName())) {
 					format = Format.RGBE;
 				} else if (val.equalsIgnoreCase(Format.XYZE.getName())) {
 					format = Format.XYZE;
 				} else {
 					throw new IOException("Unrecognized format: " + val);
 				}
 			} else if (key.equalsIgnoreCase("exposure")) {
 				exposure *= Double.parseDouble(val);
 			} else if (key.equalsIgnoreCase("colorcorr")) {
 				String[] components = val.split("\\s+", 3);
 				for (int i = 0; i < 3; i++) {
 					colorcorr[i] *= Double.parseDouble(components[i]);
 				}
 			} else if (key.equalsIgnoreCase("software")) {
 				software = val;
 			} else if (key.equalsIgnoreCase("pixaspect")) {
 				pixelAspect *= Double.parseDouble(val);
 			} else if (key.equalsIgnoreCase("view")) {
 				view = val;
 			} else if (key.equalsIgnoreCase("primaries")) {
 				String[] components = val.split("\\s++", 8);
 				for (int i = 0; i < 8; i++) {
 					primaries[i] = Double.parseDouble(components[i]);
 				}
 			} else {
 				System.err.print("Unrecognized header variable: ");
 				System.err.println(key);
 			}
 		}
 		
 		String[] res = reader.readLine().toLowerCase().split("\\s+", 4);
 		size[0] = Integer.parseInt(res[1]);
 		size[1] = Integer.parseInt(res[3]);
 		
 		if (res[0].charAt(0) == '+') {
 			size[0] = -size[0];
 			throw new UnimplementedException();
 		}
 		if (res[2].charAt(0) == '-') {
 			size[1] = -size[1];
 			throw new UnimplementedException();
 		}
 		
 		swap = (res[0].charAt(1) == 'x');
 		if (swap) {
 			throw new UnimplementedException();
 		}
 		
 		byte[] data = new byte[size[0] * size[1] * 4];
 		ByteBuffer buffer = ByteBuffer.wrap(data);
 		byte[] next = new byte[4];
 		byte[] rep = new byte[32768*4];
 		int repmult = 1;
 		while (reader.read(next) == next.length) {
 			if (next[0] == 255 && next[1] == 255 && next[2] == 255) {
 				int len = repmult * ubyte2int(next[3]);
 				buffer.reset();
 				buffer.get(rep, 0, 4);
 				for (int i = 0; i < len; i++) {
 					buffer.put(rep, 0, 4);
 				}
 				repmult *= 256;
 			} else if (next[0] == 2 && next[1] == 2) {
 				int len = 4 * ((ubyte2int(next[2]) << 8) | ubyte2int(next[3]));
 				ByteBuffer scan = ByteBuffer.wrap(rep, 0, len);
 				while (scan.hasRemaining()) {
 					int b = reader.read();
 					if (b < 0) {
 						throw new EOFException();
 					} else if (b <= 128) {
 						for (int i = 0; i < b; i++) {
 							scan.put((byte) reader.read());
 						}
 					} else {
 						int value = reader.read();
 						if (value < 0) {
 							throw new EOFException();
 						}
 						for (int i = 0, n = b & 127; i < n; i++) {
 							scan.put((byte) value);
 						}
 					}
 				}
 				int stride = len / 4;
 				int i0 = 0;
 				int i1 = stride;
 				int i2 = i1 + stride;
 				int i3 = i2 + stride;
 				while (i0 < stride) {
 					buffer.put(rep[i0++]);
 					buffer.put(rep[i1++]);
 					buffer.put(rep[i2++]);
 					buffer.put(rep[i3++]);
 				}
 				repmult = 1;
 			} else {
 				buffer.mark();
 				buffer.put(next);
 				repmult = 1;
 			}
 		}
 		
 		RadiancePicture picture = new RadiancePicture(size[1], size[0], data, format);
 		picture.exposure = exposure;
 		picture.colorcorr = colorcorr;
 		picture.software = software;
 		picture.pixelAspect = pixelAspect;
 
 		return picture;
 		
 	}
 	
 	/**
 	 * Gets the offset at which to read the specified pixel.
 	 * @param x The x-coordinate of the pixel.
 	 * @param y The y-coordinate of the pixel.
 	 * @return The offset of the pixel.
 	 */
 	private int getOffset(int x, int y) {
 		if (x < 0 || x >= sizeX || y < 0 || y >= sizeY) {
 			throw new IllegalArgumentException();
 		}
 		return (sizeY - 1 - y) * sizeX + x;
 	}
 	
 	/**
 	 * Gets the raw value of the specified pixel.
 	 * @param x The x-coordinate of the pixel.
 	 * @param y The y-coordinate of the pixel.
 	 * @return The raw value of the pixel.
 	 */
 	private int getPixelRaw(int x, int y) {
		return buffer.get(getOffset(x, y));
 	}
 	
 	/**
 	 * Sets the raw value of the specified pixel.
 	 * @param x The x-coordinate of the pixel.
 	 * @param y The y-coordinate of the pixel.
 	 * @param value The value to set the pixel to.
 	 */
 	private void setPixelRaw(int x, int y, int value) {
		buffer.put(getOffset(x, y), value);
 	}
 	
 	/**
 	 * Gets the value of the specified pixel as an <code>RGB</code> color.
 	 * @param x The x-coordinate of the pixel.
 	 * @param y The y-coordinate of the pixel.
 	 * @return The value of the specified pixel as an <code>RGB</code> color.
 	 */
 	public RGB getPixelRGB(int x, int y) {
 		return format.interp.toRGB(getPixelRaw(x, y));
 	}
 	
 	/**
 	 * Gets the value of the specified pixel as a <code>CIEXYZ</code> color.
 	 * @param x The x-coordinate of the pixel.
 	 * @param y The y-coordinate of the pixel.
 	 * @return The value of the specified pixel as an <code>CIEXYZ</code>
 	 * 		color.
 	 */
 	public CIEXYZ getPixelXYZ(int x, int y) {
 		return format.interp.toXYZ(getPixelRaw(x, y));
 	}
 	
 	/**
 	 * Sets the value of the specified pixel as an <code>RGB</code> color.
 	 * @param x The x-coordinate of the pixel.
 	 * @param y The y-coordinate of the pixel.
 	 * @param rgb The <code>RGB</code> color to set the pixel to.
 	 */
 	public void setPixelRGB(int x, int y, RGB rgb) {
 		setPixelRaw(x, y, format.interp.toRaw(rgb));
 	}
 	
 	/**
 	 * Sets the value of the specified pixel as an <code>RGB</code> color.
 	 * @param x The x-coordinate of the pixel.
 	 * @param y The y-coordinate of the pixel.
 	 * @param r The value to set the red channel to.
 	 * @param g The value to set the green channel to.
 	 * @param b The value to set the blue channel to.
 	 */
 	public void setPixelRGB(int x, int y, double r, double g, double b) {
 		setPixelRGB(x, y, new RGB(r, g, b));
 	}
 	
 	/**
 	 * Sets the value of the specified pixel as a <code>CIEXYZ</code> color.
 	 * @param x The x-coordinate of the pixel.
 	 * @param y The y-coordinate of the pixel.
 	 * @param xyz The <code>CIEXYZ</code> color to set the pixel to.
 	 */
 	public void setPixelXYZ(int x, int y, CIEXYZ xyz) {
 		setPixelRaw(x, y, format.interp.toRaw(xyz));
 	}
 	
 	/**
 	 * Sets the value of the specified pixel as a <code>CIEXYZ</code> color.
 	 * @param x The x-coordinate of the pixel.
 	 * @param y The y-coordinate of the pixel.
 	 * @param X The value to set the X channel to.
 	 * @param Y The value to set the Y channel to.
 	 * @param Z The value to set the Z channel to.
 	 */
 	public void setPixelXYZ(int x, int y, double X, double Y, double Z) {
 		setPixelXYZ(x, y, new CIEXYZ(X, Y, Z));
 	}
 	
 	/**
 	 * Gets the width of the image.
 	 * @return The width of the image, in pixels.
 	 */
 	public int getSizeX() {
 		return sizeX;
 	}
 	
 	/**
 	 * Gets the height of the image.
 	 * @return The height of the image, in pixels.
 	 */
 	public int getSizeY() {
 		return sizeY;
 	}
 	
 	/**
 	 * Gets the <code>Format</code> that the image is stored in.
 	 * @return The <code>Format</code> that the image is stored in.
 	 */
 	public Format getFormat() {
 		return format;
 	}
 	
 }
