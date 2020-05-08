 /*
 
 Copyright (C) 2009 Steffen Dienst
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package de.elateportal.editor.pages;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 
 import net.databinder.auth.hib.AuthDataSession;
 import net.databinder.hib.Databinder;
 import net.databinder.models.hib.HibernateListModel;
 import net.databinder.models.hib.HibernateObjectModel;
 import net.databinder.models.hib.QueryBuilder;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.link.DownloadLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.EmptyPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import de.elateportal.editor.TaskEditorApplication;
 import de.elateportal.editor.components.panels.tasks.CategoryPanel;
 import de.elateportal.editor.components.panels.tasks.SubtaskDefInputPanel;
 import de.elateportal.editor.components.panels.tree.ComplexTaskDefTree;
 import de.elateportal.editor.components.panels.tree.ComplexTaskdefTreeProvider;
 import de.elateportal.editor.preview.PreviewLink;
 import de.elateportal.editor.preview.PreviewPanel;
 import de.elateportal.editor.user.BasicUser;
 import de.elateportal.editor.util.RemoveNullResultTransformer;
 import de.elateportal.model.Category;
 import de.elateportal.model.ComplexTaskDef;
 import de.elateportal.model.ObjectFactory;
 import de.elateportal.model.SubTaskDefType;
 import de.elateportal.model.ComplexTaskDef.Revisions.Revision;
 
 /**
  * @author Steffen Dienst
  * 
  */
 public class TaskDefPage extends SecurePage {
 
   private Panel editPanel;
   private ComplexTaskDefTree tree;
   private final ComplexTaskdefTreeProvider treeProvider;
 
   public TaskDefPage() {
     super();
     IModel<List<?>> tasklistmodel = new HibernateListModel(new QueryBuilder() {
       public Query build(final Session sess) {
         final Query q = sess.createQuery(String.format("select tasks from BasicUser u left join u.taskdefs tasks where u.username='%s'",
             AuthDataSession.get().getUser().getUsername()));
         q.setResultTransformer(RemoveNullResultTransformer.INSTANCE);
         return q;
       }
     });
     // the admin sees all taskdefs
     if (TaskEditorApplication.isAdmin()) {
       tasklistmodel = new HibernateListModel(BasicUser.class);
     }
     treeProvider = new ComplexTaskdefTreeProvider(tasklistmodel);
     add(tree = new ComplexTaskDefTree("tree", treeProvider) {
       @Override
       protected void onSelect(final IModel<?> selectedModel, final AjaxRequestTarget target) {
         renderPanelFor(selectedModel, target);
       }
     });
     editPanel = new EmptyPanel("editpanel");
     add(editPanel.setOutputMarkupId(true));
   }
 
   /**
    * Replace right hand form panel with an edit panel for the given model object.
    * 
    * @param t
    * @param target
    */
   public void renderPanelFor(final IModel<?> selectedModel, final AjaxRequestTarget target) {
     final Object t = selectedModel.getObject();
     if (t instanceof ComplexTaskDef) {
       replaceEditPanelWith(target, new PreviewPanel("editpanel", (IModel<ComplexTaskDef>) selectedModel));
     } else if (t instanceof Category) {
       replaceEditPanelWith(target, new CategoryPanel("editpanel", (HibernateObjectModel<Category>) selectedModel));
     } else if (t instanceof SubTaskDefType) {
       final SubTaskDefType st = (SubTaskDefType) t;
       replaceEditPanelWith(target, new SubtaskDefInputPanel("editpanel", (HibernateObjectModel<SubTaskDefType>) selectedModel));
     } else {
       replaceEditPanelWith(target, new EmptyPanel("editpanel"));
     }
 
   }
 
   private void replaceEditPanelWith(final AjaxRequestTarget target, final Panel edit) {
     edit.setOutputMarkupId(true);
     editPanel.replaceWith(edit);
     editPanel = edit;
     target.addComponent(editPanel);
   }
 
   @Override
   protected Component createToolbar(final String id) {
     return new TaskDefActions(id);
   }
 
   private class TaskDefActions extends Panel {
 
     public TaskDefActions(final String id) {
       super(id);
 
       final DownloadLink downloadLink = new DownloadLink("export", new AbstractReadOnlyModel<File>() {
 
         @Override
         public File getObject() {
           File tempFile = null;
           try {
             tempFile = File.createTempFile("taskdef", "export");
             // marshal to xml
             final JAXBContext context = JAXBContext.newInstance(ComplexTaskDef.class);
             final Marshaller marshaller = context.createMarshaller();
             final BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
             final ComplexTaskDef ctd = tree.getCurrentTaskdef().getObject();
             addRevisionTo(ctd);
             marshaller.marshal(ctd, bw);
             bw.close();
           } catch (final IOException e) {
             error("Konnte leider keine Datei schreiben, Infos siehe Logfile.");
             e.printStackTrace();
           } catch (final JAXBException e) {
             error("Konnte leider kein XML erstellen, Infos siehe Logfile.");
             e.printStackTrace();
           }
           return tempFile;
         }
 
 
         /**
          * Add current timestamp+author name as new revision.
          * 
          * @param ctd
          */
         private void addRevisionTo(final ComplexTaskDef ctd) {
           final Revision rev = new ObjectFactory().createComplexTaskDefRevisionsRevision();
           rev.setAuthor(AuthDataSession.get().getUser().getUsername());
           rev.setDate(System.currentTimeMillis());
           final List<Revision> revisions = ctd.getRevisions().getRevision();
          rev.setSerialNumber(revisions.size());
           revisions.add(rev);
         }
       }, "pruefung.xml");
       downloadLink.setDeleteAfterDownload(true);
 
       final Link deleteLink = new Link("delete") {
 
         @Override
         public void onClick() {
           final Object toDelete = tree.getSelected().getObject();
           System.out.println("removing " + toDelete);
           final Object parent = treeProvider.findParentOf(toDelete);
           System.out.println("parent is " + parent);
           if (parent instanceof Category) {
             System.out.println(((Category) parent).getTaskBlocksItems());
           }
           final org.hibernate.classic.Session session = Databinder.getHibernateSession();
           Transaction transaction = session.beginTransaction();
           session.save(treeProvider.removeFromParent(toDelete));
           session.flush();
           transaction.commit();
 
           transaction = session.beginTransaction();
           session.delete(toDelete);
           transaction.commit();
         }
 
       };
       deleteLink.add(new AttributeModifier("onclick", true, Model.of("return confirm('Sind Sie sicher, dass das selektierte Element gel&ouml;scht werden soll?');")));
 
       add(new PreviewLink("preview", new AbstractReadOnlyModel<ComplexTaskDef>() {
         @Override
         public ComplexTaskDef getObject() {
           return tree.getCurrentTaskdef().getObject();
         }
       }));
 
       add(downloadLink);
       add(deleteLink);
       // add(new NullPlug("delete"));
     }
   }
 }
