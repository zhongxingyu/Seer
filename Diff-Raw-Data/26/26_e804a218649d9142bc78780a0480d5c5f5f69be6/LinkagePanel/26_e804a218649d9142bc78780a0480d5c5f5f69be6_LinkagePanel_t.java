 package ModelBuilder;
 
 import java.awt.GridLayout;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import org.jdom.Element;
 
 import VGL.Messages;
 
 public class LinkagePanel extends JPanel {

 	private ModelBuilderUI mbui;
 
 	private JComboBox g1g2Linked;
 	private JComboBox g2g3Linked;
 	private JComboBox g3g1Linked;

 	private JComboBox g1g2LinkageRelevantCage;
 	private JComboBox g2g3LinkageRelevantCage;
 	private JComboBox g3g1LinkageRelevantCage;	

 	private String[] chars;
 
 	public LinkagePanel(String[] characters, ModelBuilderUI mbui) {
 		this.mbui = mbui;
 
 		g1g2LinkageRelevantCage = new JComboBox(getCageList());
 		g2g3LinkageRelevantCage = new JComboBox(getCageList());
 		g3g1LinkageRelevantCage = new JComboBox(getCageList());

 		String[] choices = new String[51];
 		this.chars = characters;
 		choices[0] = Messages.getInstance().getString("VGLII.Unknown");
 		choices[1] = Messages.getInstance().getString("VGLII.Unlinked");
 		for (int i = 2; i < 51; i++) {
 			choices[i] = Messages.getInstance().getString("VGLII.Linked") 
 			+ " & RF= " + (i - 1) + "%";
 		}
 		g1g2Linked = new JComboBox(choices);
 		g2g3Linked = new JComboBox(choices);
 		g3g1Linked = new JComboBox(choices);
 
 
 		add(new JLabel(
 				chars[0] + " "
 				+ Messages.getInstance().getString("VGLII.And") + " "
 				+ chars[1] + " "
 				+ Messages.getInstance().getString("VGLII.Are")));
 		add(g1g2Linked);

 		add(new JLabel(Messages.getInstance().getString("VGLII.RelevantCages")));
 		add(g1g2LinkageRelevantCage);
 
 		if (chars.length == 3) {
 			setLayout(new GridLayout(3,2));
 			add(new JLabel(
 					chars[1] + " "
 					+ Messages.getInstance().getString("VGLII.And") + " "
 					+ chars[2] + " "
 					+ Messages.getInstance().getString("VGLII.Are")));
 			add(g2g3Linked);
 
 			add(new JLabel(
 					chars[0] + " "
 					+ Messages.getInstance().getString("VGLII.And") + " "
 					+ chars[2] + " "
 					+ Messages.getInstance().getString("VGLII.Are")));
 			add(g3g1Linked);
 
 		}
 	}
 
 	public void setStateFromFile(Element element) {
 		List<Element> elements = element.getChildren();
 		Iterator<Element> it = elements.iterator();
 		while(it.hasNext()) {
 			Element e = it.next();
 			if (e.getName().equals("G1G2")) {
 				g1g2Linked.setSelectedItem((String)e.getText());
 			}
 			if (e.getName().equals("G2G3")) {
 				g2g3Linked.setSelectedItem((String)e.getText());
 			}
 			if (e.getName().equals("G3G1")) {
 				g3g1Linked.setSelectedItem((String)e.getText());
 			}
 		}
 	}
 
 	public Element save() {
 		Element lpe = new Element("LinkagePanel");
 
 		Element e = new Element("G1G2");
 		e.setText((String)g1g2Linked.getSelectedItem());
 		lpe.addContent(e);
 
 		e = new Element("G2G3");
 		e.setText((String)g2g3Linked.getSelectedItem());
 		lpe.addContent(e);
 
 		e = new Element("G3G1");
 		e.setText((String)g3g1Linked.getSelectedItem());
 		lpe.addContent(e);
 
 		return lpe;
 	}
 
 	public String getAsHtml(boolean isForGrader) {
 		StringBuffer b = new StringBuffer();
 		b.append("<b>");
 		b.append(Messages.getInstance().getString("VGLII.Linkage"));
 		b.append("</b><br><ul>");
 		b.append("<li>" + chars[0] + " "
 				+ Messages.getInstance().getString("VGLII.And") + " "
 				+ chars[1] + " "
 				+ Messages.getInstance().getString("VGLII.Are") + " ");
 		b.append((String)g1g2Linked.getSelectedItem() + "</li>");
 		if (chars.length == 3) {
 			b.append("<li>" + chars[1] + " "
 					+ Messages.getInstance().getString("VGLII.And") + " "
 					+ chars[2] + " "
 					+ Messages.getInstance().getString("VGLII.Are") + " ");
 			b.append((String)g2g3Linked.getSelectedItem() + "</li>");
 			b.append("<li>" + chars[0] + " "
 					+ Messages.getInstance().getString("VGLII.And") + " "
 					+ chars[2] + " "
 					+ Messages.getInstance().getString("VGLII.Are") + " ");
 			b.append((String)g3g1Linked.getSelectedItem() + "</li>");
 		}
 		b.append("</ul>");
 		return b.toString();
 	}

 	public String[] getCageList() {
 
		// if the mbui isn't set up yet, it must be a problem starting fresh
		//  so make no cages in list (to avoid null pointer ref - see dev_log 4/11/12)
		int numCages = 0;
		if (mbui != null) {
			numCages = mbui.getVGLII().getNumCages();
		}
 		/*
 		 * see if there are no CageUI's in the VGL
 		 * 	if so, that means this is a problem for grading
 		 * 	so need to get number of cages from cage set that was loaded in file.
 		 */
 		if (numCages == -1) numCages = mbui.getGenticModelAndCageSet().getCages().size();
 
 		String[] list = new String[numCages + 1];
 		list[0] = "?";
 		for (int i = 1; i < numCages + 1; i++) {
 			list[i] = Messages.getInstance().getString("VGLII.Cage") + " " + i;
 		}
 		return list;
 	}
 
 }
