 /*
  * Copyright (c) 2013, Patrick Moawad
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.github.multitouchframework.base;
 
 import com.github.multitouchframework.api.Chainable;
 
 /**
  * Helper class to build chains of blocks more easily.<br>Starting a new chain or branch of a chain is done using the
  * {@link #queue(Chainable)} method.
  *
  * @see Chainable
  */
 public final class ChainBuilder {
 
     /**
      * Entity allowing to queue one or several blocks to a block.
      *
      * @param <T> Type of next block.
      */
     public static class Chain<T> {
 
         /**
          * Current block to which additional blocks can be queued.
          */
         private final Chainable<T> block;
 
         /**
          * Constructor specifying the current block to which additional blocks can be queued.
          *
          * @param block Current block to which additional blocks can be queued.
          */
         public Chain(final Chainable<T> block) {
             this.block = block;
         }
 
         /**
          * @param nextBlock Next chainable block to be added to the current block.
          * @param <N>       Type of block following the specified next chainable block.
          *
          * @return Entity allowing to queue more blocks.
          */
         @SuppressWarnings("unchecked")
        public <N> Chain queue(final Chainable<N> nextBlock) {
             block.queue((T) nextBlock);
             return new Chain<N>(nextBlock);
         }
 
         /**
          * Ends the chain or branch with the specified block(s).
          *
          * @param nextBlocks Next block(s) to be added to the current block.
          */
         @SuppressWarnings("unchecked")
         public void queue(final Object... nextBlocks) {
             for (final Object next : nextBlocks) {
                 block.queue((T) next);
             }
         }
     }
 
     /**
      * Private constructor for utility class.
      */
     private ChainBuilder() {
         // Nothing to be done
     }
 
     /**
      * @param block First block to start the chain with.<br>It can be the input source at the beginning of the whole
      *              chain or a block at the beginning of a new branch.
      * @param <N>   Type of the next block after the specified block.
      *
      * @return Entity allowing to queue more blocks.
      */
    public static <N> Chain queue(final Chainable<N> block) {
         return new Chain<N>(block);
     }
 }
