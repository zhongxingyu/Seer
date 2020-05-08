 /*
  * Copyright (C) 2012 Roman Elizarov
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.avrbuddy.avr;
 
 import org.avrbuddy.hex.HexFile;
 import org.avrbuddy.util.WrongFormatException;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * @author Roman Elizarov
  */
 public class AvrOperation {
     public static AvrOperation parse(String spec) throws WrongFormatException {
         String[] s = spec.split(":", 3);
         if (s.length < 3)
             throw new WrongFormatException("Expected <memtype>:<memcmd>:<filename>[:<format>], but found '" + spec + "'");
         AvrMemType memType = AvrMemType.parse(s[0]);
         AvrMemCmd memCmd = AvrMemCmd.parse(s[1]);
         String fileName = s[2];
         if (fileName.endsWith(":i") || fileName.endsWith(":I"))
             fileName = fileName.substring(0, fileName.length() - 2);
         return new AvrOperation(memType, memCmd, fileName);
     }
 
     // -------------------------- instance --------------------------
 
     private final AvrMemType memType;
     private final AvrMemCmd memCmd;
     private final String fileName;
 
     private AvrOperation(AvrMemType memType, AvrMemCmd memCmd, String fileName) {
         this.memType = memType;
         this.memCmd = memCmd;
         this.fileName = fileName;
     }
 
     public void execute(AvrProgrammer pgm) throws IOException {
         File file = new File(fileName);
         int length;
         byte[] readBuf;
         switch (memCmd) {
             case READ:
                 length = pgm.getPart().getMemInfo(memType).getMemSize();
                 readBuf = new byte[length];
                 int len = pgm.read(memType, 0, readBuf);
                 HexFile.write(file, 0, readBuf, len);
             case WRITE:
             case VERIFY:
                 HexFile hf = HexFile.read(file);
                 int offset = hf.getBaseOffset();
                 byte[] fileBuf = hf.getBytes();
                 if (memCmd == AvrMemCmd.WRITE)
                     pgm.write(memType, offset, fileBuf);
                 length = hf.getLength();
                 readBuf = new byte[length];
                 pgm.read(memType, offset, readBuf);
                 for (int i = 0; i < length; i++)
                     if (readBuf[i] != fileBuf[i])
                         throw new IOException(String.format(
                                 "Verify failed. Byte at offset %04X is expected to be %02X, but found %02X",
                                     i, fileBuf[i] & 0xff, readBuf[i] & 0xff));
                 break;
         }
     }
 }
