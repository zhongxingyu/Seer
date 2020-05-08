 package zemberek3.lexicon.tr;
 
 import zemberek3.lexicon.*;
 import zemberek3.lexicon.graph.StemNode;
 import zemberek3.lexicon.graph.SuffixData;
 import zemberek3.lexicon.graph.TerminationType;
 import zemberek3.structure.AttributeSet;
 import zemberek3.structure.TurkicLetter;
 import zemberek3.structure.TurkicSeq;
 import zemberek3.structure.TurkishAlphabet;
 
 import static zemberek3.lexicon.tr.RootAttr.*;
 import static zemberek3.lexicon.tr.TurkishSuffixes.*;
 
 
 /**
  * This class generates StemNode objects from Dictionary Items. Generated Nodes are not connected.
  */
 public class StemNodeGenerator {
 
     TurkishAlphabet alphabet = new TurkishAlphabet();
     SuffixProvider suffixProvider;
 
     public StemNodeGenerator(SuffixProvider suffixProvider) {
         this.suffixProvider = suffixProvider;
     }
 
     AttributeSet<RootAttr> modifiers = new AttributeSet<RootAttr>(
             Doubling,
             LastVowelDrop,
             ProgressiveVowelDrop,
             InverseHarmony,
             Voicing,
             VoicingOpt,
             StemChange,
             CompoundP3sg
     );
 
     /**
      * Generates StemNode objects from the dictionary item.
      * <p>Most of the time a single StemNode is generated.
      *
      * @param item DictionaryItem
      * @return one or more StemNode objects.
      */
     public StemNode[] generate(DictionaryItem item) {
         if (hasModifierAttribute(item)) {
             return generateModifiedRootNodes(item);
         } else {
             SuffixData[] roots = suffixProvider.defineSuccessorSuffixes(item);
             AttributeSet<PhonAttr> phoneticAttributes = calculateAttributes(item.root);
             StemNode stemNode = new StemNode(
                     item.root,
                     item,
                     TerminationType.TERMINAL,
                     phoneticAttributes,
                     AttributeSet.<PhoneticExpectation>emptySet());
             stemNode.exclusiveSuffixData = roots[0];
             return new StemNode[]{stemNode};
         }
     }
 
 
     public boolean hasModifierAttribute(DictionaryItem item) {
         return item.attrs.containsAny(modifiers);
     }
 
     private AttributeSet<PhonAttr> calculateAttributes(String input) {
         return calculateAttributes(new TurkicSeq(input, alphabet));
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
         if (sequence.lastLetter().isVoiceless()) {
             attrs.add(PhonAttr.LastLetterVoiceless);
             if (sequence.lastLetter().isStopConsonant()) {
                 // kitap
                 attrs.add(PhonAttr.LastLetterVoicelessStop);
             }
         } else
             attrs.add(PhonAttr.LastLetterNotVoiceless);
         return attrs;
     }
 
     private StemNode[] generateModifiedRootNodes(DictionaryItem dicItem) {
 
         if (dicItem.hasAttribute(RootAttr.StemChange))
             return handleSpecialStems(dicItem);
 
         if (dicItem.hasAttribute(CompoundP3sg))
             return handleP3sgCompounds(dicItem);
 
         TurkicSeq modifiedSeq = new TurkicSeq(dicItem.root, alphabet);
         AttributeSet<PhonAttr> originalAttrs = calculateAttributes(dicItem.root);
         AttributeSet<PhonAttr> modifiedAttrs = originalAttrs.copy();
         AttributeSet<PhoneticExpectation> originalExpectations = new AttributeSet<PhoneticExpectation>();
         AttributeSet<PhoneticExpectation> modifiedExpectations = new AttributeSet<PhoneticExpectation>();
 
         for (RootAttr attribute : dicItem.attrs.getAsList(RootAttr.class)) {
 
             // generate other boundary attributes and modified root state.
             switch (attribute) {
                 case Voicing:
                 case VoicingOpt:
                     TurkicLetter last = modifiedSeq.lastLetter();
                     TurkicLetter modifiedLetter = alphabet.voice(last);
                     if (modifiedLetter == null) {
                         throw new LexiconException("Voicing letter is not proper in:" + dicItem);
                     }
                     if (dicItem.lemma.endsWith("nk"))
                         modifiedLetter = TurkishAlphabet.L_g;
                     modifiedSeq.changeLetter(modifiedSeq.length() - 1, modifiedLetter);
                     modifiedAttrs.remove(PhonAttr.LastLetterVoicelessStop);
                    if (attribute != VoicingOpt) {
                        originalExpectations.add(PhoneticExpectation.ConsonantStart);
                    }
                     modifiedExpectations.add(PhoneticExpectation.VowelStart);
                     break;
                 case Doubling:
                     modifiedSeq.append(modifiedSeq.lastLetter());
                     originalExpectations.add(PhoneticExpectation.ConsonantStart);
                     modifiedExpectations.add(PhoneticExpectation.VowelStart);
                     break;
                 case LastVowelDrop:
                     modifiedSeq.delete(modifiedSeq.length() - 2);
                     if (!dicItem.primaryPos.equals(PrimaryPos.Verb))
                         originalExpectations.add(PhoneticExpectation.ConsonantStart);
                     modifiedExpectations.add(PhoneticExpectation.VowelStart);
                     break;
                 case InverseHarmony:
                     originalAttrs.add(PhonAttr.LastVowelFrontal);
                     originalAttrs.remove(PhonAttr.LastVowelBack);
                     modifiedAttrs.add(PhonAttr.LastVowelFrontal);
                     modifiedAttrs.remove(PhonAttr.LastVowelBack);
                     break;
                 case ProgressiveVowelDrop:
                     modifiedSeq.delete(modifiedSeq.length() - 1);
                     if (modifiedSeq.hasVowel()) {
                         modifiedAttrs = calculateAttributes(modifiedSeq);
                     }
                     break;
                 default:
                     break;
             }
         }
 
         StemNode original = new StemNode(dicItem.root, dicItem, originalAttrs, originalExpectations);
         StemNode modified = new StemNode(modifiedSeq.toString(), dicItem, modifiedAttrs, modifiedExpectations);
 
         SuffixData[] roots = suffixProvider.defineSuccessorSuffixes(dicItem);
 
         original.exclusiveSuffixData = roots[0];
         modified.exclusiveSuffixData = roots[1];
         if (original.equals(modified))
             return new StemNode[]{original};
 
         modified.setTermination(TerminationType.NON_TERMINAL);
         return new StemNode[]{original, modified};
     }
 
     // handle stem changes demek-diyecek , beni-bana
     private StemNode[] handleSpecialStems(DictionaryItem item) {
 
         TurkishSuffixes turkishSuffixes = (TurkishSuffixes) suffixProvider;
 
 
         if (item.getId().equals("yemek_Verb")) {
             StemNode[] stems = new StemNode[3];
             stems[0] = new StemNode("ye", item, TerminationType.TERMINAL, calculateAttributes(item.root));
             stems[0].exclusiveSuffixData.add(turkishSuffixes.Verb_Ye.allConnections());
             AttributeSet<PhonAttr> attrs = calculateAttributes(item.root).remove(PhonAttr.LastLetterVowel).add(PhonAttr.LastLetterConsonant);
             stems[1] = new StemNode("y", item, TerminationType.NON_TERMINAL, attrs, AttributeSet.<PhoneticExpectation>emptySet());
             stems[1].exclusiveSuffixData.add(turkishSuffixes.Verb_De_Ye_Prog.allConnections());
             stems[2] = new StemNode("yi", item, TerminationType.NON_TERMINAL, calculateAttributes(item.root));
             stems[2].exclusiveSuffixData.add(turkishSuffixes.Verb_Yi.allConnections());
             return stems;
         }
         if (item.getId().equals("demek_Verb")) {
             StemNode[] stems = new StemNode[3];
             stems[0] = new StemNode("de", item, TerminationType.TERMINAL, calculateAttributes(item.root));
             stems[0].exclusiveSuffixData.add(turkishSuffixes.Verb_De.allConnections());
             AttributeSet<PhonAttr> attrs = calculateAttributes(item.root).remove(PhonAttr.LastLetterVowel).add(PhonAttr.LastLetterConsonant);
             stems[1] = new StemNode("d", item, TerminationType.NON_TERMINAL, attrs, AttributeSet.<PhoneticExpectation>emptySet());
             stems[1].exclusiveSuffixData.add(turkishSuffixes.Verb_De_Ye_Prog.allConnections());
             stems[2] = new StemNode("di", item, TerminationType.NON_TERMINAL, calculateAttributes(item.root));
             stems[2].exclusiveSuffixData.add(turkishSuffixes.Verb_Di.allConnections());
             return stems;
         }
         if (item.getId().equals("ben_Pron") || item.getId().equals("sen_Pron")) {
             StemNode[] stems = new StemNode[2];
             stems[0] = new StemNode(item.root, item, TerminationType.TERMINAL, calculateAttributes(item.root));
             stems[0].exclusiveSuffixData.add(turkishSuffixes.PersPron_BenSen.allConnections());
             if (item.lemma.equals("ben"))
                 stems[1] = new StemNode("ban", item, TerminationType.NON_TERMINAL, calculateAttributes("san"));
             else
                 stems[1] = new StemNode("san", item, TerminationType.NON_TERMINAL, calculateAttributes("san"));
             stems[1].exclusiveSuffixData.add(turkishSuffixes.PersPron_BanSan);
             return stems;
         }
         throw new IllegalArgumentException("Lexicon Item with special stem change cannot be handled:" + item);
     }
 
     private StemNode[] handleP3sgCompounds(DictionaryItem item) {
         StemNode[] nodes = new StemNode[2];
         TurkicSeq originalSeq = new TurkicSeq(item.lemma, alphabet);
         nodes[0] = new StemNode(item.lemma, item, TerminationType.TERMINAL, calculateAttributes(originalSeq));
         TurkicSeq modifiedSeq = new TurkicSeq(item.root, alphabet);
         nodes[1] = new StemNode(modifiedSeq.toString(), item, TerminationType.NON_TERMINAL, calculateAttributes(modifiedSeq));
         SuffixData[] roots = suffixProvider.defineSuccessorSuffixes(item);
         nodes[0].exclusiveSuffixData = roots[0];
         nodes[1].exclusiveSuffixData = roots[1];
         return nodes;
     }
 
 }
