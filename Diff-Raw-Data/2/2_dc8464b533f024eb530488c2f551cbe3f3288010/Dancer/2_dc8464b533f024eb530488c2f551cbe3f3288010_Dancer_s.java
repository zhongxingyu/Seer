 package net.cscott.sdr.calls;
 
 import net.cscott.sdr.calls.TaggedFormation.Tag;
 
 /** Dancer objects represent a dancer (real or phantom).  Equality is
  *  object identity.  There are eight canonical 'real' dancers, and
  *  an unlimited number of phantoms. */
 public interface Dancer {
     public boolean isHead();
     public boolean isSide();
     public boolean isBoy();
     public boolean isGirl();
    /** Certain dancer {@Tag}s are inherent to a dancer,
      *  for example 'DANCER_1', 'COUPLE_2', 'BOY' etc.
      *  This method identifies if one of these tags
      *  is appropriate for the current {@link Dancer}.
      */
     public boolean matchesTag(Tag tag);
     /** Returns the most-specific primitive tag for the
      * given dancer, or 'null' if the dancer is a phantom. */
     public Tag primitiveTag();
 }
