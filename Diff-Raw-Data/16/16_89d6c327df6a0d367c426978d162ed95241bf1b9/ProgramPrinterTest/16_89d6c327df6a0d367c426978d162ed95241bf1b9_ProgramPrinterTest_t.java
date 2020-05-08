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
 
 package info.carlwithak.mpxg2.printing;
 
 import info.carlwithak.mpxg2.model.Ab;
 import info.carlwithak.mpxg2.model.EnvelopeGenerator;
 import info.carlwithak.mpxg2.model.Knob;
 import info.carlwithak.mpxg2.model.Lfo;
 import info.carlwithak.mpxg2.model.NoiseGate;
 import info.carlwithak.mpxg2.model.Patch;
 import info.carlwithak.mpxg2.model.PostMix;
 import info.carlwithak.mpxg2.model.Program;
 import info.carlwithak.mpxg2.model.Random;
 import info.carlwithak.mpxg2.model.SendMix;
 import info.carlwithak.mpxg2.model.Tempo;
 import info.carlwithak.mpxg2.model.effects.algorithms.OneBandMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.PedalVol;
 import info.carlwithak.mpxg2.model.parameters.BeatRate;
 import info.carlwithak.mpxg2.model.parameters.FrequencyRate;
 import info.carlwithak.mpxg2.model.parameters.GenericValue;
 import info.carlwithak.mpxg2.model.parameters.OnOffValue;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 /**
  * Tests for ProgramPrinter.
  *
  * @author carl
  */
 public class ProgramPrinterTest {
     @Test
     public void testPrintGuitarStyles() {
         Program program = new Program();
 
         assertEquals("", ProgramPrinter.printGuitarStyles(program));
 
         program.setIsAcoustic(true);
         program.setIsBass(true);
         program.setIsBlues(true);
         program.setIsClean(true);
         program.setIsCountry(true);
         program.setIsJazz(true);
         program.setIsRock(true);
 
         assertEquals("Acoustic, Bass, Blues, Clean, Country, Jazz, Rock", ProgramPrinter.printGuitarStyles(program));
     }
 
     @Test
     public void testPrintEffectTypes() {
         Program program = new Program();
 
         assertEquals("", ProgramPrinter.printEffectTypes(program));
 
         program.setIsChorus(true);
         program.setIsDelay(true);
         program.setIsDistortion(true);
         program.setIsEq(true);
         program.setIsFlanger(true);
         program.setIsGain(true);
         program.setIsMod(true);
         program.setIsOverdrive(true);
         program.setIsPhaser(true);
         program.setIsPitch(true);
         program.setIsReverb(true);
         program.setIsSpeakerSim(true);
         program.setIsWah(true);
 
         assertEquals("Chorus, Delay, Distortion, EQ, Flanger, Gain, Mod, Overdrive, Phaser, Pitch, Reverb, Speaker Sim, Wah", ProgramPrinter.printEffectTypes(program));
     }
 
     @Test
     public void testPrintApplicationTypes() {
         Program program = new Program();
 
         assertEquals("", ProgramPrinter.printApplicationTypes(program));
 
         program.setIsPrePost(true);
         program.setIsStandAlone(true);
         program.setIsInline(true);
 
         assertEquals("Amp Input + FX Loop, Stand alone, Amp Input Only", ProgramPrinter.printApplicationTypes(program));
     }
 
     @Test
     public void testPrintProgram() throws PrintException {
         StringBuilder sb = new StringBuilder();
         ProgramPrinter.printProgram(sb, null, null, null, 0);
         assertEquals("", sb.toString());
 
         sb = new StringBuilder();
         // TODO would be good to mock the effect here
         PedalVol pedalVol = new PedalVol();
         pedalVol.mix.setValue(0);
         pedalVol.level.setValue(0);
         OnOffValue effect1Status = new OnOffValue("FX1  Byp");
         effect1Status.setValue(true);
         ProgramPrinter.printProgram(sb, "Effect1", pedalVol, effect1Status, 2);
         assertThat(sb.toString(), containsString("  Effect1: Pedal Vol (On)\n    Toe Switch: on=bypass"));
     }
 
     @Test
     public void testPrintSoftrow_Knob() {
         Knob knob = new Knob();
 
         Program program = new Program();
         program.setSoftRowEffectType(0, 7);
         program.setSoftRowParameter(0, 0);
         program.setKnob(knob);
 
         String expected = "    1: Knob Value\n";
         String actual = ProgramPrinter.printSoftRow(program, 0);
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintPatch_Knob() {
         OneBandMono oneBandMono = new OneBandMono();
 
         Patch patch = new Patch();
         patch.setSource(3);
         patch.setSourceMin(0);
         patch.setSourceMax(25);
         patch.setDestinationEffect(5);
         patch.setDestinationParameter(2);
         GenericValue<Integer> destinationMin = new GenericValue<Integer>("Min", "dB", -72, 24);
         destinationMin.setValue(0);
         patch.setDestinationMin(destinationMin);
         GenericValue<Integer> destinationMid = new GenericValue<Integer>("Mid", "dB", -72, 24);
         destinationMid.setValue(null);
         patch.setDestinationMid(destinationMid);
         GenericValue<Integer> destinationMax = new GenericValue<Integer>("Max", "dB", -72, 24);
         destinationMax.setValue(24);
         patch.setDestinationMax(destinationMax);
 
         Knob knob = new Knob();
 
         Program program = new Program();
         program.setEq(oneBandMono);
         program.setKnob(knob);
 
         String expected = "    Patch 1:\n      Source: Ctls Knob\n        Min: 0\n        Mid: --\n        Max: 25\n      Destination: Eq Gain\n        Min: 0dB\n        Mid: --\n        Max: +24dB\n";
         String actual = ProgramPrinter.printPatch(program, patch, 1);
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintPatch_toSendLevel() {
         Patch patch = new Patch();
         patch.setSource(3);
         patch.setSourceMin(0);
         patch.setSourceMax(25);
         patch.setDestinationEffect(16);
         patch.setDestinationParameter(0);
         GenericValue<Integer> destinationMin = new GenericValue<Integer>("Min", "", -56, 0);
         destinationMin.setValue(0);
         patch.setDestinationMin(destinationMin);
         GenericValue<Integer> destinationMid = new GenericValue<Integer>("Mid", "", -56, 0);
         destinationMid.setValue(null);
         patch.setDestinationMid(destinationMid);
         GenericValue<Integer> destinationMax = new GenericValue<Integer>("Max", "", -56, 0);
         destinationMax.setValue(6);
         patch.setDestinationMax(destinationMax);
 
         Program program = new Program();
         program.setSendMix(new SendMix());
 
         String expected = "    Patch 1:\n      Source: Ctls Knob\n        Min: 0\n        Mid: --\n        Max: 25\n      Destination: Send Level\n        Min: 0\n        Mid: --\n        Max: +6\n";
         String actual = ProgramPrinter.printPatch(program, patch, 1);
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintKnob() {
         Knob knob = new Knob();
         knob.getValue().setValue(50);
         knob.getLow().setValue(0);
         knob.getHigh().setValue(100);
         knob.getName().setValue("Delay Adj");
 
         String expected = "      Value: 50\n      Low: 0\n      High: 100\n      Name: Delay Adj\n";
         String actual = ProgramPrinter.printKnob(knob);
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintLfo() {
         Lfo lfo = new Lfo();
         lfo.getMode().setValue(1);
         lfo.setRate(new FrequencyRate("Rate", 0.6));
         lfo.getPulseWidth().setValue(50);
         lfo.getPhase().setValue(0);
         lfo.getDepth().setValue(100);
         lfo.getOnLevel().setValue(64);
         lfo.getOnSource().setValue(0);
 
         String expected = "      Mode: On\n      Rate: 0.60Hz\n      PW: 50%\n      Phase: 0\n      Depth: 100%\n      OnLvl: 64\n      OnSrc: None\n";
         String actual = ProgramPrinter.printLfo(lfo);
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintRandom() {
         Random random = new Random();
         random.getLow().setValue(0);
         random.getHigh().setValue(50);
         random.setRate(new BeatRate("Rate", 3, 1));
 
         String expected = "      RndLo: 0\n      RndHi: 50\n      Rate: 3:1\n";
         String actual = ProgramPrinter.printRandom(random);
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintAb() {
         Ab ab = new Ab();
         ab.getMode().setValue(0);
         ab.getARate().setValue(100);
         ab.getBRate().setValue(10);
         ab.getOnLevel().setValue(64);
         ab.getOnSource().setValue(0);
 
         String expected = "      Mode: Trigger\n      ARate: 100\n      BRate: 10\n      OnLvl: 64\n      OnSrc: None\n";
         String actual = ProgramPrinter.printAb(ab);
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintEnvelopeGenerator() {
         EnvelopeGenerator envelopeGenerator = new EnvelopeGenerator();
         envelopeGenerator.getSrc1().setValue(1);
         envelopeGenerator.getSrc2().setValue(0);
         envelopeGenerator.getATrim().setValue(17);
         envelopeGenerator.getResponse().setValue(100);
 
         String expected = "      Src1: In\n      Src2: Off\n      ATrim: 17\n      Resp: 100\n";
         String actual = ProgramPrinter.printEnvelopeGenerator(envelopeGenerator);
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintSendMix() {
         SendMix sendMix = new SendMix();
         sendMix.getSendLevel().setValue(0);
         sendMix.getSendBypassLevel().setValue(0);
 
         String expected = "      Level: 0\n      Bypass Level: 0\n";
         assertThat(ProgramPrinter.printSendMix(sendMix), is(expected));
     }
 
     @Test
     public void testPrintPostMix() {
         PostMix postMix = new PostMix();
         postMix.getPostMix().setValue(100);
         postMix.getPostLevel().setValue(0);
         postMix.getPostBypassLevel().setValue(0);
 
         String expected = "      Mix: 100%\n      Level: 0dB\n      Bypass Level: 0dB\n";
         assertThat(ProgramPrinter.printPostMix(postMix), is(expected));
     }
 
     @Test
     public void testPrintTempo() {
         Tempo tempo = new Tempo();
         tempo.getRate().setValue(170);
         tempo.getSource().setValue(0);
         tempo.getBeatValue().setValue(2); // quater note
         tempo.getTapSource().setValue(0);
         tempo.getTapAverage().setValue(2);
         tempo.getTapSourceLevel().setValue(64);
 
        String expected = "    Rate: 170 BPM\n    Source: internal\n    Beat Value: quarter\n    Tap Average: 2 beats\n    Tap Source: None\n";
         assertThat(ProgramPrinter.printTempo(tempo), is(expected));
     }
 
     @Test
     public void testPrintNoiseGate() {
         NoiseGate noiseGate = new NoiseGate();
         noiseGate.getEnable().setValue(2);
         noiseGate.getSend().setValue(true);
         noiseGate.getThreshold().setValue(-31);
         noiseGate.getAttenuation().setValue(-7);
         noiseGate.getOffset().setValue(-11);
         noiseGate.getATime().setValue(1999);
         noiseGate.getHTime().setValue(499);
         noiseGate.getRTime().setValue(2000);
         noiseGate.getDelay().setValue(10);
 
         String expected = "    Enable: Returns Only\n    Send: On\n    Thrsh: -31dB\n    Atten: -7dB\n    Offset: -11dB\n    ATime: 1999\n    HTime: 499\n    RTime: 2000\n    Delay: 10\n";
         assertThat(ProgramPrinter.printNoiseGate(noiseGate), is(expected));
     }
 
 }
