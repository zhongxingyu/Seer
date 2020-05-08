 package de.nordakademie.nakjava.gamelogic.cards.verlies;
 
 import java.util.Map;
 
 import de.nordakademie.nakjava.gamelogic.cards.impl.AbstractCard;
 import de.nordakademie.nakjava.gamelogic.cards.impl.ArtifactEffect;
 import de.nordakademie.nakjava.gamelogic.cards.impl.Card;
 import de.nordakademie.nakjava.gamelogic.cards.impl.Cost;
 import de.nordakademie.nakjava.gamelogic.cards.impl.Target;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.Artifact;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.ArtifactFactory;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.ressources.Kristalle;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.ressources.Monster;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.ressources.Ziegel;
 import de.nordakademie.nakjava.gamelogic.shared.cards.CardType;
 import de.nordakademie.nakjava.gamelogic.shared.playerstate.PlayerState;
 import de.nordakademie.nakjava.server.internal.model.InGameSpecificModel;
 
 @Card(name = "Dieb",
 		type = CardType.VERLIES,
 		costs = { @Cost(amount = 12,
 				ressource = Monster.class) },
 		artifactEffects = { @ArtifactEffect(artifact = Kristalle.class,
 				count = -10,
 				target = Target.OPPONENT),
 				@ArtifactEffect(artifact = Ziegel.class,
 						count = -5,
 						target = Target.OPPONENT) },
 		additionalDescription = "Die Hälfte des Verlusts erhält man selbst (aufgerundet).")
 public class Dieb extends AbstractCard {
 
 	@Override
 	protected void performAction(Map<Target, PlayerState> states) {
 		super.performAction(states);
 		gainHalfOfOpponentsLostArtifacts(states, Kristalle.class, 10);
 		gainHalfOfOpponentsLostArtifacts(states, Ziegel.class, 5);
 	}
 
 	private void gainHalfOfOpponentsLostArtifacts(
 			Map<Target, PlayerState> states, Class<? extends Artifact> class1,
 			int maxValue) {
 		InGameSpecificModel selfModel = (InGameSpecificModel) states.get(
 				Target.SELF).getStateSpecificModel();
 		InGameSpecificModel opponentModel = (InGameSpecificModel) states.get(
 				Target.OPPONENT).getStateSpecificModel();
 
		Kristalle opponentKristall = (Kristalle) opponentModel
				.getTupelForClass(class1);
 		int opponentDamage;
		if (opponentKristall.getCount() < maxValue) {
			opponentDamage = opponentKristall.getCount();
 		} else {
 			opponentDamage = maxValue;
 		}
		int gainedKristall = (int) Math.ceil(((float) opponentDamage) / 2);
 		selfModel.getTupelForClass(class1).merge(
				ArtifactFactory.createArtifact(class1, gainedKristall));
 	}
 }
