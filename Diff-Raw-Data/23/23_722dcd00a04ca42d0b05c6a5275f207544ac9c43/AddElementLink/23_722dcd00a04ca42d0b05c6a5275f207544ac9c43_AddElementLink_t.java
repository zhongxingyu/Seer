 /*
 
 Copyright (C) 2010 Steffen Dienst
 
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
 package de.elatexam.editor.pages.taskdef;
 
import org.apache.wicket.Session;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
 
 import com.google.common.collect.ImmutableMap;
 
import de.elatexam.editor.TaskEditorSession;
 import de.elatexam.editor.components.event.AjaxUpdateEvent;
 import de.elatexam.editor.components.panels.tree.ComplexTaskDefTree;
 import de.elatexam.editor.user.BasicUser;
 import de.elatexam.editor.util.Stuff;
 import de.elatexam.model.Category;
 import de.elatexam.model.ClozeSubTaskDef;
 import de.elatexam.model.ClozeTaskBlock;
 import de.elatexam.model.ClozeTaskBlock.ClozeConfig;
 import de.elatexam.model.ComplexTaskDef;
 import de.elatexam.model.ComplexTaskDef.Config;
 import de.elatexam.model.ComplexTaskDef.Config.CorrectionMode;
 import de.elatexam.model.ComplexTaskDef.Config.CorrectionMode.Regular;
 import de.elatexam.model.Indexed;
 import de.elatexam.model.MappingSubTaskDef;
 import de.elatexam.model.MappingTaskBlock;
 import de.elatexam.model.MappingTaskBlock.MappingConfig;
 import de.elatexam.model.McSubTaskDef;
 import de.elatexam.model.McTaskBlock;
 import de.elatexam.model.McTaskBlock.McConfig;
 import de.elatexam.model.PaintSubTaskDef;
 import de.elatexam.model.PaintTaskBlock;
 import de.elatexam.model.SubTaskDef;
 import de.elatexam.model.TaskBlock;
 import de.elatexam.model.TaskblockConfig;
 import de.elatexam.model.TextSubTaskDef;
 import de.elatexam.model.TextTaskBlock;
 import de.elatexam.model.manual.HomogeneousTaskBlock;
 
 /**
  * @author Steffen Dienst
  *
  */
 public class AddElementLink extends AjaxLink<Indexed> {
 	final static ImmutableMap<Class<?>, Integer> childMap = new ImmutableMap.Builder<Class<?>, Integer>()
	
 	.put(BasicUser.class, 0)
 	.put(ComplexTaskDef.class, 1)
 	.put(Category.class, 2)
 	.put(McTaskBlock.class, 3)
 	.put(MappingTaskBlock.class, 4)
 	.put(ClozeTaskBlock.class, 5)
 	.put(TextTaskBlock.class, 6)
 	.put(PaintTaskBlock.class, 7)
 	.build();
 
 	private ComplexTaskDefTree<Indexed> tree;
 
 	private ModalWindow selectTaskBlockModal;
 	private TaskSelectorModalWindow selectTaskModal;
 
 	public AddElementLink(String id, ModalWindow selectTaskBlockModal,
 			TaskSelectorModalWindow selectTaskModal, ComplexTaskDefTree<Indexed> tree) {
 		super(id);
 		this.tree = tree;
 		this.selectTaskBlockModal = selectTaskBlockModal;
 		this.selectTaskModal = selectTaskModal;
 	}
 
 	@Override
 	public void onClick(AjaxRequestTarget target) {
		IModel<Indexed> selectedModel = tree.getSelected();
		Indexed selectedObject = null;

		// nothing selected --> use root element
		if(selectedModel == null){
			selectedObject = TaskEditorSession.get().getUser();
		}else{
			selectedObject = selectedModel.getObject();
		}
 		Object newObj = null;
		
 		switch (childMap.get(selectedObject.getClass())) {
 			case 0 : // create a new complextaskdef
 				ComplexTaskDef newtaskdef = new ComplexTaskDef();
 				newtaskdef.setTitle("<Neue Prüfung>");
 				Config config = new Config();
 				config.setCorrectionMode(new CorrectionMode());
 				config.getCorrectionMode().setRegular(new Regular());
 				newtaskdef.setConfig(config);
 				((BasicUser) selectedObject).getTaskdefs().add(newtaskdef);
 				newObj = newtaskdef;
 				new AjaxUpdateEvent(this,target).fire();
 				break;
 			case 1 : // create a new category
 				Category cat = new Category();
 				cat.setTitle("<Neue Kategorie>");
 				cat.setId(Long.toString(System.nanoTime()));
 				((ComplexTaskDef) selectedObject).getCategory().add(cat);
 				newObj = cat;
 				new AjaxUpdateEvent(this,target).fire();
 				break;
 			case 2 : // show taskblock selection modal window
 				selectTaskBlockModal.show(target);
 				break;
 			case 3 :
 			case 4 :
 			case 5 :
 			case 6 :
 			case 7 :
 				selectTaskModal.showFor((Class<? extends SubTaskDef>) selectedObject.getClass(), target);
 				break;
 			default :
 				break;
 		}
 		Stuff.saveAll(newObj, selectedObject);
 	}
 
 	/**
 	 * Create a new instance of the given taskblock type. Configure child
 	 * objects.
 	 *
 	 * @param taskblockclass
 	 */
 	public void createTaskblock(Class<? extends TaskBlock> taskblockclass) {
 		Object selectedObject = tree.getSelected().getObject();
 		if (selectedObject instanceof Category) {
 			try {
 				TaskBlock taskblock = taskblockclass.newInstance();
 				taskblock.setConfig(new TaskblockConfig());
 
 				((Category) selectedObject).getTaskBlocks().add((HomogeneousTaskBlock) taskblock);
 
 				// set subclass specific config
 				switch (childMap.get(taskblockclass)) {
 					case 3 :
 						((McTaskBlock) taskblock).setMcConfig(new McConfig(new McConfig.Regular(), null));
 						break;
 					case 4 :
 						((MappingTaskBlock) taskblock).setMappingConfig(new MappingConfig());
 						break;
 					case 5 :
 						((ClozeTaskBlock) taskblock).setClozeConfig(new ClozeConfig());
 						break;
 					default :
 						break;
 				}
 
 				Stuff.saveAll(selectedObject);
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/**
 	 * Add all {@link SubTaskDef}s to an appropriate subtype of
 	 * {@link TaskBlock}.
 	 *
 	 * @param subtaskdef
 	 */
 	public void addTasks(SubTaskDef... subtaskdefs) {
 		Object o = tree.getSelected().getObject();
 		if (o instanceof TaskBlock) {
 			for (SubTaskDef std : subtaskdefs) {
 				switch (childMap.get(o.getClass())) {
 					case 3 :
 						((McTaskBlock) o).getMcSubTaskDef().add((McSubTaskDef) std);
 						break;
 					case 4 :
 						((MappingTaskBlock) o).getMappingSubTaskDef().add((MappingSubTaskDef) std);
 						break;
 					case 5 :
 						((ClozeTaskBlock) o).getClozeSubTaskDef().add((ClozeSubTaskDef) std);
 						break;
 					case 6 :
 						((TextTaskBlock) o).getTextSubTaskDef().add((TextSubTaskDef) std);
 						break;
 					case 7 :
 						((PaintTaskBlock) o).getPaintSubTaskDef().add((PaintSubTaskDef) std);
 						break;
 					default :
 						throw new RuntimeException("Unknown taskblock type, can't add subtaskdef of type " + std.getClass());
 				}
 			}
 			Stuff.saveAll(o);
 		}
 
 	}
 
 }
