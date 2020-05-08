 package zemberek3.lexicon;
 
 import zemberek3.structure.TurkicLetter;
 import zemberek3.structure.TurkicSeq;
 import zemberek3.structure.TurkishAlphabet;
 
 import java.util.*;
 
 public class TurkishSuffixes {
 
     public static final TurkishSuffix Pl_lAr = new TurkishSuffix("Pl_lAr");
     public static final TurkishSuffix Dat_yA = new TurkishSuffix("Dat_yA");
     public static final TurkishSuffix Loc_dA = new TurkishSuffix("Loc_dA");
     public static final TurkishSuffix Abl_dAn = new TurkishSuffix("Abl_dAn");
     public static final TurkishSuffix Gen_nIn = new TurkishSuffix("Gen_nIn");
     public static final TurkishSuffix Acc_yI = new TurkishSuffix("Acc_yI");
     public static final TurkishSuffix Inst_lA = new TurkishSuffix("Inst_lA");
 
     public static final TurkishSuffix P1sg_Im = new TurkishSuffix("P1sg_Im");
     public static final TurkishSuffix P2sg_In = new TurkishSuffix("P2sg_In");
     public static final TurkishSuffix P3sg_sI = new TurkishSuffix("P3sg_sI");
     public static final TurkishSuffix P1pl_ImIz = new TurkishSuffix("P1pl_ImIz");
     public static final TurkishSuffix P2pl_InIz = new TurkishSuffix("P2pl_InIz");
     public static final TurkishSuffix P3pl_lArI = new TurkishSuffix("P3pl_lArI");
 
     public static final TurkishSuffix Dim_cIk = new TurkishSuffix("Dim_cIk");
     public static final TurkishSuffix Dim_cAgIz = new TurkishSuffix("Dim_cAgIz");
     public static final TurkishSuffix With_lI = new TurkishSuffix("With_lI");
     public static final TurkishSuffix Without_sIz = new TurkishSuffix("Without_sIz");
     public static final TurkishSuffix Rel_ki = new TurkishSuffix("Rel_ki");
 
     public static final TurkishSuffix A1sg_yIm = new TurkishSuffix("A1sg_yIm");
     public static final TurkishSuffix A2sg_sIn = new TurkishSuffix("A2sg_sIn");
     public static final TurkishSuffix A3sg = new TurkishSuffix("A3sg");
     public static final TurkishSuffix A1pl_yIz = new TurkishSuffix("A1pl_yIz");
     public static final TurkishSuffix A2pl_sInIz = new TurkishSuffix("A2pl_sInIz");
     public static final TurkishSuffix A3pl_lAr = new TurkishSuffix("A3pl_lAr");
 
     public static final TurkishSuffix Become_lAs = new TurkishSuffix("Become_lAs");
 
     public static final TurkishSuffix Aor_Ir = new TurkishSuffix("Aor_Ir");
     public static final TurkishSuffix Prog_Iyor = new TurkishSuffix("Prog_Iyor");
     public static final TurkishSuffix Prog_mAktA = new TurkishSuffix("Prog_mAktA");
     public static final TurkishSuffix Fut_yAcAk = new TurkishSuffix("Fut_yAcAk");
     public static final TurkishSuffix Past_dI = new TurkishSuffix("Past_dI");
     public static final TurkishSuffix Evid_mIs = new TurkishSuffix("Evid_mIs");
     public static final TurkishSuffix Neg_mA = new TurkishSuffix("Neg_mA");
     public static final TurkishSuffix Cond_sa = new TurkishSuffix("Cond_sa");
     public static final TurkishSuffix Necess_mAlI = new TurkishSuffix("Necess_mAlI");
     public static final TurkishSuffix Opt_yA = new TurkishSuffix("Opt_yA");
     public static final TurkishSuffix Pass_In = new TurkishSuffix("Pass_In");
     public static final TurkishSuffix Caus_dIr = new TurkishSuffix("Caus_dIr");
     public static final TurkishSuffix Recip_yIs = new TurkishSuffix("Recip_yIs");
     public static final TurkishSuffix Reflex_In = new TurkishSuffix("Reflex_In");
     public static final TurkishSuffix Abil_yAbIl = new TurkishSuffix("Abil_yAbIl");
     public static final TurkishSuffix Cont_yAdur = new TurkishSuffix("Cont_yAdur");
 
 
     public static final TurkishSuffix Cop_dIr = new TurkishSuffix("Cop_dIr");
     public static final TurkishSuffix PastCop_ydI = new TurkishSuffix("PastCop_ydI");
     public static final TurkishSuffix EvidCop_ymIs = new TurkishSuffix("EvidCop_ymIs");
     public static final TurkishSuffix CondCop_ysA = new TurkishSuffix("CondCop_ysA");
     public static final TurkishSuffix While_yken = new TurkishSuffix("While_yken");
 
     public static final TurkishSuffix AfterDoing_yIp = new TurkishSuffix("AfterDoing_yIp");
     public static final TurkishSuffix JustAfter_yIncA = new TurkishSuffix("JustAfter_yIncA");
 
     public static final TurkishSuffix[] NOUN_CASE = {Dat_yA, Loc_dA, Abl_dAn, Gen_nIn, Acc_yI, Inst_lA};
     public static final TurkishSuffix[] NOUN_POSS = {P1sg_Im, P2sg_In, P1pl_ImIz, P2pl_InIz, P3pl_lArI};
     public static final TurkishSuffix[] NOUN_PERSON = {A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz, A3pl_lAr};
     public static final TurkishSuffix[] COPULAR = {Cop_dIr, PastCop_ydI, EvidCop_ymIs, CondCop_ysA, While_yken};
 
     public void generate() {
 
         Pl_lAr.addNodes(generateNodes(Pl_lAr, "lAr"))
                 .addSuccessors(NOUN_CASE, COPULAR)
                 .addSuccessors(P1sg_Im, P2sg_In, P1pl_ImIz, P2pl_InIz, A1pl_yIz, A2pl_sInIz);
 
         Dat_yA.addNodes(generateNodes(Dat_yA, "+yA"))
                 .addSuccessors(COPULAR);
         Dat_yA.addNodes(generatePredecessorNodes(Dat_yA, "nA", Rel_ki, P3sg_sI, P3pl_lArI));
 
         Loc_dA.addNodes(generateNodes(Loc_dA, ">dA")).addSuccessors(COPULAR);
         Loc_dA.addNodes(generatePredecessorNodes(Loc_dA, "ndA", Rel_ki, P3sg_sI, P3pl_lArI));
 
         Abl_dAn.addNodes(generateNodes(Abl_dAn, ">dAn")).addSuccessors(COPULAR);
         Abl_dAn.addNodes(generatePredecessorNodes(Abl_dAn, "ndAn", Rel_ki, P3sg_sI, P3pl_lArI));
 
         Gen_nIn.addNodes(generateNodes(Gen_nIn, "+nIn"))
                 .addSuccessors(COPULAR)
                 .addSuccessors(Rel_ki);
 
         Acc_yI.addNodes(generateNodes(Acc_yI, "+yI"))
                 .addNodes(generatePredecessorNodes(Abl_dAn, "nI", Rel_ki, P3sg_sI, P3pl_lArI));
 
         P1sg_Im.addNodes(generateNodes(P1sg_Im, "Im")).
                 addSuccessors(NOUN_CASE, COPULAR).
                 addSuccessors(A2sg_sIn, A2pl_sInIz);
 
         P2sg_In.addNodes(generateNodes(P2sg_In, "In")).
                 addSuccessors(NOUN_CASE, COPULAR).
                 addSuccessors(A1sg_yIm, A1pl_yIz);
 
         P3sg_sI.addNodes(generateNodes(P3sg_sI, "+sI")).
                 addSuccessors(NOUN_CASE, COPULAR).
                 addSuccessors(A1sg_yIm, A2sg_sIn, A1pl_yIz, A2pl_sInIz);
 
 
         Dim_cIk.addNodes(generateNodes(Dim_cIk, ">cI~k")).
                 addSuccessors(NOUN_CASE, COPULAR, NOUN_POSS, NOUN_PERSON).
                 addSuccessors(Pl_lAr, With_lI, Without_sIz);
         //TODO: make it nicer.
         for (SuffixNode suffixNode : Dim_cIk.nodes) {
             if (suffixNode.seq.lastLetter() == TurkishAlphabet.L_gg)
                 suffixNode.add(MorphAttr.ExpectsVowel);
             if (suffixNode.seq.lastLetter() == TurkishAlphabet.L_k)
                 suffixNode.add(MorphAttr.ExpectsConsonant);
         }
 
         Dim_cAgIz.addNodes(generateNodes(Dim_cAgIz, ">cağız")).
                 addSuccessors(NOUN_CASE, COPULAR, NOUN_POSS, NOUN_PERSON).
                 addSuccessors(Pl_lAr, With_lI, Without_sIz);
 
         With_lI.addNodes(generateNodes(With_lI, "lI")).
                 addSuccessors(NOUN_CASE, COPULAR, NOUN_POSS, NOUN_PERSON).
                 addSuccessors(Pl_lAr);
 
         Without_sIz.addNodes(generateNodes(Without_sIz, "sIz")).
                 addSuccessors(NOUN_CASE, COPULAR, NOUN_POSS, NOUN_PERSON).
                 addSuccessors(Pl_lAr);
 
         Rel_ki.addNodes(generateNodes(Rel_ki, "ki")).addSuccessors(NOUN_CASE, COPULAR, NOUN_PERSON);
 
        A1sg_yIm.addNodes(generateNodes(A1sg_yIm,"+yIm")).addSuccessors(Cop_dIr);
        A2sg_sIn.addNodes(generateNodes(A2sg_sIn,"sIn")).addSuccessors(Cop_dIr);
        A1pl_yIz.addNodes(generateNodes(A1pl_yIz,"+yIz")).addSuccessors(Cop_dIr);
        A2pl_sInIz.addNodes(generateNodes(A2pl_sInIz,"sInIz")).addSuccessors(Cop_dIr);
        A3pl_lAr.addNodes(generateNodes(A3pl_lAr,"lAr")).addSuccessors(Cop_dIr);
     }
 
     private SuffixNode[] generatePredecessorNodes(TurkishSuffix suffix, String generationWord, TurkishSuffix... predecessorSuffixes) {
         SuffixNode[] accExceptionNodes = generateNodes(suffix, generationWord);
         for (SuffixNode node : accExceptionNodes) {
             node.addExclusivePredecessor(predecessorSuffixes);
         }
         return accExceptionNodes;
     }
 
 
     TurkishAlphabet alphabet = new TurkishAlphabet();
 
     private List<TurkicSeq> generateFromSuffixString(String suffixString) {
 
         ArrayList<TurkicSeq> sequences = new ArrayList<TurkicSeq>();
         if (suffixString.length() > 0)
             sequences.add(new TurkicSeq());
 
         Iterator<SuffixToken> tokenIterator = new SuffixStringTokenizer(suffixString);
         while (tokenIterator.hasNext()) {
             SuffixToken token = tokenIterator.next();
             switch (token.type) {
 
                 case LETTER:
                     for (TurkicSeq seq : sequences) {
                         seq.append(token.l);
                     }
                     break;
 
                 case A_WOVEL:
                     ArrayList<TurkicSeq> alist = new ArrayList<TurkicSeq>();
                     for (TurkicSeq seq : sequences) {
                         if (!seq.hasVowel()) {
                             if (seq.length() == 0 && sequences.size() == 1)
                                 alist.add(new TurkicSeq());
                             alist.add(new TurkicSeq(seq).append(TurkishAlphabet.L_a));
                             alist.add(new TurkicSeq(seq).append(TurkishAlphabet.L_e));
                         } else if (seq.lastVowel().isFrontalVowel()) {
                             alist.add(new TurkicSeq(seq).append(TurkishAlphabet.L_e));
                         } else {
                             alist.add(new TurkicSeq(seq).append(TurkishAlphabet.L_a));
                         }
                     }
                     sequences = alist;
                     break;
 
                 case I_WOVEL:
                     ArrayList<TurkicSeq> ilist = new ArrayList<TurkicSeq>();
                     for (TurkicSeq seq : sequences) {
                         if (!seq.hasVowel()) {
                             if (seq.length() == 0 && sequences.size() == 1)
                                 ilist.add(new TurkicSeq());
                             ilist.add(new TurkicSeq(seq).append(TurkishAlphabet.L_ii));
                             ilist.add(new TurkicSeq(seq).append(TurkishAlphabet.L_i));
                             ilist.add(new TurkicSeq(seq).append(TurkishAlphabet.L_u));
                             ilist.add(new TurkicSeq(seq).append(TurkishAlphabet.L_uu));
                         } else {
                             boolean frontal = seq.lastVowel().frontalVowel;
                             boolean round = seq.lastVowel().roundedVowel;
                             if (frontal && round) {
                                 ilist.add(new TurkicSeq(seq).append(TurkishAlphabet.L_uu));
                             } else if (frontal && !round) {
                                 ilist.add(new TurkicSeq(seq).append(TurkishAlphabet.L_i));
                             } else if (!frontal && round) {
                                 ilist.add(new TurkicSeq(seq).append(TurkishAlphabet.L_uu));
                             } else if (!frontal && !round) {
                                 ilist.add(new TurkicSeq(seq).append(TurkishAlphabet.L_ii));
                             }
                         }
                     }
                     sequences = ilist;
                     break;
 
                 case DEVOICE:
                     ArrayList<TurkicSeq> dlist = new ArrayList<TurkicSeq>();
                     for (TurkicSeq sequence : sequences) {
                         dlist.add(new TurkicSeq(sequence).append(token.l));
                        dlist.add(new TurkicSeq(sequence).append(alphabet.devoice(token.l)));
                     }
                     sequences = dlist;
                     break;
 
                 case VOICE:
                     ArrayList<TurkicSeq> vlist = new ArrayList<TurkicSeq>();
                     for (TurkicSeq sequence : sequences) {
                         vlist.add(new TurkicSeq(sequence).append(token.l));
                         vlist.add(new TurkicSeq(sequence).append(alphabet.voice(token.l)));
                     }
                     sequences = vlist;
                     break;
 
                 case APPEND:
                     ArrayList<TurkicSeq> aplist = new ArrayList<TurkicSeq>();
                     for (TurkicSeq sequence : sequences) {
                         aplist.add(new TurkicSeq(sequence).append(token.l));
                         aplist.add(new TurkicSeq(sequence));
                     }
                     sequences = aplist;
                     break;
             }
         }
         return sequences;
     }
 
     private SuffixNode[] generateNodes(TurkishSuffix suffix, String generationWord) {
         List<SuffixNode> nodes = new ArrayList<SuffixNode>();
         for (TurkicSeq sequence : generateFromSuffixString(generationWord)) {
             nodes.add(new SuffixNode(suffix, sequence, defineMorphemicAttributes(sequence)));
         }
         return nodes.toArray(new SuffixNode[nodes.size()]);
     }
 
     // in suffix, defining morphemic attributes is straight forward.
     private Set<MorphAttr> defineMorphemicAttributes(TurkicSeq seq) {
         Set<MorphAttr> attributes = new HashSet<MorphAttr>();
         if (seq.hasVowel()) {
             if (seq.lastVowel().isFrontalVowel())
                 attributes.add(MorphAttr.LastVowelFrontal);
         }
         if (seq.lastLetter().isVowel()) {
             attributes.add(MorphAttr.LastLetterVowel);
         }
         if (seq.lastLetter().isStopConsonant()) {
             attributes.add(MorphAttr.LastLetterVoicelessStop);
         }
 
         return attributes;
     }
 
 
     private static enum TokenType {
         I_WOVEL,
         A_WOVEL,
         VOICE,
         DEVOICE,
         APPEND,
         LETTER
     }
 
     private static class SuffixToken {
         TokenType type;
         TurkicLetter l;
 
         private SuffixToken(TokenType type, TurkicLetter l) {
             this.type = type;
             this.l = l;
         }
     }
 
     class SuffixStringTokenizer implements Iterator<SuffixToken> {
 
         private int pointer;
         private final String generationWord;
 
 
         public SuffixStringTokenizer(String generationWord) {
             this.generationWord = generationWord;
         }
 
         public boolean hasNext() {
             return generationWord != null && pointer < generationWord.length();
         }
 
         public SuffixToken next() {
             if (!hasNext()) {
                 throw new NoSuchElementException("no elements left!");
             }
             char p = generationWord.charAt(pointer++);
             switch (p) {
                 case '+':
                     return new SuffixToken(TokenType.APPEND, alphabet.getLetter(generationWord.charAt(pointer++)));
                 case '>':
                     return new SuffixToken(TokenType.DEVOICE, alphabet.getLetter(generationWord.charAt(pointer++)));
                 case '~':
                     return new SuffixToken(TokenType.VOICE, alphabet.getLetter(generationWord.charAt(pointer++)));
                 case 'I':
                     return new SuffixToken(TokenType.I_WOVEL, TurkicLetter.UNDEFINED);
                 case 'A':
                     return new SuffixToken(TokenType.A_WOVEL, TurkicLetter.UNDEFINED);
                 default:
                     return new SuffixToken(TokenType.LETTER, alphabet.getLetter(p));
             }
         }
 
         public void remove() {
         }
     }
 
     public static void main(String[] args) {
         TurkishSuffixes suffixes = new TurkishSuffixes();
         suffixes.generate();
        for (SuffixNode node : TurkishSuffixes.Acc_yI.nodes) {
             System.out.println(node.content);
         }
     }
 }
