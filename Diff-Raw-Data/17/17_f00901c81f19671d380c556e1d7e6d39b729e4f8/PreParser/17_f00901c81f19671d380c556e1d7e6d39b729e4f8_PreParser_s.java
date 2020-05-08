 package gov.nih.nci.ncicb.cadsr.loader.parser;
 
 import gov.nih.nci.ncicb.cadsr.loader.event.*;
 import gov.nih.nci.ncicb.cadsr.loader.defaults.UMLDefaults;
 
 import org.apache.log4j.Logger;
 
 import gov.nih.nci.ncicb.cadsr.loader.util.PropertyAccessor;
 import gov.nih.nci.ncicb.cadsr.loader.util.StringUtil;
 import gov.nih.nci.ncicb.cadsr.loader.util.RunMode;
 
 import gov.nih.nci.ncicb.cadsr.loader.validator.*;
 
 import gov.nih.nci.ncicb.cadsr.loader.UserSelections;
 
 import gov.nih.nci.ncicb.xmiinout.handler.*;
 import gov.nih.nci.ncicb.xmiinout.domain.*;
 
 
 import java.io.*;
 
 import java.util.*;
 
 public class PreParser implements Parser {
 
   private UMLHandler listener;
 
   private ProgressListener progressListener = null;
   private String packageName = "";
 
   private Logger logger = Logger.getLogger(PreParser.class.getName());
 
   public void setEventHandler(LoaderHandler handler) 
   {
     this.listener = (UMLHandler) handler;
 //     if (progressListener != null) {
 //       listener.addProgressListener(progressListener);
 //     }
   }
 
   /**
    * main parse method.
    *
    * @param filename a <code>String</code> value
    */
   public void parse(String filename) throws ParserException 
   {
     try {
       
       long start = System.currentTimeMillis();
       
       listener.beginParsing();
         
 
       String s = filename.replaceAll("\\ ", "%20");
 
       String ext = null;
       if(filename.indexOf(".") > 0)
         ext = filename.substring(filename.lastIndexOf(".") + 1);
 
       // Some file systems use absolute URIs that do 
       // not start with '/'. 
       if(!s.startsWith("/"))
         s = "/" + s;    
       java.net.URI uri = new java.net.URI("file://" + s);
 
 
       HandlerEnum handlerEnum = null;
       if(ext != null && ext.equals("uml")) 
         handlerEnum = HandlerEnum.ArgoUMLDefault;
       else
         handlerEnum = HandlerEnum.EADefault;
 
      XmiInOutHandler handler = XmiHandlerFactory.getXmiHandler(HandlerEnum.EADefault);
       logger.debug("pre parsing ...");
       handler.load(uri);
       logger.debug("done pre parsing.");
 

       UMLModel model = handler.getModel();
 
       if(model == null) {
         throw new Exception("Can't open file. Unknown format.");
       } 
 
       // save in memory for fast-save
       UserSelections userSelections = UserSelections.getInstance();
       userSelections.getInstance().setProperty("XMI_HANDLER", handler);
       if(handlerEnum == HandlerEnum.ArgoUMLDefault) {
         userSelections.setProperty("FILE_TYPE", "ARGO");
       } else if(handlerEnum == HandlerEnum.EADefault){
         userSelections.setProperty("FILE_TYPE", "EA");
       }
 
       for(UMLPackage pkg : model.getPackages()) {
         doPackage(pkg);
       }
 
     } catch (Exception e) {
       throw new ParserException(e);
     } // end of try-catch
 
   }
 
   private void doPackage(UMLPackage pack) 
   {
     UMLDefaults defaults = UMLDefaults.getInstance();
     
     if (packageName.length() == 0) {
       packageName = pack.getName();
     }
     else {
       packageName += ("." + pack.getName());
     }
 
     listener.newPackage(new NewPackageEvent(packageName));
 
     for(UMLPackage subPkg : pack.getPackages()) {
       String oldPackage = packageName;
       doPackage(subPkg);
       packageName = oldPackage;
     }
 
     for(UMLClass clazz : pack.getClasses()) {
       doClass(clazz);
     }
 
     packageName = "";
   }
 
   private void doClass(UMLClass clazz) 
   {
     String pName = getPackageName(clazz.getPackage());
 
     String className = clazz.getName();
 
 //     if (pName != null) {
 //       className = pName + "." + className;
 //     }
 
     NewClassEvent event = new NewClassEvent(className.trim());
     event.setPackageName(pName);
 
     listener.newClass(event);
   }
 
 
 
   private String getPackageName(UMLPackage pkg) {
     StringBuffer pack = new StringBuffer();
     String s = null;
     do {
       s = null;
       if(pkg != null) {
         s = pkg.getName(); 
         if(s.indexOf(" ") == -1) {
           if(pack.length() > 0)
             pack.insert(0, '.');
           pack.insert(0, s);
         }
         pkg = pkg.getParent();
       }
     } while (s != null);
     
     return pack.toString();
   }
 
   public void addProgressListener(ProgressListener listener) 
   {
 
   }
 
   private void fireProgressEvent(ProgressEvent evt) {
     if(progressListener != null)
       progressListener.newProgressEvent(evt);
   }
 
 
 }
 
