 /*
  * Copyright (C) 2012 Pauli Kauppinen
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package example.swingclient;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import javax.swing.BorderFactory;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import org.javnce.rfb.types.Encoding;
 import org.javnce.rfb.types.Framebuffer;
 import org.javnce.rfb.types.PixelFormat;
 import org.javnce.rfb.types.Rect;
 import org.javnce.rfb.types.Size;
 import org.javnce.upnp.RemoteServerInfo;
 import org.javnce.vnc.client.RemoteVncServerObserver;
 import org.javnce.vnc.client.VncClientController;
 
 /**
  * The class for showing the vnc image.
  */
 public class VncPanel extends JPanel implements RemoteVncServerObserver {
 
     /**
      * The image.
      */
     final private VncImage image;
     /**
      * The server info.
      */
     final private RemoteServerInfo serverInfo;
     /**
      * The controller.
      */
     final private VncClientController controller;
     /**
      * The rect.
      */
     private Rect rect;
     /**
      * The mouse event dispatcher.
      */
     private VncPointerDispatcher mouseEventDispatcher;
     /**
      * The key event dispatcher.
      */
     private VncKeyDispatcher keyEventDispatcher;
 
     /**
      * Instantiates a new vnc panel.
      *
      * @param info the info
      */
     public VncPanel(RemoteServerInfo info) {
         this.setFocusable(true);
         serverInfo = info;
         controller = new VncClientController();
         image = VncImage.instance();
         init();
     }
 
     /**
      * Inits the controller.
      */
     private void init() {
         setBorder(BorderFactory.createLineBorder(Color.black));
 
         controller.launch(serverInfo.getAddress(), this);
     }
 
     /* (non-Javadoc)
      * @see javax.swing.JComponent#getPreferredSize()
      */
     @Override
     public Dimension getPreferredSize() {
         return new Dimension(800, 480);
     }
 
     /* (non-Javadoc)
      * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
      */
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         image.paint(g, getWidth(), getHeight());
     }
 
     /* (non-Javadoc)
      * @see org.javnce.vnc.client.VncClientObserver#connectionClosed()
      */
     @Override
     public void connectionClosed() {
         final Component item = this;
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 JOptionPane.showMessageDialog(item, "VNC disconnected.", "Disconnected", JOptionPane.ERROR_MESSAGE);
                 System.exit(1);
             }
         });
     }
 
     /* (non-Javadoc)
      * @see org.javnce.vnc.client.VncClientObserver#initFramebuffer(org.javnce.rfb.types.PixelFormat, org.javnce.rfb.types.Size)
      */
     @Override
     public void initFramebuffer(PixelFormat format, Size size) {
         final PixelFormat f = format;
         final Size s = size;
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 init(f, s);
             }
         });
     }
 
     /**
      * Inits the image and dispatchers.
      *
      * @param format the format
      * @param size the size
      */
     private void init(PixelFormat format, Size size) {
         rect = new Rect(0, 0, size.width(), size.height());
         PixelFormat newformat = image.setFormat(size, format);
         if (null != newformat && !newformat.equals(format)) {
             VncClientController.setFormat(newformat);
         }
         mouseEventDispatcher = new VncPointerDispatcher(size.width(), size.height());
         mouseEventDispatcher.register(this);
 
 
         keyEventDispatcher = new VncKeyDispatcher();
         keyEventDispatcher.register(this);
 
         VncClientController.requestFramebuffer(false, rect);
         requestFocus();
     }
 
     /* (non-Javadoc)
      * @see org.javnce.vnc.client.VncClientObserver#framebufferUpdate(org.javnce.rfb.types.Framebuffer[])
      */
     @Override
     public void framebufferUpdate(Framebuffer[] buffers) {
         final Framebuffer[] b = buffers;
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 write(b);
             }
         });
     }
 
     /**
      * Write vnc image to swing image.
      *
      * @param array the array
      */
     private void write(Framebuffer[] array) {
         VncClientController.requestFramebuffer(true, rect);
         RgbDataBuffer dataBuffer = image.dataBuffer();
 
         for (Framebuffer fb : array) {
             if (Encoding.RAW == fb.encoding()) {
                 dataBuffer.writeRaw(fb.rect().x(),
                         fb.rect().y(),
                         fb.rect().width(),
                         fb.rect().height(),
                         fb.asOneBuffer());
             } else if (Encoding.JaVNCeRLE == fb.encoding()) {
                 dataBuffer.writeRle(fb.rect().x(),
                         fb.rect().y(),
                         fb.rect().width(),
                         fb.rect().height(),
                         fb.asOneBuffer());
             }
         }
         paintImmediately(0, 0, getWidth(), getHeight());
     }
 }
