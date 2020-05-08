 /**
  * Created by Michael Simons, michael-simons.eu
  * and released under The BSD License
  * http://www.opensource.org/licenses/bsd-license.php
  *
  * Copyright (c) 2010, Michael Simons
  * All rights reserved.
  *
  * Redistribution  and  use  in  source   and  binary  forms,  with  or   without
  * modification, are permitted provided that the following conditions are met:
  *
  * * Redistributions of source   code must retain   the above copyright   notice,
  *   this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary  form must reproduce  the above copyright  notice,
  *   this list of conditions  and the following  disclaimer in the  documentation
  *   and/or other materials provided with the distribution.
  *
  * * Neither the name  of  michael-simons.eu   nor the names  of its contributors
  *   may be used  to endorse   or promote  products derived  from  this  software
  *   without specific prior written permission.
  *
  * THIS SOFTWARE IS  PROVIDED BY THE  COPYRIGHT HOLDERS AND  CONTRIBUTORS "AS IS"
  * AND ANY  EXPRESS OR  IMPLIED WARRANTIES,  INCLUDING, BUT  NOT LIMITED  TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL  THE COPYRIGHT HOLDER OR CONTRIBUTORS  BE LIABLE
  * FOR ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL,  EXEMPLARY, OR  CONSEQUENTIAL
  * DAMAGES (INCLUDING,  BUT NOT  LIMITED TO,  PROCUREMENT OF  SUBSTITUTE GOODS OR
  * SERVICES; LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT  LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE  USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package ac.simons.autolinker;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Node;
 import org.jsoup.nodes.TextNode;
 
 import ac.simons.utils.StringUtils;
 
 /**
  * @author Michael J. Simons
  */
 public class AutoLinker {
 	private Map<Integer, Linkable> linkables = new TreeMap<Integer, Linkable>();
 
 	public Map<Integer, Linkable> getLinkables() {
 		return linkables;
 	}
 
 	public void setLinkables(Map<Integer, Linkable> linkables) {
 		this.linkables = linkables;
 	}
 
 	public Document autoLink(final String in) {		
 		return this.autoLink(in, null);
 	}
 	
 	public Document autoLink(final String in, final URL baseUrl) {
 		Document rv = null;
 		if(!StringUtils.isBlank(in))
 			rv = autoLink(Jsoup.parseBodyFragment(in, baseUrl == null ? "" : baseUrl.toString())); 		
 		return rv;
 	}
 	
 	public Document autoLink(final Document in) {
 		Document rv = in;
 		if(rv != null) {						
 			for(Element _element: rv.children()) {	
 				// Ensure that the linkables are sorted
 				TreeMap<Integer, Linkable> _linkables = (TreeMap<Integer, Linkable>) (linkables instanceof TreeMap ? linkables : new TreeMap<Integer, Linkable>(linkables));
 				for(Linkable linkable : _linkables.values()) { 
 					autoLinkHelper(rv.baseUri(), _element, linkable);						
 				}				
 			}
 		}
 		return rv;
 	}
 	
	private Element autoLinkHelper(final String baseUri, final Element element, final Linkable linkable) {
 		final List<Node> newAnchors = new ArrayList<Node>();		
 		if(element.tagName().equals("a")) 
			return null; 	
 		boolean changed = false;
 		for(Node childNode : element.childNodes()) {
 			// Child node is itself an element with children
 			if(childNode instanceof Element) {
 				// These are elements directly unter body and must be stored if a child node of the body is changed
 				// They will be skipped in the next call if their anchors
 				if(element.tagName().equals("body"))
 					newAnchors.add(childNode);				
				final Element changedElement = autoLinkHelper(baseUri, (Element) childNode, linkable);
				if(changedElement != null) {					
					newAnchors.remove(childNode);					
					newAnchors.add(changedElement);
				}
 			}
 			// Only TextNodes may have possible urls
 			else if(childNode instanceof TextNode) {
 				final TextNode node = (TextNode) childNode;				
 				changed |= linkable.linkTo(newAnchors, node, baseUri);
 			// Other nodes are just kept
 			} else {
 				newAnchors.add(childNode);
 			}
 		}
		Element rv = element;
 		// Rebuild the parent element
 		if(changed) {			
 			final Element newElement = new Element(element.tag(), baseUri, element.attributes());
 			for(Node node : newAnchors)
 				newElement.appendChild(node);
 			element.replaceWith(newElement);
			rv = newElement;
 		}	
		return rv;
 	}
 }
