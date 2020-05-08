 package com.clouway.cuse;
 
 import com.clouway.cuse.spi.*;
 import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;
 import com.google.appengine.tools.development.testing.LocalSearchServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import org.jmock.Expectations;
 import org.jmock.integration.junit4.JUnitRuleMockery;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 
 import java.util.List;
 
 import static com.clouway.cuse.spi.SearchFilters.isAnyOf;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 /**
  * @author Mihail Lesikov (mlesikov@gmail.com)
  */
 public abstract class SearchEngineContractTest {
 
   @Rule
   public final JUnitRuleMockery context = new JUnitRuleMockery();
 
   private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalSearchServiceTestConfig());
 
   private SearchEngine searchEngine;
 
   private InMemoryRepository repository;
 
   private EntityLoader entityLoader = context.mock(EntityLoader.class);
 
   private IndexingStrategyCatalog indexingStrategyCatalog;
 
   private IndexRegister indexRegister = context.mock(IndexRegister.class);
 
   private MatchedIdObjectFinder objectIdFinder = context.mock(MatchedIdObjectFinder.class);
 
   protected abstract SearchEngine createSearchEngine();
 
   protected abstract InMemoryRepository createInMemoryRepository();
 
   @Before
   public void setUp() {
 
     helper.setUp();
 
     indexingStrategyCatalog = new InMemoryIndexingStrategyCatalog();
 
     repository = createInMemoryRepository();
     searchEngine = createSearchEngine();
   }
 
   @After
   public void tearDown() {
     helper.tearDown();
   }
 
   @Test
   public void searchMatchingFieldValue() {
 
     store(new User(1l, "Jack"));
 
     List<User> dogs = searchEngine.search(User.class).where("name", SearchFilters.is("Jack")).returnAll().now();
 
     assertThat(dogs.size(), is(equalTo(1)));
     assertThat(dogs.get(0).name, is(equalTo("Jack")));
   }
 
   @Test
   public void noMatchingFieldValue() {
 
     store(new User(1l, "Jack"));
 
     List<User> result = searchEngine.search(User.class).where("name", SearchFilters.is("Jim")).returnAll().now();
 
     assertThat(result.size(), is(0));
   }
 
   @Test(expected = EmptySearchFilterException.class)
   public void searchByEmptyFieldValue() {
 
     store(new User(1l, "Jack"));
 
     List<User> result = searchEngine.search(User.class).where("name", SearchFilters.is("")).returnAll().now();
 
     assertThat(result.size(), is(0));
   }
 
   @Test(expected = EmptySearchFilterException.class)
   public void searchByFieldValueContainingOnlyWhiteSpaces() {
 
     store(new User(1l, "Jack"));
 
     List<User> result = searchEngine.search(User.class).where("name", SearchFilters.is("   ")).returnAll().now();
 
     assertThat(result.size(), is(0));
   }
 
   @Test
   public void searchByNotExistingFieldValue() {
 
     store(new User(1l, "Jack"));
 
     List<User> result = searchEngine.search(User.class).where("age", SearchFilters.is("12")).returnAll().now();
 
     assertThat(result.size(), is(0));
   }
 
   @Test
   public void matchingAnyOfTheGivenValues() {
 
     store(new User(1l, "Jack"), new User(2l, "Jim"));
 
     List<User> result = searchEngine.search(User.class).where("name", isAnyOf("Jack", "Jim")).returnAll().now();
 
     assertThat(result.size(), is(2));
     assertThat(result.get(0).id, is(1l));
     assertThat(result.get(1).id, is(2l));
   }
 
   @Test
   public void searchByMatchingOneValueThatMatchesAny() {
 
     store(new User(1l, "Jack"), new User(2l, "Jim"));
 
     List<User> result = searchEngine.search(User.class).where("name", isAnyOf("Jim")).returnAll().now();
 
     assertThat(result.size(), is(1));
     assertThat(result.get(0).id, is(2l));
   }
 
   @Test
   public void matchingValuesForNonExistingField() {
 
     store(new User(1l, "Jack"));
 
     List<User> result = searchEngine.search(User.class).where("age", isAnyOf("12", "14")).returnAll().now();
 
     assertThat(result.size(), is(0));
   }
 
   @Test
   public void searchForOneMatchByGivenQuery() {
 
     store(new User(1l, "Jack Smith"));
 
     List<User> result = searchEngine.search(User.class).where("Jack Smith").returnAll().now();
 
     assertThat(result.size(), is(1));
     assertThat(result.get(0).id, is(1l));
   }
 
   @Test
   public void searchForManyMatchesByGivenQuery() {
 
     store(new User(1l, "Jack Smith"), new User(2l, "Johny Smith"));
 
     List<User> result = searchEngine.search(User.class).where("Smith").returnAll().now();
 
     assertThat(result.size(), is(2));
     assertThat(result.get(0).id, is(1l));
     assertThat(result.get(1).id, is(2l));
   }
 
   @Test(expected = EmptySearchQueryException.class)
   public void searchByEmptyQuery() {
 
     store(new User(1l, "Jack"));
 
     List<User> result = searchEngine.search(User.class).where("").returnAll().now();
 
     assertThat(result.size(), is(0));
   }
 
   @Test
   public void searchInGivenIndex() {
 
     repository.store(1l, new User(1l));
     searchEngine.register(new Employee(1l, "John"));
 
     List<User> result = searchEngine.search(User.class).inIndex(Employee.class)
                                                        .where("firstName", SearchFilters.is("John"))
                                                        .returnAll().now();
 
     assertThat(result.size(), is(1));
     assertThat(result.get(0).id, is(1l));
   }
 
   @Test
   public void searchByMatchingTwoFieldValues() {
 
     store(new Employee(1l, "John", "Adams"));
 
     List<Employee> result = searchEngine.search(Employee.class).where("firstName", SearchFilters.is("John"))
                                                                .where("lastName", SearchFilters.is("Adams"))
                                                                .returnAll().now();
 
     assertThat(result.size(), is(1));
     assertThat(result.get(0).firstName, is("John"));
     assertThat(result.get(0).lastName, is("Adams"));
   }
 
   @Test
   public void limitSearchByPositiveValue() {
 
     store(new Employee(1l, "Jack Smith"), new Employee(2l, "Jack Samuel"), new Employee(3l, "Jack Jameson"));
 
     List<Employee> result = searchEngine.search(Employee.class).where("firstName", SearchFilters.is("Jack"))
                                                                .fetchMaximum(2)
                                                                .now();
 
     assertThat(result.size(), is(2));
     assertThat(result.get(0).id, is(1l));
     assertThat(result.get(1).id, is(2l));
   }
 
   @Test
   public void limitingSearchResultByZero() {
 
     store(new User(1l, "Jack"), new User(2l, "Jack Adams"));
 
     List<User> result = searchEngine.search(User.class).where("name", SearchFilters.is("Jack")).fetchMaximum(0).now();
 
     assertThat(result.size(), is(2));
     assertThat(result.get(0).id, is(1l));
     assertThat(result.get(1).id, is(2l));
   }
 
   @Test(expected = SearchLimitExceededException.class)
   public void exceededSearchLimit() {
 
     store(new User(1l, "John"));
 
     List<User> result = searchEngine.search(User.class).where("name", SearchFilters.is("John"))
                                                        .fetchMaximum(1001)
                                                        .now();
 
     assertThat(result.size(), is(0));
   }
 
   @Test(expected = NegativeSearchLimitException.class)
   public void negativeSearchLimit() {
 
     store(new User(1l, "John"));
 
     List<User> result = searchEngine.search(User.class).where("name", SearchFilters.is("John")).fetchMaximum(-1).now();
 
     assertThat(result.size(), is(0));
   }
 
   @Test(expected = MissingSearchFiltersException.class)
   public void searchWithoutSpecifyingFiltersMatcher() {
 
     store(new User(1l, "John"));
 
     List<User> result = searchEngine.search(User.class).returnAll().now();
 
     assertThat(result.size(), is(0));
   }
 
   @Test(expected = NotConfiguredIndexingStrategyException.class)
   public void notConfiguredIndexingStrategy() {
 
     searchEngine = new SearchEngineImpl(repository, new IndexingStrategyCatalog() {
       @Override
       public IndexingStrategy get(Class aClass) {
         return null;
       }
     }, null,null, objectIdFinder);
 
     store(new User(1l, "John"));
 
     List<User> result = searchEngine.search(User.class).where("name", SearchFilters.is("John")).returnAll().now();
 
     assertThat(result.size(), is(0));
   }
 
   @Test
   public void noMatchingResultsFromFullTextSearch() {
 
     store(new User(1l, "John Adams"));
 
     List<User> result = searchEngine.search(User.class).where("Jack").returnAll().now();
 
     assertThat(result.size(), is(0));
   }
 
   @Test
   public void matchingResultsFromFullTextSearch() {
 
     store(new User(1l, "John Adams"), new User(2l, "John Parker"));
 
     List<User> result = searchEngine.search(User.class).where("Jo").returnAll().now();
 
     assertThat(result.size(), is(2));
     assertThat(result.get(0).name, is("John Adams"));
     assertThat(result.get(1).name, is("John Parker"));
   }
 
   @Test
   public void searchReturnsObjectIdsAsString() {
 
     context.checking(new Expectations() {{
       never(entityLoader);
     }});
 
     store(new User(1l, "John Adams"), new User(2l, "John Parker"));
 
     List<String> result = searchEngine.searchIds(String.class).inIndex(User.class)
                                                               .where("name", SearchFilters.is("John"))
                                                               .returnAll()
                                                               .now();
 
     assertThat(result.size(), is(2));
     assertThat(result.get(0), is("1"));
     assertThat(result.get(1), is("2"));
   }
 
   @Test
   public void searchReturnsObjectIdsAsLong() {
 
     context.checking(new Expectations() {{
       never(entityLoader);
     }});
 
     store(new User(1l, "John Adams"), new User(2l, "John Parker"));
 
     List<Long> result = searchEngine.searchIds(Long.class).inIndex(User.class)
                                                           .where("name", SearchFilters.is("John"))
                                                           .returnAll()
                                                           .now();
 
     assertThat(result.size(), is(2));
     assertThat(result.get(0), is(1l));
     assertThat(result.get(1), is(2l));
   }
 
   @Test(expected = NotConfiguredIdConvertorException.class)
   public void notConfiguredIdConvertor() {
 
     searchEngine = new SearchEngineImpl(entityLoader, indexingStrategyCatalog, new IdConvertorCatalog() {
 
       public IdConvertor getConvertor(Class aClass) {
         return null;
       }
     }, indexRegister, objectIdFinder);
 
     context.checking(new Expectations() {{
       oneOf(indexRegister).register(with(any(Object.class)), with(any(IndexingStrategy.class)));
       never(objectIdFinder);
       never(entityLoader);
     }});
 
     store(new User(1l, "John Adams"));
 
     List<Long> result = searchEngine.searchIds(Long.class).inIndex(User.class)
                                                           .where("name", SearchFilters.is("John"))
                                                           .returnAll()
                                                           .now();
 
     assertThat(result.size(), is(0));
   }
 
   @Test(expected = NotConfiguredIdConvertorException.class)
   public void missingIdConvertor() {
 
     searchEngine = new SearchEngineImpl(entityLoader, indexingStrategyCatalog, new IdConvertorCatalog() {
       @Override
       public IdConvertor getConvertor(Class aClass) {
 
         if (aClass.equals(String.class)) {
           return new StringIdConvertor();
         }
 
         return null;
       }
     }, indexRegister, objectIdFinder);
 
     context.checking(new Expectations() {{
       oneOf(indexRegister).register(with(any(Object.class)), with(any(IndexingStrategy.class)));
       never(objectIdFinder);
       never(entityLoader);
     }});
 
     store(new User(1l, "John"));
 
     List<Long> result = searchEngine.searchIds(Long.class).inIndex(User.class)
                                                           .where("name", SearchFilters.is("John"))
                                                           .returnAll()
                                                           .now();
 
     assertThat(result.size(), is(0));
   }
 
   @Test(expected = NotConfiguredIndexingStrategyException.class)
   public void searchForObjectIdsWithoutGivenIndex() {
 
     context.checking(new Expectations() {{
       never(entityLoader);
     }});
 
     store(new User(1l, "John"));
 
     List<Long> result = searchEngine.searchIds(Long.class).where("name", SearchFilters.is("John")).returnAll().now();
 
     assertThat(result.size(), is(0));
   }
 
   @Test
   public void matchingBooleanFieldValues() {
 
     store(new Employee(1l, true));
 
     List<Employee> result = searchEngine.search(Employee.class).where("assigned", SearchFilters.is(true)).returnAll().now();
 
     assertThat(result.get(0).assigned, is(true));
   }
 
   @Test
   public void matchingPrivateFieldValue() {
 
     store(new User(1l, "John", "Adams"));
 
     List<User> result = searchEngine.search(User.class).where("family", SearchFilters.is("Adams")).returnAll().now();
 
     assertThat(result.get(0).id, is(1l));
   }
 
   @Test
   public void searchByOffset() {
 
     store(new User(1l, "Jack"));
     store(new User(2l, "Jack Briton"));
     store(new User(3l, "Jack Milar"));
 
     List<User> result = searchEngine.search(User.class).where("name", SearchFilters.is("Jack")).offset(1).returnAll().now();
 
     assertThat(result.size(), is(2));
     assertThat(result.get(0).name, is("Jack Briton"));
     assertThat(result.get(1).name, is("Jack Milar"));
   }
 
   @Test
   public void deleteOneIndexById(){
 
     store(new User(1l, "John"));
 
     searchEngine.delete(User.class.getSimpleName(), Lists.newArrayList(1l));
 
     List<Long> result = searchEngine.searchIds(Long.class).inIndex(User.class)
             .where("name", SearchFilters.is("John"))
             .returnAll()
             .now();
 
     assertThat(result.size(), is(0));
   }
 
   @Test
   public void deleteMoreThanOneIndexByIds(){
 
     store(new User(1l, "John"));
     store(new User(2l, "John"));
 
     searchEngine.delete(User.class.getSimpleName(), Lists.newArrayList(1l, 2l));
 
     List<Long> result = searchEngine.searchIds(Long.class).inIndex(User.class)
             .where("name", SearchFilters.is("John"))
             .returnAll()
             .now();
 
     assertThat(result.size(), is(0));
 
   }
 
   @Test
   public void searchByFieldWithMultipleValuesOneOfWhichMatchesAnotherIndexedFieldValue() {
 
     store(new Employee(1l, "John", "Adams"));
 
     List<Employee> result = searchEngine.search(Employee.class).where("firstName", isAnyOf("Someone", "Adams"))
                                                                .returnAll()
                                                                .now();
 
     assertThat(result.size(), is(0));
   }
 
   private void store(User... users) {
 
     for (User user : users) {
       repository.store(user.id, user);
       searchEngine.register(user);
     }
   }
 
   private void store(Employee... employees) {
 
     for (Employee employee : employees) {
       repository.store(employee.id, employee);
       searchEngine.register(employee);
     }
   }
 }
