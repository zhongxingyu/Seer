 import com.github.tasubo.FluentRmiException;
 import org.junit.Before;
 import org.junit.Test;
 
 import static com.github.tasubo.FluentRmi.*;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 public class ConfigTest {
 
    @Test
    public void shouldWorkWithoutExplicitConfig() {
         server().kill();
         config().reset();
         server().bind(new SomeClass()).to("someInterface");
 
         SomeClass remoteRmi = client().get("someInterface").as(SomeClass.class);
         assertThat(remoteRmi.getText(), is("trying"));
     }
 
     @Test
     public void shouldWorkOnIpAddressAndDifferentPort() {
        server().kill();
        config().reset();
 
         config().setHostname("127.0.0.1");
         config().setPort(6899);
 
         server().bind(new SomeClass()).to("someInterface");
 
         SomeClass remoteRmi = client().get("someInterface").as(SomeClass.class);
         assertThat(remoteRmi.getText(), is("trying"));
     }
 
     @Test(expected = FluentRmiException.class)
     public void shouldFailToConnectOnDifferentSettings() {
        server().kill();
        config().reset();
 
         server().bind(new SomeClass()).to("someInterface");
 
         config().setHostname("127.0.0.1");
        config().setPort(6899);
 
         SomeClass remoteRmi = client().get("someInterface").as(SomeClass.class);
         assertThat(remoteRmi.getText(), is("trying"));
     }
 
     public static class SomeClass {
 
         public String getText() {
             return "trying";
         }
     }
 
 }
