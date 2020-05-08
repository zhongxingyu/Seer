 /*
  * MovieClipEvent.java
  * Transform
  *
  * Copyright (c) 2001-2010 Flagstone Software Ltd. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *  * Neither the name of Flagstone Software Ltd. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.flagstone.transform.movieclip;
 
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 
 import com.flagstone.transform.action.Action;
 import com.flagstone.transform.action.ActionData;
 import com.flagstone.transform.coder.CoderException;
 import com.flagstone.transform.coder.Context;
 import com.flagstone.transform.coder.SWFDecoder;
 import com.flagstone.transform.coder.SWFEncodeable;
 import com.flagstone.transform.coder.SWFEncoder;
 import com.flagstone.transform.coder.SWFFactory;
 
 /**
  * <p>
  * ClipEvent is used to define the actions that a movie clip will execute in
  * response to a particular event. ClipEvent objects are added to an
  * {@link Place2} object and the actions are registered with the Flash Player
  * when the movie clip is added to the display list.
  * </p>
  *
  * <p>
  * The events that a movie clip responds to are:
  * </p>
  *
  * <table class="datasheet">
  * <tr>
  * <td valign="top">Load</td>
  * <td>the movie clip is finished loading.</td>
  * </tr>
  * <tr>
  * <td valign="top">Unload</td>
  * <td>the movie clip is unloaded from the parent movie.</td>
  * </tr>
  * <tr>
  * <td valign="top">EnterFrame</td>
  * <td>when the mouse enters the Flash Player window.</td>
  * </tr>
  * <tr>
  * <td valign="top">MouseMove</td>
  * <td>the mouse pointer is moved.</td>
  * </tr>
  * <tr>
  * <td valign="top">MouseDown</td>
  * <td>the left mouse button is pressed while the cursor is outside of the
  * bounding rectangle of the movie clip.</td>
  * </tr>
  * <tr>
  * <td valign="top">MouseUp</td>
  * <td>the left mouse button is pressed and released while the cursor is outside
  * of the bounding rectangle of the movie clip.</td>
  * </tr>
  * <tr>
  * <td valign="top">KeyDown</td>
  * <td>a key is pressed on the keyboard. From Flash 6 a key code can be
  * specified to identify a specific key rather than testing for the value inside
  * the actions that are executed in response to the event.</td>
  * </tr>
  * <tr>
  * <td valign="top">KeyUp</td>
  * <td>akey being pressed on the keyboard is released.</td>
  * </tr>
  * <tr>
  * <td valign="top">Data</td>
  * <td>a GetUrl2 action is executed with the movie clip specified as a target.</td>
  * </tr>
  * <tr>
  * <td valign="top">Construct</td>
  * <td>This event is not documented by Adobe.</td>
  * </tr>
  * </table>
  *
  * <p>
  * Starting with Flash 6 movie clips also respond to the same set of events as
  * buttons, see {@link ButtonEventHandler}
  * </p>
  *
  * <p>
  * A ClipEvent object can define the actions that will be executed in response
  * to more than one event, simply bitwise OR together the individual event
  * codes:
  * </p>
  *
  * <pre>
  * int loadAndMouseMove = ClipEvent.Load | ClipEvent.MouseMove;
  * </pre>
  *
  * @see Place2
  */
 //TODO(class)
 public final class MovieClipEventHandler implements SWFEncodeable {
 
     private static final String FORMAT = "MovieClipEventHandler: { event=%d; keyCode=%s; actions=%s }";
 
     private int event;
     private int keyCode;
     private List<Action> actions;
 
     private transient int offset;
 
     /**
      * Creates and initialises a MovieClipEventHandler object using values encoded
      * in the Flash binary format.
      *
      * @param coder
      *            an SWFDecoder object that contains the encoded Flash data.
      *
      * @param context
      *            a Context object used to manage the decoders for different
      *            type of object and to pass information on how objects are
      *            decoded.
      *
      * @throws CoderException
      *             if an error occurs while decoding the data.
      */
     public MovieClipEventHandler(final SWFDecoder coder, final Context context)
             throws CoderException {
        final int eventSize = (Context.VERSION > 5) ? 4 : 2;
 
         event = coder.readWord(eventSize, false);
         offset = coder.readWord(4, false);
 
         if ((event & 131072) != 0) {
             keyCode = coder.readByte();
             offset -= 1;
         }
 
         actions = new ArrayList<Action>();
 
         final SWFFactory<Action> decoder = context.getRegistry()
                 .getActionDecoder();
         final int end = coder.getPointer() + (offset << 3);
 
         if (decoder == null) {
             actions.add(new ActionData(coder.readBytes(new byte[offset])));
         } else {
             while (coder.getPointer() < end) {
                 actions.add(decoder.getObject(coder, context));
             }
         }
     }
 
     /**
      * Creates a ClipEvent object that with an array of actions that will be
      * executed when a particular event occurs.
      *
      * @param eventCode
      *            the code representing one or more events.
      * @param anArray
      *            the array of actions that will be executed when the specified
      *            event occurs.
      */
     public MovieClipEventHandler(final Set<MovieClipEvent> eventCode,
             final List<Action> anArray) {
         setEvent(eventCode);
         setActions(anArray);
     }
 
     /**
      * Creates a ClipEvent object that defines the array of actions that will be
      * executed when a particular event occurs or when the specified key is
      * pressed.
      *
      * @param eventCode
      *            the code representing one or more events.
      * @param keyCode
      *            the ASCII code for the key pressed on the keyboard.
      * @param anArray
      *            the array of actions that will be executed when the specified
      *            event occurs. Must not be null.
      */
     public MovieClipEventHandler(final Set<MovieClipEvent> eventCode,
             final int keyCode, final List<Action> anArray) {
         setEvent(eventCode);
         setKeyCode(keyCode);
         setActions(anArray);
     }
 
     /**
      * Creates and initialises a MovieClipEventHandler object using the values copied
      * from another MovieClipEventHandler object.
      *
      * @param object
      *            a MovieClipEventHandler object from which the values will be
      *            copied.
      */
     public MovieClipEventHandler(final MovieClipEventHandler object) {
         event = object.event;
         keyCode = object.keyCode;
 
         actions = new ArrayList<Action>(object.actions.size());
 
         for (final Action action : object.actions) {
             actions.add(action.copy());
         }
     }
 
     /**
      * Adds an action to the array of actions.
      *
      * @param anAction
      *            an action object. Must not be null.
      */
     public MovieClipEventHandler add(final Action anAction)
             throws CoderException {
         if (anAction == null) {
             throw new NullPointerException();
         }
         actions.add(anAction);
         return this;
     }
 
     /** TODO(method). */
     public void setEvent(final Set<MovieClipEvent> set) {
         for (final MovieClipEvent clipEvent : set) {
             switch (clipEvent) {
             case LOAD:
                 event |= 1;
                 break;
             case ENTER_FRAME:
                 event |= 2;
                 break;
             case UNLOAD:
                 event |= 4;
                 break;
             case MOUSE_MOVE:
                 event |= 8;
                 break;
             case MOUSE_DOWN:
                 event |= 16;
                 break;
             case MOUSE_UP:
                 event |= 32;
                 break;
             case  KEY_DOWN:
                 event |= 64;
                 break;
             case KEY_UP:
                 event |= 128;
                 break;
             case DATA:
                 event |= 256;
                 break;
             case INITIALIZE:
                 event |= 512;
                 break;
             case PRESS:
                 event |= 1024;
                 break;
             case RELEASE:
                 event |= 2048;
                 break;
             case RELEASE_OUT:
                 event |= 4096;
                 break;
             case ROLL_OVER:
                 event |= 8192;
                 break;
             case ROLL_OUT:
                 event |= 16384;
                 break;
             case DRAG_OVER:
                 event |= 32768;
                 break;
             case DRAG_OUT:
                 event |= 0;
                 break;
             case KEY_PRESS:
                 event |= 131072;
                 break;
             case CONSTRUCT:
                 event |= 262144;
                 break;
             default: 
                 throw new IllegalArgumentException();
             }
         }
     }
 
     /** TODO(method). */
     public Set<MovieClipEvent> getEvent() {
         final Set<MovieClipEvent> set = EnumSet.noneOf(MovieClipEvent.class);
 
         if ((event & 1) != 0) {
             set.add(MovieClipEvent.LOAD);
         }
         if ((event & 2) != 0) {
             set.add(MovieClipEvent.ENTER_FRAME);
         }
         if ((event & 4) != 0) {
             set.add(MovieClipEvent.UNLOAD);
         }
         if ((event & 8) != 0) {
             set.add(MovieClipEvent.MOUSE_MOVE);
         }
         if ((event & 16) != 0) {
             set.add(MovieClipEvent.MOUSE_DOWN);
         }
         if ((event & 32) != 0) {
             set.add(MovieClipEvent.MOUSE_UP);
         }
         if ((event & 64) != 0) {
             set.add(MovieClipEvent.KEY_DOWN);
         }
         if ((event & 128) != 0) {
             set.add(MovieClipEvent.KEY_UP);
         }
         if ((event & 256) != 0) {
             set.add(MovieClipEvent.DATA);
         }
         if ((event & 512) != 0) {
             set.add(MovieClipEvent.INITIALIZE);
         }
         if ((event & 1024) != 0) {
             set.add(MovieClipEvent.PRESS);
         }
         if ((event & 2048) != 0) {
             set.add(MovieClipEvent.RELEASE);
         }
         if ((event & 4096) != 0) {
             set.add(MovieClipEvent.RELEASE_OUT);
         }
         if ((event & 8192) != 0) {
             set.add(MovieClipEvent.ROLL_OVER);
         }
         if ((event & 16384) != 0) {
             set.add(MovieClipEvent.ROLL_OUT);
         }
         if ((event & 32768) != 0) {
             set.add(MovieClipEvent.DRAG_OVER);
         }
         if ((event & 65536) != 0) {
             set.add(MovieClipEvent.DRAG_OUT);
         }
         if ((event & 131072) != 0) {
             set.add(MovieClipEvent.KEY_PRESS);
         }
         if ((event & 262144) != 0) {
             set.add(MovieClipEvent.CONSTRUCT);
         }
         return set;
     }
     
     /** TODO(method). */
     public int getEventCode() {
         return event;
     }
 
     /**
      * Returns the code for the key that triggers the event when pressed. The
      * code is typically the ASCII code for standard western keyboards.
      */
     public int getKeyCode() {
         return keyCode;
     }
 
     /**
      * Sets the code for the key that triggers the event when pressed. The code
      * is typically the ASCII code for standard western keyboards.
      *
      * @param code
      *            the ASCII code for the key that triggers the event.
      */
     public void setKeyCode(final int code) {
         keyCode = code;
     }
 
     /**
      * Sets the array of actions that are executed by the movie clip in response
      * to specified event(s).
      *
      * @param array
      *            the array of actions that will be executed when the specified
      *            event occurs. Must not be null.
      */
     public void setActions(final List<Action> array) {
         if (array == null) {
             throw new NullPointerException();
         }
         actions = array;
     }
 
     /**
      * Returns the array of actions that are executed by the movie clip.
      */
     public List<Action> getActions() {
         return actions;
     }
 
     /** TODO(method). */
     public MovieClipEventHandler copy() {
         return new MovieClipEventHandler(this);
     }
 
     @Override
     public String toString() {
         return String.format(FORMAT, event, keyCode, actions);
     }
 
     /** {@inheritDoc} */
     public int prepareToEncode(final SWFEncoder coder, final Context context) {
         int length = 4 + ((context.getVariables().get(Context.VERSION) > 5) ? 4 : 2);
 
         offset = (event & 131072) == 0 ? 0 : 1;
 
         for (final Action action : actions) {
             offset += action.prepareToEncode(coder, context);
         }
 
         length += offset;
 
         return length;
     }
 
     /** {@inheritDoc} */
     public void encode(final SWFEncoder coder, final Context context)
             throws CoderException {
         final int eventSize = (context.getVariables().get(Context.VERSION)> 5) ? 4 : 2;
 
         coder.writeWord(event, eventSize);
         coder.writeWord(offset, 4);
 
         if ((event & 131072) != 0) {
             coder.writeByte(keyCode);
         }
 
         for (final Action action : actions) {
             action.encode(coder, context);
         }
     }
 }
