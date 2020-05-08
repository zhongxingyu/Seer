 package com.abudko.reseller.huuto.mvc.list;
 
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.web.WebAppConfiguration;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.test.web.servlet.setup.MockMvcBuilders;
 import org.springframework.web.context.WebApplicationContext;
 
 import com.abudko.reseller.huuto.query.enumeration.BuyerId;
 import com.abudko.reseller.huuto.query.enumeration.Category;
 import com.abudko.reseller.huuto.query.enumeration.ClosedTimeLeft;
 import com.abudko.reseller.huuto.query.enumeration.CreatedTimePassed;
 import com.abudko.reseller.huuto.query.enumeration.Keyword;
 import com.abudko.reseller.huuto.query.enumeration.Location;
 import com.abudko.reseller.huuto.query.enumeration.MaxPrice;
 import com.abudko.reseller.huuto.query.enumeration.MinPrice;
 import com.abudko.reseller.huuto.query.enumeration.SellerId;
 import com.abudko.reseller.huuto.query.enumeration.Zipcode;
 import com.abudko.reseller.huuto.query.params.QueryParams;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @WebAppConfiguration
 @ContextConfiguration(locations = { "classpath:/spring/test-webapp-config.xml" })
 public class QuerySearchFormControllerTest {
 
     private static final String SEARCH_PATH = "/search";
 
     private static final String SEARCH_FORM_URL_PATH = "/WEB-INF/views/search/searchForm.jsp";
 
     private static final String REDIRECT_URL_START_PATH = "/items/search?";
 
     private static final int POST_SEARCH_FORM_EXPECTED_HTTP_CODE = 302;
 
     @Autowired
     private WebApplicationContext wac;
 
     private MockMvc mockMvc;
 
     @Before
     public void setup() {
         this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
     }
 
     @Test
     public void testEmptySearchForm() throws Exception {
         this.mockMvc.perform(get(SEARCH_PATH)).andExpect(status().isOk())
                 .andExpect(model().attribute("queryParams", new QueryParams()))
                 .andExpect(forwardedUrl(SEARCH_FORM_URL_PATH));
     }
 
     @Test
     public void testPostSearchFormKeyword() throws Exception {
         String keyword = "keyword123";
 
         this.mockMvc.perform(post(SEARCH_PATH).param("simpleParams.keyword", keyword))
                 .andExpect(status().is(POST_SEARCH_FORM_EXPECTED_HTTP_CODE))
                 .andExpect(redirectedUrl(REDIRECT_URL_START_PATH + Keyword.PARAMETER + "=" + keyword + "&"));
     }
 
     @Test
     public void testPostSearchFormLocation() throws Exception {
         String location = "location123";
 
         this.mockMvc.perform(post(SEARCH_PATH).param("simpleParams.location", location))
                 .andExpect(status().is(POST_SEARCH_FORM_EXPECTED_HTTP_CODE))
                 .andExpect(redirectedUrl(REDIRECT_URL_START_PATH + Location.PARAMETER + "=" + location + "&"));
     }
 
     @Test
     public void testPostSearchFormZipcode() throws Exception {
         String zipcode = "zipcode123";
 
         this.mockMvc.perform(post(SEARCH_PATH).param("simpleParams.zipcode", zipcode))
                 .andExpect(status().is(POST_SEARCH_FORM_EXPECTED_HTTP_CODE))
                 .andExpect(redirectedUrl(REDIRECT_URL_START_PATH + Zipcode.PARAMETER + "=" + zipcode + "&"));
     }
 
     @Test
     public void testPostSearchFormBuyerId() throws Exception {
         String buyerId = "buyerid123";
 
         this.mockMvc.perform(post(SEARCH_PATH).param("simpleParams.buyerId", buyerId))
                 .andExpect(status().is(POST_SEARCH_FORM_EXPECTED_HTTP_CODE))
                 .andExpect(redirectedUrl(REDIRECT_URL_START_PATH + BuyerId.PARAMETER + "=" + buyerId));
     }
 
     @Test
     public void testPostSearchFormSellerId() throws Exception {
         String sellerId = "sellerid123";
 
         this.mockMvc.perform(post(SEARCH_PATH).param("simpleParams.sellerId", sellerId))
                 .andExpect(status().is(POST_SEARCH_FORM_EXPECTED_HTTP_CODE))
                 .andExpect(redirectedUrl(REDIRECT_URL_START_PATH + SellerId.PARAMETER + "=" + sellerId + "&"));
     }
 
     @Test
     public void testPostSearchFormMinPrice() throws Exception {
         String minPrice = "minprice123";
 
         this.mockMvc.perform(post(SEARCH_PATH).param("simpleParams.minPrice", minPrice))
                 .andExpect(status().is(POST_SEARCH_FORM_EXPECTED_HTTP_CODE))
                 .andExpect(redirectedUrl(REDIRECT_URL_START_PATH + MinPrice.PARAMETER + "=" + minPrice + "&"));
     }
 
     @Test
     public void testPostSearchFormMaxPrice() throws Exception {
         String maxPrice = "maxprice123";
 
         this.mockMvc.perform(post(SEARCH_PATH).param("simpleParams.maxPrice", maxPrice))
                 .andExpect(status().is(POST_SEARCH_FORM_EXPECTED_HTTP_CODE))
                 .andExpect(redirectedUrl(REDIRECT_URL_START_PATH + MaxPrice.PARAMETER + "=" + maxPrice + "&"));
     }
 
     @Test
     public void testPostSearchFormCreatedTimePassed() throws Exception {
         String createdTimePassed = "12387435";
 
         this.mockMvc
                 .perform(post(SEARCH_PATH).param("simpleParams.createdTimePassed", createdTimePassed))
                 .andExpect(status().is(POST_SEARCH_FORM_EXPECTED_HTTP_CODE))
                 .andExpect(
                         redirectedUrl(REDIRECT_URL_START_PATH + CreatedTimePassed.PARAMETER + "=-" + createdTimePassed
                                 + "&"));
     }
 
     @Test
     public void testPostSearchFormClosedTimeLeft() throws Exception {
         String closedTimeLeft = "12387435";
 
         this.mockMvc
                 .perform(post(SEARCH_PATH).param("simpleParams.closedTimeLeft", closedTimeLeft))
                 .andExpect(status().is(POST_SEARCH_FORM_EXPECTED_HTTP_CODE))
                 .andExpect(
                         redirectedUrl(REDIRECT_URL_START_PATH + ClosedTimeLeft.PARAMETER + "=-" + closedTimeLeft + "&"));
     }
 
     @Test
     public void testPostSearchFormCategories() throws Exception {
         String category1 = "category1";
         String category2 = "category2";
 
         this.mockMvc
                 .perform(
                         post(SEARCH_PATH).param("simpleParams.categories", category1).param("simpleParams.categories",
                                 category2))
                 .andExpect(status().is(POST_SEARCH_FORM_EXPECTED_HTTP_CODE))
                 .andExpect(
                         redirectedUrl(REDIRECT_URL_START_PATH + Category.PARAMETER + "=" + category1 + "&"
                                 + Category.PARAMETER + "=" + category2 + "&"));
     }
 
     @Test
     public void testPostSearchFormStatus() throws Exception {
        String status = "status123";
 
        this.mockMvc.perform(post(SEARCH_PATH).param("status", status))
                 .andExpect(status().is(POST_SEARCH_FORM_EXPECTED_HTTP_CODE))
                .andExpect(redirectedUrl(REDIRECT_URL_START_PATH + "status=" + status + "&"));
    }
    
    @Test
    public void testPostSearchFormSellerType() throws Exception {
        String sellerType = "sellerType123";
        
        this.mockMvc.perform(post(SEARCH_PATH).param("status", sellerType))
        .andExpect(status().is(POST_SEARCH_FORM_EXPECTED_HTTP_CODE))
        .andExpect(redirectedUrl(REDIRECT_URL_START_PATH + "status=" + status + "&"));
     }
 }
