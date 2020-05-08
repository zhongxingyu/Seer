 package edu.mayo.cts2.framework.plugin.service.umls.domain.entity;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.springframework.stereotype.Component;
 
 import edu.mayo.cts2.framework.model.core.CodeSystemReference;
 import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference;
 import edu.mayo.cts2.framework.model.core.DescriptionInCodeSystem;
 import edu.mayo.cts2.framework.model.core.LanguageReference;
 import edu.mayo.cts2.framework.model.core.NameAndMeaningReference;
 import edu.mayo.cts2.framework.model.core.URIAndEntityName;
 import edu.mayo.cts2.framework.model.entity.Designation;
 import edu.mayo.cts2.framework.model.entity.EntityDescription;
 import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
 import edu.mayo.cts2.framework.model.entity.EntityListEntry;
 import edu.mayo.cts2.framework.model.entity.NamedEntityDescription;
 import edu.mayo.cts2.framework.model.entity.types.DesignationRole;
 import edu.mayo.cts2.framework.model.entity.types.descriptors.DesignationRoleDescriptor;
 import edu.mayo.cts2.framework.model.util.ModelUtils;
 import edu.mayo.cts2.framework.plugin.service.umls.domain.codesystem.CodeSystemUriHandler;
 import edu.mayo.cts2.framework.plugin.service.umls.index.IndexableEntity.Description;
 import edu.mayo.cts2.framework.plugin.service.umls.index.IndexedEntity;
 import edu.mayo.cts2.framework.plugin.service.umls.mapper.CodeDTO;
 import edu.mayo.cts2.framework.plugin.service.umls.mapper.ConceptDTO;
 
 @Component
 public class EntityFactory {
 	
 	public static final String MTH_NAMESPACE = "MTH";
 	
 	private static final URIAndEntityName CONCEPT_TYPE = new URIAndEntityName();
 	static {
 		CONCEPT_TYPE.setName("Concept");
 		CONCEPT_TYPE.setNamespace("skos");
 		CONCEPT_TYPE.setUri("http://www.w3.org/2004/02/skos/core#Concept");
 	}
 	
 	@Resource
 	private EntityUriHandler entityUriHandler;
 	
 	@Resource
 	private CodeSystemUriHandler codeSystemUriHandler;
 	
 	public EntityDescription createEntity(ConceptDTO conceptDto){
 		NamedEntityDescription namedEntity = new NamedEntityDescription();
 		
 		String sab = MTH_NAMESPACE;
 		String name = conceptDto.getUi();
 		namedEntity.setEntityID(ModelUtils.createScopedEntityName(name, sab));
 		
 		namedEntity.setAbout(entityUriHandler.getUri(sab, name));
 		
 		Designation designation = new Designation();
 		designation.setValue(ModelUtils.toTsAnyType(conceptDto.getString()));
 		
 		namedEntity.addDesignation(designation);
 		namedEntity.setDescribingCodeSystemVersion(
 			this.buildCodeSystemVersionReference(sab));
 		
 		namedEntity.addEntityType(CONCEPT_TYPE);
 
 		EntityDescription entity = new EntityDescription();
 
 		entity.setNamedEntity(namedEntity);
 		
 		return entity;
 	}
 	
 	public EntityDescription createEntity(List<CodeDTO> codeDtos){
 		
 		if (codeDtos == null)
 			return null;
 		
 		if (codeDtos.isEmpty())
 			return null;
 		
 		NamedEntityDescription namedEntity = new NamedEntityDescription();
 		
 		String sab = null;
 		String name = null;
 		
 		for (CodeDTO codeDto : codeDtos)
 		{
 			if ((sab == null)||(name == null))
 			{
 					sab = codeDto.getSab();
 					name = codeDto.getUi();
 					namedEntity.setEntityID(ModelUtils.createScopedEntityName(name, sab));
 		
 					namedEntity.setAbout(entityUriHandler.getUri(sab, name));
 					namedEntity.addEntityType(CONCEPT_TYPE);
 					namedEntity.setDescribingCodeSystemVersion(
 							this.buildCodeSystemVersionReference(sab));
 
 			}
 		
 			Designation designation = new Designation();
 			designation.setValue(ModelUtils.toTsAnyType(codeDto.getName()));
 			LanguageReference lref = new LanguageReference(codeDto.getLanguage());
 			designation.setLanguage(lref);
 			
			if ("yes".equalsIgnoreCase(codeDto.isPreferred()))
 				designation.setDesignationRole(DesignationRole.PREFERRED);
 			
 			namedEntity.addDesignation(designation);
 		}		
 
 		EntityDescription entity = new EntityDescription();
 		entity.setNamedEntity(namedEntity);
 		
 		return entity;
 	}
 	
 	public EntityDirectoryEntry createEntityDirectoryEntry(IndexedEntity indexedEntity){
 		EntityDirectoryEntry entry = new EntityDirectoryEntry();
 		
 		String sab = indexedEntity.getSab();
 		String name = indexedEntity.getName();
 		entry.setName(ModelUtils.createScopedEntityName(name, sab));
 		
 		entry.setAbout(entityUriHandler.getUri(sab, name));
 
 		//TODO: Need to better determine ranking
 		Description description = indexedEntity.getDescriptions().get(0);
 		
 		DescriptionInCodeSystem descriptionInCodeSystem = new DescriptionInCodeSystem();
 		descriptionInCodeSystem.setDesignation(description.getValue());
 		descriptionInCodeSystem.
 			setDescribingCodeSystemVersion(
 				this.buildCodeSystemVersionReference(sab));
 		
 		entry.addKnownEntityDescription(descriptionInCodeSystem);
 		
 		entry.setMatchStrength(indexedEntity.getScore());
 		
 		return entry;
 	}
 	
 	public EntityListEntry createEntityListEntry(IndexedEntity indexedEntity){
 		EntityListEntry entry = new EntityListEntry();
 		
 		String sab = indexedEntity.getSab();
 		String name = indexedEntity.getName();
 		
 		EntityDescription ed = new EntityDescription();
 
 		//TODO: Need to better determine ranking
 		Description description = indexedEntity.getDescriptions().get(0);
 		
 		DescriptionInCodeSystem descriptionInCodeSystem = new DescriptionInCodeSystem();
 		descriptionInCodeSystem.setDesignation(description.getValue());
 		descriptionInCodeSystem.
 			setDescribingCodeSystemVersion(
 				this.buildCodeSystemVersionReference(sab));
 
 		
 		
 		entry.setHref(entityUriHandler.getUri(sab, name));
 		entry.setEntry(ed);
 		entry.setMatchStrength(indexedEntity.getScore());
 		
 		return entry;
 	}
 	
 	private CodeSystemVersionReference buildCodeSystemVersionReference(String sab){
 		NameAndMeaningReference versionRef = new NameAndMeaningReference();
 		versionRef.setContent(sab);
 		versionRef.setUri(this.codeSystemUriHandler.getUri(sab));
 
 		CodeSystemReference codeSystemRef = new CodeSystemReference();
 		codeSystemRef.setContent(sab);
 		codeSystemRef.setUri(this.codeSystemUriHandler.getUri(sab));
 		
 		CodeSystemVersionReference ref = new CodeSystemVersionReference();
 		ref.setCodeSystem(codeSystemRef);
 		ref.setVersion(versionRef);
 
 		return ref;
 	}
 
 }
