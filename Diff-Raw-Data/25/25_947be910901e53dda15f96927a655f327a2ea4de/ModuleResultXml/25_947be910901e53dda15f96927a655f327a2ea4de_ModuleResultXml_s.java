 package org.kalibro.service.xml;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.kalibro.Module;
 import org.kalibro.ModuleResult;
 import org.kalibro.dto.ModuleResultDto;
 
 /**
  * XML element for {@link ModuleResult}.
  * 
  * @author Carlos Morais
  */
 @XmlRootElement(name = "moduleResult")
 @XmlAccessorType(XmlAccessType.FIELD)
 public class ModuleResultXml extends ModuleResultDto {
 
 	@XmlElement
 	private Long id;
 	@XmlElement
 	private ModuleXml module;
 	@XmlElement
 	private Double grade;
 
 	public ModuleResultXml() {
 		super();
 	}
 
 	public ModuleResultXml(ModuleResult moduleResult) {
 		id = moduleResult.getId();
 		module = new ModuleXml(moduleResult.getModule());
 		grade = moduleResult.getGrade();
 	}
 
 	@Override
 	public Long id() {
 		return id;
 	}
 
 	@Override
 	public Module module() {
 		return module.convert();
 	}
 
 	@Override
 	public Double grade() {
 		return grade;
 	}
 }
