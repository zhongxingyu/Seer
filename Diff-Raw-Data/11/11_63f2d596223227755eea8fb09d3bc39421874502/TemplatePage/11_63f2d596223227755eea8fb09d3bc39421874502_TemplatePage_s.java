 package fr.mirumiru.pages;
 
import org.apache.log4j.chainsaw.Main;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.repeater.RepeatingView;
 
 import com.google.common.collect.LinkedListMultimap;
 import com.google.common.collect.Multimap;
 
 import fr.mirumiru.auth.MiruSession;
 import fr.mirumiru.nav.NavigationHeader;
 
 @SuppressWarnings("serial")
 public abstract class TemplatePage extends WebPage {
 
 	public TemplatePage() {
 
 		add(new Label("title", getTitle()));
 		addNavigationMenu();
 
 	}
 
 	private void addNavigationMenu() {
 		Multimap<String, PageModel> pages = LinkedListMultimap.create();
 //		pages.put("Home", new PageModel("Home Page", HomePage.class));
 //		pages.put("Quizz", new PageModel("Scoreboard", QuizzScorePage.class));
 //		pages.put("Quizz",
 //				new PageModel("Merge Requests", QuizzMergePage.class));
 //		pages.put("Debug", new PageModel("View Logs", LogViewerPage.class));
 
 		RepeatingView repeatingView = new RepeatingView("nav-headers");
 
 		for (String category : pages.keySet()) {
 			repeatingView.add(new NavigationHeader(repeatingView.newChildId(),
 					category, pages.get(category)));
 		}
 		add(repeatingView);
 
 	}
 
 	protected abstract String getTitle();
 
 //	protected <T> T getBean(Class<? extends T> klass) {
 //		return Main.getInstance().select(klass).get();
 //	}
 
 	public class PageModel {
 		private String name;
 		private Class<? extends TemplatePage> pageClass;
 
 		public PageModel(String name, Class<? extends TemplatePage> pageClass) {
 			super();
 			this.name = name;
 			this.pageClass = pageClass;
 		}
 
 		public Class<? extends TemplatePage> getPageClass() {
 			return pageClass;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 	}
 
 	public MiruSession getAuthSession() {
 		return (MiruSession) super.getSession();
 	}
 }
