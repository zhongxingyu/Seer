 package de.uniluebeck.itm.kma.xuggler;
 
 import com.xuggle.xuggler.Global;
 import com.xuggle.xuggler.ICodec;
 
 import javax.swing.JTextPane;
 
 import com.xuggle.xuggler.IContainer;
 import com.xuggle.xuggler.IPacket;
 import com.xuggle.xuggler.IStream;
 import com.xuggle.xuggler.IStreamCoder;
 import de.uniluebeck.itm.kma.LoggingHelper;
 
 /**
  * Analyzer for multimedia files. Outputs information to
  * a predefined JTextPane as plain text.
  *
  * @author seidel
  */
 public class FileAnalyzer
 {
   /** JTextPane object for analyzer output */
   private JTextPane logPane;
 
   /**
    * Constructor sets logPane member.
    *
    * @param logPane JTextPane object
    */
   public FileAnalyzer(JTextPane logPane)
   {
     this.logPane = logPane;
 
     this.log("Welcome to Media Converter. Please select input file.");
     this.log("");
     this.log("Your JVM is running on " + System.getProperty("sun.arch.data.model") + " bit.");
     this.log("");
   }
 
   /**
    * Setter for logPane member.
    *
    * @param logPane JTextPane object
    */
   public void setLogPane(JTextPane logPane)
   {
     this.logPane = logPane;
   }
 
   /**
    * Getter for logPane member.
    *
    * @return JTextPane object
    */
   public JTextPane getLogPane()
   {
     return this.logPane;
   }
 
   /**
    * Helper method for logging, provided for convenience.
    *
    * @param textToAppend Text to append to log pane
    */
   private void log(String textToAppend)
   {
     LoggingHelper.appendAndScroll(this.logPane, textToAppend);
   }
 
   /**
    * This method analyzes a multimedia file and outputs
    * information about it on the log pane which was set
    * in the constructor.
    *
    * @param fileName File name
    */
   public void analyzeMediafile(String fileName)
   {
     this.log("============================================================");
     this.log("");
     this.log("Analyzing metadata from file: \t'" + fileName + "'");
     this.log("");
 
     // Create local variables for Xuggler
     int numStreams = 0;
     IContainer container = IContainer.make();
 
     // Open container and read file
     if (container.open(fileName, IContainer.Type.READ, null) < 0)
     {
       throw new IllegalArgumentException("Could not open file: '" + fileName + "'");
     }
     this.log("Container opened for analysis...");
     this.log("");
 
     // Analyze basic file information
     numStreams = container.getNumStreams();
     this.log("Number of streams: \t\t" + numStreams);
 
     String duration = (container.getDuration() == Global.NO_PTS ? "unknown" : (container.getDuration() / 1000) + " ms");
     this.log("Duration: \t\t\t" + duration);
 
     this.log("File size: \t\t\t" + container.getFileSize() + " Bytes");
 
     this.log("Bitrate: \t\t\t" + container.getBitRate() + " Bit/s");
     this.log("");
 
     // Analyze streams
     this.log("Now analizing streams...");
     this.log("");
 
     // Analyze all IStreams within the IContainer
     for (int i = 0; i < numStreams; i++)
     {
       // Get stream
       IStream stream = container.getStream(i);
 
       // Get stream coder
       IStreamCoder coder = stream.getStreamCoder();
 
       this.log("------------------------------------------------------------");
       this.log("");
       this.log("New stream found...");
       this.log("");
 
       this.log("Codec type: \t\t" + coder.getCodecType());
 
       this.log("Codec ID: \t\t\t" + coder.getCodecID());
 
       String streamDuration = (stream.getDuration() == Global.NO_PTS ? "unknown" : String.valueOf(stream.getDuration()));
       this.log("Duration: \t\t\t" + streamDuration + " time units");
 
       String startTime = (stream.getStartTime() == Global.NO_PTS ? "unknown" : String.valueOf(stream.getStartTime()));
       this.log("Start time: \t\t\t" + startTime);
 
       this.log("Language: \t\t\t" + (stream.getLanguage() == null ? "unknown" : stream.getLanguage()));
 
       this.log("IStream time-base: \t\t" + stream.getTimeBase().getNumerator() + " (Numerator), "
               + stream.getTimeBase().getDenominator() + " (Denominator)");
 
       this.log("IStreamCoder time-base: \t" + coder.getTimeBase().getNumerator() + " (Numerator), "
               + coder.getTimeBase().getDenominator() + " (Denominator)");
       this.log("");
 
       this.log("Checking stream specifics...");
       this.log("");
 
       // Analyze information, which depend on file type (audio vs. video)
       if (coder.getCodecType().equals(ICodec.Type.CODEC_TYPE_AUDIO))
       {
         this.log("Media file type: \t\tAudio");
 
        this.log("Sample rate: \t\t" + coder.getSampleRate());
 
         this.log("Number of channels: \t\t" + coder.getChannels());
 
         this.log("Sample format: \t\t" + coder.getSampleFormat());
       }
       else if (coder.getCodecType().equals(ICodec.Type.CODEC_TYPE_VIDEO))
       {
         this.log("Media file type: \t\tVideo");
 
         this.log("Frame measurements: \t" + coder.getHeight() + " (height), " + coder.getWidth() + " (width)");
 
         this.log("Format (pixel type): \t\t" + coder.getPixelType());
 
         this.log("Frame rate: \t\t" + coder.getFrameRate().getDouble() + " fps");
       }
       this.log("");
     }
 
     // Close container and release resources
     this.closeContainerSafely(container);
   }
 
   /**
    * This method closes the IContainer object and releases
    * all used resources.
    *
    * @param container IContainer object
    */
   private void closeContainerSafely(IContainer container)
   {
     // Create local variables for Xuggler
     int i;
     int numStreams = container.getNumStreams();
 
     // Do some fancy stuff...
     if (container.getType() == IContainer.Type.WRITE)
     {
       for (i = 0; i < numStreams; i++)
       {
         IStreamCoder c = container.getStream(i).getStreamCoder();
         if (c != null)
         {
           IPacket oPacket = IPacket.make();
           if (c.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
           {
             c.encodeVideo(oPacket, null, 0);
           }
           if (oPacket.isComplete())
           {
             container.writePacket(oPacket, true);
           }
         }
       }
     }
 
     if (container.getType() == IContainer.Type.WRITE)
     {
       int retval = container.writeTrailer();
       if (retval < 0)
       {
         throw new RuntimeException("Could not write trailer to output file.");
       }
     }
 
     // Release all IStreamCoder resources
     for (i = 0; i < numStreams; i++)
     {
       IStreamCoder c = container.getStream(i).getStreamCoder();
       if (c != null)
       {
         c.close();
       }
     }
 
     // Close IContainer object and free memory
     container.close();
     container = null;
 
     this.log("------------------------------------------------------------");
     this.log("");
     this.log("Container successfully closed.");
     this.log("");
   }
 }
