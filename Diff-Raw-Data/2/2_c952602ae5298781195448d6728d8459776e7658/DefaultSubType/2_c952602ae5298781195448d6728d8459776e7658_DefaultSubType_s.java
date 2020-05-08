 package de.unisiegen.tpml.core.subtypingrec ;
 
 
 import java.util.TreeSet ;
 import de.unisiegen.tpml.core.latex.DefaultLatexCommand ;
 import de.unisiegen.tpml.core.latex.LatexCommand ;
 import de.unisiegen.tpml.core.latex.LatexCommandNames ;
 import de.unisiegen.tpml.core.latex.LatexInstruction ;
 import de.unisiegen.tpml.core.latex.LatexPackage ;
 import de.unisiegen.tpml.core.latex.LatexPrintable ;
 import de.unisiegen.tpml.core.latex.LatexString ;
 import de.unisiegen.tpml.core.latex.LatexStringBuilder ;
 import de.unisiegen.tpml.core.latex.LatexStringBuilderFactory ;
 import de.unisiegen.tpml.core.prettyprinter.PrettyPrintable ;
 import de.unisiegen.tpml.core.prettyprinter.PrettyString ;
 import de.unisiegen.tpml.core.prettyprinter.PrettyStringBuilder ;
 import de.unisiegen.tpml.core.prettyprinter.PrettyStringBuilderFactory ;
 import de.unisiegen.tpml.core.types.MonoType ;
 
 
 /**
  * The Default Subtype is needed for the subtyping algorithm. This object
  * contains a left and an right.
  * 
  * @author Benjamin Mies
  * @author Christian Fehler
  */
 public class DefaultSubType implements PrettyPrintable , LatexPrintable ,
     LatexCommandNames
 {
   /**
    * The left type (subtype) of this subtype object
    */
   private MonoType left ;
 
 
   /**
    * The right type (supertype) of this subtype object
    */
   private MonoType right ;
 
 
   /**
    * Allocates a new default left with the given types.
    * 
    * @param pLeft the left of this object
    * @param pRight the right of this object
    */
   public DefaultSubType ( MonoType pLeft , MonoType pRight )
   {
     this.left = pLeft ;
     this.right = pRight ;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @ Override
   public boolean equals ( Object pObject )
   {
     if ( pObject instanceof DefaultSubType )
     {
       DefaultSubType other = ( DefaultSubType ) pObject ;
       return ( this.left.equals ( other.left ) && this.right
           .equals ( other.right ) ) ;
     }
     return false ;
   }
 
 
   /**
    * Returns a set of needed latex commands for this latex printable object.
    * 
    * @return A set of needed latex commands for this latex printable object.
    */
   public TreeSet < LatexCommand > getLatexCommands ( )
   {
     TreeSet < LatexCommand > commands = new TreeSet < LatexCommand > ( ) ;
     commands.add ( new DefaultLatexCommand ( LATEX_SUB_TYPE , 2 ,
         "#1\\ <:\\ #2" , "tau1" , "tau2" ) ) ; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
     for ( LatexCommand command : this.left.getLatexCommands ( ) )
     {
       commands.add ( command ) ;
     }
     for ( LatexCommand command : this.right.getLatexCommands ( ) )
     {
       commands.add ( command ) ;
     }
     return commands ;
   }
 
 
   /**
    * Returns a set of needed latex instructions for this latex printable object.
    * 
    * @return A set of needed latex instructions for this latex printable object.
    */
   public TreeSet < LatexInstruction > getLatexInstructions ( )
   {
     TreeSet < LatexInstruction > instructions = new TreeSet < LatexInstruction > ( ) ;
     for ( LatexInstruction instruction : this.left.getLatexInstructions ( ) )
     {
       instructions.add ( instruction ) ;
     }
     for ( LatexInstruction instruction : this.right.getLatexInstructions ( ) )
     {
       instructions.add ( instruction ) ;
     }
     return instructions ;
   }
 
 
   /**
    * Returns a set of needed latex packages for this latex printable object.
    * 
    * @return A set of needed latex packages for this latex printable object.
    */
   public TreeSet < LatexPackage > getLatexPackages ( )
   {
     TreeSet < LatexPackage > packages = new TreeSet < LatexPackage > ( ) ;
     for ( LatexPackage pack : this.left.getLatexPackages ( ) )
     {
       packages.add ( pack ) ;
     }
     for ( LatexPackage pack : this.right.getLatexPackages ( ) )
     {
       packages.add ( pack ) ;
     }
     return packages ;
   }
 
 
   /**
    * Returns the left type of this object
    * 
    * @return the left type (subtype) of this object
    */
   public MonoType getLeft ( )
   {
     return this.left ;
   }
 
 
   /**
    * Returns the right type of this object
    * 
    * @return the right (type) overtype of this object
    */
   public MonoType getRight ( )
   {
     return this.right ;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @ Override
   public int hashCode ( )
   {
     return this.left.hashCode ( ) + this.right.hashCode ( ) ;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see LatexPrintable#toLatexString()
    */
   public final LatexString toLatexString ( )
   {
     return toLatexStringBuilder ( LatexStringBuilderFactory.newInstance ( ) )
         .toLatexString ( ) ;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see LatexPrintable#toLatexStringBuilder(LatexStringBuilderFactory)
    */
   public final LatexStringBuilder toLatexStringBuilder (
       LatexStringBuilderFactory pLatexStringBuilderFactory )
   {
     LatexStringBuilder builder = pLatexStringBuilderFactory.newBuilder ( this ,
         0 , LATEX_SUB_TYPE ) ;
     builder.addBuilder ( this.left
         .toLatexStringBuilder ( pLatexStringBuilderFactory ) , 0 ) ;
     builder.addBuilder ( this.right
         .toLatexStringBuilder ( pLatexStringBuilderFactory ) , 0 ) ;
     return builder ;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see de.unisiegen.tpml.core.prettyprinter.PrettyPrintable#toPrettyString()
    */
   public final PrettyString toPrettyString ( )
   {
     return toPrettyStringBuilder ( PrettyStringBuilderFactory.newInstance ( ) )
         .toPrettyString ( ) ;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see PrettyPrintable#toPrettyStringBuilder(PrettyStringBuilderFactory)
    */
   public PrettyStringBuilder toPrettyStringBuilder (
       PrettyStringBuilderFactory pPrettyStringBuilderFactory )
   {
     PrettyStringBuilder builder = pPrettyStringBuilderFactory.newBuilder (
         this , 0 ) ;
     builder.addBuilder ( this.right
         .toPrettyStringBuilder ( pPrettyStringBuilderFactory ) , 0 ) ;
    builder.addText ( " = " ) ; //$NON-NLS-1$
     builder.addBuilder ( this.left
         .toPrettyStringBuilder ( pPrettyStringBuilderFactory ) , 0 ) ;
     return builder ;
   }
 
 
   /**
    * {@inheritDoc} Mainly useful for debugging purposes.
    * 
    * @see java.lang.Object#toString()
    */
   @ Override
   public String toString ( )
   {
     final StringBuilder builder = new StringBuilder ( ) ;
     builder.append ( this.left ) ;
     builder.append ( "<b><font color=\"#FF0000\">" ) ; //$NON-NLS-1$
     builder.append ( " &#60: " ) ; //$NON-NLS-1$
     builder.append ( "</font></b>" ) ; //$NON-NLS-1$
     builder.append ( this.right ) ;
     return builder.toString ( ) ;
   }
 }
