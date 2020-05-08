 package net.eonz.bukkit.psduo.signs.normal;
 
 /*
  * This code is Copyright (C) 2011 Chris Bode, Some Rights Reserved.
  *
  * Copyright (C) 1999-2002 Technical Pursuit Inc., All Rights Reserved. Patent 
  * Pending, Technical Pursuit Inc.
  *
  * Unless explicitly acquired and licensed from Licensor under the Technical 
  * Pursuit License ("TPL") Version 1.0 or greater, the contents of this file are 
  * subject to the Reciprocal Public License ("RPL") Version 1.1, or subsequent 
  * versions as allowed by the RPL, and You may not copy or use this file in 
  * either source code or executable form, except in compliance with the terms and 
  * conditions of the RPL.
  *
  * You may obtain a copy of both the TPL and the RPL (the "Licenses") from 
  * Technical Pursuit Inc. at http://www.technicalpursuit.com.
  *
  * All software distributed under the Licenses is provided strictly on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TECHNICAL
  * PURSUIT INC. HEREBY DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT 
  * LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
  * PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the Licenses for specific 
  * language governing rights and limitations under the Licenses. 
  */
 
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.block.SignChangeEvent;
 
 import net.eonz.bukkit.psduo.signs.PSSign;
 import net.eonz.bukkit.psduo.signs.TriggerType;
 
 public class CountSign extends PSSign {
 
 	private int count;
	private int lastAction = 0;
 
 	protected void triggersign(TriggerType type, Object args) {
 		BlockRedstoneEvent event = (BlockRedstoneEvent) args;
 		int input = this.getInputId(event);
 
 		if (event.getOldCurrent() == 0 && event.getNewCurrent() > 0) {
 			actions[input].applyAction(this, count);
 
 			if (count < 0) {
 				if (wrapDown && hasMod) {
 					this.setCount(mod - 1);
 				} else {
 					this.setCount(0);
 				}
 			}
 		
 			if (count == countTo || (count == 0 && countTo == mod && wrapDown)) {
 				this.setOutput(true);
 			} else {
 				this.setOutput(false);
 			}
 
 			if (!wrapDown && hasMod && ((count - 1) % mod != (count - 1)))
 				this.setCount(count % mod);
 
 			if (wrapDown) {
 				this.applyMod(mod);
 			}
 
 		}
 	}
 
 	public String getData() {
 		return Integer.toString(this.count);
 	}
 
 	protected void setData(String data) {
 		if (data == null)
 			return;
 		try {
 			this.setCount(Integer.parseInt(data));
 		} catch (Exception e) {
 			this.setCount(0);
 		}
 	}
 
 	private enum ActionType {
 		INCREMENT('+'), DECREMENT('-'), SET('=');
 
 		public final char operator;
 
 		private ActionType(char operator) {
 			this.operator = operator;
 		}
 
 		public static ActionType getActionType(char operator) {
 			for (ActionType t : ActionType.values()) {
 				if (t.operator == operator)
 					return t;
 			}
 			return null;
 		}
 	}
 
 	private class EdgeAction {
 		private final ActionType type;
 		private final int value;
 
 		private EdgeAction(ActionType t, int val) {
 			this.type = t;
 			this.value = val;
 		}
 
 		public String toString() {
 			return type.operator + Integer.toString(value);
 		}
 
 		public void applyAction(CountSign s, int ccount) {
 			switch (type) {
 			case INCREMENT:
 				s.setCount(ccount + value);
 				break;
 			case DECREMENT:
 				s.setCount(ccount - value);
 				break;
 			case SET:
 				s.setCount(value);
 				break;
 			}
 		}
 	}
 
 	private EdgeAction[] actions;
 	private int countTo, mod;
 	private boolean hasMod;
 	private boolean wrapDown;
 
 	protected void declare(boolean reload, SignChangeEvent event) {
 
 		actions = new EdgeAction[3];
 
 		/*
 		 * :COUNT <pulse>[ <mod>] <l> <s> <r>
 		 */
 
 		// LINE 1
 
 		String boundLine = this.getLines(event)[1].trim();
 		String[] boundArgs = boundLine.split(" ");
 
 		if (boundArgs.length > 0) {
 			try {
 				countTo = Integer.parseInt(boundArgs[0]);
 			} catch (Exception e) {
 				if (!reload) {
 					this.init("There was an error reading the pulse count you specified.");
 					event.setCancelled(true);
 				}
 				return;
 			}
 
 			if (boundArgs.length > 1) {
 				wrapDown = false;
 
 				try {
 					if (boundArgs[1].contains("*")) {
 						boundArgs[1] = boundArgs[1].replace("*", "");
 						wrapDown = true;
 					}
 
 					mod = Integer.parseInt(boundArgs[1]);
 					if (mod <= 0)
 						mod = 1;
 					hasMod = true;
 				} catch (Exception e) {
 					if (!reload) {
 						this.init("There was an error reading the mod you specified.");
 						event.setCancelled(true);
 					}
 					return;
 				}
 			} else {
 				hasMod = false;
 			}
 		} else {
 			if (!reload) {
 				this.init("You MUST specify a count to pulse at on the second line.");
 				event.setCancelled(true);
 			}
 			return;
 		}
 
 		// LINE 2
 
 		String actionLine = this.getLines(event)[2].trim();
 		String[] actionArgs = actionLine.split(" ");
 
 		if (actionArgs.length == 3) {
 			for (int i = 0; i < 3; i++) {
 				actions[i] = parseAction(actionArgs[i]);
 				if (actions[i] == null) {
 					if (!reload) {
 						this.init("Specified action " + (i + 1) + " (" + actionArgs[i] + ") was invalid.");
 						event.setCancelled(true);
 						return;
 					}
 				}
 			}
 		} else {
 			if (!reload)
 				this.init("No edge actions specified, defaulting to reset from the sides, increment from the front.");
 			actions[0] = new EdgeAction(ActionType.SET, 0);
 			actions[1] = new EdgeAction(ActionType.INCREMENT, 1);
 			actions[2] = new EdgeAction(ActionType.SET, 0);
 		}
 
 		// Format Lines
 
 		if (!reload) {
 			this.clearArgLines(event);
 			String line1 = Integer.toString(countTo);
 			if (hasMod)
 				line1 += " " + Integer.toString(mod);
 			if (wrapDown && hasMod)
 				line1 += "*";
 			this.setLine(1, line1, event);
 			this.setLine(2, actions[0] + " " + actions[1] + " " + actions[2], event);
 			setCount(0, event);
 		}
 
 		main.sgc.register(this, TriggerType.REDSTONE_CHANGE);
 		if (!reload) {
 			this.init("Count sign accepted.");
 		}
 	}
 
 	protected void setCount(int c) {
		lastAction = c - count;
 		setCount(c, null);
 	}
 
 	private void setCount(int c, SignChangeEvent e) {
 		this.count = c;
 		String line = "[" + Integer.toString(count) + "]";
 		try {
 			if (e == null) {
 				this.setLine(3, line);
 			} else {
 				this.setLine(3, line, e);
 			}
 		} catch (Exception error) {
 
 		}
 	}
 	
 	private void applyMod(int mod) {
 		this.count = this.count % mod;
 	}
 
 	private EdgeAction parseAction(String action) {
 
 		try {
 			action = action.trim();
 			if (action.length() >= 2) {
 				char operator = action.charAt(0);
 				int value = Integer.parseInt(action.substring(1));
 				ActionType at = ActionType.getActionType(operator);
 				if (at != null) {
 					return new EdgeAction(at, value);
 				}
 			}
 		} catch (Exception e) {
 			return null;
 		}
 		return null;
 	}
 
 }
