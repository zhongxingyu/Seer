 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import model.AnimatorModel;
 import view.CanvasView;
 import view.PaletteView;
 import view.PlayerView;
 import view.SliderView;
 import view.ToolbarView;
 
//TODO: test minimum dimensions on VM
 public class Animator {
 	public static void main(String[] args) {
 
 		AnimatorModel model = new AnimatorModel();
 		JFrame frame = new JFrame("Assignment 3 - MVC");
 
 		Container content = frame.getContentPane();
 		content.setLayout(new BorderLayout());
 		CanvasView view = new view.CanvasView(model);
 		frame.getContentPane().add(view, BorderLayout.CENTER);
 
 		ToolbarView toolbar = new view.ToolbarView(model);
 		content.add(toolbar, BorderLayout.NORTH);
 
 		JPanel subPanel = new JPanel();
 		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
 		SliderView slider = new view.SliderView(model);
 		PlayerView player = new view.PlayerView(model);
 		subPanel.add(slider);
 		subPanel.add(player);
 		content.add(subPanel, BorderLayout.SOUTH);
 
 		PaletteView customizer = new view.PaletteView(model);
 		content.add(customizer, BorderLayout.EAST);
 
 		frame.pack();
 		frame.setSize(800, 600);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setLocation(100, 100);
 		frame.setVisible(true);
 		frame.setMinimumSize(new Dimension(800, 600));
 
 	}
 }
