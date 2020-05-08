 import java.util.Collections;
 import java.util.ArrayList;
 
 class Alkane {
 
     private String description;
     private ArrayList<Carbon> carbons = new ArrayList<Carbon>();
 
     private static String[] iupacNames = null;
 
     public Alkane( String description ) {
       this.description = description;
 
       build( description );
     }
 
     private ArrayList<Carbon> build( String desc ) {
       ArrayList<Carbon> accumCarbons = new ArrayList<Carbon>();
 
       for ( int pos = 0; pos < desc.length(); pos++ ) {
         if ( desc.charAt( pos ) == 'C' ) {
           Carbon newCarbon;
 
           if ( pos < desc.length() - 1 && desc.charAt( pos+1 ) == '(' ) {
             int startParenPos = ++pos,
               endParenPos = startParenPos,
               parenLevel = 1;
 
             while ( parenLevel > 0 ) {
               switch ( desc.charAt( ++endParenPos ) ) {
                 case '(': parenLevel++; break;
                 case ')': parenLevel--; break;
               }
             }
 
             newCarbon = new Carbon(
               build(desc.substring(startParenPos+1, endParenPos)) );
 
             pos = endParenPos;
           }
           else {
             newCarbon = new Carbon();
           }
 
           carbons.add( newCarbon );
           accumCarbons.add( newCarbon );
         }
         else {
           throw new IllegalArgumentException( "Strange description: '"
                                               + desc + "'" + pos );
         }
       }
 
       return accumCarbons;
     }
 
     public String toString() {
       return normalization();
     }
 
     public String normalization() {
       ArrayList<String> descriptions = new ArrayList<String>();
 
       if ( carbons.size() == 1 )
         return "C";
 
       for ( Carbon leaf : leaves() )
         descriptions.add( traverse(leaf, new ArrayList<Carbon>()) );
 
       Collections.sort( descriptions );
       return descriptions.size() > 0 ? descriptions.get(0) : "";
     }
 
     private String traverse( Carbon node,
                              ArrayList<Carbon> alreadyTraversed ) {
 
       ArrayList<String> descriptions = new ArrayList<String>();
 
       alreadyTraversed.add(node);
 
       for ( Carbon neighbor : node.neighbors() )
         if ( !alreadyTraversed.contains(neighbor) )
            descriptions.add( traverse(neighbor, alreadyTraversed) );
 
       Collections.sort( descriptions );
 
       StringBuilder sb = new StringBuilder();
       sb.append('C');
 
       if ( descriptions.size() > 0 ) {
         sb.append('(');
         for ( String desc : descriptions )
           sb.append( desc );
         sb.append(')');
       }
 
       return sb.toString();
     }
 
     private ArrayList<Carbon> leaves() {
       ArrayList<Carbon> leaves = new ArrayList<Carbon>();
 
       for ( Carbon carbon : this.carbons )
         if ( carbon.neighbors().size() == 1 )
           leaves.add( carbon );
 
       return leaves;
     }
 
     public int longestChainLength() {
      if (carbons.isEmpty())
        return 0;

      int longestLength = 1;
 
       for ( Carbon leaf : leaves() ) {
         ArrayList<Carbon>     queue = new ArrayList<Carbon>(),
                           traversed = new ArrayList<Carbon>();
         queue.add(leaf);
 
         int length = 0;
         for ( ; !queue.isEmpty(); length++ ) {
           ArrayList<Carbon> newNodes = new ArrayList<Carbon>();
 
           while ( !queue.isEmpty() ) {
             Carbon currentNode = queue.remove(0);
             traversed.add(currentNode);
 
             for ( Carbon neighbor : currentNode.neighbors() )
               if ( !traversed.contains(neighbor) )
                 newNodes.add(neighbor);
           }
 
           queue = newNodes;
         }
         if ( longestLength < length )
           longestLength = length;
       }
 
       return longestLength;
     }     
 
     public String iupacName() {
       
       if ( iupacNames == null ) {
         
         String[] lowNames = new String[] {
          "",
           "Methane", "Ethane",  "Propane",
           "Buthane", "Pentane", "Hexane",
           "Heptane", "Octane",  "Nonane",
         };
 
         ArrayList<String> names = new ArrayList<String>();
         for ( int i = 0; i < lowNames.length; i++ )
           names.add( lowNames[i] );
 
         String[] suffixes = new String[] {
          "",
           "Decane",       "Cosane",       "Triacontane",
           "Tetracontane", "Pentacontane", "Hexacontane",
           "Heptacontane", "Octacontane",  "Nonacontane",
         };
 
         String[] prefixes = new String[] {
          "",
           "Hen",   "Do",    "Tri",
           "Tetra", "Penta", "Hexa",
           "Hepta", "Octa",  "Nona",
         };
 
         for ( int tens = 1; tens <= 9; tens++ ) {
           names.add( suffixes[tens] );
           for ( int ones = 1; ones <= 9; ones++ )
             names.add( prefixes[ones] + suffixes[tens].toLowerCase() );
         }
 
         // Exceptions
         names.set( 11, "Undecane" );
         names.set( 20, "Eicosane" );
         names.set( 21, "Heneicosane" );
         names.add("Hectane");
 
         iupacNames = names.toArray( new String[0] );
       }
 
       return iupacNames[longestChainLength()];
     }
 
     public boolean equals( Object o ) {
       if ( !(o instanceof Alkane) )
         return false;
 
       Alkane other = (Alkane)o;
       return normalization().equals( other.normalization() );
     }
 
     public int hashCode() {
       return normalization().hashCode();
     }
 }
