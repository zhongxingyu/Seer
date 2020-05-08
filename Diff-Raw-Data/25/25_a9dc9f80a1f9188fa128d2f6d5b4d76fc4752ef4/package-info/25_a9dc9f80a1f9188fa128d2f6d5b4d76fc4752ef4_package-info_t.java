 @FilterDefs({
         @FilterDef(name="activeProject", parameters= { @ParamDef( name="project_id", type="long") }, defaultCondition="project_id = :project_id"),
         @FilterDef(name="adminProjects", parameters= { @ParamDef( name="projectIds", type="long") }, defaultCondition="project_id in (:projectIds)"),
        @FilterDef(name="projectId", parameters= { @ParamDef( name="projectIds", type="long") }, defaultCondition="id in (:projectIds)"),
        @FilterDef(name="activeProjectUsers", parameters= { @ParamDef( name="projectId", type="long") }, defaultCondition="id in (SELECT u_in_filter.id from TMUser u_in_filter, ProjectRole r_in_filter WHERE r_in_filter.project_id = (:projectId) AND r_in_filter.id in (SELECT ur_in_filter.projectRoles_id FROM TMUser_ProjectRole ur_in_filter WHERE ur_in_filter.TMUser_id = u_in_filter.id) )")
         // TODO FIXME also filter projects in activeProject mode
 })
 
 package models.tm;
 
 import org.hibernate.annotations.FilterDef;
 import org.hibernate.annotations.FilterDefs;
 import org.hibernate.annotations.ParamDef;
