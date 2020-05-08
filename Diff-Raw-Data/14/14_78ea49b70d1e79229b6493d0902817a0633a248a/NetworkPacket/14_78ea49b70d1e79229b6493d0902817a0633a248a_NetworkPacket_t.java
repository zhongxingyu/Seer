 /*
 SubspaceMobile - A subspace/continuum client for mobile phones
 Copyright (C) 2010 Kingsley Masters
 Email: kshade2001 at users.sourceforge.net
 
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
 
 
 REVISIONS:
  */
 
 package com.subspace.network;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 
 import com.subspace.snrrrub.Checksum;
 
 /**
  * 
  * @author Kingsley
  */
 public class NetworkPacket {
 	// core
 
 	public static final String CHARSET = "ISO-8859-1";
 	public static final byte CORE = 0x00;
 	public static final byte CORE_CONNECTION = 0x01;
 	public static final byte CORE_CONNECTIONACK = 0x02;
 	public static final byte CORE_RELIABLE = 0x03;
 	public static final byte CORE_RELIABLEACK = 0x04;
 	public static final byte CORE_SYNC = 0x05;
 	public static final byte CORE_SYNCACK = 0x06;
 	public static final byte CORE_DISCONNECT = 0x07;
 	public static final byte CORE_CHUNK = 0x08;
 	public static final byte CORE_CHUNKEND = 0x09;
 	public static final byte CORE_STREAM = 0x0A;
 	public static final byte CORE_STREAMCANCEL = 0x0B;
 	public static final byte CORE_STREAMCANCELACK = 0x0C;
 	public static final byte CORE_CLUSTER = 0x0E;
 	public static final byte CORE_CTMCONNECTION = 0x10;
 	public static final byte CORE_CTMCONNECTIONACK = 0x11;
 	// directory
 	public static final byte DIRECTORY_REQUEST = 0x01;
 	public static final byte DIRECTORY_RESPONSE = 0x01;
 	// game c2s
 	public static final byte C2S_ARENALOGIN = 0x01;
 	public static final byte C2S_LEAVEARENA = 0x02;
 	public static final byte C2S_POSITION = 0x03;
 	public static final byte C2S_DEATH = 0x05;
 	public static final byte C2S_CHAT = 0x06;
 	public static final byte C2S_SPECTATEPLAYER = 0x08;
 	public static final byte C2S_PASSWORD = 0x09;
 	public static final byte C2S_MAPREQUEST = 0x0C;
 	public static final byte C2S_NEWSTXTREQUEST = 0x0D;
 	public static final byte C2S_SECURITYCHECKSUM = 0x1A;
 
 	// game s2c
 	public static final byte S2C_MY_UID = 0x01;
 	public static final byte S2C_NOW_IN_GAME = 0x02;
 	public static final byte S2C_PlayerEntering = 0x03;
 	public static final byte S2C_PlayerLeaving = 0x04;
 	public static final byte S2C_LargePosition = 0x05;
 	public static final byte S2C_PlayerDeath = 0x06;
 	public static final byte S2C_ChatMessage = 0x07;
 	public static final byte S2C_PlayerGotPrize = 0x08;
 	public static final byte S2C_PlayerScoreUpdate = 0x09;
 	public static final byte S2C_PASSWORDACK = 0x0A;
 	public static final byte S2C_SoccerGoal = 0x0B;
 	public static final byte S2C_PlayerVoice = 0x0C;
 	public static final byte S2C_PlayerChangedFreq = 0x0D;
 	public static final byte S2C_TurretCreate = 0x0E;
 	public static final byte S2C_ArenaSettings = 0x0F;
 	public static final byte S2C_FileTransfer = 0x10;
 	public static final byte S2C_FlagPosition = 0x12;
 	public static final byte S2C_FlagClaim = 0x13;
 	public static final byte S2C_FlagVictory = 0x14;
 	public static final byte S2C_DestroyTurret = 0x15;
 	public static final byte S2C_FlagDrop = 0x16;
 	public static final byte S2C_ChecksumRecv = 0x18;
 	public static final byte S2C_FileRequest = 0x19;
 	public static final byte S2C_ScoreReset = 0x1A;
 	public static final byte S2C_YourShipReset = 0x1B;
 	public static final byte S2C_ShipSpec = 0x1C;
 	public static final byte S2C_ShipAndFreqChange = 0x1D;
 	public static final byte S2C_YourBannerChanged = 0x1E;
 	public static final byte S2C_PlayerBannerChanged = 0x1F;
 	public static final byte S2C_CollectedPrize = 0x20;
 	public static final byte S2C_BrickDropped = 0x21;
 	public static final byte S2C_TurfFlagUpdate = 0x22;
 	public static final byte S2C_FlagRewardGranted = 0x23;
 	public static final byte S2C_SpeedGameEnded = 0x24;
 	public static final byte S2C_ToggleUFO = 0x25;
 	public static final byte S2C_KeepAlive = 0x27;
 	public static final byte S2C_SmallPosition = 0x28;
 	public static final byte S2C_MapInformation = 0x29;
 	public static final byte S2C_CompressedMapFile = 0x2A;
 	public static final byte S2C_SetYourKotHTime = 0x2B;
 	public static final byte S2C_KotHTimerReset = 0x2C;
 	public static final byte S2C_PowerBallPosition = 0x2E;
 	public static final byte S2C_ArenaListing = 0x2F;
 	public static final byte S2C_ZoneBannerAds = 0x30;
 	public static final byte S2C_PastLogin = 0x31;
 	public static final byte S2C_ChangeShipPosition = 0x32;
 
 	// core packets
 	static ByteBuffer CreateCore(int length, byte type) {
 		ByteBuffer bb = ByteBuffer.allocate(length + 2);
 		bb.order(ByteOrder.LITTLE_ENDIAN);
 		bb.put(CORE);
 		bb.put(type);
 		bb.mark();
 		return bb;
 	}
 
 	static ByteBuffer CreatePacket(int length, byte type) {
 		ByteBuffer bb = ByteBuffer.allocate(length + 1);
 		bb.order(ByteOrder.LITTLE_ENDIAN);
 		bb.put(type);
 		bb.mark();
 		return bb;
 	}
 
 	public static ByteBuffer CreateConnection(int key, short version) {
 		ByteBuffer bb = CreateCore(6, CORE_CONNECTION);
 		bb.putInt(key);
 		bb.putShort(version);
 		return bb;
 	}
 
 	public static ByteBuffer CreateDisconnect() {
 		return CreateCore(0, CORE_DISCONNECT);
 	}
 
 	public static ByteBuffer CreateReliable(int id, ByteBuffer data) {
 		data.rewind(); // move data pointer to start
 		ByteBuffer bb = CreateCore(data.limit() + 4, CORE_RELIABLE);
 		bb.putInt(id);
 		bb.put(data);
 		return bb;
 	}
 
 	public static ByteBuffer CreateReliableAck(int id) {
 		ByteBuffer bb = CreateCore(4, CORE_RELIABLEACK);
 		bb.putInt(id);
 		return bb;
 	}
 
 	public static ByteBuffer CreateSync(int packetInCount, int packetoutCount) {
 		ByteBuffer bb = CreateCore(14, CORE_SYNC);
 		bb.putInt(Util.GetTickCount());
 		bb.putInt(packetoutCount);
 		bb.putInt(packetInCount);
 		return bb;
 	}
 
 	public static ByteBuffer CreateSyncResponse(int syncTime) {
 		ByteBuffer bb = CreateCore(8, CORE_SYNCACK);
 		bb.putInt(syncTime);
 		bb.putInt(Util.GetTickCount());
 		return bb;
 	}
 
 	// directory server packet
 	public static ByteBuffer CreateZoneListRequest() {
 		ByteBuffer bb = CreatePacket(4, DIRECTORY_REQUEST);
 		bb.putShort((short) 0); // min players
 		bb.putShort((short) 0); // max players
 		return bb;
 	}
 
 	// game
 	// <editor-fold defaultstate="collapsed" desc="Packet Description">
 	/*
 	 * Offset Length Description 0 1 Type byte 1 1 Boolean: New user *1 2 32
 	 * Name 34 32 Password 66 4 Machine ident *2 70 1 ConnectType *3 71 2
 	 * Timezone bias 73 2 ? 75 2 Client version *4 77 4 ? memory checksum, Set
 	 * to = 444 81 4 ? memory checksum, Set to = 555 85 4 Permission ident 89 12
 	 * ?
 	 * 
 	 * 1 - 1 = New, 0 = Not New
 	 * 
 	 * 2 - Should be Drive Seriel of C (can be random for bots)
 	 * 
 	 * 3 - Type 0x00 is a safe bet.
 	 * 
 	 * 4 - 0x24 = Ctm, 0x86 = SS
 	 */
 	// </editor-fold>
 	public static ByteBuffer CreatePassword(boolean newUser, String name,
 			String password, int macId) {
 		try {
 			int permissionId = (Util.GetTickCount() ^ 0xAAAAAAAA) * 0x5f346d + 0x5abcdef;
 			// create buffer
 			ByteBuffer bb = CreatePacket(100, C2S_PASSWORD);
 			bb.put((byte) (newUser ? 0x01 : 0x00));
 			bb.position(2);
 			bb.put(name.getBytes(CHARSET));
 			bb.position(34);
 			bb.put(password.getBytes(CHARSET));
 			bb.putInt(66, macId);
 			bb.put(70, (byte) 0x00); // conection type
 			bb.putShort(71, (short) 240); // timezone bias (240=est)
 			bb.put(73, (byte) 0x00); // unknown
 			bb.putShort(75, (short) 0x86); // type = VIE
 			bb.putInt(77, 444); // memory checksum
 			bb.putInt(81, 555); // memory checksum
 			bb.putInt(85, permissionId); // permissionid
 			return bb;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	// <editor-fold defaultstate="collapsed" desc=" Packet Description ">
 	/*
 	 * Offset Length Descriptoin 0 1 Type Byte 0x01 1 1 Ship type 2 2 Allow
 	 * audio? 4 2 X resolution 6 2 Y resolution 8 2 Main arena number *1
 	 * 
 	 * 10 16 Arena name (Optional)
 	 * 
 	 * 1 - Set to 0xFFFF for Random Pub, or 0xFFFD for Specific Sub (Must
 	 * provide name).
 	 * 
 	 * //ship types
 	 * 
 	 * WARBIRD = 0 JAVELIN = 1 SPIDER = 2 LEVIATHAN = 3 TERRIER = 4 WEASEL = 5
 	 * LANCASTER = 6 SHARK = 7 SPECTATOR = 8
 	 */
 	// </editor-fold>
 	public static ByteBuffer CreateArenaLogin(byte shiptype, short xres,
 			short yres, String arena) {
 		try {
 			ByteBuffer bb;
 			if (arena != null) {
 				bb = CreatePacket(25, C2S_ARENALOGIN);
 			} else {
 				bb = CreatePacket(10, C2S_ARENALOGIN);
 			}
 			bb.put(1, shiptype);
 			bb.putShort(2, (short) 0); // disable sound
 			bb.putShort(4, xres);
 			bb.putShort(6, yres);
 			if (arena == null || arena.length() == 0) {
 				bb.putShort(8, (short) 0xFFFF);
 			} else {
 				bb.putShort(8, (short) 0xFFFD);
 			}
 			if (arena != null) {
 				bb.position(10);
 				bb.put(arena.getBytes(CHARSET));
 			}
 			return bb;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static ByteBuffer CreateMapRequest() {
		ByteBuffer bb = CreatePacket(0, C2S_MAPREQUEST);
 		return bb;
 	}
 
 	/*
 	 * 0x03 Position packet
 	 * 
 	 * Offset Length Description 0 1 Type Byte 0x03 1 1 Direction (0 ... 360) 2
 	 * 4 Timestamp 6 2 X Velocity 8 2 Y Pixels (0 ... 16384) 10 1 Checksum 11 1
 	 * Togglables *1 12 2 X Pixels (0 ... 16384) 14 2 Y Velocity 16 2 Bounty 18
 	 * 2 Energy 20 2 Weapon Info *2
 	 * 
 	 * 22 2 Energy *4 (Optional) 24 2 S2C Latency *4 (Optional) 26 2 Timer *4
 	 * (Optional) 28 4 Item info *3 *4 (Optional)
 	 * 
 	 * 1 - Togglables: Each value is one bit in a byte Bit 1 - Stealth Bit 2 -
 	 * Cloak Bit 4 - XRadar Bit 8 - Antiwarp Bit 16 - Flash (Play the cloak/warp
 	 * flash) Bit 32 - Safety (In safe) Bit 64 - UFO (Using UFO) Bit 128 - ?
 	 * 
 	 * 2 - Weapon info: Unsigned Integer Values or Booleans
 	 * 
 	 * Weapon type :5 bits *a1 Weapon level :2 bits Bouncing (Boolean) :1 bit
 	 * EMP (Boolean) :1 bit Is bomb (Boolean) :1 bit Shrapnel :5 bits Alternate
 	 * (Boolean) :1 bit *a2
 	 * 
 	 * 
 	 * a1 - Weapon types: 0x00 - None 0x01 - Bullet 0x02 - Bouncing bullet 0x03
 	 * - Bomb 0x04 - Proximity bomb 0x05 - Repel 0x06 - Decoy 0x07 - Burst 0x08
 	 * - Thor
 	 * 
 	 * a2 - (Bombs -> Mines; Bullets -> Multifire)
 	 * 
 	 * 3 - Item info: Unsigned Integer Values or Booleans
 	 * 
 	 * Shields (Boolean) :1 bit Super (Boolean) :1 bit Burst Count :4 bits Repel
 	 * Count :4 bits Thor Count :4 bits Brick Count :4 bits Decoy Count :4 bits
 	 * Rocket Count :4 bits Portal Count :4 bits ? Unknown :2 bits
 	 * 
 	 * 4 - These are supposed to be optional, but I'm not certain what dictates
 	 * if you should send them or not.
 	 */
 	public static ByteBuffer CreatePosition(short xPos, short yPos,
 			byte direction, short xV, short yV, short bounty, short energy,
 			short weapon, byte togglables) {
 
 		ByteBuffer bb = CreatePacket(22, C2S_POSITION);
 
 		byte checksum = 0;
 
 		bb.put(1, direction);
 		bb.putInt(2, Util.GetTickCount()); // timestamp
 		bb.putShort(6, xV);
 		bb.putShort(8, yPos);
 		bb.put(11, togglables);
 		bb.putShort(12, xPos);
 		bb.putShort(14, yV);
 		bb.putShort(16, bounty);
 		bb.putShort(18, energy);
 		bb.putShort(20, weapon);
 
 		// now add checksum
 
 		for (int i = 0; i < 22; i++) {
 			checksum ^= bb.get(i);
 		}
 
 		bb.put(10, checksum);// checksum
 
 		// TODO Auto-generated method stub
 		return bb;
 	}
 
 	/*
 	 * 0x1A Security checksum *1
 	 * 
 	 * Offset Length Description 0 1 Type Byte 0x1A 1 4 Weapon Count 5 4
 	 * Settings Checksum *2 9 4 Subspace.EXE Checksum 13 4 Map.LVL Checksum 17 4
 	 * S2CSlowTotal 21 4 S2CFastTotal 25 2 S2CSlowCurrent 27 2 S2CFastCurrent 29
 	 * 2 S2CRelOut (?Unsure?) 31 2 Ping 33 2 Ping Average 35 2 Ping Low 37 2
 	 * Ping High 39 1 Slow Frame Detected (Boolean)
 	 * 
 	 * 1 - The checksums are generated after a server-sent seed
 	 * 
 	 * 2 - Settings checksum is the checksum of the contents of the settings
 	 * packet
 	 */
 	public static ByteBuffer CreateSecurityChecksum(int settingsChecksum,
 			int exeChecksum, int lvlChecksum) {
 		ByteBuffer bb = CreatePacket(40, C2S_SECURITYCHECKSUM);
 		// generate checksums
 		bb.putInt(5, settingsChecksum);
 		bb.putInt(9, exeChecksum);
 		bb.putInt(13, lvlChecksum);
 
 		return bb;
 	}
 
 	public static ByteBuffer CreateNewsTxtRequest() {
		ByteBuffer bb = CreatePacket(0, C2S_NEWSTXTREQUEST);
 		return bb;
 	}
 
 	/*
 	 * Offset Length Description 0 1 Type Byte 0x06 1 1 Chat Type *1 2 1 Sound
 	 * Byte 3 2 Target Player's Player ID *2 5 * Text...
 	 * 
 	 * 1 - Chat types: 0x00 - Message in green text [*arena, *zone, ...] 0x01 -
 	 * Public macro
 	 * 
 	 * 0x02 - Public message
 	 * 
 	 * 0x03 - Team message [// or '] 0x04 - Player to all members of another
 	 * team ["Whatever] 0x05 - Private message [/Whatever] 0x06 - Red warning
 	 * message [MODERATOR WARNING: Whatever -Whoever] 0x07 - Remote private
 	 * message *3 [(Whoever)> Whatever] 0x08 - Red server errors, without a name
 	 * tag (S2C only) 0x09 - Channel message *3 [X;Whatever]
 	 * 
 	 * 2 - Target player's player ID is 0x00 for the Following Chat Types: 0x00,
 	 * 0x01, 0x02, 0x03, 0x06, 0x07, 0x08 0x09. It is PlayerID for 0x05 and 0x04
 	 * 
 	 * 3 - These are formatted much as youd expect. To send a Remote Message,
 	 * you include the name of the person you are sending the message to in the
 	 * Text of the message like so: ":Name:Message" and with Channel Messages
 	 * you include the Number of the Channel you are sending to, like so:
 	 * "#;Message".
 	 */
 
 	public static ByteBuffer CreateChat(byte chatType, byte sound,
 			short targetPlayer, String text) {
 		try {
 			ByteBuffer bb = CreatePacket(text.length() + 5, C2S_CHAT);
 			bb.put(1, chatType);
 			bb.put(2, sound);
 			bb.putShort(3, targetPlayer);
 			bb.position(5);
 			bb.put(text.getBytes(CHARSET));
 			return bb;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 }
