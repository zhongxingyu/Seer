 package com.altaworks.magnolia;
 
 import info.magnolia.cms.beans.config.ContentRepository;
 import info.magnolia.cms.core.Content;
 import info.magnolia.cms.core.HierarchyManager;
 import info.magnolia.context.MgnlContext;
 import info.magnolia.module.blossom.support.RepositoryUtils;
 import info.magnolia.module.blossom.template.BlossomTemplate;
 import info.magnolia.module.blossom.template.BlossomTemplateDescription;
 import info.magnolia.module.blossom.template.DefaultBlossomTemplateRegistry;
 import info.magnolia.module.blossom.template.TemplateDescriptionBuilder;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.InitializingBean;
 
 import javax.jcr.RepositoryException;
 
 /**
  * @author Åke Argéus
  */
 public class GrailsTemplateRegistry extends DefaultBlossomTemplateRegistry implements InitializingBean {
 
 	private static final String TEMPLATES_PATH = "/modules/blossom/templates/autodetected";
 
 	@Override
 	protected void writeTemplateDefinition(BlossomTemplateDescription templateDescription) throws RepositoryException {
 		HierarchyManager hierarchyManager = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG);
 		if (!hierarchyManager.isExist(TEMPLATES_PATH + "/" + templateDescription.getName())) {
 			Content configNode = RepositoryUtils.createContentNode(hierarchyManager, TEMPLATES_PATH, templateDescription.getName());
 			configNode.createNodeData("name", templateDescription.getName());
 			configNode.createNodeData("title", templateDescription.getTitle());
 			configNode.createNodeData("description", templateDescription.getDescription());
 			configNode.createNodeData("visible", templateDescription.isVisible());
 			if (StringUtils.isNotBlank(templateDescription.getI18nBasename()))
 				configNode.createNodeData("i18nBasename", templateDescription.getI18nBasename());
 			configNode.createNodeData("type", "blossom");
 			configNode.createNodeData("class", BlossomTemplate.class.getName());
 			configNode.getParent().save();
		}else{
			Content configNode = RepositoryUtils.createContentNode(hierarchyManager, TEMPLATES_PATH, templateDescription.getName());
			configNode.setNodeData("name", templateDescription.getName());
			configNode.setNodeData("title", templateDescription.getTitle());
			configNode.setNodeData("description", templateDescription.getDescription());
			configNode.setNodeData("visible", templateDescription.isVisible());
			if (StringUtils.isNotBlank(templateDescription.getI18nBasename()))
				configNode.setNodeData("i18nBasename", templateDescription.getI18nBasename());
			configNode.setNodeData("type", "blossom");
			configNode.setNodeData("class", BlossomTemplate.class.getName());
			configNode.getParent().save();
 		}
 	}
 
 	public void afterPropertiesSet() throws Exception {
 
 		HierarchyManager hierarchyManager = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG);
 		RepositoryUtils.deleteContent(hierarchyManager, TEMPLATES_PATH);
 
 		if (descriptionBuilder == null)
 			descriptionBuilder = new TemplateDescriptionBuilder();
 	}
 }
