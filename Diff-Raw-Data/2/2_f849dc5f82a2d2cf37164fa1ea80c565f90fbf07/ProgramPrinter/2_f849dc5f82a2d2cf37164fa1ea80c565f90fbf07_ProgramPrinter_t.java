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
 
 import info.carlwithak.mpxg2.model.Program;
 import info.carlwithak.mpxg2.model.effects.algorithms.PedalWah1;
 import info.carlwithak.mpxg2.model.effects.algorithms.UniVybe;
 import java.text.DecimalFormat;
 
 /**
  * Class to print out a program nicely.
  *
  * @author carl
  */
 public class ProgramPrinter {
 
     private static final String[] EFFECT1_ALGORITHM_NAMES = {
         "no effect", "Detune (M)", "Detune (S)", "Detune (D)", "Shift (M)", "Shift (S)", "Shift (D)",
         "DiatonicHmy", "Panner", "Auto Pan", "Tremolo (M)", "Tremolo (S)", "UniVybe", "Custom Vybe",
         "Phaser", "OrangePhase", "Red Comp", "Blue Comp", "DigiDrive1", "DigiDrive2", "OctaBuzz",
         "SweepFilter", "1-Band (M)", "Wah  1", "Wah  2", "Pedal Wah 1", "Pedal Wah 2",
         "Volume (M)", "Volume (S)", "Volume (D)", "Pedal Vol", "ExtPedalVol", "Test Tone", "Click"
     };
     private static final String[] EFFECT2_ALGORITHM_NAMES = {
         "no effect", "Panner", "Auto Pan", "Tremolo (M)", "Tremolo (S)", "UniVybe", "Custom Vybe",
         "Phaser", "OrangePhase", "Red Comp", "Blue Comp", "DigiDrive1", "DigiDrive2", "OctaBuzz",
         "SweepFilter", "1-Band (M)", "Wah  1", "Wah  2", "Pedal Wah 1", "Pedal Wah 2",
         "Volume (M)", "Volume (S)", "Volume (D)", "Pedal Vol", "ExtPedalVol", "Test Tone", "Click"
     };
     private static final String[] CHORUS_ALGORITHM_NAMES = {
         "no effect", "Chorus", "Detune (M)", "Flanger (M)", "Flanger24(M)", "Flanger (S)", "Rotary Cab",
         "Aerosol", "Orbits", "Centrifuge1", "Centrifuge2", "Comb 1", "Comb 2",
         "Volume (M)", "Volume (S)", "Volume (D)", "Pedal Vol", "ExtPedalVol"
     };
     private static final String[] DELAY_ALGORITHM_NAMES = {
         "no effect", "Delay (M)", "Delay (S)", "Delay (D)", "Echo (M)", "Echo (S)", "Echo (D)",
         "Looper", "JamMan", "Ducker"
     };
     private static final String[] REVERB_ALGORITHM_NAMES = {
         "no effect", "Chamber", "Hall", "Plate", "Ambience", "Gate"
     };
     private static final String[] GAIN_ALGORITHM_NAMES = {
         "no effect", "Tone", "Crunch", "Screamer", "Overdrive", "Distortion", "Preamp", "SplitPreamp"
     };
 
     private static final String[] DELAY_INSERTS = {
        "Effect 1", "Effect 2", "Chorus", "Delay", "Reverb", "EQ", "Gain"
     };
 
     private static final double[] REVERB_DELAY_TIMES = {
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1.41
     };
 
     private static final double[] REVERB_RT_HC = {
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12.8
     };
 
     private static final String[] EFFECT_TYPES = {
         "Effect 1", "Effect 2", "Chorus", "Delay", "Reverb", "Equalizer", "Gain",
         "", "", "", "", "", "", "", "", "", "Send"
     };
     private static final String[][] EFFECT_PARAMETERS = {
         {
             "Mix", "Level", "Rate"
         },
         {},
         {},
         {
             "Mix", "Level", "Time1", "Time2", "Lvl 1", "Lvl 2", "Fbk 1", "Fbk 2", "Damp1", "Damp2", "Clear"
         },
         {
             "Mix", "Level", "Size", "Link", "Diff", "P Dly", "DTime", "D Lvl", "Rt HC"
         },
         {},
         {
             "Lo", "Mid", "Hi", "Drive", "Tone", "Level"
         },
         {},
         {},
         {},
         {},
         {},
         {},
         {},
         {},
         {},
         {
             "Level"
         }
     };
 
     private final static String[] PATCH_SOURCES = {
         "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "Ctls A/B",
         "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
         "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
         "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
         "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
         "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
         "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
         "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
         "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "Midi Toe"
     };
 
     private final static String[] LFO_ON_SOURCES = {
         "none"
     };
 
     private final static String[] TEMPO_SOURCES = {
         "internal"
     };
 
     private final static String[] BEAT_VALUES = {
         "", "", "quarter"
     };
 
     private final static String[] TAP_SOURCES = {
         "none"
     };
 
     private final static String[] SPEAKER_SIMULATOR_CABINETS = {
         "", "ComboNorml"
     };
 
     private final static String[] NOISE_GATE_ENABLES = {
         "", "Guitar Input"
     };
 
     private static final DecimalFormat DECIMAL_2DP = new DecimalFormat("0.00");
 
     static String print(Program program) {
         StringBuilder sb = new StringBuilder();
         sb.append(program.getProgramName()).append("\n");
         StringBuilder styleSB = new StringBuilder();
         if (program.isAcoustic()) {
             styleSB.append("Acoustic, ");
         }
         if (program.isBass()) {
             styleSB.append("Bass, ");
         }
         if (program.isBlues()) {
             styleSB.append("Blues, ");
         }
         if (program.isClean()) {
             styleSB.append("Clean, ");
         }
         if (program.isCountry()) {
             styleSB.append("Country, ");
         }
         if (program.isJazz()) {
             styleSB.append("Jazz, ");
         }
         if (program.isRock()) {
             styleSB.append("Rock, ");
         }
         sb.append("  Guitar Style: ");
         if (styleSB.length() > 0) {
             sb.append(styleSB.substring(0, styleSB.length() - 2));
         }
         sb.append("\n");
         StringBuilder effectTypeSB = new StringBuilder();
         if (program.isChorus()) {
             effectTypeSB.append("Chorus, ");
         }
         if (program.isDelay()) {
             effectTypeSB.append("Delay, ");
         }
         if (program.isDistortion()) {
             effectTypeSB.append("Distortion, ");
         }
         if (program.isEq()) {
             effectTypeSB.append("EQ, ");
         }
         if (program.isFlanger()) {
             effectTypeSB.append("Flanger, ");
         }
         if (program.isGain()) {
             effectTypeSB.append("Gain, ");
         }
         if (program.isMod()) {
             effectTypeSB.append("Mod, ");
         }
         if (program.isOverdrive()) {
             effectTypeSB.append("Overdrive, ");
         }
         if (program.isPhaser()) {
             effectTypeSB.append("Phaser, ");
         }
         if (program.isPitch()) {
             effectTypeSB.append("Pitch, ");
         }
         if (program.isReverb()) {
             effectTypeSB.append("Reverb, ");
         }
         if (program.isSpeakerSim()) {
             effectTypeSB.append("Speaker Sim, ");
         }
         if (program.isWah()) {
             effectTypeSB.append("Wah, ");
         }
         sb.append("  Effect Type: ");
         if (effectTypeSB.length() > 0) {
             sb.append(effectTypeSB.substring(0, effectTypeSB.length() - 2));
         }
         sb.append("\n");
         StringBuilder appTypeSB = new StringBuilder();
         if (program.isPrePost()) {
             appTypeSB.append("Amp Input + FX Loop");
         }
         if (program.isStandAlone()) {
             appTypeSB.append("Stand alone");
         }
         if (program.isInline()) {
             appTypeSB.append("Amp Input Only");
         }
         sb.append("  Application Type: ");
         sb.append(appTypeSB.toString());
         sb.append("\n");
         // TODO indicate inactive effects
         sb.append("  Effect Routing: ").append(program.getRouting()).append("\n");
         if (program.getEffect1Algorithm() > 0) {
             UniVybe univybe = (UniVybe) program.getEffect1();
             sb.append("  Effect 1: ").append(effect1AlgorithmToString(program.getEffect1Algorithm())).append(" (").append(program.isEffect1On() ? "on" : "off").append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getEffect1ToePatch())).append("\n");
             sb.append("    Mix: ").append(univybe.getMix()).append("%\n");
             sb.append("    Level: ").append(univybe.getLevel()).append("dB\n");
             sb.append("    Rate: ").append(univybe.getRate()).append("\n");
         }
         if (program.getEffect2Algorithm() > 0) {
             PedalWah1 effect2 = (PedalWah1) program.getEffect2();
             sb.append("  Effect 2: ").append(effect2AlgorithmToString(program.getEffect2Algorithm())).append(" (").append(program.isEffect2On() ? "on" : "off").append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getEffect2ToePatch())).append("\n");
             sb.append("    Mix: ").append(effect2.getMix()).append("%\n");
             sb.append("    Level: ").append(effect2.getLevel()).append("dB\n");
             sb.append("    Bass: ").append(effect2.getBass()).append("\n");
             sb.append("    Type: Model ").append(effect2.getType() == 0 ? "C" : "V").append("\n");
             sb.append("    Resp: ").append(effect2.getResponse()).append("\n");
             sb.append("    Gain: ").append(effect2.getGain() > 0 ? "+" : "").append(effect2.getGain()).append("\n");
         }
         if (program.getChorusAlgorithm() > 0) {
             sb.append("  Chorus: ").append(chorusAlgorithmToString(program.getChorusAlgorithm())).append(" (").append(program.isChorusOn() ? "on" : "off").append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getChorusToePatch())).append("\n");
             sb.append("    Mix: ").append(program.getChorus().getMix()).append("%\n");
             sb.append("    Level: ").append(program.getChorus().getLevel()).append("dB\n");
         }
         if (program.getDelayAlgorithm() > 0) {
             sb.append("  Delay: ").append(delayAlgorithmToString(program.getDelayAlgorithm())).append(" (").append(program.isDelayOn() ? "on" : "off").append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getDelayToePatch())).append("\n");
             sb.append("    Mix: ").append(program.getDelay().getMix()).append("%\n");
             sb.append("    Level: ").append(program.getDelay().getLevel() > 0 ? "+" : "").append(program.getDelay().getLevel()).append("dB\n");
             sb.append("    Time1: ").append(program.getDelay().getTime1Echoes()).append(":").append(program.getDelay().getTime1Beat()).append("\n");
             sb.append("    Time2: ").append(program.getDelay().getTime2Echoes()).append(":").append(program.getDelay().getTime2Beat()).append("\n");
             sb.append("    Level1: ").append(program.getDelay().getLevel1() > 0 ? "+" : "").append(program.getDelay().getLevel1()).append("dB\n");
             sb.append("    Level2: ").append(program.getDelay().getLevel2() > 0 ? "+" : "").append(program.getDelay().getLevel2()).append("dB\n");
             sb.append("    Feedback1: ").append(program.getDelay().getFeedback1() > 0 ? "+" : "").append(program.getDelay().getFeedback1()).append("%\n");
             sb.append("    Insert: ").append(delayInsertToString(program.getDelay().getInsert())).append("\n");
             sb.append("    Feedback2: ").append(program.getDelay().getFeedback2() > 0 ? "+" : "").append(program.getDelay().getFeedback2()).append("%\n");
             sb.append("    Damp1: ").append(program.getDelay().getDamp1()).append("%\n");
             sb.append("    Damp2: ").append(program.getDelay().getDamp2()).append("%\n");
             sb.append("    Clear: ").append(program.getDelay().getClear() == 0 ? "off" : "on").append("\n");
         }
         if (program.getReverbAlgorithm() > 0) {
             sb.append("  Reverb: ").append(reverbAlgorithmToString(program.getReverbAlgorithm())).append(" (").append(program.isReverbOn() ? "on" : "off").append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getReverbToePatch())).append("\n");
             sb.append("    Mix: ").append(program.getReverb().getMix()).append("%\n");
             sb.append("    Level: ").append(program.getReverb().getLevel()).append("dB\n");
             sb.append("    Size: ").append(program.getReverb().getSize()).append("m\n");
             sb.append("    Link: ").append(program.getReverb().getLink() == 0 ? "off" : "on").append("\n");
             sb.append("    Diff: ").append(program.getReverb().getDiff()).append("%\n");
             sb.append("    Pre Delay: ").append(program.getReverb().getPreDelay()).append("ms\n");
             sb.append("    Delay Time: ").append(reverbDelayTimeToString(program.getReverb().getDelayTime())).append("s\n");
             sb.append("    Delay Level: ").append(program.getReverb().getDelayLevel() == 0 ? "off" : "on").append("\n");
             sb.append("    Rt HC: ").append(reverbRtHCToString(program.getReverb().getRtHC())).append("k\n");
         }
         if (program.getGainAlgorithm() > 0) {
             sb.append("  Gain: ").append(gainAlgorithmToString(program.getGainAlgorithm())).append(" (").append(program.isGainOn() ? "on" : "off").append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getGainToePatch())).append("\n");
             sb.append("    Lo: ").append(program.getGain().getLo() > 0 ? "+" : "").append(program.getGain().getLo()).append("\n");
             sb.append("    Mid: ").append(program.getGain().getMid() > 0 ? "+" : "").append(program.getGain().getMid()).append("\n");
             sb.append("    Hi: ").append(program.getGain().getHi() > 0 ? "+" : "").append(program.getGain().getHi()).append("\n");
             sb.append("    Drive: ").append(program.getGain().getDrive()).append("\n");
             sb.append("    Tone: ").append(program.getGain().getTone()).append("\n");
             sb.append("    Level: ").append(program.getGain().getLevel()).append("\n");
         }
         sb.append("  Softrow:\n");
         sb.append("    1: ").append(effectTypeToString(program.getSoftRowEffectType(0))).append(" ").append(effectParameterToString(program.getSoftRowEffectType(0), program.getSoftRowParameter(0))).append("\n");
         sb.append("    2: ").append(effectTypeToString(program.getSoftRowEffectType(1))).append(" ").append(effectParameterToString(program.getSoftRowEffectType(1), program.getSoftRowParameter(1))).append("\n");
         sb.append("    3: ").append(effectTypeToString(program.getSoftRowEffectType(2))).append(" ").append(effectParameterToString(program.getSoftRowEffectType(2), program.getSoftRowParameter(2))).append("\n");
         sb.append("    4: ").append(effectTypeToString(program.getSoftRowEffectType(3))).append(" ").append(effectParameterToString(program.getSoftRowEffectType(3), program.getSoftRowParameter(3))).append("\n");
         sb.append("    5: ").append(effectTypeToString(program.getSoftRowEffectType(4))).append(" ").append(effectParameterToString(program.getSoftRowEffectType(4), program.getSoftRowParameter(4))).append("\n");
         sb.append("    6: ").append(effectTypeToString(program.getSoftRowEffectType(5))).append(" ").append(effectParameterToString(program.getSoftRowEffectType(5), program.getSoftRowParameter(5))).append("\n");
         sb.append("    7: ").append(effectTypeToString(program.getSoftRowEffectType(6))).append(" ").append(effectParameterToString(program.getSoftRowEffectType(6), program.getSoftRowParameter(6))).append("\n");
         sb.append("    8: ").append(effectTypeToString(program.getSoftRowEffectType(7))).append(" ").append(effectParameterToString(program.getSoftRowEffectType(7), program.getSoftRowParameter(7))).append("\n");
         sb.append("    9: ").append(effectTypeToString(program.getSoftRowEffectType(8))).append(" ").append(effectParameterToString(program.getSoftRowEffectType(8), program.getSoftRowParameter(8))).append("\n");
         sb.append("    10: ").append(effectTypeToString(program.getSoftRowEffectType(9))).append(" ").append(effectParameterToString(program.getSoftRowEffectType(9), program.getSoftRowParameter(9))).append("\n");
         sb.append("  Patching:\n");
         sb.append("    Patch 1:\n");
         sb.append("      Source: ").append(patchSourceToString(program.getPatch1().getSource())).append("\n");
         sb.append("        Min: ").append(program.getPatch1().getSourceMin()).append("\n");
         sb.append("        Mid: ").append(program.getPatch1().getSourceMid() == 0xff ? "--" : program.getPatch1().getSourceMid()).append("\n");
         sb.append("        Max: ").append(program.getPatch1().getSourceMax()).append("\n");
         sb.append("      Destination: ").append(patchDestinationToString(program.getPatch1().getDestinationEffect(), program.getPatch1().getDestinationParameter())).append("\n");
         sb.append("        Min: ").append(program.getPatch1().getDestinationMin()).append("%\n");
         sb.append("        Mid: ").append(program.getPatch1().getDestinationMid() == 0x80 ? "--" : program.getPatch1().getDestinationMid()).append("\n");
         sb.append("        Max: ").append(program.getPatch1().getDestinationMax()).append("%\n");
         sb.append("    Patch 2:\n");
         sb.append("      Source: ").append(patchSourceToString(program.getPatch2().getSource())).append("\n");
         sb.append("        Min: ").append(program.getPatch2().getSourceMin()).append("\n");
         sb.append("        Mid: ").append(program.getPatch2().getSourceMid() == 0xff ? "--" : program.getPatch2().getSourceMid()).append("\n");
         sb.append("        Max: ").append(program.getPatch2().getSourceMax()).append("\n");
         sb.append("      Destination: ").append(patchDestinationToString(program.getPatch2().getDestinationEffect(), program.getPatch2().getDestinationParameter())).append("\n");
         sb.append("        Min: ").append(program.getPatch2().getDestinationMin() % 0x100).append(":").append(program.getPatch2().getDestinationMin() / 0x100).append("\n");
         sb.append("        Mid: ").append(program.getPatch2().getDestinationMid() == 0x80 ? "--" : program.getPatch2().getDestinationMid()).append("\n");
         sb.append("        Max: ").append(program.getPatch2().getDestinationMax() % 0x100).append(":").append(program.getPatch2().getDestinationMax() / 0x100).append("\n");
         sb.append("    Patch 3:\n");
         sb.append("      Source: ").append(patchSourceToString(program.getPatch3().getSource())).append("\n");
         sb.append("        Min: ").append(program.getPatch3().getSourceMin()).append("\n");
         sb.append("        Mid: ").append(program.getPatch3().getSourceMid() == 0xff ? "--" : program.getPatch3().getSourceMid()).append("\n");
         sb.append("        Max: ").append(program.getPatch3().getSourceMax()).append("\n");
         sb.append("      Destination: ").append(patchDestinationToString(program.getPatch3().getDestinationEffect(), program.getPatch3().getDestinationParameter())).append("\n");
         sb.append("        Min: ").append(program.getPatch3().getDestinationMin()).append("%\n");
         sb.append("        Mid: ").append(program.getPatch3().getDestinationMid() == 0x80 ? "--" : program.getPatch3().getDestinationMid()).append("\n");
         sb.append("        Max: ").append(program.getPatch3().getDestinationMax()).append("%\n");
         sb.append("    Patch 4:\n");
         sb.append("      Source: ").append(patchSourceToString(program.getPatch4().getSource())).append("\n");
         sb.append("        Min: ").append(program.getPatch4().getSourceMin()).append("\n");
         sb.append("        Mid: ").append(program.getPatch4().getSourceMid() == 0xff ? "--" : program.getPatch4().getSourceMid()).append("\n");
         sb.append("        Max: ").append(program.getPatch4().getSourceMax()).append("\n");
         sb.append("      Destination: ").append(patchDestinationToString(program.getPatch4().getDestinationEffect(), program.getPatch4().getDestinationParameter())).append("\n");
         sb.append("        Min: ").append(program.getPatch4().getDestinationMin()).append("%\n");
         sb.append("        Mid: ").append(program.getPatch4().getDestinationMid() == 0x80 ? "--" : program.getPatch4().getDestinationMid()).append("\n");
         sb.append("        Max: ").append(program.getPatch4().getDestinationMax()).append("%\n");
         sb.append("    Patch 5:\n");
         sb.append("      Source: ").append(patchSourceToString(program.getPatch5().getSource())).append("\n");
         sb.append("        Min: ").append(program.getPatch5().getSourceMin()).append("\n");
         sb.append("        Mid: ").append(program.getPatch5().getSourceMid() == 0xff ? "--" : program.getPatch5().getSourceMid()).append("\n");
         sb.append("        Max: ").append(program.getPatch5().getSourceMax()).append("\n");
         sb.append("      Destination: ").append(patchDestinationToString(program.getPatch5().getDestinationEffect(), program.getPatch5().getDestinationParameter())).append("\n");
         sb.append("        Min: ").append(program.getPatch5().getDestinationMin()).append("\n");
         sb.append("        Mid: ").append(program.getPatch5().getDestinationMid() == 0x80 ? "--" : program.getPatch5().getDestinationMid()).append("\n");
         sb.append("        Max: ").append(program.getPatch5().getDestinationMax() > 0 ? "+" : "").append(program.getPatch5().getDestinationMax()).append("\n");
         sb.append("  Controllers:\n");
         sb.append("    Knob:\n");
         sb.append("      Value: ").append(program.getKnobValue()).append("\n");
         sb.append("      Low: ").append(program.getKnobLow()).append("\n");
         sb.append("      High: ").append(program.getKnobHigh()).append("\n");
         sb.append("      Name: ").append(program.getKnobName()).append("\n");
         sb.append("    LFO 1:\n");
         sb.append("      Mode: ").append(program.getLfo1Mode() == 0 ? "Off" : "On").append("\n");
         sb.append("      Rate: ").append(DECIMAL_2DP.format(program.getLfo1Rate())).append("Hz\n");
         sb.append("      PW: ").append(program.getLfo1PulseWidth()).append("%\n");
         sb.append("      Phase: ").append(program.getLfo1Phase()).append("\n");
         sb.append("      Depth: ").append(program.getLfo1Depth()).append("%\n");
         sb.append("      On Level: ").append(program.getLfo1OnLevel()).append("\n");
         sb.append("      On Source: ").append(lfoOnSourceToString(program.getLfo1OnSource())).append("\n");
         sb.append("    LFO 2:\n");
         sb.append("      Mode: ").append(program.getLfo2Mode() == 0 ? "Off" : "On").append("\n");
         sb.append("      Rate: ").append(DECIMAL_2DP.format(program.getLfo2Rate())).append("Hz\n");
         sb.append("      PW: ").append(program.getLfo2PulseWidth()).append("%\n");
         sb.append("      Phase: ").append(program.getLfo2Phase()).append("\n");
         sb.append("      Depth: ").append(program.getLfo2Depth()).append("%\n");
         sb.append("      On Level: ").append(program.getLfo2OnLevel()).append("\n");
         sb.append("      On Source: ").append(lfoOnSourceToString(program.getLfo2OnSource())).append("\n");
         sb.append("    Random:\n");
         sb.append("      Low: ").append(program.getRandomLow()).append("\n");
         sb.append("      High: ").append(program.getRandomHigh()).append("\n");
         sb.append("      Rate: ").append(DECIMAL_2DP.format(program.getRandomRate())).append("Hz\n");
         sb.append("    A/B:\n");
         sb.append("      Mode: ").append(program.getABMode() == 0 ? "Trigger" : "?").append("\n");
         sb.append("      A Rate: ").append(program.getARate()).append("\n");
         sb.append("      B Rate: ").append(program.getBRate()).append("\n");
         sb.append("      On Level: ").append(program.getLfo2OnLevel()).append("\n");
         sb.append("      On Source: ").append(lfoOnSourceToString(program.getLfo2OnSource())).append("\n");
         sb.append("    Envelope:\n");
         sb.append("      Src1: ").append(program.getEnvelopeGeneratorSrc1() == 0 ? "off" : program.getEnvelopeGeneratorSrc1()).append("\n");
         sb.append("      Src2: ").append(program.getEnvelopeGeneratorSrc2() == 0 ? "off" : program.getEnvelopeGeneratorSrc2()).append("\n");
         sb.append("      A Trim: ").append(program.getEnvelopeGeneratorATrim()).append("\n");
         sb.append("      Resp: ").append(program.getEnvelopeGeneratorResponse()).append("\n");
         sb.append("  Mix:\n");
         sb.append("    Send:\n");
         sb.append("      Level: ").append(program.getSendLevel() > 0 ? "+" : "").append(program.getSendLevel()).append("\n");
         sb.append("      Bypass Level: ").append(program.getSendBypassLevel()).append("\n");
         sb.append("    Post:\n");
         sb.append("      Mix: ").append(program.getPostMix()).append("%\n");
         sb.append("      Level: ").append(program.getPostLevel() > 0 ? "+" : "").append(program.getPostLevel()).append("dB\n");
         sb.append("      Bypass Level: ").append(program.getPostBypassLevel()).append("dB\n");
         sb.append("    FX1:\n");
         sb.append("      Mix: ").append(program.getEffect1().getMix()).append("%\n");
         sb.append("      Level: ").append(program.getEffect1().getLevel() > 0 ? "+" : "").append(program.getEffect1().getLevel()).append("dB\n");
         sb.append("    FX2:\n");
         sb.append("      Mix: ").append(program.getEffect2().getMix()).append("%\n");
         sb.append("      Level: ").append(program.getEffect2().getLevel() > 0 ? "+" : "").append(program.getEffect2().getLevel()).append("dB\n");
         sb.append("    Chorus:\n");
         sb.append("      Mix: ").append(program.getChorus().getMix()).append("%\n");
         sb.append("      Level: ").append(program.getChorus().getLevel() > 0 ? "+" : "").append(program.getChorus().getLevel()).append("dB\n");
         sb.append("    Delay:\n");
         sb.append("      Mix: ").append(program.getDelay().getMix()).append("%\n");
         sb.append("      Level: ").append(program.getDelay().getLevel() > 0 ? "+" : "").append(program.getDelay().getLevel()).append("dB\n");
         sb.append("    Reverb:\n");
         sb.append("      Mix: ").append(program.getReverb().getMix()).append("%\n");
         sb.append("      Level: ").append(program.getReverb().getLevel() > 0 ? "+" : "").append(program.getReverb().getLevel()).append("dB\n");
         sb.append("  Tempo:\n");
         sb.append("    Rate: ").append(program.getTempo()).append(" BPM\n");
         sb.append("    Source: ").append(tempoSourceToString(program.getTempoSource())).append("\n");
         sb.append("    Beat Value: ").append(beatValueToString(program.getBeatValue())).append("\n");
         sb.append("    Tap Average: ").append(program.getTapAverage()).append(" beats\n");
         sb.append("    Tap Source: ").append(tapSourceToString(program.getTapSource())).append("\n");
         sb.append("  Speaker Sim: ").append(program.getSpeakerSimulatorEnable() == 0 ? "off" : "on").append("\n");
         sb.append("    Cabinet: ").append(speakerSimulatorCabinetToString(program.getSpeakerSimulatorCabinet())).append("\n");
         sb.append("  Noise Gate:\n");
         sb.append("    Enable: ").append(noiseGateEnableToString(program.getNoiseGate().getEnable())).append("\n");
         sb.append("    Send: ").append(program.getNoiseGate().getSend() == 0 ? "off" : "on").append("\n");
         sb.append("    Thrsh: ").append(program.getNoiseGate().getThreshold()).append("dB\n");
         sb.append("    Atten: ").append(program.getNoiseGate().getAttenuation()).append("dB\n");
         sb.append("    Offset: ").append(program.getNoiseGate().getOffset()).append("dB\n");
         sb.append("    ATime: ").append(program.getNoiseGate().getATime()).append("\n");
         sb.append("    HTime: ").append(program.getNoiseGate().getHTime()).append("\n");
         sb.append("    RTime: ").append(program.getNoiseGate().getRTime()).append("\n");
         sb.append("    Delay: ").append(program.getNoiseGate().getDelay()).append("\n");
         return sb.toString().trim();
     }
 
     private static String effect1AlgorithmToString(final int effect1Algorithm) {
         return EFFECT1_ALGORITHM_NAMES[effect1Algorithm];
     }
 
     private static String effect2AlgorithmToString(final int effect2Algorithm) {
         return EFFECT2_ALGORITHM_NAMES[effect2Algorithm];
     }
 
     private static String chorusAlgorithmToString(final int chorusAlgorithm) {
         return CHORUS_ALGORITHM_NAMES[chorusAlgorithm];
     }
 
     private static String delayAlgorithmToString(final int delayAlgorithm) {
         return DELAY_ALGORITHM_NAMES[delayAlgorithm];
     }
 
     private static String reverbAlgorithmToString(final int reverbAlgorithm) {
         return REVERB_ALGORITHM_NAMES[reverbAlgorithm];
     }
 
     private static String gainAlgorithmToString(final int gainAlgorithm) {
         return GAIN_ALGORITHM_NAMES[gainAlgorithm];
     }
 
     private static String toePatchToString(final int toePatch) {
         String s = null;
         switch(toePatch) {
             case 0:
                 s = "disabled";
                 break;
             case 1:
                 s = "off=bypass";
                 break;
             case 2:
                 s = "on=bypass";
                 break;
         }
         return s;
     }
 
     private static String delayInsertToString(final int delayInsert) {
         return DELAY_INSERTS[delayInsert];
     }
 
     private static double reverbDelayTimeToString(final int reverbDelayTime) {
         return REVERB_DELAY_TIMES[reverbDelayTime];
     }
 
     private static double reverbRtHCToString(final int reverbRtHC) {
         return REVERB_RT_HC[reverbRtHC];
     }
 
     private static String effectTypeToString(final int effectType) {
         return EFFECT_TYPES[effectType];
     }
 
     private static String effectParameterToString(final int effectType, final int effectParameter) {
         return EFFECT_PARAMETERS[effectType][effectParameter];
     }
 
     private static String patchSourceToString(final int patchSource) {
         return PATCH_SOURCES[patchSource];
     }
 
     private static String patchDestinationToString(final int patchDestinationEffect, final int patchDestinationParameter) {
         return EFFECT_TYPES[patchDestinationEffect] + " " + EFFECT_PARAMETERS[patchDestinationEffect][patchDestinationParameter];
     }
 
     private static String lfoOnSourceToString(final int lfoOnSource) {
         return LFO_ON_SOURCES[lfoOnSource];
     }
 
     private static String tempoSourceToString(final int tempoSource) {
         return TEMPO_SOURCES[tempoSource];
     }
 
     private static String beatValueToString(final int beatValue) {
         return BEAT_VALUES[beatValue];
     }
 
     private static String tapSourceToString(final int tapSource) {
         return TAP_SOURCES[tapSource];
     }
 
     private static String speakerSimulatorCabinetToString(final int speakerSimulatorCabinet) {
         return SPEAKER_SIMULATOR_CABINETS[speakerSimulatorCabinet];
     }
 
     private static String noiseGateEnableToString(final int noiseGateEnable) {
         return NOISE_GATE_ENABLES[noiseGateEnable];
     }
 }
