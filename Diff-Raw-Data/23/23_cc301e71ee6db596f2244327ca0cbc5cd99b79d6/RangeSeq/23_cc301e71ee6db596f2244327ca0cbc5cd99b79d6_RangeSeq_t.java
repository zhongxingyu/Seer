 /*
  * Vitry, copyright (C) Hans Hoglund 2011
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * See COPYING.txt for details.
  */
 package vitry.runtime.struct;
 
 import java.math.BigInteger;
 
 
 public class RangeSeq extends AbstractSeq<BigInteger>
 {
     private final BigInteger min;
     private final BigInteger last;
 
    private RangeSeq(BigInteger min, BigInteger last) {
         this.min = min;
        this.last = last;
    }
    
    public static RangeSeq create(BigInteger min, BigInteger max)
    {
        return new RangeSeq(min, max.subtract(BigInteger.ONE));
     }
 
     public BigInteger head()
     {
         return min;
     }
 
     public Seq<BigInteger> tail()
     {
         if (!hasTail())
             return null;
         else
            return new RangeSeq(min.add(BigInteger.ONE), last);
     }
 
     public boolean hasTail()
     {
         return last.compareTo(min) > 0;
     }
 }
