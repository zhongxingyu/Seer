 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jm.device.v1;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Ogilvy
  */
 class Mikuni extends jm.device.Mikuni implements IProtocol {
 
     private Box _box;
     private Shared _shared;
     private Default<Mikuni> _default;
     public Mikuni(Box box, Shared shared) {
         _box = box;
         _shared = shared;
        _default = new Default<Mikuni>(_box, _shared, this);
     }
     
     @Override
     public void addrInit(int addrCode) throws IOException {
         if (!_box.setCommCtrl(D.PWC | D.RZFC | D.CK | D.REFC, D.SET_NULL)
                 || !_box.setCommLine(D.SK_NO, D.RK1)
                 || !_box.setCommLink(D.RS_232 | D.BIT9_MARK | D.SEL_SL | D.UN_DB20, 0xFF, 2)
                 || !_box.setCommBaud(19200)
                 || !_box.setCommTime(D.SETBYTETIME, 100000)
                 || !_box.setCommTime(D.SETWAITTIME, 1000)
                 || !_box.setCommTime(D.SETRECBBOUT, 400000)
                 || !_box.setCommTime(D.SETRECFROUT, 500000)
                 || !_box.setCommTime(D.SETLINKTIME, 500000)) {
             throw new IOException();
         }
         try {
             Thread.sleep(1000);
         } catch (InterruptedException ex) {
             Logger.getLogger(Mikuni.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public void setLines(int comLine, boolean lLine) throws IllegalArgumentException {
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
         return readOneFrame();
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
         byte[] buff = pack(data);
         _default.setKeepLink(buff);
     }
     
     private byte[] readOneFrame(boolean isFinish) throws IOException {
         byte[] buff = new byte[256];
         int i = 0;
         byte before = 0;
         for (; _box.readBytes(buff, i, 1) > 0; i++) {
             if (before == 0x0D && buff[i] == 0x0A) {
                 break;
             }
             before = buff[i];
         }
         
         if (before != 0x0D || buff[i - 1] != 0x0A) {
             throw new IOException();
         }
         byte[] result = new byte[i];
         System.arraycopy(buff, 0, result, 0, i);
         finishExecute(isFinish);
         return result;
     }
     
 }
