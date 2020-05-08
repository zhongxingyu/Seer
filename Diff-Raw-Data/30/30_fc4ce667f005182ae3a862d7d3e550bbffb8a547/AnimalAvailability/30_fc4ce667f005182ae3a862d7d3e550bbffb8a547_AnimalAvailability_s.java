 /*
  * $Log: not supported by cvs2svn $
  * Revision 1.13  2006/10/17 16:14:36  pandyas
  * modified during development of caMOD 2.2 - various
  *
  * Revision 1.12  2006/04/17 19:13:46  pandyas
  * caMod 2.1 OM changes and added log/id header
  *
  * Revision 1.11  2006/01/18 14:23:31  georgeda
  * TT# 376 - Updated to use new Java 1.5 features
  *
  * Revision 1.10  2005/12/08 21:44:23  georgeda
  * Defect #259; handle PI availability for 2-tier data
  *
  * Revision 1.9  2005/11/18 22:50:02  georgeda
  * Defect #184.  Cleanup editing of old models
  *
  * Revision 1.8  2005/11/17 18:36:47  georgeda
  * Defect #57, add a mailto link for Investigator availabilty
  *
  * Revision 1.7  2005/11/14 14:16:51  georgeda
  * Cleanup
  *
  * 
 * $Id: AnimalAvailability.java,v 1.14 2007-09-12 19:35:52 pandyas Exp $
  */
 package gov.nih.nci.camod.domain;
 
 import gov.nih.nci.camod.service.impl.PersonManagerSingleton;
 import gov.nih.nci.camod.util.Duplicatable;
 import gov.nih.nci.camod.util.HashCodeUtil;
 import java.io.Serializable;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class AnimalAvailability extends BaseObject implements Comparable, Serializable, Duplicatable
 {
 
     private static final long serialVersionUID = 4259705453799404851L;
     protected transient final Log log = LogFactory.getLog(AnimalAvailability.class);
 
     private String name;
     private String stockNumber;
     private AbstractCancerModel cancerModel;
     private AnimalDistributor animalDistributor;
 
 
     /**
      * @return Returns the display name.
      */
     public String getDisplayName()
     {
 
         String theDisplayName = name;
         if (theDisplayName == null)
         {
             theDisplayName = "Strain";
         }
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
      * @return Returns the stockNumber.
      */
     public String getStockNumber()
     {
         return stockNumber;
     }
 
     /**
      * @param stockNumber
      *            The stockNumber to set.
      */
     public void setStockNumber(String stockNumber)
     {
         this.stockNumber = stockNumber;
     }
 
     public Person getPrincipalInvestigator()
     {
         Person thePerson = null;
 
         // Hack to work around old ca-mod data. -1 indicates that this comes
         // from the 2-tier.
         if (stockNumber != null && !"-1".equals(stockNumber))
         {
 
             try
             {
             	log.debug("Unable to get Person for thePerson.getUsername(): " + thePerson.getUsername());
                 thePerson = PersonManagerSingleton.instance().get(stockNumber);
             }
             catch (Exception e)
             {
                 log.debug("Unable to get Person for stock number");
             }
         }
 
         return thePerson;
     }

     /**
      * @return Returns the cancerModel.
      */
     public AbstractCancerModel getCancerModel()
     {
         return cancerModel;
     }
 
     /**
      * @param cancerModel
      *            The cancerModel to set.
      */
     public void setCancerModel(AbstractCancerModel cancerModel)
     {
         this.cancerModel = cancerModel;
     }
 
     /**
      * @return Returns the animalDistributor.
      */
     public AnimalDistributor getAnimalDistributor()
     {
         return animalDistributor;
     }
 
     /**
      * @param animalDistributor
      *            The animalDistributor to set.
      */
     public void setAnimalDistributor(AnimalDistributor animalDistributor)
     {
         this.animalDistributor = animalDistributor;
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
         final AnimalAvailability obj = (AnimalAvailability) o;
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
         if ((o instanceof AnimalAvailability) && (this.getName() != null) && (((AnimalAvailability) o).getName() != null))
         {
             int result = this.getName().compareTo(((AnimalAvailability) o).getName());
             if (result != 0)
             {
                 return result;
             }
         }
 
         return super.compareTo(o);
     }
 
 }
