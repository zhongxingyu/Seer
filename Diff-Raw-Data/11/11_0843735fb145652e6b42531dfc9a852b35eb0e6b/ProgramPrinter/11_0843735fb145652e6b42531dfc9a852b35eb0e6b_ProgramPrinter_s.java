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
 import info.carlwithak.mpxg2.model.DataObject;
 import info.carlwithak.mpxg2.model.EnvelopeGenerator;
 import info.carlwithak.mpxg2.model.FrequencyRate;
 import info.carlwithak.mpxg2.model.GenericValue;
 import info.carlwithak.mpxg2.model.Knob;
 import info.carlwithak.mpxg2.model.Lfo;
 import info.carlwithak.mpxg2.model.Parameter;
 import info.carlwithak.mpxg2.model.Patch;
 import info.carlwithak.mpxg2.model.Program;
 import info.carlwithak.mpxg2.model.Random;
 import info.carlwithak.mpxg2.model.effects.EffectObject;
 import info.carlwithak.mpxg2.model.effects.Reverb;
 import info.carlwithak.mpxg2.model.effects.algorithms.Chamber;
 import info.carlwithak.mpxg2.model.effects.algorithms.Plate;
 import java.text.DecimalFormat;
 
 import static info.carlwithak.mpxg2.printing.Util.onOffToString;
 import static info.carlwithak.mpxg2.printing.Util.printParameter;
 import static info.carlwithak.mpxg2.printing.Util.signInt;
 
 /**
  * Class to print out a program nicely.
  *
  * @author carl
  */
 public class ProgramPrinter {
 
     private static final String[] TOE_PATCH_STATES = {
         "disabled", "off=bypass", "on=bypass"
     };
 
     private static final String[] EFFECT_TYPES = {
         "Effect 1", "Effect 2", "Chorus", "Delay", "Reverb", "Equalizer", "Gain",
         "Knob", "LFO1", "LFO2", "Rand", "A/B", "Env", "", "", "Post", "Send",
         "SpkrSim", "NGate", "Tempo"
     };
     private static final String[][] EFFECT_PARAMETERS = {
         {},
         {},
         {},
         {
             "Level"
         },
         {},
         {},
         {
             null, "Send", "Thrsh", "Atten", "Offse", "ATime", "HTime", "RTime", "Delay"
         },
         {},
         {},
         {},
         {},
         {
             "FX1", "FX2", "Chrs", "Dly", "Rvb", "EQ", "Gain", "Ins"
         }
     };
     private static final String[][] EFFECT_PARAMETER_UNITS = {
         {},
         {},
         {},
         {
             ""
         },
         {},
         {},
         {
             null, "OnOff", "dB", "dB", "dB", "", "", "", ""
         },
         {},
         {},
         {},
         {},
         {
             "OnOff", "OnOff", "OnOff", "OnOff", "OnOff", "OnOff", "OnOff", "OnOff"
         }
     };
 
     private final static String[] LFO_ON_SOURCES = {
         "None"
     };
 
     private final static String[] TEMPO_SOURCES = {
         "internal"
     };
 
     private final static String[] BEAT_VALUES = {
         "eighth", "dotted eighth", "quarter", "dotted quarter"
     };
 
     private final static String[] TAP_SOURCES = {
         "none"
     };
 
     private final static String[] AB_MODES = {
         "Trigger", "Gate"
     };
 
     private final static String[] ENVELOPE_GENERATOR_SOURCES = {
         "Off", "In", "Ret L", "Ret R", "Raw L", "Raw R", "FX 1 L", "FX 1 R",
         "FX 2 L", "FX 2 R", "Chrs L", "Chrs R", "EQ L", "EQ R", "Rvb L", "Rvb R",
         "Dly L", "Dly R", "PreOut", "MainL", "MainR"
     };
 
     private final static String[] SPEAKER_SIMULATOR_CABINET_DESIGNS = {
         "Combo1", "Combo2", "Stack1", "Stack2"
     };
 
     private final static String[] SPEAKER_SIMULATOR_SPEAKER_TYPES = {
         "Brite", "Norml", "Warm", "Dark"
     };
 
     private final static String[] NOISE_GATE_ENABLES = {
         "Off", "Guitar Input", "Returns Only"
     };
 
     private static final DecimalFormat DECIMAL_2DP = new DecimalFormat("0.00");
 
     public static String print(Program program) throws PrintException {
         StringBuilder sb = new StringBuilder();
         sb.append(program.getProgramName()).append("\n");
         sb.append("  Guitar Style: ").append(printGuitarStyles(program)).append("\n");
         sb.append("  Effect Type: ").append(printEffectTypes(program)).append("\n");
         sb.append("  Application Type: ").append(printApplicationTypes(program)).append("\n");
         // TODO indicate inactive effects
         sb.append("  Effect Routing:\n");
         for (String line : RoutingPrinter.print(program).split("\n")) {
             sb.append("    ").append(line).append("\n");
         }
         printProgram(sb, "Effect 1", program.getEffect1(), program.isEffect1On(), program.getEffect1ToePatch());
         printProgram(sb, "Effect 2", program.getEffect2(), program.isEffect2On(), program.getEffect2ToePatch());
         printProgram(sb, "Chorus", program.getChorus(), program.isChorusOn(), program.getChorusToePatch());
         printProgram(sb, "Delay", program.getDelay(), program.isDelayOn(), program.getDelayToePatch());
         printProgram(sb, "Reverb", program.getReverb(), program.isReverbOn(), program.getReverbToePatch());
         printProgram(sb, "EQ", program.getEq(), program.isEqOn(), program.getEqToePatch());
         printProgram(sb, "Gain", program.getGain(), program.isGainOn(), program.getGainToePatch());
         sb.append("  Softrow:\n");
         for (int i = 0; i < 10; i++) {
             sb.append(printSoftRow(program, i));
         }
         sb.append("  Patching:\n");
         sb.append(printPatch(program, program.getPatch1(), 1));
         sb.append(printPatch(program, program.getPatch2(), 2));
         sb.append(printPatch(program, program.getPatch3(), 3));
         sb.append(printPatch(program, program.getPatch4(), 4));
         sb.append(printPatch(program, program.getPatch5(), 5));
         sb.append("  Controllers:\n");
         sb.append("    Knob:\n").append(printKnob(program.getKnob()));
         sb.append("    LFO 1:\n").append(printLfo(program.getLfo1()));
         sb.append("    LFO 2:\n").append(printLfo(program.getLfo2()));
         sb.append("    Random:\n").append(printRandom(program.getRandom()));
         sb.append("    A/B:\n").append(printAb(program.getAb()));
         sb.append("    Envelope:\n").append(printEnvelopeGenerator(program.getEnvelopeGenerator()));
         sb.append("  Mix:\n");
         sb.append("    Send:\n");
         sb.append("      Level: ").append(signInt(program.getSendLevel())).append("\n");
         sb.append("      Bypass Level: ").append(program.getSendBypassLevel()).append("\n");
         sb.append("    Post:\n");
         sb.append("      Mix: ").append(program.getPostMix()).append("%\n");
         sb.append("      Level: ").append(signInt(program.getPostLevel())).append("dB\n");
         sb.append("      Bypass Level: ").append(program.getPostBypassLevel()).append("dB\n");
         if (program.getEffect1() != null) {
             sb.append("    FX1:\n");
             sb.append("  ").append(printParameter(program.getEffect1().getMix()));
             sb.append("  ").append(printParameter(program.getEffect1().getLevel()));
         }
         if (program.getEffect2() != null) {
             sb.append("    FX2:\n");
             sb.append("  ").append(printParameter(program.getEffect2().getMix()));
             sb.append("  ").append(printParameter(program.getEffect2().getLevel()));
         }
         if (program.getChorus() != null) {
             sb.append("    Chorus:\n");
             sb.append("  ").append(printParameter(program.getChorus().getMix()));
             sb.append("  ").append(printParameter(program.getChorus().getLevel()));
         }
         if (program.getDelay() != null) {
             sb.append("    Delay:\n");
             sb.append("  ").append(printParameter(program.getDelay().getMix()));
             sb.append("  ").append(printParameter(program.getDelay().getLevel()));
         }
         if (program.getReverb() != null) {
             sb.append("    Reverb:\n");
             sb.append("  ").append(printParameter(program.getReverb().getMix()));
             sb.append("  ").append(printParameter(program.getReverb().getLevel()));
         }
         if (program.getEq() != null) {
             sb.append("    Eq:\n");
             sb.append("  ").append(printParameter(program.getEq().getMix()));
             sb.append("  ").append(printParameter(program.getEq().getLevel()));
         }
         sb.append("  Tempo:\n");
         sb.append("    Rate: ").append(program.getTempo()).append(" BPM\n");
         sb.append("    Source: ").append(tempoSourceToString(program.getTempoSource())).append("\n");
         sb.append("    Beat Value: ").append(beatValueToString(program.getBeatValue())).append("\n");
         sb.append("    Tap Average: ").append(program.getTapAverage()).append(" beats\n");
         sb.append("    Tap Source: ").append(tapSourceToString(program.getTapSource())).append("\n");
         sb.append("  Speaker Sim: ").append(onOffToString(program.isSpeakerSimulatorEnable())).append("\n");
         sb.append("    Cabinet: ").append(speakerSimulatorCabinetToString(program.getSpeakerSimulatorCabinet())).append("\n");
         sb.append("  Noise Gate:\n");
         sb.append("    Enable: ").append(noiseGateEnableToString(program.getNoiseGate().getEnable())).append("\n");
         sb.append("    Send: ").append(onOffToString(program.getNoiseGate().isSend())).append("\n");
         sb.append("    Thrsh: ").append(program.getNoiseGate().getThreshold()).append("dB\n");
         sb.append("    Atten: ").append(program.getNoiseGate().getAttenuation()).append("dB\n");
         sb.append("    Offset: ").append(program.getNoiseGate().getOffset()).append("dB\n");
         sb.append("    ATime: ").append(program.getNoiseGate().getATime()).append("\n");
         sb.append("    HTime: ").append(program.getNoiseGate().getHTime()).append("\n");
         sb.append("    RTime: ").append(program.getNoiseGate().getRTime()).append("\n");
         sb.append("    Delay: ").append(program.getNoiseGate().getDelay()).append("\n");
         return sb.toString().trim();
     }
 
     private static String trimDelimitedList(final StringBuilder sb) {
         String result;
         if (sb.length() > 0) {
             result = sb.substring(0, sb.length() - 2);
         } else {
             result = sb.toString();
         }
         return result;
     }
 
     static String printGuitarStyles(final Program program) {
         StringBuilder sb = new StringBuilder();
         if (program.isAcoustic()) {
             sb.append("Acoustic, ");
         }
         if (program.isBass()) {
             sb.append("Bass, ");
         }
         if (program.isBlues()) {
             sb.append("Blues, ");
         }
         if (program.isClean()) {
             sb.append("Clean, ");
         }
         if (program.isCountry()) {
             sb.append("Country, ");
         }
         if (program.isJazz()) {
             sb.append("Jazz, ");
         }
         if (program.isRock()) {
             sb.append("Rock, ");
         }
 
         return trimDelimitedList(sb);
     }
 
     static String printEffectTypes(final Program program) {
         StringBuilder sb = new StringBuilder();
         if (program.isChorus()) {
             sb.append("Chorus, ");
         }
         if (program.isDelay()) {
             sb.append("Delay, ");
         }
         if (program.isDistortion()) {
             sb.append("Distortion, ");
         }
         if (program.isEq()) {
             sb.append("EQ, ");
         }
         if (program.isFlanger()) {
             sb.append("Flanger, ");
         }
         if (program.isGain()) {
             sb.append("Gain, ");
         }
         if (program.isMod()) {
             sb.append("Mod, ");
         }
         if (program.isOverdrive()) {
             sb.append("Overdrive, ");
         }
         if (program.isPhaser()) {
             sb.append("Phaser, ");
         }
         if (program.isPitch()) {
             sb.append("Pitch, ");
         }
         if (program.isReverb()) {
             sb.append("Reverb, ");
         }
         if (program.isSpeakerSim()) {
             sb.append("Speaker Sim, ");
         }
         if (program.isWah()) {
             sb.append("Wah, ");
         }
 
         return trimDelimitedList(sb);
     }
 
     static String printApplicationTypes(final Program program) {
         StringBuilder sb = new StringBuilder();
         if (program.isPrePost()) {
             sb.append("Amp Input + FX Loop, ");
         }
         if (program.isStandAlone()) {
             sb.append("Stand alone, ");
         }
         if (program.isInline()) {
             sb.append("Amp Input Only, ");
         }
 
         return trimDelimitedList(sb);
     }
 
     static void printProgram(final StringBuilder sb, final String label, final EffectObject effect, final boolean effectOn, final int effectToePatch) throws PrintException {
         if (effect != null) {
             sb.append("  ").append(label).append(": ");
             sb.append(effect.getName()).append(" (").append(onOffToString(effectOn)).append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(effectToePatch)).append("\n");
             sb.append(AlgorithmPrinter.print(effect));
         }
     }
 
     static String printSoftRow(final Program program, final int i) throws PrintException {
         // TODO should be null if it's not used
         if (program.getSoftRowEffectType(i) == 255 || program.getSoftRowParameter(i) == 255) {
             return "";
         }
         StringBuilder sb = new StringBuilder();
         sb.append("    ").append(i + 1).append(": ");
         sb.append(effectTypeToString(program.getSoftRowEffectType(i))).append(" ");
         String effectParameterName;
         if (program.getSoftRowEffectType(i) > 12) {
             effectParameterName = effectParameterToString(program.getSoftRowEffectType(i), program.getSoftRowParameter(i));
         } else {
             Parameter effectParameter = getEffectParameter(program, program.getSoftRowEffectType(i), program.getSoftRowParameter(i));
             if (effectParameter == null) {
                 return "";
             }
             effectParameterName = effectParameter.getName();
         }
         sb.append(effectParameterName).append("\n");
         return sb.toString();
     }
 
     static String printPatch(final Program program, final Patch patch, final int patchNumber) throws PrintException {
         if (patch.getSourceIndex() == 0) {
             return "";
         }
         String patchParameter;
         String patchDestinationUnit;
         Parameter parameter = getEffectParameter(program, patch.getDestinationEffectIndex(), patch.getDestinationParameter());
         if (parameter == null) {
             patchParameter = effectParameterToString(patch.getDestinationEffectIndex(), patch.getDestinationParameter());
             patchDestinationUnit = getEffectParameterUnits(patch.getDestinationEffectIndex(), patch.getDestinationParameter());
         } else {
             patchParameter = parameter.getName();
             patchDestinationUnit = parameter.getUnit();
             if (parameter instanceof GenericValue && ((GenericValue) parameter).getMinValue() instanceof Integer && ((Integer) ((GenericValue) parameter).getMinValue()) < 0) {
                 patchDestinationUnit = '-' + patchDestinationUnit;
             } else if (parameter instanceof FrequencyRate) {
                 // TODO find a better way
                 patchDestinationUnit = "100" + patchDestinationUnit;
             }
         }
         StringBuilder sb = new StringBuilder();
         sb.append("    Patch ").append(patchNumber).append(":\n");
         sb.append("      Source: ").append(patch.getSourceName()).append("\n");
         sb.append("        Min: ").append(patch.getSourceMin()).append("\n");
         sb.append("        Mid: ").append(patch.getSourceMid() == null ? "--" : patch.getSourceMid()).append("\n");
         sb.append("        Max: ").append(patch.getSourceMax()).append("\n");
         String patchEffect = patch.getDestinationEffectName();
         sb.append("      Destination: ").append(patchEffect).append(" ").append(patchParameter).append("\n");
         sb.append("        Min: ");
         sb.append(formatPatchParameter(patchDestinationUnit, patch.getDestinationMin(), patchEffect, patchParameter, program.getReverb())).append("\n");
         sb.append("        Mid: ");
         if (patch.getDestinationMid() == 0x8000) {
             sb.append("--\n");
         } else {
             sb.append(formatPatchParameter(patchDestinationUnit, patch.getDestinationMid(), patchEffect, patchParameter, program.getReverb())).append("\n");
         }
         sb.append("        Max: ");
         sb.append(formatPatchParameter(patchDestinationUnit, patch.getDestinationMax(), patchEffect, patchParameter, program.getReverb())).append("\n");
         return sb.toString();
     }
 
     static String formatPatchParameter(final String patchDestinationUnit, final int parameterValue, final String patchEffect, final String patchParameter, final Reverb reverb) throws PrintException {
         StringBuilder sb = new StringBuilder();
         if ("Decay".equals(patchParameter)) {
             boolean link;
             double size;
             if (reverb instanceof Chamber) {
                 Chamber chamber = (Chamber) reverb;
                 link = chamber.isLink().getValue();
                 size = chamber.getSize().getValue();
             } else if (reverb instanceof Plate) {
                 Plate plate = (Plate) reverb;
                 link = plate.isLink().getValue();
                 size = plate.getSize().getValue();
             } else {
                 throw new PrintException("Cannot determine reverb decay for class " + reverb.getClass());
             }
             String reverbDecay = Util.reverbDecayToString(link, size, parameterValue);
             sb.append(reverbDecay).append("s");
         } else if(":".equals(patchDestinationUnit)) {
             sb.append(parameterValue % 0x100).append(patchDestinationUnit).append(parameterValue / 0x100);
         } else if ("Hz".equals(patchDestinationUnit)) {
             sb.append(parameterValue).append(patchDestinationUnit);
         } else if ("100Hz".equals(patchDestinationUnit)) {
             sb.append(DECIMAL_2DP.format(parameterValue / 100.0)).append(patchDestinationUnit.substring(3));
         } else if ("OnOff".equals(patchDestinationUnit)) {
             sb.append(Util.onOffToString(parameterValue == 1));
         } else if ("Send".equals(patchEffect) && "Level".equals(patchParameter)) {
             sb.append(signInt(parameterValue)).append(patchDestinationUnit);
         } else {
             // TODO better way of determining what sign is necessary
             if (patchDestinationUnit.indexOf("-") == 0) {
                 String newPatchDestinationUnit = patchDestinationUnit.substring(1);
                 sb.append(signInt(parameterValue)).append(newPatchDestinationUnit);
             } else {
                 sb.append(parameterValue).append(patchDestinationUnit);
             }
         }
         return sb.toString();
     }
 
     static Parameter getEffectParameter(final Program program, final int effectIndex, final int parameterIndex) {
         DataObject dataObject;
         switch (effectIndex) {
             case 0:
                 dataObject = program.getEffect1();
                 break;
             case 1:
                 dataObject = program.getEffect2();
                 break;
             case 2:
                 dataObject = program.getChorus();
                 break;
             case 3:
                 dataObject = program.getDelay();
                 break;
             case 4:
                 dataObject = program.getReverb();
                 break;
             case 5:
                 dataObject = program.getEq();
                 break;
             case 6:
                 dataObject = program.getGain();
                 break;
             case 7:
                 dataObject = program.getKnob();
                 break;
             case 8:
                 dataObject = program.getLfo1();
                 break;
             case 9:
                 dataObject = program.getLfo2();
                 break;
             case 10:
                 dataObject = program.getRandom();
                 break;
             case 11:
                 dataObject = program.getAb();
                 break;
             case 12:
                 dataObject = program.getEnvelopeGenerator();
                 break;
             default:
                 dataObject = null;
         }
         return dataObject == null ? null : dataObject.getParameter(parameterIndex);
     }
 
     static String printKnob(final Knob knob) {
         final StringBuilder sb = new StringBuilder();
         sb.append("  ").append(printParameter(knob.getValue()));
         sb.append("  ").append(printParameter(knob.getLow()));
         sb.append("  ").append(printParameter(knob.getHigh()));
         sb.append("      Name: ").append(knob.getName()).append("\n");
         return sb.toString();
     }
 
     static String printLfo(final Lfo lfo) throws PrintException {
         final StringBuilder sb = new StringBuilder();
         sb.append("  ").append(printParameter(lfo.getMode()));
         sb.append("  ").append(printParameter(lfo.getRate()));
         sb.append("  ").append(printParameter(lfo.getPulseWidth()));
         sb.append("  ").append(printParameter(lfo.getPhase()));
         sb.append("  ").append(printParameter(lfo.getDepth()));
         sb.append("  ").append(printParameter(lfo.getOnLevel()));
         sb.append("  ").append(printParameter(lfo.getOnSource()));
         return sb.toString();
     }
 
     static String printRandom(final Random random) throws PrintException {
         final StringBuilder sb = new StringBuilder();
        sb.append("      Low: ").append(random.getLow()).append("\n");
        sb.append("      High: ").append(random.getHigh()).append("\n");
         sb.append("      Rate: ").append(random.getRate().getDisplayString()).append("\n");
         return sb.toString();
     }
 
     static String printAb(final Ab ab) {
         final StringBuilder sb = new StringBuilder();
         sb.append("      Mode: ").append(abModeToString(ab.getMode())).append("\n");
         sb.append("      A Rate: ").append(ab.getARate()).append("\n");
         sb.append("      B Rate: ").append(ab.getBRate()).append("\n");
         sb.append("      On Level: ").append(ab.getOnLevel()).append("\n");
         sb.append("      On Source: ").append(lfoOnSourceToString(ab.getOnSource())).append("\n");
         return sb.toString();
     }
 
     static String printEnvelopeGenerator(final EnvelopeGenerator envelopeGenerator) throws PrintException {
         final StringBuilder sb = new StringBuilder();
         sb.append("      Src1: ").append(envelopeGeneratorSourceToString(envelopeGenerator.getSrc1())).append("\n");
         sb.append("      Src2: ").append(envelopeGeneratorSourceToString(envelopeGenerator.getSrc2())).append("\n");
         sb.append("      A Trim: ").append(envelopeGenerator.getATrim()).append("\n");
         sb.append("      Resp: ").append(envelopeGenerator.getResponse()).append("\n");
         return sb.toString();
     }
 
     private static String toePatchToString(final int toePatch) throws PrintException {
         return TOE_PATCH_STATES[toePatch];
     }
 
     private static String effectTypeToString(final int effectType) {
         return EFFECT_TYPES[effectType];
     }
 
     private static String effectParameterToString(final int effectType, final int effectParameter) {
         // remove 13 from effectType as the 7 algorithm types and 6 controllers take care of themselves
         return EFFECT_PARAMETERS[effectType - 13][effectParameter];
     }
 
     private static String getEffectParameterUnits(final int effectType, final int effectParameter) {
         // remove 13 from effectType as the 7 algorithm types and 6 controllers take care of themselves
         return EFFECT_PARAMETER_UNITS[effectType - 13][effectParameter];
     }
 
     private static String lfoOnSourceToString(final int lfoOnSource) {
         return LFO_ON_SOURCES[lfoOnSource];
     }
 
     private static String tempoSourceToString(final int tempoSource) {
         return TEMPO_SOURCES[tempoSource];
     }
 
     static String beatValueToString(final int beatValue) {
         String value;
         if (beatValue < BEAT_VALUES.length) {
             value = BEAT_VALUES[beatValue];
         } else {
             value = (beatValue - BEAT_VALUES.length + 2) + " beats";
         }
         return value;
     }
 
     private static String tapSourceToString(final int tapSource) {
         return TAP_SOURCES[tapSource];
     }
 
     private static String abModeToString(final int abMode) {
         return AB_MODES[abMode];
     }
 
     private static String envelopeGeneratorSourceToString(final int envelopeGeneratorSource) {
         return ENVELOPE_GENERATOR_SOURCES[envelopeGeneratorSource];
     }
 
     static String speakerSimulatorCabinetToString(final int speakerSimulatorCabinet) {
         String cabinet = SPEAKER_SIMULATOR_CABINET_DESIGNS[speakerSimulatorCabinet / 4];
         String speaker = SPEAKER_SIMULATOR_SPEAKER_TYPES[speakerSimulatorCabinet % 4];
         return cabinet + speaker;
     }
 
     private static String noiseGateEnableToString(final int noiseGateEnable) {
         return NOISE_GATE_ENABLES[noiseGateEnable];
     }
 }
