 /**
  * Copyright (c) 2012-2013 Ojus Software Labs Private Limited.
  * 
  * All rights reserved. Please see the files README.md, LICENSE and COPYRIGHT
  * for details.
  */
 
 package com.ojuslabs.oct.core;
 
 import static com.ojuslabs.oct.common.Constants.LIST_SIZE_L;
 import static com.ojuslabs.oct.common.Constants.LIST_SIZE_S;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.ojuslabs.oct.common.BondOrder;
 import com.ojuslabs.oct.common.Constants;
 import com.ojuslabs.oct.lib.IRingDetector;
 
 /**
  * Molecule represents a chemical molecule. It holds information concerning its
  * atoms, bonds, rings, <i>etc.</i> <b>N.B.</b> A molecule is expected to be
  * connected, <i>i.e.,</i> it cannot have more than a single component.
  */
 public class Molecule
 {
     /* A running serial unique identifier for molecules. */
     private static long            _nextId = 0;
 
     /* A unique ID. This does not change during the lifetime of the molecule. */
     private final long             _id;
 
     /* Atoms currently belonging to this molecule. */
     private final List<Atom>       _atoms;
     /* Bonds binding the atoms in this molecule. */
     private final List<Bond>       _bonds;
     /* Rings in all the ring systems in this molecule. */
     private final List<Ring>       _rings;
     /* The ring systems formed by the rings in this molecule. */
     private final List<RingSystem> _ringSystems;
 
     /* Keeps track of running IDs of atoms. */
     private int                    _peakAId;
     /* Keeps track of running IDs of bonds. */
     private int                    _peakBId;
     /* Keeps track of running IDs of rings. */
     private int                    _peakRId;
 
     /* Vendor-assigned unique ID of this molecule. */
     public String                  vendorMoleculeId;
     /* Name of the vendor. */
     public String                  vendorName;
 
     /*
      * The following pair of lists for attributes is needed because the input
      * order of the attributes is lost otherwise.
      */
 
     /* Attribute names from either input or run-time. */
     private final List<String>     _attrNames;
     /* Corresponding attribute values. */
     private final List<String>     _attrValues;
 
     /*
      * A matrix with inter-atomic distances (in hops). This matrix uses input
      * IDs, since only those are available when this computation takes place.
      */
     private int[][]                _dists;
 
     /*
      * A matrix used in (re)constructing shortest paths between any two atoms.
      * Like the above, this too employs input IDs.
      */
     private int[][]                _paths;
 
     /**
      * Factory method for creating molecules with unique IDs.
      * 
      * @return A new, uniquely-identifiable molecule.
      */
     public static synchronized Molecule newInstance() {
         return new Molecule(++_nextId);
     }
 
     /**
      * The molecule's initialisation.
      */
     Molecule(long id) {
         _id = id;
 
         _atoms = Lists.newArrayListWithCapacity(LIST_SIZE_L);
         _bonds = Lists.newArrayListWithCapacity(LIST_SIZE_L);
         _rings = Lists.newArrayListWithCapacity(LIST_SIZE_L);
         _ringSystems = Lists.newArrayListWithCapacity(LIST_SIZE_L);
 
         _attrNames = Lists.newArrayListWithCapacity(LIST_SIZE_S);
         _attrValues = Lists.newArrayListWithCapacity(LIST_SIZE_S);
     }
 
     /**
      * @return The globally unique ID of this molecule.
      */
     public long id() {
         return _id;
     }
 
     /**
      * @param id
      *            Unique normalised ID of the atom in this molecule.
      * @return The atom with the given normalised ID.
      */
     public Atom atom(int id) {
         return _atoms.get(id - 1);
     }
 
     /**
      * @param id
      *            Input ID of the atom in this molecule.
      * @return Requested atom if found; {@code null} otherwise.
      */
     public Atom atomByInputId(int id) {
         for (Atom a : _atoms) {
             if (a.inputId() == id) {
                 return a;
             }
         }
 
         return null;
     }
 
     /**
      * @param id
      *            Unique ID of the bond in this molecule.
      * @return Requested bond if found; {@code null} otherwise.
      */
     public Bond bond(int id) {
         for (Bond b : _bonds) {
             if (b.id() == id) {
                 return b;
             }
         }
 
         return null;
     }
 
     /**
      * Answers the bond in this molecule between the two given atoms. The atoms
      * must belong to this molecule; else an exception is thrown.
      * 
      * @param a1
      *            One of the atoms in the bond.
      * @param a2
      *            The other atom in the bond.
      * @return The bond between the two given atoms, if one such exists;
      *         {@code null} otherwise.
      * @throws IllegalArgumentException
      *             if the given atoms do not belong to this molecule.
      */
     public Bond bondBetween(Atom a1, Atom a2) {
         if ((this != a1.molecule()) || (this != a2.molecule())) {
             throw new IllegalArgumentException(
                     String.format(
                             "At least one of the given atoms does not belong to this molecule: %d, atoms: %d->%d, %d->%d",
                             _id, a1.molecule().id(), a1.id(), a2.molecule()
                                     .id(), a2.id()));
         }
 
         return unsafeBondBetween(a1, a2);
     }
 
     /*
      * This part is reusable internally within this package without incurring
      * the overhead of the membership checks.
      */
     Bond unsafeBondBetween(Atom a1, Atom a2) {
         if (_bonds.isEmpty()) {
             return null;
         }
 
         int hash = Bond.hash(a1, a2);
         for (Bond b : _bonds) {
             if (b.hashValue() == hash) {
                 return b;
             }
         }
 
         return null;
     }
 
     /**
      * @param id
      *            Unique ID of the ring in this molecule.
      * @return Requested ring if found; {@code null} otherwise.
      */
     public Ring ring(int id) {
         for (Ring r : _rings) {
             if (r.id() == id) {
                 return r;
             }
         }
 
         return null;
     }
 
     /**
      * @return The number of atoms in this molecule.
      */
     public int numberOfAtoms() {
         return _atoms.size();
     }
 
     /**
      * @return The number of bonds in this molecule.
      */
     public int numberOfBonds() {
         return _bonds.size();
     }
 
     /**
      * @return The number of double bonds in this molecule.
      */
     public int numberOfDoubleBonds() {
         int c = 0;
         for (Bond b : _bonds) {
             if (BondOrder.DOUBLE == b.order()) {
                 c++;
             }
         }
         return c;
     }
 
     /**
      * @return The number of triple bonds in this molecule.
      */
     public int numberOfTripleBonds() {
         int c = 0;
         for (Bond b : _bonds) {
             if (BondOrder.TRIPLE == b.order()) {
                 c++;
             }
         }
         return c;
     }
 
     /**
      * @return The number of rings in this molecule.
      */
     public int numberOfRings() {
         return _rings.size();
     }
 
     /**
      * <b>N.B.</b> This method does <b>not</b> determine the aromaticity of the
      * rings in this molecule. This oly queries the corresponding attribute in
      * the rings. The actual determination of aromaticity is assumed to have
      * been already performed elsewhere.
      * 
      * @return The number of aromatic rings in this molecule.
      */
     public int numberOfAromaticRings() {
         int c = 0;
         for (Ring r : _rings) {
             if (r.isAromatic()) {
                 c++;
             }
         }
 
         return c;
     }
 
     /**
      * @return A read-only copy of this molecule's atoms.
      */
     public List<Atom> atoms() {
         return ImmutableList.copyOf(_atoms);
     }
 
     /**
      * @return A read-only copy of this molecule's bonds.
      */
     public List<Bond> bonds() {
         return ImmutableList.copyOf(_bonds);
     }
 
     /**
      * @return A read-only copy of this molecule's rings.
      */
     public List<Ring> rings() {
         return ImmutableList.copyOf(_rings);
     }
 
     /**
      * @return A read-only copy of this molecule's ring systems.
      */
     public List<RingSystem> ringSystems() {
         return ImmutableList.copyOf(_ringSystems);
     }
 
     /**
      * Adds the given atom to this molecule. Should it be needed, it first
      * removes it from its current containing molecule. The rest of the state of
      * the atom (such as its bonds) is also cleared.
      * 
      * @param a
      *            The atom to be added to this molecule.
      */
     public void addAtom(Atom a) {
         if (this == a.molecule()) {
             return;
         }
 
         a.setMolecule(this, true);
         a.setInputId(++_peakAId);
         _atoms.add(a);
     }
 
     /**
      * Adds the given newly-created atom to this molecule. <b>N.B. The given
      * atom should be a fresh atom: <i>i.e.,</i> it should never have been a
      * part of any molecule so far. Violating this will lead to undefined
      * results.</b>
      * 
      * @param a
      *            The atom to be added to this molecule.
      * @see #addAtom(Atom)
      */
     public void addNewAtom(Atom a) {
         a.setMolecule(this, false);
         a.setInputId(++_peakAId);
         _atoms.add(a);
     }
 
     /**
      * Adds a bond in this molecule between the two given atoms, if one such
      * does not exist already. Caller should ensure that the given atoms are not
      * {@code null}.
      * 
      * @param a1
      *            One of the atoms to be bonded.
      * @param a2
      *            The other atom to be bonded.
      * @param order
      *            Must be single, double, triple or aromatic.
      * @throws IllegalArgumentException
      *             if given atoms do not belong to this molecule, or an invalid
      *             bond order is given.
      * @throws IllegalStateException
      *             if forming this bond would violate the valence of at least
      *             one of the participating atoms.
      * @see com.ojuslabs.oct.common.BondOrder
      */
     public Bond addBond(Atom a1, Atom a2, BondOrder order) {
         if ((this != a1.molecule()) || (this != a2.molecule())) {
             throw new IllegalArgumentException(
                     String.format(
                             "Molecule: %d, atom1's molecule: %d, atom2's molecule: %d",
                             _id, a1.molecule().id(), a2.molecule().id()));
         }
         if ((BondOrder.SINGLE != order) &&
                 (BondOrder.DOUBLE != order) &&
                 (BondOrder.TRIPLE != order) &&
                 (BondOrder.AROMATIC != order)) {
             throw new IllegalArgumentException(
                     String.format(
                             "Only single, double, triple and aromatic bonds are allowed; given %s.",
                             order.name()));
         }
 
         /* Is this bond already present? */
         Bond tb = bondBetween(a1, a2);
         if (null != tb) {
             return tb;
         }
 
         /* Is it legal to form this bond between the given atoms? */
         boolean ok1 = a1.tryChangeNumberOfNeighbours(order.value());
         boolean ok2 = a2.tryChangeNumberOfNeighbours(order.value());
         if (!ok1 || !ok2) {
             throw new IllegalStateException(
                     String.format(
                             "Valence violation: molecule %d, atom1 %d, atom2 %d, new bond order %d.",
                             _id, a1.valence(), a2.valence(), order.value()));
         }
 
         Bond b = new Bond(++_peakBId, a1, a2, order);
 
         /* Set neighbours appropriately. */
         a1.unsafeAddBond(b);
         a2.unsafeAddBond(b);
         _bonds.add(b);
 
         return b;
     }
 
     /**
      * Breaks the given bond. Its participating atoms are suitably adjusted. The
      * rings in which it participates are all broken, and hence cleared.
      * 
      * @param b
      *            The bond to be broken.
      * @throws IllegalArgumentException
      *             if the given bond does not belong to this molecule.
      */
     public void breakBond(Bond b) {
         int idx = _bonds.indexOf(b);
         if (-1 == idx) {
             throw new IllegalArgumentException(
                     String.format(
                             "Given bond is not in this molecule. Molecule: %d, bond: %d",
                             _id, b.id()));
         }
 
         unsafeBreakBond(b, idx);
     }
 
     /*
      * Bypasses the membership check.
      */
     void unsafeBreakBond(Bond b, int idx) {
         /* Update both atoms. */
         b.atom1().removeBond(b);
         b.atom2().removeBond(b);
         for (Ring r : b.rings()) {
             removeRing(r);
         }
 
         if (Integer.MIN_VALUE == idx) {
             _bonds.remove(b);
         }
         else {
             _bonds.remove(idx);
         }
     }
 
     /*
      * Rings are broken only indirectly through one of their bonds. Hence this
      * method is package-internal.
      */
     void removeRing(Ring r) {
         for (Atom a : r.atoms()) {
             a.removeRing(r);
         }
         for (Bond b : r.bonds()) {
             b.removeRing(r);
         }
 
         _rings.remove(r);
     }
 
     /**
      * Removes the given atom from this molecule.
      * 
      * Usually, but not always, this method is invoked indirectly, when the atom
      * is either detached or is attached to a different molecule.
      * 
      * @param a
      *            The atom to remove from this molecule.
      * @throws IllegalArgumentException
      *             if the given atom does not belong to this molecule.
      */
     public void removeAtom(Atom a) {
         int idx = _atoms.indexOf(a);
         if (-1 == idx) {
             throw new IllegalArgumentException(
                     String.format(
                             "Given atom is not in this molecule. Molecule: %d, atom: %d",
                             _id, a.id()));
         }
 
         unsafeRemoveAtom(a, idx);
     }
 
     /*
      * Bypasses the membership check.
      */
     void unsafeRemoveAtom(Atom a, int idx) {
         for (Bond b : a.bonds()) {
             unsafeBreakBond(b, Integer.MIN_VALUE);
         }
 
         if (Integer.MIN_VALUE == idx) {
             _atoms.remove(a);
         }
         else {
             _atoms.remove(idx);
         }
     }
 
     /**
      * Adds an externally constructed ring to this molecule.
      * 
      * @param r
      *            The ring constructed using this molecule's atoms (and bonds).
      * @throws IllegalArgumentException
      *             if the given ring has a different containing molecule, or if
      *             the ring has not been completed.
      */
     void addRing(Ring r) {
         if (this != r.molecule()) {
             throw new IllegalArgumentException(
                     String.format(
                             "The given ring has a different parent.  Current molecule: %d, ring's molecule: %d.",
                             _id, r.molecule().id()));
         }
 
         if (!r.isCompleted()) {
             throw new IllegalArgumentException(
                     "Given ring is not a closed graph.");
         }
 
         if (_rings.contains(r)) {
             return;
         }
 
         r.setId(++_peakRId);
         for (Atom a : r.atoms()) {
             a.addRing(r);
         }
         for (Bond b : r.bonds()) {
             b.addRing(r);
         }
         _rings.add(r);
     }
 
     /**
      * Adds a name-value pair to this molecule's set of attributes, only if the
      * given name does <b>not</b> already occur therein.
      * 
      * @param name
      *            The name of the attribute; must not be empty.
      * @param value
      *            The value of the attribute; must not be empty.
      * @throws IllegalArgumentException
      *             if either the name or the value is empty.
      * @throws IllegalStateException
      *             if the given attribute name already exists.
      */
     public void addAttribute(String name, String value) {
         if (name.isEmpty() || value.isEmpty()) {
             throw new IllegalArgumentException(
                     "Either the given attribute name or its value is empty.");
         }
         if (_attrNames.contains(name)) {
             throw new IllegalStateException(
                     "The given attribute name already exists.");
         }
 
         _attrNames.add(name);
         _attrValues.add(value);
     }
 
     /**
      * @param name
      *            The name of the attribute; must not be empty.
      * @param value
      *            The value of the attribute; must not be empty.
      * @return The value previously set for this attribute name.
      * @throws IllegalArgumentException
      *             if either the name or the value is empty.
      * @throws NoSuchElementException
      *             if the given attribute name does not exist.
      */
     public String setAttribute(String name, String value) {
         if (name.isEmpty() || value.isEmpty()) {
             throw new IllegalArgumentException(
                     "Either the given attribute name or its value is empty.");
         }
 
         int idx = _attrNames.indexOf(name);
         if (-1 == idx) {
             throw new NoSuchElementException(
                     "The given attribute name does not exist.");
         }
 
         return _attrValues.set(idx, value);
     }
 
     /**
      * @param name
      *            The attribute to remove from this molecule.
      * @return {@code true} upon successful deletion; {@code false} otherwise.
      */
     public boolean removeAttribute(String name) {
         if (null == name || name.isEmpty()) {
             return false;
         }
 
         int idx = _attrNames.indexOf(name);
         if (-1 == idx) {
             return false;
         }
 
         _attrNames.remove(idx);
         _attrValues.remove(idx);
         return true;
     }
 
     /**
      * @param name
      *            The attribute whose presence needs to be checked.
      * @return {@code true} if the given attribute exists; {@code false}
      *         otherwise.
      */
     public boolean hasAttribute(String name) {
         return _attrNames.contains(name);
     }
 
     /**
      * @param name
      *            The attribute whose value is requested.
      * @return The requested value, if found.
      * @throws NoSuchElementException
      *             if the given attribute is not found.
      */
     public String attribute(String name) {
         int idx = _attrNames.indexOf(name);
         if (-1 == idx) {
             throw new NoSuchElementException(String.format(
                     "The given attribute `%s' does not exist.", name));
         }
 
         return _attrValues.get(idx);
     }
 
     /**
      * <b>N.B.</b> Note that values can repeat. Hence, this method answers the
      * <b><i>first</i></b> attribute (in input order) that has the given value.
      * Do not use this method for more specific results in the case of repeating
      * values.
      * 
      * @param value
      *            The attribute value whose name is requested.
      * @return The requested name, if found.
      * @throws NoSuchElementException
      *             if the given attribute value is not found.
      */
     public String attributeNameFor(String value) {
         int idx = _attrValues.indexOf(value);
         if (-1 == idx) {
             throw new NoSuchElementException(String.format(
                     "The given attribute value `%s' does not exist.", value));
         }
 
         return _attrNames.get(idx);
     }
 
     /**
      * @return A read-only copy of this molecule's attributes.
      */
     public List<String> attributes() {
         return ImmutableList.copyOf(_attrNames);
     }
 
     /**
      * This computation utilises the famous Floyd-Warshall algorithm to compute
      * the shortest pair-wise distances in the molecule (graph).
      */
     void computeAtomicDistances() {
         int size = _peakAId + 1; // IDs start with 1, not 0!
 
         /* Allocate and initialise the distance matrix. */
         _dists = new int[size][size];
         for (int i = 1; i < size; i++) {
             Arrays.fill(_dists[i], Integer.MAX_VALUE);
             _dists[i][i] = 0;
         }
         /* Allocate the paths matrix. Defaults of 0 are intended and fine. */
         _paths = new int[size][size];
 
         /* Distances between immediate neighbours are always 1. */
         for (Bond b : _bonds) {
             int id1 = b.atom1().inputId();
             int id2 = b.atom2().inputId();
             _dists[id1][id2] = _dists[id2][id1] = 1;
         }
 
         /*
          * For distinct x, y and z, given x and z are NOT neighbours, d(x, z) =
          * min({d(x, y) + d(y, z) for all y}).
          */
         for (int k = 1; k < size; k++) {
             for (int i = 1; i < size; i++) {
                 for (int j = 1; j < size; j++) {
                     if ((k == i) || (k == j) || (i == j)) {
                         continue;
                     }
                     if ((Integer.MAX_VALUE == _dists[i][k])
                             || (Integer.MAX_VALUE == _dists[k][j])) {
                         continue;
                     }
 
                     if (_dists[i][k] + _dists[k][j] < _dists[i][j]) {
                         _dists[i][j] = _dists[j][i] = _dists[i][k]
                                 + _dists[k][j];
                         _paths[i][j] = _paths[j][i] = k;
                     }
                 }
             }
         }
     }
 
     /**
      * @param inputId1
      *            Input ID of the first atom of the pair.
      * @param inputId2
      *            Input ID of the second atom of the pair.
      * @return {@code 0} if the two given IDs correspond to the same atom;
      *         {@code Integer.MAX_VALUE} if the two atoms are not connected; the
      *         length of the shortest path between the two given atoms,
      *         otherwise.
      */
     public int distanceBetween(int inputId1, int inputId2) {
         return _dists[inputId1][inputId2];
     }
 
     /**
      * Prints the atomic distances matrix to standard out.
      */
     public void dumpAtomicDistances() {
         int na = _atoms.size() + 1;
         for (int i = 1; i < na; i++) {
             System.out.println(String.format("Atom input ID: %d", i));
             for (int j = 1; j < na; j++) {
                 System.out.println(String.format("\t(%d, %d): %d", i, j,
                         _dists[i][j]));
             }
             System.out.println();
         }
     }
 
     /**
      * <b>N.B.</b> This method answers a path of only intermediate nodes
      * (atoms). It does <b><i>not</i></b> include the end atoms.
      * 
      * @param inputId1
      *            The atom where the path should begin.
      * @param inputId2
      *            The atom where the path should end.
      * @return {@code null} if the two given atoms are not connected; an empty
      *         list if the two given atoms are directly connected; a list of
      *         intermediate nodes along the shortest path connecting the two
      *         given atoms, otherwise.
      */
     public List<Integer> shortestPathBetween(int inputId1, int inputId2) {
         if (Integer.MAX_VALUE == _dists[inputId1][inputId2]) {
             return null;
         }
 
         int i = _paths[inputId1][inputId2];
         if (0 == i) { // The two given atoms are directly connected.
             return new ArrayList<Integer>();
         }
         else {
             List<Integer> path = new ArrayList<Integer>();
             path.addAll(shortestPathBetween(inputId1, i));
             path.add(i);
             path.addAll(shortestPathBetween(i, inputId2));
             return path;
         }
     }
 
     /**
      * This method converts the molecule into a standard, normalised internal
      * representation. Such a normalised representation enables a simple
      * mechanism for reasoning about the state of the molecule at any given
      * instant.
      * <p>
      * This method is idempotent if the molecule has not been altered since the
      * last call.
      * <p>
      * <b>N.B. This method lies at the foundations of a substantial part of this
      * toolkit, either directly or indirectly. Exercise caution when modifying
      * this for whatever purposes.</b>
      * 
      * @param rd
      *            The specific ring detector to employ for detecting the rings
      *            in this molecule.
      */
     public void normalise(IRingDetector rd) {
         /* Compute inter-atomic distance matrix. */
         computeAtomicDistances();
 
         /* Determine the unsaturation value of atoms. */
         for (Atom a : _atoms) {
             a.determineUnsaturation();
         }
 
         /*
          * Detect the presence of rings and ring systems in the molecule. This
          * also updates the unsaturation of aromatic ring members.
          */
         detectRings(rd);
 
         for (Atom a : _atoms) {
             a.computeHashValue();
             a.computeSHashValue();
         }
 
         /*
          * We now sort atoms based on their atomic number, unsaturation and
          * number of attached hydrogens.
          */
         reorderAtoms1();
         assignNormalisedIds();
 
        /* Normalise atoms. */
        for (Atom a : _atoms) {
            a.normalise();
        }

        /*
         * We now sort atoms based on their atomic number, unsaturation, number
         * of attached hydrogens and the tuple of sorted neighbours.
         */
        reorderAtoms2();
        assignNormalisedIds();
 
        /* Normalise atoms again. */
        for (Atom a : _atoms) {
            a.normalise();
         }
 
         /* Normalise rings. */
         for (Ring r : _rings) {
             r.normalise();
         }
     }
 
     /**
      * We sort atoms as follows:
      * <ul>
      * <li>descending on atomic number,</li>
      * <li>descending on unsaturation and</li>
      * <li>ascending on number of attached hydrogens.</li>
      * </ul>
      */
     void reorderAtoms1() {
         Comparator<Atom> c = new Comparator<Atom>() {
             @Override
             public int compare(Atom a1, Atom a2) {
                 return compare1(a1, a2);
             }
         };
         Collections.sort(_atoms, c);
     }
 
     /**
      * We sort atoms as follows:
      * <ul>
      * <li>descending on atomic number,</li>
      * <li>descending on unsaturation,</li>
      * <li>ascending on number of attached hydrogens,</li>
      * <li>ascending on the tuple of sorted neighbours.</li>
      * </ul>
      */
     void reorderAtoms2() {
         Comparator<Atom> c = new Comparator<Atom>() {
             @Override
             public int compare(Atom a1, Atom a2) {
                 return compare2(a1, a2);
             }
         };
         Collections.sort(_atoms, c);
     }
 
     private int compare1(Atom a1, Atom a2) {
         int sh1 = a1.sHashValue();
         int sh2 = a2.sHashValue();
        return (sh1 < sh2) ? -1 : (sh1 == sh2) ? 0 : 1;
     }
 
     private int compare2(Atom a1, Atom a2) {
         int c1 = compare1(a1, a2);
         if (0 != c1) {
             return c1;
         }
 
         /*
          * If we have reached here, both atoms should also have the same number
          * of neighbours.
          */
         List<Atom> nbrs1 = a1.neighbours();
         List<Atom> nbrs2 = a2.neighbours();
         for (int i = 0; i < nbrs1.size(); i++) {
             Atom t1 = nbrs1.get(i);
             Atom t2 = nbrs2.get(i);
             if (t1.id() < t2.id()) {
                 return -1; // Bonds to a higher atom.
             }
             else if (t1.id() > t2.id()) {
                 return 1; // Bonds to a lower atom.
             }
         }
 
         return 0; // Identical atoms; do not sort.
     }
 
     /**
      * This walks through the sorted atoms list, assigning a serial normalised
      * ID to each atom.
      */
     private void assignNormalisedIds() {
         int id = 0;
         for (Atom a : _atoms) {
             a.setId(++id);
         }
     }
 
     /**
      * Detects the rings in this molecule, and stores information regarding the
      * same appropriately. <b>N.B.</b> The actual detection is performed by the
      * provided ring detector.
      * 
      * @param rd
      *            The specific ring detector to employ for detecting the rings
      *            in this molecule.
      */
     void detectRings(IRingDetector rd) {
         resetRingInformation();
 
         int f = frerejacque();
         /* Check to see if the molecule is acyclic (or disconnected). */
         if (f <= 0) {
             return;
         }
         /* We don't deal with molecules having too many rings. */
         if (f > Constants.MAX_RINGS) {
             return;
         }
 
         /* Delegate detection to an appropriate detector. */
         rd.initialise(this);
         rd.detectRings();
 
         /* Store information regarding rings and ring systems. */
         for (Ring r : rd.rings()) {
             addRing(r);
             r.determineAromaticity();
         }
         for (RingSystem rs : rd.ringSystems()) {
             _ringSystems.add(rs);
         }
 
         /* Mark bridge head atoms. */
         for (Atom a : rd.bridgeHeads()) {
             a.markAsBridgeHead(true);
         }
     }
 
     /**
      * Resets all the ring information in this molecule, and removes the rings
      * themselves.
      */
     void resetRingInformation() {
         for (Atom a : _atoms) {
             a.resetRings();
         }
         for (Bond b : _bonds) {
             b.resetRings();
         }
 
         _rings.clear();
         _peakRId = 0;
     }
 
     /**
      * Frerejacque property is used to determine if a molecule (graph) is cyclic
      * or acyclic. A frerejacque value less than {@code 0} means that the
      * molecule has disconnected components. A frerejacque value of {@code 0}
      * means that the molecule is a single component, but is acyclic. A positive
      * frerejacque number is the number of rings in the molecule.
      * 
      * @return {@code num_bonds - num_atoms + 1}.
      */
     public int frerejacque() {
         return _bonds.size() - _atoms.size() + 1;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode() {
         return (int) _id;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (!(obj instanceof Molecule)) {
             return false;
         }
 
         Molecule other = (Molecule) obj;
         if (_id != other.id()) {
             return false;
         }
 
         return true;
     }
 }
