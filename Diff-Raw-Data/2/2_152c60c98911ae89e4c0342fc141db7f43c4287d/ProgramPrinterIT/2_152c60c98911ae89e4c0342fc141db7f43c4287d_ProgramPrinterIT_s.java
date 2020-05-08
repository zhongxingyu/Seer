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
 
 import info.carlwithak.mpxg2.model.Program;
 import info.carlwithak.mpxg2.sysex.SysexParser;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  *
  * @author Carl Green
  */
 public class ProgramPrinterIT {
 
     @Test
     public void testPrintG2Blue() throws Exception {
         testParseAndPrint("001_G2_Blue");
     }
 
     @Test
     public void testPrintGuitarSolo() throws Exception {
         testParseAndPrint("002_Guitar_Solo");
     }
 
     @Test
     public void testPrintCordovox() throws Exception {
         testParseAndPrint("003_Cordovox");
     }
 
     @Test
     public void testPrintPowerChords() throws Exception {
         testParseAndPrint("004_Power_Chords");
     }
 
     @Test
     public void testPrintVybeFlange() throws Exception {
         testParseAndPrint("005_Vybe_Flange");
     }
 
     @Test
     public void testPrintAnotherBrick() throws Exception {
         testParseAndPrint("006_AnotherBrick");
     }
 
     @Test
     public void testPrintEnvFilterLP() throws Exception {
         testParseAndPrint("007_EnvFilter_LP");
     }
 
     @Test
     public void testPrintTremoWah() throws Exception {
         testParseAndPrint("008_TremoWah");
     }
 
     @Test
     public void testPrintJamMan() throws Exception {
         testParseAndPrint("009_JamMan");
     }
 
     @Test
     public void testPrintVHRig() throws Exception {
         testParseAndPrint("010_VH_Rig");
     }
 
     @Test
     public void testPrintRotaryCab() throws Exception {
         testParseAndPrint("011_Rotary_Cab");
     }
 
     @Test
     public void testPrintLittleWing() throws Exception {
         testParseAndPrint("012_Little_Wing");
     }
 
     @Test
     public void testPrintTechnoChords() throws Exception {
         testParseAndPrint("013_TechnoChords");
     }
 
     @Test
     public void testPrintPedalSwell() throws Exception {
         testParseAndPrint("014_Pedal_Swell");
     }
 
     @Test
     public void testPrintSlideComp() throws Exception {
         testParseAndPrint("015_Slide_Comp");
     }
 
     @Test
     public void testPrintKissTheSky() throws Exception {
         testParseAndPrint("016_Kiss_the_Sky");
     }
 
     @Test
     public void testPrintUnchained() throws Exception {
         testParseAndPrint("017_Unchained");
     }
 
     @Test
     public void testPrintStomp() throws Exception {
         testParseAndPrint("018_Stomp!");
     }
 
     @Test
     public void testPrintOctaWah() throws Exception {
         testParseAndPrint("019_OctaWah");
     }
 
     @Test
     public void testPrintWahUni() throws Exception {
         testParseAndPrint("020_Wah_&_Uni");
     }
 
     @Test
     public void testPrintToeWahFlng() throws Exception {
         testParseAndPrint("021_ToeWah_Flng");
     }
 
     @Test
     public void testPrintToeWahPhas() throws Exception {
         testParseAndPrint("022_ToeWah_Phas");
     }
 
     @Test
     public void testPrintToeWahChrs() throws Exception {
         testParseAndPrint("023_ToeWah_Chrs");
     }
 
     @Test
     public void testPrintToeWahAero() throws Exception {
         testParseAndPrint("024_ToeWah_Aero");
     }
 
     @Test
     public void testPrintToeWahUni() throws Exception {
         testParseAndPrint("025_ToeWah_Uni");
     }
 
     @Test
     public void testPrintWahFlange() throws Exception {
         testParseAndPrint("026_Wah_&_Flange");
     }
 
     @Test
     public void testPrintWahPhaser() throws Exception {
         testParseAndPrint("027_Wah_&_Phaser");
     }
 
     @Test
     public void testPrintWahChorus() throws Exception {
         testParseAndPrint("028_Wah_&_Chorus");
     }
 
     @Test
     public void testPrintWahAero() throws Exception {
         testParseAndPrint("029_Wah_&_Aero");
     }
 
     @Test
     public void testPrintChrsDlyRvb() throws Exception {
         testParseAndPrint("030_ChrsDlyRvb+");
     }
 
     @Test
     public void testPrintTSChorus() throws Exception {
         testParseAndPrint("031_TS_Chorus+");
     }
 
     @Test
     public void testPrintTSDelay() throws Exception {
         testParseAndPrint("032_TS_Delay+");
     }
 
     @Test
     public void testPrintTSChrsDly() throws Exception {
         testParseAndPrint("033_TS_ChrsDly+");
     }
 
     @Test
     public void testPrintTSReverb() throws Exception {
         testParseAndPrint("034_TS_Reverb+");
     }
 
     @Test
     public void testPrintTSChrsRVB() throws Exception {
         testParseAndPrint("035_TS_ChrsRvb+");
     }
 
     @Test
     public void testPrintCompChorus() throws Exception {
         testParseAndPrint("036_CompChorus+");
     }
 
     @Test
     public void testPrintCompDelay() throws Exception {
         testParseAndPrint("037_CompDelay+");
     }
 
     @Test
     public void testPrintCompChrsDly() throws Exception {
         testParseAndPrint("038_CompChrsDly+");
     }
 
     @Test
     public void testPrintCompChrsRvb() throws Exception {
         testParseAndPrint("039_CompChrsRvb+");
     }
 
     @Test
     public void testPrintPitchCascade() throws Exception {
         testParseAndPrint("040_PitchCascade");
     }
 
     @Test
     public void testPrintPdlOctaves() throws Exception {
         testParseAndPrint("041_Pdl_Octaves");
     }
 
     @Test
     public void testPrintPdl2nds() throws Exception {
         testParseAndPrint("042_Pdl_2nds");
     }
 
     @Test
     public void testPrintPdl23_b33() throws Exception {
         testParseAndPrint("043_Pdl_2-3_b3-3");
     }
 
     @Test
     public void testPrintPdl23_34() throws Exception {
         testParseAndPrint("044_Pdl_2-3_3-4");
     }
 
     @Test
     public void testPrintPdl45_56() throws Exception {
         testParseAndPrint("045_Pdl_4-5_5-6");
     }
 
     @Test
     public void testPrintOctaves() throws Exception {
         testParseAndPrint("046_Octaves");
     }
 
     @Test
     public void testPrint4ths5ths() throws Exception {
         testParseAndPrint("047_4ths_&_5ths");
     }
 
     @Test
     public void testPrintEMajMin3() throws Exception {
         testParseAndPrint("048_E_Maj_Min_3");
     }
 
     @Test
     public void testPrintEDorMix3() throws Exception {
         testParseAndPrint("049_E_Dor_Mix_3");
     }
 
     @Test
     public void testPrintDetuneTrem() throws Exception {
         testParseAndPrint("050_Detune+Trem");
     }
 
     @Test
     public void testPrintSquareTrem() throws Exception {
         testParseAndPrint("051_Square_Trem");
     }
 
     @Test
     public void testPrintTremAutoWah() throws Exception {
         testParseAndPrint("052_Trem_AutoWah");
     }
 
     @Test
     public void testPrintEnvTrem() throws Exception {
         testParseAndPrint("053_Env_Trem");
     }
 
     @Test
     public void testPrintEnvAutoWahs() throws Exception {
         testParseAndPrint("054_Env_AutoWahs");
     }
 
     @Test
     public void testPrintChaosDance() throws Exception {
         testParseAndPrint("055_Chaos_Dance");
     }
 
     @Test
     public void testPrintRoundTrem() throws Exception {
         testParseAndPrint("056_Round_Trem");
     }
 
     @Test
     public void testPrintTapAutoWah() throws Exception {
         testParseAndPrint("057_Tap_AutoWah");
     }
 
     @Test
     public void testPrintVerbolo() throws Exception {
         testParseAndPrint("058_Verbolo");
     }
 
     @Test
     public void testPrintDynaChrsTrem() throws Exception {
         testParseAndPrint("059_DynaChrsTrem");
     }
 
     @Test
     public void testPrintUnivybe() throws Exception {
         testParseAndPrint("060_Univybe");
     }
 
     @Test
     public void testPrintOctaveFuzz() throws Exception {
         testParseAndPrint("061_Octave_Fuzz");
     }
 
     @Test
     public void testPrintPhaser() throws Exception {
         testParseAndPrint("062_Phaser");
     }
 
     @Test
     public void testPrintEnvFilter() throws Exception {
         testParseAndPrint("063_EnvFilter");
     }
 
     @Test
     public void testPrintCWah() throws Exception {
         testParseAndPrint("064_C-Wah");
     }
 
     @Test
     public void testPrintBlueComp() throws Exception {
         testParseAndPrint("065_Blue_Comp");
     }
 
     @Test
     public void testPrintVintageTrem() throws Exception {
         testParseAndPrint("066_Vintage_Trem");
     }
 
     @Test
     public void testPrintIPSTapeSlap() throws Exception {
         testParseAndPrint("067_IPS_TapeSlap");
     }
 
     @Test
     public void testPrintSpaceEcho() throws Exception {
         testParseAndPrint("068_Space_Echo");
     }
 
     @Test
     public void testPrintOctabuzz() throws Exception {
         testParseAndPrint("069_Octabuzz");
     }
 
     @Test
     public void testPrintOrangePhase() throws Exception {
         testParseAndPrint("070_OrangePhase");
     }
 
     @Test
     public void testPrintGrayFlange() throws Exception {
         testParseAndPrint("071_Gray_Flange");
     }
 
     @Test
     public void testPrintRedComp() throws Exception {
         testParseAndPrint("072_Red_Comp");
     }
 
     @Test
     public void testPrintSHPedal() throws Exception {
         testParseAndPrint("073_S_H_Pedal");
     }
 
     @Test
     public void testPrintVWah() throws Exception {
         testParseAndPrint("074_V-Wah");
     }
 
     @Test
     public void testPrintModernTrem() throws Exception {
         testParseAndPrint("075_Modern_Trem");
     }
 
     @Test
     public void testPrintTapEcho() throws Exception {
         testParseAndPrint("076_Tap_Echo");
     }
 
     @Test
     public void testPrintEnvWah() throws Exception {
         testParseAndPrint("077_Env_Wah");
     }
 
     @Test
     public void testPrintStereoChorus() throws Exception {
         testParseAndPrint("078_StereoChorus");
     }
 
     @Test
     public void testPrintClasscDetune() throws Exception {
         testParseAndPrint("079_ClasscDetune");
     }
 
     @Test
     public void testPrintToneBoost() throws Exception {
         testParseAndPrint("080_Tone_Boost");
     }
 
     @Test
     public void testPrintCrunchBoost() throws Exception {
         testParseAndPrint("081_Crunch_Boost");
     }
 
     @Test
     public void testPrintTSLead() throws Exception {
         testParseAndPrint("082_TS_Lead");
     }
 
     @Test
     public void testPrintTSBoost() throws Exception {
         testParseAndPrint("083_TS_Boost");
     }
 
     @Test
     public void testPrintODLead() throws Exception {
         testParseAndPrint("084_OD_Lead");
     }
 
     @Test
     public void testPrintODBoost() throws Exception {
         testParseAndPrint("085_OD_Boost");
     }
 
     @Test
     public void testPrintDistLead() throws Exception {
         testParseAndPrint("086_Dist_Lead");
     }
 
     @Test
     public void testPrintDistBoost() throws Exception {
         testParseAndPrint("087_Dist_Boost");
     }
 
     @Test
     public void testPrintFuzz1() throws Exception {
         testParseAndPrint("088_Fuzz_1");
     }
 
     @Test
     public void testPrintFuzz2() throws Exception {
         testParseAndPrint("089_Fuzz_2");
     }
 
     @Test
     public void testPrintJamChrs() throws Exception {
         testParseAndPrint("090_Jam_Chrs+");
     }
 
     @Test
     public void testPrintJam1Uni() throws Exception {
         testParseAndPrint("091_Jam_1__Uni+");
     }
 
     @Test
     public void testPrintJam1SH() throws Exception {
         testParseAndPrint("092_Jam_1_S&H+");
     }
 
     @Test
     public void testPrintJam1Env() throws Exception {
         testParseAndPrint("093_Jam_1_Env+");
     }
 
     @Test
     public void testPrintJam1Cordovox() throws Exception {
         testParseAndPrint("094_Jam1Cordovox");
     }
 
     @Test
     public void testPrintJam2Flang() throws Exception {
         testParseAndPrint("095_Jam_2_Flange");
     }
 
     @Test
     public void testPrintJam2Phase() throws Exception {
         testParseAndPrint("096_Jam_2_Phase");
     }
 
     @Test
     public void testPrintJam2Pitch() throws Exception {
         testParseAndPrint("097_Jam_2_Pitch+");
     }
 
     @Test
     public void testPrintJam2Trem() throws Exception {
         testParseAndPrint("098_Jam_2_Trem");
     }
 
     @Test
    public void testPrintJam2AuoWah() throws Exception {
         testParseAndPrint("099_Jam2AutoWah");
     }
 
     private void testParseAndPrint(String filename) throws Exception {
         File expectedFile = new File(this.getClass().getClassLoader().getResource(filename + ".txt").toURI());
         String expected = readFile(expectedFile);
         File preset = new File(this.getClass().getClassLoader().getResource(filename + ".syx").toURI());
         Program program = SysexParser.parsePrograms(preset).get(0);
         String actual = ProgramPrinter.print(program);
         assertEquals(expected, actual);
     }
 
     private static String readFile(final File file) throws FileNotFoundException {
         return new Scanner(file).useDelimiter("\\Z").next();
     }
 }
