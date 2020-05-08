 package com.huskycode.jpaquery;
 
 import com.huskycode.jpaquery.DependenciesDefinition;
 import com.huskycode.jpaquery.link.Link;
 import org.hamcrest.CoreMatchers;
 import org.junit.Assert;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import javax.persistence.metamodel.SingularAttribute;
 
 /**
  * @author Varokas Panusuwan
  */
 public class DependenciesDefinitionTest {
     @Test
     public void shouldBeAbleToDefineDependenciesByLinks() {
         SingularAttribute pointA = Mockito.mock(SingularAttribute.class);
         SingularAttribute pointB = Mockito.mock(SingularAttribute.class);
        Link anyLink = Link.from(Object.class, pointA).to(Object.class, pointB);
 
         DependenciesDefinition deps =
                 DependenciesDefinition.fromLinks(new Link[] { anyLink });
 
         Assert.assertThat(deps.getLinks().length, CoreMatchers.is(1));
         Assert.assertThat(deps.getLinks()[0],
                 CoreMatchers.is(CoreMatchers.sameInstance(anyLink)));
     }
 }
