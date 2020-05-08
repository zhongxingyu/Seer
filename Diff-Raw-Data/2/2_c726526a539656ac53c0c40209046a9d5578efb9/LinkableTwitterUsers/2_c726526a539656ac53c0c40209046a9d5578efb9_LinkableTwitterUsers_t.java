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
 
 import static ac.simons.utils.StringUtils.isBlank;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Node;
 import org.jsoup.nodes.TextNode;
 import org.jsoup.parser.Tag;
 
 /**
  * @author Michael J. Simons
  */
 public class LinkableTwitterUsers implements Linkable {
	public static final Pattern atTwitterUser = Pattern.compile("(?im)(^|[^a-z0-9_])" + LinkableEmailaddresses.atSigns + "([a-z0-9_]{1,20})(?=(\\s|$))");
 	
 	@Override
 	public boolean linkTo(List<Node> changedNodes, TextNode node, String baseUri) {
 		boolean changed = false;
 
 		int start = 0;
 		final String nodeText = node.getWholeText();
 		final Matcher matcher = atTwitterUser.matcher(nodeText);
 		
 		while(matcher.find()) {
 			// Add a new textnode for everything before the url
 			final String textBefore = String.format("%s%s", nodeText.substring(start, matcher.start()), matcher.group(1));
 			if(!isBlank(textBefore))
 				changedNodes.add(new TextNode(textBefore, baseUri));
 			final Element newAnchor = new Element(Tag.valueOf("a"), baseUri);					
 			newAnchor.attr("href", String.format("http://twitter.com/%s", matcher.group(2)));
 			newAnchor.appendChild(new TextNode(String.format("@%s", matcher.group(2)), baseUri));
 			changedNodes.add(newAnchor);
 			start = matcher.end();
 			changed = true;				
 		}
 		
 		// Add a new textnode for everything after
 		final String textAfter = nodeText.substring(start);
 		if(!isBlank(textAfter))
 			changedNodes.add(new TextNode(textAfter, baseUri));				
 		return changed;
 	}
 }
