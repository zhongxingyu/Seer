 package org.zkoss.zats.core;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import org.zkoss.zats.core.component.ComponentNode;
 import org.zkoss.zats.core.component.DesktopNode;
 import org.zkoss.zats.core.component.Node;
 import org.zkoss.zats.core.component.PageNode;
 import org.zkoss.zats.core.component.impl.DefaultComponentNode;
 import org.zkoss.zats.core.component.impl.DefaultPageNode;
 import org.zkoss.zats.core.impl.select.Selectors;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.Page;
 
 /**
  * A tool for searching components.
  * @author pao
  */
 public class Searcher
 {
 	private static Logger logger = Logger.getLogger(Searcher.class.getName());
 
 	/**
 	 * find components matched specify selector.
 	 * notice: if refer the Desktop, it will only find first page.
 	 * @param root the reference node for selector (it could be Desktop or Page).
 	 * @param selector the selector string.
 	 * @return a list contained matched components.
 	 */
 	@SuppressWarnings("unchecked")
 	public static List<ComponentNode> findAll(Node root, String selector)
 	{
 		try
 		{
 			// detect zk version to choose implementation
 			Class<?> selectors;
 			try
 			{

 				selectors = Class.forName("org.zkoss.zk.ui.select.Selectors");
				logger.info("using zk6 selectors");
 			}
 			catch(ClassNotFoundException e)
 			{
 				selectors = Selectors.class;
				logger.info("using zats selectors");
 			}
 			Method findByPage = selectors.getMethod("find", Page.class, String.class);
 
 			// find components
 			PageNode pageNode;
 			List<Component> list;
 			if(root instanceof DesktopNode)
 			{
 				DesktopNode desktopNode = (DesktopNode)root;
 				pageNode = new DefaultPageNode(desktopNode, desktopNode.cast().getFirstPage());
 				list = (List<Component>)findByPage.invoke(null, pageNode.cast(), selector);
 			}
 			else if(root instanceof PageNode)
 			{
 				pageNode = (PageNode)root;
 				list = (List<Component>)findByPage.invoke(null, pageNode.cast(), selector);
 			}
 			else
 			{
 				ComponentNode compNode = (ComponentNode)root;
 				pageNode = compNode.getPage();
 				Method findByComp = selectors.getMethod("find", Component.class, String.class);
 				list = (List<Component>)findByComp.invoke(null, compNode.cast(Component.class), selector);
 			}
 			List<ComponentNode> nodes = new ArrayList<ComponentNode>(list.size());
 			for(Component comp : list)
 				nodes.add(new DefaultComponentNode(pageNode, comp));
 			return nodes;
 		}
 		catch(Exception e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * find first component matched specify selector.
 	 * notice: if refer the Desktop, it will only find first page.
 	 * @param root the reference node for selector (it could be Desktop or Page).
 	 * @param selector the selector string.
 	 * @return matched component or null if not found.
 	 */
 	public static ComponentNode find(Node root, String selector)
 	{
 		List<ComponentNode> nodes = findAll(root, selector);
 		return nodes.size() > 0 ? nodes.get(0) : null;
 	}
 
 	public static ComponentNode find(String selector)
 	{
 		return Searcher.find(Conversations.getDesktop(), selector);
 	}
 }
