 package pl.poznan.put.cs.bioserver.helper;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.biojava.bio.structure.Atom;
 import org.biojava.bio.structure.Chain;
 import org.biojava.bio.structure.Group;
 import org.biojava.bio.structure.Structure;
 import org.biojava.bio.structure.StructureException;
 
 public class Helper {
     private static final Logger LOGGER = Logger.getLogger(Helper.class);
 
     public static boolean isNucleicAcid(Chain c) {
         int amino = 0;
         int nucleotide = 0;
         for (Group g : c.getAtomGroups()) {
             if (isAminoAcid(g)) {
                 amino++;
             } else if (isNucleotide(g)) {
                 nucleotide++;
             } else {
                 LOGGER.warn("Group is neither amino acid, nor nucleotide: " + g);
             }
         }
         return nucleotide > amino;
     }
 
     public static boolean isNucleotide(Group g) {
         return g.getType().equals("nucleotide") || g.hasAtom("P");
     }
 
     public static boolean isAminoAcid(Group g) {
         return g.getType().equals("amino") || g.hasAminoAtoms();
     }
 
     public static void normalizeAtomNames(Chain c) {
         for (Group g : c.getAtomGroups()) {
             for (Atom a : g.getAtoms()) {
                 a.setName(a.getName().replace('*', '\''));
                 a.setFullName(a.getFullName().replace('*', '\''));
             }
         }
     }
 
     public static void normalizeAtomNames(Structure s) {
         for (Chain c : s.getChains()) {
             Helper.normalizeAtomNames(c);
         }
     }
 
     public static List<Atom> getAtomArray(Chain c, String[] atomNames) {
         Map<Group, List<Atom>> map = new HashMap<>();
         for (Group g : c.getAtomGroups()) {
             if (!(isNucleotide(g) || isAminoAcid(g)))
                 continue;
             List<Atom> list = new ArrayList<>();
             boolean foundAny = false;
             for (String name : atomNames) {
                 try {
                     Atom atom = g.getAtom(name);
                     list.add(atom);
                     foundAny = true;
                 } catch (StructureException e) {
                     list.add(null);
                 }
             }
             if (foundAny) {
                 map.put(g, list);
             }
         }
 
         List<Atom> result = new ArrayList<>();
         for (Group g : c.getAtomGroups()) {
             if (map.containsKey(g)) {
                 result.addAll(map.get(g));
             }
         }
         return result;
     }
 
     public static Atom[][] getCommonAtomArray(Chain c1, Chain c2,
             String[] usedAtoms) {
         List<Atom> l1 = getAtomArray(c1, usedAtoms);
         List<Atom> l2 = getAtomArray(c2, usedAtoms);
         assert l1.size() == l2.size();
        for (int i = 0; i < l1.size(); i++) {
             if (l1.get(i) == null || l2.get(i) == null) {
                 l1.remove(i);
                 l2.remove(i);
             }
         }
         return new Atom[][] { l1.toArray(new Atom[l1.size()]),
                 l2.toArray(new Atom[l2.size()]) };
     }
 
     public static Atom[][] getCommonAtomArray(Structure s1, Structure s2,
             String[] usedAtoms) throws StructureException {
         assert s1.getChains().size() == s2.getChains().size();
         Atom[][] result = new Atom[][] { new Atom[0], new Atom[0] };
         for (Chain c1 : s1.getChains()) {
             Atom[][] common = getCommonAtomArray(c1,
                     s2.getChainByPDB(c1.getChainID()), usedAtoms);
 
             for (int i = 0; i < 2; i++) {
                 Atom[] tmp = new Atom[common[i].length + result[i].length];
                 System.arraycopy(result[i], 0, tmp, 0, result[i].length);
                 System.arraycopy(common[i], 0, tmp, result[i].length,
                         common[i].length);
                 result[i] = tmp;
             }
         }
         return result;
     }
 
     public static boolean isNucleicAcid(Structure structure) {
         boolean flag = true;
         for (Chain c : structure.getChains())
             flag &= isNucleicAcid(c);
         return flag;
     }
 }
