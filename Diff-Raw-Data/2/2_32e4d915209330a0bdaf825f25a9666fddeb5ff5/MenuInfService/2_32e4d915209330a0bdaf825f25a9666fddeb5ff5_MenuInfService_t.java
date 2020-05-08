 package com.travel.service;
 
 import java.util.List;
 import java.util.Set;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.travel.dao.MenuInfDAO;
 import com.travel.entity.MenuInf;
 
 @Service
 public class MenuInfService
 {
 	@Autowired
 	private MenuInfDAO menuDao;
 
 	/**
 	 * @param menuList
 	 * @return
 	 */
 	public String generateMenuInfor(List<MenuInf> menuList) {
 		StringBuilder menuInfStr = new StringBuilder();
 		for(int i = 0; i < menuList.size(); i++){
 			MenuInf menu = menuList.get(i);
 			if(menu.getMenuInf() == null){ //父菜单
 				if(i == 0){ // 第一个父菜单					
 					menuInfStr.append("<li><a>" + menu.getName() + "</a><ul>");
 				} else {
 					menuInfStr.append("</ul></li><li><a>" + menu.getName() + "</a><ul>");
 				}
 			} else { // 子菜单
 				menuInfStr.append("<li><a href=\"" + menu.getUrl() + "\" target=\"navTab\" rel=\"" + menu.getName() + "\">" + menu.getName() + "</a></li>");
 				if(i == menuList.size() - 1){
 					menuInfStr.append("</ul></li>");
 				}
 			}
 		}
 		return menuInfStr.toString();
 	}
 
 	/**
 	 * @return
 	 */
 	public List<MenuInf> getAllMenuItem() {
 		return menuDao.findAll();
 	}
 	
 	/**
 	 * @param menuList
 	 * @return
 	 */
 	public String generateMenuInforSelect(List<MenuInf> menuList) {
 		StringBuilder menuInfStr = new StringBuilder();
 		for(int i = 0; i < menuList.size(); i++){
 			MenuInf menu = menuList.get(i);
 			if(menu.getMenuInf() == null){ //父菜单
 				if(i == 0){ // 第一个父菜单					
 					menuInfStr.append("<li><a tname=\"treeMenuId"+ menu.getId() +"\" tvalue=\""+ menu.getId() +"\">" + menu.getName() + "</a><ul>");
 				} else {
 					menuInfStr.append("</ul></li><li><a tname=\"treeMenuId"+ menu.getId() +"\" tvalue=\""+ menu.getId() +"\">" + menu.getName() + "</a><ul>");
 				}
 			} else { // 子菜单
 				menuInfStr.append("<li><a tname=\"treeMenuId"+ menu.getId() +"\" tvalue=\""+ menu.getId() +"\">" + menu.getName() + "</a></li>");
 				if(i == menuList.size() - 1){
 					menuInfStr.append("</ul></li>");
 				}
 			}
 		}
 		return menuInfStr.toString();
 	}
 	
 	/**
 	 * @param menuList
 	 * @return
 	 */
 	public String generateMenuInforSelect(List<MenuInf> menuList, List<MenuInf>selectedMenuList) {
 		StringBuilder menuInfStr = new StringBuilder();
 		for(int i = 0; i < menuList.size(); i++){
 			MenuInf menu = menuList.get(i);
			String checkStr = "";
 			for(MenuInf selectedMenu : selectedMenuList){
 				if(selectedMenu.getId().longValue() == menu.getId().longValue()){
 					checkStr = "checked=\"checked\"";
 					break;
 				}
 			}
 			if(menu.getMenuInf() == null){ //父菜单
 				if(i == 0){ // 第一个父菜单					
 					menuInfStr.append("<li><a "+ checkStr +" tname=\"treeMenuId"+ menu.getId() +"\" tvalue=\""+ menu.getId() +"\" >" + menu.getName() + "</a><ul>");
 				} else {
 					menuInfStr.append("</ul></li><li><a "+ checkStr +" tname=\"treeMenuId"+ menu.getId() +"\" tvalue=\""+ menu.getId() +"\">" + menu.getName() + "</a><ul>");
 				}
 			} else { // 子菜单
 				menuInfStr.append("<li><a "+ checkStr +" tname=\"treeMenuId"+ menu.getId() +"\" tvalue=\""+ menu.getId() +"\">" + menu.getName() + "</a></li>");
 				if(i == menuList.size() - 1){
 					menuInfStr.append("</ul></li>");
 				}
 			}
 		}
 		return menuInfStr.toString();
 	}
 
 	/**
 	 * @param id
 	 * @return
 	 */
 	public List<MenuInf> getMenuItemByRoleId(Long id) {
 		List<MenuInf> menuList = menuDao.findMenuByRoleId(id);
 		return menuList;
 	}
 }
