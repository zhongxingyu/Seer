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
 
 package novelang ;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.ClassUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.builder.StandardToStringStyle;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import static org.junit.Assert.assertTrue;
 import novelang.system.LogFactory;
 import novelang.system.Log;
 import com.google.common.base.Preconditions;
 
 /**
  * Creates directories on-demand for test purposes.
  * Each test is supposed to instantiate this class in the <code>setUp()</code> method because
  * test name (method name) is not available at the time Test constructor is called.
  *
  * @author Laurent Caillette
  */
 public class ScratchDirectory {
 
   private static final Log LOG = LogFactory.getLog( ScratchDirectory.class ) ;
 
   private final String testIdentifier ;
 
   private final Set< String > registeredTestIdentifiers = new HashSet< String >() ;
 
     public ScratchDirectory( Class testClass ) throws IOException {
     this( ClassUtils.getShortClassName( testClass ) ) ;
   }
 
   public ScratchDirectory( String testIdentifier ) throws IOException {
     this.testIdentifier = testIdentifier ;
     if( registeredTestIdentifiers.contains( testIdentifier ) ) {
       throw new IllegalArgumentException( "Already created for: " + testIdentifier ) ;
     }
     registeredTestIdentifiers.add( testIdentifier ) ;
     LOG.debug( "Created %s", this ) ;
 
   }
 
     public String toString() {
     final StandardToStringStyle style = new StandardToStringStyle() ;
     style.setUseShortClassName( true ) ;
 
     return new ToStringBuilder( this, style )
         .append( testIdentifier )
         .toString()
     ;
   }
 
   public static final String SCRATCH_DIRECTORY_SYSTEM_PROPERTY_NAME =
       "novelang.test.scratch.dir" ;
   public static final String DELETE_SCRATCH_DIRECTORY_SYSTEM_PROPERTY_NAME =
       "novelang.test.scratch.delete" ;
 
   public static final String DEFAULT_SCRATCH_DIR_NAME = "test-scratch" ;
 
   /**
    * Static field holding the directory once defined.
    */
   private static File allFixturesDirectory ;
 
   private File getAllFixturesDirectory() throws IOException {
     File file = allFixturesDirectory ;
     if( null == file ) {
 
       final String testfilesDirSystemProperty =
           System.getProperty( SCRATCH_DIRECTORY_SYSTEM_PROPERTY_NAME ) ;
       if( null == testfilesDirSystemProperty ) {
         file = new File( DEFAULT_SCRATCH_DIR_NAME ) ;
       } else {
         file = new File( testfilesDirSystemProperty ) ;
       }
 
       if(
           file.exists() &&
           ! "no".equalsIgnoreCase(
               System.getProperty( DELETE_SCRATCH_DIRECTORY_SYSTEM_PROPERTY_NAME ) )
           ) {
         FileUtils.deleteDirectory( file ) ;
       } else {
         if( file.mkdir() ) {
           LOG.debug( "Created '%s'", file.getAbsolutePath() ) ;
         }
       }
       LOG.info( "Created '%s' as clean directory for all fixtures.", file.getAbsolutePath() ) ;
     }
     allFixturesDirectory = file;
     return allFixturesDirectory ;
   }
 
   private File scratchDirectory;
 
   public File getDirectory() throws IOException {
     if( null == scratchDirectory) {
       scratchDirectory = new File( getAllFixturesDirectory(), testIdentifier ) ;
       if( scratchDirectory.exists() ) {
         FileUtils.deleteDirectory( scratchDirectory ) ;
       }
       if( scratchDirectory.mkdirs() ) {
         LOG.debug( "Created '%s'", scratchDirectory.getAbsolutePath() ) ;
       }
     }
     return scratchDirectory;
   }
 
   public File getDirectory( final String directoryName ) throws IOException {
     Preconditions.checkArgument( ! StringUtils.isBlank( directoryName ) ) ;
    final File directory = new File( scratchDirectory, directoryName ) ;
     if( directory.mkdirs() ) {
       LOG.debug( "Created '%s'", directory.getAbsolutePath() ) ;
     }
     return directory ;
   }
 
 
 }
