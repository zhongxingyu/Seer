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
 
 import info.carlwithak.mpxg2.model.parameters.Parameter;
 import java.util.Locale;
 
 /**
  * The Patching system in the MPX G2 allows both internal and external control
  * sources to control program parameters. Each program contains 5 Patches which
  * each takes a single control source to control a single parameter.
  *
  * @author Carl Green
  */
 public class Patch {
     enum PatchSource {
         NONE(null),
         /* Internal Controllers */
         CTLS_OFF, CTLS_ON, CTLS_KNOB("Ctls Knob"), CTLS_PULS1("Ctls Puls1"), CTLS_TRI1("Ctls Tri1"), CTLS_SINE1("Ctls Sine1"),
         CTLS_COS1("Ctls Cos1"), CTLS_PULS2("Ctls Puls2"), CTLS_TRI2("Ctls Tri2"), CTLS_SINE2("Ctls Sine2"), CTLS_COS2("Ctls Cos2"), CTLS_RAND("Ctls Rand"),
         CTLS_ENV("Ctls Env"), CTLS_INLVL, CTLS_RNLVL, CTLS_AB("Ctls A/B"), CTLS_ATRG,
         CTLS_BTRG, CTLS_ABTRG, CTLS_PEDAL, CTLS_TOG1, CTLS_TOG2, CTLS_TOG3,
         CTLS_SW1, CTLS_SW2, CTLS_SW3,
         /* Midi Controllers */
         MIDI_CC1, MIDI_CC2, MIDI_CC3, MIDI_CC4, MIDI_CC5, MIDI_CC6, MIDI_CC7,
         MIDI_CC8, MIDI_CC9, MIDI_CC10, MIDI_CC11, MIDI_CC12, MIDI_CC13,
         MIDI_CC14, MIDI_CC15, MIDI_CC16, MIDI_CC17, MIDI_CC18, MIDI_CC19,
         MIDI_CC20, MIDI_CC21, MIDI_CC22, MIDI_CC23, MIDI_CC24, MIDI_CC25,
         MIDI_CC26, MIDI_CC27, MIDI_CC28, MIDI_CC29, MIDI_CC30, MIDI_CC31,
         MIDI_CC33, MIDI_CC34, MIDI_CC35, MIDI_CC36, MIDI_CC37, MIDI_CC38,
         MIDI_CC39, MIDI_CC40, MIDI_CC41, MIDI_CC42, MIDI_CC43, MIDI_CC44,
         MIDI_CC45, MIDI_CC46, MIDI_CC47, MIDI_CC48, MIDI_CC49, MIDI_CC50,
         MIDI_CC51, MIDI_CC52, MIDI_CC53, MIDI_CC54, MIDI_CC55, MIDI_CC56,
         MIDI_CC57, MIDI_CC58, MIDI_CC59, MIDI_CC60, MIDI_CC61, MIDI_CC62,
         MIDI_CC63, MIDI_CC64, MIDI_CC65, MIDI_CC66, MIDI_CC67, MIDI_CC68,
         MIDI_CC69, MIDI_CC70, MIDI_CC71, MIDI_CC72, MIDI_CC73, MIDI_CC74,
         MIDI_CC75, MIDI_CC76, MIDI_CC77, MIDI_CC78, MIDI_CC79, MIDI_CC80,
         MIDI_CC81, MIDI_CC82, MIDI_CC83, MIDI_CC84, MIDI_CC85, MIDI_CC86,
         MIDI_CC87, MIDI_CC88, MIDI_CC89, MIDI_CC90, MIDI_CC91, MIDI_CC92,
         MIDI_CC93, MIDI_CC94, MIDI_CC95, MIDI_CC96, MIDI_CC97, MIDI_CC98,
         MIDI_CC99, MIDI_CC100, MIDI_CC101, MIDI_CC102, MIDI_CC103, MIDI_CC104,
         MIDI_CC105, MIDI_CC106, MIDI_CC107, MIDI_CC108, MIDI_CC109, MIDI_CC110,
         MIDI_CC111, MIDI_CC112, MIDI_CC113, MIDI_CC114, MIDI_CC115, MIDI_CC116,
         MIDI_CC117, MIDI_CC118, MIDI_CC119,
         /* Midi Controls */
         MIDI_BEND, MIDI_TOUCH, MIDI_VEL, MIDI_LASTNOTE, MIDI_LOWNOTE,
         MIDI_HIGHNOTE, MIDI_TEMPO, MIDI_CMNDS, MIDI_GATE, MIDI_TRIG, MIDI_LGATE,
        MIDI_TSW, MIDI_TOE("Midi Toe");
 
         private final String displayName;
 
         /**
          * Generates correct display name for all Midi CC values. For anything
          * else where a name hasn't been defined it makes a guess.
          */
         private PatchSource() {
             final String MIDI_CONTROLLER_PREFIX = "MIDI_CC";
             if (name().indexOf(MIDI_CONTROLLER_PREFIX) > -1) {
                this.displayName = "Midi CC" + name().substring(MIDI_CONTROLLER_PREFIX.length());
             } else {
                 this.displayName = name();
             }
         }
 
         private PatchSource(final String displayName) {
             this.displayName = displayName;
         }
 
         private String getDisplayName() {
             return displayName;
         }
     }
 
     /**
      * Cannot select PATCHING, SOFTROW, MISC, SPKRSIM, TOE_PATCHES, AUDIO_ROUTE
      * or ALG_SELECT.
      */
     enum PatchDestination {
         EFFECT_1, EFFECT_2, CHORUS, DELAY, REVERB, EQ, GAIN,
         CTLS_KNOB, CTLS_LFO1("LFO1"), CTLS_LFO2("LFO2"), CTLS_RAND("Rand"), CTLS_AB, CTLS_ENV,
         PATCHING, SOFTROW, POST, SEND, MISC, SPKRSIM, NGATE("NGat"), TEMPO,
         TOE_PATCHES, AUDIO_ROUTE, ALG_SELECT, BYPASS("Byp");
 
         private final String displayName;
 
         private PatchDestination() {
             String name = name();
             this.displayName = name.substring(0, 1) + name.substring(1).toLowerCase(Locale.ENGLISH).replace('_', ' ');
         }
 
         private PatchDestination(final String displayName) {
             this.displayName = displayName;
         }
 
         private String getDisplayName() {
             return displayName;
         }
     }
 
     private PatchSource source = PatchSource.NONE;
     private Integer sourceMin;
     private Integer sourceMid;
     private Integer sourceMax;
     private PatchDestination destinationEffect;
     private int destinationParameter;
     private Parameter destinationMin;
     private Parameter destinationMid;
     private Parameter destinationMax;
 
     public int getSourceIndex() {
         return source.ordinal();
     }
 
     public String getSourceName() {
         return source.getDisplayName();
     }
 
     public void setSource(final int source) {
         this.source = PatchSource.values()[source];
     }
 
     public Integer getSourceMin() {
         return sourceMin;
     }
 
     public void setSourceMin(final Integer sourceMin) {
         this.sourceMin = sourceMin;
     }
 
     public Integer getSourceMid() {
         return sourceMid;
     }
 
     public void setSourceMid(final Integer sourceMid) {
         this.sourceMid = sourceMid;
     }
 
     public Integer getSourceMax() {
         return sourceMax;
     }
 
     public void setSourceMax(final Integer sourceMax) {
         this.sourceMax = sourceMax;
     }
 
     public Integer getDestinationEffectIndex() {
         return destinationEffect == null ? null : destinationEffect.ordinal();
     }
 
     public String getDestinationEffectName() {
         return destinationEffect == null ? null : destinationEffect.getDisplayName();
     }
 
     public void setDestinationEffect(final Integer destinationEffect) {
         this.destinationEffect = destinationEffect == null ? null : PatchDestination.values()[destinationEffect];
     }
 
     public int getDestinationParameter() {
         return destinationParameter;
     }
 
     public void setDestinationParameter(final int destinationParameter) {
         this.destinationParameter = destinationParameter;
     }
 
     public Parameter getDestinationMin() {
         return destinationMin;
     }
 
     public void setDestinationMin(final Parameter destinationMin) {
         this.destinationMin = destinationMin;
     }
 
     public Parameter getDestinationMid() {
         return destinationMid;
     }
 
     public void setDestinationMid(final Parameter destinationMid) {
         this.destinationMid = destinationMid;
     }
 
     public Parameter getDestinationMax() {
         return destinationMax;
     }
 
     public void setDestinationMax(final Parameter destinationMax) {
         this.destinationMax = destinationMax;
     }
 }
