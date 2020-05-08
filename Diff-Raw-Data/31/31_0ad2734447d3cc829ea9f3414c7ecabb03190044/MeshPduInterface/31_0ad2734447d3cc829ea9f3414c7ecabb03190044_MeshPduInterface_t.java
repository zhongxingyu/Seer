 package com.example.meshonandroid.pdu;
 
import java.io.UnsupportedEncodingException;

 import adhoc.aodv.exception.BadPduFormatException;
 
 public interface MeshPduInterface {
    public byte[] toBytes() throws UnsupportedEncodingException;
 
     public void parseBytes(byte[] dataToParse)  throws BadPduFormatException;
     /*
     public long getAliveTime();
 
     public void setTimer();
     */
 
     public byte getPduType();
 
     /*
     public int getSequenceNumber();
 
     public void setSequenceNumber(int sequenceNumber);
 
     public boolean resend();
     */
 }
