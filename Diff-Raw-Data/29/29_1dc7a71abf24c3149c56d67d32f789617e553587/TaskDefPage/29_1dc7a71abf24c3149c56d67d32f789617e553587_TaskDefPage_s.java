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
 
 import java.util.List;
 
 import net.databinder.auth.hib.AuthDataSession;
 import net.databinder.models.hib.HibernateListModel;
 import net.databinder.models.hib.HibernateObjectModel;
 import net.databinder.models.hib.QueryBuilder;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.panel.EmptyPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import de.elateportal.editor.components.panels.PreviewPanel;
 import de.elateportal.editor.components.panels.tasks.CategoryPanel;
 import de.elateportal.editor.components.panels.tasks.SubtaskDefInputPanel;
 import de.elateportal.editor.components.panels.tree.ComplexTaskDefTree;
 import de.elateportal.editor.components.panels.tree.ComplexTaskdefTreeProvider;
 import de.elateportal.model.Category;
 import de.elateportal.model.ComplexTaskDef;
 import de.elateportal.model.SubTaskDefType;
 
 /**
  * @author Steffen Dienst
  * 
  */
 public class TaskDefPage extends SecurePage {
 
   private Panel editPanel;
   private ComplexTaskDefTree tree;
 
   public TaskDefPage() {
     final IModel<List<ComplexTaskDef>> tasklistmodel = new HibernateListModel(new QueryBuilder() {
       public Query build(final Session sess) {
         final Query q = sess.createQuery(String.format("select tasks from BasicUser u left join u.taskdefs tasks where u.username='%s'",
             AuthDataSession.get().getUser().getUsername()));
         q.setResultTransformer(RemoveNullResultTransformer.INSTANCE);
         return q;
       }
     });
     add(tree = new ComplexTaskDefTree("tree", this, new ComplexTaskdefTreeProvider(tasklistmodel)));
     editPanel = new EmptyPanel("editpanel");
     add(editPanel.setOutputMarkupId(true));
   }
 
   /**
    * Replace right hand form panel with an edit panel for the given model
    * object.
    * 
    * @param t
    * @param target
    */
   public void renderPanelFor(final Object t, final AjaxRequestTarget target) {
     if (t instanceof ComplexTaskDef) {
      replaceEditPanelWith(target, new PreviewPanel("editpanel", tree.getCurrentTaskdef()));
     } else if (t instanceof Category) {
       final Category cat = (Category) t;
      replaceEditPanelWith(target, new CategoryPanel("editpanel", new HibernateObjectModel<Category>(Category.class, cat
          .getHjid())));
     } else if (t instanceof SubTaskDefType) {
       final SubTaskDefType st = (SubTaskDefType) t;
       replaceEditPanelWith(target, new SubtaskDefInputPanel("editpanel", new HibernateObjectModel<SubTaskDefType>(st.getClass(),
           st.getHjid())));
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
 }
