 package com.github.smart.recommendation;
 
 import com.google.common.base.Function;
 import com.google.common.collect.FluentIterable;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import static com.google.common.collect.Lists.newArrayList;
 
 public class RecommendationService {
     private com.github.smart.service.RecommendationService service;
 
     public RecommendationService(com.github.smart.service.RecommendationService service) {
         this.service = service;
     }
 
     public List<String> recommendBrands(String customerId, int limit) {
        List<BrandSimilarity> brandSimilarities = getBrandSimilarities(service.findCustomerBrands(customerId), service.retrieveBrands());
         return FluentIterable.from(brandSimilarities).transform(toBrand()).limit(limit).toList();
     }
 
     private List<BrandSimilarity> getBrandSimilarities(List<String> customerBrands, List<String> allBrands) {
         List<BrandSimilarity> brandSimilarities = newArrayList();
 
         for (String brand : allBrands) {
             if (!customerBrands.contains(brand)) {
                 double similarity = 0;
                 for (String customerBrand : customerBrands) {
                     similarity += service.retrieveSimilarity(customerBrand, brand);
                 }
                 brandSimilarities.add(new BrandSimilarity(brand, similarity));
             }
         }
 
         Collections.sort(brandSimilarities, compareSimilarity());
         return brandSimilarities;
     }
 
     private Function<BrandSimilarity, String> toBrand() {
         return new Function<BrandSimilarity, String>() {
             @Override
             public String apply(BrandSimilarity input) {
                 return input.getBrand();
             }
         };
     }
 
     private Comparator<BrandSimilarity> compareSimilarity() {
         return new Comparator<BrandSimilarity>() {
             @Override
             public int compare(BrandSimilarity thisBrandSimilarity, BrandSimilarity thatBrandSimilarity) {
                 if (thisBrandSimilarity.getSimilarity() > thatBrandSimilarity.getSimilarity()) {
                     return -1;
                 }
 
                 if (thisBrandSimilarity.getSimilarity() < thatBrandSimilarity.getSimilarity()) {
                     return 1;
                 }
 
                 return 0;
             }
         };
     }
 
     private static class BrandSimilarity {
         private String brand;
         private double similarity;
 
         private BrandSimilarity(String brand, double similarity) {
             this.brand = brand;
             this.similarity = similarity;
         }
 
         public String getBrand() {
             return brand;
         }
 
         public double getSimilarity() {
             return similarity;
         }
     }
 }
