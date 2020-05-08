 /*
  * This file is part of SchedVis.
  * 
  * SchedVis is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * SchedVis is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * SchedVis. If not, see <http://www.gnu.org/licenses/>.
  */
 /**
  * 
  */
 package cz.muni.fi.spc.SchedVis.rendering;
 
 import java.awt.Component;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTree;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 
 import cz.muni.fi.spc.SchedVis.model.entities.Machine;
 import cz.muni.fi.spc.SchedVis.model.entities.MachineGroup;
 import cz.muni.fi.spc.SchedVis.model.models.ScheduleTreeModel;
 import cz.muni.fi.spc.SchedVis.model.models.TimelineSliderModel;
 import cz.muni.fi.spc.SchedVis.ui.MachinePanel;
 
 /**
  * Tree Cell Renderer for Swing, creating the rendering requests and receiving
  * the rendered schedules.
  * 
  * @author Lukáš Petrovický <petrovicky@mail.muni.cz>
  * 
  */
 public class ScheduleTreeCellRenderer extends DefaultTreeCellRenderer {
 
 	private static final long serialVersionUID = -5148385915562957149L;
 
 	/**
 	 * Get the panel that represents "tree handle" for a group of machines.
 	 * 
 	 * @param item
 	 *          The group in question. If null, the group for machines in no
 	 *          explicit groups is rendered.
 	 * @return
 	 */
 	private JPanel getGroup(final MachineGroup item) {
 		JPanel target = new JPanel();
 		if (item == null) {
 			target.add(new JLabel("Ungrouped Machines"));
 		} else {
 			try {
 				target.add(new JLabel("Group '" + item.getName()));
 			} catch (NullPointerException ex) {
 				target.add(new JLabel("Group '" + item.getName()));
 			}
 		}
 		return target;
 	}
 
 	/**
 	 * Get the panel with the schedule. Commands the schedule rendering
 	 * controller.
 	 * 
 	 * @param item
 	 *          Machine in question. Current clock will be retrieved from the
 	 *          model(s).
 	 * @return The panel.
 	 */
 	private JPanel getMachine(final Machine item) {
 		Integer clock = TimelineSliderModel.getInstance().getValue();
 		try {
 			final MachinePanel pane = new MachinePanel();
 			pane.setImage(ScheduleRenderingController.getInstance().getRendered(item,
 			    clock));
 			return pane;
 		} catch (final Exception e) {
 			final JPanel p = new JPanel();
 			p.add(new JLabel(e.toString()));
 			return p;
 		}
 	}
 
 	@Override
 	public Component getTreeCellRendererComponent(final JTree tree,
 	    final Object value, final boolean sel, final boolean expanded,
 	    final boolean leaf, final int row, final boolean hasFocus) {
 		final Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
 		if (userObject instanceof Machine) { // is a machine
 			return this.getMachine((Machine) userObject);
 		} else if (userObject instanceof MachineGroup) { // is a group
 			return this.getGroup((MachineGroup) userObject);
 		} else if (ScheduleTreeModel.ID_UNGROUPED.equals(userObject)) {
 			// "ungrouped machines" group
 			return this.getGroup(null);
 		} else {
 			// should never happen
 			final JPanel p = new JPanel();
 			p.add(new JLabel("Unknown object!"));
 			return p;
 		}
 	}
 
 }
