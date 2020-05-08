 import java.awt.*;
 import java.io.*;
 import java.lang.*;
 import java.nio.*;
 import java.util.*;
 import javax.swing.*;
 
 import edu.mines.jtk.awt.*;
 import edu.mines.jtk.dsp.*;
 import edu.mines.jtk.interp.*;
 import edu.mines.jtk.io.*;
 import edu.mines.jtk.mosaic.*;
 import edu.mines.jtk.ogl.Gl.*;
 import edu.mines.jtk.util.*;
 import static edu.mines.jtk.util.ArrayMath.*;
 
 public class Segd{
 
   public static void main(String[] args){
     Segd s = new Segd();
     s.readLine141Segd();
     
   }
 
   public Segd(){
     
   }
 
   public void readLine141Segd(){
     try{
     File[] segdList = (new File(segdDir)).listFiles();
     //File[] segdList = new File[1];
     //segdList[0] = new File("/gpfc/ckohnke/fc2013/segd/141/00000001.00000293.segd");
     int nshot = segdList.length; 
     float[][][] g = ArrayMath.zerofloat(s1.getCount(), s2.getCount(), s3.getCount());
     for(int i=3; i<segdList.length; ++i){
       System.out.println(segdList[i].getName());
       readSegd(segdList[i]);
 
     }
     }catch(IOException e){
       System.out.println(e);
     }
   }
 
   public void readSegd(File segdFile) throws IOException{
     int n1 = 4001; // #samples (5 sweeps) 
     int n2 = 342; // #traces (5 sweeps)
     byte[] gh = zerobyte(32); // general header
     byte[] th = zerobyte(20); // trace header
     byte[] the = zerobyte(32); // trace header extension
     byte[] csh = zerobyte(32); // channel set header
     ArrayInputStream ais = new ArrayInputStream(segdFile,ByteOrder.BIG_ENDIAN);
     ais.readBytes(gh);
     int fn = bcd2(gh,0);
     ais.readBytes(gh);
     ais.readBytes(gh);
     sln = bin5(gh,3);
    //System.out.println("gh[3-7] = "+gh[3]+" "+gh[4]+" "+gh[5]+" "+gh[6]+" "+gh[7] );
     spn = bin5(gh,8);
     System.out.println("fn=" + fn + ", sln=" + sln + ", spn=" + spn);
     int cns = 0;
     int nct = 0;
     int cn;
     f = zerofloat(n1,n2);
     for(int i=0; i<16; ++i){
       ais.readBytes(csh);
       cn = csh[1];
       int ct = (csh[10]>>4)&0xf;
       int nc = bcd2(csh,8);
      //System.out.println("csh[8] = "+csh[8]+" csh[9] = "+csh[9]+" nc = "+nc);
       if(nc>0){
         System.out.println("cn =" + cn + " nc =" + nc + " ct =" + ct);
         if(ct==1){ // if seismic
           cns = cn;
           int ncs = nc;
         }
       nct += nc;
       }
     }
     System.out.println("nct =" + nct + " cns =" + cns);
     ais.skipBytes(1024); // skip extended header
     ais.skipBytes(1024); // skip external header
     rpf = 1;
     rpl = 1;
     for(int j=0; j<nct-1; ++j){
       ais.readBytes(th);
       cn = th[3];
       int ic = bcd2(th,4);
       ais.readBytes(the);
       int rln = bin3(the,0);
       int rpn = bin3(the,3);
       n1 = bin3(the,7);
       System.out.println("ic =" + ic + " rln =" + rln + " rpn =" + rpn + " n1 =" + n1);
       if(ic==1){
         rpf = rpn;
       } else if(ic == n2){
         rpl = rpn;
       }
       ais.skipBytes(6*the.length);
       if(cn==cns){
         System.out.println("ic =" + ic + " rln =" + rln + " rpn =" + rpn);
         ais.readFloats(f[ic-1]); // ic-1
       } else{
         ais.skipBytes(4*n1);
       }
     }
     ais.close();
     f = mul((float)1.0e-14,f);
 
   }
 
   public int bcd2(byte[] b, int k){
     return (int)(1000*((b[k  ]>>4)&0xf)+100*(b[k  ]&0xf)+
 	           10*((b[k+1]>>4)&0xf)+  1*(b[k+1]&0xf));
   }
 
   public int bin5(byte[] b, int k){
     byte b0 = b[k  ];
     byte b1 = b[k+1];
     byte b2 = b[k+2];
     byte b3 = b[k+3];
     byte b4 = b[k+4];
     if(b0<0) b0 += 256;
     if(b1<0) b1 += 256;
     if(b2<0) b2 += 256;
     if(b3<0) b3 += 256;
     if(b4<0) b4 += 256;
    return (int)(256.0+b0*65536.0+b1*256.0+b2+b3/256.0+b4/65536.0);
   }
 
   public int bin3(byte[] b, int k){
     byte b0 = b[k  ];
     byte b1 = b[k+1];
     byte b2 = b[k+2]; 
     if(b0<0) b0 += 256;
     if(b1<0) b1 += 256;
     if(b2<0) b2 += 256;
     return (int)((b0<<16)|(b1<<8)|(b2));
   }
   
  // public String segdDir = "/gpfc/ckohnke/fc2013/segd/141/"; // Linux Lab
  public String segdDir = "/home/colton/Documents/School/SrDesign/fc2013/segd/141/"; // Laptop
   public Sampling s1 = new Sampling(4001,0.002,0.000);
   public Sampling s2 = new Sampling(342,1,954);
   public Sampling s3 = new Sampling(215,1.0,1003);
   public float[][] f = null;
   public int sln,spn,rpf,rpl;
 
 
 }
