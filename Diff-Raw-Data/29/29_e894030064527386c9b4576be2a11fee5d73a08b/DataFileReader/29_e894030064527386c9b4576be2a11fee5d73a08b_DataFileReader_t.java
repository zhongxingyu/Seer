 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package madsdf.ardrone.utils;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.Lists;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.commons.lang3.builder.EqualsBuilder;
 
 /**
  * Reader for file in the format
  * COMMAND 1 SAMPLE 1
  * Accel X : 2803;2815;2797;2776;2793;2771;2804;2790;2797;2792;2820;2834;2823;2823;2836;2883;2952;3023;3188;3426;3653;3877;4069;4068;4067;4067;4066;4066;4063;3928;3570;3153;2832;2655;2523;2455;2327;2159;2042;1851;1822;1747;1699;1650;1559;1448;1314;1116;910;572;17;19;18;19;21;194;63;26;26;137;540;1035;1281;1291;1178;1052;998;1114;1217;1382;1603;1688;1746;1850;1972;2050;2139;2311;2532;2543;2537;2369;2236;2299;2615;3113;3537;3799;3722;3412;3158;3091;3146;3525;3876;4065;4068;3877;3751;3687
  * Accel Y : 2311;2316;2291;2294;2325;2306;2300;2320;2335;2294;2299;2310;2317;2284;2303;2304;2289;2239;2209;2192;2217;2371;2457;2577;2685;2873;3043;3163;3279;3206;3033;2842;2699;2584;2569;2538;2618;2660;2712;2676;2644;2576;2567;2621;2643;2511;2523;2402;2378;2304;2216;2104;1803;2163;1879;1573;1545;1557;1570;1689;1842;1882;1798;1681;1636;1633;1714;1719;1779;1879;2085;2265;2399;2471;2470;2431;2418;2472;2677;2840;2963;3072;3140;3221;3261;3281;3333;3369;3438;3457;3363;3218;3012;2742;2536;2386;2281;2407;2564;2599
  * Accel Z : 1691;1680;1697;1722;1704;1705;1689;1689;1712;1707;1719;1691;1665;1704;1690;1655;1566;1420;1234;1148;1178;1182;1086;1025;1060;1210;1293;1365;1440;1540;1583;1607;1699;1688;1780;1791;1762;1767;1713;1655;1606;1418;1433;1461;1387;1294;1202;1199;1209;1273;1324;1377;968;875;1255;1543;1231;1116;1282;1546;1891;2077;2001;1891;1817;1697;1542;1394;1281;1424;1676;1839;1901;1881;1751;1499;1333;1385;1737;1783;1877;1820;1720;1656;1355;1356;1452;1726;1902;1763;1740;1534;1222;1256;1235;1181;1379;1685;1824;1812
  * Gyro X : 1832;1834;1842;1844;1845;1839;1832;1824;1824;1823;1823;1829;1834;1836;1822;1795;1758;1707;1641;1591;1567;1531;1521;1515;1561;1688;1867;2071;2281;2461;2601;2644;2630;2570;2542;2532;2551;2555;2568;2539;2493;2443;2368;2325;2327;2303;2310;2254;2205;2195;2228;2392;2555;2342;2092;1971;1788;1304;945;767;756;1026;1361;1621;1778;1792;1652;1477;1309;1127;1075;1129;1186;1221;1257;1320;1322;1378;1475;1573;1518;1457;1325;1215;1294;1472;1783;2125;2351;2319;2112;1817;1520;1347;1364;1515;1693;1815;1867;1863
  * Gyro Y : 1661;1661;1661;1664;1662;1660;1659;1658;1658;1658;1657;1660;1655;1656;1659;1663;1668;1694;1733;1776;1864;1934;2001;2049;2099;2129;2141;2139;2112;2057;1986;1924;1871;1822;1773;1730;1694;1650;1601;1562;1531;1506;1510;1505;1485;1480;1490;1501;1530;1542;1568;1559;1532;1629;1693;1782;1811;1846;1849;1825;1789;1738;1700;1691;1716;1750;1779;1811;1852;1883;1877;1834;1785;1742;1699;1678;1686;1717;1709;1649;1613;1571;1518;1445;1367;1315;1287;1280;1289;1278;1271;1268;1279;1330;1413;1507;1623;1713;1748;1751
  * Gyro Z : 1840;1840;1843;1846;1848;1848;1848;1850;1851;1852;1852;1855;1854;1857;1854;1842;1820;1784;1721;1633;1510;1364;1215;1070;932;817;720;642;579;535;507;490;482;478;478;483;492;498;508;526;538;548;552;573;596;622;642;665;705;771;878;1053;1320;1615;1931;2156;2296;2320;2362;2436;2522;2596;2658;2710;2758;2795;2817;2838;2863;2894;2929;2958;2985;3013;3030;3047;3061;3094;3125;3129;3114;3097;3088;3083;3097;3104;3111;3098;3039;2952;2826;2697;2564;2440;2318;2203;2104;2029;1967;1917
  * COMMAND 1 SAMPLE 2
  * ...
  */
 public class DataFileReader {    
     private static final Pattern COMMAND_REGEXP = Pattern.compile(
            "^\\s*COMMAND\\s(\\d+)\\sSAMPLE\\s(\\d+).*$");
     
     public static class Sample {
         public final int command;
         public final int sample;
         public final float[][] accel;
         public final float[][] gyro;
         public Sample(int cmd, int sam, float[][] accel, float[][] gyro) {
             this.command = cmd;
             this.sample = sam;
             this.accel = accel;
             this.gyro = gyro;
         }
         
         @Override
         public String toString() {
             return "Sample [command="+command+", sample="+sample+
                     ", accel.length="+accel.length+", gyro.length="+gyro.length
                     +"]";
         }
         
         @Override
         public int hashCode() {
             return Objects.hashCode(command, sample, accel, gyro);
         }
         
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final Sample other = (Sample) obj;
             return Objects.equal(other.command, command) &&
                    Objects.equal(other.sample, sample) &&
                    Arrays.deepEquals(other.accel, accel) &&
                    Arrays.deepEquals(other.gyro, gyro);
         }
     }
     
     private final BufferedReader reader;
     
     public DataFileReader(Reader input) {
         reader = new BufferedReader(input);
     }
     
     public List<Sample> readAll() throws IOException {
         List<Sample> samples = Lists.newArrayList();
         String line = reader.readLine();
         while (line != null) {
             System.out.println("line : " + line);
             final Matcher cmdMatcher = COMMAND_REGEXP.matcher(line);
             cmdMatcher.matches();
             final int cmdNum = Integer.parseInt(cmdMatcher.group(1));
             final int sampleNum = Integer.parseInt(cmdMatcher.group(2));
             final float[] accX = readFloatArray("Accel X : ");
             final float[] accY = readFloatArray("Accel Y : ");
             final float[] accZ = readFloatArray("Accel Z : ");
             final float[] gyroX = readFloatArray("Gyro X : ");
             final float[] gyroY = readFloatArray("Gyro Y : ");
             final float[] gyroZ = readFloatArray("Gyro Z : ");
             samples.add(new Sample(cmdNum, sampleNum,
                                    new float[][]{accX, accY, accZ},
                                    new float[][]{gyroX, gyroY, gyroZ}));
             line = reader.readLine();
         }
         return samples;
     }
     
     // Function that reads an "Accel X" like line
     private float[] readFloatArray(String expectedPrefix) throws IOException {
         String line = readLineOrThrow();
         if (!line.startsWith(expectedPrefix)) {
             throw new RuntimeException("Expected prefix : " + expectedPrefix +
                     " in line : " + line);
         }
         String[] vals = line.substring(expectedPrefix.length()).trim().split(";");
         float[] fvals = new float[vals.length];
         for (int i = 0; i < fvals.length; ++i) {
             fvals[i] = Float.parseFloat(vals[i]);
         }
         return fvals;
     }
     
     private String readLineOrThrow() throws IOException {
         String line = reader.readLine();
         if (line == null) {
             throw new RuntimeException("No more lines to read");
         }
         return line;
     }
 }
