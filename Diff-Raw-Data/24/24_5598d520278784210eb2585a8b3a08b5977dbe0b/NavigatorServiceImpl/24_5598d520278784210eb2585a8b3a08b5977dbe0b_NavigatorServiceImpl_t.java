 package org.otherobjects.cms.workbench;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.otherobjects.cms.dao.DynaNodeDao;
 import org.otherobjects.cms.model.DynaNode;
 import org.otherobjects.cms.util.StringUtils;
 import org.springframework.util.Assert;
 
 public class NavigatorServiceImpl implements NavigatorService
 {
     private DynaNodeDao dynaNodeDao;
 
     public WorkbenchItem getRootItem()
     {
         Object o = dynaNodeDao.getByPath("/site");
         return (WorkbenchItem) o;
     }
 
     public List<WorkbenchItem> getSubContainers(String uuid)
     {
         DynaNode parent = dynaNodeDao.get(uuid);
         List<DynaNode> all = dynaNodeDao.getAllByPath(parent.getJcrPath());
 
         List<WorkbenchItem> children = new ArrayList<WorkbenchItem>();
         for (DynaNode n : all)
         {
             if (n.getOoType().equals("Folder"))
                 children.add(n);
         }
         return children;
     }
 
     public WorkbenchItem addItem(String uuid, String name)
     {
         DynaNode parent = dynaNodeDao.get(uuid);
         Assert.notNull(parent, "Parent node not found: " + uuid);
 
         int c = 0;
         do
         {
             String newPath = parent.getJcrPath() + "/untitled-" + ++c;
             boolean alreadyExists = (dynaNodeDao.existsAtPath(newPath));
             if(!alreadyExists) break;
             
         } while(true);
 
         DynaNode newFolder = dynaNodeDao.create("Folder");
         newFolder.setPath(parent.getJcrPath());
         newFolder.setCode("untitled-" + c); //TODO M2 Auto generate
         newFolder.setLabel("Untitled " + c);
         return dynaNodeDao.save(newFolder);
     }
 
     public WorkbenchItem getItem(String path)
     {
         return null;
     }
 
    public List<WorkbenchItem> getItemContents(String uuid)
     {
        DynaNode node = dynaNodeDao.get(uuid);
        
        List<DynaNode> all = dynaNodeDao.getAllByPath(node.getPath());

        List<WorkbenchItem> children = new ArrayList<WorkbenchItem>();
        for (DynaNode n : all)
        {
            if (!n.getOoType().equals("Folder"))
                children.add(n);
        }
        return children;
     }
 
     public WorkbenchItem renameItem(String uuid, String newName)
     {
         DynaNode node = dynaNodeDao.get(uuid);
         node.setCode(StringUtils.generateUrlCode(newName));
         node.set("label", newName);
         dynaNodeDao.save(node);
         return null;
     }
 
     public void moveItem(String itemId, String targetId, String point)
     {
         dynaNodeDao.moveItem(itemId, targetId, point);
     }
     
     public void removeItem(String path)
     {
     }
 
     public DynaNodeDao getDynaNodeDao()
     {
         return dynaNodeDao;
     }
 
     public void setDynaNodeDao(DynaNodeDao dynaNodeDao)
     {
         this.dynaNodeDao = dynaNodeDao;
     }
 
 }
