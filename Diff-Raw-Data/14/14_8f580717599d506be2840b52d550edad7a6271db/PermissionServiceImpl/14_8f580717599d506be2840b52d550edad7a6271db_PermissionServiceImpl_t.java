 package org.antstudio.rbac.service.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import org.antstudio.pagination.Pager;
 import org.antstudio.rbac.domain.Permission;
 import org.antstudio.rbac.repository.PermissionRepository;
 import org.antstudio.rbac.service.PermissionService;
 import org.springframework.stereotype.Service;
 
import com.reeham.component.ddd.annotation.OnEvent;
 import com.reeham.component.ddd.model.ModelContainer;
 import com.reeham.component.ddd.model.ModelLoader;
 import com.reeham.component.ddd.model.ModelUtils;
 
 /**
  * 权限服务实现类
  * @author Gavin
  * @version 1.0
  * @date 2012-12-14
  */
 @Service
 public class PermissionServiceImpl implements PermissionService,ModelLoader{
 	
 	@Resource
 	private PermissionRepository permissionRepository;
 	@Resource
 	private ModelContainer modelContainer;
	
 	@Override
 	public void batchSave(List<Permission> permissions) {
 		if(permissions.size()!=0)
 		permissionRepository.batchSave(permissions);
 	}
 
 	@Override
 	public void delete(List<Permission> permissions) {
 		permissionRepository.delete(permissions);
 		
 	}
 
 	@Override
 	public Object loadModel(Object identifier) {
		System.out.println(identifier);
 		return load((Long) identifier);
 	}
 
 	@Override
 	public Map<String, Permission> getPermissionsByCode(Map<String,Object> paramsMap) {
 		Map<String,Permission> m = new HashMap<String,Permission>();
		for(Permission permission:getPermissions(paramsMap)){
			m.put(permission.getCode(),permission);
		}
 		return m;
 	}
 
 	@Override
	@OnEvent("permission/get")
 	public Permission get(Long id) {
 		return (Permission) modelContainer.getModel(ModelUtils.asModelKey(Permission.class, id), this);
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public List<Permission> getPermissions(Map<String,Object> paramsMap) {
 		return modelContainer.identifiersToModels((List)permissionRepository.getPermissions(paramsMap), Permission.class, this);
 	}
 
 	@Override
 	public Pager getPermissionsForPage(Map<String, Object> paramsMap) {
 		
 	 return new Pager(permissionRepository.getPermission_count(paramsMap),getPermissionsForMap(paramsMap),paramsMap);
 	}
 
 	@Override
 	public List<Map<String, Object>> getPermissionsForMap(Map<String,Object> paramsMap) {
 		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
 		for(Permission p:getPermissions(paramsMap)){
 			list.add(p.toMap());
 		}
 		return list;
 	}
 
 	@Override
 	public void assignPermission(Long[] pids,Boolean[] status, Long rid) {
 	List<Long> addPids = new ArrayList<Long>();
 	List<Long> deletePids = new ArrayList<Long>();
 	for(int i=0,j=status.length;i<j;i++){
 		if(status[i])
 			addPids.add(pids[i]);
 		else
 			deletePids.add(pids[i]);
 	}
 	if(addPids.size()>0)
     permissionRepository.assignAddPermission(addPids, rid);	
 	if(deletePids.size()>0)
 		 permissionRepository.assignDeletePermission(deletePids, rid);
 	}
 
 	@Override
 	public Pager getPermissionsByRoleForPage(Map<String,Object> paramsMap) {
 		
 		return new Pager(permissionRepository.getPermissionsByRole_count(paramsMap),getPermissionsByRoleForMap(paramsMap),paramsMap);
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public List<Map<String,Object>> getPermissionsByRoleForMap(Map<String, Object> paramsMap) {
 		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
 		List<Permission> permissions  = new ArrayList<Permission>();
 		if(permissionRepository.getPermissionsByRole(paramsMap).size()!=0)
 	    permissions = modelContainer.identifiersToModels((List)permissionRepository.getPermissionsByRole(paramsMap), Permission.class, this);
 	    for(Permission p:getPermissions(paramsMap))
 	    {
 	    	if(permissions.contains(p))
 	    	list.add(addProperty(p.toMap(),"checked",true));
 	    	else
 	    		list.add(addProperty(p.toMap(),"checked",false));
 	    }
 		return list;
 	}
 
 	
 	private Map<String,Object> addProperty(Map<String,Object> m,String key,Object value){
 		m.put(key, value);
 		return  m;
 		
 	}
 
 	@Override
 	public Permission load(Long id) {
 		return permissionRepository.get(id);
 	}
 	
 }
