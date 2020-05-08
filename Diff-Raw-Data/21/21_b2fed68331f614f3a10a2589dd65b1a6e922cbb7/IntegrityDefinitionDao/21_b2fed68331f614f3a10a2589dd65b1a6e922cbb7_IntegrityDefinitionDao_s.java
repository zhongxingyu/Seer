 /*
  * Copyright (c) 2012 Chris Ellison, Mike Deats, Liron Yahdav, Ryan Neal,
  * Brandon Sutherlin, Scott Griffin
  * 
  * This software is released under the MIT license
  * (http://www.opensource.org/licenses/mit-license.php)
  * 
  * Created on Feb 7, 2012
  */
 package edu.cmu.sv.arinc838.dao;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import com.arinc.arinc838.IntegrityDefinition;
 
 import edu.cmu.sv.arinc838.binary.BdfFile;
import edu.cmu.sv.arinc838.validation.DataValidator;
 
 public class IntegrityDefinitionDao {
 
 	public static enum IntegrityType {
 		CRC16(2), CRC32(3), CRC64(6);
 
 		private long type;
 
 		private IntegrityType(long type) {
 			this.type = type;
 		}
 
 		public long getType() {
 			return type;
 		}
 
 		@Override
 		public String toString() {
 			return super.toString() + "(" + type + ")";
 		}
 
 		public static String asString() {
 			return "[" + CRC16 + "," + CRC32 + "," + CRC64 + "]";
 		}
 
 		public static IntegrityType fromLong(long value) {
 			if (value == CRC16.getType()) {
 				return CRC16;
 			} else if (value == CRC32.getType()) {
 				return CRC32;
 			} else if (value == CRC64.getType()) {
 				return CRC64;
 			}
 			return null;
 		}
 	}
 
 	private long integType;
 	private byte[] integValue;
 
 	public IntegrityDefinitionDao() {
 	}
 
 	public IntegrityDefinitionDao(IntegrityDefinition integDef) {
 		setIntegrityType(integDef.getIntegrityType());
 		setIntegrityValue(integDef.getIntegrityValue());
 	}
 
 	public IntegrityDefinitionDao(BdfFile bdfFile) throws IOException {
 		setIntegrityType(bdfFile.readUint32());
 		setIntegrityValue(bdfFile.readHexbin64k());
 	}
 
 	public void setIntegrityType(long value) {
		integType = DataValidator.validateIntegrityType(value);
 	}
 
 	public long getIntegrityType() {
 		return integType;
 	}
 
 	public void setIntegrityValue(byte[] value) {
		integValue = DataValidator.validateIntegrityValue(value);
 	}
 
 	public byte[] getIntegrityValue() {
 		return integValue;
 	}
 
 	@Override
 	public int hashCode() {
 		if (this.getIntegrityValue() != null) {
 			return this.getIntegrityValue().hashCode();
 		}
 
 		return 0;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		return obj != null
 				&& this == obj
 				|| (obj instanceof IntegrityDefinitionDao && equals((IntegrityDefinitionDao) obj));
 	}
 
 	public boolean equals(IntegrityDefinitionDao obj) {
 		return obj != null
 				&& this == obj
 				|| (Arrays.equals(this.getIntegrityValue(),
 						obj.getIntegrityValue()) && (this.getIntegrityType() == obj
 						.getIntegrityType()));
 	}
 }
