 package no.niths.application.rest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import no.niths.application.rest.exception.NotInCollectionException;
 import no.niths.application.rest.interfaces.RoleController;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.application.rest.lists.RoleList;
 import no.niths.common.AppConstants;
 import no.niths.common.SecurityConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.Student;
 import no.niths.domain.security.Role;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.interfaces.RoleService;
 import no.niths.services.interfaces.StudentService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseStatus;
 /**
  * Controller for handling roles
  *
  */
 @Controller
 @RequestMapping(AppConstants.ROLES)
 public class RoleControllerImpl extends AbstractRESTControllerImpl<Role> implements RoleController{
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(RoleControllerImpl.class);
 
 	@Autowired
 	private RoleService roleService;
 	
 	@Autowired
 	private StudentService studentService;
 	
 	private RoleList roleList = new RoleList();
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@PreAuthorize(SecurityConstants.ONLY_ADMIN)
 	public void create(@RequestBody Role domain) {
 		super.create(domain);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@PreAuthorize(SecurityConstants.ONLY_ADMIN)
 	public void update(@RequestBody Role domain) {
 		super.update(domain);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@PreAuthorize(SecurityConstants.ONLY_ADMIN)
 	public void hibernateDelete(@PathVariable long id) {
 		super.hibernateDelete(id);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Role getById(@PathVariable Long id) {
 		return super.getById(id);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ArrayList<Role> getAll(Role domain) {
 		roleList = (RoleList) super.getAll(domain);
 		clearRelations();
 		return roleList;
 	}
 	
 	@Override
 	public ArrayList<Role> getAll(Role domain, @PathVariable int firstResult, @PathVariable int maxResults) {
 		roleList = (RoleList) super.getAll(domain, firstResult, maxResults);
 		clearRelations();
 		return roleList;
 	}
 	
 	private void clearRelations(){
 		for (Role r: roleList) {
 			r.setStudents(null);
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@PreAuthorize(SecurityConstants.ONLY_ADMIN)
 	@RequestMapping(value = { 
 			"add/role/{studentId}/{roleId}" }, method = RequestMethod.PUT)
 	@ResponseStatus(value = HttpStatus.CREATED, reason = "Role added")
 	public void addStudentRole(@PathVariable Long studentId, @PathVariable Long roleId) {
 		Student stud = studentService.getStudentWithRoles(studentId);
 		ValidationHelper.isObjectNull(stud);
 		
 		Role role = roleService.getById(roleId);
 		ValidationHelper.isObjectNull(role);
 		
 		stud.getRoles().add(role);
 		studentService.update(stud);
 		
 		logger.debug("Added role to student: " + role.getRoleName());
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@PreAuthorize(SecurityConstants.ONLY_ADMIN)
 	@RequestMapping(value = { "remove/role/{studentId}/{roleId}" }, method = RequestMethod.PUT)
 	@ResponseStatus(value = HttpStatus.CREATED, reason = "Role removed")
 	public void removeStudentRole(@PathVariable Long studId, @PathVariable Long roleId) {
 		Student stud = studentService.getStudentWithRoles(studId);
 		ValidationHelper.isObjectNull(stud);
 		
 		Role role = roleService.getById(roleId);
 		ValidationHelper.isObjectNull(role);
 		
 		stud.getRoles().remove(role);
 		studentService.update(stud);
 		
 		logger.debug("Removed role from student: " + role.getRoleName());
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@PreAuthorize(SecurityConstants.ONLY_ADMIN)
 	@RequestMapping(value = { "remove/roles/{studentId}" }, method = RequestMethod.POST)
 	@ResponseStatus(value = HttpStatus.OK, reason = "Roles removed from student")
 	public void removeAllRolesFromStudent(@PathVariable Long studId) {
 		Student stud = studentService.getStudentWithRoles(studId);
 		ValidationHelper.isObjectNull(stud);
 
 		stud.getRoles().clear();
 		studentService.update(stud);
 
 		logger.debug("All roles removed from student");
 	}
 	
 	/**
 	 * Return 200 ok is student is in role, else 204, no content
 	 * @param studId
 	 * @param roleId
 	 */
 	@Override
 	@RequestMapping(value = { "isStudent/{studId}/{roleName}" }, method = RequestMethod.GET)
 	@ResponseStatus(value = HttpStatus.OK, reason = "Student has role")
 	public void isStudentInRole(@PathVariable Long studId, @PathVariable String roleName){
		Student stud = studentService.getStudentWithRoles(studId);
		ValidationHelper.isObjectNull(stud, "Student does not exist");
 		List<Role> roles = roleService.getAll(new Role(roleName));
 		
 		boolean hasRole = false;
 		if(!roles.isEmpty()){
 			Role role = roles.get(0);
 			for (int i = 0; i < stud.getRoles().size() && !hasRole; i++){
				if(stud.getRoles() == role){
 					hasRole = true;
 				}
 			}
 		}
 		if(!hasRole){
 			throw new NotInCollectionException("Student does not have the role");
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public GenericService<Role> getService() {
 		return roleService;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ListAdapter<Role> getList() {
 		return roleList;
 	}
 }
