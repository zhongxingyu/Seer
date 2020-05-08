 package zemberek3.lexicon.tr;
 
 import zemberek3.lexicon.*;
 import zemberek3.lexicon.graph.DynamicSuffixProvider;
 import zemberek3.lexicon.graph.SuffixData;
 import zemberek3.lexicon.graph.TerminationType;
 
 import java.util.List;
 
 public class TurkishSuffixes extends DynamicSuffixProvider {
 
     // ------------ case suffixes ---------------------------
 
     public Suffix Dat = registerSuffix("Dat");
     public SuffixForm Dat_yA = getForm(Dat, "+yA");
     public SuffixForm Dat_nA = getForm(Dat, "nA");
 
     public Suffix Loc = registerSuffix("Loc");
     public SuffixForm Loc_dA = getForm(Loc, ">dA");
     public SuffixForm Loc_ndA = getForm(Loc, "ndA");
 
     public Suffix Abl = registerSuffix("Abl");
     public SuffixForm Abl_dAn = getForm(Abl, ">dAn");
     public SuffixForm Abl_ndAn = getForm(Abl, "ndAn");
 
     public Suffix Gen = registerSuffix("Gen");
     public SuffixForm Gen_nIn = getForm(Gen, "+nIn");
     public SuffixForm Gen_Im = getForm("Gen_Im", Gen, "+Im"); // benim, bizim
 
     public Suffix Acc = registerSuffix("Acc");
     public SuffixForm Acc_yI = getForm(Acc, "+yI");
     public SuffixForm Acc_nI = getForm(Acc, "nI");
 
     public Suffix Inst = registerSuffix("Inst");
     public SuffixForm Inst_ylA = getForm(Inst, "+ylA");
 
     public Suffix Nom = registerSuffix("Nom");
     public SuffixFormTemplate Nom_TEMPLATE = getTemplate("Nom_TEMPLATE", Nom);
 
     // ----------------- possesive ----------------------------
 
     public Suffix Pnon = registerSuffix("Pnon");
     public SuffixFormTemplate Pnon_TEMPLATE = getTemplate("Pnon_TEMPLATE", Pnon);
 
     public Suffix P1sg = registerSuffix("P1sg");
     public SuffixForm P1sg_Im = getForm(P1sg, "Im");
 
     public Suffix P2sg = registerSuffix("P2sg");
     public SuffixForm P2sg_In = getForm(P2sg, "In");
 
     public Suffix P3sg = registerSuffix("P3sg");
     public SuffixForm P3sg_sI = getForm(P3sg, "+sI");
 
     public Suffix P1pl = registerSuffix("P1pl");
     public SuffixForm P1pl_ImIz = getForm(P1pl, "ImIz");
 
     public Suffix P2pl = registerSuffix("P2pl");
     public SuffixForm P2pl_InIz = getForm(P2pl, "InIz");
 
     public Suffix P3pl = registerSuffix("P3pl");
     public SuffixForm P3pl_lArI = getForm(P3pl, "lArI");
 
     // -------------- Number-Person agreement --------------------
 
     public Suffix A1sg = registerSuffix("A1sg");
     public SuffixForm A1sg_yIm = getForm(A1sg, "+yIm"); // gel-e-yim
     public SuffixForm A1sg_m = getForm(A1sg, "m"); // gel-se-m
     public SuffixFormTemplate A1sg_TEMPLATE = getTemplate("A1sg_TEMPLATE", A1sg); // ben
 
     public Suffix A2sg = registerSuffix("A2sg");
     public SuffixForm A2sg_sIn = getForm(A2sg, "sIn"); // gel-ecek-sin
     public SuffixForm A2sg_n = getForm(A2sg, "n"); // gel-di-n
     public SuffixFormTemplate A2sg_TEMPLATE = getTemplate("A2sg_TEMPLATE", A2sg); // gel, sen,..
     public SuffixForm A2sg2_sAnA = getForm(A2sg, "sAnA"); //gel-sene
     public SuffixForm A2sg3_yInIz = getForm(A2sg, "+yInIz"); //gel-iniz
 
     public Suffix A3sg = registerSuffix("A3sg");
     public SuffixFormTemplate A3sg_TEMPLATE = getTemplate("A3sg_TEMPLATE", A3sg); // gel-di-, o-
     public SuffixFormTemplate A3sg_Verb_TEMPLATE = getTemplate("A3sg_Verb_TEMPLATE", A3sg); // gel-di-, o-
     public SuffixForm A3sg_sIn = getForm(A3sg, "sIn"); // gel-sin
 
     public Suffix A1pl = registerSuffix("A1pl");
     public SuffixForm A1pl_yIz = getForm(A1pl, "+yIz"); // geliyor-uz
     public SuffixForm A1pl_k = getForm(A1pl, "k"); // gel-di-k
     public SuffixForm A1pl_lIm = getForm(A1pl, "lIm"); // gel-e-lim
     public SuffixFormTemplate A1pl_TEMPLATE = getTemplate("A1pl_TEMPLATE", A1pl); // biz
 
     public Suffix A2pl = registerSuffix("A2pl");
     public SuffixForm A2pl_sInIz = getForm(A2pl, "sInIz"); // gel-ecek-siniz
     public SuffixForm A2pl_nIz = getForm(A2pl, "nIz"); // gel-di-niz
     public SuffixForm A2pl_yIn = getForm(A2pl, "+yIn"); // gel-me-yin
     public SuffixFormTemplate A2pl_TEMPLATE = getTemplate("A2pl_TEMPLATE", A2pl); // gel-e-lim
 
     public Suffix A2pl2 = registerSuffix("A2pl2");
     public SuffixForm A2pl2_sAnIzA = getForm(A2pl2, "sAnIzA"); // gel-senize
 
     public Suffix A3pl = registerSuffix("A3pl");
     public SuffixForm A3pl_lAr = getForm(A3pl, "lAr"); // gel-ecek-ler
     public SuffixForm A3pl_Verb_lAr_After_Tense = getForm("A3pl_Verb_lAr_After_Tense", A3pl, "lAr"); // gel-ecek-ler
     public SuffixForm A3pl_Verb_lAr = getForm("A3pl_Verb_lAr", A3pl, "lAr"); // gel-ecek-ler
     public SuffixForm A3pl_Comp_lAr = getForm("A3pl_Comp_lAr", A3pl, "lAr", TerminationType.NON_TERMINAL); //zeytinyağlarımız
     public SuffixForm A3pl_sInlAr = getForm(A3pl, "sInlAr"); // gel-sinler
 
     // ------------ derivatioonal ----------------------
 
     public Suffix Dim = registerSuffix("Dim");
     public SuffixForm Dim_cIk = getForm(Dim, ">cI~k");
 
     public Suffix Dim2 = registerSuffix("Dim2");
     public SuffixForm Dim2_cAgIz = getForm(Dim2, "cAğIz");
 
     public Suffix With = registerSuffix("With");
     public SuffixForm With_lI = getForm(With, "lI");
 
     public Suffix Without = registerSuffix("Without");
     public SuffixForm Without_sIz = getForm(Without, "sIz");
 
     public Suffix Rel = registerSuffix("Rel");
     public SuffixForm Rel_ki = getForm(Rel, "ki"); // masa-da-ki
     public SuffixForm Rel_kI = getForm(Rel, "kI"); // dünkü
 
     public Suffix Agt = registerSuffix("Agt");
     public SuffixForm Agt_cI = getForm(Agt, ">cI"); // araba-cı. Converts to another Noun.
     public SuffixForm Agt_yIcI_2Noun = getForm("Agt_yIcI_2Noun", Agt, "+yIcI"); // otur-ucu. converts to both Noun and Adj
     public SuffixForm Agt_yIcI_2Adj = getForm("Agt_yIcI_2Adj", Agt, "+yIcI"); // otur-ucu. converts to both Noun and Adj
 
     public Suffix Ness = registerSuffix("Ness");
     public SuffixForm Ness_lIk = getForm(Ness, "lI~k");
 
     public Suffix FitFor = registerSuffix("FitFor");
     public SuffixForm FitFor_lIk = getForm(FitFor, "lI~k");
 
 
     public Suffix Become = registerSuffix("Become");
     public SuffixForm Become_lAs = getForm(Become, "lAş");
     public SuffixForm Become_Adj_lAs = getForm("Become_Adj_lAs", Become, "lAş");
 
     public Suffix Acquire = registerSuffix("Acquire");
     public SuffixForm Acquire_lAn = getForm(Acquire, "lAn");
 
     public Suffix Resemb = registerSuffix("Resemb");
     public SuffixForm Resemb_ImsI = getForm(Resemb, "ImsI"); // udunumsu
     public SuffixForm Resemb_msI = getForm(Resemb, "+msI"); // odunsu
 
     public SuffixForm Resemb_Adj_ImsI = getForm("Resemb_Adj_ImsI", Resemb, "ImsI"); // udunumsu
     public SuffixForm Resemb_Adj_msI = getForm("Resemb_Adj_msI", Resemb, "+msI"); // odunsu
 
     public Suffix Related = registerSuffix("Related");
     public SuffixForm Related_sAl = getForm(Related, "sAl");
 
     // ----------------------------  verbal tense --------------------------------
 
     public Suffix Aor = registerSuffix("Aor");
     public SuffixForm Aor_Ir = getForm(Aor, "+Ir"); //gel-ir
     public SuffixForm Aor_Ar = getForm(Aor, "+Ar"); //ser-er
     public SuffixForm Aor_z = getForm(Aor, "z"); // gel-me-z
     public SuffixFormTemplate Aor_EMPTY = getTemplate("Aor_EMPTY", Aor, TerminationType.NON_TERMINAL); // gel-me--yiz
 
     public Suffix Prog = registerSuffix("Prog");
     public SuffixForm Prog_Iyor = getForm(Prog, "Iyor");
 
     public Suffix Prog2 = registerSuffix("Prog2");
     public SuffixForm Prog2_mAktA = getForm(Prog2, "mAktA");
 
     public Suffix Fut = registerSuffix("Fut");
     public SuffixForm Fut_yAcAk = getForm(Fut, "+yAcA~k");
 
     public Suffix Past = registerSuffix("Past");
     public SuffixForm Past_dI = getForm(Past, ">dI");
 
     public Suffix Evid = registerSuffix("Evid");
     public SuffixForm Evid_mIs = getForm(Evid, "mIş");
 
     // ---------------------------------------------------
 
     public Suffix PastPart = registerSuffix("PastPart");
     public SuffixForm PastPart_dIk_2Noun = getForm("PastPart_dIk_2Noun", PastPart, ">dI~k");
     public SuffixForm PastPart_dIk_2Adj = getForm("PastPart_dIk_2Noun", PastPart, ">dI~k");
 
     public Suffix AorPart = registerSuffix("AorPart"); // convert to an Adjective
     public SuffixForm AorPart_Ir_2Adj = getForm("AorPart_Ir_2Adj", AorPart, "+Ir"); //gel-ir
     public SuffixForm AorPart_Ar_2Adj = getForm("AorPart_Ar_2Adj", AorPart, "+Ar"); //ser-er
     public SuffixForm AorPart_z_2Adj = getForm("AorPart_z_2Noun", AorPart, "z"); // gel-me-z
 
     public Suffix FutPart = registerSuffix("FutPart");
     public SuffixForm FutPart_yAcAk_2Adj = getForm("FutPart_yAcAk_2Adj", FutPart, "+yAcA~k");
     public SuffixForm FutPart_yAcAk_2Noun = getForm("FutPart_yAcAk_2Noun", FutPart, "+yAcA~k");
 
     public Suffix EvidPart = registerSuffix("EvidPart");
     public SuffixForm EvidPart_mIs_2Noun = getForm("EvidPart_mIs_2Noun", EvidPart, "mIş");
     public SuffixForm EvidPart_mIs_2Adj = getForm("EvidPart_mIs_2Adj", EvidPart, "mIş");
 
     public Suffix PresPart = registerSuffix("PresPart");
     public SuffixForm PresPart_yAn = getForm(PresPart, "+yAn");
 
     public Suffix Pos = registerSuffix("Pos");
     public SuffixFormTemplate Pos_EMPTY = getTemplate("Pos_EMPTY", Pos); // Verb Positive Null Morpheme template.
 
     public Suffix Neg = registerSuffix("Neg");
     public SuffixForm Neg_mA = getForm(Neg, "mA"); //gel-me
     public SuffixForm Neg_m = getForm("Neg_m", Neg, "m", TerminationType.NON_TERMINAL); // gel-m-iyor
 
     //TODO: May be redundant. Cond_Cop may suffice
     public Suffix Cond = registerSuffix("Cond");
     public SuffixForm Cond_sA = getForm(Cond, "sA");
 
     public Suffix Necess = registerSuffix("Necess");
     public SuffixForm Necess_mAlI = getForm(Necess, "mAlI");
 
     public Suffix Opt = registerSuffix("Opt");
     public SuffixForm Opt_yA = getForm(Opt, "+yA");
 
     public Suffix Pass = registerSuffix("Pass");
     public SuffixForm Pass_In = getForm(Pass, "+In");
     public SuffixForm Pass_InIl = getForm(Pass, "+InIl");
     public SuffixForm Pass_nIl = getForm(Pass, "+nIl");
 
     public Suffix Caus = registerSuffix("Caus");
     public SuffixForm Caus_t = getForm(Caus, "t");
     public SuffixForm Caus_tIr = getForm(Caus, ">dIr");
 
     public Suffix Imp = registerSuffix("Imp");
     public SuffixFormTemplate Imp_TEMPLATE = getTemplate("Imp_TEMPLATE", Imp);
 
     public Suffix Des = registerSuffix("Des");
     public SuffixForm Des_sA = getForm(Des, "sA");
 
     public Suffix Recip = registerSuffix("Recip");
     public SuffixForm Recip_Is = getForm(Recip, "+Iş");
     public SuffixForm Recip_yIs = getForm(Recip, "+yIş");
 
     public Suffix Reflex = registerSuffix("Reflex");
     public SuffixForm Reflex_In = getForm(Reflex, "+In");
 
     public Suffix Abil = registerSuffix("Abil");
     public SuffixForm Abil_yAbil = getForm(Abil, "+yAbil");
     public SuffixForm Abil_yA = getForm("Abil_yA", Abil, "+yA", TerminationType.NON_TERMINAL);
 
     public Suffix Cop = registerSuffix("Cop");
     public SuffixForm Cop_dIr = getForm(Cop, ">dIr");
 
     public SuffixForm PastCop_ydI = getForm("PastCop_ydI", Past, "+y>dI");
 
     public SuffixForm EvidCop_ymIs = getForm("EvidCop_ymIs", Evid, "+ymIş");
 
     public SuffixForm CondCop_ysA = getForm("CondCop_ysA", Cond, "+ysA");
 
     public Suffix While = registerSuffix("While");
     public SuffixForm While_ken = getForm(While, "+yken");
 
     public Suffix Pres = registerSuffix("Pres");  // Present tense only appears after a zero morpheme verb derivation such as "kalemdir"
     public SuffixFormTemplate Pres_TEMPLATE = getTemplate("Pres_TEMPLATE", Pres);
 
     public Suffix Equ = registerSuffix("Equ");
     public SuffixForm Equ_cA = getForm(Equ, ">cA");
     public SuffixForm Equ_ncA = getForm(Equ, "ncA");
 
     public Suffix NotState = registerSuffix("NotState");
     public SuffixForm NotState_mAzlIk = getForm(NotState, "mAzlI~k");
 
     public Suffix ActOf = registerSuffix("ActOf");
     public SuffixForm ActOf_mAcA = getForm(ActOf, "mAcA");
 
     public Suffix AsIf = registerSuffix("AsIf");
     public SuffixForm AsIf_cAsInA = getForm(AsIf, ">cAsInA");
 
     // Converts to an Adverb.
     public Suffix AsLongAs = registerSuffix("AsLongAs");
     public SuffixForm AsLongAs_dIkcA = getForm(AsLongAs, ">dIkçA");
 
     public Suffix When = registerSuffix("When");
     public SuffixForm When_yIncA = getForm(When, "+yIncA");
 
     // It also may have "worthy of doing" meaning after passive. Converts to an Adjective.
     public Suffix FeelLike = registerSuffix("FeelLike");
     public SuffixForm FeelLike_yAsI_2Noun = getForm("FeelLike_yAsI_2Noun", FeelLike, "+yAsI");
     public SuffixForm FeelLike_yAsI_2Adj = getForm("FeelLike_yAsI_2Adj", FeelLike, "+yAsI");
 
     // Converts to an Adverb.
     public Suffix SinceDoing = registerSuffix("SinceDoing");
     public SuffixForm SinceDoing_yAlI = getForm(SinceDoing, "+yAlI");
 
     // Converts to an Adverb.
     public Suffix ByDoing = registerSuffix("ByDoing");
     public SuffixForm ByDoing_yArAk = getForm(ByDoing, "+yArAk");
 
     // Converts to an Adverb.
     // TODO: this should have a Neg_null effect
     public Suffix WithoutDoing = registerSuffix("WithoutDoing");
     public SuffixForm WithoutDoing_mAdAn = getForm(WithoutDoing, "mAdAn");
 
     // Converts to an Adverb.
     public Suffix UntilDoing = registerSuffix("UntilDoing");
     public SuffixForm UntilDoing_yAsIyA = getForm(UntilDoing, "+yAsIyA");
 
 
     public Suffix WithoutDoing2 = registerSuffix("WithoutDoing2");
     public SuffixForm WithoutDoing2_mAksIzIn = getForm(WithoutDoing2, "mAksIzIn");
 
     // Converts to an Adverb.
     public Suffix AfterDoing = registerSuffix("AfterDoing");
     public SuffixForm AfterDoing_yIp = getForm(AfterDoing, "+yIp");
 
     public Suffix UnableToDo = registerSuffix("UnableToDo");
     public SuffixForm UnableToDo_yAmAdAn = getForm(UnableToDo, "+yAmAdAn");
 
     public Suffix InsteadOfDoing = registerSuffix("InsteadOfDoing");
     public SuffixForm InsteadOfDoing_mAktAnsA = getForm(InsteadOfDoing, "mAktAnsA");
 
     // Converts to an Adverb.
     public Suffix KeepDoing = registerSuffix("KeepDoing");
     public SuffixForm KeepDoing_yAgor = getForm(KeepDoing, "+yAgör");
 
     public Suffix KeepDoing2 = registerSuffix("KeepDoing2");
     public SuffixForm KeepDoing2_yAdur = getForm(KeepDoing2, "+yAdur");
 
     public Suffix EverSince = registerSuffix("EverSince");
     public SuffixForm EverSince_yAgel = getForm(EverSince, "+yAgel");
 
     public Suffix Almost = registerSuffix("Almost");
     public SuffixForm Almost_yAyAz = getForm(Almost, "+yAyaz");
 
     public Suffix Hastily = registerSuffix("Hastily");
     public SuffixForm Hastily_yIver = getForm(Hastily, "+yIver");
 
     public Suffix Stay = registerSuffix("Stay");
     public SuffixForm Stay_yAkal = getForm(Stay, "+yAkal");
 
     public Suffix Inf1 = registerSuffix("Inf1");
     public SuffixForm Inf1_mAk = getForm(Inf1, "mAk");
 
     //TODO: consider -maca inf suffix.
     //public SuffixForm Inf1_mAcA = getForm(Inf1, "mAcA");
 
     public Suffix Inf2 = registerSuffix("Inf2");
     public SuffixForm Inf2_mA = getForm(Inf2, "mA");
 
     public Suffix Inf3 = registerSuffix("Inf3");
     public SuffixForm Inf3_yIs = getForm(Inf3, "+yIş");
 
     // TODO is below a valid morpheme? 
 /*    public Suffix NounDeriv = registerSuffix("NounDeriv");
     public SuffixForm NounDeriv_nIm = getForm(NounDeriv, "+nIm");*/
 
     public Suffix Ly = registerSuffix("Ly");
     public SuffixForm Ly_cA = getForm(Ly, ">cA");
 
     public Suffix Quite = registerSuffix("Quite");
     public SuffixForm Quite_cA = getForm(Quite, ">cA");
 
     public Suffix Ordinal = registerSuffix("Ordinal");
     public SuffixForm Ordinal_IncI = getForm(Ordinal, "+IncI");
 
     public Suffix Grouping = registerSuffix("Grouping");
     public SuffixForm Grouping_sAr = getForm(Grouping, "+şAr");
 
     public Suffix NounRoot = registerSuffix("Noun");
     public SuffixFormTemplate Noun_TEMPLATE = getTemplate("Noun_TEMPLATE", NounRoot);
     public SuffixFormTemplate Noun2Noun = getTemplate("Noun2Noun", NounRoot, TerminationType.NON_TERMINAL);
     public SuffixFormTemplate Adj2Noun = getTemplate("Adj2Noun", NounRoot, TerminationType.NON_TERMINAL);
     public SuffixFormTemplate Adv2Noun = getTemplate("Adv2Noun", NounRoot, TerminationType.NON_TERMINAL);
     public SuffixFormTemplate Verb2Noun = getTemplate("Verb2Noun", NounRoot, TerminationType.NON_TERMINAL);
     public SuffixFormTemplate Verb2NounPart = getTemplate("Verb2NounPart", NounRoot, TerminationType.NON_TERMINAL);
 
     public NullSuffixForm Noun_Default = getNull("Noun_Default", Noun_TEMPLATE);
     public SuffixFormTemplate Noun_Comp_P3sg = getTemplate("Noun_Comp_P3sg", NounRoot);
     public SuffixFormTemplate Noun_Comp_P3sg_Root = getTemplate("Noun_Comp_P3sg_Root", NounRoot);
 
     public Suffix AdjRoot = registerSuffix("Adj");
     public SuffixFormTemplate Adj_TEMPLATE = getTemplate("Adj_TEMPLATE", AdjRoot);
     public SuffixFormTemplate Noun2Adj = getTemplate("Noun2Adj", AdjRoot, TerminationType.NON_TERMINAL);
     public SuffixFormTemplate Adj2Adj = getTemplate("Adj2Adj", AdjRoot, TerminationType.NON_TERMINAL);
     public SuffixFormTemplate Adv2Adj = getTemplate("Adv2Adj", AdjRoot, TerminationType.NON_TERMINAL);
     public SuffixFormTemplate Verb2Adj = getTemplate("Verb2Adj", AdjRoot, TerminationType.NON_TERMINAL);
     public SuffixFormTemplate Verb2AdjPart = getTemplate("Verb2AdjPart", AdjRoot, TerminationType.NON_TERMINAL);
     public SuffixFormTemplate Adj_Main_Rel = getTemplate("Adj_TEMPLATE", AdjRoot);
     public NullSuffixForm Adj_Default = getNull("Adj_Default", Adj_TEMPLATE, TerminationType.TERMINAL);
 
     public Suffix AdvRoot = registerSuffix("Adv");
 
     public SuffixFormTemplate Adv_TEMPLATE = getTemplate("Adv_TEMPLATE", AdvRoot);
     public SuffixFormTemplate Adj2Adv = getTemplate("Adj2Adv", AdvRoot, TerminationType.NON_TERMINAL);
     public SuffixFormTemplate Verb2Adv = getTemplate("Verb2Adv", AdvRoot, TerminationType.NON_TERMINAL);
     public NullSuffixForm Adv_Default = getNull("Adv_Default", Adv_TEMPLATE);
 
     public Suffix InterjRoot = registerSuffix("Interj");
     public SuffixFormTemplate Interj_Template = getTemplate("Interj_Template", InterjRoot);
     public NullSuffixForm Interj_Default = getNull("Interj_Default", Interj_Template);
 
     public Suffix ConjRoot = registerSuffix("Conj");
     public SuffixFormTemplate Conj_Template = getTemplate("Conj_Template", ConjRoot);
     public NullSuffixForm Conj_Default = getNull("Conj_Default", Conj_Template);
 
 
     public Suffix NumeralRoot = registerSuffix("Numeral");
     public SuffixFormTemplate Numeral_Template = getTemplate("Numeral_Template", NumeralRoot);
     public NullSuffixForm Numeral_Default = getNull("Numeral_Default", Numeral_Template);
 
     public Suffix DetRoot = registerSuffix("Det");
     public SuffixFormTemplate Det_Template = getTemplate("Det_Template", DetRoot);
     public NullSuffixForm Det_Default = getNull("Det_Default", Det_Template);
 
 
     public Suffix ProperNounRoot = registerSuffix("ProperNoun");
     public SuffixForm ProperNoun_Template = getTemplate("ProperNoun_Template", ProperNounRoot);
 
     public Suffix VerbRoot = registerSuffix("Verb");
     public SuffixFormTemplate Verb_TEMPLATE = getTemplate("Verb_TEMPLATE", VerbRoot);
     public SuffixFormTemplate Adj2Verb = getTemplate("Adj2Verb", VerbRoot, TerminationType.NON_TERMINAL);
     public SuffixFormTemplate Noun2Verb = getTemplate("Noun2Verb", VerbRoot, TerminationType.NON_TERMINAL);
     public SuffixFormTemplate Noun2VerbCopular = getTemplate("Noun2VerbCopular", VerbRoot, TerminationType.NON_TERMINAL);
     public SuffixFormTemplate Verb2Verb = getTemplate("Verb2Verb", VerbRoot, TerminationType.NON_TERMINAL);
     public SuffixFormTemplate Verb2VerbAbility = getTemplate("Verb2VerbAbility", VerbRoot, TerminationType.NON_TERMINAL);
     public SuffixFormTemplate Verb2VerbCompounds = getTemplate("Verb2VerbCompounds", VerbRoot, TerminationType.NON_TERMINAL);
     public NullSuffixForm Verb_Default = getNull("Verb_Default", Verb_TEMPLATE);
     public NullSuffixForm Verb_De = getNull("Verb_De", Verb_TEMPLATE);
     public NullSuffixForm Verb_Di = getNull("Verb_Di", Verb_TEMPLATE);
     public NullSuffixForm Verb_Ye = getNull("Verb_Ye", Verb_TEMPLATE);
     public NullSuffixForm Verb_Yi = getNull("Verb_Yi", Verb_TEMPLATE);
 
     public SuffixForm Verb_De_Ye_Prog = getNull("Verb_De_Ye_Prog", Verb_TEMPLATE);
     public SuffixForm Verb_Prog_Drop = getNull("Verb_Prog_Drop", Verb_TEMPLATE);
 
     public Suffix PersPronRoot = registerSuffix("PersPron");
     public Suffix DemonsPronRoot = registerSuffix("DemonsPron");
     public SuffixFormTemplate PersPron_TEMPLATE = getTemplate("PersPron_TEMPLATE", PersPronRoot);
     public NullSuffixForm PersPron_Default = getNull("PersPron_Default", PersPron_TEMPLATE);
     public SuffixFormTemplate PersPron_BenSen = getTemplate("PersPron_BenSen", PersPronRoot);
     public SuffixFormTemplate PersPron_BanSan = getTemplate("PersPron_BanSan", PersPronRoot);
     public SuffixFormTemplate DemonsPron_TEMPLATE = getTemplate("DemonsPron_TEMPLATE", DemonsPronRoot);
     public NullSuffixForm DemonsPron_Default = getNull("DemonsPron_Default", DemonsPron_TEMPLATE);
 
     public Suffix QuesRoot = registerSuffix("Ques");
     public SuffixFormTemplate Ques_Template = getTemplate("Ques_Template", QuesRoot);
     public NullSuffixForm Ques_Default = getNull("Ques_Default", Ques_Template);
 
     // TODO: particle may not be used in the system, it is a too broad POS
     public Suffix ParticleRoot = registerSuffix("Particle");
     public SuffixFormTemplate Particle_Main = getTemplate("Particle_Main", ParticleRoot);
 
     // TODO: add time root. (with Rel_ki + Noun)
     public final SuffixForm[] CASE_FORMS = {Nom_TEMPLATE, Dat_yA, Loc_dA, Abl_dAn, Gen_nIn, Acc_yI, Inst_ylA, Equ_cA};
 
     public final SuffixForm[] POSSESSIVE_FORMS = {Pnon_TEMPLATE, P1sg_Im, P2sg_In, P3sg_sI, P1pl_ImIz, P2pl_InIz, P3pl_lArI};
     public final SuffixForm[] PERSON_FORMS_N = {A1sg_yIm, A2sg_sIn, A3sg_TEMPLATE, A1pl_yIz, A2pl_sInIz, A3pl_lAr};
     public final SuffixForm[] PERSON_FORMS_COP = {A1sg_m, A2sg_n, A3sg_TEMPLATE, A1pl_k, A2pl_nIz, A3pl_lAr};
     public final SuffixForm[] COPULAR_FORMS = {Cop_dIr, PastCop_ydI, EvidCop_ymIs, CondCop_ysA, While_ken};
 
     public TurkishSuffixes() {
 
         //---------------------------- Root and Derivation Morphemes ---------------------------------------------------
 
         // noun template. it has all possible suffix forms that a noun can have
         Noun_TEMPLATE.connections.add(A3pl_lAr, A3pl_Comp_lAr, A3sg_TEMPLATE);
         Noun_TEMPLATE.indirectConnections
                 .add(POSSESSIVE_FORMS, CASE_FORMS)
                 .add(Cop_dIr, PastCop_ydI, EvidCop_ymIs, CondCop_ysA, While_ken)
                 .add(A1sg_yIm, A2sg_sIn, A3sg_Verb_TEMPLATE, A1pl_yIz, A2pl_sInIz, Cop_dIr)
                 .add(A1sg_m, A2sg_n, A3sg_TEMPLATE, A1pl_k, A2pl_nIz, A3pl_lAr)
                 .add(Dat_nA, Loc_ndA, Abl_ndAn, Acc_nI, Equ_ncA)
                 .add(Dim_cIk, Dim2_cAgIz, With_lI, Without_sIz, Agt_cI, Resemb_msI, Resemb_ImsI, Ness_lIk, Related_sAl, FitFor_lIk)
                 .add(Become_lAs, Acquire_lAn, Pres_TEMPLATE)
                 .add(Noun2Noun, Noun2Adj, Noun2Verb, Noun2VerbCopular);
 
         // default noun suffix form. we remove some suffixes so that words like araba-na (dative)
         Noun_Default.connections.add(A3pl_lAr, A3sg_TEMPLATE);
         Noun_Default.indirectConnections
                 .add(Noun_TEMPLATE.indirectConnections)
                 .remove(Dat_nA, Loc_ndA, Abl_ndAn, Acc_nI, Rel_kI);
 
         Noun2Noun.connections.add(Dim_cIk, Dim2_cAgIz, Agt_cI, Ness_lIk);
 
         Noun2Adj.connections.add(With_lI, Without_sIz, Resemb_msI, Resemb_ImsI, Rel_ki, Rel_kI, Related_sAl, FitFor_lIk);
 
         Noun2Verb.connections.add(Become_lAs, Acquire_lAn);
 
         Noun2VerbCopular.connections.add(Pres_TEMPLATE, PastCop_ydI, EvidCop_ymIs, CondCop_ysA, While_ken);
         Noun2VerbCopular.indirectConnections.add(A1sg_m, A2sg_n, A3sg_Verb_TEMPLATE, A1pl_k, A2pl_nIz, A3pl_Verb_lAr);
         Noun2VerbCopular.indirectConnections.add(A1sg_yIm, A2sg_sIn, A3sg_Verb_TEMPLATE, A1pl_yIz, A2pl_sInIz, Cop_dIr);
 
         Adj2Noun.connections.add(Noun_TEMPLATE.connections);
         Adj2Noun.indirectConnections.add(Noun_Default.indirectConnections).remove(Related_sAl, Become_lAs, Resemb_ImsI, Resemb_msI);
 
         Adj2Adj.connections.add(Quite_cA, Resemb_Adj_ImsI, Resemb_Adj_msI);
 
         Adj2Adv.connections.add(Ly_cA);
 
         Adv2Noun.connections.add(A3sg_TEMPLATE);
         Adv2Noun.indirectConnections.add(Pnon_TEMPLATE, Dat_yA);
 
         Adv2Adj.connections.add(Rel_ki); // ararkenki
 
         Adj2Verb.connections.add(Become_Adj_lAs, Acquire_lAn).add(COPULAR_FORMS);
 
         Verb2Verb.connections.add(Caus_t, Caus_tIr, Pass_In, Pass_nIl, Pass_InIl, Abil_yA, Recip_Is, Recip_yIs);
 
         Verb2VerbCompounds.connections.add(KeepDoing_yAgor, KeepDoing2_yAdur, EverSince_yAgel, Almost_yAyAz, Hastily_yIver, Stay_yAkal);
 
         Verb2VerbAbility.connections.add(Abil_yAbil);
 
         Verb2Noun.connections.add(Inf1_mAk, Inf2_mA, Inf3_yIs, FeelLike_yAsI_2Noun, Agt_yIcI_2Noun, NotState_mAzlIk);
 
         Verb2NounPart.connections.add(PastPart_dIk_2Noun, EvidPart_mIs_2Noun, FutPart_yAcAk_2Noun);
 
         Verb2AdjPart.connections.add(PastPart_dIk_2Adj, EvidPart_mIs_2Adj, FutPart_yAcAk_2Adj, AorPart_Ar_2Adj, AorPart_Ir_2Adj, AorPart_z_2Adj, PresPart_yAn);
 
         Verb2Adv.connections.add(When_yIncA, SinceDoing_yAlI, UnableToDo_yAmAdAn, ByDoing_yArAk, WithoutDoing_mAdAn, WithoutDoing2_mAksIzIn)
                 .add(InsteadOfDoing_mAktAnsA, AsLongAs_dIkcA, AfterDoing_yIp, AsIf_cAsInA);
 
         Verb2Adj.connections.add(When_yIncA, FeelLike_yAsI_2Adj, Agt_yIcI_2Adj);
 
         Adv_TEMPLATE.connections.add(Adv2Noun, Adv2Adj);
 
         Adj_TEMPLATE.connections.add(Adj2Noun, Adj2Adj, Adj2Adv, Adj2Verb);
         Adj_TEMPLATE.indirectConnections.add(
                 Adj2Noun.allConnections(),
                 Adj2Adj.allConnections(),
                 Adj2Adv.allConnections(),
                 Adj2Verb.allConnections());
 
         Adj_Default.connections.add(Adj_TEMPLATE.connections);
         Adj_Default.indirectConnections.add(Adj_TEMPLATE.indirectConnections);
 
         // P3sg compound suffixes. (full form. such as zeytinyağı-na)
         Noun_Comp_P3sg.connections.add(A3sg_TEMPLATE);
         Noun_Comp_P3sg.indirectConnections.add(POSSESSIVE_FORMS)
                 .add(Pnon_TEMPLATE, Nom_TEMPLATE)
                 .add(Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA)
                 .add(A1sg_yIm, A1pl_yIz, A2sg_sIn, A2pl_sInIz);
 
         // P3sg compound suffixes. (root form. such as zeytinyağ-lar-ı)
         Noun_Comp_P3sg_Root.connections.add(A3pl_Comp_lAr, A3sg_TEMPLATE); // A3pl_Comp_lAr is used, because zeytinyağ-lar is not allowed.
         Noun_Comp_P3sg_Root.indirectConnections
                 .add(Pnon_TEMPLATE, Nom_TEMPLATE, With_lI, Without_sIz, Agt_cI, Resemb_msI, Resemb_ImsI, Ness_lIk, Related_sAl)
                 .add(P3pl_lArI)
                 .add(Noun2Noun.allConnections())
                 .add(Noun2Noun)
                 .add(Noun2Adj.allConnections())
                 .add(Noun2Adj);
 
         ProperNoun_Template.connections.add(A3pl_lAr, A3sg_TEMPLATE);
         ProperNoun_Template.indirectConnections
                 .add(CASE_FORMS, POSSESSIVE_FORMS)
                 .add(Dim_cIk, Dim2_cAgIz, With_lI, Without_sIz, A3sg_TEMPLATE, Agt_cI, Ness_lIk);
 
         Verb_TEMPLATE.connections.add(Neg_mA, Neg_m, Pos_EMPTY, Verb2Verb);
         Verb_TEMPLATE.indirectConnections
                 .add(Prog_Iyor, Prog2_mAktA, Fut_yAcAk, Past_dI, Evid_mIs, Aor_Ir, Aor_Ar, Aor_z, AorPart_Ir_2Adj, AorPart_Ar_2Adj)
                 .add(Abil_yAbil, Abil_yA, Caus_tIr, Caus_t, Opt_yA, Imp_TEMPLATE, Agt_yIcI_2Adj, Agt_yIcI_2Noun, Des_sA)
                 .add(NotState_mAzlIk, ActOf_mAcA, PastPart_dIk_2Adj, PastPart_dIk_2Noun, EvidPart_mIs_2Adj, EvidPart_mIs_2Noun, Pass_In, Pass_nIl, Pass_InIl)
                 .add(FutPart_yAcAk_2Adj, FutPart_yAcAk_2Noun, PresPart_yAn, AsLongAs_dIkcA, A2pl2_sAnIzA)
                 .add(A1sg_yIm, A2sg_sIn, A2sg_TEMPLATE, A3sg_Verb_TEMPLATE, A1pl_yIz, A2pl_yIn, A2pl_sInIz, A3pl_Verb_lAr, A3sg_sIn, A3pl_sInlAr, A2sg2_sAnA, A2sg3_yInIz)
                 .add(Inf1_mAk, Inf2_mA, Inf3_yIs, Necess_mAlI)
                 .add(When_yIncA, FeelLike_yAsI_2Adj, FeelLike_yAsI_2Noun, SinceDoing_yAlI, ByDoing_yArAk, WithoutDoing_mAdAn, WithoutDoing2_mAksIzIn)
                 .add(AfterDoing_yIp, When_yIncA, UnableToDo_yAmAdAn, InsteadOfDoing_mAktAnsA, A3pl_Verb_lAr_After_Tense, AsIf_cAsInA)
                 .add(KeepDoing2_yAdur, KeepDoing_yAgor, EverSince_yAgel, Almost_yAyAz, Hastily_yIver, Stay_yAkal, Recip_Is)
                 .add(UntilDoing_yAsIyA, Verb2VerbCompounds, Verb2Noun, Verb2Adv, Verb2Adj, Verb2NounPart, Verb2AdjPart, Verb2VerbAbility);
 
         Verb_Default.connections.add(Verb_TEMPLATE.connections);
         Verb_Default.indirectConnections.add(Verb_TEMPLATE.indirectConnections).remove(Caus_t);
 
         Verb_Prog_Drop.connections.add(Pos_EMPTY);
         Verb_Prog_Drop.indirectConnections.add(Prog_Iyor).add(Prog_Iyor.allConnections());
 
         Verb_Ye.connections.add(Neg_m, Neg_mA, Pos_EMPTY, Verb2Verb);
         Verb_Ye.indirectConnections.add(Verb_Default.indirectConnections)
                 .remove(Abil_yA, Abil_yAbil, Prog_Iyor, Fut_yAcAk, Caus_tIr,
                         FutPart_yAcAk_2Adj, Opt_yA, When_yIncA, AfterDoing_yIp, PresPart_yAn, KeepDoing_yAgor,
                         KeepDoing2_yAdur, UnableToDo_yAmAdAn).add(Pass_In, Recip_Is, Inf3_yIs);
 
         Verb_De_Ye_Prog.connections.add(Pos_EMPTY);
         Verb_De_Ye_Prog.indirectConnections.add(Prog_Iyor).add(Prog_Iyor.allConnections());
 
         Verb_Yi.connections.add(Pos_EMPTY, Verb2Verb);
         Verb_Yi.indirectConnections.add(Opt_yA, Fut_yAcAk, FutPart_yAcAk_2Adj, When_yIncA, AfterDoing_yIp, Abil_yA,
                 Abil_yAbil, Recip_yIs, Inf3_yIs, FeelLike_yAsI_2Adj, PresPart_yAn, KeepDoing_yAgor, KeepDoing2_yAdur,
                 FeelLike_yAsI_2Adj, UnableToDo_yAmAdAn, Verb2Adv);
 
 
         // modification rule does not apply for some suffixes for "demek". like deyip, not diyip
 
         Verb_De.connections.add(Neg_m, Neg_mA, Pos_EMPTY, Verb2Verb);
         Verb_De.indirectConnections.add(Verb_Default.indirectConnections)
                 .remove(Abil_yA, Abil_yAbil, Prog_Iyor, Fut_yAcAk, FutPart_yAcAk_2Adj, Opt_yA,
                         PresPart_yAn, PresPart_yAn, KeepDoing_yAgor, KeepDoing2_yAdur, FeelLike_yAsI_2Adj, UnableToDo_yAmAdAn)
                 .add(Pass_In);
 
         Verb_Di.connections.add(Pos_EMPTY, Verb2Verb);
         Verb_Di.indirectConnections.add(Opt_yA, Fut_yAcAk, FutPart_yAcAk_2Adj, Abil_yA, Abil_yAbil, PresPart_yAn,
                 PresPart_yAn, KeepDoing_yAgor, KeepDoing2_yAdur, FeelLike_yAsI_2Adj, UnableToDo_yAmAdAn, Verb2Adv);
 
         //---------------------------- Noun -----------------------------------------------------------------------
 
        A3pl_lAr.connections.add(POSSESSIVE_FORMS).remove(P3pl_lArI);
         A3pl_lAr.indirectConnections
                 .add(Noun2VerbCopular)
                 .add(Noun2VerbCopular.allConnections()).remove(A3pl_Verb_lAr)
                 .add(CASE_FORMS)
                 .add(A1pl_yIz, A2pl_sInIz);
 
         //TODO: check below.
         A3pl_Comp_lAr.connections.add(A3pl_lAr.connections);
         A3pl_Comp_lAr.indirectConnections.add(CASE_FORMS)
                 .add(A1pl_yIz, A2pl_sInIz);
 
         A3sg_TEMPLATE.connections.add(POSSESSIVE_FORMS);
         A3sg_TEMPLATE.indirectConnections
                 .add(Noun_TEMPLATE.indirectConnections).remove(POSSESSIVE_FORMS)
                 .add(Noun2Noun.allConnections()).add(Noun2Noun)
                 .add(Noun2VerbCopular.allConnections())
                 .add(Noun2Adj.allConnections().add(Noun2Adj));
 
         Nom_TEMPLATE.connections.add(Noun2Noun, Noun2Adj, Noun2Verb, Noun2VerbCopular);
 
         Nom_TEMPLATE.indirectConnections
                 .add(Noun2Noun.allConnections())
                 .add(Noun2Adj.allConnections())
                 .add(Noun2VerbCopular.allConnections())
                 .add(Noun2Verb.allConnections());
 
         Pres_TEMPLATE.connections.add(A1sg_yIm, A2sg_sIn, A3sg_Verb_TEMPLATE, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr);
         Pres_TEMPLATE.indirectConnections.add(Cop_dIr);
 
         A1sg_yIm.connections.add(Cop_dIr);
         A2sg_sIn.connections.add(Cop_dIr);
         A3sg_Verb_TEMPLATE.connections.add(Cop_dIr, Verb2Adv);
         A3sg_Verb_TEMPLATE.indirectConnections.add(AsIf_cAsInA);
         A1pl_yIz.connections.add(Cop_dIr, Verb2Adv);
         A1pl_yIz.indirectConnections.add(AsIf_cAsInA);
         A2pl_sInIz.connections.add(Cop_dIr, Verb2Adv);
         A2pl_sInIz.indirectConnections.add(AsIf_cAsInA);
         A3pl_Verb_lAr.connections.add(Evid_mIs, Past_dI, Cond_sA, Cop_dIr, Verb2Adv);
         A3pl_Verb_lAr.indirectConnections.add(AsIf_cAsInA);
 
         Dim_cIk.connections.add(Noun_Default.connections);
         Dim_cIk.indirectConnections.add(Noun_Default.allConnections().add(Noun2VerbCopular).remove(Dim_cIk, Dim2_cAgIz));
 
         Agt_cI.connections.add(Noun_Default.connections);
         Agt_cI.indirectConnections.add(Noun_Default.allConnections().add(Noun2VerbCopular).add(Noun2VerbCopular.allConnections()).remove(Agt_cI));
 
         Dim2_cAgIz.connections.add(Noun_Default.connections);
         Dim2_cAgIz.indirectConnections.add(Noun_Default.allConnections().remove(Dim_cIk, Dim2_cAgIz));
 
         Ness_lIk.connections.add(Noun_Default.connections);
         Ness_lIk.indirectConnections.add(Noun_Default.allConnections().remove(Ness_lIk));
 
         Pnon_TEMPLATE.connections
                 .add(CASE_FORMS)
                 .add(Dat_nA, Loc_ndA, Abl_ndAn, Acc_nI);
         Pnon_TEMPLATE.indirectConnections
                 .add(Nom_TEMPLATE.connections)
                 .add(Noun2Noun.allConnections())
                 .add(Noun2Verb.allConnections())
                 .add(Noun2VerbCopular.allConnections())
                 .add(Noun2Adj.allConnections());
 
         P1sg_Im.connections.add(CASE_FORMS);
         P1sg_Im.indirectConnections.add(Noun2VerbCopular).add(Noun2VerbCopular.allConnections());
 
         P2sg_In.connections.add(CASE_FORMS);
         P2sg_In.indirectConnections.add(Noun2VerbCopular).add(Noun2VerbCopular.allConnections());
 
         P3sg_sI.connections.add(Nom_TEMPLATE, Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA, Equ_ncA);
         P3sg_sI.indirectConnections.add(Noun2VerbCopular).add(Noun2VerbCopular.allConnections());
 
         P1pl_ImIz.connections.add(CASE_FORMS);
         P1pl_ImIz.indirectConnections.add(Noun2VerbCopular).add(Noun2VerbCopular.allConnections());
 
         P2pl_InIz.connections.add(CASE_FORMS);
         P2pl_InIz.indirectConnections.add(Noun2VerbCopular).add(Noun2VerbCopular.allConnections());
 
         P3pl_lArI.connections.add(CASE_FORMS);
         P3pl_lArI.indirectConnections.add(Noun2VerbCopular).add(Noun2VerbCopular.allConnections());
 
         With_lI.connections.add(Adj_TEMPLATE.connections);
         With_lI.indirectConnections.add(Adj_TEMPLATE.indirectConnections);
 
         Related_sAl.connections.add(Adj_TEMPLATE.connections);
         Related_sAl.indirectConnections.add(Adj_TEMPLATE.indirectConnections);
 
         Without_sIz.connections.add(Adj_TEMPLATE.connections);
         Without_sIz.indirectConnections.add(Adj_TEMPLATE.indirectConnections);
 
         FitFor_lIk.connections.add(Adj_TEMPLATE.connections);
         FitFor_lIk.indirectConnections.add(Adj_TEMPLATE.indirectConnections);
 
         // Noun->Adj derivation elmadaki=elma+Loc-Adj+Rel
         Loc_dA.connections.add(Noun2Adj, Noun2VerbCopular);
         Loc_dA.indirectConnections.add(Rel_ki).add(Noun2VerbCopular.allConnections());
 
         Loc_ndA.connections.add(Noun2Adj, Noun2VerbCopular);
         Loc_ndA.indirectConnections.add(Rel_ki).add(Noun2VerbCopular.allConnections());
 
         Dat_nA.connections.add(Noun2VerbCopular);
         Dat_nA.indirectConnections.add(Noun2VerbCopular.allConnections());
 
         Dat_yA.connections.add(Noun2VerbCopular);
         Dat_yA.indirectConnections.add(Noun2VerbCopular.allConnections());
 
         Gen_nIn.connections.add(Noun2VerbCopular, Noun2Adj);
         Gen_nIn.indirectConnections.add(Noun2VerbCopular.allConnections()).add(Noun2Adj.connections);
 
         Dat_yA.connections.add(Noun2VerbCopular);
         Dat_yA.indirectConnections.add(Noun2VerbCopular.allConnections());
 
         Abl_dAn.connections.add(Noun2VerbCopular);
         Abl_dAn.indirectConnections.add(Noun2VerbCopular.allConnections());
 
         Abl_ndAn.connections.add(Noun2VerbCopular);
         Abl_ndAn.indirectConnections.add(Noun2VerbCopular.allConnections());
 
         Inst_ylA.connections.add(Noun2VerbCopular);
         Inst_ylA.indirectConnections.add(Noun2VerbCopular.allConnections());
 
         Equ_cA.connections.add(Noun2VerbCopular);
         Equ_cA.indirectConnections.add(Noun2VerbCopular.allConnections());
 
         Rel_ki.connections.add(Adj2Noun);
         Rel_ki.indirectConnections.add(Adj2Noun.indirectConnections).add(A3sg_TEMPLATE, Pnon_TEMPLATE, Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA, Equ_ncA);
 
         Rel_kI.connections.add(Adj2Noun);
         Rel_kI.indirectConnections.add(Adj2Noun.indirectConnections).add(A3sg_TEMPLATE, Pnon_TEMPLATE, Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA, Equ_ncA);
 
         Resemb_msI.connections.add(Adj_TEMPLATE.connections);
         Resemb_msI.indirectConnections.add(Adj_TEMPLATE.indirectConnections);
 
         Resemb_ImsI.connections.add(Adj_TEMPLATE.connections);
         Resemb_ImsI.indirectConnections.add(Adj_TEMPLATE.indirectConnections);
 
         Resemb_Adj_msI.connections.add(Adj_TEMPLATE.connections);
         Resemb_Adj_msI.indirectConnections.add(Adj_TEMPLATE.indirectConnections);
 
         Resemb_Adj_ImsI.connections.add(Adj_TEMPLATE.connections);
         Resemb_Adj_ImsI.indirectConnections.add(Adj_TEMPLATE.indirectConnections);
 
         //---------------------------- Adjective -----------------------------------------------------------------------
 
         Become_lAs.connections.add(Verb_TEMPLATE.connections);
         Become_lAs.indirectConnections.add(Verb_TEMPLATE.indirectConnections).remove(Caus_t, Pass_In, Pass_InIl);
 
         Acquire_lAn.connections.add(Verb_TEMPLATE.connections);
         Acquire_lAn.indirectConnections.add(Verb_TEMPLATE.indirectConnections).remove(Caus_t, Pass_In, Pass_InIl);
 
         Become_Adj_lAs.connections.add(Verb_TEMPLATE.connections);
         Become_Adj_lAs.indirectConnections.add(Verb_TEMPLATE.indirectConnections).remove(Caus_t, Pass_In, Pass_InIl);
 
         Quite_cA.connections.add(Adj_TEMPLATE.connections);
 
         Ly_cA.connections.add(Adv_TEMPLATE.connections);
 
         //---------------------------- Verb ----------------------------------------------------------------------------
 
         Pos_EMPTY.connections
                 .add(Verb2VerbCompounds, Verb2Noun, Verb2Adv, Verb2Adj, Verb2AdjPart, Verb2NounPart, Verb2VerbAbility,
                         Imp_TEMPLATE, Prog_Iyor, Prog2_mAktA, Fut_yAcAk, Aor_Ar, Aor_Ir, Past_dI, Evid_mIs)
                 .add(Cond_sA, Necess_mAlI, Opt_yA, Des_sA);
         Pos_EMPTY.indirectConnections
                 .add(Verb_Default.indirectConnections)
                 .add(A2pl2_sAnIzA, A2pl_yIn)
                 .add(Verb2AdjPart.connections, Verb2NounPart.connections)
                 .remove(Neg_m, Neg_mA);
 
         Neg_mA.connections.add(Verb2VerbCompounds, Verb2VerbAbility, Verb2Noun, Verb2Adv, Verb2Adj, Verb2AdjPart, Verb2NounPart,
                 Aor_z, Aor_EMPTY, Prog2_mAktA, Imp_TEMPLATE, Opt_yA, Des_sA,
                 Fut_yAcAk, Past_dI, Evid_mIs, Necess_mAlI, NotState_mAzlIk,
                 ActOf_mAcA);
 
         Neg_mA.indirectConnections.add(Verb2VerbCompounds.connections, Verb2AdjPart.connections, Verb2NounPart.connections,
                 Verb2Noun.connections, Verb2Adv.connections, Verb2Adj.connections)
                 .add(A2sg_TEMPLATE, A1sg_m, A1sg_yIm, A2sg_sIn, A2sg2_sAnA, A2sg3_yInIz, A3sg_Verb_TEMPLATE, A1pl_yIz, A2pl_sInIz, A2pl2_sAnIzA, A2pl_yIn, A3pl_Verb_lAr, A3sg_sIn, A3pl_sInlAr)
                 .add(Abil_yAbil);
 
         Neg_m.connections.add(Prog_Iyor);
 
         Imp_TEMPLATE.connections.add(A2sg_TEMPLATE, A2sg2_sAnA, A2sg3_yInIz, A2pl2_sAnIzA, A2pl_yIn, A3sg_sIn, A3pl_sInlAr);
 
         Caus_t.connections.add(Verb2Verb, Pos_EMPTY, Neg_mA, Neg_m);
         Caus_t.indirectConnections.add(Verb_TEMPLATE.indirectConnections).add(Pass_nIl).add(Caus_tIr).remove(Caus_t);
 
         Caus_tIr.connections.add(Verb_TEMPLATE.connections);
         Caus_tIr.indirectConnections.add(Verb_TEMPLATE.indirectConnections).add(Pass_nIl).add(Caus_t).remove(Caus_tIr);
 
         Recip_Is.connections.add(Verb_TEMPLATE.connections);
         Recip_Is.indirectConnections.add(Verb_TEMPLATE.indirectConnections).remove(Caus_t, Caus_tIr, Pass_nIl, Pass_InIl, Pass_In, Recip_Is, Recip_yIs);
         Recip_yIs.connections.add(Verb_TEMPLATE.connections);
         Recip_yIs.indirectConnections.add(Verb_TEMPLATE.indirectConnections).remove(Caus_t, Caus_tIr, Pass_nIl, Pass_InIl, Pass_In, Recip_Is, Recip_yIs);
 
         Pass_nIl.connections.add(Verb_TEMPLATE.connections);
         Pass_nIl.indirectConnections.add(Verb_TEMPLATE.indirectConnections).remove(Caus_t, Caus_tIr, Pass_nIl, Pass_InIl, Pass_In);
 
         Pass_In.connections.add(Verb_TEMPLATE.connections);
         Pass_In.indirectConnections.add(Verb_TEMPLATE.indirectConnections).remove(Caus_t, Caus_tIr, Pass_nIl, Pass_InIl, Pass_In);
 
         Pass_InIl.connections.add(Verb_TEMPLATE.connections);
         Pass_InIl.indirectConnections.add(Verb_TEMPLATE.indirectConnections).remove(Caus_t, Caus_tIr, Pass_nIl, Pass_InIl, Pass_In);
 
         Prog_Iyor.connections.add(A3sg_Verb_TEMPLATE, A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr).add(COPULAR_FORMS);
 
         Prog2_mAktA.connections.add(A3sg_Verb_TEMPLATE, A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr).add(COPULAR_FORMS);
 
         Fut_yAcAk.connections.add(A3sg_Verb_TEMPLATE, A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr).add(COPULAR_FORMS);
         Fut_yAcAk.indirectConnections.add(A3sg_Verb_TEMPLATE.allConnections());
 
 
         Aor_Ar.connections.add(A3sg_Verb_TEMPLATE, A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr).add(COPULAR_FORMS);
         Aor_Ar.indirectConnections.add(Verb2Adv, AsIf_cAsInA);
         Aor_Ir.connections.add(A3sg_Verb_TEMPLATE, A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr).add(COPULAR_FORMS);
         Aor_Ir.indirectConnections.add(Verb2Adv, AsIf_cAsInA);
         Aor_z.connections.add(A3sg_Verb_TEMPLATE, A3sg_sIn).add(PastCop_ydI, EvidCop_ymIs, CondCop_ysA, While_ken, Cop_dIr);
         Aor_z.indirectConnections.add(Verb2Adv, AsIf_cAsInA);
         Aor_EMPTY.connections.add(A1sg_m, A1pl_yIz);
 
         AorPart_Ar_2Adj.connections.add(Adj2Noun);
         AorPart_Ar_2Adj.indirectConnections.add(Adj2Noun.allConnections());
         AorPart_Ir_2Adj.connections.add(Adj2Noun);
         AorPart_Ir_2Adj.indirectConnections.add(Adj2Noun.allConnections());
         AorPart_z_2Adj.connections.add(Adj2Noun);
         AorPart_z_2Adj.indirectConnections.add(Adj2Noun.allConnections());
 
         PastPart_dIk_2Noun.connections.add(A3sg_TEMPLATE);
         PastPart_dIk_2Noun.indirectConnections.add(POSSESSIVE_FORMS, CASE_FORMS);
 
         EvidPart_mIs_2Noun.connections.add(A3pl_lAr, A3sg_TEMPLATE);
         EvidPart_mIs_2Noun.indirectConnections.add(POSSESSIVE_FORMS, CASE_FORMS);
 
         // Oflazer suggests only with A3pl. I think A3sg is also possible.
         FutPart_yAcAk_2Noun.connections.add(A3pl_lAr, A3sg_TEMPLATE);
         FutPart_yAcAk_2Noun.indirectConnections.add(POSSESSIVE_FORMS, CASE_FORMS);
 
         PastPart_dIk_2Adj.connections.add(Adj2Noun);
         PastPart_dIk_2Adj.indirectConnections.add(Adj2Noun.allConnections());
 
         EvidPart_mIs_2Adj.connections.add(Adj2Noun);
         EvidPart_mIs_2Adj.indirectConnections.add(Adj2Noun.allConnections());
 
         FutPart_yAcAk_2Adj.connections.add(Adj2Noun);
         FutPart_yAcAk_2Adj.indirectConnections.add(Adj2Noun.allConnections());
 
         PresPart_yAn.connections.add(Adj2Noun);
         PresPart_yAn.indirectConnections.add(Adj2Noun.allConnections());
 
         Past_dI.connections.add(A1sg_m, A2sg_n, A3sg_Verb_TEMPLATE, A1pl_k, A2pl_nIz, A3pl_Verb_lAr, CondCop_ysA, PastCop_ydI);
 
         Evid_mIs.connections.add(A1sg_yIm, A2sg_sIn, A3sg_Verb_TEMPLATE, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr)
                 .add(CondCop_ysA, PastCop_ydI, EvidCop_ymIs, While_ken, Cop_dIr);
         Evid_mIs.indirectConnections.add(A3sg_Verb_TEMPLATE.allConnections());
 
         Cond_sA.connections.add(A1sg_m, A2sg_n, A3sg_Verb_TEMPLATE, A1pl_k, A2pl_nIz, A3pl_Verb_lAr, PastCop_ydI, EvidCop_ymIs);
 
         PastCop_ydI.connections.add(PERSON_FORMS_COP);
         EvidCop_ymIs.connections.add(A1sg_yIm, A2sg_sIn, A3sg_Verb_TEMPLATE, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr);
         EvidCop_ymIs.indirectConnections.add(A3sg_Verb_TEMPLATE.allConnections());
         CondCop_ysA.connections.add(PERSON_FORMS_COP);
         Cop_dIr.connections.add(A3pl_Verb_lAr);
 
         // TODO: may be too broad
         Inf1_mAk.connections.add(A3sg_TEMPLATE);
         Inf1_mAk.indirectConnections.add(Pnon_TEMPLATE, Nom_TEMPLATE, Inst_ylA, Dat_yA, Abl_dAn, Acc_yI, Noun2Adj, Noun2Noun, Noun2VerbCopular);
         Inf1_mAk.indirectConnections.add(Noun2Adj.allConnections(), Noun2Noun.allConnections(), Noun2VerbCopular.allConnections());
         Inf2_mA.connections.add(Noun_TEMPLATE.connections);
         Inf2_mA.indirectConnections.add(Noun_TEMPLATE.indirectConnections);
         Inf3_yIs.connections.add(Noun_TEMPLATE.connections);
         Inf3_yIs.indirectConnections.add(Noun_TEMPLATE.indirectConnections);
         NotState_mAzlIk.connections.add(Noun_TEMPLATE.connections);
         NotState_mAzlIk.indirectConnections.add(Noun_TEMPLATE.indirectConnections);
 
 
         Abil_yA.connections.add(Neg_mA, Neg_m);
         Abil_yA.indirectConnections.add(Neg_mA.allConnections());
 
         Abil_yAbil.connections.add(Neg_mA.connections).add(Aor_Ir, Prog_Iyor).remove(Verb2VerbAbility);
         Abil_yAbil.indirectConnections.add(Neg_mA.indirectConnections);
 
         // TODO: removing false positives like geliveriver may be necessary
 
         KeepDoing_yAgor.connections.add(Pos_EMPTY.connections, Neg_mA.connections);
         KeepDoing_yAgor.indirectConnections.add(Pos_EMPTY.indirectConnections, Neg_mA.indirectConnections);
 
         KeepDoing2_yAdur.connections.add(Pos_EMPTY.connections, Neg_mA.connections);
         KeepDoing2_yAdur.indirectConnections.add(Pos_EMPTY.indirectConnections, Neg_mA.indirectConnections);
 
         EverSince_yAgel.connections.add(Pos_EMPTY.connections, Neg_mA.connections);
         EverSince_yAgel.indirectConnections.add(Pos_EMPTY.indirectConnections, Neg_mA.indirectConnections);
 
         Almost_yAyAz.connections.add(Pos_EMPTY.connections, Neg_mA.connections);
         Almost_yAyAz.indirectConnections.add(Pos_EMPTY.indirectConnections, Neg_mA.indirectConnections);
 
         Hastily_yIver.connections.add(Pos_EMPTY.connections, Neg_mA.connections);
         Hastily_yIver.indirectConnections.add(Pos_EMPTY.indirectConnections, Neg_mA.indirectConnections);
 
         Stay_yAkal.connections.add(Pos_EMPTY.connections, Neg_mA.connections);
         Stay_yAkal.indirectConnections.add(Pos_EMPTY.indirectConnections, Neg_mA.indirectConnections);
 
         Necess_mAlI.connections.add(A3sg_Verb_TEMPLATE, A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3pl_Verb_lAr).add(COPULAR_FORMS);
 
         Des_sA.connections.add(A1sg_m, A2sg_n, A3sg_TEMPLATE, A1pl_k, A2pl_nIz, A3pl_lAr, PastCop_ydI, EvidCop_ymIs);
 
         When_yIncA.connections.add(Adv2Noun);
         When_yIncA.indirectConnections.add(Adv2Noun.allConnections());
 
         //TODO ararkendi ararkenmiş legal?
         While_ken.connections.add(Adv2Adj);
         While_ken.indirectConnections.add(Rel_ki);
 
         // TODO: FeelLike_yAsI_2Noun, Agt_yIcI_Noun may be too broad.
         FeelLike_yAsI_2Noun.connections.add(Noun_TEMPLATE.connections);
         FeelLike_yAsI_2Noun.indirectConnections.add(Noun_TEMPLATE.indirectConnections);
 
         FeelLike_yAsI_2Adj.connections.add(Adj_TEMPLATE);
 
         Agt_yIcI_2Noun.connections.add(Noun_TEMPLATE.connections);
         Agt_yIcI_2Noun.indirectConnections.add(Noun_TEMPLATE.indirectConnections);
 
         Agt_yIcI_2Adj.connections.add(Adj_TEMPLATE);
 
         Opt_yA.connections.add(A3sg_Verb_TEMPLATE, A1sg_yIm, A2sg_sIn, A1pl_lIm, A2pl_sInIz, A3pl_Verb_lAr).add(COPULAR_FORMS);
 
         registerForms(
                 Noun_TEMPLATE, Verb_TEMPLATE, Adj_TEMPLATE, Adv_TEMPLATE, Pnon_TEMPLATE,
                 //  Noun_Comp_P3sg, Noun_Comp_P3sg_Root,
 
                 Noun2Adj, Noun2Noun, Noun2Verb, Noun2VerbCopular,
                 Adj2Adj, Adj2Adv, Adj2Noun, Adj2Verb,
                 Verb2Adj, Verb2AdjPart, Verb2Verb, Verb2VerbCompounds, Verb2VerbAbility, Verb2Noun, Verb2NounPart, Verb2Adv,
                 Adv2Noun, Adv2Adj,
 
                 Pres_TEMPLATE,
 
                 Noun_Default, Verb_Default, Adj_Default, Resemb_Adj_ImsI, Resemb_Adj_msI, Resemb_msI,
                 Pass_InIl,
                 Nom_TEMPLATE, Dat_yA, Dat_nA, Loc_dA, Loc_ndA, Abl_dAn, Abl_ndAn, Gen_nIn,
                 Acc_yI, Acc_nI, Inst_ylA,
                Pnon_TEMPLATE, P1sg_Im, P2sg_In, P3sg_sI, P1pl_ImIz, P2pl_InIz, P3pl_lArI,
                 Dim_cIk, Dim2_cAgIz,
                 With_lI, Without_sIz, Rel_ki, Rel_kI,
 
                 A1sg_yIm, A1sg_m, A1sg_TEMPLATE, A2sg_sIn, A2sg_n, A2sg_TEMPLATE, A2sg2_sAnA,
                 A3sg_TEMPLATE, A3sg_Verb_TEMPLATE, A2sg3_yInIz, A3sg_sIn,
                 A1pl_yIz, A1pl_k, A1pl_lIm, A1pl_TEMPLATE, A2pl_sInIz, A2pl_nIz, A2pl_yIn, A2pl_TEMPLATE, A2pl2_sAnIzA,
                 A3pl_lAr, A3pl_Verb_lAr, A3pl_Verb_lAr_After_Tense, A3pl_sInlAr, Agt_cI, Agt_yIcI_2Adj, Agt_yIcI_2Noun,
 
                 Ness_lIk, FitFor_lIk,
                 Become_lAs, Become_Adj_lAs, Acquire_lAn,
                 Resemb_ImsI, Resemb_msI, Related_sAl,
                 Aor_Ir, Aor_Ar, Aor_z, Des_sA,
                 Aor_EMPTY, AorPart_Ar_2Adj, AorPart_Ir_2Adj, AorPart_z_2Adj,
                 Prog_Iyor, Prog2_mAktA, Fut_yAcAk,
                 FutPart_yAcAk_2Adj, FutPart_yAcAk_2Noun, Past_dI, PastPart_dIk_2Noun, PastPart_dIk_2Adj,
                 Evid_mIs, EvidPart_mIs_2Adj, EvidPart_mIs_2Noun, PresPart_yAn, Pos_EMPTY, Neg_mA, Neg_m, Cond_sA,
                 Necess_mAlI, Opt_yA,
                 Pass_In, Pass_nIl,
                 Caus_t, Caus_tIr,
                 Imp_TEMPLATE, Recip_Is, Recip_yIs, Reflex_In, Abil_yAbil, Abil_yA, Cop_dIr,
                 PastCop_ydI, EvidCop_ymIs, CondCop_ysA, While_ken, NotState_mAzlIk, ActOf_mAcA,
                 AsIf_cAsInA, AsLongAs_dIkcA, When_yIncA, FeelLike_yAsI_2Adj, FeelLike_yAsI_2Noun, SinceDoing_yAlI, ByDoing_yArAk, WithoutDoing_mAdAn,
                 WithoutDoing2_mAksIzIn, AfterDoing_yIp, UnableToDo_yAmAdAn, InsteadOfDoing_mAktAnsA,
                 KeepDoing_yAgor, KeepDoing2_yAdur, EverSince_yAgel,
                 Almost_yAyAz, Hastily_yIver, Stay_yAkal, Inf1_mAk, Inf2_mA, Inf3_yIs, Ly_cA,
                 Quite_cA, Equ_cA, Equ_ncA, UntilDoing_yAsIyA,
                 A3pl_Comp_lAr, Interj_Template, Verb_Prog_Drop, PersPron_BenSen, PersPron_BanSan,
                 Ordinal_IncI, Grouping_sAr);
     }
 
     @Override
     public SuffixForm getRootSet(DictionaryItem item, SuffixData suffixConstraint) {
         if (suffixConstraint.isEmpty()) {
             switch (item.primaryPos) {
                 case Noun:
                     if (item.secondaryPos == SecondaryPos.ProperNoun)
                         return ProperNoun_Template;
                     return Noun_Default;
                 case Adjective:
                     return Adj_Default;
                 case Verb:
                     return Verb_Default;
                 case Adverb:
                     return Adv_Default;
                 case Numeral:
                     return Numeral_Template;
                 case Pronoun:
                     if (item.secondaryPos == SecondaryPos.Demonstrative)
                         return DemonsPron_Default;
                     else
                         return PersPron_Default;
                 default:
                     return Noun_Default;
             }
         } else {
             SuffixFormTemplate template;
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
                 case Pronoun:
                     if (item.secondaryPos == SecondaryPos.Demonstrative)
                         template = DemonsPron_TEMPLATE;
                     else
                         return PersPron_TEMPLATE;
                     break;
                 default:
                     template = Noun_TEMPLATE;
 
             }
             NullSuffixForm copy = generateNullFormFromTemplate(template, suffixConstraint).copy();
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
                     original.add(Noun_Comp_P3sg.allConnections());
                     modified.add(Noun_Comp_P3sg_Root.allConnections());
                     break;
                 default:
                     break;
             }
         }
     }
 
     private void getForVerb(DictionaryItem item, SuffixData original, SuffixData modified) {
         original.add(Verb_TEMPLATE.allConnections().remove(Caus_t));
         modified.add(Verb_TEMPLATE.allConnections().remove(Caus_t));
         List<RootAttr> attrList = item.attrs.getAsList(RootAttr.class);
         for (RootAttr attribute : attrList) {
             switch (attribute) {
                 case Aorist_A:
                     original.add(Aor_Ar, AorPart_Ar_2Adj);
                     original.remove(Aor_Ir, AorPart_Ir_2Adj);
                     if (!item.attrs.contains(RootAttr.ProgressiveVowelDrop)) {
                         modified.add(Aor_Ar, AorPart_Ar_2Adj);
                         modified.remove(Aor_Ir, AorPart_Ir_2Adj);
                     }
                     break;
                 case Aorist_I:
                     original.add(Aor_Ir, AorPart_Ir_2Adj);
                     original.remove(Aor_Ar, AorPart_Ar_2Adj);
                     if (!item.attrs.contains(RootAttr.ProgressiveVowelDrop)) {
                         modified.add(Aor_Ir, AorPart_Ir_2Adj);
                         modified.remove(Aor_Ar, AorPart_Ar_2Adj);
                     }
                     break;
                 case Voicing:
                     modified.remove(Pass_In);
                     modified.remove(Pass_InIl);
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
