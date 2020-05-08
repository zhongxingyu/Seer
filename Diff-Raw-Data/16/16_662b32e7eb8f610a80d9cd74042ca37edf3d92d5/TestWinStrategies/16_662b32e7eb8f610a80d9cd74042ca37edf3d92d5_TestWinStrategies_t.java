 package de.nordakademie.nakjava.gamelogic.winstrategies;
 
 import java.util.EnumMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import de.nordakademie.nakjava.gamelogic.cards.impl.Target;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.Artifact;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.infrastructure.Mauer;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.infrastructure.Turm;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.ressources.Kristalle;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.ressources.Monster;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.ressources.Ziegel;
 import de.nordakademie.nakjava.gamelogic.shared.playerstate.PlayerState;
 import de.nordakademie.nakjava.gamelogic.stateMachineEvenNewer.WinStrategy;
 import de.nordakademie.nakjava.gamelogic.stateMachineEvenNewer.winstrategies.RoundResult;
 import de.nordakademie.nakjava.gamelogic.stateMachineEvenNewer.winstrategies.WinStrategies;
import de.nordakademie.nakjava.util.classpathscanner.ClasspathScanner;
 
 public class TestWinStrategies {
 
 	private Map<Target, PlayerState> neutralModel;
 
 	@Before
 	public void setup() {
		ClasspathScanner.lookupAnnotatedScanners();		neutralModel = new EnumMap<>(Target.class);
 
 		List<Artifact> artifacts = new LinkedList<>();
 		Ziegel ziegel = new Ziegel();
 		ziegel.setCount(5);
 		Kristalle kristalle = new Kristalle();
 		kristalle.setCount(5);
 		Monster monster = new Monster();
 		monster.setCount(5);
 		Mauer mauer = new Mauer();
 		mauer.setCount(5);
 		Turm turm = new Turm();
 		turm.setCount(5);
 
 		artifacts.add(ziegel);
 		artifacts.add(kristalle);
 		artifacts.add(monster);
 		artifacts.add(turm);
 		artifacts.add(mauer);
 
 		PlayerState state = new PlayerState(artifacts);
 
 		neutralModel.put(Target.SELF, state);
 
 		artifacts = new LinkedList<>();
 		ziegel = new Ziegel();
 		ziegel.setCount(5);
 		kristalle = new Kristalle();
 		kristalle.setCount(5);
 		monster = new Monster();
 		monster.setCount(5);
 		mauer = new Mauer();
 		mauer.setCount(5);
 		turm = new Turm();
 		turm.setCount(5);
 
 		artifacts.add(ziegel);
 		artifacts.add(kristalle);
 		artifacts.add(monster);
 		artifacts.add(turm);
 		artifacts.add(mauer);
 
 		state = new PlayerState(artifacts);
 
 		neutralModel.put(Target.OPPONENT, state);
 	}
 
 	@Test
 	public void testExistance() {
 		Assert.assertNotNull(WinStrategies.getInstance());
 		Assert.assertEquals(true, !WinStrategies.getInstance().getStrategies()
 				.isEmpty());
 	}
 
 	@Test
 	public void testTurmbau() {
 		WinStrategy strategy = WinStrategies.getInstance().getStrategyForName(
 				"Turmbau");
 		Assert.assertNotNull(strategy);
 		Map<Target, RoundResult> results = strategy
 				.getRoundResult(neutralModel);
 		Assert.assertNotNull(results);
 
 		Assert.assertEquals(RoundResult.NEUTRAL, results.get(Target.SELF));
 		Assert.assertEquals(RoundResult.NEUTRAL, results.get(Target.OPPONENT));
 
 		neutralModel.get(Target.SELF).getTupelForClass(Turm.class)
 				.setCount(100);
 
 		results = strategy.getRoundResult(neutralModel);
 
 		Assert.assertEquals(RoundResult.WIN, results.get(Target.SELF));
 		Assert.assertEquals(RoundResult.LOST, results.get(Target.OPPONENT));
 	}
 
 	@Test
 	public void testSammelwut() {
 		WinStrategy strategy = WinStrategies.getInstance().getStrategyForName(
 				"Sammelwut");
 		Assert.assertNotNull(strategy);
 		Map<Target, RoundResult> results = strategy
 				.getRoundResult(neutralModel);
 		Assert.assertNotNull(results);
 
 		Assert.assertEquals(RoundResult.NEUTRAL, results.get(Target.SELF));
 		Assert.assertEquals(RoundResult.NEUTRAL, results.get(Target.OPPONENT));
 
 		neutralModel.get(Target.SELF).getTupelForClass(Ziegel.class).setCount(
 				100);
 		results = strategy.getRoundResult(neutralModel);
 
 		Assert.assertEquals(RoundResult.WIN, results.get(Target.SELF));
 		Assert.assertEquals(RoundResult.LOST, results.get(Target.OPPONENT));
 
 		neutralModel.get(Target.SELF).getTupelForClass(Turm.class).setCount(0);
 		results = strategy.getRoundResult(neutralModel);
 
 		// TODO is this right?
 		Assert.assertEquals(RoundResult.WIN, results.get(Target.SELF));
 		Assert.assertEquals(RoundResult.WIN, results.get(Target.OPPONENT));
 	}
 }
