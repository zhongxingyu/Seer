 /*
  * Sonar, open source software quality management tool.
  * Written (W) 2011 Andrew Tereskin
  * Copyright (C) 2008-2011 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.duplications.block;
 
 import org.sonar.duplications.statement.Statement;
 
import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Creates blocks from statements.
  * Each block will contain specified number of statements - <code>blockSize</code>.
  * Hash value computed for each block using Rabin-Karp rolling hash :
  * <blockquote><pre>
  * s[0]*31^(blockSize-1) + s[1]*31^(blockSize-2) + ... + s[blockSize-1]
  * </pre></blockquote>
  * using <code>long</code> arithmetic, where <code>s[i]</code>
  * is the hash code of <code>String</code> for statement with number i.
  * Thus running time - O(N), where N - number of statements.
  */
 public class BlockChunker {
 
   private static final long PRIME_BASE = 31;
   private long power;
 
   private int blockSize;
 
   public BlockChunker(int blockSize) {
     this.blockSize = blockSize;
 
     power = 1;
     for (int i = 0; i < blockSize; i++) {
       power = power * PRIME_BASE;
     }
   }
 
  /**
   * TODO Godin: The <tt>List</tt> interface doesn't guarantee running time O(1) on access to elements by index (e.g. <tt>LinkedList</tt>).
   * So in fact we expect <tt>ArrayList</tt> here in order to have linear running time.
   */
   public List<Block> chunk(String resourceId, List<Statement> statements) {
     if (statements.size() < blockSize) {
       return Collections.emptyList();
     }
    List<Block> blockList = new ArrayList<Block>();
     long hash = 0;
    for (int i = 0; i < statements.size(); i++) {
       // add current statement to hash
      Statement current = statements.get(i);
       hash = hash * PRIME_BASE + current.getValue().hashCode();
       // remove first statement from hash, if needed
       int j = i - blockSize + 1;
       if (j > 0) {
        hash -= power * statements.get(j - 1).getValue().hashCode();
       }
       // create block
       if (j >= 0) {
        Statement first = statements.get(j);
         blockList.add(new Block(resourceId, new ByteArray(hash), j, first.getStartLine(), current.getEndLine()));
       }
     }
     return blockList;
   }
 
 }
