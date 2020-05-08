 package fr.cg95.cvq.business.authority;
 
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 /**
  * Represents a file that can be customized on a local authority basis
  *
  * @author jsb@zenexity.fr
  *
  */
 public class LocalAuthorityResource {
     private static Map<String,String> internetMediaTypes = new HashMap<String,String>();
     static {
         internetMediaTypes.put(".png", "image/png");
         internetMediaTypes.put(".css", "text/css");
        internetMediaTypes.put(".pdf", "content/pdf");
     }
 
     public static enum Version {
         CURRENT(""),
         OLD(".old"),
         TEMP(".tmp");
         private String extension;
         private Version(String extension) { this.extension = extension; }
         public String getExtension() { return extension; }
     }
 
     public static enum Type {
         CSS("css", ".css"),
         EXTERNAL_REFERENTIAL("external_referential", ".txt"),
         HTML("html", ".html"),
         IMAGE("img", ".png"),
         DISPLAY_GROUP_IMAGE("img/display_group", ".png"),
         LOCAL_REFERENTIAL("local_referential", ".xml"),
         MAIL_TEMPLATES("html/templates/mails", ".html"),
         PDF("pdf", ".pdf"),
         REQUEST_XML("xml_request", ".xml"),
         TXT("txt", ".txt"),
         XSL("xsl", ".xsl");
         private String folder;
         private String extension;
         private Type(String folder, String extension) {
             this.folder = folder;
             this.extension = extension;
         }
         public String getFolder() { return folder; }
         public String getExtension() { return extension; }
         public String getContentType() { return internetMediaTypes.get(extension); }
     }
 
     public static final Hashtable<String, LocalAuthorityResource> localAuthorityResources = new Hashtable<String, LocalAuthorityResource>(10);
     public static final LocalAuthorityResource CSS_FO = new LocalAuthorityResource("cssFo", "cssFo", Type.CSS, false);
     public static final LocalAuthorityResource LOGO_FO = new LocalAuthorityResource("logoFo", "logoFo", Type.IMAGE, false);
     public static final LocalAuthorityResource LOGO_BO = new LocalAuthorityResource("logoBo", "logoBo", Type.IMAGE, false);
     public static final LocalAuthorityResource BANNER = new LocalAuthorityResource("banner", "banner", Type.IMAGE, false);
     public static final LocalAuthorityResource LOGO_PDF = new LocalAuthorityResource("logoPdf", "logoPdf", Type.IMAGE, false);
     public static final LocalAuthorityResource FOOTER_PDF = new LocalAuthorityResource("footerPdf", "footerPdf", Type.IMAGE, false);
     public static final LocalAuthorityResource FAQ_FO = new LocalAuthorityResource("faqFo", "faqFo", Type.PDF, true);
     public static final LocalAuthorityResource HELP_BO = new LocalAuthorityResource("helpBo", "helpBo", Type.PDF, true);
     public static final LocalAuthorityResource HELP_FO = new LocalAuthorityResource("helpFo", "helpFo", Type.PDF, true);
     public static final LocalAuthorityResource LEGAL = new LocalAuthorityResource("legal", "legal", Type.PDF, false);
     public static final LocalAuthorityResource USE = new LocalAuthorityResource("use", "use", Type.PDF, false);
     public static final LocalAuthorityResource INFORMATION_MESSAGE_FO = new LocalAuthorityResource("informationFo", "informationFo", Type.HTML, false);
     public static final LocalAuthorityResource ACCESSIBILITY_POLICY_FO = new LocalAuthorityResource("accessibilityPolicyFo", "accessibilityPolicyFo", Type.HTML, true);
 
     private String id;
     private String filename;
     private Type type;
     private boolean canFallback;
 
     private LocalAuthorityResource() {}
 
     private LocalAuthorityResource(String id, String filename, Type type, boolean canFallback) {
         this.id = id;
         this.filename = filename;
         this.type = type;
         this.canFallback = canFallback;
         localAuthorityResources.put(id, this);
     }
 
     public String getId() {
         return id;
     }
 
     public String getFilename() {
         return filename;
     }
 
     public Type getType() {
         return type;
     }
 
     public boolean canFallback() {
         return canFallback;
     }
 }
