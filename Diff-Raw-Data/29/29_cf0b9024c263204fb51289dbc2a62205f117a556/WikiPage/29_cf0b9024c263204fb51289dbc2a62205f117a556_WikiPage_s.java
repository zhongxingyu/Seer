 package module.contents.domain;
 
 import java.io.Serializable;
 
import module.contents.domain.Page.PageBean;
 import myorg.domain.VirtualHost;
 import myorg.domain.contents.INode;
 import myorg.domain.contents.Node;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixWebFramework.util.DomainReference;
 import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;
 
 public class WikiPage extends WikiPage_Base {
 
     public static class WikiPageVersionBean implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	DomainReference<Node> parentNode;
 	DomainReference<VirtualHost> virtualHost;
 	MultiLanguageString title;
 	MultiLanguageString link;
 	MultiLanguageString content;
 
 	public WikiPageVersionBean() {
 	}
 
 	public WikiPageVersionBean(final VirtualHost virtualHost, final Node parentNode) {
 	    this();
 	    setParentNode(parentNode);
 	    setVirtualHost(virtualHost);
 	}
 
 	public MultiLanguageString getTitle() {
 	    return title;
 	}
 
 	public void setTitle(MultiLanguageString title) {
 	    this.title = title;
 	}
 
 	public MultiLanguageString getLink() {
 	    return link;
 	}
 
 	public void setLink(MultiLanguageString link) {
 	    this.link = link;
 	}
 
 	public MultiLanguageString getContent() {
 	    return content;
 	}
 
 	public void setContent(MultiLanguageString content) {
 	    this.content = content ;
 	}
 
 	public Node getParentNode() {
 	    return parentNode == null ? null : parentNode.getObject();
 	}
 
 	public void setParentNode(final Node parentNode) {
 	    this.parentNode = parentNode == null ? null : new DomainReference<Node>(parentNode);
 	}
 
 	public VirtualHost getVirtualHost() {
 	    return virtualHost == null ? null : virtualHost.getObject();
 	}
 
 	public void setVirtualHost(final VirtualHost virtualHost) {
 	    this.virtualHost = virtualHost == null ? null : new DomainReference<VirtualHost>(virtualHost);
 	}
     }
 
     public WikiPage() {
         super();
     }
 
     public WikiPage(final WikiPageVersionBean wikiPageVersionBean) {
 	this();
 	new WikiPageVersion(this, wikiPageVersionBean.getTitle(), wikiPageVersionBean.getLink(), wikiPageVersionBean.getContent());
 	new WikiPageNode(wikiPageVersionBean.getVirtualHost(), wikiPageVersionBean.getParentNode(), this, null);
     }
 
     @Service
     public static INode createNewPage(final WikiPageVersionBean wikiPageVersionBean) {
 	final WikiPage wikiPage = new WikiPage(wikiPageVersionBean);
 	return wikiPage.getWikiPageNode();
     }
 
 }
