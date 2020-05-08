 package com.visionarysoftwaresolutions.menu;
 
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import com.visionarysoftwaresolutions.menu.composite.MenuComponent;
 import com.visionarysoftwaresolutions.menu.composite.MenuComposite;
 import com.visionarysoftwaresolutions.menu.composite.MenuLeaf;
 import com.visionarysoftwaresolutions.util.NullChecker;
 
 public class MenuFactory implements AbstractMenuFactory {
 
 	@Override
 	public MenuComponent createMenu(String name){
 		return new MenuComposite(name);
 	}
 	
 	@Override
 	public MenuComponent addSubMenu(MenuComponent menu, MenuComponent newMenu) {
 		NullChecker.checkNull(menu);
 		NullChecker.checkNull(newMenu);
 		menu.add(newMenu);
 		return menu;
 	}
 	
 	@Override
 	public Set<MenuComponent> getSubMenus(MenuComponent menu){
 		NullChecker.checkNull(menu);
 		Set<MenuComponent> result = new LinkedHashSet<MenuComponent>();
 		MenuIterator iterator = menu.createIterator();
 		while(iterator.hasNext()){
 			MenuComponent contained = iterator.next();
			if(contained instanceof MenuComposite || contained.hasChildren())
 			{
 				result.add(contained);
 			}
 		}
 		return result;
 	}
 	
 	public MenuComponent addItemToMenu(MenuComponent menu, String item){
 		NullChecker.checkNull(menu);
 		NullChecker.checkNull(item);
 		menu.add(new MenuLeaf(item));
 		return menu;
 	}
 }
