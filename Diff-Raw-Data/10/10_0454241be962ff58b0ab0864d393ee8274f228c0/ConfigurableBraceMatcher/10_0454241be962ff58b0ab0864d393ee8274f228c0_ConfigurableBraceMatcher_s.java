 package net.intensicode.idea.core;
 
 import com.intellij.lang.BracePair;
 import com.intellij.lang.PairedBraceMatcher;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.psi.tree.IElementType;
 import net.intensicode.idea.config.BracesConfiguration;
 import net.intensicode.idea.config.InstanceConfiguration;
 import net.intensicode.idea.config.LanguageConfiguration;
 import net.intensicode.idea.util.LoggerFactory;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 
 /**
  * TODO: Describe this!
  */
 final class ConfigurableBraceMatcher implements PairedBraceMatcher
 {
     ConfigurableBraceMatcher( final InstanceConfiguration aConfiguration )
     {
         myLanguage = aConfiguration.getLanguageConfiguration();
 
         final BracesConfiguration braces = aConfiguration.getBracesConfiguration();
         final List<BracePair> bracePairs = createBracePairs( braces.getBracePairs(), false );
         final List<BracePair> structuralPairs = createBracePairs( braces.getStructuralPairs(), true );
         bracePairs.addAll( structuralPairs );
 
         myPairs = new BracePair[bracePairs.size()];
         bracePairs.toArray( myPairs );
     }
 
     // From PairedBraceMatcher
 
     public final BracePair[] getPairs()
     {
         return myPairs;
     }
 
     public final boolean isPairedBracesAllowedBeforeType( @NotNull final IElementType aIElementType, @Nullable final IElementType aIElementType1 )
     {
         return false;
     }
 
     // Implementation
 
     private final ArrayList<BracePair> createBracePairs( final String[] aBracePairs, final boolean aStructural )
     {
         final ArrayList<BracePair> result = new ArrayList<BracePair>();
         for ( final String pair : aBracePairs )
         {
             if ( pair.length() != 2 )
             {
                 LOG.error( "Ignoring invalid brace pair spec: " + pair );
                 continue;
             }
             final char left = pair.charAt( 0 );
             final char right = pair.charAt( 1 );
             final IElementType leftToken = myLanguage.getToken( new String( new char[]{ left } ) );
             final IElementType rightToken = myLanguage.getToken( new String( new char[]{ right } ) );
            result.add( new BracePair( left, leftToken, right, rightToken, aStructural ) );
         }
         return result;
     }
 
 
 
     private final BracePair[] myPairs;
 
     private final LanguageConfiguration myLanguage;
 
     private static final Logger LOG = LoggerFactory.getLogger();
 }
