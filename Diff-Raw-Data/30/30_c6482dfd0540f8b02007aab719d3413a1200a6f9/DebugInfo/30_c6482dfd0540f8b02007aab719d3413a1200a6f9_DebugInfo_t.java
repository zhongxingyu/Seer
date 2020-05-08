 package com.wolfesoftware.mipsos.common;
 
 import java.io.*;
 import java.util.*;
 import java.util.Map.Entry;
 
 import com.wolfesoftware.mipsos.assembler.Bin.BinBase;
 import com.wolfesoftware.mipsos.assembler.Bin.Instr;
 import com.wolfesoftware.mipsos.assembler.Bin.Pseudo;
 import com.wolfesoftware.mipsos.assembler.*;
 import com.wolfesoftware.mipsos.assembler.Token.TokenBase;
 
 public class DebugInfo
 {
     public final String inputPath;
     private static final String ATTRIBUTE_INPUT_PATH = "inputPath";
     public final HashMap<String, Long> labels;
     private static final String ATTRIBUTE_LABELS = "labels";
     private final TreeMap<Integer, Integer> lineToAddressMap = new TreeMap<Integer, Integer>();
     private static final String ATTRIBUTE_LINE_TO_ADDRESS = "lineToAddress";
     private final TreeMap<Integer, Integer> addressToLineMap = new TreeMap<Integer, Integer>();
     private static final String ATTRIBUTE_ADDRESS_TO_LINE = "addressToLine";
 
     public DebugInfo(String inputPath, HashMap<String, Long> labels)
     {
         this.inputPath = inputPath;
         this.labels = labels;
     }
 
     public void write(int baseAddress, BinBase[] elements, HashMap<String, Long> labels, ArrayList<Integer> lineIndecies, TokenBase[] tokens)
     {
         long addr = baseAddress;
         for (BinBase binElem : elements) {
             byte[] data = binElem.getBinary(labels, addr);
             if (binElem instanceof Instr || binElem instanceof Pseudo) {
                 int line = Util.findInList(lineIndecies, tokens[binElem.tokenStart].srcStart);
                 lineToAddressMap.put(line, (int)addr);
                 addressToLineMap.put((int)addr, line);
             }
             addr += data.length;
         }
     }
 
     public int lineToAddress(int lineNumber)
     {
         Entry<Integer, Integer> entry = lineToAddressMap.ceilingEntry(lineNumber);
         if (entry == null)
             throw new IllegalArgumentException();
         return entry.getValue();
     }
     public int addressToLine(int address)
     {
        Entry<Integer, Integer> entry = addressToLineMap.floorEntry(address);
         if (entry == null)
             throw new IllegalArgumentException();
         return entry.getValue();
     }
 
     public Segment toSegment()
     {
         try {
             HashMap<String, byte[]> attributes = new HashMap<String, byte[]>();
             attributes.put(Segment.ATTRIBUTE_TYPE, Segment.TYPE_DEBUGINFO);
             attributes.put(ATTRIBUTE_INPUT_PATH, inputPath.getBytes());
             attributes.put(ATTRIBUTE_ADDRESS_TO_LINE, mapIntIntToBytes(addressToLineMap));
             attributes.put(ATTRIBUTE_LINE_TO_ADDRESS, mapIntIntToBytes(lineToAddressMap));
             attributes.put(ATTRIBUTE_LABELS, mapStringLongToBytes(labels));
             return new Segment(attributes, new byte[0]);
         } catch (IOException e) {
             throw null;
         }
     }
     public static DebugInfo fromSegment(Segment segment)
     {
         try {
             String inputPath = new String(segment.attributes.get(ATTRIBUTE_INPUT_PATH));
             HashMap<String, Long> labels = bytesToMapStringLong(segment.attributes.get(ATTRIBUTE_LABELS));
             TreeMap<Integer, Integer> addressToLineMap = bytesToMapIntInt(segment.attributes.get(ATTRIBUTE_ADDRESS_TO_LINE));
             TreeMap<Integer, Integer> lineToAddressMap = bytesToMapIntInt(segment.attributes.get(ATTRIBUTE_LINE_TO_ADDRESS));
 
             DebugInfo debugInfo = new DebugInfo(inputPath, labels);
             debugInfo.addressToLineMap.putAll(addressToLineMap);
             debugInfo.lineToAddressMap.putAll(lineToAddressMap);
 
             return debugInfo;
         } catch (IOException e) {
             throw null;
         }
     }
 
     private static byte[] mapStringLongToBytes(HashMap<String, Long> map) throws IOException
     {
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();
         ByteUtils.writeInt(outStream, map.size());
         for (Entry<String, Long> entry : map.entrySet()) {
             ByteUtils.writeString(outStream, entry.getKey());
             ByteUtils.writeInt(outStream, entry.getValue().intValue());
         }
         return outStream.toByteArray();
     }
 
     private static HashMap<String, Long> bytesToMapStringLong(byte[] bytes) throws IOException
     {
         HashMap<String, Long> result = new HashMap<String, Long>();
         ByteArrayInputStream inStream = new ByteArrayInputStream(bytes);
         int count = ByteUtils.readInt(inStream);
         for (int i = 0; i < count; i++)
             result.put(ByteUtils.readString(inStream), (long)ByteUtils.readInt(inStream));
         return result;
     }
 
     private static byte[] mapIntIntToBytes(TreeMap<Integer, Integer> map) throws IOException
     {
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();
         ByteUtils.writeInt(outStream, map.size());
         for (Entry<Integer, Integer> entry : map.entrySet()) {
             ByteUtils.writeInt(outStream, entry.getKey());
             ByteUtils.writeInt(outStream, entry.getValue());
         }
         return outStream.toByteArray();
     }
 
     private static TreeMap<Integer, Integer> bytesToMapIntInt(byte[] bytes) throws IOException
     {
         TreeMap<Integer, Integer> result = new TreeMap<Integer, Integer>();
         ByteArrayInputStream inStream = new ByteArrayInputStream(bytes);
         int count = ByteUtils.readInt(inStream);
         for (int i = 0; i < count; i++)
             result.put(ByteUtils.readInt(inStream), ByteUtils.readInt(inStream));
         return result;
     }
 }
