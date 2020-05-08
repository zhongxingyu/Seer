 /*
  *  Copyright (C) 2011 Carl Green
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package info.carlwithak.mpxg2.sysex;
 
 import info.carlwithak.mpxg2.model.Ab;
 import info.carlwithak.mpxg2.model.EffectsStatus;
 import info.carlwithak.mpxg2.model.EnvelopeGenerator;
 import info.carlwithak.mpxg2.model.Knob;
 import info.carlwithak.mpxg2.model.Lfo;
 import info.carlwithak.mpxg2.model.NoiseGate;
 import info.carlwithak.mpxg2.model.Program;
 import info.carlwithak.mpxg2.model.Random;
 import info.carlwithak.mpxg2.model.effects.algorithms.Aerosol;
 import info.carlwithak.mpxg2.model.effects.algorithms.Ambience;
 import info.carlwithak.mpxg2.model.effects.algorithms.AutoPan;
 import info.carlwithak.mpxg2.model.effects.algorithms.BlueComp;
 import info.carlwithak.mpxg2.model.effects.algorithms.Centrifuge1;
 import info.carlwithak.mpxg2.model.effects.algorithms.Centrifuge2;
 import info.carlwithak.mpxg2.model.effects.algorithms.Chamber;
 import info.carlwithak.mpxg2.model.effects.algorithms.ChorusAlgorithm;
 import info.carlwithak.mpxg2.model.effects.algorithms.ChorusDetuneMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.ChorusExtPedalVol;
 import info.carlwithak.mpxg2.model.effects.algorithms.ChorusPedalVol;
 import info.carlwithak.mpxg2.model.effects.algorithms.ChorusVolumeDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.ChorusVolumeMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.ChorusVolumeStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.Click;
 import info.carlwithak.mpxg2.model.effects.algorithms.Comb1;
 import info.carlwithak.mpxg2.model.effects.algorithms.Comb2;
 import info.carlwithak.mpxg2.model.effects.algorithms.Crossover;
 import info.carlwithak.mpxg2.model.effects.algorithms.Crunch;
 import info.carlwithak.mpxg2.model.effects.algorithms.CustomVybe;
 import info.carlwithak.mpxg2.model.effects.algorithms.DelayDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.DelayMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.DelayStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.DetuneDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.DetuneMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.DetuneStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.DiatonicHmy;
 import info.carlwithak.mpxg2.model.effects.algorithms.DigiDrive1;
 import info.carlwithak.mpxg2.model.effects.algorithms.DigiDrive2;
 import info.carlwithak.mpxg2.model.effects.algorithms.Distortion;
 import info.carlwithak.mpxg2.model.effects.algorithms.Ducker;
 import info.carlwithak.mpxg2.model.effects.algorithms.EchoDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.EchoMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.EchoStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.EqExtPedalVol;
 import info.carlwithak.mpxg2.model.effects.algorithms.EqPedalVol;
 import info.carlwithak.mpxg2.model.effects.algorithms.EqVolumeDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.EqVolumeMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.EqVolumeStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.ExtPedalVol;
 import info.carlwithak.mpxg2.model.effects.algorithms.FcSplitter;
 import info.carlwithak.mpxg2.model.effects.algorithms.Flanger24Mono;
 import info.carlwithak.mpxg2.model.effects.algorithms.FlangerMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.FlangerStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.FourBandMono;
import info.carlwithak.mpxg2.model.effects.algorithms.FXOneBandMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.Gate;
 import info.carlwithak.mpxg2.model.effects.algorithms.Hall;
 import info.carlwithak.mpxg2.model.effects.algorithms.JamMan;
 import info.carlwithak.mpxg2.model.effects.algorithms.Looper;
 import info.carlwithak.mpxg2.model.effects.algorithms.OctaBuzz;
 import info.carlwithak.mpxg2.model.effects.algorithms.OneBandDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.OneBandMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.OneBandStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.OrangePhase;
 import info.carlwithak.mpxg2.model.effects.algorithms.Orbits;
 import info.carlwithak.mpxg2.model.effects.algorithms.Overdrive;
 import info.carlwithak.mpxg2.model.effects.algorithms.Panner;
 import info.carlwithak.mpxg2.model.effects.algorithms.PedalVol;
 import info.carlwithak.mpxg2.model.effects.algorithms.PedalWah1;
 import info.carlwithak.mpxg2.model.effects.algorithms.PedalWah2;
 import info.carlwithak.mpxg2.model.effects.algorithms.Phaser;
 import info.carlwithak.mpxg2.model.effects.algorithms.Plate;
 import info.carlwithak.mpxg2.model.effects.algorithms.Preamp;
 import info.carlwithak.mpxg2.model.effects.algorithms.RedComp;
 import info.carlwithak.mpxg2.model.effects.algorithms.RotaryCab;
 import info.carlwithak.mpxg2.model.effects.algorithms.Screamer;
 import info.carlwithak.mpxg2.model.effects.algorithms.ShiftDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.ShiftMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.ShiftStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.SplitPreamp;
 import info.carlwithak.mpxg2.model.effects.algorithms.SweepFilter;
 import info.carlwithak.mpxg2.model.effects.algorithms.TestTone;
 import info.carlwithak.mpxg2.model.effects.algorithms.ThreeBandMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.Tone;
 import info.carlwithak.mpxg2.model.effects.algorithms.TremoloMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.TremoloStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.TwoBandDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.TwoBandMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.TwoBandStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.UniVybe;
 import info.carlwithak.mpxg2.model.effects.algorithms.VolumeDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.VolumeMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.VolumeStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.Wah1;
 import info.carlwithak.mpxg2.model.effects.algorithms.Wah2;
 import info.carlwithak.mpxg2.model.parameters.FrequencyRate;
 import info.carlwithak.mpxg2.model.parameters.GenericValue;
 import info.carlwithak.mpxg2.model.parameters.Parameter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.List;
 import org.junit.Before;
 import org.junit.Test;
 
 import static info.carlwithak.mpxg2.test.IsBeat.beat;
 import static info.carlwithak.mpxg2.test.IsFrequency.frequency;
 import static info.carlwithak.mpxg2.test.IsOnOff.off;
 import static info.carlwithak.mpxg2.test.IsOnOff.on;
 import static info.carlwithak.mpxg2.test.IsText.text;
 import static info.carlwithak.mpxg2.test.IsValue.value;
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsInstanceOf.instanceOf;
 import static org.hamcrest.core.IsNull.nullValue;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import static org.mockito.Mockito.mock;
 
 /**
  * Tests for SysexParser, using real files dumped from the MPX G2.
  *
  * @author carl
  */
 public class SysexParserTest {
     private static final byte[] FILE_INTRO = {
         (byte) 0xf0, 0x06, 0x0f, 0x00, 0x01, 0x06, 0x0c, 0x01, 0x00,
     };
     private static final byte[] FILE_OUTRO = {
         0x04, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x0a, 0x00, 0x00, 0x00,
         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
         0x00, (byte) 0xf7
     };
     private static final byte[] BLANK_PROGRAM_DATA = new byte[454 * 2];
 
     @Before
     public void setUp() {
         Arrays.fill(BLANK_PROGRAM_DATA, (byte) 0);
     }
 
     /**
      * Test parsing invalid data.
      */
     @Test
     public void testParseInvalidData() throws Exception {
         String expectedMessage = null;
         try {
             File temp = tempFileWithData(new byte[]{(byte) 0xe0});
             expectedMessage = "Invalid Sysex ID (start): 0xe0, expected 0xf0";
             SysexParser.parsePrograms(temp);
             fail("Expected \"" + expectedMessage + "\"");
         } catch (ParseException e) {
             assertEquals(expectedMessage, e.getMessage());
         }
 
         try {
             File temp = tempFileWithData(new byte[]{(byte) 0xf0, 0x05});
             expectedMessage = "Invalid Manufacturer ID: 0x05, expected 0x06";
             SysexParser.parsePrograms(temp);
             fail("Expected \"" + expectedMessage + "\"");
         } catch (ParseException e) {
             assertEquals(expectedMessage, e.getMessage());
         }
 
         try {
             File temp = tempFileWithData(new byte[]{(byte) 0xf0, 0x06, 0x10});
             expectedMessage = "Invalid Product ID: 0x10, expected 0x0f";
             SysexParser.parsePrograms(temp);
             fail("Expected \"" + expectedMessage + "\"");
         } catch (ParseException e) {
             assertEquals(expectedMessage, e.getMessage());
         }
 
         try {
             File temp = tempFileWithData(new byte[]{(byte) 0xf0, 0x06, 0x0f, 0x00, 0x00});
             expectedMessage = "Invalid Message Type: 0x00, expected 0x01";
             SysexParser.parsePrograms(temp);
             fail("Expected \"" + expectedMessage + "\"");
         } catch (ParseException e) {
             assertEquals(expectedMessage, e.getMessage());
         }
     }
 
     @Test
     public void testNotFourLevelsOfControl() throws Exception {
         String expectedMessage = null;
         try {
             File temp = tempFileWithData(new byte[]{(byte) 0xf0, 0x06, 0x0f, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,
             0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
             expectedMessage = "Expect 4 control levels for a program dump";
             SysexParser.parsePrograms(temp);
             fail("Expected \"" + expectedMessage + "\"");
         } catch (ParseException e) {
             assertEquals(expectedMessage, e.getMessage());
         }
     }
 
     @Test
     public void testNotProgramDumpControlPath() throws Exception {
         String expectedMessage = null;
         try {
             File temp = tempFileWithData(new byte[]{
                 (byte) 0xf0, 0x06, 0x0f, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,
                 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0a, 0x00, 0x00, 0x00,
                 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
             });
             expectedMessage = "Expect ProgramDump control tree path";
             SysexParser.parsePrograms(temp);
             fail("Expected \"" + expectedMessage + "\"");
         } catch (ParseException e) {
             assertEquals(expectedMessage, e.getMessage());
         }
 
         try {
             File temp = tempFileWithData(new byte[]{
                 (byte) 0xf0, 0x06, 0x0f, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,
                 0x04, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x0b, 0x00, 0x00, 0x00,
                 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
             });
             expectedMessage = "Expect ProgramDump control tree path";
             SysexParser.parsePrograms(temp);
             fail("Expected \"" + expectedMessage + "\"");
         } catch (ParseException e) {
             assertEquals(expectedMessage, e.getMessage());
         }
     }
 
     @Test
     public void testNoSysexEndMarker() throws Exception {
         String expectedMessage = null;
         try {
             File temp = tempFileWithData(new byte[]{
                 (byte) 0xf0, 0x06, 0x0f, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,
                 0x04, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x0a, 0x00, 0x00, 0x00,
                 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                 0x00, (byte) 0xf6
             });
             expectedMessage = "Invalid Sysex ID (end)";
             SysexParser.parsePrograms(temp);
             fail("Expected \"" + expectedMessage + "\"");
         } catch (ParseException e) {
             assertEquals(expectedMessage, e.getMessage());
         }
     }
 
     @Test
     public void testParsePrograms() throws IOException, ParseException {
         byte[] file = concat(FILE_INTRO, BLANK_PROGRAM_DATA, FILE_OUTRO);
         byte[] files = concat(file, file);
 
         File temp = tempFileWithData(files);
         List<Program> programs = SysexParser.parsePrograms(temp);
         assertEquals(2, programs.size());
     }
 
     @Test
     public void testReadEffectTypes() {
         Program program = new Program();
         SysexParser.readEffectTypes(program, 0);
 
         assertFalse(program.isChorus());
         SysexParser.readEffectTypes(program, 1);
         assertTrue(program.isChorus());
 
         assertFalse(program.isDelay());
         SysexParser.readEffectTypes(program, 2);
         assertTrue(program.isDelay());
 
         assertFalse(program.isDistortion());
         SysexParser.readEffectTypes(program, 4);
         assertTrue(program.isDistortion());
 
         assertFalse(program.isEq());
         SysexParser.readEffectTypes(program, 8);
         assertTrue(program.isEq());
 
         assertFalse(program.isFlanger());
         SysexParser.readEffectTypes(program, 16);
         assertTrue(program.isFlanger());
 
         assertFalse(program.isGain());
         SysexParser.readEffectTypes(program, 32);
         assertTrue(program.isGain());
 
         assertFalse(program.isMod());
         SysexParser.readEffectTypes(program, 64);
         assertTrue(program.isMod());
 
         assertFalse(program.isOverdrive());
         SysexParser.readEffectTypes(program, 128);
         assertTrue(program.isOverdrive());
 
         assertFalse(program.isPhaser());
         SysexParser.readEffectTypes(program, 256);
         assertTrue(program.isPhaser());
 
         assertFalse(program.isPitch());
         SysexParser.readEffectTypes(program, 512);
         assertTrue(program.isPitch());
 
         assertFalse(program.isReverb());
         SysexParser.readEffectTypes(program, 1024);
         assertTrue(program.isReverb());
 
         assertFalse(program.isSpeakerSim());
         SysexParser.readEffectTypes(program, 2048);
         assertTrue(program.isSpeakerSim());
 
         assertFalse(program.isWah());
         SysexParser.readEffectTypes(program, 4096);
         assertTrue(program.isWah());
 
         assertFalse(program.isPrePost());
         SysexParser.readEffectTypes(program, 8192);
         assertTrue(program.isPrePost());
 
         assertFalse(program.isStandAlone());
         SysexParser.readEffectTypes(program, 16384);
         assertTrue(program.isStandAlone());
 
         assertFalse(program.isInline());
         SysexParser.readEffectTypes(program, 32768);
         assertTrue(program.isInline());
     }
 
     @Test
     public void testReadGuitarStyles() {
         Program program = new Program();
 
         SysexParser.readGuitarStyles(program, 0);
         assertFalse(program.isAcoustic());
 
         SysexParser.readGuitarStyles(program, 1);
         assertFalse(program.isAcoustic());
 
         SysexParser.readGuitarStyles(program, 2);
         assertTrue(program.isAcoustic());
 
         assertFalse(program.isBass());
         SysexParser.readGuitarStyles(program, 4);
         assertTrue(program.isBass());
 
         assertFalse(program.isBlues());
         SysexParser.readGuitarStyles(program, 8);
         assertTrue(program.isBlues());
 
         assertFalse(program.isClean());
         SysexParser.readGuitarStyles(program, 16);
         assertTrue(program.isClean());
 
         assertFalse(program.isCountry());
         SysexParser.readGuitarStyles(program, 32);
         assertTrue(program.isCountry());
 
         assertFalse(program.isJazz());
         SysexParser.readGuitarStyles(program, 64);
         assertTrue(program.isJazz());
 
         assertFalse(program.isRock());
         SysexParser.readGuitarStyles(program, 128);
         assertTrue(program.isRock());
     }
 
     @Test
     public void testReadEffectsStatus() {
         EffectsStatus effectsStatus = SysexParser.readEffectsStatus(0);
 
         assertThat(effectsStatus.getEffect1On(), is(off()));
         effectsStatus = SysexParser.readEffectsStatus(1);
         assertThat(effectsStatus.getEffect1On(), is(on()));
 
         assertThat(effectsStatus.getEffect2On(), is(off()));
         effectsStatus = SysexParser.readEffectsStatus(2);
         assertThat(effectsStatus.getEffect2On(), is(on()));
 
         assertThat(effectsStatus.getChorusOn(), is(off()));
         effectsStatus = SysexParser.readEffectsStatus(4);
         assertThat(effectsStatus.getChorusOn(), is(on()));
 
         assertThat(effectsStatus.getDelayOn(), is(off()));
         effectsStatus = SysexParser.readEffectsStatus(8);
         assertThat(effectsStatus.getDelayOn(), is(on()));
 
         assertThat(effectsStatus.getReverbOn(), is(off()));
         effectsStatus = SysexParser.readEffectsStatus(16);
         assertThat(effectsStatus.getReverbOn(), is(on()));
 
         assertThat(effectsStatus.getEqOn(), is(off()));
         effectsStatus = SysexParser.readEffectsStatus(32);
         assertThat(effectsStatus.getEqOn(), is(on()));
 
         assertThat(effectsStatus.getGainOn(), is(off()));
         effectsStatus = SysexParser.readEffectsStatus(64);
         assertThat(effectsStatus.getGainOn(), is(on()));
 
         assertThat(effectsStatus.getInsertOn(), is(off()));
         effectsStatus = SysexParser.readEffectsStatus(128);
         assertThat(effectsStatus.getInsertOn(), is(on()));
     }
 
     @Test
     public void testInvalidEffect1AlgorithmNumber() throws IOException {
         BLANK_PROGRAM_DATA[558] = 0x22;
         byte[] file = concat(FILE_INTRO, BLANK_PROGRAM_DATA, FILE_OUTRO);
 
         File temp = tempFileWithData(file);
         String expectedMessage = "Invalid Effect 1 algorithm number: 34";
         try {
             SysexParser.parsePrograms(temp);
             fail("Expected \"" + expectedMessage + "\"");
         } catch (ParseException e) {
             assertEquals(expectedMessage, e.getMessage());
         }
     }
 
     @Test
     public void testInvalidEffect2AlgorithmNumber() throws IOException {
         BLANK_PROGRAM_DATA[560] = 0x1b;
         byte[] file = concat(FILE_INTRO, BLANK_PROGRAM_DATA, FILE_OUTRO);
 
         File temp = tempFileWithData(file);
         String expectedMessage = "Invalid Effect 2 algorithm number: 27";
         try {
             SysexParser.parsePrograms(temp);
             fail("Expected \"" + expectedMessage + "\"");
         } catch (ParseException e) {
             assertEquals(expectedMessage, e.getMessage());
         }
     }
 
     @Test
     public void testInvalidChorusAlgorithmNumber() throws IOException {
         BLANK_PROGRAM_DATA[562] = 0x12;
         byte[] file = concat(FILE_INTRO, BLANK_PROGRAM_DATA, FILE_OUTRO);
 
         File temp = tempFileWithData(file);
         String expectedMessage = "Invalid Chorus algorithm number: 18";
         try {
             SysexParser.parsePrograms(temp);
             fail("Expected \"" + expectedMessage + "\"");
         } catch (ParseException e) {
             assertEquals(expectedMessage, e.getMessage());
         }
     }
 
     @Test
     public void testInvalidDelayAlgorithmNumber() throws IOException {
         BLANK_PROGRAM_DATA[564] = 0x0a;
         byte[] file = concat(FILE_INTRO, BLANK_PROGRAM_DATA, FILE_OUTRO);
 
         File temp = tempFileWithData(file);
         String expectedMessage = "Invalid Delay algorithm number: 10";
         try {
             SysexParser.parsePrograms(temp);
             fail("Expected \"" + expectedMessage + "\"");
         } catch (ParseException e) {
             assertEquals(expectedMessage, e.getMessage());
         }
     }
 
     @Test
     public void testInvalidReverbAlgorithmNumber() throws IOException {
         BLANK_PROGRAM_DATA[566] = 0x06;
         byte[] file = concat(FILE_INTRO, BLANK_PROGRAM_DATA, FILE_OUTRO);
 
         File temp = tempFileWithData(file);
         String expectedMessage = "Invalid Reverb algorithm number: 6";
         try {
             SysexParser.parsePrograms(temp);
             fail("Expected \"" + expectedMessage + "\"");
         } catch (ParseException e) {
             assertEquals(expectedMessage, e.getMessage());
         }
     }
 
     @Test
     public void testInvalidEqAlgorithmNumber() throws IOException {
         BLANK_PROGRAM_DATA[568] = 0x10;
         byte[] file = concat(FILE_INTRO, BLANK_PROGRAM_DATA, FILE_OUTRO);
 
         File temp = tempFileWithData(file);
         String expectedMessage = "Invalid EQ algorithm number: 16";
         try {
             SysexParser.parsePrograms(temp);
             fail("Expected \"" + expectedMessage + "\"");
         } catch (ParseException e) {
             assertEquals(expectedMessage, e.getMessage());
         }
     }
 
     @Test
     public void testInvalidGainAlgorithmNumber() throws IOException {
         BLANK_PROGRAM_DATA[570] = 0x09;
         byte[] file = concat(FILE_INTRO, BLANK_PROGRAM_DATA, FILE_OUTRO);
 
         File temp = tempFileWithData(file);
         String expectedMessage = "Invalid Gain algorithm number: 9";
         try {
             SysexParser.parsePrograms(temp);
             fail("Expected \"" + expectedMessage + "\"");
         } catch (ParseException e) {
             assertEquals(expectedMessage, e.getMessage());
         }
     }
 
     private File tempFileWithData(final byte[] data) throws IOException {
         File temp = File.createTempFile("test_", ".syx", new File("target/test-classes/"));
         temp.deleteOnExit();
 
         OutputStream out = null;
         try {
             out = new FileOutputStream(temp);
             out.write(data);
         } finally {
             out.close();
         }
 
         return temp;
     }
 
     private byte[] concat(byte[]... bb) {
         int length = 0;
         for (byte[] b : bb) {
             length += b.length;
         }
         byte[] result = new byte[length];
         int offset = 0;
         for (byte[] b : bb) {
             System.arraycopy(b, 0, result, offset, b.length);
             offset += b.length;
         }
         return result;
     }
 
     @Test
     public void testParseEffect1() throws ParseException {
         final byte[] effect1ParameterData = new byte[64];
         final Class[] classes = {
             DetuneMono.class,
             DetuneStereo.class,
             DetuneDual.class,
             ShiftMono.class,
             ShiftStereo.class,
             ShiftDual.class,
             DiatonicHmy.class,
             Panner.class,
             AutoPan.class,
             TremoloMono.class,
             TremoloStereo.class,
             UniVybe.class,
             CustomVybe.class,
             Phaser.class,
             OrangePhase.class,
             RedComp.class,
             BlueComp.class,
             DigiDrive1.class,
             DigiDrive2.class,
             OctaBuzz.class,
             SweepFilter.class,
             FXOneBandMono.class,
             Wah1.class,
             Wah2.class,
             PedalWah1.class,
             PedalWah2.class,
             VolumeMono.class,
             VolumeStereo.class,
             VolumeDual.class,
             PedalVol.class,
             ExtPedalVol.class,
             TestTone.class,
             Click.class
         };
         for (int i = 0; i < classes.length; i++) {
             assertThat(SysexParser.parseEffect1(i + 1, effect1ParameterData), is(instanceOf(classes[i])));
         }
     }
 
     @Test
     public void testParseEffect2() throws ParseException {
         final byte[] effect2ParameterData = new byte[64];
         final Class[] classes = {
             Panner.class,
             AutoPan.class,
             TremoloMono.class,
             TremoloStereo.class,
             UniVybe.class,
             CustomVybe.class,
             Phaser.class,
             OrangePhase.class,
             RedComp.class,
             BlueComp.class,
             DigiDrive1.class,
             DigiDrive2.class,
             OctaBuzz.class,
             SweepFilter.class,
             FXOneBandMono.class,
             Wah1.class,
             Wah2.class,
             PedalWah1.class,
             PedalWah2.class,
             VolumeMono.class,
             VolumeStereo.class,
             VolumeDual.class,
             PedalVol.class,
             ExtPedalVol.class,
             TestTone.class,
             Click.class
         };
         for (int i = 0; i < classes.length; i++) {
             assertThat(SysexParser.parseEffect2(i + 1, effect2ParameterData), is(instanceOf(classes[i])));
         }
     }
 
     @Test
     public void testParseChorus() throws ParseException {
         final byte[] chorusParameterData = new byte[64];
         final Class[] classes = {
             ChorusAlgorithm.class,
             ChorusDetuneMono.class,
             FlangerMono.class,
             Flanger24Mono.class,
             FlangerStereo.class,
             RotaryCab.class,
             Aerosol.class,
             Orbits.class,
             Centrifuge1.class,
             Centrifuge2.class,
             Comb1.class,
             Comb2.class,
             ChorusVolumeMono.class,
             ChorusVolumeStereo.class,
             ChorusVolumeDual.class,
             ChorusPedalVol.class,
             ChorusExtPedalVol.class
         };
         for (int i = 0; i < classes.length; i++) {
             assertThat(SysexParser.parseChorus(i + 1, chorusParameterData), is(instanceOf(classes[i])));
         }
     }
 
     @Test
     public void testParseDelay() throws ParseException {
         final byte[] delayParameterData = new byte[64];
         final Class[] classes = {
             DelayMono.class,
             DelayStereo.class,
             DelayDual.class,
             EchoMono.class,
             EchoStereo.class,
             EchoDual.class,
             Looper.class,
             JamMan.class,
             Ducker.class
         };
         for (int i = 0; i < classes.length; i++) {
             assertThat(SysexParser.parseDelay(i + 1, delayParameterData), is(instanceOf(classes[i])));
         }
     }
 
     @Test
     public void testParseReverb() throws ParseException {
         final byte[] reverbParameterData = new byte[64];
         final Class[] classes = {
             Chamber.class,
             Hall.class,
             Plate.class,
             Ambience.class,
             Gate.class
         };
         for (int i = 0; i < classes.length; i++) {
             assertThat(SysexParser.parseReverb(i + 1, reverbParameterData), is(instanceOf(classes[i])));
         }
     }
 
     @Test
     public void testParseEq() throws ParseException {
         final byte[] eqParameterData = new byte[64];
         final Class[] classes = {
             OneBandMono.class,
             TwoBandMono.class,
             ThreeBandMono.class,
             FourBandMono.class,
             OneBandStereo.class,
             TwoBandStereo.class,
             OneBandDual.class,
             TwoBandDual.class,
             FcSplitter.class,
             Crossover.class,
             EqVolumeMono.class,
             EqVolumeStereo.class,
             EqVolumeDual.class,
             EqPedalVol.class,
             EqExtPedalVol.class
         };
         for (int i = 0; i < classes.length; i++) {
             assertThat(SysexParser.parseEq(i + 1, eqParameterData), is(instanceOf(classes[i])));
         }
     }
 
     @Test
     public void testParseGain() throws ParseException {
         final byte[] gainParameterData = new byte[64];
         final Class[] classes = {
             Tone.class,
             Crunch.class,
             Screamer.class,
             Overdrive.class,
             Distortion.class,
             Preamp.class,
             SplitPreamp.class
         };
         for (int i = 0; i < classes.length; i++) {
             assertThat(SysexParser.parseGain(i + 1, gainParameterData), is(instanceOf(classes[i])));
         }
     }
 
     /**
      * Test parsing the various noise gate values.
      */
     @Test
     public void testParseNoiseGate() throws Exception {
         File preset = new File(this.getClass().getClassLoader().getResource("noisegate.syx").toURI());
         NoiseGate noiseGate = SysexParser.parsePrograms(preset).get(0).getNoiseGate();
 
         assertThat(noiseGate.getEnable(), is(value(2)));
         assertThat(noiseGate.getSend(), is(on()));
         assertThat(noiseGate.getThreshold(), is(value(-31)));
         assertThat(noiseGate.getAttenuation(), is(value(-7)));
         assertThat(noiseGate.getOffset(), is(value(-11)));
         assertThat(noiseGate.getATime(), is(value(1999)));
         assertThat(noiseGate.getHTime(), is(value(499)));
         assertThat(noiseGate.getRTime(), is(value(2000)));
         assertThat(noiseGate.getDelay(), is(value(10)));
     }
 
     @Test
     public void testReadKnob() {
         byte[] bytes = {2, 3, 0, 0, 4, 6, 4, 4, 5, 6, 12, 6, 1, 6, 9, 7, 0, 2, 1, 4, 4, 6, 10, 6};
         Knob knob = SysexParser.readKnob(bytes);
 
         assertThat(knob.getValue(), is(value(50)));
         assertThat(knob.getLow(), is(value(0)));
         assertThat(knob.getHigh(), is(value(100)));
         assertThat(knob.getName(), is(text("Delay Adj")));
     }
 
     @Test
     public void testReadLfo() throws Exception {
         byte[] bytes = {1, 0, 12, 3, 0, 0, 0, 0, 2, 3, 0, 0, 4, 6, 0, 4, 0, 0};
         Lfo lfo = SysexParser.readLfo(bytes);
 
         assertThat(lfo.getMode(), is(value(1)));
         assertThat(lfo.getRate(), is(frequency(0.60)));
         assertThat(lfo.getPulseWidth(), is(value(50)));
         assertThat(lfo.getPhase(), is(value(0)));
         assertThat(lfo.getDepth(), is(value(100)));
         assertThat(lfo.getOnLevel(), is(value(64)));
         assertThat(lfo.getOnSource(), is(value(0)));
     }
 
     @Test
     public void testReadRandom() throws ParseException {
         byte[] bytes = {0, 0, 2, 3, 3, 0, 1, 0, 1, 0};
 
         Random random = SysexParser.readRandom(bytes);
         assertThat(random.getLow(), is(value(0)));
         assertThat(random.getHigh(), is(value(50)));
         assertThat(random.getRate(), is(beat(3, 1)));
     }
 
     @Test
     public void testReadAb() {
         byte[] bytes = {0, 0, 4, 6, 4, 6, 0, 4, 0, 0};
 
         Ab ab = SysexParser.readAb(bytes);
         assertThat(ab.getMode(), is(value(0)));
         assertThat(ab.getARate(), is(value(100)));
         assertThat(ab.getBRate(), is(value(100)));
         assertThat(ab.getOnLevel(), is(value(64)));
         assertThat(ab.getOnSource(), is(value(0)));
     }
 
     @Test
     public void testReadEnvelopeGenerator() {
         byte[] bytes = {0, 0, 0, 0, 4, 6, 0, 4};
 
         EnvelopeGenerator envelopeGenerator = SysexParser.readEnvelopeGenerator(bytes);
         assertThat(envelopeGenerator.getSrc1(), is(value(0)));
         assertThat(envelopeGenerator.getSrc2(), is(value(0)));
         assertThat(envelopeGenerator.getATrim(), is(value(100)));
         assertThat(envelopeGenerator.getResponse(), is(value(64)));
     }
 
     @Test
     public void testCreatePatchDestinationParameterFromGenericValueInteger() throws ParseException {
         GenericValue<Integer> input = new GenericValue<Integer>("a1", "b", -1, 1);
         input.setValue(-1);
         Parameter actual = SysexParser.createPatchDestinationParameter(input, "a2", 1);
         assertThat(actual, is(instanceOf(GenericValue.class)));
         @SuppressWarnings("unchecked")
         GenericValue<Integer> val = (GenericValue<Integer>) actual;
         assertThat(actual.getName(), (is("a2")));
         assertThat(actual.getUnit(), (is("b")));
         assertThat(val.getMinValue(), (is(-1)));
         assertThat(val.getMaxValue(), (is(1)));
         assertThat(val.getValue(), (is(1)));
     }
 
     @Test
     public void testCreatePatchDestinationParameterFromGenericValueBoolean() throws ParseException {
         GenericValue<Boolean> input = new GenericValue<Boolean>("a1", "b", false, true);
         input.setValue(false);
         Parameter actual = SysexParser.createPatchDestinationParameter(input, "a2", 1);
         assertThat(actual, is(instanceOf(GenericValue.class)));
         @SuppressWarnings("unchecked")
         GenericValue<Boolean> val = (GenericValue<Boolean>) actual;
         assertThat(actual.getName(), (is("a2")));
         assertThat(actual.getUnit(), (is("b")));
         assertThat(val.getMinValue(), (is(false)));
         assertThat(val.getMaxValue(), (is(true)));
         assertThat(val.getValue(), (is(true)));
     }
 
     @Test
     public void testCreatePatchDestinationParameterFromUnsetGenericValue() throws ParseException {
         GenericValue<Integer> input = new GenericValue<Integer>("a1", "b", -1, 1);
         input.setValue(-1);
         Parameter actual = SysexParser.createPatchDestinationParameter(input, "a2", 0x8000);
         assertThat(actual, is(instanceOf(GenericValue.class)));
         @SuppressWarnings("unchecked")
         GenericValue<Integer> val = (GenericValue<Integer>) actual;
         assertThat(val.getValue(), (is(nullValue())));
     }
 
     @Test
     public void testCreatePatchDestinationParameterFromRate() throws ParseException {
         FrequencyRate input = new FrequencyRate("a", 1.0);
         Parameter actual = SysexParser.createPatchDestinationParameter(input, "a2", 0xc8);
         assertThat(actual, is(instanceOf(FrequencyRate.class)));
         FrequencyRate val = (FrequencyRate) actual;
         assertThat(actual.getName(), (is("a2")));
         assertThat(val.getFrequency(), (is(2.0)));
     }
 
     @Test(expected = ParseException.class)
     public void testCreatePatchDestinationParameterFromUnsupportedType() throws ParseException {
         Parameter parameter = mock(Parameter.class);
         SysexParser.createPatchDestinationParameter(parameter, null, 0);
     }
 
     @Test(expected = ParseException.class)
     public void testCreatePatchDestinationParameterFromUnsupportedGenericType() throws ParseException {
         Parameter parameter = mock(GenericValue.class);
         SysexParser.createPatchDestinationParameter(parameter, null, 0);
     }
 
 }
