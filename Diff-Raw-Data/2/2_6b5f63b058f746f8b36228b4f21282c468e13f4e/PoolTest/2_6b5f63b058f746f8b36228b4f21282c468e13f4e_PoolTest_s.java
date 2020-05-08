 package com.porvak.bracket.domain;
 
 import org.apache.commons.lang3.builder.ToStringBuilder;
 import org.apache.commons.lang3.builder.ToStringStyle;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.junit.Test;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 
 import java.io.IOException;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 
 public class PoolTest {
     
     ObjectMapper mapper = new ObjectMapper();
     
     @Test
     public void testBasicBinding() throws IOException {
         Resource poolJsonResource = new ClassPathResource("/data/pool.json");
         Pool pool = mapper.readValue(poolJsonResource.getInputStream(), Pool.class);
         System.out.println(ToStringBuilder.reflectionToString(pool, ToStringStyle.MULTI_LINE_STYLE));
         assertThat(pool.getPoolName(), is("2012 NCAA Global Pool"));
         assertThat(pool.getScoringStrategy().size(), is(7));
        assertThat(pool.getScoringStrategy(), hasEntry(7,32));
     }
 }
