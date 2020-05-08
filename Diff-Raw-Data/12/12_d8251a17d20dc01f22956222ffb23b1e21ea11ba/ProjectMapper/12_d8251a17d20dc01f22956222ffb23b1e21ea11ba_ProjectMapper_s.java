 package net.alpha01.jwtest.dao;
 
 import java.math.BigInteger;
 import java.util.List;
 
 import net.alpha01.jwtest.beans.Project;
 import net.alpha01.jwtest.beans.Role;
 
 import org.apache.ibatis.exceptions.PersistenceException;
 
 public interface ProjectMapper {
 	class ProjectUserRoles{
 		private BigInteger idProject;
 		private BigInteger idUser;
 		private BigInteger idRole;
 		
 		public ProjectUserRoles(BigInteger idProject, BigInteger idUser) {
 			super();
 			this.idProject = idProject;
 			this.idUser = idUser;
 		}
 		
 		public ProjectUserRoles(BigInteger idProject, BigInteger idUser, BigInteger idRole) {
 			super();
 			this.idProject = idProject;
 			this.idUser = idUser;
 			this.idRole = idRole;
 		}
 
 		public BigInteger getIdProject() {
 			return idProject;
 		}
 		public void setIdProject(BigInteger idProject) {
 			this.idProject = idProject;
 		}
 		public BigInteger getIdUser() {
 			return idUser;
 		}
 		public void setIdUser(BigInteger idUser) {
 			this.idUser = idUser;
 		}
 		public BigInteger getIdRole() {
 			return idRole;
 		}
 		public void setIdRole(BigInteger idRole) {
 			this.idRole = idRole;
 		}
 	}
 	
 	Project get(int id);
 	List<Project> getAll();
 	Integer add(Project prj)throws PersistenceException;
 	Integer update(Project prj)throws PersistenceException;
 	Integer delete(Project prj);
 	List<Role> getRoles(ProjectUserRoles prj);
	Integer deassociateRoles(Project currentProject);
 	Integer associateRole(ProjectUserRoles prj);
 }
