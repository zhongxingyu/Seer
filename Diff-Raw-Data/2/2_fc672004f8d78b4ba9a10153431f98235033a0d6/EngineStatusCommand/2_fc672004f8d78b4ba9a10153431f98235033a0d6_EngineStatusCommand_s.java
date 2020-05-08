 package de.ptb.epics.eve.ecp1.commands;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import de.ptb.epics.eve.ecp1.intern.exceptions.AbstractRestoreECP1CommandException;
 import de.ptb.epics.eve.ecp1.intern.exceptions.BrokenByteArrayException;
 import de.ptb.epics.eve.ecp1.intern.exceptions.WrongStartTagException;
 import de.ptb.epics.eve.ecp1.intern.exceptions.WrongTypeIdException;
 import de.ptb.epics.eve.ecp1.intern.exceptions.WrongVersionException;
 import de.ptb.epics.eve.ecp1.types.EngineStatus;
 
 public class EngineStatusCommand implements IECP1Command {
 
 	public static final char COMMAND_TYPE_ID = 0x0102;
 
 	private int gerenalTimeStamp;
 	private int nanoseconds;
 	private int repeatCount;
 	private EngineStatus engineStatus;
 	private boolean autoplay;
 	private String xmlName;
 
 	public EngineStatusCommand(final int generalTimeStamp,
 			final int nanoseconds, final EngineStatus engineStatus,
 			final boolean autoplay, final String xmlName) {
 		this.gerenalTimeStamp = generalTimeStamp;
 		this.nanoseconds = nanoseconds;
 		this.engineStatus = engineStatus;
 		this.autoplay = autoplay;
 		this.xmlName = xmlName;
 	}
 
 	public EngineStatusCommand(final byte[] byteArray) throws IOException,
 			AbstractRestoreECP1CommandException {
 		if (byteArray == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'byteArray' must not be null!");
 		}
 		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
 				byteArray);
 		final DataInputStream dataInputStream = new DataInputStream(
 				byteArrayInputStream);
 
 		final int startTag = dataInputStream.readInt();
 		if (startTag != IECP1Command.START_TAG) {
 			throw new WrongStartTagException(byteArray, startTag);
 		}
 
 		final char version = dataInputStream.readChar();
 		if (version != IECP1Command.VERSION) {
 			throw new WrongVersionException(byteArray, version);
 		}
 
 		final char commandTypeID = dataInputStream.readChar();
 		if (commandTypeID != EngineStatusCommand.COMMAND_TYPE_ID) {
 			throw new WrongTypeIdException(byteArray, commandTypeID,
 					EngineStatusCommand.COMMAND_TYPE_ID);
 		}
 
 		// unused length field
 		dataInputStream.readInt();
 		this.gerenalTimeStamp = dataInputStream.readInt();
 		this.nanoseconds = dataInputStream.readInt();
		repeatCount = (int) dataInputStream.readShort();
 		if (repeatCount < 0) {
 			repeatCount = 0xffff + repeatCount;
 		}
 		byte autoplayByte = dataInputStream.readByte();
 		if (autoplayByte == 0) {
 			this.autoplay = false;
 		} else if (autoplayByte == 1) {
 			this.autoplay = true;
 		} else {
 			throw new BrokenByteArrayException(byteArray,
 					"Wrong value for autoplay. Expected 0 or 1, got "
 							+ autoplay + "!");
 		}
 
 		this.engineStatus = EngineStatus.byteToEngineStatus(dataInputStream
 				.readByte());
 
 		final int xmlNameLength = dataInputStream.readInt();
 		if (xmlNameLength != 0xffffffff) {
 			final byte[] xmlNameBuffer = new byte[xmlNameLength];
 			dataInputStream.readFully(xmlNameBuffer);
 			this.xmlName = new String(xmlNameBuffer,
 					IECP1Command.STRING_ENCODING);
 		} else {
 			this.xmlName = "";
 		}
 
 	}
 
 	public byte[] getByteArray() throws IOException {
 		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 		final DataOutputStream dataOutputStream = new DataOutputStream(
 				byteArrayOutputStream);
 
 		dataOutputStream.writeInt(IECP1Command.START_TAG);
 		dataOutputStream.writeChar(IECP1Command.VERSION);
 		
 		dataOutputStream.writeChar(ErrorCommand.COMMAND_TYPE_ID);
 		final byte[] xmlNameBuffer = this.xmlName
 				.getBytes(IECP1Command.STRING_ENCODING);
 		if (this.xmlName.length() != 0) {
 			dataOutputStream.writeInt(16 + xmlNameBuffer.length);
 		} else {
 			dataOutputStream.writeInt(16);
 		}
 
 		dataOutputStream.writeInt(this.gerenalTimeStamp);
 		dataOutputStream.writeInt(this.nanoseconds);
 		dataOutputStream.writeShort((short) this.repeatCount);
 		dataOutputStream.writeByte(this.autoplay ? 1 : 0);
 		dataOutputStream.writeByte(EngineStatus
 				.engineStatusToByte(this.engineStatus));
 
 		if (this.xmlName.length() != 0) {
 			dataOutputStream.writeInt(xmlNameBuffer.length);
 			dataOutputStream.write(xmlNameBuffer);
 		} else {
 			dataOutputStream.writeInt(0xffffffff);
 		}
 
 		dataOutputStream.close();
 
 		return byteArrayOutputStream.toByteArray();
 	}
 
 	public int getGerenalTimeStamp() {
 		return this.gerenalTimeStamp;
 	}
 
 	public void setGerenalTimeStamp(final int gerenalTimeStamp) {
 		this.gerenalTimeStamp = gerenalTimeStamp;
 	}
 
 	public int getNanoseconds() {
 		return this.nanoseconds;
 	}
 
 	public void setNanoseconds(final int nanoseconds) {
 		this.nanoseconds = nanoseconds;
 	}
 
 	public int getRepeatCount() {
 		return this.repeatCount;
 	}
 
 	public void setRepeatCount(final int repeatCount) {
 		this.repeatCount = repeatCount;
 	}
 
 	public boolean isAutoplay() {
 		return this.autoplay;
 	}
 
 	public void setAutoplay(final boolean autoplay) {
 		this.autoplay = autoplay;
 	}
 
 	public EngineStatus getEngineStatus() {
 		return this.engineStatus;
 	}
 
 	public void setEngineStatus(final EngineStatus engineStatus) {
 		this.engineStatus = engineStatus;
 	}
 
 	public String getXmlName() {
 		return xmlName;
 	}
 
 	public void setXmlName(String xmlName) {
 		this.xmlName = xmlName;
 	}
 }
