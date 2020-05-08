 /**
  * 
  */
 package net.bioclipse.brunn.pojos;
 
 import java.util.HashSet;
 import java.util.Set;
 
 
 /**
  * Persistent data storing class.
  * 
  * @author jonathan
  *
  */
 public class Well extends AbstractWell {
 	
 	private AbstractPlate plate;
 	private SampleContainer sampleContainer;
 	private Set<SampleMarker> sampleMarkers;
 	private boolean outlier;
 	
 	public Well(){
 		super();
 		sampleMarkers = new HashSet<SampleMarker>();
 	}
 
 	public Well( User creator, 
 	             String name, 
 	             int col, 
 	             char row, 
 	             AbstractPlate plate ) {
 		
 	    super(creator, name, col, row);
 	    this.plate           = plate;
 	    this.sampleContainer = new SampleContainer(creator, name, this);
 	    this.sampleMarkers   = new HashSet<SampleMarker>();
     }
 
 	public Set<SampleMarker> getSampleMarkers() {
     	return sampleMarkers;
     }
 
 	public void setSampleMarkers(Set<SampleMarker> sampleMarkers) {
     	this.sampleMarkers = sampleMarkers;
     }
 	
 	public AbstractPlate getPlate() {
     	return plate;
     }
 
 	public void setPlate(AbstractPlate plate) {
     	this.plate = plate;
     }
 
 	@Override
     public boolean equals(Object obj) {
 	    if (this == obj)
 		    return true;
 	    if (!super.equals(obj))
 		    return false;
 	    if ( !(obj instanceof Well) )
 		    return false;
 	    final Well other = (Well) obj;
 	    if (outlier != other.isOutlier())
 		    return false;
 	    if (sampleContainer == null) {
 		    if (other.getSampleContainer() != null)
 			    return false;
 	    }
 	    else if (!sampleContainer.equals(other.getSampleContainer()))
 		    return false;
 	    if (sampleMarkers == null) {
 		    if (other.getSampleMarkers() != null)
 			    return false;
 	    }
 	    else if (!sampleMarkers.equals(other.getSampleMarkers()))
 		    return false;
 	    return true;
     }
 
 	public String toString() {
 		
 		StringBuilder sb = new StringBuilder();
 		for(AbstractSample as : this.sampleContainer.getSamples()) {
 			sb.append( as.toString() );
 		}
 		
 		String result = "Well id: " + this.getId() +
 		                " Name: " + this.getName() +
 		                " Col:" + this.getCol() +
 		                " Row: " + this.getRow() + 
 		                sb.toString();
 		return result;
 	}
 
 	/**
 	 * @return  the sampleContainer
 	 */
     public SampleContainer getSampleContainer() {
     	return sampleContainer;
     }
 
 	/**
 	 * @param sampleContainer  the sampleContainer to set
 	 */
     public void setSampleContainer(SampleContainer sampleContainer) {
     	this.sampleContainer = sampleContainer;
     }
 
 	public Well deepCopy() {
 		
 		Well well = new Well();
 		
 		well.setCreator(creator);
 		well.setName(name);
 		well.setCol(col);
 		well.setRow(row);
 
 		well.setId(id);
 		well.setHashCode(hashCode);
 		
 		//wellFunctions
 		Set<WellFunction> wellFunctions = new HashSet<WellFunction>();
 		for (WellFunction f : this.getWellFunctions()) {
 			WellFunction copy = f.deepCopy();
 			copy.setWell(well);
 	        wellFunctions.add(copy);
         }
 		well.setWellFunctions(wellFunctions);
 	
 		//sampleContainer
 		SampleContainer sampleContainer = this.sampleContainer.deepCopy();
 		sampleContainer.setWell(well);
 		//Removes the created drugsamples. Ugly solution. TODO some time...
 		Set<AbstractSample> samples = sampleContainer.getSamples();
 		sampleContainer.setSamples( new HashSet<AbstractSample>() );
 		for(AbstractSample as : samples) {
 			if(as instanceof DrugSample) {
 				as.setSampleContainer(null);
 			}
			else if(as instanceof CellSample) {
 				sampleContainer.getSamples().add(as);
 				as.setSampleContainer(sampleContainer);
 			}
 			else {
 				throw new IllegalArgumentException("unknown sample type: " + as);
 			}
 		}
 		
 		well.setSampleContainer( sampleContainer );
 	
 		
 //		sampleMarkers
 		Set<SampleMarker> sampleMarkers = new HashSet<SampleMarker>();
 		for (SampleMarker marker : this.sampleMarkers) {
 			SampleMarker sampleMarker = marker.deepCopy();
 			sampleMarker.setWell(well);
 			//Add any samples to the samplecontainer
 			if( sampleMarker.getSample() != null ) {
 				sampleContainer.addSample( sampleMarker.getSample() );
 			}
 	        sampleMarkers.add(sampleMarker);
         }
 		well.setSampleMarkers(sampleMarkers);
 		
 		//sampleMarkers
 //		Set<SampleMarker> sampleMarkers = new HashSet<SampleMarker>();
 //		for (SampleMarker marker : this.sampleMarkers) {
 //			SampleMarker sampleMarker = marker.deepCopy();
 //			sampleMarker.setWell(well);
 //	        sampleMarkers.add(sampleMarker);
 //        }
 //		well.setSampleMarkers(sampleMarkers);
 //		
 //		//Only one (and the same) copy of Samples
 //		
 //		Set<AbstractSample> samples = new HashSet<AbstractSample>();
 //		for(SampleMarker marker : well.getSampleMarkers()) {
 //			if(marker.getSample() != null ) {
 //				samples.add(marker.getSample());
 //				marker.getSample().setSampleContainer(well.sampleContainer);
 //			}
 //		}
 //		well.sampleContainer.setSamples(samples);
 		
 		//outlier
 		well.setOutlier(outlier);
 		
 		//deleted
 		well.setDeleted(deleted);
 	    return well;
     }
 	
 	public void delete() {
 		super.delete();
 		if( sampleContainer != null ) {
 			sampleContainer.delete();			
 		}
 		for (SampleMarker marker : sampleMarkers) {
 	        marker.delete();
         }
 		for (WellFunction function : wellFunctions) {
 			function.delete();
 		}
 	}
 
 	public boolean isOutlier() {
     	return outlier;
     }
 
 	public void setOutlier(boolean outlier) {
     	this.outlier = outlier;
     }
 	
 	public boolean getOutlier() {
 		return isOutlier();
 	}
 
 	public Well makeNewCopy() {
 
 		Well well = new Well();
 		
 		well.setCreator(creator);
 		well.setName(name);
 		well.setCol(col);
 		well.setRow(row);
 		
 		//wellFunctions
 		Set<WellFunction> wellFunctions = new HashSet<WellFunction>();
 		for (WellFunction f : this.getWellFunctions()) {
 			WellFunction copy = f.makeNewCopy();
 			copy.setWell(well);
 	        wellFunctions.add(copy);
         }
 		well.setWellFunctions(wellFunctions);
 	
 		//sampleContainer
 		SampleContainer sampleContainer = new SampleContainer(creator, this.sampleContainer.getName(), well );
 		well.setSampleContainer( sampleContainer );
 		sampleContainer.setWorkList( this.sampleContainer.getWorkList().makeNewCopy() );
 		
 		//sampleMarkers
 		Set<SampleMarker> sampleMarkers = new HashSet<SampleMarker>();
 		for (SampleMarker marker : this.sampleMarkers) {
 			SampleMarker sampleMarker = marker.makeNewCopy();
 			sampleMarker.setWell(well);
 			//Add any samples to the samplecontainer
 			if( sampleMarker.getSample() != null ) {
 				sampleContainer.addSample( sampleMarker.getSample() );
 			}
 	        sampleMarkers.add(sampleMarker);
         }
 		well.setSampleMarkers(sampleMarkers);
 		
 		//outlier
 		well.setOutlier(outlier);
 		
 		//deleted
 		well.setDeleted(deleted);
 	    return well;
     }
 
 	public WellFunction getWellFunctionByName(String wellFunctionToBeShown) {
 
 		for (WellFunction function : wellFunctions) {
 	        if(function.getName().equals(wellFunctionToBeShown))
 	        	return function;
         }
 		return null;
     }
 }
