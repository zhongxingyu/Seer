 package com.sample.osgi.cdi.activator;
 
 import com.sample.osgi.cdi.gui.SpellCheckerGui;
 import com.sample.osgi.cdi.services.SpellCheckerService;
 import java.lang.annotation.Annotation;
 import javax.enterprise.context.spi.CreationalContext;
 import javax.enterprise.inject.spi.Bean;
 import org.jboss.weld.bootstrap.WeldBootstrap;
 import org.jboss.weld.bootstrap.api.Bootstrap;
 import org.jboss.weld.bootstrap.api.Environments;
 import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
 import org.jboss.weld.context.api.BeanStore;
 import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
 import org.jboss.weld.environment.se.ShutdownManager;
 import org.jboss.weld.environment.se.Weld;
 import org.jboss.weld.environment.se.beans.InstanceManager;
 import org.jboss.weld.environment.se.beans.ParametersFactory;
 import org.jboss.weld.environment.se.discovery.SEWeldDeployment;
 import org.jboss.weld.environment.se.threading.RunnableDecorator;
 import org.jboss.weld.environment.se.util.WeldManagerUtils;
 import org.jboss.weld.manager.api.WeldManager;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 /**
  *
  * @author Mathieu ANCELIN
  */
 public class GuiActivator implements BundleActivator {
 
     private SpellCheckerGui gui;
     
     private InstanceManager instanceManager;
     private Bootstrap bootstrap;
     private BeanStore applicationBeanStore;
     private WeldManager manager;
 
     @Override
     public void start(BundleContext context) throws Exception {
        startWeld();
         //service1 = container.instance().select(Service1.class).get();
         //service2 = container.instance().select(Service2.class).get();
         gui = new SpellCheckerGui();
         gui.setSpellService((SpellCheckerService) context.getService(context.getServiceReference(SpellCheckerService.class.getName())));
         gui.start();
     }
 
     @Override
     public void stop(BundleContext context) throws Exception {
     }
 
     private void startWeld() {
         this.bootstrap = (Bootstrap) new WeldBootstrap();
 
         this.applicationBeanStore = new ConcurrentHashMapBeanStore();
         SEWeldDeployment deployment = new SEWeldDeployment() {};
         this.bootstrap.startContainer(Environments.SE, deployment, this.applicationBeanStore);
         BeanDeploymentArchive mainBeanDepArch = (BeanDeploymentArchive) deployment.getBeanDeploymentArchives().get(0);
         this.manager = this.bootstrap.getManager(mainBeanDepArch);
        this.bootstrap.startInitialization();
        this.bootstrap.deployBeans();
        ((ShutdownManager)WeldManagerUtils.getInstanceByType(this.manager, ShutdownManager.class, new Annotation[0])).setBootstrap(this.bootstrap);
        this.bootstrap.validateBeans();
        this.bootstrap.endInitialization();
         manager.createAnnotatedType(ShutdownManager.class);
         manager.createAnnotatedType(ParametersFactory.class);
         manager.createAnnotatedType(InstanceManager.class);
         manager.createAnnotatedType(Weld.class);
         manager.createAnnotatedType(RunnableDecorator.class);
         instanceManager = (InstanceManager)WeldManagerUtils.getInstanceByType(this.manager, InstanceManager.class, new Annotation[0]);
     }
 }
