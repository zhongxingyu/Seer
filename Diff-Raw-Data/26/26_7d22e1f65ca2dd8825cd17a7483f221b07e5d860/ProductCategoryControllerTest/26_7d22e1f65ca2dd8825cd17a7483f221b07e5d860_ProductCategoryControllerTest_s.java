 package oshop.web.api.rest;
 
 import org.junit.Test;
 import org.springframework.context.i18n.LocaleContextHolder;
 import org.springframework.http.MediaType;
 import org.springframework.test.web.servlet.MvcResult;
 import oshop.model.ProductCategory;
 
 import java.math.BigDecimal;
 
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
 import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
 public class ProductCategoryControllerTest extends BaseControllerTest {
 
     @Test
     public void testAddCategory() throws Exception {
         ProductCategory category = new ProductCategory();
         category.setName("name1");
 
         String categoryAsString = mapper.writeValueAsString(category);
 
         this.mockMvc.perform(
                 post("/api/productCategories/")
                         .accept(MediaType.APPLICATION_JSON)
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(categoryAsString))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(jsonPath("$.name").value("name1"));
     }
 
     @Test
     public void testAddCategoryWithNullName() throws Exception {
         ProductCategory category = new ProductCategory();
         String categoryAsString = mapper.writeValueAsString(category);
 
         this.mockMvc.perform(
                 post("/api/productCategories/")
                         .accept(MediaType.APPLICATION_JSON)
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(categoryAsString))
                 .andDo(print())
                 .andExpect(status().isBadRequest())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(jsonPath("$.error").value("Validation failed"));
                 //It is not stable order to test mesages - sometime "may not be empty" is first some times is not
                 //.andExpect(jsonPath("$.fields.name.[0]").value("may not be empty"))
                 //.andExpect(jsonPath("$.fields.name.[1]").value("may not be null"));
     }
 
     @Test
     public void testGetProductCategoryById() throws Exception {
         Integer id = addProductCategory("category1").getId();
 
         this.mockMvc.perform(
                 get("/api/productCategories/{id}", id)
                 .accept(MediaType.APPLICATION_JSON)
                 .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(jsonPath("$.name").value("category1"))
                 .andExpect(jsonPath("$.id").value(id));
     }
 
     @Test
     public void testGetProductCategoryByIdNotExists() throws Exception {
         Integer id = addProductCategory("category1").getId();
 
        this.mockMvc.perform(
                 get("/api/productCategories/{id}", id + 1)
                 .accept(MediaType.APPLICATION_JSON)
                 .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                 .andExpect(status().isNotFound())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(content().string("\"" + messageSource.getMessage("error.entity.by.id.not.found",
                         new Object[]{id}, LocaleContextHolder.getLocale()) + "\""));
     }
 
     @Test
     public void testRemove() throws Exception {
         Integer id = addProductCategory("category1").getId();
 
         this.mockMvc.perform(
                 delete("/api/productCategories/{id}", id)
                         .accept(MediaType.APPLICATION_JSON)
                         .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isNoContent());
     }
 
     @Test
     public void testUpdate() throws Exception {
         ProductCategory productCategory = addProductCategory("category1");
         productCategory.setName("category2");
 
         this.mockMvc.perform(
                 put("/api/productCategories/{id}", productCategory.getId())
                         .content(mapper.writeValueAsString(productCategory))
                         .accept(MediaType.APPLICATION_JSON)
                         .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$.name").value("category2"));
     }
 
     @Test
     public void testRemoveNotExisting() throws Exception {
         this.mockMvc.perform(
                 delete("/api/productCategories/{id}", 1)
                         .accept(MediaType.APPLICATION_JSON)
                         .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isNotFound());
     }
 
     @Test
     public void testListProductCategories() throws Exception {
         addProductCategories("category1", "category2", "category3");
 
         this.mockMvc.perform(
                 get("/api/productCategories/")
                         .accept(MediaType.APPLICATION_JSON)
                         .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(jsonPath("$.size").value(3))
                 .andExpect(jsonPath("$.values[0].name").value("category1"))
                 .andExpect(jsonPath("$.values[1].name").value("category2"))
                 .andExpect(jsonPath("$.values[2].name").value("category3"));
 
         this.mockMvc.perform(
                 get("/api/productCategories/?offset=1&limit=1")
                         .accept(MediaType.APPLICATION_JSON)
                         .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(jsonPath("$.size").value(3))
                 .andExpect(jsonPath("$.values[0].name").value("category2"));
     }
 
     @Test
     public void testListEmptyProductCategories() throws Exception {
         this.mockMvc.perform(
                 get("/api/productCategories/")
                         .accept(MediaType.APPLICATION_JSON)
                         .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isNoContent())
                 .andExpect(content().bytes(new byte[]{}));
     }
 
     @Test
     public void testListProductCategoriesWithFiltersAndSorters() throws Exception {
         addProductCategories("category1", "category2", "category3");
 
         this.mockMvc.perform(
                 get("/api/productCategories/filter;name=category1,category3/sort;name=desc")
                         .accept(MediaType.APPLICATION_JSON)
                         .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size").value(2))
                 .andExpect(jsonPath("$.values.[0].name").value("category3"))
                .andExpect(jsonPath("$.values.[1].name").value("category1"));
 
         //Test filter only
         this.mockMvc.perform(
                 get("/api/productCategories/filter;name=category2/sort")
                         .accept(MediaType.APPLICATION_JSON)
                         .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(jsonPath("$.size").value(1))
                 .andExpect(jsonPath("$.values.[0].name").value("category2"));
 
         //Test sort only
         this.mockMvc.perform(
                 get("/api/productCategories/filter/sort;name=asc?limit=1")
                         .accept(MediaType.APPLICATION_JSON)
                         .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(jsonPath("$.size").value(3))
                 .andExpect(jsonPath("$.values.[0].name").value("category1"));
     }
 
     @Test
     public void testEmptyListProductCategoriesWithFiltersAndSorters() throws Exception {
 
         this.mockMvc.perform(
                 get("/api/productCategories/filter;name=category1,category3/sort;name=desc")
                         .accept(MediaType.APPLICATION_JSON)
                         .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isNoContent());
     }
 
     @Test
     public void testListProductCategoriesById() throws Exception {
         Integer id = addProductCategory("category1").getId();
         addProductCategories("category2", "category3");
 
         this.mockMvc.perform(
                 get("/api/productCategories/{id}", id)
                         .accept(MediaType.APPLICATION_JSON)
                         .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(jsonPath("$.name").value("category1"));
     }
 
     @Test
     public void testListProducts() throws Exception {
         ProductCategory productCategory = addProductCategory("category1");
 
         addProducts(
                 createProduct(productCategory, "Product1", new BigDecimal(10.1)),
                 createProduct(productCategory, "Product2", new BigDecimal(10.01)),
                 createProduct(productCategory, "Product3", new BigDecimal(10.001)));
 
         MvcResult result = this.mockMvc.perform(
                 get("/api/productCategories/{id}/products", productCategory.getId()).accept(MediaType.APPLICATION_JSON))
 
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(jsonPath("$.size").value(3))
                 .andExpect(jsonPath("$.values[0].name").value("Product1"))
                 .andExpect(jsonPath("$.values[1].name").value("Product2"))
                 .andExpect(jsonPath("$.values[2].name").value("Product3"))
                 .andExpect(jsonPath("$.values[0].price").value(10.1))
                 .andExpect(jsonPath("$.values[1].price").value(10.01))
                 .andExpect(jsonPath("$.values[2].price").value(10.0)).andReturn();
 
         logResponse(result);
     }
 
     @Test
     public void testListProductsWithOrderingAndFiltering() throws Exception {
         ProductCategory productCategory = addProductCategory("category1");
 
         addProducts(createProduct(productCategory, "Product1", new BigDecimal(10.01)),
                 createProduct(productCategory, "Product2", new BigDecimal(10.1)),
                 createProduct(productCategory, "Product3", new BigDecimal(10.2)));
 
         this.mockMvc.perform(
                 get("/api/productCategories/{id}/products/filter;name=Product/sort;name=asc", productCategory.getId())
                         .accept(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(jsonPath("$.size").value(3))
                 .andExpect(jsonPath("$.values[0].name").value("Product1"))
                 .andExpect(jsonPath("$.values[1].name").value("Product2"))
                 .andExpect(jsonPath("$.values[2].name").value("Product3"))
                 .andExpect(jsonPath("$.values[0].price").value(10.01))
                 .andExpect(jsonPath("$.values[1].price").value(10.1))
                 .andExpect(jsonPath("$.values[2].price").value(10.2));
     }
 }
