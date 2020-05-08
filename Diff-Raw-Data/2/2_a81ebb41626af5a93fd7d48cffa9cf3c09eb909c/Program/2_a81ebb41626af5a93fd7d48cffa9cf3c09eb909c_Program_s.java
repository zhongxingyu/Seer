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
 
 package info.carlwithak.mpxg2.model;
 
 import info.carlwithak.mpxg2.model.effects.Chorus;
 import info.carlwithak.mpxg2.model.effects.Delay;
 import info.carlwithak.mpxg2.model.effects.Effect;
 import info.carlwithak.mpxg2.model.effects.Eq;
 import info.carlwithak.mpxg2.model.effects.Gain;
 import info.carlwithak.mpxg2.model.effects.Reverb;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author carl
  */
 public class Program {
     private Effect effect1;
     private Effect effect2;
     private Chorus chorus;
     private Delay delay;
     private Reverb reverb;
     private Eq eq;
     private Gain gain;
 
     // effect types
     private boolean isChorus;
     private boolean isDelay;
     private boolean isDistortion;
     private boolean isEq;
     private boolean isFlanger;
     private boolean isGain;
     private boolean isMod;
     private boolean isOverdrive;
     private boolean isPhaser;
     private boolean isPitch;
     private boolean isReverb;
     private boolean isSpeakerSim;
     private boolean isWah;
     private boolean isPrePost;
     private boolean isStandAlone;
     private boolean isInline;
 
     // guitar styles
     private boolean isAcoustic;
     private boolean isBass;
     private boolean isBlues;
     private boolean isClean;
     private boolean isCountry;
     private boolean isJazz;
     private boolean isRock;
 
     private RoutingData routing0;
     private RoutingData routing1;
     private RoutingData routing2;
     private RoutingData routing3;
     private RoutingData routing4;
     private RoutingData routing5;
     private RoutingData routing6;
     private RoutingData routing7;
     private RoutingData routing8;
 
     private int effect1ToePatch;
     private int effect2ToePatch;
     private int chorusToePatch;
     private int delayToePatch;
     private int reverbToePatch;
     private int eqToePatch;
     private int gainToePatch;
 
     private int effect1Algorithm;
     private int effect2Algorithm;
     private int chorusAlgorithm;
     private int delayAlgorithm;
     private int reverbAlgorithm;
     private int eqAlgorithm;
     private int gainAlgorithm;
 
     private String programName;
 
     // effects status
     private boolean effect1On;
     private boolean effect2On;
     private boolean chorusOn;
     private boolean delayOn;
     private boolean reverbOn;
     private boolean eqOn;
     private boolean gainOn;
     private boolean insertOn;
 
     // soft row
    private List<SoftRowItem> softRow = Arrays.asList(null, null, null, null, null, null, null, null, null, null);
 
     // tempo
     private int tempo;
     private int tempoSource;
     private int beatValue;
     private int tapSource;
     private int tapAverage;
     private int tapSourceLevel;
 
     // patching
     private Patch patch1;
     private Patch patch2;
     private Patch patch3;
     private Patch patch4;
     private Patch patch5;
 
     // controllers
     private int knobValue;
     private int knobLow;
     private int knobHigh;
     private String knobName;
     private int lfo1Mode;
     private double lfo1Rate;
     private int lfo1PulseWidth;
     private int lfo1Phase;
     private int lfo1Depth;
     private int lfo1OnLevel;
     private int lfo1OnSource;
     private int lfo2Mode;
     private double lfo2Rate;
     private int lfo2PulseWidth;
     private int lfo2Phase;
     private int lfo2Depth;
     private int lfo2OnLevel;
     private int lfo2OnSource;
     private int randomLow;
     private int randomHigh;
     private double randomRate;
     private int abMode;
     private int aRate;
     private int bRate;
     private int abOnLevel;
     private int abOnSource;
     private int envelopeGeneratorSrc1;
     private int envelopeGeneratorSrc2;
     private int envelopeGeneratorATrim;
     private int envelopeGeneratorResponse;
 
     // noise gate
     private NoiseGate noiseGate;
 
     private int bypassState;
 
     // speaker simulator
     private int speakerSimulatorEnable;
     private int speakerSimulatorCabinet;
 
     // mix
     private int sendLevel;
     private int sendBypassLevel;
     private int postMix;
     private int postLevel;
     private int postBypassLevel;
 
     private int programNumber;
 
     public Effect getEffect1() {
         return effect1;
     }
 
     public void setEffect1(final Effect effect1) {
         this.effect1 = effect1;
     }
 
     public Effect getEffect2() {
         return effect2;
     }
 
     public void setEffect2(final Effect effect2) {
         this.effect2 = effect2;
     }
 
     public Chorus getChorus() {
         return chorus;
     }
 
     public void setChorus(final Chorus chorus) {
         this.chorus = chorus;
     }
 
     public Delay getDelay() {
         return delay;
     }
 
     public void setDelay(final Delay delay) {
         this.delay = delay;
     }
 
     public Reverb getReverb() {
         return reverb;
     }
 
     public void setReverb(final Reverb reverb) {
         this.reverb = reverb;
     }
 
     public Eq getEq() {
         return eq;
     }
 
     public void setEq(final Eq eq) {
         this.eq = eq;
     }
 
     public Gain getGain() {
         return gain;
     }
 
     public void setGain(final Gain gain) {
         this.gain = gain;
     }
 
     public boolean isChorus() {
         return isChorus;
     }
 
     public void setIsChorus(boolean isChorus) {
         this.isChorus = isChorus;
     }
 
     public boolean isDelay() {
         return isDelay;
     }
 
     public void setIsDelay(boolean isDelay) {
         this.isDelay = isDelay;
     }
 
     public boolean isDistortion() {
         return isDistortion;
     }
 
     public void setIsDistortion(boolean isDistortion) {
         this.isDistortion = isDistortion;
     }
 
     public boolean isEq() {
         return isEq;
     }
 
     public void setIsEq(boolean isEq) {
         this.isEq = isEq;
     }
 
     public boolean isFlanger() {
         return isFlanger;
     }
 
     public void setIsFlanger(boolean isFlanger) {
         this.isFlanger = isFlanger;
     }
 
     public boolean isGain() {
         return isGain;
     }
 
     public void setIsGain(boolean isGain) {
         this.isGain = isGain;
     }
 
     public boolean isMod() {
         return isMod;
     }
 
     public void setIsMod(boolean isMod) {
         this.isMod = isMod;
     }
 
     public boolean isOverdrive() {
         return isOverdrive;
     }
 
     public void setIsOverdrive(boolean isOverdrive) {
         this.isOverdrive = isOverdrive;
     }
 
     public boolean isPhaser() {
         return isPhaser;
     }
 
     public void setIsPhaser(boolean isPhaser) {
         this.isPhaser = isPhaser;
     }
 
     public boolean isPitch() {
         return isPitch;
     }
 
     public void setIsPitch(boolean isPitch) {
         this.isPitch = isPitch;
     }
 
     public boolean isReverb() {
         return isReverb;
     }
 
     public void setIsReverb(boolean isReverb) {
         this.isReverb = isReverb;
     }
 
     public boolean isSpeakerSim() {
         return isSpeakerSim;
     }
 
     public void setIsSpeakerSim(boolean isSpeakerSim) {
         this.isSpeakerSim = isSpeakerSim;
     }
 
     public boolean isWah() {
         return isWah;
     }
 
     public void setIsWah(boolean isWah) {
         this.isWah = isWah;
     }
 
     public boolean isPrePost() {
         return isPrePost;
     }
 
     public void setIsPrePost(boolean isPrePost) {
         this.isPrePost = isPrePost;
     }
 
     public boolean isStandAlone() {
         return isStandAlone;
     }
 
     public void setIsStandAlone(boolean isStandAlone) {
         this.isStandAlone = isStandAlone;
     }
 
     public boolean isInline() {
         return isInline;
     }
 
     public void setIsInline(boolean isInline) {
         this.isInline = isInline;
     }
 
     public boolean isAcoustic() {
         return isAcoustic;
     }
 
     public void setIsAcoustic(boolean isAcoustic) {
         this.isAcoustic = isAcoustic;
     }
 
     public boolean isBass() {
         return isBass;
     }
 
     public void setIsBass(boolean isBass) {
         this.isBass = isBass;
     }
 
     public boolean isBlues() {
         return isBlues;
     }
 
     public void setIsBlues(boolean isBlues) {
         this.isBlues = isBlues;
     }
 
     public boolean isClean() {
         return isClean;
     }
 
     public void setIsClean(boolean isClean) {
         this.isClean = isClean;
     }
 
     public boolean isCountry() {
         return isCountry;
     }
 
     public void setIsCountry(boolean isCountry) {
         this.isCountry = isCountry;
     }
 
     public boolean isJazz() {
         return isJazz;
     }
 
     public void setIsJazz(boolean isJazz) {
         this.isJazz = isJazz;
     }
 
     public boolean isRock() {
         return isRock;
     }
 
     public void setIsRock(boolean isRock) {
         this.isRock = isRock;
     }
 
     public RoutingData getRouting0() {
         return routing0;
     }
 
     public void setRouting0(RoutingData routing0) {
         this.routing0 = routing0;
     }
 
     public RoutingData getRouting1() {
         return routing1;
     }
 
     public void setRouting1(RoutingData routing1) {
         this.routing1 = routing1;
     }
 
     public RoutingData getRouting2() {
         return routing2;
     }
 
     public void setRouting2(RoutingData routing2) {
         this.routing2 = routing2;
     }
 
     public RoutingData getRouting3() {
         return routing3;
     }
 
     public void setRouting3(RoutingData routing3) {
         this.routing3 = routing3;
     }
 
     public RoutingData getRouting4() {
         return routing4;
     }
 
     public void setRouting4(RoutingData routing4) {
         this.routing4 = routing4;
     }
 
     public RoutingData getRouting5() {
         return routing5;
     }
 
     public void setRouting5(RoutingData routing5) {
         this.routing5 = routing5;
     }
 
     public RoutingData getRouting6() {
         return routing6;
     }
 
     public void setRouting6(RoutingData routing6) {
         this.routing6 = routing6;
     }
 
     public RoutingData getRouting7() {
         return routing7;
     }
 
     public void setRouting7(RoutingData routing7) {
         this.routing7 = routing7;
     }
 
     public RoutingData getRouting8() {
         return routing8;
     }
 
     public void setRouting8(RoutingData routing8) {
         this.routing8 = routing8;
     }
 
     public int getEffect1ToePatch() {
         return effect1ToePatch;
     }
 
     public void setEffect1ToePatch(final int effect1ToePatch) {
         this.effect1ToePatch = effect1ToePatch;
     }
 
     public int getEffect2ToePatch() {
         return effect2ToePatch;
     }
 
     public void setEffect2ToePatch(final int effect2ToePatch) {
         this.effect2ToePatch = effect2ToePatch;
     }
 
     public int getChorusToePatch() {
         return chorusToePatch;
     }
 
     public void setChorusToePatch(final int chorusToePatch) {
         this.chorusToePatch = chorusToePatch;
     }
 
     public int getDelayToePatch() {
         return delayToePatch;
     }
 
     public void setDelayToePatch(final int delayToePatch) {
         this.delayToePatch = delayToePatch;
     }
 
     public int getReverbToePatch() {
         return reverbToePatch;
     }
 
     public void setReverbToePatch(final int reverbToePatch) {
         this.reverbToePatch = reverbToePatch;
     }
 
     public int getEqToePatch() {
         return eqToePatch;
     }
 
     public void setEqToePatch(final int eqToePatch) {
         this.eqToePatch = eqToePatch;
     }
 
     public int getGainToePatch() {
         return gainToePatch;
     }
 
     public void setGainToePatch(final int gainToePatch) {
         this.gainToePatch = gainToePatch;
     }
 
     public int getEffect1Algorithm() {
         return effect1Algorithm;
     }
 
     public void setEffect1Algorithm(final int effect1Algorithm) {
         this.effect1Algorithm = effect1Algorithm;
     }
 
     public int getEffect2Algorithm() {
         return effect2Algorithm;
     }
 
     public void setEffect2Algorithm(final int effect2Algorithm) {
         this.effect2Algorithm = effect2Algorithm;
     }
 
     public int getChorusAlgorithm() {
         return chorusAlgorithm;
     }
 
     public void setChorusAlgorithm(final int chorusAlgorithm) {
         this.chorusAlgorithm = chorusAlgorithm;
     }
 
     public int getDelayAlgorithm() {
         return delayAlgorithm;
     }
 
     public void setDelayAlgorithm(final int delayAlgorithm) {
         this.delayAlgorithm = delayAlgorithm;
     }
 
     public int getReverbAlgorithm() {
         return reverbAlgorithm;
     }
 
     public void setReverbAlgorithm(final int reverbAlgorithm) {
         this.reverbAlgorithm = reverbAlgorithm;
     }
 
     public int getEqAlgorithm() {
         return eqAlgorithm;
     }
 
     public void setEqAlgorithm(final int eqAlgorithm) {
         this.eqAlgorithm = eqAlgorithm;
     }
 
     public int getGainAlgorithm() {
         return gainAlgorithm;
     }
 
     public void setGainAlgorithm(final int gainAlgorithm) {
         this.gainAlgorithm = gainAlgorithm;
     }
 
     public String getProgramName() {
         return programName;
     }
 
     public void setProgramName(final String programName) {
         this.programName = programName;
     }
 
     public boolean isEffect1On() {
         return effect1On;
     }
 
     public void setEffect1On(final boolean effect1On) {
         this.effect1On = effect1On;
     }
 
     public boolean isEffect2On() {
         return effect2On;
     }
 
     public void setEffect2On(final boolean effect2On) {
         this.effect2On = effect2On;
     }
 
     public boolean isChorusOn() {
         return chorusOn;
     }
 
     public void setChorusOn(final boolean chorusOn) {
         this.chorusOn = chorusOn;
     }
 
     public boolean isDelayOn() {
         return delayOn;
     }
 
     public void setDelayOn(final boolean delayOn) {
         this.delayOn = delayOn;
     }
 
     public boolean isReverbOn() {
         return reverbOn;
     }
 
     public void setReverbOn(final boolean reverbOn) {
         this.reverbOn = reverbOn;
     }
 
     public boolean isEqOn() {
         return eqOn;
     }
 
     public void setEqOn(final boolean eqOn) {
         this.eqOn = eqOn;
     }
 
     public boolean isGainOn() {
         return gainOn;
     }
 
     public void setGainOn(final boolean gainOn) {
         this.gainOn = gainOn;
     }
 
     public boolean isInsertOn() {
         return insertOn;
     }
 
     public void setInsertOn(final boolean insertOn) {
         this.insertOn = insertOn;
     }
 
     public int getSoftRowEffectType(final int softRowPosition) {
         return this.softRow.get(softRowPosition).getEffectType();
     }
 
     public void setSoftRowEffectType(final int softRowPosition, final int softRowEffectType) {
         if (this.softRow.get(softRowPosition) == null) {
             this.softRow.set(softRowPosition, new SoftRowItem());
         }
         this.softRow.get(softRowPosition).setEffectType(softRowEffectType);
     }
 
     public int getSoftRowParameter(final int softRowPosition) {
         return this.softRow.get(softRowPosition).getParameter();
     }
 
     public void setSoftRowParameter(final int softRowPosition, final int softRowParameter) {
         this.softRow.get(softRowPosition).setParameter(softRowParameter);
     }
 
     public int getTempo() {
         return tempo;
     }
 
     public void setTempo(final int tempo) {
         this.tempo = tempo;
     }
 
     public int getTempoSource() {
         return tempoSource;
     }
 
     public void setTempoSource(final int tempoSource) {
         this.tempoSource = tempoSource;
     }
 
     public int getBeatValue() {
         return beatValue;
     }
 
     public void setBeatValue(final int beatValue) {
         this.beatValue = beatValue;
     }
 
     public int getTapSource() {
         return tapSource;
     }
 
     public void setTapSource(final int tapSource) {
         this.tapSource = tapSource;
     }
 
     public int getTapAverage() {
         return tapAverage;
     }
 
     public void setTapAverage(final int tapAverage) {
         this.tapAverage = tapAverage;
     }
 
     public int getTapSourceLevel() {
         return tapSourceLevel;
     }
 
     public void setTapSourceLevel(final int tapSourceLevel) {
         this.tapSourceLevel = tapSourceLevel;
     }
 
     public Patch getPatch1() {
         return patch1;
     }
 
     public void setPatch1(final Patch patch1) {
         this.patch1 = patch1;
     }
 
     public Patch getPatch2() {
         return patch2;
     }
 
     public void setPatch2(final Patch patch2) {
         this.patch2 = patch2;
     }
 
     public Patch getPatch3() {
         return patch3;
     }
 
     public void setPatch3(final Patch patch3) {
         this.patch3 = patch3;
     }
 
     public Patch getPatch4() {
         return patch4;
     }
 
     public void setPatch4(final Patch patch4) {
         this.patch4 = patch4;
     }
 
     public Patch getPatch5() {
         return patch5;
     }
 
     public void setPatch5(final Patch patch5) {
         this.patch5 = patch5;
     }
 
     public int getKnobValue() {
         return knobValue;
     }
 
     public void setKnobValue(final int knobValue) {
         this.knobValue = knobValue;
     }
 
     public int getKnobLow() {
         return knobLow;
     }
 
     public void setKnobLow(final int knobLow) {
         this.knobLow = knobLow;
     }
 
     public int getKnobHigh() {
         return knobHigh;
     }
 
     public void setKnobHigh(final int knobHigh) {
         this.knobHigh = knobHigh;
     }
 
     public String getKnobName() {
         return knobName;
     }
 
     public void setKnobName(final String knobName) {
         this.knobName = knobName;
     }
 
     public int getLfo1Mode() {
         return lfo1Mode;
     }
 
     public void setLfo1Mode(final int lfo1Mode) {
         this.lfo1Mode = lfo1Mode;
     }
 
     public double getLfo1Rate() {
         return lfo1Rate;
     }
 
     public void setLfo1Rate(final double lfo1Rate) {
         this.lfo1Rate = lfo1Rate;
     }
 
     public int getLfo1PulseWidth() {
         return lfo1PulseWidth;
     }
 
     public void setLfo1PulseWidth(final int lfo1PulseWidth) {
         this.lfo1PulseWidth = lfo1PulseWidth;
     }
 
     public int getLfo1Phase() {
         return lfo1Phase;
     }
 
     public void setLfo1Phase(final int lfo1Phase) {
         this.lfo1Phase = lfo1Phase;
     }
 
     public int getLfo1Depth() {
         return lfo1Depth;
     }
 
     public void setLfo1Depth(final int lfo1Depth) {
         this.lfo1Depth = lfo1Depth;
     }
 
     public int getLfo1OnLevel() {
         return lfo1OnLevel;
     }
 
     public void setLfo1OnLevel(final int lfo1OnLevel) {
         this.lfo1OnLevel = lfo1OnLevel;
     }
 
     public int getLfo1OnSource() {
         return lfo1OnSource;
     }
 
     public void setLfo1OnSource(final int lfo1OnSource) {
         this.lfo1OnSource = lfo1OnSource;
     }
 
     public int getLfo2Mode() {
         return lfo2Mode;
     }
 
     public void setLfo2Mode(final int lfo2Mode) {
         this.lfo2Mode = lfo2Mode;
     }
 
     public double getLfo2Rate() {
         return lfo2Rate;
     }
 
     public void setLfo2Rate(final double lfo2Rate) {
         this.lfo2Rate = lfo2Rate;
     }
 
     public int getLfo2PulseWidth() {
         return lfo2PulseWidth;
     }
 
     public void setLfo2PulseWidth(final int lfo2PulseWidth) {
         this.lfo2PulseWidth = lfo2PulseWidth;
     }
 
     public int getLfo2Phase() {
         return lfo2Phase;
     }
 
     public void setLfo2Phase(final int lfo2Phase) {
         this.lfo2Phase = lfo2Phase;
     }
 
     public int getLfo2Depth() {
         return lfo2Depth;
     }
 
     public void setLfo2Depth(final int lfo2Depth) {
         this.lfo2Depth = lfo2Depth;
     }
 
     public int getLfo2OnLevel() {
         return lfo2OnLevel;
     }
 
     public void setLfo2OnLevel(final int lfo2OnLevel) {
         this.lfo2OnLevel = lfo2OnLevel;
     }
 
     public int getLfo2OnSource() {
         return lfo2OnSource;
     }
 
     public void setLfo2OnSource(final int lfo2OnSource) {
         this.lfo2OnSource = lfo2OnSource;
     }
 
     public int getRandomLow() {
         return randomLow;
     }
 
     public void setRandomLow(final int randomLow) {
         this.randomLow = randomLow;
     }
 
     public int getRandomHigh() {
         return randomHigh;
     }
 
     public void setRandomHigh(final int randomHigh) {
         this.randomHigh = randomHigh;
     }
 
     public double getRandomRate() {
         return randomRate;
     }
 
     public void setRandomRate(final double randomRate) {
         this.randomRate = randomRate;
     }
 
     public int getABMode() {
         return abMode;
     }
 
     public void setABMode(final int abMode) {
         this.abMode = abMode;
     }
 
     public int getARate() {
         return aRate;
     }
 
     public void setARate(final int aRate) {
         this.aRate = aRate;
     }
 
     public int getBRate() {
         return bRate;
     }
 
     public void setBRate(final int bRate) {
         this.bRate = bRate;
     }
 
     public int getABOnLevel() {
         return abOnLevel;
     }
 
     public void setABOnLevel(final int abOnLevel) {
         this.abOnLevel = abOnLevel;
     }
 
     public int getABOnSource() {
         return abOnSource;
     }
 
     public void setABOnSource(final int abOnSource) {
         this.abOnSource = abOnSource;
     }
 
     public int getEnvelopeGeneratorSrc1() {
         return envelopeGeneratorSrc1;
     }
 
     public void setEnvelopeGeneratorSrc1(final int envelopeGeneratorSrc1) {
         this.envelopeGeneratorSrc1 = envelopeGeneratorSrc1;
     }
 
     public int getEnvelopeGeneratorSrc2() {
         return envelopeGeneratorSrc2;
     }
 
     public void setEnvelopeGeneratorSrc2(final int envelopeGeneratorSrc2) {
         this.envelopeGeneratorSrc2 = envelopeGeneratorSrc2;
     }
 
     public int getEnvelopeGeneratorATrim() {
         return envelopeGeneratorATrim;
     }
 
     public void setEnvelopeGeneratorATrim(final int envelopeGeneratorATrim) {
         this.envelopeGeneratorATrim = envelopeGeneratorATrim;
     }
 
     public int getEnvelopeGeneratorResponse() {
         return envelopeGeneratorResponse;
     }
 
     public void setEnvelopeGeneratorResponse(final int envelopeGeneratorResponse) {
         this.envelopeGeneratorResponse = envelopeGeneratorResponse;
     }
 
     public NoiseGate getNoiseGate() {
         return noiseGate;
     }
 
     public void setNoiseGate(final NoiseGate noiseGate) {
         this.noiseGate = noiseGate;
     }
 
     public int getBypassState() {
         return bypassState;
     }
 
     public void setBypassState(final int bypassState) {
         this.bypassState = bypassState;
     }
 
     public int getSpeakerSimulatorEnable() {
         return speakerSimulatorEnable;
     }
 
     public void setSpeakerSimulatorEnable(final int speakerSimulatorEnable) {
         this.speakerSimulatorEnable = speakerSimulatorEnable;
     }
 
     public int getSpeakerSimulatorCabinet() {
         return speakerSimulatorCabinet;
     }
 
     public void setSpeakerSimulatorCabinet(final int speakerSimulatorCabinet) {
         this.speakerSimulatorCabinet = speakerSimulatorCabinet;
     }
 
     public int getSendLevel() {
         return sendLevel;
     }
 
     public void setSendLevel(final int sendLevel) {
         this.sendLevel = sendLevel;
     }
 
     public int getSendBypassLevel() {
         return sendBypassLevel;
     }
 
     public void setSendBypassLevel(final int sendBypassLevel) {
         this.sendBypassLevel = sendBypassLevel;
     }
 
     public int getPostMix() {
         return postMix;
     }
 
     public void setPostMix(final int postMix) {
         this.postMix = postMix;
     }
 
     public int getPostLevel() {
         return postLevel;
     }
 
     public void setPostLevel(final int postLevel) {
         this.postLevel = postLevel;
     }
 
     public int getPostBypassLevel() {
         return postBypassLevel;
     }
 
     public void setPostBypassLevel(final int postBypassLevel) {
         this.postBypassLevel = postBypassLevel;
     }
 
     public int getProgramNumber() {
         return programNumber;
     }
 
     public void setProgramNumber(final int programNumber) {
         this.programNumber = programNumber;
     }
 }
