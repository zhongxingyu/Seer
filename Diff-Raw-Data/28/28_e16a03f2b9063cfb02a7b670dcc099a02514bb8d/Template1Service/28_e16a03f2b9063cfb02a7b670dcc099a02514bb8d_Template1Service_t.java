 package com.cqlybest.common.service;
 
 import java.util.List;
 
 import org.hibernate.criterion.Conjunction;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.cqlybest.common.bean.Product;
 import com.cqlybest.common.bean.template1.Template1IndexPoster;
 import com.cqlybest.common.bean.template1.Template1Menu;
 import com.cqlybest.common.bean.template1.Template1ProductGroup;
import com.cqlybest.common.dao.ImageDao;
 import com.cqlybest.common.dao.ProductDao;
 import com.cqlybest.common.dao.Template1Dao;
 
 @Service
 public class Template1Service {
 
   @Autowired
   private Template1Dao template1Dao;
   @Autowired
   private ProductDao productDao;
  @Autowired
  private ImageDao imageDao;
 
   /**
    * 增加海报
    * 
    * @param poster
    */
   @Transactional
   public void add(Template1IndexPoster poster) {
     template1Dao.saveOrUpdate(poster);
   }
 
   /**
    * 删除海报
    */
   @Transactional
   public void delete(Integer id) {
     Template1IndexPoster poster = template1Dao.find(Template1IndexPoster.class, id);
     if (poster != null) {
       template1Dao.del(poster);
     }
   }
 
   /**
    * 获取所有海报
    * 
    * @return
    */
   public List<Template1IndexPoster> getPosters() {
     return template1Dao.findAll(Template1IndexPoster.class);
   }
 
   /**
    * 获取已发布的海报
    * 
    * @return
    */
   public List<Template1IndexPoster> getPublishedPosters() {
     return template1Dao.find(Template1IndexPoster.class, Restrictions.eq("published", true));
   }
 
   /**
    * 发布/取消发布海报
    * 
    * @param id
    * @param published
    */
   @Transactional
   public void togglePublished(Integer id, boolean published) {
     Template1IndexPoster poster = template1Dao.find(Template1IndexPoster.class, id);
     poster.setPublished(published);
     template1Dao.saveOrUpdate(poster);
   }
 
   @Transactional
   public void add(Template1Menu menu) {
     menu.setDisplayOrder(template1Dao.total(Template1Menu.class).intValue() + 1);
     menu.setPublished(false);
     template1Dao.saveOrUpdate(menu);
   }
 
   @Transactional
   public void modify(Template1Menu menu) {
     menu.setPublished(false);
     template1Dao.deleteSubMenus(menu.getId());
     template1Dao.saveOrUpdate(menu);
   }
 
   @Transactional
   public void delete(String id) {
     Template1Menu menu = template1Dao.find(Template1Menu.class, id);
     if (menu != null) {
       template1Dao.del(menu);
     }
   }
 
   @Transactional
   public void moveUp(String id) {
     Template1Menu menu = template1Dao.find(Template1Menu.class, id);
     int order = menu.getDisplayOrder();
     Template1Menu prev = template1Dao.prevMenu(order);
     if (prev != null) {
       menu.setDisplayOrder(prev.getDisplayOrder());
       prev.setDisplayOrder(order);
       template1Dao.saveOrUpdate(menu);
       template1Dao.saveOrUpdate(prev);
     }
   }
 
   @Transactional
   public void moveDown(String id) {
     Template1Menu menu = template1Dao.find(Template1Menu.class, id);
     int order = menu.getDisplayOrder();
     Template1Menu next = template1Dao.nextMenu(order);
     if (next != null) {
       menu.setDisplayOrder(next.getDisplayOrder());
       next.setDisplayOrder(order);
       template1Dao.saveOrUpdate(menu);
       template1Dao.saveOrUpdate(next);
     }
   }
 
   @Transactional
   public void togglePublished(String id, boolean published) {
     template1Dao.toggleMenuPublished(id, published);
   }
 
   public Template1Menu get(String id) {
     return template1Dao.find(Template1Menu.class, id);
   }
 
   public List<Template1Menu> getAllMenus() {
     return template1Dao.find(Template1Menu.class, Order.asc("displayOrder"));
   }
 
   public List<Template1Menu> getPublishedMenus() {
     return template1Dao.find(Template1Menu.class, Restrictions.eq("published", true), Order
         .asc("displayOrder"));
   }
 
   @Transactional
   public void add(Template1ProductGroup group) {
     template1Dao.saveOrUpdate(group);
   }
 
   public List<Template1ProductGroup> getAllIndexProductGroups() {
     return template1Dao.find(Template1ProductGroup.class, Order.asc("displayOrder"));
   }
 
   @Transactional
   public void delete(Template1ProductGroup group) {
     template1Dao.del(group);
   }
 
   @Transactional
   public void orderProductGroup(Integer[] ids, Integer[] orders) {
     for (int i = 0; i < ids.length; i++) {
       template1Dao.updateProductGroupOrder(ids[i], orders[i]);
     }
   }
 
   public List<Product> getSpecialProduct(int number) {
     return getProductWithTrueProperty("specialOffer", number);// 特价
   }
 
   public List<Product> getRecommendedProduct(int number) {
     return getProductWithTrueProperty("recommend", number);// 推荐
   }
 
   public List<Product> getHotProduct(int number) {
     return getProductWithTrueProperty("popular", number);// 热门
   }
 
   private List<Product> getProductWithTrueProperty(String property, int number) {
     Conjunction condition = Restrictions.conjunction();
     condition.add(Restrictions.eq(property, true));
     condition.add(Restrictions.eq("published", true));// 已发布
    List<Product> products = null;
     if (number > 0) {
      products = productDao.find(condition, 0, number);
    } else {
      products = productDao.find(condition);
     }

    for (Product product : products) {
      product.setPosters(imageDao.queryImagesWithoutData("product-poster", product.getId()));
    }

    return products;
   }
 
 }
