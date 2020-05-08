 package org.antstudio.rbac.action;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.antstudio.rbac.domain.Menu;
 import org.antstudio.rbac.domain.User;
 import org.antstudio.rbac.domain.annotation.MenuMapping;
 import org.antstudio.rbac.domain.annotation.PermissionMapping;
 import org.antstudio.rbac.service.MenuService;
 import org.antstudio.rbac.service.UserService;
 import org.antstudio.support.spring.annotation.FormParam;
 import org.antstudio.utils.ClassPropertiesUtil;
 import org.antstudio.utils.MessageUtils;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.reeham.component.ddd.model.ModelContainer;
 
 /**
  * the action controller for the menu
  * @author Gavin
  * @version 1.0
  * @date 2012-12-4
  */
 @Controller
 @RequestMapping("/menu")
 public class MenuAction {
 
 	@Resource
 	private MenuService menuService;
 	
 	@Resource
 	private UserService useruService;
 	
 	@Resource
 	private ModelContainer modelContainer;
 	
 	/**
 	 * 获取子菜单(默认获取deleteFlag=false的菜单)
 	 * @param pid 父菜单id
 	 * @param request
 	 * @param response
 	 * @return
 	 */
 	@RequestMapping("/getSubMenus")
 	@ResponseBody
 	public List<Map<String,Object>> getSubMenus(@RequestParam("pid") Long pid,HttpServletRequest request,HttpServletResponse response){
 		User currentUser = useruService.getCurrentUser(request);
 		if(pid==-1L)
 			pid = null;
 		return menuService.getSubMenusByRoleForMap(pid,currentUser.getRoleId());
 	}
 	
    /**
     * 根据角色获取菜单数据,返回一个map的 list,当该菜单分配给了这个角色，那么map.checked为true,否则为false
     * @param pid 父菜单id
     * @param rid 角色id
     * @param request
     * @param response
     * @return
     */
 	@RequestMapping("/getAssignMenuData")
 	@ResponseBody
 	public List<Map<String,Object>> getAssignMenuData(@RequestParam("pid") Long pid,@RequestParam("rid")Long rid,HttpServletRequest request,HttpServletResponse response){
 		if(pid==-1L)
 			pid = null;
 		return menuService.getAssignMenuData(pid,rid);
 	}
 	
    /**
     * 分配菜单给角色,将分配状态改变了的菜单id(即参数ids)传回,chechStatus记录每个菜单是否勾选(是否要分配).
     * 这里传回的id全部是需要修改的,这里是由前台ztree控件保证的.
     * @param ids
     * @param rid
     * @param checkStatus
     * @param request
     * @param response
     * @return
     */
 	@RequestMapping("/assignMenu")
 	@PermissionMapping(code="000002",name="分配菜单")
 	@ResponseBody
 	public Map<String,Object> assignMenu(@RequestParam("ids") Long[] ids,@RequestParam("rid")Long rid,@RequestParam("checkStatus")Boolean[] checkStatus,HttpServletRequest request,HttpServletResponse response){
 		menuService.assignMenuToRole(ids,checkStatus,rid);
 		return MessageUtils.getMapMessage(true);
 	}
 	
 	/**
 	 * 跳转菜单管理界面
 	 * @return
 	 */
 	@MenuMapping(url="/menu",name="菜单管理",code="100003",parentCode="100000")
 	@RequestMapping("")
 	public ModelAndView showMenuPage(){
 		return new ModelAndView("/pages/rbac/menu");
 	}
 	
 	@RequestMapping("/addMenu")
 	@ResponseBody
 	@PermissionMapping(code="000001",name="添加菜单信息")
 	public Map<String,Object> addMenu(@FormParam("menu") Menu menu){
		Long menuId = -1L;
		if(menuId.equals(menu.getParentId())){
 			menu.setParentId(null);
 		}
 		modelContainer.enhanceModel(menu).saveOrUpdate();
 		return MessageUtils.getMapMessage(true);
 	}
 	
 	@RequestMapping("/updateMenu")
 	@ResponseBody
 	public Map<String,Object> updateMenu(@FormParam("menu") Menu menu){
 		if(menu.getId()!=null){
 			Menu oldMenu = menuService.getModel(menu.getId());
 			menu = (Menu) ClassPropertiesUtil.copyProperties(menu, oldMenu, true, "menuName","url");
 			menuService.update(menu);
 		}
 		return MessageUtils.getMapMessage(true);
 	}
 	
 	@RequestMapping("/deleteMenu")
 	@ResponseBody
 	public Map<String,Object> deleteMenu(@FormParam("menu") Menu menu){
 		
 		
 		if(menu.getId()!=null){
 			 List<Menu> menus = new ArrayList<Menu>();
 			 menus.add(menu);
 			menuService.deleteMenus(menus);
 		}
 		return MessageUtils.getMapMessage(true);
 	}
 }
