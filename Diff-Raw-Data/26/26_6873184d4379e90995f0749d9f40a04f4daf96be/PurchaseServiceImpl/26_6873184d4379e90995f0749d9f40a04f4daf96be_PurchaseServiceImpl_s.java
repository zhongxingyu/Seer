 package com.xone.service.app;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.time.DateUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.xone.model.hibernate.app.ImageUploadedDao;
 import com.xone.model.hibernate.app.PersonDao;
 import com.xone.model.hibernate.app.PurchaseCheckDao;
 import com.xone.model.hibernate.app.PurchaseDao;
 import com.xone.model.hibernate.entity.ImageUploaded;
 import com.xone.model.hibernate.entity.Person;
 import com.xone.model.hibernate.entity.Purchase;
 import com.xone.model.hibernate.entity.PurchaseCheck;
 import com.xone.model.hibernate.support.Pagination;
 import com.xone.service.app.utils.MyServerUtils;
 
 public class PurchaseServiceImpl implements PurchaseService {
 
     private static final Log log = LogFactory.getLog(PurchaseServiceImpl.class);
 
     @Autowired
     protected PurchaseDao purchaseDao;
 
     @Autowired
     protected ImageUploadedDao imageUploadedDao;
 
     @Autowired
     protected PersonDao personDao;
 
     @Autowired
     PurchaseCheckDao purchaseCheckDao;
 
     public PersonDao getPersonDao() {
         return personDao;
     }
 
     public void setPersonDao(PersonDao personDao) {
         this.personDao = personDao;
     }
 
     public PurchaseDao getPurchaseDao() {
         return purchaseDao;
     }
 
     public void setPurchaseDao(PurchaseDao purchaseDao) {
         this.purchaseDao = purchaseDao;
     }
 
     public ImageUploadedDao getImageUploadedDao() {
         return imageUploadedDao;
     }
 
     public void setImageUploadedDao(ImageUploadedDao imageUploadedDao) {
         this.imageUploadedDao = imageUploadedDao;
     }
     
     /* (non-Javadoc)
      * @see com.xone.service.app.PurchaseService#updateFlagDeletedWhenExpired()
      */
     @Override
     public void updateFlagDeletedWhenExpired() {
         DetachedCriteria c = DetachedCriteria.forClass(Purchase.class);
         c.add(Restrictions.le("purchaseValid", new Date()));
         c.add(Restrictions.eq("flagDeleted", Purchase.FlagDeleted.NORMAL.getValue()));
         List<Purchase> l = getPurchaseDao().findByDetachedCriteria(c);
         for(Purchase p : l){
             p.setFlagDeleted(Purchase.FlagDeleted.DELETED.getValue());
             getPurchaseDao().update(p);
         }
     }
 
     @Override
     public Purchase save(Purchase entity) {
         entity.setCheckStatus(Purchase.CheckStatus.WAITING.getValue());
         entity.setFlagDeleted(Purchase.FlagDeleted.NORMAL.getValue());
         return getPurchaseDao().save(entity);
     }
 
     @Override
     public List<Purchase> save(List<Purchase> entity) {
         for (Iterator<Purchase> iterator = entity.iterator(); iterator.hasNext();) {
             Purchase purchase = (Purchase) iterator.next();
             purchase.setCheckStatus(Purchase.CheckStatus.WAITING.getValue());
             purchase.setFlagDeleted(Purchase.FlagDeleted.NORMAL.getValue());
         }
         return getPurchaseDao().save(entity);
     }
 
     @Override
     public Purchase save(Purchase entity, ImageUploaded imageUploaded) {
         entity.setCheckStatus(Purchase.CheckStatus.WAITING.getValue());
         entity.setFlagDeleted(Purchase.FlagDeleted.NORMAL.getValue());
         entity.setPurchaseValid(DateUtils.addDays(new Date(), 30));
         entity = getPurchaseDao().save(entity);
         imageUploaded.setRefId(entity.getId());
         imageUploaded.setRefType(ImageUploaded.RefType.PURCHASE.getValue());
         imageUploaded.setFlagDeleted(ImageUploaded.FlagDeleted.NORMAL.getValue());
         imageUploaded = getImageUploadedDao().save(imageUploaded);
         entity.getIds().add(imageUploaded.getId());
         return entity;
     }
 
     @Override
     public Purchase save(Purchase entity, List<ImageUploaded> imageUploadeds) {
         entity.setCheckStatus(Purchase.CheckStatus.WAITING.getValue());
         entity.setFlagDeleted(Purchase.FlagDeleted.NORMAL.getValue());
         entity.setPurchaseValid(DateUtils.addDays(new Date(), 30));
         entity = getPurchaseDao().save(entity);
         for (ImageUploaded imageUploaded : imageUploadeds) {
             imageUploaded.setRefId(entity.getId());
             imageUploaded.setRefType(ImageUploaded.RefType.PURCHASE.getValue());
             imageUploaded.setFlagDeleted(ImageUploaded.FlagDeleted.NORMAL.getValue());
         }
         imageUploadeds = getImageUploadedDao().save(imageUploadeds);
         List<Long> ids = new ArrayList<Long>();
         for (ImageUploaded imageUploaded : imageUploadeds) {
             ids.add(imageUploaded.getId());
         }
         entity.setIds(ids);
         return entity;
     }
 
     public Purchase update(Purchase entity, List<ImageUploaded> imageUploadeds, List<Long> imageIds) {
         for (ImageUploaded imageUploaded : imageUploadeds) {
             imageUploaded.setRefId(entity.getId());
             imageUploaded.setRefType(ImageUploaded.RefType.PURCHASE.getValue());
             imageUploaded.setFlagDeleted(ImageUploaded.FlagDeleted.NORMAL.getValue());
         }
         imageUploadeds = getImageUploadedDao().save(imageUploadeds);
         Map<Long, Boolean> imageIdMap = new HashMap<Long, Boolean>();
         List<Long> ids = new ArrayList<Long>();
         for (Long id : imageIds) {
         	if (null == id) {
         		continue;
         	}
             imageIdMap.put(id, true);
         }
         for (Long id : entity.getIds()) {
             if (null == imageIdMap.get(id)) {
                 getImageUploadedDao().deleteLogicById(id);
             } else {
                 ids.add(id);
             }
         }
         for (ImageUploaded imageUploaded : imageUploadeds) {
             ids.add(imageUploaded.getId());
         }
         entity.setIds(ids);
         // 审核信息处理
         Date dateCheck = new Date();
         
         PurchaseCheck check = entity.getPurchaseCheck();
 //        if (Purchase.CheckStatus.DENIED.getValue().equals(check.getCheckStatus()) || Purchase.CheckStatus.PASSED.getValue().equals(check.getCheckStatus())) {
             check.setPurchaseId(entity.getId());
             check.setDateCheck(dateCheck);
             check.setFlagDeleted(PurchaseCheck.FlagDeleted.NORMAL.getValue());
             check.setUserApply(entity.getUserApply());
             check.setDateApply(entity.getDateApply());
             purchaseCheckDao.save(check);
             entity.setDateCheck(dateCheck);
             entity.setCheckStatus(check.getCheckStatus());
 //        }
         entity = getPurchaseDao().update(entity);
         return entity;
     }
 
     @Override
     public List<Purchase> findAllByMap(Map<String, Object> params) {
         List<Purchase> list = getPurchaseDao().findAllPurchaseByUserRef(params);
         return getPurchaseWithImageIdByPurchase(list);
     }
 
 	@Override
 	public List<Purchase> findAllByIds(List<Long> ids, Map<String, String> params) {
         DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Purchase.class);
         if (null != ids && !ids.isEmpty()) {
             detachedCriteria.add(Restrictions.in("id", ids));
         }
         String checkStatus = params.get("checkStatus");
         if (!StringUtils.isBlank(checkStatus)) {
             detachedCriteria.add(Restrictions.eq("checkStatus", checkStatus));
         }
         String flagDeleted = params.get("flagDeleted");
         if (!StringUtils.isBlank(flagDeleted)) {
             detachedCriteria.add(Restrictions.eq("flagDeleted", flagDeleted));
         }
         detachedCriteria.addOrder(Order.desc("dateCreated"));
         List<Purchase> list = getPurchaseDao().findListByDetachedCriteria(detachedCriteria, 0, 5);
         return getPurchaseWithImageIdByPurchase(list);
 	}
 	
     /**
      * 返回带图片Id的产品信息
      * @param list
      * @return
      */
     protected List<Purchase> getPurchaseWithImageIdByPurchase(List<Purchase> list) {
         if (null != list && !list.isEmpty()) {
             List<Long> ids = new ArrayList<Long>();
             for (Purchase p : list) {
                 ids.add(p.getId());
             }
             Map<Long, List<Long>> maps = getImageUploadedDao().findAllIdsByRefIds(ids, ImageUploaded.RefType.PURCHASE, 0, ids.size() * 3);
             for (int i = 0; i < ids.size(); i++) {
                 Purchase ip = list.get(i);
                 List<Long> imageIds = maps.get(ip.getId());
                 if (null != imageIds) {
                     ip.setIds(maps.get(ip.getId()));
                 }
                 list.set(i, ip);
             }
         }
         return list;
     }
 
     @Override
     public List<Purchase> findAllByMapForUser(Map<String, String> params) {
         DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Purchase.class);
         String gtDateCreated = params.get("gtDateCreated");
         if (!StringUtils.isBlank(gtDateCreated)) {
             try {
                 detachedCriteria.add(Restrictions.gt("dateCreated", DateUtils.parseDate(gtDateCreated, new String[] { "yyyy-MM-dd HH:mm:ss" })));
             } catch (ParseException e) {
                 e.printStackTrace();
             }
         }
         String ltDateCreated = params.get("ltDateCreated");
         if (!StringUtils.isBlank(ltDateCreated)) {
             try {
                 detachedCriteria.add(Restrictions.lt("dateCreated", DateUtils.parseDate(ltDateCreated, new String[] { "yyyy-MM-dd HH:mm:ss" })));
             } catch (ParseException e) {
                 e.printStackTrace();
             }
         }
         String purchaseName = params.get("purchaseName");
         if (!StringUtils.isBlank(purchaseName)) {
             detachedCriteria.add(Restrictions.like("purchaseName", "%" + purchaseName + "%"));
         }
         String purchaseType = params.get("purchaseType");
         if (!StringUtils.isBlank(purchaseType)) {
             detachedCriteria.add(Restrictions.like("purchaseType", "%" + purchaseType + "%"));
         }
         String checkStatus = params.get("checkStatus");
         if (!StringUtils.isBlank(checkStatus)) {
             detachedCriteria.add(Restrictions.eq("checkStatus", checkStatus));
         }
         String purchaseAddress = params.get("purchaseAddress");
         if (!StringUtils.isBlank(purchaseAddress)) {
             detachedCriteria.add(Restrictions.like("purchaseAddress", "%" + purchaseAddress + "%"));
         }
         String purchaseLocation = params.get("purchaseLocation");
         if (!StringUtils.isBlank(purchaseLocation)) {
             detachedCriteria.add(Restrictions.like("purchaseLocation", "%" + purchaseLocation + "%"));
         }
         String flagDeleted = params.get("flagDeleted");
         if (!StringUtils.isBlank(flagDeleted)) {
             detachedCriteria.add(Restrictions.eq("flagDeleted", flagDeleted));
         }
         String userCreated = params.get("userCreated");
         if (!StringUtils.isBlank(userCreated)) {
             detachedCriteria.add(Restrictions.eq("userCreated", Long.parseLong(userCreated)));
         }
         detachedCriteria.addOrder(Order.desc("dateCreated"));
         List<Purchase> list = getPurchaseDao().findListByDetachedCriteria(detachedCriteria, 0, 5);
 //        if (null != list && !list.isEmpty()) {
 //            List<Long> ids = new ArrayList<Long>();
 //            for (Purchase p : list) {
 //                ids.add(p.getId());
 //            }
 //            Map<Long, List<Long>> maps = getImageUploadedDao().findAllIdsByRefIds(ids, ImageUploaded.RefType.PURCHASE, 0, ids.size() * 3);
 //            for (int i = 0; i < ids.size(); i++) {
 //                Purchase ip = list.get(i);
 //                List<Long> imageIds = maps.get(ip.getId());
 //                if (null != imageIds) {
 //                    ip.setIds(maps.get(ip.getId()));
 //                }
 //                list.set(i, ip);
 //            }
 //        }
 //        return list;
         return getPurchaseWithImageIdByPurchase(list);
     }
 
     @Override
     public Purchase findByMap(Map<String, String> params) {
         DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Purchase.class);
 
         handleCriteriaByParams(detachedCriteria, params);
 
         List<Purchase> l = getPurchaseDao().findListByDetachedCriteria(detachedCriteria, 0, 1);
         if (null == l || l.isEmpty()) {
             return new Purchase();
         }
         Purchase p = l.get(0);
         List<Long> ids = getImageUploadedDao().findAllIdsByRefId(p.getId(), ImageUploaded.RefType.PURCHASE);
         p.setIds(ids);
         if (null != p.getUserCreated()) {
             Person person = getPersonDao().findById(p.getUserCreated());
             if (null != person) {
                 p.setPerson(person);
             }
         }
 		if (StringUtils.isNotBlank(params.get("checklist"))
 				&& "purchaseCheckList".equalsIgnoreCase(params.get("checklist"))) {
 			p.setPurchaseCheckList(getPurchaseCheckDao().findByPurchaseId(
 					p.getId()));
 		}
         return p;
     }
 
     protected void handleCriteriaByParams(DetachedCriteria criteria, Map<String, String> params) {
         String id = params.get("id");
         if (!StringUtils.isBlank(id)) {
             criteria.add(Restrictions.eq("id", Long.parseLong(id)));
         }
         String idMin = params.get("idMin");
         if (!StringUtils.isBlank(idMin)) {
             criteria.add(Restrictions.ge("id", Long.parseLong(idMin)));
         }
         String idMax = params.get("idMax");
         if (!StringUtils.isBlank(idMax)) {
             criteria.add(Restrictions.le("id", Long.parseLong(idMax)));
         }
         String purchaseName = params.get("purchaseName");
         if (!StringUtils.isBlank(purchaseName)) {
             criteria.add(Restrictions.like("purchaseName", "%" + purchaseName + "%"));
         }
         String purchaseType = params.get("purchaseType");
         if (!StringUtils.isBlank(purchaseType)) {
             criteria.add(Restrictions.like("purchaseType", "%" + purchaseType + "%"));
         }
         String purchaseNum = params.get("purchaseNum");
         if (!StringUtils.isBlank(purchaseNum)) {
             criteria.add(Restrictions.like("purchaseNum", "%" + purchaseNum + "%"));
         }
         String purchaseValidMin = params.get("purchaseValidMin");
         if (!StringUtils.isBlank(purchaseValidMin)) {
             try {
                 criteria.add(Restrictions.ge("purchaseValid", DateUtils.parseDate(purchaseValidMin, "yyyy-MM-dd")));
             } catch (ParseException e) {
                 log.error("[purchaseValidMin] parsed exception :", e);
             }
         }
         String purchaseValidMax = params.get("purchaseValidMax");
         if (!StringUtils.isBlank(purchaseValidMax)) {
             try {
                 criteria.add(Restrictions.lt("purchaseValid", DateUtils.addDays(DateUtils.parseDate(purchaseValidMax, "yyyy-MM-dd"), 1)));
             } catch (ParseException e) {
                 log.error("[purchaseValidMax] parsed exception :", e);
             }
         }
         String purchaseAddress = params.get("purchaseAddress");
         if (!StringUtils.isBlank(purchaseAddress)) {
             criteria.add(Restrictions.like("purchaseAddress", "%" + purchaseAddress + "%"));
         }
         String purchaseLocation = params.get("purchaseLocation");
         if (!StringUtils.isBlank(purchaseLocation)) {
             criteria.add(Restrictions.like("purchaseLocation", "%" + purchaseLocation + "%"));
         }
         String purchaseDesc = params.get("purchaseDesc");
         if (!StringUtils.isBlank(purchaseDesc)) {
             criteria.add(Restrictions.like("purchaseDesc", "%" + purchaseDesc + "%"));
         }
         String flagDeleted = params.get("flagDeleted");
         if (!StringUtils.isBlank(flagDeleted)) {
             criteria.add(Restrictions.like("flagDeleted", "%" + flagDeleted + "%"));
         }
         String userApply = params.get("userApply");
         if (!StringUtils.isBlank(userApply)) {
             criteria.add(Restrictions.eq("userApply", Long.parseLong(userApply)));
         }
         String userApplyMin = params.get("userApplyMin");
         if (!StringUtils.isBlank(userApplyMin)) {
             criteria.add(Restrictions.ge("userApply", Long.parseLong(userApplyMin)));
         }
         String userApplyMax = params.get("userApplyMax");
         if (!StringUtils.isBlank(userApplyMax)) {
             criteria.add(Restrictions.le("userApply", Long.parseLong(userApplyMax)));
         }
         String dateApplyMin = params.get("dateApplyMin");
         if (!StringUtils.isBlank(dateApplyMin)) {
             try {
                 criteria.add(Restrictions.ge("dateApply", DateUtils.parseDate(dateApplyMin, "yyyy-MM-dd")));
             } catch (ParseException e) {
                 log.error("[dateApplyMin] parsed exception :", e);
             }
         }
         String dateApplyMax = params.get("dateApplyMax");
         if (!StringUtils.isBlank(dateApplyMax)) {
             try {
                 criteria.add(Restrictions.lt("dateApply", DateUtils.addDays(DateUtils.parseDate(dateApplyMax, "yyyy-MM-dd"), 1)));
             } catch (ParseException e) {
                 log.error("[dateApplyMax] parsed exception :", e);
             }
         }
         String userCheck = params.get("userCheck");
         if (!StringUtils.isBlank(userCheck)) {
             criteria.add(Restrictions.eq("userCheck", Long.parseLong(userCheck)));
         }
         String userCheckMin = params.get("userCheckMin");
         if (!StringUtils.isBlank(userCheckMin)) {
             criteria.add(Restrictions.ge("userCheck", Long.parseLong(userCheckMin)));
         }
         String userCheckMax = params.get("userCheckMax");
         if (!StringUtils.isBlank(userCheckMax)) {
             criteria.add(Restrictions.le("userCheck", Long.parseLong(userCheckMax)));
         }
         String dateCheckMin = params.get("dateCheckMin");
         if (!StringUtils.isBlank(dateCheckMin)) {
             try {
                 criteria.add(Restrictions.ge("dateCheck", DateUtils.parseDate(dateCheckMin, "yyyy-MM-dd")));
             } catch (ParseException e) {
                 log.error("[dateCheckMin] parsed exception :", e);
             }
         }
         String dateCheckMax = params.get("dateCheckMax");
         if (!StringUtils.isBlank(dateCheckMax)) {
             try {
                 criteria.add(Restrictions.lt("dateCheck", DateUtils.addDays(DateUtils.parseDate(dateCheckMax, "yyyy-MM-dd"), 1)));
             } catch (ParseException e) {
                 log.error("[dateCheckMax] parsed exception :", e);
             }
         }
         String userCreated = params.get("userCreated");
         if (!StringUtils.isBlank(userCreated)) {
             criteria.add(Restrictions.eq("userCreated", Long.parseLong(userCreated)));
         }
         String userCreatedMin = params.get("userCreatedMin");
         if (!StringUtils.isBlank(userCreatedMin)) {
             criteria.add(Restrictions.ge("userCreated", Long.parseLong(userCreatedMin)));
         }
         String userCreatedMax = params.get("userCreatedMax");
         if (!StringUtils.isBlank(userCreatedMax)) {
             criteria.add(Restrictions.le("userCreated", Long.parseLong(userCreatedMax)));
         }
         String dateCreatedMin = params.get("dateCreatedMin");
         if (!StringUtils.isBlank(dateCreatedMin)) {
             try {
                 criteria.add(Restrictions.ge("dateCreated", DateUtils.parseDate(dateCreatedMin, "yyyy-MM-dd")));
             } catch (ParseException e) {
                 log.error("[dateCreatedMin] parsed exception :", e);
             }
         }
         String dateCreatedMax = params.get("dateCreatedMax");
         if (!StringUtils.isBlank(dateCreatedMax)) {
             try {
                 criteria.add(Restrictions.lt("dateCreated", DateUtils.addDays(DateUtils.parseDate(dateCreatedMax, "yyyy-MM-dd"), 1)));
             } catch (ParseException e) {
                 log.error("[dateCreatedMax] parsed exception :", e);
             }
         }
         String userUpdated = params.get("userUpdated");
         if (!StringUtils.isBlank(userUpdated)) {
             criteria.add(Restrictions.eq("userUpdated", Long.parseLong(userUpdated)));
         }
         String userUpdatedMin = params.get("userUpdatedMin");
         if (!StringUtils.isBlank(userUpdatedMin)) {
             criteria.add(Restrictions.ge("userUpdated", Long.parseLong(userUpdatedMin)));
         }
         String userUpdatedMax = params.get("userUpdatedMax");
         if (!StringUtils.isBlank(userUpdatedMax)) {
             criteria.add(Restrictions.le("userUpdated", Long.parseLong(userUpdatedMax)));
         }
         String lastUpdatedMin = params.get("lastUpdatedMin");
         if (!StringUtils.isBlank(lastUpdatedMin)) {
             try {
                 criteria.add(Restrictions.ge("lastUpdated", DateUtils.parseDate(lastUpdatedMin, "yyyy-MM-dd")));
             } catch (ParseException e) {
                 log.error("[lastUpdatedMin] parsed exception :", e);
             }
         }
         String lastUpdatedMax = params.get("lastUpdatedMax");
         if (!StringUtils.isBlank(lastUpdatedMax)) {
             try {
                 criteria.add(Restrictions.lt("lastUpdated", DateUtils.addDays(DateUtils.parseDate(lastUpdatedMax, "yyyy-MM-dd"), 1)));
             } catch (ParseException e) {
                 log.error("[lastUpdatedMax] parsed exception :", e);
             }
         }
         
         String checkStatus = params.get("checkStatus");
         if (!StringUtils.isBlank(checkStatus)) {
         	criteria.add(Restrictions.eq("checkStatus", checkStatus));
         }
 
         criteria.addOrder(Order.desc("dateCreated"));
     }
 
     @Override
     public Purchase update(Purchase entity) {
         // 审核信息处理
         Date dateCheck = new Date();
 
         PurchaseCheck check = entity.getPurchaseCheck();
         if (Purchase.CheckStatus.DENIED.getValue().equals(check.getCheckStatus()) || Purchase.CheckStatus.PASSED.getValue().equals(check.getCheckStatus())) {
             check.setPurchaseId(entity.getId());
             check.setDateCheck(dateCheck);
             check.setFlagDeleted(PurchaseCheck.FlagDeleted.NORMAL.getValue());
             check.setUserApply(entity.getUserApply());
             check.setDateApply(entity.getDateApply());
             purchaseCheckDao.save(check);
             entity.setCheckStatus(check.getCheckStatus());
         }
 
         entity.setDateCheck(dateCheck);
         // entity.setRemark(check.getRemark());
         entity = getPurchaseDao().update(entity);
 
         return entity;
     }
 
     @Override
     public void delete(Purchase entity) {
         getPurchaseDao().deleteById(entity.getId());
     }
 
     @Override
     public Purchase findById(Long id) {
         Purchase purchase = getPurchaseDao().findById(id);
         if (purchase != null) {
             List<Long> ids = getImageUploadedDao().findAllIdsByRefId(purchase.getId(), ImageUploaded.RefType.PURCHASE);
             purchase.setIds(ids);
         	
             List<PurchaseCheck> l = purchaseCheckDao.findByPurchaseId(purchase.getId());
             purchase.setPurchaseCheckList(l);
         }
         return purchase;
     }
 
     @Override
     public Pagination findByParams(Map<String, String> params) {
         DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Purchase.class);
         handleCriteriaByParams(detachedCriteria, params);
         int pageSize = MyServerUtils.parseInteger(params.get("pageSize"), 20);
         int startIndex = MyServerUtils.parseInteger(params.get("pageNo"), 0);
         Pagination p = getPurchaseDao().findByDetachedCriteria(detachedCriteria, pageSize, startIndex);
         p.setList(getPurchaseWithImageIdByPurchase(p.getList()));
         return p;
     }
 
     public PurchaseCheckDao getPurchaseCheckDao() {
         return purchaseCheckDao;
     }
 
     public void setPurchaseCheckDao(PurchaseCheckDao purchaseCheckDao) {
         this.purchaseCheckDao = purchaseCheckDao;
     }
 
 }
