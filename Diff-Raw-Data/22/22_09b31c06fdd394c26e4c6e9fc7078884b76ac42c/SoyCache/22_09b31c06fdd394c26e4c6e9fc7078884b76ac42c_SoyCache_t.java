 package net.arunjitsingh.gsgae;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.template.soy.SoyFileSet;
 import com.google.template.soy.SoyModule;
 import com.google.template.soy.tofu.SoyTofu;
 
 import java.util.List;
 
 import javax.annotation.Nullable;
 
 public class SoyCache {
  private static final String PREFIX = "/net/arunjitsingh/gsgae/templates/";
   private static final List<String> SOY_FILES = Lists.newArrayList(
       "home.soy",
       "query.soy");
 
   private static SoyCache cache = new SoyCache(PREFIX, SOY_FILES);
 
   private ImmutableList<String> soyFiles;
   private SoyTofu tofu;
   private String prefix;
 
   protected SoyCache(@Nullable String prefix, List<String> files) {
     soyFiles = ImmutableList.copyOf(Preconditions.checkNotNull(files));
     this.prefix = Strings.nullToEmpty(prefix);
     createTofu();
   }
 
   protected void createTofu() {
     // Create a Guice injector that contains the SoyModule and use it get a
     // SoyFileSet.Builder.
     Injector injector = Guice.createInjector(new SoyModule());
     SoyFileSet.Builder builder = injector.getInstance(SoyFileSet.Builder.class);
     for (String name : soyFiles) {
       name = prefix + name;
       builder = builder.add(Preconditions.checkNotNull(this.getClass().getResource(name)));
     }
     SoyFileSet sfs = builder.build();
     tofu = sfs.compileToTofu();
   }
 
   public SoyTofu getTofu() {
     return tofu;
   }
 
   public static SoyCache getCache() {
     return cache;
   }
 }
