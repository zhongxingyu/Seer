 /**
  * Copyright (c) 2012-2013 Ojus Software Labs Private Limited.
  * 
  * All rights reserved. Please see the files README.md, LICENSE and COPYRIGHT
  * for details.
  */
 
 package com.ojuslabs.oct.core;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.ojuslabs.oct.exception.ImmutabilityException;
 
 /**
  * Ring represents a simple cycle in a molecule. It keeps track of its atoms and
  * bonds, as well as its neighbouring rings.
  * 
  * A ring is created bound to a molecule, and cannot be re-bound.
  */
 public class Ring
 {
     // Unique ID of this ring in its molecule.
     private final int              _id;
     // Containing molecule of this ring.
     private final Molecule         _mol;
 
     // The atoms in this ring. Atoms occur in order.
     private final LinkedList<Atom> _atoms;
     // The bonds forming this ring. Bonds occur in order.
     private final LinkedList<Bond> _bonds;
     // Other rings that share at least one bond with this ring.
     private final LinkedList<Ring> _nbrs;
 
     // Is this ring aromatic in its current configuration?
     private boolean                _isAro;
 
     // Is this ring completed and finalised?
     private boolean                _completed;
 
     /**
      * @param mol
      *            The containing molecule of this ring.
      * @param id
      *            The unique ID of this ring in its molecule.
      */
     Ring(int id, Molecule mol) {
         _id = id;
         _mol = mol;
 
         _atoms = Lists.newLinkedList();
         _bonds = Lists.newLinkedList();
         _nbrs = Lists.newLinkedList();
     }
 
     /**
      * @return The containing molecule of this ring.
      */
     public Molecule molecule() {
         return _mol;
     }
 
     /**
      * @return The unique ID of this ring.
      */
     public int id() {
         return _id;
     }
 
     /**
      * @return The size of this ring. This is equivalently the number of atoms
      *         or the number of bonds participating in this ring.
      */
     public int size() {
         return _atoms.size();
     }
 
     /**
      * @return True if this ring is aromatic; false otherwise.
      */
     public boolean isAromatic() {
         // TODO(js): Implement aromaticity determination.
 
         return _isAro;
     }
 
     /**
      * @param id
      *            Unique canonical ID of the requested atom.
      * @return The requested atom if it exists; {@code null} otherwise.
      */
     public Atom atom(int id) {
         for (Atom a : _atoms) {
             if (a.id() == id) {
                 return a;
             }
         }
 
         return null;
     }
 
     /**
      * Adds the given atom to this ring.
      * 
      * The given atom is ignored if it is already a member of this ring. It
      * checks to see that a bond exists between the most-recently-added atom and
      * the current atom. An {@link IllegalStateException} is thrown otherwise.
      * 
      * @param a
      *            The atom to add to this ring.
      * @throws IllegalStateException
      *             if the given atom does not logically continue from the
      *             most-recently added atom.
      * @throws ImmutabilityException
      *             if an attempt is made at adding atoms to a <i>completed</i>
      *             ring.
      */
     public void addAtom(Atom a) throws IllegalStateException,
             ImmutabilityException {
         if (_completed) {
             throw new ImmutabilityException(String.format(
                     "Ring is already completed. %s", toString()));
         }
 
         if (_atoms.contains(a)) {
             return;
         }
 
         if (!_atoms.isEmpty()) {
             Atom prev = _atoms.getLast();
             Bond b = _mol.bondBetween(prev, a);
             if (null == b) {
                 throw new IllegalStateException(
                         String.format(
                                 "There is no bond between previous atom %d and current atom %d",
                                 prev.id(), a.id()));
             }
 
             _bonds.add(b);
         }
         _atoms.add(a);
     }
 
     /**
      * Completes the link between the last atom and the first. Completion also
      * effectively freezes the ring.
      * 
      * @throws IllegalStateException
      *             if the size of the ring is less than 3, or if there is no
      *             bond connecting the first atom and the last.
      */
     public void complete() throws IllegalStateException {
         if (_completed) {
             return;
         }
 
         int len = _atoms.size();
         if (len < 3) {
             throw new IllegalStateException(
                     String.format(
                             "The smallest possible size for a ring is 3. Current ring size is: %d",
                             len));
         }
 
         Atom a1 = _atoms.getFirst();
         Atom a2 = _atoms.getLast();
         Bond b = _mol._bondBetween(a1, a2);
         if (null == b) {
             throw new IllegalStateException(String.format(
                     "No bond between the first and the last atoms: %d, %d",
                     a1.id(), a2.id()));
         }
 
         _bonds.add(b);
         canonicalise();
         _completed = true;
     }
 
     // Transforms the ring into a standard representation, where the ring
     // (logically) `begins' with that atom which has the lowest unique ID.
     void canonicalise() {
         int min = Integer.MAX_VALUE;
 
         // Find the index at which the atom with the lowest ID occurs.
         int idx = 0;
         for (int i = 0; i < _atoms.size(); i++) {
             int id = _atoms.get(i).id();
             if (id < min) {
                 min = id;
                 idx = i;
             }
         }
 
         // Rotate the list so that the atom with the minimum ID comes first.
         for (int i = 0; i < idx; i++) {
             _atoms.add(_atoms.removeFirst());
             _bonds.add(_bonds.removeFirst());
         }
     }
 
     /**
      * @return True if this ring is complete; false otherwise.
      */
     public boolean isCompleted() {
         return _completed;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         return String
                 .format("Ring %d: [%s]", _id, Joiner.on(", ").join(_atoms));
     }
 
     /**
      * @param id
      *            The unique ID of the bond to locate.
      * @return The bond with the given ID, if it occurs in this ring;
      *         {@code null} otherwise.
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
      * A ring cannot be compared until it is `completed'. Two `completed' rings
      * are equal iff they have the same participating atoms, in the same order.
      * 
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object obj) {
         if (!_completed) {
             return false;
         }
 
         if (this == obj) {
             return true;
         }
         if (!(obj instanceof Ring)) {
             return false;
         }
 
         Ring other = (Ring) obj;
         if (!other.isCompleted()) {
             return false;
         }
         if (_mol.id() != other.molecule().id()) {
             return false;
         }
         if (_atoms.size() != other.size()) {
             return false;
         }
 
         LinkedList<Atom> l = other._atoms;
         for (int i = 0; i < _atoms.size(); i++) {
             if (_atoms.get(i).id() != l.get(i).id()) {
                 return false;
             }
         }
 
         return true;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode() {
         int res = 0;
         for (Atom a : _atoms) {
            res += a.id();
            res *= 13;
         }
 
         return res;
     }
 
     /**
      * @return A read-only view of this ring's atoms.
      */
     public List<Atom> atoms() {
         return ImmutableList.copyOf(_atoms);
     }
 
     /**
      * @return A read-only view of this ring's bonds.
      */
     public List<Bond> bonds() {
         return ImmutableList.copyOf(_bonds);
     }
 
     /**
      * @return A read-only view of this ring's neighbouring rings.
      */
     public List<Ring> neighbours() {
         return ImmutableList.copyOf(_nbrs);
     }
 
     /**
      * @return Number of neighbouring rings, irrespective of configuration
      *         (spiro, fused, etc.).
      */
     public int numberOfNeighbours() {
         return _nbrs.size();
     }
 }
