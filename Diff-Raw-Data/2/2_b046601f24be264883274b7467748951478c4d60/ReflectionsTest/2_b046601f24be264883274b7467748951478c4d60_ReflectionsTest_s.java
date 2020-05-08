 package pl.jsolve.sweetener.core;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static pl.jsolve.sweetener.tests.stub.hero.HeroBuilder.aHero;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.List;
 
 import org.junit.Test;
 
 import pl.jsolve.sweetener.collection.data.Person;
 import pl.jsolve.sweetener.mapper.annotation.MapExactlyTo;
 import pl.jsolve.sweetener.tests.stub.hero.Hero;
 
 public class ReflectionsTest {
 
 	private static final String HERO_ID_FIELD_NAME = "id";
 	private static final String GETTER_METHODS_PREFIX = "get";
 	private static final String HERO_GET_NICKNAME_METHOD_NAME = "getNickname";
 	private static final String HERO_GET_LAST_NAME_METHOD_NAME = "getLastName";
 	private static final String HERO_GET_FIRST_NAME_METHOD_NAME = "getFirstName";
 	private static final String HERO_GET_ID_METHOD_NAME = "getId";
 	private static final String HERO_LAST_NAME_FIELD_NAME = "lastName";
 	private static final String HERO_FIRST_NAME_FIELD_NAME = "firstName";
 	private static final String HERO_NICKNAME_FIELD_NAME = "nickname";
 	private static final String NAME = "John";
 	private static final String COMPANY_NAME = "Jsolve";
 
 	@Test
 	public void shouldSetValueToObject() {
 		// given
 		Person person = new Person();
 
 		// when
 		Reflections.setFieldValue(person, "name", NAME);
 
 		// then
 		assertThat(person.getName()).isEqualTo(NAME);
 	}
 
 	@Test
 	public void shouldSetValueToNestedObject() {
 		// given
 		Person person = new Person();
 
 		// when
 		Reflections.setFieldValue(person, "company.name", COMPANY_NAME);
 
 		// then
 		assertThat(person.getCompany().getName()).isEqualTo(COMPANY_NAME);
 	}
 
 	@Test
 	public void shouldGetAllClassesForGivenObject() {
 		// given
 		Person person = new Person();
 
 		// when
 		List<Class<?>> classes = Reflections.getClasses(person);
 
 		// then
 		assertThat(classes).hasSize(2);
 		assertThat(classes).containsSequence(Person.class, Object.class);
 	}
 
 	@Test
 	public void shouldGetClassesSatisfyingCondition() {
 		// given
 		Person person = new Person();
 		Condition<Class<?>> classesOtherThanObjectCondition = new Condition<Class<?>>() {
 
 			@Override
 			public boolean isSatisfied(Class<?> clazz) {
 				return clazz != Object.class;
 			}
 		};
 
 		// when
 		List<Class<?>> classes = Reflections.getClassesSatisfyingCondition(person, classesOtherThanObjectCondition);
 
 		// then
 		assertThat(classes).excludes(Object.class);
 	}
 
 	@Test
 	public void shouldGetAllFieldsForGivenObject() {
 		// given
 		Person person = new Person();
 
 		// when
 		List<Field> fields = Reflections.getFields(person);
 
 		// then
 		assertThat(fields).hasSize(5);
 	}
 
 	@Test
 	public void shouldGetFieldsSatisfyingCondition() throws Exception {
 		// given
 		Hero hero = aHero().build();
 		Condition<Field> fieldContainingNameCondition = new Condition<Field>() {
 			@Override
 			public boolean isSatisfied(Field field) {
 				return field.getName().toLowerCase().contains("name");
 			}
 		};
 
 		// when
 		List<Field> fields = Reflections.getFieldsSatisfyingCondition(hero, fieldContainingNameCondition);
 
 		// then
 		Field heroNicknameField = hero.getClass().getDeclaredField(HERO_NICKNAME_FIELD_NAME);
 		Field heroFirstNameField = hero.getClass().getDeclaredField(HERO_FIRST_NAME_FIELD_NAME);
 		Field heroLastNameField = hero.getClass().getDeclaredField(HERO_LAST_NAME_FIELD_NAME);
 		assertThat(fields).containsOnly(heroNicknameField, heroFirstNameField, heroLastNameField);
 	}
 
 	@Test
 	public void shouldGetAllAnnotatedFields() throws Exception {
 		// given
 		Hero hero = aHero().build();
 
 		// when
 		List<Field> fields = Reflections.getFieldsAnnotatedBy(hero, MapExactlyTo.class);
 
 		// then
 		Field heroIdField = hero.getClass().getDeclaredField(HERO_ID_FIELD_NAME);
 		assertThat(fields).as("hero class has `id` field annotated by MapExactlyTo").contains(heroIdField);
 	}
 
 	@Test
 	public void shouldNotGetNotAnnotatedFields() {
 		// given
 		Hero hero = aHero().build();
 
 		// when
 		List<Field> fields = Reflections.getFieldsAnnotatedBy(hero, Deprecated.class);
 
 		// then
 		assertThat(fields).as("hero class does not have deprecated fields").isEmpty();
 	}
 
 	@Test
 	public void shouldGetAllConstructors() {
 		// given
 		Person person = new Person();
 
 		// when
 		List<Constructor<?>> constructors = Reflections.getConstructors(person);
 
 		// then
 		assertThat(constructors).hasSize(2);
 	}
 
 	@Test
 	public void shouldGetConstructorSatisfyingCondtion() throws Exception {
 		// given
 		Person person = new Person();
 		Condition<Constructor<?>> constructorsWithoutParametersCondition = new Condition<Constructor<?>>() {
 			@Override
 			public boolean isSatisfied(Constructor<?> constructor) {
 				return constructor.getParameterTypes().length == 0;
 			}
 		};
 
 		// when
 		List<Constructor<?>> constructors = Reflections.getConstructorsSatisfyingCondition(person,
 				constructorsWithoutParametersCondition);
 
 		// then
 		assertThat(constructors).as("person class has only one constructor with no parameters").hasSize(1);
 	}
 
 	@Test
 	public void shouldGetAllAnnotations() {
 		// given
 		Person person = new Person();
 
 		// when
 		List<Annotation> annotations = Reflections.getAnnotations(person);
 
 		// then
 		assertThat(annotations).isEmpty();
 	}
 
 	@Test
 	public void shouldGetAllMethods() {
 		// given
 		Person person = new Person();
 
 		// when
 		List<Method> methods = Reflections.getMethods(person);
 
 		// then
		assertThat(methods).hasSize(10);
 	}
 
 	@Test
 	public void shouldGetMethodsSatisfyingCondtion() throws Exception {
 		// given
 		Hero hero = aHero().build();
 		Condition<Method> getterMethodsCondition = new Condition<Method>() {
 			@Override
 			public boolean isSatisfied(Method method) {
 				return method.getName().startsWith(GETTER_METHODS_PREFIX);
 			}
 		};
 
 		// when
 		List<Method> methods = Reflections.getMethodsSatisfyingCondition(hero, getterMethodsCondition);
 
 		// then
 		Method heroGetIdMethod = hero.getClass().getDeclaredMethod(HERO_GET_ID_METHOD_NAME);
 		Method heroGetFirstNameMethod = hero.getClass().getDeclaredMethod(HERO_GET_FIRST_NAME_METHOD_NAME);
 		Method heroGetLastNameMethod = hero.getClass().getDeclaredMethod(HERO_GET_LAST_NAME_METHOD_NAME);
 		Method heroGetNicknameMethod = hero.getClass().getDeclaredMethod(HERO_GET_NICKNAME_METHOD_NAME);
 		assertThat(methods).contains(heroGetIdMethod, heroGetFirstNameMethod, heroGetLastNameMethod, heroGetNicknameMethod);
 	}
 
 	@Test
 	public void shouldNotGetNotAnnotatedMethods() {
 		// given
 		Hero hero = aHero().build();
 
 		// when
 		List<Method> methods = Reflections.getMethodsAnnotatedBy(hero, Deprecated.class);
 
 		// then
 		assertThat(methods).as("hero class is does not have deprecated methods").isEmpty();
 	}
 }
