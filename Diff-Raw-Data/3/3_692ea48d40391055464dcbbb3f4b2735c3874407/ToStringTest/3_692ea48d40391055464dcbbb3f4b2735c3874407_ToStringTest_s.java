 package herbstJennrichLehmannRitter.tests.model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import herbstJennrichLehmannRitter.engine.factory.GameCardFactory;
 import herbstJennrichLehmannRitter.engine.factory.impl.GameCardFactoryImpl;
 import herbstJennrichLehmannRitter.engine.model.Card;
 
 import org.junit.Test;
 
 public class ToStringTest {
 
 	private GameCardFactory gameCardFactory = new GameCardFactoryImpl();
 	
 	@Test
 	public void testArchitektur() {
 		Card architektur = this.gameCardFactory.createCard("Architektur");
 		assertNotNull(architektur);
 		assertEquals("Architektur[Kosten: 15 Ziegel; Selbst: +8 Mauer, +5 Turm]", architektur.toString());
 	}
 	
 	@Test
 	public void testAuferstehungComplextAction() {
 		Card auferstehung = this.gameCardFactory.createCard("Auferstehung");
 		assertNotNull(auferstehung);
 		System.out.println(auferstehung);
 		assertEquals("Auferstehung[Kosten: 6 Monster, 6 Kristall, 6 Ziegel; Selbst: +30% Turm]", auferstehung.toString());
 	}
 	
 	@Test
 	public void testBarrackeComplextAction() {
 		Card barracke = this.gameCardFactory.createCard("Barracke");
 		assertNotNull(barracke);
		assertEquals("Barracke[Kosten: 10 Ziegel; Selbst: +6 Mauer, +6 Monster, eigenes Verlies < gegnerisches Verlies => +1 Verlies]", barracke.toString());
 	}
 }
