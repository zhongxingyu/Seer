 package org.jboss.pressgangccms.restserver.factories;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.jboss.pressgangccms.rest.v1.collections.RESTPropertyTagCollectionV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTPropertyTagV1;
 import org.jboss.pressgangccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgangccms.rest.v1.expansion.ExpandDataTrunk;
 import org.jboss.pressgangccms.restserver.BaseRESTv1;
 import org.jboss.pressgangccms.restserver.entities.PropertyTag;
 import org.jboss.pressgangccms.restserver.factories.base.RESTDataObjectCollectionFactory;
 import org.jboss.pressgangccms.restserver.factories.base.RESTDataObjectFactory;
 
 public class PropertyTagV1Factory extends RESTDataObjectFactory<RESTPropertyTagV1, PropertyTag, RESTPropertyTagCollectionV1>
 {
 	public PropertyTagV1Factory()
 	{
 		super(PropertyTag.class);
 	}
 	
 	@Override
 	public RESTPropertyTagV1 createRESTEntityFromDBEntity(final PropertyTag entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final Number revision, final boolean expandParentReferences, final EntityManager entityManager)
 	{
 		assert entity != null : "Parameter topic can not be null";
 		assert baseUrl != null : "Parameter baseUrl can not be null";
 		
 		final RESTPropertyTagV1 retValue = new RESTPropertyTagV1();
 		
 		final List<String> expandOptions = new ArrayList<String>();
 		if (revision == null)
 			expandOptions.add(RESTBaseEntityV1.REVISIONS_NAME);
 		retValue.setExpand(expandOptions);
 		
 		retValue.setId(entity.getPropertyTagId());
 		retValue.setName(entity.getPropertyTagName());
 		retValue.setRegex(entity.getPropertyTagRegex());
 		retValue.setIsUnique(entity.getPropertyTagIsUnique());
 		
 		if (revision == null)
 		{
 			retValue.setRevisions(new RESTDataObjectCollectionFactory<RESTPropertyTagV1, PropertyTag, RESTPropertyTagCollectionV1>().create(RESTPropertyTagCollectionV1.class, new PropertyTagV1Factory(), entity, entity.getRevisions(entityManager), RESTBaseEntityV1.REVISIONS_NAME, dataType, expand, baseUrl, entityManager));
 		}
 		
 		retValue.setLinks(baseUrl, BaseRESTv1.PROPERTYTAG_URL_NAME, dataType, retValue.getId());
 		
 		return retValue;
 	}
 
 	@Override
 	public
 	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final PropertyTag entity, final RESTPropertyTagV1 dataObject)
 	{
 		if (dataObject.hasParameterSet(RESTPropertyTagV1.DESCRIPTION_NAME))
 			entity.setPropertyTagDescription(dataObject.getDescription());
 		if (dataObject.hasParameterSet(RESTPropertyTagV1.CANBENULL_NAME))
 			entity.setPropertyTagCanBeNull(dataObject.getCanBeNull());
 		if (dataObject.hasParameterSet(RESTPropertyTagV1.NAME_NAME))
 			entity.setPropertyTagName(dataObject.getName());
 		if (dataObject.hasParameterSet(RESTPropertyTagV1.REGEX_NAME))
 			entity.setPropertyTagRegex(dataObject.getRegex());
 		if (dataObject.hasParameterSet(RESTPropertyTagV1.ISUNIQUE_NAME))
 			entity.setPropertyTagIsUnique(dataObject.getIsUnique());
 		
 		entityManager.persist(entity);
 	}
 }
