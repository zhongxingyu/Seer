 @FilterDefs({
         @FilterDef(name="activeProject", parameters= { @ParamDef( name="project_id", type="long") }, defaultCondition="project_id = :project_id"),
         @FilterDef(name="adminProjects", parameters= { @ParamDef( name="projectIds", type="long") }, defaultCondition="project_id in (:projectIds)"),
        @FilterDef(name="projectId", parameters= { @ParamDef( name="projectIds", type="long") }, defaultCondition="id in (:projectIds)")
         // TODO FIXME also filter projects in activeProject mode
 })
 
 package models.tm;
 
 import org.hibernate.annotations.FilterDef;
 import org.hibernate.annotations.FilterDefs;
 import org.hibernate.annotations.ParamDef;
