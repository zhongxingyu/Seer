 /*
  * Copyright 2013 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.websocket;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.Random;
 
 /**
  * @since 0.1.0
  * @author xeraph
  * 
  */
 public class WebSocketFrame {
 	private static Charset utf8 = Charset.forName("utf-8");
 
 	public enum Opcode {
 		CONTINUATION(0), TEXT(1), BINARY(2), CLOSE(8), PING(9), PONG(10);
 
 		private int code;
 
 		Opcode(int code) {
 			this.code = code;
 		}
 
 		public int getCode() {
 			return code;
 		}
 	}
 
 	private boolean fin;
 	private Opcode opcode;
 	private boolean mask;
 	private byte[] maskingKey;
 	private byte[] payload;
 
 	public WebSocketFrame(String payload) {
 		this(payload.getBytes(utf8));
 	}
 
 	public WebSocketFrame(byte[] payload) {
 		this.fin = true;
 		this.mask = true;
 		this.opcode = Opcode.TEXT;
 		this.maskingKey = new byte[4];
 		new Random().nextBytes(maskingKey);
 		this.payload = payload;
 	}
 
 	public boolean isFin() {
 		return fin;
 	}
 
 	public void setFin(boolean fin) {
 		this.fin = fin;
 	}
 
 	public Opcode getOpcode() {
 		return opcode;
 	}
 
 	public void setOpcode(Opcode opcode) {
 		this.opcode = opcode;
 	}
 
 	public boolean isMask() {
 		return mask;
 	}
 
 	public void setMask(boolean mask) {
 		this.mask = mask;
 	}
 
 	public byte[] getMaskingKey() {
 		return maskingKey;
 	}
 
 	public void setMaskingKey(byte[] maskingKey) {
 		this.maskingKey = maskingKey;
 	}
 
 	public byte[] getPayload() {
 		return payload;
 	}
 
 	public void setPayload(byte[] payload) {
 		this.payload = payload;
 	}
 
 	public byte[] encode() {
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 
 		// fin(1) | rsv(3) | opcode(4)
 		int b1 = opcode.getCode();
 		if (fin)
 			b1 |= 0x80;
 
 		bos.write(b1);
 
 		// mask(1) | payload len(7) | extended payload len (16 or 64)
 		int len = payload.length;
 
 		if (len <= 125) {
 			bos.write(maskBit(len));
 		} else if (len <= 65535) {
 			bos.write(maskBit(126));
 			bos.write(len >> 8);
 			bos.write(len);
 		} else {
 			bos.write(maskBit(127));
			for (int i = 7; i >= 0; i--)
				bos.write(len >> (i * 8));
 		}
 
 		try {
 			if (mask) {
 				bos.write(maskingKey);
 
 				// encode payload
				byte[] masked = new byte[payload.length];
 				for (int i = 0; i < payload.length; i++) {
 					masked[i] = (byte) (payload[i] ^ maskingKey[i % 4]);
 				}
 				bos.write(masked);
 			} else {
 				bos.write(payload);
 			}
 		} catch (IOException e) {
 			// impossible
 		}
 
 		return bos.toByteArray();
 	}
 
 	private int maskBit(int b) {
 		return mask ? b | 0x80 : b;
 	}
 }
