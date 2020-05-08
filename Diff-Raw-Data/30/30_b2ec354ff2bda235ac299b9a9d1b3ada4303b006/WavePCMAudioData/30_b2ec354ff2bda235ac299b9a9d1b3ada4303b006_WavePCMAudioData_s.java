 package gong.audio.data;
 
 import gong.audio.AudioDataException;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Enumeration;
 
 import javax.sound.sampled.AudioFileFormat;
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 
 public class WavePCMAudioData extends BlockAudioData {
 	
 	private static final int BLOCK_SIZE_IN_SAMPLES = 4096;
 
     public static final String FILE_EXTENSION = ".wav";
 
 	public WavePCMAudioData(AudioFormat format) {
		super(format);
 		super.samplesPerBlock = BLOCK_SIZE_IN_SAMPLES;
 	}
 
 	public String getFileExtension() {
 		return FILE_EXTENSION;
 	}
 	
 	@Override
 	protected Block createBlock() {
 		return new WavePCMBlock();
 	}
 
 	@Override
 	public boolean isSupported(String feature) {
 		return false;
 	}
 
 	@Override
 	public long getMemoryUsage() {
 		return getLength()*2;
 	}
 
 	@Override
 	public void sendToStream(OutputStream stream) throws IOException,
 			AudioDataException {
 
 		ByteArrayOutputStream baos = new ByteArrayOutputStream(getLength()*2);
 		Enumeration e = blockData.elements();
 		while (e.hasMoreElements()) {
 			WavePCMBlock block = (WavePCMBlock) e.nextElement();
 			block.sendToStream(baos);
 		}
		
 		AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(baos.toByteArray()), getFormat(), getLength());
 		AudioSystem.write(ais, AudioFileFormat.Type.WAVE, stream);
 	}
 
 	@Override
 	public void receiveFromStream(InputStream stream, boolean synchronous)
 			throws IOException, AudioDataException {
		while(stream.available() > 0) {
			WavePCMBlock block = new WavePCMBlock();
			block.readFromStream(stream);
			super.blockData.add(block);
 		}
		super.availableBlocks = super.blockData.size();
		super.position = getLength();
 	}
 	
 	private class WavePCMBlock extends Block {
         
         public WavePCMBlock() {
             super(BLOCK_SIZE_IN_SAMPLES);
         }
 
         public synchronized int read() throws AudioDataException {
             int dataIndex = position << 1;
             int a = data[dataIndex] & 0x00FF;
             int b = data[dataIndex + 1] & 0x00FF;
             position++;
 
             int sample =  a << 8 | b;
             return sample;
         }
 
         public synchronized void write(int sample) throws AudioDataException {
             if (eob()) throw new AudioDataException("Invalid write request.");
             if (data == null) {
                 data = new byte[size * 2];
             }
 
             int dataIndex = position << 1;
             data[dataIndex] = (byte) ((sample >> 8) & 0xFF);
             data[dataIndex+1] = (byte) (sample & 0xFF);
             position++;
         }
 
         public synchronized void sendToStream(OutputStream stream) throws IOException, AudioDataException {
             if (data == null) throw new AudioDataException("Invalid send request.");
             stream.write(data);
         }
 
         public synchronized void readFromStream(InputStream stream) throws IOException, AudioDataException {
             if (data == null) {
                 data = new byte[size * 2];
             }
             int read = stream.read(data);
             position += read / 2;
         }
         
     }
 
 }
