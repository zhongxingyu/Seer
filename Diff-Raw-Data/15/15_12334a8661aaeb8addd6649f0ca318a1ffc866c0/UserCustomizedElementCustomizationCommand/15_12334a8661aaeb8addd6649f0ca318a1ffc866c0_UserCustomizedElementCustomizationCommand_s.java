 package org.eclipse.jst.pagedesigner.itemcreation.command;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jst.jsf.common.metadata.query.ITaglibDomainMetaDataModelContext;
 import org.eclipse.jst.jsf.common.metadata.query.TaglibDomainMetaDataQueryHelper;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.internal.ResolverUtil;
 import org.eclipse.jst.jsf.tagdisplay.internal.paletteinfos.TagCreationAttribute;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.dom.DOMPosition;
 import org.eclipse.jst.pagedesigner.dom.IDOMPosition;
 import org.eclipse.jst.pagedesigner.editors.palette.ITagDropSourceData;
 import org.eclipse.jst.pagedesigner.editors.palette.MetadataTagDropSourceData;
 import org.eclipse.jst.pagedesigner.editors.palette.TagToolCreationAdapter;
 import org.eclipse.jst.pagedesigner.editors.palette.impl.PaletteItemManager;
 import org.eclipse.jst.pagedesigner.itemcreation.CreationData;
 import org.eclipse.jst.pagedesigner.itemcreation.customizer.ICustomizationData;
 import org.eclipse.jst.pagedesigner.utils.JSPUtil;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.w3c.dom.Element;
