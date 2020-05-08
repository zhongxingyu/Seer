 package de.unisiegen.tpml.core.expressions ;
 
 
 import java.util.ArrayList ;
 import de.unisiegen.tpml.core.prettyprinter.PrettyStringBuilder ;
 import de.unisiegen.tpml.core.prettyprinter.PrettyStringBuilderFactory ;
 
 
 /**
  * Represents an identifier in the expression hierarchy. Identifiers are values
  * in the semantics of the various languages.
  * 
  * @author Benedikt Meurer
  * @author Christian Fehler
  * @version $Rev:1056 $
  * @see de.unisiegen.tpml.core.expressions.Value
  */
 public class Identifier extends Value
 {
   /**
    * The {@link Expression} in which this {@link Identifier} is bounded.
    * 
    * @see #getBoundedToExpression()
    * @see #setBoundedToExpression(Expression)
    */
   private Expression boundedToExpression ;
 
 
   /**
    * The {@link Identifier} to which this {@link Identifier} is bounded.
    * 
    * @see #getBoundedToIdentifier()
    * @see #setBoundedToIdentifier(Identifier)
    */
   private Identifier boundedToIdentifier ;
 
 
   /**
    * The name of the {@link Identifier}.
    * 
    * @see #getName()
    */
   private String name ;
 
 
   /**
    * Allocates a new {@link Identifier} with the given <code>name</code>.
    * 
    * @param pName the name of the identifier.
    */
   public Identifier ( String pName )
   {
     this.name = pName ;
     this.boundedToExpression = null ;
     this.boundedToIdentifier = null ;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Expression#clone()
    */
   @ Override
   public Identifier clone ( )
   {
     return new Identifier ( this.name ) ;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Expression#equals(Object)
    */
   @ Override
   public boolean equals ( Object obj )
   {
     if ( obj instanceof Identifier )
     {
       Identifier other = ( Identifier ) obj ;
       return this.name.equals ( other.name ) ;
     }
     return false ;
   }
 
 
   /**
    * Returns a set that contains exacty one element, which is the name of the
    * identifier.
    * 
    * @return a set which contains the name of the identifier.
    * @see #getName()
    * @see Expression#free()
    */
   @ Override
   public ArrayList < Identifier > free ( )
   {
     if ( this.free == null )
     {
       this.free = new ArrayList < Identifier > ( ) ;
       this.free.add ( this ) ;
     }
     return this.free ;
   }
 
 
   /**
    * Returns the {@link Expression} in which this {@link Identifier} is bounded.
    * 
    * @return The {@link Expression} in which this {@link Identifier} is bounded.
    * @see #boundedToExpression
    * @see #setBoundedToExpression(Expression)
    */
   public Expression getBoundedToExpression ( )
   {
     return this.boundedToExpression ;
   }
 
 
   /**
    * Returns the {@link Identifier} to which this {@link Identifier} is bounded.
    * 
    * @return The {@link Identifier} to which this {@link Identifier} is bounded.
    * @see #boundedToIdentifier
    * @see #setBoundedToIdentifier(Identifier)
    */
   public Identifier getBoundedToIdentifier ( )
   {
     return this.boundedToIdentifier ;
   }
 
 
   /**
    * {@inheritDoc}
    */
   @ Override
   public String getCaption ( )
   {
     return "Identifier" ; //$NON-NLS-1$
   }
 
 
   /**
    * Returns the name of the identifier.
    * 
    * @return the name of the identifier.
    */
   public String getName ( )
   {
     return this.name ;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Expression#hashCode()
    */
   @ Override
   public int hashCode ( )
   {
     return this.name.hashCode ( ) ;
   }
 
 
   /**
    * Sets the {@link Expression} in which this {@link Identifier} is bounded.
    * 
    * @param pBoundedToExpression The {@link Expression} in which this
    *          {@link Identifier} is bounded.
    * @see #boundedToExpression
    * @see #getBoundedToExpression()
    */
   public void setBoundedToExpression ( Expression pBoundedToExpression )
   {
     if ( this.boundedToExpression != null )
     {
      System.err
          .println ( "Identifier: Programming error! Please contact Christian Fehler!" ) ;//$NON-NLS-1$
      System.err
          .println ( "An Identifier can not be bounded to more than one Expression!" ) ; //$NON-NLS-1$
      System.err.println ( "Identifier: " + this.name ) ; //$NON-NLS-1$
      System.err.println ( "Old boundedToExpression: " //$NON-NLS-1$
          + this.boundedToExpression ) ;
      System.err.println ( "New boundedToExpression: " + pBoundedToExpression ) ; //$NON-NLS-1$
     }
     this.boundedToExpression = pBoundedToExpression ;
   }
 
 
   /**
    * Sets the {@link Identifier} to which this {@link Identifier} is bounded.
    * 
    * @param pBoundedToIdentifier The {@link Identifier} to which this
    *          {@link Identifier} is bounded.
    * @see #boundedToIdentifier
    * @see #getBoundedToIdentifier()
    */
   public void setBoundedToIdentifier ( Identifier pBoundedToIdentifier )
   {
     this.boundedToIdentifier = pBoundedToIdentifier ;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Expression#substitute(Identifier, Expression, boolean)
    */
   @ Override
   public Expression substitute ( Identifier pId , Expression pExpression )
   {
     return substitute ( pId , pExpression , false ) ;
   }
 
 
   /**
    * Returns <code>e</code> if <code>id</code> equals the name of the
    * identifier. Else the identifier itself is returned.
    * 
    * @return <code>e</code> if <code>id</code> equals the name of the
    *         identifier, else the identifier itself.
    * @see #getName()
    */
   @ Override
   public Expression substitute ( Identifier pId , Expression pExpression ,
       @ SuppressWarnings ( "unused" )
       boolean pAttributeRename )
   {
     if ( pId.equals ( this ) )
     {
       /*
        * We need to clone the expression here to make sure we can distinguish an
        * expression in the pretty printer that is substituted multiple times
        */
       return pExpression.clone ( ) ;
     }
     return this.clone ( ) ;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Expression#toPrettyStringBuilder(PrettyStringBuilderFactory)
    */
   @ Override
   public PrettyStringBuilder toPrettyStringBuilder (
       PrettyStringBuilderFactory factory )
   {
     if ( this.prettyStringBuilder == null )
     {
       this.prettyStringBuilder = factory.newBuilder ( this , PRIO_IDENTIFIER ) ;
       this.prettyStringBuilder.addText ( this.name ) ;
     }
     return this.prettyStringBuilder ;
   }
 }
