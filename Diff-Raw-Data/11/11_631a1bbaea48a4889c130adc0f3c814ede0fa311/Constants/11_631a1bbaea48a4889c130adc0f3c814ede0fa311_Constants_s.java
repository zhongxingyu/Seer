 /*
 Constants.java
 
 Description:
    A static object used as a common place to store the 
    various BARREL CDF Generator constants.
 
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    This file is part of The BARREL CDF Generator.
 
    The BARREL CDF Generator is free software: you can redistribute it and/or 
    modify it under the terms of the GNU General Public License as published 
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    The BARREL CDF Generator is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License along with 
    The BARREL CDF Generator.  If not, see <http://www.gnu.org/licenses/>.
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
 
 package edu.ucsc.barrel.cdf_gen;
 
 public class Constants{
    
    //Quality flags
    static public final int 
       FC_ROLL = 1, //The frame counter has gone beyone 2^21 and started over
       NO_GPS = 2, //There is no GPS signal present so timing info may be off
       FILL_TIME = 4, //This data point was not used to create a timing model
       PART_SPEC = 8, //The spectrum contains fill values 
       LOW_ALT = 16, //Indicator that the payload has dropped below MIN_SCI_ALT
       OUT_OF_RANGE = 32; //The rare case that the DPU returns unacceptable data
 
    //Index references for various data storage arrays 
    static public final int 
       //gps index
       ALT_I = 0, TIME_I = 1, LAT_I = 2, LON_I = 3, 
       //housekeeping index
       V0 = 0, I0 = 1, V1 = 2, I1 = 3, V2 = 4, I2 = 5, V3 = 6, I3 = 7, V4 = 8, 
       I4 = 9, V5 = 10, I5 = 11, V6 = 12, I6 = 13, V7 = 14, I7 = 15, T0 = 16, 
       T8 = 17, T1 = 18, T9 = 19, T2 = 20, T10 = 21, T3 = 22, T11 = 23, T4 = 24, 
       T12 = 25, T5 = 26, T13 = 27, T6 = 28, T14 = 29, T7 = 30, T15 = 31, 
       V8 = 32, V9 = 33, V10 = 34, V11 = 35, 
       SATSOFF = 36, WEEK = 37, CMDCNT = 38, MDMCNT = 39,
       //rate counter index
       INTER = 0, LL = 1, PD = 2, HL = 3;
 
    //fill values for vaious CDF variable types
    static public final int 
       UINT1_FILL = 255,
       UINT2_FILL = 65535,
       INT1_FILL = -128,
       INT2_FILL = -32768,
       INT4_FILL = -2147483648;
    static public final long
       UINT4_FILL = 4294967295L;
    static public final float 
       FLOAT_FILL = -1.0e+31f;
    static public final double 
       DOUBLE_FILL = -1.0e+31;
 
    //variable specific constants
    public static final short
       PAYID_MIN = 0, PAYID_MAX = 63, PAYID_FILL = INT2_FILL,
       VER_MIN = 0, VER_MAX = 31, VER_FILL = INT2_FILL,
       SATS_MIN = 0, SATS_MAX = 255, SATS_FILL = INT2_FILL,
       LEAP_SEC_MIN = 0, LEAP_SEC_MAX = 255, LEAP_SEC_FILL = INT2_FILL,
       TERM_STAT_MIN = 0, TERM_STAT_MAX = 1, TERM_STAT_FILL = INT2_FILL,
       MODEM_CNT_MIN = 0, MODEM_CNT_MAX = 255, MODEM_CNT_FILL = INT2_FILL,
       DCD_CNT_MIN = 0, DCD_CNT_MAX = 255, DCD_CNT_FILL = INT2_FILL;
    public static final int
       MS_WEEK_MIN = 0, MS_WEEK_MAX = 604800000, MS_WEEK_FILL = INT4_FILL,
       WEEKS_MIN = 1200, WEEKS_MAX = 1880, WEEKS_FILL = INT4_FILL,
       CMD_CNT_MIN = 0, CMD_CNT_MAX = 32767, CMD_CNT_FILL = INT4_FILL,
       MAG_MIN = 0, MAG_MAX = 16777215, MAG_FILL = INT4_FILL,
       SSPC_RAW_MIN = 0, SSPC_RAW_MAX = 65535, SSPC_RAW_FILL = INT4_FILL,
       MSPC_RAW_MIN = 0, MSPC_RAW_MAX = 65535, MSPC_RAW_FILL = INT4_FILL,
       FSPC_RAW_MIN = 0, FSPC_RAW_MAX = 65535, FSPC_RAW_FILL = INT4_FILL,
       LAT_RAW_MIN = -1073741824, LAT_RAW_MAX = 1073741824, 
          LAT_RAW_FILL = INT4_FILL,
       LON_RAW_MIN = -2147483647, LON_RAW_MAX = 2147483647, 
          LON_RAW_FILL = INT4_FILL,
       ALT_RAW_MIN = 0, ALT_RAW_MAX = 50000000, ALT_RAW_FILL = INT4_FILL,
       PPS_MIN = 0, PPS_MAX = 1000, PPS_FILL = INT4_FILL,
       FC_MIN = 0, FC_MAX = 2097151, FC_FILL = UINT2_FILL;
    public static final long
       HKPG_MIN = 0, HKPG_MAX = 65535, HKPG_FILL = INT4_FILL,
       RCNT_MIN = 0, RCNT_MAX = 65535, RCNT_FILL = INT4_FILL;
    public static final float 
       LAT_MIN = -90, LAT_MAX = 90, LAT_FILL = FLOAT_FILL,
       LON_MIN = -180, LON_MAX = 180, LON_FILL = FLOAT_FILL,
       ALT_MIN = 5, ALT_MAX = 50, ALT_FILL = FLOAT_FILL;
 
    //if the fc is greater than this, it may roll over today
    public static final int 
       LAST_DAY_FC = 2010752,
       FC_OFFSET = 2097152;
 
    //minimum altitude at which we can expect good science (in mm)
   public static final int =  MIN_SCI_ALT = 20000000;
 
    //various accumulation time offsets based on nominal ms/frame
    public static final long 
       SSPC_ACCUM = 31968000000L,
       QUAD_ACCUM = 3996000000L,
       SING_ACCUM = 999000000L;
 }
