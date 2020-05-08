 /*
  * Text.java
  *
  * Copyright (c) 2008 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the program it is a part of, are made available
  * to you by its authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 package com.operationaldynamics.textbase;
 
 /**
  * A mutable buffer of unicode text to which changes are undoable.
  * 
  * @author Andrew Cowie
  */
 public class Text
 {
     Piece first;
 
     public Text(String str) {
         first = new Piece();
         first.chunk = new Chunk(str);
     }
 
     Text(Chunk initial) {
         first = new Piece();
         first.chunk = initial;
     }
 
     /**
      * The length of this Text, in characters.
      */
     /*
     * TODO cache this when we cache the offsets!
      */
     public int length() {
        Piece piece;
         int result;
 
        piece = first;
         result = 0;
 
        while (piece != null) {
            result += piece.chunk.width;
            piece = piece.next;
         }
 
         return result;
     }
 
     public String toString() {
         final StringBuilder str;
         Piece piece;
 
         str = new StringBuilder();
         piece = first;
 
         while (piece != null) {
             str.append(piece.chunk.text, piece.chunk.start, piece.chunk.width);
             piece = piece.next;
         }
 
         return str.toString();
     }
 
     void append(Chunk addition) {
         Piece piece;
         final Piece last;
 
         if (addition == null) {
             throw new IllegalArgumentException();
         }
 
         piece = first;
 
         while (piece.next != null) {
             piece = piece.next;
         }
 
         last = new Piece();
         last.prev = piece;
         piece.next = last;
         last.chunk = addition;
     }
 
     /**
      * Insert the given Java String at the specified offset.
      */
     public void insert(int offset, String what) {
         insert(offset, new Chunk(what));
     }
 
     /**
      * Cut a Piece in two at point. Amend the linkages so that overall Text is
      * the same after the operation.
      */
     Piece splitAt(Piece from, int point) {
         Piece preceeding, one, two, following;
         Chunk before, after;
 
         before = new Chunk(from.chunk, 0, point);
         after = new Chunk(from.chunk, point, from.chunk.width - point);
 
         one = new Piece();
         two = new Piece();
 
         preceeding = from.prev;
         if (preceeding != null) {
             preceeding.next = one;
         }
 
         one.prev = from.prev;
         one.chunk = before;
         one.next = two;
 
         two.prev = one;
         two.chunk = after;
         two.next = from.next;
 
         following = from.next;
         if (following != null) {
             following.prev = two;
         }
 
         return one;
     }
 
     /**
      * Find the Piece containing offset, and split it into two. Handle the
      * boundary cases of an offset at a Piece boundary. Returns first of the
      * two Pieces.
      */
     /*
      * TODO Initial implementation of this is an ugly linear search; replace
      * this with an offset cache in the Pieces.
      */
     Piece splitAt(int offset) {
         Piece piece, last;
         int start;
 
         if (offset == 0) {
             return null;
         }
 
         piece = first;
         last = first;
 
         start = 0;
 
         while (piece != null) {
             /*
              * Are we already at a Piece boundary?
              */
 
             if (start == offset) {
                 return piece;
             }
 
             /*
              * Failing that, then let's see if this Piece contains the offset
              * point. If it does, figure out the delta into this Piece's Chunk
              * and split at that point.
              */
 
             start += piece.chunk.width;
 
             if (start > offset) {
                 return splitAt(piece, start - offset - 1);
             }
 
             last = piece;
             piece = piece.next;
         }
 
         /*
          * Reached the end; so long as there is nothing left we're in an
          * append situation and no problem, otherwise out of bounds.
          */
 
         if (start == offset) {
             return last;
         }
 
         throw new IndexOutOfBoundsException();
     }
 
     /**
      * Splice a Chunk into the Text. The result of doing this is three Pieces;
      * a new Piece before and after, and a Piece wrapping the Chunk and linked
      * between them. This is the workhorse of this class.
      */
     public void insert(int offset, Chunk addition) {
         Piece one, two, piece;
 
         if (offset < 0) {
             throw new IllegalArgumentException();
         }
 
         piece = new Piece();
         piece.chunk = addition;
 
         if (offset == 0) {
             piece.next = first;
             first.prev = piece;
             first = piece;
             return;
         }
 
         one = splitAt(offset);
         two = one.next;
 
         if (two != null) {
             piece.next = two;
             two.prev = piece;
         }
         one.next = piece;
         piece.prev = one;
     }
 
     /**
      * Allocate a new Chunk by concatonating any and all Chunks starting at
      * offset for width. While deleting, as such, does not require this,
      * undo/redo does.
      */
     Chunk concatonateFrom(int offset, int width) {
         int start, next, index, todo, frag;
         int i, delta;
         char[] target;
 
         if (offset < 0) {
             throw new IllegalArgumentException();
         }
         if (width < 0) {
             throw new IllegalArgumentException();
         }
 
         /*
          * Find the Chunk containing the start point of the range we want to
          * isolate.
          */
 
         start = 0;
         next = 0;
         i = 0;
         delta = 0;
 
         for (i = 0; i < chunks.length; i++) {
             if (start == offset) {
                 delta = 0;
                 break;
             }
 
             next = chunks[i].width;
 
             if (start + next > offset) {
                 delta = offset - start;
                 break;
             }
 
             start += next;
         }
 
         /*
          * Copy characters from this and subsequent Chunks into a char[]
          */
 
         index = 0;
         todo = width;
         target = new char[width];
 
         while (todo > 0) {
             if (i == chunks.length) {
                 throw new IndexOutOfBoundsException();
             }
 
             // amount of to copy, which is all unless we're on first or last.
             if (chunks[i].width > todo) {
                 frag = todo;
             } else {
                 frag = chunks[i].width - delta;
             }
 
             System.arraycopy(chunks[i].text, chunks[i].start + delta, target, index, frag);
             index += frag;
             todo -= frag;
             delta = 0;
             i++;
         }
 
         if (todo == 0) {
             return new Chunk(target);
         }
 
         if (index == 0) {
             throw new IndexOutOfBoundsException("offset too high");
         } else {
             throw new IndexOutOfBoundsException("width greater than available text");
         }
     }
 
     /**
      * Delete a width wide segment starting at offset.
      */
     public void delete(int offset, int width) {
         final Chunk before, after;
         final Chunk[] next;
         int i, j, start, following, sum, deltaI, deltaJ;
 
         if (offset < 0) {
             throw new IllegalArgumentException();
         }
         if (width < 0) {
             throw new IllegalArgumentException();
         }
 
         if (chunks.length == 1) {
             before = new Chunk(chunks[0], 0, offset);
             after = new Chunk(chunks[0], offset + width, chunks[0].width - (offset + width));
 
             next = new Chunk[2];
             next[0] = before;
             next[1] = after;
 
             chunks = next;
             return;
         }
 
         /*
          * Identify the index of the Chunk containing offset
          */
 
         start = 0;
         following = 0;
         deltaI = 0;
 
         for (i = 0; i < chunks.length; i++) {
             if (start == offset) {
                 deltaI = 0;
                 break;
             }
 
             following = chunks[i].width;
 
             if (start + following > offset) {
                 deltaI = offset - start;
                 break;
             }
 
             start += following;
         }
 
         /*
          * Cross over the Chunks that will be dropped
          */
 
         sum = chunks[i].width - deltaI;
         deltaJ = 0;
 
         for (j = i + 1; j < chunks.length; j++) {
             sum += chunks[j].width;
 
             if (sum > width) {
                 deltaJ = sum - width;
                 break;
             }
         }
 
         before = new Chunk(chunks[i], 0, deltaI);
         after = new Chunk(chunks[j], deltaJ, chunks[j].width - deltaJ);
 
         next = new Chunk[i + (chunks.length - j) + 1];
 
         if (i != 0) {
             System.arraycopy(chunks, 0, next, 0, i);
         }
 
         /*
          * Now replace
          */
 
         next[i++] = before;
         next[i++] = after;
 
         /*
          * And now copy the remainder of the original array, if any.
          */
         j++;
         if (j != chunks.length) {
             System.arraycopy(chunks, j, next, i, chunks.length - j);
         }
 
         chunks = next;
     }
 }
