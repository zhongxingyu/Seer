 /*
  * Created on May 3, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package gov.nih.nci.camod.domain;
 
import java.util.*;

 import gov.nih.nci.camod.util.HashCodeUtil;
 
 /**
  * @author rajputs
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class Xenograft extends AbstractCancerModel implements Comparable {
     
     private static final long serialVersionUID = 3257445453799404851L;
     
 	private String administrativeSite;
 	private String geneticManipulation;
 	private String modificationDescription;
 	private String parentalCellLineName;
 	private String name;
 	private String atccNumber;
 	private String cellAmount;
 	private Date harvestDate;
 	private String graftType;
 	private String graftTypeUnctrlVocab;
 	private TumorCode tumorCode;
 	private List invivoResultCollection = new ArrayList();
 	private Taxon originSpecies;
 	private Taxon hostSpecies;
 	private Organ organ;
 	
 	/**
 	 * @return Returns the organ.
 	 */
 	public Organ getOrgan() {
 		return organ;
 	}
 	/**
 	 * @param organ The organ to set.
 	 */
 	public void setOrgan(Organ organ) {
 		this.organ = organ;
 	}
 	/**
 	 * @return Returns the hostSpecies.
 	 */
 	public Taxon getHostSpecies() {
 		return hostSpecies;
 	}
 	/**
 	 * @param hostSpecies The hostSpecies to set.
 	 */
 	public void setHostSpecies(Taxon hostSpecies) {
 		this.hostSpecies = hostSpecies;
 	}
 	/**
 	 * @return Returns the originSpecies.
 	 */
 	public Taxon getOriginSpecies() {
 		return originSpecies;
 	}
 	/**
 	 * @param originSpecies The originSpecies to set.
 	 */
 	public void setOriginSpecies(Taxon originSpecies) {
 		this.originSpecies = originSpecies;
 	}
 	/**
 	 * @return Returns the atccNumber.
 	 */
 	public String getAtccNumber() {
 		return atccNumber;
 	}
 	/**
 	 * @param atccNumber The atccNumber to set.
 	 */
 	public void setAtccNumber(String atccNumber) {
 		this.atccNumber = atccNumber;
 	}
 	/**
 	 * @return Returns the cellAmount.
 	 */
 	public String getCellAmount() {
 		return cellAmount;
 	}
 	/**
 	 * @param cellAmount The cellAmount to set.
 	 */
 	public void setCellAmount(String cellAmount) {
 		this.cellAmount = cellAmount;
 	}
 	/**
 	 * @return Returns the graftType.
 	 */
 	public String getGraftType() {
 		return graftType;
 	}
 	/**
 	 * @param graftType The graftType to set.
 	 */
 	public void setGraftType(String graftType) {
 		this.graftType = graftType;
 	}
 	/**
 	 * @return Returns the graftTypeUnctrlVocab.
 	 */
 	public String getGraftTypeUnctrlVocab() {
 		return graftTypeUnctrlVocab;
 	}
 	/**
 	 * @param graftTypeUnctrlVocab The graftTypeUnctrlVocab to set.
 	 */
 	public void setGraftTypeUnctrlVocab(String graftTypeUnctrlVocab) {
 		this.graftTypeUnctrlVocab = graftTypeUnctrlVocab;
 	}	
 	/**
 	 * @return Returns the harvestDate.
 	 */
 	public Date getHarvestDate() {
 		return harvestDate;
 	}
 	/**
 	 * @param harvestDate The harvestDate to set.
 	 */
 	public void setHarvestDate(Date harvestDate) {
 		this.harvestDate = harvestDate;
 	}
 	/**
 	 * @return Returns the invivoResultCollection.
 	 */
 	public List getInvivoResultCollection() {
 		return invivoResultCollection;
 	}
 	/**
 	 * @param invivoResultCollection The invivoResultCollection to set.
 	 */
 	public void setInvivoResultCollection(List invivoResultCollection) {
 		this.invivoResultCollection = invivoResultCollection;
 	}
 	public void addInvivoResult(InvivoResult invivoResult) {
 		invivoResultCollection.add(invivoResult);
 	}	
 	/**
 	 * @return Returns the administrativeSite.
 	 */
 	public String getAdministrativeSite() {
 		return administrativeSite;
 	}
 	/**
 	 * @param administrativeSite The administrativeSite to set.
 	 */
 	public void setAdministrativeSite(String administrativeSite) {
 		this.administrativeSite = administrativeSite;
 	}
 	/**
 	 * @return Returns the geneticManipulation.
 	 */
 	public String getGeneticManipulation() {
 		return geneticManipulation;
 	}
 	/**
 	 * @param geneticManipulation The geneticManipulation to set.
 	 */
 	public void setGeneticManipulation(String geneticManipulation) {
 		this.geneticManipulation = geneticManipulation;
 	}	
 	/**
 	 * @return Returns the modificationDescription.
 	 */
 	public String getModificationDescription() {
 		return modificationDescription;
 	}
 	/**
 	 * @param modificationDescription The modificationDescription to set.
 	 */
 	public void setModificationDescription(String modificationDescription) {
 		this.modificationDescription = modificationDescription;
 	}
 	/**
 	 * @return Returns the name.
 	 */
 	public String getName() {
 		return name;
 	}
 	/**
 	 * @param name The name to set.
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	/**
 	 * @return Returns the parentalCellLineName.
 	 */
 	public String getParentalCellLineName() {
 		return parentalCellLineName;
 	}
 	/**
 	 * @param parentalCellLineName The parentalCellLineName to set.
 	 */
 	public void setParentalCellLineName(String parentalCellLineName) {
 		this.parentalCellLineName = parentalCellLineName;
 	}
 	/**
 	 * @return Returns the tumorCode.
 	 */
 	public TumorCode getTumorCode() {
 		return tumorCode;
 	}
 	/**
 	 * @param tumorCode The tumorCode to set.
 	 */
 	public void setTumorCode(TumorCode tumorCode) {
 		this.tumorCode = tumorCode;
 	}
    /**
      * @see java.lang.Object#toString()
      */
      public String toString() {
        String result = super.toString() + " - ";      
        result += this.getName();
        return result;
      }    
         
     public boolean equals(Object o) {
       if (!super.equals(o)) return false;            
       if (!(this.getClass().isInstance(o))) return false; 
       final Xenograft obj = (Xenograft) o;
       if (HashCodeUtil.notEqual(this.getName(), obj.getName())) return false;
       return true;
     }
      
     public int hashCode() {
       int result = HashCodeUtil.SEED;
       result = HashCodeUtil.hash(result, this.getName());    
       return result + super.hashCode();    
     }  
     
     public int compareTo(Object o) {
       if ((o instanceof Xenograft) && (this.getName() != null) && (((Xenograft)o).getName() != null)) {   
         int result = this.getName().compareTo( ((Xenograft)o).getName() );
         if (result != 0) { return result; }               
       }
 
       return super.compareTo(o);
     }    
 }
