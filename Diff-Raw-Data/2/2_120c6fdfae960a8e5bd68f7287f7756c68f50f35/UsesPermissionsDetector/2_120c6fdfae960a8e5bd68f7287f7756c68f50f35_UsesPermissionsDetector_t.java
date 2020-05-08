 package com.github.sophiedankel.permeate.rules;
 
 import static com.android.SdkConstants.ANDROID_MANIFEST_XML;
 import static com.android.SdkConstants.ANDROID_URI;
 import static com.android.SdkConstants.ATTR_NAME;
 import static com.android.SdkConstants.TAG_USES_PERMISSION;
 
 import com.android.annotations.NonNull;
 import com.android.tools.lint.detector.api.Category;
 import com.android.tools.lint.detector.api.Context;
 import com.android.tools.lint.detector.api.Detector;
 import com.android.tools.lint.detector.api.Implementation;
 import com.android.tools.lint.detector.api.Issue;
 import com.android.tools.lint.detector.api.Scope;
 import com.android.tools.lint.detector.api.Severity;
 import com.android.tools.lint.detector.api.Speed;
 import com.android.tools.lint.detector.api.XmlContext;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 
 import java.io.File;
 import java.util.EnumSet;
 import java.util.Collection;
 import java.util.Collections;
 
 public class UsesPermissionsDetector extends Detector implements Detector.XmlScanner {
 	/** The issue detected */
 	public static final Issue ISSUE = Issue.create(
             "FindsUsedPermissions", //$NON-NLS-1$
             "Finds all permission usage",
             "Looks for all <uses-permission> attributes in AndroidManifest.xml file.",
             
             "Identifying the app's requested permission usage will help in determining which " +
             "included permissions are unnecessary.",
             
             Category.SECURITY,
             2,
             Severity.WARNING,
             new Implementation(
                    UsesPermissionsDetector.class,
                     EnumSet.of(Scope.MANIFEST)));
 	
 	@NonNull
     @Override
     public Speed getSpeed() {
         return Speed.FAST;
     }
 	
 	@Override
     public boolean appliesTo(@NonNull Context context, @NonNull File file) {
         return file.getName().equals(ANDROID_MANIFEST_XML);
     }
 	
 	@Override
     public Collection<String> getApplicableElements() {
         return Collections.singletonList(TAG_USES_PERMISSION);
     }
 	
 	@Override
     public void visitElement(@NonNull XmlContext context, @NonNull Element element) {
         Attr nameNode = element.getAttributeNodeNS(ANDROID_URI, ATTR_NAME);
         if (nameNode != null) {
             String permissionName = nameNode.getValue();
             context.report(ISSUE, element, context.getLocation(nameNode),
             		"Permission usage detected in XML file, name: " + permissionName, null);
         }
     }
 
 }
