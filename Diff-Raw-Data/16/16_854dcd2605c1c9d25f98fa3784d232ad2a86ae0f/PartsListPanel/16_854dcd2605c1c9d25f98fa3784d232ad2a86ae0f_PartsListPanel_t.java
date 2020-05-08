 package manager.panel;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.Border;
 import javax.swing.JScrollPane;
 
 import manager.util.ClickablePanel;
 import manager.util.ClickablePanelClickHandler;
 import manager.util.OverlayPanel;
 import manager.util.WhiteLabel;
 import Utils.Constants;
 import factory.PartType;
 
 
 public class PartsListPanel extends OverlayPanel {
 	PartsListPanelHandler handler;
 	ArrayList<PartType> partTypes = new ArrayList<PartType>();
 	HashMap<PartType, ClickablePanel> panels = new HashMap<PartType, ClickablePanel>();
 	
 	public static final Border PADDING = BorderFactory.createEmptyBorder(20, 20, 20, 20);
 	public static final Border FIELD_PADDING = BorderFactory.createEmptyBorder(5, 5, 5, 5);
 	public static final Border MEDIUM_PADDING = BorderFactory.createEmptyBorder(10, 10, 10, 10);
 	public static final Border BOTTOM_PADDING = BorderFactory.createEmptyBorder(0, 0, 20, 0);
 	public static final Border TOP_PADDING = BorderFactory.createEmptyBorder(20, 0, 5, 0);
 	public static final Border VERTICAL_PADDING = BorderFactory.createEmptyBorder(10, 0, 10, 0);
 	
 	
 	public PartsListPanel(PartsListPanelHandler h) {
 		super();
 		handler = h;
 		partTypes = (ArrayList<PartType>) Constants.DEFAULT_PARTTYPES.clone();
 		
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		setAlignmentX(LEFT_ALIGNMENT);
 		setBorder(PADDING);
 		
 		parsePartTypes();
 
 	}
 	
 	public void parsePartTypes() {
 		panels.clear();
 		removeAll();
 		repaint();
 		
 		for(PartType pt : partTypes) {
 			ClickablePanel panel = new ClickablePanel(new EditClickHandler(pt));
 			panel.setSize(350, 50);
 			panel.setBorder(MEDIUM_PADDING);
 			panel.setAlignmentX(0);
 			
 			add(panel);
 			
 			JLabel imageLabel = new JLabel(new ImageIcon(pt.getImage()));
 			imageLabel.setMinimumSize(new Dimension(50, 30));
 			imageLabel.setPreferredSize(new Dimension(50, 30));
 			imageLabel.setMaximumSize(new Dimension(50, 30));
 			panel.add(imageLabel);
 			
 			WhiteLabel nameLabel = new WhiteLabel("Part: " + pt.getName());
			nameLabel.setLabelSize(165, 30);
 			panel.add(nameLabel);
 			
 			JButton deleteButton = new JButton("delete");
 			deleteButton.addActionListener(new DeleteClickHandler(pt));
 			panel.add(deleteButton);
 			
 			// add padding
 			add(Box.createVerticalStrut(10));
 			panels.put(pt, panel);
 		}
		validate();
 	}
 	
 	public void updatePartTypes(ArrayList<PartType> pt) {
 		partTypes = pt;
 		parsePartTypes();
 		restoreColors();
 	}
 	
 	public void restoreColors() {
 		for(ClickablePanel panel : panels.values()) {
 			panel.restoreColor();
 		}
 	}
 	
 	public interface PartsListPanelHandler {
 		public void editPart(PartType pt);
 		public void deletePart(PartType pt);
 	}
 	
 	private class EditClickHandler implements ClickablePanelClickHandler{
 		PartType pt;
 		public EditClickHandler(PartType pt) {
 			this.pt = pt;
 		}
 
 		@Override
 		public void mouseClicked() {
 			restoreColors();
 			handler.editPart(pt);
 			panels.get(pt).setColor(new Color(5, 151, 255));
 		}
 	}
 	
 	private class DeleteClickHandler implements ActionListener{
 		PartType pt;
 		public DeleteClickHandler(PartType pt) {
 			this.pt = pt;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			restoreColors();
 			handler.deletePart(pt);
 			panels.get(pt).setColor(new Color(150, 6, 6));
 		}
 	}
 }
