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
 package novelang.common;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.charset.Charset;
 import java.util.List;
 import java.util.Set;
 
 import org.antlr.runtime.RecognitionException;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ClassUtils;
 import org.apache.commons.lang.StringUtils;
 
 import novelang.designator.Tag;
 import novelang.system.LogFactory;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import novelang.common.metadata.MetadataHelper;
 import novelang.common.tree.TreeTools;
 import novelang.parser.GenericParser;
 import novelang.parser.unicode.UnicodeInputStream;
 import novelang.system.DefaultCharset;
 import novelang.system.Log;
 
 /**
  * Base class holding parsing and some error-handling.
  *
  * @author Laurent Caillette
  */
 public abstract class AbstractSourceReader implements LocationFactory, Renderable {
 
   protected static final Log LOG = LogFactory.getLog( AbstractSourceReader.class ) ;
   private final String thisToString ;
   private final List< Problem > problems ;
   protected final String locationName ;
   protected final Charset sourceCharset ;
   protected final Charset renderingCharset ;
 
   public AbstractSourceReader() {
     this.problems = Lists.newArrayList() ;
     this.thisToString = ClassUtils.getShortClassName( getClass() ) +
         "@" + System.identityHashCode( this ) ;
     this.locationName = "<String>" ;
     this.sourceCharset = DefaultCharset.SOURCE ;
     this.renderingCharset = DefaultCharset.RENDERING ;
   }
   
   public AbstractSourceReader( final AbstractSourceReader original ) {
     this.problems = Lists.newArrayList( original.problems ) ;
     this.thisToString = original.thisToString ;
     this.locationName = original.locationName ;
     this.sourceCharset = original.sourceCharset ;
     this.renderingCharset = original.renderingCharset ;
   }
 
   public AbstractSourceReader( 
       final Charset sourceCharset, 
       final Charset defaultRenderingCharset 
   ) {
     this.problems = Lists.newArrayList() ;
     this.thisToString = ClassUtils.getShortClassName( getClass() ) +
         "@" + System.identityHashCode( this ) ;
     this.locationName = "<String>" ;
     this.sourceCharset = Preconditions.checkNotNull( sourceCharset ) ;
     this.renderingCharset = Preconditions.checkNotNull( defaultRenderingCharset ) ;
     LOG.debug(
         "Creating %s[ sourceCharset=%s, renderingCharset=%s ]",
         thisToString,
         sourceCharset.name(),
         renderingCharset.name()
     ) ;
   }
 
   protected AbstractSourceReader(
       final String locationName,
       final Charset sourceCharset,
       final Charset renderingCharset,
       final String thisToString
   ) {
     this.problems = Lists.newArrayList() ;
     this.thisToString = thisToString + "@" + System.identityHashCode( this ) ;
     this.locationName = Preconditions.checkNotNull( locationName ) ;
     this.sourceCharset = Preconditions.checkNotNull( sourceCharset ) ;
     this.renderingCharset = Preconditions.checkNotNull( renderingCharset ) ;
     LOG.debug(
         "Creating %s[locationName='%s', sourceCharset=%s, renderingCharset=%s ]",
         thisToString,
         locationName,
         sourceCharset.name(),
         renderingCharset.name()
 
     ) ;
   }
 
   protected String readContent( final File file ) {
 
     LOG.info(
        "Attempting to load file '%s' from %s with charset %s",
         file.getAbsolutePath(),
        this,
         sourceCharset.name()
     ) ;
 
     final String stringContent ;
     try {
       final InputStream bareIinputStream = new FileInputStream( file ) ;
       final UnicodeInputStream inputStream = new UnicodeInputStream( bareIinputStream, sourceCharset ) ;
       inputStream.getEncoding() ;
       stringContent = IOUtils.toString( inputStream, sourceCharset.name() );
       inputStream.close() ;
     } catch( IOException e ) {
       LOG.warn( "Could not load file", e ) ;
       collect( Problem.createProblem( this, e ) ) ;
       return null ;
     }
     if( StringUtils.isBlank( stringContent ) ) {
       collect( Problem.createProblem( "Part is empty", createLocation() ) ) ;
       return null ;
     }    
     return stringContent ;
   }
 
 
   protected abstract GenericParser createParser( String content ) ;
 
   protected final SyntacticTree parse( final String content ) {
 
     if( null == content ) {
       return null ;
     }
 
     final GenericParser parser = createParser( content ) ;    
     try {
       // Yeah we do it here!
       final SyntacticTree syntacticTree = parser.parse() ;
       if( parser.hasProblem() ) {
         collect( parser.getProblems() ) ;
         return null ;
       }
       return syntacticTree ;
 
     } catch( RecognitionException e ) {
       LOG.warn( "Could not parse file", e ) ;
       collect( Problem.createProblem( this, e ) ) ;
       return null ;
     }
   }
 
   protected static SyntacticTree addMetadata( 
       final SyntacticTree tree, 
       final Set< Tag > tagset 
   ) {
     final SyntacticTree metadata = MetadataHelper.createMetadataDecoration( tree, tagset ) ;
     return TreeTools.addFirst( tree, metadata ) ;
   }
 
 
   public Location createLocation( final int line, final int column ) {
     return new Location( locationName, line, column ) ;
   }
 
   public Location createLocation() {
     return new Location( locationName ) ;
   }
 
   public Charset getRenderingCharset() {
     return renderingCharset;
   }
 
 
 // ========  
 // Problems
 // ========  
   
   public Iterable< Problem > getProblems() {
     return ImmutableList.copyOf( problems ) ;
   }
 
   public boolean hasProblem() {
     return ! problems.isEmpty() ;
   }
 
   protected final void collect( final Problem problem ) {
     LOG.debug( "Collecting Problem: %s", problem ) ;
     problems.add( Preconditions.checkNotNull( problem ) ) ;
   }
   
   private final ProblemCollector problemCollector = new ProblemCollector() {
     public void collect( final Problem problem ) {
       AbstractSourceReader.this.collect( problem ) ;
     }
   } ;
 
   protected final ProblemCollector getProblemCollector() {
     return problemCollector ;
   }
 
   protected final void collect( final Iterable< Problem > problems ) {
     for( final Problem problem : problems ) {
       collect( problem ) ;
     }
   }
 
 
 // =============  
 // Miscellaneous
 // =============  
   
   @Override
   public String toString() {
     return thisToString;
   }
 
 
 }
