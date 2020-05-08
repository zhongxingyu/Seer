 package greenhouse.ui.wicket.page;
 
 import greenhouse.project.Context;
 import greenhouse.project.Project;
 import greenhouse.project.ProjectRepository;
 import greenhouse.ui.wicket.WicketUtils;
 import greenhouse.ui.wicket.page.create.CreatePage;
 import greenhouse.ui.wicket.page.features.FeaturesPage;
 import greenhouse.ui.wicket.page.history.HistoryPage;
 import greenhouse.ui.wicket.page.settings.SettingsPage;
 import greenhouse.ui.wicket.page.tags.TagsPage;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.request.http.WebRequest;
 import org.apache.wicket.request.http.WebResponse;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.springframework.util.StringUtils;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 
 /**
  * Provides common layout for all Project-specific pages.
  */
 public abstract class BaseProjectPage extends GreenhousePage {
 
     @SuppressWarnings("unchecked")
     public BaseProjectPage(PageParameters params) {
         super(params);
         WebMarkupContainer base = new WebMarkupContainer("base");
         Project project = project();
         final String projectKey = getProjectKey();
        Link<Void> projectLink = new BookmarkablePageLink<Void>("project", ProjectsPage.class, WicketUtils.indexed(projectKey));
         base.add(projectLink.add(new Label("name", project.getName())));
 
         base.add(new ListView<Class<? extends GreenhousePage>>("pages", Arrays.asList(FeaturesPage.class, TagsPage.class, CreatePage.class, HistoryPage.class,
                 SettingsPage.class)) {
             @Override
             protected void populateItem(ListItem<Class<? extends GreenhousePage>> item) {
                 Class<? extends GreenhousePage> clazz = item.getModelObject();
                 if (BaseProjectPage.this.getClass().equals(clazz)) {
                     item.add(AttributeModifier.replace("class", "active"));
                 }
                 String simpleName = simpleName(clazz);
                 BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("link", clazz, new PageParameters().add("project", projectKey));
                 link.add(new Label("name", StringUtils.capitalize(simpleName)));
                 item.add(link);
             }
         });
         base.add(new Label("currentContext", getCurrentContext(project()).getName()));
 
         base.add(new ListView<Context>("contexts", new ContextsModel(repo, projectKey)) {
             @Override
             protected void populateItem(ListItem<Context> item) {
                 Context context = item.getModelObject();
                 final String contextKey = context.getKey();
                 Link<Void> link = new Link<Void>("link") {
                     @Override
                     public void onClick() {
                         WicketUtils.setContextKey(getWebRequest(), (WebResponse) getResponse(), repo.getProject(projectKey), contextKey);
                         setResponsePage(BaseProjectPage.this.getClass(), new PageParameters().add("project", projectKey));
                     }
                 };
                 item.add(link.add(new Label("name", context.getName())));
             }
         });
         add(base);
 
         get("home").setVisible(false);
     }
 
     private Context getCurrentContext(Project project) {
         String contextKey = WicketUtils.getContextKey((WebRequest) getRequest(), project);
         ImmutableMap<String, Context> contexts = project.getContexts();
         Context context = contexts.get(contextKey);
         if (contextKey == null) {
             context = contexts.values().iterator().next();
             WicketUtils.setContextKey((WebRequest) getRequest(), (WebResponse) getResponse(), project, context.getKey());
         }
         return context;
     }
 
     public static String simpleName(Class<? extends GreenhousePage> clazz) {
         String simpleName = clazz.getSimpleName();
         return simpleName.substring(0, simpleName.indexOf("Page")).toLowerCase(Locale.ENGLISH);
     }
 
     private static class ContextsModel extends LoadableDetachableModel<List<Context>> {
         private final ProjectRepository repo;
         private final String projectKey;
 
         private ContextsModel(ProjectRepository repo, String projectKey) {
             this.repo = repo;
             this.projectKey = projectKey;
         }
 
         @Override
         protected List<Context> load() {
             Project project = repo.getProject(projectKey);
             List<Context> keys = Lists.newArrayList(project.getContexts().values());
             Collections.sort(keys, new Comparator<Context>() {
                 @Override
                 public int compare(Context first, Context second) {
                     return first.getName().compareTo(second.getName());
                 }
             });
             return keys;
         }
     }
 
 }
