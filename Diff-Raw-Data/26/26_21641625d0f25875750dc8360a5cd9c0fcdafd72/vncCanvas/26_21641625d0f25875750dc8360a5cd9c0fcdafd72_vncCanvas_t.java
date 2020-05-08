 //
 //  Copyright (C) 2001 HorizonLive.com, Inc.  All Rights Reserved.
 //  Copyright (C) 2001 Const Kaplinsky.  All Rights Reserved.
 //  Copyright (C) 2000 Tridia Corporation.  All Rights Reserved.
 //  Copyright (C) 1999 AT&T Laboratories Cambridge.  All Rights Reserved.
 //
 //  This is free software; you can redistribute it and/or modify
 //  it under the terms of the GNU General Public License as published by
 //  the Free Software Foundation; either version 2 of the License, or
 //  (at your option) any later version.
 //
 //  This software is distributed in the hope that it will be useful,
 //  but WITHOUT ANY WARRANTY; without even the implied warranty of
 //  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //  GNU General Public License for more details.
 //
 //  You should have received a copy of the GNU General Public License
 //  along with this software; if not, write to the Free Software
 //  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 //  USA.
 //
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import java.io.*;
import java.lang.*;
 import java.util.zip.*;
 
 
 //
 // vncCanvas is a subclass of Canvas which draws a VNC desktop on it.
 //
 
 class vncCanvas extends Canvas
   implements KeyListener, MouseListener, MouseMotionListener {
 
   vncviewer v;
   rfbProto rfb;
   ColorModel cm;
   Color[] colors;
 
   Image rawPixelsImage;
   MemoryImageSource pixelsSource;
   byte[] pixels;
 
   byte[] zlibBuf;
   int zlibBufLen = 0;
   Inflater zlibInflater;
 
   final static int tightZlibBufferSize = 512;
   Inflater[] tightInflaters;
 
   vncCanvas(vncviewer v1) throws IOException {
     v = v1;
     rfb = v.rfb;
 
     cm = new DirectColorModel(8, 7, (7 << 3), (3 << 6));
 
     rfb.writeSetPixelFormat(8, 8, false, true, 7, 7, 3, 0, 3, 6);
 
     colors = new Color[256];
     for (int i = 0; i < 256; i++)
       colors[i] = new Color(cm.getRGB(i));
 
     pixels = new byte[rfb.framebufferWidth * rfb.framebufferHeight];
 
     pixelsSource =
       new MemoryImageSource(rfb.framebufferWidth, rfb.framebufferHeight,
                             cm, pixels, 0, rfb.framebufferWidth);
     pixelsSource.setAnimated(true);
     rawPixelsImage = createImage(pixelsSource);
 
     if (!v.options.viewOnly) {
       addKeyListener(this);
       addMouseListener(this);
       addMouseMotionListener(this);
     }
 
     tightInflaters = new Inflater[4];
   }
 
   public Dimension getPreferredSize() {
     return new Dimension(rfb.framebufferWidth, rfb.framebufferHeight);
   }
 
   public Dimension getMinimumSize() {
     return new Dimension(rfb.framebufferWidth, rfb.framebufferHeight);
   }
 
   public Dimension getMaximumSize() {
     return new Dimension(rfb.framebufferWidth, rfb.framebufferHeight);
   }
 
   public void update(Graphics g) {
     g.drawImage(rawPixelsImage, 0, 0, this);
   }
 
   public void paint(Graphics g) {
     g.drawImage(rawPixelsImage, 0, 0, this);
   }
 
   //
   // processNormalProtocol() - executed by the rfbThread to deal with the
   // RFB socket.
   //
 
   public void processNormalProtocol() throws IOException {
 
     rfb.writeFramebufferUpdateRequest(0, 0, rfb.framebufferWidth,
 				      rfb.framebufferHeight, false);
 
     //
     // main dispatch loop
     //
 
     while (true) {
       int msgType = rfb.readServerMessageType();
 
       switch (msgType) {
       case rfbProto.FramebufferUpdate:
 	rfb.readFramebufferUpdate();
 
 	for (int i = 0; i < rfb.updateNRects; i++) {
 	  rfb.readFramebufferUpdateRectHdr();
 
 	  if (rfb.updateRectEncoding == rfb.EncodingLastRect)
 	    break;
 
 	  if (rfb.updateRectEncoding == rfb.EncodingXCursor ||
 	      rfb.updateRectEncoding == rfb.EncodingRichCursor) {
 	    handleCursorShapeUpdate(rfb.updateRectEncoding,
 				    rfb.updateRectX, rfb.updateRectY,
 				    rfb.updateRectW, rfb.updateRectH);
 	    continue;
 	  }
 
 	  softCursorLockArea(rfb.updateRectX, rfb.updateRectY,
 			     rfb.updateRectW, rfb.updateRectH);
 
 	  switch (rfb.updateRectEncoding) {
 
 	  case rfbProto.EncodingRaw:
 	  {
 	    drawRawRect(rfb.updateRectX, rfb.updateRectY,
 			rfb.updateRectW, rfb.updateRectH);
 	    break;
 	  }
 
 	  case rfbProto.EncodingCopyRect:
 	  {
 	    rfb.readCopyRect();
 	    softCursorLockArea(rfb.copyRectSrcX, rfb.copyRectSrcY,
 			       rfb.updateRectW, rfb.updateRectH);
 	    handleCopyRect();
 	    break;
 	  }
 
 	  case rfbProto.EncodingRRE:
 	  {
 	    int rx = rfb.updateRectX, ry = rfb.updateRectY;
 	    int rw = rfb.updateRectW, rh = rfb.updateRectH;
 	    int nSubrects = rfb.is.readInt();
 	    int bg = rfb.is.read();
 	    int pixel, x, y, w, h;
 
 	    fillLargeArea(rx, ry, rw, rh, (byte)bg);
 
 	    for (int j = 0; j < nSubrects; j++) {
 	      pixel = rfb.is.read();
 	      x = rx + rfb.is.readUnsignedShort();
 	      y = ry + rfb.is.readUnsignedShort();
 	      w = rfb.is.readUnsignedShort();
 	      h = rfb.is.readUnsignedShort();
 
 	      fillSmallArea(x, y, w, h, (byte)pixel);
 	    }
 
 	    handleUpdatedPixels(rx, ry, rw, rh);
 	    break;
 	  }
 
 	  case rfbProto.EncodingCoRRE:
 	  {
 	    int rx = rfb.updateRectX, ry = rfb.updateRectY;
 	    int rw = rfb.updateRectW, rh = rfb.updateRectH;
 	    int nSubrects = rfb.is.readInt();
 	    int bg = rfb.is.read();
 	    int pixel, x, y, w, h;
 
 	    fillLargeArea(rx, ry, rw, rh, (byte)bg);
 
 	    for (int j = 0; j < nSubrects; j++) {
 	      pixel = rfb.is.read();
 	      x = rx + rfb.is.read();
 	      y = ry + rfb.is.read();
 	      w = rfb.is.read();
 	      h = rfb.is.read();
 
 	      fillSmallArea(x, y, w, h, (byte)pixel);
 	    }
 
 	    handleUpdatedPixels(rx, ry, rw, rh);
 	    break;
 	  }
 
 	  case rfbProto.EncodingHextile:
 	  {
 	    int rx = rfb.updateRectX, ry = rfb.updateRectY;
 	    int rw = rfb.updateRectW, rh = rfb.updateRectH;
 	    int bg = 0, fg = 0, sx, sy, sw, sh;
 
 	    for (int ty = ry; ty < ry + rh; ty += 16) {
 
 	      int th = 16;
 	      if (ry + rh - ty < 16)
 		th = ry + rh - ty;
 
 	      for (int tx = rx; tx < rx + rw; tx += 16) {
 
 		int tw = 16;
 		if (rx + rw - tx < 16)
 		  tw = rx + rw - tx;
 
 		int subencoding = rfb.is.read();
 
 		if ((subencoding & rfb.HextileRaw) != 0) {
 		  for (int j = ty; j < (ty + th); j++) {
 		    rfb.is.readFully(pixels, j*rfb.framebufferWidth+tx, tw);
 		  }
 		  continue;
 		}
 
 		if ((subencoding & rfb.HextileBackgroundSpecified) != 0)
 		  bg = rfb.is.read();
 
 		fillLargeArea(tx, ty, tw, th, (byte)bg);
 
 		if ((subencoding & rfb.HextileForegroundSpecified) != 0)
 		  fg = rfb.is.read();
 
 		if ((subencoding & rfb.HextileAnySubrects) != 0) {
 
 		  int nSubrects = rfb.is.read();
 
 		  if ((subencoding & rfb.HextileSubrectsColoured) != 0) {
 
 		    for (int j = 0; j < nSubrects; j++) {
 		      fg = rfb.is.read();
 		      int b1 = rfb.is.read();
 		      int b2 = rfb.is.read();
 		      sx = tx + (b1 >> 4);
 		      sy = ty + (b1 & 0xf);
 		      sw = (b2 >> 4) + 1;
 		      sh = (b2 & 0xf) + 1;
 
 		      fillSmallArea(sx, sy, sw, sh, (byte)fg);
 		    }
 
 		  } else {
 
 		    for (int j = 0; j < nSubrects; j++) {
 		      int b1 = rfb.is.read();
 		      int b2 = rfb.is.read();
 		      sx = tx + (b1 >> 4);
 		      sy = ty + (b1 & 0xf);
 		      sw = (b2 >> 4) + 1;
 		      sh = (b2 & 0xf) + 1;
 
 		      fillSmallArea(sx, sy, sw, sh, (byte)fg);
 		    }
 		  }
 		}
 	      }
 	      handleUpdatedPixels(rx, ty, rw, th);
 	    }
 	    break;
 	  }
 
 	  case rfbProto.EncodingZlib:
 	  {
 	    int nBytes = rfb.is.readInt();
 
             if (( zlibBuf == null ) ||
                 ( zlibBufLen < nBytes )) {
               zlibBuf = new byte[ nBytes * 2 ];
               zlibBufLen = nBytes * 2;
             }
 
             rfb.is.readFully( zlibBuf, 0, nBytes );
 
             if ( zlibInflater == null ) {
               zlibInflater = new Inflater();
             }
             zlibInflater.setInput( zlibBuf, 0, nBytes );
 
             drawZlibRect( rfb.updateRectX, rfb.updateRectY,
                           rfb.updateRectW, rfb.updateRectH );
 
 	    break;
 	  }
 
 	  case rfbProto.EncodingTight:
 	  {
 	    drawTightRect( rfb.updateRectX, rfb.updateRectY,
 			   rfb.updateRectW, rfb.updateRectH );
 
 	    break;
 	  }
 
 	  default:
 	    throw new IOException("Unknown RFB rectangle encoding " +
 				  rfb.updateRectEncoding);
 	  }
 
 	  softCursorUnlockScreen();
 	}
 	rfb.writeFramebufferUpdateRequest(0, 0, rfb.framebufferWidth,
 					  rfb.framebufferHeight, true);
 	break;
 
       case rfbProto.SetColourMapEntries:
 	throw new IOException("Can't handle SetColourMapEntries message");
 
       case rfbProto.Bell:
         Toolkit.getDefaultToolkit().beep();
 	break;
 
       case rfbProto.ServerCutText:
 	String s = rfb.readServerCutText();
 	v.clipboard.setCutText(s);
 	break;
 
       default:
 	throw new IOException("Unknown RFB message type " + msgType);
       }
     }
   }
 
 
   //
   // Draw a raw rectangle.
   //
 
   void drawRawRect(int x, int y, int w, int h) throws IOException {
 
     for (int j = y; j < (y + h); j++) {
       rfb.is.readFully(pixels, j * rfb.framebufferWidth + x, w);
     }
 
     handleUpdatedPixels(x, y, w, h);
   }
 
 
   //
   // Handle CopyRect rectangle (fast version).
   //
 
   void handleCopyRect() throws IOException {
 
     int sx = rfb.copyRectSrcX, sy = rfb.copyRectSrcY;
     int rx = rfb.updateRectX, ry = rfb.updateRectY;
     int rw = rfb.updateRectW, rh = rfb.updateRectH;
 
     int y0, y1, delta;
     if (sy > ry) {
       y0 = 0; y1 = rh; delta = 1;
     } else if (sy < ry) {
       y0 = rh - 1; y1 = -1; delta = -1;
     } else {
       handleCopyRectSlow();
       return;
     }
 
     for (int dy = y0; dy != y1; dy += delta) {
      System.arraycopy(pixels, (sy + dy) * rfb.framebufferWidth + sx,
                       pixels, (ry + dy) * rfb.framebufferWidth + rx, rw);
     }
 
     handleUpdatedPixels(rx, ry, rw, rh);
   }
 
 
   //
   // Handle CopyRect rectangle (slow version, but scanlines may overlap).
   //
 
   void handleCopyRectSlow() throws IOException {
 
     int sx = rfb.copyRectSrcX, sy = rfb.copyRectSrcY;
     int rx = rfb.updateRectX, ry = rfb.updateRectY;
     int rw = rfb.updateRectW, rh = rfb.updateRectH;
 
     int dx, dy;
     if (sy * rfb.framebufferWidth + sx > ry * rfb.framebufferWidth + rx) {
       for (dy = 0; dy < rh; dy++) {
 	for (dx = 0; dx < rw; dx++) {
 	  pixels[(ry + dy) * rfb.framebufferWidth + (rx + dx)] =
 	    pixels[(sy + dy) * rfb.framebufferWidth + (sx + dx)];
 	}
       }
     } else {
       for (dy = rh - 1; dy >= 0; dy--) {
 	for (dx = rw - 1; dx >= 0; dx--) {
 	  pixels[(ry + dy) * rfb.framebufferWidth + (rx + dx)] =
 	    pixels[(sy + dy) * rfb.framebufferWidth + (sx + dx)];
 	}
       }
     }
 
     handleUpdatedPixels(rx, ry, rw, rh);
   }
 
 
   //
   // Draw a zlib rectangle.
   //
 
   void drawZlibRect(int x, int y, int w, int h) throws IOException {
 
     try {
       for (int j = y; j < (y + h); j++) {
         zlibInflater.inflate( pixels, j * rfb.framebufferWidth + x, w );
       }
     }
     catch( DataFormatException dfe ) {
       throw new IOException( dfe.toString());
     }
 
     handleUpdatedPixels(x, y, w, h);
   }
 
 
   //
   // Draw a tight rectangle.
   //
 
   void drawTightRect(int x, int y, int w, int h) throws IOException {
 
     int comp_ctl = rfb.is.readUnsignedByte();
 
     // Flush zlib streams if we are told by the server to do so.
     for (int stream_id = 0; stream_id < 4; stream_id++) {
       if ((comp_ctl & 1) != 0 && tightInflaters[stream_id] != null) {
 	tightInflaters[stream_id] = null;
       }
       comp_ctl >>= 1;
     }
 
     // Check correctness of subencoding value.
     if (comp_ctl > rfb.TightMaxSubencoding) {
       throw new IOException("Incorrect tight subencoding: " + comp_ctl);
     }
 
     // Handle solid rectangles.
     if (comp_ctl == rfb.TightFill) {
       int bg = rfb.is.readUnsignedByte();
 
       fillLargeArea(x, y, w, h, (byte)bg);
       handleUpdatedPixels(x, y, w, h);
       return;
     }
 
     // Read filter id and parameters.
     int numColors = 0, rowSize = w;
     byte palette[] = new byte[2];
     if ((comp_ctl & rfb.TightExplicitFilter) != 0) {
       int filter_id = rfb.is.readUnsignedByte();
       if (filter_id == rfb.TightFilterPalette) {
 	numColors = rfb.is.readUnsignedByte() + 1; // Must be 2.
 	if (numColors != 2) {
 	  throw new IOException("Incorrect tight palette size: " + numColors);
 	}
 	palette[0] = rfb.is.readByte();
 	palette[1] = rfb.is.readByte();
 	rowSize = (w + 7) / 8;
       } else if (filter_id != rfb.TightFilterCopy) {
 	throw new IOException("Incorrect tight filter id: " + filter_id);
       }
     }
 
     // Read, optionally uncompress and decode data.
     int dataSize = h * rowSize;
     if (dataSize < rfb.TightMinToCompress) {
       if (numColors == 2) {
 	byte[] monoData = new byte[dataSize];
 	rfb.is.readFully(monoData, 0, dataSize);
 	drawMonoData(x, y, w, h, monoData, palette);
       } else {
 	for (int j = y; j < (y + h); j++) {
 	  rfb.is.readFully(pixels, j * rfb.framebufferWidth + x, w);
 	}
       }
     } else {
       int zlibDataLen = rfb.readCompactLen();
       byte[] zlibData = new byte[zlibDataLen];
       rfb.is.readFully(zlibData, 0, zlibDataLen);
       int stream_id = comp_ctl & 0x03;
       if (tightInflaters[stream_id] == null) {
 	tightInflaters[stream_id] = new Inflater();
       }
       Inflater myInflater = tightInflaters[stream_id];
       myInflater.setInput(zlibData, 0, zlibDataLen);
       try {
 	if (numColors == 2) {
 	  byte[] monoData = new byte[dataSize];
 	  myInflater.inflate(monoData, 0, dataSize);
 	  drawMonoData(x, y, w, h, monoData, palette);
 	} else {
 	  for (int j = y; j < (y + h); j++) {
 	    myInflater.inflate(pixels, j * rfb.framebufferWidth + x, w);
 	  }
 	}
       }
       catch(DataFormatException dfe) {
 	throw new IOException(dfe.toString());
       }
     }
 
     handleUpdatedPixels(x, y, w, h);
   }
 
 
   //
   // Decode and draw 1bpp-encoded bi-color rectangle.
   //
 
   void drawMonoData(int x, int y, int w, int h,
 		    byte[] src, byte[] palette)
     throws IOException {
 
     int dx, dy, n;
     int i = y * rfb.framebufferWidth + x;
     int rowBytes = (w + 7) / 8;
     byte b;
 
     for (dy = 0; dy < h; dy++) {
       for (dx = 0; dx < w / 8; dx++) {
 	b = src[dy*rowBytes+dx];
 	for (n = 7; n >= 0; n--)
 	  pixels[i++] = palette[b >> n & 1];
       }
       for (n = 7; n >= 8 - w % 8; n--) {
 	pixels[i++] = palette[src[dy*rowBytes+dx] >> n & 1];
       }
       i += (rfb.framebufferWidth - w);
     }
   }
 
 
   //
   // Display newly updated area of pixels.
   //
 
   synchronized void
   handleUpdatedPixels(int x, int y, int w, int h) {
 
     pixelsSource.newPixels(x, y, w, h);
 
     // Request repaint delayed by 10 milliseconds.
     repaint(10, x, y, w, h);
   }
 
 
   //
   // Emulate fillRect operation on the pixels[] array.
   // This version is optimized for very small rectangles.
   //
 
   void fillSmallArea(int x, int y, int w, int h, byte pixel) {
 
     int offset = y * rfb.framebufferWidth + x;
     for (int dy = 0; dy < h; dy++) {
       for (int dx = 0; dx < w; dx++) {
 	pixels[offset++] = pixel;
       }
       offset += (rfb.framebufferWidth - w);
     }
   }
 
 
   //
   // Emulate fillRect operation on the pixels[] array.
   // This version is optimized for rectangles that are large enough.
   //
 
   void fillLargeArea(int x, int y, int w, int h, byte pixel) {
    if (h * w == 0)
      return;
 
    int offset = y * rfb.framebufferWidth + x;
     for (int i = 0; i < w; i++) {
      pixels[offset+i] = pixel;
     }
 
    for (int rows = h - 1; rows > 0; rows--) {
      System.arraycopy(pixels, offset,
                       pixels, offset + rfb.framebufferWidth, w);
       offset += rfb.framebufferWidth;
     }
   }
 
 
   //
   // Override the ImageObserver interface method.
   // FIXME: In theory, empty imageUpdate() is not absolutely correct.
   //
 
   public boolean imageUpdate(Image img, int infoflags,
                              int x, int y, int width, int height) {
     return true;
   }
  
 
   //
   // Handle events.
   //
 
   public void keyPressed(KeyEvent evt) {
     processLocalKeyEvent(evt);
   }
   public void keyReleased(KeyEvent evt) {
     processLocalKeyEvent(evt);
   }
   public void keyTyped(KeyEvent evt) {
     evt.consume();
   }
 
   public void mousePressed(MouseEvent evt) {
     processLocalMouseEvent(evt, false);
   }
   public void mouseReleased(MouseEvent evt) {
     processLocalMouseEvent(evt, false);
   }
   public void mouseMoved(MouseEvent evt) {
     processLocalMouseEvent(evt, true);
   }
   public void mouseDragged(MouseEvent evt) {
     processLocalMouseEvent(evt, true);
   }
 
   public void processLocalKeyEvent(KeyEvent evt) {
     if (rfb != null && rfb.inNormalProtocol) {
       try {
         rfb.writeKeyEvent(evt);
       } catch (Exception e) {
 	e.printStackTrace();
       }
     }
     // Don't ever pass keyboard events to AWT for default processing. 
     // Otherwise, pressing Tab would switch focus to ButtonPanel etc.
     evt.consume();
   }
 
   public void processLocalMouseEvent(MouseEvent evt, boolean moved) {
     if (rfb != null && rfb.inNormalProtocol) {
       if (moved) {
         softCursorMove(evt.getX(), evt.getY());
       }
       try {
         rfb.writePointerEvent(evt);
       } catch (Exception e) {
         e.printStackTrace();
       }
     }
   }
 
 
   //
   // Ignored events.
   //
 
   public void mouseClicked(MouseEvent evt) {}
   public void mouseEntered(MouseEvent evt) {}
   public void mouseExited(MouseEvent evt) {}
 
 
   //////////////////////////////////////////////////////////////////
   //
   // Handle cursor shape updates (XCursor and RichCursor encodings).
   //
 
   boolean prevCursorSet = false;
 
   byte[] rcSavedArea;
   byte[] rcSource;
   boolean[] rcMask;
   int rcHotX, rcHotY, rcWidth, rcHeight;
   int rcCursorX = 0, rcCursorY = 0;
   int rcLockX, rcLockY, rcLockWidth, rcLockHeight;
   boolean rcCursorHidden, rcLockSet;
 
   //
   // Handle cursor shape update (XCursor and RichCursor encodings).
   //
 
   synchronized void
     handleCursorShapeUpdate(int encodingType,
 			    int xhot, int yhot, int width, int height)
     throws IOException {
 
     int bytesPerRow = (width + 7) / 8;
     int bytesMaskData = bytesPerRow * height;
 
     softCursorFree();
 
     if (width * height == 0)
       return;
 
     // Ignore cursor shape data if requested by user.
 
     if (v.options.ignoreCursorUpdates) {
       if (encodingType == rfb.EncodingXCursor) {
 	rfb.is.skipBytes(6 + bytesMaskData * 2);
       } else {
 	// rfb.EncodingRichCursor
 	rfb.is.skipBytes(width * height + bytesMaskData);
       }
       return;
     }
 
     // Read cursor pixel data.
 
     rcSource = new byte[width * height];
 
     if (encodingType == rfb.EncodingXCursor) {
       byte[] xcolors = new byte[6];
       rfb.is.readFully(xcolors, 0, 6);
       byte[] rcolors = new byte[2];
       rcolors[1] = (byte)((xcolors[0] >> 5 & 0x07) |
 			  (xcolors[1] >> 2 & 0x38) |
 			  (xcolors[2] & 0xC0));
       rcolors[0] = (byte)((xcolors[3] >> 5 & 0x07) |
 			  (xcolors[4] >> 2 & 0x38) |
 			  (xcolors[5] & 0xC0));
       byte[] buf = new byte[bytesMaskData];
       rfb.is.readFully(buf, 0, bytesMaskData);
 
       int x, y, n, b;
       int i = 0;
       for (y = 0; y < height; y++) {
 	for (x = 0; x < width / 8; x++) {
 	  b = buf[y * bytesPerRow + x];
 	  for (n = 7; n >= 0; n--)
 	    rcSource[i++] = rcolors[b >> n & 1];
 	}
 	for (n = 7; n >= 8 - width % 8; n--) {
 	  rcSource[i++] = rcolors[buf[y * bytesPerRow + x] >> n & 1];
 	}
       }
     } else {
       // rfb.EncodingRichCursor
       rfb.is.readFully(rcSource, 0, width * height);
     }
 
     // Read and decode mask data.
     
     byte[] buf = new byte[bytesMaskData];
     rfb.is.readFully(buf, 0, bytesMaskData);
 
     rcMask = new boolean[width * height];
 
     int x, y, n, b;
     int i = 0;
     for (y = 0; y < height; y++) {
       for (x = 0; x < width / 8; x++) {
 	b = buf[y * bytesPerRow + x];
 	for (n = 7; n >= 0; n--)
 	  rcMask[i++] = (b >> n & 1) != 0;
       }
       for (n = 7; n >= 8 - width % 8; n--) {
 	rcMask[i++] = (buf[y * bytesPerRow + x] >> n & 1) != 0;
       }
     }
 
     // Set remaining data associated with cursor.
 
     rcSavedArea = new byte[width * height];
     rcHotX = xhot;
     rcHotY = yhot;
     rcWidth = width;
     rcHeight = height;
 
     softCursorSaveArea();
     softCursorDraw();
 
     rcCursorHidden = false;
     rcLockSet = false;
 
     prevCursorSet = true;
   }
 
   //
   // softCursorLockArea(). This method should be used to prevent
   // collisions between simultaneous framebuffer update operations and
   // cursor drawing operations caused by movements of pointing device.
   // The parameters denote a rectangle where mouse cursor should not
   // be drawn. Every next call to this function expands locked area so
   // previous locks remain active.
   //
 
   synchronized void
     softCursorLockArea(int x, int y, int w, int h) {
 
     if (!prevCursorSet)
       return;
 
     if (!rcLockSet) {
       rcLockX = x;
       rcLockY = y;
       rcLockWidth = w;
       rcLockHeight = h;
       rcLockSet = true;
     } else {
       int newX = (x < rcLockX) ? x : rcLockX;
       int newY = (y < rcLockY) ? y : rcLockY;
       rcLockWidth = (x + w > rcLockX + rcLockWidth) ?
 	(x + w - newX) : (rcLockX + rcLockWidth - newX);
       rcLockHeight = (y + h > rcLockY + rcLockHeight) ?
 	(y + h - newY) : (rcLockY + rcLockHeight - newY);
       rcLockX = newX;
       rcLockY = newY;
     }
 
     if (!rcCursorHidden && softCursorInLockedArea()) {
       softCursorRestoreArea();
       rcCursorHidden = true;
     }
   }
 
   //
   // softCursorUnlockScreen(). This function discards all locks
   // performed since previous softCursorUnlockScreen() call.
   //
 
   synchronized void softCursorUnlockScreen() {
 
     if (!prevCursorSet)
       return;
 
     if (rcCursorHidden) {
       softCursorSaveArea();
       softCursorDraw();
       rcCursorHidden = false;
     }
     rcLockSet = false;
   }
 
   //
   // softCursorMove(). Moves soft cursor in particular location. This
   // function respects locking of screen areas so when the cursor is
   // moved in the locked area, it becomes invisible until
   // softCursorUnlockScreen() method is called.
   //
 
   synchronized void softCursorMove(int x, int y) {
 
     if (prevCursorSet && !rcCursorHidden) {
       softCursorRestoreArea();
       rcCursorHidden = true;
     }
 
     rcCursorX = x;
     rcCursorY = y;
 
     if (prevCursorSet && !(rcLockSet && softCursorInLockedArea())) {
       softCursorSaveArea();
       softCursorDraw();
       rcCursorHidden = false;
     }
   }
 
   //
   // Free all data associated with cursor.
   //
 
   synchronized void softCursorFree() {
 
     if (prevCursorSet) {
       softCursorRestoreArea();
       rcSavedArea = null;
       rcSource = null;
       rcMask = null;
       prevCursorSet = false;
     }
   }
 
   //////////////////////////////////////////////////////////////////
   //
   // Low-level methods implementing software cursor functionality.
   //
 
   //
   // Check if cursor is within locked part of screen.
   //
 
   boolean softCursorInLockedArea() {
 
     return (rcLockX < rcCursorX - rcHotX + rcWidth &&
 	    rcLockY < rcCursorY - rcHotY + rcHeight &&
 	    rcLockX + rcLockWidth > rcCursorX - rcHotX &&
 	    rcLockY + rcLockHeight > rcCursorY - rcHotY);
   }
 
   //
   // Save screen data in memory buffer.
   //
 
   void softCursorSaveArea() {
 
     Rectangle r = new Rectangle();
     softCursorToScreen(r, null);
     int x = r.x;
     int y = r.y;
     int w = r.width;
     int h = r.height;
 
     int dx, dy, i = 0;
     for (dy = y; dy < y + h; dy++) {
       for (dx = x; dx < x + w; dx++)
 	rcSavedArea[i++] = pixels[dy * rfb.framebufferWidth + dx];
     }
   }
 
   //
   // Restore screen data saved in memory buffer.
   //
 
   void softCursorRestoreArea() {
 
     Rectangle r = new Rectangle();
     softCursorToScreen(r, null);
     int x = r.x;
     int y = r.y;
     int w = r.width;
     int h = r.height;
 
     int dx, dy, i = 0;
     for (dy = y; dy < y + h; dy++) {
       for (dx = x; dx < x + w; dx++)
 	pixels[dy * rfb.framebufferWidth + dx] = rcSavedArea[i++];
     }
     handleUpdatedPixels(r.x, r.y, r.width, r.height);
   }
 
   //
   // Draw cursor.
   //
 
   void softCursorDraw() {
 
     int x, y, x0, y0;
     int offset;
 
     for (y = 0; y < rcHeight; y++) {
       y0 = rcCursorY - rcHotY + y;
       if (y0 >= 0 && y0 < rfb.framebufferHeight) {
 	for (x = 0; x < rcWidth; x++) {
 	  x0 = rcCursorX - rcHotX + x;
 	  if (x0 >= 0 && x0 < rfb.framebufferWidth) {
 	    offset = y * rcWidth + x;
 	    if (rcMask[offset]) {
 	      pixels[y0 * rfb.framebufferWidth + x0] = rcSource[offset];
 	    }
 	  }
 	}
       }
     }
 
     Rectangle r = new Rectangle();
     softCursorToScreen(r, null);
 
     handleUpdatedPixels(r.x, r.y, r.width, r.height);
   }
 
   //
   // Calculate position, size and offset for the part of cursor
   // located inside framebuffer bounds.
   //
 
   void softCursorToScreen(Rectangle screenArea, Point cursorOffset) {
 
     int cx = 0, cy = 0;
 
     int x = rcCursorX - rcHotX;
     int y = rcCursorY - rcHotY;
     int w = rcWidth;
     int h = rcHeight;
 
     if (x < 0) {
       cx = -x;
       w -= cx;
       x = 0;
     } else if (x + w > rfb.framebufferWidth) {
       w = rfb.framebufferWidth - x;
     }
     if (y < 0) {
       cy = -y;
       h -= cy;
       y = 0;
     } else if (y + h > rfb.framebufferHeight) {
       h = rfb.framebufferHeight - y;
     }
 
     if (w < 0) {
       cx = 0; x = 0; w = 0;
     }
     if (h < 0) {
       cy = 0; y = 0; h = 0;
     }
 
     if (screenArea != null)
       screenArea.setBounds(x, y, w, h);
     if (cursorOffset != null)
       cursorOffset.setLocation(cx, cy);
   }
 }
