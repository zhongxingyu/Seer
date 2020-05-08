 package edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.valueSetDefinitionResolutionImpl.utility;
 
 import org.apache.commons.lang.StringUtils;
 import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
 import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference;
 import edu.mayo.cts2.framework.model.core.EntityReference;
 import edu.mayo.cts2.framework.model.core.ScopedEntityName;
 import edu.mayo.cts2.framework.model.core.URIAndEntityName;
 import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.service.exception.UnknownEntity;
import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.ctsUtility.ExceptionBuilder;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.ctsUtility.Utilities;
 
 /**
  * A utility class to allow for handling and comparing different forms of an entity before it has been fully resolved,
  * and later resolving the entity.
  * 
  * @author darmbrust
  * 
  */
 
 public class EntityReferenceResolver
 {
 	private URIAndEntityName entity_;
 	EntityReference er_;
 	CodeSystemVersionReference codeSystemVersion_; 
 
 	public EntityReferenceResolver(URIAndEntityName entity, CodeSystemVersionReference codeSystemVersion)
 	{
 		this.entity_ = entity;
 		this.codeSystemVersion_ = codeSystemVersion;
 	}
 
 	public EntityReferenceResolver(EntityReferenceAndHref er)
 	{
 		er_ = er.getEntityReference();
 
 		entity_ = new URIAndEntityName();
 		entity_.setUri(er_.getAbout());
 		entity_.setHref(er.getHref());
 
 		if (er_.getName() != null)
 		{
 			entity_.setName(er_.getName().getName());
 			entity_.setNamespace(er_.getName().getNamespace());
 		}
 		if (er_.getKnownEntityDescriptionAsReference().size() > 0)
 		{
 			entity_.setDesignation(er_.getKnownEntityDescriptionAsReference().get(0).getDesignation());
 		}
 	}
 
 	public EntityReferenceResolver(EntityDirectoryEntry ede)
 	{
 		entity_ = new URIAndEntityName();
 		entity_.setUri(ede.getAbout());
 		if (StringUtils.isBlank(ede.getHref()) && ede.getKnownEntityDescriptionAsReference().size() > 0)
 		{
 			entity_.setHref(ede.getKnownEntityDescriptionAsReference().get(0).getHref());
 		}
 		else
 		{
 			entity_.setHref(ede.getHref());
 		}
 		if (ede.getName() != null)
 		{
 			entity_.setName(ede.getName().getName());
 			entity_.setNamespace(ede.getName().getNamespace());
 		}
 		if (ede.getKnownEntityDescriptionAsReference().size() > 0)
 		{
 			entity_.setDesignation(ede.getKnownEntityDescriptionAsReference().get(0).getDesignation());
 		}
 
 		er_ = new EntityReference();
 		er_.setAbout(ede.getAbout());
 		er_.setKnownEntityDescription(ede.getKnownEntityDescriptionAsReference());
 		er_.setName(ede.getName());
 	}
 
 	public EntityDirectoryEntry buildEntityDirectoryEntity()
 	{
 		EntityDirectoryEntry ede = new EntityDirectoryEntry();
 		ede.setAbout(entity_.getUri());
 		ede.setHref(entity_.getHref());
 		if (entity_.getName() != null && entity_.getNamespace() != null)
 		{
 			ScopedEntityName sen = new ScopedEntityName();
 			sen.setName(entity_.getName());
 			sen.setNamespace(entity_.getNamespace());
 			ede.setName(sen);
 		}
 		ede.setResourceName(entity_.getName());
 		ede.setKnownEntityDescription(er_.getKnownEntityDescriptionAsReference());
 		return ede;
 	}
 
 	@Override
 	public int hashCode()
 	{
 		if (StringUtils.isNotBlank(entity_.getUri()))
 		{
 			return entity_.getUri().hashCode();
 		}
 		else
 		{
 			throw new RuntimeException("URI is required for hashcode calucations on this class");
 		}
 	}
 
 	@Override
 	public boolean equals(Object obj)
 	{
 		if (obj == null || !(obj instanceof EntityReferenceResolver))
 		{
 			return false;
 		}
 		EntityReferenceResolver other = (EntityReferenceResolver) obj;
 
 		if (StringUtils.isNotBlank(entity_.getUri()))
 		{
 			return entity_.getUri().equals(other.entity_.getUri());
 		}
 		else
 		{
 			throw new RuntimeException("URI is required for .equals operations on this class");
 		}
 	}
 
 	public boolean isResolved()
 	{
 		return er_ != null;
 	}
 
 	public URIAndEntityName getEntity()
 	{
 		return entity_;
 	}
 
 	public EntityReference getEntityReference()
 	{
 		return er_;
 	}
 
 	/**
 	 * Only resolves if it has not yet been resolved
 	 */
	public void resolveEntity(ResolvedReadContext readContext, Utilities utilities) throws UnknownEntity
 	{
 		if (!isResolved())
 		{
 			if (codeSystemVersion_ == null)
 			{
 				throw new RuntimeException("Code system version wasn't passed in upon creation!");
 			}
 			EntityReferenceAndHref temp = utilities.resolveEntityReference(entity_, codeSystemVersion_, readContext);
			if (temp == null)
			{
				throw ExceptionBuilder.buildUnknownEntity("Failed to resolve the entity '" + entity_ + "' in '" + codeSystemVersion_ + "'");
			}
 			er_ = temp.getEntityReference();
 			entity_.setHref(temp.getHref());  // Reset the href to what we used to resolve it.
 		}
 	}
 
 	@Override
 	public String toString()
 	{
 		return "EntityReferenceResolver [entity_='" + entity_ + "', er_='" + er_ + "']";
 	}
 }
