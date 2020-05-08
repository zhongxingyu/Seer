 /**
  * 
  */
 package org.synyx.minos.core.web.menu;
 
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

 import org.junit.Test;
import org.mockito.Mockito;
 
 
 /**
  * @author Marc Kannegiesser - kannegiesser@synyx.de
  */
 public class FirstSubMenuUrlResolverUnitTest {
 
     @Test
     public void resolvesUrlOfFirstSubMenu() {
 
         FirstSubMenuUrlResolver strategy = new FirstSubMenuUrlResolver();
 
        Menu first = Mockito.mock(Menu.class);
        when(first.getUrl()).thenReturn("FOO");
 
        Menu second = Mockito.mock(Menu.class);
        Menu item = Menu.create(MenuItem.create("BAR").withUrlResolver(strategy).build(), new MenuItems(first, second));
 
         String url = strategy.resolveUrl(item);
 
         assertThat(url, is("FOO"));
     }
 
 
     @Test
     public void resolvesToNullIfNoSubmenus() {
 
        Menu item = Mockito.mock(Menu.class);
 
         FirstSubMenuUrlResolver strategy = new FirstSubMenuUrlResolver();
 
        when(item.getSubMenues()).thenReturn(new MenuItems());

         String url = strategy.resolveUrl(item);
 
         assertNull(url);
     }
 
 }
