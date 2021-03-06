 /*
  * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  * SVN: $Id: CallbackOnAwayStateOther.java 719 2007-03-28 13:20:56Z ShaneMcC $
  */
 
 package uk.org.ownage.dmdirc.parser.callbacks;
 
 import uk.org.ownage.dmdirc.parser.IRCParser;
 import uk.org.ownage.dmdirc.parser.ClientInfo;
 import uk.org.ownage.dmdirc.parser.ParserError;
 import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IAwayStateOther;
 
 /**
  * Callback to all objects implementing the IAwayStateOther Interface.
  */
 public final class CallbackOnAwayStateOther extends CallbackObjectSpecific {
 	
 	/**
 	 * Create a new instance of the Callback Object.
 	 *
 	 * @param parser IRCParser That owns this callback
 	 * @param manager CallbackManager that is in charge of this callback
 	 */
 	public CallbackOnAwayStateOther(final IRCParser parser, final CallbackManager manager) { super(parser, manager); }
 	
 	/**
 	 * Callback to all objects implementing the IAwayStateOther Interface.
 	 *
 	 * @param client Client this is for
 	 * @param state Away State (true if away, false if here)
 	 * @see IAwayStateOther
 	 * @return true if a callback was called, else false
 	 */
 	public boolean call(ClientInfo client, boolean state) {
 		boolean bResult = false;
 		IAwayStateOther eMethod = null;
 		for (int i = 0; i < callbackInfo.size(); i++) {
 			eMethod = (IAwayStateOther) callbackInfo.get(i);
			if (!this.isValidUser(eMethod, sHost)) { continue; }
 			try {
 				eMethod.onAwayStateOther(myParser, client, state);
 			} catch (Exception e) {
 				final ParserError ei = new ParserError(ParserError.ERROR_ERROR, "Exception in onAwayStateOther");
 				ei.setException(e);
 				callErrorInfo(ei);
 			}
 			bResult = true;
 		}
 		return bResult;
 	}	
 	
 	/**
 	 * Get SVN Version information.
 	 *
 	 * @return SVN Version String
 	 */
 	public static String getSvnInfo() { return "$Id: CallbackOnAwayStateOther.java 719 2007-03-28 13:20:56Z ShaneMcC $"; }	
 }
