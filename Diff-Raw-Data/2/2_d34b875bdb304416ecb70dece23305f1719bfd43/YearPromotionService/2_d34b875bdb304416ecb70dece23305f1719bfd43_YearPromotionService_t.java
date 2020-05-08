 package service;
 
 import models.school.Promotion;
 import models.school.YearPromotion;
 
 import javax.persistence.Query;
 import java.util.List;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class YearPromotionService {
 
     public List<YearPromotion> getYearPromotionsForYear(int year) {
 
         Query query = YearPromotion.em().createQuery("select yp from YearPromotion yp " +
                 "where yp.year = :year " +
                "order by yp.promotion, yp.name");
 
         query.setParameter("year", year);
 
         return query.getResultList();
     }
 
     public YearPromotion create(YearPromotion yearPromotion, int year) {
 
         YearPromotion promotion = new YearPromotion();
 
         promotion.name = yearPromotion.name;
         promotion.studentsNumber = yearPromotion.studentsNumber;
         promotion.promotion = yearPromotion.promotion;
         promotion.year = year;
 
         promotion.save();
 
         return promotion;
     }
 
     public YearPromotion update(YearPromotion yearPromotionModel, YearPromotion yearPromotion) {
 
         checkNotNull(yearPromotionModel);
         checkNotNull(yearPromotion);
 
         yearPromotionModel.studentsNumber = yearPromotion.studentsNumber;
         yearPromotionModel.save();
 
         return yearPromotionModel;
     }
 
     public YearPromotion getByNamePromotionAndYear(String name, Promotion promotion, int year) {
         return YearPromotion.find("byNameAndPromotionAndYear", name, promotion, year).first();
     }
 
     public List<YearPromotion> getYearPromotionsByPromotionAndYear(Promotion promotion, int year) {
 
         Query query = YearPromotion.em().createQuery("select yp from YearPromotion yp " +
                 "where yp.promotion = :promotion and yp.year = :year");
 
         query.setParameter("promotion", promotion)
                 .setParameter("year", year);
 
         return query.getResultList();
     }
 }
