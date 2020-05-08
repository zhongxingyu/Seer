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
 package org.novelang.outfit.xml;
 
 import com.google.common.collect.ImmutableList;
 import javax.xml.transform.ErrorListener;
 import javax.xml.transform.TransformerException;
 import org.novelang.logger.Logger;
 import org.novelang.logger.LoggerFactory;
 import org.novelang.outfit.CompositeException;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * Log all errors as they come and keep them for rethrowing.
  *
  * @author Laurent Caillette
  */
 public class TransformerErrorListener implements ErrorListener {
 
   private final Logger logger ;
   private final ImmutableList.Builder< Exception > exceptions = ImmutableList.builder() ;
 
   public TransformerErrorListener() {
     this( LoggerFactory.getLogger( TransformerErrorListener.class ) ) ;
   }
 
   public TransformerErrorListener( final Logger logger ) {
     this.logger = checkNotNull( logger ) ;
   }
 
   @Override
   public void warning( final TransformerException e ) throws TransformerException {
     logger.warn( e.getMessageAndLocation() ) ;
     exceptions.add( e ) ;
   }
 
   @Override
   public void error( final TransformerException e ) throws TransformerException {
     logger.error( e.getMessageAndLocation() ) ;
     exceptions.add( e ) ;
   }
 
   @Override
   public void fatalError( final TransformerException e ) throws TransformerException {
     logger.error( e.getMessageAndLocation(), "Fatal" ) ;
     exceptions.add( e ) ;
   }
 
   public void flush() throws TransformerMultiException {
     final ImmutableList< Exception > list = exceptions.build() ;
     if( ! list.isEmpty() ) {
      throw new TransformerMultiException( "Problem(s) hit when procssing stylesheet", list ) ;
     }
   }
 }
