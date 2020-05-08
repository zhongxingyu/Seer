 /*
  * com.percussion.pso.utils PSOSlotContents.java
  *  
  * @author DavidBenua
  *
  */
 package com.percussion.pso.utils;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.percussion.cms.objectstore.PSAaRelationship;
 import com.percussion.cms.objectstore.PSRelationshipFilter;
 import com.percussion.services.guidmgr.IPSGuidManager;
 import com.percussion.services.guidmgr.PSGuidManagerLocator;
 import com.percussion.utils.guid.IPSGuid;
 import com.percussion.webservices.PSErrorException;
 import com.percussion.webservices.content.IPSContentWs;
 import com.percussion.webservices.content.PSContentWsLocator;
 
 /**
  * Tool for loading the contents of a slot as PSAaRelationship objects.
  * 
  * The standard load method in Web Services 
  * {@link IPSContentWs#loadContentRelations(PSRelationshipFilter, boolean)}
  * loads all content relationships, regardless of slot.  This tool uses this method
  * and returns a filtered and sorted list of <code>PSAaRelationship</code> 
  * objects.
  * <p>
  * Note that this tool is based on relationships and does not work on autoslots.  
  * If you are assembling a item, consider using {@link com.percussion.pso.jexl.PSOSlotTools#getSlotContents(com.percussion.services.assembly.IPSAssemblyItem, String, java.util.Map)}
  * instead of this method. 
  * <p>
  * The returned slot has not been filtered by any item filters, and the revisions
  * of the dependent items will not have been set. 
  * <p>
  * The implementation of this method is highly dependent on the sortrank property
  * of the relationships having been set correctly. The behavior when the sortrank
  * is missing or invalid (e.g. 0 or -1) may be inconsistent.   
  *  
  * 
  * 
  *
  * @see com.percussion.webservices.content.IPSContentWs#loadContentRelations(PSRelationshipFilter, boolean)
  * @author DavidBenua
  *
  */
 public class PSOSlotContents
 {
    private static Log log = LogFactory.getLog(PSOSlotContents.class);
    
    private static IPSContentWs cws = null; 
    private static IPSGuidManager gmgr = null; 
    
    /**
     * Default constructor.  
     */
    public PSOSlotContents()
    {
    }
    
   
    /**
     * Gets the contents of a slot. 
     * @param parentItem the parent item
     * @param slot the slot 
     * @return all relationships in the given slot for this parent. 
     * Never <code>null</code>. May be <code>empty</code>.
     * @throws PSErrorException
     */
    public List<PSAaRelationship> getSlotContents(IPSGuid parentItem, IPSGuid slot) 
       throws PSErrorException
    {
       initServices();
 
       SortedSet<PSAaRelationship> slotRelations = 
          new TreeSet<PSAaRelationship>(new SlotItemComparator());
       
       PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY); 
      filter.setOwner(gmgr.makeLocator(parentItem)); 
       
       // load ALL AA relations for the parent item
       // Note that Slot will be null unless we load the reference info. 
       List<PSAaRelationship> allRelations = cws.loadContentRelations(filter, true);  
       
       for(PSAaRelationship rel : allRelations)
       {
         if(rel.getSlotId() == slot)
         { //this item is in our slot. Order will be determined by the comparator. 
             slotRelations.add(rel); 
          }
       }
       
       //we just need our slot as a list.  
       List<PSAaRelationship> outputRelations = new ArrayList<PSAaRelationship>(slotRelations); 
       return outputRelations; 
    }
    
    
    private static void initServices()
    {
       if(cws == null)
       {
          gmgr = PSGuidManagerLocator.getGuidMgr(); 
          cws = PSContentWsLocator.getContentWebservice(); 
       }
    }
    /**
     * Compares two AA Relationships by sort rank. Since this comparator depends solely
     * on the sort rank, it may not be consistent with the contract of the Set interface. 
     * 
     * Note: this comparator imposes orderings that are inconsistent with equals.
     * 
     * @author DavidBenua
     *
     */
    protected class SlotItemComparator implements Comparator<PSAaRelationship>
    {
       public SlotItemComparator()
       {
          
       }
 
       /**
        * Compares PSAaRelationships by sort rank. 
        * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
        */
       public int compare(PSAaRelationship rel1, PSAaRelationship rel2)
       {
          if(rel1 == null || rel2 == null)
          {
             String emsg = "cannot compare null relationships"; 
             log.error(emsg);
             throw new IllegalArgumentException(emsg);
          }
          int sr1 = rel1.getSortRank(); 
          int sr2 = rel2.getSortRank(); 
          
          if(sr1 == sr2)
             return 0;
          if(sr1 < sr2)
             return -1;
          return 1; 
       }
 
       /**
        * All SlotItemComparators return the same order. 
        * @see java.lang.Object#equals(java.lang.Object)
        */
       @Override
       public boolean equals(Object obj)
       {
          if(obj instanceof SlotItemComparator)
          {
             return true;
          }
          return super.equals(obj);
       }
       
    }
    /**
     * @param cws the cws to set. Used for testing. 
     */
    public void setCws(IPSContentWs cws)
    {
       PSOSlotContents.cws = cws;
    }
 
 
    /**
     * @param gmgr the gmgr to set. Used for testing
     */
    public void setGmgr(IPSGuidManager gmgr)
    {
       PSOSlotContents.gmgr = gmgr;
    }
    
    
 }
