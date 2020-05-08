 package herbstJennrichLehmannRitter.engine.model.xml;
 
 import herbstJennrichLehmannRitter.engine.enums.CardType;
 import herbstJennrichLehmannRitter.engine.model.action.CardAction;
 import herbstJennrichLehmannRitter.engine.model.action.ComplexCardAction;
 import herbstJennrichLehmannRitter.engine.model.action.ResourceAction;
import herbstJennrichLehmannRitter.engine.model.impl.AbstractCard;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlID;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 @XmlRootElement(name="Card")
public class XmlCard extends AbstractCard {
 
 	private String name;
 	private CardType cardType;
 	private int costBrick;
 	private int costMonster;
 	private int costCrystal;
 	private CardAction cardAction;
 	private ResourceAction ownResourceAction;
 	private ResourceAction enemyResourceAction;
 	private ComplexCardAction complexCardAction;
	private boolean canBeDiscarded = true;
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@XmlID
 	@XmlAttribute(name="name", required=true)
 	@Override
 	public String getName() {
 		return this.name;
 	}
 	
 	public void setCardType(CardType cardType) {
 		this.cardType = cardType;
 	}
 
 	@XmlElement(name="CardType", required=true)
 	@Override
 	public CardType getCardType() {
 		return this.cardType;
 	}
 
 	public void setCostBrick(int costBrick) {
 		this.costBrick = costBrick;
 	}
 
 	@XmlElement(name="CostBrick")
 	@Override
 	public int getCostBrick() {
 		return this.costBrick;
 	}
 
 	public void setCostMonsters(int costMonsters) {
 		this.costMonster = costMonsters;
 	}
 
 	@XmlElement(name="CostMonster")
 	@Override
 	public int getCostMonsters() {
 		return this.costMonster;
 	}
 
 	public void setCostCrystal(int costCrystal) {
 		this.costCrystal = costCrystal;
 	}
 
 	@XmlElement(name="CostCrystal")
 	@Override
 	public int getCostCrystal() {
 		return this.costCrystal;
 	}
 
 	public void setOwnResourceAction(ResourceAction ownResourceAction) {
 		this.ownResourceAction = ownResourceAction;
 	}
 
 	@XmlElement(name="OwnResourceAction", type=XmlResourceAction.class)
 	@Override
 	public ResourceAction getOwnResourceAction() {
 		return this.ownResourceAction;
 	}
 	
 	public void setEnemyResourceAction(ResourceAction enemyResourceAction) {
 		this.enemyResourceAction = enemyResourceAction;
 	}
 
 	@XmlElement(name="EnemyResourceAction", type=XmlResourceAction.class)
 	@Override
 	public ResourceAction getEnemyResourceAction() {
 		return this.enemyResourceAction;
 	}
 
 	public void setComplexCardAction(ComplexCardAction complexCardAction) {
 		this.complexCardAction = complexCardAction;
 	}
 
 	@XmlTransient
 	@Override
 	public ComplexCardAction getComplexCardAction() {
 		return this.complexCardAction;
 	}
 	
 	public void setCardAction(CardAction cardAction) {
 		this.cardAction = cardAction;
 	}
 
 	@XmlElement(name="CardEffect", type=XmlCardAction.class)
 	@Override
 	public CardAction getCardAction() {
 		return this.cardAction;
 	}
 	
 	public void setCanBeDiscarded(boolean canBeDiscarded) {
 		this.canBeDiscarded = canBeDiscarded;
 	}
 	
	@XmlElement(name="CanBeDiscarded", defaultValue="true")
 	@Override
 	public boolean getCanBeDiscarded() {
 		return this.canBeDiscarded;
 	}
 
 }
