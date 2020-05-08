 package net.unikernel.bummel.visual_editor;
 
 import com.kitfox.svg.SVGDiagram;
 import com.kitfox.svg.SVGException;
 import java.awt.Point;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.net.MalformedURLException;
 import java.util.HashMap;
 import java.util.Map;
 import net.unikernel.bummel.project_model.api.BasicElement;
 import org.netbeans.api.visual.layout.LayoutFactory;
 import org.netbeans.api.visual.widget.Scene;
 import org.netbeans.api.visual.widget.Widget;
 /**
  *
  * @author mcangel
  */
 public class ElementWidget extends Widget implements PropertyChangeListener
 {
 	private Widget body;
 	private SvgWidget imageWidget;
 	private ElementNode elNode;
 	private Map<Integer, SvgWidget> stateImages;
 	private static SVGDiagram defaultDiagram;
 
 	public ElementWidget(Scene scene, ElementNode element) throws MalformedURLException, SVGException
 	{
 		super(scene);
 		this.elNode = element;
 		this.elNode.getLookup().lookup(BasicElement.class)
 				.addPropertyChangeListener(this);
 		stateImages = new HashMap<>();
 		body = new Widget(scene);
 		body.setCheckClipping(true);	//to prevent element "pointing" on dragging
 		body.setLayout(LayoutFactory.createVerticalFlowLayout()); //all child widgets will be located at their preffered location
 		addChild(body);
 		imageWidget = new SvgWidget(scene, this.elNode.getGraphicsURL(this.elNode.getLookup().lookup(BasicElement.class).getState()));
 		if (imageWidget.getDiagram() != null)
 		{//if svg was parsed successful
 			stateImages.put(this.elNode.getLookup().lookup(BasicElement.class).getState(), imageWidget);
 		} else if (defaultDiagram == null)
 		{//if svg parsing failed and there was no default diagram loaded yet
 			imageWidget.setDiagram(getClass().getResource("default_element_graphics.svg"));
 			defaultDiagram = imageWidget.getDiagram();
 		} else
 		{//if svg parsing failed and the default diagram was already loaded
 			imageWidget.setDiagram(defaultDiagram);
 		}
 		body.addChild(imageWidget);
 	}
 	
 	/**
 	 * Attaches a pin widget to the node widget.
 	 *
 	 * @param widget the pin widget
 	 */
 	public void attachPortWidget(ElementPortWidget widget)
 	{
 		widget.setCheckClipping(true);
 		addChild(widget);
 		switch (elNode.getPortDirection(widget.getPort()))
 		{
 			case "right":
 				widget.setPreferredLocation(new Point(imageWidget.getPreferredBounds().width, 
 							//offset from the middle, offset in "percents" of the height half
 							(int) (imageWidget.getPreferredBounds().height*(1+elNode.getPortOffset(widget.getPort()).doubleValue())/2)));
 				widget.indentPort(imageWidget.getPreferredBounds().width);
 				break;
 			case "left":
 				widget.setPreferredLocation(new Point(0, 
 							//offset from the middle, offset in "percents" of the height half
 							(int) (imageWidget.getPreferredBounds().height*(1+elNode.getPortOffset(widget.getPort()).doubleValue())/2)));
 				widget.indentPort(imageWidget.getPreferredBounds().width);
 				break;
 			case "up":
 				widget.setPreferredLocation(new Point((int) (imageWidget.getPreferredBounds().width*(1+elNode.getPortOffset(widget.getPort()).doubleValue())/2),imageWidget.getPreferredBounds().height));
 				break;
 			case "down":
 				widget.setPreferredLocation(new Point((int) (imageWidget.getPreferredBounds().width*(1+elNode.getPortOffset(widget.getPort()).doubleValue())/2),0));
 				break;
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt)
 	{
 		if(BasicElement.PROP_STATE.equals(evt.getPropertyName()))
 		{
 			if(!stateImages.containsKey((Integer)evt.getNewValue()))
 			{
 				SvgWidget newImage = new SvgWidget(this.getScene(), this.elNode.getGraphicsURL((Integer)evt.getNewValue()));
				if(((SvgWidget)imageWidget).getDiagram() != null)
				{
					stateImages.put((Integer)evt.getNewValue(), newImage);
 				}
 			}
			if(stateImages.containsKey((Integer)evt.getNewValue()))
			{
				imageWidget.removeFromParent();
				imageWidget = stateImages.get((Integer)evt.getNewValue());
				body.addChild(imageWidget);
				this.revalidate();
				this.repaint();
			}
 		}
 	}
 }
