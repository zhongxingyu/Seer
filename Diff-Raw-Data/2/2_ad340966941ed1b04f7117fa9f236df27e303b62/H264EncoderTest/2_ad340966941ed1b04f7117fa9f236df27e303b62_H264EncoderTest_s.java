 package org.test.streaming;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertTrue;
 
 import java.io.File;
 
 import org.junit.Test;
 import org.test.streaming.encoding.H264Encoder;
 
 import com.xuggle.xuggler.ICodec;
 import com.xuggle.xuggler.IContainer;
 import com.xuggle.xuggler.IStream;
 import com.xuggle.xuggler.IStreamCoder;
 
 public class H264EncoderTest {
 
 	/*
 	 * Asume que ffmpeg y codecs estan instalados correctamente.
 	 */
 	@Test
 	public void testH264Encoding() {
 
 		Conf conf = new Conf("/alt-test-conf.properties");
 		String videoFilename = conf.get("test.video.encoding.file.name");
 		
 		File originFile = new File(conf.getSharedDir(), videoFilename);
 
 		assertNotNull("Origin file not setted correcly: "+videoFilename, originFile);
 		assertTrue("Origin file does not exist "+videoFilename, originFile.exists());
 
 		H264Encoder encoder = new H264Encoder(videoFilename, conf.getSharedDir(), conf.getCachosDir());
 
 		File encodedFile = encoder.encode();
 
 		assertTrue("Encoded file does not exist", encodedFile.exists());
 		assertTrue("Origin file should still exist at this point", originFile.exists());
 
		assertEquals("Origin and target file ahould have same name", encodedFile.getName(), originFile.getName());
 
 		String codecName = getCodecName(encodedFile);
 		assertEquals("CODEC_ID_H264", codecName);
 	}
 
 	private String getCodecName(File inFile) {
 
 		String codecName = null;
 		// Create a Xuggler container object
 		IContainer container = IContainer.make();
 		// Open up the container
 		if (container.open(inFile.getAbsolutePath(), IContainer.Type.READ, null) < 0)
 			throw new IllegalArgumentException("could not open file: " + inFile);
 
 		// query how many streams the call to open found
 		int numStreams = container.getNumStreams();
 		// and iterate through the streams to print their meta data
 		for (int i = 0; i < numStreams; i++) {
 			// Find the stream object
 			IStream stream = container.getStream(i);
 			// Get the pre-configured decoder that can decode this stream;
 			IStreamCoder coder = stream.getStreamCoder();
 			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
 				/* ignore for now */
 			} else if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
 				codecName = coder.getCodecID().name();
 			}
 		}
 		container.close();
 		return codecName;
 	}
 }
 
