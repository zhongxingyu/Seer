 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Web Questionnaires 2
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Anton Dmitrijev
  */
 package eionet.webq.dao;
 
 import eionet.webq.dao.orm.ProjectEntry;
 import eionet.webq.dao.orm.ProjectFile;
 import eionet.webq.dao.orm.ProjectFileType;
 import eionet.webq.dao.orm.util.WebQFileInfo;
 import eionet.webq.dto.WebFormType;
 import org.apache.commons.lang3.ArrayUtils;
 import org.hibernate.Session;
 import org.hibernate.criterion.Conjunction;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.sql.Timestamp;
 import java.util.Collection;
 
 import static org.hibernate.criterion.Restrictions.and;
 import static org.hibernate.criterion.Restrictions.eq;
 import static org.hibernate.criterion.Restrictions.in;
 import static org.hibernate.criterion.Restrictions.isNotNull;
 
 /**
  * ProjectFileStorage implementation.
  */
 @Repository
 @Transactional
 public class ProjectFileStorageImpl extends AbstractDao<ProjectFile> implements WebFormStorage, ProjectFileStorage {
 
     @Override
     public int save(final ProjectFile projectFile, final ProjectEntry project) {
         projectFile.setProjectId(project.getId());
         getCurrentSession().save(projectFile);
         return projectFile.getId();
     }
 
     @Override
     public void update(final ProjectFile projectFile, ProjectEntry projectEntry) {
         if (projectFile.getProjectId() == projectEntry.getId()) {
             if (WebQFileInfo.fileIsEmpty(projectFile.getFile())) {
                 updateWithoutChangingContent(projectFile);
             } else {
                 fullUpdate(projectFile);
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public Collection<ProjectFile> findAllFilesFor(ProjectEntry project) {
         return getCriteria().add(eq("projectId", project.getId())).addOrder(Order.asc("id")).list();
     }
 
     @Override
     public void remove(final ProjectEntry projectEntry, final int... fileIds) {
         removeByCriterion(and(eq("projectId", projectEntry.getId()), in("id", ArrayUtils.toObject(fileIds))));
     }
 
     @Override
     public ProjectFile findByNameAndProject(String name, ProjectEntry projectEntry) {
         return (ProjectFile) getCriteria()
                 .add(Restrictions.and(eq("projectId", projectEntry.getId()), eq("file.name", name))).uniqueResult();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public Collection<ProjectFile> getAllActiveWebForms(WebFormType type) {
         return getCriteria().add(activeWebFormCriterionForType(type)).list();
     }
 
     @Override
     public ProjectFile getActiveWebFormById(WebFormType type, int id) {
         return (ProjectFile) getCriteria().add(and(activeWebFormCriterionForType(type), Restrictions.idEq(id))).uniqueResult();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public Collection<ProjectFile> findWebFormsForSchemas(WebFormType type, Collection<String> xmlSchemas) {
         return getCriteria().add(and(activeWebFormCriterionForType(type), in("xmlSchema", xmlSchemas))).list();
     }
 
     @Override
     Class<ProjectFile> getEntityClass() {
         return ProjectFile.class;
     }
 
     /**
      * Updates all fields.
      *
      * @param projectFile project file
      */
     private void fullUpdate(ProjectFile projectFile) {
         Session currentSession = getCurrentSession();
         projectFile.setUpdated(new Timestamp(System.currentTimeMillis()));
         currentSession.merge(projectFile);
         currentSession.flush();
     }
 
     /**
      * Performs update without changing file content data.
      *
      * @param projectFile project file
      */
     private void updateWithoutChangingContent(ProjectFile projectFile) {
         getCurrentSession().createQuery("UPDATE ProjectFile SET title=:title, xmlSchema=:xmlSchema, "
                 + " description=:description, userName=:userName, "
                 + " active=:active, localForm=:localForm, remoteFileUrl=:remoteFileUrl, "
                 + " newXmlFileName=:newXmlFileName, emptyInstanceUrl=:emptyInstanceUrl, updated=CURRENT_TIMESTAMP() "
                 + " WHERE id=:id").setProperties(projectFile).executeUpdate();
     }
 
     /**
      * Criterion defining active web form.
      *
      * @param type web form type
      * @return criterion
      */
     private Conjunction activeWebFormCriterionForType(WebFormType type) {
        Conjunction criterion = and(eq("fileType", ProjectFileType.WEBFORM), eq("active", true), isNotNull("xmlSchema"));
         if (type == WebFormType.LOCAL) {
             criterion.add(eq("localForm", true));
         }
         if (type == WebFormType.REMOTE) {
             criterion.add(eq("remoteForm", true));
         }
         return criterion;
     }
 }
