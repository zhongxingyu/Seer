 package com.abudko.reseller.huuto.mvc.list;
 
 import static com.abudko.reseller.huuto.mvc.ControllerHelper.ADD_TIMES_ATTRIBUTE;
 import static com.abudko.reseller.huuto.mvc.ControllerHelper.BRAND_ATTRIBUTE;
 import static com.abudko.reseller.huuto.mvc.ControllerHelper.CATEGORIES_ATTRIBUTE;
 import static com.abudko.reseller.huuto.mvc.ControllerHelper.SEARCH_PARAMS_ATTRIBUTE;
 import static org.hamcrest.Matchers.instanceOf;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.mock.web.MockHttpSession;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.web.WebAppConfiguration;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.test.web.servlet.setup.MockMvcBuilders;
 import org.springframework.web.context.WebApplicationContext;
 
 import com.abudko.reseller.huuto.mvc.ControllerHelper;
 import com.abudko.reseller.huuto.query.QueryConstants;
 import com.abudko.reseller.huuto.query.builder.ParamBuilder;
 import com.abudko.reseller.huuto.query.enumeration.Addtime;
 import com.abudko.reseller.huuto.query.enumeration.Category;
 import com.abudko.reseller.huuto.query.html.list.ListResponse;
 import com.abudko.reseller.huuto.query.params.SearchParams;
 import com.abudko.reseller.huuto.query.rules.SearchQueryRules;
 import com.abudko.reseller.huuto.query.service.list.QueryListService;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @WebAppConfiguration
 @ContextConfiguration(locations = { "classpath:/spring/test-webapp-config.xml" })
 public class SearchControllerTest {
 
     private static final String SEARCH_PATH = "/search";
 
     private static final String SEARCH_RESULTS_PATH = "/search/results";
 
     private static final String REDIRECTED_TO_RESULTS_URL_PATH = "/items/search/results";
 
     private static final String SEARCH_FORM_URL_PATH = "/WEB-INF/views/search/search.jsp";
 
     @Autowired
     private WebApplicationContext wac;
 
     @Autowired
     private QueryListService queryListService;
 
     @Autowired
     private SearchQueryRules searchQueryRules;
 
     @Autowired
     @Qualifier("searchParamBuilder")
     private ParamBuilder searchParamBuilder;
 
     @Autowired
     private ControllerHelper controllerHelper;
 
     private MockMvc mockMvc;
 
     private MockHttpSession session;
 
     private SearchParams searchParams;
 
     @Before
     public void setup() {
         this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
         this.session = new MockHttpSession();
 
         searchParams = new SearchParams();
     }
 
     @Test
     public void testSearchFormGetSearchParams() throws Exception {
         this.mockMvc.perform(get(SEARCH_PATH).session(session)).andExpect(status().isOk())
                 .andExpect(model().attribute(SEARCH_PARAMS_ATTRIBUTE, instanceOf(SearchParams.class)))
                 .andExpect(forwardedUrl(SEARCH_FORM_URL_PATH));
     }
 
     @Test
     public void testSearchFormGetAddtimes() throws Exception {
         this.mockMvc.perform(get(SEARCH_PATH).session(session)).andExpect(status().isOk())
                 .andExpect(model().attribute(ADD_TIMES_ATTRIBUTE, Addtime.values()))
                 .andExpect(forwardedUrl(SEARCH_FORM_URL_PATH));
     }
 
     @Test
     public void testSearchFormGetCategories() throws Exception {
         this.mockMvc.perform(get(SEARCH_PATH).session(session)).andExpect(status().isOk())
                 .andExpect(model().attribute(CATEGORIES_ATTRIBUTE, Category.values()))
                 .andExpect(forwardedUrl(SEARCH_FORM_URL_PATH));
     }
 
     @Test
     public void testSearchFormGetBrandsDefault() throws Exception {
         this.mockMvc.perform(get(SEARCH_PATH).session(session)).andExpect(status().isOk())
                 .andExpect(model().attribute(BRAND_ATTRIBUTE, controllerHelper.getBrands(Category.TALVIHAALARI)))
                 .andExpect(forwardedUrl(SEARCH_FORM_URL_PATH));
     }
 
     @Test
     public void testSearchFormGetBrandsDependsOnCategory() throws Exception {
         SearchParams searchCriteria = new SearchParams();
         Category lenkkarit = Category.LENKKARIT;
         searchCriteria.setWords(lenkkarit.name());
         session.setAttribute(SEARCH_PARAMS_ATTRIBUTE, searchCriteria);
 
         this.mockMvc.perform(get(SEARCH_PATH).session(session)).andExpect(status().isOk())
                 .andExpect(model().attribute(BRAND_ATTRIBUTE, controllerHelper.getBrands(lenkkarit)))
                 .andExpect(forwardedUrl(SEARCH_FORM_URL_PATH));
     }
 
     @Test
     public void testSearchFormGetSearchParamsInSession() throws Exception {
         searchParams.setAddtime("addtime");
         searchParams.setBiddernro("biddernro");
         this.session.setAttribute("searchParams", this.searchParams);
 
         this.mockMvc.perform(get(SEARCH_PATH).session(session)).andExpect(status().isOk())
                 .andExpect(model().attribute("searchParams", this.searchParams))
                 .andExpect(forwardedUrl(SEARCH_FORM_URL_PATH));
     }
 
     @Test
     public void testSearchFormPost() throws Exception {
         searchParams.setAddtime("addtime");
         searchParams.setBiddernro("biddernro");
         searchParams.setCategory("category");
         searchParams.setLocation("location");
         searchParams.setPrice_min("10");
         searchParams.setPrice_max("20");
         this.session.setAttribute("searchParams", this.searchParams);
         List<ListResponse> searchResults = new ArrayList<ListResponse>();
         searchResults.add(new ListResponse());
         String query = "query";
         when(this.searchParamBuilder.buildQuery(searchParams)).thenReturn(query);
         when(this.queryListService.search(QueryConstants.QUERY_URL + query, searchParams)).thenReturn(searchResults);
 
         this.mockMvc.perform(post(SEARCH_PATH).session(session)).andExpect(status().isMovedTemporarily())
                 .andExpect(model().attribute("searchResults", searchResults))
                 .andExpect(model().attribute("searchParams", this.searchParams))
                 .andExpect(redirectedUrl(REDIRECTED_TO_RESULTS_URL_PATH));
 
         verify(this.searchQueryRules).apply(searchParams);
         verify(this.searchParamBuilder).buildQuery(searchParams);
     }
 
     @Test
     public void testSearchFormPostPathSearchResults() throws Exception {
         searchParams.setAddtime("addtime");
         searchParams.setBiddernro("biddernro");
         searchParams.setCategory("category");
         searchParams.setLocation("location");
         searchParams.setPrice_min("10");
         searchParams.setPrice_max("20");
         this.session.setAttribute("searchParams", this.searchParams);
         List<ListResponse> searchResults = new ArrayList<ListResponse>();
         searchResults.add(new ListResponse());
         String query = "query";
         when(this.searchParamBuilder.buildQuery(searchParams)).thenReturn(query);
         when(this.queryListService.search(QueryConstants.QUERY_URL + query, searchParams)).thenReturn(searchResults);
 
         this.mockMvc.perform(post(SEARCH_RESULTS_PATH).session(session)).andExpect(status().isMovedTemporarily())
                 .andExpect(model().attribute("searchResults", searchResults))
                 .andExpect(model().attribute("searchParams", this.searchParams))
                 .andExpect(redirectedUrl(REDIRECTED_TO_RESULTS_URL_PATH));
 
         verify(this.searchQueryRules).apply(searchParams);
         verify(this.searchParamBuilder).buildQuery(searchParams);
     }
 
     @Test
     public void testSearchResults() throws Exception {
         this.session.setAttribute("searchParams", this.searchParams);
         List<ListResponse> searchResults = new ArrayList<ListResponse>();
         searchResults.add(new ListResponse());
         this.session.setAttribute("searchResults", searchResults);
 
         this.mockMvc.perform(get(SEARCH_RESULTS_PATH).session(session)).andExpect(status().isOk())
                 .andExpect(model().attribute("searchParams", searchParams))
                 .andExpect(model().attribute("searchResults", searchResults))
                 .andExpect(forwardedUrl(SEARCH_FORM_URL_PATH));
     }
 
     @Test
     public void testGetBrands() throws Exception {
         Category category = Category.LENKKARIT;
         MockHttpServletResponse response = this.mockMvc.perform(get("/brands/" + category.name())).andReturn()
                 .getResponse();
 
         assertEquals(
                 "[\"NO_BRAND\",\"Не важно\",\"ADIDAS\",\"Adidas\",\"ASICS\",\"Asics\",\"NIKE\",\"Nike\",\"PUMA\",\"Puma\",\"VIKING\",\"Viking\"]",
                 response.getContentAsString());
     }
 
     @Test
     public void testGetBrandsInvalidCategory() throws Exception {
        this.mockMvc.perform(get("/brands/INVALID_CATEGORY")).andExpect(redirectedUrl("/error.jsp"));
     }
 }
