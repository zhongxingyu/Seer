 package main.origo.admin.event;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import main.origo.admin.annotations.Admin;
 import main.origo.core.InterceptorRepository;
 import main.origo.core.Node;
 import main.origo.core.annotations.Provides;
 import main.origo.core.internal.CachedAnnotation;
 import main.origo.core.internal.ReflectionInvoker;
 import main.origo.core.ui.Element;
 import play.Logger;
 
 import java.util.List;
 import java.util.Set;
 
 public class DashboardEventGenerator {
 
     /*
     * Convenience methods for hooks with DASHBOARD_ITEM type
     */
 
     public static List<Element> triggerProvidesDashboardItemInterceptor(Node node, String withType) {
         Set<CachedAnnotation> cachedAnnotations = findProvidersWithParent(Admin.Type.DASHBOARD_ITEM, withType);
         List<Element> items = Lists.newArrayList();
         for (CachedAnnotation cachedAnnotation : cachedAnnotations) {
            items.add(ReflectionInvoker.<Element>execute(cachedAnnotation, node, withType, Maps.newHashMap()));
         }
         return items;
     }
 
     private static Set<CachedAnnotation> findProvidersWithParent(final String type, final String parent) {
         Set<CachedAnnotation> providers = InterceptorRepository.getInterceptors(Provides.class, new CachedAnnotation.InterceptorSelector() {
             @Override
             public boolean isCorrectInterceptor(CachedAnnotation cachedAnnotation) {
                 Provides annotation = (Provides) cachedAnnotation.annotation;
                 return annotation.type().equals(type) &&
                         cachedAnnotation.relationship != null && cachedAnnotation.relationship.parent().equals(parent);
             }
         });
         if (providers.isEmpty()) {
             Logger.warn("Every type (specified by using attribute 'with') must have a class annotated with @Provides to instantiate an instance. Unable to find a provider for type \'" + type + "\'");
         }
         return providers;
     }
 
 }
