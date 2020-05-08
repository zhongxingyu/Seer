 package zemberek3.lexicon;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class TurkishSuffixes {
 
     static Map<String, SuffixFormSet> suffixSets = new HashMap<String, SuffixFormSet>();
 
     public static TurkishSuffix Pl = new TurkishSuffix("Pl");
     public static SuffixFormSet Pl_lAr = newSet(Pl, "lAr");
 
     public static TurkishSuffix Dat = new TurkishSuffix("Dat");
     public static SuffixFormSet Dat_yA = newSet(Dat, "+yA");
     public static SuffixFormSet Dat_nA = newSet(Dat, "nA");
 
     public static TurkishSuffix Loc = new TurkishSuffix("Loc");
     public static SuffixFormSet Loc_dA = newSet(Loc, ">dA");
     public static SuffixFormSet Loc_ndA = newSet(Loc, "ndA");
 
     public static TurkishSuffix Abl = new TurkishSuffix("Abl");
     public static SuffixFormSet Abl_dAn = newSet(Abl, ">dAn");
     public static SuffixFormSet Abl_ndAn = newSet(Abl, "ndAn");
 
     public static TurkishSuffix Gen = new TurkishSuffix("Gen");
     public static SuffixFormSet Gen_nIn = newSet(Gen, "+nIn");
 
     public static TurkishSuffix Acc = new TurkishSuffix("Acc");
     public static SuffixFormSet Acc_yI = newSet(Acc, "+yI");
     public static SuffixFormSet Acc_nI = newSet(Acc, "nI");
 
     public static TurkishSuffix Inst = new TurkishSuffix("Inst");
     public static SuffixFormSet Inst_ylA = newSet(Inst, "+ylA");
 
     public static TurkishSuffix P1sg = new TurkishSuffix("P1sg");
     public static SuffixFormSet P1sg_Im = newSet(P1sg, "+Im");
 
     public static TurkishSuffix P2sg = new TurkishSuffix("P2sg");
     public static SuffixFormSet P2sg_In = newSet(P2sg, "+In");
 
     public static TurkishSuffix P3sg = new TurkishSuffix("P3sg");
     public static SuffixFormSet P3sg_sI = newSet(P3sg, "+sI");
 
     public static TurkishSuffix P1pl = new TurkishSuffix("P1pl");
     public static SuffixFormSet P1pl_ImIz = newSet(P1pl, "+ImIz");
 
     public static TurkishSuffix P2pl = new TurkishSuffix("P2pl");
     public static SuffixFormSet P2pl_InIz = newSet(P2pl, "+InIz");
 
     public static TurkishSuffix P3pl = new TurkishSuffix("P3pl");
     public static SuffixFormSet P3pl_lArI = newSet(P3pl, "lArI");
 
     public static TurkishSuffix Dim = new TurkishSuffix("Dim");
     public static SuffixFormSet Dim_cIk = newSet(Dim, ">cIk");
     public static SuffixFormSet Dim_cIg = newSet(Dim, ">cIğ");
     public static SuffixFormSet Dim_cAgIz = newSet(Dim, "cAğIz");
 
     public static TurkishSuffix With = new TurkishSuffix("With");
     public static SuffixFormSet With_lI = newSet(With, "lI");
 
     public static TurkishSuffix Without = new TurkishSuffix("Without");
     public static SuffixFormSet Without_sIz = newSet(Without, "sIz");
 
     public static TurkishSuffix Rel = new TurkishSuffix("Rel");
     public static SuffixFormSet Rel_ki = newSet(Rel, "ki"); // masa-da-ki
 
     public static TurkishSuffix A1sg = new TurkishSuffix("A1sg");
     public static SuffixFormSet A1sg_yIm = newSet(A1sg, "+yIm"); // gel-e-yim
     public static SuffixFormSet A1sg_m = newSet(A1sg, "m"); // gel-se-m
 
     public static TurkishSuffix A2sg = new TurkishSuffix("A2sg");
     public static SuffixFormSet A2sg_sIn = newSet(A2sg, "sIn"); // gel-ecek-sin
     public static SuffixFormSet A2sg_n = newSet(A2sg, "n"); // gel-di-n
     public static SuffixFormSet A2sg_sAnA = newSet(A2sg, "sAnA"); //gel-sene
     public static SuffixFormSet A2sg_yInIz = newSet(A2sg, "+yInIz"); //gel-iniz
     public static SuffixFormSet A2sg_EMPTY = newSet("A2sg_EMPTY", A2sg, ""); // gel-
 
     public static TurkishSuffix A3sg = new TurkishSuffix("A3sg");
     public static SuffixFormSet A3sg_EMPTY = newSet(A3sg, ""); // gel-di-
     public static SuffixFormSet A3sg_sIn = newSet(A3sg, "sIn"); // gel-sin
 
     public static TurkishSuffix A1pl = new TurkishSuffix("A1pl");
     public static SuffixFormSet A1pl_yIz = newSet(A1pl, "+yIz"); // geliyor-uz
     public static SuffixFormSet A1pl_k = newSet(A1pl, "k"); // gel-di-k
     public static SuffixFormSet A1pl_lIm = newSet(A1pl, "lIm"); // gel-e-lim
 
     public static TurkishSuffix A2pl = new TurkishSuffix("A2pl");
     public static SuffixFormSet A2pl_sInIz = newSet(A2pl, "sInIz"); // gel-ecek-siniz
     public static SuffixFormSet A2pl_sAnIzA = newSet(A2pl, "sAnIzA"); // gel-senize
     public static SuffixFormSet A2pl_nIz = newSet(A2pl, "nIz"); // gel-di-niz
     public static SuffixFormSet A2pl_yIn = newSet(A2pl, "+yIn"); // gel-me-yin
 
     public static TurkishSuffix A3pl = new TurkishSuffix("A3pl");
     public static SuffixFormSet A3pl_lAr = newSet(A3pl, "lAr"); // gel-ecek-ler
     public static SuffixFormSet A3pl_sInlAr = newSet(A3pl, "sInlAr"); // gel-sinler
 
     public static TurkishSuffix Agt = new TurkishSuffix("Agt");
     public static SuffixFormSet Agt_cI = newSet(Agt, ">cI"); // araba-cı
     public static SuffixFormSet Agt_yIcI = newSet(Agt, "+yIcI"); // otur-ucu
 
     public static TurkishSuffix Become = new TurkishSuffix("Become");
     public static SuffixFormSet Become_lAs = newSet(Become, "lAş");
 
     public static TurkishSuffix Resemb = new TurkishSuffix("Resemb");
     public static SuffixFormSet Resemb_ImsI = newSet(Resemb, "ImsI"); // udunumsu
     public static SuffixFormSet Resemb_msI = newSet(Resemb, "+msI"); // odunsu
 
     public static TurkishSuffix Aor = new TurkishSuffix("Aor");
     public static SuffixFormSet Aor_Ir = newSet(Aor, "+Ir"); //gel-ir
     public static SuffixFormSet Aor_Ar = newSet(Aor, "+Ar"); //ser-er
     public static SuffixFormSet Aor_z = newSet(Aor, "z"); // gel-me-z
     public static SuffixFormSet Aor_EMPTY = newSet("Aor_EMPTY", Aor, ""); // gel-me--yiz
 
     public static TurkishSuffix AorPart = new TurkishSuffix("AorPart"); // convert to an Adjective
     public static SuffixFormSet AorPart_Ir = newSet(AorPart, "+Ir"); //gel-ir
     public static SuffixFormSet AorPart_Ar = newSet(AorPart, "+Ar"); //ser-er
     public static SuffixFormSet AorPart_z = newSet(AorPart, "z"); // gel-me-z
 
     public static TurkishSuffix Prog = new TurkishSuffix("Prog");
     public static SuffixFormSet Prog_Iyor = newSet(Prog, "+Iyor");
     public static SuffixFormSet Prog_mAktA = newSet(Prog, "mAktA");
 
     public static TurkishSuffix Fut = new TurkishSuffix("Fut");
     public static SuffixFormSet Fut_yAcAk = newSet(Fut, "+yAcAk");
     public static SuffixFormSet Fut_yAcAg = newSet(Fut, "+yAcAğ");
 
     public static TurkishSuffix Past = new TurkishSuffix("Past");
     public static SuffixFormSet Past_dI = newSet(Past, ">dI");
 
     public static TurkishSuffix Evid = new TurkishSuffix("Evid");
     public static SuffixFormSet Evid_mIs = newSet(Evid, "mIş");
 
     public static TurkishSuffix Neg = new TurkishSuffix("Neg");
     public static SuffixFormSet Neg_mA = newSet(Neg, "mA"); //gel-me
     public static SuffixFormSet Neg_m = newSet(Neg, "m", false); // gel-m-iyor
 
     public static TurkishSuffix Cond = new TurkishSuffix("Cond");
     public static SuffixFormSet Cond_ysA = newSet(Cond, "+ysA");
 
     public static TurkishSuffix Necess = new TurkishSuffix("Necess");
     public static SuffixFormSet Necess_mAlI = newSet(Necess, "mAlI");
 
     public static TurkishSuffix Opt = new TurkishSuffix("Opt");
     public static SuffixFormSet Opt_yA = newSet(Opt, "+yA");
 
     public static TurkishSuffix Pass = new TurkishSuffix("Pass");
     public static SuffixFormSet Pass_In = newSet(Pass, "+In");
     public static SuffixFormSet Pass_nIl = newSet(Pass, "+nIl");
     public static SuffixFormSet Pass_Il = newSet(Pass, "Il");
 
     public static TurkishSuffix Caus = new TurkishSuffix("Caus");
     public static SuffixFormSet Caus_t = newSet(Caus, "t");
     public static SuffixFormSet Caus_tIr = newSet(Pass, ">dIr");
 
     public static TurkishSuffix Imp = new TurkishSuffix("Imp");
     public static SuffixFormSet Imp_EMPTY = newSet(Imp, "");
 
 
     public static TurkishSuffix Recip = new TurkishSuffix("Recip");
     public static SuffixFormSet Recip_yIs = newSet(Recip, "+yIş");
     public static SuffixFormSet Recip_Is = newSet(Recip, "+Iş");
 
     public static TurkishSuffix Reflex = new TurkishSuffix("Reflex");
     public static SuffixFormSet Reflex_In = newSet(Reflex, "+In");
 
     public static TurkishSuffix Abil = new TurkishSuffix("Abil");
     public static SuffixFormSet Abil_yAbil = newSet(Abil, "+yAbil");
     public static SuffixFormSet Abil_yA = newSet(Abil, "+yA", false);
 
     public static TurkishSuffix Cop = new TurkishSuffix("Cop");
     public static SuffixFormSet Cop_dIr = newSet(Cop, ">dIr");
 
     public static TurkishSuffix PastCop = new TurkishSuffix("PastCop");
     public static SuffixFormSet PastCop_ydI = newSet(PastCop, "+ydI");
 
     public static TurkishSuffix EvidCop = new TurkishSuffix("EvidCop");
     public static SuffixFormSet EvidCop_ymIs = newSet(EvidCop, "+ymIş");
 
     public static TurkishSuffix CondCop = new TurkishSuffix("CondCop");
     public static SuffixFormSet CondCop_ysA = newSet(CondCop, "+ysA");
 
     public static TurkishSuffix While = new TurkishSuffix("While");
     public static SuffixFormSet While_ken = newSet(While, "+yken");
 
     public static TurkishSuffix AfterDoing = new TurkishSuffix("AfterDoing");
     public static SuffixFormSet AfterDoing_yIncA = newSet(AfterDoing, "+yIncA");
 
     public static TurkishSuffix Ly = new TurkishSuffix("Ly");
     public static SuffixFormSet Ly_cA = newSet(Ly, ">cA");
 
     public static TurkishSuffix Quite = new TurkishSuffix("Quite");
     public static SuffixFormSet Quite_cA = newSet(Quite, ">cA");
 
     public static TurkishSuffix NounRoot = new TurkishSuffix("NounRoot");
     public static SuffixFormSet Noun_Main = newSet("Noun_Main", NounRoot, "");
     public static SuffixFormSet Noun_Exp_C = newSet("Noun_Exp_C", NounRoot, "");
     public static SuffixFormSet Noun_Exp_V = newSet("Noun_Exp_V", NounRoot, "");
     public static SuffixFormSet Noun_Comp_P3sg = newSet("Noun_Comp_P3sg", NounRoot, "");
     public static SuffixFormSet Noun_Comp_P3sg_Root = newSet("Noun_Comp_P3sg_Root", NounRoot, "");
 
     public static TurkishSuffix AdjRoot = new TurkishSuffix("AdjRoot");
     public static SuffixFormSet Adj_Main = newSet("Adj_Main", AdjRoot, "");
     public static SuffixFormSet Adj_Exp_C = newSet("Adj_Exp_C", AdjRoot, "");
     public static SuffixFormSet Adj_Exp_V = newSet("Adj_Exp_V", AdjRoot, "");
 
     public static TurkishSuffix VerbRoot = new TurkishSuffix("VerbRoot");
     public static SuffixFormSet Verb_Main = newSet("Verb_Main", VerbRoot, "");
     public static SuffixFormSet Verb_Aor_Ar = newSet("Verb_Aor_Ar", VerbRoot, "");
     public static SuffixFormSet Verb_Prog_Drop = newSet("Verb_Prog_Drop", VerbRoot, "");
     public static SuffixFormSet Verb_Prog_NotDrop = newSet("Verb_Prog_NotDrop", VerbRoot, "");
     public static SuffixFormSet Verb_Vow_Drop = newSet("Verb_Vow_Drop", VerbRoot, "");
     public static SuffixFormSet Verb_Vow_NotDrop = newSet("Verb_Vow_NotDrop", VerbRoot, "");
     public static SuffixFormSet Verb_Exp_C = newSet("Verb_Exp_C", VerbRoot, "");
     public static SuffixFormSet Verb_Exp_V = newSet("Verb_Exp_V", VerbRoot, "");
 
     public static SuffixFormSet Verb_Ye = newSet("Verb_Ye", VerbRoot, "");
     public static SuffixFormSet Verb_Yi = newSet("Verb_Yi", VerbRoot, "");
 
     public static SuffixFormSet Verb_De = newSet("Verb_De", VerbRoot, "");
     public static SuffixFormSet Verb_Di = newSet("Verb_Di", VerbRoot, "");
 
 
     public static TurkishSuffix PronounRoot = new TurkishSuffix("Pronoun");
     public static SuffixFormSet Pron_Main = newSet(PronounRoot, "");
     public static SuffixFormSet Pron_BenSen = newSet("Pron_BenSen", PronounRoot, "");
     public static SuffixFormSet Pron_BanSan = newSet("Pron_BanSan", PronounRoot, "");
 
 
     public static final SuffixFormSet[] CASE_FORMS = {Dat_yA, Loc_dA, Abl_dAn, Gen_nIn, Acc_yI, Inst_ylA};
     public static final SuffixFormSet[] POSSESSIVE_FORMS = {P1sg_Im, P2sg_In, P1pl_ImIz, P2pl_InIz, P3pl_lArI};
     public static final SuffixFormSet[] PERSON_FORMS_N = {A1sg_yIm, A2sg_sIn, A3sg_EMPTY, A1pl_yIz, A2pl_sInIz, A3pl_lAr};
     public static final SuffixFormSet[] PERSON_FORMS_COP = {A1sg_m, A2sg_n, A3sg_EMPTY, A1pl_k, A2pl_nIz, A3pl_lAr};
     public static final SuffixFormSet[] COPULAR_FORMS = {Cop_dIr, PastCop_ydI, EvidCop_ymIs, CondCop_ysA, While_ken};
     public static final SuffixFormSet[] TENSE_DEFAULT_FORMS = {Prog_Iyor, Prog_mAktA, Fut_yAcAg, Fut_yAcAk, Past_dI, Evid_mIs, Aor_Ir};
     public static final SuffixFormSet[] ROOT_FORMS = {
             Noun_Main, Noun_Exp_C, Noun_Exp_V, Noun_Comp_P3sg, Noun_Comp_P3sg_Root,
             Adj_Main, Adj_Exp_C, Adj_Exp_V,
             Verb_Main, Verb_Aor_Ar, Verb_Prog_Drop, Verb_Prog_NotDrop, Verb_Vow_Drop, Verb_Vow_NotDrop, Verb_Exp_C, Verb_Exp_V,
             Verb_Ye, Verb_Yi, Verb_De, Verb_Di, Pron_Main, Pron_BenSen, Pron_BanSan};
 
     static SuffixFormSet newSet(TurkishSuffix suffix, String generation) {
         String id = suffix + "_" + generation;
         return newSet(id, suffix, generation, true);
     }
 
     static SuffixFormSet newSet(TurkishSuffix suffix, String generation, boolean endSuffix) {
         String id = suffix + "_" + generation;
         return newSet(id, suffix, generation, endSuffix);
     }
 
     static SuffixFormSet newSet(String id, TurkishSuffix suffix, String generation, boolean endSuffix) {
         if (suffixSets.containsKey(id))
             throw new IllegalArgumentException("There is already a suffix set with same id:" + id);
         SuffixFormSet newSet = new SuffixFormSet(id, suffix, generation, endSuffix);
         suffixSets.put(id, newSet);
         return newSet;
 
     }
 
     static SuffixFormSet newSet(String id, TurkishSuffix suffix, String generation) {
         return newSet(id, suffix, generation, true);
     }
 
     public SuffixFormSet getRootSuffixFormSet(PrimaryPos pos) {
         switch (pos) {
             case Noun:
                 return Noun_Main;
             case Adjective:
                 return Adj_Main;
             case Verb:
                 return Verb_Main;
         }
         return Noun_Main;
     }
 
     public Iterable<SuffixFormSet> getSets() {
         return suffixSets.values();
     }
 
     private static Map<String, TurkishSuffix> suffixMap = new HashMap<String, TurkishSuffix>();
 
     public TurkishSuffixes() {
 
         Noun_Main.succ(CASE_FORMS, COPULAR_FORMS, PERSON_FORMS_N)
                 .succ(Pl_lAr, Dim_cIg, Dim_cIk, Dim_cAgIz, With_lI, Without_sIz, A3sg_EMPTY, Agt_cI, Resemb_msI, Resemb_ImsI);
         Noun_Exp_C.succ(Pl_lAr, Loc_dA, Abl_dAn, Inst_ylA, P3pl_lArI, A2sg_sIn, A2pl_sInIz, A3pl_lAr,
                 Dim_cIg, Dim_cIk, Dim_cAgIz, With_lI, Without_sIz, Agt_cI, Resemb_msI)
                 .succ(COPULAR_FORMS);
         Noun_Exp_V.succ(Dat_yA, Acc_yI, Gen_nIn, P1sg_Im, P2sg_In, P3sg_sI, P1pl_ImIz, P2pl_InIz, A1sg_yIm, A1pl_yIz, Resemb_ImsI);
         Noun_Comp_P3sg.succ(COPULAR_FORMS)
                 .succ(Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA)
                 .succ(A1sg_yIm, A1pl_yIz, A2sg_sIn, A2pl_sInIz);
         Noun_Comp_P3sg_Root.succ(With_lI, Without_sIz, Agt_cI, Resemb_msI, Resemb_ImsI);
 
         Verb_Main.succ(Prog_Iyor, Prog_mAktA, Fut_yAcAg, Fut_yAcAk, Past_dI, Evid_mIs, Aor_Ir, AorPart_Ir)
                 .succ(Neg_mA, Neg_m, Abil_yAbil, Abil_yA, Pass_In, Caus_tIr, AfterDoing_yIncA, Opt_yA, Imp_EMPTY, Agt_yIcI);
         Verb_Aor_Ar.succ(Verb_Main.getSuccessors()).remove(Aor_Ir, AorPart_Ir).succ(Aor_Ar, AorPart_Ar);
         Verb_Vow_Drop.succ(Pass_Il);
         Verb_Vow_NotDrop.succ(Verb_Main.getSuccessors()).remove(Pass_Il);
         Verb_Prog_Drop.succ(Prog_Iyor);
         Verb_Prog_NotDrop.succ(Verb_Main.getSuccessors()).remove(Prog_Iyor);
 
         Verb_Ye.succ(Verb_Main.getSuccessors()).remove(Prog_Iyor, Fut_yAcAg, Fut_yAcAk, Opt_yA);
         Verb_Yi.succ(Opt_yA, Fut_yAcAg, Fut_yAcAk, AfterDoing_yIncA);
         // modification rule does not apply for some suffixes for "demek". like deyip, not diyip
         Verb_De.succ(Verb_Main.getSuccessors()).remove(Prog_Iyor, Fut_yAcAg, Fut_yAcAk, Opt_yA, AfterDoing_yIncA);
         Verb_Di.succ(Opt_yA, Fut_yAcAg, Fut_yAcAk);
 
         Verb_Exp_V.succ(Opt_yA, Fut_yAcAg, Fut_yAcAg, Aor_Ar, AorPart_Ar, Prog_Iyor);
         Verb_Exp_C.succ(Verb_Main.getSuccessors()).remove(Verb_Exp_V.getSuccessors()).remove(Aor_Ir, AorPart_Ir);
 
         Pron_Main.succ(CASE_FORMS);
         Pron_BenSen.succ(CASE_FORMS).remove(Dat_yA);
         Pron_BanSan.succ(Dat_yA);
 
         Adj_Main.succ(Noun_Main.getSuccessors()).succ(Ly_cA, Become_lAs, Quite_cA);
         Adj_Exp_C.succ(Noun_Exp_C.getSuccessors()).succ(Ly_cA, Become_lAs, Quite_cA);
         Adj_Exp_V.succ(Noun_Exp_V.getSuccessors());
         Become_lAs.succ(Verb_Main.getSuccessors());
 
 
         Pl_lAr.succ(CASE_FORMS, COPULAR_FORMS)
                 .succ(P1sg_Im, P2sg_In, P1pl_ImIz, P2pl_InIz, A1pl_yIz, A2pl_sInIz);
 
         P1sg_Im.succ(CASE_FORMS, COPULAR_FORMS).succ(A2sg_sIn, A2pl_sInIz);
         P2sg_In.succ(CASE_FORMS, COPULAR_FORMS).succ(A1sg_yIm, A1pl_yIz);
         P3sg_sI.succ(COPULAR_FORMS)
                 .succ(Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA)
                 .succ(A1sg_yIm, A1pl_yIz, A2sg_sIn, A2pl_sInIz);
         P1pl_ImIz.succ(CASE_FORMS, COPULAR_FORMS);
         P2pl_InIz.succ(CASE_FORMS, COPULAR_FORMS);
         P3pl_lArI.succ(P3sg_sI.getSuccessorsIterable());
 
         Rel_ki.succ(COPULAR_FORMS, PERSON_FORMS_N).succ(Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA);
         Dat_yA.succ(COPULAR_FORMS);
         Dat_nA.succ(COPULAR_FORMS);
 
         Loc_dA.succ(COPULAR_FORMS).succ(Rel_ki);
         Loc_ndA.succ(COPULAR_FORMS);
 
         Abl_dAn.succ(COPULAR_FORMS);
         Abl_ndAn.succ(COPULAR_FORMS);
 
         Gen_nIn.succ(COPULAR_FORMS).succ(Rel_ki);
 
         Dim_cIg.succ(Dat_yA, Acc_yI, Gen_nIn, P1sg_Im, P2sg_In, P3sg_sI, P1pl_ImIz, P2pl_InIz, A1sg_yIm, A1pl_yIz);
         Dim_cIk.succ(Loc_dA, Abl_dAn, Inst_ylA, P3pl_lArI, A2sg_sIn, A2pl_sInIz, A3pl_lAr).succ(COPULAR_FORMS);
         Dim_cAgIz.succ(CASE_FORMS, COPULAR_FORMS, POSSESSIVE_FORMS, PERSON_FORMS_N);
 
         With_lI.succ(CASE_FORMS, COPULAR_FORMS, POSSESSIVE_FORMS, PERSON_FORMS_N).succ(Pl_lAr);
         Without_sIz.succ(CASE_FORMS, COPULAR_FORMS, POSSESSIVE_FORMS, PERSON_FORMS_N).succ(Pl_lAr);
 
         PastCop_ydI.succ(PERSON_FORMS_COP);
         EvidCop_ymIs.succ(PERSON_FORMS_COP);
         CondCop_ysA.succ(PERSON_FORMS_COP);
 
         Neg_mA.succ(Aor_z, AorPart_z, Aor_EMPTY, Prog_mAktA, Imp_EMPTY, Opt_yA, Fut_yAcAk, Fut_yAcAg, Past_dI, Evid_mIs, Cond_ysA, Abil_yAbil, Necess_mAlI);
         Neg_m.succ(Prog_Iyor);
 
         Aor_Ar.succ(PERSON_FORMS_N, COPULAR_FORMS).succ(Cond_ysA);
         Aor_Ir.succ(PERSON_FORMS_N, COPULAR_FORMS).succ(Cond_ysA);
         Aor_z.succ(COPULAR_FORMS).succ(A3sg_sIn, Cond_ysA);
         Aor_EMPTY.succ(A1sg_m, A1pl_yIz);
 
         AorPart_Ar.succ(Adj_Main.getSuccessors()).remove(Become_lAs);
         AorPart_Ir.succ(AorPart_Ar.getSuccessors());
         AorPart_z.succ(AorPart_Ar.getSuccessors());
 
         Prog_Iyor.succ(PERSON_FORMS_N, COPULAR_FORMS).succ(Cond_ysA);
         Prog_mAktA.succ(PERSON_FORMS_N, COPULAR_FORMS).succ(Cond_ysA);
 
         Fut_yAcAg.succ(A1sg_yIm, A1pl_yIz);
         Fut_yAcAk.succ(PERSON_FORMS_N, COPULAR_FORMS).succ(Cond_ysA).remove(Fut_yAcAg.getSuccessors());
 
         Past_dI.succ(A1sg_m, A2sg_n, A3sg_EMPTY, A1pl_k, A2pl_nIz, A3pl_lAr, CondCop_ysA, PastCop_ydI);
         Evid_mIs.succ(PERSON_FORMS_N).succ(CondCop_ysA, PastCop_ydI, EvidCop_ymIs, While_ken,Cop_dIr);
 
         Cond_ysA.succ(A1sg_m, A2sg_n, A3sg_EMPTY, A1pl_k, A2pl_nIz, A3pl_lAr);
         
        Imp_EMPTY.succ(A2sg_EMPTY, A2sg_sAnA, A2sg_yInIz, A2pl_sAnIzA, A2pl_yIn, A3sg_sIn, A3pl_sInlAr);
         Agt_cI.succ(CASE_FORMS, PERSON_FORMS_N, POSSESSIVE_FORMS, COPULAR_FORMS).succ(Pl_lAr, Become_lAs, With_lI, Without_sIz);
         Agt_yIcI.succ(Agt_cI.getSuccessors());
 
         Abil_yAbil.succ(Verb_Main.getSuccessors()).remove(Abil_yAbil, Abil_yA);
         Abil_yA.succ(Neg_mA, Neg_m);
 
         Opt_yA.succ(A1sg_yIm, A2sg_sIn, A3sg_EMPTY, A1pl_lIm, A2pl_sInIz, A3pl_lAr, PastCop_ydI, EvidCop_ymIs );
         
     }
 }
