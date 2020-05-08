 package de.hswt.hrm.contact.ui.view;
 
 import java.net.URL;
 
 import javax.annotation.PostConstruct;
import javax.inject.Inject;
 
 import org.eclipse.e4.ui.di.Focus;
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
import org.eclipse.swt.widgets.Composite;
 
 public class ContactView {
     @Inject
     public ContactView() {
         // TODO Your code here
     }
 
     @PostConstruct
     public void postConstruct(Composite parent) {
 
        String name = ContactView.class.getSimpleName() + IConstants.XWT_EXTENSION_SUFFIX;
 
        URL u = ContactView.class.getClassLoader().getResource(name);
         try {
             XWT.load(parent, u);
         }
         catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
     }
 
     @Focus
     public void onFocus() {
         // TODO Your code here
     }
 
 }
