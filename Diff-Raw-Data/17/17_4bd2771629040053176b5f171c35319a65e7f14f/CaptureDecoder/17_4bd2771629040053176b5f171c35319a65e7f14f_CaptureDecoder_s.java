 package no.ebakke.studycaster.screencasting;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InterruptedIOException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.zip.GZIPInputStream;
 import no.ebakke.studycaster.api.StudyCaster;
 import no.ebakke.studycaster.ui.StatusFrame;
 
 public class CaptureDecoder extends Codec {
   private DataInputStream dis;
   private boolean reachedEOF = false, atFrame = false;
   private Image pointerImage;
   private Point pointerImageHotSpot;
   private long currentMetaTime = -1, currentFrameTime = 0, firstMetaTime = -1;
   private long nextCaptureTime = -1, lastBeforeCaptureTime = -1;
   private boolean firstFrameRead = false;
   private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
   private int frameNo = 0;
   private static final String POINTER_IMAGE_PATH =
       "no/ebakke/studycaster/resources/pointer_shine_weaker.png";
 
   public CaptureDecoder(InputStream is) throws IOException {
     dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(is)));
     if (!dis.readUTF().equals(MAGIC_STRING))
       throw new IOException("Screencast not in StudyCaster format");
     int width  = dis.readInt();
     int height = dis.readInt();
     init(new Dimension(width, height));
 
     /* Do this to properly catch an error which may otherwise cause waitForAll()
     below to hang with an "Uncaught error fetching image" output. */
     InputStream testIS = StatusFrame.class.getClassLoader().getResourceAsStream(
         POINTER_IMAGE_PATH);
     if (testIS == null) {
       throw new IOException("Cannot load pointer image; check classpath.");
     } else {
       testIS.close();
     }
     URL pointerImageURL = StatusFrame.class.getClassLoader().getResource(
        POINTER_IMAGE_PATH);
     
     pointerImage = Toolkit.getDefaultToolkit().getImage(pointerImageURL);
     pointerImageHotSpot = new Point(41,40);
     MediaTracker mt = new MediaTracker(new Canvas());
     mt.addImage(pointerImage, 0);
     try {
       mt.waitForAll();
     } catch (InterruptedException e) {
       throw new InterruptedIOException();
     }
    System.out.println("got here 8");
   }
 
   public long getCurrentTimeMillis() {
     if (currentMetaTime < -1)
       throw new IllegalStateException("No frame retrieved yet.");
     return currentFrameTime;
   }
 
   private void drawMousePointer(BufferedImage image, Point p) {
     Graphics2D g = image.createGraphics();
     if (!g.drawImage(pointerImage, p.x - pointerImageHotSpot.x, p.y - pointerImageHotSpot.y, null))
       throw new AssertionError("Expected immediate image conversion");
     g.dispose();
   }
 
   private void drawTimeStamp(BufferedImage image) {
     String formatted =
         String.format("%6d / ", frameNo) +
         dateFormat.format(new Date(currentMetaTime)) +
         String.format(" / %6d", (currentMetaTime - firstMetaTime) / 1000L);
     drawString(image, formatted, 440, 13);
   }
 
   private void drawString(BufferedImage image, String str, int x, int y) {
     Graphics2D g = image.createGraphics();
     g.setFont(new Font("Monospaced", Font.PLAIN, 14));
     g.setColor(Color.WHITE);
     g.drawString(str, x - 1, y - 1);
     g.drawString(str, x + 1, y + 1);
     g.setColor(Color.BLACK);
     g.drawString(str, x, y);
     g.dispose();
   }
 
   /** Skip forward, one byte at a time, until a valid MARKER_META structure has been read. Used to
   recover data in corrupted streams. */
   private void resync() throws IOException {
     // TODO: Avoid loosing the metadata read to sync.
     // TODO: Consider removing all of this once we have found the bug that led us to write it.
     final int MARKER_META_STRUCT_SZ = 18;
     byte buf[] = new byte[1024 * 1024];
     int curReadLoc = 0;
     while (curReadLoc < MARKER_META_STRUCT_SZ)
       buf[curReadLoc++] = dis.readByte();
     while (true) {
       DataInputStream din = new DataInputStream(new ByteArrayInputStream(buf,
           curReadLoc - MARKER_META_STRUCT_SZ, MARKER_META_STRUCT_SZ));
       byte struct_head = din.readByte();
       byte struct_type = din.readByte();
       long struct_time = din.readLong();
       int  struct_x    = din.readInt();
       int  struct_y    = din.readInt();
 
       if (struct_head != Codec.MARKER_META) {
       } else if (struct_type < 0 || struct_type >= FrameType.values().length) {
         // Two years before this code was written/January 1st 2050.
       } else if (struct_time < 1250816790841L || struct_time > 2524608000000L) {
       } else if (struct_x == Integer.MIN_VALUE && struct_y != Integer.MIN_VALUE) {
       } else if (struct_x != Integer.MIN_VALUE &&
           (struct_x < 0 || struct_y < 0 || struct_x > 30000 || struct_y > 30000)) {
       } else {
         StudyCaster.log.log(Level.INFO, "Resynced after skipping {0} bytes", curReadLoc);
         return;
       }
 
       if (curReadLoc == buf.length)
         throw new IOException("Failed to resync after skipping " + curReadLoc + "bytes");
       buf[curReadLoc++] = dis.readByte();
     }
   }
 
   private boolean readUntilFrame() throws IOException {
     MetaStamp ms;
     byte headerMarker;
     while (true) {
       try {
         headerMarker = dis.readByte();
       } catch (EOFException e) {
         return false;
       }
       if        (headerMarker == MARKER_FRAME) {
         return true;
       } else if (headerMarker == MARKER_META) {
         ms = MetaStamp.readFromStream(dis);
         switch (ms.getType()) {
           case        BEFORE_CAPTURE:
             lastBeforeCaptureTime = ms.getTimeMillis();
           break; case AFTER_CAPTURE:
             if (lastBeforeCaptureTime < 0)
               throw new IOException("Missing before-capture timestamp");
             //nextCaptureTime = lastBeforeCaptureTime / 2 + ms.getTimeMillis() / 2;
             nextCaptureTime = lastBeforeCaptureTime;
             lastBeforeCaptureTime = -1;
           break; case PERIODIC:
         }
         metaStamps.add(ms);
       } else {
         throw new IOException("Invalid header marker.");
       }
     }
   }
 
   public BufferedImage nextFrame() throws IOException {
     MetaStamp ms;
     while (true) {
       ms = metaStamps.peek();
       if (ms == null || ms.getTimeMillis() >= nextCaptureTime) {
         if (reachedEOF) {
           return null;
         } else if (atFrame) {
           readFrame();
           atFrame = false;
           firstFrameRead = true;
           continue;
         } else {
           reachedEOF = !readUntilFrame();
           atFrame = true;
           continue;
         }
       }
       metaStamps.remove();
       if (firstFrameRead && ms.getType() == FrameType.PERIODIC) {
         currentFrameTime +=
             (currentMetaTime < 0) ? 0 : Math.max(1L, ms.getTimeMillis() - currentMetaTime);
         currentMetaTime = ms.getTimeMillis();
         firstMetaTime = (firstMetaTime >= 0) ? firstMetaTime : currentMetaTime;
         BufferedImage ret = new ScreenCastImage(getDimension());
         copyImage(getCurrentFrame(), ret);
         if (ms.getMouseLocation() != null)
           drawMousePointer(ret, ms.getMouseLocation());
         drawTimeStamp(ret);
         return ret;
       }
     }
   }
 
   private void readFrame() throws IOException {
     swapOldNew();
     final byte oldBuf[] = getPreviousFrame().getBuffer();
     final byte newBuf[] = getCurrentFrame().getBuffer();
     int  currentRunLength = 0;
     byte currentRunCode   = INDEX_NO_DIFF;
     byte code;
 
     for (int i = 0; i < newBuf.length; i++) {
       if (currentRunLength == 0) {
         code = dis.readByte();
         if (code == INDEX_REPEAT) {
           currentRunLength = dis.readInt();
         } else {
           currentRunLength = 1;
           currentRunCode   = code;
         }
       }
       if (currentRunLength <= 0 || currentRunLength > newBuf.length - i) {
         StudyCaster.log.log(Level.WARNING,
             "Invalid or overflowing run length {0}", currentRunLength);
         currentRunLength = 0;
         // 23 = light blue, 14 = turquoise
         for (; i < newBuf.length; i++)
           newBuf[i] = 23;
         drawString(getCurrentFrame(), "Encoder warning: invalid run length", 50,
             getDimension().height - 100);
         resync();
         break;
       }
       newBuf[i] = (currentRunCode == INDEX_NO_DIFF) ? oldBuf[i] : currentRunCode;
       currentRunLength--;
     }
     if (currentRunLength > 0) {
       throw new AssertionError(
           "Invalid run length should have been caught by earlier consistency check");
     }
     frameNo++;
   }
 }
