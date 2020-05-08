 package org.suggs.test.sandbox.concordion.settlement.securitiessettlement;
 
 import org.agileinsider.concordion.junit.ConcordionPlus;
 import org.junit.runner.RunWith;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 import org.suggs.test.sandbox.concordion.settlement.securitiessettlement.support.dsl.DSL;
 import org.suggs.test.sandbox.concordion.settlement.securitiessettlement.support.dsl.DslSpringContext;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 @RunWith(ConcordionPlus.class)
public class AbstractSecuritiesSettlementFixture {
     protected DSL dsl;
 
     public AbstractSecuritiesSettlementFixture() {
         ApplicationContext ctx = new AnnotationConfigApplicationContext(DslSpringContext.class);
         dsl = (DSL) ctx.getBean("DSL");
         assertThat(dsl, is(notNullValue()));
     }
 
 }
