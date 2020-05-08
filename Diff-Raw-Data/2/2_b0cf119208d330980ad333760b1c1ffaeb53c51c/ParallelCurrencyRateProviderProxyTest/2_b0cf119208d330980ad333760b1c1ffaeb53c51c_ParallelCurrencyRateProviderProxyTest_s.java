 package test.strangeforest.currencywatch.unit;
 
 import java.util.*;
 
 import org.junit.*;
 import org.mockito.*;
 import org.mockito.invocation.*;
 import org.mockito.stubbing.*;
 import org.strangeforest.currencywatch.core.*;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import static test.strangeforest.currencywatch.TestData.*;
 
 public class ParallelCurrencyRateProviderProxyTest {
 
 	@Test
 	public void getRate() {
 		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
 		when(provider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE)).thenReturn(RATE);
 		CurrencyRateListener listener = mock(CurrencyRateListener.class);
 
 		try (ParallelCurrencyRateProviderProxy parallelProvider = createParallelProvider(provider, listener, 1)) {
 			RateValue rate = parallelProvider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE);
 
 			assertEquals(RATE, rate);
 			verify(listener).newRate(any(CurrencyRateEvent.class));
 		}
 	}
 
 	@Test
 	public void getRates() {
 		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
 		when(provider.getRate(eq(SYMBOL_FROM), eq(SYMBOL_TO), any(Date.class))).thenAnswer(new Answer<Object>() {
 			@Override public Object answer(InvocationOnMock invocation) throws Throwable {
 				return RATES.get(invocation.getArguments()[2]);
 			}
 		});
 		CurrencyRateListener listener = mock(CurrencyRateListener.class);
 
 		try (ParallelCurrencyRateProviderProxy parallelProvider = createParallelProvider(provider, listener, 2)) {
 			Map<Date, RateValue> rates = parallelProvider.getRates(SYMBOL_FROM, SYMBOL_TO, RATES.keySet());
 
 			assertEquals(RATES, rates);
 			verify(listener, times(RATES.size())).newRate(any(CurrencyRateEvent.class));
 		}
 	}
 
 	@Test
 	public void getRatesRetried() {
 		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
 		when(provider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE)).thenThrow(new CurrencyRateException("Booom!")).thenReturn(RATE);
 		CurrencyRateListener listener = mock(CurrencyRateListener.class);
 
 		try (ParallelCurrencyRateProviderProxy parallelProvider = createParallelProvider(provider, listener, 1)) {
 			Map<Date, RateValue> rates = parallelProvider.getRates(SYMBOL_FROM, SYMBOL_TO, Collections.singleton(DATE));
 
 			assertEquals(Collections.singletonMap(DATE, RATE), rates);
 			verify(provider, times(2)).getRate(SYMBOL_FROM, SYMBOL_TO, DATE);
 			verify(listener).newRate(any(CurrencyRateEvent.class));
 		}
 	}
 
 	@Test
 	public void getRatesRetryFail() {
 		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
 		when(provider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE)).thenThrow(new CurrencyRateException("Booom!"));
 		CurrencyRateListener listener = mock(CurrencyRateListener.class);
 
 		try (ParallelCurrencyRateProviderProxy parallelProvider = createParallelProvider(provider, listener, 1)) {
 			try {
 				parallelProvider.getRates(SYMBOL_FROM, SYMBOL_TO, Collections.singleton(DATE));
 				fail("Exception is not thrown.");
 			}
 			catch (CurrencyRateException ex) {
 				InOrder inOrder = inOrder(provider);
 				inOrder.verify(provider).init();
 				inOrder.verify(provider, times(3)).getRate(SYMBOL_FROM, SYMBOL_TO, DATE);
 				inOrder.verify(provider).close();
 				inOrder.verify(provider).init();
 				verify(listener, never()).newRate(any(CurrencyRateEvent.class));
 			}
 		}
 	}
 
 	private ParallelCurrencyRateProviderProxy createParallelProvider(CurrencyRateProvider provider, CurrencyRateListener listener, int threadCount) {
 		ParallelCurrencyRateProviderProxy parallelProvider = new ParallelCurrencyRateProviderProxy(provider, threadCount);
 		parallelProvider.addListener(listener);
 		parallelProvider.init();
 		return parallelProvider;
 	}
 }
