 package zemberek3.lexicon.graph;
 
 import com.google.common.collect.Maps;
 import zemberek3.lexicon.*;
 import zemberek3.structure.AttributeSet;
 import zemberek3.structure.TurkicLetter;
 import zemberek3.structure.TurkicSeq;
 import zemberek3.structure.TurkishAlphabet;
 
 import java.util.*;
 
 import static zemberek3.lexicon.RootAttr.*;
 import static zemberek3.lexicon.TurkishSuffixes.*;
 
 /**
  * This class gets a list of Lexicon Items and builds the dictionary part of the main graph.
  */
 public class LexiconGraph {
     List<DictionaryItem> dictionary;
     List<StemNode> stems = new ArrayList<StemNode>();
     TurkishAlphabet alphabet = new TurkishAlphabet();
     TurkishSuffixes suffixes;
     SuffixFormGenerator formGenerator = new SuffixFormGenerator();
 
     private Map<SuffixFormSet, Set<SuffixNode>> suffixFormMap = Maps.newHashMap();
 
     public LexiconGraph(List<DictionaryItem> dictionary, TurkishSuffixes suffixes) {
         this.dictionary = dictionary;
         this.suffixes = suffixes;
         for (SuffixFormSet set : suffixes.getSets()) {
             suffixFormMap.put(set.remove(), new HashSet<SuffixNode>());
         }
     }
 
     public Iterable<SuffixNode> getNodes(SuffixFormSet set) {
         return suffixFormMap.get(set);
     }
 
     public void generate() {
         // generate stems.
         for (DictionaryItem dictionaryItem : dictionary) {
             if (hasModifierAttribute(dictionaryItem)) {
                 stems.addAll(Arrays.asList(generateModifiedRootStates(dictionaryItem)));
             } else {
                 stems.add(generateRootState(dictionaryItem));
             }
         }
         // generate suffix form graph
         List<SuffixFormSet> sets = Arrays.asList(ROOT_FORMS);
         generateSuffixForms(sets);
     }
 
     public List<StemNode> getStems() {
         return stems;
     }
 
     private StemNode generateRootState(DictionaryItem dictionaryItem) {
         SuffixFormSet set = suffixes.getRootSuffixFormSet(dictionaryItem.primaryPos);
         AttributeSet<PhonAttr> phoneticAttrs = calculateRootAttributes(dictionaryItem);
         SuffixNode node = addOrReturnExisting(set, formGenerator.getNode(phoneticAttrs, set));
         return new StemNode(dictionaryItem.clean(), dictionaryItem, node, TerminationType.TERMINAL);
     }
 
     public boolean hasModifierAttribute(DictionaryItem item) {
         return item.attrs.containsAny(modifiers);
     }
 
     AttributeSet<RootAttr> modifiers = new AttributeSet<RootAttr>(
             Doubling,
             LastVowelDrop,
             ProgressiveVowelDrop,
             Voicing,
             StemChange,
             CompoundP3sg
     );
 
     private AttributeSet<PhonAttr> calculateRootAttributes(DictionaryItem dictionaryItem) {
         TurkicSeq sequence = new TurkicSeq(dictionaryItem.clean(), alphabet);
         AttributeSet<PhonAttr> attrs = calculateAttributes(sequence);
         if (dictionaryItem.hasAttribute(RootAttr.InverseHarmony)) {
             // saat, takat
             attrs.add(PhonAttr.LastVowelFrontal);
         }
         return attrs;
     }
 
     private AttributeSet<PhonAttr> calculateAttributes(TurkicSeq sequence) {
         AttributeSet<PhonAttr> attrs = new AttributeSet<PhonAttr>();
         // general phonetic attributes.
         if (sequence.lastVowel().isRounded())
             attrs.add(PhonAttr.LastVowelRounded);
         else
             attrs.add(PhonAttr.LastVowelUnrounded);
         if (sequence.lastVowel().isFrontal()) {
             attrs.add(PhonAttr.LastVowelFrontal);
         } else
             attrs.add(PhonAttr.LastVowelBack);
         if (sequence.lastLetter().isVowel()) {
             // elma
             attrs.add(PhonAttr.LastLetterVowel);
         } else
             attrs.add(PhonAttr.LastLetterConsonant);
         if (sequence.lastLetter().isVoiceless() && sequence.lastLetter().isStopConsonant()) {
             // kitap
             attrs.add(PhonAttr.LastLetterVoicelessStop);
         } else
             attrs.add(PhonAttr.LastLetterNotVoicelessStop);
         return attrs;
     }
 
 
     private StemNode[] generateModifiedRootStates(DictionaryItem lexItem) {
 
         TurkicSeq modifiedSeq = new TurkicSeq(lexItem.clean(), alphabet);
         AttributeSet<PhonAttr> originalAttrs = calculateRootAttributes(lexItem);
         AttributeSet<PhonAttr> modifiedAttrs = originalAttrs.copy();
 
         for (RootAttr attribute : lexItem.attrs.getAsList(RootAttr.class)) {
 
             // generate other boundary attributes and modified root state.
             switch (attribute) {
                 case Voicing:
                     if (lexItem.hasAttribute(RootAttr.CompoundP3sg))
                         break;
                     TurkicLetter last = modifiedSeq.lastLetter();
                     TurkicLetter modifiedLetter = alphabet.voice(last);
                     if (modifiedLetter == null) {
                         throw new LexiconException("Voicing letter is not proper in:" + lexItem);
                     }
                     if (lexItem.lemma.endsWith("nk"))
                         modifiedLetter = TurkishAlphabet.L_g;
                     modifiedSeq.changeLetter(modifiedSeq.length() - 1, modifiedLetter);
                     break;
                 case Doubling:
                     modifiedSeq.append(modifiedSeq.lastLetter());
                     break;
                 case LastVowelDrop:
                     modifiedSeq.delete(modifiedSeq.length() - 2);
                     break;
                 case ProgressiveVowelDrop:
                     modifiedSeq.delete(modifiedSeq.length() - 1);
                     break;
                 case StemChange:
                     return handleSpecialStems(lexItem);
                 case CompoundP3sg:
                     return handleP3sgCompounds(lexItem);
                 default:
                     break;
             }
         }
         SuffixFormSet origSet = null;
         SuffixFormSet modSet = null;
         switch (lexItem.primaryPos) {
             case Noun:
                 if (lexItem.attrs.containsAny(Voicing, Doubling, LastVowelDrop)) {
                     origSet = TurkishSuffixes.Noun_Exp_C;
                     modSet = TurkishSuffixes.Noun_Exp_V;
                 }
                 if (lexItem.attrs.contains(Voicing)) {
                     modifiedAttrs.remove(PhonAttr.LastLetterVoicelessStop);
                 }
                 break;
             case Verb:
                 if (lexItem.attrs.contains(Voicing)) {
                     origSet = TurkishSuffixes.Verb_Vow_NotDrop;
                     modSet = TurkishSuffixes.Verb_Vow_Drop;
                 } else if (lexItem.attrs.contains(LastVowelDrop)) {
                     origSet = TurkishSuffixes.Verb_Vow_NotDrop;
                     modSet = TurkishSuffixes.Verb_Vow_Drop;
                 } else if (lexItem.attrs.contains(ProgressiveVowelDrop)) {
                     origSet = TurkishSuffixes.Verb_Prog_NotDrop;
                     modSet = TurkishSuffixes.Verb_Prog_Drop;
                 } else if (lexItem.attrs.contains(Aorist_A)) {
                     modSet = TurkishSuffixes.Verb_Aor_Ar;
                 }
                 break;
 
         }
         if (origSet == null || modSet == null) {
             throw new IllegalStateException("Cannot find suffix root form to connect for dictionary item:" + lexItem);
         }
 
         SuffixNode origForm = addOrReturnExisting(origSet, formGenerator.getNode(originalAttrs, origSet));
         SuffixNode modiForm = addOrReturnExisting(modSet, formGenerator.getNode(modifiedAttrs, modSet));
 
         return new StemNode[]{
                 new StemNode(lexItem.clean(), lexItem, origForm, TerminationType.TERMINAL),
                 new StemNode(modifiedSeq.toString(), lexItem, modiForm, TerminationType.NON_TERMINAL)
         };
     }
 
     public SuffixNode addOrReturnExisting(SuffixFormSet set, SuffixNode newNode) {
         Set<SuffixNode> nodes = suffixFormMap.get(set);
         if (!nodes.contains(newNode)) {
             nodes.add(newNode);
             return newNode;
         }
         for (SuffixNode node : nodes) {
             if (node.equals(newNode))
                 return node;
         }
         throw new IllegalStateException("Cannot be here.");
     }
 
     private boolean nodeExists(SuffixFormSet set, SuffixNode newNode) {
         Set<SuffixNode> nodes = suffixFormMap.get(set);
         return nodes.contains(newNode);
     }
 
     public SuffixNode getSuffixRootNode(DictionaryItem item, SuffixFormSet set) {
         return addOrReturnExisting(set, formGenerator.getNode(calculateRootAttributes(item), set));
     }
 
     // handle stem changes demek-diyecek , beni-bana
     private StemNode[] handleSpecialStems(DictionaryItem item) {
 
         if (item.getId().equals("yemek_Verb")) {
             StemNode[] stems = new StemNode[3];
             SuffixNode formYe = getSuffixRootNode(item, Verb_Ye);
             stems[0] = new StemNode(item.clean(), item, formYe, TerminationType.TERMINAL);
             SuffixNode formProg = getSuffixRootNode(item, Verb_Prog_Drop);
             stems[1] = new StemNode(item.lemma.substring(0, 1), item, formProg, TerminationType.NON_TERMINAL);
             SuffixNode formYi = getSuffixRootNode(item, Verb_Yi);
             stems[2] = new StemNode("yi", item, formYi, TerminationType.NON_TERMINAL);
             return stems;
         }
         if (item.getId().equals("demek_Verb")) {
             StemNode[] stems = new StemNode[3];
             SuffixNode formDe = getSuffixRootNode(item, Verb_De);
             stems[0] = new StemNode(item.clean(), item, formDe, TerminationType.TERMINAL);
             SuffixNode formProg = getSuffixRootNode(item, Verb_Prog_Drop);
             stems[1] = new StemNode(item.lemma.substring(0, 1), item, formProg, TerminationType.NON_TERMINAL);
             SuffixNode formDi = getSuffixRootNode(item, Verb_Di);
             stems[2] = new StemNode("di", item, formDi, TerminationType.NON_TERMINAL);
             return stems;
         }
         if (item.getId().equals("ben_Pron") || item.getId().equals("sen_Pron")) {
             StemNode[] stems = new StemNode[2];
             SuffixNode formBenSen = getSuffixRootNode(item, Pron_BenSen);
             stems[0] = new StemNode(item.clean(), item, formBenSen, TerminationType.TERMINAL);
             SuffixNode formBanSan = getSuffixRootNode(item, Pron_BanSan);
             if (item.lemma.equals("ben"))
                 stems[1] = new StemNode("ban", item, formBanSan, TerminationType.NON_TERMINAL);
             else
                 stems[1] = new StemNode("san", item, formBanSan, TerminationType.NON_TERMINAL);
             return stems;
         }
         throw new IllegalArgumentException("Lexicon Item with special stem change cannot be handled:" + item);
     }
 
 
     private StemNode[] handleP3sgCompounds(DictionaryItem item) {
 
         StemNode[] nodes = new StemNode[2];
         // atkuyruGu -
         SuffixNode original = getSuffixRootNode(item, Noun_Comp_P3sg);
         nodes[0] = new StemNode(item.clean(), item, original, TerminationType.TERMINAL);
 
         TurkicSeq modifiedSeq = new TurkicSeq(item.lemma, alphabet);
         String modified = modifiedSeq.eraseLast().toString();
         // for cases compund's last word has voicing. atkuruGu
         if (item.hasAttribute(Voicing)) {
             // hack for cases denizbiyoloGu - denizbiyologlari
             if (modified.endsWith("o" + String.valueOf(TurkishAlphabet.C_gg)))
                 modifiedSeq.eraseLast().append(TurkishAlphabet.L_g);
             else {
                 TurkicLetter l = alphabet.devoice(modifiedSeq.lastLetter());
                 modifiedSeq.changeLetter(modifiedSeq.length() - 1, l);
             }
             SuffixNode mod = getSuffixRootNode(item, Noun_Comp_P3sg_Root);
             nodes[1] = new StemNode(modifiedSeq.toString(), item, mod, TerminationType.NON_TERMINAL);
         } else {
             SuffixNode mod = getSuffixRootNode(item, Noun_Comp_P3sg_Root);
             nodes[1] = new StemNode(modified, item, mod, TerminationType.NON_TERMINAL);
         }
         return nodes;
     }
 
     public void generateSuffixForms(Iterable<SuffixFormSet> startForms) {
         Set<SuffixFormSet> toProcess = new HashSet<SuffixFormSet>();
         for (SuffixFormSet rootFormSet : startForms) {
             for (SuffixFormSet succSet : rootFormSet.getSuccessorsIterable()) {
                 for (SuffixNode node : suffixFormMap.get(rootFormSet)) {
                     SuffixNode nodeInSuccessor = formGenerator.getNode(node.attributes, succSet);
                     if (!nodeExists(succSet, nodeInSuccessor)) {
                         toProcess.add(succSet);
                     }
                     nodeInSuccessor = addOrReturnExisting(succSet, nodeInSuccessor);
                     node.addSuccNode(nodeInSuccessor);
                 }
             }
         }
         if (toProcess.size() == 0)
             return;
         generateSuffixForms(toProcess);
     }
 }
