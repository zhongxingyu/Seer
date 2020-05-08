 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jm.device.v1;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import jm.device.ConnectorType;
 
 /**
  *
  * @author Ogilvy
  */
 class ISO14230 extends jm.device.KWP2000 implements IProtocol {
 
     private Box _box;
     private Shared _shared;
     private Default<ISO14230> _default;
     private boolean _lLine;
     private int _sendLine;
     private int _recvLine;
 
     public ISO14230(Box box, Shared shared) {
         _box = box;
         _shared = shared;
         _default = new Default<>(_box, _shared, this);
         _lLine = false;
         _sendLine = 0;
         _recvLine = 0;
     }
 
     private byte[] readOneFrame(boolean isFinish) throws IOException {
         byte[] temp = new byte[3];
         int length = _box.readBytes(temp, 0, 3);
         if (length <= 0) {
             finishExecute(isFinish);
             throw new IOException();
         }
         byte[] result = null;
         if ((temp[1] & 0xFF) == _sourceAddress) {
             if ((temp[0] & 0xFF) == 0x80) {
                 byte[] temp2 = new byte[1];
                 length = _box.readBytes(temp2, 0, 1);
                 if (length <= 0) {
                     finishExecute(isFinish);
                     throw new IOException();
                 }
                 length = temp2[0] & 0xFF;
                 if (length <= 0) {
                     finishExecute(isFinish);
                     throw new IOException();
                 }
                 result = new byte[KWP80HeaderLength + length + KWPChecksumLength];
                 System.arraycopy(temp, 0, result, 0, 3);
                 System.arraycopy(temp2, 0, result, 3, 1);
                 length = _box.readBytes(result, KWP80HeaderLength, length + KWPChecksumLength);
                 if (length <= 0) {
                     finishExecute(isFinish);
                     throw new IOException();
                 }
 
             } else {
                 length = (temp[0] & 0xFF) - 0x80;
                 if (length == 0) {
                     finishExecute(isFinish);
                     throw new IOException();
                 }
                 result = new byte[KWP8XHeaderLength + length + KWPChecksumLength];
                 System.arraycopy(temp, 0, result, 0, 3);
                 length = _box.readBytes(result, 3, length + KWPChecksumLength);
                 if (length <= 0) {
                     finishExecute(isFinish);
                     throw new IOException();
                 }
             }
         } else {
             if ((temp[0] & 0xFF) == 0x00) {
                 length = temp[1] & 0xFF;
                 if (length == 0) {
                     finishExecute(isFinish);
                     throw new IOException();
                 }
                 result = new byte[KWP00HeaderLength + length + KWPChecksumLength];
                 System.arraycopy(temp, 0, result, 0, 3);
                 length = _box.readBytes(result, 3, length);
             } else {
                 length = temp[0] & 0xFF;
                 if (length == 0) {
                     finishExecute(isFinish);
                     throw new IOException();
                 }
                 result = new byte[KWPXXHeaderLength + length + KWPChecksumLength];
                 System.arraycopy(temp, 0, result, 0, 3);
                 length = _box.readBytes(result, 3, length - KWPChecksumLength);
                 if (length <= 0) {
                     finishExecute(isFinish);
                     throw new IOException();
                 }
             }
         }
         finishExecute(isFinish);
         int checksum = 0;
         for (int i = 0; i < result.length - 1; i++) {
             checksum += result[i] & 0xFF;
         }
 
         if ((checksum & 0xFF) != (result[result.length - 1] & 0xFF)) {
             throw new IOException();
         }
 
         return unpack(result);
     }
 
     @Override
     public void fastInit(byte[] data) throws IOException {
         int valueOpen = 0;
         if (_lLine) {
             valueOpen = D.PWC | D.RZFC | D.CK;
         } else {
             valueOpen = D.PWC | D.RZFC | D.CK;
         }
 
         if (!_box.setCommCtrl(valueOpen, D.SET_NULL)
                 || !_box.setCommLine(_sendLine, _recvLine)
                 || !_box.setCommLink(D.RS_232 | D.BIT9_MARK | D.SEL_SL | D.UN_DB20, D.SET_NULL, D.SET_NULL)
                 || !_box.setCommBaud(10416)
                 || !_box.setCommTime(D.SETBYTETIME, 5000)
                 || !_box.setCommTime(D.SETWAITTIME, 0)
                 || !_box.setCommTime(D.SETRECBBOUT, 400000)
                 || !_box.setCommTime(D.SETRECFROUT, 500000)
                 || !_box.setCommTime(D.SETLINKTIME, 500000)) {
             throw new IOException();
         }
         try {
             Thread.sleep(1000);
         } catch (InterruptedException ex) {
             Logger.getLogger(ISO14230.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         _shared.buffID = 0;
         if (!_box.newBatch(_shared.buffID)) {
             throw new IOException();
         }
         byte[] packEnter = pack(data);
 
         if (!_box.setLineLevel(D.COMS, D.SET_NULL)
                 || !_box.commboxDelay(25000)
                 || !_box.setLineLevel(D.SET_NULL, D.COMS)
                || !_box.commboxDelay(25000)
                 || !_box.sendOutData(0, packEnter.length, packEnter)
                 || !_box.runReceive(D.REC_FR)
                 || !_box.endBatch()) {
             _box.delBatch(_shared.buffID);
             throw new IOException();
         }
 
         if (!_box.runBatch(false, _shared.buffID)) {
             throw new IOException();
         }
         packEnter = readOneFrame();
         _box.setCommTime(D.SETWAITTIME, 55000);
     }
 
     @Override
     public void addrInit(int addrCode) throws IOException {
         if (!_box.setCommCtrl(D.PWC | D.REFC | D.RZFC | D.CK, D.SET_NULL)
                 || !_box.setCommLink(D.RS_232 | D.BIT9_MARK | D.SEL_SL | D.SET_DB20, D.SET_NULL, D.INVERTBYTE)
                 || !_box.setCommBaud(5)
                 || !_box.setCommTime(D.SETBYTETIME, 5000)
                 || !_box.setCommTime(D.SETWAITTIME, 12000)
                 || !_box.setCommTime(D.SETRECBBOUT, 400000)
                 || !_box.setCommTime(D.SETRECFROUT, 500000)
                 || !_box.setCommTime(D.SETLINKTIME, 500000)) {
             throw new IOException();
         }
         try {
             Thread.sleep(1000);
         } catch (InterruptedException ex) {
             Logger.getLogger(ISO14230.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         _shared.buffID = 0;
         if (!_box.newBatch(_shared.buffID)) {
             throw new IOException();
         }
 
         if (!_box.sendOutData(0, 1, (byte) addrCode)
                 || !_box.setCommLine((_recvLine == D.RK_NO) ? _sendLine : D.SK_NO, _recvLine)
                 || !_box.runReceive(_box.getD().SET55_BAUD)
                 || !_box.runReceive(D.REC_LEN_1)
                 || !_box.turnOverOneByOne()
                 || !_box.runReceive(D.REC_LEN_1)
                 || !_box.turnOverOneByOne()
                 || !_box.runReceive(D.REC_LEN_1)
                 || !_box.endBatch()) {
             _box.delBatch(_shared.buffID);
             throw new IOException();
         }
 
         byte[] temp = new byte[3];
         if (!_box.runBatch(false, _shared.buffID)
                 || (_box.readData(temp, 0, 3, 3000000) <= 0)
                 || !_box.checkResult(500000)) {
             _box.delBatch(_shared.buffID);
             throw new IOException();
         }
 
         if (!_box.delBatch(_shared.buffID)
                 || !_box.setCommTime(D.SETWAITTIME, 55000)) {
             throw new IOException();
         }
 
         if (temp[2] != 0) {
             throw new IOException();
         }
     }
 
     @Override
     public void setLines(int comLine, boolean lLine) throws IllegalArgumentException {
         // According the connector to determine the send and receive line
         if (_shared.connector.equals(ConnectorType.OBDII_16)) {
             switch (comLine) {
                 case 7:
                     _sendLine = D.SK1;
                     _recvLine = D.RK1;
                     break;
                 default:
                     throw new IllegalArgumentException();
             }
         } else {
             throw new IllegalArgumentException();
         }
         _lLine = lLine;
     }
 
     @Override
     public void sendOneFrame(byte[] data) throws IOException {
         _default.sendOneFrame(data, false);
     }
 
     @Override
     public void sendFrames(byte[] data) throws IOException {
         sendOneFrame(data);
     }
 
     @Override
     public byte[] readOneFrame() throws IOException {
         return readOneFrame(true);
     }
 
     @Override
     public byte[] readFrames() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void finishExecute(boolean isFinish) {
         if (isFinish) {
             _box.stopNow(true);
             _box.delBatch(_shared.buffID);
             _box.checkResult(500000);
         }
     }
 
     @Override
     public void setKeepLink(byte[] data) throws IOException {
         _mode = _linkMode;
         byte[] buff = pack(data);
         _mode = _msgMode;
 
         _default.setKeepLink(buff);
     }
 }
