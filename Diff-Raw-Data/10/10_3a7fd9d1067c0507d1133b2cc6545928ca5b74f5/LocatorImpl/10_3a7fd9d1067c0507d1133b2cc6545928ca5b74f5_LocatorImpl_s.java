 package org.xbrlapi.impl;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.w3c.dom.Element;
 import org.xbrlapi.Fragment;
 import java.util.List;
 import org.xbrlapi.Locator;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 
 public class LocatorImpl extends ArcEndImpl implements Locator {
 	
 	/**
 	 * @see org.xbrlapi.Locator#setTarget(URI)
 	 */
 	public void setTarget(URI uri) throws XBRLException {
 		setMetaAttribute("absoluteHref",uri.toString());
 		setMetaAttribute("targetDocumentURI",this.getTargetDocumentURI(uri).toString());
 		setMetaAttribute("targetPointerValue",this.getTargetPointerValue(uri.getFragment()));
 	}
 
     /**
      * Get the raw xlink:href attribute value (before any resolution).
      * @return the value of the xlink:href attribute on the locator.
      * @throws XBRLException if the attribute is missing.
      * @see org.xbrlapi.Locator#getHref()
      */
     public String getHref() throws XBRLException {
     	Element root = getDataRootElement();
     	if (! root.hasAttributeNS(Constants.XLinkNamespace,"href")) throw new XBRLException("Locators must have xlink:href attributes.");
     	return root.getAttributeNS(Constants.XLinkNamespace,"href");
     }
     
 
     
     /**
      * Get the absolute value of the HREF to the metadata.
      * @return The absolute URI specified by the locator HREF attribute.
      * @throws XBRLException.
      * @see org.xbrlapi.Locator#getAbsoluteHref()
      */
     public URI getAbsoluteHref() throws XBRLException {
     	try {
     		return new URI(this.getMetadataRootElement().getAttribute("absoluteHref"));
     	} catch (URISyntaxException e) {
     		throw new XBRLException("Absolute URI in the HREF of the locator is malformed.",e);
     	}
     }
     
     /**
      * @see org.xbrlapi.Locator#getTargetDocumentURI()
      */
     public URI getTargetDocumentURI() throws XBRLException {
     	try {
     	    URI originalURI = new URI(this.getMetadataRootElement().getAttribute("targetDocumentURI"));
     		return this.getStore().getMatcher().getMatch(originalURI);
     	} catch (URISyntaxException e) {
     		throw new XBRLException("Absolute URI in the HREF of the locator is malformed.",e);
     	}
     }
     
     /**
      * @see org.xbrlapi.Locator#getTargetPointerValue()
      */
     public String getTargetPointerValue() throws XBRLException {
     	return this.getMetadataRootElement().getAttribute("targetPointerValue");
     }
     
     /**
      * Get the single fragment that this locator references.
      * @return the single fragment referenced by the locator.
      * @throws XBRLException if the locator does not reference exactly one fragment.
      * @see org.xbrlapi.Locator#getTarget()
      */
     public Fragment getTarget() throws XBRLException {
 
     	String pointerCondition = "";
     	String pointerValue = getTargetPointerValue();
     	if (! pointerValue.equals("")) 
     		pointerCondition = " and "+ Constants.XBRLAPIPrefix+ ":" + "xptr/@value='" + pointerValue + "'";
 
    	String query = "/*[@uri='" + getTargetDocumentURI() + "'" + pointerCondition + "]";
     	List<Fragment> fragments = getStore().<Fragment>query(query);
     	if (fragments.size() == 0) throw new XBRLException("The simple link does not reference a fragment.");
     	if (fragments.size() > 1) throw new XBRLException("The simple link references more than one fragment.");
     	return fragments.get(0);
     }
     
 }
