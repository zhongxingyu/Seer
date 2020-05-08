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
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package novelang.treemangling;
 
 import novelang.common.Problem;
 import novelang.common.SimpleTree;
 import novelang.common.SyntacticTree;
 import novelang.common.tree.Traversal;
 import novelang.common.tree.Treepath;
 import novelang.common.tree.TreepathTools;
 import novelang.common.tree.RobustPath;
 import novelang.designator.FragmentIdentifier;
 import novelang.parser.NodeKind;
 import novelang.system.Log;
 import novelang.system.LogFactory;
 import novelang.treemangling.designator.BabyInterpreter;
 import novelang.treemangling.designator.DesignatorTools;
 import novelang.treemangling.designator.FragmentMapper;
 
 import com.google.common.collect.Maps;
 
 import java.util.Arrays;
 import java.util.Map;
 
 /**
  * Creates a Mapping from designators ({@link FragmentIdentifier}s or, in the future, tags)
  * and to {@code Treepath}s with nodes containing expanded identifiers (both explicit
  * and implicit, resolved to absolute identifiers).
  * <p>
  * Implementation: due to the immutable nature of the {@code Treepath} this is done 
  * in several passes. 
  * <ol>
  *   <li>
  *     The {@link BabyInterpreter} (used internally) gathers
  *     references on the nodes containing identifiers, resolving implicit references and
  *     finding problems.
  *   </li>
  *   <li>
  *     The {@link #enrich(Treepath, FragmentMapper)} method (used internally) enhances the 
  *     {@code Treepath} with new nodes (
  *     {@link NodeKind#_IMPLICIT_IDENTIFIER} and
  *     {@link NodeKind#_EXPLICIT_IDENTIFIER}
  *     ) containing resolved identifiers. 
  *     Those nodes enhance document readability as they show resolved identifiers.
  *     The same method removes 
  *     {@link NodeKind#ABSOLUTE_IDENTIFIER} {@link NodeKind#RELATIVE_IDENTIFIER} 
  *     nodes which don't improve readability.
  *   </li>
  *   <li>
  *     Identifiers from {@link BabyInterpreter} are re-mapped
  *     on the new {@code Treepath}.
  *   </li>
  * </ol> 
  * 
  *
  * @author Laurent Caillette
  */
 public class DesignatorInterpreter {
   
   private static final Log LOG = LogFactory.getLog( DesignatorInterpreter.class ) ;
 
   /**
    * Contains only pure identifiers (defined explicitely).
    */
   private final Map< FragmentIdentifier, Treepath< SyntacticTree > > pureIdentifiers ;
 
   /**
    * Contains implicit identifiers, or implicit identifiers mixed with explicit identifiers.
    */
   private final Map< FragmentIdentifier, Treepath< SyntacticTree > > derivedIdentifiers ;
 
   private final Iterable< Problem > problems  ;
 
   /**
    * Made package-protected for tests.
    */
   /*package*/ static final Traversal.MirroredPostorder< SyntacticTree > TRAVERSAL =
       Traversal.MirroredPostorder.create( DesignatorTools.IDENTIFIER_TREE_FILTER )
   ;
   private final Treepath< SyntacticTree > enrichedTreepath;
 
   public DesignatorInterpreter( final Treepath< SyntacticTree > treepath ) {
     final BabyInterpreter babyInterpreter = new BabyInterpreter( treepath ) ;
     enrichedTreepath = enrich(
         TRAVERSAL.first( treepath ),
         babyInterpreter
     );
 
     pureIdentifiers = remap( babyInterpreter.getPureIdentifierMap(), enrichedTreepath ) ;
     derivedIdentifiers = remap( babyInterpreter.getDerivedIdentifierMap(), enrichedTreepath ) ;
     problems = babyInterpreter.getProblems() ;
 
     if ( LOG.isDebugEnabled() ) {
       final StringBuilder stringBuilder = new StringBuilder() ;
       stringBuilder.append( "\n  Pure identifiers:" ) ;
       DesignatorTools.dumpIdentifierMap( stringBuilder, pureIdentifiers, "\n    " ) ;
       stringBuilder.append( "\n  Derived identifiers:" ) ;
       DesignatorTools.dumpIdentifierMap( stringBuilder, derivedIdentifiers, "\n    " ) ;
       LOG.debug( "Created %s%s", this, stringBuilder ) ;
     }
 
   }
 
   /**
    * Returns the {@code Treepath} with enriched Designators.
    * @return an object with the same content as the one passed to the constructor, except
    *    for Identifiers.
    */
   public Treepath< SyntacticTree > getEnrichedTreepath() {
     return enrichedTreepath ;
   }
 
   /**
    * Given a {@link FragmentIdentifier}, returns corresponding {@link SyntacticTree}.
    */
   public Treepath< SyntacticTree > get( final FragmentIdentifier fragmentIdentifier ) {
     Treepath< SyntacticTree > treepath = pureIdentifiers.get( fragmentIdentifier ) ;
     if( treepath == null ) {
       treepath = derivedIdentifiers.get( fragmentIdentifier ) ;
     }
     return treepath ;
   }
 
 
   /**
    * Returns problems like duplicate identifier.
    */
   public Iterable< Problem > getProblems() {
     return problems ;
   }
 
   public boolean hasProblem() {
     return problems.iterator().hasNext() ;
   }
 
 
 // ===============  
 // Transformations
 // ===============
   
   private static Map< FragmentIdentifier, Treepath< SyntacticTree > >
   remap(
       final Map< FragmentIdentifier, RobustPath< SyntacticTree > > map,
       final Treepath< SyntacticTree > treepath
   ) {
     final Map< FragmentIdentifier, Treepath< SyntacticTree > > result = Maps.newHashMap() ;
     for( final Map.Entry< FragmentIdentifier, RobustPath< SyntacticTree > > entry :
         map.entrySet()
     ) {
       result.put( 
           entry.getKey(), 
           entry.getValue().apply( treepath.getTreeAtStart() )  
       ) ;
     }
     return result ;
   }
   
   
   
 
   /**
    * (Made protected for tests only.)
    * Transform a whole tree adding new "synthetic" identifiers that were calculated by 
    * {@link BabyInterpreter} and removing original ones. 
    * The tree transformation uses mirrored postorder traversal. This kind of traversal guarantees
    * that addind/removing trees doesn't affect indexes of unprocessed trees.
    * 
    * @param treepath The 
    *         {@link Traversal.MirroredPostorder#first(Treepath) first} tree in a mirrored postorder
    *         traversal.
    */
   protected static Treepath< SyntacticTree > enrich(
       Treepath< SyntacticTree > treepath,
       final FragmentMapper< RobustPath< SyntacticTree > > mapper
   ) {
     while( true ) {

/*
      // Doesn't seem to help:
      // Remove previous implicit identifiers. This is required for Books, where implicit
      // identifiers come from previously computed Parts.
      // TODO add a parameter to Part creation telling it shouldn't calculate implicit identifiers.
      treepath = removeDirectChildren(
          treepath,
          NodeKind._IMPLICIT_IDENTIFIER
      ) ;
*/
      
       final FragmentIdentifier pureIdentifier = 
           findIdentifier( treepath, mapper.getPureIdentifierMap() ) ;
       if( pureIdentifier != null ) {
         treepath = add( treepath, pureIdentifier, NodeKind._EXPLICIT_IDENTIFIER ) ;
       }
       final FragmentIdentifier derivedIdentifier = 
           findIdentifier( treepath, mapper.getDerivedIdentifierMap() ) ;
       if( derivedIdentifier != null ) {
         treepath = add( treepath, derivedIdentifier, NodeKind._IMPLICIT_IDENTIFIER ) ;
       }
       treepath = removeDirectChildren(
           treepath,
           NodeKind.ABSOLUTE_IDENTIFIER,
           NodeKind.RELATIVE_IDENTIFIER
       ) ;
 
       final Treepath< SyntacticTree > next = TRAVERSAL.next( treepath ) ;
 
       if( next == null ) {
         return treepath ;
       } else {
         treepath = next ; 
       }
     }
   }
 
   private static Treepath< SyntacticTree > removeDirectChildren(
       final Treepath< SyntacticTree > treepath, 
       final NodeKind... nodeKindsToRemove
   ) {
     final SyntacticTree tree = treepath.getTreeAtEnd() ;
     for( int i = 0 ; i < tree.getChildCount() ; i ++ ) {
       final SyntacticTree child = tree.getChildAt( i ) ;
       if( child.isOneOf( nodeKindsToRemove ) ) {
         return TreepathTools.removeEnd( Treepath.create( treepath, i ) ) ;
       }
     }
     return treepath ;    
   }
 
   private static Treepath< SyntacticTree > add( 
       final Treepath< SyntacticTree > treepath, 
       final FragmentIdentifier fragmentIdentifier, 
       final NodeKind explicitIdentifier 
   ) {
     final SyntacticTree identifierTree = new SimpleTree( 
         explicitIdentifier, 
         new SimpleTree( fragmentIdentifier.getAbsoluteRepresentation() ) 
     ) ;
     return TreepathTools.addChildFirst( treepath, identifierTree ).getPrevious() ;
   }
 
   private static FragmentIdentifier findIdentifier( 
       final Treepath< SyntacticTree > treepath, 
       final Map< FragmentIdentifier, RobustPath< SyntacticTree > > map
   ) {
     final int[] indexesInParent = treepath.getIndicesInParent() ;
     for( final Map.Entry< FragmentIdentifier, RobustPath< SyntacticTree > > entry : map.entrySet()
     ) {
       final Treepath< SyntacticTree > found = entry.getValue().apply( treepath.getTreeAtStart() ) ;
       if( Arrays.equals( indexesInParent, found.getIndicesInParent() ) ) {
         return entry.getKey() ;
       }
     }
     return null ;
   }
 }
