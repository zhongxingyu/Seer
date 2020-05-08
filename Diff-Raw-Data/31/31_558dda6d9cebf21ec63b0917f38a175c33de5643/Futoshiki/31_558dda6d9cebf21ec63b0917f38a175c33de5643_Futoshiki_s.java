 /*
  *  A Futoshiki puzzle editor and solver.
  *  Copyright © 2007, 2011 Joseph Walton
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.kafsemo.futoshiki;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * A Futoshiki puzzle state, with any number of cells filled in and
  * a set of rules constraining the values. Methods allow for the manipulation
  * of state and validity checking.
  * 
  * @author Joseph Walton
  */
 public class Futoshiki
 {
     private final int length;
     private final byte[] data;
     
     private final Map<GtRule, ValidatingRule> rules
                     = new HashMap<GtRule, ValidatingRule>();
     private final Iterable<GtRule> origRuleIterable = new OrigRuleIterable();
 
     private ValidatingRule[] vraCache;
     
     public Futoshiki()
     {
         this(5);
     }
     
     public Futoshiki(int length)
     {
         if ((length < 1) || (length > 9)) {
             throw new IllegalArgumentException("Size must be 1 to 9");
         }
         
         this.length = length;
         this.data = new byte[length * length];
     }
     
     public int getLength()
     {
         return length;
     }
     
     private int idx(int column, int row)
     {
         if (column < 1 || column > length)
             throw new IllegalArgumentException("Bad column " + column);
 
         if (row < 1 || row > length)
             throw new IllegalArgumentException("Bad row " + row);
 
         return idxInternal(column, row);
     }
 
     private int idxInternal(int column, int row)
     {
         return (row - 1) * length + (column - 1);
     }
 
     /**
      * Is this puzzle state currently valid? Checks for duplicate numbers
      * in rows or columns and that all rules are followed.
      * 
      * @return
      */
     public boolean isValid()
     {
         /* Check for duplicates */
         
        int columnMask = 0;
         
         for (int row = 1; row <= length; row++) {
             int rowMask = 0;
 
             for (int column = 1; column <= length; column++) {
                 int v = data[idxInternal(column, row)];
                 if (v == 0)
                     continue;
                 
                 int bit = (1 << v);
                 if ((rowMask & bit) != 0)
                     return false;
                 
                if ((columnMask >> (column * length) & bit) != 0)
                     return false;
                 
                 rowMask |= bit;
                columnMask |= (bit << column * length);
             }
         }
         
         /* Obey rules */
         if (vraCache == null) {
             vraCache = rules.values().toArray(new ValidatingRule[0]);
         }
         
         for (ValidatingRule r : vraCache) {
             if (!r.isValid(this))
                 return false;
         }
         
         /* No violations, so valid */
         return true;
     }
 
     public boolean isFull()
     {
         for (byte b : data) {
             if (b == 0) {
                 return false;
             }
         }
         return true;
     }
 
     public void set(int column, int row, int v)
     {
         if (v < 1 || v > length)
             throw new IllegalArgumentException("Bad cell value " + v);
         
         data[idx(column, row)] = (byte) v;
     }
 
     public void clear(int column, int row)
     {
         data[idx(column, row)] = 0;
     }
     
     public void addGtRule(int columnA, int rowA, int columnB, int rowB)
     {
         GtRule newRule = new GtRule(columnA, rowA, columnB, rowB);
 
         GtRule k = newRule.getCanonPosForm();
         
         vraCache = null;
         rules.put(k, new ValidatingRule(newRule));
     }
     
     public Futoshiki clone()
     {
         Futoshiki f = new Futoshiki(length);
         System.arraycopy(data, 0, f.data, 0, data.length);
         f.rules.putAll(rules);
         return f;
     }
 
     public byte get(int column, int row)
     {
         return data[idx(column, row)];
     }
     
     public Collection<CellPos> blankCells()
     {
         Collection<CellPos> blank = new ArrayList<CellPos>(length * length);
         
         for (int row = 1; row <= length; row++) {
             for (int column = 1; column <= length; column++) {
                 if (data[idxInternal(column, row)] == 0) {
                     blank.add(new CellPos(column, row));
                 }
             }
         }
         
         return blank;
     }
 
     public Iterable<? extends GtRule> getRules()
     {
         return origRuleIterable;
     }
     
     public GtRule getRule(GtRule ruleKey)
     {
         GtRule k = ruleKey.getCanonPosForm();
 
         ValidatingRule r = rules.get(k);
         if (r != null) {
             return r.getOrigRule();
         } else {
             return null;
         }
     }
     
     public void removeRule(GtRule ruleKey)
     {
         GtRule k = ruleKey.getCanonPosForm();
         
         vraCache = null;
         rules.remove(k);
     }
     
     private class ValidatingRule
     {
         private final int idxA, idxB;
         private final GtRule origRule;
         
         ValidatingRule(GtRule gtr)
         {
             idxA = idx(gtr.getGreaterColumn(), gtr.getGreaterRow());
             idxB = idx(gtr.getLesserColumn(), gtr.getLesserRow());
             origRule = gtr;
         }
         
         boolean isValid(Futoshiki f)
         {
             /* Is either cell blank? */
             if (f.data[idxA] == 0 || f.data[idxB] == 0)
                 return true;
 
             return f.data[idxA] > f.data[idxB];
         }
         
         GtRule getOrigRule()
         {
             return origRule;
         }
     }
     
     private class OrigRuleIterable implements Iterable<GtRule>
     {
         public Iterator<GtRule> iterator()
         {
             final Iterator<ValidatingRule> i = rules.values().iterator();
             
             return new Iterator<GtRule>() {
                 public boolean hasNext()
                 {
                     return i.hasNext();
                 }
 
                 public GtRule next()
                 {
                     return i.next().getOrigRule();
                 }
                 
                 public void remove()
                 {
                     throw new UnsupportedOperationException();
                 }
             };
         }
     }
 }
