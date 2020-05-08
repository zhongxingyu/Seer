 package jm.device.v1;
 
 final class Default<T extends IProtocol & jm.device.IProtocol> {
 
     private Box _box;
     private Shared _shared;
     private T _protocol;
 
     public Default(Box box, Shared shared, T protocol) {
         _box = box;
         _shared = shared;
         _protocol = protocol;
     }
 
     public boolean sendOneFrame(byte[] data, boolean needRecv) {
         _shared.buffID = 0;
        if (_box.newBatch(_shared.buffID)) {
             return false;
         }
         byte[] sendBuff = _protocol.pack(data);
         if (sendBuff == null) {
             return false;
         }
         if (needRecv) {
             if (!_box.sendOutData(0, sendBuff.length, sendBuff)
                     || !_box.runReceive(D.RECEIVE)
                     || !_box.endBatch()
                     || !_box.runBatch(false, _shared.buffID)) {
                 return false;
             }
         } else {
             if (!_box.sendOutData(0, sendBuff.length, sendBuff)
                     || !_box.endBatch()
                     || !_box.runBatch(false, _shared.buffID)) {
                 return false;
             }
         }
         _protocol.finishExecute(!needRecv);
         return true;
     }
 
     public boolean setKeepLink(byte[] data) {
         if (!_box.newBatch(D.LINKBLOCK)) {
             return false;
         }
         if (!_box.sendOutData(0, data.length, data)
                 || !_box.endBatch()) {
             return false;
         }
         return true;
     }
 }
