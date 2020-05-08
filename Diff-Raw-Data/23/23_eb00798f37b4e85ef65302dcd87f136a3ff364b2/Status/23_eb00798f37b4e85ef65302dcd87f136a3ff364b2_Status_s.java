 /* Cortado - a video player java applet
  * Copyright (C) 2004 Fluendo S.L.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Street #330, Boston, MA 02111-1307, USA.
  */
 
 package com.fluendo.player;
 
 import java.awt.*;
 import java.awt.image.*;
 import java.awt.event.*;
 import java.util.*;
 
 public class Status extends Component implements MouseListener,
         MouseMotionListener {
     private static final long serialVersionUID = 1L;
 
     private int bufferPercent;
     private boolean buffering;
 
     private String message;
     private String error;
 
     private Rectangle r;
     private Component component;
 
     private Font font = new Font("SansSerif", Font.PLAIN, 10);
 
     private boolean haveAudio;
     private boolean havePercent;
     private boolean seekable;
 
     private static final int NONE = 0;
     private static final int BUTTON1 = 1;
     private static final int BUTTON2 = 2;
     private static final int SEEKER = 3;
     private static final int SEEKBAR = 4;
     private int clicked = NONE;
 
     private Color button1Color;
     private Color button2Color;
     private Color seekColor;
 
     private static final int SEEK_END = 60;
 
     public static final int STATE_STOPPED = 0;
     public static final int STATE_PAUSED = 1;
     public static final int STATE_PLAYING = 2;
 
     private int state = STATE_STOPPED;
 
     private double position = 0;
     private long time;
     private double duration;
 
     private String speaker = "\0\0\0\0\0\357\0\0\357U\27"
             + "\36\0\0\0\0\357\357\0\0" + "\0\357U\30\0\0\0\357\0\357"
             + "\0\357\0\0\357\23\357" + "\357\357\0\34\357\0Z\357\0"
             + "\357\\\357\0)+F\357\0\0\357" + "\0\357r\357Ibz\221\357"
             + "\0\0\357\0\357r\357\357\357" + "\276\323\357\0Z\357\0\357"
             + "\\\0\0\0\357\357\357\0" + "\357\0\0\357\0\0\0\0\0\357"
             + "\357\0\0\0\357\\\0\0\0" + "\0\0\0\357\0\0\357\\\0\0";
 
     private Image speakerImg;
 
     private Vector listeners = new Vector();
 
     public Status(Component comp) {
         int[] pixels = new int[12 * 10];
         component = comp;
 
         for (int i = 0; i < 120; i++) {
             pixels[i] = 0xff000000 | (speaker.charAt(i) << 16)
                     | (speaker.charAt(i) << 8) | (speaker.charAt(i));
         }
         speakerImg = comp.getToolkit().createImage(
                 new MemoryImageSource(12, 10, pixels, 0, 12));
         button1Color = Color.black;
         button2Color = Color.black;
         seekColor = Color.black;
     }
 
     public void addStatusListener(StatusListener l) {
         listeners.addElement(l);
     }
 
     public void removeStatusListener(StatusListener l) {
         listeners.remove(l);
     }
 
     public void notifyNewState(int newState) {
         for (Enumeration e = listeners.elements(); e.hasMoreElements();) {
             ((StatusListener) e.nextElement()).newState(newState);
         }
     }
 
     public void notifySeek(double position) {
         for (Enumeration e = listeners.elements(); e.hasMoreElements();) {
             ((StatusListener) e.nextElement()).newSeek(position);
         }
     }
 
     public void update(Graphics g) {
         paint(g);
     }
 
     private void paintBox(Graphics g) {
         g.setColor(Color.darkGray);
         g.drawRect(0, 0, r.width - 1, r.height - 1);
         g.setColor(Color.black);
         g.fillRect(1, 1, r.width - 2, r.height - 2);
     }
 
     private void paintPercent(Graphics g) {
         if (havePercent) {
             g.setColor(Color.white);
             g.drawString("" + bufferPercent + "%", r.width - 38, r.height - 2);
         }
     }
 
     private void paintPlayPause(Graphics g) {
 	int x,y,w,h;
 
 	x = 1;
 	y = 1;
 	w = r.height-2;
 	h = r.height-2;
         g.setColor(Color.darkGray);
         g.drawRect(x, y, w, h);
         g.setColor(button1Color);
         g.fillRect(x+1, y+1, w-1, h-1);
         if (state == STATE_PLAYING) {
            g.setColor(Color.darkGray);
             g.fillRect((int)(w * .4), (int)(h * .4), (int)(w * .2), (int)(h * .5));
             g.fillRect((int)(w * .7), (int)(h * .4), (int)(w * .2), (int)(h * .5));
         } else {
             int triangleX[] = { (int)(w*.4), (int)(w*.4), (int)(w*.9) };
             int triangleY[] = { (int)(w*.3), (int)(w*.9), (int)(w*.6) };
            g.setColor(Color.darkGray);
             g.fillPolygon(triangleX, triangleY, 3);
         }
     }
 
     private void paintStop(Graphics g) {
 	int x,y,w,h;
 
 	x = r.height + 1;
 	y = 1;
 	w = r.height - 2;
 	h = r.height - 2;
 
         g.setColor(Color.darkGray);
         g.drawRect(x, y, w, h);
         g.setColor(button2Color);
         g.fillRect(x+1, y+1, w-1, h-1);
        g.setColor(Color.darkGray);
         g.fillRect(r.height + (int)(w * .4), (int)(w * .4), (int)(w * .5), (int)(w * .5));
     }
 
     private void paintMessage(Graphics g, int pos) {
         if (message != null) {
             g.setColor(Color.white);
             g.drawString(message, pos, r.height - 2);
         }
     }
 
     private void paintBuffering(Graphics g, int pos) {
         g.setColor(Color.white);
         g.drawString("Buffering", pos, r.height - 2);
     }
 
     private void paintSeekBar(Graphics g) {
         int pos, end, base;
 
         end = r.width - SEEK_END - (r.height * 2);
 
 	base = r.height*2 + 1;
 
         g.setColor(Color.darkGray);
         g.drawRect(base, 2, end, r.height-4);
 
         pos = (int) (end * position);
 
         g.setColor(Color.gray);
         g.fillRect(base + 2, 5, pos, r.height-9);
 
        g.setColor(Color.darkGray);
         g.drawLine(pos + base + 1, 1,  pos + base + 7, 1);
         g.drawLine(pos + base + 1, r.height-1, pos + base + 7, r.height-1);
         g.drawLine(pos + base,     2,  pos + base    , r.height-2);
         g.drawLine(pos + base + 8, 2,  pos + base + 8, r.height-2);
 
         g.setColor(seekColor);
         g.fillRect(pos + base + 1, 2, 7, r.height-3);
     }
 
     private void paintTime(Graphics g) {
         long hour, min, sec;
         int end;
 
         if (time < 0)
             return;
 
         sec = time % 60;
         min = time / 60;
         hour = min / 60;
         min %= 60;
 
         r = getBounds();
 
         end = r.width - 50;
 
         g.setColor(Color.white);
         g.drawString("" + hour + ":" + (min < 10 ? "0" + min : "" + min) + ":"
                 + (sec < 10 ? "0" + sec : "" + sec), end, r.height - 2);
     }
 
     private void paintSpeaker(Graphics g) {
         if (haveAudio) {
             g.drawImage(speakerImg, r.width - 12, r.height - 11, null);
         }
     }
 
     public void paint(Graphics g) {
         if (!isVisible())
             return;
 
         r = getBounds();
 
         Image img = component.createImage(r.width, r.height);
         Graphics g2 = img.getGraphics();
         g2.setFont(font);
 
         paintBox(g2);
         if (seekable) {
             paintPlayPause(g2);
             paintStop(g2);
             if (buffering) {
                 paintPercent(g2);
                 paintBuffering(g2, r.height*2 + 3);
 	    }
             else if (state == STATE_STOPPED) {
                 paintMessage(g2, r.height*2 + 3);
             } else {
                 paintSeekBar(g2);
                 paintTime(g2);
             }
         } else {
             if (buffering) {
                 paintBuffering(g2, 2);
                 paintPercent(g2);
 	    }
 	    else {
                 paintMessage(g2, 2);
                 paintTime(g2);
 	    }
         }
         paintSpeaker(g2);
 
         g.drawImage(img, r.x, r.y, null);
         img.flush();
     }
 
     public void setBufferPercent(boolean buffering, int bp) {
         this.buffering = buffering;
         this.bufferPercent = bp;
         component.repaint();
     }
 
     public void setTime(double seconds) {
         if (clicked == NONE) {
             if (seconds < duration)
                 time = (long) seconds;
             else
                 time = (long) duration;
 
             position = ((double) time) / duration;
             component.repaint();
         }
     }
 
     public void setDuration(double seconds) {
         duration = seconds;
         component.repaint();
     }
 
     public void setMessage(String m) {
         message = m;
         component.repaint();
     }
 
     public void setHaveAudio(boolean a) {
         haveAudio = a;
         component.repaint();
     }
 
     public void setHavePercent(boolean p) {
         havePercent = p;
         component.repaint();
     }
 
     public void setSeekable(boolean s) {
         seekable = s;
         component.repaint();
     }
 
     public void setState(int aState) {
         state = aState;
         component.repaint();
     }
 
     private boolean intersectButton1(MouseEvent e) {
         return (e.getX() >= 0 && e.getX() <= r.height-2 && e.getY() > 0 && e.getY() <= r.height-2);
     }
 
     private boolean intersectButton2(MouseEvent e) {
         return (e.getX() >= r.height && e.getX() <= r.height + r.height-2 && e.getY() > 0 && e.getY() <= r.height-2);
     }
 
     private boolean intersectSeeker(MouseEvent e) {
         int end;
 
         r = getBounds();
 
         end = r.width - SEEK_END - (r.height * 2);
         int pos = (int) (end * position) + r.height*2+1;
 
         return (e.getX() >= pos && e.getX() <= pos + 9 && e.getY() > 0 && e
                 .getY() <= r.height-2);
     }
 
     private boolean intersectSeekbar(MouseEvent e) {
         int end;
 
         r = getBounds();
 
         end = r.width - SEEK_END;
 
         return (e.getX() >= r.height*2 && e.getX() <= end && e.getY() > 0 && e
                 .getY() <= r.height-2);
     }
 
     private int findComponent(MouseEvent e) {
         if (intersectButton1(e))
             return BUTTON1;
         else if (intersectButton2(e))
             return BUTTON2;
         else if (intersectSeeker(e))
             return SEEKER;
         else if (intersectSeekbar(e))
             return SEEKBAR;
         else
             return NONE;
     }
 
     public void mouseClicked(MouseEvent e) {
     }
 
     public void mouseEntered(MouseEvent e) {
     }
 
     public void mouseExited(MouseEvent e) {
     }
 
     public void mousePressed(MouseEvent e) {
         if (seekable) {
             e.translatePoint(-1, -1);
             clicked = findComponent(e);
 	    if (clicked == SEEKBAR) {
 	      clicked = SEEKER;
               seekColor = Color.gray;
 	      mouseDragged (e);
 	    }
         }
     }
 
     public void mouseReleased(MouseEvent e) {
         if (seekable) {
             int comp;
 
             e.translatePoint(-1, -1);
 
             comp = findComponent(e);
             if (clicked != comp) {
                 if (clicked == SEEKER)
                     comp = clicked;
                 else
                     return;
             }
 
             switch (comp) {
             case BUTTON1:
                 if (state == STATE_PLAYING) {
                     state = STATE_PAUSED;
                     notifyNewState(state);
                 } else {
                     state = STATE_PLAYING;
                     notifyNewState(state);
                 }
                 break;
             case BUTTON2:
                 state = STATE_STOPPED;
                 notifyNewState(state);
                 break;
             case SEEKER:
                 notifySeek(position);
                 break;
             case SEEKBAR:
                 break;
             case NONE:
                 break;
             }
             clicked = NONE;
             component.repaint();
         }
     }
 
     public void mouseDragged(MouseEvent e) {
         if (seekable) {
             e.translatePoint(-1, -1);
             if (clicked == SEEKER) {
                 int end = r.width - SEEK_END - (r.height * 2);
                 double pos = (e.getX() - (r.height*2 + 5)) / (double) (end);
 
                 if (pos < 0.0)
                     position = 0.0;
                 else if (pos > 1.0)
                     position = 1.0;
                 else
                     position = pos;
 
                 time = (long) (duration * position);
 
                 component.repaint();
             }
         }
     }
 
     public void mouseMoved(MouseEvent e) {
         if (seekable) {
             e.translatePoint(-1, -1);
 
             if (intersectButton1(e)) {
                 button1Color = Color.gray;
             } else {
                 button1Color = Color.black;
 
                 if (intersectButton2(e)) {
                     button2Color = Color.gray;
                 } else {
                     button2Color = Color.black;
 
                     if (intersectSeeker(e)) {
                         seekColor = Color.gray;
                     } else
                         seekColor = Color.black;
                 }
             }
 
             component.repaint();
         }
     }
 }
