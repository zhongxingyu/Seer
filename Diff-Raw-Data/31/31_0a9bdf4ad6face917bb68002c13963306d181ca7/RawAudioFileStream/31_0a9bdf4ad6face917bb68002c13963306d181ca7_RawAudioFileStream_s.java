 package edu.cmu.pandaa.stream;
 
 import java.io.*;
 import java.util.List;
 import java.util.SortedSet;
 
 import edu.cmu.pandaa.header.RawAudioHeader;
 import edu.cmu.pandaa.header.RawAudioHeader.RawAudioFrame;
 import edu.cmu.pandaa.header.StreamHeader;
 import edu.cmu.pandaa.header.StreamHeader.StreamFrame;
 import edu.cmu.pandaa.utils.DataConversionUtil;
 
 public class RawAudioFileStream implements FrameStream {
   private OutputStream os;
   private InputStream is;
   DataInputStream dis;
   DataOutputStream dos;
   private String fileName;
   private RawAudioHeader headerRef;
   private int startFrame;
   private double timeDilation;
   private int byteCount = 0;
   private int update_pos1, update_pos2, wavDataSize;
   private int loopSize = 0;
   public int loopCount = 1;
   private final int BITS_PER_BYTE = 8;
   private int wavFrameLength;
   private List<String> fileList;
   private int frameLength;
 
   int wavSamplingRate;
   int wavSampleCount;
   int wavBitsPerSample;
 
   private final short MONO = 1;
   private final short STEREO = 2;
   private final int PCM_FORMAT = 1;
 
   private final String riffString = "RIFF";
   private final String formatString = "WAVE";
   private final String subChunk1String = "fmt ";
   private final String metadataString = "LIST";
   private final String infoString = "INFO";
   private final String commentString = "ICMT";
   private final String subChunk2String = "data";
   private final int DEFAULT_SUBCHUNK1_SIZE = 16; // For PCM
 
   public RawAudioFileStream(String fileName) throws IOException {
     this.fileName = fileName;
     is = new FileInputStream(fileName);
     wavFrameLength = RawAudioHeader.DEFAULT_FRAMETIME;
   }
 
   public RawAudioFileStream(List<String> fileList) throws IOException {
     this(fileList.remove(0));
     this.fileList = fileList;
     System.out.println("Processing " + fileName);
   }
 
   public RawAudioFileStream(String fileName, boolean overwrite) throws IOException {
     this.fileName = fileName;
     File file = new File(fileName);
     if (file.exists() && !overwrite) {
       throw new IOException("File exists");
     }
     os = new FileOutputStream(file);
   }
 
   public RawAudioFileStream(String fileName, String prototype, int length) throws Exception {
     this(fileName);
     if (prototype != null) {
       RawAudioFileStream protoStream = null;
       protoStream = new RawAudioFileStream(prototype);
       protoStream.getHeader();
       loopSize = protoStream.wavDataSize;
       int samples = protoStream.wavSamplingRate * length;
       loopCount = samples * protoStream.wavBitsPerSample / BITS_PER_BYTE / loopSize + 1;
       protoStream.close();
     }
   }
 
   public String getFileName() {
     return fileName;
   }
 
   public void setTimeDialtion(double timeDialation) {
     this.timeDilation = timeDialation;
   }
 
   private int read32() throws IOException {
     byte[] tmpInt32 = new byte[4];
     dis.read(tmpInt32);
     return DataConversionUtil.byteArrayToInt(tmpInt32);
   }
 
   private boolean resetStream() throws Exception {
     if (fileList != null) {
       if (fileList.size() <= 0)
         return false;
       fileName = fileList.remove(0);
       System.out.println("Processing " + fileName);
     } else {
       loopCount--;
       if (loopCount <= 0) {
         return false;
       }
     }
 
     dis.close();
     is = new FileInputStream(fileName);
     dis = null;
     RawAudioHeader saved = headerRef;
     headerRef = null;
     getHeader();
     headerRef = saved;
     return true;
   }
 
   @Override
   public StreamHeader getHeader() throws Exception {
     if (dis != null) {
       if (headerRef != null)
         return headerRef;
       else
         throw new RuntimeException("getHeader called twice!");
     }
     dis = new DataInputStream(is);
     byte[] tmpInt32 = new byte[4];
     byte[] tmpInt16 = new byte[2];
 
     int wavFileSize = 0, wavSubChunk1Size = 0;
     int wavByteRate = 0;
     String wavComment = null;
     int wavFormat = 0, wavChannels = 0, wavBlockAlign = 0;
 
     byte[] chunkID = new byte[4];
     int retval = dis.read(chunkID, 0, 4);
     if (!checkChunk(retval, chunkID, riffString))
       throw new RuntimeException("File not in correct format");
 
     dis.read(tmpInt32);
     wavFileSize = DataConversionUtil.byteArrayToInt(tmpInt32);
 
     retval = dis.read(chunkID, 0, 4);
     if (!checkChunk(retval, chunkID, formatString))
       throw new RuntimeException("File not in correct format");
 
     retval = dis.read(chunkID, 0, 4);
     if (!checkChunk(retval, chunkID, subChunk1String))
       throw new RuntimeException("File not in correct format");
 
     dis.read(tmpInt32);
     wavSubChunk1Size = DataConversionUtil.byteArrayToInt(tmpInt32);
 
     dis.read(tmpInt16);
     wavFormat = DataConversionUtil.byteArrayToInt(tmpInt16);
 
     if (wavFormat != PCM_FORMAT)
       throw new RuntimeException("Format not supported for conversion");
 
     dis.read(tmpInt16);
     wavChannels = DataConversionUtil.byteArrayToInt(tmpInt16);
 
     dis.read(tmpInt32);
     wavSamplingRate = DataConversionUtil.byteArrayToInt(tmpInt32);
 
     dis.read(tmpInt32);
     wavByteRate = DataConversionUtil.byteArrayToInt(tmpInt32);
 
     dis.read(tmpInt16);
     wavBlockAlign = DataConversionUtil.byteArrayToInt(tmpInt16);
 
     dis.read(tmpInt16);
     wavBitsPerSample = DataConversionUtil.byteArrayToInt(tmpInt16);
 
     retval = dis.read(chunkID, 0, 4);
     if (checkChunk(retval, chunkID, metadataString)) {
       int length = read32()-4;
       retval = dis.read(chunkID, 0, 4);
       if (checkChunk(retval, chunkID, infoString)) {
         while (length > 0) {
           retval = dis.read(chunkID, 0, 4);
           int chunkSize = read32();
           byte[] chunkData = new byte[chunkSize];
           dis.read(chunkData);
           length -= chunkSize + 8;
 
           if (checkChunk(retval, chunkID, commentString)) {
             wavComment = new String(chunkData);
             while (wavComment.charAt(wavComment.length()-1) == 0)
               wavComment = wavComment.substring(0, wavComment.length()-1);
           }
         }
       } else {
         for (int i = 0; i < length; i++)
           dis.read();
       }
 
       retval = dis.read(chunkID, 0, 4);
     }
 
     if (!checkChunk(retval, chunkID, subChunk2String))
       throw new RuntimeException("File not in correct format, bad data chunk header");
 
     dis.read(tmpInt32);
     wavDataSize = DataConversionUtil.byteArrayToInt(tmpInt32);
     wavSampleCount = wavDataSize * wavBitsPerSample / BITS_PER_BYTE;
     headerRef = new RawAudioHeader(getDeviceID(), 0, wavFrameLength, wavFormat, wavChannels,
             wavSamplingRate, wavBitsPerSample, wavComment);
     frameLength = (int) (headerRef.getSamplingRate() * wavFrameLength)/1000;
     return headerRef;
   }
 
   private boolean checkChunk(int size, byte[] data, String target) {
     if (size != 4 || data.length != 4 || target.length() != 4)
       throw new IllegalArgumentException("All should be 4!");
     byte[] tbytes = target.getBytes();
     for (int i = 0; i < size; i++) {
       if (tbytes[i] != data[i])
         return false;
     }
 
     return true;
   }
 
   private String getDeviceID() {
     int startIndex = 0, endIndex;
 
     startIndex = fileName.lastIndexOf(File.separatorChar) + 1;
 
     endIndex = fileName.lastIndexOf(".");
     if (endIndex == -1) {
       endIndex = fileName.length();
     }
 
     return fileName.substring(startIndex, endIndex);
   }
 
   @Override
   public StreamFrame recvFrame() throws Exception {
     if (timeDilation > 0)
       Thread.sleep((long) (headerRef.frameTime * timeDilation));
 
     RawAudioFrame rawAudioFrame = headerRef.makeFrame(frameLength);
     rawAudioFrame.audioData = readData();
 
     int bytesRead = rawAudioFrame.audioData.length * 2;
     byteCount += bytesRead;
 
     if (bytesRead <= 0 || (byteCount >= loopSize && loopSize > 0)) {
       if (!resetStream()) {
         return null;
       }
       byteCount = 0;
       rawAudioFrame.audioData = readData();
     }
     return rawAudioFrame;
   }
 
   private short[] readData() throws IOException {
     int numBytesInSample = headerRef.getBitsPerSample() / BITS_PER_BYTE;
     byte[] audioDataBytes =
             new byte[(int) (frameLength * numBytesInSample * headerRef.getNumChannels())];
     int bytesRead = dis.read(audioDataBytes);
     int useLen = bytesRead > 0 ? bytesRead : 0;
 
     short[] audioDataShort = DataConversionUtil.byteArrayToShortArray(audioDataBytes, useLen);
     if (headerRef.getNumChannels() == STEREO) {
       short[] monoAudioData = new short[audioDataShort.length / 2];
       for (int i = 0, j = 0; i < audioDataShort.length; i += 2, j++)
         monoAudioData[j] = (short) ((audioDataShort[i] + audioDataShort[i + 1]) / 2);
       audioDataShort = monoAudioData;
     }
     if (audioDataShort.length < frameLength) {
       if (byteCount < loopSize && loopSize > 0) {
         short[] newShort = new short[frameLength];
         System.arraycopy(newShort, 0, audioDataShort, 0, audioDataShort.length);
         audioDataShort = newShort;
       }
     }
 
     return audioDataShort;
   }
 
   @Override
   public void setHeader(StreamHeader h) throws Exception {
     headerRef = (RawAudioHeader) h;
     startFrame = headerRef.nextSeq;
     if (dos != null) {
       throw new RuntimeException("setHeader called twice!");
     }
     dos = new DataOutputStream(new FileOutputStream(fileName));
 
     long byteRate = (headerRef.getSamplingRate() * MONO * headerRef.getBitsPerSample()) / 8;
     int blockAlign = (int) (MONO * headerRef.getBitsPerSample()) / 8;
     long subChunk1Size = DEFAULT_SUBCHUNK1_SIZE;
 
     String paddedComment = null;
     if (headerRef.comment != null) {
       paddedComment = headerRef.comment;
       while (paddedComment.length() % 4 != 0)
         paddedComment = paddedComment + '\0';
       //headerSize += paddedComment.length() + 20; // LIST, size, INFO, ICMT, size
     }
 
     dos.writeBytes(riffString);
     update_pos1 = dos.size();
     dos.write(DataConversionUtil.intToByteArray(0), 0, 4); // dummy data value, updated on close
     dos.writeBytes(formatString);
     dos.writeBytes(subChunk1String);
     dos.write(DataConversionUtil.intToByteArray((int) subChunk1Size), 0, 4);
     dos.write(DataConversionUtil.shortToByteArray((short) headerRef.getAudioFormat()), 0, 2);
     dos.write(DataConversionUtil.shortToByteArray(MONO), 0, 2);
     dos.write(DataConversionUtil.intToByteArray((int) headerRef.getSamplingRate()), 0, 4);
     dos.write(DataConversionUtil.intToByteArray((int) byteRate), 0, 4);
     dos.write(DataConversionUtil.shortToByteArray((short) blockAlign), 0, 2);
     dos.write(DataConversionUtil.shortToByteArray((short) headerRef.getBitsPerSample()), 0, 2);
     if (paddedComment != null) {
       int size = paddedComment.length();
       dos.writeBytes(metadataString);
       dos.write(DataConversionUtil.intToByteArray(size + 12), 0, 4);
       dos.writeBytes(infoString);
       dos.writeBytes(commentString);
       dos.write(DataConversionUtil.intToByteArray(size), 0, 4);
       dos.writeBytes(paddedComment);
     }
     dos.writeBytes(subChunk2String);
     update_pos2 = dos.size();
     dos.write(DataConversionUtil.intToByteArray(0), 0, 4); // dummy data, updated on close
     dos.flush();
     frameLength = (int) (headerRef.getSamplingRate() * headerRef.frameTime);
     if (frameLength % 1000 != 0) {
       throw new IllegalArgumentException("Bad frame length: rounding error");
     }
     frameLength /= 1000;
   }
 
   @Override
   public void sendFrame(StreamFrame m) throws Exception {
     if (m == null)
       return;
     short[] audioData = ((RawAudioFrame) m).getAudioData();
     if (audioData.length != frameLength) {
       throw new IllegalArgumentException("Length " + audioData.length + " != " + frameLength);
     }
     for (int i = 0; i < audioData.length; i++) {
       dos.write((DataConversionUtil.shortToByteArray(audioData[i])), 0, 2);
     }
     byteCount += 2 * audioData.length;
 
     dos.flush();
   }
 
   public long getCurrentLength() {
     return (headerRef.nextSeq - startFrame) * headerRef.frameTime;
   }
 
   private void updateWavLength() throws Exception {
     RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
     raf.seek(update_pos1);
     raf.write(DataConversionUtil.intToByteArray(byteCount + update_pos2 - update_pos1));
     raf.seek(update_pos2);
     raf.write(DataConversionUtil.intToByteArray(byteCount));
     raf.close();
   }
 
   @Override
   public void close() {
     boolean update = false;
     try {
       if (dis != null) {
         dis.close();
         dis = null;
       }
       if (dos != null) {
         dos.close();
         dos = null;
         update = true;
       }
       if (os != null) {
         os.close();
         os = null;
       }
       if (is != null) {
         is.close();
         is = null;
       }
       if (update)
         updateWavLength();
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
   }
 
   public static void main(String[] args) throws Exception {
     int arg = 0;
     String outArg = args[arg++];
     String inArg = args[arg++];
     if (args.length != arg) {
       throw new IllegalArgumentException("Invalid number of arguments");
     }
 
     System.out.println("RawAudioFileStream: " + outArg + " " + inArg);
     RawAudioFileStream aIn = new RawAudioFileStream(inArg);
     RawAudioFileStream aOut = new RawAudioFileStream(outArg, true);
 
     RawAudioHeader h = (RawAudioHeader) aIn.getHeader();
     aOut.setHeader(h);
     RawAudioFrame frame;
 
     while ((frame = (RawAudioFrame) aIn.recvFrame()) != null) {
       aOut.sendFrame(frame);
     }
 
     aOut.close();
   }
 }
