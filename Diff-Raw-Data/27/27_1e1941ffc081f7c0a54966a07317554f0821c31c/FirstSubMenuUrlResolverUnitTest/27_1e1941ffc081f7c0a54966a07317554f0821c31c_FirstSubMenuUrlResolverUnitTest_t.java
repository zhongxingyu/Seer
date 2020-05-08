 /**
  * 
  */
 package org.synyx.minos.core.web.menu;
 
 import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
 
 
 /**
  * @author Marc Kannegiesser - kannegiesser@synyx.de
  */
 public class FirstSubMenuUrlResolverUnitTest {
 
     @Test
     public void resolvesUrlOfFirstSubMenu() {
 
         FirstSubMenuUrlResolver strategy = new FirstSubMenuUrlResolver();
 
        Menu first = Menu.create(MenuItem.create("FOO").withUrlResolver(new SimpleUrlResolver("FOO")).build());
        Menu second = Menu.create(MenuItem.create("BAR").withUrlResolver(new SimpleUrlResolver("BAR")).build());
 
        Menu item = Menu.create(MenuItem.create("FOOBAR").withUrlResolver(strategy).build(),
                new MenuItems(first, second));
 
         String url = strategy.resolveUrl(item);
 
         assertThat(url, is("FOO"));
     }
 
 
     @Test
     public void resolvesToNullIfNoSubmenus() {
 
        Menu item = Menu.create(MenuItem.create("FOO").withUrlResolver(new SimpleUrlResolver("FOO")).build(),
                new MenuItems());
 
         FirstSubMenuUrlResolver strategy = new FirstSubMenuUrlResolver();
 
         String url = strategy.resolveUrl(item);
 
         assertNull(url);
     }
 
 }
