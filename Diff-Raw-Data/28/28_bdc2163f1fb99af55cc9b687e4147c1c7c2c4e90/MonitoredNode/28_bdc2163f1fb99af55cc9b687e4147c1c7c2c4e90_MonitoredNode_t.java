 package org.kevoree.microsandbox.javase;
 
 import org.kevoree.library.defaultNodeTypes.JavaSENode;
 import org.kevoree.tools.aether.framework.JCLContextHandler;
 
 import org.kevoree.annotation.Library;
 import org.kevoree.annotation.NodeType;
 
 /**
  * Created by duke on 24/06/13.
  */
@NodeType(De)
 @Library(name = "JavaSE")
 public class MonitoredNode extends JavaSENode {
 
     @Override
     public void startNode() {
 
         JCLContextHandler jclhandler = (JCLContextHandler) getBootStrapperService().getKevoreeClassLoaderHandler();
         jclhandler.setKCLFactory(new CoverageKCLFactory());
         super.startNode();
     }
 }
