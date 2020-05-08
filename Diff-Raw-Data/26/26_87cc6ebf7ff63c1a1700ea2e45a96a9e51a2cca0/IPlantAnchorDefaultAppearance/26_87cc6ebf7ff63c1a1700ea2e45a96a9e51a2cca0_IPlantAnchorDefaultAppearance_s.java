 /**
  * 
  */
 package org.iplantc.core.uicommons.client.widgets;
 
 import org.iplantc.core.uicommons.client.appearance.widgets.AnchorDefaultResources;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.sencha.gxt.core.client.XTemplates;
 import com.sencha.gxt.core.client.dom.XElement;
 
 /**
  * @author sriram
  *
  */
 public class IPlantAnchorDefaultAppearance implements IPlantAnchorAppearance {
 
    /**
     * @deprecated Use AnchorDefaultResources.Style directly
     * TODO replace all usages of this interface with AnchorDefaultResources.Style
     */
    @Deprecated
    public interface Style extends AnchorDefaultResources.Style {	
    }
    
     public interface Template extends XTemplates {
         @XTemplate(source = "IPlantAnchorDefaultAppearance.html")
         SafeHtml template(AnchorDefaultResources.Style style);
     }
 
     protected AnchorDefaultResources.Style style;
     protected Template template;
 
     public IPlantAnchorDefaultAppearance() {
         this.style = AnchorDefaultResources.INSTANCE.style();
         this.style.ensureInjected();
 
         this.template = GWT.create(Template.class);
     }
 
     /* (non-Javadoc)
      * @see org.iplantc.core.uicommons.client.widgets.IPlantAnchorAppearance#onMouseOver(com.sencha.gxt.core.client.dom.XElement)
      */
     @Override
     public void onMouseOver(XElement element) {
        element.selectNode("." + style.anchorText()).removeClassName(style.anchorMouseOut());
        element.selectNode("." + style.anchorText()).addClassName(style.anchorMouseOver());

     }
 
     /* (non-Javadoc)
      * @see org.iplantc.core.uicommons.client.widgets.IPlantAnchorAppearance#onMouseOut(com.sencha.gxt.core.client.dom.XElement)
      */
     @Override
     public void onMouseOut(XElement element) {
        element.selectNode("." + style.anchorText()).removeClassName(style.anchorMouseOver());
        element.selectNode("." + style.anchorText()).addClassName(style.anchorMouseOut());
     }
 
     /* (non-Javadoc)
      * @see org.iplantc.core.uicommons.client.widgets.IPlantAnchorAppearance#onUpdateText(com.sencha.gxt.core.client.dom.XElement, java.lang.String)
      */
     @Override
     public void onUpdateText(XElement parent, String text) {
         parent.selectNode("." + style.anchorText()).setInnerText(text);
 
     }
 
     @Override
     public void render(SafeHtmlBuilder sb) {
         sb.append(template.template(style));
     }
 
 }
