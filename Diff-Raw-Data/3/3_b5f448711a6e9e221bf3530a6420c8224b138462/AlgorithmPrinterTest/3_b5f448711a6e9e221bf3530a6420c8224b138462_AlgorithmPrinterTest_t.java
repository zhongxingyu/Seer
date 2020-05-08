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
 
 import info.carlwithak.mpxg2.model.BeatRate;
 import info.carlwithak.mpxg2.model.FrequencyRate;
 import info.carlwithak.mpxg2.model.effects.algorithms.Ambience;
 import info.carlwithak.mpxg2.model.effects.algorithms.AutoPan;
 import info.carlwithak.mpxg2.model.effects.algorithms.Chamber;
 import info.carlwithak.mpxg2.model.effects.algorithms.ChorusAlgorithm;
 import info.carlwithak.mpxg2.model.effects.algorithms.DelayDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.DetuneDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.DetuneMono;
import info.carlwithak.mpxg2.model.effects.algorithms.EchoMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.EchoDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.EqPedalVol;
 import info.carlwithak.mpxg2.model.effects.algorithms.FlangerStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.Overdrive;
 import info.carlwithak.mpxg2.model.effects.algorithms.Panner;
 import info.carlwithak.mpxg2.model.effects.algorithms.PedalVol;
 import info.carlwithak.mpxg2.model.effects.algorithms.PedalWah1;
 import info.carlwithak.mpxg2.model.effects.algorithms.PedalWah2;
 import info.carlwithak.mpxg2.model.effects.algorithms.Plate;
 import info.carlwithak.mpxg2.model.effects.algorithms.Screamer;
 import info.carlwithak.mpxg2.model.effects.algorithms.ShiftDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.Tone;
 import info.carlwithak.mpxg2.model.effects.algorithms.UniVybe;
 import info.carlwithak.mpxg2.model.effects.algorithms.VolumeMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.Wah1;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * Tests AlgorithmPrinter and all the implementation of AlgorithmPrinter.Printer.
  *
  * @author Carl Green
  */
 public class AlgorithmPrinterTest {
 
     @Test(expected=PrintException.class)
     public void testPrintInvalidAlgorithm() throws PrintException {
         String notAnAlgorithm = "Not an algorithm";
         AlgorithmPrinter.print(notAnAlgorithm);
     }
 
     @Test
     public void testPrintShiftDual() throws PrintException {
         ShiftDual shiftDual = new ShiftDual();
         shiftDual.setMix(100);
         shiftDual.setLevel(6);
         shiftDual.setTune1(-1200);
         shiftDual.setOptimize(10);
         shiftDual.setTune2(-500);
         shiftDual.setGlide(true);
 
         String expected = "    Mix: 100%\n    Level: +6dB\n    Tune1: -1200\n    Optimize: 10\n    Tune2: -500\n    Glide: On\n";
         String actual = AlgorithmPrinter.print(shiftDual);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintPanner() throws PrintException {
         Panner panner = new Panner();
         panner.setMix(100);
         panner.setLevel(-24);
         panner.setPan1(-50);
         panner.setPan2(50);
 
         String expected = "    Mix: 100%\n    Level: -24dB\n    Pan1: 50L\n    Pan2: 50R\n";
         String actual = AlgorithmPrinter.print(panner);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintAutoPan() throws PrintException {
         AutoPan autoPan = new AutoPan();
         autoPan.setMix(100);
         autoPan.setLevel(0);
         autoPan.setRate(new FrequencyRate(1.0));
         autoPan.setPulseWidth(50);
         autoPan.setDepth(100);
         autoPan.setPhase(3);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Rate: 1.00Hz\n    PW: 50%\n    Depth: 100%\n    Phase: 270°\n";
         String actual = AlgorithmPrinter.print(autoPan);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintUniVybe() throws PrintException {
         UniVybe univybe = new UniVybe();
         univybe.setMix(100);
         univybe.setLevel(0);
         univybe.setRate(20);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Rate: 20\n";
         String actual = AlgorithmPrinter.print(univybe);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintWah1() throws PrintException {
         Wah1 wah1 = new Wah1();
         wah1.setMix(100);
         wah1.setLevel(0);
         wah1.setSweep(50);
         wah1.setBass(19);
         wah1.setType(0);
         wah1.setResponse(100);
         wah1.setGain(10);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Sweep: 50\n    Bass: 19\n    Type: Model C\n    Resp: 100\n    Gain: +10\n";
         String actual = AlgorithmPrinter.print(wah1);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintPedalWah1() throws PrintException {
         PedalWah1 pedalWah1 = new PedalWah1();
         pedalWah1.setMix(100);
         pedalWah1.setLevel(0);
         pedalWah1.setBass(19);
         pedalWah1.setType(0);
         pedalWah1.setResponse(100);
         pedalWah1.setGain(10);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Bass: 19\n    Type: Model C\n    Resp: 100\n    Gain: +10\n";
         String actual = AlgorithmPrinter.print(pedalWah1);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintPedalWah2() throws PrintException {
         PedalWah2 pedalWah2 = new PedalWah2();
         pedalWah2.setMix(100);
         pedalWah2.setLevel(0);
         pedalWah2.setBass(10);
         pedalWah2.setType(0);
         pedalWah2.setResponse(100);
         pedalWah2.setGain(6);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Bass: 10\n    Type: Model C\n    Resp: 100\n    Gain: +6\n";
         String actual = AlgorithmPrinter.print(pedalWah2);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintVolumeMono() throws PrintException {
         VolumeMono volumeMono = new VolumeMono();
         volumeMono.setMix(100);
         volumeMono.setLevel(0);
         volumeMono.setVolume(60);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Volume: 60%\n";
         String actual = AlgorithmPrinter.print(volumeMono);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintPedalVol() throws PrintException {
         PedalVol pedalVol = new PedalVol();
         pedalVol.setMix(100);
         pedalVol.setLevel(0);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n";
         String actual = AlgorithmPrinter.print(pedalVol);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintChorus() throws PrintException {
         ChorusAlgorithm chorus = new ChorusAlgorithm();
         chorus.setMix(100);
         chorus.setLevel(0);
         chorus.setRate1(new FrequencyRate(0.62));
         chorus.setPulseWidth1(45);
         chorus.setDepth1(30);
         chorus.setRate2(new FrequencyRate(0.56));
         chorus.setPulseWidth2(54);
         chorus.setDepth2(0);
         chorus.setResonance1(-19);
         chorus.setResonance2(0);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Rate1: 0.62Hz\n    PW1: 45%\n    Depth1: 30%\n    Rate2: 0.56Hz\n    PW2: 54%\n    Depth2: 0%\n    Res1: -19\n    Res2: 0\n";
         String actual = AlgorithmPrinter.print(chorus);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintDetuneMono() throws PrintException {
         DetuneMono detuneMono = new DetuneMono();
         detuneMono.setMix(100);
         detuneMono.setLevel(3);
         detuneMono.setTune(7);
         detuneMono.setOptimize(10);
         detuneMono.setPreDelay(22);
 
         String expected = "    Mix: 100%\n    Level: +3dB\n    Tune: 7\n    Optimize: 10ms\n    P Dly: 22ms\n";
         String actual = AlgorithmPrinter.print(detuneMono);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintDetuneDual() throws PrintException {
         DetuneDual detuneDual = new DetuneDual();
         detuneDual.setMix(100);
         detuneDual.setLevel(3);
         detuneDual.setTune1(7);
         detuneDual.setOptimize(10);
         detuneDual.setTune2(5);
         detuneDual.setPreDelay(22);
 
         String expected = "    Mix: 100%\n    Level: +3dB\n    Tune1: 7\n    Optimize: 10ms\n    Tune2: 5\n    P Dly: 22ms\n";
         String actual = AlgorithmPrinter.print(detuneDual);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintFlangerStereo() throws PrintException {
         FlangerStereo flangerStereo = new FlangerStereo();
         flangerStereo.setMix(67);
         flangerStereo.setLevel(1);
         flangerStereo.setRate(new BeatRate(1, 4));
         flangerStereo.setPulseWidth(50);
         flangerStereo.setDepth(62);
         flangerStereo.setPhase(1);
         flangerStereo.setResonance(20);
         flangerStereo.setBlend(0);
 
         String expected = "    Mix: 67%\n    Level: +1dB\n    Rate: 1:4\n    PW: 50%\n    Depth: 62%\n    Phase: 90°\n    Res: +20%\n    Blend: 0\n";
         String actual = AlgorithmPrinter.print(flangerStereo);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintDelayDual() throws PrintException {
         DelayDual delayDual = new DelayDual();
         delayDual.setMix(25);
         delayDual.setLevel(0);
         delayDual.setTime1Echoes(3);
         delayDual.setTime1Beat(4);
         delayDual.setTime2Echoes(4);
         delayDual.setTime2Beat(3);
         delayDual.setLevel1(0);
         delayDual.setLevel2(0);
         delayDual.setPan1(-50);
         delayDual.setPan2(50);
         delayDual.setFeedback1(10);
         delayDual.setInsert(3);
         delayDual.setFeedback2(10);
         delayDual.setXFbk1(0);
         delayDual.setXFbk2(0);
         delayDual.setClear(false);
 
         String expected = "    Mix: 25%\n    Level: 0dB\n    Time1: 3:4\n    Time2: 4:3\n    Level1: 0dB\n    Level2: 0dB\n    Pan1: 50L\n    Pan2: 50R\n    Feedback1: +10%\n    Insert: Delay\n    Feedback2: +10%\n    XFbk1: 0%\n    XFbk2: 0%\n    Clear: Off\n";
         String actual = AlgorithmPrinter.print(delayDual);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintEchoMono() throws PrintException {
         EchoMono echoMono = new EchoMono();
         echoMono.setMix(6);
         echoMono.setLevel(1);
         echoMono.setTimeEchoes(4);
         echoMono.setTimeBeat(4);
         echoMono.setFeedback(-15);
         echoMono.setInsert(3);
         echoMono.setDamp(20);
         echoMono.setClear(0);
 
         String expected = "    Mix: 6%\n    Level: +1dB\n    Time: 4:4\n    Feedback: -15%\n    Insert: Delay\n    Damp: 20%\n    Clear: off\n";
         String actual = AlgorithmPrinter.print(echoMono);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintEchoDual() throws PrintException {
         EchoDual echoDual = new EchoDual();
         echoDual.setMix(2);
         echoDual.setLevel(1);
         echoDual.setTime1Echoes(4);
         echoDual.setTime1Beat(4);
         echoDual.setTime2Echoes(2);
         echoDual.setTime2Beat(1);
         echoDual.setLevel1(0);
         echoDual.setLevel2(0);
         echoDual.setFeedback1(1);
         echoDual.setInsert(3);
         echoDual.setFeedback2(1);
         echoDual.setDamp1(20);
         echoDual.setDamp2(20);
         echoDual.setClear(0);
 
         String expected = "    Mix: 2%\n    Level: +1dB\n    Time1: 4:4\n    Time2: 2:1\n    Level1: 0dB\n    Level2: 0dB\n    Feedback1: +1%\n    Insert: Delay\n    Feedback2: +1%\n    Damp1: 20%\n    Damp2: 20%\n    Clear: off\n";
         String actual = AlgorithmPrinter.print(echoDual);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintChamber() throws PrintException {
         Chamber chamber = new Chamber();
         chamber.setMix(28);
         chamber.setLevel(0);
         chamber.setSize(24.0);
         chamber.setLink(1);
         chamber.setDiff(22);
         chamber.setPreDelay(0);
         chamber.setBass(6);
         chamber.setDecay(47);
         chamber.setXovr(16);
         chamber.setRtHC(34);
         chamber.setShape(62);
         chamber.setSpred(120);
 
         String expected = "    Mix: 28%\n    Level: 0dB\n    Size: 24.0m\n    Link: on\n    Diff: 22%\n    Pre Delay: 0ms\n    Bass: 1.5X\n    Decay: 1.05s\n    Xovr: 986\n    Rt HC: 9.3k\n    Shape: 62\n    Spred: 42\n";
         String actual = AlgorithmPrinter.print(chamber);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintPlate() throws PrintException {
         Plate plate = new Plate();
         plate.setMix(100);
         plate.setLevel(6);
         plate.setSize(22.5);
         plate.setLink(1);
         plate.setDiff(66);
         plate.setPreDelay(169);
         plate.setBass(5);
         plate.setDecay(50);
         plate.setXovr(16);
         plate.setRtHC(45);
         plate.setShape(36);
         plate.setSpred(222);
 
         String expected = "    Mix: 100%\n    Level: +6dB\n    Size: 22.5m\n    Link: on\n    Diff: 66%\n    Pre Delay: 169ms\n    Bass: 1.2X\n    Decay: 1.30s\n    Xovr: 986\n    Rt HC: 19.4k\n    Shape: 36\n    Spred: 73\n";
         String actual = AlgorithmPrinter.print(plate);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintAmbience() throws PrintException {
         Ambience ambience = new Ambience();
         ambience.setMix(18);
         ambience.setLevel(0);
         ambience.setSize(24.5);
         ambience.setLink(1);
         ambience.setDiff(60);
         ambience.setPreDelay(7);
         ambience.setDelayTime(51);
         ambience.setDelayLevel(0);
         ambience.setRtHC(12);
 
         String expected = "    Mix: 18%\n    Level: 0dB\n    Size: 24.5m\n    Link: on\n    Diff: 60%\n    Pre Delay: 7ms\n    Delay Time: 1.41s\n    Delay Level: off\n    Rt HC: 12.8k\n";
         String actual = AlgorithmPrinter.print(ambience);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintEqPedalVol() throws PrintException {
         EqPedalVol pedalVol = new EqPedalVol();
         pedalVol.setMix(100);
         pedalVol.setLevel(0);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n";
         String actual = AlgorithmPrinter.print(pedalVol);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintTone() throws PrintException {
         Tone tone = new Tone();
         tone.setLo(25);
         tone.setMid(10);
         tone.setHi(20);
         tone.setInLevel(0);
         tone.setLevel(55);
 
         String expected = "    Lo: +25\n    Mid: +10\n    Hi: +20\n    InLvl: 0\n    Level: 55\n";
         String actual = AlgorithmPrinter.print(tone);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintScreamer() throws PrintException {
         Screamer screamer = new Screamer();
         screamer.setLo(2);
         screamer.setMid(1);
         screamer.setHi(3);
         screamer.setDrive(22);
         screamer.setTone(25);
         screamer.setLevel(57);
 
         String expected = "    Lo: +2\n    Mid: +1\n    Hi: +3\n    Drive: 22\n    Tone: 25\n    Level: 57\n";
         String actual = AlgorithmPrinter.print(screamer);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintOverdrive() throws PrintException {
         Overdrive overdrive = new Overdrive();
         overdrive.setLo(4);
         overdrive.setMid(8);
         overdrive.setHi(0);
         overdrive.setInLevel(-8);
         overdrive.setLoCut(0);
         overdrive.setFeel(32);
         overdrive.setDrive(40);
         overdrive.setTone(21);
         overdrive.setLevel(44);
 
         String expected = "    Lo: +4\n    Mid: +8\n    Hi: 0\n    InLvl: -8\n    LoCut: 0\n    Feel: 32\n    Drive: 40\n    Tone: 21\n    Level: 44\n";
         String actual = AlgorithmPrinter.print(overdrive);
 
         assertEquals(expected, actual);
     }
 }
