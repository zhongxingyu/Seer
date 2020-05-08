 package de.elateportal.editor;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import net.databinder.auth.data.hib.BasicPassword;
 import net.databinder.auth.hib.AuthDataApplication;
 import net.databinder.auth.hib.AuthDataSession;
 import net.databinder.hib.Databinder;
 import net.databinder.hib.SessionUnit;
 
 import org.apache.wicket.Request;
 import org.apache.wicket.Response;
 import org.apache.wicket.authorization.strategies.role.Roles;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.hibernate.criterion.Projections;
 
 import de.elateportal.editor.pages.OverviewPage;
 import de.elateportal.editor.pages.ShowSubtaskDefsPage;
 import de.elateportal.editor.pages.StatisticPage;
 import de.elateportal.editor.pages.TaskDefPage;
 import de.elateportal.editor.pages.UploadComplexTaskdefPage;
 import de.elateportal.editor.user.BasicUser;
 
 public class TaskEditorApplication extends AuthDataApplication {
   AtomicInteger activeUsers = new AtomicInteger(0);
 
   public int getActiveUsersCount() {
     return activeUsers.get();
   }
 
   /**
    * @return
    */
   public static TaskEditorApplication getInstance() {
     return (TaskEditorApplication) get();
   }
 
   @Override
   public org.apache.wicket.Session newSession(final Request request, final Response response) {
     return new AuthDataSession(request) {
       @Override
      public boolean signIn(final String username, final String password, final boolean setCookie) {
        final boolean loggedIn = super.signIn(username, password, setCookie);
        if (loggedIn) {
          activeUsers.incrementAndGet();
        }
        return loggedIn;
      }

      @Override
      protected boolean cookieSignIn() {
        final boolean loggedIn = super.cookieSignIn();
        if (loggedIn) {
          activeUsers.incrementAndGet();
        }
        return loggedIn;

      }
      @Override
       public void signOut() {
         activeUsers.decrementAndGet();
         super.signOut();
       }
     };
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see
    * net.databinder.hib.DataApplication#configureHibernate(org.hibernate.cfg
    * .AnnotationConfiguration)
    */
   @Override
   protected void configureHibernate(final AnnotationConfiguration config) {
     super.configureHibernate(config);
     // add all model classes
     config.addAnnotatedClass(de.elateportal.model.McSubTaskDef.class);
     config.addAnnotatedClass(de.elateportal.model.AddonSubTaskDef.class);
     config.addAnnotatedClass(de.elateportal.model.ClozeSubTaskDef.class);
     config.addAnnotatedClass(de.elateportal.model.ClozeSubTaskDef.Cloze.class);
     config.addAnnotatedClass(de.elateportal.model.ClozeSubTaskDef.Cloze.Gap.class);
     config.addAnnotatedClass(de.elateportal.model.ClozeSubTaskDef.Cloze.Gap.GapCorrectItem.class);
     config.addAnnotatedClass(de.elateportal.model.ClozeSubTaskDef.Cloze.ClozeTextOrGapItem.class);
     config.addAnnotatedClass(de.elateportal.model.ClozeSubTaskDef.GraphicalCloze.class);
     config.addAnnotatedClass(de.elateportal.model.ClozeSubTaskDef.GraphicalCloze.Gap.class);
     config.addAnnotatedClass(de.elateportal.model.ClozeSubTaskDef.GraphicalCloze.Gap.GapCorrectItem.class);
     config.addAnnotatedClass(de.elateportal.model.ComplexTaskDef.class);
     config.addAnnotatedClass(de.elateportal.model.Category.class);
     config.addAnnotatedClass(de.elateportal.model.Category.AddonTaskBlock.class);
     config.addAnnotatedClass(de.elateportal.model.Category.AddonTaskBlock.AddonTaskBlockAddonSubTaskDefOrChoiceItem.class);
     config.addAnnotatedClass(de.elateportal.model.Category.AddonTaskBlock.Choice.class);
     config.addAnnotatedClass(de.elateportal.model.Category.ClozeTaskBlock.class);
     config.addAnnotatedClass(de.elateportal.model.Category.ClozeTaskBlock.Choice.class);
     config.addAnnotatedClass(de.elateportal.model.Category.ClozeTaskBlock.ClozeConfig.class);
     config.addAnnotatedClass(de.elateportal.model.Category.ClozeTaskBlock.ClozeTaskBlockClozeSubTaskDefOrChoiceItem.class);
     config.addAnnotatedClass(de.elateportal.model.Category.MappingTaskBlock.class);
     config.addAnnotatedClass(de.elateportal.model.Category.MappingTaskBlock.Choice.class);
     config.addAnnotatedClass(de.elateportal.model.Category.MappingTaskBlock.MappingConfig.class);
     config.addAnnotatedClass(de.elateportal.model.Category.MappingTaskBlock.MappingTaskBlockMappingSubTaskDefOrChoiceItem.class);
     config.addAnnotatedClass(de.elateportal.model.Category.McTaskBlock.class);
     config.addAnnotatedClass(de.elateportal.model.Category.McTaskBlock.Choice.class);
     config.addAnnotatedClass(de.elateportal.model.Category.McTaskBlock.McConfig.class);
     config.addAnnotatedClass(de.elateportal.model.Category.McTaskBlock.McConfig.Different.class);
     config.addAnnotatedClass(de.elateportal.model.Category.McTaskBlock.McConfig.Regular.class);
     config.addAnnotatedClass(de.elateportal.model.Category.McTaskBlock.McTaskBlockMcSubTaskDefOrChoiceItem.class);
     config.addAnnotatedClass(de.elateportal.model.Category.CategoryMcTaskBlockOrClozeTaskBlockOrTextTaskBlockItem.class);
     config.addAnnotatedClass(de.elateportal.model.Category.PaintTaskBlock.class);
     config.addAnnotatedClass(de.elateportal.model.Category.PaintTaskBlock.Choice.class);
     config.addAnnotatedClass(de.elateportal.model.Category.PaintTaskBlock.PaintTaskBlockPaintSubTaskDefOrChoiceItem.class);
     config.addAnnotatedClass(de.elateportal.model.Category.TextTaskBlock.class);
     config.addAnnotatedClass(de.elateportal.model.Category.TextTaskBlock.Choice.class);
     config.addAnnotatedClass(de.elateportal.model.Category.TextTaskBlock.TextTaskBlockTextSubTaskDefOrChoiceItem.class);
     config.addAnnotatedClass(de.elateportal.model.ComplexTaskDef.Config.class);
     config.addAnnotatedClass(de.elateportal.model.ComplexTaskDef.Config.CorrectionMode.class);
     config.addAnnotatedClass(de.elateportal.model.ComplexTaskDef.Config.CorrectionMode.CorrectOnlyProcessedTasks.class);
     config.addAnnotatedClass(de.elateportal.model.ComplexTaskDef.Config.CorrectionMode.MultipleCorrectors.class);
     config.addAnnotatedClass(de.elateportal.model.ComplexTaskDef.Config.CorrectionMode.Regular.class);
     config.addAnnotatedClass(de.elateportal.model.ComplexTaskDef.Revisions.class);
     config.addAnnotatedClass(de.elateportal.model.ComplexTaskDef.Revisions.Revision.class);
     config.addAnnotatedClass(de.elateportal.model.Config.class);
     config.addAnnotatedClass(de.elateportal.model.MappingSubTaskDef.class);
     config.addAnnotatedClass(de.elateportal.model.MappingSubTaskDef.Assignment.class);
     config.addAnnotatedClass(de.elateportal.model.MappingSubTaskDef.Concept.class);
     config.addAnnotatedClass(de.elateportal.model.MappingSubTaskDef.Concept.ConceptCorrectAssignmentIDItem.class);
     config.addAnnotatedClass(de.elateportal.model.McSubTaskDef.class);
     config.addAnnotatedClass(de.elateportal.model.McSubTaskDef.Correct.class);
     config.addAnnotatedClass(de.elateportal.model.McSubTaskDef.Incorrect.class);
     config.addAnnotatedClass(de.elateportal.model.PaintSubTaskDef.class);
     config.addAnnotatedClass(de.elateportal.model.PaintSubTaskDef.Images.class);
     config.addAnnotatedClass(de.elateportal.model.PaintSubTaskDef.TextualAnswer.class);
     config.addAnnotatedClass(de.elateportal.model.SubTaskDefType.class);
     config.addAnnotatedClass(de.elateportal.model.TaskBlockType.class);
     config.addAnnotatedClass(de.elateportal.model.TextSubTaskDef.class);
 
     // config.setProperty("hibernate.show_sql", "true");
   }
 
   @Override
   public Class getHomePage() {
     return OverviewPage.class;
   }
 
   public byte[] getSalt() {
     return "secrect hash salt".getBytes();
   }
 
   public Class getUserClass() {
     return BasicUser.class;
   }
 
   @Override
   protected void init() {
     super.init();
     mountBookmarkablePage("taskdefs", TaskDefPage.class);
     mountBookmarkablePage("statistics", StatisticPage.class);
     mountBookmarkablePage("subtaskdefs", ShowSubtaskDefsPage.class);
     mountBookmarkablePage("import", UploadComplexTaskdefPage.class);
 
     getMarkupSettings().setStripWicketTags(true);
     // open session for loading recipe titles
     Databinder.ensureSession(new SessionUnit() {
 
       public Object run(final Session sess) {
         final Transaction transaction = sess.beginTransaction();
         final Integer count = (Integer) sess.createCriteria(BasicUser.class).setProjection(Projections.rowCount()).uniqueResult();
         if (count == 0) {
           final BasicUser adminUser = new BasicUser();
           adminUser.setUsername("admin");
           adminUser.setPassword(new BasicPassword("admin"));
           adminUser.addRole(Roles.ADMIN);
           sess.save(adminUser);
           transaction.commit();
         }
         return null;
       }
     });
   }
 
 }
