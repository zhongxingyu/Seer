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
 
         if (preceeding == null) {
             first = one;
         } else {
             preceeding.next = one;
             one.prev = preceeding;
         }
 
         one.chunk = before;
         one.next = two;
 
         two.prev = one;
         two.chunk = after;
 
         following = from.next;
 
         if (following != null) {
             two.next = following;
             following.prev = two;
         }
 
         return one;
     }
 
     /**
      * Find the Piece containing offset, and split it into two. Handle the
      * boundary cases of an offset at a Piece boundary. Returns first of the
      * two Pieces; or null if the offset co
      */
     /*
      * TODO Initial implementation of this is an ugly linear search; replace
      * this with an offset cache in the Pieces.
      */
     Piece splitAt(int offset) {
         Piece piece, last;
         int start, following;
 
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
                 return last;
             }
 
             /*
              * Failing that, then let's see if this Piece contains the offset
              * point. If it does, figure out the delta into this Piece's Chunk
              * and split at that point.
              */
 
             following = start + piece.chunk.width;
 
             if (following > offset) {
                 return splitAt(piece, offset - start);
             }
             start = following;
 
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
      * 
      * This will operate on the Text, doing a splits at each end. That isn't
      * strictly necessary, except that the reason you're usually calling this
      * is to delete, so the boundaries are a good first step, and it makes the
      * algorithm here far simpler.
      * 
      * Returns a Piece wrapping the extracted Chunk (and linked in to the
      * Text).
      */
     Piece concatonateFrom(int offset, int width) {
         final Piece preceeding, two, following, splice;
         Piece p;
         Chunk c;
         int i;
         char[] data;
         final Chunk extract;
 
         if (offset < 0) {
             throw new IllegalArgumentException();
         }
         if (width < 0) {
             throw new IllegalArgumentException();
         }
 
         /*
          * TODO guard the other end, ie test for conditions
          * IndexOutOfBoundsException("offset too high") and
          * IndexOutOfBoundsException("width greater than available text")
          */
 
         /*
          * Find the Piece containing the start point of the range we want to
          * isolate.
          */
 
         preceeding = splitAt(offset);
 
         two = splitAt(offset + width);
         following = two.next;
 
         /*
          * Copy characters from the Pieces in the middle into a char[], then
          * form them into a Chunk.
          */
 
         data = new char[width];
 
         if (preceeding != null) {
             p = preceeding.next;
         } else {
             p = two;
         }
         i = 0;
 
         while (p != following) {
             c = p.chunk;
 
             System.arraycopy(c.text, c.start, data, i, c.width);
 
             i += c.width;
             p = p.next;
         }
 
         extract = new Chunk(data);
 
         /*
          * Now embed this extract into a Piece and splice that into the Text.
          * This is the "wasteful" part if we're deleting, except that it gives
          * us the handle we need to locate the boundaries. Think of it as a
          * tuple :)
          */
 
         splice = new Piece();
 
         if (preceeding == null) {
             first = splice;
         } else {
             preceeding.next = splice;
             splice.prev = preceeding;
         }
 
        splice.chunk = extract;
        splice.next = following;
        following.prev = splice;
 
         return splice;
     }
 
     /**
      * Delete a width wide segment starting at offset. All the hard work is
      * done by the concatonate method, this just removes the splice.
      */
     /*
      * TODO do something with extracted Chunk to facilitate undo
      */
     public void delete(int offset, int width) {
         final Piece splice, preceeding, following;
         // final Chunk extract;
 
         splice = concatonateFrom(offset, width);
         // extract = splice.chunk;
 
         preceeding = splice.prev;
         following = splice.next;
 
         if (offset == 0) {
             first = following;
             return;
         }
 
         preceeding.next = following;
         following.prev = preceeding;
     }
 }
