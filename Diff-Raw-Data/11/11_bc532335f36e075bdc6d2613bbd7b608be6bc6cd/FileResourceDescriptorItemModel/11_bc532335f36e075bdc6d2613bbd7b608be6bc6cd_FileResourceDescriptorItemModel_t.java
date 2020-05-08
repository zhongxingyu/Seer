 package com.delcyon.capo.webapp.models;
 
 import java.util.List;
 
 import com.delcyon.capo.resourcemanager.ResourceDescriptor;
 import com.delcyon.capo.resourcemanager.types.ContentMetaData;
 import com.delcyon.capo.webapp.models.DomItemModel.DomUse;
 
 import eu.webtoolkit.jwt.ItemDataRole;
 import eu.webtoolkit.jwt.Orientation;
 import eu.webtoolkit.jwt.SortOrder;
 import eu.webtoolkit.jwt.WAbstractItemModel;
 import eu.webtoolkit.jwt.WModelIndex;
 /**
  * 
  * @author jeremiah
  *To implement a custom model, you need to reimplement the following methods:
 
 getIndex() and getParent() methods that allow one to navigate the model
 getColumnCount() and getRowCount() to specify the top level geometry and the nested geometry at every item
 getData() to return the data for an item
 optionally, getHeaderData() to return row and column header data
 optionally, getFlags() to indicate data options
  */
 public class FileResourceDescriptorItemModel extends WAbstractItemModel
 {
 
 	int indexCounter = 0;
 	private ResourceDescriptor topLevelResourceDescriptor;
 	private DomUse domUse = null;
 	public FileResourceDescriptorItemModel(ResourceDescriptor resourceDescriptor,DomItemModel.DomUse navigation)
 	{	    
 		this.topLevelResourceDescriptor = resourceDescriptor;
 		this.domUse = navigation;
 	}
 	
 	public void reload()
 	{
 	    reset();
 	}
 	
 	public ResourceDescriptor getTopLevelResourceDescriptor()
     {
         return topLevelResourceDescriptor;
     }
 	
 	@Override
     public Object getHeaderData(int section, Orientation orientation, int role)
     {
         if (section == 0 && role == ItemDataRole.DisplayRole)
         {
             return topLevelResourceDescriptor.getLocalName();
         }
         else
         {
             return null;
         }
     }
 	
 	@Override
 	public int getColumnCount(WModelIndex parent)
 	{
 		if (domUse == DomUse.NAVIGATION)
 		{
 			return 1;
 		}
 		else if (domUse == DomUse.ATTRIBUTES)
 		{
 			return 2;
 		}
 		else
 		{
 			try
             {
                 return topLevelResourceDescriptor.getContentMetaData(null).getSupportedAttributes().size();
             }
             catch (Exception e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
                 return 1;
             }
 		}
 	}
 
 	@Override
 	public int getRowCount(WModelIndex parent)
 	{
 		ResourceDescriptor parentNode = null;
 		if(parent == null)
 		{
 			parentNode = topLevelResourceDescriptor;						
 		}
 		else
 		{
 			parentNode = (ResourceDescriptor) parent.getInternalPointer();
 		}
 		
 		if (domUse == DomUse.ATTRIBUTES)
 		{
 			if(parentNode instanceof ResourceDescriptor)
 			{
 				try
                 {
 				    if (parentNode.getResourceMetaData(null).isContainer() == false)
 	                {
 				        if(parentNode.getContentMetaData(null) == null)
 				        {
 				            return parentNode.getResourceMetaData(null).getSupportedAttributes().size();
 				        }
 				        else
 				        {
 				            return parentNode.getContentMetaData(null).getSupportedAttributes().size();
 				        }
 	                }
                     return parentNode.getResourceMetaData(null).getSupportedAttributes().size();
                 }
                 catch (Exception e)
                 {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                     return 0;
                 }
 			}
 			else
 			{
 				return 0;
 			}
 		}
 		else
 		{
 			try
             {
 			    if (parentNode.getResourceMetaData(null).isContainer() == false)
 			    {
 			        return 0;
 			    }
                 return parentNode.getResourceMetaData(null).getContainedResources().size();
             }
             catch (Exception e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
                 return 0;
             }
 		}
 	}
 
 	@Override
 	public void sort(int column, SortOrder order)
 	{
 	    
 	    // TODO Auto-generated method stub
 	    super.sort(column, order);
 	}
 	
 	@Override
 	//Should be called get this index's parent index
 	public WModelIndex getParent(WModelIndex index)
 	{
 	    try
 	    {
 	        
 	        if(index.getInternalPointer() instanceof ContentMetaData)
 	        {
 	            return null;
 	        }
 	        ResourceDescriptor node = (ResourceDescriptor) index.getInternalPointer();
 	        if(node.getParentResourceDescriptor() == null)
 	        {
 	            return null;
 	        }
 	        //check for this since were looking for the index of our parent, and if our parent isn't the child of someone, then it has no valid index
 	        else if(node.getParentResourceDescriptor().getParentResourceDescriptor() == null)
 	        {
 	            return null;
 	        }
 
 
 	        node = (ResourceDescriptor) node.getParentResourceDescriptor();
 
 
 
 
 	        ResourceDescriptor grandParentNode = (ResourceDescriptor) node.getParentResourceDescriptor();
 	        List<ContentMetaData> children = grandParentNode.getResourceMetaData(null).getContainedResources();
 	        for(int currentChild = 0; currentChild < children.size(); currentChild++)
 	        {
 	            if(children.get(currentChild).getResourceURI().equals(node.getResourceURI()))
 	            {
 	                return createIndex(currentChild, 0, node);
 	            }
 	        }
 	    } catch (Exception exception)
 	    {
 	        exception.printStackTrace();
 	    }
 		return null;
 			
 		 
 	}
 
 	@Override
 	public Object getData(WModelIndex index, int role)
 	{
 	    if (role == ItemDataRole.DisplayRole)
 	    {	
 	        if (domUse == DomUse.ATTRIBUTES)
 	        {
 	            ContentMetaData attr = (ContentMetaData) index.getInternalPointer();
 	            if (index.getColumn() == 0)
 	            {
 	                return attr.getSupportedAttributes().get(index.getRow());
 	            }
 	            else
	            {	
	            	if(attr.getSupportedAttributes().get(index.getRow()).startsWith("jcr:data"))
	            	{
	            		return "DATA";
	            	}
	            	else
	            	{
	            		return attr.getValue(attr.getSupportedAttributes().get(index.getRow()));
	            	}
 	            }
 	        }
 	        else
 	        {
 
 	            return ((ResourceDescriptor)index.getInternalPointer()).getLocalName();
 	        }
 	    }
 	    else if (role == ItemDataRole.MimeTypeRole)
 	    {
 	        if (domUse == DomUse.ATTRIBUTES)
 	        {
 	            return null;
 	        }
 	        else
 	        {
 	            try
 	            {
 	                if (((ResourceDescriptor)index.getInternalPointer()).getResourceMetaData(null).isContainer() == false)
 	                {
 	                    return ((ResourceDescriptor)index.getInternalPointer()).getContentMetaData(null).getValue("mimeType");
 	                }
 	            }catch (Exception exception)
 	            {
 	                exception.printStackTrace();
 
 	            }                
 	        }
 	    }		
 	    return null;
 
 	}
 
 	@Override
 	//we're making an index here using the our id as the ptr in create index
 	public WModelIndex getIndex(int row, int column, WModelIndex parent)
 	{
 	    
 	    try
 	    {
 	        if (domUse == DomUse.ATTRIBUTES)
 	        {
 	            //if (parent == null)
 	            {
 	                return createIndex(row, column, topLevelResourceDescriptor.getResourceMetaData(null));
 	            }
 //	            else
 //	            {
 //	                
 //	            }
 	        }
 	        else
 	        {
 	            ResourceDescriptor parentResourceDescriptor = null;
 	            if (parent != null)
 	            {
 	                parentResourceDescriptor = (ResourceDescriptor) parent.getInternalPointer();
 	            }
 	            else
 	            {
 	                parentResourceDescriptor = topLevelResourceDescriptor;
 	                //return createIndex(row, column, parentResourceDescriptor);
 	            }
 
 	            List<ContentMetaData> childResources = parentResourceDescriptor.getResourceMetaData(null).getContainedResources();
 	            
 	            if (childResources.size() > 0)
 	            {
 	                System.out.println("blah");
 	                ResourceDescriptor childResourceDescriptor = (ResourceDescriptor) parentResourceDescriptor.getChildResourceDescriptor(null, childResources.get(row).getResourceURI().getResourceURIString());
 	                if (childResourceDescriptor == null || childResourceDescriptor.getResourceMetaData(null) == null)
 	                {
 	                    System.out.println("HMMMMMMMMMMM");
 	                }
 	                if (column > 0)
 	                {
 	                    return createIndex(row, column, childResourceDescriptor.getResourceMetaData(null));
 	                }
 	                else
 	                {
 	                    return createIndex(row, column, childResourceDescriptor);	
 	                }
 	            }
 	            
 	            else
 	            {
 	                return null;
 	            }
 	        }
 	    }catch (Exception exception)
 	    {
 	        exception.printStackTrace();
 	        return null;
 	    }
 	}
 
 	@Override
 	protected WModelIndex createIndex(int row, int column, Object ptr)
 	{
 	    
 	    return super.createIndex(row, column, ptr);
 	}
 
 	@Override
 	public void beginRemoveRows(WModelIndex parent, int first, int last)
 	{
 	    // TODO Auto-generated method stub
 	    super.beginRemoveRows(parent, first, last);
 	}
 	
     public void fireDataChanged(WModelIndex parentIndex,boolean append)
     {
         //reload();
         if(append)
         {
             beginInsertRows(parentIndex, getRowCount(parentIndex)-1, getRowCount(parentIndex)-1);
          //   dataChanged().trigger(parentIndex, parentIndex);
             endInsertRows();
         }
         else
         {            
             endRemoveRows();
         }
         //int rowCount = getRowCount(parentIndex); //should be number of children
         //WModelIndex childIndex = getIndex(rowCount-1, 0, parentIndex);
 //        if (rowCount > 0)
 //        {   
 //            beginInsertRows(index, getRowCount(index)-1, getRowCount(index));
 //            endInsertRows();
 //            //
 //        }
 //        else
         //dataChanged().trigger(parentIndex,childIndex);
 //        {
 //            beginInsertRows(index, rowCount,rowCount);
 //            endInsertRows();
 //        }
         
     }
 
 }
