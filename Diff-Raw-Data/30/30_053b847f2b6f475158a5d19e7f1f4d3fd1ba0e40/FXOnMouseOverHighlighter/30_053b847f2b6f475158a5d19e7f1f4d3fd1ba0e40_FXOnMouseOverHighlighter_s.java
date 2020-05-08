 /*
  * $Id$ $Revision$ $Date$
  * 
  * ==============================================================================
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package wicket.contrib.dojo.dojofx;
 
 import wicket.AttributeModifier;
 import wicket.Component;
 import wicket.markup.html.internal.HtmlHeaderContainer;
 import wicket.model.Model;
 
 /**
  * FX highlighter which reacts to mousover events.
  * 
  * @author Ruud Booltink
  * @author Marco van de Haar
  * 
  */
 public class FXOnMouseOverHighlighter extends DojoFXHandler
 {
 
 	private final String type;
 	private String HTMLID;
 	private String componentId;
 	private RGB startColor;
 	private RGB endColor;
 
 	/**
 	 * Constructor for highlighter that highlights from current bg-color to
 	 * toR,toG,toB.
 	 * 
 	 * @param duration
 	 * @param trigger
 	 * @param toR
 	 * @param toG
 	 * @param toB
 	 */
 	public FXOnMouseOverHighlighter(int duration, Component trigger, int toR, int toG, int toB)
 	{
 		super("OnMouseOver", duration, trigger);
 		this.type = "b2c";
 		endColor = new RGB(toR, toG, toB);
 
 	}
 
 	/**
 	 * Constructor for highlighter that highlights from startR/G/B to endR/G/B.
 	 * 
 	 * @param duration
 	 * @param trigger
 	 * @param startR
 	 * @param startG
 	 * @param startB
 	 * @param endR
 	 * @param endG
 	 * @param endB
 	 */
 	public FXOnMouseOverHighlighter(int duration, Component trigger, int startR, int startG,
 			int startB, int endR, int endG, int endB)
 	{
 		super("OnMouseOver", duration, trigger);
 		this.type = "c2c";
 		startColor = new RGB(startR, startG, startB);
 		endColor = new RGB(endR, endG, endB);
 	}
 
 	/**
 	 * @see wicket.AjaxHandler#renderHeadContribution(wicket.markup.html.internal.HtmlHeaderContainer)
 	 */
 	protected void renderHeadContribution(HtmlHeaderContainer container)
 	{
 		// String to be written to header
 		String s;
 		// dojo function calls for higlighting
 		String highlightInFunction;
 		String highlightOutFunction;
 
 		// check for type, and call dojo.fx.html so that:
 		// it highlights node over duration (from startOpac to endOpac) and with
 		// callback.
 		// callback sets the right state variable to the present state and
 		// does mouseover checks for stability improvements.
 		// the following code might look a bit abracadabra, but it works and is
 		// thouroughly stress-tested.
 		if (type == "c2c")
 		{
 			highlightInFunction = "dojo.fx.html.colorFade(node, " + startColor.toString() + ","
 					+ endColor.toString() + ", duration, function(){" + componentId
 					+ "_highlighterState='highlighted';if(" + componentId + "_mouseover == 0){"
 					+ componentId + "_highlight(id, duration);}});";
 			highlightOutFunction = "dojo.fx.html.colorFade(node, " + endColor.toString() + ","
 					+ startColor.toString() + ", duration, function(){" + componentId
 					+ "_highlighterState='unhighlighted';if(" + componentId + "_mouseover == 1){"
 					+ componentId + "_highlight(id, duration);}});";
 		}
 		else
 		{
 			highlightInFunction = "dojo.fx.html.colorFadeOut(node, " + endColor.toString()
 					+ ", duration ,0,function(){" + componentId
 					+ "_highlighterState='highlighted';if(" + componentId + "_mouseover == 0){"
 					+ componentId + "_highlight(id, duration);}});";
 			highlightOutFunction = "dojo.fx.html.colorFadeOut(node, startbc, duration ,0,function(){"
 					+ componentId
 					+ "_highlighterState='unhighlighted';if("
 					+ componentId
 					+ "_mouseover == 1){" + componentId + "_highlight(id, duration);}});";
 		}
 
 
 		s = "\t<script language=\"JavaScript\" type=\"text/javascript\">\n" + "\t" + componentId
 				+ "_highlighterState = 'unhighlighted'; \n" + "\t" + componentId
 				+ "_first = false; \n" + "\t" + componentId + "_mouseover = 0; \n";
 
 		s = s + "\tfunction " + componentId + "_highlight(id, duration) { \n" + "\t\tif("
 				+ componentId + "_highlighterState!='highlighting'){\n"
 				+ "\t\t\tnode = document.getElementById(id);\n" + "\t\t\tif(!" + componentId
 				+ "_first){\n" + "\t\t\t" + componentId + "_first = true; \n"
 				+ "\t\t\t\tstartbc = dojo.html.getBackgroundColor(node);\n" + "\t\t\t}\n"
 				+ "\t\t\tif(" + componentId + "_highlighterState == 'unhighlighted') \n"
 				+ "\t\t\t{ \n" + "\t\t\t\t" + componentId + "_highlighterState = 'highlighting';\n"
 				+ "\t\t\t\t" + highlightInFunction + "\n" + "\t\t\t} else {\n" + "\t\t\t\t"
 				+ componentId + "_highlighterState = 'highlighting';\n" + "\t\t\t\t"
 				+ highlightOutFunction + "\n" + "\t\t\t}\n" + "\t\t}\n" + "\t}\n";
 
 
 		s = s + "\tfunction " + componentId + "_setMouseOver(ismouseover){\n"
 				+ "\t\tif (ismouseover == 1){\n" + "\t\t\t" + componentId + "_mouseover = 1;\n"
 				+ "\t\t}else{\n" + "\t\t\t" + componentId + "_mouseover = 0;\n" + "\t\t}\n"
 				+ "\t}\n" + "\t</script>\n\n";
 		container.getResponse().write(s);
 
 
 	}
 
 	/**
 	 * Simple inner class to manage RGB values.
 	 */
	private class RGB
 	{
 		private final int R;
 		private final int G;
 		private final int B;
 
 		/**
 		 * construct
 		 * 
 		 * @param R
 		 * @param G
 		 * @param B
 		 */
 		public RGB(int R, int G, int B)
 		{
 			this.R = R;
 			this.G = G;
 			this.B = B;
 
 		}
 
 		public int getR()
 		{
 			return R;
 		}
 
 		public int getG()
 		{
 			return G;
 		}
 
 		public int getB()
 		{
 			return B;
 		}
 
 		/**
 		 * @return string which dojo can use to pass RGB values to functions:
 		 *         [255,255,255]
 		 */
 		public String toString()
 		{
 			return "[" + R + ", " + G + ", " + B + "]";
 		}
 
 	}
 
 
 	/**
 	 * @see wicket.AjaxHandler#onBind()
 	 */
 	protected void onBind()
 	{
 		Component c = getComponent();
 		this.component = (Component)c;
 		this.componentId = c.getId();
 
 		// create a unique HTML for the wipe component
 		HTMLID = this.component.getId() + "_" + component.getPath();
 		// Add ID to component, and bind effect to trigger
 
 		this.component.add(new AttributeModifier("id", true, new Model(HTMLID)));
 		this.getTrigger().add(
 				new AppendAttributeModifier(getEventName(), true, new Model(componentId
 						+ "_setMouseOver(1);" + componentId + "_highlight('" + HTMLID + "', "
 						+ getDuration() + ");")));
 		this.getTrigger().add(
 				new AppendAttributeModifier("OnMouseOut", true, new Model(componentId
 						+ "_setMouseOver(0);" + componentId + "_highlight('" + HTMLID + "', "
 						+ getDuration() + ");")));
 
 	}
 
 }
