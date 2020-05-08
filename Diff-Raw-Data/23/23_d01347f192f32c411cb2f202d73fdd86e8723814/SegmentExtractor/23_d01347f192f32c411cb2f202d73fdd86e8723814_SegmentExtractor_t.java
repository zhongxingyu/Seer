 package novelang.treemangling.designator;
 
 import java.util.List;
 
 import novelang.common.SyntacticTree;
 import novelang.common.Problem;
 import novelang.common.tree.Treepath;
 import novelang.parser.NodeKind;
 import novelang.designator.FragmentIdentifier;
import novelang.parser.NodeKindTools;
 
 /**
  * Given a {@link Treepath} to some identifier-bearing tree, extracts a single identifier
  * segment. The segment may be self-defining if the tree has a {@link NodeKind#ABSOLUTE_IDENTIFIER}
  * as immediate child, or may rely on other segments in the parents.
  *
  * @author Laurent Caillette
 */
 public class SegmentExtractor
 {
   private final IdentifierDefinition identifierDefinition ;
   private final String segment ;
 
   /**
    *
    * @param treepath Path to the node that may carry some (implicit or explicit) identifier.
    * @param problems a list of problems to add to one occurs.
    */
   public SegmentExtractor(
       final Treepath< SyntacticTree > treepath,
       final List< Problem > problems
   ) {
 
     final Treepath< SyntacticTree > pathToIdentifier =
         DesignatorTools.findPathToExplicitIdentifier( treepath ) ;
     final SyntacticTree tree = pathToIdentifier == null ?
             null :
             pathToIdentifier.getTreeAtEnd()
     ;
 
    if( NodeKindTools.is( NodeKind.ABSOLUTE_IDENTIFIER, tree ) ) {
       segment = extractSegment( tree ) ;
       identifierDefinition = IdentifierDefinition.ABSOLUTE ;
    } else if( NodeKindTools.is( NodeKind.RELATIVE_IDENTIFIER, tree ) ) {
       segment = extractSegment( tree ) ;
       identifierDefinition = IdentifierDefinition.RELATIVE ;
     } else {
       String markerText ;
       final SyntacticTree titleTree = DesignatorTools.findTitleTree( treepath.getTreeAtEnd() ) ;
 
       if( titleTree == null ) {
         markerText = null ;
       } else {
         try {
           markerText = DesignatorTools.getMarkerText( titleTree ) ;
         } catch( Exception e ) {
           // Exception type is a bit wide but it's due to the signature of getMarkerText
           // which delegates to a Renderer/Writer inside of which so many things can go wrong.
           // Not sure this is a Problem (user input), may be a technical exception at this stage.
           problems.add( Problem.createProblem( e ) ) ;
           markerText = null ;
         }
       }
       if( markerText == null ) {
         segment = null ;
         identifierDefinition = IdentifierDefinition.NONE ;
       } else {
         segment = markerText ;
         identifierDefinition = IdentifierDefinition.IMPLICIT ;
       }
     }
   }
 
   private static String extractSegment( final SyntacticTree identifierTree ) {
     return identifierTree.getChildAt( 0 ).getText() ;
   }
 
   public IdentifierDefinition getIdentifierDefinition() {
     return identifierDefinition ;
   }
 
   public String getSegment() {
     return segment ;
   }
 
   /*package*/ static FragmentIdentifier extract(
       final FragmentIdentifier parentIdentifier,
       final Treepath< SyntacticTree > pathToIdentifier
   ) {
     final SyntacticTree identifierTree = pathToIdentifier.getTreeAtEnd() ;
     if( NodeKind.ABSOLUTE_IDENTIFIER == identifierTree.getNodeKind() ) {
       return new FragmentIdentifier( extractSegment( identifierTree ) ) ;
     } else if( NodeKind.RELATIVE_IDENTIFIER == identifierTree.getNodeKind() ) {
       if( parentIdentifier == null ) {
         throw new IllegalArgumentException( // TODO accumulate errors instead.
             "Missing absolute identifier above relative identifier " + identifierTree ) ;
       } else {
         return new FragmentIdentifier(
             parentIdentifier,
             new FragmentIdentifier( extractSegment( identifierTree ) )
         ) ;
       }
     }
     return null ;
   }
 }
