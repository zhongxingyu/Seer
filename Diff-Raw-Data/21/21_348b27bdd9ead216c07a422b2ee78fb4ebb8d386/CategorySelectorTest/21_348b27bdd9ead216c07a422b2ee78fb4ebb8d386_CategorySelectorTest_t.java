 package br.com.entropie.hellocuriosity;
 
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.not;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import br.com.entropie.hellocuriosity.news.News;
 import br.com.entropie.hellocuriosity.news.filters.CategoryFilter;
 import br.com.entropie.hellocuriosity.news.filters.NewsFilter;
 
 public class CategorySelectorTest {
 
 	private ArrayList<String> wantedCategories;
 	private ArrayList<String> notWantedCategories;
 
 	@Before
 	public void setUp() {
 
 		wantedCategories = new ArrayList<String>();
 
 		wantedCategories.add("spotlights");
 		wantedCategories.add("status report");
 
 		notWantedCategories = new ArrayList<String>();
 
 		notWantedCategories.add("Press Releases");
 
 	}
 
 	@Test
 	public void should_select_only_news_with_correct_categories() {
 
 		Date now = Calendar.getInstance().getTime();
 
		News news1 = new News("headline 1", "some text 1", "http://bit.ly",  now.toString(),
 				null, wantedCategories);
		News news2 = new News("headline 2", "some text 2","http://bit.ly", now.toString(),
 				null, wantedCategories);
		News news3 = new News("headline 3", "some text 3", "http://bit.ly",now.toString(),
 				null, new ArrayList<String>());
		News news4 = new News("headline 4", "some text 4", "http://bit.ly",now.toString(),
 				null, notWantedCategories);
 
 		ArrayList<News> newsList = new ArrayList<News>();
 
 		newsList.add(news1);
 		newsList.add(news2);
 		newsList.add(news3);
 		newsList.add(news4);
 
 		NewsFilter categoryFilter = new CategoryFilter();
 
 		List<News> filteredList = categoryFilter.filterFrom(newsList);
 
 		assertThat(filteredList, hasItem(news1));
 		assertThat(filteredList, hasItem(news2));
 		assertThat(filteredList, not(hasItem(news3)));
 		assertThat(filteredList, not(hasItem(news4)));
 	}
 }
