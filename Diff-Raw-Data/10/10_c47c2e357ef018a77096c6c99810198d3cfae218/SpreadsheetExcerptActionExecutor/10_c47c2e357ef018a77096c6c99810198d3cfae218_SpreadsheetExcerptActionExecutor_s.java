 package com.quanticate.opensource.spreadsheetexcerpt.action;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.alfresco.error.AlfrescoRuntimeException;
 import org.alfresco.model.ContentModel;
 import org.alfresco.repo.action.ParameterDefinitionImpl;
 import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
 import org.alfresco.service.cmr.action.Action;
 import org.alfresco.service.cmr.action.ParameterDefinition;
 import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
 import org.alfresco.service.cmr.repository.ChildAssociationRef;
 import org.alfresco.service.cmr.repository.ContentReader;
 import org.alfresco.service.cmr.repository.ContentService;
 import org.alfresco.service.cmr.repository.ContentWriter;
 import org.alfresco.service.cmr.repository.NodeRef;
 import org.alfresco.service.cmr.repository.NodeService;
 import org.alfresco.service.namespace.QName;
 
 import com.quanticate.opensource.spreadsheetexcerpt.excerpt.MakeReadOnlyAndExcerpt;
 
 public class SpreadsheetExcerptActionExecutor extends ActionExecuterAbstractBase
 {
    public static final String NAME = "spreadsheet-excerpt";
    public static final String PARAM_DESTINATION_FOLDER = "destination-folder";
    public static final String PARAM_KEEP_SHEETS = "keep-sheets";
    public static final String PARAM_REMOVE_FORMULAS = "remove-formulas";
    
    private NodeService nodeService;
    private ContentService contentService;
    private MakeReadOnlyAndExcerpt excerpter;
 
    @Override
    protected void executeImpl(Action action, NodeRef sourceNodeRef)
    {
       // Sanity check
      if (this.nodeService.exists(sourceNodeRef) == false)
       {
           // node doesn't exist - can't do anything
           return;
       }
       
       // Get the details of the folder to save into
       NodeRef destinationParent = (NodeRef)action.getParameterValue(PARAM_DESTINATION_FOLDER);
       
       // Get the list of sheets to keep
       List<String> sheetsToKeepL = (List<String>)action.getParameterValue(PARAM_KEEP_SHEETS);
       String[] sheetsToKeep = sheetsToKeepL.toArray(new String[sheetsToKeepL.size()]);
       
       // TODO Remove Formulas
       
       
       // Work out what to call the new node
       ChildAssociationRef sourceChildAssoc = nodeService.getPrimaryParent(sourceNodeRef);
       if (sourceChildAssoc.getParentRef().equals(destinationParent))
       {
          throw new AlfrescoRuntimeException("Cannot create the new Spreadsheet in the same folder as the existing one");
       }
       QName newAssocName = sourceChildAssoc.getQName();
       
       // Get the original sheet
       ContentReader reader = contentService.getReader(sourceNodeRef, ContentModel.PROP_CONTENT);
       
       // Get a writer to the new location
       Map<QName,Serializable> props = buildNewNodeProperties(sourceNodeRef);
       NodeRef newNode = nodeService.createNode(destinationParent, 
             ContentModel.ASSOC_CONTAINS, newAssocName,
             ContentModel.TYPE_CONTENT, props).getChildRef();
       ContentWriter writer = contentService.getWriter(newNode, ContentModel.PROP_CONTENT, true);
       
       
       // Have the excerpt performed
       try
       {
          excerpter.excerpt(sheetsToKeep, reader, writer);
       }
       catch (IOException e)
       {
          throw new AlfrescoRuntimeException("Error performing the Excerpt", e);
       }
    }
    
    private Map<QName,Serializable> buildNewNodeProperties(NodeRef sourceNodeRef)
    {
       Map<QName,Serializable> sourceProps = nodeService.getProperties(sourceNodeRef);
       Map<QName,Serializable> newProps = new HashMap<QName, Serializable>();
       for (QName qname : sourceProps.keySet())
       {
          if (qname.equals(ContentModel.PROP_NAME) ||
              qname.equals(ContentModel.PROP_TITLE) ||
              qname.equals(ContentModel.PROP_DESCRIPTION))
          {
             newProps.put(qname, sourceProps.get(qname));
          }
       }
       return newProps;
    }
 
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
       paramList.add(new ParameterDefinitionImpl(
             PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, true, 
             getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
       paramList.add(new ParameterDefinitionImpl(
             PARAM_KEEP_SHEETS, DataTypeDefinition.TEXT, true, 
             getParamDisplayLabel(PARAM_KEEP_SHEETS), true));
       paramList.add(new ParameterDefinitionImpl(
             PARAM_REMOVE_FORMULAS, DataTypeDefinition.BOOLEAN, false, 
             getParamDisplayLabel(PARAM_REMOVE_FORMULAS)));
    }
    
    public void setNodeService(NodeService nodeService)
    {
       this.nodeService = nodeService;
    }
    public void setContentService(ContentService contentService)
    {
       this.contentService = contentService;
    }
    public void setExcerpter(MakeReadOnlyAndExcerpt excerpter)
    {
       this.excerpter = excerpter;
    }
 }
