 package de.hswt.hrm.contact.ui.view;
 
 import java.net.URL;
 
 import javax.annotation.PostConstruct;
 
 import org.eclipse.e4.ui.di.Focus;
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.swt.widgets.Composite;
 
 public class ContactView {
    // @Inject
    // public ContactView() {
    //
    // }
 
     @PostConstruct
     public void postConstruct(Composite parent) {
 
         String location = "de/hswt/hrm/contact/ui/xwt/" + ContactView.class.getSimpleName()
                 + IConstants.XWT_EXTENSION_SUFFIX;
 
         URL url = ContactView.class.getClassLoader().getResource(location);
         try {
            XWT.load(parent, url);
         }
         catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
     }
 
     @Focus
     public void onFocus() {
 
     }
 
 }
