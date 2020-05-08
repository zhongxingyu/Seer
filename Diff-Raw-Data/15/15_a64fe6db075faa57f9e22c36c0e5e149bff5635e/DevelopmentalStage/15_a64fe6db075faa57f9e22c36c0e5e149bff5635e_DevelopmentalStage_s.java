 /**
  *
  * $Id: DevelopmentalStage.java,v 1.2 2009-05-20 17:07:20 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.1  2007/05/16 12:28:23  pandyas
  * Added developmental stage evs tree to Therapy when species is Zebrafsih
  *
  * 
  */
 
 package gov.nih.nci.camod.domain;
 
 import gov.nih.nci.camod.util.Duplicatable;
 import gov.nih.nci.camod.util.EvsTreeUtil;
 import gov.nih.nci.camod.util.HashCodeUtil;
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author pandyas
  */
 public class DevelopmentalStage extends BaseObject implements Comparable, Serializable, Duplicatable{
 
     private static final long serialVersionUID = 3259095453799404851L;
 
     private String name;
     private String conceptCode;
     private Set<Therapy> therapyCollection = new HashSet<Therapy>();
 
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
      * @return Returns the conceptCode.
      */
     public String getConceptCode()
     {
         return conceptCode;
     }
 
     /**
      * @param conceptCode
      *            The conceptCode to set.
      */
     public void setConceptCode(String conceptCode)
     {
         this.conceptCode = conceptCode;
     }
 
     /**
      * @return Returns the therapyCollection.
      */
     public Set<Therapy> getTherapyCollection()
     {
         return therapyCollection;
     }
 
     /**
      * @param therapyCollection
      *            The therapyCollection to set.
      */
     public void setTherapyCollection(Set<Therapy> therapyCollection)
     {
         this.therapyCollection = therapyCollection;
     }
 
     public void addTherapy(Therapy therapy)
     {
     	therapyCollection.add(therapy);
     }
     
     /**
      * @return Returns the EVS Preferred displayName
      */
     public String getEVSPreferredDescription()
     {
       String thePreferedDesc = null;

      	if(EvsTreeUtil.getConceptDetails(null, conceptCode) != null & EvsTreeUtil.getConceptDetails(null, conceptCode).length() > 0){
      		System.out.println("Developmental Stage retrieved from EVS: " + thePreferedDesc);
       		thePreferedDesc = EvsTreeUtil.getConceptDetails(null, conceptCode);
       	} else {
        		System.out.println("Developmental Stage retrieved from caMOD: " + thePreferedDesc);
       		thePreferedDesc = name;	
        	}
         return thePreferedDesc;        
     }    
 
     /**
      * @see java.lang.Object#toString()
      */
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
         final DevelopmentalStage obj = (DevelopmentalStage) o;
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
         // compare by evs description name if possible, otherwise DevelopmentalStage name
         if ((o instanceof DevelopmentalStage) && (this.getEVSPreferredDescription() != null) && (((DevelopmentalStage) o).getEVSPreferredDescription() != null))
         {
             int result = this.getEVSPreferredDescription().compareTo(((DevelopmentalStage) o).getEVSPreferredDescription());
             if (result != 0)
             {
                 return result;
             }
         }
         else if ((o instanceof DevelopmentalStage) && (this.getName() != null) && (((DevelopmentalStage) o).getName() != null))
         {
             int result = this.getName().compareTo(((DevelopmentalStage) o).getName());
             if (result != 0)
             {
                 return result;
             }
         }
 
         return super.compareTo(o);
     }	
 }
