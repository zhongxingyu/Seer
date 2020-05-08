 package br.com.caelum.argentum.modelo;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.junit.Test;
 
 public class NegociacaoTest {
 
 	@Test
 	public void dataDaNegociacaoEhImutavel() {
 
 		Calendar c = Calendar.getInstance();
 		c.set(Calendar.DAY_OF_MONTH, 15);
 
 		Negociacao n = new Negociacao(10, 5, c);
 
 		n.getData().set(Calendar.DAY_OF_MONTH, 20);
 
 		assertEquals(15, n.getData().get(Calendar.DAY_OF_MONTH));
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void naoCriaNegociacaoComDataNula() {
 		new Negociacao(10, 5, null);
 	}
 
 	@Test
 	public void mesmoMilisegundoEhDoMesmoDia() {
 		Calendar agora = Calendar.getInstance();
 		Calendar mesmoMomento = (Calendar) agora.clone();
 
 		Negociacao negociacao = new Negociacao(40.0, 100, agora);
 
 		assertTrue(negociacao.isMesmoDia(mesmoMomento));
 	}
 
 	@Test
 	public void comHorariosDiferentesEhNoMesmoDia() {
 		Calendar manha = new GregorianCalendar(2011, 10, 20, 8, 30);
 		Calendar tarde = new GregorianCalendar(2011, 10, 20, 15, 30);
 
 		Negociacao negociacao = new Negociacao(40.0, 100, manha);
 
 		assertTrue(negociacao.isMesmoDia(tarde));
 	}
 
 	@Test
 	public void mesmoDiaMasMesesDiferentesNaoSaoDoMesmoDia() {
 		Calendar mesNove = new GregorianCalendar(2011, 9, 20);
 		Calendar mesDez = new GregorianCalendar(2011, 10, 20);
 
 		Negociacao negociacao = new Negociacao(40.0, 100, mesNove);
 
 		assertFalse(negociacao.isMesmoDia(mesDez));
 	}
 
 	@Test
 	public void mesmoDiaMasAnosDiferentesNaoSaoDoMesmoDia() {
 		Calendar doisMilEDez = new GregorianCalendar(2010, 10, 20);
 		Calendar doisMilEOnze = new GregorianCalendar(2011, 10, 20);
 
 		Negociacao negociacao = new Negociacao(40.0, 100, doisMilEOnze);
 
 		assertFalse(negociacao.isMesmoDia(doisMilEDez));
 	}
 
 	@Test
 	public void paraNegociacoesDeTresDiasDistintosGeraTresCandles() {
 		Calendar hoje = Calendar.getInstance();
 
 		Negociacao negociacao1 = new Negociacao(40.5, 100, hoje);
 		Negociacao negociacao2 = new Negociacao(45.0, 100, hoje);
 		Negociacao negociacao3 = new Negociacao(39.8, 100, hoje);
 		Negociacao negociacao4 = new Negociacao(42.3, 100, hoje);
 
 		Calendar amanha = (Calendar) hoje.clone();
 		amanha.add(Calendar.DAY_OF_MONTH, 1);
 
 		Negociacao negociacao5 = new Negociacao(48.8, 100, amanha);
 		Negociacao negociacao6 = new Negociacao(49.3, 100, amanha);
 
 		Calendar depois = (Calendar) amanha.clone();
 		depois.add(Calendar.DAY_OF_MONTH, 1);
 
 		Negociacao negociacao7 = new Negociacao(51.8, 100, depois);
 		Negociacao negociacao8 = new Negociacao(52.3, 100, depois);
 
 		List<Negociacao> negociacoes = Arrays.asList(negociacao1, negociacao2,
 				negociacao3, negociacao4, negociacao5, negociacao6,
 				negociacao7, negociacao8);
 
 		CandlestickFactory fabrica = new CandlestickFactory();
 
 		List<Candlestick> candles = fabrica.constroiCandles(negociacoes);
 
 		assertEquals(3, candles.size());
 		assertEquals(40.5, candles.get(0).getAbertura(), 0.000001);
 		assertEquals(42.3, candles.get(0).getFechamento(), 0.000001);
 		assertEquals(48.8, candles.get(1).getAbertura(), 0.000001);
 		assertEquals(49.3, candles.get(1).getFechamento(), 0.00001);
 		assertEquals(51.8, candles.get(2).getAbertura(), 0.000001);
 		assertEquals(52.3, candles.get(2).getFechamento(), 0.00001);
 
 	}
 
	@Test(expected = IllegalStateException.class)
 	public void naoPermiteConstruirCandlesComNegociacoesForaDeOrdem() {
 		Calendar hoje = Calendar.getInstance();
 		Calendar amanha = (Calendar) hoje.clone();
 		amanha.add(Calendar.DAY_OF_MONTH, 1);
 		Calendar depois = (Calendar) amanha.clone();
 		depois.add(Calendar.DAY_OF_MONTH, 1);
 
 		Negociacao negociacao1 = new Negociacao(40.5, 100, hoje);
 		Negociacao negociacao2 = new Negociacao(45.0, 100, hoje);
 		Negociacao negociacao3 = new Negociacao(39.8, 100, hoje);
 		Negociacao negociacao4 = new Negociacao(42.3, 100, hoje);
 		Negociacao negociacao5 = new Negociacao(48.8, 100, amanha);
 		Negociacao negociacao6 = new Negociacao(49.3, 100, amanha);
 		Negociacao negociacao7 = new Negociacao(51.8, 100, depois);		
 		Negociacao negociacao8 = new Negociacao(52.3, 100, depois);
 
 		
 
 		List<Negociacao> negociacoes = Arrays.asList(negociacao8, negociacao5,
 				negociacao4, negociacao3, negociacao2, negociacao1,
 				negociacao6, negociacao7);
 
 		CandlestickFactory fabrica = new CandlestickFactory();
 
 		List<Candlestick> candles = fabrica.constroiCandles(negociacoes);
 
 		assertEquals(3, candles.size());
		assertEquals(40.5, candles.get(0).getAbertura(), 0.000001);
		assertEquals(42.3, candles.get(0).getFechamento(), 0.000001);
 		assertEquals(48.8, candles.get(1).getAbertura(), 0.000001);
 		assertEquals(49.3, candles.get(1).getFechamento(), 0.00001);
		assertEquals(51.8, candles.get(2).getAbertura(), 0.000001);
		assertEquals(52.3, candles.get(2).getFechamento(), 0.00001);
 
 	}
 
 	@Test
 	public void constroiCandlesOrdenadasPorData() {
 		Calendar hoje = Calendar.getInstance();
 		Calendar amanha = (Calendar) hoje.clone();
 		amanha.add(Calendar.DAY_OF_MONTH, 1);
 		Calendar depois = (Calendar) amanha.clone();
 		depois.add(Calendar.DAY_OF_MONTH, 1);
 
 		Negociacao negociacao1 = new Negociacao(40.5, 100, hoje);
 		Negociacao negociacao2 = new Negociacao(45.0, 100, hoje);
 		Negociacao negociacao3 = new Negociacao(39.8, 100, hoje);
 		Negociacao negociacao4 = new Negociacao(42.3, 100, hoje);
 		Negociacao negociacao5 = new Negociacao(48.8, 100, amanha);
 		Negociacao negociacao6 = new Negociacao(49.3, 100, amanha);
 		Negociacao negociacao7 = new Negociacao(51.8, 100, depois);
 		Negociacao negociacao8 = new Negociacao(52.3, 100, depois);
 
 		List<Negociacao> negociacoes = Arrays.asList(negociacao5, negociacao7,
 				negociacao1, negociacao4, negociacao3, negociacao2,
 				negociacao8, negociacao6);
 
 		CandlestickFactory fabrica = new CandlestickFactory();
 
 		List<Candlestick> candles = fabrica.constroiCandles(negociacoes);
 
 		assertEquals(3, candles.size());
 		assertEquals(hoje, candles.get(0).getData());
 		assertEquals(amanha, candles.get(1).getData());
 		assertEquals(depois, candles.get(2).getData());
 
 	}
 
 }
