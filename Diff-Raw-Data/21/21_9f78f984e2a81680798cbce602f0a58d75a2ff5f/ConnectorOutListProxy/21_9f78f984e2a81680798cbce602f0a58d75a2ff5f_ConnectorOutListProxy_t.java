 /**
  * 
  */
 package org.freeplane.plugin.script.proxy;
 
 import java.util.AbstractCollection;
import java.util.ArrayList;
 import java.util.Iterator;
 
import org.freeplane.features.common.link.LinkModel;
 import org.freeplane.features.common.link.NodeLinks;
 import org.freeplane.features.common.map.NodeModel;
 import org.freeplane.plugin.script.ScriptContext;
 
 class ConnectorOutListProxy extends AbstractCollection<Proxy.Connector> {
 	private final NodeModel node;
 	private final ScriptContext scriptContext;
 
 	public ConnectorOutListProxy(final NodeProxy nodeProxy) {
 		this.node = nodeProxy.getDelegate();
 		this.scriptContext = nodeProxy.getScriptContext();
 	}
 
 	@Override
 	public Iterator<Proxy.Connector> iterator() {
		return new ConnectorIterator(new ArrayList<LinkModel>(NodeLinks.getLinks(node)).iterator(), scriptContext);
 	}
 
 	@Override
 	public int size() {
 		return NodeLinks.getLinks(node).size();
 	}
 }