import org.w3c.dom.Node;
 
 /**
  * Added extra functionality to use the customization data to set tag attributes
  * based on user enter values from drop wizard display.
  * 
  * @author prusev
  * @author Debajit Adhikary
  * 
  */
 public class UserCustomizedElementCustomizationCommand extends
         ElementCustomizationCommand
 {
 
     /**
      * @param model
      * @param element
      * @param creationData
      */
     public UserCustomizedElementCustomizationCommand(IDOMModel model,
             Element element, CreationData creationData)
     {
         super(model, element, creationData);
     }
 
     
     /* (non-Javadoc)
      * @see org.eclipse.jst.pagedesigner.itemcreation.command.ElementCustomizationCommand#execute()
      */
     @Override
     public void execute()
     {
         super.execute();
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.jst.pagedesigner.itemcreation.command.ElementCustomizationCommand#applyChildElementCustomization()
      */
     @Override
     protected void applyChildElementCustomization()
     {
         // TODO: Use this to get child information from metadata?
         // super.applyChildElementCustomization();
 
         // Get customization data
         if (_creationData.getDropCustomizationData() != null)
         {
             final ICustomizationData data = (ICustomizationData) _creationData.getDropCustomizationData().getAdapter(ICustomizationData.class);
            
            if (data.getTextNodeData() != null) {
            	final Node textNode = _element.getOwnerDocument().createTextNode(data.getTextNodeData());
            	_element.appendChild(textNode);
            } 
            else if (data.getChildrenData() != null)
             {
                 int childCount = 0;
                 CHILDREN_LOOP: for (ICustomizationData child : data.getChildrenData().getChildList())
                 {
                     assert (_element.getOwnerDocument() != null);
 
                     // Setup child node
                     Element childNode = _element.getOwnerDocument().createElement(child.getTagIdentifier().getTagName());
                     String prefix = JSPUtil.getOrCreatePrefix(_model, child.getTagIdentifier().getUri(), null);
                     childNode.setPrefix(prefix);
 
                     // Attach to child the attributes from the customization data 
                     Map<String, String> attrMap = child.getAttributeData().getAttributes();
                     for (String attrName : attrMap.keySet())
                     {
                         String attrValue = attrMap.get(attrName);
                         childNode.setAttribute(attrName, attrValue);
                     }
 
                     // Attach child node to element
                     _element.appendChild(childNode);
 
                     // Set up other attributes and child tags for this child
                     IFile fileForDocument = ResolverUtil.getFileForDocument(_model.getStructuredDocument());
                     if (fileForDocument == null)
                     {
                         PDPlugin.log("File not found for model: "+_model.toString(), new Exception("Stack trace only"));  //$NON-NLS-1$ //$NON-NLS-2$
                         continue CHILDREN_LOOP;
                     }
 //                    final IProject project = fileForDocument.getProject();
 //                    PaletteItemManager itemManager = PaletteItemManager.getInstance(project);
 //                    PaletteItemManager itemManager = PaletteItemManager.getInstance(fileForDocument);
 //
 //                    if (itemManager == null)
 //                    {
 //                    	PDPlugin.log("paletteManager not found for file: "+fileForDocument.toString(), new Exception("Stack trace only")); //$NON-NLS-1$ //$NON-NLS-2$
 ////                        PDPlugin.log("paletteManager not found for project: "+project.toString(), new Exception("Stack trace only")); //$NON-NLS-1$ //$NON-NLS-2$
 //                        continue CHILDREN_LOOP;
 //                    }
 
                     final String uri = child.getTagIdentifier().getUri();
                     final String tagName = child.getTagIdentifier().getTagName();
                     final ITagDropSourceData creationProvider =
                         TagToolCreationAdapter.findProviderForContainer(uri, tagName, PaletteItemManager.createPaletteContext(fileForDocument));
                     final ITaglibDomainMetaDataModelContext modelContext = 
                         TaglibDomainMetaDataQueryHelper
                             .createMetaDataModelContext(fileForDocument.getProject(), child.getTagIdentifier().getUri());
 //                    		.createMetaDataModelContext(project, child.getTagIdentifier().getUri());
                     IDOMPosition domPosition = new DOMPosition(_element, childCount++);
                     CreationData creationData = new CreationData(creationProvider,_model, domPosition, modelContext, child);
 
                     ElementCustomizationCommand command = new UserCustomizedElementCustomizationCommand(_model, childNode, creationData);
                     command.execute();
                 }
             }
         }
     }
 
     /**
      * Override to customize the attributes of the tag being created
      * 
      * Default implementation uses meta-data to set the attribute values. This
      * is the preferred method for static attribute values (i.e. those that are
      * not calculated dynamically at runtime).
      * 
      * (non-Javadoc)
      * 
      * @see org.eclipse.jst.pagedesigner.itemcreation.command.ElementCustomizationCommand#applyAttributeCustomization()
      */
     protected void applyAttributeCustomization()
     {
         final Map<String, String> attributes = new HashMap<String, String>();
         addAttributesFromPaletteMetadata(attributes);
         addAttributesFromDropCustomization(attributes);
         /*
          * Added by Pete: Use the attribute data within the customization data
          * container to configure the tag attributes
          */
         for (final Map.Entry<String, String> entry : attributes.entrySet())
         {
             _element.setAttribute(entry.getKey(), entry.getValue());
         }
     }
 
     private void addAttributesFromDropCustomization(
             Map<String, String> attributes)
     {
         if (_creationData.getDropCustomizationData() != null)
         {
             // Pull out the attribute data using the IAdaptable interface
             ICustomizationData data = (ICustomizationData) _creationData
                     .getDropCustomizationData().getAdapter(
                             ICustomizationData.class);
             Map<String, String> attrs = data.getAttributeData().getAttributes();
             // Go through each attribute/value pair and configure it
             for (String id : attrs.keySet())
             {
                 attributes.put(id, attrs.get(id) == null ? "" : attrs.get(id)); //$NON-NLS-1$
             }
         }
     }
 
     private void addAttributesFromPaletteMetadata(Map<String, String> attributes)
     {
         final ITagDropSourceData info = _creationData.getTagCreationProvider();
         if (info != null)
         {
             final MetadataTagDropSourceData provider =
                 TagToolCreationAdapter.createMdTagCreationProvider(info, _model);
 
             final List<TagCreationAttribute> list = provider.getAttributes();
             if (list != null)
             {
                 for (final TagCreationAttribute attr : list)
                 {
                     attributes.put(attr.getId(), (attr.getValue() == null ? "" //$NON-NLS-1$
                             : attr.getValue()));
                 }
             }
         }
     }
 
 }
