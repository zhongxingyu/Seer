 /*
  * Polyglotter (http://polyglotter.org)
  * See the COPYRIGHT.txt file distributed with this work for information
  * regarding copyright ownership.  Some portions may be licensed
  * to Red Hat, Inc. under one or more contributor license agreements.
  * See the AUTHORS.txt file in the distribution for a full listing of 
  * individual contributors.
  *
  * Polyglotter is free software. Unless otherwise indicated, all code in Polyglotter
  * is licensed to you under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * Polyglotter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.modeshape.modeler.internal;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Modifier;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.Property;
 import javax.jcr.Repository;
 import javax.jcr.Session;
 import javax.jcr.Value;
 import javax.jcr.nodetype.NodeType;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.modeshape.common.util.CheckArg;
 import org.modeshape.jcr.JcrLexicon;
 import org.modeshape.jcr.api.JcrTools;
 import org.modeshape.jcr.api.sequencer.Sequencer;
 import org.modeshape.modeler.ModeShapeModeler;
 import org.modeshape.modeler.ModelType;
 import org.modeshape.modeler.ModelTypeManager;
 import org.modeshape.modeler.ModelerException;
 import org.modeshape.modeler.ModelerI18n;
 import org.modeshape.modeler.extensions.DependencyProcessor;
 import org.polyglotter.common.Logger;
 
 /**
  * 
  */
 public final class ModelTypeManagerImpl implements ModelTypeManager {
 
     static final Logger LOGGER = Logger.getLogger( ModelTypeManagerImpl.class );
 
     private static final String MODESHAPE_GROUP = "org/modeshape";
 
     final Manager manager;
 
     final LinkedList< URL > modelTypeRepositories = new LinkedList<>();
 
     final Set< ModelType > modelTypes = new HashSet<>();
     final LibraryClassLoader libraryClassLoader = new LibraryClassLoader();
     final Map< String, Set< String > > potentialSequencerClassNamesByCategory = new HashMap<>();
     final Path library;
     final Map< String, DependencyProcessor > dependencyProcessorsByModelTypeName = new HashMap<>();
 
     ModelTypeManagerImpl( final Manager manager ) throws ModelerException {
         this.manager = manager;
         try {
             library = Files.createTempDirectory( null );
         } catch ( final IOException e ) {
             throw new ModelerException( e );
         }
         library.toFile().deleteOnExit();
         manager.run( this, new SystemTask< Void >() {
 
             @SuppressWarnings( "unchecked" )
             @Override
             public Void run( final Session session,
                              final Node systemNode ) throws Exception {
                 // Load model type repositories
                 if ( !systemNode.hasProperty( ModelerLexicon.MODEL_TYPE_REPOSITORIES ) ) {
                     final Value[] vals = new Value[ 2 ];
                     vals[ 0 ] = session.getValueFactory().createValue( JBOSS_MODEL_TYPE_REPOSITORY );
                     vals[ 1 ] = session.getValueFactory().createValue( MAVEN_MODEL_TYPE_REPOSITORY );
                     systemNode.setProperty( ModelerLexicon.MODEL_TYPE_REPOSITORIES, vals );
                     session.save();
                 }
                 for ( final Value val : systemNode.getProperty( ModelerLexicon.MODEL_TYPE_REPOSITORIES ).getValues() )
                     modelTypeRepositories.add( new URL( val.getString() ) );
                 // Load jars
                 if ( !systemNode.hasNode( ModelerLexicon.JARS ) ) {
                     systemNode.addNode( ModelerLexicon.JARS );
                     session.save();
                 }
                 for ( final NodeIterator iter = systemNode.getNode( ModelerLexicon.JARS ).getNodes(); iter.hasNext(); ) {
                     final Node node = iter.nextNode();
                     final Path jarPath = library.resolve( node.getName() );
                     try ( InputStream stream =
                         node.getNode( JcrLexicon.CONTENT.getString() ).getProperty( JcrLexicon.DATA.getString() ).getBinary()
                             .getStream() ) {
                         Files.copy( stream, jarPath );
                     }
                     jarPath.toFile().deleteOnExit();
                     libraryClassLoader.addURL( jarPath.toUri().toURL() );
                     LOGGER.debug( "Installed jar: %s", jarPath );
                 }
                 // Load model types
                 if ( !systemNode.hasNode( ModelerLexicon.MODEL_TYPES ) ) {
                     systemNode.addNode( ModelerLexicon.MODEL_TYPES );
                     session.save();
                 }
                 for ( final NodeIterator iter = systemNode.getNode( ModelerLexicon.MODEL_TYPES ).getNodes(); iter.hasNext(); ) {
                     final Node node = iter.nextNode();
                     modelTypes.add( new ModelTypeImpl( manager,
                                                        node.getProperty( ModelerLexicon.CATEGORY ).getString(),
                                                        node.getName(),
                                                        ( Class< Sequencer > ) libraryClassLoader.loadClass( node.getProperty( ModelerLexicon.SEQUENCER_CLASS )
                                                                                                                 .getString() ),
                                                        null,
                                                        null ) );
                 }
                 // Load potential sequencer class names
                 if ( !systemNode.hasNode( ModelerLexicon.POTENTIAL_SEQUENCER_CLASS_NAMES_BY_CATEGORY ) ) {
                     systemNode.addNode( ModelerLexicon.POTENTIAL_SEQUENCER_CLASS_NAMES_BY_CATEGORY );
                     session.save();
                 }
                 for ( final NodeIterator iter =
                     systemNode.getNode( ModelerLexicon.POTENTIAL_SEQUENCER_CLASS_NAMES_BY_CATEGORY ).getNodes(); iter.hasNext(); ) {
                     final Node node = iter.nextNode();
                     final Set< String > names = new HashSet<>();
                     for ( final Value val : node.getProperty( ModelerLexicon.POTENTIAL_SEQUENCER_CLASS_NAMES ).getValues() )
                         names.add( val.getString() );
                     potentialSequencerClassNamesByCategory.put( node.getName(), names );
                 }
                 return null;
             }
         } );
     }
 
     String archiveName( final String category ) throws ModelerException {
         return "modeshape-sequencer-" + category + "-" + version() + "-module-with-dependencies.zip";
     }
 
     /**
      * @param fileNode
      *        the file node
      * @param modelTypes
      *        the model types applicable to the supplied file node
      * @return the default model type for the supplied file node
      * @throws Exception
      *         if any problem occurs
      */
     public ModelType defaultModelType( final Node fileNode,
                                        final ModelType[] modelTypes ) throws Exception {
         final String ext = fileNode.getName().substring( fileNode.getName().lastIndexOf( '.' ) + 1 );
         for ( final ModelType type : modelTypes )
             for ( final String typeExt : type.sourceFileExtensions() )
                 if ( typeExt.equals( ext ) ) return type;
         return modelTypes.length == 0 ? null : modelTypes[ 0 ];
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see ModelTypeManager#defaultModelType(String)
      */
     @Override
     public ModelType defaultModelType( final String filePath ) throws ModelerException {
         CheckArg.isNotEmpty( filePath, "filePath" );
         return manager.run( new Task< ModelType >() {
 
             @Override
             public ModelType run( final Session session ) throws Exception {
                 final Node node = manager.artifactNode( session, filePath );
                 final ModelType type = defaultModelType( node, modelTypes( node ) );
                 return type == null ? null : type;
             }
         } );
     }
 
     /**
      * @param modelNode
      *        the model node whose dependency processor is being requested (cannot be <code>null</code>)
      * @return the dependency processor or <code>null</code> if not found
      * @throws ModelerException
      *         if specified node is not a model node or if an error occurs
      */
     public DependencyProcessor dependencyProcessor( final Node modelNode ) throws ModelerException {
         CheckArg.isNotNull( modelNode, "modelNode" );
 
         try {
             boolean foundMixin = false;
 
             for ( final NodeType mixin : modelNode.getMixinNodeTypes() ) {
                 if ( ModelerLexicon.MODEL_MIXIN.equals( mixin.getName() ) ) {
                     foundMixin = true;
                     break;
                 }
             }
 
             if ( !foundMixin ) {
                 throw new ModelerException( ModelerI18n.mustBeModelNode, modelNode.getName() );
             }
 
             return dependencyProcessorsByModelTypeName.get( modelNode.getPath() );
         } catch ( final Exception e ) {
             throw new ModelerException( e );
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see ModelTypeManager#install(String)
      */
     @Override
     public String[] install( final String category ) throws ModelerException {
         CheckArg.isNotEmpty( category, "category" );
         LOGGER.debug( "Installing model types from category %s", category );
         try {
             final String archiveName = archiveName( category );
             // Return if archive has already been installed
             if ( manager.run( this, new SystemTask< Boolean >() {
 
                 @Override
                 public Boolean run( final Session session,
                                     final Node systemNode ) throws Exception {
                     if ( systemNode.hasProperty( ModelerLexicon.ZIPS ) )
                         for ( final Value val : systemNode.getProperty( ModelerLexicon.ZIPS ).getValues() )
                             if ( val.getString().equals( archiveName ) ) {
                                 LOGGER.debug( "Archive already installed: %s", archiveName );
                                 return true;
                             }
                     return false;
                 }
             } ) ) return potentialSequencerClassNames();
             final Path archivePath = library.resolve( archiveName );
             final String sequencerArchivePath =
                 MODESHAPE_GROUP + "/modeshape-sequencer-" + category + '/' + version() + '/' + archiveName;
             for ( final URL repositoryUrl : modelTypeRepositories ) {
                 final URL url = new URL( path( repositoryUrl.toString(), sequencerArchivePath ) );
                 InputStream urlStream = null;
                 IOException err = null;
                 try {
                     try {
                         urlStream = url.openStream();
                     } catch ( final IOException e ) {
                         continue;
                     }
                     Files.copy( urlStream, archivePath );
                 } catch ( final IOException e ) {
                     err = e;
                 } finally {
                     if ( urlStream != null ) try {
                         urlStream.close();
                     } catch ( final IOException e ) {
                         if ( err == null ) throw e;
                         err.addSuppressed( e );
                         throw err;
                     }
                 }
                 try ( final ZipFile archive = new ZipFile( archivePath.toFile() ) ) {
                     for ( final Enumeration< ? extends ZipEntry > archiveIter = archive.entries(); archiveIter.hasMoreElements(); ) {
                         final ZipEntry archiveEntry = archiveIter.nextElement();
                         if ( archiveEntry.isDirectory() ) continue;
                         String name = archiveEntry.getName().toLowerCase();
                         if ( !name.endsWith( ".jar" ) || name.endsWith( "-tests.jar" ) || name.endsWith( "-sources.jar" ) ) {
                             LOGGER.debug( "Ignoring Jar: %s", name );
                             continue;
                         }
                         final Path jarPath =
                             library.resolve( archiveEntry.getName().substring( archiveEntry.getName().lastIndexOf( '/' ) + 1 ) );
                         if ( jarPath.toFile().exists() ) {
                             LOGGER.debug( "Jar already installed: %s", jarPath );
                             continue;
                         }
                         manager.run( this, new SystemTask< Void >() {
 
                             @Override
                             public Void run( final Session session,
                                              final Node systemNode ) throws Exception {
                                 try ( InputStream stream = archive.getInputStream( archiveEntry ) ) {
                                     final Node node =
                                         new JcrTools().uploadFile( session,
                                                                    systemNode.getPath() + '/' + ModelerLexicon.JARS + '/'
                                                                                    + jarPath.getFileName().toString(),
                                                                    stream );
                                     node.addMixin( ModelerLexicon.UNSTRUCTURED_MIXIN );
                                     node.setProperty( ModelerLexicon.CATEGORY, category );
                                     session.save();
                                 }
                                 return null;
                             }
                         } );
                         try ( InputStream stream = archive.getInputStream( archiveEntry ) ) {
                             Files.copy( stream, jarPath );
                         }
                         jarPath.toFile().deleteOnExit();
                         libraryClassLoader.addURL( jarPath.toUri().toURL() );
                         LOGGER.debug( "Installed jar: %s", jarPath );
                         try ( final ZipFile jar = new ZipFile( jarPath.toFile() ) ) {
                             for ( final Enumeration< ? extends ZipEntry > jarIter = jar.entries(); jarIter.hasMoreElements(); ) {
                                 final ZipEntry jarEntry = jarIter.nextElement();
                                 if ( jarEntry.isDirectory() ) continue;
                                 name = jarEntry.getName();
                                 if ( jarPath.getFileName().toString().contains( "sequencer" )
                                      && name.endsWith( "Sequencer.class" ) ) {
                                     Set< String > names = potentialSequencerClassNamesByCategory.get( category );
                                     if ( names == null ) {
                                         names = new HashSet<>();
                                         potentialSequencerClassNamesByCategory.put( category, names );
                                     }
                                     names.add( name.replace( '/', '.' ).substring( 0, name.length() - ".class".length() ) );
                                     LOGGER.debug( "Potential sequencer: %s", name );
                                 }
                             }
                         }
                     }
                     for ( final Iterator< Entry< String, Set< String > > > categoryIter =
                         potentialSequencerClassNamesByCategory.entrySet().iterator(); categoryIter.hasNext(); ) {
                         final Entry< String, Set< String > > entry = categoryIter.next();
                         for ( final Iterator< String > iter = entry.getValue().iterator(); iter.hasNext(); ) {
                             try {
                                 @SuppressWarnings( "unchecked" ) final Class< Sequencer > sequencerClass =
                                     ( Class< Sequencer > ) libraryClassLoader.loadClass( iter.next() );
                                 if ( Sequencer.class.isAssignableFrom( sequencerClass )
                                      && !Modifier.isAbstract( sequencerClass.getModifiers() ) ) {
                                     String id = ModeShapeModeler.class.getPackage().getName() + '.' + category + '.'
                                                 + sequencerClass.getSimpleName();
                                     id = id.endsWith( "Sequencer" ) ? id.substring( 0, id.length() - "Sequencer".length() ) : id;
                                     final ModelTypeImpl type =
                                         new ModelTypeImpl( manager, category, id, sequencerClass, null, null );
                                     modelTypes.add( type );
                                     manager.run( this, new SystemTask< Void >() {
 
                                         @Override
                                         public Void run( final Session session,
                                                          final Node systemNode ) throws Exception {
                                             final Node node = systemNode.getNode( ModelerLexicon.MODEL_TYPES ).addNode( type.id() );
                                             node.setProperty( ModelerLexicon.SEQUENCER_CLASS, sequencerClass.getName() );
                                             node.setProperty( ModelerLexicon.CATEGORY, category );
                                             session.save();
                                             return null;
                                         }
                                     } );
                                 }
                                 iter.remove();
                             } catch ( final NoClassDefFoundError | ClassNotFoundException ignored ) {
                                 // Class will be re-tested as a Sequencer when the next archive is installed
                             }
                         }
                         if ( entry.getValue().isEmpty() ) categoryIter.remove();
                     }
                 }
                 archivePath.toFile().delete();
                 manager.run( this, new SystemTask< Void >() {
 
                     @Override
                     public Void run( final Session session,
                                      final Node systemNode ) throws Exception {
                         // Save that archive has been installed
                         Value[] vals =
                             systemNode.hasProperty( ModelerLexicon.ZIPS ) ? systemNode.getProperty( ModelerLexicon.ZIPS ).getValues()
                                                                          : new Value[ 0 ];
                         final Value[] newVals = new Value[ vals.length + 1 ];
                         System.arraycopy( vals, 0, newVals, 0, vals.length );
                         newVals[ vals.length ] = session.getValueFactory().createValue( archiveName );
                         systemNode.setProperty( ModelerLexicon.ZIPS, newVals );
                         // Save potential class names
                         final Node categoryNode = systemNode.getNode( ModelerLexicon.POTENTIAL_SEQUENCER_CLASS_NAMES_BY_CATEGORY );
                         for ( final Entry< String, Set< String > > entry : potentialSequencerClassNamesByCategory.entrySet() ) {
                             final Node node =
                                 !categoryNode.hasNode( entry.getKey() ) ? categoryNode.addNode( entry.getKey() ) :
                                                                        categoryNode.getNode( entry.getKey() );
                             vals = new Value[ entry.getValue().size() ];
                             int ndx = 0;
                             for ( final String className : entry.getValue() )
                                 vals[ ndx++ ] = session.getValueFactory().createValue( className );
                             node.setProperty( ModelerLexicon.POTENTIAL_SEQUENCER_CLASS_NAMES, vals );
                         }
                         session.save();
                         return null;
                     }
                 } );
                 return potentialSequencerClassNames();
             }
         } catch ( final IOException e ) {
             throw new ModelerException( e );
         }
         throw new IllegalArgumentException( ModelerI18n.unableToFindModelTypeCategory.text( category ) );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see ModelTypeManager#installableModelTypeCategories()
      */
     @Override
     public String[] installableModelTypeCategories() throws ModelerException {
         final Set< String > categories = new HashSet<>();
         for ( final URL repositoryUrl : modelTypeRepositories ) {
             try {
                 if ( repositoryUrl.getProtocol().startsWith( "file" ) ) {
                    final File folder = new File( new URL( repositoryUrl, MODESHAPE_GROUP ).getPath() );
                     for ( final File file : folder.listFiles() ) {
                         final String name = file.getName();
                         if ( name.contains( "sequencer-" ) )
                             categories.add( name.substring( name.indexOf( "sequencer-" ) + "sequencer-".length() ) );
                     }
                 } else {
                     final Document doc = Jsoup.connect( path( repositoryUrl.toString(), MODESHAPE_GROUP ) ).get();
                     final Elements elements = doc.getElementsMatchingOwnText( "sequencer-" );
                     for ( final Element element : elements ) {
                         final String href = element.attr( "href" );
                         categories.add( href.substring( href.indexOf( "sequencer-" ) + "sequencer-".length(), href.lastIndexOf( '/' ) ) );
                     }
                 }
             } catch ( final IOException e ) {
                 throw new ModelerException( e );
             }
         }
         return categories.toArray( new String[ categories.size() ] );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see ModelTypeManager#modelType(String)
      */
     @Override
     public ModelType modelType( final String id ) {
         CheckArg.isNotEmpty( id, "id" );
         for ( final ModelType type : modelTypes )
             if ( id.equals( type.id() ) ) return type;
         return null;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see ModelTypeManager#modelTypeCategories()
      */
     @Override
     public String[] modelTypeCategories() {
         final Set< String > categories = new HashSet<>();
         for ( final ModelType type : modelTypes )
             categories.add( type.category() );
         return categories.toArray( new String[ categories.size() ] );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see ModelTypeManager#modelTypeRepositories()
      */
     @Override
     public URL[] modelTypeRepositories() {
         return modelTypeRepositories.toArray( new URL[ modelTypeRepositories.size() ] );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see ModelTypeManager#modelTypes()
      */
     @Override
     public ModelType[] modelTypes() {
         return modelTypes.toArray( new ModelType[ modelTypes.size() ] );
     }
 
     /**
      * @param fileNode
      *        the file node
      * @return the model types applicable to the supplied file node
      * @throws Exception
      *         if any problem occurs
      */
     public ModelType[] modelTypes( final Node fileNode ) throws Exception {
         final Set< ModelType > applicableModelTypes = new HashSet<>();
         for ( final ModelType type : modelTypes() )
             if ( ( ( ModelTypeImpl ) type ).sequencer()
                                            .isAccepted( fileNode.getNode( JcrLexicon.CONTENT.getString() )
                                                                 .getProperty( JcrLexicon.MIMETYPE.getString() ).getString() ) )
                 applicableModelTypes.add( type );
         return applicableModelTypes.toArray( new ModelType[ applicableModelTypes.size() ] );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see ModelTypeManager#modelTypesForArtifact(String)
      */
     @Override
     public ModelType[] modelTypesForArtifact( final String filePath ) throws ModelerException {
         CheckArg.isNotEmpty( filePath, "filePath" );
         return manager.run( new Task< ModelType[] >() {
 
             @Override
             public final ModelType[] run( final Session session ) throws Exception {
                 return modelTypes( manager.artifactNode( session, filePath ) );
             }
         } );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see ModelTypeManager#modelTypesForCategory(String)
      */
     @Override
     public ModelType[] modelTypesForCategory( final String category ) {
         CheckArg.isNotEmpty( category, "category" );
         final Set< ModelType > types = new HashSet<>();
         for ( final ModelType type : modelTypes )
             if ( category.equals( type.category() ) ) types.add( type );
         return types.toArray( new ModelType[ types.size() ] );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see ModelTypeManager#moveModelTypeRepositoryDown(URL)
      */
     @Override
     public URL[] moveModelTypeRepositoryDown( final URL repositoryUrl ) throws ModelerException {
         CheckArg.isNotNull( repositoryUrl, "repositoryUrl" );
         final int ndx = modelTypeRepositories.indexOf( repositoryUrl );
         if ( ndx < 0 ) throw new IllegalArgumentException( ModelerI18n.urlNotFound.text( repositoryUrl ) );
         modelTypeRepositories.remove( ndx );
         modelTypeRepositories.add( Math.min( ndx + 1, modelTypeRepositories.size() ), repositoryUrl );
         saveModelTypeRepositories();
         return modelTypeRepositories();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see ModelTypeManager#moveModelTypeRepositoryUp(URL)
      */
     @Override
     public URL[] moveModelTypeRepositoryUp( final URL repositoryUrl ) throws ModelerException {
         CheckArg.isNotNull( repositoryUrl, "repositoryUrl" );
         final int ndx = modelTypeRepositories.indexOf( repositoryUrl );
         if ( ndx < 0 ) throw new IllegalArgumentException( ModelerI18n.urlNotFound.text( repositoryUrl ) );
         modelTypeRepositories.remove( ndx );
         modelTypeRepositories.add( Math.max( ndx - 1, 0 ), repositoryUrl );
         saveModelTypeRepositories();
         return modelTypeRepositories();
     }
 
     private String path( final String prefix,
                          final String suffix ) {
         if ( prefix.charAt( prefix.length() - 1 ) == '/' )
             return suffix.charAt( 0 ) == '/' ? prefix + suffix.substring( 1 ) : prefix + suffix;
         return suffix.charAt( 0 ) == '/' ? prefix + suffix : prefix + '/' + suffix;
     }
 
     private String[] potentialSequencerClassNames() {
         final Set< String > classNames = new HashSet<>();
         for ( final Set< String > names : potentialSequencerClassNamesByCategory.values() )
             classNames.addAll( names );
         return classNames.toArray( new String[ classNames.size() ] );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see ModelTypeManager#registerModelTypeRepository(URL)
      */
     @Override
     public URL[] registerModelTypeRepository( final URL repositoryUrl ) throws ModelerException {
         CheckArg.isNotNull( repositoryUrl, "repositoryUrl" );
         if ( !modelTypeRepositories.contains( repositoryUrl ) ) {
             modelTypeRepositories.addFirst( repositoryUrl );
             saveModelTypeRepositories();
         }
         return modelTypeRepositories();
     }
 
     private void saveModelTypeRepositories() throws ModelerException {
         manager.run( this, new SystemTask< Void >() {
 
             @Override
             public Void run( final Session session,
                              final Node systemNode ) throws Exception {
                 final Value[] vals = new Value[ modelTypeRepositories.size() ];
                 int ndx = 0;
                 for ( final URL url : modelTypeRepositories )
                     vals[ ndx++ ] = session.getValueFactory().createValue( url.toString() );
                 systemNode.setProperty( ModelerLexicon.MODEL_TYPE_REPOSITORIES, vals );
                 session.save();
                 return null;
             }
         } );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see ModelTypeManager#uninstall(String)
      */
     @Override
     public void uninstall( final String category ) throws ModelerException {
         CheckArg.isNotEmpty( category, "category" );
         for ( final Iterator< ModelType > iter = modelTypes.iterator(); iter.hasNext(); )
             if ( category.equals( iter.next().category() ) ) iter.remove();
         potentialSequencerClassNamesByCategory.remove( category );
         manager.run( this, new SystemTask< Void >() {
 
             @Override
             public Void run( final Session session,
                              final Node systemNode ) throws Exception {
                 final Property prop = systemNode.getProperty( ModelerLexicon.ZIPS );
                 final Value[] vals = prop.getValues();
                 final Value[] newVals = new Value[ vals.length - 1 ];
                 boolean found = false;
                 int newNdx = 0;
                 final String archiveName = archiveName( category );
                 for ( final Value val : vals )
                     if ( val.getString().equals( archiveName ) )
                         found = true;
                     else if ( newVals.length > 0 ) newVals[ Math.min( newNdx++, newVals.length ) ] = val;
                 if ( !found ) return null;
                 prop.setValue( newVals );
                 for ( final NodeIterator iter = systemNode.getNode( ModelerLexicon.JARS ).getNodes(); iter.hasNext(); ) {
                     final Node node = iter.nextNode();
                     if ( !node.getProperty( ModelerLexicon.CATEGORY ).equals( category ) ) continue;
                     final Path jarPath = library.resolve( node.getName() );
                     if ( jarPath.toFile().delete() ) LOGGER.debug( "Unable to delete jar: %s", jarPath );
                     node.remove();
                     LOGGER.debug( "Uninstalled jar: %s", jarPath );
                 }
                 final Node node = systemNode.getNode( ModelerLexicon.POTENTIAL_SEQUENCER_CLASS_NAMES_BY_CATEGORY );
                 if ( node.hasNode( category ) ) node.getNode( category ).remove();
                 session.save();
                 return null;
             }
         } );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see ModelTypeManager#unregisterModelTypeRepository(URL)
      */
     @Override
     public URL[] unregisterModelTypeRepository( final URL repositoryUrl ) throws ModelerException {
         CheckArg.isNotNull( repositoryUrl, "repositoryUrl" );
         if ( modelTypeRepositories.remove( repositoryUrl ) ) saveModelTypeRepositories();
         return modelTypeRepositories();
     }
 
     private String version() throws ModelerException {
         return manager.repository().getDescriptor( Repository.REP_VERSION_DESC );
     }
 
     class LibraryClassLoader extends URLClassLoader {
 
         LibraryClassLoader() {
             super( new URL[ 0 ], LibraryClassLoader.class.getClassLoader() );
         }
 
         @Override
         protected void addURL( final URL url ) {
             super.addURL( url );
         }
     }
 }
