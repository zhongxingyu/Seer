 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package novelang.rendering;
 
 import java.util.Set;
 
 import com.google.common.collect.Sets;
 import novelang.common.NodeKind;
 import static novelang.common.NodeKind.*;
 
 /**
  * Determines when a whitespace should be added between two Nodes of known type.
  * 
  * @author Laurent Caillette
  */
 public class WhitespaceTrigger {
 
   private static final Set< Sequence > SEQUENCES = Sets.newHashSet() ;
   static {
     add( EMPHASIS, WORD ) ;
     add( EMPHASIS, QUOTE ) ;
     add( HARD_INLINE_LITTERAL, WORD ) ;
     add( INTERPOLATEDCLAUSE, WORD ) ;
     add( INTERPOLATEDCLAUSE_SILENTEND, WORD ) ;
     add( PARENTHESIS, WORD ) ;
     add( PUNCTUATION_SIGN, SQUARE_BRACKETS ) ;
     add( PUNCTUATION_SIGN, EMPHASIS ) ;
     add( PUNCTUATION_SIGN, INTERPOLATEDCLAUSE ) ;
     add( PUNCTUATION_SIGN, INTERPOLATEDCLAUSE_SILENTEND ) ;
     add( PUNCTUATION_SIGN, PARENTHESIS ) ;
     add( PUNCTUATION_SIGN, QUOTE ) ;
    add( PUNCTUATION_SIGN, SOFT_INLINE_LITTERAL ) ;
    add( PUNCTUATION_SIGN, HARD_INLINE_LITTERAL ) ;
     add( PUNCTUATION_SIGN, WORD ) ;
     add( QUOTE, WORD ) ;
     add( SQUARE_BRACKETS, WORD ) ;
     add( SOFT_INLINE_LITTERAL, WORD ) ;
     add( WORD, EMPHASIS ) ;
     add( WORD, HARD_INLINE_LITTERAL ) ;
     add( WORD, INTERPOLATEDCLAUSE ) ;
     add( WORD, INTERPOLATEDCLAUSE_SILENTEND ) ;
     add( WORD, PARENTHESIS ) ;
     add( WORD, SOFT_INLINE_LITTERAL ) ;
    add( WORD, HARD_INLINE_LITTERAL ) ;
     add( WORD, QUOTE ) ;
     add( WORD, SQUARE_BRACKETS ) ;
     add( WORD, WORD ) ;
   }
 
   private static void add( NodeKind nodeKind1, NodeKind nodeKind2 ) {
     SEQUENCES.add( new Sequence( nodeKind1, nodeKind2 ) ) ;
   }
 
   public static boolean isTrigger( NodeKind first, NodeKind second ) {
 
     if( null == first ) {
       return false ;
     }
 
     final Sequence sequence = new Sequence( first, second ) ;
     return SEQUENCES.contains( sequence ) ;
   }
 
   private static final class Sequence {
     private final NodeKind first, second ;
 
 
     public Sequence( NodeKind first, NodeKind second ) {
       this.first = first;
       this.second = second;
     }
 
     public NodeKind getFirst() {
       return first;
     }
 
     public NodeKind getSecond() {
       return second;
     }
 
 
     public boolean equals( Object other ) {
       if( this == other ) {
         return true;
       }
 
       if( null == other ) {
         return false ;
       }
 
       final Sequence sequence = ( Sequence ) other ;
       return
           first == sequence.first &&
           second == sequence.second
       ;
     }
 
     public int hashCode() {
       return
           ( null == first ? 0 : first.hashCode() * 31 ) +
           ( null == second ? 0 : second.hashCode() )
       ;
     }
   }
 
 }
