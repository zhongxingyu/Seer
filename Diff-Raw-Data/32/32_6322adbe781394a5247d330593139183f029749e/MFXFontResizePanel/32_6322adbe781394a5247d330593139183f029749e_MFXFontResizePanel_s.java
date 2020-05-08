 package wicket.contrib.mootools.plugins;
 
 import org.apache.wicket.RequestCycle;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.panel.Panel;
 
 public class MFXFontResizePanel extends Panel {
 	private ResourceReference SMALLER = new ResourceReference(MFXFontResizePanel.class, "smaller.gif");
 	private ResourceReference LARGER = new ResourceReference(MFXFontResizePanel.class, "larger.gif");
 	private static final long serialVersionUID = 1L;
 
 	public MFXFontResizePanel(final String id, final String domId) {
 		super(id);
 
 		WebMarkupContainer container1, container2;
 		add(container1 = new WebMarkupContainer("smaller"));
		container1.add(new MFXFontResizer(-2, 16).setContainer(domId));
 		add(container2 = new WebMarkupContainer("larger"));
		container2.add(new MFXFontResizer(2, 10).setContainer(domId));
 
 		String smallerurl = RequestCycle.get().urlFor(SMALLER).toString();
 		String largerurl = RequestCycle.get().urlFor(LARGER).toString();
 		container1.add(new WebMarkupContainer("smallerimg").add(new SimpleAttributeModifier("src", smallerurl)));
 		container2.add(new WebMarkupContainer("largerimg").add(new SimpleAttributeModifier("src", largerurl)));
 	}
 
 }
