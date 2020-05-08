 package utils;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 public class Message {
 
 	public static final byte TYPE_KEEP_ALIVE = -1;
 	public static final byte TYPE_CHOKE = 0;
 	public static final byte TYPE_UNCHOKE = 1;
 	public static final byte TYPE_INTERESTED = 2;
 	public static final byte TYPE_NOT_INTERESTED = 3;
 	public static final byte TYPE_HAVE = 4;
 	public static final byte TYPE_BITFIELD = 5;
 	public static final byte TYPE_REQUEST = 6;
 	public static final byte TYPE_PIECE = 7;
 
 	public static final String[] MESSAGE_NAMES = { "Choke", "Unchoke",
 			"Interested", "Not Interested", "Have", "Bitfield", "Request",
 			"Piece" };
 
 	public static final Message KEEP_ALIVE = new Message(TYPE_KEEP_ALIVE, 0);
 	public static final Message CHOKE = new Message(TYPE_CHOKE, 1);
 	public static final Message UNCHOKE = new Message(TYPE_UNCHOKE, 1);
 	public static final Message INTERESTED = new Message(TYPE_INTERESTED, 1);
 	public static final Message NOT_INTERESTED = new Message(
 			TYPE_NOT_INTERESTED, 1);
 
 	protected final byte id;
 	protected final int length;
 
 	public static Message decode(final InputStream in) throws IOException {
 		DataInputStream din = new DataInputStream(in);
 
 		int length = din.readInt();
 
 		if (length == 0) {
 			return KEEP_ALIVE;
 		}
 
 		byte id = din.readByte();
 		switch (id) {
 		case TYPE_CHOKE:
 			return CHOKE;
 		case TYPE_UNCHOKE:
 			return UNCHOKE;
 		case TYPE_INTERESTED:
 			return INTERESTED;
 		case TYPE_NOT_INTERESTED:
 			return NOT_INTERESTED;
 		case TYPE_HAVE: {
 			int pieceIndex = din.readInt();
 			return new HaveMessage(pieceIndex);
 		}
 		case TYPE_PIECE: {
 			int pieceIndex = din.readInt();
 			int begin = din.readInt();
 			byte[] data = new byte[length - 9];
 			din.readFully(data);
 			return new PieceMessage(pieceIndex, begin, data);
 		}
 		case TYPE_REQUEST: {
 			int pieceIndex = din.readInt();
 			int begin = din.readInt();
 			int blockLength = din.readInt();
 			return new RequestMessage(pieceIndex, begin, blockLength);
 		}
 		case TYPE_BITFIELD: {
 			byte[] data;
 			int bitLength = Manager.getNumPieces();
 			if (bitLength % 8 == 0) {
 				data = new byte[bitLength / 8];
 			} else {
 				data = new byte[bitLength / 8 + 1];
 			}
 			din.readFully(data);
 			return new BitfieldMessage(data);
 		}
 		default:
 			System.err.println("What, no id?");
 			throw new IOException("Unknown message id: " + id);
 		}
 	}
 
 	public static void encode(final OutputStream out, final Message message)
 			throws IOException {
 		DataOutputStream dout = new DataOutputStream(out);
 		if (message.getId() == TYPE_KEEP_ALIVE) {
 			dout.writeInt(message.getLength());
 
 		} else {
 
 			switch (message.getId()) {
 			case TYPE_NOT_INTERESTED:
 				dout.writeInt(NOT_INTERESTED.getLength());
 				dout.writeByte(NOT_INTERESTED.getId());
 				break;
 			case TYPE_INTERESTED:
 				dout.writeInt(INTERESTED.getLength());
 				dout.writeByte(INTERESTED.getId());
 				break;
 			case TYPE_UNCHOKE:
 				dout.writeInt(UNCHOKE.getLength());
 				dout.writeByte(UNCHOKE.getId());
 				break;
 			case TYPE_CHOKE:
 				dout.writeInt(CHOKE.getLength());
 				dout.writeByte(CHOKE.getId());
 				break;
 			case TYPE_HAVE:
 				if (!(message instanceof HaveMessage)) {
 					throw new IllegalArgumentException(
 							"How did you make a raw message???");
 				}
 				HaveMessage mess = (HaveMessage) message;
 				dout.writeInt(mess.getLength());
 				dout.writeByte(mess.getId());
 				dout.writeInt(mess.getPieceIndex());
 				break;
 			case TYPE_PIECE:
 				if (!(message instanceof PieceMessage)) {
 					throw new IllegalArgumentException(
 							"How did you make a raw message???");
 				}
 				PieceMessage msg = (PieceMessage) message;
 				dout.writeInt(message.getLength());
 				dout.writeByte(message.getId());
 				dout.writeInt(msg.getPieceIndex());
 				dout.writeInt(msg.getBegin());
 				dout.write(msg.getData());
 				break;
 			case TYPE_REQUEST:
 				if (!(message instanceof RequestMessage)) {
 					throw new IllegalArgumentException(
 							"How did you make a raw message??");
 				}
 				RequestMessage temp = (RequestMessage) message;
 				dout.writeInt(temp.getLength());
 				dout.writeByte(temp.getId());
 				dout.writeInt(temp.getPieceIndex());
 				dout.writeInt(temp.getBegin());
 				dout.writeInt(temp.getBlockLength());
 				break;
 			case TYPE_BITFIELD:
 				if (!(message instanceof BitfieldMessage)) {
 					throw new IllegalArgumentException(
 							"How did you make a raw message??");
 				}
 				BitfieldMessage tmp = (BitfieldMessage) message;
 				dout.writeInt(tmp.getLength());
 				dout.writeByte(tmp.getId());
 				dout.write(tmp.getData(), 0, tmp.getData().length);
 				break;
 			}
 
 		}
 		dout.flush();
 	}
 
 	protected Message(final byte id, final int length) {
 		this.id = id;
 		this.length = length;
 	}
 
 	@Override
 	public String toString() {
 		if (this.id == TYPE_KEEP_ALIVE) {
 			return "Keep-Alive";
 		}
 		return MESSAGE_NAMES[this.id];
 	}
 
 	public byte getId() {
 		return id;
 	}
 
 	public int getLength() {
 		return length;
 	}
 
 	public static final class HaveMessage extends Message {
 
 		private final int pieceIndex;
 
 		public int getPieceIndex() {
 			return pieceIndex;
 		}
 
 		public HaveMessage(final int pieceIndex) {
 			super(TYPE_HAVE, 5);
 			this.pieceIndex = pieceIndex;
 		}
 	}
 
 	public static final class PieceMessage extends Message {
 		private final int pieceIndex;
 		private final int begin;
 		private final byte[] data;
 
 		public PieceMessage(final int pieceIndex, final int begin,
 				final byte[] data) {
 			super(TYPE_PIECE, data.length + 9);
 			this.pieceIndex = pieceIndex;
 			this.begin = begin;
 			this.data = data;
 		}
 
 		public int getPieceIndex() {
 			return pieceIndex;
 		}
 
 		public int getBegin() {
 			return begin;
 		}
 
 		public byte[] getData() {
 			return data;
 		}
 
 		public int getBlockLength() {
 			return data.length;
 		}
 	}
 
 	public static final class RequestMessage extends Message {
 		private final int pieceIndex;
 		private final int begin;
 		private final int blockLength;
 
 		public RequestMessage(final int pieceIndex, final int begin,
 				final int blockLength) {
 			super(TYPE_REQUEST, 13);
 			this.pieceIndex = pieceIndex;
 			this.begin = begin;
 			this.blockLength = blockLength;
 		}
 
 		public int getPieceIndex() {
 			return pieceIndex;
 		}
 
 		public int getBegin() {
 			return begin;
 		}
 
 		public int getBlockLength() {
 			return blockLength;
 		}
 	}
 
 	public static final class BitfieldMessage extends Message {
 		private final byte[] data;
 
 		public BitfieldMessage(final byte[] data) {
			super(TYPE_BITFIELD, 1 + data.length);
 			this.data = data;
 		}
 
 		public byte[] getData() {
 			return data;
 		}
 	}
 
 	public static final class InterestedMessage extends Message {
 		public InterestedMessage() {
 			super(TYPE_INTERESTED, 1);
 		}
 	}
 
 	public static final class UninterestedMessage extends Message {
 		public UninterestedMessage() {
 			super(TYPE_NOT_INTERESTED, 1);
 		}
 	}
 
 }
