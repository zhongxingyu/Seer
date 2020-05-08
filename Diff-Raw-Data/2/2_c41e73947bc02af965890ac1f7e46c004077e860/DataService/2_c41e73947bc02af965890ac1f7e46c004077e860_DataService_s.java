 package com.mmtzj.service;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.mmtzj.domain.Category;
 import com.mmtzj.domain.Eval;
 import com.mmtzj.domain.Item;
 import com.mmtzj.util.BaseUtil;
 import com.mmtzj.util.Constant;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.data.redis.core.RedisTemplate;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
 import org.springframework.stereotype.Service;
 
 import javax.annotation.Resource;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: FANLU
  * Date: 13-1-29
  * Time: 下午5:37
  * To change this template use File | Settings | File Templates.
  */
 @Service
 public class DataService {
 
     @Resource
     private JdbcTemplate jdbcTemplate;
 
     @Resource
     private JedisService jedisService;
 
     public List<Category> getCategories() {
         List<Category> categories = (List<Category>) jedisService.get("categories");
         if(categories == null){
             categories = getAllCategories();
             jedisService.set("categories", categories, Constant.CACHE_SECONDS);
         }
         return categories;
     }
 
 
     public List<Eval> getEvals(Item item) {
         List<Eval> evals = (List<Eval>) jedisService.get(item.getId() + "evals");
         if(evals == null){
             evals = getAllEvals(ImmutableList.of(item.getId()));
             for(Eval eval : evals){
                 eval.setPicId(String.valueOf(BaseUtil.getRandom(1, 100)));
             }
             jedisService.set(item.getId() + "evals", evals, Constant.CACHE_SECONDS);
         }
         return evals;
     }
 
     public List<Item> getItems(String pid) {
         List<Item> items = null;
         if(StringUtils.isBlank(pid)){
             items = (List<Item>) jedisService.get("items");
             if(items == null){
                 items = getAllItems(pid);
                 jedisService.set("items", items, Constant.CACHE_SECONDS);
             }
         }else{
             items = getAllItems(pid);
         }
         return items;
     }
 
 
     public Map<String, Integer> getItemTypeCountsMap(List<Item> items) {
         Map<String, Integer> itemTypes = Maps.newHashMap();
         for(Item item : items){
             List<Eval> evals = getEvals(item);
             item.setEvalList(evals);
             if(itemTypes.containsKey("itemtype"+item.getCategoryId())){
                 itemTypes.put("itemtype"+item.getCategoryId(), itemTypes.get("itemtype"+item.getCategoryId())+1);
             }else{
                 itemTypes.put("itemtype"+item.getCategoryId(), 1);
             }
         }
         return itemTypes;
     }
 
 
     public List<Category> getAllCategories() {
         try{
             return jdbcTemplate.query("SELECT * FROM category", ParameterizedBeanPropertyRowMapper.newInstance(Category.class));
         }catch (Exception e){
             e.printStackTrace();
         }
         return null;
     }
 
     public List<Item> getAllItems(String pid) {
         try{
             StringBuffer sql = new StringBuffer("SELECT * FROM item WHERE status=1 order by ");
             if(StringUtils.isNotBlank(pid)){
                sql.append(" id=").append(pid).append("desc, ");
             }
             sql.append(" rank desc");
             return jdbcTemplate.query(sql.toString(), ParameterizedBeanPropertyRowMapper.newInstance(Item.class));
         }catch (Exception e){
             e.printStackTrace();
         }
         return null;
     }
 
     public List<Eval> getAllEvals(List<Integer> itemIds){
         return jdbcTemplate.query("select * from eval where itemId in (?)", new Object[]{Joiner.on(",").join(itemIds)}, ParameterizedBeanPropertyRowMapper.newInstance(Eval.class));
     }
 
 }
