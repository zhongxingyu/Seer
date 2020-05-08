 package com.xone.service.app;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.time.DateUtils;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.xone.model.hibernate.app.AdbannerDao;
 import com.xone.model.hibernate.app.ImageUploadedDao;
 import com.xone.model.hibernate.entity.Adbanner;
 import com.xone.model.hibernate.entity.ImageUploaded;
 import com.xone.model.hibernate.support.Pagination;
 
 public class AdbannerServiceImpl implements AdbannerService {
 
 	@Autowired
 	protected AdbannerDao adbannerDao;
 	
 	@Autowired
 	protected ImageUploadedDao imageUploadedDao;
 
 	@Override
 	public Adbanner save(Adbanner entity) {
 		return getAdbannerDao().save(entity);
 	}
 	
 	@Override
 	public Adbanner save(Adbanner entity, ImageUploaded imageUploaded) {
 		entity = getAdbannerDao().save(entity);
 		imageUploaded.setRefId(entity.getId());
 		imageUploaded.setRefType(ImageUploaded.RefType.ABBANNER.getValue());
 		imageUploaded.setFlagDeleted(ImageUploaded.FlagDeleted.NORMAL.getValue());
 		imageUploaded = getImageUploadedDao().save(imageUploaded);
 		entity.setRefId(imageUploaded.getId());
 		return entity;
 	}
 	
 	@Override
 	public Adbanner update(Adbanner entity) {
 		return getAdbannerDao().update(entity);
 	}
 	
 	@Override
 	public Adbanner update(Adbanner entity, ImageUploaded imageUploaded, Long imageId) {
 		entity = getAdbannerDao().update(entity);
 		if (null != imageUploaded) {
 			imageUploaded.setRefId(entity.getId());
			imageUploaded.setRefType(ImageUploaded.RefType.PRODUCT.getValue());
 			imageUploaded.setFlagDeleted(ImageUploaded.FlagDeleted.NORMAL.getValue());
 			imageUploaded = getImageUploadedDao().save(imageUploaded);
 			if (null == imageId) {
 				getImageUploadedDao().deleteLogicById(imageId);
 			}
 			entity.setAdRefId(imageUploaded.getId());
 		}
 		return entity;
 	}
 	
 	@Override
 	public void delete(Adbanner entity) {
 		getAdbannerDao().deleteById(entity.getId());
 	}
 	
 	@Override
 	public Adbanner findById(Long id) {
 		Adbanner adbanner = getAdbannerDao().findById(id);
 		if (null != adbanner) {
 			List<Long> ids = getImageUploadedDao().findAllIdsByRefId(adbanner.getId(), ImageUploaded.RefType.ABBANNER);
 			if (null != ids && !ids.isEmpty()) {
 				adbanner.setAdRefId(ids.get(0));
 			}
 		}
 		return adbanner;
 	}
 	
 	@Override
 	public List<Adbanner> findAllByMap(Map<String, String> params) {
 		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Adbanner.class);
 		String today = params.get("today");
 		if (!StringUtils.isBlank(today)) {
 			try {
 				Date dateToday = DateUtils.parseDate(today, new String[] {
 						"yyyy-MM-dd HH:mm:ss"
 				});
 				detachedCriteria.add(Restrictions.ge("adStart", dateToday));
 				detachedCriteria.add(Restrictions.lt("adEnd", dateToday));
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 		}
 		detachedCriteria.add(Restrictions.eq("flagDeleted", Adbanner.FlagDeleted.NORMAL.getValue()));
 		List<Adbanner> list = getAdbannerDao().findListByDetachedCriteria(detachedCriteria, 0, 10);
 		if (null != list && !list.isEmpty()) {
 			List<Long> ids = new ArrayList<Long>();
 			for (Adbanner a : list) {
 				ids.add(a.getId());
 			}
 			Map<Long, List<Long>> maps = getImageUploadedDao().findAllIdsByRefIds(ids, ImageUploaded.RefType.ABBANNER, 0, ids.size());
 			for (int i = 0; i < ids.size(); i++) {
 				Adbanner ad = list.get(i);
 				List<Long> imageIds = maps.get(ad.getId());
 				if (null != imageIds && !imageIds.isEmpty()) {
 					ad.setAdRefId(maps.get(ad.getId()).get(0));
 				}
 				list.set(i, ad);
 			}
 		}
 		return list;
 	}
 	
 	/**
 	 * APP广告查询专用
 	 */
 	@Override
 	public List<Adbanner> findItemsByMap(Map<String, String> params) {
 		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Adbanner.class);
 		String gtDateCreated = params.get("gtDateCreated");
 		if (!StringUtils.isBlank(gtDateCreated)) {
 			try {
 				detachedCriteria.add(Restrictions.gt("dateCreated", DateUtils.parseDate(gtDateCreated, new String[] {
 						"yyyy-MM-dd HH:mm:ss"
 				})));
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 		}
 		String ltDateCreated = params.get("ltDateCreated");
 		if (!StringUtils.isBlank(ltDateCreated)) {
 			try {
 				detachedCriteria.add(Restrictions.lt("dateCreated", DateUtils.parseDate(ltDateCreated, new String[] {
 						"yyyy-MM-dd HH:mm:ss"
 				})));
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 		}
 		if (!StringUtils.isBlank(params.get("userId"))) {
 			detachedCriteria.add(Restrictions.eq("userId", Long.parseLong(params.get("userId"))));
 		}
 		detachedCriteria.add(Restrictions.eq("flagDeleted", Adbanner.FlagDeleted.NORMAL.getValue()));
 		detachedCriteria.addOrder(Order.desc("dateCreated"));
 		List<Adbanner> list = getAdbannerDao().findListByDetachedCriteria(detachedCriteria, 0, 5);
 		return list;
 	}
 
 	public Pagination findByParams(Map<String, String> params) {
 		DetachedCriteria detachedCriteria = DetachedCriteria
 				.forClass(Adbanner.class);
 		if (!StringUtils.isBlank(params.get("userId"))) {
 			detachedCriteria.add(Restrictions.eq("userId", Long.parseLong(params.get("userId"))));
 		}
 		if (!StringUtils.isBlank(params.get("refId"))) {
 			detachedCriteria.add(Restrictions.eq("refId", Long.parseLong(params.get("refId"))));
 		}
 		if (!StringUtils.isBlank(params.get("adType"))) {
 			detachedCriteria.add(Restrictions.eq("adType", params.get("adType")));
 		}
 		if (!StringUtils.isBlank(params.get("adRefId"))) {
 			detachedCriteria.add(Restrictions.eq("adRefId", Long.parseLong(params.get("adRefId"))));
 		}
 		String gtAdStart = params.get("gtAdStart");
 		if (!StringUtils.isBlank(gtAdStart)) {
 			try {
 				detachedCriteria.add(Restrictions.ge("adStart", DateUtils.parseDate(gtAdStart, new String[] {
 						"yyyy-MM-dd"
 				})));
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 		}
 		String ltAdStart = params.get("ltAdStart");
 		if (!StringUtils.isBlank(ltAdStart)) {
 			try {
 				detachedCriteria.add(Restrictions.lt("adStart", DateUtils.addDays(DateUtils.parseDate(ltAdStart, new String[] {
 						"yyyy-MM-dd"
 				}), 1)));
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 		}
 		String gtAdEnd = params.get("gtAdEnd");
 		if (!StringUtils.isBlank(gtAdEnd)) {
 			try {
 				detachedCriteria.add(Restrictions.ge("adEnd", DateUtils.parseDate(gtAdEnd, new String[] {
 						"yyyy-MM-dd"
 				})));
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 		}
 		String ltAdEnd = params.get("ltAdEnd");
 		if (!StringUtils.isBlank(ltAdEnd)) {
 			try {
 				detachedCriteria.add(Restrictions.lt("adEnd", DateUtils.addDays(DateUtils.parseDate(ltAdEnd, new String[] {
 						"yyyy-MM-dd"
 				}), 1)));
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 		}
 		int pageSize = com.xone.model.utils.StringUtils.parseInt(params.get("pageSize"), 20);
 		int startIndex = com.xone.model.utils.StringUtils.parseInt(params.get("pageNo"), 0);
 		return getAdbannerDao().findByDetachedCriteria(detachedCriteria, pageSize, startIndex);
 	}
 
 	public AdbannerDao getAdbannerDao() {
 		return adbannerDao;
 	}
 
 	public void setAdbannerDao(AdbannerDao adbannerDao) {
 		this.adbannerDao = adbannerDao;
 	}
 
 	public ImageUploadedDao getImageUploadedDao() {
 		return imageUploadedDao;
 	}
 
 	public void setImageUploadedDao(ImageUploadedDao imageUploadedDao) {
 		this.imageUploadedDao = imageUploadedDao;
 	}
 	
 }
