 package net.brainvitamins.kerberos;
 
 /*
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import javax.security.auth.callback.Callback;
 
 /**
  * A data type the encapsulates the prompts from the native library and a
  * reference to the Java intermediary between the UI and the native code.
  * 
  * The Callback type is used because it's built-in: the callback mechanism
  * depends on Android Messages, which don't play nicely with the classic
  * callback mechanism.
  */
 public class KerberosCallbackArray {
 
 	Callback[] callbacks;
 
 	public Callback[] getCallbacks() {
 		return callbacks;
 	}
 
 	AuthenticationDialogHandler source;
 
 	public AuthenticationDialogHandler getSource() {
 		return source;
 	}
 
	public KerberosCallbackArray(Callback[] callbacks, AuthenticationDialogHandler source) {
 		super();
 		this.callbacks = callbacks;
 		this.source = source;
 	}
 }
