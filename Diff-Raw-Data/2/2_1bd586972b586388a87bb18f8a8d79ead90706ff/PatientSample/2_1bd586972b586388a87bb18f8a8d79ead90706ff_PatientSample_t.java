 package net.bioclipse.brunn.pojos;
 
 import java.sql.Timestamp;
 
 public class PatientSample extends AbstractSample {
 
 	private PatientOrigin patientOrigin;
 	private Timestamp defrostingDate;
 
 	public PatientSample() {
 	    super();
     }
 
 	public PatientSample(User creator, String name,
                          SampleContainer sampleContainer,
                          PatientOrigin patientOrigin, 
                          Timestamp defrostingDate) {
 	    super(creator, name, sampleContainer);
 	    this.patientOrigin = patientOrigin;
 	    patientOrigin.getPatientSamples().add(this);
 	    this.defrostingDate = defrostingDate;
     }
 
 	@Override
 	public PatientSample deepCopy() {
 		
 		PatientSample patientSample = (PatientSample)makeNewCopy(creator);
 		
 		patientSample.setHashCode(hashCode);
 		patientSample.setId(id);
 		
 		return patientSample;
 	}
 
 	@Override
 	public AbstractSample makeNewCopy(User creator) {
 		PatientSample patientSample = new PatientSample();
 		patientSample.setName(name);
 		patientSample.setCreator(creator);
 		patientSample.setPatientOrigin(patientOrigin);
 		patientOrigin.getPatientSamples().add(patientSample);
 		patientSample.setDeleted(deleted);
 		patientSample.setDefrostingDate(defrostingDate);
 		
 		for (AbstractAnnotationInstance ai : getAbstractAnnotationInstances()) {
 			AbstractAnnotationInstance copy = ai.deepCopy();
	        patientSample.getAbstractAnnotationInstances().add(copy);
 	        copy.setAbstractAnnotatableObject(patientSample);
         }
 		
 		return patientSample;
 	}
 
 	@Override
     public boolean equals(Object obj) {
 	    if (this == obj)
 		    return true;
 	    if (!super.equals(obj))
 		    return false;
 	    if (!(obj instanceof PatientSample))
 		    return false;
 	    final PatientSample other = (PatientSample) obj;
 	    if (patientOrigin == null) {
 		    if (other.getPatientOrigin() != null)
 			    return false;
 	    }
 	    else if (!patientOrigin.equals(other.getPatientOrigin()) &&
 	    		  !defrostingDate.equals(other.getDefrostingDate()))
 		    return false;
 	    return true;
     }
 
 	public PatientOrigin getPatientOrigin() {
     	return patientOrigin;
     }
 
 	public void setPatientOrigin(PatientOrigin patientOrigin) {
     	this.patientOrigin = patientOrigin;
     }
 
 	public Timestamp getDefrostingDate() {
     	return defrostingDate;
     }
 
 	public void setDefrostingDate(Timestamp defrostingDate) {
     	this.defrostingDate = defrostingDate;
     }
 }
