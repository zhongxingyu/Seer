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
 import info.carlwithak.mpxg2.model.effects.algorithms.Aerosol;
 import info.carlwithak.mpxg2.model.effects.algorithms.Ambience;
 import info.carlwithak.mpxg2.model.effects.algorithms.AutoPan;
 import info.carlwithak.mpxg2.model.effects.algorithms.BlueComp;
 import info.carlwithak.mpxg2.model.effects.algorithms.Centrifuge1;
 import info.carlwithak.mpxg2.model.effects.algorithms.Chamber;
 import info.carlwithak.mpxg2.model.effects.algorithms.ChorusAlgorithm;
 import info.carlwithak.mpxg2.model.effects.algorithms.ChorusDetuneMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.ChorusPedalVol;
 import info.carlwithak.mpxg2.model.effects.algorithms.ChorusVolumeDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.ChorusVolumeMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.ChorusVolumeStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.Crossover;
 import info.carlwithak.mpxg2.model.effects.algorithms.Crunch;
 import info.carlwithak.mpxg2.model.effects.algorithms.CustomVybe;
 import info.carlwithak.mpxg2.model.effects.algorithms.DelayDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.DelayMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.DelayStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.DetuneDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.DiatonicHmy;
 import info.carlwithak.mpxg2.model.effects.algorithms.Distortion;
 import info.carlwithak.mpxg2.model.effects.algorithms.EchoDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.EchoMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.EchoStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.EqPedalVol;
 import info.carlwithak.mpxg2.model.effects.algorithms.EqVolumeDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.EqVolumeMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.EqVolumeStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.Flanger24Mono;
 import info.carlwithak.mpxg2.model.effects.algorithms.FlangerMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.FlangerStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.Hall;
 import info.carlwithak.mpxg2.model.effects.algorithms.JamMan;
 import info.carlwithak.mpxg2.model.effects.algorithms.OctaBuzz;
 import info.carlwithak.mpxg2.model.effects.algorithms.OneBandMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.OrangePhase;
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
 import info.carlwithak.mpxg2.model.effects.algorithms.SweepFilter;
 import info.carlwithak.mpxg2.model.effects.algorithms.Tone;
 import info.carlwithak.mpxg2.model.effects.algorithms.TremoloMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.TremoloStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.TwoBandMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.TwoBandStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.UniVybe;
 import info.carlwithak.mpxg2.model.effects.algorithms.VolumeDual;
 import info.carlwithak.mpxg2.model.effects.algorithms.VolumeMono;
 import info.carlwithak.mpxg2.model.effects.algorithms.VolumeStereo;
 import info.carlwithak.mpxg2.model.effects.algorithms.Wah1;
 import info.carlwithak.mpxg2.model.effects.algorithms.Wah2;
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
     public void testPrintShiftMono() throws PrintException {
         ShiftMono shiftMono = new ShiftMono();
         shiftMono.setMix(100);
         shiftMono.setLevel(-90);
         shiftMono.setTune(-1200);
         shiftMono.setOptimize(50);
         shiftMono.setGlide(true);
 
         // TODO level should be 'Off'
         String expected = "    Mix: 100%\n    Level: -90dB\n    Tune: -1200\n    Optimize: 50\n    Glide: On\n";
         String actual = AlgorithmPrinter.print(shiftMono);
 
         assertEquals(expected, actual);
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
     public void testPrintDiatonicHmy() throws PrintException {
         DiatonicHmy diatonicHmy = new DiatonicHmy();
         diatonicHmy.setMix(100);
         diatonicHmy.setLevel(0);
         diatonicHmy.setKey(4);
         diatonicHmy.setScale(0);
         diatonicHmy.setInterval(16);
         diatonicHmy.setOptimize(10);
         diatonicHmy.setThreshold(-83);
         diatonicHmy.setSource(1);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Key: E\n    Scale: Major\n    Int: +3rd\n    Optimize: 10\n    Thrsh: -83dB\n    Src: Guitar Input\n";
         String actual = AlgorithmPrinter.print(diatonicHmy);
 
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
         autoPan.setRate(new FrequencyRate("Rate", 1.0));
         autoPan.setPulseWidth(50);
         autoPan.setDepth(100);
         autoPan.setPhase(3);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Rate: 1.00Hz\n    PW: 50%\n    Depth: 100%\n    Phase: 270°\n";
         String actual = AlgorithmPrinter.print(autoPan);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintTremoloMono() throws PrintException {
         TremoloMono tremoloMono = new TremoloMono();
         tremoloMono.setMix(100);
         tremoloMono.setLevel(6);
         tremoloMono.setRate(new BeatRate("Rate", 7, 4));
         tremoloMono.setPulseWidth(30);
         tremoloMono.setDepth(100);
 
         String expected = "    Mix: 100%\n    Level: +6dB\n    Rate: 7:4\n    PW: 30%\n    Depth: 100%\n";
         String actual = AlgorithmPrinter.print(tremoloMono);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintTremoloStereo() throws PrintException {
         TremoloStereo tremoloStereo = new TremoloStereo();
         tremoloStereo.setMix(100);
         tremoloStereo.setLevel(0);
         tremoloStereo.setRate(new FrequencyRate("Rate", 3.0));
         tremoloStereo.setPulseWidth(50);
         tremoloStereo.setDepth(100);
         tremoloStereo.setPhase(3);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Rate: 3.00Hz\n    PW: 50%\n    Depth: 100%\n    Phase: 270°\n";
         String actual = AlgorithmPrinter.print(tremoloStereo);
 
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
     public void testPrintCustomVybe() throws PrintException {
         CustomVybe customVybe = new CustomVybe();
         customVybe.setMix(100);
         customVybe.setLevel(0);
         customVybe.setRate(new FrequencyRate("Rate", 0.97));
         customVybe.setPulseWidth(46);
         customVybe.setDepth(45);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Rate: 0.97Hz\n    PW: 46%\n    Depth: 45%\n";
         String actual = AlgorithmPrinter.print(customVybe);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintPhaser() throws PrintException {
         Phaser phaser = new Phaser();
         phaser.setMix(100);
         phaser.setLevel(0);
         phaser.setRate(new FrequencyRate("Rate", 0.83));
         phaser.setPulseWidth(50);
         phaser.setDepth(100);
         phaser.setResonance(30);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Rate: 0.83Hz\n    PW: 50%\n    Depth: 100%\n    Res: +30%\n";
         String actual = AlgorithmPrinter.print(phaser);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintRedComp() throws PrintException {
         RedComp redComp = new RedComp();
         redComp.setMix(100);
         redComp.setLevel(6);
         redComp.setSensitivity(70);
 
         String expected = "    Mix: 100%\n    Level: +6dB\n    Sense: 70\n";
         String actual = AlgorithmPrinter.print(redComp);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintBlueComp() throws PrintException {
         BlueComp blueComp = new BlueComp();
         blueComp.setMix(100);
         blueComp.setLevel(6);
         blueComp.setSensitivity(5);
         blueComp.setThreshold(-28);
         blueComp.setGain(5);
         blueComp.setAttackTime(20);
         blueComp.setReleaseTime(100);
 
         String expected = "    Mix: 100%\n    Level: +6dB\n    Sense: +5dB\n    Thrsh: -28dB\n    Gain: +5dB\n    ATime: 20ms\n    RTime: 100ms\n";
         String actual = AlgorithmPrinter.print(blueComp);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintOrangePhase() throws PrintException {
         OrangePhase orangePhase = new OrangePhase();
         orangePhase.setMix(100);
         orangePhase.setLevel(0);
         orangePhase.setRate(20);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Rate: 20\n";
         String actual = AlgorithmPrinter.print(orangePhase);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintOctaBuzz() throws PrintException {
         OctaBuzz octaBuzz = new OctaBuzz();
         octaBuzz.setMix(100);
         octaBuzz.setLevel(3);
 
         String expected = "    Mix: 100%\n    Level: +3dB\n";
         String actual = AlgorithmPrinter.print(octaBuzz);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintSweepFilter() throws PrintException {
         SweepFilter sweepFilter = new SweepFilter();
         sweepFilter.setMix(100);
         sweepFilter.setLevel(6);
         sweepFilter.setFc(88);
         sweepFilter.setFRes(34);
         sweepFilter.setMod(2120);
         sweepFilter.setScale(50);
         sweepFilter.setPan(0);
 
         String expected = "    Mix: 100%\n    Level: +6dB\n    Fc: 88Hz\n    FRes: 34\n    Mod: 2120Hz\n    Scale: +50%\n    Pan: C\n";
         String actual = AlgorithmPrinter.print(sweepFilter);
 
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
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Sweep: 50\n    Bass: 19\n    Type: Model C\n    Resp: 100\n    Gain: +10dB\n";
         String actual = AlgorithmPrinter.print(wah1);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintWah2() throws PrintException {
         Wah2 wah2 = new Wah2();
         wah2.setMix(100);
         wah2.setLevel(6);
         wah2.setSweep(50);
         wah2.setBass(0);
         wah2.setType(0);
         wah2.setResponse(100);
         wah2.setGain(10);
 
         String expected = "    Mix: 100%\n    Level: +6dB\n    Sweep: 50\n    Bass: 0\n    Type: Model C\n    Resp: 100\n    Gain: +10dB\n";
         String actual = AlgorithmPrinter.print(wah2);
 
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
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Bass: 19\n    Type: Model C\n    Resp: 100\n    Gain: +10dB\n";
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
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Bass: 10\n    Type: Model C\n    Resp: 100\n    Gain: +6dB\n";
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
     public void testPrintVolumeStereo() throws PrintException {
         VolumeStereo volumeStereo = new VolumeStereo();
         volumeStereo.setMix(100);
         volumeStereo.setLevel(0);
         volumeStereo.setVolume(0);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Volume: 0%\n";
         String actual = AlgorithmPrinter.print(volumeStereo);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintVolumeDual() throws PrintException {
         VolumeDual volumeDual = new VolumeDual();
         volumeDual.setMix(100);
         volumeDual.setLevel(0);
         volumeDual.setVolumeLeft(100);
         volumeDual.setVolumeRight(100);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Vol-L: 100%\n    Vol-R: 100%\n";
         String actual = AlgorithmPrinter.print(volumeDual);
 
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
         chorus.setRate1(new FrequencyRate("Rate1", 0.62));
         chorus.setPulseWidth1(45);
         chorus.setDepth1(30);
         chorus.setRate2(new FrequencyRate("Rate2", 0.56));
         chorus.setPulseWidth2(54);
         chorus.setDepth2(0);
         chorus.setResonance1(-19);
         chorus.setResonance2(0);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Rate1: 0.62Hz\n    PW 1: 45%\n    Dpth1: 30%\n    Rate2: 0.56Hz\n    PW 2: 54%\n    Dpth2: 0%\n    Res 1: -19\n    Res 2: 0\n";
         String actual = AlgorithmPrinter.print(chorus);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintChorusDetuneMono() throws PrintException {
         ChorusDetuneMono detuneMono = new ChorusDetuneMono();
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
     public void testPrintFlangerMono() throws PrintException {
         FlangerMono flangerMono = new FlangerMono();
         flangerMono.setMix(100);
         flangerMono.setLevel(0);
         flangerMono.setRate(new FrequencyRate("Rate", 0.05));
         flangerMono.setPulseWidth(20);
         flangerMono.setDepth(30);
         flangerMono.setResonance(-60);
         flangerMono.setBlend(30);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Rate: 0.05Hz\n    PW: 20%\n    Depth: 30%\n    Res: -60%\n    Blend: 30%\n";
         String actual = AlgorithmPrinter.print(flangerMono);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintFlanger24Mono() throws PrintException {
         Flanger24Mono flanger24Mono = new Flanger24Mono();
         flanger24Mono.setMix(100);
         flanger24Mono.setLevel(0);
         flanger24Mono.setRate(new FrequencyRate("Rate", 0.31));
         flanger24Mono.setPulseWidth(50);
         flanger24Mono.setDepth(36);
         flanger24Mono.setResonance(-30);
         flanger24Mono.setGlide(50);
         flanger24Mono.setBlend(50);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Rate: 0.31Hz\n    PW: 50%\n    Depth: 36%\n    Res: -30%\n    Glide: 50\n    Blend: 50%\n";
         String actual = AlgorithmPrinter.print(flanger24Mono);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintFlangerStereo() throws PrintException {
         FlangerStereo flangerStereo = new FlangerStereo();
         flangerStereo.setMix(67);
         flangerStereo.setLevel(1);
         flangerStereo.setRate(new BeatRate("Rate", 1, 4));
         flangerStereo.setPulseWidth(50);
         flangerStereo.setDepth(62);
         flangerStereo.setPhase(1);
         flangerStereo.setResonance(20);
         flangerStereo.setBlend(0);
 
         String expected = "    Mix: 67%\n    Level: +1dB\n    Rate: 1:4\n    PW: 50%\n    Depth: 62%\n    Phase: 90°\n    Res: +20%\n    Blend: 0%\n";
         String actual = AlgorithmPrinter.print(flangerStereo);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintRotaryCab() throws PrintException {
         RotaryCab rotaryCab = new RotaryCab();
         rotaryCab.setMix(100);
         rotaryCab.setLevel(4);
         rotaryCab.setRate1(new FrequencyRate("Rate1", 5.73));
         rotaryCab.setDepth1(43);
         rotaryCab.setRate2(new FrequencyRate("Rate2", 5.73));
         rotaryCab.setDepth2(36);
         rotaryCab.setResonance(0);
         rotaryCab.setWidth(100);
         rotaryCab.setBalance(-20);
 
         String expected = "    Mix: 100%\n    Level: +4dB\n    Rate1: 5.73Hz\n    Depth1: 43%\n    Rate2: 5.73Hz\n    Depth2: 36%\n    Res: 0\n    Width: 100%\n    Bal: -20\n";
         String actual = AlgorithmPrinter.print(rotaryCab);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintAerosol() throws PrintException {
         Aerosol aerosol = new Aerosol();
         aerosol.setMix(100);
         aerosol.setLevel(0);
         aerosol.setRate1(new FrequencyRate("Rate1", 0.26));
         aerosol.setPulseWidth1(45);
         aerosol.setDepth1(70);
         aerosol.setRate2(new FrequencyRate("Rate2", 0.26));
         aerosol.setPulseWidth2(55);
         aerosol.setDepth2(60);
         aerosol.setResonance1(-60);
         aerosol.setResonance2(60);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Rate1: 0.26Hz\n    PW 1: 45%\n    Dpth1: 70%\n    Rate2: 0.26Hz\n    PW 2: 55%\n    Dpth2: 60%\n    Res 1: -60\n    Res 2: +60\n";
         String actual = AlgorithmPrinter.print(aerosol);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintCentrifuge1() throws PrintException {
         Centrifuge1 centrifuge1 = new Centrifuge1();
         centrifuge1.setMix(100);
         centrifuge1.setLevel(6);
         centrifuge1.setRate1(new FrequencyRate("Rate1", 0.6));
         centrifuge1.setPulseWidth1(45);
         centrifuge1.setSync1(120);
         centrifuge1.setDepth1(100);
         centrifuge1.setRate2(new FrequencyRate("Rate2", 1.0));
         centrifuge1.setPulseWidth2(100);
         centrifuge1.setSync2(-120);
         centrifuge1.setDepth2(43);
         centrifuge1.setResonance(100);
 
         String expected = "    Mix: 100%\n    Level: +6dB\n    Rate1: 0.60Hz\n    PW 1: 45%\n    Sync1: +120\n    Dpth1: 100%\n    Rate2: 1.00Hz\n    PW 2: 100%\n    Sync2: -120\n    Dpth2: 43%\n    Res: +100%\n";
         String actual = AlgorithmPrinter.print(centrifuge1);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintChorusVolumeMono() throws PrintException {
         ChorusVolumeMono volumeMono = new ChorusVolumeMono();
         volumeMono.setMix(100);
         volumeMono.setLevel(0);
         volumeMono.setVolume(100);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Volume: 100%\n";
         String actual = AlgorithmPrinter.print(volumeMono);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintChorusVolumeStereo() throws PrintException {
         ChorusVolumeStereo volumeStereo = new ChorusVolumeStereo();
         volumeStereo.setMix(100);
         volumeStereo.setLevel(0);
         volumeStereo.setVolume(0);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Volume: 0%\n";
         String actual = AlgorithmPrinter.print(volumeStereo);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintChorusVolumeDual() throws PrintException {
         ChorusVolumeDual volumeDual = new ChorusVolumeDual();
         volumeDual.setMix(100);
         volumeDual.setLevel(0);
         volumeDual.setVolumeLeft(80);
         volumeDual.setVolumeRight(90);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Vol-L: 80%\n    Vol-R: 90%\n";
         String actual = AlgorithmPrinter.print(volumeDual);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintChorusPedalVol() throws PrintException {
         ChorusPedalVol pedalVol = new ChorusPedalVol();
         pedalVol.setMix(100);
         pedalVol.setLevel(0);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n";
         String actual = AlgorithmPrinter.print(pedalVol);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintDelayMono() throws PrintException {
         DelayMono delayMono = new DelayMono();
         delayMono.setMix(10);
         delayMono.setLevel(0);
         delayMono.setTime(new BeatRate("Time", 4, 3));
         delayMono.setFeedback(10);
         delayMono.setInsert(3);
         delayMono.setClear(false);
 
         String expected = "    Mix: 10%\n    Level: 0dB\n    Time: 4:3\n    Fbk: +10%\n    Fbk insert: Delay\n    Clear: Off\n";
         String actual = AlgorithmPrinter.print(delayMono);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintDelayStereo() throws PrintException {
         DelayStereo delayStereo = new DelayStereo();
         delayStereo.setMix(20);
         delayStereo.setLevel(0);
         delayStereo.setTime(new BeatRate("Time", 2, 4));
         delayStereo.setFeedback(20);
         delayStereo.setInsert(3);
         delayStereo.setClear(false);
 
         String expected = "    Mix: 20%\n    Level: 0dB\n    Time: 2:4\n    Fbk: +20%\n    Fbk insert: Delay\n    Clear: Off\n";
         String actual = AlgorithmPrinter.print(delayStereo);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintDelayDual() throws PrintException {
         DelayDual delayDual = new DelayDual();
         delayDual.setMix(25);
         delayDual.setLevel(0);
         delayDual.setTime1(new BeatRate("Time1", 3, 4));
         delayDual.setTime2(new BeatRate("Time2", 4, 3));
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
 
         String expected = "    Mix: 25%\n    Level: 0dB\n    Time1: 3:4\n    Time2: 4:3\n    Lvl 1: 0dB\n    Lvl 2: 0dB\n    Pan 1: 50L\n    Pan 2: 50R\n    Fbk 1: +10%\n    Fbk insert: Delay\n    Fbk 2: +10%\n    XFbk1: 0%\n    XFbk2: 0%\n    Clear: Off\n";
         String actual = AlgorithmPrinter.print(delayDual);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintEchoMono() throws PrintException {
         EchoMono echoMono = new EchoMono();
         echoMono.setMix(6);
         echoMono.setLevel(1);
         echoMono.setTime(new BeatRate("Time", 4, 4));
         echoMono.setFeedback(-15);
         echoMono.setInsert(3);
         echoMono.setDamp(20);
         echoMono.setClear(false);
 
         String expected = "    Mix: 6%\n    Level: +1dB\n    Time: 4:4\n    Fbk: -15%\n    Fbk insert: Delay\n    Damp: 20%\n    Clear: Off\n";
         String actual = AlgorithmPrinter.print(echoMono);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintEchoStereo() throws PrintException {
         EchoStereo echoStereo = new EchoStereo();
         echoStereo.setMix(0);
         echoStereo.setLevel(0);
         echoStereo.setTime(new BeatRate("Time", 1, 1));
         echoStereo.setFeedback(0);
         echoStereo.setInsert(3);
         echoStereo.setDamp(16);
         echoStereo.setClear(false);
 
         String expected = "    Mix: 0%\n    Level: 0dB\n    Time: 1:1\n    Fbk: 0%\n    Fbk insert: Delay\n    Damp: 16%\n    Clear: Off\n";
         String actual = AlgorithmPrinter.print(echoStereo);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintEchoDual() throws PrintException {
         EchoDual echoDual = new EchoDual();
         echoDual.setMix(2);
         echoDual.setLevel(1);
         echoDual.setTime1(new BeatRate("Time1", 4, 4));
         echoDual.setTime2(new BeatRate("Time2", 2, 1));
         echoDual.setLevel1(0);
         echoDual.setLevel2(0);
         echoDual.setFeedback1(1);
         echoDual.setInsert(3);
         echoDual.setFeedback2(1);
         echoDual.setDamp1(20);
         echoDual.setDamp2(20);
         echoDual.setClear(false);
 
         String expected = "    Mix: 2%\n    Level: +1dB\n    Time1: 4:4\n    Time2: 2:1\n    Lvl 1: 0dB\n    Lvl 2: 0dB\n    Fbk 1: +1%\n    Fbk insert: Delay\n    Fbk 2: +1%\n    Damp1: 20%\n    Damp2: 20%\n    Clear: Off\n";
         String actual = AlgorithmPrinter.print(echoDual);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintJamMan() throws PrintException {
         JamMan jamMan = new JamMan();
         jamMan.setMix(100);
         jamMan.setLevel(0);
         jamMan.setSize(250);
         jamMan.setFeedback(0);
         jamMan.setInsert(3);
         jamMan.setClear(false);
         jamMan.setLayer(false);
         jamMan.setReplace(false);
         jamMan.setDelay(false);
         jamMan.setMute(false);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Size: 250ms\n    Fbk: 0%\n    Fbk insert: Delay\n    Clear: Off\n    Layer: Off\n    Replc: Off\n    Delay: Off\n    MuteS: Off\n";
         String actual = AlgorithmPrinter.print(jamMan);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintChamber() throws PrintException {
         Chamber chamber = new Chamber();
         chamber.setMix(28);
         chamber.setLevel(0);
         chamber.setSize(24.0);
         chamber.setLink(true);
         chamber.setDiff(22);
         chamber.setPreDelay(0);
         chamber.setBass(6);
         chamber.setDecay(47);
         chamber.setXovr(16);
         chamber.setRtHC(34);
         chamber.setShape(62);
         chamber.setSpred(120);
 
         String expected = "    Mix: 28%\n    Level: 0dB\n    Size: 24.0m\n    Link: On\n    Diff: 22%\n    P Dly: 0ms\n    Bass: 1.5X\n    Decay: 1.05s\n    Xovr: 986\n    Rt HC: 9.3k\n    Shape: 62\n    Spred: 42\n";
         String actual = AlgorithmPrinter.print(chamber);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintHall() throws PrintException {
         Hall hall = new Hall();
         hall.setMix(20);
         hall.setLevel(0);
         hall.setSize(53.0);
         hall.setLink(true);
         hall.setDiff(80);
         hall.setPreDelay(25);
         hall.setBass(5);
         hall.setDecay(41);
         hall.setXovr(15);
         hall.setRtHC(31);
         hall.setShape(110);
         hall.setSpred(125);
 
         String expected = "    Mix: 20%\n    Level: 0dB\n    Size: 53.0m\n    Link: On\n    Diff: 80%\n    P Dly: 25ms\n    Bass: 1.2X\n    Decay: 1.67s\n    Xovr: 818\n    Rt HC: 7.9k\n    Shape: 110\n    Spred: 89\n";
         String actual = AlgorithmPrinter.print(hall);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintPlate() throws PrintException {
         Plate plate = new Plate();
         plate.setMix(100);
         plate.setLevel(6);
         plate.setSize(22.5);
         plate.setLink(true);
         plate.setDiff(66);
         plate.setPreDelay(169);
         plate.setBass(5);
         plate.setDecay(50);
         plate.setXovr(16);
         plate.setRtHC(45);
         plate.setShape(36);
         plate.setSpred(222);
 
         String expected = "    Mix: 100%\n    Level: +6dB\n    Size: 22.5m\n    Link: On\n    Diff: 66%\n    Pre Delay: 169ms\n    Bass: 1.2X\n    Decay: 1.30s\n    Xovr: 986\n    Rt HC: 19.4k\n    Shape: 36\n    Spred: 73\n";
         String actual = AlgorithmPrinter.print(plate);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintAmbience() throws PrintException {
         Ambience ambience = new Ambience();
         ambience.setMix(18);
         ambience.setLevel(0);
         ambience.setSize(24.5);
         ambience.setLink(true);
         ambience.setDiff(60);
         ambience.setPreDelay(7);
         ambience.setDelayTime(51);
         ambience.setDelayLevel(0);
         ambience.setRtHC(12);
 
         String expected = "    Mix: 18%\n    Level: 0dB\n    Size: 24.5m\n    Link: On\n    Diff: 60%\n    P Dly: 7ms\n    DTime: 1.41s\n    D Lvl: Off\n    Rt HC: 12.8k\n";
         String actual = AlgorithmPrinter.print(ambience);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintOneBandMono() throws PrintException {
         OneBandMono oneBandMono = new OneBandMono();
         oneBandMono.setMix(100);
         oneBandMono.setLevel(0);
         oneBandMono.setGain(9);
         oneBandMono.setFc(393);
         oneBandMono.setQ(0.1);
         oneBandMono.setMode(0);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Gain: +9dB\n    Fc: 393Hz\n    Q: 0.1\n    Mode: LShlf\n";
         String actual = AlgorithmPrinter.print(oneBandMono);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintTwoBandMono() throws PrintException {
         TwoBandMono twoBandMono = new TwoBandMono();
         twoBandMono.setMix(100);
         twoBandMono.setLevel(0);
         twoBandMono.setGain1(-5);
         twoBandMono.setFc1(200);
         twoBandMono.setQ1(0.7);
         twoBandMono.setMode1(0);
         twoBandMono.setGain2(-7);
         twoBandMono.setFc2(5000);
         twoBandMono.setQ2(0.7);
         twoBandMono.setMode2(2);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Gain1: -5dB\n    Fc 1: 200Hz\n    Q 1: 0.7\n    Mode1: LShlf\n    Gain2: -7dB\n    Fc 2: 5000Hz\n    Q 2: 0.7\n    Mode2: HShlf\n";
         String actual = AlgorithmPrinter.print(twoBandMono);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintTwoBandStereo() throws PrintException {
         TwoBandStereo twoBandStereo = new TwoBandStereo();
         twoBandStereo.setMix(100);
         twoBandStereo.setLevel(-2);
         twoBandStereo.setGain1(5);
         twoBandStereo.setFc1(5050);
         twoBandStereo.setQ1(0.1);
         twoBandStereo.setMode1(0);
         twoBandStereo.setGain2(8);
         twoBandStereo.setFc2(20);
         twoBandStereo.setQ2(0.1);
         twoBandStereo.setMode2(1);
 
         String expected = "    Mix: 100%\n    Level: -2dB\n    Gain1: +5dB\n    Fc 1: 5050Hz\n    Q 1: 0.1\n    Mode1: LShlf\n    Gain2: +8dB\n    Fc 2: 20Hz\n    Q 2: 0.1\n    Mode2: Band\n";
         String actual = AlgorithmPrinter.print(twoBandStereo);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintCrossover() throws PrintException {
         Crossover crossover = new Crossover();
         crossover.setMix(100);
         crossover.setLevel(0);
         crossover.setFc(3000);
         crossover.setBalance(4);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Fc: 3000Hz\n    Bal: +4\n";
         String actual = AlgorithmPrinter.print(crossover);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintEqVolumeMono() throws PrintException {
         EqVolumeMono volumeMono = new EqVolumeMono();
         volumeMono.setMix(100);
         volumeMono.setLevel(0);
         volumeMono.setVolume(100);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Volume: 100%\n";
         String actual = AlgorithmPrinter.print(volumeMono);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintEqVolumeStereo() throws PrintException {
         EqVolumeStereo volumeStereo = new EqVolumeStereo();
         volumeStereo.setMix(100);
         volumeStereo.setLevel(0);
         volumeStereo.setVolume(0);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Volume: 0%\n";
         String actual = AlgorithmPrinter.print(volumeStereo);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintEqVolumeDual() throws PrintException {
         EqVolumeDual volumeDual = new EqVolumeDual();
         volumeDual.setMix(100);
         volumeDual.setLevel(0);
         volumeDual.setVolumeLeft(0);
         volumeDual.setVolumeRight(100);
 
         String expected = "    Mix: 100%\n    Level: 0dB\n    Vol-L: 0%\n    Vol-R: 100%\n";
         String actual = AlgorithmPrinter.print(volumeDual);
 
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
 
         String expected = "    Lo: +25dB\n    Mid: +10dB\n    Hi: +20dB\n    InLvl: 0dB\n    Level: 55dB\n";
         String actual = AlgorithmPrinter.print(tone);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintCrunch() throws PrintException {
         Crunch crunch = new Crunch();
         crunch.setLo(6);
         crunch.setMid(12);
         crunch.setHi(15);
         crunch.setInLevel(0);
         crunch.setLevel(50);
 
         String expected = "    Lo: +6dB\n    Mid: +12dB\n    Hi: 15dB\n    InLvl: 0dB\n    Level: 50dB\n";
         String actual = AlgorithmPrinter.print(crunch);
 
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
 
         String expected = "    Lo: +2dB\n    Mid: +1dB\n    Hi: 3dB\n    Drive: 22\n    Tone: 25\n    Level: 57dB\n";
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
 
         String expected = "    Lo: +4dB\n    Mid: +8dB\n    Hi: 0dB\n    InLvl: -8dB\n    LoCut: 0\n    Feel: 32\n    Drive: 40\n    Tone: 21\n    Level: 44dB\n";
         String actual = AlgorithmPrinter.print(overdrive);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintDistortion() throws PrintException {
         Distortion distortion = new Distortion();
         distortion.setLo(0);
         distortion.setMid(4);
         distortion.setHi(11);
         distortion.setDrive(25);
         distortion.setTone(21);
         distortion.setBass(7);
         distortion.setTreble(6);
         distortion.setLevel(40);
 
         String expected = "    Lo: 0dB\n    Mid: +4dB\n    Hi: 11dB\n    Drive: 25\n    Tone: 21\n    Bass: +7dB\n    Trebl: +6dB\n    Level: 40dB\n";
         String actual = AlgorithmPrinter.print(distortion);
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testPrintPreamp() throws PrintException {
         Preamp preamp = new Preamp();
         preamp.setLo(7);
         preamp.setMid(3);
         preamp.setHi(0);
         preamp.setInLevel(-5);
         preamp.setLoCut(0);
         preamp.setFeel(32);
         preamp.setDrive(23);
         preamp.setTone(22);
         preamp.setBass(0);
         preamp.setTreble(0);
         preamp.setLevel(45);
 
         String expected = "    Lo: +7dB\n    Mid: +3dB\n    Hi: 0dB\n    InLvl: -5dB\n    LoCut: 0\n    Feel: 32\n    Drive: 23\n    Tone: 22\n    Bass: 0dB\n    Trebl: 0dB\n    Level: 45dB\n";
         String actual = AlgorithmPrinter.print(preamp);
 
         assertEquals(expected, actual);
     }
 }
