 package com.server.cx.dao.cx.spec;
 
 import com.server.cx.entity.cx.UserCommonMGraphic;
 import com.server.cx.entity.cx.UserHolidayMGraphic;
 import com.server.cx.entity.cx.UserInfo;
 import org.springframework.data.jpa.domain.Specification;
 
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 
 public class UserCommonMGraphicSpecifications {
 
     public static Specification<UserHolidayMGraphic> userHolidayMGraphicSpecification(final UserInfo userInfo){
         return  new Specification<UserHolidayMGraphic>() {
             @Override
             public Predicate toPredicate(Root<UserHolidayMGraphic> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                 return  cb.and(cb.equal(root.get("userInfo"),userInfo),cb.equal(root.get("common"),false));
             }
         };
     }
 
     public static Specification<UserCommonMGraphic> userCommonMGraphicCount(final UserInfo userInfo){
         return  new Specification<UserCommonMGraphic>() {
             @Override
             public Predicate toPredicate(Root<UserCommonMGraphic> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return  cb.and(cb.equal(root.get("userInfo"),userInfo),cb.equal(root.get("common"),false),cb.equal(root.get("modeType"),2));
             }
         };
     }
 
 }
