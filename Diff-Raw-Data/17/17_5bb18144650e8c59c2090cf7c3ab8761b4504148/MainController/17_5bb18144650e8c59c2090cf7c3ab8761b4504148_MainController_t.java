 package org.sprimaudi.zkcontroller;
 
 import org.sprimaudi.zkspring.TesWire;
 import org.sprimaudi.zkspring.repository.AuditorRepository;
 import org.zkoss.zk.ui.select.SelectorComposer;
 import org.zkoss.zk.ui.select.annotation.Listen;
 import org.zkoss.zk.ui.select.annotation.VariableResolver;
 import org.zkoss.zk.ui.select.annotation.Wire;
 import org.zkoss.zk.ui.select.annotation.WireVariable;
 import org.zkoss.zkplus.spring.DelegatingVariableResolver;
 import org.zkoss.zul.Button;
 import org.zkoss.zul.Textbox;
 import org.zkoss.zul.Window;
 
 import java.util.UUID;
 
 /**
  * Created by IntelliJ IDEA.
  * User: User
  * Date: 7/15/12
  * Time: 6:32 PM
  * To change this template use File | Settings | File Templates.
  */
 @VariableResolver(DelegatingVariableResolver.class)
 public class MainController extends SelectorComposer<Window> {

 
     @Override
     public void doAfterCompose(Window comp) throws Exception {
         super.doAfterCompose(comp);    //To change body of overridden methods use File | Settings | File Templates.

     }
 
     @Listen("onClick=#btnTes")
     public void klikTes() {

     }
 
 }
