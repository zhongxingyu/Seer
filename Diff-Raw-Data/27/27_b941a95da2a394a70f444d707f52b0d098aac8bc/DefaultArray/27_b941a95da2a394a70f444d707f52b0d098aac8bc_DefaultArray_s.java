 /*
  * Geotools - OpenSource mapping toolkit
  * (C) 2001, Institut de Recherche pour le Dveloppement
  * (C) 1999, Pches et Ocans Canada
  *
  *    This library is free software; you can redistribute it and/or
  *    modify it under the terms of the GNU Lesser General Public
  *    License as published by the Free Software Foundation; either
  *    version 2.1 of the License, or (at your option) any later version.
  *
  *    This library is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *    Lesser General Public License for more details.
  *
  *    You should have received a copy of the GNU Lesser General Public
  *    License along with this library; if not, write to the Free Software
  *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *
  * Contacts:
  *     UNITED KINGDOM: James Macgill
  *             mailto:j.macgill@geog.leeds.ac.uk
  *
  *     FRANCE: Surveillance de l'Environnement Assiste par Satellite
  *             Institut de Recherche pour le Dveloppement / US-Espace
  *             mailto:seasnet@teledetection.fr
  *
  *     CANADA: Observatoire du Saint-Laurent
  *             Institut Maurice-Lamontagne
  *             mailto:osl@osl.gc.ca
  */
 package org.geotools.renderer.array;
 
 // Divers
 import java.awt.geom.Point2D;
 import org.geotools.resources.XArray;
 import org.geotools.resources.renderer.Resources;
 import org.geotools.resources.renderer.ResourceKeys;
 
 
 /**
  * Implmentation par dfaut de {@link PointArray}. Cette classe enveloppe
  * un tableau de <code>float[]</code> sans utiliser quelle que compression
  * que ce soit. L'implmentation par dfaut est imutable. Toutefois, certaines
  * classes drives (notamment {@link DynamicArray}) ne le seront pas forcment.
  *
 * @version $Id: DefaultArray.java,v 1.5 2003/02/11 16:01:43 desruisseaux Exp $
  * @author Martin Desruisseaux
  */
 class DefaultArray extends PointArray {
     /**
      * Numro de srie (pour compatibilit avec des versions antrieures).
      */
     private static final long serialVersionUID = 3160219929318094867L;
 
     /**
      * Tableaux des coordonnes  envelopper. Ces coordonnes
      * sont normalement mmorises sous forme de paires (x,y).
      */
     protected float[] array;
 
     /**
      * Enveloppe le tableau <code>float[]</code> spcifi. Ce tableau
      * ne sera pas copi. Il devra obligatoirement avoir une longueur
      * paire.
      */
     public DefaultArray(final float[] array) {
         this.array=array;
         if ((array.length & 1) != 0) {
             throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_ODD_ARRAY_LENGTH_$1,
                                                new Integer(array.length)));
         }
     }
 
     /**
      * Retourne l'index de la premire coordonne valide.
      */
     protected int lower() {
         return 0;
     }
 
     /**
      * Retourne l'index suivant celui de la dernire coordonne valide.
      */
     protected int upper() {
         return array.length;
     }
 
     /**
      * Retourne le nombre de points dans ce tableau.
      */
     public final int count() {
         return (upper()-lower())/2;
     }
 
     /**
      * Returns an estimation of memory usage in bytes. This method count 8 bytes for each
      * (x,y) points plus 4 bytes for the internal fields (the {@link #array} reference).
      */
     public long getMemoryUsage() {
         return count()*8 + 4;
     }
 
     /**
      * Mmorise dans l'objet spcifi les coordonnes du premier point.
      *
      * @param  point Point dans lequel mmoriser la coordonne.
      * @return L'argument <code>point</code>, ou un nouveau point
      *         si <code>point</code> tait nul.
      */
     public final Point2D getFirstPoint(final Point2D point) {
         final int lower=lower();
         assert lower <= upper();
         final float x = array[lower+0];
         final float y = array[lower+1];
         if (point != null) {
             point.setLocation(x,y);
             return point;
         } else {
             return new Point2D.Float(x,y);
         }
     }
 
     /**
      * Mmorise dans l'objet spcifi les coordonnes du dernier point.
      *
      * @param  point Point dans lequel mmoriser la coordonne.
      * @return L'argument <code>point</code>, ou un nouveau point
      *         si <code>point</code> tait nul.
      */
     public final Point2D getLastPoint(final Point2D point) {
         final int upper=upper();
         assert(upper >= lower());
         final float x = array[upper-2];
         final float y = array[upper-1];
         if (point != null) {
             point.setLocation(x,y);
             return point;
         } else {
             return new Point2D.Float(x,y);
         }
     }
 
     /**
      * Retourne un itrateur qui balaiera les points partir de l'index spcifi.
      */
     public final PointIterator iterator(final int index) {
         return new DefaultIterator(array, (2*index)+lower(), upper());
     }
 
     /**
      * Retourne un tableau enveloppant les mmes points que le tableau courant,
      * mais des index <code>lower</code> inclusivement jusqu' <code>upper</code>
      * exclusivement. Si le sous-tableau ne contient aucun point (c'est--dire si
      * <code>lower==upper</code>), alors cette mthode retourne <code>null</code>.
      *
      * @param lower Index du premier point  prendre en compte.
      * @param upper Index suivant celui du dernier point  prendre en compte.
      */
     public PointArray subarray(int lower, int upper) {
         final int thisLower=lower();
         final int thisUpper=upper();
         lower = lower*2 + thisLower;
         upper = upper*2 + thisLower;
         if (lower            == upper              ) return null;
         if (lower==thisLower && upper==thisUpper   ) return this;
         if (lower==0         && upper==array.length) return new DefaultArray(array);
         return new SubArray(array, lower, upper);
     }
 
     /**
      * Insre les donnes de <code>this</code> dans le tableau spcifi. Cette mthode est
      * strictement rserve  l'implmentation de {@link #insertAt(int,PointArray,boolean)}.
      * La classe {@link DefaultArray} remplace l'implmentation par dfaut par une nouvelle
      * implmentation qui vite de copier les donnes avec {@link #toArray()}.
      */
     PointArray insertTo(final PointArray dest, final int index, final boolean reverse) {
         return dest.insertAt(index, array, lower(), upper(), reverse);
     }
 
     /**
      * Insre les donnes (<var>x</var>,<var>y</var>) du tableau <code>toMerge</code> spcifi.
      * Si le drapeau <code>reverse</code>  la valeur <code>true</code>, alors les points de
      * <code>toMerge</code> seront copies en ordre inverse.
      *
      * @param  index Index  partir d'o insrer les points dans ce tableau. Le point  cet
      *         index ainsi que tous ceux qui le suivent seront dcals vers des index plus levs.
      * @param  toMerge Tableau de coordonnes (<var>x</var>,<var>y</var>)  insrer dans ce
      *         tableau de points. Ses valeurs seront copies.
      * @param  lower Index de la premire coordonne de <code>toMerge</code>  copier dans ce tableau.
      * @param  upper Index suivant celui de la dernire coordonne de <code>toMerge</code>  copier.
      * @param  reverse <code>true</code> s'il faut inverser l'ordre des points de <code>toMerge</code>
      *         lors de la copie. Cette inversion ne change pas l'ordre (<var>x</var>,<var>y</var>) des
      *         coordonnes de chaque points.
      *
      * @return <code>this</code> si l'insertion  pu tre faite sur
      *         place, ou un autre tableau si a n'a pas t possible.
      */
     public PointArray insertAt(final int index, final float toMerge[],
                                final int lower, final int upper, final boolean reverse)
     {
         int count = upper-lower;
         if (count == 0) {
             return this;
         }
        return new DynamicArray(array, lower(), upper(),
                                count+Math.min(count, 256)).insertAt(index, toMerge, lower, upper, reverse);
     }
 
     /**
      * Renverse l'ordre de tous les points compris dans ce tableau.
      *
      * @return <code>this</code> si l'inversion a pu tre faite sur-place,
      *         ou un autre tableau si a n'a pas t possible.
      */
     public PointArray reverse() {
         return new DynamicArray(array, lower(), upper(), 16).reverse();
     }
 
     /**
      * Retourne un tableau immutable qui contient les mmes donnes que celui-ci.
      * Aprs l'appel de cette mthode, toute tentative de modification (avec les
      * mthodes {@link #insertAt} ou {@link #reverse}) vont retourner un autre
      * tableau de faon  ne pas modifier le tableau immutable.
      *
      * @param  compress <code>true</code> si l'on souhaite aussi comprimer les
      *         donnes. Cette compression peut se traduire par une plus grande
      *         lenteur lors des accs aux donnes.
      * @return Tableau immutable et ventuellement compress, <code>this</code>
      *         si ce tableau rpondait dj aux conditions ou <code>null</code>
      *         si ce tableau ne contient aucune donne.
      */
     public PointArray getFinal(final boolean compress) {
         if (compress && count() >= 8) {
             return new CompressedArray(array, lower(), upper());
         }
         return super.getFinal(compress);
     }
 
     /**
      * Append (<var>x</var>,<var>y</var>) coordinates to the specified destination array.
      * The destination array will be filled starting at index {@link ArrayData#length}.
      * If <code>resolution2</code> is greater than 0, then points that are closer than
      * <code>sqrt(resolution2)</code> from previous one will be skiped.
      *
      * @param  The destination array. The coordinates will be filled in
      *         {@link ArrayData#array}, which will be expanded if needed.
      *         After this method completed, {@link ArrayData#length} will
      *         contains the index after the <code>array</code>'s element
      *         filled with the last <var>y</var> ordinate.
      * @param  resolution2 The minimum squared distance desired between points.
      */
     public final void toArray(final ArrayData dest, final float resolution2) {
         if (!(resolution2 >= 0)) {
             throw new IllegalArgumentException(String.valueOf(resolution2));
         }
         final int offset = dest.length;
         float[]   copy   = dest.array;
         if (resolution2 == 0) {
             final int lower    = lower();
             final int length   = upper()-lower;
             final int capacity = offset + length;
             if (copy.length < capacity) {
                 dest.array = copy = XArray.resize(copy, capacity);
             }
             System.arraycopy(array, lower, copy, offset, length);
             dest.length = capacity;
         } else {
             int src = lower();
             int dst = offset;
             final int upper = upper();
             if (src < upper) {
                 if (copy.length <= dst) {
                     dest.array = copy = XArray.resize(copy, capacity(src, dst, offset));
                 }
                 float lastX = copy[dst++] = array[src++];
                 float lastY = copy[dst++] = array[src++];
                 while (src < upper) {
                     final float  x  = array[src++];
                     final float  y  = array[src++];
                     final double dx = (double)x - (double)lastX;
                     final double dy = (double)y - (double)lastY;
                     if ((dx*dx + dy*dy) >= resolution2) {
                         if (copy.length <= dst) {
                             dest.array = copy = XArray.resize(copy, capacity(src, dst, offset));
                         }
                         copy[dst++] = lastX = x;
                         copy[dst++] = lastY = y;
                     }
                 }
             }
             dest.length = dst;
         }
         assert dest.length <= dest.array.length;
     }
 }
