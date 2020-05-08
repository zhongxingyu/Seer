 package com.github.lalyos.domain;
 
 import java.util.Collection;
 
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.hibernate.validator.constraints.Length;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.hibernate.validator.constraints.Range;
 import org.hibernate.validator.constraints.ScriptAssert;
 
 import flexjson.JSONSerializer;
 
@ScriptAssert(lang="javascript", script="_this.calories > _this.price", message="{croosfield.error}")
 public class Food {
     
     @Range(min=100, max=2000)
     Integer calories;
     
     @Size(min=5)
     String name;
     
     @NotNull
     @Min(100)
     Integer price;
     
     public Integer getCalories() {
         return calories;
     }
     public void setCalories(Integer calories) {
         this.calories = calories;
     }
     public String getName() {
         return name;
     }
     public void setName(String name) {
         this.name = name;
     }
     public Integer getPrice() {
         return price;
     }
     public void setPrice(Integer price) {
         this.price = price;
     }
     
     public String toJson() {
         return new JSONSerializer().exclude("*.class").serialize(this);
     }
 
     public static String toJsonArray(Collection<Food> collection) {
         return new JSONSerializer().exclude("*.class").serialize(collection);
     }
 
 
 }
