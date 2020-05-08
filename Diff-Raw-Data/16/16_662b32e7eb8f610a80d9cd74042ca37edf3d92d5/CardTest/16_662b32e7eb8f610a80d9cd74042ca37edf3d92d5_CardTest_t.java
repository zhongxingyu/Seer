 package de.nordakademie.nakjava.gamelogic.cardstest;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.EnumMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import de.nordakademie.nakjava.gamelogic.cards.impl.AbstractCard;
 import de.nordakademie.nakjava.gamelogic.cards.impl.CardLibrary;
 import de.nordakademie.nakjava.gamelogic.cards.impl.Target;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.Artifact;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.infrastructure.Mauer;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.infrastructure.Turm;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.ressources.Kristalle;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.ressources.Monster;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.ressources.Ziegel;
 import de.nordakademie.nakjava.gamelogic.shared.cards.CardInformation;
 import de.nordakademie.nakjava.gamelogic.shared.playerstate.PlayerState;
import de.nordakademie.nakjava.util.classpathscanner.ClasspathScanner;
 
 public class CardTest {
 
 	private Map<Target, PlayerState> model;
 	Kristalle kristalle;
 
 	@Before
 	public void setUp() {
 		System.setProperty("de.nordakademie.nakjava.cardPackages",
 				"de.nordakademie.nakjava.gamelogic.cardstest");
 		model = new EnumMap<>(Target.class);
 
 		List<Artifact> initialArtifacts = new LinkedList<>();
 		Ziegel ziegel = new Ziegel();
 		ziegel.setCount(15);
 
 		kristalle = new Kristalle();
 		kristalle.setCount(25);
 
 		Monster monster = new Monster();
 		monster.setCount(10);
 
 		Mauer mauer = new Mauer();
 		mauer.setCount(3);
 
 		Turm turm = new Turm();
 		turm.setCount(5);
 
 		initialArtifacts.add(ziegel);
 		initialArtifacts.add(kristalle);
 		initialArtifacts.add(monster);
 		initialArtifacts.add(mauer);
 		initialArtifacts.add(turm);
 
 		PlayerState state = new PlayerState(initialArtifacts);
 
 		model.put(Target.SELF, state);
 	}
 
 	@Test
 	public void test() {
		ClasspathScanner.lookupAnnotatedScanners();
 		CardLibrary library = CardLibrary.get();
 
 		CardInformation information = library.getCardInformation().get(
 				"TestKarte");
 
 		assertNotNull(information);
 		testInformation(information);
 
 		AbstractCard card = library.getCards().get("TestKarte");
 		assertNotNull(card);
 
 		testCard(card);
 	}
 
 	private void testInformation(CardInformation information) {
 		assertEquals("TestKarte", information.getTitle());
 		assertEquals("-15 Monster für dich/zusätzlicherTest/", information
 				.getInformation());
 		assertEquals("15 Ziegel/30 Kristalle", information.getCost());
 	}
 
 	private void testCard(AbstractCard card) {
 		assertEquals(45, card.getTotalCosts());
 		// can not afford
 		assertEquals(card.checkPrerequirementsImpl(model), false);
 
 		kristalle.setCount(35);
 		assertEquals(card.checkPrerequirementsImpl(model), true);
 
 		card.payImpl(model);
 		assertEquals(0, model.get(Target.SELF).getTupelForClass(Ziegel.class)
 				.getCount());
 		assertEquals(5, model.get(Target.SELF)
 				.getTupelForClass(Kristalle.class).getCount());
 
 		card.performActionImpl(model);
 
 		// Generic Perfom && minimal Value = 0
 		assertEquals(0, model.get(Target.SELF).getTupelForClass(Monster.class)
 				.getCount());
 		// AdditionalAction
 		assertEquals(10, model.get(Target.SELF).getTupelForClass(
 				Kristalle.class).getCount());
 
 		// TestDamage
 		assertEquals(0, model.get(Target.SELF).getTupelForClass(Mauer.class)
 				.getCount());
 		assertEquals(3, model.get(Target.SELF).getTupelForClass(Turm.class)
 				.getCount());
 
 	}
 }
