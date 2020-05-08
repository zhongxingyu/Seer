 package yamb.util.media.thumbnail.video.xuggler;
 
 import yamb.util.media.thumbnail.video.AbstractVideoThumbnailGenerator;
 import yamb.util.media.thumbnail.video.VideoThumbnailGenerator;
 import com.xuggle.xuggler.ICodec;
 import com.xuggle.xuggler.IContainer;
 import com.xuggle.xuggler.IPacket;
 import com.xuggle.xuggler.IPixelFormat;
 import com.xuggle.xuggler.IStream;
 import com.xuggle.xuggler.IStreamCoder;
 import com.xuggle.xuggler.IVideoPicture;
 import com.xuggle.xuggler.IVideoResampler;
 import com.xuggle.xuggler.Utils;
 import com.xuggle.xuggler.io.IURLProtocolHandler;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Takes a media container, finds the first video stream,
  * decodes that stream, and then displays the video frames,
  * at the frame-rate specified by the container, on a
  * window.
  *
  * @author aclarke
  */
 public class XugglerVideoThumbnailGenerator extends AbstractVideoThumbnailGenerator implements VideoThumbnailGenerator
 {
 
     @Override
     public Image generateThumbnailImage(File aInputVideoFile, int aPosition) throws IOException
     {
         String filename = aInputVideoFile.getAbsolutePath();
 
         // Let's make sure that we can actually convert video pixel formats.
         if (!IVideoResampler.isSupported(
                 IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION))
         {
             throw new RuntimeException("you must install the GPL version" +
                     " of Xuggler (with IVideoResampler support) for " +
                     "this demo to work");
         }
 
         // Create a Xuggler container object
         IContainer container = IContainer.make();
         IStreamCoder videoCoder = null;
 
         try
         {
             // Open up the container
             if (container.open(filename, IContainer.Type.READ, null) < 0)
             {
                 throw new IllegalArgumentException("could not open file: " + filename);
             }
 
             // query how many streams the call to open found
             int numStreams = container.getNumStreams();
 
             // and iterate through the streams to find the first video stream
             int videoStreamId = -1;
             for (int i = 0; i < numStreams; i++)
             {
                 // Find the stream object
                 IStream stream = container.getStream(i);
                 // Get the pre-configured decoder that can decode this stream;
                 IStreamCoder coder = stream.getStreamCoder();
 
                 if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
                 {
                     videoStreamId = i;
                     videoCoder = coder;
                     break;
                 }
             }
             if (videoStreamId == -1)
             {
                 throw new RuntimeException("could not find video stream in container: " + filename);
             }
 
             /*
             * Now we have found the video stream in this file.  Let's open up our decoder so it can
             * do work.
             */
             if (videoCoder.open() < 0)
             {
                 throw new RuntimeException("could not open video decoder for container: " + filename);
             }
 
             IVideoResampler resampler = null;
             if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24)
             {
                 // if this stream is not in BGR24, we're going to need to
                 // convert it.  The VideoResampler does that for us.
                 resampler = IVideoResampler.make(videoCoder.getWidth(),
                         videoCoder.getHeight(), IPixelFormat.Type.BGR24,
                         videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
                 if (resampler == null)
                 {
                     throw new RuntimeException("could not create color space resampler for: " + filename);
                 }
             }
 
             // Seek to the next key frame near the specified position
            long streamDuration = container.getStream(videoStreamId).getDuration();
            if (streamDuration <= 0)
            {
                // If unable to read duration, try to seek to 2 minutes
                streamDuration = 1000 * 60 * 2;
            }
            int seekResult = container.seekKeyFrame(videoStreamId, (long) (streamDuration * (aPosition / 100.0f)),
                     IURLProtocolHandler.SEEK_SET);
             if (seekResult < 0)
             {
                 throw new RuntimeException("could not seek to specified position: " + filename);
             }
 
             /*
             * Now, we start walking through the container looking at first packet after seeking.
             */
             IPacket packet = IPacket.make();
             while (container.readNextPacket(packet) >= 0)
             {
                 /*
                 * Now we have a packet, let's see if it belongs to our video stream
                 */
                 if (packet.getStreamIndex() == videoStreamId && packet.isKeyPacket())
                 {
                     /*
                     * We allocate a new picture to get the data out of Xuggler
                     */
                     IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),
                             videoCoder.getWidth(), videoCoder.getHeight());
 
                     int offset = 0;
                     while (offset < packet.getSize())
                     {
                         /*
                         * Now, we decode the video, checking for any errors.
                         *
                         */
                         int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
                         if (bytesDecoded < 0)
                         {
                             throw new RuntimeException("got error decoding video in: " + filename);
                         }
                         offset += bytesDecoded;
 
                         /*
                         * Some decoders will consume data in a packet, but will not be able to construct
                         * a full video picture yet.  Therefore you should always check if you
                         * got a complete picture from the decoder
                         */
                         if (picture.isComplete())
                         {
                             IVideoPicture newPic = picture;
 
                             /*
                             * If the resampler is not null, that means we didn't get the
                             * video in BGR24 format and
                             * need to convert it into BGR24 format.
                             */
                             if (resampler != null)
                             {
                                 // we must resample
                                 newPic = IVideoPicture.make(resampler.getOutputPixelFormat(),
                                         picture.getWidth(), picture.getHeight());
                                 if (resampler.resample(newPic, picture) < 0)
                                 {
                                     throw new RuntimeException("could not resample video from: " + filename);
                                 }
                             }
                             if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
                             {
                                 throw new RuntimeException("could not decode video" +
                                         " as BGR 24 bit data in: " + filename);
                             }
 
                             // And finally, convert the BGR24 to an Java buffered image
                             BufferedImage javaImage = Utils.videoPictureToImage(newPic);
                             return javaImage;
                         }
                     }
                 }
                 else
                 {
                     /*
                     * This packet isn't part of our video stream, so we just
                     * silently drop it.
                     */
                     do
                     {
                     }
                     while (false);
                 }
 
             }
         }
         finally
         {
             if (videoCoder != null)
             {
                 videoCoder.close();
             }
             if (container != null)
             {
                 container.close();
             }
         }
 
         return null;
     }
 
 }
