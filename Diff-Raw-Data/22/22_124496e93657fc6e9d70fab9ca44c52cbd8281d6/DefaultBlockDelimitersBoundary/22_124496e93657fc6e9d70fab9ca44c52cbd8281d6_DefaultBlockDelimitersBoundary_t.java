 /*
  * Copyright (C) 2009 Laurent Caillette
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
 package org.novelang.parser.antlr.delimited;
 
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import org.novelang.common.LocationFactory;
 import org.novelang.common.Problem;
 import org.novelang.logger.Logger;
 import org.novelang.logger.LoggerFactory;
 import org.antlr.runtime.MismatchedTokenException;
 import org.antlr.runtime.Token;
 
 /**
  * Receives notifications from {@link org.novelang.parser.antlr.GrammarDelegate} from what's going on with delimiters.
  *
  * @author Laurent Caillette
  */
 public class DefaultBlockDelimitersBoundary implements BlockDelimitersBoundary {
 
   private static final Logger LOGGER = LoggerFactory.getLogger( DefaultBlockDelimitersBoundary.class );
 
   private final LocationFactory locationFactory ;
   private final Map< BlockDelimiter, DelimitedBlockStatus > primes = Maps.newHashMap() ;
 
 
   public DefaultBlockDelimitersBoundary( final LocationFactory locationFactory ) {
     this.locationFactory = locationFactory ;
     for( final BlockDelimiter blockDelimiter : BlockDelimiter.values() ) {
       primes.put( blockDelimiter, new DelimitedBlockStatus( blockDelimiter ) ) ;
     }
   }
 
   public void startDelimitedText( final BlockDelimiter blockDelimiter, final Token startToken ) {
     primes.get( blockDelimiter )
         .updatePosition( startToken.getLine(), startToken.getCharPositionInLine() )
         .increaseStartCount()
     ;
 
   }
 
   public void reachEndDelimiter( final BlockDelimiter blockDelimiter ) {
     primes.get( blockDelimiter ).increaseReachEndCount() ;
   }
 
   public void endDelimitedText( final BlockDelimiter blockDelimiter ) {
     primes.get( blockDelimiter ).increaseEndPassedCount() ;
   }
 
   public void reportMissingDelimiter(
       final BlockDelimiter blockDelimiter,
       final MismatchedTokenException mismatchedTokenException
   )
       throws MismatchedTokenException
   {
     primes.get( blockDelimiter ).increaseMissingDelimiterCount() ;
   }
 
   public Iterable< DelimitedBlockStatus > getFaultyDelimitedBlocks() {
     final List< DelimitedBlockStatus > faultyDelimitedBlockStatuses = Lists.newArrayList() ;
    for( final BlockDelimiter blockDelimiter : BlockDelimiter.getPairedDelimiters() ) {
       final DelimitedBlockStatus status = primes.get( blockDelimiter ) ;
       if( ! status.isConsistent() ) {
         faultyDelimitedBlockStatuses.add( status ) ;
       }
     }
     if( faultyDelimitedBlockStatuses.isEmpty() ) {
      for( final BlockDelimiter blockDelimiter : BlockDelimiter.getTwinDelimiters() ) {
         final DelimitedBlockStatus status = primes.get( blockDelimiter ) ;
         if( ! status.isConsistent() ) {
           faultyDelimitedBlockStatuses.add( status ) ;
         }
       }
     }
     return faultyDelimitedBlockStatuses ;
   }
 
   public Iterable<Problem> getProblems() {
     return BlockDelimiterTools.createProblems( locationFactory, getFaultyDelimitedBlocks() ) ;
   }
 
   public void dumpStatus() {
     final StringBuffer buffer = new StringBuffer() ;
     for( final BlockDelimiter blockDelimiter : BlockDelimiter.values() ) {
       final DelimitedBlockStatus status = primes.get( blockDelimiter ) ;
       buffer.append( "\n" ) ;
       buffer.append( String.format( "%1$16s", blockDelimiter.name() ) ) ;
       buffer.append( " " ) ;
       buffer.append( status.getInternalStatusAsString() ) ;
     }
     LOGGER.debug( buffer.toString() ) ;
 
   }
 
 
 }
