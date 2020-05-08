 /*
  * PROJECT: Phybots at http://phybots.com/
  * ----------------------------------------------------------------------------
  *
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is Phybots.
  *
  * The Initial Developer of the Original Code is Jun KATO.
  * Portions created by the Initial Developer are
  * Copyright (C) 2009-2013 Jun Kato. All Rights Reserved.
  *
  * Contributor(s): Jun Kato
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either of the GNU General Public License Version 2 or later (the "GPL"),
  * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  */
 package jp.digitalmuseum.connector;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import lejos.pc.comm.NXTCommFantom;
 
 
 public class FantomConnector extends ConnectorAbstractImpl {
 	private static final long serialVersionUID = 2928595120975023381L;
 	final public static String CON_PREFIX = "fantom:";
 	private transient NXTCommFantom fantom;
 	private String portName;
 
 	public FantomConnector(String con) {
 		fantom = new NXTCommFantom();
 		parseConnectionString(con);
 	}
 
 	public static String[] queryIdentifiers() {
 		NXTCommFantom fantom = new NXTCommFantom();
 		String[] identifiers = fantom.find();
 		if (identifiers != null)
 			for (int i = 0; i < identifiers.length; i ++) {
 				identifiers[i] = CON_PREFIX + identifiers[i];
 			}
 		return identifiers;
 	}
 
 	private void parseConnectionString(String con) {
 
 		// Remove prefix.
 		if (con.toLowerCase().startsWith(CON_PREFIX)) {
 			portName = con.substring(CON_PREFIX.length());
 		} else {
 			portName = con;
 		}
 	}
 
 	@Override
 	public String getConnectionString() {
 		return CON_PREFIX + portName;
 	}
 
 	@Override
 	public boolean connect() {
 		if (fantom.isConnected()) {
 			return true;
 		}
		if (fantom.open(portName)) {
			setInputStream(new FantomInputStream());
			setOutputStream(new FantomOutputStream());
			return true;
		}
		return false;
 	}
 
 	@Override
 	public boolean isConnected() {
 		return fantom.isConnected();
 	}
 
 	public void disconnect() {
 		super.disconnect();
 		if (fantom.isConnected()) {
 			fantom.close();
 		}
 	}
 
 	public class FantomInputStream extends InputStream {
 		@Override
 		public int read() throws IOException {
 			byte[] data = new byte[1];
 			if (fantom.readData(data, 0, 1) < 0) {
 				return -1;
 			}
 			else return data[0];
 		}
 		@Override
 		public int read(byte[] data, int offset, int len) {
 			return fantom.readData(data, offset, len);
 		}
 		@Override
 		public void close() throws IOException {
 			FantomConnector.this.disconnect();
 		}
 	}
 
 	public class FantomOutputStream extends OutputStream {
 		@Override
 		public void write(int b) throws IOException {
 			write(new byte[] { (byte) b }, 0, 1);
 			flush();
 		}
 		@Override
 		public void write(byte[] data, int offset, int len) throws IOException {
 			fantom.sendData(data, offset, len);
 			flush();
 		}
 		@Override
 		public void close() throws IOException {
 			FantomConnector.this.disconnect();
 		}
 	}
 }
