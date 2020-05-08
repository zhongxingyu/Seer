 /*
  * This file is part of VanillaClassic.
  *
  * Copyright (c) 2012, Greatman <http://www.github.com/greatman/>
  * VanillaClassic is licensed under the SpoutDev License Version 1.
  *
  * VanillaClassic is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * In addition, 180 days after any changes are published, you can use the
  * software, incorporating those changes, under the terms of the MIT license,
  * as described in the SpoutDev License Version 1.
  *
  * VanillaClassic is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License,
  * the MIT license and the SpoutDev License Version 1 along with this program.
  * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
  * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
  * including the MIT license.
  */
 package com.greatmancode.vanillaclassic.protocol;
 
 
 import org.spout.api.protocol.CodecLookupService;
 
 import com.greatmancode.vanillaclassic.protocol.codec.DespawnPlayerCodec;
 import com.greatmancode.vanillaclassic.protocol.codec.DisconnectPlayerCodec;
 import com.greatmancode.vanillaclassic.protocol.codec.IdentificationCodec;
 import com.greatmancode.vanillaclassic.protocol.codec.LevelDataChunkCodec;
 import com.greatmancode.vanillaclassic.protocol.codec.LevelFinalizeCodec;
 import com.greatmancode.vanillaclassic.protocol.codec.LevelInitializeCodec;
 import com.greatmancode.vanillaclassic.protocol.codec.PingCodec;
 import com.greatmancode.vanillaclassic.protocol.codec.PlayerMessageCodec;
 import com.greatmancode.vanillaclassic.protocol.codec.PlayerPositionCodec;
 import com.greatmancode.vanillaclassic.protocol.codec.SetBlockClientCodec;
 import com.greatmancode.vanillaclassic.protocol.codec.SetBlockServerCodec;
 import com.greatmancode.vanillaclassic.protocol.codec.SpawnPlayerCodec;
 import com.greatmancode.vanillaclassic.protocol.codec.UpdateUserTypeCodec;
 
 public class VanillaClassicCodecLookupService extends CodecLookupService {
 	public VanillaClassicCodecLookupService() {
		super(512);
 		try {
 			/* 0x00 */
 			bind(IdentificationCodec.class);
 			/* 0x01 */
 			bind(PingCodec.class);
 			/* 0x02 */
 			bind(LevelInitializeCodec.class);
 			/* 0x03 */
 			bind(LevelDataChunkCodec.class);
 			/* 0x04 */
 			bind(LevelFinalizeCodec.class);
 			/* 0x05 */
 			bind(SetBlockClientCodec.class);
 			/* 0x06 */
 			bind(SetBlockServerCodec.class);
 			/* 0x07 */
 			bind(SpawnPlayerCodec.class);
 			/* 0x08 */
 			bind(PlayerPositionCodec.class);
 			/* 0x09 */
 			// bind(PositionOrientationCodec.class); Not needed apparently
 			/* 0x0a */
 			// bind(PositionUpdateCodec.class); Not needed apparently
 			/* 0x0b */
 			// bind(OrientationUpdateCodec.class); Not needed apparently
 			/* 0x0c */
 			bind(DespawnPlayerCodec.class);
 			/* 0x0d */
 			bind(PlayerMessageCodec.class);
 			/* 0x0e */
 			bind(DisconnectPlayerCodec.class);
 			/* 0x0f */
 			bind(UpdateUserTypeCodec.class);
 		} catch (Exception ex) {
 			throw new ExceptionInInitializerError(ex);
 		}
 	}
 }
