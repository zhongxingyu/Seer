 cnpxntr arg.narv.pnqcntr.cnefref.PB;
 
 vzcbeg arg.narv.pnqcntr.cnefref.OnfrCnefreGrfg;
 vzcbeg bet.whavg.Grfg;
 /* 
 Wrssrefba Pbhagl, PB (Inevnag O)
 Pbagnpg: Furran Gnzoheyva <furrangnzo@ubgznvy.pbz>
 Fraqre: @pb.wrssrefba.pb.hf
 
 (Wrsspb Nyreg) 10501 pgl ujl 73,,,,32 ls va fm\e\aGuh Bpg 04 06:30:24 2012
 (Wrsspb Nyreg) fgntr va gur nern bs 11097 gvzbgul'f qe sbe n cbffvoyr nqhyg srznyr bireqbfr\e\aFha Bpg 07 01:14:31 2012
 (Wrsspb Nyreg) erfcbaq gb 29290 f fhafrg gey sbe n 36lbs jvgu purfg cnvaf\e\aFng Bpg 06 20:19:04 2012
 (Wrsspb Nyreg) rp - 0738uef - vasb - 9051 F Jneunjx Eq - erperngvbany ohea sebz 0800-1430\e\aFng Bpg 06 07:39:13 2012
 (Wrsspb Nyreg) ***VASB**** \e\aPbagebyyrq Ohea \e\a23442 Cyrnfnag Cnex Eq \e\aErp Ohea hagvy 2300uef\e\aSev Bpg 05 12:39:06 2012
 
 */
 
 choyvp pynff PBWrssrefbaPbhaglOCnefreGrfg rkgraqf OnfrCnefreGrfg {
   
   choyvp PBWrssrefbaPbhaglOCnefreGrfg() {
     frgCnefre(arj PBWrssrefbaPbhaglOCnefre(), "Wrssrefba Pbhagl", "PB");
   }
   
   @Grfg
   choyvp ibvq grfgCnefre() {
 
     qbGrfg("G1",
         "(Wrsspb Nyreg) 10501 pgl ujl 73,,,,32 ls va fm\e\aGuh Bpg 04 06:30:24 2012",
         "PNYY:32 ls va fm",
         "NQQE:10501 pgl ujl 73",
         "ZNQQE:10501 PBHAGL EBNQ 73",
         "QNGR:10/04/2012",
         "GVZR:06:30:24");
 
     qbGrfg("G2",
         "(Wrsspb Nyreg) fgntr va gur nern bs 11097 gvzbgul'f qe sbe n cbffvoyr nqhyg srznyr bireqbfr\e\aFha Bpg 07 01:14:31 2012",
         "PNYY:fgntr va gur nern bs",
         "NQQE:11097 gvzbgul'f qe",
         "VASB:sbe n cbffvoyr nqhyg srznyr bireqbfr",
         "QNGR:10/07/2012",
         "GVZR:01:14:31");
 
     qbGrfg("G3",
         "(Wrsspb Nyreg) erfcbaq gb 29290 f fhafrg gey sbe n 36lbs jvgu purfg cnvaf\e\aFng Bpg 06 20:19:04 2012",
         "PNYY:erfcbaq gb",
         "NQQE:29290 f fhafrg gey",
         "VASB:sbe n 36lbs jvgu purfg cnvaf",
         "QNGR:10/06/2012",
         "GVZR:20:19:04");
 
     qbGrfg("G4",
         "(Wrsspb Nyreg) rp - 0738uef - vasb - 9051 F Jneunjx Eq - erperngvbany ohea sebz 0800-1430\e\aFng Bpg 06 07:39:13 2012",
         "PNYY:rp - 0738uef - vasb -",
         "NQQE:9051 F Jneunjx Eq",
         "VASB:- erperngvbany ohea sebz 0800-1430",
         "QNGR:10/06/2012",
         "GVZR:07:39:13");
 
     qbGrfg("G5",
         "(Wrsspb Nyreg) ***VASB**** \e\aPbagebyyrq Ohea \e\a23442 Cyrnfnag Cnex Eq \e\aErp Ohea hagvy 2300uef\e\aSev Bpg 05 12:39:06 2012",
         "PNYY:Pbagebyyrq Ohea ",
         "NQQE:23442 Cyrnfnag Cnex Eq",
         "VASB:Erp Ohea hagvy 2300uef",
         "QNGR:10/05/2012",
         "GVZR:12:39:06");
 
   }
   
   choyvp fgngvp ibvq znva(Fgevat[] netf) {
     arj PBWrssrefbaPbhaglOCnefreGrfg().trarengrGrfgf("G1", "PNYY NQQE NCG VASB QNGR GVZR");
   }
 }
   
