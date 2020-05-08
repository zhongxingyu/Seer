 package de.unisiegen.gtitool.core.entities;
 
 
 import java.util.HashSet;
 
 import de.unisiegen.gtitool.core.exceptions.state.StateException;
 
 
 /**
 * A default state that contains positions and can be marked
  */
 public class DefaultPositionState extends DefaultState
 {
 
   /**
   * The serial version uid.
    */
   private static final long serialVersionUID = -7451051647706513194L;
 
 
   /**
   * The positions
    */
   private HashSet < Integer > positions;
 
 
   /**
   * The mark flag
    */
   private boolean mark;
 
 
   /**
   * Creates a new {@link DefaultPositionState}
    * 
   * @param name The name of the State
   * @param positions The positions
   * @throws StateException is thrown when state cannot be created
    */
   public DefaultPositionState ( String name, HashSet < Integer > positions )
       throws StateException
   {
     super ( "{" + name + "}" ); //$NON-NLS-1$ //$NON-NLS-2$
     this.positions = positions;
     this.mark = false;
   }
 
 
   /**
    * Returns the positions.
    * 
    * @return The positions.
    * @see #positions
    */
   public HashSet < Integer > getPositions ()
   {
     return this.positions;
   }
 
 
   /**
    * Returns the mark.
    * 
    * @return The mark.
    * @see #mark
    */
   public boolean isMarked ()
   {
     return this.mark;
   }
 
 
   /**
    * Unmarks the State
    */
   public void unMark ()
   {
     this.mark = false;
   }
 
 
   /**
    * Marks the State
    */
   public void mark ()
   {
     this.mark = true;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see DefaultState#equals(java.lang.Object)
    */
   @Override
   public boolean equals ( Object other )
   {
     if ( other instanceof DefaultPositionState )
     {
       DefaultPositionState positionState = ( DefaultPositionState ) other;
       return this.positions.equals ( positionState.getPositions () );
     }
     return false;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see DefaultState#hashCode()
    */
   @Override
   public int hashCode ()
   {
     int i = 0;
     for ( Integer n : this.positions )
     {
       i += n.hashCode () * n.hashCode ();
     }
     return i;
   }
 
 }
