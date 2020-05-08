 package com.theoryinpractise.halbuilder.guava;
 
 import com.google.common.base.Function;
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory;
 import com.theoryinpractise.halbuilder.api.ReadableRepresentation;
 import com.theoryinpractise.halbuilder.api.RepresentationFactory;
 import org.testng.annotations.Test;
 
 import javax.annotation.Nullable;
 import java.io.InputStreamReader;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 
 public class InterfaceSatisfactionTest {
 
    private RepresentationFactory representationFactory = new DefaultRepresentationFactory();
 
     public static interface IPerson {
         Integer getAge();
 
         Boolean getExpired();
 
         Integer getId();
 
         String getName();
     }
 
     public static interface INamed {
         String name();
     }
 
     public static interface IJob {
         Integer getJobId();
     }
 
     public static interface ISimpleJob {
         Integer jobId();
     }
 
     public static interface INullprop {
         String nullprop();
     }
 
     @Test
     public void testFunctionalInterfaceSatisfaction() {
 
         ReadableRepresentation representation = representationFactory.readRepresentation(new InputStreamReader(InterfaceSatisfactionTest.class.getResourceAsStream("/example.xml")));
 
         String name = Representations.ifSatisfiedBy(representation, IPerson.class, new Function<IPerson, String>() {
             public String apply(@Nullable IPerson iPerson) {
                 return iPerson.getName();
             }
         }).get();
 
         assertThat(name).isEqualTo("Example Resource");
 
     }
 
 }
