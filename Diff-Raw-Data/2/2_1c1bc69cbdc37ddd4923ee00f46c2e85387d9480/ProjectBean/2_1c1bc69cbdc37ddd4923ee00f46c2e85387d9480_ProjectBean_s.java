 package org.encuestame.web.beans.project;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.encuestame.core.exception.EnMeExpcetion;
 import org.encuestame.web.beans.MasterBean;
 import org.hibernate.HibernateException;
 
 /**
  * encuestame: system online surveys Copyright (C) 2009 encuestame Development
  * Team
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of version 3 of the GNU General Public License as published by the
  * Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * Id: ProjectBean.java Date: 26/05/2009 10:25:05
  *
  * @author juanpicado package: org.encuestame.web.beans.project
  * @version 1.0
  */
 public class ProjectBean extends MasterBean {
 
     public Boolean noProyects = true;
     public Boolean create = true;
     public Boolean edit;
     public Boolean editDetail;
     private Log log = LogFactory.getLog(this.getClass());
     private UnitProjectBean beanUProyect;
     private Integer projectSelected;
     private Collection<UnitProjectBean> list_unitBeans;
 
     public ProjectBean() {
         log.info("create proyect bean");
     }
 
     /**
      *
      * @return
      * @throws Exception
      */
     public Collection<UnitProjectBean> loadListProjects() throws Exception {
         list_unitBeans = new LinkedList<UnitProjectBean>();
         return list_unitBeans = getServicemanager().getDataEnMeSource()
                 .loadListProjects();
     }
 
     /**
      * save data new proyect
      */
     public void saveProyect() {
         try {
             log.info("save proyect");
             log.info("name->" + getBeanUProyect().getName());
 
             if (getBeanUProyect() != null) {
                 getServicemanager().getDataEnMeSource().createProject(
                         getBeanUProyect());
                 log.info("projecto creado");
                 addInfoMessage("Proyecto Creado", "");
                 cleanProyect();
             } else {
                 log.error("error create project");
                 addErrorMessage(
                         "No se pudo recuperar los datos del formulario", "");
             }
         } catch (HibernateException e) {
             log.error("error create project" + e);
             addErrorMessage("1Error Creando Proyecto", "");
         } catch (EnMeExpcetion e) {
             log.error("error create project " + e);
             addErrorMessage("2Error Creando Proyecto", "");
         } catch (Exception e) {
             log.error("error create project " + e);
             addErrorMessage("3Error Creando Proyecto", "");
         }
     }
 
     /**
      *
      */
     public void editProject() {
         log.info("edit project selected->" + getProjectSelected());
         if (getProjectSelected() != null) {
             setNoProyects(false);
             setEditDetail(true);
             loadProjectInfo(getProjectSelected());
         } else {
             addWarningMessage("Error getProjectSelected", "");
         }
     }
 
     /**
      * loadProjectInfo
      *
      * @param id
      */
     private void loadProjectInfo(Integer id) {
         try {
             log.info("loadProjectInfo");
             cleanProyect();
            getBeanUProyect().setId(getProjectSelected());
             setBeanUProyect(getServicemanager().getDataEnMeSource()
                     .loadProjectInfo(getBeanUProyect()));
             fullFormEditProject(getBeanUProyect());
             log.info("projecto Cargado");
         } catch (Exception e) {
             addErrorMessage("Error Cargando Datos Proyecto->" + e.getMessage(),
                     "");
         }
     }
 
     /**
      * full form edit project
      * @param project
      */
     private void fullFormEditProject(UnitProjectBean project) {
         log.info("fullFormEditProject");
         try {
             log.info("INFO EDIT PRO->"+project.getDescription());
             log.info("INFO EDIT PRO->"+project);
             setBeanUProyect(project);
             log.info("BEAN proyect->"+getBeanUProyect());
         } catch (Exception e) {
             addErrorMessage("Imposible Llena Formulario->" + e.getMessage(), "");
         }
     }
 
     /**
      *
      */
     public void deselectedProject(){
         log.info("deselectedProject");
         cleanProyect();
         setEditDetail(false);
         setNoProyects(true);
     }
 
     /**
      * clear form project
      */
     private void cleanProyect() {
         getBeanUProyect().setDateFinish(null);
         getBeanUProyect().setDateInit(null);
         getBeanUProyect().setDescription(null);
         getBeanUProyect().setName(null);
         getBeanUProyect().setState(null);
     }
 
     /**
      * change to create form
      */
     public void changeCreate() {
         try {
             log.info("changeCreate");
             deselectedProject();
             setCreate(true);
             setEdit(false);
             log.info("Create " + getCreate());
             log.info("Edit " + getEdit());
         } catch (Exception e) {
             addErrorMessage("No se puede cambiar->" + e, e.getMessage());
         }
     }
 
     /**
      * change to edit form
      */
     public void changeEdit() {
         log.info("changeEdit");
         deselectedProject();
         setCreate(false);
         setEdit(true);
         log.info("Create " + getCreate());
         log.info("Edit " + getEdit());
     }
 
     /**
      * @return the noProyects
      */
     public Boolean getNoProyects() {
         return noProyects;
     }
 
     /**
      * @param noProyects
      *            the noProyects to set
      */
     public void setNoProyects(Boolean noProyects) {
         this.noProyects = noProyects;
     }
 
     /**
      * @return the create
      */
     public Boolean getCreate() {
         return create;
     }
 
     /**
      * @param create
      *            the create to set
      */
     public void setCreate(Boolean create) {
         this.create = create;
     }
 
     /**
      * @return the edit
      */
     public Boolean getEdit() {
         return edit;
     }
 
     /**
      * @param edit
      *            the edit to set
      */
     public void setEdit(Boolean edit) {
         this.edit = edit;
     }
 
     /**
      * @return the beanUProyect
      */
     public UnitProjectBean getBeanUProyect() {
         log.info("DDDDDDDget UnitProjectBean->"+beanUProyect.getName());
         log.info("get UnitProjectBean->"+beanUProyect);
         return beanUProyect;
     }
 
     /**
      * @param beanUProyect
      *            the beanUProyect to set
      */
     public void setBeanUProyect(UnitProjectBean beanUProyect) {
         log.info("set UnitProjectBean->"+beanUProyect);
         this.beanUProyect = beanUProyect;
     }
 
     /**
      * @return the list_unitBeans
      */
     public Collection<UnitProjectBean> getList_unitBeans() {
         try {
             loadListProjects();
             if (list_unitBeans.size() > 0)
                 setOneRow(true);
             else
                 setOneRow(false);
             return list_unitBeans;
         } catch (Exception e) {
             addErrorMessage("Error Cargando Datos->" + e.getMessage(), e
                     .getMessage());
             return null;
         }
     }
 
     /**
      * @param list_unitBeans
      *            the list_unitBeans to set
      */
     public void setList_unitBeans(Collection<UnitProjectBean> list_unitBeans) {
         this.list_unitBeans = list_unitBeans;
     }
 
     /**
      * @return the projectSelected
      */
     public Integer getProjectSelected() {
         log.info("projectSelected->" + projectSelected);
         return projectSelected;
     }
 
     /**
      * @param projectSelected
      *            the projectSelected to set
      */
     public void setProjectSelected(Integer projectSelected) {
         this.projectSelected = projectSelected;
     }
 
     /**
      * @return the editDetail
      */
     public Boolean getEditDetail() {
         return editDetail;
     }
 
     /**
      * @param editDetail
      *            the editDetail to set
      */
     public void setEditDetail(Boolean editDetail) {
         this.editDetail = editDetail;
     }
 
 }
