 
 public class Datenmodell
 {
     private Gleisstueck g1,g2,g3,g4,g5,g6,g7,g8,g9,g10,g11,g12,g13,g14,g15,g16,g17,g18,g19,g20,g21,g22,g23,g24,g25,g26,g27,g28,g29;
     private Weiche w1,w2,w3,w5,w6,w7,w8,w10,w11,w12,w13,w14,w15,w16,w40,w39,w44,w43,w38,w37,w42,w41,w17;
     private Signal s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11,s12,s13,s14,s15,s16,s17,s18,s20,s21,s22,s25,s26,s30,s31;
     public Signal[]s;/**Array, das alle Signale enthlt*/
     public Weiche[]w;/**Array, das alle Weichen enthlt*/
     public Gleisstueck[]g;/**Array, das alle Gleissstuecke enthlt*/
     
     /** Datenmodell:
      * Konstruktor Datenmodell
      */
     public Datenmodell()
     {
         g1=new Gleisstueck("Gleisstueck 1", 640, 705, 444);
         g2=new Gleisstueck("Gleisstueck 2", 320, 885, 458);
         g3=new Gleisstueck("Gleisstueck 3", 590, 740, 479);
         g4=new Gleisstueck("Gleisstueck 4", 190, 757, 489);
         g5=new Gleisstueck("Gleisstueck 5", 210, 530, 508);
         g6=new Gleisstueck("Gleisstueck 6", 260, 675, 508);
         g7=new Gleisstueck("Gleisstueck 7", 730, 929, 508);
         g8=new Gleisstueck("Gleisstueck 8", 200, 1152, 508);
         g9=new Gleisstueck("Gleisstueck 9", 190, 527, 525);
         g10=new Gleisstueck("Gleisstueck 10", 570, 673, 525);
         g11=new Gleisstueck("Gleisstueck 11", 700, 938, 525);
         g12=new Gleisstueck("Gleisstueck 12", 220, 1156, 525);
         g13=new Gleisstueck("Gleisstueck 13", 260, 143, 608);
         g14=new Gleisstueck("Gleisstueck 14", 60, 227, 608);
         g15=new Gleisstueck("Gleisstueck 15", 690, 356, 608);
         g16=new Gleisstueck("Gleisstueck 16", 330, 596, 608);
         g17=new Gleisstueck("Gleisstueck 17", 250, 144, 627);
         g18=new Gleisstueck("Gleisstueck 18", 80, 230, 627);
         g19=new Gleisstueck("Gleisstueck 19", 690, 402, 627);
         g20=new Gleisstueck("Gleisstueck 20", 60, 550, 627);
         g21=new Gleisstueck("Gleisstueck 21", 51, 594, 627);
         g22=new Gleisstueck("Gleisstueck 22", 50, 642, 627);
         g23=new Gleisstueck("Gleisstueck 23", 690, 404, 644);
         g24=new Gleisstueck("Gleisstueck 24", 120, 596, 644);
         g25=new Gleisstueck("Gleisstueck 25", 1220, 890, 623);
         g26=new Gleisstueck("Gleisstueck 26", 60, 1088, 525);
         g27=new Gleisstueck("Gleisstueck 27", 50, 604, 508);
         g28=new Gleisstueck("Gleisstueck 28", 50, 804, 459);
         g29=new Gleisstueck("Gleisstueck 29", 60, 268, 627);
        
         w1=new Weiche("Weiche 1",g5,g9,g27);
         w2=new Weiche("Weiche 2",g10,g27,g9);
         w3=new Weiche("Weiche 3",g6,g1,g27);
         w5=new Weiche("Weiche 5",g28,g1,g2);
         w6=new Weiche("Weiche 6",g3,g4,g28);
         w7=new Weiche("Weiche 7",g7,g11,g6);
         w8=new Weiche("Weiche 8",g10,g6,g11);
         w10=new Weiche("Weiche 10",g11,g25,g26);
         w11=new Weiche("Weiche 11",g7,g26,g8);
         w12=new Weiche("Weiche 12",g12,g8,g26);
         w13=new Weiche("Weiche 13",g13,g17,g14);
         w14=new Weiche("Weiche 14",g18,g14,g17);
         w15=new Weiche("Weiche 15",g15,g19,g14);
         w16=new Weiche("Weiche 16",g18,g14,g29);
         w40=new Weiche("Weiche 40",g16,g20,g15);
         w39=new Weiche("Weiche 39",g19,g15,g20);
        w44=new Weiche("Weiche 44",g16,g22,g3);
         w43=new Weiche("Weiche 43",g4,g3,g22);
         w38=new Weiche("Weiche 38",g21,g24,g20);
         w37=new Weiche("Weiche 37",g23,g20,g24);
         w42=new Weiche("Weiche 42",g21,g24,g22);
         w41=new Weiche("Weiche 41",g25,g22,g24);
         w17=new Weiche("Weiche 17",g19,g23,g29);
 
         g1.setWeichen(w3, w5);
         g2.setWeichen(w5, null);
         g3.setWeichen(w44, w6);
         g4.setWeichen(w43, w6);
         g5.setWeichen(null, w1);
         g6.setWeichen(w3, w7);
         g7.setWeichen(w7, w11);
         g8.setWeichen(w11, null);
         g9.setWeichen(null, w2);
         g10.setWeichen(w2, w8);
         g11.setWeichen(w8, w10);
         g12.setWeichen(w12, null);
         g13.setWeichen(null, w13);
         g14.setWeichen(w13, w15);
         g15.setWeichen(w15, w40);
         g16.setWeichen(w40, w44);
         g17.setWeichen(null, w14);
         g18.setWeichen(w14, w16);
         g19.setWeichen(w17, w39);
         g20.setWeichen(w39, w38);
         g21.setWeichen(w38, w42);
         g22.setWeichen(w42, w43);
         g23.setWeichen(w17, w37);
         g24.setWeichen(w37, w41);
         g25.setWeichen(w41, w10);
         g26.setWeichen(w10, w12);
         g27.setWeichen(w1, w3);
         g28.setWeichen(w6, w5);
         g29.setWeichen(w16, w17);
 
         //true=ende
         s1=new Signal("Signal 1", g6, false);
         s2=new Signal("Signal 2", g7, false);
         s3=new Signal("Signal 3", g8, false);
         s4=new Signal("Signal 4", g9, true);
         s5=new Signal("Signal 5", g10, false);
         s6=new Signal("Signal 6", g6, true);
         s7=new Signal("Signal 7", g11, false);
         s8=new Signal("Signal 8", g12, false);
         s9=new Signal("Signal 9", g10, true);
         s10=new Signal("Signal 10", g11, true);
         s11=new Signal("Signal 11", g1, false);
         s12=new Signal("Signal 12", g1, true);
         s13=new Signal("Signal 13", g3, true);
         s14=new Signal("Signal 14", g4, true);
         s15=new Signal("Signal 15", g2, false);
         s16=new Signal("Signal 16", g13, true);
         s17=new Signal("Signal 17", g15, false);
         s18=new Signal("Signal 18", g15, true);
         s20=new Signal("Signal 20", g17, true);
         s21=new Signal("Signal 21", g19, false);
         s22=new Signal("Signal 22", g19, true);
         s25=new Signal("Signal 25", g23, false);
         s26=new Signal("Signal 26", g25, false);
         s30=new Signal("Signal 30", g23, true);
         s31=new Signal("Signal 31", g25, true);
 
         g1.setSignal(s11);
         g1.setSignal(s12);
         g2.setSignal(s15);
         g3.setSignal(s13);
         g4.setSignal(s14);
         g6.setSignal(s1);
         g6.setSignal(s6);
         g7.setSignal(s2);
         g8.setSignal(s3);
         g9.setSignal(s4);
         g10.setSignal(s5);
         g10.setSignal(s9);
         g11.setSignal(s7);
         g11.setSignal(s10);
         g12.setSignal(s8);
         g13.setSignal(s16);
         g15.setSignal(s17);
         g15.setSignal(s18);
         g17.setSignal(s20);
         g19.setSignal(s21);
         g19.setSignal(s22);
         g23.setSignal(s25);
         g23.setSignal(s30);
         g25.setSignal(s31);
         g25.setSignal(s26);
 
         s= new Signal[25];
         s[0]=s1;
         s[1]=s2;
         s[2]=s3;
         s[3]=s4;
         s[4]=s5;
         s[5]=s6;
         s[6]=s7;
         s[7]=s8;
         s[8]=s9;
         s[9]=s10;
         s[10]=s11;
         s[11]=s12;
         s[12]=s13;
         s[13]=s14;
         s[14]=s15;
         s[15]=s16;
         s[16]=s17;
         s[17]=s18;
         s[18]=s20;
         s[19]=s21;
         s[20]=s22;
         s[21]=s25;
         s[22]=s26;
         s[23]=s30;
         s[24]=s31;
         
         w= new Weiche[23];
         w[0]=w1;
         w[1]=w2;
         w[2]=w3;
         w[3]=w5;
         w[4]=w6;
         w[5]=w7;
         w[6]=w8; 
         w[7]=w10;
         w[8]=w11;
         w[9]=w12;
         w[10]=w13;
         w[11]=w14;
         w[12]=w15;
         w[13]=w16;
         w[14]=w17;
         w[15]=w37;
         w[16]=w38;
         w[17]=w39;
         w[18]=w40;
         w[19]=w41;
         w[20]=w42;
         w[21]=w43;
         w[22]=w44;
        
         
         g= new Gleisstueck[29];
         g[0]=g1;
         g[1]=g2;
         g[2]=g3;
         g[3]=g4;
         g[4]=g5;
         g[5]=g6;
         g[6]=g7;
         g[7]=g8;
         g[8]=g9;
         g[9]=g10;
         g[10]=g11;
         g[11]=g12;
         g[12]=g13;
         g[13]=g14;
         g[14]=g15;
         g[15]=g16;
         g[16]=g17;
         g[17]=g18;
         g[18]=g19;
         g[19]=g20;
         g[20]=g21;
         g[21]=g22;
         g[22]=g23;
         g[23]=g24;
         g[24]=g25;
         g[25]=g26;
         g[26]=g27;
         g[27]=g28;
         g[28]=g29;
 
        
    
         
     }
     
     /** Datenmodell:
      * Man erhaelt die Weiche mit dem Arrayindex 'nummer'.
      * @param nummer Arrayindex
      */
     public Weiche getWeiche(int nummer)
     {
         if(nummer < w.length && nummer >= 0) return w[nummer];
         return null;
     }
     
     /** Datenmodell:
      * Man erhaelt das Signal mit dem Arrayindex 'nummer'.
      * @param nummer Arrayindex
      */
     public Signal getSignal(int nummer)
     {
         if(nummer < s.length && nummer >= 0) return s[nummer];
         return null;
     }
     
     /** Datenmodell:
      * Man erhaelt das Gleisstueck mit dem Arrayindex 'nummer'.
      * @param nummer Arrayindex
      */
     public Gleisstueck getGleisstueck(int nummer)
     {
         if(nummer < g.length && nummer >= 0) return g[nummer];
         return null;
     }
 }
