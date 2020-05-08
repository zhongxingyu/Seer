 package ui.isometric.builder.inspectors;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 
 import ui.isometric.builder.InspectorPanel;
 import game.things.Enemy;
 
/**
 * Inspector for NPCs
 * 
 * @author melby
 *
 */
 public class NPCInspector extends GameThingInspector<Enemy> {
 	private static final long serialVersionUID = 1L;
 
	/**
	 * Create a NPCInspector with a given NPC
	 * @param t
	 * @param inspectorPanel
	 */
 	public NPCInspector(final Enemy t, InspectorPanel inspectorPanel) {
 		super(t, inspectorPanel);
 		
 		final JLabel label = new JLabel(t.name());
 		
		JButton name = new JButton("Set Name");
 		name.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				String name = JOptionPane.showInputDialog("Enter name");
 				t.name(name);
 				label.setText(name);
 			}
 		});
 		this.add(name);
 		this.add(label);
 	}
 }
