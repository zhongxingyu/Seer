 package pl.radical.open.gg.packet.out;
 
 import pl.radical.open.gg.ILocalStatus;
 import pl.radical.open.gg.packet.GGOutgoingPackage;
 import pl.radical.open.gg.packet.OutgoingPacket;
 import pl.radical.open.gg.packet.dicts.GGStatuses;
 import pl.radical.open.gg.packet.dicts.GGVersion;
 import pl.radical.open.gg.utils.GGConversion;
 import pl.radical.open.gg.utils.GGUtils;
 
 import java.util.Arrays;
 
 import org.apache.commons.collections.primitives.ArrayByteList;
 import org.apache.commons.collections.primitives.ByteList;
 
 /**
  * @author <a href="mailto:mati@sz.home.pl">Mateusz Szczap</a>
  * @deprecated This implementation is deprecated and was replaced by {@link GGLogin80} in current protocol
  *             implementation
  */
 @Deprecated
 @OutgoingPacket(type = 0x0015, label = "GG_LOGIN60")
 public class GGLogin60 implements GGOutgoingPackage {
 
 	public static final int GG_LOGIN60 = 0x0015;
 
 	/** Gadu-Gadu number that will be used during logging */
 	private int uin = -1;
 
 	/** Password that will be used during logging */
 	private char[] password = null;
 
 	/** Computed login hash based on seed retreived from Gadu-Gadu server */
 	private int loginHash = -1;
 
 	/** Initial status that will be set after logging */
 	private int status = GGStatuses.GG_STATUS_AVAIL;
 
 	/** Local IP */
 	private byte[] localIP = new byte[] {
 			(byte) 0, (byte) 0, (byte) 0, (byte) 0
 	};
 
 	/** Local port that we are listening on */
 	private int localPort = 1550;
 
 	/** ExternalIP */
 	private byte[] externalIP = new byte[] {
 			(byte) 0, (byte) 0, (byte) 0, (byte) 0
 	};
 
 	/** External port */
 	private int externalPort = 1550;
 
 	/** size of image in kilobytes */
 	private byte imageSize = 64;
 
 	/** Description that will be set after successfuly logging */
 	private String description = null;
 
 	/** Version of the client */
 	// FIXME This need to be updated - not being used at all
 	private final int version = GGVersion.VERSION_60_1_build_133.getCode();
 
 	/** Return time */
 	private int time = -1;
 
 	public GGLogin60(final int uin, final char[] password, final int seed) {
 		if (uin < 0) {
 			throw new IllegalArgumentException("uin cannot be less than 0");
 		}
 		if (password == null) {
 			throw new IllegalArgumentException("password cannot be null");
 		}
 		this.uin = uin;
 		this.password = Arrays.copyOf(password, password.length);
 		loginHash = GGUtils.getLoginHash(password, seed);
 	}
 
 	public void setStatus(final ILocalStatus localStatus) {
 		if (localStatus == null) {
 			throw new IllegalArgumentException("localStatus cannot be null");
 		}
 		status = GGConversion.getProtocolStatus(localStatus, localStatus.isFriendsOnly(), false);
 		if (localStatus.isDescriptionSet()) {
 			description = localStatus.getDescription();
 		}
 		if (localStatus.isReturnDateSet()) {
 			time = GGUtils.millisToSeconds(localStatus.getReturnDate().getTime());
 		}
 	}
 
 	public int getUin() {
 		return uin;
 	}
 
 	public char[] getPassword() {
 		return password;
 	}
 
 	public void setLocalIP(final byte[] localIP) {
 		if (localIP == null) {
 			throw new IllegalArgumentException("localIP cannot be null");
 		}
 		if (localIP.length != 4) {
 			throw new IllegalArgumentException("localIp table has to have 4 entries");
 		}
		this.localIP = localIP;
 	}
 
 	public byte[] getLocalIP() {
 		return localIP;
 	}
 
 	public void setLocalPort(final int port) {
 		if (port < 0) {
 			throw new IllegalArgumentException("port cannot be null");
 		}
 		localPort = port;
 	}
 
 	public int getLocalPort() {
 		return localPort;
 	}
 
 	public void setExternalIP(final byte[] externalIP) {
 		if (externalIP == null) {
 			throw new IllegalArgumentException("externalIP cannot be null");
 		}
 		if (externalIP.length != 4) {
 			throw new IllegalArgumentException("externalIP table has to have 4 entries");
 		}
 		this.externalIP = Arrays.copyOf(externalIP, externalIP.length);
 	}
 
 	public void setExternalPort(final int externalPort) {
 		if (externalPort < 0) {
 			throw new IllegalArgumentException("port cannot be null");
 		}
 		this.externalPort = externalPort;
 	}
 
 	public void setImageSize(final byte imageSize) {
 		if (imageSize < 0) {
 			throw new IllegalArgumentException("imageSize cannot be less than 0");
 		}
 		this.imageSize = imageSize;
 	}
 
 	/**
 	 * @see pl.radical.open.gg.packet.GGOutgoingPackage#getPacketType()
 	 */
 	public int getPacketType() {
 		return GG_LOGIN60;
 	}
 
 	/**
 	 * @see pl.radical.open.gg.packet.GGOutgoingPackage#getLength()
 	 */
 	public int getLength() {
 		int length = 4 + 4 + 4 + 4 + 1 + 4 + 2 + 4 + 2 + 1 + 1;
 		if (description != null) {
 			length += description.length() + 1;
 			if (time != -1) {
 				length += 4;
 			}
 		}
 		return length;
 	}
 
 	/**
 	 * @see pl.radical.open.gg.packet.GGOutgoingPackage#getContents()
 	 */
 	public byte[] getContents() {
 		final ByteList byteList = new ArrayByteList(getLength());
 
 		byteList.add((byte) (uin & 0xFF));
 		byteList.add((byte) (uin >> 8 & 0xFF));
 		byteList.add((byte) (uin >> 16 & 0xFF));
 		byteList.add((byte) (uin >> 24 & 0xFF));
 
 		byteList.add((byte) (loginHash & 0xFF));
 		byteList.add((byte) (loginHash >> 8 & 0xFF));
 		byteList.add((byte) (loginHash >> 16 & 0xFF));
 		byteList.add((byte) (loginHash >> 24 & 0xFF));
 
 		byteList.add((byte) (status & 0xFF));
 		byteList.add((byte) (status >> 8 & 0xFF));
 		byteList.add((byte) (status >> 16 & 0xFF));
 		byteList.add((byte) (status >> 24 & 0xFF));
 
 		byteList.add((byte) (version & 0xFF));
 		byteList.add((byte) (version >> 8 & 0xFF));
 		byteList.add((byte) (version >> 16 & 0xFF));
 		byteList.add((byte) (version >> 24 & 0xFF));
 
 		byteList.add((byte) 0x00);
 
 		byteList.add(localIP[0]);
 		byteList.add(localIP[1]);
 		byteList.add(localIP[2]);
 		byteList.add(localIP[3]);
 
 		byteList.add((byte) (localPort & 0xFF));
 		byteList.add((byte) (localPort >> 8 & 0xFF));
 
 		byteList.add(externalIP[0]);
 		byteList.add(externalIP[1]);
 		byteList.add(externalIP[2]);
 		byteList.add(externalIP[3]);
 
 		byteList.add((byte) (externalPort & 0xFF));
 		byteList.add((byte) (externalPort >> 8 & 0xFF));
 
 		byteList.add(imageSize);
 		byteList.add((byte) 0xBE);
 
 		if (description != null) {
 			final byte[] descBytes = description.getBytes();
 			for (final byte descByte : descBytes) {
 				byteList.add(descByte);
 				if (time != -1) {
 					byteList.add((byte) (time >> 24 & 0xFF));
 					byteList.add((byte) (time >> 16 & 0xFF));
 					byteList.add((byte) (time >> 8 & 0xFF));
 					byteList.add((byte) (time & 0xFF));
 				}
 			}
 		}
 
 		return byteList.toArray();
 	}
 
 }
