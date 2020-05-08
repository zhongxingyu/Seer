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
 
 /**
  * Various helper methods for printing.
  *
  * @author Carl Green
  */
 public class Util {
 
     private static final String[] DELAY_INSERTS = {
         "Effect 1", "Effect 2", "Chorus", "Delay", "Reverb", "EQ", "Gain"
     };
 
     private static final double[] REVERB_DELAY_TIMES = {
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1.41
     };
 
     private static final String[] REVERB_BASS = {
         "0.2", "0.4", "0.6", "0.8", "1.0", "1.2", "1.5", "2.0", "3.0", "4.0"
     };
 
     private static final double[] REVERB_LINK_SIZES = {
         12.0, 20.0, 28.0, 36.0, 44.0, 52.0, 60.0, 68.0, 76.0, 76.5
     };
 
     private static final String[][] REVERB_DECAY = {
         {
             "0.07", "0.07", "0.08", "0.08", "0.08", "0.09", "0.09", "0.09", "0.10",
             "0.10", "0.10", "0.10", "0.11", "0.11", "0.11", "0.12", "0.12", "0.12",
             "0.13", "0.13", "0.13", "0.14", "0.14", "0.15", "0.15", "0.15", "0.16",
             "0.16", "0.17", "0.18", "0.18", "0.19", "0.19", "0.20", "0.21", "0.22",
             "0.22", "0.23", "0.24", "0.25", "0.26", "0.28", "0.29", "0.30", "0.32",
             "0.34", "0.36", "0.38", "0.40", "0.43", "0.46", "0.50", "0.54", "0.59",
             "0.65", "0.72", "0.82", "0.93", "1.09", "1.31", "1.64", "2.19", "3.29",
             "6.58"
         },
         {
             "0.09", "0.10", "0.11", "0.12", "0.12", "0.13", "0.13", "0.14", "0.15",
             "0.15", "0.16", "0.16", "0.17", "0.18", "0.18", "0.19", "0.20", "0.20",
             "0.21", "0.22", "0.22", "0.23", "0.24", "0.25", "0.26", "0.27", "0.27",
             "0.28", "0.30", "0.31", "0.32", "0.33", "0.34", "0.36", "0.37", "0.39",
             "0.40", "0.42", "0.44", "0.46", "0.48", "0.51", "0.53", "0.56", "0.60",
             "0.63", "0.67", "0.71", "0.76", "0.82", "0.88", "0.95", "1.04", "1.14",
             "1.26", "1.40", "1.59", "1.82", "2.14", "2.58", "3.24", "4.34", "6.53",
             "13.1"
         },
         {
             "0.12", "0.13", "0.14", "0.15", "0.16", "0.17", "0.18", "0.19", "0.20",
             "0.21", "0.22", "0.22", "0.23", "0.24", "0.25", "0.26", "0.27", "0.28",
             "0.29", "0.30", "0.31", "0.32", "0.34", "0.35", "0.36", "0.38", "0.39",
             "0.40", "0.42", "0.44", "0.45", "0.47", "0.49", "0.51", "0.54", "0.56",
             "0.58", "0.61", "0.64", "0.67", "0.70", "0.74", "0.78", "0.82", "0.87",
             "0.92", "0.98", "1.05", "1.12", "1.20", "1.30", "1.41", "1.53", "1.68",
             "1.86", "2.08", "2.36", "2.71", "3.18", "3.84", "4.83", "6.48", "9.78",
             "19.6"
         },
         {
             "0.14", "0.16", "0.18", "0.19", "0.20", "0.21", "0.23", "0.24", "0.25",
             "0.26", "0.27", "0.28", "0.30", "0.31", "0.32", "0.33", "0.35", "0.36",
             "0.37", "0.39", "0.40", "0.42", "0.43", "0.45", "0.47", "0.49", "0.51",
             "0.53", "0.55", "0.57", "0.59", "0.62", "0.64", "0.67", "0.70", "0.73",
             "0.76", "0.80", "0.84", "0.88", "0.92", "0.97", "1.03", "1.08", "1.15",
             "1.22", "1.29", "1.38", "1.48", "1.59", "1.72", "1.86", "2.03", "2.23",
             "2.47", "2.76", "3.13", "3.60", "4.23", "5.11", "6.43", "8.63", "13.0",
             "26.2"
         },
         {
             "0.17", "0.19", "0.21", "0.23", "0.24", "0.26", "0.27", "0.29", "0.30",
             "0.31", "0.33", "0.34", "0.36", "0.37", "0.39", "0.41", "0.42", "0.44",
             "0.46", "0.47", "0.49", "0.51", "0.53", "0.55", "0.57", "0.60", "0.62",
             "0.65", "0.67", "0.70", "0.73", "0.76", "0.79", "0.83", "0.86", "0.90",
             "0.94", "0.99", "1.04", "1.09", "1.14", "1.20", "1.27", "1.34", "1.42",
             "1.51", "1.61", "1.72", "1.84", "1.98", "2.13", "2.32", "2.53", "2.78",
             "3.08", "3.45", "3.90", "4.49", "5.28", "6.38", "8.03", "10.7", "16.2",
             "32.7"
         },
         {
             "0.19", "0.22", "0.24", "0.26", "0.28", "0.30", "0.32", "0.33", "0.35",
             "0.37", "0.39", "0.40", "0.42", "0.44", "0.46", "0.48", "0.50", "0.52",
             "0.54", "0.56", "0.58", "0.61", "0.63", "0.65", "0.68", "0.71", "0.74",
             "0.77", "0.80", "0.83", "0.87", "0.90", "0.94", "0.98", "1.03", "1.07",
             "1.12", "1.18", "1.23", "1.30", "1.36", "1.44", "1.52", "1.60", "1.70",
             "1.80", "1.92", "2.05", "2.20", "2.36", "2.55", "2.77", "3.02", "3.32",
             "3.69", "4.13", "4.68", "5.38", "6.32", "7.64", "9.62", "12.9", "19.5",
             "39.2"
         },
         {
             "0.21", "0.25", "0.27", "0.30", "0.32", "0.34", "0.36", "0.38", "0.40",
             "0.42", "0.44", "0.46", "0.48", "0.51", "0.53", "0.55", "0.57", "0.60",
             "0.62", "0.65", "0.67", "0.70", "0.73", "0.76", "0.79", "0.82", "0.85",
             "0.89", "0.92", "0.96", "1.00", "1.05", "1.09", "1.14", "1.19", "1.24",
             "1.30", "1.37", "1.43", "1.51", "1.58", "1.67", "1.76", "1.86", "1.97",
             "2.10", "2.23", "2.38", "2.56", "2.75", "2.97", "3.22", "3.52", "3.87",
             "4.29", "4.81", "5.45", "6.27", "7.37", "8.91", "11.2", "15.0", "22.7",
             "45.8"
         },
         {},
         {
             "0.26", "0.31", "0.34", "0.37", "0.40", "0.43", "0.45", "0.48", "0.50",
             "0.53", "0.56", "0.58", "0.61", "0.64", "0.67", "0.69", "0.72", "0.75",
             "0.79", "0.82", "0.85", "0.89", "0.92", "0.96", "1.00", "1.04", "1.08",
             "1.13", "1.17", "1.22", "1.28", "1.33", "1.39", "1.45", "1.52", "1.59",
             "1.66", "1.74", "1.83", "1.92", "2.02", "2.13", "2.25", "2.38", "2.53",
             "2.68", "2.86", "3.05", "3.27", "3.52", "3.81", "4.13", "4.51", "4.97",
             "5.51", "6.17", "6.99", "8.05", "9.47", "11.4", "14.4", "19.3", "29.2",
             "58.9"
         },
         {
             "0.29", "0.34", "0.37", "0.41", "0.44", "0.47", "0.50", "0.53", "0.56",
             "0.59", "0.61", "0.64", "0.67", "0.70", "0.74", "0.77", "0.80", "0.83",
             "0.87", "0.90", "0.94", "0.98", "1.02", "1.06", "1.11", "1.15", "1.20",
             "1.25", "1.30", "1.36", "1.41", "1.47", "1.54", "1.61", "1.68", "1.76",
             "1.84", "1.93", "2.03", "2.13", "2.24", "2.37", "2.50", "2.64", "2.80",
             "2.98", "3.17", "3.39", "3.63", "3.91", "4.22", "4.59", "5.01", "5.51",
             "6.11", "6.85", "7.76", "8.94", "10.5", "12.7", "16.0", "21.5", "32.4",
             "65.4"
         }
     };
 
     private static final String[] REVERB_XOVR = {
         "30", "60", "90", "120", "151", "181", "212", "243", "273", "336", "398",
         "461", "525", "589", "654", "818", "986", "1.1k", "1.3k", "1.5k", "1.6k",
         "1.8k", "2.0k", "2.2k", "2.4k", "2.6k", "2.9k", "3.1k", "3.3k", "3.5k",
         "3.8k", "4.0k", "4.3k", "4.6k", "4.8k", "5.1k", "5.4k", "5.7k", "6.1k",
         "6.4k", "6.8k", "7.1k", "7.5k", "7.9k", "8.4k", "8.8k", "9.3k", "9.9k",
         "10.4k", "11.0k", "11.7k", "12.4k", "13.2k", "14.1k", "15.2k", "16.3k",
         "17.7k", "19.4k", "21.6k", "24.7k", "Full"
     };
 
     private static final String[] REVERB_RT_HC = {
         "525", "589", "654", "818", "986", "1.1k", "1.3k", "1.5k", "1.6k",
         "1.8k", "2.0k", "2.2k", "2.4k", "2.6k", "2.9k", "3.1k", "3.3k", "3.5k",
         "3.8k", "4.0k", "4.3k", "4.6k", "4.8k", "5.1k", "5.4k", "5.7k", "6.1k",
         "6.4k", "6.8k", "7.1k", "7.5k", "7.9k", "8.4k", "8.8k", "9.3k", "9.9k",
         "10.4k", "11.0k", "11.7k", "12.4k", "13.2k", "14.1k", "15.2k", "16.3k",
         "17.7k", "19.4k", "21.6k", "24.7k", "Flat"
     };
 
     private static final double[] REVERB_AMBIENCE_RT_HC = {
        0.5, 1.0, 1.6, 2.2, 2.9, 3.6, 4.4, 5.5, 6.3, 7.5, 8.9, 10.6, 12.8, 15.9, 21.2
     };
 
     private static final String[] REVERB_DELAY_LEVEL = {
         "Off", "-48dB", "-42db", "-39dB", "-36dB", "-33dB", "-30dB", "-27dB",
         "-24dB", "-22dB", "-20dB", "-18dB", "-16dB", "-14dB", "-12dB", "-10dB",
         "-9dB", "-8dB", "-7dB", "-6dB", "-5dB", "-4dB", "-3dB", "-2dB", "-1dB",
         "Full"
     };
 
     private static final String[] PHASES = {"0", "90", "180", "270"};
 
     private static final String[] EQ_MODES = {"LShlf", "Band", "HShlf"};
 
     /**
      * @return String representation of insert position.
      */
     public static String delayInsertToString(final int delayInsert) {
         return DELAY_INSERTS[delayInsert];
     }
 
     /**
      * @return String representation of delay time index.
      */
     public static double reverbDelayTimeToString(final int reverbDelayTime) {
         return REVERB_DELAY_TIMES[reverbDelayTime];
     }
 
     /**
      * @return String representation of reverb bass index.
      */
     public static String reverbBassToString(final int reverbBass) {
         return REVERB_BASS[reverbBass];
     }
 
     /**
      * @return String representation of reverb decay index.
      */
     public static String reverbDecayToString(final boolean link, final double size, final int reverbDecay) {
         int decayIndex;
         if (link) {
             decayIndex = 0;
             while (size > REVERB_LINK_SIZES[decayIndex] - 0.01) {
                 decayIndex++;
             }
         } else {
             decayIndex = REVERB_LINK_SIZES.length - 1;
         }
         return REVERB_DECAY[decayIndex][reverbDecay];
     }
 
     /**
      * @return String representation of reverb xovr index.
      */
     public static String reverbXovrToString(final int reverbXovr) {
         return REVERB_XOVR[reverbXovr];
     }
 
     /**
      * @return String representation of Rt HC index.
      */
     public static String reverbRtHCToString(final int reverbRtHC) {
         return REVERB_RT_HC[reverbRtHC];
     }
 
     /**
      * @return String representation of Rt HC index for Ambience algorithm.
      */
     public static double reverbAmbienceRtHCToString(final int reverbRtHC) {
         return REVERB_AMBIENCE_RT_HC[reverbRtHC];
     }
 
     /**
      * @return String representation of Delay Level index for Ambience algorithm.
      */
     public static String reverbDelayLevelToString(final int delayLevel) {
         return REVERB_DELAY_LEVEL[delayLevel];
     }
 
     /**
      * @return postive numbers prefixed with '+' and negative numbers prefixed with '-'.
      */
     public static String signInt(final int i) {
         int shifted = i > 32768 ? i - 65536 : i;
         return shifted > 0 ? "+" + Integer.toString(shifted) : Integer.toString(shifted);
     }
 
     /**
      * @return number with trailing 'L' or 'R', or just 'C' for zero.
      */
     public static String panToString(final int pan) {
         String suffix;
         if (pan < 0) {
             suffix = "L";
         } else if (pan > 0) {
             suffix = "R";
         } else {
             return "C";
         }
         return Integer.toString(Math.abs(pan)) + suffix;
     }
 
     /**
      * @return return phase postion indicated by phase
      */
     public static String phaseToString(final int phase) {
         return PHASES[phase] + '°';
     }
 
     /**
      * @return String 'On' or 'Off' depending on value of ind
      * @deprecated Should be a boolean value already
      */
     @Deprecated
     public static String onOffToString(final int ind) {
         return ind == 0 ? "Off" : "On";
     }
 
     /**
      * @return String 'On' or 'Off' depending on value of ind
      */
     public static String onOffToString(final boolean ind) {
         return ind ? "On" : "Off";
     }
 
     public static String wahTypeToString(final int wahType) {
         return "Model " + (wahType == 0 ? 'C' : 'V');
     }
 
     public static String eqModeToString(final int eqMode) {
         return EQ_MODES[eqMode];
     }
 }
