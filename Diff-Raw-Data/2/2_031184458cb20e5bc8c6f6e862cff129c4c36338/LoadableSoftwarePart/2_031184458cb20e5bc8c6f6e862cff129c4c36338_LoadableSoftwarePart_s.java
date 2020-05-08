 package edu.cmu.sv.arinc838.specification;
 
 import java.util.Collection;
 
 import com.arinc.arinc838.FileDefinitions;
 import com.arinc.arinc838.LspIntegrityDefinition;
 import com.arinc.arinc838.SdfIntegrityDefinition;
 import com.arinc.arinc838.SoftwareDescription;
 
 
 public interface LoadableSoftwarePart {
 	
 	public SoftwareDescription getSoftwareDescription();
 
 	public void setSoftwareDescription(SoftwareDescription value);
 
 	public Collection<TargetHardwareDefinition> getThwDefinitions();
 
	public void setTargetHardwareDefinitions(Collection value);
 
 	public FileDefinitions getFileDefinitions();
 
 	public void setFileDefinitions(FileDefinitions value);
 
 	public SdfIntegrityDefinition getSdfIntegrityDefinition();
 
 	public void setSdfIntegrityDefinition(SdfIntegrityDefinition value);
 
 	public LspIntegrityDefinition getLspIntegrityDefinition();
 
 	public void setLspIntegrityDefinition(LspIntegrityDefinition value);
 
 }
