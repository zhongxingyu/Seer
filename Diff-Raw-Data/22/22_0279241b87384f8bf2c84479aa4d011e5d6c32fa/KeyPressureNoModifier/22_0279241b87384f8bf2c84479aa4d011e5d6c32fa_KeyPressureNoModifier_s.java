 package org.malai.swing.interaction.library;
 
 import java.awt.event.KeyEvent;
 
 import org.malai.interaction.TerminalState;
 import org.malai.interaction.library.KeyInteraction;
 
 /**
  * This interaction occurs when a key (but NOT a modifier) of a keyboard is pressed.<br>
  * <br>
  * This file is part of Malai.<br>
  * Copyright (c) 2005-2014 Arnaud BLOUIN<br>
  * <br>
  * Malai is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later version.
  * <br>
  * Malai is distributed without any warranty; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.<br>
  * <br>
  * 02/14/2013<br>
  * @author Arnaud BLOUIN
  * @since 0.2
  */
 public class KeyPressureNoModifier extends KeyInteraction {
 	/**
 	 * Creates the interaction.
 	 */
 	public KeyPressureNoModifier() {
 		super();
 		initStateMachine();
 	}
 
 
 	@SuppressWarnings("unused")
 	@Override
 	protected void initStateMachine() {
 		final TerminalState pressed = new TerminalState("pressed"); //$NON-NLS-1$
 
 		addState(pressed);
 		new KeyInteractionKeyPressedTransition(initState, pressed) {
 			@Override
 			public boolean isGuardRespected() {
				return super.isGuardRespected() && key!=KeyEvent.VK_SHIFT && key!=KeyEvent.VK_CONTROL &&
 						key!=KeyEvent.VK_ALT && key!=KeyEvent.VK_ALT_GRAPH && key!=KeyEvent.VK_WINDOWS;
 			}
 
 		};
 	}
 }
