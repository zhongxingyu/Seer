 package ch.zhaw.dynsys.gui;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import ch.zhaw.dynsys.gui.models.Configuration;
 import ch.zhaw.dynsys.simulation.Culture;
 
 public class CultureEditor extends JScrollPane {
 
 	private static final long serialVersionUID = 1L;
 	private final JPanel panel;
 	private final GraphPropertyEditor graphPropertyEditor;
 	private List<Culture> cultures;
 
 	public CultureEditor(GraphPropertyEditor graphPropertyEditor) {
 		this.panel = new JPanel();
 		this.cultures = new ArrayList<Culture>();
 		this.graphPropertyEditor = graphPropertyEditor;
 
 		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 
 		Dimension d = new Dimension(300, 100);
 		getVerticalScrollBar().setUnitIncrement(d.height);
 
 		viewport.add(panel);
 		viewport.setPreferredSize(d);
 		viewport.setMaximumSize(d);
 
 		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 	}
 
 	public void remove(CulturePropertyEditor propEditor) {
 		cultures.remove(propEditor.getCulture());
 		panel.remove(propEditor);
 	}
 
 	public void add(Culture culture) {
 		cultures.add(culture);
 		panel.add(new CulturePropertyEditor(this, culture));
		panel.revalidate();
 	}
 
 	public void set(Configuration configuration) {
 		panel.removeAll();
 
 		graphPropertyEditor.setGraphProperty(configuration.getGraphProperty());
 
 		List<Culture> cultures = configuration.getCultures();
 		this.cultures.clear();
 		for (Culture culture : cultures) {
 			add(culture);
 		}
 		add(new Culture());
 		revalidate();
 		repaint();
 	}
 
 	public Configuration getConfiguration() {
 		return new Configuration(graphPropertyEditor.getGraphProperty(),
 				cultures);
 	}
 
 	@Override
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 		// getParent().revalidate();
 		getParent().repaint();
 		revalidate();
 		repaint();
 	}
 }
