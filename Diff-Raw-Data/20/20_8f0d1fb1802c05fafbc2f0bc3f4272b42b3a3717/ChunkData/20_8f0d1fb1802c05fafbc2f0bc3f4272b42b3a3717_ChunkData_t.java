 package org.hptd.format;
 
 import com.google.common.io.ByteArrayDataOutput;
 import com.google.common.io.ByteStreams;
 import org.hptd.utils.ByteBufferUtil;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * the chunk data description of HPTD chunk,it is composed of two:column types,datas.
  * <ul>
  * <li>column type length(2 bytes)</li>
  * <li>column type list(each type is 4 bits,means 1 byte has 2 types)</li>
  * <li>data list(string/array will prefix the column data length(2 bytes)</li>
  * </ul>
  *
  * @author ford
  */
 public class ChunkData implements HptdByteBuffer {
     private long datetime;
     private final List<DataValue> datas;
 
     public ChunkData(List<DataValue> datas) {
         this(datas, 0);
     }
 
     public ChunkData(List<DataValue> datas, long datetime) {
         this.datas = datas;
         this.datetime = datetime;
     }
 
     public List<DataValue> getDatas() {
         return datas;
     }
 
    /**
     * the consolidated number values size
     *
     * @return
     */
    public int numberSize() {
        int k = 0;
        for (DataValue data : datas) {
            if (data.isNumber()) k++;
        }
        return k;
    }

     public long getDatetime() {
         return datetime;
     }
 
     public void setDatetime(long datetime) {
         this.datetime = datetime;
     }
 
     @Override
     public String toString() {
         return "ChunkData{" +
                 "datetime=" + datetime +
                 ", datas=" + datas +
                 '}';
     }
 
     @Override
     public final ByteBuffer toBuffer() {
         int dataSize = datas.size();
         short typeLen = (short) ((dataSize + 1) >> 1);
         byte[] types = new byte[typeLen];
         ByteArrayDataOutput dataOut = ByteStreams.newDataOutput();
         ByteArrayDataOutput all = ByteStreams.newDataOutput();
         ValueType preType = null;
         for (int k = 0; k < dataSize; k++) {
             DataValue dataValue = datas.get(k);
             dataValue.writeDataOut(dataOut);
             ValueType type = dataValue.getType();
             int mod = k % 2;
             if (mod == 1) {
                 types[k >> 1] = preType.combine(type);
             } else if (mod == 0 && k == dataSize - 1) {
                 types[k >> 1] = type.combine(null);
             } else {
                 preType = type;
             }
         }
         all.writeShort(typeLen);
         all.write(types);
         all.write(dataOut.toByteArray());
         return ByteBufferUtil.bigEndianWrap(all.toByteArray());
     }
 
     static public ChunkData valueOf(ByteBuffer buffer) {
         ArrayList<DataValue> tmpDatas = new ArrayList<DataValue>();
         int typeLen = buffer.getShort();
         byte[] types = new byte[typeLen];
         buffer.get(types);
         for (byte type : types) {
             tmpDatas.add(new DataValue(ValueType.valueOf((byte) (type >> 4 & 0x0F))));
             if ((type & 0x0F) != 0x0F)
                 tmpDatas.add(new DataValue(ValueType.valueOf((byte) (type & 0x0F))));
         }
         ByteBufferDataInput byteBufferDataInput = new ByteBufferDataInput(buffer);
         for (DataValue tmpData : tmpDatas) {
             tmpData.readDataInput(byteBufferDataInput);
         }
         return new ChunkData(tmpDatas);
     }
 }
