 package net.theunnameddude.mcclient.protocol;
 
 import io.netty.buffer.ByteBuf;
 
 public enum PacketInstruction implements IPacketInstruction {
     // TODO Skip bytes instead of reading.
     ReadInt{
         @Override
         public void read(ByteBuf buf) {
             int i = buf.readInt();
         }
     },
     ReadInt3{
         @Override
         public void read(ByteBuf buf) {
             buf.readInt();
             buf.readInt();
             buf.readInt();
         }
     },
     ReadIntArray{
         @Override
         public void read(ByteBuf buf) {
             byte length = buf.readByte();
             for ( int i = 0; i < length; i++ ) {
                 buf.readInt();
             }
         }
     },
     ReadByte{
         @Override
         public void read(ByteBuf buf) {
             byte i = buf.readByte();
         }
     },
     ReadUShortByteArray{
         @Override
         public void read(ByteBuf buf) {
             int i = buf.readUnsignedShort();
             buf.skipBytes( i );
         }
     },
     ReadIntByteArray{
         @Override
         public void read(ByteBuf buf) {
             int length = buf.readInt();
             for ( int i = 0; i < length; i++ ) {
                 buf.readByte();
             }
         }
     },
     ReadShort{
         @Override
         public void read(ByteBuf buf) {
             short i = buf.readShort();
         }
     },
    ReadUnsignedShort{
        @Override
        public void read(ByteBuf buf) {
            buf.readUnsignedShort();
        }
    },
    ReadIntUnsignedByteArray{
        @Override
        public void read(ByteBuf buf) {
            int length = buf.readInt();
            for ( int i = 0; i < length; i ++ ) {
                buf.readUnsignedByte();
            }
        }
    },
     ReadShortByteArray{
         @Override
         public void read(ByteBuf buf) {
             short length = buf.readShort();
             buf.readBytes(new byte[length]);
         }
     },
     ReadString{
         @Override
         public void read(ByteBuf buf) {
             String result = "";
             short length = buf.readShort();
             for ( int i = 0; i < length; i++ ) {
                 result += buf.readChar();
             }
         }
     },
     ReadStringArray{
         @Override
         public void read(ByteBuf buf) {
             short length = buf.readShort();
             for ( int i = 0; i < length; i++ ) {
                 ReadString.read( buf );
             }
         }
     },
     ReadBoolean{
         @Override
         public void read(ByteBuf buf) {
             boolean i = buf.readBoolean();
         }
     },
     ReadItem{
         @Override
         public void read(ByteBuf buf) {
             short itemId = buf.readShort();
             if ( itemId != -1 ) {
                 buf.readByte();
                 buf.readShort();
                 short nbtLength = buf.readShort();
                 if ( nbtLength != -1 ) {
                     buf.readBytes( new byte[nbtLength] );
                 }
             }
         }
     },
     ReadItemArray{
         @Override
         public void read(ByteBuf buf) {
             int count = buf.readShort();
             for( int i = 0; i < count; i++ ) {
                 ReadItem.read( buf );
             }
         }
     },
     ReadLong{
         @Override
         public void read(ByteBuf buf) {
             long i = buf.readLong();
         }
     },
     ReadFloat{
         @Override
         public void read(ByteBuf buf) {
             float i = buf.readFloat();
         }
     },
     ReadBulkChunk{
         @Override
         public void read(ByteBuf buf) {
             short count = buf.readShort();
             int size = buf.readInt();
             buf.readBoolean();
             buf.skipBytes( size + count * 12 );
         }
     },
     ReadEntityMeta{
         @Override
         public void read(ByteBuf buf) {
             int itemId;
             while ( (itemId = buf.readByte()) != 127 ) {
                 int index = itemId & 0x1F;
                 int type = itemId >> 5;
                 if (type == 0) buf.readByte();
                 if (type == 1) buf.readShort();
                 if (type == 2) buf.readInt();
                 if (type == 3) buf.readFloat();
                 if (type == 4) ReadString.read( buf );
                 if (type == 5) ReadItem.read( buf );
                 if (type == 6) {
                     buf.readInt();
                     buf.readInt();
                     buf.readInt();
                 }
             }
         }
     },
     ReadEntityProperties{
         @Override
         public void read(ByteBuf buf) {
             int recordCount = buf.readInt();
             for ( int i = 0; i < recordCount; i++ ) {
                 ReadString.read( buf );
                 buf.readDouble();
                 short size = buf.readShort();
                 for ( short s = 0; s < size; s++ ) {
                     buf.skipBytes( 25 ); // long, long, double, byte - Almost directly stolen from md_5
                 }
             }
         }
     },
     ReadMotion{
         @Override
         public void read(ByteBuf buf) {
             int data = buf.readInt();
             if ( data > 0 ) {
                 buf.skipBytes( 6 );
             }
         }
     },
     ReadDouble{
         @Override
         public void read(ByteBuf buf) {
             buf.readDouble();
         }
     },
     ReadOptionalWindow{
         @Override
         public void read(ByteBuf buf) {
             ReadByte.read( buf );
             byte type = buf.readByte();
             ReadString.read( buf );
             ReadByte.read( buf );
             ReadBoolean.read( buf );
             if ( type == 11 ) {
                 ReadInt.read( buf );
             }
         }
     }
 }
