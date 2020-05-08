 package org.minispm.admin.organization.service;
 
 import org.minispm.admin.organization.dao.AccountabilityDao;
 import org.minispm.admin.organization.dao.StaffDao;
 import org.minispm.admin.organization.dao.UnitDao;
 import org.minispm.admin.organization.entity.*;
 import org.minispm.admin.organization.utils.UnitForTree;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.*;
 
 /**
  * User: shibaoxu
  * Date: 12-12-2
  * Time: 下午8:19
  */
 @Service
 @Transactional(readOnly = true)
 public class OrganizationService {
 
     private AccountabilityDao accountabilityDao;
     private UnitDao unitDao;
     private StaffDao staffDao;
 
     /*=========Accountability===============*/
     /*获得某个组织架构的跟节点*/
     public Unit getRoot(String accountabilityTypeid){
 
         List<Accountability> accountabilities = accountabilityDao.findRoot(accountabilityTypeid);
         if (accountabilities.size() > 0){
             return accountabilities.get(0).getChild();
         }else {
             return null;
         }
     }
 
     public List<Unit> getChildren(String parentId, String accountabilityId){
         List<Accountability> accountabilities = accountabilityDao.getChildren(parentId, accountabilityId);
         List<Unit> units = new ArrayList<Unit>();
         for(Accountability accountability : accountabilities){
             units.add(accountability.getChild());
         }
         return units;
     }
 
     /**
      * 获取所
      * @param staffId
      * @param accountabilityTypeId
      * @return
      */
     public List<Unit> getBelongAndDescendantOrg(String staffId, String accountabilityTypeId){
         Unit belongOrg = getBelongOrg(accountabilityTypeId, staffId);
         List<Unit> descendants = new ArrayList<Unit>();
         descendants.add(belongOrg);
         getChildOrg(belongOrg.getId(), accountabilityTypeId, descendants);
         return descendants;
     }
 
     private void getChildOrg(String unitId, String accountabilityTypeId, List<Unit> descendants){
         List<Unit> children = getChildren(unitId, accountabilityTypeId);
         for(Unit unit : children){
             if(unit instanceof Company || unit instanceof Department){
                 descendants.add(unit);
                 getChildOrg(unit.getId(), accountabilityTypeId, descendants);
             }
         }
     }
 
     public List<Staff> getSelfAndDescendantStaff(String staffId, String accountabilityTypeID){
         Staff self = staffDao.findOne(staffId);
         if (self == null){
             throw  new RuntimeException("编号为{" + staffId + "}的员工不存在");
         }
         List<Staff> descendants = new ArrayList<Staff>();
         descendants.add(self);
         getDescendantsStaff(staffId, accountabilityTypeID, descendants);
         return descendants;
     }
 
 
 
     private void getDescendantsStaff(String staffId, String accountabilityTypeId, List<Staff> descendants){
         List<Unit> children = getChildren(staffId, accountabilityTypeId);
         for(Unit unit : children){
             if(unit instanceof Company || unit instanceof Department){
                 throw new RuntimeException("违反组织结构规则，员工的下级只能是员工");
             }else{
                 descendants.add((Staff)unit);
                 getDescendantsStaff(unit.getId(), accountabilityTypeId, descendants);
             }
         }
     }
 
 
     public Unit getParent(String accountabilityTypeId, String unitId){
         Accountability parent = accountabilityDao.getParent(accountabilityTypeId, unitId);
         if (null != parent){
             return parent.getParent();
         }else {
             return null;
         }
     }
 
     public Unit getBelongOrg(String accountabilityTypeId, String staffId){
         Unit unit = unitDao.findOne(staffId);
         if(unit instanceof Company || unit instanceof Department){
             throw new RuntimeException("数据出现错误，该ID对应为公司或部门，应该对应员工");
         }
 
         Unit parent = getParent(accountabilityTypeId, staffId);
         while(parent != null){
             if(parent instanceof Department || parent instanceof Company){
                 return parent;
             }else{
                 parent = getParent(accountabilityTypeId, parent.getId());
                 continue;
             }
         }
         return null;
     }
 
     public Set<Unit> getBelongAndLowOrg(String accountabilityTypeId, String staffId){
         return null;
     }
 
     public UnitForTree getStructure(String accountabilityTypeId){
//        Unit root = getRoot(accountabilityTypeId);
//        UnitForTree unitTree  = UnitForTree.buildStructure(accountabilityDao.findByAccountabilityTypeId(accountabilityTypeId), orgAccountabilityType, root);
//        return unitTree;
        return null;
     }
 
     @Autowired
     public void setStaffDao(StaffDao staffDao) {
         this.staffDao = staffDao;
     }
 
     @Autowired
     public void setAccountabilityDao(AccountabilityDao accountabilityDao) {
         this.accountabilityDao = accountabilityDao;
     }
 
     @Autowired
     public void setUnitDao(UnitDao unitDao) {
         this.unitDao = unitDao;
     }
 }
