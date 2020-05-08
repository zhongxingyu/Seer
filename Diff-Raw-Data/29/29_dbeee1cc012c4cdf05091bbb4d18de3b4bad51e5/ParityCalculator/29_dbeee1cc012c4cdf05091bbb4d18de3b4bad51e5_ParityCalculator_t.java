 /*
  * Copyright (c) 2012. John May <jwmay@sf.net>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.ebi.mdk.prototype.hash.util;
 
 import Jama.Matrix;
 import org.openscience.cdk.interfaces.IAtom;
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.interfaces.IBond;
 
 import javax.vecmath.Point2d;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * @author John May
  */
 public class ParityCalculator {
 
     public static int getSP3Parity(IAtom atom, final IAtomContainer container) {
         List<IAtom> neighbours = container.getConnectedAtomsList(atom);
         Collections.sort(neighbours, new Comparator<IAtom>() {
             @Override
             public int compare(IAtom o1, IAtom o2) {
                 return container.getAtomNumber(o1) - container.getAtomNumber(o2);
             }
         });
         return getSP3Parity(atom, neighbours, container);
     }
 
     public static int getSP2Parity(IAtom atom, final IAtomContainer container) {
 
         List<IAtom> neighbours = container.getConnectedAtomsList(atom);
 
         Collections.sort(neighbours, new Comparator<IAtom>() {
             @Override
             public int compare(IAtom o1, IAtom o2) {
                 return container.getAtomNumber(o1) - container.getAtomNumber(o2);
             }
         });
 
         return getSP2Parity(atom, neighbours, container);
     }
 
     public static int getMDLTetrahedralParity(IAtom atom, IAtomContainer container) {
         return getMDLTetrahedralParity(atom, container.getConnectedAtomsList(atom), container);
     }
 
     public static int getSP3Parity(IAtom atom, List<IAtom> neighbours, final IAtomContainer container) {
 
         if (neighbours.size() == 3)
             neighbours.add(atom);
 
         if (neighbours.size() != 4)
             return 0;
 
         double d = Math.signum(getSP3ParityMatrix(atom, neighbours, container).det());
 
         return d > 0 ? +1
                 : d < 0 ? -1
                 : 0;
 
     }
 
     public static int getSP2Parity(IAtom atom, List<IAtom> neighbours, final IAtomContainer container) {
 

        // if we only have two neighbours we insert the atom
        // which connects them in the middle. this maintains the
        // correct parity sign
        //
        //   1                         1
        //    \        is the same as   \
        //     2 = 3     where x is      x = 3
        //                  unused      /
        //                            2
        //
        // we ensure the 3 atom is always the 'other' atom connected to
        // the double bond in the next step and therefore this atom
        // will always be in the middle
         if (neighbours.size() == 2)
             neighbours.add(atom);
 
         // move the double bond to the end
         for (int i = 0; i < neighbours.size(); i++) {
             if (IBond.Order.DOUBLE.equals(container.getBond(atom, neighbours.get(i)).getOrder())) {
                 neighbours.add(neighbours.remove(i));
                 break;
             }
         }
 
 
         if (neighbours.size() != 3)
             return 0;
 
         double d = Math.signum(getSP2ParityMatrix(atom, neighbours, container).det());
 
         return d > 0 ? +1
                 : d < 0 ? -1
                 : 0;
 
     }
 
     public static int getMDLTetrahedralParity(IAtom atom, List<IAtom> neighbours, final IAtomContainer container) {
 
         Collections.sort(neighbours, new Comparator<IAtom>() {
             @Override
             public int compare(IAtom o1, IAtom o2) {
                 return container.getAtomNumber(o1) - container.getAtomNumber(o2);
             }
         });
 
         int n = neighbours.size();
 
         // shifts any hydrogens to be the last neighbour - if we have a hydrogen we
         // must have 4 neighbours (otherwise we would have an equal)
         if (n == 4) {
 
             for (int i = 0; i < n - 1; i++) {
 
                 IAtom neighbour = neighbours.get(i);
 
                 if ("H".equals(neighbour.getSymbol()))
                     neighbours.add(neighbours.remove(i));
 
             }
         }
 
         return getSP3Parity(atom, neighbours, container);
 
     }
 
     private static Matrix getSP2ParityMatrix(IAtom chiral, List<IAtom> atoms, IAtomContainer container) {
 
         int n = atoms.size();
 
         Matrix m = new Matrix(3, 3);
 
         // using 2D coordinates for now
         for (int i = 0; i < n; i++) {
 
             IAtom atom = atoms.get(i);
             Point2d point = atom.getPoint2d();
 
             // set the coordinates
             m.set(i, 0, point.x);
             m.set(i, 1, point.y);
             m.set(i, 2, 1);
 
         }
 
         return m;
 
     }
 
     private static Matrix getSP3ParityMatrix(IAtom chiral, List<IAtom> atoms, IAtomContainer container) {
 
         int n = atoms.size();
 
         Matrix m = new Matrix(4, 4);
 
         // using 2D coordinates for now
         for (int i = 0; i < n; i++) {
 
             IAtom atom = atoms.get(i);
             Point2d point = atom.getPoint2d();
 
             // set the coordinates
             m.set(i, 0, point.x);
             m.set(i, 1, point.y);
             m.set(i, 2, 1);
 
             // set the sign of the stereo bond
             if (atom != chiral)
                 m.set(i, 3, getValue(container.getBond(chiral, atom)));
 
         }
 
         return m;
 
     }
 
     private static int getValue(IBond bond) {
 
         IBond.Stereo stereo = bond.getStereo();
 
         switch (stereo) {
             case UP:
                 return +1;
             case DOWN:
                 return -1;
             case UP_INVERTED:
                 return -1;
             case DOWN_INVERTED:
                 return +1;
         }
 
         return 0;
 
     }
 
 
 }
