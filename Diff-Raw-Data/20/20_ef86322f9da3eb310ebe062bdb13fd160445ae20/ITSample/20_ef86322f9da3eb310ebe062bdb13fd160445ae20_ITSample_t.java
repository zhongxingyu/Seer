 package ca.mrb0.hydrocitee.it;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Arrays;
 
 import org.apache.log4j.Logger;
 
 import com.google.common.base.Optional;
 
 import ca.mrb0.hydrocitee.it.ITSampleData.CompressionType;
 import ca.mrb0.hydrocitee.util.Streams;
 
 public class ITSample {
     private static Logger l = Logger.getLogger(ITInstrument.class);
     
     public enum VibratoWaveform {
         Sine,
         Ramp,
         Square,
         Random
     };
     
     public final String filename;
     public final String sampleName;
     
     public final int globalVol;
     public final int defaultVol;
     
     public final boolean _16bit;
     public final boolean stereo;
     public final boolean loopEnabled;
     public final boolean susloopEnabled;
     public final boolean loopPingPong;
     public final boolean susloopPingPong;
     
     public final long loopBegin;
     public final long loopEnd;
     public final long susloopBegin;
     public final long susloopEnd;
     
     public final long c5speed;
     
     public final int vibrSpeed;
     public final int vibrDepth;
     public final int vibrRate;
     public final VibratoWaveform vibrWaveform;
     
     public final boolean defaultPanEnabled;
     public final int defaultPan;
     
     public final Optional<ITSampleData> sampleData;
 
     public ITSample(String filename, String sampleName, int globalVol,
             int defaultVol, boolean _16bit, boolean stereo,
             boolean loopEnabled, boolean susloopEnabled, boolean loopPingPong,
             boolean susloopPingPong, long loopBegin, long loopEnd,
             long susloopBegin, long susloopEnd, long c5speed, int vibrSpeed,
             int vibrDepth, int vibrRate, VibratoWaveform vibrWaveform,
             boolean defaultPanEnabled, int defaultPan, Optional<ITSampleData> sampleData) {
         super();
         this.filename = filename;
         this.sampleName = sampleName;
         this.globalVol = globalVol;
         this.defaultVol = defaultVol;
         this._16bit = _16bit;
         this.stereo = stereo;
         this.loopEnabled = loopEnabled;
         this.susloopEnabled = susloopEnabled;
         this.loopPingPong = loopPingPong;
         this.susloopPingPong = susloopPingPong;
         this.loopBegin = loopBegin;
         this.loopEnd = loopEnd;
         this.susloopBegin = susloopBegin;
         this.susloopEnd = susloopEnd;
         this.c5speed = c5speed;
         this.vibrSpeed = vibrSpeed;
         this.vibrDepth = vibrDepth;
         this.vibrRate = vibrRate;
         this.vibrWaveform = vibrWaveform;
         this.defaultPanEnabled = defaultPanEnabled;
         this.defaultPan = defaultPan;
        this.sampleData = sampleData;
     }
     
     public ITSample() {
         this("", "", 64, 64, false, false, false, false, false, false, 0, 0, 0, 0, 8363, 0, 0, 0, VibratoWaveform.Sine, false, 0, Optional.<ITSampleData>absent());
     }
     
     public static ITSample newFromData(byte[] data, int offs, int fileOffsAdj) {
         String filename;
         String sampleName;
         
         int globalVol;
         int defaultVol;
         
         boolean _16bit;
         boolean stereo;
         boolean loopEnabled;
         boolean susloopEnabled;
         boolean loopPingPong;
         boolean susloopPingPong;
         
         long loopBegin;
         long loopEnd;
         long susloopBegin;
         long susloopEnd;
         
         long c5speed;
         
         int vibrSpeed;
         int vibrDepth;
         int vibrRate;
         VibratoWaveform vibrWaveform;
         
         boolean defaultPanEnabled;
         int defaultPan;
         
         Optional<ITSampleData> sampleData;
         
         if (!Arrays.equals(Arrays.copyOfRange(data, offs, offs + 4), new byte[] { 'I', 'M', 'P', 'S' })) {
             throw new IllegalArgumentException(String.format("sample at 0x%x had broken imps", offs));
         }
         offs += 4;
         
         byte[] rawFilename = Arrays.copyOfRange(data, offs, offs + 12);
         int nul = Streams.arrayIndexOf(rawFilename, (byte)0);
         if (nul == -1) {
             nul = rawFilename.length;
         }
         rawFilename = Arrays.copyOfRange(rawFilename, 0, nul);
         try {
             filename = new String(rawFilename, "windows-1252");
         } catch(UnsupportedEncodingException e) {
             // hopefully shouldn't happen as we are using a hardcoded encoding
             l.error("Unsupported encoding: " + e.toString());
             throw new RuntimeException(e);
         }
         offs += 12;
         
         offs++;
         
         globalVol = 0xff & data[offs++];
         
         int flags = 0xff & data[offs++];
         boolean hasSampleData = (flags & 0x1) != 0;
         _16bit = (flags & 0x2) != 0;
         stereo = (flags & 0x4) != 0;
         boolean isCompressed = (flags & 0x8) != 0;
         loopEnabled = (flags & 0x10) != 0;
         susloopEnabled = (flags & 0x20) != 0;
         loopPingPong = (flags & 0x40) != 0;
         susloopPingPong = (flags & 0x80) != 0;
         
         defaultVol = 0xff & data[offs++];
         
         byte[] smpname = Arrays.copyOfRange(data, offs, offs + 26);
         nul = Streams.arrayIndexOf(smpname, (byte)0);
         if (nul == -1) {
             nul = smpname.length;
         }
         smpname = Arrays.copyOf(smpname, nul);
         try {
             sampleName = new String(smpname, "windows-1252");
         } catch(UnsupportedEncodingException e) {
             // hopefully shouldn't happen as we are using a hardcoded encoding
             l.error("Unsupported encoding: " + e.toString());
             throw new RuntimeException(e);
         }
         offs += 26;
         
         int cvt = 0xff & data[offs++];
         defaultPan = 0x7f & data[offs];
         defaultPanEnabled = (data[offs] & 0x80) != 0;
         offs++;
         
         long dataLengthSamples = Streams.unpack32(data, offs);
         offs += 4;
         
         loopBegin = Streams.unpack32(data, offs);
         offs += 4;
         
         loopEnd = Streams.unpack32(data, offs);
         offs += 4;
         
         c5speed = Streams.unpack32(data, offs);
         offs += 4;
         
         susloopBegin = Streams.unpack32(data, offs);
         offs += 4;
         
         susloopEnd = Streams.unpack32(data, offs);
         offs += 4;
         
         long sampleOffset = Streams.unpack32(data, offs) - fileOffsAdj;
         offs += 4;
         
         vibrSpeed = 0xff & data[offs++];
         vibrDepth = 0xff & data[offs++];
         vibrRate = 0xff & data[offs++];
         vibrWaveform = VibratoWaveform.values()[0xff & data[offs++]];
         
         if (hasSampleData) {
             CompressionType ctype;
             if (isCompressed) {
                 if ((cvt & 0x04) != 0) {
                     ctype = CompressionType.IT215;
                 } else {
                     ctype = CompressionType.IT214;
                 }
             } else {
                 ctype = CompressionType.Uncompressed;
             }
             
             l.debug(String.format("Load sample data: %s, compression=%s, %d-bit", sampleName, ctype.toString(), _16bit ? 16 : 8));
             sampleData = Optional.of(ITSampleData.newFromData(data, (int)sampleOffset, (int)dataLengthSamples, _16bit, ctype));
         } else {
             sampleData = Optional.absent();
         }
         
         return new ITSample(filename, sampleName, globalVol, defaultVol, _16bit, stereo, loopEnabled, susloopEnabled, loopPingPong, susloopPingPong, loopBegin, loopEnd, susloopBegin, susloopEnd, c5speed, vibrSpeed, vibrDepth, vibrRate, vibrWaveform, defaultPanEnabled, defaultPan, sampleData);
     }
 }
