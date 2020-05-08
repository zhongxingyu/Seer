 /*
  * $Id: Organ.java,v 1.21 2009-06-04 18:49:09 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.20  2009/06/04 16:59:08  pandyas
  * getting ready for QA build
  *
  * Revision 1.19  2009/06/04 15:03:07  pandyas
  * testing preferred description in new methods
  *
  * Revision 1.18  2009/05/20 17:07:20  pandyas
  * modified for gforge #17325 Upgrade caMOD to use caBIO 4.x and EVS 4.x to get data
  *
  * Revision 1.17  2006/10/31 17:55:24  pandyas
  * Organ returns the EVS Preferred displayName
  *  if the conceptCode = 000000, then return the name just like disease.  This is needed for JAX data from MTB.
  *
  * Revision 1.16  2006/04/17 19:13:46  pandyas
  * caMod 2.1 OM changes and added log/id header
  *
  */
 package gov.nih.nci.camod.domain;
 
 import gov.nih.nci.camod.util.EvsTreeUtil;
 
 import java.io.Serializable;
 import gov.nih.nci.camod.util.Duplicatable;
 import gov.nih.nci.camod.util.HashCodeUtil;
 
 public class Organ extends BaseObject implements Comparable, Serializable, Duplicatable
 {
     private static final long serialVersionUID = 3259095453799404851L;
 
     private String name;
     private String conceptCode;
 
 
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
      * @return Returns the EVS Preferred displayName
      *      if the conceptCode = 000000, then return the name
      *   	Added code to get the name when EVS is down     *      
      */
     public String getEVSPreferredDescription()
     {
         String thePreferedDesc = null;
         if ("000000".equals(conceptCode) || "C000000".equals(conceptCode))
         {
             thePreferedDesc = name;
         }
         else
         {
        	String conceptDetails = EvsTreeUtil.getConceptDetails(null, conceptCode);
        	if(conceptDetails != null && conceptDetails.length() > 0){        		
        		thePreferedDesc = conceptDetails;
         		System.out.println("Organ retrieved from EVS: " + thePreferedDesc);
        	} else {        		
         		thePreferedDesc = name;	
        		System.out.println("Organ retrieved from caMOD: " + thePreferedDesc);
         	}
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
         final Organ obj = (Organ) o;
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
         // compare by evs description name if possible, otherwise organ name
         if ((o instanceof Organ) && (this.getEVSPreferredDescription() != null) && (((Organ) o).getEVSPreferredDescription() != null))
         {
             int result = this.getEVSPreferredDescription().compareTo(((Organ) o).getEVSPreferredDescription());
             if (result != 0)
             {
                 return result;
             }
         }
         else if ((o instanceof Organ) && (this.getName() != null) && (((Organ) o).getName() != null))
         {
             int result = this.getName().compareTo(((Organ) o).getName());
             if (result != 0)
             {
                 return result;
             }
         }
 
         return super.compareTo(o);
     }
 }
