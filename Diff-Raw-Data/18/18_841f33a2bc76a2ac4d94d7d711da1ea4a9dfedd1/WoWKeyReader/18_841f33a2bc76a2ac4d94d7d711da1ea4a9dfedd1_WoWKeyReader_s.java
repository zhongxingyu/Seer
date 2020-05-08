 /*******************************************************************************
  * Copyright (c) 2013 MinimaJack
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  *     MinimaJack - initial API and implementation
  ******************************************************************************/
 package org.jmangos.sniffer.jna;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import com.sun.jna.Memory;
 import com.sun.jna.Native;
 import com.sun.jna.Platform;
 import com.sun.jna.Pointer;
 import com.sun.jna.platform.win32.WinNT.HANDLE;
 import com.sun.jna.win32.W32APIOptions;
 
 /**
  * @author MinimaJack
  * 
  */
 @Component
 public class WoWKeyReader {
 	static final Logger log = LoggerFactory.getLogger(WoWKeyReader.class);
 	static Tools INSTANCE;
 
 	private byte[] authKey;
 	@Value("${sniffer.connection}")
 	private int connectionOffset;
 	@Value("${sniffer.key}")
 	private int keyOffset;
 
 	public WoWKeyReader() {
 		try {
 			if (Platform.isWindows()) {
 
 				if (Platform.is64Bit()) {
 					INSTANCE = (Tools) Native.loadLibrary("win32-amd64",
 							Tools.class, W32APIOptions.UNICODE_OPTIONS);
 				} else {
 					INSTANCE = (Tools) Native.loadLibrary("win32-x86",
 							Tools.class, W32APIOptions.UNICODE_OPTIONS);
 				}
 
 			} else {
 				String os = System.getProperty("os.name");
 				throw new UnsupportedOperationException("No support for " + os);
 			}
 		} catch (Exception e) {
 			log.warn("Can't instantiate native libs.");
 		}
 	};
 
 	public WoWKeyReader(final int connectionOffset, final int keyOffset) {
 		this.connectionOffset = connectionOffset;
 		this.keyOffset = keyOffset;
 	}
 
 	/**
 	 * @param args
 	 * @return
 	 */
 	synchronized public byte[] getKey() {
 		if (this.authKey == null) {
 			INSTANCE.EnableDebugPriv();
 			final HANDLE wow = com.sun.jna.platform.win32.Kernel32.INSTANCE
 					.OpenProcess(Kernel32.PROCESS_QUERY_INFORMATION
 							| Kernel32.PROCESS_VM_READ, true,
							INSTANCE.GetTargetProcessId("wow.exe"));

 			if (wow != null) {
 				final Memory connection = new Memory(4);
 				int bytes = INSTANCE.ReadMemory(wow, new Pointer(
						this.connectionOffset), connection, (int) connection
 						.size());
 				final Memory key = new Memory(40);
 				final Pointer array = new Pointer(connection.getInt(0)
 						+ this.keyOffset);
 				bytes = INSTANCE.ReadMemory(wow, array, key, (int) key.size());
 				boolean zeroKey = false;
 				this.authKey = new byte[40];
 				for (int i = 0; i < bytes; i++) {
 					zeroKey |= (key.getByte(i) != 0);
 					this.authKey[i] = key.getByte(i);
 				}
 				com.sun.jna.platform.win32.Kernel32.INSTANCE.CloseHandle(wow);
 				if (!zeroKey) {
 					System.out.println("return zero key");
 					this.authKey = null;
 				}
 				return this.authKey;
 			} else {
 				return null;
 			}
 		} else {
 			return this.authKey;
 		}
 	}
 
 }
