 package c3i.core.imageModel.server;
 
 import com.google.common.base.Preconditions;
 import com.google.common.hash.HashCode;
 import com.google.common.hash.Hashing;
 import com.google.common.io.ByteStreams;
 import com.google.common.io.Closeables;
 import com.google.common.io.Files;
 import com.google.common.io.InputSupplier;
 import org.eclipse.jgit.lib.Constants;
 import org.eclipse.jgit.lib.ObjectId;
 
 import javax.annotation.Nonnull;
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.awt.image.Raster;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.MessageDigest;
 import java.security.Provider;
 import java.security.Security;
 import java.util.HashSet;
 import java.util.Set;
 
 //import org.apache.tools.ant.filters.StringInputStream;
 
 public class ImageUtil {
 
 //    private static final int IMAGE_WIDTH = 599;
 //    private static final int IMAGE_HEIGHT = 366;
 
     public static class ImageInfo {
         private final int pixelCount;
         private final int contentPixelCount;
         private final int emptyPixelCount;
         private Pixel[] pixels;
         private int alphaCount;
 
         public ImageInfo(int height, int width, int emptyPixelCount, int contentPixelCount, Pixel[] pixels) {
             this.pixelCount = height * width;
             this.emptyPixelCount = emptyPixelCount;
             this.contentPixelCount = contentPixelCount;
             this.pixels = pixels;
             assert pixelCount == emptyPixelCount + contentPixelCount;
 
             for (Pixel pixel : pixels) {
                 alphaCount += pixel.a;
             }
 
         }
 
         public boolean isEmpty() {
             return contentPixelCount == 0;
         }
 
         public void print(File file) {
             System.out.println(alphaCount + ":" + file);
         }
 
         public boolean isAllZeroAlpha() {
             for (Pixel pixel : pixels) {
                 if (!pixel.isZeroAlpha()) return false;
             }
             return true;
         }
 
         public boolean isAll255Alpha() {
             for (Pixel pixel : pixels) {
                 if (!pixel.is255Alpha()) return false;
             }
             return true;
         }
     }
 //
 //    public static boolean isEmpty(File imageFile) {
 //        ImageInfo imageInfo = imageInfo(imageFile);
 //        return imageInfo.isEmpty();
 //    }      '
 
 
     public static boolean isEmptyPng(String fullFileName, InputSupplier<? extends InputStream> content) {
 
 
         if (!fullFileName.endsWith(".png")) return false;
         if (fullFileName.contains("01_Background")) return false;
 
         InputStream is = null;
 
         boolean retVal;
         try {
             is = content.getInput();
             retVal = ImageUtil.allPixesHaveAlphaZero(is);
         } catch (IOException e) {
             retVal = false;
            throw new RuntimeException("File[" + fullFileName + "]", e);
         } finally {
             Closeables.closeQuietly(is);
         }
 
 
         return retVal;
     }
 
     private static boolean allPixesHaveAlphaZero(InputStream is) {
         Preconditions.checkNotNull(is);
 
         BufferedImage image = readImage(is);
         final Raster raster = image.getTile(0, 0);
 
         for (int y = 0; y < raster.getHeight(); y++) {
             for (int x = 0; x < raster.getWidth(); x++) {
                 int[] pixelData = new int[4];
                 raster.getPixel(x, y, pixelData);
                 int alpha = pixelData[3];
                 if (alpha != 0) return false;
             }
         }
 
         return true;
     }
 
 
     public static ImageInfo imageInfo(File imageFile) {
         if (imageFile == null) throw new IllegalArgumentException();
         try {
             BufferedImage image = readImage(imageFile);
 
             int h = image.getHeight();
             int w = image.getWidth();
             final Raster raster = image.getTile(0, 0);
             int hh = raster.getHeight();
             int ww = raster.getWidth();
             if (hh != h) throw new IllegalStateException();
             if (ww != w) throw new IllegalStateException();
             if (hh == 0) throw new IllegalStateException();
             if (ww == 0) throw new IllegalStateException();
 
             int pixelCount = h * w;
             int contentPixelCount = 0;
             int emptyPixelCount = 0;
             Pixel[] pixels = new Pixel[pixelCount];
             if (pixelCount == 0) throw new IllegalStateException();
             int pos = -1;
             for (int y = 0; y < h; y++) {
                 for (int x = 0; x < w; x++) {
                     int[] pixelData = new int[4];
                     raster.getPixel(x, y, pixelData);
                     pos = y * w + x;
                     pixels[pos] = new Pixel(x, y, pixelData);
                     if (pixels[pos].hasContent()) contentPixelCount++;
                     if (pixels[pos].isEmpty()) emptyPixelCount++;
 
                 }
             }
             if (pos == -1) throw new IllegalStateException();
             if (pos != pixelCount - 1) throw new IllegalStateException();
             return new ImageInfo(hh, ww, emptyPixelCount, contentPixelCount, pixels);
 
         } catch (Exception e) {
             throw new RuntimeException(imageFile.toString(), e);
         }
     }
 
     public static class Pixel {
 
         public final int x;
         public final int y;
 
         public final int r;
         public final int g;
         public final int b;
         public final int a;
 
         public Pixel(int x, int y, int[] pixel) {
             this.x = x;
             this.y = y;
             r = pixel[0];
             g = pixel[1];
             b = pixel[2];
             a = pixel[3];
         }
 
         public boolean isZeroAlpha() {
             return a == 0;
         }
 
         public boolean is255Alpha() {
             return a == 255;
         }
 
         public void print() {
             System.out.println("[" + x + "," + y + "]  = (" + r + "," + g + "," + b + ") - alpha: " + a);
         }
 
 
         public boolean threeZeros() {
             return r == 0 && g == 0 && b == 0;
         }
 
         public void printAlphaIfNonZero() {
             if (a != 0) System.out.println("a: " + a);
         }
 
         public boolean hasContent() {
             return r != 0 || g != 0 || b != 0 || a != 0;
         }
 
         public boolean isEmpty() {
             return r == 0 && g == 0 && b == 0 && a == 0;
         }
     }
 
     private static BufferedImage readImage(InputStream imageData) {
         if (imageData == null) throw new IllegalArgumentException("imageData must be non-null");
         try {
             BufferedImage image = ImageIO.read(imageData);
             if (image == null) throw new IllegalStateException("Why is image null?");
             return image;
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
 
     private static BufferedImage readImage(File file) {
         if (file == null) throw new IllegalArgumentException("file must be non-null");
         try {
             BufferedImage image = ImageIO.read(file);
             if (image == null) throw new IllegalStateException("Why is image null?");
             return image;
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
 //    public static String getSHAFingerPrint(Jpg jpg) throws NoSuchAlgorithmException {
 //        StringBuilder cattedPngs = new StringBuilder();
 //        String fp = null;
 //
 //        for (ImPng png : jpg.getPngs()) {
 //            cattedPngs.append(png.getPath().toString()).append("|");
 //        }
 //
 //        MessageDigest md = MessageDigest.getInstance("MD5");
 //        md.update(cattedPngs.toString().getBytes());
 //        md.update(jpg.getPath().toString().getBytes());
 //        byte[] hashedBytes = md.digest();
 //
 //        fp = StringUtil.byteArray2Hex(hashedBytes).replaceAll(" ", "");
 //        return fp;
 //    }
 
     @Nonnull
     public static String getFingerprint(@Nonnull final String text) {
         Preconditions.checkNotNull(text);
         try {
             InputSupplier<InputStream> inputSupplier = new InputSupplier<InputStream>() {
                 public InputStream getInput() throws IOException {
                     return new ByteArrayInputStream(text.getBytes());
                 }
             };
             HashCode hash = ByteStreams.hash(inputSupplier, Hashing.sha1());
             byte[] bytes = hash.asBytes();
             return byteArray2Hex(bytes);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
 
     public static String getFingerprint(File file) {
         Preconditions.checkNotNull(file);
         byte[] bytes;
         try {
             HashCode hash = Files.hash(file, Hashing.sha1());
             bytes = hash.asBytes();
 
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         return toBase62(bytes);
     }
 
     public static ObjectId getFingerprintGitStyle(File file) {
         Preconditions.checkNotNull(file);
 
         try {
             long len = file.length();
             MessageDigest md = MessageDigest.getInstance("SHA1");
             md.reset();
 
 
             md.update(Constants.encodedTypeString(Constants.OBJ_BLOB));
             md.update((byte) ' ');
             md.update(Constants.encodeASCII(len));
             md.update((byte) 0);
 
             InputSupplier<FileInputStream> content = Files.newInputStreamSupplier(file);
 
 //            byte[] digest = ByteStreams.getDigest(content, md);
             byte[] digest = ByteStreams.hash(content, Hashing.sha1()).asBytes();
 
             return ObjectId.fromRaw(digest);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
 
 
     }
 
 //    public static InputSupplier<FileInputStream> newInputStreamSupplier(final File file) {
 //        Preconditions.checkNotNull(file);
 //        return new InputSupplier<FileInputStream>() {
 //            public FileInputStream getInput() throws IOException {
 //                return new FileInputStream(file);
 //            }
 //        };
 //    }
 //
 //    public static InputSupplier<InputStream> newInputStreamSupplier(final long blobLength) {
 //        return new InputSupplier<InputStream>() {
 //            public InputStream getInput() throws IOException {
 //                String header = "blob " + blobLength + "\0";
 //                return new StringInputStream(header);
 //            }
 //        };
 //    }
 
 
     public void test() throws Exception {
 
 
     }
     // This method returns the available implementations for a service type
 
     public static Set<String> getMessageDigestAlgorithms() {
         String serviceType = "MessageDigest";
         Set<String> result = new HashSet<String>();
 
         // All all providers
         Provider[] providers = Security.getProviders();
         for (int i = 0; i < providers.length; i++) {
             // Get services provided by each provider
             Provider provider = providers[i];
             Set<Object> propertyKeys = provider.keySet();
             for (Object propertyKey : propertyKeys) {
                 String sPropertyKey = (String) propertyKey;
                 String sPropertyKey1 = sPropertyKey.split(" ")[0];
                 if (sPropertyKey1.startsWith(serviceType + "")) {
                     result.add(sPropertyKey1.substring(serviceType.length() + 1));
                 } else if (sPropertyKey1.startsWith("Alg.Alias." + serviceType + "")) {
                     // This is an alias
                     result.add(sPropertyKey1.substring(serviceType.length() + 11) + " [alias]");
                 }
             }
         }
         return result;
     }
 
 
     /**
      * Convert a byte array to base64 string
      */
     public static String toBase64(byte[] byteArray) {
         return new sun.misc.BASE64Encoder().encode(byteArray);
     }
 
 
     /**
      * Convert a byte array to base62 string
      */
     public static String toBase62(byte[] byteArray) {
         String base64 = toBase64(byteArray);
         return toBase62(base64);
     }
 
     /**
      * Takes a base64 encoded string and eliminates the '+' and '/'.
      * Also eliminates any CRs.
      *
      * Having tokens that are a seamless string of letters and numbers
      * means that MUAs are less likely to linebreak a long token.
      */
     protected static String toBase62(String base64) {
         StringBuffer buf = new StringBuffer(base64.length() * 2);
 
         for (int i = 0; i < base64.length(); i++) {
             char ch = base64.charAt(i);
             switch (ch) {
                 case 'i':
                     buf.append("ii");
                     break;
 
                 case '+':
                     buf.append("ip");
                     break;
 
                 case '/':
                     buf.append("is");
                     break;
 
                 case '=':
                     buf.append("ie");
                     break;
 
                 case '\n':
                     // Strip out
                     break;
 
                 default:
                     buf.append(ch);
             }
         }
 
 
         return buf.toString();
     }
 
     private static final char[] hexChars = {
             '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
     };
 
     private static String byteArray2Hex(byte[] ba) {
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < ba.length; i++) {
             int hbits = (ba[i] & 0x000000f0) >> 4;
             int lbits = ba[i] & 0x0000000f;
             sb.append("" + hexChars[hbits] + hexChars[lbits] + "");
         }
         return sb.toString();
     }
 
 
 }
