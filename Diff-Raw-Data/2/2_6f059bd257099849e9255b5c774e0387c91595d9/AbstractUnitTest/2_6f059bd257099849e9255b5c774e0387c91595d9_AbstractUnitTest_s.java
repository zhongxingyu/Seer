 package pl.kikko.test.unit;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.rules.ExpectedException;
 
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class AbstractUnitTest {
 
     @Rule
    protected ExpectedException expectedException = ExpectedException.none();
 
     @Before
     public void injectMocks() {
         initMocks(this);
     }
 
 }
