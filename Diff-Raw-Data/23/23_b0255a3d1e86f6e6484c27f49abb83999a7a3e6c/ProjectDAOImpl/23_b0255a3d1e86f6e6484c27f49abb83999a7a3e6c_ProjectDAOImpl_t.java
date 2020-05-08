 /**
  *
  * @author sandy
  */
 package sands.dao.implementors;
 
 import helper.database.Crud;
 import sands.dao.interfaces.ProjectDAO;
 import model.Project;
 import java.util.ArrayList;
 import java.util.List;
 import model.ProjectDocument;
 import model.ProjectLegal;
 import model.ProjectResource;
 import model.ProjectSchedule;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Expression;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 
 public class ProjectDAOImpl extends Crud implements ProjectDAO {
 
     public void save(Project project) {
 
         ProjectDocument projectdocument = new ProjectDocument();
         ProjectLegal projectfinancial = new ProjectLegal();
         ProjectResource projectresource = new ProjectResource();
         ProjectSchedule projectschedule = new ProjectSchedule();
         project.setProjectdocument(projectdocument);
        
         project.setCreatedBy(this.getPrincipal().getUsername());
         projectfinancial.setProject(project);
         projectdocument.setProject(project);
         projectresource.setProject(project);
         projectschedule.setProject(project);
         super.save(project);
     }
 
     public void update(Project project) {
         project.setUpdateBy(this.getPrincipal().getUsername());
         super.update(project);
     }
 
     public void delete(Project project) {
         Session session = Crud.getHibernatetemplate().getSessionFactory().openSession();
         Project p = (Project) session.load(Project.class, project.getId());
         session.createQuery("UPDATE Project SET active=:active WHERE id=:project_id").
                 setLong("project_id", project.getId()).
                 setBoolean("active", false).
                 executeUpdate();
         session.createQuery("UPDATE ProjectDocument SET active=:active WHERE project_management_id=:project_document_id").
                 setLong("project_document_id", p.getProjectdocument().getPROJECT_MANAGEMENT_ID()).
                 setBoolean("active", false).
                 executeUpdate();
     }
 
     public ArrayList<Project> list(int offset) {
 
         Session session = Crud.getHibernatetemplate().getSessionFactory().openSession();
         Criteria criteria = session.createCriteria(Project.class).add(Expression.eq("active", true));
 
 
         if (this.orderByType.equals("ASC")) {
             criteria.addOrder(Order.asc(this.orderByField));
         } else {
             criteria.addOrder(Order.desc(this.orderByField));
         }
 
 
         criteria.setMaxResults(this.maxResult);
         criteria.setFirstResult(offset);
 
         List results = criteria.list();
 
         //count rows
         Criteria c = session.createCriteria(Project.class).setFirstResult(0).add(Expression.eq("active", true)).setProjection(Projections.rowCount());
         List countRow = c.list();
 
         float maxPage = countRow.get(0).hashCode() / Integer.valueOf(this.maxResult).floatValue();
         Double maxPageResults = Math.ceil(maxPage);
 
         ArrayList array = new ArrayList();
         array.add(0, results);
         array.add(1, countRow);
         array.add(2, maxPageResults.intValue());
 
         this.getHibernatetemplate().getSessionFactory().close();
         return array;
 
     }
 
     public Project getById(long id) {
         Session session = this.getHibernatetemplate().getSessionFactory().openSession();
         Project project = new Project();
         project.setId(id);
         Project p = (Project) session.load(Project.class, project.getId());
         return p;
 
     }
 }
