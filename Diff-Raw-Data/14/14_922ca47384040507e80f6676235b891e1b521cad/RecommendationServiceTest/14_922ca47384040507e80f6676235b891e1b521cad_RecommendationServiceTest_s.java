 package com.github.smart.recommendation;
 
 import com.github.smart.service.DefaultRecommendationService;
 import org.junit.Test;
 
 import java.util.List;
 
import static com.google.common.collect.ImmutableList.of;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class RecommendationServiceTest {
     @Test
     public void should_recommend_brand() {
 
         com.github.smart.service.RecommendationService persistence = mock(DefaultRecommendationService.class);
        when(persistence.retrieveBrands()).thenReturn(of("REVER", "DREAMHEAD", "TOM"));
         when(persistence.findCustomerBrands(12345)).thenReturn(of("TOM"));
         when(persistence.retrieveSimilarity("TOM", "DREAMHEAD")).thenReturn(0.2);
         when(persistence.retrieveSimilarity("TOM", "REVER")).thenReturn(0.5);
 
         com.github.smart.recommendation.RecommendationService service = new com.github.smart.recommendation.RecommendationService(persistence);
         List<String> brands = service.recommendBrands("12345", 1);
         assertThat(brands.size(), is(1));
         assertThat(brands.get(0), is("REVER"));
     }
 }
