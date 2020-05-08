 package net.joala.newdata.random;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Answers;
 import org.mockito.Mock;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.inject.Provider;
 import java.util.Random;
 
 import static net.joala.testlet.ToStringTestlet.toStringTestlet;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assume.assumeThat;
 import static org.powermock.api.mockito.PowerMockito.when;
 
 /**
  * <p>
  * Tests {@link AbstractRandomNumberProvider}.
  * </p>
  *
  * @since 10/24/12
  */
 @SuppressWarnings("ProhibitedExceptionDeclared")
 @RunWith(PowerMockRunner.class)
 public class AbstractRandomNumberProviderTest {
   private static final Long SOME_MINUMUM_VALUE = 23L;
   private static final Long SOME_MAXIMUM_VALUE = SOME_MINUMUM_VALUE * 2;
   /**
    * Implement abstract class by mocking.
    *
    * @see <a href="http://stackoverflow.com/questions/1087339/using-mockito-to-test-abstract-classes">Stackoverflow:
    *      Using Mockito to test abstract classes</a>
    */
   @Mock(answer = Answers.CALLS_REAL_METHODS)
   private AbstractRandomNumberProvider<Long> randomNumberProvider;
   @Mock
   private Provider<Random> randomProvider;
 
   // ========== S C E N A R I O S ========== //
 
   @Test
   public void provided_min_value_should_be_used_for_calculation() throws Exception {
     final PoorMansReference<Long> V = ref();
     final PoorMansReference<Class<?>> T = ref();
     final PoorMansReference<MockedProvider> P = ref();
 
     given_a_minimum_value_V_of_and_type_T_for_random_numbers(V, T);
     when_I_create_a_random_number_provider_P_of_type_T_with_minimum_value_V(P, T, V);
     then_all_created_numbers_by_provider_P_are_greater_than_or_equal_to_the_minimum_value_V(P, V);
   }
 
   @Test
   public void no_provided_min_value_should_use_default_for_calculation() throws Exception {
     final PoorMansReference<Long> V = ref();
     final PoorMansReference<Class<?>> T = ref();
     final PoorMansReference<MockedProvider> P = ref();
 
     given_a_default_minimum_value_V_of_and_type_T_for_random_numbers(V, T);
     when_I_create_a_random_number_provider_P_of_type_T_with_no_minimum_value(P, T);
     then_all_created_numbers_by_provider_P_are_greater_than_or_equal_to_the_default_minimum_value_V(P, V);
   }
 
   @Test
   public void provided_max_value_should_be_used_for_calculation() throws Exception {
     final PoorMansReference<Long> V = ref();
     final PoorMansReference<Class<?>> T = ref();
     final PoorMansReference<MockedProvider> P = ref();
 
     given_a_maximum_value_V_of_and_type_T_for_random_numbers(V, T);
     when_I_create_a_random_number_provider_P_of_type_T_with_maximum_value_V(P, T, V);
     then_all_created_numbers_by_provider_P_are_less_than_or_equal_to_the_maximum_value_V(P, V);
   }
 
   @Test
   public void no_provided_max_value_should_use_default_for_calculation() throws Exception {
     final PoorMansReference<Long> V = ref();
     final PoorMansReference<Class<?>> T = ref();
     final PoorMansReference<MockedProvider> P = ref();
 
     given_a_default_maximum_value_V_of_and_type_T_for_random_numbers(V, T);
     when_I_create_a_random_number_provider_P_of_type_T_with_no_maximum_value(P, T);
     then_all_created_numbers_by_provider_P_are_less_than_or_equal_to_the_default_maximum_value_V(P, V);
   }
 
   @Test
   public void use_default_random_provider_if_none_configured() throws Exception {
     final PoorMansReference<MockedProvider> P = ref();
     when_I_create_a_random_number_provider_P_without_a_random_generator(P);
     then_a_default_random_generator_should_be_used_for_random_number_generation(P);
   }
 
   @Test
   public void use_provided_random_provider() throws Exception {
     final PoorMansReference<Random> R = ref();
     final PoorMansReference<MockedProvider> P = ref();
     given_random_generator_R(R);
     when_I_create_a_random_number_provider_P_with_random_generator_R(P, R);
    then_random_generator_R_is_used_for_random_numbers_created_with_provider_P(P, R);
   }
 
   @Test
   public void toString_should_meet_requirements() throws Throwable {
     PowerMockito.doCallRealMethod().when(randomNumberProvider, "toString");
     toStringTestlet(randomNumberProvider)
             .ignoreClassname()
             .fieldsFromClass(AbstractRandomNumberProvider.class)
             .run();
   }
 
   // ========== S T E P S ========== //
 
   private void given_a_minimum_value_V_of_and_type_T_for_random_numbers(final PoorMansReference<Long> v,
                                                                         final PoorMansReference<Class<?>> t) {
     v.set(SOME_MINUMUM_VALUE);
     t.set(SOME_MINUMUM_VALUE.getClass());
   }
 
   private void given_a_maximum_value_V_of_and_type_T_for_random_numbers(final PoorMansReference<Long> v,
                                                                         final PoorMansReference<Class<?>> t) {
     v.set(SOME_MAXIMUM_VALUE);
     t.set(SOME_MAXIMUM_VALUE.getClass());
   }
 
   private void given_a_default_minimum_value_V_of_and_type_T_for_random_numbers(
           final PoorMansReference<Long> v,
           final PoorMansReference<Class<?>> t) {
     v.set(MockedProvider.DEFAULT_MIN);
     t.set(MockedProvider.DEFAULT_MIN.getClass());
   }
 
   private void given_a_default_maximum_value_V_of_and_type_T_for_random_numbers(
           final PoorMansReference<Long> v,
           final PoorMansReference<Class<?>> t) {
     v.set(MockedProvider.DEFAULT_MAX);
     t.set(MockedProvider.DEFAULT_MAX.getClass());
   }
 
   private void given_random_generator_R(final PoorMansReference<Random> r) {
     r.set(new MockRandom());
   }
 
   private void when_I_create_a_random_number_provider_P_of_type_T_with_minimum_value_V(
           final PoorMansReference<MockedProvider> p,
           final PoorMansReference<Class<?>> t,
           final PoorMansReference<Long> v) {
     assumeThat(t.get(), equalTo((Object) Long.class)); // other types not supported by test
     final Long minimum = v.get();
     p.set(new MockedProvider(minimum, null, null));
   }
 
   private void when_I_create_a_random_number_provider_P_of_type_T_with_maximum_value_V(
           final PoorMansReference<MockedProvider> p,
           final PoorMansReference<Class<?>> t,
           final PoorMansReference<Long> v) {
     assumeThat(t.get(), equalTo((Object) Long.class)); // other types not supported by test
     final Long maximum = v.get();
     p.set(new MockedProvider(null, maximum, null));
   }
 
   private void when_I_create_a_random_number_provider_P_of_type_T_with_no_minimum_value(
           final PoorMansReference<MockedProvider> p,
           final PoorMansReference<Class<?>> t) {
     assumeThat(t.get(), equalTo((Object) Long.class)); // other types not supported by test
     p.set(new MockedProvider(null, null, null));
   }
 
   private void when_I_create_a_random_number_provider_P_of_type_T_with_no_maximum_value(
           final PoorMansReference<MockedProvider> p,
           final PoorMansReference<Class<?>> t) {
     assumeThat(t.get(), equalTo((Object) Long.class)); // other types not supported by test
     p.set(new MockedProvider(null, null, null));
   }
 
   private void when_I_create_a_random_number_provider_P_without_a_random_generator(
           final PoorMansReference<MockedProvider> p) {
     p.set(new MockedProvider(null, null, null));
   }
 
   private void when_I_create_a_random_number_provider_P_with_random_generator_R(
           final PoorMansReference<MockedProvider> p,
           final PoorMansReference<Random> r) {
     when(randomProvider.get()).thenReturn(r.get());
     p.set(new MockedProvider(null, null, randomProvider));
   }
 
   private void then_all_created_numbers_by_provider_P_are_greater_than_or_equal_to_the_minimum_value_V(
           final PoorMansReference<MockedProvider> p,
           final PoorMansReference<Long> v) {
     final MockedProvider numberProvider = p.get();
     numberProvider.get();
     assertEquals("Minimum number should have been used as configured.", v.get(), numberProvider.getCapturedMinValue());
   }
 
   private void then_all_created_numbers_by_provider_P_are_less_than_or_equal_to_the_maximum_value_V(
           final PoorMansReference<MockedProvider> p,
           final PoorMansReference<Long> v) {
     final MockedProvider numberProvider = p.get();
     numberProvider.get();
     assertEquals("Maximum number should have been used as configured.", v.get(), numberProvider.getCapturedMaxValue());
   }
 
   private void then_all_created_numbers_by_provider_P_are_greater_than_or_equal_to_the_default_minimum_value_V(
           final PoorMansReference<MockedProvider> p,
           final PoorMansReference<Long> v) {
     final MockedProvider numberProvider = p.get();
     numberProvider.get();
     assertEquals("Default minimum number should have been used as configured.",
             v.get(),
             numberProvider.getCapturedMinValue());
   }
 
   private void then_all_created_numbers_by_provider_P_are_less_than_or_equal_to_the_default_maximum_value_V(
           final PoorMansReference<MockedProvider> p,
           final PoorMansReference<Long> v) {
     final MockedProvider numberProvider = p.get();
     numberProvider.get();
     assertEquals("Default maximum number should have been used as configured.",
             v.get(),
             numberProvider.getCapturedMaxValue());
   }
 
   private void then_a_default_random_generator_should_be_used_for_random_number_generation(
           final PoorMansReference<MockedProvider> p) {
     final MockedProvider numberProvider = p.get();
     numberProvider.get();
     assertThat(numberProvider.getCapturedPercentage(),
             allOf(greaterThanOrEqualTo(0d),
                     lessThanOrEqualTo(1d)
             )
     );
   }
 
  private void then_random_generator_R_is_used_for_random_numbers_created_with_provider_P(
           final PoorMansReference<MockedProvider> p,
           final PoorMansReference<Random> r) {
     final MockedProvider mockedProvider = p.get();
     mockedProvider.get();
     assertThat(mockedProvider.getCapturedPercentage(), equalTo(MockRandom.FIXED_RANDOM_DOUBLE));
   }
 
   private static class MockedProvider extends AbstractRandomNumberProvider<Long> {
     private static final Long DEFAULT_MAX = Long.MAX_VALUE;
     private static final Long DEFAULT_MIN = Long.MIN_VALUE;
     private Long capturedMinValue;
     private Long capturedMaxValue;
     private Double capturedPercentage;
 
     /**
      * <p>
      * Constructor setting the several values. Especially meant to enable to
      * configure this random number provider through Spring or any other IOC
      * framework. Recommended to override and to make it public in the
      * implementing class.
      * </p>
      *
      * @param minValue       minimum value; {@code null} to use default
      * @param maxValue       maximum value; {@code null} to use default
      * @param randomProvider provider for random numbers; {@code null} to use default
      */
     private MockedProvider(@Nullable final Long minValue,
                            @Nullable final Long maxValue,
                            @Nullable final Provider<Random> randomProvider) {
       super(minValue, maxValue, randomProvider);
     }
 
     public Long getCapturedMaxValue() {
       return capturedMaxValue;
     }
 
     public Long getCapturedMinValue() {
       return capturedMinValue;
     }
 
     public Double getCapturedPercentage() {
       return capturedPercentage;
     }
 
     @Nonnull
     @Override
     protected Long getMaxDefault() {
       return DEFAULT_MAX;
     }
 
     @Nonnull
     @Override
     protected Long getMinDefault() {
       return DEFAULT_MIN;
     }
 
     @Nonnull
     @Override
     protected Long get(@Nonnull final Long minValue, @Nonnull final Long maxValue, final double percentage) {
       capturedMinValue = minValue;
       capturedMaxValue = maxValue;
       capturedPercentage = percentage;
       return -1L; // just some value; ignore
     }
 
     @Nonnull
     @Override
     public FluentNumberRange<Long> min(@Nonnull final Long minValue) {
       throw new UnsupportedOperationException("Unsupported here.");
     }
 
     @Nonnull
     @Override
     public FluentNumberRange<Long> max(@Nonnull final Long maxValue) {
       throw new UnsupportedOperationException("Unsupported here.");
     }
   }
 
 
   private static <T> PoorMansReference<T> ref() {
     return new PoorMansReference<T>();
   }
 
   // In order to prevent cyclic dependencies to joala-data we have to use our
   // own reference here.
   private static final class PoorMansReference<T> {
     private T value;
 
     public T get() {
       return value;
     }
 
     public void set(final T value) {
       this.value = value;
     }
   }
 
   private static class MockRandom extends Random {
     private static final Double FIXED_RANDOM_DOUBLE = -1d;
 
     @Override
     public double nextDouble() {
       return FIXED_RANDOM_DOUBLE;
     }
   }
 }
