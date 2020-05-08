 package de.unisiegen.tpml.core.languages.l4 ;
 
 
 import java.text.MessageFormat ;
 import java.util.ArrayList ;
 import de.unisiegen.tpml.core.Messages ;
 import de.unisiegen.tpml.core.expressions.Identifier ;
 import de.unisiegen.tpml.core.languages.l1.L1Language ;
 import de.unisiegen.tpml.core.languages.l2.L2Language ;
 import de.unisiegen.tpml.core.languages.l3.L3Language ;
 import de.unisiegen.tpml.core.typechecker.DefaultTypeSubstitution ;
 import de.unisiegen.tpml.core.typechecker.SeenTypes ;
 import de.unisiegen.tpml.core.typechecker.TypeCheckerProofContext ;
 import de.unisiegen.tpml.core.typechecker.TypeCheckerProofNode ;
 import de.unisiegen.tpml.core.typeinference.DefaultTypeInferenceProofContext ;
 import de.unisiegen.tpml.core.typeinference.TypeEquationProofNode ;
 import de.unisiegen.tpml.core.typeinference.TypeEquationTypeInference ;
 import de.unisiegen.tpml.core.typeinference.TypeInferenceProofContext ;
 import de.unisiegen.tpml.core.typeinference.UnifyException ;
 import de.unisiegen.tpml.core.types.ArrowType ;
 import de.unisiegen.tpml.core.types.ListType ;
 import de.unisiegen.tpml.core.types.MonoType ;
 import de.unisiegen.tpml.core.types.ObjectType ;
 import de.unisiegen.tpml.core.types.RecType ;
 import de.unisiegen.tpml.core.types.RefType ;
 import de.unisiegen.tpml.core.types.RowType ;
 import de.unisiegen.tpml.core.types.TupleType ;
 import de.unisiegen.tpml.core.types.TypeVariable ;
 import de.unisiegen.tpml.core.util.Debug ;
 
 
 /**
  * The type proof rules for the <code>L4</code> language.
  * 
  * @author Benjamin Mies
  * @author Christian Fehler
  * @see de.unisiegen.tpml.core.typechecker.AbstractTypeCheckerProofRuleSet
  */
 public class L4TypeInferenceProofRuleSet extends L4TypeCheckerProofRuleSet
 {
   /**
    * Allocates a new <code>L4TypeInferenceProofRuleSet</code> for the
    * specified <code>language</code>.
    * 
    * @param language the <code>L4</code> or a derived language.
    * @throws NullPointerException if <code>language</code> is
    *           <code>null</code>.
    */
   public L4TypeInferenceProofRuleSet ( L4Language language )
   {
     super ( language ) ;
     // unregister("P-LET");
     unregister ( "ABSTR" ) ; //$NON-NLS-1$
     unregister ( "AND" ) ; //$NON-NLS-1$
     unregister ( "APP" ) ; //$NON-NLS-1$
     unregister ( "COND" ) ; //$NON-NLS-1$
     unregister ( "LET" ) ; //$NON-NLS-1$
     unregister ( "OR" ) ; //$NON-NLS-1$
     unregister ( "REC" ) ; //$NON-NLS-1$
     unregister ( "LIST" ) ; //$NON-NLS-1$
     unregister ( "P-CONST" ) ; //$NON-NLS-1$
     unregister ( "P-ID" ) ; //$NON-NLS-1$
     unregister ( "P-LET" ) ; //$NON-NLS-1$
     unregister ( "TUPLE" ) ; //$NON-NLS-1$
     unregister ( "COND-1" ) ; //$NON-NLS-1$
     unregister ( "SEQ" ) ; //$NON-NLS-1$
     unregister ( "WHILE" ) ; //$NON-NLS-1$
     // register the additional typeinference rule
     registerByMethodName ( L1Language.L1 , "UNIFY" , "applyUnify" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     // register the type rules
     registerByMethodName ( L1Language.L1 , "ABSTR" , "applyAbstr" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     registerByMethodName ( L2Language.L2 , "AND" , "applyAnd" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     registerByMethodName ( L1Language.L1 , "APP" , "applyApp" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     registerByMethodName ( L1Language.L1 , "COND" , "applyCond" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     registerByMethodName ( L1Language.L1 , "LET" , "applyLet" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     registerByMethodName ( L2Language.L2 , "OR" , "applyOr" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     registerByMethodName ( L2Language.L2 , "REC" , "applyRec" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     registerByMethodName ( L3Language.L3 , "LIST" , "applyList" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     registerByMethodName ( L3Language.L3 , "P-CONST" , "applyPConst" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     registerByMethodName ( L3Language.L3 , "P-ID" , "applyPId" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     // registerByMethodName(L3Language.L3, "P-LET", "applyPLet");
     registerByMethodName ( L3Language.L3 , "TUPLE" , "applyTuple" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     registerByMethodName ( L4Language.L4 , "COND-1" , "applyCond1" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     registerByMethodName ( L4Language.L4 , "SEQ" , "applySeq" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     registerByMethodName ( L4Language.L4 , "WHILE" , "applyWhile" ) ; //$NON-NLS-1$ //$NON-NLS-2$
   }
 
 
   /**
    * Applies the <b>(UNIFY)</b> rule to the <code>node</code> using the
    * <code>context</code>.
    * 
    * @param pContext the type inference proof context.
    * @param pNode the type inference proof node.
    * @throws UnifyException
    */
   public void applyUnify ( TypeCheckerProofContext pContext ,
       TypeCheckerProofNode pNode ) throws UnifyException
   {
     // convert in needed types
    TypeInferenceProofContext context = ( DefaultTypeInferenceProofContext ) pContext ;
     TypeEquationProofNode node = ( TypeEquationProofNode ) pNode ;
     TypeEquationTypeInference eqn = node.getEquation ( ) ;
     unify ( context , node , eqn ) ;
   }
 
 
   /**
    * internal implementation of the unify rule now we are able to call unify
    * recursive if needed so we get different handling for beginner or advanced
    * user
    * 
    * @param context the casted default type inference proof context.
    * @param node the casted type equation proof node.
    * @param eqn the actual type equation
    * @throws UnifyException
    */
   public void unify ( TypeInferenceProofContext context ,
       TypeEquationProofNode node , TypeEquationTypeInference eqn )
       throws UnifyException
   {
     // empty equation is not longer possible so this rule is not implemented
     MonoType left = eqn.getLeft ( ) ;
     MonoType right = eqn.getRight ( ) ;
     // ASSUME
     if ( eqn.getSeenTypes ( ).contains ( eqn ) )
     {
       Debug.out.println (
           "Unify - ASSUME      " + left + " = " + right , Debug.CHRISTIAN ) ; //$NON-NLS-1$//$NON-NLS-2$
       return ;
     }
     // TRIV
     else if ( left.equals ( right ) )
     {
       Debug.out.println (
           "Unify - TRIV        " + left + " = " + right , Debug.CHRISTIAN ) ; //$NON-NLS-1$//$NON-NLS-2$
       return ;
     }
     // MU-LEFT
     else if ( left instanceof RecType )
     {
       RecType recType = ( RecType ) left ;
       Debug.out.println (
           "Unify - MU-LEFT     " + left + " = " + right , Debug.CHRISTIAN ) ; //$NON-NLS-1$//$NON-NLS-2$
       context.addEquation ( new TypeEquationTypeInference ( recType.getTau ( )
           .substitute ( recType.getTypeName ( ) , recType ) , right , eqn
           .getSeenTypes ( ).clone ( ) ) ) ;
       return ;
     }
     // MU-RIGHT
     else if ( right instanceof RecType )
     {
       RecType recType = ( RecType ) right ;
       Debug.out.println (
           "Unify - MU-RIGHT    " + left + " = " + right , Debug.CHRISTIAN ) ; //$NON-NLS-1$//$NON-NLS-2$
       context.addEquation ( new TypeEquationTypeInference ( left , recType
           .getTau ( ).substitute ( recType.getTypeName ( ) , recType ) , eqn
           .getSeenTypes ( ).clone ( ) ) ) ;
       return ;
     }
     // VAR
     else if ( left instanceof TypeVariable || right instanceof TypeVariable )
     {
       // the left or right side of the equation is a type variable
       TypeVariable tvar = ( TypeVariable ) ( left instanceof TypeVariable ? left
           : right ) ;
       MonoType tau = ( left instanceof TypeVariable ? right : left ) ;
       Debug.out.println (
           "Unify - VAR         " + tvar + " = " + tau , Debug.CHRISTIAN ) ; //$NON-NLS-1$//$NON-NLS-2$
       // either tvar equals tau or tvar is not present in tau
       if ( ! tvar.equals ( tau )
           && ! tau.getTypeVariablesFree ( ).contains ( tvar ) )
       {
         DefaultTypeSubstitution s = new DefaultTypeSubstitution ( tvar , tau ) ;
         context.addSubstitution ( s ) ;
         return ;
       }
       // Error, because of a recursive type like: alpha1 = int -> alpha1
     }
     // ARROW
     else if ( left instanceof ArrowType && right instanceof ArrowType )
     {
       ArrowType taul = ( ArrowType ) left ;
       ArrowType taur = ( ArrowType ) right ;
       Debug.out.println (
           "Unify - ARROW       " + left + " = " + right , Debug.CHRISTIAN ) ; //$NON-NLS-1$//$NON-NLS-2$
       // check which mode is choosen
       if ( node.getMode ( ) )
       {
         // advanced mode is choosen
         // unify tau1 = tau1', tau2 = tau2'
         SeenTypes < TypeEquationTypeInference > seenTypes1 = eqn
             .getSeenTypes ( ).clone ( ) ;
         seenTypes1.add ( eqn ) ;
         unify ( context , node , new TypeEquationTypeInference ( taul
             .getTau2 ( ) , taur.getTau2 ( ) , seenTypes1 ) ) ;
         SeenTypes < TypeEquationTypeInference > seenTypes2 = eqn
             .getSeenTypes ( ).clone ( ) ;
         seenTypes2.add ( eqn ) ;
         TypeEquationTypeInference eqn2 = new TypeEquationTypeInference ( taul
             .getTau1 ( ) , taur.getTau1 ( ) , seenTypes2 ) ;
         eqn2 = eqn2.substitute ( context
             .getSubstitution ( ) ) ;
         unify ( context , node , eqn2 ) ;
       }
       else
       {
         // beginner mode is choosen
         // equations are added to list and will be unified later
         SeenTypes < TypeEquationTypeInference > seenTypes1 = eqn
             .getSeenTypes ( ).clone ( ) ;
         seenTypes1.add ( eqn ) ;
         TypeEquationTypeInference eqn1 = new TypeEquationTypeInference ( taul
             .getTau1 ( ) , taur.getTau1 ( ) , seenTypes1 ) ;
         context.addEquation ( eqn1 ) ;
         SeenTypes < TypeEquationTypeInference > seenTypes2 = eqn
             .getSeenTypes ( ).clone ( ) ;
         seenTypes2.add ( eqn ) ;
         TypeEquationTypeInference eqn2 = new TypeEquationTypeInference ( taul
             .getTau2 ( ) , taur.getTau2 ( ) , seenTypes2 ) ;
         context.addEquation ( eqn2 ) ;
       }
       return ;
     }
     // TUPLE
     else if ( left instanceof TupleType && right instanceof TupleType )
     {
       Debug.out.println (
           "Unify - TUPLE       " + left + " = " + right , Debug.CHRISTIAN ) ; //$NON-NLS-1$//$NON-NLS-2$
       // cast to TupleType instances (tau and tau')
       TupleType taul = ( TupleType ) left ;
       TupleType taur = ( TupleType ) right ;
       // determine the sub types
       MonoType [ ] typesl = taul.getTypes ( ) ;
       MonoType [ ] typesr = taur.getTypes ( ) ;
       // check if the arities match
       if ( typesl.length == typesr.length )
       {
         // check all sub types
         // context.setEquations(eqns.getRemaining());
         for ( int n = 0 ; n < typesl.length ; ++ n )
         {
           SeenTypes < TypeEquationTypeInference > seenTypes = eqn
               .getSeenTypes ( ).clone ( ) ;
           seenTypes.add ( eqn ) ;
           context.addEquation ( new TypeEquationTypeInference ( typesl [ n ] ,
               typesr [ n ] , seenTypes ) ) ;
         }
         return ;
       }
       throw new RuntimeException ( MessageFormat.format ( Messages
           .getString ( "UnificationException.4" ) , left , right ) ) ; //$NON-NLS-1$
     }
     // LIST
     else if ( left instanceof ListType && right instanceof ListType )
     {
       Debug.out.println (
           "Unify - LIST        " + left + " = " + right , Debug.CHRISTIAN ) ; //$NON-NLS-1$//$NON-NLS-2$
       // cast to ListType instances (tau and tau')
       ListType taul = ( ListType ) left ;
       ListType taur = ( ListType ) right ;
       SeenTypes < TypeEquationTypeInference > seenTypes = eqn.getSeenTypes ( )
           .clone ( ) ;
       seenTypes.add ( eqn ) ;
       context.addEquation ( new TypeEquationTypeInference ( taul.getTau ( ) ,
           taur.getTau ( ) , seenTypes ) ) ;
       return ;
     }
     // REF
     else if ( left instanceof RefType && right instanceof RefType )
     {
       Debug.out.println (
           "Unify - REF         " + left + " = " + right , Debug.CHRISTIAN ) ; //$NON-NLS-1$//$NON-NLS-2$
       // cast to RefType instances (tau and tau')
       RefType taul = ( RefType ) left ;
       RefType taur = ( RefType ) right ;
       SeenTypes < TypeEquationTypeInference > seenTypes = eqn.getSeenTypes ( )
           .clone ( ) ;
       seenTypes.add ( eqn ) ;
       context.addEquation ( new TypeEquationTypeInference ( taul.getTau ( ) ,
           taur.getTau ( ) , seenTypes ) ) ;
       return ;
     }
     // OBJECT
     else if ( ( left instanceof ObjectType ) && ( right instanceof ObjectType ) )
     {
       Debug.out.println (
           "Unify - OBJECT      " + left + " = " + right , Debug.CHRISTIAN ) ; //$NON-NLS-1$//$NON-NLS-2$
       ObjectType tau1 = ( ObjectType ) left ;
       ObjectType tau2 = ( ObjectType ) right ;
       SeenTypes < TypeEquationTypeInference > seenTypes = eqn.getSeenTypes ( )
           .clone ( ) ;
       seenTypes.add ( eqn ) ;
       TypeEquationTypeInference newEqn = new TypeEquationTypeInference ( tau1
           .getPhi ( ) , tau2.getPhi ( ) , seenTypes ) ;
       context.addEquation ( newEqn ) ;
       return ;
     }
     // ROW
     else if ( ( left instanceof RowType ) && ( right instanceof RowType ) )
     {
       Debug.out.println (
           "Unify - ROW         " + left + " = " + right , Debug.CHRISTIAN ) ; //$NON-NLS-1$//$NON-NLS-2$
       RowType tau1 = ( RowType ) left ;
       RowType tau2 = ( RowType ) right ;
       ArrayList < Identifier > tau1Identifiers = new ArrayList < Identifier > ( ) ;
       for ( Identifier id : tau1.getIdentifiers ( ) )
       {
         tau1Identifiers.add ( id ) ;
       }
       ArrayList < Identifier > tau2Identifiers = new ArrayList < Identifier > ( ) ;
       for ( Identifier id : tau2.getIdentifiers ( ) )
       {
         tau2Identifiers.add ( id ) ;
       }
       ArrayList < MonoType > tau1Types = new ArrayList < MonoType > ( ) ;
       for ( MonoType tau : tau1.getTypes ( ) )
       {
         tau1Types.add ( tau ) ;
       }
       ArrayList < MonoType > tau2Types = new ArrayList < MonoType > ( ) ;
       for ( MonoType tau : tau2.getTypes ( ) )
       {
         tau2Types.add ( tau ) ;
       }
       // Unify child types
       for ( int i = tau1Identifiers.size ( ) - 1 ; i >= 0 ; i -- )
       {
         for ( int j = tau2Identifiers.size ( ) - 1 ; j >= 0 ; j -- )
         {
           if ( tau1Identifiers.get ( i ).equals ( tau2Identifiers.get ( j ) ) )
           {
             SeenTypes < TypeEquationTypeInference > seenTypes = eqn
                 .getSeenTypes ( ).clone ( ) ;
             seenTypes.add ( eqn ) ;
             context.addEquation ( new TypeEquationTypeInference ( tau1Types
                 .get ( i ) , tau2Types.get ( j ) , seenTypes ) ) ;
             tau1Identifiers.remove ( i ) ;
             tau1Types.remove ( i ) ;
             tau2Identifiers.remove ( j ) ;
             tau2Types.remove ( j ) ;
             break ;
           }
         }
       }
       // Remaining RowType
       MonoType tau1RemainingRow = tau1.getRemainingRowType ( ) ;
       MonoType tau2RemainingRow = tau2.getRemainingRowType ( ) ;
       // First and second remaining RowTypes
       if ( ( tau1RemainingRow != null ) && ( tau2RemainingRow != null ) )
       {
         if ( ( tau1Identifiers.size ( ) > 0 )
             || ( tau2Identifiers.size ( ) > 0 ) )
         {
           throw new RuntimeException ( MessageFormat.format ( Messages
               .getString ( "UnificationException.3" ) , left , right ) ) ; //$NON-NLS-1$
         }
         SeenTypes < TypeEquationTypeInference > seenTypes = eqn.getSeenTypes ( )
             .clone ( ) ;
         seenTypes.add ( eqn ) ;
         context.addEquation ( new TypeEquationTypeInference ( tau1RemainingRow ,
             tau2RemainingRow , seenTypes ) ) ;
         return ;
       }
       // First remaining RowType
       if ( tau1RemainingRow != null )
       {
         if ( tau2Identifiers.size ( ) > 0 )
         {
           Identifier [ ] newIdentifiers = new Identifier [ tau2Identifiers
               .size ( ) ] ;
           MonoType [ ] newTypes = new MonoType [ tau2Types.size ( ) ] ;
           for ( int i = tau2Identifiers.size ( ) - 1 ; i >= 0 ; i -- )
           {
             newIdentifiers [ i ] = tau2Identifiers.get ( i ) ;
             newTypes [ i ] = tau2Types.get ( i ) ;
             tau2Identifiers.remove ( i ) ;
             tau2Types.remove ( i ) ;
           }
           RowType newRowType = new RowType ( newIdentifiers , newTypes ) ;
           SeenTypes < TypeEquationTypeInference > seenTypes = eqn
               .getSeenTypes ( ).clone ( ) ;
           seenTypes.add ( eqn ) ;
           context.addEquation ( new TypeEquationTypeInference (
               tau1RemainingRow , newRowType , seenTypes ) ) ;
         }
       }
       // Second remaining RowType
       if ( tau2RemainingRow != null )
       {
         if ( tau1Identifiers.size ( ) > 0 )
         {
           Identifier [ ] newIdentifiers = new Identifier [ tau1Identifiers
               .size ( ) ] ;
           MonoType [ ] newTypes = new MonoType [ tau1Types.size ( ) ] ;
           for ( int i = tau1Identifiers.size ( ) - 1 ; i >= 0 ; i -- )
           {
             newIdentifiers [ i ] = tau1Identifiers.get ( i ) ;
             newTypes [ i ] = tau1Types.get ( i ) ;
             tau1Identifiers.remove ( i ) ;
             tau1Types.remove ( i ) ;
           }
           RowType newRowType = new RowType ( newIdentifiers , newTypes ) ;
           SeenTypes < TypeEquationTypeInference > seenTypes = eqn
               .getSeenTypes ( ).clone ( ) ;
           seenTypes.add ( eqn ) ;
           context.addEquation ( new TypeEquationTypeInference ( newRowType ,
               tau2RemainingRow , seenTypes ) ) ;
         }
       }
       if ( ( tau1Identifiers.size ( ) > 0 ) || ( tau2Identifiers.size ( ) > 0 ) )
       {
         throw new RuntimeException ( MessageFormat.format ( Messages
             .getString ( "UnificationException.3" ) , left , right ) ) ; //$NON-NLS-1$
       }
       return ;
     }
     throw new UnifyException ( eqn ) ;
   }
 }
