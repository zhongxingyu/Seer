 package com.yuniclient;
 
 public class Packet
 {
     public Packet(byte opcode, byte[] data, byte lenght)
     {
         //if(data != null)
             set(opcode, data, lenght);
         m_writePos = 0;
         m_readPos = 0;
        m_countOpcode = false;
     }
     
     public short readByte()
     {
         if(m_readPos >= m_lenght)
             return 0;
         ++m_readPos;
         return (short)(0xFF & ((int)m_data[m_readPos-1]));
     }
     
     public int readUInt16()
     {
         if(m_readPos+1 >= m_lenght)
             return 0;
         int firstByte = (0xFF & ((int)m_data[m_readPos]));
         int secondByte = (0xFF & ((int)m_data[m_readPos+1]));
         m_readPos += 2;
         return  ((firstByte << 8) | secondByte);
     }
     
     public String readString()
     {
         if(m_data[m_readPos] != 2)
             return "";
         String read = "";
         for(++m_readPos; m_readPos < m_lenght; ++m_readPos)
         {
             if(m_data[m_readPos] == 3)
             {
                 ++m_readPos;
                 break;
             }
             read += (char)m_data[m_readPos];
         }
         return read;
     }
     
     public void writeByte(short data)
     {
         m_data[m_writePos++] = (byte)data;
     }
     
     public void writeUInt16(int data)
     {
         m_data[m_writePos++] = (byte)(data >> 8);
         m_data[m_writePos++] = (byte) data;
     }
     
     public byte get(byte pos) { return (pos >= m_lenght) ? 0 : m_data[pos]; }
     public void set(byte opcode, byte[] data, byte lenght)
     {
         m_opcode = opcode;
         if(data != null)
             m_data = data.clone();
         else
             m_data = null;
         m_lenght = lenght;
         m_readPos = 0;
     }
     public byte[] getSendData()
     {
         byte res[] = new byte[m_lenght+4];
         res[0] = (byte) 0xFF; // start
         res[1] = 0x01;        // address
        if(m_countOpcode)
            res[2] = (byte) (m_lenght+1);
        else
            res[2] = m_lenght;    // data lenght;
         res[3] = m_opcode;    // opcode
         if(m_data != null)
             for(byte i = 0; i < m_lenght; ++i)
                 res[i+4] = m_data[i];
         return res;
     }
     public boolean hasData() { return (m_data != null); }
     public byte getOpcode() { return m_opcode; }
     public byte getLenght() { return m_lenght; }
     public void setPos(byte pos) { m_readPos = pos; } 
     public void setWritePos(byte pos) { m_writePos = pos; }

    public void CountOpcode(boolean count) { m_countOpcode = count; }

     private byte m_opcode;
     private byte[] m_data;
     private byte m_lenght;
     private byte m_readPos;
     private byte m_writePos;
    private boolean m_countOpcode;
 }
