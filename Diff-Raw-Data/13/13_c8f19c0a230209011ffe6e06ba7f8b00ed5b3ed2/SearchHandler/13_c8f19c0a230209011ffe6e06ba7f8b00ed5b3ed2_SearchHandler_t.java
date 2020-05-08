 package net.cyklotron.cms.search.searching;
 
 import org.objectledge.context.Context;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableState;
 import org.objectledge.web.mvc.tools.LinkTool;
 
 /**
 * An interface for different search handler implementations. For instance: - external search
 * handler - internal search handler
 * 
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: SearchHandler.java,v 1.3 2005-01-20 06:52:40 pablo Exp $
  */
 public interface SearchHandler<T extends SearchHit>
 {
     public TableModel<T> search(CoralSession coralSession, Resource[] searchPools,
         SearchMethod method, TableState state, Parameters parameters, I18nContext i18nContext)
         throws SearchingException;

    public void resolveUrls(T hit, Subject subject, Context context, boolean generateEditLink,
        LinkTool link);
 }
