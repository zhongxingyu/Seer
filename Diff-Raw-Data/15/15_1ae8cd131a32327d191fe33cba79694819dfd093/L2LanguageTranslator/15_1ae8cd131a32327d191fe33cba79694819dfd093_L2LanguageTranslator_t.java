 package de.unisiegen.tpml.core.languages.l2 ;
 
 
 import de.unisiegen.tpml.core.expressions.CurriedLetRec ;
 import de.unisiegen.tpml.core.expressions.Expression ;
 import de.unisiegen.tpml.core.expressions.Identifier ;
 import de.unisiegen.tpml.core.expressions.Lambda ;
 import de.unisiegen.tpml.core.expressions.Let ;
 import de.unisiegen.tpml.core.expressions.LetRec ;
 import de.unisiegen.tpml.core.expressions.Recursion ;
 import de.unisiegen.tpml.core.languages.l1.L1Language ;
 import de.unisiegen.tpml.core.languages.l1.L1LanguageTranslator ;
 import de.unisiegen.tpml.core.types.ArrowType ;
 import de.unisiegen.tpml.core.types.MonoType ;
 
 
 /**
  * Language translator for the <code>L2</code> language.
  * 
  * @author Benedikt Meurer
  * @author Christian Fehler
  * @version $Rev:419 $
  * @see L1Language
  */
 public class L2LanguageTranslator extends L1LanguageTranslator
 {
   /**
    * Allocates a new <code>L2LanguageTranslator</code>.
    */
   public L2LanguageTranslator ( )
   {
     super ( ) ;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see L1LanguageTranslator#translateToCoreSyntax(Expression, boolean)
    */
   @ Override
   public Expression translateToCoreSyntax ( Expression expression ,
       boolean recursive )
   {
     if ( expression instanceof CurriedLetRec )
     {
       // translate to: let id1 = rec id1.lambda id2...lambda idn.e1 in e2
       CurriedLetRec curriedLetRec = ( CurriedLetRec ) expression ;
       MonoType [ ] types = curriedLetRec.getTypes ( ) ;
       Expression e1 = curriedLetRec.getE1 ( ) ;
       Expression e2 = curriedLetRec.getE2 ( ) ;
       // check if we should recurse
       if ( recursive )
       {
         e1 = this.translateToCoreSyntax ( e1 , true ) ;
         e2 = this.translateToCoreSyntax ( e2 , true ) ;
       }
       // add the lambdas
       Identifier [ ] identifiers = curriedLetRec.getIdentifiers ( ) ;
       for ( int n = identifiers.length - 1 ; n > 0 ; -- n )
       {
         e1 = new Lambda ( identifiers [ n ] , types [ n ] , e1 ) ;
       }
       // try to generate a recursive type
       MonoType tau = types [ 0 ] ;
       try
       {
         for ( int n = 1 ; n < types.length ; ++ n )
         {
           tau = new ArrowType ( types [ n ] , tau ) ;
         }
       }
       catch ( final NullPointerException e )
       {
         // no type for the recursion
         tau = null ;
       }
       // generate the let expression
      return new Let ( identifiers [ 0 ] , null , new Recursion (
           identifiers [ 0 ] , tau , e1 ) , e2 ) ;
     }
     else if ( expression instanceof LetRec )
     {
       // determine the sub expressions
       final LetRec letRec = ( LetRec ) expression ;
       Expression e1 = letRec.getE1 ( ) ;
       Expression e2 = letRec.getE2 ( ) ;
       // check if we should recurse
       if ( recursive )
       {
         e1 = this.translateToCoreSyntax ( e1 , true ) ;
         e2 = this.translateToCoreSyntax ( e2 , true ) ;
       }
       // generate the let expression
      return new Let ( letRec.getId ( ) , null , new Recursion (
           letRec.getId ( ) , letRec.getTau ( ) , e1 ) , e2 ) ;
     }
     else if ( ( expression instanceof Recursion ) && recursive )
     {
       // determine the sub expressions
       final Recursion recursion = ( Recursion ) expression ;
       Expression e = recursion.getE ( ) ;
       // translate the sub expression
       e = this.translateToCoreSyntax ( e , true ) ;
       // generate the recursion
       return new Recursion ( recursion.getId ( ) , recursion.getTau ( ) , e ) ;
     }
     else
     {
       return super.translateToCoreSyntax ( expression , recursive ) ;
     }
   }
 }
