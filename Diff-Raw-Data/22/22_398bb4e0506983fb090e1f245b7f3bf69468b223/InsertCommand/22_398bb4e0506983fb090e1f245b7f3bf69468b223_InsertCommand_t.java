 package novelang.book.function.builtin;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Maps;
 import novelang.book.CommandExecutionContext;
 import novelang.book.function.CommandParameterException;
 import novelang.book.function.builtin.insert.LevelHead;
 import novelang.book.function.builtin.insert.PartCreator;
 import novelang.common.FileTools;
 import novelang.common.Location;
 import novelang.common.Problem;
 import novelang.common.Renderable;
 import novelang.common.SimpleTree;
 import novelang.common.StructureKind;
 import novelang.common.SyntacticTree;
 import novelang.common.tree.TreeTools;
 import novelang.common.tree.Treepath;
 import novelang.common.tree.TreepathTools;
 import novelang.parser.NodeKind;
 import static novelang.parser.NodeKind.WORD_;
 import novelang.part.Part;
 import novelang.designator.FragmentIdentifier;
 import novelang.system.Log;
 import novelang.system.LogFactory;
 import novelang.treemangling.DesignatorInterpreter;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multimap;
 
 import novelang.treemangling.TreeManglingConstants;
 import org.apache.commons.io.FilenameUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 /**
  * @author Laurent Caillette
  */
 public class InsertCommand extends AbstractCommand {
   
   private static final Log LOG = LogFactory.getLog( InsertCommand.class ) ;  
   
   private final String fileName ;
   private final boolean recurse ;
   private final FileOrdering< ? > fileOrdering ;
   private final LevelHead levelHead;
   private final int levelAbove ;
   private final String styleName ;
   private final Iterable< FragmentIdentifier > fragmentIdentifiers ;
 
 
   public InsertCommand(
       final Location location,
       final String fileUrl, 
       final boolean recurse, 
       final FileOrdering fileOrdering,
       final LevelHead levelHead,
       final int levelAbove,
       final String styleName,
       final Iterable< FragmentIdentifier > fragmentIdentifiers
   ) {
     super( location ) ;
     this.fileName = fileUrl.substring( "file:".length() ) ; 
     this.recurse = recurse ;
     this.levelHead = levelHead ;
     this.styleName = styleName ;
     
     if( fileOrdering == null ) {
       LOG.debug( "No file ordering set, using default" ) ;
       this.fileOrdering = FileOrdering.DEFAULT ;      
     } else {
       this.fileOrdering = fileOrdering ;  
     }
 
     Preconditions.checkArgument(
         levelAbove >= 0,
         "'levelabove' must be 0 or greater, is %d",
         levelAbove
     ) ;
     this.levelAbove = levelAbove ;
     
     this.fragmentIdentifiers = Iterables.unmodifiableIterable( fragmentIdentifiers ) ;
   }
 
 
   
 
   public CommandExecutionContext evaluate( final CommandExecutionContext environment ) {
 
     final File insertedFile ;
     {
       final File candidateFile = new File( fileName ) ;
       if( candidateFile.isAbsolute() ) {
         insertedFile = candidateFile ;
       } else {
         insertedFile = new File( environment.getBookDirectory(), fileName ) ;
       }
     }
 
     if( insertedFile.isDirectory() ) {
       return evaluateMultiple( environment, insertedFile, recurse ) ;
     } else {
       return evaluateSingle( environment, insertedFile ) ;
     }
   }
 
   private CommandExecutionContext evaluateSingle( 
       final CommandExecutionContext environment,
       final File insertedFile
   ) {
     LOG.debug( "Command %s evaluating flatly on %s", this, insertedFile  ) ;
     
     final Part rawPart;
     try {
       rawPart = new Part( 
           insertedFile,
           environment.getSourceCharset(),
           environment.getRenderingCharset()
       ) ;
     } catch( IOException e ) {
       return environment.addProblems( Lists.newArrayList( Problem.createProblem( e ) ) ) ;
     }
 
     final Renderable partWithRelocation =
         rawPart.relocateResourcePaths( environment.getBaseDirectory() ) ;
 
     final SyntacticTree partTree = partWithRelocation.getDocumentTree() ;
     final SyntacticTree styleTree = createStyleTree( styleName ) ;
     
     Treepath< SyntacticTree > book = Treepath.create( environment.getDocumentTree() ) ;
     try {
       book = findLastLevel( book, levelAbove ) ;
     } catch ( CommandParameterException e ) {
       return environment.addProblem( Problem.createProblem( e ) ) ;
     }
 
     final Iterable< ? extends SyntacticTree > partTrees ;
 
     // TODO handle following cases altogether: fragments, createLevel.
 
     final boolean hasIdentifiers = fragmentIdentifiers.iterator().hasNext() ;
 
     if( partTree != null ) {
       if( hasIdentifiers ) {
         final AddIdentifiers addIdentifiers = new AddIdentifiers(
             new DesignatorInterpreter( Treepath.create( partTree ) ), 
             fragmentIdentifiers, 
             getLocation(),
             true
         ) ;
         if( addIdentifiers.hasDesignatorProblem() ) {
           return environment.addProblems( addIdentifiers.getDesignatorProblems() ) ;
         }
         partTrees = removeHeadIfNeeded( levelHead, addIdentifiers.getPartTrees() ) ;
       } else {
         partTrees = removeHeadIfNeeded( levelHead, partTree.getChildren() ) ;
       }
 
       if( levelHead == LevelHead.CREATE_LEVEL ) {
         book = createChapterFromPartFilename(
               book,
               insertedFile,
               partTrees,
               styleTree
           ) ;
       } else {
         for( SyntacticTree partChild : partTrees ) {
           if( styleTree != null ) {
             partChild = TreeTools.addFirst( partChild, styleTree ) ;
           }
           book = TreepathTools.addChildLast( book, partChild ).getStart() ;
         }
       }
 
     }
 
     return environment.update( book.getTreeAtStart() ).addProblems( rawPart.getProblems() ) ;
   }
 
   private static SyntacticTree createStyleTree( final String styleName ) {
     if( null == styleName ) {
       return null ;
     } else {
       return new SimpleTree( NodeKind._STYLE, new SimpleTree( styleName ) ) ;
     }
   }
 
   /**
    * When inserting from several files, Identifiers make things more complex.
    * Identifier resolution must occur on all scanned Parts as a whole for preserving uniqueness 
    * of the Identifiers. 
    * (If the user doesn't want unique Identifiers he/she should insert from each Part
    * explicitely, or use Tags.)
    * <p>
    * With {@link DesignatorInterpreter} alone, processing all scanned Parts at once drops
    * information about the originating Part (and therefore the originating File) of each 
    * inserted Fragment. This prevents level creation from working properly, and from 
    * reporting Part files in which collisions occur.
    * <p>
    * An approach for keeping originating Part would be to decorate every Identifier-enabled 
    * node with its origin before processing the whole with {@link DesignatorInterpreter}. 
    * This will happen in the future, but not as a particular case here.
    * <p>
    * The approach of choice relies on after-the-fact conflict detection. 
    * If requested Identifiers appear once and once only in the set of inserted Parts, 
    * then processing each Part individually gives the same result. Identifier unicity check
    * occurs by ensuring that requested Identifier appears once and once only in every
    * {@link DesignatorInterpreter}.
    */
   private CommandExecutionContext evaluateMultiple(
       final CommandExecutionContext environment,
       final File insertedFile,
       final boolean recurse
   ) {
     LOG.debug( "Command %s evaluating recursively on %s", this, insertedFile  ) ;
     
     final List< Problem > problems = Lists.newArrayList() ;
     final SyntacticTree styleTree = createStyleTree( styleName ) ;
     Treepath< SyntacticTree > book = Treepath.create( environment.getDocumentTree() ) ;
     final boolean hasIdentifiers = fragmentIdentifiers.iterator().hasNext() ;
     final Multimap< FragmentIdentifier, Part > identifiedFragments ;
     if( hasIdentifiers ) {
       identifiedFragments = HashMultimap.create();
     } else {
       identifiedFragments = null ;
     }
     
 
     try {
       book = findLastLevel( book, levelAbove ) ;
       final Iterable< File > partFiles = scanPartFiles( insertedFile, recurse ) ;
       final Map< File, Future< Part > > futureParts = Maps.newHashMap() ;
 
 
       for( final File partFile : partFiles ) {
         final PartCreator partCreator = new PartCreator(
             partFile, environment.getSourceCharset(), environment.getRenderingCharset() ) ;
         futureParts.put( partFile, environment.getExecutorService().submit( partCreator ) ) ;
       }
 
       for( final File partFile : partFiles ) {
         Part part = null ;
         try {
           // environment.getExecutorService()...
           part = futureParts.get( partFile ).get() ;
           Iterables.addAll( problems, part.getProblems() ) ;
         } catch( ExecutionException e ) {
           problems.add( Problem.createProblem( ( Exception ) e.getCause() ) ) ;
         } catch( InterruptedException e ) {
           problems.add( Problem.createProblem( e ) ) ;
         }
         if( null != part && null != part.getDocumentTree() ) {
           final Part relocatedPart = part.relocateResourcePaths( environment.getBaseDirectory() ) ;
           Iterables.addAll( problems, relocatedPart.getProblems() ) ;
 
           final SyntacticTree partTree = relocatedPart.getDocumentTree() ;
           final List< SyntacticTree > partChildren = Lists.newArrayList() ;
 
           if( hasIdentifiers ) {
             final DesignatorInterpreter designatorInterpreter =
                 new DesignatorInterpreter( Treepath.create( partTree ) ) ;
             if( designatorInterpreter.hasProblem() ) {
               Iterables.addAll( problems, designatorInterpreter.getProblems() ) ;
             } else {
               for( final FragmentIdentifier fragmentIdentifier : fragmentIdentifiers ) {
                 final Treepath< SyntacticTree > fragment =
                     designatorInterpreter.get( fragmentIdentifier ) ;
                 if( fragment != null ) {
                   identifiedFragments.put( fragmentIdentifier, part ) ;
                   Iterables.addAll( partChildren,
                       removeHeadIfNeeded( levelHead, fragment.getTreeAtEnd() ) ) ;
                 }
               }
             }
 
           } else {
             Iterables.addAll( partChildren,
                 removeHeadIfNeeded( levelHead, partTree.getChildren() ) ) ;
           }
 
           if( levelHead == LevelHead.CREATE_LEVEL ) {
             book = createChapterFromPartFilename(
                 book,
                 partFile,
                 partChildren,
                 styleTree
             ) ;
             book = findLastLevel( book, levelAbove ) ;
           } else {
             for( SyntacticTree partChild : partChildren ) {
               if( styleTree != null ) {
                 partChild = TreeTools.addFirst( partChild, styleTree ) ;
               }
               final Treepath< SyntacticTree > updatedBook =
                   TreepathTools.addChildLast( book, partChild ) ;
               book = Treepath.create( updatedBook.getTreeAtStart() ) ;
               book = findLastLevel( book, levelAbove ) ;
             }
           }
 
         }
       }
 
       if( hasIdentifiers ) {
         Iterables.addAll( problems, createProblems( identifiedFragments, getLocation() ) ) ;
       }
 
     } catch( CommandParameterException e ) {
       problems.add( Problem.createProblem( e ) ) ;
     }
 
     return environment.update( book.getTreeAtStart() ).addProblems( problems ) ;
   }
 
 
   private static Treepath< SyntacticTree > findLastLevel(
       final Treepath< SyntacticTree > document,
       final int depth
   ) throws CommandParameterException {
     if( depth == 0 ) {
       return document ;
     }
     final SyntacticTree tree = document.getTreeAtEnd() ;
     final int lastChildIndex = tree.getChildCount() - 1 ;
     if( lastChildIndex < 0 ) {
       throw new CommandParameterException( "Found no child tree while seeking level " + depth ) ;
     }
     final SyntacticTree lastChild = tree.getChildAt( lastChildIndex ) ;
     if( lastChild.isOneOf( NodeKind._LEVEL ) ) {
       return findLastLevel( Treepath.create( document, lastChildIndex ), depth - 1 ) ;
     } else {
       throw new CommandParameterException( "Found no LEVEL as child tree" ) ;
     }
   }
 
 
   private static Treepath< SyntacticTree > createChapterFromPartFilename(
       Treepath< SyntacticTree > book,
       final File partFile,
       final Iterable< ? extends SyntacticTree > partTrees,
       final SyntacticTree styleTree
   ) {
     final SyntacticTree word = new SimpleTree(
         WORD_,
         new SimpleTree( FilenameUtils.getBaseName( partFile.getName() ) )
     ) ;
     final SyntacticTree title = new SimpleTree( NodeKind.LEVEL_TITLE, word ) ;
 
     SyntacticTree chapterTree = TreeTools.addFirst(
         new SimpleTree( NodeKind._LEVEL, partTrees ),
         title
     ) ;
 
     if( styleTree != null ) {
       chapterTree = TreeTools.addFirst( chapterTree, styleTree ) ;
     }
     final Treepath< SyntacticTree > updatedBook = TreepathTools.addChildLast( book, chapterTree ) ;
     final SyntacticTree start = updatedBook.getTreeAtStart() ;
     book = Treepath.create( start ) ;
     return book ;
   }
 
   private Iterable< File > scanPartFiles( final File directory, final boolean recurse )
       throws CommandParameterException
   {
     if( directory.isDirectory() ) {
       final Iterable< File > files ;
       try {
         final List< File > scannedFiles = FileTools.scanFiles( 
             directory, StructureKind.PART.getFileExtensions(), recurse );
         files = fileOrdering.sort( scannedFiles ) ;
       } catch ( FileOrdering.CriteriaException e ) {
         LOG.info( "Could not sort files from '" + directory.getAbsolutePath() + "'", e ) ;
         throw new CommandParameterException( "Could not sort files: " + e.getMessage() ) ;
       }
 
       if( LOG.isDebugEnabled() ) {
         final StringBuffer buffer = new StringBuffer(
             "Scan of '" + directory.getAbsolutePath() + "' found those files:" ) ;
         for( final File file : files ) {
           try {
             buffer.append( "\n  " ).append( file.getCanonicalPath() ) ;
           } catch( IOException e ) {
             throw new RuntimeException( e ) ;
           }
         }
         LOG.debug( buffer.toString() ) ;
       }
 
       return files ;
 
     } else {
       throw new CommandParameterException(
           "Not a directory: '" + directory.getAbsolutePath() + "'" ) ;
     }
   }
 
 
   private static Iterable< Problem > createProblems(
       final Multimap< FragmentIdentifier, Part > identifiedFragments,
       final Location location
   ) {
     final List< Problem > problems = Lists.newArrayList() ;
     for( final FragmentIdentifier fragmentIdentifier : identifiedFragments.keySet() ) {
       final Collection< Part > parts = identifiedFragments.get( fragmentIdentifier ) ;
       if( parts == null ) {
         problems.add( Problem.createProblem(
             "Could not find " + fragmentIdentifier + " in any given Part",
             location
         ) ) ;
       } else if( parts.size() > 1 ) {
         problems.add( Problem.createProblem(
             "Identifier " + fragmentIdentifier + " found multiple times in:" +
             Joiner.on( "\n" ).join( parts )
             ,
             location
         ) ) ;
       }
     }
     return problems ;
   }

  /**
   * Kind of function with multiple return values.
   */
   private static class AddIdentifiers {
     
     private final List< Problem > designatorProblems = Lists.newArrayList() ;
     private final List< SyntacticTree > partTrees = Lists.newArrayList();
 
     public AddIdentifiers( 
         final DesignatorInterpreter designatorMapper,
         final Iterable< FragmentIdentifier > fragmentIdentifiers,
         final Location location,
         final boolean createProblemForUnmappedIdentifier
     ) {
 
       for( final FragmentIdentifier fragmentIdentifier : fragmentIdentifiers ) {
         final Treepath< SyntacticTree > fragmentTreepath =
             designatorMapper.get( fragmentIdentifier ) ;
         if( createProblemForUnmappedIdentifier && fragmentTreepath == null ) {
           designatorProblems.add( 
               Problem.createProblem( 
                   "Cannot find: '" + fragmentIdentifier + "'", location ) ) ;
         } else {
           final SyntacticTree fragment = fragmentTreepath.getTreeAtEnd() ;
           partTrees.add( fragment ) ;
         }
       }
       Iterables.addAll( designatorProblems, designatorMapper.getProblems() ) ;
     }
     
     public boolean hasDesignatorProblem() {
       return designatorProblems.size() > 0 ;
     }
     
     public Iterable< Problem > getDesignatorProblems() {
       return designatorProblems ;
     }
 
     public Iterable< SyntacticTree > getPartTrees() {
       return partTrees ;
     }
   }
 
   private static Iterable< ? extends SyntacticTree > removeHeadIfNeeded(
       final LevelHead levelHead,
       final SyntacticTree tree
   ) {
     return removeHeadIfNeeded( levelHead, ImmutableList.of( tree ) ) ;
   }
 
   private static Iterable< ? extends SyntacticTree > removeHeadIfNeeded(
       final LevelHead levelHead,
       final Iterable< ? extends SyntacticTree > trees
   ) {
     if( levelHead == LevelHead.NO_HEAD ) {
       SyntacticTree level = null ;
       int levelCount = 0 ;
       int paragraphoidCount = 0 ;
 
       for( final SyntacticTree tree : trees ) {
         if( tree.getNodeKind() == NodeKind._LEVEL ) {
           level = tree ;
            levelCount ++ ;
         }
         if( TreeManglingConstants.PARAGRAPHOID_NODEKINDS.contains( tree.getNodeKind() ) ) {
           paragraphoidCount ++ ;
         }
       }
       if( levelCount == 1 && paragraphoidCount == 0 ) {
         final SyntacticTree cleanedTree = TreeTools.remove( level, LEVEL_DECORATION_PREDICATE ) ;
         return cleanedTree.getChildren() ;
       }
     }
     return trees ;
   }
 
   
 
   private static final Predicate<SyntacticTree> LEVEL_DECORATION_PREDICATE =
       new Predicate< SyntacticTree >() {
         public boolean apply( final SyntacticTree syntacticTree ) {
           return TreeManglingConstants.LEVEL_DECORATION_NODEKINDS
               .contains( syntacticTree.getNodeKind() ) ;
         }
       }
   ;
 
 
 
   @Override
   public String toString() {
     return "InsertCommand{" +
         "fileName='" + fileName + '\'' +
         ", recurse=" + recurse +
         ", levelHead=" + levelHead +
         ", styleName='" + styleName + '\'' +
         ", fragmentIdentifiers=" + fragmentIdentifiers +
         '}'
     ;
   }
 }
