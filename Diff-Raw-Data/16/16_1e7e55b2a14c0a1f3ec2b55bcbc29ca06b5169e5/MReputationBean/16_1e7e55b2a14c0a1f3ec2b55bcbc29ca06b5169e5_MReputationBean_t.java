 package com.mymed.model.data.reputation;
 
 import com.mymed.model.data.AbstractMBean;
 
 /**
  * @author lvanni
  */
 public final class MReputationBean extends AbstractMBean {
 
   /**
    * Used for the hash code
    */
   private static final int PRIME = 31;
 
   /**
    * The value of the reputation
    */
   private double value;
 
   /**
    * The number of interaction
    */
   private int nbInteraction;
 
   /**
    * The raterList ID (userList)
    */
   private String raterList;
 
   /**
    * The raterList ID (userList)
    */
   private String interactionList;
 
   /**
    * Create an empty MReputationBean
    */
   public MReputationBean() {
     // Empty constructor, needed because of the copy constructor
     super();
   }
 
   /**
    * Copy constructor
    * <p>
    * Provide a clone of the passed MReputationBean
    * 
    * @param toClone
    *          the MReputationBean to clone
    */
   protected MReputationBean(final MReputationBean toClone) {
     super();
 
     value = toClone.getValue();
     nbInteraction = toClone.getNbInteraction();
     raterList = toClone.getRaterList();
     interactionList = toClone.getInteractionList();
   }
 
   @Override
   public MReputationBean clone() {
     return new MReputationBean(this);
   }
 
   @Override
   public String toString() {
     return "Reputation:\n" + super.toString();
   }
 
   /*
    * (non-Javadoc)
    * @see java.lang.Object#equals()
    */
   @Override
   public boolean equals(final Object object) {
 
     boolean equal = false;
 
     if (this == object) {
       equal = true;
     } else if (object instanceof MReputationBean) {
       final MReputationBean comparable = (MReputationBean) object;
 
       equal = true;
      equal &= value == comparable.getValue();
      equal &= nbInteraction == comparable.getNbInteraction();

      if ((raterList == null) && (comparable.getRaterList() != null)) {
        equal = false;
      } else {
        equal &= raterList.equals(comparable.getRaterList());
      }

      if ((interactionList == null) && (comparable.getRaterList() != null)) {
        equal = false;
      } else {
        equal &= interactionList.equals(comparable.getRaterList());
      }
     }
 
     return equal;
   }
 
   /*
    * (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
     int result = 1;
 
     result = PRIME * result;
     long temp;
     temp = Double.doubleToLongBits(value);
     result = (PRIME * result) + (int) (temp ^ (temp >>> 32));
 
     return result;
   }
 
   @Override
   public void update(final AbstractMBean mBean) {
     // TODO Auto-generated method stub
 
   }
 
   /* --------------------------------------------------------- */
   /* GETTER AND SETTER */
   /* --------------------------------------------------------- */
   /**
    * @return the value
    */
   public double getValue() {
     return value;
   }
 
   /**
    * @param value
    *          the value to set
    */
   public void setValue(final double value) {
     this.value = value;
   }
 
   /**
    * @return
    */
   public int getNbInteraction() {
     return nbInteraction;
   }
 
   /**
    * @param nbInteraction
    */
   public void setNbInteraction(final int nbInteraction) {
     this.nbInteraction = nbInteraction;
   }
 
   /**
    * @return
    */
   public String getRaterList() {
     return raterList;
   }
 
   /**
    * @param raters
    */
   public void setRaterList(final String raters) {
     this.raterList = raters;
   }
 
   /**
    * @return
    */
   public String getInteractionList() {
     return interactionList;
   }
 
   /**
    * @param interactionList
    */
   public void setInteractionList(final String interactionList) {
     this.interactionList = interactionList;
   }
 }
