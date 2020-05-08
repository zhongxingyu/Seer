 /*
 * $Id: EnvironmentalFactor.java,v 1.15 2007-10-31 15:33:31 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.14  2006/05/04 15:42:03  pandyas
  * Modified/Added to support Morpholino object data in the application
  *
  * Revision 1.13  2006/04/17 19:13:46  pandyas
  * caMod 2.1 OM changes and added log/id header
  *
  */
 package gov.nih.nci.camod.domain;
 
 import gov.nih.nci.camod.util.Duplicatable;
 import gov.nih.nci.camod.util.HashCodeUtil;
 
 import java.io.Serializable;
 
 /**
  * @author rajputs
  */
 public class EnvironmentalFactor extends BaseObject implements Comparable, Serializable, Duplicatable
 {
     private static final long serialVersionUID = 3259445453799404851L;
 
     private String type;
     private String typeAlternEntry;
     private String name;
     private String nameAlternEntry;
     private String casNumber;
     private Long nscNumber;
    private String comments;    
     
 
     /**
      * @return Returns the type.
      */
     public String getType()
     {
         return type;
     }
 
     /**
      * @param type
      *            The type to set.
      */
     public void setType(String type)
     {
         this.type = type;
     }
     
 	/**
 	 * @return the nameAlternEntry
 	 */
 	public String getNameAlternEntry() {
 		return nameAlternEntry;
 	}
 
 	/**
 	 * @param nameAlternEntry the nameAlternEntry to set
 	 */
 	public void setNameAlternEntry(String nameAlternEntry) {
 		this.nameAlternEntry = nameAlternEntry;
 	}    
 
     /**
      * @return Returns the display name.
      */
     public String getDisplayName()
     {
         String theDisplayName = name;
         if (theDisplayName == null && nameAlternEntry != null)
         {
             theDisplayName = "Other - " + nameAlternEntry;
         }
 
         return theDisplayName;
     }
     
     /**
      * @return Returns the display name for Induced Mutation.
      * name is saved in uncontrolledVocab since it is free-text.
      */
     public String getDisplayNameIM()
     {
         String theDisplayName = nameAlternEntry;
 
         return theDisplayName;
     }    
 
     /**
      * @return Returns the name.
      */
     public String getName()
     {
         return name;
     }
 
     /**
      * @param name
      *            The name to set.
      */
     public void setName(String name)
     {
         this.name = name;
     }
 
 
     /**
      * @return Returns the casNumber.
      */
     public String getCasNumber()
     {
         return casNumber;
     }
 
     /**
      * @param casNumber
      *            The casNumber to set.
      */
     public void setCasNumber(String casNumber)
     {
         this.casNumber = casNumber;
     }
     /**
      * @return Returns the nscNumber.
      */
     public Long getNscNumber() {
         return nscNumber;
     }
 
     /**
      * @param nscNumber
      *            The nscNumber to set.
      */
     public void setNscNumber(Long nscNumber) {
         this.nscNumber = nscNumber;
     }
     
     /**
      * @return Returns the comments. Comment is a reserved word so 
      * we must use the plural form comments.
      */
     public String getComments() {
         return comments;
     }
 
     /**
      * @param comments
      *            The comments to set.
      */
     public void setComments(String comments) {
         this.comments = comments;
     }    
 
     public String toString()
     {
         String result = super.toString() + " - ";
         result += this.getName();
         return result;
     }
 
     public boolean equals(Object o)
     {
         if (!super.equals(o))
             return false;
         if (!(this.getClass().isInstance(o)))
             return false;
         final EnvironmentalFactor obj = (EnvironmentalFactor) o;
         if (HashCodeUtil.notEqual(this.getName(), obj.getName()))
             return false;
         return true;
     }
 
     public int hashCode()
     {
         int result = HashCodeUtil.SEED;
         result = HashCodeUtil.hash(result, this.getName());
         return result + super.hashCode();
     }
 
     public int compareTo(Object o)
     {
         if ((o instanceof EnvironmentalFactor) && (this.getName() != null) && (((EnvironmentalFactor) o).getName() != null))
         {
             int result = this.getName().compareTo(((EnvironmentalFactor) o).getName());
             if (result != 0)
             {
                 return result;
             }
         }
 
         return super.compareTo(o);
     }
 
 	/**
 	 * @return the typeAlternEntry
 	 */
 	public String getTypeAlternEntry() {
 		return typeAlternEntry;
 	}
 
 	/**
 	 * @param typeAlternEntry the typeAlternEntry to set
 	 */
 	public void setTypeAlternEntry(String typeAlternEntry) {
 		this.typeAlternEntry = typeAlternEntry;
 	}
 
 
 
 }
