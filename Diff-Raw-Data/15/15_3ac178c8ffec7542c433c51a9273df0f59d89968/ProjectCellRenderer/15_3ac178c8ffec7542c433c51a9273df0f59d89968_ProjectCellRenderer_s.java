 package be.artesis.timelog.gui;
 
 import be.artesis.timelog.view.Project;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.font.TextAttribute;
 import java.util.Map;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 
 class ProjectCellRenderer extends JLabel implements ListCellRenderer {
 
     private Project p;
     private static DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
     // private static LineBorder border = new LineBorder(Color.BLACK, 1);
 
      public ProjectCellRenderer() {
          setOpaque(true);
      }
 
 	@Override
 	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 		JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 		Font f = list.getFont();
		if (value.getClass().equals(String.class)) {
 			renderer.setFont(new Font(f.getName(),f.getStyle() | Font.ITALIC, f.getSize()));
 		} else {
 			p = (Project) value;
 
 			try {
 				if (p.equals(UserInterface.getCurrentProject())) {
 					// FIXME andere kleur?
 					renderer.setForeground(new Color(25, 97, 252));
 				}
 			} catch (GUIException ex) {
 				//Dit moet leeg zijn
 			}
 			if (p.getPercentageComplete() == 1) {
 				Map attributes = f.getAttributes();
 				attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
 				Font font = new Font(attributes);
 				renderer.setFont(font);
 			}
 		}
 		return renderer;
 	}
 
  }
