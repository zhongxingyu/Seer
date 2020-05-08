 package ch.fhnw.apsifilter.filter;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.CheckReturnValue;
 import javax.annotation.Nonnull;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Node;
 import org.jsoup.nodes.TextNode;
 
 import ch.fhnw.apsifilter.DomFilter;
 
 public final class TagWhitelistFilter implements DomFilter {
 
 	private final Set<String> whitelist;
 
 	private TagWhitelistFilter() {
 		this.whitelist = new HashSet<String>();
 	}
 
 	public void filter(Document node) {
 		filterNode(node);
 	}
 
 	private void filterNode(@Nonnull Node node) {
 		boolean deleted = false;
 		if (!whitelist.contains(getLookupName(node))) {
 			if (node.parent() != null) {
 				node.remove();
 				deleted = true;
 			}
 		}
 		if (!deleted) {
 			final List<Node> children = new ArrayList<Node>(node.childNodes());
 			for (Node child : children)
 				filterNode(child);
 		}
 	}
 
 	public void add(String... tagNames) {
 		for (String tagName : tagNames) {
 			whitelist.add(tagName);
 		}
 	}
 
 	@CheckReturnValue
 	private String getLookupName(@Nonnull Node node) {
 		if (node instanceof Element) {
 			Element sourceEl = (Element) node;
 			return sourceEl.tagName();
 		} else if (node instanceof TextNode) {
 			return "text";
 		}
 
 		return null;
 	}
 	
 	@CheckReturnValue
 	public static TagWhitelistFilter createDefault() {
 		TagWhitelistFilter filter = new TagWhitelistFilter();
 		filter.add("text", "html", "head", "body", "a", "b", "blockquote", "br",
 				"caption", "col", "colgroup", "div", "em", "h1", "h2", "h3",
 				"h4", "h5", "h6", "i", "img", "li", "ol", "ul", "p", "pre",
 				"small", "strike", "strong", "table", "tbody", "td", "tfoot",
				"th", "thead", "tr", "div", "span", "style", "link");
 
 		return filter;
 	}
 
 }
