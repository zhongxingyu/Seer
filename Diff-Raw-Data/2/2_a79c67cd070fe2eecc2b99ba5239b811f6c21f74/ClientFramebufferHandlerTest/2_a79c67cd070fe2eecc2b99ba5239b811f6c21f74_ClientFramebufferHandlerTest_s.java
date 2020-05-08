 /*
  * Copyright (C) 2013 Pauli Kauppinen
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
 package org.javnce.vnc.server;
 
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.javnce.eventing.Event;
 import org.javnce.eventing.EventLoop;
 import org.javnce.eventing.EventSubscriber;
 import org.javnce.rfb.types.Encoding;
 import org.javnce.rfb.types.Framebuffer;
 import org.javnce.rfb.types.Point;
 import org.javnce.rfb.types.Rect;
 import org.javnce.vnc.common.FbChangeEvent;
 import org.javnce.vnc.common.FbEncodingsEvent;
 import org.javnce.vnc.common.FbRequestEvent;
 import org.javnce.vnc.common.FbUpdateEvent;
 import org.javnce.vnc.server.platform.FramebufferDevice;
 import org.junit.After;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 public class ClientFramebufferHandlerTest {
 
     static final Rect area = new Rect(new Point(0, 0), FramebufferDevice.factory().size());
 
     static class Tester implements EventSubscriber {
 
         final EventLoop eventLoop;
         final Thread thread;
         FbUpdateEvent updateEvent;
 
         Tester() {
             eventLoop = new EventLoop();
             eventLoop.subscribe(FbUpdateEvent.eventId(), this);
             thread = new Thread(eventLoop);
         }
 
         synchronized FbUpdateEvent get() {
             if (null == updateEvent) {
                 try {
                     wait();
                 } catch (InterruptedException ex) {
                     Logger.getLogger(ClientFramebufferHandlerTest.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             FbUpdateEvent temp = updateEvent;
             updateEvent = null;
             notifyAll();
             return temp;
         }
 
         @After
         public void tearDown() throws Exception {
             assertFalse(EventLoop.exists());
         }
 
         @Override
         synchronized public void event(Event event) {
             if (null != updateEvent) {
                 try {
                     wait();
                 } catch (InterruptedException ex) {
                     Logger.getLogger(ClientFramebufferHandlerTest.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             updateEvent = (FbUpdateEvent) event;
             notifyAll();
         }
     }
 
     @Test
     public void testFirstUpdateNonIncremental() {
         Tester tester = new Tester();
         tester.thread.start();
 
         ClientFramebufferHandler handler = new ClientFramebufferHandler(tester.eventLoop);
         handler.init();
 
         tester.eventLoop.publish(new FbRequestEvent(false, area));
 
         handler.start();
 
         Framebuffer[] fb = tester.get().get();
         assertEquals(1, fb.length);
         assertEquals(area, fb[0].rect());
         assertEquals(Encoding.RAW, fb[0].encoding());
         tester.eventLoop.shutdownGroup();
     }
 
     @Test
     public void testFirstUpdateIncremental() {
         Tester tester = new Tester();
         tester.thread.start();
 
         ClientFramebufferHandler handler = new ClientFramebufferHandler(tester.eventLoop);
         handler.init();
 
         tester.eventLoop.publish(new FbRequestEvent(true, area));
 
         handler.start();
 
         Framebuffer[] fb = tester.get().get();
         assertEquals(1, fb.length);
         assertEquals(area, fb[0].rect());
         assertEquals(Encoding.RAW, fb[0].encoding());
         tester.eventLoop.shutdownGroup();
     }
 
     @Test
     public void testEncode() {
         Tester tester = new Tester();
         tester.thread.start();
 
         ClientFramebufferHandler handler = new ClientFramebufferHandler(tester.eventLoop);
         handler.init();
 
         handler.start();
 
         tester.eventLoop.publish(new FbEncodingsEvent(new int[]{Encoding.JaVNCeRLE}));
         tester.eventLoop.publish(new FbRequestEvent(false, area));
 
         Framebuffer[] fb = tester.get().get();
         assertEquals(1, fb.length);
         assertEquals(area, fb[0].rect());
        assertEquals(Encoding.JaVNCeRLE, fb[0].encoding());
         tester.eventLoop.shutdownGroup();
     }
 
     @Test
     public void testWaitChange() {
         Tester tester = new Tester();
         tester.thread.start();
 
         ClientFramebufferHandler handler = new ClientFramebufferHandler(tester.eventLoop);
         handler.init();
 
         handler.start();
 
         //Get first
         tester.eventLoop.publish(new FbRequestEvent(true, area));
         Framebuffer[] fb = tester.get().get();
 
         //Get next
         tester.eventLoop.publish(new FbRequestEvent(true, area));
 
         //Notify change of few pixel
         Rect changed = new Rect(10, 20, 30, 40);
         ArrayList<Rect> list = new ArrayList<>();
         list.add(changed);
         tester.eventLoop.publish(new FbChangeEvent(list));
 
         //Should get the changed area
         fb = tester.get().get();
         assertEquals(1, fb.length);
         assertEquals(changed, fb[0].rect());
 
         tester.eventLoop.shutdownGroup();
     }
 
     @Test
     public void testOnlyChanged() {
         Tester tester = new Tester();
         tester.thread.start();
 
         ClientFramebufferHandler handler = new ClientFramebufferHandler(tester.eventLoop);
         handler.init();
 
         handler.start();
 
         //Get first
         tester.eventLoop.publish(new FbRequestEvent(true, area));
         Framebuffer[] fb = tester.get().get();
 
         //Notify change of few pixel
         Rect changed = new Rect(10, 20, 30, 40);
         ArrayList<Rect> list = new ArrayList<>();
         list.add(changed);
         tester.eventLoop.publish(new FbChangeEvent(list));
 
         //Get next
         tester.eventLoop.publish(new FbRequestEvent(true, area));
 
         //Should get the changed area
         fb = tester.get().get();
         assertEquals(1, fb.length);
         assertEquals(changed, fb[0].rect());
 
         tester.eventLoop.shutdownGroup();
     }
 }
