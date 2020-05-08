 package com.delcyon.capo.webapp.widgets;
 
 import java.util.List;
 import java.util.SortedSet;
 import java.util.logging.Level;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor;
 import com.delcyon.capo.resourcemanager.ResourceType;
 import com.delcyon.capo.resourcemanager.ResourceURI;
 import com.delcyon.capo.resourcemanager.types.JcrResourceType;
 import com.delcyon.capo.webapp.models.DomItemModel.DomUse;
 import com.delcyon.capo.webapp.models.ResourceDescriptorItemModel;
 import com.delcyon.capo.webapp.servlets.CapoWebApplication;
 
 import eu.webtoolkit.jwt.AlignmentFlag;
 import eu.webtoolkit.jwt.MatchOptions;
 import eu.webtoolkit.jwt.WApplication;
 import eu.webtoolkit.jwt.WCompositeWidget;
 import eu.webtoolkit.jwt.WContainerWidget;
 import eu.webtoolkit.jwt.WGridLayout;
 import eu.webtoolkit.jwt.WLink;
 import eu.webtoolkit.jwt.WLink.Type;
 import eu.webtoolkit.jwt.WModelIndex;
 import eu.webtoolkit.jwt.WMouseEvent;
 import eu.webtoolkit.jwt.WPushButton;
 
 /**
  * This Widget combines a resource tree and a resource editor.
  * It automatically  
  * @author jeremiah
  *
  */
 public class WCapoResourceExplorer extends WCompositeWidget
 {
     private ResourceType resourceType = new JcrResourceType();
     private WContainerWidget container = new WContainerWidget();
     private WGridLayout contentPaneLayout;
     private WCapoResourceTreeView capoResourceTreeView;    
     private WCapoResourceEditor capoResourceEditor;
     private WPushButton resetButton;
     private WPushButton saveButton;
     
     public WCapoResourceExplorer()
     {
         setImplementation(container);
         setMargin(0);
         container.setLayout(getResourceExplorerLayout());
         //container.setAttributeValue("style", "background-image: url('/wr/images/background.png'); background-repeat: no-repeat; background-position: bottom right; background-size: contain;");
         getResourceExplorerLayout().addWidget(getCapoResourceTreeView(), 0, 0,1,0);
         getResourceExplorerLayout().addWidget(getResourceEditor(), 0, 1);
         getResourceExplorerLayout().addWidget(getSaveButton(), 2, 0, 1, 1, AlignmentFlag.AlignTop); 
         getResourceExplorerLayout().addWidget(getResetButton(), 3, 0, 1, 1, AlignmentFlag.AlignTop);
         WApplication.getInstance().internalPathChanged().addListener(this,this::processInternalPathChange);
     }
     
     private WGridLayout getResourceExplorerLayout() {
         if (contentPaneLayout == null)
         {
             contentPaneLayout = new WGridLayout();          
             contentPaneLayout.setColumnStretch(1, 1);
             contentPaneLayout.setContentsMargins(0, 0, 0, 0);
             contentPaneLayout.setColumnResizable(0);
             contentPaneLayout.setRowStretch(1, 1);
             contentPaneLayout.setRowStretch(0, 1);
         }
          return contentPaneLayout;
     }
     
     private  WCapoResourceTreeView getCapoResourceTreeView() {
         if(capoResourceTreeView == null)
         {
             capoResourceTreeView = new WCapoResourceTreeView();
             //watch for selection change events
             capoResourceTreeView.selectionChanged().addListener(this, this::selectedItemChanged);
             //watch for internal patch change requests from tree
             capoResourceTreeView.doubleClicked().addListener(this, this::processTreeDoubleClick);
         }       
 
         return capoResourceTreeView;
     }
  
     private WCapoResourceEditor getResourceEditor() 
     {
         if (capoResourceEditor == null)
         {
             capoResourceEditor = new WCapoResourceEditor();
         
         }
         return capoResourceEditor;
     }
     
     /**
      * Set the kind of resource that we're dealing with, this is mostly used to figure out what kind od scheme to use when making paths.
      * The default is JcrResourceType 
      * @param resourceType
      */
     public void setResourceType(ResourceType resourceType)
     {
         this.resourceType = resourceType;
     }
     
     public ResourceType getResourceType()
     {
         return resourceType;
     }
     
     /**
      * 
      * @return Resource scheme plus a ':'
      */
     private String getResourceTypeScheme()
     {
         return getResourceType().getName()+":";
     }
     
     /**
     * Process request for tree root/internal path changes from tree
     * @param modelIndex
     * @param mouseEvent
     */
    private void processTreeDoubleClick(WModelIndex modelIndex, WMouseEvent mouseEvent)
    {
        //update the internal path with the our resource URI. by allowing this to emit processInternalPathChange() also get called
        WApplication.getInstance().setInternalPath(((ResourceDescriptor)modelIndex.getInternalPointer()).getResourceURI().getPath(), true);
    }
    
    /**
     * sends selection changes in the tree to the resource editor
     */
    private void selectedItemChanged()
    {
        SortedSet<WModelIndex> selectedIndexes = getCapoResourceTreeView().getSelectedIndexes();
        if(selectedIndexes.size() == 0)
        {
            return;
        }
        else if (selectedIndexes.size() == 1)
        {
            WModelIndex modelIndex = selectedIndexes.first();
            final Object selectedItem =  modelIndex.getInternalPointer();           
           getResourceEditor().setModel(selectedItem); //TODO add some sort of event/Signal listener to process this so that we don't have to know so much about models and events           
        }
        refresh();
    }
    
    private WPushButton getSaveButton()
    {
        if(saveButton == null)
        {
            saveButton = new WPushButton("Save");
            saveButton.clicked().addListener(this, this::saveSession);
        }
        return saveButton;
    }
    
    /**
     * saves current jcrSession, TODO this should really work with the current users workspace or something.
     */
    private void saveSession()
    {
        try
        {
            ((CapoWebApplication) CapoWebApplication.getInstance()).getJcrSession().save();
        } catch (Exception e)
        {
     	   CapoWebApplication.exception(Level.SEVERE, "Error saving JCR session", e);
        }
    }
    
    private WPushButton getResetButton()
    {
        
        if(resetButton == null)
        {
            resetButton = new WPushButton("Reset");
            resetButton.setLink(new WLink(Type.InternalPath, "/"));
            resetButton.clicked().addListener(this, this::reset);           
        }
        return resetButton;
    }
    
    /**
     * Resets session and tree view to root
     */
    private void reset()
    {
        try
        {
            //refresh the jcr session, wiping all unsaved changes
            ((CapoWebApplication) CapoWebApplication.getInstance()).getJcrSession().refresh(false);
            //reload the tree view
            ((ResourceDescriptorItemModel) capoResourceTreeView.getModel()).reload();
            capoResourceTreeView.setModel(new ResourceDescriptorItemModel(CapoApplication.getDataManager().getResourceDescriptor(null, getResourceTypeScheme()+"/"),DomUse.NAVIGATION));
        } catch (Exception e)
        {
     	   CapoWebApplication.exception(Level.SEVERE, "Error resetting", e);
        }
    }
 
    public void setRootResourceDescriptor(ResourceDescriptor resourceDescriptor)
    {
        setResourceType(resourceDescriptor.getResourceType());
        getCapoResourceTreeView().setRootResourceDescriptor(resourceDescriptor);
 
    }
  
    
    /**
     * Deal with any internal path changes in the system. Make sure that the proper components and resources are loaded and views are show etc
     */
    private void processInternalPathChange()
    {
        System.out.println(WApplication.getInstance().getInternalPath());
        //skip and changes that happen when we're not visible
        if(isVisible() == false)
        {
     	   return;
        }
        try
        {
            //TODO make this deal with resource that are not only of type repo: 
            ResourceDescriptor resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null, getResourceTypeScheme()+WApplication.getInstance().getInternalPath());
            ResourceURI originalURI = resourceDescriptor.getResourceURI();
            //we don't ever want to set the root to anything that isn't a container. So if it isn't use its parent who should be
            if(resourceDescriptor.getResourceMetaData(null).isContainer() == false)
            {
                if(resourceDescriptor.getParentResourceDescriptor() != null)
                {
                    resourceDescriptor = resourceDescriptor.getParentResourceDescriptor();
                }
                else
                {
                    String parentURI = resourceDescriptor.getResourceURI().getResourceURIString().replaceAll("/"+resourceDescriptor.getLocalName(), "");
                    if(parentURI.equals(getResourceTypeScheme())) //make sure we have a root for the repo
                    {
                        parentURI = getResourceTypeScheme()+"/";
                    }
                    
                    resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null,parentURI);
                    WApplication.getInstance().setInternalPath(resourceDescriptor.getResourceURI().getPath());
                }
            }
            setRootResourceDescriptor(resourceDescriptor);
            if(originalURI != null)
            {
         	   WModelIndex currentIndex = getCapoResourceTreeView().getModel().getIndex(0, 0);
         	   if(currentIndex != null) //not sure why this can be null, it isn't very often
         	   {
         		   List<WModelIndex> indexes = getCapoResourceTreeView().getModel().match(currentIndex, ResourceDescriptorItemModel.ResourceURI_ROLE, originalURI.toString(), 1, MatchOptions.defaultMatchOptions);
         		   if(indexes.size() > 0)
         		   {
         			   getCapoResourceTreeView().select(indexes.get(0));
         		   }
         	   }
            }
            getCapoResourceTreeView().selectionChanged();
            refresh();
        }
        catch (Exception e)
        {                         
     	   CapoWebApplication.exception(Level.SEVERE, "Error processing internal path change", e);
        }
    
    }
    
 }
