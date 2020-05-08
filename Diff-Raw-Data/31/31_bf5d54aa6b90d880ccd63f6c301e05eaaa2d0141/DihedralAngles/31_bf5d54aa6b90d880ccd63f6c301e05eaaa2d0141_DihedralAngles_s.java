 package pl.poznan.put.cs.bioserver.torsion;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.biojava.bio.structure.Atom;
 import org.biojava.bio.structure.Group;
 
 /**
  * A class to calculate and manage dihedral angles for given BioJava structure.
  * 
  * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
  */
 public class DihedralAngles {
     private static final Logger LOGGER = Logger.getLogger(DihedralAngles.class);
 
     /**
      * Implementation of torsion angle calculation for amino acids.
      * 
      * @param groups
      *            Amino acids.
      * @return Collection of calculated dihedral angles.
      */
     // private static List<AminoAcidDihedral> calculateAminoAcidDihedrals(
     // List<Group> groups) {
     // Vector<AminoAcidDihedral> aminoDihedrals = new Vector<>();
     // if (groups.size() == 0) {
     // return aminoDihedrals;
     // }
     //
     // Atom[][] atoms = new Atom[3][];
     // atoms[0] = new Atom[AminoAtoms.values().length];
     // atoms[1] = DihedralAngles.getAtoms(groups.get(0), AminoAtoms.values());
     // for (int i = 0; i < groups.size(); ++i) {
     // if (i == groups.size() - 1) {
     // atoms[2] = new Atom[AminoAtoms.values().length];
     // } else {
     // atoms[2] = DihedralAngles.getAtoms(groups.get(i + 1),
     // AminoAtoms.values());
     // }
     //
     // double phi = DihedralAngles.calculateDihedral(
     // atoms[0][AminoAtoms.C.ordinal()],
     // atoms[1][AminoAtoms.N.ordinal()],
     // atoms[1][AminoAtoms.CA.ordinal()],
     // atoms[1][AminoAtoms.C.ordinal()]);
     // double psi = DihedralAngles.calculateDihedral(
     // atoms[1][AminoAtoms.N.ordinal()],
     // atoms[1][AminoAtoms.CA.ordinal()],
     // atoms[1][AminoAtoms.C.ordinal()],
     // atoms[2][AminoAtoms.N.ordinal()]);
     // double omega = DihedralAngles.calculateDihedral(
     // atoms[1][AminoAtoms.CA.ordinal()],
     // atoms[1][AminoAtoms.C.ordinal()],
     // atoms[2][AminoAtoms.N.ordinal()],
     // atoms[2][AminoAtoms.CA.ordinal()]);
     //
     // Group group = atoms[1][0].getGroup();
     // atoms[0] = atoms[1];
     // atoms[1] = atoms[2];
     //
     // AminoAcidDihedral dihedral = new AminoAcidDihedral(phi, psi, omega,
     // group);
     // aminoDihedrals.add(dihedral);
     // }
     // return aminoDihedrals;
     // }
 
     /**
      * Implementation of torsion angle calculation for nucleotides.
      * 
      * @param groups
      *            Nucleotides.
      * @return Collection of calculated dihedral angles.
      */
     // private static List<NucleotideDihedral> calculateNucleotideDihedrals(
     // List<Group> groups) {
     // Vector<NucleotideDihedral> nucleotideDihedrals = new Vector<>();
     // if (groups.size() == 0) {
     // return nucleotideDihedrals;
     // }
     //
     // Atom[][] atoms = new Atom[3][];
     // atoms[0] = new Atom[NucleotideAtoms.values().length];
     // atoms[1] = DihedralAngles.getAtoms(groups.get(0),
     // NucleotideAtoms.values());
     //
     // for (int i = 0; i < groups.size(); ++i) {
     // if (i == groups.size() - 1) {
     // atoms[2] = new Atom[NucleotideAtoms.values().length];
     // } else {
     // atoms[2] = DihedralAngles.getAtoms(groups.get(i + 1),
     // NucleotideAtoms.values());
     // }
     //
     // double alpha = DihedralAngles.calculateDihedral(
     // atoms[0][NucleotideAtoms.O3P.ordinal()],
     // atoms[1][NucleotideAtoms.P.ordinal()],
     // atoms[1][NucleotideAtoms.O5P.ordinal()],
     // atoms[1][NucleotideAtoms.C5P.ordinal()]);
     // double beta = DihedralAngles.calculateDihedral(
     // atoms[1][NucleotideAtoms.P.ordinal()],
     // atoms[1][NucleotideAtoms.O5P.ordinal()],
     // atoms[1][NucleotideAtoms.C5P.ordinal()],
     // atoms[1][NucleotideAtoms.C4P.ordinal()]);
     // double gamma = DihedralAngles.calculateDihedral(
     // atoms[1][NucleotideAtoms.O5P.ordinal()],
     // atoms[1][NucleotideAtoms.C5P.ordinal()],
     // atoms[1][NucleotideAtoms.C4P.ordinal()],
     // atoms[1][NucleotideAtoms.C3P.ordinal()]);
     // double delta = DihedralAngles.calculateDihedral(
     // atoms[1][NucleotideAtoms.C5P.ordinal()],
     // atoms[1][NucleotideAtoms.C4P.ordinal()],
     // atoms[1][NucleotideAtoms.C3P.ordinal()],
     // atoms[1][NucleotideAtoms.O3P.ordinal()]);
     // double epsilon = DihedralAngles.calculateDihedral(
     // atoms[1][NucleotideAtoms.C4P.ordinal()],
     // atoms[1][NucleotideAtoms.C3P.ordinal()],
     // atoms[1][NucleotideAtoms.O3P.ordinal()],
     // atoms[2][NucleotideAtoms.P.ordinal()]);
     // double dzeta = DihedralAngles.calculateDihedral(
     // atoms[1][NucleotideAtoms.C3P.ordinal()],
     // atoms[1][NucleotideAtoms.O3P.ordinal()],
     // atoms[2][NucleotideAtoms.P.ordinal()],
     // atoms[2][NucleotideAtoms.O5P.ordinal()]);
     //
     // String pdbName = groups.get(i).getPDBName();
     // pdbName = pdbName.trim();
     // double chi;
     // if (pdbName.equals("G") || pdbName.equals("A")) {
     // // Guanine or Adenine
     // chi = DihedralAngles.calculateDihedral(
     // atoms[1][NucleotideAtoms.O4P.ordinal()],
     // atoms[1][NucleotideAtoms.C1P.ordinal()],
     // atoms[1][NucleotideAtoms.N9.ordinal()],
     // atoms[1][NucleotideAtoms.C4.ordinal()]);
     // } else {
     // // Uracil or Cytosine
     // chi = DihedralAngles.calculateDihedral(
     // atoms[1][NucleotideAtoms.O4P.ordinal()],
     // atoms[1][NucleotideAtoms.C1P.ordinal()],
     // atoms[1][NucleotideAtoms.N1.ordinal()],
     // atoms[1][NucleotideAtoms.C2.ordinal()]);
     // }
     //
     // double[] tau = new double[5];
     // tau[0] = DihedralAngles.calculateDihedral(
     // atoms[1][NucleotideAtoms.C4P.ordinal()],
     // atoms[1][NucleotideAtoms.O4P.ordinal()],
     // atoms[1][NucleotideAtoms.C1P.ordinal()],
     // atoms[1][NucleotideAtoms.C2P.ordinal()]);
     // tau[1] = DihedralAngles.calculateDihedral(
     // atoms[1][NucleotideAtoms.O4P.ordinal()],
     // atoms[1][NucleotideAtoms.C1P.ordinal()],
     // atoms[1][NucleotideAtoms.C2P.ordinal()],
     // atoms[1][NucleotideAtoms.C3P.ordinal()]);
     // tau[2] = DihedralAngles.calculateDihedral(
     // atoms[1][NucleotideAtoms.C1P.ordinal()],
     // atoms[1][NucleotideAtoms.C2P.ordinal()],
     // atoms[1][NucleotideAtoms.C3P.ordinal()],
     // atoms[1][NucleotideAtoms.C4P.ordinal()]);
     // tau[3] = DihedralAngles.calculateDihedral(
     // atoms[1][NucleotideAtoms.C2P.ordinal()],
     // atoms[1][NucleotideAtoms.C3P.ordinal()],
     // atoms[1][NucleotideAtoms.C4P.ordinal()],
     // atoms[1][NucleotideAtoms.O4P.ordinal()]);
     // tau[4] = DihedralAngles.calculateDihedral(
     // atoms[1][NucleotideAtoms.C3P.ordinal()],
     // atoms[1][NucleotideAtoms.C4P.ordinal()],
     // atoms[1][NucleotideAtoms.O4P.ordinal()],
     // atoms[1][NucleotideAtoms.C1P.ordinal()]);
     // double pSin = tau[1] + tau[4] - tau[0] - tau[3];
     // double pCos = 2.0 * tau[2];
     // pCos *= Math.sin(36.0 * Math.PI / 180.0)
     // + Math.sin(72.0 * Math.PI / 180.0);
     // double P = Math.atan2(pSin, pCos);
     //
     // Group group = atoms[1][0].getGroup();
     // atoms[0] = atoms[1];
     // atoms[1] = atoms[2];
     //
     // NucleotideDihedral dihedral = new NucleotideDihedral(alpha, beta,
     // gamma, delta, epsilon, dzeta, chi, P, group);
     // nucleotideDihedrals.add(dihedral);
     // }
     // return nucleotideDihedrals;
     // }
 
     // private static DihedralContainer computeDihedrals(List<Group> continuous)
     // {
     // List<Group> aminoVector = new Vector<>();
     // List<Group> nucleotideVector = new Vector<>();
     //
     // for (Group g : continuous) {
     // Group group = (Group) g.clone();
     // for (Group altLoc : g.getAltLocs()) {
     // for (Atom a : altLoc.getAtoms()) {
     // group.addAtom(a);
     // }
     // }
     // String type = group.getType();
     // if (type.equals("amino") || group.hasAminoAtoms()) {
     // aminoVector.add(group);
     // } else if (type.equals("nucleotide") || group.hasAtom("P")) {
     // nucleotideVector.add(group);
     // }
     //
     // }
     //
     // List<AminoAcidDihedral> aad = DihedralAngles
     // .calculateAminoAcidDihedrals(aminoVector);
     // List<NucleotideDihedral> nd = DihedralAngles
     // .calculateNucleotideDihedrals(nucleotideVector);
     // return new DihedralContainer(aad, nd);
     // }
 
     /**
      * Calculate one dihedral angle value for given four atoms. Use cos^-1 and a
      * check for pseudovector
      * 
      * @param a1
      *            Atom 1.
      * @param a2
      *            Atom 2.
      * @param a3
      *            Atom 3.
      * @param a4
      *            Atom 4.
      * @return Dihedral angle between atoms 1-4.
      */
     public static double calculateDihedralAcos(Atom a1, Atom a2, Atom a3,
             Atom a4) {
         if (a1 == null || a2 == null || a3 == null || a4 == null) {
             return Double.NaN;
         }
 
         Vector3D d1 = new Vector3D(a1, a2);
         Vector3D d2 = new Vector3D(a2, a3);
         Vector3D d3 = new Vector3D(a3, a4);
 
         Vector3D u1 = d1.cross(d2);
         Vector3D u2 = d2.cross(d3);
 
         double ctor = u1.dot(u2) / Math.sqrt(u1.dot(u1) * u2.dot(u2));
         ctor = ctor < -1 ? -1 : ctor > 1 ? 1 : ctor;
         double torp = Math.acos(ctor);
         if (u1.dot(u2.cross(d2)) < 0)
             torp = -torp;
         return torp;
     }
 
     /**
      * Calculate one dihedral angle value for given four atoms.
      * 
      * @param a1
      *            Atom 1.
      * @param a2
      *            Atom 2.
      * @param a3
      *            Atom 3.
      * @param a4
      *            Atom 4.
      * @return Dihedral angle between atoms 1-4.
      */
     public static double calculateDihedralAtan(Atom a1, Atom a2, Atom a3,
             Atom a4) {
         if (a1 == null || a2 == null || a3 == null || a4 == null) {
             return Double.NaN;
         }
 
         Vector3D v1 = new Vector3D(a1, a2);
         Vector3D v2 = new Vector3D(a2, a3);
         Vector3D v3 = new Vector3D(a3, a4);
 
         Vector3D tmp1 = v1.cross(v2);
         Vector3D tmp2 = v2.cross(v3);
         Vector3D tmp3 = v1.scale(v2.length());
         return Math.atan2(tmp3.dot(tmp2), tmp1.dot(tmp2));
     }
 
     public static double calculateDihedral(Atom a1, Atom a2, Atom a3, Atom a4) {
         return calculateDihedralAtan(a1, a2, a3, a4);
     }
 
     public static double calculateDihedral(Atom[] atoms) {
         return calculateDihedral(atoms[0], atoms[1], atoms[2], atoms[3]);
     }
 
     public static List<AngleDifference> calculateAngleDiff(Atom[][] atoms,
             AngleType angleType) {
         LOGGER.debug("Number of atoms: " + atoms[0].length + " "
                 + atoms[1].length);
         List<Atom[]> quads1 = getQuadruplets(atoms[0], angleType);
         List<Atom[]> quads2 = getQuadruplets(atoms[1], angleType);
         LOGGER.debug("Number of quadruplets: " + quads1.size() + " "
                 + quads2.size());
 
         Map<Atom, Integer> map1 = makeReverseMap(atoms[0]);
         Map<Atom, Integer> map2 = makeReverseMap(atoms[1]);
 
         if (quads1.size() < quads2.size()) {
             List<Atom[]> quadsTmp = quads1;
             quads1 = quads2;
             quads2 = quadsTmp;
         }
 
         List<AngleDifference> differences = new ArrayList<>();
         for (int i = 0; i < quads1.size(); i++) {
             Atom[] q1 = quads1.get(i);
             boolean found = false;
             for (int j = 0; j < quads2.size(); j++) {
                 Atom[] q2 = quads2.get(j);
 
                 int[][] ids = new int[][] { new int[4], new int[4] };
                 for (int k = 0; k < 4; k++) {
                     ids[0][k] = map1.get(q1[k]);
                     ids[1][k] = map2.get(q2[k]);
                 }
 
                 if (Arrays.equals(ids[0], ids[1])) {
                     AngleDifference diff = new AngleDifference(q1, q2);
                     differences.add(diff);
                     found = true;
                     break;
                 }
             }
 
             if (!found) {
                 AngleDifference diff = new AngleDifference(q1, new Atom[4]);
                 differences.add(diff);
             }
         }
         return differences;
     }
 
     private static Map<Atom, Integer> makeReverseMap(Atom[] atoms) {
         Map<Atom, Integer> map = new HashMap<>();
         for (int i = 0; i < atoms.length; i++) {
             map.put(atoms[i], i);
         }
         return map;
     }
 
     private static List<Atom[]> getQuadruplets(Atom[] atoms, AngleType angleType) {
         int[] groupRule = angleType.getGroupRule();
         List<List<Atom>> found = new ArrayList<>();
         for (int j = 0; j < 4; j++) {
             List<Atom> list = new ArrayList<>();
             found.add(list);
         }
 
         for (Atom atom : atoms) {
             for (int k = 0; k < 4; k++) {
                 String[] atomNames = angleType.getAtomNames(atom.getGroup());
                 if (atom.getFullName().equals(atomNames[k])) {
                     found.get(k).add(atom);
                     break;
                 }
             }
         }
 
         List<Atom[]> filtered = new ArrayList<>();
         int size = found.get(0).size();
         for (int j = 0; j < size; j++) {
             Atom refAtom = found.get(0).get(j);
             Group refGroup = refAtom.getGroup();
             int refId = refGroup.getResidueNumber().getSeqNum();
             String refChain = refGroup.getChainId();
 
             List<Atom> quad = new ArrayList<>();
             quad.add(refAtom);
             for (int k = 1; k < 4; k++) {
                 for (Atom atom : found.get(k)) {
                     Group group = atom.getGroup();
                     int id = group.getResidueNumber().getSeqNum();
                     String chain = group.getChainId();
                     if (id - refId == groupRule[k] && refChain.equals(chain)) {
                         quad.add(atom);
                         break;
                     }
                 }
             }
 
             if (quad.size() == 4) {
                 filtered.add(quad.toArray(new Atom[quad.size()]));
             } else {
                 LOGGER.debug("Quad not found, got only " + quad.size()
                         + " atoms. Angle: " + angleType.getAngleName()
                         + ". Residue of first atom: "
                         + quad.get(0).getGroup().getResidueNumber()
                         + ". Atoms: "
                         + Arrays.toString(quad.toArray(new Atom[quad.size()])));
             }
         }
         return filtered;
     }
 
     /**
      * Subtract two angles (circular values) and return the difference.
      * 
      * @param a1
      *            First angle.
      * @param a2
      *            Second angle.
      * @return Difference between angles.
      */
     public static double subtractDihedral(double a1, double a2) {
         double diff;
         // both angles are NaN, reward!
         if (Double.isNaN(a1) && Double.isNaN(a2)) {
             diff = 0;
         } else if (Double.isNaN(a1) && !Double.isNaN(a2) || !Double.isNaN(a1)
                 && Double.isNaN(a2)) {
             diff = Math.PI;
         } else {
             double full = 2 * Math.PI;
             double a1_mod = (a1 + full) % full;
             double a2_mod = (a2 + full) % full;
             diff = Math.abs(a1_mod - a2_mod);
             diff = Math.min(diff, full - diff);
         }
         return diff;
     }
 
     /**
      * Filter given atom group for specific, named atoms.
      * 
      * @param group
      *            Atom group (amino acid or nucleotide).
      * @param atomsNames
      *            Enumeration with names of atoms to filter.
      * @return An array of filtered atoms.
      */
     // private static Atom[] getAtoms(Group group, Enum<?>[] atomsNames) {
     // Atom[] atoms = new Atom[atomsNames.length];
     // int i = 0;
     // for (Enum<?> atomName : atomsNames) {
     // String name = atomName.name();
     // Atom atom;
     // if (name.length() > 1) {
     // atom = DihedralAngles
     // .tryGetAtom(group, name.replace('P', '\''));
     // if (atom == null) {
     // atom = DihedralAngles.tryGetAtom(group,
     // name.replace('P', '*'));
     // }
     // } else {
     // atom = DihedralAngles.tryGetAtom(group, name);
     // }
     // atoms[i++] = atom;
     //
     // }
     // return atoms;
     // }
 
     // public static DihedralContainer getDihedrals(AlignmentOutput alignment) {
     // DihedralContainer container = new DihedralContainer();
     // Group[][] compactGroups = alignment.getCompactGroups(whichChain);
     // for (Group[] groups : compactGroups) {
     // container.addAll(DihedralAngles.computeDihedrals(Arrays
     // .asList(groups)));
     // }
     // return container;
     // }
 
     // public static DihedralContainer getDihedrals(Chain chain) {
     // return DihedralAngles.computeDihedrals(chain.getAtomGroups());
     // }
 
     /**
      * Iterate through structure's chains, groups and atoms. Return the
      * collection of calculated dihedral angles for each chain.
      * 
      * @param structure
      *            Structure to calculate dihedral angles within.
      * @return A collection of dihedral angles for given chain and type. The
      *         "double[][][] result" array must be interpreted this way:
      *         result[i] is i-th chain, result[i][0] is for amino acids and
      *         result[i][1] is for nucleotides, result[i][j][k] is for k-th
      *         group.
      */
     // public static DihedralContainer getDihedrals(Structure structure) {
     // DihedralContainer allDihedrals = new DihedralContainer();
     // for (Chain c : structure.getChains()) {
     // allDihedrals.addAll(DihedralAngles.getDihedrals(c));
     // }
     // return allDihedrals;
     // }
 
     /**
      * Get an atom from given group or return null in case of errors (i.e. if
      * atom is not present).
      * 
      * @param group
      *            A residue.
      * @param name
      *            Name of an atom.
      * @return Object representing this atom.
      */
     // private static Atom tryGetAtom(Group group, String name) {
     // try {
     // return group.getAtom(name);
     // } catch (StructureException e) {
     // return null;
     // }
     // }
 }
