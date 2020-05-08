 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package de.javauni.yarrish.model.menu;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import de.javauni.jarcade.event.Channel;
 import de.javauni.jarcade.model.menu.AbstractMenuModel;
 import de.javauni.jarcade.model.menu.MenuItem;
 import de.javauni.jarcade.model.state.StateListener;
 import de.javauni.utils.guice.ManagedScope;
 import de.javauni.yarrish.model.main.MainModelAccess;
 import de.javauni.yarrish.model.main.MainState;
 import java.util.List;
 
 /**
  *
  * @author wabu
  */
 @ManagedScope
 class MainMenuImpl extends AbstractMenuModel<MainMenuState>
     implements MainMenuAccess, MainMenuExport {
 
     // TODO implemnen a menu
 
     @Inject
     MainMenuImpl(Channel<StateListener<MainMenuState>> channel,
                 final MainModelAccess main) {
         super(channel,
                 ImmutableMap.<MainMenuState, List<? extends MenuItem>>builder()
                 .put(MainMenuState.Main,
                     Lists.newArrayList(new MenuItem() {
                         public void submit() {
                             main.setState(MainState.Game);
                         }
                     })
                ).build(), MainMenuState.Main);
     }
 
 
 
 }
