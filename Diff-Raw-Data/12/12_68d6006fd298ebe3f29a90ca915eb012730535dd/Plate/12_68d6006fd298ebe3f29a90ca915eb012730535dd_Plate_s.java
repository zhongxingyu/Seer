 /**
  * 
  */
 package net.bioclipse.brunn.pojos;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Persistent data storing class.
  * 
  * @author jonathan
  *
  */
 public class Plate extends AbstractPlate {
 
 	private String barcode;
 	private boolean curated;
 	private MasterPlate masterPlate;
 	
 	public Plate(){
 	}
 	
 	/**
 	 * Package protected constructor not to be used when creating a Plate.
 	 * 
 	 * Use a PlateManager instead
 	 */
 	Plate(User creator, String name, int rows, int cols, MasterPlate masterPlate) {
 	    super(creator, name, rows, cols);
 	    this.masterPlate = masterPlate;
 	    masterPlate.getPlates().add(this);
     }
 	
 	/**
 	 * @return  the barcode
 	 */
 	public String getBarcode() {
 		return barcode;
 	}
 	
 	/**
 	 * @param barcode  the barcode to set
 	 */
 	public void setBarcode(String barCode) {
 		this.barcode = barCode;
 	}
 	
 	@Override
     public boolean equals(Object obj) {
 	    if (this == obj)
 		    return true;
 	    if (!super.equals(obj))
 		    return false;
 	    if ( !(obj instanceof Plate) )
 		    return false;
 	    final Plate other = (Plate) obj;
 	    if (barcode == null) {
 		    if (other.getBarcode() != null)
 			    return false;
 	    }
 	    else if (!barcode.equals(other.getBarcode()))
 		    return false;
 	    if (curated != other.isCurated())
 		    return false;
 	    return true;
     }
 
 	public String toString(){
 
 		String result =  "Plate id: " + this.getId() +
 		                 " Name = "   + this.getName() +
 		                 " Creator = \n"+ this.getCreator() +
 		                 " Barcode = " + this.getBarcode() +
 		                 " Cols = " + this.cols + 
 		                 " Rows = " + this.rows +
 		                 " Wells = \n";
 		
 		if(this.wells != null){
 			for (Object o : this.wells) {
 				Well well = (Well)o;
 				result+="\t" + well.toString() + "\n";
 			}
 		}
 		
 		return result;
 	}
 
 	public boolean isCurated() {
     	return curated;
     }
 
 	public void setCurated(boolean curated) {
     	this.curated = curated;
     }
 
 	/**
 	 * Returns the names of all WellFunctions defined for this plate
 	 * 
 	 * @return names of well functions
 	 */
 	public Collection<String> getWellFunctionNames() {
 
 		Set<String> wellFunctions = new HashSet<String>();
 		for(Well well : this.wells) {
 			for(WellFunction wf : well.getWellFunctions()) {
 				wellFunctions.add(wf.getName());
 			}
 		}
 	    return wellFunctions;
     }
 
 	public Plate deepCopy() {
 		
 		Plate plate = new Plate();
 		plate.setCreator(creator);
 		plate.setName(name);
 		plate.setDeleted(deleted);
 		plate.setRows(rows);
 		plate.setCols(cols);
 		plate.setBarcode(barcode);
 		plate.setMasterPlate(masterPlate);
 		
 		plate.setHashCode(hashCode);
 		plate.setId(id);
 		
 	    for (Well well : wells) {
 	        Well copy = well.deepCopy();
 	        copy.setPlate(plate);
 	        plate.getWells().add(copy);
         }
 	    
 	    for (AbstractAnnotationInstance annotationInstance : getAbstractAnnotationInstances()) {
 	    	AbstractAnnotationInstance copy = annotationInstance.deepCopy();
 	    	copy.setAbstractAnnotatableObject(plate);
 	    	plate.getAbstractAnnotationInstances().add(copy);
 	    }
 	    
 	    for (PlateFunction function : plateFunctions) {
 	    	PlateFunction copy = function.deepCopy();
 	    	copy.setPlate(plate);
 	    	plate.getPlateFunctions().add(copy);
 	    }
 	    
 	    return plate;
     }
 	
 	@Override
 	public void accept(LisObjectVisitor extractFolderObjects) {
 		super.accept(extractFolderObjects);
 		extractFolderObjects.visit(this);
 	}
 
 	public MasterPlate getMasterPlate() {
     	return masterPlate;
     }
 
 	public void setMasterPlate(MasterPlate masterPlate) {
     	this.masterPlate = masterPlate;
 //    	masterPlate.getPlates().add(this);
     }
 }
