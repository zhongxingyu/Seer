 /*
  * Copyright © 2012 Karel Rr without modification,
  * are permitted provided that the following conditions are met:
  *  Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  *  Redistributions in binary form must reproduce the above copyright notice, this
  *   list of conditions and the following disclaimer in the documentation and/or
  *   other materials provided with the distribution.
  *  Neither the name of Karel Rank nor the names of its contributors may be used to
  *   endorse or promote products derived from this software without specific prior
  *   written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package cz.rank.vsfs.mindex;
 
 import net.jcip.annotations.Immutable;
 import org.apache.commons.math3.util.FastMath;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Index for cluster
  *
  * @author Karel Rank
  */
 @Immutable
 public class Index {
     private final int maxIndex;
     private int calculatedIndex = Integer.MIN_VALUE;
     private final int maxLevel;
     private final int level;
     private final List<Integer> indexes;
     private final Set<Integer> indexes2LevelSet;
 
     public Index(int index, int maxLevel, int maxIndex) {
         this.indexes = new ArrayList<>(maxLevel);
         this.indexes.add(index);
         this.maxLevel = maxLevel;
         this.maxIndex = maxIndex;
         level = 1;
         indexes2LevelSet = new HashSet<>();
     }
 
     private Index(Collection<Integer> indexes, int index, int maxLevel, int level, int maxIndex) {
         this.indexes = new ArrayList<>(indexes);
         this.indexes.add(index);
         this.maxLevel = maxLevel;
         this.maxIndex = maxIndex;
         this.level = level;
        indexes2LevelSet = new HashSet<>(this.indexes.subList(0, level >= 2 ? level - 1 : 0));
     }
 
     public int getCalculatedIndex() {
         if (calculatedIndex == Integer.MIN_VALUE) {
             calculatedIndex = 0;
             for (int i = 0; i <= level - 1; ++i) {
                 calculatedIndex += indexes.get(i) * FastMath.pow(maxIndex, level - 1 - i);
             }
         }
 
         return calculatedIndex;
     }
 
     public Index addLevel(int index) {
         return new Index(this.indexes, index, maxLevel, level + 1, maxIndex);
     }
 
     @Override
     public String toString() {
         return indexes.toString();
     }
 
     public int getMaxLevel() {
         return maxLevel;
     }
 
     public int getLevel() {
         return level;
     }
 
     public Set<Integer> indexes2LevelAsSet() {
         return indexes2LevelSet;
     }
 
     public int prevLevelIndex() {
         return indexes.get(level - 1);
     }
 
     public int getMaxIndex() {
         return maxIndex;
     }
 }
