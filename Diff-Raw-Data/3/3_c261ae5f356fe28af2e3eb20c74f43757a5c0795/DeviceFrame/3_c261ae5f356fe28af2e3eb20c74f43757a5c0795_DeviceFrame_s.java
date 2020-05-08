 package com.ribomation.droidAtScreen.gui;
 
 import com.ribomation.droidAtScreen.Application;
 import com.ribomation.droidAtScreen.Settings;
 import com.ribomation.droidAtScreen.cmd.*;
 import com.ribomation.droidAtScreen.dev.*;
 import org.apache.log4j.Logger;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImageOp;
 import java.util.*;
 import java.util.Timer;
 
 /**
  * Frame holder for the device image.
  */
 public class DeviceFrame extends JFrame {
   private final static RenderingHints HINTS = new RenderingHints(
       RenderingHints.KEY_INTERPOLATION,
       RenderingHints.VALUE_INTERPOLATION_BICUBIC);
 
   private final Logger log;
   private final Application app;
   private final AndroidDevice device;
 
   private int scalePercentage = 100;
   private boolean landscapeMode = false;
   private boolean upsideDown = false;
 
   private ImageCanvas canvas;
   private JComponent toolBar;
   private AffineTransform scaleTX;
   private AffineTransform upsideDownTX;
   private RecordingListener recordingListener;
   private Timer timer;
   private TimerTask retriever;
   private InfoPane infoPane;
 
   public DeviceFrame(Application app, AndroidDevice device) {
     this.app = app;
     this.device = device;
     this.log = Logger.getLogger(DeviceFrame.class.getName() + ":" +
         device.getName());
     log.debug(String.format("DeviceFrame(device=%s)", device));
 
     Settings cfg = app.getSettings();
     setScale(cfg.getPreferredScale());
     setLandscapeMode(cfg.isLandscape());
    setUpsideDown(cfg.isUpsideDown());
 
     setTitle(device.getName());
     setIconImage(GuiUtil.loadIcon("device").getImage());
     setResizable(false);
 
     add(canvas = new ImageCanvas(), BorderLayout.CENTER);
     add(toolBar = createToolBar(), BorderLayout.WEST);
     add(infoPane = new InfoPane(), BorderLayout.SOUTH);
 
     setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
     addWindowStateListener(new WindowAdapter() {
       @Override
       public void windowClosing(WindowEvent e) {
         log.debug("windowClosing");
         retriever.cancel();
         timer.cancel();
         DeviceFrame.this.setVisible(false);
       }
     });
 
     timer = new Timer("Screenshot Timer");
     timer.schedule(retriever = new Retriever(), 0, 500);
     pack();
   }
 
   protected JComponent createToolBar() {
     JPanel buttons = new JPanel(new GridLayout(5, 1, 0, 8));
     buttons.add(new OrientationCommand(this).newButton());
     buttons.add(new ScaleCommand(this).newButton());
     buttons.add(new UpsideDownCommand(this).newButton());
     buttons.add(new ScreenshotCommand(this).newButton());
     buttons.add(new RecordingCommand(this).newButton());
 
     JPanel tb = new JPanel(new FlowLayout());
     tb.setBorder(BorderFactory.createEmptyBorder());
     tb.add(buttons);
 
     return tb;
   }
 
   public class InfoPane extends JPanel {
     JLabel size, status, elapsed;
 
     InfoPane() {
       super(new GridLayout(1, 2, 3, 0));
       setBorder(BorderFactory.createEmptyBorder());
 
       Font font = getFont().deriveFont(Font.PLAIN, 12.0F);
       status = new JLabel("UNKNOWN");
       status.setFont(font);
       status.setHorizontalAlignment(SwingConstants.LEADING);
       status.setToolTipText("Device status");
 
       size = new JLabel("? x ?");
       size.setFont(font);
       size.setHorizontalAlignment(SwingConstants.CENTER);
       size.setToolTipText("Image dimension and size");
 
       elapsed = new JLabel("");
       elapsed.setFont(font);
       elapsed.setHorizontalAlignment(SwingConstants.RIGHT);
       elapsed.setToolTipText("Elapsed time and rate of last screenshot");
 
       this.add(status);
       this.add(size);
       this.add(elapsed);
     }
 
     void setSizeInfo(ImageCanvas img) {
       Dimension sz = img.getPreferredSize();
       size.setText(String.format("%dx%d (%s)", sz.width, sz.height,
           new Unit(img.getScreenshot().getRawImage().size).toString()));
     }
 
     public void setStatus(String devStatus) {
       status.setText(devStatus);
     }
 
     public void setElapsed(long time, ScreenImage img) {
       int sz = (img != null ? (int)(img.getRawImage().size / (time / 1000.0)) :
           0);
       elapsed.setText(String.format("%d ms (%s/s)", time,
           new Unit(sz).toString()));
     }
   }
 
   class Unit {
     final int K = 1024;
     final int M = K * K;
     final int G = K * M;
     long value;
 
     Unit(long value) {
       this.value = value;
     }
 
     String unit() {
       if (value / G > 0) return "Gb";
       if (value / M > 0) return "Mb";
       if (value / K > 0) return "Kb";
       return "bytes";
     }
 
     float value() {
       if (value / G > 0) return (float)value / G;
       if (value / M > 0) return (float)value / M;
       if (value / K > 0) return (float)value / K;
       return value;
     }
 
     public String toString() {
       return String.format("%.1f %s", value(), unit());
     }
   }
 
   class Retriever extends TimerTask {
     @Override
     public void run() {
       long start = System.currentTimeMillis();
       ScreenImage image = device.getScreenImage();
       long elapsed = System.currentTimeMillis() - start;
       infoPane.setElapsed(elapsed, image);
       infoPane.setStatus(device.getState().name().toUpperCase());
 
       log.debug(String.format("Got screenshot %s, elapsed %d ms", image,
           elapsed));
       log.debug(device.getProperties());
 
       boolean fresh = canvas.getScreenshot() == null;
       if (image != null) {
         if (landscapeMode) image.rotate();
         if (recordingListener != null) recordingListener.record(image);
         canvas.setScreenshot(image);
         infoPane.setSizeInfo(canvas);
       }
 
       if (fresh) {
         setTitle(device.getName());
         pack();
       }
     }
   }
 
   class ImageCanvas extends JComponent {
     private ScreenImage image;
 
     public ImageCanvas() {
       setBorder(BorderFactory.createLoweredBevelBorder());
     }
 
     public void setScreenshot(ScreenImage image) {
       this.image = image;
       repaint();
     }
 
     public ScreenImage getScreenshot() {
       return image;
     }
 
     @Override
     protected void paintComponent(Graphics g) {
       if (image != null && g instanceof Graphics2D) {
         Graphics2D g2 = (Graphics2D)g;
         BufferedImageOp tx = null;
 
         if (scaleTX != null) {
           tx = new AffineTransformOp(scaleTX, HINTS);
         }
 
         if (upsideDownTX != null) {
           if (tx == null) {
             tx = new AffineTransformOp(upsideDownTX, HINTS);
           } else {
             AffineTransform SCTX = (AffineTransform)scaleTX.clone();
             SCTX.concatenate(upsideDownTX);
             tx = new AffineTransformOp(SCTX, HINTS);
           }
         }
 
         g2.drawImage(image.toBufferedImage(), tx, 0, 0);
       } else {
         g.setColor(Color.RED);
         g.setFont(getFont().deriveFont(16.0F));
         g.drawString("No screenshot yet", 10, 25);
       }
     }
 
     @Override
     public Dimension getPreferredSize() {
       if (image == null) return new Dimension(200, 300);
       return new Dimension(scale(image.getWidth()), scale(image.getHeight()));
     }
 
     @Override
     public Dimension getMinimumSize() {
       return getPreferredSize();
     }
   }
 
   public void setLandscapeMode(boolean landscape) {
     this.landscapeMode = landscape;
   }
 
   public void setScale(int scalePercentage) {
     this.scalePercentage = scalePercentage;
     if (scalePercentage == 100) {
       scaleTX = null;
     } else {
       double scale = scalePercentage / 100.0;
       scaleTX = AffineTransform.getScaleInstance(scale, scale);
     }
   }
 
   public void setUpsideDown(boolean upsideDown) {
     this.upsideDown = upsideDown;
     ScreenImage lastScreenshot = getLastScreenshot();
     if (upsideDown && lastScreenshot != null) {
       double x = lastScreenshot.getWidth() / 2;
       double y = lastScreenshot.getHeight() / 2;
       upsideDownTX = AffineTransform.getQuadrantRotateInstance(2, x, y);
     } else {
       upsideDownTX = null;
     }
   }
 
   public void setRecordingListener(RecordingListener recordingListener) {
     this.recordingListener = recordingListener;
   }
 
   public ScreenImage getLastScreenshot() {
     return canvas.getScreenshot();
   }
 
   public InfoPane getInfoPane() {
     return infoPane;
   }
 
   public AndroidDevice getDevice() {
     return device;
   }
 
   public String getName() {
     return device.getName();
   }
 
   public boolean isLandscapeMode() {
     return landscapeMode;
   }
 
   public int getScale() {
     return scalePercentage;
   }
 
   public boolean isUpsideDown() {
     return upsideDown;
   }
 
   private int scale(int value) {
     if (scalePercentage == 100) return value;
     return (int)Math.round(value * scalePercentage / 100.0);
   }
 }
