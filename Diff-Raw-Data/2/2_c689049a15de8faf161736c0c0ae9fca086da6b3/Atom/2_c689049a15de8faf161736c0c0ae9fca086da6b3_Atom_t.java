 /**
  * Copyright (c) 2012-2013 Ojus Software Labs Private Limited.
  * 
  * All rights reserved. Please see the files README.md, LICENSE and COPYRIGHT
  * for details.
  */
 
 package com.ojuslabs.oct.core;
 
 import static com.ojuslabs.oct.common.Constants.LIST_SIZE_S;
 
 import java.util.Iterator;
 import java.util.List;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.ojuslabs.oct.common.BondOrder;
 import com.ojuslabs.oct.common.Chirality;
 import com.ojuslabs.oct.common.Element;
 import com.ojuslabs.oct.common.PeriodicTable;
 import com.ojuslabs.oct.common.Radical;
 import com.ojuslabs.oct.common.Unsaturation;
 import com.ojuslabs.oct.util.Point3D;
 
 /**
  * Atom represents a chemical atom. It has various physical and chemical
  * properties, apart from the transient information attached to it during its
  * participation in reactions.
  * 
  * An atom is usually bound to a molecule, but may be free intermittently.
  */
 public final class Atom
 {
     // This atom's element type.
     private Element          _element;
     // Containing molecule, if the atom is bound to one.
     private Molecule         _mol;
     // A unique normalised ID within its molecule; 1-based.
     private int              _id;
     // The input order serial number of this atom; 1-based.
     private int              _inputId;
 
     // These may be given or computed.
     public Point3D           coordinates;
 
     // Total number of attached H atoms. These may be explicit or implicit.
     private byte             _numH;
     // Net charge of the atom.
     private byte             _charge;
     // Current valence configuration of this atom.
     private byte             _valence;
     // Current `unsaturation' value of this atom.
     private Unsaturation     _unsaturation;
 
     // A partial reflection of this atom for quick comparisons.
     private int              _hash;
 
     // Bonds this atom is a member of.
     private final List<Bond> _bonds;
     // This list in-line expands `_bonds` with repetitions for double/triple
     // bonds.
     private final List<Atom> _nbrs;
 
     private Chirality        _chirality;
     private Radical          _radical;
 
     // Rings this atom is a member of.
     private final List<Ring> _rings;
     private boolean          _inAroRing;
     private boolean          _inHetAroRing;
     // Is this atom a bridgehead of a bicyclic system of rings?
     private boolean          _isBridgeHead;
     // Is this atom the sole common atom of all of its rings?
     private boolean          _isSpiro;
 
     // The functional groups of this atom. These are in the descending order of
     // importance, i.e., the first feature is the primary functional group.
     private List<Integer>    _features;
 
     // The number of electron-donating neighbours.
     int                      _numEDNbrs;
     // The number of unsaturated electron-withdrawing neighbours.
     int                      _numUnsatEWNbrs;
     // The number of saturated electron-withdrawing neighbours.
     int                      _numSatEWNbrs;
 
     /**
      * Initialisation of a new atom.
      * 
      * @param elem
      *            The element type of this atom.
      */
     public Atom(Element elem) {
         _element = elem;
         if (null != elem) {
             _valence = (byte) _element.valence;
         }
 
         _chirality = Chirality.NONE;
         _radical = Radical.NONE;
 
         _bonds = Lists.newArrayListWithCapacity(LIST_SIZE_S);
         _nbrs = Lists.newArrayListWithCapacity(LIST_SIZE_S);
         _rings = Lists.newArrayListWithCapacity(LIST_SIZE_S);
     }
 
     /**
      * Resets the state of this atom. This method is useful when re-parenting an
      * atom. It may be useful in other scenarios as well, but use with caution!
      * <b>N.B.</b> This method is package-internal.
      */
     void reset() {
         _hash = 0;
         _chirality = Chirality.NONE;
         _radical = Radical.NONE;
 
         _bonds.clear();
         _nbrs.clear();
         _rings.clear();
 
         _inAroRing = false;
         _inHetAroRing = false;
         _isBridgeHead = false;
         _isSpiro = false;
     }
 
     /**
      * @return The element type of this atom.
      */
     public Element element() {
         return _element;
     }
 
     /**
      * Sets the isotope value of this atom. <b>N.B.</b> This method actually
      * alters the element type of this atom. Use with extreme caution.
      * 
      * @param n
      *            The mass difference with respect to that of the naturally most
      *            abundant variety.
      */
     public void setIsotope(int n) {
         _element = PeriodicTable.instance().element(
                 String.format("%s_%d", _element.symbol,
                         Math.round(_element.weight) + n));
     }
 
     /**
      * @return This atom's containing molecule, if any; else, {@code null}.
      */
     public Molecule molecule() {
         return _mol;
     }
 
     /**
      * Serves two purposes: (a) it can be used to re-parent the atom to a
      * different molecule, and (b) it can be used to detach this atom from its
      * current parent.
      * 
      * @param mol
      *            Containing molecule, if any. To remove the current parent,
      *            pass {@code null}.
      * @param clear
      *            If {@code true}, resets the state of the atom; else, leaves it
      *            as is.
      */
     void setMolecule(Molecule mol, boolean clear) {
         if (_mol == mol) {
             return;
         }
 
         if (null != _mol) {
             _mol.unsafeRemoveAtom(this, Integer.MIN_VALUE);
         }
 
         _mol = mol;
         _id = 0;
         _inputId = 0;
         if (clear) {
             reset();
         }
     }
 
     /**
      * @return The normalised ID of this atom in its current molecule.
      */
     public int id() {
         return _id;
     }
 
     /**
      * Sets the new unique normalised ID of this atom. <b>N.B.</b> This method
      * is package-internal, since only a molecule sets the IDs of its atoms.
      * 
      * @param id
      *            The new unique ID of this atom.
      */
     void setId(int id) {
         _id = id;
     }
 
     /**
      * <b>N.B.</b> The return value of this method makes sense only as long as
      * the molecule is in a pristine state after initial construction.
      * 
      * @return The input order ID of this atom in its current molecule.
      */
     public int inputId() {
         return _inputId;
     }
 
     /**
      * Sets the input ID of this atom. <b>N.B.</b> This method is
      * package-internal, since only a molecule sets the IDs of its atoms.
      * <p>
      * Initially, the normalised ID is set to be the same as the input ID, for
      * convenience. When the molecule is normalised, all atoms are assigned
      * their respective normalised IDs.
      * 
      * @param id
      *            The new normalised ID of this atom.
      */
     void setInputId(int id) {
         _inputId = id;
         _id = id;
     }
 
     /**
      * @return The current residual charge on this atom.
      */
     public int charge() {
         return _charge;
     }
 
     /**
      * @param chg
      *            The new residual charge on this atom.
      */
     public void setCharge(int chg) {
         _charge = (byte) chg;
     }
 
     /**
      * @return The current chiral configuration of this atom.
      */
     public Chirality chirality() {
         return _chirality;
     }
 
     /**
      * @param chirality
      *            The new chiral configuration of this atom.
      */
     void setChirality(Chirality chirality) {
         _chirality = chirality;
     }
 
     /**
      * @return The radical configuration of this atom.
      */
     public Radical radical() {
         return _radical;
     }
 
     /**
      * @param r
      *            The new radical configuration of this atom.
      */
     public void setRadical(Radical r) {
         _radical = r;
     }
 
     /**
      * @return The current valence configuration of this atom.
      */
     public byte valence() {
         return _valence;
     }
 
     /**
      * @param v
      *            The new valence configuration of this atom.
      */
     public void setValence(int v) {
         if (v > 0) { // We do not deal with rare gases.
             _valence = (byte) v;
         }
     }
 
     /**
      * @return Current unsaturation of this atom.
      */
     public Unsaturation unsaturation() {
         return _unsaturation;
     }
 
     /**
      * This method answers the number of distinct neighbours of this atom. It is
      * the same as the number of bonds in which this atom participates.
      * 
      * @return The number of bonds of which this atom is a member.
      */
     public int numberOfBonds() {
         return _bonds.size();
     }
 
     /**
      * The number of neighbours is what we get by in-line expanding the list of
      * bonds, accounting for repetitions of neighbours whenever the bond is
      * either double or triple.
      * 
      * @return The total number of electrons of this atom that are participating
      *         in its bonds.
      */
     public int numberOfNeighbours() {
         return _nbrs.size();
     }
 
     /**
      * @return The total number of hydrogen atoms bound to this atom.
      */
     public int numberOfHydrogens() {
         return _numH;
     }
 
     /**
      * @return An read-only copy of the bonds of this atom.
      */
     public List<Bond> bonds() {
         return ImmutableList.copyOf(_bonds);
     }
 
     /**
      * Answers the bond between this atom and the given other atom, if one
      * exists.
      * 
      * @param other
      *            The other atom potentially bound to this atom.
      * @return The requested bond between this atom and the other atom;
      *         {@code null} if one such does not exist.
      * @throws IllegalArgumentException
      */
     public Bond bondTo(Atom other) {
         if (null == other) {
             throw new IllegalArgumentException("Null atom given.");
         }
         if ((other.molecule() != _mol) || (null == _mol.atom(other._id))) {
             throw new IllegalArgumentException(String.format(
                     "Given atom does not belong this molecule: %d->%d", other
                             .molecule().id(), other._id));
         }
 
         for (Bond b : _bonds) {
             if (b.otherAtom(_id) == other) {
                 return b;
             }
         }
 
         return null;
     }
 
     /**
      * <b>N.B.</b> This method assumes that the molecule is in a normalised
      * state. Otherwise, the results are undefined!
      * 
      * @return The doubly-bonded neighbour atom with the lowest normalised ID,
      *         if one such exists; {@code null} otherwise.
      */
     public Atom firstDoublyBondedNeighbour() {
         if ((_unsaturation.compareTo(Unsaturation.DBOND_C) < 0) ||
                 (_unsaturation.compareTo(Unsaturation.DBOND_X_X) > 0)) {
             return null; // No double bond(s).
         }
 
         for (Bond b : _bonds) {
             if (BondOrder.DOUBLE == b.order()) {
                 return b.otherAtom(_id);
             }
         }
 
         // Should be dead code!
         return null;
     }
 
     /**
      * <b>N.B.</b> This method assumes that the molecule is in a normalised
      * state. Otherwise, the results are undefined!
      * 
      * @return The multiply-bonded neighbour atom with the lowest normalised ID,
      *         if one such exists; {@code null} otherwise.
      */
     public Atom firstMultiplyBondedNeighbour() {
         if (_unsaturation.compareTo(Unsaturation.DBOND_C) < 0) {
             return null; // No multiply-bonded neighbour(s).
         }
 
         for (Bond b : _bonds) {
             if (b.order().compareTo(BondOrder.SINGLE) > 0) {
                 return b.otherAtom(_id);
             }
         }
 
         // Should be dead code!
         return null;
     }
 
     /**
      * @return {@code true} if this atom participates in at least one ring;
      *         {@code false} otherwise.
      */
     public boolean isCyclic() {
         return _rings.size() > 0;
     }
 
     /**
      * @return Number of rings this atom participates in.
      */
     public int numberOfRings() {
         return _rings.size();
     }
 
     /**
      * @param r
      *            Ring in which we check this atom's membership.
      * @return {@code true} if this atom participates in the given ring;
      *         {@code false} otherwise.
      */
     public boolean inRing(Ring r) {
         if (_rings.contains(r)) {
             return true;
         }
 
         return false;
     }
 
     /**
      * @param n
      *            Required size of the ring in which this atom has to
      *            participate.
      * @return {@code true} if this atom participates in at least one ring of
      *         the given size; {@code false} otherwise.
      */
     public boolean inRingOfSize(int n) {
         for (Ring r : _rings) {
             if (r.size() == n) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * @param n
      *            Required minimum size of the ring in which this atom has to
      *            participate.
      * @return {@code true} if this atom participates in at least one ring of at
      *         least the given size; {@code false} otherwise.
      */
     public boolean inRingOfSizeAtLeast(int n) {
         for (Ring r : _rings) {
            if (r.size() >= n) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * @return The smallest ring in which this atom participates, if one such
      *         exists; {@code null} otherwise. If more than one ring of the
      *         smallest size if found, an exception is thrown.
      * @throws IllegalStateException
      */
     public Ring smallestRing() {
         if (_rings.isEmpty()) {
             return null;
         }
 
         int min = 0;
         int count = 0;
         Ring ret = null;
 
         for (Ring r : _rings) {
             int s = r.size();
             if (s == min) {
                 count++;
             }
             else if (s < min) {
                 ret = r;
                 count = 1;
             }
         }
 
         if (count > 1) {
             throw new IllegalStateException(String.format(
                     "Smallest ring size: %d, number of smallest rings: %d",
                     ret.size(), count));
         }
 
         return ret;
     }
 
     /**
      * @return A read-only copy of the rings this atom participates in.
      */
     public List<Ring> rings() {
         return ImmutableList.copyOf(_rings);
     }
 
     /**
      * <b>N.B.</b> This method does <b><i>not</i></b> compute this property. The
      * said computation is expected to have been already performed. It answers
      * the state of the internal flag.
      * 
      * @return {@code true} if this atom participates in an aromatic ring, with
      *         or without a hetero atom; {@code false} otherwise.
      */
     public boolean isAromatic() {
         if (_inAroRing || _inHetAroRing) {
             return true;
         }
 
         return false;
     }
 
     /**
      * <b>N.B.</b> This method does <b><i>not</i></b> compute this property. The
      * said computation is expected to have been already performed. It answers
      * the state of the internal flag.
      * 
      * @return {@code true} if this atom participates in a hetero aromatic ring;
      *         {@code false} otherwise.
      */
     public boolean inHeteroAromaticRing() {
         return _inHetAroRing;
     }
 
     /**
      * <b>N.B.</b> This condition has to be used carefully, because we are
      * imposing a heuristic limit of 8 member atoms for a useful ring.
      * 
      * @return {@code true} if the current atom does not participate in any
      *         ring, or if it participates in at least one ring of size 9 or
      *         more.
      */
     public boolean isMechanisticallyAcyclic() {
         return (0 == _rings.size()) || inRingOfSizeAtLeast(9);
     }
 
     /**
      * Checks to see that the proposed +ve or -ve change in the number of
      * neighbours is valid for this atom's current valence configuration. This
      * method is package-internal currently.
      * 
      * @param delta
      *            The number by which the number of neighbours may be liable to
      *            change.
      * @return {@code true} if the proposed change is within acceptable valence
      *         limits; {@code false} otherwise.
      */
     boolean tryChangeNumberOfNeighbours(int delta) {
         if (_nbrs.size() + delta > _valence) {
             return false;
         }
 
         return true;
     }
 
     /**
      * Adds the given bond to this atom, performing no validations.
      * 
      * This method does <b><i>not</i></b> check to see if the proposed increment
      * in the number of neighbours is valid for this atom's current valence
      * configuration. <b>N.B.</b> The current atom must be a part of the given
      * bond. However, this is assumed to have been taken care of by the parent
      * molecule.
      * 
      * @param b
      *            The bond to be added to this atom.
      * @return {@code true} upon successful addition; {@code false} otherwise.
      */
     boolean unsafeAddBond(Bond b) {
         if (_bonds.contains(b)) {
             return false;
         }
 
         _bonds.add(b);
         int delta = b.order().value();
         Atom other = b.otherAtom(_id);
         for (int i = 0; i < delta; i++) {
             _nbrs.add(other);
         }
 
         return true;
     }
 
     /**
      * Adds the given bond to this atom. <b>N.B.</b> The current atom must
      * participate in the given bond; however, this is assumed to have been
      * taken care of by the parent molecule.
      * 
      * @param b
      *            The bond to add to this atom.
      * @return {@code true} upon successful addition; {@code false} otherwise.
      * @throws IllegalStateException
      *             should the addition of this bond increase the number of bonds
      *             of this atom beyond its current valence configuration.
      */
     boolean addBond(Bond b) {
         if (tryChangeNumberOfNeighbours(b.order().value())) {
             throw new IllegalStateException(
                     String.format(
                             "Atom: %d, valence: %d, current number of bonds: %d, order of the new bond: %d",
                             _id, _valence, numberOfBonds(), b.order().value()));
         }
 
         return unsafeAddBond(b);
     }
 
     /**
      * Removes the given bond from this atom. It also adjusts the neighbours
      * list appropriately. <b>N.B.</b> The current atom must participate in the
      * given bond; however, this is assumed to have been taken care of by the
      * parent molecule. Accordingly, this method is package-internal.
      * 
      * @param b
      *            The bond to remove from this atom.
      * @return <b>true</b> if the given bond was actually removed; <b>false</b>
      *         otherwise.
      */
     boolean removeBond(Bond b) {
         Atom other = b.otherAtom(_id);
         Iterator<Atom> it = _nbrs.iterator();
         while (it.hasNext()) {
             if (it.next() == other) {
                 it.remove();
             }
         }
 
         return _bonds.remove(b);
     }
 
     /**
      * @param r
      *            The ring to add to this atom. <b>N.B.</b> The current atom
      *            must participate in the given ring; however, this is assumed
      *            to have been taken care of by the parent molecule.
      *            Accordingly, this method is package-internal.
      */
     void addRing(Ring r) {
         if (!_rings.contains(r)) {
             _rings.add(r);
         }
     }
 
     /**
      * @param r
      *            The ring to add to this atom. <b>N.B.</b> The current atom
      *            must participate in the given ring; however, this is assumed
      *            to have been taken care of by the parent molecule.
      *            Accordingly, this method is package-internal.
      * @return True if the given ring was actually removed; false otherwise.
      */
     boolean removeRing(Ring r) {
         return _rings.remove(r);
     }
 
     /**
      * @return {@code true} if this atom is currently a bridgehead;
      *         {@code false} otherwise.
      */
     public boolean isBridgeHead() {
         return _isBridgeHead;
     }
 
     /**
      * @param mark
      *            {@code true} if this atom should be set as a bridgehead;
      *            {@code false} to reset the state to being not a bridgehead.
      */
     void markAsBridgeHead(boolean mark) {
         _isBridgeHead = mark;
     }
 
     /**
      * @return {@code true} if this atom is currently in a spiro configuration;
      *         {@code false} otherwise.
      */
     public boolean isSpiro() {
         return _isSpiro;
     }
 
     /**
      * @param mark
      *            {@code true} if this atom should be set in a spiro
      *            configuration; {@code false} to reset the state to being not
      *            in a spiro configuration.
      */
     void markAsSpiro(boolean mark) {
         _isSpiro = mark;
     }
 
     /**
      * <b>N.B.</b> This method answers {@code 0} when no features are defined
      * (yet) for this atom. It is mandatory to check this return value before
      * using it.
      * 
      * @return The primary functional group ID of this atom, if one such exists;
      *         {@code 0} otherwise.
      */
     public Integer funGroup() {
         return _features.size() > 0 ? _features.get(0) : new Integer(0);
     }
 
     /**
      * Adds the given feature, if it does not already exist.
      * 
      * @param f
      *            The new feature to add to this atom.
      */
     void addFeature(Integer f) {
         if (!_features.contains(f)) {
             _features.add(f);
         }
     }
 
     /**
      * @return The total number of features, including the primary functional
      *         group, defined for this atom.
      */
     public int numberOfFeatures() {
         return _features.size();
     }
 
     /**
      * This method does <b><i>not</i></b> perform any checks on the range of the
      * given index. It is the caller's responsibility to provide a valid index.
      * 
      * @param idx
      *            The index the feature at which is requested.
      * @return The requested feature number.
      */
     public Integer feature(int idx) {
         return _features.get(idx);
     }
 
     /**
      * @param f
      *            The feature ID whose presence has to be checked.
      * @return {@code true} if the current atom has the given feature;
      *         {@code false} otherwise.
      */
     public boolean hasFeature(Integer f) {
         return _features.contains(f);
     }
 
     /**
      * Answers whether this atom can play an active role in a reaction. Note
      * that the atom may yet play an active role in a reaction, as a reaction
      * centre.
      * 
      * @return {@code true} if this atom has at least one feature defined, or is
      *         unsaturated or is not a carbon atom; {@code false} otherwise.
      */
     public boolean isFunctional() {
         return (_features.size() > 0) ||
                 (_unsaturation.compareTo(Unsaturation.NONE) > 0) ||
                 (6 != _element.number);
     }
 
     /**
      * @return The number of electron-donating neighbours of this atom.
      */
     public int numberOfEDNeighbours() {
         return _numEDNbrs;
     }
 
     /**
      * @return The total number of electron-withdrawing neighbours of this atom.
      */
     public int numberOfEWNeighbours() {
         return _numUnsatEWNbrs + _numSatEWNbrs;
     }
 
     /**
      * Answers whether this atom by itself can act as a leaving group.
      * 
      * @return {@code true} if this atom is terminal, and is not a carbon atom;
      *         {@code false} otherwise.
      */
     public boolean isAtomicLeavingGroup() {
         return (1 == _bonds.size()) && (6 != _element.number);
     }
 
     /**
      * @return {@code true} if the current atom is a carbon with exactly two
      *         hydrogen atoms bound to it; {@code false} otherwise.
      */
     public boolean isCh2() {
         return (6 == _element.number) && (2 == _numH);
     }
 
     /**
      * @return {@code true} if the current atom is a carbon with exactly three
      *         hydrogen atoms bound to it; {@code false} otherwise.
      */
     public boolean isCh3() {
         return (6 == _element.number) && (3 == _numH);
     }
 
     /**
      * @return {@code true} if the current atom is an oxygen with one hydrogen
      *         atom bound to it; {@code false} otherwise.
      */
     public boolean isHydroxyl() {
         return (8 == _element.number) && (1 == _numH);
     }
 
     /**
      * A partial reflection of this atom for quick comparisons.
      * 
      * @return A compound value that is computed using the formula
      *         <code>100 * atomic_number + 10 * unsaturation + number_of_hydrogens</code>
      *         .
      */
     public int hashValue() {
         if (0 == _hash) {
             _hash = 100 * _element.number +
                     10 * _unsaturation.value() +
                     _numH;
         }
 
         return _hash;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         return String.format("Atom ID: %d, input ID: %d, element: %s", _id,
                 _inputId, _element.symbol);
     }
 
     /**
      * Note that two atoms are considered equal only if their IDs are the same
      * AND they reside in the same molecule. Effectively, a free (unbound) atom
      * <b>cannot</b> be compared.
      * 
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof Atom)) {
             return false;
         }
 
         Atom other = (Atom) obj;
         if ((null == _mol) || (null == other.molecule())) {
             return false;
         }
         if ((_id != other._id) || (_mol.id() != other.molecule().id())) {
             return false;
         }
 
         return true;
     }
 
     /**
      * A free (unbound) atom has a hash code of 0. The hash code of a bound atom
      * depends on its molecule's globally unique ID as well as its own
      * normalised ID in that molecule.
      * 
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode() {
         if (null == _mol) {
             return 0;
         }
 
         return _id + 10000 * (int) _mol.id();
     }
 }
