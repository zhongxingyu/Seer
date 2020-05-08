 package info.carlwithak.mpxg2.sysex;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  *
  * @author carl
  */
 public class SysexParser {
 
     private static final int SYSEX_ID_START = 0xf0;
     private static final int LEXICON_MANUFACTURER_ID = 0x06;
     private static final int MPXG2_PRODUCT_ID = 0x0f;
     private static final int DATA_MESSAGE_TYPE = 0x01;
     // effect type constants
     private static final int EFFECT_TYPE_CHORUS = 0x0001;
     private static final int EFFECT_TYPE_DELAY = 0x0002;
     private static final int EFFECT_TYPE_DISTORTION = 0x0004;
     private static final int EFFECT_TYPE_EQ = 0x0008;
     private static final int EFFECT_TYPE_FLANGER = 0x0010;
     private static final int EFFECT_TYPE_GAIN = 0x0020;
     private static final int EFFECT_TYPE_MOD = 0x0040;
     private static final int EFFECT_TYPE_OVERDRIVE = 0x0080;
     private static final int EFFECT_TYPE_PHASER = 0x0100;
     private static final int EFFECT_TYPE_PITCH = 0x0200;
     private static final int EFFECT_TYPE_REVERB = 0x0400;
     private static final int EFFECT_TYPE_SPEAKERSIM = 0x0800;
     private static final int EFFECT_TYPE_WAH = 0x1000;
     private static final int APP_TYPE_PREPOST = 0x2000;
     private static final int APP_TYPE_STANDALONE = 0x4000;
     private static final int APP_TYPE_INLINE = 0x8000;
     // guitar style constants
     private static final int GUITAR_STYLE_ACOUSTIC = 0x02;
     private static final int GUITAR_STYLE_BASS = 0x04;
     private static final int GUITAR_STYLE_BLUES = 0x08;
     private static final int GUITAR_STYLE_CLEAN = 0x10;
     private static final int GUITAR_STYLE_COUNTRY = 0x20;
     private static final int GUITAR_STYLE_JAZZ = 0x40;
     private static final int GUITAR_STYLE_ROCK = 0x80;
 
     /**
      * Parse a Program dumped in SysEx format.
      *
      * See {@link http://www.stecrecords.com/gear/mpxg2/doc/MPXG2_MIDI_Impl.htm}
      */
     static Program parseProgram(final File preset) throws IOException, ParseException {
         InputStream in = new FileInputStream(preset);
 
         Program program = new Program();
 
         int b;
         if ((b = in.read()) != SYSEX_ID_START) {
             throw new ParseException("Invalid Sysex ID (start)");
         }
         if ((b = in.read()) != LEXICON_MANUFACTURER_ID) {
             throw new ParseException("Invalid Manufacturer ID");
         }
         if ((b = in.read()) != MPXG2_PRODUCT_ID) {
             throw new ParseException("Invalid Product ID");
         }
         b = in.read();
         @SuppressWarnings("unused")
         int deviceId = b;
 
         if ((b = in.read()) != DATA_MESSAGE_TYPE) {
             throw new ParseException("Invalid Message Type");
         }
 
         byte[] bytes = new byte[4];
         in.read(bytes);
         @SuppressWarnings("unused")
         int objectSize = 0;
         for (int i = 0; i < bytes.length; i++) {
             objectSize += (bytes[i] * Math.pow(16, i));
         }
 
         // effect 1 parameters
         bytes = new byte[2];
         in.read(bytes);
         int effect1Mix = bytes[0] + bytes[1] * 16;
         program.setEffect1Mix(effect1Mix);
 
         in.read(bytes);
         int effect1Level = bytes[0] + bytes[1] * 16;
         program.setEffect1Level(effect1Level);
 
         in.read(bytes);
         int effect1Rate = bytes[0] + bytes[1] * 16;
         program.setEffect1Rate(effect1Rate);
 
         // unused
         in.read(new byte[29 * 2]);
 
        // effect 1 parameters
         bytes = new byte[2];
         in.read(bytes);
         int effect2Mix = bytes[0] + bytes[1] * 16;
         program.setEffect2Mix(effect2Mix);
 
         in.read(bytes);
         int effect2Level = bytes[0] + bytes[1] * 16;
         program.setEffect2Level(effect2Level);
 
         in.read(bytes);
         int effect2Bass = bytes[0] + bytes[1] * 16;
         program.setEffect2Bass(effect2Bass);
 
         in.read(bytes);
         int effect2Type = bytes[0] + bytes[1] * 16;
         program.setEffect2Type(effect2Type);
 
         in.read(bytes);
         int effect2Response = bytes[0] + bytes[1] * 16;
         program.setEffect2Response(effect2Response);
 
         in.read(bytes);
         int effect2Gain = bytes[0] + bytes[1] * 16;
         program.setEffect2Gain(effect2Gain);
 
         // unused
         in.read(new byte[26 * 2]);
 
         // chorus parameters
         bytes = new byte[2];
         in.read(bytes);
         int chorusMix = bytes[0] + bytes[1] * 16;
         program.setChorusMix(chorusMix);
 
         in.read(bytes);
         int chorusLevel = bytes[0] + bytes[1] * 16;
         program.setChorusLevel(chorusLevel);
 
         // unused
         in.read(new byte[30 * 2]);
 
         // delay parameters
         bytes = new byte[2];
         in.read(bytes);
         int delayMix = bytes[0] + bytes[1] * 16;
         program.setDelayMix(delayMix);
 
         in.read(bytes);
         int delayLevel = bytes[0] + bytes[1] * 16;
         program.setDelayLevel(delayLevel);
 
         in.read(bytes);
         int delayTime1Beat = bytes[0] + bytes[1] * 16;
         program.setDelayTime1Beat(delayTime1Beat);
 
         in.read(bytes);
         int delayTime1Echoes = bytes[0] + bytes[1] * 16;
         program.setDelayTime1Echoes(delayTime1Echoes);
 
         in.read(bytes);
         int delayTime2Beat = bytes[0] + bytes[1] * 16;
         program.setDelayTime2Beat(delayTime2Beat);
 
         in.read(bytes);
         int delayTime2Echoes = bytes[0] + bytes[1] * 16;
         program.setDelayTime2Echoes(delayTime2Echoes);
 
         // TODO what is this?
         in.read(new byte[4]);
 
         in.read(bytes);
         int delayLevel1 = bytes[0] + bytes[1] * 16;
         program.setDelayLevel1(delayLevel1);
 
         in.read(bytes);
         int delayLevel2 = bytes[0] + bytes[1] * 16;
         program.setDelayLevel2(delayLevel2);
 
         in.read(bytes);
         int delayFeedback1 = bytes[0] + bytes[1] * 16;
         program.setDelayFeedback1(delayFeedback1);
 
         in.read(bytes);
         int delayInsert = bytes[0] + bytes[1] * 16;
         program.setDelayInsert(delayInsert);
 
         in.read(bytes);
         int delayFeedback2 = bytes[0] + bytes[1] * 16;
         program.setDelayFeedback2(delayFeedback2);
 
         in.read(bytes);
         int delayDamp1 = bytes[0] + bytes[1] * 16;
         program.setDelayDamp1(delayDamp1);
 
         in.read(bytes);
         int delayDamp2 = bytes[0] + bytes[1] * 16;
         program.setDelayDamp2(delayDamp2);
 
         in.read(bytes);
         int delayClear = bytes[0] + bytes[1] * 16;
         program.setDelayClear(delayClear);
 
         // unused
         in.read(new byte[16 * 2]);
 
         // reverb parameters
         bytes = new byte[2];
         in.read(bytes);
         int reverbMix = bytes[0] + bytes[1] * 16;
         program.setReverbMix(reverbMix);
 
         in.read(bytes);
         int reverbLevel = bytes[0] + bytes[1] * 16;
         program.setReverbLevel(reverbLevel);
 
         in.read(bytes);
         double reverbSize = (bytes[0] + bytes[1] * 16) / 2.0 + 4;
         program.setReverbSize(reverbSize);
 
         in.read(bytes);
         int reverbLink = bytes[0] + bytes[1] * 16;
         program.setReverbLink(reverbLink);
 
         in.read(bytes);
         int reverbDiff = (bytes[0] + bytes[1] * 16) * 2;
         program.setReverbDiff(reverbDiff);
 
         in.read(bytes);
         int reverbPreDelay = bytes[0] + bytes[1] * 16;
         program.setReverbPreDelay(reverbPreDelay);
 
         in.read(bytes);
         int reverbDelayTime = bytes[0] + bytes[1] * 16;
         program.setReverbDelayTime(reverbDelayTime);
 
         in.read(bytes);
         int reverbDelayLevel = bytes[0] + bytes[1] * 16;
         program.setReverbDelayLevel(reverbDelayLevel);
 
         in.read(bytes);
         int reverbRtHC = bytes[0] + bytes[1] * 16;
         program.setReverbRtHC(reverbRtHC);
 
         // unused
         in.read(new byte[23 * 2]);
 
         // TODO eq parameters
         in.read(new byte[32 * 2]);
 
         // gain parameters
         bytes = new byte[2];
         in.read(bytes);
         int gainLo = bytes[0] + bytes[1] * 16;
         program.setGainLo(gainLo);
 
         in.read(bytes);
         int gainMid = bytes[0] + bytes[1] * 16;
         program.setGainMid(gainMid);
 
         in.read(bytes);
         int gainHi = bytes[0] + bytes[1] * 16;
         program.setGainHi(gainHi);
 
         in.read(bytes);
         int gainDrive = bytes[0] + bytes[1] * 16;
         program.setGainDrive(gainDrive);
 
         in.read(bytes);
         int gainTone = bytes[0] + bytes[1] * 16;
         program.setGainTone(gainTone);
 
         in.read(bytes);
         int gainLevel = bytes[0] + bytes[1] * 16;
         program.setGainLevel(gainLevel);
 
         // unused
         in.read(new byte[26 * 2]);
 
         // TODO
         bytes = new byte[6];
         in.read(bytes);
 
         int effectTypes = 0;
         for (int i = 0; i < 4; i++) {
             effectTypes += (bytes[i] * Math.pow(16, i));
         }
         program.setIsChorus((effectTypes & EFFECT_TYPE_CHORUS) == EFFECT_TYPE_CHORUS);
         program.setIsDelay((effectTypes & EFFECT_TYPE_DELAY) == EFFECT_TYPE_DELAY);
         program.setIsDistortion((effectTypes & EFFECT_TYPE_DISTORTION) == EFFECT_TYPE_DISTORTION);
         program.setIsEq((effectTypes & EFFECT_TYPE_EQ) == EFFECT_TYPE_EQ);
         program.setIsFlanger((effectTypes & EFFECT_TYPE_FLANGER) == EFFECT_TYPE_FLANGER);
         program.setIsGain((effectTypes & EFFECT_TYPE_GAIN) == EFFECT_TYPE_GAIN);
         program.setIsMod((effectTypes & EFFECT_TYPE_MOD) == EFFECT_TYPE_MOD);
         program.setIsOverdrive((effectTypes & EFFECT_TYPE_OVERDRIVE) == EFFECT_TYPE_OVERDRIVE);
         program.setIsPhaser((effectTypes & EFFECT_TYPE_PHASER) == EFFECT_TYPE_PHASER);
         program.setIsPitch((effectTypes & EFFECT_TYPE_PITCH) == EFFECT_TYPE_PITCH);
         program.setIsReverb((effectTypes & EFFECT_TYPE_REVERB) == EFFECT_TYPE_REVERB);
         program.setIsSpeakerSim((effectTypes & EFFECT_TYPE_SPEAKERSIM) == EFFECT_TYPE_SPEAKERSIM);
         program.setIsWah((effectTypes & EFFECT_TYPE_WAH) == EFFECT_TYPE_WAH);
         program.setIsPrePost((effectTypes & APP_TYPE_PREPOST) == APP_TYPE_PREPOST);
         program.setIsStandAlone((effectTypes & APP_TYPE_STANDALONE) == APP_TYPE_STANDALONE);
         program.setIsInline((effectTypes & APP_TYPE_INLINE) == APP_TYPE_INLINE);
 
         int guitarStyle = 0;
         for (int i = 0; i < 2; i++) {
             guitarStyle += (bytes[i + 4] * Math.pow(16, i));
         }
         program.setIsAcoustic((guitarStyle & GUITAR_STYLE_ACOUSTIC) == GUITAR_STYLE_ACOUSTIC);
         program.setIsBass((guitarStyle & GUITAR_STYLE_BASS) == GUITAR_STYLE_BASS);
         program.setIsBlues((guitarStyle & GUITAR_STYLE_BLUES) == GUITAR_STYLE_BLUES);
         program.setIsClean((guitarStyle & GUITAR_STYLE_CLEAN) == GUITAR_STYLE_CLEAN);
         program.setIsCountry((guitarStyle & GUITAR_STYLE_COUNTRY) == GUITAR_STYLE_COUNTRY);
         program.setIsJazz((guitarStyle & GUITAR_STYLE_JAZZ) == GUITAR_STYLE_JAZZ);
         program.setIsRock((guitarStyle & GUITAR_STYLE_ROCK) == GUITAR_STYLE_ROCK);
 
         StringBuilder sb = new StringBuilder(17);
         for (int i = 0; i < 9; i++) {
             bytes = new byte[10];
             in.read(bytes);
             int effect = bytes[0] + bytes[1] * 16;
             @SuppressWarnings("unused")
             int upperInputConn = bytes[2] + bytes[3] * 16;
             @SuppressWarnings("unused")
             int lowerInputConn = bytes[4] + bytes[5] * 16;
             @SuppressWarnings("unused")
             int routing = bytes[6] + bytes[7] * 16;
             @SuppressWarnings("unused")
             int pathType = bytes[8] + bytes[9] * 16;
             switch (effect) {
                 case 0:
                     sb.append("=1");
                     break;
                 case 1:
                     sb.append("=2");
                     break;
                 case 2:
                     sb.append("=C");
                     break;
                 case 3:
                     sb.append("=D");
                     break;
                 case 4:
                     sb.append("=R");
                     break;
                 case 5:
                     sb.append("=E");
                     break;
                 case 6:
                     sb.append("=G");
                     break;
                 case 7:
                     sb.append("=O");
                     break;
                 case 8:
                     sb.append("I");
                     break;
             }
         }
         program.setRouting(sb.toString());
 
         // TODO skip 7 bytes of data for now
         for (int i = 0; i < 7 * 2; i++) {
             in.read();
         }
 
         for (int i = 0; i < 7; i++) {
             bytes = new byte[2];
             in.read(bytes);
             int algorithmNumber = bytes[0] + bytes[1] * 16;
             switch (i) {
                 case 0:
                     program.setEffect1Algorithm(algorithmNumber);
                     break;
                 case 1:
                     program.setEffect2Algorithm(algorithmNumber);
                     break;
                 case 2:
                     program.setChorusAlgorithm(algorithmNumber);
                     break;
                 case 3:
                     program.setDelayAlgorithm(algorithmNumber);
                     break;
                 case 4:
                     program.setReverbAlgorithm(algorithmNumber);
                     break;
                 case 5:
                     program.setEqAlgorithm(algorithmNumber);
                     break;
                 case 6:
                     program.setGainAlgorithm(algorithmNumber);
                     break;
             }
         }
 
         // read program name
         bytes = new byte[24];
         in.read(bytes);
         sb = new StringBuilder(12);
         for (int i = 0; i < bytes.length; i += 2) {
             char c = (char) (bytes[i] + (bytes[i + 1] * 16));
             sb.append(c);
         }
         program.setProgramName(sb.toString().trim());
 
         // TODO skip 21 bytes of data for now
         for (int i = 0; i < 21 * 2; i++) {
             in.read();
         }
 
         // tempo
         bytes = new byte[4];
         in.read(bytes);
         int tempo = 0;
         for (int i = 0; i < 4; i++) {
             tempo += (bytes[i] * Math.pow(16, i));
         }
         program.setTempo(tempo);
 
         bytes = new byte[2];
         in.read(bytes);
         int tempoSource = bytes[0] + (bytes[1] * 16);
         program.setTempoSource(tempoSource);
 
         in.read(bytes);
         int beatValue = bytes[0] + (bytes[1] * 16);
         program.setBeatValue(beatValue);
 
         in.read(bytes);
         int tapSource = bytes[0] + (bytes[1] * 16);
         program.setTapSource(tapSource);
 
         in.read(bytes);
         int tapAverage = bytes[0] + (bytes[1] * 16);
         program.setTapAverage(tapAverage);
 
         in.read(bytes);
         int tapSourceLevel = bytes[0] + (bytes[1] * 16);
         program.setTapSourceLevel(tapSourceLevel);
 
         // unused
         in.read(new byte[2]);
 
         // patching 1
         bytes = new byte[2];
         in.read(bytes);
         int patch1Source = bytes[0] + (bytes[1] * 16);
         program.setPatch1Source(patch1Source);
 
         in.read(bytes);
         int patch1SourceMin = bytes[0] + (bytes[1] * 16);
         program.setPatch1SourceMin(patch1SourceMin);
 
         in.read(bytes);
         int patch1SourceMid = bytes[0] + (bytes[1] * 16);
         program.setPatch1SourceMid(patch1SourceMid);
 
         in.read(bytes);
         int patch1SourceMax = bytes[0] + (bytes[1] * 16);
         program.setPatch1SourceMax(patch1SourceMax);
 
         in.read(bytes);
         int patch1DestinationEffect = bytes[0] + (bytes[1] * 16);
         program.setPatch1DestinationEffect(patch1DestinationEffect);
 
         in.read(bytes);
         int patch1DestinationParameter = bytes[0] + (bytes[1] * 16);
         program.setPatch1DestinationParameter(patch1DestinationParameter);
 
         bytes = new byte[4];
         in.read(bytes);
         int patch1DestinationMin = 0;
         for (int i = 0; i < 4; i++) {
             patch1DestinationMin += (bytes[i] * Math.pow(16, i));
         }
         program.setPatch1DestinationMin(patch1DestinationMin);
 
         // TODO find out what goes here
         bytes = new byte[2];
         in.read(bytes);
 
         bytes = new byte[2];
         in.read(bytes);
         int patch1DestinationMid = bytes[0] + (bytes[1] * 16);
         program.setPatch1DestinationMid(patch1DestinationMid);
 
         bytes = new byte[4];
         in.read(bytes);
         int patch1DestinationMax = 0;
         for (int i = 0; i < 4; i++) {
             patch1DestinationMax += (bytes[i] * Math.pow(16, i));
         }
         program.setPatch1DestinationMax(patch1DestinationMax);
 
         // patching 2
         bytes = new byte[2];
         in.read(bytes);
         int patch2Source = bytes[0] + (bytes[1] * 16);
         program.setPatch2Source(patch2Source);
 
         in.read(bytes);
         int patch2SourceMin = bytes[0] + (bytes[1] * 16);
         program.setPatch2SourceMin(patch2SourceMin);
 
         in.read(bytes);
         int patch2SourceMid = bytes[0] + (bytes[1] * 16);
         program.setPatch2SourceMid(patch2SourceMid);
 
         in.read(bytes);
         int patch2SourceMax = bytes[0] + (bytes[1] * 16);
         program.setPatch2SourceMax(patch2SourceMax);
 
         in.read(bytes);
         int patch2DestinationEffect = bytes[0] + (bytes[1] * 16);
         program.setPatch2DestinationEffect(patch2DestinationEffect);
 
         in.read(bytes);
         int patch2DestinationParameter = bytes[0] + (bytes[1] * 16);
         program.setPatch2DestinationParameter(patch2DestinationParameter);
 
         bytes = new byte[4];
         in.read(bytes);
         int patch2DestinationMin = 0;
         for (int i = 0; i < 4; i++) {
             patch2DestinationMin += (bytes[i] * Math.pow(16, i));
         }
         program.setPatch2DestinationMin(patch2DestinationMin);
 
         // TODO find out what goes here
         bytes = new byte[2];
         in.read(bytes);
 
         bytes = new byte[2];
         in.read(bytes);
         int patch2DestinationMid = bytes[0] + (bytes[1] * 16);
         program.setPatch2DestinationMid(patch2DestinationMid);
 
         bytes = new byte[4];
         in.read(bytes);
         int patch2DestinationMax = 0;
         for (int i = 0; i < 4; i++) {
             patch2DestinationMax += (bytes[i] * Math.pow(16, i));
         }
         program.setPatch2DestinationMax(patch2DestinationMax);
 
         // patching 3
         bytes = new byte[2];
         in.read(bytes);
         int patch3Source = bytes[0] + (bytes[1] * 16);
         program.setPatch3Source(patch3Source);
 
         in.read(bytes);
         int patch3SourceMin = bytes[0] + (bytes[1] * 16);
         program.setPatch3SourceMin(patch3SourceMin);
 
         in.read(bytes);
         int patch3SourceMid = bytes[0] + (bytes[1] * 16);
         program.setPatch3SourceMid(patch3SourceMid);
 
         in.read(bytes);
         int patch3SourceMax = bytes[0] + (bytes[1] * 16);
         program.setPatch3SourceMax(patch3SourceMax);
 
         in.read(bytes);
         int patch3DestinationEffect = bytes[0] + (bytes[1] * 16);
         program.setPatch3DestinationEffect(patch3DestinationEffect);
 
         in.read(bytes);
         int patch3DestinationParameter = bytes[0] + (bytes[1] * 16);
         program.setPatch3DestinationParameter(patch3DestinationParameter);
 
         bytes = new byte[4];
         in.read(bytes);
         int patch3DestinationMin = 0;
         // because this is Delay : Echo (D) : Fbk 1 it only reads the first two
         for (int i = 0; i < 2; i++) {
             patch3DestinationMin += (bytes[i] * Math.pow(16, i));
         }
         program.setPatch3DestinationMin(patch3DestinationMin);
 
         // TODO find out what goes here
         bytes = new byte[2];
         in.read(bytes);
 
         bytes = new byte[2];
         in.read(bytes);
         int patch3DestinationMid = bytes[0] + (bytes[1] * 16);
         program.setPatch3DestinationMid(patch3DestinationMid);
 
         bytes = new byte[4];
         in.read(bytes);
         int patch3DestinationMax = 0;
         for (int i = 0; i < 4; i++) {
             patch3DestinationMax += (bytes[i] * Math.pow(16, i));
         }
         program.setPatch3DestinationMax(patch3DestinationMax);
 
         // patching 4
         bytes = new byte[2];
         in.read(bytes);
         int patch4Source = bytes[0] + (bytes[1] * 16);
         program.setPatch4Source(patch4Source);
 
         in.read(bytes);
         int patch4SourceMin = bytes[0] + (bytes[1] * 16);
         program.setPatch4SourceMin(patch4SourceMin);
 
         in.read(bytes);
         int patch4SourceMid = bytes[0] + (bytes[1] * 16);
         program.setPatch4SourceMid(patch4SourceMid);
 
         in.read(bytes);
         int patch4SourceMax = bytes[0] + (bytes[1] * 16);
         program.setPatch4SourceMax(patch4SourceMax);
 
         in.read(bytes);
         int patch4DestinationEffect = bytes[0] + (bytes[1] * 16);
         program.setPatch4DestinationEffect(patch4DestinationEffect);
 
         in.read(bytes);
         int patch4DestinationParameter = bytes[0] + (bytes[1] * 16);
         program.setPatch4DestinationParameter(patch4DestinationParameter);
 
         bytes = new byte[4];
         in.read(bytes);
         int patch4DestinationMin = 0;
         // because this is Delay : Echo (D) : Fbk 2 it only reads the first two
         for (int i = 0; i < 2; i++) {
             patch4DestinationMin += (bytes[i] * Math.pow(16, i));
         }
         program.setPatch4DestinationMin(patch4DestinationMin);
 
         // TODO find out what goes here
         bytes = new byte[2];
         in.read(bytes);
 
         bytes = new byte[2];
         in.read(bytes);
         int patch4DestinationMid = bytes[0] + (bytes[1] * 16);
         program.setPatch4DestinationMid(patch4DestinationMid);
 
         bytes = new byte[4];
         in.read(bytes);
         int patch4DestinationMax = 0;
         for (int i = 0; i < 4; i++) {
             patch4DestinationMax += (bytes[i] * Math.pow(16, i));
         }
         program.setPatch4DestinationMax(patch4DestinationMax);
 
         // patching 5
         bytes = new byte[2];
         in.read(bytes);
         int patch5Source = bytes[0] + (bytes[1] * 16);
         program.setPatch5Source(patch5Source);
 
         in.read(bytes);
         int patch5SourceMin = bytes[0] + (bytes[1] * 16);
         program.setPatch5SourceMin(patch5SourceMin);
 
         in.read(bytes);
         int patch5SourceMid = bytes[0] + (bytes[1] * 16);
         program.setPatch5SourceMid(patch5SourceMid);
 
         in.read(bytes);
         int patch5SourceMax = bytes[0] + (bytes[1] * 16);
         program.setPatch5SourceMax(patch5SourceMax);
 
         in.read(bytes);
         int patch5DestinationEffect = bytes[0] + (bytes[1] * 16);
         program.setPatch5DestinationEffect(patch5DestinationEffect);
 
         in.read(bytes);
         int patch5DestinationParameter = bytes[0] + (bytes[1] * 16);
         program.setPatch5DestinationParameter(patch5DestinationParameter);
 
         bytes = new byte[4];
         in.read(bytes);
         int patch5DestinationMin = 0;
         // because this is Send Level it only reads the first two
         for (int i = 0; i < 2; i++) {
             patch5DestinationMin += (bytes[i] * Math.pow(16, i));
         }
         program.setPatch5DestinationMin(patch5DestinationMin);
 
         // TODO find out what goes here
         bytes = new byte[2];
         in.read(bytes);
 
         bytes = new byte[2];
         in.read(bytes);
         int patch5DestinationMid = bytes[0] + (bytes[1] * 16);
         program.setPatch5DestinationMid(patch5DestinationMid);
 
         bytes = new byte[4];
         in.read(bytes);
         int patch5DestinationMax = 0;
         for (int i = 0; i < 4; i++) {
             patch5DestinationMax += (bytes[i] * Math.pow(16, i));
         }
         program.setPatch5DestinationMax(patch5DestinationMax);
 
         // knob controller
         bytes = new byte[2];
         in.read(bytes);
         int knobValue = bytes[0] + (bytes[1] * 16);
         program.setKnobValue(knobValue);
 
         in.read(bytes);
         int knobLow = bytes[0] + (bytes[1] * 16);
         program.setKnobLow(knobLow);
 
         in.read(bytes);
         int knobHigh = bytes[0] + (bytes[1] * 16);
         program.setKnobHigh(knobHigh);
 
         bytes = new byte[18];
         in.read(bytes);
         StringBuilder programName = new StringBuilder(9);
         for (int i = 0; i < bytes.length; i += 2) {
             char c = (char) (bytes[i] + (bytes[i + 1] * 16));
             programName.append(c);
         }
         program.setKnobName(programName.toString());
 
         // lfo 1 controller
         bytes = new byte[2];
         in.read(bytes);
         int lfo1Mode = bytes[0] + (bytes[1] * 16);
         program.setLfo1Mode(lfo1Mode);
 
         bytes = new byte[6];
         in.read(bytes);
         int lfo1Rate = 0;
         for (int i = 0; i < bytes.length; i++) {
             lfo1Rate += (bytes[i] * Math.pow(16, i));
         }
         program.setLfo1Rate(lfo1Rate / 100.0);
 
         bytes = new byte[2];
         in.read(bytes);
         int lfo1PulseWidth = bytes[0] + (bytes[1] * 16);
         program.setLfo1PulseWidth(lfo1PulseWidth);
 
         in.read(bytes);
         int lfo1Phase = bytes[0] + (bytes[1] * 16);
         program.setLfo1Phase(lfo1Phase);
 
         in.read(bytes);
         int lfo1Depth = bytes[0] + (bytes[1] * 16);
         program.setLfo1Depth(lfo1Depth);
 
         in.read(bytes);
         int lfo1OnLevel = bytes[0] + (bytes[1] * 16);
         program.setLfo1OnLevel(lfo1OnLevel);
 
         in.read(bytes);
         int lfo1OnSource = bytes[0] + (bytes[1] * 16);
         program.setLfo1OnSource(lfo1OnSource);
 
         // lfo 2 controller
         bytes = new byte[2];
         in.read(bytes);
         int lfo2Mode = bytes[0] + (bytes[1] * 16);
         program.setLfo2Mode(lfo2Mode);
 
         bytes = new byte[6];
         in.read(bytes);
         int lfo2Rate = 0;
         for (int i = 0; i < bytes.length; i++) {
             lfo2Rate += (bytes[i] * Math.pow(16, i));
         }
         program.setLfo2Rate(lfo2Rate / 100.0);
 
         bytes = new byte[2];
         in.read(bytes);
         int lfo2PulseWidth = bytes[0] + (bytes[1] * 16);
         program.setLfo2PulseWidth(lfo2PulseWidth);
 
         in.read(bytes);
         int lfo2Phase = bytes[0] + (bytes[1] * 16);
         program.setLfo2Phase(lfo2Phase);
 
         in.read(bytes);
         int lfo2Depth = bytes[0] + (bytes[1] * 16);
         program.setLfo2Depth(lfo2Depth);
 
         in.read(bytes);
         int lfo2OnLevel = bytes[0] + (bytes[1] * 16);
         program.setLfo2OnLevel(lfo2OnLevel);
 
         in.read(bytes);
         int lfo2OnSource = bytes[0] + (bytes[1] * 16);
         program.setLfo2OnSource(lfo2OnSource);
 
         // random controller
         bytes = new byte[2];
         in.read(bytes);
         int randomLow = bytes[0] + (bytes[1] * 16);
         program.setRandomLow(randomLow);
 
         in.read(bytes);
         int randomHigh = bytes[0] + (bytes[1] * 16);
         program.setRandomHigh(randomHigh);
 
         bytes = new byte[4];
         in.read(bytes);
         int randomRate = 0;
         for (int i = 0; i < bytes.length; i++) {
             randomRate += (bytes[i] * Math.pow(16, i));
         }
         program.setRandomRate(randomRate / 100.0);
 
         // TODO what is this?
         in.read(new byte[2]);
 
         // a/b data
         bytes = new byte[2];
         in.read(bytes);
         int abMode = bytes[0] + (bytes[1] * 16);
         program.setABMode(abMode);
 
         in.read(bytes);
         int aRate = bytes[0] + (bytes[1] * 16);
         program.setARate(aRate);
 
         in.read(bytes);
         int bRate = bytes[0] + (bytes[1] * 16);
         program.setBRate(bRate);
 
         in.read(bytes);
         int abOnLevel = bytes[0] + (bytes[1] * 16);
         program.setABOnLevel(abOnLevel);
 
         in.read(bytes);
         int abOnSource = bytes[0] + (bytes[1] * 16);
         program.setABOnSource(abOnSource);
 
         // envelope generator data
         bytes = new byte[2];
         in.read(bytes);
         int envGenSrc1 = bytes[0] + (bytes[1] * 16);
         program.setEnvelopeGeneratorSrc1(envGenSrc1);
 
         in.read(bytes);
         int envGenSrc2 = bytes[0] + (bytes[1] * 16);
         program.setEnvelopeGeneratorSrc2(envGenSrc2);
 
         in.read(bytes);
         int envGenATrim = bytes[0] + (bytes[1] * 16);
         program.setEnvelopeGeneratorATrim(envGenATrim);
 
         in.read(bytes);
         int envGenResponse = bytes[0] + (bytes[1] * 16);
         program.setEnvelopeGeneratorResponse(envGenResponse);
 
         // noise gate
         bytes = new byte[2];
         in.read(bytes);
         int noiseGateEnable = bytes[0] + (bytes[1] * 16);
         program.setNoiseGateEnable(noiseGateEnable);
 
         in.read(bytes);
         int noiseGateThreshold = bytes[0] + (bytes[1] * 16);
         program.setNoiseGateThreshold(-1 * (256 - noiseGateThreshold));
 
         in.read(bytes);
         int noiseGateOffset = bytes[0] + (bytes[1] * 16);
         program.setNoiseGateOffset(-1 * noiseGateOffset);
 
         bytes = new byte[4];
         in.read(bytes);
         int noiseGateHTime = 0;
         for (int i = 0; i < bytes.length; i++) {
             noiseGateHTime += (bytes[i] * Math.pow(16, i));
         }
         program.setNoiseGateHTime(noiseGateHTime);
 
         in.read(bytes);
         int noiseGateATime = 0;
         for (int i = 0; i < bytes.length; i++) {
             noiseGateATime += (bytes[i] * Math.pow(16, i));
         }
         program.setNoiseGateATime(noiseGateATime);
 
         in.read(bytes);
         int noiseGateRTime = 0;
         for (int i = 0; i < bytes.length; i++) {
             noiseGateRTime += (bytes[i] * Math.pow(16, i));
         }
         program.setNoiseGateRTime(noiseGateRTime);
 
         bytes = new byte[2];
         in.read(bytes);
         int noiseGateAttenuation = bytes[0] + (bytes[1] * 16);
         program.setNoiseGateAttenuation(-1 * (256 - noiseGateAttenuation));
 
         in.read(bytes);
         int noiseGateDelay = bytes[0] + (bytes[1] * 16);
         program.setNoiseGateDelay(noiseGateDelay);
 
         in.read(bytes);
         int noiseGateSend = bytes[0] + (bytes[1] * 16);
         program.setNoiseGateSend(noiseGateSend);
 
         // Bypass State
         bytes = new byte[2];
         in.read(bytes);
         int bypassState = bytes[0] + (bytes[1] * 16);
         program.setBypassState(bypassState);
 
         // Speaker Simulator
         bytes = new byte[2];
         in.read(bytes);
         int speakerSimulatorEnable = bytes[0] + (bytes[1] * 16);
         program.setSpeakerSimulatorEnable(speakerSimulatorEnable);
 
         in.read(bytes);
         int speakerSimulatorCabinet = bytes[0] + (bytes[1] * 16);
         program.setSpeakerSimulatorCabinet(speakerSimulatorCabinet);
 
         // Mix
         bytes = new byte[2];
         in.read(bytes);
         int postLevel = (byte) (bytes[0] + (bytes[1] * 16));
         program.setPostLevel(postLevel);
 
         bytes = new byte[2];
         in.read(bytes);
         int postBypassLevel = (byte) (bytes[0] + (bytes[1] * 16));
         program.setPostBypassLevel(postBypassLevel);
 
         bytes = new byte[2];
         in.read(bytes);
         int postMix = bytes[0] + (bytes[1] * 16);
         program.setPostMix(postMix);
 
         bytes = new byte[2];
         in.read(bytes);
         int sendLevel = (byte) (bytes[0] + (bytes[1] * 16));
         program.setSendLevel(sendLevel);
 
         bytes = new byte[2];
         in.read(bytes);
         int sendBypassLevel = (byte) (bytes[0] + (bytes[1] * 16));
         program.setSendBypassLevel(sendBypassLevel);
 
         in.close();
 
         return program;
     }
 }
