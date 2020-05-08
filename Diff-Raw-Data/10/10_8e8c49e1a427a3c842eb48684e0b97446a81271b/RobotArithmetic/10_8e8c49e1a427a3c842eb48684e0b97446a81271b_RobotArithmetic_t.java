 /* This class is where mathematical functions are stored. It is managed by Danial. 
  * 
  */
 package edu.ames.frc.robot;
 
 public class RobotArithmetic {
     public static float[][] arctantable = new float[180][2];
     
     /* Initialize variables, such as the table of values for arctangent */
    static void init() {
         arctantable[0][0] = (float) 0.0;                arctantable[0][1] = (float) 0.0;
         arctantable[1][0] = (float) 0.017454773;  	arctantable[1][1] = (float) 0.017453;
         arctantable[2][0] = (float) 0.034920182;  	arctantable[2][1] = (float) 0.034906;
         arctantable[3][0] = (float) 0.0524069;  	arctantable[3][1] = (float) 0.052359;
         arctantable[4][0] = (float) 0.069925636;  	arctantable[4][1] = (float) 0.069812;
         arctantable[5][0] = (float) 0.08748719;  	arctantable[5][1] = (float) 0.087265;
         arctantable[6][0] = (float) 0.10510246;  	arctantable[6][1] = (float) 0.104718;
         arctantable[7][0] = (float) 0.12278248;  	arctantable[7][1] = (float) 0.122171;
         arctantable[8][0] = (float) 0.14053845;  	arctantable[8][1] = (float) 0.139624;
         arctantable[9][0] = (float) 0.15838175;  	arctantable[9][1] = (float) 0.157077;
         arctantable[10][0] = (float) 0.17632397;  	arctantable[10][1] = (float) 0.17453;
         arctantable[11][0] = (float) 0.19437698;  	arctantable[11][1] = (float) 0.191983;
         arctantable[12][0] = (float) 0.21255289;  	arctantable[12][1] = (float) 0.209436;
         arctantable[13][0] = (float) 0.23086418;  	arctantable[13][1] = (float) 0.226889;
         arctantable[14][0] = (float) 0.24932365;  	arctantable[14][1] = (float) 0.244342;
         arctantable[15][0] = (float) 0.26794448;  	arctantable[15][1] = (float) 0.26179498;
         arctantable[16][0] = (float) 0.28674033;  	arctantable[16][1] = (float) 0.279248;
         arctantable[17][0] = (float) 0.30572525;  	arctantable[17][1] = (float) 0.296701;
         arctantable[18][0] = (float) 0.32491392;  	arctantable[18][1] = (float) 0.31415403;
         arctantable[19][0] = (float) 0.34432146;  	arctantable[19][1] = (float) 0.33160704;
         arctantable[20][0] = (float) 0.36396366;  	arctantable[20][1] = (float) 0.34906006;
         arctantable[21][0] = (float) 0.38385707;  	arctantable[21][1] = (float) 0.36651307;
         arctantable[22][0] = (float) 0.40401885;  	arctantable[22][1] = (float) 0.3839661;
         arctantable[23][0] = (float) 0.424467;  	arctantable[23][1] = (float) 0.4014191;
         arctantable[24][0] = (float) 0.4452204;  	arctantable[24][1] = (float) 0.41887212;
         arctantable[25][0] = (float) 0.4662989;  	arctantable[25][1] = (float) 0.43632513;
         arctantable[26][0] = (float) 0.48772335;  	arctantable[26][1] = (float) 0.45377815;
         arctantable[27][0] = (float) 0.5095157;  	arctantable[27][1] = (float) 0.47123116;
         arctantable[28][0] = (float) 0.5316992;  	arctantable[28][1] = (float) 0.48868418;
         arctantable[29][0] = (float) 0.5542982;  	arctantable[29][1] = (float) 0.5061372;
         arctantable[30][0] = (float) 0.5773389;  	arctantable[30][1] = (float) 0.5235902;
         arctantable[31][0] = (float) 0.60084856;  	arctantable[31][1] = (float) 0.5410432;
         arctantable[32][0] = (float) 0.62485665;  	arctantable[32][1] = (float) 0.55849624;
         arctantable[33][0] = (float) 0.6493942;  	arctantable[33][1] = (float) 0.57594925;
         arctantable[34][0] = (float) 0.67449445;  	arctantable[34][1] = (float) 0.59340227;
         arctantable[35][0] = (float) 0.7001927;  	arctantable[35][1] = (float) 0.6108553;
         arctantable[36][0] = (float) 0.7265269;  	arctantable[36][1] = (float) 0.6283083;
         arctantable[37][0] = (float) 0.7535376;  	arctantable[37][1] = (float) 0.6457613;
         arctantable[38][0] = (float) 0.78126824;  	arctantable[38][1] = (float) 0.6632143;
         arctantable[39][0] = (float) 0.8097657;  	arctantable[39][1] = (float) 0.68066734;
         arctantable[40][0] = (float) 0.8390803;  	arctantable[40][1] = (float) 0.69812036;
         arctantable[41][0] = (float) 0.86926633;  	arctantable[41][1] = (float) 0.7155734;
         arctantable[42][0] = (float) 0.9003825;  	arctantable[42][1] = (float) 0.7330264;
         arctantable[43][0] = (float) 0.9324923;  	arctantable[43][1] = (float) 0.7504794;
         arctantable[44][0] = (float) 0.9656647;  	arctantable[44][1] = (float) 0.7679324;
         arctantable[45][0] = (float) 0.99997455;  	arctantable[45][1] = (float) 0.78538543;
         arctantable[46][0] = (float) 1.0355034;  	arctantable[46][1] = (float) 0.80283844;
         arctantable[47][0] = (float) 1.0723401;  	arctantable[47][1] = (float) 0.82029146;
         arctantable[48][0] = (float) 1.1105822;  	arctantable[48][1] = (float) 0.8377445;
         arctantable[49][0] = (float) 1.1503363;  	arctantable[49][1] = (float) 0.8551975;
         arctantable[50][0] = (float) 1.1917194;  	arctantable[50][1] = (float) 0.8726505;
         arctantable[51][0] = (float) 1.2348608;  	arctantable[51][1] = (float) 0.8901035;
         arctantable[52][0] = (float) 1.2799029;  	arctantable[52][1] = (float) 0.90755653;
         arctantable[53][0] = (float) 1.3270035;  	arctantable[53][1] = (float) 0.92500955;
         arctantable[54][0] = (float) 1.3763379;  	arctantable[54][1] = (float) 0.94246256;
         arctantable[55][0] = (float) 1.4281008;  	arctantable[55][1] = (float) 0.9599156;
         arctantable[56][0] = (float) 1.4825104;  	arctantable[56][1] = (float) 0.9773686;
         arctantable[57][0] = (float) 1.5398108;  	arctantable[57][1] = (float) 0.9948216;
         arctantable[58][0] = (float) 1.6002764;  	arctantable[58][1] = (float) 1.0122746;
         arctantable[59][0] = (float) 1.6642166;  	arctantable[59][1] = (float) 1.0297276;
         arctantable[60][0] = (float) 1.7319827;  	arctantable[60][1] = (float) 1.0471805;
         arctantable[61][0] = (float) 1.8039739;  	arctantable[61][1] = (float) 1.0646335;
         arctantable[62][0] = (float) 1.8806462;  	arctantable[62][1] = (float) 1.0820864;
         arctantable[63][0] = (float) 1.962523;  	arctantable[63][1] = (float) 1.0995394;
         arctantable[64][0] = (float) 2.0502083;  	arctantable[64][1] = (float) 1.1169924;
         arctantable[65][0] = (float) 2.1444023;  	arctantable[65][1] = (float) 1.1344453;
         arctantable[66][0] = (float) 2.2459216;  	arctantable[66][1] = (float) 1.1518983;
         arctantable[67][0] = (float) 2.3557255;  	arctantable[67][1] = (float) 1.1693512;
         arctantable[68][0] = (float) 2.4749463;  	arctantable[68][1] = (float) 1.1868042;
         arctantable[69][0] = (float) 2.604933;  	arctantable[69][1] = (float) 1.2042571;
         arctantable[70][0] = (float) 2.747303;  	arctantable[70][1] = (float) 1.2217101;
         arctantable[71][0] = (float) 2.9040153;  	arctantable[71][1] = (float) 1.239163;
         arctantable[72][0] = (float) 3.077463;  	arctantable[72][1] = (float) 1.256616;
         arctantable[73][0] = (float) 3.2706022;  	arctantable[73][1] = (float) 1.274069;
         arctantable[74][0] = (float) 3.4871283;  	arctantable[74][1] = (float) 1.2915219;
         arctantable[75][0] = (float) 3.7317212;  	arctantable[75][1] = (float) 1.3089749;
         arctantable[76][0] = (float) 4.010398;  	arctantable[76][1] = (float) 1.3264278;
         arctantable[77][0] = (float) 4.331026;  	arctantable[77][1] = (float) 1.3438808;
         arctantable[78][0] = (float) 4.704096;  	arctantable[78][1] = (float) 1.3613337;
         arctantable[79][0] = (float) 5.143911;  	arctantable[79][1] = (float) 1.3787867;
         arctantable[80][0] = (float) 5.670494;  	arctantable[80][1] = (float) 1.3962396;
         arctantable[81][0] = (float) 6.312767;  	arctantable[81][1] = (float) 1.4136926;
         arctantable[82][0] = (float) 7.114108;  	arctantable[82][1] = (float) 1.4311455;
         arctantable[83][0] = (float) 8.142678;  	arctantable[83][1] = (float) 1.4485985;
         arctantable[84][0] = (float) 9.512067;  	arctantable[84][1] = (float) 1.4660515;
         arctantable[85][0] = (float) 11.4267025;  	arctantable[85][1] = (float) 1.4835044;
         arctantable[86][0] = (float) 14.295369;  	arctantable[86][1] = (float) 1.5009574;
         arctantable[87][0] = (float) 19.071604;  	arctantable[87][1] = (float) 1.5184103;
         arctantable[88][0] = (float) 28.614544;  	arctantable[88][1] = (float) 1.5358633;
         arctantable[89][0] = (float) 57.202114;  	arctantable[89][1] = (float) 1.5533162;
         arctantable[90][0] = (float) 36851.406;  	arctantable[90][1] = (float) 1.5707692;
         arctantable[91][0] = (float) -57.380302;  	arctantable[91][1] = (float) 1.5882221;
         arctantable[92][0] = (float) -28.659105;  	arctantable[92][1] = (float) 1.6056751;
         arctantable[93][0] = (float) -19.09142;  	arctantable[93][1] = (float) 1.623128;
         arctantable[94][0] = (float) -14.306522;  	arctantable[94][1] = (float) 1.640581;
         arctantable[95][0] = (float) -11.433848;  	arctantable[95][1] = (float) 1.658034;
         arctantable[96][0] = (float) -9.517034;  	arctantable[96][1] = (float) 1.6754869;
         arctantable[97][0] = (float) -8.146333;  	arctantable[97][1] = (float) 1.6929399;
         arctantable[98][0] = (float) -7.1169105;  	arctantable[98][1] = (float) 1.7103928;
         arctantable[99][0] = (float) -6.314985;  	arctantable[99][1] = (float) 1.7278458;
         arctantable[100][0] = (float) -5.6722937;  	arctantable[100][1] = (float) 1.7452987;
         arctantable[101][0] = (float) -5.1454015;  	arctantable[101][1] = (float) 1.7627517;
         arctantable[102][0] = (float) -4.705352;  	arctantable[102][1] = (float) 1.7802047;
         arctantable[103][0] = (float) -4.332099;  	arctantable[103][1] = (float) 1.7976576;
         arctantable[104][0] = (float) -4.0113254;  	arctantable[104][1] = (float) 1.8151106;
         arctantable[105][0] = (float) -3.7325315;  	arctantable[105][1] = (float) 1.8325635;
         arctantable[106][0] = (float) -3.4878428;  	arctantable[106][1] = (float) 1.8500165;
         arctantable[107][0] = (float) -3.2712371;  	arctantable[107][1] = (float) 1.8674694;
         arctantable[108][0] = (float) -3.0780313;  	arctantable[108][1] = (float) 1.8849224;
         arctantable[109][0] = (float) -2.9045274;  	arctantable[109][1] = (float) 1.9023753;
         arctantable[110][0] = (float) -2.747767;  	arctantable[110][1] = (float) 1.9198283;
         arctantable[111][0] = (float) -2.6053555;  	arctantable[111][1] = (float) 1.9372813;
         arctantable[112][0] = (float) -2.4753332;  	arctantable[112][1] = (float) 1.9547342;
         arctantable[113][0] = (float) -2.356081;  	arctantable[113][1] = (float) 1.9721872;
         arctantable[114][0] = (float) -2.2462497;  	arctantable[114][1] = (float) 1.9896401;
         arctantable[115][0] = (float) -2.1447053;  	arctantable[115][1] = (float) 2.0070932;
         arctantable[116][0] = (float) -2.0504901;  	arctantable[116][1] = (float) 2.0245461;
         arctantable[117][0] = (float) -1.9627858;  	arctantable[117][1] = (float) 2.041999;
         arctantable[118][0] = (float) -1.8808919;  	arctantable[118][1] = (float) 2.059452;
         arctantable[119][0] = (float) -1.8042043;  	arctantable[119][1] = (float) 2.076905;
         arctantable[120][0] = (float) -1.7321993;  	arctantable[120][1] = (float) 2.094358;
         arctantable[121][0] = (float) -1.6644207;  	arctantable[121][1] = (float) 2.111811;
         arctantable[122][0] = (float) -1.6004692;  	arctantable[122][1] = (float) 2.1292639;
         arctantable[123][0] = (float) -1.5399935;  	arctantable[123][1] = (float) 2.1467168;
         arctantable[124][0] = (float) -1.482684;  	arctantable[124][1] = (float) 2.1641698;
         arctantable[125][0] = (float) -1.428266;  	arctantable[125][1] = (float) 2.1816227;
         arctantable[126][0] = (float) -1.3764952;  	arctantable[126][1] = (float) 2.1990757;
         arctantable[127][0] = (float) -1.3271539;  	arctantable[127][1] = (float) 2.2165287;
         arctantable[128][0] = (float) -1.2800467;  	arctantable[128][1] = (float) 2.2339816;
         arctantable[129][0] = (float) -1.2349986;  	arctantable[129][1] = (float) 2.2514346;
         arctantable[130][0] = (float) -1.1918516;  	arctantable[130][1] = (float) 2.2688875;
         arctantable[131][0] = (float) -1.1504633;  	arctantable[131][1] = (float) 2.2863405;
         arctantable[132][0] = (float) -1.1107045;  	arctantable[132][1] = (float) 2.3037934;
         arctantable[133][0] = (float) -1.072458;  	arctantable[133][1] = (float) 2.3212464;
         arctantable[134][0] = (float) -1.0356171;  	arctantable[134][1] = (float) 2.3386993;
         arctantable[135][0] = (float) -1.0000844;   	arctantable[135][1] = (float) 2.3561523;
         arctantable[136][0] = (float) -0.96577096;  	arctantable[136][1] = (float) 2.3736053;
         arctantable[137][0] = (float) -0.93259525;  	arctantable[137][1] = (float) 2.3910582;
         arctantable[138][0] = (float) -0.9004823;   	arctantable[138][1] = (float) 2.4085112;
         arctantable[139][0] = (float) -0.8693632;   	arctantable[139][1] = (float) 2.425964;
         arctantable[140][0] = (float) -0.8391744;   	arctantable[140][1] = (float) 2.443417;
         arctantable[141][0] = (float) -0.80985725;  	arctantable[141][1] = (float) 2.46087;
         arctantable[142][0] = (float) -0.7813574;    	arctantable[142][1] = (float) 2.478323;
         arctantable[143][0] = (float) -0.75362444;  	arctantable[143][1] = (float) 2.495776;
         arctantable[144][0] = (float) -0.7266116;   	arctantable[144][1] = (float) 2.513229;
         arctantable[145][0] = (float) -0.7002754;     	arctantable[145][1] = (float) 2.5306818;
         arctantable[146][0] = (float) -0.6745753;  	arctantable[146][1] = (float) 2.5481348;
         arctantable[147][0] = (float) -0.6494733;   	arctantable[147][1] = (float) 2.5655878;
         arctantable[148][0] = (float) -0.62493414;  	arctantable[148][1] = (float) 2.5830407;
         arctantable[149][0] = (float) -0.6009245;   	arctantable[149][1] = (float) 2.6004937;
         arctantable[150][0] = (float) -0.57741326;  	arctantable[150][1] = (float) 2.6179466;
         arctantable[151][0] = (float) -0.55437124;  	arctantable[151][1] = (float) 2.6353996;
         arctantable[152][0] = (float) -0.5317709;   	arctantable[152][1] = (float) 2.6528525;
         arctantable[153][0] = (float) -0.5095863;   	arctantable[153][1] = (float) 2.6703055;
         arctantable[154][0] = (float) -0.48779276;  	arctantable[154][1] = (float) 2.6877584;
         arctantable[155][0] = (float) -0.46636724;  	arctantable[155][1] = (float) 2.7052114;
         arctantable[156][0] = (float) -0.44528773;  	arctantable[156][1] = (float) 2.7226644;
         arctantable[157][0] = (float) -0.42453337;  	arctantable[157][1] = (float) 2.7401173;
         arctantable[158][0] = (float) -0.40408432;  	arctantable[158][1] = (float) 2.7575703;
         arctantable[159][0] = (float) -0.38392174;  	arctantable[159][1] = (float) 2.7750232;
         arctantable[160][0] = (float) -0.36402756;  	arctantable[160][1] = (float) 2.7924762;
         arctantable[161][0] = (float) -0.3443846;   	arctantable[161][1] = (float) 2.8099291;
         arctantable[162][0] = (float) -0.3249764;   	arctantable[162][1] = (float) 2.827382;
         arctantable[163][0] = (float) -0.30578715;  	arctantable[163][1] = (float) 2.844835;
         arctantable[164][0] = (float) -0.28680164;  	arctantable[164][1] = (float) 2.862288;
         arctantable[165][0] = (float) -0.26800525;  	arctantable[165][1] = (float) 2.879741;
         arctantable[166][0] = (float) -0.24938393;  	arctantable[166][1] = (float) 2.897194;
         arctantable[167][0] = (float) -0.23092401;  	arctantable[167][1] = (float) 2.9146469;
         arctantable[168][0] = (float) -0.2126123;   	arctantable[168][1] = (float) 2.9320998;
         arctantable[169][0] = (float) -0.194436;    	arctantable[169][1] = (float) 2.9495528;
         arctantable[170][0] = (float) -0.17638266;  	arctantable[170][1] = (float) 2.9670057;
         arctantable[171][0] = (float) -0.15844014;  	arctantable[171][1] = (float) 2.9844587;
         arctantable[172][0] = (float) -0.14059658;  	arctantable[172][1] = (float) 3.0019116;
         arctantable[173][0] = (float) -0.122840405;  	arctantable[173][1] = (float) 3.0193646;
         arctantable[174][0] = (float) -0.10516019;  	arctantable[174][1] = (float) 3.0368176;
         arctantable[175][0] = (float) -0.08754478;  	arctantable[175][1] = (float) 3.0542705;
         arctantable[176][0] = (float) -0.06998311;  	arctantable[176][1] = (float) 3.0717235;
         arctantable[177][0] = (float) -0.052464295;  	arctantable[177][1] = (float) 3.0891764;
         arctantable[178][0] = (float) -0.034977537;  	arctantable[178][1] = (float) 3.1066294;
         arctantable[179][0] = (float) -0.017512117;  	arctantable[179][1] = (float) 3.1240823;
     }
     
     /* Get the arctangent (angle) of a certain ratio. We'll use a table of values, 
      * and look up an approximate number
      */
     static double arcTangent(double x, double y) {
         double in = y / x;
         double out = 0;
        double nearest = 10000; // smallest difference for inexact values
         int position = -1;  // array position of closest variable
         
         for(int i=0;i<arctantable.length;i++) {
             if(arctantable[i][0] == in) { // unlikely, but could happen
                 out = arctantable[i][1];
             } else {
                double tmp = in - arctantable[i][0]; // temporary variable for comparing
                if(Math.abs(tmp) < Math.abs(nearest)) {
                     nearest = tmp;
                     position = i;
                 }
             }
         }
         
         // now return the closest value we've found
         if(position != -1) {
             out = arctantable[position][1];
         }
         
         // negative y - we have to add pi to our result for a proper angle
         if(y < 0) {
             out = out + Math.PI;
         }        
         return out;
     }
 }
