 package gui.watcher;
 
 import gui.Interaction;
 import gui.Language;
 import gui.Main;
 import gui.components.JTreeView;
 import gui.templates.Watcher;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JComponent;
 import javax.swing.JScrollPane;
 
 @SuppressWarnings("serial")
 public class JKnockOutWatcherWindow extends Watcher {
 	private Main main;
 	private JComponent treeView;
 
 	public JKnockOutWatcherWindow(Main m) {
 		super(Language.get("knockOutWatcher"), m);
 		main = m;
 		treeView = new JTreeView(main.getTournament().getKnockOut());
 
 	}
 
 	public static String getHtml(JTreeView jTreeView) {
 		jTreeView.repaint();
 		BufferedImage img = new BufferedImage(jTreeView.getPreferredSize().width,
 				jTreeView.getPreferredSize().height, BufferedImage.TYPE_INT_ARGB);
		jTreeView.paintComponent(img.getGraphics());
 
 		File file;
 		if (System.getProperty("java.io.tmpdir").charAt(0) == '/')
 			file = new File(System.getProperty("java.io.tmpdir")
 					+ "/treeImg.png");
 		else
 			file = new File(System.getProperty("java.io.tmpdir")
 					+ "\\treeImg.png");
 
 		try {
 			ImageIO.write(img, "png", file);
 		} catch (IOException e) {
 			System.err.println("Image " + file.getAbsolutePath() + " not writable.");
 		}
 
 		return "<h2>" + Language.get("knockOut")
 				+ "</h2><p><img src=\"treeImg.png\" alt=\"treeImg.png\"></p>";
 	}
 
 	public static void print(JTreeView treeView) {
 		Interaction.saveHtml("groupTable",
 				"<html><head><title>" + Language.get("knockOut")
 						+ "</title></head><body>" + getHtml(treeView)
 						+ "</body></html>");
 		Interaction.print("groupTable");
 	}
 
 	@Override
 	public void generateWindow() {
 		add(new JScrollPane(treeView));
 		pack();
 		setVisible(true);
 	}
 
 	@Override
 	public void refresh() {
 		treeView.repaint();
 		repaint();
 	}
 }
