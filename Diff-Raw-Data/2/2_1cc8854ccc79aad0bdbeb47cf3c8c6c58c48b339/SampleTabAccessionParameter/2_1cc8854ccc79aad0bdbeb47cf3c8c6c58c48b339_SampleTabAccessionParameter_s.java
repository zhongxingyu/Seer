 package uk.ac.ebi.fgpt.conan.process.biosd.model;
 
 import uk.ac.ebi.fgpt.conan.model.AbstractConanParameter;
 
 public class SampleTabAccessionParameter extends AbstractConanParameter {
 
 	protected String accession = null;
 	
 	public SampleTabAccessionParameter() {
         super("SampleTab Accession");
     }
 	
 	protected SampleTabAccessionParameter(String name) {
 	    super(name);
 	}
 	
 	public void setAccession(String accession) throws IllegalArgumentException {
	    if (!accession.startsWith("G") || !accession.contains("-"))
 	        throw new IllegalArgumentException("Invalid accession "+accession);
 		this.accession = accession;
 	}
 	
 	public String getAccession(){
 		return this.accession;
 	}
 }
