 package de.ptb.epics.eve.ecp1.commands;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import de.ptb.epics.eve.ecp1.intern.exceptions.AbstractRestoreECP1CommandException;
 import de.ptb.epics.eve.ecp1.intern.exceptions.WrongStartTagException;
 import de.ptb.epics.eve.ecp1.intern.exceptions.WrongTypeIdException;
 import de.ptb.epics.eve.ecp1.intern.exceptions.WrongVersionException;
 import de.ptb.epics.eve.ecp1.types.AcquisitionStatus;
 import de.ptb.epics.eve.ecp1.types.DataModifier;
 import de.ptb.epics.eve.ecp1.types.DataType;
 import de.ptb.epics.eve.ecp1.types.EpicsSeverity;
 import de.ptb.epics.eve.ecp1.types.EpicsStatus;
 
 public class MeasurementDataCommand implements IECP1Command {
 
 	public static final char COMMAND_TYPE_ID = 0x0104;
 
 	private String name;
 	private int chid;
 	private int smid;
 
 	private AcquisitionStatus acquisitionStatus;
 	private DataModifier dataModifier;
 	private DataType dataType;
 	private int positionCounter;
 	private EpicsSeverity epicsSeverity;
 	private EpicsStatus epicsStatus;
 	private int generalTimeStamp;
 	private int nanoseconds;
 	private List<?> values;
 
 	public MeasurementDataCommand(int chid, int smid, int positionCounter,
 			final DataType dataType, final DataModifier dataModifier,
 			final EpicsSeverity epicsSeverity, final EpicsStatus epicsStatus,
 			final AcquisitionStatus acquisitionStatus,
 			final int gerenalTimeStamp, final int nanoseconds, final String name) {
 		this.chid = chid;
 		this.smid = smid;
 		this.positionCounter = positionCounter;
 		this.dataType = dataType;
 		this.dataModifier = dataModifier;
 		this.epicsSeverity = epicsSeverity;
 		this.epicsStatus = epicsStatus;
 		this.acquisitionStatus = acquisitionStatus;
 		this.generalTimeStamp = gerenalTimeStamp;
 		this.nanoseconds = nanoseconds;
 		this.name = name;
 		switch (this.dataType) {
 		case INT8:
 			this.values = new ArrayList<Byte>();
 		case INT16:
			this.values = new ArrayList<Character>();
 		case INT32:
 			this.values = new ArrayList<Integer>();
 		case FLOAT:
 			this.values = new ArrayList<Float>();
 		case DOUBLE:
 			this.values = new ArrayList<Double>();
 		case STRING:
 			this.values = new ArrayList<String>();
 		}
 	}
 
 	public MeasurementDataCommand(final byte[] byteArray) throws IOException,
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
 		if (commandTypeID != MeasurementDataCommand.COMMAND_TYPE_ID) {
 			throw new WrongTypeIdException(byteArray, commandTypeID,
 					MeasurementDataCommand.COMMAND_TYPE_ID);
 		}
 
 		// TODO: Length is never read, but the readInt() is necessary !!!!
 		// for unknown reason
 		final int length = dataInputStream.readInt();
 		chid = dataInputStream.readInt();
 		smid = dataInputStream.readInt();
 		positionCounter = dataInputStream.readInt();
 		dataInputStream.readChar();
 		dataInputStream.readByte();
 		this.dataType = DataType.byteTotDataType(dataInputStream.readByte());
 		dataInputStream.readChar();
 		dataInputStream.readByte();
 		this.dataModifier = DataModifier.byteTotDataModifyer(dataInputStream
 				.readByte());
 		this.epicsSeverity = EpicsSeverity.byteToEpicsSeverity(dataInputStream
 				.readByte());
 		this.epicsStatus = EpicsStatus.byteToEpicsStatus(dataInputStream
 				.readByte());
 		dataInputStream.readByte();
 		this.acquisitionStatus = AcquisitionStatus
 				.byteToAcquisitionStatus(dataInputStream.readByte());
 		this.generalTimeStamp = dataInputStream.readInt();
 		this.nanoseconds = dataInputStream.readInt();
 		final int lengthOfName = dataInputStream.readInt();
 		if (lengthOfName != 0xffffffff) {
 			final byte[] nameBuffer = new byte[lengthOfName];
 			dataInputStream.readFully(nameBuffer);
 			this.name = new String(nameBuffer, IECP1Command.STRING_ENCODING);
 		} else {
 			this.name = "";
 		}
 		final int dataCount = dataInputStream.readInt();
 		switch (this.dataType) {
 		case INT8:
 			this.values = new ArrayList<Byte>();
 		case INT16:
			this.values = new ArrayList<Character>();
 		case INT32:
 			this.values = new ArrayList<Integer>();
 		case FLOAT:
 			this.values = new ArrayList<Float>();
 		case DOUBLE:
 			this.values = new ArrayList<Double>();
 		case STRING:
 			this.values = new ArrayList<String>();
 		case DATETIME:
 			this.values = new ArrayList<String>();
 		}
 		switch (this.dataType) {
 		case INT8:
 			for (int i = 0; i < dataCount; ++i) {
 				this.add(new Byte(dataInputStream.readByte()));
 			}
 			break;
 
 		case INT16:
 			for (int i = 0; i < dataCount; ++i) {
				this.add(new Character(dataInputStream.readChar()));
 			}
 			break;
 
 		case INT32:
 			for (int i = 0; i < dataCount; ++i) {
 				this.add(Integer.valueOf(dataInputStream.readInt()));
 			}
 			break;
 
 		case FLOAT:
 			for (int i = 0; i < dataCount; ++i) {
 				this.add(new Float(dataInputStream.readFloat()));
 			}
 			break;
 
 		case DOUBLE:
 			for (int i = 0; i < dataCount; ++i) {
 				this.add(new Double(dataInputStream.readDouble()));
 			}
 			break;
 
 		case STRING:
 			for (int i = 0; i < dataCount; ++i) {
 				final int stringLength = dataInputStream.readInt();
 				if (stringLength != 0xffffffff) {
 					final byte[] stringBuffer = new byte[stringLength];
 					dataInputStream.readFully(stringBuffer);
 					this.add(new String(stringBuffer,
 							IECP1Command.STRING_ENCODING));
 				} else {
 					this.add("");
 				}
 
 			}
 			break;
 
 		case DATETIME:
 			for (int i = 0; i < dataCount; ++i) {
 				final int stringLength = dataInputStream.readInt();
 				if (stringLength != 0xffffffff) {
 					final byte[] stringBuffer = new byte[stringLength];
 					dataInputStream.readFully(stringBuffer);
 					this.add(new String(stringBuffer,
 							IECP1Command.STRING_ENCODING));
 				} else {
 					this.add("");
 				}
 
 			}
 			break;
 		}
 	}
 
 	public void add(final Object value) {
 		switch (dataType) {
 		case INT8:
 			((List<Byte>) this.values).add((Byte) value);
 			break;
 		case INT16:
			((List<Character>) this.values).add((Character) value);
 			break;
 		case INT32:
 			((List<Integer>) this.values).add((Integer) value);
 			break;
 		case FLOAT:
 			((List<Float>) this.values).add((Float) value);
 			break;
 		case DOUBLE:
 			((List<Double>) this.values).add((Double) value);
 			break;
 		case STRING:
 			((List<String>) this.values).add((String) value);
 			break;
 		case DATETIME:
 			((List<String>) this.values).add((String) value);
 			break;
 		}
 	}
 
 	public boolean remove(final Object value) {
 		return this.values.remove(value);
 	}
 
 	public void clear() {
 		this.values.clear();
 	}
 
 	public Iterator<?> iterator() {
 		return this.values.iterator();
 	}
 
 	public byte[] getByteArray() throws IOException {
 		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 		final DataOutputStream dataOutputStream = new DataOutputStream(
 				byteArrayOutputStream);
 
 		dataOutputStream.writeInt(IECP1Command.START_TAG);
 		dataOutputStream.writeChar(IECP1Command.VERSION);
 		
 		dataOutputStream.writeChar(MeasurementDataCommand.COMMAND_TYPE_ID);
 
 		int length = 28;
 		length += 12; // smid, chid, positionCounter
 
 		final byte[] nameBuffer = this.name
 				.getBytes(IECP1Command.STRING_ENCODING);
 
 		length += nameBuffer.length;
 
 		byte[][] strings = new byte[this.values.size()][];
 		switch (this.dataType) {
 		case INT8:
 			length += this.values.size();
 			break;
 		case INT16:
 			length += this.values.size() * 2;
 			break;
 
 		case INT32:
 		case FLOAT:
 			length += this.values.size() * 4;
 			break;
 
 		case DOUBLE:
 			length += this.values.size() * 8;
 			break;
 
 		case STRING:
 			length += this.values.size();
 			for (int i = 0; i < this.values.size(); ++i) {
 				strings[i] = ((List<String>) this.values).get(i).getBytes(
 						IECP1Command.STRING_ENCODING);
 				length += strings[i].length;
 			}
 			break;
 
 		}
 
 		dataOutputStream.writeInt(length);
 		dataOutputStream.writeInt(chid);
 		dataOutputStream.writeInt(smid);
 		dataOutputStream.writeInt(positionCounter);
 
 		dataOutputStream.writeChar(0);
 		dataOutputStream.writeByte(0);
 		dataOutputStream.writeByte(DataType.dataTypeToByte(this.dataType));
 
 		dataOutputStream.writeChar(0);
 		dataOutputStream.writeByte(0);
 		dataOutputStream.writeByte(DataModifier
 				.dataModifyerToByte(this.dataModifier));
 
 		dataOutputStream.writeByte(EpicsSeverity
 				.epicsSeverityToByte(this.epicsSeverity));
 		dataOutputStream.writeByte(EpicsStatus
 				.epicsStatusToByte(this.epicsStatus));
 		dataOutputStream.writeByte(0);
 		dataOutputStream.writeByte(AcquisitionStatus
 				.acquisitionStatusToByte(this.acquisitionStatus));
 
 		dataOutputStream.writeInt(this.generalTimeStamp);
 		dataOutputStream.writeInt(this.nanoseconds);
 
 		if (this.name.length() != 0) {
 			dataOutputStream.writeInt(nameBuffer.length);
 			dataOutputStream.write(nameBuffer);
 		} else {
 			dataOutputStream.writeInt(0xffffffff);
 		}
 
 		dataOutputStream.writeInt(this.values.size());
 
 		switch (this.dataType) {
 		case INT8:
 			for (int i = 0; i < this.values.size(); ++i) {
 				dataOutputStream.writeByte(((List<Byte>) this.values).get(i));
 			}
 			break;
 		case INT16:
 			for (int i = 0; i < this.values.size(); ++i) {
 				dataOutputStream.writeChar(((List<Character>) this.values)
 						.get(i));
 			}
 			break;
 
 		case INT32:
 			for (int i = 0; i < this.values.size(); ++i) {
 				dataOutputStream.writeInt(((List<Integer>) this.values).get(i));
 			}
 			break;
 
 		case FLOAT:
 			for (int i = 0; i < this.values.size(); ++i) {
 				dataOutputStream.writeFloat(((List<Float>) this.values).get(i));
 			}
 			break;
 
 		case DOUBLE:
 			for (int i = 0; i < this.values.size(); ++i) {
 				dataOutputStream.writeDouble(((List<Double>) this.values)
 						.get(i));
 			}
 			break;
 
 		case STRING:
 			length += this.values.size();
 			for (int i = 0; i < strings.length; ++i) {
 				if (strings[i].length != 0) {
 					dataOutputStream.writeInt(strings[i].length);
 					dataOutputStream.write(strings[i]);
 				} else {
 					dataOutputStream.writeInt(0xffffffff);
 				}
 			}
 			break;
 
 		}
 
 		dataOutputStream.close();
 
 		return byteArrayOutputStream.toByteArray();
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public void setName(final String name) {
 		this.name = name;
 	}
 
 	public int getChainId() {
 		return this.chid;
 	}
 
 	public void setChainId(final int chid) {
 		this.chid = chid;
 	}
 
 	public int getScanModuleId() {
 		return this.smid;
 	}
 
 	public void setScanModuleId(final int smid) {
 		this.smid = smid;
 	}
 
 	public AcquisitionStatus getAcquisitionStatus() {
 		return this.acquisitionStatus;
 	}
 
 	public void setAcquisitionStatus(final AcquisitionStatus acquisitionStatus) {
 		this.acquisitionStatus = acquisitionStatus;
 	}
 
 	public DataModifier getDataModifier() {
 		return this.dataModifier;
 	}
 
 	public void setDataModifier(final DataModifier dataModifier) {
 		this.dataModifier = dataModifier;
 	}
 
 	public DataType getDataType() {
 		return this.dataType;
 	}
 
 	public void setDataType(final DataType dataType) {
 		if (dataType != this.dataType) {
 			switch (dataType) {
 			case INT8:
 				this.values = new ArrayList<Byte>();
 				break;
 			case INT16:
 				this.values = new ArrayList<Character>();
 				break;
 			case INT32:
 				this.values = new ArrayList<Integer>();
 				break;
 			case FLOAT:
 				this.values = new ArrayList<Float>();
 				break;
 			case DOUBLE:
 				this.values = new ArrayList<Double>();
 				break;
 			case STRING:
 				this.values = new ArrayList<String>();
 				break;
 			}
 		}
 		this.dataType = dataType;
 	}
 
 	public int getPositionCounter() {
 		return this.positionCounter;
 	}
 
 	public void setPositionCounter(final int positionCounter) {
 		this.positionCounter = positionCounter;
 	}
 
 	public EpicsSeverity getEpicsSeverity() {
 		return this.epicsSeverity;
 	}
 
 	public void setEpicsSeverity(final EpicsSeverity epicsSeverity) {
 		this.epicsSeverity = epicsSeverity;
 	}
 
 	public EpicsStatus getEpicsStatus() {
 		return this.epicsStatus;
 	}
 
 	public void setEpicsStatus(final EpicsStatus epicsStatus) {
 		this.epicsStatus = epicsStatus;
 	}
 
 	public int getGeneralTimeStamp() {
 		return this.generalTimeStamp;
 	}
 
 	public void setGerenalTimeStamp(final int generalTimeStamp) {
 		this.generalTimeStamp = generalTimeStamp;
 	}
 
 	public int getNanoseconds() {
 		return this.nanoseconds;
 	}
 
 	public void setNanoseconds(final int nanoseconds) {
 		this.nanoseconds = nanoseconds;
 	}
 }
