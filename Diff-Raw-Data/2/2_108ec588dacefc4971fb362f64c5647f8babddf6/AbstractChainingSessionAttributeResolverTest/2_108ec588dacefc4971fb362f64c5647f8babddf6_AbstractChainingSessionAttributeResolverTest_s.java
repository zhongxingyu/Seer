 package org.springframework.web.bind.support;
 
 import org.hamcrest.Matcher;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.springframework.web.bind.MissingServletRequestSessionAttributeException;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.verifyNoMoreInteractions;
 
 public class AbstractChainingSessionAttributeResolverTest {
     public static final Object RESOLUTION = new Object();
     private AbstractChainingSessionAttributeResolver resolvingResolver;
     private AbstractChainingSessionAttributeResolver nonResolvingResolver;
     private AbstractChainingSessionAttributeResolver nonResolvingResolverWithNoNextInChain;
     @Mock private SessionAttributeResolver next;
     @Mock private SessionHandler handler;
     @Mock private SessionAttributeParameter parameter;
 
     @Before
     public void setUp() throws Exception {
         MockitoAnnotations.initMocks(this);
         resolvingResolver = new ResolvingChainingSessionAttributeResolverTestImpl(next);
         nonResolvingResolver = new NonResolvingChainingSessionAttributeResolverTestImpl(next);
         nonResolvingResolverWithNoNextInChain = new NonResolvingChainingSessionAttributeResolverTestImpl(null);
     }
 
     @Test
     public void testResolveSessionAttribute_whenAbleToResolve_shouldNotInvokeNextInChain_shouldReturnResolution() throws Exception {
         assertThat(resolvingResolver.resolveSessionAttribute(handler, parameter), is(equalTo(RESOLUTION)));
         verifyNoMoreInteractions(next, handler, parameter);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testResolveSessionAttribute_whenUnableAbleToResolve_shouldInvokeNextInChain_whenNextInChainIsNull_shouldFailFast() throws Exception {
         nonResolvingResolverWithNoNextInChain.resolveSessionAttribute(handler, parameter);
     }
 
     @Test
     public void testResolveSessionAttribute_whenUnableAbleToResolve_shouldInvokeNextInChain() throws Exception {
        assertThat(nonResolvingResolver.resolveSessionAttribute(handler, parameter), passesTheBuckAround());
         verify(next).resolveSessionAttribute(handler, parameter);
     }
 
     private class ResolvingChainingSessionAttributeResolverTestImpl extends AbstractChainingSessionAttributeResolver {
         public ResolvingChainingSessionAttributeResolverTestImpl(SessionAttributeResolver next) {
             super(next);
         }
 
         @Override
         protected Object resolveSessionAttributeInternal(SessionHandler handler, SessionAttributeParameter parameter) throws MissingServletRequestSessionAttributeException {
             return RESOLUTION;
         }
     }
 
     private class NonResolvingChainingSessionAttributeResolverTestImpl extends AbstractChainingSessionAttributeResolver {
         public NonResolvingChainingSessionAttributeResolverTestImpl(SessionAttributeResolver next) {
             super(next);
         }
 
         @Override
         protected Object resolveSessionAttributeInternal(SessionHandler handler, SessionAttributeParameter parameter) throws MissingServletRequestSessionAttributeException {
             return passTheBuck();
         }
     }
 
     protected static Matcher<Object> passesTheBuckAround() {
         return is(equalTo(AbstractChainingSessionAttributeResolver.PASSING_THE_BUCK_AROUND));
     }
 }
