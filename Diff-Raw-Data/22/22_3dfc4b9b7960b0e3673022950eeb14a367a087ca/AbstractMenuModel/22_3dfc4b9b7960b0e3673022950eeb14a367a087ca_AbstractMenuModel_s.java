 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package de.javauni.jarcade.model.menu;
 
 import de.javauni.jarcade.event.Channel;
 import de.javauni.jarcade.exceptions.IllegalAction;
 import de.javauni.jarcade.model.state.AbstractStateModel;
 import de.javauni.jarcade.model.state.StateListener;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @param <S>
  * @author wabu
  */
 public class AbstractMenuModel<S extends Enum<S>>
         extends AbstractStateModel<S>
         implements MenuModelAccess, MenuModelExport<S>{
     private final Map<S, ? extends List<? extends MenuItem>> pages;
     private List<? extends MenuItem> page;
     private int sel = 0;
 
    public AbstractMenuModel(Channel<StateListener<S>> channel, Map<S, ? extends List<? extends MenuItem>> pages) {
         super(channel);
         this.pages = pages;
     }
 
     @Override
     protected void doStateTransition(S src, S tgt) throws IllegalAction {
         if(!pages.containsKey(tgt)) {
            new IllegalAction(new UnsupportedOperationException
                     ("menu "+tgt+" has not been implemented"));
         }
         page = pages.get(tgt);
         sel = 0;
     }
 
     public List<? extends MenuItem> getItems() {
         return page;
     }
 
     public MenuItem getSelectedItem() {
         return page.get(sel);
     }
 
     public void select(int i) {
         sel = i;
     }
 
     public void selectNext() {
         select((sel+1) % page.size());
     }
 
     public void selectPrevious() {
         select((sel-1+page.size()) % page.size());
     }
 
     public void submit() {
         submit(sel);
     }
 
     public void submit(int i) {
         page.get(i).submit();
     }
 }
