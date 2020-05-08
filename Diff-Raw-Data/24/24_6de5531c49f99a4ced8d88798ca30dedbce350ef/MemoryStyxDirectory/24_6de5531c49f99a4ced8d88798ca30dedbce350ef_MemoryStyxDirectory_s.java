 package com.v2soft.styxlib.library.server.vfs;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
 import com.v2soft.styxlib.library.io.StyxByteBufferWriteable;
 import com.v2soft.styxlib.library.messages.base.enums.FileMode;
 import com.v2soft.styxlib.library.messages.base.enums.ModeType;
 import com.v2soft.styxlib.library.messages.base.enums.QIDType;
 import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
 import com.v2soft.styxlib.library.messages.base.structs.StyxStat;
 import com.v2soft.styxlib.library.server.ClientState;
 import com.v2soft.styxlib.library.types.ULong;
 
 /**
  * In-Memory directory
  * @author vshcryabets@gmail.com
  *
  */
 public class MemoryStyxDirectory
 extends MemoryStyxFile {
     private Map<ClientState, StyxByteBufferWriteable> mBuffersMap;
     private List<IVirtualStyxFile> mFiles;
 
     public MemoryStyxDirectory(String name) {
         super(name);
         mQID.setType(QIDType.QTDIR);
         mFiles = new LinkedList<IVirtualStyxFile>();
         mBuffersMap = new HashMap<ClientState, StyxByteBufferWriteable>();
     }
 
     @Override
     public int getMode() {
         return (int) (FileMode.Directory.getMode() | 0x01FF);
     }
 
     @Override
     public IVirtualStyxFile walk(List<String> pathElements, List<StyxQID> qids) {
         if ( pathElements.size() > 0 ) {
             String filename = pathElements.get(0);
             for (IVirtualStyxFile file : mFiles) {
                 if ( file.getName().equals(filename)) {
                     pathElements.remove(0);
                     return file.walk(pathElements, qids);
                 }
             }
         }
         return super.walk(pathElements, qids);
     }
 
     @Override
     public boolean open(ClientState client, int mode) throws IOException {
         boolean result = ((mode&0x0F) == ModeType.OREAD);
         if ( result ) {
             // prepare binary structure of the directory
             int size = 0;
             final List<StyxStat> stats = new LinkedList<StyxStat>();
             for (IVirtualStyxFile file : mFiles) {
                 final StyxStat stat = file.getStat();
                 size += stat.getSize();
                 stats.add(stat);
             }
             // allocate buffer
             StyxByteBufferWriteable buffer = new StyxByteBufferWriteable(size);
             for (StyxStat state : stats) {
                 state.writeBinaryTo(buffer);
             }
             mBuffersMap.put(client, buffer);
         }
         return result;
     }
 
     @Override
     public long read(ClientState client, byte[] outbuffer, ULong offset, long count) throws StyxErrorMessageException {
         if ( !mBuffersMap.containsKey(client)) StyxErrorMessageException.doException("This file isn't open");
         final ByteBuffer buffer = mBuffersMap.get(client).getBuffer();
         int boffset = buffer.limit();
         if ( offset.asLong() > boffset ) return 0;
         buffer.position((int) offset.asLong());
         int bleft = buffer.remaining();
         if ( count > bleft ) {
             count = bleft;
         }
         buffer.get(outbuffer, 0, (int) count);
         return count;
     }
 
     @Override
     public void close(ClientState client) {
         // remove buffer
         mBuffersMap.remove(client);
     }
 
     /**
      * Add child file
      * @param file
      */
     public void addFile(IVirtualStyxFile file) {
         // TODO check! may be this folder already contains file with same name 
         mFiles.add(file);
     }
 
     @Override
     public int write(ClientState client, byte[] data, ULong offset)
             throws StyxErrorMessageException {
         StyxErrorMessageException.doException("Can't write to directory");
         return 0;
     }
 
     @Override
     public void onConnectionClosed(ClientState state) {
         for (IVirtualStyxFile file : mFiles) {
             file.onConnectionClosed(state);
         }
         mBuffersMap.remove(state);
     }
 }
