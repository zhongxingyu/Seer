 /*
  * Copyright 2011 Alexander Baumgartner
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.madthrax.ridiculousRPG.video;
 
 import java.applet.Applet;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.net.URL;
 
 import com.fluendo.player.Cortado;
 
 /**
  * This class extends the Cortado video player {@link Applet}.<br>
  * It hides some features like context menu and sets the used default
  * parameters.<br>
  * <ul>
  * <li>Ogg Theora</li>
  * <li>Ogg Vorbis</li>
  * <li>Mulaw audio</li>
  * <li>MJPEG</li>
  * <li>Smoke codec</li>
  * </ul>
  * 
  * @see http://www.theora.org/cortado/
  * @author Alexander Baumgartner
  */
 public class CortadoPlayerApplet extends Cortado {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * You may set this to false on startup if there are problems with this
 	 * creepy shutdown hack.
 	 */
 	public static boolean shutdownCortadoHook = true;
 	private Graphics graphics;
	//private CortadoPlayerAppletWrapper stub;
 
 	/**
 	 * This is an applet! You should NEVER use this constructor manually!<br>
 	 * Use
 	 * {@link CortadoPlayerAppletWrapper#CortadoPlayerAppletWrapper(URL, com.badlogic.gdx.math.Rectangle, boolean, boolean)
 	 * instead!
 	 */
 	public CortadoPlayerApplet(CortadoPlayerAppletWrapper stub,
 			Graphics graphics) {
		//this.stub = stub;
 		this.graphics = graphics;
 	}
 
	/* This doesn't work for standard cortado download, because
	 * all class names other than Cortado are encrypted.
	 * For example the class Message becomes the letter a :/
 	@Override
 	public void handleMessage(Message msg) {
 		if (msg.getType() == Message.EOS || msg.getType() == Message.ERROR) {
 			stub.stop();
 		}
 		super.handleMessage(msg);
 	}
	*/
 
 	@Override
 	public Graphics getGraphics() {
 		return graphics;
 	}
 
 	@Override
 	public void destroy() {
 		// Cortado strikes to release it's resourecs,
 		// thats why we have to do a lot of crazy stuff below
 		try {
 			stop();
 			setStub(null);
			//stub = null;
 			graphics = null;
 			super.destroy();
 			if (isActive()) {
 				removeAll();
 				// try to crash it
 				doPlay();
 			}
 		} catch (Throwable ignored) {
 		}
 		// spawn thread to force jvm exit
 		if (isActive() && shutdownCortadoHook) {
 			shutdownCortadoHook = false;
 			System.out.println("Cortado shutdown hook thread started");
 			final Thread current = Thread.currentThread();
 			new Thread() {
 				@Override
 				public void run() {
 					do
 						try {
 							sleep(2000);
 						} catch (InterruptedException e) {
 						}
 					while (current.isAlive());
 					System.exit(0);
 				}
 			}.start();
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 	}
 }
