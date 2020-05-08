 /*
  * This file is part of Beads. See http://www.beadsproject.net for all information.
  */
 package net.beadsproject.beads.data.audiofile;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import net.beadsproject.beads.core.IOAudioFormat;
 import net.beadsproject.beads.core.AudioIO;
 import net.beadsproject.beads.data.Sample;
 import net.beadsproject.beads.data.SampleAudioFormat;
 
 /**
  * An AudioFile provides a handle to an audio file located either on disk or online. 
  * 
  * AudioFile is used by {@link net.beadsproject.beads.data.Sample Sample}, however if you want 
  * to use it, then {@link AudioIO.getAudioFile} will return an audio file.
  * 
  * Depending on the AudioIO implementation different audio file types may or may not be available.
  * 
  * @author ben
  */
 public abstract class AudioFile {
 	/** 
 	 * The name of this audiofile.
 	 * 
 	 * Corresponds to the name of file, url, 
 	 * or a inputstream generated id 
 	 **/
 	protected String name;
 	
 	/**
 	 * The format of the decoded audio data. 
 	 */
 	protected SampleAudioFormat audioFormat;	
 		
 	/** 
 	 * The total number of frames.
 	 * 
 	 * If it equals -1 then the length is unknown. 
 	 **/
 	protected long nFrames;
 	
 	/** 
 	 * Length of the file in milliseconds
 	 *  
 	 * If <0 then it is unknown. 
 	 **/
 	protected float length;
 	
 	/**
 	 * Additional properties of this AudioFile. 
 	 * 
 	 * For example, an .mp3 file may have mp3 tags that get stored in this map.
 	 **/
 	protected Map<String, Object> audioInfo;
 	
 	/** 
 	 * Advanced.
 	 *  
 	 * Trace the open, closing, and resetting of this audio file. Useful to debug and tune the parameters of AudioFile and Sample. 
 	 * */ 
 	protected boolean trace = false;
 		
 	/**
 	 * Thrown if an operation is unsupported for this AudioFile.
 	 * 
 	 * @author ben
 	 */
 	static public class OperationUnsupportedException extends Exception
 	{
 		private static final long serialVersionUID = 1L;
 		private String operation; 
 		public OperationUnsupportedException(String operation)
 		{
 			super(operation);
 			this.operation = operation;
 		}
 		
 		public String getError()
 		{
 			return this.operation;
 		}		
 	}	
 		
 	/**
 	 * Reset the audio input stream. 
 	 * 
 	 * For some audio formats, this may involve re-opening the associated file.
 	 * @throws OperationUnsupported 
 	 */
 	public void reset() throws OperationUnsupportedException
 	{
 		throw(new OperationUnsupportedException("reset"));
 	}
 	
 	/**
 	 * Skips a number of frames.
 	 * 
 	 * For some audio formats, this may involve re-opening the associated file.
 	 * @throws OperationUnsupported 
 	 */
 	public void skip(long frames) throws OperationUnsupportedException
 	{
 		throw(new OperationUnsupportedException("skip"));
 	}	
 
 	/**
 	 * Seek to a specific frame number. Note that seeking is slower than skipping forward.
 	 * 
 	 * @param frame The frame number, relative to the start of the audio data.
 	 * @throws OperationUnsupported 
 	 */
 	public void seek(int frame) throws OperationUnsupportedException
 	{
 		throw(new OperationUnsupportedException("seek"));
 	}
 
 	/**
 	 * Opens the audio file.
 	 */
 	public abstract void open() throws UnsupportedAudioFileException, IOException;
 	
 	/**
 	 * @return The name of the audio file. 
 	 */
 	public String getName()
 	{
 		return name;
 	}
 	
 	/**
 	 * @return Information about this audio file. The file should be open if specific information is required.
 	 */
 	public String getInformation()
 	{
 		if (!isOpen())
 		{
 			String str = "Filename: " + getName() + "\n";
 			str += "File not open.\n";
 			return str;
 		}
 		else
 		{		
 			String str = "Filename: " + getName() + "\n";
 			str += "Format: " + audioFormat.toString() + "\n";
 			
 			str += "Channels: " + getNumChannels() + "\n";
 			str += "Frames: " + getNumFrames() + "\n";
 			str += "Length: " + getLength()+ "ms\n";			
 			str += "Bit Depth: " + getBitDepth() + "b\n";
 			
 			if (!audioInfo.isEmpty())
 			{			
 				str += "Additional Properties:\n";
 				for (String key: audioInfo.keySet())
 				{			
 					str += "- \"" + key + "\" : \"" + audioInfo.get(key) + "\"\n";
 				}				
 			}
 			
 			return str;
 		}
 	}
 
 	/**
 	 * Close the audio file. Can be re-opened.
 	 * @throws IOException 
 	 */
 	public abstract void close() throws IOException;
 
 	/**
 	 * Is the file stream open?
 	 */
 	public boolean isOpen()
 	{
 		return !isClosed();
 	}
 
 	/**
 	 * Is the file stream closed?
 	 */
 	public abstract boolean isClosed();
 
 	/**
 	 * @return The AudioFormat of this AudioFile. If the file is encoded, the AudioFormat is of the decoded data.
 	 */
 	public SampleAudioFormat getFormat()
 	{
 		return audioFormat;
 	}
 
 	/**
 	 * Read bytes from this audiofile. The bytes will be in an interleaved format. It is 
 	 * the responsibility of the caller to interpret this data correctly.  
 	 *  
 	 * The number of bytes read is equal to the size of the byte buffer. 
 	 * If that many bytes aren't available the buffer will only be partially filled.   
 	 * 
 	 * @param buffer A buffer to fill.
 	 * 
 	 * @return The number of bytes read. A value of -1 indicates the file has no data left.
 	 */
 	public abstract int read(byte[] buffer);
 	
 	/**
 	 * Read decoded audio data in a non-interleaved, Beads-friendly format. This function mainly
 	 * exists for debugging purposes, and is extremely slow. @{link Sample} provides a much faster 
 	 * way to access data from an AudioFile. 
 	 * 
 	 * @param buffer The buffer to fill. After execution buffer[i][j] will contain the sample in channel i, frame j. Buffer has size (numChannels,numFramesRequested). 
 	 * 
 	 * @return The number of frames read.
 	 */
 	public abstract int read(float[][] buffer);
 	
 	/**
 	 * @return Any additional properties associated with this AudioFile.
 	 */
 	public Map<String,Object> getProperties()
 	{
 		return audioInfo;
 	}	
 	
 	/**
 	 * @return The number of channels of audio data.
 	 */
 	public int getNumChannels() {
 		return audioFormat.channels;
 	}
 
 	protected void setNumFrames(long nFrames) {
 		this.nFrames = nFrames;
 	}
 
 	/**
 	 * @return The length of this audio file in number of frames. If -1 then the length is unknown.
 	 */
 	public long getNumFrames() {
 		return nFrames;
 	}
 
 	protected void setLength(float length) {
 		this.length = length;
 	}
 
 	/**
 	 * @return The length of this audio file in millseconds. If < 0 then the length is unknown.
 	 */
 	public float getLength() {
 		return length;
 	}
 	
 	/**
 	 * @return The size of each sample in bits.
 	 */
 	public int getBitDepth() {
 		return audioFormat.bitDepth;		
 	}
 
 	/**
 	 * @return The size of each frame in bytes.
 	 */
 	public int getFrameSize() {
		return audioFormat.bitDepth / 8;		
 	}
     
 	/**
 	 * A debugging tool.
 	 * 
 	 * @param trace Enable or disable a trace of file accesses. Useful for debugging.
 	 */
 	public void setTrace(boolean trace)
 	{
 		this.trace = trace;
 	}
 	
 }
 
