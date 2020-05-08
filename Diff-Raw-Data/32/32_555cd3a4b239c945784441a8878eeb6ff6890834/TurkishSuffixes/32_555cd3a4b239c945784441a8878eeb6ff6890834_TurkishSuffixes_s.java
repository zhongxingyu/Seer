 package zemberek3.lexicon;
 
 import zemberek3.lexicon.graph.DynamicSuffixProvider;
 import zemberek3.lexicon.graph.SuffixData;
 import zemberek3.lexicon.graph.TerminationType;
 
 import java.util.List;
 
 public class TurkishSuffixes extends DynamicSuffixProvider {
 
     // ------------ case suffixes ---------------------------
 
     public static Suffix Dat = new Suffix("Dat");
     public static SuffixFormSet Dat_yA = new SuffixFormSet(Dat, "+yA");
     public static SuffixFormSet Dat_nA = new SuffixFormSet(Dat, "nA");
 
     public static Suffix Loc = new Suffix("Loc");
     public static SuffixFormSet Loc_dA = new SuffixFormSet(Loc, ">dA");
     public static SuffixFormSet Loc_ndA = new SuffixFormSet(Loc, "ndA");
 
     public static Suffix Abl = new Suffix("Abl");
     public static SuffixFormSet Abl_dAn = new SuffixFormSet(Abl, ">dAn");
     public static SuffixFormSet Abl_ndAn = new SuffixFormSet(Abl, "ndAn");
 
     public static Suffix Gen = new Suffix("Gen");
     public static SuffixFormSet Gen_nIn = new SuffixFormSet(Gen, "+nIn");
 
     public static Suffix Acc = new Suffix("Acc");
     public static SuffixFormSet Acc_yI = new SuffixFormSet(Acc, "+yI");
     public static SuffixFormSet Acc_nI = new SuffixFormSet(Acc, "nI");
 
     public static Suffix Inst = new Suffix("Inst");
     public static SuffixFormSet Inst_ylA = new SuffixFormSet(Inst, "+ylA");
 
     public static Suffix Nom = new Suffix("Nom");
     public static SuffixFormSet Nom_TEMPLATE = getTemplate("Nom_TEMPLATE", Nom);
 
     // ----------------- possesive ----------------------------
 
     public static Suffix Pnon = new Suffix("Pnon");
     public static SuffixFormSet Pnon_TEMPLATE = getTemplate("Pnon_TEMPLATE", Pnon);
 
     public static Suffix P1sg = new Suffix("P1sg");
     public static SuffixFormSet P1sg_Im = new SuffixFormSet(P1sg, "Im");
 
     public static Suffix P2sg = new Suffix("P2sg");
     public static SuffixFormSet P2sg_In = new SuffixFormSet(P2sg, "In");
 
     public static Suffix P3sg = new Suffix("P3sg");
     public static SuffixFormSet P3sg_sI = new SuffixFormSet(P3sg, "+sI");
 
     public static Suffix P1pl = new Suffix("P1pl");
     public static SuffixFormSet P1pl_ImIz = new SuffixFormSet(P1pl, "ImIz");
 
     public static Suffix P2pl = new Suffix("P2pl");
     public static SuffixFormSet P2pl_InIz = new SuffixFormSet(P2pl, "InIz");
 
     public static Suffix P3pl = new Suffix("P3pl");
     public static SuffixFormSet P3pl_lArI = new SuffixFormSet(P3pl, "lArI");
 
     // -------------- Number-Person agreement --------------------
 
     public static Suffix A1sg = new Suffix("A1sg");
     public static SuffixFormSet A1sg_yIm = new SuffixFormSet(A1sg, "+yIm"); // gel-e-yim
     public static SuffixFormSet A1sg_m = new SuffixFormSet(A1sg, "m"); // gel-se-m
     public static SuffixFormSet A1sg_TEMPLATE = getTemplate("A1sg_TEMPLATE", A1sg); // ben
 
     public static Suffix A2sg = new Suffix("A2sg");
     public static SuffixFormSet A2sg_sIn = new SuffixFormSet(A2sg, "sIn"); // gel-ecek-sin
     public static SuffixFormSet A2sg_n = new SuffixFormSet(A2sg, "n"); // gel-di-n
     public static SuffixFormSet A2sg_TEMPLATE = getTemplate("A2sg_TEMPLATE", A2sg); // gel, sen,..
     public static SuffixFormSet A2sg2_sAnA = new SuffixFormSet(A2sg, "sAnA"); //gel-sene
     public static SuffixFormSet A2sg3_yInIz = new SuffixFormSet(A2sg, "+yInIz"); //gel-iniz
 
     public static Suffix A3sg = new Suffix("A3sg");
     public static SuffixFormSet A3sg_TEMPLATE = getTemplate("A3sg_TEMPLATE", A3sg); // gel-di-, o-
     public static SuffixFormSet A3sg_Verb_TEMPLATE = getTemplate("A3sg_Verb_TEMPLATE", A3sg); // gel-di-, o-
     public static SuffixFormSet A3sg_sIn = new SuffixFormSet(A3sg, "sIn"); // gel-sin
 
     public static Suffix A1pl = new Suffix("A1pl");
     public static SuffixFormSet A1pl_yIz = new SuffixFormSet(A1pl, "+yIz"); // geliyor-uz
     public static SuffixFormSet A1pl_k = new SuffixFormSet(A1pl, "k"); // gel-di-k
     public static SuffixFormSet A1pl_lIm = new SuffixFormSet(A1pl, "lIm"); // gel-e-lim
     public static SuffixFormSet A1pl_TEMPLATE = getTemplate("A1pl_TEMPLATE", A1pl); // biz
 
     public static Suffix A2pl = new Suffix("A2pl");
     public static SuffixFormSet A2pl_sInIz = new SuffixFormSet(A2pl, "sInIz"); // gel-ecek-siniz
     public static SuffixFormSet A2pl_nIz = new SuffixFormSet(A2pl, "nIz"); // gel-di-niz
     public static SuffixFormSet A2pl_yIn = new SuffixFormSet(A2pl, "+yIn"); // gel-me-yin
     public static SuffixFormSet A2pl_TEMPLATE = getTemplate("A2pl_TEMPLATE", A2pl); // gel-e-lim
 
     public static Suffix A2pl2 = new Suffix("A2pl2");
     public static SuffixFormSet A2pl2_sAnIzA = new SuffixFormSet(A2pl2, "sAnIzA"); // gel-senize
 
     public static Suffix A3pl = new Suffix("A3pl");
     public static SuffixFormSet A3pl_lAr = new SuffixFormSet(A3pl, "lAr"); // gel-ecek-ler
     public static SuffixFormSet A3pl_Verb_lAr = new SuffixFormSet("A3pl_Verb_lAr", A3pl, "lAr"); // gel-ecek-ler
     public static SuffixFormSet A3pl_Comp_lAr = new SuffixFormSet("A3pl_Comp_lAr", A3pl, "lAr", TerminationType.NON_TERMINAL); //zeytinyağlarımız
     public static SuffixFormSet A3pl_sInlAr = new SuffixFormSet(A3pl, "sInlAr"); // gel-sinler
 
     // ------------ derivatioonal ----------------------
 
     public static Suffix Dim = new Suffix("Dim");
     public static SuffixFormSet Dim_cIk = new SuffixFormSet(Dim, ">cI~k");
 
     public static Suffix Dim2 = new Suffix("Dim2");
     public static SuffixFormSet Dim2_cAgIz = new SuffixFormSet(Dim2, "cAğIz");
 
     public static Suffix With = new Suffix("With");
     public static SuffixFormSet With_lI = new SuffixFormSet(With, "lI");
 
     public static Suffix Without = new Suffix("Without");
     public static SuffixFormSet Without_sIz = new SuffixFormSet(Without, "sIz");
 
     public static Suffix Rel = new Suffix("Rel");
     public static SuffixFormSet Rel_ki = new SuffixFormSet(Rel, "ki"); // masa-da-ki
     public static SuffixFormSet Rel_kI = new SuffixFormSet(Rel, "kI"); // dünkü
 
     public static Suffix Agt = new Suffix("Agt");
     public static SuffixFormSet Agt_cI = new SuffixFormSet(Agt, ">cI"); // araba-cı. Converts to another Noun.
     public static SuffixFormSet Agt_yIcI = new SuffixFormSet(Agt, "+yIcI"); // otur-ucu. converts to both Noun and Adj
 
     public static Suffix Ness = new Suffix("Ness");
     public static SuffixFormSet Ness_lIk = new SuffixFormSet(Ness, "lI~k");
 
     public static Suffix Become = new Suffix("Become");
     public static SuffixFormSet Become_lAs = new SuffixFormSet(Become, "lAş");
     public static SuffixFormSet Become_Adj_lAs = new SuffixFormSet("Become_Adj_lAs", Become, "lAş");
 
     public static Suffix Resemb = new Suffix("Resemb");
     public static SuffixFormSet Resemb_ImsI = new SuffixFormSet(Resemb, "ImsI"); // udunumsu
     public static SuffixFormSet Resemb_msI = new SuffixFormSet(Resemb, "+msI"); // odunsu
 
     public static SuffixFormSet Resemb_Adj_ImsI = new SuffixFormSet("Resemb_Adj_ImsI", Resemb, "ImsI"); // udunumsu
     public static SuffixFormSet Resemb_Adj_msI = new SuffixFormSet("Resemb_Adj_msI", Resemb, "+msI"); // odunsu
 
     public static Suffix Related = new Suffix("Related");
     public static SuffixFormSet Related_sAl = new SuffixFormSet(Related, "sAl");
 
     // ----------------------------  verbal tense --------------------------------
 
     public static Suffix Aor = new Suffix("Aor");
     public static SuffixFormSet Aor_Ir = new SuffixFormSet(Aor, "+Ir"); //gel-ir
     public static SuffixFormSet Aor_Ar = new SuffixFormSet(Aor, "+Ar"); //ser-er
     public static SuffixFormSet Aor_z = new SuffixFormSet(Aor, "z"); // gel-me-z
     public static SuffixFormSet Aor_EMPTY = getTemplate("Aor_EMPTY", Aor, TerminationType.NON_TERMINAL); // gel-me--yiz
 
     public static Suffix Prog = new Suffix("Prog");
     public static SuffixFormSet Prog_Iyor = new SuffixFormSet(Prog, "Iyor");
 
     public static Suffix Prog2 = new Suffix("Prog2");
     public static SuffixFormSet Prog2_mAktA = new SuffixFormSet(Prog2, "mAktA");
 
     public static Suffix Fut = new Suffix("Fut");
     public static SuffixFormSet Fut_yAcAk = new SuffixFormSet(Fut, "+yAcA~k");
 
     public static Suffix Past = new Suffix("Past");
     public static SuffixFormSet Past_dI = new SuffixFormSet(Past, ">dI");
 
     public static Suffix Evid = new Suffix("Evid");
     public static SuffixFormSet Evid_mIs = new SuffixFormSet(Evid, "mIş");
 
     // ---------------------------------------------------
 
     public static Suffix PastPart = new Suffix("PastPart");
     public static SuffixFormSet PastPart_dIk = new SuffixFormSet(PastPart, ">dI~k");
 
     public static Suffix AorPart = new Suffix("AorPart"); // convert to an Adjective
     public static SuffixFormSet AorPart_Ir = new SuffixFormSet(AorPart, "+Ir"); //gel-ir
     public static SuffixFormSet AorPart_Ar = new SuffixFormSet(AorPart, "+Ar"); //ser-er
     public static SuffixFormSet AorPart_z = new SuffixFormSet(AorPart, "z"); // gel-me-z
 
     public static Suffix FutPart = new Suffix("FutPart");
     public static SuffixFormSet FutPart_yAcAk = new SuffixFormSet(FutPart, "+yAcA~k");
 
     public static Suffix EvidPart = new Suffix("EvidPart");
     public static SuffixFormSet EvidPart_mIs = new SuffixFormSet(EvidPart, "mIş");
 
     public static Suffix PresPart = new Suffix("PresPart");
     public static SuffixFormSet PresPart_yAn = new SuffixFormSet(PresPart, "+yAn");
 
     public static Suffix Pos = new Suffix("Pos");
     public static SuffixFormSet Pos_EMPTY = getTemplate("Pos_EMPTY", Pos); // Verb Positive Null Morpheme template.
 
     public static Suffix Neg = new Suffix("Neg");
     public static SuffixFormSet Neg_mA = new SuffixFormSet(Neg, "mA"); //gel-me
     public static SuffixFormSet Neg_m = new SuffixFormSet(Neg, "m", TerminationType.NON_TERMINAL); // gel-m-iyor
 
     //TODO: May be redundant. Cond_Cop may suffice
     public static Suffix Cond = new Suffix("Cond");
     public static SuffixFormSet Cond_sA = new SuffixFormSet(Cond, "sA");
 
     public static Suffix Necess = new Suffix("Necess");
     public static SuffixFormSet Necess_mAlI = new SuffixFormSet(Necess, "mAlI");
 
     public static Suffix Opt = new Suffix("Opt");
     public static SuffixFormSet Opt_yA = new SuffixFormSet(Opt, "+yA");
 
     public static Suffix Pass = new Suffix("Pass");
     public static SuffixFormSet Pass_In = new SuffixFormSet(Pass, "+In");
     public static SuffixFormSet Pass_InIl = new SuffixFormSet(Pass, "+InIl");
     public static SuffixFormSet Pass_nIl = new SuffixFormSet(Pass, "+nIl");
 
     public static Suffix Caus = new Suffix("Caus");
     public static SuffixFormSet Caus_t = new SuffixFormSet(Caus, "t");
     public static SuffixFormSet Caus_tIr = new SuffixFormSet(Caus, ">dIr");
 
     public static Suffix Imp = new Suffix("Imp");
     public static SuffixFormSet Imp_TEMPLATE = getTemplate("Imp_TEMPLATE", Imp);
 
     public static Suffix Des = new Suffix("Des");
     public static SuffixFormSet Des_sA = new SuffixFormSet(Des, "sA");
 
     public static Suffix Recip = new Suffix("Recip");
     public static SuffixFormSet Recip_Is = new SuffixFormSet(Recip, "+Iş");
     public static SuffixFormSet Recip_yIs = new SuffixFormSet(Recip, "+yIş");
 
     public static Suffix Reflex = new Suffix("Reflex");
     public static SuffixFormSet Reflex_In = new SuffixFormSet(Reflex, "+In");
 
     public static Suffix Abil = new Suffix("Abil");
     public static SuffixFormSet Abil_yAbil = new SuffixFormSet(Abil, "+yAbil");
     public static SuffixFormSet Abil_yA = new SuffixFormSet(Abil, "+yA", TerminationType.NON_TERMINAL);
 
     public static Suffix Cop = new Suffix("Cop");
     public static SuffixFormSet Cop_dIr = new SuffixFormSet(Cop, ">dIr");
 
     public static SuffixFormSet PastCop_ydI = new SuffixFormSet("PastCop_ydI", Past, "+y>dI");
 
     public static SuffixFormSet EvidCop_ymIs = new SuffixFormSet("EvidCop_ymIs", Evid, "+ymIş");
 
     public static SuffixFormSet CondCop_ysA = new SuffixFormSet("CondCop_ysA", Cond, "+ysA");
 
     public static Suffix While = new Suffix("While");
     public static SuffixFormSet While_ken = new SuffixFormSet(While, "+yken");
 
     public static Suffix Pres = new Suffix("Pres");  // Present tense only appears after a zero morpheme verb derivation such as "kalemdir"
     public static SuffixFormSet Pres_TEMPLATE = getTemplate("Pres_TEMPLATE", Pres);
 
     public static Suffix Equ = new Suffix("Equ");
     public static SuffixFormSet Equ_cA = new SuffixFormSet(Equ, ">cA");
     public static SuffixFormSet Equ_ncA = new SuffixFormSet(Equ, "ncA");
 
     public static Suffix NotState = new Suffix("NotState");
     public static SuffixFormSet NotState_mAzlIk = new SuffixFormSet(NotState, "mAzlI~k");
 
     public static Suffix ActOf = new Suffix("ActOf");
     public static SuffixFormSet ActOf_mAcA = new SuffixFormSet(ActOf, "mAcA");
 
     public static Suffix AsIf = new Suffix("AsIf");
     public static SuffixFormSet AsIf_cAsInA = new SuffixFormSet(AsIf, ">cAsInA");
 
     // Converts to an Adverb.
     public static Suffix AsLongAs = new Suffix("AsLongAs");
     public static SuffixFormSet AsLongAs_dIkcA = new SuffixFormSet(AsLongAs, ">dIkçA");
 
     public static Suffix When = new Suffix("When");
     public static SuffixFormSet When_yIncA = new SuffixFormSet(When, "+yIncA");
 
     // It also may have "worthy of doing" meaning after passive. Converts to an Adjective.
     public static Suffix FeelLike = new Suffix("FeelLike");
     public static SuffixFormSet FeelLike_yAsI = new SuffixFormSet(FeelLike, "+yAsI");
 
     // Converts to an Adverb.
     public static Suffix SinceDoing = new Suffix("SinceDoing");
     public static SuffixFormSet SinceDoing_yAlI = new SuffixFormSet(SinceDoing, "+yAlI");
 
     // Converts to an Adverb.
     public static Suffix ByDoing = new Suffix("ByDoing");
     public static SuffixFormSet ByDoing_yArAk = new SuffixFormSet(ByDoing, "+yArAk");
 
     // Converts to an Adverb.
     public static Suffix WithoutDoing = new Suffix("WithoutDoing");
     public static SuffixFormSet WithoutDoing_mAdAn = new SuffixFormSet(WithoutDoing, "mAdAn");
 
     // Converts to an Adverb.
     public static Suffix UntilDoing = new Suffix("UntilDoing");
     public static SuffixFormSet UntilDoing_yAsIyA = new SuffixFormSet(UntilDoing, "+yAsIyA");
 
 
     public static Suffix WithoutDoing2 = new Suffix("WithoutDoing2");
     public static SuffixFormSet WithoutDoing2_mAksIzIn = new SuffixFormSet(WithoutDoing2, "mAksIzIn");
 
     // Converts to an Adverb.
     public static Suffix AfterDoing = new Suffix("AfterDoing");
     public static SuffixFormSet AfterDoing_yIp = new SuffixFormSet(AfterDoing, "+yIp");
 
     public static Suffix UnableToDo = new Suffix("UnableToDo");
     public static SuffixFormSet UnableToDo_yAmAdAn = new SuffixFormSet(UnableToDo, "+yAmAdAn");
 
     public static Suffix InsteadOfDoing = new Suffix("InsteadOfDoing");
     public static SuffixFormSet InsteadOfDoing_mAktAnsA = new SuffixFormSet(InsteadOfDoing, "mAktAnsA");
 
     // Converts to an Adverb.
     public static Suffix KeepDoing = new Suffix("KeepDoing");
     public static SuffixFormSet KeepDoing_yAgor = new SuffixFormSet(KeepDoing, "+yAgör");
 
     public static Suffix KeepDoing2 = new Suffix("KeepDoing2");
     public static SuffixFormSet KeepDoing2_yAdur = new SuffixFormSet(KeepDoing2, "+yAdur");
 
     public static Suffix EverSince = new Suffix("EverSince");
     public static SuffixFormSet EverSince_yAgel = new SuffixFormSet(EverSince, "+yAgel");
 
     public static Suffix Almost = new Suffix("Almost");
     public static SuffixFormSet Almost_yAyAz = new SuffixFormSet(Almost, "+yAyaz");
 
     public static Suffix Hastily = new Suffix("Hastily");
     public static SuffixFormSet Hastily_yIver = new SuffixFormSet(Hastily, "+yIver");
 
     public static Suffix Stay = new Suffix("Stay");
     public static SuffixFormSet Stay_yAkal = new SuffixFormSet(Stay, "+yAkal");
 
     public static Suffix Inf1 = new Suffix("Inf1");
     public static SuffixFormSet Inf1_mAk = new SuffixFormSet(Inf1, "mAk");
 
     public static Suffix Inf2 = new Suffix("Inf2");
     public static SuffixFormSet Inf2_mA = new SuffixFormSet(Inf2, "mA");
 
     public static Suffix Inf3 = new Suffix("Inf3");
     public static SuffixFormSet Inf3_yIs = new SuffixFormSet(Inf3, "+yIş");
 
     // TODO is below a valid morpheme? 
 /*    public static Suffix NounDeriv = new Suffix("NounDeriv");
     public static SuffixFormSet NounDeriv_nIm = new SuffixFormSet(NounDeriv, "+nIm");*/
 
     public static Suffix Ly = new Suffix("Ly");
     public static SuffixFormSet Ly_cA = new SuffixFormSet(Ly, ">cA");
 
     public static Suffix Quite = new Suffix("Quite");
     public static SuffixFormSet Quite_cA = new SuffixFormSet(Quite, ">cA");
 
     public static Suffix Ordinal = new Suffix("Ordinal");
     public static SuffixFormSet Ordinal_IncI = new SuffixFormSet(Ordinal, "+IncI");
 
     public static Suffix Grouping = new Suffix("Grouping");
     public static SuffixFormSet Grouping_sAr = new SuffixFormSet(Grouping, "+şAr");
 
     public static Suffix NounRoot = new Suffix("Noun");
     public static SuffixFormSet Noun_TEMPLATE = getTemplate("Noun_TEMPLATE", NounRoot);
     public static SuffixFormSet Noun2Noun = getTemplate("Noun2Noun", NounRoot, TerminationType.NON_TERMINAL);
     public static SuffixFormSet Adj2Noun = getTemplate("Adj2Noun", NounRoot, TerminationType.NON_TERMINAL);
     public static SuffixFormSet Verb2Noun = getTemplate("Verb2Noun", NounRoot, TerminationType.NON_TERMINAL);
     public static SuffixFormSet Noun_Default = getNull("Noun_Default", NounRoot);
     public static SuffixFormSet Noun_Comp_P3sg = getTemplate("Noun_Comp_P3sg", NounRoot);
     public static SuffixFormSet Noun_Comp_P3sg_Root = getTemplate("Noun_Comp_P3sg_Root", NounRoot);
 
     public static Suffix AdjRoot = new Suffix("Adj");
     public static SuffixFormSet Adj_TEMPLATE = getTemplate("Adj_TEMPLATE", AdjRoot);
     public static SuffixFormSet Noun2Adj = getTemplate("Noun2Adj", AdjRoot, TerminationType.NON_TERMINAL);
     public static SuffixFormSet Adj2Adj = getTemplate("Adj2Adj", AdjRoot, TerminationType.NON_TERMINAL);
     public static SuffixFormSet Verb2Adj = getTemplate("Verb2Adj", AdjRoot, TerminationType.NON_TERMINAL);
     public static SuffixFormSet Adj_Main_Rel = getNull("Adj_TEMPLATE", AdjRoot);
     public static SuffixFormSet Adj_Default = getNull("Adj_Default", AdjRoot, TerminationType.TERMINAL);
     public static Suffix AdvRoot = new Suffix("AdvRoot");
 
     public static SuffixFormSet Adv_TEMPLATE = getTemplate("Adv_TEMPLATE", AdvRoot);
     public static SuffixFormSet Adj2Adv = getTemplate("Adj2Adv", AdvRoot, TerminationType.NON_TERMINAL);
     public static SuffixFormSet Adv_Default = getNull("Adv_Default", AdvRoot);
 
     public static Suffix InterjRoot = new Suffix("Interj");
     public static SuffixFormSet Interj_Main = getTemplate("Interj_Main", InterjRoot);
 
     public static Suffix ConjRoot = new Suffix("Conj");
     public static SuffixFormSet Conj_Main = getTemplate("Conj_Main", ConjRoot);
 
     public static Suffix NumeralRoot = new Suffix("Numeral");
     public static SuffixFormSet Numeral_Main = getTemplate("Numeral_Main", NumeralRoot);
 
     public static Suffix DetRoot = new Suffix("Det");
     public static SuffixFormSet Det_Main = getTemplate("Det_Main", DetRoot);
 
     public static Suffix ProperNounRoot = new Suffix("ProperNounRoot");
     public static SuffixFormSet ProperNoun_Main = getTemplate("ProperNoun_Main", ProperNounRoot);
 
     public static Suffix VerbRoot = new Suffix("Verb");
     public static SuffixFormSet Verb_TEMPLATE = getTemplate("Verb_TEMPLATE", VerbRoot);
     public static SuffixFormSet Adj2Verb = getTemplate("Adj2Verb", VerbRoot, TerminationType.NON_TERMINAL);
     public static SuffixFormSet Noun2Verb = getTemplate("Noun2Verb", VerbRoot, TerminationType.NON_TERMINAL);
     public static SuffixFormSet Noun2VerbCopular = getTemplate("Noun2VerbCopular", VerbRoot, TerminationType.NON_TERMINAL);
     public static SuffixFormSet Verb2Verb = getTemplate("Verb2Verb", VerbRoot, TerminationType.NON_TERMINAL);
     public static SuffixFormSet Verb2VerbCompounds = getTemplate("Verb2VerbCompounds", VerbRoot, TerminationType.NON_TERMINAL);
     public static SuffixFormSet Verb_Default = getNull("Verb_Default", VerbRoot);
     public static SuffixFormSet Verb_Prog_Drop = new SuffixFormSet("Verb_Prog_Drop", VerbRoot, "");
 
     public static Suffix PersPronRoot = new Suffix("PersPron");
     public static SuffixFormSet PersPron_TEMPLATE = getTemplate("PersPron_TEMPLATE", PersPronRoot);
     public static SuffixFormSet PersPron_BenSen = getTemplate("PersPron_BenSen", PersPronRoot);
     public static SuffixFormSet PersPron_BanSan = getTemplate("PersPron_BanSan", PersPronRoot);
 
     public static Suffix QuesRoot = new Suffix("Ques");
     public static SuffixFormSet Ques_mI = getTemplate("Ques_mI", QuesRoot);
 
     public static Suffix ParticleRoot = new Suffix("Particle");
     public static SuffixFormSet Particle_Main = getTemplate("Particle_Main", ParticleRoot);
 
     // TODO: add time root. (with Rel_ki + Noun)
 
     public static final SuffixFormSet[] CASE_FORMS = {Nom_TEMPLATE, Dat_yA, Loc_dA, Abl_dAn, Gen_nIn, Acc_yI, Inst_ylA, Equ_cA};
     public static final SuffixFormSet[] POSSESSIVE_FORMS = {Pnon_TEMPLATE, P1sg_Im, P2sg_In, P3sg_sI, P1pl_ImIz, P2pl_InIz, P3pl_lArI};
     public static final SuffixFormSet[] PERSON_FORMS_N = {A1sg_yIm, A2sg_sIn, A3sg_TEMPLATE, A1pl_yIz, A2pl_sInIz, A3pl_lAr};
     public static final SuffixFormSet[] PERSON_FORMS_COP = {A1sg_m, A2sg_n, A3sg_TEMPLATE, A1pl_k, A2pl_nIz, A3pl_lAr};
     public static final SuffixFormSet[] COPULAR_FORMS = {Cop_dIr, PastCop_ydI, EvidCop_ymIs, CondCop_ysA, While_ken};
 
     public TurkishSuffixes() {
 
         //---------------------------- Root and Derivation Morphemes ---------------------------------------------------
 
         // noun template. it has all possible suffix forms that a noun can have
         Noun_TEMPLATE.directSuccessors.add(A3pl_lAr, A3pl_Comp_lAr, A3sg_TEMPLATE);
         Noun_TEMPLATE.successors
                 .add(POSSESSIVE_FORMS, CASE_FORMS)
                 .add(Cop_dIr, PastCop_ydI, EvidCop_ymIs, CondCop_ysA, While_ken)
                 .add(A1sg_m, A2sg_n, A3sg_TEMPLATE, A1pl_k, A2pl_nIz, A3pl_lAr)
                 .add(Dat_nA, Loc_ndA, Abl_ndAn, Acc_nI, Equ_ncA)
                 .add(Dim_cIk, Dim2_cAgIz, With_lI, Without_sIz, Agt_cI, Resemb_msI, Resemb_ImsI, Ness_lIk, Related_sAl)
                 .add(Become_lAs)
                 .add(Noun2Noun, Noun2Adj, Noun2Verb, Noun2VerbCopular);
 
         // default noun suffix form. we remove some suffixes so that words like araba-na (dative)
         Noun_Default.directSuccessors.add(A3pl_lAr, A3sg_TEMPLATE);
         Noun_Default.successors
                 .add(Noun_TEMPLATE.successors)
                 .remove(Dat_nA, Loc_ndA, Abl_ndAn, Acc_nI);
 
         Noun2Noun.directSuccessors.add(Dim_cIk, Dim2_cAgIz, Agt_cI, Ness_lIk);
 
         Noun2Adj.directSuccessors.add(With_lI, Without_sIz, Resemb_msI, Resemb_ImsI, Rel_ki, Related_sAl);
 
         Noun2Verb.directSuccessors.add(Become_lAs);
 
         Noun2VerbCopular.directSuccessors.add(Cop_dIr, PastCop_ydI, EvidCop_ymIs, CondCop_ysA, While_ken); //TODO person successors are missing.
         Noun2VerbCopular.successors.add(A1sg_m, A2sg_n, A3sg_Verb_TEMPLATE, A1pl_k, A2pl_nIz, A3pl_Verb_lAr);
 
         Adj2Noun.directSuccessors.add(Noun_TEMPLATE.directSuccessors);
         Adj2Noun.successors.add(Noun_TEMPLATE.successors).remove(Related_sAl, Become_lAs, Resemb_ImsI, Resemb_msI);
 
         Adj2Adj.directSuccessors.add(Quite_cA, Resemb_Adj_ImsI, Resemb_Adj_msI);
 
         Adj2Adv.directSuccessors.add(Ly_cA);
 
         Adj2Verb.directSuccessors.add(Become_Adj_lAs).add(COPULAR_FORMS);
 
         Verb2Verb.directSuccessors.add(Caus_t, Caus_tIr, Pass_In, Pass_nIl, Pass_InIl, Abil_yA, Abil_yAbil);
 
         Verb2VerbCompounds.directSuccessors.add(KeepDoing_yAgor, KeepDoing2_yAdur, EverSince_yAgel, Almost_yAyAz, Hastily_yIver, Stay_yAkal);
 
         Verb2Noun.directSuccessors.add(Inf1_mAk, Inf2_mA, Inf3_yIs);
         Verb2Noun.successors.add(Noun_TEMPLATE.successors);
 
         Adj_TEMPLATE.directSuccessors.add(Adj2Noun, Adj2Adj, Adj2Adv, Adj2Verb);
         Adj_TEMPLATE.successors.add(
                 Adj2Noun.allSuccessors(),
                 Adj2Adj.allSuccessors(),
                 Adj2Adv.allSuccessors(),
                 Adj2Verb.allSuccessors());
 
         Adj_Default.directSuccessors.add(Adj_TEMPLATE.directSuccessors);
         Adj_Default.successors.add(Adj_TEMPLATE.successors);
 
         // P3sg compound suffixes. (full form. such as zeytinyağı-na)
         Noun_Comp_P3sg.directSuccessors.add(A3sg_TEMPLATE);
         Noun_Comp_P3sg.successors.add(POSSESSIVE_FORMS)
                 .add(Pnon_TEMPLATE, Nom_TEMPLATE)
                 .add(Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA)
                 .add(A1sg_yIm, A1pl_yIz, A2sg_sIn, A2pl_sInIz);
 
         // P3sg compound suffixes. (root form. such as zeytinyağ-lar-ı)
         Noun_Comp_P3sg_Root.directSuccessors.add(A3pl_Comp_lAr, A3sg_TEMPLATE); // A3pl_Comp_lAr is used, because zeytinyağ-lar is not allowed.
         Noun_Comp_P3sg_Root.successors
                 .add(Pnon_TEMPLATE, Nom_TEMPLATE, With_lI, Without_sIz, Agt_cI, Resemb_msI, Resemb_ImsI, Ness_lIk, Related_sAl)
                 .add(P3pl_lArI)
                 .add(Noun2Noun.allSuccessors())
                 .add(Noun2Noun)
                 .add(Noun2Adj.allSuccessors())
                 .add(Noun2Adj);
 
         // Proper noun default //TODO: should be a template
         ProperNoun_Main.directSuccessors.add(A3pl_lAr, A3sg_TEMPLATE);
         ProperNoun_Main.successors
                 .add(CASE_FORMS, POSSESSIVE_FORMS)
                 .add(Dim_cIk, Dim2_cAgIz, With_lI, Without_sIz, A3sg_TEMPLATE, Agt_cI, Ness_lIk);
 
         Verb_TEMPLATE.directSuccessors.add(Neg_mA, Neg_m, Pos_EMPTY, Verb2Verb);
         Verb_TEMPLATE.successors
                 .add(Prog_Iyor, Prog2_mAktA, Fut_yAcAk, Past_dI, Evid_mIs, Aor_Ir, Aor_Ar, AorPart_Ir)
                 .add(Abil_yAbil, Abil_yA, Caus_tIr, Caus_t, Opt_yA, Imp_TEMPLATE, Agt_yIcI, Des_sA)
                 .add(NotState_mAzlIk, ActOf_mAcA, PastPart_dIk, EvidPart_mIs, Pass_In, Pass_nIl, Pass_InIl)
                .add(FutPart_yAcAk, PresPart_yAn, AsLongAs_dIkcA)
                 .add(A1sg_yIm, A2sg_sIn, A2sg_TEMPLATE, A3sg_Verb_TEMPLATE, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr, A3sg_sIn, A3pl_sInlAr, A2sg2_sAnA, A2sg3_yInIz)
                .add(Inf1_mAk, Inf2_mA, Inf3_yIs)
                 .add(When_yIncA, FeelLike_yAsI, SinceDoing_yAlI, ByDoing_yArAk, WithoutDoing_mAdAn, WithoutDoing2_mAksIzIn)
                 .add(AfterDoing_yIp, When_yIncA, UnableToDo_yAmAdAn, InsteadOfDoing_mAktAnsA)
                 .add(KeepDoing2_yAdur, KeepDoing_yAgor, EverSince_yAgel, Almost_yAyAz, Hastily_yIver, Stay_yAkal, Recip_Is)
                 .add(UntilDoing_yAsIyA, Verb2VerbCompounds, Verb2Noun);
 
         Verb_Default.directSuccessors.add(Verb_TEMPLATE.directSuccessors);
         Verb_Default.successors.add(Verb_TEMPLATE.successors).remove(Caus_t);
 
         //---------------------------- Noun -----------------------------------------------------------------------
 
         A3pl_lAr.directSuccessors.add(POSSESSIVE_FORMS).remove(P3pl_lArI);
         A3pl_lAr.successors
                 .add(CASE_FORMS)
                 .add(A1pl_yIz, A2pl_sInIz);
 
         //TODO: check below.
         A3pl_Comp_lAr.directSuccessors.add(A3pl_lAr.directSuccessors);
         A3pl_Comp_lAr.successors.add(CASE_FORMS)
                 .add(A1pl_yIz, A2pl_sInIz);
 
         A3sg_TEMPLATE.directSuccessors.add(POSSESSIVE_FORMS);
         A3sg_TEMPLATE.successors
                 .add(Noun_TEMPLATE.successors).remove(POSSESSIVE_FORMS)
                 .add(Noun2Noun.allSuccessors()).add(Noun2Noun)
                 .add(Noun2VerbCopular.allSuccessors())
                 .add(Noun2Adj.allSuccessors().add(Noun2Adj));
 
         Nom_TEMPLATE.directSuccessors.add(Noun2Noun, Noun2Adj, Noun2Verb, Noun2VerbCopular);
 
         Nom_TEMPLATE.successors
                 .add(Noun2Noun.allSuccessors())
                 .add(Noun2Adj.allSuccessors())
                 .add(Noun2VerbCopular.allSuccessors())
                 .add(Noun2Verb.allSuccessors());
 
         Dim_cIk.directSuccessors.add(Noun_Default.directSuccessors);
         Dim_cIk.successors.add(Noun_Default.allSuccessors().remove(Dim_cIk, Dim2_cAgIz));
         Dim_cIk.successors.add(Noun2VerbCopular);
 
         Dim2_cAgIz.directSuccessors.add(Noun_Default.directSuccessors);
         Dim2_cAgIz.successors.add(Noun_Default.allSuccessors().remove(Dim_cIk, Dim2_cAgIz));
 
         Ness_lIk.directSuccessors.add(Noun_Default.directSuccessors);
         Ness_lIk.successors.add(Noun_Default.allSuccessors().remove(Ness_lIk));
 
         Pnon_TEMPLATE.directSuccessors
                 .add(CASE_FORMS)
                 .add(Dat_nA, Loc_ndA, Abl_ndAn, Acc_nI);
         Pnon_TEMPLATE.successors
                 .add(Nom_TEMPLATE.directSuccessors)
                 .add(Noun2Noun.allSuccessors())
                 .add(Noun2Verb.allSuccessors())
                 .add(Noun2VerbCopular.allSuccessors())
                 .add(Noun2Adj.allSuccessors());
 
         P1sg_Im.directSuccessors.add(CASE_FORMS);
 
         P2sg_In.directSuccessors.add(CASE_FORMS);
 
         P3sg_sI.directSuccessors.add(Nom_TEMPLATE, Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA, Equ_ncA);
 
         P1pl_ImIz.directSuccessors.add(CASE_FORMS);
 
         P2pl_InIz.directSuccessors.add(CASE_FORMS);
 
         P3pl_lArI.directSuccessors.add(CASE_FORMS);
 
         With_lI.directSuccessors.add(Adj_TEMPLATE.directSuccessors);
         With_lI.successors.add(Adj_TEMPLATE.successors);
 
         Without_sIz.directSuccessors.add(Adj_TEMPLATE.directSuccessors);
         Without_sIz.successors.add(Adj_TEMPLATE.successors);
 
         // Noun->Adj derivation elmadaki=elma+Loc-Adj+Rel
         Loc_dA.directSuccessors.add(Noun2Adj, Noun2VerbCopular);
         Loc_dA.successors.add(Rel_ki).add(Noun2VerbCopular.allSuccessors());
 
         Loc_ndA.directSuccessors.add(Noun2Adj, Noun2VerbCopular);
         Loc_ndA.successors.add(Rel_ki).add(Noun2VerbCopular.allSuccessors());
 
         Dat_nA.directSuccessors.add(Noun2VerbCopular);
         Dat_nA.successors.add(Noun2VerbCopular.allSuccessors());
 
         Dat_yA.directSuccessors.add(Noun2VerbCopular);
         Dat_yA.successors.add(Noun2VerbCopular.allSuccessors());
 
         Abl_dAn.directSuccessors.add(Noun2VerbCopular);
         Abl_dAn.successors.add(Noun2VerbCopular.allSuccessors());
 
         Abl_ndAn.directSuccessors.add(Noun2VerbCopular);
         Abl_ndAn.successors.add(Noun2VerbCopular.allSuccessors());
 
         Rel_ki.directSuccessors.add(Adj2Noun);
         Rel_ki.successors.add(Adj2Noun.successors).add(A3sg_TEMPLATE, Pnon_TEMPLATE, Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA, Equ_ncA);
 
         Resemb_msI.directSuccessors.add(Adj_TEMPLATE.directSuccessors);
         Resemb_msI.successors.add(Adj_TEMPLATE.successors);
 
         Resemb_ImsI.directSuccessors.add(Adj_TEMPLATE.directSuccessors);
         Resemb_ImsI.successors.add(Adj_TEMPLATE.successors);
 
         Resemb_Adj_msI.directSuccessors.add(Adj_TEMPLATE.directSuccessors);
         Resemb_Adj_msI.successors.add(Adj_TEMPLATE.successors);
 
         Resemb_Adj_ImsI.directSuccessors.add(Adj_TEMPLATE.directSuccessors);
         Resemb_Adj_ImsI.successors.add(Adj_TEMPLATE.successors);
 
         //---------------------------- Adjective -----------------------------------------------------------------------
 
         Become_lAs.directSuccessors.add(Verb_TEMPLATE.directSuccessors);
         Become_lAs.successors.add(Verb_TEMPLATE.successors);
 
         Become_Adj_lAs.directSuccessors.add(Verb_TEMPLATE.directSuccessors);
         Become_Adj_lAs.successors.add(Verb_TEMPLATE.successors);
 
         Quite_cA.directSuccessors.add(Adj_TEMPLATE.directSuccessors);
 
         Ly_cA.directSuccessors.add(Adv_TEMPLATE.directSuccessors);
 
         //---------------------------- Verb ----------------------------------------------------------------------------
 
         Pos_EMPTY.directSuccessors
                 .add(Verb2VerbCompounds, Verb2Noun, Imp_TEMPLATE, Prog_Iyor, Prog2_mAktA, Fut_yAcAk, Aor_Ar, Aor_Ir, Past_dI, Evid_mIs)
                 .add(Cond_sA, Necess_mAlI, Opt_yA);
         Pos_EMPTY.successors
                .add(Verb_Default.successors).remove(Neg_m, Neg_mA);
 
         Neg_mA.directSuccessors.add(Verb2VerbCompounds, Verb2Noun, Verb2Verb,
                 Aor_z, AorPart_z, Aor_EMPTY, Prog2_mAktA, Imp_TEMPLATE, Opt_yA, Des_sA,
                 Fut_yAcAk, Past_dI, Evid_mIs, Cond_sA, Necess_mAlI, NotState_mAzlIk,
                 ActOf_mAcA, PastPart_dIk, FutPart_yAcAk, EvidPart_mIs, Agt_yIcI)
                 .add(AsLongAs_dIkcA, PresPart_yAn)
                 .add(When_yIncA, FeelLike_yAsI, SinceDoing_yAlI, ByDoing_yArAk, WithoutDoing2_mAksIzIn)
                 .add(AfterDoing_yIp, When_yIncA, InsteadOfDoing_mAktAnsA);
         Neg_mA.successors.add(Verb2VerbCompounds.directSuccessors, Verb2Noun.directSuccessors)
                .add(A2sg_TEMPLATE, A1sg_yIm, A2sg_sIn, A2sg2_sAnA, A2sg3_yInIz, A3sg_Verb_TEMPLATE, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr, A3sg_sIn, A3pl_sInlAr)
                 .add(Abil_yAbil);
 
         Neg_m.directSuccessors.add(Prog_Iyor);
 
         Imp_TEMPLATE.directSuccessors.add(A2sg_TEMPLATE, A2sg2_sAnA, A2sg3_yInIz, A2pl2_sAnIzA, A2pl_yIn, A3sg_sIn, A3pl_sInlAr);
 
         Caus_t.directSuccessors.add(Verb_TEMPLATE.directSuccessors);
         Caus_t.successors.add(Verb_TEMPLATE.allSuccessors()).add(Pass_nIl).add(Caus_tIr).remove(Caus_t);
 
         Caus_tIr.directSuccessors.add(Verb_TEMPLATE.directSuccessors);
         Caus_tIr.successors.add(Verb_TEMPLATE.allSuccessors()).add(Pass_nIl).add(Caus_t).remove(Caus_tIr);
 
         Pass_nIl.directSuccessors.add(Verb_TEMPLATE.directSuccessors);
         Pass_nIl.successors.add(Verb_TEMPLATE.allSuccessors()).remove(Caus_t, Caus_tIr, Pass_nIl, Pass_InIl, Pass_In);
 
         Pass_In.directSuccessors.add(Verb_TEMPLATE.directSuccessors);
         Pass_In.successors.add(Verb_TEMPLATE.allSuccessors()).remove(Caus_t, Caus_tIr, Pass_nIl, Pass_InIl, Pass_In);
 
         Pass_InIl.directSuccessors.add(Verb_TEMPLATE.directSuccessors);
         Pass_InIl.successors.add(Verb_TEMPLATE.allSuccessors()).remove(Caus_t, Caus_tIr, Pass_nIl, Pass_InIl, Pass_In);
 
         Prog_Iyor.directSuccessors.add(A3sg_Verb_TEMPLATE, A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr).add(COPULAR_FORMS);
         Prog2_mAktA.directSuccessors.add(A3sg_Verb_TEMPLATE, A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr).add(COPULAR_FORMS);
 
         Fut_yAcAk.directSuccessors.add(A3sg_Verb_TEMPLATE, A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr).add(COPULAR_FORMS);
 
         Aor_Ar.directSuccessors.add(A3sg_Verb_TEMPLATE, A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr).add(COPULAR_FORMS);
         Aor_Ir.directSuccessors.add(A3sg_Verb_TEMPLATE, A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr).add(COPULAR_FORMS);
         Aor_z.directSuccessors.add(A3sg_Verb_TEMPLATE, A3sg_sIn).add(COPULAR_FORMS);
         Aor_EMPTY.directSuccessors.add(A1sg_m, A1pl_yIz);
 
         Past_dI.directSuccessors.add(A1sg_m, A2sg_n, A3sg_Verb_TEMPLATE, A1pl_k, A2pl_nIz, A3pl_Verb_lAr, CondCop_ysA, PastCop_ydI);
         Evid_mIs.directSuccessors.add(A1sg_yIm, A2sg_sIn, A3sg_Verb_TEMPLATE, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr)
                 .add(CondCop_ysA, PastCop_ydI, EvidCop_ymIs, While_ken, Cop_dIr);
         Cond_sA.directSuccessors.add(A1sg_m, A2sg_n, A3sg_Verb_TEMPLATE, A1pl_k, A2pl_nIz, A3pl_Verb_lAr, PastCop_ydI, EvidCop_ymIs);
 
         PastCop_ydI.directSuccessors.add(PERSON_FORMS_COP);
         EvidCop_ymIs.directSuccessors.add(A1sg_yIm, A2sg_sIn, A3sg_Verb_TEMPLATE, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr, AsIf_cAsInA);
         CondCop_ysA.directSuccessors.add(PERSON_FORMS_COP);
         Cop_dIr.directSuccessors.add(A3pl_Verb_lAr);
 
         Inf1_mAk.directSuccessors.add(A3sg_TEMPLATE);
         Inf1_mAk.successors.add(Pnon_TEMPLATE, Nom_TEMPLATE, Dat_yA, Abl_dAn, Acc_yI);
         Inf2_mA.directSuccessors.add(Noun_TEMPLATE.directSuccessors);
         Inf2_mA.successors.add(Noun_TEMPLATE.successors);
         Inf3_yIs.directSuccessors.add(Noun_TEMPLATE.directSuccessors);
         Inf3_yIs.successors.add(Noun_TEMPLATE.successors);
 
         Abil_yA.directSuccessors.add(Neg_mA);
 
         Abil_yAbil.directSuccessors.add(Neg_mA.directSuccessors).add(Aor_Ir, Prog_Iyor);
         Abil_yAbil.successors.add(Neg_mA.successors);
 
         // TODO: removing false positives like geliveriver may be necessary
 
         KeepDoing_yAgor.directSuccessors.add(Pos_EMPTY.directSuccessors, Neg_mA.directSuccessors);
         KeepDoing_yAgor.successors.add(Pos_EMPTY.successors, Neg_mA.successors);
 
         KeepDoing2_yAdur.directSuccessors.add(Pos_EMPTY.directSuccessors, Neg_mA.directSuccessors);
         KeepDoing2_yAdur.successors.add(Pos_EMPTY.successors, Neg_mA.successors);
 
         EverSince_yAgel.directSuccessors.add(Pos_EMPTY.directSuccessors, Neg_mA.directSuccessors);
         EverSince_yAgel.successors.add(Pos_EMPTY.successors, Neg_mA.successors);
 
         Almost_yAyAz.directSuccessors.add(Pos_EMPTY.directSuccessors, Neg_mA.directSuccessors);
         Almost_yAyAz.successors.add(Pos_EMPTY.successors, Neg_mA.successors);
 
         Hastily_yIver.directSuccessors.add(Pos_EMPTY.directSuccessors, Neg_mA.directSuccessors);
         Hastily_yIver.successors.add(Pos_EMPTY.successors, Neg_mA.successors);
 
         Stay_yAkal.directSuccessors.add(Pos_EMPTY.directSuccessors, Neg_mA.directSuccessors);
         Stay_yAkal.successors.add(Pos_EMPTY.successors, Neg_mA.successors);
 
         Necess_mAlI.directSuccessors.add(A3sg_Verb_TEMPLATE, A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr).add(COPULAR_FORMS);
 
        Opt_yA.directSuccessors.add(A3sg_Verb_TEMPLATE, A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr).add(COPULAR_FORMS);
 
         registerForms(
                 Noun_TEMPLATE, Verb_TEMPLATE, Adj_TEMPLATE, Adv_TEMPLATE, Pnon_TEMPLATE,
                 Noun_Comp_P3sg, Noun_Comp_P3sg_Root,
 
                 Noun2Adj, Noun2Noun, Noun2Verb, Noun2VerbCopular,
                 Adj2Adj, Adj2Adv, Adj2Noun, Adj2Verb,
                 Verb2Adj, Verb2Verb, Verb2VerbCompounds, Verb2Noun,
 
                 Noun_Default, Verb_Default, Adj_Default, Resemb_Adj_ImsI, Resemb_msI,
                 Pass_InIl,
                 Nom_TEMPLATE, Dat_yA, Dat_nA, Loc_dA, Loc_ndA, Abl_dAn, Abl_ndAn, Gen_nIn,
                 Acc_yI, Acc_nI, Inst_ylA,
                 Pnon_TEMPLATE, P1sg_Im, P2sg_In, P3sg_sI, P1pl_ImIz, P2pl_InIz, P3pl_lArI,
                 Dim_cIk, Dim2_cAgIz,
                 With_lI, Without_sIz, Rel_ki, Rel_kI,
 
                 A1sg_yIm, A1sg_m, A1sg_TEMPLATE, A2sg_sIn, A2sg_n, A2sg_TEMPLATE, A2sg2_sAnA,
                 A3sg_TEMPLATE, A3sg_Verb_TEMPLATE, A2sg3_yInIz, A3sg_sIn,
                 A1pl_yIz, A1pl_k, A1pl_lIm, A1pl_TEMPLATE, A2pl_sInIz, A2pl_nIz, A2pl_yIn, A2pl_TEMPLATE, A2pl2_sAnIzA,
                 A3pl_lAr, A3pl_Verb_lAr, A3pl_sInlAr, Agt_cI, Agt_yIcI,
 
                 Ness_lIk,
                 Become_lAs, Become_Adj_lAs,
                 Resemb_ImsI, Resemb_msI, Related_sAl,
                 Aor_Ir, Aor_Ar, Aor_z, Des_sA,
                 Aor_EMPTY, AorPart_Ir, AorPart_Ar, AorPart_z, Prog_Iyor, Prog2_mAktA, Fut_yAcAk,
                 FutPart_yAcAk, Past_dI, PastPart_dIk,
                 Evid_mIs, EvidPart_mIs, PresPart_yAn, Neg_mA, Neg_m, Cond_sA,
                 Necess_mAlI, Opt_yA, Pass_In, Pass_nIl, Caus_t,
                 Caus_tIr, Imp_TEMPLATE, Recip_Is, Recip_yIs, Reflex_In, Abil_yAbil, Abil_yA, Cop_dIr,
                 PastCop_ydI, EvidCop_ymIs, CondCop_ysA, While_ken, NotState_mAzlIk, ActOf_mAcA,
                 AsIf_cAsInA, AsLongAs_dIkcA, When_yIncA, FeelLike_yAsI, SinceDoing_yAlI, ByDoing_yArAk, WithoutDoing_mAdAn,
                 WithoutDoing2_mAksIzIn, AfterDoing_yIp, UnableToDo_yAmAdAn, InsteadOfDoing_mAktAnsA,
                 KeepDoing_yAgor, KeepDoing2_yAdur, EverSince_yAgel,
                 Almost_yAyAz, Hastily_yIver, Stay_yAkal, Inf1_mAk, Inf2_mA, Inf3_yIs, Ly_cA,
                 Quite_cA, Equ_cA, Equ_ncA, UntilDoing_yAsIyA,
                 A3pl_Comp_lAr, Interj_Main, Verb_Prog_Drop, PersPron_BenSen, PersPron_BanSan,
                 Ordinal_IncI, Grouping_sAr);
 
         System.out.println(formSetLookup.size());
         //  dumpPath(Dim_cIk, 7);
 /*
         Noun_TEMPLATE.add(CASE_FORMS, POSSESSIVE_FORMS, COPULAR_FORMS, PERSON_FORMS_N)
                 .add(Dim_cIk, Dim2_cAgIz, With_lI, Without_sIz, Agt_cI, Resemb_msI,
                         Resemb_ImsI, Ness_lIk, Related_sAl, Become_lAs, Equ_cA);
         Noun_Exp_V.add(Dat_yA, Acc_yI, Gen_nIn, P1sg_Im, P2sg_In, P3sg_sI, P1pl_ImIz, P2pl_InIz, A1sg_yIm, A1pl_yIz, Resemb_ImsI);
         Noun_Exp_C.add(Noun_TEMPLATE.getSuccSetCopy()).remove(Noun_Exp_V.getSuccSetCopy());
         Noun_Comp_P3sg.add(COPULAR_FORMS, POSSESSIVE_FORMS)
                 .add(Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA)
                 .add(A1sg_yIm, A1pl_yIz, A2sg_sIn, A2pl_sInIz);
         Noun_Comp_P3sg_Root.add(With_lI, Without_sIz, Agt_cI, Resemb_msI, Resemb_ImsI, Ness_lIk, Related_sAl,
                 P1sg_Im, P2sg_In, P1pl_ImIz, P2pl_InIz, P3pl_lArI, A3pl_Comp_lAr);
 
         ProperNoun_Main
                 .add(CASE_FORMS, POSSESSIVE_FORMS, COPULAR_FORMS, PERSON_FORMS_N)
                 .add(Pl_lAr, Dim_cIk, Dim2_cAgIz, With_lI, Without_sIz, A3sg_TEMPLATE, Agt_cI, Ness_lIk);
 
         Verb_TEMPLATE.add(Prog_Iyor, Prog2_mAktA, Fut_yAcAk, Past_dI, Evid_mIs, Aor_Ir, AorPart_Ir)
                 .add(Neg_mA, Neg_m, Abil_yAbil, Abil_yA, Caus_tIr, Opt_yA, Imp_TEMPLATE, Agt_yIcI, Des_sA)
                 .add(Pass_nIl, NotState_mAzlIk, ActOf_mAcA, PastPart_dIk, EvidPart_mIs)
                 .add(FutPart_yAcAk, PresPart_yAn, AsLongAs_dIkcA)
                 .add(Inf1_mAk, Inf2_mA, Inf3_yIs)
                 .add(When_yIncA, FeelLike_yAsI, SinceDoing_yAlI, ByDoing_yArAk, WithoutDoing_mAdAn, WithoutDoing2_mAksIzIn)
                 .add(AfterDoing_yIp, When_yIncA, UnableToDo_yAmAdAn, InsteadOfDoing_mAktAnsA)
                 .add(KeepDoing2_yAdur, KeepDoing_yAgor, EverSince_yAgel, Almost_yAyAz, Hastily_yIver, Stay_yAkal, Recip_Is)
                 .add(NounDeriv_nIm, UntilDoing_yAsIyA);
 
         Verb_Exp_V.add(Opt_yA, Fut_yAcAk, Aor_Ar, AorPart_Ar, Prog_Iyor, PresPart_yAn, Pass_nIl,
                 KeepDoing2_yAdur, KeepDoing_yAgor, EverSince_yAgel, Almost_yAyAz, Hastily_yIver, Stay_yAkal,
                 When_yIncA, UnableToDo_yAmAdAn, FeelLike_yAsI, SinceDoing_yAlI, ByDoing_yArAk, Inf3_yIs, Abil_yA,
                 Abil_yAbil, AfterDoing_yIp, Agt_yIcI, FutPart_yAcAk, Imp_EMPTY_V).remove(Imp_TEMPLATE);
         Verb_Exp_C.add(Verb_TEMPLATE.getSuccSetCopy())
                 .remove(Verb_Exp_V.getSuccSetCopy()).remove(Aor_Ir, AorPart_Ir, Imp_TEMPLATE)
                 .add(Imp_EMPTY_V);
 
         Verb_Prog_Drop.add(Prog_Iyor);
 
         Adv_TEMPLATE.add(COPULAR_FORMS);
 
         PersPron_TEMPLATE.add(CASE_FORMS).add(PastCop_ydI, EvidCop_ymIs, CondCop_ysA, While_ken);
         PersPron_BenSen.add(PersPron_TEMPLATE.getSuccSetCopy()).remove(Dat_yA);
         PersPron_BanSan.add(Dat_yA);
 
         Ques_mI.add(PERSON_FORMS_N).add(Cop_dIr, EvidCop_ymIs, PastCop_ydI);
 
         Adj_TEMPLATE.add(Noun_TEMPLATE.getSuccSetCopy()).add(Ly_cA, Become_lAs, Quite_cA).remove(Related_sAl);
         Adj_Exp_C.add(Noun_Exp_C.getSuccSetCopy()).add(Ly_cA, Become_lAs, Quite_cA);
         Adj_Exp_V.add(Noun_Exp_V.getSuccSetCopy());
         Become_lAs.add(Verb_TEMPLATE.getSuccSetCopy());
         Quite_cA.add(Noun_TEMPLATE.getSuccSetCopy()).remove(Related_sAl);
 
         Numeral_Main.add(COPULAR_FORMS, CASE_FORMS, POSSESSIVE_FORMS, PERSON_FORMS_N)
                 .add(Ordinal_IncI, Grouping_sAr, With_lI, Without_sIz, Ness_lIk, Pl_lAr);
 
         Ordinal_IncI.add(Numeral_Main.getSuccSetCopy()).remove(Ordinal_IncI, Grouping_sAr);
         Grouping_sAr.add(With_lI, Ness_lIk, Abl_dAn).add(COPULAR_FORMS);
 
         Pl_lAr.add(CASE_FORMS, COPULAR_FORMS)
                 .add(P1sg_Im, P2sg_In, P1pl_ImIz, P2pl_InIz, A1pl_yIz, A2pl_sInIz, Equ_cA);
 
         P1sg_Im.add(CASE_FORMS, COPULAR_FORMS).add(A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3sg_TEMPLATE, Equ_cA);
         P2sg_In.add(CASE_FORMS, COPULAR_FORMS).add(A1sg_yIm, A1pl_yIz, A3sg_TEMPLATE, Equ_cA);
         P3sg_sI.add(COPULAR_FORMS)
                 .add(Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA)
                 .add(A1sg_yIm, A1pl_yIz, A2sg_sIn, A2pl_sInIz, A3sg_TEMPLATE, Equ_ncA);
         P1pl_ImIz.add(CASE_FORMS, COPULAR_FORMS).add(A1sg_yIm, A2sg_sIn, A3sg_TEMPLATE, Equ_cA, A2pl_sInIz);
         P2pl_InIz.add(CASE_FORMS, COPULAR_FORMS).add(A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3sg_TEMPLATE, Equ_cA);
         P3pl_lArI.add(P3sg_sI.getSuccSetCopy()).add(A1sg_yIm, A2sg_sIn, A3sg_TEMPLATE, A1pl_yIz, A2pl_sInIz, Equ_ncA);
 
         Rel_ki.add(COPULAR_FORMS, PERSON_FORMS_N).add(Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA, Pl_lAr);
         Rel_kI.add(Rel_ki.getSuccSetCopy());
         Dat_yA.add(COPULAR_FORMS);
         Dat_nA.add(COPULAR_FORMS);
 
         Loc_dA.add(COPULAR_FORMS, PERSON_FORMS_N).add(Rel_ki);
         Loc_ndA.add(COPULAR_FORMS, PERSON_FORMS_N).add(Rel_ki);
 
         Inst_ylA.add(COPULAR_FORMS, PERSON_FORMS_N, POSSESSIVE_FORMS);
 
         Abl_dAn.add(COPULAR_FORMS, PERSON_FORMS_N);
         Abl_ndAn.add(COPULAR_FORMS, PERSON_FORMS_N);
 
         Gen_nIn.add(COPULAR_FORMS, PERSON_FORMS_N).add(Rel_ki);
 
         A1sg_yIm.add(Cop_dIr);
         A2sg_sIn.add(Cop_dIr);
         A3sg_TEMPLATE.add(Cop_dIr);
         A1pl_yIz.add(Cop_dIr);
         A2pl_sInIz.add(Cop_dIr);
         A3pl_lAr.add(Pl_lAr.getSuccSetCopy());
         A3pl_Comp_lAr.add(Pl_lAr.getSuccSetCopy());
 
         Dim_cIk.add(Loc_dA, Abl_dAn, Inst_ylA, P3pl_lArI, A2sg_sIn, A2pl_sInIz, A3pl_lAr, Pl_lAr, Inst_ylA).add(COPULAR_FORMS);
         Dim2_cAgIz.add(CASE_FORMS, COPULAR_FORMS, POSSESSIVE_FORMS, PERSON_FORMS_N).add(Pl_lAr);
 
         With_lI.add(CASE_FORMS, COPULAR_FORMS, POSSESSIVE_FORMS, PERSON_FORMS_N).add(Pl_lAr, Ness_lIk, Become_lAs, Ly_cA);
         Without_sIz.add(CASE_FORMS, COPULAR_FORMS, POSSESSIVE_FORMS, PERSON_FORMS_N).add(Pl_lAr, Ness_lIk, Become_lAs, Ly_cA);
 
         Resemb_msI.add(CASE_FORMS, PERSON_FORMS_N, COPULAR_FORMS, POSSESSIVE_FORMS)
                 .add(Pl_lAr, Ness_lIk, With_lI, Without_sIz, Become_lAs);
         Resemb_ImsI.add(Resemb_ImsI.getSuccSetCopy());
 
         Ness_lIk.add(CASE_FORMS, POSSESSIVE_FORMS, COPULAR_FORMS, CASE_FORMS).add(Pl_lAr, Agt_cI, With_lI, Without_sIz, Equ_cA);
 
         Related_sAl.add(Adj_TEMPLATE.getSuccSetCopy()).remove(Dim_cIk, Dim2_cAgIz, With_lI, Without_sIz, Related_sAl,
                 Resemb_msI, Resemb_msI);
         PastCop_ydI.add(PERSON_FORMS_COP);
         EvidCop_ymIs.add(A1sg_yIm, A2sg_sIn, A3sg_TEMPLATE, A1pl_yIz, A2pl_sInIz, A3pl_lAr, AsIf_cAsInA);
         CondCop_ysA.add(PERSON_FORMS_COP);
         Cop_dIr.add(A3pl_lAr);
 
         Neg_mA.add(Aor_z, AorPart_z, Aor_EMPTY, Prog2_mAktA, Imp_TEMPLATE, Opt_yA, Des_sA,
                 Fut_yAcAk, Past_dI, Evid_mIs, Cond_sA, Abil_yAbil, Necess_mAlI, NotState_mAzlIk,
                 ActOf_mAcA, PastPart_dIk, FutPart_yAcAk, EvidPart_mIs, Agt_yIcI)
                 .add(AsLongAs_dIkcA, PresPart_yAn)
                 .add(Inf1_mAk, Inf2_mA, Inf3_yIs)
                 .add(When_yIncA, FeelLike_yAsI, SinceDoing_yAlI, ByDoing_yArAk, WithoutDoing2_mAksIzIn)
                 .add(AfterDoing_yIp, When_yIncA, InsteadOfDoing_mAktAnsA)
                 .add(KeepDoing2_yAdur, KeepDoing_yAgor, EverSince_yAgel, Hastily_yIver);
 
         Neg_m.add(Prog_Iyor);
 
         Aor_Ar.add(PERSON_FORMS_N, COPULAR_FORMS).add(Cond_sA);
         Aor_Ir.add(PERSON_FORMS_N, COPULAR_FORMS).add(Cond_sA);
         Aor_z.add(COPULAR_FORMS).add(A3sg_sIn, Cond_sA);
         Aor_EMPTY.add(A1sg_m, A1pl_yIz);
 
         Set<SuffixFormSet> noParticipleSuff =
                 Sets.newHashSet(Become_lAs, Dim_cIk, Dim2_cAgIz, With_lI, Without_sIz, Related_sAl,
                         Resemb_msI, Resemb_msI);
 
         AorPart_Ar.add(Adj_TEMPLATE.getSuccSetCopy()).remove(noParticipleSuff).add(AsIf_cAsInA);
         AorPart_Ir.add(AorPart_Ar.getSuccSetCopy());
         AorPart_z.add(AorPart_Ar.getSuccSetCopy());
 
         FutPart_yAcAk.add(Adj_Exp_C.getSuccSetCopy());
         NotState_mAzlIk.add(Adj_Exp_C.getSuccSetCopy());
 
         PresPart_yAn.add(AorPart_Ar.getSuccSetCopy());
 
         EvidPart_mIs.add(AorPart_Ar.getSuccSetCopy());
 
         PastPart_dIk.add(Adj_Exp_C.getSuccSetCopy()).remove(AsIf_cAsInA).remove(noParticipleSuff);
 
         Prog_Iyor.add(PERSON_FORMS_N, COPULAR_FORMS).add(Cond_sA);
         Prog2_mAktA.add(PERSON_FORMS_N, COPULAR_FORMS).add(Cond_sA);
 
         Fut_yAcAk.add(PERSON_FORMS_N, COPULAR_FORMS).add(Cond_sA, AsIf_cAsInA);
 
         Past_dI.add(A1sg_m, A2sg_n, A3sg_TEMPLATE, A1pl_k, A2pl_nIz, A3pl_lAr, CondCop_ysA, PastCop_ydI);
         Evid_mIs.add(PERSON_FORMS_N).add(CondCop_ysA, PastCop_ydI, EvidCop_ymIs, While_ken, Cop_dIr);
 
         Cond_sA.add(A1sg_m, A2sg_n, A3sg_TEMPLATE, A1pl_k, A2pl_nIz, A3pl_lAr, PastCop_ydI, EvidCop_ymIs);
 
         Imp_TEMPLATE.add(A2sg_TEMPLATE, A2sg2_sAnA, A2sg3_yInIz, A2pl2_sAnIzA, A2pl_yIn, A3sg_sIn, A3pl_sInlAr);
         Imp_EMPTY_C.add(A2sg_TEMPLATE, A2sg2_sAnA, A2pl2_sAnIzA, A3sg_sIn, A3pl_sInlAr);
         Imp_EMPTY_V.add(A2sg3_yInIz, A2pl_yIn);
         Agt_cI.add(CASE_FORMS, PERSON_FORMS_N, POSSESSIVE_FORMS, COPULAR_FORMS).add(Pl_lAr, Become_lAs, With_lI, Without_sIz, Ness_lIk);
         Agt_yIcI.add(Agt_cI.getSuccSetCopy());
 
         Abil_yAbil.add(Verb_TEMPLATE.getSuccSetCopy()).remove(Abil_yAbil, Abil_yA, Neg_mA, Pass_nIl).add(Cond_sA, Pass_In);
         Abil_yA.add(Neg_mA, Neg_m);
 
         Opt_yA.add(A1sg_yIm, A2sg_sIn, A3sg_TEMPLATE, A1pl_lIm, A2pl_sInIz, A3pl_lAr);
         Des_sA.add(COPULAR_FORMS).add(A1sg_m, A2sg_n, A3sg_TEMPLATE, A1pl_k, A2pl_nIz, A3pl_lAr, PastCop_ydI, EvidCop_ymIs);
 
         Caus_t.add(Verb_TEMPLATE.getSuccSetCopy()).add(Pass_nIl);
         Caus_tIr.add(Verb_TEMPLATE.getSuccSetCopy()).remove(Caus_tIr).add(Caus_t, Pass_nIl);
 
         Pass_nIl.add(Verb_TEMPLATE.getSuccSetCopy()).remove(Pass_In, Pass_nIl, Caus_tIr).add(Caus_t);
         Pass_In.add(Verb_TEMPLATE.getSuccSetCopy()).remove(Pass_In);
 
         Reflex_In.add(Verb_TEMPLATE.getSuccSetCopy());
         Recip_Is.add(Verb_TEMPLATE.getSuccSetCopy()).remove(Recip_Is);
         Recip_yIs.add(Verb_TEMPLATE.getSuccSetCopy()).remove(Recip_Is);
 
         Inf1_mAk.add(COPULAR_FORMS).add(Abl_dAn, Loc_dA, Inst_ylA);
         Inf2_mA.add(Noun_TEMPLATE.getSuccessors());
         Inf3_yIs.add(Noun_TEMPLATE.getSuccessors());
 
         NounDeriv_nIm.add(Noun_TEMPLATE.getSuccSetCopy());
 
         When_yIncA.add(Dat_yA);
         While_ken.add(Rel_ki).add(PastCop_ydI, EvidCop_ymIs, CondCop_ysA);
         FeelLike_yAsI.add(POSSESSIVE_FORMS).add(COPULAR_FORMS);
 
         KeepDoing_yAgor.add(Neg_mA.getSuccSetCopy()).remove(Aor_z).add(Neg_mA, Neg_m, Aor_Ir, Prog_Iyor);
         KeepDoing2_yAdur.add(KeepDoing_yAgor.getSuccSetCopy());
         EverSince_yAgel.add(KeepDoing_yAgor.getSuccSetCopy());
         Almost_yAyAz.add(KeepDoing_yAgor.getSuccSetCopy());
         Hastily_yIver.add(KeepDoing_yAgor.getSuccSetCopy());
         Stay_yAkal.add(KeepDoing_yAgor.getSuccSetCopy());
         Necess_mAlI.add(COPULAR_FORMS, PERSON_FORMS_N);
 */
     }
 
     @Override
     public SuffixFormSet getRootSet(DictionaryItem item, SuffixData successorConstraint) {
         if (successorConstraint.isEmpty()) {
             switch (item.primaryPos) {
                 case Noun:
                     return Noun_Default;
                 case Adjective:
                     return Adj_Default;
                 case Verb:
                     return Verb_Default;
                 case Adverb:
                     return Adv_Default;
                 default:
                     return Noun_Default;
             }
         } else {
             SuffixFormSet template;
             switch (item.primaryPos) {
                 case Noun:
                     template = Noun_TEMPLATE;
                     break;
                 case Adjective:
                     template = Adj_TEMPLATE;
                     break;
                 case Verb:
                     template = Verb_TEMPLATE;
                     break;
                 default:
                     template = Noun_TEMPLATE;
             }
             SuffixFormSet copy = generateSetFromTemplate(template, successorConstraint);
             registerForm(copy);
             return copy;
         }
     }
 
     @Override
     public SuffixData[] defineSuccessorSuffixes(DictionaryItem item) {
         SuffixData original = new SuffixData();
         SuffixData modified = new SuffixData();
 
         PrimaryPos primaryPos = item.primaryPos;
 
         switch (primaryPos) {
             case Noun:
                 getForNoun(item, original, modified);
                 break;
             case Verb:
                 getForVerb(item, original, modified);
                 break;
             default:
                 getForNoun(item, original, modified);
         }
         return new SuffixData[]{original, modified};
     }
 
 
     private void getForNoun(DictionaryItem item, SuffixData original, SuffixData modified) {
 
         for (RootAttr attribute : item.attrs.getAsList(RootAttr.class)) {
             switch (attribute) {
                 case CompoundP3sg:
                     original.add(Noun_Comp_P3sg.allSuccessors());
                     modified.add(Noun_Comp_P3sg_Root.allSuccessors());
                     break;
                 default:
                     break;
             }
         }
     }
 
     private void getForVerb(DictionaryItem item, SuffixData original, SuffixData modified) {
         original.add(Verb_TEMPLATE.allSuccessors().remove(Caus_t));
         modified.add(Verb_TEMPLATE.allSuccessors().remove(Caus_t));
         List<RootAttr> attrList = item.attrs.getAsList(RootAttr.class);
         for (RootAttr attribute : attrList) {
             switch (attribute) {
                 case Aorist_A:
                     original.add(Aor_Ar, AorPart_Ar);
                     original.remove(Aor_Ir, AorPart_Ir);
                     if (!item.attrs.contains(RootAttr.ProgressiveVowelDrop)) {
                         modified.add(Aor_Ar, AorPart_Ar);
                         modified.remove(Aor_Ir, AorPart_Ir);
                     }
                     break;
                 case Aorist_I:
                     original.add(Aor_Ir, AorPart_Ir);
                     original.remove(Aor_Ar, AorPart_Ar);
                     if (!item.attrs.contains(RootAttr.ProgressiveVowelDrop)) {
                         modified.add(Aor_Ir, AorPart_Ir);
                         modified.remove(Aor_Ar, AorPart_Ar);
                     }
                     break;
                 case Passive_In:
                     original.add(Pass_In);
                     original.add(Pass_InIl);
                     original.remove(Pass_nIl);
                     break;
                 case LastVowelDrop:
                     original.remove(Pass_nIl);
                     modified.clear().add(Pass_nIl, Verb2Verb);
                     break;
 /*                    case VoicingOpt:
                         modified.remove(Verb_Exp_C.getSuccessors());
                         break;*/
                 case ProgressiveVowelDrop:
                     original.remove(Prog_Iyor);
                     modified.clear().add(Pos_EMPTY, Prog_Iyor);
                     break;
                 case NonTransitive:
                     original.remove(Caus_t, Caus_tIr);
                     modified.remove(Caus_t, Caus_tIr);
                     break;
                 case Reflexive:
                     original.add(Reflex_In);
                     modified.add(Reflex_In);
                     break;
                 case Reciprocal:
                     original.add(Recip_Is);
                     modified.add(Recip_Is);
                     break;
                 case Causative_t:
                     original.remove(Caus_tIr);
                     original.add(Caus_t);
                     if (!item.attrs.contains(RootAttr.ProgressiveVowelDrop)) {
                         modified.remove(Caus_tIr);
                         modified.add(Caus_t);
                     }
                     break;
                 default:
                     break;
             }
         }
     }
 }
