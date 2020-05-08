 package ui.isometric.builder.inspectors;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 
 import ui.isometric.builder.InspectorPanel;
 import game.things.Enemy;
 
 public class NPCInspector extends GameThingInspector<Enemy> {
 	private static final long serialVersionUID = 1L;
 
 	public NPCInspector(final Enemy t, InspectorPanel inspectorPanel) {
 		super(t, inspectorPanel);
 		
 		final JLabel label = new JLabel(t.name());
 		
		JButton name = new JButton("Set Amount");
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
